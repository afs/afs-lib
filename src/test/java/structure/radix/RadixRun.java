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

package structure.radix;
import static structure.radix.Str.str ;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.RandomLib ;
import org.apache.jena.atlas.logging.LogCtl ;

/** Randomly generate keys to add and delete (shuffled) using a RadixTree - check for consistency. Repeat */ 
public class RadixRun
{
    public static void main(String ...argv)
    { 
        LogCtl.setLog4j() ;
        LogCtl.enable(RadixTree.class) ;
        //RadixTree.logging = false ;
        
        RadixTreeImpl.logging = false ;


        int nRuns = 100000 ;
        int maxLen = 10 ;
        int nKeys = 100 ;
        
        final int dotsToCycle = nRuns > 10000 ? 100 : 10 ;
        final int dotsPerLine = 100 ;
        final int ticksPerLine = dotsToCycle*dotsPerLine ;

        System.out.printf("Runs: %,d maxLen=%d nKeys=%d\n", nRuns, maxLen, nKeys ) ;
        for ( int i = 0 ; i < nRuns ; i++ )
        {
            RadixTree trie = RadixTreeFactory.create() ;
            List<byte[]> x1 = gen(nKeys, maxLen, (byte)20) ;
            List<byte[]> x2 = randomize(x1) ;

            if ( i%ticksPerLine == 0 )
                System.out.printf("%,8d: ",i) ;
            if ( i%dotsToCycle == (dotsToCycle-1) )
                System.out.print(".") ;
            if ( i%ticksPerLine == (ticksPerLine-1) )
                System.out.println() ;

            //System.out.println() ;
            //print(x1) ;
            //print(x2) ;
            System.out.flush() ;
            
            try { 
                execInsert(trie, x1, false) ;
                //System.out.flush() ;
                execDelete(trie, x2, false) ;
            } catch (AtlasException ex)
            {
                print(x1) ;
                print(x2) ;
                return ;
            }
        }
        if ( nRuns%ticksPerLine != 0 )
            System.out.println() ;
        System.out.printf("Done (%,d)\n", nRuns) ;
        System.exit(0) ;
    }
    
    private static List<byte[]> randomize(List<byte[]> x)
    {
        x = new ArrayList<byte[]>(x) ;
        List<byte[]> x2 = new ArrayList<byte[]>() ;
        for ( int i = 0 ; x.size() > 0 ; i++ )
        {
            int idx = RandomLib.qrandom.nextInt(x.size()) ;
            x2.add(x.remove(idx)) ;
        }
        return x2 ;
    }

    static void execInsert(RadixTree trie, List<byte[]> entries, boolean debugMode)
    {
        try {
            for ( byte[] arr : entries )
            {
                if ( debugMode )
                    System.out.println("Insert: "+str(arr)) ;
                insertAndCheck(trie, arr) ;
                if ( debugMode )
                {
                    trie.print() ;
                    System.out.println() ;
                }
            }
        } catch (AtlasException ex)
        {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
            trie.print() ;
            throw ex ;
        }
        
        check(trie, entries) ;
    }
   
    static void execDelete(RadixTree trie, List<byte[]> entries, boolean debugMode)
    {
        try {
            for ( byte[] arr : entries )
            {
                if ( debugMode )
                    System.out.println("Delete: "+str(arr)) ;
                deleteAndCheck(trie, arr) ;
                if ( debugMode )
                {
                    trie.print() ;
                    System.out.println() ;
                }
            }
        } catch (AtlasException ex)
        {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
            trie.print() ;
            throw ex ;
        }
        if ( trie.iterator().hasNext() )
        {
            System.out.flush() ;
            System.err.println("Tree still has elements") ;
            trie.print() ;
        }
        
    }

    //    static void search(RadixTree trie, byte[] key)
//    {
//        System.out.println("Search--'"+Bytes.asHex(key)+"'") ;
//        RadixNode node = trie.search(key) ;
//        System.out.println("Search>> "+node) ;
//        System.out.println() ;
//    }

    private static void print(List<byte[]> entries)
    {
        boolean first = true ;
        StringBuilder sb = new StringBuilder() ;
        sb.append("byte[][] data = { ") ;
        for ( byte[] e : entries )
        {
            if ( ! first ) 
                sb.append(", ") ;
            first = false ;
            
            sb.append("{") ;
            sb.append(str(e,"," )) ;
            sb.append("}") ;
        }
        sb.append(" } ;") ;
        System.out.println(sb.toString()) ;
    }
    
    static void check(RadixTree trie, List<byte[]> keys)
    {
        for ( byte[] k : keys )
        {
            if ( trie.find(k, k) == null )
                System.err.println("Did not find: ["+str(k)+"]") ;
        }
        
        long N1 = trie.size() ;
        long N2 = Iter.count(trie.iterator()) ; 
        if ( N1 != N2 )
            System.err.printf("size[%d] != count[%d]\n",N1, N2) ;
        if ( N1 != keys.size() )
            System.err.printf("size[%d] != length[%d]\n",N1, keys.size()) ;
        
        // check ordered.
        byte[] prev = null ;
        for ( Iterator<RadixEntry> elements = trie.iterator() ; elements.hasNext() ; )
        {
            byte[] here = elements.next().key ;
            if ( prev != null )
            {
                if ( Bytes.compare(prev, here) >= 0 )
                    System.err.println("Not increasing: "+str(prev)+" // "+str(here)) ;
            }
            prev = here ;
        }
    }
    
    static void insertAndCheck(RadixTree trie, byte[] key)
    {
        boolean b2 = ! trie.contains(key) ;
        boolean b = trie.insert(key, key) ;
        if ( b != b2 )
        {
            System.out.flush() ;
            System.err.println("Inconsistent (insert)") ;
        }
        trie.check() ;
    }
    
    static void deleteAndCheck(RadixTree trie, byte[] key)
    {
        boolean b2 = trie.contains(key) ;
        boolean b = trie.delete(key) ;
        if ( b != b2 )
        {
            System.out.flush() ;
            System.err.println("Inconsistent (delete): "+str(key)) ;
        }
        trie.check() ;
    }
    
    static boolean contains(byte[] b, List<byte[]> entries)
    {
        for ( byte[] e : entries )
        {
            if ( Arrays.equals(e, b) )
                return true ;
        }
        return false ;
    }



    // Generate nKeys entries upto to nLen long
    static List<byte[]> gen(int nKeys, int maxLen, byte maxVal)
    {
        List<byte[]> entries = new ArrayList<byte[]>() ;
        
        for ( int i = 0 ; i < nKeys ; i++)
        {
            
            while(true)
            {
                byte[] b = gen1(maxLen, maxVal) ;
                if ( ! contains(b, entries) )
                {
                    entries.add(b) ;
                    break ;
                }
            }
        }
        
        return entries ;
    }
    
    static byte[] gen1(int nLen, byte maxVal )
    {
        int len = RandomLib.qrandom.nextInt(nLen) ;
        byte[] b = new byte[len] ;
        for ( int i = 0 ; i < len ; i++ )
            b[i] = (byte)(RandomLib.qrandom.nextInt(maxVal)&0xFF) ;
        return b ;
    }
}
