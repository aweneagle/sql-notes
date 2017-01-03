<?php

require("/home/q/php/qbus/qbus.php");

#ini_set('memory_limit', '-1');

ini_set('error_reporting', E_ALL);

if (count($argv) > 3)
{
	$cluster = $argv[1];
	$topic = $argv[2];
	$group = $argv[3];
}
else
{
	print "Invaild parameter!"."\n";
	print "usage: php consumer.php <cluster> <topic> <group>"."\n";
	exit(1);
}

$consumer = new QbusConsumer;
$ret = $consumer->init($cluster, "./consumer.log", "./conf/consumer.config");
if ($ret == false)
{
	print "Failed init";
	exit(1);
}

$ret = $consumer->subscribeOne($group, $topic);
if ($ret == false)
{
	print "Failed subscribe";
	exit(1);
}

$run = true;
declare(ticks = 1);
function sig_handler($signo)
{
    print "stop...\n";
	global $run;
	$run = false;
}

pcntl_signal(SIGINT, "sig_handler");
$consumer->start();

$msg_info = new QbusMsgContentInfo;
while ($run)
{
    if ($consumer->consume($msg_info))
    {
	echo $msg_info->msg . "\n";
    }
}

$consumer->stop();

?>
