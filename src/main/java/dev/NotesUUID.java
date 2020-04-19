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

package dev;

public class NotesUUID {
    // Revisit time and state storage
    // https://github.com/f4b6a3/uuid-creator

    // Like RFC 4122 - read clock each time.
    // except when USE_REAL_ADDRESS is false
    // except block allocator for time controlled by UUIDS_PER_BLOCK.

    // 2020-04:
    //    Spin, block of 10 - ~4.5 million/s (warmedup), 25 => 6-7m/s
    //    Spin, block of 1 - ~1 million/s,
    //    Block of 10, sleep(1) ~ 25K/s
    //    UUID.randomUUID(), 2 million/s.
}

