package org.apache.http.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public class FileEntity extends AbstractHttpEntity implements Cloneable {
    protected final File file;

    public FileEntity(File file2, String contentType) {
        if (file2 != null) {
            this.file = file2;
            setContentType(contentType);
            return;
        }
        throw new IllegalArgumentException("File may not be null");
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return this.file.length();
    }

    public InputStream getContent() throws IOException {
        return new FileInputStream(this.file);
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            InputStream instream = new FileInputStream(this.file);
            try {
                byte[] tmp = new byte[4096];
                while (true) {
                    int read = instream.read(tmp);
                    int l = read;
                    if (read != -1) {
                        outstream.write(tmp, 0, l);
                    } else {
                        outstream.flush();
                        return;
                    }
                }
            } finally {
                instream.close();
            }
        } else {
            throw new IllegalArgumentException("Output stream may not be null");
        }
    }

    public boolean isStreaming() {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
