package com.huawei.server.security.antimal;

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
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.util.NtpTrustedTimeEx;
import com.huawei.util.LogEx;
import java.util.Date;

public class HwDeviceFirstUseTime {
    private static final long GET_TIME_DELAY = 3600000;
    private static final long GET_TIME_DELAY_MOBILE_CONNECTION = 21600000;
    private static final long INVALID_TIME = -1;
    private static final boolean IS_HW_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final long NTP_INTERVAL = 86400000;
    private static final String TAG = "HwDeviceFirstUseTime";
    private static final int TYPE_HAS_GET_TIME = 7;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.antimal.HwDeviceFirstUseTime.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                Log.e(HwDeviceFirstUseTime.TAG, "onReceive intent or action is null!");
                return;
            }
            String action = intent.getAction();
            if (HwDeviceFirstUseTime.IS_HW_DEBUG) {
                Log.d(HwDeviceFirstUseTime.TAG, "onReceive action " + action);
            }
            if (HwDeviceFirstUseTime.this.mConnectivityManager == null) {
                Log.e(HwDeviceFirstUseTime.TAG, "onReceive mConnectivityManager is null!");
                return;
            }
            NetworkInfo networkInfo = HwDeviceFirstUseTime.this.mConnectivityManager.getActiveNetworkInfo();
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(action) || HwDeviceFirstUseTime.this.mHandler == null) {
                Log.w(HwDeviceFirstUseTime.TAG, "onReceive error broadcast!");
            } else if (networkInfo != null && networkInfo.isAvailable() && !HwDeviceFirstUseTime.this.mIsGetTimeFlag) {
                HwDeviceFirstUseTime.this.mHandler.post(HwDeviceFirstUseTime.this.mGetTimeRunnable);
            }
        }
    };
    private Handler mCollectionHandler;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private long mCurrentTime = INVALID_TIME;
    private Runnable mGetTimeRunnable = new Runnable() {
        /* class com.huawei.server.security.antimal.HwDeviceFirstUseTime.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (HwDeviceFirstUseTime.IS_HW_DEBUG) {
                Log.d(HwDeviceFirstUseTime.TAG, "threadRun");
            }
            if (!HwDeviceFirstUseTime.this.mIsGetTimeFlag && HwDeviceFirstUseTime.this.mHandler != null) {
                if (HwDeviceFirstUseTime.this.mCurrentTime == HwDeviceFirstUseTime.INVALID_TIME || !HwDeviceFirstUseTime.this.isMobileNetworkConnected() || HwDeviceFirstUseTime.this.mCurrentTime - SystemClock.elapsedRealtime() > 21600000) {
                    if (HwDeviceFirstUseTime.this.mNtpTime.getCacheAge() >= HwDeviceFirstUseTime.NTP_INTERVAL) {
                        HwDeviceFirstUseTime.this.mNtpTime.forceRefresh();
                    }
                    if (HwDeviceFirstUseTime.this.mNtpTime.getCacheAge() < HwDeviceFirstUseTime.NTP_INTERVAL) {
                        long time = HwDeviceFirstUseTime.this.mNtpTime.getCachedNtpTime();
                        if (time != 0) {
                            HwDeviceFirstUseTime.this.obtainHasGetTime(time);
                        }
                        if (HwDeviceFirstUseTime.IS_HW_DEBUG) {
                            Log.d(HwDeviceFirstUseTime.TAG, "NTP server returned: " + time + " (" + new Date(time) + ")");
                        }
                    } else if (!HwDeviceFirstUseTime.this.isNetworkConnected()) {
                        HwDeviceFirstUseTime.this.mHandler.removeCallbacks(HwDeviceFirstUseTime.this.mGetTimeRunnable);
                    } else {
                        HwDeviceFirstUseTime.this.mCurrentTime = SystemClock.elapsedRealtime();
                        HwDeviceFirstUseTime.this.mHandler.postDelayed(HwDeviceFirstUseTime.this.mGetTimeRunnable, HwDeviceFirstUseTime.GET_TIME_DELAY);
                    }
                } else {
                    HwDeviceFirstUseTime.this.mHandler.postDelayed(HwDeviceFirstUseTime.this.mGetTimeRunnable, 21600000);
                }
            }
        }
    };
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsGetTimeFlag = false;
    private NtpTrustedTimeEx mNtpTime;

    public HwDeviceFirstUseTime(Context context, Handler handler) {
        if (IS_HW_DEBUG) {
            Log.d(TAG, TAG);
        }
        this.mContext = context;
        this.mCollectionHandler = handler;
        this.mNtpTime = NtpTrustedTimeEx.getInstance(this.mContext);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
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
        Handler handler;
        if (IS_HW_DEBUG) {
            Log.d(TAG, "getFirstUseTime");
        }
        if (isNetworkConnected() && !this.mIsGetTimeFlag && (handler = this.mHandler) != null) {
            handler.post(this.mGetTimeRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void obtainHasGetTime(long time) {
        if (this.mHandler != null) {
            this.mIsGetTimeFlag = true;
            this.mCurrentTime = INVALID_TIME;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mHandler.removeCallbacks(this.mGetTimeRunnable);
            if (this.mCollectionHandler != null) {
                Message msg = Message.obtain();
                msg.what = TYPE_HAS_GET_TIME;
                msg.obj = Long.valueOf(time);
                this.mCollectionHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            Log.e(TAG, "isNetworkConnected mConnectivityManager is null!");
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMobileNetworkConnected() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            Log.e(TAG, "isMobileNetworkConnected mConnectivityManager is null!");
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || networkInfo.getType() != 0) {
            return false;
        }
        return true;
    }
}
