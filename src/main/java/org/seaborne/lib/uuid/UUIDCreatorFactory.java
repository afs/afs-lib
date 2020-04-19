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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDCreatorFactory {
    // -- Multiple generators
    // If wanted, use the random node and random clock seq (60bits of random) to have separate generators.

    static Set<UUIDCreator> generators = ConcurrentHashMap.newKeySet();

    /** Return a new, fresh generator of a UUID version 1. See {@link #create()} */
    /*package*/ static synchronized UUIDCreator newGeneratorV1() {
        for ( int i = 0 ; i < 10 ; i++ ) {
            UUIDCreatorV1 generator = new UUIDCreatorV1();
            if ( generators.contains(generator) )
                continue;
            generators.add(generator);
            return generator;
        }
        throw new UUIDException("Fail to create a distinct UUID generator");
    }

    /*package*/ static void releaseGenerator(UUIDCreator generator) {
        generators.remove(generator);
    }
}

