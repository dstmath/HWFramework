package com.android.server.pm;

import android.os.IUserManager.Stub;

public abstract class AbsUserManagerService extends Stub {
    protected boolean isHwTrustSpaceExist(int parentId) {
        return false;
    }

    public boolean isClonedProfile(int userId) {
        return false;
    }
}
