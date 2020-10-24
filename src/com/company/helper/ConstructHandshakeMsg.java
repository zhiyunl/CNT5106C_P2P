package com.company.helper;

public class ConstructHandshakeMsg {
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";

    public byte[] constructHandshake(int socketId) {
        byte[] result = new byte[32];
        byte[] header = peerHeaderValue.getBytes();
        byte[] id = intToByteArray(socketId);

        for (int i = 0; i < 32; i++) {
            if (i < 18) {
                result[i] = header[i];
            }
            else if (i > 27) {
                result[i] = id[i - 28];
            }
            else {
                result[i] = 0;
            }
        }

        return result;
    }

    /**
     * int到byte[] 由高位到低位
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    private byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte[]转int
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    private int byteArrayToInt(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public String getHandshakeHeader(byte[] handshakeMsg) {
        byte[] header = new byte[18];

        System.arraycopy(handshakeMsg, 0, header, 0, 18);

        return new String(header);
    }

    public int getHandshakeId(byte[] handshakeMsg) {
        byte[] id = new byte[4];

        System.arraycopy(handshakeMsg, 28, id, 0, 4);

        return byteArrayToInt(id);
    }
}
