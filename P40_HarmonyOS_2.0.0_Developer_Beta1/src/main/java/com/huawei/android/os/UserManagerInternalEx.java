package com.huawei.android.os;

import android.os.UserManagerInternal;
import com.android.server.LocalServices;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class UserManagerInternalEx {
    private static UserManagerInternal sUserManagerInternal = ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class));

    public boolean isUserUnlockingOrUnlocked(int userId) {
        return sUserManagerInternal.isUserUnlockingOrUnlocked(userId);
    }

    @HwSystemApi
    public boolean isClonedProfile(int userId) {
        return sUserManagerInternal.isClonedProfile(userId);
    }
}
