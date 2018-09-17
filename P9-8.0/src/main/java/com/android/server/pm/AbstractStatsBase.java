package com.android.server.pm;

import android.os.Environment;
import android.os.SystemClock;
import android.util.AtomicFile;
import com.android.server.am.HwBroadcastRadarUtil;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractStatsBase<T> {
    private static final int WRITE_INTERVAL_MS = 1800000;
    private final String mBackgroundThreadName;
    private final AtomicBoolean mBackgroundWriteRunning = new AtomicBoolean(false);
    private final Object mFileLock = new Object();
    private final String mFileName;
    private final AtomicLong mLastTimeWritten = new AtomicLong(0);
    private final boolean mLock;

    protected abstract void readInternal(T t);

    protected abstract void writeInternal(T t);

    protected AbstractStatsBase(String fileName, String threadName, boolean lock) {
        this.mFileName = fileName;
        this.mBackgroundThreadName = threadName;
        this.mLock = lock;
    }

    protected AtomicFile getFile() {
        return new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), this.mFileName));
    }

    protected void writeNow(T data) {
        writeImpl(data);
        this.mLastTimeWritten.set(SystemClock.elapsedRealtime());
    }

    protected boolean maybeWriteAsync(final T data) {
        if (SystemClock.elapsedRealtime() - this.mLastTimeWritten.get() < HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME || !this.mBackgroundWriteRunning.compareAndSet(false, true)) {
            return false;
        }
        new Thread(this.mBackgroundThreadName) {
            public void run() {
                try {
                    AbstractStatsBase.this.writeImpl(data);
                    AbstractStatsBase.this.mLastTimeWritten.set(SystemClock.elapsedRealtime());
                } finally {
                    AbstractStatsBase.this.mBackgroundWriteRunning.set(false);
                }
            }
        }.start();
        return true;
    }

    private void writeImpl(T data) {
        if (this.mLock) {
            synchronized (data) {
                synchronized (this.mFileLock) {
                    writeInternal(data);
                }
            }
            return;
        }
        synchronized (this.mFileLock) {
            writeInternal(data);
        }
    }

    protected void read(T data) {
        if (this.mLock) {
            synchronized (data) {
                synchronized (this.mFileLock) {
                    readInternal(data);
                }
            }
        } else {
            synchronized (this.mFileLock) {
                readInternal(data);
            }
        }
        this.mLastTimeWritten.set(SystemClock.elapsedRealtime());
    }
}
