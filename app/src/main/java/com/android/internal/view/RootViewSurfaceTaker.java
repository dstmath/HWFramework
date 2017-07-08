package com.android.internal.view;

import android.view.InputQueue.Callback;
import android.view.SurfaceHolder.Callback2;

public interface RootViewSurfaceTaker {
    void onRootViewScrollYChanged(int i);

    void setSurfaceFormat(int i);

    void setSurfaceKeepScreenOn(boolean z);

    void setSurfaceType(int i);

    Callback willYouTakeTheInputQueue();

    Callback2 willYouTakeTheSurface();
}
