package com.company.helper;

import java.io.IOException;
import java.io.ObjectOutputStream;

import static com.company.helper.P2PMessageProcess.intToByteArray;

public class SendPiece {
    final static int MSG_PIECE = 7;
    /**
     * server send piece to client
     * @param pieceIndex index of the requested piece
     *
     */
    public static void sendPiece(int pieceIndex, ObjectOutputStream out,P2PFileProcess p2pFile){
        // TODO check piece index is valid

        byte[] msg = new byte[5+p2pFile.PieceSize];
        byte[] msgLength = intToByteArray(p2pFile.FileSize);
        byte[] type = intToByteArray(MSG_PIECE);

        System.arraycopy(msgLength,0,msg,0,4);
        System.arraycopy(type,0,msg,4,1);
        System.arraycopy(p2pFile.getPiece(pieceIndex),0,msg,5,p2pFile.PieceSize);
        sendMessage(msg, out);

    }
    private static void sendMessage(byte[] message, ObjectOutputStream out)
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
