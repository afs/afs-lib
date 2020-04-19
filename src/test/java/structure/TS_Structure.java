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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import structure.avl.TestAVL;
import structure.binary_search_tree.TestBST_Tree ;
import structure.exthash.TestExtHashMem;
import structure.radix.TestRadix ;
import structure.skiplist.TestSkipList;
import structure.ttree.TestTTree;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestBST_Tree.class
    
    
    , TestAVL.class
    , TestExtHashMem.class
    , TestSkipList.class
    , TestTTree.class
    , TestRadix.class
} )
public class TS_Structure
{

}
