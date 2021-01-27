package com.android.server.gesture;

import android.graphics.Point;
import android.os.Looper;
import com.android.server.policy.WindowManagerPolicyEx;
import java.io.PrintWriter;

public interface GestureNavPolicy {
    void bringTopSubScreenNavView();

    void destroySubScreenNavView();

    void dump(String str, PrintWriter printWriter, String[] strArr);

    Looper getGestureLoooper();

    void initSubScreenNavView();

    boolean isGestureNavStartedNotLocked();

    boolean isKeyNavEnabled();

    boolean isPointInExcludedRegion(Point point);

    void notifyANR(CharSequence charSequence);

    void onConfigurationChanged();

    boolean onFocusWindowChanged(WindowManagerPolicyEx.WindowStateEx windowStateEx, WindowManagerPolicyEx.WindowStateEx windowStateEx2);

    void onKeyguardShowingChanged(boolean z);

    void onLayoutInDisplayCutoutModeChanged(WindowManagerPolicyEx.WindowStateEx windowStateEx, boolean z, boolean z2);

    void onLockTaskStateChanged(int i);

    void onMultiWindowChanged(int i);

    void onRotationChanged(int i);

    void onUserChanged(int i);

    void setGestureNavMode(String str, int i, int i2, int i3, int i4);

    void systemReady();

    void updateGestureNavRegion(boolean z, int i);
}
