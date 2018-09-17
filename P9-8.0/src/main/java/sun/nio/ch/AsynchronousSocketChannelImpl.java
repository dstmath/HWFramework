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
            HashSet<SocketOption<?>> set = new HashSet(5);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_SNDBUF);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_RCVBUF);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_KEEPALIVE);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_REUSEADDR);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.TCP_NODELAY);
            if (ExtendedOptionsImpl.flowSupported()) {
                set.-java_util_stream_DistinctOps$1-mthref-1(ExtendedSocketOptions.SO_FLOW_SLA);
            }
            return Collections.unmodifiableSet(set);
        }
    }

    abstract void implClose() throws IOException;

    abstract <A> Future<Void> implConnect(SocketAddress socketAddress, A a, CompletionHandler<Void, ? super A> completionHandler);

    abstract <V extends Number, A> Future<V> implRead(boolean z, ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr, long j, TimeUnit timeUnit, A a, CompletionHandler<V, ? super A> completionHandler);

    abstract <V extends Number, A> Future<V> implWrite(boolean z, ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr, long j, TimeUnit timeUnit, A a, CompletionHandler<V, ? super A> completionHandler);

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

    AsynchronousSocketChannelImpl(AsynchronousChannelGroupImpl group, FileDescriptor fd, InetSocketAddress remote) throws IOException {
        super(group.provider());
        this.stateLock = new Object();
        this.localAddress = null;
        this.remoteAddress = null;
        this.state = -1;
        this.readLock = new Object();
        this.writeLock = new Object();
        this.closeLock = new ReentrantReadWriteLock();
        this.open = true;
        this.fd = fd;
        this.state = 2;
        this.localAddress = Net.localAddress(fd);
        this.remoteAddress = remote;
    }

    public final boolean isOpen() {
        return this.open;
    }

    final void begin() throws IOException {
        this.closeLock.readLock().lock();
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    final void end() {
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

    final void enableReading(boolean killed) {
        synchronized (this.readLock) {
            this.reading = false;
            if (killed) {
                this.readKilled = true;
            }
        }
    }

    final void enableReading() {
        enableReading(false);
    }

    final void enableWriting(boolean killed) {
        synchronized (this.writeLock) {
            this.writing = false;
            if (killed) {
                this.writeKilled = true;
            }
        }
    }

    final void enableWriting() {
        enableWriting(false);
    }

    final void killReading() {
        synchronized (this.readLock) {
            this.readKilled = true;
        }
    }

    final void killWriting() {
        synchronized (this.writeLock) {
            this.writeKilled = true;
        }
    }

    final void killConnect() {
        killReading();
        killWriting();
    }

    public final Future<Void> connect(SocketAddress remote) {
        return implConnect(remote, null, null);
    }

    public final <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        }
        implConnect(remote, attachment, handler);
    }

    private <V extends Number, A> Future<V> read(boolean isScatteringRead, ByteBuffer dst, ByteBuffer[] dsts, long timeout, TimeUnit unit, A att, CompletionHandler<V, ? super A> handler) {
        if (!isOpen()) {
            Throwable e = new ClosedChannelException();
            if (handler == null) {
                return CompletedFuture.withFailure(e);
            }
            Invoker.invoke(this, handler, att, null, e);
            return null;
        } else if (this.remoteAddress == null) {
            throw new NotYetConnectedException();
        } else {
            int hasSpaceToRead = !isScatteringRead ? dst.hasRemaining() : 1;
            boolean shutdown = false;
            synchronized (this.readLock) {
                if (this.readKilled) {
                    throw new IllegalStateException("Reading not allowed due to timeout or cancellation");
                } else if (this.reading) {
                    throw new ReadPendingException();
                } else {
                    if (this.readShutdown) {
                        shutdown = true;
                    } else if (hasSpaceToRead != 0) {
                        this.reading = true;
                    }
                }
            }
            if (!shutdown && (hasSpaceToRead ^ 1) == 0) {
                return implRead(isScatteringRead, dst, dsts, timeout, unit, att, handler);
            }
            Number result;
            if (isScatteringRead) {
                result = shutdown ? Long.valueOf(-1) : Long.valueOf(0);
            } else {
                result = Integer.valueOf(shutdown ? -1 : 0);
            }
            if (handler == null) {
                return CompletedFuture.withResult(result);
            }
            Invoker.invoke(this, handler, att, result, null);
            return null;
        }
    }

    public final Future<Integer> read(ByteBuffer dst) {
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        }
        return read(false, dst, null, 0, TimeUnit.MILLISECONDS, null, null);
    }

    public final <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        } else if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        } else {
            read(false, dst, null, timeout, unit, (Object) attachment, (CompletionHandler) handler);
        }
    }

    public final <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        } else if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        } else {
            ByteBuffer[] bufs = Util.subsequence(dsts, offset, length);
            for (ByteBuffer isReadOnly : bufs) {
                if (isReadOnly.isReadOnly()) {
                    throw new IllegalArgumentException("Read-only buffer");
                }
            }
            read(true, null, bufs, timeout, unit, (Object) attachment, (CompletionHandler) handler);
        }
    }

    private <V extends Number, A> Future<V> write(boolean isGatheringWrite, ByteBuffer src, ByteBuffer[] srcs, long timeout, TimeUnit unit, A att, CompletionHandler<V, ? super A> handler) {
        boolean hasDataToWrite = !isGatheringWrite ? src.hasRemaining() : true;
        boolean closed = false;
        if (!isOpen()) {
            closed = true;
        } else if (this.remoteAddress == null) {
            throw new NotYetConnectedException();
        } else {
            synchronized (this.writeLock) {
                if (this.writeKilled) {
                    throw new IllegalStateException("Writing not allowed due to timeout or cancellation");
                } else if (this.writing) {
                    throw new WritePendingException();
                } else {
                    if (this.writeShutdown) {
                        closed = true;
                    } else if (hasDataToWrite) {
                        this.writing = true;
                    }
                }
            }
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
            Number result = isGatheringWrite ? Long.valueOf(0) : Integer.valueOf(0);
            if (handler == null) {
                return CompletedFuture.withResult(result);
            }
            Invoker.invoke(this, handler, att, result, null);
            return null;
        }
    }

    public final Future<Integer> write(ByteBuffer src) {
        return write(false, src, null, 0, TimeUnit.MILLISECONDS, null, null);
    }

    public final <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        }
        write(false, src, null, timeout, unit, (Object) attachment, (CompletionHandler) handler);
    }

    public final <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        } else if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        } else {
            write(true, null, Util.subsequence(srcs, offset, length), timeout, unit, (Object) attachment, (CompletionHandler) handler);
        }
    }

    public final AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        try {
            begin();
            synchronized (this.stateLock) {
                if (this.state == 1) {
                    throw new ConnectionPendingException();
                } else if (this.localAddress != null) {
                    throw new AlreadyBoundException();
                } else {
                    InetSocketAddress isa = local == null ? new InetSocketAddress(0) : Net.checkAddress(local);
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkListen(isa.getPort());
                    }
                    NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                    Net.bind(this.fd, isa.getAddress(), isa.getPort());
                    this.localAddress = Net.localAddress(this.fd);
                }
            }
            return this;
        } finally {
            end();
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
                if (this.writeShutdown) {
                    throw new IOException("Connection has been shutdown for writing");
                }
                if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    this.isReuseAddress = ((Boolean) value).booleanValue();
                } else {
                    Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
                }
                end();
                return this;
            } catch (Throwable th) {
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
                T valueOf;
                if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    valueOf = Boolean.valueOf(this.isReuseAddress);
                    return valueOf;
                }
                valueOf = Net.getSocketOption(this.fd, Net.UNSPEC, name);
                end();
                return valueOf;
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
            if (this.remoteAddress == null) {
                throw new NotYetConnectedException();
            }
            synchronized (this.readLock) {
                if (!this.readShutdown) {
                    Net.shutdown(this.fd, 0);
                    this.readShutdown = true;
                }
            }
            end();
            return this;
        } catch (Throwable th) {
            end();
        }
    }

    public final AsynchronousSocketChannel shutdownOutput() throws IOException {
        try {
            begin();
            if (this.remoteAddress == null) {
                throw new NotYetConnectedException();
            }
            synchronized (this.writeLock) {
                if (!this.writeShutdown) {
                    Net.shutdown(this.fd, 1);
                    this.writeShutdown = true;
                }
            }
            end();
            return this;
        } catch (Throwable th) {
            end();
        }
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append('[');
        synchronized (this.stateLock) {
            if (isOpen()) {
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
            } else {
                sb.append("closed");
            }
        }
        sb.append(']');
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}
