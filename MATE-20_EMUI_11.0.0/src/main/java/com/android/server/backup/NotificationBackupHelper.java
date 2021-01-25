package com.android.server.backup;

import android.app.INotificationManager;
import android.app.backup.BlobBackupHelper;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;

public class NotificationBackupHelper extends BlobBackupHelper {
    static final int BLOB_VERSION = 1;
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String KEY_NOTIFICATIONS = "notifications";
    static final String TAG = "NotifBackupHelper";
    private final int mUserId;

    public NotificationBackupHelper(int userId) {
        super(1, KEY_NOTIFICATIONS);
        this.mUserId = userId;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.backup.BlobBackupHelper
    public byte[] getBackupPayload(String key) {
        if (!KEY_NOTIFICATIONS.equals(key)) {
            return null;
        }
        try {
            return INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).getBackupPayload(this.mUserId);
        } catch (Exception e) {
            Slog.e(TAG, "Couldn't communicate with notification manager");
            return null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.backup.BlobBackupHelper
    public void applyRestoredPayload(String key, byte[] payload) {
        if (DEBUG) {
            Slog.v(TAG, "Got restore of " + key);
        }
        if (KEY_NOTIFICATIONS.equals(key)) {
            try {
                INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).applyRestore(payload, this.mUserId);
            } catch (Exception e) {
                Slog.e(TAG, "Couldn't communicate with notification manager");
            }
        }
    }
}
