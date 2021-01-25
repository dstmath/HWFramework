package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.Map;

/* compiled from: CpuXmlConfiguration */
class CpusetScreenConfig extends CpuCustBaseConfig {
    private static final String CONFIG_CPUSCREEN_OFF = "cpuset_screen_off";
    private static final String CONFIG_CPUSCREEN_ON = "cpuset_screen_on";
    private static final String TAG = "CpusetScreenConfig";
    private CpuFeature mCpuFeatureInstance;
    private Map<String, String> mItemToPropMapScreenOff = new ArrayMap();
    private Map<String, String> mItemToPropMapScreenOn = new ArrayMap();
    private Map<String, CpuPropInfoItem> mOffInfoMap = new ArrayMap();
    private Map<String, CpuPropInfoItem> mOnInfoMap = new ArrayMap();

    CpusetScreenConfig() {
        init();
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        this.mCpuFeatureInstance = feature;
        if (this.mCpuFeatureInstance == null) {
            AwareLog.e(TAG, "CpuFeature is null");
            return;
        }
        sendScreenConfig(this.mOnInfoMap, CPUSET_ITEM_INDEX, CpuFeature.MSG_SET_CPUSETCONFIG_SCREENON);
        sendScreenConfig(this.mOffInfoMap, CPUSET_ITEM_INDEX, CpuFeature.MSG_SET_CPUSETCONFIG_SCREENOFF);
    }

    private void init() {
        this.mItemToPropMapScreenOn.put("fg", "persist.sys.cpuset.fg_on");
        this.mItemToPropMapScreenOn.put("bg", "persist.sys.cpuset.bg_on");
        this.mItemToPropMapScreenOn.put("key_bg", "persist.sys.cpuset.keybg_on");
        this.mItemToPropMapScreenOn.put("sys_bg", "persist.sys.cpuset.sysbg_on");
        this.mItemToPropMapScreenOn.put("ta_boost", "persist.sys.cpuset.ta_boost_on");
        this.mItemToPropMapScreenOn.put("boost", "persist.sys.cpuset.boost_on");
        this.mItemToPropMapScreenOn.put("top_app", "persist.sys.cpuset.topapp_on");
        this.mItemToPropMapScreenOff.put("fg", "persist.sys.cpuset.fg_off");
        this.mItemToPropMapScreenOff.put("bg", "persist.sys.cpuset.bg_off");
        this.mItemToPropMapScreenOff.put("key_bg", "persist.sys.cpuset.keybg_off");
        this.mItemToPropMapScreenOff.put("sys_bg", "persist.sys.cpuset.sysbg_off");
        this.mItemToPropMapScreenOff.put("ta_boost", "persist.sys.cpuset.ta_boost_off");
        this.mItemToPropMapScreenOff.put("boost", "persist.sys.cpuset.boost_off");
        this.mItemToPropMapScreenOff.put("top_app", "persist.sys.cpuset.topapp_off");
        obtainConfigInfo(CONFIG_CPUSCREEN_OFF, this.mItemToPropMapScreenOff, this.mOffInfoMap);
        obtainConfigInfo(CONFIG_CPUSCREEN_ON, this.mItemToPropMapScreenOn, this.mOnInfoMap);
    }

    private void sendScreenConfig(Map<String, CpuPropInfoItem> infoMap, String[] itemIndex, int msg) {
        if (infoMap.isEmpty()) {
            AwareLog.d(TAG, "you don't set the xml, msg:" + msg);
            return;
        }
        int resCode = this.mCpuFeatureInstance.sendPacket(CpuXmlUtility.getConfigInfo(infoMap, itemIndex, msg));
        if (resCode != 1) {
            AwareLog.e(TAG, "sendConfig sendPacket failed, msg:" + msg + ",send error code:" + resCode);
        }
    }
}
