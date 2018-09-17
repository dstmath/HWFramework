package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import java.util.HashMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class VirtualRealityConfig extends CPUCustBaseConfig {
    private static final String CONFIG_VR = "cpuset_VR";
    private static final String TAG = "VirtualRealityConfig";
    private CPUFeature mCPUFeatureInstance;
    private Map<String, CPUPropInfoItem> mVRInfoMap;
    private Map<String, String> mVRItem2PropMap;

    public VirtualRealityConfig() {
        this.mVRItem2PropMap = new HashMap();
        this.mVRInfoMap = new HashMap();
        init();
    }

    public void setConfig(CPUFeature feature) {
        this.mCPUFeatureInstance = feature;
        if (this.mCPUFeatureInstance == null) {
            AwareLog.e(TAG, "CPUFeatureInstance is null");
        } else if (this.mVRInfoMap.isEmpty()) {
            AwareLog.d(TAG, "you don set vr config in xml");
        } else {
            int resCode = this.mCPUFeatureInstance.sendPacket(CPUXMLUtility.getConfigInfo(this.mVRInfoMap, mCpusetItemIndex, CPUFeature.MSG_SET_CPUSETCONFIG_VR));
            if (resCode != 1) {
                AwareLog.e(TAG, "sendConfig sendPacket failed, msg:,send error code:" + resCode);
            }
        }
    }

    void init() {
        this.mVRItem2PropMap.put("fg", "vr_fg");
        this.mVRItem2PropMap.put("bg", "vr_bg");
        this.mVRItem2PropMap.put("key_bg", "vr_keybg");
        this.mVRItem2PropMap.put("sys_bg", "vr_sysbg");
        this.mVRItem2PropMap.put("ta_boost", "vr_taboost");
        this.mVRItem2PropMap.put("boost", "vr_boost");
        this.mVRItem2PropMap.put("top_app", "vr_top_app");
        obtainConfigInfo(CONFIG_VR, this.mVRItem2PropMap, this.mVRInfoMap);
    }
}
