package com.android.server.locksettings;

import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.locksettings.LockSettingsStrongAuth;

public class HwLockSettingsStrongAuth extends LockSettingsStrongAuth {
    private static final int MSG_SCHEDULE_WEAK_AUTH_TIMEOUT = 10;
    private static final String WEAK_AUTH_TIMEOUT_ALARM_TAG = "LockSettingsWeakAuth.timeoutForUserHw";
    private static final int mWeekInteractTime = 14400000;

    protected class HwStrongAuthTimeoutAlarmListener extends LockSettingsStrongAuth.StrongAuthTimeoutAlarmListener {
        /* access modifiers changed from: private */
        public long mAlarmTriggerTime = 0;
        private boolean mIsStrongAuth;

        public HwStrongAuthTimeoutAlarmListener(int userId) {
            super(HwLockSettingsStrongAuth.this, userId);
        }

        public void setTrigerTime(boolean isStrong, long alarmTime) {
            this.mIsStrongAuth = isStrong;
            this.mAlarmTriggerTime = alarmTime;
        }

        public void onAlarm() {
            Slog.w("LockSettings", "STRONG_AUTH_REQUIRED_AFTER_TIMEOUT with " + this.mIsStrongAuth);
            HwLockSettingsStrongAuth.super.onAlarm();
        }
    }

    public HwLockSettingsStrongAuth(Context context) {
        super(context);
    }

    public void reportSuccessfulWeakAuthUnlock(int userId) {
        Slog.w("LockSettings", "report WeakAuth from " + Binder.getCallingUid());
        this.mHandler.obtainMessage(10, userId, 0).sendToTarget();
    }

    public void reportSuccessfulStrongAuthUnlock(int userId) {
        Slog.w("LockSettings", "report StrongAuth from " + Binder.getCallingUid());
        HwLockSettingsStrongAuth.super.reportSuccessfulStrongAuthUnlock(userId);
    }

    /* JADX WARNING: type inference failed for: r2v3, types: [com.android.server.locksettings.HwLockSettingsStrongAuth$HwStrongAuthTimeoutAlarmListener, android.app.AlarmManager$OnAlarmListener] */
    /* access modifiers changed from: protected */
    public void handleScheduleWeakAuthTimeout(int userId) {
        long when = SystemClock.elapsedRealtime() + 14400000;
        ? r2 = (HwStrongAuthTimeoutAlarmListener) this.mStrongAuthTimeoutAlarmListenerForUser.get(Integer.valueOf(userId));
        if (r2 == 0) {
            Slog.w("LockSettings", "WeakAuth and no alarm exits");
        } else if (r2.mAlarmTriggerTime >= when) {
            Slog.v("LockSettings", "WeakAuth skiped.");
        } else {
            Slog.w("LockSettings", "WeakAuth update alarm to " + when);
            this.mAlarmManager.cancel(r2);
            r2.setTrigerTime(false, when);
            this.mAlarmManager.set(3, when, WEAK_AUTH_TIMEOUT_ALARM_TAG, r2, this.mHandler);
        }
    }

    /* access modifiers changed from: protected */
    public void handleExtendMessage(Message msg) {
        if (msg.what == 10) {
            handleScheduleWeakAuthTimeout(msg.arg1);
        }
    }

    /* access modifiers changed from: protected */
    public LockSettingsStrongAuth.StrongAuthTimeoutAlarmListener createAlarmListener(int userId) {
        return new HwStrongAuthTimeoutAlarmListener(userId);
    }
}
