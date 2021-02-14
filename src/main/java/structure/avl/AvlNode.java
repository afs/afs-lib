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

import static java.lang.String.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.Printable;
import structure.tree.TreeException ;

final class AvlNode<T extends Comparable<? super T>> implements Printable
{
    private static int counter = 0 ;
    
    /* Only used for debugging.  Remove for production (or, make static and ignore)
     * But removing does not necesarily save space - Java seem to allocate in
     * fixed size, quantized units and this id does not cause it to spill
     * such a unit.   
     */
    private int id ;                    
    
    int height = AVL.InitialHeight ;
    AvlNode<T> parent ;
    AvlNode<T> left ;
    AvlNode<T> right ;
    T record ;

    AvlNode(T record,  AvlNode<T> parent)   { this(record, parent, null, null) ; }
    
    AvlNode(T record, AvlNode<T> parent, AvlNode<T> left, AvlNode<T> right)
    {
        set(record, parent, left, right) ;
        id = (++counter) ;
    }
    
    void set(T record, AvlNode<T> parent, AvlNode<T> left, AvlNode<T> right)
    {  
        this.record = record ;
        this.parent = parent ;
        this.left = left ;
        this.right = right ;
        this.parent = parent ;
    }
    
    public List<T> records()
    {
        List<T> x = new ArrayList<T>() ;
        records(x) ;
        return x ; // .iterator() ;
    }

    public void records(List<T> x)
    {
        if ( left != null )
            left.records(x) ;
        x.add(record) ;
        if ( right != null )
            right.records(x) ;

    }
    
    public long count()
    {
        long x = 1 ;
        if ( left != null )
            x = x + left.count() ;
        if ( right != null )
            x = x + right.count() ;
        return x ;
    }
    
    boolean isLeaf()    { return left == null && right == null ; }
    
    void elements(List<T> acc)
    {
        if ( left != null )
            left.elements(acc) ;
        acc.add(record) ;
        if ( right != null )
            right.elements(acc) ;
    }

    @Override
    public String toString()
    {
        //return record.toString() ;
        return toString2() ;
    }
    

    public String toString2()
    { 
        return format("[%s] rec=%s parent=%s [h=%d] left=%s, right=%s", label(this), record, label(parent), height, label(left), label(right)) ;
    }

    private static final String undef = "_" ;
    static <R extends Comparable<? super R>> String label(AvlNode<R> n)
    {
        if ( n == null )
            return undef ;
        return Integer.toString(n.id) ;
    }
    
    public void debug()
    { 
        debug(System.out) ;
    }
    
    public void debug(PrintStream ps)
    {
        IndentedWriter out = new IndentedWriter(ps) ;
        this.outputNested(out, true) ;
        out.println();
        out.flush();
        ps.flush();
        
    }
    
    /** print structured */
    public void outputNested(IndentedWriter out, boolean detailed)
    {
        out.print('(') ;
        if ( detailed )
            out.print(toString2()) ;
        else
            out.print(this.toString()) ;
        
        // Inline small things.
        // Improve to "if left and right leafves and not detailed"
        if ( left == null && right == null )
        {
            out.print(" ") ; out.print(undef) ; out.print(" ") ; out.print(undef) ;
            out.print(')') ;
            return ;
        }
        
        
        out.incIndent() ;
        out.println() ;
        if ( left != null )
            left.outputNested(out, detailed) ;
        else
            out.print(undef) ;
        out.println();

        if ( right != null )
            right.outputNested(out, detailed) ;
        else
            out.print(undef) ;
        out.print(')') ;
        out.decIndent() ;
    }

    /** Output the record of a subtree : Looses left/right distinction*/ 
    @Override
    public void output(IndentedWriter out)
    {
//        if ( left == null && right == null )
//        {
//            out.print(record) ;
//            return ;
//        }
//
        out.print('(') ;
        out.print(record.toString()) ;
        out.print(' ') ;
        
        if ( left != null )
            left.output(out) ;
        else 
            out.print('_') ;
        
        out.print(' ') ;
        if ( right != null )
            right.output(out) ;
        else
            out.print('_') ;
        out.print(')') ;
    }

    final void check()
    {
        if ( ! AVL.Checking )
            return ;

        if ( record == null ) error("Null record") ;
        
        // -- Ordering checks
        if ( left != null && record.compareTo(left.record) < 0 )    // If this less than left.
            error("Out of order (left): [id=%s] %s/%s", label(left), record, left.record) ;
        
        if ( right != null && record.compareTo(right.record) > 0 )   // If this more than right
            error("Out of order (right): [id=%s] %s/%s", label(right), record, left.record) ;
        
        // -- Balance checks
        int x = AVL.balance(left, right) ;
        if ( x < -1 || x > 1 )
            error("Out of balance %d %s [h=%d] [left (%s),right (%s)]", x,  record, height, label(left), label(right)) ;
        
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
                error("Bad height - not %d or %d", left.height+1, right.height+1) ;
        }
        else
        {
            if ( height != AVL.InitialHeight )
                error("Leaf node height not %d", AVL.InitialHeight) ;
        }
        
        // Parent.
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
           
        if ( left != null && left.parent != this ) 
            error("Left child does not point back to this node") ;
        
        if ( left != null && left.parent.id != this.id ) 
            error("Left child does not point back to this node by id") ;

        if ( right != null && right.parent != this ) 
            error("Right child does not point back to this node") ;

        if ( right != null && right.parent.id != this.id ) 
            error("Right child does not point back to this node by id") ;
    }
    
    final void checkDeep()
    {
        if ( ! AVL.Checking )
            return ;
        check() ;
        if ( left != null )
            left.check();
        if ( right != null )
            right.check();
    }
        
    private void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.printf("[%s] %s\n", label(this), msg) ;
        System.out.flush();
        this.outputNested(IndentedWriter.stderr, true) ;
        IndentedWriter.stderr.ensureStartOfLine() ;
        IndentedWriter.stderr.flush();
        throw new TreeException(msg) ;
    }


}
