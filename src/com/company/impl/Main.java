package com.company.impl;

import com.company.helper.P2PFileProcess;
import com.company.server.ServerListener;
import com.company.client.ClientListener;

import java.io.IOException;
import java.util.List;

public class Main {
    private int id = 1001;
    private Main(int id) {
        this.id = id;
        P2PFileProcess p2PFileProcess = new P2PFileProcess();
        try {
            List<P2PFileProcess.PeerInfo> peersinfo = p2PFileProcess.PeerInfoCfg();
            ServerListener serverListener = new ServerListener(peersinfo, this.id);
            serverListener.start();
            ClientListener clientListener = new ClientListener(peersinfo, this.id);
            clientListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Use id as arguments to set peerProcess ID
        if (args.length >= 1){
            new Main(Integer.parseInt(args[0]));
        }else{
            new Main(1001); // default id is 1001
        }

    }

}
