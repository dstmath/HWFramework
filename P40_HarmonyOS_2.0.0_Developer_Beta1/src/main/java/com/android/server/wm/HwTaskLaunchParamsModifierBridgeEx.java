package com.android.server.wm;

import android.content.Context;
import java.util.HashMap;

public class HwTaskLaunchParamsModifierBridgeEx {
    private HwTaskLaunchParamsModifierBridge mModifierBridge = new HwTaskLaunchParamsModifierBridge();

    public HwTaskLaunchParamsModifierBridgeEx() {
        this.mModifierBridge.setModifierBridgeEx(this);
    }

    public HwTaskLaunchParamsModifierBridge getModifierBridge() {
        return this.mModifierBridge;
    }

    public HashMap<String, Integer> computeDefaultParaForFreeForm(ActivityDisplayEx activityDisplay, Context context) {
        return null;
    }
}
