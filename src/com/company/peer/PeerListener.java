package com.company.peer;

import com.company.helper.P2PFileProcess;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class PeerListener extends Thread {
    private List<P2PFileProcess.PeerInfo> peersInfo;
    private int id;
    private byte[] field;

    public PeerListener(List<P2PFileProcess.PeerInfo> peersInfo, int id, byte[] field) {
        this.peersInfo = peersInfo;
        this.id = id;
        this.field = field;
    }

    public void run() {

        int clientNum = 0;
        //listen to all the request
        try {
            for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
                if (peerInfo.ID >= id) {
                    break;
                }

                Socket connection = new Socket(peerInfo.hostName, peerInfo.port);
                new Peer(connection, id, field, peerInfo).start();
                System.out.println("Connected to localhost in port " + peerInfo.port);
                clientNum++;
            }

            clientNum = peersInfo.size() - clientNum - 1;
            int sPort = 0;
            for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
                if (peerInfo.ID == id) {
                    sPort = peerInfo.port;
                    break;
                }
            }

            System.out.println(sPort + " begin to listen connection request");
            ServerSocket listener = new ServerSocket(sPort);
            while (true) {
                //all peers have connected our peer
                if (clientNum == 0) {
                    break;
                }

                new Peer(listener.accept(), id, field).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum--;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}
