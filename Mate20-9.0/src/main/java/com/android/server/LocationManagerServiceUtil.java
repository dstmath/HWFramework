package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.server.LocationManagerService;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeocoderProxyUtils;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationProviderProxyUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LocationManagerServiceUtil {
    private static final String FUSEDPROXY_SERVICE_ACTION = "com.android.location.service.FusedProvider";
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    private static final String GEOCODER_SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String GEOFENCE_SERVICE_ACTION = "com.android.location.service.GeofenceProvider";
    public static final String GOOGLE_GMS_PROCESS = "com.google.android.gms";
    public static final String GOOGLE_GMS_UI_PROCESS = "com.google.android.gms.ui";
    public static final String GOOGLE_MAP_PROCESS = "com.google.android.apps.maps";
    private static final String GOOGLE_MAP_STATE = "google_map_state";
    public static final String GOOGLE_NETWORK_PROCESS = "com.google.process.location";
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    public static final int STATE_INSTALLED_ONLY = 0;
    public static final int STATE_INSTALLED_RPOMPTED = 1;
    public static final int STATE_UNINSTALLED = 2;
    private static final String TAG = "LocationManagerServiceUtil";
    private static final List<String> WHITE_LIST = new ArrayList(Arrays.asList(new String[]{"android", "com.android.location.fused"}));
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
    /* access modifiers changed from: private */
    public String mNLPcivil;
    private String mNLPforeign = GOOGLE_GMS_PROCESS;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String packageName, int uid) {
            if (LocationManagerServiceUtil.GOOGLE_MAP_PROCESS.equals(packageName)) {
                Slog.d(LocationManagerServiceUtil.TAG, "onPackageAdded " + packageName);
                LocationManagerServiceUtil.this.setGoogleMapState(0);
            }
            LocationManagerServiceUtil.super.onPackageAdded(packageName, uid);
        }

        public void onPackageRemoved(String packageName, int uid) {
            if (LocationManagerServiceUtil.GOOGLE_MAP_PROCESS.equals(packageName)) {
                Slog.d(LocationManagerServiceUtil.TAG, "onPackageRemoved " + packageName);
                LocationManagerServiceUtil.this.shouldMonitorGmsProcess = false;
                LocationManagerServiceUtil.this.setGoogleMapState(2);
            }
            LocationManagerServiceUtil.super.onPackageRemoved(packageName, uid);
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private LocationManagerService mService;
    private TelephonyManager mTelephonyManager;
    public boolean shouldMonitorGmsProcess = false;
    public boolean skipGooglePrompt = true;
    public boolean useGoogleNlpNow = false;

    public static void setPidGoogleLocation(int pid, String processName) {
        if (GOOGLE_NETWORK_PROCESS.equals(processName)) {
            Slog.d(TAG, "setPidGoogleLocation pid = " + pid);
            pidGoogleLocation = pid;
        } else if (GOOGLE_MAP_PROCESS.equals(processName) && getDefault() != null && getDefault().isMultiNlpEnable() && getDefault().googleMapState == 0) {
            Slog.d(TAG, "setPidGoogleLocation processName = " + processName);
            if (getDefault().isGmsProcessRunning()) {
                getDefault().sendProvidersChangedAction();
            } else {
                getDefault().shouldMonitorGmsProcess = true;
            }
        } else if (GOOGLE_GMS_PROCESS.equals(processName) && getDefault() != null && getDefault().isMultiNlpEnable() && getDefault().shouldMonitorGmsProcess) {
            Slog.d(TAG, "setPidGoogleLocation processName = " + processName);
            getDefault().sendProvidersChangedAction();
            getDefault().shouldMonitorGmsProcess = false;
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

    private LocationManagerServiceUtil(LocationManagerService service, Context context) {
        this.mService = service;
        this.mContext = context;
        SystemProperties.set("sys.show_google_nlp", "true");
        if ("CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""))) {
            SystemProperties.set("sys.show_google_nlp", "false");
            this.googleMapState = getGoogleMapState();
            this.mPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
            this.mPhoneStateListener = new PhoneStateListener() {
                public void onServiceStateChanged(ServiceState state) {
                    if (state != null) {
                        String numeric = state.getOperatorNumeric();
                        Slog.d(LocationManagerServiceUtil.TAG, "ServiceStateChanged numeric = " + numeric);
                        if (numeric != null && numeric.length() >= 5 && numeric.substring(0, 5).equals("99999")) {
                            return;
                        }
                        if (numeric != null && numeric.length() >= 3 && numeric.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                            String pkgName = LocationManagerServiceUtil.this.mNLPcivil;
                            Slog.d(LocationManagerServiceUtil.TAG, "ServiceStateChanged use pkgName = " + pkgName);
                            LocationManagerServiceUtil.this.useGoogleNlpNow = false;
                            LocationManagerServiceUtil.this.skipGooglePrompt = true;
                            SystemProperties.set("sys.show_google_nlp", "false");
                            LocationManagerServiceUtil.this.bindSpecificService(pkgName, 10, false);
                        } else if (numeric != null && !numeric.equals("")) {
                            String pkgName2 = LocationManagerServiceUtil.this.mNLPcivil;
                            Slog.d(LocationManagerServiceUtil.TAG, "ServiceStateChanged use pkgName = " + pkgName2);
                            LocationManagerServiceUtil.this.useGoogleNlpNow = false;
                            LocationManagerServiceUtil.this.skipGooglePrompt = false;
                            SystemProperties.set("sys.show_google_nlp", "false");
                            LocationManagerServiceUtil.this.bindSpecificService(pkgName2, 10, false);
                        }
                    }
                }
            };
            if (isMultiNlpEnable()) {
                this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
                this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
            }
        }
        this.am = (ActivityManager) this.mContext.getSystemService("activity");
    }

    private boolean isGmsProcessRunning() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
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
    public void setGoogleMapState(int state) {
        Slog.d(TAG, "setGoogleMapState. " + state);
        Settings.System.putInt(this.mContext.getContentResolver(), GOOGLE_MAP_STATE, state);
        this.googleMapState = state;
    }

    private int getGoogleMapState() {
        int state = Settings.System.getInt(this.mContext.getContentResolver(), GOOGLE_MAP_STATE, 2);
        Slog.d(TAG, "getGoogleMapState. " + state);
        return state;
    }

    private void sendProvidersChangedAction() {
        Slog.d(TAG, "sendProvidersChangedAction.");
        setGoogleMapState(1);
        if (Settings.Secure.getInt(getDefault().mContext.getContentResolver(), "location_mode", 0) != 0) {
            this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
            this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
        }
    }

    public HashMap<String, LocationProviderInterface> getRealProviders() {
        return utils.getRealProviders(this.mService);
    }

    public Set<String> getEnabledProviders() {
        return utils.getEnabledProviders(this.mService);
    }

    public Handler getLocationHandler() {
        return utils.getLocationHandler(this.mService);
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

    public void addProviderLocked(LocationProviderInterface provider) {
        utils.addProviderLocked(this.mService, provider);
    }

    private void setGeocodeProvider(GeocoderProxy geocodeProvider) {
        utils.setGeocodeProvider(this.mService, geocodeProvider);
    }

    public GeocoderProxy getGeocoderProvider() {
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

    public HashMap<String, LocationProviderInterface> getProvidersByName() {
        return utils.getProvidersByName(this.mService);
    }

    public HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> getRecordsByProvider() {
        return utils.getRecordsByProvider(this.mService);
    }

    public boolean isAllowedByCurrentUserSettingsLocked(String provider) {
        return utils.isAllowedByCurrentUserSettingsLocked(this.mService, provider).booleanValue();
    }

    public boolean isMultiNlpEnable() {
        if (!this.isMultiNlpEnableLoaded) {
            if ("CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""))) {
                PackageInfo foreignPackage = null;
                PackageInfo civilPackage = null;
                try {
                    foreignPackage = this.mContext.getPackageManager().getPackageInfo(this.mNLPforeign, 4);
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(TAG, "missing foreignPackage");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                this.mNLPcivil = SystemProperties.get("ro.config.hw_nlp", "com.baidu.map.location");
                try {
                    civilPackage = this.mContext.getPackageManager().getPackageInfo(this.mNLPcivil, 4);
                } catch (PackageManager.NameNotFoundException e3) {
                    Slog.e(TAG, "missing mNLPcivil: " + this.mNLPcivil);
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                if (!(TextUtils.isEmpty(this.mNLPcivil) || foreignPackage == null || civilPackage == null)) {
                    this.isMultiNlpEnableFlag = true;
                }
            }
            this.isMultiNlpEnableLoaded = true;
            Slog.e(TAG, "isMultiNlpEnable: isMultiNlpEnableFlag = " + this.isMultiNlpEnableFlag);
        }
        return this.isMultiNlpEnableFlag;
    }

    public static boolean useCivilNlpPackage(String action, String packageName) {
        if ((GEOFENCE_SERVICE_ACTION.equals(action) || FUSED_LOCATION_SERVICE_ACTION.equals(action) || FUSEDPROXY_SERVICE_ACTION.equals(action)) && getDefault() != null && getDefault().isMultiNlpEnable()) {
            if (getDefault().useGoogleNlpNow) {
                if (getDefault().mNLPforeign.equals(packageName)) {
                    return true;
                }
            } else if (getDefault().mNLPcivil != null && getDefault().mNLPcivil.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean skipForeignNlpPackage(String action, String packageName) {
        if ((GEOFENCE_SERVICE_ACTION.equals(action) || FUSED_LOCATION_SERVICE_ACTION.equals(action) || FUSEDPROXY_SERVICE_ACTION.equals(action)) && getDefault() != null && getDefault().isMultiNlpEnable() && !getDefault().useGoogleNlpNow && getDefault().mNLPforeign.equals(packageName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void bindSpecificService(String packageName, int version, boolean bind) {
        LocationProviderProxy networkProvider = getRealProviders().get("network");
        GeocoderProxy geocoderProxy = getGeocoderProvider();
        boolean needUpdateProvider = false;
        if (bind) {
            if (networkProvider != null) {
                bindSpecificService(getLocationProviderProxyWatcher(networkProvider), packageName, version);
            } else {
                Slog.e(TAG, "bindSpecificService: no network location provider found, init again");
                needUpdateProvider = true;
                LocationProviderProxy networkProvider2 = LocationProviderProxy.createAndBind(this.mContext, "network", NETWORK_LOCATION_SERVICE_ACTION, 17956962, 17039833, 17236014, getLocationHandler());
                if (networkProvider2 != null) {
                    HashMap<String, LocationProviderInterface> realProviders = getRealProviders();
                    synchronized (realProviders) {
                        realProviders.put("network", networkProvider2);
                    }
                    getProxyProviders().add(networkProvider2);
                    addProviderLocked(networkProvider2);
                }
            }
            if (geocoderProxy != null) {
                bindSpecificService(getgetGeocoderServiceWatcher(getGeocoderProvider()), packageName, version);
            } else {
                Slog.e(TAG, "bindSpecificService: no geocoder provider found, init agan");
                needUpdateProvider = true;
                setGeocodeProvider(GeocoderProxy.createAndBind(this.mContext, 17956954, 17039809, 17236014, getLocationHandler()));
            }
            if (needUpdateProvider) {
                updateProvidersLocked();
            }
        }
    }

    private void bindSpecificService(ServiceWatcher serviceWatcher, String packageName, int version) {
        if (serviceWatcher != null) {
            if (packageName.equals(serviceWatcher.getBestPackageName())) {
                Slog.d(TAG, "bindSpecificService: same package name, return");
                return;
            }
            Slog.d(TAG, "bindSpecificService old mPackageName = " + serviceWatcher.getBestPackageName());
            String currentPackageName = serviceWatcher.getBestPackageName();
            if (this.mNLPcivil.equals(currentPackageName)) {
                Slog.d(TAG, "currentPackageName is civil, force Stop");
                this.am.forceStopPackage(currentPackageName);
            }
            Slog.d(TAG, "bindSpecificService new packageName = " + packageName);
            serviceWatcher.bindToPackageWithLock(packageName, version, false);
        }
    }

    public static boolean shouldSkipGoogleNlp(int pid) {
        if (pid == pidGoogleLocation) {
            if (getDefault() == null || !getDefault().isMultiNlpEnable() || !getDefault().skipGooglePrompt || 1 == getDefault().googleMapState) {
                Slog.w(TAG, "shouldSkipGoogleNlp return false skip process " + pid);
            } else {
                Slog.w(TAG, "skip process " + pid);
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
                if (receiver.mIdentity.mPackageName.equals(packageName)) {
                    if (deadReceivers == null) {
                        deadReceivers = new ArrayList<>();
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

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0079, code lost:
        return r0;
     */
    public boolean isGpsOrFusedStartedBySystem() {
        boolean isStartedBySytem = true;
        boolean hasFused = false;
        synchronized (getmLock()) {
            ArrayList<LocationManagerService.UpdateRecord> gpsRecords = getRecordsByProvider().get("gps");
            if (gpsRecords != null) {
                if (!gpsRecords.isEmpty()) {
                    int gpsSize = gpsRecords.size();
                    int i = 0;
                    while (true) {
                        if (i >= gpsSize) {
                            break;
                        }
                        String packageName = gpsRecords.get(i).mReceiver.mIdentity.mPackageName;
                        if (!WHITE_LIST.contains(packageName)) {
                            isStartedBySytem = false;
                            Slog.d(TAG, "find " + packageName + " started gps should exit idle");
                            break;
                        }
                        if ("com.android.location.fused".equals(packageName)) {
                            Slog.d(TAG, "find fused started gps ,should check fused.");
                            hasFused = true;
                        } else {
                            Slog.d(TAG, "find android started gps");
                        }
                        i++;
                    }
                    if (hasFused && isStartedBySytem) {
                        isStartedBySytem = checkFused();
                    }
                }
            }
            Slog.d(TAG, "gps Records is null or empty");
            return true;
        }
    }

    private boolean checkFused() {
        String packageName;
        int quality;
        boolean isStartedBySytem = true;
        ArrayList<LocationManagerService.UpdateRecord> fusedRecords = getRecordsByProvider().get("fused");
        if (fusedRecords == null || fusedRecords.isEmpty()) {
            Slog.d(TAG, "fused Records is null or empty");
            return true;
        }
        int fusedSize = fusedRecords.size();
        int i = 0;
        while (true) {
            if (i >= fusedSize) {
                break;
            }
            packageName = fusedRecords.get(i).mReceiver.mIdentity.mPackageName;
            quality = fusedRecords.get(i).mRequest.getQuality();
            if ("android".equals(packageName) || !(quality == 203 || quality == 100)) {
                i++;
            }
        }
        Slog.d(TAG, "find " + packageName + " started fused ,quality " + quality + ", should exit idle");
        isStartedBySytem = false;
        return isStartedBySytem;
    }
}
