package org.apache.http.message;

import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class BasicRequestLine implements RequestLine, Cloneable {
    private final String method;
    private final ProtocolVersion protoversion;
    private final String uri;

    public BasicRequestLine(String method2, String uri2, ProtocolVersion version) {
        if (method2 == null) {
            throw new IllegalArgumentException("Method must not be null.");
        } else if (uri2 == null) {
            throw new IllegalArgumentException("URI must not be null.");
        } else if (version != null) {
            this.method = method2;
            this.uri = uri2;
            this.protoversion = version;
        } else {
            throw new IllegalArgumentException("Protocol version must not be null.");
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
        return BasicLineFormatter.DEFAULT.formatRequestLine((CharArrayBuffer) null, (RequestLine) this).toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
