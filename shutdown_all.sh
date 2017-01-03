#!/bin/bash
pid=$(ps aux | grep "java -classpath" |  grep "canal_qbus/Main" | grep -v "grep" | awk '{print $2;}');
if [ -n "$pid" ]; then
    for p in `echo $pid`; do 
        echo "stopping old process $p ...";
        kill $p;
    done
fi
