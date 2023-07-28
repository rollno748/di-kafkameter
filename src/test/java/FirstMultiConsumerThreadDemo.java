import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * @Author: wcy
 * @Date: 2020/5/31
 */
public class FirstMultiConsumerThreadDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirstMultiConsumerThreadDemo.class);

    public static final String brokerList = "192.168.41.186:9092";
    public static final String topic = "kafka_topic_test_base-test_table_09";
    public static final String groupId = "group.demo12";

    public static Properties initConfig(){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,true);
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer",StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        return properties;
    }

    public static void main(String[] args) {
        Properties properties = initConfig();
        int consumerThreadNum = 5;
        for (int i = 0; i < consumerThreadNum; i++) {
            new KafkaConsumerThread(properties,topic).start();
        }
        properties.clear();
    }

    public static class KafkaConsumerThread extends Thread{
        private KafkaConsumer<String,String> kafkaConsumer;


        public KafkaConsumerThread(Properties props, String topic) {
            this.kafkaConsumer = new KafkaConsumer<>(props);
            this.kafkaConsumer.subscribe(Arrays.asList(topic));
            this.kafkaConsumer.seekToBeginning(this.kafkaConsumer.assignment());
        }

        @Override
        public void run() {
            try {
                while (true){
                    ConsumerRecords<String,String> records = kafkaConsumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String,String> record : records){
                        //实现处理逻辑
//                        System.out.println(String.format("%s", record.key()));
                        System.out.println("ConsumerRecords:"+record.value());
                        LOGGER.info("{}",record.value());

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                kafkaConsumer.close();
            }
        }
    }
}