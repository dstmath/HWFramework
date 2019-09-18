package com.android.server.location;

import android.app.ActivityManager;
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
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.ServiceWatcher;
import com.android.server.ServiceWatcherUtils;
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
    /* access modifiers changed from: private */
    public static final String AB_NLP_PKNAME = SystemProperties.get("ro.config.hw_nlp", "com.huawei.lbs");
    private static final int CHECK_DELAY_TIME = 10000;
    private static final boolean D = false;
    private static final String GLOBAL_NLP_CLIENT_PKG = "com.hisi.mapcon";
    private static final String GLOBAL_NLP_DEBUG_PKG = "com.huawei.android.gpsselfcheck";
    private static final String GOOGLE_NLP_PKNAME = "com.google.android.gms";
    private static final int NLP_AB = 2;
    private static final int NLP_GOOGLE = 1;
    private static final int NLP_NONE = 0;
    private static final long RETENTION_PERIOD = 3600000;
    private static final String TAG = "HwLocationProviderProxy";
    private static ServiceWatcherUtils mServiceWatcherUtils = EasyInvokeFactory.getInvokeUtils(ServiceWatcherUtils.class);
    /* access modifiers changed from: private */
    public boolean isDualNlpAlive;
    private int mABNlpPid = 0;
    private Timer mCheckTimer;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mEnabled = false;
    private int mGoogleNlpPid = 0;
    private long mLastCheckTime = 0;
    /* access modifiers changed from: private */
    public boolean mLocationMonitoring = false;
    private boolean mLocationSuccessAB = false;
    private boolean mLocationSuccessGoogle = false;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private final String mName;
    protected Runnable mNewServiceWorkAb = new Runnable() {
        public void run() {
            boolean enabled;
            ProviderRequest request;
            WorkSource source;
            ILocationProvider service;
            ProviderProperties properties = null;
            synchronized (HwLocationProviderProxy.this.mLock) {
                enabled = HwLocationProviderProxy.this.mEnabled;
                request = HwLocationProviderProxy.this.mRequest;
                source = HwLocationProviderProxy.this.mWorksource;
                service = HwLocationProviderProxy.this.getServiceAb();
            }
            if (service != null) {
                try {
                    properties = service.getProperties();
                    if (properties == null) {
                        Log.e(HwLocationProviderProxy.TAG, HwLocationProviderProxy.this.mServiceWatcherAb.getBestPackageName() + " has invalid locatino provider properties");
                    }
                    Log.d(HwLocationProviderProxy.TAG, "mNewServiceWorkAb use ab. " + HwLocationProviderProxy.this.mNlpUsed);
                    if (enabled) {
                        service.enable();
                        if (HwLocationProviderProxy.this.mNlpUsed == 2) {
                            if (request != null) {
                                service.setRequest(request, source);
                            }
                        } else if (HwMultiNlpPolicy.getDefault(HwLocationProviderProxy.this.mContext).getGlobalNLPStart() && request != null && HwLocationProviderProxy.this.isWorkSourceContain(source, HwLocationProviderProxy.GLOBAL_NLP_CLIENT_PKG)) {
                            service.setRequest(request, source);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(HwLocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(HwLocationProviderProxy.TAG, "Exception from " + HwLocationProviderProxy.this.mServiceWatcherAb.getBestPackageName(), e2);
                }
                ProviderProperties properties2 = properties;
                synchronized (HwLocationProviderProxy.this.mLock) {
                    ProviderProperties unused = HwLocationProviderProxy.this.mPropertiesAb = properties2;
                }
            }
        }
    };
    protected Runnable mNewServiceWorkGoogle = new Runnable() {
        public void run() {
            boolean enabled;
            ProviderRequest request;
            WorkSource source;
            ILocationProvider service;
            ProviderProperties properties = null;
            synchronized (HwLocationProviderProxy.this.mLock) {
                enabled = HwLocationProviderProxy.this.mEnabled;
                request = HwLocationProviderProxy.this.mRequest;
                source = HwLocationProviderProxy.this.mWorksource;
                service = HwLocationProviderProxy.this.getServiceGoogle();
            }
            if (service != null) {
                try {
                    properties = service.getProperties();
                    if (properties == null) {
                        Log.e(HwLocationProviderProxy.TAG, HwLocationProviderProxy.this.mServiceWatcherGoogle.getBestPackageName() + " has invalid locatino provider properties");
                    }
                    Log.d(HwLocationProviderProxy.TAG, "mNewServiceWorkGoogle use google. " + HwLocationProviderProxy.this.mNlpUsed);
                    if (enabled) {
                        service.enable();
                        if (HwLocationProviderProxy.this.mNlpUsed == 1 && request != null) {
                            service.setRequest(request, source);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(HwLocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(HwLocationProviderProxy.TAG, "Exception from " + HwLocationProviderProxy.this.mServiceWatcherGoogle.getBestPackageName(), e2);
                }
                ProviderProperties properties2 = properties;
                synchronized (HwLocationProviderProxy.this.mLock) {
                    ProviderProperties unused = HwLocationProviderProxy.this.mPropertiesGoogle = properties2;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mNlpUsed = 0;
    private boolean mPMAbRegisted = false;
    private boolean mPMGoogleRegisted = false;
    private final PackageMonitor mPackageMonitorAb = new PackageMonitor() {
        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwLocationProviderProxy.this.mLock) {
                if (packageName.equals(HwLocationProviderProxy.AB_NLP_PKNAME)) {
                    Log.d(HwLocationProviderProxy.TAG, "rebind onPackageChanged " + packageName);
                    if (!HwLocationProviderProxy.this.mStartAB && HwLocationProviderProxy.this.mServiceWatcherAb != null) {
                        boolean unused = HwLocationProviderProxy.this.mStartAB = HwLocationProviderProxy.this.mServiceWatcherAb.start();
                        boolean unused2 = HwLocationProviderProxy.this.isDualNlpAlive = HwLocationProviderProxy.this.mStartGoogle && HwLocationProviderProxy.this.mStartAB;
                    }
                }
            }
            return HwLocationProviderProxy.super.onPackageChanged(packageName, uid, components);
        }
    };
    private final PackageMonitor mPackageMonitorGoogle = new PackageMonitor() {
        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (HwLocationProviderProxy.this.mLock) {
                if (packageName.equals("com.google.android.gms")) {
                    Log.d(HwLocationProviderProxy.TAG, "rebind onPackageChanged " + packageName);
                    if (!HwLocationProviderProxy.this.mStartGoogle && HwLocationProviderProxy.this.mServiceWatcherGoogle != null) {
                        boolean unused = HwLocationProviderProxy.this.mStartGoogle = HwLocationProviderProxy.this.mServiceWatcherGoogle.start();
                        boolean unused2 = HwLocationProviderProxy.this.isDualNlpAlive = HwLocationProviderProxy.this.mStartGoogle && HwLocationProviderProxy.this.mStartAB;
                    }
                }
            }
            return HwLocationProviderProxy.super.onPackageChanged(packageName, uid, components);
        }
    };
    private PhoneStateListener mPhoneStateListener;
    /* access modifiers changed from: private */
    public ProviderProperties mPropertiesAb;
    /* access modifiers changed from: private */
    public ProviderProperties mPropertiesGoogle;
    /* access modifiers changed from: private */
    public ProviderRequest mRequest = null;
    private ProviderRequest mRequestOff = new ProviderRequest();
    /* access modifiers changed from: private */
    public final ServiceWatcher mServiceWatcherAb;
    private ServiceWatcher mServiceWatcherDefault;
    /* access modifiers changed from: private */
    public final ServiceWatcher mServiceWatcherGoogle;
    /* access modifiers changed from: private */
    public boolean mStartAB = false;
    /* access modifiers changed from: private */
    public boolean mStartGoogle = false;
    private TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public WorkSource mWorksource = new WorkSource();
    private WorkSource mWorksourceOff = new WorkSource();

    class CheckTask extends TimerTask {
        CheckTask() {
        }

        public void run() {
            HwLocationProviderProxy.this.validNlpChoice();
            boolean unused = HwLocationProviderProxy.this.mLocationMonitoring = false;
        }
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
        Runnable runnable;
        ArrayList<String> initialPackageNames = new ArrayList<>();
        if (nlp == 1) {
            servicePackageName = "com.google.android.gms";
            runnable = this.mNewServiceWorkGoogle;
        } else {
            servicePackageName = AB_NLP_PKNAME;
            runnable = this.mNewServiceWorkAb;
        }
        Runnable newServiceWork = runnable;
        initialPackageNames.add(servicePackageName);
        ArrayList<HashSet<Signature>> signatureSets = ServiceWatcher.getSignatureSets(this.mContext, initialPackageNames);
        Context context = this.mContext;
        ServiceWatcher serviceWatcher = new ServiceWatcher(context, "HwLocationProviderProxy-" + this.mName, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, newServiceWork, handler);
        mServiceWatcherUtils.setServicePackageName(serviceWatcher, servicePackageName);
        mServiceWatcherUtils.setSignatureSets(serviceWatcher, signatureSets);
        return serviceWatcher;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HwLocationProviderProxy(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        super(context, name);
        Context context2 = context;
        this.mContext = context2;
        this.mName = name;
        this.mRequestOff.interval = 86400000;
        String str = action;
        int i = overlaySwitchResId;
        int i2 = defaultServicePackageNameResId;
        int i3 = initialPackageNamesResId;
        Handler handler2 = handler;
        this.mServiceWatcherGoogle = serviceWatcherCreate(1, str, i, i2, i3, handler2);
        this.mServiceWatcherAb = serviceWatcherCreate(2, str, i, i2, i3, handler2);
        this.isDualNlpAlive = false;
        this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
        HwMultiNlpPolicy.getDefault(context2).initHwNLPVowifi(handler);
    }

    private boolean bind() {
        if (this.mServiceWatcherAb != null && HwMultiNlpPolicy.isChineseVersion()) {
            this.mStartAB = this.mServiceWatcherAb.start();
            if (!this.mStartAB) {
                this.mPMAbRegisted = true;
                this.mPackageMonitorAb.register(this.mContext, null, UserHandle.ALL, true);
            }
            registerPhoneStateListener();
        }
        if (this.mServiceWatcherGoogle != null && !HwMultiNlpPolicy.isChineseVersion()) {
            this.mStartGoogle = this.mServiceWatcherGoogle.start();
            if (!this.mStartGoogle) {
                this.mPMGoogleRegisted = true;
                this.mPackageMonitorGoogle.register(this.mContext, null, UserHandle.ALL, true);
            }
        }
        this.isDualNlpAlive = this.mStartAB && this.mStartGoogle;
        if (this.mStartGoogle) {
            this.mNlpUsed = 1;
            this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
        } else {
            this.mNlpUsed = 2;
            this.mServiceWatcherDefault = this.mServiceWatcherAb;
        }
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
                        return;
                    }
                    if (numeric != null && numeric.length() >= 3 && numeric.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                        HwLocationProviderProxy.this.abNlpBind();
                    } else if (numeric != null && !numeric.equals("")) {
                        HwLocationProviderProxy.this.googleNlpBind();
                    }
                }
            }
        };
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
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
            this.isDualNlpAlive = z;
        }
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
            this.isDualNlpAlive = z;
        }
    }

    /* access modifiers changed from: private */
    public ILocationProvider getServiceGoogle() {
        if (this.mServiceWatcherGoogle != null) {
            return ILocationProvider.Stub.asInterface(this.mServiceWatcherGoogle.getBinder());
        }
        return null;
    }

    /* access modifiers changed from: private */
    public ILocationProvider getServiceAb() {
        if (this.mServiceWatcherAb != null) {
            return ILocationProvider.Stub.asInterface(this.mServiceWatcherAb.getBinder());
        }
        return null;
    }

    private ILocationProvider getService() {
        if (this.mServiceWatcherDefault != null) {
            return ILocationProvider.Stub.asInterface(this.mServiceWatcherDefault.getBinder());
        }
        return null;
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
            if (1 == this.mNlpUsed) {
                return this.mServiceWatcherGoogle.getBestPackageName() + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + this.mServiceWatcherAb.getBestPackageName();
            }
            return this.mServiceWatcherAb.getBestPackageName() + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + this.mServiceWatcherGoogle.getBestPackageName();
        }
    }

    public String getName() {
        return this.mName;
    }

    public ProviderProperties getProperties() {
        ProviderProperties providerProperties;
        synchronized (this.mLock) {
            providerProperties = this.mNlpUsed == 1 ? this.mPropertiesGoogle : this.mPropertiesAb;
        }
        return providerProperties;
    }

    private void enableGoolge() {
        if (this.mStartGoogle) {
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
        if (this.mStartAB) {
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
        if (this.mStartGoogle) {
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
        if (this.mStartAB) {
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
            this.mEnabled = false;
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

    /* access modifiers changed from: private */
    public void validNlpChoice() {
        boolean shouldUseGoogle = HwMultiNlpPolicy.getDefault().shouldUseGoogleNLP(true);
        Log.d(TAG, "validNlpChoice google/" + this.mLocationSuccessGoogle + ", Ab/" + this.mLocationSuccessAB + " ,nlp/" + getNlpName(this.mNlpUsed) + ", shouldUseGoogle/" + shouldUseGoogle);
        if (!this.mLocationSuccessGoogle || !this.mLocationSuccessAB) {
            if (this.mLocationSuccessGoogle) {
                if (isEnabled()) {
                    setRequest(this.mRequestOff, this.mWorksourceOff, 2);
                }
                this.mNlpUsed = 1;
                this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
            } else if (this.mLocationSuccessAB) {
                if (isEnabled()) {
                    setRequest(this.mRequestOff, this.mWorksourceOff, 1);
                }
                this.mNlpUsed = 2;
                this.mServiceWatcherDefault = this.mServiceWatcherAb;
            } else if (isEnabled()) {
                this.mNlpUsed = 0;
            }
        } else if (shouldUseGoogle) {
            if (isEnabled()) {
                setRequest(this.mRequestOff, this.mWorksourceOff, 2);
            }
            this.mNlpUsed = 1;
            this.mServiceWatcherDefault = this.mServiceWatcherGoogle;
        } else {
            if (isEnabled()) {
                setRequest(this.mRequestOff, this.mWorksourceOff, 1);
            }
            this.mNlpUsed = 2;
            this.mServiceWatcherDefault = this.mServiceWatcherAb;
        }
        this.mLastCheckTime = SystemClock.elapsedRealtime();
    }

    private void setRequest(ProviderRequest request, WorkSource source, int provider) {
        ILocationProvider service = 1 == provider ? getServiceGoogle() : getServiceAb();
        ServiceWatcher serviceWatcher = 1 == provider ? this.mServiceWatcherGoogle : this.mServiceWatcherAb;
        Log.d(TAG, "setRequest to " + getNlpName(provider));
        if (service != null) {
            try {
                service.setRequest(request, source);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            } catch (Exception e2) {
                Log.e(TAG, "Exception from " + serviceWatcher.getBestPackageName(), e2);
            }
        } else {
            Log.e(TAG, "setRequest service is null.");
        }
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        Log.d(TAG, "setRequest " + request + ", mNlpUsed " + this.mNlpUsed + ",mLocationMonitoring " + this.mLocationMonitoring);
        synchronized (this.mLock) {
            this.mRequest = request;
            this.mWorksource = source;
        }
        if (!this.isDualNlpAlive || (this.mNlpUsed != 0 && (!this.mLocationMonitoring || (!HwMultiNlpPolicy.getDefault().shouldBeRecheck() && SystemClock.elapsedRealtime() <= this.mLastCheckTime + 3600000)))) {
            this.mLocationMonitoring = false;
            setRequest(request, source, this.mNlpUsed);
            if (HwMultiNlpPolicy.getDefault(this.mContext).getGlobalNLPStart()) {
                if (isWorkSourceContain(source, GLOBAL_NLP_CLIENT_PKG)) {
                    setRequest(request, source, 2);
                } else {
                    setRequest(this.mRequestOff, this.mWorksourceOff, 2);
                }
            }
        } else {
            setRequest(request, source, 1);
            setRequest(request, source, 2);
            startCheckTask(10000);
        }
    }

    public void resetNLPFlag() {
        Log.d(TAG, "resetNLPFlag ");
        if (!this.mLocationMonitoring) {
            this.mLocationMonitoring = true;
            this.mLocationSuccessAB = false;
            this.mLocationSuccessGoogle = false;
        }
    }

    private String getAppName(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (pid == appProcess.pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private int reportLocationNlp(int pid) {
        int ret = 0;
        if (pid == this.mABNlpPid && this.mABNlpPid != 0) {
            Log.d(TAG, "reportLocationNlp " + pid + ", ab");
            return 2;
        } else if (pid != this.mGoogleNlpPid || this.mGoogleNlpPid == 0) {
            String appName = getAppName(pid);
            if (AB_NLP_PKNAME.equals(appName)) {
                this.mABNlpPid = pid;
                ret = 2;
            }
            if (appName != null && appName.indexOf("com.google.android.gms") >= 0) {
                this.mGoogleNlpPid = pid;
                ret = 1;
            }
            Log.d(TAG, "reportLocationNlp " + appName + ", pid " + pid);
            return ret;
        } else {
            Log.d(TAG, "reportLocationNlp " + pid + ", google");
            return 1;
        }
    }

    public boolean reportNLPLocation(int pid) {
        boolean ret;
        Log.d(TAG, "reportNLPLocation " + pid + ", nlpUsed " + this.mNlpUsed + ", dualNlp " + this.isDualNlpAlive + ", monitoring " + this.mLocationMonitoring);
        boolean ret2 = true;
        int nlp = reportLocationNlp(pid);
        if (this.isDualNlpAlive) {
            if (this.mLocationMonitoring) {
                if (nlp == 2) {
                    this.mLocationSuccessAB = true;
                } else if (nlp == 1) {
                    this.mLocationSuccessGoogle = true;
                }
                if (nlp == HwMultiNlpPolicy.getDefault().shouldUseNLP() || ((nlp == 2 && !this.mLocationSuccessGoogle) || (nlp == 1 && !this.mLocationSuccessAB))) {
                    ret = true;
                } else {
                    ret = false;
                }
            } else if (nlp == this.mNlpUsed) {
                ret = true;
            } else if (this.mNlpUsed == 0) {
                if (nlp == 2) {
                    this.mLocationSuccessAB = true;
                } else if (nlp == 1) {
                    this.mLocationSuccessGoogle = true;
                }
                validNlpChoice();
                ret = true;
            } else {
                ret = false;
            }
            Log.d(TAG, "shouldReportLocation . " + ret + ", from " + getNlpName(nlp));
            return ret;
        }
        if (nlp == 1) {
            this.mLocationSuccessGoogle = true;
            ret2 = true;
        }
        if (!HwMultiNlpPolicy.getDefault(this.mContext).getGlobalNLPStart() || !this.mLocationSuccessGoogle || nlp != 2) {
            return ret2;
        }
        setRequest(this.mRequestOff, this.mWorksourceOff, 2);
        return false;
    }

    private String getNlpName(int nlp) {
        switch (nlp) {
            case 1:
                return "google map";
            case 2:
                return "Amap or baidu map";
            default:
                return "unkonw";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.append("REMOTE SERVICE");
        pw.append(" name=").append(this.mName);
        pw.append(" pkg=").append(this.mServiceWatcherDefault.getBestPackageName());
        pw.append(" version=").append(Integer.toString(this.mServiceWatcherDefault.getBestVersion()));
        pw.append(10);
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
            return 1;
        }
        try {
            return service.getStatus(extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
            return 1;
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcherDefault.getBestPackageName(), e2);
            return 1;
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
            return false;
        }
        try {
            return service.sendExtraCommand(command, extras);
        } catch (RemoteException e) {
            Log.w(TAG, e);
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "Exception from " + this.mServiceWatcherDefault.getBestPackageName(), e2);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean isWorkSourceContain(WorkSource workSource, String packageName) {
        if (workSource == null) {
            return false;
        }
        int size = workSource.size();
        boolean isTest = HwMultiNlpPolicy.isHwLocationDebug(this.mContext);
        for (int i = 0; i < size; i++) {
            if (packageName.equals(workSource.getName(i)) || (isTest && GLOBAL_NLP_DEBUG_PKG.equals(workSource.getName(i)))) {
                return true;
            }
        }
        return false;
    }

    public void handleServiceAb(boolean shouldStart) {
        if (shouldStart) {
            this.mServiceWatcherAb.start();
            return;
        }
        setRequest(this.mRequestOff, this.mWorksourceOff, 2);
        mServiceWatcherUtils.unbindLocked(this.mServiceWatcherAb);
    }
}
