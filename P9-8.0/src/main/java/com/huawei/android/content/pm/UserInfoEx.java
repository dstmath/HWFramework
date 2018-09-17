package com.huawei.android.content.pm;

import android.content.pm.UserInfo;

public class UserInfoEx {
    public static final int FLAG_HW_REPAIR_MODE = 134217728;
    private UserInfo mUserInfo;

    public UserInfoEx(UserInfo userInfo) {
        this.mUserInfo = userInfo;
    }

    public boolean isUserInfoValid() {
        return this.mUserInfo != null;
    }

    public int getUserInfoId() {
        return this.mUserInfo.id;
    }

    public boolean isManagedProfile() {
        return this.mUserInfo.isManagedProfile();
    }

    public boolean isClonedProfile() {
        return this.mUserInfo.isClonedProfile();
    }

    public boolean isGuest() {
        return this.mUserInfo.isGuest();
    }

    public boolean isRepairMode() {
        return this.mUserInfo.isRepairMode();
    }

    public UserInfo getUserInfo() {
        return this.mUserInfo;
    }
}
