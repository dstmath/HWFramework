package com.huawei.android.os;

import android.os.UserHandle;

public class UserHandleEx {
    public static final UserHandle ALL = UserHandle.ALL;
    public static final boolean MU_ENABLED = true;
    public static final UserHandle OWNER = UserHandle.OWNER;
    public static final int PER_USER_RANGE = 100000;
    public static final UserHandle SYSTEM = UserHandle.SYSTEM;
    public static final int USER_ALL = -1;
    public static final int USER_CURRENT = -2;
    public static final int USER_OWNER = 0;
    public static final int USER_SYSTEM = 0;

    public static int getUndefinedUserId() {
        return -10000;
    }

    public static UserHandle getCurrentOrSelfUserHandle() {
        return UserHandle.CURRENT_OR_SELF;
    }

    public static int getIdentifier(UserHandle userHandle) {
        return userHandle.getIdentifier();
    }

    public static int getUserId(UserHandle userHandle, int uid) {
        return UserHandle.getUserId(uid);
    }

    public static int getAppId(UserHandle userHandle, int uid) {
        return UserHandle.getAppId(uid);
    }

    public static int getUserId(int uid) {
        return UserHandle.getUserId(uid);
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
}
