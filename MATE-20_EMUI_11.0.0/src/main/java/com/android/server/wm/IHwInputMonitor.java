package com.android.server.wm;

import android.view.InputWindowHandle;

public interface IHwInputMonitor {
    void updateInputMethodTouchRegion(InputWindowHandle inputWindowHandle, WindowState windowState);
}
