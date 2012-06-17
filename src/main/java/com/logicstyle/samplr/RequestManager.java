package com.logicstyle.samplr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages requests, coordinating the work of Request Processors.
 *
 * @author juliano
 */
public class RequestManager {

    private static Logger logger = Logger.getLogger(RequestManager.class.getName());
    /**
     * Request timeout. Measurement will be interrupted after the request have
     * been processing for this amount of time (in milliseconds). Measurement
     * values will be recorded as if the request had finished.
     */
    private long requestTimeout;
    private long timeoutCheckInterval = 500;
    private List<RequestProcessor> requestProcessors;
    private ExecutorService recordingExecutor;
    private ScheduledExecutorService requestTimeoutExecutor;
    private Map<Request,RequestContext> currentRequests = new ConcurrentHashMap<Request, RequestContext>();
    private List<ResultsProcessor> resultsProcessors = new CopyOnWriteArrayList<ResultsProcessor>();

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public long getTimeoutCheckInterval() {
        return timeoutCheckInterval;
    }

    public void setTimeoutCheckInterval(long timeoutCheckInterval) {
        this.timeoutCheckInterval = timeoutCheckInterval;
    }

    class DefaultRequestContext implements RequestContext {

        private List<RequestProcessor> processors = new CopyOnWriteArrayList<RequestProcessor>();
        private Map<RequestProcessor, List<ResultFile>> resultsMap = new ConcurrentHashMap<RequestProcessor, List<ResultFile>>();
        private Request request;

        public DefaultRequestContext(Request req) {
            this.request = req;
        }

        void addRequestProcessor(RequestProcessor rp) {
            processors.add(rp);
        }

        public void measurementFinished(Request request, RequestProcessor processor, List<ResultFile> results) {
            if (results != null) {
                resultsMap.put(processor, results);
            }

            if (resultsMap.size() == processors.size()) // Finished measuring this request - need to process the results now
            {
                recordResults(this);
            }
        }

        public Request getRequest() {
           return request;
        }
    }

    public RequestManager() {

        requestProcessors = new ArrayList<RequestProcessor>();

        recordingExecutor = Executors.newSingleThreadExecutor();

        requestTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();

        requestTimeoutExecutor.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                checkRequestTimeout();
            }
        }, timeoutCheckInterval, timeoutCheckInterval, TimeUnit.MILLISECONDS);
        
        
    }

    public void requestStarting(Request request) {

        DefaultRequestContext ctx = new DefaultRequestContext(request);

        boolean measuring = false;
        for (RequestProcessor r : requestProcessors) {
            try {

                if (r.shouldMeasure(request)) {
                    ctx.addRequestProcessor(r);
                    r.startMeasuring(ctx);
                    measuring = true;
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception starting measurement with request processor " + r, e);
            }

        }

        if (measuring) {
            currentRequests.put(request,ctx);
        }


    }

    public void requestStopping(Request request) {

        RequestContext ctx=currentRequests.get(request);
        if(ctx==null)
            return; // This request is not being tracked

        request.setEndTime(System.currentTimeMillis());
        for (RequestProcessor r : requestProcessors) {

            try {
                r.stopMeasuring(ctx);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception stopping measurement with request processor " + r, e);
            }


        }

        currentRequests.remove(request);
    }

    private void recordResults(final DefaultRequestContext context) {


        recordingExecutor.submit(new Runnable() {

            public void run() {

                ArrayList<ResultFile> consolidatedResults = new ArrayList<ResultFile>();
                for (List<ResultFile> result : context.resultsMap.values()) {
                    consolidatedResults.addAll(result);
                }


                for (ResultsProcessor rp : resultsProcessors) {
                    try {
                        rp.processResult(context.request, consolidatedResults);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception processing measurement result with results processor " + rp, e);
                    }
                }

            }
        });


    }

    private void checkRequestTimeout() {

        if (requestTimeout > 0) {
            for (Request req : currentRequests.keySet()) {
                if (!req.isFinished() &&( System.currentTimeMillis() - req.getStartTime() )> requestTimeout) {
                    
                    requestStopping(req);
                    
                }
            }
        }

    }
}
