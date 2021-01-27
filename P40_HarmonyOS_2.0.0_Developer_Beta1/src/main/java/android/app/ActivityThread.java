package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.backup.BackupAgent;
import android.app.backup.FullBackup;
import android.app.job.JobInfo;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ActivityRelaunchItem;
import android.app.servertransaction.ActivityResultItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.ClientTransactionItem;
import android.app.servertransaction.PendingTransactionActions;
import android.app.servertransaction.TransactionExecutor;
import android.app.servertransaction.TransactionExecutorHelper;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.AutofillOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.contentsensor.ContentSensorManagerFactory;
import android.contentsensor.IContentSensorManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDebug;
import android.ddm.DdmHandleAppName;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.graphics.fonts.SystemFonts;
import android.hardware.display.DisplayManagerGlobal;
import android.hwtheme.HwThemeManager;
import android.iawareperf.IHwRtgSchedImpl;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Proxy;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Debug;
import android.os.Environment;
import android.os.FileUtils;
import android.os.GraphicsEnvironment;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBlockMonitor;
import android.os.ICancellationSignal;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.BlockedNumberContract;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.DeviceConfig;
import android.provider.Downloads;
import android.provider.FontsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.renderscript.RenderScriptCacheDir;
import android.rms.iaware.IAwareSdk;
import android.scrollerboostmanager.ScrollerBoostManager;
import android.security.NetworkSecurityPolicy;
import android.security.net.config.NetworkSecurityConfigProvider;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructStat;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SuperNotCalledException;
import android.util.UtilConfig;
import android.util.proto.ProtoOutputStream;
import android.view.Choreographer;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.IHwViewRootImpl;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewManager;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.webkit.WebView;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.RuntimeInit;
import com.android.internal.os.SomeArgs;
import com.android.internal.os.ZygoteInit;
import com.android.internal.policy.AbsWindow;
import com.android.internal.policy.DecorView;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.internal.widget.DecorCaptionView;
import com.android.org.conscrypt.OpenSSLSocketImpl;
import com.android.org.conscrypt.TrustedCertificateStore;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.view.HwWindowManager;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.CloseGuard;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import huawei.android.app.ActivityThreadAdapterEx;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import libcore.io.ForwardingOs;
import libcore.io.IoUtils;
import libcore.io.Os;
import libcore.net.event.NetworkEventDispatcher;
import org.apache.harmony.dalvik.ddmc.DdmVmInternal;

public final class ActivityThread extends ClientTransactionHandler {
    private static final int ACTIVITY_PROCESS_ARGS = 3;
    private static final int ACTIVITY_THREAD_CHECKIN_VERSION = 4;
    private static final long CONTENT_PROVIDER_RETAIN_TIME = 1000;
    private static final boolean DEBUG_BACKUP = false;
    public static final boolean DEBUG_BROADCAST = DEBUG_HW_BROADCAST;
    public static final boolean DEBUG_CONFIGURATION;
    static final boolean DEBUG_HW_ACTIVITY = ams_log_switch.contains(Context.ACTIVITY_SERVICE);
    static final boolean DEBUG_HW_BROADCAST = ams_log_switch.contains("broadcast");
    static final boolean DEBUG_HW_PROVIDER = ams_log_switch.contains("provider");
    static final boolean DEBUG_HW_SERVICE = ams_log_switch.contains("service");
    public static final boolean DEBUG_MEMORY_TRIM = false;
    static final boolean DEBUG_MESSAGES = false;
    public static final boolean DEBUG_ORDER;
    private static final boolean DEBUG_PROVIDER = DEBUG_HW_PROVIDER;
    private static final boolean DEBUG_RESULTS;
    private static final boolean DEBUG_SERVICE = DEBUG_HW_SERVICE;
    private static final int GIVEN_DIFFERENT_DIRECTION_INFO = 3;
    private static long HANDLER_BINDER_DURATION_TIME = JobInfo.MIN_BACKOFF_MILLIS;
    private static final String HEAP_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s";
    private static final String HEAP_FULL_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s %8s %8s %8s";
    public static final long INVALID_PROC_STATE_SEQ = -1;
    static final boolean IS_DEBUG_VERSION = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static final int LOAD_CYCLE_PATTERN_DELAY = 0;
    private static final int LOG_LIMIT = 4000;
    private static final int MAGIC_PROCESS_ARGS = 5;
    private static final long MIN_TIME_BETWEEN_GCS = 5000;
    private static final String ONE_COUNT_COLUMN = "%21s %8d";
    private static final String ONE_COUNT_COLUMN_HEADER = "%21s %8s";
    private static final int OTHER_PROCESS_ARGS = 1;
    private static final long PENDING_TOP_PROCESS_STATE_TIMEOUT = 1000;
    private static final int PRELOAD_ACCEPT = 2;
    private static final int PRELOAD_CONNECT = 3;
    private static final int PRELOAD_INIT = 1;
    private static final int PROCESS_PRELOADED = 2;
    private static final int PROCESS_PRELOADING = 1;
    private static final int PROCESS_STARTED = 3;
    public static final String PROC_START_PRELOADSTATUS = "preloadstatus=";
    public static final String PROC_START_SEQ_IDENT = "seq=";
    private static final String QUEUED_WORK_LOOPER = "queued-work-looper";
    private static final int READ_TIMEOUT = 200;
    private static final boolean REPORT_TO_ACTIVITY = true;
    public static final int SERVICE_DONE_EXECUTING_ANON = 0;
    public static final int SERVICE_DONE_EXECUTING_START = 1;
    public static final int SERVICE_DONE_EXECUTING_STOP = 2;
    public static final String SOCKETNAME_PREFIX = "preloadopt";
    private static final int SQLITE_MEM_RELEASED_EVENT_LOG_TAG = 75003;
    public static final String TAG = "ActivityThread";
    private static final Bitmap.Config THUMBNAIL_FORMAT = Bitmap.Config.RGB_565;
    private static final int TOUCHSCREEN_PROCESS_ARGS = 2;
    private static final String TWO_COUNT_COLUMNS = "%21s %8d %21s %8d";
    private static boolean USE_CACHE = SystemProperties.getBoolean("persist.sys.freqinfo.cache", true);
    private static final int VM_PROCESS_STATE_JANK_IMPERCEPTIBLE = 1;
    private static final int VM_PROCESS_STATE_JANK_PERCEPTIBLE = 0;
    private static final int VM_PROCESS_STATE_TOP = 7;
    static final String ams_log_switch = SystemProperties.get("ro.config.hw_ams_log", "");
    static final boolean localLOGV;
    private static boolean mChangedFont = false;
    private static HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private static HandlerThread mPreloadHandlerThread = null;
    private static final Object mPreloadLock = new Object();
    static IContentSensorManager sContentSensorManager = null;
    @UnsupportedAppUsage
    private static volatile ActivityThread sCurrentActivityThread;
    private static final ThreadLocal<Intent> sCurrentBroadcastIntent = new ThreadLocal<>();
    private static boolean sIsCompatible = HwActivityTaskManager.IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED;
    public static final boolean sIsMygote;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    static volatile Handler sMainThreadHandler;
    @UnsupportedAppUsage
    static volatile IPackageManager sPackageManager;
    boolean isDragable = false;
    public boolean isEnableMagic = false;
    @UnsupportedAppUsage
    final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
    final Map<IBinder, ClientTransactionItem> mActivitiesToBeDestroyed = Collections.synchronizedMap(new ArrayMap());
    @UnsupportedAppUsage
    final ArrayList<Application> mAllApplications = new ArrayList<>();
    @UnsupportedAppUsage
    final ApplicationThread mAppThread = new ApplicationThread();
    private final SparseArray<ArrayMap<String, BackupAgent>> mBackupAgentsByUser = new SparseArray<>();
    @UnsupportedAppUsage
    AppBindData mBoundApplication;
    Configuration mCompatConfiguration;
    @UnsupportedAppUsage
    Configuration mConfiguration;
    Bundle mCoreSettings = null;
    @UnsupportedAppUsage
    int mCurDefaultDisplayDpi;
    String mCurrentActivity = null;
    @UnsupportedAppUsage
    boolean mDensityCompatMode;
    int mDisplayId = 0;
    final Executor mExecutor = new HandlerExecutor(this.mH);
    final GcIdler mGcIdler = new GcIdler();
    boolean mGcIdlerScheduled = false;
    @GuardedBy({"mGetProviderLocks"})
    final ArrayMap<ProviderKey, Object> mGetProviderLocks = new ArrayMap<>();
    @UnsupportedAppUsage
    final H mH = new H();
    boolean mHiddenApiWarningShown = false;
    IHwViewRootImpl mHwViewRootImpl = HwFrameworkFactory.getHwViewRootImpl();
    @UnsupportedAppUsage
    Application mInitialApplication;
    @UnsupportedAppUsage
    Instrumentation mInstrumentation;
    @UnsupportedAppUsage
    String mInstrumentationAppDir = null;
    String mInstrumentationLibDir = null;
    String mInstrumentationPackageName = null;
    String[] mInstrumentationSplitAppDirs = null;
    @UnsupportedAppUsage
    String mInstrumentedAppDir = null;
    String mInstrumentedLibDir = null;
    String[] mInstrumentedSplitAppDirs = null;
    private boolean mIsNeedStartUiProbe = false;
    ArrayList<WeakReference<AssistStructure>> mLastAssistStructures = new ArrayList<>();
    @GuardedBy({"mAppThread"})
    private int mLastProcessState = -1;
    private int mLastSessionId;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    final ArrayMap<IBinder, ProviderClientRecord> mLocalProviders = new ArrayMap<>();
    @UnsupportedAppUsage
    final ArrayMap<ComponentName, ProviderClientRecord> mLocalProvidersByName = new ArrayMap<>();
    @UnsupportedAppUsage
    final Looper mLooper = Looper.myLooper();
    private Configuration mMainThreadConfig = new Configuration();
    Configuration mMultiWindowOverrideConfig = null;
    @GuardedBy({"mNetworkPolicyLock"})
    private long mNetworkBlockSeq = -1;
    private final Object mNetworkPolicyLock = new Object();
    ActivityClientRecord mNewActivities = null;
    private final AtomicInteger mNumLaunchingActivities = new AtomicInteger();
    @UnsupportedAppUsage
    int mNumVisibleActivities = 0;
    final ArrayMap<Activity, ArrayList<OnActivityPausedListener>> mOnPauseListeners = new ArrayMap<>();
    Configuration mOverrideConfig = null;
    @UnsupportedAppUsage
    @GuardedBy({"mResourcesManager"})
    final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<>();
    @UnsupportedAppUsage
    @GuardedBy({"mResourcesManager"})
    Configuration mPendingConfiguration = null;
    @GuardedBy({"mAppThread"})
    private int mPendingProcessState = -1;
    private PreloadThreadHandler mPreloadHandler;
    Profiler mProfiler;
    @UnsupportedAppUsage
    final ArrayMap<ProviderKey, ProviderClientRecord> mProviderMap = new ArrayMap<>();
    @UnsupportedAppUsage
    final ArrayMap<IBinder, ProviderRefCount> mProviderRefCountMap = new ArrayMap<>();
    final PurgeIdler mPurgeIdler = new PurgeIdler();
    boolean mPurgeIdlerScheduled = false;
    @GuardedBy({"mResourcesManager"})
    final ArrayList<ActivityClientRecord> mRelaunchingActivities = new ArrayList<>();
    @GuardedBy({"this"})
    private Map<SafeCancellationTransport, CancellationSignal> mRemoteCancellations;
    @UnsupportedAppUsage
    @GuardedBy({"mResourcesManager"})
    final ArrayMap<String, WeakReference<LoadedApk>> mResourcePackages = new ArrayMap<>();
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final ResourcesManager mResourcesManager = ResourcesManager.getInstance();
    @UnsupportedAppUsage
    final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
    boolean mSomeActivitiesChanged = false;
    private long mStartTime;
    @UnsupportedAppUsage
    private ContextImpl mSystemContext;
    boolean mSystemThread = false;
    private ContextImpl mSystemUiContext;
    private final TransactionExecutor mTransactionExecutor = new TransactionExecutor(this);
    boolean mUpdatingSystemConfig = false;
    private IZrHung mZrHungAppEyeUiProbe = HwFrameworkFactory.getZrHung(IZrHung.APPEYE_UIP_NAME);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nDumpGraphicsInfo(FileDescriptor fileDescriptor);

    private native void nInitZygoteChildHeapProfiling();

    private native void nPurgePendingResources();

    static {
        boolean z = DEBUG_HW_ACTIVITY;
        localLOGV = z;
        DEBUG_RESULTS = z;
        DEBUG_CONFIGURATION = z;
        DEBUG_ORDER = z;
        boolean z2 = true;
        if (System.getenv("MAPLE_RUNTIME") == null) {
            z2 = false;
        }
        sIsMygote = z2;
    }

    /* access modifiers changed from: private */
    public static final class ProviderKey {
        final String authority;
        final int userId;

        public ProviderKey(String authority2, int userId2) {
            this.authority = authority2;
            this.userId = userId2;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ProviderKey)) {
                return false;
            }
            ProviderKey other = (ProviderKey) o;
            if (!Objects.equals(this.authority, other.authority) || this.userId != other.userId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            String str = this.authority;
            return (str != null ? str.hashCode() : 0) ^ this.userId;
        }
    }

    public static final class ActivityClientRecord {
        @UnsupportedAppUsage
        Activity activity;
        @UnsupportedAppUsage
        ActivityInfo activityInfo;
        public IBinder assistToken;
        @UnsupportedAppUsage
        CompatibilityInfo compatInfo;
        ViewRootImpl.ActivityConfigCallback configCallback;
        Configuration createdConfig;
        String embeddedID;
        boolean hideForNow;
        int ident;
        @UnsupportedAppUsage
        Intent intent;
        public final boolean isForward;
        boolean isTopResumedActivity;
        Activity.NonConfigurationInstances lastNonConfigurationInstances;
        boolean lastReportedTopResumedState;
        private int mLifecycleState;
        @GuardedBy({"this"})
        private Configuration mPendingOverrideConfig;
        Window mPendingRemoveWindow;
        WindowManager mPendingRemoveWindowManager;
        @UnsupportedAppUsage
        boolean mPreserveWindow;
        Configuration newConfig;
        ActivityClientRecord nextIdle;
        Configuration overrideConfig;
        @UnsupportedAppUsage
        public LoadedApk packageInfo;
        Activity parent;
        @UnsupportedAppUsage
        boolean paused;
        int pendingConfigChanges;
        List<ReferrerIntent> pendingIntents;
        List<ResultInfo> pendingResults;
        PersistableBundle persistentState;
        ProfilerInfo profilerInfo;
        String referrer;
        boolean startsNotResumed;
        Bundle state;
        @UnsupportedAppUsage
        boolean stopped;
        private Configuration tmpConfig;
        @UnsupportedAppUsage
        public IBinder token;
        IVoiceInteractor voiceInteractor;
        Window window;

        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        @VisibleForTesting
        public ActivityClientRecord() {
            this.tmpConfig = new Configuration();
            this.mLifecycleState = 0;
            this.isForward = false;
            init();
        }

        public ActivityClientRecord(IBinder token2, Intent intent2, int ident2, ActivityInfo info, Configuration overrideConfig2, CompatibilityInfo compatInfo2, String referrer2, IVoiceInteractor voiceInteractor2, Bundle state2, PersistableBundle persistentState2, List<ResultInfo> pendingResults2, List<ReferrerIntent> pendingNewIntents, boolean isForward2, ProfilerInfo profilerInfo2, ClientTransactionHandler client, IBinder assistToken2) {
            this.tmpConfig = new Configuration();
            this.mLifecycleState = 0;
            this.token = token2;
            this.assistToken = assistToken2;
            this.ident = ident2;
            this.intent = intent2;
            this.referrer = referrer2;
            this.voiceInteractor = voiceInteractor2;
            this.activityInfo = info;
            this.compatInfo = compatInfo2;
            this.state = state2;
            this.persistentState = persistentState2;
            this.pendingResults = pendingResults2;
            this.pendingIntents = pendingNewIntents;
            this.isForward = isForward2;
            this.profilerInfo = profilerInfo2;
            this.overrideConfig = overrideConfig2;
            this.packageInfo = client.getPackageInfoNoCheck(this.activityInfo.applicationInfo, compatInfo2);
            init();
        }

        private void init() {
            this.parent = null;
            this.embeddedID = null;
            this.paused = false;
            this.stopped = false;
            this.hideForNow = false;
            this.nextIdle = null;
            this.configCallback = new ViewRootImpl.ActivityConfigCallback() {
                /* class android.app.$$Lambda$ActivityThread$ActivityClientRecord$HOrG1qglSjSUHSjKBn2rXtX0gGg */

                @Override // android.view.ViewRootImpl.ActivityConfigCallback
                public final void onConfigurationChanged(Configuration configuration, int i) {
                    ActivityThread.ActivityClientRecord.this.lambda$init$0$ActivityThread$ActivityClientRecord(configuration, i);
                }
            };
        }

        public /* synthetic */ void lambda$init$0$ActivityThread$ActivityClientRecord(Configuration overrideConfig2, int newDisplayId) {
            Activity activity2 = this.activity;
            if (activity2 != null) {
                activity2.mMainThread.handleActivityConfigurationChanged(this.token, overrideConfig2, newDisplayId);
                return;
            }
            throw new IllegalStateException("Received config update for non-existing activity");
        }

        public int getLifecycleState() {
            return this.mLifecycleState;
        }

        public void setState(int newLifecycleState) {
            this.mLifecycleState = newLifecycleState;
            int i = this.mLifecycleState;
            if (i == 1) {
                this.paused = true;
                this.stopped = true;
            } else if (i == 2) {
                this.paused = true;
                this.stopped = false;
            } else if (i == 3) {
                this.paused = false;
                this.stopped = false;
            } else if (i == 4) {
                this.paused = true;
                this.stopped = false;
            } else if (i == 5) {
                this.paused = true;
                this.stopped = true;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isPreHoneycomb() {
            Activity activity2 = this.activity;
            return activity2 != null && activity2.getApplicationInfo().targetSdkVersion < 11;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isPreP() {
            Activity activity2 = this.activity;
            return activity2 != null && activity2.getApplicationInfo().targetSdkVersion < 28;
        }

        public boolean isPersistable() {
            return this.activityInfo.persistableMode == 2;
        }

        public boolean isVisibleFromServer() {
            Activity activity2 = this.activity;
            return activity2 != null && activity2.mVisibleFromServer;
        }

        public String toString() {
            Intent intent2 = this.intent;
            ComponentName componentName = intent2 != null ? intent2.getComponent() : null;
            StringBuilder sb = new StringBuilder();
            sb.append("ActivityRecord{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" token=");
            sb.append(this.token);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(componentName == null ? "no component name" : componentName.toShortString());
            sb.append("}");
            return sb.toString();
        }

        public String getStateString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ActivityClientRecord{");
            sb.append("paused=");
            sb.append(this.paused);
            sb.append(", stopped=");
            sb.append(this.stopped);
            sb.append(", hideForNow=");
            sb.append(this.hideForNow);
            sb.append(", startsNotResumed=");
            sb.append(this.startsNotResumed);
            sb.append(", isForward=");
            sb.append(this.isForward);
            sb.append(", pendingConfigChanges=");
            sb.append(this.pendingConfigChanges);
            sb.append(", preserveWindow=");
            sb.append(this.mPreserveWindow);
            if (this.activity != null) {
                sb.append(", Activity{");
                sb.append("resumed=");
                sb.append(this.activity.mResumed);
                sb.append(", stopped=");
                sb.append(this.activity.mStopped);
                sb.append(", finished=");
                sb.append(this.activity.isFinishing());
                sb.append(", destroyed=");
                sb.append(this.activity.isDestroyed());
                sb.append(", startedActivity=");
                sb.append(this.activity.mStartedActivity);
                sb.append(", changingConfigurations=");
                sb.append(this.activity.mChangingConfigurations);
                sb.append("}");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public final class ProviderClientRecord {
        @UnsupportedAppUsage
        final ContentProviderHolder mHolder;
        @UnsupportedAppUsage
        final ContentProvider mLocalProvider;
        final String[] mNames;
        @UnsupportedAppUsage
        final IContentProvider mProvider;

        ProviderClientRecord(String[] names, IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
            this.mNames = names;
            this.mProvider = provider;
            this.mLocalProvider = localProvider;
            this.mHolder = holder;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ReceiverData extends BroadcastReceiver.PendingResult {
        @UnsupportedAppUsage
        CompatibilityInfo compatInfo;
        @UnsupportedAppUsage
        ActivityInfo info;
        @UnsupportedAppUsage
        Intent intent;

        public ReceiverData(Intent intent2, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, IBinder token, int sendingUser) {
            super(resultCode, resultData, resultExtras, 0, ordered, sticky, token, sendingUser, intent2.getFlags());
            this.intent = intent2;
        }

        public String toString() {
            return "ReceiverData{intent=" + this.intent + " packageName=" + this.info.packageName + " resultCode=" + getResultCode() + " resultData=" + getResultData() + " resultExtras=" + getResultExtras(false) + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class CreateBackupAgentData {
        ApplicationInfo appInfo;
        int backupMode;
        CompatibilityInfo compatInfo;
        int userId;

        CreateBackupAgentData() {
        }

        public String toString() {
            return "CreateBackupAgentData{appInfo=" + this.appInfo + " backupAgent=" + this.appInfo.backupAgentName + " mode=" + this.backupMode + " userId=" + this.userId + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class CreateServiceData {
        @UnsupportedAppUsage
        CompatibilityInfo compatInfo;
        @UnsupportedAppUsage
        ServiceInfo info;
        @UnsupportedAppUsage
        Intent intent;
        @UnsupportedAppUsage
        IBinder token;

        CreateServiceData() {
        }

        public String toString() {
            return "CreateServiceData{token=" + this.token + " className=" + this.info.name + " packageName=" + this.info.packageName + " intent=" + this.intent + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BindServiceData {
        @UnsupportedAppUsage
        Intent intent;
        boolean rebind;
        @UnsupportedAppUsage
        IBinder token;

        BindServiceData() {
        }

        public String toString() {
            return "BindServiceData{token=" + this.token + " intent=" + this.intent + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ServiceArgsData {
        @UnsupportedAppUsage
        Intent args;
        int flags;
        int startId;
        boolean taskRemoved;
        @UnsupportedAppUsage
        IBinder token;

        ServiceArgsData() {
        }

        public String toString() {
            return "ServiceArgsData{token=" + this.token + " startId=" + this.startId + " args=" + this.args + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class AppBindData {
        @UnsupportedAppUsage
        ApplicationInfo appInfo;
        AutofillOptions autofillOptions;
        String buildSerial;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        CompatibilityInfo compatInfo;
        Configuration config;
        ContentCaptureOptions contentCaptureOptions;
        int debugMode;
        boolean enableBinderTracking;
        @UnsupportedAppUsage
        LoadedApk info;
        ProfilerInfo initProfilerInfo;
        @UnsupportedAppUsage
        Bundle instrumentationArgs;
        ComponentName instrumentationName;
        IUiAutomationConnection instrumentationUiAutomationConnection;
        IInstrumentationWatcher instrumentationWatcher;
        @UnsupportedAppUsage
        boolean persistent;
        @UnsupportedAppUsage
        String processName;
        @UnsupportedAppUsage
        List<ProviderInfo> providers;
        @UnsupportedAppUsage
        boolean restrictedBackupMode;
        boolean trackAllocation;

        AppBindData() {
        }

        public String toString() {
            return "AppBindData{appInfo=" + this.appInfo + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Profiler {
        boolean autoStopProfiler;
        boolean handlingProfiling;
        ParcelFileDescriptor profileFd;
        String profileFile;
        boolean profiling;
        int samplingInterval;
        boolean streamingOutput;

        Profiler() {
        }

        public void setProfiler(ProfilerInfo profilerInfo) {
            ParcelFileDescriptor fd = profilerInfo.profileFd;
            if (!this.profiling) {
                ParcelFileDescriptor parcelFileDescriptor = this.profileFd;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e) {
                    }
                }
                this.profileFile = profilerInfo.profileFile;
                this.profileFd = fd;
                this.samplingInterval = profilerInfo.samplingInterval;
                this.autoStopProfiler = profilerInfo.autoStopProfiler;
                this.streamingOutput = profilerInfo.streamingOutput;
            } else if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e2) {
                }
            }
        }

        public void startProfiling() {
            if (this.profileFd != null && !this.profiling) {
                try {
                    VMDebug.startMethodTracing(this.profileFile, this.profileFd.getFileDescriptor(), SystemProperties.getInt("debug.traceview-buffer-size-mb", 8) * 1024 * 1024, 0, this.samplingInterval != 0, this.samplingInterval, this.streamingOutput);
                    this.profiling = true;
                } catch (RuntimeException e) {
                    Slog.w(ActivityThread.TAG, "Profiling failed on path " + this.profileFile, e);
                    try {
                        this.profileFd.close();
                        this.profileFd = null;
                    } catch (IOException e2) {
                        Slog.w(ActivityThread.TAG, "Failure closing profile fd", e2);
                    }
                }
            }
        }

        public void stopProfiling() {
            if (this.profiling) {
                this.profiling = false;
                Debug.stopMethodTracing();
                ParcelFileDescriptor parcelFileDescriptor = this.profileFd;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e) {
                    }
                }
                this.profileFd = null;
                this.profileFile = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class DumpComponentInfo {
        String[] args;
        ParcelFileDescriptor fd;
        String prefix;
        IBinder token;

        DumpComponentInfo() {
        }
    }

    static final class ContextCleanupInfo {
        ContextImpl context;
        String what;
        String who;

        ContextCleanupInfo() {
        }
    }

    /* access modifiers changed from: package-private */
    public static final class DumpHeapData {
        ParcelFileDescriptor fd;
        RemoteCallback finishCallback;
        public boolean mallocInfo;
        public boolean managed;
        String path;
        public boolean runGc;

        DumpHeapData() {
        }
    }

    /* access modifiers changed from: package-private */
    public static final class UpdateCompatibilityData {
        CompatibilityInfo info;
        String pkg;

        UpdateCompatibilityData() {
        }
    }

    /* access modifiers changed from: package-private */
    public static final class RequestAssistContextExtras {
        IBinder activityToken;
        int flags;
        IBinder requestToken;
        int requestType;
        int sessionId;

        RequestAssistContextExtras() {
        }
    }

    /* access modifiers changed from: private */
    public class ApplicationThread extends IApplicationThread.Stub {
        private static final String DB_INFO_FORMAT = "  %8s %8s %14s %14s  %s";

        private ApplicationThread() {
        }

        @Override // android.app.IApplicationThread
        public final void scheduleSleeping(IBinder token, boolean sleeping) {
            ActivityThread.this.sendMessage(137, token, sleeping ? 1 : 0);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState) {
            ActivityThread.this.updateProcessState(processState, false);
            ReceiverData r = new ReceiverData(intent, resultCode, data, extras, sync, false, ActivityThread.this.mAppThread.asBinder(), sendingUser);
            r.info = info;
            r.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(113, r);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode, int userId) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            d.backupMode = backupMode;
            d.userId = userId;
            ActivityThread.this.sendMessage(128, d);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int userId) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            d.userId = userId;
            ActivityThread.this.sendMessage(129, d);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) {
            ActivityThread.this.updateProcessState(processState, false);
            CreateServiceData s = new CreateServiceData();
            s.token = token;
            s.info = info;
            s.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(114, s);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) {
            ActivityThread.this.updateProcessState(processState, false);
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            s.rebind = rebind;
            if (ActivityThread.DEBUG_SERVICE) {
                Slog.v(ActivityThread.TAG, "scheduleBindService token=" + token + " intent=" + intent + " uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
            }
            ActivityThread.this.sendMessage(121, s);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleUnbindService(IBinder token, Intent intent) {
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            ActivityThread.this.sendMessage(122, s);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleServiceArgs(IBinder token, ParceledListSlice args) {
            List<ServiceStartArgs> list = args.getList();
            for (int i = 0; i < list.size(); i++) {
                ServiceStartArgs ssa = list.get(i);
                ServiceArgsData s = new ServiceArgsData();
                s.token = token;
                s.taskRemoved = ssa.taskRemoved;
                s.startId = ssa.startId;
                s.flags = ssa.flags;
                s.args = ssa.args;
                ActivityThread.this.sendMessage(115, s);
            }
        }

        @Override // android.app.IApplicationThread
        public final void scheduleStopService(IBinder token) {
            ActivityThread.this.sendMessage(116, token);
        }

        @Override // android.app.IApplicationThread
        public void preLoadClass(ApplicationInfo appInfo, CompatibilityInfo compatInfo) {
            if (appInfo != null && compatInfo != null) {
                ActivityThread.this.sendPreloadMessage(10, ActivityThread.this.getPackageInfoNoCheck(appInfo, compatInfo), 0);
            }
        }

        @Override // android.app.IApplicationThread
        public final void bindApplication(String processName, ApplicationInfo appInfo, List<ProviderInfo> providers, ComponentName instrumentationName, ProfilerInfo profilerInfo, Bundle instrumentationArgs, IInstrumentationWatcher instrumentationWatcher, IUiAutomationConnection instrumentationUiConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean isRestrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial, AutofillOptions autofillOptions, ContentCaptureOptions contentCaptureOptions) {
            if (services != null) {
                ServiceManager.initServiceCache(services);
            }
            if (coreSettings.containsKey("overrideConfig")) {
                ActivityThread.this.mMultiWindowOverrideConfig = (Configuration) coreSettings.getParcelable("overrideConfig");
                Slog.i(ActivityThread.TAG, "multi window override config: " + ActivityThread.this.mMultiWindowOverrideConfig);
                coreSettings.remove("overrideConfig");
            }
            setCoreSettings(coreSettings);
            AppBindData data = new AppBindData();
            data.processName = processName;
            data.appInfo = appInfo;
            data.providers = providers;
            data.instrumentationName = instrumentationName;
            data.instrumentationArgs = instrumentationArgs;
            data.instrumentationWatcher = instrumentationWatcher;
            data.instrumentationUiAutomationConnection = instrumentationUiConnection;
            data.debugMode = debugMode;
            data.enableBinderTracking = enableBinderTracking;
            data.trackAllocation = trackAllocation;
            data.restrictedBackupMode = isRestrictedBackupMode;
            data.persistent = persistent;
            data.config = ActivityThread.this.updateConfig(config);
            data.compatInfo = compatInfo;
            data.initProfilerInfo = profilerInfo;
            data.buildSerial = buildSerial;
            data.autofillOptions = autofillOptions;
            data.contentCaptureOptions = contentCaptureOptions;
            ActivityThread.this.sendMessage(110, data);
            if (HwFrameworkFactory.getHwActivityThread() != null) {
                HwFrameworkFactory.getHwActivityThread().handleHwPreloadStatus(1);
            }
        }

        @Override // android.app.IApplicationThread
        public final void runIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = entryPoint;
            args.arg2 = entryPointArgs;
            ActivityThread.this.sendMessage(158, args);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleExit() {
            ActivityThread.this.sendMessage(111, null);
        }

        @Override // android.app.IApplicationThread
        public final void scheduleSuicide() {
            ActivityThread.this.sendMessage(130, null);
        }

        @Override // android.app.IApplicationThread
        public void scheduleApplicationInfoChanged(ApplicationInfo ai) {
            ActivityThread.this.mH.removeMessages(156, ai);
            scheduleApplicationThemeInfoChanged(ai, false);
        }

        @Override // android.app.IApplicationThread
        public void scheduleApplicationThemeInfoChanged(ApplicationInfo ai, boolean fromThemeChange) {
            ActivityThread.this.sendMessage(156, ai, fromThemeChange ? 1 : 0);
        }

        @Override // android.app.IApplicationThread
        public void updateTimeZone() {
            TimeZone.setDefault(null);
        }

        @Override // android.app.IApplicationThread
        public void clearDnsCache() {
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        }

        @Override // android.app.IApplicationThread
        public void updateHttpProxy() {
            ActivityThread.updateHttpProxy(ActivityThread.this.getApplication() != null ? ActivityThread.this.getApplication() : ActivityThread.this.getSystemContext());
        }

        @Override // android.app.IApplicationThread
        public void processInBackground() {
            ActivityThread.this.mH.removeMessages(120);
            ActivityThread.this.mH.sendMessage(ActivityThread.this.mH.obtainMessage(120));
        }

        @Override // android.app.IApplicationThread
        public void dumpService(ParcelFileDescriptor pfd, IBinder servicetoken, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = pfd.dup();
                data.token = servicetoken;
                data.args = args;
                ActivityThread.this.sendMessage(123, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpService failed", e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(pfd);
                throw th;
            }
            IoUtils.closeQuietly(pfd);
        }

        @Override // android.app.IApplicationThread
        public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String dataStr, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
            ActivityThread.this.updateProcessState(processState, false);
            receiver.performReceive(intent, resultCode, dataStr, extras, ordered, sticky, sendingUser);
        }

        @Override // android.app.IApplicationThread
        public void scheduleLowMemory() {
            ActivityThread.this.sendMessage(124, null);
        }

        @Override // android.app.IApplicationThread
        public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
            ActivityThread.this.sendMessage(127, profilerInfo, start ? 1 : 0, profileType);
        }

        @Override // android.app.IApplicationThread
        public void dumpHeap(boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd, RemoteCallback finishCallback) {
            DumpHeapData dhd = new DumpHeapData();
            dhd.managed = managed;
            dhd.mallocInfo = mallocInfo;
            dhd.runGc = runGc;
            dhd.path = path;
            try {
                dhd.fd = fd.dup();
                dhd.finishCallback = finishCallback;
                ActivityThread.this.sendMessage(135, (Object) dhd, 0, 0, true);
            } catch (IOException e) {
                Slog.e(ActivityThread.TAG, "Failed to duplicate heap dump file descriptor", e);
            }
        }

        @Override // android.app.IApplicationThread
        public void attachAgent(String agent) {
            ActivityThread.this.sendMessage(155, agent);
        }

        @Override // android.app.IApplicationThread
        public void setSchedulingGroup(int group) {
            try {
                Process.setProcessGroup(Process.myPid(), group);
            } catch (Exception e) {
                Slog.w(ActivityThread.TAG, "Failed setting process group to " + group, e);
            }
        }

        @Override // android.app.IApplicationThread
        public void dispatchPackageBroadcast(int cmd, String[] packages) {
            ActivityThread.this.sendMessage(133, packages, cmd);
        }

        @Override // android.app.IApplicationThread
        public void scheduleCrash(String msg) {
            ActivityThread.this.sendMessage(134, msg);
        }

        @Override // android.app.IApplicationThread
        public void dumpActivity(ParcelFileDescriptor pfd, IBinder activitytoken, String prefix, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = pfd.dup();
                data.token = activitytoken;
                data.prefix = prefix;
                data.args = args;
                ActivityThread.this.sendMessage(136, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpActivity failed", e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(pfd);
                throw th;
            }
            IoUtils.closeQuietly(pfd);
        }

        @Override // android.app.IApplicationThread
        public void dumpProvider(ParcelFileDescriptor pfd, IBinder providertoken, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = pfd.dup();
                data.token = providertoken;
                data.args = args;
                ActivityThread.this.sendMessage(141, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpProvider failed", e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(pfd);
                throw th;
            }
            IoUtils.closeQuietly(pfd);
        }

        @Override // android.app.IApplicationThread
        public void dumpMemInfo(ParcelFileDescriptor pfd, Debug.MemoryInfo mem, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(pfd.getFileDescriptor()));
            try {
                dumpMemInfo(pw, mem, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable);
            } finally {
                pw.flush();
                IoUtils.closeQuietly(pfd);
            }
        }

        private void dumpMemInfo(PrintWriter pw, Debug.MemoryInfo memInfo, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable) {
            String str;
            int i;
            String str2;
            long nativeMax = Debug.getNativeHeapSize() / 1024;
            long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024;
            long nativeFree = Debug.getNativeHeapFreeSize() / 1024;
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long dalvikMax = runtime.totalMemory() / 1024;
            long dalvikFree = runtime.freeMemory() / 1024;
            long dalvikAllocated = dalvikMax - dalvikFree;
            long[] instanceCounts = VMDebug.countInstancesOfClasses(new Class[]{ContextImpl.class, Activity.class, WebView.class, OpenSSLSocketImpl.class}, true);
            long appContextInstanceCount = instanceCounts[0];
            long activityInstanceCount = instanceCounts[1];
            long webviewInstanceCount = instanceCounts[2];
            long openSslSocketCount = instanceCounts[3];
            long viewInstanceCount = ViewDebug.getViewInstanceCount();
            long viewRootInstanceCount = ViewDebug.getViewRootImplCount();
            int globalAssetCount = AssetManager.getGlobalAssetCount();
            int globalAssetManagerCount = AssetManager.getGlobalAssetManagerCount();
            int binderLocalObjectCount = Debug.getBinderLocalObjectCount();
            int binderProxyObjectCount = Debug.getBinderProxyObjectCount();
            int binderDeathObjectCount = Debug.getBinderDeathObjectCount();
            long parcelSize = Parcel.getGlobalAllocSize();
            long parcelCount = Parcel.getGlobalAllocCount();
            SQLiteDebug.PagerStats stats = SQLiteDebug.getDatabaseInfo();
            boolean showContents = true;
            ActivityThread.dumpMemInfoTable(pw, memInfo, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, Process.myPid(), ActivityThread.this.mBoundApplication != null ? ActivityThread.this.mBoundApplication.processName : "unknown", nativeMax, nativeAllocated, nativeFree, dalvikMax, dalvikAllocated, dalvikFree);
            if (checkin) {
                pw.print(viewInstanceCount);
                pw.print(',');
                long viewRootInstanceCount2 = viewRootInstanceCount;
                pw.print(viewRootInstanceCount2);
                pw.print(',');
                pw.print(appContextInstanceCount);
                pw.print(',');
                pw.print(activityInstanceCount);
                pw.print(',');
                pw.print(globalAssetCount);
                pw.print(',');
                pw.print(globalAssetManagerCount);
                pw.print(',');
                pw.print(binderLocalObjectCount);
                pw.print(',');
                pw.print(binderProxyObjectCount);
                pw.print(',');
                pw.print(binderDeathObjectCount);
                pw.print(',');
                pw.print(openSslSocketCount);
                pw.print(',');
                SQLiteDebug.PagerStats stats2 = stats;
                pw.print(stats2.memoryUsed / 1024);
                pw.print(',');
                pw.print(stats2.memoryUsed / 1024);
                pw.print(',');
                pw.print(stats2.pageCacheOverflow / 1024);
                pw.print(',');
                pw.print(stats2.largestMemAlloc / 1024);
                int i2 = 0;
                while (i2 < stats2.dbStats.size()) {
                    SQLiteDebug.DbStats dbStats = stats2.dbStats.get(i2);
                    pw.print(',');
                    pw.print(dbStats.dbName);
                    pw.print(',');
                    pw.print(dbStats.pageSize);
                    pw.print(',');
                    pw.print(dbStats.dbSize);
                    pw.print(',');
                    pw.print(dbStats.lookaside);
                    pw.print(',');
                    pw.print(dbStats.cache);
                    pw.print(',');
                    pw.print(dbStats.cache);
                    i2++;
                    viewRootInstanceCount2 = viewRootInstanceCount2;
                    stats2 = stats2;
                }
                pw.println();
                return;
            }
            String str3 = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            pw.println(str3);
            pw.println(" Objects");
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Views:", Long.valueOf(viewInstanceCount), "ViewRootImpl:", Long.valueOf(viewRootInstanceCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "AppContexts:", Long.valueOf(appContextInstanceCount), "Activities:", Long.valueOf(activityInstanceCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Assets:", Integer.valueOf(globalAssetCount), "AssetManagers:", Integer.valueOf(globalAssetManagerCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Local Binders:", Integer.valueOf(binderLocalObjectCount), "Proxy Binders:", Integer.valueOf(binderProxyObjectCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Parcel memory:", Long.valueOf(parcelSize / 1024), "Parcel count:", Long.valueOf(parcelCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Death Recipients:", Integer.valueOf(binderDeathObjectCount), "OpenSSL Sockets:", Long.valueOf(openSslSocketCount));
            ActivityThread.printRow(pw, ActivityThread.ONE_COUNT_COLUMN, "WebViews:", Long.valueOf(webviewInstanceCount));
            pw.println(str3);
            pw.println(" SQL");
            ActivityThread.printRow(pw, ActivityThread.ONE_COUNT_COLUMN, "MEMORY_USED:", Integer.valueOf(stats.memoryUsed / 1024));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "PAGECACHE_OVERFLOW:", Integer.valueOf(stats.pageCacheOverflow / 1024), "MALLOC_SIZE:", Integer.valueOf(stats.largestMemAlloc / 1024));
            pw.println(str3);
            int N = stats.dbStats.size();
            if (N > 0) {
                pw.println(" DATABASES");
                int i3 = 5;
                ActivityThread.printRow(pw, DB_INFO_FORMAT, "pgsz", "dbsz", "Lookaside(b)", "cache", "Dbname");
                int i4 = 0;
                while (i4 < N) {
                    SQLiteDebug.DbStats dbStats2 = stats.dbStats.get(i4);
                    Object[] objArr = new Object[i3];
                    objArr[0] = dbStats2.pageSize > 0 ? String.valueOf(dbStats2.pageSize) : str3;
                    objArr[1] = dbStats2.dbSize > 0 ? String.valueOf(dbStats2.dbSize) : str3;
                    objArr[2] = dbStats2.lookaside > 0 ? String.valueOf(dbStats2.lookaside) : str3;
                    objArr[3] = dbStats2.cache;
                    objArr[4] = dbStats2.dbName;
                    ActivityThread.printRow(pw, DB_INFO_FORMAT, objArr);
                    i4++;
                    N = N;
                    str3 = str3;
                    i3 = 5;
                }
                str = str3;
                i = 2;
            } else {
                str = str3;
                i = 2;
            }
            String assetAlloc = AssetManager.getAssetAllocations();
            if (assetAlloc != null) {
                str2 = str;
                pw.println(str2);
                pw.println(" Asset Allocations");
                pw.print(assetAlloc);
            } else {
                str2 = str;
            }
            if (dumpUnreachable) {
                if ((ActivityThread.this.mBoundApplication == null || (i & ActivityThread.this.mBoundApplication.appInfo.flags) == 0) && !Build.IS_DEBUGGABLE) {
                    showContents = false;
                }
                pw.println(str2);
                pw.println(" Unreachable memory");
                pw.print(Debug.getUnreachableMemory(100, showContents));
            }
        }

        @Override // android.app.IApplicationThread
        public void dumpMemInfoProto(ParcelFileDescriptor pfd, Debug.MemoryInfo mem, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) {
            ProtoOutputStream proto = new ProtoOutputStream(pfd.getFileDescriptor());
            try {
                dumpMemInfo(proto, mem, dumpFullInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable);
            } finally {
                proto.flush();
                IoUtils.closeQuietly(pfd);
            }
        }

        /* JADX INFO: Multiple debug info for r3v5 android.database.sqlite.SQLiteDebug$PagerStats: [D('stats' android.database.sqlite.SQLiteDebug$PagerStats), D('oToken' long)] */
        private void dumpMemInfo(ProtoOutputStream proto, Debug.MemoryInfo memInfo, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable) {
            long nativeMax = Debug.getNativeHeapSize() / 1024;
            long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024;
            long nativeFree = Debug.getNativeHeapFreeSize() / 1024;
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long dalvikMax = runtime.totalMemory() / 1024;
            long dalvikFree = runtime.freeMemory() / 1024;
            long dalvikAllocated = dalvikMax - dalvikFree;
            boolean showContents = false;
            long[] instanceCounts = VMDebug.countInstancesOfClasses(new Class[]{ContextImpl.class, Activity.class, WebView.class, OpenSSLSocketImpl.class}, true);
            long appContextInstanceCount = instanceCounts[0];
            long activityInstanceCount = instanceCounts[1];
            long webviewInstanceCount = instanceCounts[2];
            long openSslSocketCount = instanceCounts[3];
            long viewInstanceCount = ViewDebug.getViewInstanceCount();
            long viewRootInstanceCount = ViewDebug.getViewRootImplCount();
            int globalAssetCount = AssetManager.getGlobalAssetCount();
            int globalAssetManagerCount = AssetManager.getGlobalAssetManagerCount();
            int binderLocalObjectCount = Debug.getBinderLocalObjectCount();
            int binderProxyObjectCount = Debug.getBinderProxyObjectCount();
            int binderDeathObjectCount = Debug.getBinderDeathObjectCount();
            long parcelSize = Parcel.getGlobalAllocSize();
            long parcelCount = Parcel.getGlobalAllocCount();
            SQLiteDebug.PagerStats stats = SQLiteDebug.getDatabaseInfo();
            long mToken = proto.start(1146756268033L);
            proto.write(1120986464257L, Process.myPid());
            proto.write(1138166333442L, ActivityThread.this.mBoundApplication != null ? ActivityThread.this.mBoundApplication.processName : "unknown");
            ActivityThread.dumpMemInfoTable(proto, memInfo, dumpDalvik, dumpSummaryOnly, nativeMax, nativeAllocated, nativeFree, dalvikMax, dalvikAllocated, dalvikFree);
            proto.end(mToken);
            long oToken = proto.start(1146756268034L);
            proto.write(1120986464257L, viewInstanceCount);
            proto.write(1120986464258L, viewRootInstanceCount);
            long appContextInstanceCount2 = appContextInstanceCount;
            proto.write(1120986464259L, appContextInstanceCount2);
            long activityInstanceCount2 = activityInstanceCount;
            proto.write(1120986464260L, activityInstanceCount2);
            proto.write(1120986464261L, globalAssetCount);
            proto.write(1120986464262L, globalAssetManagerCount);
            proto.write(1120986464263L, binderLocalObjectCount);
            proto.write(1120986464264L, binderProxyObjectCount);
            proto.write(1112396529673L, parcelSize / 1024);
            proto.write(1120986464266L, parcelCount);
            proto.write(1120986464267L, binderDeathObjectCount);
            proto.write(1120986464268L, openSslSocketCount);
            proto.write(1120986464269L, webviewInstanceCount);
            proto.end(oToken);
            long sToken = proto.start(1146756268035L);
            SQLiteDebug.PagerStats stats2 = stats;
            proto.write(1120986464257L, stats2.memoryUsed / 1024);
            proto.write(1120986464258L, stats2.pageCacheOverflow / 1024);
            proto.write(1120986464259L, stats2.largestMemAlloc / 1024);
            int n = stats2.dbStats.size();
            int i = 0;
            while (i < n) {
                SQLiteDebug.DbStats dbStats = stats2.dbStats.get(i);
                long dToken = proto.start(2246267895812L);
                proto.write(1138166333441L, dbStats.dbName);
                proto.write(1120986464258L, dbStats.pageSize);
                proto.write(1120986464259L, dbStats.dbSize);
                proto.write(1120986464260L, dbStats.lookaside);
                proto.write(1138166333445L, dbStats.cache);
                proto.end(dToken);
                i++;
                n = n;
                activityInstanceCount2 = activityInstanceCount2;
                appContextInstanceCount2 = appContextInstanceCount2;
                stats2 = stats2;
            }
            proto.end(sToken);
            String assetAlloc = AssetManager.getAssetAllocations();
            if (assetAlloc != null) {
                proto.write(1138166333444L, assetAlloc);
            }
            if (dumpUnreachable) {
                if (((ActivityThread.this.mBoundApplication == null ? 0 : ActivityThread.this.mBoundApplication.appInfo.flags) & 2) != 0 || Build.IS_DEBUGGABLE) {
                    showContents = true;
                }
                proto.write(1138166333445L, Debug.getUnreachableMemory(100, showContents));
            }
        }

        @Override // android.app.IApplicationThread
        public void dumpGfxInfo(ParcelFileDescriptor pfd, String[] args) {
            ActivityThread.this.nDumpGraphicsInfo(pfd.getFileDescriptor());
            WindowManagerGlobal.getInstance().dumpGfxInfo(pfd.getFileDescriptor(), args);
            IoUtils.closeQuietly(pfd);
        }

        private File getDatabasesDir(Context context) {
            return context.getDatabasePath(FullBackup.APK_TREE_TOKEN).getParentFile();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dumpDatabaseInfo(ParcelFileDescriptor pfd, String[] args, boolean isSystem) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(pfd.getFileDescriptor()));
            SQLiteDebug.dump(new PrintWriterPrinter(pw), args, isSystem);
            pw.flush();
        }

        @Override // android.app.IApplicationThread
        public void dumpDbInfo(ParcelFileDescriptor pfd, final String[] args) {
            if (ActivityThread.this.mSystemThread) {
                try {
                    final ParcelFileDescriptor dup = pfd.dup();
                    IoUtils.closeQuietly(pfd);
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        /* class android.app.ActivityThread.ApplicationThread.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                ApplicationThread.this.dumpDatabaseInfo(dup, args, true);
                            } finally {
                                IoUtils.closeQuietly(dup);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.w(ActivityThread.TAG, "Could not dup FD " + pfd.getFileDescriptor().getInt$());
                    IoUtils.closeQuietly(pfd);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(pfd);
                    throw th;
                }
            } else {
                dumpDatabaseInfo(pfd, args, false);
                IoUtils.closeQuietly(pfd);
            }
        }

        @Override // android.app.IApplicationThread
        public void unstableProviderDied(IBinder provider) {
            ActivityThread.this.sendMessage(142, provider);
        }

        @Override // android.app.IApplicationThread
        public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) {
            RequestAssistContextExtras cmd = new RequestAssistContextExtras();
            cmd.activityToken = activityToken;
            cmd.requestToken = requestToken;
            cmd.requestType = requestType;
            cmd.sessionId = sessionId;
            cmd.flags = flags;
            ActivityThread.this.sendMessage(143, cmd);
        }

        @Override // android.app.IApplicationThread
        public void setCoreSettings(Bundle coreSettings) {
            ActivityThread.this.sendMessage(138, coreSettings);
        }

        @Override // android.app.IApplicationThread
        public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) {
            UpdateCompatibilityData ucd = new UpdateCompatibilityData();
            ucd.pkg = pkg;
            ucd.info = info;
            ActivityThread.this.sendMessage(139, ucd);
        }

        @Override // android.app.IApplicationThread
        public void scheduleTrimMemory(int level) {
            iawareTrimMemory(level, false);
        }

        @Override // android.app.IApplicationThread
        public void iawareTrimMemory(int level, boolean fromIAware) {
            Runnable r = PooledLambda.obtainRunnable($$Lambda$ActivityThread$ApplicationThread$eNIzQZ974tdrS8H1o1fp2sJZxk.INSTANCE, ActivityThread.this, Integer.valueOf(level), Boolean.valueOf(fromIAware));
            Choreographer choreographer = Choreographer.getMainThreadInstance();
            if (choreographer != null) {
                choreographer.postCallback(4, r, null);
            } else {
                ActivityThread.this.mH.post(r);
            }
        }

        @Override // android.app.IApplicationThread
        public void scheduleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
            ActivityThread.this.sendMessage(144, token, drawComplete ? 1 : 0);
        }

        @Override // android.app.IApplicationThread
        public void scheduleOnNewActivityOptions(IBinder token, Bundle options) {
            ActivityThread.this.sendMessage(146, new Pair(token, ActivityOptions.fromBundle(options)));
        }

        @Override // android.app.IApplicationThread
        public void setProcessState(int state) {
            ActivityThread.this.updateProcessState(state, true);
        }

        @Override // android.app.IApplicationThread
        public void setNetworkBlockSeq(long procStateSeq) {
            synchronized (ActivityThread.this.mNetworkPolicyLock) {
                ActivityThread.this.mNetworkBlockSeq = procStateSeq;
            }
        }

        @Override // android.app.IApplicationThread
        public void scheduleInstallProvider(ProviderInfo provider) {
            ActivityThread.this.sendMessage(145, provider);
        }

        @Override // android.app.IApplicationThread
        public final void updateTimePrefs(int timeFormatPreference) {
            Boolean timeFormatPreferenceBool;
            if (timeFormatPreference == 0) {
                timeFormatPreferenceBool = Boolean.FALSE;
            } else if (timeFormatPreference == 1) {
                timeFormatPreferenceBool = Boolean.TRUE;
            } else {
                timeFormatPreferenceBool = null;
            }
            DateFormat.set24HourTimePref(timeFormatPreferenceBool);
        }

        @Override // android.app.IApplicationThread
        public void scheduleEnterAnimationComplete(IBinder token) {
            ActivityThread.this.sendMessage(149, token);
        }

        @Override // android.app.IApplicationThread
        public void notifyCleartextNetwork(byte[] firstPacket) {
            if (StrictMode.vmCleartextNetworkEnabled()) {
                StrictMode.onCleartextNetworkDetected(firstPacket);
            }
        }

        @Override // android.app.IApplicationThread
        public void startBinderTracking() {
            ActivityThread.this.sendMessage(150, null);
        }

        @Override // android.app.IApplicationThread
        public void stopBinderTrackingAndDump(ParcelFileDescriptor pfd) {
            try {
                ActivityThread.this.sendMessage(151, pfd.dup());
            } catch (IOException e) {
            } catch (Throwable th) {
                IoUtils.closeQuietly(pfd);
                throw th;
            }
            IoUtils.closeQuietly(pfd);
        }

        @Override // android.app.IApplicationThread
        public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = voiceInteractor;
            ActivityThread.this.sendMessage(154, args);
        }

        @Override // android.app.IApplicationThread
        public void handleTrustStorageUpdate() {
            NetworkSecurityPolicy.getInstance().handleTrustStorageUpdate();
        }

        @Override // android.app.IApplicationThread
        public void schedulePCWindowStateChanged(IBinder token, int windowState) throws RemoteException {
            ActivityThread.this.sendMessage(1003, token, windowState);
        }

        @Override // android.app.IApplicationThread
        public void scheduleFreeFormOutLineChanged(IBinder token, int state) throws RemoteException {
            ActivityThread.this.sendMessageAtFront(1004, token, state, 0, false);
        }

        @Override // android.app.IApplicationThread
        public void scheduleGetFreeFormInfo(IBinder token, int direction) {
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != direction) {
                HwActivityTaskManager.saveMultiWindowTipState(Settings.Secure.FLOATING_WIN_COUNT, 3);
            } else {
                HwActivityTaskManager.saveMultiWindowTipState(Settings.Secure.FLOATING_WIN_COUNT, 0);
            }
        }

        @Override // android.app.IApplicationThread
        public void scheduleFreeFormOutLineChangedForFloating(IBinder token, int state) throws RemoteException {
            ActivityThread.this.sendMessageAtFront(1009, token, state, 0, false);
        }

        @Override // android.app.IApplicationThread
        public void notifyHwMultiWindowCaption(IBinder token, Bundle info) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = info;
            ActivityThread.this.sendMessage(1008, args);
        }

        @Override // android.app.IApplicationThread
        public void scheduleRestoreFreeFormConfig(IBinder token) throws RemoteException {
            ActivityThread.this.sendMessage(1005, token, 0);
        }

        @Override // android.app.IApplicationThread
        public void requestContentNode(IBinder appToken, Bundle data, int token) throws RemoteException {
            if (ActivityThread.sContentSensorManager == null || (ActivityThread.sContentSensorManager instanceof ContentSensorManagerFactory.DefaultContentSensorManager)) {
                ActivityThread.sContentSensorManager = ContentSensorManagerFactory.createContentSensorManager(token, ActivityThread.this.getActivity(appToken));
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = appToken;
            args.arg2 = data;
            ActivityThread.this.sendMessage(200, args, token);
        }

        @Override // android.app.IApplicationThread
        public void requestContentOther(IBinder appToken, Bundle data, int token) throws RemoteException {
            if (ActivityThread.sContentSensorManager == null) {
                ActivityThread.sContentSensorManager = ContentSensorManagerFactory.createContentSensorManager(token, ActivityThread.this.getActivity(appToken));
            }
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = appToken;
            args.arg2 = data;
            ActivityThread.this.sendMessage(201, args, token);
        }

        @Override // android.app.IApplicationThread
        public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
            ActivityThread.this.scheduleTransaction(transaction);
        }

        @Override // android.app.IApplicationThread
        public void requestDirectActions(IBinder activityToken, IVoiceInteractor interactor, RemoteCallback cancellationCallback, RemoteCallback callback) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            if (cancellationCallback != null) {
                ICancellationSignal transport = ActivityThread.this.createSafeCancellationTransport(cancellationSignal);
                Bundle cancellationResult = new Bundle();
                cancellationResult.putBinder(VoiceInteractor.KEY_CANCELLATION_SIGNAL, transport.asBinder());
                cancellationCallback.sendResult(cancellationResult);
            }
            ActivityThread.this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityThread$ApplicationThread$uR_ee5oPoxu4U_by7wU55jwtdU.INSTANCE, ActivityThread.this, activityToken, interactor, cancellationSignal, callback));
        }

        @Override // android.app.IApplicationThread
        public void performDirectAction(IBinder activityToken, String actionId, Bundle arguments, RemoteCallback cancellationCallback, RemoteCallback resultCallback) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            if (cancellationCallback != null) {
                ICancellationSignal transport = ActivityThread.this.createSafeCancellationTransport(cancellationSignal);
                Bundle cancellationResult = new Bundle();
                cancellationResult.putBinder(VoiceInteractor.KEY_CANCELLATION_SIGNAL, transport.asBinder());
                cancellationCallback.sendResult(cancellationResult);
            }
            ActivityThread.this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityThread$ApplicationThread$nBC_BR7B9W6ftKAxur3BC53SJYc.INSTANCE, ActivityThread.this, activityToken, actionId, arguments, cancellationSignal, resultCallback));
        }

        @Override // android.app.IApplicationThread
        public void notifyAppToTop(int enable) throws RemoteException {
            if (checkPermission()) {
                ActivityThread.this.sendMessage(163, null, enable);
            }
        }

        private boolean checkPermission() {
            int uid = UserHandle.getAppId(Binder.getCallingUid());
            if (uid == 1000) {
                return true;
            }
            Slog.e(ActivityThread.TAG, "Process Permission error! uid:" + uid);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SafeCancellationTransport createSafeCancellationTransport(CancellationSignal cancellationSignal) {
        SafeCancellationTransport transport;
        synchronized (this) {
            if (this.mRemoteCancellations == null) {
                this.mRemoteCancellations = new ArrayMap();
            }
            transport = new SafeCancellationTransport(this, cancellationSignal);
            this.mRemoteCancellations.put(transport, cancellationSignal);
        }
        return transport;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CancellationSignal removeSafeCancellationTransport(SafeCancellationTransport transport) {
        CancellationSignal cancellation;
        synchronized (this) {
            cancellation = this.mRemoteCancellations.remove(transport);
            if (this.mRemoteCancellations.isEmpty()) {
                this.mRemoteCancellations = null;
            }
        }
        return cancellation;
    }

    /* access modifiers changed from: private */
    public static final class SafeCancellationTransport extends ICancellationSignal.Stub {
        private final WeakReference<ActivityThread> mWeakActivityThread;

        SafeCancellationTransport(ActivityThread activityThread, CancellationSignal cancellation) {
            this.mWeakActivityThread = new WeakReference<>(activityThread);
        }

        @Override // android.os.ICancellationSignal
        public void cancel() {
            CancellationSignal cancellation;
            ActivityThread activityThread = this.mWeakActivityThread.get();
            if (activityThread != null && (cancellation = activityThread.removeSafeCancellationTransport(this)) != null) {
                cancellation.cancel();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class PreloadThreadHandler extends Handler {
        private static final int ASYNC_PRELOAD_CLASS_MSG = 10;
        private static final int ASYNC_THREAD_QUIT = 0;
        private static final int PRE_BINDER_API_CACHE = 6;
        private static final int PRE_BIND_APPLICATION = 11;
        private static final int PRE_HOOK_APPLICATION = 9;
        private static final int PRE_INIT_BUILD_MODEL = 2;
        private static final int PRE_INIT_SCROLLER_BOOST_MANAGER = 7;
        private static final int PRE_INSTALL_MEMORY_LEAK_MONITOR = 4;
        private static final int PRE_LODE_MULTIDPIINFO = 5;
        private static final int PRE_REPORT_BIND_APP_TO_AWARE = 8;

        public PreloadThreadHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                synchronized (ActivityThread.mPreloadLock) {
                    if (ActivityThread.USE_CACHE) {
                        HwFrameworkFactory.getHwApiCacheManagerEx().disableCache();
                    }
                    if (!(ActivityThread.mPreloadHandlerThread == null || ActivityThread.mPreloadHandlerThread.getLooper() == null)) {
                        ActivityThread.mPreloadHandlerThread.getLooper().quit();
                        HandlerThread unused = ActivityThread.mPreloadHandlerThread = null;
                        ActivityThread.this.mPreloadHandler = null;
                    }
                }
            } else if (i != 2) {
                switch (i) {
                    case 4:
                        ((Application) msg.obj).installMemoryLeakMonitor();
                        return;
                    case 5:
                        HwThemeManager.getPreMultidpiInfo((String) msg.obj);
                        return;
                    case 6:
                        PackageManager pm = ((ContextImpl) msg.obj).getPackageManager();
                        if (pm != null) {
                            HwFrameworkFactory.getHwApiCacheManagerEx().apiPreCache(pm);
                        }
                        ActivityThread.this.sendPreloadMessage(0, null, ActivityThread.HANDLER_BINDER_DURATION_TIME);
                        return;
                    case 7:
                        ScrollerBoostManager.getInstance().init();
                        return;
                    case 8:
                        if (HwFrameworkFactory.getHwActivityThread() != null) {
                            HwFrameworkFactory.getHwActivityThread().reportBindApplicationToAware((Application) msg.obj, Application.getProcessName());
                            return;
                        }
                        return;
                    case 9:
                        ApplicationInfo appInfo = (ApplicationInfo) msg.obj;
                        if (HwFrameworkFactory.getHwBehaviorCollectManager() != null) {
                            HwFrameworkFactory.getHwBehaviorCollectManager().regUntrustedAppToMonitorService(appInfo);
                            return;
                        }
                        return;
                    case 10:
                        ActivityThread.this.processAysncPreloadClassMsg(msg);
                        return;
                    case 11:
                        ActivityThread.this.initCompatibleState(((ApplicationInfo) msg.obj).packageName);
                        return;
                    default:
                        Slog.e(ActivityThread.TAG, "Invalid preload activity message msg:" + msg.what);
                        return;
                }
            } else {
                AppBindData data1 = (AppBindData) msg.obj;
                if (HwFrameworkFactory.getHwActivityThread() != null) {
                    HwFrameworkFactory.getHwActivityThread().changeToSpecialModel(data1.appInfo.packageName);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processAysncPreloadClassMsg(Message msg) {
        if ((msg.obj instanceof LoadedApk) && HwFrameworkFactory.getHwActivityThread() != null) {
            LoadedApk loadedApk = (LoadedApk) msg.obj;
            HwFrameworkFactory.getHwActivityThread().schedThreadToRtg(Process.myTid(), true);
            if (loadedApk != null) {
                loadedApk.getClassLoader();
            }
            HwFrameworkFactory.getHwActivityThread().schedThreadToRtg(Process.myTid(), false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPreloadMessage(int msgWhat, Object data, long delayTime) {
        synchronized (mPreloadLock) {
            initPreloadHandlerThread();
            if (this.mPreloadHandler == null) {
                this.mPreloadHandler = new PreloadThreadHandler(mPreloadHandlerThread.getLooper());
            }
            this.mPreloadHandler.sendMessageDelayed(this.mPreloadHandler.obtainMessage(msgWhat, data), delayTime);
        }
    }

    /* access modifiers changed from: package-private */
    public class H extends Handler {
        public static final int APPLICATION_INFO_CHANGED = 156;
        public static final int ATTACH_AGENT = 155;
        public static final int BIND_APPLICATION = 110;
        @UnsupportedAppUsage
        public static final int BIND_SERVICE = 121;
        public static final int CLEAN_UP_CONTEXT = 119;
        public static final int CONFIGURATION_CHANGED = 118;
        public static final int CREATE_BACKUP_AGENT = 128;
        @UnsupportedAppUsage
        public static final int CREATE_SERVICE = 114;
        public static final int CUSTOM_MSG = 1000;
        public static final int DESTROY_BACKUP_AGENT = 129;
        public static final int DISPATCH_PACKAGE_BROADCAST = 133;
        public static final int DUMP_ACTIVITY = 136;
        public static final int DUMP_HEAP = 135;
        @UnsupportedAppUsage
        public static final int DUMP_PROVIDER = 141;
        public static final int DUMP_SERVICE = 123;
        @UnsupportedAppUsage
        public static final int ENTER_ANIMATION_COMPLETE = 149;
        public static final int EXECUTE_TRANSACTION = 159;
        @UnsupportedAppUsage
        public static final int EXIT_APPLICATION = 111;
        public static final int FREEFORM_OUTLINE_CHANGED = 1004;
        public static final int FREEFORM_OUTLINE_CHANGED_FLOAT = 1009;
        @UnsupportedAppUsage
        public static final int GC_WHEN_IDLE = 120;
        @UnsupportedAppUsage
        public static final int INSTALL_PROVIDER = 145;
        public static final int LOAD_CYCLE_PATTERN = 1007;
        public static final int LOCAL_VOICE_INTERACTION_STARTED = 154;
        public static final int LOW_MEMORY = 124;
        public static final int MESSAGE_COLOR_PICKER = 1006;
        public static final int NOTIFY_APP_TO_TOP = 163;
        public static final int NOTIFY_HW_MULTI_WINDOW_CAPTION = 1008;
        public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
        public static final int PROFILER_CONTROL = 127;
        public static final int PURGE_RESOURCES = 161;
        @UnsupportedAppUsage
        public static final int RECEIVER = 113;
        public static final int RELAUNCH_ACTIVITY = 160;
        @UnsupportedAppUsage
        public static final int REMOVE_PROVIDER = 131;
        public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
        public static final int REQUEST_NODEGROUP_CONTENT = 200;
        public static final int REQUEST_OTHER_CONTENT = 201;
        public static final int RESTORE_FREEFORM_CONFIG = 1005;
        public static final int RUN_ISOLATED_ENTRY_POINT = 158;
        @UnsupportedAppUsage
        public static final int SCHEDULE_CRASH = 134;
        public static final int SCHEDULE_REPORT_RT = 2000;
        @UnsupportedAppUsage
        public static final int SERVICE_ARGS = 115;
        public static final int SET_CORE_SETTINGS = 138;
        public static final int SLEEPING = 137;
        public static final int START_BINDER_TRACKING = 150;
        public static final int STOP_BINDER_TRACKING_AND_DUMP = 151;
        @UnsupportedAppUsage
        public static final int STOP_SERVICE = 116;
        public static final int SUICIDE = 130;
        public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
        @UnsupportedAppUsage
        public static final int UNBIND_SERVICE = 122;
        public static final int UNSTABLE_PROVIDER_DIED = 142;
        public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
        public static final int WINDOW_STATE_CHANGED = 1003;

        H() {
        }

        /* access modifiers changed from: package-private */
        public String codeToString(int code) {
            return Integer.toString(code);
        }

        /* JADX INFO: Multiple debug info for r0v1 java.lang.Object: [D('data' android.app.ActivityThread$AppBindData), D('obj' java.lang.Object)] */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 110) {
                Trace.traceBegin(64, "bindApplication");
                ActivityThread.this.handleBindApplication((AppBindData) msg.obj);
                if (ActivityThread.sIsMygote && ActivityThread.IS_DEBUG_VERSION) {
                    ActivityThread.this.mStartTime = System.currentTimeMillis();
                    sendEmptyMessageDelayed(2000, 60000);
                }
                Trace.traceEnd(64);
            } else if (i == 111) {
                if (ActivityThread.sIsMygote && ActivityThread.IS_DEBUG_VERSION) {
                    removeMessages(2000);
                    if (!ActivityThread.this.handleScheduleReportRT()) {
                        Slog.e(ActivityThread.TAG, "fail to report RT infomation!!!");
                    }
                }
                if (ZygoteInit.sIsMygote) {
                    removeMessages(1007);
                }
                if (ActivityThread.this.mInitialApplication != null) {
                    ActivityThread.this.mInitialApplication.onTerminate();
                }
                Looper.myLooper().quit();
            } else if (i == 200) {
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    if (ActivityThread.sContentSensorManager != null) {
                        ActivityThread.sContentSensorManager.updateToken(msg.arg1, ActivityThread.this.getActivity((IBinder) args.arg1));
                        ActivityThread.sContentSensorManager.copyNode((Bundle) args.arg2);
                    }
                } catch (Exception e) {
                    Slog.e(ActivityThread.TAG, "copyNode get exception: " + e);
                }
            } else if (i != 201) {
                switch (i) {
                    case 113:
                        Trace.traceBegin(64, "broadcastReceiveComp");
                        ActivityThread.this.handleReceiver((ReceiverData) msg.obj);
                        Trace.traceEnd(64);
                        break;
                    case 114:
                        Trace.traceBegin(64, "serviceCreate: " + String.valueOf(msg.obj));
                        ActivityThread.this.handleCreateService((CreateServiceData) msg.obj);
                        Trace.traceEnd(64);
                        break;
                    case 115:
                        Trace.traceBegin(64, "serviceStart: " + String.valueOf(msg.obj));
                        ActivityThread.this.handleServiceArgs((ServiceArgsData) msg.obj);
                        Trace.traceEnd(64);
                        break;
                    case 116:
                        Trace.traceBegin(64, "serviceStop");
                        ActivityThread.this.handleStopService((IBinder) msg.obj);
                        ActivityThread.this.schedulePurgeIdler();
                        Trace.traceEnd(64);
                        break;
                    default:
                        LoadedApk loadedApk = null;
                        boolean z = true;
                        switch (i) {
                            case 118:
                                ActivityThread.this.handleConfigurationChanged((Configuration) msg.obj);
                                break;
                            case 119:
                                ContextCleanupInfo cci = (ContextCleanupInfo) msg.obj;
                                cci.context.performFinalCleanup(cci.who, cci.what);
                                break;
                            case 120:
                                ActivityThread.this.scheduleGcIdler();
                                break;
                            case 121:
                                Trace.traceBegin(64, "serviceBind");
                                ActivityThread.this.handleBindService((BindServiceData) msg.obj);
                                Trace.traceEnd(64);
                                break;
                            case 122:
                                Trace.traceBegin(64, "serviceUnbind");
                                ActivityThread.this.handleUnbindService((BindServiceData) msg.obj);
                                ActivityThread.this.schedulePurgeIdler();
                                Trace.traceEnd(64);
                                break;
                            case 123:
                                ActivityThread.this.handleDumpService((DumpComponentInfo) msg.obj);
                                break;
                            case 124:
                                Trace.traceBegin(64, "lowMemory");
                                ActivityThread.this.handleLowMemory();
                                Trace.traceEnd(64);
                                break;
                            case 163:
                                IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedInstance();
                                if (hwRtgSchedImpl != null) {
                                    hwRtgSchedImpl.resetRtgSchedHandle(msg.arg1);
                                }
                                if (msg.arg1 != 1) {
                                    HwFrameworkFactory.setRtgSchedImpl(null);
                                    break;
                                } else {
                                    HwFrameworkFactory.setRtgSchedImpl(hwRtgSchedImpl);
                                    break;
                                }
                            case 1003:
                                ActivityThread.this.handlePCWindowStateChanged((IBinder) msg.obj, msg.arg1);
                                break;
                            case 1004:
                                ActivityThread.this.handleFreeFormOutLineChanged((IBinder) msg.obj, msg.arg1);
                                break;
                            case 1005:
                                ActivityThread.this.handleRestoreFreeFormConfig((IBinder) msg.obj);
                                break;
                            case 1006:
                                if (HwFrameworkFactory.getHwActivityThread() != null) {
                                    Activity activity = (Activity) msg.obj;
                                    HwFrameworkFactory.getHwActivityThread().setNavigationBarColorFromActivityThread(activity, ActivityThread.this.mH, activity.mCurrentConfig);
                                    break;
                                }
                                break;
                            case 1007:
                                if (!(HwFrameworkFactory.getHwActivityThread() == null || ActivityThread.this.mBoundApplication == null || ActivityThread.this.mBoundApplication.info == null)) {
                                    HwFrameworkFactory.getHwActivityThread().loadAppCyclePatternAsync(ActivityThread.this.mBoundApplication.info.getAssets(), ActivityThread.this.mBoundApplication.appInfo, ActivityThread.this.mBoundApplication.processName);
                                    break;
                                }
                            case 1008:
                                if (msg.obj instanceof SomeArgs) {
                                    SomeArgs captionArgs = (SomeArgs) msg.obj;
                                    ActivityThread.this.notifyHwMultiWindowCaption((IBinder) captionArgs.arg1, (Bundle) captionArgs.arg2);
                                    break;
                                }
                                break;
                            case 1009:
                                ActivityThread.this.handleFreeFormOutLineChangedForFloating((IBinder) msg.obj, msg.arg1);
                                break;
                            case 2000:
                                if (!ActivityThread.this.handleScheduleReportRT()) {
                                    Slog.e(ActivityThread.TAG, "fail to report RT infomation!!!");
                                }
                                sendMessageDelayed(obtainMessage(2000), 3600000);
                                break;
                            default:
                                boolean z2 = false;
                                switch (i) {
                                    case 127:
                                        ActivityThread activityThread = ActivityThread.this;
                                        if (msg.arg1 != 0) {
                                            z2 = true;
                                        }
                                        activityThread.handleProfilerControl(z2, (ProfilerInfo) msg.obj, msg.arg2);
                                        break;
                                    case 128:
                                        Trace.traceBegin(64, "backupCreateAgent");
                                        ActivityThread.this.handleCreateBackupAgent((CreateBackupAgentData) msg.obj);
                                        Trace.traceEnd(64);
                                        break;
                                    case 129:
                                        Trace.traceBegin(64, "backupDestroyAgent");
                                        ActivityThread.this.handleDestroyBackupAgent((CreateBackupAgentData) msg.obj);
                                        Trace.traceEnd(64);
                                        break;
                                    case 130:
                                        Process.killProcess(Process.myPid());
                                        break;
                                    case 131:
                                        Trace.traceBegin(64, "providerRemove");
                                        ActivityThread.this.completeRemoveProvider((ProviderRefCount) msg.obj);
                                        Trace.traceEnd(64);
                                        break;
                                    default:
                                        switch (i) {
                                            case 133:
                                                Trace.traceBegin(64, "broadcastPackage");
                                                ActivityThread.this.handleDispatchPackageBroadcast(msg.arg1, (String[]) msg.obj);
                                                Trace.traceEnd(64);
                                                break;
                                            case 134:
                                                throw new RemoteServiceException((String) msg.obj);
                                            case 135:
                                                ActivityThread.handleDumpHeap((DumpHeapData) msg.obj);
                                                break;
                                            case 136:
                                                ActivityThread.this.handleDumpActivity((DumpComponentInfo) msg.obj);
                                                break;
                                            case 137:
                                                Trace.traceBegin(64, "sleeping");
                                                ActivityThread activityThread2 = ActivityThread.this;
                                                IBinder iBinder = (IBinder) msg.obj;
                                                if (msg.arg1 != 0) {
                                                    z2 = true;
                                                }
                                                activityThread2.handleSleeping(iBinder, z2);
                                                Trace.traceEnd(64);
                                                break;
                                            case 138:
                                                Trace.traceBegin(64, "setCoreSettings");
                                                ActivityThread.this.handleSetCoreSettings((Bundle) msg.obj);
                                                Trace.traceEnd(64);
                                                break;
                                            case 139:
                                                ActivityThread.this.handleUpdatePackageCompatibilityInfo((UpdateCompatibilityData) msg.obj);
                                                break;
                                            default:
                                                switch (i) {
                                                    case 141:
                                                        ActivityThread.this.handleDumpProvider((DumpComponentInfo) msg.obj);
                                                        break;
                                                    case 142:
                                                        ActivityThread.this.handleUnstableProviderDied((IBinder) msg.obj, z2);
                                                        break;
                                                    case 143:
                                                        ActivityThread.this.handleRequestAssistContextExtras((RequestAssistContextExtras) msg.obj);
                                                        break;
                                                    case 144:
                                                        ActivityThread activityThread3 = ActivityThread.this;
                                                        IBinder iBinder2 = (IBinder) msg.obj;
                                                        if (msg.arg1 == 1) {
                                                            z2 = true;
                                                        }
                                                        activityThread3.handleTranslucentConversionComplete(iBinder2, z2);
                                                        break;
                                                    case 145:
                                                        ActivityThread.this.handleInstallProvider((ProviderInfo) msg.obj);
                                                        break;
                                                    case 146:
                                                        Pair<IBinder, ActivityOptions> pair = (Pair) msg.obj;
                                                        ActivityThread.this.onNewActivityOptions(pair.first, pair.second);
                                                        break;
                                                    default:
                                                        switch (i) {
                                                            case 149:
                                                                ActivityThread.this.handleEnterAnimationComplete((IBinder) msg.obj);
                                                                break;
                                                            case 150:
                                                                ActivityThread.this.handleStartBinderTracking();
                                                                break;
                                                            case 151:
                                                                ActivityThread.this.handleStopBinderTrackingAndDump((ParcelFileDescriptor) msg.obj);
                                                                break;
                                                            default:
                                                                switch (i) {
                                                                    case 154:
                                                                        ActivityThread.this.handleLocalVoiceInteractionStarted((IBinder) ((SomeArgs) msg.obj).arg1, (IVoiceInteractor) ((SomeArgs) msg.obj).arg2);
                                                                        break;
                                                                    case 155:
                                                                        Application app = ActivityThread.this.getApplication();
                                                                        String str = (String) msg.obj;
                                                                        if (app != null) {
                                                                            loadedApk = app.mLoadedApk;
                                                                        }
                                                                        ActivityThread.handleAttachAgent(str, loadedApk);
                                                                        break;
                                                                    case 156:
                                                                        ActivityThread activityThread4 = ActivityThread.this;
                                                                        activityThread4.mUpdatingSystemConfig = true;
                                                                        try {
                                                                            ApplicationInfo applicationInfo = (ApplicationInfo) msg.obj;
                                                                            if (msg.arg1 != 1) {
                                                                                z = z2;
                                                                            }
                                                                            activityThread4.handleApplicationInfoChanged(applicationInfo, z);
                                                                            break;
                                                                        } finally {
                                                                            ActivityThread.this.mUpdatingSystemConfig = z2;
                                                                        }
                                                                    default:
                                                                        switch (i) {
                                                                            case 158:
                                                                                ActivityThread.this.handleRunIsolatedEntryPoint((String) ((SomeArgs) msg.obj).arg1, (String[]) ((SomeArgs) msg.obj).arg2);
                                                                                break;
                                                                            case 159:
                                                                                ClientTransaction transaction = (ClientTransaction) msg.obj;
                                                                                ActivityThread.this.mTransactionExecutor.execute(transaction);
                                                                                if (ActivityThread.isSystem()) {
                                                                                    transaction.recycle();
                                                                                    break;
                                                                                }
                                                                                break;
                                                                            case 160:
                                                                                ActivityThread activityThread5 = ActivityThread.this;
                                                                                IBinder iBinder3 = (IBinder) msg.obj;
                                                                                if (msg.arg1 == 1) {
                                                                                    z2 = true;
                                                                                }
                                                                                activityThread5.handleRelaunchActivityLocally(iBinder3, z2);
                                                                                break;
                                                                            case 161:
                                                                                ActivityThread.this.schedulePurgeIdler();
                                                                                break;
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                        break;
                }
            } else {
                SomeArgs args1 = (SomeArgs) msg.obj;
                try {
                    if (ActivityThread.sContentSensorManager != null) {
                        ActivityThread.sContentSensorManager.updateToken(msg.arg1, ActivityThread.this.getActivity((IBinder) args1.arg1));
                        ActivityThread.sContentSensorManager.processImageAndWebView((Bundle) args1.arg2);
                    }
                } catch (Exception e2) {
                    Slog.e(ActivityThread.TAG, "processImageAndWebView get exception: " + e2);
                }
            }
            Object obj = msg.obj;
            if (obj instanceof SomeArgs) {
                ((SomeArgs) obj).recycle();
            }
        }
    }

    private class Idler implements MessageQueue.IdleHandler {
        private Idler() {
        }

        @Override // android.os.MessageQueue.IdleHandler
        public final boolean queueIdle() {
            ActivityClientRecord a = ActivityThread.this.mNewActivities;
            boolean stopProfiling = false;
            if (!(ActivityThread.this.mBoundApplication == null || ActivityThread.this.mProfiler.profileFd == null || !ActivityThread.this.mProfiler.autoStopProfiler)) {
                stopProfiling = true;
            }
            if (a != null) {
                ActivityThread.this.mNewActivities = null;
                IActivityTaskManager am = ActivityTaskManager.getService();
                do {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Reporting idle of ");
                    sb.append(a);
                    sb.append(" finished=");
                    sb.append(a.activity != null && a.activity.mFinished);
                    Flog.i(101, sb.toString());
                    if (a.activity != null && !a.activity.mFinished) {
                        try {
                            am.activityIdle(a.token, a.createdConfig, stopProfiling);
                            a.createdConfig = null;
                        } catch (RemoteException ex) {
                            throw ex.rethrowFromSystemServer();
                        }
                    }
                    a = a.nextIdle;
                    a.nextIdle = null;
                } while (a != null);
            }
            if (stopProfiling) {
                ActivityThread.this.mProfiler.stopProfiling();
            }
            ActivityThread.this.applyPendingProcessState();
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public final class GcIdler implements MessageQueue.IdleHandler {
        GcIdler() {
        }

        @Override // android.os.MessageQueue.IdleHandler
        public final boolean queueIdle() {
            ActivityThread.this.doGcIfNeeded();
            ActivityThread.this.purgePendingResources();
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public final class PurgeIdler implements MessageQueue.IdleHandler {
        PurgeIdler() {
        }

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            ActivityThread.this.purgePendingResources();
            return false;
        }
    }

    @UnsupportedAppUsage
    public static ActivityThread currentActivityThread() {
        return sCurrentActivityThread;
    }

    public static boolean isSystem() {
        if (sCurrentActivityThread != null) {
            return sCurrentActivityThread.mSystemThread;
        }
        return false;
    }

    public static String currentOpPackageName() {
        ActivityThread am = currentActivityThread();
        if (am == null || am.getApplication() == null) {
            return null;
        }
        return am.getApplication().getOpPackageName();
    }

    @UnsupportedAppUsage
    public static String currentPackageName() {
        AppBindData appBindData;
        ActivityThread am = currentActivityThread();
        if (am == null || (appBindData = am.mBoundApplication) == null) {
            return null;
        }
        return appBindData.appInfo.packageName;
    }

    @UnsupportedAppUsage
    public static int currentAppInfoFlag() {
        AppBindData appBindData;
        ActivityThread am = currentActivityThread();
        if (am == null || (appBindData = am.mBoundApplication) == null) {
            return -1;
        }
        return appBindData.appInfo.flags;
    }

    @UnsupportedAppUsage
    public static String currentProcessName() {
        AppBindData appBindData;
        ActivityThread am = currentActivityThread();
        if (am == null || (appBindData = am.mBoundApplication) == null) {
            return null;
        }
        return appBindData.processName;
    }

    @UnsupportedAppUsage
    public static Application currentApplication() {
        ActivityThread am = currentActivityThread();
        if (am != null) {
            return am.mInitialApplication;
        }
        return null;
    }

    public static String currentActivityName() {
        ActivityThread am = currentActivityThread();
        if (am != null) {
            return am.mCurrentActivity;
        }
        return null;
    }

    @UnsupportedAppUsage
    public static IPackageManager getPackageManager() {
        if (sPackageManager != null) {
            return sPackageManager;
        }
        sPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return sPackageManager;
    }

    /* access modifiers changed from: package-private */
    public Configuration applyConfigCompatMainThread(int displayDensity, Configuration config, CompatibilityInfo compat) {
        if (config == null) {
            return null;
        }
        if (compat.supportsScreen()) {
            return config;
        }
        this.mMainThreadConfig.setTo(config);
        Configuration config2 = this.mMainThreadConfig;
        compat.applyToConfiguration(displayDensity, config2);
        return config2;
    }

    /* access modifiers changed from: package-private */
    public Resources getTopLevelResources(String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, LoadedApk pkgInfo) {
        return this.mResourcesManager.getResources(null, resDir, splitResDirs, overlayDirs, libDirs, displayId, null, pkgInfo.getCompatibilityInfo(), pkgInfo.getClassLoader());
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final Handler getHandler() {
        return this.mH;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags) {
        return getPackageInfo(packageName, compatInfo, flags, UserHandle.myUserId());
    }

    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags, int userId) {
        WeakReference<LoadedApk> ref;
        boolean differentUser = UserHandle.myUserId() != userId;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 268436480, userId < 0 ? UserHandle.myUserId() : userId);
            synchronized (this.mResourcesManager) {
                if (differentUser) {
                    ref = null;
                } else if ((flags & 1) != 0) {
                    ref = this.mPackages.get(packageName);
                } else {
                    ref = this.mResourcePackages.get(packageName);
                }
                LoadedApk packageInfo = ref != null ? ref.get() : null;
                if (!(ai == null || packageInfo == null)) {
                    if (!isLoadedApkResourceDirsUpToDate(packageInfo, ai)) {
                        packageInfo.updateApplicationInfo(ai, null);
                    }
                    if (packageInfo.isSecurityViolation()) {
                        if ((flags & 2) == 0) {
                            throw new SecurityException("Requesting code from " + packageName + " to be run in process " + this.mBoundApplication.processName + "/" + this.mBoundApplication.appInfo.uid);
                        }
                    }
                    if (isPackageInfoValid(ai, packageInfo)) {
                        return packageInfo;
                    }
                }
            }
            if (ai != null) {
                return getPackageInfo(ai, compatInfo, flags);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public final LoadedApk getPackageInfo(ApplicationInfo ai, CompatibilityInfo compatInfo, int flags) {
        boolean includeCode = (flags & 1) != 0;
        boolean securityViolation = includeCode && ai.uid != 0 && ai.uid != 1000 && (this.mBoundApplication == null || !UserHandle.isSameApp(ai.uid, this.mBoundApplication.appInfo.uid));
        boolean registerPackage = includeCode && (1073741824 & flags) != 0;
        if ((flags & 3) != 1 || !securityViolation) {
            return getPackageInfo(ai, compatInfo, null, securityViolation, includeCode, registerPackage);
        }
        String msg = "Requesting code from " + ai.packageName + " (with uid " + ai.uid + ")";
        if (this.mBoundApplication != null) {
            msg = msg + " to be run in process " + this.mBoundApplication.processName + " (with uid " + this.mBoundApplication.appInfo.uid + ")";
        }
        throw new SecurityException(msg);
    }

    @Override // android.app.ClientTransactionHandler
    @UnsupportedAppUsage
    public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo) {
        return getPackageInfo(ai, compatInfo, null, false, true, false);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final LoadedApk peekPackageInfo(String packageName, boolean includeCode) {
        WeakReference<LoadedApk> ref;
        LoadedApk loadedApk;
        synchronized (this.mResourcesManager) {
            if (includeCode) {
                ref = this.mPackages.get(packageName);
            } else {
                ref = this.mResourcePackages.get(packageName);
            }
            loadedApk = ref != null ? ref.get() : null;
        }
        return loadedApk;
    }

    private LoadedApk getPackageInfo(ApplicationInfo aInfo, CompatibilityInfo compatInfo, ClassLoader baseLoader, boolean securityViolation, boolean includeCode, boolean registerPackage) {
        WeakReference<LoadedApk> ref;
        String str;
        boolean differentUser = UserHandle.myUserId() != UserHandle.getUserId(aInfo.uid);
        synchronized (this.mResourcesManager) {
            if (differentUser) {
                ref = null;
            } else if (includeCode) {
                ref = this.mPackages.get(aInfo.packageName);
            } else {
                ref = this.mResourcePackages.get(aInfo.packageName);
            }
            String str2 = null;
            LoadedApk packageInfo = ref != null ? ref.get() : null;
            if (packageInfo != null) {
                if (!isLoadedApkResourceDirsUpToDate(packageInfo, aInfo)) {
                    packageInfo.updateApplicationInfo(aInfo, null);
                }
                if (isPackageInfoValid(aInfo, packageInfo)) {
                    return packageInfo;
                }
            }
            if (localLOGV) {
                StringBuilder sb = new StringBuilder();
                if (includeCode) {
                    str = "Loading code package ";
                } else {
                    str = "Loading resource-only package ";
                }
                sb.append(str);
                sb.append(aInfo.packageName);
                sb.append(" (in ");
                if (this.mBoundApplication != null) {
                    str2 = this.mBoundApplication.processName;
                }
                sb.append(str2);
                sb.append(")");
                Slog.v(TAG, sb.toString());
            }
            LoadedApk packageInfo2 = new LoadedApk(this, aInfo, compatInfo, baseLoader, securityViolation, includeCode && (aInfo.flags & 4) != 0, registerPackage);
            if (this.mSystemThread && "android".equals(aInfo.packageName)) {
                packageInfo2.installSystemApplicationInfo(aInfo, getSystemContext().mPackageInfo.getClassLoader());
            }
            if (!differentUser) {
                if (includeCode) {
                    this.mPackages.put(aInfo.packageName, new WeakReference<>(packageInfo2));
                } else {
                    this.mResourcePackages.put(aInfo.packageName, new WeakReference<>(packageInfo2));
                }
            }
            if (compatInfo != null && !compatInfo.supportsScreen() && !packageInfo2.getCompatibilityInfo().equals(compatInfo)) {
                packageInfo2.setCompatibilityInfo(compatInfo);
            }
            return packageInfo2;
        }
    }

    private static boolean isLoadedApkResourceDirsUpToDate(LoadedApk loadedApk, ApplicationInfo appInfo) {
        Resources packageResources = loadedApk.mResources;
        String[] overlayDirs = ArrayUtils.defeatNullable(loadedApk.getOverlayDirs());
        String[] resourceDirs = ArrayUtils.defeatNullable(appInfo.resourceDirs);
        return (packageResources == null || packageResources.getAssets().isUpToDate()) && overlayDirs.length == resourceDirs.length && ArrayUtils.containsAll(overlayDirs, resourceDirs);
    }

    @UnsupportedAppUsage
    ActivityThread() {
    }

    @UnsupportedAppUsage
    public ApplicationThread getApplicationThread() {
        return this.mAppThread;
    }

    @UnsupportedAppUsage
    public Instrumentation getInstrumentation() {
        return this.mInstrumentation;
    }

    public boolean isProfiling() {
        Profiler profiler = this.mProfiler;
        return (profiler == null || profiler.profileFile == null || this.mProfiler.profileFd != null) ? false : true;
    }

    public String getProfileFilePath() {
        return this.mProfiler.profileFile;
    }

    @UnsupportedAppUsage
    public Looper getLooper() {
        return this.mLooper;
    }

    public Executor getExecutor() {
        return this.mExecutor;
    }

    @UnsupportedAppUsage
    public Application getApplication() {
        return this.mInitialApplication;
    }

    @UnsupportedAppUsage
    public String getProcessName() {
        return this.mBoundApplication.processName;
    }

    @UnsupportedAppUsage
    public ContextImpl getSystemContext() {
        if (this.mSystemContext == null) {
            synchronized (this) {
                if (this.mSystemContext == null) {
                    this.mSystemContext = ContextImpl.createSystemContext(this);
                }
            }
        }
        return this.mSystemContext;
    }

    public ContextImpl getSystemUiContext() {
        if (this.mSystemUiContext == null) {
            synchronized (this) {
                if (this.mSystemUiContext == null) {
                    this.mSystemUiContext = ContextImpl.createSystemUiContext(getSystemContext());
                }
            }
        }
        return this.mSystemUiContext;
    }

    public ContextImpl createSystemUiContext(int displayId) {
        return ContextImpl.createSystemUiContext(getSystemUiContext(), displayId);
    }

    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        synchronized (this) {
            getSystemContext().installSystemApplicationInfo(info, classLoader);
            getSystemUiContext().installSystemApplicationInfo(info, classLoader);
            this.mProfiler = new Profiler();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void scheduleGcIdler() {
        if (!this.mGcIdlerScheduled) {
            this.mGcIdlerScheduled = true;
            Looper.myQueue().addIdleHandler(this.mGcIdler);
        }
        this.mH.removeMessages(120);
    }

    /* access modifiers changed from: package-private */
    public void unscheduleGcIdler() {
        if (this.mGcIdlerScheduled) {
            this.mGcIdlerScheduled = false;
            Looper.myQueue().removeIdleHandler(this.mGcIdler);
        }
        this.mH.removeMessages(120);
    }

    /* access modifiers changed from: package-private */
    public void schedulePurgeIdler() {
        if (!this.mPurgeIdlerScheduled) {
            this.mPurgeIdlerScheduled = true;
            Looper.myQueue().addIdleHandler(this.mPurgeIdler);
        }
        this.mH.removeMessages(161);
    }

    /* access modifiers changed from: package-private */
    public void unschedulePurgeIdler() {
        if (this.mPurgeIdlerScheduled) {
            this.mPurgeIdlerScheduled = false;
            Looper.myQueue().removeIdleHandler(this.mPurgeIdler);
        }
        this.mH.removeMessages(161);
    }

    /* access modifiers changed from: package-private */
    public void doGcIfNeeded() {
        doGcIfNeeded("bg");
    }

    /* access modifiers changed from: package-private */
    public void doGcIfNeeded(String reason) {
        this.mGcIdlerScheduled = false;
        if (BinderInternal.getLastGcTime() + 5000 < SystemClock.uptimeMillis()) {
            BinderInternal.forceGc(reason);
            if (ZygoteInit.sIsMygote) {
                VMRuntime.getRuntime();
                VMRuntime.onAppBackgroundGc();
            }
        }
    }

    static void printRow(PrintWriter pw, String format, Object... objs) {
        pw.println(String.format(format, objs));
    }

    /* JADX INFO: Multiple debug info for r6v2 int: [D('otherSharedClean' int), D('otherPss' int)] */
    /* JADX INFO: Multiple debug info for r6v3 int: [D('otherSharedClean' int), D('otherPrivateClean' int)] */
    /* JADX INFO: Multiple debug info for r6v4 int: [D('otherSwappedOut' int), D('otherPrivateClean' int)] */
    /* JADX INFO: Multiple debug info for r6v5 int: [D('otherSwappedOutPss' int), D('otherSwappedOut' int)] */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0253: APUT  (r6v79 java.lang.Object[]), (7 ??[int, float, short, byte, char]), (r7v19 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x031d: APUT  
      (r6v85 java.lang.Object[])
      (7 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x0317: INVOKE  (r7v55 java.lang.Integer) = (r7v54 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0387: APUT  
      (r6v87 java.lang.Object[])
      (7 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x0381: INVOKE  (r7v74 java.lang.Integer) = (r7v73 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x03c4: APUT  (r6v33 java.lang.Object[]), (4 ??[int, float, short, byte, char]), (r7v8 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x044b: APUT  
      (r7v14 java.lang.Object[])
      (4 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x0446: INVOKE  (r6v56 java.lang.Integer) = (r6v55 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0493: APUT  
      (r7v15 java.lang.Object[])
      (4 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x048e: INVOKE  (r6v70 java.lang.Integer) = (r6v69 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x054b: APUT  
      (r4v77 java.lang.Object[])
      (7 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x0545: INVOKE  (r5v39 java.lang.Integer) = (r5v38 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x058c: APUT  
      (r4v75 java.lang.Object[])
      (4 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x0586: INVOKE  (r5v26 java.lang.Integer) = (r5v25 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x05ef: APUT  
      (r2v50 java.lang.Object[])
      (7 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x05ea: INVOKE  (r4v45 java.lang.Integer) = (r4v44 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0659: APUT  
      (r2v52 java.lang.Object[])
      (7 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x0654: INVOKE  (r4v64 java.lang.Integer) = (r4v63 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x06aa: APUT  
      (r4v34 java.lang.Object[])
      (4 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x06a5: INVOKE  (r2v29 java.lang.Integer) = (r2v28 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x06f0: APUT  
      (r4v35 java.lang.Object[])
      (4 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x06eb: INVOKE  (r2v41 java.lang.Integer) = (r2v40 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x07a2: APUT  
      (r5v3 java.lang.Object[])
      (7 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x079c: INVOKE  (r6v18 java.lang.Integer) = (r6v17 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x07e8: APUT  
      (r5v1 java.lang.Object[])
      (4 ??[int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x07e2: INVOKE  (r6v12 java.lang.Integer) = (r6v11 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    public static void dumpMemInfoTable(PrintWriter pw, Debug.MemoryInfo memInfo, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, int pid, String processName, long nativeMax, long nativeAllocated, long nativeFree, long dalvikMax, long dalvikAllocated, long dalvikFree) {
        String str;
        char c;
        int i;
        int i2;
        int i3;
        int i4;
        if (checkin) {
            pw.print(4);
            pw.print(',');
            pw.print(pid);
            pw.print(',');
            pw.print(processName);
            pw.print(',');
            pw.print(nativeMax);
            pw.print(',');
            pw.print(dalvikMax);
            pw.print(',');
            pw.print("N/A,");
            pw.print(nativeMax + dalvikMax);
            pw.print(',');
            pw.print(nativeAllocated);
            pw.print(',');
            pw.print(dalvikAllocated);
            pw.print(',');
            pw.print("N/A,");
            pw.print(nativeAllocated + dalvikAllocated);
            pw.print(',');
            pw.print(nativeFree);
            pw.print(',');
            pw.print(dalvikFree);
            pw.print(',');
            pw.print("N/A,");
            pw.print(nativeFree + dalvikFree);
            pw.print(',');
            pw.print(memInfo.nativePss);
            pw.print(',');
            pw.print(memInfo.dalvikPss);
            pw.print(',');
            pw.print(memInfo.otherPss);
            pw.print(',');
            pw.print(memInfo.getTotalPss());
            pw.print(',');
            pw.print(memInfo.nativeSwappablePss);
            pw.print(',');
            pw.print(memInfo.dalvikSwappablePss);
            pw.print(',');
            pw.print(memInfo.otherSwappablePss);
            pw.print(',');
            pw.print(memInfo.getTotalSwappablePss());
            pw.print(',');
            pw.print(memInfo.nativeSharedDirty);
            pw.print(',');
            pw.print(memInfo.dalvikSharedDirty);
            pw.print(',');
            pw.print(memInfo.otherSharedDirty);
            pw.print(',');
            pw.print(memInfo.getTotalSharedDirty());
            pw.print(',');
            pw.print(memInfo.nativeSharedClean);
            pw.print(',');
            pw.print(memInfo.dalvikSharedClean);
            pw.print(',');
            pw.print(memInfo.otherSharedClean);
            pw.print(',');
            pw.print(memInfo.getTotalSharedClean());
            pw.print(',');
            pw.print(memInfo.nativePrivateDirty);
            pw.print(',');
            pw.print(memInfo.dalvikPrivateDirty);
            pw.print(',');
            pw.print(memInfo.otherPrivateDirty);
            pw.print(',');
            pw.print(memInfo.getTotalPrivateDirty());
            pw.print(',');
            pw.print(memInfo.nativePrivateClean);
            pw.print(',');
            pw.print(memInfo.dalvikPrivateClean);
            pw.print(',');
            pw.print(memInfo.otherPrivateClean);
            pw.print(',');
            pw.print(memInfo.getTotalPrivateClean());
            pw.print(',');
            pw.print(memInfo.nativeSwappedOut);
            pw.print(',');
            pw.print(memInfo.dalvikSwappedOut);
            pw.print(',');
            pw.print(memInfo.otherSwappedOut);
            pw.print(',');
            pw.print(memInfo.getTotalSwappedOut());
            pw.print(',');
            if (memInfo.hasSwappedOutPss) {
                pw.print(memInfo.nativeSwappedOutPss);
                pw.print(',');
                pw.print(memInfo.dalvikSwappedOutPss);
                pw.print(',');
                pw.print(memInfo.otherSwappedOutPss);
                pw.print(',');
                pw.print(memInfo.getTotalSwappedOutPss());
                pw.print(',');
            } else {
                pw.print("N/A,");
                pw.print("N/A,");
                pw.print("N/A,");
                pw.print("N/A,");
            }
            for (int i5 = 0; i5 < 17; i5++) {
                pw.print(Debug.MemoryInfo.getOtherLabel(i5));
                pw.print(',');
                pw.print(memInfo.getOtherPss(i5));
                pw.print(',');
                pw.print(memInfo.getOtherSwappablePss(i5));
                pw.print(',');
                pw.print(memInfo.getOtherSharedDirty(i5));
                pw.print(',');
                pw.print(memInfo.getOtherSharedClean(i5));
                pw.print(',');
                pw.print(memInfo.getOtherPrivateDirty(i5));
                pw.print(',');
                pw.print(memInfo.getOtherPrivateClean(i5));
                pw.print(',');
                pw.print(memInfo.getOtherSwappedOut(i5));
                pw.print(',');
                if (memInfo.hasSwappedOutPss) {
                    pw.print(memInfo.getOtherSwappedOutPss(i5));
                    pw.print(',');
                } else {
                    pw.print("N/A,");
                }
            }
            return;
        }
        String str2 = "------";
        if (!dumpSummaryOnly) {
            if (dumpFullInfo) {
                Object[] objArr = new Object[11];
                objArr[0] = "";
                objArr[1] = "Pss";
                objArr[2] = "Pss";
                objArr[3] = "Shared";
                objArr[4] = "Private";
                objArr[5] = "Shared";
                objArr[6] = "Private";
                objArr[7] = memInfo.hasSwappedOutPss ? "SwapPss" : "Swap";
                objArr[8] = "Heap";
                objArr[9] = "Heap";
                objArr[10] = "Heap";
                printRow(pw, HEAP_FULL_COLUMN, objArr);
                printRow(pw, HEAP_FULL_COLUMN, "", "Total", "Clean", "Dirty", "Dirty", "Clean", "Clean", "Dirty", "Size", "Alloc", "Free");
                printRow(pw, HEAP_FULL_COLUMN, "", str2, str2, str2, str2, str2, str2, str2, str2, str2, str2);
                Object[] objArr2 = new Object[11];
                objArr2[0] = "Native Heap";
                objArr2[1] = Integer.valueOf(memInfo.nativePss);
                objArr2[2] = Integer.valueOf(memInfo.nativeSwappablePss);
                objArr2[3] = Integer.valueOf(memInfo.nativeSharedDirty);
                objArr2[4] = Integer.valueOf(memInfo.nativePrivateDirty);
                objArr2[5] = Integer.valueOf(memInfo.nativeSharedClean);
                objArr2[6] = Integer.valueOf(memInfo.nativePrivateClean);
                objArr2[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? memInfo.nativeSwappedOutPss : memInfo.nativeSwappedOut);
                objArr2[8] = Long.valueOf(nativeMax);
                objArr2[9] = Long.valueOf(nativeAllocated);
                objArr2[10] = Long.valueOf(nativeFree);
                printRow(pw, HEAP_FULL_COLUMN, objArr2);
                Object[] objArr3 = new Object[11];
                objArr3[0] = "Dalvik Heap";
                objArr3[1] = Integer.valueOf(memInfo.dalvikPss);
                objArr3[2] = Integer.valueOf(memInfo.dalvikSwappablePss);
                objArr3[3] = Integer.valueOf(memInfo.dalvikSharedDirty);
                objArr3[4] = Integer.valueOf(memInfo.dalvikPrivateDirty);
                objArr3[5] = Integer.valueOf(memInfo.dalvikSharedClean);
                objArr3[6] = Integer.valueOf(memInfo.dalvikPrivateClean);
                objArr3[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? memInfo.dalvikSwappedOutPss : memInfo.dalvikSwappedOut);
                objArr3[8] = Long.valueOf(dalvikMax);
                objArr3[9] = Long.valueOf(dalvikAllocated);
                objArr3[10] = Long.valueOf(dalvikFree);
                printRow(pw, HEAP_FULL_COLUMN, objArr3);
            } else {
                Object[] objArr4 = new Object[8];
                objArr4[0] = "";
                objArr4[1] = "Pss";
                objArr4[2] = "Private";
                objArr4[3] = "Private";
                objArr4[4] = memInfo.hasSwappedOutPss ? "SwapPss" : "Swap";
                objArr4[5] = "Heap";
                objArr4[6] = "Heap";
                objArr4[7] = "Heap";
                printRow(pw, HEAP_COLUMN, objArr4);
                printRow(pw, HEAP_COLUMN, "", "Total", "Dirty", "Clean", "Dirty", "Size", "Alloc", "Free");
                printRow(pw, HEAP_COLUMN, "", str2, str2, str2, str2, str2, str2, str2, str2);
                Object[] objArr5 = new Object[8];
                objArr5[0] = "Native Heap";
                objArr5[1] = Integer.valueOf(memInfo.nativePss);
                objArr5[2] = Integer.valueOf(memInfo.nativePrivateDirty);
                objArr5[3] = Integer.valueOf(memInfo.nativePrivateClean);
                if (memInfo.hasSwappedOutPss) {
                    i3 = memInfo.nativeSwappedOutPss;
                } else {
                    i3 = memInfo.nativeSwappedOut;
                }
                objArr5[4] = Integer.valueOf(i3);
                objArr5[5] = Long.valueOf(nativeMax);
                objArr5[6] = Long.valueOf(nativeAllocated);
                objArr5[7] = Long.valueOf(nativeFree);
                printRow(pw, HEAP_COLUMN, objArr5);
                Object[] objArr6 = new Object[8];
                objArr6[0] = "Dalvik Heap";
                objArr6[1] = Integer.valueOf(memInfo.dalvikPss);
                objArr6[2] = Integer.valueOf(memInfo.dalvikPrivateDirty);
                objArr6[3] = Integer.valueOf(memInfo.dalvikPrivateClean);
                if (memInfo.hasSwappedOutPss) {
                    i4 = memInfo.dalvikSwappedOutPss;
                } else {
                    i4 = memInfo.dalvikSwappedOut;
                }
                objArr6[4] = Integer.valueOf(i4);
                objArr6[5] = Long.valueOf(dalvikMax);
                objArr6[6] = Long.valueOf(dalvikAllocated);
                objArr6[7] = Long.valueOf(dalvikFree);
                printRow(pw, HEAP_COLUMN, objArr6);
            }
            int otherPss = memInfo.otherPss;
            int otherSwappablePss = memInfo.otherSwappablePss;
            int otherSharedDirty = memInfo.otherSharedDirty;
            int otherPrivateDirty = memInfo.otherPrivateDirty;
            int otherPss2 = otherPss;
            int otherSharedClean = memInfo.otherSharedClean;
            int otherPrivateClean = memInfo.otherPrivateClean;
            int otherSwappedOut = memInfo.otherSwappedOut;
            int otherSwappedOutPss = memInfo.otherSwappedOutPss;
            int i6 = 0;
            while (true) {
                str = str2;
                if (i6 >= 17) {
                    break;
                }
                int myPss = memInfo.getOtherPss(i6);
                int mySwappablePss = memInfo.getOtherSwappablePss(i6);
                int mySharedDirty = memInfo.getOtherSharedDirty(i6);
                int myPrivateDirty = memInfo.getOtherPrivateDirty(i6);
                int mySharedClean = memInfo.getOtherSharedClean(i6);
                int myPrivateClean = memInfo.getOtherPrivateClean(i6);
                int mySwappedOut = memInfo.getOtherSwappedOut(i6);
                int mySwappedOutPss = memInfo.getOtherSwappedOutPss(i6);
                if (myPss == 0 && mySharedDirty == 0 && myPrivateDirty == 0 && mySharedClean == 0 && myPrivateClean == 0) {
                    if ((memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) == 0) {
                        i6++;
                        str2 = str;
                    }
                }
                if (dumpFullInfo) {
                    Object[] objArr7 = new Object[11];
                    objArr7[0] = Debug.MemoryInfo.getOtherLabel(i6);
                    objArr7[1] = Integer.valueOf(myPss);
                    objArr7[2] = Integer.valueOf(mySwappablePss);
                    objArr7[3] = Integer.valueOf(mySharedDirty);
                    objArr7[4] = Integer.valueOf(myPrivateDirty);
                    objArr7[5] = Integer.valueOf(mySharedClean);
                    objArr7[6] = Integer.valueOf(myPrivateClean);
                    objArr7[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut);
                    objArr7[8] = "";
                    objArr7[9] = "";
                    objArr7[10] = "";
                    printRow(pw, HEAP_FULL_COLUMN, objArr7);
                } else {
                    Object[] objArr8 = new Object[8];
                    objArr8[0] = Debug.MemoryInfo.getOtherLabel(i6);
                    objArr8[1] = Integer.valueOf(myPss);
                    objArr8[2] = Integer.valueOf(myPrivateDirty);
                    objArr8[3] = Integer.valueOf(myPrivateClean);
                    objArr8[4] = Integer.valueOf(memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut);
                    objArr8[5] = "";
                    objArr8[6] = "";
                    objArr8[7] = "";
                    printRow(pw, HEAP_COLUMN, objArr8);
                }
                otherPss2 -= myPss;
                otherSwappablePss -= mySwappablePss;
                otherSharedDirty -= mySharedDirty;
                otherPrivateDirty -= myPrivateDirty;
                otherSharedClean -= mySharedClean;
                otherPrivateClean -= myPrivateClean;
                otherSwappedOut -= mySwappedOut;
                otherSwappedOutPss -= mySwappedOutPss;
                i6++;
                str2 = str;
            }
            if (dumpFullInfo) {
                Object[] objArr9 = new Object[11];
                objArr9[0] = "Unknown";
                objArr9[1] = Integer.valueOf(otherPss2);
                objArr9[2] = Integer.valueOf(otherSwappablePss);
                objArr9[3] = Integer.valueOf(otherSharedDirty);
                objArr9[4] = Integer.valueOf(otherPrivateDirty);
                objArr9[5] = Integer.valueOf(otherSharedClean);
                objArr9[6] = Integer.valueOf(otherPrivateClean);
                objArr9[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? otherSwappedOutPss : otherSwappedOut);
                objArr9[8] = "";
                objArr9[9] = "";
                objArr9[10] = "";
                printRow(pw, HEAP_FULL_COLUMN, objArr9);
                Object[] objArr10 = new Object[11];
                objArr10[0] = "TOTAL";
                objArr10[1] = Integer.valueOf(memInfo.getTotalPss());
                objArr10[2] = Integer.valueOf(memInfo.getTotalSwappablePss());
                objArr10[3] = Integer.valueOf(memInfo.getTotalSharedDirty());
                objArr10[4] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                objArr10[5] = Integer.valueOf(memInfo.getTotalSharedClean());
                objArr10[6] = Integer.valueOf(memInfo.getTotalPrivateClean());
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.getTotalSwappedOutPss();
                } else {
                    i2 = memInfo.getTotalSwappedOut();
                }
                objArr10[7] = Integer.valueOf(i2);
                objArr10[8] = Long.valueOf(nativeMax + dalvikMax);
                objArr10[9] = Long.valueOf(nativeAllocated + dalvikAllocated);
                objArr10[10] = Long.valueOf(nativeFree + dalvikFree);
                printRow(pw, HEAP_FULL_COLUMN, objArr10);
            } else {
                Object[] objArr11 = new Object[8];
                objArr11[0] = "Unknown";
                objArr11[1] = Integer.valueOf(otherPss2);
                objArr11[2] = Integer.valueOf(otherPrivateDirty);
                objArr11[3] = Integer.valueOf(otherPrivateClean);
                objArr11[4] = Integer.valueOf(memInfo.hasSwappedOutPss ? otherSwappedOutPss : otherSwappedOut);
                objArr11[5] = "";
                objArr11[6] = "";
                objArr11[7] = "";
                printRow(pw, HEAP_COLUMN, objArr11);
                Object[] objArr12 = new Object[8];
                objArr12[0] = "TOTAL";
                objArr12[1] = Integer.valueOf(memInfo.getTotalPss());
                objArr12[2] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                objArr12[3] = Integer.valueOf(memInfo.getTotalPrivateClean());
                if (memInfo.hasSwappedOutPss) {
                    i = memInfo.getTotalSwappedOutPss();
                } else {
                    i = memInfo.getTotalSwappedOut();
                }
                objArr12[4] = Integer.valueOf(i);
                objArr12[5] = Long.valueOf(nativeMax + dalvikMax);
                objArr12[6] = Long.valueOf(nativeAllocated + dalvikAllocated);
                objArr12[7] = Long.valueOf(nativeFree + dalvikFree);
                printRow(pw, HEAP_COLUMN, objArr12);
            }
            if (dumpDalvik) {
                pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(" Dalvik Details");
                for (int i7 = 17; i7 < 31; i7++) {
                    int myPss2 = memInfo.getOtherPss(i7);
                    int mySwappablePss2 = memInfo.getOtherSwappablePss(i7);
                    int mySharedDirty2 = memInfo.getOtherSharedDirty(i7);
                    int myPrivateDirty2 = memInfo.getOtherPrivateDirty(i7);
                    int mySharedClean2 = memInfo.getOtherSharedClean(i7);
                    int myPrivateClean2 = memInfo.getOtherPrivateClean(i7);
                    int mySwappedOut2 = memInfo.getOtherSwappedOut(i7);
                    int mySwappedOutPss2 = memInfo.getOtherSwappedOutPss(i7);
                    if (myPss2 == 0 && mySharedDirty2 == 0 && myPrivateDirty2 == 0 && mySharedClean2 == 0 && myPrivateClean2 == 0) {
                        if ((memInfo.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2) == 0) {
                            c = '\t';
                        }
                    }
                    if (dumpFullInfo) {
                        Object[] objArr13 = new Object[11];
                        objArr13[0] = Debug.MemoryInfo.getOtherLabel(i7);
                        objArr13[1] = Integer.valueOf(myPss2);
                        objArr13[2] = Integer.valueOf(mySwappablePss2);
                        objArr13[3] = Integer.valueOf(mySharedDirty2);
                        objArr13[4] = Integer.valueOf(myPrivateDirty2);
                        objArr13[5] = Integer.valueOf(mySharedClean2);
                        objArr13[6] = Integer.valueOf(myPrivateClean2);
                        objArr13[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2);
                        objArr13[8] = "";
                        c = '\t';
                        objArr13[9] = "";
                        objArr13[10] = "";
                        printRow(pw, HEAP_FULL_COLUMN, objArr13);
                    } else {
                        c = '\t';
                        Object[] objArr14 = new Object[8];
                        objArr14[0] = Debug.MemoryInfo.getOtherLabel(i7);
                        objArr14[1] = Integer.valueOf(myPss2);
                        objArr14[2] = Integer.valueOf(myPrivateDirty2);
                        objArr14[3] = Integer.valueOf(myPrivateClean2);
                        objArr14[4] = Integer.valueOf(memInfo.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2);
                        objArr14[5] = "";
                        objArr14[6] = "";
                        objArr14[7] = "";
                        printRow(pw, HEAP_COLUMN, objArr14);
                    }
                }
            }
        } else {
            str = str2;
        }
        pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        pw.println(" App Summary");
        printRow(pw, ONE_COUNT_COLUMN_HEADER, "", "Pss(KB)");
        printRow(pw, ONE_COUNT_COLUMN_HEADER, "", str);
        printRow(pw, ONE_COUNT_COLUMN, "Java Heap:", Integer.valueOf(memInfo.getSummaryJavaHeap()));
        printRow(pw, ONE_COUNT_COLUMN, "Native Heap:", Integer.valueOf(memInfo.getSummaryNativeHeap()));
        printRow(pw, ONE_COUNT_COLUMN, "Code:", Integer.valueOf(memInfo.getSummaryCode()));
        printRow(pw, ONE_COUNT_COLUMN, "Stack:", Integer.valueOf(memInfo.getSummaryStack()));
        printRow(pw, ONE_COUNT_COLUMN, "Graphics:", Integer.valueOf(memInfo.getSummaryGraphics()));
        printRow(pw, ONE_COUNT_COLUMN, "Private Other:", Integer.valueOf(memInfo.getSummaryPrivateOther()));
        printRow(pw, ONE_COUNT_COLUMN, "System:", Integer.valueOf(memInfo.getSummarySystem()));
        pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (memInfo.hasSwappedOutPss) {
            printRow(pw, TWO_COUNT_COLUMNS, "TOTAL:", Integer.valueOf(memInfo.getSummaryTotalPss()), "TOTAL SWAP PSS:", Integer.valueOf(memInfo.getSummaryTotalSwapPss()));
        } else {
            printRow(pw, TWO_COUNT_COLUMNS, "TOTAL:", Integer.valueOf(memInfo.getSummaryTotalPss()), "TOTAL SWAP (KB):", Integer.valueOf(memInfo.getSummaryTotalSwap()));
        }
    }

    private static void dumpMemoryInfo(ProtoOutputStream proto, long fieldId, String name, int pss, int cleanPss, int sharedDirty, int privateDirty, int sharedClean, int privateClean, boolean hasSwappedOutPss, int dirtySwap, int dirtySwapPss) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, name);
        proto.write(1120986464258L, pss);
        proto.write(1120986464259L, cleanPss);
        proto.write(1120986464260L, sharedDirty);
        proto.write(1120986464261L, privateDirty);
        proto.write(1120986464262L, sharedClean);
        proto.write(1120986464263L, privateClean);
        if (hasSwappedOutPss) {
            proto.write(1120986464265L, dirtySwapPss);
        } else {
            proto.write(1120986464264L, dirtySwap);
        }
        proto.end(token);
    }

    /* JADX INFO: Multiple debug info for r1v20 int: [D('otherSwappedOut' int), D('dvToken' long)] */
    public static void dumpMemInfoTable(ProtoOutputStream proto, Debug.MemoryInfo memInfo, boolean dumpDalvik, boolean dumpSummaryOnly, long nativeMax, long nativeAllocated, long nativeFree, long dalvikMax, long dalvikAllocated, long dalvikFree) {
        ProtoOutputStream protoOutputStream;
        Debug.MemoryInfo memoryInfo;
        long tToken;
        long dvToken;
        int i;
        if (!dumpSummaryOnly) {
            long nhToken = proto.start(1146756268035L);
            dumpMemoryInfo(proto, 1146756268033L, "Native Heap", memInfo.nativePss, memInfo.nativeSwappablePss, memInfo.nativeSharedDirty, memInfo.nativePrivateDirty, memInfo.nativeSharedClean, memInfo.nativePrivateClean, memInfo.hasSwappedOutPss, memInfo.nativeSwappedOut, memInfo.nativeSwappedOutPss);
            protoOutputStream = proto;
            protoOutputStream.write(1120986464258L, nativeMax);
            protoOutputStream.write(1120986464259L, nativeAllocated);
            protoOutputStream.write(1120986464260L, nativeFree);
            protoOutputStream.end(nhToken);
            long dvToken2 = protoOutputStream.start(1146756268036L);
            dumpMemoryInfo(proto, 1146756268033L, "Dalvik Heap", memInfo.dalvikPss, memInfo.dalvikSwappablePss, memInfo.dalvikSharedDirty, memInfo.dalvikPrivateDirty, memInfo.dalvikSharedClean, memInfo.dalvikPrivateClean, memInfo.hasSwappedOutPss, memInfo.dalvikSwappedOut, memInfo.dalvikSwappedOutPss);
            protoOutputStream.write(1120986464258L, dalvikMax);
            protoOutputStream.write(1120986464259L, dalvikAllocated);
            protoOutputStream.write(1120986464260L, dalvikFree);
            protoOutputStream.end(dvToken2);
            Debug.MemoryInfo memoryInfo2 = memInfo;
            int otherPss = memoryInfo2.otherPss;
            int otherSwappablePss = memoryInfo2.otherSwappablePss;
            int otherSharedDirty = memoryInfo2.otherSharedDirty;
            int otherPrivateDirty = memoryInfo2.otherPrivateDirty;
            int otherSharedClean = memoryInfo2.otherSharedClean;
            int otherPrivateClean = memoryInfo2.otherPrivateClean;
            int otherSwappedOut = memoryInfo2.otherSwappedOut;
            int otherSwappedOutPss = memoryInfo2.otherSwappedOutPss;
            int otherSwappablePss2 = otherSwappablePss;
            int otherSharedDirty2 = otherSharedDirty;
            int otherPrivateDirty2 = otherPrivateDirty;
            int otherSharedClean2 = otherSharedClean;
            int otherPrivateClean2 = otherPrivateClean;
            int otherPrivateClean3 = 0;
            int otherPss2 = otherPss;
            while (otherPrivateClean3 < 17) {
                int myPss = memoryInfo2.getOtherPss(otherPrivateClean3);
                int mySwappablePss = memoryInfo2.getOtherSwappablePss(otherPrivateClean3);
                int mySharedDirty = memoryInfo2.getOtherSharedDirty(otherPrivateClean3);
                int myPrivateDirty = memoryInfo2.getOtherPrivateDirty(otherPrivateClean3);
                int mySharedClean = memoryInfo2.getOtherSharedClean(otherPrivateClean3);
                int myPrivateClean = memoryInfo2.getOtherPrivateClean(otherPrivateClean3);
                int mySwappedOut = memoryInfo2.getOtherSwappedOut(otherPrivateClean3);
                int mySwappedOutPss = memoryInfo2.getOtherSwappedOutPss(otherPrivateClean3);
                if (myPss == 0 && mySharedDirty == 0 && myPrivateDirty == 0 && mySharedClean == 0 && myPrivateClean == 0) {
                    if ((memoryInfo2.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) == 0) {
                        i = otherPrivateClean3;
                        dvToken = dvToken2;
                        otherPrivateClean3 = i + 1;
                        memoryInfo2 = memInfo;
                        dvToken2 = dvToken;
                    }
                }
                dvToken = dvToken2;
                i = otherPrivateClean3;
                dumpMemoryInfo(proto, 2246267895813L, Debug.MemoryInfo.getOtherLabel(otherPrivateClean3), myPss, mySwappablePss, mySharedDirty, myPrivateDirty, mySharedClean, myPrivateClean, memoryInfo2.hasSwappedOutPss, mySwappedOut, mySwappedOutPss);
                otherPss2 -= myPss;
                otherSwappablePss2 -= mySwappablePss;
                otherSharedDirty2 -= mySharedDirty;
                otherPrivateDirty2 -= myPrivateDirty;
                otherSharedClean2 -= mySharedClean;
                otherPrivateClean2 -= myPrivateClean;
                otherSwappedOut -= mySwappedOut;
                otherSwappedOutPss -= mySwappedOutPss;
                otherPrivateClean3 = i + 1;
                memoryInfo2 = memInfo;
                dvToken2 = dvToken;
            }
            memoryInfo = memInfo;
            dumpMemoryInfo(proto, 1146756268038L, "Unknown", otherPss2, otherSwappablePss2, otherSharedDirty2, otherPrivateDirty2, otherSharedClean2, otherPrivateClean2, memInfo.hasSwappedOutPss, otherSwappedOut, otherSwappedOutPss);
            long tToken2 = protoOutputStream.start(1146756268039L);
            dumpMemoryInfo(proto, 1146756268033L, "TOTAL", memInfo.getTotalPss(), memInfo.getTotalSwappablePss(), memInfo.getTotalSharedDirty(), memInfo.getTotalPrivateDirty(), memInfo.getTotalSharedClean(), memInfo.getTotalPrivateClean(), memoryInfo.hasSwappedOutPss, memInfo.getTotalSwappedOut(), memInfo.getTotalSwappedOutPss());
            protoOutputStream.write(1120986464258L, nativeMax + dalvikMax);
            protoOutputStream.write(1120986464259L, nativeAllocated + dalvikAllocated);
            protoOutputStream.write(1120986464260L, nativeFree + dalvikFree);
            long tToken3 = tToken2;
            protoOutputStream.end(tToken3);
            if (dumpDalvik) {
                int i2 = 17;
                while (i2 < 31) {
                    int myPss2 = memoryInfo.getOtherPss(i2);
                    int mySwappablePss2 = memoryInfo.getOtherSwappablePss(i2);
                    int mySharedDirty2 = memoryInfo.getOtherSharedDirty(i2);
                    int myPrivateDirty2 = memoryInfo.getOtherPrivateDirty(i2);
                    int mySharedClean2 = memoryInfo.getOtherSharedClean(i2);
                    int myPrivateClean2 = memoryInfo.getOtherPrivateClean(i2);
                    int mySwappedOut2 = memoryInfo.getOtherSwappedOut(i2);
                    int mySwappedOutPss2 = memoryInfo.getOtherSwappedOutPss(i2);
                    if (myPss2 == 0 && mySharedDirty2 == 0 && myPrivateDirty2 == 0 && mySharedClean2 == 0 && myPrivateClean2 == 0) {
                        if ((memoryInfo.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2) == 0) {
                            tToken = tToken3;
                            i2++;
                            tToken3 = tToken;
                        }
                    }
                    tToken = tToken3;
                    dumpMemoryInfo(proto, 2246267895816L, Debug.MemoryInfo.getOtherLabel(i2), myPss2, mySwappablePss2, mySharedDirty2, myPrivateDirty2, mySharedClean2, myPrivateClean2, memoryInfo.hasSwappedOutPss, mySwappedOut2, mySwappedOutPss2);
                    i2++;
                    tToken3 = tToken;
                }
            }
        } else {
            protoOutputStream = proto;
            memoryInfo = memInfo;
        }
        long asToken = protoOutputStream.start(1146756268041L);
        protoOutputStream.write(1120986464257L, memInfo.getSummaryJavaHeap());
        protoOutputStream.write(1120986464258L, memInfo.getSummaryNativeHeap());
        protoOutputStream.write(1120986464259L, memInfo.getSummaryCode());
        protoOutputStream.write(1120986464260L, memInfo.getSummaryStack());
        protoOutputStream.write(1120986464261L, memInfo.getSummaryGraphics());
        protoOutputStream.write(1120986464262L, memInfo.getSummaryPrivateOther());
        protoOutputStream.write(1120986464263L, memInfo.getSummarySystem());
        if (memoryInfo.hasSwappedOutPss) {
            protoOutputStream.write(1120986464264L, memInfo.getSummaryTotalSwapPss());
        } else {
            protoOutputStream.write(1120986464264L, memInfo.getSummaryTotalSwap());
        }
        protoOutputStream.end(asToken);
    }

    @UnsupportedAppUsage
    public void registerOnActivityPausedListener(Activity activity, OnActivityPausedListener listener) {
        synchronized (this.mOnPauseListeners) {
            ArrayList<OnActivityPausedListener> list = this.mOnPauseListeners.get(activity);
            if (list == null) {
                list = new ArrayList<>();
                this.mOnPauseListeners.put(activity, list);
            }
            list.add(listener);
        }
    }

    @UnsupportedAppUsage
    public void unregisterOnActivityPausedListener(Activity activity, OnActivityPausedListener listener) {
        synchronized (this.mOnPauseListeners) {
            ArrayList<OnActivityPausedListener> list = this.mOnPauseListeners.get(activity);
            if (list != null) {
                list.remove(listener);
            }
        }
    }

    public final ActivityInfo resolveActivityInfo(Intent intent) {
        ActivityInfo aInfo = intent.resolveActivityInfo(this.mInitialApplication.getPackageManager(), 1024);
        if (aInfo == null) {
            Instrumentation.checkStartActivityResult(-92, intent);
        }
        return aInfo;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final Activity startActivityNow(Activity parent, String id, Intent intent, ActivityInfo activityInfo, IBinder token, Bundle state, Activity.NonConfigurationInstances lastNonConfigurationInstances, IBinder assistToken) {
        String name;
        ActivityClientRecord r = new ActivityClientRecord();
        r.token = token;
        r.assistToken = assistToken;
        r.ident = 0;
        r.intent = intent;
        r.state = state;
        r.parent = parent;
        r.embeddedID = id;
        r.activityInfo = activityInfo;
        r.lastNonConfigurationInstances = lastNonConfigurationInstances;
        if (localLOGV) {
            ComponentName compname = intent.getComponent();
            if (compname != null) {
                name = compname.toShortString();
            } else {
                name = "(Intent " + intent + ").getComponent() returned null";
            }
            Slog.v(TAG, "Performing launch: action=" + intent.getAction() + ", comp=" + name + ", token=" + token);
        }
        return performLaunchActivity(r, null);
    }

    @Override // android.app.ClientTransactionHandler
    @UnsupportedAppUsage
    public final Activity getActivity(IBinder token) {
        ActivityClientRecord activityRecord = this.mActivities.get(token);
        if (activityRecord != null) {
            return activityRecord.activity;
        }
        return null;
    }

    @Override // android.app.ClientTransactionHandler
    public ActivityClientRecord getActivityClient(IBinder token) {
        return this.mActivities.get(token);
    }

    @Override // android.app.ClientTransactionHandler
    public void updatePendingConfiguration(Configuration config) {
        synchronized (this.mResourcesManager) {
            if (HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
                config = updateConfig(config);
            }
            if (this.mPendingConfiguration == null || this.mPendingConfiguration.isOtherSeqNewer(config)) {
                this.mPendingConfiguration = config;
            }
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void updateProcessState(int processState, boolean fromIpc) {
        synchronized (this.mAppThread) {
            if (this.mLastProcessState != processState) {
                this.mLastProcessState = processState;
                if (processState != 2 || this.mNumLaunchingActivities.get() <= 0) {
                    this.mPendingProcessState = -1;
                    updateVmProcessState(processState);
                } else {
                    this.mPendingProcessState = processState;
                    this.mH.postDelayed(new Runnable() {
                        /* class android.app.$$Lambda$ActivityThread$A4ykhsPb8qV3ffTqpQDklHSMDJ0 */

                        @Override // java.lang.Runnable
                        public final void run() {
                            ActivityThread.this.applyPendingProcessState();
                        }
                    }, 1000);
                }
                if (localLOGV) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("******************* PROCESS STATE CHANGED TO: ");
                    sb.append(processState);
                    sb.append(fromIpc ? " (from ipc" : "");
                    Slog.i(TAG, sb.toString());
                }
            }
        }
    }

    private void updateVmProcessState(int processState) {
        int state;
        if (processState <= 7) {
            state = 0;
        } else {
            state = 1;
        }
        if (sIsMygote || processState != 2) {
            VMRuntime.getRuntime().updateProcessState(state);
        } else {
            VMRuntime.getRuntime().updateProcessState(7);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void applyPendingProcessState() {
        synchronized (this.mAppThread) {
            if (this.mPendingProcessState != -1) {
                int pendingState = this.mPendingProcessState;
                this.mPendingProcessState = -1;
                if (pendingState == this.mLastProcessState) {
                    updateVmProcessState(pendingState);
                }
            }
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void countLaunchingActivities(int num) {
        this.mNumLaunchingActivities.getAndAdd(num);
    }

    @UnsupportedAppUsage
    public final void sendActivityResult(IBinder token, String id, int requestCode, int resultCode, Intent data) {
        if (DEBUG_RESULTS) {
            Slog.v(TAG, "sendActivityResult: id=" + id + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
        ArrayList<ResultInfo> list = new ArrayList<>();
        list.add(new ResultInfo(id, requestCode, resultCode, data));
        ClientTransaction clientTransaction = ClientTransaction.obtain(this.mAppThread, token);
        clientTransaction.addCallback(ActivityResultItem.obtain(list));
        try {
            this.mAppThread.scheduleTransaction(clientTransaction);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.app.ClientTransactionHandler
    public TransactionExecutor getTransactionExecutor() {
        return this.mTransactionExecutor;
    }

    /* access modifiers changed from: package-private */
    @Override // android.app.ClientTransactionHandler
    public void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessage(int what, Object obj, int arg1) {
        sendMessage(what, obj, arg1, 0, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        sendMessage(what, obj, arg1, arg2, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mH.sendMessage(msg);
        if (HwFrameworkFactory.getHwActivityThread() != null) {
            HwFrameworkFactory.getHwActivityThread().handleHwPreloadStatus(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessageAtFront(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mH.sendMessageAtFrontOfQueue(msg);
        if (HwFrameworkFactory.getHwActivityThread() != null) {
            HwFrameworkFactory.getHwActivityThread().handleHwPreloadStatus(3);
        }
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, int seq) {
        Message msg = Message.obtain();
        msg.what = what;
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = obj;
        args.argi1 = arg1;
        args.argi2 = arg2;
        args.argi3 = seq;
        msg.obj = args;
        this.mH.sendMessage(msg);
        if (HwFrameworkFactory.getHwActivityThread() != null) {
            HwFrameworkFactory.getHwActivityThread().handleHwPreloadStatus(3);
        }
    }

    /* access modifiers changed from: package-private */
    public final void scheduleContextCleanup(ContextImpl context, String who, String what) {
        ContextCleanupInfo cci = new ContextCleanupInfo();
        cci.context = context;
        cci.who = who;
        cci.what = what;
        sendMessage(119, cci);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:43:0x011c */
    /* JADX DEBUG: Multi-variable search result rejected for r6v11, resolved type: android.app.Instrumentation */
    /* JADX DEBUG: Multi-variable search result rejected for r6v12, resolved type: android.app.Instrumentation */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* JADX WARN: Type inference failed for: r1v17, types: [android.content.Intent] */
    /* JADX WARN: Type inference failed for: r4v7 */
    /* JADX WARN: Type inference failed for: r1v18 */
    /* JADX WARN: Type inference failed for: r1v27, types: [android.app.Activity] */
    /* JADX WARN: Type inference failed for: r4v13, types: [android.app.Instrumentation] */
    /* JADX WARN: Type inference failed for: r1v30, types: [android.app.Activity] */
    /* JADX WARN: Type inference failed for: r4v16 */
    /* JADX WARN: Type inference failed for: r4v24, types: [android.app.ActivityThread] */
    /* JADX WARN: Type inference failed for: r1v31 */
    /* JADX WARN: Type inference failed for: r4v30 */
    /* JADX WARN: Type inference failed for: r4v31 */
    /* JADX WARN: Type inference failed for: r1v43 */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0293, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0294, code lost:
        r1 = r6;
        r28 = r9;
        r4 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02cc, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0293 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:20:0x0094] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x02a7  */
    /* JADX WARNING: Unknown variable types count: 2 */
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ComponentName component;
        Activity activity;
        SuperNotCalledException e;
        Activity activity2;
        ComponentName component2;
        ActivityThread activityThread;
        Exception e2;
        ActivityThread activityThread2;
        ActivityClientRecord activityClientRecord;
        Window window;
        ActivityThread activityThread3;
        Activity activity3;
        ActivityInfo aInfo = r.activityInfo;
        if (r.packageInfo == null) {
            r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo, 1);
        }
        ComponentName component3 = r.intent.getComponent();
        if (component3 == null) {
            component3 = r.intent.resolveActivity(this.mInitialApplication.getPackageManager());
            r.intent.setComponent(component3);
        }
        if (r.activityInfo.targetActivity != null) {
            component = new ComponentName(r.activityInfo.packageName, r.activityInfo.targetActivity);
        } else {
            component = component3;
        }
        ContextImpl appContext = createBaseContextForActivity(r);
        this.mResourcesManager.applyConfigurationToAppResourcesLocked(appContext.getDisplayId(), r.overrideConfig);
        try {
            ClassLoader cl = appContext.getClassLoader();
            Activity activity4 = this.mInstrumentation.newActivity(cl, component.getClassName(), r.intent);
            this.mCurrentActivity = component.getClassName();
            StrictMode.incrementExpectedActivityCount(activity4.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
            activity = activity4;
        } catch (Exception e3) {
            if (this.mInstrumentation.onException(null, e3)) {
                activity = null;
            } else {
                throw new RuntimeException("Unable to instantiate activity " + component + ": " + e3.toString(), e3);
            }
        }
        try {
            Application app = r.packageInfo.makeApplication(false, this.mInstrumentation);
            if (activity != null) {
                CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
                Configuration config = new Configuration(this.mCompatConfiguration);
                if (r.overrideConfig != null) {
                    try {
                        config.updateFrom(r.overrideConfig);
                    } catch (SuperNotCalledException e4) {
                        e = e4;
                    } catch (Exception e5) {
                        e2 = e5;
                        activity2 = activity;
                        component2 = component;
                        activityThread = this;
                        if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                        }
                        return activity2;
                    }
                }
                if (localLOGV) {
                    Slog.v(TAG, "Performing launch of " + r + " with config " + config);
                }
                ?? r4 = 0;
                r4 = 0;
                r4 = 0;
                if (r.mPendingRemoveWindow == null || !r.mPreserveWindow) {
                    window = null;
                } else {
                    Window window2 = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                    window = window2;
                }
                appContext.setOuterContext(activity);
                Instrumentation instrumentation = getInstrumentation();
                IBinder iBinder = r.token;
                int i = r.ident;
                ?? r1 = r.intent;
                try {
                    try {
                    } catch (SuperNotCalledException e6) {
                        e = e6;
                        throw e;
                    } catch (Exception e7) {
                        e2 = e7;
                        activity3 = activity;
                        component2 = component;
                        activityThread3 = this;
                        activity2 = activity3;
                        activityThread = activityThread3;
                        if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                        }
                        return activity2;
                    }
                } catch (SuperNotCalledException e8) {
                    e = e8;
                    throw e;
                } catch (Exception e9) {
                    e2 = e9;
                    activity3 = activity;
                    component2 = component;
                    activityThread3 = this;
                    activity2 = activity3;
                    activityThread = activityThread3;
                    if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                    }
                    return activity2;
                }
                try {
                    r1 = activity;
                    r4 = instrumentation;
                    component2 = component;
                    try {
                        r1.attach(appContext, this, r4, iBinder, i, app, r1, r.activityInfo, title, r.parent, r.embeddedID, r.lastNonConfigurationInstances, config, r.referrer, r.voiceInteractor, window, r.configCallback, r.assistToken);
                        if (customIntent != null) {
                            Activity activity5 = activity;
                            try {
                                activity5.mIntent = customIntent;
                                r1 = activity5;
                            } catch (SuperNotCalledException e10) {
                                e = e10;
                            } catch (Exception e11) {
                                e2 = e11;
                                activityThread = this;
                                activity2 = activity5;
                                if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                                }
                                return activity2;
                            }
                        } else {
                            r1 = activity;
                        }
                        activityClientRecord = r;
                        r4 = 0;
                    } catch (SuperNotCalledException e12) {
                        e = e12;
                        throw e;
                    } catch (Exception e13) {
                        e2 = e13;
                        activityThread = this;
                        activity2 = activity;
                        if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                            throw new RuntimeException("Unable to start activity " + component2 + ": " + e2.toString(), e2);
                        }
                        return activity2;
                    }
                    try {
                        activityClientRecord.lastNonConfigurationInstances = null;
                        checkAndBlockForNetworkAccess();
                        r1.mStartedActivity = false;
                        int theme = activityClientRecord.activityInfo.getThemeResource();
                        if (theme != 0) {
                            r1.setTheme(theme);
                        }
                        r1.mCalled = false;
                        Slog.v(TAG, "callActivityOnCreate");
                        if (r.isPersistable()) {
                            r4 = this;
                            try {
                                r4.mInstrumentation.callActivityOnCreate(r1, activityClientRecord.state, activityClientRecord.persistentState);
                                activityThread2 = r4;
                            } catch (SuperNotCalledException e14) {
                                e = e14;
                                throw e;
                            } catch (Exception e15) {
                                e2 = e15;
                                activity2 = r1;
                                activityThread = r4;
                                if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                                }
                                return activity2;
                            }
                        } else {
                            ActivityThread activityThread4 = this;
                            activityThread4.mInstrumentation.callActivityOnCreate(r1, activityClientRecord.state);
                            activityThread2 = activityThread4;
                        }
                        if (r1.mCalled) {
                            activityClientRecord.activity = r1;
                            activity2 = r1;
                        } else {
                            throw new SuperNotCalledException("Activity " + activityClientRecord.intent.getComponent().toShortString() + " did not call through to super.onCreate()");
                        }
                    } catch (SuperNotCalledException e16) {
                        e = e16;
                        throw e;
                    } catch (Exception e17) {
                        e2 = e17;
                        activityThread = this;
                        activity2 = r1;
                        if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                        }
                        return activity2;
                    }
                } catch (SuperNotCalledException e18) {
                    e = e18;
                    throw e;
                } catch (Exception e19) {
                    e2 = e19;
                    activityThread3 = this;
                    activity3 = activity;
                    component2 = component;
                    activity2 = activity3;
                    activityThread = activityThread3;
                    if (!activityThread.mInstrumentation.onException(activity2, e2)) {
                    }
                    return activity2;
                }
            } else {
                activity2 = activity;
                activityClientRecord = r;
                activityThread2 = this;
            }
            activityClientRecord.setState(1);
            synchronized (activityThread2.mResourcesManager) {
                activityThread2.mActivities.put(activityClientRecord.token, activityClientRecord);
            }
            Slog.d(TAG, "add activity client record, r= " + activityClientRecord + " token= " + activityClientRecord.token);
        } catch (SuperNotCalledException e20) {
            e = e20;
            throw e;
        } catch (Exception e21) {
        }
        return activity2;
    }

    @Override // android.app.ClientTransactionHandler
    public void handleStartActivity(ActivityClientRecord r, PendingTransactionActions pendingActions) {
        Activity activity = r.activity;
        if (r.activity != null) {
            if (!r.stopped) {
                throw new IllegalStateException("Can't start activity that is not stopped.");
            } else if (!r.activity.mFinished) {
                activity.performStart("handleStartActivity");
                r.setState(2);
                if (pendingActions != null) {
                    if (pendingActions.shouldRestoreInstanceState()) {
                        if (r.isPersistable()) {
                            if (!(r.state == null && r.persistentState == null)) {
                                this.mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state, r.persistentState);
                            }
                        } else if (r.state != null) {
                            this.mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);
                        }
                    }
                    if (pendingActions.shouldCallOnPostCreate()) {
                        activity.mCalled = false;
                        if (r.isPersistable()) {
                            this.mInstrumentation.callActivityOnPostCreate(activity, r.state, r.persistentState);
                        } else {
                            this.mInstrumentation.callActivityOnPostCreate(activity, r.state);
                        }
                        if (!activity.mCalled) {
                            throw new SuperNotCalledException("Activity " + r.intent.getComponent().toShortString() + " did not call through to super.onPostCreate()");
                        }
                    }
                }
            }
        }
    }

    private void checkAndBlockForNetworkAccess() {
        synchronized (this.mNetworkPolicyLock) {
            if (this.mNetworkBlockSeq != -1) {
                try {
                    ActivityManager.getService().waitForNetworkStateUpdate(this.mNetworkBlockSeq);
                    this.mNetworkBlockSeq = -1;
                } catch (RemoteException e) {
                }
            }
        }
    }

    private ContextImpl createBaseContextForActivity(ActivityClientRecord r) {
        int displayId;
        if (!LocalActivityManager.isLocalActivityRecord(r.token) || r.parent == null) {
            try {
                displayId = ActivityTaskManager.getService().getActivityDisplayId(r.token);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            displayId = r.parent.getDisplayId();
        }
        if (displayId != this.mDisplayId && HwPCUtils.isPcCastMode()) {
            if (r.parent != null && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
                displayId = this.mDisplayId;
            }
            if (HwPCUtils.enabledInPad() && displayId != 0 && displayId != -1 && !HwPCUtils.isValidExtDisplayId(displayId)) {
                HwPCUtils.log(TAG, "displayId :" + displayId + ",mDisplayId:" + this.mDisplayId + ",getPCDisplayID:" + HwPCUtils.getPCDisplayID());
                HwPCUtils.setPCDisplayID(displayId);
            }
        }
        ContextImpl appContext = ContextImpl.createActivityContext(this, r.packageInfo, r.activityInfo, r.token, displayId, r.overrideConfig);
        DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
        String pkgName = SystemProperties.get("debug.second-display.pkg");
        if (pkgName == null || pkgName.isEmpty() || !r.packageInfo.mPackageName.contains(pkgName)) {
            return appContext;
        }
        int[] displayIds = dm.getDisplayIds();
        for (int id : displayIds) {
            if (id != 0) {
                return (ContextImpl) appContext.createDisplayContext(dm.getCompatibleDisplay(id, appContext.getResources()));
            }
        }
        return appContext;
    }

    @Override // android.app.ClientTransactionHandler
    public Activity handleLaunchActivity(ActivityClientRecord r, PendingTransactionActions pendingActions, Intent customIntent) {
        if (!(r == null || r.intent == null || r.intent.getComponent() == null)) {
            Jlog.d(336, r.intent.getComponent().getPackageName() + "/" + r.intent.getComponent().getClassName(), "");
        }
        if (this.mIsNeedStartUiProbe && this.mZrHungAppEyeUiProbe != null) {
            ZrHungData data = new ZrHungData();
            data.putString(CalendarContract.RemindersColumns.METHOD, Telephony.BaseMmsColumns.START);
            this.mZrHungAppEyeUiProbe.check(data);
        }
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        if (!(r == null || r.profilerInfo == null)) {
            this.mProfiler.setProfiler(r.profilerInfo);
            this.mProfiler.startProfiling();
        }
        handleConfigurationChanged(null, null);
        if (localLOGV) {
            Slog.v(TAG, "Handling launch of " + r);
        }
        if (!(ThreadedRenderer.sRendererDisabled || r == null || (r.activityInfo.flags & 512) == 0)) {
            HardwareRenderer.preload();
        }
        WindowManagerGlobal.initialize();
        if (r == null) {
            return null;
        }
        GraphicsEnvironment.hintActivityLaunch();
        Activity a = performLaunchActivity(r, customIntent);
        if (a != null) {
            r.createdConfig = new Configuration(this.mConfiguration);
            reportSizeConfigurations(r);
            if (!r.activity.mFinished && pendingActions != null) {
                pendingActions.setOldState(r.state);
                pendingActions.setRestoreInstanceState(true);
                pendingActions.setCallOnPostCreate(true);
            }
        } else {
            try {
                ActivityTaskManager.getService().finishActivity(r.token, 0, null, 0);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        return a;
    }

    private void reportSizeConfigurations(ActivityClientRecord r) {
        Configuration[] configurations;
        if (!this.mActivitiesToBeDestroyed.containsKey(r.token) && (configurations = r.activity.getResources().getSizeConfigurations()) != null) {
            SparseIntArray horizontal = new SparseIntArray();
            SparseIntArray vertical = new SparseIntArray();
            SparseIntArray smallest = new SparseIntArray();
            for (int i = configurations.length - 1; i >= 0; i--) {
                Configuration config = configurations[i];
                if (config.screenHeightDp != 0) {
                    vertical.put(config.screenHeightDp, 0);
                }
                if (config.screenWidthDp != 0) {
                    horizontal.put(config.screenWidthDp, 0);
                }
                if (config.smallestScreenWidthDp != 0) {
                    smallest.put(config.smallestScreenWidthDp, 0);
                }
            }
            try {
                ActivityTaskManager.getService().reportSizeConfigurations(r.token, horizontal.copyKeys(), vertical.copyKeys(), smallest.copyKeys());
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    private void deliverNewIntents(ActivityClientRecord r, List<ReferrerIntent> intents) {
        int N = intents.size();
        for (int i = 0; i < N; i++) {
            ReferrerIntent intent = intents.get(i);
            intent.setExtrasClassLoader(r.activity.getClassLoader());
            intent.prepareToEnterProcess();
            r.activity.mFragments.noteStateNotSaved();
            this.mInstrumentation.callActivityOnNewIntent(r.activity, intent);
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleNewIntent(IBinder token, List<ReferrerIntent> intents) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            checkAndBlockForNetworkAccess();
            deliverNewIntents(r, intents);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleScheduleReportRT() {
        AppBindData appBindData = this.mBoundApplication;
        if (appBindData == null) {
            return false;
        }
        return doReportRuntime(appBindData.appInfo.processName, this.mStartTime);
    }

    public void handleRequestAssistContextExtras(RequestAssistContextExtras cmd) {
        Uri referrer;
        AssistStructure structure;
        RemoteException e;
        boolean notSecure = false;
        boolean forAutofill = cmd.requestType == 2;
        if (this.mLastSessionId != cmd.sessionId) {
            this.mLastSessionId = cmd.sessionId;
            for (int i = this.mLastAssistStructures.size() - 1; i >= 0; i--) {
                AssistStructure structure2 = this.mLastAssistStructures.get(i).get();
                if (structure2 != null) {
                    structure2.clearSendChannel();
                }
                this.mLastAssistStructures.remove(i);
            }
        }
        Bundle data = new Bundle();
        AssistStructure structure3 = null;
        AssistContent content = forAutofill ? null : new AssistContent();
        long startTime = SystemClock.uptimeMillis();
        ActivityClientRecord r = this.mActivities.get(cmd.activityToken);
        Uri referrer2 = null;
        if (r != null) {
            if (!forAutofill) {
                r.activity.getApplication().dispatchOnProvideAssistData(r.activity, data);
                r.activity.onProvideAssistData(data);
                referrer2 = r.activity.onProvideReferrer();
            }
            if (cmd.requestType == 1 || forAutofill) {
                structure3 = new AssistStructure(r.activity, forAutofill, cmd.flags);
                Intent activityIntent = r.activity.getIntent();
                if (r.window == null || (r.window.getAttributes().flags & 8192) == 0) {
                    notSecure = true;
                }
                if (activityIntent == null || !notSecure) {
                    if (!forAutofill) {
                        content.setDefaultIntent(new Intent());
                    }
                } else if (!forAutofill) {
                    Intent intent = new Intent(activityIntent);
                    intent.setFlags(intent.getFlags() & -67);
                    intent.removeUnsafeExtras();
                    content.setDefaultIntent(intent);
                }
                if (!forAutofill) {
                    r.activity.onProvideAssistContent(content);
                }
                referrer = referrer2;
            } else {
                referrer = referrer2;
            }
        } else {
            referrer = null;
        }
        if (structure3 == null) {
            structure = new AssistStructure();
        } else {
            structure = structure3;
        }
        structure.setAcquisitionStartTime(startTime);
        structure.setAcquisitionEndTime(SystemClock.uptimeMillis());
        this.mLastAssistStructures.add(new WeakReference<>(structure));
        try {
            try {
                ActivityTaskManager.getService().reportAssistContextExtras(cmd.requestToken, data, structure, content, referrer);
            } catch (RemoteException e2) {
                e = e2;
            }
        } catch (RemoteException e3) {
            e = e3;
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleRequestDirectActions(IBinder activityToken, IVoiceInteractor interactor, CancellationSignal cancellationSignal, RemoteCallback callback) {
        ActivityClientRecord r = this.mActivities.get(activityToken);
        if (r == null) {
            Log.w(TAG, "requestDirectActions(): no activity for " + activityToken);
            callback.sendResult(null);
            return;
        }
        int lifecycleState = r.getLifecycleState();
        if (lifecycleState < 2 || lifecycleState >= 5) {
            Log.w(TAG, "requestDirectActions(" + r + "): wrong lifecycle: " + lifecycleState);
            callback.sendResult(null);
            return;
        }
        if (r.activity.mVoiceInteractor == null || r.activity.mVoiceInteractor.mInteractor.asBinder() != interactor.asBinder()) {
            if (r.activity.mVoiceInteractor != null) {
                r.activity.mVoiceInteractor.destroy();
            }
            r.activity.mVoiceInteractor = new VoiceInteractor(interactor, r.activity, r.activity, Looper.myLooper());
        }
        r.activity.onGetDirectActions(cancellationSignal, new Consumer(callback) {
            /* class android.app.$$Lambda$ActivityThread$FmvGY8exyv0L0oqZrnunpl8OFI8 */
            private final /* synthetic */ RemoteCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityThread.lambda$handleRequestDirectActions$0(ActivityThread.ActivityClientRecord.this, this.f$1, (List) obj);
            }
        });
    }

    static /* synthetic */ void lambda$handleRequestDirectActions$0(ActivityClientRecord r, RemoteCallback callback, List actions) {
        Preconditions.checkNotNull(actions);
        Preconditions.checkCollectionElementsNotNull(actions, Slice.HINT_ACTIONS);
        if (!actions.isEmpty()) {
            int actionCount = actions.size();
            for (int i = 0; i < actionCount; i++) {
                ((DirectAction) actions.get(i)).setSource(r.activity.getTaskId(), r.activity.getAssistToken());
            }
            Bundle result = new Bundle();
            result.putParcelable(DirectAction.KEY_ACTIONS_LIST, new ParceledListSlice(actions));
            callback.sendResult(result);
            return;
        }
        callback.sendResult(null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handlePerformDirectAction(IBinder activityToken, String actionId, Bundle arguments, CancellationSignal cancellationSignal, RemoteCallback resultCallback) {
        ActivityClientRecord r = this.mActivities.get(activityToken);
        if (r != null) {
            int lifecycleState = r.getLifecycleState();
            if (lifecycleState < 2 || lifecycleState >= 5) {
                resultCallback.sendResult(null);
                return;
            }
            Bundle nonNullArguments = arguments != null ? arguments : Bundle.EMPTY;
            Activity activity = r.activity;
            Objects.requireNonNull(resultCallback);
            activity.onPerformDirectAction(actionId, nonNullArguments, cancellationSignal, new Consumer() {
                /* class android.app.$$Lambda$ZsFzoG2loyqNOR2cNbothrNK5c */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    RemoteCallback.this.sendResult((Bundle) obj);
                }
            });
            return;
        }
        resultCallback.sendResult(null);
    }

    public void handleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            r.activity.onTranslucentConversionComplete(drawComplete);
        }
    }

    public void onNewActivityOptions(IBinder token, ActivityOptions options) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            r.activity.onNewActivityOptions(options);
        }
    }

    public void handleInstallProvider(ProviderInfo info) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            installContentProviders(this.mInitialApplication, Arrays.asList(info));
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEnterAnimationComplete(IBinder token) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            r.activity.dispatchEnterAnimationComplete();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStartBinderTracking() {
        Binder.enableTracing();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopBinderTrackingAndDump(ParcelFileDescriptor fd) {
        try {
            Binder.disableTracing();
            Binder.getTransactionTracker().writeTracesToFile(fd);
        } finally {
            IoUtils.closeQuietly(fd);
            Binder.getTransactionTracker().clearTraces();
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode, Configuration overrideConfig) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            Configuration newConfig = new Configuration(this.mConfiguration);
            if (overrideConfig != null) {
                newConfig.updateFrom(overrideConfig);
            }
            r.activity.dispatchMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handlePictureInPictureModeChanged(IBinder token, boolean isInPipMode, Configuration overrideConfig) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            Configuration newConfig = new Configuration(this.mConfiguration);
            if (overrideConfig != null) {
                newConfig.updateFrom(overrideConfig);
            }
            r.activity.dispatchPictureInPictureModeChanged(isInPipMode, newConfig);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor interactor) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            r.voiceInteractor = interactor;
            r.activity.setVoiceInteractor(interactor);
            if (interactor == null) {
                r.activity.onLocalVoiceInteractionStopped();
            } else {
                r.activity.onLocalVoiceInteractionStarted();
            }
        }
    }

    private static boolean attemptAttachAgent(String agent, ClassLoader classLoader) {
        try {
            VMDebug.attachAgent(agent, classLoader);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "Attaching agent with " + classLoader + " failed: " + agent);
            return false;
        }
    }

    static void handleAttachAgent(String agent, LoadedApk loadedApk) {
        ClassLoader classLoader = loadedApk != null ? loadedApk.getClassLoader() : null;
        if (!attemptAttachAgent(agent, classLoader) && classLoader != null) {
            attemptAttachAgent(agent, null);
        }
    }

    public static Intent getIntentBeingBroadcast() {
        return sCurrentBroadcastIntent.get();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private void handleReceiver(ReceiverData data) {
        unscheduleGcIdler();
        String component = data.intent.getComponent().getClassName();
        LoadedApk packageInfo = getPackageInfoNoCheck(data.info.applicationInfo, data.compatInfo);
        IActivityManager mgr = ActivityManager.getService();
        try {
            Application app = packageInfo.makeApplication(false, this.mInstrumentation);
            ContextImpl context = (ContextImpl) app.getBaseContext();
            if (data.info.splitName != null) {
                context = (ContextImpl) context.createContextForSplit(data.info.splitName);
            }
            ClassLoader cl = context.getClassLoader();
            data.intent.setExtrasClassLoader(cl);
            data.intent.prepareToEnterProcess();
            data.setExtrasClassLoader(cl);
            BroadcastReceiver receiver = packageInfo.getAppFactory().instantiateReceiver(cl, data.info.name, data.intent);
            try {
                if (DEBUG_BROADCAST) {
                    Slog.v(TAG, "Performing receive of " + data.intent + ": app=" + app + ", appName=" + app.getPackageName() + ", pkg=" + packageInfo.getPackageName() + ", comp=" + data.intent.getComponent().toShortString() + ", dir=" + packageInfo.getAppDir());
                }
                sCurrentBroadcastIntent.set(data.intent);
                receiver.setPendingResult(data);
                receiver.onReceive(context.getReceiverRestrictedContext(), data.intent);
            } catch (Exception e) {
                if (DEBUG_BROADCAST) {
                    Slog.i(TAG, "Finishing failed broadcast to " + data.intent.getComponent());
                }
                data.sendFinished(mgr);
                if (!this.mInstrumentation.onException(receiver, e)) {
                    throw new RuntimeException("Unable to start receiver " + component + ": " + e.toString(), e);
                }
            } catch (Throwable th) {
                sCurrentBroadcastIntent.set(null);
                throw th;
            }
            sCurrentBroadcastIntent.set(null);
            if (receiver.getPendingResult() != null) {
                data.finish();
            }
        } catch (Exception e2) {
            if (DEBUG_BROADCAST) {
                Slog.i(TAG, "Finishing failed broadcast to " + data.intent.getComponent());
            }
            data.sendFinished(mgr);
            throw new RuntimeException("Unable to instantiate receiver " + component + ": " + e2.toString(), e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCreateBackupAgent(CreateBackupAgentData data) {
        try {
            if (getPackageManager().getPackageInfo(data.appInfo.packageName, 0, UserHandle.myUserId()).applicationInfo.uid != Process.myUid()) {
                Slog.w(TAG, "Asked to instantiate non-matching package " + data.appInfo.packageName);
                return;
            }
            unscheduleGcIdler();
            LoadedApk packageInfo = getPackageInfoNoCheck(data.appInfo, data.compatInfo);
            String packageName = packageInfo.mPackageName;
            if (packageName == null) {
                Slog.d(TAG, "Asked to create backup agent for nonexistent package");
                return;
            }
            String classname = data.appInfo.backupAgentName;
            if (classname == null && (data.backupMode == 1 || data.backupMode == 3)) {
                classname = "android.app.backup.FullBackupAgent";
            }
            IBinder binder = null;
            try {
                ArrayMap<String, BackupAgent> backupAgents = getBackupAgentsForUser(data.userId);
                BackupAgent agent = backupAgents.get(packageName);
                if (agent != null) {
                    binder = agent.onBind();
                } else {
                    try {
                        BackupAgent agent2 = (BackupAgent) packageInfo.getClassLoader().loadClass(classname).newInstance();
                        ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
                        context.setOuterContext(agent2);
                        agent2.attach(context);
                        agent2.onCreate(UserHandle.of(data.userId));
                        binder = agent2.onBind();
                        backupAgents.put(packageName, agent2);
                    } catch (Exception e) {
                        Slog.e(TAG, "Agent threw during creation: " + e);
                        if (!(data.backupMode == 2 || data.backupMode == 3)) {
                            throw e;
                        }
                    }
                }
                try {
                    ActivityManager.getService().backupAgentCreated(packageName, binder, data.userId);
                } catch (RemoteException e2) {
                    throw e2.rethrowFromSystemServer();
                }
            } catch (Exception e3) {
                throw new RuntimeException("Unable to create BackupAgent " + classname + ": " + e3.toString(), e3);
            }
        } catch (RemoteException e4) {
            throw e4.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDestroyBackupAgent(CreateBackupAgentData data) {
        String packageName = getPackageInfoNoCheck(data.appInfo, data.compatInfo).mPackageName;
        ArrayMap<String, BackupAgent> backupAgents = getBackupAgentsForUser(data.userId);
        BackupAgent agent = backupAgents.get(packageName);
        if (agent != null) {
            try {
                agent.onDestroy();
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown in onDestroy by backup agent of " + data.appInfo);
                e.printStackTrace();
            }
            backupAgents.remove(packageName);
            return;
        }
        Slog.w(TAG, "Attempt to destroy unknown backup agent " + data);
    }

    private ArrayMap<String, BackupAgent> getBackupAgentsForUser(int userId) {
        ArrayMap<String, BackupAgent> backupAgents = this.mBackupAgentsByUser.get(userId);
        if (backupAgents != null) {
            return backupAgents;
        }
        ArrayMap<String, BackupAgent> backupAgents2 = new ArrayMap<>();
        this.mBackupAgentsByUser.put(userId, backupAgents2);
        return backupAgents2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleCreateService(CreateServiceData data) {
        unscheduleGcIdler();
        LoadedApk packageInfo = getPackageInfoNoCheck(data.info.applicationInfo, data.compatInfo);
        Service service = null;
        try {
            ClassLoader cl = packageInfo.getClassLoader();
            ApplicationInfo aInfo = packageInfo.getApplicationInfo();
            if (aInfo != null && aInfo.requestsIsolatedSplitLoading() && data.info.splitName != null && !ArrayUtils.isEmpty(aInfo.splitNames)) {
                cl = packageInfo.getSplitClassLoader(data.info.splitName);
            }
            service = packageInfo.getAppFactory().instantiateService(cl, data.info.name, data.intent);
        } catch (Exception e) {
            if (!this.mInstrumentation.onException(null, e)) {
                throw new RuntimeException("Unable to instantiate service " + data.info.name + ": " + e.toString(), e);
            }
        }
        try {
            if (localLOGV) {
                Slog.v(TAG, "Creating service " + data.info.name);
            }
            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
            context.setOuterContext(service);
            service.attach(context, this, data.info.name, data.token, packageInfo.makeApplication(false, this.mInstrumentation), ActivityManager.getService());
            service.onCreate();
            this.mServices.put(data.token, service);
            try {
                ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
            } catch (RemoteException e2) {
                Flog.w(102, "serviceDone failed when creating service " + data.info.name + ": " + e2.toString());
                throw e2.rethrowFromSystemServer();
            }
        } catch (Exception e3) {
            if (!this.mInstrumentation.onException(service, e3)) {
                throw new RuntimeException("Unable to create service " + data.info.name + ": " + e3.toString(), e3);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBindService(BindServiceData data) {
        Service s = this.mServices.get(data.token);
        if (DEBUG_SERVICE) {
            Slog.v(TAG, "handleBindService s=" + s + " rebind=" + data.rebind);
        }
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                try {
                    if (!data.rebind) {
                        ActivityManager.getService().publishService(data.token, data.intent, s.onBind(data.intent));
                        return;
                    }
                    s.onRebind(data.intent);
                    ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
                } catch (RemoteException ex) {
                    Flog.w(102, "publishService failed when binding service " + s + " with " + data.intent + ": " + ex.toString());
                    throw ex.rethrowFromSystemServer();
                }
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(s, e)) {
                    throw new RuntimeException("Unable to bind to service " + s + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        } else {
            Flog.v(101, "service can't be binded");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUnbindService(BindServiceData data) {
        Service s = this.mServices.get(data.token);
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                boolean doRebind = s.onUnbind(data.intent);
                if (doRebind) {
                    try {
                        ActivityManager.getService().unbindFinished(data.token, data.intent, doRebind);
                    } catch (RemoteException ex) {
                        Flog.w(102, "Unable to finish unbind to service " + s + " with " + data.intent + ": " + ex.toString());
                        throw ex.rethrowFromSystemServer();
                    }
                } else {
                    ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
                }
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(s, e)) {
                    throw new RuntimeException("Unable to unbind to service " + s + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDumpService(DumpComponentInfo info) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            Service s = this.mServices.get(info.token);
            if (s != null) {
                PrintWriter pw = new FastPrintWriter(new FileOutputStream(info.fd.getFileDescriptor()));
                s.dump(info.fd.getFileDescriptor(), pw, info.args);
                pw.flush();
            }
        } finally {
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDumpActivity(DumpComponentInfo info) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            ActivityClientRecord r = this.mActivities.get(info.token);
            if (!(r == null || r.activity == null)) {
                PrintWriter pw = new FastPrintWriter(new FileOutputStream(info.fd.getFileDescriptor()));
                r.activity.dump(info.prefix, info.fd.getFileDescriptor(), pw, info.args);
                pw.flush();
            }
        } finally {
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDumpProvider(DumpComponentInfo info) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            ProviderClientRecord r = this.mLocalProviders.get(info.token);
            if (!(r == null || r.mLocalProvider == null)) {
                PrintWriter pw = new FastPrintWriter(new FileOutputStream(info.fd.getFileDescriptor()));
                r.mLocalProvider.dump(info.fd.getFileDescriptor(), pw, info.args);
                pw.flush();
            }
        } finally {
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceArgs(ServiceArgsData data) {
        int res;
        Service s = this.mServices.get(data.token);
        if (s != null) {
            try {
                if (data.args != null) {
                    data.args.setExtrasClassLoader(s.getClassLoader());
                    data.args.prepareToEnterProcess();
                }
                if (!data.taskRemoved) {
                    res = s.onStartCommand(data.args, data.flags, data.startId);
                } else {
                    s.onTaskRemoved(data.args);
                    res = 1000;
                }
                QueuedWork.waitToFinish();
                try {
                    ActivityManager.getService().serviceDoneExecuting(data.token, 1, data.startId, res);
                } catch (RemoteException e) {
                    Flog.w(102, "Unable to finish starting service " + s + " with " + data.args + ": " + e.toString());
                    throw e.rethrowFromSystemServer();
                }
            } catch (Exception e2) {
                if (!this.mInstrumentation.onException(s, e2)) {
                    throw new RuntimeException("Unable to start service " + s + " with " + data.args + ": " + e2.toString(), e2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopService(IBinder token) {
        Service s = this.mServices.remove(token);
        if (s != null) {
            try {
                if (localLOGV) {
                    Slog.v(TAG, "Destroying service " + s);
                }
                s.onDestroy();
                s.detachAndCleanUp();
                Context context = s.getBaseContext();
                if (context instanceof ContextImpl) {
                    ((ContextImpl) context).scheduleFinalCleanup(s.getClassName(), "Service");
                }
                QueuedWork.waitToFinish();
                try {
                    ActivityManager.getService().serviceDoneExecuting(token, 2, 0, 0);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } catch (Exception e2) {
                if (this.mInstrumentation.onException(s, e2)) {
                    Slog.i(TAG, "handleStopService: exception for " + token, e2);
                    return;
                }
                throw new RuntimeException("Unable to stop service " + s + ": " + e2.toString(), e2);
            }
        } else {
            Slog.i(TAG, "handleStopService: token=" + token + " not found.");
        }
    }

    @VisibleForTesting
    public ActivityClientRecord performResumeActivity(IBinder token, boolean finalStateRequest, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (localLOGV) {
            Slog.v(TAG, "Performing resume of " + r + " finished=" + r.activity.mFinished + " for reason:" + reason);
        }
        if (r == null || r.activity.mFinished) {
            return null;
        }
        if (r.getLifecycleState() == 3) {
            if (!finalStateRequest && !HwPCUtils.PKG_DESKTOP_EXPLORER.equals(r.activity.getPackageName())) {
                RuntimeException e = new IllegalStateException("Trying to resume activity which is already resumed");
                Slog.e(TAG, e.getMessage(), e);
                Slog.e(TAG, r.getStateString());
            }
            return null;
        }
        ComponentName componentName = r.activity.getComponentName();
        if (componentName != null) {
            this.mCurrentActivity = componentName.getClassName();
        }
        if (finalStateRequest) {
            r.hideForNow = false;
            r.activity.mStartedActivity = false;
        }
        try {
            this.mResourcesManager.applyConfigurationToAppResourcesLocked(r.activity.getDisplayId(), r.overrideConfig);
            r.activity.onStateNotSaved();
            r.activity.mFragments.noteStateNotSaved();
            checkAndBlockForNetworkAccess();
            if (r.pendingIntents != null) {
                deliverNewIntents(r, r.pendingIntents);
                r.pendingIntents = null;
            }
            if (r.pendingResults != null) {
                deliverResults(r, r.pendingResults, reason);
                r.pendingResults = null;
            }
            r.activity.performResume(r.startsNotResumed, reason);
            r.state = null;
            r.persistentState = null;
            r.setState(3);
            reportTopResumedActivityChanged(r, r.isTopResumedActivity, "topWhenResuming");
        } catch (Exception e2) {
            if (!this.mInstrumentation.onException(r.activity, e2)) {
                throw new RuntimeException("Unable to resume activity " + r.intent.getComponent().toShortString() + ": " + e2.toString(), e2);
            }
        }
        return r;
    }

    static final void cleanUpPendingRemoveWindows(ActivityClientRecord r, boolean force) {
        if (!r.mPreserveWindow || force) {
            if (r.mPendingRemoveWindow != null) {
                r.mPendingRemoveWindowManager.removeViewImmediate(r.mPendingRemoveWindow.getDecorView());
                IBinder wtoken = r.mPendingRemoveWindow.getDecorView().getWindowToken();
                if (wtoken != null) {
                    WindowManagerGlobal.getInstance().closeAll(wtoken, r.activity.getClass().getName(), "Activity");
                }
            }
            r.mPendingRemoveWindow = null;
            r.mPendingRemoveWindowManager = null;
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleResumeActivity(IBinder token, boolean finalStateRequest, boolean isForward, String reason) {
        IAwareSdk.asyncSendData("UniperfClient", 4098);
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        if (this.mIsNeedStartUiProbe && this.mZrHungAppEyeUiProbe != null) {
            ZrHungData data = new ZrHungData();
            data.putString(CalendarContract.RemindersColumns.METHOD, Telephony.BaseMmsColumns.START);
            this.mZrHungAppEyeUiProbe.check(data);
        }
        ActivityClientRecord r = performResumeActivity(token, finalStateRequest, reason);
        if (r != null && !this.mActivitiesToBeDestroyed.containsKey(token)) {
            Activity a = r.activity;
            if (localLOGV) {
                Slog.v(TAG, "Resume " + r + " started activity: " + a.mStartedActivity + ", hideForNow: " + r.hideForNow + ", finished: " + a.mFinished);
            }
            int forwardBit = isForward ? 256 : 0;
            boolean willBeVisible = !a.mStartedActivity;
            if (!willBeVisible) {
                try {
                    willBeVisible = ActivityTaskManager.getService().willActivityBeVisible(a.getActivityToken());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            if (r.window == null && !a.mFinished && willBeVisible) {
                r.window = r.activity.getWindow();
                View decor = r.window.getDecorView();
                decor.setVisibility(4);
                ViewManager wm = a.getWindowManager();
                WindowManager.LayoutParams l = r.window.getAttributes();
                a.mDecor = decor;
                l.type = 1;
                l.softInputMode |= forwardBit;
                if (r.mPreserveWindow) {
                    a.mWindowAdded = true;
                    r.mPreserveWindow = false;
                    ViewRootImpl impl = decor.getViewRootImpl();
                    if (impl != null) {
                        impl.notifyChildRebuilt();
                    }
                }
                if (a.mVisibleFromClient) {
                    if (!a.mWindowAdded) {
                        a.mWindowAdded = true;
                        wm.addView(decor, l);
                    } else {
                        a.onWindowAttributesChanged(l);
                    }
                }
            } else if (!willBeVisible) {
                if (localLOGV) {
                    Slog.v(TAG, "Launch " + r + " mStartedActivity set");
                }
                r.hideForNow = true;
            }
            cleanUpPendingRemoveWindows(r, false);
            if (!r.activity.mFinished && willBeVisible && r.activity.mDecor != null && !r.hideForNow) {
                if (r.newConfig != null) {
                    performConfigurationChangedForActivity(r, r.newConfig);
                    if (DEBUG_CONFIGURATION) {
                        Slog.v(TAG, "Resuming activity " + r.activityInfo.name + " with newConfig " + r.activity.mCurrentConfig);
                    }
                    r.newConfig = null;
                }
                if (localLOGV) {
                    Slog.v(TAG, "Resuming " + r + " with isForward=" + isForward);
                }
                WindowManager.LayoutParams l2 = r.window.getAttributes();
                if ((l2.softInputMode & 256) != forwardBit) {
                    l2.softInputMode = (l2.softInputMode & TrafficStats.TAG_NETWORK_STACK_RANGE_END) | forwardBit;
                    if (r.activity.mVisibleFromClient) {
                        a.getWindowManager().updateViewLayout(r.window.getDecorView(), l2);
                    }
                }
                r.activity.mVisibleFromServer = true;
                this.mNumVisibleActivities++;
                if (r.activity.mVisibleFromClient) {
                    r.activity.makeVisible();
                }
            }
            updateNavigationBarColor(r.activity);
            r.nextIdle = this.mNewActivities;
            this.mNewActivities = r;
            if (localLOGV) {
                Slog.v(TAG, "Scheduling idle handler for " + r);
            }
            this.mHwViewRootImpl.setIsFirstFrame(true);
            if (!(r.intent == null || r.intent.getComponent() == null)) {
                Jlog.betaUserPrint(408, r.intent.getComponent().getPackageName() + "/" + r.intent.getComponent().getClassName(), "");
            }
            Looper.myQueue().addIdleHandler(new Idler());
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleTopResumedActivityChanged(IBinder token, boolean onTop, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null || r.activity == null) {
            Slog.w(TAG, "Not found target activity to report position change for token: " + token);
            return;
        }
        if (DEBUG_ORDER) {
            Slog.d(TAG, "Received position change to top: " + onTop + " for activity: " + r);
        }
        if (r.isTopResumedActivity == onTop) {
            Slog.e(TAG, "Activity top position already set to onTop=" + onTop);
            return;
        }
        r.isTopResumedActivity = onTop;
        if (r.getLifecycleState() == 3) {
            if (onTop) {
                this.mResourcesManager.applyConfigurationToAppResourcesLocked(r.activity.getDisplayId(), r.overrideConfig);
            }
            reportTopResumedActivityChanged(r, onTop, "topStateChangedWhenResumed");
            return;
        }
        Slog.d(TAG, "Won't deliver top position change in state=" + r.getLifecycleState());
    }

    private void reportTopResumedActivityChanged(ActivityClientRecord r, boolean onTop, String reason) {
        if (r.lastReportedTopResumedState != onTop) {
            r.lastReportedTopResumedState = onTop;
            r.activity.performTopResumedActivityChanged(onTop, reason);
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handlePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, PendingTransactionActions pendingActions, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        long startTime = SystemClock.uptimeMillis();
        if (localLOGV) {
            Slog.d(TAG, "Handling pause of " + r + ", finished: " + finished + " userLeaving:" + userLeaving);
        }
        if (r != null) {
            if (userLeaving) {
                performUserLeavingActivity(r);
            }
            r.activity.mConfigChangeFlags |= configChanges;
            performPauseActivity(r, finished, reason, pendingActions);
            if (r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            this.mSomeActivitiesChanged = true;
            Jlog.betaUserPrint(386, startTime, r.activity.getComponentName().getClassName() + " paused");
            if (this.mIsNeedStartUiProbe && this.mZrHungAppEyeUiProbe != null) {
                ZrHungData data = new ZrHungData();
                data.putString(CalendarContract.RemindersColumns.METHOD, "stop");
                this.mZrHungAppEyeUiProbe.check(data);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void performUserLeavingActivity(ActivityClientRecord r) {
        this.mInstrumentation.callActivityOnUserLeaving(r.activity);
    }

    /* access modifiers changed from: package-private */
    public final Bundle performPauseActivity(IBinder token, boolean finished, String reason, PendingTransactionActions pendingActions) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            return performPauseActivity(r, finished, reason, pendingActions);
        }
        return null;
    }

    private Bundle performPauseActivity(ActivityClientRecord r, boolean finished, String reason, PendingTransactionActions pendingActions) {
        ArrayList<OnActivityPausedListener> listeners;
        if (r.paused) {
            if (r.activity.mFinished) {
                return null;
            }
            RuntimeException e = new RuntimeException("Performing pause of activity that is not resumed: " + r.intent.getComponent().toShortString());
            Slog.e(TAG, e.getMessage(), e);
        }
        boolean shouldSaveState = true;
        if (finished) {
            r.activity.mFinished = true;
        }
        int size = 0;
        if (r.activity.mFinished || !r.isPreHoneycomb()) {
            shouldSaveState = false;
        }
        if (shouldSaveState) {
            callActivityOnSaveInstanceState(r);
        }
        performPauseActivityIfNeeded(r, reason);
        synchronized (this.mOnPauseListeners) {
            listeners = this.mOnPauseListeners.remove(r.activity);
        }
        if (listeners != null) {
            size = listeners.size();
        }
        for (int i = 0; i < size; i++) {
            listeners.get(i).onPaused(r.activity);
        }
        Bundle oldState = pendingActions != null ? pendingActions.getOldState() : null;
        if (oldState != null && r.isPreHoneycomb()) {
            r.state = oldState;
        }
        if (shouldSaveState) {
            return r.state;
        }
        return null;
    }

    private void performPauseActivityIfNeeded(ActivityClientRecord r, String reason) {
        if (!r.paused) {
            if (localLOGV) {
                Slog.d(TAG, "Performing pause of " + r + " for reason: " + reason);
            }
            reportTopResumedActivityChanged(r, false, "pausing");
            try {
                r.activity.mCalled = false;
                this.mInstrumentation.callActivityOnPause(r.activity);
                if (r.activity.mCalled) {
                    r.setState(4);
                    return;
                }
                throw new SuperNotCalledException("Activity " + safeToComponentShortString(r.intent) + " did not call through to super.onPause()");
            } catch (SuperNotCalledException e) {
                throw e;
            } catch (Exception e2) {
                if (!this.mInstrumentation.onException(r.activity, e2)) {
                    throw new RuntimeException("Unable to pause activity " + safeToComponentShortString(r.intent) + ": " + e2.toString(), e2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void performStopActivity(IBinder token, boolean saveState, String reason) {
        performStopActivityInner(this.mActivities.get(token), null, false, saveState, false, reason);
    }

    /* access modifiers changed from: private */
    public static final class ProviderRefCount {
        public final ProviderClientRecord client;
        public final ContentProviderHolder holder;
        public boolean removePending;
        public int stableCount;
        public int unstableCount;

        ProviderRefCount(ContentProviderHolder inHolder, ProviderClientRecord inClient, int sCount, int uCount) {
            this.holder = inHolder;
            this.client = inClient;
            this.stableCount = sCount;
            this.unstableCount = uCount;
        }
    }

    private void performStopActivityInner(ActivityClientRecord r, PendingTransactionActions.StopInfo info, boolean keepShown, boolean saveState, boolean finalStateRequest, String reason) {
        if (localLOGV) {
            Slog.v(TAG, "Performing stop of " + r + " keepShown:" + keepShown + " for reason:" + reason);
        }
        if (r != null) {
            if (!keepShown && r.stopped) {
                if (!r.activity.mFinished) {
                    if (!finalStateRequest) {
                        RuntimeException e = new RuntimeException("Performing stop of activity that is already stopped: " + r.intent.getComponent().toShortString());
                        Slog.e(TAG, e.getMessage(), e);
                        Slog.e(TAG, r.getStateString());
                    }
                } else {
                    return;
                }
            }
            performPauseActivityIfNeeded(r, reason);
            if (info != null) {
                try {
                    info.setDescription(r.activity.onCreateDescription());
                } catch (Exception e2) {
                    if (!this.mInstrumentation.onException(r.activity, e2)) {
                        throw new RuntimeException("Unable to save state of activity " + r.intent.getComponent().toShortString() + ": " + e2.toString(), e2);
                    }
                }
            }
            if (!keepShown) {
                callActivityOnStop(r, saveState, reason);
            }
        }
    }

    private void callActivityOnStop(ActivityClientRecord r, boolean saveState, String reason) {
        boolean shouldSaveState = saveState && !r.activity.mFinished && r.state == null && !r.isPreHoneycomb();
        boolean isPreP = r.isPreP();
        if (shouldSaveState && isPreP) {
            callActivityOnSaveInstanceState(r);
        }
        try {
            r.activity.performStop(r.mPreserveWindow, reason);
        } catch (SuperNotCalledException e) {
            throw e;
        } catch (Exception e2) {
            if (!this.mInstrumentation.onException(r.activity, e2)) {
                throw new RuntimeException("Unable to stop activity " + r.intent.getComponent().toShortString() + ": " + e2.toString(), e2);
            }
        }
        r.setState(5);
        if (shouldSaveState && !isPreP) {
            callActivityOnSaveInstanceState(r);
        }
    }

    private void updateVisibility(ActivityClientRecord r, boolean show) {
        View v = r.activity.mDecor;
        if (v == null) {
            return;
        }
        if (show) {
            if (!r.activity.mVisibleFromServer) {
                r.activity.mVisibleFromServer = true;
                this.mNumVisibleActivities++;
                if (r.activity.mVisibleFromClient) {
                    r.activity.makeVisible();
                }
            }
            if (r.newConfig != null) {
                performConfigurationChangedForActivity(r, r.newConfig);
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Updating activity vis " + r.activityInfo.name + " with new config " + r.activity.mCurrentConfig);
                }
                r.newConfig = null;
            }
        } else if (r.activity.mVisibleFromServer) {
            r.activity.mVisibleFromServer = false;
            this.mNumVisibleActivities--;
            v.setVisibility(4);
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleStopActivity(IBinder token, boolean show, int configChanges, PendingTransactionActions pendingActions, boolean finalStateRequest, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        r.activity.mConfigChangeFlags |= configChanges;
        PendingTransactionActions.StopInfo stopInfo = new PendingTransactionActions.StopInfo();
        performStopActivityInner(r, stopInfo, show, true, finalStateRequest, reason);
        if (localLOGV) {
            Slog.v(TAG, "Finishing stop of " + r + ": show=" + show + " win=" + r.window);
        }
        updateVisibility(r, show);
        if (!r.isPreHoneycomb()) {
            QueuedWork.waitToFinish();
        }
        stopInfo.setActivity(r);
        stopInfo.setState(r.state);
        stopInfo.setPersistentState(r.persistentState);
        pendingActions.setStopInfo(stopInfo);
        this.mSomeActivitiesChanged = true;
    }

    @Override // android.app.ClientTransactionHandler
    public void reportStop(PendingTransactionActions pendingActions) {
        this.mH.post(pendingActions.getStopInfo());
    }

    @Override // android.app.ClientTransactionHandler
    public void performRestartActivity(IBinder token, boolean start) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (localLOGV) {
            Slog.v(TAG, "Performing restart of " + r + " stopped=" + r.stopped);
        }
        if (r.stopped) {
            r.activity.performRestart(start, "performRestartActivity");
            if (start) {
                r.setState(2);
            }
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleWindowVisibility(IBinder token, boolean show) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "handleWindowVisibility: no activity for token " + token);
            return;
        }
        if (!show && !r.stopped) {
            performStopActivityInner(r, null, show, false, false, "handleWindowVisibility");
        } else if (show && r.getLifecycleState() == 5) {
            unscheduleGcIdler();
            r.activity.performRestart(true, "handleWindowVisibility");
            r.setState(2);
        }
        if (r.activity.mDecor != null) {
            Slog.i(TAG, "Handle window " + r + " visibility: " + show);
            updateVisibility(r, show);
        }
        this.mSomeActivitiesChanged = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSleeping(IBinder token, boolean sleeping) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "handleSleeping: no activity for token " + token);
        } else if (sleeping) {
            if (!r.stopped && !r.isPreHoneycomb()) {
                callActivityOnStop(r, true, "sleeping");
            }
            if (!r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            try {
                ActivityTaskManager.getService().activitySlept(r.token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else if (r.stopped && r.activity.mVisibleFromServer) {
            r.activity.performRestart(true, "handleSleeping");
            r.setState(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetCoreSettings(Bundle coreSettings) {
        synchronized (this.mResourcesManager) {
            this.mCoreSettings = coreSettings;
        }
        onCoreSettingsChange();
    }

    private void onCoreSettingsChange() {
        if (updateDebugViewAttributeState()) {
            relaunchAllActivities(false, false);
        }
    }

    private boolean updateDebugViewAttributeState() {
        boolean previousState = View.sDebugViewAttributes;
        String currentPackage = "";
        View.sDebugViewAttributesApplicationPackage = this.mCoreSettings.getString(Settings.Global.DEBUG_VIEW_ATTRIBUTES_APPLICATION_PACKAGE, currentPackage);
        AppBindData appBindData = this.mBoundApplication;
        if (!(appBindData == null || appBindData.appInfo == null)) {
            currentPackage = this.mBoundApplication.appInfo.packageName;
        }
        View.sDebugViewAttributes = this.mCoreSettings.getInt(Settings.Global.DEBUG_VIEW_ATTRIBUTES, 0) != 0 || View.sDebugViewAttributesApplicationPackage.equals(currentPackage);
        return previousState != View.sDebugViewAttributes;
    }

    private void relaunchAllActivities(boolean preserveWindows, boolean fromThemeChange) {
        for (Map.Entry<IBinder, ActivityClientRecord> entry : this.mActivities.entrySet()) {
            if (!entry.getValue().activity.mFinished) {
                if (preserveWindows && entry.getValue().window != null) {
                    entry.getValue().mPreserveWindow = true;
                }
                scheduleRelaunchActivity(entry.getKey(), fromThemeChange);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdatePackageCompatibilityInfo(UpdateCompatibilityData data) {
        LoadedApk apk = peekPackageInfo(data.pkg, false);
        if (apk != null) {
            apk.setCompatibilityInfo(data.info);
        }
        LoadedApk apk2 = peekPackageInfo(data.pkg, true);
        if (apk2 != null) {
            apk2.setCompatibilityInfo(data.info);
        }
        handleConfigurationChanged(this.mConfiguration, data.info);
        WindowManagerGlobal.getInstance().reportNewConfiguration(this.mConfiguration);
    }

    private void deliverResults(ActivityClientRecord r, List<ResultInfo> results, String reason) {
        int N = results.size();
        for (int i = 0; i < N; i++) {
            ResultInfo ri = results.get(i);
            try {
                if (ri.mData != null) {
                    ri.mData.setExtrasClassLoader(r.activity.getClassLoader());
                    ri.mData.prepareToEnterProcess();
                }
                if (DEBUG_RESULTS) {
                    Slog.v(TAG, "Delivering result to activity " + r + " : " + ri);
                }
                r.activity.dispatchActivityResult(ri.mResultWho, ri.mRequestCode, ri.mResultCode, ri.mData, reason);
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(r.activity, e)) {
                    throw new RuntimeException("Failure delivering result " + ri + " to activity " + r.intent.getComponent().toShortString() + ": " + e.toString(), e);
                }
            }
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleSendResult(IBinder token, List<ResultInfo> results, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (DEBUG_RESULTS) {
            Slog.v(TAG, "Handling send result to " + r);
        }
        if (r != null) {
            boolean resumed = !r.paused;
            if (!r.activity.mFinished && r.activity.mDecor != null && r.hideForNow && resumed) {
                updateVisibility(r, true);
            }
            if (resumed) {
                try {
                    r.activity.mCalled = false;
                    this.mInstrumentation.callActivityOnPause(r.activity);
                    if (!r.activity.mCalled) {
                        throw new SuperNotCalledException("Activity " + r.intent.getComponent().toShortString() + " did not call through to super.onPause()");
                    }
                } catch (SuperNotCalledException e) {
                    throw e;
                } catch (Exception e2) {
                    if (!this.mInstrumentation.onException(r.activity, e2)) {
                        throw new RuntimeException("Unable to pause activity " + r.intent.getComponent().toShortString() + ": " + e2.toString(), e2);
                    }
                }
            }
            checkAndBlockForNetworkAccess();
            deliverResults(r, results, reason);
            if (resumed) {
                r.activity.performResume(false, reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityClientRecord performDestroyActivity(IBinder token, boolean finishing, int configChanges, boolean getNonConfigInstance, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        Class<?> cls = null;
        if (localLOGV) {
            Slog.v(TAG, "Performing destroy of " + r + " finishing:" + finishing);
        }
        if (r != null) {
            cls = r.activity.getClass();
            r.activity.mConfigChangeFlags |= configChanges;
            if (finishing) {
                r.activity.mFinished = true;
            }
            performPauseActivityIfNeeded(r, "destroy");
            if (!r.stopped) {
                callActivityOnStop(r, false, "destroy");
            }
            if (getNonConfigInstance) {
                try {
                    r.lastNonConfigurationInstances = r.activity.retainNonConfigurationInstances();
                } catch (Exception e) {
                    if (!this.mInstrumentation.onException(r.activity, e)) {
                        throw new RuntimeException("Unable to retain activity " + r.intent.getComponent().toShortString() + ": " + e.toString(), e);
                    }
                }
            }
            try {
                r.activity.mCalled = false;
                this.mInstrumentation.callActivityOnDestroy(r.activity);
                if (r.activity.mCalled) {
                    if (r.window != null) {
                        r.window.closeAllPanels();
                    }
                    r.setState(6);
                } else {
                    throw new SuperNotCalledException("Activity " + safeToComponentShortString(r.intent) + " did not call through to super.onDestroy()");
                }
            } catch (SuperNotCalledException e2) {
                throw e2;
            } catch (Exception e3) {
                if (!this.mInstrumentation.onException(r.activity, e3)) {
                    throw new RuntimeException("Unable to destroy activity " + safeToComponentShortString(r.intent) + ": " + e3.toString(), e3);
                }
            }
        }
        schedulePurgeIdler();
        synchronized (this.mResourcesManager) {
            this.mActivities.remove(token);
        }
        Slog.d(TAG, "Remove activity client record, r= " + r + " token= " + token);
        StrictMode.decrementExpectedActivityCount(cls);
        return r;
    }

    private static String safeToComponentShortString(Intent intent) {
        ComponentName component = intent.getComponent();
        return component == null ? "[Unknown]" : component.toShortString();
    }

    @Override // android.app.ClientTransactionHandler
    public Map<IBinder, ClientTransactionItem> getActivitiesToBeDestroyed() {
        return this.mActivitiesToBeDestroyed;
    }

    @Override // android.app.ClientTransactionHandler
    public void handleDestroyActivity(IBinder token, boolean finishing, int configChanges, boolean getNonConfigInstance, String reason) {
        ActivityClientRecord r = performDestroyActivity(token, finishing, configChanges, getNonConfigInstance, reason);
        if (r != null) {
            cleanUpPendingRemoveWindows(r, finishing);
            WindowManager wm = r.activity.getWindowManager();
            View v = r.activity.mDecor;
            if (v != null) {
                if (r.activity.mVisibleFromServer) {
                    this.mNumVisibleActivities--;
                }
                IBinder wtoken = v.getWindowToken();
                if (r.activity.mWindowAdded) {
                    if (r.mPreserveWindow) {
                        r.mPendingRemoveWindow = r.window;
                        r.mPendingRemoveWindowManager = wm;
                        r.window.clearContentView();
                    } else {
                        wm.removeViewImmediate(v);
                    }
                }
                if (wtoken != null && r.mPendingRemoveWindow == null) {
                    WindowManagerGlobal.getInstance().closeAll(wtoken, r.activity.getClass().getName(), "Activity");
                } else if (r.mPendingRemoveWindow != null) {
                    WindowManagerGlobal.getInstance().closeAllExceptView(token, v, r.activity.getClass().getName(), "Activity");
                }
                r.activity.mDecor = null;
            }
            if (r.mPendingRemoveWindow == null) {
                WindowManagerGlobal.getInstance().closeAll(token, r.activity.getClass().getName(), "Activity");
            }
            Context c = r.activity.getBaseContext();
            if (c instanceof ContextImpl) {
                ((ContextImpl) c).scheduleFinalCleanup(r.activity.getClass().getName(), "Activity");
            }
        }
        if (finishing) {
            try {
                ActivityTaskManager.getService().activityDestroyed(token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        this.mSomeActivitiesChanged = true;
    }

    @Override // android.app.ClientTransactionHandler
    public ActivityClientRecord prepareRelaunchActivity(IBinder token, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, MergedConfiguration config, boolean preserveWindow) {
        ActivityClientRecord target = null;
        boolean scheduleRelaunch = false;
        if (HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            config.setGlobalConfiguration(updateConfig(config.getGlobalConfiguration()));
        }
        synchronized (this.mResourcesManager) {
            int i = 0;
            while (true) {
                if (i >= this.mRelaunchingActivities.size()) {
                    break;
                }
                ActivityClientRecord r = this.mRelaunchingActivities.get(i);
                if (DEBUG_ORDER) {
                    Slog.d(TAG, "requestRelaunchActivity: " + this + ", trying: " + r);
                }
                if (r.token == token) {
                    target = r;
                    if (pendingResults != null) {
                        if (r.pendingResults != null) {
                            r.pendingResults.addAll(pendingResults);
                        } else {
                            r.pendingResults = pendingResults;
                        }
                    }
                    if (pendingNewIntents != null) {
                        if (r.pendingIntents != null) {
                            r.pendingIntents.addAll(pendingNewIntents);
                        } else {
                            r.pendingIntents = pendingNewIntents;
                        }
                    }
                } else {
                    i++;
                }
            }
            if (target == null) {
                if (DEBUG_ORDER) {
                    Slog.d(TAG, "requestRelaunchActivity: target is null");
                }
                target = new ActivityClientRecord();
                target.token = token;
                target.pendingResults = pendingResults;
                target.pendingIntents = pendingNewIntents;
                target.mPreserveWindow = preserveWindow;
                this.mRelaunchingActivities.add(target);
                scheduleRelaunch = true;
            }
            if (target.createdConfig == null || target.overrideConfig == null) {
                target.createdConfig = config.getGlobalConfiguration();
                target.overrideConfig = config.getOverrideConfiguration();
            } else {
                if (target.createdConfig.isOtherSeqNewer(config.getGlobalConfiguration())) {
                    target.createdConfig = config.getGlobalConfiguration();
                }
                if (target.overrideConfig.isOtherSeqNewer(config.getOverrideConfiguration())) {
                    target.overrideConfig = config.getOverrideConfiguration();
                }
            }
            target.pendingConfigChanges |= configChanges;
        }
        if (scheduleRelaunch) {
            return target;
        }
        return null;
    }

    @Override // android.app.ClientTransactionHandler
    public void handleRelaunchActivity(ActivityClientRecord tmp, PendingTransactionActions pendingActions) {
        RemoteException e;
        ActivityClientRecord activityClientRecord;
        int configChanges;
        ActivityClientRecord tmp2;
        Configuration changedConfig;
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        Configuration changedConfig2 = null;
        synchronized (this.mResourcesManager) {
            try {
                int N = this.mRelaunchingActivities.size();
                activityClientRecord = tmp;
                try {
                    IBinder token = activityClientRecord.token;
                    int i = 0;
                    configChanges = 0;
                    tmp2 = null;
                    while (i < N) {
                        try {
                            ActivityClientRecord r = this.mRelaunchingActivities.get(i);
                            if (r.token == token) {
                                try {
                                    int configChanges2 = r.pendingConfigChanges | configChanges;
                                    try {
                                        this.mRelaunchingActivities.remove(i);
                                        i--;
                                        N--;
                                        tmp2 = r;
                                        configChanges = configChanges2;
                                    } catch (Throwable th) {
                                        e = th;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th2) {
                                                e = th2;
                                            }
                                        }
                                        throw e;
                                    }
                                } catch (Throwable th3) {
                                    e = th3;
                                    while (true) {
                                        break;
                                    }
                                    throw e;
                                }
                            }
                            i++;
                        } catch (Throwable th4) {
                            e = th4;
                            while (true) {
                                break;
                            }
                            throw e;
                        }
                    }
                    if (tmp2 == null) {
                        if (DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Abort, activity not relaunching!");
                        }
                        return;
                    } else if (this.mPendingConfiguration != null) {
                        changedConfig2 = this.mPendingConfiguration;
                        this.mPendingConfiguration = null;
                    }
                } catch (Throwable th5) {
                    e = th5;
                    while (true) {
                        break;
                    }
                    throw e;
                }
            } catch (Throwable th6) {
                e = th6;
                activityClientRecord = tmp;
                while (true) {
                    break;
                }
                throw e;
            }
        }
        if (tmp2.createdConfig == null || ((this.mConfiguration != null && (!tmp2.createdConfig.isOtherSeqNewer(this.mConfiguration) || this.mConfiguration.diff(tmp2.createdConfig) == 0)) || (changedConfig2 != null && !tmp2.createdConfig.isOtherSeqNewer(changedConfig2)))) {
            changedConfig = changedConfig2;
        } else {
            changedConfig = tmp2.createdConfig;
        }
        if (changedConfig != null) {
            this.mCurDefaultDisplayDpi = changedConfig.densityDpi;
            updateDefaultDensity();
            handleConfigurationChanged(changedConfig, null);
        }
        ActivityClientRecord r2 = this.mActivities.get(tmp2.token);
        if (localLOGV) {
            Slog.v(TAG, "Handling relaunch of " + r2 + ": changedConfig=" + changedConfig + " with configChanges=0x" + Integer.toHexString(configChanges));
        }
        if (r2 != null) {
            HwActivityTaskManager.adjustOverlayWindowWhileActivityConfigChanged(tmp2.overrideConfig, r2.overrideConfig);
            r2.activity.mConfigChangeFlags |= configChanges;
            r2.mPreserveWindow = tmp2.mPreserveWindow;
            r2.activity.mChangingConfigurations = true;
            try {
                if (r2.mPreserveWindow) {
                    WindowManagerGlobal.getWindowSession().prepareToReplaceWindows(r2.token, true);
                }
                handleRelaunchActivityInner(r2, configChanges, tmp2.pendingResults, tmp2.pendingIntents, pendingActions, tmp2.startsNotResumed, tmp2.overrideConfig, "handleRelaunchActivity");
                if (pendingActions != null) {
                    pendingActions.setReportRelaunchToWindowManager(true);
                }
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleRelaunchActivity(IBinder token) {
        scheduleRelaunchActivity(token, false);
    }

    /* access modifiers changed from: package-private */
    public void scheduleRelaunchActivity(IBinder token, boolean fromThemeChange) {
        this.mH.removeMessages(160, token);
        sendMessage(160, token, fromThemeChange ? 1 : 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRelaunchActivityLocally(IBinder token, boolean fromThemeChange) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "Activity to relaunch no longer exists");
            return;
        }
        int prevState = r.getLifecycleState();
        if (fromThemeChange && (prevState == 5 || "com.huawei.android.thememanager".equals(r.activityInfo.packageName) || "com.huawei.android.launcher".equals(r.activityInfo.packageName))) {
            Log.w(TAG, "Activity can not relaunch in state ON_STOP and fromThemeChange");
        } else if (prevState < 3 || prevState > 5) {
            Log.w(TAG, "Activity state must be in [ON_RESUME..ON_STOP] in order to be relaunched,current state is " + prevState);
        } else {
            ActivityRelaunchItem activityRelaunchItem = ActivityRelaunchItem.obtain(null, null, 0, new MergedConfiguration(r.createdConfig != null ? r.createdConfig : this.mConfiguration, r.overrideConfig), r.mPreserveWindow);
            ActivityLifecycleItem lifecycleRequest = TransactionExecutorHelper.getLifecycleRequestForCurrentState(r);
            ClientTransaction transaction = ClientTransaction.obtain(this.mAppThread, r.token);
            transaction.addCallback(activityRelaunchItem);
            transaction.setLifecycleStateRequest(lifecycleRequest);
            executeTransaction(transaction);
        }
    }

    private void handleRelaunchActivityInner(ActivityClientRecord r, int configChanges, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingIntents, PendingTransactionActions pendingActions, boolean startsNotResumed, Configuration overrideConfig, String reason) {
        Intent customIntent = r.activity.mIntent;
        if (!r.paused) {
            performPauseActivity(r, false, reason, (PendingTransactionActions) null);
        }
        if (!r.stopped) {
            callActivityOnStop(r, true, reason);
        }
        handleDestroyActivity(r.token, false, configChanges, true, reason);
        r.activity = null;
        r.window = null;
        r.hideForNow = false;
        r.nextIdle = null;
        if (pendingResults != null) {
            if (r.pendingResults == null) {
                r.pendingResults = pendingResults;
            } else {
                r.pendingResults.addAll(pendingResults);
            }
        }
        if (pendingIntents != null) {
            if (r.pendingIntents == null) {
                r.pendingIntents = pendingIntents;
            } else {
                r.pendingIntents.addAll(pendingIntents);
            }
        }
        r.startsNotResumed = startsNotResumed;
        r.overrideConfig = overrideConfig;
        handleLaunchActivity(r, pendingActions, customIntent);
    }

    @Override // android.app.ClientTransactionHandler
    public void reportRelaunch(IBinder token, PendingTransactionActions pendingActions) {
        try {
            ActivityTaskManager.getService().activityRelaunched(token);
            ActivityClientRecord r = this.mActivities.get(token);
            if (pendingActions.shouldReportRelaunchToWindowManager() && r != null && r.window != null) {
                r.window.reportActivityRelaunched();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void callActivityOnSaveInstanceState(ActivityClientRecord r) {
        r.state = new Bundle();
        r.state.setAllowFds(false);
        if (r.isPersistable()) {
            r.persistentState = new PersistableBundle();
            this.mInstrumentation.callActivityOnSaveInstanceState(r.activity, r.state, r.persistentState);
            return;
        }
        this.mInstrumentation.callActivityOnSaveInstanceState(r.activity, r.state);
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ComponentCallbacks2> collectComponentCallbacks(boolean allActivities, Configuration newConfig) {
        ArrayList<ComponentCallbacks2> callbacks = new ArrayList<>();
        synchronized (this.mResourcesManager) {
            int NAPP = this.mAllApplications.size();
            for (int i = 0; i < NAPP; i++) {
                callbacks.add(this.mAllApplications.get(i));
            }
            int NACT = this.mActivities.size();
            for (int i2 = 0; i2 < NACT; i2++) {
                ActivityClientRecord ar = this.mActivities.valueAt(i2);
                Activity a = ar.activity;
                if (a != null) {
                    Configuration thisConfig = applyConfigCompatMainThread(this.mCurDefaultDisplayDpi, newConfig, ar.packageInfo.getCompatibilityInfo());
                    if (!ar.activity.mFinished && (allActivities || !ar.paused)) {
                        callbacks.add(a);
                    } else if (thisConfig != null) {
                        if (DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Setting activity " + ar.activityInfo.name + " newConfig=" + thisConfig);
                        }
                        ar.newConfig = thisConfig;
                    }
                }
            }
            int NSVC = this.mServices.size();
            for (int i3 = 0; i3 < NSVC; i3++) {
                callbacks.add(this.mServices.valueAt(i3));
            }
        }
        synchronized (this.mProviderMap) {
            int NPRV = this.mLocalProviders.size();
            for (int i4 = 0; i4 < NPRV; i4++) {
                callbacks.add(this.mLocalProviders.valueAt(i4).mLocalProvider);
            }
        }
        return callbacks;
    }

    private void performConfigurationChangedForActivity(ActivityClientRecord r, Configuration newBaseConfig) {
        performConfigurationChangedForActivity(r, newBaseConfig, r.activity.getDisplayId(), false);
    }

    private Configuration performConfigurationChangedForActivity(ActivityClientRecord r, Configuration newBaseConfig, int displayId, boolean movedToDifferentDisplay) {
        r.tmpConfig.setTo(newBaseConfig);
        if (r.overrideConfig != null) {
            r.tmpConfig.updateFrom(r.overrideConfig);
        }
        Configuration reportedConfig = performActivityConfigurationChanged(r.activity, r.tmpConfig, r.overrideConfig, displayId, movedToDifferentDisplay);
        freeTextLayoutCachesIfNeeded(r.activity.mCurrentConfig.diff(r.tmpConfig));
        return reportedConfig;
    }

    private static Configuration createNewConfigAndUpdateIfNotNull(Configuration base, Configuration override) {
        if (override == null) {
            return base;
        }
        Configuration newConfig = new Configuration(base);
        newConfig.updateFrom(override);
        return newConfig;
    }

    private void performConfigurationChanged(ComponentCallbacks2 cb, Configuration newConfig) {
        Configuration contextThemeWrapperOverrideConfig = null;
        if (cb instanceof ContextThemeWrapper) {
            contextThemeWrapperOverrideConfig = ((ContextThemeWrapper) cb).getOverrideConfiguration();
        }
        cb.onConfigurationChanged(createNewConfigAndUpdateIfNotNull(newConfig, contextThemeWrapperOverrideConfig));
    }

    private Configuration performActivityConfigurationChanged(Activity activity, Configuration newConfig, Configuration amOverrideConfig, int displayId, boolean movedToDifferentDisplay) {
        if (activity != null) {
            IBinder activityToken = activity.getActivityToken();
            if (activityToken != null) {
                boolean shouldChangeConfig = false;
                int tmpdiff = 0;
                if (activity.mCurrentConfig == null) {
                    shouldChangeConfig = true;
                } else {
                    int diff = activity.mCurrentConfig.diffPublicOnly(newConfig);
                    tmpdiff = diff;
                    if ((diff != 0 || !this.mResourcesManager.isSameResourcesOverrideConfig(activityToken, amOverrideConfig)) && (!this.mUpdatingSystemConfig || ((~activity.mActivityInfo.getRealConfigChanged()) & diff) == 0)) {
                        shouldChangeConfig = true;
                    }
                }
                if (!shouldChangeConfig && !movedToDifferentDisplay) {
                    return null;
                }
                Configuration contextThemeWrapperOverrideConfig = activity.getOverrideConfiguration();
                this.mResourcesManager.updateResourcesForActivity(activityToken, createNewConfigAndUpdateIfNotNull(amOverrideConfig, contextThemeWrapperOverrideConfig), displayId, movedToDifferentDisplay);
                HwActivityTaskManager.adjustOverlayWindowWhileActivityConfigChanged(newConfig, activity.mCurrentConfig);
                activity.mConfigChangeFlags = 0;
                activity.mCurrentConfig = new Configuration(newConfig);
                Configuration configToReport = createNewConfigAndUpdateIfNotNull(newConfig, contextThemeWrapperOverrideConfig);
                if (movedToDifferentDisplay) {
                    activity.dispatchMovedToDisplay(displayId, configToReport);
                }
                if (shouldChangeConfig) {
                    activity.mCalled = false;
                    activity.onConfigurationChanged(configToReport);
                    if ((tmpdiff & 128) != 0) {
                        updateNavigationBarColor(activity);
                    }
                    if (!activity.mCalled) {
                        throw new SuperNotCalledException("Activity " + activity.getLocalClassName() + " did not call through to super.onConfigurationChanged()");
                    }
                }
                return configToReport;
            }
            throw new IllegalArgumentException("Activity token not set. Is the activity attached?");
        }
        throw new IllegalArgumentException("No activity provided.");
    }

    public final void applyConfigurationToResources(Configuration config) {
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyConfigurationToResourcesLocked(config, null);
        }
    }

    /* access modifiers changed from: package-private */
    public final Configuration applyCompatConfiguration(int displayDensity) {
        Configuration config = this.mConfiguration;
        if (this.mCompatConfiguration == null) {
            this.mCompatConfiguration = new Configuration();
        }
        this.mCompatConfiguration.setTo(this.mConfiguration);
        if (this.mResourcesManager.applyCompatConfigurationLocked(displayDensity, this.mCompatConfiguration)) {
            return this.mCompatConfiguration;
        }
        return config;
    }

    /* JADX INFO: finally extract failed */
    @Override // android.app.ClientTransactionHandler
    public void handleConfigurationChanged(Configuration config) {
        Trace.traceBegin(64, "configChanged");
        this.mCurDefaultDisplayDpi = config.densityDpi;
        this.mUpdatingSystemConfig = true;
        try {
            handleConfigurationChanged(config, null);
            this.mUpdatingSystemConfig = false;
            Trace.traceEnd(64);
        } catch (Throwable th) {
            this.mUpdatingSystemConfig = false;
            throw th;
        }
    }

    private void handleConfigurationChanged(Configuration config, CompatibilityInfo compat) {
        boolean equivalent;
        int configDiff;
        Configuration config2;
        Resources.Theme systemTheme = getSystemContext().getTheme();
        Resources.Theme systemUiTheme = getSystemUiContext().getTheme();
        synchronized (this.mResourcesManager) {
            if (this.mPendingConfiguration != null) {
                if (!this.mPendingConfiguration.isOtherSeqNewer(config)) {
                    config = this.mPendingConfiguration;
                    this.mCurDefaultDisplayDpi = config.densityDpi;
                    updateDefaultDensity();
                }
                this.mPendingConfiguration = null;
            }
            if (config == null) {
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Config is null for app:" + currentPackageName());
                }
                return;
            }
            adjustGlobalConfigIfNeeded(config);
            equivalent = this.mConfiguration != null && this.mConfiguration.diffPublicOnly(config) == 0;
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Handle configuration changed: " + config);
            }
            this.mResourcesManager.applyConfigurationToResourcesLocked(config, compat);
            updateLocaleListFromAppContext(this.mInitialApplication.getApplicationContext(), this.mResourcesManager.getConfiguration().getLocales());
            if (this.mConfiguration == null) {
                this.mConfiguration = new Configuration();
            }
            if (this.mConfiguration.isOtherSeqNewer(config) || compat != null) {
                configDiff = this.mConfiguration.updateFrom(config);
                config2 = applyCompatConfiguration(this.mCurDefaultDisplayDpi);
                if ((systemTheme.getChangingConfigurations() & configDiff) != 0) {
                    systemTheme.rebase();
                }
                if ((systemUiTheme.getChangingConfigurations() & configDiff) != 0) {
                    systemUiTheme.rebase();
                }
            } else {
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Skipping new config:" + this.mConfiguration + ", config:" + config + " for app:" + currentPackageName());
                }
                return;
            }
        }
        ArrayList<ComponentCallbacks2> callbacks = collectComponentCallbacks(false, config2);
        freeTextLayoutCachesIfNeeded(configDiff);
        if (HwThemeManager.setThemeFontOnConfigChg(config2)) {
            SystemFonts.loadHwSystemFonts();
        }
        if (callbacks != null) {
            int N = callbacks.size();
            for (int i = 0; i < N; i++) {
                ComponentCallbacks2 cb = callbacks.get(i);
                if (cb instanceof Activity) {
                    performConfigurationChangedForActivity(this.mActivities.get(((Activity) cb).getActivityToken()), config2);
                } else if (!equivalent) {
                    performConfigurationChanged(cb, config2);
                } else if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Skipping handle non-activity callbacks for app:" + currentPackageName());
                }
            }
        } else if (DEBUG_CONFIGURATION) {
            Slog.v(TAG, "There are no configuration change callbacks for app:" + currentPackageName());
        }
    }

    public void handleSystemApplicationInfoChanged(ApplicationInfo ai) {
        Preconditions.checkState(this.mSystemThread, "Must only be called in the system process");
        handleApplicationInfoChanged(ai, false);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void handleApplicationInfoChanged(ApplicationInfo ai, boolean fromThemeChange) {
        LoadedApk apk;
        LoadedApk resApk;
        synchronized (this.mResourcesManager) {
            WeakReference<LoadedApk> ref = this.mPackages.get(ai.packageName);
            apk = ref != null ? ref.get() : null;
            WeakReference<LoadedApk> ref2 = this.mResourcePackages.get(ai.packageName);
            resApk = ref2 != null ? ref2.get() : null;
        }
        String[] oldResDirs = new String[2];
        int i = 0;
        if (apk != null) {
            oldResDirs[0] = apk.getResDir();
            ArrayList<String> oldPaths = new ArrayList<>();
            LoadedApk.makePaths(this, apk.getApplicationInfo(), oldPaths);
            apk.updateApplicationInfo(ai, oldPaths);
        }
        if (resApk != null) {
            oldResDirs[1] = resApk.getResDir();
            ArrayList<String> oldPaths2 = new ArrayList<>();
            LoadedApk.makePaths(this, resApk.getApplicationInfo(), oldPaths2);
            resApk.updateApplicationInfo(ai, oldPaths2);
        }
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyNewResourceDirsLocked(ai, oldResDirs);
        }
        ApplicationPackageManager.configurationChanged();
        Configuration newConfig = new Configuration();
        Configuration configuration = this.mConfiguration;
        if (configuration != null) {
            i = configuration.assetsSeq;
        }
        newConfig.assetsSeq = i + 1;
        handleConfigurationChanged(newConfig, null);
        relaunchAllActivities(true, fromThemeChange);
    }

    static void freeTextLayoutCachesIfNeeded(int configDiff) {
        if (configDiff != 0) {
            if ((configDiff & 4) != 0) {
                Canvas.freeTextLayoutCaches();
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Cleared TextLayout Caches");
                }
            }
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void updatePendingActivityConfiguration(IBinder activityToken, Configuration overrideConfig) {
        ActivityClientRecord r;
        synchronized (this.mResourcesManager) {
            r = this.mActivities.get(activityToken);
        }
        if (r != null) {
            synchronized (r) {
                r.mPendingOverrideConfig = overrideConfig;
            }
        } else if (DEBUG_CONFIGURATION) {
            Slog.w(TAG, "Not found target activity to update its pending config.");
        }
    }

    @Override // android.app.ClientTransactionHandler
    public void handleActivityConfigurationChanged(IBinder activityToken, Configuration overrideConfig, int displayId) {
        ViewRootImpl viewRoot;
        ActivityClientRecord r = this.mActivities.get(activityToken);
        if (r != null && r.activity != null) {
            boolean movedToDifferentDisplay = (displayId == -1 || displayId == r.activity.getDisplayId()) ? false : true;
            synchronized (r) {
                if (r.mPendingOverrideConfig != null && !r.mPendingOverrideConfig.isOtherSeqNewer(overrideConfig)) {
                    overrideConfig = r.mPendingOverrideConfig;
                }
                viewRoot = null;
                r.mPendingOverrideConfig = null;
            }
            if (r.overrideConfig == null || r.overrideConfig.isOtherSeqNewer(overrideConfig) || movedToDifferentDisplay) {
                boolean isNeedForceUpdate = (r.overrideConfig == null || overrideConfig == null || (!r.overrideConfig.windowConfiguration.inHwMultiStackWindowingMode() && !overrideConfig.windowConfiguration.inHwMultiStackWindowingMode() && r.overrideConfig.windowConfiguration.getWindowingMode() != 5 && overrideConfig.windowConfiguration.getWindowingMode() != 5) || r.overrideConfig.windowConfiguration.getWindowingMode() == overrideConfig.windowConfiguration.getWindowingMode()) ? false : true;
                r.overrideConfig = overrideConfig;
                if (r.getLifecycleState() == 3) {
                    this.mResourcesManager.applyConfigurationToAppResourcesLocked(movedToDifferentDisplay ? displayId : r.activity.getDisplayId(), r.overrideConfig);
                }
                if (r.activity.mDecor != null) {
                    viewRoot = r.activity.mDecor.getViewRootImpl();
                }
                if (movedToDifferentDisplay) {
                    if (DEBUG_CONFIGURATION) {
                        Slog.v(TAG, "Handle activity moved to display, activity:" + r.activityInfo.name + ", displayId=" + displayId + ", config=" + overrideConfig);
                    }
                    Configuration reportedConfig = performConfigurationChangedForActivity(r, this.mCompatConfiguration, displayId, true);
                    if (viewRoot != null) {
                        viewRoot.onMovedToDisplay(displayId, reportedConfig);
                    }
                } else {
                    if (DEBUG_CONFIGURATION) {
                        Slog.v(TAG, "Handle activity config changed: " + r.activityInfo.name + ", config=" + overrideConfig);
                    }
                    performConfigurationChangedForActivity(r, this.mCompatConfiguration);
                }
                if (viewRoot != null) {
                    viewRoot.setForceNextConfigUpdate(isNeedForceUpdate);
                    viewRoot.updateConfiguration(displayId);
                    viewRoot.setForceNextConfigUpdate(false);
                }
                this.mSomeActivitiesChanged = true;
            } else if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Activity already handled newer configuration so drop this transaction. overrideConfig=" + overrideConfig + " r.overrideConfig=" + r.overrideConfig);
            }
        } else if (DEBUG_CONFIGURATION) {
            Slog.w(TAG, "Not found target activity to report to: " + r);
        }
    }

    /* access modifiers changed from: package-private */
    public final void handleProfilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
        if (start) {
            try {
                this.mProfiler.setProfiler(profilerInfo);
                this.mProfiler.startProfiling();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Profiling failed on path " + profilerInfo.profileFile + " -- can the process access this path?");
            } catch (Throwable th) {
                profilerInfo.closeFd();
                throw th;
            }
            profilerInfo.closeFd();
            return;
        }
        this.mProfiler.stopProfiling();
    }

    public void stopProfiling() {
        Profiler profiler = this.mProfiler;
        if (profiler != null) {
            profiler.stopProfiling();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
        if (r1 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        throw r3;
     */
    static void handleDumpHeap(DumpHeapData dhd) {
        if (dhd.runGc) {
            System.gc();
            System.runFinalization();
            System.gc();
        }
        try {
            ParcelFileDescriptor fd = dhd.fd;
            if (dhd.managed) {
                Debug.dumpHprofData(dhd.path, fd.getFileDescriptor());
            } else if (dhd.mallocInfo) {
                Debug.dumpNativeMallocInfo(fd.getFileDescriptor());
            } else {
                Debug.dumpNativeHeap(fd.getFileDescriptor());
            }
            if (fd != null) {
                fd.close();
            }
        } catch (IOException e) {
            if (dhd.managed) {
                Slog.w(TAG, "Managed heap dump failed on path " + dhd.path + " -- can the process access this path?", e);
            } else {
                Slog.w(TAG, "Failed to dump heap", e);
            }
        } catch (RuntimeException e2) {
            Slog.wtf(TAG, "Heap dumper threw a runtime exception", e2);
        }
        try {
            ActivityManager.getService().dumpHeapFinished(dhd.path);
            if (dhd.finishCallback != null) {
                dhd.finishCallback.sendResult(null);
            }
        } catch (RemoteException e3) {
            throw e3.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public final void handleDispatchPackageBroadcast(int cmd, String[] packages) {
        Throwable th;
        boolean hasPkgInfo;
        boolean hasPkgInfo2;
        boolean hasPkgInfo3 = false;
        boolean killApp = false;
        int i = 1;
        if (cmd == 0 || cmd == 2) {
            if (cmd == 0) {
                killApp = true;
            }
            if (packages != null) {
                synchronized (this.mResourcesManager) {
                    for (int i2 = packages.length - 1; i2 >= 0; i2--) {
                        if (!hasPkgInfo3) {
                            WeakReference<LoadedApk> ref = this.mPackages.get(packages[i2]);
                            if (ref == null || ref.get() == null) {
                                WeakReference<LoadedApk> ref2 = this.mResourcePackages.get(packages[i2]);
                                if (!(ref2 == null || ref2.get() == null)) {
                                    hasPkgInfo3 = true;
                                }
                            } else {
                                hasPkgInfo3 = true;
                            }
                        }
                        if (killApp) {
                            this.mPackages.remove(packages[i2]);
                            this.mResourcePackages.remove(packages[i2]);
                        }
                    }
                }
            }
        } else if (cmd == 3 && packages != null) {
            List<String> packagesHandled = new ArrayList<>();
            synchronized (this.mResourcesManager) {
                try {
                    hasPkgInfo = false;
                    int i3 = packages.length - 1;
                    while (i3 >= 0) {
                        try {
                            String packageName = packages[i3];
                            WeakReference<LoadedApk> ref3 = this.mPackages.get(packageName);
                            LoadedApk loadedApk = null;
                            LoadedApk pkgInfo = ref3 != null ? ref3.get() : null;
                            if (pkgInfo != null) {
                                hasPkgInfo2 = true;
                            } else {
                                WeakReference<LoadedApk> ref4 = this.mResourcePackages.get(packageName);
                                if (ref4 != null) {
                                    loadedApk = ref4.get();
                                }
                                pkgInfo = loadedApk;
                                hasPkgInfo2 = pkgInfo != null ? true : hasPkgInfo;
                            }
                            if (pkgInfo != null) {
                                try {
                                    packagesHandled.add(packageName);
                                    try {
                                        ApplicationInfo aInfo = sPackageManager.getApplicationInfo(packageName, 1024, UserHandle.myUserId());
                                        if (this.mActivities.size() > 0) {
                                            for (ActivityClientRecord ar : this.mActivities.values()) {
                                                if (ar.activityInfo.applicationInfo.packageName.equals(packageName)) {
                                                    ar.activityInfo.applicationInfo = aInfo;
                                                    ar.packageInfo = pkgInfo;
                                                }
                                            }
                                        }
                                        if (aInfo == null) {
                                            Slog.w(TAG, "ApplicationInfo is null for " + packageName);
                                        } else {
                                            String[] oldResDirs = new String[i];
                                            oldResDirs[0] = pkgInfo.getResDir();
                                            ArrayList<String> oldPaths = new ArrayList<>();
                                            LoadedApk.makePaths(this, pkgInfo.getApplicationInfo(), oldPaths);
                                            pkgInfo.updateApplicationInfo(aInfo, oldPaths);
                                            synchronized (this.mResourcesManager) {
                                                this.mResourcesManager.applyNewResourceDirsLocked(aInfo, oldResDirs);
                                            }
                                        }
                                    } catch (RemoteException e) {
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                            i3--;
                            hasPkgInfo = hasPkgInfo2;
                            i = 1;
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
            try {
                getPackageManager().notifyPackagesReplacedReceived((String[]) packagesHandled.toArray(new String[0]));
            } catch (RemoteException e2) {
            }
            hasPkgInfo3 = hasPkgInfo;
        }
        ApplicationPackageManager.handlePackageBroadcast(cmd, packages, hasPkgInfo3);
    }

    /* access modifiers changed from: package-private */
    public final void handleLowMemory() {
        ArrayList<ComponentCallbacks2> callbacks = collectComponentCallbacks(true, null);
        int N = callbacks.size();
        for (int i = 0; i < N; i++) {
            callbacks.get(i).onLowMemory();
        }
        if (Process.myUid() != 1000) {
            EventLog.writeEvent((int) SQLITE_MEM_RELEASED_EVENT_LOG_TAG, SQLiteDatabase.releaseMemory());
        }
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
        BinderInternal.forceGc("mem");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleTrimMemory(int level, boolean fromIAware) {
        Trace.traceBegin(64, "trimMemory");
        if (!fromIAware) {
            ArrayList<ComponentCallbacks2> callbacks = collectComponentCallbacks(true, null);
            int N = callbacks.size();
            for (int i = 0; i < N; i++) {
                callbacks.get(i).onTrimMemory(level);
            }
        }
        WindowManagerGlobal.getInstance().trimMemory(level);
        Trace.traceEnd(64);
        if (SystemProperties.getInt("debug.am.run_gc_trim_level", Integer.MAX_VALUE) <= level) {
            unscheduleGcIdler();
            doGcIfNeeded("tm");
        }
        if (SystemProperties.getInt("debug.am.run_mallopt_trim_level", Integer.MAX_VALUE) <= level) {
            unschedulePurgeIdler();
            purgePendingResources();
        }
    }

    private void setupGraphicsSupport(Context context) {
        Trace.traceBegin(64, "setupGraphicsSupport");
        if (!"android".equals(context.getPackageName())) {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null) {
                System.setProperty("java.io.tmpdir", cacheDir.getAbsolutePath());
            } else {
                Log.v(TAG, "Unable to initialize \"java.io.tmpdir\" property due to missing cache directory");
            }
            File codeCacheDir = context.createDeviceProtectedStorageContext().getCodeCacheDir();
            if (codeCacheDir != null) {
                try {
                    if (getPackageManager().getPackagesForUid(Process.myUid()) != null) {
                        HardwareRenderer.setupDiskCache(codeCacheDir);
                        RenderScriptCacheDir.setupDiskCache(codeCacheDir);
                    }
                } catch (RemoteException e) {
                    Trace.traceEnd(64);
                    throw e.rethrowFromSystemServer();
                }
            } else {
                Log.w(TAG, "Unable to use shader/script cache: missing code-cache directory");
            }
        }
        GraphicsEnvironment.getInstance().setup(context, this.mCoreSettings);
        Trace.traceEnd(64);
    }

    private void updateDefaultDensity() {
        int densityDpi = this.mCurDefaultDisplayDpi;
        AppBindData appBindData = this.mBoundApplication;
        if (appBindData != null) {
            CompatibilityInfo ci = appBindData.compatInfo;
            if (!ci.supportsScreen() && densityDpi == DisplayMetrics.DENSITY_DEVICE) {
                Bitmap.setDefaultDensity((int) ((((float) densityDpi) * ci.getSdrLowResolutionRatio()) + 0.5f));
                return;
            }
        }
        if (!this.mDensityCompatMode && densityDpi != 0 && densityDpi != DisplayMetrics.DENSITY_DEVICE) {
            DisplayMetrics.DENSITY_DEVICE = densityDpi;
            Bitmap.setDefaultDensity(densityDpi);
        }
    }

    private String getInstrumentationLibrary(ApplicationInfo appInfo, InstrumentationInfo insInfo) {
        if (!(appInfo.primaryCpuAbi == null || appInfo.secondaryCpuAbi == null || !appInfo.secondaryCpuAbi.equals(insInfo.secondaryCpuAbi))) {
            String secondaryIsa = VMRuntime.getInstructionSet(appInfo.secondaryCpuAbi);
            String secondaryDexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + secondaryIsa);
            if (VMRuntime.getRuntime().vmInstructionSet().equals(secondaryDexCodeIsa.isEmpty() ? secondaryIsa : secondaryDexCodeIsa)) {
                return insInfo.secondaryNativeLibraryDir;
            }
        }
        return insInfo.nativeLibraryDir;
    }

    private void updateLocaleListFromAppContext(Context context, LocaleList newLocaleList) {
        Locale bestLocale = context.getResources().getConfiguration().getLocales().get(0);
        int newLocaleListSize = newLocaleList.size();
        for (int i = 0; i < newLocaleListSize; i++) {
            if (bestLocale.equals(newLocaleList.get(i))) {
                LocaleList.setDefault(newLocaleList, i);
                return;
            }
        }
        LocaleList.setDefault(new LocaleList(bestLocale, newLocaleList));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0615, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x03a9  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x03b1  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x03e3  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0495  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x049d  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x04d6 A[Catch:{ all -> 0x05fb }] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00de  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x051f A[Catch:{ Exception -> 0x0525 }] */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x054a A[SYNTHETIC, Splitter:B:191:0x054a] */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x0582  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00ee  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x010e A[SYNTHETIC] */
    @UnsupportedAppUsage
    private void handleBindApplication(AppBindData data) {
        String agent;
        Boolean is24Hr;
        InstrumentationInfo ii;
        ContextImpl appContext;
        boolean z;
        Application app;
        int preloadedFontsResource;
        ApplicationInfo instrApp;
        ApplicationInfo instrApp2;
        IBlockMonitor blockMonitor;
        sendPreloadMessage(11, data.appInfo, 0);
        long hiviewTime = SystemClock.uptimeMillis();
        sendPreloadMessage(7, null, 0);
        VMRuntime.registerSensitiveThread();
        String property = SystemProperties.get("debug.allocTracker.stackDepth");
        if (property.length() != 0) {
            VMDebug.setAllocTrackerStackDepth(Integer.parseInt(property));
        }
        if (data.trackAllocation) {
            DdmVmInternal.enableRecentAllocations(true);
        }
        sendPreloadMessage(5, currentPackageName(), 0);
        sendPreloadMessage(9, data.appInfo, 0);
        Process.setStartTimes(SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        this.mBoundApplication = data;
        this.mConfiguration = new Configuration(data.config);
        this.mCompatConfiguration = new Configuration(data.config);
        this.mProfiler = new Profiler();
        if (data.initProfilerInfo != null) {
            this.mProfiler.profileFile = data.initProfilerInfo.profileFile;
            this.mProfiler.profileFd = data.initProfilerInfo.profileFd;
            this.mProfiler.samplingInterval = data.initProfilerInfo.samplingInterval;
            this.mProfiler.autoStopProfiler = data.initProfilerInfo.autoStopProfiler;
            this.mProfiler.streamingOutput = data.initProfilerInfo.streamingOutput;
            if (data.initProfilerInfo.attachAgentDuringBind) {
                agent = data.initProfilerInfo.agent;
                sendPreloadMessage(5, currentPackageName(), 0);
                Process.setArgV0(data.processName);
                DdmHandleAppName.setAppName(data.processName, UserHandle.myUserId());
                VMRuntime.setProcessPackageName(data.appInfo.packageName);
                ServiceManager.handleApplicationBinded(data.processName);
                VMRuntime.setProcessDataDirectory(data.appInfo.dataDir);
                if (this.mProfiler.profileFd != null) {
                    this.mProfiler.startProfiling();
                }
                if (data.appInfo.targetSdkVersion <= 12) {
                    AsyncTask.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                UtilConfig.setThrowExceptionForUpperArrayOutOfBounds(data.appInfo.targetSdkVersion < 29);
                Message.updateCheckRecycle(data.appInfo.targetSdkVersion);
                ImageDecoder.sApiLevel = data.appInfo.targetSdkVersion;
                TimeZone.setDefault(null);
                LocaleList.setDefault(data.config.getLocales());
                synchronized (this.mResourcesManager) {
                    this.mResourcesManager.applyConfigurationToResourcesLocked(data.config, data.compatInfo);
                    this.mCurDefaultDisplayDpi = data.config.densityDpi;
                    applyCompatConfiguration(this.mCurDefaultDisplayDpi);
                }
                data.info = getPackageInfoNoCheck(data.appInfo, data.compatInfo);
                if (agent != null) {
                    handleAttachAgent(agent, data.info);
                }
                if ((data.appInfo.flags & 8192) == 0) {
                    this.mDensityCompatMode = true;
                    Bitmap.setDefaultDensity(160);
                }
                updateDefaultDensity();
                String use24HourSetting = this.mCoreSettings.getString(Settings.System.TIME_12_24);
                if (use24HourSetting != null) {
                    is24Hr = WifiScanLog.EVENT_KEY24.equals(use24HourSetting) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    is24Hr = null;
                }
                DateFormat.set24HourTimePref(is24Hr);
                updateDebugViewAttributeState();
                StrictMode.initThreadDefaults(data.appInfo);
                StrictMode.initVmDefaults(data.appInfo);
                sendPreloadMessage(2, data, 0);
                if (data.debugMode != 0) {
                    Debug.changeDebugPort(8100);
                    if (data.debugMode == 2) {
                        Slog.w(TAG, "Application " + data.info.getPackageName() + " is waiting for the debugger on port 8100...");
                        IActivityManager mgr = ActivityManager.getService();
                        try {
                            mgr.showWaitingForDebugger(this.mAppThread, true);
                            Debug.waitForDebugger();
                            try {
                                mgr.showWaitingForDebugger(this.mAppThread, false);
                            } catch (RemoteException ex) {
                                throw ex.rethrowFromSystemServer();
                            }
                        } catch (RemoteException ex2) {
                            throw ex2.rethrowFromSystemServer();
                        }
                    } else {
                        Slog.w(TAG, "Application " + data.info.getPackageName() + " can be debugged on port 8100...");
                    }
                }
                boolean isAppProfileable = data.appInfo.isProfileableByShell();
                Trace.setAppTracingAllowed(isAppProfileable);
                if (isAppProfileable && data.enableBinderTracking) {
                    Binder.enableTracing();
                }
                if (isAppProfileable || Build.IS_DEBUGGABLE) {
                    nInitZygoteChildHeapProfiling();
                }
                HardwareRenderer.setDebuggingEnabled(((data.appInfo.flags & 2) != 0) || Build.IS_DEBUGGABLE);
                HardwareRenderer.setPackageName(data.appInfo.packageName);
                Trace.traceBegin(64, "Setup proxies");
                IBinder b = ServiceManager.getService("connectivity");
                if (b != null) {
                    try {
                        Proxy.setHttpProxySystemProperty(IConnectivityManager.Stub.asInterface(b).getProxyForNetwork(null));
                    } catch (RemoteException e) {
                        Trace.traceEnd(64);
                        throw e.rethrowFromSystemServer();
                    }
                }
                Trace.traceEnd(64);
                if (data.instrumentationName != null) {
                    try {
                        InstrumentationInfo ii2 = new ApplicationPackageManager(null, getPackageManager()).getInstrumentationInfo(data.instrumentationName, 0);
                        if (!Objects.equals(data.appInfo.primaryCpuAbi, ii2.primaryCpuAbi) || !Objects.equals(data.appInfo.secondaryCpuAbi, ii2.secondaryCpuAbi)) {
                            Slog.w(TAG, "Package uses different ABI(s) than its instrumentation: package[" + data.appInfo.packageName + "]: " + data.appInfo.primaryCpuAbi + ", " + data.appInfo.secondaryCpuAbi + " instrumentation[" + ii2.packageName + "]: " + ii2.primaryCpuAbi + ", " + ii2.secondaryCpuAbi);
                        }
                        this.mInstrumentationPackageName = ii2.packageName;
                        this.mInstrumentationAppDir = ii2.sourceDir;
                        this.mInstrumentationSplitAppDirs = ii2.splitSourceDirs;
                        this.mInstrumentationLibDir = getInstrumentationLibrary(data.appInfo, ii2);
                        this.mInstrumentedAppDir = data.info.getAppDir();
                        this.mInstrumentedSplitAppDirs = data.info.getSplitAppDirs();
                        this.mInstrumentedLibDir = data.info.getLibDir();
                        ii = ii2;
                    } catch (PackageManager.NameNotFoundException e2) {
                        throw new RuntimeException("Unable to find instrumentation info for: " + data.instrumentationName);
                    }
                } else {
                    this.mIsNeedStartUiProbe = SystemProperties.getBoolean("ro.feature.dfr.appeye", true) || SystemProperties.getInt("ro.logsystem.usertype", 0) == 3;
                    if (this.mIsNeedStartUiProbe && (blockMonitor = HwFrameworkFactory.getBlockMonitor()) != null) {
                        blockMonitor.initialize();
                    }
                    ii = null;
                }
                if (ZygoteInit.sIsMygote) {
                    VMRuntime.getRuntime();
                    VMRuntime.setAppInfo(data.appInfo.dataDir, data.appInfo.longVersionCode, data.persistent);
                }
                ContextImpl appContext2 = ContextImpl.createAppContext(this, data.info);
                if (USE_CACHE) {
                    sendPreloadMessage(6, appContext2, 0);
                }
                updateLocaleListFromAppContext(appContext2, this.mResourcesManager.getConfiguration().getLocales());
                if (!Process.isIsolated()) {
                    int oldMask = StrictMode.allowThreadDiskWritesMask();
                    try {
                        setupGraphicsSupport(appContext2);
                    } finally {
                        StrictMode.setThreadPolicyMask(oldMask);
                    }
                } else {
                    HardwareRenderer.setIsolatedProcess(true);
                }
                Trace.traceBegin(64, "NetworkSecurityConfigProvider.install");
                NetworkSecurityConfigProvider.install(appContext2);
                Trace.traceEnd(64);
                if (ii != null) {
                    try {
                        try {
                            instrApp = getPackageManager().getApplicationInfo(ii.packageName, 0, UserHandle.myUserId());
                        } catch (RemoteException e3) {
                        }
                    } catch (RemoteException e4) {
                        instrApp = null;
                        if (instrApp != null) {
                        }
                        ii.copyTo(instrApp2);
                        instrApp2.initForUser(UserHandle.myUserId());
                        appContext = appContext2;
                        z = true;
                        LoadedApk pi = getPackageInfo(instrApp2, data.compatInfo, appContext2.getClassLoader(), false, true, false);
                        if (ZygoteInit.sIsMygote) {
                        }
                        ContextImpl instrContext = ContextImpl.createAppContext(this, pi, appContext.getOpPackageName());
                        this.mInstrumentation = (Instrumentation) instrContext.getClassLoader().loadClass(data.instrumentationName.getClassName()).newInstance();
                        this.mInstrumentation.init(this, instrContext, appContext, new ComponentName(ii.packageName, ii.name), data.instrumentationWatcher, data.instrumentationUiAutomationConnection);
                        Profiler profiler = this.mProfiler;
                        profiler.handlingProfiling = true;
                        File file = new File(profiler.profileFile);
                        file.getParentFile().mkdirs();
                        Debug.startMethodTracing(file.toString(), 8388608);
                        if ((data.appInfo.flags & 1048576) == 0) {
                        }
                        if (HwFrameworkFactory.getHwActivityThread() != null) {
                        }
                        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
                        StrictMode.ThreadPolicy writesAllowedPolicy = StrictMode.getThreadPolicy();
                        app = data.info.makeApplication(data.restrictedBackupMode, null);
                        app.setAutofillOptions(data.autofillOptions);
                        app.setContentCaptureOptions(data.contentCaptureOptions);
                        this.mInitialApplication = app;
                        if (this.mMultiWindowOverrideConfig != null) {
                        }
                        sendPreloadMessage(8, app, 0);
                        installContentProviders(app, data.providers);
                        this.mInstrumentation.onCreate(data.instrumentationArgs);
                        try {
                            this.mInstrumentation.callApplicationOnCreate(app);
                            sendPreloadMessage(4, app, 0);
                            HwThemeManager.initForThemeFont(this.mConfiguration);
                            HwThemeManager.setThemeFont();
                            if (!mChangedFont) {
                            }
                        } catch (Exception e5) {
                            if (!this.mInstrumentation.onException(app, e5)) {
                                throw new RuntimeException("Unable to create application " + app.getClass().getName() + ": " + e5.toString(), e5);
                            }
                        }
                        FontsContract.setApplicationContextForResources(appContext);
                        if (!Process.isIsolated()) {
                        }
                        if (ZygoteInit.sIsMygote) {
                        }
                        finishPreloaded();
                        Jlog.betaUserPrint(407, "#ST:<" + hiviewTime + ">");
                        return;
                    }
                    if (instrApp != null) {
                        instrApp2 = new ApplicationInfo();
                    } else {
                        instrApp2 = instrApp;
                    }
                    ii.copyTo(instrApp2);
                    instrApp2.initForUser(UserHandle.myUserId());
                    appContext = appContext2;
                    z = true;
                    LoadedApk pi2 = getPackageInfo(instrApp2, data.compatInfo, appContext2.getClassLoader(), false, true, false);
                    if (ZygoteInit.sIsMygote) {
                        pi2.setDefaultClassLoader(data.info.getDefaultClassLoader());
                    }
                    ContextImpl instrContext2 = ContextImpl.createAppContext(this, pi2, appContext.getOpPackageName());
                    try {
                        this.mInstrumentation = (Instrumentation) instrContext2.getClassLoader().loadClass(data.instrumentationName.getClassName()).newInstance();
                        this.mInstrumentation.init(this, instrContext2, appContext, new ComponentName(ii.packageName, ii.name), data.instrumentationWatcher, data.instrumentationUiAutomationConnection);
                        if (this.mProfiler.profileFile != null && !ii.handleProfiling && this.mProfiler.profileFd == null) {
                            Profiler profiler2 = this.mProfiler;
                            profiler2.handlingProfiling = true;
                            File file2 = new File(profiler2.profileFile);
                            file2.getParentFile().mkdirs();
                            Debug.startMethodTracing(file2.toString(), 8388608);
                        }
                    } catch (Exception e6) {
                        throw new RuntimeException("Unable to instantiate instrumentation " + data.instrumentationName + ": " + e6.toString(), e6);
                    }
                } else {
                    appContext = appContext2;
                    z = true;
                    this.mInstrumentation = new Instrumentation();
                    this.mInstrumentation.basicInit(this);
                }
                if ((data.appInfo.flags & 1048576) == 0) {
                    VMRuntime.getRuntime().clearGrowthLimit();
                } else {
                    VMRuntime.getRuntime().clampGrowthLimit();
                }
                if (HwFrameworkFactory.getHwActivityThread() != null) {
                    HwFrameworkFactory.getHwActivityThread().handleHwPreloadStatus(2);
                }
                StrictMode.ThreadPolicy savedPolicy2 = StrictMode.allowThreadDiskWrites();
                StrictMode.ThreadPolicy writesAllowedPolicy2 = StrictMode.getThreadPolicy();
                try {
                    app = data.info.makeApplication(data.restrictedBackupMode, null);
                    app.setAutofillOptions(data.autofillOptions);
                    app.setContentCaptureOptions(data.contentCaptureOptions);
                    this.mInitialApplication = app;
                    if (this.mMultiWindowOverrideConfig != null) {
                        Slog.i(TAG, "Bind application and apply override config");
                        this.mResourcesManager.applyConfigurationToAppResourcesLocked(app.getDisplayId(), this.mMultiWindowOverrideConfig);
                    }
                    sendPreloadMessage(8, app, 0);
                    if (!data.restrictedBackupMode && !ArrayUtils.isEmpty(data.providers)) {
                        installContentProviders(app, data.providers);
                    }
                    try {
                        this.mInstrumentation.onCreate(data.instrumentationArgs);
                        this.mInstrumentation.callApplicationOnCreate(app);
                        sendPreloadMessage(4, app, 0);
                        HwThemeManager.initForThemeFont(this.mConfiguration);
                        HwThemeManager.setThemeFont();
                        if (!mChangedFont) {
                            mChangedFont = z;
                            SystemFonts.loadHwSystemFonts();
                        }
                        FontsContract.setApplicationContextForResources(appContext);
                        if (!Process.isIsolated()) {
                            try {
                                ApplicationInfo info = getPackageManager().getApplicationInfo(data.appInfo.packageName, 128, UserHandle.myUserId());
                                if (!(info == null || info.metaData == null || (preloadedFontsResource = info.metaData.getInt(ApplicationInfo.METADATA_PRELOADED_FONTS, 0)) == 0)) {
                                    data.info.getResources().preloadFonts(preloadedFontsResource);
                                }
                            } catch (RemoteException e7) {
                                throw e7.rethrowFromSystemServer();
                            }
                        }
                        if (ZygoteInit.sIsMygote) {
                            this.mH.sendEmptyMessageDelayed(1007, 0);
                        }
                        finishPreloaded();
                        Jlog.betaUserPrint(407, "#ST:<" + hiviewTime + ">");
                        return;
                    } catch (Exception e8) {
                        throw new RuntimeException("Exception thrown in onCreate() of " + data.instrumentationName + ": " + e8.toString(), e8);
                    }
                } finally {
                    if (data.appInfo.targetSdkVersion < 27 || StrictMode.getThreadPolicy().equals(writesAllowedPolicy2)) {
                        StrictMode.setThreadPolicy(savedPolicy2);
                    }
                }
            }
        }
        agent = null;
        sendPreloadMessage(5, currentPackageName(), 0);
        Process.setArgV0(data.processName);
        DdmHandleAppName.setAppName(data.processName, UserHandle.myUserId());
        VMRuntime.setProcessPackageName(data.appInfo.packageName);
        ServiceManager.handleApplicationBinded(data.processName);
        VMRuntime.setProcessDataDirectory(data.appInfo.dataDir);
        if (this.mProfiler.profileFd != null) {
        }
        if (data.appInfo.targetSdkVersion <= 12) {
        }
        UtilConfig.setThrowExceptionForUpperArrayOutOfBounds(data.appInfo.targetSdkVersion < 29);
        Message.updateCheckRecycle(data.appInfo.targetSdkVersion);
        ImageDecoder.sApiLevel = data.appInfo.targetSdkVersion;
        TimeZone.setDefault(null);
        LocaleList.setDefault(data.config.getLocales());
        synchronized (this.mResourcesManager) {
        }
        while (true) {
        }
    }

    /* access modifiers changed from: package-private */
    public final void finishInstrumentation(int resultCode, Bundle results) {
        IActivityManager am = ActivityManager.getService();
        if (this.mProfiler.profileFile != null && this.mProfiler.handlingProfiling && this.mProfiler.profileFd == null) {
            Debug.stopMethodTracing();
        }
        try {
            am.finishInstrumentation(this.mAppThread, resultCode, results);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    private void installContentProviders(Context context, List<ProviderInfo> providers) {
        ArrayList<ContentProviderHolder> results = new ArrayList<>();
        for (ProviderInfo cpi : providers) {
            if (DEBUG_PROVIDER) {
                StringBuilder buf = new StringBuilder(128);
                buf.append("Pub ");
                buf.append(cpi.authority);
                buf.append(": ");
                buf.append(cpi.name);
                Log.i(TAG, buf.toString());
            }
            ContentProviderHolder cph = installProvider(context, null, cpi, false, true, true);
            if (cph != null) {
                cph.noReleaseNeeded = true;
                results.add(cph);
            }
        }
        try {
            ActivityManager.getService().publishContentProviders(getApplicationThread(), results);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public final IContentProvider acquireProvider(Context c, String auth, int userId, boolean stable) {
        ContentProviderHolder holder;
        IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
        if (provider != null) {
            return provider;
        }
        try {
            synchronized (getGetProviderLock(auth, userId)) {
                holder = ActivityManager.getService().getContentProvider(getApplicationThread(), c.getOpPackageName(), auth, userId, stable);
            }
            if (holder != null) {
                return installProvider(c, holder, holder.info, true, holder.noReleaseNeeded, stable).provider;
            }
            Slog.e(TAG, "Failed to find provider info for " + auth);
            return null;
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    private Object getGetProviderLock(String auth, int userId) {
        Object lock;
        ProviderKey key = new ProviderKey(auth, userId);
        synchronized (this.mGetProviderLocks) {
            lock = this.mGetProviderLocks.get(key);
            if (lock == null) {
                lock = key;
                this.mGetProviderLocks.put(key, lock);
            }
        }
        return lock;
    }

    private final void incProviderRefLocked(ProviderRefCount prc, boolean stable) {
        int unstableDelta;
        if (stable) {
            prc.stableCount++;
            if (prc.stableCount == 1) {
                if (prc.removePending) {
                    unstableDelta = -1;
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "incProviderRef: stable snatched provider from the jaws of death");
                    }
                    prc.removePending = false;
                    this.mH.removeMessages(131, prc);
                } else {
                    unstableDelta = 0;
                }
                try {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "incProviderRef Now stable - " + prc.holder.info.name + ": unstableDelta=" + unstableDelta);
                    }
                    ActivityManager.getService().refContentProvider(prc.holder.connection, 1, unstableDelta);
                } catch (RemoteException e) {
                }
            }
        } else {
            prc.unstableCount++;
            if (prc.unstableCount != 1) {
                return;
            }
            if (prc.removePending) {
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "incProviderRef: unstable snatched provider from the jaws of death");
                }
                prc.removePending = false;
                this.mH.removeMessages(131, prc);
                return;
            }
            try {
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "incProviderRef: Now unstable - " + prc.holder.info.name);
                }
                ActivityManager.getService().refContentProvider(prc.holder.connection, 0, 1);
            } catch (RemoteException e2) {
            }
        }
    }

    @UnsupportedAppUsage
    public final IContentProvider acquireExistingProvider(Context c, String auth, int userId, boolean stable) {
        synchronized (this.mProviderMap) {
            ProviderClientRecord pr = this.mProviderMap.get(new ProviderKey(auth, userId));
            if (pr == null) {
                return null;
            }
            IContentProvider provider = pr.mProvider;
            IBinder jBinder = provider.asBinder();
            if (!jBinder.isBinderAlive()) {
                Log.i(TAG, "Acquiring provider " + auth + " for user " + userId + ": existing object's process dead");
                handleUnstableProviderDiedLocked(jBinder, true);
                return null;
            }
            ProviderRefCount prc = this.mProviderRefCountMap.get(jBinder);
            if (prc != null) {
                incProviderRefLocked(prc, stable);
            }
            return provider;
        }
    }

    @UnsupportedAppUsage
    public final boolean releaseProvider(IContentProvider provider, boolean stable) {
        int i = 0;
        if (provider == null) {
            return false;
        }
        IBinder jBinder = provider.asBinder();
        synchronized (this.mProviderMap) {
            ProviderRefCount prc = this.mProviderRefCountMap.get(jBinder);
            if (prc == null) {
                return false;
            }
            boolean lastRef = false;
            if (stable) {
                if (prc.stableCount == 0) {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "releaseProvider: stable ref count already 0, how?");
                    }
                    return false;
                }
                prc.stableCount--;
                if (prc.stableCount == 0) {
                    lastRef = prc.unstableCount == 0;
                    try {
                        if (DEBUG_PROVIDER) {
                            Slog.v(TAG, "releaseProvider: No longer stable w/lastRef=" + lastRef + " - " + prc.holder.info.name);
                        }
                        IActivityManager service = ActivityManager.getService();
                        IBinder iBinder = prc.holder.connection;
                        if (lastRef) {
                            i = 1;
                        }
                        service.refContentProvider(iBinder, -1, i);
                    } catch (RemoteException e) {
                    }
                }
            } else if (prc.unstableCount == 0) {
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "releaseProvider: unstable ref count already 0, how?");
                }
                return false;
            } else {
                prc.unstableCount--;
                if (prc.unstableCount == 0) {
                    lastRef = prc.stableCount == 0;
                    if (!lastRef) {
                        try {
                            if (DEBUG_PROVIDER) {
                                Slog.v(TAG, "releaseProvider: No longer unstable - " + prc.holder.info.name);
                            }
                            ActivityManager.getService().refContentProvider(prc.holder.connection, 0, -1);
                        } catch (RemoteException e2) {
                        }
                    }
                }
            }
            if (lastRef) {
                if (!prc.removePending) {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "releaseProvider: Enqueueing pending removal - " + prc.holder.info.name);
                    }
                    prc.removePending = true;
                    this.mH.sendMessageDelayed(this.mH.obtainMessage(131, prc), 1000);
                } else {
                    Slog.w(TAG, "Duplicate remove pending of provider " + prc.holder.info.name);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public final void completeRemoveProvider(ProviderRefCount prc) {
        synchronized (this.mProviderMap) {
            if (!prc.removePending) {
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "completeRemoveProvider: lost the race, provider still in use");
                }
                return;
            }
            prc.removePending = false;
            IBinder jBinder = prc.holder.provider.asBinder();
            if (this.mProviderRefCountMap.get(jBinder) == prc) {
                this.mProviderRefCountMap.remove(jBinder);
            }
            for (int i = this.mProviderMap.size() - 1; i >= 0; i--) {
                if (this.mProviderMap.valueAt(i).mProvider.asBinder() == jBinder) {
                    this.mProviderMap.removeAt(i);
                }
            }
        }
        try {
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "removeProvider: Invoking ActivityManagerService.removeContentProvider(" + prc.holder.info.name + ")");
            }
            ActivityManager.getService().removeContentProvider(prc.holder.connection, false);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void handleUnstableProviderDied(IBinder provider, boolean fromClient) {
        synchronized (this.mProviderMap) {
            handleUnstableProviderDiedLocked(provider, fromClient);
        }
    }

    /* access modifiers changed from: package-private */
    public final void handleUnstableProviderDiedLocked(IBinder provider, boolean fromClient) {
        ProviderRefCount prc = this.mProviderRefCountMap.get(provider);
        if (prc != null) {
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "Cleaning up dead provider " + provider + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + prc.holder.info.name);
            }
            this.mProviderRefCountMap.remove(provider);
            for (int i = this.mProviderMap.size() - 1; i >= 0; i--) {
                ProviderClientRecord pr = this.mProviderMap.valueAt(i);
                if (pr != null && pr.mProvider.asBinder() == provider) {
                    Slog.i(TAG, "Removing dead content provider:" + pr.mProvider.toString());
                    this.mProviderMap.removeAt(i);
                }
            }
            if (fromClient) {
                try {
                    ActivityManager.getService().unstableProviderDied(prc.holder.connection);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void appNotRespondingViaProvider(IBinder provider) {
        synchronized (this.mProviderMap) {
            ProviderRefCount prc = this.mProviderRefCountMap.get(provider);
            if (prc != null) {
                try {
                    ActivityManager.getService().appNotRespondingViaProvider(prc.holder.connection);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    private ProviderClientRecord installProviderAuthoritiesLocked(IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
        String[] auths = holder.info.authority.split(";");
        int userId = UserHandle.getUserId(holder.info.applicationInfo.uid);
        if (provider != null) {
            for (String auth : auths) {
                char c = 65535;
                switch (auth.hashCode()) {
                    case -845193793:
                        if (auth.equals("com.android.contacts")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -456066902:
                        if (auth.equals(CalendarContract.AUTHORITY)) {
                            c = 4;
                            break;
                        }
                        break;
                    case -172298781:
                        if (auth.equals(CallLog.AUTHORITY)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 63943420:
                        if (auth.equals(CallLog.SHADOW_AUTHORITY)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 783201304:
                        if (auth.equals(DeviceConfig.NAMESPACE_TELEPHONY)) {
                            c = 6;
                            break;
                        }
                        break;
                    case 1312704747:
                        if (auth.equals(Downloads.Impl.AUTHORITY)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1995645513:
                        if (auth.equals(BlockedNumberContract.AUTHORITY)) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        Binder.allowBlocking(provider.asBinder());
                        break;
                }
            }
        }
        ProviderClientRecord pcr = new ProviderClientRecord(auths, provider, localProvider, holder);
        for (String auth2 : auths) {
            ProviderKey key = new ProviderKey(auth2, userId);
            if (this.mProviderMap.get(key) != null) {
                Slog.w(TAG, "Content provider " + pcr.mHolder.info.name + " already published as " + auth2);
            } else {
                this.mProviderMap.put(key, pcr);
            }
        }
        return pcr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00dd  */
    @UnsupportedAppUsage
    private ContentProviderHolder installProvider(Context context, ContentProviderHolder holder, ProviderInfo info, boolean noisy, boolean noReleaseNeeded, boolean stable) {
        IContentProvider provider;
        ContentProvider localProvider;
        Throwable th;
        ContentProviderHolder retHolder;
        ProviderRefCount providerRefCount;
        if (holder == null || holder.provider == null) {
            if (DEBUG_PROVIDER || noisy) {
                Slog.d(TAG, "Loading provider " + info.authority + ": " + info.name);
            }
            Context c = null;
            ApplicationInfo ai = info.applicationInfo;
            if (context.getPackageName().equals(ai.packageName)) {
                c = context;
            } else {
                Application application = this.mInitialApplication;
                if (application == null || !application.getPackageName().equals(ai.packageName)) {
                    try {
                        try {
                            c = context.createPackageContext(ai.packageName, 1);
                        } catch (PackageManager.NameNotFoundException e) {
                            Slog.w(TAG, "Unable to get context for package " + ai.packageName);
                            if (c == null) {
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e2) {
                        Slog.w(TAG, "Unable to get context for package " + ai.packageName);
                        if (c == null) {
                        }
                    }
                } else {
                    c = this.mInitialApplication;
                }
            }
            if (c == null) {
                Slog.w(TAG, "Unable to get context for package " + ai.packageName + " while loading content provider " + info.name);
                return null;
            }
            if (info.splitName != null) {
                try {
                    c = c.createContextForSplit(info.splitName);
                } catch (PackageManager.NameNotFoundException e3) {
                    throw new RuntimeException(e3);
                }
            }
            try {
                ClassLoader cl = c.getClassLoader();
                LoadedApk packageInfo = peekPackageInfo(ai.packageName, true);
                if (packageInfo == null) {
                    packageInfo = getSystemContext().mPackageInfo;
                }
                ContentProvider localProvider2 = packageInfo.getAppFactory().instantiateProvider(cl, info.name);
                provider = localProvider2.getIContentProvider();
                if (provider == null) {
                    Slog.e(TAG, "Failed to instantiate class " + info.name + " from sourceDir " + info.applicationInfo.sourceDir);
                    return null;
                }
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "Instantiating local provider " + info.name);
                }
                localProvider2.attachInfo(c, info);
                localProvider = localProvider2;
            } catch (Exception e4) {
                if (this.mInstrumentation.onException(null, e4)) {
                    return null;
                }
                throw new RuntimeException("Unable to get provider " + info.name + ": " + e4.toString(), e4);
            }
        } else {
            IContentProvider provider2 = holder.provider;
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "Installing external provider " + info.authority + ": " + info.name);
            }
            provider = provider2;
            localProvider = null;
        }
        if (!context.getPackageName().equals(info.applicationInfo.packageName)) {
            LogPower.notifyAction(UserHandle.getAppId(info.applicationInfo.uid), "acquire_provider", info.applicationInfo.packageName, "", null);
        }
        synchronized (this.mProviderMap) {
            try {
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "Checking to add " + provider + " / " + info.name);
                }
                IBinder jBinder = provider.asBinder();
                if (localProvider != null) {
                    ComponentName cname = new ComponentName(info.packageName, info.name);
                    ProviderClientRecord pr = this.mLocalProvidersByName.get(cname);
                    if (pr != null) {
                        if (DEBUG_PROVIDER) {
                            Slog.v(TAG, "installProvider: lost the race, using existing local provider");
                        }
                        IContentProvider iContentProvider = pr.mProvider;
                    } else {
                        ContentProviderHolder holder2 = new ContentProviderHolder(info);
                        try {
                            holder2.provider = provider;
                            holder2.noReleaseNeeded = true;
                            pr = installProviderAuthoritiesLocked(provider, localProvider, holder2);
                            this.mLocalProviders.put(jBinder, pr);
                            this.mLocalProvidersByName.put(cname, pr);
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    retHolder = pr.mHolder;
                } else {
                    ProviderRefCount prc = this.mProviderRefCountMap.get(jBinder);
                    if (prc != null) {
                        if (DEBUG_PROVIDER) {
                            Slog.v(TAG, "installProvider: lost the race, updating ref count");
                        }
                        if (!noReleaseNeeded) {
                            incProviderRefLocked(prc, stable);
                            try {
                                ActivityManager.getService().removeContentProvider(holder.connection, stable);
                            } catch (RemoteException e5) {
                                Log.e(TAG, "installProvider()");
                            }
                        }
                    } else {
                        ProviderClientRecord client = installProviderAuthoritiesLocked(provider, localProvider, holder);
                        if (noReleaseNeeded) {
                            prc = new ProviderRefCount(holder, client, 1000, 1000);
                        } else {
                            if (stable) {
                                providerRefCount = new ProviderRefCount(holder, client, 1, 0);
                            } else {
                                providerRefCount = new ProviderRefCount(holder, client, 0, 1);
                            }
                            prc = providerRefCount;
                        }
                        this.mProviderRefCountMap.put(jBinder, prc);
                    }
                    retHolder = prc.holder;
                }
                return retHolder;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRunIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) {
        try {
            Class.forName(entryPoint).getMethod("main", String[].class).invoke(null, entryPointArgs);
            System.exit(0);
        } catch (ReflectiveOperationException e) {
            throw new AndroidRuntimeException("runIsolatedEntryPoint failed", e);
        }
    }

    @UnsupportedAppUsage
    private void attach(boolean system, long startSeq) {
        sCurrentActivityThread = this;
        this.mSystemThread = system;
        if (!system) {
            DdmHandleAppName.setAppName("<pre-initialized>", UserHandle.myUserId());
            RuntimeInit.setApplicationObject(this.mAppThread.asBinder());
            IActivityManager mgr = ActivityManager.getService();
            try {
                Slog.d(TAG, "Attach thread to application");
                mgr.attachApplication(this.mAppThread, startSeq);
                BinderInternal.addGcWatcher(new Runnable() {
                    /* class android.app.ActivityThread.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (ActivityThread.this.mSomeActivitiesChanged) {
                            Runtime runtime = Runtime.getRuntime();
                            if (runtime.totalMemory() - runtime.freeMemory() > (3 * runtime.maxMemory()) / 4) {
                                ActivityThread.this.mSomeActivitiesChanged = false;
                                try {
                                    ActivityTaskManager.getService().releaseSomeActivities(ActivityThread.this.mAppThread);
                                } catch (RemoteException e) {
                                    throw e.rethrowFromSystemServer();
                                }
                            }
                        }
                    }
                });
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            DdmHandleAppName.setAppName("system_process", UserHandle.myUserId());
            try {
                this.mInstrumentation = new Instrumentation();
                this.mInstrumentation.basicInit(this);
                this.mInitialApplication = ContextImpl.createAppContext(this, getSystemContext().mPackageInfo).mPackageInfo.makeApplication(true, null);
                this.mInitialApplication.onCreate();
                HwThemeManager.setThemeFont();
                SystemFonts.loadHwSystemFonts();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate Application():" + e.toString(), e);
            }
        }
        ViewRootImpl.addConfigCallback(new ViewRootImpl.ConfigChangedCallback() {
            /* class android.app.$$Lambda$ActivityThread$Wg40iAoNYFxps_KmrqtgptTB054 */

            @Override // android.view.ViewRootImpl.ConfigChangedCallback
            public final void onConfigurationChanged(Configuration configuration) {
                ActivityThread.this.lambda$attach$1$ActivityThread(configuration);
            }
        });
    }

    public /* synthetic */ void lambda$attach$1$ActivityThread(Configuration globalConfig) {
        synchronized (this.mResourcesManager) {
            Configuration globalConfig2 = updateConfig(globalConfig);
            if (this.mResourcesManager.applyConfigurationToResourcesLocked(globalConfig2, null)) {
                updateLocaleListFromAppContext(this.mInitialApplication.getApplicationContext(), this.mResourcesManager.getConfiguration().getLocales());
                if (this.mPendingConfiguration == null || this.mPendingConfiguration.isOtherSeqNewer(globalConfig2)) {
                    this.mPendingConfiguration = globalConfig2;
                    sendMessage(118, globalConfig2);
                }
            }
        }
    }

    @UnsupportedAppUsage
    public static ActivityThread systemMain() {
        if (!ActivityManager.isHighEndGfx()) {
            ThreadedRenderer.disable(true);
        } else {
            ThreadedRenderer.enableForegroundTrimming();
        }
        ActivityThread thread = new ActivityThread();
        thread.attach(true, 0);
        return thread;
    }

    public static void updateHttpProxy(Context context) {
        ConnectivityManager cm = ConnectivityManager.from(context);
        if (cm != null) {
            Proxy.setHttpProxySystemProperty(cm.getDefaultProxy());
        }
    }

    @UnsupportedAppUsage
    public final void installSystemProviders(List<ProviderInfo> providers) {
        if (providers != null) {
            installContentProviders(this.mInitialApplication, providers);
        }
    }

    public int getIntCoreSetting(String key, int defaultValue) {
        synchronized (this.mResourcesManager) {
            if (this.mCoreSettings == null) {
                return defaultValue;
            }
            return this.mCoreSettings.getInt(key, defaultValue);
        }
    }

    private static class AndroidOs extends ForwardingOs {
        public static void install() {
            Os def;
            if (ContentResolver.DEPRECATE_DATA_COLUMNS) {
                do {
                    def = Os.getDefault();
                } while (!Os.compareAndSetDefault(def, new AndroidOs(def)));
            }
        }

        private AndroidOs(Os os) {
            super(os);
        }

        private FileDescriptor openDeprecatedDataPath(String path, int mode) throws ErrnoException {
            Uri uri = ContentResolver.translateDeprecatedDataPath(path);
            Log.v(ActivityThread.TAG, "Redirecting " + path + " to " + uri);
            ContentResolver cr = ActivityThread.currentActivityThread().getApplication().getContentResolver();
            try {
                FileDescriptor fd = new FileDescriptor();
                fd.setInt$(cr.openFileDescriptor(uri, FileUtils.translateModePosixToString(mode)).detachFd());
                return fd;
            } catch (SecurityException e) {
                throw new ErrnoException(e.getMessage(), OsConstants.EACCES);
            } catch (FileNotFoundException e2) {
                throw new ErrnoException(e2.getMessage(), OsConstants.ENOENT);
            }
        }

        private void deleteDeprecatedDataPath(String path) throws ErrnoException {
            Uri uri = ContentResolver.translateDeprecatedDataPath(path);
            Log.v(ActivityThread.TAG, "Redirecting " + path + " to " + uri);
            try {
                if (ActivityThread.currentActivityThread().getApplication().getContentResolver().delete(uri, null, null) == 0) {
                    throw new FileNotFoundException();
                }
            } catch (SecurityException e) {
                throw new ErrnoException(e.getMessage(), OsConstants.EACCES);
            } catch (FileNotFoundException e2) {
                throw new ErrnoException(e2.getMessage(), OsConstants.ENOENT);
            }
        }

        public boolean access(String path, int mode) throws ErrnoException {
            if (path == null || !path.startsWith(ContentResolver.DEPRECATE_DATA_PREFIX)) {
                return ActivityThread.super.access(path, mode);
            }
            IoUtils.closeQuietly(openDeprecatedDataPath(path, FileUtils.translateModeAccessToPosix(mode)));
            return true;
        }

        public FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
            if (path == null || !path.startsWith(ContentResolver.DEPRECATE_DATA_PREFIX)) {
                return ActivityThread.super.open(path, flags, mode);
            }
            return openDeprecatedDataPath(path, mode);
        }

        public StructStat stat(String path) throws ErrnoException {
            if (path == null || !path.startsWith(ContentResolver.DEPRECATE_DATA_PREFIX)) {
                return ActivityThread.super.stat(path);
            }
            FileDescriptor fd = openDeprecatedDataPath(path, OsConstants.O_RDONLY);
            try {
                return android.system.Os.fstat(fd);
            } finally {
                IoUtils.closeQuietly(fd);
            }
        }

        public void unlink(String path) throws ErrnoException {
            if (path == null || !path.startsWith(ContentResolver.DEPRECATE_DATA_PREFIX)) {
                ActivityThread.super.unlink(path);
            } else {
                deleteDeprecatedDataPath(path);
            }
        }

        public void remove(String path) throws ErrnoException {
            if (path == null || !path.startsWith(ContentResolver.DEPRECATE_DATA_PREFIX)) {
                ActivityThread.super.remove(path);
            } else {
                deleteDeprecatedDataPath(path);
            }
        }

        public void rename(String oldPath, String newPath) throws ErrnoException {
            try {
                ActivityThread.super.rename(oldPath, newPath);
            } catch (ErrnoException e) {
                if (e.errno == OsConstants.EXDEV) {
                    Log.v(ActivityThread.TAG, "Recovering failed rename " + oldPath + " to " + newPath);
                    try {
                        Files.move(new File(oldPath).toPath(), new File(newPath).toPath(), new CopyOption[0]);
                    } catch (IOException e2) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    public static void main(String[] args) {
        Trace.traceBegin(64, "ActivityThreadMain");
        long hiviewTime = SystemClock.uptimeMillis();
        AndroidOs.install();
        CloseGuard.setEnabled(false);
        Environment.initForCurrentUser();
        Log.initHWLog();
        TrustedCertificateStore.setDefaultUserDirectory(Environment.getUserConfigDirectory(UserHandle.myUserId()));
        Process.setArgV0("<pre-initialized>");
        Looper.prepareMainLooper();
        long startSeq = 0;
        long preloadStatus = 0;
        ArrayList<String> displayArgs = new ArrayList<>();
        if (args != null) {
            for (int i = args.length - 1; i >= 0; i--) {
                try {
                    if (args[i] != null && args[i].startsWith(PROC_START_SEQ_IDENT)) {
                        startSeq = Long.parseLong(args[i].substring(PROC_START_SEQ_IDENT.length()));
                    } else if (args[i] == null || !args[i].startsWith(PROC_START_PRELOADSTATUS)) {
                        if (args[i] != null) {
                            displayArgs.add(0, args[i]);
                        }
                    } else {
                        preloadStatus = Long.parseLong(args[i].substring(PROC_START_PRELOADSTATUS.length()));
                        Slog.i(TAG, "preloadStatus:" + preloadStatus);
                    }
                } catch (NumberFormatException e) {
                    Slog.w(TAG, "args format error.");
                }
            }
        }
        ActivityThread thread = new ActivityThread();
        if (HwFrameworkFactory.getHwActivityThread() != null) {
            HwFrameworkFactory.getHwActivityThread().setHwPreloadStatus(preloadStatus);
            if (!HwFrameworkFactory.getHwActivityThread().initHwArgs(new ActivityThreadAdapterEx(thread), (String[]) displayArgs.toArray(new String[displayArgs.size()]))) {
                initHwArgs(thread, (String[]) displayArgs.toArray(new String[displayArgs.size()]));
            }
        }
        synchronized (mPreloadLock) {
            initPreloadHandlerThread();
        }
        thread.attach(false, startSeq);
        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
        Jlog.betaUserPrint(406, "#ST:<" + hiviewTime + ">");
        Trace.traceEnd(64);
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void purgePendingResources() {
        Trace.traceBegin(64, "purgePendingResources");
        nPurgePendingResources();
        Trace.traceEnd(64);
    }

    public void handlePCWindowStateChanged(IBinder token, int windowState) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null && (r.window instanceof AbsWindow)) {
            ((AbsWindow) r.window).onWindowStateChanged(windowState);
        }
    }

    private void setInitParam(int displayId, int width, int height) {
        Display display;
        this.mDisplayId = displayId;
        HwPCUtils.setPCDisplayID(displayId);
        if (((width > 0 && height > 0) || HwPCUtils.mTouchDeviceID != -1) && (display = this.mResourcesManager.getAdjustedDisplay(displayId, Resources.getSystem())) != null) {
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            if (this.mOverrideConfig == null) {
                this.mOverrideConfig = new Configuration();
            }
            if (width > 0 && height > 0) {
                dm.widthPixels = width;
                dm.heightPixels = height;
            }
            Configuration configuration = this.mOverrideConfig;
            configuration.touchscreen = 1;
            configuration.densityDpi = dm.densityDpi;
            Configuration configuration2 = this.mOverrideConfig;
            int i = (int) (((float) dm.widthPixels) / dm.density);
            configuration2.screenWidthDp = i;
            configuration2.compatScreenWidthDp = i;
            Configuration configuration3 = this.mOverrideConfig;
            int i2 = (int) (((float) dm.heightPixels) / dm.density);
            configuration3.screenHeightDp = i2;
            configuration3.compatScreenHeightDp = i2;
            Configuration configuration4 = this.mOverrideConfig;
            int i3 = configuration4.screenWidthDp > this.mOverrideConfig.screenHeightDp ? this.mOverrideConfig.screenHeightDp : this.mOverrideConfig.screenWidthDp;
            configuration4.smallestScreenWidthDp = i3;
            configuration4.compatSmallestScreenWidthDp = i3;
            int sl = Configuration.resetScreenLayout(this.mOverrideConfig.screenLayout);
            if (dm.widthPixels > dm.heightPixels) {
                Configuration configuration5 = this.mOverrideConfig;
                configuration5.orientation = 2;
                configuration5.screenLayout = Configuration.reduceScreenLayout(sl, configuration5.screenWidthDp, this.mOverrideConfig.screenHeightDp);
            } else {
                Configuration configuration6 = this.mOverrideConfig;
                configuration6.orientation = 1;
                configuration6.screenLayout = Configuration.reduceScreenLayout(sl, configuration6.screenHeightDp, this.mOverrideConfig.screenWidthDp);
            }
            if (HwPCUtils.isValidExtDisplayId(displayId)) {
                this.mOverrideConfig.windowConfiguration.setWindowingMode(10);
                this.mOverrideConfig.windowConfiguration.setActivityType(1);
            }
            if (this.isEnableMagic) {
                this.mOverrideConfig.windowConfiguration.setWindowingMode(103);
                this.mOverrideConfig.windowConfiguration.setActivityType(1);
            }
        }
    }

    public Configuration getOverrideConfig() {
        return this.mOverrideConfig;
    }

    public void updateOverrideConfig(Configuration config) {
        if (config != null && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            Configuration configuration = this.mOverrideConfig;
            if (configuration != null && !configuration.equals(config)) {
                this.mOverrideConfig.setTo(config);
                if (this.mOverrideConfig.windowConfiguration.getAppBounds() != null) {
                    this.mOverrideConfig.windowConfiguration.getAppBounds().offsetTo(0, 0);
                }
            } else if (this.mOverrideConfig == null) {
                this.mOverrideConfig = new Configuration(config);
                if (this.mOverrideConfig.windowConfiguration.getAppBounds() != null) {
                    this.mOverrideConfig.windowConfiguration.getAppBounds().offsetTo(0, 0);
                }
            }
        }
    }

    public int getDisplayId() {
        return this.mDisplayId;
    }

    public void setDisplayId(int displayId) {
        this.mDisplayId = displayId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Configuration updateConfig(Configuration config) {
        Configuration configuration;
        if (config != null && (configuration = this.mOverrideConfig) != null && !configuration.equals(Configuration.EMPTY) && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            config = new Configuration(config);
            int configSeq = config.seq;
            config.updateFrom(this.mOverrideConfig);
            if (HwPCUtils.mTouchDeviceID == -1) {
                config.seq = configSeq + 1;
            }
        }
        return config;
    }

    private static void initHwArgs(ActivityThread thread, String[] args) {
        if (HwMwUtils.ENABLED && args != null && args.length == 5) {
            if (args[0] != null && "true".equals(args[0])) {
                Slog.i(TAG, "initHwArgs  set isEnableMagic true");
                thread.isEnableMagic = true;
            }
            if (Boolean.parseBoolean(args[4])) {
                Slog.i(TAG, "initHwArgs  set isDragable true.");
                thread.isDragable = true;
            }
            if (Integer.parseInt(args[3]) == 103) {
                thread.setInitParam(0, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            }
        } else if (HwPCUtils.enabled() && args != null) {
            if (args.length == 3 || args.length == 1 || args.length == 2) {
                try {
                    if (Integer.parseInt(args[0]) > 0) {
                        if (args.length == 1) {
                            thread.setInitParam(Integer.parseInt(args[0]), 0, 0);
                        } else if (args.length == 2) {
                            HwPCUtils.mTouchDeviceID = Integer.parseInt(args[1]);
                            thread.setInitParam(Integer.parseInt(args[0]), 0, 0);
                        } else {
                            thread.setInitParam(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                        }
                        HwPCUtils.setPCDisplayID(Integer.parseInt(args[0]));
                        return;
                    }
                    HwPCUtils.setPCDisplayID(-Integer.parseInt(args[0]));
                } catch (NumberFormatException e) {
                    HwPCUtils.log(TAG, "args format error.");
                }
            }
        }
    }

    private static void initPreloadHandlerThread() {
        if (mPreloadHandlerThread == null) {
            mPreloadHandlerThread = new HandlerThread(QUEUED_WORK_LOOPER, 10);
            mPreloadHandlerThread.start();
        }
    }

    private void finishPreloaded() {
        long preloadStatus = 0;
        if (HwFrameworkFactory.getHwActivityThread() != null) {
            preloadStatus = HwFrameworkFactory.getHwActivityThread().getHwPreloadStatus();
        }
        Slog.i(TAG, "finishPreloaded preloadStatus " + preloadStatus);
        if (preloadStatus == 3) {
            try {
                ActivityManager.getService().finishBindApplication(this.mAppThread);
                if (HwFrameworkFactory.getHwActivityThread() != null) {
                    HwFrameworkFactory.getHwActivityThread().setHwPreloadStatus(0);
                }
                Slog.i(TAG, "finishPreloaded end preloadStatus " + preloadStatus);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    public static IContentSensorManager getContentSensorManager() {
        return sContentSensorManager;
    }

    public static void setContentSensorManager(IContentSensorManager contentSensorManager) {
        sContentSensorManager = contentSensorManager;
    }

    private final void updateNavigationBarColor(Activity activity) {
        if ((SystemProperties.getInt("persist.sys.navigationbar.mode", 0) & 2) != 0) {
            this.mH.removeMessages(1006);
            sendMessage(1006, activity);
        }
    }

    public void handleFreeFormOutLineChanged(IBinder token, int state) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null) {
            r.window.onFreeFormOutLineChanged(state);
        }
    }

    public void handleFreeFormOutLineChangedForFloating(IBinder token, int state) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null) {
            r.window.onFreeFormOutLineChangedForFloating(state);
        }
    }

    public void notifyHwMultiWindowCaption(IBinder token, Bundle info) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null || r.activity == null) {
            Log.d(TAG, "no r or no r.activity!");
            return;
        }
        DecorView decorView = null;
        if (r.activity.mDecor instanceof DecorView) {
            decorView = (DecorView) r.activity.mDecor;
        }
        if (decorView == null) {
            Log.d(TAG, "no decorView!");
            return;
        }
        DecorCaptionView captionView = decorView.getDecorCaptionView();
        if (captionView == null) {
            Log.d(TAG, "no captionView!");
        } else {
            captionView.onNotifyHwMultiWindowCaption(info);
        }
    }

    public void handleRestoreFreeFormConfig(IBinder token) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null) {
            r.window.restoreFreeFormConfig();
        }
    }

    public boolean doReportRuntime(final String procName, final long startTime) {
        if (!sIsMygote || procName == null || startTime <= 0) {
            return false;
        }
        MessageQueue.IdleHandler handler = new MessageQueue.IdleHandler() {
            /* class android.app.ActivityThread.AnonymousClass2 */

            @Override // android.os.MessageQueue.IdleHandler
            public boolean queueIdle() {
                ActivityThread.this.doReportRuntimeByIdleHandler(procName, startTime);
                return false;
            }
        };
        if ("system_server".equals(procName)) {
            BackgroundThread.get().getLooper().getQueue().addIdleHandler(handler);
            return true;
        }
        Looper.myQueue().addIdleHandler(handler);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doReportRuntimeByIdleHandler(String procName, long startTime) {
        Map<String, String> stats = VMDebug.getRuntimeStats();
        if (stats != null && stats.size() != 8) {
            String threadsLocalWaterLine = stats.get("mpl.ref.threads-local-water-line");
            String cyclePattern = stats.get("mpl.mem.cycle-pattern");
            if (threadsLocalWaterLine.length() > 4000) {
                threadsLocalWaterLine = threadsLocalWaterLine.substring(0, 4000);
            }
            if (cyclePattern.length() > 4000) {
                cyclePattern = cyclePattern.substring(0, 4000);
            }
            Bundle data = new Bundle();
            try {
                data.putString(HwFrameworkMonitor.PROC_NAME, procName);
                data.putInt(HwFrameworkMonitor.STAT_DURATION, (int) ((System.currentTimeMillis() - startTime) / 1000));
                data.putInt(HwFrameworkMonitor.CIRCREF_RCYCL_CNT, Integer.valueOf(stats.get("mpl.mem.gc-count")).intValue());
                data.putInt(HwFrameworkMonitor.CIRCREF_RCYCL_MAX_DURATION, Integer.valueOf(stats.get("mpl.mem.gc-max-time")).intValue() / 1000000);
                data.putInt(HwFrameworkMonitor.MEM_LEAK_AVRG, Integer.valueOf(stats.get("mpl.mem.leak-avg")).intValue());
                data.putInt(HwFrameworkMonitor.MEM_LEAK_PEAK, Integer.valueOf(stats.get("mpl.mem.leak-peak")).intValue());
                data.putFloat(HwFrameworkMonitor.MEM_ALLOC_SPACE_UTIL, Float.valueOf(stats.get("mpl.mem.allocation-utilization")).floatValue());
                data.putInt(HwFrameworkMonitor.MEM_ALLOC_ABNORMAL, Integer.valueOf(stats.get("mpl.mem.allocation-abnormal-count")).intValue());
                data.putInt(HwFrameworkMonitor.RC_ABNORMAL, Integer.valueOf(stats.get("mpl.mem.rc-abnormal-count")).intValue());
                data.putInt(HwFrameworkMonitor.GLOBAL_WATER_LINE, Integer.valueOf(stats.get("mpl.ref.global-water-line")).intValue());
                data.putInt(HwFrameworkMonitor.WEAK_WATER_LINE, Integer.valueOf(stats.get("mpl.ref.weak-water-line")).intValue());
                data.putString(HwFrameworkMonitor.THREADS_LOCAL_WATER_LINE, threadsLocalWaterLine);
                data.putInt(HwFrameworkMonitor.NATIVE_TABLE_SIZE, Integer.valueOf(stats.get("mpl.ref.native-table-size")).intValue());
                data.putInt(HwFrameworkMonitor.CONSUM_MPL_FILES, Integer.valueOf(stats.get("mpl.mem.consum-mpl-files")).intValue());
                data.putInt(HwFrameworkMonitor.CONSUM_CLASS_LOCATOR, Integer.valueOf(stats.get("mpl.mem.consum-class-locator")).intValue());
                data.putInt(HwFrameworkMonitor.REFLECT_MANAGE_HEAP, Integer.valueOf(stats.get("mpl.mem.reflect-manage-heap")).intValue());
                data.putInt(HwFrameworkMonitor.GC_MANAGE_HEAP, Integer.valueOf(stats.get("mpl.mem.gc-manage-heap")).intValue());
                data.putString(HwFrameworkMonitor.CYCLE_PATTERN, cyclePattern);
                HwFrameworkMonitor hwFrameworkMonitor = mMonitor;
                if (hwFrameworkMonitor != null && hwFrameworkMonitor.monitor(HwFrameworkMonitor.SCENE_REPORT_RT, data)) {
                    Log.i(TAG, "upload bigdata success for: " + procName);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "upload bigdata decode failed: " + procName);
            }
        }
    }

    private boolean isPackageInfoValid(ApplicationInfo appInfo, LoadedApk packageInfo) {
        if (appInfo.sourceDir == null || appInfo.sourceDir.equals(packageInfo.getAppDir()) || !"com.huawei.health".equals(appInfo.packageName)) {
            return true;
        }
        Slog.w(TAG, "package: " + appInfo.packageName + " appInfo.sourceDir " + appInfo.sourceDir + " packageInfo.AppDir " + packageInfo.getAppDir());
        return false;
    }

    public boolean hasActivities() {
        return !this.mActivities.isEmpty();
    }

    public Rect getActivityStackBounds(Activity activity) {
        ActivityClientRecord activityClientRecord;
        if (activity == null || (activityClientRecord = getActivityClient(activity.getActivityToken())) == null || activityClientRecord.overrideConfig == null) {
            return null;
        }
        return new Rect(activityClientRecord.overrideConfig.windowConfiguration.getBounds());
    }

    @UnsupportedAppUsage
    public static boolean isAdjustConfig(Configuration config) {
        if (isSystem() || !sIsCompatible) {
            return false;
        }
        return HwActivityTaskManager.isAdjustConfig(config);
    }

    @UnsupportedAppUsage
    public static boolean isAdjustPCConfig(Configuration config) {
        if (isSystem() || !sIsCompatible) {
            return false;
        }
        return HwActivityTaskManager.isAdjustPCConfig(config);
    }

    @UnsupportedAppUsage
    public static boolean isAdjustConfigForMagicWindow(Configuration config) {
        if (!HwMwUtils.ENABLED || !HwMwUtils.IS_TABLET || isSystem() || !sIsCompatible || config == null || config.windowConfiguration.getAppBounds() == null || !config.windowConfiguration.inHwMagicWindowingMode()) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    public static boolean isAdjustDisplays(int curDisplayId, int newDisplayId) {
        if (isSystem() || !sIsCompatible || !SystemProperties.getBoolean(HwActivityTaskManager.PAD_CAST_MODE_KEY, false)) {
            return false;
        }
        if (curDisplayId == newDisplayId && newDisplayId == 0) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    private static void adjustGlobalConfigIfNeeded(Configuration config) {
        if (!isSystem() && sIsCompatible) {
            HwActivityTaskManager.adjustGlobalConfigIfNeeded(config);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initCompatibleState(String packageName) {
        List<String> list;
        if (HwActivityTaskManager.IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED) {
            try {
                if (!isSystem() && (list = HwWindowManager.getNotchSystemApps()) != null && !HwPCUtils.PKG_HIMOVIE.equals(packageName)) {
                    sIsCompatible = !list.contains(packageName);
                }
                ApplicationInfo info = getPackageManager().getApplicationInfo(packageName, 128, UserHandle.myUserId());
                if (!(info == null || info.metaData == null || !info.metaData.containsKey("hw.compatible_support"))) {
                    sIsCompatible = info.metaData.getBoolean("hw.compatible_support", true);
                }
            } catch (RemoteException | NullPointerException e) {
                sIsCompatible = false;
                Slog.e(TAG, "getApplicationInfo metadata error");
            }
            Slog.i(TAG, "Init compatible state: " + sIsCompatible);
        }
    }
}
