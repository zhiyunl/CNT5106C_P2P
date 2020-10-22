package com.company.impl;

import com.company.receiver.Listener;
import com.company.sender.Sender;

public class Main {

    private static final int sPort = 8001;   //The server will be listening on this port number
    private static final int id = 1001;
    private int[] peerList = new int[]{1001, 1002, 1003};
    private int[] socketList = new int[]{8001, 8002, 8003};

    private Main() {
        Listener listener = new Listener(id, sPort);
        listener.start();
        for (int i = 0; i < peerList.length && peerList[i] < id; i++) {
            new Sender(socketList[i]).start();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

}
