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

package structure;

import org.apache.jena.atlas.io.Printable;

import java.util.Iterator;
import java.util.List;

// May change to allow duplicates -- OrderedBag
public interface OrderedSet<T extends Comparable<? super T>> extends Iterable<T>, Printable
{
    /** Clear all elements */ 
    public void clear() ;
    
    // This could be java.util.SortedSet - except that's quite large.

    public boolean contains(T item);

    public boolean isEmpty();
    
    public T search(T item);
    
    public boolean add(T item);
    
    public boolean remove(T item);
    
    public T max() ;

    public T min() ;
    
    // hashCode
    // equals
    // Comparator<? super T>  comparator() ; 
    // subset
    // headSet
    // tailSet
    
    /** Number of elements in the set */
    public long size() ;
    
    /** Size by actually walking the tree and counting - mainly for testing */
    public long count() ;
    public void checkTree() ;
    public List<T> elements() ;
    
    @Override
    public Iterator<T> iterator() ;
    public Iterator<T> iterator(T startInc, T endExc) ;
}
