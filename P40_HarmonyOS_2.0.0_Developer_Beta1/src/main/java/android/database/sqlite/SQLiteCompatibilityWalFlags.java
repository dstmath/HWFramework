package android.database.sqlite;

import android.app.ActivityThread;
import android.app.Application;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.KeyValueListParser;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;

public class SQLiteCompatibilityWalFlags {
    private static final String TAG = "SQLiteCompatibilityWalFlags";
    private static volatile boolean sCallingGlobalSettings;
    private static volatile boolean sInitialized;
    private static volatile boolean sLegacyCompatibilityWalEnabled;
    private static volatile long sTruncateSize = -1;
    private static volatile String sWALSyncMode;

    private SQLiteCompatibilityWalFlags() {
    }

    @VisibleForTesting
    public static boolean isLegacyCompatibilityWalEnabled() {
        initIfNeeded();
        return sLegacyCompatibilityWalEnabled;
    }

    @VisibleForTesting
    public static String getWALSyncMode() {
        initIfNeeded();
        if (sLegacyCompatibilityWalEnabled) {
            return sWALSyncMode;
        }
        throw new IllegalStateException("isLegacyCompatibilityWalEnabled() == false");
    }

    @VisibleForTesting
    public static long getTruncateSize() {
        initIfNeeded();
        return sTruncateSize;
    }

    /* JADX INFO: finally extract failed */
    private static void initIfNeeded() {
        if (!sInitialized && !sCallingGlobalSettings) {
            ActivityThread activityThread = ActivityThread.currentActivityThread();
            Application app = activityThread == null ? null : activityThread.getApplication();
            String flags = null;
            if (app == null) {
                Log.w(TAG, "Cannot read global setting sqlite_compatibility_wal_flags - Application state not available");
            } else {
                try {
                    sCallingGlobalSettings = true;
                    flags = Settings.Global.getString(app.getContentResolver(), Settings.Global.SQLITE_COMPATIBILITY_WAL_FLAGS);
                    sCallingGlobalSettings = false;
                } catch (Throwable th) {
                    sCallingGlobalSettings = false;
                    throw th;
                }
            }
            init(flags);
        }
    }

    @VisibleForTesting
    public static void init(String flags) {
        if (TextUtils.isEmpty(flags)) {
            sInitialized = true;
            return;
        }
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(flags);
            sLegacyCompatibilityWalEnabled = parser.getBoolean("legacy_compatibility_wal_enabled", false);
            sWALSyncMode = parser.getString("wal_syncmode", SQLiteGlobal.getWALSyncMode());
            sTruncateSize = (long) parser.getInt("truncate_size", -1);
            Log.i(TAG, "Read compatibility WAL flags: legacy_compatibility_wal_enabled=" + sLegacyCompatibilityWalEnabled + ", wal_syncmode=" + sWALSyncMode);
            sInitialized = true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Setting has invalid format: " + flags, e);
            sInitialized = true;
        }
    }

    @VisibleForTesting
    public static void reset() {
        sInitialized = false;
        sLegacyCompatibilityWalEnabled = false;
        sWALSyncMode = null;
    }
}
