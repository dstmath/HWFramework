package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.TimeSparseArray;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.content.res.Configuration;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwPCUtils;

class IntervalStats {
    public Configuration activeConfiguration;
    public long beginTime;
    public final ArrayMap<Configuration, ConfigurationStats> configurations = new ArrayMap();
    public long endTime;
    public TimeSparseArray<Event> events;
    int intervalType;
    public long lastTimeSaved;
    private final ArraySet<String> mStringCache = new ArraySet();
    private String packageInForeground;
    public final ArrayMap<String, UsageStats> packageStats = new ArrayMap();

    IntervalStats() {
    }

    UsageStats getOrCreateUsageStats(String packageName) {
        UsageStats usageStats = (UsageStats) this.packageStats.get(packageName);
        if (usageStats != null) {
            return usageStats;
        }
        usageStats = new UsageStats();
        usageStats.mPackageName = getCachedStringRef(packageName);
        usageStats.mBeginTimeStamp = this.beginTime;
        usageStats.mEndTimeStamp = this.endTime;
        this.packageStats.put(usageStats.mPackageName, usageStats);
        return usageStats;
    }

    ConfigurationStats getOrCreateConfigurationStats(Configuration config) {
        ConfigurationStats configStats = (ConfigurationStats) this.configurations.get(config);
        if (configStats != null) {
            return configStats;
        }
        configStats = new ConfigurationStats();
        configStats.mBeginTimeStamp = this.beginTime;
        configStats.mEndTimeStamp = this.endTime;
        configStats.mConfiguration = config;
        this.configurations.put(config, configStats);
        return configStats;
    }

    Event buildEvent(String packageName, String className) {
        Event event = new Event();
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

    void update(String packageName, long timeStamp, int eventType) {
        update(packageName, timeStamp, eventType, 0);
    }

    void update(String packageName, long timeStamp, int eventType, int displayId) {
        UsageStats usageStats = getOrCreateUsageStats(packageName);
        if ((eventType == 2 || eventType == 3) && (usageStats.mLastEvent == 1 || usageStats.mLastEvent == 4)) {
            if (this.intervalType == 0) {
                Flog.i(1701, "usagestats update: pkg=" + packageName + " timeStamp=" + timeStamp + ",type=" + eventType + ",lastTotalTime=" + usageStats.mTotalTimeInForeground + ",lastTimeUsed=" + usageStats.mLastTimeUsed + ",deltaTime=" + (timeStamp - usageStats.mLastTimeUsed) + ",totalTime=" + ((usageStats.mTotalTimeInForeground + timeStamp) - usageStats.mLastTimeUsed));
            }
            usageStats.mTotalTimeInForeground += timeStamp - usageStats.mLastTimeUsed;
            if (isLandscape(this.activeConfiguration)) {
                usageStats.mLandTimeInForeground += timeStamp - usageStats.mLastLandTimeUsed;
            }
            if (displayId == HwPCUtils.getPCDisplayID()) {
                usageStats.mTimeInPCForeground += timeStamp - usageStats.mLastTimeUsedInPC;
            }
        }
        if (isStatefulEvent(eventType)) {
            usageStats.mLastEvent = eventType;
        }
        if (eventType != 6) {
            usageStats.mLastTimeUsed = timeStamp;
            this.packageInForeground = packageName;
            if (isLandscape(this.activeConfiguration)) {
                usageStats.mLastLandTimeUsed = timeStamp;
            }
            if (displayId == HwPCUtils.getPCDisplayID()) {
                usageStats.mLastTimeUsedInPC = timeStamp;
            }
        }
        usageStats.mEndTimeStamp = timeStamp;
        if (eventType == 1) {
            usageStats.mLaunchCount++;
        }
        this.endTime = timeStamp;
    }

    void updateChooserCounts(String packageName, String category, String action) {
        ArrayMap<String, Integer> chooserCounts;
        UsageStats usageStats = getOrCreateUsageStats(packageName);
        if (usageStats.mChooserCounts == null) {
            usageStats.mChooserCounts = new ArrayMap();
        }
        int idx = usageStats.mChooserCounts.indexOfKey(action);
        if (idx < 0) {
            chooserCounts = new ArrayMap();
            usageStats.mChooserCounts.put(action, chooserCounts);
        } else {
            chooserCounts = (ArrayMap) usageStats.mChooserCounts.valueAt(idx);
        }
        chooserCounts.put(category, Integer.valueOf(((Integer) chooserCounts.getOrDefault(category, Integer.valueOf(0))).intValue() + 1));
    }

    private boolean isLandscape(Configuration config) {
        return config != null && config.orientation == 2;
    }

    void updateConfigurationStats(Configuration config, long timeStamp) {
        if (this.activeConfiguration != null) {
            ConfigurationStats activeStats = (ConfigurationStats) this.configurations.get(this.activeConfiguration);
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

    private String getCachedStringRef(String str) {
        int index = this.mStringCache.indexOf(str);
        if (index >= 0) {
            return (String) this.mStringCache.valueAt(index);
        }
        this.mStringCache.add(str);
        return str;
    }
}
