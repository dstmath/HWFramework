package com.android.server.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.notification.CalendarTracker.Callback;
import com.android.server.notification.CalendarTracker.CheckEventResult;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class EventConditionProvider extends SystemConditionProviderService {
    private static final String ACTION_EVALUATE = (SIMPLE_NAME + ".EVALUATE");
    private static final long CHANGE_DELAY = 2000;
    public static final ComponentName COMPONENT = new ComponentName("android", EventConditionProvider.class.getName());
    private static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    private static final String EXTRA_TIME = "time";
    private static final String NOT_SHOWN = "...";
    private static final int REQUEST_CODE_EVALUATE = 1;
    private static final String SIMPLE_NAME = EventConditionProvider.class.getSimpleName();
    private static final String TAG = "ConditionProviders.ECP";
    private boolean mBootComplete;
    private boolean mConnected;
    private final Context mContext = this;
    private final Runnable mEvaluateSubscriptionsW = new Runnable() {
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
    private final ArraySet<Uri> mSubscriptions = new ArraySet();
    private final HandlerThread mThread;
    private final Callback mTrackerCallback = new Callback() {
        public void onChanged() {
            if (EventConditionProvider.DEBUG) {
                Slog.d(EventConditionProvider.TAG, "mTrackerCallback.onChanged");
            }
            EventConditionProvider.this.mWorker.removeCallbacks(EventConditionProvider.this.mEvaluateSubscriptionsW);
            EventConditionProvider.this.mWorker.postDelayed(EventConditionProvider.this.mEvaluateSubscriptionsW, EventConditionProvider.CHANGE_DELAY);
        }
    };
    private final SparseArray<CalendarTracker> mTrackers = new SparseArray();
    private final Handler mWorker;

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

    public void dump(PrintWriter pw, DumpFilter filter) {
        pw.print("    ");
        pw.print(SIMPLE_NAME);
        pw.println(":");
        pw.print("      mConnected=");
        pw.println(this.mConnected);
        pw.print("      mRegistered=");
        pw.println(this.mRegistered);
        pw.print("      mBootComplete=");
        pw.println(this.mBootComplete);
        SystemConditionProviderService.dumpUpcomingTime(pw, "mNextAlarmTime", this.mNextAlarmTime, System.currentTimeMillis());
        synchronized (this.mSubscriptions) {
            pw.println("      mSubscriptions=");
            for (Uri conditionId : this.mSubscriptions) {
                pw.print("        ");
                pw.println(conditionId);
            }
        }
        pw.println("      mTrackers=");
        for (int i = 0; i < this.mTrackers.size(); i++) {
            pw.print("        user=");
            pw.println(this.mTrackers.keyAt(i));
            ((CalendarTracker) this.mTrackers.valueAt(i)).dump("          ", pw);
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
        if (ZenModeConfig.isValidEventConditionId(conditionId)) {
            synchronized (this.mSubscriptions) {
                if (this.mSubscriptions.add(conditionId)) {
                    evaluateSubscriptions();
                }
            }
            return;
        }
        notifyCondition(createCondition(conditionId, 0));
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
        return (IConditionProvider) onBind(null);
    }

    private void reloadTrackers() {
        if (DEBUG) {
            Slog.d(TAG, "reloadTrackers");
        }
        for (int i = 0; i < this.mTrackers.size(); i++) {
            ((CalendarTracker) this.mTrackers.valueAt(i)).setCallback(null);
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

    private void evaluateSubscriptions() {
        if (!this.mWorker.hasCallbacks(this.mEvaluateSubscriptionsW)) {
            this.mWorker.post(this.mEvaluateSubscriptionsW);
        }
    }

    private void evaluateSubscriptionsW() {
        if (DEBUG) {
            Slog.d(TAG, "evaluateSubscriptions");
        }
        if (this.mBootComplete) {
            long now = System.currentTimeMillis();
            List<Condition> conditionsToNotify = new ArrayList();
            synchronized (this.mSubscriptions) {
                int i;
                for (i = 0; i < this.mTrackers.size(); i++) {
                    ((CalendarTracker) this.mTrackers.valueAt(i)).setCallback(this.mSubscriptions.isEmpty() ? null : this.mTrackerCallback);
                }
                setRegistered(this.mSubscriptions.isEmpty() ^ 1);
                long reevaluateAt = 0;
                for (Uri conditionId : this.mSubscriptions) {
                    EventInfo event = ZenModeConfig.tryParseEventConditionId(conditionId);
                    if (event == null) {
                        conditionsToNotify.add(createCondition(conditionId, 0));
                    } else {
                        CheckEventResult result = null;
                        if (event.calendar == null) {
                            for (i = 0; i < this.mTrackers.size(); i++) {
                                CheckEventResult r = ((CalendarTracker) this.mTrackers.valueAt(i)).checkEvent(event, now);
                                if (result == null) {
                                    result = r;
                                } else {
                                    result.inEvent |= r.inEvent;
                                    result.recheckAt = Math.min(result.recheckAt, r.recheckAt);
                                }
                            }
                        } else {
                            int userId = EventInfo.resolveUserId(event.userId);
                            CalendarTracker tracker = (CalendarTracker) this.mTrackers.get(userId);
                            if (tracker == null) {
                                Slog.w(TAG, "No calendar tracker found for user " + userId);
                                conditionsToNotify.add(createCondition(conditionId, 0));
                            } else {
                                result = tracker.checkEvent(event, now);
                            }
                        }
                        if (result.recheckAt != 0 && (reevaluateAt == 0 || result.recheckAt < reevaluateAt)) {
                            reevaluateAt = result.recheckAt;
                        }
                        if (result.inEvent) {
                            conditionsToNotify.add(createCondition(conditionId, 1));
                        } else {
                            conditionsToNotify.add(createCondition(conditionId, 0));
                        }
                    }
                }
                rescheduleAlarm(now, reevaluateAt);
            }
            notifyConditions((Condition[]) conditionsToNotify.toArray(new Condition[conditionsToNotify.size()]));
            if (DEBUG) {
                Slog.d(TAG, "evaluateSubscriptions took " + (System.currentTimeMillis() - now));
            }
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "Skipping evaluate before boot complete");
        }
    }

    private void rescheduleAlarm(long now, long time) {
        this.mNextAlarmTime = time;
        AlarmManager alarms = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 1, new Intent(ACTION_EVALUATE).addFlags(268435456).putExtra(EXTRA_TIME, time), 134217728);
        alarms.cancel(pendingIntent);
        if (time == 0 || time < now) {
            if (DEBUG) {
                String str;
                String str2 = TAG;
                StringBuilder append = new StringBuilder().append("Not scheduling evaluate: ");
                if (time == 0) {
                    str = "no time specified";
                } else {
                    str = "specified time in the past";
                }
                Slog.d(str2, append.append(str).toString());
            }
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, String.format("Scheduling evaluate for %s, in %s, now=%s", new Object[]{SystemConditionProviderService.ts(time), SystemConditionProviderService.formatDuration(time - now), SystemConditionProviderService.ts(now)}));
        }
        alarms.setExact(0, time, pendingIntent);
    }

    private Condition createCondition(Uri id, int state) {
        String summary = NOT_SHOWN;
        String line1 = NOT_SHOWN;
        String line2 = NOT_SHOWN;
        return new Condition(id, NOT_SHOWN, NOT_SHOWN, NOT_SHOWN, 0, state, 2);
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
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
