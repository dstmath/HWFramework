package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.location.AbstractLocationProvider;

public class HwNLPCache {
    private static final long DEFAULT_DELAY_TIME = 0;
    private static final int GPS_CACHE_SPEED_THROTTLE = 280;
    private static final long GPS_DELAY_REPORT_TIME = 0;
    private static final int MAX_REPORT_GPS_CACHE_TIME = 10000;
    private static final int MAX_REPORT_NLP_CACHE_TIME = 5000;
    private static final int MSG_CHECK_CACHE = 1;
    private static final int MSG_PENDING_CHECK = 3;
    private static final int MSG_REPORT_CACHE = 2;
    private static final long NLP_DELAY_REPORT_TIME = 100;
    private static final long PENDING_CHECK_DELAY_TIME = 3000;
    private static final String TAG = "HwNLPCache";
    private static HwNLPCache sInstance;
    private boolean isTriggerBySelf = false;
    private Location mCachedGPSLocation = new Location("gps");
    private Location mCachedNlpLocation = new Location("network");
    private Context mContext;
    private long mCurrentTimeStamp = 0;
    private Handler mHandler;
    private LocationManager mLocationManager;
    private AbstractLocationProvider.LocationProviderManager mLocationProviderManager;
    private Location mReportLocation;
    private HandlerThread mThread;

    private HwNLPCache(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        this.mContext = context;
        this.mLocationProviderManager = locationProviderManager;
        this.mThread = new HandlerThread("NLPCacheThread");
        this.mThread.start();
        this.mHandler = new NlpCacheHandler(this.mThread.getLooper());
    }

    public static HwNLPCache getInstance(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        HwNLPCache hwNLPCache;
        synchronized (HwNLPCache.class) {
            if (sInstance == null) {
                sInstance = new HwNLPCache(context, locationProviderManager);
            }
            hwNLPCache = sInstance;
        }
        return hwNLPCache;
    }

    public void startCheckCache(boolean reportLocation) {
        if (reportLocation) {
            if (this.isTriggerBySelf) {
                this.isTriggerBySelf = false;
                return;
            }
            this.mCachedGPSLocation = getValidLocationCache("gps");
            LBSLog.i(TAG, false, "get last GPS location :%{public}s", this.mCachedGPSLocation);
            this.mCachedNlpLocation = getValidLocationCache("network");
            LBSLog.i(TAG, false, "get last NLP location :%{public}s", this.mCachedNlpLocation);
            this.mCurrentTimeStamp = SystemClock.elapsedRealtime();
            handleSendMassage(1, 0);
        }
    }

    public void onLocationChange(Location location) {
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
            LBSLog.i(TAG, false, "remove massage ", 1);
        }
        if (this.mHandler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
            LBSLog.i(TAG, false, "remove massage ", 3);
        }
    }

    public void reportLocation() {
        LBSLog.i(TAG, false, "report cache location, provider: %{public}s", this.mReportLocation.getProvider());
        this.mReportLocation.setProvider("network");
        this.mLocationProviderManager.onReportLocation(this.mReportLocation);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refrashRequest() {
        LBSLog.i(TAG, false, "ask LMS to refrash network provider", new Object[0]);
        this.isTriggerBySelf = true;
        LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
        if (util != null) {
            util.applyRequirementsLocked("network");
        }
    }

    public boolean isJumpingPoint(Location location) {
        Location lastLocation = getValidLocationCache("gps");
        if (lastLocation == null) {
            LBSLog.e(TAG, false, "can not get cached gps location", new Object[0]);
            return false;
        }
        long diffTime = SystemClock.elapsedRealtime() - (lastLocation.getElapsedRealtimeNanos() / 1000000);
        if (diffTime <= 0) {
            LBSLog.e(TAG, false, "calculate diff time invalid, diff time:%{public}d", Long.valueOf(diffTime));
            return false;
        }
        float speed = location.distanceTo(lastLocation) / ((float) diffTime);
        if (speed < 280.0f) {
            return false;
        }
        LBSLog.i(TAG, false, "drop the location, due to ivalid jumping speed %{public}f", Float.valueOf(speed));
        return true;
    }

    private Location getValidLocationCache(String provider) {
        LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
        if (util != null) {
            return util.getRealLastLocation(provider);
        }
        return null;
    }

    private void handleSendMassage(int what, long delay) {
        if (!this.mHandler.hasMessages(3) || what != 3) {
            this.mHandler.removeMessages(what);
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, what), delay);
        }
    }

    private class NlpCacheHandler extends Handler {
        public NlpCacheHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwNLPCache.this.handleCheckCache();
            } else if (i == 2) {
                HwNLPCache.this.reportLocation();
            } else if (i != 3) {
                LBSLog.e(HwNLPCache.TAG, false, "receive unexpected message", new Object[0]);
            } else {
                HwNLPCache.this.refrashRequest();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCheckCache() {
        if (isCacheValid(this.mCachedGPSLocation, 10000)) {
            handleSendMassage(2, 0);
        } else if (isCacheValid(this.mCachedNlpLocation, 5000)) {
            handleSendMassage(2, NLP_DELAY_REPORT_TIME);
        } else {
            handleSendMassage(3, 3000);
        }
    }

    private boolean isCacheValid(Location location, int throttleTime) {
        if (location == null) {
            return false;
        }
        long locationTime = location.getElapsedRealtimeNanos() / 1000000;
        LBSLog.i(TAG, false, "%{public}s cache location, locationTime:%{public}d, current time:%{public}d, check throttle:%{public}d", location.getProvider(), Long.valueOf(locationTime), Long.valueOf(this.mCurrentTimeStamp), Integer.valueOf(throttleTime));
        if (locationTime > 0) {
            long j = this.mCurrentTimeStamp;
            if (j - locationTime < ((long) throttleTime) && j - locationTime > 0) {
                this.mReportLocation = new Location(location);
                return true;
            }
        }
        return false;
    }
}
