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

/** str(...) for any kind of object */

public class Str
{

    public static String strToPosn(ByteBuffer bytes)
    {
        return str(bytes,0, bytes.position()) ;
    }
    
    public static String strPosnLimit(ByteBuffer bytes)
    {
        return str(bytes, bytes.position(), bytes.limit()) ;
    }
    
    public static String str(ByteBuffer bytes) { return strToPosn(bytes) ; }

    public static String str(ByteBuffer bytes, int start, int finish)
    {
        // Do not use relative operations.
        StringBuilder sb = new StringBuilder() ;
        sb.append(String.format("[pos=%d lim=%d cap=%d] ", bytes.position(), bytes.limit(), bytes.capacity())) ;
        char sep = 0 ;
        for ( int i = start ; i < finish ; i++ )
        {
            byte b = bytes.get(i) ;
            if ( sep != 0 )
                sb.append(sep) ;
            else
                sep = ' ' ;
            str(sb, b) ;
        }
        return sb.toString() ;
    }

    public static String str(byte[] bytes)
    { return str(bytes, "") ; }

    public static String str(byte[] bytes, String sep)
    {
        if ( bytes == null )
            return "" ;
        StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for ( byte b : bytes )
        {
            if ( ! first )
                sb.append(sep) ;
            first = false ;
            str(sb, b) ;
        }
        return sb.toString() ;
    }
    
    private static void str(StringBuilder sb, byte x)
    {
        sb.append(String.format("%02X", x)) ;
    }

}

