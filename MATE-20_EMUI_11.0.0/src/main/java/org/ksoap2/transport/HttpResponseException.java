package org.ksoap2.transport;

import java.io.IOException;
import java.util.List;

public class HttpResponseException extends IOException {
    private List responseHeaders;
    private int statusCode;

    public HttpResponseException(int statusCode2) {
        this.statusCode = statusCode2;
    }

    public HttpResponseException(String detailMessage, int statusCode2) {
        super(detailMessage);
        this.statusCode = statusCode2;
    }

    public HttpResponseException(String detailMessage, int statusCode2, List responseHeaders2) {
        super(detailMessage);
        this.statusCode = statusCode2;
        this.responseHeaders = responseHeaders2;
    }

    public HttpResponseException(String message, Throwable cause, int statusCode2) {
        super(message, cause);
        this.statusCode = statusCode2;
    }

    public HttpResponseException(Throwable cause, int statusCode2) {
        super(cause);
        this.statusCode = statusCode2;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public List getResponseHeaders() {
        return this.responseHeaders;
    }
}
