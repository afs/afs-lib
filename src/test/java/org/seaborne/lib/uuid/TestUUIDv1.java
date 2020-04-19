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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import java.util.UUID;

import org.junit.Test;

public class TestUUIDv1 {

    @Test public void uuid1() {
        UUID uuid1 = UUIDFactory.generateV1();
        UUID uuid2 = UUIDFactory.generateV1();
        assertNotEquals(uuid1, uuid2);
    }

    // Not secure - nodes match. This may change to rotating random nodes, in
    // which case this test will fail.
    @Test public void uuid2() {
        assumeTrue(UUIDCreatorV1.USE_REAL_ADDRESS);
        UUID uuid1 = UUIDCreatorFactory.newGeneratorV1().create();
        UUID uuid2 = UUIDCreatorFactory.newGeneratorV1().create();
        assertEquals(uuid1.node(), uuid2.node());
    }

    @Test public void uuid3() {
        assumeTrue(UUIDCreatorV1.USE_REAL_ADDRESS);
        UUID uuid1 = UUIDCreatorFactory.newGeneratorV1().create();
        UUID uuid2 = UUIDCreatorFactory.newGeneratorV1().create();
        assertEquals(uuid1.node(), uuid2.node());
    }

    @Test public void uuid4() {
        UUID uuid1 = LibUUID.nil();
        UUID uuid2 = LibUUID.nil();
        // Same object.
        assertSame(uuid1, uuid2);
    }
}

