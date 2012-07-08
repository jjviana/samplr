
package com.logicstyle.samplr;

import java.util.List;

/**
 * Defines the common interface for all result processors.
 *
 * @author juliano
 */
public interface  ResultsProcessor {
    
    /**
     * Processes the provided request results.
     * Once this method returns it is assumed that the request processor has kept no references 
     * to the request or the resultFiles.
     * @param request - the request related to this result.
     * @param resultFiles  - the list of result files available from this request.
     */
    
    public void processResult(Request request,List<ResultFile> resultFiles);
    
    
    
}
