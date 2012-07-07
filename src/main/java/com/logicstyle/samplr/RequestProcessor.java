
package com.logicstyle.samplr;

import java.util.List;

/**
 * General interface for request processors.
 * Request processors are notified about requests starting and stopping
 * and can contribute measurement results at the end of a request processing cycle.
 * @author juliano
 */
public  abstract class RequestProcessor<T extends RequestProcessor> {
    
    private MeasuringCriteria measuringCriteria;
    
    public   boolean shouldMeasure(Request request) {
        return measuringCriteria== null || measuringCriteria.shouldMeasure(request);
    }
    
    public  abstract void startMeasuring(RequestContext context);
    
    public  abstract void stopMeasuring(RequestContext request);

    public MeasuringCriteria getMeasuringCriteria() {
        return measuringCriteria;
    }

    public void setMeasuringCriteria(MeasuringCriteria measuringCriteria) {
        this.measuringCriteria = measuringCriteria;
    }
    
    
    public T withMeasuringCriteria(MeasuringCriteria measuringCriteria) {
        
        setMeasuringCriteria(measuringCriteria);
        
        return (T) this;
    }
    
    
}
