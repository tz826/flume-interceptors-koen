package com.tsm.flume.interceptor.json;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.JSONEvent;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(JUnit4.class)
public class JsonInterceptorTest {

    @Before
    public void prepare() {
    }

    private JsonInterceptor getInterceptor(Context context) {
        JsonInterceptor.Builder interceptorBuilder = new JsonInterceptor.Builder();
        interceptorBuilder.configure(context);

        JsonInterceptor interceptor = interceptorBuilder.build();
        interceptor.initialize();
        return interceptor;
    }

    private String getDefaultEventBody() {
        return "{ " +
                "\"pageViewId\":\"4eae0122-052d-41ff-ac5c-120279891184\"," +
                "\"published\":\"2015-04-23T01:37:09+00:00\"," +
                "\"params\": {" +
                "\"v1\":\"1\"," +
                "\"v2\":\"2\"," +
                "\"v3\":\"3\"" +
                "}" +
                " }";
    }

    private String getInvalidEventBody() {
        return "{ \"pageViewId\":\"4eae0122-052d-41ff-ac5c-120279891184\",";
    }

    private Event getEvent(Map<String, String> headers, String body) {
        Event event = new JSONEvent();
        event.setBody(body.getBytes());
        event.setHeaders(headers);
        return event;
    }

    private Context getDefaultContext(String headerName, String headerJSONPath) {
        Context context = new Context();
        context.put("serializers", "s1");
        context.put("serializers.s1.name", "s1");
        if (!headerName.isEmpty()) {
            context.put("name", headerName);
        }
        if (!headerJSONPath.isEmpty()) {
            context.put("jsonpath", headerJSONPath);
        }
        return context;
    }

    @Test
    public void testBasicChecks() {

        String headerName = "testName";
        String headerJSONPath = "$.published";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getDefaultEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);

        JsonInterceptor interceptor = getInterceptor(context);

        Event interceptedEvent =
                interceptor.intercept(event);

        assertEquals("Event body should not have been altered",
                body,
                new String(interceptedEvent.getBody()));

        assertTrue("Header should now contain " + headerName,
                interceptedEvent.getHeaders().containsKey(headerName));

        String published = "2015-04-23T01:37:09+00:00";

        assertEquals("Header's " + headerName + " should be correct",
                published,
                interceptedEvent.getHeaders().get(headerName));

    }

    @Test
    public void testIncorrectJSONPathShoudNotChangeEvent() {

        String headerName = "testName";
        String headerJSONPath = "$.notExists";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getDefaultEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);

        JsonInterceptor interceptor = getInterceptor(context);

        Event interceptedEvent = interceptor.intercept(event);

        assertEquals(interceptedEvent, event);
    }

    @Test
    public void testNonScalarResultShoudNotChangeEvent() {

        String headerName = "testName";
        String headerJSONPath = "$.params";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getDefaultEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);

        JsonInterceptor interceptor = getInterceptor(context);

        Event interceptedEvent = interceptor.intercept(event);

        assertEquals(interceptedEvent, event);
    }

    @Test
    public void testInvalidEventBodyShoudNotChangeEvent() {

        String headerName = "testName";
        String headerJSONPath = "$.published";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getInvalidEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);

        JsonInterceptor interceptor = getInterceptor(context);

        Event interceptedEvent = interceptor.intercept(event);

        assertEquals(interceptedEvent, event);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testMissedHeaderName() {

        String headerName = "";
        String headerJSONPath = "$.published";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getInvalidEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);

        JsonInterceptor interceptor = getInterceptor(context);

        interceptor.intercept(event);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testMissedJSONPath() {

        String headerName = "testName";
        String headerJSONPath = "";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getInvalidEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);

        JsonInterceptor interceptor = getInterceptor(context);

        interceptor.intercept(event);
    }

    @Test
    public void testMillisSerializer() {

        String headerName = "testName";
        String headerJSONPath = "$.published";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getDefaultEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);
        context.put("serializers.s1.type", "com.tsm.flume.interceptor.json.JsonInterceptorMillisSerializer");
        context.put("serializers.s1.pattern", "yyyy-MM-dd'T'HH:mm:ssZ");

        JsonInterceptor interceptor = getInterceptor(context);

        Event interceptedEvent = interceptor.intercept(event);

        assertEquals("Event body should not have been altered",
                body,
                new String(interceptedEvent.getBody()));

        assertTrue("Header should now contain " + headerName,
                interceptedEvent.getHeaders().containsKey(headerName));

        String published = "1429753029000"; // => 2015-04-23T01:37:09+00:00

        assertEquals("Header's " + headerName + " should be correct",
                published,
                interceptedEvent.getHeaders().get(headerName));
    }

    @Test
    public void testDateStrSerializer() {

        String headerName = "testName";
        String headerJSONPath = "$.published";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("existingKey", "existingValue");

        String body = getDefaultEventBody();
        Event event = getEvent(headers, body);

        Context context = getDefaultContext(headerName, headerJSONPath);
        context.put("serializers.s1.type", "com.tsm.flume.interceptor.json.JsonInterceptorDateStrSerializer");
        context.put("serializers.s1.pattern", "yyyy-MM-dd'T'HH:mm:ssZ");
        context.put("serializers.s1.patternTarget", "yyyyMMdd");

        JsonInterceptor interceptor = getInterceptor(context);

        Event interceptedEvent = interceptor.intercept(event);

        assertEquals("Event body should not have been altered",
                body,
                new String(interceptedEvent.getBody()));

        assertTrue("Header should now contain " + headerName,
                interceptedEvent.getHeaders().containsKey(headerName));

        String published = "20150423"; // => 2015-04-23T01:37:09+00:00

        assertEquals("Header's " + headerName + " should be correct",
                published,
                interceptedEvent.getHeaders().get(headerName));
    }

}
