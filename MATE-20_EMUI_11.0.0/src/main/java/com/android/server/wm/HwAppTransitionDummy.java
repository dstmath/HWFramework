package com.android.server.wm;

import android.content.Context;
import android.view.WindowManager;
import com.android.server.AttributeCache;

public class HwAppTransitionDummy implements IHwAppTransition {
    private static HwAppTransitionDummy sHwAppTransitionDummy = null;

    private HwAppTransitionDummy() {
    }

    public static HwAppTransitionDummy getDefault() {
        if (sHwAppTransitionDummy == null) {
            sHwAppTransitionDummy = new HwAppTransitionDummy();
        }
        return sHwAppTransitionDummy;
    }

    @Override // com.android.server.wm.IHwAppTransition
    public AttributeCache.Entry overrideAnimation(WindowManager.LayoutParams lp, int animAttr, Context mContext, AttributeCache.Entry mEnt, AppTransition appTransition) {
        return null;
    }
}
