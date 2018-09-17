package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;

@Deprecated
public class HttpEntityWrapper implements HttpEntity {
    protected HttpEntity wrappedEntity;

    public HttpEntityWrapper(HttpEntity wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped entity must not be null");
        }
        this.wrappedEntity = wrapped;
    }

    public boolean isRepeatable() {
        return this.wrappedEntity.isRepeatable();
    }

    public boolean isChunked() {
        return this.wrappedEntity.isChunked();
    }

    public long getContentLength() {
        return this.wrappedEntity.getContentLength();
    }

    public Header getContentType() {
        return this.wrappedEntity.getContentType();
    }

    public Header getContentEncoding() {
        return this.wrappedEntity.getContentEncoding();
    }

    public InputStream getContent() throws IOException {
        return this.wrappedEntity.getContent();
    }

    public void writeTo(OutputStream outstream) throws IOException {
        this.wrappedEntity.writeTo(outstream);
    }

    public boolean isStreaming() {
        return this.wrappedEntity.isStreaming();
    }

    public void consumeContent() throws IOException {
        this.wrappedEntity.consumeContent();
    }
}
