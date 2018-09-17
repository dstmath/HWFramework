package android.os;

import android.animation.ValueAnimator;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.LinkQualityInfo;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.INetworkManagementService.Stub;
import android.os.MessageQueue.IdleHandler;
import android.os.health.HealthKeys;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Printer;
import android.util.Singleton;
import android.util.Slog;
import android.view.IWindowManager;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.HexDump;
import dalvik.system.BlockGuard;
import dalvik.system.BlockGuard.BlockGuardPolicyException;
import dalvik.system.BlockGuard.Policy;
import dalvik.system.CloseGuard;
import dalvik.system.CloseGuard.Reporter;
import dalvik.system.VMDebug;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class StrictMode {
    private static final int ALL_THREAD_DETECT_BITS = 31;
    private static final int ALL_VM_DETECT_BITS = 32512;
    public static final boolean ART_OPT_ENABLE = false;
    private static final String CLEARTEXT_PROPERTY = "persist.sys.strictmode.clear";
    public static final int DETECT_CUSTOM = 8;
    public static final int DETECT_DISK_READ = 2;
    public static final int DETECT_DISK_WRITE = 1;
    public static final int DETECT_NETWORK = 4;
    public static final int DETECT_RESOURCE_MISMATCH = 16;
    public static final int DETECT_VM_ACTIVITY_LEAKS = 1024;
    private static final int DETECT_VM_CLEARTEXT_NETWORK = 16384;
    public static final int DETECT_VM_CLOSABLE_LEAKS = 512;
    public static final int DETECT_VM_CURSOR_LEAKS = 256;
    private static final int DETECT_VM_FILE_URI_EXPOSURE = 8192;
    private static final int DETECT_VM_INSTANCE_LEAKS = 2048;
    public static final int DETECT_VM_REGISTRATION_LEAKS = 4096;
    public static final String DISABLE_PROPERTY = "persist.sys.strictmode.disable";
    private static final HashMap<Class, Integer> EMPTY_CLASS_LIMIT_MAP = null;
    private static final boolean IS_ENG_BUILD = false;
    private static final boolean IS_USER_BUILD = false;
    private static final boolean LOG_V = false;
    private static final int MAX_OFFENSES_PER_LOOP = 10;
    private static final int MAX_SPAN_TAGS = 20;
    private static final long MIN_DIALOG_INTERVAL_MS = 30000;
    private static final long MIN_LOG_INTERVAL_MS = 1000;
    public static final int NETWORK_POLICY_ACCEPT = 0;
    public static final int NETWORK_POLICY_LOG = 1;
    public static final int NETWORK_POLICY_REJECT = 2;
    private static final Span NO_OP_SPAN = null;
    public static final int PENALTY_DEATH = 262144;
    public static final int PENALTY_DEATH_ON_CLEARTEXT_NETWORK = 33554432;
    public static final int PENALTY_DEATH_ON_FILE_URI_EXPOSURE = 67108864;
    public static final int PENALTY_DEATH_ON_NETWORK = 16777216;
    public static final int PENALTY_DIALOG = 131072;
    public static final int PENALTY_DROPBOX = 2097152;
    public static final int PENALTY_FLASH = 1048576;
    public static final int PENALTY_GATHER = 4194304;
    public static final int PENALTY_LOG = 65536;
    private static final String TAG = "StrictMode";
    private static final int THREAD_PENALTY_MASK = 24576000;
    public static final String VISUAL_PROPERTY = "persist.sys.strictmode.visual";
    private static final int VM_PENALTY_MASK = 103088128;
    private static final ThreadLocal<ArrayList<ViolationInfo>> gatheredViolations = null;
    private static final AtomicInteger sDropboxCallsInFlight = null;
    private static final HashMap<Class, Integer> sExpectedActivityInstanceCount = null;
    private static boolean sIsIdlerRegistered;
    private static long sLastInstanceCountCheckMillis;
    private static final HashMap<Integer, Long> sLastVmViolationTime = null;
    private static final IdleHandler sProcessIdleHandler = null;
    private static final ThreadLocal<ThreadSpanState> sThisThreadSpanState = null;
    private static volatile VmPolicy sVmPolicy;
    private static volatile int sVmPolicyMask;
    private static Singleton<IWindowManager> sWindowManager;
    private static final ThreadLocal<AndroidBlockGuardPolicy> threadAndroidPolicy = null;
    private static final ThreadLocal<Handler> threadHandler = null;
    private static final ThreadLocal<ArrayList<ViolationInfo>> violationsBeingTimed = null;

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

        public void finish() {
            ThreadSpanState state = this.mContainerState;
            synchronized (state) {
                if (this.mName == null) {
                    return;
                }
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
                    state.mFreeListSize += StrictMode.NETWORK_POLICY_LOG;
                }
            }
        }
    }

    /* renamed from: android.os.StrictMode.9 */
    static class AnonymousClass9 extends Thread {
        final /* synthetic */ ViolationInfo val$info;
        final /* synthetic */ int val$violationMaskSubset;

        AnonymousClass9(String $anonymous0, int val$violationMaskSubset, ViolationInfo val$info) {
            this.val$violationMaskSubset = val$violationMaskSubset;
            this.val$info = val$info;
            super($anonymous0);
        }

        public void run() {
            Process.setThreadPriority(StrictMode.MAX_OFFENSES_PER_LOOP);
            try {
                IActivityManager am = ActivityManagerNative.getDefault();
                if (am == null) {
                    Log.d(StrictMode.TAG, "No activity manager; failed to Dropbox violation.");
                } else {
                    am.handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), this.val$violationMaskSubset, this.val$info);
                }
            } catch (RemoteException e) {
                if (!(e instanceof DeadObjectException)) {
                    Log.e(StrictMode.TAG, "RemoteException handling StrictMode violation", e);
                }
            }
            int outstanding = StrictMode.sDropboxCallsInFlight.decrementAndGet();
            if (StrictMode.LOG_V) {
                Log.d(StrictMode.TAG, "Dropbox complete; in-flight=" + outstanding);
            }
        }
    }

    private static class AndroidBlockGuardPolicy implements Policy {
        private ArrayMap<Integer, Long> mLastViolationTime;
        private int mPolicyMask;

        /* renamed from: android.os.StrictMode.AndroidBlockGuardPolicy.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ boolean val$isVisual;
            final /* synthetic */ ArrayList val$records;
            final /* synthetic */ IWindowManager val$windowManager;

            AnonymousClass1(IWindowManager val$windowManager, boolean val$isVisual, ArrayList val$records) {
                this.val$windowManager = val$windowManager;
                this.val$isVisual = val$isVisual;
                this.val$records = val$records;
            }

            public void run() {
                long loopFinishTime = SystemClock.uptimeMillis();
                if (this.val$windowManager != null && this.val$isVisual) {
                    try {
                        this.val$windowManager.showStrictModeViolation(StrictMode.LOG_V);
                    } catch (RemoteException e) {
                    }
                }
                for (int n = StrictMode.NETWORK_POLICY_ACCEPT; n < this.val$records.size(); n += StrictMode.NETWORK_POLICY_LOG) {
                    ViolationInfo v = (ViolationInfo) this.val$records.get(n);
                    v.violationNumThisLoop = n + StrictMode.NETWORK_POLICY_LOG;
                    v.durationMillis = (int) (loopFinishTime - v.violationUptimeMillis);
                    AndroidBlockGuardPolicy.this.handleViolation(v);
                }
                this.val$records.clear();
            }
        }

        public AndroidBlockGuardPolicy(int policyMask) {
            this.mPolicyMask = policyMask;
        }

        public String toString() {
            return "AndroidBlockGuardPolicy; mPolicyMask=" + this.mPolicyMask;
        }

        public int getPolicyMask() {
            return this.mPolicyMask;
        }

        public void onWriteToDisk() {
            if ((this.mPolicyMask & StrictMode.NETWORK_POLICY_LOG) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeDiskWriteViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        void onCustomSlowCall(String name) {
            if ((this.mPolicyMask & StrictMode.DETECT_CUSTOM) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeCustomViolation(this.mPolicyMask, name);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        void onResourceMismatch(Object tag) {
            if ((this.mPolicyMask & StrictMode.DETECT_RESOURCE_MISMATCH) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeResourceMismatchViolation(this.mPolicyMask, tag);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onReadFromDisk() {
            if ((this.mPolicyMask & StrictMode.NETWORK_POLICY_REJECT) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeDiskReadViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onNetwork() {
            if ((this.mPolicyMask & StrictMode.DETECT_NETWORK) != 0) {
                if ((this.mPolicyMask & StrictMode.PENALTY_DEATH_ON_NETWORK) != 0) {
                    throw new NetworkOnMainThreadException();
                } else if (!StrictMode.tooManyViolationsThisLoop()) {
                    BlockGuardPolicyException e = new StrictModeNetworkViolation(this.mPolicyMask);
                    e.fillInStackTrace();
                    startHandlingViolationException(e);
                }
            }
        }

        public void setPolicyMask(int policyMask) {
            this.mPolicyMask = policyMask;
        }

        void startHandlingViolationException(BlockGuardPolicyException e) {
            ViolationInfo info = new ViolationInfo((Throwable) e, e.getPolicy());
            info.violationUptimeMillis = SystemClock.uptimeMillis();
            handleViolationWithTimingAttempt(info);
        }

        void handleViolationWithTimingAttempt(ViolationInfo info) {
            if (Looper.myLooper() == null || (info.policy & StrictMode.THREAD_PENALTY_MASK) == StrictMode.PENALTY_DEATH) {
                info.durationMillis = -1;
                handleViolation(info);
                return;
            }
            ArrayList<ViolationInfo> records = (ArrayList) StrictMode.violationsBeingTimed.get();
            if (records.size() < StrictMode.MAX_OFFENSES_PER_LOOP) {
                records.add(info);
                if (records.size() <= StrictMode.NETWORK_POLICY_LOG) {
                    IWindowManager iWindowManager = (info.policy & StrictMode.PENALTY_FLASH) != 0 ? (IWindowManager) StrictMode.sWindowManager.get() : null;
                    boolean isVisual = SystemProperties.getBoolean(StrictMode.VISUAL_PROPERTY, StrictMode.LOG_V);
                    if (iWindowManager != null && isVisual) {
                        try {
                            iWindowManager.showStrictModeViolation(true);
                        } catch (RemoteException e) {
                        }
                    }
                    ((Handler) StrictMode.threadHandler.get()).postAtFrontOfQueue(new AnonymousClass1(iWindowManager, isVisual, records));
                }
            }
        }

        void handleViolation(ViolationInfo info) {
            if (!(info == null || info.crashInfo == null)) {
                if (info.crashInfo.stackTrace != null) {
                    if (StrictMode.LOG_V) {
                        Log.d(StrictMode.TAG, "handleViolation; policy=" + info.policy);
                    }
                    if ((info.policy & StrictMode.PENALTY_GATHER) != 0) {
                        ArrayList<ViolationInfo> violations = (ArrayList) StrictMode.gatheredViolations.get();
                        if (violations == null) {
                            ArrayList<ViolationInfo> arrayList = new ArrayList(StrictMode.NETWORK_POLICY_LOG);
                            StrictMode.gatheredViolations.set(arrayList);
                        } else if (violations.size() >= 5) {
                            return;
                        }
                        for (ViolationInfo previous : violations) {
                            if (info.crashInfo.stackTrace.equals(previous.crashInfo.stackTrace)) {
                                return;
                            }
                        }
                        violations.add(info);
                        return;
                    }
                    Integer crashFingerprint = Integer.valueOf(info.hashCode());
                    long lastViolationTime = 0;
                    if (this.mLastViolationTime != null) {
                        Long vtime = (Long) this.mLastViolationTime.get(crashFingerprint);
                        if (vtime != null) {
                            lastViolationTime = vtime.longValue();
                        }
                    } else {
                        this.mLastViolationTime = new ArrayMap(StrictMode.NETWORK_POLICY_LOG);
                    }
                    long now = SystemClock.uptimeMillis();
                    this.mLastViolationTime.put(crashFingerprint, Long.valueOf(now));
                    long timeSinceLastViolationMillis = lastViolationTime == 0 ? LinkQualityInfo.UNKNOWN_LONG : now - lastViolationTime;
                    if ((info.policy & StrictMode.PENALTY_LOG) != 0 && timeSinceLastViolationMillis > StrictMode.MIN_LOG_INTERVAL_MS) {
                        int i = info.durationMillis;
                        if (r0 != -1) {
                            Log.d(StrictMode.TAG, "StrictMode policy violation; ~duration=" + info.durationMillis + " ms: " + info.crashInfo.stackTrace);
                        } else {
                            Log.d(StrictMode.TAG, "StrictMode policy violation: " + info.crashInfo.stackTrace);
                        }
                    }
                    int violationMaskSubset = StrictMode.NETWORK_POLICY_ACCEPT;
                    if ((info.policy & StrictMode.PENALTY_DIALOG) != 0 && timeSinceLastViolationMillis > StrictMode.MIN_DIALOG_INTERVAL_MS) {
                        violationMaskSubset = StrictMode.PENALTY_DIALOG;
                    }
                    if ((info.policy & StrictMode.PENALTY_DROPBOX) != 0 && lastViolationTime == 0) {
                        violationMaskSubset |= StrictMode.PENALTY_DROPBOX;
                    }
                    if (violationMaskSubset != 0) {
                        violationMaskSubset |= StrictMode.parseViolationFromMessage(info.crashInfo.exceptionMessage);
                        int savedPolicyMask = StrictMode.getThreadPolicyMask();
                        if ((info.policy & StrictMode.THREAD_PENALTY_MASK) == StrictMode.PENALTY_DROPBOX ? true : StrictMode.LOG_V) {
                            StrictMode.dropboxViolationAsync(violationMaskSubset, info);
                            return;
                        }
                        try {
                            StrictMode.setThreadPolicyMask(StrictMode.NETWORK_POLICY_ACCEPT);
                            ActivityManagerNative.getDefault().handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
                            StrictMode.setThreadPolicyMask(savedPolicyMask);
                        } catch (RemoteException e) {
                            if (!(e instanceof DeadObjectException)) {
                                Log.e(StrictMode.TAG, "RemoteException trying to handle StrictMode violation", e);
                            }
                            StrictMode.setThreadPolicyMask(savedPolicyMask);
                        } catch (Throwable th) {
                            StrictMode.setThreadPolicyMask(savedPolicyMask);
                        }
                    }
                    if ((info.policy & StrictMode.PENALTY_DEATH) != 0) {
                        StrictMode.executeDeathPenalty(info);
                    }
                    return;
                }
            }
            Log.wtf(StrictMode.TAG, "unexpected null stacktrace");
        }
    }

    private static class AndroidCloseGuardReporter implements Reporter {
        private AndroidCloseGuardReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            StrictMode.onVmPolicyViolation(message, allocationSite);
        }
    }

    private static class InstanceCountViolation extends Throwable {
        private static final StackTraceElement[] FAKE_STACK = null;
        final Class mClass;
        final long mInstances;
        final int mLimit;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.StrictMode.InstanceCountViolation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.StrictMode.InstanceCountViolation.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.InstanceCountViolation.<clinit>():void");
        }

        public InstanceCountViolation(Class klass, long instances, int limit) {
            super(klass.toString() + "; instances=" + instances + "; limit=" + limit);
            setStackTrace(FAKE_STACK);
            this.mClass = klass;
            this.mInstances = instances;
            this.mLimit = limit;
        }
    }

    private static final class InstanceTracker {
        private static final HashMap<Class<?>, Integer> sInstanceCounts = null;
        private final Class<?> mKlass;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.StrictMode.InstanceTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.StrictMode.InstanceTracker.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.InstanceTracker.<clinit>():void");
        }

        public InstanceTracker(Object instance) {
            this.mKlass = instance.getClass();
            synchronized (sInstanceCounts) {
                Integer value = (Integer) sInstanceCounts.get(this.mKlass);
                sInstanceCounts.put(this.mKlass, Integer.valueOf(value != null ? value.intValue() + StrictMode.NETWORK_POLICY_LOG : StrictMode.NETWORK_POLICY_LOG));
            }
        }

        protected void finalize() throws Throwable {
            try {
                synchronized (sInstanceCounts) {
                    Integer value = (Integer) sInstanceCounts.get(this.mKlass);
                    if (value != null) {
                        int newValue = value.intValue() - 1;
                        if (newValue > 0) {
                            sInstanceCounts.put(this.mKlass, Integer.valueOf(newValue));
                        } else {
                            sInstanceCounts.remove(this.mKlass);
                        }
                    }
                }
                if (StrictMode.ART_OPT_ENABLE) {
                    synchronized (StrictMode.class) {
                        Integer expectValue = (Integer) StrictMode.sExpectedActivityInstanceCount.get(this.mKlass);
                        if (expectValue != null) {
                            int newexpectValue = expectValue.intValue() - 1;
                            if (newexpectValue > 0) {
                                StrictMode.sExpectedActivityInstanceCount.put(this.mKlass, Integer.valueOf(newexpectValue));
                            } else {
                                StrictMode.sExpectedActivityInstanceCount.remove(this.mKlass);
                            }
                        }
                    }
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }

        public static int getInstanceCount(Class<?> klass) {
            int intValue;
            synchronized (sInstanceCounts) {
                Integer value = (Integer) sInstanceCounts.get(klass);
                intValue = value != null ? value.intValue() : StrictMode.NETWORK_POLICY_ACCEPT;
            }
            return intValue;
        }
    }

    private static class LogStackTrace extends Exception {
        /* synthetic */ LogStackTrace(LogStackTrace logStackTrace) {
            this();
        }

        private LogStackTrace() {
        }
    }

    public static class StrictModeViolation extends BlockGuardPolicyException {
        public StrictModeViolation(int policyState, int policyViolated, String message) {
            super(policyState, policyViolated, message);
        }
    }

    private static class StrictModeCustomViolation extends StrictModeViolation {
        public StrictModeCustomViolation(int policyMask, String name) {
            super(policyMask, StrictMode.DETECT_CUSTOM, name);
        }
    }

    private static class StrictModeDiskReadViolation extends StrictModeViolation {
        public StrictModeDiskReadViolation(int policyMask) {
            super(policyMask, StrictMode.NETWORK_POLICY_REJECT, null);
        }
    }

    private static class StrictModeDiskWriteViolation extends StrictModeViolation {
        public StrictModeDiskWriteViolation(int policyMask) {
            super(policyMask, StrictMode.NETWORK_POLICY_LOG, null);
        }
    }

    public static class StrictModeNetworkViolation extends StrictModeViolation {
        public StrictModeNetworkViolation(int policyMask) {
            super(policyMask, StrictMode.DETECT_NETWORK, null);
        }
    }

    private static class StrictModeResourceMismatchViolation extends StrictModeViolation {
        public StrictModeResourceMismatchViolation(int policyMask, Object tag) {
            String str = null;
            if (tag != null) {
                str = tag.toString();
            }
            super(policyMask, StrictMode.DETECT_RESOURCE_MISMATCH, str);
        }
    }

    public static final class ThreadPolicy {
        public static final ThreadPolicy LAX = null;
        final int mask;

        public static final class Builder {
            private int mMask;

            private android.os.StrictMode.ThreadPolicy.Builder disable(int r1) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.StrictMode.ThreadPolicy.Builder.disable(int):android.os.StrictMode$ThreadPolicy$Builder
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 9 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.ThreadPolicy.Builder.disable(int):android.os.StrictMode$ThreadPolicy$Builder");
            }

            public Builder() {
                this.mMask = StrictMode.NETWORK_POLICY_ACCEPT;
                this.mMask = StrictMode.NETWORK_POLICY_ACCEPT;
            }

            public Builder(ThreadPolicy policy) {
                this.mMask = StrictMode.NETWORK_POLICY_ACCEPT;
                this.mMask = policy.mask;
            }

            public Builder detectAll() {
                return enable(StrictMode.ALL_THREAD_DETECT_BITS);
            }

            public Builder permitAll() {
                return disable(StrictMode.ALL_THREAD_DETECT_BITS);
            }

            public Builder detectNetwork() {
                return enable(StrictMode.DETECT_NETWORK);
            }

            public Builder permitNetwork() {
                return disable(StrictMode.DETECT_NETWORK);
            }

            public Builder detectDiskReads() {
                return enable(StrictMode.NETWORK_POLICY_REJECT);
            }

            public Builder permitDiskReads() {
                return disable(StrictMode.NETWORK_POLICY_REJECT);
            }

            public Builder detectCustomSlowCalls() {
                return enable(StrictMode.DETECT_CUSTOM);
            }

            public Builder permitCustomSlowCalls() {
                return disable(StrictMode.DETECT_CUSTOM);
            }

            public Builder permitResourceMismatches() {
                return disable(StrictMode.DETECT_RESOURCE_MISMATCH);
            }

            public Builder detectResourceMismatches() {
                return enable(StrictMode.DETECT_RESOURCE_MISMATCH);
            }

            public Builder detectDiskWrites() {
                return enable(StrictMode.NETWORK_POLICY_LOG);
            }

            public Builder permitDiskWrites() {
                return disable(StrictMode.NETWORK_POLICY_LOG);
            }

            public Builder penaltyDialog() {
                return enable(StrictMode.PENALTY_DIALOG);
            }

            public Builder penaltyDeath() {
                return enable(StrictMode.PENALTY_DEATH);
            }

            public Builder penaltyDeathOnNetwork() {
                return enable(StrictMode.PENALTY_DEATH_ON_NETWORK);
            }

            public Builder penaltyFlashScreen() {
                return enable(StrictMode.PENALTY_FLASH);
            }

            public Builder penaltyLog() {
                return enable(StrictMode.PENALTY_LOG);
            }

            public Builder penaltyDropBox() {
                return enable(StrictMode.PENALTY_DROPBOX);
            }

            private Builder enable(int bit) {
                this.mMask |= bit;
                return this;
            }

            public ThreadPolicy build() {
                if (this.mMask != 0 && (this.mMask & 2555904) == 0) {
                    penaltyLog();
                }
                return new ThreadPolicy(this.mMask, null);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.StrictMode.ThreadPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.StrictMode.ThreadPolicy.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.ThreadPolicy.<clinit>():void");
        }

        /* synthetic */ ThreadPolicy(int mask, ThreadPolicy threadPolicy) {
            this(mask);
        }

        private ThreadPolicy(int mask) {
            this.mask = mask;
        }

        public String toString() {
            return "[StrictMode.ThreadPolicy; mask=" + this.mask + "]";
        }
    }

    private static class ThreadSpanState {
        public Span mActiveHead;
        public int mActiveSize;
        public Span mFreeListHead;
        public int mFreeListSize;

        /* synthetic */ ThreadSpanState(ThreadSpanState threadSpanState) {
            this();
        }

        private ThreadSpanState() {
        }
    }

    public static class ViolationInfo {
        public String broadcastIntentAction;
        public final CrashInfo crashInfo;
        public int durationMillis;
        public String message;
        public int numAnimationsRunning;
        public long numInstances;
        public final int policy;
        public String[] tags;
        public int violationNumThisLoop;
        public long violationUptimeMillis;

        public ViolationInfo() {
            this.durationMillis = -1;
            this.numAnimationsRunning = StrictMode.NETWORK_POLICY_ACCEPT;
            this.numInstances = -1;
            this.crashInfo = null;
            this.policy = StrictMode.NETWORK_POLICY_ACCEPT;
        }

        public ViolationInfo(Throwable tr, int policy) {
            this(null, tr, policy);
        }

        public ViolationInfo(String message, Throwable tr, int policy) {
            this.durationMillis = -1;
            this.numAnimationsRunning = StrictMode.NETWORK_POLICY_ACCEPT;
            this.numInstances = -1;
            this.message = message;
            this.crashInfo = new CrashInfo(tr);
            this.violationUptimeMillis = SystemClock.uptimeMillis();
            this.policy = policy;
            this.numAnimationsRunning = ValueAnimator.getCurrentAnimationsCount();
            Intent broadcastIntent = ActivityThread.getIntentBeingBroadcast();
            if (broadcastIntent != null) {
                this.broadcastIntentAction = broadcastIntent.getAction();
            }
            ThreadSpanState state = (ThreadSpanState) StrictMode.sThisThreadSpanState.get();
            if (tr instanceof InstanceCountViolation) {
                this.numInstances = ((InstanceCountViolation) tr).mInstances;
            }
            synchronized (state) {
                int spanActiveCount = state.mActiveSize;
                if (spanActiveCount > StrictMode.MAX_SPAN_TAGS) {
                    spanActiveCount = StrictMode.MAX_SPAN_TAGS;
                }
                if (spanActiveCount != 0) {
                    this.tags = new String[spanActiveCount];
                    int index = StrictMode.NETWORK_POLICY_ACCEPT;
                    for (Span iter = state.mActiveHead; iter != null && index < spanActiveCount; iter = iter.mNext) {
                        this.tags[index] = iter.mName;
                        index += StrictMode.NETWORK_POLICY_LOG;
                    }
                }
            }
        }

        public int hashCode() {
            int result = this.crashInfo.stackTrace.hashCode() + 629;
            if (this.numAnimationsRunning != 0) {
                result *= 37;
            }
            if (this.broadcastIntentAction != null) {
                result = (result * 37) + this.broadcastIntentAction.hashCode();
            }
            if (this.tags != null) {
                String[] strArr = this.tags;
                for (int i = StrictMode.NETWORK_POLICY_ACCEPT; i < strArr.length; i += StrictMode.NETWORK_POLICY_LOG) {
                    result = (result * 37) + strArr[i].hashCode();
                }
            }
            return result;
        }

        public ViolationInfo(Parcel in) {
            this(in, (boolean) StrictMode.LOG_V);
        }

        public ViolationInfo(Parcel in, boolean unsetGatheringBit) {
            this.durationMillis = -1;
            this.numAnimationsRunning = StrictMode.NETWORK_POLICY_ACCEPT;
            this.numInstances = -1;
            this.message = in.readString();
            this.crashInfo = new CrashInfo(in);
            int rawPolicy = in.readInt();
            if (unsetGatheringBit) {
                this.policy = -4194305 & rawPolicy;
            } else {
                this.policy = rawPolicy;
            }
            this.durationMillis = in.readInt();
            this.violationNumThisLoop = in.readInt();
            this.numAnimationsRunning = in.readInt();
            this.violationUptimeMillis = in.readLong();
            this.numInstances = in.readLong();
            this.broadcastIntentAction = in.readString();
            this.tags = in.readStringArray();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.message);
            this.crashInfo.writeToParcel(dest, flags);
            int start = dest.dataPosition();
            dest.writeInt(this.policy);
            dest.writeInt(this.durationMillis);
            dest.writeInt(this.violationNumThisLoop);
            dest.writeInt(this.numAnimationsRunning);
            dest.writeLong(this.violationUptimeMillis);
            dest.writeLong(this.numInstances);
            dest.writeString(this.broadcastIntentAction);
            dest.writeStringArray(this.tags);
            if (dest.dataPosition() - start > GLES20.GL_TEXTURE_MAG_FILTER) {
                Slog.d(StrictMode.TAG, "VIO: policy=" + this.policy + " dur=" + this.durationMillis + " numLoop=" + this.violationNumThisLoop + " anim=" + this.numAnimationsRunning + " uptime=" + this.violationUptimeMillis + " numInst=" + this.numInstances);
                Slog.d(StrictMode.TAG, "VIO: action=" + this.broadcastIntentAction);
                Slog.d(StrictMode.TAG, "VIO: tags=" + Arrays.toString(this.tags));
                Slog.d(StrictMode.TAG, "VIO: TOTAL BYTES WRITTEN: " + (dest.dataPosition() - start));
            }
        }

        public void dump(Printer pw, String prefix) {
            int i = StrictMode.NETWORK_POLICY_ACCEPT;
            this.crashInfo.dump(pw, prefix);
            pw.println(prefix + "policy: " + this.policy);
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
            if (this.tags != null) {
                String[] strArr = this.tags;
                int length = strArr.length;
                int index = StrictMode.NETWORK_POLICY_ACCEPT;
                while (i < length) {
                    int index2 = index + StrictMode.NETWORK_POLICY_LOG;
                    pw.println(prefix + "tag[" + index + "]: " + strArr[i]);
                    i += StrictMode.NETWORK_POLICY_LOG;
                    index = index2;
                }
            }
        }
    }

    public static final class VmPolicy {
        public static final VmPolicy LAX = null;
        final HashMap<Class, Integer> classInstanceLimit;
        final int mask;

        public static final class Builder {
            private HashMap<Class, Integer> mClassInstanceLimit;
            private boolean mClassInstanceLimitNeedCow;
            private int mMask;

            public Builder() {
                this.mClassInstanceLimitNeedCow = StrictMode.LOG_V;
                this.mMask = StrictMode.NETWORK_POLICY_ACCEPT;
            }

            public Builder(VmPolicy base) {
                this.mClassInstanceLimitNeedCow = StrictMode.LOG_V;
                this.mMask = base.mask;
                this.mClassInstanceLimitNeedCow = true;
                this.mClassInstanceLimit = base.classInstanceLimit;
            }

            public Builder setClassInstanceLimit(Class klass, int instanceLimit) {
                if (klass == null) {
                    throw new NullPointerException("klass == null");
                }
                if (this.mClassInstanceLimitNeedCow) {
                    if (this.mClassInstanceLimit.containsKey(klass) && ((Integer) this.mClassInstanceLimit.get(klass)).intValue() == instanceLimit) {
                        return this;
                    }
                    this.mClassInstanceLimitNeedCow = StrictMode.LOG_V;
                    this.mClassInstanceLimit = (HashMap) this.mClassInstanceLimit.clone();
                } else if (this.mClassInstanceLimit == null) {
                    this.mClassInstanceLimit = new HashMap();
                }
                this.mMask |= StrictMode.DETECT_VM_INSTANCE_LEAKS;
                this.mClassInstanceLimit.put(klass, Integer.valueOf(instanceLimit));
                return this;
            }

            public Builder detectActivityLeaks() {
                return enable(StrictMode.DETECT_VM_ACTIVITY_LEAKS);
            }

            public Builder detectAll() {
                int flags = 14080;
                if (SystemProperties.getBoolean(StrictMode.CLEARTEXT_PROPERTY, StrictMode.LOG_V)) {
                    flags = 30464;
                }
                return enable(flags);
            }

            public Builder detectLeakedSqlLiteObjects() {
                return enable(StrictMode.DETECT_VM_CURSOR_LEAKS);
            }

            public Builder detectLeakedClosableObjects() {
                return enable(StrictMode.DETECT_VM_CLOSABLE_LEAKS);
            }

            public Builder detectLeakedRegistrationObjects() {
                return enable(StrictMode.DETECT_VM_REGISTRATION_LEAKS);
            }

            public Builder detectFileUriExposure() {
                return enable(StrictMode.DETECT_VM_FILE_URI_EXPOSURE);
            }

            public Builder detectCleartextNetwork() {
                return enable(StrictMode.DETECT_VM_CLEARTEXT_NETWORK);
            }

            public Builder penaltyDeath() {
                return enable(StrictMode.PENALTY_DEATH);
            }

            public Builder penaltyDeathOnCleartextNetwork() {
                return enable(StrictMode.PENALTY_DEATH_ON_CLEARTEXT_NETWORK);
            }

            public Builder penaltyDeathOnFileUriExposure() {
                return enable(StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE);
            }

            public Builder penaltyLog() {
                return enable(StrictMode.PENALTY_LOG);
            }

            public Builder penaltyDropBox() {
                return enable(StrictMode.PENALTY_DROPBOX);
            }

            private Builder enable(int bit) {
                this.mMask |= bit;
                return this;
            }

            public VmPolicy build() {
                if (this.mMask != 0 && (this.mMask & 2555904) == 0) {
                    penaltyLog();
                }
                return new VmPolicy(this.mMask, this.mClassInstanceLimit != null ? this.mClassInstanceLimit : StrictMode.EMPTY_CLASS_LIMIT_MAP, null);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.StrictMode.VmPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.StrictMode.VmPolicy.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.VmPolicy.<clinit>():void");
        }

        /* synthetic */ VmPolicy(int mask, HashMap classInstanceLimit, VmPolicy vmPolicy) {
            this(mask, classInstanceLimit);
        }

        private VmPolicy(int mask, HashMap<Class, Integer> classInstanceLimit) {
            if (classInstanceLimit == null) {
                throw new NullPointerException("classInstanceLimit == null");
            }
            this.mask = mask;
            this.classInstanceLimit = classInstanceLimit;
        }

        public String toString() {
            return "[StrictMode.VmPolicy; mask=" + this.mask + "]";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.StrictMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.StrictMode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.<clinit>():void");
    }

    private StrictMode() {
    }

    public static void setThreadPolicy(ThreadPolicy policy) {
        setThreadPolicyMask(policy.mask);
    }

    private static void setThreadPolicyMask(int policyMask) {
        setBlockGuardPolicy(policyMask);
        Binder.setThreadStrictModePolicy(policyMask);
    }

    private static void setBlockGuardPolicy(int policyMask) {
        if (policyMask == 0) {
            BlockGuard.setThreadPolicy(BlockGuard.LAX_POLICY);
            return;
        }
        AndroidBlockGuardPolicy androidPolicy;
        Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            androidPolicy = (AndroidBlockGuardPolicy) policy;
        } else {
            androidPolicy = (AndroidBlockGuardPolicy) threadAndroidPolicy.get();
            BlockGuard.setThreadPolicy(androidPolicy);
        }
        androidPolicy.setPolicyMask(policyMask);
    }

    private static void setCloseGuardEnabled(boolean enabled) {
        if (!(CloseGuard.getReporter() instanceof AndroidCloseGuardReporter)) {
            CloseGuard.setReporter(new AndroidCloseGuardReporter());
        }
        CloseGuard.setEnabled(enabled);
    }

    public static int getThreadPolicyMask() {
        return BlockGuard.getThreadPolicy().getPolicyMask();
    }

    public static ThreadPolicy getThreadPolicy() {
        return new ThreadPolicy(getThreadPolicyMask(), null);
    }

    public static ThreadPolicy allowThreadDiskWrites() {
        int oldPolicyMask = getThreadPolicyMask();
        int newPolicyMask = oldPolicyMask & -4;
        if (newPolicyMask != oldPolicyMask) {
            setThreadPolicyMask(newPolicyMask);
        }
        return new ThreadPolicy(oldPolicyMask, null);
    }

    public static ThreadPolicy allowThreadDiskReads() {
        int oldPolicyMask = getThreadPolicyMask();
        int newPolicyMask = oldPolicyMask & -3;
        if (newPolicyMask != oldPolicyMask) {
            setThreadPolicyMask(newPolicyMask);
        }
        return new ThreadPolicy(oldPolicyMask, null);
    }

    private static boolean amTheSystemServerProcess() {
        if (Process.myUid() != Process.SYSTEM_UID) {
            return LOG_V;
        }
        Throwable stack = new Throwable();
        stack.fillInStackTrace();
        StackTraceElement[] stackTrace = stack.getStackTrace();
        int length = stackTrace.length;
        for (int i = NETWORK_POLICY_ACCEPT; i < length; i += NETWORK_POLICY_LOG) {
            String clsName = stackTrace[i].getClassName();
            if (clsName != null && clsName.startsWith("com.android.server.")) {
                return true;
            }
        }
        return LOG_V;
    }

    public static boolean conditionallyEnableDebugLogging() {
        boolean doFlashes = SystemProperties.getBoolean(VISUAL_PROPERTY, LOG_V) ? amTheSystemServerProcess() ? LOG_V : true : LOG_V;
        boolean suppress = SystemProperties.getBoolean(DISABLE_PROPERTY, LOG_V);
        if (doFlashes || !(IS_USER_BUILD || suppress)) {
            int threadPolicyMask = 7;
            if (!IS_USER_BUILD) {
                threadPolicyMask = 2097159;
            }
            if (doFlashes) {
                threadPolicyMask |= PENALTY_FLASH;
            }
            setThreadPolicyMask(threadPolicyMask);
            if (IS_USER_BUILD) {
                setCloseGuardEnabled(LOG_V);
            } else {
                Builder policyBuilder = new Builder().detectAll().penaltyDropBox();
                if (IS_ENG_BUILD) {
                    policyBuilder.penaltyLog();
                }
                setVmPolicy(policyBuilder.build());
                setCloseGuardEnabled(vmClosableObjectLeaksEnabled());
            }
            return true;
        }
        setCloseGuardEnabled(LOG_V);
        return LOG_V;
    }

    public static void enableDeathOnNetwork() {
        setThreadPolicyMask((getThreadPolicyMask() | DETECT_NETWORK) | PENALTY_DEATH_ON_NETWORK);
    }

    public static void enableDeathOnFileUriExposure() {
        sVmPolicyMask |= 67117056;
    }

    public static void disableDeathOnFileUriExposure() {
        sVmPolicyMask &= -67117057;
    }

    private static int parsePolicyFromMessage(String message) {
        if (message == null || !message.startsWith("policy=")) {
            return NETWORK_POLICY_ACCEPT;
        }
        int spaceIndex = message.indexOf(32);
        if (spaceIndex == -1) {
            return NETWORK_POLICY_ACCEPT;
        }
        try {
            return Integer.parseInt(message.substring(7, spaceIndex));
        } catch (NumberFormatException e) {
            return NETWORK_POLICY_ACCEPT;
        }
    }

    private static int parseViolationFromMessage(String message) {
        if (message == null) {
            return NETWORK_POLICY_ACCEPT;
        }
        int violationIndex = message.indexOf("violation=");
        if (violationIndex == -1) {
            return NETWORK_POLICY_ACCEPT;
        }
        int numberStartIndex = violationIndex + "violation=".length();
        int numberEndIndex = message.indexOf(32, numberStartIndex);
        if (numberEndIndex == -1) {
            numberEndIndex = message.length();
        }
        try {
            return Integer.parseInt(message.substring(numberStartIndex, numberEndIndex));
        } catch (NumberFormatException e) {
            return NETWORK_POLICY_ACCEPT;
        }
    }

    private static boolean tooManyViolationsThisLoop() {
        return ((ArrayList) violationsBeingTimed.get()).size() >= MAX_OFFENSES_PER_LOOP ? true : LOG_V;
    }

    private static void executeDeathPenalty(ViolationInfo info) {
        throw new StrictModeViolation(info.policy, parseViolationFromMessage(info.crashInfo.exceptionMessage), null);
    }

    private static void dropboxViolationAsync(int violationMaskSubset, ViolationInfo info) {
        int outstanding = sDropboxCallsInFlight.incrementAndGet();
        if (outstanding > MAX_SPAN_TAGS) {
            sDropboxCallsInFlight.decrementAndGet();
            return;
        }
        if (LOG_V) {
            Log.d(TAG, "Dropboxing async; in-flight=" + outstanding);
        }
        new AnonymousClass9("callActivityManagerForStrictModeDropbox", violationMaskSubset, info).start();
    }

    static boolean hasGatheredViolations() {
        return gatheredViolations.get() != null ? true : LOG_V;
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
            long[] instanceCounts = VMDebug.countInstancesOfClasses(classes, LOG_V);
            for (int i = NETWORK_POLICY_ACCEPT; i < classes.length; i += NETWORK_POLICY_LOG) {
                Class klass = classes[i];
                int limit = ((Integer) policy.classInstanceLimit.get(klass)).intValue();
                long instances = instanceCounts[i];
                if (instances > ((long) limit)) {
                    Throwable tr = new InstanceCountViolation(klass, instances, limit);
                    onVmPolicyViolation(tr.getMessage(), tr);
                }
            }
        }
    }

    public static void setVmPolicy(VmPolicy policy) {
        synchronized (StrictMode.class) {
            sVmPolicy = policy;
            sVmPolicyMask = policy.mask;
            setCloseGuardEnabled(vmClosableObjectLeaksEnabled());
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                MessageQueue mq = looper.mQueue;
                if (policy.classInstanceLimit.size() == 0 || (sVmPolicyMask & VM_PENALTY_MASK) == 0) {
                    mq.removeIdleHandler(sProcessIdleHandler);
                    sIsIdlerRegistered = LOG_V;
                } else if (!sIsIdlerRegistered) {
                    mq.addIdleHandler(sProcessIdleHandler);
                    sIsIdlerRegistered = true;
                }
            }
            int networkPolicy = NETWORK_POLICY_ACCEPT;
            if ((sVmPolicyMask & DETECT_VM_CLEARTEXT_NETWORK) != 0) {
                if ((sVmPolicyMask & PENALTY_DEATH) == 0 && (sVmPolicyMask & PENALTY_DEATH_ON_CLEARTEXT_NETWORK) == 0) {
                    networkPolicy = NETWORK_POLICY_LOG;
                } else {
                    networkPolicy = NETWORK_POLICY_REJECT;
                }
            }
            INetworkManagementService netd = Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
            if (netd != null) {
                try {
                    netd.setUidCleartextNetworkPolicy(Process.myUid(), networkPolicy);
                } catch (RemoteException e) {
                }
            } else if (networkPolicy != 0) {
                Log.w(TAG, "Dropping requested network policy due to missing service!");
            }
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
        setThreadPolicy(new Builder().detectAll().penaltyLog().build());
        setVmPolicy(new Builder().detectAll().penaltyLog().build());
    }

    public static boolean vmSqliteObjectLeaksEnabled() {
        return (sVmPolicyMask & DETECT_VM_CURSOR_LEAKS) != 0 ? true : LOG_V;
    }

    public static boolean vmClosableObjectLeaksEnabled() {
        return (sVmPolicyMask & DETECT_VM_CLOSABLE_LEAKS) != 0 ? true : LOG_V;
    }

    public static boolean vmRegistrationLeaksEnabled() {
        return (sVmPolicyMask & DETECT_VM_REGISTRATION_LEAKS) != 0 ? true : LOG_V;
    }

    public static boolean vmFileUriExposureEnabled() {
        return (sVmPolicyMask & DETECT_VM_FILE_URI_EXPOSURE) != 0 ? true : LOG_V;
    }

    public static boolean vmCleartextNetworkEnabled() {
        return (sVmPolicyMask & DETECT_VM_CLEARTEXT_NETWORK) != 0 ? true : LOG_V;
    }

    public static void onSqliteObjectLeaked(String message, Throwable originStack) {
        onVmPolicyViolation(message, originStack);
    }

    public static void onWebViewMethodCalledOnWrongThread(Throwable originStack) {
        onVmPolicyViolation(null, originStack);
    }

    public static void onIntentReceiverLeaked(Throwable originStack) {
        onVmPolicyViolation(null, originStack);
    }

    public static void onServiceConnectionLeaked(Throwable originStack) {
        onVmPolicyViolation(null, originStack);
    }

    public static void onFileUriExposed(Uri uri, String location) {
        String message = uri + " exposed beyond app through " + location;
        if ((sVmPolicyMask & PENALTY_DEATH_ON_FILE_URI_EXPOSURE) != 0) {
            throw new FileUriExposedException(message);
        }
        onVmPolicyViolation(null, new Throwable(message));
    }

    public static void onCleartextNetworkDetected(byte[] firstPacket) {
        byte[] rawAddr = null;
        if (firstPacket != null) {
            if (firstPacket.length >= MAX_SPAN_TAGS && (firstPacket[NETWORK_POLICY_ACCEPT] & NetworkPolicyManager.MASK_ALL_NETWORKS) == 64) {
                rawAddr = new byte[DETECT_NETWORK];
                System.arraycopy(firstPacket, DETECT_RESOURCE_MISMATCH, rawAddr, NETWORK_POLICY_ACCEPT, DETECT_NETWORK);
            } else if (firstPacket.length >= 40 && (firstPacket[NETWORK_POLICY_ACCEPT] & NetworkPolicyManager.MASK_ALL_NETWORKS) == 96) {
                rawAddr = new byte[DETECT_RESOURCE_MISMATCH];
                System.arraycopy(firstPacket, 24, rawAddr, NETWORK_POLICY_ACCEPT, DETECT_RESOURCE_MISMATCH);
            }
        }
        int uid = Process.myUid();
        String msg = "Detected cleartext network traffic from UID " + uid;
        if (rawAddr != null) {
            try {
                msg = "Detected cleartext network traffic from UID " + uid + " to " + InetAddress.getByAddress(rawAddr);
            } catch (UnknownHostException e) {
            }
        }
        onVmPolicyViolation(HexDump.dumpHexString(firstPacket).trim(), new Throwable(msg), (sVmPolicyMask & PENALTY_DEATH_ON_CLEARTEXT_NETWORK) != 0 ? true : LOG_V);
    }

    public static void onVmPolicyViolation(String message, Throwable originStack) {
        onVmPolicyViolation(message, originStack, LOG_V);
    }

    public static void onVmPolicyViolation(String message, Throwable originStack, boolean forceDeath) {
        boolean penaltyDropbox = (sVmPolicyMask & PENALTY_DROPBOX) != 0 ? true : LOG_V;
        boolean z = (sVmPolicyMask & PENALTY_DEATH) == 0 ? forceDeath : true;
        boolean penaltyLog = (sVmPolicyMask & PENALTY_LOG) != 0 ? true : LOG_V;
        ViolationInfo info = new ViolationInfo(message, originStack, sVmPolicyMask);
        info.numAnimationsRunning = NETWORK_POLICY_ACCEPT;
        info.tags = null;
        info.broadcastIntentAction = null;
        Integer fingerprint = Integer.valueOf(info.hashCode());
        long now = SystemClock.uptimeMillis();
        long lastViolationTime = 0;
        long timeSinceLastViolationMillis = LinkQualityInfo.UNKNOWN_LONG;
        synchronized (sLastVmViolationTime) {
            if (sLastVmViolationTime.containsKey(fingerprint)) {
                lastViolationTime = ((Long) sLastVmViolationTime.get(fingerprint)).longValue();
                timeSinceLastViolationMillis = now - lastViolationTime;
            }
            if (timeSinceLastViolationMillis > MIN_LOG_INTERVAL_MS) {
                sLastVmViolationTime.put(fingerprint, Long.valueOf(now));
            }
        }
        if (penaltyLog && timeSinceLastViolationMillis > MIN_LOG_INTERVAL_MS) {
            Log.e(TAG, message, originStack);
        }
        int violationMaskSubset = PENALTY_DROPBOX | (sVmPolicyMask & ALL_VM_DETECT_BITS);
        if (!penaltyDropbox || z) {
            if (penaltyDropbox && lastViolationTime == 0) {
                int savedPolicyMask = getThreadPolicyMask();
                try {
                    setThreadPolicyMask(NETWORK_POLICY_ACCEPT);
                    ActivityManagerNative.getDefault().handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
                    setThreadPolicyMask(savedPolicyMask);
                } catch (RemoteException e) {
                    if (!(e instanceof DeadObjectException)) {
                        Log.e(TAG, "RemoteException trying to handle StrictMode violation", e);
                    }
                    setThreadPolicyMask(savedPolicyMask);
                } catch (Throwable th) {
                    setThreadPolicyMask(savedPolicyMask);
                }
            }
            if (z) {
                System.err.println("StrictMode VmPolicy violation with POLICY_DEATH; shutting down.");
                Process.killProcess(Process.myPid());
                System.exit(MAX_OFFENSES_PER_LOOP);
            }
            return;
        }
        dropboxViolationAsync(violationMaskSubset, info);
    }

    static void writeGatheredViolationsToParcel(Parcel p) {
        ArrayList<ViolationInfo> violations = (ArrayList) gatheredViolations.get();
        if (violations == null) {
            p.writeInt(NETWORK_POLICY_ACCEPT);
        } else {
            p.writeInt(violations.size());
            for (int i = NETWORK_POLICY_ACCEPT; i < violations.size(); i += NETWORK_POLICY_LOG) {
                int start = p.dataPosition();
                ((ViolationInfo) violations.get(i)).writeToParcel(p, NETWORK_POLICY_ACCEPT);
                if (p.dataPosition() - start > GLES20.GL_TEXTURE_MAG_FILTER) {
                    Slog.d(TAG, "Wrote violation #" + i + " of " + violations.size() + ": " + (p.dataPosition() - start) + " bytes");
                }
            }
            if (LOG_V) {
                Log.d(TAG, "wrote violations to response parcel; num=" + violations.size());
            }
            violations.clear();
        }
        gatheredViolations.set(null);
    }

    static void readAndHandleBinderCallViolations(Parcel p) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, LOG_V, DETECT_VM_CURSOR_LEAKS);
        new LogStackTrace().printStackTrace(pw);
        pw.flush();
        String ourStack = sw.toString();
        int policyMask = getThreadPolicyMask();
        boolean currentlyGathering = (PENALTY_GATHER & policyMask) != 0 ? true : LOG_V;
        int numViolations = p.readInt();
        int i = NETWORK_POLICY_ACCEPT;
        while (i < numViolations) {
            if (LOG_V) {
                Log.d(TAG, "strict mode violation stacks read from binder call.  i=" + i);
            }
            ViolationInfo info = new ViolationInfo(p, currentlyGathering ? LOG_V : true);
            if (info.crashInfo.stackTrace == null || info.crashInfo.stackTrace.length() <= HealthKeys.BASE_PROCESS) {
                CrashInfo crashInfo = info.crashInfo;
                crashInfo.stackTrace += "# via Binder call with stack:\n" + ourStack;
                Policy policy = BlockGuard.getThreadPolicy();
                if (policy instanceof AndroidBlockGuardPolicy) {
                    ((AndroidBlockGuardPolicy) policy).handleViolationWithTimingAttempt(info);
                }
                i += NETWORK_POLICY_LOG;
            } else {
                String front = info.crashInfo.stackTrace.substring(NETWORK_POLICY_ACCEPT, DETECT_VM_CURSOR_LEAKS);
                for (i += NETWORK_POLICY_LOG; i < numViolations; i += NETWORK_POLICY_LOG) {
                    info = new ViolationInfo(p, currentlyGathering ? LOG_V : true);
                }
                clearGatheredViolations();
                Slog.wtfStack(TAG, "Stack is too large: numViolations=" + numViolations + " policy=#" + Integer.toHexString(policyMask) + " front=" + front);
                return;
            }
        }
    }

    private static void onBinderStrictModePolicyChange(int newPolicy) {
        setBlockGuardPolicy(newPolicy);
    }

    public static Span enterCriticalSpan(String name) {
        if (IS_USER_BUILD) {
            return NO_OP_SPAN;
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must be non-null and non-empty");
        }
        Span span;
        ThreadSpanState state = (ThreadSpanState) sThisThreadSpanState.get();
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
            state.mActiveSize += NETWORK_POLICY_LOG;
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
        Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onCustomSlowCall(name);
        }
    }

    public static void noteResourceMismatch(Object tag) {
        Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onResourceMismatch(tag);
        }
    }

    public static void noteDiskRead() {
        Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onReadFromDisk();
        }
    }

    public static void noteDiskWrite() {
        Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onWriteToDisk();
        }
    }

    public static Object trackActivity(Object instance) {
        return new InstanceTracker(instance);
    }

    public static void incrementExpectedActivityCount(Class klass) {
        if (klass != null) {
            synchronized (StrictMode.class) {
                if ((sVmPolicy.mask & DETECT_VM_ACTIVITY_LEAKS) == 0) {
                    return;
                }
                int i;
                Integer expected = (Integer) sExpectedActivityInstanceCount.get(klass);
                if (expected == null) {
                    i = NETWORK_POLICY_LOG;
                } else {
                    i = expected.intValue() + NETWORK_POLICY_LOG;
                }
                sExpectedActivityInstanceCount.put(klass, Integer.valueOf(i));
            }
        }
    }

    public static void decrementExpectedActivityCount(Class klass) {
        if (klass != null) {
            synchronized (StrictMode.class) {
                if ((sVmPolicy.mask & DETECT_VM_ACTIVITY_LEAKS) == 0) {
                    return;
                }
                Integer expected = (Integer) sExpectedActivityInstanceCount.get(klass);
                int newExpected = ART_OPT_ENABLE ? (expected == null || expected.intValue() == 0) ? NETWORK_POLICY_ACCEPT : expected.intValue() : (expected == null || expected.intValue() == 0) ? NETWORK_POLICY_ACCEPT : expected.intValue() - 1;
                if (newExpected == 0) {
                    sExpectedActivityInstanceCount.remove(klass);
                } else {
                    sExpectedActivityInstanceCount.put(klass, Integer.valueOf(newExpected));
                }
                int limit = newExpected + NETWORK_POLICY_LOG;
                int actual = InstanceTracker.getInstanceCount(klass);
                if (ART_OPT_ENABLE) {
                    if (actual <= limit && actual <= MAX_OFFENSES_PER_LOOP) {
                        return;
                    }
                } else if (actual <= limit) {
                    return;
                }
                System.gc();
                System.runFinalization();
                System.gc();
                long instances = VMDebug.countInstancesOfClass(klass, LOG_V);
                if (instances > ((long) limit)) {
                    Throwable tr = new InstanceCountViolation(klass, instances, limit);
                    onVmPolicyViolation(tr.getMessage(), tr);
                }
            }
        }
    }
}
