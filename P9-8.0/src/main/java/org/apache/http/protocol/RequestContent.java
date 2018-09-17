package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;

@Deprecated
public class RequestContent implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            if (request.containsHeader(HTTP.TRANSFER_ENCODING)) {
                throw new ProtocolException("Transfer-encoding header already present");
            } else if (request.containsHeader(HTTP.CONTENT_LEN)) {
                throw new ProtocolException("Content-Length header already present");
            } else {
                ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity == null) {
                    request.addHeader(HTTP.CONTENT_LEN, "0");
                    return;
                }
                if (!entity.isChunked() && entity.getContentLength() >= 0) {
                    request.addHeader(HTTP.CONTENT_LEN, Long.toString(entity.getContentLength()));
                } else if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                    throw new ProtocolException("Chunked transfer encoding not allowed for " + ver);
                } else {
                    request.addHeader(HTTP.TRANSFER_ENCODING, HTTP.CHUNK_CODING);
                }
                if (!(entity.getContentType() == null || (request.containsHeader(HTTP.CONTENT_TYPE) ^ 1) == 0)) {
                    request.addHeader(entity.getContentType());
                }
                if (!(entity.getContentEncoding() == null || (request.containsHeader(HTTP.CONTENT_ENCODING) ^ 1) == 0)) {
                    request.addHeader(entity.getContentEncoding());
                }
            }
        }
    }
}
