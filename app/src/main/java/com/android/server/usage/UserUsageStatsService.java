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
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import com.android.server.usage.UsageStatsDatabase.CheckinAction;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class UserUsageStatsService {
    private static final boolean DEBUG = false;
    private static final long[] INTERVAL_LENGTH = null;
    private static final String TAG = "UsageStatsService";
    private static final StatCombiner<ConfigurationStats> sConfigStatsCombiner = null;
    private static final SimpleDateFormat sDateFormat = null;
    private static final int sDateFormatFlags = 131093;
    private static final StatCombiner<UsageStats> sUsageStatsCombiner = null;
    private final Context mContext;
    private final IntervalStats[] mCurrentStats;
    private final UnixCalendar mDailyExpiryDate;
    private final UsageStatsDatabase mDatabase;
    private final StatsUpdatedListener mListener;
    private final String mLogPrefix;
    private boolean mStatsChanged;
    private final int mUserId;

    interface StatsUpdatedListener {
        void onNewUpdate(int i);

        void onStatsReloaded();

        void onStatsUpdated();
    }

    /* renamed from: com.android.server.usage.UserUsageStatsService.3 */
    class AnonymousClass3 implements StatCombiner<Event> {
        final /* synthetic */ long val$beginTime;
        final /* synthetic */ long val$endTime;
        final /* synthetic */ ArraySet val$names;

        AnonymousClass3(long val$beginTime, long val$endTime, ArraySet val$names) {
            this.val$beginTime = val$beginTime;
            this.val$endTime = val$endTime;
            this.val$names = val$names;
        }

        public void combine(IntervalStats stats, boolean mutable, List<Event> accumulatedResult) {
            if (stats.events != null) {
                int startIndex = stats.events.closestIndexOnOrAfter(this.val$beginTime);
                if (startIndex >= 0) {
                    int size = stats.events.size();
                    int i = startIndex;
                    while (i < size && stats.events.keyAt(i) < this.val$endTime) {
                        Event event = (Event) stats.events.valueAt(i);
                        this.val$names.add(event.mPackage);
                        if (event.mClass != null) {
                            this.val$names.add(event.mClass);
                        }
                        accumulatedResult.add(event);
                        i++;
                    }
                }
            }
        }
    }

    /* renamed from: com.android.server.usage.UserUsageStatsService.4 */
    class AnonymousClass4 implements CheckinAction {
        final /* synthetic */ IndentingPrintWriter val$pw;

        AnonymousClass4(IndentingPrintWriter val$pw) {
            this.val$pw = val$pw;
        }

        public boolean checkin(IntervalStats stats) {
            UserUsageStatsService.this.printIntervalStats(this.val$pw, stats, UserUsageStatsService.DEBUG);
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.usage.UserUsageStatsService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.usage.UserUsageStatsService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usage.UserUsageStatsService.<clinit>():void");
    }

    UserUsageStatsService(Context context, int userId, File usageStatsDir, StatsUpdatedListener listener) {
        this.mStatsChanged = DEBUG;
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
            }
        }
        if (nullCount > 0) {
            if (nullCount != this.mCurrentStats.length) {
                Slog.w(TAG, this.mLogPrefix + "Some stats have no latest available");
            }
            loadActiveStats(currentTimeMillis, DEBUG);
        } else {
            updateRolloverDeadline();
        }
        IntervalStats[] intervalStatsArr = this.mCurrentStats;
        int length = intervalStatsArr.length;
        while (i2 < length) {
            IntervalStats stat = intervalStatsArr[i2];
            int pkgCount = stat.packageStats.size();
            for (i = 0; i < pkgCount; i++) {
                UsageStats pkgStats = (UsageStats) stat.packageStats.valueAt(i);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
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
        int i = 0;
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
        IntervalStats[] intervalStatsArr = this.mCurrentStats;
        int length = intervalStatsArr.length;
        while (i < length) {
            IntervalStats stats = intervalStatsArr[i];
            if (event.mEventType == 5) {
                stats.updateConfigurationStats(newFullConfig, event.mTimeStamp);
            } else {
                stats.update(event.mPackage, event.mTimeStamp, event.mEventType);
            }
            i++;
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

    UsageEvents queryEvents(long beginTime, long endTime) {
        ArraySet<String> names = new ArraySet();
        List<Event> results = queryStats(0, beginTime, endTime, new AnonymousClass3(beginTime, endTime, names));
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
            this.mStatsChanged = DEBUG;
        }
    }

    private void rolloverStats(long currentTimeMillis) {
        long startTime = SystemClock.elapsedRealtime();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats");
        Configuration previousConfig = this.mCurrentStats[0].activeConfiguration;
        ArraySet<String> continuePreviousDay = new ArraySet();
        for (IntervalStats stat : this.mCurrentStats) {
            int i;
            int pkgCount = stat.packageStats.size();
            for (i = 0; i < pkgCount; i++) {
                UsageStats pkgStats = (UsageStats) stat.packageStats.valueAt(i);
                int i2 = pkgStats.mLastEvent;
                if (r0 != 1) {
                    i2 = pkgStats.mLastEvent;
                    if (r0 != 4) {
                    }
                }
                continuePreviousDay.add(pkgStats.mPackageName);
                stat.update(pkgStats.mPackageName, this.mDailyExpiryDate.getTimeInMillis() - 1, 3);
                notifyStatsChanged();
            }
            stat.updateConfigurationStats(null, this.mDailyExpiryDate.getTimeInMillis() - 1);
        }
        persistActiveStats();
        this.mDatabase.prune(currentTimeMillis);
        loadActiveStats(currentTimeMillis, DEBUG);
        int continueCount = continuePreviousDay.size();
        for (i = 0; i < continueCount; i++) {
            String name = (String) continuePreviousDay.valueAt(i);
            long beginTime = this.mCurrentStats[0].beginTime;
            for (IntervalStats stat2 : this.mCurrentStats) {
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
        UnixCalendar tempCal = this.mDailyExpiryDate;
        int intervalType = 0;
        while (intervalType < this.mCurrentStats.length) {
            tempCal.setTimeInMillis(currentTimeMillis);
            UnixCalendar.truncateTo(tempCal, intervalType);
            if (force || this.mCurrentStats[intervalType] == null || this.mCurrentStats[intervalType].beginTime != tempCal.getTimeInMillis()) {
                if (this.mDatabase.getLatestUsageStatsBeginTime(intervalType) >= tempCal.getTimeInMillis()) {
                    this.mCurrentStats[intervalType] = this.mDatabase.getLatestUsageStats(intervalType);
                } else {
                    this.mCurrentStats[intervalType] = null;
                }
                if (this.mCurrentStats[intervalType] == null) {
                    this.mCurrentStats[intervalType] = new IntervalStats();
                    this.mCurrentStats[intervalType].beginTime = tempCal.getTimeInMillis();
                    this.mCurrentStats[intervalType].endTime = currentTimeMillis;
                }
            }
            intervalType++;
        }
        this.mStatsChanged = DEBUG;
        updateRolloverDeadline();
        this.mListener.onStatsReloaded();
    }

    private void updateRolloverDeadline() {
        this.mDailyExpiryDate.setTimeInMillis(this.mCurrentStats[0].beginTime);
        this.mDailyExpiryDate.addDays(1);
        this.mDailyExpiryDate.truncateToDay();
        Slog.i(TAG, this.mLogPrefix + "Rollover scheduled @ " + sDateFormat.format(Long.valueOf(this.mDailyExpiryDate.getTimeInMillis())) + "(" + this.mDailyExpiryDate.getTimeInMillis() + ")");
    }

    void checkin(IndentingPrintWriter pw) {
        this.mDatabase.checkinDailyFiles(new AnonymousClass4(pw));
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
        if (prettyDates) {
            IndentingPrintWriter indentingPrintWriter = pw;
            String str = "timeRange";
            indentingPrintWriter.printPair(str, "\"" + DateUtils.formatDateRange(this.mContext, stats.beginTime, stats.endTime, sDateFormatFlags) + "\"");
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
            UsageStats usageStats = (UsageStats) pkgStats.valueAt(i);
            pw.printPair(HwBroadcastRadarUtil.KEY_PACKAGE, usageStats.mPackageName);
            pw.printPair("totalTime", formatElapsedTime(usageStats.mTotalTimeInForeground, prettyDates));
            pw.printPair("lastTime", formatDateTime(usageStats.mLastTimeUsed, prettyDates));
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
            pw.println();
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    private static String intervalToString(int interval) {
        switch (interval) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "daily";
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "weekly";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "monthly";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "yearly";
            default:
                return "?";
        }
    }

    private static String eventToString(int eventType) {
        switch (eventType) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "NONE";
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "MOVE_TO_FOREGROUND";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "MOVE_TO_BACKGROUND";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "END_OF_DAY";
            case H.DO_TRAVERSAL /*4*/:
                return "CONTINUE_PREVIOUS_DAY";
            case H.ADD_STARTING /*5*/:
                return "CONFIGURATION_CHANGE";
            case H.REMOVE_STARTING /*6*/:
                return "SYSTEM_INTERACTION";
            case H.FINISHED_STARTING /*7*/:
                return "USER_INTERACTION";
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
