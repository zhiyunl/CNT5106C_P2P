package com.company.helper;

import java.io.*;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * File Processing and Logging.
 */
public class P2PFileProcess {
    // parameters for common.cfg
    int NumberOfPreferredNeighbors;
    int UnchokingInterval;
    int OptimisticUnchokingInterval;
    String FileName;
    public int FileSize;
    public int PieceSize;

    // parameters for peerInfo.cfg
    List<PeerInfo> peers;
    int peerNum; // total # of valid peers

    //Constants for LOG TYPE
    final int LOG_CONNECTTO= 1;
    final int LOG_CONNECTFROM= 2;
    final int LOG_PREFER= 3;
    final int LOG_OPTIMISTIC= 4;
    final int LOG_UNCHOKING= 5;
    final int LOG_CHOKING= 6;
    final int LOG_HAVE = 7;
    final int LOG_INTEREST = 8;
    final int LOG_NOTINTEREST = 9;
    final int LOG_DOWNLOAD = 10;
    final int LOG_COMPLETE = 11;

    /**
     * A struct for peer info
     */
    public static class PeerInfo{
        public int ID,
        port,
        hasFile;
        public String hostName;

        /**
         * Instantiates a new Peer info.
         */
        PeerInfo(){

        }

        /**
         * Instantiates a new Peer info.
         *
         * @param id   the id
         * @param Port the port
         * @param hasF the has f
         * @param host the host
         */
        PeerInfo(int id, int Port, int hasF, String host){
            this.ID = id;
            this.port = Port;
            this.hasFile = hasF;
            this.hostName = host;
        }
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
        System.out.println("common config file path:"+absolutePath);
        try(BufferedReader bReader = new BufferedReader(new FileReader(absolutePath))) {
            String str;
            String[] line;
            int cnt = 0;
            while ((str = bReader.readLine())!= null){
                line = str.split(" ");
                // check the first string to determine the var to set
                switch (line[0]){
                    case "NumberOfPreferredNeighbors":
                        this.NumberOfPreferredNeighbors = Integer.parseInt(line[1]);cnt++;break;
                    case "UnchokingInterval":
                        this.UnchokingInterval= Integer.parseInt(line[1]);cnt++;break;
                    case "OptimisticUnchokingInterval":
                        this.OptimisticUnchokingInterval= Integer.parseInt(line[1]);cnt++;break;
                    case "FileName":
                        this.FileName= line[1];cnt++;break;
                    case "FileSize":
                        this.FileSize= Integer.parseInt(line[1]);cnt++;break;
                    case "PieceSize":
                        this.PieceSize= Integer.parseInt(line[1]);cnt++;break;
                    default: break;
                }
            }
            System.out.println("loaded "+cnt+" parameters");
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
    public List<PeerInfo> PeerInfoCfg() throws IOException{
        this.peers = new LinkedList<>();
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
                this.peers.add(peer);
                /*this.peers[cnt] = new PeerInfo();
                this.peers[cnt].ID = Integer.parseInt(line[0]);
                this.peers[cnt].hostName = line[1];
                this.peers[cnt].port = Integer.parseInt(line[2]);
                this.peers[cnt].hasFile = Integer.parseInt(line[3]);*/
                cnt++;
            }
            System.out.println("Loaded "+cnt+" peers' information");
            this.peerNum = cnt;
        } catch (FileNotFoundException e) {
            System.out.println("PeerInfo.cfg file not found");
        }

        return this.peers;
    }

    /**
     *  Test File Generation.
     *
     * @param ID the peer Process ID to generate the file
     * @throws IOException the io exception
     */
    public void DataGeneration(int ID) throws IOException {
        //  use file size information in the common.cfg to generate file before every run
        //  only peerProcess need to generate file for itself, (if hasFile)
        int cnt = this.FileSize/20;
        // note below string is of fixed size 20 bytes.
        //String fileContent = "this is 20 bytes !!!".repeat(cnt);
        String fileContent = String.join("", Collections.nCopies(cnt, "this is 20 bytes !!!"));
        int mod = this.FileSize % 20;
        if (mod != 0){
            // append '1's for remainder.
            //fileContent += "1".repeat(mod);
            fileContent += String.join("", Collections.nCopies(mod, "1"));
        }
        // check ID and hasFile
        for (int i = 0; i < peers.size(); i++) {
            if (ID == peers.get(i).ID){ // locate the peers by ID
                String workDir = System.getProperty("user.dir");
                String peerFolder = workDir + File.separator + "peer_"+peers.get(i).ID+File.separator;
                System.out.println("data file path:" + peerFolder);
//                Files.createDirectories(Paths.get(peerFolder));
                File dir = new File(peerFolder);
                if (!dir.exists()) dir.mkdirs();// create dir if not exists.
                if (peers.get(i).hasFile==1){ // check hasFile field
                    try (FileWriter fileWriter = new FileWriter(peerFolder+this.FileName)) {
                        fileWriter.write(fileContent);
                    }
                }else{
                    System.out.println("This peer does not have file!");
                }
                break;
            }
            if(i==this.peerNum-1){
                System.out.println("peer ID Not Found!");
            }
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
        String logPath = workDir+File.separator+"log_peer_"+ID+".log";
//        File log = new File(logPath);
//        log.createNewFile(); // this will not clean the log
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
     *                   args is a int: ID2
     *             In 3.preferred
     *                   args is a array of int: preferred neighbors ID
     *             In 7.HAVE
     *                  args is [ID2, piece Number]
     *             In 10.Download
     *                  args is [ID2, piece Number, Total number of pieces]
     *             In 11.Complete
     *                  args is null
     */
    public void Log(int ID1, int type, int ... args){
        // get current working directory
        String workDir = System.getProperty("user.dir");
        String logPath = workDir+File.separator+"log_peer_"+ID1+".log";
        try (FileWriter fw = new FileWriter(logPath,true)) {
            // get timestamp
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            switch (type){
                case LOG_CONNECTTO:
                    fw.write("["+ts+"]: Peer "+ID1+" makes a connection to Peer "+args[0]+".\n");
                    break;
                case LOG_CONNECTFROM:
                    fw.write("["+ts+"]: Peer "+ID1+" is connected from Peer "+args[0]+".\n");
                    break;
                case LOG_PREFER:
                    fw.write("["+ts+"]: Peer "+ID1+" has the preferred neighbors "+ Arrays.toString(args)+".\n");
                    break;
                case LOG_OPTIMISTIC:
                    fw.write("["+ts+"]: Peer "+ID1+" has the optimistically unchoked neighbor "+ args[0]+".\n");
                    break;
                case LOG_UNCHOKING:
                    fw.write("["+ts+"]: Peer "+ID1+" is unchoked by "+ args[0]+".\n");
                    break;
                case LOG_CHOKING:
                    fw.write("["+ts+"]: Peer "+ID1+" is choked by "+ args[0]+".\n");
                    break;
                case LOG_HAVE:
                    fw.write("["+ts+"]: Peer "+ID1+" received the 'have' message from "+args[0] +"for the piece "+ args[1]+".\n");
                    break;
                case LOG_INTEREST:
                    fw.write("["+ts+"]: Peer "+ID1+" received the 'interested' message from "+args[0] +".\n");
                    break;
                case LOG_NOTINTEREST:
                    fw.write("["+ts+"]: Peer "+ID1+" received the 'not interested' message from "+args[0] +".\n");
                    break;
                case LOG_DOWNLOAD:
                    fw.write("["+ts+"]: Peer "+ID1+" has downloaded the piece "+args[1]+" from "+args[0]+
                            ". Now the number of pieces it has is "+args[2] +".\n");
                    break;
                case LOG_COMPLETE:
                    fw.write("["+ts+"]: Peer "+ID1+" has downloaded the complete file.\n");
                    break;
                default:System.out.println("wrong log type!");break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
