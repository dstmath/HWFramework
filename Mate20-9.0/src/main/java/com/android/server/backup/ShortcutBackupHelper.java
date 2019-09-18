package com.android.server.backup;

import android.app.backup.BlobBackupHelper;
import android.content.pm.IShortcutService;
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
        return IShortcutService.Stub.asInterface(ServiceManager.getService("shortcut"));
    }

    /* access modifiers changed from: protected */
    public byte[] getBackupPayload(String key) {
        if (((key.hashCode() == -792920646 && key.equals(KEY_USER_FILE)) ? (char) 0 : 65535) != 0) {
            Slog.w(TAG, "Unknown key: " + key);
        } else {
            try {
                return getShortcutService().getBackupPayload(0);
            } catch (Exception e) {
                Slog.wtf(TAG, "Backup failed", e);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void applyRestoredPayload(String key, byte[] payload) {
        if (((key.hashCode() == -792920646 && key.equals(KEY_USER_FILE)) ? (char) 0 : 65535) != 0) {
            Slog.w(TAG, "Unknown key: " + key);
            return;
        }
        try {
            getShortcutService().applyRestore(payload, 0);
        } catch (Exception e) {
            Slog.wtf(TAG, "Restore failed", e);
        }
    }
}
