package com.di.jmeter.kafka.config;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.di.jmeter.kafka.utils.VariableSettings;

public class KafkaProducerConfigBeanInfo extends BeanInfoSupport{
	
	private static Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfigBeanInfo.class);

	public KafkaProducerConfigBeanInfo() {
		super(KafkaProducerConfig.class);
		
		createPropertyGroup("KafkaConnectionConfigs", new String[] {"kafkaBrokers", "batchSize", "clientId", "serializerKey", "serializerValue"});
		//Additional Configs
		createPropertyGroup("AdditionalConfigs", new String[] {"extraConfigs"});
		//SSL
		createPropertyGroup("SSLConfigs", new String[] {"isSsl", 
				"kafkaSslKeystore", "kafkaSslKeystorePassword", "kafkaSslTruststore", "kafkaSslTruststorePassword"});


		PropertyDescriptor propDesc =  property("kafkaBrokers");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "localhost:9091");
		propDesc.setDisplayName("Kafka Brokers");
		propDesc.setShortDescription("List of Kafka Brokers - comma separated");
		
		propDesc =  property("batchSize");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "16384");
		propDesc.setDisplayName("Batch Size");
		propDesc.setShortDescription("Batch Size");
		
		propDesc =  property("clientId");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "Jmeter-Producer-1");
		propDesc.setDisplayName("Client ID");
		propDesc.setShortDescription("Client ID - Unique Id to connect to Broker");

		propDesc =  property("serializerKey");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringSerializer");
		propDesc.setDisplayName("Serializer Key");
		propDesc.setShortDescription("Serializer Key");
		
		propDesc =  property("serializerValue");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringSerializer");
		propDesc.setDisplayName("Serializer Value");
		propDesc.setShortDescription("Serializer Value");
		
		PropertyDescriptor configProps = property("extraConfigs", TypeEditor.TableEditor);
		configProps.setValue(TableEditor.CLASSNAME, VariableSettings.class.getName());
		configProps.setValue(TableEditor.HEADERS, new String[]{ "KafkaConfigKey", "KafkaConfigValue" } );
		configProps.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ VariableSettings.CONFIG_KEY, VariableSettings.CONFIG_VALUE } );
		configProps.setValue(DEFAULT, new ArrayList<>());
		configProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
		configProps.setDisplayName("Kafka Additional Configs (Optional)");
		
		propDesc =  property("isSsl");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		propDesc.setValue(DEFAULT, Boolean.FALSE);
		propDesc.setDisplayName("Enable SSL");
		propDesc.setShortDescription("Boolean to enable/disbale SSL");

		propDesc =  property("kafkaSslKeystore");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "");
		propDesc.setDisplayName("KafkaSSLKeystore");
		propDesc.setShortDescription("Kafka SSL Keystore");

		propDesc =  property("kafkaSslKeystorePassword", TypeEditor.PasswordEditor);
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "Chang3M3");
		propDesc.setDisplayName("KafkaSSLKeystorePassword");
		propDesc.setShortDescription("Kafka SSL Keystore Password");

		propDesc =  property("kafkaSslTruststore");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "");
		propDesc.setDisplayName("KafkaSSLTruststore");
		propDesc.setShortDescription("Kafka SSL Truststore");
		
		propDesc =  property("kafkaSslTruststorePassword", TypeEditor.PasswordEditor);
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE); 
		propDesc.setValue(DEFAULT, "Chang3M3");
		propDesc.setDisplayName("KafkaSSLTruststorePassword");
		propDesc.setShortDescription("Kafka SSL Truststore Password");
		

		if (LOGGER.isDebugEnabled()) {
			String pubDescriptorsAsString = Arrays.stream(getPropertyDescriptors())
					.map(pd -> pd.getName() + "=" + pd.getDisplayName()).collect(Collectors.joining(" ,"));
			LOGGER.debug(pubDescriptorsAsString);
		}

	}

}
