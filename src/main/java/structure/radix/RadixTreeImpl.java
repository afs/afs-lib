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

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.function.Function ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Chars ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
/* http://en.wikipedia.org/wiki/Radix_tree */
public final class RadixTreeImpl implements RadixTree
{
    // TODO Sepaarte RadixNode and RadixTree
    // nibble nodes.
    
    // Need a reallocating ByteArrayBuilder
    
    // Package wide error function and exception.
    // Class Radix.
    
    // RLib - all str to a Str class.
    
    static public boolean logging = true ;
    static public /*final*/ boolean checking = true ;
    
    static final byte[] bytes0 = new byte[]{} ;
    static final byte[] bytesNotFound = new byte[]{} ;
    
    static Logger log = LoggerFactory.getLogger(RadixTreeImpl.class) ;
    private RadixNode root = null ;
    
    public RadixNode getRoot() { return root ; }
    
    // TODO
    
    // Either re-locate in current node with another call of node.countMatchPrefix(key)
    // or return search state as a struct.
    static class LocatedNode {
        RadixNode node ;
        int N ;
    }
    
    /** Find the starting point - call the action */
    private static RadixNode locator(RadixNode root, byte[] key)
    {
        // TODO ?? Return a struct and be normal.
        // TODO ?? An object that presents the process (and slots for multi returns).
        
        // Such short lives objects are very cheap in java (e.g. pure eden heap)
        
        RadixNode node = root ;     // The current node.
        for(;;)
        {
            // Does the prefix (partially) match?
            int N = node.countMatchPrefix(key) ;
            if ( N < node.prefix.length )
                // Includes negative N
                // Key ran out.
                return node ;
            // N == prefix
            if ( node.isLeaf() )
                // Whether it matches or not, this is the node where the action is.
                return node ;
            
            // Reached end; may end here, may not.
            // Longer or same length key.
            int j = node.locate(key, node.lenFinish) ;
            if ( j < 0 ) //|| j == node.nodes.length )
                // No match across subnodes - this node is the point of longest match.
                return node ;
            
            // See if there is a next node down to try.
            RadixNode node1 = node.get(j) ;
            // Nothing to go to
            if ( node1 == null )
                // Nothing to go to
                return node ;
            // Next node to try.
            node = node1 ;
        }
        // Does not happen
    }
    
    
    /** Test whether the key is in the tree */
    @Override
    public boolean contains(byte[] key)
    {
        return find(key, bytesNotFound) != bytesNotFound ;
    }
    
    /** Find by key.
     *  Return some default if not found (neds if values can be null 
     */
    @Override
    public byte[] find(byte[] key, byte[] dft)
    {
        if ( root == null )
            return dft ;
        RadixNode node = locator(root, key) ;
        if ( node.lenFinish == key.length )
        {
            int N = node.countMatchPrefix(key) ;
            if ( N == node.prefix.length && node.hasEntry() )
              // Exact match of key.
              return node.getValue() ;
        }
        return dft ;
    }        
    
    @Override
    public boolean insert(byte[] key, byte[] value)
    {
        if (logging && log.isDebugEnabled() )
        {
            String v = (value==null)?"null":Bytes.asHex(value) ;
            log.debug("** Insert : ("+Bytes.asHex(key)+","+v+")") ;
        }
     
        if ( root == null )
        {
            root = RadixNode.allocBlank(null) ;
            root.prefix = key ;
            root.lenStart = 0 ;
            root.lenFinish = key.length ;
            root.setValue(value) ;
            return true ;
        }
        
        RadixNode node = locator(root, key) ;
        int N = node.countMatchPrefix(key) ;
        return insert$(node, N, key, value) != null ;
    }
    
    private RadixNode insert$(RadixNode node, int N, byte[] key, byte[] value)
    {
        if (logging && log.isDebugEnabled() )
        {
            log.debug("insert: ("+Str.str(key)+", "+Str.str(value)+")") ;
            log.debug("insert: here => "+node) ;
            log.debug("insert: N = "+N) ;
        }
        /* Cases:
         * Leaf.
         * L1/  Key exists                 (N == prefix.length && node.lenFinish == key.length )
         * L2/  Key does not exist         (error)
         * L3/  Inserted key shorter       (N >=0 && N < prefix.length)
         * L4/  Inserted key diverges      (N < 0)
         * L5/  Inserted key longer        (N == prefix.length)
         * Branch:
         *   Key same length               (N == prefix.length && node.lenFinish == key.length )
         * B1/    Exists, and is already a value branch.
         * B2/       Does not exists, is not a value branch.
         * B3/  Key shorter than prefix.   (N >=0 && N < prefix.length)
         * B4/  Key diverges               (N < 0)
         * B5/  Key longer than prefix.    (N == prefix.length)
         *
         * There is common processing.
         */

        // Key already present - we ended at leaf or branch exactly.
        if ( N == node.prefix.length && node.lenFinish == key.length ) 
        {
            // Cases L1, B1 and B2
            if (  node.isLeaf() )
            {          
                // L1 and B1
                if (logging && log.isDebugEnabled() )
                    log.debug("insert: Already present") ;
                node.setValue(value) ;
                //XXX return node is different.
                return null ;
            }
            // B2
            node.setValue(value) ;
            return node ;
        }

        // Key longer than an existing key
        if ( N == node.prefix.length )
        {
            byte[] prefixNew = Bytes.copyOf(key, node.lenFinish, key.length - node.lenFinish) ;
            if (logging && log.isDebugEnabled() )
            {
                log.debug("Key longer than matching node") ;
                log.debug("  Prefix new : "+Bytes.asHex(prefixNew)) ;
            }

            // Case L5 and B5
            // L5: Leaf to branch.
            if ( node.isLeaf() )
            {
                byte[] v = node.getValue() ;
                node = node.convertToEmptyBranch() ;
                node.setValue(v) ;
            }
            RadixNode n = RadixNode.allocBlank(node) ;
            n = n.convertToLeaf() ;
            n.prefix = prefixNew ;
            n.lenStart = node.lenFinish ;
            n.lenFinish = key.length ;
            n.setValue(value) ;

            int idx = node.locate(prefixNew) ;
            if ( node.get(idx) != null )
                error("Key longer than node but subnode location already set") ;
            node.set(idx, n) ;
            return node ;
        }

        // Key shorter or diverges in the middle of the prefix.

        // Cases remaining. L3, B3, L4, B4  

        // Cases L3, B3 : Key is shorter than prefix.
        // Split the prefix upto the end of the key.
        // Make this node a value node (or leaf).

        // Cases L4, B4.
        // Key diverges at this node at point N, not the full prefix.
        // Split the prefix upto the point of diverence.
        // Don't make this node a value node.

        if ( N < 0 )
        {
            // Key ran out.  new node will be inserted.
            // Cases L3, B3.
            N = -(N+1) ;
            byte[] prefixHere = Bytes.copyOf(node.prefix, 0, N) ;
            // Remainder of original data.
            byte[] prefixSub  = Bytes.copyOf(node.prefix, N) ;

            if (logging && log.isDebugEnabled() )
            {
                log.debug("Key ends mid-way through this node") ;
                log.debug("  Prefix here : "+Bytes.asHex(prefixHere)) ;
                log.debug("  Prefix sub  : "+Bytes.asHex(prefixSub)) ;
            }

            // New node to go under this one.
            RadixNode node1 = RadixNode.allocBlank(node) ;
            node1.prefix = prefixSub ; 
            node1.lenStart = node.lenStart+N ;
            node1.lenFinish = node.lenFinish ;
            if ( ! node.isLeaf() )
            {
                node1 = node1.convertToEmptyBranch() ;
                node1.takeSubNodes(node) ;
            }
            else
                node1.setValue(node.getValue()) ;

            // Clear this node, making is a part of the prefix from before.
            node = node.convertToEmptyBranch() ;
            node.prefix = prefixHere ;
            //node.lenStart = node.lenStart ;
            node.lenFinish = node.lenStart+N ;
            // Carry the inserted value.
            node.setValue(value) ;

            // Put in the neew subnode.
            int idx1 = node.locate(prefixSub) ;
            node.set(idx1, node1) ;
            return node ;
        }
        // While there is some duplication of code fragement between this case
        // and the last, it's easier to have simple, direct code without
        // various "if"s.

        // N >= 0
        // Key diverges at N
        // Cases L4, B4


        if ( key.length <= node.lenStart )
            error("Incorrect key length: "+key.length+" ("+node.lenStart+","+node.lenFinish+")") ;
        // Case N = 0 only occurs at root (else a match to previous node).
        // Key shorter than this node and ends here.

        // New prefix to the split point
        byte[] prefixHere = Bytes.copyOf(node.prefix, 0, N) ;
        // Remainder of original data.
        byte[] prefixSub1  = Bytes.copyOf(node.prefix, N) ;
        // New data from key.
        byte[] prefixSub2  = Bytes.copyOf(key, N+node.lenStart) ;

        if (logging && log.isDebugEnabled() )
        {
            log.debug("Key splits this node") ;
            log.debug("  Prefix here : "+Bytes.asHex(prefixHere)) ;
            log.debug("  Prefix sub1 : "+Bytes.asHex(prefixSub1)) ;
            log.debug("  Prefix sub2 : "+((prefixSub2==null)?"null":Bytes.asHex(prefixSub2))) ;
        }

        // The tail of the original data and all the sub nodes.
        // Could do this in-place but have to alter the parent to point to a new node.
        // XXX
        RadixNode node1 = RadixNode.allocBlank(node) ;
        node1.prefix = prefixSub1 ; 
        node1.lenStart = node.lenStart+N ;
        node1.lenFinish = node.lenFinish ;
        if ( ! node.isLeaf() )
        {
            node1 = node1.convertToEmptyBranch() ;
            node1.takeSubNodes(node) ;
        }
        if ( node.hasEntry() )
            node1.setValue(node.getValue()) ;

        // The new leaf for the new data
        RadixNode node2 = RadixNode.allocBlank(node) ;
        node2.prefix = prefixSub2 ; 
        node2.lenStart = node.lenStart+N ;
        node2.lenFinish = key.length ;
        node2.setValue(value) ;

        // Now make node a two way in-place.

        node = node.convertToEmptyBranch() ;
        node.prefix = prefixHere ;
        //node.lenStart
        node.lenFinish = node.lenStart+N ;
        node.clearValue() ;

        int idx1 = node.locate(prefixSub1) ;
        node.set(idx1, node1) ;
        int idx2 = node.locate(prefixSub2) ;
        node.set(idx2, node2) ;
        return node ;
    }

    /** Delete - return true if the tree changed (i.e the key was present and so was removed) */
    @Override
    public boolean delete(byte[] key)
    {
        if (logging && log.isDebugEnabled() )
            log.debug("** Delete : "+Bytes.asHex(key)) ;

        if ( root == null )
            return false ;

        RadixNode node = locator(root, key) ;
        int N = node.countMatchPrefix(key) ;
        RadixNode n = delete$(node, N, key) ;

        // Fixup root.
        // If the root changed and now has no-subnodes and no value, free it.
        if ( n != null && n.isRoot() && (n.countSubNodes() == 0 && ! n.hasEntry() ) )
        {
            RadixNode.dealloc(n) ;
            root = null ;
        }
        return n != null ;
    }

    private RadixNode delete$(RadixNode node , int N, byte[] key)
    {
        RadixNode prevNode = node.getParent() ;
        if (logging && log.isDebugEnabled() )
        {
            log.debug("delete: "+Str.str(key)) ;
            log.debug("delete: here => "+node) ;
            log.debug("delete: N = "+N) ;
        }

        /* Cases
         * 1/ Node = leaf, does not exist
         * 2/ Node = branch, not value
         * 
         * Then we have to work to sort out the tree.
         * A/ If leaf delete, remove from parent.
         * B/ If branch/value delete switch off flag.
         * Then  
         * C/ Simplify parent or branch
         *    If not a value and has one subnode, collapse
         */

        // Cases 1 and 2 : must be full length

        // Key not already present - not a full match (short, diverges) 
        if ( N != node.prefix.length || node.lenFinish != key.length )
        {
            if (logging && log.isDebugEnabled() )
                log.debug("delete: Not present") ;
            return null ;
        }

        if ( node.isLeaf() ) 
        {
            // Leaf - delete node.
            if ( prevNode != null )
            {
                // not leaf root.
                int idx = prevNode.locate(node.prefix) ;
                RadixNode x = prevNode.get(idx) ;
                prevNode.set(idx, null) ;
                RadixNode.dealloc(node) ;
                node = null ;
                // Drop though to fixup.
            }
            else
            {
                // Root.
                node.clearValue() ;
                return node ;
            }
        }
        else
        {
            // Branch.  Delete is a value branch.
            if ( node.hasEntry() && node.hasEntry() )
                node.clearValue() ;
            // Drop though to fixup.
            else
                return null ;       // Didn't match after all.
        }

        // Now we need to sort out the tree after a change.
        // We need to work on the parent if it was a leaf, or node, if it was a branch.

        RadixNode fixupNode = (node==null ? prevNode : node ) ;

        RadixNode fixupNode2 = fixup(fixupNode) ;
        if ( fixupNode2 != null )
            fixupNode = fixupNode2 ;
        return fixupNode ;
    }
    
    /** After delete, need to remove redundant nodes. */
    protected static RadixNode fixup(RadixNode node)
    {
        // Must be a branch.
        if ( node.isLeaf() )
            error("Attempt to fixup a leaf") ;

        // count = 0 => can we become a leaf?
        // count = 1 => c an we merge with subnode? 
        
        int c = node.countSubNodes() ;
        if ( c == 0 )
        {
            if ( node.hasEntry() )
            {
                byte[] v = node.getValue() ;
                node = node.convertToLeaf() ;
                node.setValue(v) ;
            }
            // else; should not happen  - a non-value branch which had one leaf subnode 
            else
                error("Branch has no subnodes but didn't have a value") ;
            return node ;
        }

        if ( c != 1 )
            return null ;
        
        if ( node.hasEntry() )
            return null ;
        // Find exactly one subnode.
        RadixNode sub = node.oneSubNode() ;
        if ( sub == null )
            error("Branch has one subnodes but can't find it") ;
        // Single subnode to node.
        // Merge it in and delete it.
        // It may be a leaf.
        if ( logging && log.isDebugEnabled() )
        {
            log.debug("Collapse node") ;
            log.debug("  node: "+node) ;
            log.debug("  sub : "+sub) ;
        }
        
//        // We're a value if the subnode was a value.
//        node.setValue(sub.getValue()) ;

        // Combined prefix.
        int len1 = node.prefix.length + sub.prefix.length ;  
        int len =  sub.lenFinish - node.lenStart ;
        if ( len1 != len )
            error("Inconsistency in length calculations") ;

        // Merged prefix.
        byte [] newPrefix = new byte[len] ;
        System.arraycopy(node.prefix, 0, newPrefix, 0, node.prefix.length) ;
        System.arraycopy(sub.prefix,  0, newPrefix, node.prefix.length, sub.prefix.length) ;
        if ( logging && log.isDebugEnabled() )
            log.debug("New prefix: "+Str.str(newPrefix)) ;

        // Prefix
        node.prefix = newPrefix ;
        // node.lenStart ;
        node.lenFinish = sub.lenFinish ;

        if ( sub.isLeaf() )
        {
            node.convertToLeaf() ;
            node.setValue(sub.getValue()) ;
        }
        else
        {
            // Clear, pull up subnodes.
            node = node.convertToEmptyBranch() ;
            node.takeSubNodes(sub) ;
            if ( sub.hasEntry() )
                node.setValue(sub.getValue()) ;
        }

        if ( logging && log.isDebugEnabled() )
            log.debug("  --> : "+node) ;
        RadixNode.dealloc(sub) ;
        return node; 
    }

    @Override
    public void print()
    {
        if ( root == null )
        {
            System.out.println("<empty>") ;
            return ;
        }
        root.output(IndentedWriter.stdout) ;
        IndentedWriter.stdout.flush();
    }

    @Override
    public void clear()
    {
        if ( root == null )
            return ;
        clear(root) ;
    }
    
    private void clear(RadixNode node)
    {
        if ( !node.isLeaf() )
        {
            int idx = 0 ;
            while( ( idx = node.nextIndex(idx)) >= 0 )
            {
                RadixNode n = node.get(idx) ;
                clear(node) ;
            }
        }        
        RadixNode.dealloc(node) ;
        return ;
    }

    @Override
    public ByteBuffer min() { return min(null) ; }
    
    @Override
    public ByteBuffer min(byte[] b)
    {
        if ( root == null )
            return null ;
        ByteBuffer bb = (b == null ) ? ByteBuffer.allocate(50) : ByteBuffer.wrap(b) ;
        bb = RadixIterator.min(root, bb) ;
        bb.flip() ;
        return bb ;
    }
    
    @Override
    public ByteBuffer max() { return max(null) ; }

    @Override
    public ByteBuffer max(byte[] b)
    {
        if ( root == null )
            return null ;
        ByteBuffer bb = (b == null ) ? ByteBuffer.allocate(50) : ByteBuffer.wrap(b) ;
        bb = RadixIterator.max(root, bb) ;
        bb.flip() ;
        return bb ;
    }

    @Override
    public long size()
    {
        if ( root == null )
            return 0 ;
        
        RadixNodeVisitor<Object> v = new RadixNodeVisitorBase()
        {
            int count = 0 ;
            @Override
            public void before(RadixNode node)
            {
                if (node.hasEntry()) 
                    count++ ;
            }
            
            @Override
            public Object result()
            { return count ; }
        } ;
        root.visit(v) ;
        return (Integer)v.result() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        if ( root == null )
            return true ;
        if ( root.isLeaf() )
            return false ;
        // Denormalized tree
        return root.zeroSubNodes() ;
    }
    
    @Override
    public Iterator<RadixEntry>iterator() { return iterator(null, null) ; }
    
    @Override
    public Iterator<RadixEntry> iterator(byte[] start, byte[] finish)
    { 
        if ( logging && log.isDebugEnabled() )
            RadixTreeImpl.log.debug("Iterator("+Str.str(start)+", "+Str.str(finish)+")") ;
        
        if ( root == null )
        {
            if ( logging && log.isDebugEnabled() )
                log.debug("iterator: empty tree") ;
            return Iter.nullIterator() ;
        }
        // TODO -- Empty root : should not occur but cope with it.
        return new RadixIterator(this, start, finish) ;
    }
    
    static Function<Byte, String> hex = (byte$)->{
        int hi = (byte$.byteValue() >> 4) & 0xF ;
        int lo = byte$.byteValue() & 0xF ;
        return "0x"+Chars.hexDigitsUC[hi]+Chars.hexDigitsUC[lo] ;
    };

    
    // TODO Leaves and values.        
    @Override
    public void printLeaves()
    {
        if ( root == null )
        {
            System.out.println("Tree: empty") ;
            return ;
        }

        Iterator<RadixEntry> iter = iterator() ;
        Iter.apply(iter, System.out::println) ;
    }
    
    static void error(String string)
    {
        throw new AtlasException(string) ;
    }

    @Override
    public void check()
    { 
        if ( root != null )
            root.check() ; 
    }
}
