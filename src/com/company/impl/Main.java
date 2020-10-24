package com.company.impl;

import com.company.receiver.ReceiverListener;
import com.company.sender.SenderListener;

public class Main {

    private static final int sPort = 8001;   //The server will be listening on this port number
    private static final int id = 1001;
    private int[] peerList = new int[]{1001, 1002, 1003};
    private int[] socketList = new int[]{8001, 8002, 8003};

    private Main() {
        ReceiverListener receiverListener = new ReceiverListener(id, sPort);
        receiverListener.start();
        SenderListener senderListener = new SenderListener(socketList, peerList, id);
        senderListener.start();
    }

    public static void main(String[] args) {
        new Main();
    }

}
