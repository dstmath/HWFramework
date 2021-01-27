package com.huawei.server.policy;

import android.content.Context;
import android.os.Handler;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;

public class DefaultPickUpWakeScreenManager {
    protected DefaultPickUpWakeScreenManager() {
    }

    public static synchronized DefaultPickUpWakeScreenManager getInstance() {
        DefaultPickUpWakeScreenManager defaultPickUpWakeScreenManager;
        synchronized (DefaultPickUpWakeScreenManager.class) {
            defaultPickUpWakeScreenManager = new DefaultPickUpWakeScreenManager();
        }
        return defaultPickUpWakeScreenManager;
    }

    public void initIfNeed(Context context, Handler handler, WindowManagerPolicyEx.WindowManagerFuncsEx windowManagerFuncsEx, KeyguardServiceDelegateEx keyguardDelegateEx) {
    }

    public void enablePickupMotionOrNot(boolean isScreenOff) {
    }

    public void stopTurnOffController() {
    }
}
