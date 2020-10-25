package com.company.server;

import com.company.helper.P2PFileProcess;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public class ServerListener extends Thread{
    private List<P2PFileProcess.PeerInfo> peersInfo;
    private int id;

    public ServerListener(List<P2PFileProcess.PeerInfo> peersInfo, int id) {
        this.peersInfo = peersInfo;
        this.id = id;
    }

    public void run() {
        int sPort = 0;
        for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
            if (peerInfo.ID == id) {
                sPort = peerInfo.port;
                break;
            }
        }

        try {
            System.out.println(id + " begin to listen connection request");
            ServerSocket listener = new ServerSocket(sPort);
            int clientNum = 1;
            while (true) {
                new Server(listener.accept(), clientNum, id).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
