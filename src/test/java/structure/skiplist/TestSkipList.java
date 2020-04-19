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

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static structure.skiplist.SkipListTestBase.create;
import static structure.skiplist.SkipListTestBase.delete;
import static structure.skiplist.SkipListTestBase.testIteration;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class TestSkipList
{
    static {
        SkipList.Checking = true ;
        //SkipList.Logging = false ;
    }

    @Test public void skiplist_01()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.check() ;
    }

    @Test public void skiplist_02()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.insert(3) ;
        assertTrue(sk.contains(3)) ;
        assertEquals(1, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_03()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.insert(3) ;
        sk.insert(3) ;
        assertTrue(sk.contains(3)) ;
        assertEquals(1, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_04()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.insert(3) ;
        sk.insert(4) ;
        assertTrue(sk.contains(3)) ;
        assertTrue(sk.contains(4)) ;
        assertEquals(2, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_05()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.insert(4) ;
        sk.insert(3) ;
        assertTrue(sk.contains(3)) ;
        assertTrue(sk.contains(4)) ;
        assertEquals(2, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_06()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.insert(4) ;
        assertTrue(sk.contains(4)) ;
        sk.delete(4) ;
        assertFalse(sk.contains(4)) ;
        assertEquals(0, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_07()
    {
        SkipList<Integer> sk = new SkipList<Integer>(3) ;
        sk.insert(4) ;
        assertTrue(sk.contains(4)) ;
        sk.delete(3) ;
        assertTrue(sk.contains(4)) ;
        assertEquals(1, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_08()
    {
        int[] r = { 1,2,3,4,5,6,7,8,9} ;
        SkipList<Integer> sk = create(r) ;
        sk.check() ;
        delete(sk, r) ;
        assertEquals(0, sk.size()) ;
        sk.check() ;
    }

    @Test public void skiplist_09()
    {
        int[] r = { 1,2,3,4,5,6,7,8,9} ;
        SkipList<Integer> sk = create(r) ;
        sk.check() ;
        testIteration(sk, r, 5) ;
    }

    @Test public void skiplist_10()
    {
        int[] r = { 1,2,3,4,5,6,7,8,9} ;
        SkipList<Integer> sk = create(r) ;
        testIter(sk, 2, 4, 2,3) ;
        testIter(sk, -1,4,  1,2,3) ;
        testIter(sk, -1,99,  r) ;
    }

    static void testIter(SkipList<Integer> sk, Integer lo, Integer hi, int... ans)
    {
        List<Integer> x = new ArrayList<Integer>() ;
        for ( int r : sk.records(lo,  hi))
        {
            x.add(r) ;
        }

        assertEquals(ans.length, x.size()) ;


        List<Integer> y = new ArrayList<Integer>() ;
        for ( Integer ii : ans )
            y.add(ii) ;
        assertEquals(format("(%s,%s)",lo, hi), y, x) ;

    }

}
