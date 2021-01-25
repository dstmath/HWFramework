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
        super(3, KEY_PREFERRED, KEY_DEFAULT_APPS, KEY_INTENT_VERIFICATION);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003f A[Catch:{ Exception -> 0x0067 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0062 A[Catch:{ Exception -> 0x0067 }] */
    @Override // android.app.backup.BlobBackupHelper
    public byte[] getBackupPayload(String key) {
        IPackageManager pm = AppGlobals.getPackageManager();
        char c = 65535;
        try {
            int hashCode = key.hashCode();
            if (hashCode != -696985986) {
                if (hashCode != -429170260) {
                    if (hashCode == 1336142555 && key.equals(KEY_PREFERRED)) {
                        c = 0;
                        if (c != 0) {
                            return pm.getPreferredActivityBackup(0);
                        }
                        if (c == 1) {
                            return pm.getDefaultAppsBackup(0);
                        }
                        if (c == 2) {
                            return pm.getIntentFilterVerificationBackup(0);
                        }
                        Slog.w(TAG, "Unexpected backup key " + key);
                        return null;
                    }
                } else if (key.equals(KEY_INTENT_VERIFICATION)) {
                    c = 2;
                    if (c != 0) {
                    }
                }
            } else if (key.equals(KEY_DEFAULT_APPS)) {
                c = 1;
                if (c != 0) {
                }
            }
            if (c != 0) {
            }
        } catch (Exception e) {
            Slog.e(TAG, "Unable to store payload " + key);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003f A[Catch:{ Exception -> 0x0065 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0060 A[Catch:{ Exception -> 0x0065 }] */
    @Override // android.app.backup.BlobBackupHelper
    public void applyRestoredPayload(String key, byte[] payload) {
        IPackageManager pm = AppGlobals.getPackageManager();
        char c = 65535;
        try {
            int hashCode = key.hashCode();
            if (hashCode != -696985986) {
                if (hashCode != -429170260) {
                    if (hashCode == 1336142555 && key.equals(KEY_PREFERRED)) {
                        c = 0;
                        if (c != 0) {
                            pm.restorePreferredActivities(payload, 0);
                            return;
                        } else if (c == 1) {
                            pm.restoreDefaultApps(payload, 0);
                            return;
                        } else if (c != 2) {
                            Slog.w(TAG, "Unexpected restore key " + key);
                            return;
                        } else {
                            pm.restoreIntentFilterVerification(payload, 0);
                            return;
                        }
                    }
                } else if (key.equals(KEY_INTENT_VERIFICATION)) {
                    c = 2;
                    if (c != 0) {
                    }
                }
            } else if (key.equals(KEY_DEFAULT_APPS)) {
                c = 1;
                if (c != 0) {
                }
            }
            if (c != 0) {
            }
        } catch (Exception e) {
            Slog.w(TAG, "Unable to restore key " + key);
        }
    }
}
