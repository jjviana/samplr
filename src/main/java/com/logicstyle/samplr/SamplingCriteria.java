
package com.logicstyle.samplr;

/**
 * Decides if requests should be sampled or not.
 * Called periodically for the ongoing requests.
 * @author juliano
 */
public interface SamplingCriteria {
    
    /**
     * Returns true if the specified request should be monitored.
     * Monitored requests are periodically passed to the shouldSample method in order to
     * check if the request has become interesting enough to sample.
     * @param request The ongoing request, just starting or about to start
     
     */
    public boolean shouldMonitor(Request request);
    
    /**
     * Returns true if the request supplied should be sampled, false otherwise.
     * @param request the ongoing request upon which the decision should be made.
     * 
     */
    public boolean shouldSample(Request request);
    
}
