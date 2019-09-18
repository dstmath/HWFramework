package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;

public class BlitParallelFeature extends RFeature {
    private static final int BASE_VERSION = 3;
    private static final String TAG = "BlitParallelFeature";

    public BlitParallelFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    public boolean disable() {
        AwareLog.i(TAG, "disable iaware BlitParallel feature!");
        SystemProperties.set("persist.sys.iaware.blitparallel", "false");
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 3) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", blitparallel baseVersion: " + 3);
            SystemProperties.set("persist.sys.iaware.blitparallel", "false");
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware blitparallel feature!");
        SystemProperties.set("persist.sys.iaware.blitparallel", "true");
        return true;
    }
}
