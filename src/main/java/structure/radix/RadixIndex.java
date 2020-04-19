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

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.tdb.base.record.Record ;
import org.apache.jena.tdb.base.record.RecordFactory ;
import org.apache.jena.tdb.index.RangeIndex ;

public class RadixIndex implements RangeIndex
{
    private RadixTree radix = RadixTreeFactory.create() ;
    private RecordFactory recordFactory ;
    
    static int counter = 0 ;
    
    public RadixIndex(RecordFactory recordFactory)
    {
        //radix = new RadixTreeLogger(("Idx-"+counter++), radix) ; 
        this.recordFactory = recordFactory ;
    }
    
    @Override
    public Record find(Record record)
    {
        byte[] v = radix.find(record.getKey(), null) ;
        if ( v == null )
            return null ;
        
        return recordFactory.create(record.getKey(), v) ;
    }

    @Override
    public boolean contains(Record record)
    {
        return find(record) != null ;
    }

    @Override
    public boolean add(Record record)
    {
        return radix.insert(record.getKey(), record.getValue()) ;
    }

    @Override
    public boolean delete(Record record)
    {
        return radix.delete(record.getKey()) ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        return Iter.map(radix.iterator(null, null), (entry)->new Record(entry.key, entry.value)) ;
    }

    @Override
    public Iterator<Record> iterator(Record recordMin, Record recordMax)
    {
        byte[] s = (recordMin==null)?null:recordMin.getKey() ;
        byte[] f = (recordMax==null)?null:recordMax.getKey() ;
        return Iter.map(radix.iterator(s,f), (entry)->new Record(entry.key, entry.value)) ;
    }

    @Override
    public Record minKey()
    {
        // Provide the byte buffer to the min() function.
        // Avoid copy.
        Record r = recordFactory.create() ;
        radix.min(r.getKey()) ;
        return r ;
    }

    @Override
    public Record maxKey()
    {
        // Provide the byte buffer to the max() function.
        // Avoid copy.
        Record r = recordFactory.create() ;
        radix.max(r.getKey()) ;
        return r ;
    }

    @Override
    public RecordFactory getRecordFactory()
    {
        return recordFactory ;
    }

    @Override
    public void close()
    {}

    @Override
    public boolean isEmpty()
    {
        return radix.isEmpty() ;
    }

    @Override
    public void clear()
    {
        radix.clear() ;
    }

    @Override
    public void check()
    {
        radix.check() ;
    }

    @Override
    public long size()
    {
        return radix.size() ;
    }

    @Override
    public void sync()
    {}
    
}
