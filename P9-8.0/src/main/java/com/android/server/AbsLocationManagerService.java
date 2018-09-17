package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geofence;
import android.location.ILocationManager.Stub;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Message;
import android.util.ArraySet;
import com.android.server.location.GnssLocationProvider;
import java.io.PrintWriter;
import java.util.List;

public abstract class AbsLocationManagerService extends Stub {
    public static final String DEL_PKG = "pkg";
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;
    private static final String TAG = "LocationManagerService";
    protected String mComboNlpPackageName;
    protected String mComboNlpReadyMarker;
    protected String mComboNlpScreenMarker;
    protected boolean mGeoFencerEnabled;
    protected String mGeoFencerPackageName;

    protected void enableLocalLocationProviders(GnssLocationProvider gnssProvider) {
    }

    protected void updateLocalLocationDB(Location location, String provider) {
    }

    public void initHwLocationPowerTracker(Context context) {
    }

    public void hwLocationPowerTrackerRecordRequest(String pkgName, int quality, boolean isIntent) {
    }

    public void hwLocationPowerTrackerRemoveRequest(String pkgName) {
    }

    public void hwLocationPowerTrackerDump(PrintWriter pw) {
    }

    public void hwQuickGpsSwitch() {
    }

    public void hwSendLocationChangedAction(Context context, String packageName) {
    }

    public void checkGeoFencerEnabled(PackageManager packageManager) {
    }

    public boolean addQcmGeoFencer(Geofence geofence, LocationRequest sanitizedRequest, int uid, PendingIntent intent, String packageName) {
        return false;
    }

    public boolean removeQcmGeoFencer(PendingIntent intent) {
        return false;
    }

    protected Location screenLocationLocked(Location location, String provider) {
        return location;
    }

    protected void setGeoFencerEnabled(boolean enabled) {
    }

    protected void dumpGeoFencer(PrintWriter pw) {
    }

    public boolean proxyGps(String pkg, int uid, boolean proxy) {
        return false;
    }

    protected boolean isFreeze(String pkg) {
        return false;
    }

    protected void dumpGpsFreezeProxy(PrintWriter pw) {
    }

    protected boolean isGPSDisabled() {
        return false;
    }

    public void refreshPackageWhitelist(int type, List<String> list) {
    }

    protected ArraySet<String> getPackageWhiteList(int type) {
        return new ArraySet();
    }

    protected String getLocationProvider(int uid, LocationRequest request, String packageName, String provider) {
        return provider;
    }

    protected boolean hwLocationHandleMessage(Message msg) {
        return false;
    }
}
