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

import com.di.jmeter.kafka.config.KafkaProducerConfig;
import com.di.jmeter.kafka.utils.VariableSettings;
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

	private String kafkaProducerClientVariableName;
	private String kafkaTopic;
	private String partitionString;
	private String kafkaMessageKey;
	private Object kafkaMessage;
	private List<VariableSettings> messageHeaders;
	private KafkaProducer<K, V> kafkaProducer;

	@Override
	public SampleResult sample(Entry e) {

		SampleResult result = new SampleResult();

		try {
			if(this.kafkaProducer == null){
				this.validateClient();
			}
			ProducerRecord<K, V> producerRecord = getProducerRecord();
			result.setSampleLabel(getName());
			result.setDataType(SampleResult.TEXT);
			result.setContentType("text/plain");
			result.setDataEncoding(StandardCharsets.UTF_8.name());
			result.setSamplerData(getKafkaMessageKey()+": "+ getKafkaMessage());
			result.setRequestHeaders(producerRecord.headers().toString());
			result.sampleStart();
			Future<RecordMetadata> metaData = kafkaProducer.send(producerRecord);
			result.setResponseData("Success", StandardCharsets.UTF_8.name());
			result.setResponseHeaders(String.format("Topic: %s\nOffset: %s \nPartition: %s\nTimestamp: %s", metaData.get().topic(), metaData.get().offset(), metaData.get().partition(), metaData.get().timestamp()));
			result.setResponseOK();
		} catch (Exception ex) {
			LOGGER.info("Exception occurred while sending message to kafka");
			handleException(result, ex);
		} finally {
			result.sampleEnd();
		}
		return result;
	}

	private ProducerRecord<K, V> getProducerRecord() {
		String keySerializerClassName = getKeySerializer();
		String valueSerializerClassName = getValueSerializer();

		if (keySerializerClassName == null || valueSerializerClassName == null) {
			throw new IllegalStateException("Key or Value serializer is null");
		}

		try {
			Class<?> keySerializerClass = Class.forName(keySerializerClassName);
			Class<?> valueSerializerClass = Class.forName(valueSerializerClassName);

			K key = getKafkaMessageKey() != null ? getTypedValue(getKafkaMessageKey(), keySerializerClass) : null;
			V value = getKafkaMessage() != null ? getTypedValue(getKafkaMessage(), valueSerializerClass) : null;

			if (value == null) {
				throw new IllegalArgumentException("Kafka message (value) cannot be null");
			}

			ProducerRecord<K, V> producerRecord = createProducerRecord(key, value);
			addHeadersToRecord(producerRecord);
			return producerRecord;
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Invalid serializer class", e);
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

	private void validateClient() {
		if (this.kafkaProducer == null && getKafkaProducerClient() != null) {
			this.kafkaProducer = getKafkaProducerClient();
		}else{
			throw new RuntimeException("Kafka Producer Client not found. Check Variable Name in KafkaProducerSampler.");
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Serializer<T> createSerializer(String serializerClass) throws ReflectiveOperationException {
		return (Serializer<T>) Class.forName(serializerClass).getDeclaredConstructor().newInstance();
	}

	private <K, V> ProducerRecord<K, V> createProducerRecord(K key, V value) {
		if (Strings.isNullOrEmpty(getPartitionString())) {
			return new ProducerRecord<>(getKafkaTopic(), key, value);
		} else {
			int partitionNumber = Integer.parseInt(getPartitionString());
			return new ProducerRecord<>(getKafkaTopic(), partitionNumber, key, value);
		}
	}

	private <K, V> void addHeadersToRecord(ProducerRecord<K, V> producerRecord) {
		if (!getMessageHeaders().isEmpty()) {
			LOGGER.debug("Setting up additional header to message");
			for (VariableSettings entry : getMessageHeaders()) {
				producerRecord.headers().add(new RecordHeader(entry.getHeaderKey(), entry.getHeaderValue().getBytes()));
				LOGGER.debug("Adding Headers : {}", entry.getHeaderKey());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getTypedValue(String value, Class<?> serializerClass) {
		if (serializerClass.equals(StringSerializer.class)) {
			return (T) value;
		} else if (serializerClass.equals(ByteArraySerializer.class)) {
			return (T) value.getBytes();
		} else if (serializerClass.equals(BooleanSerializer.class)) {
			return (T) Boolean.valueOf(value);
		} else if (serializerClass.equals(IntegerSerializer.class)) {
			return (T) Integer.valueOf(value);
		} else if (serializerClass.equals(LongSerializer.class)) {
			return (T) Long.valueOf(value);
		} else if (serializerClass.equals(FloatSerializer.class)) {
			return (T) Float.valueOf(value);
		} else if (serializerClass.equals(DoubleSerializer.class)) {
			return (T) Double.valueOf(value);
		}
		// Add more type conversions as needed
		throw new IllegalArgumentException("Unsupported serializer type: " + serializerClass.getName());
	}

	private void handleException(SampleResult result, Exception ex) {
		result.setResponseMessage("Message: Error sending message to kafka topic");
		result.setResponseCode("500");
		result.setResponseData(String.format("Error sending message to kafka topic : %s", ex.toString()).getBytes());
		result.setSuccessful(false);
	}

	//Getters Setters
	public String getKafkaProducerClientVariableName() {
		return kafkaProducerClientVariableName;
	}

	public void setKafkaProducerClientVariableName(String kafkaProducerClientVariableName) { this.kafkaProducerClientVariableName = kafkaProducerClientVariableName; }

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
		return (String) kafkaMessage;
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
		return (KafkaProducer<K, V>) JMeterContextService.getContext().getVariables().getObject(getKafkaProducerClientVariableName());
	}
	private String getKeySerializer() {
		return (String) JMeterContextService.getContext().getVariables().getObject(KafkaProducerConfig.getKeySerializerVariableName());
	}
	private String getValueSerializer() {
		return (String) JMeterContextService.getContext().getVariables().getObject(KafkaProducerConfig.getValueSerializerVariableName());
	}

}
