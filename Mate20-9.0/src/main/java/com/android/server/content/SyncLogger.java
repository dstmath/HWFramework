package com.android.server.content;

import android.app.job.JobParameters;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
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

    private static class RotatingFileLogger extends SyncLogger {
        private static final boolean DO_LOGCAT = Log.isLoggable(SyncLogger.TAG, 3);
        private static final SimpleDateFormat sFilenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private static final SimpleDateFormat sTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        @GuardedBy("mLock")
        private final Date mCachedDate = new Date();
        @GuardedBy("mLock")
        private long mCurrentLogFileDayTimestamp;
        @GuardedBy("mLock")
        private boolean mErrorShown;
        private final long mKeepAgeMs = TimeUnit.DAYS.toMillis(7);
        private final Object mLock = new Object();
        private final File mLogPath = new File(Environment.getDataSystemDirectory(), "syncmanager-log");
        @GuardedBy("mLock")
        private Writer mLogWriter;
        @GuardedBy("mLock")
        private final StringBuilder mStringBuilder = new StringBuilder();

        RotatingFileLogger() {
        }

        public boolean enabled() {
            return true;
        }

        private void handleException(String message, Exception e) {
            if (!this.mErrorShown) {
                Slog.e(SyncLogger.TAG, message, e);
                this.mErrorShown = true;
            }
        }

        public void log(Object... message) {
            if (message != null) {
                synchronized (this.mLock) {
                    long now = System.currentTimeMillis();
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
                        this.mStringBuilder.append(10);
                        try {
                            this.mLogWriter.append(this.mStringBuilder);
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
        }

        @GuardedBy("mLock")
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

        @GuardedBy("mLock")
        private void closeCurrentLogLocked() {
            IoUtils.closeQuietly(this.mLogWriter);
            this.mLogWriter = null;
        }

        public void purgeOldLogs() {
            synchronized (this.mLock) {
                FileUtils.deleteOlderFiles(this.mLogPath, 1, this.mKeepAgeMs);
            }
        }

        public String jobParametersToString(JobParameters params) {
            return SyncJobService.jobParametersToString(params);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
            return;
         */
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

        private void dumpFile(PrintWriter pw, File file) {
            Reader in;
            Slog.w(SyncLogger.TAG, "Dumping " + file);
            char[] buffer = new char[32768];
            try {
                in = new BufferedReader(new FileReader(file));
                while (true) {
                    int read = in.read(buffer);
                    int read2 = read;
                    if (read < 0) {
                        in.close();
                        return;
                    } else if (read2 > 0) {
                        pw.write(buffer, 0, read2);
                    }
                }
            } catch (IOException e) {
                return;
            } catch (Throwable th) {
                r2.addSuppressed(th);
            }
            throw th;
        }
    }

    SyncLogger() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0030  */
    public static synchronized SyncLogger getInstance() {
        SyncLogger syncLogger;
        boolean enable;
        synchronized (SyncLogger.class) {
            if (sInstance == null) {
                if (!Build.IS_DEBUGGABLE && !"1".equals(SystemProperties.get("debug.synclog"))) {
                    if (!Log.isLoggable(TAG, 2)) {
                        enable = false;
                        if (!enable) {
                            sInstance = new RotatingFileLogger();
                        } else {
                            sInstance = new SyncLogger();
                        }
                    }
                }
                enable = true;
                if (!enable) {
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
        return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    }

    public void dumpAll(PrintWriter pw) {
    }

    public boolean enabled() {
        return false;
    }
}
