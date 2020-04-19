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

import org.apache.jena.atlas.iterator.Iter ;

public class RLib
{
    // Collect all str(X) into a single class
    // See also ByteBufferLib
    // Bytes, Chars, StrUtils.
        
    private static String str(RadixTree rt)
    {
        Iterator<String> iter = Iter.map(rt.iterator(), (item)->"["+item+"]") ;
        return Iter.asString(iter, ", ") ;
    }

    // When right , move to ByteBufferLib.
    /** Copy from a byte buffer */
    final public static byte[] bb2array(ByteBuffer bb, int start, int finish)
    {
        byte[] b = new byte[finish-start] ;
        bb2array(bb, start, finish, b) ;
        return b ;
    }
    
    private static void bb2array(ByteBuffer bb, int start, int finish, byte[] b)
    {
        for ( int j = 0 , i = start; i < finish ; j++,i++ )
            b[j] = bb.get(i) ;
    }
    
}

