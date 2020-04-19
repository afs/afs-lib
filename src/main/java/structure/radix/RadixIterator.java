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

import static structure.radix.RadixTreeImpl.log ;
import static structure.radix.RadixTreeImpl.logging ;
import static structure.radix.Str.strToPosn ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.lib.Bytes ;

class RadixIterator implements Iterator<RadixEntry>
{
    // Or parent.
    // Deque<RadixNode> stack = new ArrayDeque<RadixNode>() ;
    // Still need the place-in-parent.
    RadixNode node ;
    ByteBuffer slot = null ;
    ByteBuffer prefix = null ;

    byte[] finish = null ;

    RadixIterator(RadixTreeImpl tree, byte[] start, byte[] finish)
    {
        node = tree.getRoot() ;
        this.finish = finish ;
        if ( start == null )
        {
            prefix = ByteBuffer.allocate(50) ;    //Reallocating?
            node = downToMinNode(node, prefix) ;
            slot = prefix ;
            if ( logging && log.isDebugEnabled() )
            {
                log.debug("Iterator start min node") ;
                log.debug("Iterator start: "+node) ;
            }
            return ;
        }

        // BB : basically broken.

        // We need to find the first node equal to or just greater than the start.
        // If we find that start is (just) bugger than some point, we can do that
        // by setting slot to null so it's fixed on first .hasNext.


        // Like RadixTree.applicator, except copies into slot on the way down.
        // RadixTree.applicator -> a struct
        // Add arg: a per step action.

        int N = -999 ;

        // Find node of interest.
        
        // Doh! We do not need to accumulate the prefix, because we can copy out of start.
        // Whice means can share with RadixTree.applicator.

        for(;;)
        {
            if ( logging && log.isDebugEnabled() )
                log.debug("    Loop: node   = "+node) ;
            // Does the prefix (partially) match?
            N = node.countMatchPrefix(start) ;
            if ( logging && log.isDebugEnabled() )
                log.debug("    Loop: N = "+N) ;

            // Copy prefix.
            int numMatch = N ;
            if ( numMatch < 0 )
                numMatch = -(numMatch+1) ;
            // else matched up to end of prefix.

            if ( N < 0 )
                break ;
            if ( N < node.prefix.length )
                break ;
            
            // N == node.prefix.length, not a leaf.
            RadixNode node2 = null ;
            if ( ! node.isLeaf() )
            {
                int j = node.locate(start, node.lenFinish) ;
                if ( j < 0 ) //|| j == node.nodes.size() )
                    // No match across subnodes - this node is the point of longest match.
                    break ;
                // There is a next node down to try.
                node2 = node.get(j) ;
            }
            
            //****************************************
            if ( node2 != null )
            {
                // Start key continues.
                node = node2 ;
                continue ;      // LOOP
            }
                
            // TODO Move out of loop.
            
            if ( logging && log.isDebugEnabled() )
                log.debug("    Loop: no where to go") ; 
                
            // no matching next level down but key longer.  
            // Start at min tree of next index, if any.
            // DRY
            ByteBuffer bb = ByteBuffer.allocate(node.lenFinish+50) ;
            bb.put(start, 0, node.lenFinish) ;
            prefix = bb ;
                
            if ( !node.isLeaf() )
            {
                int j = node.locate(start, node.lenFinish) ;
                j = node.nextIndex(j) ;
                if ( j > 0 )
                {
                    node2 = node.get(j) ;
                    node = downToMinNode(node2, prefix) ;
                    slot = prefix ;
                    return ;
                }
                // above all this tree - drop out. 
            }
            
            RadixNode node3 = gotoUpAndAcross(node) ;
            if ( node3 == null )
            {
                node = null ;
                return ;
            }
            // Very like in hasNext - can be combine?
            prefix.position(node3.lenStart) ;
            node3 = downToMinNode(node3, prefix) ;
            slot = prefix ;
            node = node3 ;
            return ;
        }                

        if ( logging && log.isDebugEnabled() )
        {
            log.debug("  node   = "+node) ;
            log.debug("  N = "+N) ;
        }

        int numHere = (N<0)?(-(N+1)):N ;
        
        // generate the key prefix so far.
        // Better : max key in the tree.
        ByteBuffer bb = ByteBuffer.allocate(node.lenFinish+50) ;
        bb.put(start, 0, node.lenStart) ;
        //bb.position(node.lenStart+numHere) ;
        prefix = bb ;
        if ( logging && log.isDebugEnabled() )
            log.debug("  prefix = "+strToPosn(prefix)) ;
        
        // Exit at Node of interest.
        if ( N < 0 )
        {
            node = downToMinNode(node, prefix) ;
            slot = prefix ;
            if ( logging && log.isDebugEnabled() )
            {
                log.debug("  Short key: "+strToPosn(prefix)) ;
                log.debug("  Iterator start: "+node) ;
            }
            return ;
        }

        if ( N < node.prefix.length )
        {
            // Key diverges.
            byte a = start[node.lenStart+N] ;   // Key byte of divergence
            byte b = node.prefix[N] ;           // Prefix byte of divergence
            int x = Byte.compare(a, b) ;
            if ( x == 0 )
                throw new AtlasException("bytes compare same - expected different") ;
            if ( x < 0 )
            {
                // Diverge - less.
                // Start min here
                node = downToMinNode(node, prefix) ;
                slot = prefix ;
                if ( logging && log.isDebugEnabled() )
                {
                    log.debug("  Diverge/less: "+strToPosn(prefix)) ;
                    log.debug("  Iterator start: "+node) ;
                }
            }
            else
            {
                if ( logging && log.isDebugEnabled() )
                {
                    log.debug("  Diverge/more: "+strToPosn(prefix)) ;
                    log.debug("  Iterator (non)start: "+node) ;
                }
                // Diverge - key more than this node and all it's sub nodes.
                // Start here but do not yield a slot.
                RadixNode node2 = gotoUpAndAcross(node) ;
                if ( node2 == null )
                {
                    node = null ;
                    return ;
                }
                // Very like in hasNext - can be combine?
                prefix.position(node2.lenStart) ;
                node2 = downToMinNode(node2, prefix) ;
                slot = prefix ;
                node = node2 ;
                return ;
            }
            // Done.
            return ;
        }

        // N < node.prefix.length
        // N == node.prefix.length
        // Ends here; key may continue.  Start is min of this tree.
        
        if ( start.length > node.lenFinish )
        {
            // Don't yield this node. Start from next one.
            slot = null ;
            return ;
        }
        

        node = downToMinNode(node, prefix) ;
        slot = prefix ;                    

        if ( logging && log.isDebugEnabled() )
        {
            log.debug("  Min subtree: "+node) ;
            log.debug("  Slot: "+strToPosn(slot)) ;
        }
    }

    static ByteBuffer min(RadixNode node, ByteBuffer slot)
    {
        while(!node.hasEntry())
        {
            // Copy as we go.
            slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
            int idx = node.nextIndex(0) ;
            if ( idx < 0 )
                break ;
            node = node.get(idx) ;
        }
        // Copy leaf details.
        slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
        return slot ;
    }

    // TODO assumes bytebuffer large enough.
    // TODO Common code in radix
    // TODO Check downToMinNode() with RadixTree.min() -- common code?

    private static RadixNode downToMinNode(RadixNode node, ByteBuffer slot)
    {
        while(!node.hasEntry())
        {
            // Copy as we go.
            slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
            int idx = node.nextIndex(0) ;
            if ( idx < 0 )
                break ;
            node = node.get(idx) ;
        }
        // Copy leaf details.
        slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
        return node ;
    }

    static ByteBuffer max(RadixNode node, ByteBuffer slot)
    {
        while(!node.isLeaf())
        {
            // Copy as we go.
            slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
            int idx = node.lastIndex() ;
            if ( idx < 0 )
                break ;
            node = node.get(idx) ;
        }
        // Copy leaf details.
        slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
        return slot ;
    }

    /** Copy bytes from the array ([], start, length) to the end of a ByteBuffer */
    static ByteBuffer appendBytes(byte[] array, int start, int length, ByteBuffer bb) 
    {
        if ( bb.position()+length > bb.capacity() )
        {
            ByteBuffer bb2 = ByteBuffer.allocate(bb.capacity()*2 ) ;
            System.arraycopy(bb.array(), 0, bb2.array(), 0, bb.position()) ;
            return bb2 ;
        }
        //            System.arraycopy(bb.array(), bb.position(), array, 0, length) ;
        //            bb.position((bb.position()+length)) ;
        try {
            bb.put(array, start, length) ;
        } catch (java.nio.BufferOverflowException ex)
        {
            System.err.println() ;
            System.err.println(bb) ;
            System.err.printf("([%d], %d, %d)", array.length, start, length) ;
            throw ex ;
        }
        return bb ;
    }

    @Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        if ( node == null )
            // Ended
            return false ;

        RadixNode node2 ;
        if ( node.isLeaf() )
        {
            // Go up one or more.
            node2 = gotoUpAndAcross(node) ;
        }
        else
        {
            // Next across this node? When does this happen?
            int idx = node.nextIndex(0) ;
            node2 = ( idx < 0 ) ? null : node.get(idx) ;
        }
        if ( node2 == null )
        {
            node = null ;
            return false ;
        }
        prefix.position(node2.lenStart) ;

        // Now go down the next one
        node2 = downToMinNode(node2, prefix) ;
        slot = prefix ;
        node = node2 ;
        
        // Finished?
        if ( finish != null )
        {
            int x = Bytes.compare(slot.array(), finish) ;
            if ( x >= 0 )
            {
                slot = null ;
                node = null ;
                return false ;
            }
        }
        
        return true ;
    }

    private static RadixNode gotoUpAndAcross(RadixNode node2)
    {
        //System.out.println("gotoUpAndAcross: "+node2) ;

        RadixNode parent = node2.getParent() ;
        //System.out.println("gotoUpAndAcross:     "+parent) ;
        if ( parent == null )
            return null ;
        // Find self.
        int idx = parent.locate(node2.prefix) ;

//            // Find self.
//            int N = parent.nodes.size() ;
//            int idx = 0 ; 
//            for ( ; idx < N ; idx++ )
//            {
//                if ( parent.nodes.get(idx) == node2)
//                    break ;
//            }

//            if ( idx >= N )
//            {
//                System.out.println("NOT FOUND") ;
//                System.out.println("   "+parent) ;
//                System.out.println("   "+node2) ;
//            }
        idx = parent.nextIndex(idx+1) ;
        if ( idx >= 0 )
            return parent.get(idx) ;
        // tail recursion - remove.
        return gotoUpAndAcross(parent) ;
    }

    @Override
    public RadixEntry next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        if ( ! node.hasEntry() )
            throw new AtlasException("yielding a non value") ;
        byte[] x = RLib.bb2array(prefix, 0, slot.position()) ;
        slot = null ;
        return new RadixEntry(x, node.getValue()) ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

}
