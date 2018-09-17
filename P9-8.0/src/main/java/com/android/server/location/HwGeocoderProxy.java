package com.android.server.location;

import android.content.Context;
import android.content.pm.Signature;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.location.IGeocodeProvider.Stub;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.ServiceWatcher;
import com.android.server.ServiceWatcherUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HwGeocoderProxy extends GeocoderProxy {
    private static final int NLP_AB = 2;
    private static final int NLP_GOOGLE = 1;
    private static final int NLP_NONE = 0;
    private static final String SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String TAG = "HwGeocoderProxy";
    private static ServiceWatcherUtils mServiceWatcherUtils = ((ServiceWatcherUtils) EasyInvokeFactory.getInvokeUtils(ServiceWatcherUtils.class));
    private String ab_nlp_pkName = SystemProperties.get("ro.config.hw_nlp", "com.baidu.map.location");
    private String google_nlp_pkName = LocationManagerServiceUtil.GOOGLE_GMS_PROCESS;
    private boolean isDualNlpAlive;
    private final Context mContext;
    private Object mLock = new Object();
    private int mNlpUsed = 0;
    private boolean mPMAbRegisted = false;
    private boolean mPMGoogleRegisted = false;
    private final PackageMonitor mPackageMonitorAb = new PackageMonitor() {
        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwGeocoderProxy.this.mLock) {
                if (packageName.equals(HwGeocoderProxy.this.ab_nlp_pkName)) {
                    Log.d(HwGeocoderProxy.TAG, "rebind onPackageChanged packageName : " + packageName);
                    if (!(HwGeocoderProxy.this.start_ab || HwGeocoderProxy.this.mServiceWatcherAb == null)) {
                        HwGeocoderProxy.this.start_ab = HwGeocoderProxy.this.mServiceWatcherAb.start();
                        HwGeocoderProxy.this.isDualNlpAlive = HwGeocoderProxy.this.start_g ? HwGeocoderProxy.this.start_ab : false;
                    }
                }
            }
            return super.onPackageChanged(packageName, uid, components);
        }
    };
    private final PackageMonitor mPackageMonitorGoogle = new PackageMonitor() {
        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwGeocoderProxy.this.mLock) {
                if (packageName.equals(HwGeocoderProxy.this.google_nlp_pkName)) {
                    Log.d(HwGeocoderProxy.TAG, "rebind onPackageChanged packageName : " + packageName);
                    if (!(HwGeocoderProxy.this.start_g || HwGeocoderProxy.this.mServiceWatcherGoogle == null)) {
                        HwGeocoderProxy.this.start_g = HwGeocoderProxy.this.mServiceWatcherGoogle.start();
                        HwGeocoderProxy.this.isDualNlpAlive = HwGeocoderProxy.this.start_g ? HwGeocoderProxy.this.start_ab : false;
                    }
                }
            }
            return super.onPackageChanged(packageName, uid, components);
        }
    };
    private PhoneStateListener mPhoneStateListener;
    private final ServiceWatcher mServiceWatcherAb;
    private final ServiceWatcher mServiceWatcherGoogle;
    private TelephonyManager mTelephonyManager;
    private boolean start_ab;
    private boolean start_g;

    public static HwGeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        HwGeocoderProxy proxy = new HwGeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private ServiceWatcher serviceWatcherCreate(int nlp, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        String servicePackageName;
        ArrayList<String> initialPackageNames = new ArrayList();
        if (nlp == 1) {
            servicePackageName = this.google_nlp_pkName;
        } else {
            servicePackageName = this.ab_nlp_pkName;
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
        this.mServiceWatcherGoogle = serviceWatcherCreate(1, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        this.mServiceWatcherAb = serviceWatcherCreate(2, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    private boolean bind() {
        if (this.mServiceWatcherAb != null && HwMultiNlpPolicy.isChineseVersion()) {
            this.start_ab = this.mServiceWatcherAb.start();
            if (!this.start_ab) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
        }
        if (!(this.mServiceWatcherGoogle == null || (HwMultiNlpPolicy.isChineseVersion() ^ 1) == 0)) {
            this.start_g = this.mServiceWatcherGoogle.start();
            if (!this.start_g) {
                this.mPMGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, null, UserHandle.ALL, true);
            }
        }
        registerPhoneStateListener();
        this.isDualNlpAlive = this.start_ab ? this.start_g : false;
        Log.d(TAG, "mNlp:start_g " + this.start_g + " , start_ab " + this.start_ab);
        if (this.start_g) {
            return true;
        }
        return this.start_ab;
    }

    private void registerPhoneStateListener() {
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    String numeric = state.getOperatorNumeric();
                    if (numeric != null && numeric.length() >= 5 && numeric.substring(0, 5).equals("99999")) {
                        return;
                    }
                    if (numeric != null && numeric.length() >= 3 && numeric.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                        HwGeocoderProxy.this.abNlpBind();
                    } else if (numeric != null && (numeric.equals("") ^ 1) != 0) {
                        HwGeocoderProxy.this.googleNlpBind();
                    }
                }
            }
        };
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
    }

    private void abNlpBind() {
        if (!this.start_ab && (this.mPMAbRegisted ^ 1) != 0 && this.mServiceWatcherAb != null) {
            this.start_ab = this.mServiceWatcherAb.start();
            if (!(this.start_ab || (this.mPMAbRegisted ^ 1) == 0)) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
            this.isDualNlpAlive = this.start_g ? this.start_ab : false;
        }
    }

    private void googleNlpBind() {
        if (!this.start_g && (this.mPMGoogleRegisted ^ 1) != 0 && this.mServiceWatcherGoogle != null) {
            this.start_g = this.mServiceWatcherGoogle.start();
            if (!(this.start_g || (this.mPMGoogleRegisted ^ 1) == 0)) {
                this.mPMGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, null, UserHandle.ALL, true);
            }
            this.isDualNlpAlive = this.start_g ? this.start_ab : false;
        }
    }

    private IGeocodeProvider getServiceGoogle() {
        return Stub.asInterface(this.mServiceWatcherGoogle.getBinder());
    }

    private IGeocodeProvider getServiceAb() {
        return Stub.asInterface(this.mServiceWatcherAb.getBinder());
    }

    private IGeocodeProvider getService() {
        if (this.isDualNlpAlive) {
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
        } else if (this.start_g) {
            return getServiceGoogle();
        } else {
            if (this.start_ab) {
                return getServiceAb();
            }
            Log.d(TAG, "no service.");
            return null;
        }
    }

    public String getConnectedPackageName() {
        if (this.start_g && this.start_ab) {
            if (this.mServiceWatcherGoogle.getBestPackageName() == null) {
                return this.mServiceWatcherAb.getBestPackageName();
            }
            if (this.mServiceWatcherAb.getBestPackageName() == null) {
                return this.mServiceWatcherGoogle.getBestPackageName();
            }
            if (this.mNlpUsed == 1) {
                return this.mServiceWatcherGoogle.getBestPackageName() + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + this.mServiceWatcherAb.getBestPackageName();
            }
            return this.mServiceWatcherAb.getBestPackageName() + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + this.mServiceWatcherGoogle.getBestPackageName();
        } else if (this.start_g) {
            return this.mServiceWatcherGoogle.getBestPackageName();
        } else {
            return this.mServiceWatcherAb.getBestPackageName();
        }
    }

    private String getFromLocationImpl(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocation(latitude, longitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        this.mNlpUsed = 0;
        String result = getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        Log.d(TAG, "getFromLocation " + result);
        if (!this.isDualNlpAlive || result == null) {
            return result;
        }
        if (this.mNlpUsed == 1) {
            this.mNlpUsed = 2;
            return getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        }
        this.mNlpUsed = 1;
        return getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
    }

    private String getFromLocationNameImpl(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        this.mNlpUsed = 0;
        String result = getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        Log.d(TAG, "getFromLocationName " + result);
        if (!this.isDualNlpAlive || result == null) {
            return result;
        }
        if (this.mNlpUsed == 1) {
            this.mNlpUsed = 2;
            return getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
        this.mNlpUsed = 1;
        return getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
    }
}
