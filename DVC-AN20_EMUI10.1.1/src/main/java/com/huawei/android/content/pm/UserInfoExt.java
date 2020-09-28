package com.huawei.android.content.pm;

import android.content.pm.UserInfo;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class UserInfoExt {
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
}
