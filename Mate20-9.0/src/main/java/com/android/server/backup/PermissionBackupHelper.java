package com.android.server.backup;

import android.app.AppGlobals;
import android.app.backup.BlobBackupHelper;
import android.content.pm.IPackageManager;
import android.util.Slog;

public class PermissionBackupHelper extends BlobBackupHelper {
    private static final boolean DEBUG = false;
    private static final String KEY_PERMISSIONS = "permissions";
    private static final int STATE_VERSION = 1;
    private static final String TAG = "PermissionBackup";

    public PermissionBackupHelper() {
        super(1, new String[]{KEY_PERMISSIONS});
    }

    /* access modifiers changed from: protected */
    public byte[] getBackupPayload(String key) {
        IPackageManager pm = AppGlobals.getPackageManager();
        char c = 65535;
        try {
            if (key.hashCode() == 1133704324) {
                if (key.equals(KEY_PERMISSIONS)) {
                    c = 0;
                }
            }
            if (c == 0) {
                return pm.getPermissionGrantBackup(0);
            }
            Slog.w(TAG, "Unexpected backup key " + key);
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "Unable to store payload " + key);
        }
    }

    /* access modifiers changed from: protected */
    public void applyRestoredPayload(String key, byte[] payload) {
        IPackageManager pm = AppGlobals.getPackageManager();
        char c = 65535;
        try {
            if (key.hashCode() == 1133704324) {
                if (key.equals(KEY_PERMISSIONS)) {
                    c = 0;
                }
            }
            if (c != 0) {
                Slog.w(TAG, "Unexpected restore key " + key);
                return;
            }
            pm.restorePermissionGrants(payload, 0);
        } catch (Exception e) {
            Slog.w(TAG, "Unable to restore key " + key);
        }
    }
}
