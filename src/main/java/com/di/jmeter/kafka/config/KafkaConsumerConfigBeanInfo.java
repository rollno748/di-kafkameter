package com.di.jmeter.kafka.config;

import com.di.jmeter.kafka.utils.VariableSettings;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class KafkaConsumerConfigBeanInfo extends BeanInfoSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfigBeanInfo.class);

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

    public KafkaConsumerConfigBeanInfo() {
        super(KafkaConsumerConfig.class);

        createPropertyGroup("Variable Name bound to Kafka Client", new String[] {"kafkaConsumerClientVariableName"});
        //Connection configs
        createPropertyGroup("Kafka Connection Configs", new String[] {"kafkaBrokers", "groupId", "topic", "deSerializerKey", "deSerializerValue", "numberOfMsgToPoll", "autoCommit"});
        //Security configs
        createPropertyGroup("Security", new String[] {SECURITYTYPE, "kafkaSslTruststore", "kafkaSslTruststorePassword", "kafkaSslKeystore", "kafkaSslKeystorePassword", "kafkaSslPrivateKeyPass"});
        //Additional configs
        createPropertyGroup("Additional Configs", new String[] {"extraConfigs"});

        PropertyDescriptor consumerClientVariableNamePropDesc =  property("kafkaConsumerClientVariableName");
        consumerClientVariableNamePropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        consumerClientVariableNamePropDesc.setValue(DEFAULT, "KafkaConsumerClient");
        consumerClientVariableNamePropDesc.setDisplayName("Variable Name");
        consumerClientVariableNamePropDesc.setShortDescription("Variable name to use in Kafka Consumer Sampler");

        PropertyDescriptor connectionConfigPropDesc =  property("kafkaBrokers");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, "localhost1:9091,localhost2:9091");
        connectionConfigPropDesc.setDisplayName("Kafka Brokers");
        connectionConfigPropDesc.setShortDescription("List of Kafka Brokers - comma separated");

        connectionConfigPropDesc =  property("groupId");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, "consumer-group-1");
        connectionConfigPropDesc.setDisplayName("Group ID");
        connectionConfigPropDesc.setShortDescription("Group ID - Unique Id to identify the consumer group");

        connectionConfigPropDesc =  property("topic");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, "kafka_topic");
        connectionConfigPropDesc.setDisplayName("Topic");
        connectionConfigPropDesc.setShortDescription("Kafka Topic for the Consumer to subscribe");

        connectionConfigPropDesc =  property("deSerializerKey");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringDeserializer");
        connectionConfigPropDesc.setDisplayName("Deserializer Key");
        connectionConfigPropDesc.setShortDescription("Deserializer class for key");

        connectionConfigPropDesc =  property("deSerializerValue");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, "org.apache.kafka.common.serialization.StringDeserializer");
        connectionConfigPropDesc.setDisplayName("Deserializer Value");
        connectionConfigPropDesc.setShortDescription("Deserializer class for value");

        connectionConfigPropDesc = property("numberOfMsgToPoll");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, "1");
        connectionConfigPropDesc.setDisplayName("No. of Messages to Poll");
        connectionConfigPropDesc.setShortDescription("Number of messages to poll from the topic");

        connectionConfigPropDesc = property("autoCommit");
        connectionConfigPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        connectionConfigPropDesc.setValue(DEFAULT, Boolean.TRUE);
        connectionConfigPropDesc.setDisplayName("Auto Commit");
        connectionConfigPropDesc.setShortDescription("Commit offsets returned on the last poll() for all the subscribed list of topics and partitions");

        PropertyDescriptor securityDescriptor =  property(SECURITYTYPE, TypeEditor.ComboStringEditor);
        securityDescriptor.setValue(RESOURCE_BUNDLE, getBeanDescriptor().getValue(RESOURCE_BUNDLE));
        securityDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        securityDescriptor.setValue(DEFAULT, SECURITYTYPE_TAGS[PLAINTEXT]);
        securityDescriptor.setValue(NOT_OTHER, Boolean.FALSE);
        securityDescriptor.setValue(NOT_EXPRESSION, Boolean.FALSE);
        securityDescriptor.setValue(TAGS, SECURITYTYPE_TAGS);
        securityDescriptor.setDisplayName("Type");
        securityDescriptor.setShortDescription("Select the security type");

        securityDescriptor =  property("kafkaSslTruststore");
        securityDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        securityDescriptor.setValue(DEFAULT, "");
        securityDescriptor.setDisplayName("Truststore Location");
        securityDescriptor.setShortDescription("Kafka SSL Truststore file location");

        securityDescriptor =  property("kafkaSslTruststorePassword", TypeEditor.PasswordEditor);
        securityDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        securityDescriptor.setValue(DEFAULT, "Chang3M3");
        securityDescriptor.setDisplayName("Truststore Password");
        securityDescriptor.setShortDescription("Kafka SSL Truststore Password");

        securityDescriptor =  property("kafkaSslKeystore");
        securityDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        securityDescriptor.setValue(DEFAULT, "");
        securityDescriptor.setDisplayName("Keystore Location");
        securityDescriptor.setShortDescription("Kafka SSL Keystore file location");

        securityDescriptor =  property("kafkaSslKeystorePassword", TypeEditor.PasswordEditor);
        securityDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        securityDescriptor.setValue(DEFAULT, "Chang3M3");
        securityDescriptor.setDisplayName("Keystore Password");
        securityDescriptor.setShortDescription("Kafka SSL Keystore Password");

        securityDescriptor =  property("kafkaSslPrivateKeyPass", TypeEditor.PasswordEditor);
        securityDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        securityDescriptor.setValue(DEFAULT, "Chang3M3");
        securityDescriptor.setDisplayName("Keystore Private Key Password");
        securityDescriptor.setShortDescription("Kafka SSL Keystore private key password");

        PropertyDescriptor extraConfigProps = property("extraConfigs", TypeEditor.TableEditor);
        extraConfigProps.setValue(TableEditor.CLASSNAME, VariableSettings.class.getName());
        extraConfigProps.setValue(TableEditor.HEADERS, new String[]{ KAFKA_CONFIG_KEY, KAFKA_CONFIG_VALUE} );
        extraConfigProps.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ VariableSettings.CONFIG_KEY, VariableSettings.CONFIG_VALUE } );
        extraConfigProps.setValue(DEFAULT, new ArrayList<>());
        extraConfigProps.setValue(NOT_UNDEFINED, Boolean.TRUE);
        extraConfigProps.setDisplayName("Consumer Additional Properties (Optional)");

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
