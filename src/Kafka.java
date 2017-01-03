package canal_qbus;

import java.util.ArrayList;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;

public class Kafka extends Qbus {

	public void conn() throws Exception
	{
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		this.qbus = new KafkaProducer<String, String>(props);
        alive = true;
        logger.info("connection established");
	}
}
