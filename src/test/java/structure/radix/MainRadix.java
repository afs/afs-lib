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
import static structure.radix.Str.str ;

import java.util.Arrays ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.logging.LogCtl ;

public class MainRadix
{
    // isleaf => isValue 
    // Store (k,v) by having a V slot in leaf or hasValue position 
    
    static byte[] key0 = { } ;
    
    static byte[] key1 = { 2 , 4 , 6 , 8  } ;
    
    static byte[] key2 = { 2 , 4 , 6 , 10  } ;
    // Insert - shorter key
    static byte[] key3 = { 2 , 4 } ;
    
    // Insert - existing leaf
    static byte[] key4 = { 2 , 4 , 6,  8 , 10 } ;

    // Insert - partial prefix match.
    static byte[] key5 = { 2 , 4 , 3 , 1 } ;
    
    // Insert - new root
    static byte[] key6 = { 0 , 1 , 2 , 3 , 4  } ;
    
    static boolean print = true ;

    public static void main(String ...argv)
    { 
        LogCtl.setLogging();
        //runJUnit() ;
        RadixTreeImpl.checking = true ;
        RadixTreeImpl.logging = false ;
        LogCtl.enable("structure.radix") ;
        print = false ;
        
        if ( false )
        {
            // To become more iterator tests. 
            // cases
            //  Diverge before at branch
            //  Diverge between at branch
            //  Diverge after at branch
            
            // Test key exactly on finish of iterator.
            
            byte[] k1 = { 0 , 2 } ;
            byte[] k2 = { 0 , 3 } ;

            byte[] k3 = { 0 , 2 , 4 , 7  } ;
            
            byte[] k4 = { 0 , 2 , 4 , 5 } ;
            byte[] k5 = { 0 , 1 } ; 
            byte[] k6 = { 7 } ; 
            
            byte[] keyStart1 = { 0 , 2 } ;
            byte[] keyStart2 = { 0 , 1 } ;
            byte[] keyStart3 = { 0 , 2 , 4} ;
            byte[] keyStart4 = { 0 , 2 , 4, 5 } ;
            byte[] keyStart5 = { 0 , 2 , 4, 6 } ;
            byte[] keyStart6 = { 0 , 2 , 4, 7 } ;
            
            byte[] keyStart9 = { 99 } ;
            
            byte[] keyFinish = null ;
            //RadixTree t = tree(k1,k2,k3,k6) ;
            RadixTree t = tree(k1,k6) ;
            t.print() ;
            t.printLeaves() ;

            //RadixTree.logging = true ;
            iterator(t, keyStart1, keyFinish) ;
            iterator(t, keyStart2, keyFinish) ;
            iterator(t, keyStart3, keyFinish) ;
            iterator(t, keyStart4, keyFinish) ;
            iterator(t, keyStart5, keyFinish) ;
            iterator(t, keyStart6, keyFinish) ;
            iterator(t, keyStart9, keyFinish) ;
        }
        
        
        if ( true )
        {    
            byte[] keyStart = key1 ;
            byte[] keyFinish = { 2 , 4 , 6 , 9 } ;
            RadixTree t = tree(key1, key2, key3, key4, key5, key6) ;
            t.print() ;
            t.printLeaves() ;
            RadixTreeImpl.logging = true ;
            System.out.println("** Iterator("+Str.str(keyStart)+", "+Str.str(keyFinish)+")") ;
            
            iterator(t, keyStart, keyFinish) ;
            testIter(t, keyStart, keyFinish, key1, key4) ;
        }

        System.exit(0) ;
        
        if ( true )
        {
            byte[] k1 = { 1 , 2 , 3 } ;
            byte[] k2 = { 1 , 2 } ;
            byte[] k3 = { 1 } ;
            
            byte[][] insertOrder = { k2, k1, k3 } ;
            byte[][] deleteOrder = { k2, k1, k3 } ;

            RadixTree t = tree(insertOrder) ;
            t.print() ;
            t.check() ;

            LogCtl.enable("structure.radix") ;
            for ( byte[] d : deleteOrder )
            {
                t.delete(d) ;
                t.print() ;
                t.check() ;
            }
            System.exit(0) ;

            // Unrolled.
            t.delete(deleteOrder[0]) ;
            t.print() ;
            t.check() ;
            
            t.delete(deleteOrder[1]) ;
            t.check() ;
            t.print() ;
            
            t.delete(deleteOrder[2]) ;
            t.print() ;
            t.check() ;
            System.exit(0) ;
        }
        
        if ( false )
        {   
            byte[][] data1 = { {0x01}, {0x09}, {0x02}, {}, {0x04,0x12}, {0x04,0x0E}, {0x01,0x11,0x11,0x00,0x05,0x05}, {0x0A,0x07,0x01,0x02,0x00,0x06}, {0x0F}, {0x0F,0x0B,0x05,0x00,0x0B} } ;
            byte[][] data2 = { {}, {0x01,0x11,0x11,0x00,0x05,0x05}, {0x04,0x12}, {0x0F,0x0B,0x05,0x00,0x0B}, {0x02}, {0x0A,0x07,0x01,0x02,0x00,0x06}, {0x01}, {0x09}, {0x04,0x0E}, {0x0F} } ;

            RadixTree tree = tree() ;
            RadixRun.execInsert(tree, Arrays.asList(data1), false) ;
            RadixRun.execDelete(tree, Arrays.asList(data2), true) ;
            System.exit(0) ;
        }
        
        RadixTree tree = tree(key0, key1, key2, key3, key4, key5) ;
        tree.printLeaves() ;
        System.exit(0) ;
        
        System.out.println() ;
    }
    
    private static void iterator(RadixTree t, byte[] keyStart, byte[] keyFinish)
    {
        System.out.println("** Iterator("+Str.str(keyStart)+", "+Str.str(keyFinish)+")") ;
        Iterator<RadixEntry> iter = t.iterator(keyStart, keyFinish) ;
        for ( ; iter.hasNext() ; )
            System.out.println(iter.next()) ;
        System.out.println() ;
    }

    static private RadixTree tree(byte[] ... keys)
    {
        return tree(RadixTreeFactory.create(), keys) ;
    }
    
    private static void testIter(RadixTree t, byte[] keyStart, byte[] keyFinish, byte[]...results)
    {
        Iterator<RadixEntry> iter = t.iterator(keyStart, keyFinish) ;
        for ( int i = 0 ;  i < results.length ; i++ )
        {
            if ( ! iter.hasNext() ) throw new RuntimeException("Iterator ran out") ;
            byte[] k = iter.next().key ;
            if ( 0 != Bytes.compare(results[i], k) ) 
                throw new RuntimeException("idx = "+i+" Arrays differ: "+Str.str(results[i])+" : "+Str.str(k)) ;
        }
        if ( iter.hasNext() )
        {
            System.out.println("-- Iterator still has elements") ;
            Iterator<RadixEntry> iter2 = t.iterator(keyStart, keyFinish) ;
            for ( ; iter2.hasNext() ; )
                System.out.println(iter2.next()) ;
            System.out.println("----") ;
            for ( byte[] r : results ) 
                System.out.println(str(r)) ;
            System.out.println("----") ;
            
        }
        if ( iter.hasNext() ) throw new RuntimeException("Iterator still has elements") ;
    }
    
    static private RadixTree tree(RadixTree t, byte[] ... keys)
    {
        for ( int i = 0 ; i < keys.length ; i++ )
        {
            byte[] k = keys[i] ;
            byte[] v = valFromKey(k) ;
            if (print) System.out.println("Build: "+Str.str(k)) ;
            t.insert(k,v) ;
            if (print) t.print() ;
            t.check() ;
            if (print) System.out.println() ;
        }
        return t ;
    }
    

    public static byte[] valFromKey(byte[] k)
    {
        byte[] v = new byte[k.length] ;
        for ( int j = 0 ; j < v.length ; j++ )
            v[j] = (byte)(k[j]+10) ;
        return v ;
    }



//    static private void runJUnit()
//    {
//        JUnitCore runner = new org.junit.runner.JUnitCore() ;
//        runner.addListener(new TextListener2(System.out)) ;
//        //TestRadix.beforeClass() ;
//        Result result = runner.run(TestRadix.class) ;
//        //TestRadix.afterClass() ;
//        System.exit(0) ;
//    }
}
