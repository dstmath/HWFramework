package com.android.contacts.update;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.android.contacts.external.separated.SeparatedResourceUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.update.utils.NetWorkUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public abstract class Updater implements IUpdate {
    private static final long DELAY_TIME = 600000;
    private static final long EVERY_FIFTEEN_DAYS = 1296000000;
    private static final long EVERY_MINUTE = 60000;
    private static final int ITEM_CLOSE = 2;
    private static final int ITEM_WLAN_ONLY = 1;
    private static final String KEY_AUTO_ITEM = "auto_item_";
    private static final String KEY_LAST_UPDATE = "last_update_";
    private static final String KEY_VER = "key_ver_";
    private static final long RE_DELAY_TIME = 21600000;
    private static final String TAG = "DownloadService";
    private String mAutoItemKey;
    protected Context mContext;
    protected int mFileId;
    private String mLastUpdate;
    private PendingIntent mPIntent;
    private SharedPreferences mPref = SharePreferenceUtil.getDefaultSp_de(this.mContext);
    protected String mVerKey;

    public abstract String getSucessNoti();

    public abstract String getTitle();

    public Updater(Context context, int fileId) {
        this.mContext = context.getApplicationContext();
        this.mFileId = fileId;
        this.mAutoItemKey = KEY_AUTO_ITEM + this.mFileId;
        this.mVerKey = KEY_VER + this.mFileId;
        this.mLastUpdate = KEY_LAST_UPDATE + this.mFileId;
        this.mPIntent = getAlarmPendingIntent();
    }

    private PendingIntent getAlarmPendingIntent() {
        Intent intent = new Intent("com.android.contacts.action.UPDATE_FILE");
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra("fileId", this.mFileId);
        return PendingIntent.getService(this.mContext, this.mFileId, intent, 134217728);
    }

    public void scheduleAutoUpdate() {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "fileid : " + this.mFileId + " scheduleAutoUpdate:" + (!isAutoClose()));
        }
        if (!isAutoClose()) {
            scheduleAlarm(EVERY_FIFTEEN_DAYS);
        }
    }

    public void scheduleAlarm(long time) {
        long updatedTime = getLastUpdatedtime();
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - updatedTime;
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(currentTime);
        long deltaTime = time - diffTime;
        long delayTime = getRandomTime(DELAY_TIME);
        if (diffTime >= time || deltaTime <= delayTime) {
            mCalendar.add(12, Long.valueOf(delayTime / EVERY_MINUTE).intValue());
        } else {
            mCalendar.add(12, Long.valueOf(deltaTime / EVERY_MINUTE).intValue());
        }
        scheduleAlarmAtTime(mCalendar.getTimeInMillis());
    }

    public void scheduleAlarmImmediately() {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "fileid : " + this.mFileId + " scheduleAlarmImmediately");
        }
        scheduleAlarmAtTime(System.currentTimeMillis());
    }

    @SuppressLint({"HwHardCodeDateFormat"})
    private void scheduleAlarmAtTime(long time) {
        ((AlarmManager) this.mContext.getSystemService("alarm")).set(0, time, this.mPIntent);
        if (HwLog.HWDBG) {
            HwLog.d("DownloadServiceTime", "ID:" + this.mFileId + ", scheduleAlarm : " + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Long.valueOf(time)));
        }
    }

    @SuppressLint({"HwHardCodeDateFormat"})
    public void reScheduleAlarmDelay() {
        AlarmManager mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (!isAutoClose()) {
            long delayTime = getRandomTime(RE_DELAY_TIME);
            Calendar mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mCalendar.add(12, Long.valueOf(delayTime / EVERY_MINUTE).intValue());
            mAlarmManager.set(0, mCalendar.getTimeInMillis(), this.mPIntent);
            if (HwLog.HWDBG) {
                HwLog.d("DownloadServiceTime", "ID:" + this.mFileId + ", reScheduleAlarmDelay : " + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(mCalendar.getTime()));
            }
        }
    }

    private long getRandomTime(long time) {
        return (time / 2) + ((long) new Random().nextInt(Long.valueOf(time).intValue()));
    }

    private void cancelAlarm() {
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mPIntent);
    }

    public int getItem() {
        return this.mPref.getInt(this.mAutoItemKey, 2);
    }

    public void setItem(int item, boolean isImmediate) {
        this.mPref.edit().putInt(this.mAutoItemKey, item).apply();
        if (item == 2) {
            cancelAlarm();
        } else if (isImmediate) {
            scheduleAlarmImmediately();
        } else {
            scheduleAutoUpdate();
        }
        switch (this.mFileId) {
            case DownloadService.MSG_OK /*3*/:
                StatisticalHelper.sendReport(4038, "CC," + item);
                return;
            case DownloadService.MSG_CANCEL /*4*/:
                StatisticalHelper.sendReport(4038, "YP," + item);
                return;
            default:
                return;
        }
    }

    public void setItem(int item) {
        setItem(item, false);
    }

    private boolean isAutoClose() {
        return getItem() == 2;
    }

    private boolean isWlanOnly() {
        return getItem() == 1;
    }

    private long getLastUpdatedtime() {
        return this.mPref.getLong(this.mLastUpdate, 0);
    }

    private void setLastUpdateTime(long time) {
        this.mPref.edit().putLong(this.mLastUpdate, time).apply();
    }

    public DownloadRequest contructRequest() {
        return new DownloadRequest(String.valueOf(this.mFileId), String.valueOf(this.mPref.getLong(this.mVerKey, 0)));
    }

    protected void updateCurVer(String ver) {
        this.mPref.edit().putLong(this.mVerKey, Long.parseLong(ver)).apply();
    }

    public boolean tryUpdate() {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "tryUpdate fileid : " + this.mFileId + " checkConnectivityStatus: " + NetWorkUtil.checkConnectivityStatus(this.mContext));
        }
        if (NetWorkUtil.checkConnectivityStatus(this.mContext)) {
            setLastUpdateTime(System.currentTimeMillis());
            scheduleAutoUpdate();
            if (isWlanOnly() && NetWorkUtil.isNetworkMobile(this.mContext)) {
                return false;
            }
            return true;
        }
        reScheduleAlarmDelay();
        return false;
    }

    public void handleComplete(DownloadResponse response) {
        updateCurVer(response.getVer());
        doFinishNotification(getSucessNoti());
    }

    public String getMessage() {
        return null;
    }

    public String getNegativeString() {
        return this.mContext.getString(17039360);
    }

    public String getPositiveString() {
        return null;
    }

    private void doFinishNotification(String title) {
        NotificationManager mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        Intent intent = new Intent();
        intent.setAction("com.android.contacts.action.LIST_DEFAULT");
        intent.setPackage(this.mContext.getPackageName());
        CommonUtilMethods.constructAndSendSummaryNotification(this.mContext);
        mNotificationManager.notify("DEFAULT_NOTIFICATION_TAG", this.mFileId, constructFinishNotification(this.mContext, title, intent));
    }

    private static Notification constructFinishNotification(Context context, String title, Intent intent) {
        Builder groupSummary = new Builder(context).setAutoCancel(true).setSmallIcon(new SeparatedResourceUtils().getBitampIcon(context, 101)).setContentTitle(title).setTicker(title).setGroup("group_key_contacts").setGroupSummary(false);
        if (intent == null) {
            intent = new Intent();
        }
        return groupSummary.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0)).getNotification();
    }
}
