package com.android.server.security.deviceusage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Slog;
import java.util.Date;

public class HwDeviceFirstUseTime {
    private static final long GET_TIME_DELAY = 3600000;
    private static final long GET_TIME_DELAY_MOBILE_CONNCTION = 21600000;
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final long NTP_INTERVAL = 86400000;
    private static final String TAG = "HwDeviceFirstUseTime";
    private static int TYPE_HAS_GET_TIME = 7;
    /* access modifiers changed from: private */
    public Runnable getTimeRunnable = new Runnable() {
        public void run() {
            if (HwDeviceFirstUseTime.HW_DEBUG) {
                Slog.d(HwDeviceFirstUseTime.TAG, "threadRun");
            }
            if (!HwDeviceFirstUseTime.this.isGetTimeFlag) {
                if (HwDeviceFirstUseTime.this.mCurrentTime == -1 || !HwDeviceFirstUseTime.this.isMobileNetworkConnected() || HwDeviceFirstUseTime.this.mCurrentTime - SystemClock.elapsedRealtime() > HwDeviceFirstUseTime.GET_TIME_DELAY_MOBILE_CONNCTION) {
                    if (HwDeviceFirstUseTime.this.mNtpTime.getCacheAge() >= 86400000) {
                        HwDeviceFirstUseTime.this.mNtpTime.forceRefresh();
                    }
                    if (HwDeviceFirstUseTime.this.mNtpTime.getCacheAge() < 86400000) {
                        long unused = HwDeviceFirstUseTime.this.mTime = HwDeviceFirstUseTime.this.mNtpTime.getCachedNtpTime();
                        if (HwDeviceFirstUseTime.this.mTime != 0) {
                            HwDeviceFirstUseTime.this.obtianHasGetTime(HwDeviceFirstUseTime.this.mTime);
                        }
                        if (HwDeviceFirstUseTime.HW_DEBUG) {
                            Slog.d(HwDeviceFirstUseTime.TAG, "NTP server returned: " + HwDeviceFirstUseTime.this.mTime + " (" + new Date(HwDeviceFirstUseTime.this.mTime) + ")");
                        }
                        return;
                    }
                    if (!HwDeviceFirstUseTime.this.isNetworkConnected()) {
                        HwDeviceFirstUseTime.this.mHandler.removeCallbacks(HwDeviceFirstUseTime.this.getTimeRunnable);
                    } else {
                        long unused2 = HwDeviceFirstUseTime.this.mCurrentTime = SystemClock.elapsedRealtime();
                        HwDeviceFirstUseTime.this.mHandler.postDelayed(HwDeviceFirstUseTime.this.getTimeRunnable, 3600000);
                    }
                    return;
                }
                HwDeviceFirstUseTime.this.mHandler.postDelayed(HwDeviceFirstUseTime.this.getTimeRunnable, HwDeviceFirstUseTime.GET_TIME_DELAY_MOBILE_CONNCTION);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean isGetTimeFlag = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = HwDeviceFirstUseTime.this.mConnectivityManager.getActiveNetworkInfo();
            String action = intent.getAction();
            if (HwDeviceFirstUseTime.HW_DEBUG) {
                Slog.d(HwDeviceFirstUseTime.TAG, "action  " + action);
            }
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                Slog.e(HwDeviceFirstUseTime.TAG, "Receive error broadcast");
            } else if (networkInfo != null && networkInfo.isAvailable() && !HwDeviceFirstUseTime.this.isGetTimeFlag) {
                HwDeviceFirstUseTime.this.mHandler.post(HwDeviceFirstUseTime.this.getTimeRunnable);
            }
        }
    };
    private Handler mCollectionHandler;
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    private Context mContext;
    /* access modifiers changed from: private */
    public long mCurrentTime = -1;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public NtpTrustedTime mNtpTime;
    /* access modifiers changed from: private */
    public long mTime;

    public HwDeviceFirstUseTime(Context context, Handler handler) {
        if (HW_DEBUG) {
            Slog.d(TAG, TAG);
        }
        this.mContext = context;
        this.mCollectionHandler = handler;
        this.mNtpTime = NtpTrustedTime.getInstance(this.mContext);
        Context context2 = this.mContext;
        Context context3 = this.mContext;
        this.mConnectivityManager = (ConnectivityManager) context2.getSystemService("connectivity");
        this.mHandlerThread = new HandlerThread("HwDeviceFirstUseTimeThread");
    }

    public void start() {
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public void triggerGetFirstUseTime() {
        if (HW_DEBUG) {
            Slog.d(TAG, "getFirstUseTime");
        }
        if (isNetworkConnected() && !this.isGetTimeFlag) {
            this.mHandler.post(this.getTimeRunnable);
        }
    }

    /* access modifiers changed from: private */
    public void obtianHasGetTime(long time) {
        if (this.mHandler != null) {
            this.isGetTimeFlag = true;
            this.mCurrentTime = -1;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mHandler.removeCallbacks(this.getTimeRunnable);
            Message msg = Message.obtain();
            msg.what = TYPE_HAS_GET_TIME;
            msg.obj = Long.valueOf(time);
            this.mCollectionHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public boolean isNetworkConnected() {
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /* access modifiers changed from: private */
    public boolean isMobileNetworkConnected() {
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == 0;
    }
}
