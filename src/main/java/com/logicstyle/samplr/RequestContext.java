
 
package com.logicstyle.samplr;

import java.util.List;

/**
 * Allows RequestProcessots to notify the RequestManager that they are have stopped
 * recording measurements for a specific request. The results list can be null or empty indicating that
 * this processor has no measurements for the specified request.
 * @author juliano
 */
public interface RequestContext {
    
    
    public Request getRequest();
    
    public void measurementFinished(Request request,RequestProcessor processor,List<ResultFile> results);
    
}
