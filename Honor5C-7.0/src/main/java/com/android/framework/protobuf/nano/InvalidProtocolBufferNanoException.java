package com.android.framework.protobuf.nano;

import java.io.IOException;

public class InvalidProtocolBufferNanoException extends IOException {
    private static final long serialVersionUID = -1616151763072450476L;

    public InvalidProtocolBufferNanoException(String description) {
        super(description);
    }

    static InvalidProtocolBufferNanoException truncatedMessage() {
        return new InvalidProtocolBufferNanoException("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.");
    }

    static InvalidProtocolBufferNanoException negativeSize() {
        return new InvalidProtocolBufferNanoException("CodedInputStream encountered an embedded string or message which claimed to have negative size.");
    }

    static InvalidProtocolBufferNanoException malformedVarint() {
        return new InvalidProtocolBufferNanoException("CodedInputStream encountered a malformed varint.");
    }

    static InvalidProtocolBufferNanoException invalidTag() {
        return new InvalidProtocolBufferNanoException("Protocol message contained an invalid tag (zero).");
    }

    static InvalidProtocolBufferNanoException invalidEndTag() {
        return new InvalidProtocolBufferNanoException("Protocol message end-group tag did not match expected tag.");
    }

    static InvalidProtocolBufferNanoException invalidWireType() {
        return new InvalidProtocolBufferNanoException("Protocol message tag had invalid wire type.");
    }

    static InvalidProtocolBufferNanoException recursionLimitExceeded() {
        return new InvalidProtocolBufferNanoException("Protocol message had too many levels of nesting.  May be malicious.  Use CodedInputStream.setRecursionLimit() to increase the depth limit.");
    }

    static InvalidProtocolBufferNanoException sizeLimitExceeded() {
        return new InvalidProtocolBufferNanoException("Protocol message was too large.  May be malicious.  Use CodedInputStream.setSizeLimit() to increase the size limit.");
    }
}
