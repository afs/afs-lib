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

package lib ;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.Bytes ;

/** Utilities for working with arrays including especially for working with a slice in a larger array. 
 *  This avoids the need to copy/move data outside the slice. 
 *  Slice starts a zero.
 *  Shift up extends the slice, moving elements into the area above the slice.
 *  Shift down introduces nulls at the top of the slice array.
 * @see Bytes for operations 
 */
public class ArrayOps
{
    static boolean Checking = true ;
    public static final boolean NullOut =  true ;

//    /** Check a zero-based array slice */ 
//    public static <T> void checkSlice(T[] array, int idx, int length)
//    {
//        if ( length > array.length )        error("Bad length") ;
//        if ( idx >= length )                error("Index out of bounds of slice") ;
//        if ( idx < 0  )                     error("Negative index for slice") ;
//    }
    
    // -- Variable slice
    
    // insert(array, slice, int idx) ;
    // shiftUp(array, slice, idx) 
    // shiftUp(array, slice, idx) -> Slice.
    
    // Repeat for int[] and long[]

    
    /**
     * Insert an element at a specific index
     * 
     * @param <T>
     * @param array
     * @param idx
     * @param item
     */
    public static <T> void insert(T[] array, int idx, T item) {
        insert(array, idx, item, array.length);
    }

    /**
     * Insert an element at a specific index, given a active area of 'length'
     * elements.
     * 
     * @param <T>
     * @param array
     * @param idx
     * @param item
     * @param length
     */
    public static <T> void insert(T[] array, int idx, T item, int length) {
        // Can insert one beyond the array slice.
        if ( Checking ) {
            if ( length > array.length )
                error("Bad slice");
            if ( idx > length )
                error("No room for insert");
            if ( idx >= array.length )
                error("Out of bounds");
            if ( idx < 0 )
                error("Negative index for insert");
        }

        // Shuffle up one place
        if ( idx < length )
            shiftUp(array, idx, length);
        array[idx] = item;
    }

    /**
     * Delete at a specific index, given a active area of 'length' elements of the
     * array
     * 
     * @param <T>
     * @param array
     * @param idx
     * @param length
     * @return Element at slot idx
     */
    public static <T> T delete(T[] array, int idx, int length) {
        T rc = array[idx];
        // Shuffle down one place
        shiftDown(array, idx, length);
        return rc;
    }

    /**
     * Delete at a specific index
     * 
     * @param <T>
     * @param array
     * @param idx
     * @return Element at slot idx
     */
    public static <T> T delete(T[] array, int idx) {
        return delete(array, idx, array.length);
    }

    /**
     * Clear the array.
     * 
     * @param array
     */
    public static <T> void clear(T[] array) {
        clear(array, 0);
    }

    public static <T> void clear(T[] array, int idx) {
        clear(array, idx, array.length - idx);
    }

    public static <T> void clear(T[] array, int idx, int num) {
        num = adjustLength(array, idx, num);

        for ( int i = 0 ; i < num ; i++ )
            array[idx + i] = null;
    }

    /**
     * Copy the byte array (not the contents
     * 
     * @param bytes THe array to copy from
     */
    public static byte[] copyOf(byte[] bytes) {
        // Java6: Arrays.copyOf(bytes, bytes.length)
        return copyOf(bytes, 0, bytes.length);
// byte[] newByteArray = new byte[bytes.length] ;
// System.arraycopy(bytes, 0, newByteArray, 0, bytes.length) ;
// return newByteArray ;
    }

    /**
     * Copy of the byte array, start from given point
     * 
     * @param bytes THe array to copy from
     * @param start Starting point.
     */
    public static byte[] copyOf(byte[] bytes, int start) {
        // Java6: Arrays.copyOf(bytes, bytes.length)
        return copyOf(bytes, start, bytes.length - start);
    }

    /** Copy of the byte array, start from given point */
    public static byte[] copyOf(byte[] bytes, int start, int length) {
        byte[] newByteArray = new byte[length];
        System.arraycopy(bytes, start, newByteArray, 0, length);
        return newByteArray;
    }

    /** Truncate a length of from idx of delta to the array length at most */
    private static <T> int adjustLength(T[] array, int idx, int num) {
        if ( num + idx <= array.length )
            return num;
        // Overshoot amount.
        int x = (num + idx) - array.length;
        return num - x;
    }

    /**
     * Shift up one place, opening up idx, given an array up to 'length' elements.
     * Increases the active length by 1.
     */
    public static <T> void shiftUp(T[] array, int idx, int length) {
        shiftUpN(array, idx, 1, length);
    }

    /**
     * Shift up N places - increases the active length by N - truncates and eleemnts
     * dropp off the top.
     */

    public static <T> void shiftUpN(T[] array, int idx, int places, int length) {
        // System.out.printf("shiftUpN(,idx=%d,places=%d,length=%d)\n", idx, places,
        // length);
        if ( places == 0 )
            return;

        if ( places < 0 )
            error("Negative shift up");

        if ( idx + places > array.length )
            error("out of bounds: " + (idx + places));

        int lengthToMove = length - idx - places + 1;  // Move from idx+1 to the end
                                                       // of slice.

        if ( length + places > array.length ) {
            // System.out.println("Correct: "+lengthToMove+" => "+(array.length - idx
            // - places)) ;
            lengthToMove = array.length - idx - places;
        }

        if ( lengthToMove < 0 )
            error("Negative slice");

        if ( lengthToMove > 0 ) {
            // System.out.printf("arraycopy(,src=%d, dst=%d, length=%d)\n", idx,
            // idx+places, lengthToMove);
            // If equals, no copy needed
            System.arraycopy(array, idx, array, idx + places, lengthToMove);
        }

        if ( NullOut )
            clear(array, idx, places);
    }

    public static <T> void shiftDown(T[] array, int idx, int length) {
        shiftDownN(array, idx, 1, length);
    }

    public static <T> void shiftDownN(T[] array, int idx, int places, int length) {
        if ( Checking ) {
            if ( places < 0 )
                error("Negative shift down");
            if ( idx + places > length )
                error("Out of bounds: " + idx);
        }

        if ( places == 0 )
            return;

        System.arraycopy(array, idx + places, array, idx, length - idx - places);
        if ( NullOut )
            clear(array, length - places, places);
    }

    // ----

    @SafeVarargs
    public static <T> Iterator<T> iterator(T...things) {
        return Arrays.asList(things).iterator();
    }

    public static <T> String toString(T[] array) {
        return Arrays.asList(array).toString();
    }

    public static <T> void print(T[] array) {
        System.out.println(toString(array));
    }

    public static void print(int[] array) {
        List<Integer> x = new ArrayList<Integer>(array.length);
        for ( int i : array )
            x.add(i);
        System.out.println(x);
    }

    public static void print(long[] array) {
        List<Long> x = new ArrayList<Long>(array.length);
        for ( long i : array )
            x.add(i);
        System.out.println(x);
    }

    // ----

    private static void error(String msg) {
        throw new ArrayException(msg);
    }

    public static class ArrayException extends RuntimeException {
        public ArrayException(String msg) {
            super(msg);
        }
    }
}
