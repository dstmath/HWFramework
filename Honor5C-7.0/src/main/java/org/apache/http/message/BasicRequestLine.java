package org.apache.http.message;

import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;

@Deprecated
public class BasicRequestLine implements RequestLine, Cloneable {
    private final String method;
    private final ProtocolVersion protoversion;
    private final String uri;

    public BasicRequestLine(String method, String uri, ProtocolVersion version) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null.");
        } else if (uri == null) {
            throw new IllegalArgumentException("URI must not be null.");
        } else if (version == null) {
            throw new IllegalArgumentException("Protocol version must not be null.");
        } else {
            this.method = method;
            this.uri = uri;
            this.protoversion = version;
        }
    }

    public String getMethod() {
        return this.method;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoversion;
    }

    public String getUri() {
        return this.uri;
    }

    public String toString() {
        return BasicLineFormatter.DEFAULT.formatRequestLine(null, (RequestLine) this).toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
