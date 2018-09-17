package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.server.LocationManagerService.Receiver;
import com.android.server.LocationManagerService.UpdateRecord;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeocoderProxyUtils;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationProviderProxyUtils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static GeocoderProxyUtils geocoderProxyUtilsUtils;
    private static volatile LocationManagerServiceUtil instance;
    private static LocationProviderProxyUtils locationProviderProxyUtils;
    private static int pidGoogleLocation;
    private static LocationManagerServiceUtils utils;
    private ActivityManager am;
    public int googleMapState;
    private boolean isMultiNlpEnableFlag;
    private boolean isMultiNlpEnableLoaded;
    private Context mContext;
    private String mNLPcivil;
    private String mNLPforeign;
    private final PackageMonitor mPackageMonitor;
    private PhoneStateListener mPhoneStateListener;
    private LocationManagerService mService;
    private TelephonyManager mTelephonyManager;
    public boolean shouldMonitorGmsProcess;
    public boolean skipGooglePrompt;
    public boolean useGoogleNlpNow;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.LocationManagerServiceUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.LocationManagerServiceUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LocationManagerServiceUtil.<clinit>():void");
    }

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
        this.mNLPforeign = GOOGLE_GMS_PROCESS;
        this.mContext = null;
        this.isMultiNlpEnableLoaded = false;
        this.isMultiNlpEnableFlag = false;
        this.useGoogleNlpNow = false;
        this.skipGooglePrompt = true;
        this.shouldMonitorGmsProcess = false;
        this.googleMapState = STATE_UNINSTALLED;
        this.am = null;
        this.mPackageMonitor = new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                if (LocationManagerServiceUtil.GOOGLE_MAP_PROCESS.equals(packageName)) {
                    Slog.d(LocationManagerServiceUtil.TAG, "onPackageAdded " + packageName);
                    LocationManagerServiceUtil.this.setGoogleMapState(LocationManagerServiceUtil.STATE_INSTALLED_ONLY);
                }
                super.onPackageAdded(packageName, uid);
            }

            public void onPackageRemoved(String packageName, int uid) {
                if (LocationManagerServiceUtil.GOOGLE_MAP_PROCESS.equals(packageName)) {
                    Slog.d(LocationManagerServiceUtil.TAG, "onPackageRemoved " + packageName);
                    LocationManagerServiceUtil.this.shouldMonitorGmsProcess = false;
                    LocationManagerServiceUtil.this.setGoogleMapState(LocationManagerServiceUtil.STATE_UNINSTALLED);
                }
                super.onPackageRemoved(packageName, uid);
            }
        };
        this.mService = service;
        this.mContext = context;
        SystemProperties.set("sys.show_google_nlp", "true");
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG))) {
            SystemProperties.set("sys.show_google_nlp", "false");
            this.googleMapState = getGoogleMapState();
            this.mPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
            this.mPhoneStateListener = new PhoneStateListener() {
                public void onServiceStateChanged(ServiceState state) {
                    if (state != null) {
                        String numeric = state.getOperatorNumeric();
                        Slog.d(LocationManagerServiceUtil.TAG, "ServiceStateChanged numeric = " + numeric);
                        if (numeric != null && numeric.length() >= 5 && numeric.substring(LocationManagerServiceUtil.STATE_INSTALLED_ONLY, 5).equals("99999")) {
                            return;
                        }
                        String pkgName;
                        if (numeric != null && numeric.length() >= 3 && numeric.substring(LocationManagerServiceUtil.STATE_INSTALLED_ONLY, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                            pkgName = LocationManagerServiceUtil.this.mNLPcivil;
                            Slog.d(LocationManagerServiceUtil.TAG, "ServiceStateChanged use pkgName = " + pkgName);
                            LocationManagerServiceUtil.this.useGoogleNlpNow = false;
                            LocationManagerServiceUtil.this.skipGooglePrompt = true;
                            SystemProperties.set("sys.show_google_nlp", "false");
                            LocationManagerServiceUtil.this.bindSpecificService(pkgName, 10, false);
                        } else if (numeric != null && !numeric.equals(AppHibernateCst.INVALID_PKG)) {
                            pkgName = LocationManagerServiceUtil.this.mNLPcivil;
                            Slog.d(LocationManagerServiceUtil.TAG, "ServiceStateChanged use pkgName = " + pkgName);
                            LocationManagerServiceUtil.this.useGoogleNlpNow = false;
                            LocationManagerServiceUtil.this.skipGooglePrompt = false;
                            SystemProperties.set("sys.show_google_nlp", "false");
                            LocationManagerServiceUtil.this.bindSpecificService(pkgName, 10, false);
                        }
                    }
                }
            };
            if (isMultiNlpEnable()) {
                this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
                this.mTelephonyManager.listen(this.mPhoneStateListener, STATE_INSTALLED_RPOMPTED);
            }
        }
        this.am = (ActivityManager) this.mContext.getSystemService("activity");
    }

    private boolean isGmsProcessRunning() {
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (GOOGLE_GMS_PROCESS.equals(appProcess.processName)) {
                return true;
            }
        }
        return false;
    }

    private void setGoogleMapState(int state) {
        Slog.d(TAG, "setGoogleMapState. " + state);
        System.putInt(this.mContext.getContentResolver(), GOOGLE_MAP_STATE, state);
        this.googleMapState = state;
    }

    private int getGoogleMapState() {
        int state = System.getInt(this.mContext.getContentResolver(), GOOGLE_MAP_STATE, STATE_UNINSTALLED);
        Slog.d(TAG, "getGoogleMapState. " + state);
        return state;
    }

    private void sendProvidersChangedAction() {
        Slog.d(TAG, "sendProvidersChangedAction.");
        setGoogleMapState(STATE_INSTALLED_RPOMPTED);
        if (Secure.getInt(getDefault().mContext.getContentResolver(), "location_mode", STATE_INSTALLED_ONLY) != 0) {
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

    public HashMap<Object, Receiver> getReceivers() {
        return utils.getReceivers(this.mService);
    }

    public void removeUpdatesLocked(Receiver receiver) {
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

    public HashMap<String, LocationProviderInterface> getProvidersByName() {
        return utils.getProvidersByName(this.mService);
    }

    public HashMap<String, ArrayList<UpdateRecord>> getRecordsByProvider() {
        return utils.getRecordsByProvider(this.mService);
    }

    public boolean isMultiNlpEnable() {
        if (!this.isMultiNlpEnableLoaded) {
            if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG))) {
                PackageInfo foreignPackage = null;
                PackageInfo civilPackage = null;
                try {
                    foreignPackage = this.mContext.getPackageManager().getPackageInfo(this.mNLPforeign, 4);
                } catch (NameNotFoundException e) {
                    Slog.e(TAG, "missing foreignPackage: " + foreignPackage);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                this.mNLPcivil = SystemProperties.get("ro.config.hw_nlp", "com.baidu.map.location");
                try {
                    civilPackage = this.mContext.getPackageManager().getPackageInfo(this.mNLPcivil, 4);
                } catch (NameNotFoundException e3) {
                    Slog.e(TAG, "missing mNLPcivil: " + this.mNLPcivil);
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
                if (!(TextUtils.isEmpty(this.mNLPcivil) || r3 == null || r0 == null)) {
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

    private void bindSpecificService(String packageName, int version, boolean bind) {
        LocationProviderProxy networkProvider = (LocationProviderProxy) getRealProviders().get("network");
        GeocoderProxy geocoderProxy = getGeocoderProvider();
        boolean needUpdateProvider = false;
        if (bind) {
            if (networkProvider != null) {
                bindSpecificService(getLocationProviderProxyWatcher(networkProvider), packageName, version);
            } else {
                Slog.e(TAG, "bindSpecificService: no network location provider found, init again");
                needUpdateProvider = true;
                networkProvider = LocationProviderProxy.createAndBind(this.mContext, "network", NETWORK_LOCATION_SERVICE_ACTION, 17956945, 17039423, 17236013, getLocationHandler());
                if (networkProvider != null) {
                    getRealProviders().put("network", networkProvider);
                    getProxyProviders().add(networkProvider);
                    addProviderLocked(networkProvider);
                }
            }
            if (geocoderProxy != null) {
                bindSpecificService(getgetGeocoderServiceWatcher(getGeocoderProvider()), packageName, version);
            } else {
                Slog.e(TAG, "bindSpecificService: no geocoder provider found, init agan");
                needUpdateProvider = true;
                setGeocodeProvider(GeocoderProxy.createAndBind(this.mContext, 17956948, 17039426, 17236013, getLocationHandler()));
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
            if (getDefault() == null || !getDefault().isMultiNlpEnable() || !getDefault().skipGooglePrompt || STATE_INSTALLED_RPOMPTED == getDefault().googleMapState) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearPackageLocation(String packageName) {
        Throwable th;
        synchronized (getmLock()) {
            try {
                ArrayList<Receiver> deadReceivers = null;
                for (Receiver receiver : getReceivers().values()) {
                    ArrayList<Receiver> deadReceivers2;
                    try {
                        if (receiver.mPackageName.equals(packageName)) {
                            if (deadReceivers == null) {
                                deadReceivers2 = new ArrayList();
                            } else {
                                deadReceivers2 = deadReceivers;
                            }
                            deadReceivers2.add(receiver);
                        } else {
                            deadReceivers2 = deadReceivers;
                        }
                        deadReceivers = deadReceivers2;
                    } catch (Throwable th2) {
                        th = th2;
                        deadReceivers2 = deadReceivers;
                    }
                }
                if (deadReceivers != null) {
                    for (Receiver receiver2 : deadReceivers) {
                        removeUpdatesLocked(receiver2);
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }
}
