#!/bin/bash
pid=$(ps aux | grep "java -classpath" |  grep "canal_qbus/Main" | grep -v "grep" | awk '{print $2;}');
if [ -n "$pid" ]; then 
    echo "canal-qbus already running ...";
    exit;
fi
echo "start to run ..."
not_find_errlog=`grep -e '"error_log"\s*:\s*"\s*\"' conf/canal-qbus.json`
not_find_infolog=`grep -e '"info_log"\s*:\s*"\s*\"' conf/canal-qbus.json`
if [ -n "$not_find_infolog" ]; then
    if [ -n "$not_find_errlog" ]; then
        java -classpath libs/*:classes/: canal_qbus/Main
    else
        java -classpath libs/*:classes/: canal_qbus/Main 1>/dev/null &
    fi
else
    java -classpath libs/*:classes/: canal_qbus/Main 1>/dev/null 2>&1 &
fi

if [ "$?" = "0" ]; then 
    echo "start succefully!"
else
    echo "start failed!";
fi
