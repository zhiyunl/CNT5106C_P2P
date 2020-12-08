# CNT5106C_P2P
This is a course project for CNT5106C Computer Networks (FALL 2020)

### Using script to compile, deploy and run on all servers
1. Put cfg files and file folders under *$PROJ_DIR$* directory, cd to *$PROJ_DIR$*. e.g. CNT5106C_P2P
1. make sure you have generated your own ssh key pair, and have it uploaded onto server.
   Otherwise, you need to enter password for each login.
2. Change the $user in *deploy.sh* to your own username to login lin114-00 server.
```
bash deploy.sh
```

### Manual deploy or local test in terminal
1. Upload all src code and files onto server

To build, use
```
find . -name "*.java" > build.txt
javac -d . @build.txt
```
To run, use
```
java com.company.impl.Main 1001
```
Here 1001 is the ID for the peer.

Note: you need to start each peer **in order**, as described in *PeerInfo.cfg*


