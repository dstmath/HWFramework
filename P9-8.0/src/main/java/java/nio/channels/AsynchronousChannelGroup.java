package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class AsynchronousChannelGroup {
    private final AsynchronousChannelProvider provider;

    public abstract boolean awaitTermination(long j, TimeUnit timeUnit) throws InterruptedException;

    public abstract boolean isShutdown();

    public abstract boolean isTerminated();

    public abstract void shutdown();

    public abstract void shutdownNow() throws IOException;

    protected AsynchronousChannelGroup(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }

    public final AsynchronousChannelProvider provider() {
        return this.provider;
    }

    public static AsynchronousChannelGroup withFixedThreadPool(int nThreads, ThreadFactory threadFactory) throws IOException {
        return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(nThreads, threadFactory);
    }

    public static AsynchronousChannelGroup withCachedThreadPool(ExecutorService executor, int initialSize) throws IOException {
        return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(executor, initialSize);
    }

    public static AsynchronousChannelGroup withThreadPool(ExecutorService executor) throws IOException {
        return AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(executor, 0);
    }
}
