package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.qos.AwareQosFeatureManager;

public class AwareQosFeature extends RFeature {
    private static final int BASE_VERSION = 5;
    private static final String TAG = "AwareQosFeature";

    public AwareQosFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        AwareQosFeatureManager.getInstance().reportData(data);
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
            AwareQosFeatureManager.getInstance().disable();
            return false;
        }
        subscribeResourceTypes();
        AwareQosFeatureManager.getInstance().enable();
        AwareLog.d(TAG, "enableFeatureEx");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        unsubscribeResourceTypes();
        AwareQosFeatureManager.getInstance().disable();
        AwareLog.d(TAG, "disable feature");
        return true;
    }

    private void subscribeResourceTypes() {
        this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD, this.mFeatureType);
    }

    private void unsubscribeResourceTypes() {
        this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SHOW_INPUTMETHOD, this.mFeatureType);
    }
}
