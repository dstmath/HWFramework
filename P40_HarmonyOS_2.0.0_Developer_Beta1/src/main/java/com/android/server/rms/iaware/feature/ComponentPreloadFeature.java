package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareComponentPreloadManager;

public class ComponentPreloadFeature extends RFeature {
    private static final int BASE_VERSION = 5;
    private static final String TAG = "ComponentPreloadFeature";

    public ComponentPreloadFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareConstant.ResourceType[] types = AwareConstant.ResourceType.values();
        int resId = data.getResId();
        if (resId < 0 || resId >= types.length) {
            return false;
        }
        if (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[types[resId].ordinal()] != 1) {
            return false;
        }
        AwareComponentPreloadManager.getInstance().reportPkgClearData(data.getBundle());
        return true;
    }

    /* renamed from: com.android.server.rms.iaware.feature.ComponentPreloadFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_PKG_CLEAR_DATA.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.i(TAG, "ComponentPreloadFeature enable");
        AwareComponentPreloadManager.getInstance().enable(this.mContext);
        subscribeData(true);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i(TAG, "ComponentPreloadFeature disable");
        AwareComponentPreloadManager.getInstance().disable();
        subscribeData(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 5) {
            return false;
        }
        AwareComponentPreloadManager.getInstance().enable(this.mContext);
        subscribeData(true);
        return true;
    }

    private void subscribeData(boolean enable) {
        if (this.mDataRegister == null) {
            AwareLog.e(TAG, "mDataRegister is null,can not subscribeData");
        } else if (enable) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_PKG_CLEAR_DATA, this.mFeatureType);
        } else {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_PKG_CLEAR_DATA, this.mFeatureType);
        }
    }
}
