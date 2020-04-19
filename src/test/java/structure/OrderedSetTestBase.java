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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

public abstract class OrderedSetTestBase
{
    protected abstract OrderedSet<Integer> create() ;

    protected OrderedSet<Integer> create(int[] items)
    {
        OrderedSet<Integer> index = create() ;
        for ( int i : items )
            index.add(i) ;
        return index ;

    }

    @Test public void ins_00()
    {
        int[] r = { } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_01()
    {
        int[] r = { 1 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_02()
    {
        int[] r = { 1, 2 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_03()
    {
        int[] r = { 2 , 1 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_04()
    {
        int[] r = { 1, 2, 3, 4, 5 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_05()
    {
        int[] r = { 5, 4, 3, 2, 1 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_06()
    {
        int[] r = { 1, 3, 5, 7, 2, 4, 6 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    // Causes a pivotLeftRight (RightLeft) in AVL
    @Test public void ins_07()
    {
        int[] r = { 1, 6, 3, 4, 5, 2, 7, 0 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_08()
    {
        int[] r = { 1, 3, 2 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_09()
    {
        int[] r = { 3, 1, 2 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_10()
    {
        int[] r = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_11()
    {
        int[] r = { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_12()
    {
        int[] r = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19 };
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_13()
    {
        int[] r = { 20, 18, 16, 14, 12, 10, 8, 6, 4, 2, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19 };
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_14()
    {
        int[] r = { 20, 18, 16, 14, 12, 10, 8, 6, 4, 2, 19, 17, 15, 13, 11, 9, 7, 5, 3, 1 };
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_15()
    {
        int[] r = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 19, 17, 15, 13, 11, 9, 7, 5, 3, 1 };
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
    }

    @Test public void ins_16()
    {
        int[] r = { 1, 4, 5, 2, 3, 6, 11, 14, 15, 22, 23, 26 };
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        assertTrue(index.add(7)) ;
        assertFalse(index.add(7)) ;

        for ( int i : r )
            assertFalse(index.add(i)) ;
    }


    @Test public void del_01_1()
    {
        int[] r = { 1 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        OrderedSetTestLib.delete(index, r) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_01_2()
    {
        int[] r = { 1 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        OrderedSetTestLib.delete(index, 2) ;
        OrderedSetTestLib.check(index, r) ;
    }


    @Test public void del_02_1()
    {
        int[] r1 = { 1, 2 } ;
        int[] r2 = { 1, 2 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_02_2()
    {
        int[] r1 = { 1, 2 } ;
        int[] r2 = { 2, 1 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }


    @Test public void del_03_1()
    {
        int[] r1 = { 2 , 1 } ;
        int[] r2 = { 1 , 2 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_03_2()
    {
        int[] r1 = { 2 , 1 } ;
        int[] r2 = { 2 , 1 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_03_3()
    {
        int[] r1 = { 2, 3, 4, 1 } ;
        int[] r2 = { 3 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, 1, 2, 4) ;
    }

    @Test public void del_03_4()
    {
        int[] r1 = { 3, 2, 1, 4 } ;
        int[] r2 = { 2 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, 1, 3, 4) ;
    }

    @Test public void del_04_1()
    {
        int[] r1 = { 1, 2, 3, 4, 5 } ;
        int[] r2 = { 1, 2, 3, 4, 5 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_04_2()
    {
        int[] r1 = { 1, 2, 3, 4, 5 } ;
        int[] r2 = { 5, 4, 3, 2, 1 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_04_3()
    {
        int[] r1 = { 1, 2, 3, 4, 5 } ;
        int[] r2 = { 1, 3, 5 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, 2, 4) ;
    }

    @Test public void del_04_4()
    {
        int[] r1 = { 1, 2, 3, 4, 5 } ;
        int[] r2 = { 4, 2 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, 1,3,5) ;
    }

    @Test public void del_04_5()
    {
        int[] r1 = { 1, 2, 3, 4, 5 } ;
        int[] r2 = { 4, 2 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, 1,3,5) ;
        OrderedSetTestLib.delete(index, 9) ;
        OrderedSetTestLib.check(index, 1,3,5) ;
    }

    @Test public void del_05_1()
    {
        int[] r1 = { 5, 4, 3, 2, 1 } ;
        int[] r2 = { 1, 2, 3, 4, 5 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_05_2()
    {
        int[] r1 = { 5, 4, 3, 2, 1 } ;
        int[] r2 = { 1, 3, 5 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, 2,4) ;
    }

    @Test public void del_06_1()
    {
        int[] r1 = { 1, 3, 5, 7, 2, 4, 6 } ;
        int[] r2 = { 1, 3, 5, 7, 2, 4, 6 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_07()
    {
        int[] r1 = { 1, 6, 3, 4, 5, 2, 7, 0 } ;
        int[] r2 = { 1, 6, 3, 4, 5, 2, 7, 0 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.size(index, 0) ;
    }

    @Test public void del_08()
    {
        int[] r1 = { 1, 6, 3, 4, 5, 2, 7, 0 } ;
        int[] r2 = { 4, 6, 3, 5 } ;
        int[] r3 = { 1, 7, 0, 2 } ;
        OrderedSet<Integer> index = create(r1) ;
        OrderedSetTestLib.check(index, r1) ;
        OrderedSetTestLib.delete(index, r2) ;
        OrderedSetTestLib.check(index, r3) ;
        OrderedSetTestLib.size(index, 4) ;
    }


    @Test public void del_10()
    {
        int[] r = { 1, 16, 3, 14, 5, 2, 37, 11, 6, 23, 4, 25, 12, 7, 40 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        // Some things not in the ordered set
        assertFalse(index.remove(99)) ;
        assertFalse(index.remove(0)) ;
        assertFalse(index.remove(20)) ;

        for ( int i : r )
            assertTrue("remove i="+i,index.remove(i)) ;
    }

    @Test public void iter_01()
    {
        int[] r = { 3, 1, 2 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator() ;
        OrderedSetTestLib.check(iter, 1,2,3) ;
    }

    @Test public void iter_02()
    {
        int[] r = { 1, 2, 3, 4 , 5 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator() ;
        OrderedSetTestLib.check(iter, 1,2,3,4,5) ;
    }

    @Test public void iter_03()
    {
        int[] r = { 10, 8, 6, 4, 2 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator(2,4) ;
        OrderedSetTestLib.check(iter, 2) ;

        iter = index.iterator(-99,4) ;
        OrderedSetTestLib.check(iter, 2) ;

    }

    @Test public void iter_04()
    {
        int[] r = { 2, 4, 6, 8, 10 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator(-99,4) ;
        OrderedSetTestLib.check(iter, 2) ;
    }

    @Test public void iter_05()
    {
        int[] r = { 2, 4, 6, 8, 10 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator(6,99) ;
        OrderedSetTestLib.check(iter, 6,8,10) ;
    }

    @Test public void iter_06()
    {
        int[] r = { 2, 4, 6, 8, 10 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator(null,null) ;
        OrderedSetTestLib.check(iter, r) ;
    }

    @Test public void iter_07()
    {
        int[] r = { 2 } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator(null,null) ;
        OrderedSetTestLib.check(iter, r) ;
    }

    @Test public void iter_08()
    {
        int[] r = { } ;
        OrderedSet<Integer> index = create(r) ;
        OrderedSetTestLib.check(index, r) ;
        Iterator<Integer> iter = index.iterator(null,null) ;
        OrderedSetTestLib.check(iter, r) ;
    }

}
