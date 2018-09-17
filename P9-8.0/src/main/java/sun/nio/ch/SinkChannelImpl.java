package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.spi.SelectorProvider;

class SinkChannelImpl extends SinkChannel implements SelChImpl {
    static final /* synthetic */ boolean -assertionsDisabled = (SinkChannelImpl.class.desiredAssertionStatus() ^ 1);
    private static final int ST_INUSE = 0;
    private static final int ST_KILLED = 1;
    private static final int ST_UNINITIALIZED = -1;
    private static final NativeDispatcher nd = new FileDispatcherImpl();
    FileDescriptor fd;
    int fdVal;
    private final Object lock = new Object();
    private volatile int state = -1;
    private final Object stateLock = new Object();
    private volatile long thread = 0;

    public FileDescriptor getFD() {
        return this.fd;
    }

    public int getFDVal() {
        return this.fdVal;
    }

    SinkChannelImpl(SelectorProvider sp, FileDescriptor fd) {
        super(sp);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = 0;
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (this.stateLock) {
            if (this.state != 1) {
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
        synchronized (this.stateLock) {
            if (this.state == 1) {
            } else if (this.state == -1) {
                this.state = 1;
            } else if (-assertionsDisabled || !(isOpen() || isRegistered())) {
                nd.close(this.fd);
                this.state = 1;
            } else {
                throw new AssertionError();
            }
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        boolean z = true;
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((Net.POLLNVAL & ops) != 0) {
            throw new Error("POLLNVAL detected");
        } else if (((Net.POLLERR | Net.POLLHUP) & ops) != 0) {
            newOps = intOps;
            sk.nioReadyOps(intOps);
            if (((~oldOps) & intOps) == 0) {
                z = -assertionsDisabled;
            }
            return z;
        } else {
            if (!((Net.POLLOUT & ops) == 0 || (intOps & 4) == 0)) {
                newOps = initialOps | 4;
            }
            sk.nioReadyOps(newOps);
            if (((~oldOps) & newOps) == 0) {
                z = -assertionsDisabled;
            }
            return z;
        }
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        if (ops == 4) {
            ops = Net.POLLOUT;
        }
        sk.selector.putEventOps(sk, ops);
    }

    private void ensureOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /* JADX WARNING: Missing block: B:17:0x002f, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:38:0x006b, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    } else {
                        throw new AssertionError();
                    }
                }
                this.thread = 0;
                end(-assertionsDisabled);
                if (-assertionsDisabled || IOStatus.check(0)) {
                } else {
                    throw new AssertionError();
                }
            } finally {
                this.thread = 0;
                if (null <= null && 0 != -2) {
                    z = -assertionsDisabled;
                }
                end(z);
                if (!-assertionsDisabled && !IOStatus.check(0)) {
                    AssertionError assertionError = new AssertionError();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x003b, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:43:0x007c, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    } else {
                        throw new AssertionError();
                    }
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
                } else {
                    throw new AssertionError();
                }
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
