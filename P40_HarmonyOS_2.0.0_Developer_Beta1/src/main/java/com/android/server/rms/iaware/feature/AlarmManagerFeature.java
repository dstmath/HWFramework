package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AlarmManagerDumpRadar;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;

public class AlarmManagerFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String TAG = "AlarmManagerFeature";
    private static boolean sFeature = false;

    public AlarmManagerFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareConstant.ResourceType[] types = AwareConstant.ResourceType.values();
        if (types.length <= data.getResId() || data.getResId() < 0) {
            AwareLog.e(TAG, "bad resId = " + data.getResId());
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[types[data.getResId()].ordinal()];
        if (i == 1) {
            AwareWakeUpManager.getInstance().screenOff();
        } else if (i == 2) {
            AwareWakeUpManager.getInstance().screenOn();
        }
        return true;
    }

    /* renamed from: com.android.server.rms.iaware.feature.AlarmManagerFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i(TAG, "disable iaware AlarmManagerFeature!");
        setEnable(false);
        subscribeData(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", AlarmManagerFeature baseVersion: 2");
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware AlarmManagerFeature!");
        setEnable(true);
        subscribeData(true);
        return true;
    }

    private static void setEnable(boolean enable) {
        sFeature = enable;
    }

    private void subscribeData(boolean enable) {
        if (this.mDataRegister == null) {
            AwareLog.e(TAG, "mDataRegister is null, can't subscribe data!");
        } else if (enable) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        } else {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String saveBigData(boolean clear) {
        AwareLog.i(TAG, "Feature based on IAware2.0, saveBigData return null.");
        return null;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (sFeature && forBeta && iawareVer >= 2) {
            return AlarmManagerDumpRadar.getInstance().saveBigData(clearData);
        }
        AwareLog.e(TAG, "bigdata is not support, sFeature=" + sFeature + ", iawareVer=" + iawareVer);
        return null;
    }

    public static boolean isEnable() {
        return sFeature;
    }
}
