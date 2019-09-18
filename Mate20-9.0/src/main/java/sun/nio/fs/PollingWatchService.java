package sun.nio.fs;

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class PollingWatchService extends AbstractWatchService {
    /* access modifiers changed from: private */
    public final Map<Object, PollingWatchKey> map = new HashMap();
    /* access modifiers changed from: private */
    public final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private static class CacheEntry {
        /* access modifiers changed from: private */
        public long lastModified;
        private int lastTickCount;

        CacheEntry(long lastModified2, int lastTickCount2) {
            this.lastModified = lastModified2;
            this.lastTickCount = lastTickCount2;
        }

        /* access modifiers changed from: package-private */
        public int lastTickCount() {
            return this.lastTickCount;
        }

        /* access modifiers changed from: package-private */
        public long lastModified() {
            return this.lastModified;
        }

        /* access modifiers changed from: package-private */
        public void update(long lastModified2, int tickCount) {
            this.lastModified = lastModified2;
            this.lastTickCount = tickCount;
        }
    }

    private class PollingWatchKey extends AbstractWatchKey {
        private Map<Path, CacheEntry> entries = new HashMap();
        private Set<? extends WatchEvent.Kind<?>> events;
        private final Object fileKey;
        private ScheduledFuture<?> poller;
        private int tickCount = 0;
        private volatile boolean valid = true;

        PollingWatchKey(Path dir, PollingWatchService watcher, Object fileKey2) throws IOException {
            super(dir, watcher);
            DirectoryStream<Path> stream;
            Throwable th;
            this.fileKey = fileKey2;
            try {
                stream = Files.newDirectoryStream(dir);
                for (Path entry : stream) {
                    this.entries.put(entry.getFileName(), new CacheEntry(Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis(), this.tickCount));
                }
                if (stream != null) {
                    stream.close();
                    return;
                }
                return;
            } catch (DirectoryIteratorException e) {
                throw e.getCause();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }

        /* access modifiers changed from: package-private */
        public Object fileKey() {
            return this.fileKey;
        }

        public boolean isValid() {
            return this.valid;
        }

        /* access modifiers changed from: package-private */
        public void invalidate() {
            this.valid = false;
        }

        /* access modifiers changed from: package-private */
        public void enable(Set<? extends WatchEvent.Kind<?>> events2, long period) {
            synchronized (this) {
                this.events = events2;
                this.poller = PollingWatchService.this.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        PollingWatchKey.this.poll();
                    }
                }, period, period, TimeUnit.SECONDS);
            }
        }

        /* access modifiers changed from: package-private */
        public void disable() {
            synchronized (this) {
                if (this.poller != null) {
                    this.poller.cancel(false);
                }
            }
        }

        public void cancel() {
            this.valid = false;
            synchronized (PollingWatchService.this.map) {
                PollingWatchService.this.map.remove(fileKey());
            }
            disable();
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:43:0x00b0=Splitter:B:43:0x00b0, B:49:0x00b8=Splitter:B:49:0x00b8} */
        public synchronized void poll() {
            if (this.valid) {
                this.tickCount++;
                try {
                    DirectoryStream<Path> stream = Files.newDirectoryStream(watchable());
                    try {
                        for (Path entry : stream) {
                            try {
                                long lastModified = Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
                                CacheEntry e = this.entries.get(entry.getFileName());
                                if (e == null) {
                                    this.entries.put(entry.getFileName(), new CacheEntry(lastModified, this.tickCount));
                                    if (this.events.contains(StandardWatchEventKinds.ENTRY_CREATE)) {
                                        signalEvent(StandardWatchEventKinds.ENTRY_CREATE, entry.getFileName());
                                    } else if (this.events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                        signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
                                    }
                                } else {
                                    if (e.lastModified != lastModified && this.events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                        signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
                                    }
                                    e.update(lastModified, this.tickCount);
                                }
                            } catch (IOException e2) {
                            }
                        }
                        try {
                            stream.close();
                        } catch (IOException e3) {
                        }
                    } catch (DirectoryIteratorException e4) {
                        stream.close();
                    } catch (Throwable th) {
                        try {
                            stream.close();
                        } catch (IOException e5) {
                        }
                        throw th;
                    }
                    Iterator<Map.Entry<Path, CacheEntry>> i = this.entries.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry<Path, CacheEntry> mapEntry = i.next();
                        if (mapEntry.getValue().lastTickCount() != this.tickCount) {
                            Path name = mapEntry.getKey();
                            i.remove();
                            if (this.events.contains(StandardWatchEventKinds.ENTRY_DELETE)) {
                                signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
                            }
                        }
                    }
                    return;
                } catch (IOException e6) {
                    cancel();
                    signal();
                    return;
                }
            } else {
                return;
            }
        }
    }

    PollingWatchService() {
    }

    /* access modifiers changed from: package-private */
    public WatchKey register(final Path path, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        final Set<WatchEvent.Kind<?>> eventSet = new HashSet<>(events.length);
        int i = 0;
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
            SensitivityWatchEventModifier sensivity = SensitivityWatchEventModifier.MEDIUM;
            if (modifiers.length > 0) {
                int length = modifiers.length;
                while (i < length) {
                    SensitivityWatchEventModifier modifier = modifiers[i];
                    if (modifier == null) {
                        throw new NullPointerException();
                    } else if (modifier instanceof SensitivityWatchEventModifier) {
                        sensivity = modifier;
                        i++;
                    } else {
                        throw new UnsupportedOperationException("Modifier not supported");
                    }
                }
            }
            if (isOpen()) {
                final SensitivityWatchEventModifier s = sensivity;
                try {
                    return (WatchKey) AccessController.doPrivileged(new PrivilegedExceptionAction<PollingWatchKey>() {
                        public PollingWatchKey run() throws IOException {
                            return PollingWatchService.this.doPrivilegedRegister(path, eventSet, s);
                        }
                    });
                } catch (PrivilegedActionException pae) {
                    Throwable cause = pae.getCause();
                    if (cause == null || !(cause instanceof IOException)) {
                        throw new AssertionError((Object) pae);
                    }
                    throw ((IOException) cause);
                }
            } else {
                throw new ClosedWatchServiceException();
            }
        } else {
            throw new IllegalArgumentException("No events to register");
        }
    }

    /* access modifiers changed from: private */
    public PollingWatchKey doPrivilegedRegister(Path path, Set<? extends WatchEvent.Kind<?>> events, SensitivityWatchEventModifier sensivity) throws IOException {
        PollingWatchKey watchKey;
        PollingWatchKey watchKey2;
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
        if (attrs.isDirectory()) {
            Object fileKey = attrs.fileKey();
            if (fileKey != null) {
                synchronized (closeLock()) {
                    if (isOpen()) {
                        synchronized (this.map) {
                            watchKey = this.map.get(fileKey);
                            if (watchKey == null) {
                                watchKey = new PollingWatchKey(path, this, fileKey);
                                this.map.put(fileKey, watchKey);
                            } else {
                                watchKey.disable();
                            }
                        }
                        watchKey2 = watchKey;
                        watchKey2.enable(events, (long) sensivity.sensitivityValueInSeconds());
                    } else {
                        throw new ClosedWatchServiceException();
                    }
                }
                return watchKey2;
            }
            throw new AssertionError((Object) "File keys must be supported");
        }
        throw new NotDirectoryException(path.toString());
    }

    /* access modifiers changed from: package-private */
    public void implClose() throws IOException {
        synchronized (this.map) {
            for (Map.Entry<Object, PollingWatchKey> entry : this.map.entrySet()) {
                PollingWatchKey watchKey = entry.getValue();
                watchKey.disable();
                watchKey.invalidate();
            }
            this.map.clear();
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                PollingWatchService.this.scheduledExecutor.shutdown();
                return null;
            }
        });
    }
}
