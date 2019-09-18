package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.EventList;
import android.app.usage.EventStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.am.AssistDataRequester;
import com.android.server.audio.AudioService;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.usage.IntervalStats;
import com.android.server.usage.UsageStatsDatabase;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

class UserUsageStatsService {
    private static final boolean DEBUG = false;
    private static final long[] INTERVAL_LENGTH = {86400000, UnixCalendar.WEEK_IN_MILLIS, UnixCalendar.MONTH_IN_MILLIS, UnixCalendar.YEAR_IN_MILLIS};
    private static final String TAG = "UsageStatsService";
    private static final UsageStatsDatabase.StatCombiner<ConfigurationStats> sConfigStatsCombiner = new UsageStatsDatabase.StatCombiner<ConfigurationStats>() {
        public void combine(IntervalStats stats, boolean mutable, List<ConfigurationStats> accResult) {
            if (!mutable) {
                accResult.addAll(stats.configurations.values());
                return;
            }
            int configCount = stats.configurations.size();
            for (int i = 0; i < configCount; i++) {
                accResult.add(new ConfigurationStats(stats.configurations.valueAt(i)));
            }
        }
    };
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int sDateFormatFlags = 131093;
    private static final UsageStatsDatabase.StatCombiner<EventStats> sEventStatsCombiner = new UsageStatsDatabase.StatCombiner<EventStats>() {
        public void combine(IntervalStats stats, boolean mutable, List<EventStats> accResult) {
            stats.addEventStatsTo(accResult);
        }
    };
    private static final UsageStatsDatabase.StatCombiner<UsageStats> sUsageStatsCombiner = new UsageStatsDatabase.StatCombiner<UsageStats>() {
        public void combine(IntervalStats stats, boolean mutable, List<UsageStats> accResult) {
            if (!mutable) {
                accResult.addAll(stats.packageStats.values());
                return;
            }
            int statCount = stats.packageStats.size();
            for (int i = 0; i < statCount; i++) {
                accResult.add(new UsageStats(stats.packageStats.valueAt(i)));
            }
        }
    };
    private final Context mContext;
    private final IntervalStats[] mCurrentStats;
    private final UnixCalendar mDailyExpiryDate;
    private final UsageStatsDatabase mDatabase;
    private String mLastBackgroundedPackage;
    public int mLastEvent;
    public String mLastForegroundedPackage;
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

    /* access modifiers changed from: package-private */
    public void init(long currentTimeMillis) {
        this.mDatabase.init(currentTimeMillis);
        int nullCount = 0;
        for (int i = 0; i < this.mCurrentStats.length; i++) {
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
        for (IntervalStats stat : this.mCurrentStats) {
            int pkgCount = stat.packageStats.size();
            for (int i2 = 0; i2 < pkgCount; i2++) {
                UsageStats pkgStats = stat.packageStats.valueAt(i2);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
                    Flog.i(1701, "init report ev3: pkg=" + pkgStats.mPackageName + " timeStamp=" + stat.lastTimeSaved + ", lastEvent:" + pkgStats.mLastEvent);
                    stat.update(pkgStats.mPackageName, stat.lastTimeSaved, 3);
                    notifyStatsChanged();
                }
            }
            stat.updateConfigurationStats(null, stat.lastTimeSaved);
        }
        if (this.mDatabase.isNewUpdate()) {
            notifyNewUpdate();
        }
    }

    /* access modifiers changed from: package-private */
    public void onTimeChanged(long oldTime, long newTime) {
        persistActiveStats();
        this.mDatabase.onTimeChanged(newTime - oldTime);
        loadActiveStats(newTime, true);
    }

    /* access modifiers changed from: package-private */
    public void reportEvent(UsageEvents.Event event) {
        UsageEvents.Event event2 = event;
        if (event2.mEventType == 6 && this.mTimeZoneOffset != TimeZone.getDefault().getRawOffset()) {
            Slog.d(TAG, "TimeZone has changed, updateRolloverDeadline!");
            this.mTimeZoneOffset = TimeZone.getDefault().getRawOffset();
            updateRolloverDeadline(event2.mTimeStamp);
        }
        if (event2.mTimeStamp >= this.mDailyExpiryDate.getTimeInMillis()) {
            rolloverStats(event2.mTimeStamp);
        }
        IntervalStats currentDailyStats = this.mCurrentStats[0];
        Configuration newFullConfig = event2.mConfiguration;
        if (event2.mEventType == 5 && currentDailyStats.activeConfiguration != null) {
            event2.mConfiguration = Configuration.generateDelta(currentDailyStats.activeConfiguration, newFullConfig);
        }
        if (currentDailyStats.events == null) {
            currentDailyStats.events = new EventList();
        }
        if (event2.mEventType != 6) {
            currentDailyStats.events.insert(event2);
        }
        boolean incrementAppLaunch = false;
        if (event2.mEventType == 1) {
            if (event2.mPackage != null && !event2.mPackage.equals(this.mLastBackgroundedPackage)) {
                incrementAppLaunch = true;
            }
            if (event2.mPackage != null) {
                this.mLastForegroundedPackage = event2.mPackage;
            }
            this.mLastEvent = event2.mEventType;
        } else if (event2.mEventType == 2) {
            if (event2.mPackage != null) {
                this.mLastBackgroundedPackage = event2.mPackage;
            }
            this.mLastEvent = event2.mEventType;
        }
        for (IntervalStats stats : this.mCurrentStats) {
            int i = event2.mEventType;
            if (i == 5) {
                stats.updateConfigurationStats(newFullConfig, event2.mTimeStamp);
            } else if (i != 9) {
                switch (i) {
                    case 15:
                        stats.updateScreenInteractive(event2.mTimeStamp);
                        break;
                    case 16:
                        stats.updateScreenNonInteractive(event2.mTimeStamp);
                        break;
                    case 17:
                        stats.updateKeyguardShown(event2.mTimeStamp);
                        break;
                    case 18:
                        stats.updateKeyguardHidden(event2.mTimeStamp);
                        break;
                    default:
                        IntervalStats stats2 = stats;
                        stats.update(event2.mPackage, event2.mTimeStamp, event2.mEventType, event2.mDisplayId);
                        if (!incrementAppLaunch) {
                            break;
                        } else {
                            stats2.incrementAppLaunchCount(event2.mPackage);
                            break;
                        }
                }
            } else {
                IntervalStats stats3 = stats;
                stats3.updateChooserCounts(event2.mPackage, event2.mContentType, event2.mAction);
                String[] annotations = event2.mContentAnnotations;
                if (annotations != null) {
                    for (String annotation : annotations) {
                        stats3.updateChooserCounts(event2.mPackage, annotation, event2.mAction);
                    }
                }
            }
        }
        notifyStatsChanged();
    }

    private <T> List<T> queryStats(int intervalType, long beginTime, long endTime, UsageStatsDatabase.StatCombiner<T> combiner) {
        int intervalType2;
        long j = beginTime;
        long j2 = endTime;
        int i = intervalType;
        if (i == 4) {
            int intervalType3 = this.mDatabase.findBestFitBucket(j, j2);
            if (intervalType3 < 0) {
                intervalType3 = 0;
            }
            intervalType2 = intervalType3;
        } else {
            intervalType2 = i;
        }
        if (intervalType2 < 0 || intervalType2 >= this.mCurrentStats.length) {
            UsageStatsDatabase.StatCombiner<T> statCombiner = combiner;
            return null;
        }
        IntervalStats currentStats = this.mCurrentStats[intervalType2];
        if (j >= currentStats.endTime) {
            return null;
        }
        List<T> results = this.mDatabase.queryUsageStats(intervalType2, j, Math.min(currentStats.beginTime, j2), combiner);
        if (j >= currentStats.endTime || j2 <= currentStats.beginTime) {
            UsageStatsDatabase.StatCombiner<T> statCombiner2 = combiner;
        } else {
            if (results == null) {
                results = new ArrayList<>();
            }
            combiner.combine(currentStats, true, results);
        }
        return results;
    }

    /* access modifiers changed from: package-private */
    public List<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sUsageStatsCombiner);
    }

    /* access modifiers changed from: package-private */
    public List<ConfigurationStats> queryConfigurationStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sConfigStatsCombiner);
    }

    /* access modifiers changed from: package-private */
    public List<EventStats> queryEventStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sEventStatsCombiner);
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEvents(long beginTime, long endTime, boolean obfuscateInstantApps) {
        ArraySet<String> names = new ArraySet<>();
        final long j = beginTime;
        final long j2 = endTime;
        final boolean z = obfuscateInstantApps;
        final ArraySet<String> arraySet = names;
        AnonymousClass4 r1 = new UsageStatsDatabase.StatCombiner<UsageEvents.Event>() {
            public void combine(IntervalStats stats, boolean mutable, List<UsageEvents.Event> accumulatedResult) {
                if (stats.events != null) {
                    int startIndex = stats.events.firstIndexOnOrAfter(j);
                    int size = stats.events.size();
                    int i = startIndex;
                    while (i < size && stats.events.get(i).mTimeStamp < j2) {
                        UsageEvents.Event event = stats.events.get(i);
                        if (z) {
                            event = event.getObfuscatedIfInstantApp();
                        }
                        arraySet.add(event.mPackage);
                        if (event.mClass != null) {
                            arraySet.add(event.mClass);
                        }
                        accumulatedResult.add(event);
                        i++;
                    }
                }
            }
        };
        List<UsageEvents.Event> results = queryStats(0, j, j2, r1);
        if (results == null || results.isEmpty()) {
            return null;
        }
        String[] table = (String[]) names.toArray(new String[names.size()]);
        Arrays.sort(table);
        return new UsageEvents(results, table);
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEventsForPackage(long beginTime, long endTime, String packageName) {
        ArraySet<String> names = new ArraySet<>();
        names.add(packageName);
        $$Lambda$UserUsageStatsService$aWxPyFEggMepoyju6mPXDEUesw r1 = new UsageStatsDatabase.StatCombiner(beginTime, endTime, packageName, names) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ ArraySet f$3;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r5;
                this.f$3 = r6;
            }

            public final void combine(IntervalStats intervalStats, boolean z, List list) {
                UserUsageStatsService.lambda$queryEventsForPackage$0(this.f$0, this.f$1, this.f$2, this.f$3, intervalStats, z, list);
            }
        };
        List<UsageEvents.Event> results = queryStats(0, beginTime, endTime, r1);
        if (results == null || results.isEmpty()) {
            return null;
        }
        String[] table = (String[]) names.toArray(new String[names.size()]);
        Arrays.sort(table);
        return new UsageEvents(results, table);
    }

    static /* synthetic */ void lambda$queryEventsForPackage$0(long beginTime, long endTime, String packageName, ArraySet names, IntervalStats stats, boolean mutable, List accumulatedResult) {
        if (stats.events != null) {
            int startIndex = stats.events.firstIndexOnOrAfter(beginTime);
            int size = stats.events.size();
            int i = startIndex;
            while (i < size && stats.events.get(i).mTimeStamp < endTime) {
                UsageEvents.Event event = stats.events.get(i);
                if (packageName.equals(event.mPackage)) {
                    if (event.mClass != null) {
                        names.add(event.mClass);
                    }
                    accumulatedResult.add(event);
                }
                i++;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void persistActiveStats() {
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
        IntervalStats[] intervalStatsArr;
        long j = currentTimeMillis;
        long startTime = SystemClock.elapsedRealtime();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats");
        int i2 = 0;
        Configuration previousConfig = this.mCurrentStats[0].activeConfiguration;
        ArraySet<String> continuePreviousDay = new ArraySet<>();
        IntervalStats[] intervalStatsArr2 = this.mCurrentStats;
        int length = intervalStatsArr2.length;
        int i3 = 0;
        while (i3 < length) {
            IntervalStats stat = intervalStatsArr2[i3];
            int pkgCount = stat.packageStats.size();
            int i4 = i2;
            while (i4 < pkgCount) {
                UsageStats pkgStats = stat.packageStats.valueAt(i4);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
                    continuePreviousDay.add(pkgStats.mPackageName);
                    StringBuilder sb = new StringBuilder();
                    sb.append("rollover report ev3: pkg=");
                    sb.append(pkgStats.mPackageName);
                    sb.append(" timeStamp=");
                    intervalStatsArr = intervalStatsArr2;
                    i = length;
                    sb.append(this.mDailyExpiryDate.getTimeInMillis() - 1);
                    Flog.i(1701, sb.toString());
                    stat.update(pkgStats.mPackageName, this.mDailyExpiryDate.getTimeInMillis() - 1, 3);
                    notifyStatsChanged();
                } else {
                    intervalStatsArr = intervalStatsArr2;
                    i = length;
                }
                i4++;
                intervalStatsArr2 = intervalStatsArr;
                length = i;
            }
            stat.updateConfigurationStats(null, this.mDailyExpiryDate.getTimeInMillis() - 1);
            stat.commitTime(this.mDailyExpiryDate.getTimeInMillis() - 1);
            i3++;
            intervalStatsArr2 = intervalStatsArr2;
            length = length;
            i2 = 0;
        }
        persistActiveStats();
        this.mDatabase.prune(j);
        loadActiveStats(j, false);
        int continueCount = continuePreviousDay.size();
        int i5 = 0;
        while (i5 < continueCount) {
            String name = continuePreviousDay.valueAt(i5);
            long beginTime = this.mCurrentStats[0].beginTime;
            IntervalStats[] intervalStatsArr3 = this.mCurrentStats;
            int length2 = intervalStatsArr3.length;
            int i6 = 0;
            while (i6 < length2) {
                IntervalStats stat2 = intervalStatsArr3[i6];
                Flog.i(1701, "rollover report ev4: pkg=" + name + " timeStamp=" + beginTime);
                stat2.update(name, beginTime, 4);
                stat2.updateConfigurationStats(previousConfig, beginTime);
                notifyStatsChanged();
                i6++;
                long j2 = currentTimeMillis;
            }
            i5++;
            long j3 = currentTimeMillis;
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
        for (int intervalType = 0; intervalType < this.mCurrentStats.length; intervalType++) {
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
                    Slog.d(TAG, this.mLogPrefix + "Creating new stats " + tempCal.getTimeInMillis() + " for interval " + intervalType);
                    this.mCurrentStats[intervalType] = new IntervalStats();
                    this.mCurrentStats[intervalType].beginTime = tempCal.getTimeInMillis();
                    this.mCurrentStats[intervalType].endTime = currentTimeMillis;
                }
                this.mCurrentStats[intervalType].intervalType = intervalType;
            }
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

    /* access modifiers changed from: package-private */
    public void checkin(final IndentingPrintWriter pw) {
        this.mDatabase.checkinDailyFiles(new UsageStatsDatabase.CheckinAction() {
            public boolean checkin(IntervalStats stats) {
                UserUsageStatsService.this.printIntervalStats(pw, stats, false, false, null);
                return true;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void dump(IndentingPrintWriter pw, String pkg) {
        dump(pw, pkg, false);
    }

    /* access modifiers changed from: package-private */
    public void dump(IndentingPrintWriter pw, String pkg, boolean compact) {
        printLast24HrEvents(pw, !compact, pkg);
        for (int interval = 0; interval < this.mCurrentStats.length; interval++) {
            pw.print("In-memory ");
            pw.print(intervalToString(interval));
            pw.println(" stats");
            printIntervalStats(pw, this.mCurrentStats[interval], !compact, true, pkg);
        }
    }

    private String formatDateTime(long dateTime, boolean pretty) {
        if (!pretty) {
            return Long.toString(dateTime);
        }
        return "\"" + sDateFormat.format(Long.valueOf(dateTime)) + "\"";
    }

    private String formatElapsedTime(long elapsedTime, boolean pretty) {
        if (!pretty) {
            return Long.toString(elapsedTime);
        }
        return "\"" + DateUtils.formatElapsedTime(elapsedTime / 1000) + "\"";
    }

    /* access modifiers changed from: package-private */
    public void printEvent(IndentingPrintWriter pw, UsageEvents.Event event, boolean prettyDates) {
        pw.printPair("time", formatDateTime(event.mTimeStamp, prettyDates));
        pw.printPair(DatabaseHelper.SoundModelContract.KEY_TYPE, eventToString(event.mEventType));
        pw.printPair("package", event.mPackage);
        if (event.mClass != null) {
            pw.printPair(AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS, event.mClass);
        }
        if (event.mConfiguration != null) {
            pw.printPair("config", Configuration.resourceQualifierString(event.mConfiguration));
        }
        if (event.mShortcutId != null) {
            pw.printPair("shortcutId", event.mShortcutId);
        }
        if (event.mEventType == 11) {
            pw.printPair("standbyBucket", Integer.valueOf(event.getStandbyBucket()));
            pw.printPair(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, UsageStatsManager.reasonToString(event.getStandbyReason()));
        }
        pw.printHexPair("flags", event.mFlags);
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public void printLast24HrEvents(IndentingPrintWriter pw, boolean prettyDates, String pkg) {
        IndentingPrintWriter indentingPrintWriter = pw;
        boolean z = prettyDates;
        long endTime = System.currentTimeMillis();
        UnixCalendar yesterday = new UnixCalendar(endTime);
        yesterday.addDays(-1);
        long beginTime = yesterday.getTimeInMillis();
        final long j = beginTime;
        final long j2 = endTime;
        final String str = pkg;
        AnonymousClass6 r0 = new UsageStatsDatabase.StatCombiner<UsageEvents.Event>() {
            public void combine(IntervalStats stats, boolean mutable, List<UsageEvents.Event> accumulatedResult) {
                if (stats.events != null) {
                    int startIndex = stats.events.firstIndexOnOrAfter(j);
                    int size = stats.events.size();
                    int i = startIndex;
                    while (i < size && stats.events.get(i).mTimeStamp < j2) {
                        UsageEvents.Event event = stats.events.get(i);
                        if (str == null || str.equals(event.mPackage)) {
                            accumulatedResult.add(event);
                        }
                        i++;
                    }
                }
            }
        };
        List<UsageEvents.Event> events = queryStats(0, j, j2, r0);
        indentingPrintWriter.print("Last 24 hour events (");
        if (z) {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            UnixCalendar unixCalendar = yesterday;
            StringBuilder sb2 = sb;
            sb2.append(DateUtils.formatDateRange(this.mContext, beginTime, endTime, sDateFormatFlags));
            sb2.append("\"");
            indentingPrintWriter.printPair("timeRange", sb2.toString());
        } else {
            indentingPrintWriter.printPair("beginTime", Long.valueOf(beginTime));
            indentingPrintWriter.printPair("endTime", Long.valueOf(endTime));
        }
        indentingPrintWriter.println(")");
        if (events != null) {
            pw.increaseIndent();
            for (UsageEvents.Event event : events) {
                printEvent(indentingPrintWriter, event, z);
            }
            pw.decreaseIndent();
        }
    }

    /* access modifiers changed from: package-private */
    public void printEventAggregation(IndentingPrintWriter pw, String label, IntervalStats.EventTracker tracker, boolean prettyDates) {
        if (tracker.count != 0 || tracker.duration != 0) {
            pw.print(label);
            pw.print(": ");
            pw.print(tracker.count);
            pw.print("x for ");
            pw.print(formatElapsedTime(tracker.duration, prettyDates));
            if (tracker.curStartTime != 0) {
                pw.print(" (now running, started at ");
                formatDateTime(tracker.curStartTime, prettyDates);
                pw.print(")");
            }
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public void printIntervalStats(IndentingPrintWriter pw, IntervalStats stats, boolean prettyDates, boolean skipEvents, String pkg) {
        UsageStats usageStats;
        Iterator<UsageStats> it;
        int pkgCount;
        IndentingPrintWriter indentingPrintWriter = pw;
        IntervalStats intervalStats = stats;
        boolean z = prettyDates;
        String str = pkg;
        if (z) {
            indentingPrintWriter.printPair("timeRange", "\"" + DateUtils.formatDateRange(this.mContext, intervalStats.beginTime, intervalStats.endTime, sDateFormatFlags) + "\"");
        } else {
            indentingPrintWriter.printPair("beginTime", Long.valueOf(intervalStats.beginTime));
            indentingPrintWriter.printPair("endTime", Long.valueOf(intervalStats.endTime));
        }
        pw.println();
        pw.increaseIndent();
        indentingPrintWriter.println("packages");
        pw.increaseIndent();
        ArrayMap<String, UsageStats> pkgStats = intervalStats.packageStats;
        int pkgCount2 = pkgStats.size();
        for (int i = 0; i < pkgCount2; i++) {
            UsageStats usageStats2 = pkgStats.valueAt(i);
            if (str == null || str.equals(usageStats2.mPackageName)) {
                indentingPrintWriter.printPair("package", usageStats2.mPackageName);
                indentingPrintWriter.printPair("totalTime", formatElapsedTime(usageStats2.mTotalTimeInForeground, z));
                indentingPrintWriter.printPair("lastTime", formatDateTime(usageStats2.mLastTimeUsed, z));
                indentingPrintWriter.printPair("appLaunchCount", Integer.valueOf(usageStats2.mAppLaunchCount));
                pw.println();
            }
        }
        pw.decreaseIndent();
        pw.println();
        indentingPrintWriter.println("ChooserCounts");
        pw.increaseIndent();
        Iterator<UsageStats> it2 = pkgStats.values().iterator();
        while (it2.hasNext()) {
            UsageStats usageStats3 = it2.next();
            if (str == null || str.equals(usageStats3.mPackageName)) {
                indentingPrintWriter.printPair("package", usageStats3.mPackageName);
                if (usageStats3.mChooserCounts != null) {
                    int chooserCountSize = usageStats3.mChooserCounts.size();
                    for (int i2 = 0; i2 < chooserCountSize; i2++) {
                        String action = (String) usageStats3.mChooserCounts.keyAt(i2);
                        ArrayMap<String, Integer> counts = (ArrayMap) usageStats3.mChooserCounts.valueAt(i2);
                        int annotationSize = counts.size();
                        int j = 0;
                        while (j < annotationSize) {
                            String key = counts.keyAt(j);
                            ArrayMap<String, UsageStats> pkgStats2 = pkgStats;
                            int count = counts.valueAt(j).intValue();
                            if (count != 0) {
                                pkgCount = pkgCount2;
                                it = it2;
                                StringBuilder sb = new StringBuilder();
                                sb.append(action);
                                usageStats = usageStats3;
                                sb.append(":");
                                sb.append(key);
                                sb.append(" is ");
                                sb.append(Integer.toString(count));
                                indentingPrintWriter.printPair("ChooserCounts", sb.toString());
                                pw.println();
                            } else {
                                pkgCount = pkgCount2;
                                it = it2;
                                usageStats = usageStats3;
                            }
                            j++;
                            pkgStats = pkgStats2;
                            pkgCount2 = pkgCount;
                            it2 = it;
                            usageStats3 = usageStats;
                        }
                        int i3 = pkgCount2;
                        Iterator<UsageStats> it3 = it2;
                        UsageStats usageStats4 = usageStats3;
                    }
                }
                UsageStats usageStats5 = usageStats3;
                pw.println();
                pkgStats = pkgStats;
                pkgCount2 = pkgCount2;
                it2 = it2;
            }
        }
        int i4 = pkgCount2;
        pw.decreaseIndent();
        if (str == null) {
            indentingPrintWriter.println("configurations");
            pw.increaseIndent();
            ArrayMap<Configuration, ConfigurationStats> configStats = intervalStats.configurations;
            int configCount = configStats.size();
            for (int i5 = 0; i5 < configCount; i5++) {
                ConfigurationStats config = configStats.valueAt(i5);
                indentingPrintWriter.printPair("config", Configuration.resourceQualifierString(config.mConfiguration));
                indentingPrintWriter.printPair("totalTime", formatElapsedTime(config.mTotalTimeActive, z));
                indentingPrintWriter.printPair("lastTime", formatDateTime(config.mLastTimeActive, z));
                indentingPrintWriter.printPair(AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, Integer.valueOf(config.mActivationCount));
                pw.println();
            }
            pw.decreaseIndent();
            indentingPrintWriter.println("event aggregations");
            pw.increaseIndent();
            printEventAggregation(indentingPrintWriter, "screen-interactive", intervalStats.interactiveTracker, z);
            printEventAggregation(indentingPrintWriter, "screen-non-interactive", intervalStats.nonInteractiveTracker, z);
            printEventAggregation(indentingPrintWriter, "keyguard-shown", intervalStats.keyguardShownTracker, z);
            printEventAggregation(indentingPrintWriter, "keyguard-hidden", intervalStats.keyguardHiddenTracker, z);
            pw.decreaseIndent();
        }
        if (!skipEvents) {
            indentingPrintWriter.println("events");
            pw.increaseIndent();
            EventList events = intervalStats.events;
            int eventCount = events != null ? events.size() : 0;
            int i6 = 0;
            while (true) {
                int i7 = i6;
                if (i7 >= eventCount) {
                    break;
                }
                UsageEvents.Event event = events.get(i7);
                if (str == null || str.equals(event.mPackage)) {
                    printEvent(indentingPrintWriter, event, z);
                }
                i6 = i7 + 1;
            }
            pw.decreaseIndent();
        }
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
            case 10:
                return "NOTIFICATION_SEEN";
            case 11:
                return "STANDBY_BUCKET_CHANGED";
            case 12:
                return "NOTIFICATION_INTERRUPTION";
            case 13:
                return "SLICE_PINNED_PRIV";
            case 14:
                return "SLICE_PINNED";
            case 15:
                return "SCREEN_INTERACTIVE";
            case 16:
                return "SCREEN_NON_INTERACTIVE";
            default:
                return "UNKNOWN";
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getBackupPayload(String key) {
        return this.mDatabase.getBackupPayload(key);
    }

    /* access modifiers changed from: package-private */
    public void applyRestoredPayload(String key, byte[] payload) {
        this.mDatabase.applyRestoredPayload(key, payload);
    }
}
