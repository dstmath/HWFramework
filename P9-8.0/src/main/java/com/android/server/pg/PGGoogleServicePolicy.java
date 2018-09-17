package com.android.server.pg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.NetworkInfo;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.util.Log;
import com.android.server.display.DisplayTransformManager;
import com.android.server.power.IHwShutdownThread;
import com.huawei.pgmng.log.LogPower;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PGGoogleServicePolicy {
    private static final String GOOGLE_GMS_PAC = "com.google.android.gms";
    private static final String GOOGLE_GSF_PAC = "com.google.android.gsf";
    private static final long STATIC_WAKELOCK_CHECK_TIME = 1800000;
    private static final String TAG = "PGGoogleServicePolicy";
    private static final String US_GOOGLE_URL = "http://www.google.com";
    private final BroadcastReceiver mBroadcastReceiver;
    private long mCheckDuration;
    private Context mContext;
    private boolean mEnabled;
    private int mGmsUid;
    private boolean mIsGoogleServerConnectedOK;
    private Handler mWakeLockHandler;
    private Runnable mWakelockMonitor;

    public PGGoogleServicePolicy(Context context) {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.hw_optb", 0) != 156) {
            z = true;
        }
        this.mIsGoogleServerConnectedOK = z;
        this.mGmsUid = -1;
        this.mEnabled = true;
        this.mCheckDuration = 1800000;
        this.mBroadcastReceiver = new BroadcastReceiver() {
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
                                PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                                PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, 0);
                            }
                        }
                    }
                }
            }
        };
        this.mWakelockMonitor = new Runnable() {
            public void run() {
                Log.d(PGGoogleServicePolicy.TAG, "check isGoogleConnect start");
                new Thread() {
                    public void run() {
                        PGGoogleServicePolicy.this.mGmsUid = PGGoogleServicePolicy.this.getGmsUid();
                        if (PGGoogleServicePolicy.this.isGoogleConnectOK()) {
                            PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = true;
                            Log.d(PGGoogleServicePolicy.TAG, "connect google success, PreventWake change to invalid");
                        } else {
                            PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK = false;
                            Log.d(PGGoogleServicePolicy.TAG, "connect google failed, PreventWake change to valid");
                        }
                        LogPower.push(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, PGGoogleServicePolicy.this.mIsGoogleServerConnectedOK ? "1" : "0");
                    }
                }.start();
                PGGoogleServicePolicy.this.mWakeLockHandler.removeCallbacks(PGGoogleServicePolicy.this.mWakelockMonitor);
                PGGoogleServicePolicy.this.mWakeLockHandler.postDelayed(PGGoogleServicePolicy.this.mWakelockMonitor, PGGoogleServicePolicy.this.mCheckDuration);
            }
        };
        this.mContext = context;
        this.mWakeLockHandler = new Handler();
    }

    public void onSystemReady() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
    }

    private boolean isGoogleConnectOK() {
        HttpURLConnection conn = null;
        int httpResponseCode = 599;
        try {
            try {
                conn = (HttpURLConnection) new URL(US_GOOGLE_URL).openConnection();
                conn.setConnectTimeout(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
                conn.setReadTimeout(10000);
                conn.connect();
                httpResponseCode = conn.getResponseCode();
                Log.d(TAG, "httpResponseCode = " + httpResponseCode);
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                Log.d(TAG, "failed to connect google.");
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Throwable th) {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE == httpResponseCode) {
                return true;
            }
            return false;
        } catch (MalformedURLException e2) {
            Log.d(TAG, "PreventWake MalformedURLException");
            return false;
        }
    }

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
        if (!(packageName == null || 1 != (NetworkConstants.ARP_HWTYPE_RESERVED_HI & flags) || this.mIsGoogleServerConnectedOK)) {
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
