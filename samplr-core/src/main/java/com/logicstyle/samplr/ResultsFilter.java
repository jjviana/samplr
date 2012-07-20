
package com.logicstyle.samplr;

import java.util.List;

/**
 * Defines the interface of a filter that can decide wether results for a specific request
 * should be output.
 * @author oracle
 */
public interface ResultsFilter {
    
    public boolean shouldRecord(Request request,List<ResultFile> results);
    
}
