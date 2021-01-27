package com.huawei.android.os;

import android.os.UserHandle;

public class UserHandleExt {
    public static boolean isApp(int uid) {
        return UserHandle.isApp(uid);
    }
}
