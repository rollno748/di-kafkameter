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

import com.di.jmeter.kafka.utils.VariableSettings;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;

public class KafkaProducerSamplerBeanInfo extends BeanInfoSupport {
	
	public KafkaProducerSamplerBeanInfo() {
		super(KafkaProducerSampler.class);

		createPropertyGroup("Variable Name bound to Kafka Client", new String[] {"kafkaProducerClientVariableName"});
		createPropertyGroup("Message to Produce", new String[] { "kafkaTopic","partitionString","kafkaMessageKey","kafkaMessage","messageHeaders" });

		PropertyDescriptor kafkaProducerClientVariableNamePropDesc =  property("kafkaProducerClientVariableName");
		kafkaProducerClientVariableNamePropDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		kafkaProducerClientVariableNamePropDesc.setValue(DEFAULT, "KafkaProducerClient");
		kafkaProducerClientVariableNamePropDesc.setDisplayName("Variable Name of Producer Client declared in Config element");
		kafkaProducerClientVariableNamePropDesc.setShortDescription("Variable name declared in Kafka Producer client config");

		PropertyDescriptor propDesc = property("kafkaTopic");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		propDesc.setValue(DEFAULT, "kafka_topic");
		propDesc.setDisplayName("Kafka Topic");

		propDesc = property("partitionString");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		propDesc.setValue(DEFAULT, "");
		propDesc.setDisplayName("Partition String");
		propDesc.setShortDescription("Leave it blank/empty if not required");

		propDesc = property("kafkaMessageKey");
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		propDesc.setValue(DEFAULT, "Key");
		propDesc.setDisplayName("Kafka Message Key");
		propDesc.setShortDescription("Kafka Message Key (blank/empty for null key)");

		propDesc = property("kafkaMessage", TypeEditor.TextAreaEditor);
		propDesc.setValue(NOT_UNDEFINED, Boolean.TRUE);
		propDesc.setValue(DEFAULT, "{\"Message\": \"It's a Hello from DI Kafka Sampler!\"}");
		propDesc.setDisplayName("Kafka Message");

		PropertyDescriptor headerTable = property("messageHeaders", TypeEditor.TableEditor);
		headerTable.setValue(TableEditor.CLASSNAME, VariableSettings.class.getName());
		headerTable.setValue(TableEditor.HEADERS, new String[]{ "Key", "Value" } );
		headerTable.setValue(TableEditor.OBJECT_PROPERTIES, new String[]{ VariableSettings.KEY, VariableSettings.VALUE } );
		headerTable.setValue(DEFAULT, new ArrayList<>());
		headerTable.setValue(NOT_UNDEFINED, Boolean.TRUE);
		headerTable.setDisplayName("Message Headers (Optional)");
	}
}
