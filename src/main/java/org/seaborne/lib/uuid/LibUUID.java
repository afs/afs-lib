/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seaborne.lib.uuid;

import java.util.Locale;
import java.util.UUID;

import org.apache.jena.atlas.lib.BitsLong;

public class LibUUID {

    private static final int StdVersion    = 1 ;
    private static final int StdVariant    = 2 ;
    private static final String nilString  = "00000000-0000-0000-0000-000000000000";
    private static final UUID uuid_nil     = UUID.fromString(nilString);

    public static UUID nil() {
        return uuid_nil;
    }

    public static String strNil() {
        return nilString;
    }

    private static final boolean UseSystemUUID = true;

    public static String asString(UUID uuid) {
        if ( UseSystemUUID )
            return uuid.toString();
        return uuidString(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    /** Recreate a UUID from string */
    public static UUID parse(String s) {
        if ( s.equals(strNil()) )
            return nil() ;

        // Canonical: this works in conjunction with .equals
        s = s.toLowerCase(Locale.ROOT) ;

        if ( s.startsWith("urn:") )
            s = s.substring(4) ;
        if ( s.startsWith("uuid:") )
            s = s.substring(5) ;

        if ( s.length() != 36 )
            throw new UUIDException("UUID string is not 36 chars long: it's " + s.length() + " [" + s + "]") ;

        if ( s.charAt(8) != '-' || s.charAt(13) != '-' || s.charAt(18) != '-' || s.charAt(23) != '-' )
            throw new UUIDException("String does not have dashes in the right places: " + s) ;

        if ( UseSystemUUID )
            return UUID.fromString(s);

        // Deciding code. Historical.
        // The UUID broken up into parts.
        //       00000000-0000-0000-0000-000000000000
        //       ^        ^    ^    ^    ^
        // Byte: 0        4    6    8    10
        // Char: 0        9    14   19   24  including hyphens
        int x = (int)BitsLong.unpack(s, 19, 23) ;
        int variant = (x >>> 14) ;
        int version = (int)BitsLong.unpack(s, 14, 15) ;

        if ( variant != StdVariant )
            throw new UnsupportedOperationException("String specifies unsupported UUID variant: " + variant) ;

        long mostSigBits = BitsLong.unpack(s, 0, 8) ;
        // Skip -
        mostSigBits = mostSigBits << 16 | BitsLong.unpack(s, 9, 13) ;
        // Skip -
        mostSigBits = mostSigBits << 16 | BitsLong.unpack(s, 14, 18) ;
        // Skip -
        long leastSigBits = BitsLong.unpack(s, 19, 23) ;
        // Skip -
        leastSigBits = leastSigBits<<48 | BitsLong.unpack(s, 24, 36) ;
        return new UUID(mostSigBits, leastSigBits);
    }

    /** Format using two longs - assumed valid for an UUID of some kind */
    public static String uuidString(long mostSignificantBits, long leastSignificantBits) {
        StringBuffer sb = new StringBuffer(36) ;
        toHex(sb, BitsLong.unpack(mostSignificantBits, 32, 64), 4) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(mostSignificantBits, 16, 32), 2) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(mostSignificantBits, 0, 16), 2) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(leastSignificantBits, 48, 64), 2) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(leastSignificantBits, 0, 48), 6) ;
        return sb.toString() ;
    }

    /** Get the variant with full variable length encoding, decoding any variant usage. */
    public static int getVariantDecode(UUID uuid) {
        return getVariantDecode(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    /** Get the variant with full variable length encoding, decoding any variant usage. */
    public static int getVariantDecode(long mostSigBits, long leastSigBits) {
        // This could be sensitive to the variant encoding.
        // https://tools.ietf.org/html/rfc4122#page-6
        int b0 = (int)BitsLong.unpack(leastSigBits, 63, 64);
        if ( b0 == 0 )
            // Bit pattern 0xx
            return 0;
        int b1 = (int)BitsLong.unpack(leastSigBits, 62, 63);
        if ( b1 == 0 )
            // Bit pattern 10x - the normal UUID variant.
            return 2;
        int b2 = (int)BitsLong.unpack(leastSigBits, 61, 62);
        if ( b2 == 0 )
            // 110
            return 0x4;
        else
            // Bit pattern 111
            return 0x7;
    }

    // ----------------------------------------------------
    // Worker functions

    private static void toHex(StringBuffer sBuff, long value, int lenBytes) {
        // Insert in high-low order, by nibble
        for (int i = 2 * lenBytes - 1; i >= 0; i--) {
            int shift = 4 * i ;
            int x = (int)(value >>> shift & 0xF) ;
            sBuff.append(Character.forDigit(x, 16)) ;
        }
    }

    // Only set to "true" for development use.
    private static boolean VALIDATING = false ;

    private static void checkVersion1(UUID uuid) {
        if ( uuid.version() != UUIDCreatorV1.VERSION || uuid.variant() != UUIDCreatorV1.VARIANT )
            throw new IllegalArgumentException("Not a RFC 4122 V1 UUID");
    }

    // ---- Accesssors

    public static int version(UUID uuid) {
        return uuid.version();
    }

    public static int variant(UUID uuid) {
        return uuid.variant();
    }

    public static long timeHigh(UUID uuid) {
        checkVersion1(uuid);
        if ( VALIDATING ) {
            long x1 = BitsLong.unpack(uuid.getMostSignificantBits(), 0, 12);
            long x = uuid.timestamp();
            long x2 = x >>> 48 ;
            if ( x1 != x2 )
                throw new UUIDException("High time differeces");
            return x1;
        }
        return BitsLong.unpack(uuid.getMostSignificantBits(), 0, 12);
    }

    public static long timeMid(UUID uuid) {
        checkVersion1(uuid);
        if ( VALIDATING ) {
            long x1 = BitsLong.unpack(uuid.getMostSignificantBits(), 16, 32);
            long x = uuid.timestamp();
            long x2 = x >>> 32;
            x2 = x2 & 0xFFFFL;
            if ( x1 != x2 )
                throw new UUIDException("Mid time differeces");
        }
        return BitsLong.unpack(uuid.getMostSignificantBits(), 16, 32);
    }

    public static long timeLow(UUID uuid) {
        checkVersion1(uuid);
        if ( VALIDATING ) {
            long x1 = BitsLong.unpack(uuid.getMostSignificantBits(), 32, 64);
            long x = uuid.timestamp();
            long x2 = x & 0xFFFFFFFFL;
            if ( x1 != x2 )
                throw new UUIDException("Low time differeces");
        }
        return BitsLong.unpack(uuid.getMostSignificantBits(), 32, 64);
    }

    public static long timestamp(UUID uuid) {
        checkVersion1(uuid);
        if ( VALIDATING ) {
            long x1 = timeLow(uuid) | timeMid(uuid)<<32 | timeHigh(uuid)<<48 ;
            long x2 = uuid.timestamp();
            if ( x1 != x2 )
                throw new UUIDException("Timestamp differeces");
        }
        //return uuid.timestamp();
        return timeLow(uuid) | timeMid(uuid)<<32 | timeHigh(uuid)<<48 ;
    }

    public static long clockSequence(UUID uuid) {
        checkVersion1(uuid);
        // return uuid.clockSequence();
        return BitsLong.unpack(uuid.getLeastSignificantBits(), 48, 62) ;
    }

    public static long node(UUID uuid) {
        checkVersion1(uuid);
        //return uuid.node();
        return BitsLong.unpack(uuid.getLeastSignificantBits(), 0, 48);
    }

    public static String macAddress(UUID uuid) {
        long node = uuid.node();
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x",
            BitsLong.unpack(node, 40, 48),
            BitsLong.unpack(node, 32, 40),
            BitsLong.unpack(node, 24, 32),
            BitsLong.unpack(node, 16, 24),
            BitsLong.unpack(node, 8, 16),
            BitsLong.unpack(node, 0, 8));
    }
 // Unicast vs. multicast : least significant bit of an address's first octet is 0 (zero) => unicast
}

