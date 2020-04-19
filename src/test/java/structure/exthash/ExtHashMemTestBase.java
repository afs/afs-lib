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
import static org.apache.jena.atlas.lib.ListUtils.asList;
import static org.apache.jena.atlas.lib.ListUtils.unique;
import static org.apache.jena.atlas.lib.RandomLib.random;
import static org.apache.jena.atlas.test.Gen.permute;
import static org.apache.jena.atlas.test.Gen.rand;
import static org.apache.jena.atlas.test.Gen.strings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.test.ExecGenerator;
import org.apache.jena.atlas.test.RepeatExecution;

public class ExtHashMemTestBase
{
    public static int BucketSize = 2 ;  
    public static void randTests(int maxValue, int maxNumKeys, int iterations, boolean showProgess)
    {
        ExtHashTest test = new ExtHashTest(maxValue, maxNumKeys) ;
        RepeatExecution.repeatExecutions(test, iterations, showProgess) ;
    }
    
    static class ExtHashTest implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;
        ExtHashTest(int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("ExtHashTest: Max value less than number of keys") ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }
        
        @Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            randTest(maxValue, numKeys) ;
        }
    }

    public static void randTest(int maxValue, int numKeys)
    {
//      if ( numKeys >= 3000 )
//      System.err.printf("Warning: too many keys\n") ;

        int[] r1 = rand(numKeys, 0, maxValue) ;
        int[] r2 = permute(r1) ;
        runTest(r1, r2) ;
    }
        
    public static void runTest(int[] r1, int[] r2)
    {
        try {
            ExtHashMem<Integer, String> table = create(r1) ;
            check(table, r1) ;
            delete(table, r2) ;
            check(table) ;
        } catch (RuntimeException ex)
        {
            System.err.println() ;
            System.err.printf("int[] r1 = {%s} ;\n", strings(r1)) ;
            System.err.printf("int[] r2 = {%s}; \n", strings(r2)) ;
            throw ex ;
        }
    }

    public static ExtHashMem<Integer, String> create(int...recs)
    {
        ExtHashMem<Integer, String> table = new ExtHashMem<Integer, String>(BucketSize) ;
        for ( int i : recs )
        {
            table.put(i, "X"+i) ;
            if ( false ) table.dump() ;
        }
        return table ;
    }

    public static ExtHashMem<Integer, String> delete(ExtHashMem<Integer, String> table, int...recs)
    {
        for ( int i : recs )
            table.remove(i) ;
        return table ;
    }

    
    public static void check(ExtHashMem<Integer, String> table, int...recs)
    {
        table.check();
        for ( int i : recs )
            assertNotNull(table.get(i)) ;
        List<Integer> y = unique(asList(recs)) ;
        assertEquals(y.size(), table.size()); 
    }

    
    public static void check(Iterator<Integer> iter, int...recs)
    {
        for ( int i : recs )
        {
            assertTrue("Iterator shorter than test answers", iter.hasNext()) ;
            int j = iter.next() ;
            assertEquals(i,j) ;
        }
        assertFalse("Iterator longer than test answers", iter.hasNext()) ;
    }
    
    public  static void size(ExtHashMem<Integer, String> table, long size)
    {
//        long x = avl.size() ;
//        assertEquals(size, x) ;
//        if ( size == 0 )
//            assertTrue(avl.isEmpty()) ;
//        else
//            assertFalse(avl.isEmpty()) ;
    }
    
}
