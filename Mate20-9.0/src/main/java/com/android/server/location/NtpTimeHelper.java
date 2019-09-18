package com.android.server.location;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.NtpTrustedTime;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceFactory;
import java.util.Date;

class NtpTimeHelper {
    private static final boolean DEBUG = Log.isLoggable("NtpTimeHelper", 3);
    private static final long MAX_RETRY_INTERVAL = 14400000;
    @VisibleForTesting
    static final long NTP_INTERVAL = 86400000;
    @VisibleForTesting
    static final long RETRY_INTERVAL = 300000;
    private static final int STATE_IDLE = 2;
    private static final int STATE_PENDING_NETWORK = 0;
    private static final int STATE_RETRIEVING_AND_INJECTING = 1;
    private static final String TAG = "NtpTimeHelper";
    private static final String WAKELOCK_KEY = "NtpTimeHelper";
    private static final long WAKELOCK_TIMEOUT_MILLIS = 60000;
    @GuardedBy("this")
    private final InjectNtpTimeCallback mCallback;
    private final ConnectivityManager mConnMgr;
    private final Handler mHandler;
    IHwGpsLocationManager mHwGpsLocationManager;
    private IHwGpsLogServices mHwGpsLogServices;
    @GuardedBy("this")
    private int mInjectNtpTimeState;
    private final ExponentialBackOff mNtpBackOff;
    private final NtpTrustedTime mNtpTime;
    @GuardedBy("this")
    private boolean mOnDemandTimeInjection;
    private Runnable mRetrieveAndInjectNtpTime;
    private final PowerManager.WakeLock mWakeLock;

    interface InjectNtpTimeCallback {
        void gpsXtra(long j, long j2);

        void injectTime(long j, long j2, int i);
    }

    @VisibleForTesting
    NtpTimeHelper(Context context, Looper looper, InjectNtpTimeCallback callback, NtpTrustedTime ntpTime) {
        this.mNtpBackOff = new ExponentialBackOff(300000, 14400000);
        this.mInjectNtpTimeState = 0;
        this.mRetrieveAndInjectNtpTime = new Runnable() {
            public void run() {
                NtpTimeHelper.this.retrieveAndInjectNtpTime();
            }
        };
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mCallback = callback;
        this.mNtpTime = ntpTime;
        this.mHandler = new Handler(looper);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "NtpTimeHelper");
        this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(context);
        this.mHwGpsLocationManager = HwServiceFactory.getHwGpsLocationManager(context);
    }

    NtpTimeHelper(Context context, Looper looper, InjectNtpTimeCallback callback) {
        this(context, looper, callback, NtpTrustedTime.getInstance(context));
    }

    /* access modifiers changed from: package-private */
    public synchronized void enablePeriodicTimeInjection() {
        this.mOnDemandTimeInjection = true;
    }

    /* access modifiers changed from: package-private */
    public synchronized void onNetworkAvailable() {
        if (this.mInjectNtpTimeState == 0) {
            retrieveAndInjectNtpTime();
        }
    }

    private boolean isNetworkConnected() {
        NetworkInfo activeNetworkInfo = this.mConnMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        return;
     */
    public synchronized void retrieveAndInjectNtpTime() {
        if (this.mInjectNtpTimeState != 1) {
            if (!isNetworkConnected()) {
                this.mInjectNtpTimeState = 0;
                InjectTimeRecord injectTimeRecord = this.mHwGpsLocationManager.getInjectTime(0);
                if (0 != injectTimeRecord.getInjectTime()) {
                    this.mHandler.post(new Runnable(injectTimeRecord) {
                        private final /* synthetic */ InjectTimeRecord f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            NtpTimeHelper.this.mCallback.injectTime(this.f$1.getInjectTime(), SystemClock.elapsedRealtime(), this.f$1.getUncertainty());
                        }
                    });
                }
            } else {
                this.mInjectNtpTimeState = 1;
                this.mWakeLock.acquire(60000);
                new Thread(new Runnable() {
                    public final void run() {
                        NtpTimeHelper.this.blockingGetNtpTimeAndInject();
                    }
                }).start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void blockingGetNtpTimeAndInject() {
        long delay;
        long delay2;
        boolean refreshSuccess = true;
        if (this.mNtpTime.getCacheAge() >= 86400000) {
            refreshSuccess = this.mNtpTime.forceRefresh();
            this.mHwGpsLogServices.updateNtpDloadStatus(refreshSuccess);
            if (refreshSuccess) {
                this.mHwGpsLogServices.updateNtpServerInfo(this.mNtpTime.getCachedNtpIpAddress());
            }
        }
        synchronized (this) {
            this.mInjectNtpTimeState = 2;
            if (this.mNtpTime.getCacheAge() < 86400000) {
                long time = this.mNtpTime.getCachedNtpTime();
                long timeReference = this.mNtpTime.getCachedNtpTimeReference();
                long certainty = this.mNtpTime.getCacheCertainty();
                if (DEBUG) {
                    long now = System.currentTimeMillis();
                    Log.d("NtpTimeHelper", "NTP server returned: " + time + " (" + new Date(time) + ") reference: " + timeReference + " certainty: " + certainty + " system time offset: " + (time - now));
                }
                long currentNtpTime = 0;
                if (this.mHwGpsLocationManager.checkNtpTime(time, timeReference)) {
                    this.mCallback.gpsXtra(0, timeReference);
                    this.mHwGpsLogServices.injectExtraParam("ntp_time");
                    currentNtpTime = (SystemClock.elapsedRealtime() + time) - timeReference;
                }
                InjectTimeRecord injectTimeRecord = this.mHwGpsLocationManager.getInjectTime(currentNtpTime);
                if (0 != injectTimeRecord.getInjectTime()) {
                    this.mHandler.post(new Runnable(injectTimeRecord) {
                        private final /* synthetic */ InjectTimeRecord f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            NtpTimeHelper.this.mCallback.injectTime(this.f$1.getInjectTime(), SystemClock.elapsedRealtime(), this.f$1.getUncertainty());
                        }
                    });
                }
                delay = 86400000;
                this.mNtpBackOff.reset();
            } else {
                Log.e("NtpTimeHelper", "requestTime failed");
                delay = this.mNtpBackOff.nextBackoffMillis();
            }
            delay2 = delay;
            if (DEBUG) {
                Log.d("NtpTimeHelper", String.format("onDemandTimeInjection=%s, refreshSuccess=%s, delay=%s", new Object[]{Boolean.valueOf(this.mOnDemandTimeInjection), Boolean.valueOf(refreshSuccess), Long.valueOf(delay2)}));
            }
            if ((this.mOnDemandTimeInjection || !refreshSuccess) && !this.mHandler.hasCallbacks(this.mRetrieveAndInjectNtpTime)) {
                this.mHandler.postDelayed(this.mRetrieveAndInjectNtpTime, delay2);
            }
        }
        long j = delay2;
        try {
            this.mWakeLock.release();
        } catch (Exception e) {
        }
    }
}
