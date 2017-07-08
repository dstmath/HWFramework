package org.apache.http.entity;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

@Deprecated
public abstract class AbstractHttpEntity implements HttpEntity {
    protected boolean chunked;
    protected Header contentEncoding;
    protected Header contentType;

    protected AbstractHttpEntity() {
    }

    public Header getContentType() {
        return this.contentType;
    }

    public Header getContentEncoding() {
        return this.contentEncoding;
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }

    public void setContentType(String ctString) {
        Header header = null;
        if (ctString != null) {
            header = new BasicHeader(HTTP.CONTENT_TYPE, ctString);
        }
        setContentType(header);
    }

    public void setContentEncoding(Header contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public void setContentEncoding(String ceString) {
        Header header = null;
        if (ceString != null) {
            header = new BasicHeader(HTTP.CONTENT_ENCODING, ceString);
        }
        setContentEncoding(header);
    }

    public void setChunked(boolean b) {
        this.chunked = b;
    }

    public void consumeContent() throws IOException, UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException("streaming entity does not implement consumeContent()");
        }
    }
}
