package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import java.util.concurrent.atomic.AtomicBoolean;

public class SysLoadFeature extends RFeature {
    private static final int BASE_VERSION = 3;
    private static final String TAG = "AwareSysLoad_SysLoadFeature";
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    public SysLoadFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mContext = context;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        subscribleEvents();
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        if (realVersion >= 3) {
            SysLoadManager.getInstance().setContext(this.mContext);
            SysLoadManager.getInstance().enable();
            this.mRunning.set(true);
        }
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        SysLoadManager.getInstance().disable();
        this.mRunning.set(false);
        return true;
    }

    private void subscribleEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SYSLOAD, this.mFeatureType);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (this.mRunning.get()) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean configUpdate() {
        return true;
    }
}
