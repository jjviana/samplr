
package com.logicstyle.samplr;

/**
 * Decides if requests should be sampled or not.
 * Called periodically for the ongoing requests.
 * @author juliano
 */
public interface MeasuringFilter {
    
    /**
     * Returns true if the specified request should be measured.
     * @param request The request about to start
     
     */
    public boolean shouldMeasure(Request request);
    
    
    
}
