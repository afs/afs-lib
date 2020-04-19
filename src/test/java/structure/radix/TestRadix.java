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

package structure.radix;

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.List ;

import static org.junit.Assert.*;
import static structure.radix.Str.str ;
import org.junit.Test ;
import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.iterator.Iter ;

public class TestRadix
{
    // Test order : This sequence of keys triggers every case of insert.

    static byte[] key1 = { 2 , 4 , 6 , 8  } ;

    static byte[] key2 = { 2 , 4 , 6 , 10  } ;
    // Insert - shorter key
    static byte[] key3 = { 2 , 4 } ;

    // Insert - existing leaf
    static byte[] key4 = { 2 , 4 , 6,  8 , 10 } ;

    // Insert - partial prefix match.
    static byte[] key5 = { 2 , 4 , 3 , 1 } ;

    // Insert - new root
    static byte[] key6 = { 0 , 1 , 2 , 3 , 4  } ;

    // TODO contains tests
    // TODO Min tests, max tests, iterator tests.
    // TODO isEmpty

    @Test public void radix_01()
    {
        RadixTree t = tree() ;
        test(t) ;
        count(t, 0) ;
        assertTrue(t.isEmpty()) ;
    }

    @Test public void radix_02()
    {
        byte[] k = {1,2,3,4} ;
        test(k) ;
    }

    @Test public void radix_03()
    {
        byte[] k1 = {1,2,3,4} ;
        byte[] k2 = {0,1,2,3} ;
        test(k1, k2) ;
    }

    @Test public void radix_04()
    {
        // Reverse order
        byte[] k1 = {0,1,2,3} ;
        byte[] k2 = {1,2,3,4} ;
        test(k1, k2) ;
    }

    @Test
    public void radix_05()
    {
        byte[] k1 = { 1 , 2 , 3 } ;
        byte[] k2 = { 1 , 2 } ;
        byte[] k3 = { 1 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_06()
    {
        byte[] k1 = { 1 , 2 , 3 } ;
        byte[] k2 = { 1 , 2 , 5 } ;
        byte[] k3 = { 1 , 2 , 6 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_07()
    {
        byte[] k1 = { 1 , 3 , 4 } ;
        byte[] k2 = { 1 , 3 , 5 } ;
        byte[] k3 = { 1 , 2 , 6 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_08()
    {
        byte[] k1 = { 3 , 4 } ;
        byte[] k2 = { 3 , 5 } ;
        byte[] k3 = { 2 , 6 } ;
        testPermute(k1, k2, k3) ;
    }

    @Test
    public void radix_09()
    {
        byte[] k1 = { } ;
        byte[] k2 = { 1 } ;
        byte[] k3 = { 2, 3 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_10()
    {
        byte[] k1 = { 2 , 4 , 6 , 8  } ;
        byte[] k2 = { 2 , 4 , 6 , 10  } ;
        byte[] k3 = { 2 , 4 } ;
        testPermute(k1, k2, k3) ;
    }

    @Test public void radix_11()
    {
        test(key1, key2, key3, key4, key5, key6) ;
    }


    @Test
    public void radix_12()
    {
        byte[] k1 = { 2 , 4 , 6 , 8  } ;
        byte[] k2 = { 2 , 4 , 6 , 10  } ;
        RadixTree t = tree(k1) ;
        assertTrue(t.contains(k1)) ;
        assertFalse(t.contains(k2)) ;
    }

    @Test
    public void radix_13()
    {
        byte[] k1 = { 2 , 4 , 6 , 8  } ;
        byte[] k2 = { 2 , 4 , 6 , 10  } ;
        byte[] k3 = { 2 , 4 , 6 , 9  } ;
        RadixTree t = tree(k1, k2) ;
        assertTrue(t.contains(k1)) ;
        assertTrue(t.contains(k2)) ;
        assertFalse(t.contains(k3)) ;
    }

    @Test
    public void radix_iter_01()
    {
        RadixTree t = tree() ;
        Iterator<RadixEntry> iter = t.iterator() ;
        assertFalse(iter.hasNext()) ;
    }

    @Test
    public void radix_iter_02()
    {
        RadixTree t = tree(key1, key2, key3) ;
        Iterator<RadixEntry> iter = t.iterator() ;
        List<RadixEntry> x = Iter.toList(iter) ;
        assertArrayEquals(key3, x.get(0).key) ;
        assertArrayEquals(key1, x.get(1).key) ;
        assertArrayEquals(key2, x.get(2).key) ;
    }

    static byte[][] order = { key6, key3, key5, key1, key4, key2 } ;

    @Test
    public void radix_iter_03()
    {
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, null, null, order) ;
    }

    @Test
    public void radix_iter_10()
    {
        // Short key over subtree.
        byte[] keyStart = key1 ;
        byte[] keyFinish = null ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key1, key4, key2) ;
    }

    @Test
    public void radix_iter_11()
    {
        // Short key over subtree.
        byte[] keyStart = { 2 , 4 , 6 } ;
        byte[] keyFinish = null ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key1, key4, key2) ;
    }

    @Test
    public void radix_iter_12()
    {
        byte[] keyStart = { 9 } ;
        byte[] keyFinish = null ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish) ;
    }

    @Test
    public void radix_iter_13()
    {
        // Key diverges, below.
        byte[] keyStart = { 2, 4, 6, 1} ;
        byte[] keyFinish = null ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key1, key4, key2) ;
    }

    @Test
    public void radix_iter_14()
    {
        // Key diverges, above
        byte[] keyStart = { 2, 4, 6, 9} ;
        byte[] keyFinish = null ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key2) ;
    }


    @Test
    public void radix_iter_20()
    {
        byte[] keyStart = null ;
        byte[] keyFinish = { 2 , 4 , 6 } ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key6, key3, key5) ;
    }

    @Test
    public void radix_iter_21()
    {
        byte[] keyStart = null ;
        byte[] keyFinish = { 2 , 4 , 5 } ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key6, key3, key5) ;
    }

    // All start keys.
    @Test
    public void radix_iter_22()
    {
        byte[] keyStart = key1 ;
        byte[] keyFinish = { 2 , 4 , 6 , 9 } ;
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        testIter(t, keyStart, keyFinish, key1, key4) ;
    }

    private static void testIter(RadixTree t, byte[] keyStart, byte[] keyFinish, byte[]...results)
    {
        Iterator<RadixEntry> iter = t.iterator(keyStart, keyFinish) ;
        for ( int i = 0 ;  i < results.length ; i++ )
        {
            assertTrue("Iterator ran out", iter.hasNext()) ;
            byte[] k = iter.next().key ;
            assertArrayEquals("At idx="+i+" : Expected: "+str(results[i])+" / Actual: "+str(k),
                              results[i], k) ;
        }

        if ( iter.hasNext() )
        {
            System.out.println("-- Iterator still has elements") ;
            Iterator<RadixEntry> iter2 = t.iterator(keyStart, keyFinish) ;
            for ( ; iter2.hasNext() ; )
                System.out.println(iter2.next()) ;
            System.out.println("----") ;
            for ( byte[] r : results )
                System.out.println(str(r)) ;
            System.out.println("----") ;

        }

        assertFalse("Iterator still has elements", iter.hasNext()) ;
    }

    @Test
    public void radix_minmax_0()
    {
        RadixTree t = tree() ;
        assertNull(t.min()) ;
        assertNull(t.max()) ;
    }


    @Test
    public void radix_minmax_1()
    {
        RadixTree t = tree(key1) ;
        minmaxtest(t, key1, key1) ;
    }

    @Test
    public void radix_minmax_2()
    {
        RadixTree t = tree(key1, key2, key3) ;
        minmaxtest(t, key3, key2) ;
    }


    @Test
    public void radix_minmax_3()
    {
        RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
        minmaxtest(t, key6, key2) ;
    }

    @Test
    public void radix_minmax_9()
    {
        RadixTree t = tree(key1) ;
        t.delete(key1) ;
        assertNull(t.min()) ;
        assertNull(t.max()) ;
    }

    private static void minmaxtest(RadixTree t, byte[] min, byte[] max)
    {
        ByteBuffer bb = t.min() ;
        byte[] b = new byte[bb.limit()] ;
        bb.get(b) ;
        assertArrayEquals(min, b) ;
        bb = t.max() ;
        b = new byte[bb.limit()] ;
        bb.get(b) ;
        assertArrayEquals(max, b) ;

    }

    static RadixTree tree(byte[] ... keys)
    {
        return tree(RadixTreeFactory.create(), keys) ;
    }

    final static boolean print = false ;
    static RadixTree tree(RadixTree t, byte[] ... keys)
    {
        for ( byte[]k : keys )
        {
            if (print) System.out.println("Build: "+Str.str(k)) ;
            t.insert(k, k) ;
            if (print) t.print() ;
            t.check() ;
            if (print) System.out.println() ;
        }
        return t ;
    }

    /** Add the keys, delete the keys. */
    static void test(byte[]... keys)
    {
        test(RadixTreeFactory.create(), keys) ;
    }

    static void test(RadixTree t, byte[]... keys)
    {

        for ( byte[]k : keys )
        {
            byte[] v = valFromKey(k) ;
            insert(t, k, v) ;
        }
        count(t, keys.length) ;
        check(t, keys) ;
        if ( keys.length > 0 )
            assertFalse(t.isEmpty()) ;
        for ( byte[]k : keys )
            delete(t, k) ;
        assertTrue(t.isEmpty()) ;
    }

    public static byte[] valFromKey(byte[] k)
    {
        byte[] v = new byte[k.length] ;
        for ( int j = 0 ; j < v.length ; j++ )
            v[j] = (byte)(k[j]+10) ;
        return v ;
    }

    static void check(RadixTree t, byte[] ... keys)
    {
        for ( byte[]k : keys )
            assertTrue("Not found: Key: "+Str.str(k), t.contains(k)) ;
    }

    private static void insert(RadixTree t, byte[] key, byte[] value)
    {
        t.insert(key, value) ;
        t.check();
        assertTrue(t.contains(key)) ;
        byte v[] = t.find(key,null) ;
        assertNotNull(v) ;
        assertArrayEquals(value, v) ;
    }

    private static void delete(RadixTree trie, byte[] key)
    {
        boolean b2 = ( trie.find(key,null) != null ) ;
        boolean b = trie.delete(key) ;
        try {
            trie.check() ;
        } catch (AtlasException ex)
        {
            //trie.print() ;
            throw ex ;
        }

        assertFalse(trie.contains(key)) ;
        byte v[] = trie.find(key,null) ;
        assertNull(v) ;
    }

    static void count(RadixTree t, int size)
    {
        t.check();
        assertEquals(size, t.size()) ;
    }

    static void testPermute(byte[] k1, byte[] k2, byte[] k3)
    {
        test(k1, k2, k3) ;
        test(k1, k3, k2) ;
        test(k2, k1, k3) ;
        test(k2, k3, k1) ;
        test(k3, k1, k2) ;
        test(k3, k2, k1) ;
    }
}
