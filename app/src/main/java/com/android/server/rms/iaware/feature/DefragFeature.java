package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.PPPOEStateMachine;
import com.android.server.rms.iaware.IRDataRegister;

public class DefragFeature extends RFeature {
    private static final String F2FSDEFRAG_PROP = "persist.sys.io.f2fs.defrag";
    private static final String TAG = "DefragFeature";

    public DefragFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    public boolean enable() {
        SystemProperties.set(F2FSDEFRAG_PROP, PPPOEStateMachine.PHASE_INITIALIZE);
        AwareLog.d(TAG, "DefragFeature enabled");
        return true;
    }

    public boolean disable() {
        SystemProperties.set(F2FSDEFRAG_PROP, PPPOEStateMachine.PHASE_DEAD);
        AwareLog.d(TAG, "DefragFeature disable");
        return true;
    }
}
