package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class FreqInteractiveConfig extends CPUCustBaseConfig {
    private static final String CONFIG_FREQINTERACTIVE = "freq_interactive";
    private static final String CONFIG_POWER_NOSAVE = "power_nosave_freq";
    private static final String CONFIG_POWER_SAVE = "power_save_freq";
    private static final String CONFIG_POWER_SUPERSAVE = "power_supersave_freq";
    private static final String TAG = "FreqInteractiveConfig";
    private CPUFeature mCPUFeatureInstance;
    private Map<String, CPUPropInfoItem> mFreqInterInfo = new ArrayMap();
    private Map<String, String> mFreqInterItem2PropMap = new ArrayMap();
    private Map<String, CPUPropInfoItem> mNoSaveInfoMap = new ArrayMap();
    private Map<String, String> mNoSaveItem2PropMap = new ArrayMap();
    private Map<String, CPUPropInfoItem> mSaveInfoMap = new ArrayMap();
    private Map<String, String> mSaveItem2PropMap = new ArrayMap();
    private Map<String, CPUPropInfoItem> mSuperSaveInfoMap = new ArrayMap();
    private Map<String, String> mSuperSaveItem2PropMap = new ArrayMap();

    public FreqInteractiveConfig() {
        init();
    }

    public void setConfig(CPUFeature feature) {
        this.mCPUFeatureInstance = feature;
        if (this.mCPUFeatureInstance == null) {
            AwareLog.e(TAG, "CPUFeature is null");
            return;
        }
        sendFreqInteractiveConfig(this.mFreqInterInfo, mInteractiveItemIndex, 128);
        sendFreqInteractiveConfig(this.mSaveInfoMap, mInteractiveItemIndex, CPUFeature.MSG_SET_INTERACTIVE_SAVE);
        sendFreqInteractiveConfig(this.mNoSaveInfoMap, mInteractiveItemIndex, 130);
        sendFreqInteractiveConfig(this.mSuperSaveInfoMap, mInteractiveItemIndex, CPUFeature.MSG_SET_INTERACTIVE_SPSAVE);
    }

    private void init() {
        this.mFreqInterItem2PropMap.put("go_hispeed_load_b", "persist.sys.set.load.b");
        this.mFreqInterItem2PropMap.put("go_hispeed_load_l", "persist.sys.set.load.l");
        this.mFreqInterItem2PropMap.put("hispeed_freq_b", "persist.sys.set.hispeed.b");
        this.mFreqInterItem2PropMap.put("hispeed_freq_l", "persist.sys.set.hispeed.l");
        this.mFreqInterItem2PropMap.put("target_load_freq_b", "persist.sys.set.targets.b");
        this.mFreqInterItem2PropMap.put("target_load_freq_l", "persist.sys.set.targets.l");
        this.mSaveItem2PropMap.put("go_hispeed_load_b", "persist.sys.save.cpu.load.b");
        this.mSaveItem2PropMap.put("go_hispeed_load_l", "persist.sys.save.cpu.load.l");
        this.mSaveItem2PropMap.put("hispeed_freq_b", "persist.sys.save.hispeed.b");
        this.mSaveItem2PropMap.put("hispeed_freq_l", "persist.sys.save.hispeed.l");
        this.mSaveItem2PropMap.put("target_load_freq_b", "persist.sys.save.targets.b");
        this.mSaveItem2PropMap.put("target_load_freq_l", "persist.sys.save.targets.l");
        this.mNoSaveItem2PropMap.put("go_hispeed_load_b", "persist.sys.nosave.cpu.load.b");
        this.mNoSaveItem2PropMap.put("go_hispeed_load_l", "persist.sys.nosave.cpu.load.l");
        this.mNoSaveItem2PropMap.put("hispeed_freq_b", "persist.sys.nosave.hispeed.b");
        this.mNoSaveItem2PropMap.put("hispeed_freq_l", "persist.sys.nosave.hispeed.l");
        this.mNoSaveItem2PropMap.put("target_load_freq_b", "persist.sys.nosave.targets.b");
        this.mNoSaveItem2PropMap.put("target_load_freq_l", "persist.sys.nosave.targets.l");
        this.mSuperSaveItem2PropMap.put("go_hispeed_load_b", "persist.sys.spsave.cpu.load.b");
        this.mSuperSaveItem2PropMap.put("go_hispeed_load_l", "persist.sys.spsave.cpu.load.l");
        this.mSuperSaveItem2PropMap.put("hispeed_freq_b", "persist.sys.spsave.hispeed.b");
        this.mSuperSaveItem2PropMap.put("hispeed_freq_l", "persist.sys.spsave.hispeed.l");
        this.mSuperSaveItem2PropMap.put("target_load_freq_b", "persist.sys.spsave.targets.b");
        this.mSuperSaveItem2PropMap.put("target_load_freq_l", "persist.sys.spsave.targets.l");
        obtainConfigInfo(CONFIG_FREQINTERACTIVE, this.mFreqInterItem2PropMap, this.mFreqInterInfo);
        obtainConfigInfo(CONFIG_POWER_NOSAVE, this.mNoSaveItem2PropMap, this.mNoSaveInfoMap);
        obtainConfigInfo(CONFIG_POWER_SAVE, this.mSaveItem2PropMap, this.mSaveInfoMap);
        obtainConfigInfo(CONFIG_POWER_SUPERSAVE, this.mSuperSaveItem2PropMap, this.mSuperSaveInfoMap);
    }

    private void sendFreqInteractiveConfig(Map<String, CPUPropInfoItem> infoMap, String[] itemIndex, int msg) {
        if (infoMap.isEmpty()) {
            AwareLog.w(TAG, "you don't set xml, msg:" + msg);
            return;
        }
        int resCode = this.mCPUFeatureInstance.sendPacket(CPUXMLUtility.getConfigInfo(infoMap, itemIndex, msg));
        if (resCode != 1) {
            AwareLog.e(TAG, "sendConfig sendPacket failed, msg:" + msg + ",send error code:" + resCode);
        }
    }
}
