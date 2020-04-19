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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class AvlIterator<R extends Comparable<? super R>> implements Iterator<R>
{
    public static <R extends Comparable<? super R>> Iterator<R> iterator(AVL<R> avl, R min, R max)
    {
        return new AvlIterator<R>(avl, min, max) ;
    }

    boolean finished = false ;
    R record ;              // Yield this before moving on
    AvlNode<R> node ;
    R max ;

    AvlIterator(AVL<R> avl, R min, R max)
    {
        if ( min != null && max != null )
        {
            int x = min.compareTo(max) ;
            if ( x >= 0 )
            {
                finished = true ;
                return ;
            }
        }
        
        node = avl.findNodeAbove(min) ;
        if ( node == null )
        {
            record = null ;
            finished = true ;
            return ; 
        }

        record = node.record ;
        this.max = max ;
    }

    @Override
    public boolean hasNext()
    {
        if ( finished )
            return false ;

        if ( record != null )
            return true ;

        // Record null, move to next node. 
        if ( node == null )
        {
            System.err.println("Happened!") ;
            // Does not happen?
            finished = true ;
            return false ;
        }

        // Have yielded the value for "node" (and hence all left subtree) 
        // Move the left-est node of the right subtree.
        // If no right subtree
        //   Go up to parent.
        //   Check we were the left subtree of parent, not right.
        // If no parent (this is the root), and we were left of parent,
        //   go down 
        
        AvlNode<R> nextNode = node.right ;
        if ( nextNode != null )
        {
            // There is a right tree to do.
            // Find min (left chasing)
            while ( nextNode.left != null )
                nextNode = nextNode.left ; 
        }
        else   
        {
            // No right subtree from here.
            // Walk up tree until we were not the right node of our parent.
            AvlNode<R> n2 = node ;
            AvlNode<R> n3 = n2.parent ;
            while( n3 != null )
            {
                if ( n3.right != n2 )
                {
                    n2 = n3 ;
                    break ;
                }
           
                n2 = n3 ;
                n3 = n2.parent ;
            }
            
            if ( n3 == null )
            {
                finished = true ;
                return false ;
            }
            
            // Now at the first node upwards when we're the left
            // (i.e. less than the value)
            
            nextNode = n2 ;
        }
        
        node = nextNode ;
        return testAndSetRecord(nextNode) ;
    }

    private boolean testAndSetRecord(AvlNode<R> node)
    {
        if ( node == null )
        {
            record = null ;
            finished = true ;
            return false ;
        }

        if ( max != null )
        {
            int x = node.record.compareTo(max) ;
            if ( x >= 0 )
            {
                // End
                finished = true ;
                return false ;
            }
        }
        record = node.record ;
        return true ;
    }

    @Override
    public R next()
    {
        if ( ! hasNext())
            throw new NoSuchElementException("AvlIterator") ;
        R r = record ;
        record = null ;  
        return r ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("AvlIterator") ; }

    // Calculate the iterator.  For testing. 
    static <R extends Comparable<? super R>> List<R> calcIter(AvlNode<R> node, R min, R max)
    {
        List<R> x = new ArrayList<R>() ;
        if ( node == null )
            return x ;
        node.records(x) ; // Sorted.
        if ( min != null )
            while ( x.size() > 0 && x.get(0).compareTo(min) < 0 )
                x.remove(0) ;
        
        if ( max != null )
            while ( x.size() > 0 && x.get(x.size()-1).compareTo(max) >= 0 )
                x.remove(x.size()-1) ; 
        return x ;
    }

}
