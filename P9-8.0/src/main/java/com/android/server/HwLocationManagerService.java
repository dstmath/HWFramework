package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.location.GeoFenceParams;
import android.location.Geofence;
import android.location.Location;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.HwNetworkPolicyManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService.Receiver;
import com.android.server.LocationManagerService.UpdateRecord;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GpsFreezeProc;
import com.android.server.location.HwGeoFencerBase;
import com.android.server.location.HwGeoFencerProxy;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.location.HwLocalLocationManager;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.location.LocationProviderProxy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class HwLocationManagerService extends LocationManagerService {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = ".HwLBSService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.lbs";
    private static final int CHECK_HIGH_POWER_REQUEST_INTERVAL = 600000;
    static final int CODE_ADD_LOCATION_MODE = 1004;
    static final int CODE_GET_POWR_TYPE = 1001;
    static final int CODE_LOG_EVENT = 1002;
    static final int CODE_REMOVE_LOCATION_MODE = 1005;
    public static final boolean D = Log.isLoggable(TAG, 3);
    protected static final int DEFAULT_MODE = 0;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final String GNSS_LOCATION_FIX_STATUS = "GNSS_LOCATION_FIX_STATUS";
    private static final int INVALID_MODE_CODE = -1;
    protected static final int MODE_BATCHING = 2;
    protected static final int MODE_FREEZE = 5;
    protected static final int MODE_GPS = 1;
    protected static final int MODE_NETWORK = 3;
    protected static final int MODE_PASSIVE = 4;
    private static final int MSG_CHECK_HIGH_POWER_REQUEST = 28;
    private static final int MSG_DELAY_START_LBS_SERVICE = 26;
    private static final int MSG_LOCATION_FIX_TIMEOUT = 25;
    private static final int MSG_LOCATION_FIX_TIMEOUT_DELAY = 10000;
    private static final int MSG_LOCTION_HUAWEI_BEGIN = 20;
    private static final int MSG_SCREEN_OFF = 23;
    private static final int MSG_SCREEN_ON = 24;
    private static final int MSG_WIFI_ALWAYS_SCAN_REOPEN = 22;
    private static final int MSG_WIFI_ALWAYS_SCAN_RESET = 21;
    private static final int NETWORK_LOCATION_MIN_INTERVAL_BY_5G = 40000;
    private static final int OPERATION_SUCCESS = 0;
    private static final int OTHER_EXCEPTION = -2;
    private static final int SERVICE_RESTART_COUNT = 3;
    private static final long SERVICE_RESTART_TIME_INTERVAL = 60000;
    private static final long SERVICE_RUN_TIME_INTERVAL = 600000;
    private static final String TAG = "HwLocationManagerService";
    private static final int WIFI_ALWAYS_SCAN_REOPEN_DELAY = 30000;
    private static final int WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY = 5000;
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG = "wifi_scan_always_available_reset_flag";
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR = "wifi_scan_always_available_user_selection";
    private boolean isAlwaysScanReset = false;
    private boolean isAlwaysScanScreenOff = false;
    private IBinder mBinderLBSService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(HwLocationManagerService.TAG, "receive broadcast intent, action: " + action);
            if ("android.location.GPS_ENABLED_CHANGE".equals(action)) {
                if (intent.hasExtra("enabled")) {
                    HwLocationManagerService.this.navigating = intent.getBooleanExtra("enabled", false);
                    Log.d(HwLocationManagerService.TAG, "EXTRA_GPS_ENABLED navigating=" + HwLocationManagerService.this.navigating);
                    HwLocationManagerService.this.refreshSystemUIStatus(HwLocationManagerService.this.navigating);
                    if (!HwLocationManagerService.this.navigating) {
                        HwLocationManagerService.this.sendHwLocationMessage(25, 0);
                    }
                    HwLocationManagerService.this.sendHwLocationMessage(28, 600000);
                }
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                HwLocationManagerService.this.handleWifiStateChanged(intent.getIntExtra("wifi_state", 4));
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwLocationManagerService.this.sendHwLocationMessage(24, HwLocationManagerService.WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwLocationManagerService.this.sendHwLocationMessage(23, HwLocationManagerService.WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY);
            }
        }
    };
    private HwGeoFencerBase mGeoFencer;
    private final Context mHwContext;
    private HwGpsPowerTracker mHwGpsPowerTracker;
    private boolean mIs5GHzBandSupported = false;
    private boolean mLBSServiceStart = false;
    private LBSServiceDeathHandler mLBSServicedeathHandler;
    private Handler mLbsServiceHandler;
    private HwLocalLocationProvider mLocalLocationProvider;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;
    private long mServiceRestartCount;
    private long mStartServiceTime;
    HashMap<String, String> mSupervisoryPkgList = new HashMap();
    private boolean navigating = false;

    private class LBSServiceDeathHandler implements ServiceConnection, DeathRecipient {
        /* synthetic */ LBSServiceDeathHandler(HwLocationManagerService this$0, LBSServiceDeathHandler -this1) {
            this();
        }

        private LBSServiceDeathHandler() {
        }

        public void onServiceConnected(ComponentName arg0, IBinder service) {
            Log.d(HwLocationManagerService.TAG, "bindLbsService Connect lbs service successful");
            HwLocationManagerService.this.mBinderLBSService = service;
            HwLocationManagerService.this.mLBSServiceStart = true;
            HwLocationManagerService.this.notifyServiceDied();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(HwLocationManagerService.TAG, "bindLbsService disconnect lbs service");
            HwLocationManagerService.this.mBinderLBSService = null;
            HwLocationManagerService.this.mLBSServiceStart = false;
        }

        public void binderDied() {
            Log.d(HwLocationManagerService.TAG, "bindLbsService lbs service has died!");
            if (HwLocationManagerService.this.mBinderLBSService != null) {
                HwLocationManagerService.this.mBinderLBSService.unlinkToDeath(HwLocationManagerService.this.mLBSServicedeathHandler, 0);
                HwLocationManagerService.this.mBinderLBSService = null;
            }
            if (System.currentTimeMillis() - HwLocationManagerService.this.mStartServiceTime > 600000) {
                HwLocationManagerService.this.mServiceRestartCount = 0;
            }
            HwLocationManagerService.this.sendLbsServiceRestartMessage();
        }
    }

    public HwLocationManagerService(Context context) {
        super(context);
        this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault(this, context);
        this.mHwContext = context;
        this.mGeoFencerEnabled = false;
        this.mIs5GHzBandSupported = isDualBandSupported();
    }

    private void refreshSystemUIStatus(boolean isRefreshMonitor) {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            for (Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
                receiver.updateMonitoring(isRefreshMonitor);
            }
        }
    }

    private void checkWifiScanAlwaysResetFlag() {
        if (Global.getInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 0) == 1) {
            Log.d(TAG, " the phone is boot before the wlan alwas scan reset to open.");
            sendHwLocationMessage(22, 0);
        }
    }

    private void sendHwLocationMessage(int what, int delay) {
        Handler locationHandler = this.mLocationManagerServiceUtil.getLocationHandler();
        locationHandler.removeMessages(what);
        Message m = Message.obtain(locationHandler, what);
        if (delay > 0) {
            locationHandler.sendMessageDelayed(m, (long) delay);
        } else {
            locationHandler.sendMessage(m);
        }
    }

    private void handleWifiStateChanged(int state) {
        Log.d(TAG, "wifistate :" + state);
        switch (state) {
            case 1:
                int always_wifi_scan = Global.getInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                int userAction = Global.getInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR, 0);
                if (this.isAlwaysScanReset && always_wifi_scan == 1 && userAction != 1) {
                    sendHwLocationMessage(21, 0);
                }
                this.isAlwaysScanReset = false;
                return;
            case 3:
                this.isAlwaysScanReset = true;
                return;
            default:
                return;
        }
    }

    protected boolean hwLocationHandleMessage(Message msg) {
        Log.d(TAG, "hwLocationHandleMessage :" + msg.what + ", sceenoff:" + this.isAlwaysScanScreenOff);
        switch (msg.what) {
            case 21:
                Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 1);
                sendHwLocationMessage(22, 30000);
                break;
            case 22:
                Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 1);
                Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 0);
                break;
            case 23:
                int always_wifi_scan = Global.getInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                int userAction = Global.getInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR, 0);
                if (always_wifi_scan == 1 && userAction != 1) {
                    this.isAlwaysScanScreenOff = true;
                    Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                    Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 1);
                    break;
                }
            case 24:
                if (this.isAlwaysScanScreenOff) {
                    this.isAlwaysScanScreenOff = false;
                    Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 1);
                    Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 0);
                    break;
                }
                break;
            case 25:
                if (Global.getInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) == 1) {
                    Log.d(TAG, "set gnss_location_fix_status to 0");
                    Global.putInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0);
                    break;
                }
                break;
            case 26:
                Log.d(TAG, "bindLbsService start service message has come.");
                startLBSService();
                bindLBSService();
                break;
            case 28:
                checkHighPowerRequest();
                if (this.navigating) {
                    sendHwLocationMessage(28, 600000);
                    break;
                }
                break;
            default:
                return false;
        }
        return true;
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
        if (this.mLocalLocationProvider == null || (this.mLocalLocationProvider.isValidLocation(location) ^ 1) == 0) {
            if (!(provider.equals("passive") || (provider.equals(HwLocalLocationManager.LOCAL_PROVIDER) ^ 1) == 0 || this.mLocalLocationProvider == null || !this.mLocalLocationProvider.isEnabled())) {
                this.mLocalLocationProvider.updataLocationDB(location);
            }
            return;
        }
        Log.d(TAG, "incoming location is invdlid,and not report app");
    }

    public void initHwLocationPowerTracker(Context context) {
        this.mHwGpsPowerTracker = new HwGpsPowerTracker(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.GPS_ENABLED_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mHwContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, this.mLocationManagerServiceUtil.getLocationHandler());
        this.mLBSServicedeathHandler = new LBSServiceDeathHandler(this, null);
        this.mLbsServiceHandler = this.mLocationManagerServiceUtil.getLocationHandler();
        checkWifiScanAlwaysResetFlag();
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
        if ("hi110x".equalsIgnoreCase(SystemProperties.get("ro.connectivity.chiptype", ""))) {
            this.mLocationManagerServiceUtil.getContext().getContentResolver().registerContentObserver(Global.getUriFor("quick_gps_switch"), true, new ContentObserver(this.mLocationManagerServiceUtil.getLocationHandler()) {
                public void onChange(boolean selfChange) {
                    int quickGpsSettings = Global.getInt(HwLocationManagerService.this.mLocationManagerServiceUtil.getContext().getContentResolver(), "quick_gps_switch", 1);
                    SystemProperties.set("persist.sys.pgps.config", Integer.toString(quickGpsSettings));
                    Log.d(HwLocationManagerService.TAG, "Settings.Global.QUICK_GPS_SWITCH  set " + quickGpsSettings);
                }
            }, -1);
        }
    }

    public void hwSendLocationChangedAction(Context context, String packageName) {
        Intent intent = new Intent("android.location.LOCATION_REQUEST_CHANGE_ACTION");
        intent.putExtra("package", packageName);
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void checkGeoFencerEnabled(PackageManager packageManager) {
        this.mGeoFencerPackageName = Resources.getSystem().getString(33685939);
        if (this.mGeoFencerPackageName == null || packageManager.resolveService(new Intent(this.mGeoFencerPackageName), 0) == null) {
            this.mGeoFencer = null;
            this.mGeoFencerEnabled = false;
        } else {
            this.mGeoFencer = HwGeoFencerProxy.getGeoFencerProxy(this.mLocationManagerServiceUtil.getContext(), this.mGeoFencerPackageName);
            this.mGeoFencerEnabled = true;
        }
        this.mComboNlpPackageName = Resources.getSystem().getString(33685940);
        if (this.mComboNlpPackageName != null) {
            this.mComboNlpReadyMarker = this.mComboNlpPackageName + ".nlp:ready";
            this.mComboNlpScreenMarker = this.mComboNlpPackageName + ".nlp:screen";
        }
    }

    public boolean addQcmGeoFencer(Geofence geofence, LocationRequest sanitizedRequest, int uid, PendingIntent intent, String packageName) {
        if (this.mGeoFencer == null || !this.mGeoFencerEnabled) {
            return false;
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
            return false;
        }
        this.mGeoFencer.remove(intent);
        return true;
    }

    private void setGnssLocationFixStatus(Location location, String provider) {
        int location_fix_status = Global.getInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0);
        Bundle extras = location.getExtras();
        if ((extras == null || !extras.getBoolean("QUICKGPS")) && "gps".equals(provider)) {
            if (location_fix_status == 0) {
                Log.d(TAG, "set gnss_location_fix_status to 1");
                Global.putInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 1);
            }
            sendHwLocationMessage(25, 10000);
        }
    }

    protected Location screenLocationLocked(Location location, String provider) {
        setGnssLocationFixStatus(location, provider);
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
            if (!(pNames[0].equals(this.mComboNlpPackageName) || (pNames[1].equals(this.mComboNlpPackageName) ^ 1) == 0)) {
                return location;
            }
        } else if (!connectedNlpPackage.equals(this.mComboNlpPackageName)) {
            return location;
        }
        Bundle extras = location.getExtras();
        boolean isBeingScreened = false;
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
                    if (r.mReceiver.mIdentity.mPackageName.equals(this.mComboNlpPackageName)) {
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
            this.mGeoFencer.dump(pw, "");
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

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        GpsFreezeProc.getInstance().refreshPackageWhitelist(type, pkgList);
    }

    protected ArraySet<String> getPackageWhiteList(int type) {
        return GpsFreezeProc.getInstance().getPackageWhiteList(type);
    }

    private int addSupervisoryControlProc(String packagename, int uid, int expectedmode) {
        String mExpectedMode = getMode(expectedmode);
        if (mExpectedMode == null) {
            return -1;
        }
        if (uid <= 1000) {
            Slog.i(TAG, "addSupervisoryControlProc, uid less than 1000, invalid! uid is " + uid);
            return -2;
        } else if (GpsFreezeProc.getInstance().isInPackageWhiteListByType(5, packagename)) {
            Slog.i(TAG, "addSupervisoryControlProc, packagename:" + packagename + " is in PackageWhiteList break");
            return -2;
        } else {
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                this.mSupervisoryPkgList.put(packagename, mExpectedMode);
            }
            if (isNetworkAvailable(uid) || !mExpectedMode.equals("network")) {
                Slog.d(TAG, "addSupervisoryControlProc, mExpectedMode is " + mExpectedMode + ",packagename=" + packagename + ", mSupervisoryPkgList is " + this.mSupervisoryPkgList.toString());
                synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                    HashMap<UpdateRecord, String> mSwitchRecords = new HashMap();
                    HashMap<Object, Receiver> mReceivers = this.mLocationManagerServiceUtil.getReceivers();
                    if (mReceivers != null) {
                        for (Receiver receiver : mReceivers.values()) {
                            String currentMode = "";
                            if (receiver.mIdentity.mPackageName.equals(packagename)) {
                                for (UpdateRecord record : receiver.mUpdateRecords.values()) {
                                    currentMode = record.mProvider;
                                    String mode = getSwitchMode(uid, record.mRealRequest.getProvider(), currentMode, mExpectedMode);
                                    if (!mode.equals(currentMode)) {
                                        mSwitchRecords.put(record, mode);
                                        Slog.d(TAG, "addSupervisoryControlProc, originalMode: " + record.mRealRequest.getProvider() + ", currentMode: " + currentMode + ", mExpectedMode: " + mExpectedMode + ", receiver: " + Integer.toHexString(System.identityHashCode(receiver)));
                                    }
                                }
                            }
                        }
                    }
                    updateLocationMode(mSwitchRecords);
                }
                return 0;
            }
            Slog.i(TAG, "addSupervisoryControlProc, packagename=" + packagename + "expectedmode is network, but Network is not Available");
            return -2;
        }
    }

    private void removeSupervisoryControlProc(String packagename, int uid) {
        if (uid > 1000 || uid == 0) {
            HashMap<UpdateRecord, String> mSwitchRecords = new HashMap();
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                if (packagename == null && uid == 0) {
                    Slog.d(TAG, "remove All SupervisoryControlProc " + this.mSupervisoryPkgList.toString());
                    for (Entry<String, String> entry : this.mSupervisoryPkgList.entrySet()) {
                        mSwitchRecords.putAll(getRemoveSwitchRecords((String) entry.getKey()));
                    }
                    this.mSupervisoryPkgList.clear();
                } else if (packagename != null) {
                    Slog.d(TAG, "remove SupervisoryControlProc packagename=" + packagename);
                    mSwitchRecords.putAll(getRemoveSwitchRecords(packagename));
                    this.mSupervisoryPkgList.remove(packagename);
                }
                updateLocationMode(mSwitchRecords);
            }
            return;
        }
        Slog.i(TAG, "removeSupervisoryControlProc, uid less than 1000, invalid! uid is " + uid);
    }

    private HashMap<UpdateRecord, String> getRemoveSwitchRecords(String packagename) {
        HashMap<Object, Receiver> mReceivers = this.mLocationManagerServiceUtil.getReceivers();
        HashMap<UpdateRecord, String> mSwitchRecords = new HashMap();
        if (mReceivers != null) {
            for (Receiver receiver : mReceivers.values()) {
                if (receiver.mIdentity.mPackageName.equals(packagename)) {
                    for (UpdateRecord record : receiver.mUpdateRecords.values()) {
                        if (!record.mProvider.equals(record.mRealRequest.getProvider())) {
                            mSwitchRecords.put(record, record.mRealRequest.getProvider());
                            Slog.d(TAG, "getRemoveSwitchRecords, packagename:" + packagename + " originalMode: " + record.mRealRequest.getProvider() + ", currentMode: " + record.mProvider + ", receiver: " + Integer.toHexString(System.identityHashCode(receiver)));
                        }
                    }
                }
            }
        }
        return mSwitchRecords;
    }

    private void updateLocationMode(HashMap<UpdateRecord, String> mSwitchRecords) {
        if (mSwitchRecords.size() == 0) {
            Slog.d(TAG, " package has no request send yet");
            return;
        }
        HashMap<String, ArrayList<UpdateRecord>> recordsByProvider = this.mLocationManagerServiceUtil.getRecordsByProvider();
        ArrayList<String> updateproviders = new ArrayList();
        for (Entry<UpdateRecord, String> entry : mSwitchRecords.entrySet()) {
            String target = (String) entry.getValue();
            String current = ((UpdateRecord) entry.getKey()).mProvider;
            ((UpdateRecord) entry.getKey()).mProvider = target;
            ((ArrayList) recordsByProvider.get(current)).remove(entry.getKey());
            if (((ArrayList) recordsByProvider.get(target)) == null) {
                recordsByProvider.put(target, new ArrayList());
            }
            ((ArrayList) recordsByProvider.get(target)).add((UpdateRecord) entry.getKey());
            ((UpdateRecord) entry.getKey()).mReceiver.mUpdateRecords.remove(current, entry.getKey());
            ((UpdateRecord) entry.getKey()).mReceiver.mUpdateRecords.put(target, (UpdateRecord) entry.getKey());
            if (!updateproviders.contains(current)) {
                updateproviders.add(current);
            }
            if (!updateproviders.contains(target)) {
                updateproviders.add(target);
            }
            Slog.d(TAG, "updateLocationMode receive : " + ((UpdateRecord) entry.getKey()).mReceiver.toString() + " " + current + " -> " + target);
        }
        Handler mHandler = this.mLocationManagerServiceUtil.getLocationHandler();
        mHandler.sendMessage(Message.obtain(mHandler, 6, updateproviders));
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getSwitchMode(int uid, String originalMode, String currentMode, String mExpectedMode) {
        String targetmode = null;
        if (originalMode == null || mExpectedMode == null || currentMode == null || mExpectedMode.equals(currentMode) || "passive".equals(originalMode)) {
            return currentMode;
        }
        if ("gps".equals(mExpectedMode)) {
            targetmode = originalMode;
        }
        if ("network".equals(mExpectedMode) && "gps".equals(currentMode)) {
            targetmode = "network";
        }
        if ("passive".equals(mExpectedMode)) {
            targetmode = "passive";
        }
        if (targetmode == null || !isLocationModeAvailable(targetmode, uid)) {
            return currentMode;
        }
        return targetmode;
    }

    private String getMode(int mode) {
        switch (mode) {
            case 0:
            case 2:
            case 5:
                break;
            case 1:
                return "gps";
            case 3:
                return "network";
            case 4:
                return "passive";
            default:
                Slog.e(TAG, "add unknow LocationMode, error! expectedmode is " + mode);
                break;
        }
        return null;
    }

    private boolean isLocationModeAvailable(String locationMode, int uid) {
        if (locationMode == null) {
            return false;
        }
        if ("network".equals(locationMode)) {
            if (isProviderEnabled("network")) {
                return true;
            }
            Slog.i(TAG, "network setting is unavailable.");
            return false;
        } else if (!"gps".equals(locationMode)) {
            return false;
        } else {
            if (isProviderEnabled("gps")) {
                return true;
            }
            Slog.i(TAG, "gps setting is unavailable.");
            return false;
        }
    }

    private boolean isNetworkAvailable(int uid) {
        long identity;
        try {
            if (this.mHwContext == null) {
                Slog.e(TAG, "mHwContext is null error!");
                return false;
            }
            ConnectivityManager connectivity = (ConnectivityManager) this.mHwContext.getSystemService("connectivity");
            if (connectivity == null) {
                Slog.e(TAG, "connectivityManager is null error!");
                return false;
            }
            NetworkInfo infoWifi = connectivity.getNetworkInfo(1);
            NetworkInfo infoMoblie = connectivity.getNetworkInfo(0);
            if (infoWifi == null || infoMoblie == null) {
                Slog.e(TAG, "infoWifi or infoMoblie is null error!");
                return false;
            }
            boolean isWifiConn = infoWifi.isConnected();
            boolean isMobileConn = infoMoblie.isConnected();
            Slog.d(TAG, "uid is " + uid + " , isWifiConn is " + isWifiConn + " , isMobileConn is " + isMobileConn);
            HwNetworkPolicyManager manager = HwNetworkPolicyManager.from(this.mHwContext);
            if (manager == null) {
                Slog.e(TAG, "HwNetworkPolicyManager is null error!");
                return false;
            }
            identity = Binder.clearCallingIdentity();
            int policy = manager.getHwUidPolicy(uid);
            Binder.restoreCallingIdentity(identity);
            boolean wifiAccess = (policy & 2) == 0;
            boolean mobileAccess = (policy & 1) == 0;
            Slog.d(TAG, "policy is " + policy + " , wifiAccess is " + wifiAccess + ", mobileAccess is " + mobileAccess);
            boolean wifiAvaiable = wifiAccess ? isWifiConn : false;
            boolean mobileAvailable = mobileAccess ? isMobileConn : false;
            Slog.d(TAG, "wifiAvaiable is " + wifiAvaiable + " , mobileAvailable is " + mobileAvailable);
            if (wifiAvaiable || mobileAvailable) {
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            Slog.e(TAG, "RuntimeException error!" + e.toString());
            return false;
        } catch (Exception e2) {
            Slog.e(TAG, "Exception error!");
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean enforceAccessPermission(int pid, int uid) {
        if (this.mHwContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0 || this.mHwContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0) {
            return true;
        }
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int _result;
        String packagename;
        int uid;
        switch (code) {
            case 1001:
                data.enforceInterface(DESCRIPTOR);
                _result = getPowerTypeByPackageName(data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1002:
                data.enforceInterface(DESCRIPTOR);
                _result = logEvent(data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1004:
                data.enforceInterface(DESCRIPTOR);
                packagename = data.readString();
                uid = data.readInt();
                int expectedmode = data.readInt();
                Slog.d(TAG, "on transact ADD_LOCATION_MODE  uid is " + uid + " , expectedmode is " + expectedmode + " , packagename is " + packagename);
                _result = addSupervisoryControlProc(packagename, uid, expectedmode);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1005:
                data.enforceInterface(DESCRIPTOR);
                packagename = data.readString();
                uid = data.readInt();
                Slog.d(TAG, "on transact REMOVE_LOCATION_MODE uid is " + uid + " , packagename is " + packagename);
                removeSupervisoryControlProc(packagename, uid);
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public static int qualityToType(int quality) {
        switch (quality) {
            case 100:
            case 203:
                return 2;
            case 102:
            case 104:
            case 201:
                return 1;
            case 200:
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
                if (packageName.equals(receiver.mIdentity.mPackageName)) {
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

    public int logEvent(int type, int event, String parameter) {
        if (enforceAccessPermission(Binder.getCallingPid(), Binder.getCallingUid()) && !TextUtils.isEmpty(parameter)) {
            return HwGpsLogServices.getInstance(this.mHwContext).logEvent(type, event, parameter);
        }
        return -1;
    }

    protected boolean isGPSDisabled() {
        String allowedProviders = Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", ActivityManager.getCurrentUser());
        if (!HwDeviceManager.disallowOp(13)) {
            return false;
        }
        if (allowedProviders.contains("gps")) {
            Slog.i(TAG, "gps provider cannot be enabled, set it to false .");
            Secure.setLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", false, ActivityManager.getCurrentUser());
        }
        return true;
    }

    protected String getLocationProvider(int uid, LocationRequest request, String packageName, String provider) {
        String targetprovider = provider;
        if ("gps".equals(provider) && GpsFreezeProc.getInstance().isInPackageWhiteListByType(5, packageName)) {
            targetprovider = "network";
            Log.d(TAG, "packageName:" + packageName + " is change gps provider to " + targetprovider);
        } else if (this.mSupervisoryPkgList.containsKey(packageName)) {
            if (isNetworkAvailable(uid) || !((String) this.mSupervisoryPkgList.get(packageName)).equals("network")) {
                targetprovider = getSwitchMode(uid, provider, provider, (String) this.mSupervisoryPkgList.get(packageName));
            } else {
                Log.d(TAG, "network is not available, packageName:" + packageName + " need not change provider to network");
                targetprovider = provider;
            }
            Log.d(TAG, "packageName:" + packageName + " is change " + provider + " provider to " + targetprovider);
        }
        if ("network".equals(provider) && this.mIs5GHzBandSupported && request.getInterval() < 40000) {
            Log.d(TAG, "5G network min interval need to 40s");
            request.setInterval(40000);
        }
        Log.d(TAG, "lbsService mLBSServiceStart:" + this.mLBSServiceStart);
        if ("gps".equals(provider) && (this.mLBSServiceStart ^ 1) != 0) {
            Log.d(TAG, " LocationManager. bindLbsService start.");
            startLBSService();
            bindLBSService();
            if (this.mLbsServiceHandler.hasMessages(26)) {
                Log.d(TAG, " mLbsServiceHandler has delay message. bindLbsService start.");
                this.mLbsServiceHandler.removeMessages(26);
                this.mServiceRestartCount = 0;
            }
        }
        return targetprovider;
    }

    private void startLBSService() {
        this.mStartServiceTime = System.currentTimeMillis();
        Log.d(TAG, this.mContext.getPackageName() + " start lbs service. bindLbsService start time:" + this.mStartServiceTime);
        try {
            Intent bindIntent = new Intent();
            bindIntent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, "com.huawei.lbs.HwLBSService");
            bindIntent.addFlags(268435456);
            this.mContext.startService(bindIntent);
        } catch (Exception e) {
            Log.e(TAG, "startLBSService Exception: " + e.getMessage());
        }
    }

    private void bindLBSService() {
        Log.d(TAG, this.mContext.getPackageName() + " bind lbs service. bindLbsService");
        try {
            Intent bindIntent = new Intent();
            bindIntent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, "com.huawei.lbs.HwLBSService");
            this.mContext.bindService(bindIntent, this.mLBSServicedeathHandler, 1);
        } catch (Exception e) {
            Log.e(TAG, "bindLBSService Exception: " + e.getMessage());
        }
    }

    private void notifyServiceDied() {
        try {
            if (this.mBinderLBSService != null) {
                this.mBinderLBSService.linkToDeath(this.mLBSServicedeathHandler, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "IBinder register linkToDeath function fail.");
        }
    }

    private void sendLbsServiceRestartMessage() {
        long delayTime = 0;
        if (!this.mLbsServiceHandler.hasMessages(26)) {
            Message mTimeOut = Message.obtain(this.mLbsServiceHandler, 26);
            delayTime = (long) (Math.pow(2.0d, (double) this.mServiceRestartCount) * 60000.0d);
            if (delayTime > 0) {
                this.mLbsServiceHandler.sendMessageDelayed(mTimeOut, delayTime);
            } else {
                this.mLbsServiceHandler.sendMessage(mTimeOut);
            }
        }
        if (this.mServiceRestartCount < 3) {
            this.mServiceRestartCount++;
        }
        Log.d(TAG, "bindLbsService sendLbsServiceRestartMessage mServiceRestartCount: " + this.mServiceRestartCount + ",delayTime:" + delayTime);
    }

    private boolean isDualBandSupported() {
        return this.mContext.getResources().getBoolean(17957051);
    }

    private void checkHighPowerRequest() {
        ArrayList<UpdateRecord> gpsRecords = (ArrayList) this.mLocationManagerServiceUtil.getRecordsByProvider().get("gps");
        if (gpsRecords != null) {
            ArrayList<UpdateRecord> mRecords = (ArrayList) gpsRecords.clone();
            List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList == null) {
                Log.w(TAG, "no Process find");
                synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                    for (UpdateRecord record : mRecords) {
                        this.mLocationManagerServiceUtil.removeUpdatesLocked(record.mReceiver);
                    }
                }
                return;
            }
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                for (UpdateRecord record2 : mRecords) {
                    boolean isfound = false;
                    for (RunningAppProcessInfo appProcess : appProcessList) {
                        if (appProcess.pid == record2.mReceiver.mIdentity.mPid && appProcess.uid == record2.mReceiver.mIdentity.mUid) {
                            isfound = true;
                            break;
                        }
                    }
                    if (!isfound) {
                        this.mLocationManagerServiceUtil.removeUpdatesLocked(record2.mReceiver);
                        Log.w(TAG, "process may be died, but request not remove!  pid = " + record2.mReceiver.mIdentity.mPid + " uid = " + record2.mReceiver.mIdentity.mUid + " receiver = " + record2.mReceiver.toString());
                    }
                }
            }
        }
    }
}
