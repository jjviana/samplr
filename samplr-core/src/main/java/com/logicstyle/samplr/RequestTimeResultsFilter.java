/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.util.List;

/**
 *
 * @author oracle
 */
public class RequestTimeResultsFilter implements ResultsFilter  {

    private long outputThreshold;

    public long getOutputThreshold() {
        return outputThreshold;
    }

    public void setOutputThreshold(long outputThreshold) {
        this.outputThreshold = outputThreshold;
    }

    public RequestTimeResultsFilter(long outputThreshold) {
        this.outputThreshold = outputThreshold;
    }
    
    
    
    public boolean shouldRecord(Request request, List<ResultFile> results) {
        
        return (request.getEndTime()-request.getStartTime())>=outputThreshold;
    }
    
}
