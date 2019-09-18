package com.huawei.hwsqlite;

import android.util.Log;
import android.util.Printer;
import java.util.ArrayList;

public final class SQLiteDebug {
    public static final boolean DEBUG_SQL_LOG = Log.isLoggable("SQLiteLog", 2);
    public static final boolean DEBUG_SQL_STATEMENTS = Log.isLoggable("SQLiteStatements", 2);
    public static final boolean DEBUG_SQL_TIME = Log.isLoggable("SQLiteTime", 2);

    public static class DbStats {
        public String cache;
        public String dbName;
        public long dbSize;
        public int lookaside;
        public long pageSize;

        public DbStats(String dbName2, long pageCount, long pageSize2, int lookaside2, int hits, int misses, int cachesize) {
            this.dbName = dbName2;
            this.pageSize = pageSize2 / 1024;
            this.dbSize = (pageCount * pageSize2) / 1024;
            this.lookaside = lookaside2;
            this.cache = hits + "/" + misses + "/" + cachesize;
        }
    }

    public static class PagerStats {
        public ArrayList<DbStats> dbStats;
        public int largestMemAlloc;
        public int memoryUsed;
        public int pageCacheOverflow;
    }

    private static native void nativeGetPagerStats(PagerStats pagerStats);

    private SQLiteDebug() {
    }

    public static final boolean shouldLogSlowQuery(long elapsedTimeMillis) {
        int slowQueryMillis = SQLiteGlobal.getSlowQueryThreshold();
        return slowQueryMillis >= 0 && elapsedTimeMillis >= ((long) slowQueryMillis);
    }

    public static PagerStats getDatabaseInfo() {
        PagerStats stats = new PagerStats();
        nativeGetPagerStats(stats);
        stats.dbStats = SQLiteDatabase.getDbStats();
        return stats;
    }

    public static void dump(Printer printer, String[] args) {
        boolean verbose = false;
        for (String arg : args) {
            if (arg.equals("-v")) {
                verbose = true;
            }
        }
        SQLiteDatabase.dumpAll(printer, verbose);
    }
}
