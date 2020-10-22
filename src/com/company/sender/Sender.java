package com.company.sender;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sender extends Thread {
    private Socket requestSocket;           //socket connect to the server
    private ObjectOutputStream out;         //stream write to the socket
    private ObjectInputStream in;
    private int socketId;
    public Sender(int socketId) {
        this.socketId = socketId;
    }

    public void run() {
        try {
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", socketId);
            System.out.println("Connected to localhost in port " + socketId);
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("Hello, please input a sentence: ");
                //read a sentence from the standard input
                //message send to the server
                String message = bufferedReader.readLine();
                //Send the sentence to the server
                sendMessage(message);
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

        //send a message to the output stream
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
}
