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

import java.util.Iterator ;
import java.util.List ;

/**  
 * A map from key to value with the addition feature of a iterator over a range.
 * A simplifed form of SortedMap.
 */
public interface OrderedMap<K extends Comparable<K>, V> extends Iterable<Entry<K,V>>
{
    /** Clear all elements */ 
    public void clear() ;

    public boolean contains(K key);

    public boolean isEmpty();
    
    public V search(K key);
    
    public void insert(K Key, V value);
    
    public boolean remove(K key);
    
//    /** Remove the key if and only if the value matches as well */
//    public boolean remove(K key, V value);
    
    public K min() ;
    public K max() ;

    /** Number of keys in the map */
    public long size() ;
    
    /** Size by actually walking the tree and counting - mainly for testing */
    public long count() ;
    public void check() ;
    
    public Iterator<K> iteratorKeys() ;
    public Iterator<K> iteratorKeys(K startInc, K endExc) ;
    
    public Iterator<V> iteratorValues() ;
    public Iterator<V> iteratorValues(K startInc, K endExc) ;
    
    public Iterator<Entry<K, V>>  iteratorEntries() ;
    public Iterator<Entry<K, V>>  iteratorEntries(K startInc, K endExc) ;

    public List<K> keys() ;
}
