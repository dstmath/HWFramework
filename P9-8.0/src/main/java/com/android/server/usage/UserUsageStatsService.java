package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.TimeSparseArray;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import com.android.server.usage.UsageStatsDatabase.CheckinAction;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

class UserUsageStatsService {
    private static final boolean DEBUG = false;
    private static final long[] INTERVAL_LENGTH = new long[]{UnixCalendar.DAY_IN_MILLIS, UnixCalendar.WEEK_IN_MILLIS, UnixCalendar.MONTH_IN_MILLIS, UnixCalendar.YEAR_IN_MILLIS};
    private static final String TAG = "UsageStatsService";
    private static final StatCombiner<ConfigurationStats> sConfigStatsCombiner = new StatCombiner<ConfigurationStats>() {
        public void combine(IntervalStats stats, boolean mutable, List<ConfigurationStats> accResult) {
            if (mutable) {
                int configCount = stats.configurations.size();
                for (int i = 0; i < configCount; i++) {
                    accResult.add(new ConfigurationStats((ConfigurationStats) stats.configurations.valueAt(i)));
                }
                return;
            }
            accResult.addAll(stats.configurations.values());
        }
    };
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int sDateFormatFlags = 131093;
    private static final StatCombiner<UsageStats> sUsageStatsCombiner = new StatCombiner<UsageStats>() {
        public void combine(IntervalStats stats, boolean mutable, List<UsageStats> accResult) {
            if (mutable) {
                int statCount = stats.packageStats.size();
                for (int i = 0; i < statCount; i++) {
                    accResult.add(new UsageStats((UsageStats) stats.packageStats.valueAt(i)));
                }
                return;
            }
            accResult.addAll(stats.packageStats.values());
        }
    };
    private final Context mContext;
    private final IntervalStats[] mCurrentStats;
    private final UnixCalendar mDailyExpiryDate;
    private final UsageStatsDatabase mDatabase;
    private final StatsUpdatedListener mListener;
    private final String mLogPrefix;
    private boolean mStatsChanged = false;
    private int mTimeZoneOffset = TimeZone.getDefault().getRawOffset();
    private final int mUserId;

    interface StatsUpdatedListener {
        void onNewUpdate(int i);

        void onStatsReloaded();

        void onStatsUpdated();
    }

    UserUsageStatsService(Context context, int userId, File usageStatsDir, StatsUpdatedListener listener) {
        this.mContext = context;
        this.mDailyExpiryDate = new UnixCalendar(0);
        this.mDatabase = new UsageStatsDatabase(usageStatsDir);
        this.mCurrentStats = new IntervalStats[4];
        this.mListener = listener;
        this.mLogPrefix = "User[" + Integer.toString(userId) + "] ";
        this.mUserId = userId;
    }

    void init(long currentTimeMillis) {
        int i;
        int i2 = 0;
        this.mDatabase.init(currentTimeMillis);
        int nullCount = 0;
        for (i = 0; i < this.mCurrentStats.length; i++) {
            this.mCurrentStats[i] = this.mDatabase.getLatestUsageStats(i);
            if (this.mCurrentStats[i] == null) {
                nullCount++;
            } else {
                this.mCurrentStats[i].intervalType = i;
            }
        }
        if (nullCount > 0) {
            if (nullCount != this.mCurrentStats.length) {
                Slog.w(TAG, this.mLogPrefix + "Some stats have no latest available");
            }
            loadActiveStats(currentTimeMillis, false);
        } else {
            updateRolloverDeadline(this.mCurrentStats[0].endTime);
        }
        IntervalStats[] intervalStatsArr = this.mCurrentStats;
        int length = intervalStatsArr.length;
        while (i2 < length) {
            IntervalStats stat = intervalStatsArr[i2];
            int pkgCount = stat.packageStats.size();
            for (i = 0; i < pkgCount; i++) {
                UsageStats pkgStats = (UsageStats) stat.packageStats.valueAt(i);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
                    Flog.i(1701, "init report ev3: pkg=" + pkgStats.mPackageName + " timeStamp=" + stat.lastTimeSaved + ", lastEvent:" + pkgStats.mLastEvent);
                    stat.update(pkgStats.mPackageName, stat.lastTimeSaved, 3);
                    notifyStatsChanged();
                }
            }
            stat.updateConfigurationStats(null, stat.lastTimeSaved);
            i2++;
        }
        if (this.mDatabase.isNewUpdate()) {
            notifyNewUpdate();
        }
    }

    void onTimeChanged(long oldTime, long newTime) {
        persistActiveStats();
        this.mDatabase.onTimeChanged(newTime - oldTime);
        loadActiveStats(newTime, true);
    }

    void reportEvent(Event event) {
        if (event.mEventType == 6 && this.mTimeZoneOffset != TimeZone.getDefault().getRawOffset()) {
            Slog.d(TAG, "TimeZone has changed, updateRolloverDeadline!");
            this.mTimeZoneOffset = TimeZone.getDefault().getRawOffset();
            updateRolloverDeadline(event.mTimeStamp);
        }
        if (event.mTimeStamp >= this.mDailyExpiryDate.getTimeInMillis()) {
            rolloverStats(event.mTimeStamp);
        }
        IntervalStats currentDailyStats = this.mCurrentStats[0];
        Configuration newFullConfig = event.mConfiguration;
        if (event.mEventType == 5 && currentDailyStats.activeConfiguration != null) {
            event.mConfiguration = Configuration.generateDelta(currentDailyStats.activeConfiguration, newFullConfig);
        }
        if (currentDailyStats.events == null) {
            currentDailyStats.events = new TimeSparseArray();
        }
        if (event.mEventType != 6) {
            currentDailyStats.events.put(event.mTimeStamp, event);
        }
        for (IntervalStats stats : this.mCurrentStats) {
            if (event.mEventType == 5) {
                stats.updateConfigurationStats(newFullConfig, event.mTimeStamp);
            } else if (event.mEventType == 9) {
                stats.updateChooserCounts(event.mPackage, event.mContentType, event.mAction);
                String[] annotations = event.mContentAnnotations;
                if (annotations != null) {
                    for (String annotation : annotations) {
                        stats.updateChooserCounts(event.mPackage, annotation, event.mAction);
                    }
                }
            } else {
                stats.update(event.mPackage, event.mTimeStamp, event.mEventType, event.mDisplayId);
            }
        }
        notifyStatsChanged();
    }

    private <T> List<T> queryStats(int intervalType, long beginTime, long endTime, StatCombiner<T> combiner) {
        if (intervalType == 4) {
            intervalType = this.mDatabase.findBestFitBucket(beginTime, endTime);
            if (intervalType < 0) {
                intervalType = 0;
            }
        }
        if (intervalType < 0 || intervalType >= this.mCurrentStats.length) {
            return null;
        }
        IntervalStats currentStats = this.mCurrentStats[intervalType];
        if (beginTime >= currentStats.endTime) {
            return null;
        }
        List<T> results = this.mDatabase.queryUsageStats(intervalType, beginTime, Math.min(currentStats.beginTime, endTime), combiner);
        if (beginTime < currentStats.endTime && endTime > currentStats.beginTime) {
            if (results == null) {
                results = new ArrayList();
            }
            combiner.combine(currentStats, true, results);
        }
        return results;
    }

    List<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sUsageStatsCombiner);
    }

    List<ConfigurationStats> queryConfigurationStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sConfigStatsCombiner);
    }

    UsageEvents queryEvents(long beginTime, long endTime, boolean obfuscateInstantApps) {
        final ArraySet<String> names = new ArraySet();
        final long j = beginTime;
        final long j2 = endTime;
        final boolean z = obfuscateInstantApps;
        List<Event> results = queryStats(0, beginTime, endTime, new StatCombiner<Event>() {
            public void combine(IntervalStats stats, boolean mutable, List<Event> accumulatedResult) {
                if (stats.events != null) {
                    int startIndex = stats.events.closestIndexOnOrAfter(j);
                    if (startIndex >= 0) {
                        int size = stats.events.size();
                        int i = startIndex;
                        while (i < size && stats.events.keyAt(i) < j2) {
                            Event event = (Event) stats.events.valueAt(i);
                            if (z) {
                                event = event.getObfuscatedIfInstantApp();
                            }
                            names.add(event.mPackage);
                            if (event.mClass != null) {
                                names.add(event.mClass);
                            }
                            accumulatedResult.add(event);
                            i++;
                        }
                    }
                }
            }
        });
        if (results == null || results.isEmpty()) {
            return null;
        }
        String[] table = (String[]) names.toArray(new String[names.size()]);
        Arrays.sort(table);
        return new UsageEvents(results, table);
    }

    void persistActiveStats() {
        if (this.mStatsChanged) {
            Slog.i(TAG, this.mLogPrefix + "Flushing usage stats to disk");
            int i = 0;
            while (i < this.mCurrentStats.length) {
                try {
                    this.mDatabase.putUsageStats(i, this.mCurrentStats[i]);
                    i++;
                } catch (IOException e) {
                    Slog.e(TAG, this.mLogPrefix + "Failed to persist active stats", e);
                    return;
                }
            }
            this.mStatsChanged = false;
        }
    }

    private void rolloverStats(long currentTimeMillis) {
        int i;
        long startTime = SystemClock.elapsedRealtime();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats");
        Configuration previousConfig = this.mCurrentStats[0].activeConfiguration;
        ArraySet<String> continuePreviousDay = new ArraySet();
        for (IntervalStats stat : this.mCurrentStats) {
            int pkgCount = stat.packageStats.size();
            for (i = 0; i < pkgCount; i++) {
                UsageStats pkgStats = (UsageStats) stat.packageStats.valueAt(i);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
                    continuePreviousDay.add(pkgStats.mPackageName);
                    Flog.i(1701, "rollover report ev3: pkg=" + pkgStats.mPackageName + " timeStamp=" + (this.mDailyExpiryDate.getTimeInMillis() - 1));
                    stat.update(pkgStats.mPackageName, this.mDailyExpiryDate.getTimeInMillis() - 1, 3);
                    notifyStatsChanged();
                }
            }
            stat.updateConfigurationStats(null, this.mDailyExpiryDate.getTimeInMillis() - 1);
        }
        persistActiveStats();
        this.mDatabase.prune(currentTimeMillis);
        loadActiveStats(currentTimeMillis, false);
        int continueCount = continuePreviousDay.size();
        for (i = 0; i < continueCount; i++) {
            String name = (String) continuePreviousDay.valueAt(i);
            long beginTime = this.mCurrentStats[0].beginTime;
            for (IntervalStats stat2 : this.mCurrentStats) {
                Flog.i(1701, "rollover report ev4: pkg=" + name + " timeStamp=" + beginTime);
                stat2.update(name, beginTime, 4);
                stat2.updateConfigurationStats(previousConfig, beginTime);
                notifyStatsChanged();
            }
        }
        persistActiveStats();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats complete. Took " + (SystemClock.elapsedRealtime() - startTime) + " milliseconds");
    }

    private void notifyStatsChanged() {
        if (!this.mStatsChanged) {
            this.mStatsChanged = true;
            this.mListener.onStatsUpdated();
        }
    }

    private void notifyNewUpdate() {
        this.mListener.onNewUpdate(this.mUserId);
    }

    private void loadActiveStats(long currentTimeMillis, boolean force) {
        UnixCalendar tempCal = new UnixCalendar(0);
        UnixCalendar lastExpiryDate = new UnixCalendar(0);
        int intervalType = 0;
        while (intervalType < this.mCurrentStats.length) {
            tempCal.setTimeInMillis(currentTimeMillis);
            UnixCalendar.truncateTo(tempCal, intervalType);
            if (force || this.mCurrentStats[intervalType] == null || this.mCurrentStats[intervalType].beginTime != tempCal.getTimeInMillis()) {
                IntervalStats stats = this.mDatabase.getLatestUsageStats(intervalType);
                this.mCurrentStats[intervalType] = null;
                if (stats != null) {
                    lastExpiryDate.setTimeInMillis(stats.endTime);
                    UnixCalendar.truncateTo(lastExpiryDate, intervalType);
                    Slog.d(TAG, "check currentTime(" + currentTimeMillis + ") is between endTime(" + stats.endTime + ") and expireTime(" + (lastExpiryDate.getTimeInMillis() + INTERVAL_LENGTH[intervalType]) + ") for interval " + intervalType);
                    if (currentTimeMillis > stats.endTime && currentTimeMillis < lastExpiryDate.getTimeInMillis() + INTERVAL_LENGTH[intervalType]) {
                        Slog.d(TAG, this.mLogPrefix + "Loading existing stats " + stats.beginTime + " for interval " + intervalType);
                        this.mCurrentStats[intervalType] = stats;
                    }
                }
                if (this.mCurrentStats[intervalType] == null) {
                    this.mCurrentStats[intervalType] = new IntervalStats();
                    this.mCurrentStats[intervalType].beginTime = tempCal.getTimeInMillis();
                    this.mCurrentStats[intervalType].endTime = currentTimeMillis;
                    this.mCurrentStats[intervalType].intervalType = intervalType;
                }
            }
            intervalType++;
        }
        this.mStatsChanged = false;
        updateRolloverDeadline(currentTimeMillis);
        this.mListener.onStatsReloaded();
    }

    private void updateRolloverDeadline(long currentTimeMillis) {
        this.mDailyExpiryDate.setTimeInMillis(currentTimeMillis);
        this.mDailyExpiryDate.addDays(1);
        this.mDailyExpiryDate.truncateToDay();
        Slog.i(TAG, this.mLogPrefix + "Rollover scheduled @ " + sDateFormat.format(Long.valueOf(this.mDailyExpiryDate.getTimeInMillis())) + "(" + this.mDailyExpiryDate.getTimeInMillis() + ")");
    }

    void checkin(final IndentingPrintWriter pw) {
        this.mDatabase.checkinDailyFiles(new CheckinAction() {
            public boolean checkin(IntervalStats stats) {
                UserUsageStatsService.this.printIntervalStats(pw, stats, false);
                return true;
            }
        });
    }

    void dump(IndentingPrintWriter pw) {
        for (int interval = 0; interval < this.mCurrentStats.length; interval++) {
            pw.print("In-memory ");
            pw.print(intervalToString(interval));
            pw.println(" stats");
            printIntervalStats(pw, this.mCurrentStats[interval], true);
        }
    }

    private String formatDateTime(long dateTime, boolean pretty) {
        if (pretty) {
            return "\"" + DateUtils.formatDateTime(this.mContext, dateTime, sDateFormatFlags) + "\"";
        }
        return Long.toString(dateTime);
    }

    private String formatElapsedTime(long elapsedTime, boolean pretty) {
        if (pretty) {
            return "\"" + DateUtils.formatElapsedTime(elapsedTime / 1000) + "\"";
        }
        return Long.toString(elapsedTime);
    }

    void printIntervalStats(IndentingPrintWriter pw, IntervalStats stats, boolean prettyDates) {
        int i;
        UsageStats usageStats;
        if (prettyDates) {
            pw.printPair("timeRange", "\"" + DateUtils.formatDateRange(this.mContext, stats.beginTime, stats.endTime, sDateFormatFlags) + "\"");
        } else {
            pw.printPair("beginTime", Long.valueOf(stats.beginTime));
            pw.printPair("endTime", Long.valueOf(stats.endTime));
        }
        pw.println();
        pw.increaseIndent();
        pw.println("packages");
        pw.increaseIndent();
        ArrayMap<String, UsageStats> pkgStats = stats.packageStats;
        int pkgCount = pkgStats.size();
        for (i = 0; i < pkgCount; i++) {
            usageStats = (UsageStats) pkgStats.valueAt(i);
            pw.printPair(HwBroadcastRadarUtil.KEY_PACKAGE, usageStats.mPackageName);
            pw.printPair("totalTime", formatElapsedTime(usageStats.mTotalTimeInForeground, prettyDates));
            pw.printPair("lastTime", formatDateTime(usageStats.mLastTimeUsed, prettyDates));
            pw.println();
        }
        pw.decreaseIndent();
        pw.println();
        pw.println("ChooserCounts");
        pw.increaseIndent();
        for (UsageStats usageStats2 : pkgStats.values()) {
            pw.printPair(HwBroadcastRadarUtil.KEY_PACKAGE, usageStats2.mPackageName);
            if (usageStats2.mChooserCounts != null) {
                int chooserCountSize = usageStats2.mChooserCounts.size();
                for (i = 0; i < chooserCountSize; i++) {
                    String action = (String) usageStats2.mChooserCounts.keyAt(i);
                    ArrayMap<String, Integer> counts = (ArrayMap) usageStats2.mChooserCounts.valueAt(i);
                    int annotationSize = counts.size();
                    for (int j = 0; j < annotationSize; j++) {
                        String key = (String) counts.keyAt(j);
                        int count = ((Integer) counts.valueAt(j)).intValue();
                        if (count != 0) {
                            pw.printPair("ChooserCounts", action + ":" + key + " is " + Integer.toString(count));
                            pw.println();
                        }
                    }
                }
            }
            pw.println();
        }
        pw.decreaseIndent();
        pw.println("configurations");
        pw.increaseIndent();
        ArrayMap<Configuration, ConfigurationStats> configStats = stats.configurations;
        int configCount = configStats.size();
        for (i = 0; i < configCount; i++) {
            ConfigurationStats config = (ConfigurationStats) configStats.valueAt(i);
            pw.printPair("config", Configuration.resourceQualifierString(config.mConfiguration));
            pw.printPair("totalTime", formatElapsedTime(config.mTotalTimeActive, prettyDates));
            pw.printPair("lastTime", formatDateTime(config.mLastTimeActive, prettyDates));
            pw.printPair("count", Integer.valueOf(config.mActivationCount));
            pw.println();
        }
        pw.decreaseIndent();
        pw.println("events");
        pw.increaseIndent();
        TimeSparseArray<Event> events = stats.events;
        int eventCount = events != null ? events.size() : 0;
        for (i = 0; i < eventCount; i++) {
            Event event = (Event) events.valueAt(i);
            pw.printPair("time", formatDateTime(event.mTimeStamp, prettyDates));
            pw.printPair(SoundModelContract.KEY_TYPE, eventToString(event.mEventType));
            pw.printPair(HwBroadcastRadarUtil.KEY_PACKAGE, event.mPackage);
            if (event.mClass != null) {
                pw.printPair(AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS, event.mClass);
            }
            if (event.mConfiguration != null) {
                pw.printPair("config", Configuration.resourceQualifierString(event.mConfiguration));
            }
            if (event.mShortcutId != null) {
                pw.printPair("shortcutId", event.mShortcutId);
            }
            pw.printHexPair("flags", event.mFlags);
            pw.println();
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    private static String intervalToString(int interval) {
        switch (interval) {
            case 0:
                return "daily";
            case 1:
                return "weekly";
            case 2:
                return "monthly";
            case 3:
                return "yearly";
            default:
                return "?";
        }
    }

    private static String eventToString(int eventType) {
        switch (eventType) {
            case 0:
                return "NONE";
            case 1:
                return "MOVE_TO_FOREGROUND";
            case 2:
                return "MOVE_TO_BACKGROUND";
            case 3:
                return "END_OF_DAY";
            case 4:
                return "CONTINUE_PREVIOUS_DAY";
            case 5:
                return "CONFIGURATION_CHANGE";
            case 6:
                return "SYSTEM_INTERACTION";
            case 7:
                return "USER_INTERACTION";
            case 8:
                return "SHORTCUT_INVOCATION";
            case 9:
                return "CHOOSER_ACTION";
            default:
                return "UNKNOWN";
        }
    }

    byte[] getBackupPayload(String key) {
        return this.mDatabase.getBackupPayload(key);
    }

    void applyRestoredPayload(String key, byte[] payload) {
        this.mDatabase.applyRestoredPayload(key, payload);
    }
}
