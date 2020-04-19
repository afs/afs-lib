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

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.io.BlockUTF8;
import org.apache.jena.atlas.io.InStreamUTF8;
import org.apache.jena.atlas.io.OutStreamUTF8;
import org.junit.Assert;
import org.junit.Test;

/** Encode chars as UTF-8 - this code exists to remember the process and requirements.
 * Normally, done in a stream and is much more improtant.
 * @see InStreamUTF8
 * @see OutStreamUTF8
 * @see BlockUTF8
 */

public class CodecUTF8
{
    public static void main(String...argv) {
        if ( argv.length != 1 ) {
            System.err.println("Usage: main 'string'");
            System.exit(1);
        }

        String s = argv[0];

        for ( int i = 0; i < s.length(); i++ ) {
            char ch = s.charAt(i);
            byte[] bytes = toUTF8(ch);
            System.out.printf("%c =>", ch);
            for ( int j = 0; j < bytes.length; j++ )
                System.out.printf(" %02X", bytes[j]);
            System.out.println();
        }
    }

    // In Modified UTF-8,[15] the null character (U+0000) is encoded as 0xC0,0x80; this is not valid UTF-8[16]
    // Char to bytes.
    /* http://en.wikipedia.org/wiki/UTF-8
Bits    Last code point     Byte 1  Byte 2  Byte 3  Byte 4  Byte 5  Byte 6
  7   U+007F  0xxxxxxx
  11  U+07FF  110xxxxx    10xxxxxx
  16  U+FFFF  1110xxxx    10xxxxxx    10xxxxxx
  21  U+1FFFFF    11110xxx    10xxxxxx    10xxxxxx    10xxxxxx
  26  U+3FFFFFF   111110xx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx
  31  U+7FFFFFFF  1111110x    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx     */


    /** char to int, where int is value, at the low end of the int, of the UTF-8 encoding. */

    static public byte[] toUTF8(char ch)
    {
        // if ( ! Character.isDefined(ch))
        //     throw new AtlasException("No such character: "+(int)ch);

        if ( ch != 0 && ch <= 127 ) return new byte[] {(byte)ch };
        if ( ch == 0 ) return new byte[] { (byte)0xC0, (byte)0x80 };   // Modified UTF-8
        if ( ch == 0 ) return new byte[] { (byte)0 };                  // Java

        if ( ch <= 0x07FF ) {
            @SuppressWarnings("cast")
            final int v = (int)ch;
            // x = low 11 bits yyyyy xxxxxx
            // x = 00000yyyyyxxxxxx
            // x1 = 110yyyyy x2 = 10xxxxxx

            // Hi 5 bits
            int x1 = (v & 0x7C0) >> 6; // BitsInt.access(ch, 21, 26);
            x1 = x1 | 0xC0;

            int x2 = v & 0x3F; // BitsInt.access(ch, 26, 32);
            x2 = x2 | 0x80;
            return new byte[]{(byte)x1, (byte)x2};
        }
        if ( ch <= 0xFFFF ) {
            @SuppressWarnings("cast")
            final int v = (int)ch;
            // x = aaaa bbbbbb cccccc
            // x1 = 1110aaaa x2 = 10bbbbbb x3 = 10cccccc
            int x1 = (v >> 12) & 0x1F;
            x1 = x1 | 0xE0;

            int x2 = (v >> 6) & 0x3F;
            x2 = x2 | 0x80;

            int x3 = v & 0x3F;
            x3 = x3 | 0x80;

            return new byte[]{(byte)x1, (byte)x2, (byte)x3};
        }

        if ( true ) throw new AtlasException();
        // Not java, where chars are 16 bit.
        //if ( ch <= 0x1FFFFF );
        //if ( ch <= 0x3FFFFFF );
        //if ( ch <= 0x7FFFFFFF );

        return null;

    }

//    /** Encode a char as UTF-8, using Java's built-in encoders -
//    may be slow - this is for testing */
//    static public char fromUTF8_test(byte[] x)
//    {
//        InputStream in = new ByteArrayInputStream(x);
//        Reader r = new InStreamUTF8(in);
//        try
//        {
//            return (char)r.read();
//        } catch (IOException e) { throw new AtlasException(e); }
//    }

    static public char fromUTF8(byte[] x) {
        if ( x == null )
            return (char)0;

        for ( int i = 0; i < x.length; i++ ) {
            int b = x[i];
            if ( b == 0xC0 || b == 0xC1 || b >= 0xF5 )
                throw new AtlasException("Bad UTF-8 byte: " + b);
        }

        if ( x.length == 0 )
            return (char)0;
        // if ( x <= 127 )
        if ( x.length == 1 )
            return (char)x[0];

        // if ( x <= 0xFFFF )
        if ( x.length == 2 ) {
            // check: byte 0 is 110aaaaa, byte 1 is 10bbbbbb

            int hi = x[0] & 0x1F;
            int lo = x[1] & 0x3F;
            return (char)((hi << 6) | lo);
        }
        // 1110.... => 3 bytes : 16 bits : not outside 16bit chars
        // if ( x <= 0xFFFFFF )
        if ( x.length == 3 ) {
            // check: byte 0 is 110aaaaa, byte 1 and 2 are 10bbbbbb
// int b0 = (x>>16) & 0x1F;
// int b1 = (x>>8) & 0x3F;
// int b2 = x&0x3F;
            int b0 = x[0] & 0x1F;
            int b1 = x[1] & 0x3F;
            int b2 = x[2] & 0x3F;
            return (char)((b0 << 12) | (b1 << 6) | b2);
        }

        throw new AtlasException("Out of range: " + x);
    }

    // UTF-8 encoding.
    // character '¢' = code point U+00A2 -> C2 A2
    // character '€' = code point U+20AC -> E2 82 AC

    @Test
    public void utf8_c1() {
        testChar(' ');
    }

    @Test
    public void utf8_c2() {
        testChar('¢');
    }

    @Test
    public void utf8_c3() {
        testChar('€');
    }

    @Test
    public void utf8_c4() {
        testChar('\uFFFF');
    }

    @Test
    public void utf8_b1() {
        testBytes((byte)20);
    }

    @Test
    public void utf8_b2() {
        testBytes((byte)0xC2, (byte)0xA2);
    }

    @Test
    public void utf8_b3() {
        testBytes((byte)0xE2, (byte)0x82, (byte)0xAC);
    }

    @Test
    public void utf8_b4() {
        testBytes((byte)0xE2, (byte)0xBF, (byte)0xBF);
    }

    private void testChar(char c) {
        byte[] b = toUTF8(c);
        char c2 = fromUTF8(b);
        Assert.assertEquals(c, c2);
    }

    private void testBytes(byte...b) {
        char c = fromUTF8(b);
        byte[] b2 = toUTF8(c);
        Assert.assertArrayEquals(b, b2);
    }
}
