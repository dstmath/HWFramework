package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.EventList;
import android.app.usage.EventStats;
import android.app.usage.TimeSparseArray;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.TrustedUIService;
import com.android.server.am.AssistDataRequester;
import com.android.server.pm.PackageManagerService;
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

/* access modifiers changed from: package-private */
public class UserUsageStatsService {
    private static final boolean DEBUG = false;
    private static final long[] INTERVAL_LENGTH = {86400000, UnixCalendar.WEEK_IN_MILLIS, UnixCalendar.MONTH_IN_MILLIS, 31536000000L};
    private static final int MAX_DAYS_ONECE = 10;
    private static final int MAX_EVENT_ONE_DAY = 30000;
    private static final String TAG = "UsageStatsService";
    private static final UsageStatsDatabase.StatCombiner<ConfigurationStats> sConfigStatsCombiner = new UsageStatsDatabase.StatCombiner<ConfigurationStats>() {
        /* class com.android.server.usage.UserUsageStatsService.AnonymousClass2 */

        @Override // com.android.server.usage.UsageStatsDatabase.StatCombiner
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
        /* class com.android.server.usage.UserUsageStatsService.AnonymousClass3 */

        @Override // com.android.server.usage.UsageStatsDatabase.StatCombiner
        public void combine(IntervalStats stats, boolean mutable, List<EventStats> accResult) {
            stats.addEventStatsTo(accResult);
        }
    };
    private static final UsageStatsDatabase.StatCombiner<UsageStats> sUsageStatsCombiner = new UsageStatsDatabase.StatCombiner<UsageStats>() {
        /* class com.android.server.usage.UserUsageStatsService.AnonymousClass1 */

        @Override // com.android.server.usage.UsageStatsDatabase.StatCombiner
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
    private final StatsUpdatedListener mListener;
    private final String mLogPrefix;
    private boolean mStatsChanged = false;
    private int mTimeZoneOffset = TimeZone.getDefault().getRawOffset();
    private final int mUserId;

    /* access modifiers changed from: package-private */
    public interface StatsUpdatedListener {
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
        IntervalStats[] intervalStatsArr;
        this.mDatabase.init(currentTimeMillis);
        int nullCount = 0;
        int i = 0;
        while (true) {
            intervalStatsArr = this.mCurrentStats;
            if (i >= intervalStatsArr.length) {
                break;
            }
            intervalStatsArr[i] = this.mDatabase.getLatestUsageStats(i);
            if (this.mCurrentStats[i] == null) {
                nullCount++;
            }
            i++;
        }
        if (nullCount > 0) {
            if (nullCount != intervalStatsArr.length) {
                Slog.w(TAG, this.mLogPrefix + "Some stats have no latest available");
            }
            loadActiveStats(currentTimeMillis, false);
        } else {
            updateRolloverDeadline(intervalStatsArr[0].endTime);
        }
        IntervalStats currentDailyStats = this.mCurrentStats[0];
        if (currentDailyStats != null) {
            UsageEvents.Event shutdownEvent = new UsageEvents.Event(26, currentDailyStats.lastTimeSaved + 1000);
            shutdownEvent.mPackage = PackageManagerService.PLATFORM_PACKAGE_NAME;
            currentDailyStats.addEvent(shutdownEvent);
            UsageEvents.Event startupEvent = new UsageEvents.Event(27, currentTimeMillis);
            startupEvent.mPackage = PackageManagerService.PLATFORM_PACKAGE_NAME;
            currentDailyStats.addEvent(startupEvent);
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
        int i = 5;
        if (event.mEventType == 5 && currentDailyStats.activeConfiguration != null) {
            event.mConfiguration = Configuration.generateDelta(currentDailyStats.activeConfiguration, newFullConfig);
        }
        if (!(event.mEventType == 6 || event.mEventType == 24 || event.mEventType == 25 || event.mEventType == 26)) {
            currentDailyStats.addEvent(event);
        }
        boolean incrementAppLaunch = false;
        if (event.mEventType == 1) {
            if (event.mPackage != null && !event.mPackage.equals(this.mLastBackgroundedPackage)) {
                incrementAppLaunch = true;
            }
        } else if (event.mEventType == 2 && event.mPackage != null) {
            this.mLastBackgroundedPackage = event.mPackage;
        }
        IntervalStats[] intervalStatsArr = this.mCurrentStats;
        int length = intervalStatsArr.length;
        int i2 = 0;
        while (i2 < length) {
            IntervalStats stats = intervalStatsArr[i2];
            int i3 = event.mEventType;
            if (i3 == i) {
                stats.updateConfigurationStats(newFullConfig, event.mTimeStamp);
            } else if (i3 != 9) {
                switch (i3) {
                    case 15:
                        stats.updateScreenInteractive(event.mTimeStamp);
                        continue;
                    case 16:
                        stats.updateScreenNonInteractive(event.mTimeStamp);
                        continue;
                    case 17:
                        stats.updateKeyguardShown(event.mTimeStamp);
                        continue;
                    case 18:
                        stats.updateKeyguardHidden(event.mTimeStamp);
                        continue;
                    default:
                        stats.update(event.mPackage, event.getClassName(), event.mTimeStamp, event.mEventType, event.mInstanceId);
                        if (incrementAppLaunch) {
                            stats.incrementAppLaunchCount(event.mPackage);
                            break;
                        } else {
                            continue;
                        }
                }
            } else {
                stats.updateChooserCounts(event.mPackage, event.mContentType, event.mAction);
                String[] annotations = event.mContentAnnotations;
                if (annotations != null) {
                    for (String annotation : annotations) {
                        stats.updateChooserCounts(event.mPackage, annotation, event.mAction);
                    }
                }
            }
            i2++;
            i = 5;
        }
        notifyStatsChanged();
    }

    private <T> List<T> queryStats(int intervalType, long beginTime, long endTime, UsageStatsDatabase.StatCombiner<T> combiner) {
        return queryStats(intervalType, beginTime, endTime, false, combiner);
    }

    private <T> List<T> queryStats(int intervalType, long beginTime, long endTime, boolean mayDropEvent, UsageStatsDatabase.StatCombiner<T> combiner) {
        int intervalType2;
        if (intervalType == 4) {
            int intervalType3 = this.mDatabase.findBestFitBucket(beginTime, endTime);
            if (intervalType3 < 0) {
                intervalType2 = 0;
            } else {
                intervalType2 = intervalType3;
            }
        } else {
            intervalType2 = intervalType;
        }
        if (intervalType2 >= 0) {
            IntervalStats[] intervalStatsArr = this.mCurrentStats;
            if (intervalType2 < intervalStatsArr.length) {
                IntervalStats currentStats = intervalStatsArr[intervalType2];
                if (beginTime >= currentStats.endTime) {
                    return null;
                }
                long truncatedEndTime = Math.min(currentStats.beginTime, endTime);
                int i = 0;
                boolean needDropEvent = mayDropEvent && intervalType2 == 0;
                if (needDropEvent) {
                    Slog.d(TAG, "drop event");
                }
                List<T> results = this.mDatabase.queryUsageStats(intervalType2, beginTime, truncatedEndTime, needDropEvent, combiner);
                if (beginTime < currentStats.endTime && endTime > currentStats.beginTime) {
                    if (results == null) {
                        results = new ArrayList();
                    }
                    combiner.combine(currentStats, true, results);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(this.mLogPrefix);
                sb.append("Results: ");
                if (results != null) {
                    i = results.size();
                }
                sb.append(i);
                Slog.i(TAG, sb.toString());
                return results;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public List<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, true, sUsageStatsCombiner);
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
    public UsageEvents queryEvents(final long beginTime, final long endTime, final boolean obfuscateInstantApps) {
        long minBeginTime;
        if (((int) ((endTime - beginTime) / 86400000)) + 1 > 10) {
            minBeginTime = endTime - (((long) 10) * 86400000);
        } else {
            minBeginTime = beginTime;
        }
        final ArraySet<String> names = new ArraySet<>();
        List<UsageEvents.Event> results = queryStats(0, minBeginTime, endTime, new UsageStatsDatabase.StatCombiner<UsageEvents.Event>() {
            /* class com.android.server.usage.UserUsageStatsService.AnonymousClass4 */

            @Override // com.android.server.usage.UsageStatsDatabase.StatCombiner
            public void combine(IntervalStats stats, boolean mutable, List<UsageEvents.Event> accumulatedResult) {
                int startIndex = stats.events.firstIndexOnOrAfter(beginTime);
                int size = stats.events.size();
                if (size > startIndex + UserUsageStatsService.MAX_EVENT_ONE_DAY) {
                    SparseArray<Integer> counts = new SparseArray<>();
                    for (int i = startIndex; i < size; i++) {
                        UsageEvents.Event event = stats.events.get(i);
                        int index = counts.indexOfKey(event.getEventType());
                        if (index >= 0) {
                            counts.setValueAt(index, Integer.valueOf(counts.get(event.getEventType()).intValue() + 1));
                        } else {
                            counts.append(event.getEventType(), 1);
                        }
                    }
                    for (int i2 = 0; i2 < counts.size(); i2++) {
                        Slog.w(UserUsageStatsService.TAG, "queryEvents type: " + counts.keyAt(i2) + " count: " + counts.valueAt(i2));
                    }
                    size = startIndex + UserUsageStatsService.MAX_EVENT_ONE_DAY;
                    Slog.w(UserUsageStatsService.TAG, "queryEvents too much event " + stats.events.size() + " limit to " + size);
                }
                int i3 = startIndex;
                while (i3 < size && stats.events.get(i3).mTimeStamp < endTime) {
                    UsageEvents.Event event2 = stats.events.get(i3);
                    if (obfuscateInstantApps) {
                        event2 = event2.getObfuscatedIfInstantApp();
                    }
                    if (event2.mPackage != null) {
                        names.add(event2.mPackage);
                    }
                    if (event2.mClass != null) {
                        names.add(event2.mClass);
                    }
                    if (event2.mTaskRootPackage != null) {
                        names.add(event2.mTaskRootPackage);
                    }
                    if (event2.mTaskRootClass != null) {
                        names.add(event2.mTaskRootClass);
                    }
                    accumulatedResult.add(event2);
                    i3++;
                }
            }
        });
        if (results == null || results.isEmpty()) {
            return null;
        }
        String[] table = (String[]) names.toArray(new String[names.size()]);
        Arrays.sort(table);
        return new UsageEvents(results, table, true);
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEventsForPackage(long beginTime, long endTime, String packageName, boolean includeTaskRoot) {
        ArraySet<String> names = new ArraySet<>();
        names.add(packageName);
        List<UsageEvents.Event> results = queryStats(0, beginTime, endTime, new UsageStatsDatabase.StatCombiner(beginTime, endTime, packageName, names, includeTaskRoot) {
            /* class com.android.server.usage.$$Lambda$UserUsageStatsService$wWX7s9XZT5O4B7JcG_IB_VcPI9s */
            private final /* synthetic */ long f$0;
            private final /* synthetic */ long f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ ArraySet f$3;
            private final /* synthetic */ boolean f$4;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
            }

            @Override // com.android.server.usage.UsageStatsDatabase.StatCombiner
            public final void combine(IntervalStats intervalStats, boolean z, List list) {
                UserUsageStatsService.lambda$queryEventsForPackage$0(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, intervalStats, z, list);
            }
        });
        if (results == null) {
            return null;
        }
        if (results.isEmpty()) {
            return null;
        }
        String[] table = (String[]) names.toArray(new String[names.size()]);
        Arrays.sort(table);
        return new UsageEvents(results, table, includeTaskRoot);
    }

    static /* synthetic */ void lambda$queryEventsForPackage$0(long beginTime, long endTime, String packageName, ArraySet names, boolean includeTaskRoot, IntervalStats stats, boolean mutable, List accumulatedResult) {
        int startIndex = stats.events.firstIndexOnOrAfter(beginTime);
        int size = stats.events.size();
        int i = startIndex;
        while (i < size && stats.events.get(i).mTimeStamp < endTime) {
            UsageEvents.Event event = stats.events.get(i);
            if (packageName.equals(event.mPackage)) {
                if (event.mClass != null) {
                    names.add(event.mClass);
                }
                if (includeTaskRoot && event.mTaskRootPackage != null) {
                    names.add(event.mTaskRootPackage);
                }
                if (includeTaskRoot && event.mTaskRootClass != null) {
                    names.add(event.mTaskRootClass);
                }
                accumulatedResult.add(event);
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    public void persistActiveStats() {
        if (this.mStatsChanged) {
            Slog.i(TAG, this.mLogPrefix + "Flushing usage stats to disk");
            for (int i = 0; i < this.mCurrentStats.length; i++) {
                try {
                    this.mDatabase.putUsageStats(i, this.mCurrentStats[i]);
                } catch (IOException e) {
                    Slog.e(TAG, this.mLogPrefix + "Failed to persist active stats", e);
                    return;
                }
            }
            this.mStatsChanged = false;
        }
    }

    private void rolloverStats(long currentTimeMillis) {
        IntervalStats stat;
        int i;
        int i2;
        long beginTime;
        IntervalStats[] intervalStatsArr;
        int continueCount;
        IntervalStats stat2;
        int pkgCount;
        long startTime = SystemClock.elapsedRealtime();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats");
        Configuration previousConfig = this.mCurrentStats[0].activeConfiguration;
        ArraySet<String> continuePkgs = new ArraySet<>();
        ArrayMap<String, SparseIntArray> continueActivity = new ArrayMap<>();
        ArrayMap<String, ArrayMap<String, Integer>> continueForegroundService = new ArrayMap<>();
        IntervalStats[] intervalStatsArr2 = this.mCurrentStats;
        int length = intervalStatsArr2.length;
        int i3 = 0;
        while (i3 < length) {
            IntervalStats stat3 = intervalStatsArr2[i3];
            int pkgCount2 = stat3.packageStats.size();
            int i4 = 0;
            while (i4 < pkgCount2) {
                UsageStats pkgStats = stat3.packageStats.valueAt(i4);
                if (pkgStats.mActivities.size() > 0 || !pkgStats.mForegroundServices.isEmpty()) {
                    if (pkgStats.mActivities.size() > 0) {
                        continueActivity.put(pkgStats.mPackageName, pkgStats.mActivities);
                        pkgCount = pkgCount2;
                        stat2 = stat3;
                        stat3.update(pkgStats.mPackageName, null, this.mDailyExpiryDate.getTimeInMillis() - 1, 3, 0);
                    } else {
                        pkgCount = pkgCount2;
                        stat2 = stat3;
                    }
                    if (!pkgStats.mForegroundServices.isEmpty()) {
                        continueForegroundService.put(pkgStats.mPackageName, pkgStats.mForegroundServices);
                        stat2.update(pkgStats.mPackageName, null, this.mDailyExpiryDate.getTimeInMillis() - 1, 22, 0);
                    }
                    continuePkgs.add(pkgStats.mPackageName);
                    notifyStatsChanged();
                } else {
                    pkgCount = pkgCount2;
                    stat2 = stat3;
                }
                i4++;
                intervalStatsArr2 = intervalStatsArr2;
                length = length;
                pkgCount2 = pkgCount;
                stat3 = stat2;
            }
            stat3.updateConfigurationStats(null, this.mDailyExpiryDate.getTimeInMillis() - 1);
            stat3.commitTime(this.mDailyExpiryDate.getTimeInMillis() - 1);
            i3++;
            intervalStatsArr2 = intervalStatsArr2;
            length = length;
        }
        persistActiveStats();
        this.mDatabase.prune(currentTimeMillis);
        loadActiveStats(currentTimeMillis, false);
        int continueCount2 = continuePkgs.size();
        for (int i5 = 0; i5 < continueCount2; i5++) {
            String pkgName = continuePkgs.valueAt(i5);
            long beginTime2 = this.mCurrentStats[0].beginTime;
            IntervalStats[] intervalStatsArr3 = this.mCurrentStats;
            long beginTime3 = beginTime2;
            int i6 = 0;
            for (int length2 = intervalStatsArr3.length; i6 < length2; length2 = i) {
                int i7 = length2;
                IntervalStats stat4 = intervalStatsArr3[i6];
                if (continueActivity.containsKey(pkgName)) {
                    SparseIntArray eventMap = continueActivity.get(pkgName);
                    int size = eventMap.size();
                    continueCount = continueCount2;
                    int j = 0;
                    while (j < size) {
                        stat4.update(pkgName, null, beginTime3, eventMap.valueAt(j), eventMap.keyAt(j));
                        j++;
                        intervalStatsArr3 = intervalStatsArr3;
                        i6 = i6;
                        i7 = i7;
                        stat4 = stat4;
                    }
                    intervalStatsArr = intervalStatsArr3;
                    i2 = i6;
                    stat = stat4;
                    beginTime = beginTime3;
                    i = i7;
                } else {
                    continueCount = continueCount2;
                    intervalStatsArr = intervalStatsArr3;
                    i2 = i6;
                    stat = stat4;
                    beginTime = beginTime3;
                    i = i7;
                }
                if (continueForegroundService.containsKey(pkgName)) {
                    ArrayMap<String, Integer> eventMap2 = continueForegroundService.get(pkgName);
                    int size2 = eventMap2.size();
                    for (int j2 = 0; j2 < size2; j2++) {
                        stat.update(pkgName, eventMap2.keyAt(j2), beginTime, eventMap2.valueAt(j2).intValue(), 0);
                    }
                }
                stat.updateConfigurationStats(previousConfig, beginTime);
                notifyStatsChanged();
                i6 = i2 + 1;
                beginTime3 = beginTime;
                continueCount2 = continueCount;
                intervalStatsArr3 = intervalStatsArr;
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

    private void loadActiveStats(long currentTimeMillis, boolean isForce) {
        UnixCalendar tempCal = new UnixCalendar(0);
        UnixCalendar lastExpiryDate = new UnixCalendar(0);
        tempCal.setTimeInMillis(currentTimeMillis);
        UnixCalendar.truncateTo(tempCal, 0);
        int intervalType = 0;
        while (true) {
            IntervalStats[] intervalStatsArr = this.mCurrentStats;
            if (intervalType < intervalStatsArr.length) {
                if (isForce || intervalStatsArr[intervalType] == null || intervalStatsArr[intervalType].beginTime != tempCal.getTimeInMillis()) {
                    IntervalStats stats = this.mDatabase.getLatestUsageStats(intervalType);
                    this.mCurrentStats[intervalType] = null;
                    if (stats != null) {
                        lastExpiryDate.setTimeInMillis(stats.endTime);
                        UnixCalendar.truncateTo(lastExpiryDate, intervalType);
                        Slog.d(TAG, "check currentTime(" + currentTimeMillis + ") is between endTime(" + stats.endTime + ") and expireTime(" + (lastExpiryDate.getTimeInMillis() + INTERVAL_LENGTH[intervalType]) + "), interval: " + intervalType);
                        if (currentTimeMillis > stats.endTime && currentTimeMillis < lastExpiryDate.getTimeInMillis() + INTERVAL_LENGTH[intervalType]) {
                            this.mCurrentStats[intervalType] = stats;
                        }
                    }
                    IntervalStats[] intervalStatsArr2 = this.mCurrentStats;
                    if (intervalStatsArr2[intervalType] == null) {
                        intervalStatsArr2[intervalType] = new IntervalStats();
                        this.mCurrentStats[intervalType].beginTime = tempCal.getTimeInMillis();
                        this.mCurrentStats[intervalType].endTime = currentTimeMillis;
                    }
                }
                intervalType++;
            } else {
                this.mStatsChanged = false;
                updateRolloverDeadline(currentTimeMillis);
                this.mListener.onStatsReloaded();
                return;
            }
        }
    }

    private void updateRolloverDeadline(long currentTimeMillis) {
        this.mDailyExpiryDate.setTimeInMillis(currentTimeMillis);
        this.mDailyExpiryDate.addDays(1);
        this.mDailyExpiryDate.truncateToDay();
        Slog.i(TAG, this.mLogPrefix + "Rollover scheduled @ " + sDateFormat.format(Long.valueOf(this.mDailyExpiryDate.getTimeInMillis())) + "(" + this.mDailyExpiryDate.getTimeInMillis() + ").");
    }

    /* access modifiers changed from: package-private */
    public void checkin(final IndentingPrintWriter pw) {
        this.mDatabase.checkinDailyFiles(new UsageStatsDatabase.CheckinAction() {
            /* class com.android.server.usage.UserUsageStatsService.AnonymousClass5 */

            @Override // com.android.server.usage.UsageStatsDatabase.CheckinAction
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
        this.mDatabase.dump(pw, compact);
    }

    /* access modifiers changed from: package-private */
    public void dumpDatabaseInfo(IndentingPrintWriter ipw) {
        this.mDatabase.dump(ipw, false);
    }

    /* access modifiers changed from: package-private */
    public void dumpFile(IndentingPrintWriter ipw, String[] args) {
        int interval;
        if (args == null || args.length == 0) {
            int numIntervals = this.mDatabase.mSortedStatFiles.length;
            for (int interval2 = 0; interval2 < numIntervals; interval2++) {
                ipw.println("interval=" + intervalToString(interval2));
                ipw.increaseIndent();
                dumpFileDetailsForInterval(ipw, interval2);
                ipw.decreaseIndent();
            }
            return;
        }
        try {
            int intervalValue = stringToInterval(args[0]);
            if (intervalValue == -1) {
                interval = Integer.valueOf(args[0]).intValue();
            } else {
                interval = intervalValue;
            }
            if (interval < 0 || interval >= this.mDatabase.mSortedStatFiles.length) {
                ipw.println("the specified interval does not exist.");
            } else if (args.length == 1) {
                dumpFileDetailsForInterval(ipw, interval);
            } else {
                try {
                    IntervalStats stats = this.mDatabase.readIntervalStatsForFile(interval, Long.valueOf(args[1]).longValue());
                    if (stats == null) {
                        ipw.println("the specified filename does not exist.");
                    } else {
                        dumpFileDetails(ipw, stats, Long.valueOf(args[1]).longValue());
                    }
                } catch (NumberFormatException e) {
                    ipw.println("invalid filename specified.");
                }
            }
        } catch (NumberFormatException e2) {
            ipw.println("invalid interval specified.");
        }
    }

    private void dumpFileDetailsForInterval(IndentingPrintWriter ipw, int interval) {
        TimeSparseArray<AtomicFile> files = this.mDatabase.mSortedStatFiles[interval];
        int numFiles = files.size();
        for (int i = 0; i < numFiles; i++) {
            long filename = files.keyAt(i);
            dumpFileDetails(ipw, this.mDatabase.readIntervalStatsForFile(interval, filename), filename);
            ipw.println();
        }
    }

    private void dumpFileDetails(IndentingPrintWriter ipw, IntervalStats stats, long filename) {
        ipw.println("file=" + filename);
        ipw.increaseIndent();
        printIntervalStats(ipw, stats, false, false, null);
        ipw.decreaseIndent();
    }

    static String formatDateTime(long dateTime, boolean pretty) {
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
            pw.printPair("class", event.mClass);
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
        } else if (event.mEventType == 1 || event.mEventType == 2 || event.mEventType == 23) {
            pw.printPair("instanceId", Integer.valueOf(event.getInstanceId()));
        }
        if (event.getTaskRootPackageName() != null) {
            pw.printPair("taskRootPackage", event.getTaskRootPackageName());
        }
        if (event.getTaskRootClassName() != null) {
            pw.printPair("taskRootClass", event.getTaskRootClassName());
        }
        if (event.mNotificationChannelId != null) {
            pw.printPair("channelId", event.mNotificationChannelId);
        }
        pw.printHexPair("flags", event.mFlags);
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public void printLast24HrEvents(IndentingPrintWriter pw, boolean prettyDates, final String pkg) {
        final long endTime = System.currentTimeMillis();
        UnixCalendar yesterday = new UnixCalendar(endTime);
        yesterday.addDays(-1);
        final long beginTime = yesterday.getTimeInMillis();
        List<UsageEvents.Event> events = queryStats(0, beginTime, endTime, new UsageStatsDatabase.StatCombiner<UsageEvents.Event>() {
            /* class com.android.server.usage.UserUsageStatsService.AnonymousClass6 */

            @Override // com.android.server.usage.UsageStatsDatabase.StatCombiner
            public void combine(IntervalStats stats, boolean mutable, List<UsageEvents.Event> accumulatedResult) {
                int startIndex = stats.events.firstIndexOnOrAfter(beginTime);
                int size = stats.events.size();
                int i = startIndex;
                while (i < size && stats.events.get(i).mTimeStamp < endTime) {
                    UsageEvents.Event event = stats.events.get(i);
                    String str = pkg;
                    if (str == null || str.equals(event.mPackage)) {
                        accumulatedResult.add(event);
                    }
                    i++;
                }
            }
        });
        pw.print("Last 24 hour events (");
        if (prettyDates) {
            pw.printPair("timeRange", "\"" + DateUtils.formatDateRange(this.mContext, beginTime, endTime, sDateFormatFlags) + "\"");
        } else {
            pw.printPair("beginTime", Long.valueOf(beginTime));
            pw.printPair("endTime", Long.valueOf(endTime));
        }
        pw.println(")");
        if (events != null) {
            pw.increaseIndent();
            for (UsageEvents.Event event : events) {
                printEvent(pw, event, prettyDates);
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
        String str;
        Iterator<UsageStats> it;
        String str2;
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
        int i = 0;
        while (true) {
            str = "package";
            if (i >= pkgCount) {
                break;
            }
            UsageStats usageStats = pkgStats.valueAt(i);
            if (pkg == null || pkg.equals(usageStats.mPackageName)) {
                pw.printPair(str, usageStats.mPackageName);
                pw.printPair("totalTimeUsed", formatElapsedTime(usageStats.mTotalTimeInForeground, prettyDates));
                pw.printPair("lastTimeUsed", formatDateTime(usageStats.mLastTimeUsed, prettyDates));
                pw.printPair("totalTimeVisible", formatElapsedTime(usageStats.mTotalTimeVisible, prettyDates));
                pw.printPair("lastTimeVisible", formatDateTime(usageStats.mLastTimeVisible, prettyDates));
                pw.printPair("totalTimeFS", formatElapsedTime(usageStats.mTotalTimeForegroundServiceUsed, prettyDates));
                pw.printPair("lastTimeFS", formatDateTime(usageStats.mLastTimeForegroundServiceUsed, prettyDates));
                pw.printPair("appLaunchCount", Integer.valueOf(usageStats.mAppLaunchCount));
                pw.println();
            }
            i++;
        }
        pw.decreaseIndent();
        pw.println();
        pw.println("ChooserCounts");
        pw.increaseIndent();
        Iterator<UsageStats> it2 = pkgStats.values().iterator();
        while (it2.hasNext()) {
            UsageStats usageStats2 = it2.next();
            if (pkg == null || pkg.equals(usageStats2.mPackageName)) {
                pw.printPair(str, usageStats2.mPackageName);
                if (usageStats2.mChooserCounts != null) {
                    int chooserCountSize = usageStats2.mChooserCounts.size();
                    int i2 = 0;
                    while (i2 < chooserCountSize) {
                        String action = (String) usageStats2.mChooserCounts.keyAt(i2);
                        ArrayMap<String, Integer> counts = (ArrayMap) usageStats2.mChooserCounts.valueAt(i2);
                        int annotationSize = counts.size();
                        int j = 0;
                        while (j < annotationSize) {
                            String key = counts.keyAt(j);
                            int count = counts.valueAt(j).intValue();
                            if (count != 0) {
                                str2 = str;
                                StringBuilder sb = new StringBuilder();
                                sb.append(action);
                                it = it2;
                                sb.append(":");
                                sb.append(key);
                                sb.append(" is ");
                                sb.append(Integer.toString(count));
                                pw.printPair("ChooserCounts", sb.toString());
                                pw.println();
                            } else {
                                str2 = str;
                                it = it2;
                            }
                            j++;
                            pkgCount = pkgCount;
                            str = str2;
                            it2 = it;
                        }
                        i2++;
                        pkgStats = pkgStats;
                    }
                }
                pw.println();
                pkgStats = pkgStats;
                pkgCount = pkgCount;
                str = str;
                it2 = it2;
            }
        }
        pw.decreaseIndent();
        if (pkg == null) {
            pw.println("configurations");
            pw.increaseIndent();
            ArrayMap<Configuration, ConfigurationStats> configStats = stats.configurations;
            int configCount = configStats.size();
            for (int i3 = 0; i3 < configCount; i3++) {
                ConfigurationStats config = configStats.valueAt(i3);
                pw.printPair("config", Configuration.resourceQualifierString(config.mConfiguration));
                pw.printPair("totalTime", formatElapsedTime(config.mTotalTimeActive, prettyDates));
                pw.printPair("lastTime", formatDateTime(config.mLastTimeActive, prettyDates));
                pw.printPair(AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, Integer.valueOf(config.mActivationCount));
                pw.println();
            }
            pw.decreaseIndent();
            pw.println("event aggregations");
            pw.increaseIndent();
            printEventAggregation(pw, "screen-interactive", stats.interactiveTracker, prettyDates);
            printEventAggregation(pw, "screen-non-interactive", stats.nonInteractiveTracker, prettyDates);
            printEventAggregation(pw, "keyguard-shown", stats.keyguardShownTracker, prettyDates);
            printEventAggregation(pw, "keyguard-hidden", stats.keyguardHiddenTracker, prettyDates);
            pw.decreaseIndent();
        }
        if (!skipEvents) {
            pw.println("events");
            pw.increaseIndent();
            EventList events = stats.events;
            int eventCount = events != null ? events.size() : 0;
            for (int i4 = 0; i4 < eventCount; i4++) {
                UsageEvents.Event event = events.get(i4);
                if (pkg == null || pkg.equals(event.mPackage)) {
                    printEvent(pw, event, prettyDates);
                }
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }

    public static String intervalToString(int interval) {
        if (interval == 0) {
            return "daily";
        }
        if (interval == 1) {
            return "weekly";
        }
        if (interval == 2) {
            return "monthly";
        }
        if (interval != 3) {
            return "?";
        }
        return "yearly";
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int stringToInterval(String interval) {
        boolean z;
        String lowerCase = interval.toLowerCase();
        switch (lowerCase.hashCode()) {
            case -791707519:
                if (lowerCase.equals("weekly")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -734561654:
                if (lowerCase.equals("yearly")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 95346201:
                if (lowerCase.equals("daily")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 1236635661:
                if (lowerCase.equals("monthly")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 0;
        }
        if (z) {
            return 1;
        }
        if (!z) {
            return !z ? -1 : 3;
        }
        return 2;
    }

    static String eventToString(int eventType) {
        switch (eventType) {
            case 0:
                return "NONE";
            case 1:
                return "ACTIVITY_RESUMED";
            case 2:
                return "ACTIVITY_PAUSED";
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
            case 17:
                return "KEYGUARD_SHOWN";
            case 18:
                return "KEYGUARD_HIDDEN";
            case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
                return "FOREGROUND_SERVICE_START";
            case 20:
                return "FOREGROUND_SERVICE_STOP";
            case 21:
                return "CONTINUING_FOREGROUND_SERVICE";
            case FaceAcquiredInfo.VENDOR /* 22 */:
                return "ROLLOVER_FOREGROUND_SERVICE";
            case 23:
                return "ACTIVITY_STOPPED";
            case 24:
            case 25:
            default:
                return "UNKNOWN_TYPE_" + eventType;
            case TrustedUIService.TUI_POLL_FOLD /* 26 */:
                return "DEVICE_SHUTDOWN";
            case 27:
                return "DEVICE_STARTUP";
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
