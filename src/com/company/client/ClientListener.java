package com.company.client;

public class ClientListener extends Thread {
    private int[] socketList;
    private int[] peerList;
    private int id;
    public ClientListener(int[] socketList, int[] peerList, int id) {
        this.socketList = socketList;
        this.peerList = peerList;
        this.id = id;
    }

    public void run() {
        for (int i = 0; i < peerList.length && peerList[i] < id; i++) {
            new Client(socketList[i]).start();
        }
    }

}
