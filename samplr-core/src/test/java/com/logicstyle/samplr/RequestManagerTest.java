package com.logicstyle.samplr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openide.util.Exceptions;



/**
 * Unit test for simple App.
 */


public class RequestManagerTest 
{
    
    class TestRequest extends Request implements RequestInfoProvider {

        public List<ResultFile> getRequestFiles() {
           
            ResultFile rs=new ResultFile();
            rs.setName("test-file.txt");
            rs.setContent(new ByteArrayInputStream("This is a test string\n".getBytes()));
            return Collections.singletonList(rs);
            
            
        }
        
    }
    
    
    @BeforeClass
    public static void initOutputDirectory() {
        
         File testOutputDir=new File("target/test-output");
        if(testOutputDir.exists())
           deleteDir(testOutputDir);
        
        //testOutputDir.mkdir();
            
    }
    
    public RequestManager  initRequestManager(long requestTimeout,long samplingThreshold) {
        
        
        
      
       
        RequestManager requestManager=new RequestManager()
                .withRequestProcessor(new ThreadSamplingRequestProcessor()
                                           .withRequestLengthSamplingThreshold(samplingThreshold))
                .withRequestProcessor(new RequestRecorderRequestProcessor())
                .withResultsProcessor(new FileResultsArchiver()
                                          .withOutputDirectory(new File("target/test-output")))
                .withRequestTimeout(requestTimeout);
                
           
        requestManager.start();
        return requestManager;
    }
   
    private static boolean deleteDir(File dir) {
        
        if(dir.isDirectory()) {
            String[] children=dir.list();
            for(int i=0;i<children.length;i++) {
                if(!children[i].equals(".") && !children[i].equals("..")) {
                    boolean success=deleteDir(new File(dir,children[i]));
                    if(!success)
                        return false;
                }
            }
            
        }
        
        return dir.delete();
        
        
    }
    @Test
    public void testRequestManager()
    {
        RequestManager requestManager=initRequestManager(0,5000);
        
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
        
        requestManager.requestFinished(testRequest);
        requestManager.shutdown();
        requestManager.awaitTermination(30000);
        File resultsFile=new File("target/test-output/"+testRequest.getId()+"/request-sampling.nps");
        assertTrue(resultsFile.exists());
        
        
        
    }
    
    @Test
    public void testRequestTimeout() {
        
        RequestManager requestManager=initRequestManager(15000,5000);
        
        TestProcessingThread testThread=new TestProcessingThread(5000); // no way this will end in 15 seconds
        
        Request testRequest=new TestRequest();
        testRequest.setThreadId(testThread.getId());
        
        testThread.start();
        
        requestManager.requestStarting(testRequest);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
           
        }
        
        requestManager.shutdown();
        RequestManager.Status status=requestManager.awaitTermination(30000);
        
        testThread.stopRunning();
        
        assertEquals(RequestManager.Status.STOPPED,status);
        
        
        File resultsFile=new File("target/test-output/"+testRequest.getId()+"/request-sampling.nps");
        assertTrue(resultsFile.exists());
        
        
    }
    
    @Test
    public void testRequestManagerSomeProcessorsDoNotReturnResults() throws Exception {
        
        RequestManager requestManager=initRequestManager(150000,100000); 
        
       
        
        TestProcessingThread testThread=new TestProcessingThread(2); // this will end much sooner
        
        Request testRequest=new TestRequest();
        testRequest.setThreadId(testThread.getId());
        
        testThread.start();
        
        requestManager.requestStarting(testRequest);
         try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
           
        }
         requestManager.requestFinished(testRequest);
         
        
        requestManager.shutdown();
        RequestManager.Status status=requestManager.awaitTermination(30000);
        
        testThread.stopRunning();
        
        assertEquals(RequestManager.Status.STOPPED,status);
        
        File resultsFile=new File("target/test-output/"+testRequest.getId()+"/request.txt");
        assertTrue(resultsFile.exists());
        resultsFile=new File("target/test-output/"+testRequest.getId()+"/request-sampling.nps");
        assertFalse(resultsFile.exists());
    }
    
    @Test
    public void testRequestManagerRequestTimeResultsFilter() throws Exception {
        RequestManager requestManager=initRequestManager(0,5000);
         for (RequestProcessor rp: requestManager.getRequestProcessors()) {
            rp.setResultsFilter(new RequestTimeResultsFilter(600000l));
            
        }
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
        
        requestManager.requestFinished(testRequest);
        requestManager.shutdown();
        requestManager.awaitTermination(30000);
        File outputDir=new File("target/test-output/"+testRequest.getId());
        assertFalse(outputDir.exists());
        
    }
}
