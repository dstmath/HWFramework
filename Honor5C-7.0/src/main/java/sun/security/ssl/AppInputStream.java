package sun.security.ssl;

import java.io.IOException;
import java.io.InputStream;

class AppInputStream extends InputStream {
    private static final byte[] SKIP_ARRAY = null;
    private SSLSocketImpl c;
    private final byte[] oneByte;
    InputRecord r;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.AppInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.AppInputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.AppInputStream.<clinit>():void");
    }

    AppInputStream(SSLSocketImpl conn) {
        this.oneByte = new byte[1];
        this.r = new InputRecord();
        this.c = conn;
    }

    public int available() throws IOException {
        if (this.c.checkEOF() || !this.r.isAppDataValid()) {
            return 0;
        }
        return this.r.available();
    }

    public synchronized int read() throws IOException {
        if (read(this.oneByte, 0, 1) <= 0) {
            return -1;
        }
        return this.oneByte[0] & 255;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            if (this.c.checkEOF()) {
                return -1;
            }
            do {
                try {
                    if (this.r.available() == 0) {
                        this.c.readDataRecord(this.r);
                    } else {
                        return this.r.read(b, off, Math.min(len, this.r.available()));
                    }
                } catch (Exception e) {
                    this.c.handleException(e);
                    return -1;
                }
            } while (!this.c.checkEOF());
            return -1;
        }
    }

    public synchronized long skip(long n) throws IOException {
        long skipped;
        skipped = 0;
        while (n > 0) {
            int r = read(SKIP_ARRAY, 0, (int) Math.min(n, (long) SKIP_ARRAY.length));
            if (r <= 0) {
                break;
            }
            n -= (long) r;
            skipped += (long) r;
        }
        return skipped;
    }

    public void close() throws IOException {
        this.c.close();
    }
}
