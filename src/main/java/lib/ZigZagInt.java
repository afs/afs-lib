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

import java.nio.ByteBuffer ;
import java.util.Arrays ;

import org.apache.jena.atlas.lib.NotImplemented ;

/** 
 * ZigZag integers: map signed numbers onto unsigned numbers
 * that can then be {@linkplain VarInt} encoded.
 */
public final class ZigZagInt
{
	/*
	  http://code.google.com/apis/protocolbuffers/docs/encoding.html
	  0 => 0
	  -1 => 1
	  1 => 2
	  etc
	  which is (32 bit)
	  (n << 1) ^ (n >> 31)
	  The >> is arthimetic shift
	  n>>32 is zero for n >= 0
	  n>>32 is all ones for n < 0
	*/

	public static byte[] encode(long value)
	{
	    //return null ;
	    throw new NotImplemented("ZigZagInt.encode") ;
	}

	public static int encode(ByteBuffer bytes, long value)
	{ return -1 ; }

	public static int encode(ByteBuffer bytes, int startIdx, long value)
	{ return -1 ; }

	public static long decode(byte[] bytes, int idx)
    { return decode(ByteBuffer.wrap(bytes), idx) ; }
    
    public static long decode(ByteBuffer bytes)
    { return decode(bytes, 0) ; }
    
    public static long decode(ByteBuffer bytes, int idx)
    { return -1 ; }
	
    /** Make a VarInteger from the bytes found start from idx */ 
    public static ZigZagInt make(byte[] bytes)
    {
        return new ZigZagInt(bytes) ;
    }
    
    private static String toString(byte[] bytes) { return toString(bytes, 0) ; }
    private static String toString(byte[] bytes, int idx)
    { return null ; }

	public static boolean equals(long value, byte[] bytes) { return equals(value, ByteBuffer.wrap(bytes)) ; }
    
    public static boolean equals(long value, ByteBuffer bytes) { return equals(value, bytes, 0) ; }
    
    public static boolean equals(long value, ByteBuffer bytes, int idx)
    {
        long x = decode(bytes, idx) ;
        return x == value ;
    }
    // ---- Factory
    
    // -- Some constants
    public static ZigZagInt zigzag_0 = new ZigZagInt(0) ;
    public static ZigZagInt zigzag_1 = new ZigZagInt(1) ;
    public static ZigZagInt zigzag_2 = new ZigZagInt(2) ;
    public static ZigZagInt zigzag_minus_1 = new ZigZagInt(-1) ;

    /** Return a ZigZagInteger that encodes the value */
    public static ZigZagInt valueOf(long value)
    { 
        if ( value == 0 ) return zigzag_0 ;
        if ( value == 1 ) return zigzag_1 ;
        if ( value == -1 ) return zigzag_minus_1 ;
        if ( value == 2 ) return zigzag_2 ;
        return new ZigZagInt(value) ;
    }
    
    public static int lengthOf(long value)
    {
        return calcLengthTable(value) ;
    }
    
    // ---- The object

    byte[] bytes ;
    long value = -1 ;
    
    private ZigZagInt(long value)
    {
        if ( value < 0 )
            throw new IllegalArgumentException("Positive integers only") ;
        bytes = encode(value) ;
    }
    
    private ZigZagInt(byte[] bytes)
    {
        if ( bytes.length == 0 )
            throw new IllegalArgumentException("Zero length byte[]") ;
        this.bytes = bytes ;
    }
    
    public int length()     { return bytes.length ; }
    public byte[] bytes()   { return bytes ; }
    
    public long value()
    { 
        if ( value == -1 )
            value = decode(bytes, 0) ;
        return value ;
    }
    
    @Override
    public int hashCode()
    { return Arrays.hashCode(bytes) ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof ZigZagInt ) ) return false ;
        ZigZagInt vint = (ZigZagInt)other ;
        return Arrays.equals(bytes, vint.bytes) ;
    }
    
    @Override
    public String toString() { return toString(bytes) ; }
    
     // Next is the impossible (it's the sign bit) 1L<<63
     static final long VarIntLengths[] = { 1L<<7 , 1L<<14 , 1L<<21 , 1L<<28 , 1L<<35, 1L<<42, 1L<<49, 1L<<56 } ;
 
     // By semi-lookup.
     private static int calcLengthTable(long value)
     {
         // Convert to VarInteger 
         value = (value << 1) ^ (value >> 63) ;
         return VarInt.calcLengthTable(value) ;
     }
     
//     // By calculation.
//     private static int calcLength(long value)
//     {
//         int len = 1 ;   // The byte with high bit zero.
//         long N = value ;
//         byte b = 0 ;
//         while (true)
//         {
//             N >>>= 7 ;
//             if ( N == 0 )
//                 break ;
//             len ++ ;
//         }
//         return len ;
//     }
}
