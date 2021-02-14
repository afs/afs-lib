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

package structure.ttree;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.test.Gen;
import structure.OrderedSet;
import structure.OrderedSetTestLib;

public class MainTTree
{

    public static void ttree()
        {
            // Insertion element to log.  Set -1 for all logged, set very large for no logging
//            {
//                String a[] = { "perf", "50", "500" } ;
//                test.TTreeRun.main(a) ;
//                System.exit(0) ; 
//            }
                
            TTree.Logging = true ;
            
            int[] r1 = {15, 99, 59, 94, 35, 45, 66} ;
            int[] r2 = {66, 15, 45, 94, 99, 35, 59}; 
    //        int[] r1 = {8, 133, 22, 83, 74, 39, 72, 81, 91, 4, 26, 56, 0, 68} ;
    //        int[] r2 = {0, 83, 56, 133, 74, 72, 26, 39, 22, 68, 8, 91, 81, 4}; 
            
            try {
                OrderedSet<Integer> sIndx = new TTree<Integer>(4,3) ;
                for ( int i : r1 )
                    sIndx.add(i) ;
                OrderedSetTestLib.check(sIndx, r1) ;   // These throw java.lang.AssertionError. - catch that
                OrderedSetTestLib.delete(sIndx, r2) ;
                
                sIndx.output(IndentedWriter.stdout) ;
                
                OrderedSetTestLib.check(sIndx) ;
            } catch (RuntimeException ex)
            {
                System.err.printf("int[] r1 = {%s} ;\n", Gen.strings(r1)) ;
                System.err.printf("int[] r2 = {%s}; \n", Gen.strings(r2)) ;
                throw ex ;
            }
            catch (AssertionError ex)
            {
                System.err.printf("int[] r1 = {%s} ;\n", Gen.strings(r1)) ;
                System.err.printf("int[] r2 = {%s}; \n", Gen.strings(r2)) ;
                throw ex ;
            }
            System.exit(0) ;
            
        }

}
