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

import static structure.ttree.TTree.Checking ;
import static structure.ttree.TTree.error ;

import java.util.List ;
import java.util.Objects ;

import lib.ArrayOps ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.lib.Alg ;

final
class TTreeNode<T extends Comparable<? super T>> implements Printable
{
    // Debug
    private static int counter = 0 ;
    // Make this static (or remove).
    private int id ;
    
    int height = TTree.InitialHeight ;      // New nodes are always leaves.
    TTreeNode<T> parent ;
    TTreeNode<T> left ;
    TTreeNode<T> right ;
    // Need to record start and stop if want slicing.
    // Or nulls at low end during insert into a full node.
    int nodeSize ; 
    T elements[] ;
    
    /** Create a new T-Tree node */
    @SuppressWarnings("unchecked")
    TTreeNode(TTreeNode<T>parent, int size)
    {
        id = (++counter) ;
        this.elements = (T[])new Comparable[size] ;
        this.nodeSize = 0 ;
        this.height = TTree.InitialHeight ;
        this.parent = parent ;
        //Arrays.fill(elements, null) ;
    }

    void set(T[] elements, int els, TTreeNode<T> parent, TTreeNode<T> left, TTreeNode<T> right)
    {  
        this.elements = elements ;
        this.nodeSize = els ;
        this.parent = parent ;
        this.left = left ;
        this.right = right ;
        this.parent = parent ;
        this.height = -1 ;
    }
    
    /** Insert a item into a node.
     *  
     * @param item
     * @return true if the node changed (replace a different element or insert the element) 
     */
    boolean add(T item)
    { 
        int idx = find(item) ;

        if ( idx < 0 )
        {
            if ( elements.length == nodeSize )
                error("Already full") ;
            insertAt(Alg.decodeIndex(idx), item) ;
            return true ;
        }
        else
        {
            T orig = elements[idx] ;
            if ( Objects.equals(item, orig) )
                return false ;
            elements[idx] = item ;
            return true ;
        }
    }
    
    void insertAt(int idx,T item)
    {
        ArrayOps.insert(elements, idx, item) ;
        nodeSize++ ;
    }
    
    T removeBottom()
    {
        if ( Checking && isEmpty() )
            throw new TTreeException("node empty") ;
        T item = elements[0] ;
        ArrayOps.shiftDown(elements, 0, nodeSize) ;
        nodeSize-- ;
        return item ;
    }
    
    T removeTop()
    {
        if ( Checking && isEmpty() )
            throw new TTreeException("node empty") ;
        T item = elements[nodeSize-1] ;
        if ( TTree.NullOut ) elements[nodeSize-1] = null ;
        nodeSize-- ;
        return item ;
    }
     
    /** Find an item - return the index in the array or -(index+1)
     * encoding the insertion point.  
     * 
     * @param item
     * @return encoded index.
     */
    int find(T item)
    {
        int x = Alg.binarySearch(elements, 0, nodeSize, item) ;
        return x ;
    }
    
    /** Delete from this TTreeNode
     * 
     * @param item
     * @return true if a change to the node occurred 
     */
    boolean delete(T item)
    { 
        if ( elements.length == 0 )
            error("Already empty") ;
        int idx = find(item) ;
        if ( idx < 0 )
            return false ;
        T item2 = ArrayOps.delete(elements, idx) ;
        nodeSize-- ;
        //if ( Checking ) check() ; // Can be invalid pending fixup
        return true ;
    }
    
    boolean isFull()             { return nodeSize == elements.length ; }
    boolean isEmpty()            { return nodeSize == 0 ; }
    
    /** Both sides have nodes below them */
    boolean isInternal()         { return left != null && right != null ; }
    
    /** One side or the other has a node, the other does not */
    boolean isHalfLeaf()         { return isLeftHalfLeaf() || isRightHalfLeaf() ; }
    
    /** LeftHalfLeaf - no node below on the left, but there is one on the right */
    boolean isLeftHalfLeaf()     { return left == null && right != null ; } 
    /** LeftHalfLeaf - node below on the left, but not on the right */
    boolean isRightHalfLeaf()    { return left != null && right == null ; } 
    
    /** No nodes below this one, to left or to the right */
    boolean isLeaf()             { return left == null && right == null ; }
    
    int nodeSize()                { return nodeSize ; }
    
    private static final String undef = "_" ;
    /** Only makes sense when the "id" is being allocated for all nodes */
    static <T extends Comparable<? super T>> String label(TTreeNode<T> n)
    {
        if ( n == null )
            return undef ;
        return Integer.toString(n.id) ;
    }

    
    T getMin()
    {
        if ( isEmpty() )
            return null ;
        return elements[0] ;
    }

    T getMax()
    {
        if ( isEmpty() )
            return null ;
        return elements[nodeSize-1] ;
    }

//    T getGreatestLowerBound()
//    {
//        if ( isEmpty() )
//            return null ;
//        TTreeNode<T> node = this ;
//        if ( node.left != null )
//            node = getRightDeep(node.left) ;
//        return node.getMax() ;
//    }
//
//    T getLeastUpperBound()
//    {
//        if ( isEmpty() )
//            return null ;
//        TTreeNode<T> node = this ;
//        if ( node.right != null )
//            node = getLeftDeep(node.right) ;
//        return node.getMin() ;
//    }

    static <T extends Comparable<? super T>> TTreeNode<T> getLeftDeep(TTreeNode<T> node)
    {
        TTreeNode<T> node2 = node.left ;
        while( node2 != null )
        {
            node = node2 ;
            node2 = node2.left ;
        }
        return node ;
    }
        
    static <T extends Comparable<? super T>> TTreeNode<T> getRightDeep(TTreeNode<T> node)
    {
        TTreeNode<T> node2 = node.right ;
        while( node2 != null )
        {
            node = node2 ;
            node2 = node2.right ;
        }
        return node ;
    }

//    TTreeNode<T> getParent()
//    {
//        return parent ;
//    }
//
//    TTreeNode<T> getLeft()
//    {
//        return left ;
//    }
//
//    TTreeNode<T> getRight()
//    {
//        return right ;
//    }

    void elements(List<T> acc)
    {
        if ( left != null )
            left.elements(acc) ;
        for ( T item : elements )
        {
            if ( item == null ) break ;
            acc.add(item) ;
        }
        if ( right != null )
            right.elements(acc) ;
    }

    long sizeDeep()
    {
        long size = 0 ;
        if ( left != null )
            size += left.sizeDeep() ;
        size += nodeSize ;
        if ( right != null )
            size += right.sizeDeep() ;
        return size ;
    }
    
    // ---- Output
    @Override
    public void output(IndentedWriter out)
    {
        out.printf("id=%d parent=%s h=%d len=%d left=%s right=%s [",id, label(parent), height, nodeSize, label(left), label(right)) ;
        for ( int i = 0 ; i < nodeSize ; i++ )
        {
            if ( i != 0 ) out.print(" ") ;
            out.print(elements[i].toString()) ;
        }
        out.print("]") ;
    }
    
    /** Print structured */
    void outputNested(IndentedWriter out, boolean detailed)
    {
        out.print("(") ;
        output(out) ;
        if ( left == null && right == null )
        {
            out.println(")") ;
            return ;
        }
        out.println() ;
        

        out.incIndent() ;
        if ( left != null )
        {
            out.ensureStartOfLine() ;
            left.outputNested(out, detailed) ;
        }
        else
        {
            out.ensureStartOfLine() ;
            out.println("()") ;
        }
        out.decIndent() ;

        out.incIndent() ;
        if ( right != null )
        {
            out.ensureStartOfLine() ;
            right.outputNested(out, detailed) ;
        }
        else
        {
            out.ensureStartOfLine() ;
            out.println("()") ;
        }
        out.decIndent() ;
        out.print(")") ;
    }
    
    @Override
    public String toString() { return PrintUtils.toString(this) ; }
    
    // ---- Check
    
    final void checkDeep(TTree<T> ttree)
    {
        if ( ! Checking )
            return ;
        check(ttree) ;
        if ( left != null )
            left.checkDeep(ttree);
        if ( right != null )
            right.checkDeep(ttree);
    }
    
    final void check(TTree<T> ttree)
    {
        if ( ! Checking )
            return ;
        if ( nodeSize > elements.length )
            error("Node size %d, Array size: %d : %s",  nodeSize, elements.length, this) ;
        
        // -- Structure checks
        if ( parent != null )
        {
            if ( parent.left == this )
            {
                if ( parent.left.id != this.id )
                    error("Parent.left does not point to this node by id") ;
            }
            else if ( parent.right == this )
            {
                if ( parent.right.id != this.id )
                    error("Parent.right does not point to this node by id") ;
            }
            else
                error("Parent does not point to this node") ;
        }

        if ( isInternal() || isHalfLeaf() )
        {
            if ( ttree != null )
            {
                // Internal nodes are always full
                // Half-leaves are always full (by modified half-leaf rule on deletion)  
                if ( nodeSize < ttree.NodeSizeMin )
                    error("Internal node too small") ;
            }
        }
        else if ( isLeftHalfLeaf() )
        {
            if ( ! right.isLeaf() )
                error("LeftHalfLeaf has no leaf to the right") ;
        }
        else if ( isRightHalfLeaf() )
        {
            if ( ! left.isLeaf() )
                error("RightHalfLeaf has no leaf to the left") ;
        }
        else if ( isLeaf())
        {
            if ( parent != null && nodeSize <= 0 )
                error("Zero length node") ;
        }
        else
            error("Node has no leaf status") ;
       
        // Children checks
        if ( left != null && left.parent != this ) 
            error("Left child does not point back to this node") ;
        
        if ( left != null && left.parent.id != this.id ) 
            error("Left child does not point back to this node by id") ;
  
        if ( right != null && right.parent != this ) 
            error("Right child does not point back to this node") ;
  
        if ( right != null && right.parent.id != this.id ) 
            error("Right child does not point back to this node by id") ;

        // -- Ordering checks
        // Order within this node
        T prev = null ;
        for ( int i = 0 ; i < nodeSize ; i++ )
        {
            if ( elements[i] == null )
                error("Null array entry idx=%d : %s", i, this) ;
            if ( prev != null )
            {
                if ( prev.compareTo(elements[i]) >= 0 )
                    error("Unordered: idx=%d : %s %s : %s", i, prev, elements[i], this) ;
            }
            prev =  elements[i] ;
        }
        // Check upper area is cleared.
        if ( TTree.NullOut )
        {
            for ( int i = nodeSize ; i < elements.length ; i++ )
            {
                if ( elements[i] != null )
                    error("Not null array entry idx=%d : %s", i, this) ;
            }
        }
        
        if ( nodeSize > 0 )
        {
            // Check ordering from left and right. 
            if ( left != null && left.nodeSize>0 && left.getMax().compareTo(getMin()) > 0 )    // If this less than left.
                error("Out of order (left): [id=%s] %s/%s", label(left), left.getMax(), getMin()) ;
            
            if ( left != null && left.nodeSize>0 && left.getMax().compareTo(getMin()) == 0 )    // Duplicate.
                error("Duplicate (left): [id=%s] %s/%s", label(left), left.getMax(), getMin()) ;
          
            if ( right != null && right.nodeSize>0 && right.getMin().compareTo(getMax()) < 0 )   // If this more than right
                error("Out of order (right): [id=%s] %s/%s", label(right), right.getMin(), getMax()) ;
    
            if ( right != null && right.nodeSize>0 && right.getMin().compareTo(getMax()) == 0 )    // Duplicate.
                error("Duplicate (right): [id=%s] %s/%s", label(right), right.getMin(), getMin()) ;
        }

        // -- Balance checks
        int x = TTree.balance(this) ;
        if ( x < -1 || x > 1 )
            error("Out of balance %d: %s", x, this) ;

        // -- Height checks

        if ( left != null && height < left.height )
            error("Height error (left) [%d,%d]", height, left.height) ;
            
        if ( right != null && height < right.height )
                error("Height error (right) [%d,%d]", height, right.height) ;
        
        if ( left == null  && right != null )
        {
            if ( height != right.height+1 )
                error("Bad height (right) - not %d", right.height+1) ;
        }
        else if ( left != null  && right == null )
        {
            if ( height != left.height+1 )
                error("Bad height (left) - not %d", left.height+1) ;

        }
        else if ( left != null  && right != null )
        {
            if ( height < left.height || height < right.height )
            {}
            
            if ( height != left.height+1 && height != right.height+1 )
                error("Bad height (%d) - not %d or %d", id, left.height+1, right.height+1) ;
        }
        else
        {
            if ( height != TTree.InitialHeight )
                error("Leaf node height not %d", TTree.InitialHeight) ;
        }
    }
}
