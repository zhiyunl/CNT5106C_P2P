package com.company.helper;

import com.company.impl.Main;
import com.company.peer.Peer;
import com.company.timer.CountTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2PMessageProcess {
    private static final String peerHeaderValue = "P2PFILESHARINGPROJ";
    public static int id;
    //private long optimalTimer = System.currentTimeMillis();
    //private long UnChokeTimer = System.currentTimeMillis();
    private static Set<Integer> UnChokeSet = new HashSet<>(); // the peer UnChoked
    private static Set<Integer> optimalUnChokeSet = new HashSet<>(); // the peer optimal UnChoked
    //private Set<Integer> chokeMeSet; // the peer that choked me
    private boolean firstInterestedFlag = false;
    private CountTask unChokeCountTask;
    private CountTask optimalUnChokeCountTask;
    private static Map<Integer, Peer> peerMap = new ConcurrentHashMap<>();
    private static Map<Integer, List<Integer>> interestingMap = new ConcurrentHashMap<>();
    private static Map<Integer, Integer> interestedInMe = new ConcurrentHashMap<>(); // the peers who interested in this peer
    private static final int MSG_CHOKE = 0;
    private static final int MSG_UN_CHOKE = 1;
    private static final int MSG_INTERESTED = 2;
    private static final int MSG_NOT_INTERESTED = 3;
    private static final int MSG_HAVE = 4;
    private static final int MSG_BIT_FIELD = 5;
    private static final int MSG_REQUEST = 6;
    private static final int MSG_PIECE = 7;
    private static final int MSG_FINISH = 8;
    private static final int MSG_ALL_FINISH = 9;

    public P2PMessageProcess(int id) {
        P2PMessageProcess.id = id;
    }

    /**
     * send handshake message
     *
     * @param out output stream to send message
     */
    public void sendHandShakeMsg(ObjectOutputStream out) {
        byte[] result = new byte[32];
        byte[] header = peerHeaderValue.getBytes();
        byte[] idArray = intToByteArray(id);

        for (int i = 0; i < 32; i++) {
            if (i < 18) {
                result[i] = header[i];
            } else if (i > 27) {
                result[i] = idArray[i - 28];
            } else {
                result[i] = 0;
            }
        }

        sendMessage(result, out);
    }

    /**
     * initialize peer map
     *
     * @param peerId ID of peer
     * @param peer   object of peer
     */
    public void constructPeerMap(int peerId, Peer peer) {
        peerMap.put(peerId, peer);
    }

    /**
     * send BitField message
     *
     * @param out output stream to send message
     */
    public void sendBitField(ObjectOutputStream out) {
        if (!isEmptyBitField()) {
            return;
        }

        byte[] result = new byte[Main.field.length + 5];
        byte[] fieldLength = intToByteArray(Main.field.length);
        byte[] type = intToByteArray(MSG_BIT_FIELD);

        for (int i = 0; i < result.length; i++) {
            if (i < 4) {
                result[i] = fieldLength[i];
            } else if (i == 4) {
                result[i] = type[3];
            } else {
                result[i] = Main.field[i - 5];
            }
        }

        sendMessage(result, out);
    }

    /**
     * send request or have message
     *
     * @param type  indicate the type of actual message
     * @param index indicate the index of piece that it wants to request
     * @param out   output stream to send message
     */
    private void sendRequestHaveMsg(int type, int index, ObjectOutputStream out) {
        byte[] result = new byte[9];
        byte[] length = intToByteArray(4);
        byte[] indexByteArray = intToByteArray(index);

        System.arraycopy(length, 0, result, 0, 4);
        result[4] = intToByteArray(type)[3];
        System.arraycopy(indexByteArray, 0, result, 5, 4);

        sendMessage(result, out);
    }

    /**
     * send piece to another peer
     *
     * @param pieceIndex index of the requested piece
     */
    private void sendPiece(byte[] pieceIndex, ObjectOutputStream out) {
        // msg = length+type+[index+piece]
        byte[] pieceContent = P2PFileProcess.getPiece(byteArrayToInt(pieceIndex));
        int pieceSize = pieceContent.length;
        byte[] msg = new byte[5 + 4 + pieceSize];
        byte[] msgLength = intToByteArray(5 + pieceSize);
        byte type = intToByteArray(MSG_PIECE)[3]; // only last byte

        // ArrayCopy args: source array, start point, des array, start point, length
        System.arraycopy(msgLength, 0, msg, 0, 4); // 1-4 bytes: msg length
        msg[4] = type; // 5th byte: msg type
        System.arraycopy(pieceIndex, 0, msg, 5, 4);// 6-10 bytes: pieceIndex
        // 9 - remaining bytes: piece
        System.arraycopy(pieceContent, 0, msg, 9, pieceSize);
        sendMessage(msg, out);
    }

    /**
     * generate and send actual choke, unchoke, interested, not interested msg
     *
     * @param type one of the four types
     * @param out  output stream to send message
     */
    private void sendActualMsg(int type, ObjectOutputStream out) {
        byte[] message = new byte[5];
        message[4] = intToByteArray(type)[3];
        //byte[] fieldLength = intToByteArray(Main.field.length);

       /* for (int i = 0; i < message.length; i++) {
            if (i < 4) {
                message[i] = fieldLength[i];
            }
            else {
                message[i] = intToByteArray(type)[3];
            }
        }*/
        sendMessage(message, out);
    }

    /**
     * decide interested to the peer or not
     *
     * @param message message received from peer
     * @param out     output stream to send message
     */
    private void interestOrNot(byte[] message, ObjectOutputStream out, int peerID) {
        boolean flag;
        flag = false;
        for (int i = 0; i < Main.field.length; i++) {
            if (message[i] == 1 && Main.field[i] == 0) {
                sendActualMsg(MSG_INTERESTED, out);
                selectRandomPiece(peerID, out);
                flag = true;
                break;
            }
        }
        if (!flag) {
            sendActualMsg(MSG_NOT_INTERESTED, out);
        }
    }

    /**
     * find interesting pieces that this peer doesn't have but its neighbour has. And return the index of piece with list
     *
     * @return List<Integer>
     */
    private List<Integer> findInterestingPieces(int peerId) {
        List<Integer> result = new LinkedList<>();
        byte[] peerList = Main.peersBitField.get(peerId);
        for (int i = 0; i < Main.field.length; i++) {
            if (Main.field[i] == 0 && peerList[i] == 1) {
                result.add(i);
            }
        }

        if (result.size() != 0) {
            return result;
        }
        return null;
    }

    /**
     * select a random piece index from interesting map's list
     */
    private void selectRandomPiece(int peerId, ObjectOutputStream out) {
        synchronized (P2PMessageProcess.class) {
            Random rand = new Random();
            while (interestingMap.containsKey(peerId)) {
                int index = rand.nextInt(interestingMap.get(peerId).size());

                if (Main.field[interestingMap.get(peerId).get(index)] == 0) {
                    //change field bit to 2 in order to represent this piece is requesting from other neighbours
                    Main.field[interestingMap.get(peerId).get(index)] = 2;
                    // TODO 3-a: send request
                    sendRequestHaveMsg(MSG_REQUEST, interestingMap.get(peerId).get(index), out);
                    //delete index from map which has been requested
                    deleteMap(interestingMap.get(peerId).get(index));
                    break;
                }
            }
        }


    }

    /**
     * handle actual message in this function
     *
     * @param in     input stream to receive message
     * @param out    output stream to send message
     * @param peerID peer ID
     */
    public void handleActualMsg(ObjectInputStream in, ObjectOutputStream out, int peerID) {
        byte[] peerBitField; // corresponding BitField
        //optimalTimer = System.currentTimeMillis();
        //UnChokeTimer = System.currentTimeMillis();
        while (true) {
            //TODO 1-d: identify whether system should be terminated
            if (id == Main.firstPeerID && Main.processingList.size() == 0) {
                System.out.println("all peers have finished their task, the system will be terminated");
                for (Integer key : peerMap.keySet()) {
                    sendActualMsg(MSG_ALL_FINISH, peerMap.get(key).getOut());
                }

                P2PFileProcess.Log(id, P2PFileProcess.LOG_STOP, id);
                System.exit(0);
            }

            //message received from the client
            try {
                byte[] message = (byte[]) in.readObject();
                int type = message[4];

                // select preferred neighbors
                //selectPreferredNeighbors();
                // choose the optimistic UnChoke peer
                //selectOptimisticPeer();

                switch (type) {
                    case MSG_CHOKE:
                        System.out.println("received choke, parse and update choke list");
                        P2PFileProcess.Log(id, P2PFileProcess.LOG_CHOKING, peerID);
                        break;
                    case MSG_UN_CHOKE:
                        System.out.println("received unchoke, parse it and update");
                        P2PFileProcess.Log(id, P2PFileProcess.LOG_UNCHOKING, peerID);
                        break;
                    case MSG_INTERESTED:
                        // TODO 2-c,3-d: receive interest
                        System.out.println("interested time");
                        P2PFileProcess.Log(id, P2PFileProcess.LOG_INTEREST, peerID);
                        if (!interestedInMe.containsKey(peerID)) {
                            interestedInMe.put(peerID, 0); // initially, 0 piece received from peerID
                        }

                        synchronized (P2PMessageProcess.class) {

                            if (!firstInterestedFlag) {
                                Timer time = new Timer();
                                Date now = new Date();
                                // TODO 2-d: send choke/unchoke every p seconds
                                unChokeCountTask = new CountTask(0, this);
                                // TODO 2-e: set optimistically unchoke neighbor every m seconds
                                optimalUnChokeCountTask = new CountTask(1, this);
                                time.schedule(unChokeCountTask, now, 1000 * P2PFileProcess.UnchokingInterval);
                                time.schedule(optimalUnChokeCountTask, now, 1000 * P2PFileProcess.OptimisticUnchokingInterval);
                                firstInterestedFlag = true;
                            }
                        }

//                        System.out.println("after put: " + interestedInMe);

                        break;
                    case MSG_NOT_INTERESTED:
                        // TODO 2-c,3-c: receive not interest
                        System.out.println("not_interested time");
                        P2PFileProcess.Log(id, P2PFileProcess.LOG_NOTINTEREST, peerID);
                        interestedInMe.remove(peerID);

//                        System.out.println("after remove" + interestedInMe);

                        break;
                    case MSG_HAVE:
                        System.out.println("received have msg, update bit field and send interest/not back");

                        // TODO 3-f: receive have, update peer BitField
                        synchronized (P2PMessageProcess.class) {
                            byte[] havePieceID = new byte[4];
                            System.arraycopy(message, 5, havePieceID, 0, 4);
                            if (!Main.peersBitField.containsKey(peerID)) {
                                Main.peersBitField.put(peerID, new byte[P2PFileProcess.getTotalPieces()]);
                            }
                            peerBitField = Main.peersBitField.get(peerID);
                            peerBitField[byteArrayToInt(havePieceID)] = 1;

                            //log have related message
                            P2PFileProcess.Log(id, P2PFileProcess.LOG_HAVE, peerID, byteArrayToInt(havePieceID));
                            // send an interested/non-interested message
                            interestOrNot(Main.peersBitField.get(peerID), out, peerID);
                        }

                        break;
                    case MSG_BIT_FIELD:
                        // TODO 2-b: receive bit field msg
                        System.out.println("Received bitfield: ");
                        synchronized (P2PMessageProcess.class) {
                            for (byte b : message) {
                                System.out.print(b + " ");
                            }
                            System.out.println();

                            // store peers bitfield
                            byte[] peerField = new byte[Main.field.length];
                            System.arraycopy(message, 5, peerField, 0, Main.field.length);
                            Main.peersBitField.put(peerID, peerField);

                            // TODO 2-c: send interested/non-interested
                            //add interesting part for neighbour into interestingMap
                            List<Integer> interestingPiecesList = findInterestingPieces(peerID);
                            if (interestingPiecesList != null) {
                                interestingMap.put(peerID, interestingPiecesList);
                            }
                            // send an interested/non-interested message
                            if (interestingMap.containsKey(peerID)) {
                                sendActualMsg(MSG_INTERESTED, out);
                                selectRandomPiece(peerID, out);
                            } else {
                                sendActualMsg(MSG_NOT_INTERESTED, out);
                            }
                            //interestOrNot(Main.peersBitField.get(peerID), out);
                        }


                        break;
                    case MSG_REQUEST:
                        // TODO 3-a: receive request msg
                        while (true) {
                            if (UnChokeSet.contains(peerID) || optimalUnChokeSet.contains(peerID)) {
                                break;
                            }
                        }

                        System.out.println("received request, parse and send piece back.");
                        byte[] requestPieceID = new byte[4];
                        System.arraycopy(message, 5, requestPieceID, 0, 4);
                        // TODO 3-e:send out piece
                        sendPiece(requestPieceID, out);
                        break;
                    case MSG_PIECE:
                        // TODO 3-e: receive piece
                        System.out.println("received piece msg, save it and update bit field.");

                        synchronized (P2PMessageProcess.class) {
                            // received piece number plus 1
                            if (interestedInMe.containsKey(peerID)) {
                                interestedInMe.put(peerID, interestedInMe.get(peerID) + 1);
                            } else {
                                interestedInMe.put(peerID, 1);
                            }

                            // save piece
                            byte[] pieceID = new byte[4];
                            System.arraycopy(message, 5, pieceID, 0, 4); // get pieceID (byte[])
                            // git the index of piece
                            int pieceIDInt = byteArrayToInt(pieceID);
                            // update this BitField
                            Main.field[pieceIDInt] = 1;

                            //delete related index in map which has been got
                            deleteMap(pieceIDInt);

                            // save the piece into filePieces by index
                            System.arraycopy(message, 9, P2PFileProcess.filePieces[pieceIDInt], 0, Math.min(message.length - 9, P2PFileProcess.filePieces[pieceIDInt].length));

                            //TODO 3-b: send have message to neighbours
                            sendRequestHaveMsg(MSG_HAVE, pieceIDInt, out);

                            // send non-interested message or not after receiving piece
                            //flag = false;
                            for (Integer key : Main.peersBitField.keySet()) {
                                peerBitField = Main.peersBitField.get(key);
                                interestOrNot(peerBitField, peerMap.get(key).getOut(), peerID);
                            }
                            // send have to all neighbors

                            // get current # of pieces
                            int sum = 0;
                            for (byte a : Main.field) {
                                if (a == 1) {
                                    sum += a;
                                }
                            }
                            P2PFileProcess.Log(id, P2PFileProcess.LOG_DOWNLOAD, peerID, pieceIDInt, sum);

                            //identify combine condition
                            boolean combineFlag = true;
                            for (byte b : Main.field) {
                                if (b == 0 || b == 2) {
                                    combineFlag = false;
                                }
                            }
                            //TODO 1-d: identify combine pieces into a file when the condition is satisfied
                            if (combineFlag) {

                                System.out.println("the file has been completed");
                                P2PFileProcess.Log(id, P2PFileProcess.LOG_COMPLETE);

                                //write back message to file
                                P2PFileProcess.combinePieces(id);

                                //send finish message to first peer
                                sendActualMsg(MSG_FINISH, peerMap.get(Main.firstPeerID).getOut());
                            }

                        }

                        break;
                    case MSG_FINISH:
                        System.out.println("The neighbour "+peerID+" has finished his task");
                        Main.processingList.remove(Integer.valueOf(peerID));
                        break;
                    case MSG_ALL_FINISH:
                        System.out.println("ALL peers have finished his task");
                        P2PFileProcess.Log(id, P2PFileProcess.LOG_STOP, id);
                        System.exit(0);
                        break;
                    default:
                        System.out.println("default msg type, wrong!");
                        break;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * select preferred neighbors every UnChoke interval time
     */
    public synchronized void selectPreferredNeighbors() {
        if (interestedInMe != null && interestedInMe.size() > 0) {
            // check whether the peer has complete file
            boolean flag = true; // default it has the complete file
            for (int i = 0; i < Main.field.length; i++) {
                if (Main.field[i] == 0) {
                    flag = false;
                    break;
                }
            }

            if (!flag) {
                // compare download rate and send UnChoke/choke messages
                compareDownloadRate();
            } else {
                // randomly select and send UnChoke/choke messages
                ArrayList<Integer> neighbors = new ArrayList<>(interestedInMe.keySet());

                // choose random peer
                int[] arr = new int[P2PFileProcess.NumberOfPreferredNeighbors];
                int neighborsSize = neighbors.size();
                for (int i = 0; i < arr.length; i++) {
                    if (i < neighborsSize) {
                        Random r = new Random();
                        int uc = r.nextInt(neighbors.size());
                        int ID = neighbors.get(uc);
                        arr[i] = ID;
                        // send UnChoke message, only send if the peerID is not already unchoked
                        if (!UnChokeSet.contains(ID)) {
                            System.out.println("--------------------compete random UnChoke part-------------------------------");
                            sendActualMsg(MSG_UN_CHOKE, peerMap.get(ID).getOut());
                            //P2PFileProcess.Log(id, P2PFileProcess.LOG_UNCHOKING, ID);
                        }
                        neighbors.remove(uc);
                    }
                }
                // send choke message to not selected neighbors
                for (Integer i : neighbors) {
                    sendActualMsg(MSG_CHOKE, peerMap.get(i).getOut());
                    //P2PFileProcess.Log(id, P2PFileProcess.LOG_CHOKING, i);
                }
                // update UnChoke set
                if (!UnChokeSet.isEmpty()) {
                    UnChokeSet.clear();
                }

                P2PFileProcess.Log(id, P2PFileProcess.LOG_PREFER, arr);

                for (int i1 : arr) {
                    UnChokeSet.add(i1);
                }
            }

            //after one interval, we should make interested me map's value(pieces that it received) to be 0
            clearMapValue();
            //UnChokeTimer = System.currentTimeMillis();
        }
    }

    /**
     * calculate and compare download rate, select peers with the highest download rate
     */
    private synchronized void compareDownloadRate() {
        // calculate download rates of all peers interested in this peer
        HashMap<Integer, Double> downloadRates = new HashMap<>();
        for (Map.Entry<Integer, Integer> peer : interestedInMe.entrySet()) {
            double dr = (peer.getValue() * P2PFileProcess.PieceSize * 1.0) / P2PFileProcess.UnchokingInterval;
            downloadRates.put(peer.getKey(), dr);
        }

        // sort the download rates
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(downloadRates.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        for (Map.Entry<Integer, Double> mapping : list) {
            System.out.println(mapping.getKey() + ": " + mapping.getValue());
        }

        // select NumberOfPreferredNeighbors peers with highest rate
        int[] arr = new int[P2PFileProcess.NumberOfPreferredNeighbors];
        int num = 0;
        List<Map.Entry<Integer, Double>> deleteUnChokeList = new LinkedList<>();
        for (Map.Entry<Integer, Double> mapping : list) {
            if (num < arr.length) {
                arr[num] = mapping.getKey();
                if (!UnChokeSet.contains(mapping.getKey())) {
                    System.out.println("--------------------compete rate UnChoke part-------------------------------");
                    sendActualMsg(MSG_UN_CHOKE, peerMap.get(mapping.getKey()).getOut());
                    //P2PFileProcess.Log(id, P2PFileProcess.LOG_UNCHOKING, mapping.getKey());
                }
                deleteUnChokeList.add(mapping);
                num++;
            } else {
                break;
            }
        }

        for (Map.Entry<Integer, Double> integerDoubleEntry : deleteUnChokeList) {
            list.remove(integerDoubleEntry);
        }

        // send choke message to not selected neighbors
        if (list.size() > 0) {
            for (Map.Entry<Integer, Double> mapping : list) {
                sendActualMsg(MSG_CHOKE, peerMap.get(mapping.getKey()).getOut());
                //P2PFileProcess.Log(id, P2PFileProcess.LOG_CHOKING, mapping.getKey());
            }
        }

        // update UnChoke set
        if (!UnChokeSet.isEmpty()) {
            UnChokeSet.clear();
        }
        for (int i1 : arr) {
            UnChokeSet.add(i1);
        }
    }

    /**
     * reselect a new random optimistic unchoke peer
     */
    public synchronized void selectOptimisticPeer() {
        if (interestedInMe != null && interestedInMe.size() > 0) {
            // save all possible peers
            List<Integer> optimalPeers = new ArrayList<>();
            for (int peerID : interestedInMe.keySet()) {
                if (!UnChokeSet.contains(peerID)) {
                    optimalPeers.add(peerID);
                }
            }
            // choose random peer
            if (optimalPeers.size() > 0) {
                Main.optimalPeer = optimalPeers.get(new Random().nextInt(optimalPeers.size()));
                // send out UnChoke msg
                System.out.println("--------------------optimal UnChoke part-------------------------------");
                sendActualMsg(MSG_UN_CHOKE, peerMap.get(Main.optimalPeer).getOut());

                if (!optimalUnChokeSet.isEmpty()) {
                    optimalUnChokeSet.clear();
                }

                optimalUnChokeSet.add(Main.optimalPeer);

                //optimalTimer = System.currentTimeMillis(); // update time
                P2PFileProcess.Log(id, P2PFileProcess.LOG_OPTIMISTIC, Main.optimalPeer);
            }

        }
    }

    /**
     * delete related index from map
     *
     * @param pieceIDInt input integer that represent index that we want to delete
     */
    private void deleteMap(int pieceIDInt) {
        synchronized (P2PMessageProcess.class) {
            List<Integer> nullList = new LinkedList<>();
            for (Integer ID : interestingMap.keySet()) {
                //remove interesting part
                if (interestingMap.get(ID).contains(pieceIDInt)) {
                    interestingMap.get(ID).remove(Integer.valueOf(pieceIDInt));
                }
                //record map whose value list is null
                if (interestingMap.get(ID).size() == 0) {
                    nullList.add(ID);
                }
            }
            //delete related map whose value list is null
            for (Integer ID : nullList) {
                interestingMap.remove(ID);
            }
        }

    }

    /**
     * transfer int to byte array
     *
     * @param i input integer that we need to transfer
     * @return byte array
     */
    private static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * transfer byte to int array
     *
     * @param bytes input byte array that we need to transfer
     * @return int value
     */
    private int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * clear interested me map's value which make it be 0
     */
    private synchronized void clearMapValue() {
        for (Integer id : interestedInMe.keySet()) {
            interestedInMe.put(id, 0);
        }
    }

    /**
     * get peer's header from the handshake message
     *
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
     *
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
     *
     * @return boolean value
     */
    private boolean isEmptyBitField() {
        boolean result = false;

        for (byte b : Main.field) {
            if (b == 1) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * send message to other peers
     *
     * @param message byte message that will be sent
     * @param out     output stream
     */
    private void sendMessage(byte[] message, ObjectOutputStream out) {
        try {
            //stream write the message
            out.writeObject(message);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
