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

package structure.runner;

import static org.apache.jena.atlas.lib.RandomLib.random ;
import static org.apache.jena.atlas.test.Gen.permute ;
import static org.apache.jena.atlas.test.Gen.rand ;
import static org.apache.jena.atlas.test.Gen.strings ;
import static structure.skiplist.SkipListTestBase.create ;
import static structure.skiplist.SkipListTestBase.delete ;

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.test.ExecGenerator ;
import org.apache.jena.atlas.test.RepeatExecution ;
import structure.avl.AVL ;
import structure.skiplist.SkipList ;
import structure.skiplist.SkipListTestBase ;

public abstract class SkipListRun
{
    static boolean showProgress = true ;
    
    static public void main(String...a)
    {
        List<String> args = new ArrayList<String>(Arrays.asList(a)) ;
        if ( args.size() == 0 )
        {
            System.err.println("No subcommand") ;
            System.exit(1) ;
        }
        String subCmd = args.remove(0) ;
        if ( "test".equalsIgnoreCase(subCmd) )
            new Test().exec(args) ;
        else if ( "perf".equalsIgnoreCase(subCmd) )
            new Perf().exec(args) ;
        else
        {
            System.err.println("Unknown subcommand: "+subCmd) ;
            System.exit(1) ;
        }
    }
    
    public void exec(List<String> args)
    {
        args = processArgs(args) ;
        int numKeys = Integer.parseInt(args.get(0)) ;
        int iterations = Integer.parseInt(args.get(1)) ;
        
        int maxLevel = 20 ;
        if ( args.size() == 3 )
            maxLevel = Integer.parseInt(args.get(2)) ;
        exec(maxLevel ,numKeys, iterations) ;
    }        
    
    protected abstract void exec(int maxLevel, int numKeys, int iterations) ;
    

    // ---- Test
    public static class Test extends SkipListRun
    {
        @Override
        protected void exec(int maxLevel, int numKeys, int iterations)
        {
            SkipListTestBase.randTests(maxLevel, 10*numKeys, numKeys, iterations, showProgress) ;
        }
    }

    // ---- Performance
    public static class Perf extends SkipListRun
    {
        @Override
        public void exec(List<String> args)
        {
            showProgress = false ;
            SkipList.Checking = false ;
            SkipList.Logging = false ;
            super.exec(args) ;
        }

        @Override
        protected void exec(int maxLevel, int numKeys, int iterations)
        {
            RandomGen rand = new RandomGen(100*numKeys, numKeys) ;
            RepeatExecution.repeatExecutions(rand, iterations, showProgress) ;
        }
    }
    
    List<String> processArgs(List<String> args)
    {
        LogCtl.setLog4j() ;
        int i = 0 ;
        while ( args.size()>0 )
        {
            if ( !args.get(0).startsWith("-") )
                break ;

            String a = args.remove(0) ;
            if ( a.startsWith("--") )
                a = a.substring(2) ;
            else
                a = a.substring(1) ;

            if ( a.equals("h") || a.equals("help") )
            {
                usage(System.out) ;
                System.exit(0) ;
            }
            else if ( a.equalsIgnoreCase("check") )
            {
                AVL.Checking = true ;
            }
            else if ( a.equalsIgnoreCase("display") )
            {
                showProgress = ! showProgress ;
            }
            else   
            {
                System.err.println("Unknown argument: "+a) ;
                System.exit(1) ;
            }
        }
        
        if ( args.size() != 2 && args.size() != 3 )
        {
            usage(System.err) ;
            System.exit(1) ;
        }
        return args ;
    }
    
    public static void usage(PrintStream printStream)
    {
        printStream.println("Usage: OPTIONS NumKeys Iterations [max level]") ;
        printStream.println("Options:") ;
        printStream.println("   --display") ;
        printStream.println("   --check") ;
    }
    
 
    static class RandomGen implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;

        RandomGen(int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("SkipList: Max value less than number of keys") ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }

        @Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            perfTest(maxValue, numKeys) ;
        }
    }

    
    /* Performance test : print the keys if there was a problem */ 
    
    static void perfTest(int maxValue, int numKeys)
    {
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1) ;
        try {
            SkipList<Integer> skiplist = create(keys1);
            delete(skiplist, keys2) ;
        } catch (RuntimeException ex)
        {
            //System.err.printf("int maxLevel=%d ;\n", maxLevel) ;
            System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
            System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
            throw ex ;
        }
    }
}
