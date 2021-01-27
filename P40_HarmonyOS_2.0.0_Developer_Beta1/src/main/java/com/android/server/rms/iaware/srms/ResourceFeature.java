package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.am.HwActivityManagerService;
import com.android.server.rms.HwSysResManagerService;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.feature.RFeature;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.ArrayList;

public class ResourceFeature extends RFeature {
    static final boolean ENABLE_RES_MONITOR = SystemPropertiesEx.getBoolean("ro.config.res_monitor", true);
    static final boolean ENABLE_RES_QUEUE = SystemPropertiesEx.getBoolean("ro.config.res_queue", true);
    private static final int QUEUESIZE_FOR_DUMPDATA = 12;
    private static final String TAG = "ResourceFeature";
    private static boolean sFeature;

    static {
        boolean z = true;
        if (!SystemPropertiesEx.getBoolean("persist.sys.enable_iaware", false) || !SystemPropertiesEx.getBoolean("persist.sys.srms.enable", true)) {
            z = false;
        }
        sFeature = z;
    }

    public ResourceFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        setEnable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        setDisable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean configUpdate() {
        HwSysResManagerService.self().cloudFileUpdate();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String saveBigData(boolean clear) {
        if (sFeature) {
            return SrmsDumpRadar.getInstance().saveSrmsBigData(clear);
        }
        AwareLog.e(TAG, "iaware srms is close, it is invalid operation to save big data.");
        return null;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public ArrayList<StatisticsData> getStatisticsData() {
        if (sFeature) {
            return SrmsDumpRadar.getInstance().getStatisticsData();
        }
        AwareLog.e(TAG, "iaware srms is close, it is invalid operation to get statistics data.");
        return null;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public ArrayList<DumpData> getDumpData(int time) {
        if (sFeature) {
            return getDumpData();
        }
        AwareLog.e(TAG, "iaware srms is close, it is invalid operation to get dump data.");
        return null;
    }

    private void setEnable() {
        AwareLog.i(TAG, "open iaware srms feature!");
        sFeature = true;
    }

    private void setDisable() {
        AwareLog.i(TAG, "close iaware srms feature!");
        sFeature = false;
    }

    public static boolean getIawareResourceFeature(int type) {
        if (type == 1) {
            return sFeature && ENABLE_RES_QUEUE;
        }
        if (type != 2) {
            return sFeature;
        }
        return sFeature && ENABLE_RES_MONITOR;
    }

    private ArrayList<DumpData> getDumpData() {
        ArrayList<Integer> queueSizes = HwActivityManagerService.self().getIawareDumpData();
        if (queueSizes.size() < 12) {
            AwareLog.e(TAG, "get iaware srms dump data error,size is too small.");
            return null;
        }
        StringBuffer queueSizeBuffer = new StringBuffer();
        queueSizeBuffer.append(queueSizes.get(0));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(1));
        queueSizeBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(2));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(3));
        queueSizeBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(4));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(5));
        queueSizeBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(6));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(7));
        queueSizeBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(8));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(9));
        queueSizeBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
        queueSizeBuffer.append(queueSizes.get(10));
        queueSizeBuffer.append("&");
        queueSizeBuffer.append(queueSizes.get(11));
        String queueSizeString = new String(queueSizeBuffer);
        int resourceFeatureId = AwareConstant.FeatureType.getFeatureId(AwareConstant.FeatureType.FEATURE_RESOURCE);
        ArrayList<DumpData> dumpDatas = new ArrayList<>();
        dumpDatas.add(new DumpData(System.currentTimeMillis(), resourceFeatureId, "fg.p&o#bg.p&o#fg3.p&o#bg3.p&o#fgk.p&o#fgk.p&o", 0, queueSizeString));
        return dumpDatas;
    }
}
