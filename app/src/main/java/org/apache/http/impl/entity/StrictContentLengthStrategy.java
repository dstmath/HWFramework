package org.apache.http.impl.entity;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.protocol.HTTP;

@Deprecated
public class StrictContentLengthStrategy implements ContentLengthStrategy {
    public long determineLength(HttpMessage message) throws HttpException {
        if (message == null) {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
        Header transferEncodingHeader = message.getFirstHeader(HTTP.TRANSFER_ENCODING);
        Header contentLengthHeader = message.getFirstHeader(HTTP.CONTENT_LEN);
        String s;
        if (transferEncodingHeader != null) {
            s = transferEncodingHeader.getValue();
            if (HTTP.CHUNK_CODING.equalsIgnoreCase(s)) {
                if (!message.getProtocolVersion().lessEquals(HttpVersion.HTTP_1_0)) {
                    return -2;
                }
                throw new ProtocolException("Chunked transfer encoding not allowed for " + message.getProtocolVersion());
            } else if (HTTP.IDENTITY_CODING.equalsIgnoreCase(s)) {
                return -1;
            } else {
                throw new ProtocolException("Unsupported transfer encoding: " + s);
            }
        } else if (contentLengthHeader == null) {
            return -1;
        } else {
            s = contentLengthHeader.getValue();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new ProtocolException("Invalid content length: " + s);
            }
        }
    }
}
