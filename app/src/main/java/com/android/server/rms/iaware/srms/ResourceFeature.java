package com.android.server.rms.iaware.srms;

import android.content.Context;
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
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;

public class ResourceFeature extends RFeature {
    private static final /* synthetic */ int[] -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues = null;
    private static final int QUEUESIZE_FOR_DUMPDATA = 12;
    private static final String TAG = "ResourceFeature";
    static final boolean enableResMonitor = false;
    static final boolean enableResQueue = false;
    private static boolean mFeature;
    private AwareBroadcastPolicy mIawareBrPolicy;

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
            iArr[ResourceType.RESOURCE_BOOT_COMPLETED.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResourceType.RESOURCE_GAME_BOOST.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResourceType.RESOURCE_HOME.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResourceType.RESOURCE_INVALIDE_TYPE.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCENE_REC.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCREEN_OFF.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ResourceType.RESOURCE_SCREEN_ON.ordinal()] = 2;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ResourceType.RESOURCE_USERHABIT.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ResourceType.RES_APP.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ResourceType.RES_DEV_STATUS.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ResourceType.RES_INPUT.ordinal()] = QUEUESIZE_FOR_DUMPDATA;
        } catch (NoSuchFieldError e12) {
        }
        -android-rms-iaware-AwareConstant$ResourceTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.srms.ResourceFeature.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.srms.ResourceFeature.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.srms.ResourceFeature.<clinit>():void");
    }

    public ResourceFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        this.mIawareBrPolicy = null;
        if (MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
    }

    public boolean reportData(CollectData data) {
        switch (-getandroid-rms-iaware-AwareConstant$ResourceTypeSwitchesValues()[ResourceType.getResourceType(data.getResId()).ordinal()]) {
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                if (getIawareBrPolicy() != null) {
                    this.mIawareBrPolicy.reportSysEvent(90011);
                    break;
                }
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
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
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                if (mFeature) {
                    z = enableResQueue;
                }
                return z;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
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
        if (queueSizes.size() < QUEUESIZE_FOR_DUMPDATA) {
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
