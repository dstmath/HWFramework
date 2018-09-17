package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.params.HttpProtocolParams;

@Deprecated
public class RequestUserAgent implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (!request.containsHeader(HTTP.USER_AGENT)) {
            String useragent = HttpProtocolParams.getUserAgent(request.getParams());
            if (useragent != null) {
                request.addHeader(HTTP.USER_AGENT, useragent);
            }
        }
    }
}
