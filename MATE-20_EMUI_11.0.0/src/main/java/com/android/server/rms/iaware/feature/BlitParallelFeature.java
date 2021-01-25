package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.huawei.android.os.SystemPropertiesEx;

public class BlitParallelFeature extends RFeature {
    private static final int BASE_VERSION = 3;
    private static final String TAG = "BlitParallelFeature";

    public BlitParallelFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i(TAG, "disable iaware BlitParallel feature!");
        SystemPropertiesEx.set("persist.sys.iaware.blitparallel", "false");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 3) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", baseVersion: 3");
            SystemPropertiesEx.set("persist.sys.iaware.blitparallel", "false");
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware blitparallel feature!");
        SystemPropertiesEx.set("persist.sys.iaware.blitparallel", "true");
        return true;
    }
}
