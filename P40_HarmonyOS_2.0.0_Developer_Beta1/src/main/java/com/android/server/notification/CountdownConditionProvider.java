package com.android.server.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.notification.NotificationManagerService;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import java.io.PrintWriter;

public class CountdownConditionProvider extends SystemConditionProviderService {
    private static final String ACTION = CountdownConditionProvider.class.getName();
    public static final ComponentName COMPONENT = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, CountdownConditionProvider.class.getName());
    private static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    private static final String EXTRA_CONDITION_ID = "condition_id";
    private static final int REQUEST_CODE = 100;
    private static final String TAG = "ConditionProviders.CCP";
    private boolean mConnected;
    private final Context mContext = this;
    private boolean mIsAlarm;
    private final Receiver mReceiver = new Receiver();
    private long mTime;

    public CountdownConditionProvider() {
        if (DEBUG) {
            Slog.d(TAG, "new CountdownConditionProvider()");
        }
    }

    @Override // com.android.server.notification.SystemConditionProviderService
    public ComponentName getComponent() {
        return COMPONENT;
    }

    @Override // com.android.server.notification.SystemConditionProviderService
    public boolean isValidConditionId(Uri id) {
        return ZenModeConfig.isValidCountdownConditionId(id);
    }

    @Override // com.android.server.notification.SystemConditionProviderService
    public void attachBase(Context base) {
        attachBaseContext(base);
    }

    @Override // com.android.server.notification.SystemConditionProviderService
    public void onBootComplete() {
    }

    @Override // com.android.server.notification.SystemConditionProviderService
    public IConditionProvider asInterface() {
        return onBind(null);
    }

    @Override // com.android.server.notification.SystemConditionProviderService
    public void dump(PrintWriter pw, NotificationManagerService.DumpFilter filter) {
        pw.println("    CountdownConditionProvider:");
        pw.print("      mConnected=");
        pw.println(this.mConnected);
        pw.print("      mTime=");
        pw.println(this.mTime);
    }

    @Override // android.service.notification.ConditionProviderService
    public void onConnected() {
        if (DEBUG) {
            Slog.d(TAG, "onConnected");
        }
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(ACTION));
        this.mConnected = true;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Slog.d(TAG, "onDestroy");
        }
        if (this.mConnected) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        this.mConnected = false;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x008d: APUT  (r5v5 java.lang.Object[]), (0 ??[int, short, byte, char]), (r6v3 java.lang.String) */
    @Override // android.service.notification.ConditionProviderService
    public void onSubscribe(Uri conditionId) {
        if (DEBUG) {
            Slog.d(TAG, "onSubscribe " + conditionId);
        }
        this.mTime = ZenModeConfig.tryParseCountdownConditionId(conditionId);
        this.mIsAlarm = ZenModeConfig.isValidCountdownToAlarmConditionId(conditionId);
        AlarmManager alarms = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 100, new Intent(ACTION).putExtra(EXTRA_CONDITION_ID, conditionId).setFlags(1073741824), DumpState.DUMP_HWFEATURES);
        alarms.cancel(pendingIntent);
        if (this.mTime > 0) {
            long now = System.currentTimeMillis();
            CharSequence span = DateUtils.getRelativeTimeSpanString(this.mTime, now, 60000);
            long j = this.mTime;
            if (j <= now) {
                notifyCondition(newCondition(j, this.mIsAlarm, 0));
            } else {
                alarms.setExact(0, j, pendingIntent);
            }
            if (DEBUG) {
                Object[] objArr = new Object[6];
                objArr[0] = this.mTime <= now ? "Not scheduling" : "Scheduling";
                objArr[1] = ACTION;
                objArr[2] = ts(this.mTime);
                objArr[3] = Long.valueOf(this.mTime - now);
                objArr[4] = span;
                objArr[5] = ts(now);
                Slog.d(TAG, String.format("%s %s for %s, %s in the future (%s), now=%s", objArr));
            }
        }
    }

    @Override // android.service.notification.ConditionProviderService
    public void onUnsubscribe(Uri conditionId) {
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (CountdownConditionProvider.ACTION.equals(intent.getAction())) {
                Uri conditionId = (Uri) intent.getParcelableExtra(CountdownConditionProvider.EXTRA_CONDITION_ID);
                boolean alarm = ZenModeConfig.isValidCountdownToAlarmConditionId(conditionId);
                long time = ZenModeConfig.tryParseCountdownConditionId(conditionId);
                if (CountdownConditionProvider.DEBUG) {
                    Slog.d(CountdownConditionProvider.TAG, "Countdown condition fired: " + conditionId);
                }
                if (time > 0) {
                    CountdownConditionProvider.this.notifyCondition(CountdownConditionProvider.newCondition(time, alarm, 0));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final Condition newCondition(long time, boolean alarm, int state) {
        return new Condition(ZenModeConfig.toCountdownConditionId(time, alarm), "", "", "", 0, state, 1);
    }

    public static String tryParseDescription(Uri conditionUri) {
        long time = ZenModeConfig.tryParseCountdownConditionId(conditionUri);
        if (time == 0) {
            return null;
        }
        long now = System.currentTimeMillis();
        return String.format("Scheduled for %s, %s in the future (%s), now=%s", ts(time), Long.valueOf(time - now), DateUtils.getRelativeTimeSpanString(time, now, 60000), ts(now));
    }
}
