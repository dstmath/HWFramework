package java.nio.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class SocketChannel extends AbstractSelectableChannel implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {
    static final /* synthetic */ boolean -assertionsDisabled = (SocketChannel.class.desiredAssertionStatus() ^ 1);

    public abstract SocketChannel bind(SocketAddress socketAddress) throws IOException;

    public abstract boolean connect(SocketAddress socketAddress) throws IOException;

    public abstract boolean finishConnect() throws IOException;

    public abstract SocketAddress getLocalAddress() throws IOException;

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract boolean isConnected();

    public abstract boolean isConnectionPending();

    public abstract int read(ByteBuffer byteBuffer) throws IOException;

    public abstract long read(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;

    public abstract <T> SocketChannel setOption(SocketOption<T> socketOption, T t) throws IOException;

    public abstract SocketChannel shutdownInput() throws IOException;

    public abstract SocketChannel shutdownOutput() throws IOException;

    public abstract Socket socket();

    public abstract int write(ByteBuffer byteBuffer) throws IOException;

    public abstract long write(ByteBuffer[] byteBufferArr, int i, int i2) throws IOException;

    protected SocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    public static SocketChannel open(SocketAddress remote) throws IOException {
        SocketChannel sc = open();
        try {
            sc.connect(remote);
            if (-assertionsDisabled || sc.isConnected()) {
                return sc;
            }
            throw new AssertionError();
        } catch (Throwable suppressed) {
            x.addSuppressed(suppressed);
        }
    }

    public final int validOps() {
        return 13;
    }

    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }
}
