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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import com.android.server.HwConnectivityService;
import com.android.server.PPPOEStateMachine;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HwGpsXtraDownloadReceiver implements IHwGpsXtraDownloadReceiver {
    private static final String ACTION_SET_XTRA = "android.intent.action.SET_GPSXTRA";
    private static final String ACTION_XTRA_DOWNLOAD = "com.android.xtra.download";
    private static final String ACTION_XTRA_DOWNLOAD_COMPLETED = "android.intent.action.XTRA_DOWNLOAD_COMPLETED";
    private static final boolean DEBUG;
    private static final String IS_GET_NTP_TIME = "is_get_ntp_time";
    private static final String LOCATION_METHOD = "location_method";
    private static final String PROPERTIES_FILE = "gps.conf";
    private static final String TAG = "GpsXtraReceiver";
    private static final boolean VERBOSE;
    private static final String XTRADATADATE = "XtraDataDate";
    private static final long XTRA_DOWNLOAD_INTERVAL_DEFAULT = 24;
    private static final long XTRA_DOWNLOAD_INTERVAL_UNIT = 3600000;
    private static final int XTRA_METHOD = 3;
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mBroadcastReceiver;
    private ConnectivityManager mConnectivity;
    private Context mContext;
    private long mCurrentNtpTime;
    private boolean mDownloadPending;
    private long mElapsedTime;
    private boolean mGetNtpTime;
    private LocationProviderInterface mLocProvider;
    private LocationManager mLocationManager;
    private String mNtpServer;
    private PendingIntent mPendingIntent;
    private boolean mXtraSwitch;
    private String xtraString;
    private String xtraStringOnMobile;
    private String xtraStringOnWifi;
    private long xtra_download_interval;

    public HwGpsXtraDownloadReceiver() {
        this.xtra_download_interval = 0;
        this.mCurrentNtpTime = 0;
        this.mElapsedTime = 0;
        this.mGetNtpTime = VERBOSE;
        this.mDownloadPending = VERBOSE;
        this.mXtraSwitch = VERBOSE;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwGpsXtraDownloadReceiver.this.mAlarmManager != null && HwGpsXtraDownloadReceiver.this.mLocationManager != null && HwGpsXtraDownloadReceiver.this.mConnectivity != null) {
                    String action = intent.getAction();
                    if (HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(action)) {
                        NetworkInfo info = (NetworkInfo) intent.getExtra("networkInfo");
                        if (info != null && info.isConnected() && info.isAvailable()) {
                            if (1 == info.getType()) {
                                HwGpsXtraDownloadReceiver.this.setXtraStringWithoutNetCheck(1);
                                HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtra_download_interval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                            } else if (info.getType() == 0) {
                                HwGpsXtraDownloadReceiver.this.setXtraStringWithoutNetCheck(0);
                                HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtra_download_interval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                            }
                        }
                    } else if (HwGpsXtraDownloadReceiver.ACTION_SET_XTRA.equals(action)) {
                        if (HwGpsXtraDownloadReceiver.this.isXtraMethod(context)) {
                            HwGpsXtraDownloadReceiver.this.setXtraString();
                            HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtra_download_interval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                        }
                    } else if (HwGpsXtraDownloadReceiver.ACTION_XTRA_DOWNLOAD.equals(action)) {
                        LocationManager locationManager = null;
                        if (context != null) {
                            locationManager = (LocationManager) context.getSystemService("location");
                        }
                        if (locationManager != null && locationManager.isProviderEnabled("gps")) {
                            int curNetType = HwGpsXtraDownloadReceiver.this.getCurrentNetType();
                            if (curNetType == 1) {
                                HwGpsXtraDownloadReceiver.this.mLocationManager.sendExtraCommand("gps", "force_xtra_injection", null);
                            } else if (curNetType == 0 && HwGpsXtraDownloadReceiver.this.isXtraMethod(context)) {
                                HwGpsXtraDownloadReceiver.this.mLocationManager.sendExtraCommand("gps", "force_xtra_injection", null);
                            }
                        } else if (HwGpsXtraDownloadReceiver.DEBUG) {
                            Log.d(HwGpsXtraDownloadReceiver.TAG, "GPS is not enable, do not send extra command");
                        }
                    } else if (HwGpsXtraDownloadReceiver.ACTION_XTRA_DOWNLOAD_COMPLETED.equals(action)) {
                        System.putLong(context.getContentResolver(), HwGpsXtraDownloadReceiver.XTRADATADATE, intent.getLongExtra("downloadDate", System.currentTimeMillis()));
                        if (intent.getBooleanExtra("bGetNtpTime", HwGpsXtraDownloadReceiver.VERBOSE)) {
                            System.putInt(context.getContentResolver(), HwGpsXtraDownloadReceiver.IS_GET_NTP_TIME, 1);
                        } else {
                            System.putInt(context.getContentResolver(), HwGpsXtraDownloadReceiver.IS_GET_NTP_TIME, 0);
                        }
                        HwGpsXtraDownloadReceiver.this.setXtraString();
                        HwGpsXtraDownloadReceiver.this.mAlarmManager.setRepeating(0, HwGpsXtraDownloadReceiver.this.getTriggerAtTime(context), HwGpsXtraDownloadReceiver.this.xtra_download_interval, HwGpsXtraDownloadReceiver.this.mPendingIntent);
                        HwGpsXtraDownloadReceiver.this.mLocationManager.sendExtraCommand("gps", "force_time_injection", null);
                    }
                }
            }
        };
    }

    static {
        DEBUG = Log.isLoggable(TAG, XTRA_METHOD);
        VERBOSE = Log.isLoggable(TAG, 2);
    }

    public boolean handleUpdateNetworkState(NetworkInfo info, boolean isPending) {
        if (!isXtraDownloadEnable()) {
            return isPending;
        }
        this.mDownloadPending = isPending;
        if (info != null && info.isConnected() && info.isAvailable()) {
            if (1 == info.getType() && Long.parseLong(this.xtraStringOnWifi) > 0) {
                setXtraStringWithoutNetCheck(1);
                this.mAlarmManager.setRepeating(0, getTriggerAtTime(this.mContext), this.xtra_download_interval, this.mPendingIntent);
            } else if (info.getType() == 0 && this.mLocProvider.isEnabled() && Long.parseLong(this.xtraStringOnMobile) > 0) {
                setXtraStringWithoutNetCheck(0);
                this.mAlarmManager.setRepeating(0, getTriggerAtTime(this.mContext), this.xtra_download_interval, this.mPendingIntent);
            }
        }
        return VERBOSE;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SET_XTRA);
        intentFilter.addAction(ACTION_XTRA_DOWNLOAD);
        return intentFilter;
    }

    public void init(Context context, LocationProviderInterface lp) {
        this.mContext = context;
        this.mLocProvider = lp;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_XTRA_DOWNLOAD, null), 0);
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        this.mConnectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (getXtraDownConfig()) {
            HandlerThread thread = new HandlerThread("GpsXtraReceiverThread");
            thread.setPriority(10);
            thread.start();
            this.mContext.registerReceiver(this.mBroadcastReceiver, getIntentFilter(), null, new Handler(thread.getLooper()));
            return;
        }
        Log.e(TAG, "the switch of xtra download optimizer is not on!");
    }

    private boolean getXtraDownConfig() {
        Throwable th;
        Properties xtraProperties = new Properties();
        FileInputStream fileInputStream = null;
        try {
            File file = HwCfgFilePolicy.getCfgFile(PROPERTIES_FILE, 0);
            if (file != null) {
                Log.v(TAG, file.getAbsolutePath() + " be read ! ");
                FileInputStream stream = new FileInputStream(file);
                try {
                    xtraProperties.load(stream);
                    fileInputStream = stream;
                } catch (IOException e) {
                    fileInputStream = stream;
                    try {
                        if (VERBOSE) {
                            Log.w(TAG, "Could not open GPS configuration file gps.conf");
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        this.mXtraSwitch = "true".equals(xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", "true"));
                        this.xtraStringOnMobile = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", Long.toString(48));
                        this.xtraStringOnWifi = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG_WIFI", Long.toString(XTRA_DOWNLOAD_INTERVAL_DEFAULT));
                        this.xtraString = this.xtraStringOnMobile;
                        this.xtra_download_interval = Long.parseLong(this.xtraString) * XTRA_DOWNLOAD_INTERVAL_UNIT;
                        this.mNtpServer = xtraProperties.getProperty("NTP_SERVER", null);
                        return this.mXtraSwitch;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = stream;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        } catch (IOException e3) {
            if (VERBOSE) {
                Log.w(TAG, "Could not open GPS configuration file gps.conf");
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            this.mXtraSwitch = "true".equals(xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", "true"));
            this.xtraStringOnMobile = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", Long.toString(48));
            this.xtraStringOnWifi = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG_WIFI", Long.toString(XTRA_DOWNLOAD_INTERVAL_DEFAULT));
            this.xtraString = this.xtraStringOnMobile;
            this.xtra_download_interval = Long.parseLong(this.xtraString) * XTRA_DOWNLOAD_INTERVAL_UNIT;
            this.mNtpServer = xtraProperties.getProperty("NTP_SERVER", null);
            return this.mXtraSwitch;
        }
        this.mXtraSwitch = "true".equals(xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", "true"));
        this.xtraStringOnMobile = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG", Long.toString(48));
        this.xtraStringOnWifi = xtraProperties.getProperty("XTRA_DOWNLOAD_INTERVAL_CONFIG_WIFI", Long.toString(XTRA_DOWNLOAD_INTERVAL_DEFAULT));
        this.xtraString = this.xtraStringOnMobile;
        this.xtra_download_interval = Long.parseLong(this.xtraString) * XTRA_DOWNLOAD_INTERVAL_UNIT;
        this.mNtpServer = xtraProperties.getProperty("NTP_SERVER", null);
        return this.mXtraSwitch;
    }

    private int getCurrentNetType() {
        NetworkInfo info = this.mConnectivity.getActiveNetworkInfo();
        if (info != null && info.isConnected() && info.isAvailable()) {
            return info.getType();
        }
        return -1;
    }

    private void setXtraStringWithoutNetCheck(int netType) {
        if (1 == netType) {
            this.xtraString = this.xtraStringOnWifi;
            if (this.mDownloadPending) {
                this.xtraString = PPPOEStateMachine.PHASE_DEAD;
            }
        } else {
            this.xtraString = this.xtraStringOnMobile;
        }
        this.xtra_download_interval = Long.parseLong(this.xtraString) * XTRA_DOWNLOAD_INTERVAL_UNIT;
        Log.d(TAG, " pengding:" + this.mDownloadPending + ", donwload_interval=" + this.xtra_download_interval);
    }

    private boolean isXtraDownloadEnable() {
        boolean enable = VERBOSE;
        if (this.mXtraSwitch && (Long.parseLong(this.xtraStringOnMobile) > 0 || Long.parseLong(this.xtraStringOnWifi) > 0)) {
            enable = true;
        }
        Log.d(TAG, "xtra download enable =" + enable);
        return enable;
    }

    private long getTriggerAtTime(Context context) {
        long nowDate;
        long downloadDataDate = System.getLong(context.getContentResolver(), XTRADATADATE, 0);
        SntpClient client = new SntpClient();
        if (1 == System.getInt(context.getContentResolver(), IS_GET_NTP_TIME, 0) && client.requestTime(this.mNtpServer, 3000)) {
            nowDate = client.getNtpTime();
        } else {
            nowDate = System.currentTimeMillis();
        }
        long tiggerTime = nowDate - downloadDataDate > this.xtra_download_interval ? nowDate : downloadDataDate + this.xtra_download_interval;
        if (downloadDataDate == 0) {
            return nowDate;
        }
        return tiggerTime;
    }

    private boolean isXtraMethod(Context context) {
        return XTRA_METHOD == System.getInt(context.getContentResolver(), LOCATION_METHOD, 0) ? true : VERBOSE;
    }

    private void setXtraString() {
        NetworkInfo info = this.mConnectivity.getNetworkInfo(1);
        if (info == null || !info.isConnected()) {
            this.xtraString = this.xtraStringOnMobile;
        } else {
            this.xtraString = this.xtraStringOnWifi;
        }
        this.xtra_download_interval = Long.parseLong(this.xtraString) * XTRA_DOWNLOAD_INTERVAL_UNIT;
    }

    public void setNtpTime(long time, long reftime) {
        this.mGetNtpTime = true;
        this.mCurrentNtpTime = time;
        this.mElapsedTime = reftime;
    }

    public void sendXtraDownloadComplete() {
        if (isXtraDownloadEnable()) {
            long downloadDate;
            if (this.mGetNtpTime) {
                downloadDate = this.mCurrentNtpTime + (SystemClock.elapsedRealtime() - this.mElapsedTime);
                System.putInt(this.mContext.getContentResolver(), IS_GET_NTP_TIME, 1);
            } else {
                downloadDate = System.currentTimeMillis();
                System.putInt(this.mContext.getContentResolver(), IS_GET_NTP_TIME, 0);
            }
            System.putLong(this.mContext.getContentResolver(), XTRADATADATE, downloadDate);
            setXtraString();
            this.mAlarmManager.setRepeating(0, getTriggerAtTime(this.mContext), this.xtra_download_interval, this.mPendingIntent);
            this.mLocationManager.sendExtraCommand("gps", "force_time_injection", null);
        }
    }
}
