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
    private IStatusDecoupling mStatusDecoupling;
    private Map<Integer, IStatusDecoupling> mStatusDecouplingMap = new HashMap();

    public IStatusDecoupling addStatusDecoupling(int modelID, StatusDecouplingPolicy.CallBack sdpCallback, int delayTime) {
        if (isStatusDecouplingPolicyAvailable(modelID)) {
            this.mStatusDecoupling = new StatusDecouplingPolicy(sdpCallback, delayTime, getModelName(modelID));
        } else {
            this.mStatusDecoupling = new StatusDecouplingDummy();
        }
        this.mStatusDecouplingMap.put(Integer.valueOf(modelID), this.mStatusDecoupling);
        return this.mStatusDecoupling;
    }

    public boolean isStatusDecouplingPolicyAvailable(int modelID) {
        switch (modelID) {
            case 1:
                return MODEL_SYSTEMUI_ALL_PROP;
            case 2:
                return MODEL_SYSTEMUI_ALL_PROP;
            case 3:
                return MODEL_SYSTEMUI_ALL_PROP;
            case 4:
                return MODEL_SYSTEMUI_ALL_PROP;
            case 5:
                return MODEL_SYSTEMUI_ALL_PROP;
            default:
                return false;
        }
    }

    public String getModelName(int modelID) {
        switch (modelID) {
            case 1:
                return "SystemUI_Wifi";
            case 2:
                return "SystemUI_Bluetooth";
            case 3:
                return "SystemUI_Hotspot";
            case 4:
                return "SystemUI_Instantshare";
            case 5:
                return "SystemUI_Nfc";
            default:
                return "None";
        }
    }

    public IStatusDecoupling getStatusDecouplingPolicy(int modelID) {
        if (this.mStatusDecouplingMap == null) {
            return new StatusDecouplingDummy();
        }
        return this.mStatusDecouplingMap.get(Integer.valueOf(modelID));
    }
}
