package com.di.jmeter.kafka.config;

import com.di.jmeter.kafka.utils.VariableSettings;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class KafkaConsumerConfig extends ConfigTestElement
        implements TestBean, TestStateListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private List<VariableSettings> extraConfigs;
    private String kafkaConsumerClientVariableName;
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
    private String autoOffsetReset;

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();
        LOGGER.info("KafkaConsumerConfig:{}", kafkaConsumerClientVariableName);
        if (variables.getObject(kafkaConsumerClientVariableName) != null) {
            LOGGER.error("Kafka consumer is already running.");
        } else {
            synchronized (this) {
                try {
                    variables.putObject(kafkaConsumerClientVariableName+"props", getProps0());
                    variables.putObject(kafkaConsumerClientVariableName+"topic", getTopic());
                    LOGGER.info("Kafka consumer client successfully Initialized");
                } catch (Exception e) {
                    LOGGER.error("Error establishing kafka consumer client!", e);
                }
            }
        }
    }


    private Properties getProps0() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());//groupId
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, getDeSerializerKey());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, getDeSerializerValue());
        props.put("security.protocol", getSecurityType().replaceAll("securityType.", "").toUpperCase());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, isAutoCommit());

        LOGGER.info(getAutoOffsetReset());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, getAutoOffsetReset());
        LOGGER.debug("Consumer Additional Config Size::: " + getExtraConfigs().size());
        if (getExtraConfigs().size() >= 1) {
            LOGGER.info("Setting Producer up Additional properties");
            for (VariableSettings entry : getExtraConfigs()){
                props.put(entry.getConfigKey(), entry.getConfigValue());
                LOGGER.debug(String.format("Adding Producer property : %s", entry.getConfigKey()));
            }
        }

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Math.max(Integer.parseInt(getNumberOfMsgToPoll()), 1));

        if (getSecurityType().equalsIgnoreCase("securityType.ssl") || getSecurityType().equalsIgnoreCase("securityType.sasl_ssl")) {
            LOGGER.info("Kafka security type: " + getSecurityType().replaceAll("securityType.", "").toUpperCase());
            LOGGER.info(String.format("Setting up Kafka %s properties", getSecurityType()));
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
    }

    @Override
    public void testEnded(String host) {
        testEnded();
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


    public void setAutoOffsetReset(String autoOffsetReset){
        this.autoOffsetReset = autoOffsetReset;
    }
    public String getAutoOffsetReset(){
        return this.autoOffsetReset;
    }
}
