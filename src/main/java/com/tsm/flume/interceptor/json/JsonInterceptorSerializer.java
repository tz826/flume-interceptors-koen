package com.tsm.flume.interceptor.json;

import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurableComponent;

public interface JsonInterceptorSerializer extends Configurable,
        ConfigurableComponent {

    String serialize(String value);
}
