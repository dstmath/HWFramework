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

/* access modifiers changed from: package-private */
public class NtpTimeHelper {
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
    @GuardedBy({"this"})
    private final InjectNtpTimeCallback mCallback;
    private final ConnectivityManager mConnMgr;
    private final Handler mHandler;
    private IHwGpsLocationManager mHwGpsLocationManager;
    private IHwGpsLogServices mHwGpsLogServices;
    @GuardedBy({"this"})
    private int mInjectNtpTimeState;
    private final ExponentialBackOff mNtpBackOff;
    private final NtpTrustedTime mNtpTime;
    @GuardedBy({"this"})
    private boolean mOnDemandTimeInjection;
    private Runnable mRetrieveAndInjectNtpTime;
    private final PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: package-private */
    public interface InjectNtpTimeCallback {
        void injectTime(long j, long j2, int i);
    }

    @VisibleForTesting
    NtpTimeHelper(Context context, Looper looper, InjectNtpTimeCallback callback, NtpTrustedTime ntpTime) {
        this.mNtpBackOff = new ExponentialBackOff(300000, 14400000);
        this.mInjectNtpTimeState = 0;
        this.mRetrieveAndInjectNtpTime = new Runnable() {
            /* class com.android.server.location.NtpTimeHelper.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                NtpTimeHelper.this.retrieveAndInjectNtpTime();
            }
        };
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mCallback = callback;
        this.mNtpTime = ntpTime;
        this.mHandler = new Handler(looper);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "NtpTimeHelper");
        this.mHwGpsLocationManager = HwServiceFactory.getHwGpsLocationManager(context);
        this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(context);
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
    public synchronized void retrieveAndInjectNtpTime() {
        if (this.mInjectNtpTimeState != 1) {
            if (!isNetworkConnected()) {
                this.mInjectNtpTimeState = 0;
                InjectTimeRecord injectTimeRecord = this.mHwGpsLocationManager.getInjectTime(0);
                if (injectTimeRecord.getInjectTime() != 0) {
                    this.mHandler.post(new Runnable(injectTimeRecord) {
                        /* class com.android.server.location.$$Lambda$NtpTimeHelper$k0GtK3dcbMDplmz4dv1jWzspA54 */
                        private final /* synthetic */ InjectTimeRecord f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            NtpTimeHelper.this.lambda$retrieveAndInjectNtpTime$0$NtpTimeHelper(this.f$1);
                        }
                    });
                }
                return;
            }
            this.mInjectNtpTimeState = 1;
            this.mWakeLock.acquire(60000);
            new Thread(new Runnable() {
                /* class com.android.server.location.$$Lambda$NtpTimeHelper$xWqlqJuq4jBJ5xhFLCwEKGVB0k */

                @Override // java.lang.Runnable
                public final void run() {
                    NtpTimeHelper.this.blockingGetNtpTimeAndInject();
                }
            }).start();
        }
    }

    public /* synthetic */ void lambda$retrieveAndInjectNtpTime$0$NtpTimeHelper(InjectTimeRecord injectTimeRecord) {
        this.mCallback.injectTime(injectTimeRecord.getInjectTime(), SystemClock.elapsedRealtime(), injectTimeRecord.getUncertainty());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void blockingGetNtpTimeAndInject() {
        long delay;
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
                    HwServiceFactory.getHwGpsXtraDownloadReceiver().setNtpTime(time, timeReference);
                    this.mHwGpsLogServices.injectExtraParam("ntp_time");
                    currentNtpTime = (SystemClock.elapsedRealtime() + time) - timeReference;
                }
                InjectTimeRecord injectTimeRecord = this.mHwGpsLocationManager.getInjectTime(currentNtpTime);
                if (injectTimeRecord.getInjectTime() != 0) {
                    this.mHandler.post(new Runnable(injectTimeRecord) {
                        /* class com.android.server.location.$$Lambda$NtpTimeHelper$VWcGj6bD_x0NVYo9CMNar4104Wk */
                        private final /* synthetic */ InjectTimeRecord f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            NtpTimeHelper.this.lambda$blockingGetNtpTimeAndInject$1$NtpTimeHelper(this.f$1);
                        }
                    });
                }
                delay = 86400000;
                this.mNtpBackOff.reset();
            } else {
                Log.e("NtpTimeHelper", "requestTime failed");
                delay = this.mNtpBackOff.nextBackoffMillis();
            }
            if (DEBUG) {
                Log.d("NtpTimeHelper", String.format("onDemandTimeInjection=%s, refreshSuccess=%s, delay=%s", Boolean.valueOf(this.mOnDemandTimeInjection), Boolean.valueOf(refreshSuccess), Long.valueOf(delay)));
            }
            if ((this.mOnDemandTimeInjection || !refreshSuccess) && !this.mHandler.hasCallbacks(this.mRetrieveAndInjectNtpTime)) {
                this.mHandler.postDelayed(this.mRetrieveAndInjectNtpTime, delay);
            }
        }
        try {
            this.mWakeLock.release();
        } catch (Exception e) {
        }
    }

    public /* synthetic */ void lambda$blockingGetNtpTimeAndInject$1$NtpTimeHelper(InjectTimeRecord injectTimeRecord) {
        this.mCallback.injectTime(injectTimeRecord.getInjectTime(), SystemClock.elapsedRealtime(), injectTimeRecord.getUncertainty());
    }
}
