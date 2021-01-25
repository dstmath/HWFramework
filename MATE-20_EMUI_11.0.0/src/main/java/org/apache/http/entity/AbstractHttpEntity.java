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

    @Override // org.apache.http.HttpEntity
    public Header getContentType() {
        return this.contentType;
    }

    @Override // org.apache.http.HttpEntity
    public Header getContentEncoding() {
        return this.contentEncoding;
    }

    @Override // org.apache.http.HttpEntity
    public boolean isChunked() {
        return this.chunked;
    }

    public void setContentType(Header contentType2) {
        this.contentType = contentType2;
    }

    public void setContentType(String ctString) {
        Header h = null;
        if (ctString != null) {
            h = new BasicHeader(HTTP.CONTENT_TYPE, ctString);
        }
        setContentType(h);
    }

    public void setContentEncoding(Header contentEncoding2) {
        this.contentEncoding = contentEncoding2;
    }

    public void setContentEncoding(String ceString) {
        Header h = null;
        if (ceString != null) {
            h = new BasicHeader(HTTP.CONTENT_ENCODING, ceString);
        }
        setContentEncoding(h);
    }

    public void setChunked(boolean b) {
        this.chunked = b;
    }

    @Override // org.apache.http.HttpEntity
    public void consumeContent() throws IOException, UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException("streaming entity does not implement consumeContent()");
        }
    }
}
