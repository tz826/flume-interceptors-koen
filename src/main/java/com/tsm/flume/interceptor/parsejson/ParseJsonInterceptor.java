package com.tsm.flume.interceptor.parsejson;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.avro.data.Json;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Interceptor class that appends a static, pre-configured header to all events.
 * <p>
 * Properties:<p>
 * <p>
 * key: Key to use in json
 * (default is "data")<p>
 * <p>
 * charset: charset to use in string of charset .
 * (default is "utf-8")<p>
 * <p>
 * Sample config:<p>
 *
 * <code>
 * agent.sources.r1.channels = c1<p>
 * agent.sources.r1.type = SEQ<p>
 * agent.sources.r1.interceptors = i1<p>
 * agent.sources.r1.interceptors.i1.type = com.tsm.flume.interceptor.parsejson.ParseJsonInterceptor$Builder<p>
 * agent.sources.r1.interceptors.i1.key = data<p>
 * agent.sources.r1.interceptors.i1.charset= utf-8<p>
 * </code>
 */
public class ParseJsonInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(ParseJsonInterceptor.class);

    private String headerName;
    private Boolean useHeaderName;
    private String fixedValue;
    private final String key;
    private final String sourceCharset;
    private final String targetCharset;

    /**
     * Only {@link ParseJsonInterceptor.Builder} can build me
     */
    private ParseJsonInterceptor(String headerName, Boolean useHeaderName, String fixedValue, String key, String sourceCharset, String targetCharset) {
        this.headerName = headerName;
        this.useHeaderName = useHeaderName;
        this.fixedValue = fixedValue;
        this.key = key;
        this.sourceCharset = sourceCharset;
        this.targetCharset = targetCharset;
    }

    @Override
    public void initialize() {
        // no-op
    }

    /**
     * Modifies events in-place.
     */
    @Override
    public Event intercept(Event event) {

        byte[] content = event.getBody();
        String keyValue = fixedValue;
        if(useHeaderName)
        {
            Map<String, String> headers = event.getHeaders();

            if (headers.containsKey(headerName)) {
                keyValue = headers.get(headerName);
            }
        }
        JsonObject jsonObject = new JsonParser().parse(new String(content, Charset.forName(sourceCharset))).getAsJsonObject();
        jsonObject.addProperty(key, keyValue);
        event.setBody(jsonObject.toString().getBytes(Charset.forName(targetCharset)));
        return event;
    }

    /**
     * Delegates to {@link #intercept(Event)} in a loop.
     *
     * @param events
     * @return
     */
    @Override
    /**
     */
    public List<Event> intercept(List<Event> events) {
        for (Event event : events) {
            intercept(event);
        }
        return events;
    }

    @Override
    public void close() {
        // no-op
    }

    /**
     * Builder which builds new instance of the StaticInterceptor.
     */
    public static class Builder implements Interceptor.Builder {

        String headerName;
        Boolean useHeaderName;
        String fixedValue;
        String key;
        String sourceCharset;
        String targetCharset;

        @Override
        public void configure(Context context) {
            headerName = context.getString(Constants.HEADER_NAME);
            useHeaderName = context.getBoolean(Constants.USE_HEADER_NAME);
            fixedValue = context.getString(Constants.FIXED_VALUE);
            key = context.getString(Constants.KEY);
            sourceCharset = context.getString(Constants.SOURCECHARSET, Constants.CHARSET_DEFAULT);
            targetCharset = context.getString(Constants.TARGETCHARSET, sourceCharset);
        }

        @Override
        public Interceptor build() {
            logger.info(String.format(
                    "Creating ParseJsonInterceptor:headerName=%s, useHeaderName=%s, fixedValue=%s, key=%s,source_charset=%s,target_charset=%s",
                    headerName, useHeaderName, fixedValue, key, sourceCharset, targetCharset));

            Preconditions.checkArgument(headerName != null, "headerName name was misconfigured");
            Preconditions.checkArgument(key != null, "key name was misconfigured");

            return new ParseJsonInterceptor(headerName, useHeaderName, fixedValue, key, sourceCharset, targetCharset);
        }

    }

    public static class Constants {

        private static String HEADER_NAME= "headerName";
        private static String USE_HEADER_NAME = "useHeaderName";
        private static String FIXED_VALUE = "fixedValue";

        public static final String KEY = "key";

        public static final String SOURCECHARSET = "charset";
        public static final String TARGETCHARSET = "target_charset";
        public static final String CHARSET_DEFAULT = "utf-8";

    }
}
