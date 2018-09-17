package org.apache.http.impl.entity;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

@Deprecated
public class LaxContentLengthStrategy implements ContentLengthStrategy {
    public long determineLength(HttpMessage message) throws HttpException {
        if (message == null) {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
        boolean strict = message.getParams().isParameterTrue(CoreProtocolPNames.STRICT_TRANSFER_ENCODING);
        Header transferEncodingHeader = message.getFirstHeader(HTTP.TRANSFER_ENCODING);
        Header contentLengthHeader = message.getFirstHeader(HTTP.CONTENT_LEN);
        int i;
        if (transferEncodingHeader != null) {
            try {
                HeaderElement[] encodings = transferEncodingHeader.getElements();
                if (strict) {
                    i = 0;
                    while (i < encodings.length) {
                        String encoding = encodings[i].getName();
                        if (encoding == null || encoding.length() <= 0 || (encoding.equalsIgnoreCase(HTTP.CHUNK_CODING) ^ 1) == 0 || (encoding.equalsIgnoreCase(HTTP.IDENTITY_CODING) ^ 1) == 0) {
                            i++;
                        } else {
                            throw new ProtocolException("Unsupported transfer encoding: " + encoding);
                        }
                    }
                }
                int len = encodings.length;
                if (HTTP.IDENTITY_CODING.equalsIgnoreCase(transferEncodingHeader.getValue())) {
                    return -1;
                }
                if (len > 0 && HTTP.CHUNK_CODING.equalsIgnoreCase(encodings[len - 1].getName())) {
                    return -2;
                }
                if (!strict) {
                    return -1;
                }
                throw new ProtocolException("Chunk-encoding must be the last one applied");
            } catch (ParseException px) {
                throw new ProtocolException("Invalid Transfer-Encoding header value: " + transferEncodingHeader, px);
            }
        } else if (contentLengthHeader == null) {
            return -1;
        } else {
            long contentlen = -1;
            Header[] headers = message.getHeaders(HTTP.CONTENT_LEN);
            if (!strict || headers.length <= 1) {
                i = headers.length - 1;
                while (i >= 0) {
                    Header header = headers[i];
                    try {
                        contentlen = Long.parseLong(header.getValue());
                        break;
                    } catch (NumberFormatException e) {
                        if (strict) {
                            throw new ProtocolException("Invalid content length: " + header.getValue());
                        }
                        i--;
                    }
                }
                if (contentlen >= 0) {
                    return contentlen;
                }
                return -1;
            }
            throw new ProtocolException("Multiple content length headers");
        }
    }
}
