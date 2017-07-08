package android.database.sqlite;

import android.os.Looper;
import android.os.SystemProperties;
import android.util.Jlog;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JankSqlite {
    private static final Pattern JANKDBCFG_PATTERN = null;
    private static long mDeleteLimit;
    private static long mExecsqlLimit;
    private static long mInsertLimit;
    private static long mJankSamplingNum;
    private static long mQueryLimit;
    private static long mUpdateLimit;
    private DBOprMonitor mDelete;
    private DBOprMonitor mExecsql;
    private DBOprMonitor mInsert;
    private DBOprMonitor mQuery;
    private DBOprMonitor mUpdate;

    private static class DBOprMonitor {
        private long mLimit;
        private int mLogID;
        private long mMaxtime;
        private int mNextIndex;
        private long[] mOprtimes;
        private long mTotaltime;
        private boolean mbCircle;

        public DBOprMonitor(long limit, int logid) {
            this.mbCircle = false;
            this.mNextIndex = 0;
            this.mOprtimes = null;
            this.mTotaltime = 0;
            this.mMaxtime = 0;
            this.mLimit = 0;
            this.mLimit = limit;
            if (JankSqlite.mJankSamplingNum > 0) {
                this.mOprtimes = new long[((int) JankSqlite.mJankSamplingNum)];
                this.mLogID = logid;
                this.mbCircle = false;
            }
        }

        public synchronized boolean addOpr(long tl, String table, String dbname) {
            if (this.mOprtimes == null) {
                return false;
            }
            if (this.mbCircle) {
                this.mTotaltime -= this.mOprtimes[this.mNextIndex];
            }
            if (this.mMaxtime < tl) {
                this.mMaxtime = tl;
            }
            this.mOprtimes[this.mNextIndex] = tl;
            this.mNextIndex++;
            if (((long) this.mNextIndex) >= JankSqlite.mJankSamplingNum) {
                this.mNextIndex = 0;
                this.mbCircle = true;
            }
            this.mTotaltime += tl;
            if (this.mbCircle) {
                long averagetl = this.mTotaltime / JankSqlite.mJankSamplingNum;
                if (averagetl > this.mLimit) {
                    Jlog.d(this.mLogID, dbname, (int) averagetl, "<" + this.mMaxtime + ">,table(" + table + "),database(" + dbname + "),(" + (Looper.myLooper() == Looper.getMainLooper()) + ")");
                    this.mbCircle = false;
                    this.mNextIndex = 0;
                    this.mTotaltime = 0;
                    this.mMaxtime = 0;
                }
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.JankSqlite.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.JankSqlite.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.JankSqlite.<clinit>():void");
    }

    public JankSqlite() {
        this.mInsert = new DBOprMonitor(mInsertLimit, 62);
        this.mUpdate = new DBOprMonitor(mUpdateLimit, 63);
        this.mQuery = new DBOprMonitor(mQueryLimit, 64);
        this.mDelete = new DBOprMonitor(mDeleteLimit, 65);
        this.mExecsql = new DBOprMonitor(mExecsqlLimit, 66);
    }

    public boolean addInsert(long tl, String table, String dbname) {
        return this.mInsert.addOpr(tl, table, dbname);
    }

    public boolean addUpdate(long tl, String table, String dbname) {
        return this.mUpdate.addOpr(tl, table, dbname);
    }

    public boolean addQuery(long tl, String table, String dbname) {
        return this.mQuery.addOpr(tl, table, dbname);
    }

    public boolean addDelete(long tl, String table, String dbname) {
        return this.mDelete.addOpr(tl, table, dbname);
    }

    public boolean addExecsql(long tl, String table, String dbname) {
        return this.mExecsql.addOpr(tl, table, dbname);
    }

    public static synchronized boolean getProp() {
        synchronized (JankSqlite.class) {
            Matcher m = JANKDBCFG_PATTERN.matcher(SystemProperties.get("persist.sys.jankdb", "<20><400><400><400><400><400>"));
            if (m.matches()) {
                mJankSamplingNum = (long) Integer.parseInt(m.group(1));
                mInsertLimit = (long) Integer.parseInt(m.group(2));
                mUpdateLimit = (long) Integer.parseInt(m.group(3));
                mQueryLimit = (long) Integer.parseInt(m.group(4));
                mDeleteLimit = (long) Integer.parseInt(m.group(5));
                mExecsqlLimit = (long) Integer.parseInt(m.group(6));
            }
        }
        return true;
    }
}
