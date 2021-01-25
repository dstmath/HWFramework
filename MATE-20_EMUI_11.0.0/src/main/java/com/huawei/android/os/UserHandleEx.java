package com.huawei.android.os;

import android.os.UserHandle;
import com.huawei.annotation.HwSystemApi;

public class UserHandleEx {
    public static final UserHandle ALL = UserHandle.ALL;
    public static final UserHandle CURRENT = UserHandle.CURRENT;
    @HwSystemApi
    public static final UserHandle CURRENT_OR_SELF = UserHandle.CURRENT_OR_SELF;
    public static final boolean MU_ENABLED = true;
    public static final UserHandle OWNER = UserHandle.OWNER;
    public static final int PER_USER_RANGE = 100000;
    public static final UserHandle SYSTEM = UserHandle.SYSTEM;
    public static final int USER_ALL = -1;
    public static final int USER_CURRENT = -2;
    @HwSystemApi
    public static final int USER_NULL = -10000;
    public static final int USER_OWNER = 0;
    public static final int USER_SYSTEM = 0;

    public static int getUndefinedUserId() {
        return USER_NULL;
    }

    public static UserHandle getCurrentOrSelfUserHandle() {
        return UserHandle.CURRENT_OR_SELF;
    }

    public static int getIdentifier(UserHandle userHandle) {
        if (userHandle == null) {
            return -1;
        }
        return userHandle.getIdentifier();
    }

    public static int getUserId(UserHandle userHandle, int uid) {
        if (userHandle == null) {
            return -1;
        }
        return UserHandle.getUserId(uid);
    }

    public static int getUserId(int uid) {
        return UserHandle.getUserId(uid);
    }

    public static int getAppId(UserHandle userHandle, int uid) {
        if (userHandle == null) {
            return -1;
        }
        return UserHandle.getAppId(uid);
    }

    @HwSystemApi
    public static int getAppId(int uid) {
        return UserHandle.getAppId(uid);
    }

    public static UserHandle getUserHandle(int userId) {
        return new UserHandle(userId);
    }

    public static UserHandle of(int userId) {
        return UserHandle.of(userId);
    }

    public static int myUserId() {
        return UserHandle.myUserId();
    }

    public static boolean isSameApp(int uid1, int uid2) {
        return UserHandle.isSameApp(uid1, uid2);
    }

    @HwSystemApi
    public static int getCallingUserId() {
        return UserHandle.getCallingUserId();
    }

    @HwSystemApi
    public static boolean isIsolated(int uid) {
        return UserHandle.isIsolated(uid);
    }

    @HwSystemApi
    public static boolean isClonedProfile(int userId) {
        return UserHandle.isClonedProfile(userId);
    }
}
