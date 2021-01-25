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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.location.GeoFenceParams;
import android.location.Geofence;
import android.location.ILocationListener;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IMonitor;
import com.android.internal.annotations.GuardedBy;
import com.android.server.LocationManagerService;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.location.CallerIdentity;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GpsFreezeListener;
import com.android.server.location.GpsFreezeProc;
import com.android.server.location.GpsLocationProviderUtils;
import com.android.server.location.HwCryptoUtility;
import com.android.server.location.HwGeoFencerBase;
import com.android.server.location.HwGeoFencerProxy;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.location.HwLbsConfigManager;
import com.android.server.location.HwLocalLocationManager;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.location.HwLocator;
import com.android.server.location.HwQuickTTFFMonitor;
import com.android.server.location.LBSLog;
import com.android.server.location.LbsConfigContent;
import com.android.server.location.LbsParaUpdater;
import com.android.server.location.LocationHandlerEx;
import com.android.server.location.RemoteListenerHelper;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.pgmng.log.LogPower;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class HwLocationManagerService extends LocationManagerService {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = ".HwLBSService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.lbs";
    private static final int CODE_ADD_LOCATION_MODE = 1004;
    private static final int CODE_GET_POWR_TYPE = 1001;
    private static final int CODE_GNSS_DETECT = 1007;
    private static final int CODE_LOG_EVENT = 1002;
    private static final int CODE_REMOVE_LOCATION_MODE = 1005;
    private static final int DEALY_LBS = 1000;
    private static final int DEFAULT_MODE = 0;
    private static final int DEFAULT_SIZE = 16;
    public static final String DEL_PKG = "pkg";
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final int DFT_ASSERT_ERROR_CODE = 1001;
    private static final int DFT_CHIP_ASSERT_EVENT = 910009014;
    private static final int DOMESTIC_BETA = 3;
    private static final int ERROR_CODE = -1;
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;
    private static final String FORBID_LOCATION_REQUEST_STATUS = "FORBID_LOCATION_REQUEST_STATUS";
    private static final int FORBID_REQUEST_STATUS_DISABLE = 1;
    private static final int FORBID_REQUEST_STATUS_ENABLE = 0;
    private static final int FOREVER = -1;
    private static final int GNSS_FIX_STATUS_DISABLE = 0;
    private static final int GNSS_FIX_STATUS_ENABLE = 1;
    private static final int GNSS_LOCATION_FIRST_FIX_TIMEOUT_DELAY = 120000;
    private static final String GNSS_LOCATION_FIX_STATUS = "GNSS_LOCATION_FIX_STATUS";
    private static final int GNSS_LOCATION_FIX_TIMEOUT_DELAY = 10000;
    private static final int HICAR_PC_CASTING_ERRORCODE = -1;
    private static final int HICAR_RUNNING_STATE_OFF = 0;
    private static final int HICAR_RUNNING_STATE_ON = 1;
    private static final String HWNLP_SERVICE_NAME = "com.huawei.lbs";
    private static final int INVALID_MODE_CODE = -1;
    private static final String LBS_DESCRIPTOR = "com.huawei.lbs.IHuaweiHiGeoService";
    private static final String LOCATION_MODE_BATTERY_SAVING = Integer.toString(2);
    private static final String LOCATION_MODE_HIGH_ACCURACY = Integer.toString(3);
    private static final String LOCATION_MODE_OFF = Integer.toString(0);
    private static final String LOCATION_MODE_SENSORS_ONLY = Integer.toString(1);
    private static final int LOCATION_POWER_TYPE_HIGH = 2;
    private static final int LOCATION_POWER_TYPE_LOW = 1;
    private static final int LOCATION_POWER_TYPE_NONE = 0;
    private static final String MASTER_PASSWORD = HwLocalLocationManager.MASTER_PASSWORD;
    private static final int MAX_MEMORY_LIMIT_SIZE = 19456;
    private static final int MAX_RECEIVER_LIMIT_SIZE = 1;
    private static final Object MEMORY_LOCK = new Object();
    private static final int MODE_BATCHING = 2;
    private static final int MODE_FREEZE = 5;
    private static final int MODE_GPS = 1;
    private static final int MODE_NETWORK = 3;
    private static final int MODE_PASSIVE = 4;
    private static final int MSG_DELAY_START_LBS_SERVICE = 26;
    private static final int MSG_FIRST_START_LBS = 35;
    private static final int MSG_GPSFREEZEPROC_LISTNER = 29;
    private static final int MSG_LOCATION_FIRST_FIX_TIMEOUT = 31;
    private static final int MSG_LOCATION_FIX_TIMEOUT = 25;
    private static final int MSG_LOCTION_HUAWEI_BEGIN = 20;
    private static final int MSG_LOG_PERMISSION_DENY = 32;
    private static final int MSG_MOCK_LOCATION = 34;
    private static final int MSG_QUICK_LOCATION = 33;
    private static final int MSG_SUPERVISORY_CONTROL = 28;
    private static final int MSG_WHITELIST_LISTNER = 30;
    private static final int OPERATION_SUCCESS = 0;
    private static final int OTHER_EXCEPTION = -2;
    private static final int POWER_PUSH_STOP_CODE = -1;
    private static final String REMOVE_MOCK = "remove";
    private static final long SCHEDULE_PERIOD_TIME = 180000;
    private static final int SERVICE_RESTART_COUNT = 3;
    private static final long SERVICE_RESTART_TIME_INTERVAL = 60000;
    private static final long SERVICE_RUN_TIME_INTERVAL = 600000;
    private static final int SETTINGS_LOCATION_MODE = 1006;
    private static final long START_DELAY_TIME = 180000;
    private static final String START_MOCK = "add";
    private static final String TAG = "HwLocationManagerService";
    private static final int TRANSACTION_CALLBACK_MOCK_POSITION = 1009;
    private static final int TRANSACTION_CALLBACK_QUICK_LOCATION = 1008;
    private static final int TRANSACTION_CANCEL_NOTIFICATION = 51;
    private static final int TRANSACTION_CHECK_EXIST_BUSINESS = 47;
    private static final int TRANSACTION_HANDLE_MOCK_POSITION = 49;
    private static final int TRANSACTION_HANDLE_QUICK = 50;
    private static final int USER_SETTING_COUNT_PLUS = 2;
    private static final int WIFI_ALWAYS_SCAN_REOPEN_DELAY = 30000;
    private static final int WIFI_ALWAYS_SCAN_SCEENONOFF_DELAY = 5000;
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE_RESET_FLAG = "wifi_scan_always_available_reset_flag";
    private static final String WIFI_SCAN_ALWAYS_AVAILABLE_USER_OPR = "wifi_scan_always_available_user_selection";
    private static boolean isBetaUser;
    private static Map<Integer, Integer> sSettingsByUserIds = new HashMap(16);
    private long endTime = 0;
    private boolean isAlwaysScanReset = false;
    private boolean isAlwaysScanScreenOff = false;
    private boolean isGeoFencerEnabled;
    private boolean isLbsServiceStart = false;
    private boolean isTimerRunning = false;
    private IBinder mBinderLbsService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwLocationManagerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                LBSLog.i(HwLocationManagerService.TAG, false, "receive broadcast intent, action: %{public}s", action);
                if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwLocationManagerService.this.bindLppeService();
                    HwLocationManagerService.this.bindNlpService();
                    return;
                }
                LBSLog.i(HwLocationManagerService.TAG, false, "receive broadcast last else", new Object[0]);
            }
        }
    };
    private String mComboNlpPackageName;
    private String mComboNlpReadyMarker;
    private String mComboNlpScreenMarker;
    private HwGeoFencerBase mGeoFencer;
    private String mGeoFencerPackageName;
    private GpsLocationProviderUtils mGpsLocationProviderUtils = EasyInvokeFactory.getInvokeUtils(GpsLocationProviderUtils.class);
    private ActivityManager mHwActivityManager;
    private final Context mHwContext;
    private HwGpsPowerTracker mHwGpsPowerTracker;
    private HwLbsConfigManager mHwLbsConfigManager;
    private HwLocator mHwLocator;
    private Handler mLbsServiceHandler;
    private LbsServiceDeathHandler mLbsServicedeathHandler;
    private HwLocalLocationProvider mLocalLocationProvider;
    private Handler mLocationHandler;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;
    private Timer mMemoryTimer;
    private HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> mPreservedRecordsByPkgs = new HashMap<>(16);
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.server.HwLocationManagerService.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            LBSLog.i(HwLocationManagerService.TAG, false, "MTK service onServiceConnected", new Object[0]);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            LBSLog.e(HwLocationManagerService.TAG, false, "MTK service onServiceDisconnected", new Object[0]);
        }
    };
    private long mServiceRestartCount;
    private long mStartServiceTime;
    private HashMap<String, String> mSupervisoryPkgLists = new HashMap<>(16);
    private final Object mockLock = new Object();
    private long startTime = 0;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        }
        isBetaUser = z;
    }

    public HwLocationManagerService(Context context) {
        super(context);
        this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault(this, context);
        this.mHwContext = context;
        this.isGeoFencerEnabled = false;
        this.mHwQuickTTFFMonitor = HwServiceFactory.getHwQuickTTFFMonitor(this.mContext, (GnssLocationProvider) null);
        this.mLocationManagerServiceUtil.setHwLocationGpsLogServices(HwServiceFactory.getHwGpsLogServices(this.mContext));
        Handler handler = LocationHandlerEx.getInstance();
        this.mLocationManagerServiceUtil.setLocationHandler(handler);
        this.mLocationHandler = new Handler(handler.getLooper()) {
            /* class com.android.server.HwLocationManagerService.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                HwLocationManagerService.this.hwLocationHandleMessage(msg);
            }
        };
        LbsParaUpdater.getInstance(this.mHwContext).init();
        this.mHwLbsConfigManager = HwLbsConfigManager.getInstance(this.mHwContext);
        GpsFreezeProc.getInstance().initHwLbsConfigManager(this.mHwContext);
        Object mHwActivityManagerObject = this.mHwContext.getSystemService("activity");
        if (mHwActivityManagerObject instanceof ActivityManager) {
            this.mHwActivityManager = (ActivityManager) mHwActivityManagerObject;
        }
        this.mHwLocator = HwLocator.getInstance();
    }

    private void refreshSystemUiStatus(boolean isRefreshMonitor) {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            for (LocationManagerService.Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
                receiver.updateMonitoring(isRefreshMonitor);
            }
        }
    }

    public void updateNavigatingStatus(boolean isNavigating) {
        LBSLog.i(TAG, false, "updateNavigatingStatus navigating = %{public}b", Boolean.valueOf(isNavigating));
        refreshSystemUiStatus(isNavigating);
        if (!isNavigating) {
            sendHwLocationMessage(25, 0);
        }
    }

    private void sendHwLocationMessage(int what, int delay) {
        this.mLocationHandler.removeMessages(what);
        Message msg = Message.obtain(this.mLocationHandler, what);
        if (delay > 0) {
            this.mLocationHandler.sendMessageDelayed(msg, (long) delay);
        } else {
            this.mLocationHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: protected */
    public boolean hwLocationHandleMessage(Message msg) {
        int i = msg.what;
        if (i == 7) {
            HwLocationLockManager.getInstance(this.mHwContext).hwLocationLockCheck();
        } else if (i == 25) {
            operateWhenLocationFixTimeout();
        } else if (i != 26) {
            switch (i) {
                case 28:
                    operateWhenSupervisoryControl(msg);
                    break;
                case 29:
                    handleGpsFreezeChanged(msg);
                    break;
                case 30:
                    updateFreezeList(msg);
                    break;
                case 31:
                    LBSLog.i(TAG, false, "set gnss_location_fix_status to GNSS_FIX_STATUS_ENABLE", new Object[0]);
                    Settings.Global.putInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 1);
                    break;
                case 32:
                    hwLogPermissionDeny(msg);
                    break;
                case 33:
                    sendMessageQuickLocation(msg);
                    break;
                case 34:
                    removeMock(msg);
                    break;
                case 35:
                    firstStartLbs(msg);
                    break;
                default:
                    return false;
            }
        } else {
            LBSLog.i(TAG, false, "bindLbsService start service message has come.", new Object[0]);
            startLbsService();
            bindLbsService();
        }
        return true;
    }

    private void firstStartLbs(Message msg) {
        ArrayList list = null;
        if (msg.obj instanceof ArrayList) {
            list = (ArrayList) msg.obj;
        }
        if (list != null) {
            handleMockPosition(String.valueOf(list.get(0)), String.valueOf(list.get(1)));
        }
    }

    private void sendMessageQuickLocation(Message msg) {
        if (msg == null) {
            LBSLog.e(TAG, false, "message is null", new Object[0]);
            return;
        }
        Location qLocation = null;
        if (msg.obj instanceof Location) {
            qLocation = (Location) msg.obj;
        }
        if (qLocation == null) {
            LBSLog.e(TAG, false, "gnssLocationProvider is null", new Object[0]);
            return;
        }
        LBSLog.i(TAG, false, "handle send message success", new Object[0]);
        GnssLocationProvider gnssLocationProvider = null;
        ArrayList<LocationManagerService.LocationProvider> locationProviderManagers = this.mLocationManagerServiceUtil.getRealProviders();
        if (locationProviderManagers == null) {
            LBSLog.e(TAG, false, "locationProviderManagers is null", new Object[0]);
            return;
        }
        Iterator<LocationManagerService.LocationProvider> it = locationProviderManagers.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            LocationManagerService.LocationProvider providerManager = it.next();
            if ("gps".equals(providerManager.getName()) && (providerManager.mProvider instanceof GnssLocationProvider)) {
                gnssLocationProvider = providerManager.mProvider;
                break;
            }
        }
        GpsLocationProviderUtils gpsLocationProviderUtils = this.mGpsLocationProviderUtils;
        if (gpsLocationProviderUtils == null) {
            LBSLog.e(TAG, false, "mGpsLocationProviderUtils is null", new Object[0]);
        } else if (gnssLocationProvider == null) {
            LBSLog.e(TAG, false, "gnssLocationProvider is null", new Object[0]);
        } else {
            gpsLocationProviderUtils.reportLocation(gnssLocationProvider, true, qLocation);
        }
    }

    private void removeMock(Message msg) {
        LBSLog.i(TAG, false, "remove mock message", new Object[0]);
        ArrayList list = null;
        if (msg.obj instanceof ArrayList) {
            list = (ArrayList) msg.obj;
        }
        if (list != null) {
            removeMockTest(String.valueOf(list.get(0)), String.valueOf(list.get(1)));
        }
    }

    private void operateWhenLocationFixTimeout() {
        if (this.mLocationHandler.hasMessages(31)) {
            this.mLocationHandler.removeMessages(31);
        }
        if (Settings.Global.getInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) == 1) {
            Settings.Global.putInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0);
        }
    }

    private void operateWhenSupervisoryControl(Message msg) {
        ArrayList<String> providers = (ArrayList) msg.obj;
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            Iterator<String> it = providers.iterator();
            while (it.hasNext()) {
                this.mLocationManagerServiceUtil.applyRequirementsLocked(it.next());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateLocalLocationDB(Location location, String provider) {
        HwLocalLocationProvider hwLocalLocationProvider;
        if (location != null) {
            setGnssLocationFixStatus(location, provider);
            if ((this.mHwQuickTTFFMonitor instanceof HwQuickTTFFMonitor) && (("gps".equals(provider) || "network".equals(provider)) && !this.mHwQuickTTFFMonitor.isQuickLocation(location))) {
                this.mLocationManagerServiceUtil.setRealLastLocation(location, provider);
            }
            quickTtffPowerTracker(provider);
            if (!"passive".equals(provider)) {
                try {
                    String str = MASTER_PASSWORD;
                    String encryptedLong = HwCryptoUtility.AESLocalDbCrypto.encrypt(str, location.getLongitude() + "");
                    String str2 = MASTER_PASSWORD;
                    LBSLog.i(TAG, false, "result loc: %{public}s, %{public}s", encryptedLong, HwCryptoUtility.AESLocalDbCrypto.encrypt(str2, location.getLatitude() + ""));
                } catch (Exception e) {
                    LBSLog.e(TAG, false, "print loc Exception", new Object[0]);
                }
            }
            HwLocalLocationProvider hwLocalLocationProvider2 = this.mLocalLocationProvider;
            if (hwLocalLocationProvider2 != null && !hwLocalLocationProvider2.isValidLocation(location)) {
                LBSLog.i(TAG, false, "incoming location is invdlid,and not report app", new Object[0]);
            } else if (!"passive".equals(provider) && (hwLocalLocationProvider = this.mLocalLocationProvider) != null && hwLocalLocationProvider.isEnabled()) {
                this.mLocalLocationProvider.updataLocationDB(location);
            }
        }
    }

    private boolean isHiCar() {
        return SystemProperties.getBoolean("ro.config.hw_hicar_mode", false) && SystemPropertiesEx.getInt("hw.pc.casting.displayid", -1) != -1 && Settings.Global.getInt(this.mHwContext.getContentResolver(), "hicar_running_status", 0) == 1;
    }

    public void initHwLocationPowerTracker(Context context) {
        this.mHwGpsPowerTracker = new HwGpsPowerTracker(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mHwContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, this.mLocationManagerServiceUtil.getLocationHandler());
        this.mLbsServicedeathHandler = new LbsServiceDeathHandler();
        this.mLbsServiceHandler = this.mLocationHandler;
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
                /* class com.android.server.HwLocationManagerService.AnonymousClass4 */

                @Override // android.database.ContentObserver
                public void onChange(boolean isSelfChange) {
                    int quickGpsSettings = Settings.Global.getInt(HwLocationManagerService.this.mLocationManagerServiceUtil.getContext().getContentResolver(), "quick_gps_switch", 1);
                    SystemProperties.set("persist.sys.pgps.config", Integer.toString(quickGpsSettings));
                    LBSLog.i(HwLocationManagerService.TAG, false, "Settings.Global.QUICK_GPS_SWITCH set %{public}d", Integer.valueOf(quickGpsSettings));
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
        String str = this.mGeoFencerPackageName;
        if (str == null || packageManager.resolveService(new Intent(str), 0) == null) {
            this.mGeoFencer = null;
            this.isGeoFencerEnabled = false;
        } else {
            this.mGeoFencer = HwGeoFencerProxy.getGeoFencerProxy(this.mLocationManagerServiceUtil.getContext(), this.mGeoFencerPackageName);
            this.isGeoFencerEnabled = true;
        }
        this.mComboNlpPackageName = Resources.getSystem().getString(33685940);
        if (this.mComboNlpPackageName != null) {
            this.mComboNlpReadyMarker = this.mComboNlpPackageName + ".nlp:ready";
            this.mComboNlpScreenMarker = this.mComboNlpPackageName + ".nlp:screen";
        }
    }

    public boolean addQcmGeoFencer(Geofence geofence, LocationRequest sanitizedRequest, int uid, PendingIntent intent, String packageName) {
        long expiration;
        if (this.mGeoFencer == null || !this.isGeoFencerEnabled) {
            return false;
        }
        if (sanitizedRequest.getExpireAt() == Long.MAX_VALUE) {
            expiration = -1;
        } else {
            expiration = sanitizedRequest.getExpireAt() - SystemClock.elapsedRealtime();
        }
        this.mGeoFencer.add(new GeoFenceParams(uid, geofence.getLatitude(), geofence.getLongitude(), geofence.getRadius(), expiration, intent, packageName));
        return true;
    }

    public boolean removeQcmGeoFencer(PendingIntent intent) {
        HwGeoFencerBase hwGeoFencerBase = this.mGeoFencer;
        if (hwGeoFencerBase == null || !this.isGeoFencerEnabled) {
            return false;
        }
        hwGeoFencerBase.remove(intent);
        return true;
    }

    private void setGnssLocationFixStatus(Location location, String provider) {
        Bundle extras = location.getExtras();
        if ((extras == null || !extras.getBoolean("QUICKGPS")) && "gps".equals(provider)) {
            int delayTime = 0;
            if (Settings.Global.getInt(this.mHwContext.getContentResolver(), GNSS_LOCATION_FIX_STATUS, 0) == 0 && !this.mLocationHandler.hasMessages(31)) {
                if (!isHiCar()) {
                    delayTime = GNSS_LOCATION_FIRST_FIX_TIMEOUT_DELAY;
                }
                sendHwLocationMessage(31, delayTime);
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
        LocationManagerService.LocationProvider providerProxy = null;
        Object locationProviderObject = this.mLocationManagerServiceUtil.getProvidersByName().get("network");
        if (locationProviderObject != null && (locationProviderObject instanceof LocationManagerService.LocationProvider)) {
            providerProxy = (LocationManagerService.LocationProvider) locationProviderObject;
        }
        if (this.mComboNlpPackageName == null || providerProxy == null || !"network".equals(provider) || this.mLocationManagerServiceUtil.isMockProvider("network")) {
            return location;
        }
        Bundle extras = location.getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        if (!extras.containsKey(this.mComboNlpReadyMarker)) {
            ArrayList<LocationManagerService.UpdateRecord> records = this.mLocationManagerServiceUtil.getRecordsByProvider().get("passive");
            if (records == null) {
                return location;
            }
            if (callLocationChanged(location, extras, records)) {
                return null;
            }
            LBSLog.i(TAG, false, "Not screening locations", new Object[0]);
        } else {
            LBSLog.i(TAG, false, "This location is marked as ready for broadcast", new Object[0]);
            extras.remove(this.mComboNlpReadyMarker);
        }
        return location;
    }

    private boolean callLocationChanged(Location location, Bundle extras, ArrayList<LocationManagerService.UpdateRecord> records) {
        boolean isBeingScreenedExt = false;
        Iterator<LocationManagerService.UpdateRecord> it = records.iterator();
        while (it.hasNext()) {
            LocationManagerService.UpdateRecord updateRecord = it.next();
            if (Objects.equals(updateRecord.mReceiver.mCallerIdentity.mPackageName, this.mComboNlpPackageName)) {
                if (!isBeingScreenedExt) {
                    isBeingScreenedExt = true;
                    extras.putBoolean(this.mComboNlpScreenMarker, true);
                }
                if (!updateRecord.mReceiver.callLocationChangedLocked(location)) {
                    LBSLog.w(TAG, false, "RemoteException calling onLocationChanged on %{public}s", updateRecord.mReceiver);
                } else {
                    LBSLog.i(TAG, false, "Sending location for screening", new Object[0]);
                }
            }
        }
        return isBeingScreenedExt;
    }

    /* access modifiers changed from: protected */
    public void setGeoFencerEnabled(boolean isEnabled) {
        if (this.mGeoFencer != null) {
            this.isGeoFencerEnabled = isEnabled;
        }
    }

    /* access modifiers changed from: protected */
    public void dumpGeoFencer(PrintWriter printWriter) {
        HwGeoFencerBase hwGeoFencerBase = this.mGeoFencer;
        if (hwGeoFencerBase != null && this.isGeoFencerEnabled) {
            hwGeoFencerBase.dump(printWriter, "");
        }
    }

    public boolean proxyGps(String packageName, int uid, boolean isProxy) {
        if (isProxy) {
            GpsFreezeProc.getInstance().addFreezeProcess(packageName, uid);
        } else {
            GpsFreezeProc.getInstance().removeFreezeProcess(packageName, uid);
        }
        HwGpsLogServices.getInstance(this.mHwContext).gpsFreeze(packageName, uid, isProxy);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isFreeze(String packageName) {
        int forbidRequestStatus = Settings.Global.getInt(this.mHwContext.getContentResolver(), FORBID_LOCATION_REQUEST_STATUS, 0);
        if (forbidRequestStatus != 1) {
            return GpsFreezeProc.getInstance().isFreeze(packageName);
        }
        LBSLog.i(TAG, false, "forbidRequestStatus is " + forbidRequestStatus, new Object[0]);
        return true;
    }

    /* access modifiers changed from: protected */
    public void dumpGpsFreezeProxy(PrintWriter pw) {
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        GpsFreezeProc.getInstance().refreshPackageWhitelist(type, pkgList);
    }

    /* access modifiers changed from: protected */
    public ArraySet<String> getPackageWhiteList(int type) {
        return null;
    }

    private int addSupervisoryControlProc(String packageName, int uid, int expectedmode) {
        if (!hasAccessHwLmsPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            LBSLog.i(TAG, false, "addSupervisoryControlProc has no hw access_location_service permission!", new Object[0]);
            return -2;
        }
        String expectedModeEx = getMode(expectedmode);
        if (expectedModeEx == null) {
            return -1;
        }
        if (uid <= 1000) {
            LBSLog.i(TAG, false, "addSupervisoryControlProc, uid less than 1000, invalid! uid is %{public}d", Integer.valueOf(uid));
            return -2;
        } else if (GpsFreezeProc.getInstance().isInPackageWhiteListByType(5, packageName)) {
            LBSLog.i(TAG, false, "addSupervisoryControlProc, packageName:%{public}s is in PackageWhiteList break", packageName);
            return -2;
        } else if (this.mHwLbsConfigManager.getListForFeature(LbsConfigContent.CONFIG_IAWARE_CONTROLL_WHITELIST).contains(packageName)) {
            LBSLog.i(TAG, false, "addSupervisoryControlProc, packageName:%{public}s is in local whiteList, return", packageName);
            return 0;
        } else {
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                this.mSupervisoryPkgLists.put(packageName, expectedModeEx);
            }
            if (this.mLocationManagerServiceUtil.isNetworkAvailable() || !"network".equals(expectedModeEx)) {
                LBSLog.i(TAG, false, "addSupervisoryControlProc, %{public}s, %{public}s , mSupervisoryPkgLists is %{public}s", expectedModeEx, packageName, this.mSupervisoryPkgLists.toString());
                HwGpsLogServices.getInstance(this.mHwContext).addIAwareControl(packageName, expectedmode);
                synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                    updateProviderInControl(packageName, uid, expectedModeEx);
                }
                return 0;
            }
            LBSLog.i(TAG, false, "addSupervisoryControlProc, %{public}s expectedmode is network, but Network is not Available", packageName);
            return -2;
        }
    }

    private void updateProviderInControl(String packageName, int uid, String expectedModeEx) {
        HashMap<LocationManagerService.UpdateRecord, String> switchRecords = new HashMap<>(16);
        updateSwitchRecords(this.mLocationManagerServiceUtil.getReceivers(), switchRecords, uid, expectedModeEx, packageName);
        ArrayList<String> updateProviders = updateLocationMode(switchRecords);
        if (updateProviders != null && !updateProviders.isEmpty()) {
            this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 28, updateProviders));
        }
    }

    private void updateSwitchRecords(HashMap<Object, LocationManagerService.Receiver> receivers, HashMap<LocationManagerService.UpdateRecord, String> switchRecords, int uid, String expectedModeEx, String packageName) {
        if (receivers != null) {
            for (LocationManagerService.Receiver receiver : receivers.values()) {
                if (Objects.equals(receiver.mCallerIdentity.mPackageName, packageName)) {
                    for (LocationManagerService.UpdateRecord record : receiver.mUpdateRecords.values()) {
                        String currentMode = record.mProvider;
                        String mode = getSwitchMode(uid, record.mRealRequest.getProvider(), currentMode, expectedModeEx);
                        if (!Objects.equals(mode, currentMode)) {
                            switchRecords.put(record, mode);
                            LBSLog.i(TAG, false, "addSupervisoryControlProc, %{public}s, %{public}s, %{public}s, %{public}s", record.mRealRequest.getProvider(), currentMode, expectedModeEx, Integer.toHexString(System.identityHashCode(receiver)));
                        }
                    }
                }
            }
        }
    }

    private void removeSupervisoryControlProc(String packageName, int uid) {
        if (!hasAccessHwLmsPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            LBSLog.i(TAG, false, "removeSupervisoryControlProc has no hw access_location_service permission!", new Object[0]);
        } else if (uid <= 1000 && uid != 0) {
            LBSLog.i(TAG, false, "removeSupervisoryControlProc, uid less than 1000, invalid! uid is %{public}d", Integer.valueOf(uid));
        } else if (this.mHwLbsConfigManager.getListForFeature(LbsConfigContent.CONFIG_IAWARE_CONTROLL_WHITELIST).contains(packageName)) {
            LBSLog.i(TAG, false, "removeSupervisoryControlProc : %{public}s is in local whiteList, return", packageName);
        } else {
            HwGpsLogServices.getInstance(this.mHwContext).removeIAwareControl(packageName);
            HashMap<LocationManagerService.UpdateRecord, String> switchRecords = new HashMap<>(16);
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                updateProvider(switchRecords, packageName, uid);
            }
        }
    }

    private void updateProvider(HashMap<LocationManagerService.UpdateRecord, String> switchRecords, String packageName, int uid) {
        if (packageName == null && uid == 0) {
            LBSLog.i(TAG, false, "remove All SupervisoryControlProc %{public}s", this.mSupervisoryPkgLists.toString());
            for (Map.Entry<String, String> entry : this.mSupervisoryPkgLists.entrySet()) {
                switchRecords.putAll(getRemoveSwitchRecords(entry.getKey()));
            }
            this.mSupervisoryPkgLists.clear();
        } else if (packageName != null) {
            LBSLog.i(TAG, false, "remove SupervisoryControlProc packageName = %{public}s", packageName);
            switchRecords.putAll(getRemoveSwitchRecords(packageName));
            this.mSupervisoryPkgLists.remove(packageName);
        } else {
            LBSLog.i(TAG, false, "remove SupervisoryControlProc last else", new Object[0]);
        }
        ArrayList<String> updateProviders = updateLocationMode(switchRecords);
        if (updateProviders == null) {
            updateProviders = new ArrayList<>(16);
        }
        ArrayList<LocationManagerService.UpdateRecord> preservedRecords = this.mPreservedRecordsByPkgs.get(packageName);
        if (preservedRecords != null) {
            Iterator<LocationManagerService.UpdateRecord> it = preservedRecords.iterator();
            while (it.hasNext()) {
                LocationManagerService.UpdateRecord record = it.next();
                if (!record.mReceiver.mUpdateRecords.containsValue(record)) {
                    String provider = record.mRealRequest.getProvider();
                    record.mReceiver.mUpdateRecords.put(provider, record);
                    LBSLog.i(TAG, false, "removeSupervisoryControlProc add origin record %{public}s", provider);
                    if (!updateProviders.contains(provider)) {
                        updateProviders.add(provider);
                    }
                }
            }
        }
        this.mPreservedRecordsByPkgs.remove(packageName);
        if (!updateProviders.isEmpty()) {
            this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 28, updateProviders));
        }
    }

    private HashMap<LocationManagerService.UpdateRecord, String> getRemoveSwitchRecords(String packageName) {
        HashMap<Object, LocationManagerService.Receiver> mReceivers = this.mLocationManagerServiceUtil.getReceivers();
        HashMap<LocationManagerService.UpdateRecord, String> mSwitchRecords = new HashMap<>(16);
        if (mReceivers != null) {
            for (LocationManagerService.Receiver receiver : mReceivers.values()) {
                if (receiver.mCallerIdentity.mPackageName.equals(packageName)) {
                    for (LocationManagerService.UpdateRecord record : receiver.mUpdateRecords.values()) {
                        if (!record.mProvider.equals(record.mRealRequest.getProvider())) {
                            mSwitchRecords.put(record, record.mRealRequest.getProvider());
                            LBSLog.i(TAG, false, "getRemoveSwitchRecords, %{public}s, %{public}s, %{public}s, %{public}s", packageName, record.mRealRequest.getProvider(), record.mProvider, Integer.toHexString(System.identityHashCode(receiver)));
                        }
                    }
                }
            }
        }
        ArrayList<LocationManagerService.UpdateRecord> preservedRecords = this.mPreservedRecordsByPkgs.get(packageName);
        if (preservedRecords != null) {
            Iterator<LocationManagerService.UpdateRecord> it = preservedRecords.iterator();
            while (it.hasNext()) {
                LocationManagerService.UpdateRecord record2 = it.next();
                if (!record2.mProvider.equals(record2.mRealRequest.getProvider())) {
                    LBSLog.i(TAG, false, "getRemoveSwitchRecords record %{public}s", record2);
                    mSwitchRecords.put(record2, record2.mRealRequest.getProvider());
                    it.remove();
                }
            }
        }
        return mSwitchRecords;
    }

    private ArrayList<String> updateLocationMode(HashMap<LocationManagerService.UpdateRecord, String> switchRecords) {
        if (switchRecords.size() == 0) {
            LBSLog.i(TAG, false, " package has no request send yet", new Object[0]);
            return null;
        }
        HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProviders = this.mLocationManagerServiceUtil.getRecordsByProvider();
        int i = 16;
        ArrayList<String> updateproviders = new ArrayList<>(16);
        for (Map.Entry<LocationManagerService.UpdateRecord, String> entry : switchRecords.entrySet()) {
            String target = entry.getValue();
            String current = entry.getKey().mProvider;
            entry.getKey().mProvider = target;
            recordsByProviders.get(current).remove(entry.getKey());
            if (recordsByProviders.get(target) == null) {
                recordsByProviders.put(target, new ArrayList<>(i));
            }
            if (!recordsByProviders.get(target).contains(entry.getKey())) {
                recordsByProviders.get(target).add(entry.getKey());
            }
            entry.getKey().mReceiver.mUpdateRecords.remove(current, entry.getKey());
            LocationManagerService.Receiver receiver = entry.getKey().mReceiver;
            LocationManagerService.UpdateRecord oldRecord = (LocationManagerService.UpdateRecord) entry.getKey().mReceiver.mUpdateRecords.put(target, entry.getKey());
            if (oldRecord != null) {
                ArrayList<LocationManagerService.UpdateRecord> oldRecords = this.mPreservedRecordsByPkgs.get(oldRecord.mReceiver.mCallerIdentity.mPackageName);
                if (oldRecords == null) {
                    oldRecords = new ArrayList<>(i);
                    this.mPreservedRecordsByPkgs.put(oldRecord.mReceiver.mCallerIdentity.mPackageName, oldRecords);
                }
                LBSLog.i(TAG, false, "updateLocationMode oldRecord %{public}s", oldRecord);
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
            LBSLog.i(TAG, false, "updateLocationMode receive : %{public}s, %{public}s -> %{public}s", entry.getKey().mReceiver.toString(), current, target);
            i = 16;
        }
        return updateproviders;
    }

    private String getSwitchMode(int uid, String originalMode, String currentMode, String expectedMode) {
        String targetmode = null;
        if (originalMode == null || expectedMode == null || currentMode == null || Objects.equals(expectedMode, currentMode) || "passive".equals(originalMode)) {
            return currentMode;
        }
        if ("gps".equals(expectedMode)) {
            targetmode = originalMode;
        }
        if ("network".equals(expectedMode) && "gps".equals(currentMode)) {
            targetmode = "network";
        }
        if ("passive".equals(expectedMode)) {
            targetmode = "passive";
        }
        if (targetmode == null || !isLocationModeAvailable(targetmode, uid)) {
            return currentMode;
        }
        return targetmode;
    }

    private String getMode(int mode) {
        if (mode == 0) {
            return null;
        }
        if (mode == 1) {
            return "gps";
        }
        if (mode == 2) {
            return null;
        }
        if (mode == 3) {
            return "network";
        }
        if (mode == 4) {
            return "passive";
        }
        if (mode == 5) {
            return null;
        }
        LBSLog.e(TAG, false, "add unknow LocationMode, error! expectedmode is %{public}d", Integer.valueOf(mode));
        return null;
    }

    private boolean isLocationModeAvailable(String locationMode, int uid) {
        if (locationMode == null) {
            return false;
        }
        if ("network".equals(locationMode)) {
            LocationManagerServiceUtil locationManagerServiceUtil = this.mLocationManagerServiceUtil;
            if (locationManagerServiceUtil.isProviderEnabledForUser("network", locationManagerServiceUtil.getCurrentUserId())) {
                return true;
            }
            LBSLog.i(TAG, false, "network setting is unavailable.", new Object[0]);
            return false;
        } else if ("gps".equals(locationMode)) {
            LocationManagerServiceUtil locationManagerServiceUtil2 = this.mLocationManagerServiceUtil;
            if (locationManagerServiceUtil2.isProviderEnabledForUser("gps", locationManagerServiceUtil2.getCurrentUserId())) {
                return true;
            }
            LBSLog.i(TAG, false, "gps setting is unavailable.", new Object[0]);
            return false;
        } else {
            LBSLog.i(TAG, false, "locationMode :%{public}s", locationMode);
            return false;
        }
    }

    private void hwRemoveSwitchUpdates(LocationManagerService.Receiver receiver) {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            ArrayList<LocationManagerService.UpdateRecord> preservedRecords = this.mPreservedRecordsByPkgs.get(receiver.mCallerIdentity.mPackageName);
            if (preservedRecords != null) {
                Iterator<LocationManagerService.UpdateRecord> it = preservedRecords.iterator();
                while (it.hasNext()) {
                    LocationManagerService.UpdateRecord record = it.next();
                    if (Objects.equals(record.mReceiver, receiver)) {
                        LBSLog.i(TAG, false, "hwRemoveUpdatesLocked %{public}s", record);
                        record.disposeLocked(false);
                        it.remove();
                    }
                }
            }
        }
    }

    private boolean enforceAccessPermission(int pid, int uid) {
        if (this.mHwContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0 || this.mHwContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0) {
            return true;
        }
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                transactWithCodeGetPowerType(data, reply, flags);
                return true;
            case 1002:
                transactWithCodeLogEvent(data, reply, flags);
                return true;
            case 1003:
            default:
                return HwLocationManagerService.super.onTransact(code, data, reply, flags);
            case 1004:
                if (!this.mHwLbsConfigManager.isEnableForParam(LbsConfigContent.CONFIG_IAWARE_ENABLE)) {
                    LBSLog.i(TAG, false, "iaware controll disabled", new Object[0]);
                    return true;
                }
                transactWithCodeAddLocationMode(data, reply, flags);
                return true;
            case 1005:
                if (!this.mHwLbsConfigManager.isEnableForParam(LbsConfigContent.CONFIG_IAWARE_ENABLE)) {
                    LBSLog.i(TAG, false, "iaware controll disabled", new Object[0]);
                    return true;
                }
                transactWithCodeRemoveLocationMode(data, reply, flags);
                return true;
            case 1006:
                transactWithCodeSettingLocationMode(data, reply, flags);
                return true;
            case 1007:
                transactWithCodeGnssDetect(data, reply, flags);
                return true;
            case 1008:
                return transactQuickLocationCallBack(data, reply, flags);
            case 1009:
                transactMockCallBack(data, reply, flags);
                return true;
        }
    }

    private boolean transactQuickLocationCallBack(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        if (!hasAccessHwLmsPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            return false;
        }
        Location location = (Location) Location.CREATOR.createFromParcel(data);
        if (location == null) {
            LBSLog.i(TAG, false, "ontransact location is null", new Object[0]);
            return false;
        }
        this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 33, location));
        return true;
    }

    private void transactMockCallBack(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        String type = data.readString();
        String opPackageName = data.readString();
        ArrayList<String> list = new ArrayList<>();
        list.add(type);
        list.add(opPackageName);
        LBSLog.i(TAG, false, "remove mock, type is %{public}s, opPackageName is %{public}s", type, opPackageName);
        this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 34, list));
    }

    private void transactWithCodeGetPowerType(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        int result = getPowerTypeByPackageName(data.readString());
        reply.writeNoException();
        reply.writeInt(result);
    }

    private void transactWithCodeLogEvent(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        int result = logEvent(data.readInt(), data.readInt(), data.readString());
        reply.writeNoException();
        reply.writeInt(result);
    }

    private void transactWithCodeAddLocationMode(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        String packageName = data.readString();
        int uid = data.readInt();
        int expectedMode = data.readInt();
        LBSLog.i(TAG, false, "on transact ADD_LOCATION_MODE uid is %{public}d , %{public}d , %{public}s", Integer.valueOf(uid), Integer.valueOf(expectedMode), packageName);
        int result = addSupervisoryControlProc(packageName, uid, expectedMode);
        reply.writeNoException();
        reply.writeInt(result);
    }

    private void transactWithCodeRemoveLocationMode(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        String packageName = data.readString();
        int uid = data.readInt();
        LBSLog.i(TAG, false, "on transact REMOVE_LOCATION_MODE uid is %{public}d , packageName is %{public}s", Integer.valueOf(uid), packageName);
        removeSupervisoryControlProc(packageName, uid);
        reply.writeNoException();
    }

    private void transactWithCodeSettingLocationMode(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        int result = checkLocationSettings(data.readInt(), data.readString(), data.readString());
        reply.writeNoException();
        reply.writeInt(result);
    }

    private void transactWithCodeGnssDetect(Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        LBSLog.i(TAG, false, "on transact CODE_GNSS_DETECT", new Object[0]);
        ArrayList<String> results = gnssDetect(data.readString());
        reply.writeNoException();
        reply.writeStringList(results);
    }

    private int checkLocationSettings(int userId, String name, String value) {
        return -1;
    }

    private boolean isChineseVersion() {
        return "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    }

    public static int qualityToType(int quality) {
        if (quality == 100) {
            return 2;
        }
        if (!(quality == 102 || quality == 104)) {
            if (quality == 203) {
                return 2;
            }
            if (quality == 200) {
                return 0;
            }
            if (quality != 201) {
                LBSLog.i(TAG, false, "quality( %{public}d ) is error !", Integer.valueOf(quality));
                return -1;
            }
        }
        return 1;
    }

    public int getPowerTypeByPackageName(String packageName) {
        int powerTypeRet;
        if (!enforceAccessPermission(Binder.getCallingPid(), Binder.getCallingUid()) || TextUtils.isEmpty(packageName)) {
            return -1;
        }
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            powerTypeRet = getPowerType(packageName);
        }
        return powerTypeRet;
    }

    private int getPowerType(String packageName) {
        HashMap<String, LocationManagerService.UpdateRecord> updateRecords;
        int powerTypeRet = -1;
        for (LocationManagerService.Receiver receiver : this.mLocationManagerServiceUtil.getReceivers().values()) {
            if (Objects.equals(packageName, receiver.mCallerIdentity.mPackageName) && (updateRecords = receiver.mUpdateRecords) != null) {
                for (LocationManagerService.UpdateRecord updateRecord : updateRecords.values()) {
                    int powerType = qualityToType(updateRecord.mRequest.getQuality());
                    if (powerType > powerTypeRet && (powerTypeRet = powerType) == 2) {
                        return powerTypeRet;
                    }
                }
                continue;
            }
        }
        return powerTypeRet;
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
        if (!allowedProviders.contains("gps")) {
            return true;
        }
        LBSLog.i(TAG, false, "gps provider cannot be enabled, set it to false .", new Object[0]);
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", 0, ActivityManager.getCurrentUser());
        return true;
    }

    /* access modifiers changed from: protected */
    public String getLocationProvider(int uid, LocationRequest request, String packageName, String provider) {
        String targetprovider = provider;
        if ("gps".equals(provider) && GpsFreezeProc.getInstance().isInPackageWhiteListByType(5, packageName)) {
            targetprovider = "network";
            LBSLog.i(TAG, false, "packageName:%{public}s is change gps provider to %{public}s", packageName, targetprovider);
        } else if (!this.mSupervisoryPkgLists.containsKey(packageName) || !this.mHwLbsConfigManager.isEnableForParam(LbsConfigContent.CONFIG_IAWARE_ENABLE)) {
            LBSLog.i(TAG, false, "getLocationProvider last else", new Object[0]);
        } else {
            if (this.mLocationManagerServiceUtil.isNetworkAvailable() || !"network".equals(this.mSupervisoryPkgLists.get(packageName))) {
                targetprovider = getSwitchMode(uid, provider, provider, this.mSupervisoryPkgLists.get(packageName));
            } else {
                LBSLog.i(TAG, false, "network is not available, packageName:%{public}s need not change provider to network", packageName);
                targetprovider = provider;
            }
            LBSLog.i(TAG, false, "packageName:%{public}s is change %{public}s provider to %{public}s", packageName, provider, targetprovider);
        }
        LBSLog.i(TAG, false, "lbsService isLbsServiceStart:%{public}b", Boolean.valueOf(this.isLbsServiceStart));
        if ("gps".equals(provider) && !this.isLbsServiceStart) {
            LBSLog.i(TAG, false, " LocationManager. bindLbsService start.", new Object[0]);
            startLbsService();
            bindLbsService();
            if (this.mLbsServiceHandler.hasMessages(26)) {
                LBSLog.i(TAG, false, " mLbsServiceHandler has delay message. bindLbsService start.", new Object[0]);
                this.mLbsServiceHandler.removeMessages(26);
                this.mServiceRestartCount = 0;
            }
        }
        return targetprovider;
    }

    private void startLbsService() {
        this.mStartServiceTime = System.currentTimeMillis();
        LBSLog.i(TAG, false, "%{public}s start lbs service. bindLbsService start time:%{public}tQ", this.mContext.getPackageName(), Long.valueOf(this.mStartServiceTime));
        try {
            Intent bindIntent = new Intent();
            bindIntent.setClassName("com.huawei.lbs", "com.huawei.lbs.HwLBSService");
            bindIntent.addFlags(268435456);
            this.mContext.startService(bindIntent);
        } catch (Exception e) {
            LBSLog.e(TAG, false, "startLbsService Exception", new Object[0]);
        }
    }

    private void bindLbsService() {
        LBSLog.i(TAG, false, "%{public}s bind lbs service. bindLbsService", this.mContext.getPackageName());
        try {
            Intent bindIntent = new Intent();
            bindIntent.setClassName("com.huawei.lbs", "com.huawei.lbs.HwLBSService");
            this.mContext.bindService(bindIntent, this.mLbsServicedeathHandler, 1);
        } catch (Exception e) {
            LBSLog.e(TAG, false, "bindLbsService Exception", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyServiceDied() {
        try {
            if (this.mBinderLbsService != null) {
                this.mBinderLbsService.linkToDeath(this.mLbsServicedeathHandler, 0);
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "IBinder register linkToDeath function fail.", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public class LbsServiceDeathHandler implements ServiceConnection, IBinder.DeathRecipient {
        private LbsServiceDeathHandler() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            LBSLog.i(HwLocationManagerService.TAG, false, "bindLbsService Connect lbs service successful", new Object[0]);
            HwLocationManagerService.this.mBinderLbsService = service;
            HwLocationManagerService.this.isLbsServiceStart = true;
            HwLocationManagerService.this.notifyServiceDied();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            LBSLog.i(HwLocationManagerService.TAG, false, "bindLbsService disconnect lbs service", new Object[0]);
            HwLocationManagerService.this.mBinderLbsService = null;
            HwLocationManagerService.this.isLbsServiceStart = false;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            LBSLog.i(HwLocationManagerService.TAG, false, "bindLbsService lbs service has died!", new Object[0]);
            if (HwLocationManagerService.this.mBinderLbsService != null) {
                HwLocationManagerService.this.mBinderLbsService.unlinkToDeath(HwLocationManagerService.this.mLbsServicedeathHandler, 0);
                HwLocationManagerService.this.mBinderLbsService = null;
            }
            if (System.currentTimeMillis() - HwLocationManagerService.this.mStartServiceTime > 600000) {
                HwLocationManagerService.this.mServiceRestartCount = 0;
            }
            HwLocationManagerService.this.sendLbsServiceRestartMessage();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLbsServiceRestartMessage() {
        long delayTime = 0;
        if (!this.mLbsServiceHandler.hasMessages(26)) {
            Message mTimeOut = Message.obtain(this.mLbsServiceHandler, 26);
            delayTime = new Double(Math.pow(2.0d, (double) this.mServiceRestartCount)).longValue() * SERVICE_RESTART_TIME_INTERVAL;
            if (delayTime > 0) {
                this.mLbsServiceHandler.sendMessageDelayed(mTimeOut, delayTime);
            } else {
                this.mLbsServiceHandler.sendMessage(mTimeOut);
            }
        }
        long j = this.mServiceRestartCount;
        if (j < 3) {
            this.mServiceRestartCount = j + 1;
        }
        LBSLog.i(TAG, false, "bindLbsService sendLbsServiceRestartMessage ,mServiceRestartCount: %{public}s, delayTime:%{public}d", Long.valueOf(this.mServiceRestartCount), Long.valueOf(delayTime));
    }

    /* access modifiers changed from: protected */
    public void hwSendBehavior(IHwBehaviorCollectManager.BehaviorId bid) {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
        }
    }

    public void hwRequestLocationUpdatesLocked(LocationRequest request, LocationManagerService.Receiver receiver, int pid, int uid, String packageName) {
        boolean isAwareControl;
        HwGnssDetectManager hwGnssDetectManager = HwGnssDetectManager.getInstance(this.mHwContext);
        hwGnssDetectManager.hwRequestLocationUpdatesLocked(request, receiver, pid, uid, packageName);
        hwGnssDetectManager.startGnssDetected(request, receiver, packageName);
        if (!this.mLocationHandler.hasMessages(7)) {
            this.mLocationHandler.sendEmptyMessageDelayed(7, 300000);
        }
        HwLocalLocationProvider hwLocalLocationProvider = this.mLocalLocationProvider;
        if (hwLocalLocationProvider != null) {
            hwLocalLocationProvider.requestLocation();
        } else {
            LBSLog.e(TAG, false, "LocalLocationDB has not init when start request", new Object[0]);
        }
        if (this.mSupervisoryPkgLists.containsKey(packageName)) {
            isAwareControl = true;
        } else {
            isAwareControl = false;
        }
        HwGpsLogServices.getInstance(this.mHwContext).requestStart(request, receiver, packageName, isAwareControl);
        String providerName = request.getProvider();
        HwGpsLogServices.getInstance(this.mHwContext).updateApkNetworkRequest(providerName, receiver, true);
        HwGpsLogServices.getInstance(this.mHwContext).updateApkName(providerName, Integer.toHexString(System.identityHashCode(receiver)), packageName, "apkstart", String.valueOf(System.currentTimeMillis()));
        recordRequestListener(1, packageName);
        if (this.mHwLbsConfigManager.isEnableForParam(LbsConfigContent.CONFIG_NLP_MEMORY_ENABLE)) {
            stopMemoryMonitor(providerName);
        }
        HwSystemManager.notifyBackgroundMgr(receiver.mCallerIdentity.mPackageName, receiver.mCallerIdentity.mPid, receiver.mCallerIdentity.mUid, 2, 1);
    }

    public ArrayList<String> gnssDetect(String packageName) {
        ArrayList<String> gnssDetect;
        if (!hasAccessHwLmsPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            LBSLog.i(TAG, false, "gnssDetect has no hw access_location_service permission!", new Object[0]);
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
        Iterator it = receiver.mUpdateRecords.values().iterator();
        while (it.hasNext()) {
            providers = providers + ((LocationManagerService.UpdateRecord) it.next()).mRealRequest.getProvider() + ",";
        }
        HwGpsLogServices.getInstance(this.mHwContext).requestStop(receiver, providers);
        HwGpsLogServices.getInstance(this.mHwContext).updateApkNetworkRequest(null, receiver, false);
        HwGpsLogServices.getInstance(this.mHwContext).updateApkName("APKSTOPPROVIDER", Integer.toHexString(System.identityHashCode(receiver)), "", "apkstop", String.valueOf(System.currentTimeMillis()));
        recordRequestListener(0, receiver.mCallerIdentity.mPackageName);
        LogPower.push((int) HwAPPQoEUtils.MSG_INTERNAL_CHR_EXCP_TRIGGER, receiver.mCallerIdentity.mPackageName, Integer.toString(receiver.mCallerIdentity.mPid), Integer.toString(-1), new String[]{Integer.toHexString(System.identityHashCode(receiver))});
        if (this.mHwLbsConfigManager.isEnableForParam(LbsConfigContent.CONFIG_NLP_MEMORY_ENABLE)) {
            startMemoryMonitor(receiver);
        }
        HwSystemManager.notifyBackgroundMgr(receiver.mCallerIdentity.mPackageName, receiver.mCallerIdentity.mPid, receiver.mCallerIdentity.mUid, 2, 0);
        HwGnssDetectManager.getInstance(this.mHwContext).cancelGnssSignalDetection(receiver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindLppeService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.mediatek.location.lppe.main", "com.mediatek.location.lppe.main.LPPeServiceWrapper"));
        LBSLog.i(TAG, false, "binding lppe service for MTK isBounded = %{public}b", Boolean.valueOf(this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1073741829, new UserHandle(0))));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindNlpService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.mediatek.nlpservice", "com.mediatek.nlpservice.NlpService"));
        LBSLog.i(TAG, false, "binding nlp service for MTK isBounded = %{public}b", Boolean.valueOf(this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1073741829, new UserHandle(0))));
    }

    public void initializeLockedEx() {
        LBSLog.i(TAG, false, "initializeLockedEx", new Object[0]);
        HwServiceFactory.getHwNLPManager().setLocationManagerService(this, this.mHwContext);
        HwServiceFactory.getHwNLPManager().setHwMultiNlpPolicy(this.mHwContext);
        if (HwLocalLocationProvider.isLocalDBEnabled()) {
            LocationManagerService.LocationProvider localLocationProviderManager = new LocationManagerService.LocationProvider(this, HwLocalLocationManager.LOCAL_PROVIDER);
            this.mLocalLocationProvider = HwLocalLocationProvider.getInstance(this.mHwContext, localLocationProviderManager);
            this.mLocalLocationProvider.enableLocalLocationProviders(localLocationProviderManager);
        } else {
            LBSLog.e(TAG, false, "LocalLocationDB is setted disable", new Object[0]);
        }
        GpsFreezeProc.getInstance().registerFreezeListener(new GpsFreezeListener() {
            /* class com.android.server.HwLocationManagerService.AnonymousClass5 */

            public void onFreezeProChange(String pkg) {
                if (!HwLocationManagerService.this.mLocationManagerServiceUtil.isProviderEnabledForUser("gps", HwLocationManagerService.this.mLocationManagerServiceUtil.getCurrentUserId())) {
                    LBSLog.i(HwLocationManagerService.TAG, false, "LocationManager.GPS_PROVIDER is not enable", new Object[0]);
                    return;
                }
                HwLocationManagerService.this.mLocationHandler.sendMessage(Message.obtain(HwLocationManagerService.this.mLocationHandler, 29, pkg));
            }

            public void onWhiteListChange(int type, List<String> pkgList) {
                HwLocationManagerService.this.mLocationHandler.sendMessage(Message.obtain(HwLocationManagerService.this.mLocationHandler, 30, type, 0, pkgList));
            }
        });
    }

    private void handleGpsFreezeChanged(Message msg) {
        String packageName = null;
        if (msg.obj != null && (msg.obj instanceof String)) {
            packageName = (String) msg.obj;
        }
        if (packageName != null) {
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                refreshProvider("gps", packageName);
                refreshProvider("network", packageName);
                refreshProvider("fused", packageName);
                refreshMeasurementsProvider(this.mLocationManagerServiceUtil.getGnssMeasurementsListeners(), this.mLocationManagerServiceUtil.getGnssMeasurementsProvider(), $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ.INSTANCE, packageName);
            }
        }
    }

    private void refreshProvider(String providerName, String packageName) {
        boolean isFound = false;
        ArrayList<LocationManagerService.UpdateRecord> records = this.mLocationManagerServiceUtil.getRecordsByProvider().get(providerName);
        if (records != null) {
            Iterator<LocationManagerService.UpdateRecord> it = records.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (Objects.equals(it.next().mReceiver.mCallerIdentity.mPackageName, packageName)) {
                        LBSLog.i(TAG, false, " GpsFreezeProc pkgname in %{public}s request:%{public}s", providerName, packageName);
                        isFound = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (isFound) {
            this.mLocationManagerServiceUtil.applyRequirementsLocked(providerName);
        }
    }

    private <TListener extends IInterface> void refreshMeasurementsProvider(ArrayMap<IBinder, ? extends LocationManagerService.LinkedListener> gnssDataListeners, RemoteListenerHelper<TListener> gnssDataProvider, Function<IBinder, TListener> mapBinderToListener, String packageName) {
        if (!(gnssDataListeners == null || gnssDataProvider == null)) {
            for (Map.Entry<IBinder, ? extends LocationManagerService.LinkedListener> entry : gnssDataListeners.entrySet()) {
                CallerIdentity callerIdentity = ((LocationManagerService.LinkedListener) entry.getValue()).mCallerIdentity;
                if (packageName != null && Objects.equals(packageName, callerIdentity.mPackageName)) {
                    boolean isFreeze = isFreeze(callerIdentity.mPackageName);
                    TListener listener = mapBinderToListener.apply(entry.getKey());
                    if (isFreeze) {
                        LBSLog.i(TAG, false, "GpsFreezeProc pkgname in %{public}s request:%{public}s", "GnssMeasurementsProvider", packageName);
                        gnssDataProvider.removeListener(listener);
                    } else {
                        LBSLog.i(TAG, false, "GpsUnFreezeProc pkgname in %{public}s request:%{public}s", "GnssMeasurementsProvider", packageName);
                        gnssDataProvider.addListener(listener, callerIdentity);
                    }
                }
            }
        }
    }

    private void updateFreezeList(Message msg) {
        List<String> pkgList = (List) msg.obj;
        int type = msg.arg1;
        if (this.mHwQuickTTFFMonitor instanceof HwQuickTTFFMonitor) {
            this.mHwQuickTTFFMonitor.updateWhiteList(type, pkgList);
        }
        if (type == 5) {
            synchronized (this.mLocationManagerServiceUtil.getmLock()) {
                this.mLocationManagerServiceUtil.onBackgroundThrottleWhitelistChangedLocked();
            }
        }
    }

    private boolean hasAccessHwLmsPermission(int pid, int uid) {
        if (this.mHwContext.checkPermission("huawei.permission.ACCESS_LOCATION_SERVICE", pid, uid) == 0) {
            return true;
        }
        if (!isBetaUser) {
            return false;
        }
        this.mLbsServiceHandler.sendMessage(Message.obtain(this.mLbsServiceHandler, 32, Integer.valueOf(uid)));
        return false;
    }

    private void hwLogPermissionDeny(Message msg) {
        if (msg.obj instanceof Integer) {
            int uid = ((Integer) msg.obj).intValue();
            String[] packageNames = this.mHwContext.getPackageManager().getPackagesForUid(uid);
            String data = "";
            if (packageNames != null) {
                String data2 = data;
                for (String packageName : packageNames) {
                    data2 = data2 + packageName + ",";
                }
                data = data2;
            } else {
                LBSLog.w(TAG, false, "hwLogPermissionDeny invalid uid: %{public}d", Integer.valueOf(uid));
            }
            IMonitor.EventStream assertStream = IMonitor.openEventStream((int) DFT_CHIP_ASSERT_EVENT);
            if (assertStream == null) {
                LBSLog.e(TAG, false, "assertStream is null", new Object[0]);
                return;
            }
            assertStream.setParam(0, 1001);
            assertStream.setParam(1, data);
            IMonitor.sendEvent(assertStream);
            IMonitor.closeEventStream(assertStream);
        }
    }

    /* access modifiers changed from: private */
    public class MemoryTimerTask extends TimerTask {
        private MemoryTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            LBSLog.i(HwLocationManagerService.TAG, false, "memory timer comes to start", new Object[0]);
            HwLocationManagerService.this.releaseAppMemory("com.huawei.lbs");
        }
    }

    private void startMemoryMonitor(LocationManagerService.Receiver receiver) {
        int networkSize;
        int gpsSize;
        synchronized (MEMORY_LOCK) {
            if (!this.isTimerRunning) {
                HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsMap = this.mLocationManagerServiceUtil.getRecordsByProvider();
                if (recordsMap != null) {
                    ArrayList<LocationManagerService.UpdateRecord> networkRecords = recordsMap.get("network");
                    ArrayList<LocationManagerService.UpdateRecord> gpsRecords = recordsMap.get("gps");
                    if (networkRecords != null) {
                        networkSize = networkRecords.size();
                    } else {
                        networkSize = 0;
                    }
                    if (gpsRecords != null) {
                        gpsSize = gpsRecords.size();
                    } else {
                        gpsSize = 0;
                    }
                    if (isExistRecords(networkSize, gpsSize, networkRecords, gpsRecords, receiver)) {
                        LBSLog.i(TAG, false, "start memory Timer after three minutes", new Object[0]);
                        startMemoryTimer(180000, 180000);
                    }
                }
            }
        }
    }

    private boolean isSameReceiver(LocationManagerService.Receiver receiverOne, LocationManagerService.Receiver receiverTwo) {
        if (receiverOne == null || receiverTwo == null || System.identityHashCode(receiverOne) != System.identityHashCode(receiverTwo)) {
            return false;
        }
        return true;
    }

    private boolean isExistRecords(int networkSize, int gpsSize, List<LocationManagerService.UpdateRecord> networkRecords, List<LocationManagerService.UpdateRecord> gpsRecords, LocationManagerService.Receiver receiver) {
        int recordSize = networkSize + gpsSize;
        LBSLog.i(TAG, false, "recordSize: %{public}d, networkSize = %{public}d, gpsSize = %{public}d", Integer.valueOf(recordSize), Integer.valueOf(networkSize), Integer.valueOf(gpsSize));
        boolean shouldStartTimer = false;
        if (recordSize > 1) {
            return false;
        }
        if (recordSize != 1) {
            return true;
        }
        if (networkSize == 1) {
            shouldStartTimer = isSameReceiver(networkRecords.get(0).mReceiver, receiver);
        }
        if (gpsSize == 1) {
            return isSameReceiver(gpsRecords.get(0).mReceiver, receiver);
        }
        return shouldStartTimer;
    }

    private void stopMemoryMonitor(String providerName) {
        synchronized (MEMORY_LOCK) {
            if (this.isTimerRunning) {
                if ("gps".equals(providerName) || "network".equals(providerName)) {
                    LBSLog.i(TAG, false, "stop memory timer, provider : %{public}s", providerName);
                    stopMemoryTimer();
                }
            }
        }
    }

    private void startMemoryTimer(long delay, long period) {
        this.mMemoryTimer = new Timer();
        this.mMemoryTimer.schedule(new MemoryTimerTask(), delay, period);
        this.isTimerRunning = true;
        if (this.mBinderLbsService == null) {
            LBSLog.i(TAG, false, "startMemoryTimer, need bind lbs service", new Object[0]);
            startLbsService();
            bindLbsService();
        }
    }

    private void stopMemoryTimer() {
        this.mMemoryTimer.cancel();
        this.isTimerRunning = false;
        this.mMemoryTimer = null;
    }

    private boolean isMockProviderExist() {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            Iterator<LocationManagerService.LocationProvider> it = this.mLocationManagerServiceUtil.getProviders().iterator();
            while (it.hasNext()) {
                if (it.next().isMock()) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAppMemory(String packageName) {
        List<ActivityManager.RunningAppProcessInfo> processInfos;
        LBSLog.i(TAG, false, "releaseAppMemory, package: %{public}s", packageName);
        if (packageName != null && !isMockProviderExist() && (processInfos = this.mHwActivityManager.getRunningAppProcesses()) != null && processInfos.size() > 0) {
            int processSize = processInfos.size();
            for (int i = 0; i < processSize; i++) {
                ActivityManager.RunningAppProcessInfo processInfo = processInfos.get(i);
                if (processInfo != null && Objects.equals(packageName, processInfo.processName)) {
                    LBSLog.i(TAG, false, "processName: %{public}s, pid = %{public}d, uid = %{public}d", processInfo.processName, Integer.valueOf(processInfo.pid), Integer.valueOf(processInfo.uid));
                    int pid = processInfo.pid;
                    if (this.mHwActivityManager.getProcessMemoryInfo(new int[]{pid})[0].getTotalPss() > MAX_MEMORY_LIMIT_SIZE) {
                        LBSLog.i(TAG, false, "hwlbs service memory over 19M, kill processs", new Object[0]);
                        if (getDoingBusiness() == 0) {
                            Process.killProcess(pid);
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    private int getDoingBusiness() {
        int doingBusiness = 0;
        if (this.mBinderLbsService == null) {
            return 0;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(LBS_DESCRIPTOR);
            this.mBinderLbsService.transact(47, data, reply, 0);
            reply.readException();
            doingBusiness = reply.readInt();
            LBSLog.i(TAG, false, "hwlbs service memory over 19M, doingBusiness=" + doingBusiness, new Object[0]);
        } catch (RemoteException e) {
            LBSLog.e(TAG, "TRANSACTION_CHECK_EXIST_BUSINESS localRemoteException, error !");
        } catch (NullPointerException e2) {
            LBSLog.e(TAG, "NullPointerException, error !");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return doingBusiness;
    }

    public void notifyMockPosition(String type, String opPackageName) throws IllegalArgumentException {
        if (type == null || opPackageName == null) {
            throw new IllegalArgumentException("Illegal parameter");
        }
        char c = 65535;
        int hashCode = type.hashCode();
        if (hashCode != -934610812) {
            if (hashCode == 96417 && type.equals(START_MOCK)) {
                c = 0;
            }
        } else if (type.equals(REMOVE_MOCK)) {
            c = 1;
        }
        if (c == 0) {
            startMockPosition(type, opPackageName);
        } else if (c == 1) {
            stopMockStatustion();
        }
    }

    @GuardedBy({"mockLock"})
    private void stopMockStatustion() {
        if (this.mLocationHandler.hasMessages(35)) {
            this.endTime = System.currentTimeMillis();
            if (this.endTime - this.startTime < 1000) {
                this.mLocationHandler.removeMessages(35);
                return;
            }
        }
        cancelNotification();
    }

    private void cancelNotification() {
        if (!isMockProviderExist()) {
            cancelMockNotification();
        }
    }

    private void cancelMockNotification() {
        if (this.mBinderLbsService == null) {
            LBSLog.e(TAG, "mBinderLBSService is null");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        LBSLog.i(TAG, "cancel unused notification");
        try {
            data.writeInterfaceToken(LBS_DESCRIPTOR);
            this.mBinderLbsService.transact(51, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            LBSLog.e(TAG, "localRemoteException, error !");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    @GuardedBy({"mockLock"})
    private void startMockPosition(String type, String opPackageName) {
        if (this.mBinderLbsService != null) {
            handleMockPosition(type, opPackageName);
        } else if (!this.mLocationHandler.hasMessages(35)) {
            this.startTime = System.currentTimeMillis();
            startLbsService();
            bindLbsService();
            ArrayList<String> list = new ArrayList<>();
            list.add(type);
            list.add(opPackageName);
            this.mLocationHandler.sendMessageDelayed(Message.obtain(this.mLocationHandler, 35, list), 1000);
        }
    }

    private void handleMockPosition(String type, String opPackageName) {
        if (this.mBinderLbsService == null) {
            LBSLog.e(TAG, "mBinderLbsService is null");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(LBS_DESCRIPTOR);
            data.writeString(type);
            data.writeString(opPackageName);
            this.mBinderLbsService.transact(49, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            LBSLog.e(TAG, "localRemoteException, error !");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    private void removeMockByName(String name) {
        LocationManagerService.LocationProvider provider = this.mLocationManagerServiceUtil.getLocationProviderLocked(name);
        if (provider != null && provider.isMock()) {
            this.mLocationManagerServiceUtil.removeProviderLocked(provider);
            LocationManagerService.LocationProvider realProvider = null;
            Iterator<LocationManagerService.LocationProvider> it = this.mLocationManagerServiceUtil.getRealProviders().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                LocationManagerService.LocationProvider isMockProvider = it.next();
                if (name.equals(isMockProvider.getName())) {
                    realProvider = isMockProvider;
                    break;
                }
            }
            if (realProvider != null) {
                LBSLog.i(TAG, false, "recovery system %{public}s ability", realProvider.getName());
                this.mLocationManagerServiceUtil.addProviderLocked(realProvider);
            }
        }
    }

    private void removeMockTest(String type, String opPackageName) {
        synchronized (this.mLocationManagerServiceUtil.getmLock()) {
            long identity = Binder.clearCallingIdentity();
            try {
                ArrayList<LocationManagerService.LocationProvider> providers = this.mLocationManagerServiceUtil.getProviders();
                ArrayList<String> providerNames = new ArrayList<>();
                Iterator<LocationManagerService.LocationProvider> it = providers.iterator();
                while (it.hasNext()) {
                    LocationManagerService.LocationProvider provider = it.next();
                    if (provider.isMock()) {
                        providerNames.add(provider.getName());
                        LBSLog.i(TAG, false, "%{public}s is mock location", provider.getName());
                    }
                }
                Iterator<String> it2 = providerNames.iterator();
                while (it2.hasNext()) {
                    LBSLog.i(TAG, false, "remove mock %{public}s", providerNames);
                    removeMockByName(it2.next());
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void handleQuickLocation(int command) {
        if (this.mBinderLbsService == null) {
            LBSLog.e(TAG, false, "mBinder is null", new Object[0]);
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(LBS_DESCRIPTOR);
            data.writeInt(command);
            this.mBinderLbsService.transact(50, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "localRemoteException, error !", new Object[0]);
        } catch (NullPointerException e2) {
            LBSLog.e(TAG, false, "NullPointerException, error in handleQuickLocation!", new Object[0]);
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public boolean isRequestValid(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        return !this.mHwLocator.requestLocating(request, listener, packageName);
    }

    public boolean isRemoveValid(ILocationListener listener, PendingIntent intent, String packageName) {
        return !this.mHwLocator.finishLocating(listener, packageName);
    }

    public boolean isGetLastLocationValid(String info) {
        return !this.mHwLocator.isGetLastLocationValid(info);
    }

    public Location hwGetLastLocation(LocationRequest request) {
        return this.mHwLocator.getLastLocation(request);
    }

    private void quickTtffPowerTracker(String providerName) {
        if ("network".equals(providerName) || "gps".equals(providerName)) {
            boolean isNeedApply = false;
            ArrayList<LocationManagerService.UpdateRecord> records = this.mLocationManagerServiceUtil.getRecordsByProvider().get(providerName);
            if (records == null || records.isEmpty()) {
                isNeedApply = true;
            }
            if (isNeedApply) {
                this.mLocationManagerServiceUtil.applyRequirementsLocked(providerName);
            }
        }
    }

    private void recordRequestListener(int operationType, String packageName) {
        HashMap<Object, LocationManagerService.Receiver> receivers = this.mLocationManagerServiceUtil.getReceivers();
        if (receivers != null) {
            int size = receivers.size();
            if (operationType == 0) {
                size--;
            }
            Bundle bundle = new Bundle();
            bundle.putString("providerType", "LocationRequest");
            bundle.putInt("size", size);
            HwGpsLogServices.getInstance(this.mHwContext).recordGnssStatusStatistics(0, operationType, packageName, bundle);
        }
    }
}
