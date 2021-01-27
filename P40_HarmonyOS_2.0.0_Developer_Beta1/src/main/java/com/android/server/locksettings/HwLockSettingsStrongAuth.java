package com.android.server.locksettings;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.locksettings.LockSettingsStrongAuth;
import com.huawei.android.os.SystemPropertiesEx;

public class HwLockSettingsStrongAuth extends LockSettingsStrongAuth {
    private static final boolean IS_SUPPORT_STRONG_AUTH_MODE_STRONG;
    private static final int MSG_SCHEDULE_WEAK_AUTH_TIMEOUT = 10;
    private static final int STRONG_AUTH_LEVEL_MIDDLE = 1;
    private static final int STRONG_AUTH_LEVEL_STRONG = 2;
    private static final int STRONG_AUTH_LEVEL_WEAK = 0;
    private static final int STRONG_AUTH_MODE_DEF = 0;
    private static final int STRONG_AUTH_MODE_STRONG = 1;
    private static final int STRONG_INTERACT_TIME_24_HOUR = 86400000;
    private static final int STRONG_INTERACT_TIME_4_HOUR = 14400000;
    private static final String WEAK_AUTH_TIMEOUT_ALARM_TAG = "LockSettingsWeakAuth.timeoutForUserHw";
    private static final int WEEK_INTERACT_TIME = 14400000;
    private final ArrayMap<Integer, Long> mSetAuthTimeForUser = new ArrayMap<>();
    private final ArrayMap<Integer, Integer> mStrongAuthLevelForUser = new ArrayMap<>();

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("hw_mc.keyguard.strong_auth_mode", 0) == 1) {
            z = true;
        }
        IS_SUPPORT_STRONG_AUTH_MODE_STRONG = z;
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

    /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: android.app.AlarmManager */
    /* JADX DEBUG: Multi-variable search result rejected for r4v4, resolved type: android.app.AlarmManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v3, types: [com.android.server.locksettings.HwLockSettingsStrongAuth$HwStrongAuthTimeoutAlarmListener, android.app.AlarmManager$OnAlarmListener] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void handleScheduleWeakAuthTimeout(int userId) {
        long when = SystemClock.elapsedRealtime() + 14400000;
        ?? r2 = (HwStrongAuthTimeoutAlarmListener) this.mStrongAuthTimeoutAlarmListenerForUser.get(Integer.valueOf(userId));
        if (r2 == 0) {
            Slog.w("LockSettings", "WeakAuth and no alarm exits");
        } else if (((HwStrongAuthTimeoutAlarmListener) r2).mAlarmTriggerTime >= when) {
            Slog.v("LockSettings", "WeakAuth skiped.");
        } else {
            Slog.w("LockSettings", "WeakAuth update alarm to " + when);
            this.mAlarmManager.cancel((AlarmManager.OnAlarmListener) r2);
            r2.setTrigerTime(false, when);
            this.mAlarmManager.set(3, when, WEAK_AUTH_TIMEOUT_ALARM_TAG, r2, this.mHandler);
        }
    }

    /* access modifiers changed from: protected */
    public long getStrongAuthTrigerTime(int userId) {
        int level = this.mStrongAuthLevelForUser.getOrDefault(Integer.valueOf(userId), 0).intValue();
        if (level == 0) {
            return HwLockSettingsStrongAuth.super.getStrongAuthTrigerTime(userId);
        }
        long currTime = SystemClock.elapsedRealtime();
        long updateTime = this.mSetAuthTimeForUser.getOrDefault(Integer.valueOf(userId), 0L).longValue();
        if (level == 1) {
            if (currTime - updateTime <= Constant.MILLISEC_ONE_DAY) {
                return Constant.MILLISEC_ONE_DAY + currTime;
            }
            this.mStrongAuthLevelForUser.put(Integer.valueOf(userId), 0);
            return HwLockSettingsStrongAuth.super.getStrongAuthTrigerTime(userId);
        } else if (level != 2) {
            return HwLockSettingsStrongAuth.super.getStrongAuthTrigerTime(userId);
        } else {
            if (currTime - updateTime <= 14400000) {
                return 14400000 + currTime;
            }
            this.mStrongAuthLevelForUser.put(Integer.valueOf(userId), 1);
            this.mSetAuthTimeForUser.put(Integer.valueOf(userId), Long.valueOf(currTime));
            return Constant.MILLISEC_ONE_DAY + currTime;
        }
    }

    public void updateSetCredentialTime(int userId) {
        if (IS_SUPPORT_STRONG_AUTH_MODE_STRONG) {
            Slog.i("LockSettings", "update set credential for " + userId + " at " + SystemClock.elapsedRealtime());
            this.mStrongAuthLevelForUser.put(Integer.valueOf(userId), 2);
            this.mSetAuthTimeForUser.put(Integer.valueOf(userId), Long.valueOf(SystemClock.elapsedRealtime()));
        }
    }

    /* access modifiers changed from: protected */
    public class HwStrongAuthTimeoutAlarmListener extends LockSettingsStrongAuth.StrongAuthTimeoutAlarmListener {
        private long mAlarmTriggerTime = 0;
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
