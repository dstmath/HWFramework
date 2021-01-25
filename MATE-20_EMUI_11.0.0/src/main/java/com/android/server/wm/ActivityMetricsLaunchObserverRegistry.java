package com.android.server.wm;

public interface ActivityMetricsLaunchObserverRegistry {
    void registerLaunchObserver(ActivityMetricsLaunchObserver activityMetricsLaunchObserver);

    void unregisterLaunchObserver(ActivityMetricsLaunchObserver activityMetricsLaunchObserver);
}
