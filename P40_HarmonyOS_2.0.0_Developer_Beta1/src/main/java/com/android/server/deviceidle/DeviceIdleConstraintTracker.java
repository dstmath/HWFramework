package com.android.server.deviceidle;

public class DeviceIdleConstraintTracker {
    public boolean active = false;
    public final int minState;
    public boolean monitoring = false;
    public final String name;

    public DeviceIdleConstraintTracker(String name2, int minState2) {
        this.name = name2;
        this.minState = minState2;
    }
}
