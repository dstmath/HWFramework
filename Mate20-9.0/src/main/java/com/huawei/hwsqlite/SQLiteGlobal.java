package com.huawei.hwsqlite;

import android.annotation.SuppressLint;
import com.huawei.systemmanager.power.HwHistoryItem;

public final class SQLiteGlobal {
    private static int SLOW_QUERY_THRESHOLD = -1;
    private static final String TAG = "SQLiteGlobal";
    private static int sDefaultPageSize;
    private static final Object sLock = new Object();

    private static native int nativeReleaseMemory();

    private SQLiteGlobal() {
    }

    public static int releaseMemory() {
        return nativeReleaseMemory();
    }

    public static int getDefaultPageSize() {
        synchronized (sLock) {
        }
        return 4096;
    }

    public static String getDefaultJournalMode() {
        return "TRUNCATE";
    }

    public static int getJournalSizeLimit() {
        return HwHistoryItem.STATE_BATTERY_PLUGGED_FLAG;
    }

    public static String getDefaultSyncMode() {
        return "FULL";
    }

    public static String getWALSyncMode() {
        return "FULL";
    }

    @SuppressLint({"AvoidMax/Min"})
    public static int getWALAutoCheckpoint() {
        return Math.max(1, 100);
    }

    @SuppressLint({"AvoidMax/Min"})
    public static int getWALConnectionPoolSize() {
        return Math.max(2, 4);
    }

    public static int getSlowQueryThreshold() {
        return SLOW_QUERY_THRESHOLD;
    }

    public static void setSlowQueryThreshold(int millis) {
        SLOW_QUERY_THRESHOLD = millis;
    }
}
