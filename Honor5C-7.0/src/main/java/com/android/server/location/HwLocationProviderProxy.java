package com.android.server.location;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ILocationProvider.Stub;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.ServiceWatcher;
import com.android.server.ServiceWatcherUtils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.utils.Utils;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HwLocationProviderProxy extends LocationProviderProxy implements IHwLocationProviderInterface {
    private static final int CHECK_DELAY_TIME = 10000;
    private static final boolean D = false;
    private static final int NLP_AB = 2;
    private static final int NLP_GOOGLE = 1;
    private static final int NLP_NONE = 0;
    private static final long RETENTION_PERIOD = 3600000;
    private static final String TAG = "HwLocationProviderProxy";
    private static ServiceWatcherUtils mServiceWatcherUtils;
    private int ab_nlp_pid;
    private String ab_nlp_pkName;
    private int google_nlp_pid;
    private String google_nlp_pkName;
    private boolean isDualNlpAlive;
    private Timer mCheckTimer;
    private final Context mContext;
    private boolean mEnabled;
    private long mLastCheckTime;
    private boolean mLocationMonitoring;
    private boolean mLocationSuccess_Ab;
    private boolean mLocationSuccess_google;
    private Object mLock;
    private final String mName;
    protected Runnable mNewServiceWorkAb;
    protected Runnable mNewServiceWorkGoogle;
    private int mNlpUsed;
    private boolean mPMAbRegisted;
    private boolean mPMGoogleRegisted;
    private final PackageMonitor mPackageMonitorAb;
    private final PackageMonitor mPackageMonitorGoogle;
    private PhoneStateListener mPhoneStateListener;
    private ProviderProperties mPropertiesAb;
    private ProviderProperties mPropertiesGoogle;
    private ProviderRequest mRequest;
    private ProviderRequest mRequestOff;
    private final ServiceWatcher mServiceWatcherAb;
    private ServiceWatcher mServiceWatcherDefault;
    private final ServiceWatcher mServiceWatcherGoogle;
    private TelephonyManager mTelephonyManager;
    private WorkSource mWorksource;
    private WorkSource mWorksourceOff;
    private boolean start_ab;
    private boolean start_g;

    class CheckTask extends TimerTask {
        CheckTask() {
        }

        public void run() {
            HwLocationProviderProxy.this.validNlpChoice();
            HwLocationProviderProxy.this.mLocationMonitoring = HwLocationProviderProxy.D;
        }
    }

    static {
        mServiceWatcherUtils = (ServiceWatcherUtils) EasyInvokeFactory.getInvokeUtils(ServiceWatcherUtils.class);
    }

    public static HwLocationProviderProxy createAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        HwLocationProviderProxy proxy = new HwLocationProviderProxy(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private ServiceWatcher serviceWatcherCreate(int nlp, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        String servicePackageName;
        Runnable newServiceWork;
        ArrayList<String> initialPackageNames = new ArrayList();
        if (nlp == NLP_GOOGLE) {
            servicePackageName = this.google_nlp_pkName;
            newServiceWork = this.mNewServiceWorkGoogle;
        } else {
            servicePackageName = this.ab_nlp_pkName;
            newServiceWork = this.mNewServiceWorkAb;
        }
        initialPackageNames.add(servicePackageName);
        ArrayList<HashSet<Signature>> signatureSets = ServiceWatcher.getSignatureSets(this.mContext, initialPackageNames);
        ServiceWatcher serviceWatcher = new ServiceWatcher(this.mContext, "HwLocationProviderProxy-" + this.mName, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, newServiceWork, handler);
        mServiceWatcherUtils.setServicePackageName(serviceWatcher, servicePackageName);
        mServiceWatcherUtils.setSignatureSets(serviceWatcher, signatureSets);
        return serviceWatcher;
    }

    public HwLocationProviderProxy(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        super(context, name);
        this.mLock = new Object();
        this.mEnabled = D;
        this.mRequest = null;
        this.mWorksource = new WorkSource();
        this.mLocationMonitoring = D;
        this.mLastCheckTime = 0;
        this.mNlpUsed = NLP_NONE;
        this.start_g = D;
        this.start_ab = D;
        this.mPMAbRegisted = D;
        this.mPMGoogleRegisted = D;
        this.ab_nlp_pkName = SystemProperties.get("ro.config.hw_nlp", "com.baidu.map.location");
        this.google_nlp_pkName = LocationManagerServiceUtil.GOOGLE_GMS_PROCESS;
        this.ab_nlp_pid = NLP_NONE;
        this.google_nlp_pid = NLP_NONE;
        this.mLocationSuccess_Ab = D;
        this.mLocationSuccess_google = D;
        this.mWorksourceOff = new WorkSource();
        this.mRequestOff = new ProviderRequest();
        this.mPackageMonitorGoogle = new PackageMonitor() {
            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                synchronized (HwLocationProviderProxy.this.mLock) {
                    if (packageName.equals(HwLocationProviderProxy.this.google_nlp_pkName)) {
                        Log.d(HwLocationProviderProxy.TAG, "rebind onPackageChanged " + packageName);
                        if (!(HwLocationProviderProxy.this.start_g || HwLocationProviderProxy.this.mServiceWatcherGoogle == null)) {
                            HwLocationProviderProxy.this.start_g = HwLocationProviderProxy.this.mServiceWatcherGoogle.start();
                            HwLocationProviderProxy.this.isDualNlpAlive = HwLocationProviderProxy.this.start_g ? HwLocationProviderProxy.this.start_ab : HwLocationProviderProxy.D;
                        }
                    }
                }
                return super.onPackageChanged(packageName, uid, components);
            }
        };
        this.mPackageMonitorAb = new PackageMonitor() {
            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                synchronized (HwLocationProviderProxy.this.mLock) {
                    if (packageName.equals(HwLocationProviderProxy.this.ab_nlp_pkName)) {
                        Log.d(HwLocationProviderProxy.TAG, "rebind onPackageChanged " + packageName);
                        if (!(HwLocationProviderProxy.this.start_ab || HwLocationProviderProxy.this.mServiceWatcherAb == null)) {
                            HwLocationProviderProxy.this.start_ab = HwLocationProviderProxy.this.mServiceWatcherAb.start();
                            HwLocationProviderProxy.this.isDualNlpAlive = HwLocationProviderProxy.this.start_g ? HwLocationProviderProxy.this.start_ab : HwLocationProviderProxy.D;
                        }
                    }
                }
                return super.onPackageChanged(packageName, uid, components);
            }
        };
        this.mNewServiceWorkGoogle = new Runnable() {
            public void run() {
                ProviderProperties providerProperties = null;
                synchronized (HwLocationProviderProxy.this.mLock) {
                    boolean enabled = HwLocationProviderProxy.this.mEnabled;
                    ProviderRequest request = HwLocationProviderProxy.this.mRequest;
                    WorkSource source = HwLocationProviderProxy.this.mWorksource;
                    ILocationProvider service = HwLocationProviderProxy.this.getServiceGoogle();
                }
                if (service != null) {
                    try {
                        providerProperties = service.getProperties();
                        if (providerProperties == null) {
                            Log.e(HwLocationProviderProxy.TAG, HwLocationProviderProxy.this.mServiceWatcherGoogle.getBestPackageName() + " has invalid locatino provider properties");
                        }
                        Log.d(HwLocationProviderProxy.TAG, "mNewServiceWorkGoogle use google. " + HwLocationProviderProxy.this.mNlpUsed);
                        if (enabled) {
                            service.enable();
                            if (HwLocationProviderProxy.this.mNlpUsed == HwLocationProviderProxy.NLP_GOOGLE && request != null) {
                                service.setRequest(request, source);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.w(HwLocationProviderProxy.TAG, e);
                    } catch (Exception e2) {
                        Log.e(HwLocationProviderProxy.TAG, "Exception from " + HwLocationProviderProxy.this.mServiceWatcherGoogle.getBestPackageName(), e2);
                    }
                    synchronized (HwLocationProviderProxy.this.mLock) {
                        HwLocationProviderProxy.this.mPropertiesGoogle = providerProperties;
                    }
                }
            }
        };
        this.mNewServiceWorkAb = new Runnable() {
            public void run() {
                ProviderProperties providerProperties = null;
                synchronized (HwLocationProviderProxy.this.mLock) {
                    boolean enabled = HwLocationProviderProxy.this.mEnabled;
                    ProviderRequest request = HwLocationProviderProxy.this.mRequest;
                    WorkSource source = HwLocationProviderProxy.this.mWorksource;
                    ILocationProvider service = HwLocationProviderProxy.this.getServiceAb();
                }
                if (service != null) {
                    try {
                        providerProperties = service.getProperties();
                        if (providerProperties == null) {
                            Log.e(HwLocationProviderProxy.TAG, HwLocationProviderProxy.this.mServiceWatcherAb.getBestPackageName() + " has invalid locatino provider properties");
                        }
                        Log.d(HwLocationProviderProxy.TAG, "mNewServiceWorkAb use ab. " + HwLocationProviderProxy.this.mNlpUsed);
                        if (enabled) {
                            service.enable();
                            if (HwLocationProviderProxy.this.mNlpUsed == HwLocationProviderProxy.NLP_AB && request != null) {
                                service.setRequest(request, source);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.w(HwLocationProviderProxy.TAG, e);
                    } catch (Exception e2) {
                        Log.e(HwLocationProviderProxy.TAG, "Exception from " + HwLocationProviderProxy.this.mServiceWatcherAb.getBestPackageName(), e2);
                    }
                    synchronized (HwLocationProviderProxy.this.mLock) {
                        HwLocationProviderProxy.this.mPropertiesAb = providerProperties;
                    }
                }
            }
        };
        this.mContext = context;
        this.mName = name;
        this.mRequestOff.interval = Utils.DATE_TIME_24HOURS;
        this.mServiceWatcherGoogle = serviceWatcherCreate(NLP_GOOGLE, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        this.mServiceWatcherAb = serviceWatcherCreate(NLP_AB, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        this.isDualNlpAlive = D;
        this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
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
        this.isDualNlpAlive = this.start_ab ? this.start_g : D;
        if (this.start_g) {
            this.mNlpUsed = NLP_GOOGLE;
            this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
        } else {
            this.mNlpUsed = NLP_AB;
            this.mServiceWatcherDefault = this.mServiceWatcherAb;
        }
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
                    if (numeric != null && numeric.length() >= 5 && numeric.substring(HwLocationProviderProxy.NLP_NONE, 5).equals("99999")) {
                        return;
                    }
                    if (numeric != null && numeric.length() >= 3 && numeric.substring(HwLocationProviderProxy.NLP_NONE, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                        HwLocationProviderProxy.this.abNlpBind();
                    } else if (numeric != null && !numeric.equals(AppHibernateCst.INVALID_PKG)) {
                        HwLocationProviderProxy.this.googleNlpBind();
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
            this.isDualNlpAlive = this.start_g ? this.start_ab : D;
        }
    }

    private void abNlpBind() {
        if (!this.start_ab && !this.mPMAbRegisted && this.mServiceWatcherAb != null) {
            this.start_ab = this.mServiceWatcherAb.start();
            if (!(this.start_ab || this.mPMAbRegisted)) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
            this.isDualNlpAlive = this.start_g ? this.start_ab : D;
        }
    }

    private ILocationProvider getServiceGoogle() {
        if (this.mServiceWatcherGoogle != null) {
            return Stub.asInterface(this.mServiceWatcherGoogle.getBinder());
        }
        return null;
    }

    private ILocationProvider getServiceAb() {
        if (this.mServiceWatcherAb != null) {
            return Stub.asInterface(this.mServiceWatcherAb.getBinder());
        }
        return null;
    }

    private ILocationProvider getService() {
        if (this.mServiceWatcherDefault != null) {
            return Stub.asInterface(this.mServiceWatcherDefault.getBinder());
        }
        return null;
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

    public String getName() {
        return this.mName;
    }

    public ProviderProperties getProperties() {
        ProviderProperties providerProperties;
        synchronized (this.mLock) {
            providerProperties = this.mNlpUsed == NLP_GOOGLE ? this.mPropertiesGoogle : this.mPropertiesAb;
        }
        return providerProperties;
    }

    private void enableGoolge() {
        if (this.start_g) {
            ILocationProvider service = getServiceGoogle();
            if (service != null) {
                try {
                    service.enable();
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                } catch (Exception e2) {
                    Log.e(TAG, "Exception from " + this.mServiceWatcherGoogle.getBestPackageName(), e2);
                }
            }
        }
    }

    private void enableAb() {
        if (this.start_ab) {
            ILocationProvider service = getServiceAb();
            if (service != null) {
                try {
                    service.enable();
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                } catch (Exception e2) {
                    Log.e(TAG, "Exception from " + this.mServiceWatcherGoogle.getBestPackageName(), e2);
                }
            }
        }
    }

    public void enable() {
        synchronized (this.mLock) {
            this.mEnabled = true;
        }
        enableGoolge();
        enableAb();
    }

    private void disableGoolge() {
        if (this.start_g) {
            ILocationProvider service = getServiceGoogle();
            if (service != null) {
                try {
                    service.disable();
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                } catch (Exception e2) {
                    Log.e(TAG, "Exception from " + this.mServiceWatcherGoogle.getBestPackageName(), e2);
                }
            }
        }
    }

    private void disableAb() {
        if (this.start_ab) {
            ILocationProvider service = getServiceAb();
            if (service != null) {
                try {
                    service.disable();
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                } catch (Exception e2) {
                    Log.e(TAG, "Exception from " + this.mServiceWatcherAb.getBestPackageName(), e2);
                }
            }
        }
    }

    public void disable() {
        synchronized (this.mLock) {
            this.mEnabled = D;
        }
        disableGoolge();
        disableAb();
        cancelCheckTimer();
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
    }

    private void cancelCheckTimer() {
        if (this.mCheckTimer != null) {
            this.mCheckTimer.cancel();
            this.mCheckTimer = null;
        }
    }

    private void startCheckTask(int delytime) {
        cancelCheckTimer();
        this.mCheckTimer = new Timer("LocationCheckTimer");
        this.mCheckTimer.schedule(new CheckTask(), (long) delytime);
    }

    private void validNlpChoice() {
        boolean shouldUseGoogle = HwMultiNlpPolicy.getDefault().shouldUseGoogleNLP(true);
        Log.d(TAG, "validNlpChoice google/" + this.mLocationSuccess_google + ", Ab/" + this.mLocationSuccess_Ab + " ,nlp/" + getNlpName(this.mNlpUsed) + ", shouldUseGoogle/" + shouldUseGoogle);
        if (this.mLocationSuccess_google && this.mLocationSuccess_Ab) {
            if (shouldUseGoogle) {
                if (isEnabled()) {
                    setRequest(this.mRequestOff, this.mWorksourceOff, NLP_AB);
                }
                this.mNlpUsed = NLP_GOOGLE;
                this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
            } else {
                if (isEnabled()) {
                    setRequest(this.mRequestOff, this.mWorksourceOff, NLP_GOOGLE);
                }
                this.mNlpUsed = NLP_AB;
                this.mServiceWatcherDefault = this.mServiceWatcherAb;
            }
        } else if (this.mLocationSuccess_google) {
            if (isEnabled()) {
                setRequest(this.mRequestOff, this.mWorksourceOff, NLP_AB);
            }
            this.mNlpUsed = NLP_GOOGLE;
            this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
        } else if (this.mLocationSuccess_Ab) {
            if (isEnabled()) {
                setRequest(this.mRequestOff, this.mWorksourceOff, NLP_GOOGLE);
            }
            this.mNlpUsed = NLP_AB;
            this.mServiceWatcherDefault = this.mServiceWatcherAb;
        } else if (isEnabled()) {
            this.mNlpUsed = NLP_NONE;
        }
        this.mLastCheckTime = SystemClock.elapsedRealtime();
    }

    private void setRequest(ProviderRequest request, WorkSource source, int provider) {
        ILocationProvider service = NLP_GOOGLE == provider ? getServiceGoogle() : getServiceAb();
        ServiceWatcher serviceWatcher = NLP_GOOGLE == provider ? this.mServiceWatcherGoogle : this.mServiceWatcherAb;
        Log.d(TAG, "setRequest to " + getNlpName(provider));
        if (service != null) {
            try {
                service.setRequest(request, source);
                return;
            } catch (RemoteException e) {
                Log.w(TAG, e);
                return;
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + serviceWatcher.getBestPackageName(), e2);
                return;
            }
        }
        Log.e(TAG, "setRequest service is null.");
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        Log.d(TAG, "setRequest " + request + ", mNlpUsed " + this.mNlpUsed + ",mLocationMonitoring " + this.mLocationMonitoring);
        synchronized (this.mLock) {
            this.mRequest = request;
            this.mWorksource = source;
        }
        if (!this.isDualNlpAlive || (this.mNlpUsed != 0 && (!this.mLocationMonitoring || (!HwMultiNlpPolicy.getDefault().shouldBeRecheck() && SystemClock.elapsedRealtime() <= this.mLastCheckTime + RETENTION_PERIOD)))) {
            this.mLocationMonitoring = D;
            setRequest(request, source, this.mNlpUsed);
            return;
        }
        setRequest(request, source, NLP_GOOGLE);
        setRequest(request, source, NLP_AB);
        startCheckTask(CHECK_DELAY_TIME);
    }

    public void resetNLPFlag() {
        Log.d(TAG, "resetNLPFlag ");
        if (!this.mLocationMonitoring) {
            this.mLocationMonitoring = true;
            this.mLocationSuccess_Ab = D;
            this.mLocationSuccess_google = D;
        }
    }

    private String getAppName(int pid) {
        String processName = AppHibernateCst.INVALID_PKG;
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private int reportLocationNlp(int pid) {
        int ret = NLP_NONE;
        if (pid == this.ab_nlp_pid && this.ab_nlp_pid != 0) {
            Log.d(TAG, "reportLocationNlp " + pid + ", ab");
            return NLP_AB;
        } else if (pid != this.google_nlp_pid || this.google_nlp_pid == 0) {
            String appName = getAppName(pid);
            if (this.ab_nlp_pkName.equals(appName)) {
                this.ab_nlp_pid = pid;
                ret = NLP_AB;
            }
            if (appName != null && appName.indexOf(this.google_nlp_pkName) >= 0) {
                this.google_nlp_pid = pid;
                ret = NLP_GOOGLE;
            }
            Log.d(TAG, "reportLocationNlp " + appName + ", pid " + pid);
            return ret;
        } else {
            Log.d(TAG, "reportLocationNlp " + pid + ", google");
            return NLP_GOOGLE;
        }
    }

    public boolean reportNLPLocation(int pid) {
        Log.d(TAG, "reportNLPLocation " + pid + ", nlpUsed " + this.mNlpUsed + ", dualNlp " + this.isDualNlpAlive + ", monitoring " + this.mLocationMonitoring);
        boolean ret = true;
        if (this.isDualNlpAlive) {
            int nlp = reportLocationNlp(pid);
            if (this.mLocationMonitoring) {
                if (nlp == NLP_AB) {
                    this.mLocationSuccess_Ab = true;
                } else if (nlp == NLP_GOOGLE) {
                    this.mLocationSuccess_google = true;
                }
                if (nlp == HwMultiNlpPolicy.getDefault().shouldUseNLP() || ((nlp == NLP_AB && !this.mLocationSuccess_google) || (nlp == NLP_GOOGLE && !this.mLocationSuccess_Ab))) {
                    ret = true;
                } else {
                    ret = D;
                }
            } else if (nlp == this.mNlpUsed) {
                ret = true;
            } else if (this.mNlpUsed == 0) {
                if (nlp == NLP_AB) {
                    this.mLocationSuccess_Ab = true;
                } else if (nlp == NLP_GOOGLE) {
                    this.mLocationSuccess_google = true;
                }
                validNlpChoice();
                ret = true;
            } else {
                ret = D;
            }
            Log.d(TAG, "shouldReportLocation . " + ret + ", from " + getNlpName(nlp));
        }
        return ret;
    }

    private String getNlpName(int nlp) {
        switch (nlp) {
            case NLP_GOOGLE /*1*/:
                return "google map";
            case NLP_AB /*2*/:
                return "Amap or baidu map";
            default:
                return "unkonw";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.append("REMOTE SERVICE");
        pw.append(" name=").append(this.mName);
        pw.append(" pkg=").append(this.mServiceWatcherDefault.getBestPackageName());
        pw.append(" version=").append(AppHibernateCst.INVALID_PKG + this.mServiceWatcherDefault.getBestVersion());
        pw.append('\n');
        ILocationProvider service = getService();
        if (service == null) {
            pw.println("service down (null)");
            return;
        }
        pw.flush();
        try {
            service.asBinder().dump(fd, args);
        } catch (RemoteException e) {
            pw.println("service down (RemoteException)");
            Log.w(TAG, e);
        } catch (Exception e2) {
            pw.println("service down (Exception)");
            Log.e(TAG, "Exception from " + this.mServiceWatcherDefault.getBestPackageName(), e2);
        }
    }

    public int getStatus(Bundle extras) {
        ILocationProvider service = getService();
        if (service == null) {
            return NLP_GOOGLE;
        }
        try {
            return service.getStatus(extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
            return NLP_GOOGLE;
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcherDefault.getBestPackageName(), e2);
            return NLP_GOOGLE;
        }
    }

    public long getStatusUpdateTime() {
        ILocationProvider service = getService();
        if (service == null) {
            return 0;
        }
        try {
            return service.getStatusUpdateTime();
        } catch (RemoteException e) {
            Log.w(TAG, e);
            return 0;
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcherDefault.getBestPackageName(), e2);
            return 0;
        }
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        ILocationProvider service = getService();
        if (service == null) {
            return D;
        }
        try {
            return service.sendExtraCommand(command, extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
            return D;
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcherDefault.getBestPackageName(), e2);
            return D;
        }
    }
}
