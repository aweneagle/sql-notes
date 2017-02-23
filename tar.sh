#!/bin/bash
dir=$(dirname $(readlink -f "$0"))
rm -rf /tmp/canal-qbus
cp -r $dir /tmp/canal-qbus
rm -rf /tmp/canal-qbus/.git
rm -f /tmp/canal-qbus.tar.gz
tar -czf /tmp/canal-qbus.tar.gz -C /tmp/ canal-qbus
echo "archive file '/tmp/canal-qbus.tar.gz' is created succefully!"


