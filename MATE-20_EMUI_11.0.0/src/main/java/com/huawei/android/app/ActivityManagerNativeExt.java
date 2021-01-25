package com.huawei.android.app;

import android.app.ActivityManagerNative;
import android.os.RemoteException;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.content.pm.UserInfoExUtils;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ActivityManagerNativeExt {
    public static UserInfoExAdapter getCurrentUser() throws RemoteException {
        UserInfoExAdapter userInfo = new UserInfoExAdapter();
        if (ActivityManagerNative.getDefault().getCurrentUser() == null) {
            return null;
        }
        UserInfoExUtils.setUserInfo(userInfo, ActivityManagerNative.getDefault().getCurrentUser());
        return userInfo;
    }
}
