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

import structure.Entry ;

public interface TreeNode<K,V> extends Entry<K,V> {
    public TreeNode<K,V> left() ;
    public TreeNode<K,V> right() ;
    @Override
    public K key() ;
    @Override
    public V value() ;
    
    public void setLeft(TreeNode<K,V> left) ;
    public void setRight(TreeNode<K,V> right) ;
    
    public void set(K key, V value) ;
    public void setKeyValue(TreeNode<K,V> other) ;
    public void setKey(K key) ;
    public void setValue(V value) ;

}

