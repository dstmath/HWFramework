package com.android.server.policy;

import android.util.EventLog;

public class EventLogTags {
    public static final int INTERCEPT_POWER = 70001;
    public static final int SCREEN_TOGGLED = 70000;

    private EventLogTags() {
    }

    public static void writeScreenToggled(int screenState) {
        EventLog.writeEvent((int) SCREEN_TOGGLED, screenState);
    }

    public static void writeInterceptPower(String action, int mpowerkeyhandled, int mpowerkeypresscounter) {
        EventLog.writeEvent((int) INTERCEPT_POWER, action, Integer.valueOf(mpowerkeyhandled), Integer.valueOf(mpowerkeypresscounter));
    }
}
