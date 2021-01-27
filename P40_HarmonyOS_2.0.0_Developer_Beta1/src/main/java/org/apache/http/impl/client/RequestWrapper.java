package org.apache.http.impl.client;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpProtocolParams;

@Deprecated
public class RequestWrapper extends AbstractHttpMessage implements HttpUriRequest {
    private int execCount;
    private String method;
    private final HttpRequest original;
    private URI uri;
    private ProtocolVersion version;

    public RequestWrapper(HttpRequest request) throws ProtocolException {
        if (request != null) {
            this.original = request;
            setParams(request.getParams());
            if (request instanceof HttpUriRequest) {
                this.uri = ((HttpUriRequest) request).getURI();
                this.method = ((HttpUriRequest) request).getMethod();
                this.version = null;
            } else {
                RequestLine requestLine = request.getRequestLine();
                try {
                    this.uri = new URI(requestLine.getUri());
                    this.method = requestLine.getMethod();
                    this.version = request.getProtocolVersion();
                } catch (URISyntaxException ex) {
                    throw new ProtocolException("Invalid request URI: " + requestLine.getUri(), ex);
                }
            }
            this.execCount = 0;
            return;
        }
        throw new IllegalArgumentException("HTTP request may not be null");
    }

    public void resetHeaders() {
        this.headergroup.clear();
        setHeaders(this.original.getAllHeaders());
    }

    @Override // org.apache.http.client.methods.HttpUriRequest
    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method2) {
        if (method2 != null) {
            this.method = method2;
            return;
        }
        throw new IllegalArgumentException("Method name may not be null");
    }

    @Override // org.apache.http.HttpMessage
    public ProtocolVersion getProtocolVersion() {
        ProtocolVersion protocolVersion = this.version;
        if (protocolVersion != null) {
            return protocolVersion;
        }
        return HttpProtocolParams.getVersion(getParams());
    }

    public void setProtocolVersion(ProtocolVersion version2) {
        this.version = version2;
    }

    @Override // org.apache.http.client.methods.HttpUriRequest
    public URI getURI() {
        return this.uri;
    }

    public void setURI(URI uri2) {
        this.uri = uri2;
    }

    @Override // org.apache.http.HttpRequest
    public RequestLine getRequestLine() {
        String method2 = getMethod();
        ProtocolVersion ver = getProtocolVersion();
        String uritext = null;
        URI uri2 = this.uri;
        if (uri2 != null) {
            uritext = uri2.toASCIIString();
        }
        if (uritext == null || uritext.length() == 0) {
            uritext = "/";
        }
        return new BasicRequestLine(method2, uritext, ver);
    }

    @Override // org.apache.http.client.methods.HttpUriRequest, org.apache.http.client.methods.AbortableHttpRequest
    public void abort() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override // org.apache.http.client.methods.HttpUriRequest
    public boolean isAborted() {
        return false;
    }

    public HttpRequest getOriginal() {
        return this.original;
    }

    public boolean isRepeatable() {
        return true;
    }

    public int getExecCount() {
        return this.execCount;
    }

    public void incrementExecCount() {
        this.execCount++;
    }
}
