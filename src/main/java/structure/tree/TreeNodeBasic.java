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

final
public class TreeNodeBasic<K extends Comparable<K>,V> implements TreeNode<K,V>{
    private TreeNode<K,V> right ;
    private TreeNode<K,V> left ;
    private K key ;
    private V value ;
    
    public TreeNodeBasic(K key, V value) {
        this.left = null ;
        this.right = null ;
        this.key = key ;
        this.value = value ;
    }
   
    @Override
    public TreeNode<K,V> left()                 { return left ; } 
    @Override
    public TreeNode<K,V> right()                { return right ; }
    @Override
    public K key()                              { return key ; }
    @Override
    public V value()                            { return value ; }              
    @Override
    public void setLeft(TreeNode<K,V> left)     { this.left = left ; } 
    @Override
    public void setRight(TreeNode<K,V> right)   { this.right = right ; }
    
    @Override
    public void set(K key, V value)             { setKey(key) ; setValue(value) ; }
    @Override
    public void setKeyValue(TreeNode<K,V> other)             { setKey(other.key()) ; setValue(other.value()) ; }
    @Override
    public void setKey(K key)                   { this.key = key ; }
    @Override
    public void setValue(V value)               { this.value = value ; }
    
    @Override
    public String toString() {
        return String.format("(key=%s, value=%s)", key, value) ;
    }
}

