package com.company.helper;

import com.company.impl.Main;
import com.company.peer.Peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class P2PMessageProcess {
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";
    private int id;
    private byte[] field;
    public static Map<Integer, Peer> peerMap = new HashMap<>();
    private static final int MSG_CHOKE= 0;
    private static final int MSG_UN_CHOKE= 1;
    private static final int MSG_INTERESTED= 2;
    private static final int MSG_NOT_INTERESTED= 3;
    private static final int MSG_HAVE= 4;
    private static final int MSG_BIT_FIELD= 5;
    private static final int MSG_REQUEST= 6;
    private static final int MSG_PIECE = 7;

    public P2PMessageProcess(int id, byte[] field) {
        this.id = id;
        this.field = field;
    }

    /**
     * send handshake message
     * @param out output stream to send message
     *
     */
    public void sendHandShakeMsg(ObjectOutputStream out) {
        byte[] result = new byte[32];
        byte[] header = peerHeaderValue.getBytes();
        byte[] idArray = intToByteArray(id);

        for (int i = 0; i < 32; i++) {
            if (i < 18) {
                result[i] = header[i];
            }
            else if (i > 27) {
                result[i] = idArray[i - 28];
            }
            else {
                result[i] = 0;
            }
        }

        sendMessage(result, out);
    }

    /**
     * send BitField message
     * @param out output stream to send message
     *
     */
    public void sendBitField(ObjectOutputStream out) {
        if (!isEmptyBitField()) {
            return;
        }

        byte[] result = new byte[field.length + 5];
        byte[] fieldLength = intToByteArray(field.length);
        byte[] type = intToByteArray(5);

        for (int i = 0; i < result.length; i++) {
            if (i < 4) {
                result[i] = fieldLength[i];
            }
            else if (i == 4) {
                result[i] = type[3];
            }
            else {
                result[i] = field[i - 5];
            }
        }

        sendMessage(result, out);
    }

    /**
     * generate and send actual choke, unchoke, interested, not interested msg
     * @param type one of the four types
     * @param out output stream to send message
     *
     */
    public void sendActualMsg(int type, ObjectOutputStream out) {
        byte[] message = new byte[5];
        byte[] fieldLength = intToByteArray(field.length);

        for (int i = 0; i < message.length; i++) {
            if (i < 4) {
                message[i] = fieldLength[i];
            }
            else {
                message[i] = intToByteArray(type)[3];
            }
        }
        sendMessage(message, out);
    }

    /**
     * decide interested to the peer or not
     * @param message message received from peer
     * @param out output stream to send message
     *
     */
    public void interestOrNot(byte[] message, ObjectOutputStream out) {
        boolean flag;
        flag = false;
        for (int i = 0; i < field.length; i++) {
            if (message[i] == 1 && field[i] == 0) {
                sendActualMsg(MSG_INTERESTED, out);
                flag = true;
                break;
            }
        }
        if (!flag) {
            sendActualMsg(MSG_NOT_INTERESTED, out);
        }
    }

    /**
     * handle actual message in this function
     * @param in input stream to receive message
     * @param out output stream to send message
     * @param peerID peer ID
     *
     */
    public void handleActualMsg(ObjectInputStream in, ObjectOutputStream out, int peerID) throws IOException {
        boolean flag;
        byte[] pieceID = new byte[4]; // received piece ID in have and piece
        byte[] peerBitfield; // corresponding bitfield

        while(true)
        {
            //message received from the client
            try {
                byte[] message = (byte[]) in.readObject();
                int type = message[4];

                switch (type){
                    case MSG_CHOKE:
                        System.out.println("choke time");
                        break;
                    case MSG_UN_CHOKE:
                        System.out.println("unchoke time");
                        break;
                    case MSG_INTERESTED:
                        System.out.println("interested time");
                        break;
                    case MSG_NOT_INTERESTED:
                        System.out.println("not_interested time");
                        break;
                    case MSG_HAVE:
                        System.out.println("have time");
                        // update peer bitfield
                        System.arraycopy(message, 5, pieceID, 0, 4);
                        peerBitfield = Main.peersBitField.get(peerID);
                        peerBitfield[byteArrayToInt(pieceID)-1] = 1;

                        // send an interested/non-interested message
                        interestOrNot(Main.peersBitField.get(peerID), out);
                        break;
                    case MSG_BIT_FIELD:
                        System.out.println("Received bitfield: ");
                        for (byte b : message) {
                            System.out.print(b + " ");
                        }

                        // store peers bitfield
                        byte[] peerField = new byte[field.length];
                        System.arraycopy(message, 5, peerField, 0, field.length);
                        Main.peersBitField.put(peerID, peerField);

                        // send an interested/non-interested message
                        interestOrNot(Main.peersBitField.get(peerID), out);
                        break;
                    case MSG_REQUEST:
                        System.out.println("received request from client");
                        // TODO parse the request and get the pieceIndex

                        // send out piece
                        int pieceIndex = 5;
//                        P2PFileProcess p2pFile = new P2PFileProcess();
//                        SendPiece.sendPiece(pieceIndex,out,p2pFile);
                        break;
                    case MSG_PIECE:
                        System.out.println("piece time");
                        // update this bitfield
                        System.arraycopy(message, 5, pieceID, 0, 4);
                        this.field[byteArrayToInt(pieceID)-1] = 1;
                        // send non-interested message or not after receiving piece
                        flag = false;
                        for (Integer key : Main.peersBitField.keySet()) {
                            peerBitfield = Main.peersBitField.get(key);
                            for (int j = 0; j < field.length; j++) {
                                if (peerBitfield[j] == 1 && field[j] == 0) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag) {
                                // send non-interested to peer i
                                sendActualMsg(MSG_NOT_INTERESTED, peerMap.get(key).getOut());
                            }
                        }
                        // send have to all neighbors
                        // TBD
                        break;
                    default: break;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * transfer int to byte array
     * @param i input integer that we need to transfer
     * @return byte array
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * transfer byte to int array
     * @param bytes input byte array that we need to transfer
     * @return int value
     */
    private int byteArrayToInt(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * get peer's header from the handshake message
     * @param handshakeMsg handshake message that we received
     * @return string value
     */
    public String getHandshakeHeader(byte[] handshakeMsg) {
        byte[] header = new byte[18];

        System.arraycopy(handshakeMsg, 0, header, 0, 18);

        return new String(header);
    }

    /**
     * get peer's id from the handshake message
     * @param handshakeMsg handshake message that we received
     * @return int value
     */
    public int getHandshakeId(byte[] handshakeMsg) {
        byte[] id = new byte[4];

        System.arraycopy(handshakeMsg, 28, id, 0, 4);

        return byteArrayToInt(id);
    }

    /**
     * identify whether it has pieces
     * @return boolean value
     */
    public boolean isEmptyBitField() {
        boolean result = false;

        for (byte b : field) {
            if (b == 1) {
                result = true;
            }
        }

        return result;
    }

    /**
     * send message to other peers
     * @param
     * message byte message that will be sent
     * @param
     * out output stream
     *
     */
    private void sendMessage(byte[] message, ObjectOutputStream out)
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
