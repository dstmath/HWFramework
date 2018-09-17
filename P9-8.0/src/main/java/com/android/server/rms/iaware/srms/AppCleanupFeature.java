package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.content.Context;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.feature.RFeature;

public class AppCleanupFeature extends RFeature {
    private static final /* synthetic */ int[] -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues = null;
    private static final int BASE_VERSION = 2;
    private static final String TAG = "AppCleanupFeature";
    private static boolean mFeature = false;

    private static /* synthetic */ int[] -getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues() {
        if (-android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues != null) {
            return -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues;
        }
        int[] iArr = new int[ResourceType.values().length];
        try {
            iArr[ResourceType.RESOURCE_APPASSOC.ordinal()] = 4;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResourceType.RESOURCE_APP_FREEZE.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResourceType.RESOURCE_BOOT_COMPLETED.ordinal()] = 6;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResourceType.RESOURCE_GAME_BOOST.ordinal()] = 7;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResourceType.RESOURCE_HOME.ordinal()] = 8;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResourceType.RESOURCE_INSTALLER_MANAGER.ordinal()] = 9;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResourceType.RESOURCE_INVALIDE_TYPE.ordinal()] = 10;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ResourceType.RESOURCE_MEDIA_BTN.ordinal()] = 11;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ResourceType.RESOURCE_NET_MANAGE.ordinal()] = 12;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCENE_REC.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 1;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 2;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ResourceType.RESOURCE_STATUS_BAR.ordinal()] = 14;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ResourceType.RESOURCE_USERHABIT.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ResourceType.RESOURCE_USER_PRESENT.ordinal()] = 3;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ResourceType.RES_APP.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ResourceType.RES_DEV_STATUS.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ResourceType.RES_INPUT.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues = iArr;
        return iArr;
    }

    public AppCleanupFeature(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        ResourceType[] types = ResourceType.values();
        if (types.length <= data.getResId() || data.getResId() < 0) {
            AwareLog.e(TAG, "bad resId = " + data.getResId());
            return false;
        }
        ResourceType resType = types[data.getResId()];
        MultiTaskManagerService mtm = MultiTaskManagerService.self();
        if (mtm == null) {
            AwareLog.e(TAG, "can't do anything when event [" + resType + "] is comming, because mtm services is down");
            return false;
        }
        switch (-getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues()[resType.ordinal()]) {
            case 1:
                mtm.requestAppClean(AppCleanSource.SMART_CLEAN);
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
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_USER_PRESENT, this.mFeatureType);
        } else {
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_USER_PRESENT, this.mFeatureType);
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
