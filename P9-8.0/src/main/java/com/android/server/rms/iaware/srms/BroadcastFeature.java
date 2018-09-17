package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;

public class BroadcastFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String TAG = "BroadcastFeature";
    private static boolean mFeature = false;
    private static boolean mFlowCtrlEnable = SystemProperties.getBoolean("ro.config.res_brctrl", true);
    private static boolean mImplicitCapEnable = SystemProperties.getBoolean("ro.config.res_implicit", true);
    private AwareBroadcastDumpRadar mDumpRadar = null;

    public BroadcastFeature(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
    }

    public static boolean isFeatureEnabled(int feature) {
        boolean z = false;
        if (feature == 10) {
            if (mFeature) {
                z = mFlowCtrlEnable;
            }
            return z;
        } else if (feature != 11) {
            return false;
        } else {
            if (mFeature) {
                z = mImplicitCapEnable;
            }
            return z;
        }
    }

    public boolean reportData(CollectData data) {
        return true;
    }

    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    public boolean disable() {
        AwareLog.i(TAG, "disable iaware broadcast feature!");
        setEnable(false);
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", BroadcastFeature baseVersion: " + 2);
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware broadcast feature!");
        setEnable(true);
        return true;
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
}
