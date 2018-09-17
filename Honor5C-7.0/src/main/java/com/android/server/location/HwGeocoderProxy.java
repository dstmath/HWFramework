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
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
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
    private static ServiceWatcherUtils mServiceWatcherUtils;
    private String ab_nlp_pkName;
    private String google_nlp_pkName;
    private boolean isDualNlpAlive;
    private final Context mContext;
    private Object mLock;
    private int mNlpUsed;
    private boolean mPMAbRegisted;
    private boolean mPMGoogleRegisted;
    private final PackageMonitor mPackageMonitorAb;
    private final PackageMonitor mPackageMonitorGoogle;
    private PhoneStateListener mPhoneStateListener;
    private final ServiceWatcher mServiceWatcherAb;
    private final ServiceWatcher mServiceWatcherGoogle;
    private TelephonyManager mTelephonyManager;
    private boolean start_ab;
    private boolean start_g;

    static {
        mServiceWatcherUtils = (ServiceWatcherUtils) EasyInvokeFactory.getInvokeUtils(ServiceWatcherUtils.class);
    }

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
        if (nlp == NLP_GOOGLE) {
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
        this.ab_nlp_pkName = SystemProperties.get("ro.config.hw_nlp", "com.baidu.map.location");
        this.google_nlp_pkName = LocationManagerServiceUtil.GOOGLE_GMS_PROCESS;
        this.mLock = new Object();
        this.mNlpUsed = NLP_NONE;
        this.mPMAbRegisted = false;
        this.mPMGoogleRegisted = false;
        this.mPackageMonitorGoogle = new PackageMonitor() {
            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                synchronized (HwGeocoderProxy.this.mLock) {
                    if (packageName.equals(HwGeocoderProxy.this.google_nlp_pkName)) {
                        Log.d(HwGeocoderProxy.TAG, "rebind onPackageChanged " + packageName);
                        if (!(HwGeocoderProxy.this.start_g || HwGeocoderProxy.this.mServiceWatcherGoogle == null)) {
                            HwGeocoderProxy.this.start_g = HwGeocoderProxy.this.mServiceWatcherGoogle.start();
                            HwGeocoderProxy.this.isDualNlpAlive = HwGeocoderProxy.this.start_g ? HwGeocoderProxy.this.start_ab : false;
                        }
                    }
                }
                return super.onPackageChanged(packageName, uid, components);
            }
        };
        this.mPackageMonitorAb = new PackageMonitor() {
            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                synchronized (HwGeocoderProxy.this.mLock) {
                    if (packageName.equals(HwGeocoderProxy.this.ab_nlp_pkName)) {
                        Log.d(HwGeocoderProxy.TAG, "rebind onPackageChanged " + packageName);
                        if (!(HwGeocoderProxy.this.start_ab || HwGeocoderProxy.this.mServiceWatcherAb == null)) {
                            HwGeocoderProxy.this.start_ab = HwGeocoderProxy.this.mServiceWatcherAb.start();
                            HwGeocoderProxy.this.isDualNlpAlive = HwGeocoderProxy.this.start_g ? HwGeocoderProxy.this.start_ab : false;
                        }
                    }
                }
                return super.onPackageChanged(packageName, uid, components);
            }
        };
        this.mContext = context;
        this.mServiceWatcherGoogle = serviceWatcherCreate(NLP_GOOGLE, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        this.mServiceWatcherAb = serviceWatcherCreate(NLP_AB, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    private boolean bind() {
        if (this.mServiceWatcherAb != null && HwMultiNlpPolicy.isChineseVersion()) {
            this.start_ab = this.mServiceWatcherAb.start();
            if (!this.start_ab) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
        }
        if (!(this.mServiceWatcherGoogle == null || HwMultiNlpPolicy.isChineseVersion())) {
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
                    if (numeric != null && numeric.length() >= 5 && numeric.substring(HwGeocoderProxy.NLP_NONE, 5).equals("99999")) {
                        return;
                    }
                    if (numeric != null && numeric.length() >= 3 && numeric.substring(HwGeocoderProxy.NLP_NONE, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                        HwGeocoderProxy.this.abNlpBind();
                    } else if (numeric != null && !numeric.equals(AppHibernateCst.INVALID_PKG)) {
                        HwGeocoderProxy.this.googleNlpBind();
                    }
                }
            }
        };
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager.listen(this.mPhoneStateListener, NLP_GOOGLE);
    }

    private void googleNlpBind() {
        if (!this.start_g && !this.mPMGoogleRegisted && this.mServiceWatcherGoogle != null) {
            this.start_g = this.mServiceWatcherGoogle.start();
            if (!(this.start_g || this.mPMGoogleRegisted)) {
                this.mPMGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, null, UserHandle.ALL, true);
            }
            this.isDualNlpAlive = this.start_g ? this.start_ab : false;
        }
    }

    private void abNlpBind() {
        if (!this.start_ab && !this.mPMAbRegisted && this.mServiceWatcherAb != null) {
            this.start_ab = this.mServiceWatcherAb.start();
            if (!(this.start_ab || this.mPMAbRegisted)) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
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
                    this.mNlpUsed = NLP_GOOGLE;
                    return getServiceGoogle();
                }
                this.mNlpUsed = NLP_AB;
                return getServiceAb();
            } else if (this.mNlpUsed == NLP_GOOGLE) {
                return getServiceGoogle();
            } else {
                if (this.mNlpUsed == NLP_AB) {
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
            if (this.mNlpUsed == NLP_GOOGLE) {
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
        this.mNlpUsed = NLP_NONE;
        String result = getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        Log.d(TAG, "getFromLocation " + result);
        if (!this.isDualNlpAlive || result == null) {
            return result;
        }
        if (this.mNlpUsed == NLP_GOOGLE) {
            this.mNlpUsed = NLP_AB;
            return getFromLocationImpl(latitude, longitude, maxResults, params, addrs);
        }
        this.mNlpUsed = NLP_GOOGLE;
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
        this.mNlpUsed = NLP_NONE;
        String result = getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        Log.d(TAG, "getFromLocationName " + result);
        if (!this.isDualNlpAlive || result == null) {
            return result;
        }
        if (this.mNlpUsed == NLP_GOOGLE) {
            this.mNlpUsed = NLP_AB;
            return getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
        this.mNlpUsed = NLP_GOOGLE;
        return getFromLocationNameImpl(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
    }
}
