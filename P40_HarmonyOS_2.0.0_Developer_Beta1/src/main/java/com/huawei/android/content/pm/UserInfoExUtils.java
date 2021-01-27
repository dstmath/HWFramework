package com.huawei.android.content.pm;

import android.content.pm.UserInfo;

public class UserInfoExUtils {
    public static void setUserInfo(UserInfoExAdapter infoExt, UserInfo info) {
        if (infoExt != null) {
            infoExt.setUserInfo(info);
        }
    }
}
