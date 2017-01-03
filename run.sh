#!/bin/bash
errlog="./error.log"
echo "start to run ..."
if [ -z $1 ]; then
    java -classpath libs/*:classes/: canal_qbus/Main 2>$errlog 1>null & 
else 
    java -classpath libs/*:classes/: canal_qbus/Main
fi
echo "start succefully!"
