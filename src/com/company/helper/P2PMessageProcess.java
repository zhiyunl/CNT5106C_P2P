package com.company.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class P2PMessageProcess {
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";
    private int id;
    private byte[] field;
    private PeersBitfield peersBitfield;

    final int choke= 0;
    final int unchoke= 1;
    final int interested= 2;
    final int not_interested= 3;
    final int have= 4;
    final int bitfield= 5;
    final int request= 6;
    final int piece = 7;

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
     * generate actual choke, unchoke, interested, not interested msg
     * @param type one of the four types
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
     * handle actual message in this function
     * @param in input stream to receive message
     *
     */
    public void handleActualMsg(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        boolean flag;

        while(true)
        {
            //message received from the client
            try {
                byte[] message = (byte[]) in.readObject();
                int type = message[4];

                switch (type){
                    case choke:
                        System.out.println("choke time");
                        break;
                    case unchoke:
                        System.out.println("unchoke time");
                        break;
                    case interested:
                        System.out.println("interested time");
                        break;
                    case not_interested:
                        System.out.println("not_interested time");
                        break;
                    case have:
                        System.out.println("have time");
                        // send an interested/non-interested message
                        flag = false;
                        for (int i = 0; i < field.length; i++) {
                            if (message[i+5] == 1 && field[i] == 0) {
                                sendActualMsg(interested, out);
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            sendActualMsg(not_interested, out);
                        }
                        break;
                    case bitfield:
                        System.out.println("Received bitfield: ");
                        for (byte b : message) {
                            System.out.print(b + " ");
                        }
                        // add new entry to peersBitfield
                        // peersBitfield.addNewEntry(发送方id, message);
                        // send an interested/non-interested message
                        flag = false;
                        for (int i = 0; i < field.length; i++) {
                            if (message[i+5] == 1 && field[i] == 0) {
                                sendActualMsg(interested, out);
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            sendActualMsg(not_interested, out);
                        }
                        break;
                    case request:
                        System.out.println("request time");
                        break;
                    case piece:
                        System.out.println("piece time");
                        // send non-interested message or not after receiving piece
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
