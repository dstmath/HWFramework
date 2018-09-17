package com.android.server.devicepolicy;

import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SecurityLogMonitor implements Runnable {
    private static final long BROADCAST_RETRY_INTERVAL_MILLISECONDS = TimeUnit.MINUTES.toMillis(30);
    private static final int BUFFER_ENTRIES_MAXIMUM_LEVEL = 10240;
    private static final int BUFFER_ENTRIES_NOTIFICATION_LEVEL = 1024;
    private static final boolean DEBUG = false;
    private static final long OVERLAP_NANOS = TimeUnit.SECONDS.toNanos(3);
    private static final long POLLING_INTERVAL_MILLISECONDS = TimeUnit.MINUTES.toMillis(1);
    private static final long RATE_LIMIT_INTERVAL_MILLISECONDS = TimeUnit.HOURS.toMillis(2);
    private static final String TAG = "SecurityLogMonitor";
    @GuardedBy("mLock")
    private boolean mAllowedToRetrieve = false;
    private long mLastEventNanos = -1;
    private final ArrayList<SecurityEvent> mLastEvents = new ArrayList();
    private final Lock mLock = new ReentrantLock();
    @GuardedBy("mLock")
    private Thread mMonitorThread = null;
    @GuardedBy("mLock")
    private long mNextAllowedRetrievalTimeMillis = -1;
    @GuardedBy("mLock")
    private boolean mPaused = false;
    @GuardedBy("mLock")
    private ArrayList<SecurityEvent> mPendingLogs = new ArrayList();
    private final DevicePolicyManagerService mService;

    SecurityLogMonitor(DevicePolicyManagerService service) {
        this.mService = service;
    }

    void start() {
        Slog.i(TAG, "Starting security logging.");
        this.mLock.lock();
        try {
            if (this.mMonitorThread == null) {
                this.mPendingLogs = new ArrayList();
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrievalTimeMillis = -1;
                this.mPaused = false;
                this.mMonitorThread = new Thread(this);
                this.mMonitorThread.start();
            }
            this.mLock.unlock();
        } catch (Throwable th) {
            this.mLock.unlock();
        }
    }

    void stop() {
        Slog.i(TAG, "Stopping security logging.");
        this.mLock.lock();
        try {
            if (this.mMonitorThread != null) {
                this.mMonitorThread.interrupt();
                this.mMonitorThread.join(TimeUnit.SECONDS.toMillis(5));
                this.mPendingLogs = new ArrayList();
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrievalTimeMillis = -1;
                this.mPaused = false;
                this.mMonitorThread = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for thread to stop", e);
        } catch (Throwable th) {
            this.mLock.unlock();
        }
        this.mLock.unlock();
    }

    void pause() {
        Slog.i(TAG, "Paused.");
        this.mLock.lock();
        this.mPaused = true;
        this.mAllowedToRetrieve = false;
        this.mLock.unlock();
    }

    void resume() {
        this.mLock.lock();
        try {
            if (this.mPaused) {
                this.mPaused = false;
                this.mAllowedToRetrieve = false;
                this.mLock.unlock();
                Slog.i(TAG, "Resumed.");
                try {
                    notifyDeviceOwnerIfNeeded();
                } catch (InterruptedException e) {
                    Log.w(TAG, "Thread interrupted.", e);
                }
                return;
            }
            Log.d(TAG, "Attempted to resume, but logging is not paused.");
        } finally {
            this.mLock.unlock();
        }
    }

    void discardLogs() {
        this.mLock.lock();
        this.mAllowedToRetrieve = false;
        this.mPendingLogs = new ArrayList();
        this.mLock.unlock();
        Slog.i(TAG, "Discarded all logs.");
    }

    List<SecurityEvent> retrieveLogs() {
        this.mLock.lock();
        try {
            if (this.mAllowedToRetrieve) {
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrievalTimeMillis = SystemClock.elapsedRealtime() + RATE_LIMIT_INTERVAL_MILLISECONDS;
                List<SecurityEvent> result = this.mPendingLogs;
                this.mPendingLogs = new ArrayList();
                return result;
            }
            this.mLock.unlock();
            return null;
        } finally {
            this.mLock.unlock();
        }
    }

    private void getNextBatch(ArrayList<SecurityEvent> newLogs) throws IOException, InterruptedException {
        if (this.mLastEventNanos < 0) {
            SecurityLog.readEvents(newLogs);
        } else {
            SecurityLog.readEventsSince(this.mLastEvents.isEmpty() ? this.mLastEventNanos : Math.max(0, this.mLastEventNanos - OVERLAP_NANOS), newLogs);
        }
        for (int i = 0; i < newLogs.size() - 1; i++) {
            if (((SecurityEvent) newLogs.get(i)).getTimeNanos() > ((SecurityEvent) newLogs.get(i + 1)).getTimeNanos()) {
                newLogs.sort(new -$Lambda$yPMQJaI1L2rJhTx00Ubn7ktEjSE());
                return;
            }
        }
    }

    private void saveLastEvents(ArrayList<SecurityEvent> newLogs) {
        this.mLastEvents.clear();
        if (!newLogs.isEmpty()) {
            this.mLastEventNanos = ((SecurityEvent) newLogs.get(newLogs.size() - 1)).getTimeNanos();
            int pos = newLogs.size() - 2;
            while (pos >= 0 && this.mLastEventNanos - ((SecurityEvent) newLogs.get(pos)).getTimeNanos() < OVERLAP_NANOS) {
                pos--;
            }
            this.mLastEvents.addAll(newLogs.subList(pos + 1, newLogs.size()));
        }
    }

    @GuardedBy("mLock")
    private void mergeBatchLocked(ArrayList<SecurityEvent> newLogs) {
        this.mPendingLogs.ensureCapacity(this.mPendingLogs.size() + newLogs.size());
        int curPos = 0;
        int lastPos = 0;
        while (lastPos < this.mLastEvents.size() && curPos < newLogs.size()) {
            SecurityEvent curEvent = (SecurityEvent) newLogs.get(curPos);
            long currentNanos = curEvent.getTimeNanos();
            if (currentNanos > this.mLastEventNanos) {
                break;
            }
            SecurityEvent lastEvent = (SecurityEvent) this.mLastEvents.get(lastPos);
            long lastNanos = lastEvent.getTimeNanos();
            if (lastNanos > currentNanos) {
                this.mPendingLogs.add(curEvent);
                curPos++;
            } else if (lastNanos < currentNanos) {
                lastPos++;
            } else {
                if (!lastEvent.equals(curEvent)) {
                    this.mPendingLogs.add(curEvent);
                }
                lastPos++;
                curPos++;
            }
        }
        this.mPendingLogs.addAll(newLogs.subList(curPos, newLogs.size()));
        if (this.mPendingLogs.size() > BUFFER_ENTRIES_MAXIMUM_LEVEL) {
            this.mPendingLogs = new ArrayList(this.mPendingLogs.subList(this.mPendingLogs.size() - 5120, this.mPendingLogs.size()));
            Slog.i(TAG, "Pending logs buffer full. Discarding old logs.");
        }
    }

    public void run() {
        Process.setThreadPriority(10);
        ArrayList<SecurityEvent> newLogs = new ArrayList();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(POLLING_INTERVAL_MILLISECONDS);
                getNextBatch(newLogs);
                this.mLock.lockInterruptibly();
                mergeBatchLocked(newLogs);
                this.mLock.unlock();
                saveLastEvents(newLogs);
                newLogs.clear();
                notifyDeviceOwnerIfNeeded();
            } catch (IOException e) {
                Log.e(TAG, "Failed to read security log", e);
            } catch (InterruptedException e2) {
                Log.i(TAG, "Thread interrupted, exiting.", e2);
            } catch (Throwable th) {
                this.mLock.unlock();
            }
        }
        this.mLastEvents.clear();
        if (this.mLastEventNanos != -1) {
            this.mLastEventNanos++;
        }
        Slog.i(TAG, "MonitorThread exit.");
    }

    private void notifyDeviceOwnerIfNeeded() throws InterruptedException {
        boolean allowRetrievalAndNotifyDO = false;
        this.mLock.lockInterruptibly();
        try {
            if (!this.mPaused) {
                int logSize = this.mPendingLogs.size();
                if (logSize >= 1024 && !this.mAllowedToRetrieve) {
                    allowRetrievalAndNotifyDO = true;
                }
                if (logSize > 0 && SystemClock.elapsedRealtime() >= this.mNextAllowedRetrievalTimeMillis) {
                    allowRetrievalAndNotifyDO = true;
                }
                if (allowRetrievalAndNotifyDO) {
                    this.mAllowedToRetrieve = true;
                    this.mNextAllowedRetrievalTimeMillis = SystemClock.elapsedRealtime() + BROADCAST_RETRY_INTERVAL_MILLISECONDS;
                }
                this.mLock.unlock();
                if (allowRetrievalAndNotifyDO) {
                    Slog.i(TAG, "notify DO");
                    this.mService.sendDeviceOwnerCommand("android.app.action.SECURITY_LOGS_AVAILABLE", null);
                }
            }
        } finally {
            this.mLock.unlock();
        }
    }
}
