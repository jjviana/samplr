
package com.logicstyle.samplr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class monitors existing requests and pass them to Samplr if
 * they become interesting.
 * @author ubuntu
 */
public class SamplrMonitor {
    
    private Samplr samplr;
    
    class SamplrMonitorThread extends Thread {
        // TODO
    }
    
    private Thread samplrMonitorThread;

   

    
    
    class RequestHolder {
        Request request;
        boolean isSampling;
        
        public RequestHolder(Request req) {
            request=req;
        }
    }
    
    private Map <Long,RequestHolder> ongoingRequests;
    
    private long monitoringInterval;
    
    private SamplingCriteria samplingCriteria;; 
    
    public SamplrMonitor(Samplr s) {
        samplr=s;
        ongoingRequests=new ConcurrentHashMap<Long,RequestHolder>();
        
        
       
        
    }
    
    public void requestStarted(Request request) {
        if(ongoingRequests.containsKey(request.getId()))
            return; // can never monitor the same request twice
        
        
        
            if(samplingCriteria.shouldMonitor(request))
                startMonitoring(request);
            
            
        
    }
    
    public void requestFinished(Request request) {
        
        // TODO
    }

    private void startMonitoring(Request request) {
        
        RequestHolder holder=new RequestHolder(request);
        
        ongoingRequests.put(request.getId(),holder);
        
        holder.isSampling=checkShouldSample(holder); // In case sampling for this case of request should begin immediately
        
        
        
        
    }
    
    private boolean checkShouldSample(RequestHolder request) {
        
        if(!request.isSampling && !samplingCriteria.shouldSample(request.request)) {
            startSampling(request);
            return true;
        }
        
        return false;
        
        
    }
    
     private void startSampling(RequestHolder request) {
        
         samplr.startSampling(request.request);
         
    }
}
