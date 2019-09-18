package com.android.server.wm;

import com.android.server.input.InputWindowHandle;

public interface IHwInputMonitor {
    void updateInputMethodTouchRegion(InputWindowHandle inputWindowHandle, WindowState windowState);
}
