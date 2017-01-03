#!/bin/bash
echo "start to compile ..."
javac src/* -classpath :libs/* -encoding "utf8" -d classes
