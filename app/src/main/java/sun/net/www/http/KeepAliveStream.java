package sun.net.www.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.net.ProgressSource;
import sun.net.www.MeteredStream;

public class KeepAliveStream extends MeteredStream implements Hurryable {
    private static Thread cleanerThread;
    private static final KeepAliveStreamCleaner queue = null;
    HttpClient hc;
    boolean hurried;
    protected boolean queuedForCleanup;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.http.KeepAliveStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.http.KeepAliveStream.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.http.KeepAliveStream.<clinit>():void");
    }

    public KeepAliveStream(InputStream is, ProgressSource pi, long expected, HttpClient hc) {
        super(is, pi, expected);
        this.queuedForCleanup = false;
        this.hc = hc;
    }

    public void close() throws IOException {
        if (!this.closed && !this.queuedForCleanup) {
            try {
                if (this.expected > this.count) {
                    long nskip = this.expected - this.count;
                    if (nskip <= ((long) available())) {
                        long n = 0;
                        while (n < nskip) {
                            nskip -= n;
                            n = skip(nskip);
                        }
                    } else if (this.expected > ((long) KeepAliveStreamCleaner.MAX_DATA_REMAINING) || this.hurried) {
                        this.hc.closeServer();
                    } else {
                        queueForCleanup(new KeepAliveCleanerEntry(this, this.hc));
                    }
                }
                if (!(this.closed || this.hurried)) {
                    if (!this.queuedForCleanup) {
                        this.hc.finished();
                    }
                }
                if (this.pi != null) {
                    this.pi.finishTracking();
                }
                if (!this.queuedForCleanup) {
                    this.in = null;
                    this.hc = null;
                    this.closed = true;
                }
            } catch (Throwable th) {
                if (this.pi != null) {
                    this.pi.finishTracking();
                }
                if (!this.queuedForCleanup) {
                    this.in = null;
                    this.hc = null;
                    this.closed = true;
                }
            }
        }
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int limit) {
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public synchronized boolean hurry() {
        try {
            if (this.closed || this.count >= this.expected) {
                return false;
            }
            if (((long) this.in.available()) < this.expected - this.count) {
                return false;
            }
            byte[] buf = new byte[((int) (this.expected - this.count))];
            new DataInputStream(this.in).readFully(buf);
            this.in = new ByteArrayInputStream(buf);
            this.hurried = true;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void queueForCleanup(KeepAliveCleanerEntry kace) {
        synchronized (queue) {
            if (!kace.getQueuedForCleanup()) {
                if (queue.offer(kace)) {
                    kace.setQueuedForCleanup();
                    queue.notifyAll();
                } else {
                    kace.getHttpClient().closeServer();
                    return;
                }
            }
            boolean startCleanupThread = cleanerThread == null;
            if (!(startCleanupThread || cleanerThread.isAlive())) {
                startCleanupThread = true;
            }
            if (startCleanupThread) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        ThreadGroup grp = Thread.currentThread().getThreadGroup();
                        while (true) {
                            ThreadGroup parent = grp.getParent();
                            if (parent != null) {
                                grp = parent;
                            } else {
                                KeepAliveStream.cleanerThread = new Thread(grp, KeepAliveStream.queue, "Keep-Alive-SocketCleaner");
                                KeepAliveStream.cleanerThread.setDaemon(true);
                                KeepAliveStream.cleanerThread.setPriority(8);
                                KeepAliveStream.cleanerThread.setContextClassLoader(null);
                                KeepAliveStream.cleanerThread.start();
                                return null;
                            }
                        }
                    }
                });
            }
        }
    }

    protected long remainingToRead() {
        return this.expected - this.count;
    }

    protected void setClosed() {
        this.in = null;
        this.hc = null;
        this.closed = true;
    }
}
