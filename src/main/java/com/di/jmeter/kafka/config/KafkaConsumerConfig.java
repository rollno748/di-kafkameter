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
package com.di.jmeter.kafka.config;

import com.di.jmeter.kafka.utils.VariableSettings;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class KafkaConsumerConfig<K, V> extends ConfigTestElement
        implements ConfigElement, TestBean, TestStateListener, Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);
    private static final long serialVersionUID = 3328926106250797599L;

    private KafkaConsumer<K, V> kafkaConsumer;
    private List<VariableSettings> extraConfigs;
    private String kafkaBrokers;
    private String groupId;
    private String topic;
    private String numberOfMsgToPoll;
    private boolean autoCommit;
    private String deSerializerKey;
    private String deSerializerValue;
    private String securityType;
    private String kafkaSslKeystore; // Kafka ssl keystore (include path information); e.g; "server.keystore.jks"
    private String kafkaSslKeystorePassword; // Keystore Password
    private String kafkaSslTruststore;
    private String kafkaSslTruststorePassword;
    private String kafkaSslPrivateKeyPass;
    private String kafkaConsumerClientVariableName;


    @Override
    public void addConfigElement(ConfigElement config) {
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();

        if (variables.getObject(kafkaConsumerClientVariableName) != null) {
            LOGGER.error("Kafka consumer is already running.");
        } else {
            synchronized (this) {
                try {
                    String deSerializerKey = getDeSerializerKey();
                    String deSerializerValue = getDeSerializerValue();
                    Deserializer<K> consumerDeserializerKey = createDeserializer(deSerializerKey);
                    Deserializer<V> consumerDeserializerValue = createDeserializer(deSerializerValue);
                    kafkaConsumer = new KafkaConsumer<>(getProps(), consumerDeserializerKey, consumerDeserializerValue);
                    kafkaConsumer.subscribe(Collections.singletonList(getTopic()));
                    variables.putObject(kafkaConsumerClientVariableName, kafkaConsumer);
                    variables.putObject("consumerDeserializerKeyVariableName", deSerializerKey);
                    variables.putObject("consumerDeserializerValueVariableName", deSerializerValue);
                    LOGGER.info("Kafka consumer client successfully Initialized");
                } catch (Exception e) {
                    LOGGER.error("Error establishing kafka consumer client!", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Deserializer<T> createDeserializer(String deserializerClass) throws ReflectiveOperationException {
        return (Deserializer<T>) Class.forName(deserializerClass).getDeclaredConstructor().newInstance();
    }

    private Properties getProps() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());//groupId
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, isAutoCommit());
        props.put("security.protocol", getSecurityType().replaceAll("securityType.", "").toUpperCase());

        LOGGER.debug("Additional Config Size::: " + getExtraConfigs().size());
        if (!getExtraConfigs().isEmpty()) {
            LOGGER.info("Setting up Additional properties");
            for (VariableSettings entry : getExtraConfigs()){
                props.put(entry.getConfigKey(), entry.getConfigValue());
                LOGGER.debug(String.format("Adding property : %s", entry.getConfigKey()));
            }
        }

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Math.max(Integer.parseInt(getNumberOfMsgToPoll()), 1));
        if (getSecurityType().equalsIgnoreCase("securityType.ssl") || getSecurityType().equalsIgnoreCase("securityType.sasl_ssl")) {
            LOGGER.info("Kafka security type: " + getSecurityType().replaceAll("securityType.", "").toUpperCase());
            LOGGER.info("Setting up Kafka {} properties", getSecurityType());
            if(!getKafkaSslKeystore().isEmpty()) {
                props.put("ssl.truststore.location", getKafkaSslTruststore());
                props.put("ssl.truststore.password", getKafkaSslTruststorePassword());
            }
            if(!getKafkaSslKeystore().isEmpty()){
                props.put("ssl.keystore.location", getKafkaSslKeystore());
                props.put("ssl.keystore.password", getKafkaSslKeystorePassword());
                props.put("ssl.key.password", getKafkaSslPrivateKeyPass());
            }
        }
        return props;
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        if(kafkaConsumer != null){
            kafkaConsumer.unsubscribe();
            kafkaConsumer.close();
            LOGGER.info("Kafka consumer client connection terminated");
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    // Getters and setters
    public KafkaConsumer<K, V> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public String getKafkaConsumerClientVariableName() {
        return kafkaConsumerClientVariableName;
    }

    public void setKafkaConsumerClientVariableName(String kafkaConsumerClientVariableName) {
        this.kafkaConsumerClientVariableName = kafkaConsumerClientVariableName;
    }

    public String getKafkaBrokers() {
        return kafkaBrokers;
    }

    public void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getKafkaSslKeystore() {
        return kafkaSslKeystore;
    }

    public void setKafkaSslKeystore(String kafkaSslKeystore) {
        this.kafkaSslKeystore = kafkaSslKeystore;
    }

    public String getKafkaSslKeystorePassword() {
        return kafkaSslKeystorePassword;
    }

    public void setKafkaSslKeystorePassword(String kafkaSslKeystorePassword) {
        this.kafkaSslKeystorePassword = kafkaSslKeystorePassword;
    }

    public String getKafkaSslTruststore() {
        return kafkaSslTruststore;
    }

    public void setKafkaSslTruststore(String kafkaSslTruststore) {
        this.kafkaSslTruststore = kafkaSslTruststore;
    }

    public String getKafkaSslTruststorePassword() {
        return kafkaSslTruststorePassword;
    }

    public void setKafkaSslTruststorePassword(String kafkaSslTruststorePassword) {
        this.kafkaSslTruststorePassword = kafkaSslTruststorePassword;
    }

    public String getKafkaSslPrivateKeyPass() {
        return kafkaSslPrivateKeyPass;
    }

    public void setKafkaSslPrivateKeyPass(String kafkaSslPrivateKeyPass) {
        this.kafkaSslPrivateKeyPass = kafkaSslPrivateKeyPass;
    }

    public void setExtraConfigs(List<VariableSettings> extraConfigs) {
        this.extraConfigs = extraConfigs;
    }

    public List<VariableSettings> getExtraConfigs() {
        return this.extraConfigs;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNumberOfMsgToPoll() {
        return numberOfMsgToPoll;
    }

    public void setNumberOfMsgToPoll(String numberOfMsgToPoll) {
        this.numberOfMsgToPoll = numberOfMsgToPoll;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public String getDeSerializerKey() {
        return deSerializerKey;
    }

    public void setDeSerializerKey(String deSerializerKey) {
        this.deSerializerKey = deSerializerKey;
    }

    public String getDeSerializerValue() {
        return deSerializerValue;
    }

    public void setDeSerializerValue(String deSerializerValue) {
        this.deSerializerValue = deSerializerValue;
    }
}