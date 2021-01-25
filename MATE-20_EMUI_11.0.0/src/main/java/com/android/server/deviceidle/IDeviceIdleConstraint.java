package com.android.server.deviceidle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IDeviceIdleConstraint {
    public static final int ACTIVE = 0;
    public static final int SENSING_OR_ABOVE = 1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface MinimumState {
    }

    void startMonitoring();

    void stopMonitoring();
}
