package android.app;

import android.app.IAlarmListener.Stub;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.io.IOException;
import libcore.util.ZoneInfoDB;

public class AlarmManager {
    public static final String ACTION_NEXT_ALARM_CLOCK_CHANGED = "android.app.action.NEXT_ALARM_CLOCK_CHANGED";
    public static final int ELAPSED_REALTIME = 3;
    public static final int ELAPSED_REALTIME_WAKEUP = 2;
    public static final int FLAG_ALLOW_WHILE_IDLE = 4;
    public static final int FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED = 8;
    public static final int FLAG_IDLE_UNTIL = 16;
    public static final int FLAG_STANDALONE = 1;
    public static final int FLAG_WAKE_FROM_IDLE = 2;
    public static final long INTERVAL_DAY = 86400000;
    public static final long INTERVAL_FIFTEEN_MINUTES = 900000;
    public static final long INTERVAL_HALF_DAY = 43200000;
    public static final long INTERVAL_HALF_HOUR = 1800000;
    public static final long INTERVAL_HOUR = 3600000;
    public static final int RTC = 1;
    public static final int RTC_WAKEUP = 0;
    private static final String TAG = "AlarmManager";
    public static final long WINDOW_EXACT = 0;
    public static final long WINDOW_HEURISTIC = -1;
    private static ArrayMap<OnAlarmListener, ListenerWrapper> sWrappers;
    private final boolean mAlwaysExact;
    private final Handler mMainThreadHandler;
    private final String mPackageName;
    private final IAlarmManager mService;
    private final int mTargetSdkVersion;

    public static final class AlarmClockInfo implements Parcelable {
        public static final Creator<AlarmClockInfo> CREATOR = null;
        private final PendingIntent mShowIntent;
        private final long mTriggerTime;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.AlarmManager.AlarmClockInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.AlarmManager.AlarmClockInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.AlarmManager.AlarmClockInfo.<clinit>():void");
        }

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

        public int describeContents() {
            return AlarmManager.RTC_WAKEUP;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.mTriggerTime);
            dest.writeParcelable(this.mShowIntent, flags);
        }
    }

    final class ListenerWrapper extends Stub implements Runnable {
        IAlarmCompleteListener mCompletion;
        Handler mHandler;
        final OnAlarmListener mListener;
        final /* synthetic */ AlarmManager this$0;

        public ListenerWrapper(AlarmManager this$0, OnAlarmListener listener) {
            this.this$0 = this$0;
            this.mListener = listener;
        }

        public void setHandler(Handler h) {
            this.mHandler = h;
        }

        public void cancel() {
            try {
                this.this$0.mService.remove(null, this);
                synchronized (AlarmManager.class) {
                    if (AlarmManager.sWrappers != null) {
                        AlarmManager.sWrappers.remove(this.mListener);
                    }
                }
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }

        public void doAlarm(IAlarmCompleteListener alarmManager) {
            this.mCompletion = alarmManager;
            this.mHandler.post(this);
        }

        public void run() {
            synchronized (AlarmManager.class) {
                if (AlarmManager.sWrappers != null) {
                    AlarmManager.sWrappers.remove(this.mListener);
                }
            }
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

    public interface OnAlarmListener {
        void onAlarm();
    }

    AlarmManager(IAlarmManager service, Context ctx) {
        this.mService = service;
        this.mPackageName = ctx.getPackageName();
        this.mTargetSdkVersion = ctx.getApplicationInfo().targetSdkVersion;
        this.mAlwaysExact = this.mTargetSdkVersion < 19;
        this.mMainThreadHandler = new Handler(ctx.getMainLooper());
    }

    private long legacyExactLength() {
        return this.mAlwaysExact ? WINDOW_EXACT : WINDOW_HEURISTIC;
    }

    public void set(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, legacyExactLength(), WINDOW_EXACT, RTC_WAKEUP, operation, null, null, null, null, null);
    }

    public void set(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, triggerAtMillis, legacyExactLength(), WINDOW_EXACT, RTC_WAKEUP, null, listener, tag, targetHandler, null, null);
    }

    public void setRepeating(int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, legacyExactLength(), intervalMillis, RTC_WAKEUP, operation, null, null, null, null, null);
    }

    public void setWindow(int type, long windowStartMillis, long windowLengthMillis, PendingIntent operation) {
        setImpl(type, windowStartMillis, windowLengthMillis, WINDOW_EXACT, RTC_WAKEUP, operation, null, null, null, null, null);
    }

    public void setWindow(int type, long windowStartMillis, long windowLengthMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, windowStartMillis, windowLengthMillis, WINDOW_EXACT, RTC_WAKEUP, null, listener, tag, targetHandler, null, null);
    }

    public void setExact(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, WINDOW_EXACT, WINDOW_EXACT, RTC_WAKEUP, operation, null, null, null, null, null);
    }

    public void setExact(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, triggerAtMillis, WINDOW_EXACT, WINDOW_EXACT, RTC_WAKEUP, null, listener, tag, targetHandler, null, null);
    }

    public void setIdleUntil(int type, long triggerAtMillis, String tag, OnAlarmListener listener, Handler targetHandler) {
        setImpl(type, triggerAtMillis, WINDOW_EXACT, WINDOW_EXACT, FLAG_IDLE_UNTIL, null, listener, tag, targetHandler, null, null);
    }

    public void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {
        setImpl(RTC_WAKEUP, info.getTriggerTime(), WINDOW_EXACT, WINDOW_EXACT, RTC_WAKEUP, operation, null, null, null, null, info);
    }

    public void set(int type, long triggerAtMillis, long windowMillis, long intervalMillis, PendingIntent operation, WorkSource workSource) {
        setImpl(type, triggerAtMillis, windowMillis, intervalMillis, RTC_WAKEUP, operation, null, null, null, workSource, null);
    }

    public void set(int type, long triggerAtMillis, long windowMillis, long intervalMillis, String tag, OnAlarmListener listener, Handler targetHandler, WorkSource workSource) {
        setImpl(type, triggerAtMillis, windowMillis, intervalMillis, RTC_WAKEUP, null, listener, tag, targetHandler, workSource, null);
    }

    public void set(int type, long triggerAtMillis, long windowMillis, long intervalMillis, OnAlarmListener listener, Handler targetHandler, WorkSource workSource) {
        setImpl(type, triggerAtMillis, windowMillis, intervalMillis, RTC_WAKEUP, null, listener, null, targetHandler, workSource, null);
    }

    private void setImpl(int type, long triggerAtMillis, long windowMillis, long intervalMillis, int flags, PendingIntent operation, OnAlarmListener listener, String listenerTag, Handler targetHandler, WorkSource workSource, AlarmClockInfo alarmClock) {
        Throwable th;
        if (triggerAtMillis < WINDOW_EXACT) {
            triggerAtMillis = WINDOW_EXACT;
        }
        IAlarmListener iAlarmListener = null;
        if (listener != null) {
            synchronized (AlarmManager.class) {
                try {
                    if (sWrappers == null) {
                        sWrappers = new ArrayMap();
                    }
                    iAlarmListener = (ListenerWrapper) sWrappers.get(listener);
                    if (iAlarmListener == null) {
                        ListenerWrapper listenerWrapper = new ListenerWrapper(this, listener);
                        try {
                            sWrappers.put(listener, listenerWrapper);
                            iAlarmListener = listenerWrapper;
                        } catch (Throwable th2) {
                            th = th2;
                            ListenerWrapper listenerWrapper2 = listenerWrapper;
                            throw th;
                        }
                    }
                    iAlarmListener.setHandler(targetHandler != null ? targetHandler : this.mMainThreadHandler);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        try {
            if (this.mService != null) {
                this.mService.set(this.mPackageName, type, triggerAtMillis, windowMillis, intervalMillis, flags, operation, iAlarmListener, listenerTag, workSource, alarmClock);
            }
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void setInexactRepeating(int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, WINDOW_HEURISTIC, intervalMillis, RTC_WAKEUP, operation, null, null, null, null, null);
    }

    public void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, WINDOW_HEURISTIC, WINDOW_EXACT, FLAG_ALLOW_WHILE_IDLE, operation, null, null, null, null, null);
    }

    public void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, WINDOW_EXACT, WINDOW_EXACT, FLAG_ALLOW_WHILE_IDLE, operation, null, null, null, null, null);
    }

    public void cancel(PendingIntent operation) {
        if (operation == null) {
            String msg = "cancel() called with a null PendingIntent";
            if (this.mTargetSdkVersion >= 24) {
                throw new NullPointerException("cancel() called with a null PendingIntent");
            }
            Log.e(TAG, "cancel() called with a null PendingIntent");
            return;
        }
        try {
            if (this.mService != null) {
                this.mService.remove(operation, null);
            }
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void cancel(OnAlarmListener listener) {
        if (listener == null) {
            throw new NullPointerException("cancel() called with a null OnAlarmListener");
        }
        ListenerWrapper listenerWrapper = null;
        synchronized (AlarmManager.class) {
            if (sWrappers != null) {
                listenerWrapper = (ListenerWrapper) sWrappers.get(listener);
            }
        }
        if (listenerWrapper == null) {
            Log.w(TAG, "Unrecognized alarm listener " + listener);
        } else {
            listenerWrapper.cancel();
        }
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
        return getNextAlarmClock(UserHandle.myUserId());
    }

    public AlarmClockInfo getNextAlarmClock(int userId) {
        try {
            return this.mService.getNextAlarmClock(userId);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public int getWakeUpNum(int uid, String pkg) {
        int num = RTC_WAKEUP;
        try {
            num = this.mService.getWakeUpNum(uid, pkg);
        } catch (RemoteException e) {
        }
        return num;
    }

    public long checkHasHwRTCAlarm(String packageName) {
        long time = WINDOW_HEURISTIC;
        try {
            time = this.mService.checkHasHwRTCAlarm(packageName);
        } catch (RemoteException e) {
        }
        return time;
    }

    public void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) {
        try {
            this.mService.adjustHwRTCAlarm(deskClockTime, bootOnTime, typeState);
        } catch (RemoteException e) {
        }
    }

    public void setHwAirPlaneStateProp() {
        try {
            this.mService.setHwAirPlaneStateProp();
        } catch (RemoteException e) {
        }
    }
}
