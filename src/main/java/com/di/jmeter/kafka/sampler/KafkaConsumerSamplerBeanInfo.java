package com.di.jmeter.kafka.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

public class KafkaConsumerSamplerBeanInfo extends BeanInfoSupport {

    public KafkaConsumerSamplerBeanInfo() {
        super(KafkaConsumerSampler.class);
        createPropertyGroup("Variable Name bound to Kafka Client", new String[] {"kafkaConsumerClientVariableName"});
        createPropertyGroup("Consumer Settings", new String[] {"commitType", "autoCommit"}); //poll timeout

        PropertyDescriptor varPropDesc = property("kafkaConsumerClientVariableName");
        varPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        varPropDesc.setValue(DEFAULT, "Kafka Consumer Client");
        varPropDesc.setDisplayName("Variable Name of Consumer Client declared in Config element");
        varPropDesc.setShortDescription("Variable name declared in Kafka Consumer client config");

        PropertyDescriptor consumerSettingsPropDesc = property("commitType");
        consumerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        consumerSettingsPropDesc.setValue(DEFAULT, "Sync");
        consumerSettingsPropDesc.setDisplayName("Commit Type");
        consumerSettingsPropDesc.setShortDescription("Commit type - Sync/Async");

        consumerSettingsPropDesc = property("autoCommit");
        consumerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        consumerSettingsPropDesc.setValue(DEFAULT, Boolean.TRUE);
        consumerSettingsPropDesc.setDisplayName("Auto Commit");
        consumerSettingsPropDesc.setShortDescription("Commit offsets returned on the last poll() for all the subscribed list of topics and partitions");

    }


}
