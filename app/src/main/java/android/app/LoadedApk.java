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
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.hwtheme.HwThemeManager;
import android.net.ProxyInfo;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.DocumentsContract.Document;
import android.rms.HwSysResource;
import android.security.keymaster.KeymasterDefs;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayAdjustments;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

public final class LoadedApk {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final String TAG = "LoadedApk";
    private final ActivityThread mActivityThread;
    private String mAppDir;
    private Application mApplication;
    private ApplicationInfo mApplicationInfo;
    private final ClassLoader mBaseClassLoader;
    private ClassLoader mClassLoader;
    int mClientCount;
    private File mCredentialProtectedDataDirFile;
    private String mDataDir;
    private File mDataDirFile;
    private File mDeviceProtectedDataDirFile;
    private final DisplayAdjustments mDisplayAdjustments;
    private final boolean mIncludeCode;
    private String mLibDir;
    private String[] mOverlayDirs;
    final String mPackageName;
    private HwSysResource mReceiverResource;
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mReceivers;
    private final boolean mRegisterPackage;
    private String mResDir;
    Resources mResources;
    private final boolean mSecurityViolation;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mServices;
    private String[] mSharedLibraries;
    private String[] mSplitAppDirs;
    private String[] mSplitResDirs;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mUnboundServices;
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mUnregisteredReceivers;

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

        final class Args extends PendingResult implements Runnable {
            private Intent mCurIntent;
            private boolean mDispatched;
            private Intent mLastIntent;
            private final boolean mOrdered;

            public Args(Intent intent, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, int sendingUser) {
                super(resultCode, resultData, resultExtras, ReceiverDispatcher.this.mRegistered ? 1 : 2, ordered, sticky, ReceiverDispatcher.this.mIIntentReceiver.asBinder(), sendingUser, intent.getFlags());
                this.mCurIntent = intent;
                this.mOrdered = ordered;
            }

            public void run() {
                BroadcastReceiver receiver = ReceiverDispatcher.this.mReceiver;
                boolean ordered = this.mOrdered;
                IActivityManager mgr = ActivityManagerNative.getDefault();
                Intent intent = this.mCurIntent;
                if (intent == null) {
                    Log.wtf(LoadedApk.TAG, "Null intent being dispatched, mDispatched=" + this.mDispatched);
                    return;
                }
                this.mCurIntent = null;
                this.mDispatched = true;
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
                    receiver.onReceive(ReceiverDispatcher.this.mContext, intent);
                } catch (Exception e) {
                    if (ReceiverDispatcher.this.mRegistered && ordered) {
                        sendFinished(mgr);
                    }
                    if (ReceiverDispatcher.this.mInstrumentation == null || !ReceiverDispatcher.this.mInstrumentation.onException(ReceiverDispatcher.this.mReceiver, e)) {
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
                ReceiverDispatcher receiverDispatcher;
                if (intent == null) {
                    Log.wtf(LoadedApk.TAG, "Null intent received");
                    receiverDispatcher = null;
                } else {
                    receiverDispatcher = (ReceiverDispatcher) this.mDispatcher.get();
                }
                if (receiverDispatcher != null) {
                    receiverDispatcher.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
                    return;
                }
                IActivityManager mgr = ActivityManagerNative.getDefault();
                if (extras != null) {
                    try {
                        extras.setAllowFds(LoadedApk.-assertionsDisabled);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
                mgr.finishReceiver(this, resultCode, data, extras, LoadedApk.-assertionsDisabled, intent.getFlags());
            }
        }

        ReceiverDispatcher(BroadcastReceiver receiver, Context context, Handler activityThread, Instrumentation instrumentation, boolean registered) {
            if (activityThread == null) {
                throw new NullPointerException("Handler must not be null");
            }
            this.mIIntentReceiver = new InnerReceiver(this, registered ? LoadedApk.-assertionsDisabled : true);
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
            Args args = new Args(intent, resultCode, data, extras, ordered, sticky, sendingUser);
            if (intent == null) {
                Log.wtf(LoadedApk.TAG, "Null intent received");
            }
            if ((intent == null || !this.mActivityThread.post(args)) && this.mRegistered && ordered) {
                args.sendFinished(ActivityManagerNative.getDefault());
            }
        }
    }

    static final class ServiceDispatcher {
        private final ArrayMap<ComponentName, ConnectionInfo> mActiveConnections;
        private final Handler mActivityThread;
        private final ServiceConnection mConnection;
        private final Context mContext;
        private final int mFlags;
        private boolean mForgotten;
        private final InnerConnection mIServiceConnection;
        private final ServiceConnectionLeaked mLocation;
        private RuntimeException mUnbindLocation;

        private static class ConnectionInfo {
            IBinder binder;
            DeathRecipient deathMonitor;

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

            public void connected(ComponentName name, IBinder service) throws RemoteException {
                ServiceDispatcher sd = (ServiceDispatcher) this.mDispatcher.get();
                if (sd != null) {
                    sd.connected(name, service);
                }
            }
        }

        private final class RunConnection implements Runnable {
            final int mCommand;
            final ComponentName mName;
            final IBinder mService;

            RunConnection(ComponentName name, IBinder service, int command) {
                this.mName = name;
                this.mService = service;
                this.mCommand = command;
            }

            public void run() {
                if (this.mCommand == 0) {
                    ServiceDispatcher.this.doConnected(this.mName, this.mService);
                } else if (this.mCommand == 1) {
                    ServiceDispatcher.this.doDeath(this.mName, this.mService);
                }
            }
        }

        ServiceDispatcher(ServiceConnection conn, Context context, Handler activityThread, int flags) {
            this.mActiveConnections = new ArrayMap();
            this.mIServiceConnection = new InnerConnection(this);
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void doForget() {
            synchronized (this) {
                int i = 0;
                while (true) {
                    if (i < this.mActiveConnections.size()) {
                        ConnectionInfo ci = (ConnectionInfo) this.mActiveConnections.valueAt(i);
                        ci.binder.unlinkToDeath(ci.deathMonitor, 0);
                        i++;
                    } else {
                        this.mActiveConnections.clear();
                        this.mForgotten = true;
                    }
                }
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

        public void connected(ComponentName name, IBinder service) {
            if (name == null || !"com.android.systemui.keyguard.KeyguardService".equals(name.getClassName())) {
                if (this.mActivityThread != null) {
                    this.mActivityThread.post(new RunConnection(name, service, 0));
                } else {
                    doConnected(name, service);
                }
                return;
            }
            doConnected(name, service);
        }

        public void death(ComponentName name, IBinder service) {
            if (this.mActivityThread != null) {
                this.mActivityThread.post(new RunConnection(name, service, 1));
            } else {
                doDeath(name, service);
            }
        }

        public void doConnected(ComponentName name, IBinder service) {
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
                    if (old != null) {
                        this.mConnection.onServiceDisconnected(name);
                    }
                    if (service != null) {
                        this.mConnection.onServiceConnected(name, service);
                    }
                    return;
                }
            }
        }

        public void doDeath(ComponentName name, IBinder service) {
            synchronized (this) {
                ConnectionInfo old = (ConnectionInfo) this.mActiveConnections.get(name);
                if (old == null || old.binder != service) {
                    return;
                }
                this.mActiveConnections.remove(name);
                old.binder.unlinkToDeath(old.deathMonitor, 0);
                this.mConnection.onServiceDisconnected(name);
            }
        }
    }

    private static class WarningContextClassLoader extends ClassLoader {
        private static boolean warned;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.LoadedApk.WarningContextClassLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.LoadedApk.WarningContextClassLoader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.LoadedApk.WarningContextClassLoader.<clinit>():void");
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.LoadedApk.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.LoadedApk.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.LoadedApk.<clinit>():void");
    }

    Application getApplication() {
        return this.mApplication;
    }

    public LoadedApk(ActivityThread activityThread, ApplicationInfo aInfo, CompatibilityInfo compatInfo, ClassLoader baseLoader, boolean securityViolation, boolean includeCode, boolean registerPackage) {
        this.mDisplayAdjustments = new DisplayAdjustments();
        this.mReceivers = new ArrayMap();
        this.mUnregisteredReceivers = new ArrayMap();
        this.mServices = new ArrayMap();
        this.mUnboundServices = new ArrayMap();
        this.mClientCount = 0;
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
        this.mDisplayAdjustments = new DisplayAdjustments();
        this.mReceivers = new ArrayMap();
        this.mUnregisteredReceivers = new ArrayMap();
        this.mServices = new ArrayMap();
        this.mUnboundServices = new ArrayMap();
        this.mClientCount = 0;
        this.mActivityThread = activityThread;
        this.mApplicationInfo = new ApplicationInfo();
        this.mApplicationInfo.packageName = ZenModeConfig.SYSTEM_AUTHORITY;
        this.mPackageName = ZenModeConfig.SYSTEM_AUTHORITY;
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
        this.mSecurityViolation = -assertionsDisabled;
        this.mIncludeCode = true;
        this.mRegisterPackage = -assertionsDisabled;
        this.mClassLoader = ClassLoader.getSystemClassLoader();
        this.mResources = Resources.getSystem();
    }

    void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        if (-assertionsDisabled || info.packageName.equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
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
            ApplicationInfo ai = ActivityThread.getPackageManager().getApplicationInfo(packageName, Document.FLAG_SUPPORTS_REMOVE, UserHandle.myUserId());
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
        makePaths(this.mActivityThread, aInfo, newPaths, null);
        List<String> addedPaths = new ArrayList(newPaths.size());
        if (oldPaths != null) {
            for (String path : newPaths) {
                String apkName = path.substring(path.lastIndexOf(File.separator));
                boolean match = -assertionsDisabled;
                for (String oldPath : oldPaths) {
                    if (apkName.equals(oldPath.substring(path.lastIndexOf(File.separator)))) {
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
                this.mResources = this.mActivityThread.getTopLevelResources(this.mResDir, this.mSplitResDirs, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, this);
            }
        }
    }

    private void setApplicationInfo(ApplicationInfo aInfo) {
        int myUid = Process.myUid();
        aInfo = adjustNativeLibraryPaths(aInfo);
        this.mApplicationInfo = aInfo;
        this.mAppDir = aInfo.sourceDir;
        this.mResDir = aInfo.uid == myUid ? aInfo.sourceDir : aInfo.publicSourceDir;
        this.mSplitAppDirs = aInfo.splitSourceDirs;
        this.mSplitResDirs = aInfo.uid == myUid ? aInfo.splitSourceDirs : aInfo.splitPublicSourceDirs;
        this.mOverlayDirs = aInfo.resourceDirs;
        this.mSharedLibraries = aInfo.sharedLibraryFiles;
        this.mDataDir = aInfo.dataDir;
        this.mLibDir = aInfo.nativeLibraryDir;
        this.mDataDirFile = FileUtils.newFileOrNull(aInfo.dataDir);
        this.mDeviceProtectedDataDirFile = FileUtils.newFileOrNull(aInfo.deviceProtectedDataDir);
        this.mCredentialProtectedDataDirFile = FileUtils.newFileOrNull(aInfo.credentialProtectedDataDir);
        this.mDataDirFile = HwFrameworkFactory.getHwSettingsManager().handleDateDirectoryForClone(this.mDataDirFile, aInfo.euid);
        this.mDeviceProtectedDataDirFile = HwFrameworkFactory.getHwSettingsManager().handleDateDirectoryForClone(this.mDeviceProtectedDataDirFile, aInfo.euid);
        this.mCredentialProtectedDataDirFile = HwFrameworkFactory.getHwSettingsManager().handleDateDirectoryForClone(this.mCredentialProtectedDataDirFile, aInfo.euid);
    }

    public static void makePaths(ActivityThread activityThread, ApplicationInfo aInfo, List<String> outZipPaths, List<String> outLibPaths) {
        String appDir = aInfo.sourceDir;
        String[] splitAppDirs = aInfo.splitSourceDirs;
        String libDir = aInfo.nativeLibraryDir;
        String[] sharedLibraries = aInfo.sharedLibraryFiles;
        outZipPaths.clear();
        outZipPaths.add(appDir);
        if (splitAppDirs != null) {
            Collections.addAll(outZipPaths, splitAppDirs);
        }
        if (outLibPaths != null) {
            outLibPaths.clear();
        }
        String instrumentationPackageName = activityThread.mInstrumentationPackageName;
        String instrumentationAppDir = activityThread.mInstrumentationAppDir;
        String[] instrumentationSplitAppDirs = activityThread.mInstrumentationSplitAppDirs;
        String instrumentationLibDir = activityThread.mInstrumentationLibDir;
        String instrumentedAppDir = activityThread.mInstrumentedAppDir;
        String[] instrumentedSplitAppDirs = activityThread.mInstrumentedSplitAppDirs;
        String instrumentedLibDir = activityThread.mInstrumentedLibDir;
        String[] instrumentationLibs = null;
        if (appDir.equals(instrumentationAppDir) || appDir.equals(instrumentedAppDir)) {
            outZipPaths.clear();
            outZipPaths.add(instrumentationAppDir);
            if (instrumentationSplitAppDirs != null) {
                Collections.addAll(outZipPaths, instrumentationSplitAppDirs);
            }
            if (!instrumentationAppDir.equals(instrumentedAppDir)) {
                outZipPaths.add(instrumentedAppDir);
                if (instrumentedSplitAppDirs != null) {
                    Collections.addAll(outZipPaths, instrumentedSplitAppDirs);
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
        if (outLibPaths != null) {
            if (outLibPaths.isEmpty()) {
                outLibPaths.add(libDir);
            }
            if (aInfo.primaryCpuAbi != null) {
                int i = aInfo.targetSdkVersion;
                if (r0 <= 23) {
                    outLibPaths.add("/system/fake-libs" + (VMRuntime.is64BitAbi(aInfo.primaryCpuAbi) ? "64" : ProxyInfo.LOCAL_EXCL_LIST));
                }
                for (String apk : outZipPaths) {
                    outLibPaths.add(((String) apk$iterator.next()) + "!/lib/" + aInfo.primaryCpuAbi);
                }
            }
            if (aInfo.isSystemApp() && !aInfo.isUpdatedSystemApp()) {
                outLibPaths.add(System.getProperty("java.library.path"));
            }
        }
        if (sharedLibraries != null) {
            for (String lib : sharedLibraries) {
                if (!outZipPaths.contains(lib)) {
                    outZipPaths.add(0, lib);
                }
            }
        }
        if (instrumentationLibs != null) {
            for (String lib2 : instrumentationLibs) {
                if (!outZipPaths.contains(lib2)) {
                    outZipPaths.add(0, lib2);
                }
            }
        }
    }

    private void createOrUpdateClassLoaderLocked(List<String> addedPaths) {
        if (!this.mPackageName.equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
            if (!Objects.equals(this.mPackageName, ActivityThread.currentPackageName())) {
                VMRuntime.getRuntime().vmInstructionSet();
                try {
                    ActivityThread.getPackageManager().notifyPackageUse(this.mPackageName, 6);
                } catch (RemoteException re) {
                    throw re.rethrowFromSystemServer();
                }
            }
            if (this.mRegisterPackage) {
                try {
                    ActivityManagerNative.getDefault().addPackageDependency(this.mPackageName);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            List<String> zipPaths = new ArrayList(10);
            List<String> libPaths = new ArrayList(10);
            makePaths(this.mActivityThread, this.mApplicationInfo, zipPaths, libPaths);
            boolean isBundledApp = this.mApplicationInfo.isSystemApp() ? this.mApplicationInfo.isUpdatedSystemApp() ? -assertionsDisabled : true : -assertionsDisabled;
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
                boolean needToSetupJitProfiles = -assertionsDisabled;
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
                if (needToSetupJitProfiles && !ActivityThread.isSystem()) {
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
        if (SystemProperties.getBoolean("dalvik.vm.usejitprofiles", -assertionsDisabled) && this.mApplicationInfo.uid == Process.myUid()) {
            List<String> codePaths = new ArrayList();
            if ((this.mApplicationInfo.flags & 4) != 0) {
                codePaths.add(this.mApplicationInfo.sourceDir);
            }
            if (this.mApplicationInfo.splitSourceDirs != null) {
                Collections.addAll(codePaths, this.mApplicationInfo.splitSourceDirs);
            }
            if (!codePaths.isEmpty()) {
                VMRuntime.registerAppInfo(getPrimaryProfileFile(this.mPackageName).getPath(), this.mApplicationInfo.dataDir, (String[]) codePaths.toArray(new String[codePaths.size()]), Environment.getDataProfilesDeForeignDexDirectory(UserHandle.myUserId()).getPath());
            }
        }
    }

    private void initializeJavaContextClassLoader() {
        try {
            PackageInfo pi = ActivityThread.getPackageManager().getPackageInfo(this.mPackageName, KeymasterDefs.KM_ENUM, UserHandle.myUserId());
            if (pi == null) {
                Slog.w(TAG, "Unable to get package info for " + this.mPackageName + "; is package not installed?");
                return;
            }
            ClassLoader contextClassLoader;
            boolean sharedUserIdSet = pi.sharedUserId != null ? true : -assertionsDisabled;
            boolean processNameNotDefault = pi.applicationInfo != null ? this.mPackageName.equals(pi.applicationInfo.processName) ? -assertionsDisabled : true : -assertionsDisabled;
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

    public AssetManager getAssets(ActivityThread mainThread) {
        Resources resources = getResources(mainThread);
        if (resources != null) {
            return resources.getAssets();
        }
        return null;
    }

    public Resources getResources(ActivityThread mainThread) {
        if (this.mResources == null) {
            this.mResources = mainThread.getTopLevelResources(this.mResDir, this.mSplitResDirs, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, this);
        }
        this.mResources = HwThemeManager.updateHwtResource(mainThread, this.mResources, this.mResDir, this.mSplitResDirs, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, this);
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
        Application application = null;
        String appClass = this.mApplicationInfo.className;
        if (forceDefaultAppClass || appClass == null) {
            appClass = "android.app.Application";
        }
        try {
            ClassLoader cl = getClassLoader();
            if (!this.mPackageName.equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
                Trace.traceBegin(64, "initializeJavaContextClassLoader");
                initializeJavaContextClassLoader();
                Trace.traceEnd(64);
            }
            ContextImpl appContext = ContextImpl.createAppContext(this.mActivityThread, this);
            application = this.mActivityThread.mInstrumentation.newApplication(cl, appClass, appContext);
            appContext.setOuterContext(application);
        } catch (Exception e) {
            if (!this.mActivityThread.mInstrumentation.onException(null, e)) {
                Trace.traceEnd(64);
                throw new RuntimeException("Unable to instantiate application " + appClass + ": " + e.toString(), e);
            }
        }
        this.mActivityThread.mAllApplications.add(application);
        this.mApplication = application;
        if (this.mReceiverResource == null) {
            this.mReceiverResource = HwFrameworkFactory.getHwResource(12);
        }
        if (instrumentation != null) {
            try {
                instrumentation.callApplicationOnCreate(application);
            } catch (Exception e2) {
                if (!instrumentation.onException(application, e2)) {
                    Trace.traceEnd(64);
                    throw new RuntimeException("Unable to create application " + application.getClass().getName() + ": " + e2.toString(), e2);
                }
            }
        }
        AssetManager assetManager = getAssets(this.mActivityThread);
        if (assetManager != null) {
            SparseArray<String> packageIdentifiers = assetManager.getAssignedPackageIdentifiers();
            int N = packageIdentifiers.size();
            for (int i = 0; i < N; i++) {
                int id = packageIdentifiers.keyAt(i);
                if (!(id == 1 || id == InformationElement.EID_EXTENDED_CAPS)) {
                    rewriteRValues(getClassLoader(), (String) packageIdentifiers.valueAt(i), id);
                }
            }
        }
        Trace.traceEnd(64);
        return application;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                        ActivityManagerNative.getDefault().unregisterReceiver(rd.getIIntentReceiver());
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
                        ActivityManagerNative.getDefault().unbindService(sd.getIServiceConnection());
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
            Log.e(TAG, getPackageName() + " registered " + count + " Receivers " + " in " + contextNum + " Contexts" + '\n' + Log.getStackTraceString(new Throwable()));
            throw new AssertionError("Register too many Broadcast Receivers");
        }
    }

    public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r, Context context, Handler handler, Instrumentation instrumentation, boolean registered) {
        ArrayMap<BroadcastReceiver, ReceiverDispatcher> map;
        Throwable th;
        synchronized (this.mReceivers) {
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
                                } else {
                                    if (map != null) {
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
                        rd2.mForgotten = -assertionsDisabled;
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
                rd.validate(context, handler);
                map2 = map;
                rd2 = rd;
            } else {
                rd2 = new ReceiverDispatcher(r, context, handler, instrumentation, registered);
                if (registered) {
                } else {
                    if (map != null) {
                        map2 = map;
                    } else {
                        map2 = new ArrayMap();
                        this.mReceivers.put(context, map2);
                    }
                    map2.put(r, rd2);
                    if (this.mReceiverResource != null) {
                        checkRecevierRegisteredLeakLocked();
                    }
                }
            }
            rd2.mForgotten = -assertionsDisabled;
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
