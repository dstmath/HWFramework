package com.huawei.android.os;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.util.Log;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class UserManagerExt {
    public static final int FLAG_HW_HIDDENSPACE = 33554432;
    public static final int MKDIR_FOR_USER_TRANSACTION = 1001;
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

    public static List<UserInfoExt> getProfiles(UserManager userManager, int userHandle) {
        List<UserInfoExt> exProfiles = new ArrayList<>();
        if (userManager == null) {
            return exProfiles;
        }
        for (UserInfo userInfo : userManager.getProfiles(userHandle)) {
            UserInfoExt profileExt = new UserInfoExt();
            profileExt.setUserInfo(userInfo);
            exProfiles.add(profileExt);
        }
        return exProfiles;
    }

    public static List<UserInfoExt> getUsers(UserManager userManager, boolean excludeDying) {
        List<UserInfoExt> exUsers = new ArrayList<>();
        if (userManager == null) {
            return exUsers;
        }
        for (UserInfo userInfo : userManager.getUsers(excludeDying)) {
            UserInfoExt userExt = new UserInfoExt();
            userExt.setUserInfo(userInfo);
            exUsers.add(userExt);
        }
        return exUsers;
    }

    public static boolean isUserUnlocked(UserManager userManager, int userId) {
        if (userManager == null) {
            return false;
        }
        return userManager.isUserUnlocked(userId);
    }

    public static boolean isHwHiddenSpace(UserInfoExt userInfoExt) {
        return (userInfoExt == null || userInfoExt.getUserInfo() == null || (userInfoExt.getUserInfo().flags & 33554432) != 33554432) ? false : true;
    }
}
