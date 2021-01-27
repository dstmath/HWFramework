package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.dev.FeatureXmlConfigParserRt;

public class ScreenOnWakelockSchedFeatureRt extends DevSchedFeatureBase {
    private static final String TAG = "ScreenOnWakelockSchedFeatureRT";
    private boolean mEnable = false;

    public ScreenOnWakelockSchedFeatureRt(Context context, String name) {
        super(context);
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public void readFeatureConfig(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        if (config != null && config.subSwitch) {
            this.mEnable = true;
        }
    }

    public boolean isAwarePreventWakelockScreenOn(String pkgName, String tag) {
        if (pkgName == null || tag == null) {
            AwareLog.e(TAG, "isAwarePreventWakelockScreenOn: pkgName or tag is null!");
            return false;
        }
        int appAttr = AppTypeRecoManager.getInstance().getAppAttribute(pkgName);
        AwareLog.d(TAG, "isAwarePreventWakelockScreenOn: get pkg " + pkgName + " tag " + tag + " appAttribute " + appAttr);
        if (appAttr != -1 && (appAttr & 8) == 8) {
            return true;
        }
        return false;
    }
}
