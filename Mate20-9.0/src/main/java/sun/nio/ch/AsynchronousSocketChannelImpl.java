package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jdk.net.ExtendedSocketOptions;
import sun.net.ExtendedOptionsImpl;
import sun.net.NetHooks;

abstract class AsynchronousSocketChannelImpl extends AsynchronousSocketChannel implements Cancellable, Groupable {
    static final int ST_CONNECTED = 2;
    static final int ST_PENDING = 1;
    static final int ST_UNCONNECTED = 0;
    static final int ST_UNINITIALIZED = -1;
    private final ReadWriteLock closeLock;
    protected final FileDescriptor fd;
    private boolean isReuseAddress;
    protected volatile InetSocketAddress localAddress;
    private volatile boolean open;
    private boolean readKilled;
    private final Object readLock;
    private boolean readShutdown;
    private boolean reading;
    protected volatile InetSocketAddress remoteAddress;
    protected volatile int state;
    protected final Object stateLock;
    private boolean writeKilled;
    private final Object writeLock;
    private boolean writeShutdown;
    private boolean writing;

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet<>(5);
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_KEEPALIVE);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.TCP_NODELAY);
            if (ExtendedOptionsImpl.flowSupported()) {
                set.add(ExtendedSocketOptions.SO_FLOW_SLA);
            }
            return Collections.unmodifiableSet(set);
        }
    }

    /* access modifiers changed from: package-private */
    public abstract void implClose() throws IOException;

    /* access modifiers changed from: package-private */
    public abstract <A> Future<Void> implConnect(SocketAddress socketAddress, A a, CompletionHandler<Void, ? super A> completionHandler);

    /* access modifiers changed from: package-private */
    public abstract <V extends Number, A> Future<V> implRead(boolean z, ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr, long j, TimeUnit timeUnit, A a, CompletionHandler<V, ? super A> completionHandler);

    /* access modifiers changed from: package-private */
    public abstract <V extends Number, A> Future<V> implWrite(boolean z, ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr, long j, TimeUnit timeUnit, A a, CompletionHandler<V, ? super A> completionHandler);

    AsynchronousSocketChannelImpl(AsynchronousChannelGroupImpl group) throws IOException {
        super(group.provider());
        this.stateLock = new Object();
        this.localAddress = null;
        this.remoteAddress = null;
        this.state = -1;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.closeLock = new ReentrantReadWriteLock();
        this.open = true;
        this.fd = Net.socket(true);
        this.state = 0;
    }

    AsynchronousSocketChannelImpl(AsynchronousChannelGroupImpl group, FileDescriptor fd2, InetSocketAddress remote) throws IOException {
        super(group.provider());
        this.stateLock = new Object();
        this.localAddress = null;
        this.remoteAddress = null;
        this.state = -1;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.closeLock = new ReentrantReadWriteLock();
        this.open = true;
        this.fd = fd2;
        this.state = 2;
        this.localAddress = Net.localAddress(fd2);
        this.remoteAddress = remote;
    }

    public final boolean isOpen() {
        return this.open;
    }

    /* access modifiers changed from: package-private */
    public final void begin() throws IOException {
        this.closeLock.readLock().lock();
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /* access modifiers changed from: package-private */
    public final void end() {
        this.closeLock.readLock().unlock();
    }

    public final void close() throws IOException {
        this.closeLock.writeLock().lock();
        try {
            if (this.open) {
                this.open = false;
                this.closeLock.writeLock().unlock();
                implClose();
            }
        } finally {
            this.closeLock.writeLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public final void enableReading(boolean killed) {
        synchronized (this.readLock) {
            this.reading = false;
            if (killed) {
                this.readKilled = true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void enableReading() {
        enableReading(false);
    }

    /* access modifiers changed from: package-private */
    public final void enableWriting(boolean killed) {
        synchronized (this.writeLock) {
            this.writing = false;
            if (killed) {
                this.writeKilled = true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void enableWriting() {
        enableWriting(false);
    }

    /* access modifiers changed from: package-private */
    public final void killReading() {
        synchronized (this.readLock) {
            this.readKilled = true;
        }
    }

    /* access modifiers changed from: package-private */
    public final void killWriting() {
        synchronized (this.writeLock) {
            this.writeKilled = true;
        }
    }

    /* access modifiers changed from: package-private */
    public final void killConnect() {
        killReading();
        killWriting();
    }

    public final Future<Void> connect(SocketAddress remote) {
        return implConnect(remote, null, null);
    }

    public final <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        if (handler != null) {
            implConnect(remote, attachment, handler);
            return;
        }
        throw new NullPointerException("'handler' is null");
    }

    private <V extends Number, A> Future<V> read(boolean isScatteringRead, ByteBuffer dst, ByteBuffer[] dsts, long timeout, TimeUnit unit, A att, CompletionHandler<V, ? super A> handler) {
        Number result;
        if (!isOpen()) {
            Throwable e = new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withFailure(e);
            }
            Invoker.invoke(this, handler, att, null, e);
            return null;
        } else if (this.remoteAddress != null) {
            int i = 0;
            boolean hasSpaceToRead = isScatteringRead || dst.hasRemaining();
            boolean shutdown = false;
            synchronized (this.readLock) {
                if (this.readKilled) {
                    throw new IllegalStateException("Reading not allowed due to timeout or cancellation");
                } else if (this.reading) {
                    throw new ReadPendingException();
                } else if (this.readShutdown) {
                    shutdown = true;
                } else if (hasSpaceToRead) {
                    this.reading = true;
                }
            }
            if (!shutdown && hasSpaceToRead) {
                return implRead(isScatteringRead, dst, dsts, timeout, unit, att, handler);
            }
            if (isScatteringRead) {
                result = Long.valueOf(shutdown ? -1 : 0);
            } else {
                if (shutdown) {
                    i = -1;
                }
                result = Integer.valueOf(i);
            }
            if (handler == null) {
                return CompletedFuture.withResult(result);
            }
            Invoker.invoke(this, handler, att, result, null);
            return null;
        } else {
            throw new NotYetConnectedException();
        }
    }

    public final Future<Integer> read(ByteBuffer dst) {
        if (!dst.isReadOnly()) {
            return read(false, dst, (ByteBuffer[]) null, 0, TimeUnit.MILLISECONDS, (Object) null, (CompletionHandler) null);
        }
        throw new IllegalArgumentException("Read-only buffer");
    }

    public final <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        } else if (!dst.isReadOnly()) {
            read(false, dst, (ByteBuffer[]) null, timeout, unit, attachment, handler);
        } else {
            throw new IllegalArgumentException("Read-only buffer");
        }
    }

    public final <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        int i = offset;
        if (handler != null) {
            if (i < 0 || length < 0) {
                ByteBuffer[] byteBufferArr = dsts;
            } else {
                ByteBuffer[] byteBufferArr2 = dsts;
                if (i <= byteBufferArr2.length - length) {
                    ByteBuffer[] bufs = Util.subsequence(byteBufferArr2, i, length);
                    int i2 = 0;
                    while (i2 < bufs.length) {
                        if (!bufs[i2].isReadOnly()) {
                            i2++;
                        } else {
                            throw new IllegalArgumentException("Read-only buffer");
                        }
                    }
                    read(true, (ByteBuffer) null, bufs, timeout, unit, attachment, handler);
                    return;
                }
            }
            throw new IndexOutOfBoundsException();
        }
        ByteBuffer[] byteBufferArr3 = dsts;
        throw new NullPointerException("'handler' is null");
    }

    private <V extends Number, A> Future<V> write(boolean isGatheringWrite, ByteBuffer src, ByteBuffer[] srcs, long timeout, TimeUnit unit, A att, CompletionHandler<V, ? super A> handler) {
        boolean hasDataToWrite = isGatheringWrite || src.hasRemaining();
        boolean closed = false;
        if (!isOpen()) {
            closed = true;
        } else if (this.remoteAddress != null) {
            synchronized (this.writeLock) {
                if (this.writeKilled) {
                    throw new IllegalStateException("Writing not allowed due to timeout or cancellation");
                } else if (this.writing) {
                    throw new WritePendingException();
                } else if (this.writeShutdown) {
                    closed = true;
                } else if (hasDataToWrite) {
                    this.writing = true;
                }
            }
        } else {
            throw new NotYetConnectedException();
        }
        if (closed) {
            Throwable e = new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withFailure(e);
            }
            Invoker.invoke(this, handler, att, null, e);
            return null;
        } else if (hasDataToWrite) {
            return implWrite(isGatheringWrite, src, srcs, timeout, unit, att, handler);
        } else {
            int result = isGatheringWrite ? 0L : 0;
            if (handler == null) {
                return CompletedFuture.withResult(result);
            }
            Invoker.invoke(this, handler, att, result, null);
            return null;
        }
    }

    public final Future<Integer> write(ByteBuffer src) {
        return write(false, src, (ByteBuffer[]) null, 0, TimeUnit.MILLISECONDS, (Object) null, (CompletionHandler) null);
    }

    public final <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler != null) {
            write(false, src, (ByteBuffer[]) null, timeout, unit, attachment, handler);
            return;
        }
        throw new NullPointerException("'handler' is null");
    }

    public final <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        int i = offset;
        if (handler != null) {
            if (i < 0 || length < 0) {
                ByteBuffer[] byteBufferArr = srcs;
            } else {
                ByteBuffer[] byteBufferArr2 = srcs;
                if (i <= byteBufferArr2.length - length) {
                    write(true, (ByteBuffer) null, Util.subsequence(byteBufferArr2, i, length), timeout, unit, attachment, handler);
                    return;
                }
            }
            throw new IndexOutOfBoundsException();
        }
        ByteBuffer[] byteBufferArr3 = srcs;
        throw new NullPointerException("'handler' is null");
    }

    public final AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        try {
            begin();
            synchronized (this.stateLock) {
                if (this.state == 1) {
                    throw new ConnectionPendingException();
                } else if (this.localAddress == null) {
                    InetSocketAddress isa = local == null ? new InetSocketAddress(0) : Net.checkAddress(local);
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkListen(isa.getPort());
                    }
                    NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                    Net.bind(this.fd, isa.getAddress(), isa.getPort());
                    this.localAddress = Net.localAddress(this.fd);
                } else {
                    throw new AlreadyBoundException();
                }
            }
            end();
            return this;
        } catch (Throwable th) {
            end();
            throw th;
        }
    }

    public final SocketAddress getLocalAddress() throws IOException {
        if (isOpen()) {
            return Net.getRevealedLocalAddress(this.localAddress);
        }
        throw new ClosedChannelException();
    }

    public final <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            try {
                begin();
                if (!this.writeShutdown) {
                    if (name != StandardSocketOptions.SO_REUSEADDR || !Net.useExclusiveBind()) {
                        Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                    } else {
                        this.isReuseAddress = ((Boolean) value).booleanValue();
                    }
                    return this;
                }
                throw new IOException("Connection has been shutdown for writing");
            } finally {
                end();
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public final <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            try {
                begin();
                if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    return Boolean.valueOf(this.isReuseAddress);
                }
                T socketOption = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                end();
                return socketOption;
            } finally {
                end();
            }
        } else {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
    }

    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    public final SocketAddress getRemoteAddress() throws IOException {
        if (isOpen()) {
            return this.remoteAddress;
        }
        throw new ClosedChannelException();
    }

    public final AsynchronousSocketChannel shutdownInput() throws IOException {
        try {
            begin();
            if (this.remoteAddress != null) {
                synchronized (this.readLock) {
                    if (!this.readShutdown) {
                        Net.shutdown(this.fd, 0);
                        this.readShutdown = true;
                    }
                }
                end();
                return this;
            }
            throw new NotYetConnectedException();
        } catch (Throwable th) {
            end();
            throw th;
        }
    }

    public final AsynchronousSocketChannel shutdownOutput() throws IOException {
        try {
            begin();
            if (this.remoteAddress != null) {
                synchronized (this.writeLock) {
                    if (!this.writeShutdown) {
                        Net.shutdown(this.fd, 1);
                        this.writeShutdown = true;
                    }
                }
                end();
                return this;
            }
            throw new NotYetConnectedException();
        } catch (Throwable th) {
            end();
            throw th;
        }
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append('[');
        synchronized (this.stateLock) {
            if (!isOpen()) {
                sb.append("closed");
            } else {
                switch (this.state) {
                    case 0:
                        sb.append("unconnected");
                        break;
                    case 1:
                        sb.append("connection-pending");
                        break;
                    case 2:
                        sb.append("connected");
                        if (this.readShutdown) {
                            sb.append(" ishut");
                        }
                        if (this.writeShutdown) {
                            sb.append(" oshut");
                            break;
                        }
                        break;
                }
                if (this.localAddress != null) {
                    sb.append(" local=");
                    sb.append(Net.getRevealedLocalAddressAsString(this.localAddress));
                }
                if (this.remoteAddress != null) {
                    sb.append(" remote=");
                    sb.append(this.remoteAddress.toString());
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
