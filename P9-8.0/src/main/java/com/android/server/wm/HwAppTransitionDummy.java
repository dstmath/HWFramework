package com.android.server.wm;

import android.content.Context;
import android.view.WindowManager.LayoutParams;
import com.android.server.AttributeCache.Entry;

public class HwAppTransitionDummy implements IHwAppTransition {
    private static HwAppTransitionDummy mHwAppTransitionDummy = null;

    private HwAppTransitionDummy() {
    }

    public static HwAppTransitionDummy getDefault() {
        if (mHwAppTransitionDummy == null) {
            mHwAppTransitionDummy = new HwAppTransitionDummy();
        }
        return mHwAppTransitionDummy;
    }

    public Entry overrideAnimation(LayoutParams lp, int animAttr, Context mContext, Entry mEnt, AppTransition appTransition) {
        return null;
    }
}
