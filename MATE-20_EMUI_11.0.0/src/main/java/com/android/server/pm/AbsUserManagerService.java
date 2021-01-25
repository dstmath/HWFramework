package com.android.server.pm;

import android.os.IUserManager;

public abstract class AbsUserManagerService extends IUserManager.Stub {
    public boolean isClonedProfile(int userId) {
        return false;
    }
}
