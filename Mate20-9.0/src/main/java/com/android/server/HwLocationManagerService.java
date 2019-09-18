package com.android.server;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
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
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GpsFreezeProc;
import com.android.server.location.HwCryptoUtility;
import com.android.server.location.HwGeoFencerBase;
import com.android.server.location.HwGeoFencerProxy;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.location.HwLocalLocationManager;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hiai.awareness.AwarenessConstants;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class HwLocationManagerService extends LocationManagerService {
    private static final String ACCURACY = "accuracy";
    private static final String AIDL_MESSAGE_SERVICE_CLASS = ".HwLBSService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.lbs";
    static final int CODE_ADD_LOCATION_MODE = 1004;
    static final int CODE_GET_POWR_TYPE = 1001;
    static final int CODE_GNSS_DETECT = 1007;
    static final int CODE_LOG_EVENT = 1002;
    static final int CODE_REMOVE_LOCATION_MODE = 1005;
    public static final boolean D = Log.isLoggable(TAG, 3);
    protected static final int DEFAULT_MODE = 0;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final int DFT_ASSERT_ERROR_CODE = 1001;
    private static final int DFT_CHIP_ASSERT_EVENT = 910009014;
    private static final String GNSS_LOCATION_FIX_STATUS = "GNSS_LOCATION_FIX_STATUS";
    private static final String HW_CHECK_EXCESS_RECEIVER = "hw_check_excess_receiver";
    private static final int INVALID_MODE_CODE = -1;
    private static final String LATITUDE = "latitude";
    private static final String LOCATION_MODE_BATTERY_SAVING = Integer.toString(2);
    private static final String LOCATION_MODE_HIGH_ACCURACY = Integer.toString(3);
    private static final String LOCATION_MODE_OFF = Integer.toString(0);
    private static final String LOCATION_MODE_SENSORS_ONLY = Integer.toString(1);
    private static final String LONGITUDE = "longitude";
    private static final String MASTER_PASSWORD = HwLocalLocationManager.MASTER_PASSWORD;
    private static final int MAX_DIFF_WITH_SOURCE = 5;
    private static final int MAX_RECEIVER_PER_PKG = 200;
    protected static final int MODE_BATCHING = 2;
    protected static final int MODE_FREEZE = 5;
    protected static final int MODE_GPS = 1;
    protected static final int MODE_NETWORK = 3;
    protected static final int MODE_PASSIVE = 4;
    private static final int MSG_DELAY_START_LBS_SERVICE = 26;
    private static final int MSG_LOCATION_FIX_TIMEOUT = 25;
    private static final int MSG_LOCATION_FIX_TIMEOUT_DELAY = 10000;
    private static final int MSG_LOCTION_HUAWEI_BEGIN = 20;
    private static final int MSG_LOG_EXCESS_RECEIVER = 29;
    private static final int MSG_LOG_PERMISSION_DENY = 28;
    private static final int MSG_SAVE_LOCATION_IN_DATABASE = 27;
    private static final int MSG_SAVE_LOCATION_TIMEOUT_DELAY = 600000;
    private static final int MSG_SAVE_LOCATION_VALID_PERIOD = 3600000;
    private static final int MSG_SCREEN_OFF = 23;
    private static final int MSG_SCREEN_ON = 24;
    private static final int MSG_WIFI_ALWAYS_SCAN_REOPEN = 22;
    private static final int MSG_WIFI_ALWAYS_SCAN_RESET = 21;
    private static final int NETWORK_LOCATION_MIN_INTERVAL_BY_5G = 40000;
    private static final int OPERATION_SUCCESS = 0;
    private static final int OTHER_EXCEPTION = -2;
    private static final String SAVED_LOCATION = "save_location_database";
    private static final int SERVICE_RESTART_COUNT = 3;
    private static final long SERVICE_RESTART_TIME_INTERVAL = 60000;
    private static final long SERVICE_RUN_TIME_INTERVAL = 600000;
    static final int SETTINGS_LOCATION_MODE = 1006;
    private static final String SOURCE = "source";
    private static final String TAG = "HwLocationManagerService";
    private static final String TIMESTAMP = "timestamp";
    private static final int WIFI_ALWAYS_SCAN_REOPEN_DELAY = 30000;
    private static final int WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY = 5000;
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG = "wifi_scan_always_available_reset_flag";
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR = "wifi_scan_always_available_user_selection";
    private static boolean isBetaUser = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static Map<Integer, Integer> mSettingsByUserId = new HashMap();
    private static ArrayList<String> mSupervisoryControlWhiteList = new ArrayList<>(Arrays.asList(new String[]{"com.sankuai.meituan"}));
    private long LOCK_HELD_BAD_TIME = 60000;
    private boolean WIFISCAN_CONTROL_ON = SystemProperties.getBoolean("ro.config.hw_wifiscan_control_on", false);
    private boolean isAlwaysScanReset = false;
    private boolean isAlwaysScanScreenOff = false;
    /* access modifiers changed from: private */
    public IBinder mBinderLBSService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(HwLocationManagerService.TAG, "receive broadcast intent, action: " + action);
            if ("android.location.GPS_ENABLED_CHANGE".equals(action)) {
                if (intent.hasExtra("enabled")) {
                    boolean navigating = intent.getBooleanExtra("enabled", false);
                    Log.d(HwLocationManagerService.TAG, "EXTRA_GPS_ENABLED navigating=" + navigating);
                    HwLocationManagerService.this.refreshSystemUIStatus(navigating);
                    if (!navigating) {
                        HwLocationManagerService.this.sendHwLocationMessage(25, 0);
                    }
                }
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                HwLocationManagerService.this.handleWifiStateChanged(intent.getIntExtra("wifi_state", 4));
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwLocationManagerService.this.sendHwLocationMessage(24, HwLocationManagerService.WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwLocationManagerService.this.sendHwLocationMessage(23, HwLocationManagerService.WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwLocationManagerService.this.bindLPPeService();
                HwLocationManagerService.this.bindNlpService();
            }
        }
    };
    /* access modifiers changed from: private */
    public volatile boolean mCheckExcessReceiver = false;
    private HwGeoFencerBase mGeoFencer;
    /* access modifiers changed from: private */
    public final Context mHwContext;
    private HwGpsPowerTracker mHwGpsPowerTracker;
    private boolean mIs5GHzBandSupported = false;
    /* access modifiers changed from: private */
    public boolean mLBSServiceStart = false;
    /* access modifiers changed from: private */
    public LBSServiceDeathHandler mLBSServicedeathHandler;
    private Location mLastLocation = null;
    private Handler mLbsServiceHandler;
    private HwLocalLocationProvider mLocalLocationProvider;
    /* access modifiers changed from: private */
    public LocationManagerServiceUtil mLocationManagerServiceUtil;
    private ArrayList<String> mLogExcessReceiverPkgs = new ArrayList<>();
    private BroadcastReceiver mPackegeClearReceiver = new BroadcastReceiver() {
        private String getPackageName(Intent intent) {
            Uri uri = intent.getData();
            if (uri != null) {
                return uri.getSchemeSpecificPart();
            }
            return null;
        }

        private boolean explicitlyStopped(String pkg) {
            PackageManager pm = HwLocationManagerService.this.mContext.getPackageManager();
            if (pm == null) {
                return false;
            }
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                if (ai == null || (ai.flags & HighBitsCompModeID.MODE_EYE_PROTECT) == 0) {
                    return false;
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(HwLocationManagerService.TAG, "package info not found:" + pkg);
                return false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action) || "android.intent.action.PACKAGE_RESTARTED".equals(action)) {
                    String pkg = getPackageName(intent);
                    if (explicitlyStopped(pkg)) {
                        HwLocationManagerService.this.removeStoppedRecords(pkg);
                    }
                }
            }
        }
    };
    HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> mPreservedRecordsByPkg = new HashMap<>();
    private HashMap<String, ArrayList<LocationManagerService.Receiver>> mReceiversByPkg = new HashMap<>();
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwLocationManagerService.TAG, "MTK service onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.e(HwLocationManagerService.TAG, "MTK service onServiceDisconnected");
        }
    };
    /* access modifiers changed from: private */
    public long mServiceRestartCount;
    private int mSizeOfReceiversByPkg = 0;
    /* access modifiers changed from: private */
    public long mStartServiceTime;
    HashMap<String, String> mSupervisoryPkgList = new HashMap<>();

    private class LBSServiceDeathHandler implements ServiceConnection, IBinder.DeathRecipient {
        private LBSServiceDeathHandler() {
        }

        public void onServiceConnected(ComponentName arg0, IBinder service) {
            Log.d(HwLocationManagerService.TAG, "bindLbsService Connect lbs service successful");
            IBinder unused = HwLocationManagerService.this.mBinderLBSService = service;
            boolean unused2 = HwLocationManagerService.this.mLBSServiceStart = true;
            HwLocationManagerService.this.notifyServiceDied();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(HwLocationManagerService.TAG, "bindLbsService disconnect lbs service");
            IBinder unused = HwLocationManagerService.this.mBinderLBSService = null;
            boolean unused2 = HwLocationManagerService.this.mLBSServiceStart = false;
        }

        public void binderDied() {
            Log.d(HwLocationManagerService.TAG, "bindLbsService lbs service has died!");
            if (HwLocationManagerService.this.mBinderLBSService != null) {
                HwLocationManagerService.this.mBinderLBSService.unlinkToDeath(HwLocationManagerService.this.mLBSServicedeathHandler, 0);
                IBinder unused = HwLocationManagerService.this.mBinderLBSService = null;
            }
            if (System.currentTimeMillis() - HwLocationManagerService.this.mStartServiceTime > 600000) {
                long unused2 = HwLocationManagerService.this.mServiceRestartCount = 0;
            }
            HwLocationManagerService.this.sendLbsServiceRestartMessage();
        }
    }

    static {
        mSupervisoryControlWhiteList.add("com.sankuai.meituan.dispatch.homebrew");
        mSupervisoryControlWhiteList.add("com.huawei.hidisk");
        mSupervisoryControlWhiteList.add("com.nianticlabs.pokemongo");
        mSupervisoryControlWhiteList.add("com.motoband");
        mSupervisoryControlWhiteList.add("com.tencent.gwgo");
    }

    public HwLocationManagerService(Context context) {
        super(context);
        boolean z = false;
        this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault(this, context);
        this.mHwContext = context;
        this.mGeoFencerEnabled = false;
        this.mIs5GHzBandSupported = isDualBandSupported();
        registerPkgClearReceiver();
        this.mCheckExcessReceiver = Settings.Secure.getInt(this.mHwContext.getContentResolver(), HW_CHECK_EXCESS_RECEIVER, 1) == 1 ? true : z;
    }

    /* access modifiers changed from: private */
    public void refreshSystemUIStatus(boolean isRefreshMonitor) {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            for (LocationManagerService.Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
                receiver.updateMonitoring(isRefreshMonitor);
            }
        }
    }

    private void checkWifiScanAlwaysResetFlag() {
        if (Settings.Global.getInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 0) == 1) {
            Log.d(TAG, " the phone is boot before the wlan alwas scan reset to open.");
            sendHwLocationMessage(22, 0);
        }
    }

    /* access modifiers changed from: private */
    public void sendHwLocationMessage(int what, int delay) {
        Handler locationHandler = this.mLocationManagerServiceUtil.getLocationHandler();
        locationHandler.removeMessages(what);
        Message m = Message.obtain(locationHandler, what);
        if (delay > 0) {
            locationHandler.sendMessageDelayed(m, (long) delay);
        } else {
            locationHandler.sendMessage(m);
        }
    }

    /* access modifiers changed from: private */
    public void handleWifiStateChanged(int state) {
        Log.d(TAG, "wifistate :" + state);
        if (state == 1) {
            int always_wifi_scan = Settings.Global.getInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
            int userAction = Settings.Global.getInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR, 0);
            if ((Settings.Secure.getInt(this.mHwContext.getContentResolver(), "device_provisioned", 0) != 0) && this.isAlwaysScanReset && always_wifi_scan == 1 && userAction != 1) {
                sendHwLocationMessage(21, 0);
            }
            this.isAlwaysScanReset = false;
        } else if (state == 3) {
            this.isAlwaysScanReset = true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hwLocationHandleMessage(Message msg) {
        Log.d(TAG, "hwLocationHandleMessage :" + msg.what + ", sceenoff:" + this.isAlwaysScanScreenOff);
        int i = msg.what;
        if (i != 7) {
            switch (i) {
                case 21:
                    Settings.Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                    Settings.Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 1);
                    sendHwLocationMessage(22, 30000);
                    break;
                case 22:
                    Settings.Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 1);
                    Settings.Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 0);
                    break;
                case 23:
                    int always_wifi_scan = Settings.Global.getInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                    int userAction = Settings.Global.getInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR, 0);
                    if ((Settings.Secure.getInt(this.mHwContext.getContentResolver(), "device_provisioned", 0) != 0) && always_wifi_scan == 1 && userAction != 1) {
                        this.isAlwaysScanScreenOff = true;
                        Settings.Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 0);
                        Settings.Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 1);
                        break;
                    }
                case 24:
                    if (this.isAlwaysScanScreenOff != 0) {
                        this.isAlwaysScanScreenOff = false;
                        Settings.Global.putInt(this.mHwContext.getContentResolver(), "wifi_scan_always_enabled", 1);
                        Settings.Global.putInt(this.mHwContext.getContentResolver(), WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG, 0);
                        break;
                    }
                    break;
                case 25:
                    if (Settings.Global.getInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) == 1) {
                        Log.d(TAG, "set gnss_location_fix_status to 0");
                        Settings.Global.putInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0);
                    }
                    readySaveLocationToDataBase();
                    break;
                case 26:
                    Log.d(TAG, "bindLbsService start service message has come.");
                    startLBSService();
                    bindLBSService();
                    break;
                case 27:
                    saveLocationToDataBase();
                    break;
                case 28:
                    hwLogPermissionDeny(((Integer) msg.obj).intValue());
                    break;
                case 29:
                    HwGpsLogServices.getInstance(this.mHwContext).logExcessReceiver((String) msg.obj);
                    break;
                default:
                    return false;
            }
        } else {
            hwLocationCheck();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void enableLocalLocationProviders(GnssLocationProvider gnssProvider) {
        if (gnssProvider == null || !gnssProvider.isLocalDBEnabled()) {
            Log.e(TAG, "localDB is disabled");
            return;
        }
        Log.e(TAG, "init and enable localLocationProvider ");
        this.mLocalLocationProvider = HwLocalLocationProvider.getInstance(this.mLocationManagerServiceUtil.getContext(), this);
        this.mLocalLocationProvider.enable();
        this.mLocationManagerServiceUtil.addProviderLocked(this.mLocalLocationProvider);
        HashMap<String, LocationProviderInterface> realProviders = this.mLocationManagerServiceUtil.getRealProviders();
        synchronized (realProviders) {
            realProviders.put(HwLocalLocationManager.LOCAL_PROVIDER, this.mLocalLocationProvider);
        }
        this.mLocationManagerServiceUtil.getEnabledProviders().add(this.mLocalLocationProvider.getName());
    }

    /* access modifiers changed from: protected */
    public void updateLocalLocationDB(Location location, String provider) {
        if (location != null) {
            if (!provider.equals("passive")) {
                try {
                    String str = MASTER_PASSWORD;
                    String encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, location.getLongitude() + "");
                    String str2 = MASTER_PASSWORD;
                    String encryptedLat = HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, location.getLatitude() + "");
                    Log.d(TAG, "result loc: " + encryptedLong + ", " + encryptedLat);
                } catch (Exception e) {
                    Log.e(TAG, "print loc Exception");
                }
            }
            if (this.mLocalLocationProvider == null || this.mLocalLocationProvider.isValidLocation(location)) {
                if (!provider.equals("passive") && !provider.equals(HwLocalLocationManager.LOCAL_PROVIDER) && this.mLocalLocationProvider != null && this.mLocalLocationProvider.isEnabled()) {
                    this.mLocalLocationProvider.updataLocationDB(location);
                }
                updateLastLocationDataBase(location, provider);
                return;
            }
            Log.d(TAG, "incoming location is invdlid,and not report app");
        }
    }

    public void initHwLocationPowerTracker(Context context) {
        this.mHwGpsPowerTracker = new HwGpsPowerTracker(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.GPS_ENABLED_CHANGE");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        if (this.WIFISCAN_CONTROL_ON) {
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addCategory("android.net.wifi.WIFI_STATE_CHANGED@hwBrExpand@WifiStatus=WIFIENABLED|WifiStatus=WIFIDISABLED");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
        }
        this.mHwContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, this.mLocationManagerServiceUtil.getLocationHandler());
        this.mLBSServicedeathHandler = new LBSServiceDeathHandler();
        this.mLbsServiceHandler = this.mLocationManagerServiceUtil.getLocationHandler();
        checkWifiScanAlwaysResetFlag();
        registerLocationObserver();
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
            this.mLocationManagerServiceUtil.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("quick_gps_switch"), true, new ContentObserver(this.mLocationManagerServiceUtil.getLocationHandler()) {
                public void onChange(boolean selfChange) {
                    int quickGpsSettings = Settings.Global.getInt(HwLocationManagerService.this.mLocationManagerServiceUtil.getContext().getContentResolver(), "quick_gps_switch", 1);
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
        long expireAt;
        if (this.mGeoFencer == null || !this.mGeoFencerEnabled) {
            return false;
        }
        if (sanitizedRequest.getExpireAt() == Long.MAX_VALUE) {
            expireAt = -1;
        } else {
            expireAt = sanitizedRequest.getExpireAt() - SystemClock.elapsedRealtime();
        }
        long expiration = expireAt;
        HwGeoFencerBase hwGeoFencerBase = this.mGeoFencer;
        GeoFenceParams geoFenceParams = new GeoFenceParams(uid, geofence.getLatitude(), geofence.getLongitude(), geofence.getRadius(), expiration, intent, packageName);
        hwGeoFencerBase.add(geoFenceParams);
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
        int location_fix_status = Settings.Global.getInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0);
        Bundle extras = location.getExtras();
        if ((extras == null || !extras.getBoolean("QUICKGPS")) && "gps".equals(provider)) {
            if (location_fix_status == 0) {
                Log.d(TAG, "set gnss_location_fix_status to 1");
                Settings.Global.putInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 1);
            }
            sendHwLocationMessage(25, 10000);
        }
    }

    /* access modifiers changed from: protected */
    public Location screenLocationLocked(Location location, String provider) {
        setGnssLocationFixStatus(location, provider);
        if (this.mLocationManagerServiceUtil.isMockProvider("network")) {
            return location;
        }
        LocationProviderProxy providerProxy = this.mLocationManagerServiceUtil.getProvidersByName().get("network");
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
            if (!pNames[0].equals(this.mComboNlpPackageName) && !pNames[1].equals(this.mComboNlpPackageName)) {
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
        if (!extras.containsKey(this.mComboNlpReadyMarker)) {
            ArrayList<LocationManagerService.UpdateRecord> records = this.mLocationManagerServiceUtil.getRecordsByProvider().get("passive");
            if (records != null) {
                Iterator<LocationManagerService.UpdateRecord> it = records.iterator();
                while (it.hasNext()) {
                    LocationManagerService.UpdateRecord r = it.next();
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
        } else {
            if (D) {
                Log.d(TAG, "This location is marked as ready for broadcast");
            }
            extras.remove(this.mComboNlpReadyMarker);
        }
        return location;
    }

    /* access modifiers changed from: protected */
    public void setGeoFencerEnabled(boolean enabled) {
        if (this.mGeoFencer != null) {
            this.mGeoFencerEnabled = enabled;
        }
    }

    /* access modifiers changed from: protected */
    public void dumpGeoFencer(PrintWriter pw) {
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
        HwGpsLogServices.getInstance(this.mHwContext).gpsFreeze(pkg, uid, proxy);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isFreeze(String pkg) {
        return GpsFreezeProc.getInstance().isFreeze(pkg);
    }

    /* access modifiers changed from: protected */
    public void dumpGpsFreezeProxy(PrintWriter pw) {
        GpsFreezeProc.getInstance().dump(pw);
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        GpsFreezeProc.getInstance().refreshPackageWhitelist(type, pkgList);
    }

    /* access modifiers changed from: protected */
    public ArraySet<String> getPackageWhiteList(int type) {
        return GpsFreezeProc.getInstance().getPackageWhiteList(type);
    }

    private int addSupervisoryControlProc(String packagename, int uid, int expectedmode) {
        String str = packagename;
        int i = uid;
        int i2 = expectedmode;
        if (!enforceAccessHwLMSPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            Slog.i(TAG, "addSupervisoryControlProc has no hw access_location_service permission!");
            return -2;
        }
        String mExpectedMode = getMode(i2);
        if (mExpectedMode == null) {
            return -1;
        }
        if (i <= 1000) {
            Slog.i(TAG, "addSupervisoryControlProc, uid less than 1000, invalid! uid is " + i);
            return -2;
        } else if (GpsFreezeProc.getInstance().isInPackageWhiteListByType(5, str)) {
            Slog.i(TAG, "addSupervisoryControlProc, packagename:" + str + " is in PackageWhiteList break");
            return -2;
        } else if (mSupervisoryControlWhiteList.contains(str)) {
            Slog.i(TAG, "addSupervisoryControlProc, packagename:" + str + " is in local whiteList, return");
            return 0;
        } else {
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                this.mSupervisoryPkgList.put(str, mExpectedMode);
            }
            if (isNetworkAvailable(i) || !mExpectedMode.equals("network")) {
                Slog.d(TAG, "addSupervisoryControlProc, mExpectedMode is " + mExpectedMode + ",packagename=" + str + ", mSupervisoryPkgList is " + this.mSupervisoryPkgList.toString());
                HwGpsLogServices.getInstance(this.mHwContext).addIAwareControl(str, i2);
                synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                    HashMap<LocationManagerService.UpdateRecord, String> mSwitchRecords = new HashMap<>();
                    HashMap<Object, LocationManagerService.Receiver> mReceivers = this.mLocationManagerServiceUtil.getReceivers();
                    if (mReceivers != null) {
                        for (LocationManagerService.Receiver receiver : mReceivers.values()) {
                            if (receiver.mIdentity.mPackageName.equals(str)) {
                                for (LocationManagerService.UpdateRecord record : receiver.mUpdateRecords.values()) {
                                    String currentMode = record.mProvider;
                                    String mode = getSwitchMode(i, record.mRealRequest.getProvider(), currentMode, mExpectedMode);
                                    if (!mode.equals(currentMode)) {
                                        mSwitchRecords.put(record, mode);
                                        Slog.d(TAG, "addSupervisoryControlProc, originalMode: " + record.mRealRequest.getProvider() + ", currentMode: " + currentMode + ", mExpectedMode: " + mExpectedMode + ", receiver: " + Integer.toHexString(System.identityHashCode(receiver)));
                                    }
                                    String str2 = packagename;
                                }
                            }
                            str = packagename;
                        }
                    }
                    ArrayList<String> updateProviders = updateLocationMode(mSwitchRecords);
                    if (updateProviders != null && !updateProviders.isEmpty()) {
                        Handler mHandler = this.mLocationManagerServiceUtil.getLocationHandler();
                        mHandler.sendMessage(Message.obtain(mHandler, 6, updateProviders));
                    }
                }
                return 0;
            }
            Slog.i(TAG, "addSupervisoryControlProc, packagename=" + str + "expectedmode is network, but Network is not Available");
            return -2;
        }
    }

    private void removeSupervisoryControlProc(String packagename, int uid) {
        if (!enforceAccessHwLMSPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            Slog.i(TAG, "removeSupervisoryControlProc has no hw access_location_service permission!");
        } else if (uid <= 1000 && uid != 0) {
            Slog.i(TAG, "removeSupervisoryControlProc, uid less than 1000, invalid! uid is " + uid);
        } else if (mSupervisoryControlWhiteList.contains(packagename)) {
            Slog.i(TAG, "removeSupervisoryControlProc packagename:" + packagename + " is in local whiteList, return");
        } else {
            HwGpsLogServices.getInstance(this.mHwContext).removeIAwareControl(packagename);
            HashMap<LocationManagerService.UpdateRecord, String> mSwitchRecords = new HashMap<>();
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                if (packagename == null && uid == 0) {
                    try {
                        Slog.d(TAG, "remove All SupervisoryControlProc " + this.mSupervisoryPkgList.toString());
                        for (Map.Entry<String, String> entry : this.mSupervisoryPkgList.entrySet()) {
                            mSwitchRecords.putAll(getRemoveSwitchRecords(entry.getKey()));
                        }
                        this.mSupervisoryPkgList.clear();
                    } catch (Throwable th) {
                        throw th;
                    }
                } else if (packagename != null) {
                    Slog.d(TAG, "remove SupervisoryControlProc packagename=" + packagename);
                    mSwitchRecords.putAll(getRemoveSwitchRecords(packagename));
                    this.mSupervisoryPkgList.remove(packagename);
                }
                ArrayList<String> updateProviders = updateLocationMode(mSwitchRecords);
                if (updateProviders == null) {
                    updateProviders = new ArrayList<>();
                }
                ArrayList<LocationManagerService.UpdateRecord> preservedRecords = this.mPreservedRecordsByPkg.get(packagename);
                if (preservedRecords != null) {
                    Iterator<LocationManagerService.UpdateRecord> it = preservedRecords.iterator();
                    while (it.hasNext()) {
                        LocationManagerService.UpdateRecord record = it.next();
                        if (!record.mReceiver.mUpdateRecords.containsValue(record)) {
                            String provider = record.mRealRequest.getProvider();
                            record.mReceiver.mUpdateRecords.put(provider, record);
                            Log.i(TAG, "removeSupervisoryControlProc add origin record " + provider);
                            if (!updateProviders.contains(provider)) {
                                updateProviders.add(provider);
                            }
                        }
                    }
                }
                this.mPreservedRecordsByPkg.remove(packagename);
                if (updateProviders != null && !updateProviders.isEmpty()) {
                    Handler mHandler = this.mLocationManagerServiceUtil.getLocationHandler();
                    mHandler.sendMessage(Message.obtain(mHandler, 6, updateProviders));
                }
            }
        }
    }

    private HashMap<LocationManagerService.UpdateRecord, String> getRemoveSwitchRecords(String packagename) {
        HashMap<Object, LocationManagerService.Receiver> mReceivers = this.mLocationManagerServiceUtil.getReceivers();
        HashMap<LocationManagerService.UpdateRecord, String> mSwitchRecords = new HashMap<>();
        if (mReceivers != null) {
            for (LocationManagerService.Receiver receiver : mReceivers.values()) {
                if (receiver.mIdentity.mPackageName.equals(packagename)) {
                    for (LocationManagerService.UpdateRecord record : receiver.mUpdateRecords.values()) {
                        if (!record.mProvider.equals(record.mRealRequest.getProvider())) {
                            mSwitchRecords.put(record, record.mRealRequest.getProvider());
                            Slog.d(TAG, "getRemoveSwitchRecords, packagename:" + packagename + " originalMode: " + record.mRealRequest.getProvider() + ", currentMode: " + record.mProvider + ", receiver: " + Integer.toHexString(System.identityHashCode(receiver)));
                        }
                    }
                }
            }
        }
        ArrayList<LocationManagerService.UpdateRecord> preservedRecords = this.mPreservedRecordsByPkg.get(packagename);
        if (preservedRecords != null) {
            Iterator<LocationManagerService.UpdateRecord> it = preservedRecords.iterator();
            while (it.hasNext()) {
                LocationManagerService.UpdateRecord record2 = it.next();
                if (!record2.mProvider.equals(record2.mRealRequest.getProvider())) {
                    Log.i(TAG, "getRemoveSwitchRecords record " + record2);
                    mSwitchRecords.put(record2, record2.mRealRequest.getProvider());
                    it.remove();
                }
            }
        }
        return mSwitchRecords;
    }

    private ArrayList<String> updateLocationMode(HashMap<LocationManagerService.UpdateRecord, String> mSwitchRecords) {
        if (mSwitchRecords.size() == 0) {
            Slog.d(TAG, " package has no request send yet");
            return null;
        }
        HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProvider = this.mLocationManagerServiceUtil.getRecordsByProvider();
        ArrayList<String> updateproviders = new ArrayList<>();
        for (Map.Entry<LocationManagerService.UpdateRecord, String> entry : mSwitchRecords.entrySet()) {
            String target = entry.getValue();
            String current = entry.getKey().mProvider;
            entry.getKey().mProvider = target;
            recordsByProvider.get(current).remove(entry.getKey());
            if (recordsByProvider.get(target) == null) {
                recordsByProvider.put(target, new ArrayList());
            }
            if (!recordsByProvider.get(target).contains(entry.getKey())) {
                recordsByProvider.get(target).add(entry.getKey());
            }
            entry.getKey().mReceiver.mUpdateRecords.remove(current, entry.getKey());
            LocationManagerService.Receiver receiver = entry.getKey().mReceiver;
            LocationManagerService.UpdateRecord oldRecord = (LocationManagerService.UpdateRecord) entry.getKey().mReceiver.mUpdateRecords.put(target, entry.getKey());
            if (oldRecord != null) {
                ArrayList<LocationManagerService.UpdateRecord> oldRecords = this.mPreservedRecordsByPkg.get(oldRecord.mReceiver.mIdentity.mPackageName);
                if (oldRecords == null) {
                    oldRecords = new ArrayList<>();
                    this.mPreservedRecordsByPkg.put(oldRecord.mReceiver.mIdentity.mPackageName, oldRecords);
                }
                Log.i(TAG, "updateLocationMode oldRecord " + oldRecord);
                if (!oldRecords.contains(oldRecord)) {
                    oldRecords.add(oldRecord);
                }
            }
            if (!updateproviders.contains(current)) {
                updateproviders.add(current);
            }
            if (!updateproviders.contains(target)) {
                updateproviders.add(target);
            }
            Slog.d(TAG, "updateLocationMode receive : " + entry.getKey().mReceiver.toString() + " " + current + " -> " + target);
        }
        return updateproviders;
    }

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
            if (this.mLocationManagerServiceUtil.isAllowedByCurrentUserSettingsLocked("network")) {
                return true;
            }
            Slog.i(TAG, "network setting is unavailable.");
            return false;
        } else if (!"gps".equals(locationMode)) {
            return false;
        } else {
            if (this.mLocationManagerServiceUtil.isAllowedByCurrentUserSettingsLocked("gps")) {
                return true;
            }
            Slog.i(TAG, "gps setting is unavailable.");
            return false;
        }
    }

    private void hwRemoveSwitchUpdates(LocationManagerService.Receiver receiver) {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            ArrayList<LocationManagerService.UpdateRecord> preservedRecords = this.mPreservedRecordsByPkg.get(receiver.mIdentity.mPackageName);
            if (preservedRecords != null) {
                Iterator<LocationManagerService.UpdateRecord> it = preservedRecords.iterator();
                while (it.hasNext()) {
                    LocationManagerService.UpdateRecord record = it.next();
                    if (record.mReceiver.equals(receiver)) {
                        Log.i(TAG, "hwRemoveUpdatesLocked " + record);
                        record.disposeLocked(false);
                        it.remove();
                    }
                }
            }
        }
    }

    private boolean isNetworkAvailable(int uid) {
        long identity;
        int i = uid;
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
            if (infoWifi != null) {
                if (infoMoblie != null) {
                    boolean isWifiConn = infoWifi.isConnected();
                    boolean isMobileConn = infoMoblie.isConnected();
                    Slog.d(TAG, "uid is " + i + " , isWifiConn is " + isWifiConn + " , isMobileConn is " + isMobileConn);
                    HwNetworkPolicyManager manager = HwNetworkPolicyManager.from(this.mHwContext);
                    if (manager == null) {
                        Slog.e(TAG, "HwNetworkPolicyManager is null error!");
                        return false;
                    }
                    identity = Binder.clearCallingIdentity();
                    int policy = manager.getHwUidPolicy(i);
                    Binder.restoreCallingIdentity(identity);
                    boolean wifiAccess = (policy & 2) == 0;
                    boolean mobileAccess = (policy & 1) == 0;
                    Slog.d(TAG, "policy is " + policy + " , wifiAccess is " + wifiAccess + ", mobileAccess is " + mobileAccess);
                    boolean wifiAvaiable = wifiAccess && isWifiConn;
                    boolean mobileAvailable = mobileAccess && isMobileConn;
                    Slog.d(TAG, "wifiAvaiable is " + wifiAvaiable + " , mobileAvailable is " + mobileAvailable);
                    if (!wifiAvaiable) {
                        if (!mobileAvailable) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            Slog.e(TAG, "infoWifi or infoMoblie is null error!");
            return false;
        } catch (RuntimeException e) {
            Slog.e(TAG, "RuntimeException error!" + e.toString());
            return false;
        } catch (Exception e2) {
            Slog.e(TAG, "Exception error!");
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private boolean enforceAccessPermission(int pid, int uid) {
        if (this.mHwContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0 || this.mHwContext.checkPermission(AwarenessConstants.REGISTER_LOCATION_FENCE_PERMISSION, pid, uid) == 0) {
            return true;
        }
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                data.enforceInterface(DESCRIPTOR);
                int _result = getPowerTypeByPackageName(data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1002:
                data.enforceInterface(DESCRIPTOR);
                int _result2 = logEvent(data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            case 1004:
                data.enforceInterface(DESCRIPTOR);
                String packagename = data.readString();
                int uid = data.readInt();
                int expectedmode = data.readInt();
                Slog.d(TAG, "on transact ADD_LOCATION_MODE  uid is " + uid + " , expectedmode is " + expectedmode + " , packagename is " + packagename);
                int _result3 = addSupervisoryControlProc(packagename, uid, expectedmode);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            case 1005:
                data.enforceInterface(DESCRIPTOR);
                String packagename2 = data.readString();
                int uid2 = data.readInt();
                Slog.d(TAG, "on transact REMOVE_LOCATION_MODE uid is " + uid2 + " , packagename is " + packagename2);
                removeSupervisoryControlProc(packagename2, uid2);
                reply.writeNoException();
                return true;
            case 1006:
                data.enforceInterface(DESCRIPTOR);
                int _result4 = checkLocationSettings(data.readInt(), data.readString(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            case 1007:
                data.enforceInterface(DESCRIPTOR);
                Slog.d(TAG, "on transact CODE_GNSS_DETECT");
                ArrayList<String> _result5 = gnssDetect(data.readString());
                reply.writeNoException();
                reply.writeStringList(_result5);
                return true;
            default:
                return HwLocationManagerService.super.onTransact(code, data, reply, flags);
        }
    }

    private int checkLocationSettings(int userId, String name, String value) {
        if (!enforceAccessHwLMSPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            Slog.i(TAG, "checkLocationSettings has no hw access_location_service permission!");
            return -1;
        }
        int resultValue = -1;
        int mUserId = userId;
        if (!isChineseVersion()) {
            return -1;
        }
        if ("location_mode".equals(name)) {
            Log.d(TAG, "LOCATION_MODE, name: " + name + ", value: " + value);
            if (LOCATION_MODE_SENSORS_ONLY.equals(value) || LOCATION_MODE_BATTERY_SAVING.equals(value)) {
                resultValue = 3;
                mSettingsByUserId.put(Integer.valueOf(mUserId), 2);
            } else if (LOCATION_MODE_OFF.equals(value) || LOCATION_MODE_HIGH_ACCURACY.equals(value)) {
                mSettingsByUserId.put(Integer.valueOf(mUserId), 2);
            }
        } else if ("location_providers_allowed".equals(name)) {
            Log.d(TAG, "LOCATION_PROVIDERS_ALLOWED, name: " + name + ", value: " + value);
            if (mSettingsByUserId.containsKey(Integer.valueOf(mUserId))) {
                int cnt = mSettingsByUserId.get(Integer.valueOf(mUserId)).intValue();
                if (cnt > 0) {
                    int cnt2 = cnt - 1;
                    if (cnt2 == 0) {
                        Log.d(TAG, "clear mSettingsByUserId, cnt: " + cnt2);
                        mSettingsByUserId.clear();
                    } else {
                        mSettingsByUserId.put(Integer.valueOf(mUserId), Integer.valueOf(cnt2));
                    }
                }
            } else if ("+gps".equals(value) || "+network".equals(value)) {
                resultValue = 3;
                mSettingsByUserId.put(Integer.valueOf(mUserId), 2);
            } else if ("-gps".equals(value) || "-network".equals(value)) {
                resultValue = 0;
                mSettingsByUserId.put(Integer.valueOf(mUserId), 2);
            }
        }
        return resultValue;
    }

    private boolean isChineseVersion() {
        return "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    }

    public static int qualityToType(int quality) {
        if (quality != 100) {
            if (!(quality == 102 || quality == 104)) {
                if (quality != 203) {
                    switch (quality) {
                        case 200:
                            return 0;
                        case 201:
                            break;
                        default:
                            Slog.d(TAG, "quality( " + quality + " ) is error !");
                            return -1;
                    }
                }
            }
            return 1;
        }
        return 2;
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
            for (LocationManagerService.Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
                if (packageName.equals(receiver.mIdentity.mPackageName)) {
                    HashMap<String, LocationManagerService.UpdateRecord> updateRecords = receiver.mUpdateRecords;
                    if (updateRecords != null) {
                        for (LocationManagerService.UpdateRecord updateRecord : updateRecords.values()) {
                            int power_type = qualityToType(updateRecord.mRequest.getQuality());
                            if (power_type > power_type_ret) {
                                power_type_ret = power_type;
                                if (power_type_ret == 2) {
                                    return power_type_ret;
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

    /* access modifiers changed from: protected */
    public boolean isGPSDisabled() {
        String allowedProviders = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", ActivityManager.getCurrentUser());
        if (!HwDeviceManager.disallowOp(13)) {
            return false;
        }
        if (allowedProviders.contains("gps")) {
            Slog.i(TAG, "gps provider cannot be enabled, set it to false .");
            Settings.Secure.setLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", false, ActivityManager.getCurrentUser());
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public String getLocationProvider(int uid, LocationRequest request, String packageName, String provider) {
        String targetprovider = provider;
        if ("gps".equals(provider) && GpsFreezeProc.getInstance().isInPackageWhiteListByType(5, packageName)) {
            targetprovider = "network";
            Log.d(TAG, "packageName:" + packageName + " is change gps provider to " + targetprovider);
        } else if (this.mSupervisoryPkgList.containsKey(packageName)) {
            if (isNetworkAvailable(uid) || !this.mSupervisoryPkgList.get(packageName).equals("network")) {
                targetprovider = getSwitchMode(uid, provider, provider, this.mSupervisoryPkgList.get(packageName));
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
        if ("gps".equals(provider) && !this.mLBSServiceStart) {
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

    /* access modifiers changed from: private */
    public void notifyServiceDied() {
        try {
            if (this.mBinderLBSService != null) {
                this.mBinderLBSService.linkToDeath(this.mLBSServicedeathHandler, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "IBinder register linkToDeath function fail.");
        }
    }

    /* access modifiers changed from: private */
    public void sendLbsServiceRestartMessage() {
        long delayTime = 0;
        if (!this.mLbsServiceHandler.hasMessages(26)) {
            Message mTimeOut = Message.obtain(this.mLbsServiceHandler, 26);
            delayTime = (long) (60000.0d * Math.pow(2.0d, (double) this.mServiceRestartCount));
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

    /* access modifiers changed from: protected */
    public void updateLastLocationDataBase(Location location, String provider) {
        if (this.mLocalLocationProvider == null || this.mLocalLocationProvider.isValidLocation(location)) {
            try {
                if (this.mLastLocation == null) {
                    this.mLastLocation = readLastLocationDataBase();
                    if (this.mLastLocation == null) {
                        this.mLastLocation = new Location(location);
                        saveLocationToDataBase();
                        return;
                    }
                }
                if (!location.getProvider().equals("network") || !this.mLastLocation.getProvider().equals("gps") || location.getTime() - this.mLastLocation.getTime() <= WifiProCommonUtils.RECHECK_DELAYED_MS) {
                    this.mLastLocation.setLongitude(location.getLongitude());
                    this.mLastLocation.setLatitude(location.getLatitude());
                    this.mLastLocation.setTime(location.getTime());
                    this.mLastLocation.setProvider(location.getProvider());
                    this.mLastLocation.setAccuracy(location.getAccuracy());
                    return;
                }
                Log.e(TAG, "New GPS Provider result not be refreshed by Network Provider result");
            } catch (Exception e) {
                Log.e(TAG, "updateLastLocationDataBase failed", e);
            }
        } else {
            Log.e(TAG, "incoming location is invdlid,and not report app");
        }
    }

    /* access modifiers changed from: protected */
    public void readySaveLocationToDataBase() {
        if (this.mLastLocation == null) {
            Log.e(TAG, "mLastLocation is NULL, nothing to saved");
            return;
        }
        long nowTime = System.currentTimeMillis();
        if (0 == this.mLastLocation.getTime() || nowTime - this.mLastLocation.getTime() > WifiProCommonUtils.RECHECK_DELAYED_MS) {
            sendHwLocationMessage(27, 0);
        } else {
            sendHwLocationMessage(27, 600000);
        }
    }

    /* access modifiers changed from: protected */
    public void saveLocationToDataBase() {
        JSONObject jsonObj = new JSONObject();
        try {
            String str = MASTER_PASSWORD;
            String encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, this.mLastLocation.getLongitude() + "");
            String str2 = MASTER_PASSWORD;
            String encryptedLat = HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, this.mLastLocation.getLatitude() + "");
            jsonObj.put(LONGITUDE, encryptedLong);
            jsonObj.put(LATITUDE, encryptedLat);
            jsonObj.put(ACCURACY, (double) this.mLastLocation.getAccuracy());
            jsonObj.put(TIMESTAMP, this.mLastLocation.getTime());
            jsonObj.put(SOURCE, this.mLastLocation.getProvider());
        } catch (JSONException e) {
            Log.e(TAG, "updateLastLocationDataBase json error!");
            return;
        } catch (Exception e2) {
            Log.e(TAG, "saveLocationToDataBase Exception :" + e2.getMessage());
        }
        Settings.Global.putString(this.mHwContext.getContentResolver(), SAVED_LOCATION, jsonObj.toString());
    }

    /* access modifiers changed from: protected */
    public Location readLastLocationDataBase() {
        try {
            String locationStr = Settings.Global.getString(this.mHwContext.getContentResolver(), SAVED_LOCATION);
            if (locationStr == null) {
                Log.e(TAG, "readLastLocationDataBase locationStr null");
                return null;
            }
            JSONObject jsonObj = new JSONObject(locationStr);
            String encryptedLong = jsonObj.getString(LONGITUDE);
            String decryptedLat = HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, jsonObj.getString(LATITUDE));
            double longitudeSaved = Double.parseDouble(HwCryptoUtility.AESLocalDbCrypto.decrypt(MASTER_PASSWORD, encryptedLong));
            double latitudeSaved = Double.parseDouble(decryptedLat);
            String providerSeaved = jsonObj.getString(SOURCE);
            long timestampSaved = jsonObj.getLong(TIMESTAMP);
            float accuricy = (float) jsonObj.getLong(TIMESTAMP);
            if (0.0d != longitudeSaved) {
                if (0.0d != latitudeSaved) {
                    Location location = new Location("gps");
                    location.setLongitude(longitudeSaved);
                    location.setLatitude(latitudeSaved);
                    location.setTime(timestampSaved);
                    location.setProvider(providerSeaved);
                    location.setAccuracy(accuricy);
                    return location;
                }
            }
            Log.e(TAG, "No record in Database");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "readLastLocationDataBase failed", e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isExceptionAppForUserSettingsLocked(String packageName) {
        if (packageName == null) {
            return false;
        }
        Log.e(TAG, "isExceptionAppForUserSettingsLocked packageName:" + packageName);
        if ("com.hisi.mapcon".equals(packageName)) {
            return true;
        }
        return false;
    }

    private boolean isDualBandSupported() {
        return this.mContext.getResources().getBoolean(17957074);
    }

    /* access modifiers changed from: protected */
    public void hwSendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
        }
    }

    public void hwRequestLocationUpdatesLocked(LocationRequest request, LocationManagerService.Receiver receiver, int pid, int uid, String packageName) {
        HwGnssDetectManager.getInstance(this.mHwContext).hwRequestLocationUpdatesLocked(request, receiver, pid, uid, packageName);
        Handler locationHandler = this.mLocationManagerServiceUtil.getLocationHandler();
        if (!locationHandler.hasMessages(7)) {
            locationHandler.sendEmptyMessageDelayed(7, HwArbitrationDEFS.DelayTimeMillisB);
        }
        boolean isIAwareControl = false;
        if (this.mSupervisoryPkgList.containsKey(packageName)) {
            isIAwareControl = true;
        }
        HwGpsLogServices.getInstance(this.mHwContext).requestStart(request, receiver, pid, uid, packageName, isIAwareControl);
        if (this.mCheckExcessReceiver) {
            checkExcessReceiver(receiver, packageName);
        }
    }

    public ArrayList<String> gnssDetect(String packageName) {
        ArrayList<String> gnssDetect;
        if (!enforceAccessHwLMSPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            Slog.i(TAG, "gnssDetect has no hw access_location_service permission!");
            return null;
        }
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            gnssDetect = HwGnssDetectManager.getInstance(this.mHwContext).gnssDetect(packageName);
        }
        return gnssDetect;
    }

    public void hwRemoveUpdatesLocked(LocationManagerService.Receiver receiver) {
        hwRemoveSwitchUpdates(receiver);
        String providers = null;
        while (receiver.mUpdateRecords.values().iterator().hasNext()) {
            providers = providers + ((LocationManagerService.UpdateRecord) r1.next()).mRealRequest.getProvider() + ",";
        }
        HwGpsLogServices.getInstance(this.mHwContext).requestStop(receiver, providers);
        if (this.mCheckExcessReceiver) {
            removePkgReceiver(receiver, receiver.mIdentity.mPackageName);
        }
    }

    private void registerPkgClearReceiver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        packageFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mPackegeClearReceiver, UserHandle.OWNER, packageFilter, null, null);
    }

    private void hwLocationCheck() {
        removeNotRunAndNotExistRecords();
        hwLockCheck();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0120, code lost:
        return;
     */
    private void removeNotRunAndNotExistRecords() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProvider = this.mLocationManagerServiceUtil.getRecordsByProvider();
            if (recordsByProvider != null) {
                if (!recordsByProvider.isEmpty()) {
                    if (appProcessList == null) {
                        Log.w(TAG, "no Process find");
                        return;
                    }
                    HashMap<Object, LocationManagerService.Receiver> receivers = (HashMap) this.mLocationManagerServiceUtil.getReceivers().clone();
                    for (Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>> entry : recordsByProvider.entrySet()) {
                        ArrayList<LocationManagerService.UpdateRecord> records = entry.getValue();
                        if (records != null) {
                            Iterator it = ((ArrayList) records.clone()).iterator();
                            while (it.hasNext()) {
                                LocationManagerService.UpdateRecord record = (LocationManagerService.UpdateRecord) it.next();
                                if (!receivers.containsValue(record.mReceiver)) {
                                    record.disposeLocked(false);
                                    Log.w(TAG, "receiver not exists, but updateRecord not remove!  pid = " + record.mReceiver.mIdentity.mPid + " uid = " + record.mReceiver.mIdentity.mUid + " UpdateRecord = " + record);
                                } else {
                                    boolean isfound = false;
                                    Iterator<ActivityManager.RunningAppProcessInfo> it2 = appProcessList.iterator();
                                    while (true) {
                                        if (!it2.hasNext()) {
                                            break;
                                        }
                                        ActivityManager.RunningAppProcessInfo appProcess = it2.next();
                                        if (appProcess.pid == record.mReceiver.mIdentity.mPid && appProcess.uid == record.mReceiver.mIdentity.mUid) {
                                            isfound = true;
                                            break;
                                        }
                                    }
                                    if (!isfound) {
                                        this.mLocationManagerServiceUtil.removeUpdatesLocked(record.mReceiver);
                                        Log.w(TAG, "process may be died, but request not remove!  pid = " + record.mReceiver.mIdentity.mPid + " uid = " + record.mReceiver.mIdentity.mUid + " receiver = " + record.mReceiver.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void hwLockCheck() {
        ArrayList<LocationManagerService.UpdateRecord> longLockRecords = checkLongLock();
        clearLongLock(longLockRecords);
        callClientHandle(longLockRecords);
        finalCheck();
    }

    private ArrayList<LocationManagerService.UpdateRecord> checkLongLock() {
        long currentTime;
        ArrayList<LocationManagerService.UpdateRecord> longLockRecords = new ArrayList<>();
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProvider = this.mLocationManagerServiceUtil.getRecordsByProvider();
            long currentTime2 = SystemClock.elapsedRealtime();
            for (Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>> entry : recordsByProvider.entrySet()) {
                Iterator it = entry.getValue().iterator();
                while (it.hasNext()) {
                    LocationManagerService.UpdateRecord record = (LocationManagerService.UpdateRecord) it.next();
                    long acquireLockTime = record.mReceiver.mAcquireLockTime;
                    if (acquireLockTime > record.mReceiver.mReleaseLockTime) {
                        currentTime = currentTime2;
                        if (currentTime2 - acquireLockTime >= this.LOCK_HELD_BAD_TIME) {
                            Log.i(TAG, "hold lock for too long time " + record + " receiver " + Integer.toHexString(System.identityHashCode(record.mReceiver)));
                            longLockRecords.add(record);
                        }
                    } else {
                        currentTime = currentTime2;
                    }
                    currentTime2 = currentTime;
                }
            }
        }
        return longLockRecords;
    }

    private void clearLongLock(ArrayList<LocationManagerService.UpdateRecord> longLockRecords) {
        if (longLockRecords != null && !longLockRecords.isEmpty()) {
            int listSize = longLockRecords.size();
            for (int i = 0; i < listSize; i++) {
                LocationManagerService.UpdateRecord record = longLockRecords.get(i);
                synchronized (record.mReceiver) {
                    record.mReceiver.clearPendingBroadcastsLocked();
                }
            }
        }
    }

    private void callClientHandle(ArrayList<LocationManagerService.UpdateRecord> badRecords) {
        if (badRecords != null && !badRecords.isEmpty()) {
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                Iterator<LocationManagerService.UpdateRecord> it = badRecords.iterator();
                while (it.hasNext()) {
                    LocationManagerService.UpdateRecord record = it.next();
                    if (record.mReceiver.mListener != null) {
                        record.mReceiver.callLocationChangedLocked(new Location("DEAD"));
                    }
                }
            }
        }
    }

    private void finalCheck() {
        boolean needCheckAgain;
        int totalPendingBroadcasts = 0;
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProvider = this.mLocationManagerServiceUtil.getRecordsByProvider();
            for (Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>> entry : recordsByProvider.entrySet()) {
                Iterator it = entry.getValue().iterator();
                while (it.hasNext()) {
                    totalPendingBroadcasts += ((LocationManagerService.UpdateRecord) it.next()).mReceiver.mPendingBroadcasts;
                }
            }
            ArrayList<LocationManagerService.UpdateRecord> gpsRecords = recordsByProvider.get("gps");
            needCheckAgain = totalPendingBroadcasts > 0 && gpsRecords != null && !gpsRecords.isEmpty();
        }
        Log.i(TAG, "hwCheckLock finalCheck " + totalPendingBroadcasts + " needCheckAgain " + needCheckAgain);
        if (needCheckAgain) {
            Handler locationHandler = this.mLocationManagerServiceUtil.getLocationHandler();
            if (!locationHandler.hasMessages(7)) {
                locationHandler.sendEmptyMessageDelayed(7, HwArbitrationDEFS.DelayTimeMillisB);
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeStoppedRecords(String pkgName) {
        if (pkgName != null) {
            boolean isPkgHasRecord = false;
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                Iterator<Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>>> it = this.mLocationManagerServiceUtil.getRecordsByProvider().entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ArrayList<LocationManagerService.UpdateRecord> records = it.next().getValue();
                    if (records != null) {
                        Iterator it2 = ((ArrayList) records.clone()).iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                break;
                            } else if (pkgName.equals(((LocationManagerService.UpdateRecord) it2.next()).mReceiver.mIdentity.mPackageName)) {
                                Log.i(TAG, "package stopped, remove updateRecords and receivers: " + pkgName);
                                isPkgHasRecord = true;
                                break;
                            }
                        }
                        if (isPkgHasRecord) {
                            break;
                        }
                    }
                }
            }
            if (isPkgHasRecord) {
                removeNotRunAndNotExistRecords();
            }
        }
    }

    /* access modifiers changed from: private */
    public void bindLPPeService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.mediatek.location.lppe.main", "com.mediatek.location.lppe.main.LPPeServiceWrapper"));
        boolean bound = this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1073741829, new UserHandle(0));
        if (D) {
            Log.d(TAG, "binding lppe service for MTK bound = " + bound);
        }
    }

    /* access modifiers changed from: private */
    public void bindNlpService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.mediatek.nlpservice", "com.mediatek.nlpservice.NlpService"));
        boolean bound = this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1073741829, new UserHandle(0));
        if (D) {
            Log.d(TAG, "binding nlp service for MTK bound = " + bound);
        }
    }

    private boolean enforceAccessHwLMSPermission(int pid, int uid) {
        if (this.mHwContext.checkPermission("huawei.permission.ACCESS_LOCATION_SERVICE", pid, uid) == 0) {
            return true;
        }
        if (isBetaUser) {
            this.mLbsServiceHandler.sendMessage(Message.obtain(this.mLbsServiceHandler, 28, Integer.valueOf(uid)));
        }
        return false;
    }

    private void hwLogPermissionDeny(int uid) {
        String[] packages = this.mHwContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            Log.d(TAG, "hwLogPermissionDeny invalid uid " + uid);
            return;
        }
        String data = "";
        for (String pkg : packages) {
            data = data + pkg + ",";
        }
        IMonitor.EventStream assertStream = IMonitor.openEventStream(DFT_CHIP_ASSERT_EVENT);
        if (assertStream == null) {
            Log.e(TAG, "assertStream is null");
            return;
        }
        assertStream.setParam(0, 1001);
        assertStream.setParam(1, data);
        IMonitor.sendEvent(assertStream);
        IMonitor.closeEventStream(assertStream);
    }

    private void registerLocationObserver() {
        this.mHwContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(HW_CHECK_EXCESS_RECEIVER), false, new ContentObserver(this.mLocationManagerServiceUtil.getLocationHandler()) {
            public void onChange(boolean selfChange) {
                HwLocationManagerService hwLocationManagerService = HwLocationManagerService.this;
                boolean z = true;
                if (Settings.Secure.getInt(HwLocationManagerService.this.mHwContext.getContentResolver(), HwLocationManagerService.HW_CHECK_EXCESS_RECEIVER, 1) != 1) {
                    z = false;
                }
                boolean unused = hwLocationManagerService.mCheckExcessReceiver = z;
                synchronized (HwLocationManagerService.this.mLocationManagerServiceUtil.getmLock()) {
                    HwLocationManagerService.this.resetPkgReceivers();
                }
            }
        }, -1);
    }

    private void checkExcessReceiver(LocationManagerService.Receiver receiver, String packageName) {
        if (addPkgReceiver(receiver, packageName)) {
            ArrayList<LocationManagerService.Receiver> curReceivers = this.mReceiversByPkg.get(packageName);
            if (curReceivers.size() >= 200) {
                Log.i(TAG, "app requests too much receiver: " + packageName + " size: " + pkgSize);
                if (!this.mLogExcessReceiverPkgs.contains(packageName)) {
                    this.mLogExcessReceiverPkgs.add(packageName);
                    Message.obtain(this.mLocationManagerServiceUtil.getLocationHandler(), 29, packageName).sendToTarget();
                }
                ArrayList<LocationManagerService.Receiver> toRemoveReceivers = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    toRemoveReceivers.add(curReceivers.get(i));
                }
                for (int i2 = 0; i2 < 100; i2++) {
                    this.mLocationManagerServiceUtil.removeUpdatesLocked(toRemoveReceivers.get(i2));
                }
            }
        }
    }

    private boolean addPkgReceiver(LocationManagerService.Receiver receiver, String packageName) {
        ArrayList<LocationManagerService.Receiver> pkgReceivers = this.mReceiversByPkg.get(packageName);
        if (pkgReceivers == null) {
            pkgReceivers = new ArrayList<>();
            this.mReceiversByPkg.put(packageName, pkgReceivers);
        }
        if (pkgReceivers.contains(receiver)) {
            return false;
        }
        pkgReceivers.add(receiver);
        this.mSizeOfReceiversByPkg++;
        return true;
    }

    private void removePkgReceiver(LocationManagerService.Receiver receiver, String pkgName) {
        ArrayList<LocationManagerService.Receiver> curReceivers = this.mReceiversByPkg.get(pkgName);
        if (curReceivers != null && curReceivers.remove(receiver)) {
            this.mSizeOfReceiversByPkg--;
        }
        checkWithSourceReceivers();
    }

    private void checkWithSourceReceivers() {
        int sizeOfSourceReceivers = this.mLocationManagerServiceUtil.getReceivers().size();
        if (Math.abs(sizeOfSourceReceivers - this.mSizeOfReceiversByPkg) > 5) {
            StringBuilder assertInfo = new StringBuilder();
            assertInfo.append("Inconsistent Receivers, here:");
            assertInfo.append(this.mSizeOfReceiversByPkg);
            assertInfo.append(", source:");
            assertInfo.append(sizeOfSourceReceivers);
            Log.i(TAG, assertInfo.toString());
            Message.obtain(this.mLocationManagerServiceUtil.getLocationHandler(), 29, assertInfo.toString()).sendToTarget();
            resetPkgReceivers();
        }
    }

    /* access modifiers changed from: private */
    public void resetPkgReceivers() {
        this.mReceiversByPkg.clear();
        this.mSizeOfReceiversByPkg = 0;
        for (LocationManagerService.Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
            addPkgReceiver(receiver, receiver.mIdentity.mPackageName);
        }
    }
}
