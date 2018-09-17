package com.android.server;

import com.android.internal.widget.ILockSettings.Stub;

public abstract class AbsLockSettingsService extends Stub {
    protected int getOldCredentialType(int userId) {
        return -1;
    }

    protected int getPasswordStatus(int currentCredentialType, int oldCredentialType) {
        return 0;
    }

    protected void notifyPasswordStatusChanged(int userId, int status) {
    }

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        return false;
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        return false;
    }
}
