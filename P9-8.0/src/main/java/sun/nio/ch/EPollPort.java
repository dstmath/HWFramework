package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

final class EPollPort extends Port {
    private static final int ENOENT = 2;
    private static final int MAX_EPOLL_EVENTS = 512;
    private final Event EXECUTE_TASK_OR_SHUTDOWN = new Event(null, 0);
    private final Event NEED_TO_POLL = new Event(null, 0);
    private final long address;
    private boolean closed;
    private final int epfd = EPoll.epollCreate();
    private final ArrayBlockingQueue<Event> queue;
    private final int[] sp;
    private final AtomicInteger wakeupCount = new AtomicInteger();

    static class Event {
        final PollableChannel channel;
        final int events;

        Event(PollableChannel channel, int events) {
            this.channel = channel;
            this.events = events;
        }

        PollableChannel channel() {
            return this.channel;
        }

        int events() {
            return this.events;
        }
    }

    private class EventHandlerTask implements Runnable {
        /* synthetic */ EventHandlerTask(EPollPort this$0, EventHandlerTask -this1) {
            this();
        }

        private EventHandlerTask() {
        }

        /* JADX WARNING: Missing block: B:29:?, code:
            r12.this$0.fdToChannelLock.readLock().unlock();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Event poll() throws IOException {
            while (true) {
                try {
                    int n = EPoll.epollWait(EPollPort.this.epfd, EPollPort.this.address, 512);
                    EPollPort.this.fdToChannelLock.readLock().lock();
                    while (true) {
                        int n2 = n - 1;
                        if (n <= 0) {
                            break;
                        }
                        long eventAddress = EPoll.getEvent(EPollPort.this.address, n2);
                        int fd = EPoll.getDescriptor(eventAddress);
                        if (fd == EPollPort.this.sp[0]) {
                            if (EPollPort.this.wakeupCount.decrementAndGet() == 0) {
                                EPollPort.drain1(EPollPort.this.sp[0]);
                            }
                            if (n2 > 0) {
                                EPollPort.this.queue.offer(EPollPort.this.EXECUTE_TASK_OR_SHUTDOWN);
                                n = n2;
                            } else {
                                Event -get0 = EPollPort.this.EXECUTE_TASK_OR_SHUTDOWN;
                                EPollPort.this.fdToChannelLock.readLock().unlock();
                                EPollPort.this.queue.offer(EPollPort.this.NEED_TO_POLL);
                                return -get0;
                            }
                        }
                        PollableChannel channel = (PollableChannel) EPollPort.this.fdToChannel.get(Integer.valueOf(fd));
                        if (channel != null) {
                            Event ev = new Event(channel, EPoll.getEvents(eventAddress));
                            if (n2 > 0) {
                                EPollPort.this.queue.offer(ev);
                            } else {
                                EPollPort.this.fdToChannelLock.readLock().unlock();
                                EPollPort.this.queue.offer(EPollPort.this.NEED_TO_POLL);
                                return ev;
                            }
                        }
                        n = n2;
                    }
                } catch (Throwable th) {
                    EPollPort.this.queue.offer(EPollPort.this.NEED_TO_POLL);
                }
            }
        }

        public void run() {
            GroupAndInvokeCount myGroupAndInvokeCount = Invoker.getGroupAndInvokeCount();
            boolean isPooledThread = myGroupAndInvokeCount != null;
            boolean replaceMe = false;
            while (true) {
                if (isPooledThread) {
                    try {
                        myGroupAndInvokeCount.resetInvokeCount();
                    } catch (Error x) {
                        replaceMe = true;
                        throw x;
                    } catch (RuntimeException x2) {
                        throw x2;
                    } catch (Throwable th) {
                        if (EPollPort.this.threadExit(this, replaceMe) == 0 && EPollPort.this.isShutdown()) {
                            EPollPort.this.implClose();
                        }
                    }
                }
                replaceMe = false;
                try {
                    Event ev = (Event) EPollPort.this.queue.take();
                    if (ev == EPollPort.this.NEED_TO_POLL) {
                        try {
                            ev = poll();
                        } catch (IOException x3) {
                            x3.printStackTrace();
                            if (EPollPort.this.threadExit(this, false) == 0 && EPollPort.this.isShutdown()) {
                                EPollPort.this.implClose();
                            }
                            return;
                        }
                    }
                    if (ev == EPollPort.this.EXECUTE_TASK_OR_SHUTDOWN) {
                        Runnable task = EPollPort.this.pollTask();
                        if (task == null) {
                            if (EPollPort.this.threadExit(this, false) == 0 && EPollPort.this.isShutdown()) {
                                EPollPort.this.implClose();
                            }
                            return;
                        }
                        replaceMe = true;
                        task.run();
                    } else {
                        ev.channel().onEvent(ev.events(), isPooledThread);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static native void close0(int i);

    private static native void drain1(int i) throws IOException;

    private static native void interrupt(int i) throws IOException;

    private static native void socketpair(int[] iArr) throws IOException;

    EPollPort(AsynchronousChannelProvider provider, ThreadPool pool) throws IOException {
        super(provider, pool);
        int[] sv = new int[2];
        try {
            socketpair(sv);
            EPoll.epollCtl(this.epfd, 1, sv[0], Net.POLLIN);
            this.sp = sv;
            this.address = EPoll.allocatePollArray(512);
            this.queue = new ArrayBlockingQueue(512);
            this.queue.offer(this.NEED_TO_POLL);
        } catch (IOException x) {
            close0(this.epfd);
            throw x;
        }
    }

    EPollPort start() {
        startThreads(new EventHandlerTask(this, null));
        return this;
    }

    private void implClose() {
        synchronized (this) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            EPoll.freePollArray(this.address);
            close0(this.sp[0]);
            close0(this.sp[1]);
            close0(this.epfd);
        }
    }

    private void wakeup() {
        if (this.wakeupCount.incrementAndGet() == 1) {
            try {
                interrupt(this.sp[1]);
            } catch (Object x) {
                throw new AssertionError(x);
            }
        }
    }

    void executeOnHandlerTask(Runnable task) {
        synchronized (this) {
            if (this.closed) {
                throw new RejectedExecutionException();
            }
            offerTask(task);
            wakeup();
        }
    }

    void shutdownHandlerTasks() {
        int nThreads = threadCount();
        if (nThreads == 0) {
            implClose();
            return;
        }
        while (true) {
            int i = nThreads;
            nThreads = i - 1;
            if (i > 0) {
                wakeup();
            } else {
                return;
            }
        }
    }

    void startPoll(int fd, int events) {
        int err = EPoll.epollCtl(this.epfd, 3, fd, events | 1073741824);
        if (err == 2) {
            err = EPoll.epollCtl(this.epfd, 1, fd, events | 1073741824);
        }
        if (err != 0) {
            throw new AssertionError();
        }
    }
}
