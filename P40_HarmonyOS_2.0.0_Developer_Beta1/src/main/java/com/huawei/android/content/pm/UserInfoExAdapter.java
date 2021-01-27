package com.huawei.android.content.pm;

import android.content.pm.UserInfo;
import android.os.UserManager;
import android.util.Log;

public class UserInfoExAdapter {
    private static final String TAG = "UserInfoExAdapter";
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
        return -10000;
    }

    public static UserInfoExAdapter getUserInfo(UserManager userManager, int userHandle) {
        if (userManager == null) {
            Log.e(TAG, "getUserInfoEx: userManager is null!");
            return null;
        }
        UserInfo userInfo = userManager.getUserInfo(userHandle);
        if (userInfo != null) {
            UserInfoExAdapter userInfoEx = new UserInfoExAdapter();
            userInfoEx.setUserInfo(userInfo);
            return userInfoEx;
        }
        Log.e(TAG, "getUserInfoEx: userInfo is null!");
        return null;
    }
}
