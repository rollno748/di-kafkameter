package com.di.jmeter.kafka.sampler;

import com.google.common.base.Strings;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

public class KafkaConsumerSampler extends AbstractTestElement
        implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerSampler.class);

    private String kafkaConsumerClientVariableName;
    private String pollTimeout;
    private String commitType;
    private final long DEFAULT_TIMEOUT = 100;

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
            ConsumerRecord<String, Object> fetchRecord = getConsumerRecord();
            if(!(fetchRecord == null)){
                result.setResponseHeaders(String.format("Topic: %s\nPartition: %s\nOffset: %s\nKey: %s\nTimestamp: %s\nHeaders: %s\n", fetchRecord.topic(), fetchRecord.partition(), fetchRecord.offset(), fetchRecord.key(), fetchRecord.timestamp(), fetchRecord.headers()));
                result.setResponseData(fetchRecord.value().toString(), StandardCharsets.UTF_8.name());
                result.setResponseOK();
            }else{
                result.setResponseData("No records retrieved", StandardCharsets.UTF_8.name());
                result.setResponseCode("401");
            }

        }catch (KafkaException e){
            LOGGER.info("Kafka Consumer config not initialized properly.. Check the config element");
            result = handleException(result, e);
        }finally {
            result.sampleEnd();
        }
        return result;
    }

    private ConsumerRecord<String, Object> getConsumerRecord() {
        ConsumerRecord<String, Object> fetchedRecord = null;
        this.pollTimeout = (Strings.isNullOrEmpty(pollTimeout)) ?  String.valueOf(DEFAULT_TIMEOUT) : pollTimeout;

        // poll for new data
        ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofMillis(Long.parseLong(getPollTimeout()))); // This will poll multiple messages to records
        if(!records.isEmpty()){
            fetchedRecord = records.iterator().next();
            LOGGER.debug(String.format("offset = %d, key = %s, value = %s%n", fetchedRecord.offset(), fetchedRecord.key(), fetchedRecord.value()));
            // commit offset of the message
            Map<TopicPartition, OffsetAndMetadata> offset = Collections.singletonMap(
                    new TopicPartition(fetchedRecord.topic(), fetchedRecord.partition()),
                    new OffsetAndMetadata(fetchedRecord.offset() + 1)
            );

            if(getCommitType().equalsIgnoreCase("sync")){
                kafkaConsumer.commitSync(offset); //Commit the offset after reading single message
            }else{
                kafkaConsumer.commitAsync((OffsetCommitCallback) offset);//Commit the offset after reading single message
            }
        }
        return fetchedRecord;
    }

    private void validateClient() {
        if (this.kafkaConsumer == null && getKafkaConsumer() != null) {
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

    @Override
    public void testStarted() {
    }
    @Override
    public void testStarted(String s) {
    }
    @Override
    public void testEnded() {
    }
    @Override
    public void testEnded(String s) {
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
        return (KafkaConsumer<String, Object>) JMeterContextService.getContext().getVariables().getObject(getKafkaConsumerClientVariableName());
    }

}

//https://stackoverflow.com/questions/46546489/how-does-kafka-consumer-auto-commit-work