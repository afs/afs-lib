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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.ByteBuffer;

import org.apache.jena.atlas.lib.ByteBufferLib;
import org.junit.Test;

public class TestVarInt {
    @Test public void varint_01() {
        VarInt vint = VarInt.valueOf(0);
        assertEquals(1, vint.length());
        assertEquals((byte)0, vint.bytes()[0]);
        assertEquals(0L, vint.value());
    }

    @Test public void varint_02() {
        VarInt vint = VarInt.valueOf(1);
        assertEquals(1, vint.length());
        assertEquals((byte)1, vint.bytes()[0]);
        assertEquals(1L, vint.value());
    }

    @Test public void varint_03() {
        VarInt vint = VarInt.valueOf(127);
        assertEquals(1, vint.length());
        assertEquals((byte)0x7F, vint.bytes()[0]);
        assertEquals(127L, vint.value());
    }

    @Test public void varint_04() {
        VarInt vint = VarInt.valueOf(128);
        assertEquals(2, vint.length());
        assertEquals((byte)0x80, vint.bytes()[0]);
        assertEquals((byte)0x01, vint.bytes()[1]);
        assertEquals(128L, vint.value());
    }

    @Test public void varint_05() {
        VarInt vint = VarInt.valueOf(129);
        assertEquals(2, vint.length());
        assertEquals((byte)0x81, vint.bytes()[0]);
        assertEquals((byte)0x01, vint.bytes()[1]);
        assertEquals(129L, vint.value());
    }

    @Test public void varint_10() {
        VarInt vint = VarInt.valueOf(1L << 45);
        // assertEquals(2, vint.length()) ;
        assertEquals(1L << 45, vint.value());
    }

    // General hammering.
    @Test public void varint_N() {
        for ( long x = 0 ; x < (1L << 17) ; x++ ) {
            VarInt vint = VarInt.valueOf(x);
            assertEquals(x, vint.value());
        }
    }

    @Test public void varint_eq_1() {
        VarInt x = VarInt.valueOf(0);
        VarInt x0 = VarInt.valueOf(0);
        VarInt x1 = VarInt.valueOf(1);
        assertEquals(x.hashCode(), x0.hashCode());
        assertNotEquals(x.hashCode(), x1.hashCode());
        assertEquals(x, x0);
        assertNotEquals(x, x1);
    }

    @Test public void varint_eq_2() {
        VarInt x = VarInt.valueOf(1);
        VarInt x0 = VarInt.valueOf(0);
        VarInt x1 = VarInt.valueOf(1);
        assertEquals(x.hashCode(), x1.hashCode());
        assertNotEquals(x.hashCode(), x0.hashCode());
        assertEquals(x, x1);
        assertNotEquals(x, x0);
    }

    private static void eq(long value) {
        VarInt x0 = VarInt.valueOf(value);
        VarInt x1 = VarInt.valueOf(value);
        assertEquals(x0.hashCode(), x1.hashCode());
        assertEquals(x0, x1);
    }

    @Test public void varint_eq_3() {
        eq(127);
    }

    @Test public void varint_eq_4() {
        eq(128);
    }

    @Test public void varint_eq_5() {
        eq(129);
    }

    private static void varint_bb_abs(long v, int len) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        ByteBufferLib.fill(bb, (byte)23);
        int l = VarInt.encode(bb, 1, v);
        assertEquals(len, l);
        assertEquals(23, bb.get(0));
        assertEquals(23, bb.get(l + 1));
        long v2 = VarInt.decode(bb, 1);
        assertEquals(v, v2);
    }

    private static void varint_bb_rel(long v, int len) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        ByteBufferLib.fill(bb, (byte)23);
        bb.position(1);
        int l = VarInt.encode(bb, v);
        assertEquals(len, l);
        assertEquals(23, bb.get(0));
        assertEquals(23, bb.get(l + 1));
        bb.position(1);
        long v2 = VarInt.decode(bb);
        assertEquals(v, v2);
    }

    @Test public void varint_bb_abs_1() {
        varint_bb_abs(0, 1);
    }

    @Test public void varint_bb_abs_2() {
        varint_bb_abs(1, 1);
    }

    @Test public void varint_bb_abs_3() {
        varint_bb_abs(128, 2);
    }

    @Test public void varint_bb_abs_4() {
        varint_bb_abs((1L << 63) - 1, 9);
    }

    @Test public void varint_bb_rel_1() {
        varint_bb_rel(0, 1);
    }

    @Test public void varint_bb_rel_2() {
        varint_bb_rel(1, 1);
    }

    @Test public void varint_bb_rel_3() {
        varint_bb_rel(128, 2);
    }

    @Test public void varint_bb_rel_4() {
        varint_bb_rel((1L << 63) - 1, 9);
    }

    @Test public void varint_extract_1() {
        VarInt x0 = VarInt.valueOf(113);
        VarInt x1 = VarInt.make(x0.bytes);
        assertEquals(x0, x1);
    }

    @Test public void varint_extract_2() {
        VarInt x0 = VarInt.valueOf(113);
        ByteBuffer bb = ByteBuffer.wrap(x0.bytes());
        VarInt x1 = VarInt.make(bb, 0);
        assertEquals(x0, x1);
    }

    @Test public void varint_extract_3() {
        VarInt x0 = VarInt.valueOf(11377);
        ByteBuffer bb = ByteBuffer.wrap(x0.bytes());
        VarInt x1 = VarInt.make(bb, 0);
        assertEquals(x0, x1);
    }

    @Test public void varint_length_1() {
        assertEquals(1, VarInt.lengthOf(0));
        assertEquals(1, VarInt.lengthOf(1));
        assertEquals(1, VarInt.lengthOf(127));
        assertEquals(2, VarInt.lengthOf(128));
        assertEquals(2, VarInt.lengthOf(1L << 14 - 1));
        assertEquals(3, VarInt.lengthOf(1L << 14));
        assertEquals(8, VarInt.lengthOf(1L << 56 - 1));
    }
}
