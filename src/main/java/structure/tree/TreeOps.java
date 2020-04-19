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


/** Operations on trees */
public class TreeOps {
    
    public static final boolean CHECKING = true ; 

    /**
     * Binary tree search.
     * @param node  Starting node.
     * @param key   Key
     * @return V or null.
     */
    final public
    static <K extends Comparable<K>,V> V search(TreeNode<K, V> node, K key) {
        while ( node != null ) {
            int x = node.key().compareTo(node.key()) ;
            if ( x == 0 ) return node.value() ;
            if ( x < 0 ) node = node.left() ;
            if ( x > 0 ) node = node.right() ;
        }
        return null ;
    }
    
    /**
     * Binary tree search.
     * @param node  Starting node.
     * @param key   Key
     * @return boolean
     */
    final public
    static <K extends Comparable<K>,V> boolean contains(TreeNode<K, V> node, K key) {
        return search(node, key) != null ;
    }
    
    /** Count the nodes in the (sub)tree */
    final public
    static <K extends Comparable<K>,V> long count(TreeNode<K, V> node) {
        if ( node == null ) return 0 ;
        return count(node.left()) + 1 + count(node.right()) ;  
    }
    
    /** Get the rightmost tree node */ 
    final public
    static <K extends Comparable<K>,V> TreeNode<K, V> getRightDeep(TreeNode<K, V> node) {
        if ( node == null ) return null ; 
        TreeNode<K, V> node2 = node.right() ;
        while( node2 != null )
        {
            node = node2 ;
            node2 = node2.right() ;
        }
        return node ;
    }
    
    /** Get the leftmost tree node */ 
    final public
    static <K extends Comparable<K>,V> TreeNode<K, V> getLeftDeep(TreeNode<K, V> node) {
        if ( node == null ) return null ; 
        TreeNode<K, V> node2 = node.left() ;
        while( node2 != null )
        {
            node = node2 ;
            node2 = node2.left() ;
        }
        return node ;
    }

    // Naming: rotateRight means move the left child up to the root and the root to the right
    // The left is the pivot 
    // == shift right
    // == clockwise
    // This is the wikipedia naming but that does not extend to the double rotations. 
    // Different books have different namings, based on the location of the pivot (which would be a left rotate)
    // But when we talk about double rotations, the pivotLeft terminolgy works better.
    // pivotLeft (= case left left) , pivotLeftRight, 
    
    
    // (K1 (K2 A B) C) ==> (K2 A (K1 B C))
    final public
    static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> node) { return pivotLeft(node) ; }

    // (here) version 1 - leaves the input node as the top node. 
    // Version 2 - leave (K,V) in place, changes node returned. 
    // Can reduce the pointless assignments.
    
    final public 
    static <K,V> TreeNode<K,V> pivotLeft(TreeNode<K,V> node)
    {
        
        if ( CHECKING ) {
            checkNotNull(node) ;
            checkNotNull(node.left()) ;
        }
        
        K k1 = node.key() ;
        K k2 = node.left().key() ;
        
        V v1 = node.value() ;
        V v2 = node.left().value() ;
        
        TreeNode<K,V> a = node.left().left() ;
        TreeNode<K,V> b = node.left().right() ;
        TreeNode<K,V> c = node.right() ;
        
        TreeNode<K,V> t = node.left() ; t.set(k1, v1) ; t.setLeft(b) ; t.setRight(c) ;
        
        node.set(k2, v2) ;
        node.setLeft(a) ;
        node.setRight(t) ;
        return node ;
        
    }

    // (K1 A (K2 B C)) ==> (K2 (K1 A B) C)  
    final public
    static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> node) { return pivotRight(node) ; }

    final public
    static <K,V> TreeNode<K,V> pivotRight(TreeNode<K,V> node)
    {
        if ( CHECKING ) {
            checkNotNull(node) ;
            checkNotNull(node.right()) ;
        }
        K k1 = node.key() ;
        K k2 = node.right().key() ;
        V v1 = node.value() ;
        V v2 = node.right().value() ;
        
        TreeNode<K,V> a = node.left() ;
        TreeNode<K,V> b = node.right().left() ;
        TreeNode<K,V> c = node.right().right() ;
        
        //TreeNode t = new TreeNode(k1, a, b) ;
        TreeNode<K,V> t = node.right() ; t.set(k1,v1) ; t.setLeft(a) ; t.setRight(b) ;  
        
        node.set(k2, v2) ;
        node.setLeft(t) ;
        node.setRight(c) ;
        return node ;
    }

    // Needs testing
    
    // LeftRight
    // (K3 (K1 A (K2 B C)) D) ==> (K2 (K1 A B) (K3 C D))
    //final public void rotateDoubleRight() { pivotLeftRight() ; }
    final public
    static <K,V> TreeNode<K,V> pivotLeftRight(TreeNode<K,V> node)
    {
        if ( CHECKING ) {
            checkNotNull(node) ;
            checkNotNull(node.left()) ;
            checkNotNull(node.left().right()) ;
        }

        K k3 = node.key() ;
        K k1 = node.left().key() ;
        K k2 = node.left().right().key() ;
        V v3 = node.value() ;
        V v1 = node.left().value() ;
        V v2 = node.left().right().value() ;


        TreeNode<K,V> a = node.left().left() ;
        TreeNode<K,V> b = node.left().right().left() ;
        TreeNode<K,V> c = node.left().right().right() ;
        TreeNode<K,V> d = node.right() ;

        node.set(k2, v2) ;

        node.setLeft(node.left().right()) ;
        /*node.left().set(k1,v1) ; Already */ node.left().setLeft(a) ; node.left().setRight(b) ; 
        node.setRight(node.left()) ;
        /*node.right().set(k3,v3) ;*/ node.right().setLeft(c) ; node.right().setRight(d) ;
        return node ;
    }

    // RightLeft
    // (K1 A (K3 (K2 B C) D)) ==> (K2 (K1 A B) (K3 C D))
    //final public void rotateDoubleLeft() { pivotRightLeft() ; }
    final public
    static <K,V> TreeNode<K,V> pivotRightLeft(TreeNode<K,V> node)
    {
        if ( CHECKING ) {
            checkNotNull(node) ;
            checkNotNull(node.right()) ;
            checkNotNull(node.right().left()) ;
        }

        K k1 = node.key() ;
        K k3 = node.right().key() ;
        K k2 = node.right().left().key() ;
        V v1 = node.value() ;
        V v3 = node.right().value() ;
        V v2 = node.right().left().value() ;

        TreeNode<K,V> a = node.left() ;
        TreeNode<K,V> b = node.right().left().left() ;
        TreeNode<K,V> c = node.right().left().right() ;
        TreeNode<K,V> d = node.right().right() ;

        node.set(k2, v2) ;

        node.setLeft(node.left().right()) ; 
        /*node.left().set(k1,v1) ; */ node.left().setLeft(a) ; node.left().setRight(b) ; 
        node.setRight(node.left()) ; 
        /*node.right().set(k3,v3) ; */ node.right().setLeft(c) ; node.right().setRight(d) ;
        return node ;
    }

    final private static void checkNotNull(Object object)
    {
        if ( object == null )
            throw new TreeException("Null") ;
    }

}

