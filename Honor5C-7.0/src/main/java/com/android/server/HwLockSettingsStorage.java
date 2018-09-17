package com.android.server;

import android.content.Context;
import com.android.server.LockSettingsStorage.Callback;

class HwLockSettingsStorage extends LockSettingsStorage {
    public static final String LOCK_PASSWORD_FILE2 = "password2.key";
    private static final String TABLE = "locksettings";
    private static final String TAG = "HwLockSettingsStorage";

    public HwLockSettingsStorage(Context context, Callback callback) {
        super(context, callback);
    }

    String getLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser2(userId, LOCK_PASSWORD_FILE2);
    }
}
