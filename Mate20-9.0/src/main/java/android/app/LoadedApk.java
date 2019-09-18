package android.app;

import android.app.IServiceConnection;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.dex.ArtManager;
import android.content.pm.split.SplitDependencyLoader;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
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
import java.lang.annotation.RCUnownedThisRef;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoadedApk {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final boolean DEBUG = false;
    private static final String PROPERTY_NAME_APPEND_NATIVE = "pi.append_native_lib_paths";
    static final String TAG = "LoadedApk";
    static AtomicInteger mBroadcastDebugID = new AtomicInteger(0);
    private static boolean sIsMygote;
    private final ActivityThread mActivityThread;
    private AppComponentFactory mAppComponentFactory;
    private String mAppDir;
    private Application mApplication;
    /* access modifiers changed from: private */
    public ApplicationInfo mApplicationInfo;
    private final ClassLoader mBaseClassLoader;
    /* access modifiers changed from: private */
    public ClassLoader mClassLoader;
    private File mCredentialProtectedDataDirFile;
    private String mDataDir;
    private File mDataDirFile;
    private File mDeviceProtectedDataDirFile;
    private final DisplayAdjustments mDisplayAdjustments = new DisplayAdjustments();
    private final boolean mIncludeCode;
    private String mLibDir;
    private String[] mOverlayDirs;
    final String mPackageName;
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mReceivers = new ArrayMap<>();
    private final boolean mRegisterPackage;
    private String mResDir;
    Resources mResources;
    private final boolean mSecurityViolation;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mServices = new ArrayMap<>();
    /* access modifiers changed from: private */
    public String[] mSplitAppDirs;
    /* access modifiers changed from: private */
    public String[] mSplitClassLoaderNames;
    private SplitDependencyLoaderImpl mSplitLoader;
    /* access modifiers changed from: private */
    public String[] mSplitNames;
    /* access modifiers changed from: private */
    public String[] mSplitResDirs;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mUnboundServices = new ArrayMap<>();
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mUnregisteredReceivers = new ArrayMap<>();

    static final class ReceiverDispatcher {
        final Handler mActivityThread;
        final Context mContext;
        boolean mForgotten;
        final IIntentReceiver.Stub mIIntentReceiver;
        final Instrumentation mInstrumentation;
        final IntentReceiverLeaked mLocation;
        final BroadcastReceiver mReceiver;
        final boolean mRegistered;
        RuntimeException mUnregisterLocation;

        final class Args extends BroadcastReceiver.PendingResult {
            private Intent mCurIntent;
            int mDebugID;
            private boolean mDispatched;
            private Intent mLastIntent;
            private final boolean mOrdered;
            private Throwable mPreviousRunStacktrace;
            final /* synthetic */ ReceiverDispatcher this$0;

            /* JADX WARNING: Illegal instructions before constructor call */
            public Args(ReceiverDispatcher this$02, Intent intent, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, int sendingUser, int debugID) {
                super(resultCode, resultData, resultExtras, r11.mRegistered ? 1 : 2, ordered, sticky, r11.mIIntentReceiver.asBinder(), sendingUser, intent.getFlags());
                ReceiverDispatcher receiverDispatcher = this$02;
                this.this$0 = receiverDispatcher;
                this.mCurIntent = intent;
                this.mOrdered = ordered;
                this.mDebugID = debugID;
            }

            public final Runnable getRunnable() {
                return 
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0005: RETURN  (wrap: android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA
                      0x0002: CONSTRUCTOR  (r0v0 android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA) = (r1v0 'this' android.app.LoadedApk$ReceiverDispatcher$Args A[THIS]) android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA.<init>(android.app.LoadedApk$ReceiverDispatcher$Args):void CONSTRUCTOR) in method: android.app.LoadedApk.ReceiverDispatcher.Args.getRunnable():java.lang.Runnable, dex: boot-framework_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0v0 android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA) = (r1v0 'this' android.app.LoadedApk$ReceiverDispatcher$Args A[THIS]) android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA.<init>(android.app.LoadedApk$ReceiverDispatcher$Args):void CONSTRUCTOR in method: android.app.LoadedApk.ReceiverDispatcher.Args.getRunnable():java.lang.Runnable, dex: boot-framework_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:303)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 26 more
                    */
                /*
                    this = this;
                    android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA r0 = new android.app.-$$Lambda$LoadedApk$ReceiverDispatcher$Args$_BumDX2UKsnxLVrE6UJsJZkotuA
                    r0.<init>(r1)
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.LoadedApk.ReceiverDispatcher.Args.getRunnable():java.lang.Runnable");
            }

            public static /* synthetic */ void lambda$getRunnable$0(Args args) {
                BroadcastReceiver receiver = args.this$0.mReceiver;
                boolean ordered = args.mOrdered;
                if (ActivityThread.DEBUG_BROADCAST) {
                    int seq = args.mCurIntent.getIntExtra("seq", -1);
                    Slog.i(ActivityThread.TAG, "Dispatching broadcast " + args.mCurIntent.getAction() + " seq=" + seq + " to " + args.this$0.mReceiver);
                    StringBuilder sb = new StringBuilder();
                    sb.append("  mRegistered=");
                    sb.append(args.this$0.mRegistered);
                    sb.append(" mOrderedHint=");
                    sb.append(ordered);
                    Slog.i(ActivityThread.TAG, sb.toString());
                }
                IActivityManager mgr = ActivityManager.getService();
                Intent intent = args.mCurIntent;
                if (intent == null) {
                    Log.wtf(LoadedApk.TAG, "Null intent being dispatched, mDispatched=" + args.mDispatched + ": run() previously called at " + Log.getStackTraceString(args.mPreviousRunStacktrace));
                    return;
                }
                args.mCurIntent = null;
                args.mDispatched = true;
                args.mPreviousRunStacktrace = new Throwable("Previous stacktrace");
                if (receiver == null || intent == null || args.this$0.mForgotten) {
                    if (args.this$0.mRegistered && ordered) {
                        if (ActivityThread.DEBUG_BROADCAST) {
                            Slog.i(ActivityThread.TAG, "Finishing null broadcast to " + args.this$0.mReceiver);
                        }
                        args.sendFinished(mgr);
                    }
                    return;
                }
                Trace.traceBegin(64, "broadcastReceiveReg");
                try {
                    ClassLoader cl = args.this$0.mReceiver.getClass().getClassLoader();
                    intent.setExtrasClassLoader(cl);
                    intent.prepareToEnterProcess();
                    args.setExtrasClassLoader(cl);
                    receiver.setPendingResult(args);
                    args.mLastIntent = intent;
                    if (Log.HWINFO) {
                        Trace.traceBegin(64, "broadcast[" + intent.getAction() + "](" + args.mDebugID + ") call onReceive[" + receiver.getClass().getName() + "]");
                    }
                    receiver.onReceive(args.this$0.mContext, intent);
                    if (Log.HWINFO) {
                        Trace.traceEnd(64);
                    }
                } catch (Exception e) {
                    if (args.this$0.mRegistered && ordered) {
                        if (ActivityThread.DEBUG_BROADCAST) {
                            Slog.i(ActivityThread.TAG, "Finishing failed broadcast to " + args.this$0.mReceiver);
                        }
                        args.sendFinished(mgr);
                    }
                    if (args.this$0.mInstrumentation == null || !args.this$0.mInstrumentation.onException(args.this$0.mReceiver, e)) {
                        Trace.traceEnd(64);
                        throw new RuntimeException("Error receiving broadcast " + intent + " in " + args.this$0.mReceiver, e);
                    }
                }
                if (receiver.getPendingResult() != null) {
                    args.finish();
                }
                Trace.traceEnd(64);
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
                try {
                    if (this.this$0.mReceiver != null) {
                        sb.append(" receiver=");
                        sb.append(this.this$0.mReceiver.getClass().getName());
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

        static final class InnerReceiver extends IIntentReceiver.Stub {
            final WeakReference<ReceiverDispatcher> mDispatcher;
            final ReceiverDispatcher mStrongRef;

            InnerReceiver(ReceiverDispatcher rd, boolean strong) {
                this.mDispatcher = new WeakReference<>(rd);
                this.mStrongRef = strong ? rd : null;
            }

            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                InnerReceiver innerReceiver;
                ReceiverDispatcher rd;
                Intent intent2 = intent;
                Bundle bundle = extras;
                if (intent2 == null) {
                    Log.wtf(LoadedApk.TAG, "Null intent received");
                    rd = null;
                    innerReceiver = this;
                } else {
                    innerReceiver = this;
                    rd = (ReceiverDispatcher) innerReceiver.mDispatcher.get();
                }
                ReceiverDispatcher rd2 = rd;
                if (ActivityThread.DEBUG_BROADCAST) {
                    int seq = intent2.getIntExtra("seq", -1);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Receiving broadcast ");
                    sb.append(intent2.getAction());
                    sb.append(" seq=");
                    sb.append(seq);
                    sb.append(" to ");
                    sb.append(rd2 != null ? rd2.mReceiver : null);
                    Slog.i(ActivityThread.TAG, sb.toString());
                }
                if (rd2 != null) {
                    rd2.performReceive(intent2, resultCode, data, bundle, ordered, sticky, sendingUser);
                    return;
                }
                if (ActivityThread.DEBUG_BROADCAST) {
                    Slog.i(ActivityThread.TAG, "Finishing broadcast to unregistered receiver");
                }
                IActivityManager mgr = ActivityManager.getService();
                if (bundle != null) {
                    try {
                        bundle.setAllowFds(false);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
                mgr.finishReceiver(innerReceiver, resultCode, data, bundle, false, intent2.getFlags());
            }
        }

        ReceiverDispatcher(BroadcastReceiver receiver, Context context, Handler activityThread, Instrumentation instrumentation, boolean registered) {
            if (activityThread != null) {
                this.mIIntentReceiver = new InnerReceiver(this, !registered);
                this.mReceiver = receiver;
                this.mContext = context;
                this.mActivityThread = activityThread;
                this.mInstrumentation = instrumentation;
                this.mRegistered = registered;
                this.mLocation = new IntentReceiverLeaked(null);
                this.mLocation.fillInStackTrace();
                return;
            }
            throw new NullPointerException("Handler must not be null");
        }

        /* access modifiers changed from: package-private */
        public void validate(Context context, Handler activityThread) {
            if (this.mContext != context) {
                throw new IllegalStateException("Receiver " + this.mReceiver + " registered with differing Context (was " + this.mContext + " now " + context + ")");
            } else if (this.mActivityThread != activityThread) {
                throw new IllegalStateException("Receiver " + this.mReceiver + " registered with differing handler (was " + this.mActivityThread + " now " + activityThread + ")");
            }
        }

        /* access modifiers changed from: package-private */
        public IntentReceiverLeaked getLocation() {
            return this.mLocation;
        }

        /* access modifiers changed from: package-private */
        public BroadcastReceiver getIntentReceiver() {
            return this.mReceiver;
        }

        /* access modifiers changed from: package-private */
        public IIntentReceiver getIIntentReceiver() {
            return this.mIIntentReceiver;
        }

        /* access modifiers changed from: package-private */
        public void setUnregisterLocation(RuntimeException ex) {
            this.mUnregisterLocation = ex;
        }

        /* access modifiers changed from: package-private */
        public RuntimeException getUnregisterLocation() {
            return this.mUnregisterLocation;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
            Intent intent2 = intent;
            int debugID = 0;
            if (Log.HWINFO) {
                debugID = LoadedApk.mBroadcastDebugID.incrementAndGet();
            }
            int debugID2 = debugID;
            Args args = new Args(this, intent2, resultCode, data, extras, ordered, sticky, sendingUser, debugID2);
            if (intent2 == null) {
                Log.wtf(LoadedApk.TAG, "Null intent received");
            } else if (ActivityThread.DEBUG_BROADCAST) {
                int seq = intent2.getIntExtra("seq", -1);
                Slog.i(ActivityThread.TAG, "Enqueueing broadcast " + intent2.getAction() + " seq=" + seq + " to " + this.mReceiver);
            }
            if (Log.HWINFO != 0) {
                Long l = null;
                Looper looper = this.mActivityThread != null ? this.mActivityThread.getLooper() : null;
                Thread thread = looper != null ? looper.getThread() : null;
                StringBuilder sb = new StringBuilder();
                sb.append("broadcast[");
                sb.append(intent2 != null ? intent2.getAction() : null);
                sb.append("](");
                sb.append(debugID2);
                sb.append(") send msg to thread[");
                sb.append(thread != null ? thread.getName() : null);
                sb.append("(");
                if (thread != null) {
                    l = Long.valueOf(thread.getId());
                }
                sb.append(l);
                sb.append(")");
                sb.append("], msg queue length[");
                sb.append(looper != null ? looper.getQueue().getMessageCount() : 0);
                sb.append("]");
                Trace.traceBegin(64, sb.toString());
                if (ActivityThread.DEBUG_HW_BROADCAST) {
                    Slog.i(ActivityThread.TAG, sb.toString());
                }
            }
            if ((intent2 == null || !this.mActivityThread.post(args.getRunnable())) && this.mRegistered && ordered) {
                IActivityManager mgr = ActivityManager.getService();
                if (ActivityThread.DEBUG_BROADCAST) {
                    Slog.i(ActivityThread.TAG, "Finishing sync broadcast to " + this.mReceiver);
                }
                args.sendFinished(mgr);
            }
            if (Log.HWINFO) {
                Trace.traceEnd(64);
            }
        }
    }

    static final class ServiceDispatcher {
        private final ArrayMap<ComponentName, ConnectionInfo> mActiveConnections = new ArrayMap<>();
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
            IBinder.DeathRecipient deathMonitor;

            private ConnectionInfo() {
            }
        }

        private final class DeathMonitor implements IBinder.DeathRecipient {
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

        @RCUnownedThisRef
        private static class InnerConnection extends IServiceConnection.Stub {
            final WeakReference<ServiceDispatcher> mDispatcher;

            InnerConnection(ServiceDispatcher sd) {
                this.mDispatcher = new WeakReference<>(sd);
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

        /* access modifiers changed from: package-private */
        public void validate(Context context, Handler activityThread) {
            if (this.mContext != context) {
                throw new RuntimeException("ServiceConnection " + this.mConnection + " registered with differing Context (was " + this.mContext + " now " + context + ")");
            } else if (this.mActivityThread != activityThread) {
                throw new RuntimeException("ServiceConnection " + this.mConnection + " registered with differing handler (was " + this.mActivityThread + " now " + activityThread + ")");
            }
        }

        /* access modifiers changed from: package-private */
        public void doForget() {
            synchronized (this) {
                for (int i = 0; i < this.mActiveConnections.size(); i++) {
                    ConnectionInfo ci = this.mActiveConnections.valueAt(i);
                    ci.binder.unlinkToDeath(ci.deathMonitor, 0);
                }
                this.mActiveConnections.clear();
                this.mForgotten = true;
            }
        }

        /* access modifiers changed from: package-private */
        public ServiceConnectionLeaked getLocation() {
            return this.mLocation;
        }

        /* access modifiers changed from: package-private */
        public ServiceConnection getServiceConnection() {
            return this.mConnection;
        }

        /* access modifiers changed from: package-private */
        public IServiceConnection getIServiceConnection() {
            return this.mIServiceConnection;
        }

        /* access modifiers changed from: package-private */
        public int getFlags() {
            return this.mFlags;
        }

        /* access modifiers changed from: package-private */
        public void setUnbindLocation(RuntimeException ex) {
            this.mUnbindLocation = ex;
        }

        /* access modifiers changed from: package-private */
        public RuntimeException getUnbindLocation() {
            return this.mUnbindLocation;
        }

        public void connected(ComponentName name, IBinder service, boolean dead) {
            if (name == null || !"com.android.systemui.keyguard.KeyguardService".equals(name.getClassName())) {
                if (this.mActivityThread != null) {
                    Handler handler = this.mActivityThread;
                    RunConnection runConnection = new RunConnection(name, service, 0, dead);
                    handler.post(runConnection);
                } else {
                    doConnected(name, service, dead);
                }
                return;
            }
            doConnected(name, service, dead);
        }

        public void death(ComponentName name, IBinder service) {
            if (this.mActivityThread != null) {
                Handler handler = this.mActivityThread;
                RunConnection runConnection = new RunConnection(name, service, 1, false);
                handler.post(runConnection);
                return;
            }
            doDeath(name, service);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:26:0x004b, code lost:
            if (r0 == null) goto L_0x0052;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x004d, code lost:
            r4.mConnection.onServiceDisconnected(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0052, code lost:
            if (r7 == false) goto L_0x0059;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0054, code lost:
            r4.mConnection.onBindingDied(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0059, code lost:
            if (r6 == null) goto L_0x0061;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
            r4.mConnection.onServiceConnected(r5, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0061, code lost:
            r4.mConnection.onNullBinding(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0066, code lost:
            return;
         */
        public void doConnected(ComponentName name, IBinder service, boolean dead) {
            synchronized (this) {
                if (!this.mForgotten) {
                    ConnectionInfo old = this.mActiveConnections.get(name);
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
                        } else {
                            this.mActiveConnections.remove(name);
                        }
                        if (old != null) {
                            old.binder.unlinkToDeath(old.deathMonitor, 0);
                        }
                    }
                }
            }
        }

        public void doDeath(ComponentName name, IBinder service) {
            synchronized (this) {
                ConnectionInfo old = this.mActiveConnections.get(name);
                if (old != null) {
                    if (old.binder == service) {
                        this.mActiveConnections.remove(name);
                        old.binder.unlinkToDeath(old.deathMonitor, 0);
                        this.mConnection.onServiceDisconnected(name);
                    }
                }
            }
        }
    }

    private class SplitDependencyLoaderImpl extends SplitDependencyLoader<PackageManager.NameNotFoundException> {
        private final ClassLoader[] mCachedClassLoaders;
        private final String[][] mCachedResourcePaths;

        SplitDependencyLoaderImpl(SparseArray<int[]> dependencies) {
            super(dependencies);
            this.mCachedResourcePaths = new String[(LoadedApk.this.mSplitNames.length + 1)][];
            this.mCachedClassLoaders = new ClassLoader[(LoadedApk.this.mSplitNames.length + 1)];
        }

        /* access modifiers changed from: protected */
        public boolean isSplitCached(int splitIdx) {
            return this.mCachedClassLoaders[splitIdx] != null;
        }

        private void makeSplitLibPaths(String splitDir, ApplicationInfo aInfo, List<String> outLibPaths) {
            if (outLibPaths != null && aInfo.primaryCpuAbi != null) {
                outLibPaths.clear();
                outLibPaths.add(splitDir + "!/lib/" + aInfo.primaryCpuAbi);
                String filePath = splitDir.substring(0, splitDir.lastIndexOf(File.separator));
                outLibPaths.add(filePath + "/" + "lib" + "/" + VMRuntime.getInstructionSet(aInfo.primaryCpuAbi));
            }
        }

        /* access modifiers changed from: protected */
        public void constructSplit(int splitIdx, int[] configSplitIndices, int parentSplitIdx) throws PackageManager.NameNotFoundException {
            int[] iArr = configSplitIndices;
            ArrayList<String> splitPaths = new ArrayList<>();
            if (splitIdx == 0) {
                LoadedApk.this.createOrUpdateClassLoaderLocked(null);
                this.mCachedClassLoaders[0] = LoadedApk.this.mClassLoader;
                int length = iArr.length;
                for (int i = 0; i < length; i++) {
                    splitPaths.add(LoadedApk.this.mSplitResDirs[iArr[i] - 1]);
                }
                this.mCachedResourcePaths[0] = (String[]) splitPaths.toArray(new String[splitPaths.size()]);
                return;
            }
            ClassLoader parent = this.mCachedClassLoaders[parentSplitIdx];
            if (!LoadedApk.this.mApplicationInfo.isPlugin()) {
                this.mCachedClassLoaders[splitIdx] = ApplicationLoaders.getDefault().getClassLoader(LoadedApk.this.mSplitAppDirs[splitIdx - 1], LoadedApk.this.getTargetSdkVersion(), false, null, null, parent, LoadedApk.this.mSplitClassLoaderNames[splitIdx - 1]);
            } else {
                List<String> libPaths = new ArrayList<>(32);
                makeSplitLibPaths(LoadedApk.this.mSplitAppDirs[splitIdx - 1], LoadedApk.this.mApplicationInfo, libPaths);
                this.mCachedClassLoaders[splitIdx] = ApplicationLoaders.getDefault().getSplitClassLoader(LoadedApk.this.mSplitAppDirs[splitIdx - 1], LoadedApk.this.getTargetSdkVersion(), false, TextUtils.join(File.pathSeparator, libPaths), null, parent, LoadedApk.this.mSplitClassLoaderNames[splitIdx - 1]);
            }
            Collections.addAll(splitPaths, this.mCachedResourcePaths[parentSplitIdx]);
            splitPaths.add(LoadedApk.this.mSplitResDirs[splitIdx - 1]);
            int length2 = iArr.length;
            for (int i2 = 0; i2 < length2; i2++) {
                splitPaths.add(LoadedApk.this.mSplitResDirs[iArr[i2] - 1]);
            }
            this.mCachedResourcePaths[splitIdx] = (String[]) splitPaths.toArray(new String[splitPaths.size()]);
        }

        private int ensureSplitLoaded(String splitName) throws PackageManager.NameNotFoundException {
            int idx = 0;
            if (splitName != null) {
                int idx2 = Arrays.binarySearch(LoadedApk.this.mSplitNames, splitName);
                if (idx2 >= 0) {
                    idx = idx2 + 1;
                } else {
                    throw new PackageManager.NameNotFoundException("Split name '" + splitName + "' is not installed");
                }
            }
            loadDependenciesForSplit(idx);
            return idx;
        }

        /* access modifiers changed from: package-private */
        public ClassLoader getClassLoaderForSplit(String splitName) throws PackageManager.NameNotFoundException {
            return this.mCachedClassLoaders[ensureSplitLoaded(splitName)];
        }

        /* access modifiers changed from: package-private */
        public String[] getSplitPathsForSplit(String splitName) throws PackageManager.NameNotFoundException {
            return this.mCachedResourcePaths[ensureSplitLoaded(splitName)];
        }
    }

    private static class WarningContextClassLoader extends ClassLoader {
        private static boolean warned = false;

        private WarningContextClassLoader() {
        }

        private void warn(String methodName) {
            if (!warned) {
                warned = true;
                Thread.currentThread().setContextClassLoader(getParent());
                Slog.w(ActivityThread.TAG, "ClassLoader." + methodName + ": The class loader returned by Thread.getContextClassLoader() may fail for processes that host multiple applications. You should explicitly specify a context class loader. For example: Thread.setContextClassLoader(getClass().getClassLoader());");
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
        boolean z = false;
        if (System.getenv("MAPLE_RUNTIME") != null) {
            z = true;
        }
        sIsMygote = z;
    }

    /* access modifiers changed from: package-private */
    public Application getApplication() {
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
        this.mAppComponentFactory = createAppFactory(this.mApplicationInfo, this.mBaseClassLoader);
    }

    private static ApplicationInfo adjustNativeLibraryPaths(ApplicationInfo info) {
        if (!(info.primaryCpuAbi == null || info.secondaryCpuAbi == null)) {
            String runtimeIsa = VMRuntime.getRuntime().vmInstructionSet();
            String secondaryIsa = VMRuntime.getInstructionSet(info.secondaryCpuAbi);
            String secondaryDexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + secondaryIsa);
            if (runtimeIsa.equals(secondaryDexCodeIsa.isEmpty() ? secondaryIsa : secondaryDexCodeIsa)) {
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
        this.mSplitClassLoaderNames = null;
        this.mOverlayDirs = null;
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
        this.mAppComponentFactory = createAppFactory(this.mApplicationInfo, this.mClassLoader);
    }

    /* access modifiers changed from: package-private */
    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        this.mApplicationInfo = info;
        this.mClassLoader = classLoader;
        this.mAppComponentFactory = createAppFactory(info, classLoader);
    }

    private AppComponentFactory createAppFactory(ApplicationInfo appInfo, ClassLoader cl) {
        if (!(appInfo.appComponentFactory == null || cl == null)) {
            try {
                return (AppComponentFactory) cl.loadClass(appInfo.appComponentFactory).newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                Slog.e(TAG, "Unable to instantiate appComponentFactory", e);
            }
        }
        return AppComponentFactory.DEFAULT;
    }

    public AppComponentFactory getAppFactory() {
        return this.mAppComponentFactory;
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
        ApplicationInfo applicationInfo = aInfo;
        setApplicationInfo(aInfo);
        List<String> newPaths = new ArrayList<>();
        makePaths(this.mActivityThread, applicationInfo, newPaths);
        List<String> addedPaths = new ArrayList<>(newPaths.size());
        if (oldPaths != null) {
            for (String path : newPaths) {
                String apkName = path.substring(path.lastIndexOf(File.separator));
                boolean match = false;
                Iterator<String> it = oldPaths.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String oldPath = it.next();
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
                    ResourcesManager.getInstance().setHwThemeType(this.mResDir, this.mApplicationInfo.hwThemeType);
                    if (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().mDisplayId)) {
                        this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, null, getCompatibilityInfo(), getClassLoader());
                    } else {
                        this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, ActivityThread.currentActivityThread().mDisplayId, null, getCompatibilityInfo(), getClassLoader());
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    PackageManager.NameNotFoundException nameNotFoundException = e;
                    throw new AssertionError("null split not found");
                }
            }
        }
        this.mAppComponentFactory = createAppFactory(applicationInfo, this.mClassLoader);
    }

    private void setApplicationInfo(ApplicationInfo aInfo) {
        int myUid = Process.myUid();
        ApplicationInfo aInfo2 = adjustNativeLibraryPaths(aInfo);
        this.mApplicationInfo = aInfo2;
        this.mAppDir = aInfo2.sourceDir;
        this.mResDir = aInfo2.uid == myUid ? aInfo2.sourceDir : aInfo2.publicSourceDir;
        this.mOverlayDirs = aInfo2.resourceDirs;
        this.mDataDir = aInfo2.dataDir;
        this.mLibDir = aInfo2.nativeLibraryDir;
        this.mDataDirFile = FileUtils.newFileOrNull(aInfo2.dataDir);
        this.mDeviceProtectedDataDirFile = FileUtils.newFileOrNull(aInfo2.deviceProtectedDataDir);
        this.mCredentialProtectedDataDirFile = FileUtils.newFileOrNull(aInfo2.credentialProtectedDataDir);
        this.mSplitNames = aInfo2.splitNames;
        this.mSplitAppDirs = aInfo2.splitSourceDirs;
        this.mSplitResDirs = aInfo2.uid == myUid ? aInfo2.splitSourceDirs : aInfo2.splitPublicSourceDirs;
        this.mSplitClassLoaderNames = aInfo2.splitClassLoaderNames;
        if (aInfo2.requestsIsolatedSplitLoading() && !ArrayUtils.isEmpty(this.mSplitNames)) {
            this.mSplitLoader = new SplitDependencyLoaderImpl(aInfo2.splitDependencies);
        }
    }

    public static void makePaths(ActivityThread activityThread, ApplicationInfo aInfo, List<String> outZipPaths) {
        makePaths(activityThread, false, aInfo, outZipPaths, null);
    }

    public static void makePaths(ActivityThread activityThread, boolean isBundledApp, ApplicationInfo aInfo, List<String> outZipPaths, List<String> outLibPaths) {
        ActivityThread activityThread2 = activityThread;
        ApplicationInfo applicationInfo = aInfo;
        List<String> list = outZipPaths;
        List<String> list2 = outLibPaths;
        String appDir = applicationInfo.sourceDir;
        String libDir = applicationInfo.nativeLibraryDir;
        String[] sharedLibraries = applicationInfo.sharedLibraryFiles;
        outZipPaths.clear();
        list.add(appDir);
        if (applicationInfo.splitSourceDirs != null && !aInfo.requestsIsolatedSplitLoading()) {
            Collections.addAll(list, applicationInfo.splitSourceDirs);
        }
        if (list2 != null) {
            outLibPaths.clear();
        }
        String[] instrumentationLibs = null;
        if (activityThread2 != null) {
            String instrumentationPackageName = activityThread2.mInstrumentationPackageName;
            String instrumentationAppDir = activityThread2.mInstrumentationAppDir;
            String[] instrumentationSplitAppDirs = activityThread2.mInstrumentationSplitAppDirs;
            String instrumentationLibDir = activityThread2.mInstrumentationLibDir;
            String instrumentedAppDir = activityThread2.mInstrumentedAppDir;
            String[] instrumentedSplitAppDirs = activityThread2.mInstrumentedSplitAppDirs;
            String instrumentedLibDir = activityThread2.mInstrumentedLibDir;
            if (appDir.equals(instrumentationAppDir) || appDir.equals(instrumentedAppDir)) {
                outZipPaths.clear();
                list.add(instrumentationAppDir);
                if (!aInfo.requestsIsolatedSplitLoading()) {
                    if (instrumentationSplitAppDirs != null) {
                        Collections.addAll(list, instrumentationSplitAppDirs);
                    }
                    if (!instrumentationAppDir.equals(instrumentedAppDir)) {
                        list.add(instrumentedAppDir);
                        if (instrumentedSplitAppDirs != null) {
                            Collections.addAll(list, instrumentedSplitAppDirs);
                        }
                    }
                }
                if (list2 != null) {
                    list2.add(instrumentationLibDir);
                    if (!instrumentationLibDir.equals(instrumentedLibDir)) {
                        list2.add(instrumentedLibDir);
                    }
                }
                if (!instrumentedAppDir.equals(instrumentationAppDir)) {
                    instrumentationLibs = getLibrariesFor(instrumentationPackageName);
                }
            }
        }
        if (list2 != null) {
            if (outLibPaths.isEmpty()) {
                list2.add(libDir);
            }
            if (applicationInfo.primaryCpuAbi != null) {
                if (applicationInfo.targetSdkVersion < 24) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("/system/fake-libs");
                    sb.append(VMRuntime.is64BitAbi(applicationInfo.primaryCpuAbi) ? "64" : "");
                    list2.add(sb.toString());
                }
                Iterator<String> it = outZipPaths.iterator();
                while (it.hasNext()) {
                    list2.add(it.next() + "!/lib/" + applicationInfo.primaryCpuAbi);
                }
            }
            if (isBundledApp) {
                list2.add(System.getProperty("java.library.path"));
            }
        }
        if (!sIsMygote && sharedLibraries != null) {
            int index = 0;
            for (String lib : sharedLibraries) {
                if (!list.contains(lib)) {
                    list.add(index, lib);
                    index++;
                    appendApkLibPathIfNeeded(lib, applicationInfo, list2);
                }
            }
        }
        if (instrumentationLibs != null) {
            for (String lib2 : instrumentationLibs) {
                if (!list.contains(lib2)) {
                    list.add(0, lib2);
                    appendApkLibPathIfNeeded(lib2, applicationInfo, list2);
                }
            }
        }
    }

    private static void appendApkLibPathIfNeeded(String path, ApplicationInfo applicationInfo, List<String> outLibPaths) {
        if (outLibPaths != null && applicationInfo.primaryCpuAbi != null && path.endsWith(PackageParser.APK_FILE_EXTENSION) && applicationInfo.targetSdkVersion >= 26) {
            outLibPaths.add(path + "!/lib/" + applicationInfo.primaryCpuAbi);
        }
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getSplitClassLoader(String splitName) throws PackageManager.NameNotFoundException {
        if (this.mSplitLoader == null) {
            return this.mClassLoader;
        }
        return this.mSplitLoader.getClassLoaderForSplit(splitName);
    }

    /* access modifiers changed from: package-private */
    public String[] getSplitPaths(String splitName) throws PackageManager.NameNotFoundException {
        if (this.mSplitLoader == null) {
            return this.mSplitResDirs;
        }
        return this.mSplitLoader.getSplitPathsForSplit(splitName);
    }

    /* access modifiers changed from: private */
    public void createOrUpdateClassLoaderLocked(List<String> addedPaths) {
        String join;
        List<String> list = addedPaths;
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
            List<String> zipPaths = new ArrayList<>(10);
            List<String> libPaths = new ArrayList<>(10);
            boolean isBundledApp = (this.mApplicationInfo.isSystemApp() && !this.mApplicationInfo.isUpdatedSystemApp()) || (this.mApplicationInfo.hwFlags & 536870912) != 0;
            String defaultSearchPaths = System.getProperty("java.library.path");
            boolean treatVendorApkAsUnbundled = !defaultSearchPaths.contains("/vendor/lib");
            if (this.mApplicationInfo.getCodePath() != null && this.mApplicationInfo.isVendor() && treatVendorApkAsUnbundled) {
                isBundledApp = false;
            }
            boolean isBundledApp2 = isBundledApp;
            makePaths(this.mActivityThread, isBundledApp2, this.mApplicationInfo, zipPaths, libPaths);
            String libraryPermittedPath = this.mDataDir;
            if (isBundledApp2) {
                String libraryPermittedPath2 = libraryPermittedPath + File.pathSeparator + Paths.get(getAppDir(), new String[0]).getParent().toString();
                libraryPermittedPath = libraryPermittedPath2 + File.pathSeparator + defaultSearchPaths;
            }
            String libraryPermittedPath3 = libraryPermittedPath;
            String librarySearchPath = TextUtils.join(File.pathSeparator, libPaths);
            if (!this.mIncludeCode) {
                if (this.mClassLoader == null) {
                    StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
                    boolean z = isBundledApp2;
                    this.mClassLoader = ApplicationLoaders.getDefault().getClassLoader("", this.mApplicationInfo.targetSdkVersion, isBundledApp2, librarySearchPath, libraryPermittedPath3, this.mBaseClassLoader, null);
                    StrictMode.setThreadPolicy(oldPolicy);
                    this.mAppComponentFactory = AppComponentFactory.DEFAULT;
                }
                return;
            }
            boolean isBundledApp3 = isBundledApp2;
            if (zipPaths.size() == 1) {
                join = zipPaths.get(0);
            } else {
                join = TextUtils.join(File.pathSeparator, zipPaths);
            }
            String zip = join;
            boolean needToSetupJitProfiles = false;
            if (this.mClassLoader == null) {
                if (sIsMygote) {
                    updateSystemClassLoaderLocked();
                }
                StrictMode.ThreadPolicy oldPolicy2 = StrictMode.allowThreadDiskReads();
                this.mClassLoader = ApplicationLoaders.getDefault().getClassLoader(zip, this.mApplicationInfo.targetSdkVersion, isBundledApp3, librarySearchPath, libraryPermittedPath3, this.mBaseClassLoader, this.mApplicationInfo.classLoaderName);
                this.mAppComponentFactory = createAppFactory(this.mApplicationInfo, this.mClassLoader);
                StrictMode.setThreadPolicy(oldPolicy2);
                needToSetupJitProfiles = true;
            }
            boolean needToSetupJitProfiles2 = needToSetupJitProfiles;
            if (!libPaths.isEmpty() && SystemProperties.getBoolean(PROPERTY_NAME_APPEND_NATIVE, true)) {
                StrictMode.ThreadPolicy oldPolicy3 = StrictMode.allowThreadDiskReads();
                try {
                    ApplicationLoaders.getDefault().addNative(this.mClassLoader, libPaths);
                } finally {
                    StrictMode.setThreadPolicy(oldPolicy3);
                }
            }
            List<String> extraLibPaths = new ArrayList<>(3);
            String abiSuffix = VMRuntime.getRuntime().is64Bit() ? "64" : "";
            if (!defaultSearchPaths.contains("/vendor/lib")) {
                extraLibPaths.add("/vendor/lib" + abiSuffix);
            }
            if (!defaultSearchPaths.contains("/odm/lib")) {
                extraLibPaths.add("/odm/lib" + abiSuffix);
            }
            if (!defaultSearchPaths.contains("/product/lib")) {
                extraLibPaths.add("/product/lib" + abiSuffix);
            }
            if (!extraLibPaths.isEmpty()) {
                StrictMode.ThreadPolicy oldPolicy4 = StrictMode.allowThreadDiskReads();
                try {
                    ApplicationLoaders.getDefault().addNative(this.mClassLoader, extraLibPaths);
                } finally {
                    StrictMode.setThreadPolicy(oldPolicy4);
                }
            }
            if (list != null && addedPaths.size() > 0) {
                ApplicationLoaders.getDefault().addPath(this.mClassLoader, TextUtils.join(File.pathSeparator, list));
                needToSetupJitProfiles2 = true;
            }
            if (needToSetupJitProfiles2 && !ActivityThread.isSystem()) {
                setupJitProfileSupport();
            }
        } else if (this.mClassLoader == null) {
            if (this.mBaseClassLoader != null) {
                this.mClassLoader = this.mBaseClassLoader;
            } else {
                this.mClassLoader = ClassLoader.getSystemClassLoader();
            }
            this.mAppComponentFactory = createAppFactory(this.mApplicationInfo, this.mClassLoader);
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

    private void setupJitProfileSupport() {
        if (SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false) && this.mApplicationInfo.uid == Process.myUid()) {
            List<String> codePaths = new ArrayList<>();
            if ((this.mApplicationInfo.flags & 4) != 0) {
                codePaths.add(this.mApplicationInfo.sourceDir);
            }
            if (this.mApplicationInfo.splitSourceDirs != null) {
                Collections.addAll(codePaths, this.mApplicationInfo.splitSourceDirs);
            }
            if (!codePaths.isEmpty()) {
                int i = codePaths.size() - 1;
                while (i >= 0) {
                    VMRuntime.registerAppInfo(ArtManager.getCurrentProfilePath(this.mPackageName, UserHandle.myUserId(), i == 0 ? null : this.mApplicationInfo.splitNames[i - 1]), new String[]{codePaths.get(i)});
                    i--;
                }
                DexLoadReporter.getInstance().registerAppDataDir(this.mPackageName, this.mDataDir);
            }
        }
    }

    private void initializeJavaContextClassLoader() {
        ClassLoader contextClassLoader;
        try {
            PackageInfo pi = HwFrameworkFactory.getHwApiCacheManagerEx().getPackageInfoAsUser(ActivityThread.getPackageManager(), this.mPackageName, 268435456, UserHandle.myUserId());
            if (pi == null) {
                Slog.w(TAG, "Unable to get package info for " + this.mPackageName + "; is package not installed?");
                return;
            }
            boolean sharable = false;
            boolean sharedUserIdSet = pi.sharedUserId != null;
            boolean processNameNotDefault = pi.applicationInfo != null && !this.mPackageName.equals(pi.applicationInfo.processName);
            if (sharedUserIdSet || processNameNotDefault) {
                sharable = true;
            }
            if (sharable) {
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
                ResourcesManager.getInstance().setHwThemeType(this.mResDir, this.mApplicationInfo.hwThemeType);
                if (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().mDisplayId)) {
                    this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, null, getCompatibilityInfo(), getClassLoader());
                } else {
                    this.mResources = ResourcesManager.getInstance().getResources(null, this.mResDir, splitPaths, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, ActivityThread.currentActivityThread().mDisplayId, null, getCompatibilityInfo(), getClassLoader());
                }
            } catch (PackageManager.NameNotFoundException e) {
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
                    rewriteRValues(getClassLoader(), packageIdentifiers.valueAt(i), id);
                }
            }
        }
        Trace.traceEnd(64);
        return app;
    }

    private void rewriteRValues(ClassLoader cl, String packageName, int id) {
        try {
            Class<?> rClazz = cl.loadClass(packageName + ".R");
            try {
                Method callback = rClazz.getMethod("onResourcesLoaded", new Class[]{Integer.TYPE});
                try {
                    callback.invoke(null, new Object[]{Integer.valueOf(id)});
                } catch (IllegalAccessException e) {
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
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> rmap = this.mReceivers.remove(context);
            i = 0;
            if (rmap != null) {
                int i2 = 0;
                while (i2 < rmap.size()) {
                    ReceiverDispatcher rd = rmap.valueAt(i2);
                    IntentReceiverLeaked leak = new IntentReceiverLeaked(what + " " + who + " has leaked IntentReceiver " + rd.getIntentReceiver() + " that was originally registered here. Are you missing a call to unregisterReceiver()?");
                    leak.setStackTrace(rd.getLocation().getStackTrace());
                    Slog.e(ActivityThread.TAG, leak.getMessage(), leak);
                    if (reportRegistrationLeaks) {
                        StrictMode.onIntentReceiverLeaked(leak);
                    }
                    try {
                        ActivityManager.getService().unregisterReceiver(rd.getIIntentReceiver());
                        i2++;
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
            this.mUnregisteredReceivers.remove(context);
        }
        synchronized (this.mServices) {
            ArrayMap<ServiceConnection, ServiceDispatcher> smap = this.mServices.remove(context);
            if (smap != null) {
                while (i < smap.size()) {
                    ServiceDispatcher sd = smap.valueAt(i);
                    ServiceConnectionLeaked leak2 = new ServiceConnectionLeaked(what + " " + who + " has leaked ServiceConnection " + sd.getServiceConnection() + " that was originally bound here");
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

    public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r, Context context, Handler handler, Instrumentation instrumentation, boolean registered) {
        IIntentReceiver iIntentReceiver;
        synchronized (this.mReceivers) {
            ReceiverDispatcher rd = null;
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> map = null;
            if (registered) {
                try {
                    map = this.mReceivers.get(context);
                    if (map != null) {
                        rd = map.get(r);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (rd == null) {
                ReceiverDispatcher receiverDispatcher = new ReceiverDispatcher(r, context, handler, instrumentation, registered);
                rd = receiverDispatcher;
                if (registered) {
                    if (map == null) {
                        map = new ArrayMap<>();
                        this.mReceivers.put(context, map);
                    }
                    map.put(r, rd);
                }
            } else {
                rd.validate(context, handler);
            }
            rd.mForgotten = false;
            iIntentReceiver = rd.getIIntentReceiver();
        }
        return iIntentReceiver;
    }

    public IIntentReceiver forgetReceiverDispatcher(Context context, BroadcastReceiver r) {
        IIntentReceiver iIntentReceiver;
        synchronized (this.mReceivers) {
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> map = this.mReceivers.get(context);
            if (map != null) {
                ReceiverDispatcher rd = map.get(r);
                if (rd != null) {
                    map.remove(r);
                    if (map.size() == 0) {
                        this.mReceivers.remove(context);
                    }
                    if (r.getDebugUnregister()) {
                        ArrayMap<BroadcastReceiver, ReceiverDispatcher> holder = this.mUnregisteredReceivers.get(context);
                        if (holder == null) {
                            holder = new ArrayMap<>();
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
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> holder2 = this.mUnregisteredReceivers.get(context);
            if (holder2 != null) {
                ReceiverDispatcher rd2 = holder2.get(r);
                if (rd2 != null) {
                    RuntimeException ex2 = rd2.getUnregisterLocation();
                    throw new IllegalArgumentException("Unregistering Receiver " + r + " that was already unregistered", ex2);
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
        IServiceConnection iServiceConnection;
        synchronized (this.mServices) {
            ServiceDispatcher sd = null;
            ArrayMap<ServiceConnection, ServiceDispatcher> map = this.mServices.get(context);
            if (map != null) {
                sd = map.get(c);
            }
            if (sd == null) {
                sd = new ServiceDispatcher(c, context, handler, flags);
                if (map == null) {
                    map = new ArrayMap<>();
                    this.mServices.put(context, map);
                }
                map.put(c, sd);
            } else {
                sd.validate(context, handler);
            }
            iServiceConnection = sd.getIServiceConnection();
        }
        return iServiceConnection;
    }

    public final IServiceConnection forgetServiceDispatcher(Context context, ServiceConnection c) {
        IServiceConnection iServiceConnection;
        synchronized (this.mServices) {
            ArrayMap<ServiceConnection, ServiceDispatcher> map = this.mServices.get(context);
            if (map != null) {
                ServiceDispatcher sd = map.get(c);
                if (sd != null) {
                    map.remove(c);
                    sd.doForget();
                    if (map.size() == 0) {
                        this.mServices.remove(context);
                    }
                    if ((sd.getFlags() & 2) != 0) {
                        ArrayMap<ServiceConnection, ServiceDispatcher> holder = this.mUnboundServices.get(context);
                        if (holder == null) {
                            holder = new ArrayMap<>();
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
            ArrayMap<ServiceConnection, ServiceDispatcher> holder2 = this.mUnboundServices.get(context);
            if (holder2 != null) {
                ServiceDispatcher sd2 = holder2.get(c);
                if (sd2 != null) {
                    RuntimeException ex2 = sd2.getUnbindLocation();
                    throw new IllegalArgumentException("Unbinding Service " + c + " that was already unbound", ex2);
                }
            }
            if (context == null) {
                throw new IllegalStateException("Unbinding Service " + c + " from Context that is no longer in use: " + context);
            }
            throw new IllegalArgumentException("Service not registered: " + c);
        }
        return iServiceConnection;
    }

    private void updateSystemClassLoaderLocked() {
        String[] sharedLibraries = this.mApplicationInfo.sharedLibraryFiles;
        if (sharedLibraries != null && sharedLibraries.length > 0) {
            List<String> classPaths = new ArrayList<>(10);
            List<String> libsPaths = new ArrayList<>(10);
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            int index = 0;
            for (String lib : sharedLibraries) {
                if (!classPaths.contains(lib)) {
                    classPaths.add(index, lib);
                    index++;
                    appendApkLibPathIfNeeded(lib, this.mApplicationInfo, libsPaths);
                }
            }
            String classPathString = TextUtils.join(File.pathSeparator, classPaths);
            String join = TextUtils.join(File.pathSeparator, libsPaths);
            ApplicationLoaders.getDefault().addPath(systemClassLoader, classPathString);
        }
    }
}
