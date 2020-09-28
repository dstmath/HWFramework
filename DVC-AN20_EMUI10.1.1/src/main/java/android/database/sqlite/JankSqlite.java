package android.database.sqlite;

import android.os.Looper;
import android.os.SystemProperties;
import android.util.Jlog;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JankSqlite {
    private static final Pattern JANKDBCFG_PATTERN = Pattern.compile("<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}<{1}(\\d+)>{1}");
    private static final String TAG = "JankSqlite";
    private static long sDeleteLimit = 400;
    private static long sExecsqlLimit = 400;
    private static long sInsertLimit = 400;
    private static long sJankSamplingNum = 20;
    private static long sQueryLimit = 400;
    private static long sUpdateLimit = 400;
    private DbOprMonitor mDelete = new DbOprMonitor(sDeleteLimit, 65);
    private DbOprMonitor mExecsql = new DbOprMonitor(sExecsqlLimit, 66);
    private DbOprMonitor mInsert = new DbOprMonitor(sInsertLimit, 62);
    private DbOprMonitor mQuery = new DbOprMonitor(sQueryLimit, 64);
    private DbOprMonitor mUpdate = new DbOprMonitor(sUpdateLimit, 63);

    static {
        getProp();
    }

    /* access modifiers changed from: private */
    public static class DbOprMonitor {
        private boolean mIsDbCircle = false;
        private long mLimit = 0;
        private int mLogId;
        private long mMaxtime = 0;
        private int mNextIndex = 0;
        private long[] mOprtimes = null;
        private long mTotaltime = 0;

        DbOprMonitor(long limit, int logId) {
            this.mLimit = limit;
            if (JankSqlite.sJankSamplingNum > 0) {
                this.mOprtimes = new long[((int) JankSqlite.sJankSamplingNum)];
                this.mLogId = logId;
                this.mIsDbCircle = false;
            }
        }

        public synchronized boolean addOpr(long tl, String table, String dbname) {
            if (this.mOprtimes == null) {
                return false;
            }
            if (this.mIsDbCircle) {
                this.mTotaltime -= this.mOprtimes[this.mNextIndex];
            }
            if (this.mMaxtime < tl) {
                this.mMaxtime = tl;
            }
            this.mOprtimes[this.mNextIndex] = tl;
            this.mNextIndex++;
            if (((long) this.mNextIndex) >= JankSqlite.sJankSamplingNum) {
                this.mNextIndex = 0;
                this.mIsDbCircle = true;
            }
            this.mTotaltime += tl;
            if (this.mIsDbCircle) {
                long averagetl = this.mTotaltime / JankSqlite.sJankSamplingNum;
                if (averagetl > this.mLimit) {
                    boolean mIsDbUiThread = Looper.myLooper() == Looper.getMainLooper();
                    if (mIsDbUiThread) {
                        Jlog.d(this.mLogId, dbname, (int) averagetl, "<" + this.mMaxtime + ">,table(" + table + "),database(" + dbname + "),(" + mIsDbUiThread + ")");
                    }
                    this.mIsDbCircle = false;
                    this.mNextIndex = 0;
                    this.mTotaltime = 0;
                    this.mMaxtime = 0;
                }
            }
            return true;
        }
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

    private static synchronized boolean getProp() {
        synchronized (JankSqlite.class) {
            Matcher matcher = JANKDBCFG_PATTERN.matcher(SystemProperties.get("persist.sys.jankdb", "<20><400><400><400><400><400>"));
            if (matcher.matches()) {
                try {
                    sJankSamplingNum = (long) Integer.parseInt(matcher.group(1));
                    sInsertLimit = (long) Integer.parseInt(matcher.group(2));
                    sUpdateLimit = (long) Integer.parseInt(matcher.group(3));
                    sQueryLimit = (long) Integer.parseInt(matcher.group(4));
                    sDeleteLimit = (long) Integer.parseInt(matcher.group(5));
                    sExecsqlLimit = (long) Integer.parseInt(matcher.group(6));
                } catch (NumberFormatException e) {
                    Log.v(TAG, "parseInt has Exception");
                }
            }
        }
        return true;
    }
}
