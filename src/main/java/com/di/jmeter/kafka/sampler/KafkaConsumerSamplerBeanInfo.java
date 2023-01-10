package com.di.jmeter.kafka.sampler;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.TestBean;

public class KafkaConsumerSamplerBeanInfo extends BeanInfoSupport {
    /**
     * Construct a BeanInfo for the given class.
     *
     * @param beanClass class for which to construct a BeanInfo
     */
    protected KafkaConsumerSamplerBeanInfo(Class<? extends TestBean> beanClass) {
        super(beanClass);
    }


}
