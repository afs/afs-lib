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

import static structure.radix.Str.str;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class RadixTreeLogger implements RadixTree
{
    private static Logger defaultLogger = LoggerFactory.getLogger(RadixTree.class) ;
    private Logger log = defaultLogger ;
    private RadixTree tree ;
    private String label ;

    public RadixTreeLogger(String label, RadixTree tree)
    {
        this.label = label ;
        this.tree = tree ;
    }

    @Override
    public boolean contains(byte[] key)
    {
        boolean b = tree.contains(key) ;
        info("contains(%s) => %s", str(key), b) ;
        return b ;
    }

    @Override
    public byte[] find(byte[] key, byte[] dft)
    {
        byte[] b = tree.find(key, dft) ;
        info("find(%s, %s) => %s", str(key), str(dft), str(b)) ;
        return b ;
    }

    @Override
    public boolean insert(byte[] key, byte[] value)
    {
        boolean b = tree.insert(key, value) ;
        info("insert(%s, %s) => %s", str(key), str(value), b) ;
        return b ;
    }

    @Override
    public boolean delete(byte[] key)
    {
        boolean b = tree.delete(key) ;
        info("delete(%s) => %s", str(key), b) ;
        return b ;
    }

    @Override
    public void print()
    {
        tree.print() ;
    }

    @Override
    public void clear()
    {
        tree.clear() ;
        info("clear()") ;
    }

    @Override
    public ByteBuffer min()
    {
        ByteBuffer bb =  tree.min();
        info("min() => %s", str(bb)) ;
        return bb ;
    }

    @Override
    public ByteBuffer min(byte[] b)
    {
        ByteBuffer bb =  tree.min(b);
        info("min(%s) => %s", str(b), str(bb)) ;
        return bb ;
    }

    @Override
    public ByteBuffer max()
    {
        ByteBuffer bb =  tree.max();
        info("min() => %s", str(bb)) ;
        return bb ;
    }

    @Override
    public ByteBuffer max(byte[] b)
    {
        ByteBuffer bb =  tree.max(b);
        info("min(%s) => %s", str(b), str(bb)) ;
        return bb ;
    }

    @Override
    public long size()
    {
        long x = tree.size() ;
        info("size() => %d", x) ;
        return x ;
    }

    @Override
    public boolean isEmpty()
    {
        boolean b = tree.isEmpty() ;
        info("isEmpty() => %s", b) ;
        return b ;
    }

    @Override
    public Iterator<RadixEntry> iterator()
    {
        info("iterator()") ;
        return tree.iterator() ;
    }

    @Override
    public Iterator<RadixEntry> iterator(byte[] start, byte[] finish)
    {
        info("iterator(%s, %s)", str(start), str(finish)) ;
        return tree.iterator(start, finish) ;
    }

    @Override
    public void printLeaves()
    {
        tree.printLeaves() ;
    }

    @Override
    public void check()
    {
        tree.check() ;
    }
    
    private void info(String string, Object ... args)
    {
        if ( ! log.isInfoEnabled() )
            return ;
        String s = String.format(string, args) ;
        if ( label != null )
            s = label+": "+s ;
        log.info(s) ; 
    }
}

