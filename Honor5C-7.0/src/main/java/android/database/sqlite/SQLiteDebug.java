package android.database.sqlite;

import android.os.SystemProperties;
import android.os.Trace;
import android.util.Printer;
import java.util.ArrayList;

public final class SQLiteDebug {
    public static final boolean DEBUG_LOG_SLOW_QUERIES = false;
    public static final boolean DEBUG_SQL_LOG = false;
    public static final boolean DEBUG_SQL_STATEMENTS = false;
    public static final boolean DEBUG_SQL_TIME = false;

    public static class DbStats {
        public String cache;
        public String dbName;
        public long dbSize;
        public int lookaside;
        public long pageSize;

        public DbStats(String dbName, long pageCount, long pageSize, int lookaside, int hits, int misses, int cachesize) {
            this.dbName = dbName;
            this.pageSize = pageSize / Trace.TRACE_TAG_CAMERA;
            this.dbSize = (pageCount * pageSize) / Trace.TRACE_TAG_CAMERA;
            this.lookaside = lookaside;
            this.cache = hits + "/" + misses + "/" + cachesize;
        }
    }

    public static class PagerStats {
        public ArrayList<DbStats> dbStats;
        public int largestMemAlloc;
        public int memoryUsed;
        public int pageCacheOverflow;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteDebug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteDebug.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteDebug.<clinit>():void");
    }

    private static native void nativeGetPagerStats(PagerStats pagerStats);

    private SQLiteDebug() {
    }

    public static final boolean shouldLogSlowQuery(long elapsedTimeMillis) {
        int slowQueryMillis = SystemProperties.getInt("db.log.slow_query_threshold", -1);
        if (slowQueryMillis < 0 || elapsedTimeMillis < ((long) slowQueryMillis)) {
            return false;
        }
        return true;
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
