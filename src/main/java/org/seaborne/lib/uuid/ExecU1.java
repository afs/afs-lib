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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.jena.atlas.io.IO;

public class ExecU1 {

    public static void main(String... args) {
        examine();
        timing();
    }

    public static void timing() {
        Supplier<UUID> generator = ()->UUIDFactory.generateV1();
        //Supplier<UUID> generator = ()->UUID.randomUUID();
        int W = 1_000;
        int N = 1_000_000;

        // Warm up.
        for ( int i = 0 ; i < W ; i++ ) {
            UUID u1 = UUIDFactory.generateV1();
        }

        UUID[] a = new UUID[N];
        long x1 = System.nanoTime();
        for ( int i = 0 ; i < N ; i++ ) {
            UUID u1 = generator.get();
            //System.out.println(u1);
            a[i] = u1;
        }
        long x2 = System.nanoTime();

        long nanos = x2-x1;
        System.out.printf("%,d ns\n", nanos);

        double millis = (x2-x1)/1_000_000;
        double rate = N/((x2-x1)/1_000_000_000.0);
        System.out.printf("%,d in %,.3f sec : rate = %,.0f/s\n",N, millis/1000, rate);

        Set<UUID> x = new HashSet<>();
        for ( int i = 0 ; i < N ; i++ ) {
            UUID u = a[i];

            if ( ! x.contains(u) ) {
                x.add(u);
            }
            else {
                System.err.printf("Dup: %d\n", i);
                for ( int j = 0 ; j < i ; j++ ) {
                    UUID u2 =  a[j];
                    if ( u2.equals(u))
                        System.err.printf("Dup: %d %d\n", i,j);
                }
            }
        }
        System.out.printf("Checked: %,d\n", x.size());
    }

    public static void examine() {
//        System.out.println(System.getProperty("java.version"));
//        System.out.println(System.getProperty("java.home"));
        UUID u1 = UUIDFactory.generateV1();
        examine("U", u1.toString());

//        String s = LibUUID1.asString(u1);
//        System.out.println(s);
//        UUID u2 = LibUUID1.parse(s);
//        System.out.println(u2.toString());
    }

    public static void generateSome() {
        for ( int i = 0 ; i < 10 ; i++ ) {
            UUID u1 = UUIDFactory.generateV1();
            System.out.printf("%-2d: %s\n",i,u1.toString());
        }
    }

    public static void systemTick() {
        // Experimentally this ticks every 10 base units. = microseconds
        long x = UUIDCreatorV1.dev_nowSystemTicks();
        // Wait until tick.
        for(;;) {
            long x1 = UUIDCreatorV1.dev_nowSystemTicks();
            if ( x1 != x ) {
                x = x1;
                break;
            }
        }
        // Wait until tick.
        for(;;) {
            long x1 = UUIDCreatorV1.dev_nowSystemTicks();
            if ( x1 != x ) {
                System.out.printf("%d \n", x1-x);
                return;
            }
        }
    }

    static void examine(String label, String x) {
        System.out.println("Examine: "+label);
        System.out.println(x);
        System.out.println();
        UUID uuid = UUID.fromString(x);

        System.out.printf("M:%016x\n", uuid.getMostSignificantBits());
        System.out.printf("L:%016x\n", uuid.getLeastSignificantBits());
        System.out.println();
        if (uuid.getMostSignificantBits() == 0L && uuid.getLeastSignificantBits() == 0L ) {
            System.out.println("Nil UUID");
            return;
        }
        System.out.printf("Version:    %d\n", uuid.version());
        System.out.printf("Variant:    %d\n", uuid.variant());

        if ( uuid.version() == 1 ) {
            System.out.printf("Clock seq:  0x%04x (%d)\n", uuid.clockSequence(), uuid.clockSequence());
            System.out.printf("Node:       0x%012x\n", uuid.node());
            System.out.printf("Timestamp:  0x%015x\n", uuid.timestamp());
            String s =
                p("timeLow(0x%08x)", LibUUID.timeLow(uuid)) + "-"+
                p("timeMid(0x%04x)",LibUUID.timeMid(uuid)) + "-"+
                p("ver(%d)",LibUUID.version(uuid))+"-"+
                p("timeHigh(0x%03x)",LibUUID.timeHigh(uuid)) + "\n"+
                p("var(%d)",LibUUID.variant(uuid))+"-"+
                p("seq(0x%04x)", LibUUID.clockSequence(uuid))+"-"+ // 14 bits, hex
                p("seq(%d)", LibUUID.clockSequence(uuid))+"-"+   // 14 bits, decimal
                p("node(0x%012x)", LibUUID.node(uuid));
            System.out.println(s);
            System.out.println("node: "+LibUUID.macAddress(uuid));
        }
        System.out.println();
        exec("uuid", "-d", x);
    }
    private static String p(String fmt, long value) {
        return String.format(fmt, value);
    }

    static void exec(String... args) {
        try {
            Process process = Runtime.getRuntime().exec(args);
            String s = IO.readWholeFileAsUTF8(process.getInputStream());
            System.out.print(s);
            if ( ! s.endsWith("\n") )
                System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

