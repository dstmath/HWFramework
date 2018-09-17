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
import javax.net.ssl.SSLSession;

public class FileClientSessionCache {
    public static final int MAX_SIZE = 12;
    static final Map<File, Impl> caches = new HashMap();

    static class CacheFile extends File {
        long lastModified = -1;
        final String name;

        CacheFile(File dir, String name) {
            super(dir, name);
            this.name = name;
        }

        public long lastModified() {
            long lastModified = this.lastModified;
            if (lastModified != -1) {
                return lastModified;
            }
            lastModified = super.lastModified();
            this.lastModified = lastModified;
            return lastModified;
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

        Impl(File directory) throws IOException {
            boolean exists = directory.exists();
            if (!exists || (directory.isDirectory() ^ 1) == 0) {
                if (exists) {
                    this.initialFiles = directory.list();
                    if (this.initialFiles == null) {
                        throw new IOException(directory + " exists but cannot list contents.");
                    }
                    Arrays.sort(this.initialFiles);
                    this.size = this.initialFiles.length;
                } else if (directory.mkdirs()) {
                    this.size = 0;
                } else {
                    throw new IOException("Creation of " + directory + " directory failed.");
                }
                this.directory = directory;
                return;
            }
            throw new IOException(directory + " exists but is not a directory.");
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

        public synchronized byte[] getSessionData(String host, int port) {
            byte[] data;
            String name = fileName(host, port);
            File file = (File) this.accessOrder.get(name);
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
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    data = new byte[((int) file.length())];
                    new DataInputStream(in).readFully(data);
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    }
                } catch (IOException e2) {
                    logReadError(host, file, e2);
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e3) {
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e4) {
                        }
                    }
                }
            } catch (FileNotFoundException e5) {
                logReadError(host, file, e5);
                return null;
            }
            return data;
        }

        static void logReadError(String host, File file, Throwable t) {
            System.err.println("FileClientSessionCache: Error reading session data for " + host + " from " + file + ".");
            t.printStackTrace();
        }

        /* JADX WARNING: Removed duplicated region for block: B:59:0x0094 A:{ExcHandler: java.io.IOException (r2_3 'e' java.io.IOException), Splitter: B:52:0x0084} */
        /* JADX WARNING: Missing block: B:53:?, code:
            r7.close();
     */
        /* JADX WARNING: Missing block: B:55:0x0088, code:
            if (r8 == false) goto L_0x0090;
     */
        /* JADX WARNING: Missing block: B:57:?, code:
            r11.accessOrder.put(r6, r4);
     */
        /* JADX WARNING: Missing block: B:59:0x0094, code:
            r2 = move-exception;
     */
        /* JADX WARNING: Missing block: B:61:?, code:
            logWriteError(r5, r4, r2);
     */
        /* JADX WARNING: Missing block: B:64:0x009d, code:
            delete(r4);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void putSessionData(SSLSession session, byte[] sessionData) {
            File file;
            String host = session.getPeerHost();
            if (sessionData == null) {
                throw new NullPointerException("sessionData == null");
            }
            String name = fileName(host, session.getPeerPort());
            file = new File(this.directory, name);
            boolean existedBefore = file.exists();
            try {
                FileOutputStream out = new FileOutputStream(file);
                if (!existedBefore) {
                    this.size++;
                    makeRoom();
                }
                boolean writeSuccessful = false;
                try {
                    out.write(sessionData);
                    writeSuccessful = true;
                    try {
                        out.close();
                        if (writeSuccessful) {
                            this.accessOrder.put(name, file);
                        }
                    } catch (IOException e) {
                        logWriteError(host, file, e);
                    } finally {
                    }
                } catch (IOException e2) {
                    logWriteError(host, file, e2);
                    try {
                        out.close();
                        if (writeSuccessful) {
                            this.accessOrder.put(name, file);
                        }
                    } catch (IOException e22) {
                        logWriteError(host, file, e22);
                    } finally {
                    }
                    return;
                } finally {
                }
            } catch (FileNotFoundException e3) {
                logWriteError(host, file, e3);
                return;
            }
            return;
            delete(file);
            delete(file);
        }

        private void makeRoom() {
            if (this.size > 12) {
                indexFiles();
                int removals = this.size - 12;
                Iterator<File> i = this.accessOrder.values().iterator();
                do {
                    delete((File) i.next());
                    i.remove();
                    removals--;
                } while (removals > 0);
            }
        }

        private void indexFiles() {
            String[] initialFiles = this.initialFiles;
            if (initialFiles != null) {
                this.initialFiles = null;
                Set<org.conscrypt.FileClientSessionCache.CacheFile> diskOnly = new TreeSet();
                for (String name : initialFiles) {
                    if (!this.accessOrder.containsKey(name)) {
                        diskOnly.add(new CacheFile(this.directory, name));
                    }
                }
                if (!diskOnly.isEmpty()) {
                    Map<String, File> newOrder = newAccessOrder();
                    Iterator cacheFile$iterator = diskOnly.iterator();
                    while (cacheFile$iterator.hasNext()) {
                        CacheFile cacheFile = (CacheFile) cacheFile$iterator.next();
                        newOrder.put(cacheFile.name, cacheFile);
                    }
                    newOrder.putAll(this.accessOrder);
                    this.accessOrder = newOrder;
                }
            }
        }

        private void delete(File file) {
            if (!file.delete()) {
                new IOException("FileClientSessionCache: Failed to delete " + file + ".").printStackTrace();
            }
            this.size--;
        }

        static void logWriteError(String host, File file, Throwable t) {
            System.err.println("FileClientSessionCache: Error writing session data for " + host + " to " + file + ".");
            t.printStackTrace();
        }
    }

    private FileClientSessionCache() {
    }

    public static synchronized SSLClientSessionCache usingDirectory(File directory) throws IOException {
        Impl cache;
        synchronized (FileClientSessionCache.class) {
            cache = (Impl) caches.get(directory);
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
