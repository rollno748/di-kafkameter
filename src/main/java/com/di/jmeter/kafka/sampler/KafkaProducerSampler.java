package com.di.jmeter.kafka.sampler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.di.jmeter.kafka.config.KafkaProducerConfig;
import com.google.common.base.Strings;

public class KafkaProducerSampler extends KafkaProducerTestElement
		implements Sampler, TestBean, ConfigMergabilityIndicator {

	private static Logger LOGGER = LoggerFactory.getLogger(KafkaProducerSampler.class);
	private static final long serialVersionUID = -1299097780294947281L;

	private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
			Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

	private KafkaProducer<String, Object> kafkaProducer;
	private ProducerRecord<String, Object> producerRecord;
	private byte[] byteKafkaMessage;
	
	@Override
	public SampleResult sample(Entry e) {

		SampleResult result = new SampleResult();
		byteKafkaMessage = null;
		result.setSampleLabel(getName());
		result.setSamplerData(request());
		result.setDataType(SampleResult.TEXT);
		result.setContentType("text/plain");
		result.setDataEncoding(StandardCharsets.UTF_8.name());

		try {

			if (Strings.isNullOrEmpty(getPartitionString())) {
				producerRecord = new ProducerRecord<String, Object>(getKafkaTopic(), getKafkaMessageKey(), getKafkaMessage());
				
			} else {
				final int partitionNumber = Integer.parseInt(getPartitionString());
				producerRecord = new ProducerRecord<String, Object>(getKafkaTopic(), partitionNumber, getKafkaMessageKey(), getKafkaMessage());
			}
			
			LOGGER.debug("Additional Headers Size::: "+ getMessageHeaders().size());

			if (getMessageHeaders().size() >= 1) {
				StringBuilder headers = new StringBuilder(); 
				LOGGER.debug("Setting up additional header to message");
				for (int i = 0; i < getMessageHeaders().size(); i++) {
					producerRecord.headers().add(new RecordHeader(getMessageHeaders().get(i).getHeaderKey(),
							getMessageHeaders().get(i).getHeaderValue().getBytes()));
					headers.append(getMessageHeaders().get(i).getHeaderKey() + ": " +getMessageHeaders().get(i).getHeaderValue() + "\n");
					LOGGER.debug(String.format("Adding Headers : %s", getMessageHeaders().get(i).getConfigKey()));
				}
				result.setRequestHeaders(headers.toString());
			}
			
			byteKafkaMessage = getKafkaMessage().getBytes();
			result.sampleStart();

			produce(byteKafkaMessage, result);

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

	

	private String produce(byte[] byteKafkaMessage, SampleResult result) {
		String resp = null;

		if (this.kafkaProducer == null) {
			this.kafkaProducer = KafkaProducerConfig.getKafkaProducerClient();
		}

		try {

			kafkaProducer.send(producerRecord);
			result.setResponseData(getKafkaMessage(), StandardCharsets.UTF_8.name());
			result.setSuccessful(true);
			result.setResponseCode("200");
			result.setResponseMessageOK();

		} catch (KafkaException e) {
			LOGGER.info("Kafka producer config not initialized properly.. Check the config element");
			handleException(result, e);
		}
		return resp;
	}

	private SampleResult handleException(SampleResult result, Exception ex) {
		result.setResponseMessage("Error sending message to kafka topic");
		result.setResponseCode("500");
		result.setResponseData(String.format("Error sending message to kafka topic : %s", ex.toString()).getBytes());
		result.setSuccessful(false);
		return result;
	}

	private String request() {
		StringBuilder requestBody = new StringBuilder();
		requestBody.append("KakfaMessage: \n").append(getKafkaMessage()).append("\n");
		return requestBody.toString();
	}

}
