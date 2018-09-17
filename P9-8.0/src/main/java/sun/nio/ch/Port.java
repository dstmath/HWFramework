package sun.nio.ch;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

abstract class Port extends AsynchronousChannelGroupImpl {
    protected final Map<Integer, PollableChannel> fdToChannel = new HashMap();
    protected final ReadWriteLock fdToChannelLock = new ReentrantReadWriteLock();

    interface PollableChannel extends Closeable {
        void onEvent(int i, boolean z);
    }

    abstract void startPoll(int i, int i2);

    Port(AsynchronousChannelProvider provider, ThreadPool pool) {
        super(provider, pool);
    }

    final void register(int fd, PollableChannel ch) {
        this.fdToChannelLock.writeLock().lock();
        try {
            if (isShutdown()) {
                throw new ShutdownChannelGroupException();
            }
            this.fdToChannel.put(Integer.valueOf(fd), ch);
        } finally {
            this.fdToChannelLock.writeLock().unlock();
        }
    }

    protected void preUnregister(int fd) {
    }

    final void unregister(int fd) {
        boolean checkForShutdown = false;
        preUnregister(fd);
        this.fdToChannelLock.writeLock().lock();
        try {
            this.fdToChannel.remove(Integer.valueOf(fd));
            if (this.fdToChannel.isEmpty()) {
                checkForShutdown = true;
            }
            this.fdToChannelLock.writeLock().unlock();
            if (checkForShutdown && isShutdown()) {
                try {
                    shutdownNow();
                } catch (IOException e) {
                }
            }
        } catch (Throwable th) {
            this.fdToChannelLock.writeLock().unlock();
        }
    }

    final boolean isEmpty() {
        this.fdToChannelLock.writeLock().lock();
        try {
            boolean isEmpty = this.fdToChannel.isEmpty();
            return isEmpty;
        } finally {
            this.fdToChannelLock.writeLock().unlock();
        }
    }

    final Object attachForeignChannel(final Channel channel, FileDescriptor fd) {
        int fdVal = IOUtil.fdVal(fd);
        register(fdVal, new PollableChannel() {
            public void onEvent(int events, boolean mayInvokeDirect) {
            }

            public void close() throws IOException {
                channel.close();
            }
        });
        return Integer.valueOf(fdVal);
    }

    final void detachForeignChannel(Object key) {
        unregister(((Integer) key).lambda$-java_util_stream_IntPipeline_14709());
    }

    final void closeAllChannels() {
        Throwable th;
        PollableChannel[] channels = new PollableChannel[128];
        int count;
        do {
            this.fdToChannelLock.writeLock().lock();
            count = 0;
            try {
                Iterator fd$iterator = this.fdToChannel.keySet().iterator();
                while (true) {
                    int count2;
                    try {
                        count2 = count;
                        if (!fd$iterator.hasNext()) {
                            count = count2;
                            break;
                        }
                        count = count2 + 1;
                        channels[count2] = (PollableChannel) this.fdToChannel.get((Integer) fd$iterator.next());
                        if (count >= 128) {
                            break;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        count = count2;
                        this.fdToChannelLock.writeLock().unlock();
                        throw th;
                    }
                }
                this.fdToChannelLock.writeLock().unlock();
                for (int i = 0; i < count; i++) {
                    try {
                        channels[i].close();
                    } catch (IOException e) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        } while (count > 0);
    }
}
