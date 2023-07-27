package com.di.jmeter.kafka.sampler;

import com.google.common.base.Strings;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class KafkaConsumerSampler extends AbstractTestElement
        implements Sampler, TestBean, ThreadListener, ConfigMergabilityIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerSampler.class);

    private String kafkaConsumerClientVariableName;
    private String pollTimeout;
    private String commitType;
    private final long DEFAULT_TIMEOUT = 100;
    private Properties properties;

    private KafkaConsumer<String, Object> kafkaConsumer;

    @Override
    public SampleResult sample(Entry entry) {

        SampleResult result = new SampleResult();
        try{
            if(this.kafkaConsumer == null){
                this.validateClient();
            }
            result.setSampleLabel(getName());
            result.setDataType(SampleResult.TEXT);
            result.setContentType("text/plain");
            result.setDataEncoding(StandardCharsets.UTF_8.name());
            result.setRequestHeaders(String.format("TimeStamp: %s\n", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

            result.sampleStart();
            this.processRecordsToResults(getConsumerRecords(), result);

        }catch (KafkaException e){
            LOGGER.info("Kafka Consumer config not initialized properly.. Check the config element");
            result = handleException(result, e);
        }finally {
            result.sampleEnd();
        }
        return result;
    }

    private ConsumerRecords<String, Object> getConsumerRecords() {
        ConsumerRecords<String, Object> records;
        this.pollTimeout = (Strings.isNullOrEmpty(pollTimeout)) ?  String.valueOf(DEFAULT_TIMEOUT) : pollTimeout;

        // This will poll Single/multiple messages of records as per the config
        do{
            records = kafkaConsumer.poll(Duration.ofMillis(Long.parseLong(getPollTimeout())));
        }while(records.isEmpty());

        for(ConsumerRecord<String, Object> record : records){
            record = records.iterator().next();
            LOGGER.debug(String.format("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value()));
            // commit offset of the message
            Map<TopicPartition, OffsetAndMetadata> offset = Collections.singletonMap(
                    new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1)
            );

            if(getCommitType().equalsIgnoreCase("sync")){
                kafkaConsumer.commitSync(offset); //Commit the offset after reading single message
            }else{
                kafkaConsumer.commitAsync();//Commit the offset after reading single message
            }
        }
        return records;
    }

    private void processRecordsToResults(ConsumerRecords<String, Object> consumerRecords, SampleResult result) {
        if(!consumerRecords.isEmpty()){
            StringBuilder headers = new StringBuilder();
            StringBuilder response = new StringBuilder();
            for(ConsumerRecord<String, Object> record : consumerRecords){
                headers.append(String.format("Timestamp: %s\nTopic: %s\nPartition: %s\nOffset: %s\nHeaders: %s\n\n", record.timestamp(), record.topic(), record.partition(), record.offset(), record.headers().toString()));
                response.append(record.key() + ": " + record.value().toString()+"\n\n");
            }
            result.setResponseHeaders(String.valueOf(headers));
            result.setResponseData(String.valueOf(response), StandardCharsets.UTF_8.name());
            result.setResponseOK();
        }else{
            result.setResponseData("No records retrieved", StandardCharsets.UTF_8.name());
            result.setResponseCode("401");
        }
    }

    private void validateClient() {
        if (this.kafkaConsumer == null) {
            this.kafkaConsumer = getKafkaConsumer();
        }else{
            throw new RuntimeException("Kafka Consumer Client not found. Check Variable Name in KafkaConsumerSampler.");
        }
    }

    private SampleResult handleException(SampleResult result, Exception ex) {
        result.setResponseMessage("Error sending message to kafka topic");
        result.setResponseCode("500");
        result.setResponseData(String.format("Error sending message to kafka topic : %s", ex.toString()).getBytes());
        result.setSuccessful(false);
        return result;
    }

    @Override
    public boolean applies(ConfigTestElement configTestElement) {
        return false;
    }

    //Getters and setters
    public String getKafkaConsumerClientVariableName() {
        return kafkaConsumerClientVariableName;
    }

    public void setKafkaConsumerClientVariableName(String kafkaConsumerClientVariableName) {
        this.kafkaConsumerClientVariableName = kafkaConsumerClientVariableName;
    }

    public String getPollTimeout() {
        return (Strings.isNullOrEmpty(pollTimeout)) ? pollTimeout : String.valueOf(DEFAULT_TIMEOUT);
    }

    public void setPollTimeout(String pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public String getCommitType() {
        return commitType;
    }

    public void setCommitType(String commitType) {
        this.commitType = commitType;
    }

    public KafkaConsumer<String, Object> getKafkaConsumer() {
        LOGGER.info("KafkaConsumerSampler:{}",this.kafkaConsumerClientVariableName);

        properties = (Properties)JMeterContextService.getContext().getVariables().getObject(getKafkaConsumerClientVariableName()+"props");
        String topic = (String)JMeterContextService.getContext().getVariables().getObject(getKafkaConsumerClientVariableName()+"topic");
        kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        return kafkaConsumer;
    }

    @Override
    public void threadStarted() {
            LOGGER.info("threadStarted");
    }

    @Override
    public void threadFinished() {
        properties.clear();
        kafkaConsumer.close();
    }

}
