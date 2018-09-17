package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.rms.HwSysResManagerService;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;
import java.util.ArrayList;

public class ResourceFeature extends RFeature {
    private static final /* synthetic */ int[] -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues = null;
    private static final int QUEUESIZE_FOR_DUMPDATA = 12;
    private static final String TAG = "ResourceFeature";
    static final boolean enableResMonitor = SystemProperties.getBoolean("ro.config.res_monitor", true);
    static final boolean enableResQueue = SystemProperties.getBoolean("ro.config.res_queue", true);
    private static boolean mFeature;
    private AwareBroadcastPolicy mIawareBrPolicy = null;

    private static /* synthetic */ int[] -getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues() {
        if (-android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues != null) {
            return -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues;
        }
        int[] iArr = new int[ResourceType.values().length];
        try {
            iArr[ResourceType.RESOURCE_APPASSOC.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResourceType.RESOURCE_APP_FREEZE.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResourceType.RESOURCE_BOOT_COMPLETED.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResourceType.RESOURCE_GAME_BOOST.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResourceType.RESOURCE_HOME.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResourceType.RESOURCE_INSTALLER_MANAGER.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResourceType.RESOURCE_INVALIDE_TYPE.ordinal()] = 9;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ResourceType.RESOURCE_MEDIA_BTN.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ResourceType.RESOURCE_NET_MANAGE.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCENE_REC.ordinal()] = 12;
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
            iArr[ResourceType.RESOURCE_STATUS_BAR.ordinal()] = 13;
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

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            z = SystemProperties.getBoolean("persist.sys.srms.enable", true);
        }
        mFeature = z;
    }

    public ResourceFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        if (MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
    }

    public boolean reportData(CollectData data) {
        if (data == null) {
            return false;
        }
        switch (-getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues()[ResourceType.getResourceType(data.getResId()).ordinal()]) {
            case 1:
                if (getIawareBrPolicy() != null) {
                    this.mIawareBrPolicy.reportSysEvent(90011);
                    break;
                }
                break;
            case 2:
                if (getIawareBrPolicy() != null) {
                    this.mIawareBrPolicy.reportSysEvent(20011);
                    break;
                }
                break;
        }
        return true;
    }

    public boolean enable() {
        setEnable();
        subscribeResourceTypes();
        return true;
    }

    public boolean disable() {
        setDisable();
        unsubscribeResourceTypes();
        return true;
    }

    private void subscribeResourceTypes() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        }
    }

    private void unsubscribeResourceTypes() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
        }
    }

    public boolean configUpdate() {
        HwSysResManagerService.self().cloudFileUpate();
        return true;
    }

    public String saveBigData(boolean clear) {
        if (mFeature) {
            return SRMSDumpRadar.getInstance().saveSRMSBigData(clear);
        }
        AwareLog.e(TAG, "iaware srms is close, it is invalid operation to save big data.");
        return null;
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        if (mFeature) {
            return SRMSDumpRadar.getInstance().getStatisticsData();
        }
        AwareLog.e(TAG, "iaware srms is close, it is invalid operation to get statistics data.");
        return null;
    }

    public ArrayList<DumpData> getDumpData(int time) {
        if (mFeature) {
            return getDumpData();
        }
        AwareLog.e(TAG, "iaware srms is close, it is invalid operation to get dump data.");
        return null;
    }

    private void setEnable() {
        AwareLog.i(TAG, "open iaware srms feature!");
        mFeature = true;
    }

    private void setDisable() {
        AwareLog.i(TAG, "close iaware srms feature!");
        mFeature = false;
    }

    public static boolean getIawareResourceFeature(int type) {
        boolean z = false;
        switch (type) {
            case 1:
                if (mFeature) {
                    z = enableResQueue;
                }
                return z;
            case 2:
                if (mFeature) {
                    z = enableResMonitor;
                }
                return z;
            default:
                return mFeature;
        }
    }

    private ArrayList<DumpData> getDumpData() {
        ArrayList<Integer> queueSizes = HwActivityManagerService.self().getIawareDumpData();
        if (queueSizes.size() < 12) {
            AwareLog.e(TAG, "get iaware srms dump data error,size is too small.");
            return null;
        }
        ArrayList<DumpData> dumpDatas = new ArrayList();
        long currTime = System.currentTimeMillis();
        int RESOURCE_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_RESOURCE);
        StringBuffer queueSizeBuffer = new StringBuffer();
        queueSizeBuffer.append(queueSizes.get(0));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(1));
        queueSizeBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(2));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(3));
        queueSizeBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(4));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(5));
        queueSizeBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(6));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(7));
        queueSizeBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(8));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(9));
        queueSizeBuffer.append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(10));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(11));
        dumpDatas.add(new DumpData(currTime, RESOURCE_FEATURE_ID, "fg.p&o#bg.p&o#fg3.p&o#bg3.p&o#fgk.p&o#fgk.p&o", 0, new String(queueSizeBuffer)));
        return dumpDatas;
    }

    private AwareBroadcastPolicy getIawareBrPolicy() {
        if (this.mIawareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
        return this.mIawareBrPolicy;
    }
}
