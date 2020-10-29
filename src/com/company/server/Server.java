package com.company.server;

import com.company.helper.P2PMessageProcess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Server extends Thread {
    private Socket connection;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private String state = "connection"; // the state of the program
    private int no;		//The index number of the client
    private int id;
    private byte[] field;

    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";

    Server(Socket connection, int no, int id, byte[] field) {
        this.connection = connection;
        this.no = no;
        this.id = id;
        this.field = field;
    }

    public void run() {
        try{
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());

            //implement P2PMessageProcess to help us handle different message
            P2PMessageProcess p2PMessageProcess = new P2PMessageProcess(id, field);
            //send handshake message
            p2PMessageProcess.sendHandShakeMsg(out);

            byte[] handshakeMsg = (byte[]) in.readObject();
            int peerID = p2PMessageProcess.getHandshakeId(handshakeMsg);

            //identify whether it is a right handshake
            if (p2PMessageProcess.getHandshakeHeader(handshakeMsg).equals(peerHeaderValue) && p2PMessageProcess.getHandshakeId(handshakeMsg) > id) {
                state = "handshake";
                System.out.println("-----------------we have received the request of handshake-----------------");
                p2PMessageProcess.sendBitField(out);
            }

            //handle the actual message after handshake
            if (state.equals("handshake")) {
                p2PMessageProcess.handleActualMsg(in, out, peerID);
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally{
            //Close connections
            try{
                in.close();
                out.close();
                connection.close();
            }
            catch(IOException ioException){
                System.out.println("Disconnect with Client " + no);
            }
        }
    }

}
