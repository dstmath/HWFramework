package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

abstract class AbstractPoller implements Runnable {
    private final LinkedList<Request> requestList = new LinkedList<>();
    private boolean shutdown = false;

    private static class Request {
        private boolean completed = false;
        private final Object[] params;
        private Object result = null;
        private final RequestType type;

        Request(RequestType type2, Object... params2) {
            this.type = type2;
            this.params = params2;
        }

        /* access modifiers changed from: package-private */
        public RequestType type() {
            return this.type;
        }

        /* access modifiers changed from: package-private */
        public Object[] parameters() {
            return this.params;
        }

        /* access modifiers changed from: package-private */
        public void release(Object result2) {
            synchronized (this) {
                this.completed = true;
                this.result = result2;
                notifyAll();
            }
        }

        /* access modifiers changed from: package-private */
        public Object awaitResult() {
            Object obj;
            boolean interrupted = false;
            synchronized (this) {
                while (!this.completed) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                obj = this.result;
            }
            return obj;
        }
    }

    private enum RequestType {
        REGISTER,
        CANCEL,
        CLOSE
    }

    /* access modifiers changed from: package-private */
    public abstract void implCancelKey(WatchKey watchKey);

    /* access modifiers changed from: package-private */
    public abstract void implCloseAll();

    /* access modifiers changed from: package-private */
    public abstract Object implRegister(Path path, Set<? extends WatchEvent.Kind<?>> set, WatchEvent.Modifier... modifierArr);

    /* access modifiers changed from: package-private */
    public abstract void wakeup() throws IOException;

    protected AbstractPoller() {
    }

    public void start() {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                Thread thr = new Thread(this);
                thr.setDaemon(true);
                thr.start();
                return null;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public final WatchKey register(Path dir, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        if (dir != null) {
            Set<WatchEvent.Kind<?>> eventSet = new HashSet<>(events.length);
            for (WatchEvent.Kind<?> event : events) {
                if (event == StandardWatchEventKinds.ENTRY_CREATE || event == StandardWatchEventKinds.ENTRY_MODIFY || event == StandardWatchEventKinds.ENTRY_DELETE) {
                    eventSet.add(event);
                } else if (event != StandardWatchEventKinds.OVERFLOW) {
                    if (event == null) {
                        throw new NullPointerException("An element in event set is 'null'");
                    }
                    throw new UnsupportedOperationException(event.name());
                }
            }
            if (!eventSet.isEmpty()) {
                return (WatchKey) invoke(RequestType.REGISTER, dir, eventSet, modifiers);
            }
            throw new IllegalArgumentException("No events to register");
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final void cancel(WatchKey key) {
        try {
            invoke(RequestType.CANCEL, key);
        } catch (IOException x) {
            throw new AssertionError((Object) x.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public final void close() throws IOException {
        invoke(RequestType.CLOSE, new Object[0]);
    }

    private Object invoke(RequestType type, Object... params) throws IOException {
        Request req = new Request(type, params);
        synchronized (this.requestList) {
            if (!this.shutdown) {
                this.requestList.add(req);
            } else {
                throw new ClosedWatchServiceException();
            }
        }
        wakeup();
        Object result = req.awaitResult();
        if (result instanceof RuntimeException) {
            throw ((RuntimeException) result);
        } else if (!(result instanceof IOException)) {
            return result;
        } else {
            throw ((IOException) result);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean processRequests() {
        synchronized (this.requestList) {
            while (true) {
                Request poll = this.requestList.poll();
                Request req = poll;
                if (poll != null) {
                    if (this.shutdown) {
                        req.release(new ClosedWatchServiceException());
                    }
                    switch (req.type()) {
                        case REGISTER:
                            Object[] params = req.parameters();
                            req.release(implRegister((Path) params[0], (Set) params[1], (WatchEvent.Modifier[]) params[2]));
                            break;
                        case CANCEL:
                            implCancelKey((WatchKey) req.parameters()[0]);
                            req.release(null);
                            break;
                        case CLOSE:
                            implCloseAll();
                            req.release(null);
                            this.shutdown = true;
                            break;
                        default:
                            req.release(new IOException("request not recognized"));
                            break;
                    }
                }
            }
            while (true) {
            }
        }
        return this.shutdown;
    }
}
