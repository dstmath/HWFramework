package android.database.sqlite;

import android.content.res.Resources;
import android.os.StatFs;
import android.os.SystemProperties;
import com.android.internal.R;

public final class SQLiteGlobal {
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
        int i;
        synchronized (sLock) {
            if (sDefaultPageSize == 0) {
                sDefaultPageSize = new StatFs("/data").getBlockSize();
            }
            i = SystemProperties.getInt("debug.sqlite.pagesize", sDefaultPageSize);
        }
        return i;
    }

    public static String getDefaultJournalMode() {
        return SystemProperties.get("debug.sqlite.journalmode", Resources.getSystem().getString(R.string.db_default_journal_mode));
    }

    public static boolean isCompatibilityWalSupported() {
        return SystemProperties.getBoolean("debug.sqlite.compatibility_wal_supported", Resources.getSystem().getBoolean(R.bool.db_compatibility_wal_supported));
    }

    public static int getJournalSizeLimit() {
        return SystemProperties.getInt("debug.sqlite.journalsizelimit", Resources.getSystem().getInteger(R.integer.db_journal_size_limit));
    }

    public static String getDefaultSyncMode() {
        return SystemProperties.get("debug.sqlite.syncmode", Resources.getSystem().getString(R.string.db_default_sync_mode));
    }

    public static String getWALSyncMode() {
        return SystemProperties.get("debug.sqlite.wal.syncmode", Resources.getSystem().getString(R.string.db_wal_sync_mode));
    }

    public static int getWALAutoCheckpoint() {
        return Math.max(1, SystemProperties.getInt("debug.sqlite.wal.autocheckpoint", Resources.getSystem().getInteger(R.integer.db_wal_autocheckpoint)));
    }

    public static int getWALConnectionPoolSize() {
        return Math.max(2, SystemProperties.getInt("debug.sqlite.wal.poolsize", Resources.getSystem().getInteger(R.integer.db_connection_pool_size)));
    }

    public static int getIdleConnectionTimeout() {
        return SystemProperties.getInt("debug.sqlite.idle_connection_timeout", Resources.getSystem().getInteger(R.integer.db_default_idle_connection_timeout));
    }
}
