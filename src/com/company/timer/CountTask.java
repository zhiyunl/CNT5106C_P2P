package com.company.timer;

import com.company.helper.P2PMessageProcess;

import java.util.TimerTask;

public class CountTask extends TimerTask {

    private int type;
    private P2PMessageProcess p2PMessageProcess;

    public CountTask(int type, P2PMessageProcess p2PMessageProcess) {
        this.type = type;
        this.p2PMessageProcess = p2PMessageProcess;
    }

    @Override
    public void run() {

        synchronized (CountTask.class) {
            if (type == 0) {
                p2PMessageProcess.selectPreferredNeighbors();
            } else if (type == 1) {
                p2PMessageProcess.selectOptimisticPeer();
            }
        }


    }

    public void stopSignal() {
        System.out.println("the timer is finished");
        System.gc();
        cancel();
    }

}
