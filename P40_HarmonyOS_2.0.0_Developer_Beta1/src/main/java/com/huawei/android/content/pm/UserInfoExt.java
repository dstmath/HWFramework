package com.huawei.android.content.pm;

import android.content.pm.UserInfo;
import android.util.Log;
import com.huawei.android.os.UserHandleEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class UserInfoExt {
    public static final int FLAG_CLONED_PROFILE = 67108864;
    public static final int INVALID_SERIAL_NUMBER = -1;
    private static final String TAG = "UserInfoExt";
    private UserInfo mUserInfo;

    public void setUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            Log.e(TAG, "setUserInfo: userInfo is null!");
        } else {
            this.mUserInfo = userInfo;
        }
    }

    public UserInfo getUserInfo() {
        return this.mUserInfo;
    }

    public boolean isClonedProfile() {
        UserInfo userInfo = this.mUserInfo;
        if (userInfo != null) {
            return userInfo.isClonedProfile();
        }
        Log.e(TAG, "isClonedProfile: mUserInfo is null!");
        return false;
    }

    public int getUserId() {
        UserInfo userInfo = this.mUserInfo;
        if (userInfo != null) {
            return userInfo.id;
        }
        return UserHandleEx.USER_NULL;
    }

    public boolean isRepairMode() {
        UserInfo userInfo = this.mUserInfo;
        if (userInfo != null) {
            return userInfo.isRepairMode();
        }
        Log.e(TAG, "isRepairMode: mUserInfo is null!");
        return false;
    }

    public int getSerialNumber() {
        UserInfo userInfo = this.mUserInfo;
        if (userInfo != null) {
            return userInfo.serialNumber;
        }
        return -1;
    }

    public boolean isManagedProfile() {
        UserInfo userInfo = this.mUserInfo;
        if (userInfo != null) {
            return userInfo.isManagedProfile();
        }
        return false;
    }

    public boolean canHaveProfile() {
        return this.mUserInfo.canHaveProfile();
    }

    public boolean isHwHiddenSpace() {
        return this.mUserInfo.isHwHiddenSpace();
    }

    public String getLastLoggedInFingerprint() {
        return this.mUserInfo.lastLoggedInFingerprint;
    }

    public String getLastLoggedInFingerprintEx() {
        return this.mUserInfo.lastLoggedInFingerprintEx;
    }

    public int getProfileGroupId() {
        return this.mUserInfo.profileGroupId;
    }

    public boolean isGuest() {
        return this.mUserInfo.isGuest();
    }
}
