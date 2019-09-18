package com.android.server.gesture;

import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;

public interface GestureNavPolicy {
    void dump(String str, PrintWriter printWriter, String[] strArr);

    boolean isGestureNavStartedNotLocked();

    void onConfigurationChanged();

    boolean onFocusWindowChanged(WindowManagerPolicy.WindowState windowState, WindowManagerPolicy.WindowState windowState2);

    void onKeyguardShowingChanged(boolean z);

    void onLayoutInDisplayCutoutModeChanged(WindowManagerPolicy.WindowState windowState, boolean z, boolean z2);

    void onRotationChanged(int i);

    void onUserChanged(int i);

    void setGestureNavMode(String str, int i, int i2, int i3, int i4);

    void systemReady();
}
