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

    /* access modifiers changed from: protected */
    public byte[] getBackupPayload(String key) {
        IPackageManager pm = AppGlobals.getPackageManager();
        char c = 65535;
        try {
            int hashCode = key.hashCode();
            if (hashCode != -696985986) {
                if (hashCode != -429170260) {
                    if (hashCode == 1336142555) {
                        if (key.equals(KEY_PREFERRED)) {
                            c = 0;
                        }
                    }
                } else if (key.equals(KEY_INTENT_VERIFICATION)) {
                    c = 2;
                }
            } else if (key.equals(KEY_DEFAULT_APPS)) {
                c = 1;
            }
            switch (c) {
                case 0:
                    return pm.getPreferredActivityBackup(0);
                case 1:
                    return pm.getDefaultAppsBackup(0);
                case 2:
                    return pm.getIntentFilterVerificationBackup(0);
                default:
                    Slog.w(TAG, "Unexpected backup key " + key);
                    break;
            }
        } catch (Exception e) {
            Slog.e(TAG, "Unable to store payload " + key);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void applyRestoredPayload(String key, byte[] payload) {
        IPackageManager pm = AppGlobals.getPackageManager();
        char c = 65535;
        try {
            int hashCode = key.hashCode();
            if (hashCode != -696985986) {
                if (hashCode != -429170260) {
                    if (hashCode == 1336142555) {
                        if (key.equals(KEY_PREFERRED)) {
                            c = 0;
                        }
                    }
                } else if (key.equals(KEY_INTENT_VERIFICATION)) {
                    c = 2;
                }
            } else if (key.equals(KEY_DEFAULT_APPS)) {
                c = 1;
            }
            switch (c) {
                case 0:
                    pm.restorePreferredActivities(payload, 0);
                    return;
                case 1:
                    pm.restoreDefaultApps(payload, 0);
                    return;
                case 2:
                    pm.restoreIntentFilterVerification(payload, 0);
                    return;
                default:
                    Slog.w(TAG, "Unexpected restore key " + key);
                    return;
            }
        } catch (Exception e) {
            Slog.w(TAG, "Unable to restore key " + key);
        }
    }
}
