package com.android.server.appop;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteCallback;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.AtomicDirectory;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.FgThread;
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public final class HistoricalRegistry {
    private static final boolean DEBUG = false;
    private static final long DEFAULT_COMPRESSION_STEP = 10;
    private static final int DEFAULT_MODE = 1;
    private static final long DEFAULT_SNAPSHOT_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(15);
    private static final String HISTORY_FILE_SUFFIX = ".xml";
    private static final boolean KEEP_WTF_LOG = Build.IS_DEBUGGABLE;
    private static final String LOG_TAG = HistoricalRegistry.class.getSimpleName();
    private static final int MAX_RECURSIVE_DEPTH = 10;
    private static final int MSG_WRITE_PENDING_HISTORY = 1;
    private static final String PARAMETER_ASSIGNMENT = "=";
    private static final String PARAMETER_DELIMITER = ",";
    @GuardedBy({"mInMemoryLock"})
    private long mBaseSnapshotInterval = DEFAULT_SNAPSHOT_INTERVAL_MILLIS;
    @GuardedBy({"mInMemoryLock"})
    private AppOpsManager.HistoricalOps mCurrentHistoricalOps;
    private final Object mInMemoryLock;
    @GuardedBy({"mInMemoryLock"})
    private long mIntervalCompressionMultiplier = DEFAULT_COMPRESSION_STEP;
    @GuardedBy({"mInMemoryLock"})
    private int mMode = 1;
    @GuardedBy({"mInMemoryLock"})
    private long mNextPersistDueTimeMillis;
    private final Object mOnDiskLock = new Object();
    @GuardedBy({"mInMemoryLock"})
    private long mPendingHistoryOffsetMillis;
    @GuardedBy({"mLock"})
    private LinkedList<AppOpsManager.HistoricalOps> mPendingWrites = new LinkedList<>();
    @GuardedBy({"mOnDiskLock"})
    private Persistence mPersistence;

    HistoricalRegistry(Object lock) {
        this.mInMemoryLock = lock;
    }

    /* access modifiers changed from: package-private */
    public void systemReady(final ContentResolver resolver) {
        resolver.registerContentObserver(Settings.Global.getUriFor("appop_history_parameters"), false, new ContentObserver(FgThread.getHandler()) {
            /* class com.android.server.appop.HistoricalRegistry.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HistoricalRegistry.this.updateParametersFromSetting(resolver);
            }
        });
        updateParametersFromSetting(resolver);
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                if (this.mMode != 0) {
                    if (!isPersistenceInitializedMLocked()) {
                        this.mPersistence = new Persistence(this.mBaseSnapshotInterval, this.mIntervalCompressionMultiplier);
                    }
                    long lastPersistTimeMills = this.mPersistence.getLastPersistTimeMillisDLocked();
                    long current = System.currentTimeMillis();
                    if (lastPersistTimeMills <= 0 || lastPersistTimeMills >= current) {
                        this.mPendingHistoryOffsetMillis = 0;
                    } else {
                        this.mPendingHistoryOffsetMillis = current - lastPersistTimeMills;
                    }
                }
            }
        }
    }

    private boolean isPersistenceInitializedMLocked() {
        return this.mPersistence != null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateParametersFromSetting(ContentResolver resolver) {
        String setting = Settings.Global.getString(resolver, "appop_history_parameters");
        if (setting != null) {
            String[] parameters = setting.split(PARAMETER_DELIMITER);
            int length = parameters.length;
            char c = 0;
            String intervalMultiplierValue = null;
            String baseSnapshotIntervalValue = null;
            String modeValue = null;
            int i = 0;
            while (i < length) {
                String parameter = parameters[i];
                String[] parts = parameter.split(PARAMETER_ASSIGNMENT);
                if (parts.length == 2) {
                    String key = parts[c].trim();
                    char c2 = 65535;
                    int hashCode = key.hashCode();
                    if (hashCode != -190198682) {
                        if (hashCode != 3357091) {
                            if (hashCode == 245634204 && key.equals("baseIntervalMillis")) {
                                c2 = 1;
                            }
                        } else if (key.equals("mode")) {
                            c2 = 0;
                        }
                    } else if (key.equals("intervalMultiplier")) {
                        c2 = 2;
                    }
                    if (c2 == 0) {
                        modeValue = parts[1].trim();
                    } else if (c2 == 1) {
                        baseSnapshotIntervalValue = parts[1].trim();
                    } else if (c2 != 2) {
                        Slog.w(LOG_TAG, "Unknown parameter: " + parameter);
                    } else {
                        intervalMultiplierValue = parts[1].trim();
                    }
                }
                i++;
                c = 0;
            }
            if (!(modeValue == null || baseSnapshotIntervalValue == null || intervalMultiplierValue == null)) {
                try {
                    setHistoryParameters(AppOpsManager.parseHistoricalMode(modeValue), Long.parseLong(baseSnapshotIntervalValue), (long) Integer.parseInt(intervalMultiplierValue));
                    return;
                } catch (NumberFormatException e) {
                }
            }
            Slog.w(LOG_TAG, "Bad value forappop_history_parameters=" + setting + " resetting!");
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw, int filterUid, String filterPackage, int filterOp) {
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                pw.println();
                pw.print(prefix);
                pw.print("History:");
                pw.print("  mode=");
                pw.println(AppOpsManager.historicalModeToString(this.mMode));
                StringDumpVisitor visitor = new StringDumpVisitor(prefix + "  ", pw, filterUid, filterPackage, filterOp);
                long nowMillis = System.currentTimeMillis();
                AppOpsManager.HistoricalOps currentOps = getUpdatedPendingHistoricalOpsMLocked(nowMillis);
                makeRelativeToEpochStart(currentOps, nowMillis);
                currentOps.accept(visitor);
                if (isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                    return;
                }
                List<AppOpsManager.HistoricalOps> ops = this.mPersistence.readHistoryDLocked();
                if (ops != null) {
                    long remainingToFillBatchMillis = (this.mNextPersistDueTimeMillis - nowMillis) - this.mBaseSnapshotInterval;
                    int opCount = ops.size();
                    for (int i = 0; i < opCount; i++) {
                        AppOpsManager.HistoricalOps op = ops.get(i);
                        op.offsetBeginAndEndTime(remainingToFillBatchMillis);
                        makeRelativeToEpochStart(op, nowMillis);
                        op.accept(visitor);
                    }
                } else {
                    pw.println("  Empty");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getMode() {
        int i;
        synchronized (this.mInMemoryLock) {
            i = this.mMode;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void getHistoricalOpsFromDiskRaw(int uid, String packageName, String[] opNames, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) {
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                    callback.sendResult(new Bundle());
                    return;
                }
                AppOpsManager.HistoricalOps result = new AppOpsManager.HistoricalOps(beginTimeMillis, endTimeMillis);
                this.mPersistence.collectHistoricalOpsDLocked(result, uid, packageName, opNames, beginTimeMillis, endTimeMillis, flags);
                Bundle payload = new Bundle();
                payload.putParcelable("historical_ops", result);
                callback.sendResult(payload);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getHistoricalOps(int uid, String packageName, String[] opNames, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) {
        long endTimeMillis2;
        AppOpsManager.HistoricalOps currentOps;
        AppOpsManager.HistoricalOps currentOps2;
        long inMemoryAdjEndTimeMillis;
        Parcelable result;
        List<AppOpsManager.HistoricalOps> pendingWrites;
        boolean collectOpsFromDisk;
        long currentTimeMillis = System.currentTimeMillis();
        if (endTimeMillis == JobStatus.NO_LATEST_RUNTIME) {
            endTimeMillis2 = currentTimeMillis;
        } else {
            endTimeMillis2 = endTimeMillis;
        }
        long inMemoryAdjBeginTimeMillis = Math.max(currentTimeMillis - endTimeMillis2, 0L);
        long inMemoryAdjEndTimeMillis2 = Math.max(currentTimeMillis - beginTimeMillis, 0L);
        Parcelable historicalOps = new AppOpsManager.HistoricalOps(inMemoryAdjBeginTimeMillis, inMemoryAdjEndTimeMillis2);
        synchronized (this.mOnDiskLock) {
            try {
                synchronized (this.mInMemoryLock) {
                    try {
                        if (!isPersistenceInitializedMLocked()) {
                            try {
                                try {
                                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                                    callback.sendResult(new Bundle());
                                } catch (Throwable th) {
                                    currentOps2 = th;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            currentOps2 = th2;
                                        }
                                    }
                                    throw currentOps2;
                                }
                            } catch (Throwable th3) {
                                currentOps2 = th3;
                                while (true) {
                                    break;
                                }
                                throw currentOps2;
                            }
                            try {
                                return;
                            } catch (Throwable th4) {
                                currentOps = th4;
                                throw currentOps;
                            }
                        } else {
                            try {
                                AppOpsManager.HistoricalOps currentOps3 = getUpdatedPendingHistoricalOpsMLocked(currentTimeMillis);
                                if (inMemoryAdjBeginTimeMillis < currentOps3.getEndTimeMillis()) {
                                    try {
                                        if (inMemoryAdjEndTimeMillis2 > currentOps3.getBeginTimeMillis()) {
                                            AppOpsManager.HistoricalOps currentOpsCopy = new AppOpsManager.HistoricalOps(currentOps3);
                                            result = historicalOps;
                                            inMemoryAdjEndTimeMillis = inMemoryAdjEndTimeMillis2;
                                            try {
                                                currentOpsCopy.filter(uid, packageName, opNames, inMemoryAdjBeginTimeMillis, inMemoryAdjEndTimeMillis);
                                                result.merge(currentOpsCopy);
                                            } catch (Throwable th5) {
                                                currentOps2 = th5;
                                            }
                                        } else {
                                            inMemoryAdjEndTimeMillis = inMemoryAdjEndTimeMillis2;
                                            result = historicalOps;
                                        }
                                    } catch (Throwable th6) {
                                        currentOps2 = th6;
                                        while (true) {
                                            break;
                                        }
                                        throw currentOps2;
                                    }
                                } else {
                                    inMemoryAdjEndTimeMillis = inMemoryAdjEndTimeMillis2;
                                    result = historicalOps;
                                }
                                try {
                                    pendingWrites = new ArrayList<>(this.mPendingWrites);
                                    this.mPendingWrites.clear();
                                    collectOpsFromDisk = inMemoryAdjEndTimeMillis > currentOps3.getEndTimeMillis();
                                } catch (Throwable th7) {
                                    currentOps2 = th7;
                                    while (true) {
                                        break;
                                    }
                                    throw currentOps2;
                                }
                            } catch (Throwable th8) {
                                currentOps2 = th8;
                                while (true) {
                                    break;
                                }
                                throw currentOps2;
                            }
                        }
                    } catch (Throwable th9) {
                        currentOps2 = th9;
                        while (true) {
                            break;
                        }
                        throw currentOps2;
                    }
                }
                if (collectOpsFromDisk) {
                    try {
                        persistPendingHistory(pendingWrites);
                    } catch (Throwable th10) {
                        currentOps = th10;
                        throw currentOps;
                    }
                    try {
                        long onDiskAndInMemoryOffsetMillis = (currentTimeMillis - this.mNextPersistDueTimeMillis) + this.mBaseSnapshotInterval;
                        try {
                            this.mPersistence.collectHistoricalOpsDLocked(result, uid, packageName, opNames, Math.max(inMemoryAdjBeginTimeMillis - onDiskAndInMemoryOffsetMillis, 0L), Math.max(inMemoryAdjEndTimeMillis - onDiskAndInMemoryOffsetMillis, 0L), flags);
                        } catch (Throwable th11) {
                            currentOps = th11;
                        }
                    } catch (Throwable th12) {
                        currentOps = th12;
                        throw currentOps;
                    }
                }
                try {
                    result.setBeginAndEndTime(beginTimeMillis, endTimeMillis2);
                    Bundle payload = new Bundle();
                    payload.putParcelable("historical_ops", result);
                    try {
                        callback.sendResult(payload);
                    } catch (Throwable th13) {
                        currentOps = th13;
                        throw currentOps;
                    }
                } catch (Throwable th14) {
                    currentOps = th14;
                    throw currentOps;
                }
            } catch (Throwable th15) {
                currentOps = th15;
                throw currentOps;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void incrementOpAccessedCount(int op, int uid, String packageName, int uidState, int flags) {
        synchronized (this.mInMemoryLock) {
            if (this.mMode == 1) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                    return;
                }
                getUpdatedPendingHistoricalOpsMLocked(System.currentTimeMillis()).increaseAccessCount(op, uid, packageName, uidState, flags, 1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void incrementOpRejected(int op, int uid, String packageName, int uidState, int flags) {
        synchronized (this.mInMemoryLock) {
            if (this.mMode == 1) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                    return;
                }
                getUpdatedPendingHistoricalOpsMLocked(System.currentTimeMillis()).increaseRejectCount(op, uid, packageName, uidState, flags, 1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void increaseOpAccessDuration(int op, int uid, String packageName, int uidState, int flags, long increment) {
        synchronized (this.mInMemoryLock) {
            if (this.mMode == 1) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                    return;
                }
                getUpdatedPendingHistoricalOpsMLocked(System.currentTimeMillis()).increaseAccessDuration(op, uid, packageName, uidState, flags, increment);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setHistoryParameters(int mode, long baseSnapshotInterval, long intervalCompressionMultiplier) {
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                boolean resampleHistory = false;
                String str = LOG_TAG;
                Slog.i(str, "New history parameters: mode:" + AppOpsManager.historicalModeToString(this.mMode) + " baseSnapshotInterval:" + baseSnapshotInterval + " intervalCompressionMultiplier:" + intervalCompressionMultiplier);
                if (this.mMode != mode) {
                    this.mMode = mode;
                    if (this.mMode == 0) {
                        clearHistoryOnDiskDLocked();
                    }
                }
                if (this.mBaseSnapshotInterval != baseSnapshotInterval) {
                    this.mBaseSnapshotInterval = baseSnapshotInterval;
                    resampleHistory = true;
                }
                if (this.mIntervalCompressionMultiplier != intervalCompressionMultiplier) {
                    this.mIntervalCompressionMultiplier = intervalCompressionMultiplier;
                    resampleHistory = true;
                }
                if (resampleHistory) {
                    resampleHistoryOnDiskInMemoryDMLocked(0);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void offsetHistory(long offsetMillis) {
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                    return;
                }
                List<AppOpsManager.HistoricalOps> history = this.mPersistence.readHistoryDLocked();
                clearHistory();
                if (history != null) {
                    int historySize = history.size();
                    for (int i = 0; i < historySize; i++) {
                        history.get(i).offsetBeginAndEndTime(offsetMillis);
                    }
                    if (offsetMillis < 0) {
                        pruneFutureOps(history);
                    }
                    this.mPersistence.persistHistoricalOpsDLocked(history);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addHistoricalOps(AppOpsManager.HistoricalOps ops) {
        synchronized (this.mInMemoryLock) {
            if (!isPersistenceInitializedMLocked()) {
                Slog.e(LOG_TAG, "Interaction before persistence initialized");
                return;
            }
            ops.offsetBeginAndEndTime(this.mBaseSnapshotInterval);
            this.mPendingWrites.offerFirst(ops);
            List<AppOpsManager.HistoricalOps> pendingWrites = new ArrayList<>(this.mPendingWrites);
            this.mPendingWrites.clear();
            persistPendingHistory(pendingWrites);
        }
    }

    private void resampleHistoryOnDiskInMemoryDMLocked(long offsetMillis) {
        this.mPersistence = new Persistence(this.mBaseSnapshotInterval, this.mIntervalCompressionMultiplier);
        offsetHistory(offsetMillis);
    }

    /* access modifiers changed from: package-private */
    public void resetHistoryParameters() {
        if (!isPersistenceInitializedMLocked()) {
            Slog.e(LOG_TAG, "Interaction before persistence initialized");
        } else {
            setHistoryParameters(1, DEFAULT_SNAPSHOT_INTERVAL_MILLIS, DEFAULT_COMPRESSION_STEP);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearHistory(int uid, String packageName) {
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                } else if (this.mMode == 1) {
                    for (int index = 0; index < this.mPendingWrites.size(); index++) {
                        this.mPendingWrites.get(index).clearHistory(uid, packageName);
                    }
                    getUpdatedPendingHistoricalOpsMLocked(System.currentTimeMillis()).clearHistory(uid, packageName);
                    this.mPersistence.clearHistoryDLocked(uid, packageName);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearHistory() {
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                if (!isPersistenceInitializedMLocked()) {
                    Slog.e(LOG_TAG, "Interaction before persistence initialized");
                } else {
                    clearHistoryOnDiskDLocked();
                }
            }
        }
    }

    private void clearHistoryOnDiskDLocked() {
        BackgroundThread.getHandler().removeMessages(1);
        synchronized (this.mInMemoryLock) {
            this.mCurrentHistoricalOps = null;
            this.mNextPersistDueTimeMillis = System.currentTimeMillis();
            this.mPendingWrites.clear();
        }
        Persistence.clearHistoryDLocked();
    }

    private AppOpsManager.HistoricalOps getUpdatedPendingHistoricalOpsMLocked(long now) {
        if (this.mCurrentHistoricalOps != null) {
            long remainingTimeMillis = this.mNextPersistDueTimeMillis - now;
            long j = this.mBaseSnapshotInterval;
            if (remainingTimeMillis > j) {
                this.mPendingHistoryOffsetMillis = remainingTimeMillis - j;
            }
            this.mCurrentHistoricalOps.setEndTime(this.mBaseSnapshotInterval - remainingTimeMillis);
            if (remainingTimeMillis > 0) {
                return this.mCurrentHistoricalOps;
            }
            if (this.mCurrentHistoricalOps.isEmpty()) {
                this.mCurrentHistoricalOps.setBeginAndEndTime(0, 0);
                this.mNextPersistDueTimeMillis = this.mBaseSnapshotInterval + now;
                return this.mCurrentHistoricalOps;
            }
            this.mCurrentHistoricalOps.offsetBeginAndEndTime(this.mBaseSnapshotInterval);
            AppOpsManager.HistoricalOps historicalOps = this.mCurrentHistoricalOps;
            historicalOps.setBeginTime(historicalOps.getEndTimeMillis() - this.mBaseSnapshotInterval);
            this.mCurrentHistoricalOps.offsetBeginAndEndTime(Math.abs(remainingTimeMillis));
            schedulePersistHistoricalOpsMLocked(this.mCurrentHistoricalOps);
        }
        this.mCurrentHistoricalOps = new AppOpsManager.HistoricalOps(0, 0);
        this.mNextPersistDueTimeMillis = this.mBaseSnapshotInterval + now;
        return this.mCurrentHistoricalOps;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void persistPendingHistory() {
        List<AppOpsManager.HistoricalOps> pendingWrites;
        synchronized (this.mOnDiskLock) {
            synchronized (this.mInMemoryLock) {
                pendingWrites = new ArrayList<>(this.mPendingWrites);
                this.mPendingWrites.clear();
                if (this.mPendingHistoryOffsetMillis != 0) {
                    resampleHistoryOnDiskInMemoryDMLocked(this.mPendingHistoryOffsetMillis);
                    this.mPendingHistoryOffsetMillis = 0;
                }
            }
            persistPendingHistory(pendingWrites);
        }
    }

    private void persistPendingHistory(List<AppOpsManager.HistoricalOps> pendingWrites) {
        synchronized (this.mOnDiskLock) {
            BackgroundThread.getHandler().removeMessages(1);
            if (!pendingWrites.isEmpty()) {
                int opCount = pendingWrites.size();
                for (int i = 0; i < opCount; i++) {
                    AppOpsManager.HistoricalOps current = pendingWrites.get(i);
                    if (i > 0) {
                        current.offsetBeginAndEndTime(pendingWrites.get(i - 1).getBeginTimeMillis());
                    }
                }
                this.mPersistence.persistHistoricalOpsDLocked(pendingWrites);
            }
        }
    }

    private void schedulePersistHistoricalOpsMLocked(AppOpsManager.HistoricalOps ops) {
        Message message = PooledLambda.obtainMessage($$Lambda$HistoricalRegistry$dJrtb4M71TzV6sx9vPEImQG_akU.INSTANCE, this);
        message.what = 1;
        BackgroundThread.getHandler().sendMessage(message);
        this.mPendingWrites.offerFirst(ops);
    }

    private static void makeRelativeToEpochStart(AppOpsManager.HistoricalOps ops, long nowMillis) {
        ops.setBeginAndEndTime(nowMillis - ops.getEndTimeMillis(), nowMillis - ops.getBeginTimeMillis());
    }

    private void pruneFutureOps(List<AppOpsManager.HistoricalOps> ops) {
        for (int i = ops.size() - 1; i >= 0; i--) {
            AppOpsManager.HistoricalOps op = ops.get(i);
            if (op.getEndTimeMillis() <= this.mBaseSnapshotInterval) {
                ops.remove(i);
            } else if (op.getBeginTimeMillis() < this.mBaseSnapshotInterval) {
                Persistence.spliceFromBeginning(op, ((double) (op.getEndTimeMillis() - this.mBaseSnapshotInterval)) / ((double) op.getDurationMillis()));
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class Persistence {
        private static final String ATTR_ACCESS_COUNT = "ac";
        private static final String ATTR_ACCESS_DURATION = "du";
        private static final String ATTR_BEGIN_TIME = "beg";
        private static final String ATTR_END_TIME = "end";
        private static final String ATTR_NAME = "na";
        private static final String ATTR_OVERFLOW = "ov";
        private static final String ATTR_REJECT_COUNT = "rc";
        private static final String ATTR_VERSION = "ver";
        private static final int CURRENT_VERSION = 2;
        private static final boolean DEBUG = false;
        private static final String LOG_TAG = Persistence.class.getSimpleName();
        private static final String TAG_HISTORY = "history";
        private static final String TAG_OP = "op";
        private static final String TAG_OPS = "ops";
        private static final String TAG_PACKAGE = "pkg";
        private static final String TAG_STATE = "st";
        private static final String TAG_UID = "uid";
        private static final AtomicDirectory sHistoricalAppOpsDir = new AtomicDirectory(new File(new File(Environment.getDataSystemDirectory(), "appops"), TAG_HISTORY));
        private final long mBaseSnapshotInterval;
        private final long mIntervalCompressionMultiplier;

        Persistence(long baseSnapshotInterval, long intervalCompressionMultiplier) {
            this.mBaseSnapshotInterval = baseSnapshotInterval;
            this.mIntervalCompressionMultiplier = intervalCompressionMultiplier;
        }

        private File generateFile(File baseDir, int depth) {
            long globalBeginMillis = computeGlobalIntervalBeginMillis(depth);
            return new File(baseDir, Long.toString(globalBeginMillis) + HistoricalRegistry.HISTORY_FILE_SUFFIX);
        }

        /* access modifiers changed from: package-private */
        public void clearHistoryDLocked(int uid, String packageName) {
            List<AppOpsManager.HistoricalOps> historicalOps = readHistoryDLocked();
            if (historicalOps != null) {
                for (int index = 0; index < historicalOps.size(); index++) {
                    historicalOps.get(index).clearHistory(uid, packageName);
                }
                clearHistoryDLocked();
                persistHistoricalOpsDLocked(historicalOps);
            }
        }

        static void clearHistoryDLocked() {
            sHistoricalAppOpsDir.delete();
        }

        /* access modifiers changed from: package-private */
        public void persistHistoricalOpsDLocked(List<AppOpsManager.HistoricalOps> ops) {
            try {
                File newBaseDir = sHistoricalAppOpsDir.startWrite();
                File oldBaseDir = sHistoricalAppOpsDir.getBackupDirectory();
                handlePersistHistoricalOpsRecursiveDLocked(newBaseDir, oldBaseDir, ops, getHistoricalFileNames(oldBaseDir), 0);
                sHistoricalAppOpsDir.finishWrite();
            } catch (Throwable t) {
                HistoricalRegistry.wtf("Failed to write historical app ops, restoring backup", t, null);
                sHistoricalAppOpsDir.failWrite();
            }
        }

        /* access modifiers changed from: package-private */
        public List<AppOpsManager.HistoricalOps> readHistoryRawDLocked() {
            return collectHistoricalOpsBaseDLocked(-1, null, null, 0, JobStatus.NO_LATEST_RUNTIME, 31);
        }

        /* access modifiers changed from: package-private */
        public List<AppOpsManager.HistoricalOps> readHistoryDLocked() {
            List<AppOpsManager.HistoricalOps> result = readHistoryRawDLocked();
            if (result != null) {
                int opCount = result.size();
                for (int i = 0; i < opCount; i++) {
                    result.get(i).offsetBeginAndEndTime(this.mBaseSnapshotInterval);
                }
            }
            return result;
        }

        /* access modifiers changed from: package-private */
        public long getLastPersistTimeMillisDLocked() {
            try {
                File[] files = sHistoricalAppOpsDir.startRead().listFiles();
                if (files == null || files.length <= 0) {
                    sHistoricalAppOpsDir.finishRead();
                    return 0;
                }
                File shortestFile = null;
                for (File candidate : files) {
                    String candidateName = candidate.getName();
                    if (candidateName.endsWith(HistoricalRegistry.HISTORY_FILE_SUFFIX)) {
                        if (shortestFile == null) {
                            shortestFile = candidate;
                        } else if (candidateName.length() < shortestFile.getName().length()) {
                            shortestFile = candidate;
                        }
                    }
                }
                if (shortestFile == null) {
                    return 0;
                }
                return shortestFile.lastModified();
            } catch (Throwable e) {
                HistoricalRegistry.wtf("Error reading historical app ops. Deleting history.", e, null);
                sHistoricalAppOpsDir.delete();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void collectHistoricalOpsDLocked(AppOpsManager.HistoricalOps currentOps, int filterUid, String filterPackageName, String[] filterOpNames, long filterBeingMillis, long filterEndMillis, int filterFlags) {
            List<AppOpsManager.HistoricalOps> readOps = collectHistoricalOpsBaseDLocked(filterUid, filterPackageName, filterOpNames, filterBeingMillis, filterEndMillis, filterFlags);
            if (readOps != null) {
                int readCount = readOps.size();
                for (int i = 0; i < readCount; i++) {
                    currentOps.merge(readOps.get(i));
                }
            }
        }

        private LinkedList<AppOpsManager.HistoricalOps> collectHistoricalOpsBaseDLocked(int filterUid, String filterPackageName, String[] filterOpNames, long filterBeginTimeMillis, long filterEndTimeMillis, int filterFlags) {
            File baseDir;
            Throwable t;
            try {
                baseDir = sHistoricalAppOpsDir.startRead();
                try {
                    LinkedList<AppOpsManager.HistoricalOps> ops = collectHistoricalOpsRecursiveDLocked(baseDir, filterUid, filterPackageName, filterOpNames, filterBeginTimeMillis, filterEndTimeMillis, filterFlags, new long[]{0}, null, 0, getHistoricalFileNames(baseDir));
                    sHistoricalAppOpsDir.finishRead();
                    return ops;
                } catch (Throwable th) {
                    t = th;
                    HistoricalRegistry.wtf("Error reading historical app ops. Deleting history.", t, baseDir);
                    sHistoricalAppOpsDir.delete();
                    return null;
                }
            } catch (Throwable th2) {
                t = th2;
                baseDir = null;
                HistoricalRegistry.wtf("Error reading historical app ops. Deleting history.", t, baseDir);
                sHistoricalAppOpsDir.delete();
                return null;
            }
        }

        private LinkedList<AppOpsManager.HistoricalOps> collectHistoricalOpsRecursiveDLocked(File baseDir, int filterUid, String filterPackageName, String[] filterOpNames, long filterBeginTimeMillis, long filterEndTimeMillis, int filterFlags, long[] globalContentOffsetMillis, LinkedList<AppOpsManager.HistoricalOps> outOps, int depth, Set<String> historyFiles) throws IOException, XmlPullParserException {
            long previousIntervalEndMillis = ((long) Math.pow((double) this.mIntervalCompressionMultiplier, (double) depth)) * this.mBaseSnapshotInterval;
            long currentIntervalEndMillis = this.mBaseSnapshotInterval * ((long) Math.pow((double) this.mIntervalCompressionMultiplier, (double) (depth + 1)));
            long filterBeginTimeMillis2 = Math.max(filterBeginTimeMillis - previousIntervalEndMillis, 0L);
            long filterEndTimeMillis2 = filterEndTimeMillis - previousIntervalEndMillis;
            List<AppOpsManager.HistoricalOps> readOps = readHistoricalOpsLocked(baseDir, previousIntervalEndMillis, currentIntervalEndMillis, filterUid, filterPackageName, filterOpNames, filterBeginTimeMillis2, filterEndTimeMillis2, filterFlags, globalContentOffsetMillis, depth, historyFiles);
            if (readOps != null && readOps.isEmpty()) {
                return outOps;
            }
            LinkedList<AppOpsManager.HistoricalOps> outOps2 = collectHistoricalOpsRecursiveDLocked(baseDir, filterUid, filterPackageName, filterOpNames, filterBeginTimeMillis2, filterEndTimeMillis2, filterFlags, globalContentOffsetMillis, outOps, depth + 1, historyFiles);
            if (outOps2 != null) {
                int opCount = outOps2.size();
                for (int i = 0; i < opCount; i++) {
                    outOps2.get(i).offsetBeginAndEndTime(currentIntervalEndMillis);
                }
            }
            if (readOps != null) {
                if (outOps2 == null) {
                    outOps2 = new LinkedList<>();
                }
                for (int i2 = readOps.size() - 1; i2 >= 0; i2--) {
                    outOps2.offerFirst(readOps.get(i2));
                }
            }
            return outOps2;
        }

        private void handlePersistHistoricalOpsRecursiveDLocked(File newBaseDir, File oldBaseDir, List<AppOpsManager.HistoricalOps> passedOps, Set<String> oldFileNames, int depth) throws IOException, XmlPullParserException {
            Persistence persistence;
            File file;
            int i;
            Set<String> set;
            List<AppOpsManager.HistoricalOps> list;
            AppOpsManager.HistoricalOps overflowedOp;
            AppOpsManager.HistoricalOps persistedOp;
            long previousIntervalEndMillis = ((long) Math.pow((double) this.mIntervalCompressionMultiplier, (double) depth)) * this.mBaseSnapshotInterval;
            long currentIntervalEndMillis = ((long) Math.pow((double) this.mIntervalCompressionMultiplier, (double) (depth + 1))) * this.mBaseSnapshotInterval;
            if (passedOps == null) {
                set = oldFileNames;
                i = depth;
                file = newBaseDir;
                persistence = this;
            } else if (passedOps.isEmpty()) {
                set = oldFileNames;
                i = depth;
                file = newBaseDir;
                persistence = this;
            } else {
                int passedOpCount = passedOps.size();
                for (int i2 = 0; i2 < passedOpCount; i2++) {
                    passedOps.get(i2).offsetBeginAndEndTime(-previousIntervalEndMillis);
                }
                List<AppOpsManager.HistoricalOps> existingOps = readHistoricalOpsLocked(oldBaseDir, previousIntervalEndMillis, currentIntervalEndMillis, -1, null, null, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, 31, null, depth, null);
                if (existingOps != null) {
                    int existingOpCount = existingOps.size();
                    if (existingOpCount > 0) {
                        list = passedOps;
                        long elapsedTimeMillis = list.get(passedOps.size() - 1).getEndTimeMillis();
                        for (int i3 = 0; i3 < existingOpCount; i3++) {
                            existingOps.get(i3).offsetBeginAndEndTime(elapsedTimeMillis);
                        }
                    } else {
                        list = passedOps;
                    }
                } else {
                    list = passedOps;
                }
                List<AppOpsManager.HistoricalOps> allOps = new LinkedList<>(list);
                if (existingOps != null) {
                    allOps.addAll(existingOps);
                }
                int opCount = allOps.size();
                List<AppOpsManager.HistoricalOps> persistedOps = null;
                List<AppOpsManager.HistoricalOps> overflowedOps = null;
                long intervalOverflowMillis = 0;
                for (int i4 = 0; i4 < opCount; i4++) {
                    AppOpsManager.HistoricalOps op = allOps.get(i4);
                    if (op.getEndTimeMillis() <= currentIntervalEndMillis) {
                        persistedOp = op;
                        overflowedOp = null;
                    } else if (op.getBeginTimeMillis() < currentIntervalEndMillis) {
                        persistedOp = op;
                        long intervalOverflowMillis2 = op.getEndTimeMillis() - currentIntervalEndMillis;
                        if (intervalOverflowMillis2 > previousIntervalEndMillis) {
                            overflowedOp = spliceFromEnd(op, ((double) intervalOverflowMillis2) / ((double) op.getDurationMillis()));
                            persistedOp = persistedOp;
                            intervalOverflowMillis = op.getEndTimeMillis() - currentIntervalEndMillis;
                        } else {
                            overflowedOp = null;
                            intervalOverflowMillis = intervalOverflowMillis2;
                        }
                    } else {
                        persistedOp = null;
                        overflowedOp = op;
                    }
                    if (persistedOp != null) {
                        if (persistedOps == null) {
                            persistedOps = new ArrayList<>();
                        }
                        persistedOps.add(persistedOp);
                    }
                    if (overflowedOp != null) {
                        if (overflowedOps == null) {
                            overflowedOps = new ArrayList<>();
                        }
                        overflowedOps.add(overflowedOp);
                    }
                }
                File newFile = generateFile(newBaseDir, depth);
                oldFileNames.remove(newFile.getName());
                if (persistedOps != null) {
                    normalizeSnapshotForSlotDuration(persistedOps, previousIntervalEndMillis);
                    writeHistoricalOpsDLocked(persistedOps, intervalOverflowMillis, newFile);
                }
                handlePersistHistoricalOpsRecursiveDLocked(newBaseDir, oldBaseDir, overflowedOps, oldFileNames, depth + 1);
                return;
            }
            if (!oldFileNames.isEmpty()) {
                File oldFile = persistence.generateFile(oldBaseDir, i);
                if (i >= 10) {
                    Slog.d(TAG_HISTORY, "depth excess! fileName:" + oldFile.getName());
                }
                if (set.remove(oldFile.getName())) {
                    Files.createLink(persistence.generateFile(file, i).toPath(), oldFile.toPath());
                }
                handlePersistHistoricalOpsRecursiveDLocked(newBaseDir, oldBaseDir, passedOps, oldFileNames, i + 1);
            }
        }

        private List<AppOpsManager.HistoricalOps> readHistoricalOpsLocked(File baseDir, long intervalBeginMillis, long intervalEndMillis, int filterUid, String filterPackageName, String[] filterOpNames, long filterBeginTimeMillis, long filterEndTimeMillis, int filterFlags, long[] cumulativeOverflowMillis, int depth, Set<String> historyFiles) throws IOException, XmlPullParserException {
            File file = generateFile(baseDir, depth);
            if (historyFiles != null) {
                historyFiles.remove(file.getName());
            }
            if (filterBeginTimeMillis >= filterEndTimeMillis || filterEndTimeMillis < intervalBeginMillis) {
                return Collections.emptyList();
            }
            if (filterBeginTimeMillis < intervalEndMillis + ((intervalEndMillis - intervalBeginMillis) / this.mIntervalCompressionMultiplier) + (cumulativeOverflowMillis != null ? cumulativeOverflowMillis[0] : 0) && file.exists()) {
                return readHistoricalOpsLocked(file, filterUid, filterPackageName, filterOpNames, filterBeginTimeMillis, filterEndTimeMillis, filterFlags, cumulativeOverflowMillis);
            }
            if (historyFiles == null || historyFiles.isEmpty()) {
                return Collections.emptyList();
            }
            return null;
        }

        private List<AppOpsManager.HistoricalOps> readHistoricalOpsLocked(File file, int filterUid, String filterPackageName, String[] filterOpNames, long filterBeginTimeMillis, long filterEndTimeMillis, int filterFlags, long[] cumulativeOverflowMillis) throws IOException, XmlPullParserException {
            Throwable th;
            int depth;
            List<AppOpsManager.HistoricalOps> allOps;
            try {
                FileInputStream stream = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, StandardCharsets.UTF_8.name());
                    XmlUtils.beginDocument(parser, TAG_HISTORY);
                    if (XmlUtils.readIntAttribute(parser, ATTR_VERSION) >= 2) {
                        long overflowMillis = XmlUtils.readLongAttribute(parser, ATTR_OVERFLOW, 0);
                        int depth2 = parser.getDepth();
                        List<AppOpsManager.HistoricalOps> allOps2 = null;
                        while (XmlUtils.nextElementWithin(parser, depth2)) {
                            try {
                                if (TAG_OPS.equals(parser.getName())) {
                                    depth = depth2;
                                    AppOpsManager.HistoricalOps ops = readeHistoricalOpsDLocked(parser, filterUid, filterPackageName, filterOpNames, filterBeginTimeMillis, filterEndTimeMillis, filterFlags, cumulativeOverflowMillis);
                                    if (ops != null) {
                                        if (ops.isEmpty()) {
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            if (allOps2 == null) {
                                                allOps = new ArrayList<>();
                                            } else {
                                                allOps = allOps2;
                                            }
                                            try {
                                                allOps.add(ops);
                                                allOps2 = allOps;
                                                depth2 = depth;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                try {
                                                    throw th;
                                                } catch (Throwable th3) {
                                                    th.addSuppressed(th3);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    depth = depth2;
                                }
                                depth2 = depth;
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                        if (cumulativeOverflowMillis != null) {
                            cumulativeOverflowMillis[0] = cumulativeOverflowMillis[0] + overflowMillis;
                        }
                        try {
                            stream.close();
                            return allOps2;
                        } catch (FileNotFoundException e) {
                        }
                    } else {
                        throw new IllegalStateException("Dropping unsupported history version 1 for file:" + file);
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
                throw th;
            } catch (FileNotFoundException e2) {
                Slog.i(LOG_TAG, "No history file: " + file.getName());
                return Collections.emptyList();
            }
        }

        private AppOpsManager.HistoricalOps readeHistoricalOpsDLocked(XmlPullParser parser, int filterUid, String filterPackageName, String[] filterOpNames, long filterBeginTimeMillis, long filterEndTimeMillis, int filterFlags, long[] cumulativeOverflowMillis) throws IOException, XmlPullParserException {
            XmlPullParser xmlPullParser = parser;
            long beginTimeMillis = XmlUtils.readLongAttribute(xmlPullParser, ATTR_BEGIN_TIME, 0) + (cumulativeOverflowMillis != null ? cumulativeOverflowMillis[0] : 0);
            long endTimeMillis = XmlUtils.readLongAttribute(xmlPullParser, ATTR_END_TIME, 0) + (cumulativeOverflowMillis != null ? cumulativeOverflowMillis[0] : 0);
            if (filterEndTimeMillis < beginTimeMillis) {
                return null;
            }
            if (filterBeginTimeMillis > endTimeMillis) {
                return new AppOpsManager.HistoricalOps(0, 0);
            }
            long filteredBeginTimeMillis = Math.max(beginTimeMillis, filterBeginTimeMillis);
            long filteredEndTimeMillis = Math.min(endTimeMillis, filterEndTimeMillis);
            long filteredEndTimeMillis2 = filteredEndTimeMillis;
            double filterScale = ((double) (filteredEndTimeMillis - filteredBeginTimeMillis)) / ((double) (endTimeMillis - beginTimeMillis));
            int depth = parser.getDepth();
            AppOpsManager.HistoricalOps ops = null;
            while (XmlUtils.nextElementWithin(xmlPullParser, depth)) {
                if ("uid".equals(parser.getName())) {
                    AppOpsManager.HistoricalOps returnedOps = readHistoricalUidOpsDLocked(ops, parser, filterUid, filterPackageName, filterOpNames, filterFlags, filterScale);
                    if (ops == null) {
                        ops = returnedOps;
                    } else {
                        ops = ops;
                    }
                    filteredBeginTimeMillis = filteredBeginTimeMillis;
                    depth = depth;
                    endTimeMillis = endTimeMillis;
                    filteredEndTimeMillis2 = filteredEndTimeMillis2;
                    xmlPullParser = parser;
                } else {
                    filteredEndTimeMillis2 = filteredEndTimeMillis2;
                    xmlPullParser = parser;
                }
            }
            if (ops != null) {
                ops.setBeginAndEndTime(filteredBeginTimeMillis, filteredEndTimeMillis2);
            }
            return ops;
        }

        private AppOpsManager.HistoricalOps readHistoricalUidOpsDLocked(AppOpsManager.HistoricalOps ops, XmlPullParser parser, int filterUid, String filterPackageName, String[] filterOpNames, int filterFlags, double filterScale) throws IOException, XmlPullParserException {
            int uid = XmlUtils.readIntAttribute(parser, ATTR_NAME);
            if (filterUid == -1 || filterUid == uid) {
                int depth = parser.getDepth();
                AppOpsManager.HistoricalOps ops2 = ops;
                while (XmlUtils.nextElementWithin(parser, depth)) {
                    if ("pkg".equals(parser.getName())) {
                        AppOpsManager.HistoricalOps returnedOps = readHistoricalPackageOpsDLocked(ops2, uid, parser, filterPackageName, filterOpNames, filterFlags, filterScale);
                        if (ops2 == null) {
                            ops2 = returnedOps;
                        }
                    }
                }
                return ops2;
            }
            XmlUtils.skipCurrentTag(parser);
            return null;
        }

        private AppOpsManager.HistoricalOps readHistoricalPackageOpsDLocked(AppOpsManager.HistoricalOps ops, int uid, XmlPullParser parser, String filterPackageName, String[] filterOpNames, int filterFlags, double filterScale) throws IOException, XmlPullParserException {
            String packageName = XmlUtils.readStringAttribute(parser, ATTR_NAME);
            if (filterPackageName == null || filterPackageName.equals(packageName)) {
                int depth = parser.getDepth();
                AppOpsManager.HistoricalOps ops2 = ops;
                while (XmlUtils.nextElementWithin(parser, depth)) {
                    if (TAG_OP.equals(parser.getName())) {
                        AppOpsManager.HistoricalOps returnedOps = readHistoricalOpDLocked(ops2, uid, packageName, parser, filterOpNames, filterFlags, filterScale);
                        if (ops2 == null) {
                            ops2 = returnedOps;
                        }
                    }
                }
                return ops2;
            }
            XmlUtils.skipCurrentTag(parser);
            return null;
        }

        private AppOpsManager.HistoricalOps readHistoricalOpDLocked(AppOpsManager.HistoricalOps ops, int uid, String packageName, XmlPullParser parser, String[] filterOpNames, int filterFlags, double filterScale) throws IOException, XmlPullParserException {
            int op = XmlUtils.readIntAttribute(parser, ATTR_NAME);
            if (filterOpNames == null || ArrayUtils.contains(filterOpNames, AppOpsManager.opToPublicName(op))) {
                int depth = parser.getDepth();
                AppOpsManager.HistoricalOps ops2 = ops;
                while (XmlUtils.nextElementWithin(parser, depth)) {
                    if (TAG_STATE.equals(parser.getName())) {
                        AppOpsManager.HistoricalOps returnedOps = readStateDLocked(ops2, uid, packageName, op, parser, filterFlags, filterScale);
                        if (ops2 == null) {
                            ops2 = returnedOps;
                        }
                    }
                }
                return ops2;
            }
            XmlUtils.skipCurrentTag(parser);
            return null;
        }

        private AppOpsManager.HistoricalOps readStateDLocked(AppOpsManager.HistoricalOps ops, int uid, String packageName, int op, XmlPullParser parser, int filterFlags, double filterScale) throws IOException {
            AppOpsManager.HistoricalOps ops2;
            long accessDuration;
            long rejectCount;
            long accessCount;
            long key = XmlUtils.readLongAttribute(parser, ATTR_NAME);
            int flags = AppOpsManager.extractFlagsFromKey(key) & filterFlags;
            if (flags == 0) {
                return null;
            }
            int uidState = AppOpsManager.extractUidStateFromKey(key);
            long accessCount2 = XmlUtils.readLongAttribute(parser, ATTR_ACCESS_COUNT, 0);
            if (accessCount2 > 0) {
                if (!Double.isNaN(filterScale)) {
                    accessCount = (long) AppOpsManager.HistoricalOps.round(((double) accessCount2) * filterScale);
                } else {
                    accessCount = accessCount2;
                }
                if (ops == null) {
                    ops2 = new AppOpsManager.HistoricalOps(0, 0);
                } else {
                    ops2 = ops;
                }
                ops2.increaseAccessCount(op, uid, packageName, uidState, flags, accessCount);
            } else {
                ops2 = ops;
            }
            long rejectCount2 = XmlUtils.readLongAttribute(parser, ATTR_REJECT_COUNT, 0);
            if (rejectCount2 > 0) {
                if (!Double.isNaN(filterScale)) {
                    rejectCount = (long) AppOpsManager.HistoricalOps.round(((double) rejectCount2) * filterScale);
                } else {
                    rejectCount = rejectCount2;
                }
                if (ops2 == null) {
                    ops2 = new AppOpsManager.HistoricalOps(0, 0);
                }
                ops2.increaseRejectCount(op, uid, packageName, uidState, flags, rejectCount);
            }
            long accessDuration2 = XmlUtils.readLongAttribute(parser, ATTR_ACCESS_DURATION, 0);
            if (accessDuration2 > 0) {
                if (!Double.isNaN(filterScale)) {
                    accessDuration = (long) AppOpsManager.HistoricalOps.round(((double) accessDuration2) * filterScale);
                } else {
                    accessDuration = accessDuration2;
                }
                if (ops2 == null) {
                    ops2 = new AppOpsManager.HistoricalOps(0, 0);
                }
                ops2.increaseAccessDuration(op, uid, packageName, uidState, flags, accessDuration);
            }
            return ops2;
        }

        private void writeHistoricalOpsDLocked(List<AppOpsManager.HistoricalOps> allOps, long intervalOverflowMillis, File file) throws IOException {
            FileOutputStream output = sHistoricalAppOpsDir.openWrite(file);
            try {
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(output, StandardCharsets.UTF_8.name());
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startDocument(null, true);
                serializer.startTag(null, TAG_HISTORY);
                serializer.attribute(null, ATTR_VERSION, String.valueOf(2));
                if (intervalOverflowMillis != 0) {
                    serializer.attribute(null, ATTR_OVERFLOW, Long.toString(intervalOverflowMillis));
                }
                if (allOps != null) {
                    int opsCount = allOps.size();
                    for (int i = 0; i < opsCount; i++) {
                        writeHistoricalOpDLocked(allOps.get(i), serializer);
                    }
                }
                serializer.endTag(null, TAG_HISTORY);
                serializer.endDocument();
                sHistoricalAppOpsDir.closeWrite(output);
            } catch (IOException e) {
                sHistoricalAppOpsDir.failWrite(output);
                throw e;
            }
        }

        private void writeHistoricalOpDLocked(AppOpsManager.HistoricalOps ops, XmlSerializer serializer) throws IOException {
            serializer.startTag(null, TAG_OPS);
            serializer.attribute(null, ATTR_BEGIN_TIME, Long.toString(ops.getBeginTimeMillis()));
            serializer.attribute(null, ATTR_END_TIME, Long.toString(ops.getEndTimeMillis()));
            int uidCount = ops.getUidCount();
            for (int i = 0; i < uidCount; i++) {
                writeHistoricalUidOpsDLocked(ops.getUidOpsAt(i), serializer);
            }
            serializer.endTag(null, TAG_OPS);
        }

        private void writeHistoricalUidOpsDLocked(AppOpsManager.HistoricalUidOps uidOps, XmlSerializer serializer) throws IOException {
            serializer.startTag(null, "uid");
            serializer.attribute(null, ATTR_NAME, Integer.toString(uidOps.getUid()));
            int packageCount = uidOps.getPackageCount();
            for (int i = 0; i < packageCount; i++) {
                writeHistoricalPackageOpsDLocked(uidOps.getPackageOpsAt(i), serializer);
            }
            serializer.endTag(null, "uid");
        }

        private void writeHistoricalPackageOpsDLocked(AppOpsManager.HistoricalPackageOps packageOps, XmlSerializer serializer) throws IOException {
            String opsPackageName = packageOps.getPackageName();
            if (opsPackageName != null) {
                serializer.startTag(null, "pkg");
                serializer.attribute(null, ATTR_NAME, opsPackageName);
                int opCount = packageOps.getOpCount();
                for (int i = 0; i < opCount; i++) {
                    writeHistoricalOpDLocked(packageOps.getOpAt(i), serializer);
                }
                serializer.endTag(null, "pkg");
            }
        }

        private void writeHistoricalOpDLocked(AppOpsManager.HistoricalOp op, XmlSerializer serializer) throws IOException {
            LongSparseArray keys = op.collectKeys();
            if (keys != null && keys.size() > 0) {
                serializer.startTag(null, TAG_OP);
                serializer.attribute(null, ATTR_NAME, Integer.toString(op.getOpCode()));
                int keyCount = keys.size();
                for (int i = 0; i < keyCount; i++) {
                    writeStateOnLocked(op, keys.keyAt(i), serializer);
                }
                serializer.endTag(null, TAG_OP);
            }
        }

        private void writeStateOnLocked(AppOpsManager.HistoricalOp op, long key, XmlSerializer serializer) throws IOException {
            int uidState = AppOpsManager.extractUidStateFromKey(key);
            int flags = AppOpsManager.extractFlagsFromKey(key);
            long accessCount = op.getAccessCount(uidState, uidState, flags);
            long rejectCount = op.getRejectCount(uidState, uidState, flags);
            long accessDuration = op.getAccessDuration(uidState, uidState, flags);
            if (accessCount > 0 || rejectCount > 0 || accessDuration > 0) {
                serializer.startTag(null, TAG_STATE);
                serializer.attribute(null, ATTR_NAME, Long.toString(key));
                if (accessCount > 0) {
                    serializer.attribute(null, ATTR_ACCESS_COUNT, Long.toString(accessCount));
                }
                if (rejectCount > 0) {
                    serializer.attribute(null, ATTR_REJECT_COUNT, Long.toString(rejectCount));
                }
                if (accessDuration > 0) {
                    serializer.attribute(null, ATTR_ACCESS_DURATION, Long.toString(accessDuration));
                }
                serializer.endTag(null, TAG_STATE);
            }
        }

        private static void enforceOpsWellFormed(List<AppOpsManager.HistoricalOps> ops) {
            if (ops != null) {
                AppOpsManager.HistoricalOps current = null;
                int opsCount = ops.size();
                for (int i = 0; i < opsCount; i++) {
                    current = ops.get(i);
                    if (current.isEmpty()) {
                        throw new IllegalStateException("Empty ops:\n" + opsToDebugString(ops));
                    } else if (current.getEndTimeMillis() >= current.getBeginTimeMillis()) {
                        if (current != null) {
                            if (current.getEndTimeMillis() > current.getBeginTimeMillis()) {
                                throw new IllegalStateException("Intersecting ops:\n" + opsToDebugString(ops));
                            } else if (current.getBeginTimeMillis() > current.getBeginTimeMillis()) {
                                throw new IllegalStateException("Non increasing ops:\n" + opsToDebugString(ops));
                            }
                        }
                    } else {
                        throw new IllegalStateException("Begin after end:\n" + opsToDebugString(ops));
                    }
                }
            }
        }

        private long computeGlobalIntervalBeginMillis(int depth) {
            long beginTimeMillis = 0;
            for (int i = 0; i < depth + 1; i++) {
                beginTimeMillis = (long) (((double) beginTimeMillis) + Math.pow((double) this.mIntervalCompressionMultiplier, (double) i));
            }
            return this.mBaseSnapshotInterval * beginTimeMillis;
        }

        private static AppOpsManager.HistoricalOps spliceFromEnd(AppOpsManager.HistoricalOps ops, double spliceRatio) {
            return ops.spliceFromEnd(spliceRatio);
        }

        /* access modifiers changed from: private */
        public static AppOpsManager.HistoricalOps spliceFromBeginning(AppOpsManager.HistoricalOps ops, double spliceRatio) {
            return ops.spliceFromBeginning(spliceRatio);
        }

        private static void normalizeSnapshotForSlotDuration(List<AppOpsManager.HistoricalOps> ops, long slotDurationMillis) {
            int processedIdx = ops.size() - 1;
            while (processedIdx >= 0) {
                AppOpsManager.HistoricalOps processedOp = ops.get(processedIdx);
                long slotBeginTimeMillis = Math.max(processedOp.getEndTimeMillis() - slotDurationMillis, 0L);
                for (int candidateIdx = processedIdx - 1; candidateIdx >= 0; candidateIdx--) {
                    AppOpsManager.HistoricalOps candidateOp = ops.get(candidateIdx);
                    long candidateSlotIntersectionMillis = candidateOp.getEndTimeMillis() - Math.min(slotBeginTimeMillis, processedOp.getBeginTimeMillis());
                    if (candidateSlotIntersectionMillis <= 0) {
                        break;
                    }
                    float candidateSplitRatio = ((float) candidateSlotIntersectionMillis) / ((float) candidateOp.getDurationMillis());
                    if (Float.compare(candidateSplitRatio, 1.0f) >= 0) {
                        ops.remove(candidateIdx);
                        processedIdx--;
                        processedOp.merge(candidateOp);
                    } else {
                        AppOpsManager.HistoricalOps endSplice = spliceFromEnd(candidateOp, (double) candidateSplitRatio);
                        if (endSplice != null) {
                            processedOp.merge(endSplice);
                        }
                        if (candidateOp.isEmpty()) {
                            ops.remove(candidateIdx);
                            processedIdx--;
                        }
                    }
                }
                processedIdx--;
            }
        }

        private static String opsToDebugString(List<AppOpsManager.HistoricalOps> ops) {
            StringBuilder builder = new StringBuilder();
            int opCount = ops.size();
            for (int i = 0; i < opCount; i++) {
                builder.append("  ");
                builder.append(ops.get(i));
                if (i < opCount - 1) {
                    builder.append('\n');
                }
            }
            return builder.toString();
        }

        private static Set<String> getHistoricalFileNames(File historyDir) {
            File[] files = historyDir.listFiles();
            if (files == null) {
                return Collections.emptySet();
            }
            ArraySet<String> fileNames = new ArraySet<>(files.length);
            for (File file : files) {
                fileNames.add(file.getName());
            }
            return fileNames;
        }
    }

    private static class HistoricalFilesInvariant {
        private final List<File> mBeginFiles = new ArrayList();

        private HistoricalFilesInvariant() {
        }

        public void startTracking(File folder) {
            File[] files = folder.listFiles();
            if (files != null) {
                Collections.addAll(this.mBeginFiles, files);
            }
        }

        public void stopTracking(File folder) {
            List<File> endFiles = new ArrayList<>();
            File[] files = folder.listFiles();
            if (files != null) {
                Collections.addAll(endFiles, files);
            }
            if (getOldestFileOffsetMillis(endFiles) < getOldestFileOffsetMillis(this.mBeginFiles)) {
                String message = "History loss detected!\nold files: " + this.mBeginFiles;
                HistoricalRegistry.wtf(message, null, folder);
                throw new IllegalStateException(message);
            }
        }

        private static long getOldestFileOffsetMillis(List<File> files) {
            if (files.isEmpty()) {
                return 0;
            }
            String longestName = files.get(0).getName();
            int fileCount = files.size();
            for (int i = 1; i < fileCount; i++) {
                File file = files.get(i);
                if (file.getName().length() > longestName.length()) {
                    longestName = file.getName();
                }
            }
            return Long.parseLong(longestName.replace(HistoricalRegistry.HISTORY_FILE_SUFFIX, ""));
        }
    }

    /* access modifiers changed from: private */
    public final class StringDumpVisitor implements AppOpsManager.HistoricalOpsVisitor {
        private final Date mDate = new Date();
        private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        private final String mEntryPrefix;
        private final int mFilterOp;
        private final String mFilterPackage;
        private final int mFilterUid;
        private final long mNow = System.currentTimeMillis();
        private final String mOpsPrefix;
        private final String mPackagePrefix;
        private final String mUidPrefix;
        private final String mUidStatePrefix;
        private final PrintWriter mWriter;

        StringDumpVisitor(String prefix, PrintWriter writer, int filterUid, String filterPackage, int filterOp) {
            this.mOpsPrefix = prefix + "  ";
            this.mUidPrefix = this.mOpsPrefix + "  ";
            this.mPackagePrefix = this.mUidPrefix + "  ";
            this.mEntryPrefix = this.mPackagePrefix + "  ";
            this.mUidStatePrefix = this.mEntryPrefix + "  ";
            this.mWriter = writer;
            this.mFilterUid = filterUid;
            this.mFilterPackage = filterPackage;
            this.mFilterOp = filterOp;
        }

        public void visitHistoricalOps(AppOpsManager.HistoricalOps ops) {
            this.mWriter.println();
            this.mWriter.print(this.mOpsPrefix);
            this.mWriter.println("snapshot:");
            this.mWriter.print(this.mUidPrefix);
            this.mWriter.print("begin = ");
            this.mDate.setTime(ops.getBeginTimeMillis());
            this.mWriter.print(this.mDateFormatter.format(this.mDate));
            this.mWriter.print("  (");
            TimeUtils.formatDuration(ops.getBeginTimeMillis() - this.mNow, this.mWriter);
            this.mWriter.println(")");
            this.mWriter.print(this.mUidPrefix);
            this.mWriter.print("end = ");
            this.mDate.setTime(ops.getEndTimeMillis());
            this.mWriter.print(this.mDateFormatter.format(this.mDate));
            this.mWriter.print("  (");
            TimeUtils.formatDuration(ops.getEndTimeMillis() - this.mNow, this.mWriter);
            this.mWriter.println(")");
        }

        public void visitHistoricalUidOps(AppOpsManager.HistoricalUidOps ops) {
            int i = this.mFilterUid;
            if (i == -1 || i == ops.getUid()) {
                this.mWriter.println();
                this.mWriter.print(this.mUidPrefix);
                this.mWriter.print("Uid ");
                UserHandle.formatUid(this.mWriter, ops.getUid());
                this.mWriter.println(":");
            }
        }

        public void visitHistoricalPackageOps(AppOpsManager.HistoricalPackageOps ops) {
            String str = this.mFilterPackage;
            if (str == null || str.equals(ops.getPackageName())) {
                this.mWriter.print(this.mPackagePrefix);
                this.mWriter.print("Package ");
                this.mWriter.print(ops.getPackageName());
                this.mWriter.println(":");
            }
        }

        public void visitHistoricalOp(AppOpsManager.HistoricalOp ops) {
            int keyCount;
            int i = this.mFilterOp;
            if (i == -1 || i == ops.getOpCode()) {
                this.mWriter.print(this.mEntryPrefix);
                this.mWriter.print(AppOpsManager.opToName(ops.getOpCode()));
                this.mWriter.println(":");
                LongSparseArray keys = ops.collectKeys();
                int keyCount2 = keys.size();
                int i2 = 0;
                while (i2 < keyCount2) {
                    long key = keys.keyAt(i2);
                    int uidState = AppOpsManager.extractUidStateFromKey(key);
                    int flags = AppOpsManager.extractFlagsFromKey(key);
                    boolean printedUidState = false;
                    long accessCount = ops.getAccessCount(uidState, uidState, flags);
                    if (accessCount > 0) {
                        if (0 == 0) {
                            this.mWriter.print(this.mUidStatePrefix);
                            this.mWriter.print(AppOpsManager.keyToString(key));
                            this.mWriter.print(" = ");
                            printedUidState = true;
                        }
                        this.mWriter.print("access=");
                        this.mWriter.print(accessCount);
                    }
                    long rejectCount = ops.getRejectCount(uidState, uidState, flags);
                    if (rejectCount > 0) {
                        if (!printedUidState) {
                            keyCount = keyCount2;
                            this.mWriter.print(this.mUidStatePrefix);
                            this.mWriter.print(AppOpsManager.keyToString(key));
                            this.mWriter.print(" = ");
                            printedUidState = true;
                        } else {
                            keyCount = keyCount2;
                            this.mWriter.print(", ");
                        }
                        this.mWriter.print("reject=");
                        this.mWriter.print(rejectCount);
                    } else {
                        keyCount = keyCount2;
                    }
                    long accessDuration = ops.getAccessDuration(uidState, uidState, flags);
                    if (accessDuration > 0) {
                        if (!printedUidState) {
                            this.mWriter.print(this.mUidStatePrefix);
                            this.mWriter.print(AppOpsManager.keyToString(key));
                            this.mWriter.print(" = ");
                            printedUidState = true;
                        } else {
                            this.mWriter.print(", ");
                        }
                        this.mWriter.print("duration=");
                        TimeUtils.formatDuration(accessDuration, this.mWriter);
                    }
                    if (printedUidState) {
                        this.mWriter.println("");
                    }
                    i2++;
                    keys = keys;
                    keyCount2 = keyCount;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0082, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0087, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0088, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008b, code lost:
        throw r3;
     */
    public static void wtf(String message, Throwable t, File storage) {
        Slog.wtf(LOG_TAG, message, t);
        if (KEEP_WTF_LOG) {
            try {
                File file = new File(Environment.getDataSystemDirectory(), "appops");
                File file2 = new File(file, "wtf" + TimeUtils.formatForLogging(System.currentTimeMillis()));
                if (file2.createNewFile()) {
                    PrintWriter writer = new PrintWriter(file2);
                    if (t != null) {
                        writer.append('\n').append((CharSequence) t.toString());
                    }
                    writer.append('\n').append((CharSequence) Debug.getCallers(10));
                    if (storage != null) {
                        writer.append((CharSequence) ("\nfiles: " + Arrays.toString(storage.listFiles())));
                    } else {
                        writer.append((CharSequence) "\nfiles: none");
                    }
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
