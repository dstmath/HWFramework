package com.android.server.gesture;

import android.content.Context;
import com.android.server.policy.WindowManagerPolicyEx;

public class DefaultHwGestureNavWhiteConfig {
    protected DefaultHwGestureNavWhiteConfig() {
    }

    public static synchronized DefaultHwGestureNavWhiteConfig getInstance() {
        DefaultHwGestureNavWhiteConfig defaultHwGestureNavWhiteConfig;
        synchronized (DefaultHwGestureNavWhiteConfig.class) {
            defaultHwGestureNavWhiteConfig = new DefaultHwGestureNavWhiteConfig();
        }
        return defaultHwGestureNavWhiteConfig;
    }

    public void updateWhitelistByHot(Context context, String fileName) {
    }

    public synchronized boolean isEnable(WindowManagerPolicyEx.WindowStateEx focusWindow, int rotation, WindowManagerPolicyEx policy) {
        return false;
    }
}
