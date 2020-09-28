package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.IAlarmListener;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import libcore.timezone.ZoneInfoDB;

public class AlarmManager {
    public static final String ACTION_NEXT_ALARM_CLOCK_CHANGED = "android.app.action.NEXT_ALARM_CLOCK_CHANGED";
    public static final int ELAPSED_REALTIME = 3;
    public static final int ELAPSED_REALTIME_WAKEUP = 2;
    public static final int FLAG_ALLOW_WHILE_IDLE = 4;
    @UnsupportedAppUsage
    public static final int FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED = 8;
    @UnsupportedAppUsage
    public static final int FLAG_IDLE_UNTIL = 16;
    @UnsupportedAppUsage
    public static final int FLAG_STANDALONE = 1;
    @UnsupportedAppUsage
    public static final int FLAG_WAKE_FROM_IDLE = 2;
    public static final long INTERVAL_DAY = 86400000;
    public static final long INTERVAL_FIFTEEN_MINUTES = 900000;
    public static final long INTERVAL_HALF_DAY = 43200000;
    public static final long INTERVAL_HALF_HOUR = 1800000;
    public static final long INTERVAL_HOUR = 3600000;
    public static final int RTC = 1;
    public static final int RTC_WAKEUP = 0;
    private static final String TAG = "AlarmManager";
    @UnsupportedAppUsage
    public static final long WINDOW_EXACT = 0;
    @UnsupportedAppUsage
    public static final long WINDOW_HEURISTIC = -1;
    private static ArrayMap<OnAlarmListener, ListenerWrapper> sWrappers;
    private final boolean mAlwaysExact;
    private final Context mContext;
    private final Handler mMainThreadHandler;
    private final String mPackageName;
    @UnsupportedAppUsage
    private final IAlarmManager mService;
    private final int mTargetSdkVersion;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AlarmType {
    }

    public interface OnAlarmListener {
        void onAlarm();
    }

    /* access modifiers changed from: package-private */
    public final class ListenerWrapper extends IAlarmListener.Stub implements Runnable {
        IAlarmCompleteListener mCompletion;
        Handler mHandler;
        final OnAlarmListener mListener;

        public ListenerWrapper(OnAlarmListener listener) {
            this.mListener = listener;
        }

        public void setHandler(Handler h) {
            this.mHandler = h;
        }

        public void cancel() {
            try {
                AlarmManager.this.mService.remove(null, this);
                synchronized (AlarmManager.class) {
                    if (AlarmManager.sWrappers != null) {
                        AlarmManager.sWrappers.remove(this.mListener);
                    }
                }
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }

        @Override // android.app.IAlarmListener
        public void doAlarm(IAlarmCompleteListener alarmManager) {
            this.mCompletion = alarmManager;
            synchronized (AlarmManager.class) {
                if (AlarmManager.sWrappers != null) {
                    AlarmManager.sWrappers.remove(this.mListener);
                }
            }
            this.mHandler.post(this);
        }

        public void run() {
            try {
                this.mListener.onAlarm();
            } finally {
                try {
                    this.mCompletion.alarmComplete(this);
                } catch (Exception e) {
                    Log.e(AlarmManager.TAG, "Unable to report completion to Alarm Manager!", e);
                }
            }
        }
    }

    AlarmManager(IAlarmManager service, Context ctx) {
        this.mService = service;
        this.mContext = ctx;
        this.mPackageName = ctx.getPackageName();
        this.mTargetSdkVersion = ctx.getApplicationInfo().targetSdkVersion;
        this.mAlwaysExact = this.mTargetSdkVersion < 19;
        this.mMainThreadHandler = new Handler(ctx.getMainLooper());
    }

    private long legacyExactLength() {
        return this.mAlwaysExact ? 0 : -1;
    }

    public void set(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, legacyExactLength(), 0, 0, operation, null, null, null, null, null);
    }

    public void set(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, triggerAtMillis, legacyExactLength(), 0, 0, null, listener, tag, targetHandler, null, null);
    }

    public void setRepeating(int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, legacyExactLength(), intervalMillis, 0, operation, null, null, null, null, null);
    }

    public void setWindow(int type, long windowStartMillis, long windowLengthMillis, PendingIntent operation) {
        setImpl(type, windowStartMillis, windowLengthMillis, 0, 0, operation, null, null, null, null, null);
    }

    public void setWindow(int type, long windowStartMillis, long windowLengthMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, windowStartMillis, windowLengthMillis, 0, 0, null, listener, tag, targetHandler, null, null);
    }

    public void setExact(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, 0, 0, 0, operation, null, null, null, null, null);
    }

    public void setExact(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, triggerAtMillis, 0, 0, 0, null, listener, tag, targetHandler, null, null);
    }

    public void setIdleUntil(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, triggerAtMillis, 0, 0, 16, null, listener, tag, targetHandler, null, null);
    }

    public void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {
        setImpl(0, info.getTriggerTime(), 0, 0, 0, operation, null, null, null, null, info);
    }

    @SystemApi
    public void set(int type, long triggerAtMillis, long windowMillis, long intervalMillis, PendingIntent operation, WorkSource workSource) {
        setImpl(type, triggerAtMillis, windowMillis, intervalMillis, 0, operation, null, null, null, workSource, null);
    }

    @UnsupportedAppUsage
    public void set(int type, long triggerAtMillis, long windowMillis, long intervalMillis, String tag, OnAlarmListener listener, Handler targetHandler, WorkSource workSource) {
        setImpl(type, triggerAtMillis, windowMillis, intervalMillis, 0, null, listener, tag, targetHandler, workSource, null);
    }

    @SystemApi
    public void set(int type, long triggerAtMillis, long windowMillis, long intervalMillis, OnAlarmListener listener, Handler targetHandler, WorkSource workSource) {
        setImpl(type, triggerAtMillis, windowMillis, intervalMillis, 0, null, listener, null, targetHandler, workSource, null);
    }

    private void setImpl(int type, long triggerAtMillis, long windowMillis, long intervalMillis, int flags, PendingIntent operation, OnAlarmListener listener, String listenerTag, Handler targetHandler, WorkSource workSource, AlarmClockInfo alarmClock) {
        long triggerAtMillis2;
        ListenerWrapper recipientWrapper;
        ListenerWrapper recipientWrapper2;
        if (triggerAtMillis < 0) {
            triggerAtMillis2 = 0;
        } else {
            triggerAtMillis2 = triggerAtMillis;
        }
        if (listener != null) {
            synchronized (AlarmManager.class) {
                if (sWrappers == null) {
                    sWrappers = new ArrayMap<>();
                }
                recipientWrapper2 = sWrappers.get(listener);
                if (recipientWrapper2 == null) {
                    recipientWrapper2 = new ListenerWrapper(listener);
                    sWrappers.put(listener, recipientWrapper2);
                }
            }
            recipientWrapper2.setHandler(targetHandler != null ? targetHandler : this.mMainThreadHandler);
            recipientWrapper = recipientWrapper2;
        } else {
            recipientWrapper = null;
        }
        try {
            this.mService.set(this.mPackageName, type, triggerAtMillis2, windowMillis, intervalMillis, flags, operation, recipientWrapper, listenerTag, workSource, alarmClock);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setInexactRepeating(int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, -1, intervalMillis, 0, operation, null, null, null, null, null);
    }

    public void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, -1, 0, 4, operation, null, null, null, null, null);
    }

    public void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, 0, 0, 4, operation, null, null, null, null, null);
    }

    public void cancel(PendingIntent operation) {
        if (operation != null) {
            try {
                this.mService.remove(operation, null);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else if (this.mTargetSdkVersion < 24) {
            Log.e(TAG, "cancel() called with a null PendingIntent");
        } else {
            throw new NullPointerException("cancel() called with a null PendingIntent");
        }
    }

    public void cancel(OnAlarmListener listener) {
        if (listener != null) {
            ListenerWrapper wrapper = null;
            synchronized (AlarmManager.class) {
                if (sWrappers != null) {
                    wrapper = sWrappers.get(listener);
                }
            }
            if (wrapper == null) {
                Log.w(TAG, "Unrecognized alarm listener " + listener);
                return;
            }
            wrapper.cancel();
            return;
        }
        throw new NullPointerException("cancel() called with a null OnAlarmListener");
    }

    public void setTime(long millis) {
        try {
            this.mService.setTime(millis);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setTimeZone(String timeZone) {
        if (!TextUtils.isEmpty(timeZone)) {
            if (this.mTargetSdkVersion >= 23) {
                boolean hasTimeZone = false;
                try {
                    hasTimeZone = ZoneInfoDB.getInstance().hasTimeZone(timeZone);
                } catch (IOException e) {
                }
                if (!hasTimeZone) {
                    throw new IllegalArgumentException("Timezone: " + timeZone + " is not an Olson ID");
                }
            }
            try {
                this.mService.setTimeZone(timeZone);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public long getNextWakeFromIdleTime() {
        try {
            return this.mService.getNextWakeFromIdleTime();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public AlarmClockInfo getNextAlarmClock() {
        return getNextAlarmClock(this.mContext.getUserId());
    }

    public AlarmClockInfo getNextAlarmClock(int userId) {
        try {
            return this.mService.getNextAlarmClock(userId);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public static final class AlarmClockInfo implements Parcelable {
        public static final Parcelable.Creator<AlarmClockInfo> CREATOR = new Parcelable.Creator<AlarmClockInfo>() {
            /* class android.app.AlarmManager.AlarmClockInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public AlarmClockInfo createFromParcel(Parcel in) {
                return new AlarmClockInfo(in);
            }

            @Override // android.os.Parcelable.Creator
            public AlarmClockInfo[] newArray(int size) {
                return new AlarmClockInfo[size];
            }
        };
        private final PendingIntent mShowIntent;
        private final long mTriggerTime;

        public AlarmClockInfo(long triggerTime, PendingIntent showIntent) {
            this.mTriggerTime = triggerTime;
            this.mShowIntent = showIntent;
        }

        AlarmClockInfo(Parcel in) {
            this.mTriggerTime = in.readLong();
            this.mShowIntent = (PendingIntent) in.readParcelable(PendingIntent.class.getClassLoader());
        }

        public long getTriggerTime() {
            return this.mTriggerTime;
        }

        public PendingIntent getShowIntent() {
            return this.mShowIntent;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.mTriggerTime);
            dest.writeParcelable(this.mShowIntent, flags);
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1112396529665L, this.mTriggerTime);
            PendingIntent pendingIntent = this.mShowIntent;
            if (pendingIntent != null) {
                pendingIntent.writeToProto(proto, 1146756268034L);
            }
            proto.end(token);
        }
    }
}
