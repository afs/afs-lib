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

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.test.ExecGenerator ;
import org.apache.jena.atlas.test.RepeatExecution ;
import structure.OrderedSet ;
import structure.OrderedSetTestFactory ;
import structure.OrderedSetTestLib ;
import structure.avl.AVL ;

public abstract class AVLRun
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
        {
            showProgress = false ;
            new Perf().exec(args) ;
        }
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
        exec(numKeys, iterations) ;
    }        
    
    protected abstract void exec(int numKeys, int iterations) ;
    

    // ---- Test
    public static class Test extends AVLRun
    {
        @Override
        protected void exec(int numKeys, int iterations)
        {
            showProgress = true ;
            AVL.Checking = true ;
            AVL.Verbose = false ;
            AVL.Logging = false ;
            OrderedSetTestLib.randTests(factory, 10*numKeys, numKeys, iterations, showProgress) ;
        }
    }

    // ---- Performance
    public static class Perf extends AVLRun
    {
        @Override
        public void exec(List<String> args)
        {
            showProgress = false ;
            AVL.Checking = false ;
            AVL.Verbose = false ;
            AVL.Logging = false ;
            super.exec(args) ;
        }
        
        @Override
        protected void exec(int numKeys, int iterations)
        {
            RandomGen rand = new RandomGen(100*numKeys, numKeys) ;
            RepeatExecution.repeatExecutions(rand, iterations, showProgress) ;
        }
    }
    
    List<String> processArgs(List<String> args)
    {
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
        
        if ( args.size() != 2 )
        {
            usage(System.err) ;
            System.exit(1) ;
        }
        return args ;
    }
    
    public static void usage(PrintStream printStream)
    {
        printStream.println("Usage: OPTIONS NumKeys Iterations") ;
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
                throw new IllegalArgumentException("AVLRun: Max value less than number of keys") ;
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
    
    public static void perfTest(int maxValue, int numKeys)
    {
        if ( numKeys >= 3000 )
            System.err.printf("Warning: too many keys\n") ;
            
        int[] r1 = rand(numKeys, 0, maxValue) ;
        int[] r2 = permute(r1) ;
        try {
            OrderedSet<Integer> avl = factory.create(r1) ;
            OrderedSetTestLib.delete(avl, r2) ;
        } catch (RuntimeException ex)
        {
            System.err.printf("int[] r1 = {%s} ;\n", strings(r1)) ;
            System.err.printf("int[] r2 = {%s}; \n", strings(r2)) ;
            throw ex ;
        }
    }

    static OrderedSetTestFactory factory = new OrderedSetTestFactory()
    {

        @Override
        public OrderedSet<Integer> create(int ... recs)
        {
 
          AVL<Integer> avl = new AVL<Integer>() ;
          for ( int i : recs )
              avl.add(i) ;
          return avl ;
      }
    } ;
    

}
