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

package structure.exthash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static structure.exthash.ExtHashMemTestBase.check;
import static structure.exthash.ExtHashMemTestBase.create;
import static structure.exthash.ExtHashMemTestBase.delete;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestExtHashMem
{
    @BeforeClass static public void setup()
    {
        ExtHashMem.NullOut = true ;
        ExtHashMem.Checking = true ;
        ExtHashMem.DefaultBucketSize = 4 ;
    }

    @AfterClass static public void teardown()
    {

    }

    @Test public void create1()
    {
        ExtHashMem<Integer, String> eHash = new ExtHashMem<Integer, String>() ;
        check(eHash) ;
    }

//    @Test public void create2()
//    { ExtHash eHash = build() ; assertNotNull(eHash) ; }

    @Test public void insert1()
    {
        ExtHashMem<Integer, String> eHash = create(1) ;
        check(eHash, 1) ;
        assertEquals(eHash.get(1), "X1") ;
        assertEquals(1, eHash.size()) ;

    }

    @Test public void insert2()
    {
        ExtHashMem<Integer, String> eHash = create(1,2) ;
        check(eHash, 1, 2) ;
        assertEquals(eHash.get(1), "X1") ;
        assertEquals(eHash.get(2), "X2") ;
        assertEquals(2, eHash.size()) ;
    }

    @Test public void insert3()
    {
        ExtHashMem<Integer, String> eHash = create(1,2,3,4,5,6,7,8) ;
        check(eHash, 1, 2, 3, 4, 5, 6, 7, 8) ;
    }

    // Nasty cases
    @Test public void insert4()
    {
        ExtHashMem<Integer, String> eHash = create(0,2,4,8,16) ;
        assertEquals(5, eHash.size()) ;
    }

    @Test public void delete1()
    {
        ExtHashMem<Integer, String> eHash = createAndCheck(1) ;
        assertEquals(1, eHash.size()) ;
        delete(eHash, 1) ;
        assertFalse(eHash.contains(1)) ;
        assertEquals(0, eHash.size()) ;
    }

    @Test public void delete2()
    {
        ExtHashMem<Integer, String> eHash = createAndCheck(1, 2, 3, 4, 5, 6, 7, 8) ;
        delete(eHash, 1, 2, 3, 4, 5, 6, 7, 8) ;
        check(eHash) ;
    }

    @Test public void delete3()
    {
        ExtHashMem<Integer, String> eHash = createAndCheck(1, 2, 3, 4, 5, 6, 7, 8) ;
        delete(eHash, 8, 7, 6, 5, 4, 3, 2, 1) ;
        check(eHash) ;
    }

    static private ExtHashMem<Integer, String> createAndCheck(int... keys)
    {
        ExtHashMem<Integer, String> eHash = create(keys) ;
        check(eHash, keys);
        assertEquals(keys.length, eHash.size()) ;
        return eHash ;
    }
}
