#!/bin/bash  
#set -x 

download_console() {
curl -#LO https://github.com/FISCO-BCOS/console/releases/download/v2.7.1/download_console.sh
bash download_console.sh
rm -rf console/lib/fisco-bcos-java-sdk-2.7.1.jar
cp fisco-bcos-java-sdk-2.7.2-SNAPSHOT.jar console/lib/ 
}

download_console