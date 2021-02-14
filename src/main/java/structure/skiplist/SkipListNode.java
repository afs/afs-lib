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

import static java.lang.String.format ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;

final
public class SkipListNode <R extends Comparable<? super R>> implements Printable
{
    static int counter = 1 ;
    int id = (counter++) ;
    
    R record ;
    // Arrays are a bit yukky in Java - they are objects+length so the overhead is 3*4 bytes (32 bit Java).
    // Alternative is link lists across and down. 
    
    SkipListNode<R> forward[] ;
    
    @SuppressWarnings("unchecked")
    SkipListNode(R record, int len)
    {
        this.record = record ;
        //forward = new ArrayList<SkipListNode<R>>(len) ;
        forward = (SkipListNode<R>[])new SkipListNode<?>[len] ;
    }
    
    SkipListNode<R> get(int i)
    { return forward[i] ; }
    
//    void set(int i, SkipListNode<?> v)
//    { forward[i] = v ; }
    
    public String debug()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        this.outputFull(buff) ;
        return buff.toString() ;
    }
    
    @Override
    public String toString() { return debug() ; }
    
    @Override
    public void output(IndentedWriter out)
    {
        out.print(record.toString()) ;
    }

    public void outputFull(IndentedWriter out)
    {
        out.print(format("[ id=%-2d rec=%-4s {", id, record)) ;
        boolean first = true ;
        for ( int i = 0 ; i < forward.length ; i++ )
        {
            if ( ! first ) out.print(" ") ;
            first = false ;
            SkipListNode<R> n = get(i) ;
            out.print(SkipList.label(n)) ; 
        }
        out.print("} ]") ; 
    }
}
