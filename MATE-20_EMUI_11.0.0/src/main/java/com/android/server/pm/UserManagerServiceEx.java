package com.android.server.pm;

import com.huawei.android.content.pm.UserInfoExt;

public class UserManagerServiceEx {
    public static UserInfoExt getUserInfo(int userId) {
        UserInfoExt userInfoExt = new UserInfoExt();
        userInfoExt.setUserInfo(UserManagerService.getInstance().getUserInfo(userId));
        return userInfoExt;
    }
}
