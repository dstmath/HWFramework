package org.apache.http.impl;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;

@Deprecated
public class DefaultHttpRequestFactory implements HttpRequestFactory {
    private static final String[] RFC2616_COMMON_METHODS = {HttpGet.METHOD_NAME};
    private static final String[] RFC2616_ENTITY_ENC_METHODS = {HttpPost.METHOD_NAME, HttpPut.METHOD_NAME};
    private static final String[] RFC2616_SPECIAL_METHODS = {HttpHead.METHOD_NAME, HttpOptions.METHOD_NAME, HttpDelete.METHOD_NAME, HttpTrace.METHOD_NAME};

    private static boolean isOneOf(String[] methods, String method) {
        for (String str : methods) {
            if (str.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    @Override // org.apache.http.HttpRequestFactory
    public HttpRequest newHttpRequest(RequestLine requestline) throws MethodNotSupportedException {
        if (requestline != null) {
            String method = requestline.getMethod();
            if (isOneOf(RFC2616_COMMON_METHODS, method)) {
                return new BasicHttpRequest(requestline);
            }
            if (isOneOf(RFC2616_ENTITY_ENC_METHODS, method)) {
                return new BasicHttpEntityEnclosingRequest(requestline);
            }
            if (isOneOf(RFC2616_SPECIAL_METHODS, method)) {
                return new BasicHttpRequest(requestline);
            }
            throw new MethodNotSupportedException(method + " method not supported");
        }
        throw new IllegalArgumentException("Request line may not be null");
    }

    @Override // org.apache.http.HttpRequestFactory
    public HttpRequest newHttpRequest(String method, String uri) throws MethodNotSupportedException {
        if (isOneOf(RFC2616_COMMON_METHODS, method)) {
            return new BasicHttpRequest(method, uri);
        }
        if (isOneOf(RFC2616_ENTITY_ENC_METHODS, method)) {
            return new BasicHttpEntityEnclosingRequest(method, uri);
        }
        if (isOneOf(RFC2616_SPECIAL_METHODS, method)) {
            return new BasicHttpRequest(method, uri);
        }
        throw new MethodNotSupportedException(method + " method not supported");
    }
}
