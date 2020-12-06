# CNT5106C_P2P
This is a course project for CNT5106C Computer Networks (FALL 2020)

### Usage
#### Terminal
Put cfg files under *$PROJ_DIR$* directory, cd to *$PROJ_DIR$*. e.g CNT5106C_P2P

To build, use
```
find . -name "*.java" > build.txt
javac -d . @build.txt
```
To Run, use
```
java com.company.impl.Main 1001
```
Here 1001 is the ID for the peer.

Note: you need to start each peer **in order**, as described in *PeerInfo.cfg*

### To deploy compiled binaries onto server and run
make sure you have generated your own ssh key pair, and have it uploaded onto server.
Change the $username in *deploy.sh* to your own username to login lin114-00 server.
```
bash deploy.sh
```
