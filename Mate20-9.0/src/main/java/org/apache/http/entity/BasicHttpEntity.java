package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public class BasicHttpEntity extends AbstractHttpEntity {
    private InputStream content;
    private boolean contentObtained;
    private long length = -1;

    public long getContentLength() {
        return this.length;
    }

    public InputStream getContent() throws IllegalStateException {
        if (this.content == null) {
            throw new IllegalStateException("Content has not been provided");
        } else if (!this.contentObtained) {
            this.contentObtained = true;
            return this.content;
        } else {
            throw new IllegalStateException("Content has been consumed");
        }
    }

    public boolean isRepeatable() {
        return false;
    }

    public void setContentLength(long len) {
        this.length = len;
    }

    public void setContent(InputStream instream) {
        this.content = instream;
        this.contentObtained = false;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            InputStream instream = getContent();
            byte[] tmp = new byte[2048];
            while (true) {
                int read = instream.read(tmp);
                int l = read;
                if (read != -1) {
                    outstream.write(tmp, 0, l);
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("Output stream may not be null");
        }
    }

    public boolean isStreaming() {
        return !this.contentObtained && this.content != null;
    }

    public void consumeContent() throws IOException {
        if (this.content != null) {
            this.content.close();
        }
    }
}
