package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.TimeSparseArray;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.content.res.Configuration;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;

class IntervalStats {
    public Configuration activeConfiguration;
    public long beginTime;
    public final ArrayMap<Configuration, ConfigurationStats> configurations;
    public long endTime;
    public TimeSparseArray<Event> events;
    public long lastTimeSaved;
    private final ArraySet<String> mStringCache;
    private String packageInForeground;
    public final ArrayMap<String, UsageStats> packageStats;

    IntervalStats() {
        this.packageStats = new ArrayMap();
        this.configurations = new ArrayMap();
        this.mStringCache = new ArraySet();
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
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
            case H.REPORT_LOSING_FOCUS /*3*/:
            case H.DO_TRAVERSAL /*4*/:
                return true;
            default:
                return false;
        }
    }

    void update(String packageName, long timeStamp, int eventType) {
        UsageStats usageStats = getOrCreateUsageStats(packageName);
        if ((eventType == 2 || eventType == 3) && (usageStats.mLastEvent == 1 || usageStats.mLastEvent == 4)) {
            usageStats.mTotalTimeInForeground += timeStamp - usageStats.mLastTimeUsed;
            if (isLandscape(this.activeConfiguration)) {
                usageStats.mLandTimeInForeground += timeStamp - usageStats.mLastLandTimeUsed;
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
        }
        usageStats.mEndTimeStamp = timeStamp;
        if (eventType == 1) {
            usageStats.mLaunchCount++;
        }
        this.endTime = timeStamp;
    }

    private boolean isLandscape(Configuration config) {
        return config != null && config.orientation == 2;
    }

    void updateConfigurationStats(Configuration config, long timeStamp) {
        if (this.activeConfiguration != null) {
            ConfigurationStats activeStats = (ConfigurationStats) this.configurations.get(this.activeConfiguration);
            activeStats.mTotalTimeActive += timeStamp - activeStats.mLastTimeActive;
            activeStats.mLastTimeActive = timeStamp - 1;
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
