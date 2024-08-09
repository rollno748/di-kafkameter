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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

public class KafkaProducerConfig<K, V> extends ConfigTestElement
		implements ConfigElement, TestBean, TestStateListener, Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfig.class);
	private static final long serialVersionUID = 3328926106250797599L;
	private static String producerSerializerKeyVariableName;
	private static String producerSerializerValueVariableName;
	private KafkaProducer<K, V> kafkaProducer;
	private List<VariableSettings> extraConfigs;
	private String producerClientVariableName;
	private String kafkaBrokers;
	private String batchSize; // default: 16384
	private String clientId;
	private String serializerKey;
	private String serializerValue;
	private String securityType;
	private String kafkaSslKeystore; // Kafka ssl keystore (include path information); e.g; "server.keystore.jks"
	private String kafkaSslKeystorePassword; // Keystore Password
	private String kafkaSslTruststore;
	private String kafkaSslTruststorePassword;
	private String kafkaSslPrivateKeyPass;

	@Override
	public void addConfigElement(ConfigElement config) {
	}

    @Override
	public void testStarted() {
		this.setRunningVersion(true);
		TestBeanHelper.prepare(this);
		JMeterVariables variables = getThreadContext().getVariables();

		if (variables.getObject(producerClientVariableName) != null) {
			LOGGER.error("Kafka Client is already running.");
		} else {
			synchronized (this) {
				try {
					Serializer<K> producerSerializerKey = createSerializer(getSerializerKey());
					Serializer<V> producerSerializerValue = createSerializer(getSerializerValue());
					kafkaProducer = new KafkaProducer<>(getProps(), producerSerializerKey, producerSerializerValue);
					variables.putObject(producerClientVariableName, kafkaProducer);
					variables.putObject(producerSerializerKeyVariableName, getSerializerKey());
					variables.putObject(producerSerializerValueVariableName, getSerializerValue());
					LOGGER.info("Kafka Producer client successfully Initialized");
				} catch (Exception e) {
					LOGGER.error("Error establishing Kafka producer client!", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Serializer<T> createSerializer(String serializerClass) throws ReflectiveOperationException {
		return (Serializer<T>) Class.forName(serializerClass).getDeclaredConstructor().newInstance();
	}

	private Properties getProps() {
		Properties props = new Properties();

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokers());
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, getBatchSize());
		props.put(ProducerConfig.CLIENT_ID_CONFIG, getClientId());
		props.put("security.protocol", getSecurityType().replaceAll("securityType.", "").toUpperCase());

		LOGGER.debug("Additional Config Size::: " + getExtraConfigs().size());
		if (!getExtraConfigs().isEmpty()) {
			LOGGER.info("Setting up Additional properties");
			for (VariableSettings entry : getExtraConfigs()){
				props.put(entry.getConfigKey(), entry.getConfigValue());
				LOGGER.debug(String.format("Adding property : %s", entry.getConfigKey()));
			}
		}

		if (getSecurityType().equalsIgnoreCase("securityType.ssl") || getSecurityType().equalsIgnoreCase("securityType.sasl_ssl")) {
			LOGGER.info("Kafka security type: " + getSecurityType().replaceAll("securityType.", "").toUpperCase());
			LOGGER.info("Setting up Kafka {} properties", getSecurityType());
			props.put("ssl.truststore.location", getKafkaSslTruststore());
			props.put("ssl.truststore.password", getKafkaSslTruststorePassword());
			props.put("ssl.keystore.location", getKafkaSslKeystore());
			props.put("ssl.keystore.password", getKafkaSslKeystorePassword());
			props.put("ssl.key.password", getKafkaSslPrivateKeyPass());
		}

		return props;
	}

	@Override
	public void testStarted(String host) {
		testStarted();
	}

	@Override
	public void testEnded() {
		if(kafkaProducer != null){
			kafkaProducer.flush();
			kafkaProducer.close();
			LOGGER.info("Kafka Producer client connection terminated");
		}
	}

	@Override
	public void testEnded(String host) {
		testEnded();
	}

	// Getters and setters
	public KafkaProducer<K, V> getKafkaProducer() {
		return kafkaProducer;
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

	public String getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSerializerKey() {
		return serializerKey;
	}

	public void setSerializerKey(String serializerKey) {
		this.serializerKey = serializerKey;
	}

	public String getSerializerValue() {
		return serializerValue;
	}

	public void setSerializerValue(String serializerValue) {
		this.serializerValue = serializerValue;
	}

	public static String getProducerSerializerKeyVariableName() {
		return producerSerializerKeyVariableName;
	}

	public static void setProducerSerializerKeyVariableName(String producerSerializerKeyVariableName) {
		KafkaProducerConfig.producerSerializerKeyVariableName = producerSerializerKeyVariableName;
	}

	public static String getProducerSerializerValueVariableName() {
		return producerSerializerValueVariableName;
	}

	public static void setProducerSerializerValueVariableName(String producerSerializerValueVariableName) {
		KafkaProducerConfig.producerSerializerValueVariableName = producerSerializerValueVariableName;
	}

	public String getProducerClientVariableName() { return producerClientVariableName; }

	public void setProducerClientVariableName(String producerClientVariableName) { this.producerClientVariableName = producerClientVariableName; }
}
