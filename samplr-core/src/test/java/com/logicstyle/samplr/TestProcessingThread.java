/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicstyle.samplr;

import java.util.Random;

/**
 *
 * @author juliano
 */
public class TestProcessingThread extends Thread {
    private final int maxIterations;
    private boolean keepRunning = true;
    

    public TestProcessingThread(int maxIterations) {
        super("Test Request Processing Thread");
      
        this.maxIterations = maxIterations;
    }

    @Override
    public void run() {
        Random random = new Random();
        for (int i = 0; i < maxIterations && keepRunning; i++) {
            int randomInt = random.nextInt(10);
            switch (randomInt) {
                case 0:
                case 1:
                case 2:
                    methodA();
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    methodB();
                    break;
                default:
                    methodC(100);
            }
        }
    }

    public void stopRunning() {
        keepRunning = false;
    }

    private void methodA() {
        Random random = new Random();
        for (int i = 0; i < 50000; i++) {
            System.err.println(random.nextInt());
        }
    }

    private void methodB() {
        Random random = new Random();
        int randomInt = random.nextInt();
        for (int j = 1; j < randomInt / 2; j++) {
            if (randomInt % j == 0) {
                System.err.println("Not prime!");
                return;
            }
        }
        System.err.println("Prime!!!!!");
    }

    private void methodC(int maxDepth) {
        if (maxDepth == 0) {
            System.err.println("Max depth reached!");
            return;
        }
        try {
            sleep(new Random().nextInt(300));
        } catch (InterruptedException ex) {
        }
        methodC(maxDepth - 1);
    }
    
}
