package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.srms.SrmsDumpRadar;

public class AppFakeFeature extends RFeature {
    private static final int BASE_VERSION = 1;
    private static final int MIN_VERSION = 2;
    private static final String TAG = "AppFakeFeature";
    private int mAwareVersion = 1;

    public AppFakeFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
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
        if (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[types[resId].ordinal()] == 1) {
            AwareFakeActivityRecg.self().setScreenLockState(false);
        }
        return true;
    }

    /* renamed from: com.android.server.rms.iaware.feature.AppFakeFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_USER_PRESENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (iawareVer >= 2) {
            return SrmsDumpRadar.getInstance().getFakeBigData(forBeta, clearData);
        }
        AwareLog.e(TAG, "bigdata is not support, iawareVer=" + iawareVer);
        return null;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (this.mAwareVersion < 2) {
            return false;
        }
        AwareFakeActivityRecg.commDisable();
        subscribeData(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        this.mAwareVersion = realVersion;
        if (realVersion < 2) {
            return false;
        }
        AwareLog.i(TAG, "AppFakeFeature enableFeatureEx");
        AwareFakeActivityRecg.commEnable();
        subscribeData(true);
        return true;
    }

    private void subscribeData(boolean enable) {
        if (this.mDataRegister == null) {
            AwareLog.e(TAG, "mDataRegister is null,can not subscribeData");
        } else if (enable) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_USER_PRESENT, this.mFeatureType);
        } else {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_USER_PRESENT, this.mFeatureType);
        }
    }
}
