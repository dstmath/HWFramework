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
import android.provider.Settings;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;

public class HwLocationUpdateManager {
    private static final double BASE_RADIAN = 180.0d;
    private static final double CONVERSION_ACCURACY = 10000.0d;
    private static final int DIGIT_TWO = 2;
    private static final int DOMESTIC_BETA = 3;
    private static final double EARTH_RADIUS = 6378137.0d;
    private static final int EVENT_GPS_LOCATION_FIX_TIMEOUT = 2;
    public static final int EVENT_LOCATION_CHANGED = 100;
    private static final int EVENT_NET_LOCATION_FIX_TIMEOUT = 1;
    private static final int GPS_LOCATION_FIX_TIMEOUT = 40000;
    private static final int LOCATION_ACCURACY_DIFF = 200;
    private static final int LOCATION_UPDATE_DISTANCE = 0;
    private static final int LOCATION_UPDATE_INTERVAL = 0;
    private static final String LOG_TAG = "HwLocationUpdateManager";
    private static final int NET_LOCATION_FIX_TIMEOUT = 11000;
    private static final int OVERSEA_BETA = 5;
    private static final int TRUSTED_LOCATION_ACCURACY = 500;
    private static final long TWO_MINUTES_NANOS = 120000000000L;
    private static final int USER_TYPE = SystemPropertiesEx.getInt("ro.logsystem.usertype", 0);
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private LocationListener mGpsLocationListener = new LocationListener() {
        /* class com.android.internal.telephony.HwLocationUpdateManager.AnonymousClass1 */

        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            if (location != null) {
                HwLocationUpdateManager.this.log("onLocationChanged, gps location");
                HwLocationUpdateManager.this.onLocationChangedInner(location, "gps");
            }
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private Handler mHandler;
    private Location mLocation;
    private LocationManager mLocationManager;
    private Handler mMyHandler = new Handler() {
        /* class com.android.internal.telephony.HwLocationUpdateManager.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i == 1) {
                    boolean useNetOnly = ((Boolean) msg.obj).booleanValue();
                    HwLocationUpdateManager hwLocationUpdateManager = HwLocationUpdateManager.this;
                    hwLocationUpdateManager.log("EVENT_NET_LOCATION_FIX_TIMEOUT, unregister NetworkLocationListener useNetOnly: " + useNetOnly);
                    HwLocationUpdateManager.this.unregisterListener(HwLocationUpdateManager.this.mNetworkLocationListener);
                    if (!useNetOnly && HwLocationUpdateManager.this.mLocationManager != null && HwLocationUpdateManager.this.mLocationManager.isProviderEnabled("gps")) {
                        HwLocationUpdateManager.this.registerListener("gps", 0, 0.0f, HwLocationUpdateManager.this.mGpsLocationListener);
                        HwLocationUpdateManager.this.mMyHandler.sendMessageDelayed(HwLocationUpdateManager.this.mMyHandler.obtainMessage(2), 40000);
                        HwLocationUpdateManager.this.log("retry gps location.");
                    }
                } else if (i == 2) {
                    HwLocationUpdateManager.this.log("EVENT_GPS_LOCATION_FIX_TIMEOUT, unregister GpsLocationListener.");
                    HwLocationUpdateManager.this.unregisterListener(HwLocationUpdateManager.this.mGpsLocationListener);
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
        /* class com.android.internal.telephony.HwLocationUpdateManager.AnonymousClass2 */

        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            if (location != null) {
                HwLocationUpdateManager.this.log("onLocationChanged, network location");
                HwLocationUpdateManager.this.onLocationChangedInner(location, "network");
            }
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationListener mPassiveLocationListener = new LocationListener() {
        /* class com.android.internal.telephony.HwLocationUpdateManager.AnonymousClass4 */

        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            if (location != null) {
                HwLocationUpdateManager.this.log("onLocationChanged, passive location");
                HwLocationUpdateManager.this.onLocationChangedInner(location, "passive");
            }
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public HwLocationUpdateManager(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mLocationManager = (LocationManager) context2.getSystemService("location");
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private static double rad(double d) {
        return (3.141592653589793d * d) / BASE_RADIAN;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        return ((double) Math.round(((Math.asin(Math.sqrt(Math.pow(Math.sin((radLat1 - radLat2) / 2.0d), 2.0d) + ((Math.cos(radLat1) * Math.cos(radLat2)) * Math.pow(Math.sin((rad(lng1) - rad(lng2)) / 2.0d), 2.0d)))) * 2.0d) * EARTH_RADIUS) * CONVERSION_ACCURACY)) / CONVERSION_ACCURACY;
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
                        this.mMyHandler.sendMessageDelayed(this.mMyHandler.obtainMessage(1, Boolean.valueOf(useNetOnly)), 11000);
                        log("requestLocationUpdate, use network location.");
                    } else if (isGpsProviderEnabled && !useNetOnly) {
                        registerListener("gps", 0, 0.0f, this.mGpsLocationListener);
                        this.mMyHandler.sendMessageDelayed(this.mMyHandler.obtainMessage(2), 40000);
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
        }
    }

    public synchronized void stopLocationUpdate() {
        stopNetworkLocation();
        stopGpsLocation();
    }

    public synchronized boolean isNetworkLocationAvailable() {
        boolean ret = false;
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (this.mLocationManager == null) {
            loge("isNetworkLocationAvailable, mLocationManager is null and getSystemService again.");
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        }
        if (this.mLocationManager != null) {
            if (this.mLocationManager.isProviderEnabled("network") && isNetworkAvailable()) {
                z = true;
            }
            ret = z;
        }
        return ret;
    }

    public synchronized boolean isLocationEnabled() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (this.mLocationManager == null) {
            loge("isLocationEnabled, mLocationManager is null and getSystemService again.");
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        }
        if (this.mLocationManager != null) {
            z = this.mLocationManager.isLocationEnabled();
        }
        return z;
    }

    public void setLocationEnabled(boolean enabled) {
        if (this.mContext != null) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "location_mode", enabled ? DOMESTIC_BETA : 0);
        }
    }

    private boolean isNetworkAvailable() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        }
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        NetworkInfo ni = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void onLocationChangedInner(Location location, String provider) {
        if ("network".equals(provider)) {
            stopNetworkLocation();
        }
        if ("gps".equals(provider)) {
            stopGpsLocation();
        }
        log("onLocationChangedInner, location source: " + location.getProvider() + ", accuracy: " + location.getAccuracy() + ", is mock location: " + location.isFromMockProvider());
        if ((isBetaUser() || !location.isFromMockProvider()) && isBetterLocation(location, this.mLocation) && ((int) location.getAccuracy()) < TRUSTED_LOCATION_ACCURACY) {
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
        log("isBetterLocation, timeDelta: " + timeDelta + "ns, isSignificantlyNewer: " + isSignificantlyNewer + ", isSignificantlyOlder: " + isSignificantlyOlder + ", isNewer: " + isNewer);
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
        if (isNewer && !isLessAccurate) {
            return true;
        }
        if (!isNewer || isSignificantlyLessAccurate || !isFromSameProvider) {
            return false;
        }
        return true;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private boolean isBetaUser() {
        int i = USER_TYPE;
        return i == DOMESTIC_BETA || i == OVERSEA_BETA;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListener(String provider, long minTime, float minDistance, LocationListener listener) {
        long bid = Binder.clearCallingIdentity();
        try {
            if (this.mLocationManager != null) {
                this.mLocationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
            }
        } finally {
            Binder.restoreCallingIdentity(bid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterListener(LocationListener listener) {
        long bid = Binder.clearCallingIdentity();
        try {
            if (this.mLocationManager != null) {
                this.mLocationManager.removeUpdates(listener);
            }
        } finally {
            Binder.restoreCallingIdentity(bid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        RlogEx.i(LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        RlogEx.e(LOG_TAG, s);
    }
}
