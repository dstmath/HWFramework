package com.android.server.devicepolicy;

import android.app.admin.SecurityLog;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* access modifiers changed from: package-private */
public class SecurityLogMonitor implements Runnable {
    private static final long BROADCAST_RETRY_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30);
    private static final int BUFFER_ENTRIES_CRITICAL_LEVEL = 9216;
    private static final int BUFFER_ENTRIES_MAXIMUM_LEVEL = 10240;
    @VisibleForTesting
    static final int BUFFER_ENTRIES_NOTIFICATION_LEVEL = 1024;
    private static final boolean DEBUG = false;
    private static final long FORCE_FETCH_THROTTLE_NS = TimeUnit.SECONDS.toNanos(10);
    private static final int HALF = 2;
    private static final String MDPP_TAG = "MDPPWriteEvent";
    private static final long OVERLAP_NS = TimeUnit.SECONDS.toNanos(3);
    private static final long POLLING_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long RATE_LIMIT_INTERVAL_MS = TimeUnit.HOURS.toMillis(2);
    private static final String TAG = "SecurityLogMonitor";
    @GuardedBy({"mLock"})
    private boolean mAllowedToRetrieve;
    @GuardedBy({"mLock"})
    private boolean mCriticalLevelLogged;
    private final Semaphore mForceSemaphore;
    @GuardedBy({"mLock"})
    private long mId;
    private long mLastEventNanos;
    private final ArrayList<SecurityLog.SecurityEvent> mLastEvents;
    @GuardedBy({"mForceSemaphore"})
    private long mLastForceNanos;
    private final Lock mLock;
    @GuardedBy({"mLock"})
    private Thread mMonitorThread;
    @GuardedBy({"mLock"})
    private long mNextAllowedRetrievalTimeMillis;
    @GuardedBy({"mLock"})
    private boolean mPaused;
    @GuardedBy({"mLock"})
    private ArrayList<SecurityLog.SecurityEvent> mPendingLogs;
    private SecurityLogSaver mSaver;
    private final DevicePolicyManagerService mService;

    SecurityLogMonitor(DevicePolicyManagerService service) {
        this(service, 0);
    }

    @VisibleForTesting
    SecurityLogMonitor(DevicePolicyManagerService service, long id) {
        this.mLock = new ReentrantLock();
        this.mSaver = new SecurityLogSaver();
        this.mMonitorThread = null;
        this.mPendingLogs = new ArrayList<>();
        this.mAllowedToRetrieve = false;
        this.mCriticalLevelLogged = false;
        this.mLastEvents = new ArrayList<>();
        this.mLastEventNanos = -1;
        this.mNextAllowedRetrievalTimeMillis = -1;
        this.mPaused = false;
        this.mForceSemaphore = new Semaphore(0);
        this.mLastForceNanos = 0;
        this.mService = service;
        this.mId = id;
        this.mLastForceNanos = System.nanoTime();
    }

    /* access modifiers changed from: package-private */
    public void start() {
        Slog.i(TAG, "Starting security logging.");
        SecurityLog.writeEvent(210011, new Object[0]);
        Log.i(MDPP_TAG, "TAG 210011");
        this.mLock.lock();
        try {
            if (this.mMonitorThread == null) {
                this.mPendingLogs = new ArrayList<>();
                this.mCriticalLevelLogged = false;
                this.mId = 0;
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrievalTimeMillis = -1;
                this.mPaused = false;
                this.mMonitorThread = new Thread(this);
                this.mMonitorThread.start();
            }
        } finally {
            this.mLock.unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        Slog.i(TAG, "Stopping security logging.");
        SecurityLog.writeEvent(210012, new Object[0]);
        Log.i(MDPP_TAG, "TAG 210012");
        this.mLock.lock();
        try {
            if (this.mMonitorThread != null) {
                this.mMonitorThread.interrupt();
                try {
                    this.mMonitorThread.join(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting for thread to stop", e);
                }
                this.mPendingLogs = new ArrayList<>();
                this.mId = 0;
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrievalTimeMillis = -1;
                this.mPaused = false;
                this.mMonitorThread = null;
            }
        } finally {
            this.mLock.unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void pause() {
        Slog.i(TAG, "Paused.");
        this.mLock.lock();
        this.mPaused = true;
        this.mAllowedToRetrieve = false;
        this.mLock.unlock();
    }

    /* access modifiers changed from: package-private */
    public void resume() {
        this.mLock.lock();
        try {
            if (!this.mPaused) {
                Log.d(TAG, "Attempted to resume, but logging is not paused.");
                return;
            }
            this.mPaused = false;
            this.mAllowedToRetrieve = false;
            this.mLock.unlock();
            Slog.i(TAG, "Resumed.");
            try {
                notifyDeviceOwnerIfNeeded(false);
            } catch (InterruptedException e) {
                Log.w(TAG, "Thread interrupted.", e);
            }
        } finally {
            this.mLock.unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void discardLogs() {
        this.mLock.lock();
        this.mAllowedToRetrieve = false;
        this.mSaver.saveLogsToFileIfNeeded(this.mPendingLogs);
        this.mPendingLogs = new ArrayList<>(1024);
        this.mCriticalLevelLogged = false;
        this.mLock.unlock();
        Slog.i(TAG, "Discarded all logs.");
    }

    /* access modifiers changed from: package-private */
    public List<SecurityLog.SecurityEvent> retrieveLogs() {
        this.mLock.lock();
        try {
            if (this.mAllowedToRetrieve) {
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrievalTimeMillis = SystemClock.elapsedRealtime() + RATE_LIMIT_INTERVAL_MS;
                List<SecurityLog.SecurityEvent> result = this.mPendingLogs;
                this.mSaver.saveLogsToFileIfNeeded(this.mPendingLogs);
                this.mPendingLogs = new ArrayList<>();
                this.mCriticalLevelLogged = false;
                return result;
            }
            this.mLock.unlock();
            return null;
        } finally {
            this.mLock.unlock();
        }
    }

    private void getNextBatch(ArrayList<SecurityLog.SecurityEvent> newLogs) throws IOException {
        if (this.mLastEventNanos < 0) {
            SecurityLog.readEvents(newLogs);
        } else {
            SecurityLog.readEventsSince(this.mLastEvents.isEmpty() ? this.mLastEventNanos : Math.max(0L, this.mLastEventNanos - OVERLAP_NS), newLogs);
        }
        for (int i = 0; i < newLogs.size() - 1; i++) {
            if (newLogs.get(i).getTimeNanos() > newLogs.get(i + 1).getTimeNanos()) {
                newLogs.sort($$Lambda$SecurityLogMonitor$y5Q3dMmmJ8bk5nBh8WR2MUroKrI.INSTANCE);
                return;
            }
        }
    }

    private void saveLastEvents(ArrayList<SecurityLog.SecurityEvent> newLogs) {
        this.mLastEvents.clear();
        if (!newLogs.isEmpty()) {
            this.mLastEventNanos = newLogs.get(newLogs.size() - 1).getTimeNanos();
            int pos = newLogs.size() - 2;
            while (pos >= 0 && this.mLastEventNanos - newLogs.get(pos).getTimeNanos() < OVERLAP_NS) {
                pos--;
            }
            this.mLastEvents.addAll(newLogs.subList(pos + 1, newLogs.size()));
        }
    }

    @GuardedBy({"mLock"})
    private void mergeBatchLocked(ArrayList<SecurityLog.SecurityEvent> newLogs) {
        ArrayList<SecurityLog.SecurityEvent> arrayList = this.mPendingLogs;
        arrayList.ensureCapacity(arrayList.size() + newLogs.size());
        int curPos = 0;
        int lastPos = 0;
        while (lastPos < this.mLastEvents.size() && curPos < newLogs.size()) {
            SecurityLog.SecurityEvent curEvent = newLogs.get(curPos);
            long currentNanos = curEvent.getTimeNanos();
            if (currentNanos > this.mLastEventNanos) {
                break;
            }
            SecurityLog.SecurityEvent lastEvent = this.mLastEvents.get(lastPos);
            long lastNanos = lastEvent.getTimeNanos();
            if (lastNanos > currentNanos) {
                assignLogId(curEvent);
                this.mPendingLogs.add(curEvent);
                curPos++;
            } else if (lastNanos < currentNanos) {
                lastPos++;
            } else {
                if (!lastEvent.eventEquals(curEvent)) {
                    assignLogId(curEvent);
                    this.mPendingLogs.add(curEvent);
                }
                lastPos++;
                curPos++;
            }
        }
        List<SecurityLog.SecurityEvent> idLogs = newLogs.subList(curPos, newLogs.size());
        for (SecurityLog.SecurityEvent event : idLogs) {
            assignLogId(event);
        }
        this.mPendingLogs.addAll(idLogs);
        checkCriticalLevel();
        if (this.mPendingLogs.size() > BUFFER_ENTRIES_MAXIMUM_LEVEL) {
            SecurityLogSaver securityLogSaver = this.mSaver;
            ArrayList<SecurityLog.SecurityEvent> arrayList2 = this.mPendingLogs;
            securityLogSaver.saveLogsToFileIfNeeded(new ArrayList<>(arrayList2.subList(0, arrayList2.size() - 5120)));
            ArrayList<SecurityLog.SecurityEvent> arrayList3 = this.mPendingLogs;
            this.mPendingLogs = new ArrayList<>(arrayList3.subList(arrayList3.size() - 5120, this.mPendingLogs.size()));
            this.mCriticalLevelLogged = false;
            Slog.i(TAG, "Pending logs buffer full. Discarding old logs.");
        }
    }

    @GuardedBy({"mLock"})
    private void checkCriticalLevel() {
        if (SecurityLog.isLoggingEnabled() && this.mPendingLogs.size() >= BUFFER_ENTRIES_CRITICAL_LEVEL && !this.mCriticalLevelLogged) {
            this.mCriticalLevelLogged = true;
            SecurityLog.writeEvent(210015, new Object[0]);
            Log.i(MDPP_TAG, "TAG 210015");
        }
    }

    @GuardedBy({"mLock"})
    private void assignLogId(SecurityLog.SecurityEvent event) {
        event.setId(this.mId);
        long j = this.mId;
        if (j == JobStatus.NO_LATEST_RUNTIME) {
            Slog.i(TAG, "Reached maximum id value; wrapping around.");
            this.mId = 0;
            return;
        }
        this.mId = j + 1;
    }

    /* JADX INFO: finally extract failed */
    @Override // java.lang.Runnable
    public void run() {
        Process.setThreadPriority(10);
        ArrayList<SecurityLog.SecurityEvent> newLogs = new ArrayList<>();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                boolean force = this.mForceSemaphore.tryAcquire(POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
                getNextBatch(newLogs);
                this.mLock.lockInterruptibly();
                try {
                    mergeBatchLocked(newLogs);
                    this.mLock.unlock();
                    saveLastEvents(newLogs);
                    newLogs.clear();
                    notifyDeviceOwnerIfNeeded(force);
                } catch (Throwable th) {
                    this.mLock.unlock();
                    throw th;
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to read security log", e);
            } catch (InterruptedException e2) {
                Log.i(TAG, "Thread interrupted, exiting.", e2);
            }
        }
        this.mLastEvents.clear();
        long j = this.mLastEventNanos;
        if (j != -1) {
            this.mLastEventNanos = j + 1;
        }
        this.mSaver.saveLogsToFileIfNeeded(this.mPendingLogs);
        Slog.i(TAG, "MonitorThread exit.");
    }

    private void notifyDeviceOwnerIfNeeded(boolean force) throws InterruptedException {
        boolean allowRetrievalAndNotifyDO = false;
        this.mLock.lockInterruptibly();
        try {
            if (!this.mPaused) {
                int logSize = this.mPendingLogs.size();
                if ((logSize >= 1024 || (force && logSize > 0)) && !this.mAllowedToRetrieve) {
                    allowRetrievalAndNotifyDO = true;
                }
                if (logSize > 0 && SystemClock.elapsedRealtime() >= this.mNextAllowedRetrievalTimeMillis) {
                    allowRetrievalAndNotifyDO = true;
                }
                if (allowRetrievalAndNotifyDO) {
                    this.mAllowedToRetrieve = true;
                    this.mNextAllowedRetrievalTimeMillis = SystemClock.elapsedRealtime() + BROADCAST_RETRY_INTERVAL_MS;
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

    public long forceLogs() {
        long nowNanos = System.nanoTime();
        synchronized (this.mForceSemaphore) {
            long toWaitNanos = (this.mLastForceNanos + FORCE_FETCH_THROTTLE_NS) - nowNanos;
            if (toWaitNanos > 0) {
                return TimeUnit.NANOSECONDS.toMillis(toWaitNanos) + 1;
            }
            this.mLastForceNanos = nowNanos;
            if (this.mForceSemaphore.availablePermits() == 0) {
                this.mForceSemaphore.release();
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public static class SecurityLogSaver {
        private static final int INIT_BATCH_LOGS_SIZE = 102400;
        private static final int INIT_SINGLE_LOGENTRY_SIZE = 100;
        private static final int MAX_FILE_NUM = 5;
        private static final int MAX_FILE_SIZE = 1048576;
        private static final String RO_BOOT_CC_MODE_PROP = "ro.boot.sys.ccmode";
        private static final String SECURITY_LOG_DIR_PATH = "/data/system/admin/auditlog/";
        private int mCurrentFileIndex = -1;

        SecurityLogSaver() {
            File secureLogDir = new File(SECURITY_LOG_DIR_PATH);
            if (!secureLogDir.exists() || !secureLogDir.isDirectory()) {
                if (!(secureLogDir.mkdirs() || secureLogDir.isDirectory())) {
                    Log.e(SecurityLogMonitor.TAG, "Make dir for audit log save failed.");
                    return;
                }
            }
            for (int i = 0; i < 5; i++) {
                try {
                    new File("/data/system/admin/auditlog/audit.log." + i).createNewFile();
                } catch (IOException e) {
                    Log.e(SecurityLogMonitor.TAG, "Create file failed.");
                    return;
                }
            }
            File[] allFiles = secureLogDir.listFiles();
            if (allFiles == null) {
                Log.e(SecurityLogMonitor.TAG, "Fail to refactor log files with empty dir ");
                return;
            }
            int validFileCount = 0;
            for (File file : allFiles) {
                if (file.isDirectory() || !file.getName().startsWith("audit.log.")) {
                    Log.w(SecurityLogMonitor.TAG, "Delete file with abnormal file name with res:" + file.delete());
                }
                validFileCount++;
            }
            if (validFileCount != 5) {
                Log.w(SecurityLogMonitor.TAG, "Valid log files number is not correct");
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:0x00de A[SYNTHETIC, Splitter:B:51:0x00de] */
        public synchronized void saveLogsToFileIfNeeded(ArrayList<SecurityLog.SecurityEvent> pendingLogs) {
            Writer writer;
            Throwable th;
            String str;
            String str2;
            long fileLength;
            if (isCCMode() && pendingLogs != null) {
                if (pendingLogs.size() != 0) {
                    Log.i(SecurityLogMonitor.TAG, "Start to save security log");
                    Writer writer2 = null;
                    try {
                        File file = findAvailableFiles();
                        long j = 1048576;
                        boolean isAppend = file.length() <= 1048576;
                        if (isAppend) {
                            try {
                                fileLength = file.length();
                            } catch (IOException e) {
                            }
                        } else {
                            fileLength = 0;
                        }
                        Writer writer3 = new FileWriter(file, isAppend);
                        StringBuffer sb = new StringBuffer((int) INIT_BATCH_LOGS_SIZE);
                        Iterator<SecurityLog.SecurityEvent> it = pendingLogs.iterator();
                        while (it.hasNext()) {
                            sb.append(toString(it.next()));
                            sb.append(System.lineSeparator());
                            if (((long) sb.length()) + fileLength > j) {
                                writer3.write(sb.toString());
                                file.setLastModified(System.currentTimeMillis());
                                sb.setLength(0);
                                writer3.close();
                                Log.i(SecurityLogMonitor.TAG, "Log File is full,find next available file");
                                file = findAvailableFiles();
                                writer3 = new FileWriter(file, false);
                                fileLength = 0;
                            }
                            j = 1048576;
                        }
                        writer3.write(sb.toString());
                        file.setLastModified(System.currentTimeMillis());
                        try {
                            writer3.close();
                        } catch (IOException e2) {
                            str = SecurityLogMonitor.TAG;
                            str2 = "Save security log failed in finally close IOException";
                        }
                    } catch (IOException e3) {
                        try {
                            Log.e(SecurityLogMonitor.TAG, "Save security log failed due to IOException");
                            if (0 != 0) {
                                try {
                                    writer2.close();
                                } catch (IOException e4) {
                                    str = SecurityLogMonitor.TAG;
                                    str2 = "Save security log failed in finally close IOException";
                                }
                            }
                        } catch (Throwable th2) {
                            writer = null;
                            th = th2;
                            if (writer != null) {
                                try {
                                    writer.close();
                                } catch (IOException e5) {
                                    Log.e(SecurityLogMonitor.TAG, "Save security log failed in finally close IOException");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        writer = null;
                        th = th3;
                        if (writer != null) {
                        }
                        throw th;
                    }
                }
            }
            return;
            Log.e(str, str2);
        }

        private File findAvailableFiles() throws IOException {
            File target = null;
            File[] allFiles = new File(SECURITY_LOG_DIR_PATH).listFiles();
            if (allFiles == null || allFiles.length == 0) {
                Log.e(SecurityLogMonitor.TAG, "No available log files.");
                throw new IOException("No available log files.");
            }
            int i = this.mCurrentFileIndex;
            if (i == -1) {
                int length = allFiles.length;
                int index = 0;
                int index2 = 0;
                while (true) {
                    if (index2 >= length) {
                        break;
                    } else if (allFiles[index2].length() < 1048576) {
                        this.mCurrentFileIndex = index;
                        target = allFiles[this.mCurrentFileIndex];
                        break;
                    } else {
                        index++;
                        index2++;
                    }
                }
                if (this.mCurrentFileIndex != -1) {
                    return target;
                }
                Arrays.sort(allFiles, new Comparator<File>() {
                    /* class com.android.server.devicepolicy.SecurityLogMonitor.SecurityLogSaver.AnonymousClass1 */

                    public int compare(File lhs, File rhs) {
                        long diff = lhs.lastModified() - rhs.lastModified();
                        if (diff > 0) {
                            return 1;
                        }
                        if (diff < 0) {
                            return -1;
                        }
                        return 0;
                    }
                });
                return allFiles[0];
            }
            if (allFiles[i].length() > 1048576) {
                this.mCurrentFileIndex++;
                this.mCurrentFileIndex %= 5;
            }
            return allFiles[this.mCurrentFileIndex];
        }

        private boolean isCCMode() {
            if (HuaweiTelephonyConfigs.isHisiPlatform()) {
                return "enable".equals(SystemProperties.get(RO_BOOT_CC_MODE_PROP, "disable"));
            }
            HwLog.i(SecurityLogMonitor.TAG, "current platform unsupport cc mode");
            return false;
        }

        private String toString(SecurityLog.SecurityEvent event) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(getStringEventTagFromId(event.getTag()));
            if (Build.VERSION.SDK_INT >= 28) {
                sb.append(" (id: " + event.getId() + ")");
            }
            sb.append(" (");
            sb.append(new Date(TimeUnit.NANOSECONDS.toMillis(event.getTimeNanos())));
            sb.append("): ");
            printData(sb, event.getData());
            return sb.toString();
        }

        private String getStringEventTagFromId(int eventId) {
            switch (eventId) {
                case 210001:
                    return "ADB_SHELL_INTERACTIVE";
                case 210002:
                    return "ADB_SHELL_CMD";
                case 210003:
                    return "SYNC_RECV_FILE";
                case 210004:
                    return "SYNC_SEND_FILE";
                case 210005:
                    return "APP_PROCESS_START";
                case 210006:
                    return "KEYGUARD_DISMISSED";
                case 210007:
                    return "KEYGUARD_DISMISS_AUTH_ATTEMPT";
                case 210008:
                    return "KEYGUARD_SECURED";
                case 210009:
                    return "OS_STARTUP";
                case 210010:
                    return "OS_SHUTDOWN";
                case 210011:
                    return "LOGGING_STARTED";
                case 210012:
                    return "LOGGING_STOPPED";
                case 210013:
                    return "MEDIA_MOUNTED";
                case 210014:
                    return "MEDIA_UNMOUNTED";
                case 210015:
                    return "LOG_BUFFER_SIZE_CRITICAL";
                case 210016:
                    return "PASSWORD_EXPIRATION_SET";
                case 210017:
                    return "PASSWORD_COMPLEXITY_SET";
                case 210018:
                    return "PASSWORD_HISTORY_LENGTH_SET";
                case 210019:
                    return "MAX_SCREEN_LOCK_TIMEOUT_SET";
                case 210020:
                    return "MAX_PASSWORD_ATTEMPTS_SET";
                case 210021:
                    return "KEYGUARD_DISABLED_FEATURES_SET";
                case 210022:
                    return "REMOTE_LOCK";
                case 210023:
                    return "WIPE_FAILED";
                case 210024:
                    return "KEY_GENERATED";
                case 210025:
                    return "KEY_IMPORTED";
                case 210026:
                    return "KEY_DESTROYED";
                case 210027:
                    return "USER_RESTRICTION_ADDED";
                case 210028:
                    return "USER_RESTRICTION_REMOVED";
                case 210029:
                    return "CERT_AUTHORITY_INSTALLED";
                case 210030:
                    return "CERT_AUTHORITY_REMOVED";
                case 210031:
                    return "CRYPTO_SELF_TEST_COMPLETED";
                case 210032:
                    return "KEY_INTEGRITY_VIOLATION";
                case 210033:
                    return "CERT_VALIDATION_FAILURE";
                case 210034:
                case 210035:
                default:
                    return "UNKNOWN(" + eventId + ")";
                case 210036:
                    return "APPPOLICY_UNINSTALL_ALLOWED";
                case 210037:
                    return "APPPOLICY_UNINSTALL_REJECT";
                case 210038:
                    return "SECURITY_CRYPTO_SELF_TEST_INIT";
            }
        }

        private void printData(StringBuilder sb, Object data) {
            if ((data instanceof Integer) || (data instanceof Long) || (data instanceof Float) || (data instanceof String)) {
                sb.append(data.toString());
                sb.append(" ");
            } else if (data instanceof Object[]) {
                for (Object item : (Object[]) data) {
                    printData(sb, item);
                }
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("printData unhandled type:");
                sb2.append(data != null ? data.getClass().getSimpleName() : "null");
                Log.e(SecurityLogMonitor.TAG, sb2.toString());
            }
        }
    }
}
