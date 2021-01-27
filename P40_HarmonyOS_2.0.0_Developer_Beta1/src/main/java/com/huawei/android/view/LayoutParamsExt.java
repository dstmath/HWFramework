package com.huawei.android.view;

import android.view.WindowManager;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class LayoutParamsExt {
    @HwSystemApi
    public static final int PRIVATE_FLAG_NO_MOVE_ANIMATION = 64;
    @HwSystemApi
    public static final int PRIVATE_FLAG_SHOW_FOR_ALL_USERS = 16;

    public static void orPrivateFlags(WindowManager.LayoutParams lp, int privateFlag) {
        if (lp != null) {
            lp.privateFlags |= privateFlag;
        }
    }

    public static int getOrPrivateFlags(WindowManager.LayoutParams lp, int privateFlag) {
        if (lp == null) {
            return -1;
        }
        lp.privateFlags |= privateFlag;
        return lp.privateFlags;
    }
}
