package com.android.org.conscrypt;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSession;

public final class FileClientSessionCache {
    public static final int MAX_SIZE = 12;
    static final Map<File, Impl> caches = new HashMap();
    /* access modifiers changed from: private */
    public static final Logger logger = Logger.getLogger(FileClientSessionCache.class.getName());

    static class CacheFile extends File {
        long lastModified = -1;
        final String name;

        CacheFile(File dir, String name2) {
            super(dir, name2);
            this.name = name2;
        }

        public long lastModified() {
            long lastModified2 = this.lastModified;
            if (lastModified2 != -1) {
                return lastModified2;
            }
            long lastModified3 = super.lastModified();
            this.lastModified = lastModified3;
            return lastModified3;
        }

        public int compareTo(File another) {
            long result = lastModified() - another.lastModified();
            if (result == 0) {
                return super.compareTo(another);
            }
            return result < 0 ? -1 : 1;
        }
    }

    static class Impl implements SSLClientSessionCache {
        Map<String, File> accessOrder = newAccessOrder();
        final File directory;
        String[] initialFiles;
        int size;

        Impl(File directory2) throws IOException {
            boolean exists = directory2.exists();
            if (!exists || directory2.isDirectory()) {
                if (exists) {
                    this.initialFiles = directory2.list();
                    if (this.initialFiles != null) {
                        Arrays.sort(this.initialFiles);
                        this.size = this.initialFiles.length;
                    } else {
                        throw new IOException(directory2 + " exists but cannot list contents.");
                    }
                } else if (directory2.mkdirs()) {
                    this.size = 0;
                } else {
                    throw new IOException("Creation of " + directory2 + " directory failed.");
                }
                this.directory = directory2;
                return;
            }
            throw new IOException(directory2 + " exists but is not a directory.");
        }

        private static Map<String, File> newAccessOrder() {
            return new LinkedHashMap(12, 0.75f, true);
        }

        private static String fileName(String host, int port) {
            if (host != null) {
                return host + "." + port;
            }
            throw new NullPointerException("host == null");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0062, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0063, code lost:
            logReadError(r8, r1, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0067, code lost:
            return null;
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        public synchronized byte[] getSessionData(String host, int port) {
            byte[] data;
            String name = fileName(host, port);
            File file = this.accessOrder.get(name);
            if (file == null) {
                if (this.initialFiles == null) {
                    return null;
                }
                if (Arrays.binarySearch(this.initialFiles, name) < 0) {
                    return null;
                }
                file = new File(this.directory, name);
                this.accessOrder.put(name, file);
            }
            FileInputStream in = new FileInputStream(file);
            try {
                data = new byte[((int) file.length())];
                new DataInputStream(in).readFully(data);
                try {
                    in.close();
                } catch (Exception e) {
                }
            } catch (IOException e2) {
                try {
                    logReadError(host, file, e2);
                    return null;
                } finally {
                    try {
                        in.close();
                    } catch (Exception e3) {
                    }
                }
            }
            return data;
        }

        static void logReadError(String host, File file, Throwable t) {
            Logger access$000 = FileClientSessionCache.logger;
            Level level = Level.WARNING;
            access$000.log(level, "FileClientSessionCache: Error reading session data for " + host + " from " + file + ".", t);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0046, code lost:
            r7 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0055, code lost:
            if (1 != 0) goto L_0x0057;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0057, code lost:
            if (0 == 0) goto L_0x0059;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x005a, code lost:
            r9.accessOrder.put(r1, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0060, code lost:
            delete(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0064, code lost:
            r7 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
            r8 = r9.accessOrder;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x00a5, code lost:
            r8.put(r1, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x00a9, code lost:
            delete(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x00ad, code lost:
            r7 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x00af, code lost:
            r8 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
            logWriteError(r0, r2, r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
            r8 = r9.accessOrder;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x00bb, code lost:
            throw r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x00bc, code lost:
            if (0 != 0) goto L_0x00be;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x00be, code lost:
            if (0 == 0) goto L_0x00c0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x00c1, code lost:
            r9.accessOrder.put(r1, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x00c7, code lost:
            delete(r2);
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [B:26:0x0049, B:41:0x0067] */
        public synchronized void putSessionData(SSLSession session, byte[] sessionData) {
            Map<String, File> map;
            Map<String, File> map2;
            String host = session.getPeerHost();
            if (sessionData != null) {
                String name = fileName(host, session.getPeerPort());
                File file = new File(this.directory, name);
                boolean existedBefore = file.exists();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (!existedBefore) {
                        this.size++;
                        makeRoom();
                    }
                    try {
                        out.write(sessionData);
                        try {
                            out.close();
                            if (!(1 == 0 || 1 == 0)) {
                                map2 = this.accessOrder;
                                map2.put(name, file);
                            }
                        } catch (IOException e) {
                            logWriteError(host, file, e);
                            if (!(1 == 0 || 0 == 0)) {
                                map2 = this.accessOrder;
                            }
                        }
                    } catch (IOException e2) {
                        logWriteError(host, file, e2);
                        try {
                            out.close();
                            if (!(0 == 0 || 1 == 0)) {
                                map = this.accessOrder;
                                map.put(name, file);
                            }
                        } catch (IOException e3) {
                            logWriteError(host, file, e3);
                            if (!(0 == 0 || 0 == 0)) {
                                map = this.accessOrder;
                                map.put(name, file);
                            }
                        } catch (Throwable th) {
                            th = th;
                            if (0 == 0 || 0 == 0) {
                                delete(file);
                                throw th;
                            } else {
                                this.accessOrder.put(name, file);
                                throw th;
                            }
                        }
                    }
                    delete(file);
                } catch (FileNotFoundException e4) {
                    logWriteError(host, file, e4);
                }
            } else {
                throw new NullPointerException("sessionData == null");
            }
        }

        private void makeRoom() {
            if (this.size > 12) {
                indexFiles();
                int removals = this.size - 12;
                Iterator<File> i = this.accessOrder.values().iterator();
                do {
                    delete(i.next());
                    i.remove();
                    removals--;
                } while (removals > 0);
            }
        }

        private void indexFiles() {
            String[] initialFiles2 = this.initialFiles;
            if (initialFiles2 != null) {
                this.initialFiles = null;
                Set<CacheFile> diskOnly = new TreeSet<>();
                for (String name : initialFiles2) {
                    if (!this.accessOrder.containsKey(name)) {
                        diskOnly.add(new CacheFile(this.directory, name));
                    }
                }
                if (!diskOnly.isEmpty()) {
                    Map<String, File> newOrder = newAccessOrder();
                    for (CacheFile cacheFile : diskOnly) {
                        newOrder.put(cacheFile.name, cacheFile);
                    }
                    newOrder.putAll(this.accessOrder);
                    this.accessOrder = newOrder;
                }
            }
        }

        private void delete(File file) {
            if (!file.delete()) {
                Exception e = new IOException("FileClientSessionCache: Failed to delete " + file + ".");
                FileClientSessionCache.logger.log(Level.WARNING, e.getMessage(), e);
            }
            this.size--;
        }

        static void logWriteError(String host, File file, Throwable t) {
            Logger access$000 = FileClientSessionCache.logger;
            Level level = Level.WARNING;
            access$000.log(level, "FileClientSessionCache: Error writing session data for " + host + " to " + file + ".", t);
        }
    }

    private FileClientSessionCache() {
    }

    public static synchronized SSLClientSessionCache usingDirectory(File directory) throws IOException {
        Impl cache;
        synchronized (FileClientSessionCache.class) {
            cache = caches.get(directory);
            if (cache == null) {
                cache = new Impl(directory);
                caches.put(directory, cache);
            }
        }
        return cache;
    }

    static synchronized void reset() {
        synchronized (FileClientSessionCache.class) {
            caches.clear();
        }
    }
}
