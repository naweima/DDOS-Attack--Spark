package kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

public class test {
    public static void main(String[] args) throws InterruptedException {

        Properties properties = new Properties();
        properties.put("zk.connect", "192.168.48.131:2181");
        properties.put("metadata.broker.list", "192.168.48.131:9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        ProducerConfig producerConfig = new ProducerConfig(properties);
        Producer<String, String> producer = new Producer<String, String>(producerConfig);

        KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>("wzq", "kafka.test-message");
        producer.send(keyedMessage);

        producer.close();
    }
}
