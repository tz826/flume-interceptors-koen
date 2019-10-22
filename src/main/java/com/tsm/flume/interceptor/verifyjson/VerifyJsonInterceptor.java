package com.tsm.flume.interceptor.verifyjson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Interceptor class that appends a static, pre-configured header to all events.
 */
public class VerifyJsonInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(VerifyJsonInterceptor.class);

    String headerName;
    String headerValue;
    Boolean preserveExisting;
    String eventJsonKey;
    String eventTimeKey;
    //String eventTimePattern;
    final String sourceCharset;
    final String targetCharset;

    private DateTimeFormatter formatter;

    /**
     * Only {@link VerifyJsonInterceptor.Builder} can build me
     */
    private VerifyJsonInterceptor(String headerName, String headerValue, Boolean preserveExisting, String eventJsonKey, String eventTimeKey, String eventTimePattern, String sourceCharset, String targetCharset) {
        this.headerName = headerName;
        this.headerValue = headerValue;
        this.preserveExisting = preserveExisting;
        this.eventJsonKey = eventJsonKey;
        this.eventTimeKey = eventTimeKey;
        //this.eventTimePattern = eventTimePattern;
        this.sourceCharset = sourceCharset;
        this.targetCharset = targetCharset;

        formatter = DateTimeFormat.forPattern(eventTimePattern);
    }


    @Override
    public void initialize() {
        // no-op
    }

    public static boolean validateJson(String jsonStr) {
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(jsonStr);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            return false;
        }

        if (jsonElement == null) {
            return false;
        }

        if (!jsonElement.isJsonObject()) {
            return false;
        }
        return true;
    }

    /**
     * Modifies events in-place.
     */
    @Override
    public Event intercept(Event event) {

        byte[] content = event.getBody();
        String contentStr = new String(content, Charset.forName(sourceCharset));
        Boolean isJson = validateJson(contentStr);
        if (!isJson) {
            Map<String, String> headers = event.getHeaders();
            if (headers.containsKey(headerName) && preserveExisting) {
                headers.put(headerName, headerValue);
            }
            else
            {
                headers.put(headerName, headerValue);
            }

            JsonObject jsonObject = new JsonObject();

            DateTime dateTime = DateTime.now();
            jsonObject.addProperty(eventTimeKey, dateTime.toString(formatter));
            jsonObject.addProperty(eventJsonKey, contentStr);

            event.setBody(jsonObject.toString().getBytes(Charset.forName(targetCharset)));
        }

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
        String headerValue;
        Boolean preserveExisting;
        String eventJsonKey;
        String eventTimeKey;
        String eventTimePattern;
        String sourceCharset;
        String targetCharset;

        @Override
        public void configure(Context context) {
            headerName = context.getString(Constants.HEADER_NAME, Constants.HEADER_NAME_DEFAULT);
            headerValue = context.getString(Constants.HEADER_VALUE, Constants.HEADER_VALUE_DEFAULT);
            preserveExisting = context.getBoolean(Constants.PRESERVE_EXISTING, Constants.PRESERVE_EXISTING_DEFAULT);
            eventJsonKey = context.getString(Constants.EVENT_JSON_KEY, Constants.EVENT_JSON_KEY_DEFAULT);
            eventTimeKey = context.getString(Constants.EVENT_TIME_KEY, Constants.EVENT_TIME_KEY_DEFAULT);
            eventTimePattern = context.getString(Constants.EVENT_TIME_PATTERN, Constants.EVENT_TIME_PATTERN_DEFAULT);
            sourceCharset = context.getString(Constants.SOURCECHARSET, Constants.CHARSET_DEFAULT);
            targetCharset = context.getString(Constants.TARGETCHARSET, sourceCharset);
        }

        @Override
        public Interceptor build() {
            logger.info(String.format(
                    "Creating ParseJsonInterceptor:headerName=%s, headerValue=%s, preserveExisting=%s, eventJsonKey=%s, eventTimeKey=%s, eventTimePattern=%s,source_charset=%s,target_charset=%s",
                    headerName, headerValue, preserveExisting, eventJsonKey, eventTimeKey, eventTimePattern, sourceCharset, targetCharset));

            return new VerifyJsonInterceptor(headerName, headerValue, preserveExisting, eventJsonKey, eventTimeKey, eventTimePattern, sourceCharset, targetCharset);
        }

    }

    public static class Constants {

        private static String HEADER_NAME = "headerName";
        private static String HEADER_NAME_DEFAULT = "verify_json";

        private static String HEADER_VALUE = "headerValue";
        private static String HEADER_VALUE_DEFAULT = "verify_error_";

        private static String PRESERVE_EXISTING = "preserveExisting";
        private static Boolean PRESERVE_EXISTING_DEFAULT = true;

        private static String EVENT_TIME_KEY = "eventTimeKey";
        private static String EVENT_TIME_KEY_DEFAULT = "time";

        private static String EVENT_TIME_PATTERN = "eventTimePattern";
        private static String EVENT_TIME_PATTERN_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        private static String EVENT_JSON_KEY = "eventJsonKey";
        private static String EVENT_JSON_KEY_DEFAULT = "event_json";


        public static final String SOURCECHARSET = "charset";
        public static final String SOURCECHARSET_DEFAULT = "charset";

        public static final String TARGETCHARSET = "target_charset";
        public static final String CHARSET_DEFAULT = "utf-8";

    }

}
