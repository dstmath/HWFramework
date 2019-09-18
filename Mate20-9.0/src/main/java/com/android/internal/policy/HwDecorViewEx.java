package com.android.internal.policy;

import android.content.Context;
import android.view.HwAppSceneImpl;

public class HwDecorViewEx implements IHwDecorViewEx {
    public void handleWindowFocusChanged(boolean hasWindowFocus, Context context) {
        if (hasWindowFocus) {
            HwAppSceneImpl.getDefault().handleWindowFocusChanged(context);
        }
    }
}
