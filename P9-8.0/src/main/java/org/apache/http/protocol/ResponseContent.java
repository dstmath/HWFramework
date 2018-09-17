package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;

@Deprecated
public class ResponseContent implements HttpResponseInterceptor {
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (response.containsHeader(HTTP.TRANSFER_ENCODING)) {
            throw new ProtocolException("Transfer-encoding header already present");
        } else if (response.containsHeader(HTTP.CONTENT_LEN)) {
            throw new ProtocolException("Content-Length header already present");
        } else {
            ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                long len = entity.getContentLength();
                if (entity.isChunked() && (ver.lessEquals(HttpVersion.HTTP_1_0) ^ 1) != 0) {
                    response.addHeader(HTTP.TRANSFER_ENCODING, HTTP.CHUNK_CODING);
                } else if (len >= 0) {
                    response.addHeader(HTTP.CONTENT_LEN, Long.toString(entity.getContentLength()));
                }
                if (!(entity.getContentType() == null || (response.containsHeader(HTTP.CONTENT_TYPE) ^ 1) == 0)) {
                    response.addHeader(entity.getContentType());
                }
                if (entity.getContentEncoding() != null && (response.containsHeader(HTTP.CONTENT_ENCODING) ^ 1) != 0) {
                    response.addHeader(entity.getContentEncoding());
                    return;
                }
                return;
            }
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_NO_CONTENT && status != HttpStatus.SC_NOT_MODIFIED && status != HttpStatus.SC_RESET_CONTENT) {
                response.addHeader(HTTP.CONTENT_LEN, "0");
            }
        }
    }
}
