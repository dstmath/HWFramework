package com.android.server.content;

import android.accounts.Account;
import android.app.job.JobParameters;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IntPair;
import com.android.server.IoThread;
import com.android.server.content.SyncManager;
import com.android.server.content.SyncStorageEngine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;

public class SyncLogger {
    public static final int CALLING_UID_SELF = -1;
    private static final String TAG = "SyncLogger";
    private static SyncLogger sInstance;

    SyncLogger() {
    }

    public static synchronized SyncLogger getInstance() {
        SyncLogger syncLogger;
        synchronized (SyncLogger.class) {
            if (sInstance == null) {
                String flag = SystemProperties.get("debug.synclog");
                if ((Build.IS_DEBUGGABLE || "1".equals(flag) || Log.isLoggable(TAG, 2)) && !"0".equals(flag)) {
                    sInstance = new RotatingFileLogger();
                } else {
                    sInstance = new SyncLogger();
                }
            }
            syncLogger = sInstance;
        }
        return syncLogger;
    }

    public void log(Object... message) {
    }

    public void purgeOldLogs() {
    }

    public String jobParametersToString(JobParameters params) {
        return "";
    }

    public void dumpAll(PrintWriter pw) {
    }

    public boolean enabled() {
        return false;
    }

    /* access modifiers changed from: private */
    public static class RotatingFileLogger extends SyncLogger {
        private static final boolean DO_LOGCAT = Log.isLoggable(SyncLogger.TAG, 3);
        private static final SimpleDateFormat sFilenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private static final SimpleDateFormat sTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        @GuardedBy({"mLock"})
        private final Date mCachedDate = new Date();
        @GuardedBy({"mLock"})
        private long mCurrentLogFileDayTimestamp;
        @GuardedBy({"mLock"})
        private boolean mErrorShown;
        private final MyHandler mHandler = new MyHandler(IoThread.get().getLooper());
        private final long mKeepAgeMs = TimeUnit.DAYS.toMillis(7);
        private final Object mLock = new Object();
        private final File mLogPath = new File(Environment.getDataSystemDirectory(), "syncmanager-log");
        @GuardedBy({"mLock"})
        private Writer mLogWriter;
        @GuardedBy({"mLock"})
        private final StringBuilder mStringBuilder = new StringBuilder();

        RotatingFileLogger() {
        }

        @Override // com.android.server.content.SyncLogger
        public boolean enabled() {
            return true;
        }

        private void handleException(String message, Exception e) {
            if (!this.mErrorShown) {
                Slog.e(SyncLogger.TAG, message, e);
                this.mErrorShown = true;
            }
        }

        @Override // com.android.server.content.SyncLogger
        public void log(Object... message) {
            if (message != null) {
                this.mHandler.log(System.currentTimeMillis(), message);
            }
        }

        /* access modifiers changed from: package-private */
        public void logInner(long now, Object[] message) {
            synchronized (this.mLock) {
                openLogLocked(now);
                if (this.mLogWriter != null) {
                    this.mStringBuilder.setLength(0);
                    this.mCachedDate.setTime(now);
                    this.mStringBuilder.append(sTimestampFormat.format(this.mCachedDate));
                    this.mStringBuilder.append(' ');
                    this.mStringBuilder.append(Process.myTid());
                    this.mStringBuilder.append(' ');
                    int messageStart = this.mStringBuilder.length();
                    for (Object o : message) {
                        this.mStringBuilder.append(o);
                    }
                    this.mStringBuilder.append('\n');
                    try {
                        this.mLogWriter.append((CharSequence) this.mStringBuilder);
                        this.mLogWriter.flush();
                        if (DO_LOGCAT) {
                            Log.d(SyncLogger.TAG, this.mStringBuilder.substring(messageStart));
                        }
                    } catch (IOException e) {
                        handleException("Failed to write log", e);
                    }
                }
            }
        }

        @GuardedBy({"mLock"})
        private void openLogLocked(long now) {
            long day = now % 86400000;
            if (this.mLogWriter == null || day != this.mCurrentLogFileDayTimestamp) {
                closeCurrentLogLocked();
                this.mCurrentLogFileDayTimestamp = day;
                this.mCachedDate.setTime(now);
                File file = new File(this.mLogPath, "synclog-" + sFilenameDateFormat.format(this.mCachedDate) + ".log");
                file.getParentFile().mkdirs();
                try {
                    this.mLogWriter = new FileWriter(file, true);
                } catch (IOException e) {
                    handleException("Failed to open log file: " + file, e);
                }
            }
        }

        @GuardedBy({"mLock"})
        private void closeCurrentLogLocked() {
            IoUtils.closeQuietly(this.mLogWriter);
            this.mLogWriter = null;
        }

        @Override // com.android.server.content.SyncLogger
        public void purgeOldLogs() {
            synchronized (this.mLock) {
                FileUtils.deleteOlderFiles(this.mLogPath, 1, this.mKeepAgeMs);
            }
        }

        @Override // com.android.server.content.SyncLogger
        public String jobParametersToString(JobParameters params) {
            return SyncJobService.jobParametersToString(params);
        }

        @Override // com.android.server.content.SyncLogger
        public void dumpAll(PrintWriter pw) {
            synchronized (this.mLock) {
                String[] files = this.mLogPath.list();
                if (files != null) {
                    if (files.length != 0) {
                        Arrays.sort(files);
                        for (String file : files) {
                            dumpFile(pw, new File(this.mLogPath, file));
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0039, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x003e, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x003f, code lost:
            r2.addSuppressed(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0042, code lost:
            throw r3;
         */
        private void dumpFile(PrintWriter pw, File file) {
            Slog.w(SyncLogger.TAG, "Dumping " + file);
            char[] buffer = new char[32768];
            try {
                Reader in = new BufferedReader(new FileReader(file));
                while (true) {
                    int read = in.read(buffer);
                    if (read < 0) {
                        in.close();
                        return;
                    } else if (read > 0) {
                        pw.write(buffer, 0, read);
                    }
                }
            } catch (IOException e) {
            }
        }

        private class MyHandler extends Handler {
            public static final int MSG_LOG_ID = 1;

            MyHandler(Looper looper) {
                super(looper);
            }

            public void log(long now, Object[] message) {
                obtainMessage(1, IntPair.first(now), IntPair.second(now), message).sendToTarget();
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    RotatingFileLogger.this.logInner(IntPair.of(msg.arg1, msg.arg2), (Object[]) msg.obj);
                }
            }
        }
    }

    static String logSafe(Account account) {
        return account == null ? "[null]" : account.toSafeString();
    }

    static String logSafe(SyncStorageEngine.EndPoint endPoint) {
        return endPoint == null ? "[null]" : endPoint.toSafeString();
    }

    static String logSafe(SyncOperation operation) {
        return operation == null ? "[null]" : operation.toSafeString();
    }

    static String logSafe(SyncManager.ActiveSyncContext asc) {
        return asc == null ? "[null]" : asc.toSafeString();
    }
}
