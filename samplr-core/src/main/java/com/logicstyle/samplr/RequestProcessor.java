
package com.logicstyle.samplr;

import java.util.List;

/**
 * General interface for request processors.
 * Request processors are notified about requests starting and stopping
 * and can contribute measurement results at the end of a request processing cycle.
 * @author juliano
 */
public  abstract class RequestProcessor<T extends RequestProcessor> {
    
    private MeasuringFilter measuringFilter;
    
    private ResultsFilter resultsFilter;
    
    
    public   boolean shouldMeasure(Request request) {
        return measuringFilter== null || measuringFilter.shouldMeasure(request);
    }
    
    public  abstract void startMeasuring(RequestContext context);
    
    public  abstract void stopMeasuring(RequestContext request);

    public MeasuringFilter getMeasuringFilter() {
        return measuringFilter;
    }

    public void setMeasuringFilter(MeasuringFilter measuringCriteria) {
        this.measuringFilter = measuringCriteria;
    }
    
    
    public T withMeasuringFilter(MeasuringFilter measuringCriteria) {
        
        setMeasuringFilter(measuringCriteria);
        
        return (T) this;
    }

    public ResultsFilter getResultsFilter() {
        return resultsFilter;
    }

    public void setResultsFilter(ResultsFilter resultsFilter) {
        this.resultsFilter = resultsFilter;
    }
    
    public T withResultsFilter(ResultsFilter rf) {
        setResultsFilter(rf);
        return (T)this;
    }

    boolean shouldRecord(Request request,List<ResultFile> results) {
        return (resultsFilter==null || resultsFilter.shouldRecord(request, results));
    }
    
}
