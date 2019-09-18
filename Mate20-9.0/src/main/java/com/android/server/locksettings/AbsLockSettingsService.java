package com.android.server.locksettings;

import com.android.internal.widget.ILockSettings;

public abstract class AbsLockSettingsService extends ILockSettings.Stub {
    /* access modifiers changed from: protected */
    public int getOldCredentialType(int userId) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getPasswordStatus(int currentCredentialType, int oldCredentialType) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void notifyPasswordStatusChanged(int userId, int status) {
    }

    /* access modifiers changed from: protected */
    public void notifyModifyPwdForPrivSpacePwdProtect(String credential, String savedCredential, int userId) {
    }

    /* access modifiers changed from: protected */
    public void notifyBigDataForPwdProtectFail(int userId) {
    }

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        return false;
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        return false;
    }

    public void handleUserClearLockForAnti(int userId) {
    }
}
