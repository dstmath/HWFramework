package android.os;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.MessageQueue.IdleHandler;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Printer;
import android.util.Singleton;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.HexDump;
import dalvik.system.BlockGuard;
import dalvik.system.BlockGuard.BlockGuardPolicyException;
import dalvik.system.BlockGuard.Policy;
import dalvik.system.CloseGuard;
import dalvik.system.CloseGuard.Reporter;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class StrictMode {
    private static final int ALL_THREAD_DETECT_BITS = 63;
    private static final int ALL_VM_DETECT_BITS = -2147418368;
    public static final boolean ART_OPT_ENABLE = SystemProperties.getBoolean("persist.sys.art.opt.enable", false);
    private static final String CLEARTEXT_PROPERTY = "persist.sys.strictmode.clear";
    public static final int DETECT_CUSTOM = 8;
    public static final int DETECT_DISK_READ = 2;
    public static final int DETECT_DISK_WRITE = 1;
    public static final int DETECT_NETWORK = 4;
    public static final int DETECT_RESOURCE_MISMATCH = 16;
    public static final int DETECT_UNBUFFERED_IO = 32;
    public static final int DETECT_VM_ACTIVITY_LEAKS = 1024;
    private static final int DETECT_VM_CLEARTEXT_NETWORK = 16384;
    public static final int DETECT_VM_CLOSABLE_LEAKS = 512;
    private static final int DETECT_VM_CONTENT_URI_WITHOUT_PERMISSION = 32768;
    public static final int DETECT_VM_CURSOR_LEAKS = 256;
    private static final int DETECT_VM_FILE_URI_EXPOSURE = 8192;
    private static final int DETECT_VM_INSTANCE_LEAKS = 2048;
    public static final int DETECT_VM_REGISTRATION_LEAKS = 4096;
    private static final int DETECT_VM_UNTAGGED_SOCKET = Integer.MIN_VALUE;
    public static final String DISABLE_PROPERTY = "persist.sys.strictmode.disable";
    private static final HashMap<Class, Integer> EMPTY_CLASS_LIMIT_MAP = new HashMap();
    private static final boolean IS_ENG_BUILD = "eng".equals(Build.TYPE);
    private static final boolean IS_USER_BUILD = Context.USER_SERVICE.equals(Build.TYPE);
    private static final boolean LOG_V = Log.isLoggable(TAG, 2);
    private static final int MAX_OFFENSES_PER_LOOP = 10;
    private static final int MAX_SPAN_TAGS = 20;
    private static final long MIN_DIALOG_INTERVAL_MS = 30000;
    private static final long MIN_LOG_INTERVAL_MS = 1000;
    public static final int NETWORK_POLICY_ACCEPT = 0;
    public static final int NETWORK_POLICY_LOG = 1;
    public static final int NETWORK_POLICY_REJECT = 2;
    private static final Span NO_OP_SPAN = new Span() {
        public void finish() {
        }
    };
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
    private static final ThreadLocal<ArrayList<ViolationInfo>> gatheredViolations = new ThreadLocal<ArrayList<ViolationInfo>>() {
        protected ArrayList<ViolationInfo> initialValue() {
            return null;
        }
    };
    private static final AtomicInteger sDropboxCallsInFlight = new AtomicInteger(0);
    private static final HashMap<Class, Integer> sExpectedActivityInstanceCount = new HashMap();
    private static boolean sIsIdlerRegistered = false;
    private static long sLastInstanceCountCheckMillis = 0;
    private static final HashMap<Integer, Long> sLastVmViolationTime = new HashMap();
    private static volatile ViolationListener sListener;
    private static final IdleHandler sProcessIdleHandler = new IdleHandler() {
        public boolean queueIdle() {
            long now = SystemClock.uptimeMillis();
            if (now - StrictMode.sLastInstanceCountCheckMillis > 30000) {
                StrictMode.sLastInstanceCountCheckMillis = now;
                StrictMode.conditionallyCheckInstanceCounts();
            }
            return true;
        }
    };
    private static final ThreadLocal<ThreadSpanState> sThisThreadSpanState = new ThreadLocal<ThreadSpanState>() {
        protected ThreadSpanState initialValue() {
            return new ThreadSpanState();
        }
    };
    private static volatile VmPolicy sVmPolicy = VmPolicy.LAX;
    private static volatile int sVmPolicyMask = 0;
    private static Singleton<IWindowManager> sWindowManager = new Singleton<IWindowManager>() {
        protected IWindowManager create() {
            return Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
        }
    };
    private static final ThreadLocal<AndroidBlockGuardPolicy> threadAndroidPolicy = new ThreadLocal<AndroidBlockGuardPolicy>() {
        protected AndroidBlockGuardPolicy initialValue() {
            return new AndroidBlockGuardPolicy(0);
        }
    };
    private static final ThreadLocal<Handler> threadHandler = new ThreadLocal<Handler>() {
        protected Handler initialValue() {
            return new Handler();
        }
    };
    private static final ThreadLocal<ArrayList<ViolationInfo>> violationsBeingTimed = new ThreadLocal<ArrayList<ViolationInfo>>() {
        protected ArrayList<ViolationInfo> initialValue() {
            return new ArrayList();
        }
    };

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

        /* JADX WARNING: Missing block: B:24:0x0079, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    state.mFreeListSize++;
                }
            }
        }
    }

    private static class AndroidBlockGuardPolicy implements Policy {
        private ArrayMap<Integer, Long> mLastViolationTime;
        private int mPolicyMask;

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
            if ((this.mPolicyMask & 1) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeDiskWriteViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        void onCustomSlowCall(String name) {
            if ((this.mPolicyMask & 8) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeCustomViolation(this.mPolicyMask, name);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        void onResourceMismatch(Object tag) {
            if ((this.mPolicyMask & 16) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeResourceMismatchViolation(this.mPolicyMask, tag);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onUnbufferedIO() {
            if ((this.mPolicyMask & 32) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeUnbufferedIOViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onReadFromDisk() {
            if ((this.mPolicyMask & 2) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuardPolicyException e = new StrictModeDiskReadViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onNetwork() {
            if ((this.mPolicyMask & 4) != 0) {
                if ((this.mPolicyMask & 16777216) != 0) {
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
            if (Looper.myLooper() == null || (info.policy & StrictMode.THREAD_PENALTY_MASK) == 262144) {
                info.durationMillis = -1;
                handleViolation(info);
                return;
            }
            final ArrayList<ViolationInfo> records = (ArrayList) StrictMode.violationsBeingTimed.get();
            if (records.size() < 10) {
                records.add(info);
                if (records.size() <= 1) {
                    final IWindowManager windowManager = (info.policy & 1048576) != 0 ? (IWindowManager) StrictMode.sWindowManager.get() : null;
                    final boolean isVisual = SystemProperties.getBoolean(StrictMode.VISUAL_PROPERTY, false);
                    if (windowManager != null && isVisual) {
                        try {
                            windowManager.showStrictModeViolation(true);
                        } catch (RemoteException e) {
                        }
                    }
                    ((Handler) StrictMode.threadHandler.get()).postAtFrontOfQueue(new Runnable() {
                        public void run() {
                            long loopFinishTime = SystemClock.uptimeMillis();
                            if (windowManager != null && isVisual) {
                                try {
                                    windowManager.showStrictModeViolation(false);
                                } catch (RemoteException e) {
                                }
                            }
                            for (int n = 0; n < records.size(); n++) {
                                ViolationInfo v = (ViolationInfo) records.get(n);
                                v.violationNumThisLoop = n + 1;
                                v.durationMillis = (int) (loopFinishTime - v.violationUptimeMillis);
                                AndroidBlockGuardPolicy.this.handleViolation(v);
                            }
                            records.clear();
                        }
                    });
                }
            }
        }

        void handleViolation(ViolationInfo info) {
            if (info == null || info.crashInfo == null || info.crashInfo.stackTrace == null) {
                Log.wtf(StrictMode.TAG, "unexpected null stacktrace");
                return;
            }
            if (StrictMode.LOG_V) {
                Log.d(StrictMode.TAG, "handleViolation; policy=" + info.policy);
            }
            if ((info.policy & 4194304) != 0) {
                ArrayList<ViolationInfo> violations = (ArrayList) StrictMode.gatheredViolations.get();
                if (violations == null) {
                    ArrayList<ViolationInfo> arrayList = new ArrayList(1);
                    StrictMode.gatheredViolations.set(arrayList);
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
                this.mLastViolationTime = new ArrayMap(1);
            }
            long now = SystemClock.uptimeMillis();
            this.mLastViolationTime.put(crashFingerprint, Long.valueOf(now));
            long timeSinceLastViolationMillis = lastViolationTime == 0 ? Long.MAX_VALUE : now - lastViolationTime;
            if (!((info.policy & 65536) == 0 || StrictMode.sListener == null)) {
                StrictMode.sListener.onViolation(info.crashInfo.stackTrace);
            }
            if ((info.policy & 65536) != 0 && timeSinceLastViolationMillis > StrictMode.MIN_LOG_INTERVAL_MS) {
                if (info.durationMillis != -1) {
                    Log.d(StrictMode.TAG, "StrictMode policy violation; ~duration=" + info.durationMillis + " ms: " + info.crashInfo.stackTrace);
                } else {
                    Log.d(StrictMode.TAG, "StrictMode policy violation: " + info.crashInfo.stackTrace);
                }
            }
            int violationMaskSubset = 0;
            if ((info.policy & 131072) != 0 && timeSinceLastViolationMillis > 30000) {
                violationMaskSubset = 131072;
            }
            if ((info.policy & 2097152) != 0 && lastViolationTime == 0) {
                violationMaskSubset |= 2097152;
            }
            if (violationMaskSubset != 0) {
                violationMaskSubset |= StrictMode.parseViolationFromMessage(info.crashInfo.exceptionMessage);
                int savedPolicyMask = StrictMode.getThreadPolicyMask();
                if ((info.policy & StrictMode.THREAD_PENALTY_MASK) == 2097152) {
                    StrictMode.dropboxViolationAsync(violationMaskSubset, info);
                    return;
                }
                try {
                    StrictMode.setThreadPolicyMask(0);
                    ActivityManager.getService().handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
                    StrictMode.setThreadPolicyMask(savedPolicyMask);
                } catch (RemoteException e) {
                    if (!(e instanceof DeadObjectException)) {
                        Log.e(StrictMode.TAG, "RemoteException trying to handle StrictMode violation", e);
                    }
                    StrictMode.setThreadPolicyMask(savedPolicyMask);
                } catch (Throwable th) {
                    StrictMode.setThreadPolicyMask(savedPolicyMask);
                    throw th;
                }
            }
            if ((info.policy & 262144) != 0) {
                StrictMode.executeDeathPenalty(info);
            }
        }
    }

    private static class AndroidCloseGuardReporter implements Reporter {
        /* synthetic */ AndroidCloseGuardReporter(AndroidCloseGuardReporter -this0) {
            this();
        }

        private AndroidCloseGuardReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            StrictMode.onVmPolicyViolation(message, allocationSite);
        }
    }

    private static class InstanceCountViolation extends Throwable {
        private static final StackTraceElement[] FAKE_STACK = new StackTraceElement[]{new StackTraceElement("android.os.StrictMode", "setClassInstanceLimit", "StrictMode.java", 1)};
        final Class mClass;
        final long mInstances;
        final int mLimit;

        public InstanceCountViolation(Class klass, long instances, int limit) {
            super(klass.toString() + "; instances=" + instances + "; limit=" + limit);
            setStackTrace(FAKE_STACK);
            this.mClass = klass;
            this.mInstances = instances;
            this.mLimit = limit;
        }
    }

    private static final class InstanceTracker {
        private static final HashMap<Class<?>, Integer> sInstanceCounts = new HashMap();
        private final Class<?> mKlass;

        public InstanceTracker(Object instance) {
            this.mKlass = instance.getClass();
            synchronized (sInstanceCounts) {
                Integer value = (Integer) sInstanceCounts.get(this.mKlass);
                sInstanceCounts.put(this.mKlass, Integer.valueOf(value != null ? value.intValue() + 1 : 1));
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
                intValue = value != null ? value.intValue() : 0;
            }
            return intValue;
        }
    }

    private static class LogStackTrace extends Exception {
        /* synthetic */ LogStackTrace(LogStackTrace -this0) {
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
            super(policyMask, 8, name);
        }
    }

    private static class StrictModeDiskReadViolation extends StrictModeViolation {
        public StrictModeDiskReadViolation(int policyMask) {
            super(policyMask, 2, null);
        }
    }

    private static class StrictModeDiskWriteViolation extends StrictModeViolation {
        public StrictModeDiskWriteViolation(int policyMask) {
            super(policyMask, 1, null);
        }
    }

    public static class StrictModeNetworkViolation extends StrictModeViolation {
        public StrictModeNetworkViolation(int policyMask) {
            super(policyMask, 4, null);
        }
    }

    private static class StrictModeResourceMismatchViolation extends StrictModeViolation {
        public StrictModeResourceMismatchViolation(int policyMask, Object tag) {
            String str = null;
            if (tag != null) {
                str = tag.toString();
            }
            super(policyMask, 16, str);
        }
    }

    private static class StrictModeUnbufferedIOViolation extends StrictModeViolation {
        public StrictModeUnbufferedIOViolation(int policyMask) {
            super(policyMask, 32, null);
        }
    }

    public static final class ThreadPolicy {
        public static final ThreadPolicy LAX = new ThreadPolicy(0);
        final int mask;

        public static final class Builder {
            private int mMask;

            public Builder() {
                this.mMask = 0;
                this.mMask = 0;
            }

            public Builder(ThreadPolicy policy) {
                this.mMask = 0;
                this.mMask = policy.mask;
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
                return disable(63);
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

            public Builder penaltyDialog() {
                return enable(131072);
            }

            public Builder penaltyDeath() {
                return enable(262144);
            }

            public Builder penaltyDeathOnNetwork() {
                return enable(16777216);
            }

            public Builder penaltyFlashScreen() {
                return enable(1048576);
            }

            public Builder penaltyLog() {
                return enable(65536);
            }

            public Builder penaltyDropBox() {
                return enable(2097152);
            }

            private Builder enable(int bit) {
                this.mMask |= bit;
                return this;
            }

            private Builder disable(int bit) {
                this.mMask &= ~bit;
                return this;
            }

            public ThreadPolicy build() {
                if (this.mMask != 0 && (this.mMask & 2555904) == 0) {
                    penaltyLog();
                }
                return new ThreadPolicy(this.mMask, null);
            }
        }

        /* synthetic */ ThreadPolicy(int mask, ThreadPolicy -this1) {
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

        /* synthetic */ ThreadSpanState(ThreadSpanState -this0) {
            this();
        }

        private ThreadSpanState() {
        }
    }

    public static class ViolationInfo implements Parcelable {
        public static final Creator<ViolationInfo> CREATOR = new Creator<ViolationInfo>() {
            public ViolationInfo createFromParcel(Parcel in) {
                return new ViolationInfo(in);
            }

            public ViolationInfo[] newArray(int size) {
                return new ViolationInfo[size];
            }
        };
        public String broadcastIntentAction;
        public final CrashInfo crashInfo;
        public int durationMillis;
        public final String message;
        public int numAnimationsRunning;
        public long numInstances;
        public final int policy;
        public String[] tags;
        public int violationNumThisLoop;
        public long violationUptimeMillis;

        public ViolationInfo() {
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.message = null;
            this.crashInfo = null;
            this.policy = 0;
        }

        public ViolationInfo(Throwable tr, int policy) {
            this(null, tr, policy);
        }

        public ViolationInfo(String message, Throwable tr, int policy) {
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
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
                if (spanActiveCount > 20) {
                    spanActiveCount = 20;
                }
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

        public int hashCode() {
            int result = 17;
            if (this.crashInfo != null) {
                result = this.crashInfo.stackTrace.hashCode() + 629;
            }
            if (this.numAnimationsRunning != 0) {
                result *= 37;
            }
            if (this.broadcastIntentAction != null) {
                result = (result * 37) + this.broadcastIntentAction.hashCode();
            }
            if (this.tags != null) {
                for (String tag : this.tags) {
                    result = (result * 37) + tag.hashCode();
                }
            }
            return result;
        }

        public ViolationInfo(Parcel in) {
            this(in, false);
        }

        public ViolationInfo(Parcel in, boolean unsetGatheringBit) {
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.message = in.readString();
            if (in.readInt() != 0) {
                this.crashInfo = new CrashInfo(in);
            } else {
                this.crashInfo = null;
            }
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
            if (this.crashInfo != null) {
                dest.writeInt(1);
                this.crashInfo.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            int start = dest.dataPosition();
            dest.writeInt(this.policy);
            dest.writeInt(this.durationMillis);
            dest.writeInt(this.violationNumThisLoop);
            dest.writeInt(this.numAnimationsRunning);
            dest.writeLong(this.violationUptimeMillis);
            dest.writeLong(this.numInstances);
            dest.writeString(this.broadcastIntentAction);
            dest.writeStringArray(this.tags);
            int total = dest.dataPosition() - start;
        }

        public void dump(Printer pw, String prefix) {
            int i = 0;
            if (this.crashInfo != null) {
                this.crashInfo.dump(pw, prefix);
            }
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
                int index = 0;
                while (i < length) {
                    int index2 = index + 1;
                    pw.println(prefix + "tag[" + index + "]: " + strArr[i]);
                    i++;
                    index = index2;
                }
            }
        }

        public int describeContents() {
            return 0;
        }
    }

    public interface ViolationListener {
        void onViolation(String str);
    }

    public static final class VmPolicy {
        public static final VmPolicy LAX = new VmPolicy(0, StrictMode.EMPTY_CLASS_LIMIT_MAP);
        final HashMap<Class, Integer> classInstanceLimit;
        final int mask;

        public static final class Builder {
            private HashMap<Class, Integer> mClassInstanceLimit;
            private boolean mClassInstanceLimitNeedCow;
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
            }

            public Builder setClassInstanceLimit(Class klass, int instanceLimit) {
                if (klass == null) {
                    throw new NullPointerException("klass == null");
                }
                if (this.mClassInstanceLimitNeedCow) {
                    if (this.mClassInstanceLimit.containsKey(klass) && ((Integer) this.mClassInstanceLimit.get(klass)).intValue() == instanceLimit) {
                        return this;
                    }
                    this.mClassInstanceLimitNeedCow = false;
                    this.mClassInstanceLimit = (HashMap) this.mClassInstanceLimit.clone();
                } else if (this.mClassInstanceLimit == null) {
                    this.mClassInstanceLimit = new HashMap();
                }
                this.mMask |= 2048;
                this.mClassInstanceLimit.put(klass, Integer.valueOf(instanceLimit));
                return this;
            }

            public Builder detectActivityLeaks() {
                return enable(1024);
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
                return this;
            }

            public Builder detectLeakedSqlLiteObjects() {
                return enable(256);
            }

            public Builder detectLeakedClosableObjects() {
                return enable(512);
            }

            public Builder detectLeakedRegistrationObjects() {
                return enable(4096);
            }

            public Builder detectFileUriExposure() {
                return enable(8192);
            }

            public Builder detectCleartextNetwork() {
                return enable(16384);
            }

            public Builder detectContentUriWithoutPermission() {
                return enable(32768);
            }

            public Builder detectUntaggedSockets() {
                return enable(Integer.MIN_VALUE);
            }

            public Builder penaltyDeath() {
                return enable(262144);
            }

            public Builder penaltyDeathOnCleartextNetwork() {
                return enable(33554432);
            }

            public Builder penaltyDeathOnFileUriExposure() {
                return enable(67108864);
            }

            public Builder penaltyLog() {
                return enable(65536);
            }

            public Builder penaltyDropBox() {
                return enable(2097152);
            }

            private Builder enable(int bit) {
                this.mMask |= bit;
                return this;
            }

            Builder disable(int bit) {
                this.mMask &= ~bit;
                return this;
            }

            public VmPolicy build() {
                if (this.mMask != 0 && (this.mMask & 2555904) == 0) {
                    penaltyLog();
                }
                return new VmPolicy(this.mMask, this.mClassInstanceLimit != null ? this.mClassInstanceLimit : StrictMode.EMPTY_CLASS_LIMIT_MAP, null);
            }
        }

        /* synthetic */ VmPolicy(int mask, HashMap classInstanceLimit, VmPolicy -this2) {
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

    public static void setViolationListener(ViolationListener listener) {
        sListener = listener;
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
        if (Process.myUid() != 1000) {
            return false;
        }
        Throwable stack = new Throwable();
        stack.fillInStackTrace();
        for (StackTraceElement ste : stack.getStackTrace()) {
            String clsName = ste.getClassName();
            if (clsName != null && clsName.startsWith("com.android.server.")) {
                return true;
            }
        }
        return false;
    }

    public static boolean conditionallyEnableDebugLogging() {
        int doFlashes;
        if (SystemProperties.getBoolean(VISUAL_PROPERTY, false)) {
            doFlashes = amTheSystemServerProcess() ^ 1;
        } else {
            doFlashes = 0;
        }
        boolean suppress = SystemProperties.getBoolean(DISABLE_PROPERTY, false);
        if (doFlashes == 0 && (IS_USER_BUILD || suppress)) {
            setCloseGuardEnabled(false);
            return false;
        }
        int threadPolicyMask = 7;
        if (!IS_USER_BUILD) {
            threadPolicyMask = 2097159;
        }
        if (doFlashes != 0) {
            threadPolicyMask |= 1048576;
        }
        setThreadPolicyMask(threadPolicyMask);
        if (IS_USER_BUILD) {
            setCloseGuardEnabled(false);
        } else {
            Builder policyBuilder = new Builder().detectAll();
            if (!IS_ENG_BUILD) {
                policyBuilder = policyBuilder.disable(1024);
            }
            policyBuilder = policyBuilder.penaltyDropBox();
            if (IS_ENG_BUILD) {
                policyBuilder.penaltyLog();
            }
            if (Process.myUid() < 10000) {
                policyBuilder.detectUntaggedSockets();
            }
            setVmPolicy(policyBuilder.build());
            setCloseGuardEnabled(vmClosableObjectLeaksEnabled());
        }
        return true;
    }

    public static void enableDeathOnNetwork() {
        setThreadPolicyMask((getThreadPolicyMask() | 4) | 16777216);
    }

    public static void enableDeathOnFileUriExposure() {
        sVmPolicyMask |= 67117056;
    }

    public static void disableDeathOnFileUriExposure() {
        sVmPolicyMask &= -67117057;
    }

    private static int parsePolicyFromMessage(String message) {
        if (message == null || (message.startsWith("policy=") ^ 1) != 0) {
            return 0;
        }
        int spaceIndex = message.indexOf(32);
        if (spaceIndex == -1) {
            return 0;
        }
        try {
            return Integer.parseInt(message.substring(7, spaceIndex));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseViolationFromMessage(String message) {
        if (message == null) {
            return 0;
        }
        int violationIndex = message.indexOf("violation=");
        if (violationIndex == -1) {
            return 0;
        }
        int numberStartIndex = violationIndex + "violation=".length();
        int numberEndIndex = message.indexOf(32, numberStartIndex);
        if (numberEndIndex == -1) {
            numberEndIndex = message.length();
        }
        try {
            return Integer.parseInt(message.substring(numberStartIndex, numberEndIndex));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean tooManyViolationsThisLoop() {
        return ((ArrayList) violationsBeingTimed.get()).size() >= 10;
    }

    private static void executeDeathPenalty(ViolationInfo info) {
        throw new StrictModeViolation(info.policy, parseViolationFromMessage(info.crashInfo.exceptionMessage), null);
    }

    private static void dropboxViolationAsync(final int violationMaskSubset, final ViolationInfo info) {
        int outstanding = sDropboxCallsInFlight.incrementAndGet();
        if (outstanding > 20) {
            sDropboxCallsInFlight.decrementAndGet();
            return;
        }
        if (LOG_V) {
            Log.d(TAG, "Dropboxing async; in-flight=" + outstanding);
        }
        new Thread("callActivityManagerForStrictModeDropbox") {
            public void run() {
                Process.setThreadPriority(10);
                try {
                    IActivityManager am = ActivityManager.getService();
                    if (am == null) {
                        Log.d(StrictMode.TAG, "No activity manager; failed to Dropbox violation.");
                    } else {
                        am.handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
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
        }.start();
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
                    sIsIdlerRegistered = false;
                } else if (!sIsIdlerRegistered) {
                    mq.addIdleHandler(sProcessIdleHandler);
                    sIsIdlerRegistered = true;
                }
            }
            int networkPolicy = 0;
            if ((sVmPolicyMask & 16384) != 0) {
                if ((sVmPolicyMask & 262144) == 0 && (sVmPolicyMask & 33554432) == 0) {
                    networkPolicy = 1;
                } else {
                    networkPolicy = 2;
                }
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
        return (sVmPolicyMask & 256) != 0;
    }

    public static boolean vmClosableObjectLeaksEnabled() {
        return (sVmPolicyMask & 512) != 0;
    }

    public static boolean vmRegistrationLeaksEnabled() {
        return (sVmPolicyMask & 4096) != 0;
    }

    public static boolean vmFileUriExposureEnabled() {
        return (sVmPolicyMask & 8192) != 0;
    }

    public static boolean vmCleartextNetworkEnabled() {
        return (sVmPolicyMask & 16384) != 0;
    }

    public static boolean vmContentUriWithoutPermissionEnabled() {
        return (sVmPolicyMask & 32768) != 0;
    }

    public static boolean vmUntaggedSocketEnabled() {
        return (sVmPolicyMask & Integer.MIN_VALUE) != 0;
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
        if ((sVmPolicyMask & 67108864) != 0) {
            throw new FileUriExposedException(message);
        }
        onVmPolicyViolation(null, new Throwable(message));
    }

    public static void onContentUriWithoutPermission(Uri uri, String location) {
        onVmPolicyViolation(null, new Throwable(uri + " exposed beyond app through " + location + " without permission grant flags; did you forget" + " FLAG_GRANT_READ_URI_PERMISSION?"));
    }

    public static void onCleartextNetworkDetected(byte[] firstPacket) {
        byte[] rawAddr = null;
        if (firstPacket != null) {
            if (firstPacket.length >= 20 && (firstPacket[0] & 240) == 64) {
                rawAddr = new byte[4];
                System.arraycopy(firstPacket, 16, rawAddr, 0, 4);
            } else if (firstPacket.length >= 40 && (firstPacket[0] & 240) == 96) {
                rawAddr = new byte[16];
                System.arraycopy(firstPacket, 24, rawAddr, 0, 16);
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
        onVmPolicyViolation(HexDump.dumpHexString(firstPacket).trim(), new Throwable(msg), (sVmPolicyMask & 33554432) != 0);
    }

    public static void onUntaggedSocket() {
        onVmPolicyViolation(null, new Throwable("Untagged socket detected; use TrafficStats.setThreadSocketTag() to track all network usage"));
    }

    public static void onVmPolicyViolation(String message, Throwable originStack) {
        onVmPolicyViolation(message, originStack, false);
    }

    public static void onVmPolicyViolation(String message, Throwable originStack, boolean forceDeath) {
        boolean penaltyDropbox = (sVmPolicyMask & 2097152) != 0;
        int penaltyDeath = (sVmPolicyMask & 262144) == 0 ? forceDeath : 1;
        boolean penaltyLog = (sVmPolicyMask & 65536) != 0;
        ViolationInfo info = new ViolationInfo(message, originStack, sVmPolicyMask);
        info.numAnimationsRunning = 0;
        info.tags = null;
        info.broadcastIntentAction = null;
        Integer fingerprint = Integer.valueOf(info.hashCode());
        long now = SystemClock.uptimeMillis();
        long lastViolationTime = 0;
        long timeSinceLastViolationMillis = Long.MAX_VALUE;
        synchronized (sLastVmViolationTime) {
            try {
                if (sLastVmViolationTime.containsKey(fingerprint)) {
                    lastViolationTime = ((Long) sLastVmViolationTime.get(fingerprint)).longValue();
                    timeSinceLastViolationMillis = now - lastViolationTime;
                }
                if (timeSinceLastViolationMillis > MIN_LOG_INTERVAL_MS) {
                    sLastVmViolationTime.put(fingerprint, Long.valueOf(now));
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        if (penaltyLog && sListener != null) {
            sListener.onViolation(originStack.toString());
        }
        if (penaltyLog && timeSinceLastViolationMillis > MIN_LOG_INTERVAL_MS) {
            Log.e(TAG, message, originStack);
        }
        int violationMaskSubset = 2097152 | (sVmPolicyMask & ALL_VM_DETECT_BITS);
        if (!penaltyDropbox || (penaltyDeath ^ 1) == 0) {
            if (penaltyDropbox && lastViolationTime == 0) {
                int savedPolicyMask = getThreadPolicyMask();
                try {
                    setThreadPolicyMask(0);
                    ActivityManager.getService().handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
                    setThreadPolicyMask(savedPolicyMask);
                } catch (RemoteException e) {
                    if (!(e instanceof DeadObjectException)) {
                        Log.e(TAG, "RemoteException trying to handle StrictMode violation", e);
                    }
                    setThreadPolicyMask(savedPolicyMask);
                } catch (Throwable th2) {
                    setThreadPolicyMask(savedPolicyMask);
                    throw th2;
                }
            }
            if (penaltyDeath != 0) {
                System.err.println("StrictMode VmPolicy violation with POLICY_DEATH; shutting down.");
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
            return;
        }
        dropboxViolationAsync(violationMaskSubset, info);
    }

    static void writeGatheredViolationsToParcel(Parcel p) {
        ArrayList<ViolationInfo> violations = (ArrayList) gatheredViolations.get();
        if (violations == null) {
            p.writeInt(0);
        } else {
            int size = Math.min(violations.size(), 3);
            p.writeInt(size);
            for (int i = 0; i < size; i++) {
                ((ViolationInfo) violations.get(i)).writeToParcel(p, 0);
            }
        }
        gatheredViolations.set(null);
    }

    static void readAndHandleBinderCallViolations(Parcel p) {
        StringWriter sw = new StringWriter();
        sw.append("# via Binder call with stack:\n");
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        new LogStackTrace().printStackTrace(pw);
        pw.flush();
        String ourStack = sw.toString();
        boolean currentlyGathering = (4194304 & getThreadPolicyMask()) != 0;
        int size = p.readInt();
        for (int i = 0; i < size; i++) {
            ViolationInfo info = new ViolationInfo(p, currentlyGathering ^ 1);
            info.crashInfo.appendStackTrace(ourStack);
            Policy policy = BlockGuard.getThreadPolicy();
            if (policy instanceof AndroidBlockGuardPolicy) {
                ((AndroidBlockGuardPolicy) policy).handleViolationWithTimingAttempt(info);
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

    public static void noteUnbufferedIO() {
        Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onUnbufferedIO();
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
                if ((sVmPolicy.mask & 1024) == 0) {
                    return;
                }
                int i;
                Integer expected = (Integer) sExpectedActivityInstanceCount.get(klass);
                if (expected == null) {
                    i = 1;
                } else {
                    i = expected.intValue() + 1;
                }
                sExpectedActivityInstanceCount.put(klass, Integer.valueOf(i));
            }
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0031, code:
            r0 = android.os.StrictMode.InstanceTracker.getInstanceCount(r11);
     */
    /* JADX WARNING: Missing block: B:23:0x0037, code:
            if (ART_OPT_ENABLE == false) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:24:0x0039, code:
            if (r0 > r4) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:26:0x003d, code:
            if (r0 > 10) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:27:0x003f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x0063, code:
            if (r0 > r4) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:40:0x0065, code:
            return;
     */
    /* JADX WARNING: Missing block: B:41:0x0066, code:
            java.lang.System.gc();
            java.lang.System.runFinalization();
            java.lang.System.gc();
            r2 = dalvik.system.VMDebug.countInstancesOfClass(r11, false);
     */
    /* JADX WARNING: Missing block: B:42:0x0076, code:
            if (r2 <= ((long) r4)) goto L_0x0084;
     */
    /* JADX WARNING: Missing block: B:43:0x0078, code:
            r6 = new android.os.StrictMode.InstanceCountViolation(r11, r2, r4);
            onVmPolicyViolation(r6.getMessage(), r6);
     */
    /* JADX WARNING: Missing block: B:44:0x0084, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void decrementExpectedActivityCount(Class klass) {
        if (klass != null) {
            synchronized (StrictMode.class) {
                if ((sVmPolicy.mask & 1024) == 0) {
                    return;
                }
                Integer expected = (Integer) sExpectedActivityInstanceCount.get(klass);
                int newExpected = ART_OPT_ENABLE ? (expected == null || expected.intValue() == 0) ? 0 : expected.intValue() : (expected == null || expected.intValue() == 0) ? 0 : expected.intValue() - 1;
                if (newExpected == 0) {
                    sExpectedActivityInstanceCount.remove(klass);
                } else {
                    sExpectedActivityInstanceCount.put(klass, Integer.valueOf(newExpected));
                }
                int limit = newExpected + 1;
            }
        }
    }
}
