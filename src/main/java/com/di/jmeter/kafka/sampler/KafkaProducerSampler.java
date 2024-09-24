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

import com.di.jmeter.kafka.utils.VariableSettings;
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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class KafkaProducerSampler<K, V> extends AbstractTestElement
		implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerSampler.class);
	private static final long serialVersionUID = -1299097780294947281L;
	private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Collections.singletonList("org.apache.jmeter.config.gui.SimpleConfigGui"));

	private String kafkaTopic;
	private String partitionString;
	private String kafkaMessageKey;
	private String kafkaMessage;
	private List<VariableSettings> messageHeaders;
	private String kafkaProducerClientVariableName;
	private String kafkaProducerSerializerKeyVariableName;
	private String kafkaProducerSerializerValueVariableName;

	@Override
	public SampleResult sample(Entry e) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		result.setDataType(SampleResult.TEXT);
		result.setContentType("text/plain");
		result.setDataEncoding(StandardCharsets.UTF_8.name());

		try {
			KafkaProducer<K, V> kafkaProducer = getKafkaProducerClient();
			ProducerRecord<K, V> producerRecord = getProducerRecord(kafkaProducer);
			result.setSamplerData(kafkaMessageKey + ": " + kafkaMessage);
			result.setRequestHeaders(producerRecord.headers().toString());
			result.sampleStart();
			Future<RecordMetadata> metaData = kafkaProducer.send(producerRecord);
			RecordMetadata recordMetadata = metaData.get();
			result.setResponseData("Success", StandardCharsets.UTF_8.name());
			result.setResponseHeaders(String.format("Topic: %s\nOffset: %s\nPartition: %s\nTimestamp: %s",
					recordMetadata.topic(), recordMetadata.offset(), recordMetadata.partition(), recordMetadata.timestamp()));
			result.setResponseOK();
		} catch (Exception ex) {
			LOGGER.error("Exception occurred while sending message to Kafka", ex);
			result.setResponseMessage("Error sending message to Kafka topic");
			result.setResponseCode("500");
			result.setResponseData(ex.toString(), StandardCharsets.UTF_8.name());
			result.setSuccessful(false);
		} finally {
			result.sampleEnd();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private ProducerRecord<K, V> getProducerRecord(KafkaProducer<K, V> producer) {
		K key = (K) getTypedValue(getKafkaMessageKey(), getProducerSerializerKey());
		V value = (V) getTypedValue(getKafkaMessage(), getProducerSerializerValue());

		// Create the producer record with or without a specific partition
		ProducerRecord<K, V> producerRecord;
		if (partitionString != null && !partitionString.isEmpty()) {
			int partitionNumber = Integer.parseInt(partitionString);
			producerRecord = new ProducerRecord<>(kafkaTopic, partitionNumber, key, value);
		} else {
			producerRecord = new ProducerRecord<>(kafkaTopic, key, value);
		}

		// Add headers to the producer record, if any
		addHeadersToRecord(producerRecord);
		return producerRecord;
	}

	private static Object getTypedValue(String value, String serializerClassName) {
		if (value == null) return null;
		try {
			Class<?> serializerClass = Class.forName(serializerClassName);
			if (serializerClass.equals(StringSerializer.class)) {
				return value;
			} else if (serializerClass.equals(ByteArraySerializer.class)) {
				return value.getBytes();
			} else if (serializerClass.equals(IntegerSerializer.class)) {
				return Integer.valueOf(value);
			} else if (serializerClass.equals(LongSerializer.class)) {
				return Long.valueOf(value);
			} else if (serializerClass.equals(FloatSerializer.class)) {
				return Float.valueOf(value);
			} else if (serializerClass.equals(DoubleSerializer.class)) {
				return Double.valueOf(value);
			}
			throw new IllegalArgumentException("Unsupported serializer type: " + serializerClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Invalid serializer class: " + serializerClassName, e);
		}
	}

	private void addHeadersToRecord(ProducerRecord<K, V> producerRecord) {
		if (messageHeaders != null && !messageHeaders.isEmpty()) {
			LOGGER.debug("Setting up additional header to message");
			for (VariableSettings entry : getMessageHeaders()) {
				producerRecord.headers().add(new RecordHeader(entry.getHeaderKey(), entry.getHeaderValue().getBytes()));
				LOGGER.debug("Adding Headers : {}", entry.getHeaderKey());
			}
		}
	}

	@Override
	public void testStarted() {
	}

	@Override
	public void testStarted(String host) {
	}

	@Override
	public void testEnded() {

	}

	@Override
	public void testEnded(String host) {
	}

	@Override
	public boolean applies(ConfigTestElement configElement) {
		String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
		return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
	}

	//Getters Setters
	public String getKafkaProducerClientVariableName() {
		return kafkaProducerClientVariableName;
	}

	public void setKafkaProducerClientVariableName(String kafkaProducerClientVariableName) { this.kafkaProducerClientVariableName = kafkaProducerClientVariableName; }

	public String getKafkaProducerSerializerKeyVariableName() {
		return kafkaProducerSerializerKeyVariableName;
	}

	public String getKafkaProducerSerializerValueVariableName() {
		return kafkaProducerSerializerValueVariableName;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	public String getPartitionString() {
		return partitionString;
	}

	public void setPartitionString(String partitionString) {
		this.partitionString = partitionString;
	}

	public String getKafkaMessageKey() {
		return kafkaMessageKey.isEmpty() ? null : kafkaMessageKey;
	}

	public void setKafkaMessageKey(String kafkaMessageKey) {
		this.kafkaMessageKey = kafkaMessageKey;
	}

	public String getKafkaMessage() {
		return kafkaMessage;
	}

	public void setKafkaMessage(String kafkaMessage) {
		this.kafkaMessage = kafkaMessage;
	}

	public List<VariableSettings> getMessageHeaders() {
		return messageHeaders;
	}

	public void setMessageHeaders(List<VariableSettings> messageHeaders) {
		this.messageHeaders = messageHeaders;
	}

	@SuppressWarnings("unchecked")
	private KafkaProducer<K, V> getKafkaProducerClient() {
		String variableName = getKafkaProducerClientVariableName();
		Object producerObject = JMeterContextService.getContext().getVariables().getObject(variableName);

		if (producerObject == null) {
			throw new IllegalStateException("Kafka Producer Client not found. Check Variable Name '" + variableName + "' in KafkaProducerSampler.");
		}

		if (!(producerObject instanceof KafkaProducer)) {
			throw new IllegalStateException("Object stored in '" + variableName + "' is not a KafkaProducer. Found: " + producerObject.getClass().getName());
		}

		try {
			@SuppressWarnings("unchecked")
			KafkaProducer<K, V> producer = (KafkaProducer<K, V>) producerObject;
			return producer;
		} catch (ClassCastException e) {
			throw new IllegalStateException("Failed to cast object to KafkaProducer<K, V>. This might be due to a mismatch in generic types.", e);
		}
	}

	private String getProducerSerializerKey() {
		return JMeterContextService.getContext().getVariables().get(kafkaProducerSerializerKeyVariableName);
	}

	private String getProducerSerializerValue() {
		return JMeterContextService.getContext().getVariables().get(kafkaProducerSerializerValueVariableName);
	}
}
