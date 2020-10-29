package com.company.client;

import com.company.helper.P2PFileProcess;

import java.util.List;

public class ClientListener extends Thread {
    private List<P2PFileProcess.PeerInfo> peersInfo;
    private int id;
    private byte[] field;

    public ClientListener(List<P2PFileProcess.PeerInfo> peersInfo, int id, byte[] field) {
        this.peersInfo = peersInfo;
        this.id = id;
        this.field = field;
    }

   /* public void run() {
        //connect with peers whose id is smaller than itself
        for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
            if (peerInfo.ID >= id) {
                break;
            }
            new Client(peerInfo, id, field).start();
        }
    }*/

}
