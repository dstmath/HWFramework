package com.android.server.pm;

import android.content.pm.ActivityInfo;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.server.pm.PreferredComponent;

public class HwCustPreferredComponentImpl extends HwCustPreferredComponent {
    private static final boolean IS_SKIP_HWSTARTUPGUIDE = SystemProperties.getBoolean("ro.config.skip_startup_ota", true);
    private static final String PACKAGE_START_UP_GUIDE = "com.huawei.hwstartupguide";
    private static final String PACKAGE_START_UP_GUIDE_CLASS = "com.huawei.hwstartupguide.LanguageSelectActivity";

    public boolean isSkipHwStarupGuide(PreferredComponent.Callbacks callbacks, ActivityInfo activityInfo) {
        if (!IS_SKIP_HWSTARTUPGUIDE || activityInfo == null || TextUtils.isEmpty(activityInfo.packageName) || TextUtils.isEmpty(activityInfo.name) || !PACKAGE_START_UP_GUIDE.equals(activityInfo.packageName) || !PACKAGE_START_UP_GUIDE_CLASS.equals(activityInfo.name) || callbacks == null || !(callbacks instanceof PreferredActivity) || !((PreferredActivity) callbacks).hasCategory("android.intent.category.HOME")) {
            return false;
        }
        return true;
    }
}
