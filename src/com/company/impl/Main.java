package com.company.impl;

import com.company.helper.P2PFileProcess;
import com.company.peer.PeerListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static Map<Integer, byte[]> peersBitField = new ConcurrentHashMap<>(); // HashMap for storing peers id and corresponding BitField
    public static List<Integer> processingList = new LinkedList<>();
    public static int firstPeerID;
    public static byte[] field;
    public static int optimalPeer;

    private Main(int id) {
        P2PFileProcess p2PFileProcess = new P2PFileProcess();

        try {
            //get the number of pieces that we should cut.
            p2PFileProcess.CommonCfg();

            //get all peers information
            List<P2PFileProcess.PeerInfo> peersInfo = p2PFileProcess.PeerInfoCfg();
            // TODO uncomment this to generate test data
//            p2PFileProcess.DataGeneration(id);

            // load pieces from file into 2d byte array
            p2PFileProcess.initPieces(id);
            // init Log
            p2PFileProcess.LogSetup(id);

//            int hasFile = 0;
            field = new byte[P2PFileProcess.getTotalPieces()];

            //initialize processingList
            firstPeerID = peersInfo.get(0).ID;
            if (id == firstPeerID) {
                for (P2PFileProcess.PeerInfo peerInfo : peersInfo) {
                    if (peerInfo.hasFile == 0) {
                        processingList.add(peerInfo.ID);
                    }
                }
            }

            //initialize field
            int myIndex = P2PFileProcess.getPeerIndexByID(id);
            if (peersInfo.get(myIndex).hasFile == 0) {
                Arrays.fill(field, (byte) 0);
            } else {
                Arrays.fill(field, (byte) 1);
            }

            //create server and client thread for this peer
            PeerListener peerListener = new PeerListener(id, peersInfo);
            peerListener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Use id as arguments to set peerProcess ID
        if (args.length >= 1) {
            new Main(Integer.parseInt(args[0]));
        } else {
            new Main(1001); // default id is 1001
        }
    }

}
