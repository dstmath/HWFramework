package com.android.server.pg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.power.IHwShutdownThread;
import com.huawei.pgmng.log.LogPower;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PGGoogleServicePolicy {
    private static final String GOOGLE_GMS_PAC = "com.google.android.gms";
    private static final String GOOGLE_GSF_PAC = "com.google.android.gsf";
    private static final Boolean IS_CHINA_MARKET;
    private static final long STATIC_WAKELOCK_CHECK_TIME_MAX = 1800000;
    private static final long STATIC_WAKELOCK_CHECK_TIME_MIN = 60000;
    private static final String TAG = "PGGoogleServicePolicy";
    private static final String US_GOOGLE_URL = "http://www.google.com";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (PGGoogleServicePolicy.this.mEnabled) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                        PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 0);
                    } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null && networkInfo.isConnected()) {
                            long unused = PGGoogleServicePolicy.this.mCheckDuration = 60000;
                            PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                            PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 0);
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public long mCheckDuration = 60000;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mEnabled = true;
    /* access modifiers changed from: private */
    public int mGmsUid = -1;
    /* access modifiers changed from: private */
    public boolean mIsGoogleServerConnectedOK = (!IS_CHINA_MARKET.booleanValue());
    /* access modifiers changed from: private */
    public Handler mWakeLockHandler;
    /* access modifiers changed from: private */
    public Runnable mWakelockMonitor = new Runnable() {
        public void run() {
            Log.d(PGGoogleServicePolicy.TAG, "check isGoogleConnect start");
            new Thread() {
                public void run() {
                    int unused = PGGoogleServicePolicy.this.mGmsUid = PGGoogleServicePolicy.this.getGmsUid();
                    if (!PGGoogleServicePolicy.this.isGoogleConnectOK()) {
                        boolean unused2 = PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = false;
                        Log.d(PGGoogleServicePolicy.TAG, "connect google failed, PreventWake change to valid");
                    } else {
                        boolean unused3 = PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = true;
                        Log.d(PGGoogleServicePolicy.TAG, "connect google success, PreventWake change to invalid");
                    }
                    LogPower.push(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK ? "1" : "0");
                    HwServiceFactory.reportGoogleConn(PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK);
                    PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                    if (PGGoogleServicePolicy.this.isShangHaiTimeZone() || PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK) {
                        PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 1800000);
                        return;
                    }
                    PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, PGGoogleServicePolicy.this.mCheckDuration);
                    Log.d(PGGoogleServicePolicy.TAG, "retry after " + PGGoogleServicePolicy.this.mCheckDuration);
                    PGGoogleServicePolicy.access$330(PGGoogleServicePolicy.this, 2);
                    if (PGGoogleServicePolicy.this.mCheckDuration >= 1800000) {
                        long unused4 = PGGoogleServicePolicy.this.mCheckDuration = 1800000;
                    }
                }
            }.start();
        }
    };

    static /* synthetic */ long access$330(PGGoogleServicePolicy x0, long x1) {
        long j = x0.mCheckDuration * x1;
        x0.mCheckDuration = j;
        return j;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.hw_optb", 0) == 156) {
            z = true;
        }
        IS_CHINA_MARKET = Boolean.valueOf(z);
    }

    public PGGoogleServicePolicy(Context context) {
        this.mContext = context;
        this.mWakeLockHandler = new Handler();
    }

    public void onSystemReady() {
        if (IS_CHINA_MARKET.booleanValue()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
        }
    }

    /* access modifiers changed from: private */
    public boolean isShangHaiTimeZone() {
        return "Asia/Shanghai".equals(SystemProperties.get("persist.sys.timezone", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
    }

    /* access modifiers changed from: private */
    public boolean isGoogleConnectOK() {
        int httpResponseCode;
        HttpURLConnection conn = null;
        Boolean isConnectOk = false;
        BufferedReader reader = null;
        try {
            try {
                HttpURLConnection conn2 = (HttpURLConnection) new URL(US_GOOGLE_URL).openConnection();
                conn2.setConnectTimeout(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
                conn2.setReadTimeout(10000);
                conn2.connect();
                Log.d(TAG, "httpResponseCode = " + httpResponseCode);
                if (200 == httpResponseCode) {
                    if (IS_CHINA_MARKET.booleanValue()) {
                        String networkOperator = TelephonyManager.getDefault().getSimOperator();
                        if (networkOperator != null && networkOperator.startsWith("460")) {
                            reader = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                            StringBuffer sb = new StringBuffer();
                            while (true) {
                                String readLine = reader.readLine();
                                String line = readLine;
                                if (readLine == null) {
                                    break;
                                }
                                sb.append(line);
                                if (sb.toString().length() > 2) {
                                    isConnectOk = true;
                                    Log.d(TAG, "openrator is " + networkOperator + "httpResponseData length : " + length);
                                    break;
                                }
                            }
                        } else {
                            isConnectOk = true;
                        }
                    } else {
                        isConnectOk = true;
                    }
                }
                if (conn2 != null) {
                    conn2.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.w(TAG, "close reader Exception.");
                    }
                }
            } catch (Exception e2) {
                Log.d(TAG, "failed to connect google.");
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        Log.w(TAG, "close reader Exception.");
                    }
                }
                throw th;
            }
            if (isConnectOk.booleanValue()) {
                return true;
            }
            return false;
        } catch (MalformedURLException e4) {
            Log.d(TAG, "PreventWake MalformedURLException");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public int getGmsUid() {
        ApplicationInfo ai = null;
        try {
            ai = this.mContext.getPackageManager().getApplicationInfo(GOOGLE_GMS_PAC, 0);
        } catch (Exception e) {
            Log.d(TAG, "failed to get application info");
        }
        if (ai == null) {
            return -1;
        }
        Log.d(TAG, "gmsUid = " + ai.uid);
        return ai.uid;
    }

    public boolean isGmsWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
        if (packageName != null && 1 == (65535 & flags) && !this.mIsGoogleServerConnectedOK) {
            if (packageName.contains(GOOGLE_GMS_PAC) || packageName.contains(GOOGLE_GSF_PAC)) {
                Log.d(TAG, "prevent gms/gsf hold partial wakelock");
                return true;
            } else if (ws != null) {
                for (int i = 0; i < ws.size(); i++) {
                    if (ws.get(i) == this.mGmsUid) {
                        Log.d(TAG, "worksource has gms, prevent hold partial wakelock");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
