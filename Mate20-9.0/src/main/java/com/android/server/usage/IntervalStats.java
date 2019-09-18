package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.EventList;
import android.app.usage.EventStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.content.res.Configuration;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwPCUtils;
import java.util.List;

class IntervalStats {
    public Configuration activeConfiguration;
    public long beginTime;
    public final ArrayMap<Configuration, ConfigurationStats> configurations = new ArrayMap<>();
    public long endTime;
    public EventList events;
    public final EventTracker interactiveTracker = new EventTracker();
    int intervalType;
    public final EventTracker keyguardHiddenTracker = new EventTracker();
    public final EventTracker keyguardShownTracker = new EventTracker();
    public long lastTimeSaved;
    private final ArraySet<String> mStringCache = new ArraySet<>();
    public final EventTracker nonInteractiveTracker = new EventTracker();
    private String packageInForeground;
    public final ArrayMap<String, UsageStats> packageStats = new ArrayMap<>();

    public static final class EventTracker {
        public int count;
        public long curStartTime;
        public long duration;
        public long lastEventTime;

        public void commitTime(long timeStamp) {
            if (this.curStartTime != 0) {
                this.duration += timeStamp - this.duration;
                this.curStartTime = 0;
            }
        }

        public void update(long timeStamp) {
            if (this.curStartTime == 0) {
                this.count++;
            }
            commitTime(timeStamp);
            this.curStartTime = timeStamp;
            this.lastEventTime = timeStamp;
        }

        /* access modifiers changed from: package-private */
        public void addToEventStats(List<EventStats> out, int event, long beginTime, long endTime) {
            if (this.count != 0 || this.duration != 0) {
                EventStats ev = new EventStats();
                ev.mEventType = event;
                ev.mCount = this.count;
                ev.mTotalTime = this.duration;
                ev.mLastEventTime = this.lastEventTime;
                ev.mBeginTimeStamp = beginTime;
                ev.mEndTimeStamp = endTime;
                out.add(ev);
            }
        }
    }

    IntervalStats() {
    }

    /* access modifiers changed from: package-private */
    public UsageStats getOrCreateUsageStats(String packageName) {
        UsageStats usageStats = this.packageStats.get(packageName);
        if (usageStats != null) {
            return usageStats;
        }
        UsageStats usageStats2 = new UsageStats();
        usageStats2.mPackageName = getCachedStringRef(packageName);
        usageStats2.mBeginTimeStamp = this.beginTime;
        usageStats2.mEndTimeStamp = this.endTime;
        this.packageStats.put(usageStats2.mPackageName, usageStats2);
        return usageStats2;
    }

    /* access modifiers changed from: package-private */
    public ConfigurationStats getOrCreateConfigurationStats(Configuration config) {
        ConfigurationStats configStats = this.configurations.get(config);
        if (configStats != null) {
            return configStats;
        }
        ConfigurationStats configStats2 = new ConfigurationStats();
        configStats2.mBeginTimeStamp = this.beginTime;
        configStats2.mEndTimeStamp = this.endTime;
        configStats2.mConfiguration = config;
        this.configurations.put(config, configStats2);
        return configStats2;
    }

    /* access modifiers changed from: package-private */
    public UsageEvents.Event buildEvent(String packageName, String className) {
        UsageEvents.Event event = new UsageEvents.Event();
        event.mPackage = getCachedStringRef(packageName);
        if (className != null) {
            event.mClass = getCachedStringRef(className);
        }
        return event;
    }

    private boolean isStatefulEvent(int eventType) {
        switch (eventType) {
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isUserVisibleEvent(int eventType) {
        return (eventType == 6 || eventType == 11) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void update(String packageName, long timeStamp, int eventType) {
        update(packageName, timeStamp, eventType, 0);
    }

    /* access modifiers changed from: package-private */
    public void update(String packageName, long timeStamp, int eventType, int displayId) {
        UsageStats usageStats = getOrCreateUsageStats(packageName);
        if ((eventType == 2 || eventType == 3) && (usageStats.mLastEvent == 1 || usageStats.mLastEvent == 4)) {
            if (this.intervalType == 0) {
                Flog.i(1701, "usagestats update: pkg=" + packageName + ", intervalType=" + this.intervalType + " timeStamp=" + timeStamp + ",type=" + eventType + ",beginTime=" + usageStats.mBeginTimeStamp + ",lastTotalTime=" + usageStats.mTotalTimeInForeground + ",lastTimeUsed=" + usageStats.mLastTimeUsed + ",deltaTime=" + (timeStamp - usageStats.mLastTimeUsed) + ",totalTime=" + ((usageStats.mTotalTimeInForeground + timeStamp) - usageStats.mLastTimeUsed) + ",LaunchCount=" + usageStats.mLaunchCount);
            }
            usageStats.mTotalTimeInForeground += timeStamp - usageStats.mLastTimeUsed;
            if (isLandscape(this.activeConfiguration)) {
                usageStats.mLandTimeInForeground += timeStamp - usageStats.mLastLandTimeUsed;
            }
            if (displayId == HwPCUtils.getPCDisplayID()) {
                if (HwPCUtils.getIsWifiMode()) {
                    usageStats.mTimeInWirelessPCForeground += timeStamp - usageStats.mLastTimeUsedInWirelessPC;
                } else {
                    usageStats.mTimeInPCForeground += timeStamp - usageStats.mLastTimeUsedInPC;
                }
            }
        }
        if (isStatefulEvent(eventType)) {
            usageStats.mLastEvent = eventType;
        }
        if (isStatefulEvent(eventType)) {
            usageStats.mLastTimeUsed = timeStamp;
            this.packageInForeground = packageName;
            if (isLandscape(this.activeConfiguration)) {
                usageStats.mLastLandTimeUsed = timeStamp;
            }
            if (displayId == HwPCUtils.getPCDisplayID()) {
                if (HwPCUtils.getIsWifiMode()) {
                    usageStats.mLastTimeUsedInWirelessPC = timeStamp;
                } else {
                    usageStats.mLastTimeUsedInPC = timeStamp;
                }
            }
        }
        usageStats.mEndTimeStamp = timeStamp;
        if (eventType == 1) {
            usageStats.mLaunchCount++;
        }
        if (eventType == 4) {
            usageStats.mLaunchCount++;
            usageStats.mAppLaunchCount++;
        }
        this.endTime = timeStamp;
    }

    /* access modifiers changed from: package-private */
    public void updateChooserCounts(String packageName, String category, String action) {
        ArrayMap<String, Integer> chooserCounts;
        UsageStats usageStats = getOrCreateUsageStats(packageName);
        if (usageStats.mChooserCounts == null) {
            usageStats.mChooserCounts = new ArrayMap();
        }
        int idx = usageStats.mChooserCounts.indexOfKey(action);
        if (idx < 0) {
            chooserCounts = new ArrayMap<>();
            usageStats.mChooserCounts.put(action, chooserCounts);
        } else {
            chooserCounts = (ArrayMap) usageStats.mChooserCounts.valueAt(idx);
        }
        chooserCounts.put(category, Integer.valueOf(((Integer) chooserCounts.getOrDefault(category, 0)).intValue() + 1));
    }

    private boolean isLandscape(Configuration config) {
        return config != null && config.orientation == 2;
    }

    /* access modifiers changed from: package-private */
    public void updateConfigurationStats(Configuration config, long timeStamp) {
        if (this.activeConfiguration != null) {
            ConfigurationStats activeStats = this.configurations.get(this.activeConfiguration);
            if (activeStats != null) {
                activeStats.mTotalTimeActive += timeStamp - activeStats.mLastTimeActive;
                activeStats.mLastTimeActive = timeStamp - 1;
            }
        }
        if (!(this.packageInForeground == null || this.activeConfiguration == null || config == null || this.activeConfiguration.orientation == config.orientation)) {
            UsageStats usageStats = getOrCreateUsageStats(this.packageInForeground);
            if (usageStats.mLastEvent == 1 || usageStats.mLastEvent == 4) {
                if (!isLandscape(config)) {
                    usageStats.mLandTimeInForeground += timeStamp - usageStats.mLastLandTimeUsed;
                }
                usageStats.mLastLandTimeUsed = timeStamp;
            }
        }
        if (config != null) {
            ConfigurationStats configStats = getOrCreateConfigurationStats(config);
            configStats.mLastTimeActive = timeStamp;
            configStats.mActivationCount++;
            this.activeConfiguration = configStats.mConfiguration;
        }
        this.endTime = timeStamp;
    }

    /* access modifiers changed from: package-private */
    public void incrementAppLaunchCount(String packageName) {
        getOrCreateUsageStats(packageName).mAppLaunchCount++;
    }

    /* access modifiers changed from: package-private */
    public void commitTime(long timeStamp) {
        this.interactiveTracker.commitTime(timeStamp);
        this.nonInteractiveTracker.commitTime(timeStamp);
        this.keyguardShownTracker.commitTime(timeStamp);
        this.keyguardHiddenTracker.commitTime(timeStamp);
    }

    /* access modifiers changed from: package-private */
    public void updateScreenInteractive(long timeStamp) {
        this.interactiveTracker.update(timeStamp);
        this.nonInteractiveTracker.commitTime(timeStamp);
    }

    /* access modifiers changed from: package-private */
    public void updateScreenNonInteractive(long timeStamp) {
        this.nonInteractiveTracker.update(timeStamp);
        this.interactiveTracker.commitTime(timeStamp);
    }

    /* access modifiers changed from: package-private */
    public void updateKeyguardShown(long timeStamp) {
        this.keyguardShownTracker.update(timeStamp);
        this.keyguardHiddenTracker.commitTime(timeStamp);
    }

    /* access modifiers changed from: package-private */
    public void updateKeyguardHidden(long timeStamp) {
        this.keyguardHiddenTracker.update(timeStamp);
        this.keyguardShownTracker.commitTime(timeStamp);
    }

    /* access modifiers changed from: package-private */
    public void addEventStatsTo(List<EventStats> out) {
        List<EventStats> list = out;
        this.interactiveTracker.addToEventStats(list, 15, this.beginTime, this.endTime);
        List<EventStats> list2 = out;
        this.nonInteractiveTracker.addToEventStats(list2, 16, this.beginTime, this.endTime);
        this.keyguardShownTracker.addToEventStats(list, 17, this.beginTime, this.endTime);
        this.keyguardHiddenTracker.addToEventStats(list2, 18, this.beginTime, this.endTime);
    }

    private String getCachedStringRef(String str) {
        int index = this.mStringCache.indexOf(str);
        if (index >= 0) {
            return this.mStringCache.valueAt(index);
        }
        this.mStringCache.add(str);
        return str;
    }
}
