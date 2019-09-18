package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public String mBoundPackageName;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IBinder mBoundService;
    @GuardedBy("mLock")
    private int mBoundUserId = -10000;
    @GuardedBy("mLock")
    private int mBoundVersion = Integer.MIN_VALUE;
    private final Context mContext;
    @GuardedBy("mLock")
    private int mCurrentUserId = 0;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final Runnable mNewServiceWork;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageUpdateFinished(String packageName, int uid) {
            synchronized (ServiceWatcher.this.mLock) {
                boolean unused = ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
        }

        public void onPackageAdded(String packageName, int uid) {
            synchronized (ServiceWatcher.this.mLock) {
                boolean unused = ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
        }

        public void onPackageRemoved(String packageName, int uid) {
            synchronized (ServiceWatcher.this.mLock) {
                boolean unused = ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (ServiceWatcher.this.mLock) {
                boolean unused = ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
            return ServiceWatcher.super.onPackageChanged(packageName, uid, components);
        }
    };
    private final PackageManager mPm;
    /* access modifiers changed from: private */
    public int mRetryCount;
    Runnable mRetryRunnable = new Runnable() {
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x006e, code lost:
            return;
         */
        public void run() {
            synchronized (ServiceWatcher.this.mLock) {
                if (ServiceWatcher.this.mBoundService == null) {
                    if (ServiceWatcher.this.mRetryCount < 3) {
                        String access$300 = ServiceWatcher.this.mTag;
                        Log.w(access$300, "rebind count:" + ServiceWatcher.this.mRetryCount);
                        int unused = ServiceWatcher.this.mRetryCount = ServiceWatcher.this.mRetryCount + 1;
                        ServiceWatcher.this.unbindLocked();
                        boolean unused2 = ServiceWatcher.this.bindBestPackageLocked(ServiceWatcher.this.mServicePackageName, false);
                        ServiceWatcher.this.mHandler.postDelayed(ServiceWatcher.this.mRetryRunnable, ServiceWatcher.RETRY_TIME_INTERVAL);
                    } else {
                        Log.e(ServiceWatcher.this.mTag, "max rebind failed");
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final String mServicePackageName;
    private final List<HashSet<Signature>> mSignatureSets;
    /* access modifiers changed from: private */
    public final String mTag;

    public interface BinderRunner {
        void run(IBinder iBinder);
    }

    public static ArrayList<HashSet<Signature>> getSignatureSets(Context context, List<String> initialPackageNames) {
        PackageManager pm = context.getPackageManager();
        ArrayList<HashSet<Signature>> sigSets = new ArrayList<>();
        int size = initialPackageNames.size();
        for (int i = 0; i < size; i++) {
            String pkg = initialPackageNames.get(i);
            try {
                HashSet<Signature> set = new HashSet<>();
                set.addAll(Arrays.asList(pm.getPackageInfo(pkg, 1048640).signatures));
                sigSets.add(set);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("ServiceWatcher", pkg + " not found");
            }
        }
        return sigSets;
    }

    public ServiceWatcher(Context context, String logTag, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Runnable newServiceWork, Handler handler) {
        this.mContext = context;
        this.mTag = logTag;
        this.mAction = action;
        this.mPm = this.mContext.getPackageManager();
        this.mNewServiceWork = newServiceWork;
        this.mHandler = handler;
        Resources resources = context.getResources();
        boolean enableOverlay = resources.getBoolean(overlaySwitchResId);
        ArrayList<String> initialPackageNames = new ArrayList<>();
        if (enableOverlay) {
            String[] pkgs = resources.getStringArray(initialPackageNamesResId);
            if (pkgs != null) {
                initialPackageNames.addAll(Arrays.asList(pkgs));
            }
            this.mServicePackageName = null;
            String str = this.mTag;
            Log.i(str, "Overlay enabled, packages=" + Arrays.toString(pkgs));
        } else {
            String servicePackageName = resources.getString(defaultServicePackageNameResId);
            if (servicePackageName != null) {
                initialPackageNames.add(servicePackageName);
            }
            this.mServicePackageName = servicePackageName;
            String str2 = this.mTag;
            Log.i(str2, "Overlay disabled, default package=" + servicePackageName);
        }
        this.mSignatureSets = getSignatureSets(context, initialPackageNames);
    }

    public boolean start() {
        if (isServiceMissing()) {
            return false;
        }
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

    private boolean isServiceMissing() {
        return this.mPm.queryIntentServicesAsUser(new Intent(this.mAction), 786432, this.mCurrentUserId).isEmpty();
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean bindBestPackageLocked(String justCheckThisPackage, boolean forceRebind) {
        String str;
        String str2;
        String str3 = justCheckThisPackage;
        Intent intent = new Intent(this.mAction);
        if (str3 != null) {
            intent.setPackage(str3);
        }
        List<ResolveInfo> rInfos = this.mPm.queryIntentServicesAsUser(intent, 268435584, this.mCurrentUserId);
        int version = Integer.MIN_VALUE;
        ComponentName bestComponent = null;
        boolean bestIsMultiuser = false;
        if (rInfos != null) {
            Iterator<ResolveInfo> it = rInfos.iterator();
            boolean bestIsMultiuser2 = false;
            ComponentName bestComponent2 = null;
            int bestVersion = Integer.MIN_VALUE;
            while (true) {
                if (it.hasNext() == 0) {
                    version = bestVersion;
                    bestComponent = bestComponent2;
                    bestIsMultiuser = bestIsMultiuser2;
                    break;
                }
                ResolveInfo rInfo = it.next();
                ComponentName component = rInfo.serviceInfo.getComponentName();
                String packageName = component.getPackageName();
                try {
                    if (!isSignatureMatch(this.mPm.getPackageInfo(packageName, 268435520).signatures)) {
                        Log.w(this.mTag, packageName + " resolves service " + this.mAction + ", but has wrong signature, ignoring");
                    } else {
                        version = Integer.MIN_VALUE;
                        boolean isMultiuser = false;
                        if (rInfo.serviceInfo.metaData != null) {
                            version = rInfo.serviceInfo.metaData.getInt(EXTRA_SERVICE_VERSION, Integer.MIN_VALUE);
                            isMultiuser = rInfo.serviceInfo.metaData.getBoolean(EXTRA_SERVICE_IS_MULTIUSER);
                        }
                        if (HwServiceFactory.getHwNLPManager().skipForeignNlpPackage(this.mAction, packageName)) {
                            continue;
                        } else if (HwServiceFactory.getHwNLPManager().useCivilNlpPackage(this.mAction, packageName)) {
                            Log.d(this.mTag, packageName + " useCivilNlpPackage");
                            int bestVersion2 = version;
                            bestComponent = component;
                            bestIsMultiuser = isMultiuser;
                            break;
                        } else if (version > bestVersion) {
                            bestVersion = version;
                            bestComponent2 = component;
                            bestIsMultiuser2 = isMultiuser;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.wtf(this.mTag, e);
                }
            }
            String str4 = this.mTag;
            Object[] objArr = new Object[4];
            objArr[0] = this.mAction;
            if (str3 == null) {
                str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            } else {
                str = "(" + str3 + ") ";
            }
            objArr[1] = str;
            objArr[2] = Integer.valueOf(rInfos.size());
            if (bestComponent == null) {
                str2 = "no new best component";
            } else {
                str2 = "new best component: " + bestComponent;
            }
            objArr[3] = str2;
            Log.i(str4, String.format("bindBestPackage for %s : %s found %d, %s", objArr));
        } else {
            Log.i(this.mTag, "Unable to query intent services for action: " + this.mAction);
        }
        if (bestComponent == null) {
            Slog.w(this.mTag, "Odd, no component found for service " + this.mAction);
            unbindLocked();
            return false;
        }
        boolean z = false;
        int userId = bestIsMultiuser ? 0 : this.mCurrentUserId;
        if (Objects.equals(bestComponent, this.mBoundComponent) && version == this.mBoundVersion && userId == this.mBoundUserId) {
            z = true;
        }
        boolean alreadyBound = z;
        if (forceRebind || !alreadyBound) {
            unbindLocked();
            bindToPackageLocked(bestComponent, version, userId);
        }
        return true;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void unbindLocked() {
        ComponentName component = this.mBoundComponent;
        this.mBoundComponent = null;
        this.mBoundPackageName = null;
        this.mBoundVersion = Integer.MIN_VALUE;
        this.mBoundUserId = -10000;
        if (component != null) {
            String str = this.mTag;
            Log.i(str, "unbinding " + component);
            this.mBoundService = null;
            this.mContext.unbindService(this);
        }
    }

    @GuardedBy("mLock")
    private void bindToPackageLocked(ComponentName component, int version, int userId) {
        Intent intent = new Intent(this.mAction);
        intent.setComponent(component);
        this.mBoundComponent = component;
        this.mBoundPackageName = component.getPackageName();
        this.mBoundVersion = version;
        this.mBoundUserId = userId;
        String str = this.mTag;
        Log.i(str, "binding " + component + " (v" + version + ") (u" + userId + ")");
        this.mContext.bindServiceAsUser(intent, this, 1073741829, new UserHandle(userId));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0067, code lost:
        return;
     */
    public void bindToPackageWithLock(String packageName, int version, boolean isMultiuser) {
        if (packageName != null) {
            synchronized (this.mLock) {
                List<ResolveInfo> rInfos = this.mPm.queryIntentServicesAsUser(new Intent(this.mAction), 268435584, this.mCurrentUserId);
                if (rInfos != null) {
                    for (ResolveInfo rInfo : rInfos) {
                        ComponentName component = rInfo.serviceInfo.getComponentName();
                        if (packageName.equals(component.getPackageName())) {
                            if (rInfo.serviceInfo.metaData != null) {
                                version = rInfo.serviceInfo.metaData.getInt(EXTRA_SERVICE_VERSION, Integer.MIN_VALUE);
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
        HashSet<Signature> inputSet = new HashSet<>();
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
                String str = this.mTag;
                Log.i(str, component + " connected");
                this.mBoundService = binder;
                if (!(this.mHandler == null || this.mNewServiceWork == null)) {
                    this.mHandler.post(this.mNewServiceWork);
                }
            } else {
                String str2 = this.mTag;
                Log.w(str2, "unexpected onServiceConnected: " + component);
            }
        }
    }

    public void onServiceDisconnected(ComponentName component) {
        synchronized (this.mLock) {
            String str = this.mTag;
            Log.i(str, component + " disconnected");
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

    public boolean runOnBinder(BinderRunner runner) {
        synchronized (this.mLock) {
            if (this.mBoundService == null) {
                return false;
            }
            runner.run(this.mBoundService);
            return true;
        }
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
