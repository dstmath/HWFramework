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
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class PollingWatchService extends AbstractWatchService {
    private final Map<Object, PollingWatchKey> map = new HashMap();
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private static class CacheEntry {
        private long lastModified;
        private int lastTickCount;

        CacheEntry(long lastModified, int lastTickCount) {
            this.lastModified = lastModified;
            this.lastTickCount = lastTickCount;
        }

        int lastTickCount() {
            return this.lastTickCount;
        }

        long lastModified() {
            return this.lastModified;
        }

        void update(long lastModified, int tickCount) {
            this.lastModified = lastModified;
            this.lastTickCount = tickCount;
        }
    }

    private class PollingWatchKey extends AbstractWatchKey {
        private Map<Path, CacheEntry> entries = new HashMap();
        private Set<? extends Kind<?>> events;
        private final Object fileKey;
        private ScheduledFuture<?> poller;
        private int tickCount = 0;
        private volatile boolean valid = true;

        PollingWatchKey(Path dir, PollingWatchService watcher, Object fileKey) throws IOException {
            Throwable th;
            super(dir, watcher);
            this.fileKey = fileKey;
            Throwable th2 = null;
            DirectoryStream directoryStream = null;
            try {
                DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
                for (Path entry : stream) {
                    this.entries.put(entry.getFileName(), new CacheEntry(Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis(), this.tickCount));
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                return;
            } catch (Throwable th22) {
                Throwable th4 = th22;
                th22 = th;
                th = th4;
            }
            if (directoryStream != null) {
                try {
                    directoryStream.close();
                } catch (Throwable th5) {
                    if (th22 == null) {
                        th22 = th5;
                    } else if (th22 != th5) {
                        th22.addSuppressed(th5);
                    }
                }
            }
            if (th22 != null) {
                try {
                    throw th22;
                } catch (DirectoryIteratorException e) {
                    throw e.getCause();
                }
            }
            throw th;
        }

        Object fileKey() {
            return this.fileKey;
        }

        public boolean isValid() {
            return this.valid;
        }

        void invalidate() {
            this.valid = false;
        }

        void enable(Set<? extends Kind<?>> events, long period) {
            synchronized (this) {
                this.events = events;
                this.poller = PollingWatchService.this.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        PollingWatchKey.this.poll();
                    }
                }, period, period, TimeUnit.SECONDS);
            }
        }

        void disable() {
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

        synchronized void poll() {
            if (this.valid) {
                this.tickCount++;
                DirectoryStream<Path> stream;
                try {
                    stream = Files.newDirectoryStream(watchable());
                    for (Path entry : stream) {
                        try {
                            long lastModified = Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
                            CacheEntry e = (CacheEntry) this.entries.get(entry.getFileName());
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
                    try {
                        stream.close();
                    } catch (IOException e5) {
                    }
                } catch (IOException e6) {
                    cancel();
                    signal();
                    return;
                } catch (Throwable th) {
                    try {
                        stream.close();
                    } catch (IOException e7) {
                    }
                }
                Iterator<Entry<Path, CacheEntry>> i = this.entries.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<Path, CacheEntry> mapEntry = (Entry) i.next();
                    if (((CacheEntry) mapEntry.getValue()).lastTickCount() != this.tickCount) {
                        Path name = (Path) mapEntry.getKey();
                        i.remove();
                        if (this.events.contains(StandardWatchEventKinds.ENTRY_DELETE)) {
                            signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
                        }
                    }
                }
                return;
            }
            return;
        }
    }

    PollingWatchService() {
    }

    WatchKey register(final Path path, Kind<?>[] events, Modifier... modifiers) throws IOException {
        int length;
        int i = 0;
        final Set<Kind<?>> eventSet = new HashSet(events.length);
        for (Kind<?> event : events) {
            if (event == StandardWatchEventKinds.ENTRY_CREATE || event == StandardWatchEventKinds.ENTRY_MODIFY || event == StandardWatchEventKinds.ENTRY_DELETE) {
                eventSet.-java_util_stream_Collectors-mthref-4(event);
            } else if (event != StandardWatchEventKinds.OVERFLOW) {
                if (event == null) {
                    throw new NullPointerException("An element in event set is 'null'");
                }
                throw new UnsupportedOperationException(event.name());
            }
        }
        if (eventSet.isEmpty()) {
            throw new IllegalArgumentException("No events to register");
        }
        SensitivityWatchEventModifier sensivity = SensitivityWatchEventModifier.MEDIUM;
        if (modifiers.length > 0) {
            length = modifiers.length;
            while (i < length) {
                Modifier modifier = modifiers[i];
                if (modifier == null) {
                    throw new NullPointerException();
                } else if (modifier instanceof SensitivityWatchEventModifier) {
                    sensivity = (SensitivityWatchEventModifier) modifier;
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
            } catch (Object pae) {
                Throwable cause = pae.getCause();
                if (cause == null || !(cause instanceof IOException)) {
                    throw new AssertionError(pae);
                }
                throw ((IOException) cause);
            }
        }
        throw new ClosedWatchServiceException();
    }

    private PollingWatchKey doPrivilegedRegister(Path path, Set<? extends Kind<?>> events, SensitivityWatchEventModifier sensivity) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
        if (attrs.isDirectory()) {
            Object fileKey = attrs.fileKey();
            if (fileKey == null) {
                throw new AssertionError((Object) "File keys must be supported");
            }
            PollingWatchKey watchKey;
            synchronized (closeLock()) {
                if (isOpen()) {
                    synchronized (this.map) {
                        watchKey = (PollingWatchKey) this.map.get(fileKey);
                        if (watchKey == null) {
                            watchKey = new PollingWatchKey(path, this, fileKey);
                            this.map.put(fileKey, watchKey);
                        } else {
                            watchKey.disable();
                        }
                    }
                    watchKey.enable(events, (long) sensivity.sensitivityValueInSeconds());
                } else {
                    throw new ClosedWatchServiceException();
                }
            }
            return watchKey;
        }
        throw new NotDirectoryException(path.toString());
    }

    void implClose() throws IOException {
        synchronized (this.map) {
            for (Entry<Object, PollingWatchKey> entry : this.map.entrySet()) {
                PollingWatchKey watchKey = (PollingWatchKey) entry.getValue();
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
