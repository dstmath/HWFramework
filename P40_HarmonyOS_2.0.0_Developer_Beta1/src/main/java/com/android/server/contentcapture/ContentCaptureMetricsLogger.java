package com.android.server.contentcapture;

import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.service.contentcapture.FlushMetrics;
import android.util.StatsLog;
import java.util.List;

public final class ContentCaptureMetricsLogger {
    private ContentCaptureMetricsLogger() {
    }

    public static void writeServiceEvent(int eventType, String serviceName, String targetPackage) {
        StatsLog.write(207, eventType, serviceName, targetPackage);
    }

    public static void writeServiceEvent(int eventType, ComponentName service, ComponentName target) {
        writeServiceEvent(eventType, ComponentName.flattenToShortString(service), ComponentName.flattenToShortString(target));
    }

    public static void writeServiceEvent(int eventType, ComponentName service, String targetPackage) {
        writeServiceEvent(eventType, ComponentName.flattenToShortString(service), targetPackage);
    }

    public static void writeServiceEvent(int eventType, ComponentName service) {
        writeServiceEvent(eventType, ComponentName.flattenToShortString(service), (String) null);
    }

    public static void writeSetWhitelistEvent(ComponentName service, List<String> packages, List<ComponentName> activities) {
        String serviceName = ComponentName.flattenToShortString(service);
        StringBuilder stringBuilder = new StringBuilder();
        if (packages != null && packages.size() > 0) {
            int size = packages.size();
            stringBuilder.append(packages.get(0));
            for (int i = 1; i < size; i++) {
                stringBuilder.append(" ");
                stringBuilder.append(packages.get(i));
            }
        }
        if (activities != null && activities.size() > 0) {
            stringBuilder.append(" ");
            stringBuilder.append(activities.get(0).flattenToShortString());
            int size2 = activities.size();
            for (int i2 = 1; i2 < size2; i2++) {
                stringBuilder.append(" ");
                stringBuilder.append(activities.get(i2).flattenToShortString());
            }
        }
        StatsLog.write(207, 3, serviceName, stringBuilder.toString());
    }

    public static void writeSessionEvent(int sessionId, int event, int flags, ComponentName service, ComponentName app, boolean isChildSession) {
        StatsLog.write(208, sessionId, event, flags, ComponentName.flattenToShortString(service), ComponentName.flattenToShortString(app), isChildSession);
    }

    public static void writeSessionFlush(int sessionId, ComponentName service, ComponentName app, FlushMetrics fm, ContentCaptureOptions options, int flushReason) {
        StatsLog.write(209, sessionId, ComponentName.flattenToShortString(service), ComponentName.flattenToShortString(app), fm.sessionStarted, fm.sessionFinished, fm.viewAppearedCount, fm.viewDisappearedCount, fm.viewTextChangedCount, options.maxBufferSize, options.idleFlushingFrequencyMs, options.textChangeFlushingFrequencyMs, flushReason);
    }
}
