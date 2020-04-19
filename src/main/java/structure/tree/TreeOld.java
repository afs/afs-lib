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

package structure.tree;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;
import org.apache.jena.atlas.io.Printable ;


/** Simple binary tree nodes, including operations that apply to all trees */  

public class TreeOld<R extends Comparable<R>> implements Printable
{
    private TreeOld<R> left ;
    private TreeOld<R> right ;
    private R record ;
    
    public TreeOld(R record)
    {
        this.record = record ;
    }
    
    public TreeOld(R record, TreeOld<R> left, TreeOld<R> right)
    {
        this(record) ;
        this.left = left ;
        this.right = right ;
    }
    
    public R insert(R newRecord)
    { return insert(newRecord, true) ; }
    
    // Unbalanced insert - return the record if the record already present, else null if new.
    public R insert(R newRecord, boolean duplicates)
    {
        int x = this.record.compareTo(newRecord) ;
        
        if ( x > 0 )
        {
            if ( left == null )
            {
                left = new TreeOld<R>(newRecord) ;
                return null ;
            }
            return left.insert(newRecord, duplicates) ;
        }
        
        if ( x == 0 && ! duplicates )
            return this.record ;

        if ( right == null )
        {
            right = new TreeOld<R>(newRecord) ;
            return null ;
        }
        return right.insert(newRecord, duplicates) ;
    }
    
    // Naming: rotateRight means move the left child up to the root and the root to the right
    // The left is the pivot 
    // == shift right
    // == clockwise
    // This is the wikipedia naming but that does not extend to the double rotations. 
    // Different books have different namings, based on the location of the pivot (which would be a left rotate)
    // But when we talk about double rotations, the pivotLeft terminolgy works better.
    // pivotLeft (= case left left) , pivotLeftRight, 
    
    
    // (K1 (K2 A B) C) ==> (K2 A (K1 B C))
    final public void rotateRight() { pivotLeft() ; }
    final public void pivotLeft()
    {
        // Validity checking?
        checkNotNull(left) ;
        
        R k1 = record ;
        R k2 = left.record ;
        TreeOld<R> a = left.left ;
        TreeOld<R> b = left.right ;
        TreeOld<R> c = right ;
        
        // Or reuse left.
        // TreeNode t = new TreeNode(k1, b, c) ;
        TreeOld<R> t = left ; t.record = k1 ; t.left = b ; t.right = c ;
        
        this.record = k2 ;
        this.left = a ;
        this.right = t ;
    }
    
    // (K1 A (K2 B C)) ==> (K2 (K1 A B) C)  
    final public void rotateLeft() { pivotRight() ; }
    final public void pivotRight()
    {
        checkNotNull(right) ;
        R k1 = record ;
        R k2 = right.record ;
        TreeOld<R> a = left ;
        TreeOld<R> b = right.left ;
        TreeOld<R> c = right.right ;
        
        //TreeNode t = new TreeNode(k1, a, b) ;
        TreeOld<R> t = right ; right.record = k1 ; right.left = a ; right.right = b ;  
        
        this.record = k2 ;
        this.left = t ;
        this.right = c ;
    }
    
    // LeftRight
    // (K3 (K1 A (K2 B C)) D) ==> (K2 (K1 A B) (K3 C D))
    //final public void rotateDoubleRight() { pivotLeftRight() ; }
    final public void pivotLeftRight()
    {
        checkNotNull(left) ;
        checkNotNull(left.right) ;
        R k3 = record ;
        R k1 = left.record ;
        R k2 = left.right.record ;
        TreeOld<R> a = left.left ;
        TreeOld<R> b = left.right.left ;
        TreeOld<R> c = left.right.right ;
        TreeOld<R> d = right ;
        
        this.record = k2 ;
//        this.left = new TreeNode<R>(k1, a, b) ;     // Reuse LeftRight
//        this.right = new TreeNode<R>(k3, c, d) ;    // reuse Left
        
        // XXX WRONG?
        this.left = left.right ;
        /*this.left.record = k1 ;*/ this.left.left = a ; this.left.right = b ; 
        this.right = left ;
        /*this.right.record = k3 ;*/ this.right.left = c ; this.right.right = d ;
        
    }
    
    // RightLeft
    // (K1 A (K3 (K2 B C) D)) ==> (K2 (K1 A B) (K3 C D))
    //final public void rotateDoubleLeft() { pivotRightLeft() ; }
    final public void pivotRightLeft()
    {
        checkNotNull(right) ;
        checkNotNull(right.left) ;
        
        R k1 = record ;
        R k3 = right.record ;
        R k2 = right.left.record ;
        TreeOld<R> a = left ;
        TreeOld<R> b = right.left.left ;
        TreeOld<R> c = right.left.right ;
        TreeOld<R> d = right.right ;
        
        this.record = k2 ;
//        this.left = new TreeNode<R>(k1, a, b) ;     // replace by in-place / RightLeft
//        this.right = new TreeNode<R>(k3, c, d) ;    // Right
        
     // XXX WRONG?
        this.left = left.right ; 
        /*this.left.record = k1 ; */this.left.left = a ; this.left.right = b ; 
        this.right = left ; 
        /*this.right.record = k3 ;*/ this.right.left = c ; this.right.right = d ;
        
    }
    
    // Not in this class
    public Iterator<R> records(R min, R max)
    { 
        return null ;
    }
    
    //public Iterator<R> records()
    
    public List<R> records()
    {
        List<R> x = new ArrayList<R>() ;
        records(x) ;
        return x ; // .iterator() ;
    }
    
    public void records(List<R> x)
    {
        if ( left != null )
            left.records(x) ;
        x.add(record) ;
        if ( right != null )
            right.records(x) ;

    }

    @Override
    public String toString() { return PrintUtils.toString(this) ; }
    
    public void outputNested(IndentedWriter out)
    {
        if ( left == null && right == null )
        {
            out.print(record.toString()) ;
            return ;
        }
        
        // At least one of left and right.
        
        out.print('(') ;
        out.print(record.toString()) ;
        out.print(' ') ;
        out.incIndent() ;
        out.println() ;
        if ( left != null )
            left.outputNested(out) ;
        else
            out.print("undef") ;
        out.println();

        if ( right != null )
            right.outputNested(out) ;
        else
            out.print("undef") ;
        out.print(')') ;
        out.decIndent() ;
    }
    
    @Override
    public void output(IndentedWriter out)
    {
        if ( left == null && right == null )
        {
            out.print(record.toString()) ;
            return ;
        }
        
        out.print('(') ;
        out.print(record.toString()) ;
        if ( left != null )
        {
            out.print(' ') ;
            left.output(out) ;
        }
        if ( right != null )
        {
            out.print(' ') ;
            right.output(out) ;
        }
        out.print(')') ;
    }

    // Inline me :-)
    final private static void checkNotNull(Object object)
    {
        if ( object == null )
            throw new TreeException("Null") ;
    }
}
