package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.IGnssMeasurementsListener;
import android.location.ILocationListener;
import android.location.Location;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.internal.content.PackageMonitor;
import com.android.server.LocationManagerService;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeocoderProxyUtils;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.LBSLog;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationProviderProxyUtils;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LocationManagerServiceUtil {
    private static final String CHINA_SHORT_STR = "CN";
    private static final String COUNTRY_CODE_CHINA = "460";
    private static final int COUNTRY_CODE_CHINA_LENGTH = 2;
    private static final String COUNTRY_CODE_DEFAULT = "99999";
    private static final int COUNTRY_CODE_DEFAULT_LENGTH = 5;
    private static final String COUNTRY_CODE_NONE = "";
    private static final int COUNTRY_CODE_VERSION = 10;
    private static final int DEFAULT_SIZE = 16;
    private static final String FUSEDPROXY_SERVICE_ACTION = "com.android.location.service.FusedProvider";
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    private static final String GEOCODE_SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String GEOFENCE_SERVICE_ACTION = "com.android.location.service.GeofenceProvider";
    private static final String GMS_VERSION = SystemProperties.get("ro.com.google.gmsversion", "");
    public static final String GOOGLE_GMS_PROCESS = "com.google.android.gms";
    public static final String GOOGLE_GMS_UI_PROCESS = "com.google.android.gms.ui";
    public static final String GOOGLE_MAP_PROCESS = "com.google.android.apps.maps";
    private static final String GOOGLE_MAP_STATE = "google_map_state";
    public static final String GOOGLE_NETWORK_PROCESS = "com.google.process.location";
    private static final String HMS_PACAKE_NAME = "com.huawei.hwid";
    private static final String LOCALE_REGION = SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, "");
    private static final int MAP_NAVIGATING_APP = 3;
    private static final String SHOW_GOOGLE_NLP = "sys.show_google_nlp";
    public static final int STATE_INSTALLED_ONLY = 0;
    public static final int STATE_INSTALLED_RPOMPTED = 1;
    public static final int STATE_UNINSTALLED = 2;
    private static final String TAG = "LocationManagerServiceUtil";
    private static final String WATCH_HMS_PACAKE_NAME = "com.huawei.hms";
    private static final List<String> WHITE_LIST = new ArrayList(Arrays.asList(PackageManagerServiceEx.PLATFORM_PACKAGE_NAME, "com.android.location.fused"));
    private static GeocoderProxyUtils geocoderProxyUtilsUtils = EasyInvokeFactory.getInvokeUtils(GeocoderProxyUtils.class);
    private static volatile LocationManagerServiceUtil instance;
    private static LocationProviderProxyUtils locationProviderProxyUtils = EasyInvokeFactory.getInvokeUtils(LocationProviderProxyUtils.class);
    private static int pidGoogleLocation = -99;
    private static LocationManagerServiceUtils utils = EasyInvokeFactory.getInvokeUtils(LocationManagerServiceUtils.class);
    private ActivityManager am = null;
    public int googleMapState = 2;
    private boolean isMultiNlpEnableFlag = false;
    private boolean isMultiNlpEnableLoaded = false;
    private Context mContext = null;
    private ConnectivityManager.NetworkCallback mNetworkConnectivityCallback = null;
    private String mNlpCivil;
    private String mNlpForeign = GOOGLE_GMS_PROCESS;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* class com.android.server.LocationManagerServiceUtil.AnonymousClass1 */

        public void onPackageAdded(String packageName, int uid) {
            if (LocationManagerServiceUtil.GOOGLE_MAP_PROCESS.equals(packageName)) {
                LBSLog.d(LocationManagerServiceUtil.TAG, false, "onPackageAdded %{public}s", packageName);
                LocationManagerServiceUtil.this.setGoogleMapState(0);
            }
            LocationManagerServiceUtil.super.onPackageAdded(packageName, uid);
        }

        public void onPackageRemoved(String packageName, int uid) {
            if (LocationManagerServiceUtil.GOOGLE_MAP_PROCESS.equals(packageName)) {
                LBSLog.d(LocationManagerServiceUtil.TAG, false, "onPackageRemoved %{public}s", packageName);
                LocationManagerServiceUtil.this.shouldMonitorGmsProcess = false;
                LocationManagerServiceUtil.this.setGoogleMapState(2);
            }
            LocationManagerServiceUtil.super.onPackageRemoved(packageName, uid);
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private Location mRealLastGpsLocation;
    private Location mRealLastNlpLocation;
    private LocationManagerService mService;
    private TelephonyManager mTelephonyManager;
    private int[] navigationAppTypes = {2, 3, 22, 23, 24, 11};
    private ConcurrentHashMap<Network, NetworkCapabilities> networkMap = new ConcurrentHashMap<>();
    private boolean shouldMonitorGmsProcess = false;
    public boolean skipGooglePrompt = true;
    public boolean useGoogleNlpNow = false;

    private LocationManagerServiceUtil(LocationManagerService service, Context context) {
        this.mService = service;
        this.mContext = context;
        SystemProperties.set(SHOW_GOOGLE_NLP, AppActConstant.VALUE_TRUE);
        if (CHINA_SHORT_STR.equalsIgnoreCase(LOCALE_REGION)) {
            SystemProperties.set(SHOW_GOOGLE_NLP, AppActConstant.VALUE_FALSE);
            this.googleMapState = getGoogleMapState();
            this.mPackageMonitor.register(this.mContext, (Looper) null, UserHandle.ALL, true);
            this.mPhoneStateListener = new PhoneStateListener() {
                /* class com.android.server.LocationManagerServiceUtil.AnonymousClass2 */

                @Override // android.telephony.PhoneStateListener
                public void onServiceStateChanged(ServiceState state) {
                    if (state != null) {
                        LocationManagerServiceUtil.this.serviceStateChanged(state);
                    }
                }
            };
            if (isMultiNlpEnable()) {
                this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
                this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
            }
        }
        this.am = (ActivityManager) this.mContext.getSystemService("activity");
        this.mNetworkConnectivityCallback = createNetworkConnectivityCallback();
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(this.mNetworkConnectivityCallback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void serviceStateChanged(ServiceState state) {
        String numeric = state.getOperatorNumeric();
        LBSLog.d(TAG, false, "ServiceStateChanged numeric = %{private}s", numeric);
        if (numeric != null && numeric.length() >= 5 && COUNTRY_CODE_DEFAULT.equals(numeric.substring(0, 5))) {
            LBSLog.d(TAG, false, "ServiceStateChanged numeric 99999 ", new Object[0]);
        } else if (numeric != null && numeric.length() >= 2 && "460".equals(numeric.substring(0, 2))) {
            String pkgName = this.mNlpCivil;
            LBSLog.d(TAG, false, "ServiceStateChanged use pkgName = %{public}s", pkgName);
            this.useGoogleNlpNow = false;
            this.skipGooglePrompt = true;
            SystemProperties.set(SHOW_GOOGLE_NLP, AppActConstant.VALUE_FALSE);
            bindSpecificService(pkgName, 10, false);
        } else if (numeric == null || "".equals(numeric)) {
            LBSLog.d(TAG, false, "ServiceStateChanged last else", new Object[0]);
        } else {
            String pkgName2 = this.mNlpCivil;
            LBSLog.d(TAG, false, "ServiceStateChanged use pkgName = %{public}s", pkgName2);
            this.useGoogleNlpNow = false;
            this.skipGooglePrompt = false;
            SystemProperties.set(SHOW_GOOGLE_NLP, AppActConstant.VALUE_FALSE);
            bindSpecificService(pkgName2, 10, false);
        }
    }

    public static LocationManagerServiceUtil getDefault(LocationManagerService service, Context context) {
        if (instance == null) {
            instance = new LocationManagerServiceUtil(service, context);
        }
        return instance;
    }

    public static LocationManagerServiceUtil getDefault() {
        return instance;
    }

    public static void setPidGoogleLocation(int pid, String processName) {
        if (GOOGLE_NETWORK_PROCESS.equals(processName)) {
            LBSLog.d(TAG, false, "setPidGoogleLocation pid = %{public}d", Integer.valueOf(pid));
            pidGoogleLocation = pid;
        } else if (GOOGLE_MAP_PROCESS.equals(processName) && getDefault() != null && getDefault().isMultiNlpEnable() && getDefault().googleMapState == 0) {
            LBSLog.d(TAG, false, "setPidGoogleLocation processName = %{public}s", processName);
            if (getDefault().isGmsProcessRunning()) {
                getDefault().sendProvidersChangedAction();
            } else {
                getDefault().shouldMonitorGmsProcess = true;
            }
        } else if (!GOOGLE_GMS_PROCESS.equals(processName) || getDefault() == null || !getDefault().isMultiNlpEnable() || !getDefault().shouldMonitorGmsProcess) {
            LBSLog.d(TAG, false, "setPidGoogleLocation last else", new Object[0]);
        } else {
            LBSLog.d(TAG, false, "setPidGoogleLocation processName = %{public}s", processName);
            getDefault().sendProvidersChangedAction();
            getDefault().shouldMonitorGmsProcess = false;
        }
    }

    private boolean isGmsProcessRunning() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        ActivityManager activityManager = null;
        Object activityManagerObject = this.mContext.getSystemService("activity");
        if (activityManagerObject instanceof ActivityManager) {
            activityManager = (ActivityManager) activityManagerObject;
        }
        if (activityManager == null || (appProcessList = activityManager.getRunningAppProcesses()) == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (GOOGLE_GMS_PROCESS.equals(appProcess.processName)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGoogleMapState(int state) {
        LBSLog.d(TAG, false, "setGoogleMapState. %{public}d", Integer.valueOf(state));
        Settings.System.putInt(this.mContext.getContentResolver(), GOOGLE_MAP_STATE, state);
        this.googleMapState = state;
    }

    private int getGoogleMapState() {
        int state = Settings.System.getInt(this.mContext.getContentResolver(), GOOGLE_MAP_STATE, 2);
        LBSLog.d(TAG, false, "getGoogleMapState. %{public}d", Integer.valueOf(state));
        return state;
    }

    private void sendProvidersChangedAction() {
        LBSLog.d(TAG, false, "sendProvidersChangedAction.", new Object[0]);
        setGoogleMapState(1);
        if (Settings.Secure.getInt(getDefault().mContext.getContentResolver(), "location_mode", 0) != 0) {
            this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
            this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
        }
    }

    public ArrayList<LocationManagerService.LocationProvider> getRealProviders() {
        return utils.getRealProviders(this.mService);
    }

    public Handler getLocationHandler() {
        return utils.getLocationHandler(this.mService);
    }

    public void setLocationHandler(Handler handler) {
        utils.setLocationHandler(this.mService, handler);
    }

    public Object getmLock() {
        return utils.getmLock(this.mService);
    }

    public HashMap<Object, LocationManagerService.Receiver> getReceivers() {
        return utils.getReceivers(this.mService);
    }

    public void removeUpdatesLocked(LocationManagerService.Receiver receiver) {
        utils.removeUpdatesLocked(this.mService, receiver);
    }

    private ArrayList<LocationProviderProxy> getProxyProviders() {
        return utils.getProxyProviders(this.mService);
    }

    public void addProviderLocked(LocationManagerService.LocationProvider provider) {
        utils.addProviderLocked(this.mService, provider);
    }

    private void setGeocodeProvider(GeocoderProxy geocodeProvider) {
        utils.setGeocodeProvider(this.mService, geocodeProvider);
    }

    private GeocoderProxy getGeocoderProvider() {
        return utils.getGeocodeProvider(this.mService);
    }

    private ServiceWatcher getLocationProviderProxyWatcher(LocationProviderProxy locationProviderProxy) {
        return locationProviderProxyUtils.getServiceWatcher(locationProviderProxy);
    }

    private ServiceWatcher getgetGeocoderServiceWatcher(GeocoderProxy geocoderProxy) {
        return geocoderProxyUtilsUtils.getServiceWatcher(geocoderProxy);
    }

    private void updateProvidersLocked() {
        utils.updateProvidersLocked(this.mService);
    }

    public boolean isMockProvider(String provider) {
        return utils.isMockProvider(this.mService, provider).booleanValue();
    }

    public HashMap<String, LocationManagerService.LocationProvider> getProvidersByName() {
        return utils.getProvidersByName(this.mService);
    }

    public ArrayList<LocationManagerService.LocationProvider> getProviders() {
        return utils.getProviders(this.mService);
    }

    public HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> getRecordsByProvider() {
        return utils.getRecordsByProvider(this.mService);
    }

    public boolean isProviderEnabledForUser(String providerName, int userId) {
        return utils.isProviderEnabledForUser(this.mService, providerName, userId).booleanValue();
    }

    public final boolean isMultiNlpEnable() {
        if (!this.isMultiNlpEnableLoaded) {
            if (CHINA_SHORT_STR.equalsIgnoreCase(LOCALE_REGION)) {
                PackageInfo foreignPackage = null;
                PackageInfo civilPackage = null;
                try {
                    foreignPackage = this.mContext.getPackageManager().getPackageInfo(this.mNlpForeign, 4);
                } catch (PackageManager.NameNotFoundException e) {
                    LBSLog.e(TAG, false, "missing foreignPackage", new Object[0]);
                } catch (Exception e2) {
                    LBSLog.e(TAG, false, "foreignPackage exception", new Object[0]);
                }
                this.mNlpCivil = SystemProperties.get("ro.config.hw_nlp", "com.baidu.map.location");
                try {
                    civilPackage = this.mContext.getPackageManager().getPackageInfo(this.mNlpCivil, 4);
                } catch (PackageManager.NameNotFoundException e3) {
                    LBSLog.e(TAG, false, "missing mNlpCivil: %{public}s", this.mNlpCivil);
                } catch (Exception e4) {
                    LBSLog.e(TAG, false, "mNlpCivil exception", new Object[0]);
                }
                if (!(TextUtils.isEmpty(this.mNlpCivil) || foreignPackage == null || civilPackage == null)) {
                    this.isMultiNlpEnableFlag = true;
                }
            }
            this.isMultiNlpEnableLoaded = true;
            LBSLog.e(TAG, false, "isMultiNlpEnable: isMultiNlpEnableFlag = %{public}b", Boolean.valueOf(this.isMultiNlpEnableFlag));
        }
        return this.isMultiNlpEnableFlag;
    }

    public static boolean useCivilNlpPackage(String action, String packageName) {
        if ((!GEOFENCE_SERVICE_ACTION.equals(action) && !FUSED_LOCATION_SERVICE_ACTION.equals(action) && !FUSEDPROXY_SERVICE_ACTION.equals(action)) || getDefault() == null || !getDefault().isMultiNlpEnable()) {
            return false;
        }
        if (getDefault().useGoogleNlpNow) {
            if (Objects.equals(getDefault().mNlpForeign, packageName)) {
                return true;
            }
        } else if (getDefault().mNlpCivil != null && Objects.equals(getDefault().mNlpCivil, packageName)) {
            return true;
        }
        return false;
    }

    public static boolean skipForeignNlpPackage(String action, String packageName) {
        if (GEOCODE_SERVICE_ACTION.equals(action) && isChineseVersion() && (HMS_PACAKE_NAME.equals(packageName) || WATCH_HMS_PACAKE_NAME.equals(packageName))) {
            return true;
        }
        if ((GEOFENCE_SERVICE_ACTION.equals(action) || FUSED_LOCATION_SERVICE_ACTION.equals(action) || FUSEDPROXY_SERVICE_ACTION.equals(action)) && getDefault() != null && getDefault().isMultiNlpEnable() && !getDefault().useGoogleNlpNow && Objects.equals(packageName, getDefault().mNlpForeign)) {
            return true;
        }
        return false;
    }

    private void bindSpecificService(String packageName, int version, boolean bind) {
    }

    private void bindSpecificService(ServiceWatcher serviceWatcher, String packageName, int version) {
        if (serviceWatcher != null) {
            if (Objects.equals(packageName, serviceWatcher.getCurrentPackageName())) {
                LBSLog.d(TAG, false, "bindSpecificService: same package name, return", new Object[0]);
                return;
            }
            LBSLog.d(TAG, false, "bindSpecificService old mPackageName = %{public}s", serviceWatcher.getCurrentPackageName());
            String currentPackageName = serviceWatcher.getCurrentPackageName();
            if (Objects.equals(this.mNlpCivil, currentPackageName)) {
                LBSLog.d(TAG, false, "currentPackageName is civil, force Stop", new Object[0]);
                this.am.forceStopPackage(currentPackageName);
            }
            LBSLog.d(TAG, false, "bindSpecificService new packageName = %{public}s", packageName);
        }
    }

    public static boolean shouldSkipGoogleNlp(int pid) {
        if (pid == pidGoogleLocation) {
            if (getDefault() == null || !getDefault().isMultiNlpEnable() || !getDefault().skipGooglePrompt || getDefault().googleMapState == 1) {
                LBSLog.w(TAG, false, "shouldSkipGoogleNlp return false skip process ", Integer.valueOf(pid));
            } else {
                LBSLog.w(TAG, false, "skip process %{public}d", Integer.valueOf(pid));
                return true;
            }
        }
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void clearPackageLocation(String packageName) {
        synchronized (getmLock()) {
            ArrayList<LocationManagerService.Receiver> deadReceivers = null;
            for (LocationManagerService.Receiver receiver : getReceivers().values()) {
                if (Objects.equals(receiver.mCallerIdentity.mPackageName, packageName)) {
                    if (deadReceivers == null) {
                        deadReceivers = new ArrayList<>(16);
                    }
                    deadReceivers.add(receiver);
                }
            }
            if (deadReceivers != null) {
                Iterator<LocationManagerService.Receiver> it = deadReceivers.iterator();
                while (it.hasNext()) {
                    removeUpdatesLocked(it.next());
                }
            }
        }
    }

    public void applyRequirementsLocked(String provider) {
        utils.applyRequirementsLocked(this.mService, provider);
    }

    public int getCurrentUserId() {
        return utils.getCurrentUserId(this.mService);
    }

    public ArrayMap<IBinder, LocationManagerService.LinkedListener<IGnssMeasurementsListener>> getGnssMeasurementsListeners() {
        return utils.getGnssMeasurementsListeners(this.mService);
    }

    public GnssMeasurementsProvider getGnssMeasurementsProvider() {
        return utils.getGnssMeasurementsProvider(this.mService);
    }

    public void onBackgroundThrottleWhitelistChangedLocked() {
        utils.onBackgroundThrottleWhitelistChangedLocked(this.mService);
    }

    public void setRealLastLocation(Location location, String provider) {
        if ("gps".equals(provider)) {
            this.mRealLastGpsLocation = new Location(location);
        }
        if ("network".equals(provider)) {
            this.mRealLastNlpLocation = new Location(location);
        }
    }

    public Location getRealLastLocation(String provider) {
        if ("gps".equals(provider)) {
            return this.mRealLastGpsLocation;
        }
        if ("network".equals(provider)) {
            return this.mRealLastNlpLocation;
        }
        return null;
    }

    public boolean shouldEnterIdle() {
        boolean shouldEnterIdle = true;
        synchronized (getmLock()) {
            ArrayList<LocationManagerService.UpdateRecord> gpsRecords = getRecordsByProvider().get("gps");
            if (gpsRecords != null) {
                if (!gpsRecords.isEmpty()) {
                    int recordSize = gpsRecords.size();
                    int index = 0;
                    while (true) {
                        if (index >= recordSize) {
                            break;
                        } else if (isNavigatingApp(gpsRecords.get(index).mReceiver.mCallerIdentity.mPackageName)) {
                            shouldEnterIdle = false;
                            LBSLog.i(TAG, false, "find navigation app, should not enter idle", new Object[0]);
                            break;
                        } else {
                            index++;
                        }
                    }
                    return shouldEnterIdle;
                }
            }
            LBSLog.i(TAG, false, "gps Records is null or empty", new Object[0]);
            return true;
        }
    }

    private boolean isNavigatingApp(String packageName) {
        int appType = AppTypeRecoManager.getInstance().getAppType(packageName);
        for (int type : this.navigationAppTypes) {
            if (appType == type) {
                LBSLog.i(TAG, false, "%{public}s is navigation type app, type is %{public}d", packageName, Integer.valueOf(appType));
                return true;
            }
        }
        return false;
    }

    public boolean isMapTypeNavigatingApp(String packageName) {
        if (AppTypeRecoManager.getInstance().getAppType(packageName) == 3) {
            return true;
        }
        return false;
    }

    public static boolean isForeGroundProc(Context context, String packageName) {
        return ((ActivityManager) context.getSystemService("activity")).getPackageImportance(packageName) <= 125;
    }

    public static int getReceiverLockCnt(LocationManagerService.Receiver receiver) {
        return receiver.mPendingBroadcasts;
    }

    public int countRealGps() {
        int count = 0;
        ArrayList<LocationManagerService.UpdateRecord> gpsRecords = getRecordsByProvider().get("gps");
        ArrayList<LocationManagerService.UpdateRecord> networkRecords = getRecordsByProvider().get("network");
        if (gpsRecords != null) {
            count = 0 + gpsRecords.size();
        }
        if (networkRecords != null) {
            int size = networkRecords.size();
            for (int i = 0; i < size; i++) {
                if ("gps".equals(networkRecords.get(i).mRealRequest.getProvider())) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isNetworkLocationSuccess(LocationManagerService.Receiver receiver) {
        synchronized (getmLock()) {
            LocationManagerService.UpdateRecord networkRecord = (LocationManagerService.UpdateRecord) receiver.mUpdateRecords.get("network");
            if (networkRecord == null || utils.getLastFixBroadcast(networkRecord) == null) {
                return false;
            }
            return true;
        }
    }

    public boolean isReceiverHasNetworkProvider(LocationManagerService.Receiver receiver) {
        synchronized (getmLock()) {
            if (((LocationManagerService.UpdateRecord) receiver.mUpdateRecords.get("network")) != null) {
                return true;
            }
            return false;
        }
    }

    public String getReceiverPackageName(LocationManagerService.Receiver receiver) {
        return receiver.mCallerIdentity.mPackageName;
    }

    public void updateNavigatingStatus(boolean isNavigating) {
        HwLocationManagerService locationManagerService = this.mService;
        if (locationManagerService instanceof HwLocationManagerService) {
            locationManagerService.updateNavigatingStatus(isNavigating);
        }
    }

    public void handleQuickLocation(int command) {
        HwLocationManagerService locationManagerService = this.mService;
        if (locationManagerService instanceof HwLocationManagerService) {
            locationManagerService.handleQuickLocation(command);
        }
    }

    private ConnectivityManager.NetworkCallback createNetworkConnectivityCallback() {
        return new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.LocationManagerServiceUtil.AnonymousClass3 */

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                LBSLog.d(LocationManagerServiceUtil.TAG, false, "NetworkCallback onCapabilitiesChanged", new Object[0]);
                LocationManagerServiceUtil locationManagerServiceUtil = LocationManagerServiceUtil.this;
                if (!locationManagerServiceUtil.hasCapabilitiesChanged((NetworkCapabilities) locationManagerServiceUtil.networkMap.get(network), capabilities)) {
                    LBSLog.d(LocationManagerServiceUtil.TAG, false, "Relevant network capabilities unchanged. Capabilities: %{public}s", capabilities);
                } else {
                    LocationManagerServiceUtil.this.networkMap.put(network, capabilities);
                }
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                LBSLog.d(LocationManagerServiceUtil.TAG, false, "NetworkCallback onLost", new Object[0]);
                LocationManagerServiceUtil.this.networkMap.remove(network);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasCapabilitiesChanged(NetworkCapabilities curCapabilities, NetworkCapabilities newCapabilities) {
        if (curCapabilities == null || newCapabilities == null) {
            return true;
        }
        return hasCapabilityChanged(curCapabilities, newCapabilities, 16);
    }

    private boolean hasCapabilityChanged(NetworkCapabilities curCapabilities, NetworkCapabilities newCapabilities, int capability) {
        return curCapabilities.hasCapability(capability) != newCapabilities.hasCapability(capability);
    }

    public boolean isNetworkAvailable() {
        boolean isWifiConnected = false;
        boolean isMobileConnected = false;
        for (Map.Entry<Network, NetworkCapabilities> entry : this.networkMap.entrySet()) {
            NetworkCapabilities capabilities = entry.getValue();
            if (capabilities != null) {
                if (capabilities.hasTransport(1) && capabilities.hasCapability(16)) {
                    isWifiConnected = true;
                }
                if (capabilities.hasTransport(0) && capabilities.hasCapability(16)) {
                    isMobileConnected = true;
                }
            }
        }
        LBSLog.i(TAG, false, "isWifiConnected is %{public}b , isMobileConnected is %{public}b", Boolean.valueOf(isWifiConnected), Boolean.valueOf(isMobileConnected));
        return isWifiConnected || isMobileConnected;
    }

    public static boolean isGmsVersion() {
        return !"".equals(GMS_VERSION);
    }

    public static boolean isChineseVersion() {
        return CHINA_SHORT_STR.equalsIgnoreCase(LOCALE_REGION);
    }

    public LocationManagerService.LocationProvider getLocationProviderLocked(String providerName) {
        return utils.getLocationProviderLocked(this.mService, providerName);
    }

    public void removeProviderLocked(LocationManagerService.LocationProvider provider) {
        utils.removeProviderLocked(this.mService, provider);
    }

    public void setHwLocationGpsLogServices(IHwGpsLogServices hwGpsLogServices) {
        utils.setHwLocationGpsLogServices(this.mService, hwGpsLogServices);
    }

    public LocationManagerService.Receiver getReceiverLocked(ILocationListener listener, int pid, int uid, String packageName) {
        LocationManagerServiceUtils locationManagerServiceUtils = utils;
        if (locationManagerServiceUtils == null) {
            return null;
        }
        return locationManagerServiceUtils.getReceiverLocked(this.mService, listener, pid, uid, packageName, null, false);
    }

    public void requestLocationUpdatesLocked(LocationRequest request, LocationManagerService.Receiver receiver, int uid, String packageName) {
        LocationManagerServiceUtils locationManagerServiceUtils = utils;
        if (locationManagerServiceUtils != null) {
            locationManagerServiceUtils.requestLocationUpdatesLocked(this.mService, request, receiver, uid, packageName);
        }
    }

    public HashMap<String, Location> getLastLocation() {
        return utils.getLastLocation(this.mService);
    }
}
