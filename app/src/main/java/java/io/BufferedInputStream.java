package java.io;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import sun.util.logging.PlatformLogger;

public class BufferedInputStream extends FilterInputStream {
    private static final AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater = null;
    private static int defaultBufferSize;
    protected volatile byte[] buf;
    protected int count;
    protected int marklimit;
    protected int markpos;
    protected int pos;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.BufferedInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.BufferedInputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.io.BufferedInputStream.<clinit>():void");
    }

    private InputStream getInIfOpen() throws IOException {
        InputStream input = this.in;
        if (input != null) {
            return input;
        }
        throw new IOException("Stream closed");
    }

    private byte[] getBufIfOpen() throws IOException {
        byte[] buffer = this.buf;
        if (buffer != null) {
            return buffer;
        }
        throw new IOException("Stream closed");
    }

    public BufferedInputStream(InputStream in) {
        this(in, defaultBufferSize);
    }

    public BufferedInputStream(InputStream in, int size) {
        super(in);
        this.markpos = -1;
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.buf = new byte[size];
    }

    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (this.markpos < 0) {
            this.pos = 0;
        } else if (this.pos >= buffer.length) {
            if (this.markpos > 0) {
                int sz = this.pos - this.markpos;
                System.arraycopy(buffer, this.markpos, buffer, 0, sz);
                this.pos = sz;
                this.markpos = 0;
            } else if (buffer.length >= this.marklimit) {
                this.markpos = -1;
                this.pos = 0;
            } else {
                int nsz = this.pos * 2;
                if (nsz > this.marklimit) {
                    nsz = this.marklimit;
                }
                byte[] nbuf = new byte[nsz];
                System.arraycopy(buffer, 0, nbuf, 0, this.pos);
                if (bufUpdater.compareAndSet(this, buffer, nbuf)) {
                    buffer = nbuf;
                } else {
                    throw new IOException("Stream closed");
                }
            }
        }
        this.count = this.pos;
        int n = getInIfOpen().read(buffer, this.pos, buffer.length - this.pos);
        if (n > 0) {
            this.count = this.pos + n;
        }
    }

    public synchronized int read() throws IOException {
        if (this.pos >= this.count) {
            fill();
            if (this.pos >= this.count) {
                return -1;
            }
        }
        byte[] bufIfOpen = getBufIfOpen();
        int i = this.pos;
        this.pos = i + 1;
        return bufIfOpen[i] & 255;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = this.count - this.pos;
        if (avail <= 0) {
            if (len >= getBufIfOpen().length && this.markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            fill();
            avail = this.count - this.pos;
            if (avail <= 0) {
                return -1;
            }
        }
        int cnt = avail < len ? avail : len;
        System.arraycopy(getBufIfOpen(), this.pos, b, off, cnt);
        this.pos += cnt;
        return cnt;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        getBufIfOpen();
        if ((((off | len) | (off + len)) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int n = 0;
            while (true) {
                int nread = read1(b, off + n, len - n);
                if (nread <= 0) {
                    break;
                }
                n += nread;
                if (n >= len) {
                    return n;
                }
                InputStream input = this.in;
                if (input != null && input.available() <= 0) {
                    return n;
                }
            }
        }
    }

    public synchronized long skip(long n) throws IOException {
        getBufIfOpen();
        if (n <= 0) {
            return 0;
        }
        long avail = (long) (this.count - this.pos);
        if (avail <= 0) {
            if (this.markpos < 0) {
                return getInIfOpen().skip(n);
            }
            fill();
            avail = (long) (this.count - this.pos);
            if (avail <= 0) {
                return 0;
            }
        }
        long skipped = avail < n ? avail : n;
        this.pos = (int) (((long) this.pos) + skipped);
        return skipped;
    }

    public synchronized int available() throws IOException {
        int i = PlatformLogger.OFF;
        synchronized (this) {
            int n = this.count - this.pos;
            int avail = getInIfOpen().available();
            if (n <= PlatformLogger.OFF - avail) {
                i = n + avail;
            }
        }
        return i;
    }

    public synchronized void mark(int readlimit) {
        this.marklimit = readlimit;
        this.markpos = this.pos;
    }

    public synchronized void reset() throws IOException {
        getBufIfOpen();
        if (this.markpos < 0) {
            throw new IOException("Resetting to invalid mark");
        }
        this.pos = this.markpos;
    }

    public boolean markSupported() {
        return true;
    }

    public void close() throws IOException {
        byte[] buffer;
        do {
            buffer = this.buf;
            if (buffer == null) {
                return;
            }
        } while (!bufUpdater.compareAndSet(this, buffer, null));
        InputStream input = this.in;
        this.in = null;
        if (input != null) {
            input.close();
        }
    }
}
