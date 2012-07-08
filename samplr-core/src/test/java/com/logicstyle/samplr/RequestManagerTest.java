package com.logicstyle.samplr;

import java.io.File;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.openide.util.Exceptions;



/**
 * Unit test for simple App.
 */


public class RequestManagerTest 
{


    private RequestManager requestManager;
    
    
    class TestProcessingThread extends Thread {
        private final int maxIterations;

        public TestProcessingThread(int maxIterations) {
            super("Test Request Processing Thread");
            this.maxIterations=maxIterations;
        }

        @Override
        public void run() {
            
            Random random=new Random();
            
            for(int i=0;i<maxIterations;i++) {
                int randomInt=random.nextInt(10);
                
                switch (randomInt) {
                    case 0:
                    case 1:
                    case 2:
                        methodA();
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        methodB();
                        break;
                    default:
                        methodC(100);
                        
                        
                }
            }
            
        }
        
        
        private void methodA() {
            
            Random random=new Random();
            for(int i=0;i<50000;i++) {
                System.err.println(random.nextInt());
            }
            
        }
        
        private void methodB() {
            
            Random random=new Random();
            int randomInt=random.nextInt();
            
            for(int j=1;j<randomInt/2;j++) {
                if(randomInt%j==0) {
                    System.err.println("Not prime!");
                    return;
                }
            }
            
            System.err.println("Prime!!!!!");
            
            
        }
        
        private void methodC(int maxDepth) {
            
            if(maxDepth==0) {
                System.err.println("Max depth reached!");
                return;
            }
            try {
                sleep(new Random().nextInt(300));
            } catch (InterruptedException ex) {
               
            }
            methodC(maxDepth-1);
           
        }
        
        
    }
    
    @Before
    public void init() {
        
        
        File testOutputDir=new File("target/test-output");
        testOutputDir.mkdir();
       
        requestManager=new RequestManager()
                .withRequestProcessor(new ThreadSamplingRequestProcessor()
                                           .withRequestLengthSamplingThreshold(5000))
                .withResultsProcessor(new FileResultsArchiver()
                                          .withOutputDirectory(new File("target/test-output")));
                
                
    }
   
    @Test
    public void testRequestManager()
    {
        
        TestProcessingThread testThread=new TestProcessingThread(5);
        
        Request testRequest=new Request();
        testRequest.setThreadId(testThread.getId());
        
        testThread.start();
        
        requestManager.requestStarting(testRequest);
        try {
            testThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        requestManager.requestStopping(testRequest);
        
        
        
        
    }
}
