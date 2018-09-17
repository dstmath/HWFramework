package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;

public class AppFakeFeature extends RFeature {
    private static final int BASE_VERSION = 1;
    private static final int MIN_VERSION = 2;
    private static final String TAG = "AppFakeFeature";
    private int mIAwareVersion = 1;

    public AppFakeFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (iawareVer >= 2) {
            return SRMSDumpRadar.getInstance().getFakeBigData(forBeta, clearData);
        }
        AwareLog.e(TAG, "bigdata is not support, iawareVer=" + iawareVer);
        return null;
    }

    public boolean enable() {
        return false;
    }

    public boolean disable() {
        if (this.mIAwareVersion < 2) {
            return false;
        }
        AwareFakeActivityRecg.commDisable();
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        this.mIAwareVersion = realVersion;
        if (realVersion < 2) {
            return false;
        }
        AwareLog.i(TAG, "AppFakeFeature enableFeatureEx");
        AwareFakeActivityRecg.commEnable();
        return true;
    }
}
