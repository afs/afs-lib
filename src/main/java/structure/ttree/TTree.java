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

package structure.ttree;

import static org.apache.jena.atlas.io.IndentedWriter.stdout ;
import static structure.ttree.TTreeNode.label ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import lib.ArrayOps ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;
import org.apache.jena.atlas.io.Printable ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import structure.OrderedSet ;
import structure.tree.TreeException ;

public final
class TTree<T extends Comparable<? super T>> implements Printable , OrderedSet<T>
{
    /* T*Tree : in each leaf or half-leaf, have pointer to successor node.
     * Speeds traversal.  But need to know it's a threading pointer.
     * "T*-tree : A Main Memory Database Index Structure for Real Time Applications" / 1996
     */
    
    /* TODO
     + Sort out workers - TTreeNode has similar.
       lib.log(Logger, fmt, args)
     + size by counting successful ins/del
     + Delete mods: do before fixup, then fixup.
         Amalgamate rules come simpler?
     + Remove id logging from TTreeNodes
     + Check the rebalanceDelete and termination criterion
    */
    
    /* Notes:
     * See:
     * http://en.wikipedia.org/wiki/T-tree
     * 
     * Tobin J. Lehman and Michael J. Carey
     * "A Study of Index Structures for Main Memory Database Management Systems"
     * VLDB 1986 -- http://www.vldb.org/conf/1986/P294.PDF
     * 
     * Deletion:
     * 
     * The paper allow half-leaves to become less than the min size (3.2.1 - deletion algorithm - step 2)
     * These can be merged (step 3) but that means we can still have overly small leaves (not mergeable).
     * These can become internal nodes through rotation resulting in internal nodes that are
     * not full, which is something that T-Trees try to avoid.
     * Solution: pull up elements from leaves into a half leaf.   
     * 
     * Rotations cause a branch to become shorter.
     *
     * Rebalance after insertion:
     *   Only leaves are added
     *   Rebalance can stop after one rotation occurs
     *   But need to reset heights in tree all the way to the root.
     * Rebalance after deletion:
     *   One rotation can cause a shorter subtree
     *   Stop when an evenly balanced node is found
     *   But need to reset heights in tree all the way to the root.
     */
    
    static Logger log = LoggerFactory.getLogger(TTree.class) ;
    
    /* 
     * The algorithm of T-Trees goes in this class - the TTreeNode class is
     * just a structure to be manipulated. 
     */
    public static boolean NullOut = true ;
    public static boolean Checking = true ;
    public static boolean Logging = true ;
    public static boolean Verbose = false ;

    public final int NodeSize = 2 ;        // Maximum node size.
    public final int NodeSizeMin = 2 ;     // Limit at which we rebalance on delete in internal nodes to keep nodes full.  
    
    static int InitialHeight = 1 ;      // The height of a node with no nodes below it.
    
    TTreeNode<T> root ;
    
    public TTree(int nodeSize)
    {
        this(nodeSize, nodeSize) ;
    }

    public TTree(int nodeSize, int intNodeSize)
    {
        
        root = newRoot() ;
    }

    
    //    public TTree(int NodeSize, Comparator<T> comparator)
//    {
//        root = newNode(null) ;
//        
//        comparator = new Comparator<T>()
//        {
//            @Override
//            public int compare(T obj1, T obj2)
//            { return obj1.compareTo(obj2) ; }
//        } ;
//    }
    
    private TTreeNode<T> newRoot()
    {
        return newNode(null) ;
    }
    
    private TTreeNode<T> newNode(TTreeNode<T> parent)
    {
        TTreeNode<T> n = new TTreeNode<T>(parent, NodeSize) ;
        if ( Logging )
            log("** New node: %s [parent=%s]", label(n), label(parent)) ;
        return n ;
    }

    @Override
    public boolean isEmpty()
    { 
        if ( root == null ) return true ;
        return root.isEmpty() ;
    }

    @Override
    public boolean contains(T item)
    { return search(item) != null ; } 


    @Override
    public T search(T item)
    {
        // findBoundingNode checks down the tree using min/max only.
        // Assuming a tree of non-trivial depth, this is faster than calling 
        // node.find on each node traversed as binary search does not
        // touch min/max until last.

        if ( root == null )
            return null ;
        
        TTreeNode<T> node = findBoundingNode(root, item) ;
        int x = node.find(item) ;
        if ( x < 0 )
            return null ;
        return node.elements[x] ;
    }
   
    @Override
    public boolean add(T item)
    { 
        if ( Logging )
            log.debug(">> Insert: "+item) ;
        if ( root.isEmpty() )
            return root.add(item) ;
        
        TTreeNode<T> node = findBoundingNode(root, item) ;
        if ( Logging )
            log.debug("Bounding node: "+node) ;
        
        boolean b = insertBoundingNode(node, item) ;
        
        if ( Checking )
            checkTree() ;
        if ( Logging )
            log.debug("<< Insert: "+item) ; 
        
        return b ;
    }

    @Override
    public void clear()         { root = newRoot() ; }

    private boolean insertBoundingNode(TTreeNode<T> node, T item)
    {
        if ( Logging )
            log("insertBoundingNode(%s, %s)", label(node), item) ;
        int idx = node.find(item) ;
        if ( idx >= 0 )
        {
            // Duplicate.
            if ( Logging )
                log("insertBoundingNode: duplicate") ;
            return node.add(item) ; // Key same - value may not be.
        }
            
        if ( ! node.isFull() )
        {
            if ( Logging )
                log("insertBoundingNode: node not full") ;
            return node.add(item) ;
        }
        
        // Remove minimal element.
        // Insert item (at decode(idx))
        // Use minimal element as new insert into left substree.
        // (Modification: do on right as no shift).
        // Or put a null and shift down to insert.

        // -1 is encodeIndex(0)
        
        if ( idx == -1 )
        {
            if ( Logging )
                log("Insert at min point: (%s, %s)", label(node), item) ;
            // Insert at min in a full node.
            if ( Checking )
            {
                if ( node.left != null ) error("Left not null") ;
            }
            node.left = newNode(node) ;
            node.left.add(item) ;
            rebalanceInsert(node) ;
            return true ;
        }
        
        // Improve this
        T min = node.removeBottom() ; 
        node.add(item) ;
        if ( Logging )
            log("ShiftDown/add->%s: (%s)", min, node) ;
        
        // Insert min
        if ( node.left == null )
        {
            if ( Logging )
                log("Insert new left") ;
            TTreeNode<T> newNode = newNode(node) ;
            node.left = newNode ;
            boolean b = newNode.add(min) ;
            rebalanceInsert(node) ;
            return b ;
        }
        
        // Insert at greatest lower bound.
        node = TTreeNode.getRightDeep(node.left) ;
        int idx2 = node.find(min) ;     // Only for same key storage
        if ( idx2 > 0 || ! node.isFull() )
        {
            boolean b = node.add(min) ;
            if ( Logging )
                log("Replace GLB: %s", node) ; 
            return b ;
        }
        
        // Node full; not a replacement.  Add new right and place one element in  it.
        if ( Logging )
            log("Insert new right") ; 
        TTreeNode<T> newNode = newNode(node) ;
        node.right = newNode ;
        boolean b = node.right.add(min) ;
        rebalanceInsert(node) ;
        return b ;
    }

    private void rebalanceInsert(TTreeNode<T> node)
    {
        // Need to abort early.
        while ( node != null )
        {
            if ( ! rebalanceNode(node) )
                return ;
            node = node.parent ;
        }
        return ;  
    }
    
    private void rebalanceDelete(TTreeNode<T> node)
    {
        while ( node != null )
        {
            // Need to work all the way up to the tree root.
//            if ( ! rebalanceNode(node) )
//                return ;
            rebalanceNode(node) ; 
            node = node.parent ;
        }
        return ;  
    }
    
    static <T extends Comparable<? super T>>
    TTreeNode<T> findBoundingNode(TTreeNode<T> node, T item)
    {
        for ( ;; )
        {
            // Avoid tail recursion.  Sigh.
            int x = item.compareTo(node.getMin()) ;
            if ( x < 0 )
            {
                if ( node.left == null )
                    return node ;
                node = node.left ;
                continue ;
            }

            x = item.compareTo(node.getMax()) ;
            if ( x > 0 )
            {
                if ( node.right == null )
                    return node ;
                 node = node.right ;
                 continue ;
            }
            // Between min and max - this node.
            x = 0 ;
            return node ;        
        }
    }
    
    @Override public boolean remove(T item)
    { 
        if ( Logging )
        {
            log.debug(">> Delete: "+item) ;
            output() ;
        }
            
        if ( root == null || root.isEmpty() )
            return false ;
        
        TTreeNode<T> node = findBoundingNode(root, item) ;
        boolean b = node.delete(item) ;
        if ( b )
        {
            TTreeNode<T> fixupNode = node ; 

            if ( node.isInternal() )
            {
                // Internal node - find GLB (must exist for an internal node)
                // and insert the GLB here, then fixup from bottom node.
                TTreeNode<T> n2 = TTreeNode.getRightDeep(node.left) ;
                T glb = n2.removeTop() ;
                node.add(glb) ;
                fixupNode = n2 ;
            }

            fixupDelete(fixupNode) ;
        }

        if ( Checking )
            checkTree() ;
        if ( Logging )
            log.debug("<< Delete: "+item) ; 
        
        return b ;
    }
    
    /** Fix up a node - it is the node that has changed size. */
    private void fixupDelete(TTreeNode<T> node)
    {
        if ( node.nodeSize() >= NodeSizeMin ) return ;
        if ( Checking && node.isInternal() ) error("Attempt to fixup internal node") ;
        
        if ( node.isLeaf() )
        {
            if  ( node.nodeSize() > 0 )
            {
                // Check whether to amalgamate with parent (if half leaf??)
                // Modified deletion: half-leaves are always nearly full.
                return ;
            }

            // Empty leaf.  Remove in parent.

            if ( node.parent == null )
            {
                //root = newRoot() ;
                // Root now empty.
                return ;
            }
            if ( node.parent.left == node )
            {
                // Fix parent height : might have chnaged.
                node.parent.left = null ;
                if ( node.parent.right == null )
                    node.parent.height = node.parent.height-1 ;
            }
            else
            {
                node.parent.right = null ;
                if ( node.parent.left == null )
                    node.parent.height = node.parent.height-1 ;
            }
            
            rebalanceDelete(node.parent) ;
            return ;
        }

        if ( node.isLeftHalfLeaf() )
        {
            TTreeNode<T> leaf = node.right ;
            if ( Checking && ! leaf.isLeaf() ) 
                error("Expected leaf to right") ;
            if ( node.nodeSize + leaf.nodeSize <= NodeSize )
            {
                // Amalgamate: copy leaf elements to parent half-leaf.
                System.arraycopy(leaf.elements, 0, node.elements, node.nodeSize, leaf.nodeSize) ;
                node.nodeSize += leaf.nodeSize ;
                node.right = null ;
                node.height = 1 ;
                rebalanceDelete(node) ;
            }
            else    // Deletion modification
            {
                T item = leaf.removeBottom() ;
                node.insertAt(node.nodeSize, item) ;
            }
            return ;
        }
        
        if ( node.isRightHalfLeaf() )
        {
            TTreeNode<T> leaf = node.left ;
            if ( Checking && ! leaf.isLeaf() ) error("Expected leaf to left") ;
            if ( node.nodeSize + leaf.nodeSize <= NodeSize )
            {
                // Amalgamate
                if ( node.nodeSize > 0 )
                    ArrayOps.shiftUpN(node.elements, 0, leaf.nodeSize, node.nodeSize) ;
                System.arraycopy(leaf.elements, 0, node.elements, 0, leaf.nodeSize) ;
                node.nodeSize += leaf.nodeSize ;
                node.left = null ;
                rebalanceDelete(node) ;
            }
            else
            {
                T item = leaf.removeTop() ;
                node.insertAt(0, item) ;
            }
            return ;
        }

        error("Unknown node type");
    }

    @Override public T max()
    { 
        TTreeNode<T> node = TTreeNode.getRightDeep(root) ;
        return node.getMax() ;
    }
    
    @Override public T min()
    { 
        TTreeNode<T> node = TTreeNode.getLeftDeep(root) ;
        return node.getMin() ;
    }
    
    /** Rebalance one node - return true if a rotation is performed */
    // Currently returns true if the height changes
    private boolean rebalanceNode(TTreeNode<T> node)
    {
        if ( node == null )
            return false ;
        
        if ( Logging )
        {
            log(">> Rebalance : %s", node) ;
            output() ;
        }
        
        
        int bal = balance(node) ;
        if ( Logging )
            log("-- Rebalance : %d", bal) ;
        
        if ( bal < -2 || bal > 2 )
            brokenTree(node, "Unbalanced") ;
     
        int h = height(node) ;
        
        if ( bal == 1 || bal == 0 || bal == -1 )
        {
            setHeight(node) ;   // Propagate height up tree
            if ( Logging )
                log("<< Rebalance : %s", node) ;
            return h != height(node) ;
        }
        
        if ( bal == 2 )
        {
            // Right heavy :
            // If we added to the right under a right add then pivotRight
            // If we added to the left under a right add then pivotRightLeft
            if ( height(node.right.left) > height(node.right.right) )
                // Right, then left subnode heavy - double rotation.
                pivotRightLeft(node) ;
            else
                // Right-right heavy
                pivotRight(node) ;
        }
        else// if ( bal == -2 )
        {
            // Right, then left heavy:
            if ( height(node.left.right) > height(node.left.left) )
                pivotLeftRight(node) ;
            else
                pivotLeft(node) ;
        }
        setHeight(node) ;

        if ( Logging )
            log("<< Rebalance : %s", node) ;
        return h != height(node) ;
    }

    static <T extends Comparable<? super T>> int balance(TTreeNode<T> node)
    {
        if ( node == null )
            return 0 ;
        return height(node.right) - height(node.left) ;
    }
    
    private static <T extends Comparable<? super T>> void setHeight(TTreeNode<T> node)
    {
        //if ( node == null ) return ;
        node.height = Math.max(height(node.left), height(node.right)) + 1;
    }
    
    private static <T extends Comparable<? super T>> int height(TTreeNode<T> node)
    {
        if ( node == null )
            return InitialHeight-1 ;
        return node.height ;
    }
    
    private void pivotLeft(TTreeNode<T> node)
    {
        if ( Logging )
            log(">> pivotLeft : %s", label(node)) ;
        
        if ( Verbose )
            output() ;
        
        // Validity checking?
        if ( Checking ) checkNotNull(node.left) ;
        
        TTreeNode<T> n = node.left ;
        T[] r1 = node.elements ;
        int r1Size = node.nodeSize ;
        T[] r2 = n.elements ;
        int r2Size = n.nodeSize ;
        
        TTreeNode<T> a = n.left ;
        TTreeNode<T> b = n.right ;
        TTreeNode<T> c = node.right ;
        
        // Reuse n as the node (R1 B C) 
        n.set(r1, r1Size, node, b, c) ;
        setHeight(n) ;
        
        // Move to set?
        if ( a != null ) a.parent = node ;
        if ( b != null ) b.parent = n ;     // No-op
        if ( c != null ) c.parent = n ;
        
        node.set(r2, r2Size, node.parent, a, n) ;
        setHeight(node) ; 
        
        if ( Checking )
            node.checkDeep(this) ;
        if ( Logging )
            log("<< pivotLeft : %s", label(node)) ;
    }
    
    // (R1 A (R2 B C)) ==> (R2 (R1 A B) C)  
    private void pivotRight(TTreeNode<T> node)
    {
        if ( Logging )
            log(">> pivotRight : %s", label(node)) ;

        if ( Verbose )
            output() ;

        if ( Checking )
            checkNotNull(node.right) ;
        // Take nodes apart
        TTreeNode<T> n = node.right ;
        T[] r1 = node.elements ;
        int r1Size = node.nodeSize ;
        T[] r2 = n.elements ;
        int r2Size = n.nodeSize ;
        
        TTreeNode<T> a = node.left ;
        TTreeNode<T> b = n.left ;
        TTreeNode<T> c = n.right ;

        // Reuse n as the node (R1 A B) 
        n.set(r1, r1Size, node, a, b) ;
        setHeight(n) ;
        
        if ( a != null ) a.parent = n ;
        if ( b != null ) b.parent = n ;
        if ( c != null ) c.parent = node ;
        
        node.set(r2, r2Size, node.parent, n, c) ;
        setHeight(node) ;
        
        if ( Checking )
            node.checkDeep(this) ;
        if ( Logging )
            log("<< pivotRight : %s", label(node)) ;
    }
    
    // LeftRight
    // (R3 (R1 A (R2 B C)) D) ==> (R2 (R1 A B) (R3 C D))
    private void pivotLeftRight(TTreeNode<T> node)
    {
        if ( Logging )
            log(">> pivotLeftRight : %s", label(node)) ;
        
        if ( Verbose )
            output() ;

        
        if ( Checking ) 
        {
            checkNotNull(node.left) ;
            checkNotNull(node.left.right) ;
        }
        
        // Take apart ...
        TTreeNode<T> n1 = node.left ;
        TTreeNode<T> n2 = node.left.right ;
        
        T[] r3 = node.elements ;
        int r3Size = node.nodeSize ;
        
        T[] r1 = n1.elements ;
        int r1Size = n1.nodeSize ;

        T[] r2 = n2.elements ;
        int r2Size = n2.nodeSize ;
        // Check new top node (leaf becomes internal)
        if ( r2Size == 1 )
        {
            // From the T-Tree paper:
            // A is r3 = node
            // B is r1 = n1 
            // C is r2 = n2
            // Slide els from B to C
            if ( Logging )
                log("** Special case LR") ;
            if ( Checking )
            {
                if ( ! n2.isLeaf() )            warn("LR: Not a leaf (C)") ;
                if ( ! n1.isLeftHalfLeaf() )    warn("LR: Not a left half-leaf (B)") ;
                if ( ! node.isRightHalfLeaf() ) warn("LR: Not a right half-leaf (A)") ;
            }
            // Slide els from B(r1) to C(r2)
            // Old C element
            T x = r2[0] ;
            // New C elements
            System.arraycopy(r1, 1, r2, 0, r1Size-1) ;
            r2[r1Size-1] = x ;
            r2Size = r1Size ;
            n2.nodeSize = r1Size ;
            // New B element (singular)
            ArrayOps.clear(r1, 1, r1Size-1) ;
            n1.nodeSize = 1 ;
            r1Size = 1 ;
        }

        
        TTreeNode<T> a = n1.left ;
        TTreeNode<T> b = n2.left ;
        TTreeNode<T> c = n2.right ;
        TTreeNode<T> d = node.right ;
        
        // Reuse nodes ; n1 becomes the R1 node, n2 the R3 node.
        n1.set(r1, r1Size, node, a, b) ;
        setHeight(n1) ;
        n2.set(r3, r3Size, node, c, d) ;
        setHeight(n2) ;
        
        if ( a != null ) a.parent = n1 ;
        if ( b != null ) b.parent = n1 ;
        if ( c != null ) c.parent = n2 ;
        if ( d != null ) d.parent = n2 ;
        
        node.set(r2, r2Size, node.parent, n1, n2) ;
        setHeight(node) ;
        
        if ( Checking )
            node.checkDeep(this) ;
        
        if ( Logging )
            log("<< pivotLeftRight : %s", label(node)) ;
    }
    
    // RightLeft
    // (R1 A (R3 (R2 B C) D)) ==> (R2 (R1 A B) (R3 C D))
    private void pivotRightLeft(TTreeNode<T> node)
    {
        if ( Logging )
            log(">> pivotRightLeft : %s", label(node)) ;
        
        if ( Verbose )
            output() ;

        if ( Checking )
        {
            checkNotNull(node.right) ;
            checkNotNull(node.right.left) ;
        }
        TTreeNode<T> n1 = node.right ;
        TTreeNode<T> n2 = node.right.left ;
        
        T[] r1 = node.elements ;
        int r1Size = node.nodeSize ;
        
        T[] r3 = n1.elements ;
        int r3Size = n1.nodeSize ;
        
        T[] r2 = n2.elements ;
        int r2Size = n2.nodeSize ;
        // Check new top node (leaf becomes internal)
        if ( r2Size == 1 )
        {
            // A = node ; B = n1 ; C = n2
            if ( Logging )
                log("** Special case RL") ;
            if ( Checking )
            {
                if ( ! n2.isLeaf() )            warn("RL: Not a leaf (C)") ;
                if ( ! n1.isRightHalfLeaf() )   warn("RL: Not a right half-leaf (B)") ;
                if ( ! node.isLeftHalfLeaf() )  warn("RL: Not a left half-leaf (A)") ;
            }
            // Slide els from B(r1) to C(r2)
         // Old C element
            T x = r2[0] ;
            // New C elements
            System.arraycopy(r1, 1, r2, 0, r1Size-1) ;
            r2[r1Size-1] = x ;
            r2Size = r1Size ;
            n2.nodeSize = r1Size ;
            // New B element (singular)
            ArrayOps.clear(r1, 1, r1Size-1) ;
            n1.nodeSize = 1 ;
            r1Size = 1 ;
        }
        
        TTreeNode<T> a = node.left ;
        TTreeNode<T> b = n2.left ;
        TTreeNode<T> c = n2.right ;
        TTreeNode<T> d = n1.right ;
        
        // Reuse nodes ; n1 becomes the R1 node, n2 the R3 node.
        n1.set(r1, r1Size, node, a, b) ;
        setHeight(n1) ;
        n2.set(r3, r3Size, node, c, d) ;
        setHeight(n2) ;
        
        if ( a != null ) a.parent = n1 ;
        if ( b != null ) b.parent = n1 ;
        if ( c != null ) c.parent = n2 ;
        if ( d != null ) d.parent = n2 ;
        
        node.set(r2, r2Size, node.parent, n1, n2) ;
        setHeight(node) ;

        if ( Checking )
            node.checkDeep(this) ;
        
        if ( Logging )
            log("<< pivotRightLeft : %s", label(node)) ;
    }
 
 
    @Override
    final public void checkTree()
    {
        if ( ! Checking )
            return ;
        if ( root != null )
        {
            if ( root.parent != null )
                brokenTree(root, "Root parent is not null") ;
            root.checkDeep(this) ;
        }
            
    }
    
    final static void checkNotNull(Object object)
    {
        if ( object == null )
            throw new TreeException("Null") ;
    }

    final void brokenTree(TTreeNode<T> node, String msg)
    {
        IndentedWriter.stderr.println();
        output(IndentedWriter.stderr) ;
        throw new TTreeException(msg+" : "+node.toString()) ;
    }
    
    @Override
    public Iterator<T> iterator() { return iterator(null, null) ; }
    

    @Override
    public Iterator<T> iterator(T fromItem, T toItem)
    { return TTreeIterator.iterator(this, fromItem, toItem) ; }

    @Override
    public long size() 
    { return count() ; } 
    
    // Size by actually counting the tree
    @Override
    public long count() 
    { 
        if ( root == null )
            return 0 ;
        return root.sizeDeep() ;
    }

    /** Collect all the elements in the TTree into a list and return the list. */
    @Override
    public List<T> elements()
    {
        List<T> x = new ArrayList<T>() ;
        if ( root == null )
            return x ;
        // Avoids using an iterator but instead directly goes to the tree structure
        // Can test the iterator code with this!
        root.elements(x) ;
        return x ;
    }
    

    @Override
    public String toString() { return PrintUtils.toString(this) ; }

    public void output()
    {
        output(stdout) ;
    }

    @Override
    public void output(IndentedWriter out)
    {
        if ( root == null )
            out.print("<empty>") ;
        else
            root.outputNested(out, true) ;
        out.ensureStartOfLine() ;
        out.flush();
    }
    
    // ---- Workers
    private static void log(String msg)
    { 
        if ( log.isDebugEnabled() )
            log.debug(msg) ;
    }
    
    private static void log(String fmt, Object ...args)
    { 
        if ( log.isDebugEnabled() )
            log.debug(String.format(fmt, args)) ;
    }

    static void warn(String msg)
    { 
        System.out.flush() ;
        System.err.println(msg) ;
    }
    
    
    static void warn(String fmt, Object ...args)
    { warn(String.format(fmt, args)) ; }

    static void error(String msg)
    { throw new TTreeException(msg) ; }
    
    static void error(String fmt, Object ...args)
    { error(String.format(fmt, args)) ; }
}
