package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import com.android.server.policy.WindowManagerPolicyEx;

public class DefaultHwScreenOnProximityLock {
    public static final int FORCE_QUIT = 0;
    public static final int LOCK_GOAWAY = 3;
    public static final int SCREEN_OFF = 1;

    public DefaultHwScreenOnProximityLock(Context context, HwPhoneWindowManager phoneWindowManager, WindowManagerPolicyEx.WindowManagerFuncsEx windowFuncs, Handler handler) {
    }

    public void registerDeviceListener() {
    }

    public void unregisterDeviceListener() {
    }

    public void acquireLock(WindowManagerPolicyEx policy, int mode) {
    }

    public void releaseLock(int reason) {
    }

    public boolean isShowing() {
        return false;
    }

    public void forceShowHint() {
    }

    public void refreshForRotationChange(int rotation) {
    }

    public void forceRefreshHintView() {
    }

    public void refreshHintTextView() {
    }

    public void swipeExitHintView() {
    }
}
