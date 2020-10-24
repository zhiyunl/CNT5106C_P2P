package com.company.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerListener extends Thread{
    private int id;
    private int sPort;
    public ServerListener(int id, int sPort) {
        this.id = id;
        this.sPort = sPort;
    }

    public void run() {
        try {
            System.out.println(id + " begin to listen connection request");
            ServerSocket listener = new ServerSocket(sPort);
            int clientNum = 1;
            while (true) {
                new Server(listener.accept(), clientNum, id).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
