
package com.logicstyle.samplr;

import java.util.List;

/**
 * General interface for request processors.
 * Request processors are notified about requests starting and stopping
 * and can contribute measurement results at the end of a request processing cycle.
 * @author juliano
 */
public  abstract class RequestProcessor {
    
    private MeasuringCriteria measuringCriteria;
    
    public   boolean shouldMeasure(Request request) {
        return measuringCriteria.shouldMeasure(request);
    }
    
    public  abstract void startMeasuring(RequestContext context);
    
    public  abstract void stopMeasuring(RequestContext request);

    public MeasuringCriteria getMeasuringCriteria() {
        return measuringCriteria;
    }

    public void setMeasuringCriteria(MeasuringCriteria measuringCriteria) {
        this.measuringCriteria = measuringCriteria;
    }
    
    
    
}
