package com.android.server.wm;

import android.graphics.Rect;

public interface IHwWindowManagerServiceEx {
    void adjustWindowPosForPadPC(Rect rect, Rect rect2, WindowState windowState, WindowState windowState2, WindowState windowState3);

    void layoutWindowForPadPCMode(WindowState windowState, WindowState windowState2, WindowState windowState3, Rect rect, Rect rect2, Rect rect3, Rect rect4, int i);

    int releaseSnapshots(int i);
}
