package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.content.res.Configuration;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.proto.ProtoInputStream;
import android.util.proto.ProtoOutputStream;
import com.android.server.usage.IntervalStats;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

final class UsageStatsProto {
    private static String TAG = "UsageStatsProto";

    private UsageStatsProto() {
    }

    private static List<String> readStringPool(ProtoInputStream proto) throws IOException {
        List<String> stringPool;
        long token = proto.start(1146756268034L);
        if (proto.isNextField(1120986464257L)) {
            stringPool = new ArrayList<>(proto.readInt(1120986464257L));
        } else {
            stringPool = new ArrayList<>();
        }
        while (proto.nextField() != -1) {
            if (proto.getFieldNumber() == 2) {
                stringPool.add(proto.readString(2237677961218L));
            }
        }
        proto.end(token);
        return stringPool;
    }

    private static void loadUsageStats(ProtoInputStream proto, long fieldId, IntervalStats statsOut, List<String> stringPool) throws IOException {
        UsageStats stats;
        long token = proto.start(fieldId);
        if (proto.isNextField(1120986464258L)) {
            stats = statsOut.getOrCreateUsageStats(stringPool.get(proto.readInt(1120986464258L) - 1));
        } else if (proto.isNextField(1138166333441L)) {
            stats = statsOut.getOrCreateUsageStats(proto.readString(1138166333441L));
        } else {
            stats = new UsageStats();
        }
        while (proto.nextField() != -1) {
            switch (proto.getFieldNumber()) {
                case 1:
                    UsageStats tempPackage = statsOut.getOrCreateUsageStats(proto.readString(1138166333441L));
                    tempPackage.mLastTimeUsed = stats.mLastTimeUsed;
                    tempPackage.mTotalTimeInForeground = stats.mTotalTimeInForeground;
                    tempPackage.mLastEvent = stats.mLastEvent;
                    tempPackage.mAppLaunchCount = stats.mAppLaunchCount;
                    stats = tempPackage;
                    break;
                case 2:
                    UsageStats tempPackageIndex = statsOut.getOrCreateUsageStats(stringPool.get(proto.readInt(1120986464258L) - 1));
                    tempPackageIndex.mLastTimeUsed = stats.mLastTimeUsed;
                    tempPackageIndex.mTotalTimeInForeground = stats.mTotalTimeInForeground;
                    tempPackageIndex.mLastEvent = stats.mLastEvent;
                    tempPackageIndex.mAppLaunchCount = stats.mAppLaunchCount;
                    stats = tempPackageIndex;
                    break;
                case 3:
                    stats.mLastTimeUsed = statsOut.beginTime + proto.readLong(1112396529667L);
                    break;
                case 4:
                    stats.mTotalTimeInForeground = proto.readLong(1112396529668L);
                    break;
                case 5:
                    stats.mLastEvent = proto.readInt(1120986464261L);
                    break;
                case 6:
                    stats.mAppLaunchCount = proto.readInt(1120986464262L);
                    break;
                case 7:
                    long chooserToken = proto.start(2246267895815L);
                    loadChooserCounts(proto, stats);
                    proto.end(chooserToken);
                    break;
                case 8:
                    stats.mLastTimeForegroundServiceUsed = statsOut.beginTime + proto.readLong(1112396529672L);
                    break;
                case 9:
                    stats.mTotalTimeForegroundServiceUsed = proto.readLong(1112396529673L);
                    break;
                case 10:
                    stats.mLastTimeVisible = statsOut.beginTime + proto.readLong(1112396529674L);
                    break;
                case 11:
                    stats.mTotalTimeVisible = proto.readLong(1112396529675L);
                    break;
            }
        }
        if (stats.mLastTimeUsed == 0) {
            stats.mLastTimeUsed = statsOut.beginTime;
        }
        proto.end(token);
    }

    private static void loadCountAndTime(ProtoInputStream proto, long fieldId, IntervalStats.EventTracker tracker) throws IOException {
        long token = proto.start(fieldId);
        while (true) {
            int nextField = proto.nextField();
            if (nextField == -1) {
                proto.end(token);
                return;
            } else if (nextField == 1) {
                tracker.count = proto.readInt(1120986464257L);
            } else if (nextField == 2) {
                tracker.duration = proto.readLong(1112396529666L);
            }
        }
    }

    private static void loadChooserCounts(ProtoInputStream proto, UsageStats usageStats) throws IOException {
        ArrayMap<String, Integer> counts;
        if (usageStats.mChooserCounts == null) {
            usageStats.mChooserCounts = new ArrayMap();
        }
        String action = null;
        if (proto.isNextField(1138166333441L)) {
            action = proto.readString(1138166333441L);
            counts = (ArrayMap) usageStats.mChooserCounts.get(action);
            if (counts == null) {
                counts = new ArrayMap<>();
                usageStats.mChooserCounts.put(action, counts);
            }
        } else {
            counts = new ArrayMap<>();
        }
        while (true) {
            int nextField = proto.nextField();
            if (nextField == -1) {
                break;
            } else if (nextField != 1) {
                if (nextField == 3) {
                    long token = proto.start(2246267895811L);
                    loadCountsForAction(proto, counts);
                    proto.end(token);
                    break;
                }
            } else {
                action = proto.readString(1138166333441L);
                usageStats.mChooserCounts.put(action, counts);
            }
        }
        if (action == null) {
            usageStats.mChooserCounts.put("", counts);
        }
    }

    private static void loadCountsForAction(ProtoInputStream proto, ArrayMap<String, Integer> counts) throws IOException {
        String category = null;
        int count = 0;
        while (true) {
            int nextField = proto.nextField();
            if (nextField == -1) {
                break;
            } else if (nextField == 1) {
                category = proto.readString(1138166333441L);
            } else if (nextField == 3) {
                count = proto.readInt(1120986464259L);
            }
        }
        if (category == null) {
            counts.put("", Integer.valueOf(count));
        } else {
            counts.put(category, Integer.valueOf(count));
        }
    }

    private static void loadConfigStats(ProtoInputStream proto, long fieldId, IntervalStats statsOut) throws IOException {
        ConfigurationStats configStats;
        long token = proto.start(fieldId);
        boolean configActive = false;
        Configuration config = new Configuration();
        if (proto.isNextField(1146756268033L)) {
            config.readFromProto(proto, 1146756268033L);
            configStats = statsOut.getOrCreateConfigurationStats(config);
        } else {
            configStats = new ConfigurationStats();
        }
        while (true) {
            int nextField = proto.nextField();
            if (nextField == -1) {
                break;
            } else if (nextField == 1) {
                config.readFromProto(proto, 1146756268033L);
                ConfigurationStats temp = statsOut.getOrCreateConfigurationStats(config);
                temp.mLastTimeActive = configStats.mLastTimeActive;
                temp.mTotalTimeActive = configStats.mTotalTimeActive;
                temp.mActivationCount = configStats.mActivationCount;
                configStats = temp;
            } else if (nextField == 2) {
                configStats.mLastTimeActive = statsOut.beginTime + proto.readLong(1112396529666L);
            } else if (nextField == 3) {
                configStats.mTotalTimeActive = proto.readLong(1112396529667L);
            } else if (nextField == 4) {
                configStats.mActivationCount = proto.readInt(1120986464260L);
            } else if (nextField == 5) {
                configActive = proto.readBoolean(1133871366149L);
            }
        }
        if (configStats.mLastTimeActive == 0) {
            configStats.mLastTimeActive = statsOut.beginTime;
        }
        if (configActive) {
            statsOut.activeConfiguration = configStats.mConfiguration;
        }
        proto.end(token);
    }

    private static void loadEvent(ProtoInputStream proto, long fieldId, IntervalStats statsOut, List<String> stringPool) throws IOException {
        long token = proto.start(fieldId);
        UsageEvents.Event event = statsOut.buildEvent(proto, stringPool);
        proto.end(token);
        if (event.mPackage != null) {
            statsOut.events.insert(event);
            return;
        }
        throw new ProtocolException("no package field present");
    }

    private static void writeStringPool(ProtoOutputStream proto, IntervalStats stats) throws IOException {
        long token = proto.start(1146756268034L);
        int size = stats.mStringCache.size();
        proto.write(1120986464257L, size);
        for (int i = 0; i < size; i++) {
            proto.write(2237677961218L, stats.mStringCache.valueAt(i));
        }
        proto.end(token);
    }

    private static void writeUsageStats(ProtoOutputStream proto, long fieldId, IntervalStats stats, UsageStats usageStats) throws IOException {
        long token = proto.start(fieldId);
        int packageIndex = stats.mStringCache.indexOf(usageStats.mPackageName);
        if (packageIndex >= 0) {
            proto.write(1120986464258L, packageIndex + 1);
        } else {
            String str = TAG;
            Slog.w(str, "UsageStats package name (" + usageStats.mPackageName + ") not found in IntervalStats string cache");
            proto.write(1138166333441L, usageStats.mPackageName);
        }
        proto.write(1112396529667L, usageStats.mLastTimeUsed - stats.beginTime);
        proto.write(1112396529668L, usageStats.mTotalTimeInForeground);
        proto.write(1120986464261L, usageStats.mLastEvent);
        proto.write(1112396529672L, usageStats.mLastTimeForegroundServiceUsed - stats.beginTime);
        proto.write(1112396529673L, usageStats.mTotalTimeForegroundServiceUsed);
        proto.write(1112396529674L, usageStats.mLastTimeVisible - stats.beginTime);
        proto.write(1112396529675L, usageStats.mTotalTimeVisible);
        proto.write(1120986464262L, usageStats.mAppLaunchCount);
        writeChooserCounts(proto, usageStats);
        proto.end(token);
    }

    private static void writeCountAndTime(ProtoOutputStream proto, long fieldId, int count, long time) throws IOException {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, count);
        proto.write(1112396529666L, time);
        proto.end(token);
    }

    private static void writeChooserCounts(ProtoOutputStream proto, UsageStats usageStats) throws IOException {
        if (!(usageStats == null || usageStats.mChooserCounts == null || usageStats.mChooserCounts.keySet().isEmpty())) {
            int chooserCountSize = usageStats.mChooserCounts.size();
            for (int i = 0; i < chooserCountSize; i++) {
                String action = (String) usageStats.mChooserCounts.keyAt(i);
                ArrayMap<String, Integer> counts = (ArrayMap) usageStats.mChooserCounts.valueAt(i);
                if (!(action == null || counts == null || counts.isEmpty())) {
                    long token = proto.start(2246267895815L);
                    proto.write(1138166333441L, action);
                    writeCountsForAction(proto, counts);
                    proto.end(token);
                }
            }
        }
    }

    private static void writeCountsForAction(ProtoOutputStream proto, ArrayMap<String, Integer> counts) throws IOException {
        int countsSize = counts.size();
        for (int i = 0; i < countsSize; i++) {
            String key = counts.keyAt(i);
            int count = counts.valueAt(i).intValue();
            if (count > 0) {
                long token = proto.start(2246267895811L);
                proto.write(1138166333441L, key);
                proto.write(1120986464259L, count);
                proto.end(token);
            }
        }
    }

    private static void writeConfigStats(ProtoOutputStream proto, long fieldId, IntervalStats stats, ConfigurationStats configStats, boolean isActive) throws IOException {
        long token = proto.start(fieldId);
        configStats.mConfiguration.writeToProto(proto, 1146756268033L);
        proto.write(1112396529666L, configStats.mLastTimeActive - stats.beginTime);
        proto.write(1112396529667L, configStats.mTotalTimeActive);
        proto.write(1120986464260L, configStats.mActivationCount);
        proto.write(1133871366149L, isActive);
        proto.end(token);
    }

    private static void writeEvent(ProtoOutputStream proto, long fieldId, IntervalStats stats, UsageEvents.Event event) throws IOException {
        long token = proto.start(fieldId);
        int packageIndex = stats.mStringCache.indexOf(event.mPackage);
        if (packageIndex >= 0) {
            proto.write(1120986464258L, packageIndex + 1);
        } else {
            String str = TAG;
            Slog.w(str, "Usage event package name (" + event.mPackage + ") not found in IntervalStats string cache");
            proto.write(1138166333441L, event.mPackage);
        }
        if (event.mClass != null) {
            int classIndex = stats.mStringCache.indexOf(event.mClass);
            if (classIndex >= 0) {
                proto.write(1120986464260L, classIndex + 1);
            } else {
                String str2 = TAG;
                Slog.w(str2, "Usage event class name (" + event.mClass + ") not found in IntervalStats string cache");
                proto.write(1138166333443L, event.mClass);
            }
        }
        proto.write(1112396529669L, event.mTimeStamp - stats.beginTime);
        proto.write(1120986464262L, event.mFlags);
        proto.write(1120986464263L, event.mEventType);
        proto.write(1120986464270L, event.mInstanceId);
        if (event.mTaskRootPackage != null) {
            int taskRootPackageIndex = stats.mStringCache.indexOf(event.mTaskRootPackage);
            if (taskRootPackageIndex >= 0) {
                proto.write(1120986464271L, taskRootPackageIndex + 1);
            } else {
                String str3 = TAG;
                Slog.w(str3, "Usage event task root package name (" + event.mTaskRootPackage + ") not found in IntervalStats string cache");
            }
        }
        if (event.mTaskRootClass != null) {
            int taskRootClassIndex = stats.mStringCache.indexOf(event.mTaskRootClass);
            if (taskRootClassIndex >= 0) {
                proto.write(1120986464272L, taskRootClassIndex + 1);
            } else {
                String str4 = TAG;
                Slog.w(str4, "Usage event task root class name (" + event.mTaskRootClass + ") not found in IntervalStats string cache");
            }
        }
        int taskRootClassIndex2 = event.mEventType;
        if (taskRootClassIndex2 != 5) {
            if (taskRootClassIndex2 != 8) {
                if (taskRootClassIndex2 != 11) {
                    if (taskRootClassIndex2 == 12 && event.mNotificationChannelId != null) {
                        int channelIndex = stats.mStringCache.indexOf(event.mNotificationChannelId);
                        if (channelIndex >= 0) {
                            proto.write(1120986464269L, channelIndex + 1);
                        } else {
                            String str5 = TAG;
                            Slog.w(str5, "Usage event notification channel name (" + event.mNotificationChannelId + ") not found in IntervalStats string cache");
                            proto.write(1138166333452L, event.mNotificationChannelId);
                        }
                    }
                } else if (event.mBucketAndReason != 0) {
                    proto.write(1120986464267L, event.mBucketAndReason);
                }
            } else if (event.mShortcutId != null) {
                proto.write(1138166333449L, event.mShortcutId);
            }
        } else if (event.mConfiguration != null) {
            event.mConfiguration.writeToProto(proto, 1146756268040L);
        }
        proto.end(token);
    }

    public static void read(InputStream in, IntervalStats statsOut, boolean dropEvent) throws IOException {
        ProtoInputStream proto = new ProtoInputStream(in);
        List<String> stringPool = null;
        statsOut.packageStats.clear();
        statsOut.configurations.clear();
        statsOut.activeConfiguration = null;
        statsOut.events.clear();
        while (true) {
            int nextField = proto.nextField();
            if (nextField == -1) {
                if (statsOut.endTime == 0) {
                    statsOut.endTime = statsOut.beginTime;
                }
                statsOut.upgradeIfNeeded();
                return;
            } else if (nextField == 1) {
                statsOut.endTime = statsOut.beginTime + proto.readLong(1112396529665L);
            } else if (nextField == 2) {
                stringPool = readStringPool(proto);
                statsOut.mStringCache.addAll(stringPool);
            } else if (nextField == 3) {
                statsOut.majorVersion = proto.readInt(1120986464259L);
            } else if (nextField != 4) {
                switch (nextField) {
                    case 10:
                        loadCountAndTime(proto, 1146756268042L, statsOut.interactiveTracker);
                        continue;
                    case 11:
                        loadCountAndTime(proto, 1146756268043L, statsOut.nonInteractiveTracker);
                        continue;
                    case 12:
                        loadCountAndTime(proto, 1146756268044L, statsOut.keyguardShownTracker);
                        continue;
                    case 13:
                        loadCountAndTime(proto, 1146756268045L, statsOut.keyguardHiddenTracker);
                        continue;
                    default:
                        switch (nextField) {
                            case 20:
                                loadUsageStats(proto, 2246267895828L, statsOut, stringPool);
                                continue;
                            case 21:
                                loadConfigStats(proto, 2246267895829L, statsOut);
                                continue;
                            case FaceAcquiredInfo.VENDOR /* 22 */:
                                if (!dropEvent) {
                                    loadEvent(proto, 2246267895830L, statsOut, stringPool);
                                    break;
                                } else {
                                    continue;
                                }
                            default:
                                continue;
                        }
                }
            } else {
                statsOut.minorVersion = proto.readInt(1120986464260L);
            }
        }
    }

    public static void write(OutputStream out, IntervalStats stats) throws IOException {
        ProtoOutputStream proto = new ProtoOutputStream(out);
        proto.write(1112396529665L, stats.endTime - stats.beginTime);
        proto.write(1120986464259L, stats.majorVersion);
        proto.write(1120986464260L, stats.minorVersion);
        writeStringPool(proto, stats);
        writeCountAndTime(proto, 1146756268042L, stats.interactiveTracker.count, stats.interactiveTracker.duration);
        writeCountAndTime(proto, 1146756268043L, stats.nonInteractiveTracker.count, stats.nonInteractiveTracker.duration);
        writeCountAndTime(proto, 1146756268044L, stats.keyguardShownTracker.count, stats.keyguardShownTracker.duration);
        writeCountAndTime(proto, 1146756268045L, stats.keyguardHiddenTracker.count, stats.keyguardHiddenTracker.duration);
        int statsCount = stats.packageStats.size();
        for (int i = 0; i < statsCount; i++) {
            writeUsageStats(proto, 2246267895828L, stats, stats.packageStats.valueAt(i));
        }
        int configCount = stats.configurations.size();
        for (int i2 = 0; i2 < configCount; i2++) {
            writeConfigStats(proto, 2246267895829L, stats, stats.configurations.valueAt(i2), stats.activeConfiguration.equals(stats.configurations.keyAt(i2)));
        }
        int eventCount = stats.events.size();
        for (int i3 = 0; i3 < eventCount; i3++) {
            writeEvent(proto, 2246267895830L, stats, stats.events.get(i3));
        }
        proto.flush();
    }
}
