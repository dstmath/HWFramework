package com.android.server.timezone;

interface PackageTrackerIntentHelper {
    void initialize(String str, String str2, PackageTracker packageTracker);

    void scheduleReliabilityTrigger(long j);

    void sendTriggerUpdateCheck(CheckToken checkToken);

    void unscheduleReliabilityTrigger();
}
