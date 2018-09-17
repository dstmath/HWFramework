package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

abstract class AbstractWatchService implements WatchService {
    private final WatchKey CLOSE_KEY = new AbstractWatchKey(null, null) {
        public boolean isValid() {
            return true;
        }

        public void cancel() {
        }
    };
    private final Object closeLock = new Object();
    private volatile boolean closed;
    private final LinkedBlockingDeque<WatchKey> pendingKeys = new LinkedBlockingDeque();

    abstract void implClose() throws IOException;

    abstract WatchKey register(Path path, Kind<?>[] kindArr, Modifier... modifierArr) throws IOException;

    protected AbstractWatchService() {
    }

    final void enqueueKey(WatchKey key) {
        this.pendingKeys.offer(key);
    }

    private void checkOpen() {
        if (this.closed) {
            throw new ClosedWatchServiceException();
        }
    }

    private void checkKey(WatchKey key) {
        if (key == this.CLOSE_KEY) {
            enqueueKey(key);
        }
        checkOpen();
    }

    public final WatchKey poll() {
        checkOpen();
        WatchKey key = (WatchKey) this.pendingKeys.poll();
        checkKey(key);
        return key;
    }

    public final WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        checkOpen();
        WatchKey key = (WatchKey) this.pendingKeys.poll(timeout, unit);
        checkKey(key);
        return key;
    }

    public final WatchKey take() throws InterruptedException {
        checkOpen();
        WatchKey key = (WatchKey) this.pendingKeys.take();
        checkKey(key);
        return key;
    }

    final boolean isOpen() {
        return this.closed ^ 1;
    }

    final Object closeLock() {
        return this.closeLock;
    }

    public final void close() throws IOException {
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            implClose();
            this.pendingKeys.clear();
            this.pendingKeys.offer(this.CLOSE_KEY);
        }
    }
}
