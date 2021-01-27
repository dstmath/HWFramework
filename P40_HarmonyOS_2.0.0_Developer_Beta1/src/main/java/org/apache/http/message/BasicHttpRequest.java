package org.apache.http.message;

import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.params.HttpProtocolParams;

@Deprecated
public class BasicHttpRequest extends AbstractHttpMessage implements HttpRequest {
    private final String method;
    private final RequestLine requestline;
    private final String uri;

    public BasicHttpRequest(String method2, String uri2) {
        if (method2 == null) {
            throw new IllegalArgumentException("Method name may not be null");
        } else if (uri2 != null) {
            this.method = method2;
            this.uri = uri2;
            this.requestline = null;
        } else {
            throw new IllegalArgumentException("Request URI may not be null");
        }
    }

    public BasicHttpRequest(String method2, String uri2, ProtocolVersion ver) {
        this(new BasicRequestLine(method2, uri2, ver));
    }

    public BasicHttpRequest(RequestLine requestline2) {
        if (requestline2 != null) {
            this.requestline = requestline2;
            this.method = requestline2.getMethod();
            this.uri = requestline2.getUri();
            return;
        }
        throw new IllegalArgumentException("Request line may not be null");
    }

    @Override // org.apache.http.HttpMessage
    public ProtocolVersion getProtocolVersion() {
        RequestLine requestLine = this.requestline;
        if (requestLine != null) {
            return requestLine.getProtocolVersion();
        }
        return HttpProtocolParams.getVersion(getParams());
    }

    @Override // org.apache.http.HttpRequest
    public RequestLine getRequestLine() {
        RequestLine requestLine = this.requestline;
        if (requestLine != null) {
            return requestLine;
        }
        return new BasicRequestLine(this.method, this.uri, HttpProtocolParams.getVersion(getParams()));
    }
}
