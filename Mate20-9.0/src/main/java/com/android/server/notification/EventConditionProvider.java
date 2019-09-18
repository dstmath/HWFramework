package com.android.server.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.notification.CalendarTracker;
import com.android.server.notification.NotificationManagerService;
import com.android.server.pm.PackageManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventConditionProvider extends SystemConditionProviderService {
    private static final String ACTION_EVALUATE = (SIMPLE_NAME + ".EVALUATE");
    private static final long CHANGE_DELAY = 2000;
    public static final ComponentName COMPONENT = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, EventConditionProvider.class.getName());
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    private static final String EXTRA_TIME = "time";
    private static final String NOT_SHOWN = "...";
    private static final int REQUEST_CODE_EVALUATE = 1;
    private static final String SIMPLE_NAME = EventConditionProvider.class.getSimpleName();
    private static final String TAG = "ConditionProviders.ECP";
    private boolean mBootComplete;
    private boolean mConnected;
    private final Context mContext = this;
    /* access modifiers changed from: private */
    public final Runnable mEvaluateSubscriptionsW = new Runnable() {
        public void run() {
            EventConditionProvider.this.evaluateSubscriptionsW();
        }
    };
    private long mNextAlarmTime;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (EventConditionProvider.DEBUG) {
                Slog.d(EventConditionProvider.TAG, "onReceive " + intent.getAction());
            }
            EventConditionProvider.this.evaluateSubscriptions();
        }
    };
    private boolean mRegistered;
    private final ArraySet<Uri> mSubscriptions = new ArraySet<>();
    private final HandlerThread mThread;
    private final CalendarTracker.Callback mTrackerCallback = new CalendarTracker.Callback() {
        public void onChanged() {
            if (EventConditionProvider.DEBUG) {
                Slog.d(EventConditionProvider.TAG, "mTrackerCallback.onChanged");
            }
            EventConditionProvider.this.mWorker.removeCallbacks(EventConditionProvider.this.mEvaluateSubscriptionsW);
            EventConditionProvider.this.mWorker.postDelayed(EventConditionProvider.this.mEvaluateSubscriptionsW, EventConditionProvider.CHANGE_DELAY);
        }
    };
    private final SparseArray<CalendarTracker> mTrackers = new SparseArray<>();
    /* access modifiers changed from: private */
    public final Handler mWorker;

    public EventConditionProvider() {
        if (DEBUG) {
            Slog.d(TAG, "new " + SIMPLE_NAME + "()");
        }
        this.mThread = new HandlerThread(TAG, 10);
        this.mThread.start();
        this.mWorker = new Handler(this.mThread.getLooper());
    }

    public ComponentName getComponent() {
        return COMPONENT;
    }

    public boolean isValidConditionId(Uri id) {
        return ZenModeConfig.isValidEventConditionId(id);
    }

    public void dump(PrintWriter pw, NotificationManagerService.DumpFilter filter) {
        pw.print("    ");
        pw.print(SIMPLE_NAME);
        pw.println(":");
        pw.print("      mConnected=");
        pw.println(this.mConnected);
        pw.print("      mRegistered=");
        pw.println(this.mRegistered);
        pw.print("      mBootComplete=");
        pw.println(this.mBootComplete);
        dumpUpcomingTime(pw, "mNextAlarmTime", this.mNextAlarmTime, System.currentTimeMillis());
        synchronized (this.mSubscriptions) {
            pw.println("      mSubscriptions=");
            Iterator<Uri> it = this.mSubscriptions.iterator();
            while (it.hasNext()) {
                pw.print("        ");
                pw.println(it.next());
            }
        }
        pw.println("      mTrackers=");
        for (int i = 0; i < this.mTrackers.size(); i++) {
            pw.print("        user=");
            pw.println(this.mTrackers.keyAt(i));
            this.mTrackers.valueAt(i).dump("          ", pw);
        }
    }

    public void onBootComplete() {
        if (DEBUG) {
            Slog.d(TAG, "onBootComplete");
        }
        if (!this.mBootComplete) {
            this.mBootComplete = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
            filter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    EventConditionProvider.this.reloadTrackers();
                }
            }, filter);
            reloadTrackers();
        }
    }

    public void onConnected() {
        if (DEBUG) {
            Slog.d(TAG, "onConnected");
        }
        this.mConnected = true;
    }

    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Slog.d(TAG, "onDestroy");
        }
        this.mConnected = false;
    }

    public void onSubscribe(Uri conditionId) {
        if (DEBUG) {
            Slog.d(TAG, "onSubscribe " + conditionId);
        }
        if (!ZenModeConfig.isValidEventConditionId(conditionId)) {
            notifyCondition(createCondition(conditionId, 0));
            return;
        }
        synchronized (this.mSubscriptions) {
            if (this.mSubscriptions.add(conditionId)) {
                evaluateSubscriptions();
            }
        }
    }

    public void onUnsubscribe(Uri conditionId) {
        if (DEBUG) {
            Slog.d(TAG, "onUnsubscribe " + conditionId);
        }
        synchronized (this.mSubscriptions) {
            if (this.mSubscriptions.remove(conditionId)) {
                evaluateSubscriptions();
            }
        }
    }

    public void attachBase(Context base) {
        attachBaseContext(base);
    }

    public IConditionProvider asInterface() {
        return onBind(null);
    }

    /* access modifiers changed from: private */
    public void reloadTrackers() {
        if (DEBUG) {
            Slog.d(TAG, "reloadTrackers");
        }
        for (int i = 0; i < this.mTrackers.size(); i++) {
            this.mTrackers.valueAt(i).setCallback(null);
        }
        this.mTrackers.clear();
        for (UserHandle user : UserManager.get(this.mContext).getUserProfiles()) {
            Context context = user.isSystem() ? this.mContext : getContextForUser(this.mContext, user);
            if (context == null) {
                Slog.w(TAG, "Unable to create context for user " + user.getIdentifier());
            } else {
                this.mTrackers.put(user.getIdentifier(), new CalendarTracker(this.mContext, context));
            }
        }
        evaluateSubscriptions();
    }

    /* access modifiers changed from: private */
    public void evaluateSubscriptions() {
        if (!this.mWorker.hasCallbacks(this.mEvaluateSubscriptionsW)) {
            this.mWorker.post(this.mEvaluateSubscriptionsW);
        }
    }

    /* access modifiers changed from: private */
    public void evaluateSubscriptionsW() {
        long reevaluateAt;
        Iterator<Uri> it;
        CalendarTracker.CheckEventResult result;
        boolean z;
        long reevaluateAt2;
        Iterator<Uri> it2;
        if (DEBUG) {
            Slog.d(TAG, "evaluateSubscriptions");
        }
        if (!this.mBootComplete) {
            if (DEBUG) {
                Slog.d(TAG, "Skipping evaluate before boot complete");
            }
            return;
        }
        long now = System.currentTimeMillis();
        List<Condition> conditionsToNotify = new ArrayList<>();
        synchronized (this.mSubscriptions) {
            int i = 0;
            for (int i2 = 0; i2 < this.mTrackers.size(); i2++) {
                this.mTrackers.valueAt(i2).setCallback(this.mSubscriptions.isEmpty() ? null : this.mTrackerCallback);
            }
            setRegistered(!this.mSubscriptions.isEmpty());
            long reevaluateAt3 = 0;
            Iterator<Uri> it3 = this.mSubscriptions.iterator();
            while (it3.hasNext()) {
                Uri conditionId = it3.next();
                ZenModeConfig.EventInfo event = ZenModeConfig.tryParseEventConditionId(conditionId);
                if (event == null) {
                    conditionsToNotify.add(createCondition(conditionId, i));
                    it = it3;
                    reevaluateAt = reevaluateAt3;
                } else {
                    if (event.calendar == null) {
                        result = null;
                        int i3 = i;
                        while (i3 < this.mTrackers.size()) {
                            CalendarTracker.CheckEventResult r = this.mTrackers.valueAt(i3).checkEvent(event, now);
                            if (result == null) {
                                result = r;
                                it2 = it3;
                                reevaluateAt2 = reevaluateAt3;
                            } else {
                                result.inEvent = r.inEvent | result.inEvent;
                                it2 = it3;
                                reevaluateAt2 = reevaluateAt3;
                                result.recheckAt = Math.min(result.recheckAt, r.recheckAt);
                            }
                            i3++;
                            it3 = it2;
                            reevaluateAt3 = reevaluateAt2;
                        }
                        it = it3;
                        reevaluateAt = reevaluateAt3;
                    } else {
                        it = it3;
                        reevaluateAt = reevaluateAt3;
                        CalendarTracker tracker = this.mTrackers.get(ZenModeConfig.EventInfo.resolveUserId(event.userId));
                        if (tracker == null) {
                            Slog.w(TAG, "No calendar tracker found for user " + userId);
                            conditionsToNotify.add(createCondition(conditionId, 0));
                        } else {
                            result = tracker.checkEvent(event, now);
                        }
                    }
                    if (result.recheckAt == 0 || (reevaluateAt != 0 && result.recheckAt >= reevaluateAt)) {
                        reevaluateAt3 = reevaluateAt;
                    } else {
                        reevaluateAt3 = result.recheckAt;
                    }
                    if (!result.inEvent) {
                        i = 0;
                        conditionsToNotify.add(createCondition(conditionId, 0));
                        z = true;
                    } else {
                        i = 0;
                        z = true;
                        conditionsToNotify.add(createCondition(conditionId, 1));
                    }
                    boolean z2 = z;
                    it3 = it;
                }
                it3 = it;
                reevaluateAt3 = reevaluateAt;
                i = 0;
            }
            long j = reevaluateAt3;
            rescheduleAlarm(now, reevaluateAt3);
        }
        notifyConditions((Condition[]) conditionsToNotify.toArray(new Condition[conditionsToNotify.size()]));
        if (DEBUG) {
            Slog.d(TAG, "evaluateSubscriptions took " + (System.currentTimeMillis() - now));
        }
    }

    private void rescheduleAlarm(long now, long time) {
        String str;
        this.mNextAlarmTime = time;
        AlarmManager alarms = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 1, new Intent(ACTION_EVALUATE).addFlags(268435456).putExtra(EXTRA_TIME, time), 134217728);
        alarms.cancel(pendingIntent);
        if (time == 0 || time < now) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Not scheduling evaluate: ");
                if (time == 0) {
                    str = "no time specified";
                } else {
                    str = "specified time in the past";
                }
                sb.append(str);
                Slog.d(TAG, sb.toString());
            }
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, String.format("Scheduling evaluate for %s, in %s, now=%s", new Object[]{ts(time), formatDuration(time - now), ts(now)}));
        }
        alarms.setExact(0, time, pendingIntent);
    }

    private Condition createCondition(Uri id, int state) {
        Condition condition = new Condition(id, NOT_SHOWN, NOT_SHOWN, NOT_SHOWN, 0, state, 2);
        return condition;
    }

    private void setRegistered(boolean registered) {
        if (this.mRegistered != registered) {
            if (DEBUG) {
                Slog.d(TAG, "setRegistered " + registered);
            }
            this.mRegistered = registered;
            if (this.mRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.TIME_SET");
                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                filter.addAction(ACTION_EVALUATE);
                filter.addAction("android.intent.action.USER_SWITCHED");
                registerReceiver(this.mReceiver, filter);
            } else {
                unregisterReceiver(this.mReceiver);
            }
        }
    }

    private static Context getContextForUser(Context context, UserHandle user) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
