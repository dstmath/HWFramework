package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.feature.RFeature;

public class AppCleanupFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String TAG = "AppCleanupFeature";
    private static boolean mFeature = false;

    /* renamed from: com.android.server.rms.iaware.srms.AppCleanupFeature$1  reason: invalid class name */
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
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_USER_PRESENT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public AppCleanupFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareConstant.ResourceType[] types = AwareConstant.ResourceType.values();
        if (types.length <= data.getResId() || data.getResId() < 0) {
            AwareLog.e(TAG, "bad resId = " + data.getResId());
            return false;
        }
        AwareConstant.ResourceType resType = types[data.getResId()];
        MultiTaskManagerService mtm = MultiTaskManagerService.self();
        if (mtm == null) {
            AwareLog.e(TAG, "can't do anything when event [" + resType + "] is comming, because mtm services is down");
            return false;
        }
        switch (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[resType.ordinal()]) {
            case 1:
                mtm.requestAppClean(AppMngConstant.AppCleanSource.SMART_CLEAN);
                AwareAppAssociate.getInstance().screenStateChange(true);
                break;
            case 2:
                mtm.cancelAppClean();
                AwareAppAssociate.getInstance().screenStateChange(false);
                break;
            case 3:
                AwareAppAssociate.getInstance().checkBakUpVisWin();
                break;
        }
        return true;
    }

    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    public boolean disable() {
        AwareLog.i(TAG, "disable iaware cleanupFeature!");
        setEnable(false);
        subscribeData(false);
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", CleanupFeature baseVersion: " + 2);
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware CleanupFeature!");
        setEnable(true);
        subscribeData(true);
        return true;
    }

    private static void setEnable(boolean enable) {
        mFeature = enable;
    }

    private void subscribeData(boolean enable) {
        if (this.mIRDataRegister == null) {
            AwareLog.e(TAG, "mIRDataRegister is null, can't subscribe data!");
            return;
        }
        if (enable) {
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_USER_PRESENT, this.mFeatureType);
        } else {
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_USER_PRESENT, this.mFeatureType);
        }
    }

    public String saveBigData(boolean clear) {
        AwareLog.i(TAG, "Feature based on IAware2.0, saveBigData return null.");
        return null;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (mFeature && forBeta && iawareVer >= 2) {
            return AppCleanupDumpRadar.getInstance().saveCleanBigData(clearData);
        }
        AwareLog.e(TAG, "bigdata is not support, mFeature=" + mFeature + ", iawareVer=" + iawareVer);
        return null;
    }

    public static boolean isAppCleanEnable() {
        return mFeature;
    }
}
