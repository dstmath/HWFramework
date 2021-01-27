package com.android.server.timezone;

interface ConfigHelper {
    int getCheckTimeAllowedMillis();

    String getDataAppPackageName();

    int getFailedCheckRetryCount();

    String getUpdateAppPackageName();

    boolean isTrackingEnabled();
}
