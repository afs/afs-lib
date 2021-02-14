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

package lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestArrayOps
{
    @BeforeClass public static void beforeClass()
    {
        //ArrayOps.NullOut = true;
        ArrayOps.Checking = true;
    }

    // ---- Clear
    @Test public void clear1() {
        String[] array = {"a", "b", "c", null, null };
        String[] array2 = {null, "b", "c", null, null };
        ArrayOps.clear(array, 0, 1 );
        assertArrayEquals(array2,array);
    }

    @Test public void clear2() {
        String[] array = {"a", "b", "c", "d", null };
        String[] array2 = {"a", null, null, "d", null };
        ArrayOps.clear(array, 1, 2 );
        assertArrayEquals(array2,array);
    }

    @Test public void clear3() {
        String[] array = {"a", "b", "c"};
        String[] array2 = {null, null, null};
        ArrayOps.clear(array);
        assertArrayEquals(array2, array);
    }

    // ---- Shift Up
    // Should shift extends the array?  Yes.
    @Test public void shift_up_1() {
        String[] array = {"a", "b", "c", "d", "e" };
        String[] array2 = {null, "a", "b", "c", "e"};  // Extends to length 4.
        ArrayOps.shiftUp(array, 0, 3);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_up_2() {
        String[] array = {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", null, "d"};
        ArrayOps.shiftUp(array, 3, array.length-1);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_up_3() {
        String[] array = {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", "d", null};
        ArrayOps.shiftUp(array, 4, 5);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_up_4() {
        String[] array = {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c",  null, "d" };
        // Shift at top
        ArrayOps.shiftUp(array, 3, 4);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_up_5() {
        String[] array = {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", null, null, "b", "c"};
        ArrayOps.shiftUpN(array, 1, 2, 5);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_up_6() {
        String[] array = {"a", "b", "c", "d", "e"};
        String[] array2 = {null, null, null, null, null};
        ArrayOps.shiftUpN(array, 0, 5, 3);
        assertArrayEquals(array2, array);
    }

    @Test(expected = ArrayOps.ArrayException.class)
    public void shift_up_7() {
        String[] array = {"a", "b", "c", "d", "e"};
        String[] array2 = {null, null, null, null, null};
        ArrayOps.shiftUpN(array, 0, 6, 3);
        assertArrayEquals(array2, array);
    }

    // ---- Shift Down

    @Test public void shift_down_1() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"b", "c", null, "d", "e"};
        ArrayOps.shiftDown(array, 0, 3);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_down_2() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "c", null, "d", "e"};
        ArrayOps.shiftDown(array, 1, 3);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_down_3() {
        String[] array = {"a", "b", "c", "d", "e"};
        String[] array2 = {"a", "b", null, "d", "e"};
        ArrayOps.shiftDown(array, 2, 3);
        assertArrayEquals(array2, array);
    }

    @Test(expected=ArrayOps.ArrayException.class)
    public void shift_down_4() {
        String[] array = {"a", "b", "c", "d", "e"};
        String[] array2 = {};
        ArrayOps.shiftDown(array, 3, 3);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_down_5() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", "d", null };
        ArrayOps.shiftDown(array, 4, 5);
        assertArrayEquals(array2, array);
    }

    @Test public void shift_down_6() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", null, null };
        ArrayOps.shiftDownN(array, 3, 2, 5);
        assertArrayEquals(array2, array);
    }

    @Test(expected=ArrayOps.ArrayException.class)
    public void shift_down_7() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {};
        ArrayOps.shiftDownN(array, 4, 2, 5);
        assertArrayEquals(array2, array);
    }

    // ---- Insert

    @Test public void insert1() {
        String[] array = {"a", "b", "c", "d", "e" };
        String[] array2 = {"z", "a", "b", "c", "e"};
        ArrayOps.insert(array, 0, "z", 3);
        assertArrayEquals(array2, array);
    }

    @Test public void insert2() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "z", "b", "c", "e" };
        ArrayOps.insert(array, 1, "z", 3);
        assertArrayEquals(array2, array);
    }

    @Test public void insert3() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "z", "c", "e" };
        ArrayOps.insert(array, 2, "z", 3);
        assertArrayEquals(array2, array);
    }

    @Test public void insert4() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", "d", "z" };
        ArrayOps.insert(array, 4, "z", 4);
        assertArrayEquals(array2, array);
    }

    @Test public void insert5() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", "d", "z" };
        ArrayOps.insert(array, 4, "z", 5);
        assertArrayEquals(array2, array);
    }

    @Test public void insert7() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"z", "a", "b", "c", "d"};
        ArrayOps.insert(array, 0, "z", 5);
        assertArrayEquals(array2, array);
    }

    @Test(expected=ArrayOps.ArrayException.class)
    public void insert8() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {};
        ArrayOps.insert(array, 5, "z", 5);
        assertArrayEquals(array2, array);
    }

    @Test(expected=ArrayOps.ArrayException.class)
    public void insert9() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {};
        ArrayOps.insert(array, 5, "z", 4);
        assertArrayEquals(array2, array);
    }

    // ---- Delete

    @Test public void delete1() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"b", "c", null, "d", "e"};
        String x = ArrayOps.delete(array, 0, 3);
        assertArrayEquals(array2, array);
        assertEquals("a", x);
    }

    @Test public void delete2() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "c", null, "d", "e"};
        String x = ArrayOps.delete(array, 1, 3);
        assertArrayEquals(array2, array);
        assertEquals("b", x);
    }

    @Test public void delete3() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", null, "d", "e"};
        String x = ArrayOps.delete(array, 2, 3);
        assertArrayEquals(array2, array);
        assertEquals("c", x);
    }

    @Test public void delete4() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "d", "e", null};
        String x = ArrayOps.delete(array, 2, 5);
        assertArrayEquals(array2, array);
        assertEquals("c", x);
    }

    @Test public void delete5() {
        String[] array =  {"a", "b", "c", "d", "e" };
        String[] array2 = {"a", "b", "c", "d", null};
        String x = ArrayOps.delete(array, 4, 5);
        assertArrayEquals(array2, array);
        assertEquals("e", x);
    }

    @Test public void iterate1() {
        String[] array =  {"a", "b", "c", "d", "e" };
        Iterator<String> iter = ArrayOps.iterator(array);
        for ( int i = 0; iter.hasNext(); i++ )
            assertEquals(array[i], iter.next());
    }
}
