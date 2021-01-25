package com.android.server.wm;

import android.content.Context;

public interface IHwScreenRotationAnimation {
    ScreenRotationAnimation create(Context context, DisplayContent displayContent, boolean z, boolean z2, WindowManagerService windowManagerService);
}
