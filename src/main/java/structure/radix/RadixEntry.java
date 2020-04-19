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


public class RadixEntry
{
    public final byte[] key ;
    public final byte[] value ;
    
    public RadixEntry(byte[] key, byte[] value)
    {
        super() ;
        this.key = key ;
        this.value = value ;
    }
    
    @Override
    public String toString()
    {
        if ( value == null )
            return "["+Str.str(key)+"]" ;
        else
            return "["+Str.str(key)+" :: "+Str.str(value)+"]" ;
    }
}

