package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;
import com.huawei.android.os.SystemPropertiesEx;

public class BroadcastFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String TAG = "BroadcastFeature";
    private static boolean sFeature = false;
    private static boolean sFlowCtrlEnable = SystemPropertiesEx.getBoolean("ro.config.res_brctrl", true);
    private static boolean sImplicitCapEnable = SystemPropertiesEx.getBoolean("ro.config.res_implicit", true);
    private AwareBroadcastPolicy mAwareBrPolicy = null;
    private AwareBroadcastDumpRadar mDumpRadar = null;

    public BroadcastFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        if (MultiTaskManagerService.self() != null) {
            this.mAwareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
        }
    }

    public static boolean isFeatureEnabled(int feature) {
        if (feature == 10) {
            return sFeature && sFlowCtrlEnable;
        }
        if (feature == 11) {
            return sFeature && sImplicitCapEnable;
        }
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        Bundle bundle;
        if (data == null) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.getResourceType(data.getResId()).ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (!(i != 3 || getIawareBrPolicy() == null || (bundle = data.getBundle()) == null)) {
                    this.mAwareBrPolicy.reportSysEvent(15016, bundle.getInt("eventId", 0));
                }
            } else if (getIawareBrPolicy() != null) {
                this.mAwareBrPolicy.reportSysEvent(90011, -1);
            }
        } else if (getIawareBrPolicy() != null) {
            this.mAwareBrPolicy.reportSysEvent(20011, -1);
        }
        return true;
    }

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

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i(TAG, "disable iaware broadcast feature!");
        setEnable(false);
        unsubscribeResourceTypes();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", BroadcastFeature baseVersion: 2");
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware broadcast feature!");
        setEnable(true);
        subscribeResourceTypes();
        return true;
    }

    private void subscribeResourceTypes() {
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
    }

    private void unsubscribeResourceTypes() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER, this.mFeatureType);
        }
    }

    private static void setEnable(boolean enable) {
        sFeature = enable;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String saveBigData(boolean clear) {
        AwareLog.i(TAG, "Feature based on IAware2.0, saveBigData return null.");
        return null;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        if (iawareVer < 2) {
            AwareLog.i(TAG, "Feature based on IAware2.0, getBigDataByVersion return null. iawareVer: " + iawareVer);
        } else if (!sFeature) {
            AwareLog.e(TAG, "Broadcast feature is disabled, it is invalid operation to save big data.");
            return null;
        } else if (getDumpRadar() != null) {
            return getDumpRadar().getData(forBeta, clearData);
        }
        return null;
    }

    private AwareBroadcastDumpRadar getDumpRadar() {
        if (MultiTaskManagerService.self() != null) {
            this.mDumpRadar = MultiTaskManagerService.self().getAwareBrRadar();
        }
        return this.mDumpRadar;
    }

    private AwareBroadcastPolicy getIawareBrPolicy() {
        if (this.mAwareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mAwareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
        }
        return this.mAwareBrPolicy;
    }
}
