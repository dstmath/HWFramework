package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import sun.net.NetHooks;

abstract class AsynchronousServerSocketChannelImpl extends AsynchronousServerSocketChannel implements Cancellable, Groupable {
    private volatile boolean acceptKilled;
    private ReadWriteLock closeLock = new ReentrantReadWriteLock();
    protected final FileDescriptor fd = Net.serverSocket(true);
    private boolean isReuseAddress;
    protected volatile InetSocketAddress localAddress = null;
    private volatile boolean open = true;
    private final Object stateLock = new Object();

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet(2);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_RCVBUF);
            set.-java_util_stream_DistinctOps$1-mthref-1(StandardSocketOptions.SO_REUSEADDR);
            return Collections.unmodifiableSet(set);
        }
    }

    abstract Future<AsynchronousSocketChannel> implAccept(Object obj, CompletionHandler<AsynchronousSocketChannel, Object> completionHandler);

    abstract void implClose() throws IOException;

    AsynchronousServerSocketChannelImpl(AsynchronousChannelGroupImpl group) {
        super(group.provider());
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

    public final Future<AsynchronousSocketChannel> accept() {
        return implAccept(null, null);
    }

    public final <A> void accept(A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler) {
        if (handler == null) {
            throw new NullPointerException("'handler' is null");
        }
        implAccept(attachment, handler);
    }

    final boolean isAcceptKilled() {
        return this.acceptKilled;
    }

    public final void onCancel(PendingFuture<?, ?> pendingFuture) {
        this.acceptKilled = true;
    }

    public final AsynchronousServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        InetSocketAddress isa;
        if (local == null) {
            isa = new InetSocketAddress(0);
        } else {
            isa = Net.checkAddress(local);
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkListen(isa.getPort());
        }
        try {
            begin();
            synchronized (this.stateLock) {
                if (this.localAddress != null) {
                    throw new AlreadyBoundException();
                }
                NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
                Net.bind(this.fd, isa.getAddress(), isa.getPort());
                FileDescriptor fileDescriptor = this.fd;
                if (backlog < 1) {
                    backlog = 50;
                }
                Net.listen(fileDescriptor, backlog);
                this.localAddress = Net.localAddress(this.fd);
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

    public final <T> AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        } else if (supportedOptions().contains(name)) {
            try {
                begin();
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

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append('[');
        if (!isOpen()) {
            sb.append("closed");
        } else if (this.localAddress == null) {
            sb.append("unbound");
        } else {
            sb.append(Net.getRevealedLocalAddressAsString(this.localAddress));
        }
        sb.append(']');
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}
