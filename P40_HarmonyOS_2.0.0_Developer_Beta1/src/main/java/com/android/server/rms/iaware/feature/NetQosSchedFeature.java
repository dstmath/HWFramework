package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.qos.AwareNetQosSchedManager;

public class NetQosSchedFeature extends RFeature {
    private static final int BASE_VERSION = 5;
    private static final String TAG = "NetQosSchedFeature";

    public NetQosSchedFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        AwareNetQosSchedManager.getInstance().reportData(data);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 5) {
            AwareLog.e(TAG, "Failed to enableFeatureEx for invalid iaware version no: " + realVersion + ", but the expected base version no: 5");
            AwareNetQosSchedManager.getInstance().disable();
            return false;
        }
        registerDataReport();
        AwareNetQosSchedManager.getInstance().enable();
        AwareLog.d(TAG, "enableFeatureEx");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        unregisterDataReport();
        AwareNetQosSchedManager.getInstance().disable();
        AwareLog.d(TAG, "disable feature");
        return true;
    }

    private void registerDataReport() {
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_VPN_CONN, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
        }
    }

    private void unregisterDataReport() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCENE_REC, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_VPN_CONN, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
        }
    }
}
