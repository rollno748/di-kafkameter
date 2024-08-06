/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.di.jmeter.kafka.sampler;

import com.di.jmeter.kafka.config.KafkaConsumerConfig;
import com.google.common.base.Strings;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

public class KafkaConsumerSampler<K, V> extends AbstractTestElement
        implements Sampler, TestBean, TestStateListener, Serializable, Searchable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerSampler.class);
    private static final long DEFAULT_TIMEOUT = 100L;
    private String kafkaConsumerClientVariableName;
    private String pollTimeout;
    private String commitType;


    private KafkaConsumer<K, V> kafkaConsumer;

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

    private ConsumerRecords<K, V> getConsumerRecords() {
        String keyDeserializerClassName = getKeyDeserializer();
        String valueDeserializerClassName = getValueDeserializer();

        if (keyDeserializerClassName == null || valueDeserializerClassName == null) {
            throw new IllegalStateException("Key or Value deserializer is null");
        }

        try{
            Class<?> keyDeserializerClass = Class.forName(keyDeserializerClassName);
            Class<?> valueDeserializerClass = Class.forName(valueDeserializerClassName);

            this.pollTimeout = (Strings.isNullOrEmpty(pollTimeout)) ?  String.valueOf(DEFAULT_TIMEOUT) : pollTimeout;
            ConsumerRecords<K, V> records;

            do {
                // This will poll Single/multiple messages of records as per the config
                records = kafkaConsumer.poll(Duration.ofMillis(Long.parseLong(getPollTimeout())));
            } while (records.isEmpty());

            for(ConsumerRecord<K, V> record : records){
                K key = getTypedValue(record.key(), keyDeserializerClass);
                V value = getTypedValue(record.value(), valueDeserializerClass);

                LOGGER.debug(String.format("offset = %d, key = %s, value = %s%n", record.offset(), key, value));

                // Commit offset of the message
                Map<TopicPartition, OffsetAndMetadata> offset = Collections.singletonMap(
                        new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset() + 1)
                );

                if(getCommitType().equalsIgnoreCase("sync")){
                    kafkaConsumer.commitSync(offset); //Commit the offset after reading single message
                }else{
                    //Commit the offset after reading single message
                    kafkaConsumer.commitAsync((offsets, exception) -> {
                        if (exception != null) {
                            LOGGER.error("Async commit failed for offsets " + offsets, exception);
                        }
                    });
                }
            }
            return records;
        }catch (ClassNotFoundException cnfe){
            throw new IllegalStateException("Invalid deserializer class", cnfe);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getTypedValue(Object value, Class<?> deserializerClass) {
        if (value == null) {
            return null;
        }

        if (deserializerClass.equals(StringDeserializer.class)) {
            return (T) value.toString();
        } else if (deserializerClass.equals(IntegerDeserializer.class)) {
            return (T) Integer.valueOf(value.toString());
        } else if (deserializerClass.equals(LongDeserializer.class)) {
            return (T) Long.valueOf(value.toString());
        } else if (deserializerClass.equals(DoubleDeserializer.class)) {
            return (T) Double.valueOf(value.toString());
        } else if (deserializerClass.equals(ByteArrayDeserializer.class)) {
            return (T) value;
        } else {
            throw new IllegalArgumentException("Unsupported deserializer type: " + deserializerClass.getName());
        }
    }

    private void processRecordsToResults(ConsumerRecords<K, V> consumerRecords, SampleResult result) {
        if(!consumerRecords.isEmpty()){
            StringBuilder headers = new StringBuilder();
            StringBuilder response = new StringBuilder();
            for(ConsumerRecord<K, V> record : consumerRecords){
                headers.append(String.format("Timestamp: %s\nTopic: %s\nPartition: %s\nOffset: %s\nHeaders: %s\n\n", record.timestamp(), record.topic(), record.partition(), record.offset(), record.headers().toString()));
                response.append(record.key()).append(": ").append(record.value().toString()).append("\n\n");
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
        if (this.kafkaConsumer == null && getKafkaConsumer() != null) {
            this.kafkaConsumer = (KafkaConsumer<K, V>) getKafkaConsumer();
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

    @SuppressWarnings("unchecked")
    public KafkaConsumer<String, Object> getKafkaConsumer() {
        return (KafkaConsumer<String, Object>) JMeterContextService.getContext().getVariables().getObject(getKafkaConsumerClientVariableName());
    }
    private String getKeyDeserializer() {
        return (String) JMeterContextService.getContext().getVariables().getObject(KafkaConsumerConfig.getKeyDeserializerVariableName());
    }
    private String getValueDeserializer() {
        return (String) JMeterContextService.getContext().getVariables().getObject(KafkaConsumerConfig.getValueDeserializerVariableName());
    }

}
