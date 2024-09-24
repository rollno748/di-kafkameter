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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class KafkaConsumerSampler<K, V> extends AbstractTestElement
        implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerSampler.class);

    private String pollTimeout;
    private String commitType;
    private final long DEFAULT_TIMEOUT = 100;
    private KafkaConsumer<K, V> kafkaConsumer;
    private String kafkaConsumerClientVariableName;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setDataType(SampleResult.TEXT);
        result.setContentType("text/plain");
        result.setDataEncoding(StandardCharsets.UTF_8.name());

        try{
            KafkaConsumer<K, V> kafkaConsumer = getKafkaConsumerClient();
            result.sampleStart();
            ConsumerRecords<K, V> consumerRecords = getConsumerRecords(kafkaConsumer);
            processRecordsToResults(consumerRecords, result);
        }catch (Exception ex){
            LOGGER.error("Exception occurred while reading message from Kafka", ex);
            result.setResponseMessage("Error reading message from kafka topic");
            result.setResponseCode("500");
            result.setResponseData(ex.toString(), StandardCharsets.UTF_8.name());
            result.setSuccessful(false);
        }finally {
            result.sampleEnd();
        }
        return result;
    }

    private ConsumerRecords<K, V> getConsumerRecords(KafkaConsumer<K, V> consumer) {
        long timeout = pollTimeout != null && !pollTimeout.isEmpty() ? Long.parseLong(pollTimeout) : DEFAULT_TIMEOUT;
        ConsumerRecords<K, V> records;
        do {
            records = consumer.poll(Duration.ofMillis(timeout)); // This will poll Single/multiple messages of records as per the config
        } while (records.isEmpty());

        for(ConsumerRecord<K, V> record : records){
            LOGGER.debug(String.format("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value()));

            // Commit offset of the message
            Map<TopicPartition, OffsetAndMetadata> offset = Collections.singletonMap(
                    new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1)
            );

            if(getCommitType().equalsIgnoreCase("sync")){
                consumer.commitSync(offset); //Commit the offset after reading single message
            }else{
                //Commit the offset after reading single message
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Async commit failed for offsets " + offsets, exception);
                    }
                });
            }
        }

        return records;
    }

    private void processRecordsToResults(ConsumerRecords<K, V> consumerRecords, SampleResult result) {
        if (!consumerRecords.isEmpty()) {
            StringBuilder headers = new StringBuilder();
            StringBuilder response = new StringBuilder();
            String keyDeserializerClassName = JMeterContextService.getContext().getVariables().get("consumerDeserializerKeyVariableName");
            String valueDeserializerClassName = JMeterContextService.getContext().getVariables().get("consumerDeserializerValueVariableName");

            for (ConsumerRecord<K, V> record : consumerRecords) {
                headers.append(String.format("Timestamp: %d\nTopic: %s\nPartition: %d\nOffset: %d\nHeaders: %s\n\n",
                        record.timestamp(), record.topic(), record.partition(), record.offset(), record.headers()));
                String key = getTypedValue(record.key(), keyDeserializerClassName);
                String value = getTypedValue( record.value(), valueDeserializerClassName);
                response.append(key).append(": ").append(value).append("\n\n");
            }
            result.setResponseHeaders(headers.toString());
            result.setResponseData(response.toString(), StandardCharsets.UTF_8.name());
            result.setResponseOK();
        } else {
            result.setResponseData("No records retrieved", StandardCharsets.UTF_8.name());
            result.setResponseCode("204");
        }
    }

    private static String getTypedValue(Object value, String deserializerClassName) {
        if (value == null) return null;
        try{
            Class<?> deserializerClass = Class.forName(deserializerClassName);
            if (deserializerClass.equals(StringDeserializer.class)) {
                return (String) value;
            } else if (deserializerClass.equals(ByteArrayDeserializer.class)) {
                return new String((byte[]) value, StandardCharsets.UTF_8);
            } else if (deserializerClass.equals(IntegerDeserializer.class) ||
                    deserializerClass.equals(LongDeserializer.class) ||
                    deserializerClass.equals(FloatDeserializer.class) ||
                    deserializerClass.equals(DoubleDeserializer.class)) {
                return value.toString();
            }
            throw new IllegalArgumentException("Unsupported deserializer type: " + deserializerClass.getName());
        } catch (ClassNotFoundException e){
            throw new IllegalStateException("Invalid deserializer class: " + deserializerClassName, e);
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

    public KafkaConsumer<K, V> getKafkaConsumerClient() {
        String variableName = getKafkaConsumerClientVariableName();
        Object consumerObject = JMeterContextService.getContext().getVariables().getObject(variableName);

        if (consumerObject == null) {
            throw new IllegalStateException("Kafka Consumer Client not found. Check Variable Name '" + variableName + "' in KafkaConsumerSampler.");
        }

        if (!(consumerObject instanceof KafkaConsumer)) {
            throw new IllegalStateException("Object stored in '" + variableName + "' is not a KafkaProducer. Found: " + consumerObject.getClass().getName());
        }

        try {
            @SuppressWarnings("unchecked")
            KafkaConsumer<K, V> consumer = (KafkaConsumer<K, V>) consumerObject;
            return consumer;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Failed to cast object to KafkaConsumer<K, V>. This might be due to a mismatch in generic types.", e);
        }
    }

}