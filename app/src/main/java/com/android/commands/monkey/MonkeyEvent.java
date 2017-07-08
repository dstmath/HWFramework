package com.android.commands.monkey;

import android.app.IActivityManager;
import android.view.IWindowManager;

public abstract class MonkeyEvent {
    public static final int EVENT_TYPE_ACTIVITY = 4;
    public static final int EVENT_TYPE_FLIP = 5;
    public static final int EVENT_TYPE_KEY = 0;
    public static final int EVENT_TYPE_NOOP = 8;
    public static final int EVENT_TYPE_PERMISSION = 7;
    public static final int EVENT_TYPE_ROTATION = 3;
    public static final int EVENT_TYPE_THROTTLE = 6;
    public static final int EVENT_TYPE_TOUCH = 1;
    public static final int EVENT_TYPE_TRACKBALL = 2;
    public static final int INJECT_ERROR_REMOTE_EXCEPTION = -1;
    public static final int INJECT_ERROR_SECURITY_EXCEPTION = -2;
    public static final int INJECT_FAIL = 0;
    public static final int INJECT_SUCCESS = 1;
    protected int eventType;

    public abstract int injectEvent(IWindowManager iWindowManager, IActivityManager iActivityManager, int i);

    public MonkeyEvent(int type) {
        this.eventType = type;
    }

    public int getEventType() {
        return this.eventType;
    }

    public boolean isThrottlable() {
        return true;
    }
}
