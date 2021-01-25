package com.android.server.wm;

import android.util.HwMwUtils;
import android.util.HwPCUtils;

public class HwActivityDisplayEx implements IHwActivityDisplayEx {
    public boolean launchMagicOnSplitScreenDismissed(ActivityStack top) {
        return top != null && top.inHwMagicWindowingMode() && !HwPCUtils.isPcCastModeInServer();
    }

    public boolean keepStackResumed(ActivityStack stack) {
        if (stack == null || !stack.inHwMagicWindowingMode() || !HwMwUtils.performPolicy(132, new Object[]{Integer.valueOf(stack.getStackId()), false}).getBoolean("RESULT_IN_APP_SPLIT", false)) {
            return false;
        }
        return true;
    }
}
