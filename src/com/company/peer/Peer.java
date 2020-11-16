package com.company.peer;
import com.company.helper.P2PFileProcess;
import com.company.helper.P2PMessageProcess;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Peer extends Thread {
    private Socket connection;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private String state = "connection"; // the state of the program
    private P2PFileProcess.PeerInfo peerInfo = null;
    private P2PMessageProcess p2PMessageProcess;

    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";

    Peer(Socket connection, P2PMessageProcess p2PMessageProcess) {
        this.connection = connection;
        this.p2PMessageProcess = p2PMessageProcess;
    }

    Peer(Socket connection, P2PMessageProcess p2PMessageProcess, P2PFileProcess.PeerInfo peerInfo) {
        this.connection = connection;
        this.p2PMessageProcess = p2PMessageProcess;
        this.peerInfo = peerInfo;
    }

    public void run() {
        try {
            if (peerInfo != null) {
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
            }
            else {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
            }

            //P2PMessageProcess p2PMessageProcess = new P2PMessageProcess(id);
            //send handshake message
            p2PMessageProcess.sendHandShakeMsg(out);

            byte[] handshakeMsg = (byte[]) in.readObject();
            int peerID = p2PMessageProcess.getHandshakeId(handshakeMsg);
            P2PFileProcess.Log(P2PMessageProcess.id, P2PFileProcess.LOG_CONNECTFROM, peerID);

            //identify whether it is a right handshake
            if (p2PMessageProcess.getHandshakeHeader(handshakeMsg).equals(peerHeaderValue) && (peerInfo == null || peerInfo.ID == peerID)) {
                state = "handshake";
                System.out.println("-----------------we have connected the right neighbour-----------------");
                //send BitField
                p2PMessageProcess.sendBitField(out);
                //add peer to the peer map in order to send not interested to all neighbours
                p2PMessageProcess.constructPeerMap(peerID, this);
                System.out.println("the peer " + P2PMessageProcess.id + " " + "can send message to " + peerID + " by this out channel");

                // test send piece
//                byte[] pieceID = P2PMessageProcess.intToByteArray(5);
//                p2PMessageProcess.sendPiece(pieceID,out);
            }

            //handle the actual message after handshake
            if (state.equals("handshake")) {
                p2PMessageProcess.handleActualMsg(in, out, peerID);
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

    public ObjectOutputStream getOut() {
        return out;
    }

}
