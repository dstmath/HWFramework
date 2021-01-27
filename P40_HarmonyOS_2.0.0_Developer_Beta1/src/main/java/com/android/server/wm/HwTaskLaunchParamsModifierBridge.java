package com.android.server.wm;

import android.content.Context;
import com.huawei.server.wm.IHwTaskLaunchParamsModifierEx;
import java.util.HashMap;

public class HwTaskLaunchParamsModifierBridge implements IHwTaskLaunchParamsModifierEx {
    private HwTaskLaunchParamsModifierBridgeEx mModifierBridgeEx;

    public void setModifierBridgeEx(HwTaskLaunchParamsModifierBridgeEx modifierBridgeEx) {
        this.mModifierBridgeEx = modifierBridgeEx;
    }

    public HashMap<String, Integer> computeDefaultParaForFreeForm(ActivityDisplay activityDisplay, Context context) {
        ActivityDisplayEx activityDisplayEx = null;
        if (activityDisplay != null) {
            activityDisplayEx = new ActivityDisplayEx();
            activityDisplayEx.setActivityDisplay(activityDisplay);
        }
        return this.mModifierBridgeEx.computeDefaultParaForFreeForm(activityDisplayEx, context);
    }
}
