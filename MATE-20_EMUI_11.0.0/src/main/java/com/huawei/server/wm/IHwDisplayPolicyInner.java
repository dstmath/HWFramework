package com.huawei.server.wm;

import com.android.server.wm.WindowState;

public interface IHwDisplayPolicyInner {
    WindowState getFocusedWindow();

    IHwDisplayPolicyEx getHwDisplayPolicyEx();

    WindowState getStatusBar();

    void setNavigationBarHeightDef(int[] iArr);

    void setNavigationBarWidthDef(int[] iArr);
}
