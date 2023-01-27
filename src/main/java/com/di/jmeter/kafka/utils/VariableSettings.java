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
package com.di.jmeter.kafka.utils;

import org.apache.jmeter.testelement.AbstractTestElement;

public class VariableSettings extends AbstractTestElement {

	private static final long serialVersionUID = 5456773306165856817L;

	public static final String KEY = "headerKey";
	public static final String VALUE = "headerValue";
	public static final String CONFIG_KEY = "configKey";
	public static final String CONFIG_VALUE = "configValue";

	public String getHeaderKey() {
		return getProperty(KEY).getStringValue();
	}

	public void setHeaderKey(String fieldName) {
		setProperty(KEY, fieldName);
	}

	public String getHeaderValue() {
		return getProperty(VALUE).getStringValue();
	}

	public void setHeaderValue(String propertyValue) {
		setProperty(VALUE, propertyValue);
	}
	
	public String getConfigKey() {
		return getProperty(CONFIG_KEY).getStringValue();
	}

	public void setConfigKey(String fieldName) {
		setProperty(CONFIG_KEY, fieldName);
	}

	public String getConfigValue() {
		return getProperty(CONFIG_VALUE).getStringValue();
	}

	public void setConfigValue(String propertyValue) {
		setProperty(CONFIG_VALUE, propertyValue);
	}
	

}
