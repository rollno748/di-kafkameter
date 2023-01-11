package com.di.jmeter.kafka.sampler;

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
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerSampler extends AbstractTestElement
        implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {

    @Override
    public boolean applies(ConfigTestElement configTestElement) {
        return false;
    }

    @Override
    public SampleResult sample(Entry entry) {
//        Properties properties = new Properties();
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-1");
//        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//
//        Consumer<String, String> consumer = new KafkaConsumer<>(properties);
//        consumer.subscribe(Collections.singletonList(TOPIC));
//
//        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//            for (ConsumerRecord<String, String> record : records) {
//                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
//            }
//        }

//        Properties properties = new Properties();
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-1");
//        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//
//        Consumer<String, String> consumer = new KafkaConsumer<>(properties);
//        consumer.subscribe(Collections.singletonList(TOPIC));
//
//        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//        for (ConsumerRecord<String, String> record : records) {
//            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
//        }

        return null;
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
}
