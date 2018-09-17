package com.android.server.backup;

import android.app.backup.BlobBackupHelper;
import android.content.pm.IShortcutService;
import android.content.pm.IShortcutService.Stub;
import android.os.ServiceManager;
import android.util.Slog;

public class ShortcutBackupHelper extends BlobBackupHelper {
    private static final int BLOB_VERSION = 1;
    private static final String KEY_USER_FILE = "shortcutuser.xml";
    private static final String TAG = "ShortcutBackupAgent";

    public ShortcutBackupHelper() {
        super(1, new String[]{KEY_USER_FILE});
    }

    private IShortcutService getShortcutService() {
        return Stub.asInterface(ServiceManager.getService("shortcut"));
    }

    protected byte[] getBackupPayload(String key) {
        if (key.equals(KEY_USER_FILE)) {
            try {
                return getShortcutService().getBackupPayload(0);
            } catch (Exception e) {
                Slog.wtf(TAG, "Backup failed", e);
            }
        } else {
            Slog.w(TAG, "Unknown key: " + key);
            return null;
        }
    }

    protected void applyRestoredPayload(String key, byte[] payload) {
        if (key.equals(KEY_USER_FILE)) {
            try {
                getShortcutService().applyRestore(payload, 0);
                return;
            } catch (Exception e) {
                Slog.wtf(TAG, "Restore failed", e);
                return;
            }
        }
        Slog.w(TAG, "Unknown key: " + key);
    }
}
