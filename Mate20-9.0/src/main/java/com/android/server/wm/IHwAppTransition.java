package com.android.server.wm;

import android.content.Context;
import android.view.WindowManager;
import com.android.server.AttributeCache;

public interface IHwAppTransition {
    AttributeCache.Entry overrideAnimation(WindowManager.LayoutParams layoutParams, int i, Context context, AttributeCache.Entry entry, AppTransition appTransition);
}
