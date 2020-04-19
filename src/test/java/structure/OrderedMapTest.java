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

package structure;

import static org.apache.jena.atlas.lib.RandomLib.random;
import org.apache.jena.atlas.test.ExecGenerator;

class OrderedMapTest implements ExecGenerator
{
    int maxNumKeys ;
    int maxValue ;
    OrderedMapTestFactory factory ;
    
    OrderedMapTest(OrderedMapTestFactory factory, int maxValue, int maxNumKeys)
    {
        if ( maxValue <= maxNumKeys )
            throw new IllegalArgumentException("SortedIndexTest: Max value less than number of keys") ;
        this.maxValue = maxValue ; 
        this.maxNumKeys = maxNumKeys ;
        this.factory = factory ;
    }
    
    @Override
    public void executeOneTest()
    {
        int numKeys = random.nextInt(maxNumKeys)+1 ;
        OrderedMapTestLib.randTest(factory, maxValue, numKeys) ;
    }
}
