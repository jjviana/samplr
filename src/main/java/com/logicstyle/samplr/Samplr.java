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
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.openide.util.Exceptions;

/**
 * Profiler entry point.
 *
 * @author juliano
 */
public class Samplr {

    private Map<Long, Request> ongoingRequests;
    private Map<Long, Request> samplingRequests;
    private Queue<Request> finishedRequests;
    private Map<Long, StackTraceSnapshotBuilder> snapshotBuilders;
    ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    public Samplr() {
        ongoingRequests = new ConcurrentHashMap<Long, Request>();
        samplingRequests = new ConcurrentHashMap<Long, Request>();
        snapshotBuilders = new ConcurrentHashMap<Long, StackTraceSnapshotBuilder>();
        samplingThread = new SamplingThread();
        samplrMonitorThread = new SamplrMonitorThread();

        finishedRequests=new ConcurrentLinkedQueue<Request>();
        

    }

    class SamplrMonitorThread extends Thread {

        private boolean keepRunning = true;

        @Override
        public void run() {


            while (keepRunning) {

                for (Request r : ongoingRequests.values()) {
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
    private SamplingCriteria samplingCriteria;

    public void requestStarted(Request request) {
        if (ongoingRequests.containsKey(request.getId())) {
            return; // can never monitor the same request twice
        }
        if (samplingCriteria.shouldMonitor(request)) {
            startMonitoring(request);
        }



    }

    private void startMonitoring(Request request) {

        ongoingRequests.put(request.getId(), request);

        checkShouldSample(request); // In case sampling for this case of request should begin immediately

    }

    private boolean checkShouldSample(Request request) {

        if (!request.isSampling() && !samplingCriteria.shouldSample(request)) {
            startSampling(request);
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
                
                for (Request request : samplingRequests.values()) {
                    if (!request.isFinished()) {
                        ThreadInfo ti = mxBean.getThreadInfo(request.getThreadId(), Integer.MAX_VALUE);
                        StackTraceSnapshotBuilder builder = snapshotBuilders.get(request.getId());
                        builder.addStacktrace(new ThreadInfo[]{ti}, System.nanoTime());
                    }

                }
                try {
                    Thread.sleep(samplingInterval);
                } catch (InterruptedException ex) {
                    
                }
            }



        }
    }
    private SamplingThread samplingThread;

    void startSampling(Request request) {

        samplingRequests.put(request.getId(), request);
        snapshotBuilders.put(request.getId(), new StackTraceSnapshotBuilder());
        samplingThread.wakeUp();
        request.setSampleStartTime(System.currentTimeMillis());
    }

    public void requestFinished(Request request) {
       
        samplingRequests.remove(request.getId());
        ongoingRequests.remove(request.getId());
        
        if(request.isSampling()) 
            finishedRequests.add(request);
        
        // TODO: implement processing results...
            
            
        
            
        
        
    }
}
