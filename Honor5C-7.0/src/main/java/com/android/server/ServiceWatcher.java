package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.server.usb.UsbAudioDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ServiceWatcher implements ServiceConnection {
    public static final String EXTRA_SERVICE_IS_MULTIUSER = "serviceIsMultiuser";
    public static final String EXTRA_SERVICE_VERSION = "serviceVersion";
    private static final int MAX_REBIND_NUM = 3;
    private static final long RETRY_TIME_INTERVAL = 5000;
    private final String mAction;
    @GuardedBy("mLock")
    private ComponentName mBoundComponent;
    @GuardedBy("mLock")
    private String mBoundPackageName;
    @GuardedBy("mLock")
    private IBinder mBoundService;
    @GuardedBy("mLock")
    private int mBoundUserId;
    @GuardedBy("mLock")
    private int mBoundVersion;
    private final Context mContext;
    @GuardedBy("mLock")
    private int mCurrentUserId;
    private final Handler mHandler;
    private final Object mLock;
    private final Runnable mNewServiceWork;
    private final PackageMonitor mPackageMonitor;
    private final PackageManager mPm;
    private int mRetryCount;
    Runnable mRetryRunnable;
    private final String mServicePackageName;
    private final List<HashSet<Signature>> mSignatureSets;
    private final String mTag;

    public static ArrayList<HashSet<Signature>> getSignatureSets(Context context, List<String> initialPackageNames) {
        PackageManager pm = context.getPackageManager();
        ArrayList<HashSet<Signature>> sigSets = new ArrayList();
        int size = initialPackageNames.size();
        for (int i = 0; i < size; i++) {
            String pkg = (String) initialPackageNames.get(i);
            try {
                HashSet<Signature> set = new HashSet();
                set.addAll(Arrays.asList(pm.getPackageInfo(pkg, 1048640).signatures));
                sigSets.add(set);
            } catch (NameNotFoundException e) {
                Log.w("ServiceWatcher", pkg + " not found");
            }
        }
        return sigSets;
    }

    public ServiceWatcher(Context context, String logTag, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Runnable newServiceWork, Handler handler) {
        this.mLock = new Object();
        this.mCurrentUserId = 0;
        this.mBoundVersion = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        this.mBoundUserId = -10000;
        this.mRetryRunnable = new Runnable() {
            public void run() {
                synchronized (ServiceWatcher.this.mLock) {
                    if (ServiceWatcher.this.mBoundService != null) {
                        return;
                    }
                    if (ServiceWatcher.this.mRetryCount < ServiceWatcher.MAX_REBIND_NUM) {
                        Log.w(ServiceWatcher.this.mTag, "rebind count:" + ServiceWatcher.this.mRetryCount);
                        ServiceWatcher serviceWatcher = ServiceWatcher.this;
                        serviceWatcher.mRetryCount = serviceWatcher.mRetryCount + 1;
                        ServiceWatcher.this.unbindLocked();
                        ServiceWatcher.this.bindBestPackageLocked(ServiceWatcher.this.mServicePackageName, false);
                        ServiceWatcher.this.mHandler.postDelayed(ServiceWatcher.this.mRetryRunnable, ServiceWatcher.RETRY_TIME_INTERVAL);
                    } else {
                        Log.e(ServiceWatcher.this.mTag, "max rebind failed");
                    }
                }
            }
        };
        this.mPackageMonitor = new PackageMonitor() {
            public void onPackageUpdateFinished(String packageName, int uid) {
                synchronized (ServiceWatcher.this.mLock) {
                    ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
                }
            }

            public void onPackageAdded(String packageName, int uid) {
                synchronized (ServiceWatcher.this.mLock) {
                    ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
                }
            }

            public void onPackageRemoved(String packageName, int uid) {
                synchronized (ServiceWatcher.this.mLock) {
                    ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
                }
            }

            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                synchronized (ServiceWatcher.this.mLock) {
                    ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
                }
                return super.onPackageChanged(packageName, uid, components);
            }
        };
        this.mContext = context;
        this.mTag = logTag;
        this.mAction = action;
        this.mPm = this.mContext.getPackageManager();
        this.mNewServiceWork = newServiceWork;
        this.mHandler = handler;
        Resources resources = context.getResources();
        boolean enableOverlay = resources.getBoolean(overlaySwitchResId);
        ArrayList<String> initialPackageNames = new ArrayList();
        if (enableOverlay) {
            String[] pkgs = resources.getStringArray(initialPackageNamesResId);
            if (pkgs != null) {
                initialPackageNames.addAll(Arrays.asList(pkgs));
            }
            this.mServicePackageName = null;
            Log.i(this.mTag, "Overlay enabled, packages=" + Arrays.toString(pkgs));
        } else {
            String servicePackageName = resources.getString(defaultServicePackageNameResId);
            if (servicePackageName != null) {
                initialPackageNames.add(servicePackageName);
            }
            this.mServicePackageName = servicePackageName;
            Log.i(this.mTag, "Overlay disabled, default package=" + servicePackageName);
        }
        this.mSignatureSets = getSignatureSets(context, initialPackageNames);
    }

    public boolean start() {
        synchronized (this.mLock) {
            bindBestPackageLocked(this.mServicePackageName, false);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    ServiceWatcher.this.switchUser(userId);
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    ServiceWatcher.this.unlockUser(userId);
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
        if (this.mServicePackageName == null) {
            this.mPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        }
        return true;
    }

    private boolean bindBestPackageLocked(String justCheckThisPackage, boolean forceRebind) {
        Intent intent = new Intent(this.mAction);
        if (justCheckThisPackage != null) {
            intent.setPackage(justCheckThisPackage);
        }
        List<ResolveInfo> rInfos = this.mPm.queryIntentServicesAsUser(intent, 268435584, this.mCurrentUserId);
        int bestVersion = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        ComponentName bestComponent = null;
        boolean bestIsMultiuser = false;
        if (rInfos != null) {
            String str;
            for (ResolveInfo rInfo : rInfos) {
                ComponentName component = rInfo.serviceInfo.getComponentName();
                String packageName = component.getPackageName();
                try {
                    if (isSignatureMatch(this.mPm.getPackageInfo(packageName, 268435520).signatures)) {
                        int version = UsbAudioDevice.kAudioDeviceMeta_Alsa;
                        boolean isMultiuser = false;
                        if (rInfo.serviceInfo.metaData != null) {
                            version = rInfo.serviceInfo.metaData.getInt(EXTRA_SERVICE_VERSION, UsbAudioDevice.kAudioDeviceMeta_Alsa);
                            isMultiuser = rInfo.serviceInfo.metaData.getBoolean(EXTRA_SERVICE_IS_MULTIUSER);
                        }
                        if (HwServiceFactory.getHwNLPManager().skipForeignNlpPackage(this.mAction, packageName)) {
                            continue;
                        } else {
                            if (HwServiceFactory.getHwNLPManager().useCivilNlpPackage(this.mAction, packageName)) {
                                Log.d(this.mTag, packageName + " useCivilNlpPackage");
                                bestVersion = version;
                                bestComponent = component;
                                bestIsMultiuser = isMultiuser;
                                break;
                            } else if (version > bestVersion) {
                                bestVersion = version;
                                bestComponent = component;
                                bestIsMultiuser = isMultiuser;
                            }
                        }
                    } else {
                        Log.w(this.mTag, packageName + " resolves service " + this.mAction + ", but has wrong signature, ignoring");
                    }
                } catch (NameNotFoundException e) {
                    Log.wtf(this.mTag, e);
                }
            }
            String str2 = this.mTag;
            String str3 = "bindBestPackage for %s : %s found %d, %s";
            Object[] objArr = new Object[4];
            objArr[0] = this.mAction;
            if (justCheckThisPackage == null) {
                str = "";
            } else {
                str = "(" + justCheckThisPackage + ") ";
            }
            objArr[1] = str;
            objArr[2] = Integer.valueOf(rInfos.size());
            if (bestComponent == null) {
                str = "no new best component";
            } else {
                str = "new best component: " + bestComponent;
            }
            objArr[MAX_REBIND_NUM] = str;
            Log.i(str2, String.format(str3, objArr));
        } else {
            Log.i(this.mTag, "Unable to query intent services for action: " + this.mAction);
        }
        if (bestComponent == null) {
            Slog.w(this.mTag, "Odd, no component found for service " + this.mAction);
            unbindLocked();
            return false;
        }
        boolean alreadyBound;
        int userId = bestIsMultiuser ? 0 : this.mCurrentUserId;
        if (Objects.equals(bestComponent, this.mBoundComponent)) {
            int i = this.mBoundVersion;
            if (bestVersion == r0) {
                alreadyBound = userId == this.mBoundUserId;
                if (forceRebind || !alreadyBound) {
                    unbindLocked();
                    bindToPackageLocked(bestComponent, bestVersion, userId);
                }
                return true;
            }
        }
        alreadyBound = false;
        unbindLocked();
        bindToPackageLocked(bestComponent, bestVersion, userId);
        return true;
    }

    private void unbindLocked() {
        ComponentName component = this.mBoundComponent;
        this.mBoundComponent = null;
        this.mBoundPackageName = null;
        this.mBoundVersion = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        this.mBoundUserId = -10000;
        if (component != null) {
            Log.i(this.mTag, "unbinding " + component);
            this.mContext.unbindService(this);
        }
    }

    private void bindToPackageLocked(ComponentName component, int version, int userId) {
        Intent intent = new Intent(this.mAction);
        intent.setComponent(component);
        this.mBoundComponent = component;
        this.mBoundPackageName = component.getPackageName();
        this.mBoundVersion = version;
        this.mBoundUserId = userId;
        Log.i(this.mTag, "binding " + component + " (v" + version + ") (u" + userId + ")");
        this.mContext.bindServiceAsUser(intent, this, 1073741829, new UserHandle(userId));
    }

    public void bindToPackageWithLock(String packageName, int version, boolean isMultiuser) {
        if (packageName != null) {
            synchronized (this.mLock) {
                List<ResolveInfo> rInfos = this.mPm.queryIntentServicesAsUser(new Intent(this.mAction), 268435584, this.mCurrentUserId);
                if (rInfos != null) {
                    for (ResolveInfo rInfo : rInfos) {
                        ComponentName component = rInfo.serviceInfo.getComponentName();
                        if (packageName.equals(component.getPackageName())) {
                            if (rInfo.serviceInfo.metaData != null) {
                                version = rInfo.serviceInfo.metaData.getInt(EXTRA_SERVICE_VERSION, UsbAudioDevice.kAudioDeviceMeta_Alsa);
                                isMultiuser = rInfo.serviceInfo.metaData.getBoolean(EXTRA_SERVICE_IS_MULTIUSER);
                            }
                            bindToPackageLocked(component, version, isMultiuser ? 0 : this.mCurrentUserId);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static boolean isSignatureMatch(Signature[] signatures, List<HashSet<Signature>> sigSets) {
        if (signatures == null) {
            return false;
        }
        HashSet<Signature> inputSet = new HashSet();
        for (Signature s : signatures) {
            inputSet.add(s);
        }
        for (HashSet<Signature> referenceSet : sigSets) {
            if (referenceSet.equals(inputSet)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSignatureMatch(Signature[] signatures) {
        return isSignatureMatch(signatures, this.mSignatureSets);
    }

    public void onServiceConnected(ComponentName component, IBinder binder) {
        synchronized (this.mLock) {
            if (component.equals(this.mBoundComponent)) {
                Log.i(this.mTag, component + " connected");
                this.mBoundService = binder;
                if (!(this.mHandler == null || this.mNewServiceWork == null)) {
                    this.mHandler.post(this.mNewServiceWork);
                }
            } else {
                Log.w(this.mTag, "unexpected onServiceConnected: " + component);
            }
        }
    }

    public void onServiceDisconnected(ComponentName component) {
        synchronized (this.mLock) {
            Log.i(this.mTag, component + " disconnected");
            if (component.equals(this.mBoundComponent)) {
                this.mBoundService = null;
                if (this.mHandler != null) {
                    this.mRetryCount = 0;
                    Log.i(this.mTag, "delay rebind begin");
                    this.mHandler.postDelayed(this.mRetryRunnable, RETRY_TIME_INTERVAL);
                }
            }
        }
    }

    public String getBestPackageName() {
        String str;
        synchronized (this.mLock) {
            str = this.mBoundPackageName;
        }
        return str;
    }

    public int getBestVersion() {
        int i;
        synchronized (this.mLock) {
            i = this.mBoundVersion;
        }
        return i;
    }

    public IBinder getBinder() {
        IBinder iBinder;
        synchronized (this.mLock) {
            iBinder = this.mBoundService;
        }
        return iBinder;
    }

    public void switchUser(int userId) {
        synchronized (this.mLock) {
            this.mCurrentUserId = userId;
            bindBestPackageLocked(this.mServicePackageName, false);
        }
    }

    public void unlockUser(int userId) {
        synchronized (this.mLock) {
            if (userId == this.mCurrentUserId) {
                bindBestPackageLocked(this.mServicePackageName, false);
            }
        }
    }
}
