package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HTTP;

@Deprecated
public class BasicHttpEntityEnclosingRequest extends BasicHttpRequest implements HttpEntityEnclosingRequest {
    private HttpEntity entity;

    public BasicHttpEntityEnclosingRequest(String method, String uri) {
        super(method, uri);
    }

    public BasicHttpEntityEnclosingRequest(String method, String uri, ProtocolVersion ver) {
        this(new BasicRequestLine(method, uri, ver));
    }

    public BasicHttpEntityEnclosingRequest(RequestLine requestline) {
        super(requestline);
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public boolean expectContinue() {
        Header expect = getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        return expect != null ? HTTP.EXPECT_CONTINUE.equalsIgnoreCase(expect.getValue()) : false;
    }
}
