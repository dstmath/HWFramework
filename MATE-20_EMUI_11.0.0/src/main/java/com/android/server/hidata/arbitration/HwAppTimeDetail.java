package com.android.server.hidata.arbitration;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.IHidataCallback;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.systemmanager.netassistant.HwNetworkManager;
import com.huawei.systemmanager.netassistant.INetworkPolicyManager;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAppTimeDetail {
    private static final String APK = "APK";
    private static final String APK_INFO = "APKINFO";
    private static final String APP_CHR_STATICS = "APP_CHR_STATICS";
    private static final Object APP_LOCK = new Object();
    private static final String APP_PATH_SETTINGS = "/apps_settings";
    private static final String APP_SELECT_CONDITIONS = "name=?";
    private static final String AUDIO_IN_CELL = "AUDIOINCELL";
    private static final String AUDIO_IN_WIFI = "AUDIOINWIFI";
    private static final int BETA_USER = 3;
    private static final int CHR_APP_COUNT = 10;
    private static final String CLOSE_CELL_CNT = "CLOSECELLCNT";
    private static final String CODE_FORMAT = "UTF-8";
    private static final int COMMERCIAL_USER = 1;
    private static final String CONSUME_CELL = "CONSUMECELL";
    private static final String CONSUME_WIFI = "CONSUMEWIFI";
    private static final String CONTENT_NOLIMIT = "no_limit_switch";
    private static final String CONTENT_TOTAL_SWITCH = "total_switch";
    private static final Uri CONTENT_URI = Uri.parse("content://com.huawei.systemserver.emcom.networkengine.SmartNetworkProvider/total_settings");
    private static final String CONTENT_VALUE = "value";
    private static final int COVERT_MB = 1048576;
    private static final int COVERT_SECOND = 1000;
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private static final String DUR_CELL = "DURCELL";
    private static final String DUR_WIFI = "DURWIFI";
    private static final int EVENT_APP_CHR_STATICS = 909001009;
    private static final int EVENT_APP_DETAIL_STATICS = 909009145;
    private static final Object FILE_LOCK = new Object();
    private static final String GIVE_LUCKY_MONEY = "GIVELUCKYMONEY";
    private static final String HOME_AP_CNT = "HOMEAPCNT";
    private static final String INFO_SETTINGS = "/settinginfo";
    private static final int INTERVAL_SPEEDTEST = 3000;
    private static final long INTERVAL_TIME = 21600000;
    private static final int INVALID_NETWORK = -2;
    private static final int INVALID_VALUE = -1;
    private static final String JSON_FILE_PATH = "/data/system/app.json";
    private static final String LIMIT_TIME = "LIMITTIME";
    private static final int MAX_APP_COUNT = 100;
    private static final int MAX_RECORDS_COUNT = 4;
    private static final String NET_ACCS_WITCH = "NETACCSWITCH";
    private static final String NET_APP_ALLOW = "NETAPPALLOW";
    private static final String NET_APP_LIMIT = "NETAPPLIMIT";
    private static final Uri NET_MONTH_LIMIT_BYTE_URI = Uri.parse("content://com.huawei.systemmanager.NetAssistantProvider/settinginfo");
    private static final int NOT_EXIST = -3;
    private static final String NO_LIMIT = "NOLIMIT";
    private static final String OPEN_CELL_CNT = "OPENCELLCNT";
    private static final String OPEN_MOBILE_CNT = "OPENMOBILECNT";
    private static final String OVERSPEED_CELL = "OVERSPEEDCELL";
    private static final String OVERSPEED_WIFI = "OVERSPEEDWIFI";
    private static final String PATH_SETTINGS = "/total_settings";
    private static final String REC_LUCKY_MONEY = "RECLUCKYMONEY";
    private static final String REMAIN_DATA = "REMAINDATA";
    private static final String REMAIN_SLAVE_DATA = "REMAINSLAVEDATA";
    private static final String SCHEME = "content://";
    private static final String SETTINGS_AUTHORITY = "com.huawei.systemserver.emcom.networkengine.SmartNetworkProvider";
    private static final String SETTINGS_AUTHORITY1 = "com.huawei.systemmanager.NetAssistantProvider";
    private static final String SLAVE_NO_LIMIT = "SLAVENOLIMIT";
    private static final String SMART_SAVE = "SMARTSAVE";
    private static final String STAY_MOBLIE_TIME = "STAYMOBLIETIME";
    private static final String STAY_SOFTAP_TIME = "STAYSOFTAPTIME";
    private static final int SWITCH_DEFAULT = -1;
    private static final int SWITCH_OPEN = 1;
    private static final String TAG = "HiData_HwAppTimeDetail";
    private static final String TIME_STAMP = "TIMESTAMP";
    private static final String TOTAL_CELL_DATA = "TOTALCELLDATA";
    private static final String TOTAL_SLAVE_CELL_DATA = "TOTALSLAVECELLDATA";
    private static final String TOTAL_WIFI_DATA = "TOTALWIFIDATA";
    private static final String VIDEO_IN_CELL = "VIDEOINCELL";
    private static final String VIDEO_IN_WIFI = "VIDEOINWIFI";
    private static final String WIFI_AP_LIMIT = "WIFIAPLIMIT";
    private static final int WINDOW_SIZE = 6;
    private static final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private static int[] allowdUids = {0};
    private static int curNetwork = 802;
    private static long curTotalMobileByte = 0;
    private static long curTotalWiFiByte = 0;
    private static int curUid;
    private static int dataSaverSwitch = 0;
    private static int homeApCnt = 0;
    private static boolean isAppRunning = false;
    private static boolean isBetaType = false;
    private static boolean isVideoScene = false;
    private static long lastMoblieTime = 0;
    private static long lastSoftApTime = 0;
    private static long lastTime = 0;
    private static long lastTotalMobileByte = 0;
    private static long lastTotalWiFiByte = 0;
    private static long lastUploadTime = 0;
    private static int masterSubId = -1;
    private static int mobileTraffic = -1;
    private static long monthLimitByte = -1;
    private static int openMobileCnt = 0;
    private static int overSpeedTimeInCell = 0;
    private static int overSpeedTimeInWiFi = 0;
    private static long remainData = -1;
    private static long remainSlaveData = -1;
    private static int[] restrictedUids = {0};
    private static HwAppTimeDetail sHwAppTimeDetail;
    private static int slaveMobileTraffic = -1;
    private static long slaveMonthLimitByte = -1;
    private static int slaveSubId = -1;
    private static String slaveSubImsi = null;
    private static long stayMoblieTime = 0;
    private static long staySoftApTime = 0;
    private static String subImsi = null;
    private static int totalMobileByte = -1;
    private static int totalSlaveMobileByte = -1;
    private static int totalWiFiByte = -1;
    private static int userType = 1;
    private String curPkgName;
    private HwAPPStateInfo mAppInfo;
    private BroadcastReceiver mBroadcastReceiver = new StateBroadcastReceiver();
    private Context mContext;
    private IHidataCallback mHidataCallback;
    private ArrayList<HwPackageInfo> mPackageInfoList = new ArrayList<>();
    private PackageManager mPackageManager;
    private INetworkPolicyManager mPolicyManager;
    private ContentResolver mResolver;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private UserDataEnableObserver userDataEnableObserver;

    public HwAppTimeDetail(Context context) {
        this.mContext = context;
        this.mPolicyManager = HwNetworkManager.getNetworkPolicyManager(this.mContext);
        this.mPackageManager = this.mContext.getPackageManager();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.userDataEnableObserver = new UserDataEnableObserver(HwArbitrationStateMachine.getInstance(context).getHandler());
        this.userDataEnableObserver.register();
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            isBetaType = true;
        }
    }

    public static synchronized HwAppTimeDetail createInstance(Context context) {
        HwAppTimeDetail hwAppTimeDetail;
        synchronized (HwAppTimeDetail.class) {
            if (sHwAppTimeDetail == null) {
                sHwAppTimeDetail = new HwAppTimeDetail(context);
            }
            hwAppTimeDetail = sHwAppTimeDetail;
        }
        return hwAppTimeDetail;
    }

    public static HwAppTimeDetail getInstance() {
        return sHwAppTimeDetail;
    }

    private class UserDataEnableObserver extends ContentObserver {
        private UserDataEnableObserver(Handler handler) {
            super(handler);
            HwAppTimeDetail.this.mResolver = HwAppTimeDetail.this.mContext.getContentResolver();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isChange) {
            HwAppTimeDetail.this.moblieStateChange();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void register() {
            HwAppTimeDetail.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        private void unregister() {
            HwAppTimeDetail.this.mResolver.unregisterContentObserver(this);
        }
    }

    private class StateBroadcastReceiver extends BroadcastReceiver {
        private StateBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                HwAppTimeDetail.this.notifyWifiApStateChange(intent);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWifiApStateChange(Intent intent) {
        int wifiState = intent.getIntExtra("wifi_state", 14);
        if (wifiState == 13) {
            lastSoftApTime = SystemClock.elapsedRealtime();
            HwHiLog.i(TAG, false, "User start softap", new Object[0]);
        } else if (wifiState == 11) {
            staySoftApTime += (SystemClock.elapsedRealtime() - lastSoftApTime) / 1000;
            HwHiLog.i(TAG, false, "User staySoftApTime %{public}d", new Object[]{Long.valueOf(staySoftApTime)});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void moblieStateChange() {
        int mobileState = Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", -1);
        if (mobileState == 1) {
            openMobileCnt++;
        } else if (mobileState == 0 && openMobileCnt > 0) {
            stayMoblieTime += (SystemClock.elapsedRealtime() - lastMoblieTime) / 1000;
        }
        lastMoblieTime = SystemClock.elapsedRealtime();
        HwHiLog.i(TAG, false, "moblieStateChange openMobileCnt %{public}d stayMoblieTime %{public}d", new Object[]{Integer.valueOf(openMobileCnt), Long.valueOf(stayMoblieTime)});
    }

    private void updateMobileUseTime() {
        int mobileState = Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", -1);
        if (mobileState == 1) {
            stayMoblieTime += (SystemClock.elapsedRealtime() - lastMoblieTime) / 1000;
            lastMoblieTime = SystemClock.elapsedRealtime();
        }
        HwHiLog.i(TAG, false, "updateMobileUseTime mobileState %{public}d stayMoblieTime %{public}d", new Object[]{Integer.valueOf(mobileState), Long.valueOf(stayMoblieTime)});
    }

    private static int isNoLimitSwitchEnabled(Context context, String subImsi2) {
        int noLimitSwitch = -1;
        if (context == null) {
            HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled context is null.", new Object[0]);
            return -1;
        }
        Cursor cursor = null;
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled resolver is null.", new Object[0]);
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e) {
                    HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled close exception", new Object[0]);
                }
            }
            return -1;
        }
        try {
            Cursor cursor2 = resolver.query(NET_MONTH_LIMIT_BYTE_URI, null, null, null, null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                int imsiIndex = cursor2.getColumnIndex("imsi");
                int switchIndex = cursor2.getColumnIndex(CONTENT_NOLIMIT);
                while (true) {
                    if (cursor2.moveToNext()) {
                        String imsi = cursor2.getString(imsiIndex);
                        if (imsi != null && imsi.equals(subImsi2)) {
                            noLimitSwitch = cursor2.getInt(switchIndex);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (cursor2 != null) {
                try {
                    cursor2.close();
                } catch (SQLiteException e2) {
                    HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled close exception", new Object[0]);
                }
            }
            return noLimitSwitch;
        } catch (SQLiteException | IllegalArgumentException e3) {
            HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled SQLiteException or IllegalArgumentException", new Object[0]);
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e4) {
                    HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled close exception", new Object[0]);
                }
            }
            return -1;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e5) {
                    HwHiLog.e(TAG, false, "isNoLimitSwitchEnabled close exception", new Object[0]);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x0099 A[SYNTHETIC, Splitter:B:50:0x0099] */
    private static long getMonthLimitByte(Context context, String subImsi2) {
        SQLiteException e;
        Object e2;
        long limitByte = -1;
        if (context == null) {
            HwHiLog.e(TAG, false, "isMonthLimitByte context is null.", new Object[0]);
            return -1;
        }
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                HwHiLog.e(TAG, false, "isMonthLimitByte resolver is null.", new Object[0]);
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e3) {
                        HwHiLog.e(TAG, false, "isMonthLimitByte close exception", new Object[0]);
                    }
                }
                return -1;
            }
            try {
                cursor = resolver.query(NET_MONTH_LIMIT_BYTE_URI, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    int imsiIndex = cursor.getColumnIndex("imsi");
                    int limitByteIndex = cursor.getColumnIndex("month_limit_byte");
                    while (true) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        String imsi = cursor.getString(imsiIndex);
                        if (imsi != null) {
                            try {
                                if (imsi.equals(subImsi2)) {
                                    limitByte = cursor.getLong(limitByteIndex);
                                    break;
                                }
                            } catch (SQLiteException | IllegalArgumentException e4) {
                                e2 = e4;
                                try {
                                    HwHiLog.e(TAG, false, "isMonthLimitByte SQLiteException or IllegalArgumentException", new Object[0]);
                                    if (cursor != null) {
                                    }
                                    return -1;
                                } catch (Throwable th) {
                                    e = th;
                                }
                            }
                        }
                    }
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e5) {
                        HwHiLog.e(TAG, false, "isMonthLimitByte close exception", new Object[0]);
                    }
                }
                return limitByte;
            } catch (SQLiteException | IllegalArgumentException e6) {
                e2 = e6;
                HwHiLog.e(TAG, false, "isMonthLimitByte SQLiteException or IllegalArgumentException", new Object[0]);
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e7) {
                        HwHiLog.e(TAG, false, "isMonthLimitByte close exception", new Object[0]);
                    }
                }
                return -1;
            }
        } catch (Throwable th2) {
            e = th2;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (SQLiteException e8) {
                    HwHiLog.e(TAG, false, "isMonthLimitByte close exception", new Object[0]);
                }
            }
            throw e;
        }
    }

    private static void updateUserSetting(Context context) {
        mobileTraffic = isNoLimitSwitchEnabled(context, subImsi);
        slaveMobileTraffic = isNoLimitSwitchEnabled(context, slaveSubImsi);
        HwHiLog.i(TAG, false, "master card no limit switch is %{public}d; slave card no limit switch is %{public}d", new Object[]{Integer.valueOf(mobileTraffic), Integer.valueOf(slaveMobileTraffic)});
        monthLimitByte = getMonthLimitByte(context, subImsi);
        slaveMonthLimitByte = getMonthLimitByte(context, slaveSubImsi);
        HwHiLog.i(TAG, false, "master card month limit byte is %{public}d; slave card month limit byte is %{public}d", new Object[]{Long.valueOf(monthLimitByte), Long.valueOf(slaveMonthLimitByte)});
        long j = monthLimitByte;
        if (j == -1) {
            remainData = -1;
        } else {
            remainData = (long) (((int) (j - ((long) totalMobileByte))) / 1048576);
            if (remainData < 0) {
                remainData = 0;
            }
        }
        long j2 = slaveMonthLimitByte;
        if (j2 == -1) {
            remainSlaveData = -1;
        } else {
            remainSlaveData = (long) (((int) (j2 - ((long) totalSlaveMobileByte))) / 1048576);
            if (remainSlaveData < 0) {
                remainSlaveData = 0;
            }
        }
        HwHiLog.i(TAG, false, "master card remainData is %{public}dM; slave card remainData is %{public}dM", new Object[]{Long.valueOf(remainData), Long.valueOf(remainSlaveData)});
    }

    private static int isTotalLinkTurboEnabled(Context context) {
        if (context == null) {
            HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled context is null.", new Object[0]);
            return 0;
        }
        int totalSwitch = -1;
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled resolver is null.", new Object[0]);
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (SQLiteException e) {
                        HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                    }
                }
                return 0;
            }
            Cursor cursor2 = resolver.query(CONTENT_URI, new String[]{CONTENT_TOTAL_SWITCH}, null, null, null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                int index = cursor2.getColumnIndex("value");
                cursor2.moveToFirst();
                totalSwitch = cursor2.getInt(index);
            }
            if (cursor2 != null) {
                try {
                    cursor2.close();
                } catch (SQLiteException e2) {
                    HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                }
            }
            HwHiLog.i(TAG, false, "isTotalLinkTurboEnabled:%{public}d", new Object[]{Integer.valueOf(totalSwitch)});
            return totalSwitch;
        } catch (SQLiteException e3) {
            HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled SQLiteException", new Object[0]);
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e4) {
                    HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                }
            }
            return 0;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    cursor.close();
                } catch (SQLiteException e5) {
                    HwHiLog.e(TAG, false, "isTotalLinkTurboEnabled close exception", new Object[0]);
                }
            }
            throw th;
        }
    }

    private long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(1), cal.get(2), cal.get(5), 0, 0, 0);
        cal.set(5, cal.getActualMinimum(5));
        return cal.getTimeInMillis();
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a0  */
    private int getUidUsedBytes(String subscriberId, int uid, int networkType) {
        Exception e;
        NetworkStats detailStats;
        int i = uid;
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) this.mContext.getSystemService("netstats");
        if (!(networkStatsManager instanceof NetworkStatsManager)) {
            return 0;
        }
        long endUidTimeStamp = System.currentTimeMillis();
        NetworkStats.Bucket summaryBucket = new NetworkStats.Bucket();
        NetworkStats detailStats2 = null;
        if (networkType == 800) {
            try {
                detailStats = networkStatsManager.querySummary(1, "", getTimesMonthMorning(), endUidTimeStamp);
            } catch (RemoteException | SecurityException e2) {
                try {
                    HwHiLog.e(TAG, false, "getUidUsedBytes error", new Object[0]);
                    if (detailStats2 != null) {
                        detailStats2.close();
                    }
                    return 0;
                } catch (Throwable th) {
                    e = th;
                }
            }
        } else if (networkType != 801 || subscriberId == null) {
            detailStats = null;
        } else {
            detailStats = networkStatsManager.querySummary(0, subscriberId, getTimesMonthMorning(), endUidTimeStamp);
        }
        long rxBytes = 0;
        long txBytes = 0;
        if (detailStats != null) {
            while (true) {
                try {
                    detailStats.getNextBucket(summaryBucket);
                    if (i > 0 && i == summaryBucket.getUid()) {
                        rxBytes += summaryBucket.getRxBytes();
                        txBytes += summaryBucket.getTxBytes();
                    }
                    if (!detailStats.hasNextBucket()) {
                        int i2 = (int) ((rxBytes + txBytes) / 1048576);
                        detailStats.close();
                        return i2;
                    }
                    i = uid;
                } catch (RemoteException | SecurityException e3) {
                    detailStats2 = detailStats;
                    HwHiLog.e(TAG, false, "getUidUsedBytes error", new Object[0]);
                    if (detailStats2 != null) {
                    }
                    return 0;
                } catch (Throwable th2) {
                    e = th2;
                    detailStats2 = detailStats;
                    if (detailStats2 != null) {
                        detailStats2.close();
                    }
                    throw e;
                }
            }
        } else {
            if (detailStats != null) {
                detailStats.close();
            }
            return 0;
        }
    }

    private int getUsedAllBytes(String subscriberId, int networkType) {
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) this.mContext.getSystemService("netstats");
        if (!(networkStatsManager instanceof NetworkStatsManager)) {
            return 0;
        }
        NetworkStats.Bucket bucket = null;
        if (networkType == 800) {
            try {
                bucket = networkStatsManager.querySummaryForDevice(1, "", getTimesMonthMorning(), System.currentTimeMillis());
            } catch (RemoteException | SecurityException e) {
                HwHiLog.e(TAG, false, "getUsedAllBytes error", new Object[0]);
                return 0;
            }
        } else if (networkType == 801) {
            bucket = networkStatsManager.querySummaryForDevice(0, subscriberId, getTimesMonthMorning(), System.currentTimeMillis());
        }
        if (bucket != null) {
            return (int) ((bucket.getRxBytes() + bucket.getTxBytes()) / 1048576);
        }
        HwHiLog.e(TAG, false, "bucket is null", new Object[0]);
        return 0;
    }

    private void updateAllUsedData() {
        curTotalWiFiByte = (TrafficStats.getRxBytes(WLAN_IFACE) + TrafficStats.getTxBytes(WLAN_IFACE)) / 1048576;
        long tempMoblieType = (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()) / 1048576;
        if (tempMoblieType != 0) {
            curTotalMobileByte = tempMoblieType;
        }
    }

    public void handleTimeThread() {
        updateAllUsedData();
        if (SystemClock.elapsedRealtime() - lastTime >= 3000) {
            lastTime = SystemClock.elapsedRealtime();
            if (curTotalWiFiByte - lastTotalWiFiByte > 1 || curTotalMobileByte - lastTotalMobileByte > 1) {
                int i = curNetwork;
                if (i == 800) {
                    overSpeedTimeInWiFi++;
                } else if (i == 801) {
                    overSpeedTimeInCell++;
                }
                HwHiLog.i(TAG, false, "overSpeedTimeInWiFi %{public}d overSpeedTimeInCell %{public}d", new Object[]{Integer.valueOf(overSpeedTimeInWiFi), Integer.valueOf(overSpeedTimeInCell)});
            }
            if (!isAppRunning && !isVideoScene) {
                uploadChrApkInfo();
            }
            lastTotalWiFiByte = curTotalWiFiByte;
            lastTotalMobileByte = curTotalMobileByte;
        }
    }

    public void notifyNetworkChange(int netwokType) {
        if (netwokType == curNetwork || netwokType == 802) {
            HwHiLog.i(TAG, false, "change network last curNetwork the same or not care", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "curNetwork = " + netwokType, new Object[0]);
        if ((!isAppRunning && !isVideoScene) || this.curPkgName == null || curUid == 0) {
            curNetwork = netwokType;
            return;
        }
        HwHiLog.i(TAG, false, "app is currently running while networkchange", new Object[0]);
        getAppUseTime(this.curPkgName, curUid);
        if (isVideoScene) {
            streamMediaStopTime(this.mAppInfo);
            curNetwork = netwokType;
            streamMediaStartTime(this.mAppInfo);
        } else {
            curNetwork = netwokType;
        }
        startAppTime(this.curPkgName, curUid);
    }

    private void updateLastUsedData(HwPackageInfo info, int uid) {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            subImsi = telephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        }
        info.setLastUsedDataInWiFi(getUidUsedBytes(subImsi, uid, 800));
        info.setLastUsedDataInCell(getUidUsedBytes(subImsi, uid, 801));
        HwHiLog.i(TAG, false, "Package %{public}s consume wifi %{public}dMB consume cell %{public}dMB", new Object[]{info.getPackageName(), Integer.valueOf(info.getLastUsedDataInWiFi()), Integer.valueOf(info.getLastUsedDataInCell())});
    }

    private void updateUsedData(HwPackageInfo info) {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            subImsi = telephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        }
        int calcRxByte = getUidUsedBytes(subImsi, info.getPkgUid(), 800) - info.getLastUsedDataInWiFi();
        if (calcRxByte > 0) {
            info.setUsedData(800, calcRxByte);
        }
        int calcRxByte2 = getUidUsedBytes(subImsi, info.getPkgUid(), 801) - info.getLastUsedDataInCell();
        if (calcRxByte2 > 0) {
            info.setUsedData(801, calcRxByte2);
        }
        HwHiLog.i(TAG, false, "Package %{public}s consume wifi %{public}dMB consume cell %{public}dMB", new Object[]{info.getPackageName(), Integer.valueOf(info.getUsedDataInWiFi()), Integer.valueOf(info.getUsedDataInCell())});
    }

    private int searchOrAddPkgList(String pkgName, int uid) {
        int index = isExistPackageIndex(pkgName);
        if (index != -3) {
            return index;
        }
        if (this.mPackageInfoList.size() >= 100) {
            Collections.sort(this.mPackageInfoList);
            ArrayList<HwPackageInfo> arrayList = this.mPackageInfoList;
            HwHiLog.i(TAG, false, "Package is over remove not often use %{public}s", new Object[]{arrayList.get(arrayList.size() - 1).getPackageName()});
            ArrayList<HwPackageInfo> arrayList2 = this.mPackageInfoList;
            arrayList2.remove(arrayList2.size() - 1);
        }
        HwPackageInfo info = new HwPackageInfo(pkgName, 0, uid);
        updateLastUsedData(info, uid);
        this.mPackageInfoList.add(info);
        int index2 = this.mPackageInfoList.size() - 1;
        HwHiLog.i(TAG, false, "addAppName = %{public}s curPkgIndex = %{public}d", new Object[]{pkgName, Integer.valueOf(index2)});
        return index2;
    }

    private int isExistPackageIndex(String pkgName) {
        if (pkgName == null) {
            HwHiLog.e(TAG, false, "isExistPackageIndex pkgName null", new Object[0]);
            return -1;
        } else if (curNetwork == 802) {
            return -2;
        } else {
            for (int index = 0; index < this.mPackageInfoList.size(); index++) {
                if (this.mPackageInfoList.get(index).getPackageName().equals(pkgName)) {
                    HwHiLog.i(TAG, false, "pkgName is exist %{public}d", new Object[]{Integer.valueOf(index)});
                    return index;
                }
            }
            return -3;
        }
    }

    private void cleanPackageList() {
        synchronized (APP_LOCK) {
            if (!isAppRunning && !isVideoScene && this.mPackageInfoList != null) {
                this.mPackageInfoList.clear();
            }
        }
    }

    public void streamMediaStartTime(HwAPPStateInfo appInfo) {
        synchronized (APP_LOCK) {
            if (appInfo == null) {
                HwHiLog.e(TAG, false, "streamMediaStartTime appInfo is null", new Object[0]);
                return;
            }
            int pkgIndex = isExistPackageIndex(this.mPackageManager.getNameForUid(appInfo.mAppUID));
            if (pkgIndex < 0) {
                HwHiLog.e(TAG, false, "streamMediaStartTime curPkgName error!", new Object[0]);
                return;
            }
            this.mAppInfo = appInfo;
            isVideoScene = true;
            this.mPackageInfoList.get(pkgIndex).setVideoStartTime(curNetwork, appInfo.mScenceId, (int) (SystemClock.elapsedRealtime() / 1000));
            HwHiLog.i(TAG, false, "streamMediaStartTime curNetwork = %{public}d ScenceId = %{public}d", new Object[]{Integer.valueOf(curNetwork), Integer.valueOf(appInfo.mScenceId)});
        }
    }

    public void streamMediaStopTime(HwAPPStateInfo appInfo) {
        synchronized (APP_LOCK) {
            if (appInfo == null) {
                HwHiLog.e(TAG, false, "streamMediaStopTime appInfo is null", new Object[0]);
                return;
            }
            int pkgIndex = isExistPackageIndex(this.mPackageManager.getNameForUid(appInfo.mAppUID));
            if (pkgIndex < 0) {
                HwHiLog.i(TAG, false, "streamMediaStopTime curPkgName error!", new Object[0]);
                return;
            }
            int useTime = (int) ((SystemClock.elapsedRealtime() / 1000) - ((long) this.mPackageInfoList.get(pkgIndex).getVideoStartTime(curNetwork, appInfo.mScenceId)));
            int lastUsetime = this.mPackageInfoList.get(pkgIndex).getVideoUsedTime(curNetwork, appInfo.mScenceId);
            this.mPackageInfoList.get(pkgIndex).setVideoUsedTime(curNetwork, appInfo.mScenceId, lastUsetime + useTime);
            HwHiLog.i(TAG, false, "getVideoUseTime = %{public}d lastUsetime = %{public}d", new Object[]{Integer.valueOf(useTime), Integer.valueOf(lastUsetime)});
            isVideoScene = false;
        }
    }

    public synchronized void registWifiBoostCallback(IHidataCallback callback) {
        if (this.mHidataCallback == null) {
            this.mHidataCallback = callback;
        }
    }

    public void startAppTime(String pkgName, int uid) {
        synchronized (APP_LOCK) {
            int pkgIndex = searchOrAddPkgList(pkgName, uid);
            if (pkgIndex < 0) {
                HwHiLog.i(TAG, false, "startAppTime curPkgName not care!", new Object[0]);
                return;
            }
            isAppRunning = true;
            this.mPackageInfoList.get(pkgIndex).addCount();
            this.mPackageInfoList.get(pkgIndex).setStartTime(curNetwork, (int) (SystemClock.elapsedRealtime() / 1000));
            this.curPkgName = pkgName;
            curUid = uid;
        }
    }

    public void getAppUseTime(String pkgName, int uid) {
        synchronized (APP_LOCK) {
            int pkgIndex = isExistPackageIndex(pkgName);
            if (pkgIndex >= 0) {
                int useTime = (int) ((SystemClock.elapsedRealtime() / 1000) - ((long) this.mPackageInfoList.get(pkgIndex).getStartTime(curNetwork)));
                int lastUseTime = this.mPackageInfoList.get(pkgIndex).getUsedTime(curNetwork);
                HwHiLog.i(TAG, false, "getUseTime = %{public}d lastUsetime = %{public}d", new Object[]{Integer.valueOf(useTime), Integer.valueOf(lastUseTime)});
                this.mPackageInfoList.get(pkgIndex).setUsedTime(curNetwork, lastUseTime + useTime);
                HwHiLog.i(TAG, false, "mPackageInfoList = %{public}s curNetwork %{public}d", new Object[]{this.mPackageInfoList.get(pkgIndex).toString(), Integer.valueOf(curNetwork)});
                isAppRunning = false;
            }
        }
    }

    private void cleanVaram() {
        openMobileCnt = 0;
        overSpeedTimeInWiFi = 0;
        overSpeedTimeInCell = 0;
        stayMoblieTime = 0;
        staySoftApTime = 0;
    }

    private void updateHomeApCount() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "mWifiManager is null", new Object[0]);
            return;
        }
        List<WifiConfiguration> configNetworks = wifiManager.getConfiguredNetworks();
        if (configNetworks == null) {
            HwHiLog.e(TAG, false, "updateHomeApCount configNetworks is null", new Object[0]);
            return;
        }
        homeApCnt = 0;
        int configNetLength = configNetworks.size();
        for (int i = 0; i < configNetLength; i++) {
            if (WifiProCommonUtils.isWpaOrWpa2(configNetworks.get(i))) {
                homeApCnt++;
            }
        }
        HwHiLog.i(TAG, false, "total wifi count %{public}d homeApCnt = %{public}d", new Object[]{Integer.valueOf(configNetworks.size()), Integer.valueOf(homeApCnt)});
    }

    private static int convertSlotIdToSubId(int slotId) {
        if (!HwArbitrationDEFS.isValidSlotId(slotId)) {
            HwHiLog.e(TAG, false, "convertSlotIdToSubId, Invalid slotId: %{public}d", new Object[]{Integer.valueOf(slotId)});
            return -1;
        } else if (slotId == 2) {
            HwHiLog.e(TAG, false, "convertSlotIdToSubId, vsim slotId: %{public}d to VSIM_SubId", new Object[]{Integer.valueOf(slotId)});
            return 999999;
        } else {
            int[] subIds = SubscriptionManagerEx.getSubId(slotId);
            if (subIds == null || subIds.length <= 0) {
                return -1;
            }
            return subIds[0];
        }
    }

    private static int getSlaveSubId() {
        int subId = -1;
        if (convertSlotIdToSubId(0) == masterSubId && convertSlotIdToSubId(1) != -1) {
            subId = convertSlotIdToSubId(1);
        }
        if (convertSlotIdToSubId(1) != masterSubId || convertSlotIdToSubId(0) == -1) {
            return subId;
        }
        return convertSlotIdToSubId(0);
    }

    private void updateDataSaver() {
        INetworkPolicyManager iNetworkPolicyManager = this.mPolicyManager;
        if (iNetworkPolicyManager != null) {
            if (iNetworkPolicyManager.getRestrictBackground()) {
                dataSaverSwitch = 1;
            } else {
                dataSaverSwitch = 0;
            }
            restrictedUids = this.mPolicyManager.getUidsWithPolicy(1);
            allowdUids = this.mPolicyManager.getUidsWithPolicy(4);
            HwHiLog.i(TAG, false, "switch %{public}d restrict = %{public}s allow = %{public}s", new Object[]{Integer.valueOf(dataSaverSwitch), Arrays.toString(restrictedUids), Arrays.toString(allowdUids)});
        }
        if (this.mTelephonyManager != null) {
            masterSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            slaveSubId = getSlaveSubId();
            subImsi = this.mTelephonyManager.getSubscriberId(masterSubId);
            slaveSubImsi = this.mTelephonyManager.getSubscriberId(slaveSubId);
        }
        String str = subImsi;
        if (str != null) {
            totalWiFiByte = getUsedAllBytes(str, 800);
            totalMobileByte = getUsedAllBytes(subImsi, 801);
        } else {
            totalMobileByte = -1;
        }
        String str2 = slaveSubImsi;
        if (str2 != null) {
            totalWiFiByte += getUsedAllBytes(str2, 800);
            totalSlaveMobileByte = getUsedAllBytes(slaveSubImsi, 801);
        } else {
            totalSlaveMobileByte = -1;
        }
        updateHomeApCount();
        updateMobileUseTime();
        HwHiLog.i(TAG, false, "totalWiFiByte %{public}d totalMobileByte = %{public}d totalSlaveMobileByte %{public}d", new Object[]{Integer.valueOf(totalWiFiByte), Integer.valueOf(totalMobileByte), Integer.valueOf(totalSlaveMobileByte)});
    }

    private int uploadAppInfo() {
        int apkNum = 0;
        IMonitor.EventStream staticsInfo = IMonitor.openEventStream((int) EVENT_APP_CHR_STATICS);
        IMonitor.EventStream staticApk = IMonitor.openEventStream((int) EVENT_APP_DETAIL_STATICS);
        Collections.sort(this.mPackageInfoList);
        Iterator<HwPackageInfo> it = this.mPackageInfoList.iterator();
        while (it.hasNext()) {
            HwPackageInfo info = it.next();
            updateUsedData(info);
            HwHiLog.i(TAG, false, "uploadChrApkInfo mPackageInfoList = " + info.toString(), new Object[0]);
            staticApk.setParam(APK, info.getPackageName()).setParam(DUR_WIFI, info.getUsedTimeInWiFi()).setParam(CONSUME_WIFI, info.getUsedDataInWiFi()).setParam(DUR_CELL, info.getUsedTimeInCell()).setParam(CONSUME_CELL, info.getUsedDataInCell()).setParam(VIDEO_IN_CELL, info.getVideoUsedTimeInCell()).setParam(VIDEO_IN_WIFI, info.getVideoUsedTimeInWiFi()).setParam(AUDIO_IN_CELL, info.getAudioUsedTimeInCell()).setParam(AUDIO_IN_WIFI, info.getAudioUsedTimeInWiFi());
            staticsInfo.fillArrayParam("TOPAPPINFO", staticApk);
            apkNum++;
            if (apkNum >= 10) {
                break;
            }
        }
        HwHiLog.i(TAG, false, "upload apkNum = " + apkNum, new Object[0]);
        if (apkNum > 0) {
            updateDataSaver();
            updateUserSetting(this.mContext);
            learnUserType(JSON_FILE_PATH);
            HwHiLog.i(TAG, false, "userType is " + userType, new Object[0]);
            Settings.Secure.getInt(this.mContext.getContentResolver(), "wifiap_one_usage_limit", -1);
            staticsInfo.setParam(TOTAL_WIFI_DATA, totalWiFiByte).setParam(TOTAL_CELL_DATA, totalMobileByte).setParam(TOTAL_SLAVE_CELL_DATA, totalSlaveMobileByte).setParam(OVERSPEED_WIFI, overSpeedTimeInWiFi).setParam(OVERSPEED_CELL, overSpeedTimeInCell).setParam(NO_LIMIT, mobileTraffic).setParam(SLAVE_NO_LIMIT, slaveMobileTraffic).setParam(LIMIT_TIME, userType).setParam(REMAIN_DATA, remainData).setParam(REMAIN_SLAVE_DATA, remainSlaveData).setParam(SMART_SAVE, dataSaverSwitch).setParam(NET_ACCS_WITCH, isTotalLinkTurboEnabled(this.mContext)).setParam(NET_APP_ALLOW, allowdUids.length).setParam(NET_APP_LIMIT, restrictedUids.length).setParam(OPEN_CELL_CNT, openMobileCnt).setParam(CLOSE_CELL_CNT, stayMoblieTime).setParam(GIVE_LUCKY_MONEY, staySoftApTime).setParam(REC_LUCKY_MONEY, homeApCnt);
            IMonitor.sendEvent(staticsInfo);
            cleanPackageList();
            cleanVaram();
        }
        IMonitor.closeEventStream(staticApk);
        IMonitor.closeEventStream(staticsInfo);
        return apkNum;
    }

    private void uploadChrApkInfo() {
        if (isBetaType && SystemClock.elapsedRealtime() - lastUploadTime >= INTERVAL_TIME) {
            lastUploadTime = SystemClock.elapsedRealtime();
            if (uploadAppInfo() > 0) {
                HwHiLog.i(TAG, false, "upload APP Info success", new Object[0]);
            }
        }
    }

    private void learnUserType(String path) {
        IHidataCallback iHidataCallback;
        if (TextUtils.isEmpty(path)) {
            HwHiLog.e(TAG, false, "learnUserType: path is empty", new Object[0]);
        } else if (!isJsonFileExist(path)) {
            jsonWrite(path);
        } else if (!isValidJsonFile(path)) {
            new File(path).delete();
            jsonWrite(path);
        } else if (addJson(path) >= 4 && (iHidataCallback = this.mHidataCallback) != null) {
            userType = iHidataCallback.onSetHumanFactor(path, 6);
            new File(path).delete();
        }
    }

    private boolean isJsonFileExist(String path) {
        if (!TextUtils.isEmpty(path)) {
            return new File(path).exists();
        }
        HwHiLog.e(TAG, false, "isJsonFileExist: path is empty", new Object[0]);
        return false;
    }

    private String readFile(String path) {
        synchronized (FILE_LOCK) {
            String res = "";
            if (TextUtils.isEmpty(path)) {
                HwHiLog.e(TAG, false, "readFile: path is empty", new Object[0]);
                return res;
            }
            StringBuilder stringBuilder = new StringBuilder();
            InputStreamReader inputStreamReader = null;
            try {
                InputStream instream = new FileInputStream(new File(path));
                inputStreamReader = new InputStreamReader(instream, CODE_FORMAT);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    stringBuilder.append(line);
                }
                res = stringBuilder.toString();
                closeFileStream(instream);
                closeFileStream(bufferedReader);
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "read file Exception", new Object[0]);
                closeFileStream(null);
                closeFileStream(null);
            } catch (Throwable th) {
                closeFileStream(null);
                closeFileStream(null);
                closeFileStream(null);
                throw th;
            }
            closeFileStream(inputStreamReader);
            return res;
        }
    }

    private void writeFile(String path, String content) {
        synchronized (FILE_LOCK) {
            if (TextUtils.isEmpty(path)) {
                HwHiLog.e(TAG, false, "writeFile: path is empty", new Object[0]);
            } else if (TextUtils.isEmpty(content)) {
                HwHiLog.e(TAG, false, "writeFile: content is empty", new Object[0]);
            } else {
                OutputStreamWriter osw = null;
                try {
                    osw = new OutputStreamWriter(new FileOutputStream(path), CODE_FORMAT);
                    osw.write(content);
                    osw.flush();
                } catch (IOException e) {
                    HwHiLog.e(TAG, false, "writeJson file Exception", new Object[0]);
                } finally {
                    closeFileStream(osw);
                }
            }
        }
    }

    private void closeFileStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "close file Exception", new Object[0]);
            }
        }
    }

    private boolean isValidJsonFile(String path) {
        if (TextUtils.isEmpty(path)) {
            HwHiLog.e(TAG, false, "isValidJsonFile: path is empty", new Object[0]);
            return false;
        }
        try {
            JSONArray staticsArray = new JSONObject(readFile(path)).getJSONArray(APP_CHR_STATICS);
            for (int i = 0; i < staticsArray.length(); i++) {
                JSONObject staticsDetail = staticsArray.getJSONObject(i);
                staticsDetail.getLong(TIME_STAMP);
                staticsDetail.getInt(TOTAL_WIFI_DATA);
                staticsDetail.getInt(TOTAL_CELL_DATA);
                staticsDetail.getInt(TOTAL_SLAVE_CELL_DATA);
                staticsDetail.getInt(OVERSPEED_WIFI);
                staticsDetail.getInt(OVERSPEED_CELL);
                staticsDetail.getInt(NO_LIMIT);
                staticsDetail.getInt(SLAVE_NO_LIMIT);
                staticsDetail.getInt(WIFI_AP_LIMIT);
                staticsDetail.getInt(REMAIN_DATA);
                staticsDetail.getInt(REMAIN_SLAVE_DATA);
                staticsDetail.getInt(SMART_SAVE);
                staticsDetail.getInt(NET_ACCS_WITCH);
                staticsDetail.getInt(NET_APP_ALLOW);
                staticsDetail.getInt(NET_APP_LIMIT);
                staticsDetail.getInt(OPEN_MOBILE_CNT);
                staticsDetail.getLong(STAY_MOBLIE_TIME);
                staticsDetail.getLong(STAY_SOFTAP_TIME);
                staticsDetail.getInt(HOME_AP_CNT);
                JSONArray appDataList = staticsDetail.getJSONArray(APK_INFO);
                for (int j = 0; j < appDataList.length(); j++) {
                    JSONObject appInfoDetail = appDataList.getJSONObject(j);
                    appInfoDetail.getString(APK);
                    appInfoDetail.getInt(DUR_WIFI);
                    appInfoDetail.getInt(CONSUME_WIFI);
                    appInfoDetail.getInt(DUR_CELL);
                    appInfoDetail.getInt(CONSUME_CELL);
                    appInfoDetail.getInt(VIDEO_IN_CELL);
                    appInfoDetail.getInt(VIDEO_IN_WIFI);
                    appInfoDetail.getInt(AUDIO_IN_CELL);
                    appInfoDetail.getInt(AUDIO_IN_WIFI);
                }
            }
            return true;
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "isValidJsonFile JSONException", new Object[0]);
            return false;
        }
    }

    private void jsonWrite(String path) {
        if (TextUtils.isEmpty(path)) {
            HwHiLog.e(TAG, false, "jsonWrite: path is empty", new Object[0]);
            return;
        }
        JSONObject chrStatics = new JSONObject();
        JSONArray staticsArray = new JSONArray();
        try {
            staticsArray.put(collectJsonData());
            chrStatics.put(APP_CHR_STATICS, staticsArray);
            writeFile(path, chrStatics.toString());
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "jsonWrite JSONException", new Object[0]);
        }
    }

    private int addJson(String path) {
        if (TextUtils.isEmpty(path)) {
            HwHiLog.e(TAG, false, "jsonWrite: path is empty", new Object[0]);
            return 0;
        }
        try {
            JSONObject chrStatics = new JSONObject(readFile(path));
            JSONArray staticsArray = chrStatics.getJSONArray(APP_CHR_STATICS);
            staticsArray.put(collectJsonData());
            chrStatics.put(APP_CHR_STATICS, staticsArray);
            writeFile(path, chrStatics.toString());
            return staticsArray.length();
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "add json Exception", new Object[0]);
            return 0;
        }
    }

    private JSONObject collectJsonData() {
        JSONObject staticsDetail = new JSONObject();
        try {
            if (this.mPackageInfoList.size() > 0) {
                staticsDetail.put(TIME_STAMP, System.currentTimeMillis());
                staticsDetail.put(TOTAL_WIFI_DATA, totalWiFiByte);
                staticsDetail.put(TOTAL_CELL_DATA, totalMobileByte);
                staticsDetail.put(TOTAL_SLAVE_CELL_DATA, totalSlaveMobileByte);
                staticsDetail.put(OVERSPEED_WIFI, overSpeedTimeInWiFi);
                staticsDetail.put(OVERSPEED_CELL, overSpeedTimeInCell);
                staticsDetail.put(NO_LIMIT, mobileTraffic);
                staticsDetail.put(SLAVE_NO_LIMIT, slaveMobileTraffic);
                staticsDetail.put(WIFI_AP_LIMIT, Settings.Secure.getInt(this.mContext.getContentResolver(), "wifiap_one_usage_limit", -1));
                staticsDetail.put(REMAIN_DATA, remainData);
                staticsDetail.put(REMAIN_SLAVE_DATA, remainSlaveData);
                staticsDetail.put(SMART_SAVE, dataSaverSwitch);
                staticsDetail.put(NET_ACCS_WITCH, isTotalLinkTurboEnabled(this.mContext));
                staticsDetail.put(NET_APP_ALLOW, allowdUids.length);
                staticsDetail.put(NET_APP_LIMIT, restrictedUids.length);
                staticsDetail.put(OPEN_MOBILE_CNT, openMobileCnt);
                staticsDetail.put(STAY_MOBLIE_TIME, stayMoblieTime);
                staticsDetail.put(STAY_SOFTAP_TIME, staySoftApTime);
                staticsDetail.put(HOME_AP_CNT, homeApCnt);
            }
            Iterator<HwPackageInfo> it = this.mPackageInfoList.iterator();
            while (it.hasNext()) {
                HwPackageInfo info = it.next();
                JSONObject appInfoDetal = new JSONObject();
                appInfoDetal.put(APK, info.getPackageName());
                appInfoDetal.put(DUR_WIFI, info.getUsedTimeInWiFi());
                appInfoDetal.put(CONSUME_WIFI, info.getUsedDataInWiFi());
                appInfoDetal.put(DUR_CELL, info.getUsedTimeInCell());
                appInfoDetal.put(CONSUME_CELL, info.getUsedDataInCell());
                appInfoDetal.put(VIDEO_IN_CELL, info.getVideoUsedTimeInCell());
                appInfoDetal.put(VIDEO_IN_WIFI, info.getVideoUsedTimeInWiFi());
                appInfoDetal.put(AUDIO_IN_CELL, info.getAudioUsedTimeInCell());
                appInfoDetal.put(AUDIO_IN_WIFI, info.getAudioUsedTimeInWiFi());
                JSONArray appDetalList = new JSONArray();
                appDetalList.put(appInfoDetal);
                staticsDetail.put(APK_INFO, appDetalList);
            }
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "collect json data Exception", new Object[0]);
        }
        return staticsDetail;
    }
}
