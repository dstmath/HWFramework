package android.app;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentReceiver.Stub;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.split.SplitDependencyLoader;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.rms.HwSysResource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayAdjustments;
import com.android.internal.util.ArrayUtils;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoadedApk {
    static final /* synthetic */ boolean -assertionsDisabled = (LoadedApk.class.desiredAssertionStatus() ^ 1);
    static final boolean DEBUG = false;
    static final String TAG = "LoadedApk";
    static AtomicInteger mBroadcastDebugID = new AtomicInteger(0);
    private final ActivityThread mActivityThread;
    private String mAppDir;
    private Application mApplication;
    private ApplicationInfo mApplicationInfo;
    private final ClassLoader mBaseClassLoader;
    private ClassLoader mClassLoader;
    int mClientCount = 0;
    private File mCredentialProtectedDataDirFile;
    private String mDataDir;
    private File mDataDirFile;
    private File mDeviceProtectedDataDirFile;
    private final DisplayAdjustments mDisplayAdjustments = new DisplayAdjustments();
    private final boolean mIncludeCode;
    private String mLibDir;
    private String[] mOverlayDirs;
    final String mPackageName;
    private HwSysResource mReceiverResource;
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mReceivers = new ArrayMap();
    private final boolean mRegisterPackage;
    private String mResDir;
    Resources mResources;
    private final boolean mSecurityViolation;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mServices = new ArrayMap();
    private String[] mSharedLibraries;
    private String[] mSplitAppDirs;
    private SplitDependencyLoaderImpl mSplitLoader;
    private String[] mSplitNames;
    private String[] mSplitResDirs;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mUnboundServices = new ArrayMap();
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mUnregisteredReceivers = new ArrayMap();

    static final class ReceiverDispatcher {
        final Handler mActivityThread;
        final Context mContext;
        boolean mForgotten;
        final Stub mIIntentReceiver;
        final Instrumentation mInstrumentation;
        final IntentReceiverLeaked mLocation;
        final BroadcastReceiver mReceiver;
        final boolean mRegistered;
        RuntimeException mUnregisterLocation;

        final class Args extends PendingResult {
            private Intent mCurIntent;
            int mDebugID;
            private boolean mDispatched;
            private Intent mLastIntent;
            private final boolean mOrdered;
            private Throwable mPreviousRunStacktrace;

            public Args(Intent intent, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, int sendingUser, int debugID) {
                super(resultCode, resultData, resultExtras, ReceiverDispatcher.this.mRegistered ? 1 : 2, ordered, sticky, ReceiverDispatcher.this.mIIntentReceiver.asBinder(), sendingUser, intent.getFlags());
                this.mCurIntent = intent;
                this.mOrdered = ordered;
                this.mDebugID = debugID;
            }

            public final Runnable getRunnable() {
                return new -$Lambda$FilBqgnXJrN9Mgyks1XHeAxzSTk(this);
            }

            /* synthetic */ void lambda$-android_app_LoadedApk$ReceiverDispatcher$Args_57741() {
                BroadcastReceiver receiver = ReceiverDispatcher.this.mReceiver;
                boolean ordered = this.mOrdered;
                IActivityManager mgr = ActivityManager.getService();
                Intent intent = this.mCurIntent;
                if (intent == null) {
                    Log.wtf(LoadedApk.TAG, "Null intent being dispatched, mDispatched=" + this.mDispatched + ": run() previously called at " + Log.getStackTraceString(this.mPreviousRunStacktrace));
                    return;
                }
                this.mCurIntent = null;
                this.mDispatched = true;
                this.mPreviousRunStacktrace = new Throwable("Previous stacktrace");
                if (receiver == null || intent == null || ReceiverDispatcher.this.mForgotten) {
                    if (ReceiverDispatcher.this.mRegistered && ordered) {
                        sendFinished(mgr);
                    }
                    return;
                }
                Trace.traceBegin(64, "broadcastReceiveReg");
                try {
                    ClassLoader cl = ReceiverDispatcher.this.mReceiver.getClass().getClassLoader();
                    intent.setExtrasClassLoader(cl);
                    intent.prepareToEnterProcess();
                    setExtrasClassLoader(cl);
                    receiver.setPendingResult(this);
                    this.mLastIntent = intent;
                    if (Log.HWINFO) {
                        Trace.traceBegin(64, "broadcast[" + intent.getAction() + "](" + this.mDebugID + ") call onReceive[" + receiver.getClass().getName() + "]");
                    }
                    receiver.onReceive(ReceiverDispatcher.this.mContext, intent);
                    if (Log.HWINFO) {
                        Trace.traceEnd(64);
                    }
                } catch (Exception e) {
                    if (ReceiverDispatcher.this.mRegistered && ordered) {
                        sendFinished(mgr);
                    }
                    if (ReceiverDispatcher.this.mInstrumentation == null || (ReceiverDispatcher.this.mInstrumentation.onException(ReceiverDispatcher.this.mReceiver, e) ^ 1) != 0) {
                        Trace.traceEnd(64);
                        throw new RuntimeException("Error receiving broadcast " + intent + " in " + ReceiverDispatcher.this.mReceiver, e);
                    }
                }
                if (receiver.getPendingResult() != null) {
                    finish();
                }
                Trace.traceEnd(64);
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
                try {
                    if (ReceiverDispatcher.this.mReceiver != null) {
                        sb.append(" receiver=");
                        sb.append(ReceiverDispatcher.this.mReceiver.getClass().getName());
                        sb.append(" act=");
                        if (this.mLastIntent != null) {
                            sb.append(this.mLastIntent.getAction());
                        } else {
                            sb.append("null");
                        }
                    }
                } catch (Exception e) {
                    Log.i(LoadedApk.TAG, "Could not get Class Name", e);
                }
                return sb.toString();
            }
        }

        static final class InnerReceiver extends Stub {
            final WeakReference<ReceiverDispatcher> mDispatcher;
            final ReceiverDispatcher mStrongRef;

            InnerReceiver(ReceiverDispatcher rd, boolean strong) {
                this.mDispatcher = new WeakReference(rd);
                if (!strong) {
                    rd = null;
                }
                this.mStrongRef = rd;
            }

            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                ReceiverDispatcher rd;
                if (intent == null) {
                    Log.wtf(LoadedApk.TAG, "Null intent received");
                    rd = null;
                } else {
                    rd = (ReceiverDispatcher) this.mDispatcher.get();
                }
                if (rd != null) {
                    rd.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
                    return;
                }
                IActivityManager mgr = ActivityManager.getService();
                if (extras != null) {
                    try {
                        extras.setAllowFds(false);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
                mgr.finishReceiver(this, resultCode, data, extras, false, intent.getFlags());
            }
        }

        ReceiverDispatcher(BroadcastReceiver receiver, Context context, Handler activityThread, Instrumentation instrumentation, boolean registered) {
            if (activityThread == null) {
                throw new NullPointerException("Handler must not be null");
            }
            this.mIIntentReceiver = new InnerReceiver(this, registered ^ 1);
            this.mReceiver = receiver;
            this.mContext = context;
            this.mActivityThread = activityThread;
            this.mInstrumentation = instrumentation;
            this.mRegistered = registered;
            this.mLocation = new IntentReceiverLeaked(null);
            this.mLocation.fillInStackTrace();
        }

        void validate(Context context, Handler activityThread) {
            if (this.mContext != context) {
                throw new IllegalStateException("Receiver " + this.mReceiver + " registered with differing Context (was " + this.mContext + " now " + context + ")");
            } else if (this.mActivityThread != activityThread) {
                throw new IllegalStateException("Receiver " + this.mReceiver + " registered with differing handler (was " + this.mActivityThread + " now " + activityThread + ")");
            }
        }

        IntentReceiverLeaked getLocation() {
            return this.mLocation;
        }

        BroadcastReceiver getIntentReceiver() {
            return this.mReceiver;
        }

        IIntentReceiver getIIntentReceiver() {
            return this.mIIntentReceiver;
        }

        void setUnregisterLocation(RuntimeException ex) {
            this.mUnregisterLocation = ex;
        }

        RuntimeException getUnregisterLocation() {
            return this.mUnregisterLocation;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
            int debugID = 0;
            if (Log.HWINFO) {
                debugID = LoadedApk.mBroadcastDebugID.incrementAndGet();
            }
            Args args = new Args(intent, resultCode, data, extras, ordered, sticky, sendingUser, debugID);
            if (intent == null) {
                Log.wtf(LoadedApk.TAG, "Null intent received");
            }
            if (Log.HWINFO) {
                Looper looper = this.mActivityThread != null ? this.mActivityThread.getLooper() : null;
                Thread thread = looper != null ? looper.getThread() : null;
                Trace.traceBegin(64, "broadcast[" + (intent != null ? intent.getAction() : null) + "](" + debugID + ") send msg to thread[" + ((thread != null ? thread.getName() : null) + "(" + (thread != null ? Long.valueOf(thread.getId()) : null) + ")") + "], msg queue length[" + (looper != null ? Integer.valueOf(looper.getQueue().getMessageCount()) : null).intValue() + "]");
            }
            if ((intent == null || (this.mActivityThread.post(args.getRunnable()) ^ 1) != 0) && this.mRegistered && ordered) {
                args.sendFinished(ActivityManager.getService());
            }
            if (Log.HWINFO) {
                Trace.traceEnd(64);
            }
        }
    }

    static final class ServiceDispatcher {
        private final ArrayMap<ComponentName, ConnectionInfo> mActiveConnections = new ArrayMap();
        private final Handler mActivityThread;
        private final ServiceConnection mConnection;
        private final Context mContext;
        private final int mFlags;
        private boolean mForgotten;
        private final InnerConnection mIServiceConnection = new InnerConnection(this);
        private final ServiceConnectionLeaked mLocation;
        private RuntimeException mUnbindLocation;

        private static class ConnectionInfo {
            IBinder binder;
            DeathRecipient deathMonitor;

            /* synthetic */ ConnectionInfo(ConnectionInfo -this0) {
                this();
            }

            private ConnectionInfo() {
            }
        }

        private final class DeathMonitor implements DeathRecipient {
            final ComponentName mName;
            final IBinder mService;

            DeathMonitor(ComponentName name, IBinder service) {
                this.mName = name;
                this.mService = service;
            }

            public void binderDied() {
                ServiceDispatcher.this.death(this.mName, this.mService);
            }
        }

        private static class InnerConnection extends IServiceConnection.Stub {
            final WeakReference<ServiceDispatcher> mDispatcher;

            InnerConnection(ServiceDispatcher sd) {
                this.mDispatcher = new WeakReference(sd);
            }

            public void connected(ComponentName name, IBinder service, boolean dead) throws RemoteException {
                ServiceDispatcher sd = (ServiceDispatcher) this.mDispatcher.get();
                if (sd != null) {
                    sd.connected(name, service, dead);
                }
            }
        }

        private final class RunConnection implements Runnable {
            final int mCommand;
            final boolean mDead;
            final ComponentName mName;
            final IBinder mService;

            RunConnection(ComponentName name, IBinder service, int command, boolean dead) {
                this.mName = name;
                this.mService = service;
                this.mCommand = command;
                this.mDead = dead;
            }

            public void run() {
                if (this.mCommand == 0) {
                    ServiceDispatcher.this.doConnected(this.mName, this.mService, this.mDead);
                } else if (this.mCommand == 1) {
                    ServiceDispatcher.this.doDeath(this.mName, this.mService);
                }
            }
        }

        ServiceDispatcher(ServiceConnection conn, Context context, Handler activityThread, int flags) {
            this.mConnection = conn;
            this.mContext = context;
            this.mActivityThread = activityThread;
            this.mLocation = new ServiceConnectionLeaked(null);
            this.mLocation.fillInStackTrace();
            this.mFlags = flags;
        }

        void validate(Context context, Handler activityThread) {
            if (this.mContext != context) {
                throw new RuntimeException("ServiceConnection " + this.mConnection + " registered with differing Context (was " + this.mContext + " now " + context + ")");
            } else if (this.mActivityThread != activityThread) {
                throw new RuntimeException("ServiceConnection " + this.mConnection + " registered with differing handler (was " + this.mActivityThread + " now " + activityThread + ")");
            }
        }

        void doForget() {
            synchronized (this) {
                for (int i = 0; i < this.mActiveConnections.size(); i++) {
                    ConnectionInfo ci = (ConnectionInfo) this.mActiveConnections.valueAt(i);
                    ci.binder.unlinkToDeath(ci.deathMonitor, 0);
                }
                this.mActiveConnections.clear();
                this.mForgotten = true;
            }
        }

        ServiceConnectionLeaked getLocation() {
            return this.mLocation;
        }

        ServiceConnection getServiceConnection() {
            return this.mConnection;
        }

        IServiceConnection getIServiceConnection() {
            return this.mIServiceConnection;
        }

        int getFlags() {
            return this.mFlags;
        }

        void setUnbindLocation(RuntimeException ex) {
            this.mUnbindLocation = ex;
        }

        RuntimeException getUnbindLocation() {
            return this.mUnbindLocation;
        }

        public void connected(ComponentName name, IBinder service, boolean dead) {
            if (name == null || !"com.android.systemui.keyguard.KeyguardService".equals(name.getClassName())) {
                if (this.mActivityThread != null) {
                    this.mActivityThread.post(new RunConnection(name, service, 0, dead));
                } else {
                    doConnected(name, service, dead);
                }
                return;
            }
            doConnected(name, service, dead);
        }

        public void death(ComponentName name, IBinder service) {
            if (this.mActivityThread != null) {
                this.mActivityThread.post(new RunConnection(name, service, 1, false));
            } else {
                doDeath(name, service);
            }
        }

        /* JADX WARNING: Missing block: B:22:0x003e, code:
            if (r2 == null) goto L_0x0045;
     */
        /* JADX WARNING: Missing block: B:23:0x0040, code:
            r6.mConnection.onServiceDisconnected(r7);
     */
        /* JADX WARNING: Missing block: B:24:0x0045, code:
            if (r9 == false) goto L_0x004c;
     */
        /* JADX WARNING: Missing block: B:25:0x0047, code:
            r6.mConnection.onBindingDied(r7);
     */
        /* JADX WARNING: Missing block: B:26:0x004c, code:
            if (r8 == null) goto L_0x0053;
     */
        /* JADX WARNING: Missing block: B:27:0x004e, code:
            r6.mConnection.onServiceConnected(r7, r8);
     */
        /* JADX WARNING: Missing block: B:28:0x0053, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void doConnected(ComponentName name, IBinder service, boolean dead) {
            synchronized (this) {
                if (this.mForgotten) {
                    return;
                }
                ConnectionInfo old = (ConnectionInfo) this.mActiveConnections.get(name);
                if (old == null || old.binder != service) {
                    if (service != null) {
                        ConnectionInfo info = new ConnectionInfo();
                        info.binder = service;
                        info.deathMonitor = new DeathMonitor(name, service);
                        try {
                            service.linkToDeath(info.deathMonitor, 0);
                            this.mActiveConnections.put(name, info);
                        } catch (RemoteException e) {
                            this.mActiveConnections.remove(name);
                            return;
                        }
                    }
                    this.mActiveConnections.remove(name);
                    if (old != null) {
                        old.binder.unlinkToDeath(old.deathMonitor, 0);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:7:0x0010, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void doDeath(ComponentName name, IBinder service) {
            synchronized (this) {
                ConnectionInfo old = (ConnectionInfo) this.mActiveConnections.get(name);
                if (old == null || old.binder != service) {
                } else {
                    this.mActiveConnections.remove(name);
                    old.binder.unlinkToDeath(old.deathMonitor, 0);
                    this.mConnection.onServiceDisconnected(name);
                }
            }
        }
    }

    private class SplitDependencyLoaderImpl extends SplitDependencyLoader<NameNotFoundException> {
        private final ClassLoader[] mCachedClassLoaders;
        private final String[][] mCachedResourcePaths;

        SplitDependencyLoaderImpl(SparseArray<int[]> dependencies) {
            super(dependencies);
            this.mCachedResourcePaths = new String[(LoadedApk.this.mSplitNames.length + 1)][];
            this.mCachedClassLoaders = new ClassLoader[(LoadedApk.this.mSplitNames.length + 1)];
        }

        protected boolean isSplitCached(int splitIdx) {
            return this.mCachedClassLoaders[splitIdx] != null;
        }

        protected void constructSplit(int splitIdx, int[] configSplitIndices, int parentSplitIdx) throws NameNotFoundException {
            int i = 0;
            ArrayList<String> splitPaths = new ArrayList();
            int length;
            if (splitIdx == 0) {
                LoadedApk.this.createOrUpdateClassLoaderLocked(null);
                this.mCachedClassLoaders[0] = LoadedApk.this.mClassLoader;
                for (int configSplitIdx : configSplitIndices) {
                    splitPaths.add(LoadedApk.this.mSplitResDirs[configSplitIdx - 1]);
                }
                this.mCachedResourcePaths[0] = (String[]) splitPaths.toArray(new String[splitPaths.size()]);
                return;
            }
            this.mCachedClassLoaders[splitIdx] = ApplicationLoaders.getDefault().getClassLoader(LoadedApk.this.mSplitAppDirs[splitIdx - 1], LoadedApk.this.getTargetSdkVersion(), false, null, null, this.mCachedClassLoaders[parentSplitIdx]);
            Collections.addAll(splitPaths, this.mCachedResourcePaths[parentSplitIdx]);
            splitPaths.add(LoadedApk.this.mSplitResDirs[splitIdx - 1]);
            length = configSplitIndices.length;
            while (i < length) {
                splitPaths.add(LoadedApk.this.mSplitResDirs[configSplitIndices[i] - 1]);
                i++;
            }
            this.mCachedResourcePaths[splitIdx] = (String[]) splitPaths.toArray(new String[splitPaths.size()]);
        }

        private int ensureSplitLoaded(String splitName) throws NameNotFoundException {
            int idx = 0;
            if (splitName != null) {
                idx = Arrays.binarySearch(LoadedApk.this.mSplitNames, splitName);
                if (idx < 0) {
                    throw new NameNotFoundException("Split name '" + splitName + "' is not installed");
                }
                idx++;
            }
            loadDependenciesForSplit(idx);
            return idx;
        }

        ClassLoader getClassLoaderForSplit(String splitName) throws NameNotFoundException {
            return this.mCachedClassLoaders[ensureSplitLoaded(splitName)];
        }

        String[] getSplitPathsForSplit(String splitName) throws NameNotFoundException {
            return this.mCachedResourcePaths[ensureSplitLoaded(splitName)];
        }
    }

    private static class WarningContextClassLoader extends ClassLoader {
        private static boolean warned = false;

        /* synthetic */ WarningContextClassLoader(WarningContextClassLoader -this0) {
            this();
        }

        private WarningContextClassLoader() {
        }

        private void warn(String methodName) {
            if (!warned) {
                warned = true;
                Thread.currentThread().setContextClassLoader(getParent());
                Slog.w(ActivityThread.TAG, "ClassLoader." + methodName + ": " + "The class loader returned by " + "Thread.getContextClassLoader() may fail for processes " + "that host multiple applications. You should explicitly " + "specify a context class loader. For example: " + "Thread.setContextClassLoader(getClass().getClassLoader());");
            }
        }

        public URL getResource(String resName) {
            warn("getResource");
            return getParent().getResource(resName);
        }

        public Enumeration<URL> getResources(String resName) throws IOException {
            warn("getResources");
            return getParent().getResources(resName);
        }

        public InputStream getResourceAsStream(String resName) {
            warn("getResourceAsStream");
            return getParent().getResourceAsStream(resName);
        }

        public Class<?> loadClass(String className) throws ClassNotFoundException {
            warn("loadClass");
            return getParent().loadClass(className);
        }

        public void setClassAssertionStatus(String cname, boolean enable) {
            warn("setClassAssertionStatus");
            getParent().setClassAssertionStatus(cname, enable);
        }

        public void setPackageAssertionStatus(String pname, boolean enable) {
            warn("setPackageAssertionStatus");
            getParent().setPackageAssertionStatus(pname, enable);
        }

        public void setDefaultAssertionStatus(boolean enable) {
            warn("setDefaultAssertionStatus");
            getParent().setDefaultAssertionStatus(enable);
        }

        public void clearAssertionStatus() {
            warn("clearAssertionStatus");
            getParent().clearAssertionStatus();
        }
    }

    Application getApplication() {
        return this.mApplication;
    }

    public LoadedApk(ActivityThread activityThread, ApplicationInfo aInfo, CompatibilityInfo compatInfo, ClassLoader baseLoader, boolean securityViolation, boolean includeCode, boolean registerPackage) {
        this.mActivityThread = activityThread;
        setApplicationInfo(aInfo);
        this.mPackageName = aInfo.packageName;
        this.mBaseClassLoader = baseLoader;
        this.mSecurityViolation = securityViolation;
        this.mIncludeCode = includeCode;
        this.mRegisterPackage = registerPackage;
        this.mDisplayAdjustments.setCompatibilityInfo(compatInfo);
    }

    private static ApplicationInfo adjustNativeLibraryPaths(ApplicationInfo info) {
        if (!(info.primaryCpuAbi == null || info.secondaryCpuAbi == null)) {
            String runtimeIsa = VMRuntime.getRuntime().vmInstructionSet();
            String secondaryIsa = VMRuntime.getInstructionSet(info.secondaryCpuAbi);
            String secondaryDexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + secondaryIsa);
            if (!secondaryDexCodeIsa.isEmpty()) {
                secondaryIsa = secondaryDexCodeIsa;
            }
            if (runtimeIsa.equals(secondaryIsa)) {
                ApplicationInfo modified = new ApplicationInfo(info);
                modified.nativeLibraryDir = modified.secondaryNativeLibraryDir;
                modified.primaryCpuAbi = modified.secondaryCpuAbi;
                return modified;
            }
        }
        return info;
    }

    LoadedApk(ActivityThread activityThread) {
        this.mActivityThread = activityThread;
        this.mApplicationInfo = new ApplicationInfo();
        this.mApplicationInfo.packageName = "android";
        this.mPackageName = "android";
        this.mAppDir = null;
        this.mResDir = null;
        this.mSplitAppDirs = null;
        this.mSplitResDirs = null;
        this.mOverlayDirs = null;
        this.mSharedLibraries = null;
        this.mDataDir = null;
        this.mDataDirFile = null;
        this.mDeviceProtectedDataDirFile = null;
        this.mCredentialProtectedDataDirFile = null;
        this.mLibDir = null;
        this.mBaseClassLoader = null;
        this.mSecurityViolation = false;
        this.mIncludeCode = true;
        this.mRegisterPackage = false;
        this.mClassLoader = ClassLoader.getSystemClassLoader();
        this.mResources = Resources.getSystem();
    }

    void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        if (-assertionsDisabled || info.packageName.equals("android")) {
            this.mApplicationInfo = info;
            this.mClassLoader = classLoader;
            return;
        }
        throw new AssertionError();
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public ApplicationInfo getApplicationInfo() {
        return this.mApplicationInfo;
    }

    public int getTargetSdkVersion() {
        return this.mApplicationInfo.targetSdkVersion;
    }

    public boolean isSecurityViolation() {
        return this.mSecurityViolation;
    }

    public CompatibilityInfo getCompatibilityInfo() {
        return this.mDisplayAdjustments.getCompatibilityInfo();
    }

    public void setCompatibilityInfo(CompatibilityInfo compatInfo) {
        this.mDisplayAdjustments.setCompatibilityInfo(compatInfo);
    }

    private static String[] getLibrariesFor(String packageName) {
        try {
            ApplicationInfo ai = ActivityThread.getPackageManager().getApplicationInfo(packageName, 1024, UserHandle.myUserId());
            if (ai == null) {
                return null;
            }
            return ai.sharedLibraryFiles;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateApplicationInfo(ApplicationInfo aInfo, List<String> oldPaths) {
        setApplicationInfo(aInfo);
        List<String> newPaths = new ArrayList();
        makePaths(this.mActivityThread, aInfo, newPaths);
        List<String> addedPaths = new ArrayList(newPaths.size());
        if (oldPaths != null) {
            for (String path : newPaths) {
                String apkName = path.substring(path.lastIndexOf(File.separator));
                boolean match = false;
                for (String oldPath : oldPaths) {
                    if (apkName.equals(oldPath.substring(oldPath.lastIndexOf(File.separator)))) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    addedPaths.add(path);
                }
            }
        } else {
            addedPaths.addAll(newPaths);
        }
        synchronized (this) {
            createOrUpdateClassLoaderLocked(addedPaths);
            if (this.mResources != null) {
                try {
                    String[] splitPaths = getSplitPaths(null);
                    if (HwPCUtils.enabled() && ActivityThread.currentActivityThread() != null && HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().mDisplayId)) {
                        this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, ActivityThread.currentActivityThread().mDisplayId, null, getCompatibilityInfo(), getClassLoader());
                    } else {
                        this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, null, getCompatibilityInfo(), getClassLoader());
                    }
                } catch (NameNotFoundException e) {
                    throw new AssertionError("null split not found");
                }
            }
        }
    }

    private void setApplicationInfo(ApplicationInfo aInfo) {
        int myUid = Process.myUid();
        aInfo = adjustNativeLibraryPaths(aInfo);
        this.mApplicationInfo = aInfo;
        this.mAppDir = aInfo.sourceDir;
        this.mResDir = aInfo.uid == myUid ? aInfo.sourceDir : aInfo.publicSourceDir;
        this.mOverlayDirs = aInfo.resourceDirs;
        this.mSharedLibraries = aInfo.sharedLibraryFiles;
        this.mDataDir = aInfo.dataDir;
        this.mLibDir = aInfo.nativeLibraryDir;
        this.mDataDirFile = FileUtils.newFileOrNull(aInfo.dataDir);
        this.mDeviceProtectedDataDirFile = FileUtils.newFileOrNull(aInfo.deviceProtectedDataDir);
        this.mCredentialProtectedDataDirFile = FileUtils.newFileOrNull(aInfo.credentialProtectedDataDir);
        this.mSplitNames = aInfo.splitNames;
        this.mSplitAppDirs = aInfo.splitSourceDirs;
        this.mSplitResDirs = aInfo.uid == myUid ? aInfo.splitSourceDirs : aInfo.splitPublicSourceDirs;
        if (aInfo.requestsIsolatedSplitLoading() && (ArrayUtils.isEmpty(this.mSplitNames) ^ 1) != 0) {
            this.mSplitLoader = new SplitDependencyLoaderImpl(aInfo.splitDependencies);
        }
    }

    public static void makePaths(ActivityThread activityThread, ApplicationInfo aInfo, List<String> outZipPaths) {
        makePaths(activityThread, false, aInfo, outZipPaths, null);
    }

    public static void makePaths(ActivityThread activityThread, boolean isBundledApp, ApplicationInfo aInfo, List<String> outZipPaths, List<String> outLibPaths) {
        String appDir = aInfo.sourceDir;
        String libDir = aInfo.nativeLibraryDir;
        String[] sharedLibraries = aInfo.sharedLibraryFiles;
        outZipPaths.clear();
        outZipPaths.add(appDir);
        if (!(aInfo.splitSourceDirs == null || (aInfo.requestsIsolatedSplitLoading() ^ 1) == 0)) {
            Collections.addAll(outZipPaths, aInfo.splitSourceDirs);
        }
        if (outLibPaths != null) {
            outLibPaths.clear();
        }
        String[] instrumentationLibs = null;
        if (activityThread != null) {
            String instrumentationPackageName = activityThread.mInstrumentationPackageName;
            String instrumentationAppDir = activityThread.mInstrumentationAppDir;
            String[] instrumentationSplitAppDirs = activityThread.mInstrumentationSplitAppDirs;
            String instrumentationLibDir = activityThread.mInstrumentationLibDir;
            String instrumentedAppDir = activityThread.mInstrumentedAppDir;
            String[] instrumentedSplitAppDirs = activityThread.mInstrumentedSplitAppDirs;
            String instrumentedLibDir = activityThread.mInstrumentedLibDir;
            if (appDir.equals(instrumentationAppDir) || appDir.equals(instrumentedAppDir)) {
                outZipPaths.clear();
                outZipPaths.add(instrumentationAppDir);
                if (!aInfo.requestsIsolatedSplitLoading()) {
                    if (instrumentationSplitAppDirs != null) {
                        Collections.addAll(outZipPaths, instrumentationSplitAppDirs);
                    }
                    if (!instrumentationAppDir.equals(instrumentedAppDir)) {
                        outZipPaths.add(instrumentedAppDir);
                        if (instrumentedSplitAppDirs != null) {
                            Collections.addAll(outZipPaths, instrumentedSplitAppDirs);
                        }
                    }
                }
                if (outLibPaths != null) {
                    outLibPaths.add(instrumentationLibDir);
                    if (!instrumentationLibDir.equals(instrumentedLibDir)) {
                        outLibPaths.add(instrumentedLibDir);
                    }
                }
                if (!instrumentedAppDir.equals(instrumentationAppDir)) {
                    instrumentationLibs = getLibrariesFor(instrumentationPackageName);
                }
            }
        }
        if (outLibPaths != null) {
            if (outLibPaths.isEmpty()) {
                outLibPaths.add(libDir);
            }
            if (aInfo.primaryCpuAbi != null) {
                if (aInfo.targetSdkVersion < 24) {
                    outLibPaths.add("/system/fake-libs" + (VMRuntime.is64BitAbi(aInfo.primaryCpuAbi) ? "64" : ProxyInfo.LOCAL_EXCL_LIST));
                }
                for (String apk : outZipPaths) {
                    outLibPaths.add(apk + "!/lib/" + aInfo.primaryCpuAbi);
                }
            }
            if (isBundledApp) {
                outLibPaths.add(System.getProperty("java.library.path"));
            }
        }
        if (sharedLibraries != null) {
            for (String lib : sharedLibraries) {
                if (!outZipPaths.contains(lib)) {
                    outZipPaths.add(0, lib);
                    appendApkLibPathIfNeeded(lib, aInfo, outLibPaths);
                }
            }
        }
        if (instrumentationLibs != null) {
            for (String lib2 : instrumentationLibs) {
                if (!outZipPaths.contains(lib2)) {
                    outZipPaths.add(0, lib2);
                    appendApkLibPathIfNeeded(lib2, aInfo, outLibPaths);
                }
            }
        }
    }

    private static void appendApkLibPathIfNeeded(String path, ApplicationInfo applicationInfo, List<String> outLibPaths) {
        if (outLibPaths != null && applicationInfo.primaryCpuAbi != null && path.endsWith(".apk") && applicationInfo.targetSdkVersion >= 26) {
            outLibPaths.add(path + "!/lib/" + applicationInfo.primaryCpuAbi);
        }
    }

    ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException {
        if (this.mSplitLoader == null) {
            return this.mClassLoader;
        }
        return this.mSplitLoader.getClassLoaderForSplit(splitName);
    }

    String[] getSplitPaths(String splitName) throws NameNotFoundException {
        if (this.mSplitLoader == null) {
            return this.mSplitResDirs;
        }
        return this.mSplitLoader.getSplitPathsForSplit(splitName);
    }

    private void createOrUpdateClassLoaderLocked(List<String> addedPaths) {
        if (!this.mPackageName.equals("android")) {
            if (!Objects.equals(this.mPackageName, ActivityThread.currentPackageName()) && this.mIncludeCode) {
                try {
                    ActivityThread.getPackageManager().notifyPackageUse(this.mPackageName, 6);
                } catch (RemoteException re) {
                    throw re.rethrowFromSystemServer();
                }
            }
            if (this.mRegisterPackage) {
                try {
                    ActivityManager.getService().addPackageDependency(this.mPackageName);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            List<String> zipPaths = new ArrayList(10);
            List<String> libPaths = new ArrayList(10);
            boolean isBundledApp = (!this.mApplicationInfo.isSystemApp() || (this.mApplicationInfo.isUpdatedSystemApp() ^ 1) == 0) ? (this.mApplicationInfo.hwFlags & 536870912) != 0 : true;
            makePaths(this.mActivityThread, isBundledApp, this.mApplicationInfo, zipPaths, libPaths);
            String libraryPermittedPath = this.mDataDir;
            if (isBundledApp) {
                libraryPermittedPath = libraryPermittedPath + File.pathSeparator + System.getProperty("java.library.path");
            }
            String librarySearchPath = TextUtils.join(File.pathSeparator, libPaths);
            ThreadPolicy oldPolicy;
            if (this.mIncludeCode) {
                String zip;
                if (zipPaths.size() == 1) {
                    zip = (String) zipPaths.get(0);
                } else {
                    zip = TextUtils.join(File.pathSeparator, zipPaths);
                }
                boolean needToSetupJitProfiles = false;
                if (this.mClassLoader == null) {
                    oldPolicy = StrictMode.allowThreadDiskReads();
                    this.mClassLoader = ApplicationLoaders.getDefault().getClassLoader(zip, this.mApplicationInfo.targetSdkVersion, isBundledApp, librarySearchPath, libraryPermittedPath, this.mBaseClassLoader);
                    StrictMode.setThreadPolicy(oldPolicy);
                    needToSetupJitProfiles = true;
                }
                if (addedPaths != null && addedPaths.size() > 0) {
                    ApplicationLoaders.getDefault().addPath(this.mClassLoader, TextUtils.join(File.pathSeparator, addedPaths));
                    needToSetupJitProfiles = true;
                }
                if (needToSetupJitProfiles && (ActivityThread.isSystem() ^ 1) != 0) {
                    setupJitProfileSupport();
                }
                return;
            }
            if (this.mClassLoader == null) {
                oldPolicy = StrictMode.allowThreadDiskReads();
                this.mClassLoader = ApplicationLoaders.getDefault().getClassLoader(ProxyInfo.LOCAL_EXCL_LIST, this.mApplicationInfo.targetSdkVersion, isBundledApp, librarySearchPath, libraryPermittedPath, this.mBaseClassLoader);
                StrictMode.setThreadPolicy(oldPolicy);
            }
        } else if (this.mClassLoader == null) {
            if (this.mBaseClassLoader != null) {
                this.mClassLoader = this.mBaseClassLoader;
            } else {
                this.mClassLoader = ClassLoader.getSystemClassLoader();
            }
        }
    }

    public ClassLoader getClassLoader() {
        ClassLoader classLoader;
        synchronized (this) {
            if (this.mClassLoader == null) {
                createOrUpdateClassLoaderLocked(null);
            }
            classLoader = this.mClassLoader;
        }
        return classLoader;
    }

    private static File getPrimaryProfileFile(String packageName) {
        return new File(Environment.getDataProfilesDePackageDirectory(UserHandle.myUserId(), packageName), "primary.prof");
    }

    private void setupJitProfileSupport() {
        if (SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false) && this.mApplicationInfo.uid == Process.myUid()) {
            List<String> codePaths = new ArrayList();
            if ((this.mApplicationInfo.flags & 4) != 0) {
                codePaths.add(this.mApplicationInfo.sourceDir);
            }
            if (this.mApplicationInfo.splitSourceDirs != null) {
                Collections.addAll(codePaths, this.mApplicationInfo.splitSourceDirs);
            }
            if (!codePaths.isEmpty()) {
                VMRuntime.registerAppInfo(getPrimaryProfileFile(this.mPackageName).getPath(), (String[]) codePaths.toArray(new String[codePaths.size()]));
                DexLoadReporter.getInstance().registerAppDataDir(this.mPackageName, this.mDataDir);
            }
        }
    }

    private void initializeJavaContextClassLoader() {
        try {
            PackageInfo pi = ActivityThread.getPackageManager().getPackageInfo(this.mPackageName, 268435456, UserHandle.myUserId());
            if (pi == null) {
                Slog.w(TAG, "Unable to get package info for " + this.mPackageName + "; is package not installed?");
                return;
            }
            boolean processNameNotDefault;
            ClassLoader contextClassLoader;
            boolean sharedUserIdSet = pi.sharedUserId != null;
            if (pi.applicationInfo != null) {
                processNameNotDefault = this.mPackageName.equals(pi.applicationInfo.processName) ^ 1;
            } else {
                processNameNotDefault = false;
            }
            if (!sharedUserIdSet ? processNameNotDefault : true) {
                contextClassLoader = new WarningContextClassLoader();
            } else {
                contextClassLoader = this.mClassLoader;
            }
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getAppDir() {
        return this.mAppDir;
    }

    public String getLibDir() {
        return this.mLibDir;
    }

    public String getResDir() {
        return this.mResDir;
    }

    public String[] getSplitAppDirs() {
        return this.mSplitAppDirs;
    }

    public String[] getSplitResDirs() {
        return this.mSplitResDirs;
    }

    public String[] getOverlayDirs() {
        return this.mOverlayDirs;
    }

    public String getDataDir() {
        return this.mDataDir;
    }

    public File getDataDirFile() {
        return this.mDataDirFile;
    }

    public File getDeviceProtectedDataDirFile() {
        return this.mDeviceProtectedDataDirFile;
    }

    public File getCredentialProtectedDataDirFile() {
        return this.mCredentialProtectedDataDirFile;
    }

    public AssetManager getAssets() {
        Resources resources = getResources();
        if (resources != null) {
            return resources.getAssets();
        }
        return null;
    }

    public Resources getResources() {
        if (this.mResources == null) {
            try {
                String[] splitPaths = getSplitPaths(null);
                if (HwPCUtils.enabled() && ActivityThread.currentActivityThread() != null && HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().mDisplayId)) {
                    this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, ActivityThread.currentActivityThread().mDisplayId, null, getCompatibilityInfo(), getClassLoader());
                } else {
                    this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, null, getCompatibilityInfo(), getClassLoader());
                }
            } catch (NameNotFoundException e) {
                throw new AssertionError("null split not found");
            }
        }
        if (this.mResources != null) {
            this.mResources.getImpl().getHwResourcesImpl().setPackageName(getPackageName());
            this.mResources.setPackageName(getPackageName());
        }
        return this.mResources;
    }

    public Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation) {
        if (this.mApplication != null) {
            return this.mApplication;
        }
        Trace.traceBegin(64, "makeApplication");
        Application app = null;
        String appClass = this.mApplicationInfo.className;
        if (forceDefaultAppClass || appClass == null) {
            appClass = "android.app.Application";
        }
        try {
            ClassLoader cl = getClassLoader();
            if (!this.mPackageName.equals("android")) {
                Trace.traceBegin(64, "initializeJavaContextClassLoader");
                initializeJavaContextClassLoader();
                Trace.traceEnd(64);
            }
            ContextImpl appContext = ContextImpl.createAppContext(this.mActivityThread, this);
            app = this.mActivityThread.mInstrumentation.newApplication(cl, appClass, appContext);
            appContext.setOuterContext(app);
        } catch (Exception e) {
            if (!this.mActivityThread.mInstrumentation.onException(null, e)) {
                Trace.traceEnd(64);
                throw new RuntimeException("Unable to instantiate application " + appClass + ": " + e.toString(), e);
            }
        }
        this.mActivityThread.mAllApplications.add(app);
        this.mApplication = app;
        if (this.mReceiverResource == null) {
            this.mReceiverResource = HwFrameworkFactory.getHwResource(12);
        }
        if (instrumentation != null) {
            try {
                instrumentation.callApplicationOnCreate(app);
            } catch (Exception e2) {
                if (!instrumentation.onException(app, e2)) {
                    Trace.traceEnd(64);
                    throw new RuntimeException("Unable to create application " + app.getClass().getName() + ": " + e2.toString(), e2);
                }
            }
        }
        AssetManager assetManager = getAssets();
        if (assetManager != null) {
            SparseArray<String> packageIdentifiers = assetManager.getAssignedPackageIdentifiers();
            int N = packageIdentifiers.size();
            for (int i = 0; i < N; i++) {
                int id = packageIdentifiers.keyAt(i);
                if (!(id == 1 || id == 127)) {
                    rewriteRValues(getClassLoader(), (String) packageIdentifiers.valueAt(i), id);
                }
            }
        }
        Trace.traceEnd(64);
        return app;
    }

    private void rewriteRValues(ClassLoader cl, String packageName, int id) {
        Throwable cause;
        try {
            try {
                Method callback = cl.loadClass(packageName + ".R").getMethod("onResourcesLoaded", new Class[]{Integer.TYPE});
                try {
                    callback.invoke(null, new Object[]{Integer.valueOf(id)});
                } catch (Throwable e) {
                    cause = e;
                    throw new RuntimeException("Failed to rewrite resource references for " + packageName, cause);
                } catch (InvocationTargetException e2) {
                    cause = e2.getCause();
                    throw new RuntimeException("Failed to rewrite resource references for " + packageName, cause);
                }
            } catch (NoSuchMethodException e3) {
            }
        } catch (ClassNotFoundException e4) {
            Log.i(TAG, "No resource references to update in package " + packageName);
        }
    }

    public void removeContextRegistrations(Context context, String who, String what) {
        int i;
        boolean reportRegistrationLeaks = StrictMode.vmRegistrationLeaksEnabled();
        synchronized (this.mReceivers) {
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> rmap = (ArrayMap) this.mReceivers.remove(context);
            if (rmap != null) {
                i = 0;
                while (i < rmap.size()) {
                    ReceiverDispatcher rd = (ReceiverDispatcher) rmap.valueAt(i);
                    IntentReceiverLeaked leak = new IntentReceiverLeaked(what + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + who + " has leaked IntentReceiver " + rd.getIntentReceiver() + " that was " + "originally registered here. Are you missing a " + "call to unregisterReceiver()?");
                    leak.setStackTrace(rd.getLocation().getStackTrace());
                    Slog.e(ActivityThread.TAG, leak.getMessage(), leak);
                    if (reportRegistrationLeaks) {
                        StrictMode.onIntentReceiverLeaked(leak);
                    }
                    try {
                        ActivityManager.getService().unregisterReceiver(rd.getIIntentReceiver());
                        i++;
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
            this.mUnregisteredReceivers.remove(context);
        }
        synchronized (this.mServices) {
            ArrayMap<ServiceConnection, ServiceDispatcher> smap = (ArrayMap) this.mServices.remove(context);
            if (smap != null) {
                i = 0;
                while (i < smap.size()) {
                    ServiceDispatcher sd = (ServiceDispatcher) smap.valueAt(i);
                    ServiceConnectionLeaked leak2 = new ServiceConnectionLeaked(what + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + who + " has leaked ServiceConnection " + sd.getServiceConnection() + " that was originally bound here");
                    leak2.setStackTrace(sd.getLocation().getStackTrace());
                    Slog.e(ActivityThread.TAG, leak2.getMessage(), leak2);
                    if (reportRegistrationLeaks) {
                        StrictMode.onServiceConnectionLeaked(leak2);
                    }
                    try {
                        ActivityManager.getService().unbindService(sd.getIServiceConnection());
                        sd.doForget();
                        i++;
                    } catch (RemoteException e2) {
                        throw e2.rethrowFromSystemServer();
                    }
                }
            }
            this.mUnboundServices.remove(context);
        }
    }

    private void checkRecevierRegisteredLeakLocked() {
        int count = 0;
        int contextNum = 0;
        for (Entry<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> entry : this.mReceivers.entrySet()) {
            count += ((ArrayMap) entry.getValue()).size();
            contextNum++;
        }
        if (2 == this.mReceiverResource.acquire(getApplicationInfo().uid, getPackageName(), (this.mApplicationInfo.flags & 1) != 0 ? 2 : 0, count)) {
            Log.e(TAG, getPackageName() + " registered " + count + " Receivers " + " in " + contextNum + " Contexts" + 10 + Log.getStackTraceString(new Throwable()));
            throw new AssertionError("Register too many Broadcast Receivers");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{SYNTHETIC, Splitter: B:24:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x001f A:{SYNTHETIC, Splitter: B:10:0x001f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r, Context context, Handler handler, Instrumentation instrumentation, boolean registered) {
        Throwable th;
        synchronized (this.mReceivers) {
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> map;
            ReceiverDispatcher rd;
            ReceiverDispatcher rd2;
            IIntentReceiver iIntentReceiver;
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> map2 = null;
            if (registered) {
                try {
                    map2 = (ArrayMap) this.mReceivers.get(context);
                    if (map2 != null) {
                        map = map2;
                        rd = (ReceiverDispatcher) map2.get(r);
                        if (rd != null) {
                            try {
                                rd2 = new ReceiverDispatcher(r, context, handler, instrumentation, registered);
                                if (registered) {
                                    if (map == null) {
                                        try {
                                            map2 = new ArrayMap();
                                            this.mReceivers.put(context, map2);
                                        } catch (Throwable th2) {
                                            th = th2;
                                            map2 = map;
                                            throw th;
                                        }
                                    }
                                    map2 = map;
                                    map2.put(r, rd2);
                                    if (this.mReceiverResource != null) {
                                        checkRecevierRegisteredLeakLocked();
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                map2 = map;
                                throw th;
                            }
                        }
                        rd.validate(context, handler);
                        map2 = map;
                        rd2 = rd;
                        rd2.mForgotten = false;
                        iIntentReceiver = rd2.getIIntentReceiver();
                        return iIntentReceiver;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
            map = map2;
            rd = null;
            if (rd != null) {
            }
            rd2.mForgotten = false;
            iIntentReceiver = rd2.getIIntentReceiver();
            return iIntentReceiver;
        }
    }

    public IIntentReceiver forgetReceiverDispatcher(Context context, BroadcastReceiver r) {
        IIntentReceiver iIntentReceiver;
        synchronized (this.mReceivers) {
            ReceiverDispatcher rd;
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> holder;
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> map = (ArrayMap) this.mReceivers.get(context);
            if (map != null) {
                rd = (ReceiverDispatcher) map.get(r);
                if (rd != null) {
                    map.remove(r);
                    if (map.size() == 0) {
                        this.mReceivers.remove(context);
                    }
                    if (r.getDebugUnregister()) {
                        holder = (ArrayMap) this.mUnregisteredReceivers.get(context);
                        if (holder == null) {
                            holder = new ArrayMap();
                            this.mUnregisteredReceivers.put(context, holder);
                        }
                        RuntimeException ex = new IllegalArgumentException("Originally unregistered here:");
                        ex.fillInStackTrace();
                        rd.setUnregisterLocation(ex);
                        holder.put(r, rd);
                    }
                    rd.mForgotten = true;
                    iIntentReceiver = rd.getIIntentReceiver();
                }
            }
            holder = (ArrayMap) this.mUnregisteredReceivers.get(context);
            if (holder != null) {
                rd = (ReceiverDispatcher) holder.get(r);
                if (rd != null) {
                    throw new IllegalArgumentException("Unregistering Receiver " + r + " that was already unregistered", rd.getUnregisterLocation());
                }
            }
            if (context == null) {
                throw new IllegalStateException("Unbinding Receiver " + r + " from Context that is no longer in use: " + context);
            }
            throw new IllegalArgumentException("Receiver not registered: " + r);
        }
        return iIntentReceiver;
    }

    public final IServiceConnection getServiceDispatcher(ServiceConnection c, Context context, Handler handler, int flags) {
        Throwable th;
        synchronized (this.mServices) {
            try {
                ServiceDispatcher sd;
                ServiceDispatcher sd2;
                ArrayMap<ServiceConnection, ServiceDispatcher> map = (ArrayMap) this.mServices.get(context);
                if (map != null) {
                    sd = (ServiceDispatcher) map.get(c);
                } else {
                    sd = null;
                }
                if (sd == null) {
                    try {
                        sd2 = new ServiceDispatcher(c, context, handler, flags);
                        if (map == null) {
                            map = new ArrayMap();
                            this.mServices.put(context, map);
                        }
                        map.put(c, sd2);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                sd.validate(context, handler);
                sd2 = sd;
                IServiceConnection iServiceConnection = sd2.getIServiceConnection();
                return iServiceConnection;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public final IServiceConnection forgetServiceDispatcher(Context context, ServiceConnection c) {
        IServiceConnection iServiceConnection;
        synchronized (this.mServices) {
            ServiceDispatcher sd;
            ArrayMap<ServiceConnection, ServiceDispatcher> holder;
            ArrayMap<ServiceConnection, ServiceDispatcher> map = (ArrayMap) this.mServices.get(context);
            if (map != null) {
                sd = (ServiceDispatcher) map.get(c);
                if (sd != null) {
                    map.remove(c);
                    sd.doForget();
                    if (map.size() == 0) {
                        this.mServices.remove(context);
                    }
                    if ((sd.getFlags() & 2) != 0) {
                        holder = (ArrayMap) this.mUnboundServices.get(context);
                        if (holder == null) {
                            holder = new ArrayMap();
                            this.mUnboundServices.put(context, holder);
                        }
                        RuntimeException ex = new IllegalArgumentException("Originally unbound here:");
                        ex.fillInStackTrace();
                        sd.setUnbindLocation(ex);
                        holder.put(c, sd);
                    }
                    iServiceConnection = sd.getIServiceConnection();
                }
            }
            holder = (ArrayMap) this.mUnboundServices.get(context);
            if (holder != null) {
                sd = (ServiceDispatcher) holder.get(c);
                if (sd != null) {
                    throw new IllegalArgumentException("Unbinding Service " + c + " that was already unbound", sd.getUnbindLocation());
                }
            }
            if (context == null) {
                throw new IllegalStateException("Unbinding Service " + c + " from Context that is no longer in use: " + context);
            }
            throw new IllegalArgumentException("Service not registered: " + c);
        }
        return iServiceConnection;
    }
}
