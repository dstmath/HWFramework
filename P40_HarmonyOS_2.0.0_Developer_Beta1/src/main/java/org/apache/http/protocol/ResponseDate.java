package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;

@Deprecated
public class ResponseDate implements HttpResponseInterceptor {
    private static final HttpDateGenerator DATE_GENERATOR = new HttpDateGenerator();

    @Override // org.apache.http.HttpResponseInterceptor
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null.");
        } else if (response.getStatusLine().getStatusCode() >= 200 && !response.containsHeader(HTTP.DATE_HEADER)) {
            response.setHeader(HTTP.DATE_HEADER, DATE_GENERATOR.getCurrentDate());
        }
    }
}
