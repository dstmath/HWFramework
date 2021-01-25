package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geofence;
import android.location.ILocationListener;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Message;
import android.util.ArraySet;
import com.android.server.LocationManagerService;
import com.android.server.location.GnssLocationProvider;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.PrintWriter;
import java.util.List;

public abstract class AbsLocationManagerService extends ILocationManager.Stub {
    public static final String DEL_PKG = "pkg";
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;
    private static final String TAG = "LocationManagerService";
    protected String mComboNlpPackageName;
    protected String mComboNlpReadyMarker;
    protected String mComboNlpScreenMarker;
    protected boolean mGeoFencerEnabled;
    protected String mGeoFencerPackageName;

    /* access modifiers changed from: protected */
    public void enableLocalLocationProviders(GnssLocationProvider gnssProvider) {
    }

    /* access modifiers changed from: protected */
    public void updateLocalLocationDB(Location location, String provider) {
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

    /* access modifiers changed from: protected */
    public Location screenLocationLocked(Location location, String provider) {
        return location;
    }

    /* access modifiers changed from: protected */
    public void setGeoFencerEnabled(boolean enabled) {
    }

    /* access modifiers changed from: protected */
    public void dumpGeoFencer(PrintWriter pw) {
    }

    public boolean proxyGps(String pkg, int uid, boolean proxy) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isFreeze(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void dumpGpsFreezeProxy(PrintWriter pw) {
    }

    /* access modifiers changed from: protected */
    public boolean isGPSDisabled() {
        return false;
    }

    public void refreshPackageWhitelist(int type, List<String> list) {
    }

    /* access modifiers changed from: protected */
    public ArraySet<String> getPackageWhiteList(int type) {
        return new ArraySet<>();
    }

    /* access modifiers changed from: protected */
    public String getLocationProvider(int uid, LocationRequest request, String packageName, String provider) {
        return provider;
    }

    /* access modifiers changed from: protected */
    public boolean hwLocationHandleMessage(Message msg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void hwSendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
    }

    public void hwRequestLocationUpdatesLocked(LocationRequest request, LocationManagerService.Receiver receiver, int pid, int uid, String packageName) {
    }

    public void hwRemoveUpdatesLocked(LocationManagerService.Receiver receiver) {
    }

    public void initializeLockedEx() {
    }

    public void notifyMockPosition(String name, String opPackageName) {
    }

    public boolean isRequestValid(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        return true;
    }

    public boolean isRemoveValid(ILocationListener listener, PendingIntent intent, String packageName) {
        return true;
    }

    public boolean isGetLastLocationValid(String packageName) {
        return true;
    }

    public Location hwGetLastLocation(LocationRequest request) {
        return new Location("fused");
    }
}
