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

import structure.Ref;
import structure.binary_search_tree.BST_Tree ;
import structure.tree.TreeNode ;

public class RBTree<K extends Comparable<K>, V> extends BST_Tree<K, V> {
    public RBTree() { }
    
    @Override
    protected TreeNode<K,V> insert(TreeNode<K, V> node, K key, V value) {
        return super.insert(node, key, value) ;
    }
    
    @Override
    protected TreeNode<K,V> remove(TreeNode<K,V> node, K key, Ref<V> valueSlot) {
        return super.remove(node, key, valueSlot) ;
    }
}

