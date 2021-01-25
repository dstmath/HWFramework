package org.apache.http.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

@Deprecated
public class BufferedHttpEntity extends HttpEntityWrapper {
    private final byte[] buffer;

    public BufferedHttpEntity(HttpEntity entity) throws IOException {
        super(entity);
        if (!entity.isRepeatable() || entity.getContentLength() < 0) {
            this.buffer = EntityUtils.toByteArray(entity);
        } else {
            this.buffer = null;
        }
    }

    @Override // org.apache.http.entity.HttpEntityWrapper, org.apache.http.HttpEntity
    public long getContentLength() {
        byte[] bArr = this.buffer;
        if (bArr != null) {
            return (long) bArr.length;
        }
        return this.wrappedEntity.getContentLength();
    }

    @Override // org.apache.http.entity.HttpEntityWrapper, org.apache.http.HttpEntity
    public InputStream getContent() throws IOException {
        byte[] bArr = this.buffer;
        if (bArr != null) {
            return new ByteArrayInputStream(bArr);
        }
        return this.wrappedEntity.getContent();
    }

    @Override // org.apache.http.entity.HttpEntityWrapper, org.apache.http.HttpEntity
    public boolean isChunked() {
        return this.buffer == null && this.wrappedEntity.isChunked();
    }

    @Override // org.apache.http.entity.HttpEntityWrapper, org.apache.http.HttpEntity
    public boolean isRepeatable() {
        return true;
    }

    @Override // org.apache.http.entity.HttpEntityWrapper, org.apache.http.HttpEntity
    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            byte[] bArr = this.buffer;
            if (bArr != null) {
                outstream.write(bArr);
            } else {
                this.wrappedEntity.writeTo(outstream);
            }
        } else {
            throw new IllegalArgumentException("Output stream may not be null");
        }
    }

    @Override // org.apache.http.entity.HttpEntityWrapper, org.apache.http.HttpEntity
    public boolean isStreaming() {
        return this.buffer == null && this.wrappedEntity.isStreaming();
    }
}
