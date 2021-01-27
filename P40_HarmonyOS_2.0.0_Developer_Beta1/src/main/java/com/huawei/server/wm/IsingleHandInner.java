package com.huawei.server.wm;

import com.android.server.wm.WindowManagerServiceEx;

/* access modifiers changed from: package-private */
public interface IsingleHandInner {
    void doAnimation(boolean z, boolean z2);

    void doQuickQuitLazyMode();

    WindowManagerServiceEx getWindowManagerServiceEx();

    boolean isDoAnimation();

    void relayoutMatrix();

    void setQuickQuitMode(boolean z);
}
