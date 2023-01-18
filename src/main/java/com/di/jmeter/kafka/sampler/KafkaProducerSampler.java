package com.di.jmeter.kafka.sampler;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class KafkaProducerSampler extends AbstractTestElement
		implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerSampler.class);
	private static final long serialVersionUID = -1299097780294947281L;

	private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
			Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

	private String kafkaProducerClientVariableName;
	private String kafkaTopic;
	private String partitionString;
	private String kafkaMessageKey;
	private String kafkaMessage;
	private List<VariableSettings> messageHeaders;

	private KafkaProducer<String, Object> kafkaProducer;
	private ProducerRecord<String, Object> producerRecord;

	@Override
	public SampleResult sample(Entry e) {

		SampleResult result = new SampleResult();
		try {
			if(this.kafkaProducer == null){
				this.validateClient();
			}
			ProducerRecord<String, Object> producerRecord = getProducerRecord();

			result.setSampleLabel(getName());
			result.setDataType(SampleResult.TEXT);
			result.setContentType("text/plain");
			result.setDataEncoding(StandardCharsets.UTF_8.name());
			result.setSamplerData(getKafkaMessage());
			result.setRequestHeaders(producerRecord.headers().toString());

			result.sampleStart();
			Future<RecordMetadata> metaData = kafkaProducer.send(producerRecord);
			result.setResponseData("Success", StandardCharsets.UTF_8.name());
			result.setResponseHeaders(String.format("Topic: %s\nOffset: %s \nPartition: %s\nTimestamp: %s", metaData.get().topic(), metaData.get().offset(), metaData.get().partition(), metaData.get().timestamp()));
			result.setResponseOK();
		} catch (Exception ex) {
			LOGGER.info("Exception occurred while sending message to kafka");
			result = handleException(result, ex);
		} finally {
			result.sampleEnd();
		}
		return result;
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

	private ProducerRecord<String, Object> getProducerRecord() {
		if (Strings.isNullOrEmpty(getPartitionString())) {
			producerRecord = new ProducerRecord<String, Object>(getKafkaTopic(), getKafkaMessageKey(), getKafkaMessage());
		} else {
			final int partitionNumber = Integer.parseInt(getPartitionString());
			producerRecord = new ProducerRecord<String, Object>(getKafkaTopic(), partitionNumber, getKafkaMessageKey(), getKafkaMessage());
		}

		LOGGER.debug("Additional Headers Size::: "+ getMessageHeaders().size());

		if (getMessageHeaders().size() >= 1) {
			LOGGER.debug("Setting up additional header to message");
			for (VariableSettings entry : getMessageHeaders()){
				producerRecord.headers().add(new RecordHeader(entry.getHeaderKey(), entry.getHeaderValue().getBytes()));
				LOGGER.debug(String.format("Adding Headers : %s", entry.getHeaderKey()));
			}
		}
		return producerRecord;
	}

	private SampleResult handleException(SampleResult result, Exception ex) {
		result.setResponseMessage("Message: Error sending message to kafka topic");
		result.setResponseCode("500");
		result.setResponseData(String.format("Error sending message to kafka topic : %s", ex.toString()).getBytes());
		result.setSuccessful(false);
		return result;
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
	private KafkaProducer<String, Object> getKafkaProducerClient() {
		return (KafkaProducer<String, Object>) JMeterContextService.getContext().getVariables().getObject(getKafkaProducerClientVariableName());
	}
}
