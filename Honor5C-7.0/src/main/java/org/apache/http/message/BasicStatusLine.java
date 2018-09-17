package org.apache.http.message;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

@Deprecated
public class BasicStatusLine implements StatusLine, Cloneable {
    private final ProtocolVersion protoVersion;
    private final String reasonPhrase;
    private final int statusCode;

    public BasicStatusLine(ProtocolVersion version, int statusCode, String reasonPhrase) {
        if (version == null) {
            throw new IllegalArgumentException("Protocol version may not be null.");
        } else if (statusCode < 0) {
            throw new IllegalArgumentException("Status code may not be negative.");
        } else {
            this.protoVersion = version;
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
        }
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoVersion;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public String toString() {
        return BasicLineFormatter.DEFAULT.formatStatusLine(null, (StatusLine) this).toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
