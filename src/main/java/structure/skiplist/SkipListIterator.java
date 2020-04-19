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

package structure.skiplist;

import java.util.Iterator;
import java.util.NoSuchElementException;

final
public class SkipListIterator <R extends Comparable<? super R>> implements Iterator<R>
{
    boolean finished = false ;
    SkipListNode<R> node ;
    R limit ;                   // Exclusive

    SkipListIterator(SkipListNode<R> node)
    { 
        this(node, null) ;
    }

    SkipListIterator(SkipListNode<R> node, R limit)
    { 
        this.node = node ;
        this.limit = limit ;
    }

    @Override
    public boolean hasNext()
    {
        if ( finished ) return false ;
        if ( node != null )
        {
            if ( limit != null && SkipList.cmpRR(node.record, limit) >= 0 )
            {
                finished = true ;
                return false ;
            }
        }

        return node != null ;
    }

    @Override
    public R next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("SkipListIterator") ;

        // Move on - test for limit is doen in hasNext
        R rec = node.record ;
        node = node.get(0) ;
        return rec ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("SkipListIterator.remove") ; }

}
