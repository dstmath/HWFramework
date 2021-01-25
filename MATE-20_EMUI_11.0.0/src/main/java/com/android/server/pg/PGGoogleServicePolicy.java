package com.android.server.pg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.power.IHwShutdownThread;
import com.huawei.pgmng.log.LogPower;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PGGoogleServicePolicy {
    private static final String CHINA_OPERATOR = "460";
    private static final String GOOGLE_GMS_PAC = "com.google.android.gms";
    private static final String GOOGLE_GSF_PAC = "com.google.android.gsf";
    private static final Boolean IS_CHINA_MARKET;
    private static final long STATIC_RETRY_CHECK_DELAY = 5000;
    private static final long STATIC_WAKELOCK_CHECK_TIME_MAX = 1800000;
    private static final long STATIC_WAKELOCK_CHECK_TIME_MIN = 60000;
    private static final String TAG = "PGGoogleServicePolicy";
    private static ConnectivityManager mConManager = null;
    private String googleUrl = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.pg.PGGoogleServicePolicy.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo;
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                    PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 0);
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") && (networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo")) != null) {
                    if (networkInfo.isConnected()) {
                        PGGoogleServicePolicy.this.mCheckDuration = 60000;
                        PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                        PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 0);
                        return;
                    }
                    if (PGGoogleServicePolicy.mConManager == null) {
                        ConnectivityManager unused = PGGoogleServicePolicy.mConManager = (ConnectivityManager) context.getSystemService("connectivity");
                        if (PGGoogleServicePolicy.mConManager == null) {
                            return;
                        }
                    }
                    for (Network network : PGGoogleServicePolicy.mConManager.getAllNetworks()) {
                        NetworkInfo infoItem = PGGoogleServicePolicy.mConManager.getNetworkInfo(network);
                        if (infoItem != null && infoItem.isConnected()) {
                            Log.d(PGGoogleServicePolicy.TAG, "network is still connected.");
                            PGGoogleServicePolicy.this.mCheckDuration = 60000;
                            PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                            PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, PGGoogleServicePolicy.STATIC_RETRY_CHECK_DELAY);
                            return;
                        }
                    }
                    Log.d(PGGoogleServicePolicy.TAG, "no network.");
                    PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = false;
                    LogPower.push((int) HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, "0");
                    HwServiceFactory.reportGoogleConn(PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK);
                    PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                }
            }
        }
    };
    private long mCheckDuration = 60000;
    private final Context mContext;
    private int mGmsUid = -1;
    private boolean mIsConnectedOnce = false;
    private boolean mIsGoogleServerConnectedOK = (!IS_CHINA_MARKET.booleanValue());
    private final Handler mWakeLockHandler;
    private final Runnable mWakelockMonitor = new Runnable() {
        /* class com.android.server.pg.PGGoogleServicePolicy.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            Log.d(PGGoogleServicePolicy.TAG, "check isGoogleConnect start");
            new Thread() {
                /* class com.android.server.pg.PGGoogleServicePolicy.AnonymousClass2.AnonymousClass1 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    PGGoogleServicePolicy.this.mGmsUid = PGGoogleServicePolicy.this.getGmsUid();
                    String networkOperator = TelephonyManager.getDefault().getSimOperator();
                    if (networkOperator != null && !networkOperator.isEmpty() && !networkOperator.startsWith(PGGoogleServicePolicy.CHINA_OPERATOR) && PGGoogleServicePolicy.this.mIsConnectedOnce) {
                        PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = true;
                        Log.d(PGGoogleServicePolicy.TAG, "oversea network operator, connect google success.");
                    } else if (!PGGoogleServicePolicy.this.isGoogleConnectOK()) {
                        PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = false;
                        Log.d(PGGoogleServicePolicy.TAG, "connect google failed.");
                    } else {
                        PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = true;
                        PGGoogleServicePolicy.this.mIsConnectedOnce = true;
                        Log.d(PGGoogleServicePolicy.TAG, "connect google success.");
                    }
                    LogPower.push((int) HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK ? "1" : "0");
                    HwServiceFactory.reportGoogleConn(PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK);
                    PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                    if (PGGoogleServicePolicy.this.isShangHaiTimeZone() || PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK) {
                        PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 1800000);
                        return;
                    }
                    PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, PGGoogleServicePolicy.this.mCheckDuration);
                    Log.d(PGGoogleServicePolicy.TAG, "retry after " + PGGoogleServicePolicy.this.mCheckDuration);
                    PGGoogleServicePolicy.access$230(PGGoogleServicePolicy.this, 2);
                    if (PGGoogleServicePolicy.this.mCheckDuration >= 1800000) {
                        PGGoogleServicePolicy.this.mCheckDuration = 1800000;
                    }
                }
            }.start();
        }
    };

    static /* synthetic */ long access$230(PGGoogleServicePolicy x0, long x1) {
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
        this.mWakeLockHandler = new Handler(Looper.getMainLooper());
    }

    /* access modifiers changed from: package-private */
    public void onSystemReady() {
        if (IS_CHINA_MARKET.booleanValue()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isShangHaiTimeZone() {
        return "Asia/Shanghai".equals(SystemProperties.get("persist.sys.timezone", ""));
    }

    /* access modifiers changed from: protected */
    public void setGoogleUrl(String url) {
        this.googleUrl = url;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGoogleConnectOK() {
        if (this.googleUrl == null) {
            Log.d(TAG, "google url is null and cancel check.");
            return false;
        }
        HttpURLConnection conn = null;
        Boolean isConnectOk = false;
        BufferedReader reader = null;
        try {
            try {
                HttpURLConnection conn2 = (HttpURLConnection) new URL(this.googleUrl).openConnection();
                conn2.setConnectTimeout(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
                conn2.setReadTimeout(10000);
                conn2.connect();
                int httpResponseCode = conn2.getResponseCode();
                Log.d(TAG, "httpResponseCode = " + httpResponseCode);
                boolean isNeedCheckLength = false;
                if (httpResponseCode == 200) {
                    String networkOperator = TelephonyManager.getDefault().getSimOperator();
                    if (networkOperator == null || !networkOperator.startsWith(CHINA_OPERATOR)) {
                        isConnectOk = true;
                    } else {
                        isNeedCheckLength = true;
                    }
                }
                if (isNeedCheckLength) {
                    reader = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                        int length = sb.toString().length();
                        if (length > 2) {
                            isConnectOk = true;
                            Log.d(TAG, "httpResponseData length : " + length);
                            break;
                        }
                    }
                }
                conn2.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.w(TAG, "close reader Exception.");
                    }
                }
            } catch (Exception e2) {
                Log.d(TAG, "failed to connect google.");
                if (0 != 0) {
                    conn.disconnect();
                }
                if (0 != 0) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    conn.disconnect();
                }
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        Log.w(TAG, "close reader Exception.");
                    }
                }
                throw th;
            }
            return isConnectOk.booleanValue();
        } catch (MalformedURLException e4) {
            Log.d(TAG, "PreventWake MalformedURLException");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getGmsUid() {
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
