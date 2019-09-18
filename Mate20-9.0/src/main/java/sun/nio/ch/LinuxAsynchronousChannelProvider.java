package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.IllegalChannelGroupException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class LinuxAsynchronousChannelProvider extends AsynchronousChannelProvider {
    private static volatile EPollPort defaultPort;

    private EPollPort defaultEventPort() throws IOException {
        if (defaultPort == null) {
            synchronized (LinuxAsynchronousChannelProvider.class) {
                if (defaultPort == null) {
                    defaultPort = new EPollPort(this, ThreadPool.getDefault()).start();
                }
            }
        }
        return defaultPort;
    }

    public AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory factory) throws IOException {
        return new EPollPort(this, ThreadPool.create(nThreads, factory)).start();
    }

    public AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize) throws IOException {
        return new EPollPort(this, ThreadPool.wrap(executor, initialSize)).start();
    }

    private Port toPort(AsynchronousChannelGroup group) throws IOException {
        if (group == null) {
            return defaultEventPort();
        }
        if (group instanceof EPollPort) {
            return (Port) group;
        }
        throw new IllegalChannelGroupException();
    }

    public AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(AsynchronousChannelGroup group) throws IOException {
        return new UnixAsynchronousServerSocketChannelImpl(toPort(group));
    }

    public AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup group) throws IOException {
        return new UnixAsynchronousSocketChannelImpl(toPort(group));
    }
}
