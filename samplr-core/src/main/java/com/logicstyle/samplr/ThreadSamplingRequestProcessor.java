/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot.NoDataAvailableException;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot;

/**
 * Profiler entry point.
 *
 * @author juliano
 */
public class ThreadSamplingRequestProcessor extends RequestProcessor<ThreadSamplingRequestProcessor> {

    private Map<RequestContext, SamplingRequestContext> ongoingRequests;
    private Map<RequestContext, SamplingRequestContext> samplingRequests;
    private Map<Long, StackTraceSnapshotBuilder> snapshotBuilders;
    ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    private long requestLengthSamplingThreshold;

    public long getRequestLengthSamplingThreshold() {
        return requestLengthSamplingThreshold;
    }

    public void setRequestLengthSamplingThreshold(long requestLengthSamplingThreshold) {
        this.requestLengthSamplingThreshold = requestLengthSamplingThreshold;
    }

    public ThreadSamplingRequestProcessor withRequestLengthSamplingThreshold(long requestLengthThreshold) {
        setRequestLengthSamplingThreshold(requestLengthThreshold);
        return this;
    }

    public class SamplingRequestContext {

        private RequestContext requestContext;
        private StackTraceSnapshotBuilder snapshotBuilder;
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

        public StackTraceSnapshotBuilder getSnapshotBuilder() {
            return snapshotBuilder;
        }

        public void setSnapshotBuilder(StackTraceSnapshotBuilder snapshotBuilder) {
            this.snapshotBuilder = snapshotBuilder;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SamplingRequestContext other = (SamplingRequestContext) obj;
            if (this.requestContext != other.requestContext && (this.requestContext == null || !this.requestContext.equals(other.requestContext))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.requestContext != null ? this.requestContext.hashCode() : 0);
            return hash;
        }
    }

    public ThreadSamplingRequestProcessor() {
        ongoingRequests = new ConcurrentHashMap<RequestContext, SamplingRequestContext>();
        samplingRequests = new ConcurrentHashMap<RequestContext, SamplingRequestContext>();
        snapshotBuilders = new ConcurrentHashMap<Long, StackTraceSnapshotBuilder>();
        samplingThread = new SamplingThread();
        samplrMonitorThread = new SamplrMonitorThread();

        samplingThread.start();
        samplrMonitorThread.start();

    }

    public void startMeasuring(RequestContext context) {
        startMonitoring(context);
    }

    public void stopMeasuring(RequestContext context) {

        SamplingRequestContext samplingContext = ongoingRequests.get(context);
        if (samplingContext == null) {
            return; // not monitoring this request
        }
        
        List<ResultFile> resultList=Collections.EMPTY_LIST;
        

        try {
            if (samplingContext.isSampling()) {

                samplingRequests.remove(context);
                samplingContext.setSampleEndTime(System.currentTimeMillis());
                StackTraceSnapshotBuilder snapshotBuilder = snapshotBuilders.get(context.getRequest().getThreadId());
                snapshotBuilders.remove(context.getRequest().getThreadId());
                CPUResultsSnapshot snapshot;
                try {
                    snapshot = snapshotBuilder.createSnapshot(System.currentTimeMillis());
                } catch (NoDataAvailableException ex) {
                    throw new RuntimeException(ex);
                }

                LoadedSnapshot ls = new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bout);
                ls.save(out);
                out.flush();

                ResultFile samplingFile = new ResultFile();

                samplingFile.setName("request-sampling.nps");
                samplingFile.setContent(new ByteArrayInputStream(bout.toByteArray()));

                bout = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(bout));
                pw.println("Sampling start time: " + new Date(samplingContext.getSampleStartTime()));
                pw.println("Sampling end time: " + new Date(samplingContext.getSampleEndTime()));
                pw.flush();

                ResultFile infoFile = new ResultFile();
                infoFile.setName("sampling-info.txt");
                infoFile.setContent(new ByteArrayInputStream(bout.toByteArray()));

                resultList= Arrays.asList(new ResultFile[]{samplingFile, infoFile});
                
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            context.measurementFinished(context.getRequest(), this,resultList);
            ongoingRequests.remove(context);
        }






        



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

                for (SamplingRequestContext r : ongoingRequests.values()) {
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

    private void startMonitoring(RequestContext request) {

        SamplingRequestContext samplingContext = new SamplingRequestContext(request);


        ongoingRequests.put(request, samplingContext);

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

        private final Object waitLock = new Object();
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

                if (samplingRequests.isEmpty()) {
                    synchronized (waitLock) {
                        try {
                            waitLock.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }

                long[] threadIds = new long[samplingRequests.size()];
                int i = 0;
                for (SamplingRequestContext context : samplingRequests.values()) {
                    Request request = context.getRequestContext().getRequest();

                    if (!request.isFinished()) {
                        threadIds[i] = request.getThreadId();
                        i++;
                        if (i > threadIds.length - 1) {
                            break;
                        }
                    }
                }
                if (i == 0) {
                    continue;
                }

                if (i < threadIds.length) {
                    // Unlikely, but thread can have been terminated between size() and values()
                    long[] tmp = threadIds;
                    threadIds = new long[i];
                    for (int j = 0; j < i; j++) {
                        threadIds[j] = tmp[i];
                    }
                }




                ThreadInfo[] ti = mxBean.getThreadInfo(threadIds, Integer.MAX_VALUE);

                if (ti != null) {
                    for (ThreadInfo t : ti) {
                        if (t != null) {
                            StackTraceSnapshotBuilder builder = snapshotBuilders.get(t.getThreadId());
                            if (builder != null) // builder is null if thread has finished
                            {
                                builder.addStacktrace(new ThreadInfo[]{t}, System.nanoTime());
                            }
                        }
                    }
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

        samplingRequests.put(context.getRequestContext(), context);
        snapshotBuilders.put(context.getRequestContext().getRequest().getThreadId(), new StackTraceSnapshotBuilder());
        context.setSampleStartTime(System.currentTimeMillis());
        samplingThread.wakeUp();
    }
}
