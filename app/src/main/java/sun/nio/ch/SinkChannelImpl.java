package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.spi.SelectorProvider;

class SinkChannelImpl extends SinkChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ST_INUSE = 0;
    private static final int ST_KILLED = 1;
    private static final int ST_UNINITIALIZED = -1;
    private static NativeDispatcher nd;
    FileDescriptor fd;
    int fdVal;
    private final Object lock;
    private volatile int state;
    private final Object stateLock;
    private volatile long thread;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SinkChannelImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.SinkChannelImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SinkChannelImpl.<clinit>():void");
    }

    public boolean translateReadyOps(int r1, int r2, sun.nio.ch.SelectionKeyImpl r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SinkChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SinkChannelImpl.translateReadyOps(int, int, sun.nio.ch.SelectionKeyImpl):boolean");
    }

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }

    SinkChannelImpl(SelectorProvider sp, FileDescriptor fd) {
        super(sp);
        this.thread = 0;
        this.lock = new Object();
        this.stateLock = new Object();
        this.state = ST_UNINITIALIZED;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_INUSE;
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != ST_KILLED) {
                nd.preClose(this.fd);
            }
            long th = this.thread;
            if (th != 0) {
                NativeThread.signal(th);
            }
            if (!isRegistered()) {
                kill();
            }
        }
    }

    public void kill() throws IOException {
        Object obj = null;
        synchronized (this.stateLock) {
            if (this.state == ST_KILLED) {
            } else if (this.state == ST_UNINITIALIZED) {
                this.state = ST_KILLED;
            } else {
                if (!-assertionsDisabled) {
                    if (!(isOpen() || isRegistered())) {
                        obj = ST_KILLED;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                nd.close(this.fd);
                this.state = ST_KILLED;
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, ST_INUSE, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        if (ops == 4) {
            ops = 4;
        }
        sk.selector.putEventOps(sk, ops);
    }

    private void ensureOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    public int write(ByteBuffer src) throws IOException {
        boolean z = true;
        ensureOpen();
        synchronized (this.lock) {
            try {
                begin();
                if (isOpen()) {
                    int n;
                    this.thread = NativeThread.current();
                    while (true) {
                        n = IOUtil.write(this.fd, src, -1, nd);
                        if (n == -3) {
                            if (!isOpen()) {
                                break;
                            }
                        }
                        break;
                    }
                    int normalize = IOStatus.normalize(n);
                    this.thread = 0;
                    if (n <= 0 && n != -2) {
                        z = -assertionsDisabled;
                    }
                    end(z);
                    if (-assertionsDisabled || IOStatus.check(n)) {
                        return normalize;
                    }
                    throw new AssertionError();
                }
                this.thread = 0;
                end(-assertionsDisabled);
                if (-assertionsDisabled || IOStatus.check((int) ST_INUSE)) {
                    return ST_INUSE;
                }
                throw new AssertionError();
            } finally {
                this.thread = 0;
                if (ST_INUSE <= null && ST_INUSE != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check((int) ST_INUSE)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    public long write(ByteBuffer[] srcs) throws IOException {
        boolean z = true;
        if (srcs == null) {
            throw new NullPointerException();
        }
        ensureOpen();
        synchronized (this.lock) {
            try {
                begin();
                long isOpen = isOpen();
                if (isOpen == null) {
                    this.thread = isOpen;
                    end(-assertionsDisabled);
                    if (-assertionsDisabled || IOStatus.check(0)) {
                        return 0;
                    }
                    throw new AssertionError();
                }
                long n;
                this.thread = NativeThread.current();
                while (true) {
                    n = IOUtil.write(this.fd, srcs, nd);
                    if (n == -3) {
                        if (!isOpen()) {
                            break;
                        }
                    }
                    break;
                }
                long normalize = IOStatus.normalize(n);
                this.thread = 0;
                if (n <= 0 && n != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                if (-assertionsDisabled || IOStatus.check(n)) {
                    return normalize;
                }
                throw new AssertionError();
            } finally {
                this.thread = 0;
                if (0 <= 0 && 0 != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset >= 0 && length >= 0 && offset <= srcs.length - length) {
            return write(Util.subsequence(srcs, offset, length));
        }
        throw new IndexOutOfBoundsException();
    }
}
