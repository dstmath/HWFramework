package com.android.server.locksettings;

import android.app.AlarmManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.IStrongAuthTracker;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseIntArray;
import com.android.internal.widget.LockPatternUtils;

public class LockSettingsStrongAuth {
    private static final int MSG_REGISTER_TRACKER = 2;
    private static final int MSG_REMOVE_USER = 4;
    private static final int MSG_REQUIRE_STRONG_AUTH = 1;
    private static final int MSG_SCHEDULE_STRONG_AUTH_TIMEOUT = 5;
    private static final int MSG_UNREGISTER_TRACKER = 3;
    private static final String STRONG_AUTH_TIMEOUT_ALARM_TAG = "LockSettingsStrongAuth.timeoutForUser";
    protected static final String TAG = "LockSettings";
    protected AlarmManager mAlarmManager;
    private final Context mContext;
    private final int mDefaultStrongAuthFlags;
    protected final Handler mHandler = new Handler() {
        /* class com.android.server.locksettings.LockSettingsStrongAuth.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                LockSettingsStrongAuth.this.handleRequireStrongAuth(msg.arg1, msg.arg2);
            } else if (i == 2) {
                LockSettingsStrongAuth.this.handleAddStrongAuthTracker((IStrongAuthTracker) msg.obj);
            } else if (i == 3) {
                LockSettingsStrongAuth.this.handleRemoveStrongAuthTracker((IStrongAuthTracker) msg.obj);
            } else if (i == 4) {
                LockSettingsStrongAuth.this.handleRemoveUser(msg.arg1);
            } else if (i != 5) {
                LockSettingsStrongAuth.this.handleExtendMessage(msg);
            } else {
                LockSettingsStrongAuth.this.handleScheduleStrongAuthTimeout(msg.arg1);
            }
        }
    };
    private final SparseIntArray mStrongAuthForUser = new SparseIntArray();
    protected final ArrayMap<Integer, StrongAuthTimeoutAlarmListener> mStrongAuthTimeoutAlarmListenerForUser = new ArrayMap<>();
    private final RemoteCallbackList<IStrongAuthTracker> mTrackers = new RemoteCallbackList<>();

    public LockSettingsStrongAuth(Context context) {
        this.mContext = context;
        this.mDefaultStrongAuthFlags = LockPatternUtils.StrongAuthTracker.getDefaultFlags(context);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAddStrongAuthTracker(IStrongAuthTracker tracker) {
        this.mTrackers.register(tracker);
        for (int i = 0; i < this.mStrongAuthForUser.size(); i++) {
            try {
                tracker.onStrongAuthRequiredChanged(this.mStrongAuthForUser.valueAt(i), this.mStrongAuthForUser.keyAt(i));
            } catch (RemoteException e) {
                Slog.e(TAG, "Exception while adding StrongAuthTracker.", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveStrongAuthTracker(IStrongAuthTracker tracker) {
        this.mTrackers.unregister(tracker);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRequireStrongAuth(int strongAuthReason, int userId) {
        if (userId == -1) {
            for (int i = 0; i < this.mStrongAuthForUser.size(); i++) {
                handleRequireStrongAuthOneUser(strongAuthReason, this.mStrongAuthForUser.keyAt(i));
            }
            return;
        }
        handleRequireStrongAuthOneUser(strongAuthReason, userId);
    }

    private void handleRequireStrongAuthOneUser(int strongAuthReason, int userId) {
        int newValue;
        int oldValue = this.mStrongAuthForUser.get(userId, this.mDefaultStrongAuthFlags);
        if (strongAuthReason == 0) {
            newValue = 0;
        } else {
            newValue = oldValue | strongAuthReason;
        }
        if (oldValue != newValue) {
            this.mStrongAuthForUser.put(userId, newValue);
            notifyStrongAuthTrackers(newValue, userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveUser(int userId) {
        int index = this.mStrongAuthForUser.indexOfKey(userId);
        if (index >= 0) {
            this.mStrongAuthForUser.removeAt(index);
            notifyStrongAuthTrackers(this.mDefaultStrongAuthFlags, userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScheduleStrongAuthTimeout(int userId) {
        long when = SystemClock.elapsedRealtime() + ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getRequiredStrongAuthTimeout(null, userId);
        StrongAuthTimeoutAlarmListener alarm = this.mStrongAuthTimeoutAlarmListenerForUser.get(Integer.valueOf(userId));
        if (alarm != null) {
            this.mAlarmManager.cancel(alarm);
            alarm.setTrigerTime(true, when);
        } else {
            alarm = createAlarmListener(userId);
            alarm.setTrigerTime(true, when);
            this.mStrongAuthTimeoutAlarmListenerForUser.put(Integer.valueOf(userId), alarm);
        }
        this.mAlarmManager.set(3, when, STRONG_AUTH_TIMEOUT_ALARM_TAG, alarm, this.mHandler);
    }

    private void notifyStrongAuthTrackers(int strongAuthReason, int userId) {
        int i = this.mTrackers.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                this.mTrackers.getBroadcastItem(i).onStrongAuthRequiredChanged(strongAuthReason, userId);
            } catch (RemoteException e) {
                Slog.e(TAG, "Exception while notifying StrongAuthTracker.", e);
            } catch (Throwable th) {
                this.mTrackers.finishBroadcast();
                throw th;
            }
        }
        this.mTrackers.finishBroadcast();
    }

    public void registerStrongAuthTracker(IStrongAuthTracker tracker) {
        this.mHandler.obtainMessage(2, tracker).sendToTarget();
    }

    public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) {
        this.mHandler.obtainMessage(3, tracker).sendToTarget();
    }

    public void removeUser(int userId) {
        this.mHandler.obtainMessage(4, userId, 0).sendToTarget();
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        if (userId == -1 || userId >= 0) {
            this.mHandler.obtainMessage(1, strongAuthReason, userId).sendToTarget();
            return;
        }
        throw new IllegalArgumentException("userId must be an explicit user id or USER_ALL");
    }

    public void reportUnlock(int userId) {
        requireStrongAuth(0, userId);
    }

    public void reportSuccessfulStrongAuthUnlock(int userId) {
        this.mHandler.obtainMessage(5, userId, 0).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public class StrongAuthTimeoutAlarmListener implements AlarmManager.OnAlarmListener {
        private final int mUserId;

        public StrongAuthTimeoutAlarmListener(int userId) {
            this.mUserId = userId;
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            LockSettingsStrongAuth.this.requireStrongAuth(16, this.mUserId);
        }

        /* access modifiers changed from: protected */
        public void setTrigerTime(boolean isStrong, long alarmTime) {
        }
    }

    /* access modifiers changed from: protected */
    public StrongAuthTimeoutAlarmListener createAlarmListener(int userId) {
        return new StrongAuthTimeoutAlarmListener(userId);
    }

    public void reportSuccessfulWeakAuthUnlock(int userId) {
    }

    /* access modifiers changed from: protected */
    public void handleScheduleWeakAuthTimeout(int userId) {
    }

    /* access modifiers changed from: protected */
    public void handleExtendMessage(Message msg) {
    }
}
