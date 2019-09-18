package com.android.server.backup;

import android.app.INotificationManager;
import android.app.backup.BlobBackupHelper;
import android.content.Context;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;

public class NotificationBackupHelper extends BlobBackupHelper {
    static final int BLOB_VERSION = 1;
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String KEY_NOTIFICATIONS = "notifications";
    static final String TAG = "NotifBackupHelper";

    public NotificationBackupHelper(Context context) {
        super(1, new String[]{KEY_NOTIFICATIONS});
    }

    /* access modifiers changed from: protected */
    public byte[] getBackupPayload(String key) {
        if (!KEY_NOTIFICATIONS.equals(key)) {
            return null;
        }
        try {
            return INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).getBackupPayload(0);
        } catch (Exception e) {
            Slog.e(TAG, "Couldn't communicate with notification manager");
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void applyRestoredPayload(String key, byte[] payload) {
        if (DEBUG) {
            Slog.v(TAG, "Got restore of " + key);
        }
        if (KEY_NOTIFICATIONS.equals(key)) {
            try {
                INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).applyRestore(payload, 0);
            } catch (Exception e) {
                Slog.e(TAG, "Couldn't communicate with notification manager");
            }
        }
    }
}
