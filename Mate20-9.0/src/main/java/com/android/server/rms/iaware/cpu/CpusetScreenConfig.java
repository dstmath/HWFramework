package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class CpusetScreenConfig extends CPUCustBaseConfig {
    private static final String CONFIG_CPUSCREEN_OFF = "cpuset_screen_off";
    private static final String CONFIG_CPUSCREEN_ON = "cpuset_screen_on";
    private static final String TAG = "CpusetScreenConfig";
    private CPUFeature mCPUFeatureInstance;
    private Map<String, String> mItem2PropMapScreenOFF = new ArrayMap();
    private Map<String, String> mItem2PropMapScreenOn = new ArrayMap();
    private Map<String, CPUPropInfoItem> mOffInfoMap = new ArrayMap();
    private Map<String, CPUPropInfoItem> mOnInfoMap = new ArrayMap();

    public CpusetScreenConfig() {
        init();
    }

    public void setConfig(CPUFeature feature) {
        this.mCPUFeatureInstance = feature;
        if (this.mCPUFeatureInstance == null) {
            AwareLog.e(TAG, "CPUFeature is null");
            return;
        }
        sendScreenConfig(this.mOnInfoMap, mCpusetItemIndex, CPUFeature.MSG_SET_CPUSETCONFIG_SCREENON);
        sendScreenConfig(this.mOffInfoMap, mCpusetItemIndex, 127);
    }

    private void init() {
        this.mItem2PropMapScreenOn.put("fg", "persist.sys.cpuset.fg_on");
        this.mItem2PropMapScreenOn.put("bg", "persist.sys.cpuset.bg_on");
        this.mItem2PropMapScreenOn.put("key_bg", "persist.sys.cpuset.keybg_on");
        this.mItem2PropMapScreenOn.put("sys_bg", "persist.sys.cpuset.sysbg_on");
        this.mItem2PropMapScreenOn.put("ta_boost", "persist.sys.cpuset.ta_boost_on");
        this.mItem2PropMapScreenOn.put("boost", "persist.sys.cpuset.boost_on");
        this.mItem2PropMapScreenOn.put("top_app", "persist.sys.cpuset.topapp_on");
        this.mItem2PropMapScreenOFF.put("fg", "persist.sys.cpuset.fg_off");
        this.mItem2PropMapScreenOFF.put("bg", "persist.sys.cpuset.bg_off");
        this.mItem2PropMapScreenOFF.put("key_bg", "persist.sys.cpuset.keybg_off");
        this.mItem2PropMapScreenOFF.put("sys_bg", "persist.sys.cpuset.sysbg_off");
        this.mItem2PropMapScreenOFF.put("ta_boost", "persist.sys.cpuset.ta_boost_off");
        this.mItem2PropMapScreenOFF.put("boost", "persist.sys.cpuset.boost_off");
        this.mItem2PropMapScreenOFF.put("top_app", "persist.sys.cpuset.topapp_off");
        obtainConfigInfo(CONFIG_CPUSCREEN_OFF, this.mItem2PropMapScreenOFF, this.mOffInfoMap);
        obtainConfigInfo(CONFIG_CPUSCREEN_ON, this.mItem2PropMapScreenOn, this.mOnInfoMap);
    }

    private void sendScreenConfig(Map<String, CPUPropInfoItem> infoMap, String[] itemIndex, int msg) {
        if (infoMap.isEmpty()) {
            AwareLog.d(TAG, "you don't set the xml, msg:" + msg);
            return;
        }
        int resCode = this.mCPUFeatureInstance.sendPacket(CPUXMLUtility.getConfigInfo(infoMap, itemIndex, msg));
        if (resCode != 1) {
            AwareLog.e(TAG, "sendConfig sendPacket failed, msg:" + msg + ",send error code:" + resCode);
        }
    }
}
