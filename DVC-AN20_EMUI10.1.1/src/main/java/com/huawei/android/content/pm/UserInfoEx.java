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
        if (isUserInfoValid()) {
            return this.mUserInfo.id;
        }
        return -1;
    }

    public int getProfileGroupId() {
        if (isUserInfoValid()) {
            return this.mUserInfo.profileGroupId;
        }
        return -1;
    }

    public boolean isManagedProfile() {
        if (!isUserInfoValid()) {
            return false;
        }
        return this.mUserInfo.isManagedProfile();
    }

    public boolean isClonedProfile() {
        if (!isUserInfoValid()) {
            return false;
        }
        return this.mUserInfo.isClonedProfile();
    }

    public boolean isGuest() {
        if (!isUserInfoValid()) {
            return false;
        }
        return this.mUserInfo.isGuest();
    }

    public boolean isRepairMode() {
        if (!isUserInfoValid()) {
            return false;
        }
        return this.mUserInfo.isRepairMode();
    }

    public UserInfo getUserInfo() {
        return this.mUserInfo;
    }
}
