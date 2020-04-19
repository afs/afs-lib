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

package structure.binary_search_tree;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import structure.Entry ;
import structure.OrderedMap ;
import structure.Ref;
import structure.tree.TreeNode ;
import structure.tree.TreeNodeBasic ;
import structure.tree.TreeOps ;

/** Basic binary search tree (BST) - no balancing */
public class BST_Tree<K extends Comparable<K>,V> implements OrderedMap<K, V>{

    private TreeNode<K,V> root ;
    
    public BST_Tree() {
        root = null ;
    }
    
    protected TreeNode<K,V> create(K key, V value) { return new TreeNodeBasic<>(key, value) ; }

    @Override
    public void insert(K key, V value) {
        root = insert(root, key, value) ;
    }
    
    protected TreeNode<K,V> insert(TreeNode<K, V> node, K key, V value) {
        if ( node == null ) {
            node = create(key, value) ;
            return node ;
        }
        int x = key.compareTo(node.key());
        if (x == 0) {
            // Replace
            node.setValue(value) ;
            return node ;
        }
        
        if (x < 0) {
            TreeNode<K,V> z = insert(node.left(), key, value);
            node.setLeft(z) ;
        }
        else { // ( x > 0 )
            TreeNode<K,V> z = insert(node.right(), key, value);
            node.setRight(z) ;
        }
        return node ;
    }

    @Override
    public boolean remove(K key) {
        Ref<V> ref = new Ref<>(null) ;
        root = remove(root, key, ref) ;
        return ref.getValue() != null  ;
    }
    
    //XXX return the Value removed?
    // Returns the replacement node.  Pass a ref down?!
    // Another trailing walk?
    protected TreeNode<K,V> remove(TreeNode<K,V> node, K key, Ref<V> valueSlot) {
        if ( node == null )
            return node ;
        int x = key.compareTo(node.key());
        if (x == 0) {
            if ( valueSlot != null )
                // Unset.
                valueSlot.setValue(node.value()) ; 
            // Pull up and do a deleteMin/deleteMax
            if ( node.left() == null && node.right() == null ) {
                // Node freed.
                return null ;
            }

            if ( node.left() == null ) {
                // Node freed.
                return node.right() ;
            }

            if ( node.right() == null ) {
                // Node freed.
                return node.left() ;
            }

            // We are a 2-node. Choice point.
            TreeNode<K,V> replacement = TreeOps.getLeftDeep(node.right()) ;
            K k = replacement.key() ;
            V v = replacement.value() ;
            
            // Set here, then delete "replacement"
            node.set(k,v) ;
            // Stop passing valueSlot down.
            TreeNode<K,V> z = remove(node.right(), k, null) ;
            node.setRight(z) ;
            return node ;
        }
        
        if (x < 0) {
            TreeNode<K,V> z = remove(node.left(), key, valueSlot);
            node.setLeft(z) ;
        }
        else { // ( x > 0 )
            TreeNode<K,V> z = remove(node.right(), key, valueSlot);
            node.setRight(z) ;
        }
        return node ;
    }

    @Override
    public V search(K key) {
        return TreeOps.search(root, key) ;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return entries().iterator() ;
    }

    @Override
    public void clear() { root = null ; }

    @Override
    public boolean contains(K key) {
        return TreeOps.contains(root, key) ;
    }

    @Override
    public boolean isEmpty() {
        return root == null ;
    }

    @Override
    public K min() {
        TreeNode<K,V> n = TreeOps.getLeftDeep(root) ;
        if ( n == null ) return null ;
        return n.key(); 
    }

    @Override
    public K max() { 
        TreeNode<K,V> n = TreeOps.getRightDeep(root) ;
        if ( n == null ) return null ;
        return n.key(); 
    }

    @Override
    public long size() {
        return count() ;
    }

    @Override
    public long count() {
        return TreeOps.count(root) ; 
    }

    @Override
    public void check() {}

    @Override
    public Iterator<K> iteratorKeys() {
        return iteratorKeys(null, null) ;
        
    }

    @Override
    public Iterator<K> iteratorKeys(K startInc, K endExc) {
        List<K> acc = new ArrayList<>() ;
        keys(acc, root, startInc, endExc) ;
        return acc.iterator() ;
    }

    @Override
    public List<K> keys() {
        List<K> acc = new ArrayList<>() ;
        keys(acc, root, null, null) ;
        return acc ;
    }

    private static <K extends Comparable<K>,V> void keys(List<K> acc, TreeNode<K, V> node, K startInc, K endExc) {
        if ( node == null )
            return ;
        K key = node.key();
        // Trim.
//        if ( startInc != null ) {
//            // it's all to the left of here.
//            int x1 = key.compareTo(startInc) ;
//            if ( x1 > 0 )
//                keys(acc, node.left(), startInc, endExc) ;
//            return ;
//        }
//        if ( endExc != null ) {
//            // it's all to the right of here.
//            int x1 = key.compareTo(endExc) ;
//            if ( x1 < 0 )
//                keys(acc, node.right(), startInc, endExc) ;
//            return ;
//        }
        
        keys(acc, node.left(), startInc, endExc) ;
        int x1 = 0 ;
        if ( startInc != null )
            x1 = key.compareTo(startInc) ;
        int x2 = -1 ;
        if ( endExc != null )
            x2 = key.compareTo(endExc) ;
        
        if ( x1 >= 0 && x2 < 0 )
            acc.add(node.key()) ;
        
        keys(acc, node.right(), startInc, endExc) ;
    }

    public List<Entry<K,V>> entries() {
        List<Entry<K,V>> acc = new ArrayList<>() ;
        entries(acc, root) ;
        return acc ;
    }

    private void entries(List<Entry<K, V>> acc, TreeNode<K, V> node) {
        if ( node == null )
            return ;
        entries(acc, node.left()) ;
        acc.add(node) ;
        entries(acc, node.right()) ;
    }


    @Override
    public Iterator<V> iteratorValues() {
        return null ;
    }

    @Override
    public Iterator<V> iteratorValues(K startInc, K endExc) {
        return null ;
    }

    @Override
    public Iterator<Entry<K, V>> iteratorEntries() {
        return entries().iterator() ;
    }

    @Override
    public Iterator<Entry<K, V>> iteratorEntries(K startInc, K endExc) {
        return null ;
    }
    
    @Override
    public String toString() {
        IndentedLineBuffer out = new IndentedLineBuffer() ; 
        printNode(out, root) ;
        return out.asString() ;
    }
    
    public void print() {
        if ( root == null )
            IndentedWriter.stdout.println("Empty") ;
        else
            printNode(IndentedWriter.stdout, root) ;
        IndentedWriter.stdout.flush();
    }
    
    private void printNode(IndentedWriter out, TreeNode<K,V> node) {
        if ( node == null )
            return ;
        //out.incIndent();
        printNode(out, node.left()) ;
        out.printf("(%s %s)", node.key(), node.value()) ;
        printNode(out, node.right()) ;
        //out.decIndent();
    }

    public void printValues() {
        if ( root == null )
            IndentedWriter.stdout.print("Empty") ;
        else
            printValues(IndentedWriter.stdout, root, false) ;
        
        IndentedWriter.stdout.println() ;
        IndentedWriter.stdout.flush();
    }
    
    private boolean printValues(IndentedWriter out, TreeNode<K,V> node, boolean printed) {
        if ( node == null )
            return printed ;
        printed = printValues(out, node.left(), printed) ;
        if ( printed )
            out.print(".") ;
        out.printf("(%s %s)", node.key(), node.value()) ;
        printValues(out, node.right(), true) ;
        return true ;
    }
}

