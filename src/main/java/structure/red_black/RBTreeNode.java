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

package structure.red_black;

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;
import org.apache.jena.atlas.io.Printable ;

import static structure.red_black.RBTreeNode.Colour.* ;


/** Reb-Black tree nodes */  

class RBTreeNode<R extends Comparable<R>> implements Printable
{
    enum Colour { RED, BLACK }
    private RBTreeNode<R> left ;
    private RBTreeNode<R> right ;
    private R record ;
    private Colour colour ; 
    
    public RBTreeNode(R record)
    {
        this.record = record ;
        this.left = null ;
        this.right = null ;
        this.colour = RED ;
    }
    
    public RBTreeNode(R record, RBTreeNode<R> left, RBTreeNode<R> right)
    {
        this(record) ;
        this.left = left ;
        this.right = right ;
    }
    
    private static <X extends Comparable<X>> boolean isRed(RBTreeNode<X> node) {
        if ( node == null )
            return false ;
        return node.colour == RED ;
        
    }
    
    public R insert(R newRecord) {
        
        if (isRed(this.left) && isRed(this.right))
            colourRebalance();
        
        int x = this.record.compareTo(newRecord) ;
        
        if ( x == 0 ) {
            // Replace -- the case of partial R as key
            this.record = newRecord ;
            return this.record ;
        }

        
        
        if ( x > 0 ) {
            if ( left == null ) {
                left = new RBTreeNode<R>(newRecord) ;
                return null ;
            }
            return left.insert(newRecord) ;
        }


        else if ( x < 0 ) {
            if ( right == null ) {
                right = new RBTreeNode<R>(newRecord) ;
                return null ;
            }
            return right.insert(newRecord) ;
        }
        
        // ??
        return null ;
        
    }
    
    private void colourRebalance() {}

    public Iterator<R> iterator(R min, R max)
    { 
        return null ;
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
}
