package com.huawei.uifirst.fastview.systemui;

import android.os.SystemProperties;
import com.huawei.uifirst.fastview.systemui.StatusDecouplingPolicy;
import java.util.HashMap;
import java.util.Map;

public class StatusDecouplingPolicyManager {
    private static final boolean MODEL_SYSTEMUI_ALL_PROP = SystemProperties.getBoolean("systemui_fastview_fast_response_enable", false);
    public static final int MODEL_SYSTEMUI_BLUETOOTH = 2;
    private static final boolean MODEL_SYSTEMUI_BLUETOOTH_PROP = true;
    public static final int MODEL_SYSTEMUI_HOTSPOT = 3;
    private static final boolean MODEL_SYSTEMUI_HOTSPOT_PROP = true;
    public static final int MODEL_SYSTEMUI_INSTANTSHARE = 4;
    private static final boolean MODEL_SYSTEMUI_INSTANTSHARE_PROP = true;
    public static final int MODEL_SYSTEMUI_NFC = 5;
    private static final boolean MODEL_SYSTEMUI_NFC_PROP = true;
    public static final int MODEL_SYSTEMUI_WLAN = 1;
    private static final boolean MODEL_SYSTEMUI_WLAN_PROP = true;
    private StatusDecoupling mStatusDecoupling;
    private Map<Integer, StatusDecoupling> mStatusDecouplingMap = new HashMap();

    public StatusDecoupling addStatusDecoupling(int modelId, StatusDecouplingPolicy.CallBack sdpCallback, int delayTime) {
        if (isStatusDecouplingPolicyAvailable(modelId)) {
            this.mStatusDecoupling = new StatusDecouplingPolicy(sdpCallback, delayTime, getModelName(modelId));
        } else {
            this.mStatusDecoupling = new StatusDecouplingDummy();
        }
        this.mStatusDecouplingMap.put(Integer.valueOf(modelId), this.mStatusDecoupling);
        return this.mStatusDecoupling;
    }

    public boolean isStatusDecouplingPolicyAvailable(int modelId) {
        if (modelId == 1) {
            return MODEL_SYSTEMUI_ALL_PROP;
        }
        if (modelId == 2) {
            return MODEL_SYSTEMUI_ALL_PROP;
        }
        if (modelId == 3) {
            return MODEL_SYSTEMUI_ALL_PROP;
        }
        if (modelId == 4) {
            return MODEL_SYSTEMUI_ALL_PROP;
        }
        if (modelId != 5) {
            return false;
        }
        return MODEL_SYSTEMUI_ALL_PROP;
    }

    public String getModelName(int modelId) {
        if (modelId == 1) {
            return "SystemUI_Wifi";
        }
        if (modelId == 2) {
            return "SystemUI_Bluetooth";
        }
        if (modelId == 3) {
            return "SystemUI_Hotspot";
        }
        if (modelId == 4) {
            return "SystemUI_Instantshare";
        }
        if (modelId != 5) {
            return "None";
        }
        return "SystemUI_Nfc";
    }

    public StatusDecoupling getStatusDecouplingPolicy(int modelId) {
        Map<Integer, StatusDecoupling> map = this.mStatusDecouplingMap;
        if (map == null) {
            return new StatusDecouplingDummy();
        }
        return map.get(Integer.valueOf(modelId));
    }
}
