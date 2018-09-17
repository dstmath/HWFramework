package com.android.server.display;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import android.util.Slog;
import java.util.Calendar;

public class HwEyeProtectionDividedTimeControl {
    private static final String[] ACTION_DIVIDED_TIME_CONTROL = null;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_END = 1;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX = 2;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_START = 0;
    public static final int DAY_IN_MINUTE = 1440;
    public static final long DAY_IN_MIllIS = 86400000;
    private static final int DEFAULT_TIME = -1;
    private static final int DEFAULT_USER = 0;
    private static final int HOUR_IN_MINUTE = 60;
    private static final String TAG = "EyeProtectionDividedTimeControl";
    private static final String USER_ID = "user_id";
    private AlarmManager mAlarmManager;
    private Context mContext;
    private HwEyeProtectionControllerImpl mEyeProtectionControlImpl;
    private boolean mInDividedTimeFlag;
    private boolean mInNextDay;
    private boolean mNowInNextDay;
    private int mScheduledBeginHour;
    private int mScheduledBeginMinute;
    private long mScheduledBeginTime;
    private int mScheduledEndHour;
    private int mScheduledEndMinute;
    private long mScheduledEndTime;
    private TimeControlAlarmReceiver mTimeControlAlarmReceiver;

    private class TimeControlAlarmReceiver extends BroadcastReceiver {
        public TimeControlAlarmReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[HwEyeProtectionDividedTimeControl.DEFAULT_USER]);
            filter.addAction(HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[HwEyeProtectionDividedTimeControl.ALARM_TYPE_DIVIDED_TIME_CONTROL_END]);
            HwEyeProtectionDividedTimeControl.this.mContext.registerReceiver(this, filter);
            Slog.d(HwEyeProtectionDividedTimeControl.TAG, "TimeControlAlarmReceiver registerReceiver");
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                PowerManager pm = (PowerManager) context.getSystemService("power");
                boolean isScreenOn = pm.isScreenOn();
                Slog.i(HwEyeProtectionDividedTimeControl.TAG, "onReceive intent action = " + intent.getAction() + ",isScreenOn " + isScreenOn);
                if (HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[HwEyeProtectionDividedTimeControl.DEFAULT_USER].equals(intent.getAction())) {
                    HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, HwEyeProtectionDividedTimeControl.DEFAULT_USER);
                    if (!isScreenOn) {
                        Slog.i(HwEyeProtectionDividedTimeControl.TAG, "isScreenon is " + pm.isScreenOn());
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeProtectionScreenTurnOffMode(HwEyeProtectionDividedTimeControl.ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX);
                    } else if (HwEyeProtectionDividedTimeControl.this.mInDividedTimeFlag) {
                        Slog.i(HwEyeProtectionDividedTimeControl.TAG, "onReceive intent action = " + intent.getAction() + " has in divided time status");
                    } else {
                        HwEyeProtectionDividedTimeControl.this.mInDividedTimeFlag = true;
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeScheduleSwitchToUserMode(3);
                    }
                } else if (HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[HwEyeProtectionDividedTimeControl.ALARM_TYPE_DIVIDED_TIME_CONTROL_END].equals(intent.getAction())) {
                    HwEyeProtectionDividedTimeControl.this.mInDividedTimeFlag = false;
                    if (HwEyeProtectionDividedTimeControl.this.mInNextDay) {
                        HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(0, HwEyeProtectionDividedTimeControl.ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
                    } else {
                        HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, HwEyeProtectionDividedTimeControl.ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
                    }
                    if (isScreenOn) {
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeScheduleSwitchToUserMode(HwEyeProtectionDividedTimeControl.DEFAULT_USER);
                    } else {
                        Slog.i(HwEyeProtectionDividedTimeControl.TAG, "isScreenon is " + pm.isScreenOn());
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeProtectionScreenTurnOffMode(HwEyeProtectionDividedTimeControl.ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwEyeProtectionDividedTimeControl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwEyeProtectionDividedTimeControl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwEyeProtectionDividedTimeControl.<clinit>():void");
    }

    public HwEyeProtectionDividedTimeControl(Context context, HwEyeProtectionControllerImpl eyeProtectionControlImpl) {
        this.mInDividedTimeFlag = false;
        this.mNowInNextDay = false;
        this.mScheduledBeginTime = -1;
        this.mScheduledEndTime = -1;
        this.mScheduledBeginHour = DEFAULT_TIME;
        this.mScheduledBeginMinute = DEFAULT_TIME;
        this.mScheduledEndHour = DEFAULT_TIME;
        this.mScheduledEndMinute = DEFAULT_TIME;
        this.mInNextDay = false;
        this.mAlarmManager = null;
        this.mEyeProtectionControlImpl = null;
        this.mTimeControlAlarmReceiver = null;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Slog.d(TAG, "can not get alarmManager");
        }
        this.mContext = context;
        this.mEyeProtectionControlImpl = eyeProtectionControlImpl;
        this.mTimeControlAlarmReceiver = new TimeControlAlarmReceiver();
    }

    public void setTimeControlAlarm(long delayTime, int type) {
        Slog.d(TAG, "set setTimeControlAlarm type is " + type + ", delayTime is " + delayTime);
        if (type < ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX && this.mScheduledBeginHour != DEFAULT_TIME && this.mScheduledEndHour != DEFAULT_TIME) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int day = cal.get(5);
            cal.set(13, DEFAULT_USER);
            cal.set(14, DEFAULT_USER);
            if (type == 0) {
                if (delayTime != 0) {
                    day += ALARM_TYPE_DIVIDED_TIME_CONTROL_END;
                }
                cal.set(11, this.mScheduledBeginHour);
                cal.set(12, this.mScheduledBeginMinute);
                cal.set(5, day);
                Slog.i(TAG, "set setTimeControlAlarm mScheduledBeginHour " + this.mScheduledBeginHour + ", mScheduledBeginMinute is " + this.mScheduledBeginMinute + ", day is " + day);
            } else {
                cal.set(11, this.mScheduledEndHour);
                cal.set(12, this.mScheduledEndMinute);
                if (this.mInNextDay && delayTime != 0) {
                    day += ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX;
                }
                if (this.mInNextDay && delayTime == 0) {
                    if (this.mInDividedTimeFlag && this.mNowInNextDay) {
                        Slog.i(TAG, "end time has not coming set it in today");
                    } else {
                        day += ALARM_TYPE_DIVIDED_TIME_CONTROL_END;
                    }
                }
                if (!(this.mInNextDay || delayTime == 0)) {
                    day += ALARM_TYPE_DIVIDED_TIME_CONTROL_END;
                }
                cal.set(5, day);
                Slog.i(TAG, "set setTimeControlAlarm mScheduledEndHour " + this.mScheduledEndHour + ", mScheduledEndMinute is " + this.mScheduledEndMinute + ", day is " + day);
            }
            Slog.i(TAG, "set setTimeControlAlarm day is " + day);
            long time = cal.getTimeInMillis();
            Intent intent = new Intent(ACTION_DIVIDED_TIME_CONTROL[type]);
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, DEFAULT_USER, intent, DEFAULT_USER);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
                Slog.d(TAG, "setTimeControlAlarm(): cancel prev alarm");
            }
            this.mAlarmManager.setExact(DEFAULT_USER, time, PendingIntent.getBroadcast(this.mContext, DEFAULT_USER, intent, DEFAULT_USER));
        }
    }

    protected void setTime(int time, int type) {
        Slog.d(TAG, "set time type is " + type);
        if (type == 0) {
            this.mScheduledBeginTime = (long) time;
        } else if (type == ALARM_TYPE_DIVIDED_TIME_CONTROL_END) {
            this.mScheduledEndTime = (long) time;
        } else {
            Slog.d(TAG, "no type to settime");
            return;
        }
        SetHourAndMininte(time, type);
    }

    private void SetHourAndMininte(int time, int type) {
        int hour = time / HOUR_IN_MINUTE;
        int minute = time % HOUR_IN_MINUTE;
        Slog.d(TAG, "SetHourAndMininte hour is " + hour + ",minute is " + minute);
        if (type == 0) {
            this.mScheduledBeginHour = hour;
            this.mScheduledBeginMinute = minute;
        } else if (type == ALARM_TYPE_DIVIDED_TIME_CONTROL_END) {
            this.mScheduledEndHour = hour;
            this.mScheduledEndMinute = minute;
        } else {
            Log.d(TAG, "no type to set hour and minute");
        }
    }

    private void SetTimeToDefault(int defaultValue) {
        this.mScheduledBeginTime = (long) defaultValue;
        this.mScheduledEndTime = (long) defaultValue;
        this.mScheduledBeginHour = defaultValue;
        this.mScheduledBeginMinute = defaultValue;
        this.mScheduledEndHour = defaultValue;
        this.mScheduledEndMinute = defaultValue;
    }

    public void cancelTimeControlAlarm(int type) {
        Slog.d(TAG, "cancelTimeControlAlarm type is " + type);
        if (type < ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX) {
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, DEFAULT_USER, new Intent(ACTION_DIVIDED_TIME_CONTROL[type]), DEFAULT_USER);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
            }
        }
    }

    public void delayTimeControlAlarm(long time, int type) {
        Slog.d(TAG, "delayTimeControlAlarm type is " + type);
        if (type < ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX) {
            Intent intent = new Intent(ACTION_DIVIDED_TIME_CONTROL[type]);
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, DEFAULT_USER, intent, DEFAULT_USER);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
            }
            this.mAlarmManager.setExact(DEFAULT_USER, time, PendingIntent.getBroadcast(this.mContext, DEFAULT_USER, intent, DEFAULT_USER));
        }
    }

    public boolean getInDividedTimeFlag() {
        return this.mInDividedTimeFlag;
    }

    public void setInDividedTimeFlag(boolean flag) {
        this.mInDividedTimeFlag = flag;
    }

    public void updateDiviedTimeFlag() {
        if (this.mScheduledBeginTime != -1 && this.mScheduledEndTime != -1) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int curTime = (cal.get(11) * HOUR_IN_MINUTE) + cal.get(12);
            int beginTime = (this.mScheduledBeginHour * HOUR_IN_MINUTE) + this.mScheduledBeginMinute;
            int endTime = (this.mScheduledEndHour * HOUR_IN_MINUTE) + this.mScheduledEndMinute;
            this.mInNextDay = false;
            this.mNowInNextDay = false;
            if (endTime < beginTime) {
                this.mInNextDay = true;
            }
            Slog.d(TAG, "updateDiviedTimeFlag mScheduledBeginHour=" + this.mScheduledBeginHour + ",mScheduledBeginMinute=" + this.mScheduledBeginMinute);
            Slog.d(TAG, "updateDiviedTimeFlag mScheduledEndHour=" + this.mScheduledEndHour + ",mScheduledEndMinute=" + this.mScheduledEndMinute + ",inNextDay is " + this.mInNextDay);
            if (this.mInNextDay) {
                if ((curTime < beginTime || curTime >= DAY_IN_MINUTE) && (curTime < 0 || curTime > endTime)) {
                    this.mInDividedTimeFlag = false;
                } else {
                    this.mInDividedTimeFlag = true;
                    if (curTime >= 0 && curTime <= endTime) {
                        this.mNowInNextDay = true;
                    }
                }
            } else if (curTime < beginTime || curTime >= endTime) {
                this.mInDividedTimeFlag = false;
            } else {
                this.mInDividedTimeFlag = true;
            }
            Slog.d(TAG, "updateDiviedTimeFlag mInDividedTimeFlag=" + this.mInDividedTimeFlag);
        }
    }

    public void reSetTimeControlAlarm() {
        if (this.mScheduledBeginTime != -1 && this.mScheduledEndTime != -1) {
            Slog.d(TAG, "reSetTimeControlAlarm");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int curTime = (cal.get(11) * HOUR_IN_MINUTE) + cal.get(12);
            int beginTime = (this.mScheduledBeginHour * HOUR_IN_MINUTE) + this.mScheduledBeginMinute;
            int endTime = (this.mScheduledEndHour * HOUR_IN_MINUTE) + this.mScheduledEndMinute;
            this.mInNextDay = false;
            this.mNowInNextDay = false;
            if (endTime < beginTime) {
                this.mInNextDay = true;
            }
            if (this.mInNextDay) {
                if ((curTime < beginTime || curTime >= DAY_IN_MINUTE) && (curTime < 0 || curTime > endTime)) {
                    this.mInDividedTimeFlag = false;
                    setTimeControlAlarm(0, DEFAULT_USER);
                    setTimeControlAlarm(0, ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
                } else {
                    this.mInDividedTimeFlag = true;
                    if (curTime >= 0 && curTime <= endTime) {
                        this.mNowInNextDay = true;
                        setTimeControlAlarm(0, DEFAULT_USER);
                        setTimeControlAlarm(0, ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
                    }
                    if (curTime >= beginTime && curTime < DAY_IN_MINUTE) {
                        setTimeControlAlarm(DAY_IN_MIllIS, DEFAULT_USER);
                        setTimeControlAlarm(0, ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
                    }
                }
            } else if (curTime >= beginTime && curTime < endTime) {
                this.mInDividedTimeFlag = true;
                setTimeControlAlarm(DAY_IN_MIllIS, DEFAULT_USER);
                setTimeControlAlarm(0, ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
            } else if (curTime >= endTime) {
                this.mInDividedTimeFlag = false;
                setTimeControlAlarm(DAY_IN_MIllIS, DEFAULT_USER);
                setTimeControlAlarm(DAY_IN_MIllIS, ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
            } else if (curTime < beginTime) {
                this.mInDividedTimeFlag = false;
                setTimeControlAlarm(0, DEFAULT_USER);
                setTimeControlAlarm(0, ALARM_TYPE_DIVIDED_TIME_CONTROL_END);
            }
        }
    }

    public boolean testTimeIsValid(long time) {
        if (time != 0 && System.currentTimeMillis() < time) {
            return true;
        }
        return false;
    }
}
