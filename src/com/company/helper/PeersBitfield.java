package com.company.helper;

import java.util.ArrayList;

public class PeersBitfield {
    private int id;
    private byte[] field;
    private ArrayList<byte[]> peersBitfield;

    public PeersBitfield(int id, byte[] field) {
        this.id = id;
        this.field = field;
        this.peersBitfield = new ArrayList<byte[]>(); // assume only allows 20 peers
    }

    public void addNewEntry(int ID, byte[] peerField) {
        byte[] peerId = P2PMessageProcess.intToByteArray(ID);
        byte[] newEntry = new byte[peerId.length+peerField.length];
        System.arraycopy(peerId,0,newEntry,0,peerId.length);
        System.arraycopy(peerField,0,newEntry,peerId.length,peerField.length);
        System.out.println(newEntry);
        peersBitfield.add(newEntry);
        System.out.println(peersBitfield);
    }

    public void updateEntry(int ID, byte[] pieceIndex) {

    }

    public ArrayList<byte[]> getPeersBitfield() {
        return peersBitfield;
    }
}
