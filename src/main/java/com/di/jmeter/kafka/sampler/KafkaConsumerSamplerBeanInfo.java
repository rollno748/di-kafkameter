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
