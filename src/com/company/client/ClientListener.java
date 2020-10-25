package com.company.client;

import com.company.helper.P2PFileProcess;

import java.util.List;

public class ClientListener extends Thread {
    private List<P2PFileProcess.PeerInfo> peersInfo;
    private int id;
    public ClientListener(List<P2PFileProcess.PeerInfo> peersInfo, int id) {
        this.peersInfo = peersInfo;
        this.id = id;
    }

    public void run() {
        for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
            if (peerInfo.ID >= id) {
                break;
            }
            new Client(peerInfo, id).start();
        }
    }

}
