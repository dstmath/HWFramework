package com.android.server;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.location.GeoFenceParams;
import android.location.Geofence;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService.Receiver;
import com.android.server.LocationManagerService.UpdateRecord;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GpsFreezeProc;
import com.android.server.location.HwGeoFencerBase;
import com.android.server.location.HwGeoFencerProxy;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.location.HwLocalLocationManager;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwWindowManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class HwLocationManagerService extends LocationManagerService {
    static final int CODE_GET_POWR_TYPE = 1001;
    public static final boolean D;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final String TAG = "LocationManagerService";
    private HwGeoFencerBase mGeoFencer;
    private final Context mHwContext;
    private HwGpsPowerTracker mHwGpsPowerTracker;
    private HwLocalLocationProvider mLocalLocationProvider;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;

    /* renamed from: com.android.server.HwLocationManagerService.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            int quickGpsSettings = Global.getInt(HwLocationManagerService.this.mLocationManagerServiceUtil.getContext().getContentResolver(), "quick_gps_switch", 1);
            SystemProperties.set("persist.sys.pgps.config", Integer.toString(quickGpsSettings));
            Log.d(HwLocationManagerService.TAG, "Settings.Global.QUICK_GPS_SWITCH  set " + quickGpsSettings);
        }
    }

    static {
        D = Log.isLoggable(TAG, 3);
    }

    public HwLocationManagerService(Context context) {
        super(context);
        this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault(this, context);
        this.mHwContext = context;
        this.mGeoFencerEnabled = D;
    }

    protected void enableLocalLocationProviders(GnssLocationProvider gnssProvider) {
        if (gnssProvider == null || !gnssProvider.isLocalDBEnabled()) {
            Log.e(TAG, "localDB is disabled");
            return;
        }
        Log.e(TAG, "init and enable localLocationProvider ");
        this.mLocalLocationProvider = HwLocalLocationProvider.getInstance(this.mLocationManagerServiceUtil.getContext(), this);
        this.mLocalLocationProvider.enable();
        this.mLocationManagerServiceUtil.addProviderLocked(this.mLocalLocationProvider);
        this.mLocationManagerServiceUtil.getRealProviders().put(HwLocalLocationManager.LOCAL_PROVIDER, this.mLocalLocationProvider);
        this.mLocationManagerServiceUtil.getEnabledProviders().add(this.mLocalLocationProvider.getName());
    }

    protected void updateLocalLocationDB(Location location, String provider) {
        if (this.mLocalLocationProvider == null || this.mLocalLocationProvider.isValidLocation(location)) {
            if (!(provider.equals("passive") || provider.equals(HwLocalLocationManager.LOCAL_PROVIDER) || this.mLocalLocationProvider == null || !this.mLocalLocationProvider.isEnabled())) {
                this.mLocalLocationProvider.updataLocationDB(location);
            }
            return;
        }
        Log.d(TAG, "incoming location is invdlid,and not report app");
    }

    public void initHwLocationPowerTracker(Context context) {
        this.mHwGpsPowerTracker = new HwGpsPowerTracker(context);
    }

    public void hwLocationPowerTrackerRecordRequest(String pkgName, int quality, boolean isIntent) {
        this.mHwGpsPowerTracker.recordRequest(pkgName, quality, isIntent);
    }

    public void hwLocationPowerTrackerRemoveRequest(String pkgName) {
        this.mHwGpsPowerTracker.removeRequest(pkgName);
    }

    public void hwLocationPowerTrackerDump(PrintWriter pw) {
        this.mHwGpsPowerTracker.dump(pw);
    }

    public void hwQuickGpsSwitch() {
        if ("hi110x".equalsIgnoreCase(SystemProperties.get("ro.connectivity.chiptype", AppHibernateCst.INVALID_PKG))) {
            this.mLocationManagerServiceUtil.getContext().getContentResolver().registerContentObserver(Global.getUriFor("quick_gps_switch"), true, new AnonymousClass1(this.mLocationManagerServiceUtil.getLocationHandler()), -1);
        }
    }

    public void hwSendLocationChangedAction(Context context, String packageName) {
        Intent intent = new Intent("android.location.LOCATION_REQUEST_CHANGE_ACTION");
        intent.putExtra(ControlScope.PACKAGE_ELEMENT_KEY, packageName);
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void checkGeoFencerEnabled(PackageManager packageManager) {
        this.mGeoFencerPackageName = Resources.getSystem().getString(33685525);
        if (this.mGeoFencerPackageName == null || packageManager.resolveService(new Intent(this.mGeoFencerPackageName), 0) == null) {
            this.mGeoFencer = null;
            this.mGeoFencerEnabled = D;
        } else {
            this.mGeoFencer = HwGeoFencerProxy.getGeoFencerProxy(this.mLocationManagerServiceUtil.getContext(), this.mGeoFencerPackageName);
            this.mGeoFencerEnabled = true;
        }
        this.mComboNlpPackageName = Resources.getSystem().getString(33685527);
        if (this.mComboNlpPackageName != null) {
            this.mComboNlpReadyMarker = this.mComboNlpPackageName + ".nlp:ready";
            this.mComboNlpScreenMarker = this.mComboNlpPackageName + ".nlp:screen";
        }
    }

    public boolean addQcmGeoFencer(Geofence geofence, LocationRequest sanitizedRequest, int uid, PendingIntent intent, String packageName) {
        if (this.mGeoFencer == null || !this.mGeoFencerEnabled) {
            return D;
        }
        long expiration;
        if (sanitizedRequest.getExpireAt() == Long.MAX_VALUE) {
            expiration = -1;
        } else {
            expiration = sanitizedRequest.getExpireAt() - SystemClock.elapsedRealtime();
        }
        this.mGeoFencer.add(new GeoFenceParams(uid, geofence.getLatitude(), geofence.getLongitude(), geofence.getRadius(), expiration, intent, packageName));
        return true;
    }

    public boolean removeQcmGeoFencer(PendingIntent intent) {
        if (this.mGeoFencer == null || !this.mGeoFencerEnabled) {
            return D;
        }
        this.mGeoFencer.remove(intent);
        return true;
    }

    protected Location screenLocationLocked(Location location, String provider) {
        if (this.mLocationManagerServiceUtil.isMockProvider("network")) {
            return location;
        }
        LocationProviderProxy providerProxy = (LocationProviderProxy) this.mLocationManagerServiceUtil.getProvidersByName().get("network");
        if (this.mComboNlpPackageName == null || providerProxy == null || !provider.equals("network") || this.mLocationManagerServiceUtil.isMockProvider("network")) {
            return location;
        }
        String connectedNlpPackage = providerProxy.getConnectedPackageName();
        Log.d(TAG, "connectedNlpPackage " + connectedNlpPackage);
        if (connectedNlpPackage == null) {
            return location;
        }
        String[] pNames = connectedNlpPackage.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        if (pNames.length == 2) {
            if (!(pNames[0].equals(this.mComboNlpPackageName) || pNames[1].equals(this.mComboNlpPackageName))) {
                return location;
            }
        } else if (!connectedNlpPackage.equals(this.mComboNlpPackageName)) {
            return location;
        }
        Bundle extras = location.getExtras();
        boolean isBeingScreened = D;
        if (extras == null) {
            extras = new Bundle();
        }
        if (extras.containsKey(this.mComboNlpReadyMarker)) {
            if (D) {
                Log.d(TAG, "This location is marked as ready for broadcast");
            }
            extras.remove(this.mComboNlpReadyMarker);
        } else {
            ArrayList<UpdateRecord> records = (ArrayList) this.mLocationManagerServiceUtil.getRecordsByProvider().get("passive");
            if (records != null) {
                for (UpdateRecord r : records) {
                    if (r.mReceiver.mPackageName.equals(this.mComboNlpPackageName)) {
                        if (!isBeingScreened) {
                            isBeingScreened = true;
                            extras.putBoolean(this.mComboNlpScreenMarker, true);
                        }
                        if (!r.mReceiver.callLocationChangedLocked(location)) {
                            Slog.w(TAG, "RemoteException calling onLocationChanged on " + r.mReceiver);
                        } else if (D) {
                            Log.d(TAG, "Sending location for screening");
                        }
                    }
                }
            }
            if (isBeingScreened) {
                return null;
            }
            if (D) {
                Log.d(TAG, "Not screening locations");
            }
        }
        return location;
    }

    protected void setGeoFencerEnabled(boolean enabled) {
        if (this.mGeoFencer != null) {
            this.mGeoFencerEnabled = enabled;
        }
    }

    protected void dumpGeoFencer(PrintWriter pw) {
        if (this.mGeoFencer != null && this.mGeoFencerEnabled) {
            this.mGeoFencer.dump(pw, AppHibernateCst.INVALID_PKG);
        }
    }

    public boolean proxyGps(String pkg, int uid, boolean proxy) {
        if (proxy) {
            GpsFreezeProc.getInstance().addFreezeProcess(pkg, uid);
        } else {
            GpsFreezeProc.getInstance().removeFreezeProcess(pkg, uid);
        }
        return true;
    }

    protected boolean isFreeze(String pkg) {
        return GpsFreezeProc.getInstance().isFreeze(pkg);
    }

    protected void dumpGpsFreezeProxy(PrintWriter pw) {
        GpsFreezeProc.getInstance().dump(pw);
    }

    private boolean enforceAccessPermission(int pid, int uid) {
        if (this.mHwContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0 || this.mHwContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0) {
            return true;
        }
        return D;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case CODE_GET_POWR_TYPE /*1001*/:
                data.enforceInterface(DESCRIPTOR);
                int _result = getPowerTypeByPackageName(data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public static int qualityToType(int quality) {
        switch (quality) {
            case HwWindowManagerService.ROG_FREEZE_TIMEOUT /*100*/:
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                return 2;
            case WifiProCommonUtils.HISTORY_TYPE_PORTAL /*102*/:
            case IOTController.EV_CANCEL_AUTH_ALL /*104*/:
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
                return 1;
            case WifiProCommonUtils.HTTP_REACHALBE_HOME /*200*/:
                return 0;
            default:
                Slog.d(TAG, "quality( " + quality + " ) is error !");
                return -1;
        }
    }

    public int getPowerTypeByPackageName(String packageName) {
        if (!enforceAccessPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            return -1;
        }
        int power_type_ret = -1;
        if (TextUtils.isEmpty(packageName)) {
            return -1;
        }
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            for (Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
                if (packageName.equals(receiver.mPackageName)) {
                    HashMap<String, UpdateRecord> updateRecords = receiver.mUpdateRecords;
                    if (updateRecords != null) {
                        for (UpdateRecord updateRecord : updateRecords.values()) {
                            int power_type = qualityToType(updateRecord.mRequest.getQuality());
                            if (power_type > power_type_ret) {
                                power_type_ret = power_type;
                                if (power_type == 2) {
                                    return power_type;
                                }
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
            }
            return power_type_ret;
        }
    }

    protected boolean isGPSDisabled() {
        String allowedProviders = Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", ActivityManager.getCurrentUser());
        if (!HwDeviceManager.disallowOp(13)) {
            return D;
        }
        if (allowedProviders.contains("gps")) {
            Slog.i(TAG, "gps provider cannot be enabled, set it to false .");
            Secure.setLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", D, ActivityManager.getCurrentUser());
        }
        return true;
    }
}
