package com.android.server.notification;

import android.content.Context;

public class HwCustZenModeHelper {
    private String[] ABNORMAL_WHITE_APPS_UNDER_ZENMODE = {"com.tencent.mobileqq", "com.tencent.mm", "com.tencent.tim", "com.tencent.pb", "com.immomo.momo", "com.alibaba.mobileim", "im.yixin", "com.whatsapp", "com.facebook.orca", "com.snapchat.android", "com.imo.android.imoim", "ru.ok.android", "com.zing.zalo"};

    public String[] getWhiteApps(Context context) {
        return null;
    }

    public String[] getWhiteAppsInZenMode() {
        return this.ABNORMAL_WHITE_APPS_UNDER_ZENMODE;
    }
}
