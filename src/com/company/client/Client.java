package com.company.client;

import com.company.helper.P2PFileProcess;
import com.company.helper.P2PMessageProcess;
import com.company.helper.PeersBitfield;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client extends Thread {
    private Socket connection;           //socket connect to the server
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private String state = "connection"; // the state of the program
    private P2PFileProcess.PeerInfo peerInfo;
    private int id;
    private byte[] field;
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";
    private PeersBitfield peersBitfield; // storing peers id and corresponding bitfield

    Client(P2PFileProcess.PeerInfo peerInfo, int id, byte[] field) {
        this.peerInfo = peerInfo;
        this.id = id;
        this.field = field;
    }

    public void run() {
        try {
            //create a socket to connect to the server
            connection = new Socket(peerInfo.hostName, peerInfo.port);
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            System.out.println("Connected to localhost in port " + peerInfo.port);

            //implement P2PMessageProcess to help us handle different message
            P2PMessageProcess p2PMessageProcess = new P2PMessageProcess(id, field);
            //send handshake message
            p2PMessageProcess.sendHandShakeMsg(out);

            byte[] handshakeMsg = (byte[]) in.readObject();

            //identify whether it is a right handshake
            if (p2PMessageProcess.getHandshakeHeader(handshakeMsg).equals(peerHeaderValue) && p2PMessageProcess.getHandshakeId(handshakeMsg) == peerInfo.ID) {
                state = "handshake";
                System.out.println("-----------------we have connected the right neighbour-----------------");
                //send BitField
                p2PMessageProcess.sendBitField(out);
            }

            // create arraylist for storing peers id and corresponding bitfield
            if (state.equals("handshake")) {
                peersBitfield = new PeersBitfield(id, field);
                System.out.println("created peer bitfield");
            }

            //handle the actual message after handshake
            if (state.equals("handshake")) {
                p2PMessageProcess.handleActualMsg(in, out);
            }

        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
                connection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
