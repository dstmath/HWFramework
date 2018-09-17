package com.android.server.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri.Builder;
import android.os.Binder;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.notification.ManagedServices.UserProfiles;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SnoozeHelper {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String EXTRA_KEY = "key";
    private static final String EXTRA_USER_ID = "userId";
    private static final String INDENT = "    ";
    private static final String REPOST_ACTION = (SnoozeHelper.class.getSimpleName() + ".EVALUATE");
    private static final String REPOST_SCHEME = "repost";
    private static final int REQUEST_CODE_REPOST = 1;
    private static final String TAG = "SnoozeHelper";
    private AlarmManager mAm;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SnoozeHelper.DEBUG) {
                Slog.d(SnoozeHelper.TAG, "Reposting notification");
            }
            if (SnoozeHelper.REPOST_ACTION.equals(intent.getAction())) {
                SnoozeHelper.this.repost(intent.getStringExtra(SnoozeHelper.EXTRA_KEY), intent.getIntExtra(SnoozeHelper.EXTRA_USER_ID, 0));
            }
        }
    };
    private Callback mCallback;
    private final Context mContext;
    private ArrayMap<String, String> mPackages = new ArrayMap();
    private ArrayMap<Integer, ArrayMap<String, ArrayMap<String, NotificationRecord>>> mSnoozedNotifications = new ArrayMap();
    private final UserProfiles mUserProfiles;
    private ArrayMap<String, Integer> mUsers = new ArrayMap();

    protected interface Callback {
        void repost(int i, NotificationRecord notificationRecord);
    }

    public SnoozeHelper(Context context, Callback callback, UserProfiles userProfiles) {
        this.mContext = context;
        IntentFilter filter = new IntentFilter(REPOST_ACTION);
        filter.addDataScheme(REPOST_SCHEME);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mAm = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mCallback = callback;
        this.mUserProfiles = userProfiles;
    }

    protected boolean isSnoozed(int userId, String pkg, String key) {
        if (this.mSnoozedNotifications.containsKey(Integer.valueOf(userId)) && ((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).containsKey(pkg)) {
            return ((ArrayMap) ((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).get(pkg)).containsKey(key);
        }
        return false;
    }

    protected Collection<NotificationRecord> getSnoozed(int userId, String pkg) {
        if (this.mSnoozedNotifications.containsKey(Integer.valueOf(userId)) && ((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).containsKey(pkg)) {
            return ((ArrayMap) ((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).get(pkg)).values();
        }
        return Collections.EMPTY_LIST;
    }

    protected List<NotificationRecord> getSnoozed() {
        List<NotificationRecord> snoozedForUser = new ArrayList();
        int[] userIds = this.mUserProfiles.getCurrentProfileIds();
        if (userIds != null) {
            for (int valueOf : userIds) {
                ArrayMap<String, ArrayMap<String, NotificationRecord>> snoozedPkgs = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(valueOf));
                if (snoozedPkgs != null) {
                    int M = snoozedPkgs.size();
                    for (int j = 0; j < M; j++) {
                        ArrayMap<String, NotificationRecord> records = (ArrayMap) snoozedPkgs.valueAt(j);
                        if (records != null) {
                            snoozedForUser.addAll(records.values());
                        }
                    }
                }
            }
        }
        return snoozedForUser;
    }

    protected void snooze(NotificationRecord record, long duration) {
        snooze(record);
        scheduleRepost(record.sbn.getPackageName(), record.getKey(), record.getUserId(), duration);
    }

    protected void snooze(NotificationRecord record) {
        int userId = record.getUser().getIdentifier();
        if (DEBUG) {
            Slog.d(TAG, "Snoozing " + record.getKey());
        }
        ArrayMap<String, ArrayMap<String, NotificationRecord>> records = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId));
        if (records == null) {
            records = new ArrayMap();
        }
        ArrayMap<String, NotificationRecord> pkgRecords = (ArrayMap) records.get(record.sbn.getPackageName());
        if (pkgRecords == null) {
            pkgRecords = new ArrayMap();
        }
        pkgRecords.put(record.getKey(), record);
        records.put(record.sbn.getPackageName(), pkgRecords);
        this.mSnoozedNotifications.put(Integer.valueOf(userId), records);
        this.mPackages.put(record.getKey(), record.sbn.getPackageName());
        this.mUsers.put(record.getKey(), Integer.valueOf(userId));
    }

    protected boolean cancel(int userId, String pkg, String tag, int id) {
        if (this.mSnoozedNotifications.containsKey(Integer.valueOf(userId))) {
            ArrayMap<String, NotificationRecord> recordsForPkg = (ArrayMap) ((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).get(pkg);
            if (recordsForPkg != null) {
                for (Entry<String, NotificationRecord> record : recordsForPkg.entrySet()) {
                    StatusBarNotification sbn = ((NotificationRecord) record.getValue()).sbn;
                    if (Objects.equals(sbn.getTag(), tag) && sbn.getId() == id) {
                        ((NotificationRecord) record.getValue()).isCanceled = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean cancel(int userId, boolean includeCurrentProfiles) {
        int[] userIds = new int[]{userId};
        if (includeCurrentProfiles) {
            userIds = this.mUserProfiles.getCurrentProfileIds();
        }
        for (int valueOf : userIds) {
            ArrayMap<String, ArrayMap<String, NotificationRecord>> snoozedPkgs = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(valueOf));
            if (snoozedPkgs != null) {
                int M = snoozedPkgs.size();
                for (int j = 0; j < M; j++) {
                    ArrayMap<String, NotificationRecord> records = (ArrayMap) snoozedPkgs.valueAt(j);
                    if (records != null) {
                        int P = records.size();
                        for (int k = 0; k < P; k++) {
                            ((NotificationRecord) records.valueAt(k)).isCanceled = true;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected boolean cancel(int userId, String pkg) {
        if (!this.mSnoozedNotifications.containsKey(Integer.valueOf(userId)) || !((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).containsKey(pkg)) {
            return false;
        }
        ArrayMap<String, NotificationRecord> records = (ArrayMap) ((ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId))).get(pkg);
        int N = records.size();
        for (int i = 0; i < N; i++) {
            ((NotificationRecord) records.valueAt(i)).isCanceled = true;
        }
        return true;
    }

    protected void update(int userId, NotificationRecord record) {
        ArrayMap<String, ArrayMap<String, NotificationRecord>> records = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId));
        if (records != null) {
            ArrayMap<String, NotificationRecord> pkgRecords = (ArrayMap) records.get(record.sbn.getPackageName());
            if (pkgRecords != null) {
                NotificationRecord existing = (NotificationRecord) pkgRecords.get(record.getKey());
                if (existing == null || !existing.isCanceled) {
                    pkgRecords.put(record.getKey(), record);
                }
            }
        }
    }

    protected void repost(String key) {
        Integer userId = (Integer) this.mUsers.get(key);
        if (userId != null) {
            repost(key, userId.intValue());
        }
    }

    protected void repost(String key, int userId) {
        String pkg = (String) this.mPackages.remove(key);
        ArrayMap<String, ArrayMap<String, NotificationRecord>> records = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId));
        if (records != null) {
            ArrayMap<String, NotificationRecord> pkgRecords = (ArrayMap) records.get(pkg);
            if (pkgRecords != null) {
                NotificationRecord record = (NotificationRecord) pkgRecords.remove(key);
                this.mPackages.remove(key);
                this.mUsers.remove(key);
                if (!(record == null || (record.isCanceled ^ 1) == 0)) {
                    MetricsLogger.action(record.getLogMaker().setCategory(831).setType(1));
                    this.mCallback.repost(userId, record);
                }
            }
        }
    }

    protected void repostGroupSummary(String pkg, int userId, String groupKey) {
        if (this.mSnoozedNotifications.containsKey(Integer.valueOf(userId))) {
            ArrayMap<String, ArrayMap<String, NotificationRecord>> keysByPackage = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId));
            if (keysByPackage != null && keysByPackage.containsKey(pkg)) {
                ArrayMap<String, NotificationRecord> recordsByKey = (ArrayMap) keysByPackage.get(pkg);
                if (recordsByKey != null) {
                    Object groupSummaryKey = null;
                    int N = recordsByKey.size();
                    for (int i = 0; i < N; i++) {
                        NotificationRecord potentialGroupSummary = (NotificationRecord) recordsByKey.valueAt(i);
                        if (potentialGroupSummary.sbn.isGroup() && potentialGroupSummary.getNotification().isGroupSummary() && groupKey.equals(potentialGroupSummary.getGroupKey())) {
                            groupSummaryKey = potentialGroupSummary.getKey();
                            break;
                        }
                    }
                    if (groupSummaryKey != null) {
                        NotificationRecord record = (NotificationRecord) recordsByKey.remove(groupSummaryKey);
                        this.mPackages.remove(groupSummaryKey);
                        this.mUsers.remove(groupSummaryKey);
                        if (record != null && (record.isCanceled ^ 1) != 0) {
                            MetricsLogger.action(record.getLogMaker().setCategory(831).setType(1));
                            this.mCallback.repost(userId, record);
                        }
                    }
                }
            }
        }
    }

    private PendingIntent createPendingIntent(String pkg, String key, int userId) {
        return PendingIntent.getBroadcast(this.mContext, 1, new Intent(REPOST_ACTION).setData(new Builder().scheme(REPOST_SCHEME).appendPath(key).build()).addFlags(268435456).putExtra(EXTRA_KEY, key).putExtra(EXTRA_USER_ID, userId), 134217728);
    }

    private void scheduleRepost(String pkg, String key, int userId, long duration) {
        long identity = Binder.clearCallingIdentity();
        try {
            PendingIntent pi = createPendingIntent(pkg, key, userId);
            this.mAm.cancel(pi);
            long time = SystemClock.elapsedRealtime() + duration;
            if (DEBUG) {
                Slog.d(TAG, "Scheduling evaluate for " + new Date(time));
            }
            this.mAm.setExactAndAllowWhileIdle(2, time, pi);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void dump(PrintWriter pw, DumpFilter filter) {
        pw.println("\n  Snoozed notifications:");
        for (Integer intValue : this.mSnoozedNotifications.keySet()) {
            int userId = intValue.intValue();
            pw.print(INDENT);
            pw.println("user: " + userId);
            ArrayMap<String, ArrayMap<String, NotificationRecord>> snoozedPkgs = (ArrayMap) this.mSnoozedNotifications.get(Integer.valueOf(userId));
            for (String pkg : snoozedPkgs.keySet()) {
                pw.print(INDENT);
                pw.print(INDENT);
                pw.println("package: " + pkg);
                for (String key : ((ArrayMap) snoozedPkgs.get(pkg)).keySet()) {
                    pw.print(INDENT);
                    pw.print(INDENT);
                    pw.print(INDENT);
                    pw.println(key);
                }
            }
        }
    }

    protected void writeXml(XmlSerializer out, boolean forBackup) throws IOException {
    }

    public void readXml(XmlPullParser parser, boolean forRestore) throws XmlPullParserException, IOException {
    }

    void setAlarmManager(AlarmManager am) {
        this.mAm = am;
    }
}
