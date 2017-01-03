#!/bin/bash
ps aux | grep "java -classpath" | grep 'canal_qbus/Main' | grep -v "grep";
