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

package structure.avl;

import static java.lang.String.format ;
import static org.apache.jena.atlas.io.IndentedWriter.stderr ;
import static org.apache.jena.atlas.io.IndentedWriter.stdout ;
import static structure.avl.AvlNode.label ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.iterator.Iter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import structure.OrderedSet ;
import structure.tree.TreeException ;

public class AVL<T extends Comparable<? super T>> implements Printable, OrderedSet<T>
{
    private static Logger log = LoggerFactory.getLogger(AVL.class) ;
    
    /* ==== AVL to do
     * Re-enable all of test "del_10" 
     * + Boolean returns on delete
     * + Size by tracking ins/del
     */
    
    /* AVL Tree.
     * 
     *  http://en.wikipedia.org/wiki/AVL_tree
     * and on the rotations aspect:
     *   http://en.wikipedia.org/wiki/Tree_rotation
     * 
     * And this is useful:
     * http://fortheloot.com/public/AVLTreeTutorial.rtf
     * lance
     * 
     * Balance factor: height(right) - height(left)
     * and should in the range -2 to 2.
     * 
     * (NB Some descriptions use "left-right")
     * 
     * Notation: tuples of the form (A B C) are used to mean
     * a node A with left B and right C
     * 
     *    A
     *   / \
     *  B   C
     *   
     * "A" "B" "C" for nodes, and "R1" "R2" "R3" are the record values:   
     * 
     * (R1 A (R2 B C)) is:
     * 
     *    R1
     *   /  \
     *  A    R2
     *      /  \
     *     B    C
     */
    
    // In rotations, the code keeps the top node object as the top node.
    /** The height of node with no subtrees : a node below this (i.e. null) has height InitialHeight-1 */
    static final int InitialHeight = 1 ;
    
    public static boolean Checking = false ;
    public static boolean Verbose = false ;
    public static boolean Logging = false ;

    private AvlNode<T> root = null ;
    
    //---
    
    public AVL()
    {
    }

    @Override
    public boolean contains(T record)
    { return search(record) != null ; }

    @Override
    public boolean isEmpty()
    { return root == null ; }
    
    @Override
    public T search(T record)
    {
        // Remember the input record may be a part key.
        if ( root == null )
            return null ;
        AvlNode<T> n = find(record) ;
        return record(n) ;
    }
    
    @Override
    public boolean add(T newRecord)
    { 
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> insert(%s)", newRecord)) ;
        if ( Verbose )
            output() ;
        boolean b = insertAtNode(newRecord) ;
        
        if ( Verbose )
            output() ;
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< insert(%s)", newRecord)) ;
        checkTree() ;
        return b ;
    }
    
    @Override
    public boolean remove(T record)
    { 
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> delete(%s)", record)) ;
        if ( Verbose )
            output() ;
        if ( root == null )
            return false ;

        // TEMP - cirrect but two tree walks.
//        boolean b = contains(record) ;
//        root = delete1(record, root) ;
        
        boolean b = delete(root, record) ; 
        
        if ( Verbose )
            output() ;
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< delete(%s)", record)) ;
        checkTree() ;
        return b ;
    }
    
    @Override
    public T max()
    {
        if ( root == null )
            return null ;
        AvlNode<T> node = root ;
        AvlNode<T> n2 = null ;
        
        while ( node != null )
        {
            n2 = node ;
            node = node.right ;
        }
        return n2.record ;
    }

    @Override
    public T min()
    {
        if ( root == null )
            return null ;
        AvlNode<T> node = root ;
        AvlNode<T> n2 = null ;
        while ( node != null )
        {
            n2 = node ;
            node = node.left ; 
        }
        return n2.record ;
    }

    @Override
    public void clear()         { root = null ; }
    
    // -------- Search
    
    // Find the parent of maximal node
    // i.e. for (A 
    //             (B (D E) (F G)) 
    //             (H (I J) (K L)) )
    // return H (max node is .right => L)
    
    /** Find the record that is same as or least higher than the given record.
     *  Return null if no such node. */
    
    public T findRecordAbove(T record)
    {
        AvlNode<T> n = findNodeAbove(record) ;
        return record(n) ;
    }

    AvlNode<T> findNodeAbove(T record)
    {
        if ( record == null )
        {
            if ( root == null )
                return null ;
            AvlNode<T> node = root ;
            while ( node.left != null )
                node = node.left ; 
            return node ;
        }
        
        return findNodeAbove(root, record) ;
    }

    
    /** Find the record that is same as or greatest lower record than the given record.
     *  Return null if no such node.
     */
    
    public T findRecordBelow(T record)
    {
        AvlNode<T> n = findNodeBelow(record) ;
        return record(n) ;
    }
    
    AvlNode<T> findNodeBelow(T record)
    {
        if ( record == null )
        {
            if ( root == null )
                return null ;
            AvlNode<T> node = root ;
            while ( node.right != null )
                node = node.right ; 
            return node ;
        }
        
        return findNodeBelow(root, record) ;
    }

    // Go down right until we find exact or overshoot.
    private static <R extends Comparable<? super R>> AvlNode<R> findNodeAbove(AvlNode<R> node, R record)
    {
        if ( node == null )
            return null ;
        int x = record.compareTo(node.record) ;
        if ( x < 0 )
            // This node above - switch to phase2.
            return findNodeAbove(node, record, node) ;
        else if ( x > 0 )
            // This node below - try right.
            return findNodeAbove(node.right, record) ;
        else
            return node ;
    }

    private static <R extends Comparable<? super R>> AvlNode<R> findNodeAbove(AvlNode<R> node, R record, AvlNode<R> bestGuess)
    {
        if ( node == null )
            // Hit bottom and still less.
            return bestGuess ;
        
        int x = record.compareTo(node.record) ;
        if ( x < 0 )
            // Somewhere in the left tree , but maybe this node.
            return findNodeAbove(node.left, record, node) ; 
        else if ( x > 0 )
            // This node is below the target.  
            return bestGuess ;
        else
            // Found.
            return node ;
    }

    // Go down left until we find exact or undershoot.
    private static <R extends Comparable<? super R>> AvlNode<R> findNodeBelow(AvlNode<R> node, R record)
    {
        if ( node == null )
            return null ;
        int x = record.compareTo(node.record) ;
        if ( x < 0 )
            // This node is above - keep looking
            return findNodeBelow(node.left, record) ;
        else if ( x > 0 )
            // This node below - switch to phase 2  
            return findNodeBelow(node, record, node) ;
        else
            return node ;
    }

    private static <R extends Comparable<? super R>> AvlNode<R> findNodeBelow(AvlNode<R> node, R record, AvlNode<R> bestGuess)
    {
        if ( node == null )
            // Hit bottom and still more
            return bestGuess ;
        
        int x = record.compareTo(node.record) ;
        if ( x < 0 )
            // This node is above the target.  
            return bestGuess ;
        else if ( x > 0 )
            // Somewhere in the right tree, but maybe this node.
            return findNodeBelow(node.right, record, node) ;
        else
            // Found.
            return node ;
    }

    private AvlNode<T> find(T record)
    {
        // Remember the input record may be a part key.
        if ( root == null )
            return null ;
        AvlNode<T> node = root ;
        //AvlNode<R> n2 = null ;
        
        while ( node != null )
        {
            int x = record.compareTo(node.record) ;
            if ( x < 0 )
                node = node.left ;
            else if ( x > 0 )
                node = node.right ;
            else
                // Found.
                return node ;
        }
        return null ;
    }

    // -------- Insert
    
    // Insert a record - return true on change (i.e. not already in tree)
    private boolean insertAtNode(T newRecord)
    {
        if ( root == null )
        {
            if ( Verbose )
                log.debug("-- insertAtNode : new root") ;
            root = new AvlNode<T>(newRecord, null) ;
            return true ;
        }
        
        AvlNode<T> node = root ;
        AvlNode<T> parent = root.parent ;

        while( node != null )
        {
            int x = newRecord.compareTo(node.record) ;
            if ( x < 0 )
            {
                parent = node ;
                node = node.left ;
                if ( node == null )
                {
                    parent.left = new AvlNode<T>(newRecord, parent) ;
                    break ;
                }
            }
            else if ( x > 0 )
            {
                parent = node ;
                node = node.right ;
                if ( node == null )
                {
                    parent.right = new AvlNode<T>(newRecord, parent) ;
                    break ;
                }
            }
            else // x == 0 : Same : no action needed, no rebalance.
            {
                if ( Verbose )
                    log.debug(format("insertAtNode same %s", label(node))) ;
                T rec = node.record ;
                node.record = newRecord ;       // Records may be partial.
                return ! rec.equals(newRecord) ;
            }
        }
        
        // Bottom of tree.  node == null.
        // Set heights and rebalance.
        rebalanceInsert(parent) ;
        return true ;
    }

    private void rebalanceInsert(AvlNode<T> node)
    {
        // Need to abort early.
        while ( node != null )
        {
            if ( ! rebalance(node) )
                // This is still too conservative. ??
                return ;
            node = node.parent ;
        }
        return ;  
    }
    
    private void rebalanceDelete(AvlNode<T> node)
    {
        while ( node != null )
        {
            rebalance(node) ;
            node = node.parent ;
        }
        return ;  
    }
    
    // -------- Delete
    
    private boolean delete(AvlNode<T> node, T record)
    {
        checkNotNull(node) ;
        while( node != null )
        {
            int x = record.compareTo(node.record) ;
            if ( x < 0 )
                node = node.left ;
            else if ( x > 0 )
                node = node.right ;
            else // x == 0
                break ;
        }
        if ( node == null )
            // Not found.
            return false ;
        
        // -- swapNode is the node with the replacement record.
        // If node is a leaf, then swapNode == node
        AvlNode<T> swapNode ;
        if ( node.left != null )
        {
            swapNode = getRightDeep(node.left) ;
            // Swap in value from leaf.
            node.record = swapNode.record ;
        }
        else if ( node.right != null )
        {
            swapNode = getLeftDeep(node.right) ;
            // Swap in value from leaf.
            node.record = swapNode.record ;
        }
        else
            // Already a leaf : No record swap need 
            swapNode = node ;

        // So swapNode is the min or max of a subtree below "node".
        // It is a half-leaf or leaf (or it would not be a min or max). 
        
        // -- The replacement tree.  Maybe null.
        AvlNode<T> subTree ;
        if ( swapNode.left != null )
            subTree = swapNode.left ;
        else if ( swapNode.right != null )
            subTree = swapNode.right ;
        else
            // swapNode is a leaf.
            subTree = null ;
        
        if ( subTree != null )
            // Half-leaf - splice out swapNode.
            subTree.parent = swapNode.parent ;
        
        if ( swapNode.parent == null )
        {
            // At root.
            root = subTree ;
            //rebalanceDelete(root) ;
            return true ;
        }
        
        // Replace in parent.
        if ( swapNode.parent.left == swapNode )
            swapNode.parent.left = subTree ;
        else
            swapNode.parent.right = subTree ; 
        rebalanceDelete(swapNode.parent) ;
        return true ;
    }

    // Uses the fact that that the pivot operations keep the original node object
    // as the top of the pivoted structure.
    // Returns whether the height of the node changed.
    
    private boolean rebalance(AvlNode<T> node)
    {
        if ( node == null )
            return false ;
        
        if ( Verbose )
        {
            log.debug(format(">> Rebalance : %s", label(node))) ;
            //output() ;
        }
        
        int bal = balance(node.left, node.right) ;
        
        if ( bal < -2 || bal > 2 )
            brokenTree(node, "Unbalanced") ;
     
        int h = height(node) ;
        
        if ( bal == 1 || bal == 0 || bal == -1 )
        {
            setHeight(node) ;
            checkNode(node) ;
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
        checkNode(node) ;

        if ( Verbose )
            log.debug(format("<< Rebalance : %s", label(node))) ;
        return h != height(node) ;
    }

    private static <R extends Comparable<? super R>> R record(AvlNode<R> n)
    {
        return (n == null) ? null : n.record ;
    }

    private static <R extends Comparable<? super R>> void setHeight(AvlNode<R> node)
    {
        //if ( node == null ) return ;
        node.height = Math.max(height(node.left), height(node.right)) + 1; 
    }
    
    private static <R extends Comparable<? super R>> int height(AvlNode<R> node)
    {
        if ( node == null )
            return InitialHeight-1 ;
        return node.height ;
    }
    
    static <R extends Comparable<? super R>> int balance(AvlNode<R> left, AvlNode<R> right)
    {
        return height(right) - height(left) ;
    }

    // ---- Rotations
    // Naming: pivotRight means move the left child up to the root and the root to the right
    // The left is the pivot 
    // == shift right
    // == clockwise
    // This is the wikipedia naming but that does not extend to the double rotations.
    // 
    // Different books have different namings, based on the location of the pivot (which would be a left rotate)
    // But when we talk about double rotations, the pivotLeft terminolgy works better.
    // pivotLeft (= case left left) , pivotLeftRight (case left right), etc
    
    // These operations act in-place : the nodes reused so the top node (the argument) remain the same object 
    
    // (R1 (R2 A B) C) ==> (R2 A (R1 B C))
    private void pivotLeft(AvlNode<T> node)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> pivotLeft : %s", label(node))) ;
        
        // Validity checking?
        checkNotNull(node.left) ;
        
        AvlNode<T> n = node.left ;
        T r1 = node.record ;
        T r2 = n.record ;
        
        AvlNode<T> a = n.left ;
        AvlNode<T> b = n.right ;
        AvlNode<T> c = node.right ;
        
        // Reuse n as the node (R1 B C) 
        n.set(r1, node, b, c) ;
        setHeight(n) ;
        
        // Move to set?
        if ( a != null ) a.parent = node ;
        if ( b != null ) b.parent = n ;     // No-op
        if ( c != null ) c.parent = n ;
        
        node.set(r2, node.parent, a, n) ;
        setHeight(node) ; 
        
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< pivotLeft : %s", label(node))) ;
    }
    
    // (R1 A (R2 B C)) ==> (R2 (R1 A B) C)  
    private void pivotRight(AvlNode<T> node)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> pivotRight : %s", label(node))) ;

        checkNotNull(node.right) ;
        // Take nodes apart
        AvlNode<T> n = node.right ;
        T r1 = node.record ;
        T r2 = n.record ;
        AvlNode<T> a = node.left ;
        AvlNode<T> b = n.left ;
        AvlNode<T> c = n.right ;

        // Reuse n as the node (R1 A B) 
        n.set(r1, node, a, b) ;
        setHeight(n) ;
        
        if ( a != null ) a.parent = n ;
        if ( b != null ) b.parent = n ;
        if ( c != null ) c.parent = node ;
        
        node.set(r2, node.parent, n, c) ;
        setHeight(node) ;
        
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< pivotRight : %s", label(node))) ;
    }
    
    // LeftRight
    // (R3 (R1 A (R2 B C)) D) ==> (R2 (R1 A B) (R3 C D))
    private void pivotLeftRight(AvlNode<T> node)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> pivotLeftRight : %s", label(node))) ;
        checkNotNull(node.left) ;
        checkNotNull(node.left.right) ;
        
        // Take apart ...
        AvlNode<T> n1 = node.left ;
        AvlNode<T> n2 = node.left.right ;
        
        T r3 = node.record ;
        T r1 = n1.record ;
        T r2 = n2.record ;
        AvlNode<T> a = n1.left ;
        AvlNode<T> b = n2.left ;
        AvlNode<T> c = n2.right ;
        AvlNode<T> d = node.right ;
        
        // Reuse nodes ; n1 becomes the R1 node, n2 the R3 node.
        n1.set(r1, node, a, b) ;
        setHeight(n1) ;
        n2.set(r3, node, c, d) ;
        setHeight(n2) ;
        
        if ( a != null ) a.parent = n1 ;
        if ( b != null ) b.parent = n1 ;
        if ( c != null ) c.parent = n2 ;
        if ( d != null ) d.parent = n2 ;
        
        node.set(r2, node.parent, n1, n2) ;
        setHeight(node) ;
        
        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< pivotLeftRight : %s", label(node))) ;
    }
    
    // RightLeft
    // (R1 A (R3 (R2 B C) D)) ==> (R2 (R1 A B) (R3 C D))
    private void pivotRightLeft(AvlNode<T> node)
    {
        if ( Logging && log.isDebugEnabled() )
            log.debug(format(">> pivotRightLeft : %s", label(node))) ;
        checkNotNull(node.right) ;
        checkNotNull(node.right.left) ;
        AvlNode<T> n1 = node.right ;
        AvlNode<T> n2 = node.right.left ;
        
        T r1 = node.record ;
        T r3 = n1.record ;
        T r2 = n2.record ;
        AvlNode<T> a = node.left ;
        AvlNode<T> b = n2.left ;
        AvlNode<T> c = n2.right ;
        AvlNode<T> d = n1.right ;
        
        // Reuse nodes ; n1 becomes the R1 node, n2 the R3 node.
        n1.set(r1, node, a, b) ;
        setHeight(n1) ;
        n2.set(r3, node, c, d) ;
        setHeight(n2) ;
        
        if ( a != null ) a.parent = n1 ;
        if ( b != null ) b.parent = n1 ;
        if ( c != null ) c.parent = n2 ;
        if ( d != null ) d.parent = n2 ;
        
        node.set(r2, node.parent, n1, n2) ;
        setHeight(node) ;

        if ( Logging && log.isDebugEnabled() )
            log.debug(format("<< pivotRightLeft : %s", label(node))) ;
    }
    
    // ---- Iteration
    
    @Override
    public Iterator<T> iterator()               { return iterator(null, null) ; }

    @Override
    public Iterator<T> iterator(T r1, T r2)     { return AvlIterator.iterator(this, r1, r2) ; }
    
    public Iterable<T> records()                { return records(null, null) ; }
    
    public Iterable<T> records(T r1, T r2)      { return ()->Iter.iter(AvlIterator.iterator(this, r1, r2)) ; }

    public List<T> calcRecords()                { return calcRecords(null, null) ; }
    
    public List<T> calcRecords(T r1, T r2)      { return AvlIterator.calcIter(root, r1, r2) ; }
    

    static <R extends Comparable<? super R>> AvlNode<R> getRightDeep(AvlNode<R> node)
    {
        AvlNode<R> node2 = node.right ;
        while( node2 != null )
        {
            node = node2 ;
            node2 = node2.right ;
        }
        return node ;
    }
    
    static <R extends Comparable<? super R>> AvlNode<R> getLeftDeep(AvlNode<R> node)
    {
        AvlNode<R> node2 = node.left ;
        while( node2 != null )
        {
            node = node2 ;
            node2 = node2.left ;
        }
        return node ;
    }

    
    @Override
    public long size()  { return count() ; }
    
    @Override
    public long count()
    {
        if ( root == null )
            return 0 ;
        return root.count() ;
    }
    
    /** Collect all the elements in the AVL into a list and return the list. */
    @Override
    public List<T> elements()
    {
        List<T> x = new ArrayList<T>() ;
        if ( root == null )
            return x ;
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
    
    private void checkNode(AvlNode<T> node)
    {
        if ( ! Checking )
            return ;
        checkNotNull(node) ;
        try {
            node.checkDeep() ;
        } catch (TreeException ex)
        {
            try {
                stderr.println("****") ;
                output(stderr) ;
                stderr.println("****") ;
                stderr.flush();
            } catch (Exception ex2) { stderr.ensureStartOfLine() ; }   // try to dump tree
            throw ex ;
        }
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
            root.checkDeep() ;
        }
            
    }
    
    
    final static void checkNotNull(Object object)
    {
        if ( object == null )
            throw new TreeException("Null") ;
    }

    final void brokenTree(AvlNode<T> node, String msg)
    {
        IndentedWriter.stderr.println();
        output(IndentedWriter.stderr) ;
        throw new TreeException(msg+" : "+node.toString()) ;
    }
}
