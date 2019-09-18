package android.database.sqlite;

import android.os.Looper;
import android.os.SystemProperties;
import android.util.Jlog;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JankSqlite {
    private static final Pattern JANKDBCFG_PATTERN = Pattern.compile("<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}");
    private static long mDeleteLimit = 400;
    private static long mExecsqlLimit = 400;
    private static long mInsertLimit = 400;
    /* access modifiers changed from: private */
    public static long mJankSamplingNum = 20;
    private static long mQueryLimit = 400;
    private static long mUpdateLimit = 400;
    private DBOprMonitor mDelete = new DBOprMonitor(mDeleteLimit, 65);
    private DBOprMonitor mExecsql = new DBOprMonitor(mExecsqlLimit, 66);
    private DBOprMonitor mInsert = new DBOprMonitor(mInsertLimit, 62);
    private DBOprMonitor mQuery = new DBOprMonitor(mQueryLimit, 64);
    private DBOprMonitor mUpdate = new DBOprMonitor(mUpdateLimit, 63);

    private static class DBOprMonitor {
        private long mLimit = 0;
        private int mLogID;
        private long mMaxtime = 0;
        private int mNextIndex = 0;
        private long[] mOprtimes = null;
        private long mTotaltime = 0;
        private boolean mbCircle = false;

        public DBOprMonitor(long limit, int logid) {
            this.mLimit = limit;
            if (JankSqlite.mJankSamplingNum > 0) {
                this.mOprtimes = new long[((int) JankSqlite.mJankSamplingNum)];
                this.mLogID = logid;
                this.mbCircle = false;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a0, code lost:
            return true;
         */
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
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        Jlog.d(this.mLogID, dbname, (int) averagetl, "<" + this.mMaxtime + ">,table(" + table + "),database(" + dbname + "),(" + bUIThread + ")");
                    }
                    this.mbCircle = false;
                    this.mNextIndex = 0;
                    this.mTotaltime = 0;
                    this.mMaxtime = 0;
                }
            }
        }
    }

    static {
        getProp();
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
