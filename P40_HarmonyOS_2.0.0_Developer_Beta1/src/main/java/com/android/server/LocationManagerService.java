package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.location.ActivityRecognitionHardware;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.location.Address;
import android.location.Criteria;
import android.location.GeocoderParams;
import android.location.Geofence;
import android.location.GnssMeasurementCorrections;
import android.location.IBatchedLocationCallback;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssNavigationMessageListener;
import android.location.IGnssStatusListener;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationListener;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationRequest;
import android.location.LocationTime;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocationManagerService;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.location.ActivityRecognitionProxy;
import com.android.server.location.CallerIdentity;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeofenceManager;
import com.android.server.location.GeofenceProxy;
import com.android.server.location.GnssBatchingProvider;
import com.android.server.location.GnssCapabilitiesProvider;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssMeasurementCorrectionsProvider;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.GnssNavigationMessageProvider;
import com.android.server.location.GnssStatusListenerHelper;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.IHwLocationProviderInterface;
import com.android.server.location.IHwQuickTTFFMonitor;
import com.android.server.location.LocationBlacklist;
import com.android.server.location.LocationFudger;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.MockProvider;
import com.android.server.location.PassiveProvider;
import com.android.server.location.RemoteListenerHelper;
import com.android.server.pm.DumpState;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public class LocationManagerService extends AbsLocationManagerService {
    private static final String ACCESS_LOCATION_EXTRA_COMMANDS = "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS";
    private static final String ADD_MOCK = "add";
    public static final boolean D = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final long DEFAULT_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final long DEFAULT_LAST_LOCATION_MAX_AGE_MS = 1200000;
    private static final LocationRequest DEFAULT_LOCATION_REQUEST = new LocationRequest();
    private static final int FOREGROUND_IMPORTANCE_CUTOFF = 125;
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    private static final int GNSS_SOURCE_SIZE = 2;
    private static final int GV_LOCATION = 32;
    private static final String HD_LOCATION = "HDLocation";
    private static final long HIGH_POWER_INTERVAL_MS = 300000;
    private static final String HMS_PACKAGENAME = "com.huawei.hwid";
    private static final String LBS_PACKAGENAME = "com.huawei.lbs";
    private static final int LONG_AXIS = 6378137;
    private static final int MAX_PROVIDER_SCHEDULING_JITTER_MS = 100;
    protected static final int MSG_CHECK_LOCATION = 7;
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static final String REMOVE_MOCK = "remove";
    private static final int RESOLUTION_LEVEL_COARSE = 1;
    private static final int RESOLUTION_LEVEL_FINE = 2;
    private static final int RESOLUTION_LEVEL_NONE = 0;
    private static final int RTK_LOCATION = 8;
    private static final String TAG = "LocationManagerService";
    private static final String WAKELOCK_KEY = "*location*";
    private static final String WATCH_HMS_PACKAGENAME = "com.huawei.hms";
    private boolean isChrFirstRequest = false;
    private boolean isNetworkChrAllowed = true;
    private ActivityManager mActivityManager;
    private AppOpsManager mAppOps;
    private final ArraySet<String> mBackgroundThrottlePackageWhitelist = new ArraySet<>();
    @GuardedBy({"mLock"})
    private int mBatterySaverMode;
    private LocationBlacklist mBlacklist;
    protected final Context mContext;
    private int mCurrentUserId = 0;
    private int[] mCurrentUserProfiles = {0};
    @GuardedBy({"mLock"})
    private String mExtraLocationControllerPackage;
    private boolean mExtraLocationControllerPackageEnabled;
    private GeocoderProxy mGeocodeProvider;
    private GeofenceManager mGeofenceManager;
    @GuardedBy({"mLock"})
    private IBatchedLocationCallback mGnssBatchingCallback;
    @GuardedBy({"mLock"})
    private LinkedListener<IBatchedLocationCallback> mGnssBatchingDeathCallback;
    @GuardedBy({"mLock"})
    private boolean mGnssBatchingInProgress = false;
    private GnssBatchingProvider mGnssBatchingProvider;
    private GnssCapabilitiesProvider mGnssCapabilitiesProvider;
    private GnssMeasurementCorrectionsProvider mGnssMeasurementCorrectionsProvider;
    @GuardedBy({"mLock"})
    private final ArrayMap<IBinder, LinkedListener<IGnssMeasurementsListener>> mGnssMeasurementsListeners = new ArrayMap<>();
    private GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssLocationProvider.GnssMetricsProvider mGnssMetricsProvider;
    @GuardedBy({"mLock"})
    private final ArrayMap<IBinder, LinkedListener<IGnssNavigationMessageListener>> mGnssNavigationMessageListeners = new ArrayMap<>();
    private GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    @GuardedBy({"mLock"})
    private final ArrayMap<IBinder, LinkedListener<IGnssStatusListener>> mGnssStatusListeners = new ArrayMap<>();
    private GnssStatusListenerHelper mGnssStatusProvider;
    private GnssLocationProvider.GnssSystemInfoProvider mGnssSystemInfoProvider;
    private IGpsGeofenceHardware mGpsGeofenceProxy;
    private final Handler mHandler;
    private IHwGpsLogServices mHwLocationGpsLogServices;
    protected IHwQuickTTFFMonitor mHwQuickTTFFMonitor;
    private final ArraySet<String> mIgnoreSettingsPackageWhitelist = new ArraySet<>();
    private int[] mLastGnssSourceType = new int[2];
    @GuardedBy({"mLock"})
    private final HashMap<String, Location> mLastLocation = new HashMap<>();
    @GuardedBy({"mLock"})
    private final HashMap<String, Location> mLastLocationCoarseInterval = new HashMap<>();
    private LocationFudger mLocationFudger;
    @GuardedBy({"mLock"})
    private final LocationUsageLogger mLocationUsageLogger;
    private final Object mLock = new Object();
    private INetInitiatedListener mNetInitiatedListener;
    private PackageManager mPackageManager;
    private PassiveProvider mPassiveProvider;
    private PowerManager mPowerManager;
    @GuardedBy({"mLock"})
    private final ArrayList<LocationProvider> mProviders = new ArrayList<>();
    @GuardedBy({"mLock"})
    private final ArrayList<LocationProvider> mRealProviders = new ArrayList<>();
    @GuardedBy({"mLock"})
    private final HashMap<Object, Receiver> mReceivers = new HashMap<>();
    private final HashMap<String, ArrayList<UpdateRecord>> mRecordsByProvider = new HashMap<>();
    private final LocationRequestStatistics mRequestStatistics = new LocationRequestStatistics();
    private UserManager mUserManager;

    public LocationManagerService(Context context) {
        this.mContext = context;
        this.mHandler = FgThread.getHandler();
        this.mLocationUsageLogger = new LocationUsageLogger();
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        packageManagerInternal.setLocationPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.$$Lambda$LocationManagerService$bojY6dMaI07zh6_sF7ERxgmk6U0 */

            public final String[] getPackages(int i) {
                return LocationManagerService.this.lambda$new$0$LocationManagerService(i);
            }
        });
        packageManagerInternal.setLocationExtraPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.$$Lambda$LocationManagerService$pUnNobtfzLC9eAlVqCMKySwbo3U */

            public final String[] getPackages(int i) {
                return LocationManagerService.this.lambda$new$1$LocationManagerService(i);
            }
        });
    }

    public /* synthetic */ String[] lambda$new$0$LocationManagerService(int userId) {
        return this.mContext.getResources().getStringArray(17236033);
    }

    public /* synthetic */ String[] lambda$new$1$LocationManagerService(int userId) {
        return this.mContext.getResources().getStringArray(17236032);
    }

    public void systemRunning() {
        synchronized (this.mLock) {
            initializeLocked();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v19, resolved type: android.app.AppOpsManager */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v8, types: [android.app.AppOpsManager$OnOpChangedListener, com.android.server.LocationManagerService$1] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @GuardedBy({"mLock"})
    private void initializeLocked() {
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mLocationFudger = new LocationFudger(this.mContext, this.mHandler);
        this.mBlacklist = new LocationBlacklist(this.mContext, this.mHandler);
        this.mBlacklist.init();
        this.mGeofenceManager = new GeofenceManager(this.mContext, this.mBlacklist);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.$$Lambda$LocationManagerService$GJjItJofmJkJhbftqezuIe8Sio */

            @Override // java.lang.Runnable
            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$2$LocationManagerService();
            }
        });
        this.mAppOps.startWatchingMode(0, (String) null, 1, (AppOpsManager.OnOpChangedListener) new AppOpsManager.OnOpChangedInternalListener() {
            /* class com.android.server.LocationManagerService.AnonymousClass1 */

            public void onOpChanged(int op, String packageName) {
                LocationManagerService.this.mHandler.post(new Runnable(packageName, op) {
                    /* class com.android.server.$$Lambda$LocationManagerService$1$TAxZBrK0SZGyFmeUET64QcBr0 */
                    private final /* synthetic */ String f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LocationManagerService.AnonymousClass1.this.lambda$onOpChanged$0$LocationManagerService$1(this.f$1, this.f$2);
                    }
                });
            }

            public /* synthetic */ void lambda$onOpChanged$0$LocationManagerService$1(String packageName, int op) {
                synchronized (LocationManagerService.this.mLock) {
                    if (LocationManagerService.this.isExistReceiver(packageName)) {
                        if (LocationManagerService.D) {
                            Log.i(LocationManagerService.TAG, "onOpChanged:" + op + " " + packageName);
                        }
                        LocationManagerService.this.onAppOpChangedLocked();
                    }
                }
            }
        });
        this.mPackageManager.addOnPermissionsChangeListener(new PackageManager.OnPermissionsChangedListener() {
            /* class com.android.server.$$Lambda$LocationManagerService$394dHabjqiGM877pfWeRxjuhtdk */

            public final void onPermissionsChanged(int i) {
                LocationManagerService.this.lambda$initializeLocked$4$LocationManagerService(i);
            }
        });
        initHwLocationPowerTracker(this.mContext);
        this.mActivityManager.addOnUidImportanceListener(new ActivityManager.OnUidImportanceListener() {
            /* class com.android.server.$$Lambda$LocationManagerService$JwBsRPZNHn1yeW1Qoy5YdGf5E */

            public final void onUidImportance(int i, int i2) {
                LocationManagerService.this.lambda$initializeLocked$6$LocationManagerService(i, i2);
            }
        }, 125);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_mode"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                if (LocationManagerService.this.isGPSDisabled()) {
                    Log.i(LocationManagerService.TAG, "gps is disabled by dpm .");
                }
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onLocationModeChangedLocked(true);
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                if (LocationManagerService.this.isGPSDisabled()) {
                    Log.i(LocationManagerService.TAG, "gps is disabled by dpm .");
                }
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onProviderAllowedChangedLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_background_throttle_interval_ms"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onBackgroundThrottleIntervalChangedLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_background_throttle_package_whitelist"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onBackgroundThrottleWhitelistChangedLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_ignore_settings_package_whitelist"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass6 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onIgnoreSettingsWhitelistChangedLocked();
                }
            }
        }, -1);
        ((PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class)).registerLowPowerModeObserver(1, new Consumer() {
            /* class com.android.server.$$Lambda$LocationManagerService$mWgljPRZw0wUOQGM0Gf72CH4Zo */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.lambda$initializeLocked$8$LocationManagerService((PowerSaveState) obj);
            }
        });
        new PackageMonitor() {
            /* class com.android.server.LocationManagerService.AnonymousClass7 */

            public void onPackageDisappeared(String packageName, int reason) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onPackageDisappearedLocked(packageName);
                }
            }
        }.register(this.mContext, this.mHandler.getLooper(), true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.LocationManagerService.AnonymousClass8 */

            /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                char c;
                UserInfo ui;
                String action = intent.getAction();
                if (action != null) {
                    synchronized (LocationManagerService.this.mLock) {
                        switch (action.hashCode()) {
                            case -2128145023:
                                if (action.equals("android.intent.action.SCREEN_OFF")) {
                                    c = 4;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -2061058799:
                                if (action.equals("android.intent.action.USER_REMOVED")) {
                                    c = 6;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -1454123155:
                                if (action.equals("android.intent.action.SCREEN_ON")) {
                                    c = 3;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -385593787:
                                if (action.equals("android.intent.action.MANAGED_PROFILE_ADDED")) {
                                    c = 1;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 959232034:
                                if (action.equals("android.intent.action.USER_SWITCHED")) {
                                    c = 0;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1051477093:
                                if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                                    c = 2;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 1121780209:
                                if (action.equals("android.intent.action.USER_ADDED")) {
                                    c = 5;
                                    break;
                                }
                                c = 65535;
                                break;
                            default:
                                c = 65535;
                                break;
                        }
                        switch (c) {
                            case 0:
                                LocationManagerService.this.onUserChangedLocked(intent.getIntExtra("android.intent.extra.user_handle", 0));
                                break;
                            case 1:
                            case 2:
                                LocationManagerService.this.onUserProfilesChangedLocked();
                                break;
                            case 3:
                            case 4:
                                LocationManagerService.this.onScreenStateChangedLocked();
                                break;
                            case 5:
                            case 6:
                                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                                if (!(userId == LocationManagerService.this.mCurrentUserId || (ui = LocationManagerService.this.mUserManager.getUserInfo(userId)) == null || ui.profileGroupId != LocationManagerService.this.mCurrentUserId)) {
                                    LocationManagerService.this.onUserProfilesChangedLocked();
                                    Log.i(LocationManagerService.TAG, "onReceive action:" + action + ", userId:" + userId + ",updateUserProfiles for currentUserId:" + LocationManagerService.this.mCurrentUserId);
                                    break;
                                }
                        }
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
        this.mCurrentUserId = -10000;
        onUserChangedLocked(0);
        onBackgroundThrottleWhitelistChangedLocked();
        initializeLockedEx();
        onIgnoreSettingsWhitelistChangedLocked();
        onBatterySaverModeChangedLocked(this.mPowerManager.getLocationPowerSaveMode());
        this.mHwLocationGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
    }

    public /* synthetic */ void lambda$initializeLocked$4$LocationManagerService(int uid) {
        this.mHandler.post(new Runnable(uid) {
            /* class com.android.server.$$Lambda$LocationManagerService$MboxQRAM1yLY_Jyc2z9mQCBMi90 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$3$LocationManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$initializeLocked$3$LocationManagerService(int uid) {
        synchronized (this.mLock) {
            if (isExistReceiver(uid)) {
                if (D) {
                    Log.i(TAG, "onPermissionsChanged: uid=" + uid);
                }
                onPermissionsChangedLocked();
            }
        }
    }

    public /* synthetic */ void lambda$initializeLocked$6$LocationManagerService(int uid, int importance) {
        this.mHandler.post(new Runnable(uid, importance) {
            /* class com.android.server.$$Lambda$LocationManagerService$kluQn2t9LQW7tDVpEZEO7j5_Bgg */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$5$LocationManagerService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$initializeLocked$5$LocationManagerService(int uid, int importance) {
        synchronized (this.mLock) {
            onUidImportanceChangedLocked(uid, importance);
        }
    }

    public /* synthetic */ void lambda$initializeLocked$8$LocationManagerService(PowerSaveState state) {
        this.mHandler.post(new Runnable(state) {
            /* class com.android.server.$$Lambda$LocationManagerService$U9wvQVNZMkW64361zDE6lAO4loU */
            private final /* synthetic */ PowerSaveState f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$7$LocationManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$initializeLocked$7$LocationManagerService(PowerSaveState state) {
        synchronized (this.mLock) {
            onBatterySaverModeChangedLocked(state.locationMode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onAppOpChangedLocked() {
        for (Receiver receiver : this.mReceivers.values()) {
            receiver.updateMonitoring(true);
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    @GuardedBy({"mLock"})
    private void onPermissionsChangedLocked() {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    @GuardedBy({"mLock"})
    private void onBatterySaverModeChangedLocked(int newLocationMode) {
        if (D) {
            Slog.d(TAG, "Battery Saver location mode changed from " + PowerManager.locationPowerSaveModeToString(this.mBatterySaverMode) + " to " + PowerManager.locationPowerSaveModeToString(newLocationMode));
        }
        if (this.mBatterySaverMode != newLocationMode) {
            this.mBatterySaverMode = newLocationMode;
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                applyRequirementsLocked(it.next());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onScreenStateChangedLocked() {
        if (this.mBatterySaverMode == 4) {
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                applyRequirementsLocked(it.next());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onLocationModeChangedLocked(boolean broadcast) {
        if (D) {
            Log.d(TAG, "location enabled is now " + isLocationEnabled());
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            it.next().onLocationModeChangedLocked();
        }
        if (broadcast) {
            Intent intent = new Intent("android.location.MODE_CHANGED");
            intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onProviderAllowedChangedLocked() {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            it.next().onAllowedChangedLocked();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onPackageDisappearedLocked(String packageName) {
        ArrayList<Receiver> deadReceivers = null;
        for (Receiver receiver : this.mReceivers.values()) {
            if (receiver.mCallerIdentity.mPackageName.equals(packageName)) {
                if (deadReceivers == null) {
                    deadReceivers = new ArrayList<>();
                }
                deadReceivers.add(receiver);
            }
        }
        if (deadReceivers != null) {
            Iterator<Receiver> it = deadReceivers.iterator();
            while (it.hasNext()) {
                removeUpdatesLocked(it.next());
            }
        }
    }

    @GuardedBy({"mLock"})
    private void onUidImportanceChangedLocked(int uid, int importance) {
        boolean foreground = isImportanceForeground(importance);
        HashSet<String> affectedProviders = new HashSet<>(this.mRecordsByProvider.size());
        for (Map.Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
            String provider = entry.getKey();
            Iterator<UpdateRecord> it = entry.getValue().iterator();
            while (it.hasNext()) {
                UpdateRecord record = it.next();
                if (record.mReceiver.mCallerIdentity.mUid == uid && record.mIsForegroundUid != foreground) {
                    if (D) {
                        Log.i(TAG, "request from uid " + uid + " is now " + foregroundAsString(foreground));
                    }
                    record.updateForeground(foreground);
                    if (!isThrottlingExemptLocked(record.mReceiver.mCallerIdentity)) {
                        affectedProviders.add(provider);
                    }
                }
            }
        }
        Iterator<String> it2 = affectedProviders.iterator();
        while (it2.hasNext()) {
            applyRequirementsLocked(it2.next());
        }
        updateGnssDataProviderOnUidImportanceChangedLocked(this.mGnssMeasurementsListeners, this.mGnssMeasurementsProvider, $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ.INSTANCE, uid, foreground);
        updateGnssDataProviderOnUidImportanceChangedLocked(this.mGnssNavigationMessageListeners, this.mGnssNavigationMessageProvider, $$Lambda$HALkbmbB2IPr_wdFkPjiIWCzJsY.INSTANCE, uid, foreground);
        updateGnssDataProviderOnUidImportanceChangedLocked(this.mGnssStatusListeners, this.mGnssStatusProvider, $$Lambda$hu4394T6QBT8QyZnspMtXqICWs.INSTANCE, uid, foreground);
    }

    @GuardedBy({"mLock"})
    private <TListener extends IInterface> void updateGnssDataProviderOnUidImportanceChangedLocked(ArrayMap<IBinder, ? extends LinkedListenerBase> gnssDataListeners, RemoteListenerHelper<TListener> gnssDataProvider, Function<IBinder, TListener> mapBinderToListener, int uid, boolean foreground) {
        for (Map.Entry<IBinder, ? extends LinkedListenerBase> entry : gnssDataListeners.entrySet()) {
            LinkedListenerBase linkedListener = (LinkedListenerBase) entry.getValue();
            CallerIdentity callerIdentity = linkedListener.mCallerIdentity;
            if (callerIdentity.mUid == uid) {
                if (D) {
                    Log.d(TAG, linkedListener.mListenerName + " from uid " + uid + " is now " + foregroundAsString(foreground));
                }
                TListener listener = mapBinderToListener.apply(entry.getKey());
                if (foreground || isThrottlingExemptLocked(callerIdentity)) {
                    gnssDataProvider.addListener(listener, callerIdentity);
                } else {
                    gnssDataProvider.removeListener(listener);
                }
            }
        }
    }

    private static String foregroundAsString(boolean foreground) {
        return foreground ? "foreground" : "background";
    }

    /* access modifiers changed from: private */
    public static boolean isImportanceForeground(int importance) {
        return importance <= 125;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onBackgroundThrottleIntervalChangedLocked() {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onBackgroundThrottleWhitelistChangedLocked() {
        this.mBackgroundThrottlePackageWhitelist.clear();
        this.mBackgroundThrottlePackageWhitelist.addAll(SystemConfig.getInstance().getAllowUnthrottledLocation());
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), "location_background_throttle_package_whitelist");
        if (!TextUtils.isEmpty(setting)) {
            this.mBackgroundThrottlePackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        }
        this.mBackgroundThrottlePackageWhitelist.addAll((ArraySet<? extends String>) HwServiceFactory.getGpsFreezeProc().getPackageWhiteList(1));
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"lock"})
    private void onIgnoreSettingsWhitelistChangedLocked() {
        this.mIgnoreSettingsPackageWhitelist.clear();
        this.mIgnoreSettingsPackageWhitelist.addAll(SystemConfig.getInstance().getAllowIgnoreLocationSettings());
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), "location_ignore_settings_package_whitelist");
        if (!TextUtils.isEmpty(setting)) {
            this.mIgnoreSettingsPackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onUserProfilesChangedLocked() {
        this.mCurrentUserProfiles = this.mUserManager.getProfileIdsWithDisabled(this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private boolean isCurrentProfileLocked(int userId) {
        return ArrayUtils.contains(this.mCurrentUserProfiles, userId);
    }

    @GuardedBy({"mLock"})
    private void ensureFallbackFusedProviderPresentLocked(String[] pkgs) {
        PackageManager pm = this.mContext.getPackageManager();
        String systemPackageName = this.mContext.getPackageName();
        ArrayList<HashSet<Signature>> sigSets = ServiceWatcher.getSignatureSets(this.mContext, pkgs);
        for (ResolveInfo rInfo : pm.queryIntentServicesAsUser(new Intent(FUSED_LOCATION_SERVICE_ACTION), 128, this.mCurrentUserId)) {
            String packageName = rInfo.serviceInfo.packageName;
            try {
                if (!ServiceWatcher.isSignatureMatch(pm.getPackageInfo(packageName, 64).signatures, sigSets)) {
                    Log.w(TAG, packageName + " resolves service " + FUSED_LOCATION_SERVICE_ACTION + ", but has wrong signature, ignoring");
                } else if (rInfo.serviceInfo.metaData == null) {
                    Log.w(TAG, "Found fused provider without metadata: " + packageName);
                } else if (rInfo.serviceInfo.metaData.getInt(ServiceWatcher.EXTRA_SERVICE_VERSION, -1) == 0) {
                    if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                        if (D) {
                            Log.d(TAG, "Fallback candidate not in /system: " + packageName);
                        }
                    } else if (pm.checkSignatures(systemPackageName, packageName) != 0) {
                        if (D) {
                            Log.d(TAG, "Fallback candidate not signed the same as system: " + packageName);
                        }
                    } else if (D) {
                        Log.d(TAG, "Found fallback provider: " + packageName);
                        return;
                    } else {
                        return;
                    }
                } else if (D) {
                    Log.d(TAG, "Fallback candidate not version 0: " + packageName);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "missing package: " + packageName);
            }
        }
        throw new IllegalStateException("Unable to find a fused location provider that is in the system partition with version 0 and signed with the platform certificate. Such a package is needed to provide a default fused location provider in the event that no other fused location provider has been installed or is currently available. For example, coreOnly boot mode when decrypting the data partition. The fallback must also be marked coreApp=\"true\" in the manifest");
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    /* renamed from: initializeProvidersLocked */
    public void lambda$initializeLocked$2$LocationManagerService() {
        ActivityRecognitionHardware activityRecognitionHardware;
        LocationManagerService locationManagerService = this;
        LocationProvider passiveProviderManager = new LocationProvider(locationManagerService, "passive");
        locationManagerService.addProviderLocked(passiveProviderManager);
        locationManagerService.mPassiveProvider = new PassiveProvider(locationManagerService.mContext, passiveProviderManager);
        passiveProviderManager.attachLocked(locationManagerService.mPassiveProvider);
        if (GnssLocationProvider.isSupported()) {
            LocationProvider gnssProviderManager = new LocationProvider("gps", true);
            locationManagerService.mRealProviders.add(gnssProviderManager);
            locationManagerService.addProviderLocked(gnssProviderManager);
            GnssLocationProvider gnssProvider = new GnssLocationProvider(locationManagerService.mContext, gnssProviderManager, locationManagerService.mHandler.getLooper());
            gnssProviderManager.attachLocked(gnssProvider);
            locationManagerService.mGnssSystemInfoProvider = gnssProvider.getGnssSystemInfoProvider();
            locationManagerService.mGnssBatchingProvider = gnssProvider.getGnssBatchingProvider();
            locationManagerService.mGnssMetricsProvider = gnssProvider.getGnssMetricsProvider();
            locationManagerService.mGnssCapabilitiesProvider = gnssProvider.getGnssCapabilitiesProvider();
            locationManagerService.mGnssStatusProvider = gnssProvider.getGnssStatusProvider();
            locationManagerService.mNetInitiatedListener = gnssProvider.getNetInitiatedListener();
            locationManagerService.mGnssMeasurementsProvider = gnssProvider.getGnssMeasurementsProvider();
            locationManagerService.mGnssMeasurementCorrectionsProvider = gnssProvider.getGnssMeasurementCorrectionsProvider();
            locationManagerService.mGnssNavigationMessageProvider = gnssProvider.getGnssNavigationMessageProvider();
            locationManagerService.mGpsGeofenceProxy = gnssProvider.getGpsGeofenceProxy();
            locationManagerService.mHwQuickTTFFMonitor = HwServiceFactory.getHwQuickTTFFMonitor(locationManagerService.mContext, gnssProvider);
        } else {
            locationManagerService.mHwQuickTTFFMonitor = HwServiceFactory.getHwQuickTTFFMonitor(locationManagerService.mContext, null);
        }
        Resources resources = locationManagerService.mContext.getResources();
        String[] pkgs = resources.getStringArray(17236033);
        if (D) {
            Log.d(TAG, "certificates for location providers pulled from: " + Arrays.toString(pkgs));
        }
        locationManagerService.ensureFallbackFusedProviderPresentLocked(pkgs);
        LocationProvider networkProviderManager = new LocationProvider("network", true);
        LocationProviderProxy networkProvider = HwServiceFactory.locationProviderProxyCreateAndBind(locationManagerService.mContext, networkProviderManager, NETWORK_LOCATION_SERVICE_ACTION, 17891446, 17039869, 17236033);
        if (networkProvider != null) {
            locationManagerService.mRealProviders.add(networkProviderManager);
            locationManagerService.addProviderLocked(networkProviderManager);
            networkProviderManager.attachLocked(networkProvider);
        } else {
            Slog.w(TAG, "no network location provider found");
        }
        LocationProvider fusedProviderManager = new LocationProvider(locationManagerService, "fused");
        LocationProviderProxy fusedProvider = LocationProviderProxy.createAndBind(locationManagerService.mContext, fusedProviderManager, FUSED_LOCATION_SERVICE_ACTION, 17891438, 17039844, 17236033);
        if (fusedProvider != null) {
            locationManagerService.mRealProviders.add(fusedProviderManager);
            locationManagerService.addProviderLocked(fusedProviderManager);
            fusedProviderManager.attachLocked(fusedProvider);
        } else {
            Slog.e(TAG, "no fused location provider found", new IllegalStateException("Location service needs a fused location provider"));
        }
        locationManagerService.mGeocodeProvider = HwServiceFactory.geocoderProxyCreateAndBind(locationManagerService.mContext, 17891439, 17039845, 17236033);
        if (locationManagerService.mGeocodeProvider == null) {
            Slog.e(TAG, "no geocoder provider found");
        }
        if (GeofenceProxy.createAndBind(locationManagerService.mContext, 17891440, 17039846, 17236033, locationManagerService.mGpsGeofenceProxy, null) == null) {
            Slog.d(TAG, "Unable to bind FLP Geofence proxy.");
        }
        boolean activityRecognitionHardwareIsSupported = ActivityRecognitionHardware.isSupported();
        if (activityRecognitionHardwareIsSupported) {
            activityRecognitionHardware = ActivityRecognitionHardware.getInstance(locationManagerService.mContext);
        } else {
            Slog.d(TAG, "Hardware Activity-Recognition not supported.");
            activityRecognitionHardware = null;
        }
        if (ActivityRecognitionProxy.createAndBind(locationManagerService.mContext, activityRecognitionHardwareIsSupported, activityRecognitionHardware, 17891432, 17039781, 17236033) == null) {
            Slog.d(TAG, "Unable to bind ActivityRecognitionProxy.");
        }
        String[] testProviderStrings = resources.getStringArray(17236067);
        int length = testProviderStrings.length;
        int i = 0;
        while (i < length) {
            String[] fragments = testProviderStrings[i].split(",");
            String name = fragments[0].trim();
            ProviderProperties properties = new ProviderProperties(Boolean.parseBoolean(fragments[1]), Boolean.parseBoolean(fragments[2]), Boolean.parseBoolean(fragments[3]), Boolean.parseBoolean(fragments[4]), Boolean.parseBoolean(fragments[5]), Boolean.parseBoolean(fragments[6]), Boolean.parseBoolean(fragments[7]), Integer.parseInt(fragments[8]), Integer.parseInt(fragments[9]));
            LocationProvider testProviderManager = new LocationProvider(locationManagerService, name);
            locationManagerService.addProviderLocked(testProviderManager);
            new MockProvider(locationManagerService.mContext, testProviderManager, properties);
            i++;
            locationManagerService = this;
            resources = resources;
            passiveProviderManager = passiveProviderManager;
            networkProviderManager = networkProviderManager;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onUserChangedLocked(int userId) {
        if (this.mCurrentUserId != userId) {
            this.mCurrentUserId = userId;
            if (D) {
                Log.d(TAG, "foreground user is changing to " + userId);
            }
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                it.next().onUserChangingLocked();
            }
            onUserProfilesChangedLocked();
            this.mBlacklist.switchUser(userId);
            onLocationModeChangedLocked(false);
            onProviderAllowedChangedLocked();
            Iterator<LocationProvider> it2 = this.mProviders.iterator();
            while (it2.hasNext()) {
                it2.next().onUseableChangedLocked(false);
            }
        }
    }

    public class LocationProvider implements AbstractLocationProvider.LocationProviderManager {
        @GuardedBy({"mLock"})
        private boolean mAllowed;
        @GuardedBy({"mLock"})
        private boolean mEnabled;
        private final boolean mIsManagedBySettings;
        private final String mName;
        @GuardedBy({"mLock"})
        private ProviderProperties mProperties;
        @GuardedBy({"mLock"})
        protected AbstractLocationProvider mProvider;
        @GuardedBy({"mLock"})
        private boolean mUseable;

        public LocationProvider(LocationManagerService this$02, String name) {
            this(name, false);
        }

        private LocationProvider(String name, boolean isManagedBySettings) {
            this.mName = name;
            this.mIsManagedBySettings = isManagedBySettings;
            this.mProvider = null;
            this.mUseable = false;
            boolean z = this.mIsManagedBySettings;
            this.mAllowed = !z;
            this.mEnabled = false;
            this.mProperties = null;
            if (z) {
                ContentResolver contentResolver = LocationManagerService.this.mContext.getContentResolver();
                Settings.Secure.putStringForUser(contentResolver, "location_providers_allowed", "-" + this.mName, LocationManagerService.this.mCurrentUserId);
            }
        }

        @GuardedBy({"mLock"})
        public void attachLocked(AbstractLocationProvider provider) {
            Preconditions.checkNotNull(provider);
            Preconditions.checkState(this.mProvider == null);
            if (LocationManagerService.D) {
                Log.d(LocationManagerService.TAG, this.mName + " provider attached");
            }
            this.mProvider = provider;
            onUseableChangedLocked(false);
        }

        public String getName() {
            return this.mName;
        }

        @GuardedBy({"mLock"})
        public List<String> getPackagesLocked() {
            AbstractLocationProvider abstractLocationProvider = this.mProvider;
            if (abstractLocationProvider == null) {
                return Collections.emptyList();
            }
            return abstractLocationProvider.getProviderPackages();
        }

        public boolean isMock() {
            return false;
        }

        @GuardedBy({"mLock"})
        public boolean isPassiveLocked() {
            return this.mProvider == LocationManagerService.this.mPassiveProvider;
        }

        @GuardedBy({"mLock"})
        public ProviderProperties getPropertiesLocked() {
            return this.mProperties;
        }

        @GuardedBy({"mLock"})
        public void setRequestLocked(ProviderRequest request, WorkSource workSource) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    this.mProvider.setRequest(request, workSource);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @GuardedBy({"mLock"})
        public void dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.print("  " + this.mName + " provider");
            if (isMock()) {
                pw.print(" [mock]");
            }
            pw.println(":");
            pw.println("    useable=" + this.mUseable);
            if (!this.mUseable) {
                StringBuilder sb = new StringBuilder();
                sb.append("    attached=");
                sb.append(this.mProvider != null);
                pw.println(sb.toString());
                if (this.mIsManagedBySettings) {
                    pw.println("    allowed=" + this.mAllowed);
                }
                pw.println("    enabled=" + this.mEnabled);
            }
            pw.println("    properties=" + this.mProperties);
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    this.mProvider.dump(fd, pw, args);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @GuardedBy({"mLock"})
        public long getStatusUpdateTimeLocked() {
            if (this.mProvider == null) {
                return 0;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return this.mProvider.getStatusUpdateTime();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @GuardedBy({"mLock"})
        public int getStatusLocked(Bundle extras) {
            if (this.mProvider == null) {
                return 2;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return this.mProvider.getStatus(extras);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @GuardedBy({"mLock"})
        public void sendExtraCommandLocked(String command, Bundle extras) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    this.mProvider.sendExtraCommand(command, extras);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onReportLocation(Location location) {
            LocationManagerService.this.mHandler.post(new Runnable(location) {
                /* class com.android.server.$$Lambda$LocationManagerService$LocationProvider$R123rmQLJrCf8yBSKrQD6XPhpZs */
                private final /* synthetic */ Location f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    LocationManagerService.LocationProvider.this.lambda$onReportLocation$0$LocationManagerService$LocationProvider(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onReportLocation$0$LocationManagerService$LocationProvider(Location location) {
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.handleLocationChangedLocked(location, this);
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onReportLocation(List<Location> locations) {
            LocationManagerService.this.mHandler.post(new Runnable(locations) {
                /* class com.android.server.$$Lambda$LocationManagerService$LocationProvider$UwV519Q998DTiPhy1rbdXyO3Geo */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    LocationManagerService.LocationProvider.this.lambda$onReportLocation$1$LocationManagerService$LocationProvider(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onReportLocation$1$LocationManagerService$LocationProvider(List locations) {
            synchronized (LocationManagerService.this.mLock) {
                LocationProvider gpsProvider = LocationManagerService.this.getLocationProviderLocked("gps");
                if (gpsProvider != null) {
                    if (gpsProvider.isUseableLocked()) {
                        if (LocationManagerService.this.mGnssBatchingCallback == null) {
                            Slog.e(LocationManagerService.TAG, "reportLocationBatch() called without active Callback");
                            return;
                        }
                        try {
                            LocationManagerService.this.mGnssBatchingCallback.onLocationBatch(locations);
                        } catch (RemoteException e) {
                            Slog.e(LocationManagerService.TAG, "mGnssBatchingCallback.onLocationBatch failed", e);
                        }
                        return;
                    }
                }
                Slog.w(LocationManagerService.TAG, "reportLocationBatch() called without user permission");
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onSetEnabled(boolean enabled) {
            LocationManagerService.this.mHandler.post(new Runnable(enabled) {
                /* class com.android.server.$$Lambda$LocationManagerService$LocationProvider$nsL4uwojBLPzs1TzMfpQIBSm7p0 */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    LocationManagerService.LocationProvider.this.lambda$onSetEnabled$2$LocationManagerService$LocationProvider(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onSetEnabled$2$LocationManagerService$LocationProvider(boolean enabled) {
            synchronized (LocationManagerService.this.mLock) {
                if (enabled != this.mEnabled) {
                    if (LocationManagerService.D) {
                        Log.d(LocationManagerService.TAG, this.mName + " provider enabled is now " + this.mEnabled);
                    }
                    this.mEnabled = enabled;
                    onUseableChangedLocked(false);
                }
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onSetProperties(ProviderProperties properties) {
            synchronized (LocationManagerService.this.mLock) {
                this.mProperties = properties;
            }
        }

        @GuardedBy({"mLock"})
        public void onLocationModeChangedLocked() {
            onUseableChangedLocked(false);
        }

        @GuardedBy({"mLock"})
        public void onAllowedChangedLocked() {
            boolean allowed;
            if (this.mIsManagedBySettings && (allowed = TextUtils.delimitedStringContains(Settings.Secure.getStringForUser(LocationManagerService.this.mContext.getContentResolver(), "location_providers_allowed", LocationManagerService.this.mCurrentUserId), ',', this.mName)) != this.mAllowed) {
                if (LocationManagerService.D) {
                    Log.d(LocationManagerService.TAG, this.mName + " provider allowed is now " + this.mAllowed);
                }
                this.mAllowed = allowed;
                onUseableChangedLocked(true);
            }
        }

        @GuardedBy({"mLock"})
        public boolean isUseableLocked() {
            return isUseableForUserLocked(LocationManagerService.this.mCurrentUserId);
        }

        @GuardedBy({"mLock"})
        public boolean isUseableForUserLocked(int userId) {
            return LocationManagerService.this.isCurrentProfileLocked(userId) && this.mUseable;
        }

        @GuardedBy({"mLock"})
        private boolean isUseableIgnoringAllowedLocked() {
            return this.mProvider != null && LocationManagerService.this.mProviders.contains(this) && LocationManagerService.this.isLocationEnabled() && this.mEnabled;
        }

        @GuardedBy({"mLock"})
        public void onUseableChangedLocked(boolean isAllowedChanged) {
            boolean useableIgnoringAllowed = isUseableIgnoringAllowedLocked();
            boolean useable = useableIgnoringAllowed && this.mAllowed;
            if (this.mIsManagedBySettings) {
                if (useableIgnoringAllowed && !isAllowedChanged) {
                    ContentResolver contentResolver = LocationManagerService.this.mContext.getContentResolver();
                    Settings.Secure.putStringForUser(contentResolver, "location_providers_allowed", "+" + this.mName, LocationManagerService.this.mCurrentUserId);
                } else if (!useableIgnoringAllowed) {
                    ContentResolver contentResolver2 = LocationManagerService.this.mContext.getContentResolver();
                    Settings.Secure.putStringForUser(contentResolver2, "location_providers_allowed", "-" + this.mName, LocationManagerService.this.mCurrentUserId);
                }
                Intent intent = new Intent("android.location.PROVIDERS_CHANGED");
                intent.putExtra("android.location.extra.PROVIDER_NAME", this.mName);
                LocationManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
            if (useable != this.mUseable) {
                this.mUseable = useable;
                if (LocationManagerService.D) {
                    Log.i(LocationManagerService.TAG, this.mName + " provider useable is now " + this.mUseable);
                }
                if (!this.mUseable) {
                    LocationManagerService.this.mLastLocation.clear();
                    LocationManagerService.this.mLastLocationCoarseInterval.clear();
                }
                LocationManagerService.this.updateProviderUseableLocked(this);
            }
        }

        @GuardedBy({"mLock"})
        public void onUserChangingLocked() {
            this.mUseable = false;
            LocationManagerService.this.updateProviderUseableLocked(this);
        }
    }

    private class MockLocationProvider extends LocationProvider {
        private ProviderRequest mCurrentRequest;

        private MockLocationProvider(String name) {
            super(LocationManagerService.this, name);
        }

        @Override // com.android.server.LocationManagerService.LocationProvider
        public void attachLocked(AbstractLocationProvider provider) {
            Preconditions.checkState(provider instanceof MockProvider);
            super.attachLocked(provider);
        }

        @Override // com.android.server.LocationManagerService.LocationProvider
        public boolean isMock() {
            return true;
        }

        @GuardedBy({"mLock"})
        public void setEnabledLocked(boolean enabled) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ((MockProvider) this.mProvider).setEnabled(enabled);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @GuardedBy({"mLock"})
        public void setLocationLocked(Location location) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ((MockProvider) this.mProvider).setLocation(location);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @Override // com.android.server.LocationManagerService.LocationProvider
        @GuardedBy({"mLock"})
        public void setRequestLocked(ProviderRequest request, WorkSource workSource) {
            super.setRequestLocked(request, workSource);
            this.mCurrentRequest = request;
        }

        @GuardedBy({"mLock"})
        public void setStatusLocked(int status, Bundle extras, long updateTime) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ((MockProvider) this.mProvider).setStatus(status, extras, updateTime);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public final class Receiver extends LinkedListenerBase implements PendingIntent.OnFinished {
        private static final long WAKELOCK_TIMEOUT_MILLIS = 60000;
        private final int mAllowedResolutionLevel;
        private final boolean mHideFromAppOps;
        private final Object mKey;
        final ILocationListener mListener;
        private boolean mOpHighPowerMonitoring;
        private boolean mOpMonitoring;
        int mPendingBroadcasts;
        final PendingIntent mPendingIntent;
        final HashMap<String, UpdateRecord> mUpdateRecords;
        PowerManager.WakeLock mWakeLock;
        final WorkSource mWorkSource;

        private Receiver(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
            super(new CallerIdentity(uid, pid, packageName), "LocationListener");
            this.mUpdateRecords = new HashMap<>();
            this.mListener = listener;
            this.mPendingIntent = intent;
            if (listener != null) {
                this.mKey = listener.asBinder();
            } else {
                this.mKey = intent;
            }
            this.mAllowedResolutionLevel = LocationManagerService.this.getAllowedResolutionLevel(pid, uid);
            if (workSource != null && workSource.isEmpty()) {
                workSource = null;
            }
            this.mWorkSource = workSource;
            this.mHideFromAppOps = hideFromAppOps;
            updateMonitoring(true);
            this.mWakeLock = LocationManagerService.this.mPowerManager.newWakeLock(1, LocationManagerService.WAKELOCK_KEY);
            this.mWakeLock.setWorkSource(workSource == null ? new WorkSource(this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName) : workSource);
            this.mWakeLock.setReferenceCounted(false);
        }

        @Override // java.lang.Object
        public boolean equals(Object otherObj) {
            return (otherObj instanceof Receiver) && this.mKey.equals(((Receiver) otherObj).mKey);
        }

        @Override // java.lang.Object
        public int hashCode() {
            return this.mKey.hashCode();
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Reciever[");
            s.append(Integer.toHexString(System.identityHashCode(this)));
            if (this.mListener != null) {
                s.append(" listener");
            } else {
                s.append(" intent");
            }
            for (String p : this.mUpdateRecords.keySet()) {
                s.append(" ");
                s.append(this.mUpdateRecords.get(p).toString());
            }
            s.append(" monitoring location: ");
            s.append(this.mOpMonitoring);
            s.append("]");
            return s.toString();
        }

        public void updateMonitoring(boolean allow) {
            if (!this.mHideFromAppOps) {
                boolean requestingLocation = false;
                boolean requestingHighPowerLocation = false;
                if (allow) {
                    Iterator<UpdateRecord> it = this.mUpdateRecords.values().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        UpdateRecord updateRecord = it.next();
                        LocationProvider provider = LocationManagerService.this.getLocationProviderLocked(updateRecord.mProvider);
                        if (provider != null && (provider.isUseableLocked() || LocationManagerService.this.isSettingsExemptLocked(updateRecord))) {
                            requestingLocation = true;
                            ProviderProperties properties = provider.getPropertiesLocked();
                            Log.i(LocationManagerService.TAG, "mPackageName = " + this.mCallerIdentity.mPackageName + ", interval = " + updateRecord.mRequest.getInterval() + ", provider:" + updateRecord.mProvider);
                            if (properties != null && properties.mPowerRequirement == 3 && updateRecord.mRealRequest.getInterval() < 300000 && !LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                                requestingHighPowerLocation = true;
                                break;
                            }
                        }
                    }
                }
                Log.i(LocationManagerService.TAG, "requestingHighPowerLocation = " + requestingHighPowerLocation);
                boolean wasOpMonitoring = this.mOpMonitoring;
                this.mOpMonitoring = updateMonitoring(requestingLocation, this.mOpMonitoring, 41);
                if (this.mOpMonitoring != wasOpMonitoring) {
                    LocationManagerService locationManagerService = LocationManagerService.this;
                    locationManagerService.hwSendLocationChangedAction(locationManagerService.mContext, this.mCallerIdentity.mPackageName);
                }
                boolean wasHighPowerMonitoring = this.mOpHighPowerMonitoring;
                this.mOpHighPowerMonitoring = updateMonitoring(requestingHighPowerLocation, this.mOpHighPowerMonitoring, 42);
                if (this.mOpHighPowerMonitoring != wasHighPowerMonitoring) {
                    LocationManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.location.HIGH_POWER_REQUEST_CHANGE"), UserHandle.ALL);
                }
            }
        }

        private boolean updateMonitoring(boolean allowMonitoring, boolean currentlyMonitoring, int op) {
            if (!currentlyMonitoring) {
                if (allowMonitoring) {
                    if (LocationManagerService.this.mAppOps.startOpNoThrow(op, this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName) == 0) {
                        return true;
                    }
                    return false;
                }
            } else if (!allowMonitoring || LocationManagerService.this.mAppOps.checkOpNoThrow(op, this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName) != 0) {
                LocationManagerService.this.mAppOps.finishOp(op, this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName);
                return false;
            }
            return currentlyMonitoring;
        }

        public boolean isListener() {
            return this.mListener != null;
        }

        public boolean isPendingIntent() {
            return this.mPendingIntent != null;
        }

        public ILocationListener getListener() {
            ILocationListener iLocationListener = this.mListener;
            if (iLocationListener != null) {
                return iLocationListener;
            }
            throw new IllegalStateException("Request for non-existent listener");
        }

        public boolean callStatusChangedLocked(String provider, int status, Bundle extras) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                            return true;
                        }
                        Log.i(LocationManagerService.TAG, "callStatusChangedLocked receiver " + Integer.toHexString(System.identityHashCode(this)) + " mPendingBroadcasts=" + this.mPendingBroadcasts);
                        this.mListener.onStatusChanged(provider, status, extras);
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent statusChanged = new Intent();
                statusChanged.putExtras(new Bundle(extras));
                statusChanged.putExtra("status", status);
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                            return true;
                        }
                        this.mPendingIntent.send(LocationManagerService.this.mContext, 0, statusChanged, this, LocationManagerService.this.mHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        public boolean callLocationChangedLocked(Location location) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                            Log.i(LocationManagerService.TAG, "PackageName is freezed, don't send location: " + this.mCallerIdentity.mPackageName);
                            return true;
                        }
                        Log.i(LocationManagerService.TAG, "key of receiver: " + Integer.toHexString(System.identityHashCode(this)) + " mPendingBroadcasts=" + this.mPendingBroadcasts);
                        this.mListener.onLocationChanged(LocationManagerService.this.putHdExtras(this.mCallerIdentity.mPackageName, new Location(location)));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent locationChanged = new Intent();
                locationChanged.putExtra("location", new Location(location));
                locationChanged.addHwFlags(512);
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                            return true;
                        }
                        this.mPendingIntent.send(LocationManagerService.this.mContext, 0, locationChanged, this, LocationManagerService.this.mHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean callProviderEnabledLocked(String provider, boolean enabled) {
            updateMonitoring(true);
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                            return true;
                        }
                        Log.i(LocationManagerService.TAG, "callProviderEnabledLocked receiver " + Integer.toHexString(System.identityHashCode(this)) + " mPendingBroadcasts=" + this.mPendingBroadcasts);
                        if (enabled) {
                            this.mListener.onProviderEnabled(provider);
                        } else {
                            this.mListener.onProviderDisabled(provider);
                        }
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent providerIntent = new Intent();
                providerIntent.putExtra("providerEnabled", enabled);
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mCallerIdentity.mPackageName)) {
                            return true;
                        }
                        this.mPendingIntent.send(LocationManagerService.this.mContext, 0, providerIntent, this, LocationManagerService.this.mHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            if (LocationManagerService.D) {
                Log.i(LocationManagerService.TAG, "Remote " + this.mListenerName + " died.");
            }
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.removeUpdatesLocked(this);
                clearPendingBroadcastsLocked();
            }
        }

        @Override // android.app.PendingIntent.OnFinished
        public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (LocationManagerService.this.mLock) {
                decrementPendingBroadcastsLocked();
            }
        }

        private void incrementPendingBroadcastsLocked() {
            this.mPendingBroadcasts++;
            long identity = Binder.clearCallingIdentity();
            try {
                this.mWakeLock.acquire(60000);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void decrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts - 1;
            this.mPendingBroadcasts = i;
            if (i == 0) {
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mWakeLock.isHeld()) {
                        this.mWakeLock.release();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            if (this.mPendingBroadcasts < 0) {
                this.mPendingBroadcasts = 0;
            }
        }

        public void clearPendingBroadcastsLocked() {
            if (this.mPendingBroadcasts > 0) {
                this.mPendingBroadcasts = 0;
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mWakeLock.isHeld()) {
                        this.mWakeLock.release();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void locationCallbackFinished(ILocationListener listener) {
        synchronized (this.mLock) {
            Receiver receiver = this.mReceivers.get(listener.asBinder());
            if (receiver != null) {
                receiver.decrementPendingBroadcastsLocked();
            }
        }
    }

    public int getGnssYearOfHardware() {
        GnssLocationProvider.GnssSystemInfoProvider gnssSystemInfoProvider = this.mGnssSystemInfoProvider;
        if (gnssSystemInfoProvider != null) {
            return gnssSystemInfoProvider.getGnssYearOfHardware();
        }
        return 0;
    }

    public String getGnssHardwareModelName() {
        GnssLocationProvider.GnssSystemInfoProvider gnssSystemInfoProvider = this.mGnssSystemInfoProvider;
        if (gnssSystemInfoProvider != null) {
            return gnssSystemInfoProvider.getGnssHardwareModelName();
        }
        return null;
    }

    private boolean hasGnssPermissions(String packageName) {
        boolean checkLocationAccess;
        HwSystemManager.allowOp(this.mContext, 8);
        synchronized (this.mLock) {
            int allowedResolutionLevel = getCallerAllowedResolutionLevel();
            checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, "gps");
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            try {
                checkLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        return checkLocationAccess;
    }

    public int getGnssBatchSize(String packageName) {
        GnssBatchingProvider gnssBatchingProvider;
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || (gnssBatchingProvider = this.mGnssBatchingProvider) == null) {
            return 0;
        }
        return gnssBatchingProvider.getBatchSize();
    }

    public boolean addGnssBatchingCallback(IBatchedLocationCallback callback, String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return false;
        }
        CallerIdentity callerIdentity = new CallerIdentity(Binder.getCallingUid(), Binder.getCallingPid(), packageName);
        synchronized (this.mLock) {
            this.mGnssBatchingCallback = callback;
            this.mGnssBatchingDeathCallback = new LinkedListener<>(callback, "BatchedLocationCallback", callerIdentity, new Consumer() {
                /* class com.android.server.$$Lambda$LocationManagerService$xcF26TLkIZbIZSx4KycvEL0Ums */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    LocationManagerService.this.lambda$addGnssBatchingCallback$9$LocationManagerService((IBatchedLocationCallback) obj);
                }
            });
            if (!linkToListenerDeathNotificationLocked(callback.asBinder(), this.mGnssBatchingDeathCallback)) {
                return false;
            }
            return true;
        }
    }

    public /* synthetic */ void lambda$addGnssBatchingCallback$9$LocationManagerService(IBatchedLocationCallback listener) {
        stopGnssBatch();
        removeGnssBatchingCallback();
    }

    public void removeGnssBatchingCallback() {
        synchronized (this.mLock) {
            unlinkFromListenerDeathNotificationLocked(this.mGnssBatchingCallback.asBinder(), this.mGnssBatchingDeathCallback);
            this.mGnssBatchingCallback = null;
            this.mGnssBatchingDeathCallback = null;
        }
    }

    public boolean startGnssBatch(long periodNanos, boolean wakeOnFifoFull, String packageName) {
        boolean start;
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mGnssBatchingInProgress) {
                Log.e(TAG, "startGnssBatch unexpectedly called w/o stopping prior batch");
                stopGnssBatch();
            }
            this.mGnssBatchingInProgress = true;
            start = this.mGnssBatchingProvider.start(periodNanos, wakeOnFifoFull);
        }
        return start;
    }

    public void flushGnssBatch(String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName)) {
            Log.e(TAG, "flushGnssBatch called without GNSS permissions");
            return;
        }
        synchronized (this.mLock) {
            if (!this.mGnssBatchingInProgress) {
                Log.w(TAG, "flushGnssBatch called with no batch in progress");
            }
            if (this.mGnssBatchingProvider != null) {
                this.mGnssBatchingProvider.flush();
            }
        }
    }

    public boolean stopGnssBatch() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        synchronized (this.mLock) {
            if (this.mGnssBatchingProvider == null) {
                return false;
            }
            this.mGnssBatchingInProgress = false;
            return this.mGnssBatchingProvider.stop();
        }
    }

    @GuardedBy({"mLock"})
    private void addProviderLocked(LocationProvider provider) {
        Preconditions.checkState(getLocationProviderLocked(provider.getName()) == null);
        this.mProviders.add(provider);
        provider.onAllowedChangedLocked();
        provider.onUseableChangedLocked(false);
    }

    @GuardedBy({"mLock"})
    private void removeProviderLocked(LocationProvider provider) {
        if (this.mProviders.remove(provider)) {
            provider.onUseableChangedLocked(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private LocationProvider getLocationProviderLocked(String providerName) {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            LocationProvider provider = it.next();
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getResolutionPermission(int resolutionLevel) {
        if (resolutionLevel == 1) {
            return "android.permission.ACCESS_COARSE_LOCATION";
        }
        if (resolutionLevel != 2) {
            return null;
        }
        return "android.permission.ACCESS_FINE_LOCATION";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getAllowedResolutionLevel(int pid, int uid) {
        if (this.mContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0) {
            return 2;
        }
        if (this.mContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0) {
            return 1;
        }
        return 0;
    }

    private int getCallerAllowedResolutionLevel() {
        return getAllowedResolutionLevel(Binder.getCallingPid(), Binder.getCallingUid());
    }

    private void checkResolutionLevelIsSufficientForGeofenceUse(int allowedResolutionLevel) {
        if (allowedResolutionLevel < 2) {
            throw new SecurityException("Geofence usage requires ACCESS_FINE_LOCATION permission");
        }
    }

    @GuardedBy({"mLock"})
    private int getMinimumResolutionLevelForProviderUseLocked(String provider) {
        ProviderProperties properties;
        if ("gps".equals(provider) || "passive".equals(provider)) {
            return 2;
        }
        if ("network".equals(provider) || "fused".equals(provider)) {
            return 1;
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            LocationProvider lp = it.next();
            if (lp.getName().equals(provider) && (properties = lp.getPropertiesLocked()) != null) {
                if (properties.mRequiresSatellite) {
                    return 2;
                }
                if (properties.mRequiresNetwork || properties.mRequiresCell) {
                    return 1;
                }
            }
        }
        return 2;
    }

    @GuardedBy({"mLock"})
    private void checkResolutionLevelIsSufficientForProviderUseLocked(int allowedResolutionLevel, String providerName) {
        int requiredResolutionLevel = getMinimumResolutionLevelForProviderUseLocked(providerName);
        if (allowedResolutionLevel >= requiredResolutionLevel) {
            return;
        }
        if (requiredResolutionLevel == 1) {
            throw new SecurityException("\"" + providerName + "\" location provider requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.");
        } else if (requiredResolutionLevel != 2) {
            throw new SecurityException("Insufficient permission for \"" + providerName + "\" location provider.");
        } else {
            throw new SecurityException("\"" + providerName + "\" location provider requires ACCESS_FINE_LOCATION permission.");
        }
    }

    public static int resolutionLevelToOp(int allowedResolutionLevel) {
        if (allowedResolutionLevel == 0) {
            return -1;
        }
        if (allowedResolutionLevel == 1) {
            return 0;
        }
        return 1;
    }

    private static String resolutionLevelToOpStr(int allowedResolutionLevel) {
        if (allowedResolutionLevel == 0) {
            return "android:fine_location";
        }
        if (allowedResolutionLevel != 1) {
            return allowedResolutionLevel != 2 ? "android:fine_location" : "android:fine_location";
        }
        return "android:coarse_location";
    }

    private boolean reportLocationAccessNoThrow(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if ((op < 0 || this.mAppOps.noteOpNoThrow(op, uid, packageName) == 0) && getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            return true;
        }
        return false;
    }

    private boolean checkLocationAccess(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if ((op < 0 || this.mAppOps.checkOp(op, uid, packageName) == 0) && getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            return true;
        }
        return false;
    }

    public List<String> getAllProviders() {
        ArrayList<String> providers;
        synchronized (this.mLock) {
            providers = new ArrayList<>(this.mProviders.size());
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                String name = it.next().getName();
                if (!"fused".equals(name)) {
                    if (!IHwLocalLocationProvider.LOCAL_PROVIDER.equals(name)) {
                        providers.add(name);
                    }
                }
            }
        }
        return providers;
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        ArrayList<String> providers;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_GETPROVIDERS);
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        synchronized (this.mLock) {
            providers = new ArrayList<>(this.mProviders.size());
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                LocationProvider provider = it.next();
                String name = provider.getName();
                if (!"fused".equals(name)) {
                    if (!IHwLocalLocationProvider.LOCAL_PROVIDER.equals(name)) {
                        if (allowedResolutionLevel >= getMinimumResolutionLevelForProviderUseLocked(name)) {
                            if (!enabledOnly || provider.isUseableLocked()) {
                                if (criteria == null || android.location.LocationProvider.propertiesMeetCriteria(name, provider.getPropertiesLocked(), criteria)) {
                                    providers.add(name);
                                }
                            }
                        }
                    }
                }
            }
        }
        return providers;
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_GETBESTPROVIDER);
        List<String> providers = getProviders(criteria, enabledOnly);
        if (providers.isEmpty()) {
            providers = getProviders(null, enabledOnly);
        }
        if (providers.isEmpty()) {
            return null;
        }
        if (providers.contains("gps")) {
            return "gps";
        }
        if (providers.contains("network")) {
            return "network";
        }
        return providers.get(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void updateProviderUseableLocked(LocationProvider provider) {
        boolean useable = provider.isUseableLocked();
        ArrayList<Receiver> deadReceivers = null;
        ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(provider.getName());
        if (records != null) {
            Iterator<UpdateRecord> it = records.iterator();
            while (it.hasNext()) {
                UpdateRecord record = it.next();
                if (isCurrentProfileLocked(UserHandle.getUserId(record.mReceiver.mCallerIdentity.mUid)) && !isSettingsExemptLocked(record) && !record.mReceiver.callProviderEnabledLocked(provider.getName(), useable)) {
                    if (deadReceivers == null) {
                        deadReceivers = new ArrayList<>();
                    }
                    deadReceivers.add(record.mReceiver);
                }
            }
        }
        if (deadReceivers != null) {
            for (int i = deadReceivers.size() - 1; i >= 0; i--) {
                removeUpdatesLocked(deadReceivers.get(i));
            }
        }
        applyRequirementsLocked(provider);
    }

    @GuardedBy({"mLock"})
    private void applyRequirementsLocked(String providerName) {
        LocationProvider provider = getLocationProviderLocked(providerName);
        if (provider != null) {
            applyRequirementsLocked(provider);
        }
    }

    /* JADX INFO: finally extract failed */
    @GuardedBy({"mLock"})
    private void applyRequirementsLocked(LocationProvider provider) {
        boolean z;
        ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(provider.getName());
        WorkSource worksource = new WorkSource();
        ProviderRequest providerRequest = new ProviderRequest();
        if (this.mProviders.contains(provider) && records != null && !records.isEmpty()) {
            long identity = Binder.clearCallingIdentity();
            try {
                long backgroundThrottleInterval = Settings.Global.getLong(this.mContext.getContentResolver(), "location_background_throttle_interval_ms", 1800000);
                Binder.restoreCallingIdentity(identity);
                boolean isForegroundOnlyMode = this.mBatterySaverMode == 3;
                boolean shouldThrottleRequests = this.mBatterySaverMode == 4 && !this.mPowerManager.isInteractive();
                providerRequest.lowPowerMode = true;
                Iterator<UpdateRecord> it = records.iterator();
                while (it.hasNext()) {
                    UpdateRecord record = it.next();
                    if (isCurrentProfileLocked(UserHandle.getUserId(record.mReceiver.mCallerIdentity.mUid))) {
                        if (!checkLocationAccess(record.mReceiver.mCallerIdentity.mPid, record.mReceiver.mCallerIdentity.mUid, record.mReceiver.mCallerIdentity.mPackageName, record.mReceiver.mAllowedResolutionLevel)) {
                            it = it;
                        } else {
                            boolean isBatterySaverDisablingLocation = shouldThrottleRequests || (isForegroundOnlyMode && !record.mIsForegroundUid);
                            if (!provider.isUseableLocked() || isBatterySaverDisablingLocation) {
                                if (isSettingsExemptLocked(record)) {
                                    providerRequest.locationSettingsIgnored = true;
                                    providerRequest.lowPowerMode = false;
                                } else {
                                    it = it;
                                    isForegroundOnlyMode = isForegroundOnlyMode;
                                }
                            }
                            if ("gps".equals(provider.getName()) && isFreeze(record.mReceiver.mCallerIdentity.mPackageName)) {
                                Log.i(TAG, "packageName:" + record.mReceiver.mCallerIdentity.mPackageName + " is freeze, can't start gps");
                                it = it;
                            } else if ("network".equals(provider.getName()) && isFreeze(record.mReceiver.mCallerIdentity.mPackageName)) {
                                Log.i(TAG, "packageName:" + record.mReceiver.mCallerIdentity.mPackageName + " is freeze, can't start network");
                                it = it;
                            } else if (!"fused".equals(provider.getName()) || !isFreeze(record.mReceiver.mCallerIdentity.mPackageName)) {
                                LocationRequest locationRequest = record.mRealRequest;
                                long interval = locationRequest.getInterval();
                                if (!providerRequest.locationSettingsIgnored && !isThrottlingExemptLocked(record.mReceiver.mCallerIdentity)) {
                                    if (!record.mIsForegroundUid) {
                                        interval = Math.max(interval, backgroundThrottleInterval);
                                    }
                                    if (interval != locationRequest.getInterval()) {
                                        locationRequest = new LocationRequest(locationRequest);
                                        locationRequest.setInterval(interval);
                                    }
                                }
                                record.mRequest = locationRequest;
                                providerRequest.locationRequests.add(locationRequest);
                                if (!locationRequest.isLowPowerMode()) {
                                    providerRequest.lowPowerMode = false;
                                }
                                if (interval < providerRequest.interval) {
                                    z = true;
                                    providerRequest.reportLocation = true;
                                    providerRequest.interval = interval;
                                } else {
                                    z = true;
                                }
                                it = it;
                                isForegroundOnlyMode = isForegroundOnlyMode;
                                backgroundThrottleInterval = backgroundThrottleInterval;
                            } else {
                                Log.i(TAG, "packageName:" + record.mReceiver.mCallerIdentity.mPackageName + " is freeze, can't start fused");
                                it = it;
                            }
                        }
                    }
                }
                if (providerRequest.reportLocation) {
                    long thresholdInterval = ((providerRequest.interval + 1000) * 3) / 2;
                    Iterator<UpdateRecord> it2 = records.iterator();
                    while (it2.hasNext()) {
                        UpdateRecord record2 = it2.next();
                        if (isCurrentProfileLocked(UserHandle.getUserId(record2.mReceiver.mCallerIdentity.mUid))) {
                            LocationRequest locationRequest2 = record2.mRequest;
                            if (providerRequest.locationRequests.contains(locationRequest2) && locationRequest2.getInterval() <= thresholdInterval) {
                                if (record2.mReceiver.mWorkSource == null || !isValidWorkSource(record2.mReceiver.mWorkSource)) {
                                    worksource.add(record2.mReceiver.mCallerIdentity.mUid, record2.mReceiver.mCallerIdentity.mPackageName);
                                } else {
                                    worksource.add(record2.mReceiver.mWorkSource);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
        provider.setRequestLocked(providerRequest, worksource);
        if (this.isChrFirstRequest) {
            this.mHwLocationGpsLogServices.netWorkLocation(provider.getName(), providerRequest);
            this.isChrFirstRequest = false;
            Log.i(TAG, "network chr update");
        }
        resetGnssSourceType(provider, providerRequest.reportLocation);
    }

    private void resetGnssSourceType(LocationProvider provider, boolean isReportLocation) {
        if ("gps".equals(provider.getName()) && !isReportLocation) {
            if (D) {
                Log.i(TAG, "reset gnss source type to zero");
            }
            int[] iArr = this.mLastGnssSourceType;
            iArr[0] = 0;
            iArr[1] = 0;
        }
    }

    private static boolean isValidWorkSource(WorkSource workSource) {
        if (workSource.size() > 0) {
            return workSource.getName(0) != null;
        }
        ArrayList<WorkSource.WorkChain> workChains = workSource.getWorkChains();
        return (workChains == null || workChains.isEmpty() || workChains.get(0).getAttributionTag() == null) ? false : true;
    }

    public String[] getBackgroundThrottlingWhitelist() {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) this.mBackgroundThrottlePackageWhitelist.toArray(new String[0]);
        }
        return strArr;
    }

    public String[] getIgnoreSettingsWhitelist() {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) this.mIgnoreSettingsPackageWhitelist.toArray(new String[0]);
        }
        return strArr;
    }

    @GuardedBy({"mLock"})
    private boolean isThrottlingExemptLocked(CallerIdentity callerIdentity) {
        if (callerIdentity.mUid != 1000 && !this.mBackgroundThrottlePackageWhitelist.contains(callerIdentity.mPackageName)) {
            return isProviderPackage(callerIdentity.mPackageName);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private boolean isSettingsExemptLocked(UpdateRecord record) {
        if (!record.mRealRequest.isLocationSettingsIgnored()) {
            return false;
        }
        if (this.mIgnoreSettingsPackageWhitelist.contains(record.mReceiver.mCallerIdentity.mPackageName)) {
            return true;
        }
        return isProviderPackage(record.mReceiver.mCallerIdentity.mPackageName);
    }

    public class UpdateRecord {
        private boolean mIsForegroundUid;
        private Location mLastFixBroadcast;
        private long mLastStatusBroadcast;
        String mProvider;
        final LocationRequest mRealRequest;
        final Receiver mReceiver;
        LocationRequest mRequest;
        private Throwable mStackTrace;

        private UpdateRecord(String provider, LocationRequest request, Receiver receiver) {
            this.mProvider = provider;
            this.mRealRequest = request;
            this.mRequest = request;
            this.mReceiver = receiver;
            this.mIsForegroundUid = LocationManagerService.isImportanceForeground(LocationManagerService.this.mActivityManager.getPackageImportance(this.mReceiver.mCallerIdentity.mPackageName));
            if (LocationManagerService.D && receiver.mCallerIdentity.mPid == Process.myPid()) {
                this.mStackTrace = new Throwable();
            }
            ArrayList<UpdateRecord> records = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(provider);
            if (records == null) {
                records = new ArrayList<>();
                LocationManagerService.this.mRecordsByProvider.put(provider, records);
            }
            if (!records.contains(this)) {
                records.add(this);
            }
            LocationManagerService.this.mRequestStatistics.startRequesting(this.mReceiver.mCallerIdentity.mPackageName, provider, request.getInterval(), this.mIsForegroundUid);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateForeground(boolean isForeground) {
            this.mIsForegroundUid = isForeground;
            LocationManagerService.this.mRequestStatistics.updateForeground(this.mReceiver.mCallerIdentity.mPackageName, this.mProvider, isForeground);
        }

        public void disposeLocked(boolean removeReceiver) {
            String packageName = this.mReceiver.mCallerIdentity.mPackageName;
            LocationManagerService.this.mRequestStatistics.stopRequesting(packageName, this.mProvider);
            LocationManagerService.this.mLocationUsageLogger.logLocationApiUsage(1, 1, packageName, this.mRealRequest, this.mReceiver.isListener(), this.mReceiver.isPendingIntent(), null, LocationManagerService.this.mActivityManager.getPackageImportance(packageName));
            ArrayList<UpdateRecord> globalRecords = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(this.mProvider);
            if (globalRecords != null) {
                globalRecords.remove(this);
            }
            if (removeReceiver) {
                HashMap<String, UpdateRecord> receiverRecords = this.mReceiver.mUpdateRecords;
                receiverRecords.remove(this.mProvider);
                if (receiverRecords.size() == 0) {
                    LocationManagerService.this.removeUpdatesLocked(this.mReceiver);
                }
            }
        }

        public String toString() {
            StringBuilder b = new StringBuilder("UpdateRecord[");
            b.append(this.mProvider);
            b.append(" ");
            b.append(this.mReceiver.mCallerIdentity.mPackageName);
            b.append("(");
            b.append(this.mReceiver.mCallerIdentity.mUid);
            if (this.mIsForegroundUid) {
                b.append(" foreground");
            } else {
                b.append(" background");
            }
            b.append(") ");
            b.append(this.mRealRequest);
            b.append(" ");
            b.append(this.mReceiver.mWorkSource);
            if (this.mStackTrace != null) {
                ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                this.mStackTrace.printStackTrace(new PrintStream(tmp));
                b.append("\n\n");
                b.append(tmp.toString());
                b.append("\n");
            }
            b.append("]");
            return b.toString();
        }
    }

    @GuardedBy({"mLock"})
    private Receiver getReceiverLocked(ILocationListener listener, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        IBinder binder = listener.asBinder();
        Receiver receiver = this.mReceivers.get(binder);
        if (receiver == null) {
            receiver = new Receiver(listener, null, pid, uid, packageName, workSource, hideFromAppOps);
            if (!linkToListenerDeathNotificationLocked(receiver.getListener().asBinder(), receiver)) {
                return null;
            }
            this.mReceivers.put(binder, receiver);
        }
        return receiver;
    }

    @GuardedBy({"mLock"})
    private Receiver getReceiverLocked(PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        Receiver receiver = this.mReceivers.get(intent);
        if (receiver != null) {
            return receiver;
        }
        Receiver receiver2 = new Receiver(null, intent, pid, uid, packageName, workSource, hideFromAppOps);
        this.mReceivers.put(intent, receiver2);
        return receiver2;
    }

    private LocationRequest createSanitizedRequest(LocationRequest request, int resolutionLevel, boolean callerHasLocationHardwarePermission) {
        LocationRequest sanitizedRequest = new LocationRequest(request);
        if (!callerHasLocationHardwarePermission) {
            sanitizedRequest.setLowPowerMode(false);
        }
        if (resolutionLevel < 2) {
            int quality = sanitizedRequest.getQuality();
            if (quality == 100) {
                sanitizedRequest.setQuality(102);
            } else if (quality == 203) {
                sanitizedRequest.setQuality(201);
            }
            if (sanitizedRequest.getInterval() < 600000) {
                sanitizedRequest.setInterval(600000);
            }
            if (sanitizedRequest.getFastestInterval() < 600000) {
                sanitizedRequest.setFastestInterval(600000);
            }
        }
        if (sanitizedRequest.getFastestInterval() > sanitizedRequest.getInterval()) {
            sanitizedRequest.setFastestInterval(request.getInterval());
        }
        return sanitizedRequest;
    }

    private void checkPackageName(String packageName) {
        if (packageName != null) {
            int uid = Binder.getCallingUid();
            String[] packages = this.mPackageManager.getPackagesForUid(uid);
            if (packages != null) {
                for (String pkg : packages) {
                    if (packageName.equals(pkg)) {
                        return;
                    }
                }
                throw new SecurityException("invalid package name: " + packageName);
            }
            throw new SecurityException("invalid UID " + uid);
        }
        throw new SecurityException("invalid package name: " + ((Object) null));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        String str;
        Throwable th;
        String str2;
        Receiver receiver;
        Object obj = this.mLock;
        synchronized (obj) {
            try {
                if (!isRequestValid(request, listener, intent, packageName)) {
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        str = obj;
                        throw th;
                    }
                } else {
                    LocationRequest request2 = request == null ? DEFAULT_LOCATION_REQUEST : request;
                    try {
                        checkPackageName(packageName);
                        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
                        checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, request2.getProvider());
                        WorkSource workSource = request2.getWorkSource();
                        if (workSource != null) {
                            try {
                                if (!workSource.isEmpty()) {
                                    this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                str = obj;
                                throw th;
                            }
                        }
                        boolean hideFromAppOps = request2.getHideFromAppOps();
                        if (hideFromAppOps) {
                            this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_APP_OPS_STATS", null);
                        }
                        if (request2.isLocationSettingsIgnored()) {
                            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS", null);
                        }
                        boolean z = true;
                        LocationRequest sanitizedRequest = createSanitizedRequest(request2, allowedResolutionLevel, this.mContext.checkCallingPermission("android.permission.LOCATION_HARDWARE") == 0);
                        int pid = Binder.getCallingPid();
                        int uid = Binder.getCallingUid();
                        HwSystemManager.allowOp(this.mContext, 8);
                        long identity = Binder.clearCallingIdentity();
                        try {
                            boolean permission = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
                            if (intent == null && listener == null) {
                                try {
                                    throw new IllegalArgumentException("need either listener or intent");
                                } catch (Throwable th4) {
                                    th = th4;
                                    Binder.restoreCallingIdentity(identity);
                                    throw th;
                                }
                            } else {
                                if (intent != null) {
                                    if (listener != null) {
                                        throw new IllegalArgumentException("cannot register both listener and intent");
                                    }
                                }
                                this.isNetworkChrAllowed = true;
                                if (!permission) {
                                    this.isNetworkChrAllowed = false;
                                    this.mHwLocationGpsLogServices.permissionErr(packageName);
                                }
                                this.mHwQuickTTFFMonitor.setPermission(permission);
                                LocationUsageLogger locationUsageLogger = this.mLocationUsageLogger;
                                boolean z2 = listener != null;
                                if (intent == null) {
                                    z = false;
                                }
                                str = packageName;
                                try {
                                    locationUsageLogger.logLocationApiUsage(0, 1, str, request2, z2, z, null, this.mActivityManager.getPackageImportance(packageName));
                                    if (intent != null) {
                                        str = obj;
                                        str2 = packageName;
                                        try {
                                            receiver = getReceiverLocked(intent, pid, uid, packageName, workSource, hideFromAppOps);
                                        } catch (Throwable th5) {
                                            th = th5;
                                        }
                                    } else {
                                        str = obj;
                                        str2 = packageName;
                                        try {
                                            receiver = getReceiverLocked(listener, pid, uid, packageName, workSource, hideFromAppOps);
                                        } catch (Throwable th6) {
                                            th = th6;
                                            Binder.restoreCallingIdentity(identity);
                                            throw th;
                                        }
                                    }
                                } catch (Throwable th7) {
                                    th = th7;
                                    Binder.restoreCallingIdentity(identity);
                                    throw th;
                                }
                                try {
                                    requestLocationUpdatesLocked(sanitizedRequest, receiver, uid, str2);
                                    Binder.restoreCallingIdentity(identity);
                                } catch (Throwable th8) {
                                    th = th8;
                                    throw th;
                                }
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        str = obj;
                        throw th;
                    }
                }
            } catch (Throwable th11) {
                th = th11;
                str = obj;
                throw th;
            }
        }
    }

    @GuardedBy({"mLock"})
    private void requestLocationUpdatesLocked(LocationRequest request, Receiver receiver, int uid, String packageName) {
        hwRequestLocationUpdatesLocked(request, receiver, Binder.getCallingPid(), uid, packageName);
        LocationRequest request2 = request == null ? DEFAULT_LOCATION_REQUEST : request;
        String name = request2.getProvider();
        if (name != null) {
            String name2 = getLocationProvider(uid, request2, packageName, name);
            LocationProvider provider = getLocationProviderLocked(name2);
            if (provider != null) {
                UpdateRecord record = new UpdateRecord(name2, request2, receiver);
                StringBuilder sb = new StringBuilder();
                sb.append("request ");
                sb.append(Integer.toHexString(System.identityHashCode(receiver)));
                sb.append(" ");
                sb.append(name2);
                sb.append(" ");
                sb.append(request2);
                sb.append(" from ");
                sb.append(packageName);
                sb.append("(");
                sb.append(uid);
                sb.append(" ");
                sb.append(record.mIsForegroundUid ? "foreground" : "background");
                sb.append(isThrottlingExemptLocked(receiver.mCallerIdentity) ? " [whitelisted]" : "");
                sb.append(")");
                Log.i(TAG, sb.toString());
                UpdateRecord oldRecord = receiver.mUpdateRecords.put(name2, record);
                boolean z = false;
                if (oldRecord != null) {
                    oldRecord.disposeLocked(false);
                }
                if ("gps".equals(name2)) {
                    LogPower.push(202, packageName, Integer.toString(Binder.getCallingPid()), Long.toString(request2.getInterval()), new String[]{Integer.toHexString(System.identityHashCode(receiver))});
                }
                if ("network".equals(name2)) {
                    AbstractLocationProvider locationProvider = provider.mProvider;
                    if (locationProvider == null || !(locationProvider instanceof IHwLocationProviderInterface)) {
                        Log.w(TAG, "get LocationProviderInterface error, mock provider is : " + provider.isMock());
                    } else {
                        ((IHwLocationProviderInterface) locationProvider).resetNLPFlag();
                    }
                }
                this.isChrFirstRequest = false;
                if (provider.isUseableLocked() || isSettingsExemptLocked(record)) {
                    this.mHwQuickTTFFMonitor.requestHwQuickTTFF(request2, packageName, name2, Integer.toHexString(System.identityHashCode(receiver)));
                    if (!isFreeze(packageName) && this.isNetworkChrAllowed) {
                        this.isChrFirstRequest = true;
                    }
                } else {
                    receiver.callProviderEnabledLocked(name2, false);
                    this.mHwLocationGpsLogServices.setLocationSettingsOffErr(name2);
                }
                applyRequirementsLocked(name2);
                receiver.updateMonitoring(true);
                int quality = request2.getQuality();
                if (receiver.mListener == null) {
                    z = true;
                }
                hwLocationPowerTrackerRecordRequest(packageName, quality, z);
                return;
            }
            throw new IllegalArgumentException("provider doesn't exist: " + name2);
        }
        throw new IllegalArgumentException("provider name must not be null");
    }

    public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) {
        Receiver receiver;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REMOVEUPDATES);
        if (isRemoveValid(listener, intent, packageName)) {
            checkPackageName(packageName);
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            if (intent == null && listener == null) {
                throw new IllegalArgumentException("need either listener or intent");
            } else if (intent == null || listener == null) {
                synchronized (this.mLock) {
                    if (intent != null) {
                        receiver = getReceiverLocked(intent, pid, uid, packageName, (WorkSource) null, false);
                    } else {
                        receiver = getReceiverLocked(listener, pid, uid, packageName, (WorkSource) null, false);
                    }
                    long identity = Binder.clearCallingIdentity();
                    try {
                        removeUpdatesLocked(receiver);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            } else {
                throw new IllegalArgumentException("cannot register both listener and intent");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void removeUpdatesLocked(Receiver receiver) {
        if (receiver == null) {
            Log.e(TAG, "receiver is null");
            return;
        }
        Log.i(TAG, "remove " + Integer.toHexString(System.identityHashCode(receiver)));
        hwRemoveUpdatesLocked(receiver);
        if (this.mReceivers.remove(receiver.mKey) != null && receiver.isListener()) {
            unlinkFromListenerDeathNotificationLocked(receiver.getListener().asBinder(), receiver);
            receiver.clearPendingBroadcastsLocked();
        }
        this.mHwQuickTTFFMonitor.removeHwQuickTTFF(receiver.mCallerIdentity.mPackageName, Integer.toHexString(System.identityHashCode(receiver)), receiver.mUpdateRecords.containsKey("gps"));
        receiver.updateMonitoring(false);
        HashSet<String> providers = new HashSet<>();
        HashMap<String, UpdateRecord> oldRecords = receiver.mUpdateRecords;
        if (oldRecords != null) {
            for (UpdateRecord record : oldRecords.values()) {
                record.disposeLocked(false);
            }
            providers.addAll(oldRecords.keySet());
        }
        Iterator<String> it = providers.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
        if (oldRecords != null) {
            oldRecords.clear();
        }
    }

    public Location getLastLocation(LocationRequest r, String packageName) {
        Throwable th;
        Location location;
        synchronized (this.mLock) {
            if (!isGetLastLocationValid(packageName)) {
                return hwGetLastLocation(r);
            }
            LocationRequest request = r != null ? r : DEFAULT_LOCATION_REQUEST;
            int allowedResolutionLevel = getCallerAllowedResolutionLevel();
            checkPackageName(packageName);
            checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, request.getProvider());
            HwSystemManager.allowOp(this.mContext, 8);
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            try {
                if (this.mBlacklist.isBlacklisted(packageName)) {
                    try {
                        if (D) {
                            Log.d(TAG, "not returning last loc for blacklisted app: " + packageName);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                } else {
                    String name = request.getProvider();
                    if (name == null) {
                        name = "fused";
                    }
                    LocationProvider provider = getLocationProviderLocked(name);
                    if (provider == null) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (!isCurrentProfileLocked(UserHandle.getUserId(uid)) && !isProviderPackage(packageName)) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (!provider.isUseableLocked()) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else {
                        if (allowedResolutionLevel < 2) {
                            location = this.mLastLocationCoarseInterval.get(name);
                        } else {
                            location = this.mLastLocation.get(name);
                        }
                        if (location == null) {
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        }
                        String op = resolutionLevelToOpStr(allowedResolutionLevel);
                        try {
                            if (SystemClock.elapsedRealtime() - (location.getElapsedRealtimeNanos() / NANOS_PER_MILLI) > Settings.Global.getLong(this.mContext.getContentResolver(), "location_last_location_max_age_millis", DEFAULT_LAST_LOCATION_MAX_AGE_MS)) {
                                try {
                                    if (this.mAppOps.unsafeCheckOp(op, uid, packageName) == 4) {
                                        Binder.restoreCallingIdentity(identity);
                                        return null;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    Binder.restoreCallingIdentity(identity);
                                    throw th;
                                }
                            }
                            Location lastLocation = null;
                            if (allowedResolutionLevel < 2) {
                                try {
                                    Location noGPSLocation = location.getExtraLocation("noGPSLocation");
                                    if (noGPSLocation != null) {
                                        lastLocation = new Location(this.mLocationFudger.getOrCreate(noGPSLocation));
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    Binder.restoreCallingIdentity(identity);
                                    throw th;
                                }
                            } else {
                                lastLocation = new Location(location);
                            }
                            if (lastLocation != null && !reportLocationAccessNoThrow(pid, uid, packageName, allowedResolutionLevel)) {
                                if (D) {
                                    Log.d(TAG, "not returning last loc for no op app: " + packageName);
                                }
                                lastLocation = null;
                            }
                            Binder.restoreCallingIdentity(identity);
                            return lastLocation;
                        } catch (Throwable th5) {
                            th = th5;
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                    }
                }
            } catch (Throwable th6) {
                th = th6;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    public LocationTime getGnssTimeMillis() {
        synchronized (this.mLock) {
            Location location = this.mLastLocation.get("gps");
            if (location == null) {
                return null;
            }
            long currentNanos = SystemClock.elapsedRealtimeNanos();
            return new LocationTime(location.getTime() + ((currentNanos - location.getElapsedRealtimeNanos()) / NANOS_PER_MILLI), currentNanos);
        }
    }

    public boolean injectLocation(Location location) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to inject location");
        this.mContext.enforceCallingPermission("android.permission.ACCESS_FINE_LOCATION", "Access Fine Location permission not granted to inject Location");
        if (location == null) {
            if (D) {
                Log.d(TAG, "injectLocation(): called with null location");
            }
            return false;
        }
        synchronized (this.mLock) {
            LocationProvider provider = getLocationProviderLocked(location.getProvider());
            if (provider != null) {
                if (provider.isUseableLocked()) {
                    if (this.mLastLocation.get(provider.getName()) != null) {
                        return false;
                    }
                    updateLastLocationLocked(location, provider.getName());
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00bd, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00cd, code lost:
        r0 = th;
     */
    public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) {
        Throwable th;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REQUESTGEOFENCE);
        LocationRequest request2 = request == null ? DEFAULT_LOCATION_REQUEST : request;
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForGeofenceUse(allowedResolutionLevel);
        if (intent != null) {
            checkPackageName(packageName);
            synchronized (this.mLock) {
                checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, request2.getProvider());
            }
            LocationRequest sanitizedRequest = createSanitizedRequest(request2, allowedResolutionLevel, this.mContext.checkCallingPermission("android.permission.LOCATION_HARDWARE") == 0);
            if (D) {
                Log.d(TAG, "requestGeofence: " + sanitizedRequest + " " + geofence + " " + intent);
            }
            int uid = Binder.getCallingUid();
            if (UserHandle.getUserId(uid) != 0) {
                Log.w(TAG, "proximity alerts are currently available only to the primary user");
                return;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    this.mLocationUsageLogger.logLocationApiUsage(0, 4, packageName, request2, false, true, geofence, this.mActivityManager.getPackageImportance(packageName));
                }
                try {
                    this.mGeofenceManager.addFence(sanitizedRequest, geofence, intent, allowedResolutionLevel, uid, packageName);
                    Binder.restoreCallingIdentity(identity);
                    return;
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("invalid pending intent: " + ((Object) null));
        }
        while (true) {
        }
        while (true) {
        }
    }

    public void removeGeofence(Geofence geofence, PendingIntent intent, String packageName) {
        if (intent != null) {
            checkPackageName(packageName);
            if (D) {
                Log.d(TAG, "removeGeofence: " + geofence + " " + intent);
            }
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    this.mLocationUsageLogger.logLocationApiUsage(1, 4, packageName, null, false, true, geofence, this.mActivityManager.getPackageImportance(packageName));
                }
                this.mGeofenceManager.removeFence(geofence, intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new IllegalArgumentException("invalid pending intent: " + ((Object) null));
        }
    }

    public boolean registerGnssStatusCallback(IGnssStatusListener listener, String packageName) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REGISTERGNSSSTATUSCALLBACK);
        return addGnssDataListener(listener, packageName, "GnssStatusListener", this.mGnssStatusProvider, this.mGnssStatusListeners, new Consumer() {
            /* class com.android.server.$$Lambda$1kw1pGRY14l4iRI8vioJeswbbZ0 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.unregisterGnssStatusCallback((IGnssStatusListener) obj);
            }
        });
    }

    public void unregisterGnssStatusCallback(IGnssStatusListener listener) {
        removeGnssDataListener(listener, this.mGnssStatusProvider, this.mGnssStatusListeners);
    }

    public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) {
        return addGnssDataListener(listener, packageName, "GnssMeasurementsListener", this.mGnssMeasurementsProvider, this.mGnssMeasurementsListeners, new Consumer() {
            /* class com.android.server.$$Lambda$XnEj1qgrS2tLlw6uNlntfcuKl88 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.removeGnssMeasurementsListener((IGnssMeasurementsListener) obj);
            }
        });
    }

    public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) {
        removeGnssDataListener(listener, this.mGnssMeasurementsProvider, this.mGnssMeasurementsListeners);
    }

    /* access modifiers changed from: private */
    public static abstract class LinkedListenerBase implements IBinder.DeathRecipient {
        protected final CallerIdentity mCallerIdentity;
        protected final String mListenerName;

        private LinkedListenerBase(CallerIdentity callerIdentity, String listenerName) {
            this.mCallerIdentity = callerIdentity;
            this.mListenerName = listenerName;
        }
    }

    public static class LinkedListener<TListener> extends LinkedListenerBase {
        private final Consumer<TListener> mBinderDeathCallback;
        private final TListener mListener;

        private LinkedListener(TListener listener, String listenerName, CallerIdentity callerIdentity, Consumer<TListener> binderDeathCallback) {
            super(callerIdentity, listenerName);
            this.mListener = listener;
            this.mBinderDeathCallback = binderDeathCallback;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            if (LocationManagerService.D) {
                Log.d(LocationManagerService.TAG, "Remote " + this.mListenerName + " died.");
            }
            this.mBinderDeathCallback.accept(this.mListener);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x009a A[Catch:{ NullPointerException -> 0x00be, all -> 0x00ba }] */
    private <TListener extends IInterface> boolean addGnssDataListener(TListener listener, String packageName, String listenerName, RemoteListenerHelper<TListener> gnssDataProvider, ArrayMap<IBinder, LinkedListener<TListener>> gnssDataListeners, Consumer<TListener> binderDeathCallback) {
        Object obj;
        Throwable th;
        NullPointerException e;
        NullPointerException e2;
        IBinder binder;
        int i;
        if (!hasGnssPermissions(packageName)) {
            return false;
        }
        if (gnssDataProvider == null) {
            return false;
        }
        CallerIdentity callerIdentity = new CallerIdentity(Binder.getCallingUid(), Binder.getCallingPid(), packageName);
        LinkedListener<TListener> linkedListener = new LinkedListener<>(listener, listenerName, callerIdentity, binderDeathCallback);
        IBinder binder2 = listener.asBinder();
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                if (!linkToListenerDeathNotificationLocked(binder2, linkedListener)) {
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        obj = obj2;
                        throw th;
                    }
                } else {
                    gnssDataListeners.put(binder2, linkedListener);
                    long identity = Binder.clearCallingIdentity();
                    try {
                        if (gnssDataProvider != this.mGnssMeasurementsProvider) {
                            try {
                                if (gnssDataProvider != this.mGnssStatusProvider) {
                                    obj = obj2;
                                    binder = binder2;
                                    if (!isThrottlingExemptLocked(callerIdentity)) {
                                        if (!isImportanceForeground(this.mActivityManager.getPackageImportance(packageName))) {
                                            Binder.restoreCallingIdentity(identity);
                                            return true;
                                        }
                                    }
                                    printListenerInfo(binder, gnssDataProvider, packageName, true);
                                    try {
                                        gnssDataProvider.addListener(listener, callerIdentity);
                                    } catch (NullPointerException e3) {
                                        e2 = e3;
                                    }
                                    try {
                                        Binder.restoreCallingIdentity(identity);
                                        return true;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        throw th;
                                    }
                                }
                            } catch (NullPointerException e4) {
                                e2 = e4;
                                obj = obj2;
                                try {
                                    Log.e(TAG, "addGnssDataListener error ", e2);
                                    Binder.restoreCallingIdentity(identity);
                                    return false;
                                } catch (Throwable th4) {
                                    e = th4;
                                    Binder.restoreCallingIdentity(identity);
                                    throw e;
                                }
                            } catch (Throwable th5) {
                                e = th5;
                                Binder.restoreCallingIdentity(identity);
                                throw e;
                            }
                        }
                        LocationUsageLogger locationUsageLogger = this.mLocationUsageLogger;
                        if (gnssDataProvider == this.mGnssMeasurementsProvider) {
                            i = 2;
                        } else {
                            i = 3;
                        }
                        obj = obj2;
                        binder = binder2;
                        try {
                            locationUsageLogger.logLocationApiUsage(0, i, packageName, null, true, false, null, this.mActivityManager.getPackageImportance(packageName));
                            if (!isThrottlingExemptLocked(callerIdentity)) {
                            }
                            printListenerInfo(binder, gnssDataProvider, packageName, true);
                            gnssDataProvider.addListener(listener, callerIdentity);
                            Binder.restoreCallingIdentity(identity);
                            return true;
                        } catch (NullPointerException e5) {
                            e2 = e5;
                            Log.e(TAG, "addGnssDataListener error ", e2);
                            Binder.restoreCallingIdentity(identity);
                            return false;
                        } catch (Throwable th6) {
                            e = th6;
                            Binder.restoreCallingIdentity(identity);
                            throw e;
                        }
                    } catch (NullPointerException e6) {
                        e2 = e6;
                        obj = obj2;
                        Log.e(TAG, "addGnssDataListener error ", e2);
                        Binder.restoreCallingIdentity(identity);
                        return false;
                    } catch (Throwable th7) {
                        e = th7;
                        Binder.restoreCallingIdentity(identity);
                        throw e;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                obj = obj2;
                throw th;
            }
        }
        return false;
    }

    private <TListener extends IInterface> void removeGnssDataListener(TListener listener, RemoteListenerHelper<TListener> gnssDataProvider, ArrayMap<IBinder, LinkedListener<TListener>> gnssDataListeners) {
        int i;
        if (gnssDataProvider != null) {
            IBinder binder = listener.asBinder();
            synchronized (this.mLock) {
                try {
                    LinkedListener<TListener> linkedListener = gnssDataListeners.remove(binder);
                    if (linkedListener != null) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            if (gnssDataProvider == this.mGnssMeasurementsProvider || gnssDataProvider == this.mGnssStatusProvider) {
                                LocationUsageLogger locationUsageLogger = this.mLocationUsageLogger;
                                if (gnssDataProvider == this.mGnssMeasurementsProvider) {
                                    i = 2;
                                } else {
                                    i = 3;
                                }
                                locationUsageLogger.logLocationApiUsage(1, i, linkedListener.mCallerIdentity.mPackageName, null, true, false, null, this.mActivityManager.getPackageImportance(linkedListener.mCallerIdentity.mPackageName));
                            }
                            Binder.restoreCallingIdentity(identity);
                            printListenerInfo(binder, gnssDataProvider, linkedListener.mCallerIdentity.mPackageName, false);
                            unlinkFromListenerDeathNotificationLocked(binder, linkedListener);
                            gnssDataProvider.removeListener(listener);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
    }

    private boolean linkToListenerDeathNotificationLocked(IBinder binder, LinkedListenerBase linkedListener) {
        try {
            binder.linkToDeath(linkedListener, 0);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Could not link " + linkedListener.mListenerName + " death callback.", e);
            return false;
        }
    }

    private boolean unlinkFromListenerDeathNotificationLocked(IBinder binder, LinkedListenerBase linkedListener) {
        try {
            binder.unlinkToDeath(linkedListener, 0);
            return true;
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Could not unlink " + linkedListener.mListenerName + " death callback.", e);
            return false;
        }
    }

    public void injectGnssMeasurementCorrections(GnssMeasurementCorrections measurementCorrections, String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to inject GNSS measurement corrections.");
        if (!hasGnssPermissions(packageName)) {
            Slog.e(TAG, "Can not inject GNSS corrections due to no permission.");
            return;
        }
        GnssMeasurementCorrectionsProvider gnssMeasurementCorrectionsProvider = this.mGnssMeasurementCorrectionsProvider;
        if (gnssMeasurementCorrectionsProvider == null) {
            Slog.e(TAG, "Can not inject GNSS corrections. GNSS measurement corrections provider not available.");
        } else {
            gnssMeasurementCorrectionsProvider.injectGnssMeasurementCorrections(measurementCorrections);
        }
    }

    public long getGnssCapabilities(String packageName) {
        GnssCapabilitiesProvider gnssCapabilitiesProvider;
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to obtain GNSS chipset capabilities.");
        if (!hasGnssPermissions(packageName) || (gnssCapabilitiesProvider = this.mGnssCapabilitiesProvider) == null) {
            return -1;
        }
        return gnssCapabilitiesProvider.getGnssCapabilities();
    }

    public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) {
        return addGnssDataListener(listener, packageName, "GnssNavigationMessageListener", this.mGnssNavigationMessageProvider, this.mGnssNavigationMessageListeners, new Consumer() {
            /* class com.android.server.$$Lambda$wg7j1ZorSDGIu2L17I_NmjcwgzQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.removeGnssNavigationMessageListener((IGnssNavigationMessageListener) obj);
            }
        });
    }

    public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) {
        removeGnssDataListener(listener, this.mGnssNavigationMessageProvider, this.mGnssNavigationMessageListeners);
    }

    public boolean sendExtraCommand(String providerName, String command, Bundle extras) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_SENDEXTRACOMMAND);
        if (providerName != null) {
            synchronized (this.mLock) {
                checkResolutionLevelIsSufficientForProviderUseLocked(getCallerAllowedResolutionLevel(), providerName);
                this.mLocationUsageLogger.logLocationApiUsage(0, 5, providerName);
                if (this.mContext.checkCallingOrSelfPermission(ACCESS_LOCATION_EXTRA_COMMANDS) == 0) {
                    HwSystemManager.allowOp(this.mContext, 8);
                    LocationProvider provider = getLocationProviderLocked(providerName);
                    if (provider != null) {
                        provider.sendExtraCommandLocked(command, extras);
                    }
                    this.mLocationUsageLogger.logLocationApiUsage(1, 5, providerName);
                } else {
                    throw new SecurityException("Requires ACCESS_LOCATION_EXTRA_COMMANDS permission");
                }
            }
            return true;
        }
        throw new NullPointerException();
    }

    public boolean sendNiResponse(int notifId, int userResponse) {
        if (Binder.getCallingUid() == Process.myUid()) {
            try {
                return this.mNetInitiatedListener.sendNiResponse(notifId, userResponse);
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in LocationManagerService.sendNiResponse");
                return false;
            }
        } else {
            throw new SecurityException("calling sendNiResponse from outside of the system is not allowed");
        }
    }

    public ProviderProperties getProviderProperties(String providerName) {
        synchronized (this.mLock) {
            checkResolutionLevelIsSufficientForProviderUseLocked(getCallerAllowedResolutionLevel(), providerName);
            LocationProvider provider = getLocationProviderLocked(providerName);
            if (provider == null) {
                return null;
            }
            return provider.getPropertiesLocked();
        }
    }

    public boolean isProviderPackage(String packageName) {
        synchronized (this.mLock) {
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                if (it.next().getPackagesLocked().contains(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setExtraLocationControllerPackage(String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "android.permission.LOCATION_HARDWARE permission required");
        synchronized (this.mLock) {
            this.mExtraLocationControllerPackage = packageName;
        }
    }

    public String getExtraLocationControllerPackage() {
        String str;
        synchronized (this.mLock) {
            str = this.mExtraLocationControllerPackage;
        }
        return str;
    }

    public void setExtraLocationControllerPackageEnabled(boolean enabled) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "android.permission.LOCATION_HARDWARE permission required");
        synchronized (this.mLock) {
            this.mExtraLocationControllerPackageEnabled = enabled;
        }
    }

    public boolean isExtraLocationControllerPackageEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mExtraLocationControllerPackageEnabled && this.mExtraLocationControllerPackage != null;
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLocationEnabled() {
        return isLocationEnabledForUser(this.mCurrentUserId);
    }

    public boolean isLocationEnabledForUser(int userId) {
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Requires INTERACT_ACROSS_USERS permission");
        }
        long identity = Binder.clearCallingIdentity();
        try {
            boolean z = false;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, userId) != 0) {
                z = true;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean isProviderEnabledForUser(String providerName, int userId) {
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Requires INTERACT_ACROSS_USERS permission");
        }
        boolean z = false;
        if ("fused".equals(providerName)) {
            return false;
        }
        synchronized (this.mLock) {
            LocationProvider provider = getLocationProviderLocked(providerName);
            if (provider != null && provider.isUseableForUserLocked(userId)) {
                z = true;
            }
        }
        return z;
    }

    @GuardedBy({"mLock"})
    private static boolean shouldBroadcastSafeLocked(Location loc, Location lastLoc, UpdateRecord record, long now) {
        if (lastLoc == null) {
            return true;
        }
        if ((loc.getElapsedRealtimeNanos() - lastLoc.getElapsedRealtimeNanos()) / NANOS_PER_MILLI < record.mRealRequest.getFastestInterval() - 100) {
            return false;
        }
        double minDistance = (double) record.mRealRequest.getSmallestDisplacement();
        if ((minDistance > 0.0d && ((double) loc.distanceTo(lastLoc)) <= minDistance) || record.mRealRequest.getNumUpdates() <= 0) {
            return false;
        }
        if (record.mRealRequest.getExpireAt() >= now) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r5v2 com.android.server.LocationManagerService$Receiver: [D('receiver' com.android.server.LocationManagerService$Receiver), D('nowNanos' long)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x02cd, code lost:
        if (r11 != 2) goto L_0x02d2;
     */
    @GuardedBy({"mLock"})
    private void handleLocationChangedLocked(Location location, LocationProvider provider) {
        Location noGPSLocation;
        long timeDiffNanos;
        Location notifyLocation;
        Location location2 = location;
        if (!this.mProviders.contains(provider) || !this.mHwQuickTTFFMonitor.checkLocationChanged(location2, provider)) {
            return;
        }
        if (!location.isComplete()) {
            Log.w(TAG, "Dropping incomplete location: " + location2);
            return;
        }
        if (provider.isUseableLocked() && !provider.isPassiveLocked() && !HwDeviceManager.disallowOp(101) && !this.mHwQuickTTFFMonitor.isQuickLocation(location2)) {
            this.mPassiveProvider.updateLocation(location2);
            if (D) {
                Log.d(TAG, "MDM allowed update passive provider");
            }
        }
        long nowNanos = SystemClock.elapsedRealtimeNanos();
        if (!"gps".equals(location.getProvider()) || !this.mHwQuickTTFFMonitor.isQuickLocation(location2)) {
            this.mHwLocationGpsLogServices.updateLocation(location2, nowNanos, provider.getName());
        } else {
            this.mHwLocationGpsLogServices.updateLocation(location2, nowNanos, "quickgps");
        }
        if (D) {
            Log.i(TAG, "incoming location: " + location2);
        }
        updateLocalLocationDB(location2, provider.getName());
        long now = SystemClock.elapsedRealtime();
        if (provider.isUseableLocked()) {
            updateLastLocationLocked(location2, provider.getName());
        }
        Location lastLocationCoarseInterval = this.mLastLocationCoarseInterval.get(provider.getName());
        if (lastLocationCoarseInterval == null) {
            lastLocationCoarseInterval = new Location(location2);
            if (provider.isUseableLocked()) {
                this.mLastLocationCoarseInterval.put(provider.getName(), lastLocationCoarseInterval);
            }
        }
        long timeDiffNanos2 = location.getElapsedRealtimeNanos() - lastLocationCoarseInterval.getElapsedRealtimeNanos();
        if (timeDiffNanos2 > 600000000000L) {
            lastLocationCoarseInterval.set(location2);
        }
        Location noGPSLocation2 = lastLocationCoarseInterval.getExtraLocation("noGPSLocation");
        ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(provider.getName());
        if (records == null) {
            return;
        }
        if (records.size() != 0) {
            Location coarseLocation = null;
            if (noGPSLocation2 != null) {
                coarseLocation = this.mLocationFudger.getOrCreate(noGPSLocation2);
            }
            ArrayList<Receiver> deadReceivers = null;
            ArrayList<UpdateRecord> deadUpdateRecords = null;
            Iterator<UpdateRecord> it = records.iterator();
            while (it.hasNext()) {
                UpdateRecord r = it.next();
                Receiver receiver = r.mReceiver;
                if (provider.isUseableLocked() || isSettingsExemptLocked(r)) {
                    boolean receiverDead = false;
                    int receiverUserId = UserHandle.getUserId(receiver.mCallerIdentity.mUid);
                    if (!isCurrentProfileLocked(receiverUserId)) {
                        timeDiffNanos = timeDiffNanos2;
                        if (!isProviderPackage(receiver.mCallerIdentity.mPackageName)) {
                            if (D) {
                                Log.d(TAG, "skipping loc update for background user " + receiverUserId + " (current user: " + this.mCurrentUserId + ", app: " + receiver.mCallerIdentity.mPackageName + ")");
                                noGPSLocation = noGPSLocation2;
                            } else {
                                noGPSLocation = noGPSLocation2;
                            }
                        }
                    } else {
                        timeDiffNanos = timeDiffNanos2;
                    }
                    if (this.mBlacklist.isBlacklisted(receiver.mCallerIdentity.mPackageName)) {
                        if (D) {
                            Log.d(TAG, "skipping loc update for blacklisted app: " + receiver.mCallerIdentity.mPackageName);
                            noGPSLocation = noGPSLocation2;
                        } else {
                            noGPSLocation = noGPSLocation2;
                        }
                    } else if (!this.mHwQuickTTFFMonitor.isLocationReportToApp(receiver.mCallerIdentity.mPackageName, provider.getName(), location2)) {
                        Log.i(TAG, "skipping qiuckttff loc update for  app: " + receiver.mCallerIdentity.mPackageName);
                        noGPSLocation = noGPSLocation2;
                    } else {
                        if (receiver.mAllowedResolutionLevel < 2) {
                            notifyLocation = coarseLocation;
                        } else {
                            notifyLocation = location;
                        }
                        if (notifyLocation != null) {
                            Location lastLoc = r.mLastFixBroadcast;
                            if (lastLoc == null || shouldBroadcastSafeLocked(notifyLocation, lastLoc, r, now)) {
                                if (lastLoc == null) {
                                    lastLoc = new Location(notifyLocation);
                                    r.mLastFixBroadcast = lastLoc;
                                } else {
                                    lastLoc.set(notifyLocation);
                                }
                                noGPSLocation = noGPSLocation2;
                                if (reportLocationAccessNoThrow(receiver.mCallerIdentity.mPid, receiver.mCallerIdentity.mUid, receiver.mCallerIdentity.mPackageName, receiver.mAllowedResolutionLevel)) {
                                    if (!receiver.callLocationChangedLocked(notifyLocation)) {
                                        Slog.w(TAG, "RemoteException calling onLocationChanged on " + receiver);
                                        receiverDead = true;
                                    }
                                    r.mRealRequest.decrementNumUpdates();
                                } else if (D) {
                                    Log.d(TAG, "skipping loc update for no op app: " + receiver.mCallerIdentity.mPackageName);
                                }
                            } else {
                                noGPSLocation = noGPSLocation2;
                            }
                        } else {
                            noGPSLocation = noGPSLocation2;
                        }
                        if (Settings.Global.getInt(this.mContext.getContentResolver(), "location_disable_status_callbacks", 1) == 0) {
                            long newStatusUpdateTime = provider.getStatusUpdateTimeLocked();
                            Bundle extras = new Bundle();
                            int status = provider.getStatusLocked(extras);
                            long prevStatusUpdateTime = r.mLastStatusBroadcast;
                            gpsLogServicesUploadNlpStatus(provider, extras, status);
                            if (newStatusUpdateTime > prevStatusUpdateTime) {
                                if (prevStatusUpdateTime == 0) {
                                }
                                r.mLastStatusBroadcast = newStatusUpdateTime;
                                if (!receiver.callStatusChangedLocked(provider.getName(), status, extras)) {
                                    receiverDead = true;
                                    Slog.w(TAG, "RemoteException calling onStatusChanged on " + receiver);
                                }
                            }
                        }
                        if (r.mRealRequest.getNumUpdates() <= 0 || r.mRealRequest.getExpireAt() < now) {
                            if (deadUpdateRecords == null) {
                                deadUpdateRecords = new ArrayList<>();
                            }
                            deadUpdateRecords.add(r);
                        }
                        if (receiverDead) {
                            if (deadReceivers == null) {
                                deadReceivers = new ArrayList<>();
                            }
                            if (!deadReceivers.contains(receiver)) {
                                deadReceivers.add(receiver);
                            }
                        }
                        location2 = location;
                        lastLocationCoarseInterval = lastLocationCoarseInterval;
                        nowNanos = nowNanos;
                        timeDiffNanos2 = timeDiffNanos;
                        noGPSLocation2 = noGPSLocation;
                    }
                } else {
                    timeDiffNanos = timeDiffNanos2;
                    noGPSLocation = noGPSLocation2;
                }
                location2 = location;
                lastLocationCoarseInterval = lastLocationCoarseInterval;
                nowNanos = nowNanos;
                timeDiffNanos2 = timeDiffNanos;
                noGPSLocation2 = noGPSLocation;
            }
            updateGnssSourceType(location, provider);
            if (deadReceivers != null) {
                Iterator<Receiver> it2 = deadReceivers.iterator();
                while (it2.hasNext()) {
                    removeUpdatesLocked(it2.next());
                }
            }
            if (deadUpdateRecords != null) {
                Iterator<UpdateRecord> it3 = deadUpdateRecords.iterator();
                while (it3.hasNext()) {
                    it3.next().disposeLocked(true);
                }
                applyRequirementsLocked(provider);
            }
        }
    }

    private void updateGnssSourceType(Location location, LocationProvider provider) {
        if ("gps".equals(location.getProvider()) && !this.mHwQuickTTFFMonitor.isQuickLocation(location) && "gps".equals(provider.getName()) && location.getExtras() != null) {
            int[] iArr = this.mLastGnssSourceType;
            iArr[1] = iArr[0];
            iArr[0] = location.getExtras().getInt("SourceType");
            if (D) {
                Log.i(TAG, "update gnss source type, index 0 = " + this.mLastGnssSourceType[0] + ", index 1 = " + this.mLastGnssSourceType[1]);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void updateLastLocationLocked(Location location, String provider) {
        Location noGPSLocation = location.getExtraLocation("noGPSLocation");
        Location lastLocation = this.mLastLocation.get(provider);
        if (lastLocation == null) {
            lastLocation = new Location(provider);
            this.mLastLocation.put(provider, lastLocation);
        } else {
            Location lastNoGPSLocation = lastLocation.getExtraLocation("noGPSLocation");
            if (noGPSLocation == null && lastNoGPSLocation != null) {
                location.setExtraLocation("noGPSLocation", lastNoGPSLocation);
            }
        }
        lastLocation.set(location);
    }

    public boolean geocoderIsPresent() {
        return this.mGeocodeProvider != null;
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        GeocoderProxy geocoderProxy = this.mGeocodeProvider;
        if (geocoderProxy != null) {
            return geocoderProxy.getFromLocation(latitude, longitude, maxResults, params, addrs);
        }
        return null;
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        GeocoderProxy geocoderProxy = this.mGeocodeProvider;
        if (geocoderProxy != null) {
            return geocoderProxy.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
        return null;
    }

    private boolean canCallerAccessMockLocation(String opPackageName) {
        return this.mAppOps.checkOp(58, Binder.getCallingUid(), opPackageName) == 0;
    }

    public void addTestProvider(String name, ProviderProperties properties, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            if (!"passive".equals(name)) {
                synchronized (this.mLock) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        LocationProvider oldProvider = getLocationProviderLocked(name);
                        if (oldProvider != null) {
                            if (!oldProvider.isMock()) {
                                removeProviderLocked(oldProvider);
                            } else {
                                throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
                            }
                        }
                        Log.i(TAG, "addTestProvider " + name + " opPackageName " + opPackageName);
                        MockLocationProvider mockProviderManager = new MockLocationProvider(name);
                        addProviderLocked(mockProviderManager);
                        mockProviderManager.attachLocked(new MockProvider(this.mContext, mockProviderManager, properties));
                        notifyMockPosition(ADD_MOCK, opPackageName);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Cannot mock the passive location provider");
        }
    }

    public void removeTestProvider(String name, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                long identity = Binder.clearCallingIdentity();
                try {
                    LocationProvider testProvider = getLocationProviderLocked(name);
                    if (testProvider == null || !testProvider.isMock()) {
                        throw new IllegalArgumentException("Provider \"" + name + "\" unknown");
                    }
                    removeProviderLocked(testProvider);
                    Log.i(TAG, "removeTestProvider " + name + " opPackageName " + opPackageName);
                    LocationProvider realProvider = null;
                    Iterator<LocationProvider> it = this.mRealProviders.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        LocationProvider provider = it.next();
                        if (name.equals(provider.getName())) {
                            realProvider = provider;
                            break;
                        }
                    }
                    if (realProvider != null) {
                        addProviderLocked(realProvider);
                    }
                    notifyMockPosition(REMOVE_MOCK, opPackageName);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void setTestProviderLocation(String providerName, Location location, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                LocationProvider testProvider = getLocationProviderLocked(providerName);
                if (testProvider == null || !testProvider.isMock()) {
                    throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
                }
                String locationProvider = location.getProvider();
                if (!TextUtils.isEmpty(locationProvider) && !providerName.equals(locationProvider)) {
                    EventLog.writeEvent(1397638484, "33091107", Integer.valueOf(Binder.getCallingUid()), providerName + "!=" + location.getProvider());
                }
                ((MockLocationProvider) testProvider).setLocationLocked(location);
            }
        }
    }

    public void setTestProviderEnabled(String providerName, boolean enabled, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                LocationProvider testProvider = getLocationProviderLocked(providerName);
                if (testProvider == null || !testProvider.isMock()) {
                    throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
                }
                Log.i(TAG, "setTestProviderEnabled " + providerName + " enabled " + enabled);
                ((MockLocationProvider) testProvider).setEnabledLocked(enabled);
            }
        }
    }

    public void setTestProviderStatus(String providerName, int status, Bundle extras, long updateTime, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                LocationProvider testProvider = getLocationProviderLocked(providerName);
                if (testProvider == null || !testProvider.isMock()) {
                    throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
                }
                ((MockLocationProvider) testProvider).setStatusLocked(status, extras, updateTime);
            }
        }
    }

    public List<LocationRequest> getTestProviderCurrentRequests(String providerName, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return Collections.emptyList();
        }
        synchronized (this.mLock) {
            LocationProvider testProvider = getLocationProviderLocked(providerName);
            if (testProvider == null || !testProvider.isMock()) {
                throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
            }
            MockLocationProvider provider = (MockLocationProvider) testProvider;
            if (provider.mCurrentRequest == null) {
                return Collections.emptyList();
            }
            List<LocationRequest> requests = new ArrayList<>();
            for (LocationRequest request : provider.mCurrentRequest.locationRequests) {
                requests.add(new LocationRequest(request));
            }
            return requests;
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                if (args.length <= 0 || !args[0].equals("--gnssmetrics")) {
                    pw.println("Current Location Manager state:");
                    pw.print("  Current System Time: " + TimeUtils.logTimeOfDay(System.currentTimeMillis()));
                    pw.println(", Current Elapsed Time: " + TimeUtils.formatDuration(SystemClock.elapsedRealtime()));
                    pw.println("  Current user: " + this.mCurrentUserId + " " + Arrays.toString(this.mCurrentUserProfiles));
                    StringBuilder sb = new StringBuilder();
                    sb.append("  Location mode: ");
                    sb.append(isLocationEnabled());
                    pw.println(sb.toString());
                    pw.println("  Battery Saver Location Mode: " + PowerManager.locationPowerSaveModeToString(this.mBatterySaverMode));
                    pw.println("  Location Listeners:");
                    Iterator<Receiver> it = this.mReceivers.values().iterator();
                    while (it.hasNext()) {
                        pw.println("    " + it.next());
                    }
                    pw.println("  Active Records by Provider:");
                    for (Map.Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
                        pw.println("    " + entry.getKey() + ":");
                        Iterator<UpdateRecord> it2 = entry.getValue().iterator();
                        while (it2.hasNext()) {
                            pw.println("      " + it2.next());
                        }
                    }
                    pw.println("  Active GnssMeasurement Listeners:");
                    dumpGnssDataListenersLocked(pw, this.mGnssMeasurementsListeners);
                    pw.println("  Active GnssNavigationMessage Listeners:");
                    dumpGnssDataListenersLocked(pw, this.mGnssNavigationMessageListeners);
                    pw.println("  Active GnssStatus Listeners:");
                    dumpGnssDataListenersLocked(pw, this.mGnssStatusListeners);
                    pw.println("  Historical Records by Provider:");
                    for (Map.Entry<LocationRequestStatistics.PackageProviderKey, LocationRequestStatistics.PackageStatistics> entry2 : this.mRequestStatistics.statistics.entrySet()) {
                        LocationRequestStatistics.PackageProviderKey key = entry2.getKey();
                        pw.println("    " + key.packageName + ": " + key.providerName + ": " + entry2.getValue());
                    }
                    pw.println("  Last Known Locations:");
                    for (Map.Entry<String, Location> entry3 : this.mLastLocation.entrySet()) {
                        pw.println("    " + entry3.getKey() + ": " + entry3.getValue());
                    }
                    pw.println("  Last Known Locations Coarse Intervals:");
                    for (Map.Entry<String, Location> entry4 : this.mLastLocationCoarseInterval.entrySet()) {
                        pw.println("    " + entry4.getKey() + ": " + entry4.getValue());
                    }
                    if (this.mGeofenceManager != null) {
                        this.mGeofenceManager.dump(pw);
                    } else {
                        pw.println("  Geofences: null");
                    }
                    if (this.mBlacklist != null) {
                        pw.append("  ");
                        this.mBlacklist.dump(pw);
                    } else {
                        pw.println("  mBlacklist=null");
                    }
                    if (this.mExtraLocationControllerPackage != null) {
                        pw.println(" Location controller extra package: " + this.mExtraLocationControllerPackage + " enabled: " + this.mExtraLocationControllerPackageEnabled);
                    }
                    if (!this.mBackgroundThrottlePackageWhitelist.isEmpty()) {
                        pw.println("  Throttling Whitelisted Packages:");
                        Iterator<String> it3 = this.mBackgroundThrottlePackageWhitelist.iterator();
                        while (it3.hasNext()) {
                            pw.println("    " + it3.next());
                        }
                    }
                    if (!this.mIgnoreSettingsPackageWhitelist.isEmpty()) {
                        pw.println("  Bypass Whitelisted Packages:");
                        Iterator<String> it4 = this.mIgnoreSettingsPackageWhitelist.iterator();
                        while (it4.hasNext()) {
                            pw.println("    " + it4.next());
                        }
                    }
                    if (this.mLocationFudger != null) {
                        pw.append("  fudger: ");
                        this.mLocationFudger.dump(fd, pw, args);
                    } else {
                        pw.println("  fudger: null");
                    }
                    if (args.length <= 0 || !"short".equals(args[0])) {
                        Iterator<LocationProvider> it5 = this.mProviders.iterator();
                        while (it5.hasNext()) {
                            it5.next().dumpLocked(fd, pw, args);
                        }
                        if (this.mGnssBatchingInProgress) {
                            pw.println("  GNSS batching in progress");
                        }
                        hwLocationPowerTrackerDump(pw);
                        HwServiceFactory.getGpsFreezeProc().dump(pw);
                        return;
                    }
                    return;
                }
                if (this.mGnssMetricsProvider != null) {
                    pw.append((CharSequence) this.mGnssMetricsProvider.getGnssMetricsAsProtoString());
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void dumpGnssDataListenersLocked(PrintWriter pw, ArrayMap<IBinder, ? extends LinkedListenerBase> gnssDataListeners) {
        for (LinkedListenerBase listener : gnssDataListeners.values()) {
            CallerIdentity callerIdentity = listener.mCallerIdentity;
            pw.println("    " + callerIdentity.mPid + " " + callerIdentity.mUid + " " + callerIdentity.mPackageName + ": " + isThrottlingExemptLocked(callerIdentity));
        }
    }

    private void gpsLogServicesUploadNlpStatus(LocationProvider provider, Bundle extras, int status) {
        if ("network".equals(provider.getName())) {
            ArrayList<Integer> statusList = extras.getIntegerArrayList("status");
            if (statusList != null) {
                Iterator<Integer> it = statusList.iterator();
                while (it.hasNext()) {
                    Integer nlpStatus = it.next();
                    Log.d(TAG, "list network LocationChanged,  NLP status: " + nlpStatus + " , provider : " + provider);
                    this.mHwLocationGpsLogServices.updateNLPStatus(nlpStatus.intValue());
                }
                return;
            }
            Log.d(TAG, " network LocationChanged,  NLP status: " + status + " , provider : " + provider);
            this.mHwLocationGpsLogServices.updateNLPStatus(status);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isExistReceiver(String packageName) {
        if (packageName == null) {
            return false;
        }
        for (Receiver receiver : this.mReceivers.values()) {
            if (packageName.equals(receiver.mCallerIdentity.mPackageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExistReceiver(int uid) {
        for (Receiver receiver : this.mReceivers.values()) {
            if (receiver.mCallerIdentity.mUid == uid) {
                return true;
            }
        }
        return false;
    }

    private <TListener extends IInterface> void printListenerInfo(IBinder binder, RemoteListenerHelper<TListener> gnssDataProvider, String packageName, boolean isAdd) {
        if (gnssDataProvider == this.mGnssMeasurementsProvider) {
            StringBuilder sb = new StringBuilder();
            sb.append(isAdd ? ADD_MOCK : REMOVE_MOCK);
            sb.append("GnssMeasurementsListener : ");
            sb.append(Integer.toHexString(System.identityHashCode(binder)));
            sb.append(" packageName ");
            sb.append(packageName);
            Log.i(TAG, sb.toString());
        }
    }

    private boolean checkValid(Location location) {
        if (location != null && "gps".equals(location.getProvider())) {
            return true;
        }
        return false;
    }

    private Location dealHdExtras(Location location, int sourceType, float hdAcc) {
        float bias;
        Location hdLocation = new Location(location);
        if (hdAcc > 0.0f) {
            hdLocation.setAccuracy(hdAcc);
        }
        if (hdAcc > 0.0f && hdAcc < 2.0f) {
            if ((sourceType & 32) == 32) {
                int[] iArr = this.mLastGnssSourceType;
                if ((iArr[0] & 8) == 0) {
                    bias = hdAcc / 2.0f;
                } else if ((iArr[1] & 8) == 0) {
                    bias = hdAcc;
                } else {
                    bias = 2.0f * hdAcc;
                }
            } else {
                int[] iArr2 = this.mLastGnssSourceType;
                if ((iArr2[0] & 8) == 0) {
                    bias = hdAcc / 4.0f;
                } else if ((iArr2[1] & 8) == 0) {
                    bias = hdAcc / 2.0f;
                } else {
                    bias = hdAcc;
                }
            }
            double tempHeading = (45.0d * 3.141592653589793d) / 180.0d;
            double parameterLat = location.getLatitude();
            if (parameterLat > 85.0d) {
                parameterLat = 85.0d;
            }
            if (parameterLat < -85.0d) {
                parameterLat = -85.0d;
            }
            double offsetLong = (((double) bias) * Math.sin(tempHeading)) / (Math.cos((parameterLat * 3.141592653589793d) / 180.0d) * 6378137.0d);
            double offsetLat = (((double) bias) * Math.cos(tempHeading)) / 6378137.0d;
            hdLocation.setLongitude(location.getLongitude() - ((offsetLong * 180.0d) / 3.141592653589793d));
            hdLocation.setLatitude(location.getLatitude() - ((offsetLat * 180.0d) / 3.141592653589793d));
            if (D) {
                Log.d(TAG, " hd recovery,  " + ((offsetLong * 180.0d) / 3.141592653589793d) + "," + ((180.0d * offsetLat) / 3.141592653589793d));
            }
        }
        return hdLocation;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Location putHdExtras(String packageName, Location location) {
        Bundle locBundle;
        if (!checkValid(location) || (locBundle = location.getExtras()) == null) {
            return location;
        }
        int sourceType = locBundle.getInt("SourceType");
        float hdAcc = locBundle.getFloat("HDACC");
        locBundle.remove("HDACC");
        location.setExtras(locBundle);
        if ((sourceType & 8) != 8) {
            return location;
        }
        String hmsPackageName = getHmsPackageName();
        boolean isHms = true;
        if (hmsPackageName != null && !"".equals(hmsPackageName) && hmsPackageName.equals(packageName)) {
            isHms = false;
        }
        if (!HMS_PACKAGENAME.equals(packageName) && !LBS_PACKAGENAME.equals(packageName) && !WATCH_HMS_PACKAGENAME.equals(packageName) && isHms) {
            return location;
        }
        location.setExtraLocation(HD_LOCATION, dealHdExtras(location, sourceType, hdAcc));
        return location;
    }

    private String getHmsPackageName() {
        Context context = this.mContext;
        if (context == null) {
            Log.w(TAG, "mContext is null");
            return "";
        }
        String hmsPackageName = Settings.Global.getString(context.getContentResolver(), "hms_package_name");
        if (hmsPackageName == null || "".equals(hmsPackageName)) {
            return "";
        }
        return hmsPackageName;
    }
}
