package org.apache.http.client.protocol;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class RequestDefaultHeaders implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        Collection<?> defHeaders = (Collection) request.getParams().getParameter(ClientPNames.DEFAULT_HEADERS);
        if (defHeaders != null) {
            Iterator defHeader$iterator = defHeaders.iterator();
            while (defHeader$iterator.hasNext()) {
                request.addHeader((Header) defHeader$iterator.next());
            }
        }
    }
}
