package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public class InputStreamEntity extends AbstractHttpEntity {
    private static final int BUFFER_SIZE = 2048;
    private boolean consumed = false;
    private final InputStream content;
    private final long length;

    public InputStreamEntity(InputStream instream, long length2) {
        if (instream != null) {
            this.content = instream;
            this.length = length2;
            return;
        }
        throw new IllegalArgumentException("Source input stream may not be null");
    }

    @Override // org.apache.http.HttpEntity
    public boolean isRepeatable() {
        return false;
    }

    @Override // org.apache.http.HttpEntity
    public long getContentLength() {
        return this.length;
    }

    @Override // org.apache.http.HttpEntity
    public InputStream getContent() throws IOException {
        return this.content;
    }

    @Override // org.apache.http.HttpEntity
    public void writeTo(OutputStream outstream) throws IOException {
        int l;
        if (outstream != null) {
            InputStream instream = this.content;
            byte[] buffer = new byte[BUFFER_SIZE];
            if (this.length < 0) {
                while (true) {
                    int l2 = instream.read(buffer);
                    if (l2 == -1) {
                        break;
                    }
                    outstream.write(buffer, 0, l2);
                }
            } else {
                long remaining = this.length;
                while (remaining > 0 && (l = instream.read(buffer, 0, (int) Math.min(2048L, remaining))) != -1) {
                    outstream.write(buffer, 0, l);
                    remaining -= (long) l;
                }
            }
            this.consumed = true;
            return;
        }
        throw new IllegalArgumentException("Output stream may not be null");
    }

    @Override // org.apache.http.HttpEntity
    public boolean isStreaming() {
        return !this.consumed;
    }

    @Override // org.apache.http.entity.AbstractHttpEntity, org.apache.http.HttpEntity
    public void consumeContent() throws IOException {
        this.consumed = true;
        this.content.close();
    }
}
