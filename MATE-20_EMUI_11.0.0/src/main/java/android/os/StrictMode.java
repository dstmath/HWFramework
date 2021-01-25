package android.os;

import android.animation.ValueAnimator;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.INetworkManagementService;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.storage.IStorageManager;
import android.os.strictmode.CleartextNetworkViolation;
import android.os.strictmode.ContentUriWithoutPermissionViolation;
import android.os.strictmode.CredentialProtectedWhileLockedViolation;
import android.os.strictmode.CustomViolation;
import android.os.strictmode.DiskReadViolation;
import android.os.strictmode.DiskWriteViolation;
import android.os.strictmode.ExplicitGcViolation;
import android.os.strictmode.FileUriExposedViolation;
import android.os.strictmode.ImplicitDirectBootViolation;
import android.os.strictmode.InstanceCountViolation;
import android.os.strictmode.IntentReceiverLeakedViolation;
import android.os.strictmode.LeakedClosableViolation;
import android.os.strictmode.NetworkViolation;
import android.os.strictmode.ResourceMismatchViolation;
import android.os.strictmode.ServiceConnectionLeakedViolation;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.os.strictmode.UnbufferedIoViolation;
import android.os.strictmode.UntaggedSocketViolation;
import android.os.strictmode.Violation;
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Printer;
import android.util.Singleton;
import android.view.IWindowManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.HexDump;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class StrictMode {
    private static final String CLEARTEXT_PROPERTY = "persist.sys.strictmode.clear";
    private static final int DETECT_THREAD_ALL = 65535;
    private static final int DETECT_THREAD_CUSTOM = 8;
    private static final int DETECT_THREAD_DISK_READ = 2;
    private static final int DETECT_THREAD_DISK_WRITE = 1;
    private static final int DETECT_THREAD_EXPLICIT_GC = 64;
    private static final int DETECT_THREAD_NETWORK = 4;
    private static final int DETECT_THREAD_RESOURCE_MISMATCH = 16;
    private static final int DETECT_THREAD_UNBUFFERED_IO = 32;
    private static final int DETECT_VM_ACTIVITY_LEAKS = 4;
    private static final int DETECT_VM_ALL = 65535;
    private static final int DETECT_VM_CLEARTEXT_NETWORK = 64;
    private static final int DETECT_VM_CLOSABLE_LEAKS = 2;
    private static final int DETECT_VM_CONTENT_URI_WITHOUT_PERMISSION = 128;
    private static final int DETECT_VM_CREDENTIAL_PROTECTED_WHILE_LOCKED = 2048;
    private static final int DETECT_VM_CURSOR_LEAKS = 1;
    private static final int DETECT_VM_FILE_URI_EXPOSURE = 32;
    private static final int DETECT_VM_IMPLICIT_DIRECT_BOOT = 1024;
    private static final int DETECT_VM_INSTANCE_LEAKS = 8;
    private static final int DETECT_VM_NON_SDK_API_USAGE = 512;
    private static final int DETECT_VM_REGISTRATION_LEAKS = 16;
    private static final int DETECT_VM_UNTAGGED_SOCKET = 256;
    private static final boolean DISABLE = false;
    public static final String DISABLE_PROPERTY = "persist.sys.strictmode.disable";
    private static final HashMap<Class, Integer> EMPTY_CLASS_LIMIT_MAP = new HashMap<>();
    private static final ViolationLogger LOGCAT_LOGGER = $$Lambda$StrictMode$1yH8AK0bTwVwZOb9x8HoiSBdzr0.INSTANCE;
    private static final boolean LOG_V = Log.isLoggable(TAG, 2);
    private static final int MAX_OFFENSES_PER_LOOP = 10;
    private static final int MAX_SPAN_TAGS = 20;
    private static final long MIN_DIALOG_INTERVAL_MS = 30000;
    private static final long MIN_LOG_INTERVAL_MS = 1000;
    private static final long MIN_VM_INTERVAL_MS = 1000;
    public static final int NETWORK_POLICY_ACCEPT = 0;
    public static final int NETWORK_POLICY_LOG = 1;
    public static final int NETWORK_POLICY_REJECT = 2;
    private static final Span NO_OP_SPAN = new Span() {
        /* class android.os.StrictMode.AnonymousClass7 */

        @Override // android.os.StrictMode.Span
        public void finish() {
        }
    };
    public static final int PENALTY_ALL = -65536;
    public static final int PENALTY_DEATH = 268435456;
    public static final int PENALTY_DEATH_ON_CLEARTEXT_NETWORK = 16777216;
    public static final int PENALTY_DEATH_ON_FILE_URI_EXPOSURE = 8388608;
    public static final int PENALTY_DEATH_ON_NETWORK = 33554432;
    public static final int PENALTY_DIALOG = 536870912;
    public static final int PENALTY_DROPBOX = 67108864;
    public static final int PENALTY_FLASH = 134217728;
    public static final int PENALTY_GATHER = Integer.MIN_VALUE;
    public static final int PENALTY_LOG = 1073741824;
    private static final String TAG = "StrictMode";
    private static final ThreadLocal<AndroidBlockGuardPolicy> THREAD_ANDROID_POLICY = new ThreadLocal<AndroidBlockGuardPolicy>() {
        /* class android.os.StrictMode.AnonymousClass4 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public AndroidBlockGuardPolicy initialValue() {
            return new AndroidBlockGuardPolicy(0);
        }
    };
    private static final ThreadLocal<Handler> THREAD_HANDLER = new ThreadLocal<Handler>() {
        /* class android.os.StrictMode.AnonymousClass3 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Handler initialValue() {
            return new Handler();
        }
    };
    public static final String VISUAL_PROPERTY = "persist.sys.strictmode.visual";
    private static final BlockGuard.VmPolicy VM_ANDROID_POLICY = new BlockGuard.VmPolicy() {
        /* class android.os.StrictMode.AnonymousClass5 */

        public void onPathAccess(String path) {
            if (path != null) {
                if (path.startsWith("/data/user/") || path.startsWith("/data/media/") || path.startsWith("/data/system_ce/") || path.startsWith("/data/misc_ce/") || path.startsWith("/data/vendor_ce/") || path.startsWith("/storage/emulated/")) {
                    int third = path.indexOf(47, path.indexOf(47, 1) + 1);
                    int fourth = path.indexOf(47, third + 1);
                    if (fourth != -1) {
                        try {
                            StrictMode.onCredentialProtectedPathAccess(path, Integer.parseInt(path.substring(third + 1, fourth)));
                        } catch (NumberFormatException e) {
                        }
                    }
                } else if (path.startsWith("/data/data/")) {
                    StrictMode.onCredentialProtectedPathAccess(path, 0);
                }
            }
        }
    };
    private static final ThreadLocal<ArrayList<ViolationInfo>> gatheredViolations = new ThreadLocal<ArrayList<ViolationInfo>>() {
        /* class android.os.StrictMode.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public ArrayList<ViolationInfo> initialValue() {
            return null;
        }
    };
    private static final AtomicInteger sDropboxCallsInFlight = new AtomicInteger(0);
    @GuardedBy({"StrictMode.class"})
    private static final HashMap<Class, Integer> sExpectedActivityInstanceCount = new HashMap<>();
    private static boolean sIsIdlerRegistered = false;
    private static long sLastInstanceCountCheckMillis = 0;
    @UnsupportedAppUsage
    private static final HashMap<Integer, Long> sLastVmViolationTime = new HashMap<>();
    private static volatile ViolationLogger sLogger = LOGCAT_LOGGER;
    private static final Consumer<String> sNonSdkApiUsageConsumer = $$Lambda$StrictMode$lu9ekkHJ2HMz0jd3F8K8MnhenxQ.INSTANCE;
    private static final MessageQueue.IdleHandler sProcessIdleHandler = new MessageQueue.IdleHandler() {
        /* class android.os.StrictMode.AnonymousClass6 */

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            long now = SystemClock.uptimeMillis();
            if (now - StrictMode.sLastInstanceCountCheckMillis <= 30000) {
                return true;
            }
            long unused = StrictMode.sLastInstanceCountCheckMillis = now;
            StrictMode.conditionallyCheckInstanceCounts();
            return true;
        }
    };
    private static final ThreadLocal<ThreadSpanState> sThisThreadSpanState = new ThreadLocal<ThreadSpanState>() {
        /* class android.os.StrictMode.AnonymousClass8 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public ThreadSpanState initialValue() {
            return new ThreadSpanState();
        }
    };
    private static final ThreadLocal<Executor> sThreadViolationExecutor = new ThreadLocal<>();
    private static final ThreadLocal<OnThreadViolationListener> sThreadViolationListener = new ThreadLocal<>();
    private static volatile boolean sUserKeyUnlocked = false;
    private static volatile VmPolicy sVmPolicy = VmPolicy.LAX;
    @UnsupportedAppUsage
    private static Singleton<IWindowManager> sWindowManager = new Singleton<IWindowManager>() {
        /* class android.os.StrictMode.AnonymousClass9 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IWindowManager create() {
            return IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        }
    };
    @UnsupportedAppUsage
    private static final ThreadLocal<ArrayList<ViolationInfo>> violationsBeingTimed = new ThreadLocal<ArrayList<ViolationInfo>>() {
        /* class android.os.StrictMode.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public ArrayList<ViolationInfo> initialValue() {
            return new ArrayList<>();
        }
    };

    public interface OnThreadViolationListener {
        void onThreadViolation(Violation violation);
    }

    public interface OnVmViolationListener {
        void onVmViolation(Violation violation);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ThreadPolicyMask {
    }

    public interface ViolationLogger {
        void log(ViolationInfo violationInfo);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VmPolicyMask {
    }

    static /* synthetic */ void lambda$static$0(ViolationInfo info) {
        String msg;
        if (info.durationMillis != -1) {
            msg = "StrictMode policy violation; ~duration=" + info.durationMillis + " ms:";
        } else {
            msg = "StrictMode policy violation:";
        }
        Log.d(TAG, msg + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + info.getStackTrace());
    }

    public static void setViolationLogger(ViolationLogger listener) {
        if (listener == null) {
            listener = LOGCAT_LOGGER;
        }
        sLogger = listener;
    }

    private StrictMode() {
    }

    public static final class ThreadPolicy {
        public static final ThreadPolicy LAX = new ThreadPolicy(0, null, null);
        final Executor mCallbackExecutor;
        final OnThreadViolationListener mListener;
        @UnsupportedAppUsage
        final int mask;

        private ThreadPolicy(int mask2, OnThreadViolationListener listener, Executor executor) {
            this.mask = mask2;
            this.mListener = listener;
            this.mCallbackExecutor = executor;
        }

        public String toString() {
            return "[StrictMode.ThreadPolicy; mask=" + this.mask + "]";
        }

        public static final class Builder {
            private Executor mExecutor;
            private OnThreadViolationListener mListener;
            private int mMask;

            public Builder() {
                this.mMask = 0;
                this.mMask = 0;
            }

            public Builder(ThreadPolicy policy) {
                this.mMask = 0;
                this.mMask = policy.mask;
                this.mListener = policy.mListener;
                this.mExecutor = policy.mCallbackExecutor;
            }

            public Builder detectAll() {
                detectDiskReads();
                detectDiskWrites();
                detectNetwork();
                int targetSdk = VMRuntime.getRuntime().getTargetSdkVersion();
                if (targetSdk >= 11) {
                    detectCustomSlowCalls();
                }
                if (targetSdk >= 23) {
                    detectResourceMismatches();
                }
                if (targetSdk >= 26) {
                    detectUnbufferedIo();
                }
                return this;
            }

            public Builder permitAll() {
                return disable(65535);
            }

            public Builder detectNetwork() {
                return enable(4);
            }

            public Builder permitNetwork() {
                return disable(4);
            }

            public Builder detectDiskReads() {
                return enable(2);
            }

            public Builder permitDiskReads() {
                return disable(2);
            }

            public Builder detectCustomSlowCalls() {
                return enable(8);
            }

            public Builder permitCustomSlowCalls() {
                return disable(8);
            }

            public Builder permitResourceMismatches() {
                return disable(16);
            }

            public Builder detectUnbufferedIo() {
                return enable(32);
            }

            public Builder permitUnbufferedIo() {
                return disable(32);
            }

            public Builder detectResourceMismatches() {
                return enable(16);
            }

            public Builder detectDiskWrites() {
                return enable(1);
            }

            public Builder permitDiskWrites() {
                return disable(1);
            }

            public Builder detectExplicitGc() {
                return enable(64);
            }

            public Builder permitExplicitGc() {
                return disable(64);
            }

            public Builder penaltyDialog() {
                return enable(536870912);
            }

            public Builder penaltyDeath() {
                return enable(268435456);
            }

            public Builder penaltyDeathOnNetwork() {
                return enable(33554432);
            }

            public Builder penaltyFlashScreen() {
                return enable(134217728);
            }

            public Builder penaltyLog() {
                return enable(1073741824);
            }

            public Builder penaltyDropBox() {
                return enable(67108864);
            }

            public Builder penaltyListener(Executor executor, OnThreadViolationListener listener) {
                if (executor != null) {
                    this.mListener = listener;
                    this.mExecutor = executor;
                    return this;
                }
                throw new NullPointerException("executor must not be null");
            }

            public Builder penaltyListener(OnThreadViolationListener listener, Executor executor) {
                return penaltyListener(executor, listener);
            }

            private Builder enable(int mask) {
                this.mMask |= mask;
                return this;
            }

            private Builder disable(int mask) {
                this.mMask &= ~mask;
                return this;
            }

            public ThreadPolicy build() {
                int i;
                if (this.mListener == null && (i = this.mMask) != 0 && (i & 1946157056) == 0) {
                    penaltyLog();
                }
                return new ThreadPolicy(this.mMask, this.mListener, this.mExecutor);
            }
        }
    }

    public static final class VmPolicy {
        public static final VmPolicy LAX = new VmPolicy(0, StrictMode.EMPTY_CLASS_LIMIT_MAP, null, null);
        final HashMap<Class, Integer> classInstanceLimit;
        final Executor mCallbackExecutor;
        final OnVmViolationListener mListener;
        @UnsupportedAppUsage
        final int mask;

        private VmPolicy(int mask2, HashMap<Class, Integer> classInstanceLimit2, OnVmViolationListener listener, Executor executor) {
            if (classInstanceLimit2 != null) {
                this.mask = mask2;
                this.classInstanceLimit = classInstanceLimit2;
                this.mListener = listener;
                this.mCallbackExecutor = executor;
                return;
            }
            throw new NullPointerException("classInstanceLimit == null");
        }

        public String toString() {
            return "[StrictMode.VmPolicy; mask=" + this.mask + "]";
        }

        public static final class Builder {
            private HashMap<Class, Integer> mClassInstanceLimit;
            private boolean mClassInstanceLimitNeedCow;
            private Executor mExecutor;
            private OnVmViolationListener mListener;
            @UnsupportedAppUsage
            private int mMask;

            public Builder() {
                this.mClassInstanceLimitNeedCow = false;
                this.mMask = 0;
            }

            public Builder(VmPolicy base) {
                this.mClassInstanceLimitNeedCow = false;
                this.mMask = base.mask;
                this.mClassInstanceLimitNeedCow = true;
                this.mClassInstanceLimit = base.classInstanceLimit;
                this.mListener = base.mListener;
                this.mExecutor = base.mCallbackExecutor;
            }

            public Builder setClassInstanceLimit(Class klass, int instanceLimit) {
                if (klass != null) {
                    if (this.mClassInstanceLimitNeedCow) {
                        if (this.mClassInstanceLimit.containsKey(klass) && this.mClassInstanceLimit.get(klass).intValue() == instanceLimit) {
                            return this;
                        }
                        this.mClassInstanceLimitNeedCow = false;
                        this.mClassInstanceLimit = (HashMap) this.mClassInstanceLimit.clone();
                    } else if (this.mClassInstanceLimit == null) {
                        this.mClassInstanceLimit = new HashMap<>();
                    }
                    this.mMask |= 8;
                    this.mClassInstanceLimit.put(klass, Integer.valueOf(instanceLimit));
                    return this;
                }
                throw new NullPointerException("klass == null");
            }

            public Builder detectActivityLeaks() {
                return enable(4);
            }

            public Builder permitActivityLeaks() {
                return disable(4);
            }

            public Builder detectNonSdkApiUsage() {
                return enable(512);
            }

            public Builder permitNonSdkApiUsage() {
                return disable(512);
            }

            public Builder detectAll() {
                detectLeakedSqlLiteObjects();
                int targetSdk = VMRuntime.getRuntime().getTargetSdkVersion();
                if (targetSdk >= 11) {
                    detectActivityLeaks();
                    detectLeakedClosableObjects();
                }
                if (targetSdk >= 16) {
                    detectLeakedRegistrationObjects();
                }
                if (targetSdk >= 18) {
                    detectFileUriExposure();
                }
                if (targetSdk >= 23 && SystemProperties.getBoolean(StrictMode.CLEARTEXT_PROPERTY, false)) {
                    detectCleartextNetwork();
                }
                if (targetSdk >= 26) {
                    detectContentUriWithoutPermission();
                    detectUntaggedSockets();
                }
                if (targetSdk >= 29) {
                    detectCredentialProtectedWhileLocked();
                }
                return this;
            }

            public Builder detectLeakedSqlLiteObjects() {
                return enable(1);
            }

            public Builder detectLeakedClosableObjects() {
                return enable(2);
            }

            public Builder detectLeakedRegistrationObjects() {
                return enable(16);
            }

            public Builder detectFileUriExposure() {
                return enable(32);
            }

            public Builder detectCleartextNetwork() {
                return enable(64);
            }

            public Builder detectContentUriWithoutPermission() {
                return enable(128);
            }

            public Builder detectUntaggedSockets() {
                return enable(256);
            }

            public Builder permitUntaggedSockets() {
                return disable(256);
            }

            public Builder detectImplicitDirectBoot() {
                return enable(1024);
            }

            public Builder permitImplicitDirectBoot() {
                return disable(1024);
            }

            public Builder detectCredentialProtectedWhileLocked() {
                return enable(2048);
            }

            public Builder permitCredentialProtectedWhileLocked() {
                return disable(2048);
            }

            public Builder penaltyDeath() {
                return enable(268435456);
            }

            public Builder penaltyDeathOnCleartextNetwork() {
                return enable(16777216);
            }

            public Builder penaltyDeathOnFileUriExposure() {
                return enable(8388608);
            }

            public Builder penaltyLog() {
                return enable(1073741824);
            }

            public Builder penaltyDropBox() {
                return enable(67108864);
            }

            public Builder penaltyListener(Executor executor, OnVmViolationListener listener) {
                if (executor != null) {
                    this.mListener = listener;
                    this.mExecutor = executor;
                    return this;
                }
                throw new NullPointerException("executor must not be null");
            }

            public Builder penaltyListener(OnVmViolationListener listener, Executor executor) {
                return penaltyListener(executor, listener);
            }

            private Builder enable(int mask) {
                this.mMask |= mask;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder disable(int mask) {
                this.mMask &= ~mask;
                return this;
            }

            public VmPolicy build() {
                int i;
                if (this.mListener == null && (i = this.mMask) != 0 && (i & 1946157056) == 0) {
                    penaltyLog();
                }
                int i2 = this.mMask;
                HashMap<Class, Integer> hashMap = this.mClassInstanceLimit;
                if (hashMap == null) {
                    hashMap = StrictMode.EMPTY_CLASS_LIMIT_MAP;
                }
                return new VmPolicy(i2, hashMap, this.mListener, this.mExecutor);
            }
        }
    }

    public static void setThreadPolicy(ThreadPolicy policy) {
        setThreadPolicyMask(policy.mask);
        sThreadViolationListener.set(policy.mListener);
        sThreadViolationExecutor.set(policy.mCallbackExecutor);
    }

    public static void setThreadPolicyMask(int threadPolicyMask) {
        setBlockGuardPolicy(threadPolicyMask);
        Binder.setThreadStrictModePolicy(threadPolicyMask);
    }

    private static void setBlockGuardPolicy(int threadPolicyMask) {
        AndroidBlockGuardPolicy androidPolicy;
        if (threadPolicyMask == 0) {
            BlockGuard.setThreadPolicy(BlockGuard.LAX_POLICY);
            return;
        }
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            androidPolicy = (AndroidBlockGuardPolicy) policy;
        } else {
            androidPolicy = THREAD_ANDROID_POLICY.get();
            BlockGuard.setThreadPolicy(androidPolicy);
        }
        androidPolicy.setThreadPolicyMask(threadPolicyMask);
    }

    private static void setBlockGuardVmPolicy(int vmPolicyMask) {
        if ((vmPolicyMask & 2048) != 0) {
            BlockGuard.setVmPolicy(VM_ANDROID_POLICY);
        } else {
            BlockGuard.setVmPolicy(BlockGuard.LAX_VM_POLICY);
        }
    }

    private static void setCloseGuardEnabled(boolean enabled) {
        if (!(CloseGuard.getReporter() instanceof AndroidCloseGuardReporter)) {
            CloseGuard.setReporter(new AndroidCloseGuardReporter());
        }
        CloseGuard.setEnabled(enabled);
    }

    @UnsupportedAppUsage
    public static int getThreadPolicyMask() {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            return ((AndroidBlockGuardPolicy) policy).getThreadPolicyMask();
        }
        return 0;
    }

    public static ThreadPolicy getThreadPolicy() {
        return new ThreadPolicy(getThreadPolicyMask(), sThreadViolationListener.get(), sThreadViolationExecutor.get());
    }

    public static ThreadPolicy allowThreadDiskWrites() {
        return new ThreadPolicy(allowThreadDiskWritesMask(), sThreadViolationListener.get(), sThreadViolationExecutor.get());
    }

    public static int allowThreadDiskWritesMask() {
        int oldPolicyMask = getThreadPolicyMask();
        int newPolicyMask = oldPolicyMask & -4;
        if (newPolicyMask != oldPolicyMask) {
            setThreadPolicyMask(newPolicyMask);
        }
        return oldPolicyMask;
    }

    public static ThreadPolicy allowThreadDiskReads() {
        return new ThreadPolicy(allowThreadDiskReadsMask(), sThreadViolationListener.get(), sThreadViolationExecutor.get());
    }

    public static int allowThreadDiskReadsMask() {
        int oldPolicyMask = getThreadPolicyMask();
        int newPolicyMask = oldPolicyMask & -3;
        if (newPolicyMask != oldPolicyMask) {
            setThreadPolicyMask(newPolicyMask);
        }
        return oldPolicyMask;
    }

    public static ThreadPolicy allowThreadViolations() {
        ThreadPolicy oldPolicy = getThreadPolicy();
        setThreadPolicyMask(0);
        return oldPolicy;
    }

    public static VmPolicy allowVmViolations() {
        VmPolicy oldPolicy = getVmPolicy();
        sVmPolicy = VmPolicy.LAX;
        return oldPolicy;
    }

    public static boolean isBundledSystemApp(ApplicationInfo ai) {
        if (ai == null || ai.packageName == null) {
            return true;
        }
        if (!ai.isSystemApp() || ai.packageName.equals("com.android.vending") || ai.packageName.equals("com.android.chrome") || ai.packageName.equals("com.android.phone")) {
            return false;
        }
        if (ai.packageName.equals("android") || ai.packageName.startsWith("android.") || ai.packageName.startsWith("com.android.")) {
            return true;
        }
        return false;
    }

    public static void initThreadDefaults(ApplicationInfo ai) {
        ThreadPolicy.Builder builder = new ThreadPolicy.Builder();
        if ((ai != null ? ai.targetSdkVersion : 10000) >= 11) {
            builder.detectNetwork();
            builder.penaltyDeathOnNetwork();
        }
        if (!Build.IS_USER && !SystemProperties.getBoolean(DISABLE_PROPERTY, false)) {
            if (Build.IS_USERDEBUG) {
                if (isBundledSystemApp(ai)) {
                    builder.detectAll();
                    builder.penaltyDropBox();
                    if (SystemProperties.getBoolean(VISUAL_PROPERTY, false)) {
                        builder.penaltyFlashScreen();
                    }
                }
            } else if (Build.IS_ENG && isBundledSystemApp(ai)) {
                builder.detectAll();
                builder.penaltyDropBox();
                builder.penaltyLog();
                builder.penaltyFlashScreen();
            }
        }
        setThreadPolicy(builder.build());
    }

    public static void initVmDefaults(ApplicationInfo ai) {
        VmPolicy.Builder builder = new VmPolicy.Builder();
        if ((ai != null ? ai.targetSdkVersion : 10000) >= 24) {
            builder.detectFileUriExposure();
            builder.penaltyDeathOnFileUriExposure();
        }
        if (!Build.IS_USER && !SystemProperties.getBoolean(DISABLE_PROPERTY, false)) {
            if (Build.IS_USERDEBUG) {
                if (isBundledSystemApp(ai)) {
                    builder.detectAll();
                    builder.permitActivityLeaks();
                    builder.penaltyDropBox();
                }
            } else if (Build.IS_ENG && isBundledSystemApp(ai)) {
                builder.detectAll();
                builder.penaltyDropBox();
                builder.penaltyLog();
            }
        }
        setVmPolicy(builder.build());
    }

    @UnsupportedAppUsage
    public static void enableDeathOnFileUriExposure() {
        sVmPolicy = new VmPolicy(8388608 | sVmPolicy.mask | 32, sVmPolicy.classInstanceLimit, sVmPolicy.mListener, sVmPolicy.mCallbackExecutor);
    }

    @UnsupportedAppUsage
    public static void disableDeathOnFileUriExposure() {
        sVmPolicy = new VmPolicy(-8388641 & sVmPolicy.mask, sVmPolicy.classInstanceLimit, sVmPolicy.mListener, sVmPolicy.mCallbackExecutor);
    }

    /* access modifiers changed from: private */
    public static boolean tooManyViolationsThisLoop() {
        return violationsBeingTimed.get().size() >= 10;
    }

    /* access modifiers changed from: private */
    public static class AndroidBlockGuardPolicy implements BlockGuard.Policy {
        private ArrayMap<Integer, Long> mLastViolationTime;
        private int mThreadPolicyMask;

        public AndroidBlockGuardPolicy(int threadPolicyMask) {
            this.mThreadPolicyMask = threadPolicyMask;
        }

        public String toString() {
            return "AndroidBlockGuardPolicy; mPolicyMask=" + this.mThreadPolicyMask;
        }

        public int getPolicyMask() {
            return this.mThreadPolicyMask;
        }

        public void onWriteToDisk() {
            if ((this.mThreadPolicyMask & 1) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                startHandlingViolationException(new DiskWriteViolation());
            }
        }

        /* access modifiers changed from: package-private */
        public void onCustomSlowCall(String name) {
            if ((this.mThreadPolicyMask & 8) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                startHandlingViolationException(new CustomViolation(name));
            }
        }

        /* access modifiers changed from: package-private */
        public void onResourceMismatch(Object tag) {
            if ((this.mThreadPolicyMask & 16) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                startHandlingViolationException(new ResourceMismatchViolation(tag));
            }
        }

        public void onUnbufferedIO() {
            if ((this.mThreadPolicyMask & 32) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                startHandlingViolationException(new UnbufferedIoViolation());
            }
        }

        public void onReadFromDisk() {
            if ((this.mThreadPolicyMask & 2) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                startHandlingViolationException(new DiskReadViolation());
            }
        }

        public void onNetwork() {
            int i = this.mThreadPolicyMask;
            if ((i & 4) != 0) {
                if ((i & 33554432) != 0) {
                    throw new NetworkOnMainThreadException();
                } else if (!StrictMode.tooManyViolationsThisLoop()) {
                    startHandlingViolationException(new NetworkViolation());
                }
            }
        }

        public void onExplicitGc() {
            if ((this.mThreadPolicyMask & 64) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                startHandlingViolationException(new ExplicitGcViolation());
            }
        }

        public int getThreadPolicyMask() {
            return this.mThreadPolicyMask;
        }

        public void setThreadPolicyMask(int threadPolicyMask) {
            this.mThreadPolicyMask = threadPolicyMask;
        }

        /* access modifiers changed from: package-private */
        public void startHandlingViolationException(Violation e) {
            ViolationInfo info = new ViolationInfo(e, this.mThreadPolicyMask & -65536);
            info.violationUptimeMillis = SystemClock.uptimeMillis();
            handleViolationWithTimingAttempt(info);
        }

        /* access modifiers changed from: package-private */
        public void handleViolationWithTimingAttempt(ViolationInfo info) {
            if (Looper.myLooper() == null || info.mPenaltyMask == 268435456) {
                info.durationMillis = -1;
                onThreadPolicyViolation(info);
                return;
            }
            ArrayList<ViolationInfo> records = (ArrayList) StrictMode.violationsBeingTimed.get();
            if (records.size() < 10) {
                records.add(info);
                if (records.size() <= 1) {
                    IWindowManager windowManager = info.penaltyEnabled(134217728) ? (IWindowManager) StrictMode.sWindowManager.get() : null;
                    if (windowManager != null) {
                        try {
                            windowManager.showStrictModeViolation(true);
                        } catch (RemoteException e) {
                        }
                    }
                    ((Handler) StrictMode.THREAD_HANDLER.get()).postAtFrontOfQueue(new Runnable(windowManager, records) {
                        /* class android.os.$$Lambda$StrictMode$AndroidBlockGuardPolicy$9nBulCQKaMajrWr41SB7f7YRT1I */
                        private final /* synthetic */ IWindowManager f$1;
                        private final /* synthetic */ ArrayList f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            StrictMode.AndroidBlockGuardPolicy.this.lambda$handleViolationWithTimingAttempt$0$StrictMode$AndroidBlockGuardPolicy(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$handleViolationWithTimingAttempt$0$StrictMode$AndroidBlockGuardPolicy(IWindowManager windowManager, ArrayList records) {
            long loopFinishTime = SystemClock.uptimeMillis();
            if (windowManager != null) {
                try {
                    windowManager.showStrictModeViolation(false);
                } catch (RemoteException e) {
                }
            }
            for (int n = 0; n < records.size(); n++) {
                ViolationInfo v = (ViolationInfo) records.get(n);
                v.violationNumThisLoop = n + 1;
                v.durationMillis = (int) (loopFinishTime - v.violationUptimeMillis);
                onThreadPolicyViolation(v);
            }
            records.clear();
        }

        /* access modifiers changed from: package-private */
        public void onThreadPolicyViolation(ViolationInfo info) {
            int penaltyMask;
            if (StrictMode.LOG_V) {
                Log.d(StrictMode.TAG, "onThreadPolicyViolation; penalty=" + info.mPenaltyMask);
            }
            boolean justDropBox = true;
            if (info.penaltyEnabled(Integer.MIN_VALUE)) {
                ArrayList<ViolationInfo> violations = (ArrayList) StrictMode.gatheredViolations.get();
                if (violations == null) {
                    violations = new ArrayList<>(1);
                    StrictMode.gatheredViolations.set(violations);
                }
                Iterator<ViolationInfo> it = violations.iterator();
                while (it.hasNext()) {
                    if (info.getStackTrace().equals(it.next().getStackTrace())) {
                        return;
                    }
                }
                violations.add(info);
                return;
            }
            Integer crashFingerprint = Integer.valueOf(info.hashCode());
            long lastViolationTime = 0;
            ArrayMap<Integer, Long> arrayMap = this.mLastViolationTime;
            if (arrayMap != null) {
                Long vtime = arrayMap.get(crashFingerprint);
                if (vtime != null) {
                    lastViolationTime = vtime.longValue();
                }
            } else {
                this.mLastViolationTime = new ArrayMap<>(1);
            }
            long now = SystemClock.uptimeMillis();
            this.mLastViolationTime.put(crashFingerprint, Long.valueOf(now));
            long timeSinceLastViolationMillis = lastViolationTime == 0 ? Long.MAX_VALUE : now - lastViolationTime;
            if (info.penaltyEnabled(1073741824) && timeSinceLastViolationMillis > 1000) {
                StrictMode.sLogger.log(info);
            }
            Violation violation = info.mViolation;
            int penaltyMask2 = 0;
            if (info.penaltyEnabled(536870912) && timeSinceLastViolationMillis > 30000) {
                penaltyMask2 = 0 | 536870912;
            }
            if (!info.penaltyEnabled(67108864) || lastViolationTime != 0) {
                penaltyMask = penaltyMask2;
            } else {
                penaltyMask = penaltyMask2 | 67108864;
            }
            if (penaltyMask != 0) {
                if (info.mPenaltyMask != 67108864) {
                    justDropBox = false;
                }
                if (justDropBox) {
                    StrictMode.dropboxViolationAsync(penaltyMask, info);
                } else {
                    StrictMode.handleApplicationStrictModeViolation(penaltyMask, info);
                }
            }
            if (!info.penaltyEnabled(268435456)) {
                OnThreadViolationListener listener = (OnThreadViolationListener) StrictMode.sThreadViolationListener.get();
                Executor executor = (Executor) StrictMode.sThreadViolationExecutor.get();
                if (listener != null && executor != null) {
                    try {
                        executor.execute(new Runnable(violation) {
                            /* class android.os.$$Lambda$StrictMode$AndroidBlockGuardPolicy$FxZGA9KtfTewqdcxlUwvIe5Nx9I */
                            private final /* synthetic */ Violation f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                StrictMode.AndroidBlockGuardPolicy.lambda$onThreadPolicyViolation$1(StrictMode.OnThreadViolationListener.this, this.f$1);
                            }
                        });
                    } catch (RejectedExecutionException e) {
                        Log.e(StrictMode.TAG, "ThreadPolicy penaltyCallback failed", e);
                    }
                }
            } else {
                throw new RuntimeException("StrictMode ThreadPolicy violation", violation);
            }
        }

        static /* synthetic */ void lambda$onThreadPolicyViolation$1(OnThreadViolationListener listener, Violation violation) {
            ThreadPolicy oldPolicy = StrictMode.allowThreadViolations();
            try {
                listener.onThreadViolation(violation);
            } finally {
                StrictMode.setThreadPolicy(oldPolicy);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dropboxViolationAsync(int penaltyMask, ViolationInfo info) {
        int outstanding = sDropboxCallsInFlight.incrementAndGet();
        if (outstanding > 20) {
            sDropboxCallsInFlight.decrementAndGet();
            return;
        }
        if (LOG_V) {
            Log.d(TAG, "Dropboxing async; in-flight=" + outstanding);
        }
        BackgroundThread.getHandler().post(new Runnable(penaltyMask, info) {
            /* class android.os.$$Lambda$StrictMode$yZJXPvy2veRNAxL_SWdXzX_OLg */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ StrictMode.ViolationInfo f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                StrictMode.lambda$dropboxViolationAsync$2(this.f$0, this.f$1);
            }
        });
    }

    static /* synthetic */ void lambda$dropboxViolationAsync$2(int penaltyMask, ViolationInfo info) {
        handleApplicationStrictModeViolation(penaltyMask, info);
        int outstandingInner = sDropboxCallsInFlight.decrementAndGet();
        if (LOG_V) {
            Log.d(TAG, "Dropbox complete; in-flight=" + outstandingInner);
        }
    }

    /* access modifiers changed from: private */
    public static void handleApplicationStrictModeViolation(int penaltyMask, ViolationInfo info) {
        int oldMask = getThreadPolicyMask();
        try {
            setThreadPolicyMask(0);
            IActivityManager am = ActivityManager.getService();
            if (am == null) {
                Log.w(TAG, "No activity manager; failed to Dropbox violation.");
            } else {
                am.handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), penaltyMask, info);
            }
        } catch (RemoteException e) {
            if (!(e instanceof DeadObjectException)) {
                Log.e(TAG, "RemoteException handling StrictMode violation", e);
            }
        } catch (Throwable th) {
            setThreadPolicyMask(oldMask);
            throw th;
        }
        setThreadPolicyMask(oldMask);
    }

    /* access modifiers changed from: private */
    public static class AndroidCloseGuardReporter implements CloseGuard.Reporter {
        private AndroidCloseGuardReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            StrictMode.onVmPolicyViolation(new LeakedClosableViolation(message, allocationSite));
        }
    }

    static boolean hasGatheredViolations() {
        return gatheredViolations.get() != null;
    }

    static void clearGatheredViolations() {
        gatheredViolations.set(null);
    }

    public static void conditionallyCheckInstanceCounts() {
        VmPolicy policy = getVmPolicy();
        int policySize = policy.classInstanceLimit.size();
        if (policySize != 0) {
            System.gc();
            System.runFinalization();
            System.gc();
            Class[] classes = (Class[]) policy.classInstanceLimit.keySet().toArray(new Class[policySize]);
            long[] instanceCounts = VMDebug.countInstancesOfClasses(classes, false);
            for (int i = 0; i < classes.length; i++) {
                Class klass = classes[i];
                int limit = policy.classInstanceLimit.get(klass).intValue();
                long instances = instanceCounts[i];
                if (instances > ((long) limit)) {
                    onVmPolicyViolation(new InstanceCountViolation(klass, instances, limit));
                }
            }
        }
    }

    public static void setVmPolicy(VmPolicy policy) {
        synchronized (StrictMode.class) {
            sVmPolicy = policy;
            setCloseGuardEnabled(vmClosableObjectLeaksEnabled());
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                MessageQueue mq = looper.mQueue;
                if (policy.classInstanceLimit.size() != 0) {
                    if ((sVmPolicy.mask & -65536) != 0) {
                        if (!sIsIdlerRegistered) {
                            mq.addIdleHandler(sProcessIdleHandler);
                            sIsIdlerRegistered = true;
                        }
                    }
                }
                mq.removeIdleHandler(sProcessIdleHandler);
                sIsIdlerRegistered = false;
            }
            int networkPolicy = 0;
            if ((sVmPolicy.mask & 64) != 0) {
                if ((sVmPolicy.mask & 268435456) == 0) {
                    if ((sVmPolicy.mask & 16777216) == 0) {
                        networkPolicy = 1;
                    }
                }
                networkPolicy = 2;
            }
            INetworkManagementService netd = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            if (netd != null) {
                try {
                    netd.setUidCleartextNetworkPolicy(Process.myUid(), networkPolicy);
                } catch (RemoteException e) {
                }
            } else if (networkPolicy != 0) {
                Log.w(TAG, "Dropping requested network policy due to missing service!");
            }
            if ((sVmPolicy.mask & 512) != 0) {
                VMRuntime.setNonSdkApiUsageConsumer(sNonSdkApiUsageConsumer);
                VMRuntime.setDedupeHiddenApiWarnings(false);
            } else {
                VMRuntime.setNonSdkApiUsageConsumer((Consumer) null);
                VMRuntime.setDedupeHiddenApiWarnings(true);
            }
            setBlockGuardVmPolicy(sVmPolicy.mask);
        }
    }

    public static VmPolicy getVmPolicy() {
        VmPolicy vmPolicy;
        synchronized (StrictMode.class) {
            vmPolicy = sVmPolicy;
        }
        return vmPolicy;
    }

    public static void enableDefaults() {
        setThreadPolicy(new ThreadPolicy.Builder().detectAll().penaltyLog().build());
        setVmPolicy(new VmPolicy.Builder().detectAll().penaltyLog().build());
    }

    public static boolean vmSqliteObjectLeaksEnabled() {
        return (sVmPolicy.mask & 1) != 0;
    }

    public static boolean vmClosableObjectLeaksEnabled() {
        return (sVmPolicy.mask & 2) != 0;
    }

    public static boolean vmRegistrationLeaksEnabled() {
        return (sVmPolicy.mask & 16) != 0;
    }

    public static boolean vmFileUriExposureEnabled() {
        return (sVmPolicy.mask & 32) != 0;
    }

    public static boolean vmCleartextNetworkEnabled() {
        return (sVmPolicy.mask & 64) != 0;
    }

    public static boolean vmContentUriWithoutPermissionEnabled() {
        return (sVmPolicy.mask & 128) != 0;
    }

    public static boolean vmUntaggedSocketEnabled() {
        return (sVmPolicy.mask & 256) != 0;
    }

    public static boolean vmImplicitDirectBootEnabled() {
        return (sVmPolicy.mask & 1024) != 0;
    }

    public static boolean vmCredentialProtectedWhileLockedEnabled() {
        return (sVmPolicy.mask & 2048) != 0;
    }

    public static void onSqliteObjectLeaked(String message, Throwable originStack) {
        onVmPolicyViolation(new SqliteObjectLeakedViolation(message, originStack));
    }

    @UnsupportedAppUsage
    public static void onWebViewMethodCalledOnWrongThread(Throwable originStack) {
        onVmPolicyViolation(new WebViewMethodCalledOnWrongThreadViolation(originStack));
    }

    public static void onIntentReceiverLeaked(Throwable originStack) {
        onVmPolicyViolation(new IntentReceiverLeakedViolation(originStack));
    }

    public static void onServiceConnectionLeaked(Throwable originStack) {
        onVmPolicyViolation(new ServiceConnectionLeakedViolation(originStack));
    }

    public static void onFileUriExposed(Uri uri, String location) {
        String message = uri + " exposed beyond app through " + location;
        if ((sVmPolicy.mask & 8388608) == 0) {
            onVmPolicyViolation(new FileUriExposedViolation(message));
            return;
        }
        throw new FileUriExposedException(message);
    }

    public static void onContentUriWithoutPermission(Uri uri, String location) {
        onVmPolicyViolation(new ContentUriWithoutPermissionViolation(uri, location));
    }

    public static void onCleartextNetworkDetected(byte[] firstPacket) {
        byte[] rawAddr = null;
        boolean forceDeath = false;
        if (firstPacket != null) {
            if (firstPacket.length >= 20 && (firstPacket[0] & 240) == 64) {
                rawAddr = new byte[4];
                System.arraycopy(firstPacket, 16, rawAddr, 0, 4);
            } else if (firstPacket.length >= 40 && (firstPacket[0] & 240) == 96) {
                rawAddr = new byte[16];
                System.arraycopy(firstPacket, 24, rawAddr, 0, 16);
            }
        }
        String msg = "Detected cleartext network traffic from UID " + Process.myUid();
        if (rawAddr != null) {
            try {
                msg = msg + " to " + InetAddress.getByAddress(rawAddr);
            } catch (UnknownHostException e) {
            }
        }
        String msg2 = msg + HexDump.dumpHexString(firstPacket).trim() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        if ((sVmPolicy.mask & 16777216) != 0) {
            forceDeath = true;
        }
        onVmPolicyViolation(new CleartextNetworkViolation(msg2), forceDeath);
    }

    public static void onUntaggedSocket() {
        onVmPolicyViolation(new UntaggedSocketViolation());
    }

    public static void onImplicitDirectBoot() {
        onVmPolicyViolation(new ImplicitDirectBootViolation());
    }

    private static boolean isUserKeyUnlocked(int userId) {
        IStorageManager storage = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        if (storage == null) {
            return false;
        }
        try {
            return storage.isUserKeyUnlocked(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static void onCredentialProtectedPathAccess(String path, int userId) {
        if (userId == UserHandle.myUserId()) {
            if (!sUserKeyUnlocked) {
                if (isUserKeyUnlocked(userId)) {
                    sUserKeyUnlocked = true;
                    return;
                }
            } else {
                return;
            }
        } else if (isUserKeyUnlocked(userId)) {
            return;
        }
        onVmPolicyViolation(new CredentialProtectedWhileLockedViolation("Accessed credential protected path " + path + " while user " + userId + " was locked"));
    }

    public static void onVmPolicyViolation(Violation originStack) {
        onVmPolicyViolation(originStack, false);
    }

    public static void onVmPolicyViolation(Violation violation, boolean forceDeath) {
        boolean penaltyLog = true;
        boolean penaltyDropbox = (sVmPolicy.mask & 67108864) != 0;
        boolean penaltyDeath = (sVmPolicy.mask & 268435456) != 0 || forceDeath;
        if ((sVmPolicy.mask & 1073741824) == 0) {
            penaltyLog = false;
        }
        ViolationInfo info = new ViolationInfo(violation, -65536 & sVmPolicy.mask);
        info.numAnimationsRunning = 0;
        info.tags = null;
        info.broadcastIntentAction = null;
        Integer fingerprint = Integer.valueOf(info.hashCode());
        long now = SystemClock.uptimeMillis();
        long timeSinceLastViolationMillis = Long.MAX_VALUE;
        synchronized (sLastVmViolationTime) {
            if (sLastVmViolationTime.containsKey(fingerprint)) {
                timeSinceLastViolationMillis = now - sLastVmViolationTime.get(fingerprint).longValue();
            }
            if (timeSinceLastViolationMillis > 1000) {
                sLastVmViolationTime.put(fingerprint, Long.valueOf(now));
            }
        }
        if (timeSinceLastViolationMillis > 1000) {
            if (penaltyLog && sLogger != null && timeSinceLastViolationMillis > 1000) {
                sLogger.log(info);
            }
            if (penaltyDropbox) {
                if (penaltyDeath) {
                    handleApplicationStrictModeViolation(67108864, info);
                } else {
                    dropboxViolationAsync(67108864, info);
                }
            }
            if (penaltyDeath) {
                System.err.println("StrictMode VmPolicy violation with POLICY_DEATH; shutting down.");
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
            if (sVmPolicy.mListener != null && sVmPolicy.mCallbackExecutor != null) {
                try {
                    sVmPolicy.mCallbackExecutor.execute(new Runnable(violation) {
                        /* class android.os.$$Lambda$StrictMode$UFC_nI1x6u8ZwMQmA7bmj9NHZz4 */
                        private final /* synthetic */ Violation f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            StrictMode.lambda$onVmPolicyViolation$3(StrictMode.OnVmViolationListener.this, this.f$1);
                        }
                    });
                } catch (RejectedExecutionException e) {
                    Log.e(TAG, "VmPolicy penaltyCallback failed", e);
                }
            }
        }
    }

    static /* synthetic */ void lambda$onVmPolicyViolation$3(OnVmViolationListener listener, Violation violation) {
        VmPolicy oldPolicy = allowVmViolations();
        try {
            listener.onVmViolation(violation);
        } finally {
            setVmPolicy(oldPolicy);
        }
    }

    static void writeGatheredViolationsToParcel(Parcel p) {
        ArrayList<ViolationInfo> violations = gatheredViolations.get();
        if (violations == null) {
            p.writeInt(0);
        } else {
            int size = Math.min(violations.size(), 3);
            p.writeInt(size);
            for (int i = 0; i < size; i++) {
                violations.get(i).writeToParcel(p, 0);
            }
        }
        gatheredViolations.set(null);
    }

    static void readAndHandleBinderCallViolations(Parcel p) {
        Throwable localCallSite = new Throwable();
        boolean currentlyGathering = (Integer.MIN_VALUE & getThreadPolicyMask()) != 0;
        int size = p.readInt();
        for (int i = 0; i < size; i++) {
            ViolationInfo info = new ViolationInfo(p, !currentlyGathering);
            info.addLocalStack(localCallSite);
            BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
            if (policy instanceof AndroidBlockGuardPolicy) {
                ((AndroidBlockGuardPolicy) policy).handleViolationWithTimingAttempt(info);
            }
        }
    }

    @UnsupportedAppUsage
    private static void onBinderStrictModePolicyChange(int newPolicy) {
        setBlockGuardPolicy(newPolicy);
    }

    public static class Span {
        private final ThreadSpanState mContainerState;
        private long mCreateMillis;
        private String mName;
        private Span mNext;
        private Span mPrev;

        Span(ThreadSpanState threadState) {
            this.mContainerState = threadState;
        }

        protected Span() {
            this.mContainerState = null;
        }

        @UnsupportedAppUsage
        public void finish() {
            ThreadSpanState state = this.mContainerState;
            synchronized (state) {
                if (this.mName != null) {
                    if (this.mPrev != null) {
                        this.mPrev.mNext = this.mNext;
                    }
                    if (this.mNext != null) {
                        this.mNext.mPrev = this.mPrev;
                    }
                    if (state.mActiveHead == this) {
                        state.mActiveHead = this.mNext;
                    }
                    state.mActiveSize--;
                    if (StrictMode.LOG_V) {
                        Log.d(StrictMode.TAG, "Span finished=" + this.mName + "; size=" + state.mActiveSize);
                    }
                    this.mCreateMillis = -1;
                    this.mName = null;
                    this.mPrev = null;
                    this.mNext = null;
                    if (state.mFreeListSize < 5) {
                        this.mNext = state.mFreeListHead;
                        state.mFreeListHead = this;
                        state.mFreeListSize++;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ThreadSpanState {
        public Span mActiveHead;
        public int mActiveSize;
        public Span mFreeListHead;
        public int mFreeListSize;

        private ThreadSpanState() {
        }
    }

    @UnsupportedAppUsage
    public static Span enterCriticalSpan(String name) {
        Span span;
        if (Build.IS_USER) {
            return NO_OP_SPAN;
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must be non-null and non-empty");
        }
        ThreadSpanState state = sThisThreadSpanState.get();
        synchronized (state) {
            if (state.mFreeListHead != null) {
                span = state.mFreeListHead;
                state.mFreeListHead = span.mNext;
                state.mFreeListSize--;
            } else {
                span = new Span(state);
            }
            span.mName = name;
            span.mCreateMillis = SystemClock.uptimeMillis();
            span.mNext = state.mActiveHead;
            span.mPrev = null;
            state.mActiveHead = span;
            state.mActiveSize++;
            if (span.mNext != null) {
                span.mNext.mPrev = span;
            }
            if (LOG_V) {
                Log.d(TAG, "Span enter=" + name + "; size=" + state.mActiveSize);
            }
        }
        return span;
    }

    public static void noteSlowCall(String name) {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onCustomSlowCall(name);
        }
    }

    public static void noteResourceMismatch(Object tag) {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onResourceMismatch(tag);
        }
    }

    public static void noteUnbufferedIO() {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            policy.onUnbufferedIO();
        }
    }

    public static void noteDiskRead() {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            policy.onReadFromDisk();
        }
    }

    public static void noteDiskWrite() {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            policy.onWriteToDisk();
        }
    }

    public static Object trackActivity(Object instance) {
        return new InstanceTracker(instance);
    }

    @UnsupportedAppUsage
    public static void incrementExpectedActivityCount(Class klass) {
        if (klass != null) {
            synchronized (StrictMode.class) {
                if ((sVmPolicy.mask & 4) != 0) {
                    Integer expected = sExpectedActivityInstanceCount.get(klass);
                    int i = 1;
                    if (expected != null) {
                        i = 1 + expected.intValue();
                    }
                    sExpectedActivityInstanceCount.put(klass, Integer.valueOf(i));
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r3v1 int: [D('newExpected' int), D('limit' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0032  */
    public static void decrementExpectedActivityCount(Class klass) {
        int newExpected;
        int limit;
        if (klass != null) {
            synchronized (StrictMode.class) {
                if ((sVmPolicy.mask & 4) != 0) {
                    Integer expected = sExpectedActivityInstanceCount.get(klass);
                    if (expected != null) {
                        if (expected.intValue() != 0) {
                            newExpected = expected.intValue() - 1;
                            if (newExpected != 0) {
                                sExpectedActivityInstanceCount.remove(klass);
                            } else {
                                sExpectedActivityInstanceCount.put(klass, Integer.valueOf(newExpected));
                            }
                            limit = newExpected + 1;
                        }
                    }
                    newExpected = 0;
                    if (newExpected != 0) {
                    }
                    limit = newExpected + 1;
                } else {
                    return;
                }
            }
            if (InstanceTracker.getInstanceCount(klass) > limit) {
                System.gc();
                System.runFinalization();
                System.gc();
                long instances = VMDebug.countInstancesOfClass(klass, false);
                if (instances > ((long) limit)) {
                    onVmPolicyViolation(new InstanceCountViolation(klass, instances, limit));
                }
            }
        }
    }

    public static final class ViolationInfo implements Parcelable {
        public static final Parcelable.Creator<ViolationInfo> CREATOR = new Parcelable.Creator<ViolationInfo>() {
            /* class android.os.StrictMode.ViolationInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ViolationInfo createFromParcel(Parcel in) {
                return new ViolationInfo(in);
            }

            @Override // android.os.Parcelable.Creator
            public ViolationInfo[] newArray(int size) {
                return new ViolationInfo[size];
            }
        };
        public String broadcastIntentAction;
        public int durationMillis;
        private final Deque<StackTraceElement[]> mBinderStack;
        private final int mPenaltyMask;
        private String mStackTrace;
        private final Violation mViolation;
        public int numAnimationsRunning;
        public long numInstances;
        public String[] tags;
        public int violationNumThisLoop;
        public long violationUptimeMillis;

        ViolationInfo(Violation tr, int penaltyMask) {
            this.mBinderStack = new ArrayDeque();
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.mViolation = tr;
            this.mPenaltyMask = penaltyMask;
            this.violationUptimeMillis = SystemClock.uptimeMillis();
            this.numAnimationsRunning = ValueAnimator.getCurrentAnimationsCount();
            Intent broadcastIntent = ActivityThread.getIntentBeingBroadcast();
            if (broadcastIntent != null) {
                this.broadcastIntentAction = broadcastIntent.getAction();
            }
            ThreadSpanState state = (ThreadSpanState) StrictMode.sThisThreadSpanState.get();
            if (tr instanceof InstanceCountViolation) {
                this.numInstances = ((InstanceCountViolation) tr).getNumberOfInstances();
            }
            synchronized (state) {
                int spanActiveCount = state.mActiveSize;
                spanActiveCount = spanActiveCount > 20 ? 20 : spanActiveCount;
                if (spanActiveCount != 0) {
                    this.tags = new String[spanActiveCount];
                    int index = 0;
                    for (Span iter = state.mActiveHead; iter != null && index < spanActiveCount; iter = iter.mNext) {
                        this.tags[index] = iter.mName;
                        index++;
                    }
                }
            }
        }

        public String getStackTrace() {
            if (this.mStackTrace == null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new FastPrintWriter((Writer) sw, false, 256);
                this.mViolation.printStackTrace(pw);
                for (StackTraceElement[] traces : this.mBinderStack) {
                    pw.append("# via Binder call with stack:\n");
                    for (StackTraceElement traceElement : traces) {
                        pw.append("\tat ");
                        pw.append(traceElement.toString());
                        pw.append('\n');
                    }
                }
                pw.flush();
                pw.close();
                this.mStackTrace = sw.toString();
            }
            return this.mStackTrace;
        }

        /* JADX DEBUG: Type inference failed for r0v1. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends android.os.strictmode.Violation> */
        public Class<? extends Violation> getViolationClass() {
            return this.mViolation.getClass();
        }

        public String getViolationDetails() {
            return this.mViolation.getMessage();
        }

        /* access modifiers changed from: package-private */
        public boolean penaltyEnabled(int p) {
            return (this.mPenaltyMask & p) != 0;
        }

        /* access modifiers changed from: package-private */
        public void addLocalStack(Throwable t) {
            this.mBinderStack.addFirst(t.getStackTrace());
        }

        public int hashCode() {
            int result = 17;
            Violation violation = this.mViolation;
            if (violation != null) {
                result = (17 * 37) + violation.hashCode();
            }
            if (this.numAnimationsRunning != 0) {
                result *= 37;
            }
            String str = this.broadcastIntentAction;
            if (str != null) {
                result = (result * 37) + str.hashCode();
            }
            String[] strArr = this.tags;
            if (strArr != null) {
                for (String tag : strArr) {
                    result = (result * 37) + tag.hashCode();
                }
            }
            return result;
        }

        public ViolationInfo(Parcel in) {
            this(in, false);
        }

        public ViolationInfo(Parcel in, boolean unsetGatheringBit) {
            this.mBinderStack = new ArrayDeque();
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.mViolation = (Violation) in.readSerializable();
            int binderStackSize = in.readInt();
            for (int i = 0; i < binderStackSize; i++) {
                StackTraceElement[] traceElements = new StackTraceElement[in.readInt()];
                for (int j = 0; j < traceElements.length; j++) {
                    traceElements[j] = new StackTraceElement(in.readString(), in.readString(), in.readString(), in.readInt());
                }
                this.mBinderStack.add(traceElements);
            }
            int rawPenaltyMask = in.readInt();
            if (unsetGatheringBit) {
                this.mPenaltyMask = Integer.MAX_VALUE & rawPenaltyMask;
            } else {
                this.mPenaltyMask = rawPenaltyMask;
            }
            this.durationMillis = in.readInt();
            this.violationNumThisLoop = in.readInt();
            this.numAnimationsRunning = in.readInt();
            this.violationUptimeMillis = in.readLong();
            this.numInstances = in.readLong();
            this.broadcastIntentAction = in.readString();
            this.tags = in.readStringArray();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.mViolation);
            dest.writeInt(this.mBinderStack.size());
            for (StackTraceElement[] traceElements : this.mBinderStack) {
                dest.writeInt(traceElements.length);
                for (StackTraceElement element : traceElements) {
                    dest.writeString(element.getClassName());
                    dest.writeString(element.getMethodName());
                    dest.writeString(element.getFileName());
                    dest.writeInt(element.getLineNumber());
                }
            }
            int start = dest.dataPosition();
            dest.writeInt(this.mPenaltyMask);
            dest.writeInt(this.durationMillis);
            dest.writeInt(this.violationNumThisLoop);
            dest.writeInt(this.numAnimationsRunning);
            dest.writeLong(this.violationUptimeMillis);
            dest.writeLong(this.numInstances);
            dest.writeString(this.broadcastIntentAction);
            dest.writeStringArray(this.tags);
            int dataPosition = dest.dataPosition() - start;
        }

        public void dump(Printer pw, String prefix) {
            pw.println(prefix + "stackTrace: " + getStackTrace());
            pw.println(prefix + "penalty: " + this.mPenaltyMask);
            if (this.durationMillis != -1) {
                pw.println(prefix + "durationMillis: " + this.durationMillis);
            }
            if (this.numInstances != -1) {
                pw.println(prefix + "numInstances: " + this.numInstances);
            }
            if (this.violationNumThisLoop != 0) {
                pw.println(prefix + "violationNumThisLoop: " + this.violationNumThisLoop);
            }
            if (this.numAnimationsRunning != 0) {
                pw.println(prefix + "numAnimationsRunning: " + this.numAnimationsRunning);
            }
            pw.println(prefix + "violationUptimeMillis: " + this.violationUptimeMillis);
            if (this.broadcastIntentAction != null) {
                pw.println(prefix + "broadcastIntentAction: " + this.broadcastIntentAction);
            }
            String[] strArr = this.tags;
            if (strArr != null) {
                int index = 0;
                int length = strArr.length;
                int i = 0;
                while (i < length) {
                    String tag = strArr[i];
                    pw.println(prefix + "tag[" + index + "]: " + tag);
                    i++;
                    index++;
                }
            }
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static final class InstanceTracker {
        private static final HashMap<Class<?>, Integer> sInstanceCounts = new HashMap<>();
        private final Class<?> mKlass;

        public InstanceTracker(Object instance) {
            this.mKlass = instance.getClass();
            synchronized (sInstanceCounts) {
                Integer value = sInstanceCounts.get(this.mKlass);
                sInstanceCounts.put(this.mKlass, Integer.valueOf(value != null ? 1 + value.intValue() : 1));
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                synchronized (sInstanceCounts) {
                    Integer value = sInstanceCounts.get(this.mKlass);
                    if (value != null) {
                        int newValue = value.intValue() - 1;
                        if (newValue > 0) {
                            sInstanceCounts.put(this.mKlass, Integer.valueOf(newValue));
                        } else {
                            sInstanceCounts.remove(this.mKlass);
                        }
                    }
                }
            } finally {
                super.finalize();
            }
        }

        public static int getInstanceCount(Class<?> klass) {
            int intValue;
            synchronized (sInstanceCounts) {
                Integer value = sInstanceCounts.get(klass);
                intValue = value != null ? value.intValue() : 0;
            }
            return intValue;
        }
    }
}
