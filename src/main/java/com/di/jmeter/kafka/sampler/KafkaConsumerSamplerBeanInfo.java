package com.di.jmeter.kafka.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;

import java.beans.PropertyDescriptor;

public class KafkaConsumerSamplerBeanInfo extends BeanInfoSupport {

    public KafkaConsumerSamplerBeanInfo() {
        super(KafkaConsumerSampler.class);
        createPropertyGroup("Variable Name bound to Kafka Client", new String[] {"kafkaConsumerClientVariableName"});
        createPropertyGroup("Consumer Settings", new String[] {"pollTimeout", "commitType"});

        PropertyDescriptor varPropDesc = property("kafkaConsumerClientVariableName");
        varPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        varPropDesc.setValue(DEFAULT, "KafkaConsumerClient");
        varPropDesc.setDisplayName("Variable Name of Consumer Client declared in Config element");
        varPropDesc.setShortDescription("Variable name declared in Kafka Consumer client config");

        PropertyDescriptor consumerSettingsPropDesc = property("pollTimeout");
        consumerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        consumerSettingsPropDesc.setValue(DEFAULT, "100");
        consumerSettingsPropDesc.setDisplayName("Poll Timeout");
        consumerSettingsPropDesc.setShortDescription("Consumer poll timeout (in MilliSecs)");

        consumerSettingsPropDesc = property("commitType");
        consumerSettingsPropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
        consumerSettingsPropDesc.setValue(DEFAULT, "Sync");
        consumerSettingsPropDesc.setDisplayName("Commit Type");
        consumerSettingsPropDesc.setShortDescription("Commit type - Sync/Async");

    }
}
