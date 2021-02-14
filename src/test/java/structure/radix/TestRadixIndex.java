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

package structure.radix;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.tdb.base.record.Record ;
import org.apache.jena.tdb.base.record.RecordFactory ;
import org.junit.Test ;

public class TestRadixIndex
{
    public static RecordFactory recordFactory = new RecordFactory(4,0) ;

    static byte[] key0 = { 0, 0, 0, 0} ;
    static byte[] key1 = { 1, 1, 1, 1} ;
    static byte[] key2 = { 1, 2, 3, 4 } ;
    static byte[] key3 = { 4, 3, 2, 1} ;
    static byte[] key4 = { 9, 1, 1, 1 } ;

    @Test public void radixindex_01()
    {
        RadixIndex index = new RadixIndex(recordFactory) ;
        assertTrue(index.isEmpty()) ;
    }

    @Test public void radixindex_02()
    {
        RadixIndex index = new RadixIndex(recordFactory) ;
        add(index, key0, key1, key2, key3, key4) ;

        Record r = index.minKey() ;
        assertArrayEquals(key0, r.getKey()) ;
        r = index.maxKey() ;
        assertArrayEquals(key4, r.getKey()) ;
    }

    private void add(RadixIndex index, byte[] ... keys)
    {
        for ( byte[] k : keys )
        {
            Record r = recordFactory.create(k) ;
            index.add(r) ;
        }
    }
}

