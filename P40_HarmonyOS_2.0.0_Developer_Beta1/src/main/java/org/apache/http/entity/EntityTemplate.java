package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public class EntityTemplate extends AbstractHttpEntity {
    private final ContentProducer contentproducer;

    public EntityTemplate(ContentProducer contentproducer2) {
        if (contentproducer2 != null) {
            this.contentproducer = contentproducer2;
            return;
        }
        throw new IllegalArgumentException("Content producer may not be null");
    }

    @Override // org.apache.http.HttpEntity
    public long getContentLength() {
        return -1;
    }

    @Override // org.apache.http.HttpEntity
    public InputStream getContent() {
        throw new UnsupportedOperationException("Entity template does not implement getContent()");
    }

    @Override // org.apache.http.HttpEntity
    public boolean isRepeatable() {
        return true;
    }

    @Override // org.apache.http.HttpEntity
    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            this.contentproducer.writeTo(outstream);
            return;
        }
        throw new IllegalArgumentException("Output stream may not be null");
    }

    @Override // org.apache.http.HttpEntity
    public boolean isStreaming() {
        return true;
    }

    @Override // org.apache.http.entity.AbstractHttpEntity, org.apache.http.HttpEntity
    public void consumeContent() throws IOException {
    }
}
