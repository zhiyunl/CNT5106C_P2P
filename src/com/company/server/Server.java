package com.company.server;

import com.company.helper.ConstructHandshakeMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Server extends Thread {
    private String message;    //message received from the client
    private String MESSAGE;    //uppercase message send to the client
    private Socket connection;
    private ObjectInputStream in;	//stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private int no;		//The index number of the client
    private int socketId;
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";

    public Server(Socket connection, int no, int socketId) {
        this.connection = connection;
        this.no = no;
        this.socketId = socketId;
    }

    public void run() {
        try{
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            byte[] handshakeMsg = (byte[]) in.readObject();
            ConstructHandshakeMsg constructHandshakeMsg = new ConstructHandshakeMsg();
            if (constructHandshakeMsg.getHandshakeHeader(handshakeMsg).equals(peerHeaderValue) && constructHandshakeMsg.getHandshakeId(handshakeMsg) > socketId) {
                System.out.println("-----------------we have received the request of handshake-----------------");
                sendMessage(constructHandshakeMsg.constructHandshake(socketId));
            }

            try{
                while(true)
                {
                    //receive the message sent from the client
                    message = (String)in.readObject();
                    //show the message to the user
                    System.out.println("Receive message: " + message + " from client " + no);
                    //Capitalize all letters in the message
                    MESSAGE = message.toUpperCase();
                    //send MESSAGE back to the client
                    sendMessage(MESSAGE);
                }
            }
            catch(ClassNotFoundException classnot){
                System.err.println("Data received in unknown format");
            }
        }
        catch(IOException | ClassNotFoundException ioException){
            System.out.println("Disconnect with Client " + no);
        }
        finally{
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

    //send a message to the output stream
    private void sendMessage(String msg)
    {
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg + " to Client " + no);
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
