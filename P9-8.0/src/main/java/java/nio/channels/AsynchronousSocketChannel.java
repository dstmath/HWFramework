package java.nio.channels;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class AsynchronousSocketChannel implements AsynchronousByteChannel, NetworkChannel {
    private final AsynchronousChannelProvider provider;

    public abstract AsynchronousSocketChannel bind(SocketAddress socketAddress) throws IOException;

    public abstract Future<Void> connect(SocketAddress socketAddress);

    public abstract <A> void connect(SocketAddress socketAddress, A a, CompletionHandler<Void, ? super A> completionHandler);

    public abstract SocketAddress getLocalAddress() throws IOException;

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract Future<Integer> read(ByteBuffer byteBuffer);

    public abstract <A> void read(ByteBuffer byteBuffer, long j, TimeUnit timeUnit, A a, CompletionHandler<Integer, ? super A> completionHandler);

    public abstract <A> void read(ByteBuffer[] byteBufferArr, int i, int i2, long j, TimeUnit timeUnit, A a, CompletionHandler<Long, ? super A> completionHandler);

    public abstract <T> AsynchronousSocketChannel setOption(SocketOption<T> socketOption, T t) throws IOException;

    public abstract AsynchronousSocketChannel shutdownInput() throws IOException;

    public abstract AsynchronousSocketChannel shutdownOutput() throws IOException;

    public abstract Future<Integer> write(ByteBuffer byteBuffer);

    public abstract <A> void write(ByteBuffer byteBuffer, long j, TimeUnit timeUnit, A a, CompletionHandler<Integer, ? super A> completionHandler);

    public abstract <A> void write(ByteBuffer[] byteBufferArr, int i, int i2, long j, TimeUnit timeUnit, A a, CompletionHandler<Long, ? super A> completionHandler);

    protected AsynchronousSocketChannel(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }

    public final AsynchronousChannelProvider provider() {
        return this.provider;
    }

    public static AsynchronousSocketChannel open(AsynchronousChannelGroup group) throws IOException {
        return (group == null ? AsynchronousChannelProvider.provider() : group.provider()).openAsynchronousSocketChannel(group);
    }

    public static AsynchronousSocketChannel open() throws IOException {
        return open(null);
    }

    public final <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        read(dst, 0, TimeUnit.MILLISECONDS, attachment, handler);
    }

    public final <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        write(src, 0, TimeUnit.MILLISECONDS, attachment, handler);
    }
}
