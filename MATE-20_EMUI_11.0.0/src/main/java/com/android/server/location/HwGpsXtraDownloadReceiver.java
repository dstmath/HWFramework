package com.android.server.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HwGpsXtraDownloadReceiver implements IHwGpsXtraDownloadReceiver {
    private static final String ACTION_SET_XTRA = "android.intent.action.SET_GPSXTRA";
    private static final String ACTION_XTRA_DOWNLOAD = "com.android.xtra.download";
    private static final String ACTION_XTRA_DOWNLOAD_COMPLETED = "android.intent.action.XTRA_DOWNLOAD_COMPLETED";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String IS_GET_NTP_TIME = "is_get_ntp_time";
    private static final String LOCATION_METHOD = "location_method";
    private static final int NET_TYPE_ERROR = -1;
    private static final String PROPERTIES_FILE = "gps.conf";
    private static final String TAG = "GpsXtraReceiver";
    private static final int TIMEOUT = 3000;
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static final String XTRA_DATA_DATE = "XtraDataDate";
    private static final long XTRA_DOWNLOAD_INTERVAL_DEFAULT = 12;
    private static final long XTRA_DOWNLOAD_INTERVAL_UNIT = 3600000;
    private static final int XTRA_METHOD = 3;
    private static volatile HwGpsXtraDownloadReceiver sInstance;
    private boolean isDownloadPending = false;
    private boolean isGetNtpTime = false;
    private boolean isXtraSwitch = false;
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.HwGpsXtraDownloadReceiver.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwGpsXtraDownloadReceiver.this.mAlarmManager != null && HwGpsXtraDownloadReceiver.this.mLocationManager != null && HwGpsXtraDownloadReceiver.this.mConnectivity != null && intent != null) {
                String action = intent.getAction();
                if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                    NetworkInfo info = null;
                    if (intent.getExtra("networkInfo") instanceof NetworkInfo) {
                        info = (NetworkInfo) intent.getExtra("networkInfo");
                    }
                    if (info != null && info.isConnected() && info.isAvailable()) {
                        if (info.getType() == 1) {
                            HwGpsXtraDownloadReceiver.this.setXtraStringWithoutNetCheck(1);
                            HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtraDownloadInterval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                        } else if (info.getType() == 0) {
                            HwGpsXtraDownloadReceiver.this.setXtraStringWithoutNetCheck(0);
                            HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtraDownloadInterval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                        }
                    }
                } else if (HwGpsXtraDownloadReceiver.ACTION_SET_XTRA.equals(action)) {
                    if (HwGpsXtraDownloadReceiver.this.isXtraMethod(context)) {
                        HwGpsXtraDownloadReceiver.this.setXtraString();
                        HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtraDownloadInterval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                    }
                } else if (HwGpsXtraDownloadReceiver.ACTION_XTRA_DOWNLOAD.equals(action)) {
                    LocationManager location = null;
                    if (context != null) {
                        location = (LocationManager) context.getSystemService("location");
                    }
                    if (location == null || !location.isProviderEnabled("gps")) {
                        LBSLog.i(HwGpsXtraDownloadReceiver.TAG, false, "GPS is not enable, do not send extra command", new Object[0]);
                        return;
                    }
                    int curNetType = HwGpsXtraDownloadReceiver.this.getCurrentNetType();
                    if (curNetType == 1) {
                        HwGpsXtraDownloadReceiver.this.mLocationManager.sendExtraCommand("gps", "force_xtra_injection", null);
                    } else if (curNetType == 0 && HwGpsXtraDownloadReceiver.this.isXtraMethod(context)) {
                        HwGpsXtraDownloadReceiver.this.mLocationManager.sendExtraCommand("gps", "force_xtra_injection", null);
                    }
                } else if (HwGpsXtraDownloadReceiver.ACTION_XTRA_DOWNLOAD_COMPLETED.equals(action)) {
                    Settings.System.putLong(context.getContentResolver(), HwGpsXtraDownloadReceiver.XTRA_DATA_DATE, intent.getLongExtra("downloadDate", System.currentTimeMillis()));
                    if (intent.getBooleanExtra("bGetNtpTime", false)) {
                        Settings.System.putInt(context.getContentResolver(), HwGpsXtraDownloadReceiver.IS_GET_NTP_TIME, 1);
                    } else {
                        Settings.System.putInt(context.getContentResolver(), HwGpsXtraDownloadReceiver.IS_GET_NTP_TIME, 0);
                    }
                    HwGpsXtraDownloadReceiver.this.setXtraString();
                    HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtraDownloadInterval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                    HwGpsXtraDownloadReceiver.this.mLocationManager.sendExtraCommand("gps", "force_time_injection", null);
                }
            }
        }
    };
    private ConnectivityManager mConnectivity;
    private Context mContext;
    private long mCurrentNtpTime = 0;
    private long mElapsedTime = 0;
    private LocationManager mLocationManager;
    private String mNtpServer;
    private PendingIntent mPendingIntent;
    private long xtraDownloadInterval = 0;
    private String xtraString;
    private String xtraStringOnMobile;
    private String xtraStringOnWifi;

    private HwGpsXtraDownloadReceiver() {
    }

    public static HwGpsXtraDownloadReceiver getInstance() {
        if (sInstance == null) {
            synchronized (HwGpsXtraDownloadReceiver.class) {
                if (sInstance == null) {
                    sInstance = new HwGpsXtraDownloadReceiver();
                }
            }
        }
        return sInstance;
    }

    public boolean handleUpdateNetworkState(NetworkInfo info, boolean isPending) {
        if (!isXtraDownloadEnable()) {
            return isPending;
        }
        this.isDownloadPending = isPending;
        if (info != null && info.isConnected() && info.isAvailable()) {
            if (info.getType() == 1 && Long.parseLong(this.xtraStringOnWifi) > 0) {
                setXtraStringWithoutNetCheck(1);
                this.mAlarmManager.setRepeating(0, getTriggerAtTime(this.mContext), this.xtraDownloadInterval, this.mPendingIntent);
            } else if (info.getType() == 0 && this.mLocationManager.isLocationEnabled() && Long.parseLong(this.xtraStringOnMobile) > 0) {
                setXtraStringWithoutNetCheck(0);
                this.mAlarmManager.setRepeating(0, getTriggerAtTime(this.mContext), this.xtraDownloadInterval, this.mPendingIntent);
            }
        }
        return false;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SET_XTRA);
        intentFilter.addAction(ACTION_XTRA_DOWNLOAD);
        return intentFilter;
    }

    public void init(Context context) {
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_XTRA_DOWNLOAD, (Uri) null), 0);
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        this.mConnectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (!getXtraDownConfig()) {
            LBSLog.e(TAG, false, "the switch of xtra download optimizer is not on!", new Object[0]);
            return;
        }
        HandlerThread thread = new HandlerThread("GpsXtraReceiverThread");
        thread.setPriority(10);
        thread.start();
        this.mContext.registerReceiver(this.mBroadcastReceiver, getIntentFilter(), null, new Handler(thread.getLooper()));
    }

    private boolean getXtraDownConfig() {
        Properties xtraProperties = new Properties();
        FileInputStream stream = null;
        try {
            File file = HwCfgFilePolicy.getCfgFile(PROPERTIES_FILE, 0);
            if (file != null) {
                LBSLog.i(TAG, false, "%{private}s be read ! ", file.getCanonicalPath());
                stream = new FileInputStream(file);
                xtraProperties.load(stream);
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LBSLog.e(TAG, e.getMessage());
                }
            }
        } catch (IOException e2) {
            if (VERBOSE) {
                LBSLog.w(TAG, false, "Could not open GPS configuration file %{public}s", PROPERTIES_FILE);
            }
            if (0 != 0) {
                stream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    LBSLog.e(TAG, e3.getMessage());
                }
            }
            throw th;
        }
        this.isXtraSwitch = AppActConstant.VALUE_TRUE.equals(xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", AppActConstant.VALUE_TRUE));
        this.xtraStringOnMobile = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", Long.toString(XTRA_DOWNLOAD_INTERVAL_DEFAULT));
        this.xtraStringOnWifi = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG_WIFI", Long.toString(XTRA_DOWNLOAD_INTERVAL_DEFAULT));
        this.xtraString = this.xtraStringOnMobile;
        this.xtraDownloadInterval = Long.parseLong(this.xtraString) * 3600000;
        this.mNtpServer = xtraProperties.getProperty("NTP_SERVER", null);
        return this.isXtraSwitch;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCurrentNetType() {
        NetworkInfo info = this.mConnectivity.getActiveNetworkInfo();
        if (info == null || !info.isConnected() || !info.isAvailable()) {
            return -1;
        }
        return info.getType();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setXtraStringWithoutNetCheck(int netType) {
        if (netType == 1) {
            this.xtraString = this.xtraStringOnWifi;
            if (this.isDownloadPending) {
                this.xtraString = "0";
            }
        } else {
            this.xtraString = this.xtraStringOnMobile;
        }
        this.xtraDownloadInterval = Long.parseLong(this.xtraString) * 3600000;
        LBSLog.i(TAG, false, " pengding:%{public}b, donwload_interval=%{public}d", Boolean.valueOf(this.isDownloadPending), Long.valueOf(this.xtraDownloadInterval));
    }

    private boolean isXtraDownloadEnable() {
        boolean isXtraDownload = false;
        if (this.isXtraSwitch && (Long.parseLong(this.xtraStringOnMobile) > 0 || Long.parseLong(this.xtraStringOnWifi) > 0)) {
            isXtraDownload = true;
        }
        LBSLog.i(TAG, false, "xtra download isXtraDownload =%{public}b", Boolean.valueOf(isXtraDownload));
        return isXtraDownload;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getTriggerAtTime(Context context) {
        long nowDate;
        long downloadDataDate = Settings.System.getLong(context.getContentResolver(), XTRA_DATA_DATE, 0);
        SntpClient client = new SntpClient();
        if (Settings.System.getInt(context.getContentResolver(), IS_GET_NTP_TIME, 0) != 1 || !client.requestTime(this.mNtpServer, 3000)) {
            nowDate = System.currentTimeMillis();
        } else {
            nowDate = client.getNtpTime();
        }
        long j = this.xtraDownloadInterval;
        return downloadDataDate == 0 ? nowDate : nowDate - downloadDataDate > j ? nowDate : downloadDataDate + j;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isXtraMethod(Context context) {
        return Settings.System.getInt(context.getContentResolver(), LOCATION_METHOD, 0) == 3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setXtraString() {
        NetworkInfo info = this.mConnectivity.getNetworkInfo(1);
        if (info == null || !info.isConnected()) {
            this.xtraString = this.xtraStringOnMobile;
        } else {
            this.xtraString = this.xtraStringOnWifi;
        }
        this.xtraDownloadInterval = Long.parseLong(this.xtraString) * 3600000;
    }

    public void setNtpTime(long time, long reftime) {
        this.isGetNtpTime = true;
        this.mCurrentNtpTime = time;
        this.mElapsedTime = reftime;
    }

    public void sendXtraDownloadComplete() {
        long downloadDate;
        if (isXtraDownloadEnable()) {
            if (this.isGetNtpTime) {
                downloadDate = this.mCurrentNtpTime + (SystemClock.elapsedRealtime() - this.mElapsedTime);
                Settings.System.putInt(this.mContext.getContentResolver(), IS_GET_NTP_TIME, 1);
            } else {
                downloadDate = System.currentTimeMillis();
                Settings.System.putInt(this.mContext.getContentResolver(), IS_GET_NTP_TIME, 0);
            }
            Settings.System.putLong(this.mContext.getContentResolver(), XTRA_DATA_DATE, downloadDate);
            setXtraString();
            this.mAlarmManager.setRepeating(0, getTriggerAtTime(this.mContext), this.xtraDownloadInterval, this.mPendingIntent);
            this.mLocationManager.sendExtraCommand("gps", "force_time_injection", null);
        }
    }
}
