package com.di.jmeter.kafka.config;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.di.jmeter.kafka.utils.VariableSettings;

public class KafkaProducerConfigBeanInfo extends BeanInfoSupport{

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfigBeanInfo.class);
	private static final String SECURITYTYPE= "securityType";
	private static final String[] SECURITYTYPE_TAGS = new String[4];
	static final int PLAINTEXT = 0;
	static final int SSL = 1;
	static final int SASL_PLAINTEXT = 2;
	static final int SASL_SSL = 3;
	private static final String KAFKA_CONFIG_KEY = "Config key";
	private static final String KAFKA_CONFIG_VALUE = "Config value";

	static {
		SECURITYTYPE_TAGS[PLAINTEXT] = "securityType.plaintext";
		SECURITYTYPE_TAGS[SSL] = "securityType.ssl";
		SECURITYTYPE_TAGS[SASL_PLAINTEXT] = "securityType.sasl_plaintext";
		SECURITYTYPE_TAGS[SASL_SSL] = "securityType.sasl_ssl";
	}

	public KafkaProducerConfigBeanInfo() {
		super(KafkaProducerConfig.class);

		createPropertyGroup("Variable Name bound to Kafka Client", new String[] {"kafkaProducerClientVariableName"});

		createPropertyGroup("Kafka Connection Configs", new String[] {"kafkaBrokers", "batchSize", "clientId", "serializerKey", "serializerValue"});
		//Security
		createPropertyGroup("Security", new String[] { SECURITYTYPE, "kafkaSslTruststore", "kafkaSslTruststorePassword", "kafkaSslKeystore", "kafkaSslKeystorePassword", "kafkaSslPrivateKeyPass"});
		//Additional Configs
		createPropertyGroup("Additional Configs", new String[] {"extraConfigs"});


		PropertyDescriptor kafkaProducerClientVariableNamePropDesc =  property("kafkaProducerClientVariableName");
		kafkaProducerClientVariableNamePropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		kafkaProducerClientVariableNamePropDesc.setValue(DEFAULT, "KafkaProducerClient");
		kafkaProducerClientVariableNamePropDesc.setDisplayName("Variable Name");
		kafkaProducerClientVariableNamePropDesc.setShortDescription("Variable Name to use in KafkaProducerSampler");

		PropertyDescriptor connectionConfigpropDesc =  property("kafkaBrokers");
		connectionConfigpropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		connectionConfigpropDesc.setValue(DEFAULT, "localhost1:9091,localhost2:9091");
		connectionConfigpropDesc.setDisplayName("Kafka Brokers");
		connectionConfigpropDesc.setShortDescription("List of Kafka Brokers - comma separated");

		connectionConfigpropDesc =  property("batchSize");
		connectionConfigpropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		connectionConfigpropDesc.setValue(DEFAULT, "16384");
		connectionConfigpropDesc.setDisplayName("Batch Size");
		connectionConfigpropDesc.setShortDescription("Batch Size");

		connectionConfigpropDesc =  property("clientId");
		connectionConfigpropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		connectionConfigpropDesc.setValue(DEFAULT, "JMeter-Producer-1");
		connectionConfigpropDesc.setDisplayName("Client ID");
		connectionConfigpropDesc.setShortDescription("Client ID - Unique Id to connect to Broker");

		connectionConfigpropDesc =  property("serializerKey");
		connectionConfigpropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		connectionConfigpropDesc.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringSerializer");
		connectionConfigpropDesc.setDisplayName("Serializer Key");
		connectionConfigpropDesc.setShortDescription("Serializer Key");

		connectionConfigpropDesc =  property("serializerValue");
		connectionConfigpropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		connectionConfigpropDesc.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringSerializer");
		connectionConfigpropDesc.setDisplayName("Serializer Value");
		connectionConfigpropDesc.setShortDescription("Serializer Value (must accept String input)");

		PropertyDescriptor securityPropDesc =  property(SECURITYTYPE, TypeEditor.ComboStringEditor);
		securityPropDesc.setValue(RESOURCE_BUNDLE, getBeanDescriptor().getValue(RESOURCE_BUNDLE));
		securityPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		securityPropDesc.setValue(DEFAULT, SECURITYTYPE_TAGS[PLAINTEXT]);
		securityPropDesc.setValue(NOT_OTHER, Boolean.FALSE);
		securityPropDesc.setValue(NOT_EXPRESSION, Boolean.FALSE);
		securityPropDesc.setValue(TAGS, SECURITYTYPE_TAGS);
		securityPropDesc.setDisplayName("Type");
		securityPropDesc.setShortDescription("Select the security type");

		securityPropDesc =  property("kafkaSslTruststore");
		securityPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		securityPropDesc.setValue(DEFAULT, "");
		securityPropDesc.setDisplayName("Truststore Location");
		securityPropDesc.setShortDescription("Kafka SSL Truststore file location");

		securityPropDesc =  property("kafkaSslTruststorePassword", TypeEditor.PasswordEditor);
		securityPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		securityPropDesc.setValue(DEFAULT, "Chang3M3");
		securityPropDesc.setDisplayName("Truststore Password");
		securityPropDesc.setShortDescription("Kafka SSL Truststore Password");

		securityPropDesc =  property("kafkaSslKeystore");
		securityPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		securityPropDesc.setValue(DEFAULT, "");
		securityPropDesc.setDisplayName("Keystore Location");
		securityPropDesc.setShortDescription("Kafka SSL Keystore file location");

		securityPropDesc =  property("kafkaSslKeystorePassword", TypeEditor.PasswordEditor);
		securityPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		securityPropDesc.setValue(DEFAULT, "Chang3M3");
		securityPropDesc.setDisplayName("Keystore Password");
		securityPropDesc.setShortDescription("Kafka SSL Keystore Password");

		securityPropDesc =  property("kafkaSslPrivateKeyPass", TypeEditor.PasswordEditor);
		securityPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		securityPropDesc.setValue(DEFAULT, "Chang3M3");
		securityPropDesc.setDisplayName("Keystore Private Key Password");
		securityPropDesc.setShortDescription("Kafka SSL Keystore private key password");

		PropertyDescriptor configProps = property("extraConfigs", TypeEditor.TableEditor);
		configProps.setValue(TableEditor.CLASSNAME, VariableSettings.class.getName());
		configProps.setValue(TableEditor.HEADERS, new String[]{ KAFKA_CONFIG_KEY, KAFKA_CONFIG_VALUE } );
		configProps.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ VariableSettings.CONFIG_KEY, VariableSettings.CONFIG_VALUE } );
		configProps.setValue(DEFAULT, new ArrayList<>());
		configProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
		configProps.setDisplayName("Producer Additional Properties (Optional)");

		if (LOGGER.isDebugEnabled()) {
			String pubDescriptorsAsString = Arrays.stream(getPropertyDescriptors())
					.map(pd -> pd.getName() + "=" + pd.getDisplayName()).collect(Collectors.joining(" ,"));
			LOGGER.debug(pubDescriptorsAsString);
		}
	}

	public static int getSecurityTypeAsInt(String mode) {
		if (mode == null || mode.length() == 0) {
			return PLAINTEXT;
		}
		for (int i = 0; i < SECURITYTYPE_TAGS.length; i++) {
			if (SECURITYTYPE_TAGS[i].equals(mode)) {
				return i;
			}
		}
		return -1;
	}
	public static String[] getSecurityTypeTags() {
		String[] copy = new String[SECURITYTYPE_TAGS.length];
		System.arraycopy(SECURITYTYPE_TAGS, 0, copy, 0, SECURITYTYPE_TAGS.length);
		return copy;
	}
}
