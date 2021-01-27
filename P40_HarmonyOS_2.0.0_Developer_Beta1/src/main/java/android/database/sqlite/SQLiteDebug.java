package android.database.sqlite;

import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Printer;
import java.util.ArrayList;

public final class SQLiteDebug {

    public static class PagerStats {
        public ArrayList<DbStats> dbStats;
        public int largestMemAlloc;
        public int memoryUsed;
        public int pageCacheOverflow;
    }

    private static native void nativeGetPagerStats(PagerStats pagerStats);

    public static final class NoPreloadHolder {
        public static final boolean DEBUG_LOG_DETAILED;
        public static final boolean DEBUG_LOG_SLOW_QUERIES = Build.IS_DEBUGGABLE;
        public static final boolean DEBUG_SQL_LOG = Log.isLoggable("SQLiteLog", 2);
        public static final boolean DEBUG_SQL_STATEMENTS = Log.isLoggable("SQLiteStatements", 2);
        public static final boolean DEBUG_SQL_TIME = Log.isLoggable("SQLiteTime", 2);
        private static final String SLOW_QUERY_THRESHOLD_PROP = "db.log.slow_query_threshold";
        private static final String SLOW_QUERY_THRESHOLD_UID_PROP = ("db.log.slow_query_threshold." + Process.myUid());

        static {
            boolean z = false;
            if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("db.log.detailed", false)) {
                z = true;
            }
            DEBUG_LOG_DETAILED = z;
        }
    }

    private SQLiteDebug() {
    }

    public static boolean shouldLogSlowQuery(long elapsedTimeMillis) {
        return elapsedTimeMillis >= ((long) Math.min(SystemProperties.getInt("db.log.slow_query_threshold", Integer.MAX_VALUE), SystemProperties.getInt(NoPreloadHolder.SLOW_QUERY_THRESHOLD_UID_PROP, Integer.MAX_VALUE)));
    }

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

    public static PagerStats getDatabaseInfo() {
        PagerStats stats = new PagerStats();
        nativeGetPagerStats(stats);
        stats.dbStats = SQLiteDatabase.getDbStats();
        return stats;
    }

    public static void dump(Printer printer, String[] args) {
        dump(printer, args, false);
    }

    public static void dump(Printer printer, String[] args, boolean isSystem) {
        boolean verbose = false;
        for (String arg : args) {
            if (arg.equals("-v")) {
                verbose = true;
            }
        }
        SQLiteDatabase.dumpAll(printer, verbose, isSystem);
    }
}
