package com.huawei.opcollect.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import com.huawei.opcollect.collector.servicecollection.LocationRecordAction;
import com.huawei.opcollect.utils.LocationChange;
import com.huawei.opcollect.utils.OPCollectLog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class SystemLocation {
    /* access modifiers changed from: private */
    public static String TAG = "SystemLocation";
    private static final long TWENTY_SECOND = 20000;
    private static final long TWO_MINUTES = 120000;
    private static SystemLocation mInstance = null;
    /* access modifiers changed from: private */
    public boolean isLocating = false;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private boolean mEnable = false;
    /* access modifiers changed from: private */
    public Location mLocation = null;
    /* access modifiers changed from: private */
    public LocationChange mLocationChangeListener;
    /* access modifiers changed from: private */
    public LocationManager mLocationManager = null;
    /* access modifiers changed from: private */
    public LocationListener mNetListener = null;
    private LocationListener mPassiveListener = null;
    /* access modifiers changed from: private */
    public Timer mTimer;
    /* access modifiers changed from: private */
    public boolean network_enabled = false;

    private static class GetLastLocationTask extends TimerTask {
        private WeakReference<SystemLocation> service;

        GetLastLocationTask(SystemLocation service2) {
            this.service = new WeakReference<>(service2);
        }

        public void run() {
            SystemLocation action = (SystemLocation) this.service.get();
            if (action != null && action.mLocationManager != null) {
                if (action.mNetListener != null) {
                    action.mLocationManager.removeUpdates(action.mNetListener);
                }
                LocationListener unused = action.mNetListener = null;
                boolean unused2 = action.isLocating = false;
                Location net_loc = null;
                if (action.network_enabled) {
                    try {
                        net_loc = action.mLocationManager.getLastKnownLocation("network");
                    } catch (SecurityException e) {
                        OPCollectLog.e(SystemLocation.TAG, "getLastKnownLocation security failed: " + e.getMessage());
                    } catch (IllegalArgumentException e2) {
                        OPCollectLog.e(SystemLocation.TAG, "getLastKnownLocation failed: " + e2.getMessage());
                    }
                }
                if (net_loc != null) {
                    Location unused3 = action.mLocation = net_loc;
                    if (action.mLocationChangeListener != null) {
                        action.mLocationChangeListener.onGetLocation(net_loc);
                        return;
                    }
                    return;
                }
                Location unused4 = action.mLocation = null;
                if (action.mLocationChangeListener != null) {
                    action.mLocationChangeListener.onGetLocation(null);
                }
            }
        }
    }

    private static class NetLocationListener implements LocationListener {
        private WeakReference<SystemLocation> service;

        NetLocationListener(SystemLocation service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onLocationChanged(Location location) {
            OPCollectLog.r(SystemLocation.TAG, "locationListenerNetwork onLocationChanged");
            SystemLocation action = (SystemLocation) this.service.get();
            if (action != null) {
                if (action.mTimer != null) {
                    action.mTimer.cancel();
                }
                if (action.mLocationManager != null) {
                    action.mLocationManager.removeUpdates(this);
                }
                Location unused = action.mLocation = location;
                boolean unused2 = action.isLocating = false;
                if (action.mLocationChangeListener != null) {
                    action.mLocationChangeListener.onGetLocation(location);
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private static class PassiveLocationListener implements LocationListener {
        private WeakReference<SystemLocation> service;

        PassiveLocationListener(SystemLocation service2) {
            this.service = new WeakReference<>(service2);
        }

        public void onLocationChanged(Location location) {
            if (location != null) {
                SystemLocation action = (SystemLocation) this.service.get();
                if (action != null) {
                    Location unused = action.mLocation = location;
                    boolean unused2 = action.isLocating = false;
                    HwLocation hwLocation = new HwLocation();
                    hwLocation.setAccuracy((int) location.getAccuracy());
                    hwLocation.setLatitude(location.getLatitude());
                    hwLocation.setLongitude(location.getLongitude());
                    hwLocation.setAltitude(location.getAltitude());
                    hwLocation.setProvider(location.getProvider());
                    LocationRecordAction locationRecordAction = LocationRecordAction.getInstance(action.mContext);
                    synchronized (locationRecordAction.getLock()) {
                        Handler locationHandler = locationRecordAction.getLocationHandler();
                        if (locationHandler != null) {
                            locationHandler.obtainMessage(2, hwLocation).sendToTarget();
                        }
                    }
                }
            }
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        public void onProviderEnabled(String s) {
        }

        public void onProviderDisabled(String s) {
        }
    }

    private SystemLocation(Context context) {
        OPCollectLog.r(TAG, "create");
        this.mContext = context;
    }

    private void registerPassiveListener() {
        this.mPassiveListener = new PassiveLocationListener(this);
        if (this.mLocationManager == null) {
            OPCollectLog.e(TAG, "registerPassiveListener mLocationManager is null.");
            return;
        }
        try {
            this.mLocationManager.requestLocationUpdates("passive", 500, 0.0f, this.mPassiveListener);
        } catch (SecurityException e) {
            OPCollectLog.e(TAG, "registerPassiveListener security failed: " + e.getMessage());
        } catch (IllegalArgumentException e2) {
            OPCollectLog.e(TAG, "registerPassiveListener failed: " + e2.getMessage());
        }
    }

    public static synchronized SystemLocation getInstance(Context context) {
        SystemLocation systemLocation;
        synchronized (SystemLocation.class) {
            if (mInstance == null) {
                mInstance = new SystemLocation(context);
            }
            systemLocation = mInstance;
        }
        return systemLocation;
    }

    public boolean isLocating() {
        return this.isLocating;
    }

    public Location getLocation() {
        return this.mLocation;
    }

    public void enable() {
        OPCollectLog.r(TAG, "enable");
        if (!this.mEnable) {
            OPCollectLog.r(TAG, "register passive listener");
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            registerPassiveListener();
            this.mEnable = true;
        }
    }

    public void disable() {
        OPCollectLog.r(TAG, "disable");
        if (!(this.mPassiveListener == null || this.mLocationManager == null)) {
            this.mLocationManager.removeUpdates(this.mPassiveListener);
            this.mPassiveListener = null;
        }
        this.mEnable = false;
        this.mLocation = null;
        this.mLocationManager = null;
        this.mNetListener = null;
        this.mLocationChangeListener = null;
    }

    public void destroy() {
        OPCollectLog.r(TAG, "destroy");
        destroyInstance();
    }

    private static synchronized void destroyInstance() {
        synchronized (SystemLocation.class) {
            mInstance = null;
        }
    }

    public void getCurrentLocation(LocationChange lc) {
        boolean isAirPlanModeEnable = true;
        if (lc != null && this.mContext != null) {
            this.mLocationChangeListener = lc;
            if (this.mLocation == null || !isFreshLocation(this.mLocation)) {
                if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
                    isAirPlanModeEnable = false;
                }
                if (isAirPlanModeEnable) {
                    this.mLocationChangeListener.onGetLocation(null);
                } else if (this.mLocationManager == null) {
                    OPCollectLog.e(TAG, "mLocationManager is null.");
                } else {
                    try {
                        this.network_enabled = this.mLocationManager.isProviderEnabled("network");
                    } catch (Exception e) {
                        OPCollectLog.r(TAG, "provider is not permitted");
                    }
                    if (!this.network_enabled) {
                        this.mLocationChangeListener.onGetLocation(null);
                        return;
                    }
                    try {
                        Location location = this.mLocationManager.getLastKnownLocation("network");
                        if (location == null) {
                            this.mNetListener = new NetLocationListener(this);
                            this.mLocationManager.requestLocationUpdates("network", 0, 0.0f, this.mNetListener);
                            this.isLocating = true;
                            this.mTimer = new Timer();
                            this.mTimer.schedule(new GetLastLocationTask(this), TWO_MINUTES);
                        } else if (isFreshLocation(location)) {
                            this.mLocation = location;
                            this.mLocationChangeListener.onGetLocation(this.mLocation);
                        } else {
                            this.mNetListener = new NetLocationListener(this);
                            this.mLocationManager.requestLocationUpdates("network", 0, 0.0f, this.mNetListener);
                            this.isLocating = true;
                            this.mTimer = new Timer();
                            this.mTimer.schedule(new GetLastLocationTask(this), TWO_MINUTES);
                        }
                    } catch (SecurityException e2) {
                        OPCollectLog.e(TAG, "getCurrentLocation security failed: " + e2.getMessage());
                    } catch (IllegalArgumentException e3) {
                        OPCollectLog.e(TAG, "getCurrentLocation failed: " + e3.getMessage());
                    }
                }
            } else {
                lc.onGetLocation(this.mLocation);
            }
        }
    }

    private boolean isFreshLocation(Location location) {
        if (System.currentTimeMillis() - location.getTime() < TWENTY_SECOND) {
            return true;
        }
        return false;
    }

    public void dump(int indentNum, PrintWriter pw) {
    }
}
