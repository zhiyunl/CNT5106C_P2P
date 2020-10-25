package com.company.client;

import com.company.helper.ConstructHandshakeMsg;
import com.company.helper.P2PFileProcess;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Client extends Thread {
    private Socket requestSocket;           //socket connect to the server
    private ObjectOutputStream out;         //stream write to the socket
    private ObjectInputStream in;
    private P2PFileProcess.PeerInfo peerInfo;
    private int id;
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";

    public Client(P2PFileProcess.PeerInfo peerInfo, int id) {
        this.peerInfo = peerInfo;
        this.id = id;
    }

    public void run() {
        try {
            //create a socket to connect to the server
            requestSocket = new Socket(peerInfo.hostName, peerInfo.port);
            System.out.println("Connected to localhost in port " + peerInfo.port);
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            ConstructHandshakeMsg constructHandshakeMsg = new ConstructHandshakeMsg();
            sendMessage(constructHandshakeMsg.constructHandshake(id));
            byte[] handshakeMsg = (byte[]) in.readObject();
            if (constructHandshakeMsg.getHandshakeHeader(handshakeMsg).equals(peerHeaderValue) && constructHandshakeMsg.getHandshakeId(handshakeMsg) == peerInfo.ID) {
                System.out.println("-----------------we have connected the right neighbour-----------------");
            }

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                //System.out.print("Hello, please input a sentence: ");
                //read a sentence from the standard input
                //message send to the server
                //String message = bufferedReader.readLine();
                //Send the sentence to the server
                //sendMessage(message);
                //Receive the upperCase sentence from the server
                //capitalized message read from the server
                String MESSAGE = (String) in.readObject();
                //show the message to the user
                System.out.println("Receive message: " + MESSAGE);
            }
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }



    private void sendMessage(String msg)
    {
        try{
            //stream write the message
            out.writeObject(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }


    //send a message to the output stream
    private void sendMessage(byte[] message)
    {
        try{
            //stream write the message
            out.writeObject(message);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }



}
