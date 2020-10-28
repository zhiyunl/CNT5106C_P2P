package com.company.impl;

import com.company.helper.P2PFileProcess;
import com.company.server.ServerListener;
import com.company.client.ClientListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static Map<Integer, byte[]> peersBitfield = new HashMap<>(); // hashmap for storing peers id and corresponding bitfield

    private Main(int id) {
        P2PFileProcess p2PFileProcess = new P2PFileProcess();

        try {
            //get the number of pieces that we should cut.
            p2PFileProcess.CommonCfg();
//            int filePieces = p2PFileProcess.FileSize % p2PFileProcess.PieceSize == 0 ? p2PFileProcess.FileSize / p2PFileProcess.PieceSize : p2PFileProcess.FileSize / p2PFileProcess.PieceSize + 1;

            //get all peers information
            List<P2PFileProcess.PeerInfo> peersInfo = p2PFileProcess.PeerInfoCfg();
            // generate test data
            p2PFileProcess.DataGeneration(id);
            // load pieces from file into 2d byte array
            p2PFileProcess.initPieces(id);
            // combine all pieces and write into file
            p2PFileProcess.combinePieces(id);

            int hasFile = 0;
            byte[] field = new byte[p2PFileProcess.getTotalPieces()];

            //identify whether it has the whole file
            for (P2PFileProcess.PeerInfo peer : peersInfo) {
                if (peer.ID == id) {
                    hasFile = peer.hasFile;
                    break;
                }
            }

            //create bit field array at the initial stage
            if (hasFile == 0) {
                Arrays.fill(field, (byte) 0);
            }
            else {
                Arrays.fill(field, (byte) 1);
            }

            //create server and client thread for this peer
            ServerListener serverListener = new ServerListener(peersInfo, id, field);
            serverListener.start();
            ClientListener clientListener = new ClientListener(peersInfo, id, field);
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
