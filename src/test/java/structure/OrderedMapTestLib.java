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

package structure ;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.test.ExecGenerator ;
import org.apache.jena.atlas.test.Gen ;
import org.apache.jena.atlas.test.RepeatExecution ;
import structure.OrderedMap ;

public class OrderedMapTestLib {

    public static void randTests(OrderedMapTestFactory factory, int maxValue, int maxNumKeys, int iterations,
                                 boolean showProgess) {
        ExecGenerator execGenerator = new OrderedMapTest(factory, maxValue, maxNumKeys) ;
        RepeatExecution.repeatExecutions(execGenerator, iterations, showProgess) ;
    }

    public static void randTest(OrderedMapTestFactory factory, int maxValue, int numKeys) {
        // if ( numKeys >= 3000 )
        // System.err.printf("Warning: too many keys\n") ;
        //
        int[] r1 = Gen.rand(numKeys, 0, maxValue) ;
        int[] r2 = Gen.permute(r1) ;
        try {
            OrderedMap<Integer, Integer> sIndx = factory.create(r1) ;
            check(sIndx, r1) ;
            delete(sIndx, r2) ;
            check(sIndx) ;
        }
        catch (RuntimeException ex) {
            System.err.printf("int[] r1 = {%s} ;\n", Gen.strings(r1)) ;
            System.err.printf("int[] r2 = {%s}; \n", Gen.strings(r2)) ;
            throw ex ;
        }
        catch (Error ex) {
            System.err.printf("int[] r1 = {%s} ;\n", Gen.strings(r1)) ;
            System.err.printf("int[] r2 = {%s}; \n", Gen.strings(r2)) ;
            throw ex ;
        }

    }

    public static OrderedMap<Integer, Integer> delete(OrderedMap<Integer, Integer> sIndx, int... recs) {
        for ( int i : recs )
            sIndx.remove(i) ;
        return sIndx ;
    }

    public static void check(OrderedMap<Integer, Integer> sIndx, int... recs) {
        sIndx.check() ;
        for ( int i : recs )
            assertTrue(sIndx.contains(i)) ;

        List<Integer> x = Iter.toList(sIndx.iteratorKeys()) ;
        List<Integer> y = sIndx.keys() ;
        assertEquals(x, y) ;

        SortedSet<Integer> r = new TreeSet<Integer>() ;

        // -- Sort to list.
        for ( int i : recs )
            r.add(i) ;

        List<Integer> z = new ArrayList<Integer>() ;
        for ( int i : r )
            z.add(i) ;
        // --

        if ( !z.equals(x) ) {
            System.out.println("About to crash") ;
        }

        assertEquals(z, x) ;

        if ( r.size() > 0 ) {
            Integer min = z.get(0) ;
            Integer max = z.get(r.size() - 1) ;
            assertEquals(min, sIndx.min()) ;
            assertEquals(max, sIndx.max()) ;
        }
    }

    public static void check(Iterator<Integer> iter, int... recs) {
        for ( int i : recs ) {
            assertTrue("Iterator shorter than test answers", iter.hasNext()) ;
            int j = iter.next() ;
            assertEquals(i, j) ;
        }
        assertFalse("Iterator longer than test answers", iter.hasNext()) ;
    }

    public static void size(OrderedMap<Integer, Integer> sIdx, long size) {
        long x = sIdx.count() ;
        long x2 = sIdx.size() ;
        assertEquals(size, x) ;
        assertEquals(size, x2) ;
        if ( size == 0 )
            assertTrue(sIdx.isEmpty()) ;
        else
            assertFalse(sIdx.isEmpty()) ;
    }

}
