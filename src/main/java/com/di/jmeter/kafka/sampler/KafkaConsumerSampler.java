package com.di.jmeter.kafka.sampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;

import java.io.Serializable;

public class KafkaConsumerSampler extends AbstractTestElement
        implements Sampler, TestBean, ConfigMergabilityIndicator, TestStateListener, TestElement, Serializable, Searchable {

    @Override
    public boolean applies(ConfigTestElement configTestElement) {
        return false;
    }

    @Override
    public SampleResult sample(Entry entry) {
        return null;
    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String s) {

    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String s) {

    }
}
