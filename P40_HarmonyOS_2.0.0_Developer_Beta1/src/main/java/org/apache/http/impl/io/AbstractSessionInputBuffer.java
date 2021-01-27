package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public abstract class AbstractSessionInputBuffer implements SessionInputBuffer {
    private boolean ascii = true;
    private byte[] buffer;
    private int bufferlen;
    private int bufferpos;
    private String charset = "US-ASCII";
    private InputStream instream;
    private ByteArrayBuffer linebuffer = null;
    private int maxLineLen = -1;
    private HttpTransportMetricsImpl metrics;

    /* access modifiers changed from: protected */
    public void init(InputStream instream2, int buffersize, HttpParams params) {
        if (instream2 == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        } else if (buffersize <= 0) {
            throw new IllegalArgumentException("Buffer size may not be negative or zero");
        } else if (params != null) {
            this.instream = instream2;
            this.buffer = new byte[buffersize];
            boolean z = false;
            this.bufferpos = 0;
            this.bufferlen = 0;
            this.linebuffer = new ByteArrayBuffer(buffersize);
            this.charset = HttpProtocolParams.getHttpElementCharset(params);
            if (this.charset.equalsIgnoreCase("US-ASCII") || this.charset.equalsIgnoreCase(HTTP.ASCII)) {
                z = true;
            }
            this.ascii = z;
            this.maxLineLen = params.getIntParameter("http.connection.max-line-length", -1);
            this.metrics = new HttpTransportMetricsImpl();
        } else {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public int fillBuffer() throws IOException {
        int i = this.bufferpos;
        if (i > 0) {
            int len = this.bufferlen - i;
            if (len > 0) {
                byte[] bArr = this.buffer;
                System.arraycopy(bArr, i, bArr, 0, len);
            }
            this.bufferpos = 0;
            this.bufferlen = len;
        }
        int off = this.bufferlen;
        byte[] bArr2 = this.buffer;
        int l = this.instream.read(bArr2, off, bArr2.length - off);
        if (l == -1) {
            return -1;
        }
        this.bufferlen = off + l;
        this.metrics.incrementBytesTransferred((long) l);
        return l;
    }

    /* access modifiers changed from: protected */
    public boolean hasBufferedData() {
        return this.bufferpos < this.bufferlen;
    }

    @Override // org.apache.http.io.SessionInputBuffer
    public int read() throws IOException {
        while (!hasBufferedData()) {
            if (fillBuffer() == -1) {
                return -1;
            }
        }
        byte[] bArr = this.buffer;
        int i = this.bufferpos;
        this.bufferpos = i + 1;
        return bArr[i] & 255;
    }

    @Override // org.apache.http.io.SessionInputBuffer
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            return 0;
        }
        while (!hasBufferedData()) {
            if (fillBuffer() == -1) {
                return -1;
            }
        }
        int chunk = this.bufferlen - this.bufferpos;
        if (chunk > len) {
            chunk = len;
        }
        System.arraycopy(this.buffer, this.bufferpos, b, off, chunk);
        this.bufferpos += chunk;
        return chunk;
    }

    @Override // org.apache.http.io.SessionInputBuffer
    public int read(byte[] b) throws IOException {
        if (b == null) {
            return 0;
        }
        return read(b, 0, b.length);
    }

    private int locateLF() {
        for (int i = this.bufferpos; i < this.bufferlen; i++) {
            if (this.buffer[i] == 10) {
                return i;
            }
        }
        return -1;
    }

    @Override // org.apache.http.io.SessionInputBuffer
    public int readLine(CharArrayBuffer charbuffer) throws IOException {
        if (charbuffer != null) {
            this.linebuffer.clear();
            int noRead = 0;
            boolean retry = true;
            while (retry) {
                int i = locateLF();
                if (i == -1) {
                    if (hasBufferedData()) {
                        int i2 = this.bufferlen;
                        int i3 = this.bufferpos;
                        this.linebuffer.append(this.buffer, i3, i2 - i3);
                        this.bufferpos = this.bufferlen;
                    }
                    noRead = fillBuffer();
                    if (noRead == -1) {
                        retry = false;
                    }
                } else if (this.linebuffer.isEmpty()) {
                    return lineFromReadBuffer(charbuffer, i);
                } else {
                    retry = false;
                    int i4 = this.bufferpos;
                    this.linebuffer.append(this.buffer, i4, (i + 1) - i4);
                    this.bufferpos = i + 1;
                }
                if (this.maxLineLen > 0 && this.linebuffer.length() >= this.maxLineLen) {
                    throw new IOException("Maximum line length limit exceeded");
                }
            }
            if (noRead != -1 || !this.linebuffer.isEmpty()) {
                return lineFromLineBuffer(charbuffer);
            }
            return -1;
        }
        throw new IllegalArgumentException("Char array buffer may not be null");
    }

    private int lineFromLineBuffer(CharArrayBuffer charbuffer) throws IOException {
        int l = this.linebuffer.length();
        if (l > 0) {
            if (this.linebuffer.byteAt(l - 1) == 10) {
                l--;
                this.linebuffer.setLength(l);
            }
            if (l > 0 && this.linebuffer.byteAt(l - 1) == 13) {
                this.linebuffer.setLength(l - 1);
            }
        }
        int l2 = this.linebuffer.length();
        if (this.ascii) {
            charbuffer.append(this.linebuffer, 0, l2);
        } else {
            charbuffer.append(new String(this.linebuffer.buffer(), 0, l2, this.charset));
        }
        return l2;
    }

    private int lineFromReadBuffer(CharArrayBuffer charbuffer, int pos) throws IOException {
        int off = this.bufferpos;
        this.bufferpos = pos + 1;
        if (pos > off && this.buffer[pos - 1] == 13) {
            pos--;
        }
        int len = pos - off;
        if (this.ascii) {
            charbuffer.append(this.buffer, off, len);
        } else {
            charbuffer.append(new String(this.buffer, off, len, this.charset));
        }
        return len;
    }

    @Override // org.apache.http.io.SessionInputBuffer
    public String readLine() throws IOException {
        CharArrayBuffer charbuffer = new CharArrayBuffer(64);
        if (readLine(charbuffer) != -1) {
            return charbuffer.toString();
        }
        return null;
    }

    @Override // org.apache.http.io.SessionInputBuffer
    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}
