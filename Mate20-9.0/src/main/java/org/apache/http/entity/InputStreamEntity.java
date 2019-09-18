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

    public boolean isRepeatable() {
        return false;
    }

    public long getContentLength() {
        return this.length;
    }

    public InputStream getContent() throws IOException {
        return this.content;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            InputStream instream = this.content;
            byte[] buffer = new byte[BUFFER_SIZE];
            if (this.length < 0) {
                while (true) {
                    int read = instream.read(buffer);
                    int l = read;
                    if (read == -1) {
                        break;
                    }
                    outstream.write(buffer, 0, l);
                }
            } else {
                long remaining = this.length;
                while (remaining > 0) {
                    int l2 = instream.read(buffer, 0, (int) Math.min(2048, remaining));
                    if (l2 == -1) {
                        break;
                    }
                    outstream.write(buffer, 0, l2);
                    remaining -= (long) l2;
                }
            }
            this.consumed = true;
            return;
        }
        throw new IllegalArgumentException("Output stream may not be null");
    }

    public boolean isStreaming() {
        return !this.consumed;
    }

    public void consumeContent() throws IOException {
        this.consumed = true;
        this.content.close();
    }
}
