package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.spi.SelectorProvider;

class SinkChannelImpl extends Pipe.SinkChannel implements SelChImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    SinkChannelImpl(SelectorProvider sp, FileDescriptor fd2) {
        super(sp);
        this.fd = fd2;
        this.fdVal = IOUtil.fdVal(fd2);
        this.state = 0;
    }

    /* access modifiers changed from: protected */
    public void implCloseSelectableChannel() throws IOException {
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
            if (this.state != 1) {
                if (this.state == -1) {
                    this.state = 1;
                    return;
                }
                nd.close(this.fd);
                this.state = 1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(this.fd, block);
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((Net.POLLNVAL & ops) == 0) {
            short s = (Net.POLLERR | Net.POLLHUP) & ops;
            boolean z = $assertionsDisabled;
            if (s != 0) {
                int newOps2 = intOps;
                sk.nioReadyOps(newOps2);
                if (((~oldOps) & newOps2) != 0) {
                    z = true;
                }
                return z;
            }
            if (!((Net.POLLOUT & ops) == 0 || (intOps & 4) == 0)) {
                newOps |= 4;
            }
            sk.nioReadyOps(newOps);
            if (((~oldOps) & newOps) != 0) {
                z = true;
            }
            return z;
        }
        throw new Error("POLLNVAL detected");
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

    public int write(ByteBuffer src) throws IOException {
        int n;
        ensureOpen();
        synchronized (this.lock) {
            boolean z = $assertionsDisabled;
            boolean z2 = true;
            try {
                begin();
                if (!isOpen()) {
                    this.thread = 0;
                    if (0 <= 0) {
                        if (0 != -2) {
                            z2 = false;
                        }
                    }
                    end(z2);
                    return 0;
                }
                this.thread = NativeThread.current();
                do {
                    n = IOUtil.write(this.fd, src, -1, nd);
                    if (n != -3) {
                        break;
                    }
                } while (isOpen());
                int normalize = IOStatus.normalize(n);
                this.thread = 0;
                if (n <= 0) {
                    if (n != -2) {
                        end(z);
                        return normalize;
                    }
                }
                z = true;
                end(z);
                return normalize;
            } catch (Throwable th) {
                this.thread = 0;
                if (0 <= 0) {
                    if (0 != -2) {
                        end(z);
                        throw th;
                    }
                }
                z = true;
                end(z);
                throw th;
            }
        }
    }

    public long write(ByteBuffer[] srcs) throws IOException {
        long n;
        if (srcs != null) {
            ensureOpen();
            synchronized (this.lock) {
                boolean z = true;
                try {
                    begin();
                    if (!isOpen()) {
                        this.thread = 0;
                        if (0 <= 0) {
                            if (0 != -2) {
                                z = false;
                            }
                        }
                        end(z);
                        return 0;
                    }
                    this.thread = NativeThread.current();
                    do {
                        n = IOUtil.write(this.fd, srcs, nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(n);
                    this.thread = 0;
                    if (n <= 0) {
                        if (n != -2) {
                            z = false;
                        }
                    }
                    end(z);
                    return normalize;
                } catch (Throwable th) {
                    this.thread = 0;
                    if (0 <= 0) {
                        if (0 != -2) {
                            z = false;
                        }
                    }
                    end(z);
                    throw th;
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset >= 0 && length >= 0 && offset <= srcs.length - length) {
            return write(Util.subsequence(srcs, offset, length));
        }
        throw new IndexOutOfBoundsException();
    }
}
