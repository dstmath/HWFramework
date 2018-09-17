package com.android.internal.telephony;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.telephony.Rlog;

public class HwLocationUpdateManager {
    private static final double BASE_RADIAN = 180.0d;
    private static final double CONVERSION_ACCURACY = 10000.0d;
    private static final int DIGIT_TWO = 2;
    private static final double EARTH_RADIUS = 6378137.0d;
    private static final int EVENT_GPS_LOCATION_FIX_TIMEOUT = 2;
    public static final int EVENT_LOCATION_CHANGED = 100;
    private static final int EVENT_NET_LOCATION_FIX_TIMEOUT = 1;
    private static final int GPS_LOCATION_FIX_TIMEOUT = 60000;
    private static final int LOCATION_ACCURACY_DIFF = 200;
    private static final int LOCATION_UPDATE_DISTANCE = 0;
    private static final int LOCATION_UPDATE_INTERVAL = 0;
    private static final String LOG_TAG = "HwLocationUpdateManager";
    private static final int NET_LOCATION_FIX_TIMEOUT = 5000;
    private static final long TWO_MINUTES_NANOS = 120000000000L;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private LocationListener mGpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location != null) {
                HwLocationUpdateManager.this.log("onLocationChanged, gps location");
                HwLocationUpdateManager.this.onLocationChangedInner(location, "gps");
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private Handler mHandler;
    private Location mLocation;
    private LocationManager mLocationManager;
    private Handler mMyHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 1:
                        boolean useNetOnly = ((Boolean) msg.obj).booleanValue();
                        HwLocationUpdateManager.this.log("EVENT_NET_LOCATION_FIX_TIMEOUT, unregister NetworkLocationListener useNetOnly: " + useNetOnly);
                        HwLocationUpdateManager.this.unregisterListener(HwLocationUpdateManager.this.mNetworkLocationListener);
                        if (!useNetOnly && HwLocationUpdateManager.this.mLocationManager != null && HwLocationUpdateManager.this.mLocationManager.isProviderEnabled("gps")) {
                            HwLocationUpdateManager.this.registerListener("gps", 0, 0.0f, HwLocationUpdateManager.this.mGpsLocationListener);
                            HwLocationUpdateManager.this.mMyHandler.sendMessageDelayed(HwLocationUpdateManager.this.mMyHandler.obtainMessage(2), 60000);
                            HwLocationUpdateManager.this.log("retry gps location.");
                            return;
                        }
                        return;
                    case 2:
                        HwLocationUpdateManager.this.log("EVENT_GPS_LOCATION_FIX_TIMEOUT, unregister GpsLocationListener.");
                        HwLocationUpdateManager.this.unregisterListener(HwLocationUpdateManager.this.mGpsLocationListener);
                        return;
                    default:
                        return;
                }
            } catch (IllegalArgumentException e) {
                HwLocationUpdateManager.this.loge("unregisterListener got IllegalArgumentException");
            } catch (SecurityException e2) {
                HwLocationUpdateManager.this.loge("unregisterListener got SecurityException");
            } catch (RuntimeException e3) {
                HwLocationUpdateManager.this.loge("unregisterListener got RuntimeException");
            }
        }
    };
    private LocationListener mNetworkLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location != null) {
                HwLocationUpdateManager.this.log("onLocationChanged, network location");
                HwLocationUpdateManager.this.onLocationChangedInner(location, "network");
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationListener mPassiveLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location != null) {
                HwLocationUpdateManager.this.log("onLocationChanged, passive location");
                HwLocationUpdateManager.this.onLocationChangedInner(location, "passive");
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public HwLocationUpdateManager(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        if (this.mContext != null) {
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    public synchronized void registerPassiveLocationUpdate() {
        if (this.mContext != null) {
            if (this.mLocationManager == null) {
                loge("registerPassiveLocationUpdate, mLocationManager is null and getSystemService again.");
                this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            }
            if (this.mLocationManager != null) {
                try {
                    if (this.mLocationManager.isProviderEnabled("passive")) {
                        log("registerPassiveLocationUpdate.");
                        registerListener("passive", 0, 0.0f, this.mPassiveLocationListener);
                    }
                } catch (IllegalArgumentException e) {
                    loge("registerPassiveLocationUpdate got IllegalArgumentException");
                } catch (SecurityException e2) {
                    loge("registerPassiveLocationUpdate got SecurityException");
                } catch (RuntimeException e3) {
                    loge("registerPassiveLocationUpdate got RuntimeException");
                }
            }
        } else {
            return;
        }
    }

    public synchronized void unregisterPassiveLocationUpdate() {
        try {
            log("unregisterPassiveLocationUpdate.");
            unregisterListener(this.mPassiveLocationListener);
        } catch (IllegalArgumentException e) {
            loge("unregisterPassiveLocationUpdate got IllegalArgumentException");
        } catch (RuntimeException e2) {
            loge("unregisterPassiveLocationUpdate got RuntimeException");
        }
        return;
    }

    public synchronized void requestLocationUpdate(boolean useNetOnly) {
        if (this.mContext != null) {
            if (this.mLocationManager == null) {
                loge("requestLocationUpdate, mLocationManager is null and getSystemService again.");
                this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            }
            if (this.mLocationManager != null) {
                try {
                    boolean isNetProviderEnabled = this.mLocationManager.isProviderEnabled("network");
                    boolean isGpsProviderEnabled = this.mLocationManager.isProviderEnabled("gps");
                    boolean isNetAvailable = isNetworkAvailable();
                    log("requestLocationUpdate, network enabled: " + isNetProviderEnabled + ", gps enabled: " + isGpsProviderEnabled + ", isNetAvailable: " + isNetAvailable + ", useNetOnly: " + useNetOnly);
                    if (isNetProviderEnabled && isNetAvailable) {
                        registerListener("network", 0, 0.0f, this.mNetworkLocationListener);
                        this.mMyHandler.sendMessageDelayed(this.mMyHandler.obtainMessage(1, Boolean.valueOf(useNetOnly)), 5000);
                        log("requestLocationUpdate, use network location.");
                    } else if (isGpsProviderEnabled && (useNetOnly ^ 1) != 0) {
                        registerListener("gps", 0, 0.0f, this.mGpsLocationListener);
                        this.mMyHandler.sendMessageDelayed(this.mMyHandler.obtainMessage(2), 60000);
                        log("requestLocationUpdate, use gps location.");
                    }
                } catch (IllegalArgumentException e) {
                    loge("requestLocationUpdate got IllegalArgumentException");
                } catch (SecurityException e2) {
                    loge("requestLocationUpdate got SecurityException");
                } catch (RuntimeException e3) {
                    loge("requestLocationUpdate got RuntimeException");
                }
            }
        } else {
            return;
        }
        return;
    }

    public synchronized void stopLocationUpdate() {
        stopNetworkLocation();
        stopGpsLocation();
    }

    /* JADX WARNING: Missing block: B:18:0x0034, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isNetworkLocationAvailable() {
        boolean ret = false;
        if (this.mContext == null) {
            return false;
        }
        if (this.mLocationManager == null) {
            loge("isNetworkLocationAvailable, mLocationManager is null and getSystemService again.");
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        }
        if (this.mLocationManager != null) {
            if (this.mLocationManager.isProviderEnabled("network")) {
                ret = isNetworkAvailable();
            } else {
                ret = false;
            }
        }
    }

    public boolean isLocationEnabled() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0) {
            z = true;
        }
        return z;
    }

    public void setLocationEnabled(boolean enabled) {
        if (this.mContext != null) {
            int mode;
            if (enabled) {
                mode = -1;
            } else {
                mode = 0;
            }
            Secure.putInt(this.mContext.getContentResolver(), "location_mode", mode);
        }
    }

    private static double rad(double d) {
        return (3.141592653589793d * d) / BASE_RADIAN;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        return ((double) Math.round(CONVERSION_ACCURACY * ((2.0d * Math.asin(Math.sqrt(Math.pow(Math.sin((radLat1 - radLat2) / 2.0d), 2.0d) + ((Math.cos(radLat1) * Math.cos(radLat2)) * Math.pow(Math.sin((rad(lng1) - rad(lng2)) / 2.0d), 2.0d))))) * EARTH_RADIUS))) / CONVERSION_ACCURACY;
    }

    private boolean isNetworkAvailable() {
        if (this.mContext == null) {
            return false;
        }
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        NetworkInfo ni = this.mConnectivityManager == null ? null : this.mConnectivityManager.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            return false;
        }
        return true;
    }

    private synchronized void onLocationChangedInner(Location location, String provider) {
        if ("network".equals(provider)) {
            stopNetworkLocation();
        }
        if ("gps".equals(provider)) {
            stopGpsLocation();
        }
        if (isBetterLocation(location, this.mLocation)) {
            this.mLocation = location;
            if (this.mHandler != null) {
                log("onLocationChangedInner, send EVENT_LOCATION_CHANGED.");
                this.mHandler.sendMessage(this.mHandler.obtainMessage(100, this.mLocation));
            }
        }
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (location == null) {
            return false;
        }
        if (currentBestLocation == null) {
            return true;
        }
        long timeDelta = location.getElapsedRealtimeNanos() - currentBestLocation.getElapsedRealtimeNanos();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES_NANOS;
        boolean isSignificantlyOlder = timeDelta < -120000000000L;
        boolean isNewer = timeDelta > 0;
        log("isBetterLocation, timeDelta: " + timeDelta + "ns" + ", isSignificantlyNewer: " + isSignificantlyNewer + ", isSignificantlyOlder: " + isSignificantlyOlder + ", isNewer: " + isNewer);
        if (isSignificantlyNewer) {
            return true;
        }
        if (isSignificantlyOlder) {
            return false;
        }
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > LOCATION_ACCURACY_DIFF;
        log("isBetterLocation, accuracyDelta: " + accuracyDelta + ", isLessAccurate: " + isLessAccurate + ", isMoreAccurate: " + isMoreAccurate + ", isSignificantlyLessAccurate: " + isSignificantlyLessAccurate);
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return true;
        }
        if (isNewer && (isLessAccurate ^ 1) != 0) {
            return true;
        }
        if (isNewer && (isSignificantlyLessAccurate ^ 1) != 0 && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 != null) {
            return provider1.equals(provider2);
        }
        return provider2 == null;
    }

    private void stopNetworkLocation() {
        try {
            if (this.mMyHandler.hasMessages(1)) {
                log("stopNetworkLocation, remove EVENT_NET_LOCATION_FIX_TIMEOUT.");
                this.mMyHandler.removeMessages(1);
                unregisterListener(this.mNetworkLocationListener);
            }
        } catch (IllegalArgumentException e) {
            loge("stopNetworkLocation got IllegalArgumentException");
        } catch (RuntimeException e2) {
            loge("stopNetworkLocation got RuntimeException");
        }
    }

    private void stopGpsLocation() {
        try {
            if (this.mMyHandler.hasMessages(2)) {
                log("stopGpsLocation, remove EVENT_GPS_LOCATION_FIX_TIMEOUT.");
                this.mMyHandler.removeMessages(2);
                unregisterListener(this.mGpsLocationListener);
            }
        } catch (IllegalArgumentException e) {
            loge("stopGpsLocation got IllegalArgumentException");
        } catch (RuntimeException e2) {
            loge("stopGpsLocation got RuntimeException");
        }
    }

    private void registerListener(String provider, long minTime, float minDistance, LocationListener listener) {
        long bid = Binder.clearCallingIdentity();
        try {
            if (this.mLocationManager != null) {
                this.mLocationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
            }
            Binder.restoreCallingIdentity(bid);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(bid);
        }
    }

    private void unregisterListener(LocationListener listener) {
        long bid = Binder.clearCallingIdentity();
        try {
            if (this.mLocationManager != null) {
                this.mLocationManager.removeUpdates(listener);
            }
            Binder.restoreCallingIdentity(bid);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(bid);
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
