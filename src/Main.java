package canal_qbus;

import java.lang.Thread;
import java.util.ArrayList;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  

public class Main {
    
    private static Logger logger;
    private static MysqlQueue q;
    
    public static void main(String args[]) {
        String cfgFile = "./conf/canal-qbus.json";
        try {
            Config.init(cfgFile);
        } catch (Exception e) {
            System.out.println("[" + cfgFile + "], failed to load config file, error:" + e.getMessage());
            return;
        }
        Router.init();
        Log.init();
        q = new MysqlQueue();
        logger = LoggerFactory.getLogger(Main.class);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("shut down by KILL ....");
                //q.Stop();
            }
        });
        q.Start();
        logger.info("here we go");
        /*
        try {
            Thread t = new Thread() {
                public void run() {
                    q.Start();
                }
            };
            t.start();
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
