package com.android.server.wifi.HwQoE;

import android.app.ActivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.os.RemoteException;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HiDataTrafficManager {
    private static final int INVALID_VALUE = -1;
    private static final long MIN_LIMIT_TIME = 259200000;
    public static final long MONTH_TIME = 2592000000L;
    public static final String TAG = "HiData_TrafficManager";
    private static final long WEEK_TIME = 604800000;
    private ActivityManager mActivityManager = ((ActivityManager) this.mContext.getSystemService("activity"));
    private Context mContext;
    private DefaultMobileTrafficInfo mDefaultMobileTrafficInfo = new DefaultMobileTrafficInfo();
    private boolean mIsBootCompled;
    private NetworkStats mNetworkStats;
    private NetworkStatsManager mNetworkStatsManager = ((NetworkStatsManager) this.mContext.getSystemService("netstats"));
    private TelephonyManager mTelephonyManager = TelephonyManager.from(this.mContext);
    private NetworkStats.Bucket summaryBucket;

    public static class DefaultMobileTrafficInfo {
        long averagLastMonthly;
        long averageLastWeek;
        long averageMonthly;
        long lastMonth;
        long lastWeek;
        long month;
        int subId;
        long today;
        int uid;
        boolean valid;

        public void clean() {
            this.subId = 0;
            this.today = 0;
            this.month = 0;
            this.lastWeek = 0;
            this.lastMonth = 0;
            this.averageMonthly = 0;
            this.averagLastMonthly = 0;
            this.averageLastWeek = 0;
            this.uid = 0;
            this.valid = false;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("valid: ");
            sb.append(this.valid);
            sb.append(" ,today: ");
            sb.append(this.today);
            sb.append(" Bytes, LastWeek: ");
            sb.append(this.lastWeek);
            sb.append(" Bytes, month: ");
            sb.append(this.month);
            sb.append(" Bytes, averageLastWeek: ");
            sb.append(this.averageLastWeek);
            sb.append(" Bytes, averageMonthly: ");
            sb.append(this.averageMonthly);
            sb.append(" Bytes, lastMonth: ");
            sb.append(this.lastMonth);
            sb.append(" Bytes, averagLastMonthly: ");
            sb.append(this.averagLastMonthly);
            sb.append(" Bytes, uid: ");
            sb.append(this.uid);
            return sb.toString();
        }
    }

    public HiDataTrafficManager(Context context) {
        this.mContext = context;
    }

    public synchronized void systemBootCompled() {
        this.mIsBootCompled = true;
    }

    public synchronized DefaultMobileTrafficInfo getDefaultMobileTrafficInfo(int uid) {
        synchronized (this) {
            if (!this.mIsBootCompled) {
                Log.e(TAG, "DefaultMobileTrafficInfo,Boot is not Compled");
                return null;
            }
            this.mDefaultMobileTrafficInfo.clean();
            this.mDefaultMobileTrafficInfo.subId = SubscriptionManager.getDefaultDataSubscriptionId();
            String imsi = this.mTelephonyManager.getSubscriberId(this.mDefaultMobileTrafficInfo.subId);
            if (TextUtils.isEmpty(imsi)) {
                Log.e(TAG, "imsi is Empty");
                return null;
            }
            if (getMobileTraffic(imsi, 0, getTimeFromNow(MIN_LIMIT_TIME), uid) <= 0) {
                this.mDefaultMobileTrafficInfo.valid = false;
                Log.d(TAG, "MobileTrafficInfo is invalid ");
                DefaultMobileTrafficInfo defaultMobileTrafficInfo = this.mDefaultMobileTrafficInfo;
                return defaultMobileTrafficInfo;
            }
            this.mDefaultMobileTrafficInfo.valid = true;
            long now = System.currentTimeMillis();
            this.mDefaultMobileTrafficInfo.today = getMobileTraffic(imsi, getTimesMorning(), now, uid);
            this.mDefaultMobileTrafficInfo.lastWeek = getMobileTraffic(imsi, getTimeFromNow(WEEK_TIME), now, uid);
            this.mDefaultMobileTrafficInfo.lastMonth = getMobileTraffic(imsi, getTimeFromNow(MONTH_TIME), now, uid);
            this.mDefaultMobileTrafficInfo.month = getMobileTraffic(imsi, getTimesMonthMorning(), now, uid);
            this.mDefaultMobileTrafficInfo.uid = uid;
            int day = getDayFromMonth() - 1;
            if (day > 0) {
                this.mDefaultMobileTrafficInfo.averageMonthly = this.mDefaultMobileTrafficInfo.month / ((long) day);
            } else if (day == 0) {
                this.mDefaultMobileTrafficInfo.averageMonthly = this.mDefaultMobileTrafficInfo.month;
            }
            this.mDefaultMobileTrafficInfo.averageLastWeek = this.mDefaultMobileTrafficInfo.lastWeek / 7;
            this.mDefaultMobileTrafficInfo.averagLastMonthly = this.mDefaultMobileTrafficInfo.lastMonth / 30;
            Log.d(TAG, "MobileTrafficInfo: " + this.mDefaultMobileTrafficInfo.toString());
            DefaultMobileTrafficInfo defaultMobileTrafficInfo2 = this.mDefaultMobileTrafficInfo;
            return defaultMobileTrafficInfo2;
        }
    }

    private long getMobileTraffic(String subscriberId, long startTime, long endTime, int uid) {
        int i = uid;
        if (TextUtils.isEmpty(subscriberId)) {
            Log.e(TAG, "subscriberId is Empty");
            return -1;
        }
        try {
            this.summaryBucket = new NetworkStats.Bucket();
            this.mNetworkStats = this.mNetworkStatsManager.querySummary(0, subscriberId, startTime, endTime);
            long totalBytes = 0;
            long rxBytes = 0;
            long txBytes = 0;
            if (this.mNetworkStats != null) {
                do {
                    this.mNetworkStats.getNextBucket(this.summaryBucket);
                    if (i == 0) {
                        rxBytes += this.summaryBucket.getRxBytes();
                        txBytes += this.summaryBucket.getTxBytes();
                    } else if (i > 0 && i == this.summaryBucket.getUid()) {
                        rxBytes += this.summaryBucket.getRxBytes();
                        txBytes += this.summaryBucket.getTxBytes();
                    }
                } while (this.mNetworkStats.hasNextBucket());
                totalBytes = rxBytes + txBytes;
            } else {
                Log.e(TAG, "mNetworkStats == null");
            }
            return totalBytes;
        } catch (SecurityException e) {
            Log.e(TAG, "getMobileTraffic Exception", e);
            return -1;
        } catch (RemoteException e2) {
            Log.e(TAG, "getMobileTraffic Exception", e2);
            return -1;
        }
    }

    public static long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(1), cal.get(2), cal.get(5), 0, 0, 0);
        cal.set(5, cal.getActualMinimum(5));
        return cal.getTimeInMillis();
    }

    public static long getTimesWeekMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(1), cal.get(2), cal.get(5), 0, 0, 0);
        cal.set(7, 2);
        return cal.getTimeInMillis();
    }

    public static long getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    public static long getWeekFromNow() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getTimesMorning() - WEEK_TIME);
        return cal.getTimeInMillis();
    }

    public static long getTimeFromNow(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() - time);
        return cal.getTimeInMillis();
    }

    public static long getMinValidDayFromNow() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getTimesMorning() - MIN_LIMIT_TIME);
        return cal.getTimeInMillis();
    }

    public static int getDayFromMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(5);
    }

    public static String utcForLocalDate(long utc) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date(utc));
    }

    public String getAppNameUid(int uid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
