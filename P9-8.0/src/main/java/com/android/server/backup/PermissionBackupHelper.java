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

    protected byte[] getBackupPayload(String key) {
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            if (key.equals(KEY_PERMISSIONS)) {
                return pm.getPermissionGrantBackup(0);
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
            if (key.equals(KEY_PERMISSIONS)) {
                pm.restorePermissionGrants(payload, 0);
            } else {
                Slog.w(TAG, "Unexpected restore key " + key);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Unable to restore key " + key);
        }
    }
}
