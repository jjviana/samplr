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
import org.openide.util.Exceptions;

/**
 * Manages requests, coordinating the work of Request Processors.
 *
 * @author juliano
 */
public class RequestManager {

    private static final Logger logger = Logger.getLogger(RequestManager.class.getName());
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
    private Map<Request, RequestContext> measuringRequests = new ConcurrentHashMap<Request, RequestContext>();
    private Map<Request, RequestContext> finishingRequests = new ConcurrentHashMap<Request, RequestContext>();
    private List<ResultsProcessor> resultsProcessors = new CopyOnWriteArrayList<ResultsProcessor>();

    public enum Status {

        RUNNING, SHUTTING_DOWN, STOPPED
    };
    private Status status;

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

    private void checkStarted() {
        if (status != Status.RUNNING) {
            throw new IllegalStateException("RequestManager is not started");
        }

    }

    class DefaultRequestContext implements RequestContext {

        private List<RequestProcessor> processors = new CopyOnWriteArrayList<RequestProcessor>();
        private Map<RequestProcessor, List<ResultFile>> resultsMap = new ConcurrentHashMap<RequestProcessor, List<ResultFile>>();
        private Request request;
        private int finishedProcessors = 0;

        public DefaultRequestContext(Request req) {
            this.request = req;
        }

        void addRequestProcessor(RequestProcessor rp) {
            processors.add(rp);
        }

        public synchronized void measurementFinished(Request request, RequestProcessor processor, List<ResultFile> results) {
            if (results != null) {
                resultsMap.put(processor, results);
            }
            finishedProcessors++;

            if (finishedProcessors == processors.size() && !resultsMap.isEmpty()) {
                recordResults(this);
            } else {
                terminateRequest(request);
            }
        }

        public Request getRequest() {
            return request;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DefaultRequestContext other = (DefaultRequestContext) obj;
            if (this.request != other.request && (this.request == null || !this.request.equals(other.request))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.request != null ? this.request.hashCode() : 0);
            return hash;
        }
    }
    private final Object shutdownLock = new Object();

    private void terminateRequest(Request request) {
        synchronized (shutdownLock) {
            finishingRequests.remove(request);
            if (status == Status.SHUTTING_DOWN) {
                if (measuringRequests.isEmpty() && finishingRequests.isEmpty()) {
                    recordingExecutor.shutdown();
                    requestTimeoutExecutor.shutdown();
                    status = Status.STOPPED;
                    shutdownLock.notifyAll();
                }
            }
        }

    }

    public RequestManager() {
           requestProcessors = new ArrayList<RequestProcessor>();
    }

    public void start() {
     

        recordingExecutor = Executors.newSingleThreadExecutor();

        requestTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();

        requestTimeoutExecutor.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                checkRequestTimeout();
            }
        }, timeoutCheckInterval, timeoutCheckInterval, TimeUnit.MILLISECONDS);

        status = Status.RUNNING;
    }

    public void requestStarting(Request request) {

        checkStarted();

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
            measuringRequests.put(request, ctx);
        }


    }

    public void requestFinished(Request request) {

        RequestContext ctx = measuringRequests.get(request);
        if (ctx == null) {
            return; // This request is not being tracked
        }
        measuringRequests.remove(request);
        request.setEndTime(System.currentTimeMillis());
        finishingRequests.put(request, ctx);


        for (RequestProcessor r : requestProcessors) {

            try {
                r.stopMeasuring(ctx);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception stopping measurement with request processor " + r, e);
            }


        }


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

                terminateRequest(context.getRequest());



            }
        });


    }

    private void checkRequestTimeout() {

        if (requestTimeout > 0) {
            for (Request req : measuringRequests.keySet()) {
                if (!req.isFinished() && (System.currentTimeMillis() - req.getStartTime()) > requestTimeout) {

                    requestFinished(req);

                }
            }
        }

    }

    public RequestManager withRequestProcessor(RequestProcessor processor) {

        requestProcessors.add(processor);

        return this;
    }

    public RequestManager withResultsProcessor(ResultsProcessor processor) {

        resultsProcessors.add(processor);
        return this;

    }

    public void shutdown() {

        if (status != Status.RUNNING) {
            return;
        }

        status = Status.SHUTTING_DOWN;



    }

    public Status awaitTermination(long timeout) {

        synchronized (shutdownLock) {
            if (status != Status.STOPPED) {

                try {
                    shutdownLock.wait(timeout);
                } catch (InterruptedException e) {
                }
            }

            return status;
        }


    }
}