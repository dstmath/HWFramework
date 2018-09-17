package com.android.server.wm;

public interface AppWindowContainerListener extends WindowContainerListener {
    boolean keyDispatchingTimedOut(String str, int i);

    void onStartingWindowDrawn(long j);

    void onWindowsDrawn(long j);

    void onWindowsGone();

    void onWindowsVisible();
}
