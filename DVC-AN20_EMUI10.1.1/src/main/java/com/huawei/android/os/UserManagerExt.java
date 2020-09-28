package com.huawei.android.os;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.util.Log;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class UserManagerExt {
    private static final String TAG = "UserManagerExt";

    public static UserManager get(Context context) {
        return UserManager.get(context);
    }

    public static int getCredentialOwnerProfile(UserManager userManager, int userHandle) {
        if (userManager != null) {
            return userManager.getCredentialOwnerProfile(userHandle);
        }
        Log.e(TAG, "getCredentialOwnerProfile: userManager is null!");
        return userHandle;
    }

    public static UserInfoExt getUserInfoEx(UserManager userManager, int userHandle) {
        if (userManager == null) {
            Log.e(TAG, "getUserInfoEx: userManager is null!");
            return null;
        }
        UserInfo userInfo = userManager.getUserInfo(userHandle);
        if (userInfo != null) {
            UserInfoExt userInfoExt = new UserInfoExt();
            userInfoExt.setUserInfo(userInfo);
            return userInfoExt;
        }
        Log.e(TAG, "getUserInfoEx: userInfo is null!");
        return null;
    }
}
