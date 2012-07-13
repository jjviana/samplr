/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * This request processor records the request parameters.
 * @author juliano
 */
public class RequestRecorderRequestProcessor extends RequestProcessor<RequestRecorderRequestProcessor>{

    @Override
    public void startMeasuring(RequestContext context) {
        
        
        
    }

    @Override
    public void stopMeasuring(RequestContext context) {
       
        Request request=context.getRequest();
        
        ByteArrayInputStream in = new ByteArrayInputStream(request.toString().getBytes());
        
        ResultFile rf=new ResultFile();
        rf.setName("request.txt");
        rf.setContent(in);
        
        List<ResultFile> results=new ArrayList<ResultFile>();
        results.add(rf);
        
        if(request instanceof RequestInfoProvider) {
            results.addAll(((RequestInfoProvider)request).getRequestFiles());
            
        }
        
        context.measurementFinished(request, this, results);
        
        
    }
    
}
