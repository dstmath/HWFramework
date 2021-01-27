package com.android.server.notification;

import android.content.Context;

public class HwCustZenModeHelper {
    private static final String[] ABNORMAL_WHITE_APPS_UNDER_ZENMODES = {"com.tencent.mobileqq", "com.tencent.mm", "com.tencent.tim", "com.tencent.pb", "com.immomo.momo", "com.alibaba.mobileim", "im.yixin", "com.whatsapp", "com.facebook.orca", "com.snapchat.android", "com.imo.android.imoim", "ru.ok.android", "com.zing.zalo"};

    public String[] getWhiteApps(Context context) {
        return null;
    }

    public String[] getWhiteAppsInZenMode() {
        return ABNORMAL_WHITE_APPS_UNDER_ZENMODES;
    }
}
