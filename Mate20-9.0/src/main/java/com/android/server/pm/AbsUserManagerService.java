package com.android.server.pm;

import android.os.IUserManager;

public abstract class AbsUserManagerService extends IUserManager.Stub {
    /* access modifiers changed from: protected */
    public boolean isHwTrustSpaceExist(int parentId) {
        return false;
    }

    public boolean isClonedProfile(int userId) {
        return false;
    }
}
