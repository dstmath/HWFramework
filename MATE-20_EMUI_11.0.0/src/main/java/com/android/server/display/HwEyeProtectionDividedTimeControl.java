package com.android.server.display;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import java.util.Calendar;

public class HwEyeProtectionDividedTimeControl {
    private static final String[] ACTION_DIVIDED_TIME_CONTROLS = {"com.android.server.action.DIVIDED_TIME_CONTROL_START", "com.android.server.action.DIVIDED_TIME_CONTROL_END"};
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_END = 1;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX = 2;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_START = 0;
    public static final long DAY_IN_MILLIS = 86400000;
    public static final int DAY_IN_MINUTE = 1440;
    private static final int DEFAULT_VALUE = -1;
    private static final int DELAY_TWO_DAY = 2;
    private static final int HOUR_IN_MINUTE = 60;
    private static final String TAG = "EyeProtectionDividedTimeControl";
    private AlarmManager mAlarmManager = null;
    private Context mContext;
    private HwEyeProtectionControllerImpl mEyeProtectionControlImpl = null;
    private boolean mIsInDividedTimeFlag = false;
    private boolean mIsInNextDay = false;
    private boolean mIsNowInNextDay = false;
    private int mScheduledBeginHour = -1;
    private int mScheduledBeginMinute = -1;
    private long mScheduledBeginTime = -1;
    private int mScheduledEndHour = -1;
    private int mScheduledEndMinute = -1;
    private long mScheduledEndTime = -1;
    private TimeControlAlarmReceiver mTimeControlAlarmReceiver = null;

    public HwEyeProtectionDividedTimeControl(Context context, HwEyeProtectionControllerImpl eyeProtectionControlImpl) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            SlogEx.d(TAG, "can not get alarmManager");
        }
        this.mContext = context;
        this.mEyeProtectionControlImpl = eyeProtectionControlImpl;
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.mTimeControlAlarmReceiver = new TimeControlAlarmReceiver();
    }

    public void setTimeControlAlarm(long delayTime, int type) {
        SlogEx.d(TAG, "set setTimeControlAlarm type is " + type + ", delayTime is " + delayTime);
        if (type < 2 && this.mScheduledBeginHour != -1 && this.mScheduledEndHour != -1) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int day = cal.get(5);
            cal.set(13, 0);
            cal.set(14, 0);
            if (type == 0) {
                if (delayTime != 0) {
                    day++;
                }
                cal.set(11, this.mScheduledBeginHour);
                cal.set(12, this.mScheduledBeginMinute);
                cal.set(5, day);
                SlogEx.i(TAG, "set setTimeControlAlarm mScheduledBeginHour " + this.mScheduledBeginHour + ", mScheduledBeginMinute is " + this.mScheduledBeginMinute + ", day is " + day);
            } else {
                cal.set(11, this.mScheduledEndHour);
                cal.set(12, this.mScheduledEndMinute);
                cal.set(5, getScheduledEndDay(delayTime, day));
                SlogEx.i(TAG, "set setTimeControlAlarm mScheduledEndHour " + this.mScheduledEndHour + ", mScheduledEndMinute is " + this.mScheduledEndMinute + ", day is " + day);
            }
            SlogEx.i(TAG, "set setTimeControlAlarm day is " + day);
            long time = cal.getTimeInMillis();
            Intent intent = new Intent(ACTION_DIVIDED_TIME_CONTROLS[type]);
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
                SlogEx.d(TAG, "setTimeControlAlarm(): cancel prev alarm");
            }
            this.mAlarmManager.setExact(0, time, PendingIntent.getBroadcast(this.mContext, 0, intent, 0));
        }
    }

    private int getScheduledEndDay(long delayTime, int day) {
        int scheduledEndDay = day;
        if (this.mIsInNextDay && delayTime != 0) {
            scheduledEndDay += 2;
        }
        if (this.mIsInNextDay && delayTime == 0) {
            if (!this.mIsInDividedTimeFlag || !this.mIsNowInNextDay) {
                scheduledEndDay++;
            } else {
                SlogEx.i(TAG, "end time has not coming set it in today");
            }
        }
        if (this.mIsInNextDay || delayTime == 0) {
            return scheduledEndDay;
        }
        return scheduledEndDay + 1;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedDelay() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(11);
        int minute = cal.get(12);
        SlogEx.d(TAG, "hour is " + hour + ",minute is " + minute + ",mScheduledBeginHour is " + this.mScheduledBeginHour + ", mScheduledBeginMinute is " + this.mScheduledBeginMinute);
        int i = this.mScheduledBeginHour;
        if (hour > i) {
            return true;
        }
        if (hour != i || minute < this.mScheduledBeginMinute) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setTime(int time, int type) {
        SlogEx.d(TAG, "set time type is " + type);
        if (type == 0) {
            this.mScheduledBeginTime = (long) time;
        } else if (type == 1) {
            this.mScheduledEndTime = (long) time;
        } else {
            SlogEx.d(TAG, "no type to settime");
            return;
        }
        setHourAndMininte(time, type);
    }

    private void setHourAndMininte(int time, int type) {
        int hour = time / HOUR_IN_MINUTE;
        int minute = time % HOUR_IN_MINUTE;
        SlogEx.d(TAG, "setHourAndMininte hour is " + hour + ",minute is " + minute);
        if (type == 0) {
            this.mScheduledBeginHour = hour;
            this.mScheduledBeginMinute = minute;
        } else if (type == 1) {
            this.mScheduledEndHour = hour;
            this.mScheduledEndMinute = minute;
        } else {
            Log.d(TAG, "no type to set hour and minute");
        }
    }

    public void cancelTimeControlAlarm(int type) {
        PendingIntent pending;
        SlogEx.d(TAG, "cancelTimeControlAlarm type is " + type);
        if (type < 2 && (pending = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DIVIDED_TIME_CONTROLS[type]), 0)) != null) {
            this.mAlarmManager.cancel(pending);
        }
    }

    public void delayTimeControlAlarm(long time, int type) {
        SlogEx.d(TAG, "delayTimeControlAlarm type is " + type);
        if (type < 2) {
            Intent intent = new Intent(ACTION_DIVIDED_TIME_CONTROLS[type]);
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
            }
            this.mAlarmManager.setExact(0, time, PendingIntent.getBroadcast(this.mContext, 0, intent, 0));
        }
    }

    public boolean getInDividedTimeFlag() {
        return this.mIsInDividedTimeFlag;
    }

    public void setInDividedTimeFlag(boolean isIn) {
        this.mIsInDividedTimeFlag = isIn;
    }

    public void updateDiviedTimeFlag() {
        if (this.mScheduledBeginTime != -1 && this.mScheduledEndTime != -1) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int hour = cal.get(11);
            int minute = cal.get(12);
            int beginTime = (this.mScheduledBeginHour * HOUR_IN_MINUTE) + this.mScheduledBeginMinute;
            int endTime = (this.mScheduledEndHour * HOUR_IN_MINUTE) + this.mScheduledEndMinute;
            this.mIsInNextDay = false;
            this.mIsNowInNextDay = false;
            if (endTime < beginTime) {
                this.mIsInNextDay = true;
            }
            int curTime = (hour * HOUR_IN_MINUTE) + minute;
            SlogEx.d(TAG, "updateDiviedTimeFlag mScheduledBeginHour=" + this.mScheduledBeginHour + ",mScheduledBeginMinute=" + this.mScheduledBeginMinute);
            SlogEx.d(TAG, "updateDiviedTimeFlag mScheduledEndHour=" + this.mScheduledEndHour + ",mScheduledEndMinute=" + this.mScheduledEndMinute + ",inNextDay is " + this.mIsInNextDay);
            if (!this.mIsInNextDay) {
                if (curTime < beginTime || curTime >= endTime) {
                    this.mIsInDividedTimeFlag = false;
                } else {
                    this.mIsInDividedTimeFlag = true;
                }
            } else if ((curTime < beginTime || curTime >= 1440) && (curTime < 0 || curTime >= endTime)) {
                this.mIsInDividedTimeFlag = false;
            } else {
                this.mIsInDividedTimeFlag = true;
                if (curTime >= 0 && curTime < endTime) {
                    this.mIsNowInNextDay = true;
                }
            }
            SlogEx.d(TAG, "updateDiviedTimeFlag mIsInDividedTimeFlag=" + this.mIsInDividedTimeFlag);
        }
    }

    public void reSetTimeControlAlarm() {
        if (this.mScheduledBeginTime != -1 && this.mScheduledEndTime != -1) {
            SlogEx.d(TAG, "reSetTimeControlAlarm");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int hour = cal.get(11);
            int curTime = (hour * HOUR_IN_MINUTE) + cal.get(12);
            int beginTime = (this.mScheduledBeginHour * HOUR_IN_MINUTE) + this.mScheduledBeginMinute;
            int endTime = (this.mScheduledEndHour * HOUR_IN_MINUTE) + this.mScheduledEndMinute;
            this.mIsInNextDay = false;
            this.mIsNowInNextDay = false;
            if (endTime < beginTime) {
                this.mIsInNextDay = true;
            }
            if (!this.mIsInNextDay) {
                resetTimeControlAlarmToday(curTime, beginTime, endTime);
            } else {
                resetTimeControlAlarmNextDay(curTime, beginTime, endTime);
            }
        }
    }

    private void resetTimeControlAlarmToday(int curTime, int beginTime, int endTime) {
        if (curTime >= beginTime && curTime < endTime) {
            this.mIsInDividedTimeFlag = true;
            setTimeControlAlarm(DAY_IN_MILLIS, 0);
            setTimeControlAlarm(0, 1);
        } else if (curTime >= endTime) {
            this.mIsInDividedTimeFlag = false;
            setTimeControlAlarm(DAY_IN_MILLIS, 0);
            setTimeControlAlarm(DAY_IN_MILLIS, 1);
        } else if (curTime < beginTime) {
            this.mIsInDividedTimeFlag = false;
            setTimeControlAlarm(0, 0);
            setTimeControlAlarm(0, 1);
        }
    }

    private void resetTimeControlAlarmNextDay(int curTime, int beginTime, int endTime) {
        if ((curTime < beginTime || curTime >= 1440) && (curTime < 0 || curTime >= endTime)) {
            this.mIsInDividedTimeFlag = false;
            setTimeControlAlarm(0, 0);
            setTimeControlAlarm(0, 1);
            return;
        }
        this.mIsInDividedTimeFlag = true;
        if (curTime >= 0 && curTime < endTime) {
            this.mIsNowInNextDay = true;
            setTimeControlAlarm(0, 0);
            setTimeControlAlarm(0, 1);
        }
        if (curTime >= beginTime && curTime < 1440) {
            setTimeControlAlarm(DAY_IN_MILLIS, 0);
            setTimeControlAlarm(0, 1);
        }
    }

    /* access modifiers changed from: private */
    public class TimeControlAlarmReceiver extends BroadcastReceiver {
        public TimeControlAlarmReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROLS[0]);
            filter.addAction(HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROLS[1]);
            HwEyeProtectionDividedTimeControl.this.mContext.registerReceiver(this, filter);
            SlogEx.d(HwEyeProtectionDividedTimeControl.TAG, "TimeControlAlarmReceiver registerReceiver");
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                PowerManager pm = (PowerManager) context.getSystemService("power");
                boolean isScreenOn = pm.isScreenOn();
                SlogEx.i(HwEyeProtectionDividedTimeControl.TAG, "onReceive intent action = " + intent.getAction() + ",isScreenOn " + isScreenOn);
                if (HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROLS[0].equals(intent.getAction())) {
                    HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MILLIS, 0);
                    HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setLongDimmingFlag(true);
                    HwEyeProtectionDividedTimeControl.this.setValidTime(false);
                    if (!isScreenOn) {
                        SlogEx.i(HwEyeProtectionDividedTimeControl.TAG, "isScreenon is " + pm.isScreenOn());
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeProtectionScreenTurnOffMode(2);
                    } else if (HwEyeProtectionDividedTimeControl.this.mIsInDividedTimeFlag) {
                        SlogEx.i(HwEyeProtectionDividedTimeControl.TAG, "onReceive intent action = " + intent.getAction() + " has in divided time status");
                    } else {
                        HwEyeProtectionDividedTimeControl.this.updateDiviedTimeFlag();
                        if (HwEyeProtectionDividedTimeControl.this.mIsInDividedTimeFlag) {
                            HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeScheduleSwitchToUserMode(3);
                        }
                    }
                } else if (HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROLS[1].equals(intent.getAction())) {
                    HwEyeProtectionDividedTimeControl.this.updateDiviedTimeFlag();
                    SlogEx.i(HwEyeProtectionDividedTimeControl.TAG, "in divided time status = " + HwEyeProtectionDividedTimeControl.this.mIsInDividedTimeFlag);
                    if (HwEyeProtectionDividedTimeControl.this.mIsInNextDay) {
                        HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(0, 1);
                    } else {
                        HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MILLIS, 1);
                    }
                    if (!HwEyeProtectionDividedTimeControl.this.mIsInDividedTimeFlag) {
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setLongDimmingFlag(true);
                        if (!isScreenOn) {
                            SlogEx.i(HwEyeProtectionDividedTimeControl.TAG, "isScreenon is " + pm.isScreenOn());
                            HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeProtectionScreenTurnOffMode(1);
                            return;
                        }
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeScheduleSwitchToUserMode(0);
                    }
                }
            }
        }
    }

    public boolean testTimeIsValid(long time) {
        if (time != 0 && System.currentTimeMillis() < time) {
            return true;
        }
        return false;
    }

    public void setValidTime(boolean isValidFlag) {
        long validtime = 0;
        if (isValidFlag) {
            validtime = getValidTime();
        }
        SettingsEx.System.putLongForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_COMFORT_VALID, validtime, -2);
        SlogEx.i(TAG, "setValidTime validtime is " + validtime + ", user =-2");
    }

    private long getValidTime() {
        Calendar c = Calendar.getInstance();
        c.set(5, c.get(5) + 1);
        return c.getTimeInMillis();
    }
}
