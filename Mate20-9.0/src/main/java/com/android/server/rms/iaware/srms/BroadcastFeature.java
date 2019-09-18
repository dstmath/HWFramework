package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;

public class BroadcastFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String TAG = "BroadcastFeature";
    private static boolean mFeature = false;
    private static boolean mFlowCtrlEnable = SystemProperties.getBoolean("ro.config.res_brctrl", true);
    private static boolean mImplicitCapEnable = SystemProperties.getBoolean("ro.config.res_implicit", true);
    private AwareBroadcastDumpRadar mDumpRadar = null;
    private AwareBroadcastPolicy mIawareBrPolicy = null;

    /* renamed from: com.android.server.rms.iaware.srms.BroadcastFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public BroadcastFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        if (MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
    }

    public static boolean isFeatureEnabled(int feature) {
        boolean z = true;
        if (feature == 10) {
            if (!mFeature || !mFlowCtrlEnable) {
                z = false;
            }
            return z;
        } else if (feature != 11) {
            return false;
        } else {
            if (!mFeature || !mImplicitCapEnable) {
                z = false;
            }
            return z;
        }
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        switch (AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.getResourceType(data.getResId()).ordinal()]) {
            case 1:
                if (getIawareBrPolicy() != null) {
                    this.mIawareBrPolicy.reportSysEvent(20011, -1);
                    break;
                }
                break;
            case 2:
                if (getIawareBrPolicy() != null) {
                    this.mIawareBrPolicy.reportSysEvent(90011, -1);
                    break;
                }
                break;
            case 3:
                if (getIawareBrPolicy() != null) {
                    Bundle bundle = data.getBundle();
                    if (bundle != null) {
                        this.mIawareBrPolicy.reportSysEvent(15016, bundle.getInt("eventId", 0));
                        break;
                    }
                }
                break;
        }
        return true;
    }

    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    public boolean disable() {
        AwareLog.i(TAG, "disable iaware broadcast feature!");
        setEnable(false);
        unsubscribeResourceTypes();
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", BroadcastFeature baseVersion: " + 2);
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware broadcast feature!");
        setEnable(true);
        subscribeResourceTypes();
        return true;
    }

    private void subscribeResourceTypes() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
    }

    private void unsubscribeResourceTypes() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
    }

    private static void setEnable(boolean enable) {
        mFeature = enable;
    }

    public String saveBigData(boolean clear) {
        AwareLog.i(TAG, "Feature based on IAware2.0, saveBigData return null.");
        return null;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (iawareVer < 2) {
            AwareLog.i(TAG, "Feature based on IAware2.0, getBigDataByVersion return null. iawareVer: " + iawareVer);
        } else if (!mFeature) {
            AwareLog.e(TAG, "Broadcast feature is disabled, it is invalid operation to save big data.");
            return null;
        } else if (getDumpRadar() != null) {
            return getDumpRadar().getData(forBeta, clearData);
        }
        return null;
    }

    private AwareBroadcastDumpRadar getDumpRadar() {
        if (MultiTaskManagerService.self() != null) {
            this.mDumpRadar = MultiTaskManagerService.self().getIawareBrRadar();
        }
        return this.mDumpRadar;
    }

    private AwareBroadcastPolicy getIawareBrPolicy() {
        if (this.mIawareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
        return this.mIawareBrPolicy;
    }
}
