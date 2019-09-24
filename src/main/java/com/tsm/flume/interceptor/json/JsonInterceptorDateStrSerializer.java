package com.tsm.flume.interceptor.json;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.conf.ComponentConfiguration;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JsonInterceptorDateStrSerializer implements
        JsonInterceptorSerializer {
    private DateTimeFormatter formatter;
    private DateTimeFormatter formatterTarget;
    private String defaultValue;

    @Override
    public void configure(Context context) {
        String pattern = context.getString("pattern");
        Preconditions.checkArgument(!StringUtils.isEmpty(pattern),
                "Must configure with a valid pattern");

        String patternTarget = context.getString("patternTarget");
        Preconditions.checkArgument(!StringUtils.isEmpty(patternTarget),
                "Must configure with a valid patternTarget");

        defaultValue = context.getString("defaultValue");
        formatter = DateTimeFormat.forPattern(pattern);
        formatterTarget = DateTimeFormat.forPattern(patternTarget);
    }

    @Override
    public String serialize(String value) {
        String reValue = defaultValue;
        try {
            if (StringUtils.isNotEmpty(value)) {
                DateTime dateTime = formatter.parseDateTime(value);
                reValue = dateTime.toString(formatterTarget);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return reValue;
    }

    @Override
    public void configure(ComponentConfiguration conf) {
    }
}
