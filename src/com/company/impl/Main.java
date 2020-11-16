package com.company.impl;

import com.company.helper.P2PFileProcess;
import com.company.helper.P2PMessageProcess;
import com.company.peer.Peer;
import com.company.peer.PeerListener;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Main {

    public static Map<Integer, byte[]> peersBitField = new HashMap<>(); // HashMap for storing peers id and corresponding BitField
    public static List<Integer> processingList = new LinkedList<>();
    public static int firstPeerID;
    public static byte[] field;

    private Main(int id) {
        P2PFileProcess p2PFileProcess = new P2PFileProcess();

        try {
            //get the number of pieces that we should cut.
            p2PFileProcess.CommonCfg();
//            int filePieces = p2PFileProcess.FileSize % p2PFileProcess.PieceSize == 0 ? p2PFileProcess.FileSize / p2PFileProcess.PieceSize : p2PFileProcess.FileSize / p2PFileProcess.PieceSize + 1;

            //get all peers information
            List<P2PFileProcess.PeerInfo> peersInfo = p2PFileProcess.PeerInfoCfg();
            // generate test data
            //p2PFileProcess.DataGeneration(id);
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

//            //identify whether it has the whole file
//            for (P2PFileProcess.PeerInfo peer : peersInfo) {
//                if (peer.ID == id) {
//                    hasFile = peer.hasFile;
//                    break;
//                }
//            }
//
//            //create bit field array at the initial stage
//            if (hasFile == 0) {
//                Arrays.fill(field, (byte) 0);
//            }
//            else {
//                Arrays.fill(field, (byte) 1);
//            }

            //initialize field
            int myIndex = P2PFileProcess.getPeerIndexByID(id);
            if (peersInfo.get(myIndex).hasFile == 0){
                Arrays.fill(field, (byte) 0);
            }else{
                Arrays.fill(field, (byte) 1);
            }

            //create server and client thread for this peer
            PeerListener peerListener = new PeerListener(new P2PMessageProcess(id), peersInfo);
            peerListener.start();
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
