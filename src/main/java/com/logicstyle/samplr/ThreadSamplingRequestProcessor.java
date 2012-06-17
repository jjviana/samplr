/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.openide.util.Exceptions;

/**
 * Profiler entry point.
 *
 * @author juliano
 */
public class ThreadSamplingRequestProcessor extends RequestProcessor {

    private List<SamplingRequestContext> ongoingRequests;
    private List<SamplingRequestContext> samplingRequests;
    private Map<Long, StackTraceSnapshotBuilder> snapshotBuilders;
    ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    private long requestLengthSamplingThreshold;

    public long getRequestLengthSamplingThreshold() {
        return requestLengthSamplingThreshold;
    }

    public void setRequestLengthSamplingThreshold(long requestLengthSamplingThreshold) {
        this.requestLengthSamplingThreshold = requestLengthSamplingThreshold;
    }

    public class SamplingRequestContext {

        private RequestContext requestContext;
        private long sampleStartTime;
        private long sampleEndTime;

        public SamplingRequestContext(RequestContext requestContext) {
            this.requestContext = requestContext;
        }

        public RequestContext getRequestContext() {
            return requestContext;
        }

        public void setRequestContext(RequestContext requestContext) {
            this.requestContext = requestContext;
        }

        public long getSampleEndTime() {
            return sampleEndTime;
        }

        public void setSampleEndTime(long sampleEndTime) {
            this.sampleEndTime = sampleEndTime;
        }

        public long getSampleStartTime() {
            return sampleStartTime;
        }

        public void setSampleStartTime(long sampleStartTime) {
            this.sampleStartTime = sampleStartTime;
        }

        public boolean isSampling() {
            return sampleStartTime > 0;
        }
        
        
    }

    public ThreadSamplingRequestProcessor() {
        ongoingRequests = new CopyOnWriteArrayList<SamplingRequestContext>();
        samplingRequests = new CopyOnWriteArrayList<SamplingRequestContext>();
        snapshotBuilders = new ConcurrentHashMap<Long, StackTraceSnapshotBuilder>();
        samplingThread = new SamplingThread();
        samplrMonitorThread = new SamplrMonitorThread();

    }

    public void startMeasuring(RequestContext context) {
        startMonitoring(context);
    }

    public void stopMeasuring(RequestContext request) {
        
        // TODO: how to locate a sampling request contet from a request context (equals, hashcode)
    }

    class SamplrMonitorThread extends Thread {

        public SamplrMonitorThread() {
            super("Samplr monitor thread");
            setDaemon(true);
        }
        private boolean keepRunning = true;

        @Override
        public void run() {


            while (keepRunning) {

                for (SamplingRequestContext r : ongoingRequests) {
                    checkShouldSample(r);

                }
                try {
                    Thread.sleep(monitoringInterval);
                } catch (InterruptedException ex) {
                }

            }
        }
    }
    private SamplrMonitorThread samplrMonitorThread;
    private long monitoringInterval = 500;
    private MeasuringCriteria samplingCriteria;

    private void startMonitoring(RequestContext request) {

        SamplingRequestContext samplingContext = new SamplingRequestContext(request);


        ongoingRequests.add(samplingContext);

        checkShouldSample(samplingContext); // In case sampling for this case of request should begin immediately

    }

    private boolean checkShouldSample(SamplingRequestContext context) {




        if (!context.isSampling() && requestLengthSamplingThreshold > 0 && (System.currentTimeMillis() - context.getRequestContext().getRequest().getStartTime()) > requestLengthSamplingThreshold) {
            startSampling(context);
            return true;
        }

        return false;


    }
    private long samplingInterval = 20;

    class SamplingThread extends Thread {

        private Object waitLock = new Object();
        private boolean keepRunning = true;

        public SamplingThread() {

            super("Samplr sampling thread");
            setDaemon(true);
        }

        public void wakeUp() {
            synchronized (waitLock) {
                waitLock.notifyAll();
            }
        }

        @Override
        public void run() {

            while (keepRunning) {

                if (ongoingRequests.isEmpty()) {
                    synchronized (waitLock) {
                        try {
                            waitLock.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }

                long[] threadIds = new long[samplingRequests.size()];
                int i = 0;
                for (SamplingRequestContext context : samplingRequests) {
                    Request request = context.getRequestContext().getRequest();

                    if (!request.isFinished()) {
                        threadIds[i] = request.getThreadId();
                        i++;
                        if (i > threadIds.length - 1) {
                            break;
                        }
                    }
                }




                ThreadInfo[] ti = mxBean.getThreadInfo(threadIds, Integer.MAX_VALUE);

                for (ThreadInfo t : ti) {

                    StackTraceSnapshotBuilder builder = snapshotBuilders.get(t.getThreadId());
                    builder.addStacktrace(new ThreadInfo[]{t}, System.nanoTime());
                }

            }

            try {
                Thread.sleep(samplingInterval);
            } catch (InterruptedException ex) {
            }
        }
    }
    private SamplingThread samplingThread;

    void startSampling(SamplingRequestContext context) {

        samplingRequests.add(context);
        snapshotBuilders.put(context.getRequestContext().getRequest().getThreadId(), new StackTraceSnapshotBuilder());
        context.setSampleStartTime(System.currentTimeMillis());
         samplingThread.wakeUp();
    }

   
}
