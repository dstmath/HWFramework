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

    public BasicHttpRequest(String method, String uri) {
        if (method == null) {
            throw new IllegalArgumentException("Method name may not be null");
        } else if (uri == null) {
            throw new IllegalArgumentException("Request URI may not be null");
        } else {
            this.method = method;
            this.uri = uri;
            this.requestline = null;
        }
    }

    public BasicHttpRequest(String method, String uri, ProtocolVersion ver) {
        this(new BasicRequestLine(method, uri, ver));
    }

    public BasicHttpRequest(RequestLine requestline) {
        if (requestline == null) {
            throw new IllegalArgumentException("Request line may not be null");
        }
        this.requestline = requestline;
        this.method = requestline.getMethod();
        this.uri = requestline.getUri();
    }

    public ProtocolVersion getProtocolVersion() {
        if (this.requestline != null) {
            return this.requestline.getProtocolVersion();
        }
        return HttpProtocolParams.getVersion(getParams());
    }

    public RequestLine getRequestLine() {
        if (this.requestline != null) {
            return this.requestline;
        }
        return new BasicRequestLine(this.method, this.uri, HttpProtocolParams.getVersion(getParams()));
    }
}
