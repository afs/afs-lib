/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package structure.skiplist;

import static java.lang.String.format ;

import java.util.Iterator ;
import java.util.Random ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.RandomLib ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

final
public class SkipList <R extends Comparable<? super R>> implements Printable, Iterable<R>
{
    static public boolean Checking = false ;
    static public /*final*/ boolean Logging = false ;       // Even faster than log.isDebugEnabled.
    private static Logger log = LoggerFactory.getLogger(SkipList.class) ;
    
    // Expensive?  Do a caching call?
    private static Random rand = RandomLib.qrandom ;  
    static int DftMaxLevel = 20 ;
    
    // This list
    int maxLevel = DftMaxLevel ;    // Maximum levels allowed for this list
    int currentLevel = 0 ;          // Current largest in-use level
    // This node is special.  It does not have a record - it's just the forward pointers.
    SkipListNode<R> root = null ;
    int size = 0 ;
    //private int randomSeed;

    //SkipListNode<R> infinity = new SkipListNode<R>(null, 0) ; -- use nulls instead.
    
    public SkipList()
    {
        root = new SkipListNode<R>(null, DftMaxLevel) ;
    }
    
    public SkipList(int maxLevel)
    {
        this.maxLevel = maxLevel ;
        this.currentLevel = 0 ;
        root = new SkipListNode<R>(null, maxLevel) ;
        //randomSeed = RandomLib.random.nextInt() | 0x0100; // ensure nonzero
    }
    
    public boolean contains(R record)
    { return find(record) != null ; }
    
    public R find(R record)
    {
        if ( record == null )
            return null ;
        
        SkipListNode<R> x = root ;
        for ( int i = currentLevel-1 ; i >= 0; i-- )
        {
            while (cmpNR(x.get(i), record) <= 0 )
                x = x.get(i) ;
        }
        if ( cmpNR(x, record) == 0 ) return x.record ;
        return null ;
    }
    
    private SkipListNode<R> findBefore(R record)
    {
        SkipListNode<R> x = root ;
        // Find strictly less than node.
        for ( int i = currentLevel-1 ; i >= 0; i-- )
        {
            while (cmpNR(x.get(i), record) < 0 )
                x = x.get(i) ;
        }
        return x ;
    }
    
    
    SkipListNode<R> notANode = new SkipListNode<R>(null, 0) ;
    
    public R findDebug(R record)
    {
        SkipListNode<R> x = root ;
        // Find node.
        // Speed up by avaoid unnecessary comparisons (see Skiplist cookbook 3.5)
        
        SkipListNode<R> lastCompare = notANode ;
        
        loop1: for ( int i = currentLevel-1 ; i >= 0; i-- )
        {
            System.out.printf("Search level=%-2d %s\n",i, x) ;
            while ( true )
            {
                SkipListNode<R> y = x.get(i) ;
                if ( lastCompare == y )
                {
                    // Same as before.
                    System.out.printf("Skip:           %s\n", y) ;
                    continue loop1;
                }
                
                // 
                System.out.printf("Compare         %s %s\n", y, record) ;
                int cmp = cmpNR(y, record) ;
                lastCompare = y ;
                
                if ( cmp == 0 )
                {
                    System.out.printf("Found:          %s\n", x.get(i)) ;
                    return x.get(i).record ;
                }
                if ( cmp > 0 )
                    break ;
                x = y ;
                System.out.printf("Advance:        %s \n", x) ;
            }
        }
        System.out.println("Not found") ;
        return null ;
    }
    
    public R insert(R record)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> Insert : %s", record)) ;
        
        Object update[] = new Object[maxLevel] ;
        SkipListNode<R> x = opSetUp(update, record) ;

        if ( cmpNR(x, record) == 0 )
        {
            // Replace
            x.record = record ;
            if ( Logging && log.isDebugEnabled() )
                log.debug(format("<< Insert : %s (replace)", record)) ;
            return x.record ;
        }
        
        int lvl = randomLevel() ;
        if ( lvl > currentLevel )
        {
            // If very large insert, point to first node (root)
            for ( int i = currentLevel ; i < lvl ; i++ )
                update[i] = root ;
            currentLevel = lvl ;
        }
        x = new SkipListNode<R>(record, lvl) ;
        for ( int i = 0 ; i < lvl ; i++ )
        {
            @SuppressWarnings("unchecked")
            SkipListNode<R> y = ((SkipListNode<R>)update[i]) ;
            
//            x.set(i, y.get(i)) ;
//            y.set(i, x) ;

            x.forward[i] = y.get(i) ;
            y.forward[i] = x ;
        }
        
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< Insert : %s (addition)", record)) ;

        // New
        size++ ;
        return null ;
    }

    public R delete(R record)
    {
        SkipListNode<?> update[] = new SkipListNode<?>[maxLevel] ;
        SkipListNode<R> x = opSetUp(update, record) ;

        if ( cmpNR(x, record) != 0 )
            // Not found.
            return null ;
        
        record = x.record ;
        
        for ( int i = 0 ; i < currentLevel ; i++ )
        {
            @SuppressWarnings("unchecked")
            SkipListNode<R> y = ((SkipListNode<R>)update[i]) ;
            if ( y.get(i) != x )
                break ;
            //y.set(i, x.get(i)) ;
            y.forward[i] = x.get(i) ;
            // free x
            // Reset current level.
            // XXX
        }
        size -- ;
        return record ;
    }
    
    // Common setup for insert and delete
    final private SkipListNode<R> opSetUp(Object[] update, R record)
    {
        SkipListNode<R> x = root ;
        
        // Find less than or equal node, remembering pointers as we go down levels. 
        for ( int i = currentLevel-1 ; i >= 0; i-- )
        {
            while ( cmpNR(x.get(i), record) < 0 )
                x = x.get(i) ;
            update[i] = x ;
        }
        // Advance to same or greater
        return x.get(0) ;
    }

    public boolean isEmpty()
    {
        return root.get(0) == null ;
    }
    
    public int size()
    {
        return size ;
    }
    
    // Min - inclusive; max - exclusive
    
    public Iterator<R> iterator(R min, R max)
    {
        SkipListNode<R> x = null ;
        if ( min != null )
        {
            x = findBefore(min) ;
            x = x.get(0) ;          // Move forward (possibly over)
        }
        else
            x = root.get(0) ;
        return new SkipListIterator<R>(x, max) ;
        
    }

    @Override
    public Iterator<R> iterator()
    {
        return iterator(null,null) ;
    }
    
    public Iterable<R> records()
    {
        return ()->Iter.iter(iterator()) ;
    }

    public Iterable<R> records(R min, R max)
    {
        return ()->Iter.iter(iterator(min, max)) ;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for ( R r : this )
        {
            if ( ! first ) sb.append(" ") ;
            first = false ;
            sb.append(r) ;
        }
        return sb.toString() ;
    }
    
    @Override
    public void output(IndentedWriter out)
    {
        SkipListNode<R> x = root ;
        
        boolean first = true ;
        while( x != null )
        {
            if ( ! first ) out.print(" ") ;
            first = false ;
            x.output(out) ;
            x = x.get(0) ;
        }
    }

    // -----
//    /**
//     * Returns a random level for inserting a new node.
//     * Hardwired to k=1, p=0.5, max 31 (see above and
//     * Pugh's "Skip List Cookbook", sec 3.4).
//     *
//     * This uses the simplest of the generators described in George
//     * Marsaglia's "Xorshift RNGs" paper.  This is not a high-quality
//     * generator but is acceptable here.
//     */
    
        // Very, very slow for some reason.  Linear lists?
//    private int randomLevel() {
//        int x = randomSeed;
//        x ^= x << 13;
//        x ^= x >>> 17;
//        randomSeed = x ^= x << 5;
//        if ((x & 0x8001) != 0) // test highest and lowest bits
//            return 0;
//        int level = 1;
//        while (((x >>>= 1) & 1) != 0) ++level;
//        return level;
//    }

    
    private static final int OneOverP = 2;
    // Log distibution.
    private int randomLevel()
    { 
        int level = 1; 
        while ( rand.nextInt(OneOverP) == 0  && level < maxLevel )
            level ++ ;
        return level ;
    }
    // -----

    // The infinite node is marked by null pointers.
    private static <R extends Comparable<? super R>> int cmpNR(SkipListNode<R> node, R record)
    {
        if ( node == null )
            return (record == null ) ? 0 : 1 ;
        return cmpRR(node.record, record) ; 
    }

    static <R extends Comparable<? super R>> int cmpRR(R record1, R record2)
    {
        // A null record is the lowest element (exists in the root node)
        if ( record1 == null )
            return (record2 == null ) ? 0 : -1 ;
        if ( record2 == null )
            return 1 ;
        
        return record1.compareTo(record2) ;
    }

    private static <R extends Comparable<? super R>> int cmpNodeRecord(SkipListNode<R> node1, SkipListNode<R> node2)
    {
        if ( node1 == null )
            return (node2 == null ) ? 0 : 1 ;
        if ( node2 == null )
            return -1 ;
        return cmpRR(node1.record, node2.record) ; 
    }
    
    public void check()
    { checkList() ; }
    
    private static <R extends Comparable<? super R>>  void internalCheck(SkipList<R> list)
    {
        if ( ! Checking )
            return ;
        if ( list == null )
            error("Null pointer for list") ;
        list.checkList() ;
    }
    
    private void checkList()
    {
        SkipListNode<R> x = root ;
        while( x != null )
        {
            check(x) ;
            x = x.get(0) ;
        }
        R rec1 = null ;
        for ( R rec2 : this )
        {
            if ( rec1 != null )
            {
                if ( rec1.compareTo(rec2) > 0 )
                    error("Output order %s,%s", rec1, rec2) ;
            }
            rec1 = rec2 ;
        }
    }
    
    private void check(SkipListNode<R> node)
    {
        if ( node == null )
            error("check: Null node") ;
        SkipListNode<R> z = null ;
        for ( int i = node.forward.length-1 ; i >= 0 ; i-- )
        {
            SkipListNode<R> n = node.get(i) ;
            if ( cmpNodeRecord(z,n) < 0 )
                error("Greater node has lesser record: %s %s", debug(z), debug(n)) ;
            if ( n != null && n.forward.length < i )
                error("Pointing to a smaller node %s %s", debug(z), debug(n)) ;
            z = n ;
        }   
    }
    
    private static <R extends Comparable<? super R>> String debug(SkipListNode<R> node)
    {
        if ( node == null ) return "_" ;
        return node.debug() ;
    }
    
    public String debug()
    {
        if ( isEmpty() )
            return "<empty>" ;
        
        IndentedLineBuffer out = new IndentedLineBuffer() ;

        boolean first = true ;
        SkipListNode<R> x = root ;
        while ( x != null )
        {
            //if ( ! first ) out.print(" ") ;
            first = false ;
            x.outputFull(out) ;
            out.println();
            x = x.get(0) ;
        }
        return out.toString() ;
    }
    
    public static <R extends Comparable<? super R>> String label(SkipListNode<R> node)
    {
        if ( node == null ) return "_" ;
        return Integer.toString(node.id) ;
    }


    private static void error(String format, Object ... args)
    {
        String x = format(format, args) ;
        System.err.print(x) ;
        if ( ! x.endsWith("\n") )
            System.err.println() ;
        else
            x = x.substring(0, x.length()-1) ;
        throw new SkipListException(x) ;
    }
}
