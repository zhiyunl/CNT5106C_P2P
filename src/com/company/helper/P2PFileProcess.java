package com.company.helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

/**
 * File Processing and Logging.
 */
public class P2PFileProcess {

    // parameters for common.cfg
    int NumberOfPreferredNeighbors;
    int UnchokingInterval;
    static int OptimisticUnchokingInterval;
    static String FileName;
    public static int FileSize;
    public static int PieceSize;
    public static byte[][] filePieces; // hold temporary pieces
    // parameters for peerInfo.cfg
    static List<PeerInfo> peers;
    int peerNum; // total # of valid peers
    //Constants for LOG TYPE
    public final static int LOG_CONNECTTO = 1;
    public final static int LOG_CONNECTFROM = 2;
    public final static int LOG_PREFER = 3;
    public final static int LOG_OPTIMISTIC = 4;
    public final static int LOG_UNCHOKING = 5;
    public final static int LOG_CHOKING = 6;
    public final static int LOG_HAVE = 7;
    public final static int LOG_INTEREST = 8;
    public final static int LOG_NOTINTEREST = 9;
    public final static int LOG_DOWNLOAD = 10;
    public final static int LOG_COMPLETE = 11;

    /**
     * Get the piece by index.
     * @param pieceIndex the index for the piece
     * @return the total number
     */
    public static byte[] getPiece(int pieceIndex) {
        return filePieces[pieceIndex];
    }

    /**
     * A struct for peer info
     */
    public static class PeerInfo {
        public int ID,
        port,
        hasFile;
        public String hostName;

        /**
         * Instantiates a new Peer info.
         */
        PeerInfo() {

        }

        /**
         * Instantiates a new Peer info.
         *
         * @param id   the id
         * @param Port the port
         * @param hasF the has file tag
         * @param host the hostname
         */
        PeerInfo(int id, int Port, int hasF, String host) {
            this.ID = id;
            this.port = Port;
            this.hasFile = hasF;
            this.hostName = host;
        }
    }

    /**
     * load file into pieces.
     *
     * @param ID the id
     * @throws IOException the io exception
     */
    public void initPieces(int ID) throws IOException {
        filePieces = new byte[getTotalPieces()][PieceSize];
        int peerIndex = P2PFileProcess.getPeerIndexByID(ID);
        if (peerIndex!=-1){
            String peerFolder = P2PFileProcess.initPeerFolder(ID);
            if (peers.get(peerIndex).hasFile == 1) { // check hasFile field
                byte[] files = Files.readAllBytes(Paths.get(peerFolder + FileName));
                // turn 1d into 2d byte array
                int total = getTotalPieces();
                for (int j = 0; j < total - 1; j++) {
//                    System.arraycopy(files,j * PieceSize,filePieces[j],0,PieceSize);
                    filePieces[j] = Arrays.copyOfRange(files, j * PieceSize, (j + 1) * PieceSize);
                }
//                System.arraycopy(files,(total - 1) * PieceSize, filePieces[total-1],0,files.length- (total - 1)* PieceSize);
                filePieces[total - 1] = Arrays.copyOfRange(files, (total - 1) * PieceSize, files.length);
            }
        }else{
            System.out.println("peer ID Not Found!");
        }
    }
    /**
     * Create folder for the peer.
     *
     * @param ID the id
     * @param str the string to write
     */
    public static void writeBack(int ID, String str){
        int peerIndex = getPeerIndexByID(ID);
        if (peerIndex == - 1) {
            System.out.println("peer ID Not Found!");
        }else{
            String peerFolder = initPeerFolder(ID);
            try (FileWriter fileWriter = new FileWriter(peerFolder + FileName)) {
                    fileWriter.write(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
//    /**
//     * Get the correct project dir for running at both CMD and IDE .
//     *
//     * @return the path string for project
//     */
//    public static String getProjDir(){
//        String path =
//    }
    /**
     * Create folder for the peer.
     *
     * @param ID the id
     * @return the folder path string
     */
    public static String initPeerFolder(int ID){
        String workDir = System.getProperty("user.dir");
        String peerFolder = workDir + File.separator + "peer_" + ID + File.separator;
        File dir = new File(peerFolder);
        if (!dir.exists()) dir.mkdirs();// create dir if not exists.
        // Files.createDirectories(Paths.get(peerFolder));
        return peerFolder;
    }

    /**
     * Combine pieces and write back into file.
     *
     * @param ID the id
     */
    public static void combinePieces(int ID) {
        StringBuilder out = new StringBuilder();
        int total = getTotalPieces();
        for (int j = 0; j < total - 1; j++) {
            // for images/binary, use base64
//            out +=Base64.getEncoder().encodeToString(this.filePieces[j]);
            // for text,use utf-8
            out.append(new String(filePieces[j], StandardCharsets.UTF_8));
        }
//        out+=Base64.getEncoder().encodeToString(Arrays.copyOfRange(this.filePieces[total-1],0,this.FileSize-1-this.PieceSize*(total-1)));
        out.append(new String(Arrays.copyOfRange(filePieces[total - 1], 0, FileSize - PieceSize * (total - 1)), StandardCharsets.UTF_8));
        // write out to files
        writeBack(ID, out.toString());
    }

    /**
     * Get total pieces number.
     *
     * @return the total number
     */
    public static int getTotalPieces() {
        return FileSize % PieceSize == 0 ? FileSize / PieceSize : FileSize / PieceSize + 1;
    }

    /**
     * Read common.cfg into variables.
     *
     * @throws IOException the io exception
     */
    public void CommonCfg() throws IOException {
        // set working directory to current
        String workDir = System.getProperty("user.dir");
        String commonCfg = "Common.cfg";
        String absolutePath = workDir + File.separator + commonCfg;
        System.out.println("common config file path:" + absolutePath);
        try (BufferedReader bReader = new BufferedReader(new FileReader(absolutePath))) {
            String str;
            String[] line;
            int cnt = 0;
            while ((str = bReader.readLine()) != null) {
                line = str.split(" ");
                // check the first string to determine the var to set
                switch (line[0]) {
                    case "NumberOfPreferredNeighbors":
                        this.NumberOfPreferredNeighbors = Integer.parseInt(line[1]);
                        cnt++;
                        break;
                    case "UnchokingInterval":
                        this.UnchokingInterval = Integer.parseInt(line[1]);
                        cnt++;
                        break;
                    case "OptimisticUnchokingInterval":
                        OptimisticUnchokingInterval = Integer.parseInt(line[1]);
                        cnt++;
                        break;
                    case "FileName":
                        FileName = line[1];
                        cnt++;
                        break;
                    case "FileSize":
                        FileSize = Integer.parseInt(line[1]);
                        cnt++;
                        break;
                    case "PieceSize":
                        PieceSize = Integer.parseInt(line[1]);
                        cnt++;
                        break;
                    default:
                        break;
                }
            }
            System.out.println("loaded " + cnt + " parameters");
        } catch (FileNotFoundException e) {
            System.out.println("common.cfg file not found");
        }
    }

    /**
     * read PeerInfo.cfg.
     *
     * @return the proceeding peers' list
     * @throws IOException the io exception
     */
    public List<PeerInfo> PeerInfoCfg() throws IOException {
        peers = new LinkedList<>();
        String workDir = System.getProperty("user.dir");
        String peerInfoCfg = "PeerInfo.cfg";
        String absolutePath = workDir + File.separator + peerInfoCfg;
        System.out.println("Peer Info config file path:" + absolutePath);

        try (BufferedReader bReader = new BufferedReader(new FileReader(absolutePath))) {
            String str;
            int cnt = 0;
            String[] line;
            while ((str = bReader.readLine()) != null) {
                line = str.split(" ");
                // save peer info one by one into linked list
                PeerInfo peer = new PeerInfo();
                peer.ID = Integer.parseInt(line[0]);
                peer.hostName = line[1];
                peer.port = Integer.parseInt(line[2]);
                peer.hasFile = Integer.parseInt(line[3]);
                peers.add(peer);
                cnt++;
            }
            System.out.println("Loaded " + cnt + " peers' information");
            this.peerNum = cnt;
        } catch (FileNotFoundException e) {
            System.out.println("PeerInfo.cfg file not found");
        }

        return peers;
    }

    /**
     * Lookup peer index by ID.
     *
     * @param ID the peer Process ID to generate the file
     * @return index for the peer
     */
    public static int getPeerIndexByID(int ID){
        for (int i = 0; i < peers.size(); i++) {
            if (ID == peers.get(i).ID) { // locate the peers by ID
                return i;
            }
        }
        System.out.println("ID not found");
        return -1; // not found
    }

    /**
     * Test File Generation.
     *
     * @param ID the peer Process ID to generate the file
     */
    public void DataGeneration(int ID) {
        //  use file size information in the common.cfg to generate file before every run
        //  only peerProcess need to generate file for itself, (if hasFile)
        int cnt = FileSize / 20;
        // note below string is of fixed size 20 bytes.
        String fileContent = String.join("", Collections.nCopies(cnt, "this is 20 bytes !!!"));
        int mod = FileSize % 20;
        if (mod != 0) {
            // append '1's for remainder.
            fileContent += String.join("", Collections.nCopies(mod, "1"));
        }
        // check hasFile and write back into file
        if (peers.get(getPeerIndexByID(ID)).hasFile == 1) { // check hasFile field
            writeBack(ID,fileContent);
        } else {
            System.out.println("This peer does not have file!");
        }
    }

    /**
     * Setup Log file and clean log.
     *
     * @param ID the peerProcess id to create the log
     */
    public void LogSetup(int ID) {
        //  peerProcess create log file for itself.
        String workDir = System.getProperty("user.dir");
        String logPath = workDir + File.separator + "log_peer_" + ID + ".log";
        // clean log before every run
        try (FileWriter fw = new FileWriter(logPath)) {
            fw.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write log into files for each peerProcess.
     *
     * @param ID1  the peerProcess id to write the log
     * @param type the log type
     * @param args the args for the log
     *             In connect 1.to 2.from and 4.optimistic 5.unchoking 6.choking 8.interested 9.not interested.
     *                  args is a int: ID2
     *             In 3.preferred
     *                  args is a array of int: preferred neighbors ID
     *             In 7.HAVE
     *                  args is [ID2, pieceID]
     *             In 10.Download
     *                  args is [ID2, pieceID, Total number of pieces]
     *             In 11.Complete
     *                  args is null
     */
    public static void Log(int ID1, int type, int... args) {
        // get current working directory
        String workDir = System.getProperty("user.dir");
        String logPath = workDir + File.separator + "log_peer_" + ID1 + ".log";
        try (FileWriter fw = new FileWriter(logPath, true)) {
            // get timestamp
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            switch (type) {
                case LOG_CONNECTTO:
                    fw.write("[" + ts + "]: Peer " + ID1 + " makes a connection to Peer " + args[0] + ".\n");
                    break;
                case LOG_CONNECTFROM:
                    fw.write("[" + ts + "]: Peer " + ID1 + " is connected from Peer " + args[0] + ".\n");
                    break;
                case LOG_PREFER:
                    fw.write("[" + ts + "]: Peer " + ID1 + " has the preferred neighbors " + Arrays.toString(args) + ".\n");
                    break;
                case LOG_OPTIMISTIC:
                    fw.write("[" + ts + "]: Peer " + ID1 + " has the optimistically unchoked neighbor " + args[0] + ".\n");
                    break;
                case LOG_UNCHOKING:
                    fw.write("[" + ts + "]: Peer " + ID1 + " is unchoked by " + args[0] + ".\n");
                    break;
                case LOG_CHOKING:
                    fw.write("[" + ts + "]: Peer " + ID1 + " is choked by " + args[0] + ".\n");
                    break;
                case LOG_HAVE:
                    fw.write("[" + ts + "]: Peer " + ID1 + " received the 'have' message from " + args[0] + " for the piece " + args[1] + ".\n");
                    break;
                case LOG_INTEREST:
                    fw.write("[" + ts + "]: Peer " + ID1 + " received the 'interested' message from " + args[0] + ".\n");
                    break;
                case LOG_NOTINTEREST:
                    fw.write("[" + ts + "]: Peer " + ID1 + " received the 'not interested' message from " + args[0] + ".\n");
                    break;
                case LOG_DOWNLOAD:
                    fw.write("[" + ts + "]: Peer " + ID1 + " has downloaded the piece " + args[1] + " from " + args[0] +
                            ". Now the number of pieces it has is " + args[2] + ".\n");
                    break;
                case LOG_COMPLETE:
                    fw.write("[" + ts + "]: Peer " + ID1 + " has downloaded the complete file.\n");
                    break;
                default:
                    System.out.println("wrong log type!");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
