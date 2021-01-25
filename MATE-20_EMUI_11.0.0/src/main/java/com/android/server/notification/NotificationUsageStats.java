package com.android.server.notification;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.server.BatteryService;
import com.android.server.am.AssistDataRequester;
import com.android.server.notification.NotificationManagerService;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationUsageStats {
    private static final boolean DEBUG = false;
    private static final String DEVICE_GLOBAL_STATS = "__global";
    private static final long EMIT_PERIOD = 14400000;
    private static final AggregatedStats[] EMPTY_AGGREGATED_STATS = new AggregatedStats[0];
    private static final boolean ENABLE_AGGREGATED_IN_MEMORY_STATS = true;
    private static final boolean ENABLE_SQLITE_LOG = true;
    public static final int FOUR_HOURS = 14400000;
    private static final int MSG_EMIT = 1;
    private static final String TAG = "NotificationUsageStats";
    public static final int TEN_SECONDS = 10000;
    private final Context mContext;
    private final Handler mHandler;
    private long mLastEmitTime;
    private final SQLiteLog mSQLiteLog;
    private ArraySet<String> mStatExpiredkeys = new ArraySet<>();
    private final Map<String, AggregatedStats> mStats = new HashMap();
    private final ArrayDeque<AggregatedStats[]> mStatsArrays = new ArrayDeque<>();

    public NotificationUsageStats(Context context) {
        this.mContext = context;
        this.mLastEmitTime = SystemClock.elapsedRealtime();
        this.mSQLiteLog = new SQLiteLog(context);
        this.mHandler = new Handler(this.mContext.getMainLooper()) {
            /* class com.android.server.notification.NotificationUsageStats.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what != 1) {
                    Log.wtf(NotificationUsageStats.TAG, "Unknown message type: " + msg.what);
                    return;
                }
                NotificationUsageStats.this.emit();
            }
        };
        this.mHandler.sendEmptyMessageDelayed(1, 14400000);
    }

    public synchronized float getAppEnqueueRate(String packageName) {
        AggregatedStats stats = getOrCreateAggregatedStatsLocked(packageName);
        if (stats == null) {
            return 0.0f;
        }
        return stats.getEnqueueRate(SystemClock.elapsedRealtime());
    }

    public synchronized boolean isAlertRateLimited(String packageName) {
        AggregatedStats stats = getOrCreateAggregatedStatsLocked(packageName);
        if (stats == null) {
            return false;
        }
        return stats.isAlertRateLimited();
    }

    public synchronized void registerEnqueuedByApp(String packageName) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(packageName);
        for (AggregatedStats stats : aggregatedStatsArray) {
            stats.numEnqueuedByApp++;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerPostedByApp(NotificationRecord notification) {
        long now = SystemClock.elapsedRealtime();
        notification.stats.posttimeElapsedMs = now;
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        for (AggregatedStats stats : aggregatedStatsArray) {
            stats.numPostedByApp++;
            stats.updateInterarrivalEstimate(now);
            stats.countApiUse(notification);
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
        this.mSQLiteLog.logPosted(notification);
    }

    public synchronized void registerUpdatedByApp(NotificationRecord notification, NotificationRecord old) {
        notification.stats.updateFrom(old.stats);
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        for (AggregatedStats stats : aggregatedStatsArray) {
            stats.numUpdatedByApp++;
            stats.updateInterarrivalEstimate(SystemClock.elapsedRealtime());
            stats.countApiUse(notification);
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
        this.mSQLiteLog.logPosted(notification);
    }

    public synchronized void registerRemovedByApp(NotificationRecord notification) {
        notification.stats.onRemoved();
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        for (AggregatedStats stats : aggregatedStatsArray) {
            stats.numRemovedByApp++;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
        this.mSQLiteLog.logRemoved(notification);
    }

    public synchronized void registerDismissedByUser(NotificationRecord notification) {
        MetricsLogger.histogram(this.mContext, "note_dismiss_longevity", ((int) (System.currentTimeMillis() - notification.getRankingTimeMs())) / 60000);
        notification.stats.onDismiss();
        this.mSQLiteLog.logDismissed(notification);
    }

    public synchronized void registerClickedByUser(NotificationRecord notification) {
        MetricsLogger.histogram(this.mContext, "note_click_longevity", ((int) (System.currentTimeMillis() - notification.getRankingTimeMs())) / 60000);
        notification.stats.onClick();
        this.mSQLiteLog.logClicked(notification);
    }

    public synchronized void registerPeopleAffinity(NotificationRecord notification, boolean valid, boolean starred, boolean cached) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        for (AggregatedStats stats : aggregatedStatsArray) {
            if (valid) {
                stats.numWithValidPeople++;
            }
            if (starred) {
                stats.numWithStaredPeople++;
            }
            if (cached) {
                stats.numPeopleCacheHit++;
            } else {
                stats.numPeopleCacheMiss++;
            }
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerBlocked(NotificationRecord notification) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        for (AggregatedStats stats : aggregatedStatsArray) {
            stats.numBlocked++;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerSuspendedByAdmin(NotificationRecord notification) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        for (AggregatedStats stats : aggregatedStatsArray) {
            stats.numSuspendedByAdmin++;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerOverRateQuota(String packageName) {
        for (AggregatedStats stats : getAggregatedStatsLocked(packageName)) {
            stats.numRateViolations++;
        }
    }

    public synchronized void registerOverCountQuota(String packageName) {
        for (AggregatedStats stats : getAggregatedStatsLocked(packageName)) {
            stats.numQuotaViolations++;
        }
    }

    private AggregatedStats[] getAggregatedStatsLocked(NotificationRecord record) {
        return getAggregatedStatsLocked(record.sbn.getPackageName());
    }

    private AggregatedStats[] getAggregatedStatsLocked(String packageName) {
        AggregatedStats[] array = this.mStatsArrays.poll();
        if (array == null) {
            array = new AggregatedStats[2];
        }
        array[0] = getOrCreateAggregatedStatsLocked(DEVICE_GLOBAL_STATS);
        array[1] = getOrCreateAggregatedStatsLocked(packageName);
        return array;
    }

    private void releaseAggregatedStatsLocked(AggregatedStats[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = null;
        }
        this.mStatsArrays.offer(array);
    }

    private AggregatedStats getOrCreateAggregatedStatsLocked(String key) {
        AggregatedStats result = this.mStats.get(key);
        if (result == null) {
            result = new AggregatedStats(this.mContext, key);
            this.mStats.put(key, result);
        }
        result.mLastAccessTime = SystemClock.elapsedRealtime();
        return result;
    }

    public synchronized JSONObject dumpJson(NotificationManagerService.DumpFilter filter) {
        JSONObject dump;
        dump = new JSONObject();
        try {
            JSONArray aggregatedStats = new JSONArray();
            for (AggregatedStats as : this.mStats.values()) {
                if (filter == null || filter.matches(as.key)) {
                    aggregatedStats.put(as.dumpJson());
                }
            }
            dump.put("current", aggregatedStats);
        } catch (JSONException e) {
        }
        try {
            dump.put("historical", this.mSQLiteLog.dumpJson(filter));
        } catch (JSONException e2) {
        }
        return dump;
    }

    public synchronized void dump(PrintWriter pw, String indent, NotificationManagerService.DumpFilter filter) {
        for (AggregatedStats as : this.mStats.values()) {
            if (filter == null || filter.matches(as.key)) {
                as.dump(pw, indent);
            }
        }
        pw.println(indent + "mStatsArrays.size(): " + this.mStatsArrays.size());
        pw.println(indent + "mStats.size(): " + this.mStats.size());
        this.mSQLiteLog.dump(pw, indent, filter);
    }

    public synchronized void emit() {
        getOrCreateAggregatedStatsLocked(DEVICE_GLOBAL_STATS).emit();
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 14400000);
        for (String key : this.mStats.keySet()) {
            if (this.mStats.get(key).mLastAccessTime < this.mLastEmitTime) {
                this.mStatExpiredkeys.add(key);
            }
        }
        Iterator<String> it = this.mStatExpiredkeys.iterator();
        while (it.hasNext()) {
            this.mStats.remove(it.next());
        }
        this.mStatExpiredkeys.clear();
        this.mLastEmitTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public static class AggregatedStats {
        public AlertRateLimiter alertRate;
        public RateEstimator enqueueRate;
        public ImportanceHistogram finalImportance;
        public final String key;
        private final Context mContext;
        private final long mCreated = SystemClock.elapsedRealtime();
        public long mLastAccessTime;
        private AggregatedStats mPrevious;
        public ImportanceHistogram noisyImportance;
        public int numAlertViolations;
        public int numAutoCancel;
        public int numBlocked;
        public int numEnqueuedByApp;
        public int numForegroundService;
        public int numInterrupt;
        public int numOngoing;
        public int numPeopleCacheHit;
        public int numPeopleCacheMiss;
        public int numPostedByApp;
        public int numPrivate;
        public int numQuotaViolations;
        public int numRateViolations;
        public int numRemovedByApp;
        public int numSecret;
        public int numSuspendedByAdmin;
        public int numUpdatedByApp;
        public int numWithActions;
        public int numWithBigPicture;
        public int numWithBigText;
        public int numWithInbox;
        public int numWithInfoText;
        public int numWithLargeIcon;
        public int numWithMediaSession;
        public int numWithStaredPeople;
        public int numWithSubText;
        public int numWithText;
        public int numWithTitle;
        public int numWithValidPeople;
        public ImportanceHistogram quietImportance;

        public AggregatedStats(Context context, String key2) {
            this.key = key2;
            this.mContext = context;
            this.noisyImportance = new ImportanceHistogram(context, "note_imp_noisy_");
            this.quietImportance = new ImportanceHistogram(context, "note_imp_quiet_");
            this.finalImportance = new ImportanceHistogram(context, "note_importance_");
            this.enqueueRate = new RateEstimator();
            this.alertRate = new AlertRateLimiter();
        }

        public AggregatedStats getPrevious() {
            if (this.mPrevious == null) {
                this.mPrevious = new AggregatedStats(this.mContext, this.key);
            }
            return this.mPrevious;
        }

        public void countApiUse(NotificationRecord record) {
            Notification n = record.getNotification();
            if (n.actions != null) {
                this.numWithActions++;
            }
            if ((n.flags & 64) != 0) {
                this.numForegroundService++;
            }
            if ((n.flags & 2) != 0) {
                this.numOngoing++;
            }
            if ((n.flags & 16) != 0) {
                this.numAutoCancel++;
            }
            if (!((n.defaults & 1) == 0 && (n.defaults & 2) == 0 && n.sound == null && n.vibrate == null)) {
                this.numInterrupt++;
            }
            int i = n.visibility;
            if (i == -1) {
                this.numSecret++;
            } else if (i == 0) {
                this.numPrivate++;
            }
            if (record.stats.isNoisy) {
                this.noisyImportance.increment(record.stats.requestedImportance);
            } else {
                this.quietImportance.increment(record.stats.requestedImportance);
            }
            this.finalImportance.increment(record.getImportance());
            Set<String> names = n.extras.keySet();
            if (names.contains("android.bigText")) {
                this.numWithBigText++;
            }
            if (names.contains("android.picture")) {
                this.numWithBigPicture++;
            }
            if (names.contains("android.largeIcon")) {
                this.numWithLargeIcon++;
            }
            if (names.contains("android.textLines")) {
                this.numWithInbox++;
            }
            if (names.contains("android.mediaSession")) {
                this.numWithMediaSession++;
            }
            if (names.contains("android.title") && !TextUtils.isEmpty(n.extras.getCharSequence("android.title"))) {
                this.numWithTitle++;
            }
            if (names.contains("android.text") && !TextUtils.isEmpty(n.extras.getCharSequence("android.text"))) {
                this.numWithText++;
            }
            if (names.contains("android.subText") && !TextUtils.isEmpty(n.extras.getCharSequence("android.subText"))) {
                this.numWithSubText++;
            }
            if (names.contains("android.infoText") && !TextUtils.isEmpty(n.extras.getCharSequence("android.infoText"))) {
                this.numWithInfoText++;
            }
        }

        public void emit() {
            AggregatedStats previous = getPrevious();
            maybeCount("note_enqueued", this.numEnqueuedByApp - previous.numEnqueuedByApp);
            maybeCount("note_post", this.numPostedByApp - previous.numPostedByApp);
            maybeCount("note_update", this.numUpdatedByApp - previous.numUpdatedByApp);
            maybeCount("note_remove", this.numRemovedByApp - previous.numRemovedByApp);
            maybeCount("note_with_people", this.numWithValidPeople - previous.numWithValidPeople);
            maybeCount("note_with_stars", this.numWithStaredPeople - previous.numWithStaredPeople);
            maybeCount("people_cache_hit", this.numPeopleCacheHit - previous.numPeopleCacheHit);
            maybeCount("people_cache_miss", this.numPeopleCacheMiss - previous.numPeopleCacheMiss);
            maybeCount("note_blocked", this.numBlocked - previous.numBlocked);
            maybeCount("note_suspended", this.numSuspendedByAdmin - previous.numSuspendedByAdmin);
            maybeCount("note_with_actions", this.numWithActions - previous.numWithActions);
            maybeCount("note_private", this.numPrivate - previous.numPrivate);
            maybeCount("note_secret", this.numSecret - previous.numSecret);
            maybeCount("note_interupt", this.numInterrupt - previous.numInterrupt);
            maybeCount("note_big_text", this.numWithBigText - previous.numWithBigText);
            maybeCount("note_big_pic", this.numWithBigPicture - previous.numWithBigPicture);
            maybeCount("note_fg", this.numForegroundService - previous.numForegroundService);
            maybeCount("note_ongoing", this.numOngoing - previous.numOngoing);
            maybeCount("note_auto", this.numAutoCancel - previous.numAutoCancel);
            maybeCount("note_large_icon", this.numWithLargeIcon - previous.numWithLargeIcon);
            maybeCount("note_inbox", this.numWithInbox - previous.numWithInbox);
            maybeCount("note_media", this.numWithMediaSession - previous.numWithMediaSession);
            maybeCount("note_title", this.numWithTitle - previous.numWithTitle);
            maybeCount("note_text", this.numWithText - previous.numWithText);
            maybeCount("note_sub_text", this.numWithSubText - previous.numWithSubText);
            maybeCount("note_info_text", this.numWithInfoText - previous.numWithInfoText);
            maybeCount("note_over_rate", this.numRateViolations - previous.numRateViolations);
            maybeCount("note_over_alert_rate", this.numAlertViolations - previous.numAlertViolations);
            maybeCount("note_over_quota", this.numQuotaViolations - previous.numQuotaViolations);
            this.noisyImportance.maybeCount(previous.noisyImportance);
            this.quietImportance.maybeCount(previous.quietImportance);
            this.finalImportance.maybeCount(previous.finalImportance);
            previous.numEnqueuedByApp = this.numEnqueuedByApp;
            previous.numPostedByApp = this.numPostedByApp;
            previous.numUpdatedByApp = this.numUpdatedByApp;
            previous.numRemovedByApp = this.numRemovedByApp;
            previous.numPeopleCacheHit = this.numPeopleCacheHit;
            previous.numPeopleCacheMiss = this.numPeopleCacheMiss;
            previous.numWithStaredPeople = this.numWithStaredPeople;
            previous.numWithValidPeople = this.numWithValidPeople;
            previous.numBlocked = this.numBlocked;
            previous.numSuspendedByAdmin = this.numSuspendedByAdmin;
            previous.numWithActions = this.numWithActions;
            previous.numPrivate = this.numPrivate;
            previous.numSecret = this.numSecret;
            previous.numInterrupt = this.numInterrupt;
            previous.numWithBigText = this.numWithBigText;
            previous.numWithBigPicture = this.numWithBigPicture;
            previous.numForegroundService = this.numForegroundService;
            previous.numOngoing = this.numOngoing;
            previous.numAutoCancel = this.numAutoCancel;
            previous.numWithLargeIcon = this.numWithLargeIcon;
            previous.numWithInbox = this.numWithInbox;
            previous.numWithMediaSession = this.numWithMediaSession;
            previous.numWithTitle = this.numWithTitle;
            previous.numWithText = this.numWithText;
            previous.numWithSubText = this.numWithSubText;
            previous.numWithInfoText = this.numWithInfoText;
            previous.numRateViolations = this.numRateViolations;
            previous.numAlertViolations = this.numAlertViolations;
            previous.numQuotaViolations = this.numQuotaViolations;
            this.noisyImportance.update(previous.noisyImportance);
            this.quietImportance.update(previous.quietImportance);
            this.finalImportance.update(previous.finalImportance);
        }

        /* access modifiers changed from: package-private */
        public void maybeCount(String name, int value) {
            if (value > 0) {
                MetricsLogger.count(this.mContext, name, value);
            }
        }

        public void dump(PrintWriter pw, String indent) {
            pw.println(toStringWithIndent(indent));
        }

        public String toString() {
            return toStringWithIndent("");
        }

        public float getEnqueueRate() {
            return getEnqueueRate(SystemClock.elapsedRealtime());
        }

        public float getEnqueueRate(long now) {
            return this.enqueueRate.getRate(now);
        }

        public void updateInterarrivalEstimate(long now) {
            this.enqueueRate.update(now);
        }

        public boolean isAlertRateLimited() {
            boolean limited = this.alertRate.shouldRateLimitAlert(SystemClock.elapsedRealtime());
            if (limited) {
                this.numAlertViolations++;
            }
            return limited;
        }

        private String toStringWithIndent(String indent) {
            StringBuilder output = new StringBuilder();
            output.append(indent);
            output.append("AggregatedStats{\n");
            String indentPlusTwo = indent + "  ";
            output.append(indentPlusTwo);
            output.append("key='");
            output.append(this.key);
            output.append("',\n");
            output.append(indentPlusTwo);
            output.append("numEnqueuedByApp=");
            output.append(this.numEnqueuedByApp);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numPostedByApp=");
            output.append(this.numPostedByApp);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numUpdatedByApp=");
            output.append(this.numUpdatedByApp);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numRemovedByApp=");
            output.append(this.numRemovedByApp);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numPeopleCacheHit=");
            output.append(this.numPeopleCacheHit);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numWithStaredPeople=");
            output.append(this.numWithStaredPeople);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numWithValidPeople=");
            output.append(this.numWithValidPeople);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numPeopleCacheMiss=");
            output.append(this.numPeopleCacheMiss);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numBlocked=");
            output.append(this.numBlocked);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numSuspendedByAdmin=");
            output.append(this.numSuspendedByAdmin);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numWithActions=");
            output.append(this.numWithActions);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numPrivate=");
            output.append(this.numPrivate);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numSecret=");
            output.append(this.numSecret);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numInterrupt=");
            output.append(this.numInterrupt);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numWithBigText=");
            output.append(this.numWithBigText);
            output.append(",\n");
            output.append(indentPlusTwo);
            output.append("numWithBigPicture=");
            output.append(this.numWithBigPicture);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numForegroundService=");
            output.append(this.numForegroundService);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numOngoing=");
            output.append(this.numOngoing);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numAutoCancel=");
            output.append(this.numAutoCancel);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithLargeIcon=");
            output.append(this.numWithLargeIcon);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithInbox=");
            output.append(this.numWithInbox);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithMediaSession=");
            output.append(this.numWithMediaSession);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithTitle=");
            output.append(this.numWithTitle);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithText=");
            output.append(this.numWithText);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithSubText=");
            output.append(this.numWithSubText);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numWithInfoText=");
            output.append(this.numWithInfoText);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numRateViolations=");
            output.append(this.numRateViolations);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numAlertViolations=");
            output.append(this.numAlertViolations);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append("numQuotaViolations=");
            output.append(this.numQuotaViolations);
            output.append("\n");
            output.append(indentPlusTwo);
            output.append(this.noisyImportance.toString());
            output.append("\n");
            output.append(indentPlusTwo);
            output.append(this.quietImportance.toString());
            output.append("\n");
            output.append(indentPlusTwo);
            output.append(this.finalImportance.toString());
            output.append("\n");
            output.append(indent);
            output.append("}");
            return output.toString();
        }

        public JSONObject dumpJson() throws JSONException {
            AggregatedStats previous = getPrevious();
            JSONObject dump = new JSONObject();
            dump.put("key", this.key);
            dump.put("duration", SystemClock.elapsedRealtime() - this.mCreated);
            maybePut(dump, "numEnqueuedByApp", this.numEnqueuedByApp);
            maybePut(dump, "numPostedByApp", this.numPostedByApp);
            maybePut(dump, "numUpdatedByApp", this.numUpdatedByApp);
            maybePut(dump, "numRemovedByApp", this.numRemovedByApp);
            maybePut(dump, "numPeopleCacheHit", this.numPeopleCacheHit);
            maybePut(dump, "numPeopleCacheMiss", this.numPeopleCacheMiss);
            maybePut(dump, "numWithStaredPeople", this.numWithStaredPeople);
            maybePut(dump, "numWithValidPeople", this.numWithValidPeople);
            maybePut(dump, "numBlocked", this.numBlocked);
            maybePut(dump, "numSuspendedByAdmin", this.numSuspendedByAdmin);
            maybePut(dump, "numWithActions", this.numWithActions);
            maybePut(dump, "numPrivate", this.numPrivate);
            maybePut(dump, "numSecret", this.numSecret);
            maybePut(dump, "numInterrupt", this.numInterrupt);
            maybePut(dump, "numWithBigText", this.numWithBigText);
            maybePut(dump, "numWithBigPicture", this.numWithBigPicture);
            maybePut(dump, "numForegroundService", this.numForegroundService);
            maybePut(dump, "numOngoing", this.numOngoing);
            maybePut(dump, "numAutoCancel", this.numAutoCancel);
            maybePut(dump, "numWithLargeIcon", this.numWithLargeIcon);
            maybePut(dump, "numWithInbox", this.numWithInbox);
            maybePut(dump, "numWithMediaSession", this.numWithMediaSession);
            maybePut(dump, "numWithTitle", this.numWithTitle);
            maybePut(dump, "numWithText", this.numWithText);
            maybePut(dump, "numWithSubText", this.numWithSubText);
            maybePut(dump, "numWithInfoText", this.numWithInfoText);
            maybePut(dump, "numRateViolations", this.numRateViolations);
            maybePut(dump, "numQuotaLViolations", this.numQuotaViolations);
            maybePut(dump, "notificationEnqueueRate", getEnqueueRate());
            maybePut(dump, "numAlertViolations", this.numAlertViolations);
            this.noisyImportance.maybePut(dump, previous.noisyImportance);
            this.quietImportance.maybePut(dump, previous.quietImportance);
            this.finalImportance.maybePut(dump, previous.finalImportance);
            return dump;
        }

        private void maybePut(JSONObject dump, String name, int value) throws JSONException {
            if (value > 0) {
                dump.put(name, value);
            }
        }

        private void maybePut(JSONObject dump, String name, float value) throws JSONException {
            if (((double) value) > 0.0d) {
                dump.put(name, (double) value);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ImportanceHistogram {
        private static final String[] IMPORTANCE_NAMES = {"none", "min", "low", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, "high", "max"};
        private static final int NUM_IMPORTANCES = 6;
        private final Context mContext;
        private int[] mCount = new int[6];
        private final String[] mCounterNames = new String[6];
        private final String mPrefix;

        ImportanceHistogram(Context context, String prefix) {
            this.mContext = context;
            this.mPrefix = prefix;
            for (int i = 0; i < 6; i++) {
                String[] strArr = this.mCounterNames;
                strArr[i] = this.mPrefix + IMPORTANCE_NAMES[i];
            }
        }

        /* access modifiers changed from: package-private */
        public void increment(int imp) {
            int imp2 = Math.max(0, Math.min(imp, this.mCount.length - 1));
            int[] iArr = this.mCount;
            iArr[imp2] = iArr[imp2] + 1;
        }

        /* access modifiers changed from: package-private */
        public void maybeCount(ImportanceHistogram prev) {
            for (int i = 0; i < 6; i++) {
                int value = this.mCount[i] - prev.mCount[i];
                if (value > 0) {
                    MetricsLogger.count(this.mContext, this.mCounterNames[i], value);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void update(ImportanceHistogram that) {
            for (int i = 0; i < 6; i++) {
                this.mCount[i] = that.mCount[i];
            }
        }

        public void maybePut(JSONObject dump, ImportanceHistogram prev) throws JSONException {
            dump.put(this.mPrefix, new JSONArray(this.mCount));
        }

        public String toString() {
            StringBuilder output = new StringBuilder();
            output.append(this.mPrefix);
            output.append(": [");
            for (int i = 0; i < 6; i++) {
                output.append(this.mCount[i]);
                if (i < 5) {
                    output.append(", ");
                }
            }
            output.append("]");
            return output.toString();
        }
    }

    public static class SingleNotificationStats {
        public long airtimeCount = 0;
        public long airtimeExpandedMs = 0;
        public long airtimeMs = 0;
        public long currentAirtimeExpandedStartElapsedMs = -1;
        public long currentAirtimeStartElapsedMs = -1;
        private boolean isExpanded = false;
        public boolean isNoisy;
        private boolean isVisible = false;
        public int naturalImportance;
        public long posttimeElapsedMs = -1;
        public long posttimeToDismissMs = -1;
        public long posttimeToFirstAirtimeMs = -1;
        public long posttimeToFirstClickMs = -1;
        public long posttimeToFirstVisibleExpansionMs = -1;
        public int requestedImportance;
        public long userExpansionCount = 0;

        public long getCurrentPosttimeMs() {
            if (this.posttimeElapsedMs < 0) {
                return 0;
            }
            return SystemClock.elapsedRealtime() - this.posttimeElapsedMs;
        }

        public long getCurrentAirtimeMs() {
            long result = this.airtimeMs;
            if (this.currentAirtimeStartElapsedMs >= 0) {
                return result + (SystemClock.elapsedRealtime() - this.currentAirtimeStartElapsedMs);
            }
            return result;
        }

        public long getCurrentAirtimeExpandedMs() {
            long result = this.airtimeExpandedMs;
            if (this.currentAirtimeExpandedStartElapsedMs >= 0) {
                return result + (SystemClock.elapsedRealtime() - this.currentAirtimeExpandedStartElapsedMs);
            }
            return result;
        }

        public void onClick() {
            if (this.posttimeToFirstClickMs < 0) {
                this.posttimeToFirstClickMs = SystemClock.elapsedRealtime() - this.posttimeElapsedMs;
            }
        }

        public void onDismiss() {
            if (this.posttimeToDismissMs < 0) {
                this.posttimeToDismissMs = SystemClock.elapsedRealtime() - this.posttimeElapsedMs;
            }
            finish();
        }

        public void onCancel() {
            finish();
        }

        public void onRemoved() {
            finish();
        }

        public void onVisibilityChanged(boolean visible) {
            long elapsedNowMs = SystemClock.elapsedRealtime();
            boolean wasVisible = this.isVisible;
            this.isVisible = visible;
            if (visible) {
                if (this.currentAirtimeStartElapsedMs < 0) {
                    this.airtimeCount++;
                    this.currentAirtimeStartElapsedMs = elapsedNowMs;
                }
                if (this.posttimeToFirstAirtimeMs < 0) {
                    this.posttimeToFirstAirtimeMs = elapsedNowMs - this.posttimeElapsedMs;
                }
            } else {
                long j = this.currentAirtimeStartElapsedMs;
                if (j >= 0) {
                    this.airtimeMs += elapsedNowMs - j;
                    this.currentAirtimeStartElapsedMs = -1;
                }
            }
            if (wasVisible != this.isVisible) {
                updateVisiblyExpandedStats();
            }
        }

        public void onExpansionChanged(boolean userAction, boolean expanded) {
            this.isExpanded = expanded;
            if (this.isExpanded && userAction) {
                this.userExpansionCount++;
            }
            updateVisiblyExpandedStats();
        }

        public boolean hasBeenVisiblyExpanded() {
            return this.posttimeToFirstVisibleExpansionMs >= 0;
        }

        private void updateVisiblyExpandedStats() {
            long elapsedNowMs = SystemClock.elapsedRealtime();
            if (!this.isExpanded || !this.isVisible) {
                long j = this.currentAirtimeExpandedStartElapsedMs;
                if (j >= 0) {
                    this.airtimeExpandedMs += elapsedNowMs - j;
                    this.currentAirtimeExpandedStartElapsedMs = -1;
                    return;
                }
                return;
            }
            if (this.currentAirtimeExpandedStartElapsedMs < 0) {
                this.currentAirtimeExpandedStartElapsedMs = elapsedNowMs;
            }
            if (this.posttimeToFirstVisibleExpansionMs < 0) {
                this.posttimeToFirstVisibleExpansionMs = elapsedNowMs - this.posttimeElapsedMs;
            }
        }

        public void finish() {
            onVisibilityChanged(false);
        }

        public String toString() {
            return "SingleNotificationStats{posttimeElapsedMs=" + this.posttimeElapsedMs + ", posttimeToFirstClickMs=" + this.posttimeToFirstClickMs + ", posttimeToDismissMs=" + this.posttimeToDismissMs + ", airtimeCount=" + this.airtimeCount + ", airtimeMs=" + this.airtimeMs + ", currentAirtimeStartElapsedMs=" + this.currentAirtimeStartElapsedMs + ", airtimeExpandedMs=" + this.airtimeExpandedMs + ", posttimeToFirstVisibleExpansionMs=" + this.posttimeToFirstVisibleExpansionMs + ", currentAirtimeExpandedStartElapsedMs=" + this.currentAirtimeExpandedStartElapsedMs + ", requestedImportance=" + this.requestedImportance + ", naturalImportance=" + this.naturalImportance + ", isNoisy=" + this.isNoisy + '}';
        }

        public void updateFrom(SingleNotificationStats old) {
            this.posttimeElapsedMs = old.posttimeElapsedMs;
            this.posttimeToFirstClickMs = old.posttimeToFirstClickMs;
            this.airtimeCount = old.airtimeCount;
            this.posttimeToFirstAirtimeMs = old.posttimeToFirstAirtimeMs;
            this.currentAirtimeStartElapsedMs = old.currentAirtimeStartElapsedMs;
            this.airtimeMs = old.airtimeMs;
            this.posttimeToFirstVisibleExpansionMs = old.posttimeToFirstVisibleExpansionMs;
            this.currentAirtimeExpandedStartElapsedMs = old.currentAirtimeExpandedStartElapsedMs;
            this.airtimeExpandedMs = old.airtimeExpandedMs;
            this.userExpansionCount = old.userExpansionCount;
        }
    }

    public static class Aggregate {
        double avg;
        long numSamples;
        double sum2;
        double var;

        public void addSample(long sample) {
            this.numSamples++;
            long j = this.numSamples;
            double n = (double) j;
            double d = this.avg;
            double delta = ((double) sample) - d;
            double divisor = 1.0d;
            this.avg = d + ((1.0d / n) * delta);
            this.sum2 += ((n - 1.0d) / n) * delta * delta;
            if (j != 1) {
                divisor = n - 1.0d;
            }
            this.var = this.sum2 / divisor;
        }

        public String toString() {
            return "Aggregate{numSamples=" + this.numSamples + ", avg=" + this.avg + ", var=" + this.var + '}';
        }
    }

    /* access modifiers changed from: private */
    public static class SQLiteLog {
        private static final String COL_ACTION_COUNT = "action_count";
        private static final String COL_AIRTIME_EXPANDED_MS = "expansion_airtime_ms";
        private static final String COL_AIRTIME_MS = "airtime_ms";
        private static final String COL_CATEGORY = "category";
        private static final String COL_DEFAULTS = "defaults";
        private static final String COL_DEMOTED = "demoted";
        private static final String COL_EVENT_TIME = "event_time_ms";
        private static final String COL_EVENT_TYPE = "event_type";
        private static final String COL_EVENT_USER_ID = "event_user_id";
        private static final String COL_EXPAND_COUNT = "expansion_count";
        private static final String COL_FIRST_EXPANSIONTIME_MS = "first_expansion_time_ms";
        private static final String COL_FLAGS = "flags";
        private static final String COL_IMPORTANCE_FINAL = "importance_final";
        private static final String COL_IMPORTANCE_REQ = "importance_request";
        private static final String COL_KEY = "key";
        private static final String COL_MUTED = "muted";
        private static final String COL_NOISY = "noisy";
        private static final String COL_NOTIFICATION_ID = "nid";
        private static final String COL_PKG = "pkg";
        private static final String COL_POSTTIME_MS = "posttime_ms";
        private static final String COL_TAG = "tag";
        private static final String COL_WHEN_MS = "when_ms";
        private static final long DAY_MS = 86400000;
        private static final String DB_NAME = "notification_log.db";
        private static final int DB_VERSION = 5;
        private static final int EVENT_TYPE_CLICK = 2;
        private static final int EVENT_TYPE_DISMISS = 4;
        private static final int EVENT_TYPE_POST = 1;
        private static final int EVENT_TYPE_REMOVE = 3;
        private static final long HORIZON_MS = 604800000;
        private static final int IDLE_CONNECTION_TIMEOUT_MS = 30000;
        private static final int MSG_CLICK = 2;
        private static final int MSG_DISMISS = 4;
        private static final int MSG_POST = 1;
        private static final int MSG_REMOVE = 3;
        private static final long PRUNE_MIN_DELAY_MS = 21600000;
        private static final long PRUNE_MIN_WRITES = 1024;
        private static final String STATS_QUERY = "SELECT event_user_id, pkg, CAST(((%d - event_time_ms) / 86400000) AS int) AS day, COUNT(*) AS cnt, SUM(muted) as muted, SUM(noisy) as noisy, SUM(demoted) as demoted FROM log WHERE event_type=1 AND event_time_ms > %d  GROUP BY event_user_id, day, pkg";
        private static final String TAB_LOG = "log";
        private static final String TAG = "NotificationSQLiteLog";
        private static long sLastPruneMs;
        private static long sNumWrites;
        private final SQLiteOpenHelper mHelper;
        private final Handler mWriteHandler;

        public SQLiteLog(Context context) {
            HandlerThread backgroundThread = new HandlerThread("notification-sqlite-log", 10);
            backgroundThread.start();
            this.mWriteHandler = new Handler(backgroundThread.getLooper()) {
                /* class com.android.server.notification.NotificationUsageStats.SQLiteLog.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    NotificationRecord r = (NotificationRecord) msg.obj;
                    long nowMs = System.currentTimeMillis();
                    int i = msg.what;
                    if (i == 1) {
                        SQLiteLog.this.writeEvent(r.sbn.getPostTime(), 1, r);
                    } else if (i == 2) {
                        SQLiteLog.this.writeEvent(nowMs, 2, r);
                    } else if (i == 3) {
                        SQLiteLog.this.writeEvent(nowMs, 3, r);
                    } else if (i != 4) {
                        Log.wtf(SQLiteLog.TAG, "Unknown message type: " + msg.what);
                    } else {
                        SQLiteLog.this.writeEvent(nowMs, 4, r);
                    }
                }
            };
            this.mHelper = new SQLiteOpenHelper(context, DB_NAME, null, 5) {
                /* class com.android.server.notification.NotificationUsageStats.SQLiteLog.AnonymousClass2 */

                @Override // android.database.sqlite.SQLiteOpenHelper
                public void onCreate(SQLiteDatabase db) {
                    db.execSQL("CREATE TABLE log (_id INTEGER PRIMARY KEY AUTOINCREMENT,event_user_id INT,event_type INT,event_time_ms INT,key TEXT,pkg TEXT,nid INT,tag TEXT,when_ms INT,defaults INT,flags INT,importance_request INT,importance_final INT,noisy INT,muted INT,demoted INT,category TEXT,action_count INT,posttime_ms INT,airtime_ms INT,first_expansion_time_ms INT,expansion_airtime_ms INT,expansion_count INT)");
                }

                @Override // android.database.sqlite.SQLiteOpenHelper
                public void onConfigure(SQLiteDatabase db) {
                    setIdleConnectionTimeout(30000);
                }

                @Override // android.database.sqlite.SQLiteOpenHelper
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    if (oldVersion != newVersion) {
                        db.execSQL("DROP TABLE IF EXISTS log");
                        onCreate(db);
                    }
                }
            };
        }

        public void logPosted(NotificationRecord notification) {
            Handler handler = this.mWriteHandler;
            handler.sendMessage(handler.obtainMessage(1, notification));
        }

        public void logClicked(NotificationRecord notification) {
            Handler handler = this.mWriteHandler;
            handler.sendMessage(handler.obtainMessage(2, notification));
        }

        public void logRemoved(NotificationRecord notification) {
            Handler handler = this.mWriteHandler;
            handler.sendMessage(handler.obtainMessage(3, notification));
        }

        public void logDismissed(NotificationRecord notification) {
            Handler handler = this.mWriteHandler;
            handler.sendMessage(handler.obtainMessage(4, notification));
        }

        private JSONArray jsonPostFrequencies(NotificationManagerService.DumpFilter filter) throws JSONException {
            NotificationManagerService.DumpFilter dumpFilter = filter;
            JSONArray frequencies = new JSONArray();
            SQLiteDatabase db = this.mHelper.getReadableDatabase();
            long midnight = getMidnightMs();
            if (dumpFilter == null) {
                return frequencies;
            }
            int i = 2;
            int i2 = 0;
            int i3 = 1;
            Cursor cursor = db.rawQuery(String.format(STATS_QUERY, Long.valueOf(midnight), Long.valueOf(dumpFilter.since)), null);
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int userId = cursor.getInt(i2);
                    String pkg = cursor.getString(i3);
                    if (dumpFilter.matches(pkg)) {
                        int day = cursor.getInt(i);
                        int count = cursor.getInt(3);
                        int muted = cursor.getInt(4);
                        int noisy = cursor.getInt(5);
                        int demoted = cursor.getInt(6);
                        JSONObject row = new JSONObject();
                        row.put("user_id", userId);
                        row.put("package", pkg);
                        row.put("day", day);
                        row.put(AssistDataRequester.KEY_RECEIVER_EXTRA_COUNT, count);
                        row.put(COL_NOISY, noisy);
                        row.put(COL_MUTED, muted);
                        row.put(COL_DEMOTED, demoted);
                        frequencies.put(row);
                    }
                    cursor.moveToNext();
                    dumpFilter = filter;
                    i = 2;
                    i2 = 0;
                    i3 = 1;
                }
                return frequencies;
            } finally {
                cursor.close();
            }
        }

        public void printPostFrequencies(PrintWriter pw, String indent, NotificationManagerService.DumpFilter filter) {
            Throwable th;
            NotificationManagerService.DumpFilter dumpFilter = filter;
            SQLiteDatabase db = this.mHelper.getReadableDatabase();
            long midnight = getMidnightMs();
            if (dumpFilter != null) {
                int i = 2;
                int i2 = 0;
                int i3 = 1;
                Cursor cursor = db.rawQuery(String.format(STATS_QUERY, Long.valueOf(midnight), Long.valueOf(dumpFilter.since)), null);
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        int userId = cursor.getInt(i2);
                        String pkg = cursor.getString(i3);
                        if (dumpFilter.matches(pkg)) {
                            int day = cursor.getInt(i);
                            int count = cursor.getInt(3);
                            int muted = cursor.getInt(4);
                            int noisy = cursor.getInt(5);
                            int demoted = cursor.getInt(6);
                            StringBuilder sb = new StringBuilder();
                            try {
                                sb.append(indent);
                                sb.append("post_frequency{user_id=");
                                sb.append(userId);
                                sb.append(",pkg=");
                                sb.append(pkg);
                                sb.append(",day=");
                                sb.append(day);
                                sb.append(",count=");
                                sb.append(count);
                                sb.append(",muted=");
                                sb.append(muted);
                                sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                                sb.append(noisy);
                                sb.append(",demoted=");
                                sb.append(demoted);
                                sb.append("}");
                                try {
                                    pw.println(sb.toString());
                                } catch (Throwable th2) {
                                    th = th2;
                                    cursor.close();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                cursor.close();
                                throw th;
                            }
                        }
                        cursor.moveToNext();
                        dumpFilter = filter;
                        i = 2;
                        i2 = 0;
                        i3 = 1;
                    }
                    cursor.close();
                } catch (Throwable th4) {
                    th = th4;
                    cursor.close();
                    throw th;
                }
            }
        }

        private long getMidnightMs() {
            GregorianCalendar midnight = new GregorianCalendar();
            midnight.set(midnight.get(1), midnight.get(2), midnight.get(5), 23, 59, 59);
            return midnight.getTimeInMillis();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void writeEvent(long eventTimeMs, int eventType, NotificationRecord r) {
            ContentValues cv = new ContentValues();
            cv.put(COL_EVENT_USER_ID, Integer.valueOf(r.sbn.getUser().getIdentifier()));
            cv.put(COL_EVENT_TIME, Long.valueOf(eventTimeMs));
            cv.put(COL_EVENT_TYPE, Integer.valueOf(eventType));
            putNotificationIdentifiers(r, cv);
            if (eventType == 1) {
                putNotificationDetails(r, cv);
            } else {
                putPosttimeVisibility(r, cv);
            }
            try {
                SQLiteDatabase db = this.mHelper.getWritableDatabase();
                if (db.insert(TAB_LOG, null, cv) < 0) {
                    Log.wtf(TAG, "Error while trying to insert values: " + cv);
                }
                sNumWrites++;
                pruneIfNecessary(db);
            } catch (SQLiteException e) {
                Log.e(TAG, " pruneIfNecessary has SQLiteException");
            }
        }

        private void pruneIfNecessary(SQLiteDatabase db) {
            long nowMs = System.currentTimeMillis();
            if (sNumWrites > PRUNE_MIN_WRITES || nowMs - sLastPruneMs > PRUNE_MIN_DELAY_MS) {
                sNumWrites = 0;
                sLastPruneMs = nowMs;
                int deletedRows = db.delete(TAB_LOG, "event_time_ms < ?", new String[]{String.valueOf(nowMs - 604800000)});
                Log.d(TAG, "Pruned event entries: " + deletedRows);
            }
        }

        private static void putNotificationIdentifiers(NotificationRecord r, ContentValues outCv) {
            outCv.put(COL_KEY, r.sbn.getKey());
            outCv.put("pkg", r.sbn.getPackageName());
        }

        private static void putNotificationDetails(NotificationRecord r, ContentValues outCv) {
            outCv.put(COL_NOTIFICATION_ID, Integer.valueOf(r.sbn.getId()));
            if (r.sbn.getTag() != null) {
                outCv.put(COL_TAG, r.sbn.getTag());
            }
            outCv.put(COL_WHEN_MS, Long.valueOf(r.sbn.getPostTime()));
            outCv.put(COL_FLAGS, Integer.valueOf(r.getNotification().flags));
            int before = r.stats.requestedImportance;
            int after = r.getImportance();
            boolean noisy = r.stats.isNoisy;
            outCv.put(COL_IMPORTANCE_REQ, Integer.valueOf(before));
            outCv.put(COL_IMPORTANCE_FINAL, Integer.valueOf(after));
            int i = 0;
            outCv.put(COL_DEMOTED, Integer.valueOf(after < before ? 1 : 0));
            outCv.put(COL_NOISY, Boolean.valueOf(noisy));
            if (!noisy || after >= 4) {
                outCv.put(COL_MUTED, (Integer) 0);
            } else {
                outCv.put(COL_MUTED, (Integer) 1);
            }
            if (r.getNotification().category != null) {
                outCv.put(COL_CATEGORY, r.getNotification().category);
            }
            if (r.getNotification().actions != null) {
                i = r.getNotification().actions.length;
            }
            outCv.put(COL_ACTION_COUNT, Integer.valueOf(i));
        }

        private static void putPosttimeVisibility(NotificationRecord r, ContentValues outCv) {
            outCv.put(COL_POSTTIME_MS, Long.valueOf(r.stats.getCurrentPosttimeMs()));
            outCv.put(COL_AIRTIME_MS, Long.valueOf(r.stats.getCurrentAirtimeMs()));
            outCv.put(COL_EXPAND_COUNT, Long.valueOf(r.stats.userExpansionCount));
            outCv.put(COL_AIRTIME_EXPANDED_MS, Long.valueOf(r.stats.getCurrentAirtimeExpandedMs()));
            outCv.put(COL_FIRST_EXPANSIONTIME_MS, Long.valueOf(r.stats.posttimeToFirstVisibleExpansionMs));
        }

        public void dump(PrintWriter pw, String indent, NotificationManagerService.DumpFilter filter) {
            printPostFrequencies(pw, indent, filter);
        }

        public JSONObject dumpJson(NotificationManagerService.DumpFilter filter) {
            JSONObject dump = new JSONObject();
            try {
                dump.put("post_frequency", jsonPostFrequencies(filter));
                if (filter != null) {
                    dump.put("since", filter.since);
                }
                dump.put("now", System.currentTimeMillis());
            } catch (JSONException e) {
            }
            return dump;
        }
    }
}
