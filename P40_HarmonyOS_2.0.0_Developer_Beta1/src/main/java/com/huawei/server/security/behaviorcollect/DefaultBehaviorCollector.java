package com.huawei.server.security.behaviorcollect;

import android.content.Context;
import com.huawei.server.policy.WindowManagerFuncsEx;

public class DefaultBehaviorCollector {
    public static final int EVENT_GAME_OFF = 2;
    public static final int EVENT_GAME_ON = 1;
    private static final Object LOCK = new Object();
    private static volatile DefaultBehaviorCollector sInstance;
    public static volatile boolean sIsActiveTouched = false;

    public static DefaultBehaviorCollector getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DefaultBehaviorCollector();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context, WindowManagerFuncsEx windowManagerFuncs) {
    }

    public void notifyEvent(int eventType) {
    }
}
