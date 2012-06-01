package com.logicstyle.insider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.openide.util.Exceptions;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws Exception {
        
        
        Thread testThread=new Thread(new Runnable() {

            public void run() {
                
                while(true) {
                    double random=Math.random();
                    System.err.println(random);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    
                }
                
            }
            
        });

        testThread.start();
        StackTraceSnapshotBuilder builder = new StackTraceSnapshotBuilder();

        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

        for (int i = 0; i < 1000; i++) {
            ThreadInfo ti = mxBean.getThreadInfo(testThread.getId(), Integer.MAX_VALUE);

            builder.addStacktrace(new ThreadInfo[] {ti}, System.nanoTime());
            
            Thread.sleep(20);

        }

        CPUResultsSnapshot snapshot = builder.createSnapshot(System.currentTimeMillis());

        LoadedSnapshot ls = new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);

        File file = new File("test.nps");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
        ls.save(dos);
        ls.setFile(file);
        ls.setSaved(true);

        System.out.println("Snapshot saved!");
        
        System.exit(0);
    }
}
