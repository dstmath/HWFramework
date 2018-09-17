package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng;
import com.android.server.rms.iaware.IRDataRegister;
import java.util.concurrent.atomic.AtomicBoolean;

public class APPFreezeFeature extends RFeature {
    private static final /* synthetic */ int[] -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues = null;
    private static final int MIN_VERSION = 2;
    private static final String TAG = "APPFreezeFeature";
    private static AtomicBoolean mIsInitialized = new AtomicBoolean(false);

    private static /* synthetic */ int[] -getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues() {
        if (-android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues != null) {
            return -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues;
        }
        int[] iArr = new int[ResourceType.values().length];
        try {
            iArr[ResourceType.RESOURCE_APPASSOC.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResourceType.RESOURCE_APP_FREEZE.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResourceType.RESOURCE_BOOT_COMPLETED.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResourceType.RESOURCE_GAME_BOOST.ordinal()] = 8;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResourceType.RESOURCE_HOME.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResourceType.RESOURCE_INSTALLER_MANAGER.ordinal()] = 1;
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
            iArr[ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 2;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 3;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ResourceType.RESOURCE_STATUS_BAR.ordinal()] = 4;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ResourceType.RESOURCE_USERHABIT.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ResourceType.RESOURCE_USER_PRESENT.ordinal()] = 15;
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

    public APPFreezeFeature(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        initConfig();
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        AwareAppFreezeMng freeMng = AwareAppFreezeMng.getInstance();
        ResourceType type = ResourceType.getResourceType(data.getResId());
        switch (-getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                if (freeMng != null) {
                    freeMng.report(type, data);
                }
                return true;
            case 2:
                if (freeMng != null) {
                    freeMng.report(90011);
                }
                return true;
            case 3:
                if (freeMng != null) {
                    freeMng.report(20011);
                }
                return true;
            case 4:
                Bundle bundle = data.getBundle();
                if (!(bundle == null || freeMng == null)) {
                    freeMng.report(bundle.getInt("eventid"));
                }
                return true;
            default:
                return false;
        }
    }

    public boolean enable() {
        return false;
    }

    public boolean disable() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_STATUS_BAR, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
        AwareAppFreezeMng.disable();
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion >= 2) {
            if (this.mIRDataRegister != null) {
                this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_STATUS_BAR, this.mFeatureType);
                this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
                this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
                this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
            }
            AwareAppFreezeMng.enable();
        }
        return true;
    }

    private void initConfig() {
        if (!mIsInitialized.get()) {
            AwareAppFreezeMng.getInstance();
            mIsInitialized.set(true);
        }
    }
}
