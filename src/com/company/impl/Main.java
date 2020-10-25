package com.company.impl;

import com.company.helper.P2PFileProcess;
import com.company.server.ServerListener;
import com.company.client.ClientListener;

import java.io.IOException;
import java.util.List;

public class Main {

    private static final int sPort = 8001;   //The server will be listening on this port number
    private static final int id = 1001;
    private int[] peerList = new int[]{1001, 1002, 1003};
    private int[] socketList = new int[]{8001, 8002, 8003};

    private Main() {
        P2PFileProcess p2PFileProcess = new P2PFileProcess();
        try {
            List<P2PFileProcess.PeerInfo> peersinfo = p2PFileProcess.PeerInfoCfg();
            ServerListener serverListener = new ServerListener(peersinfo, id);
            serverListener.start();
            ClientListener clientListener = new ClientListener(peersinfo, id);
            clientListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

}
