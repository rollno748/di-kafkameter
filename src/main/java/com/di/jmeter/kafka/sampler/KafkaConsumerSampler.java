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
import java.util.Collections;
import java.util.Map;

public class KafkaConsumerSampler extends AbstractTestElement
        implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerSampler.class);

    private String kafkaConsumerClientVariableName;
    private String pollTimeout;
    private String commitType;
    private boolean autoCommit;
    private final long DEFAULT_TIMEOUT = 100;

    private KafkaConsumer<String, Object> kafkaConsumer;

    @Override
    public SampleResult sample(Entry entry) {

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSamplerData("Sampler Data");
        result.setDataType(SampleResult.TEXT);
        result.setContentType("text/plain");
        result.setDataEncoding(StandardCharsets.UTF_8.name());

        try{
            result.sampleStart();
            this.readMessage(result);
        }catch (KafkaException e){
            LOGGER.info("Kafka Consumer config not initialized properly.. Check the config element");
            handleException(result, e);
        }finally {
            result.sampleEnd();
        }
        return result;
    }

    private void readMessage(SampleResult result) {
        this.pollTimeout = (Strings.isNullOrEmpty(pollTimeout)) ?  String.valueOf(DEFAULT_TIMEOUT) : pollTimeout;
        if (this.kafkaConsumer == null && getKafkaConsumer() != null) {
            this.kafkaConsumer = getKafkaConsumer();
        }else{
            throw new RuntimeException("Kafka Consumer Client not found. Check Variable Name in KafkaConsumerSampler.");
        }

        // poll for new data
        ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofMillis(Long.parseLong(getPollTimeout()))); // This will poll multiple messages to records

        for (ConsumerRecord<String, Object> record : records) {
            LOGGER.debug(String.format("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value()));
            // commit offset of the message
            Map<TopicPartition, OffsetAndMetadata> offset = Collections.singletonMap(
                    new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1)
            );
            if(getCommitType().equalsIgnoreCase("sync")){
                kafkaConsumer.commitSync(offset); //Commit the offset after reading single message
            }else{
                kafkaConsumer.commitAsync((OffsetCommitCallback) offset);//Commit the offset after reading single message
            }
            result.setResponseData(getKafkaMessage(), StandardCharsets.UTF_8.name());
            result.setResponseOK();
            break; // Exit loop after reading single message
        }
    }

    private String getKafkaMessage() {
        return null;
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

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public KafkaConsumer<String, Object> getKafkaConsumer() {
        return (KafkaConsumer<String, Object>) JMeterContextService.getContext().getVariables().getObject(getKafkaConsumerClientVariableName());
    }

}