package com.android.server.notification;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationUsageStats {
    private static final boolean DEBUG = false;
    private static final String DEVICE_GLOBAL_STATS = "__global";
    private static final long EMIT_PERIOD = 14400000;
    private static final AggregatedStats[] EMPTY_AGGREGATED_STATS = null;
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
    private ArraySet<String> mStatExpiredkeys;
    private final Map<String, AggregatedStats> mStats;
    private final ArrayDeque<AggregatedStats[]> mStatsArrays;

    /* renamed from: com.android.server.notification.NotificationUsageStats.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NotificationUsageStats.MSG_EMIT /*1*/:
                    NotificationUsageStats.this.emit();
                default:
                    Log.wtf(NotificationUsageStats.TAG, "Unknown message type: " + msg.what);
            }
        }
    }

    public static class Aggregate {
        double avg;
        long numSamples;
        double sum2;
        double var;

        public void addSample(long sample) {
            this.numSamples++;
            double n = (double) this.numSamples;
            double delta = ((double) sample) - this.avg;
            this.avg += (1.0d / n) * delta;
            this.sum2 += (((n - 1.0d) / n) * delta) * delta;
            this.var = this.sum2 / (this.numSamples == 1 ? 1.0d : n - 1.0d);
        }

        public String toString() {
            return "Aggregate{numSamples=" + this.numSamples + ", avg=" + this.avg + ", var=" + this.var + '}';
        }
    }

    private static class AggregatedStats {
        public RateEstimator enqueueRate;
        public ImportanceHistogram finalImportance;
        public final String key;
        private final Context mContext;
        private final long mCreated;
        public long mLastAccessTime;
        private AggregatedStats mPrevious;
        public ImportanceHistogram noisyImportance;
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

        private java.lang.String toStringWithIndent(java.lang.String r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.NotificationUsageStats.AggregatedStats.toStringWithIndent(java.lang.String):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.NotificationUsageStats.AggregatedStats.toStringWithIndent(java.lang.String):java.lang.String
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationUsageStats.AggregatedStats.toStringWithIndent(java.lang.String):java.lang.String");
        }

        public AggregatedStats(Context context, String key) {
            this.key = key;
            this.mContext = context;
            this.mCreated = SystemClock.elapsedRealtime();
            this.noisyImportance = new ImportanceHistogram(context, "note_imp_noisy_");
            this.quietImportance = new ImportanceHistogram(context, "note_imp_quiet_");
            this.finalImportance = new ImportanceHistogram(context, "note_importance_");
            this.enqueueRate = new RateEstimator();
        }

        public AggregatedStats getPrevious() {
            if (this.mPrevious == null) {
                this.mPrevious = new AggregatedStats(this.mContext, this.key);
            }
            return this.mPrevious;
        }

        public void countApiUse(NotificationRecord record) {
            Set<String> names;
            Notification n = record.getNotification();
            if (n.actions != null) {
                this.numWithActions += NotificationUsageStats.MSG_EMIT;
            }
            if ((n.flags & 64) != 0) {
                this.numForegroundService += NotificationUsageStats.MSG_EMIT;
            }
            if ((n.flags & 2) != 0) {
                this.numOngoing += NotificationUsageStats.MSG_EMIT;
            }
            if ((n.flags & 16) != 0) {
                this.numAutoCancel += NotificationUsageStats.MSG_EMIT;
            }
            if ((n.defaults & NotificationUsageStats.MSG_EMIT) == 0 && (n.defaults & 2) == 0 && n.sound == null) {
                if (n.vibrate != null) {
                }
                switch (n.visibility) {
                    case AppTransition.TRANSIT_UNSET /*-1*/:
                        this.numSecret += NotificationUsageStats.MSG_EMIT;
                        break;
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                        this.numPrivate += NotificationUsageStats.MSG_EMIT;
                        break;
                }
                if (record.stats.isNoisy) {
                    this.quietImportance.increment(record.stats.requestedImportance);
                } else {
                    this.noisyImportance.increment(record.stats.requestedImportance);
                }
                this.finalImportance.increment(record.getImportance());
                names = n.extras.keySet();
                if (names.contains("android.bigText")) {
                    this.numWithBigText += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.picture")) {
                    this.numWithBigPicture += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.largeIcon")) {
                    this.numWithLargeIcon += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.textLines")) {
                    this.numWithInbox += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.mediaSession")) {
                    this.numWithMediaSession += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.title") && !TextUtils.isEmpty(n.extras.getCharSequence("android.title"))) {
                    this.numWithTitle += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.text") && !TextUtils.isEmpty(n.extras.getCharSequence("android.text"))) {
                    this.numWithText += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.subText") && !TextUtils.isEmpty(n.extras.getCharSequence("android.subText"))) {
                    this.numWithSubText += NotificationUsageStats.MSG_EMIT;
                }
                if (names.contains("android.infoText") && !TextUtils.isEmpty(n.extras.getCharSequence("android.infoText"))) {
                    this.numWithInfoText += NotificationUsageStats.MSG_EMIT;
                    return;
                }
            }
            this.numInterrupt += NotificationUsageStats.MSG_EMIT;
            switch (n.visibility) {
                case AppTransition.TRANSIT_UNSET /*-1*/:
                    this.numSecret += NotificationUsageStats.MSG_EMIT;
                    break;
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    this.numPrivate += NotificationUsageStats.MSG_EMIT;
                    break;
            }
            if (record.stats.isNoisy) {
                this.quietImportance.increment(record.stats.requestedImportance);
            } else {
                this.noisyImportance.increment(record.stats.requestedImportance);
            }
            this.finalImportance.increment(record.getImportance());
            names = n.extras.keySet();
            if (names.contains("android.bigText")) {
                this.numWithBigText += NotificationUsageStats.MSG_EMIT;
            }
            if (names.contains("android.picture")) {
                this.numWithBigPicture += NotificationUsageStats.MSG_EMIT;
            }
            if (names.contains("android.largeIcon")) {
                this.numWithLargeIcon += NotificationUsageStats.MSG_EMIT;
            }
            if (names.contains("android.textLines")) {
                this.numWithInbox += NotificationUsageStats.MSG_EMIT;
            }
            if (names.contains("android.mediaSession")) {
                this.numWithMediaSession += NotificationUsageStats.MSG_EMIT;
            }
            this.numWithTitle += NotificationUsageStats.MSG_EMIT;
            this.numWithText += NotificationUsageStats.MSG_EMIT;
            this.numWithSubText += NotificationUsageStats.MSG_EMIT;
            if (names.contains("android.infoText")) {
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
            previous.numQuotaViolations = this.numQuotaViolations;
            this.noisyImportance.update(previous.noisyImportance);
            this.quietImportance.update(previous.quietImportance);
            this.finalImportance.update(previous.finalImportance);
        }

        void maybeCount(String name, int value) {
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

    private static class ImportanceHistogram {
        private static final String[] IMPORTANCE_NAMES = null;
        private static final int NUM_IMPORTANCES = 6;
        private final Context mContext;
        private int[] mCount;
        private final String[] mCounterNames;
        private final String mPrefix;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.NotificationUsageStats.ImportanceHistogram.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.NotificationUsageStats.ImportanceHistogram.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationUsageStats.ImportanceHistogram.<clinit>():void");
        }

        ImportanceHistogram(Context context, String prefix) {
            this.mContext = context;
            this.mCount = new int[NUM_IMPORTANCES];
            this.mCounterNames = new String[NUM_IMPORTANCES];
            this.mPrefix = prefix;
            for (int i = 0; i < NUM_IMPORTANCES; i += NotificationUsageStats.MSG_EMIT) {
                this.mCounterNames[i] = this.mPrefix + IMPORTANCE_NAMES[i];
            }
        }

        void increment(int imp) {
            if (imp < 0) {
                imp = 0;
            } else if (imp > NUM_IMPORTANCES) {
                imp = NUM_IMPORTANCES;
            }
            int[] iArr = this.mCount;
            iArr[imp] = iArr[imp] + NotificationUsageStats.MSG_EMIT;
        }

        void maybeCount(ImportanceHistogram prev) {
            for (int i = 0; i < NUM_IMPORTANCES; i += NotificationUsageStats.MSG_EMIT) {
                int value = this.mCount[i] - prev.mCount[i];
                if (value > 0) {
                    MetricsLogger.count(this.mContext, this.mCounterNames[i], value);
                }
            }
        }

        void update(ImportanceHistogram that) {
            for (int i = 0; i < NUM_IMPORTANCES; i += NotificationUsageStats.MSG_EMIT) {
                this.mCount[i] = that.mCount[i];
            }
        }

        public void maybePut(JSONObject dump, ImportanceHistogram prev) throws JSONException {
            dump.put(this.mPrefix, new JSONArray(this.mCount));
        }

        public String toString() {
            StringBuilder output = new StringBuilder();
            output.append(this.mPrefix).append(": [");
            for (int i = 0; i < NUM_IMPORTANCES; i += NotificationUsageStats.MSG_EMIT) {
                output.append(this.mCount[i]);
                if (i < 5) {
                    output.append(", ");
                }
            }
            output.append("]");
            return output.toString();
        }
    }

    private static class SQLiteLog {
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

        /* renamed from: com.android.server.notification.NotificationUsageStats.SQLiteLog.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ SQLiteLog this$1;

            AnonymousClass1(SQLiteLog this$1, Looper $anonymous0) {
                this.this$1 = this$1;
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                NotificationRecord r = msg.obj;
                long nowMs = System.currentTimeMillis();
                switch (msg.what) {
                    case SQLiteLog.MSG_POST /*1*/:
                        this.this$1.writeEvent(r.sbn.getPostTime(), SQLiteLog.MSG_POST, r);
                    case SQLiteLog.MSG_CLICK /*2*/:
                        this.this$1.writeEvent(nowMs, SQLiteLog.MSG_CLICK, r);
                    case SQLiteLog.MSG_REMOVE /*3*/:
                        this.this$1.writeEvent(nowMs, SQLiteLog.MSG_REMOVE, r);
                    case SQLiteLog.MSG_DISMISS /*4*/:
                        this.this$1.writeEvent(nowMs, SQLiteLog.MSG_DISMISS, r);
                    default:
                        Log.wtf(SQLiteLog.TAG, "Unknown message type: " + msg.what);
                }
            }
        }

        /* renamed from: com.android.server.notification.NotificationUsageStats.SQLiteLog.2 */
        class AnonymousClass2 extends SQLiteOpenHelper {
            final /* synthetic */ SQLiteLog this$1;

            AnonymousClass2(SQLiteLog this$1, Context $anonymous0, String $anonymous1, CursorFactory $anonymous2, int $anonymous3) {
                this.this$1 = this$1;
                super($anonymous0, $anonymous1, $anonymous2, $anonymous3);
            }

            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE log (_id INTEGER PRIMARY KEY AUTOINCREMENT,event_user_id INT,event_type INT,event_time_ms INT,key TEXT,pkg TEXT,nid INT,tag TEXT,when_ms INT,defaults INT,flags INT,importance_request INT,importance_final INT,noisy INT,muted INT,demoted INT,category TEXT,action_count INT,posttime_ms INT,airtime_ms INT,first_expansion_time_ms INT,expansion_airtime_ms INT,expansion_count INT)");
            }

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                if (oldVersion != newVersion) {
                    db.execSQL("DROP TABLE IF EXISTS log");
                    onCreate(db);
                }
            }
        }

        public SQLiteLog(Context context) {
            HandlerThread backgroundThread = new HandlerThread("notification-sqlite-log", 10);
            backgroundThread.start();
            this.mWriteHandler = new AnonymousClass1(this, backgroundThread.getLooper());
            this.mHelper = new AnonymousClass2(this, context, DB_NAME, null, DB_VERSION);
        }

        public void logPosted(NotificationRecord notification) {
            this.mWriteHandler.sendMessage(this.mWriteHandler.obtainMessage(MSG_POST, notification));
        }

        public void logClicked(NotificationRecord notification) {
            this.mWriteHandler.sendMessage(this.mWriteHandler.obtainMessage(MSG_CLICK, notification));
        }

        public void logRemoved(NotificationRecord notification) {
            this.mWriteHandler.sendMessage(this.mWriteHandler.obtainMessage(MSG_REMOVE, notification));
        }

        public void logDismissed(NotificationRecord notification) {
            this.mWriteHandler.sendMessage(this.mWriteHandler.obtainMessage(MSG_DISMISS, notification));
        }

        private JSONArray jsonPostFrequencies(DumpFilter filter) throws JSONException {
            JSONArray frequencies = new JSONArray();
            SQLiteDatabase db = this.mHelper.getReadableDatabase();
            long midnight = getMidnightMs();
            String str = STATS_QUERY;
            Object[] objArr = new Object[MSG_CLICK];
            objArr[0] = Long.valueOf(midnight);
            objArr[MSG_POST] = Long.valueOf(filter.since);
            Cursor cursor = db.rawQuery(String.format(str, objArr), null);
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int userId = cursor.getInt(0);
                    String pkg = cursor.getString(MSG_POST);
                    if (filter == null || filter.matches(pkg)) {
                        int day = cursor.getInt(MSG_CLICK);
                        int count = cursor.getInt(MSG_REMOVE);
                        int muted = cursor.getInt(MSG_DISMISS);
                        int noisy = cursor.getInt(DB_VERSION);
                        int demoted = cursor.getInt(6);
                        JSONObject row = new JSONObject();
                        row.put("user_id", userId);
                        row.put(HwBroadcastRadarUtil.KEY_PACKAGE, pkg);
                        row.put("day", day);
                        row.put("count", count);
                        row.put(COL_NOISY, noisy);
                        row.put(COL_MUTED, muted);
                        row.put(COL_DEMOTED, demoted);
                        frequencies.put(row);
                    }
                    cursor.moveToNext();
                }
                return frequencies;
            } finally {
                cursor.close();
            }
        }

        public void printPostFrequencies(PrintWriter pw, String indent, DumpFilter filter) {
            SQLiteDatabase db = this.mHelper.getReadableDatabase();
            long midnight = getMidnightMs();
            String str = STATS_QUERY;
            Object[] objArr = new Object[MSG_CLICK];
            objArr[0] = Long.valueOf(midnight);
            objArr[MSG_POST] = Long.valueOf(filter.since);
            Cursor cursor = db.rawQuery(String.format(str, objArr), null);
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int userId = cursor.getInt(0);
                    String pkg = cursor.getString(MSG_POST);
                    if (filter == null || filter.matches(pkg)) {
                        int day = cursor.getInt(MSG_CLICK);
                        int count = cursor.getInt(MSG_REMOVE);
                        int muted = cursor.getInt(MSG_DISMISS);
                        int noisy = cursor.getInt(DB_VERSION);
                        PrintWriter printWriter = pw;
                        printWriter.println(indent + "post_frequency{user_id=" + userId + ",pkg=" + pkg + ",day=" + day + ",count=" + count + ",muted=" + muted + "/" + noisy + ",demoted=" + cursor.getInt(6) + "}");
                    }
                    cursor.moveToNext();
                }
            } finally {
                cursor.close();
            }
        }

        private long getMidnightMs() {
            GregorianCalendar midnight = new GregorianCalendar();
            midnight.set(midnight.get(MSG_POST), midnight.get(MSG_CLICK), midnight.get(DB_VERSION), 23, 59, 59);
            return midnight.getTimeInMillis();
        }

        private void writeEvent(long eventTimeMs, int eventType, NotificationRecord r) {
            ContentValues cv = new ContentValues();
            cv.put(COL_EVENT_USER_ID, Integer.valueOf(r.sbn.getUser().getIdentifier()));
            cv.put(COL_EVENT_TIME, Long.valueOf(eventTimeMs));
            cv.put(COL_EVENT_TYPE, Integer.valueOf(eventType));
            putNotificationIdentifiers(r, cv);
            if (eventType == MSG_POST) {
                putNotificationDetails(r, cv);
            } else {
                putPosttimeVisibility(r, cv);
            }
            SQLiteDatabase db = this.mHelper.getWritableDatabase();
            if (db.insert(TAB_LOG, null, cv) < 0) {
                Log.wtf(TAG, "Error while trying to insert values: " + cv);
            }
            sNumWrites++;
            try {
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
                String[] strArr = new String[MSG_POST];
                strArr[0] = String.valueOf(nowMs - HORIZON_MS);
                Log.d(TAG, "Pruned event entries: " + db.delete(TAB_LOG, "event_time_ms < ?", strArr));
            }
        }

        private static void putNotificationIdentifiers(NotificationRecord r, ContentValues outCv) {
            outCv.put(COL_KEY, r.sbn.getKey());
            outCv.put(COL_PKG, r.sbn.getPackageName());
        }

        private static void putNotificationDetails(NotificationRecord r, ContentValues outCv) {
            int i = 0;
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
            outCv.put(COL_DEMOTED, Integer.valueOf(after < before ? MSG_POST : 0));
            outCv.put(COL_NOISY, Boolean.valueOf(noisy));
            if (!noisy || after >= MSG_DISMISS) {
                outCv.put(COL_MUTED, Integer.valueOf(0));
            } else {
                outCv.put(COL_MUTED, Integer.valueOf(MSG_POST));
            }
            if (r.getNotification().category != null) {
                outCv.put(COL_CATEGORY, r.getNotification().category);
            }
            String str = COL_ACTION_COUNT;
            if (r.getNotification().actions != null) {
                i = r.getNotification().actions.length;
            }
            outCv.put(str, Integer.valueOf(i));
        }

        private static void putPosttimeVisibility(NotificationRecord r, ContentValues outCv) {
            outCv.put(COL_POSTTIME_MS, Long.valueOf(r.stats.getCurrentPosttimeMs()));
            outCv.put(COL_AIRTIME_MS, Long.valueOf(r.stats.getCurrentAirtimeMs()));
            outCv.put(COL_EXPAND_COUNT, Long.valueOf(r.stats.userExpansionCount));
            outCv.put(COL_AIRTIME_EXPANDED_MS, Long.valueOf(r.stats.getCurrentAirtimeExpandedMs()));
            outCv.put(COL_FIRST_EXPANSIONTIME_MS, Long.valueOf(r.stats.posttimeToFirstVisibleExpansionMs));
        }

        public void dump(PrintWriter pw, String indent, DumpFilter filter) {
            printPostFrequencies(pw, indent, filter);
        }

        public JSONObject dumpJson(DumpFilter filter) {
            JSONObject dump = new JSONObject();
            try {
                dump.put("post_frequency", jsonPostFrequencies(filter));
                dump.put("since", filter.since);
                dump.put("now", System.currentTimeMillis());
            } catch (JSONException e) {
            }
            return dump;
        }
    }

    public static class SingleNotificationStats {
        public long airtimeCount;
        public long airtimeExpandedMs;
        public long airtimeMs;
        public long currentAirtimeExpandedStartElapsedMs;
        public long currentAirtimeStartElapsedMs;
        private boolean isExpanded;
        public boolean isNoisy;
        private boolean isVisible;
        public int naturalImportance;
        public long posttimeElapsedMs;
        public long posttimeToDismissMs;
        public long posttimeToFirstAirtimeMs;
        public long posttimeToFirstClickMs;
        public long posttimeToFirstVisibleExpansionMs;
        public int requestedImportance;
        public long userExpansionCount;

        public SingleNotificationStats() {
            this.isVisible = NotificationUsageStats.DEBUG;
            this.isExpanded = NotificationUsageStats.DEBUG;
            this.posttimeElapsedMs = -1;
            this.posttimeToFirstClickMs = -1;
            this.posttimeToDismissMs = -1;
            this.airtimeCount = 0;
            this.posttimeToFirstAirtimeMs = -1;
            this.currentAirtimeStartElapsedMs = -1;
            this.airtimeMs = 0;
            this.posttimeToFirstVisibleExpansionMs = -1;
            this.currentAirtimeExpandedStartElapsedMs = -1;
            this.airtimeExpandedMs = 0;
            this.userExpansionCount = 0;
        }

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
            } else if (this.currentAirtimeStartElapsedMs >= 0) {
                this.airtimeMs += elapsedNowMs - this.currentAirtimeStartElapsedMs;
                this.currentAirtimeStartElapsedMs = -1;
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

        private void updateVisiblyExpandedStats() {
            long elapsedNowMs = SystemClock.elapsedRealtime();
            if (this.isExpanded && this.isVisible) {
                if (this.currentAirtimeExpandedStartElapsedMs < 0) {
                    this.currentAirtimeExpandedStartElapsedMs = elapsedNowMs;
                }
                if (this.posttimeToFirstVisibleExpansionMs < 0) {
                    this.posttimeToFirstVisibleExpansionMs = elapsedNowMs - this.posttimeElapsedMs;
                }
            } else if (this.currentAirtimeExpandedStartElapsedMs >= 0) {
                this.airtimeExpandedMs += elapsedNowMs - this.currentAirtimeExpandedStartElapsedMs;
                this.currentAirtimeExpandedStartElapsedMs = -1;
            }
        }

        public void finish() {
            onVisibilityChanged(NotificationUsageStats.DEBUG);
        }

        public String toString() {
            StringBuilder output = new StringBuilder();
            output.append("SingleNotificationStats{");
            output.append("posttimeElapsedMs=").append(this.posttimeElapsedMs).append(", ");
            output.append("posttimeToFirstClickMs=").append(this.posttimeToFirstClickMs).append(", ");
            output.append("posttimeToDismissMs=").append(this.posttimeToDismissMs).append(", ");
            output.append("airtimeCount=").append(this.airtimeCount).append(", ");
            output.append("airtimeMs=").append(this.airtimeMs).append(", ");
            output.append("currentAirtimeStartElapsedMs=").append(this.currentAirtimeStartElapsedMs).append(", ");
            output.append("airtimeExpandedMs=").append(this.airtimeExpandedMs).append(", ");
            output.append("posttimeToFirstVisibleExpansionMs=").append(this.posttimeToFirstVisibleExpansionMs).append(", ");
            output.append("currentAirtimeExpandedStartElapsedMs=").append(this.currentAirtimeExpandedStartElapsedMs).append(", ");
            output.append("requestedImportance=").append(this.requestedImportance).append(", ");
            output.append("naturalImportance=").append(this.naturalImportance).append(", ");
            output.append("isNoisy=").append(this.isNoisy);
            output.append('}');
            return output.toString();
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.NotificationUsageStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.NotificationUsageStats.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationUsageStats.<clinit>():void");
    }

    public NotificationUsageStats(Context context) {
        this.mStats = new HashMap();
        this.mStatsArrays = new ArrayDeque();
        this.mStatExpiredkeys = new ArraySet();
        this.mContext = context;
        this.mLastEmitTime = SystemClock.elapsedRealtime();
        this.mSQLiteLog = new SQLiteLog(context);
        this.mHandler = new AnonymousClass1(this.mContext.getMainLooper());
        this.mHandler.sendEmptyMessageDelayed(MSG_EMIT, EMIT_PERIOD);
    }

    public synchronized float getAppEnqueueRate(String packageName) {
        AggregatedStats stats = getOrCreateAggregatedStatsLocked(packageName);
        if (stats == null) {
            return 0.0f;
        }
        return stats.getEnqueueRate(SystemClock.elapsedRealtime());
    }

    public synchronized void registerEnqueuedByApp(String packageName) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(packageName);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numEnqueuedByApp += MSG_EMIT;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerPostedByApp(NotificationRecord notification) {
        long now = SystemClock.elapsedRealtime();
        notification.stats.posttimeElapsedMs = now;
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numPostedByApp += MSG_EMIT;
            stats.updateInterarrivalEstimate(now);
            stats.countApiUse(notification);
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
        this.mSQLiteLog.logPosted(notification);
    }

    public synchronized void registerUpdatedByApp(NotificationRecord notification, NotificationRecord old) {
        notification.stats.updateFrom(old.stats);
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numUpdatedByApp += MSG_EMIT;
            stats.updateInterarrivalEstimate(SystemClock.elapsedRealtime());
            stats.countApiUse(notification);
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
        this.mSQLiteLog.logPosted(notification);
    }

    public synchronized void registerRemovedByApp(NotificationRecord notification) {
        notification.stats.onRemoved();
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numRemovedByApp += MSG_EMIT;
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
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            if (valid) {
                stats.numWithValidPeople += MSG_EMIT;
            }
            if (starred) {
                stats.numWithStaredPeople += MSG_EMIT;
            }
            if (cached) {
                stats.numPeopleCacheHit += MSG_EMIT;
            } else {
                stats.numPeopleCacheMiss += MSG_EMIT;
            }
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerBlocked(NotificationRecord notification) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numBlocked += MSG_EMIT;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerSuspendedByAdmin(NotificationRecord notification) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(notification);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numSuspendedByAdmin += MSG_EMIT;
        }
        releaseAggregatedStatsLocked(aggregatedStatsArray);
    }

    public synchronized void registerOverRateQuota(String packageName) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(packageName);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numRateViolations += MSG_EMIT;
        }
    }

    public synchronized void registerOverCountQuota(String packageName) {
        AggregatedStats[] aggregatedStatsArray = getAggregatedStatsLocked(packageName);
        int length = aggregatedStatsArray.length;
        for (int i = 0; i < length; i += MSG_EMIT) {
            AggregatedStats stats = aggregatedStatsArray[i];
            stats.numQuotaViolations += MSG_EMIT;
        }
    }

    private AggregatedStats[] getAggregatedStatsLocked(NotificationRecord record) {
        return getAggregatedStatsLocked(record.sbn.getPackageName());
    }

    private AggregatedStats[] getAggregatedStatsLocked(String packageName) {
        AggregatedStats[] array = (AggregatedStats[]) this.mStatsArrays.poll();
        if (array == null) {
            array = new AggregatedStats[2];
        }
        array[0] = getOrCreateAggregatedStatsLocked(DEVICE_GLOBAL_STATS);
        array[MSG_EMIT] = getOrCreateAggregatedStatsLocked(packageName);
        return array;
    }

    private void releaseAggregatedStatsLocked(AggregatedStats[] array) {
        for (int i = 0; i < array.length; i += MSG_EMIT) {
            array[i] = null;
        }
        this.mStatsArrays.offer(array);
    }

    private AggregatedStats getOrCreateAggregatedStatsLocked(String key) {
        AggregatedStats result = (AggregatedStats) this.mStats.get(key);
        if (result == null) {
            result = new AggregatedStats(this.mContext, key);
            this.mStats.put(key, result);
        }
        result.mLastAccessTime = SystemClock.elapsedRealtime();
        return result;
    }

    public synchronized JSONObject dumpJson(DumpFilter filter) {
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

    public synchronized void dump(PrintWriter pw, String indent, DumpFilter filter) {
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
        this.mHandler.removeMessages(MSG_EMIT);
        this.mHandler.sendEmptyMessageDelayed(MSG_EMIT, EMIT_PERIOD);
        for (String key : this.mStats.keySet()) {
            if (((AggregatedStats) this.mStats.get(key)).mLastAccessTime < this.mLastEmitTime) {
                this.mStatExpiredkeys.add(key);
            }
        }
        for (String key2 : this.mStatExpiredkeys) {
            this.mStats.remove(key2);
        }
        this.mStatExpiredkeys.clear();
        this.mLastEmitTime = SystemClock.elapsedRealtime();
    }
}
