package android.net.http;

import android.os.Process;
import android.os.SystemClock;
import org.apache.http.HttpHost;

class IdleCache {
    private static final int CHECK_INTERVAL = 2000;
    private static final int EMPTY_CHECK_MAX = 5;
    private static final int IDLE_CACHE_MAX = 8;
    private static final int TIMEOUT = 6000;
    private int mCached;
    private int mCount;
    private Entry[] mEntries;
    private int mReused;
    private IdleReaper mThread;

    class Entry {
        Connection mConnection;
        HttpHost mHost;
        long mTimeout;

        Entry() {
        }
    }

    private class IdleReaper extends Thread {
        private IdleReaper() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            int check = 0;
            setName("IdleReaper");
            Process.setThreadPriority(10);
            synchronized (IdleCache.this) {
                while (check < IdleCache.EMPTY_CHECK_MAX) {
                    try {
                        IdleCache.this.wait(2000);
                    } catch (InterruptedException e) {
                    }
                    if (IdleCache.this.mCount == 0) {
                        check++;
                    } else {
                        check = 0;
                        IdleCache.this.clearIdle();
                    }
                }
                IdleCache.this.mThread = null;
            }
        }
    }

    IdleCache() {
        this.mEntries = new Entry[IDLE_CACHE_MAX];
        this.mCount = 0;
        this.mThread = null;
        this.mCached = 0;
        this.mReused = 0;
        for (int i = 0; i < IDLE_CACHE_MAX; i++) {
            this.mEntries[i] = new Entry();
        }
    }

    synchronized boolean cacheConnection(HttpHost host, Connection connection) {
        boolean ret;
        ret = false;
        if (this.mCount < IDLE_CACHE_MAX) {
            long time = SystemClock.uptimeMillis();
            int i = 0;
            while (i < IDLE_CACHE_MAX) {
                Entry entry = this.mEntries[i];
                if (entry.mHost == null) {
                    entry.mHost = host;
                    entry.mConnection = connection;
                    entry.mTimeout = 6000 + time;
                    this.mCount++;
                    ret = true;
                    if (this.mThread == null) {
                        this.mThread = new IdleReaper();
                        this.mThread.start();
                    }
                } else {
                    i++;
                }
            }
        }
        return ret;
    }

    synchronized Connection getConnection(HttpHost host) {
        Connection ret;
        ret = null;
        if (this.mCount > 0) {
            for (int i = 0; i < IDLE_CACHE_MAX; i++) {
                Entry entry = this.mEntries[i];
                HttpHost eHost = entry.mHost;
                if (eHost != null && eHost.equals(host)) {
                    ret = entry.mConnection;
                    entry.mHost = null;
                    entry.mConnection = null;
                    this.mCount--;
                    break;
                }
            }
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void clear() {
        int i = 0;
        while (true) {
            if (this.mCount > 0 && i < IDLE_CACHE_MAX) {
                Entry entry = this.mEntries[i];
                if (entry.mHost != null) {
                    entry.mHost = null;
                    entry.mConnection.closeConnection();
                    entry.mConnection = null;
                    this.mCount--;
                }
                i++;
            }
        }
    }

    private synchronized void clearIdle() {
        if (this.mCount > 0) {
            long time = SystemClock.uptimeMillis();
            for (int i = 0; i < IDLE_CACHE_MAX; i++) {
                Entry entry = this.mEntries[i];
                if (entry.mHost != null && time > entry.mTimeout) {
                    entry.mHost = null;
                    entry.mConnection.closeConnection();
                    entry.mConnection = null;
                    this.mCount--;
                }
            }
        }
    }
}
