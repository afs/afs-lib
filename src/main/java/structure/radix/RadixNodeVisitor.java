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

// Or:
//public interface Visitor<T, R> {
//    public void visit(String key, RadixNode parent, RadixNode node);
//    public R getResult();
//}

public interface RadixNodeVisitor<R>
{
    public void before(RadixNode node) ;
    public void after(RadixNode node) ;
    public R result() ;
}
