package com.di.jmeter.kafka.sampler;

import java.io.Serializable;
import java.util.List;

import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;

import com.di.jmeter.kafka.utils.VariableSettings;

public abstract class KafkaProducerTestElement extends AbstractTestElement
		implements TestStateListener, TestElement, Serializable, Searchable {

	private static final long serialVersionUID = 2414070493635061825L;

	private String kafkaTopic;
	private String partitionString;
	private String kafkaMessageKey;
	private String kafkaMessage;
	private List<VariableSettings> messageHeaders;
	

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
		return kafkaMessageKey;
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


}
