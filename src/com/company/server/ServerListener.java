package com.company.server;

import com.company.helper.P2PFileProcess;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public class ServerListener extends Thread{
    private List<P2PFileProcess.PeerInfo> peersInfo;
    private int id;
    private byte[] field;

    public ServerListener(List<P2PFileProcess.PeerInfo> peersInfo, int id, byte[] field) {
        this.peersInfo = peersInfo;
        this.id = id;
        this.field = field;
    }

    public void run() {
        //get the port that we should connect
        int sPort = 0;
        for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
            if (peerInfo.ID == id) {
                sPort = peerInfo.port;
                break;
            }
        }

        //listen to all the request
        try {
            System.out.println(id + " begin to listen connection request");
            ServerSocket listener = new ServerSocket(sPort);
            int clientNum = 1;
            while (true) {
                new Server(listener.accept(), clientNum, id, field).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
