package com.company.peer;

import com.company.helper.P2PFileProcess;
import com.company.helper.P2PMessageProcess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class PeerListener extends Thread {
    private List<P2PFileProcess.PeerInfo> peersInfo;
    private P2PMessageProcess p2PMessageProcess;
    private int id;

    public PeerListener(int id, List<P2PFileProcess.PeerInfo> peersInfo) {
        this.peersInfo = peersInfo;
        this.id = id;
        //this.p2PMessageProcess = p2PMessageProcess;
    }

    public void run() {

        int clientNum = 0;
        //listen to all the request
        try {
            //The higher id try to connect the lower id peer
            for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
                if (peerInfo.ID >= id) {
                    break;
                }

                Socket connection = new Socket(peerInfo.hostName, peerInfo.port);
                new Peer(connection, new P2PMessageProcess(id), peerInfo).start();
                P2PFileProcess.Log(id, P2PFileProcess.LOG_CONNECTTO, peerInfo.ID);
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

            // //The lower id try to listen to the higher id peer to connect
            System.out.println(sPort + " begin to listen connection request");
            ServerSocket listener = new ServerSocket(sPort);
            while (true) {
                //all peers have connected our peer
                if (clientNum == 0) {
                    break;
                }

                new Peer(listener.accept(), new P2PMessageProcess(id)).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum--;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}
