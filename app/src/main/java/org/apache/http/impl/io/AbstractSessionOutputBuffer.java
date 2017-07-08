package org.apache.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public abstract class AbstractSessionOutputBuffer implements SessionOutputBuffer {
    private static final byte[] CRLF = null;
    private static final int MAX_CHUNK = 256;
    private boolean ascii;
    private ByteArrayBuffer buffer;
    private String charset;
    private HttpTransportMetricsImpl metrics;
    private OutputStream outstream;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.impl.io.AbstractSessionOutputBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.impl.io.AbstractSessionOutputBuffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.io.AbstractSessionOutputBuffer.<clinit>():void");
    }

    public AbstractSessionOutputBuffer() {
        this.charset = HTTP.US_ASCII;
        this.ascii = true;
    }

    protected void init(OutputStream outstream, int buffersize, HttpParams params) {
        if (outstream == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        } else if (buffersize <= 0) {
            throw new IllegalArgumentException("Buffer size may not be negative or zero");
        } else if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        } else {
            boolean z;
            this.outstream = outstream;
            this.buffer = new ByteArrayBuffer(buffersize);
            this.charset = HttpProtocolParams.getHttpElementCharset(params);
            if (this.charset.equalsIgnoreCase(HTTP.US_ASCII)) {
                z = true;
            } else {
                z = this.charset.equalsIgnoreCase(HTTP.ASCII);
            }
            this.ascii = z;
            this.metrics = new HttpTransportMetricsImpl();
        }
    }

    protected void flushBuffer() throws IOException {
        int len = this.buffer.length();
        if (len > 0) {
            this.outstream.write(this.buffer.buffer(), 0, len);
            this.buffer.clear();
            this.metrics.incrementBytesTransferred((long) len);
        }
    }

    public void flush() throws IOException {
        flushBuffer();
        this.outstream.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (b != null) {
            if (len > MAX_CHUNK || len > this.buffer.capacity()) {
                flushBuffer();
                this.outstream.write(b, off, len);
                this.metrics.incrementBytesTransferred((long) len);
            } else {
                if (len > this.buffer.capacity() - this.buffer.length()) {
                    flushBuffer();
                }
                this.buffer.append(b, off, len);
            }
        }
    }

    public void write(byte[] b) throws IOException {
        if (b != null) {
            write(b, 0, b.length);
        }
    }

    public void write(int b) throws IOException {
        if (this.buffer.isFull()) {
            flushBuffer();
        }
        this.buffer.append(b);
    }

    public void writeLine(String s) throws IOException {
        if (s != null) {
            if (s.length() > 0) {
                write(s.getBytes(this.charset));
            }
            write(CRLF);
        }
    }

    public void writeLine(CharArrayBuffer s) throws IOException {
        if (s != null) {
            if (this.ascii) {
                int off = 0;
                int remaining = s.length();
                while (remaining > 0) {
                    int chunk = Math.min(this.buffer.capacity() - this.buffer.length(), remaining);
                    if (chunk > 0) {
                        this.buffer.append(s, off, chunk);
                    }
                    if (this.buffer.isFull()) {
                        flushBuffer();
                    }
                    off += chunk;
                    remaining -= chunk;
                }
            } else {
                write(s.toString().getBytes(this.charset));
            }
            write(CRLF);
        }
    }

    public HttpTransportMetrics getMetrics() {
        return this.metrics;
    }
}
