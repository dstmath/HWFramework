package com.android.server.backup;

import android.app.AppGlobals;
import android.app.backup.BlobBackupHelper;
import android.content.pm.IPackageManager;
import android.util.Slog;

public class PreferredActivityBackupHelper extends BlobBackupHelper {
    private static final boolean DEBUG = false;
    private static final String KEY_DEFAULT_APPS = "default-apps";
    private static final String KEY_INTENT_VERIFICATION = "intent-verification";
    private static final String KEY_PREFERRED = "preferred-activity";
    private static final int STATE_VERSION = 3;
    private static final String TAG = "PreferredBackup";

    public PreferredActivityBackupHelper() {
        super(3, new String[]{KEY_PREFERRED, KEY_DEFAULT_APPS, KEY_INTENT_VERIFICATION});
    }

    protected byte[] getBackupPayload(String key) {
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            if (key.equals(KEY_PREFERRED)) {
                return pm.getPreferredActivityBackup(0);
            }
            if (key.equals(KEY_DEFAULT_APPS)) {
                return pm.getDefaultAppsBackup(0);
            }
            if (key.equals(KEY_INTENT_VERIFICATION)) {
                return pm.getIntentFilterVerificationBackup(0);
            }
            Slog.w(TAG, "Unexpected backup key " + key);
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "Unable to store payload " + key);
        }
    }

    protected void applyRestoredPayload(String key, byte[] payload) {
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            if (key.equals(KEY_PREFERRED)) {
                pm.restorePreferredActivities(payload, 0);
            } else if (key.equals(KEY_DEFAULT_APPS)) {
                pm.restoreDefaultApps(payload, 0);
            } else if (key.equals(KEY_INTENT_VERIFICATION)) {
                pm.restoreIntentFilterVerification(payload, 0);
            } else {
                Slog.w(TAG, "Unexpected restore key " + key);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Unable to restore key " + key);
        }
    }
}
