package com.android.server.location;

import android.content.Context;
import android.content.pm.Signature;
import android.database.ContentObserver;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.content.PackageMonitor;
import com.android.server.ServiceWatcher;
import com.android.server.ServiceWatcherUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HwGeocoderProxy extends GeocoderProxy implements HwLocationProxy {
    private static final String AB_NLP_PKNAME = SystemProperties.get("ro.config.hw_nlp", "com.huawei.lbs");
    private static final int DEFAULE_SIZE = 16;
    private static final int ENABLE_GEO_FOR_HMS = 1;
    private static final String GEO_FOR_HMS_FLAG = "enable_geo_for_hms";
    private static final String GOOGLE_NLP_PKNAME = "com.google.android.gms";
    private static final String HMS_NLP_PKNAME = "com.huawei.hwid";
    private static final int NLP_AB = 2;
    private static final int NLP_GOOGLE = 1;
    private static final int NLP_NONE = 0;
    private static final String SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String TAG = "HwGeocoderProxy";
    private static final String WATCH_HMS_NLP_PKNAME = "com.huawei.hms";
    private static ServiceWatcherUtils sServiceWatcherUtils = EasyInvokeFactory.getInvokeUtils(ServiceWatcherUtils.class);
    private boolean isEnableGeoForHms = true;
    private ContentObserver mContentObserver;
    private final Context mContext;
    private boolean mIsDualNlpAlive;
    private boolean mIsPMAbRegisted = false;
    private boolean mIsPmGoogleRegisted = false;
    private boolean mIsStartAB;
    private boolean mIsStartGoogle;
    private final Object mLock = new Object();
    private int mNlpUsed = 0;
    private final PackageMonitor mPackageMonitorAb = new PackageMonitor() {
        /* class com.android.server.location.HwGeocoderProxy.AnonymousClass2 */

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwGeocoderProxy.this.mLock) {
                boolean isHms = false;
                String hmsNlpPackageName = HwMultiNlpPolicy.getHmsPackageName(HwGeocoderProxy.this.mContext);
                if (hmsNlpPackageName != null && !"".equals(hmsNlpPackageName) && hmsNlpPackageName.equals(packageName)) {
                    isHms = true;
                }
                if (HwGeocoderProxy.AB_NLP_PKNAME.equals(packageName) || HwGeocoderProxy.HMS_NLP_PKNAME.equals(packageName) || HwGeocoderProxy.WATCH_HMS_NLP_PKNAME.equals(packageName) || isHms) {
                    LBSLog.i(HwGeocoderProxy.TAG, false, "rebind onPackageChanged packageName : %{public}s", packageName);
                    HwGeocoderProxy.this.startServiceWatcher();
                }
            }
            return HwGeocoderProxy.super.onPackageChanged(packageName, uid, components);
        }
    };
    private final PackageMonitor mPackageMonitorGoogle = new PackageMonitor() {
        /* class com.android.server.location.HwGeocoderProxy.AnonymousClass3 */

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwGeocoderProxy.this.mLock) {
                if (packageName.equals("com.google.android.gms")) {
                    boolean z = true;
                    LBSLog.i(HwGeocoderProxy.TAG, false, "rebind onPackageChanged packageName : %{public}s", packageName);
                    if (!HwGeocoderProxy.this.mIsStartGoogle && HwGeocoderProxy.this.mServiceWatcherGoogle != null) {
                        HwGeocoderProxy.this.mIsStartGoogle = HwGeocoderProxy.this.mServiceWatcherGoogle.start();
                        HwGeocoderProxy hwGeocoderProxy = HwGeocoderProxy.this;
                        if (!HwGeocoderProxy.this.mIsStartGoogle || !HwGeocoderProxy.this.mIsStartAB) {
                            z = false;
                        }
                        hwGeocoderProxy.mIsDualNlpAlive = z;
                    }
                }
            }
            return HwGeocoderProxy.super.onPackageChanged(packageName, uid, components);
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private final ServiceWatcher mServiceWatcherAb;
    private final ServiceWatcher mServiceWatcherGoogle;
    private TelephonyManager mTelephonyManager;

    public static HwGeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        HwGeocoderProxy proxy = new HwGeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    public HwGeocoderProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        super(context);
        this.mContext = context;
        this.isEnableGeoForHms = Settings.Global.getInt(this.mContext.getContentResolver(), GEO_FOR_HMS_FLAG, 1) == 1;
        this.mServiceWatcherGoogle = serviceWatcherCreate(1, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, LocationHandlerEx.getGeoInstance());
        this.mServiceWatcherAb = serviceWatcherCreate(2, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, LocationHandlerEx.getGeoInstance());
        HwMultiNlpPolicy.getDefault(this.mContext).setHwGeocoderProxy(this);
        LBSLog.i(TAG, false, "isEnableGeoForHms =  %{public}b", Boolean.valueOf(this.isEnableGeoForHms));
    }

    private ServiceWatcher serviceWatcherCreate(int nlp, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        String servicePackageName;
        ArrayList<String> initialPackageNames = new ArrayList<>();
        if (nlp == 1) {
            servicePackageName = "com.google.android.gms";
        } else {
            servicePackageName = AB_NLP_PKNAME;
            initialPackageNames = getPackageNames();
        }
        initialPackageNames.add(servicePackageName);
        ArrayList<HashSet<Signature>> signatureSets = ServiceWatcher.getSignatureSets(this.mContext, (String[]) initialPackageNames.toArray(new String[initialPackageNames.size()]));
        ServiceWatcher serviceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (HwMultiNlpPolicy.isOverseasNoGms() && this.isEnableGeoForHms) {
            if (nlp != 1) {
                registerHmsPackageNameChanges();
            }
            servicePackageName = null;
        }
        sServiceWatcherUtils.setServicePackageName(serviceWatcher, servicePackageName);
        sServiceWatcherUtils.setSignatureSets(serviceWatcher, signatureSets);
        return serviceWatcher;
    }

    private boolean bind() {
        if (this.mServiceWatcherAb != null && (HwMultiNlpPolicy.isChineseVersion() || !HwMultiNlpPolicy.isGmsExist())) {
            this.mIsStartAB = this.mServiceWatcherAb.start();
            registerPhoneStateListener();
            if (!this.mIsStartAB) {
                this.mIsPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, (Looper) null, UserHandle.ALL, true);
            }
        }
        if (this.mServiceWatcherGoogle != null && !HwMultiNlpPolicy.isChineseVersion()) {
            this.mIsStartGoogle = this.mServiceWatcherGoogle.start();
            if (!this.mIsStartGoogle) {
                this.mIsPmGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, (Looper) null, UserHandle.ALL, true);
            }
        }
        this.mIsDualNlpAlive = this.mIsStartAB && this.mIsStartGoogle;
        LBSLog.i(TAG, false, "mNlp:start_g %{public}b , start_ab %{public}b", Boolean.valueOf(this.mIsStartGoogle), Boolean.valueOf(this.mIsStartAB));
        if (this.mIsStartGoogle || this.mIsStartAB) {
            return true;
        }
        return false;
    }

    public String getConnectedPackageName() {
        if (!this.mIsStartGoogle || !this.mIsStartAB) {
            if (this.mIsStartGoogle) {
                return this.mServiceWatcherGoogle.getCurrentPackageName();
            }
            return this.mServiceWatcherAb.getCurrentPackageName();
        } else if (this.mServiceWatcherGoogle.getCurrentPackageName() == null) {
            return this.mServiceWatcherAb.getCurrentPackageName();
        } else {
            if (this.mServiceWatcherAb.getCurrentPackageName() == null) {
                return this.mServiceWatcherGoogle.getCurrentPackageName();
            }
            if (this.mNlpUsed == 1) {
                return this.mServiceWatcherGoogle.getCurrentPackageName() + AwarenessInnerConstants.SEMI_COLON_KEY + this.mServiceWatcherAb.getCurrentPackageName();
            }
            return this.mServiceWatcherAb.getCurrentPackageName() + AwarenessInnerConstants.SEMI_COLON_KEY + this.mServiceWatcherGoogle.getCurrentPackageName();
        }
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        this.mNlpUsed = 0;
        String result = getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        LBSLog.i(TAG, false, "getFromLocation %{public}s", result);
        if (!this.mIsDualNlpAlive || result == null) {
            if (needRequestGlobalAb(params, result)) {
                return getFromLocationGlobalAb(latitude, longitude, maxResults, params, addrs);
            }
            return result;
        } else if (this.mNlpUsed == 1) {
            this.mNlpUsed = 2;
            return getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        } else {
            this.mNlpUsed = 1;
            return getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        }
    }

    private String getFromLocationImpl(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        ServiceWatcher serviceWatcher = getServiceWatcher();
        if (serviceWatcher == null) {
            return "ServiceWatcher not Available";
        }
        return (String) serviceWatcher.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(latitude, longitude, maxResults, params, addrs) {
            /* class com.android.server.location.$$Lambda$HwGeocoderProxy$3TLUNmqbbHF6tGM_K4JRAlf1psc */
            private final /* synthetic */ double f$0;
            private final /* synthetic */ double f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ GeocoderParams f$3;
            private final /* synthetic */ List f$4;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
            }

            public final Object run(IBinder iBinder) {
                return IGeocodeProvider.Stub.asInterface(iBinder).getFromLocation(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, "Service not Available");
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        this.mNlpUsed = 0;
        String result = getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        LBSLog.i(TAG, false, "getFromLocationName %{public}s", result);
        if (!this.mIsDualNlpAlive || result == null) {
            if (needRequestGlobalAb(params, result)) {
                return getFromLocationNameGlobalAb(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            }
            return result;
        } else if (this.mNlpUsed == 1) {
            this.mNlpUsed = 2;
            return getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        } else {
            this.mNlpUsed = 1;
            return getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
    }

    private String getFromLocationNameImpl(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        ServiceWatcher serviceWatcher = getServiceWatcher();
        if (serviceWatcher == null) {
            return "ServiceWatcher not Available";
        }
        return (String) serviceWatcher.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs) {
            /* class com.android.server.location.$$Lambda$HwGeocoderProxy$IZqjmY3B9HY2sT4seU2jWT4bSWE */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ double f$1;
            private final /* synthetic */ double f$2;
            private final /* synthetic */ double f$3;
            private final /* synthetic */ double f$4;
            private final /* synthetic */ int f$5;
            private final /* synthetic */ GeocoderParams f$6;
            private final /* synthetic */ List f$7;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
                this.f$4 = r8;
                this.f$5 = r10;
                this.f$6 = r11;
                this.f$7 = r12;
            }

            public final Object run(IBinder iBinder) {
                return IGeocodeProvider.Stub.asInterface(iBinder).getFromLocationName(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        }, "Service not Available");
    }

    private ServiceWatcher getServiceWatcher() {
        if (this.mIsDualNlpAlive) {
            int i = this.mNlpUsed;
            if (i == 0) {
                if (HwMultiNlpPolicy.getDefault().shouldUseGoogleNLP(false)) {
                    this.mNlpUsed = 1;
                    return this.mServiceWatcherGoogle;
                }
                this.mNlpUsed = 2;
                return this.mServiceWatcherAb;
            } else if (i == 1) {
                return this.mServiceWatcherGoogle;
            } else {
                if (i == 2) {
                    return this.mServiceWatcherAb;
                }
                LBSLog.i(TAG, false, "should not be here.", new Object[0]);
                return null;
            }
        } else if (this.mIsStartGoogle) {
            return this.mServiceWatcherGoogle;
        } else {
            if (this.mIsStartAB) {
                return this.mServiceWatcherAb;
            }
            LBSLog.i(TAG, false, "no service.", new Object[0]);
            return null;
        }
    }

    private void registerPhoneStateListener() {
        this.mPhoneStateListener = new PhoneStateListener() {
            /* class com.android.server.location.HwGeocoderProxy.AnonymousClass1 */

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    String numeric = state.getOperatorNumeric();
                    if (numeric != null && numeric.length() >= 5 && "99999".equals(numeric.substring(0, 5))) {
                        LBSLog.i(HwGeocoderProxy.TAG, false, "numeric is contain 99999.", new Object[0]);
                    } else if (numeric != null && numeric.length() >= 3 && WifiProCommonUtils.COUNTRY_CODE_CN.equals(numeric.substring(0, 3))) {
                        HwGeocoderProxy.this.abNlpBind();
                    } else if (numeric == null || "".equals(numeric)) {
                        LBSLog.i(HwGeocoderProxy.TAG, false, "numeric is null or not belong to abNlp and googleNlp.", new Object[0]);
                    } else {
                        HwGeocoderProxy.this.googleNlpBind();
                    }
                }
            }
        };
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void abNlpBind() {
        ServiceWatcher serviceWatcher;
        if (!this.mIsStartAB && !this.mIsPMAbRegisted && (serviceWatcher = this.mServiceWatcherAb) != null) {
            this.mIsStartAB = serviceWatcher.start();
            boolean z = true;
            if (!this.mIsStartAB && !this.mIsPMAbRegisted) {
                this.mIsPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, (Looper) null, UserHandle.ALL, true);
            }
            if (!this.mIsStartGoogle || !this.mIsStartAB) {
                z = false;
            }
            this.mIsDualNlpAlive = z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void googleNlpBind() {
        ServiceWatcher serviceWatcher;
        if (!this.mIsStartGoogle && !this.mIsPmGoogleRegisted && (serviceWatcher = this.mServiceWatcherGoogle) != null) {
            this.mIsStartGoogle = serviceWatcher.start();
            boolean z = true;
            if (!this.mIsStartGoogle && !this.mIsPmGoogleRegisted) {
                this.mIsPmGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, (Looper) null, UserHandle.ALL, true);
            }
            if (!this.mIsStartGoogle || !this.mIsStartAB) {
                z = false;
            }
            this.mIsDualNlpAlive = z;
        }
    }

    private boolean needRequestGlobalAb(GeocoderParams params, String result) {
        String packageName = params.getClientPackage();
        return (HwMultiNlpPolicy.GLOBAL_NLP_CLIENT_PKG.equals(packageName) || (HwMultiNlpPolicy.GLOBAL_NLP_DEBUG_PKG.equals(packageName) && HwMultiNlpPolicy.isHwLocationDebug(this.mContext))) && result != null && HwMultiNlpPolicy.getDefault(this.mContext).getGlobalGeocoderStart();
    }

    @Override // com.android.server.location.HwLocationProxy
    public void handleServiceAB(boolean isShouldStart) {
        if (isShouldStart) {
            this.mServiceWatcherAb.start();
        } else {
            sServiceWatcherUtils.unbindLocked(this.mServiceWatcherAb);
        }
    }

    private String getFromLocationGlobalAb(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        return (String) this.mServiceWatcherAb.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(latitude, longitude, maxResults, params, addrs) {
            /* class com.android.server.location.$$Lambda$HwGeocoderProxy$snUnjuEIx40G1LJplkmPIqzb3tQ */
            private final /* synthetic */ double f$0;
            private final /* synthetic */ double f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ GeocoderParams f$3;
            private final /* synthetic */ List f$4;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
            }

            public final Object run(IBinder iBinder) {
                return IGeocodeProvider.Stub.asInterface(iBinder).getFromLocation(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, "Service not Available");
    }

    private String getFromLocationNameGlobalAb(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        return (String) this.mServiceWatcherAb.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs) {
            /* class com.android.server.location.$$Lambda$HwGeocoderProxy$ywm89zfnW26ea8xnjfBN9nHc9Qo */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ double f$1;
            private final /* synthetic */ double f$2;
            private final /* synthetic */ double f$3;
            private final /* synthetic */ double f$4;
            private final /* synthetic */ int f$5;
            private final /* synthetic */ GeocoderParams f$6;
            private final /* synthetic */ List f$7;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
                this.f$4 = r8;
                this.f$5 = r10;
                this.f$6 = r11;
                this.f$7 = r12;
            }

            public final Object run(IBinder iBinder) {
                return IGeocodeProvider.Stub.asInterface(iBinder).getFromLocationName(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        }, "Service not Available");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startServiceWatcher() {
        ServiceWatcher serviceWatcher;
        if (!this.mIsStartAB && (serviceWatcher = this.mServiceWatcherAb) != null) {
            this.mIsStartAB = serviceWatcher.start();
            this.mIsDualNlpAlive = this.mIsStartGoogle && this.mIsStartAB;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<String> getPackageNames() {
        ArrayList<String> initialPackageNames = new ArrayList<>(16);
        if (HwMultiNlpPolicy.isOverseasNoGms() && this.isEnableGeoForHms) {
            initialPackageNames.add(HMS_NLP_PKNAME);
            initialPackageNames.add(WATCH_HMS_NLP_PKNAME);
            String hmsNlpPackageName = HwMultiNlpPolicy.getHmsPackageName(this.mContext);
            if (hmsNlpPackageName != null && !"".equals(hmsNlpPackageName) && !initialPackageNames.contains(hmsNlpPackageName)) {
                initialPackageNames.add(hmsNlpPackageName);
            }
        }
        return initialPackageNames;
    }

    private void registerHmsPackageNameChanges() {
        this.mContentObserver = new ContentObserver(LocationHandlerEx.getGeoInstance()) {
            /* class com.android.server.location.HwGeocoderProxy.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                synchronized (HwGeocoderProxy.this.mLock) {
                    String hmsNlpPackageName = HwMultiNlpPolicy.getHmsPackageName(HwGeocoderProxy.this.mContext);
                    if (hmsNlpPackageName != null && !"".equals(hmsNlpPackageName) && HwMultiNlpPolicy.isOverseasNoGms() && HwGeocoderProxy.this.isEnableGeoForHms) {
                        ArrayList<String> initialPackageNames = HwGeocoderProxy.this.getPackageNames();
                        initialPackageNames.add(HwGeocoderProxy.AB_NLP_PKNAME);
                        HwGeocoderProxy.sServiceWatcherUtils.setSignatureSets(HwGeocoderProxy.this.mServiceWatcherAb, ServiceWatcher.getSignatureSets(HwGeocoderProxy.this.mContext, (String[]) initialPackageNames.toArray(new String[initialPackageNames.size()])));
                        HwGeocoderProxy.this.startServiceWatcher();
                    }
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("hms_package_name"), false, this.mContentObserver);
    }
}
