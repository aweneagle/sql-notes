package canal_qbus;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  

public class MysqlQueue {
	
	private boolean stop;

	private Logger logger;

    private SafeService qbus;
    private SafeService canal;
	
	public void Start()
	{
		logger = LoggerFactory.getLogger(MysqlQueue.class);
        stop = false;
        init();
        this.process();
	}

    private void init()
    {
		/*
		 * 准备好 qbus 配置， 连接上 qbus
		 */
        if (qbus == null) {
            if (Config.get("global").getBoolean("local", true)) {
                qbus = new SafeService(
                        new Kafka(),
                        "qbus");
            } else {
                qbus = new SafeService(
                        new Qbus(),
                        "qbus");
            }
        }
		/*
		 * 准备好canal 配置，连接上canal
		 */
        if (canal == null) {
            canal = new SafeService(
                    new Canal(),
                    "canal");
        }
    }
	
	/*
	 * Start() 启动服务
	 */
	private void process()
	{
		stop = !canal.conn() || !qbus.conn();
		/*
		 * 开始处理消息
		 */
		Msg msg;
		while (!stop) {
			try {
				msg = canal.recv();
			} catch (Exception e) {
                e.printStackTrace();
				canal.reconn();
				continue;
			}
            if (msg == null) {
                continue;
            }
			
			if (!qbus.send(msg)) {
				logger.error("failed to send message:" + msg.toString());
			} else {
				canal.ack();
			}
		}
        close_conn();
	}
	
	public void Stop()
	{
		logger.info("shut down");
		//this.stop = true;
        //close_conn();
	}

    private void close_conn()
    {
        canal.close();
        qbus.close();
    }
	
}
