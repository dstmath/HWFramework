package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import java.util.concurrent.atomic.AtomicBoolean;

public class SysLoadFeature extends RFeature {
    private static final String TAG = "AwareSysLoad_SysLoadFeature";
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    public SysLoadFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mContext = context;
    }

    public boolean enable() {
        return false;
    }

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

    public boolean disable() {
        SysLoadManager.getInstance().disable();
        this.mRunning.set(false);
        return true;
    }

    private void subscribleEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SYSLOAD, this.mFeatureType);
        }
    }

    public boolean reportData(CollectData data) {
        if (!this.mRunning.get()) {
            return false;
        }
        SysLoadManager.getInstance().reportData(data);
        return true;
    }

    public boolean configUpdate() {
        return true;
    }
}
