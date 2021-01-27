package org.apache.http.message;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class BasicStatusLine implements StatusLine, Cloneable {
    private final ProtocolVersion protoVersion;
    private final String reasonPhrase;
    private final int statusCode;

    public BasicStatusLine(ProtocolVersion version, int statusCode2, String reasonPhrase2) {
        if (version == null) {
            throw new IllegalArgumentException("Protocol version may not be null.");
        } else if (statusCode2 >= 0) {
            this.protoVersion = version;
            this.statusCode = statusCode2;
            this.reasonPhrase = reasonPhrase2;
        } else {
            throw new IllegalArgumentException("Status code may not be negative.");
        }
    }

    @Override // org.apache.http.StatusLine
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override // org.apache.http.StatusLine
    public ProtocolVersion getProtocolVersion() {
        return this.protoVersion;
    }

    @Override // org.apache.http.StatusLine
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override // java.lang.Object
    public String toString() {
        return BasicLineFormatter.DEFAULT.formatStatusLine((CharArrayBuffer) null, this).toString();
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
