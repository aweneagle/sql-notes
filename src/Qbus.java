package canal_qbus;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import qbus2.QbusProducer;
import javax.json.JsonObject;
import javax.json.JsonArray;
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  

public class Qbus implements Session{

    /* 
     * qbus
     */
    protected KafkaProducer<String, String> qbus;
    
    private String conf_file;
    

    private ArrayList<String> topicNames;
    
    private String cluster;

    protected boolean alive = false;

    protected Logger logger;
    
    
    public Qbus()
    {       
        alive = false;
        logger = LoggerFactory.getLogger(Qbus.class);
        this.cluster = Config.get("qbus").getString("cluster");
        this.conf_file = Config.get("qbus").getString("conf_file");
        this.topicNames = new ArrayList<String>();
        List<JsonObject> topicObjs = Config.getTopics();
        for (JsonObject tobj : topicObjs) {
            topicNames.add(tobj.getString("topic"));
        }
    }
    
    public void conn() throws Exception 
    {
        qbus = new QbusProducer<String, String>(cluster, conf_file, topicNames).getProducer();
        logger.info("connection established");
        alive = true;
    }
    
    public void close() throws Exception 
    {
        if (alive) {
            qbus.close();
            logger.info("connetion close");
        }
    }
    
    public void send(Msg msg) throws Exception 
    {
        for (Msg.Record record : msg.records) {
            if (record.data != null) {
                record.receiver = Router.getTopics(record.data);
                for (String topic : record.receiver) {
                    qbus.send(new ProducerRecord<String, String>(topic, null, record.data.toString()));
                }
                if (record.receiver.size() > 0) {
                    logger.info("send succefully:" + record.debugStr());
                } else {
                    logger.info("ignored:" + record.debugStr());
                }
            } else {
                logger.info("ignored:" + record.debugStr());
            }
        }
    }


    public Msg recv() throws Exception
    {
        // nothing to do
        throw new Exception("Qbus::recv() should not be called");
    }

    public void ack() throws Exception {
        // nothing to do
        throw new Exception("Qbus::ack() should not be called");
    }
    
}
