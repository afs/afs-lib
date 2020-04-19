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

package structure.skiplist;

import static java.lang.String.format;
import static org.apache.jena.atlas.lib.RandomLib.random;
import static org.apache.jena.atlas.test.Gen.permute;
import static org.apache.jena.atlas.test.Gen.rand;
import static org.apache.jena.atlas.test.Gen.strings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.atlas.test.ExecGenerator;
import org.apache.jena.atlas.test.RepeatExecution;

public class SkipListTestBase
{
    public static void randTests(int maxLevel, int maxValue, int maxNumKeys, int iterations, boolean showProgess)
    {
        SkipListTestGenerator test = new SkipListTestGenerator(maxLevel, maxValue, maxNumKeys) ;
        RepeatExecution.repeatExecutions(test, iterations, showProgess) ;
    }

    static class SkipListTestGenerator implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;
        int maxLevel ;

        SkipListTestGenerator(int maxLevel, int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("SkipListTest: Max value less than number of recs") ;
            this.maxLevel = maxLevel ;
            this.maxValue = maxValue ;
            this.maxNumKeys = maxNumKeys ;
        }

        @Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            SkipListTestBase.randTest(maxLevel, maxValue, numKeys) ;
        }
    }

    public static SkipList<Integer> create(int...recs)
    {
        SkipList<Integer> skiplist = new SkipList<Integer>() ;
        for ( int i : recs )
            skiplist.insert(i) ;
        return skiplist ;
    }

    public static SkipList<Integer> delete(SkipList<Integer> skiplist, int...recs)
    {
        for ( int i : recs )
            skiplist.delete(i) ;
        return skiplist ;
    }

    public static void check(SkipList<Integer> skiplist, int...recs)
    {
        skiplist.check() ;
        for ( int r : recs )
            assertTrue(skiplist.contains(r)) ;

        // XXX
//        List<Integer> x = skiplist.calcRecords() ;
//        SortedSet<Integer> r = new TreeSet<Integer>() ;
//
//        // -- Sort to list.
//        for ( int i : recs )
//            r.add(i) ;
//
//        List<Integer> z = new ArrayList<Integer>() ;
//        for ( int i : r )
//            z.add(i) ;
//        // --
//
//        assertEquals(z, x) ;
//
//        if ( r.size() > 0 )
//        {
//            Integer min = z.get(0) ;
//            Integer max = z.get(r.size()-1) ;
//            assertEquals(min, skiplist.min()) ;
//            assertEquals(max, skiplist.max()) ;
//        }
    }

    /* One random test : print the recs if there was a problem */

    public static void randTest(int maxLevel, int maxValue, int numKeys)
    {
        if ( numKeys >= 3000 )
            System.err.printf("Warning: too many recs\n") ;

        int[] recs1 = rand(numKeys, 0, maxValue) ;
        int[] recs2 = permute(recs1) ;
        try {
            SkipList<Integer> skiplist = buildSkipList(maxLevel, recs1);
            if ( true )
            {
                // Checking tests.
                check(skiplist, recs2);
                testIteration(skiplist, recs1, 10) ;
            }
            testDelete(skiplist, recs2) ;
        } catch (RuntimeException ex)
        {
            System.err.printf("int maxLevel=%d ;\n", maxLevel) ;
            System.err.printf("int[] recs1 = {%s} ;\n", strings(recs1)) ;
            System.err.printf("int[] recs2 = {%s}; \n", strings(recs2)) ;
            throw ex ;
        }
    }

    private static SkipList<Integer> buildSkipList(int maxLevel, int[] records)
    {
        SkipList<Integer> skiplist = new SkipList<Integer>(maxLevel) ;
        for ( int r : records )
            skiplist.insert(r) ;
        skiplist.check();
        return skiplist ;
    }

    private static void testDelete(SkipList<Integer> skiplist, int[] recs)
    {
        for ( int r : recs )
            skiplist.delete(r) ;
        skiplist.check();
        for ( int r : recs )
            assertFalse(skiplist.contains(r)) ;
    }

    public static void testIteration(SkipList<Integer> skiplist, int[] recs, int numIterations)
    {
        for ( int r : recs )
            assertTrue(skiplist.contains(r)) ;

        // Shared across test-lets
        SortedSet<Integer> x = new TreeSet<Integer>() ;
        for ( int v : recs )
            x.add(v) ;

        for ( int i = 0 ; i < numIterations ; i++ )
        {
            int lo = random.nextInt(recs.length) ;
            int hi = random.nextInt(recs.length) ;
            if ( lo > hi )
            {
                int t = lo ;
                lo = hi ;
                hi = t ;
            }
            // Does not consider nulls - assumed to be part of functional testing.
            // Tweak lo and hi
            if ( lo != 0 && random.nextFloat() < 0.5 )
                lo-- ;  // Negatives confuse the int/record code.
            if ( random.nextFloat() < 0.5 )
                hi++ ;

            List<Integer> slice = new ArrayList<Integer>(recs.length) ;
            for ( int r : skiplist.records(lo, hi) )
                slice.add(r) ;

            List<Integer> expected = new ArrayList<Integer>(recs.length) ;
            for ( Integer ii : x.subSet(lo, hi) )
                expected.add(ii) ;
            assertEquals(format("(%d,%d)",lo, hi), expected, slice) ;
        }
    }

}
