package com.android.server.location;

import android.content.Context;
import android.content.pm.Signature;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.server.ServiceWatcher;
import com.android.server.ServiceWatcherUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HwGeocoderProxy extends GeocoderProxy {
    /* access modifiers changed from: private */
    public static final String AB_NLP_PKNAME = SystemProperties.get("ro.config.hw_nlp", "com.huawei.lbs");
    private static final String GLOBAL_GEOCODER_CLIENT_PKG = "com.hisi.mapcon";
    private static final String GLOBAL_GEOCODER_DEBUG_PKG = "com.huawei.android.gpsselfcheck";
    private static final String GOOGLE_NLP_PKNAME = "com.google.android.gms";
    private static final int NLP_AB = 2;
    private static final int NLP_GOOGLE = 1;
    private static final int NLP_NONE = 0;
    private static final String SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String TAG = "HwGeocoderProxy";
    private static ServiceWatcherUtils mServiceWatcherUtils = EasyInvokeFactory.getInvokeUtils(ServiceWatcherUtils.class);
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsDualNlpAlive;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private int mNlpUsed = 0;
    private boolean mPMAbRegisted = false;
    private boolean mPMGoogleRegisted = false;
    private final PackageMonitor mPackageMonitorAb = new PackageMonitor() {
        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwGeocoderProxy.this.mLock) {
                if (packageName.equals(HwGeocoderProxy.AB_NLP_PKNAME)) {
                    Log.d(HwGeocoderProxy.TAG, "rebind onPackageChanged packageName : " + packageName);
                    if (!HwGeocoderProxy.this.mStartAB && HwGeocoderProxy.this.mServiceWatcherAb != null) {
                        boolean unused = HwGeocoderProxy.this.mStartAB = HwGeocoderProxy.this.mServiceWatcherAb.start();
                        boolean unused2 = HwGeocoderProxy.this.mIsDualNlpAlive = HwGeocoderProxy.this.mStartGoogle && HwGeocoderProxy.this.mStartAB;
                    }
                }
            }
            return HwGeocoderProxy.super.onPackageChanged(packageName, uid, components);
        }
    };
    private final PackageMonitor mPackageMonitorGoogle = new PackageMonitor() {
        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwGeocoderProxy.this.mLock) {
                if (packageName.equals("com.google.android.gms")) {
                    Log.d(HwGeocoderProxy.TAG, "rebind onPackageChanged packageName : " + packageName);
                    if (!HwGeocoderProxy.this.mStartGoogle && HwGeocoderProxy.this.mServiceWatcherGoogle != null) {
                        boolean unused = HwGeocoderProxy.this.mStartGoogle = HwGeocoderProxy.this.mServiceWatcherGoogle.start();
                        boolean unused2 = HwGeocoderProxy.this.mIsDualNlpAlive = HwGeocoderProxy.this.mStartGoogle && HwGeocoderProxy.this.mStartAB;
                    }
                }
            }
            return HwGeocoderProxy.super.onPackageChanged(packageName, uid, components);
        }
    };
    private PhoneStateListener mPhoneStateListener;
    /* access modifiers changed from: private */
    public final ServiceWatcher mServiceWatcherAb;
    /* access modifiers changed from: private */
    public final ServiceWatcher mServiceWatcherGoogle;
    /* access modifiers changed from: private */
    public boolean mStartAB;
    /* access modifiers changed from: private */
    public boolean mStartGoogle;
    private TelephonyManager mTelephonyManager;

    public static HwGeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        HwGeocoderProxy proxy = new HwGeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private ServiceWatcher serviceWatcherCreate(int nlp, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        String servicePackageName;
        ArrayList<String> initialPackageNames = new ArrayList<>();
        if (nlp == 1) {
            servicePackageName = "com.google.android.gms";
        } else {
            servicePackageName = AB_NLP_PKNAME;
        }
        initialPackageNames.add(servicePackageName);
        ArrayList<HashSet<Signature>> signatureSets = ServiceWatcher.getSignatureSets(this.mContext, initialPackageNames);
        ServiceWatcher serviceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, null, handler);
        mServiceWatcherUtils.setServicePackageName(serviceWatcher, servicePackageName);
        mServiceWatcherUtils.setSignatureSets(serviceWatcher, signatureSets);
        return serviceWatcher;
    }

    public HwGeocoderProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        super(context);
        this.mContext = context;
        int i = overlaySwitchResId;
        int i2 = defaultServicePackageNameResId;
        int i3 = initialPackageNamesResId;
        Handler handler2 = handler;
        this.mServiceWatcherGoogle = serviceWatcherCreate(1, i, i2, i3, handler2);
        this.mServiceWatcherAb = serviceWatcherCreate(2, i, i2, i3, handler2);
    }

    private boolean bind() {
        if (this.mServiceWatcherAb != null && HwMultiNlpPolicy.isChineseVersion()) {
            this.mStartAB = this.mServiceWatcherAb.start();
            registerPhoneStateListener();
            if (!this.mStartAB) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
        }
        if (this.mServiceWatcherGoogle != null && !HwMultiNlpPolicy.isChineseVersion()) {
            this.mStartGoogle = this.mServiceWatcherGoogle.start();
            if (!this.mStartGoogle) {
                this.mPMGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, null, UserHandle.ALL, true);
            }
        }
        this.mIsDualNlpAlive = this.mStartAB && this.mStartGoogle;
        Log.d(TAG, "mNlp:start_g " + this.mStartGoogle + " , start_ab " + this.mStartAB);
        if (this.mStartGoogle || this.mStartAB) {
            return true;
        }
        return false;
    }

    private void registerPhoneStateListener() {
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    String numeric = state.getOperatorNumeric();
                    if (numeric != null && numeric.length() >= 5 && numeric.substring(0, 5).equals("99999")) {
                        Log.d(HwGeocoderProxy.TAG, "numeric is contain 99999.");
                    } else if (numeric != null && numeric.length() >= 3 && numeric.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                        HwGeocoderProxy.this.abNlpBind();
                    } else if (numeric == null || numeric.equals("")) {
                        Log.d(HwGeocoderProxy.TAG, "numeric is null or not belong to abNlp and googleNlp.");
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
    public void abNlpBind() {
        if (!this.mStartAB && !this.mPMAbRegisted && this.mServiceWatcherAb != null) {
            this.mStartAB = this.mServiceWatcherAb.start();
            boolean z = true;
            if (!this.mStartAB && !this.mPMAbRegisted) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
            if (!this.mStartGoogle || !this.mStartAB) {
                z = false;
            }
            this.mIsDualNlpAlive = z;
        }
    }

    /* access modifiers changed from: private */
    public void googleNlpBind() {
        if (!this.mStartGoogle && !this.mPMGoogleRegisted && this.mServiceWatcherGoogle != null) {
            this.mStartGoogle = this.mServiceWatcherGoogle.start();
            boolean z = true;
            if (!this.mStartGoogle && !this.mPMGoogleRegisted) {
                this.mPMGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, null, UserHandle.ALL, true);
            }
            if (!this.mStartGoogle || !this.mStartAB) {
                z = false;
            }
            this.mIsDualNlpAlive = z;
        }
    }

    private IGeocodeProvider getServiceGoogle() {
        return IGeocodeProvider.Stub.asInterface(this.mServiceWatcherGoogle.getBinder());
    }

    private IGeocodeProvider getServiceAb() {
        return IGeocodeProvider.Stub.asInterface(this.mServiceWatcherAb.getBinder());
    }

    private IGeocodeProvider getService() {
        if (this.mIsDualNlpAlive) {
            if (this.mNlpUsed == 0) {
                if (HwMultiNlpPolicy.getDefault().shouldUseGoogleNLP(false)) {
                    this.mNlpUsed = 1;
                    return getServiceGoogle();
                }
                this.mNlpUsed = 2;
                return getServiceAb();
            } else if (this.mNlpUsed == 1) {
                return getServiceGoogle();
            } else {
                if (this.mNlpUsed == 2) {
                    return getServiceAb();
                }
                Log.d(TAG, "should not be here.");
                return null;
            }
        } else if (this.mStartGoogle) {
            return getServiceGoogle();
        } else {
            if (this.mStartAB) {
                return getServiceAb();
            }
            Log.d(TAG, "no service.");
            return null;
        }
    }

    public String getConnectedPackageName() {
        if (!this.mStartGoogle || !this.mStartAB) {
            if (this.mStartGoogle) {
                return this.mServiceWatcherGoogle.getBestPackageName();
            }
            return this.mServiceWatcherAb.getBestPackageName();
        } else if (this.mServiceWatcherGoogle.getBestPackageName() == null) {
            return this.mServiceWatcherAb.getBestPackageName();
        } else {
            if (this.mServiceWatcherAb.getBestPackageName() == null) {
                return this.mServiceWatcherGoogle.getBestPackageName();
            }
            if (this.mNlpUsed == 1) {
                return this.mServiceWatcherGoogle.getBestPackageName() + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + this.mServiceWatcherAb.getBestPackageName();
            }
            return this.mServiceWatcherAb.getBestPackageName() + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + this.mServiceWatcherGoogle.getBestPackageName();
        }
    }

    private String getFromLocationImpl(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocation(latitude, longitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        this.mNlpUsed = 0;
        String result = getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        Log.d(TAG, "getFromLocation " + result);
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

    private String getFromLocationNameImpl(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        String result;
        this.mNlpUsed = 0;
        String result2 = getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        Log.d(TAG, "getFromLocationName " + result2);
        if (this.mIsDualNlpAlive && result2 != null) {
            if (this.mNlpUsed == 1) {
                this.mNlpUsed = 2;
                result = getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } else {
                this.mNlpUsed = 1;
                result = getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            }
            GeocoderParams geocoderParams = params;
            return result;
        } else if (needRequestGlobalAb(params, result2)) {
            return getFromLocationNameGlobalAb(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        } else {
            return result2;
        }
    }

    private boolean needRequestGlobalAb(GeocoderParams params, String result) {
        String pkg = params.getClientPackage();
        if (!(GLOBAL_GEOCODER_CLIENT_PKG.equals(pkg) || (HwMultiNlpPolicy.isHwLocationDebug(this.mContext) && GLOBAL_GEOCODER_DEBUG_PKG.equals(pkg))) || !HwMultiNlpPolicy.getDefault(this.mContext).getGlobalGeocoderStart() || result == null) {
            return false;
        }
        return true;
    }

    public void handleServiceAb(boolean shouldStart) {
        if (shouldStart) {
            this.mServiceWatcherAb.start();
        } else {
            mServiceWatcherUtils.unbindLocked(this.mServiceWatcherAb);
        }
    }

    private String getFromLocationGlobalAb(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getServiceAb();
        if (provider != null) {
            try {
                return provider.getFromLocation(latitude, longitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                Log.w(TAG, e);
            }
        }
        return "Global ServiceAb not Available";
    }

    private String getFromLocationNameGlobalAb(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getServiceAb();
        if (provider != null) {
            try {
                return provider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                Log.w(TAG, e);
            }
        }
        return "Global ServiceAb not Available";
    }
}
