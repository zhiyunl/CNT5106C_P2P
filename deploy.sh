#!/bin/bash
### turn on vpn before accessing lin114-xx series of servers.

### ssh remote PC without password
## on your PC (Linux/Mac/Windows)
# ssh-keygen
# ssh-add ~/.ssh/id_rsa
# scp ~/.ssh/id_rsa.pub zhiyun@thunder.cise.ufl.edu:
## on remote PC (thunder.cise.ufl.edu)
# cat id_rsa.pub >> ~/.ssh/authorized_keys
## Done!

user="zhiyun"
### build
find . -name "*.java" > build.txt
javac -d . @build.txt

### deploy binary and config files onto remote servers
### only one server, because they share files
# delete old files
ssh $user@lin114-00.cise.ufl.edu 'rm -rf CNT5106C_P2P'
# create folder
ssh -t $user@lin114-00.cise.ufl.edu 'mkdir CNT5106C_P2P'
# upload
scp -r ./100* ./com Common.cfg PeerInfo.cfg $user@lin114-00.cise.ufl.edu:~/CNT5106C_P2P/
### repeatedly open terminal and run java
IFS=' '
while read line || [ -n "$line" ] ; do
    read -r -a array <<< "$line"
    # echo "${array[1]}"
    # ssh -t zhiyun@${array[1]} "cd CNT5106C_P2P;tmux new-session -d -s ${array[0]} 'java com.company.impl.Main ${array[0]}'" <de
    # below only works on linux
#    x-terminal-emulator -e "ssh $user@${array[1]} 'cd CNT5106C_P2P;java com.company.impl.Main ${array[0]} > log${array[0]}.txt'"
    ssh -f $user@"${array[1]}" "cd CNT5106C_P2P;java com.company.impl.Main '${array[0]}' > log'${array[0]}'.log"
    echo "$line is on"
    sleep 1
done < PeerInfo.cfg
unset IFS

