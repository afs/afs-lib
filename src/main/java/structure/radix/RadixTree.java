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

import java.nio.ByteBuffer ;
import java.util.Iterator ;

public interface RadixTree
{
    /** Test whether the key is in the tree */
    public boolean contains(byte[] key) ;

    /** Find by key.
     *  Return some default if not found. 
     */
    public byte[] find(byte[] key, byte[] dft) ;

    public boolean insert(byte[] key, byte[] value) ;

    /** Delete - return true if the tree changed (i.e the key was present and so was removed) */
    public boolean delete(byte[] key) ;

    public void print() ;

    public void clear() ;

    public ByteBuffer min() ;

    public ByteBuffer min(byte[] b) ;

    public ByteBuffer max() ;

    public ByteBuffer max(byte[] b) ;

    public long size() ;

    public boolean isEmpty() ;

    public Iterator<RadixEntry> iterator() ;

    public Iterator<RadixEntry> iterator(byte[] start, byte[] finish) ;

    public void printLeaves() ;

    public void check() ;

}

