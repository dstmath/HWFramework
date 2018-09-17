package com.android.server.wm;

import android.content.Context;
import android.view.WindowManager.LayoutParams;
import com.android.server.AttributeCache.Entry;

public interface IHwAppTransition {
    Entry overrideAnimation(LayoutParams layoutParams, int i, Context context, Entry entry, AppTransition appTransition);
}
