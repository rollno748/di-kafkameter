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
