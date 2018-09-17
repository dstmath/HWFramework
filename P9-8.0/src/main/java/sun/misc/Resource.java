package sun.misc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.jar.Manifest;
import sun.nio.ByteBuffered;

public abstract class Resource {
    private InputStream cis;

    public abstract URL getCodeSourceURL();

    public abstract int getContentLength() throws IOException;

    public abstract InputStream getInputStream() throws IOException;

    public abstract String getName();

    public abstract URL getURL();

    private synchronized InputStream cachedInputStream() throws IOException {
        if (this.cis == null) {
            this.cis = getInputStream();
        }
        return this.cis;
    }

    public byte[] getBytes() throws IOException {
        int len;
        byte[] b;
        InputStream in = cachedInputStream();
        boolean isInterrupted = Thread.interrupted();
        while (true) {
            try {
                len = getContentLength();
                break;
            } catch (InterruptedIOException e) {
                Thread.interrupted();
                isInterrupted = true;
            }
        }
        try {
            b = new byte[0];
            if (len == -1) {
                len = Integer.MAX_VALUE;
            }
            int pos = 0;
            while (pos < len) {
                int bytesToRead;
                if (pos >= b.length) {
                    bytesToRead = Math.min(len - pos, b.length + 1024);
                    if (b.length < pos + bytesToRead) {
                        b = Arrays.copyOf(b, pos + bytesToRead);
                    }
                } else {
                    bytesToRead = b.length - pos;
                }
                int cc = 0;
                cc = in.read(b, pos, bytesToRead);
                if (cc >= 0) {
                    pos += cc;
                } else if (len != Integer.MAX_VALUE) {
                    throw new EOFException("Detect premature EOF");
                } else if (b.length != pos) {
                    b = Arrays.copyOf(b, pos);
                }
            }
        } catch (InterruptedIOException e2) {
            Thread.interrupted();
            isInterrupted = true;
        } catch (Throwable th) {
            try {
                in.close();
            } catch (InterruptedIOException e3) {
                isInterrupted = true;
            } catch (IOException e4) {
            }
            if (isInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            in.close();
        } catch (InterruptedIOException e5) {
            isInterrupted = true;
        } catch (IOException e6) {
        }
        if (isInterrupted) {
            Thread.currentThread().interrupt();
        }
        return b;
    }

    public ByteBuffer getByteBuffer() throws IOException {
        InputStream in = cachedInputStream();
        if (in instanceof ByteBuffered) {
            return ((ByteBuffered) in).getByteBuffer();
        }
        return null;
    }

    public Manifest getManifest() throws IOException {
        return null;
    }

    public Certificate[] getCertificates() {
        return null;
    }

    public CodeSigner[] getCodeSigners() {
        return null;
    }
}
