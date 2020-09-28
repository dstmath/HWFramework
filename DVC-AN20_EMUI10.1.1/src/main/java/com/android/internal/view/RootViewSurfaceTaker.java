package com.android.internal.view;

import android.view.InputQueue;
import android.view.SurfaceHolder;

public interface RootViewSurfaceTaker {
    void onRootViewScrollYChanged(int i);

    void setSurfaceFormat(int i);

    void setSurfaceKeepScreenOn(boolean z);

    void setSurfaceType(int i);

    InputQueue.Callback willYouTakeTheInputQueue();

    SurfaceHolder.Callback2 willYouTakeTheSurface();
}
