package com.tsm.flume.interceptor.json;

import org.apache.flume.Context;
import org.apache.flume.conf.ComponentConfiguration;

public class JsonInterceptorPassThroughSerializer implements JsonInterceptorSerializer {


    @Override
    public String serialize(String value) {
        return value;
    }

    @Override
    public void configure(Context context) {
    }

    @Override
    public void configure(ComponentConfiguration conf) {
    }

}
