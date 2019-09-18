package org.apache.http.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public class ByteArrayEntity extends AbstractHttpEntity implements Cloneable {
    protected final byte[] content;

    public ByteArrayEntity(byte[] b) {
        if (b != null) {
            this.content = b;
            return;
        }
        throw new IllegalArgumentException("Source byte array may not be null");
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return (long) this.content.length;
    }

    public InputStream getContent() {
        return new ByteArrayInputStream(this.content);
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            outstream.write(this.content);
            outstream.flush();
            return;
        }
        throw new IllegalArgumentException("Output stream may not be null");
    }

    public boolean isStreaming() {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
