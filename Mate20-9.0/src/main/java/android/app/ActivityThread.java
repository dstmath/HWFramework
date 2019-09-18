package android.app;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.backup.BackupAgent;
import android.app.job.JobInfo;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ActivityRelaunchItem;
import android.app.servertransaction.ActivityResultItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.PendingTransactionActions;
import android.app.servertransaction.TransactionExecutor;
import android.app.servertransaction.TransactionExecutorHelper;
import android.common.HwActivityThread;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentProto;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ApplicationInfoProto;
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
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.hardware.display.DisplayManagerGlobal;
import android.hwtheme.HwThemeManager;
import android.iawareperf.UniPerf;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Proxy;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.GraphicsEnvironment;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.FontsContract;
import android.renderscript.RenderScriptCacheDir;
import android.scrollerboost.ScrollerBoostManager;
import android.security.NetworkSecurityPolicy;
import android.security.net.config.NetworkSecurityConfigProvider;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.LogException;
import android.util.MergedConfiguration;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.SuperNotCalledException;
import android.util.proto.ProtoOutputStream;
import android.view.Choreographer;
import android.view.ContextThemeWrapper;
import android.view.Display;
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
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.RuntimeInit;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.AbsWindow;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.org.conscrypt.OpenSSLSocketImpl;
import com.android.org.conscrypt.TrustedCertificateStore;
import com.huawei.pgmng.common.Utils;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.CloseGuard;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import libcore.io.DropBox;
import libcore.io.EventLogger;
import libcore.io.IoUtils;
import libcore.net.event.NetworkEventDispatcher;
import org.apache.harmony.dalvik.ddmc.DdmVmInternal;

public final class ActivityThread extends ClientTransactionHandler {
    private static final int ACTIVITY_PROCESS_ARGS = 3;
    private static final int ACTIVITY_THREAD_CHECKIN_VERSION = 4;
    private static final boolean DEBUG_BACKUP = false;
    public static final boolean DEBUG_BROADCAST = DEBUG_HW_BROADCAST;
    public static final boolean DEBUG_CONFIGURATION = DEBUG_HW_ACTIVITY;
    static final boolean DEBUG_HW_ACTIVITY = ams_log_switch.contains(Context.ACTIVITY_SERVICE);
    static final boolean DEBUG_HW_BROADCAST = ams_log_switch.contains("broadcast");
    static final boolean DEBUG_HW_PROVIDER = ams_log_switch.contains("provider");
    static final boolean DEBUG_HW_SERVICE = ams_log_switch.contains(Notification.CATEGORY_SERVICE);
    public static final boolean DEBUG_MEMORY_TRIM = false;
    static final boolean DEBUG_MESSAGES = false;
    public static final boolean DEBUG_ORDER = DEBUG_HW_ACTIVITY;
    private static final boolean DEBUG_PROVIDER = DEBUG_HW_PROVIDER;
    private static final boolean DEBUG_RESULTS = DEBUG_HW_ACTIVITY;
    /* access modifiers changed from: private */
    public static final boolean DEBUG_SERVICE = DEBUG_HW_SERVICE;
    /* access modifiers changed from: private */
    public static long HANDLER_BINDER_DURATION_TIME = JobInfo.MIN_BACKOFF_MILLIS;
    private static final String HEAP_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s";
    private static final String HEAP_FULL_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s %8s %8s %8s";
    public static final long INVALID_PROC_STATE_SEQ = -1;
    static final boolean IS_DEBUG_VERSION = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static final int LOAD_CYCLE_PATTERN_DELAY = 0;
    private static final long MIN_TIME_BETWEEN_GCS = 5000;
    private static final String ONE_COUNT_COLUMN = "%21s %8d";
    private static final String ONE_COUNT_COLUMN_HEADER = "%21s %8s";
    private static final int OTHER_PROCESS_ARGS = 1;
    public static final String PROC_START_SEQ_IDENT = "seq=";
    private static final boolean REPORT_TO_ACTIVITY = true;
    public static final int SERVICE_DONE_EXECUTING_ANON = 0;
    public static final int SERVICE_DONE_EXECUTING_START = 1;
    public static final int SERVICE_DONE_EXECUTING_STOP = 2;
    private static final int SQLITE_MEM_RELEASED_EVENT_LOG_TAG = 75003;
    public static final String TAG = "ActivityThread";
    private static final Bitmap.Config THUMBNAIL_FORMAT = Bitmap.Config.RGB_565;
    private static final int TOUCHSCREEN_PROCESS_ARGS = 2;
    private static final String TWO_COUNT_COLUMNS = "%21s %8d %21s %8d";
    /* access modifiers changed from: private */
    public static boolean USE_CACHE = SystemProperties.getBoolean("persist.sys.freqinfo.cache", true);
    static final String ams_log_switch = SystemProperties.get("ro.config.hw_ams_log", "");
    static final boolean localLOGV = DEBUG_HW_ACTIVITY;
    private static boolean mChangedFont = false;
    /* access modifiers changed from: private */
    public static final Object mPreloadLock = new Object();
    static IContentSensorManager sContentSensorManager = null;
    private static volatile ActivityThread sCurrentActivityThread;
    private static final ThreadLocal<Intent> sCurrentBroadcastIntent = new ThreadLocal<>();
    public static final boolean sIsMygote;
    static volatile Handler sMainThreadHandler;
    static volatile IPackageManager sPackageManager;
    final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
    final ArrayList<Application> mAllApplications = new ArrayList<>();
    final ApplicationThread mAppThread = new ApplicationThread();
    final ArrayMap<String, BackupAgent> mBackupAgents = new ArrayMap<>();
    AppBindData mBoundApplication;
    Configuration mCompatConfiguration;
    Configuration mConfiguration;
    Bundle mCoreSettings = null;
    int mCurDefaultDisplayDpi;
    String mCurrentActivity = null;
    boolean mDensityCompatMode;
    int mDisplayId = 0;
    final Executor mExecutor = new HandlerExecutor(this.mH);
    final GcIdler mGcIdler = new GcIdler();
    boolean mGcIdlerScheduled = false;
    @GuardedBy("mGetProviderLocks")
    final ArrayMap<ProviderKey, Object> mGetProviderLocks = new ArrayMap<>();
    final H mH = new H();
    boolean mHiddenApiWarningShown = false;
    /* access modifiers changed from: private */
    public HwActivityThread mHwActivityThread = HwFrameworkFactory.getHwActivityThread();
    Application mInitialApplication;
    Instrumentation mInstrumentation;
    String mInstrumentationAppDir = null;
    String mInstrumentationLibDir = null;
    String mInstrumentationPackageName = null;
    String[] mInstrumentationSplitAppDirs = null;
    String mInstrumentedAppDir = null;
    String mInstrumentedLibDir = null;
    String[] mInstrumentedSplitAppDirs = null;
    private boolean mIsNeedStartUiProbe = false;
    boolean mJitEnabled = false;
    ArrayList<WeakReference<AssistStructure>> mLastAssistStructures = new ArrayList<>();
    private int mLastSessionId;
    final ArrayMap<IBinder, ProviderClientRecord> mLocalProviders = new ArrayMap<>();
    final ArrayMap<ComponentName, ProviderClientRecord> mLocalProvidersByName = new ArrayMap<>();
    final Looper mLooper = Looper.myLooper();
    private Configuration mMainThreadConfig = new Configuration();
    /* access modifiers changed from: private */
    @GuardedBy("mNetworkPolicyLock")
    public long mNetworkBlockSeq = -1;
    /* access modifiers changed from: private */
    public final Object mNetworkPolicyLock = new Object();
    ActivityClientRecord mNewActivities = null;
    int mNumVisibleActivities = 0;
    final ArrayMap<Activity, ArrayList<OnActivityPausedListener>> mOnPauseListeners = new ArrayMap<>();
    Configuration mOverrideConfig = null;
    @GuardedBy("mResourcesManager")
    final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<>();
    @GuardedBy("mResourcesManager")
    Configuration mPendingConfiguration = null;
    /* access modifiers changed from: private */
    public PreloadThreadHandler mPreloadHandler;
    /* access modifiers changed from: private */
    public HandlerThread mPreloadHandlerThread;
    Profiler mProfiler;
    final ArrayMap<ProviderKey, ProviderClientRecord> mProviderMap = new ArrayMap<>();
    final ArrayMap<IBinder, ProviderRefCount> mProviderRefCountMap = new ArrayMap<>();
    @GuardedBy("mResourcesManager")
    final ArrayList<ActivityClientRecord> mRelaunchingActivities = new ArrayList<>();
    @GuardedBy("mResourcesManager")
    final ArrayMap<String, WeakReference<LoadedApk>> mResourcePackages = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final ResourcesManager mResourcesManager = ResourcesManager.getInstance();
    final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
    boolean mSomeActivitiesChanged = false;
    /* access modifiers changed from: private */
    public long mStartTime;
    private volatile ContextImpl mSystemContext;
    boolean mSystemThread = false;
    private volatile ContextImpl mSystemUiContext;
    /* access modifiers changed from: private */
    public final TransactionExecutor mTransactionExecutor = new TransactionExecutor(this);
    boolean mUpdatingSystemConfig = false;
    private IZrHung mZrHungAppEyeUiProbe = HwFrameworkFactory.getZrHung("appeye_uiprobe");

    public static final class ActivityClientRecord {
        Activity activity;
        ActivityInfo activityInfo;
        CompatibilityInfo compatInfo;
        ViewRootImpl.ActivityConfigCallback configCallback;
        Configuration createdConfig;
        String embeddedID;
        boolean hideForNow;
        int ident;
        Intent intent;
        public final boolean isForward;
        Activity.NonConfigurationInstances lastNonConfigurationInstances;
        private int mLifecycleState;
        Window mPendingRemoveWindow;
        WindowManager mPendingRemoveWindowManager;
        boolean mPreserveWindow;
        Configuration newConfig;
        ActivityClientRecord nextIdle;
        Configuration overrideConfig;
        public LoadedApk packageInfo;
        Activity parent;
        boolean paused;
        int pendingConfigChanges;
        List<ReferrerIntent> pendingIntents;
        List<ResultInfo> pendingResults;
        PersistableBundle persistentState;
        ProfilerInfo profilerInfo;
        String referrer;
        boolean startsNotResumed;
        Bundle state;
        boolean stopped;
        /* access modifiers changed from: private */
        public Configuration tmpConfig;
        public IBinder token;
        IVoiceInteractor voiceInteractor;
        Window window;

        @VisibleForTesting
        public ActivityClientRecord() {
            this.tmpConfig = new Configuration();
            this.mLifecycleState = 0;
            this.isForward = false;
            init();
        }

        public ActivityClientRecord(IBinder token2, Intent intent2, int ident2, ActivityInfo info, Configuration overrideConfig2, CompatibilityInfo compatInfo2, String referrer2, IVoiceInteractor voiceInteractor2, Bundle state2, PersistableBundle persistentState2, List<ResultInfo> pendingResults2, List<ReferrerIntent> pendingNewIntents, boolean isForward2, ProfilerInfo profilerInfo2, ClientTransactionHandler client) {
            CompatibilityInfo compatibilityInfo = compatInfo2;
            this.tmpConfig = new Configuration();
            this.mLifecycleState = 0;
            this.token = token2;
            this.ident = ident2;
            this.intent = intent2;
            this.referrer = referrer2;
            this.voiceInteractor = voiceInteractor2;
            this.activityInfo = info;
            this.compatInfo = compatibilityInfo;
            this.state = state2;
            this.persistentState = persistentState2;
            this.pendingResults = pendingResults2;
            this.pendingIntents = pendingNewIntents;
            this.isForward = isForward2;
            this.profilerInfo = profilerInfo2;
            this.overrideConfig = overrideConfig2;
            this.packageInfo = client.getPackageInfoNoCheck(this.activityInfo.applicationInfo, compatibilityInfo);
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
                public final void onConfigurationChanged(Configuration configuration, int i) {
                    ActivityThread.ActivityClientRecord.lambda$init$0(ActivityThread.ActivityClientRecord.this, configuration, i);
                }
            };
        }

        public static /* synthetic */ void lambda$init$0(ActivityClientRecord activityClientRecord, Configuration overrideConfig2, int newDisplayId) {
            if (activityClientRecord.activity != null) {
                if (activityClientRecord.activity.getPackageName() != null && activityClientRecord.activity.getPackageName().contains("launcher") && activityClientRecord.activity.getLocalClassName() != null && (activityClientRecord.activity.getLocalClassName().contains("UniHomeLauncher") || activityClientRecord.activity.getLocalClassName().contains("DrawerLauncher") || activityClientRecord.activity.getLocalClassName().contains("NewSimpleLauncher"))) {
                    Display display = activityClientRecord.activity.getDisplay();
                    if (display != null && ((overrideConfig2.orientation == 1 && display.getHeight() < display.getWidth()) || (overrideConfig2.orientation == 2 && display.getHeight() > display.getWidth()))) {
                        Log.d(ActivityThread.TAG, "abnormal configuration for launcher " + overrideConfig2.orientation + " " + display.getHeight() + " " + display.getWidth());
                        return;
                    }
                }
                activityClientRecord.activity.mMainThread.handleActivityConfigurationChanged(activityClientRecord.token, overrideConfig2, newDisplayId);
                return;
            }
            throw new IllegalStateException("Received config update for non-existing activity");
        }

        public int getLifecycleState() {
            return this.mLifecycleState;
        }

        public void setState(int newLifecycleState) {
            this.mLifecycleState = newLifecycleState;
            switch (this.mLifecycleState) {
                case 1:
                    this.paused = true;
                    this.stopped = true;
                    return;
                case 2:
                    this.paused = true;
                    this.stopped = false;
                    return;
                case 3:
                    this.paused = false;
                    this.stopped = false;
                    return;
                case 4:
                    this.paused = true;
                    this.stopped = false;
                    return;
                case 5:
                    this.paused = true;
                    this.stopped = true;
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: private */
        public boolean isPreHoneycomb() {
            return this.activity != null && this.activity.getApplicationInfo().targetSdkVersion < 11;
        }

        /* access modifiers changed from: private */
        public boolean isPreP() {
            return this.activity != null && this.activity.getApplicationInfo().targetSdkVersion < 28;
        }

        public boolean isPersistable() {
            return this.activityInfo.persistableMode == 2;
        }

        public boolean isVisibleFromServer() {
            return this.activity != null && this.activity.mVisibleFromServer;
        }

        public String toString() {
            ComponentName componentName = this.intent != null ? this.intent.getComponent() : null;
            StringBuilder sb = new StringBuilder();
            sb.append("ActivityRecord{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" token=");
            sb.append(this.token);
            sb.append(" ");
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
                sb.append(", temporaryPause=");
                sb.append(this.activity.mTemporaryPause);
                sb.append(", changingConfigurations=");
                sb.append(this.activity.mChangingConfigurations);
                sb.append("}");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    static final class AppBindData {
        ApplicationInfo appInfo;
        boolean autofillCompatibilityEnabled;
        String buildSerial;
        CompatibilityInfo compatInfo;
        Configuration config;
        int debugMode;
        boolean enableBinderTracking;
        LoadedApk info;
        ProfilerInfo initProfilerInfo;
        Bundle instrumentationArgs;
        ComponentName instrumentationName;
        IUiAutomationConnection instrumentationUiAutomationConnection;
        IInstrumentationWatcher instrumentationWatcher;
        boolean persistent;
        String processName;
        List<ProviderInfo> providers;
        boolean restrictedBackupMode;
        boolean trackAllocation;

        AppBindData() {
        }

        public String toString() {
            return "AppBindData{appInfo=" + this.appInfo + "}";
        }
    }

    private class ApplicationThread extends IApplicationThread.Stub {
        private static final String DB_INFO_FORMAT = "  %8s %8s %14s %14s  %s";
        private int mLastProcessState;

        private ApplicationThread() {
            this.mLastProcessState = -1;
        }

        /* access modifiers changed from: private */
        public void updatePendingConfiguration(Configuration config) {
            synchronized (ActivityThread.this.mResourcesManager) {
                if (ActivityThread.this.mPendingConfiguration == null || ActivityThread.this.mPendingConfiguration.isOtherSeqNewer(config)) {
                    ActivityThread.this.mPendingConfiguration = config;
                }
            }
        }

        public final void scheduleSleeping(IBinder token, boolean sleeping) {
            ActivityThread.this.sendMessage(137, token, sleeping);
        }

        public final void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState) {
            updateProcessState(processState, false);
            ReceiverData receiverData = new ReceiverData(intent, resultCode, data, extras, sync, false, ActivityThread.this.mAppThread.asBinder(), sendingUser);
            receiverData.info = info;
            receiverData.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(113, receiverData);
        }

        public final void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            d.backupMode = backupMode;
            ActivityThread.this.sendMessage(128, d);
        }

        public final void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(129, d);
        }

        public final void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) {
            updateProcessState(processState, false);
            CreateServiceData s = new CreateServiceData();
            s.token = token;
            s.info = info;
            s.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(114, s);
        }

        public final void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) {
            updateProcessState(processState, false);
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            s.rebind = rebind;
            if (ActivityThread.DEBUG_SERVICE) {
                Slog.v(ActivityThread.TAG, "scheduleBindService token=" + token + " intent=" + intent + " uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
            }
            ActivityThread.this.sendMessage(121, s);
        }

        public final void scheduleUnbindService(IBinder token, Intent intent) {
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            ActivityThread.this.sendMessage(122, s);
        }

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

        public final void scheduleStopService(IBinder token) {
            ActivityThread.this.sendMessage(116, token);
        }

        public final void bindApplication(String processName, ApplicationInfo appInfo, List<ProviderInfo> providers, ComponentName instrumentationName, ProfilerInfo profilerInfo, Bundle instrumentationArgs, IInstrumentationWatcher instrumentationWatcher, IUiAutomationConnection instrumentationUiConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean isRestrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial, boolean autofillCompatibilityEnabled) {
            String str = processName;
            if (Jlog.isPerfTest()) {
                Jlog.i(3035, Jlog.getMessage(ActivityThread.TAG, "bindApplication", "pid=" + Process.myPid() + "&processname=" + str));
            }
            if (services != null) {
                ServiceManager.initServiceCache(services);
            }
            setCoreSettings(coreSettings);
            AppBindData data = new AppBindData();
            data.processName = str;
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
            Configuration config2 = ActivityThread.this.updateConfig(config);
            data.config = config2;
            data.compatInfo = compatInfo;
            Configuration configuration = config2;
            data.initProfilerInfo = profilerInfo;
            data.buildSerial = buildSerial;
            data.autofillCompatibilityEnabled = autofillCompatibilityEnabled;
            ActivityThread.this.sendMessage(110, data);
        }

        public final void runIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = entryPoint;
            args.arg2 = entryPointArgs;
            ActivityThread.this.sendMessage(158, args);
        }

        public final void scheduleExit() {
            ActivityThread.this.sendMessage(111, null);
        }

        public final void scheduleSuicide() {
            ActivityThread.this.sendMessage(130, null);
        }

        public void scheduleApplicationInfoChanged(ApplicationInfo ai) {
            scheduleApplicationThemeInfoChanged(ai, false);
        }

        public void scheduleApplicationThemeInfoChanged(ApplicationInfo ai, boolean fromThemeChange) {
            ActivityThread.this.sendMessage(156, ai, fromThemeChange);
        }

        public void updateTimeZone() {
            TimeZone.setDefault(null);
        }

        public void clearDnsCache() {
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        }

        public void setHttpProxy(String host, String port, String exclList, Uri pacFileUrl) {
            ConnectivityManager cm = ConnectivityManager.from(ActivityThread.this.getApplication() != null ? ActivityThread.this.getApplication() : ActivityThread.this.getSystemContext());
            if (cm == null || cm.getBoundNetworkForProcess() == null) {
                Proxy.setHttpProxySystemProperty(host, port, exclList, pacFileUrl);
            } else {
                Proxy.setHttpProxySystemProperty(cm.getDefaultProxy());
            }
        }

        public void processInBackground() {
            ActivityThread.this.mH.removeMessages(120);
            ActivityThread.this.mH.sendMessage(ActivityThread.this.mH.obtainMessage(120));
        }

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

        public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String dataStr, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
            updateProcessState(processState, false);
            receiver.performReceive(intent, resultCode, dataStr, extras, ordered, sticky, sendingUser);
        }

        public void scheduleLowMemory() {
            ActivityThread.this.sendMessage(124, null);
        }

        public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
            ActivityThread.this.sendMessage(127, profilerInfo, start, profileType);
        }

        public void dumpHeap(boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd) {
            DumpHeapData dhd = new DumpHeapData();
            dhd.managed = managed;
            dhd.mallocInfo = mallocInfo;
            dhd.runGc = runGc;
            dhd.path = path;
            dhd.fd = fd;
            ActivityThread.this.sendMessage(135, (Object) dhd, 0, 0, true);
        }

        public void attachAgent(String agent) {
            ActivityThread.this.sendMessage(155, agent);
        }

        public void setSchedulingGroup(int group) {
            try {
                Process.setProcessGroup(Process.myPid(), group);
            } catch (Exception e) {
                Slog.w(ActivityThread.TAG, "Failed setting process group to " + group, e);
            }
        }

        public void dispatchPackageBroadcast(int cmd, String[] packages) {
            ActivityThread.this.sendMessage(133, packages, cmd);
        }

        public void scheduleCrash(String msg) {
            ActivityThread.this.sendMessage(134, msg);
        }

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

        public void dumpMemInfo(ParcelFileDescriptor pfd, Debug.MemoryInfo mem, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) {
            FileOutputStream fout = new FileOutputStream(pfd.getFileDescriptor());
            FastPrintWriter fastPrintWriter = new FastPrintWriter(fout);
            try {
                dumpMemInfo(fastPrintWriter, mem, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable);
                fastPrintWriter.flush();
                try {
                    fout.close();
                } catch (IOException e) {
                    IOException iOException = e;
                    Slog.w(ActivityThread.TAG, "Unable to close fout ");
                }
                fastPrintWriter.close();
                IoUtils.closeQuietly(pfd);
            } catch (Throwable th) {
                th = th;
                fastPrintWriter.flush();
                try {
                    fout.close();
                } catch (IOException e2) {
                    th = e2;
                    IOException iOException2 = th;
                    Slog.w(ActivityThread.TAG, "Unable to close fout ");
                }
                fastPrintWriter.close();
                IoUtils.closeQuietly(pfd);
                throw th;
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v0, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v1, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v2, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v3, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v4, resolved type: int} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void dumpMemInfo(PrintWriter pw, Debug.MemoryInfo memInfo, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable) {
            PrintWriter printWriter = pw;
            long nativeMax = Debug.getNativeHeapSize() / 1024;
            long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024;
            long nativeFree = Debug.getNativeHeapFreeSize() / 1024;
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long dalvikMax = runtime.totalMemory() / 1024;
            long dalvikFree = runtime.freeMemory() / 1024;
            long dalvikAllocated = dalvikMax - dalvikFree;
            int i = 0;
            Class[] classesToCount = {ContextImpl.class, Activity.class, WebView.class, OpenSSLSocketImpl.class};
            long[] instanceCounts = VMDebug.countInstancesOfClasses(classesToCount, true);
            long appContextInstanceCount = instanceCounts[0];
            long activityInstanceCount = instanceCounts[1];
            long webviewInstanceCount = instanceCounts[2];
            long openSslSocketCount = instanceCounts[3];
            Runtime runtime2 = runtime;
            long viewInstanceCount = ViewDebug.getViewInstanceCount();
            long viewRootInstanceCount = ViewDebug.getViewRootImplCount();
            int globalAssetCount = AssetManager.getGlobalAssetCount();
            long viewRootInstanceCount2 = viewRootInstanceCount;
            int globalAssetManagerCount = AssetManager.getGlobalAssetManagerCount();
            int binderLocalObjectCount = Debug.getBinderLocalObjectCount();
            int globalAssetManagerCount2 = globalAssetManagerCount;
            int binderProxyObjectCount = Debug.getBinderProxyObjectCount();
            int binderDeathObjectCount = Debug.getBinderDeathObjectCount();
            long parcelSize = Parcel.getGlobalAllocSize();
            int binderDeathObjectCount2 = binderDeathObjectCount;
            int binderLocalObjectCount2 = binderLocalObjectCount;
            long parcelCount = Parcel.getGlobalAllocCount();
            SQLiteDebug.PagerStats stats = SQLiteDebug.getDatabaseInfo();
            int myPid = Process.myPid();
            String str = ActivityThread.this.mBoundApplication != null ? ActivityThread.this.mBoundApplication.processName : "unknown";
            long openSslSocketCount2 = openSslSocketCount;
            long webviewInstanceCount2 = webviewInstanceCount;
            long activityInstanceCount2 = activityInstanceCount;
            long appContextInstanceCount2 = appContextInstanceCount;
            Class[] clsArr = classesToCount;
            SQLiteDebug.PagerStats stats2 = stats;
            long viewInstanceCount2 = viewInstanceCount;
            long viewRootInstanceCount3 = viewRootInstanceCount2;
            int globalAssetManagerCount3 = globalAssetManagerCount2;
            int binderProxyObjectCount2 = binderProxyObjectCount;
            int binderLocalObjectCount3 = binderLocalObjectCount2;
            int binderDeathObjectCount3 = binderDeathObjectCount2;
            long parcelCount2 = parcelCount;
            int globalAssetCount2 = globalAssetCount;
            PrintWriter printWriter2 = printWriter;
            ActivityThread.dumpMemInfoTable(printWriter, memInfo, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, myPid, str, nativeMax, nativeAllocated, nativeFree, dalvikMax, dalvikAllocated, dalvikFree);
            if (checkin) {
                printWriter2.print(viewInstanceCount2);
                printWriter2.print(',');
                printWriter2.print(viewRootInstanceCount3);
                printWriter2.print(',');
                printWriter2.print(appContextInstanceCount2);
                printWriter2.print(',');
                printWriter2.print(activityInstanceCount2);
                printWriter2.print(',');
                printWriter2.print(globalAssetCount2);
                printWriter2.print(',');
                int globalAssetManagerCount4 = globalAssetManagerCount3;
                printWriter2.print(globalAssetManagerCount4);
                printWriter2.print(',');
                int binderLocalObjectCount4 = binderLocalObjectCount3;
                printWriter2.print(binderLocalObjectCount4);
                printWriter2.print(',');
                int binderProxyObjectCount3 = binderProxyObjectCount2;
                printWriter2.print(binderProxyObjectCount3);
                printWriter2.print(',');
                int binderDeathObjectCount4 = binderDeathObjectCount3;
                printWriter2.print(binderDeathObjectCount4);
                printWriter2.print(',');
                int i2 = binderProxyObjectCount3;
                int i3 = binderDeathObjectCount4;
                long openSslSocketCount3 = openSslSocketCount2;
                printWriter2.print(openSslSocketCount3);
                printWriter2.print(',');
                SQLiteDebug.PagerStats stats3 = stats2;
                printWriter2.print(stats3.memoryUsed / 1024);
                printWriter2.print(',');
                printWriter2.print(stats3.memoryUsed / 1024);
                printWriter2.print(',');
                printWriter2.print(stats3.pageCacheOverflow / 1024);
                printWriter2.print(',');
                printWriter2.print(stats3.largestMemAlloc / 1024);
                while (true) {
                    int i4 = i;
                    long openSslSocketCount4 = openSslSocketCount3;
                    if (i4 < stats3.dbStats.size()) {
                        SQLiteDebug.DbStats dbStats = stats3.dbStats.get(i4);
                        printWriter2.print(',');
                        printWriter2.print(dbStats.dbName);
                        printWriter2.print(',');
                        printWriter2.print(dbStats.pageSize);
                        printWriter2.print(',');
                        printWriter2.print(dbStats.dbSize);
                        printWriter2.print(',');
                        printWriter2.print(dbStats.lookaside);
                        printWriter2.print(',');
                        printWriter2.print(dbStats.cache);
                        printWriter2.print(',');
                        printWriter2.print(dbStats.cache);
                        i = i4 + 1;
                        openSslSocketCount3 = openSslSocketCount4;
                        globalAssetManagerCount4 = globalAssetManagerCount4;
                        binderLocalObjectCount4 = binderLocalObjectCount4;
                    } else {
                        int i5 = binderLocalObjectCount4;
                        pw.println();
                        return;
                    }
                }
            } else {
                long viewInstanceCount3 = viewInstanceCount2;
                long viewRootInstanceCount4 = viewRootInstanceCount3;
                SQLiteDebug.PagerStats stats4 = stats2;
                printWriter2.println(" ");
                printWriter2.println(" Objects");
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "Views:", Long.valueOf(viewInstanceCount3), "ViewRootImpl:", Long.valueOf(viewRootInstanceCount4));
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "AppContexts:", Long.valueOf(appContextInstanceCount2), "Activities:", Long.valueOf(activityInstanceCount2));
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "Assets:", Integer.valueOf(globalAssetCount2), "AssetManagers:", Integer.valueOf(globalAssetManagerCount3));
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "Local Binders:", Integer.valueOf(binderLocalObjectCount3), "Proxy Binders:", Integer.valueOf(binderProxyObjectCount2));
                long j = viewInstanceCount3;
                long j2 = viewRootInstanceCount4;
                long parcelCount3 = parcelCount2;
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "Parcel memory:", Long.valueOf(parcelSize / 1024), "Parcel count:", Long.valueOf(parcelCount3));
                int binderDeathObjectCount5 = binderDeathObjectCount3;
                int i6 = binderDeathObjectCount5;
                long j3 = parcelCount3;
                long parcelCount4 = openSslSocketCount2;
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "Death Recipients:", Integer.valueOf(binderDeathObjectCount5), "OpenSSL Sockets:", Long.valueOf(parcelCount4));
                long j4 = parcelCount4;
                ActivityThread.printRow(printWriter2, ActivityThread.ONE_COUNT_COLUMN, "WebViews:", Long.valueOf(webviewInstanceCount2));
                printWriter2.println(" ");
                printWriter2.println(" SQL");
                ActivityThread.printRow(printWriter2, ActivityThread.ONE_COUNT_COLUMN, "MEMORY_USED:", Integer.valueOf(stats4.memoryUsed / 1024));
                ActivityThread.printRow(printWriter2, ActivityThread.TWO_COUNT_COLUMNS, "PAGECACHE_OVERFLOW:", Integer.valueOf(stats4.pageCacheOverflow / 1024), "MALLOC_SIZE:", Integer.valueOf(stats4.largestMemAlloc / 1024));
                printWriter2.println(" ");
                int N = stats4.dbStats.size();
                if (N > 0) {
                    printWriter2.println(" DATABASES");
                    int i7 = 5;
                    ActivityThread.printRow(printWriter2, DB_INFO_FORMAT, "pgsz", "dbsz", "Lookaside(b)", "cache", "Dbname");
                    int i8 = 0;
                    while (i8 < N) {
                        SQLiteDebug.DbStats dbStats2 = stats4.dbStats.get(i8);
                        int N2 = N;
                        SQLiteDebug.PagerStats stats5 = stats4;
                        Object[] objArr = new Object[i7];
                        objArr[0] = dbStats2.pageSize > 0 ? String.valueOf(dbStats2.pageSize) : " ";
                        objArr[1] = dbStats2.dbSize > 0 ? String.valueOf(dbStats2.dbSize) : " ";
                        objArr[2] = dbStats2.lookaside > 0 ? String.valueOf(dbStats2.lookaside) : " ";
                        objArr[3] = dbStats2.cache;
                        objArr[4] = dbStats2.dbName;
                        ActivityThread.printRow(printWriter2, DB_INFO_FORMAT, objArr);
                        i8++;
                        N = N2;
                        stats4 = stats5;
                        i7 = 5;
                    }
                }
                SQLiteDebug.PagerStats pagerStats = stats4;
                String assetAlloc = AssetManager.getAssetAllocations();
                if (assetAlloc != null) {
                    printWriter2.println(" ");
                    printWriter2.println(" Asset Allocations");
                    printWriter2.print(assetAlloc);
                }
                if (dumpUnreachable) {
                    if (!(ActivityThread.this.mBoundApplication == null || (2 & ActivityThread.this.mBoundApplication.appInfo.flags) == 0) || Build.IS_DEBUGGABLE) {
                        i = 1;
                    }
                    printWriter2.println(" ");
                    printWriter2.println(" Unreachable memory");
                    printWriter2.print(Debug.getUnreachableMemory(100, i));
                }
            }
        }

        public void dumpMemInfoProto(ParcelFileDescriptor pfd, Debug.MemoryInfo mem, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) {
            ProtoOutputStream proto = new ProtoOutputStream(pfd.getFileDescriptor());
            try {
                dumpMemInfo(proto, mem, dumpFullInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable);
                proto.flush();
                IoUtils.closeQuietly(pfd);
            } catch (Throwable th) {
                proto.flush();
                IoUtils.closeQuietly(pfd);
                throw th;
            }
        }

        private void dumpMemInfo(ProtoOutputStream proto, Debug.MemoryInfo memInfo, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable) {
            ProtoOutputStream protoOutputStream = proto;
            long nativeMax = Debug.getNativeHeapSize() / 1024;
            long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024;
            long nativeFree = Debug.getNativeHeapFreeSize() / 1024;
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long dalvikMax = runtime.totalMemory() / 1024;
            long dalvikFree = runtime.freeMemory() / 1024;
            long dalvikAllocated = dalvikMax - dalvikFree;
            boolean showContents = false;
            Class[] classesToCount = {ContextImpl.class, Activity.class, WebView.class, OpenSSLSocketImpl.class};
            long[] instanceCounts = VMDebug.countInstancesOfClasses(classesToCount, true);
            long appContextInstanceCount = instanceCounts[0];
            long activityInstanceCount = instanceCounts[1];
            long webviewInstanceCount = instanceCounts[2];
            long openSslSocketCount = instanceCounts[3];
            long viewInstanceCount = ViewDebug.getViewInstanceCount();
            Runtime runtime2 = runtime;
            long viewRootInstanceCount = ViewDebug.getViewRootImplCount();
            int globalAssetCount = AssetManager.getGlobalAssetCount();
            long viewRootInstanceCount2 = viewRootInstanceCount;
            int globalAssetManagerCount = AssetManager.getGlobalAssetManagerCount();
            int binderLocalObjectCount = Debug.getBinderLocalObjectCount();
            int globalAssetManagerCount2 = globalAssetManagerCount;
            int binderProxyObjectCount = Debug.getBinderProxyObjectCount();
            int binderDeathObjectCount = Debug.getBinderDeathObjectCount();
            long parcelSize = Parcel.getGlobalAllocSize();
            int binderDeathObjectCount2 = binderDeathObjectCount;
            int binderLocalObjectCount2 = binderLocalObjectCount;
            long parcelCount = Parcel.getGlobalAllocCount();
            SQLiteDebug.PagerStats stats = SQLiteDebug.getDatabaseInfo();
            long viewInstanceCount2 = viewInstanceCount;
            long mToken = protoOutputStream.start(1146756268033L);
            Class[] classesToCount2 = classesToCount;
            int globalAssetCount2 = globalAssetCount;
            protoOutputStream.write(1120986464257L, Process.myPid());
            protoOutputStream.write(1138166333442L, ActivityThread.this.mBoundApplication != null ? ActivityThread.this.mBoundApplication.processName : "unknown");
            long openSslSocketCount2 = openSslSocketCount;
            long webviewInstanceCount2 = webviewInstanceCount;
            long viewInstanceCount3 = viewInstanceCount2;
            Class[] clsArr = classesToCount2;
            ProtoOutputStream protoOutputStream2 = protoOutputStream;
            ActivityThread.dumpMemInfoTable(protoOutputStream, memInfo, dumpDalvik, dumpSummaryOnly, nativeMax, nativeAllocated, nativeFree, dalvikMax, dalvikAllocated, dalvikFree);
            long mToken2 = mToken;
            protoOutputStream2.end(mToken2);
            long oToken = protoOutputStream2.start(1146756268034L);
            long viewInstanceCount4 = viewInstanceCount3;
            protoOutputStream2.write(1120986464257L, viewInstanceCount4);
            protoOutputStream2.write(1120986464258L, viewRootInstanceCount2);
            long appContextInstanceCount2 = appContextInstanceCount;
            protoOutputStream2.write(1120986464259L, appContextInstanceCount2);
            long activityInstanceCount2 = activityInstanceCount;
            protoOutputStream2.write(1120986464260L, activityInstanceCount2);
            int globalAssetCount3 = globalAssetCount2;
            protoOutputStream2.write(1120986464261L, globalAssetCount3);
            long j = mToken2;
            int globalAssetManagerCount3 = globalAssetManagerCount2;
            protoOutputStream2.write(1120986464262L, globalAssetManagerCount3);
            int binderLocalObjectCount3 = binderLocalObjectCount2;
            protoOutputStream2.write(1120986464263L, binderLocalObjectCount3);
            int i = globalAssetManagerCount3;
            int globalAssetManagerCount4 = binderProxyObjectCount;
            protoOutputStream2.write(1120986464264L, globalAssetManagerCount4);
            int i2 = globalAssetManagerCount4;
            int i3 = binderLocalObjectCount3;
            protoOutputStream2.write(1112396529673L, parcelSize / 1024);
            protoOutputStream2.write(1120986464266L, parcelCount);
            long j2 = viewInstanceCount4;
            int binderDeathObjectCount3 = binderDeathObjectCount2;
            protoOutputStream2.write(1120986464267L, binderDeathObjectCount3);
            int i4 = binderDeathObjectCount3;
            long openSslSocketCount3 = openSslSocketCount2;
            protoOutputStream2.write(ApplicationInfoProto.Detail.DESCRIPTION_RES, openSslSocketCount3);
            long j3 = openSslSocketCount3;
            long webviewInstanceCount3 = webviewInstanceCount2;
            protoOutputStream2.write(ApplicationInfoProto.Detail.UI_OPTIONS, webviewInstanceCount3);
            protoOutputStream2.end(oToken);
            long sToken = protoOutputStream2.start(1146756268035L);
            long j4 = oToken;
            SQLiteDebug.PagerStats stats2 = stats;
            long j5 = webviewInstanceCount3;
            protoOutputStream2.write(1120986464257L, stats2.memoryUsed / 1024);
            protoOutputStream2.write(1120986464258L, stats2.pageCacheOverflow / 1024);
            protoOutputStream2.write(1120986464259L, stats2.largestMemAlloc / 1024);
            int n = stats2.dbStats.size();
            int i5 = 0;
            while (true) {
                long activityInstanceCount3 = activityInstanceCount2;
                if (i5 >= n) {
                    break;
                }
                SQLiteDebug.DbStats dbStats = stats2.dbStats.get(i5);
                long dToken = protoOutputStream2.start(2246267895812L);
                protoOutputStream2.write(1138166333441L, dbStats.dbName);
                protoOutputStream2.write(1120986464258L, dbStats.pageSize);
                protoOutputStream2.write(1120986464259L, dbStats.dbSize);
                protoOutputStream2.write(1120986464260L, dbStats.lookaside);
                protoOutputStream2.write(1138166333445L, dbStats.cache);
                protoOutputStream2.end(dToken);
                i5++;
                activityInstanceCount2 = activityInstanceCount3;
                stats2 = stats2;
                n = n;
                appContextInstanceCount2 = appContextInstanceCount2;
            }
            int i6 = n;
            long j6 = appContextInstanceCount2;
            protoOutputStream2.end(sToken);
            String assetAlloc = AssetManager.getAssetAllocations();
            if (assetAlloc != null) {
                protoOutputStream2.write(1138166333444L, assetAlloc);
            }
            if (dumpUnreachable) {
                int i7 = globalAssetCount3;
                if (((ActivityThread.this.mBoundApplication == null ? 0 : ActivityThread.this.mBoundApplication.appInfo.flags) & 2) != 0 || Build.IS_DEBUGGABLE) {
                    showContents = true;
                }
                long j7 = sToken;
                protoOutputStream2.write(1138166333445L, Debug.getUnreachableMemory(100, showContents));
                return;
            }
            int i8 = globalAssetCount3;
        }

        public void dumpGfxInfo(ParcelFileDescriptor pfd, String[] args) {
            ActivityThread.this.nDumpGraphicsInfo(pfd.getFileDescriptor());
            WindowManagerGlobal.getInstance().dumpGfxInfo(pfd.getFileDescriptor(), args);
            IoUtils.closeQuietly(pfd);
        }

        /* access modifiers changed from: private */
        public void dumpDatabaseInfo(ParcelFileDescriptor pfd, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(pfd.getFileDescriptor()));
            SQLiteDebug.dump(new PrintWriterPrinter(pw), args);
            pw.flush();
        }

        public void dumpDbInfo(ParcelFileDescriptor pfd, final String[] args) {
            if (ActivityThread.this.mSystemThread) {
                try {
                    final ParcelFileDescriptor dup = pfd.dup();
                    IoUtils.closeQuietly(pfd);
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        public void run() {
                            try {
                                ApplicationThread.this.dumpDatabaseInfo(dup, args);
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
                dumpDatabaseInfo(pfd, args);
                IoUtils.closeQuietly(pfd);
            }
        }

        public void unstableProviderDied(IBinder provider) {
            ActivityThread.this.sendMessage(142, provider);
        }

        public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) {
            RequestAssistContextExtras cmd = new RequestAssistContextExtras();
            cmd.activityToken = activityToken;
            cmd.requestToken = requestToken;
            cmd.requestType = requestType;
            cmd.sessionId = sessionId;
            cmd.flags = flags;
            ActivityThread.this.sendMessage(143, cmd);
        }

        public void setCoreSettings(Bundle coreSettings) {
            ActivityThread.this.sendMessage(138, coreSettings);
        }

        public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) {
            UpdateCompatibilityData ucd = new UpdateCompatibilityData();
            ucd.pkg = pkg;
            ucd.info = info;
            ActivityThread.this.sendMessage(139, ucd);
        }

        public void scheduleTrimMemory(int level) {
            iawareTrimMemory(level, false);
        }

        public void iawareTrimMemory(int level, boolean fromIAware) {
            Runnable r = PooledLambda.obtainRunnable($$Lambda$ActivityThread$ApplicationThread$eNIzQZ974tdrS8H1o1fp2sJZxk.INSTANCE, ActivityThread.this, Integer.valueOf(level), Boolean.valueOf(fromIAware));
            Choreographer choreographer = Choreographer.getMainThreadInstance();
            if (choreographer != null) {
                choreographer.postCallback(3, r, null);
            } else {
                ActivityThread.this.mH.post(r);
            }
        }

        public void scheduleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
            ActivityThread.this.sendMessage(144, token, drawComplete);
        }

        public void scheduleOnNewActivityOptions(IBinder token, Bundle options) {
            ActivityThread.this.sendMessage(146, new Pair(token, ActivityOptions.fromBundle(options)));
        }

        public void setProcessState(int state) {
            updateProcessState(state, true);
        }

        public void updateProcessState(int processState, boolean fromIpc) {
            synchronized (this) {
                if (this.mLastProcessState != processState) {
                    this.mLastProcessState = processState;
                    int dalvikProcessState = 1;
                    if (processState <= 5) {
                        dalvikProcessState = 0;
                    }
                    VMRuntime.getRuntime().updateProcessState(dalvikProcessState);
                }
            }
        }

        public void setNetworkBlockSeq(long procStateSeq) {
            synchronized (ActivityThread.this.mNetworkPolicyLock) {
                long unused = ActivityThread.this.mNetworkBlockSeq = procStateSeq;
            }
        }

        public void scheduleInstallProvider(ProviderInfo provider) {
            ActivityThread.this.sendMessage(145, provider);
        }

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

        public void scheduleEnterAnimationComplete(IBinder token) {
            ActivityThread.this.sendMessage(149, token);
        }

        public void notifyCleartextNetwork(byte[] firstPacket) {
            if (StrictMode.vmCleartextNetworkEnabled()) {
                StrictMode.onCleartextNetworkDetected(firstPacket);
            }
        }

        public void startBinderTracking() {
            ActivityThread.this.sendMessage(150, null);
        }

        public void stopBinderTrackingAndDump(ParcelFileDescriptor pfd) {
            try {
                ActivityThread.this.sendMessage(151, pfd.dup());
            } catch (IOException e) {
                Log.e(ActivityThread.TAG, "stopBinderTrackingAndDump()");
            } catch (Throwable th) {
                IoUtils.closeQuietly(pfd);
                throw th;
            }
            IoUtils.closeQuietly(pfd);
        }

        public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = voiceInteractor;
            ActivityThread.this.sendMessage(154, args);
        }

        public void handleTrustStorageUpdate() {
            NetworkSecurityPolicy.getInstance().handleTrustStorageUpdate();
        }

        public void schedulePCWindowStateChanged(IBinder token, int windowState) throws RemoteException {
            ActivityThread.this.sendMessage(1003, token, windowState);
        }

        public void scheduleFreeFormOutLineChanged(IBinder token, int state) throws RemoteException {
            ActivityThread.this.sendMessageAtFront(1004, token, state, 0, false);
        }

        public void scheduleRestoreFreeFormConfig(IBinder token) throws RemoteException {
            ActivityThread.this.sendMessage(1005, token, 0);
        }

        public void requestContentNode(IBinder appToken, Bundle data, int token) throws RemoteException {
            if (ActivityThread.sContentSensorManager == null) {
                ActivityClientRecord r = ActivityThread.this.mActivities.get(appToken);
                ActivityThread.sContentSensorManager = ContentSensorManagerFactory.createContentSensorManager(token, r == null ? null : r.activity);
            }
            ActivityClientRecord r2 = SomeArgs.obtain();
            r2.arg1 = appToken;
            r2.arg2 = data;
            ActivityThread.this.sendMessage(162, r2, token);
        }

        public void requestContentOther(IBinder appToken, Bundle data, int token) throws RemoteException {
            if (ActivityThread.sContentSensorManager == null) {
                ActivityClientRecord r = ActivityThread.this.mActivities.get(appToken);
                ActivityThread.sContentSensorManager = ContentSensorManagerFactory.createContentSensorManager(token, r == null ? null : r.activity);
            }
            ActivityClientRecord r2 = SomeArgs.obtain();
            r2.arg1 = appToken;
            r2.arg2 = data;
            ActivityThread.this.sendMessage(161, r2, token);
        }

        public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
            ActivityThread.this.scheduleTransaction(transaction);
        }
    }

    static final class BindServiceData {
        Intent intent;
        boolean rebind;
        IBinder token;

        BindServiceData() {
        }

        public String toString() {
            return "BindServiceData{token=" + this.token + " intent=" + this.intent + "}";
        }
    }

    static final class ContextCleanupInfo {
        ContextImpl context;
        String what;
        String who;

        ContextCleanupInfo() {
        }
    }

    static final class CreateBackupAgentData {
        ApplicationInfo appInfo;
        int backupMode;
        CompatibilityInfo compatInfo;

        CreateBackupAgentData() {
        }

        public String toString() {
            return "CreateBackupAgentData{appInfo=" + this.appInfo + " backupAgent=" + this.appInfo.backupAgentName + " mode=" + this.backupMode + "}";
        }
    }

    static final class CreateServiceData {
        CompatibilityInfo compatInfo;
        ServiceInfo info;
        Intent intent;
        IBinder token;

        CreateServiceData() {
        }

        public String toString() {
            return "CreateServiceData{token=" + this.token + " className=" + this.info.name + " packageName=" + this.info.packageName + " intent=" + this.intent + "}";
        }
    }

    private class DropBoxReporter implements DropBox.Reporter {
        private DropBoxManager dropBox;

        public DropBoxReporter() {
        }

        public void addData(String tag, byte[] data, int flags) {
            ensureInitialized();
            this.dropBox.addData(tag, data, flags);
        }

        public void addText(String tag, String data) {
            ensureInitialized();
            this.dropBox.addText(tag, data);
        }

        private synchronized void ensureInitialized() {
            if (this.dropBox == null) {
                this.dropBox = (DropBoxManager) ActivityThread.this.getSystemContext().getSystemService(Context.DROPBOX_SERVICE);
            }
        }
    }

    static final class DumpComponentInfo {
        String[] args;
        ParcelFileDescriptor fd;
        String prefix;
        IBinder token;

        DumpComponentInfo() {
        }
    }

    static final class DumpHeapData {
        ParcelFileDescriptor fd;
        public boolean mallocInfo;
        public boolean managed;
        String path;
        public boolean runGc;

        DumpHeapData() {
        }
    }

    private static class EventLoggingReporter implements EventLogger.Reporter {
        private EventLoggingReporter() {
        }

        public void report(int code, Object... list) {
            EventLog.writeEvent(code, list);
        }
    }

    final class GcIdler implements MessageQueue.IdleHandler {
        GcIdler() {
        }

        public final boolean queueIdle() {
            ActivityThread.this.doGcIfNeeded();
            return false;
        }
    }

    class H extends Handler {
        public static final int APPLICATION_INFO_CHANGED = 156;
        public static final int ATTACH_AGENT = 155;
        public static final int BIND_APPLICATION = 110;
        public static final int BIND_SERVICE = 121;
        public static final int CLEAN_UP_CONTEXT = 119;
        public static final int CONFIGURATION_CHANGED = 118;
        public static final int CREATE_BACKUP_AGENT = 128;
        public static final int CREATE_SERVICE = 114;
        public static final int CUSTOM_MSG = 1000;
        public static final int DESTROY_BACKUP_AGENT = 129;
        public static final int DISPATCH_PACKAGE_BROADCAST = 133;
        public static final int DUMP_ACTIVITY = 136;
        public static final int DUMP_HEAP = 135;
        public static final int DUMP_PROVIDER = 141;
        public static final int DUMP_SERVICE = 123;
        public static final int ENABLE_JIT = 132;
        public static final int ENTER_ANIMATION_COMPLETE = 149;
        public static final int EXECUTE_TRANSACTION = 159;
        public static final int EXIT_APPLICATION = 111;
        public static final int FREEFORM_OUTLINE_CHANGED = 1004;
        public static final int GC_WHEN_IDLE = 120;
        public static final int INSTALL_PROVIDER = 145;
        public static final int LOAD_CYCLE_PATTERN = 1007;
        public static final int LOCAL_VOICE_INTERACTION_STARTED = 154;
        public static final int LOW_MEMORY = 124;
        public static final int MESSAGE_COLOR_PICKER = 1006;
        public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
        public static final int PROFILER_CONTROL = 127;
        public static final int RECEIVER = 113;
        public static final int RELAUNCH_ACTIVITY = 160;
        public static final int REMOVE_PROVIDER = 131;
        public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
        public static final int REQUEST_NODEGROUP_CONTENT = 162;
        public static final int REQUEST_OTHER_CONTENT = 161;
        public static final int RESTORE_FREEFORM_CONFIG = 1005;
        public static final int RUN_ISOLATED_ENTRY_POINT = 158;
        public static final int SCHEDULE_CRASH = 134;
        public static final int SCHEDULE_REPORT_RT = 2000;
        public static final int SERVICE_ARGS = 115;
        public static final int SET_CORE_SETTINGS = 138;
        public static final int SLEEPING = 137;
        public static final int START_BINDER_TRACKING = 150;
        public static final int STOP_BINDER_TRACKING_AND_DUMP = 151;
        public static final int STOP_SERVICE = 116;
        public static final int SUICIDE = 130;
        public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
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

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 2000) {
                switch (i) {
                    case 110:
                        Trace.traceBegin(64, "bindApplication");
                        ActivityThread.this.handleBindApplication((AppBindData) msg.obj);
                        if (ActivityThread.sIsMygote && ActivityThread.IS_DEBUG_VERSION) {
                            long unused = ActivityThread.this.mStartTime = System.currentTimeMillis();
                            sendEmptyMessageDelayed(2000, 60000);
                        }
                        Trace.traceEnd(64);
                        break;
                    case 111:
                        if (ActivityThread.sIsMygote && ActivityThread.IS_DEBUG_VERSION) {
                            removeMessages(2000);
                            if (!ActivityThread.this.handleScheduleReportRT()) {
                                Slog.e(ActivityThread.TAG, "fail to report RT infomation!!!");
                            }
                        }
                        if (ActivityThread.sIsMygote) {
                            removeMessages(1007);
                        }
                        if (ActivityThread.this.mInitialApplication != null) {
                            ActivityThread.this.mInitialApplication.onTerminate();
                        }
                        Looper.myLooper().quit();
                        break;
                    default:
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
                                Trace.traceEnd(64);
                                break;
                            default:
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
                                    default:
                                        boolean z = false;
                                        boolean z2 = true;
                                        switch (i) {
                                            case 127:
                                                ActivityThread activityThread = ActivityThread.this;
                                                if (msg.arg1 != 0) {
                                                    z = true;
                                                }
                                                activityThread.handleProfilerControl(z, (ProfilerInfo) msg.obj, msg.arg2);
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
                                            case 132:
                                                ActivityThread.this.ensureJitEnabled();
                                                break;
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
                                                    z = true;
                                                }
                                                activityThread2.handleSleeping(iBinder, z);
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
                                                        ActivityThread.this.handleUnstableProviderDied((IBinder) msg.obj, z);
                                                        break;
                                                    case 143:
                                                        ActivityThread.this.handleRequestAssistContextExtras((RequestAssistContextExtras) msg.obj);
                                                        break;
                                                    case 144:
                                                        ActivityThread activityThread3 = ActivityThread.this;
                                                        IBinder iBinder2 = (IBinder) msg.obj;
                                                        if (msg.arg1 == 1) {
                                                            z = true;
                                                        }
                                                        activityThread3.handleTranslucentConversionComplete(iBinder2, z);
                                                        break;
                                                    case 145:
                                                        ActivityThread.this.handleInstallProvider((ProviderInfo) msg.obj);
                                                        break;
                                                    case 146:
                                                        Pair<IBinder, ActivityOptions> pair = (Pair) msg.obj;
                                                        ActivityThread.this.onNewActivityOptions((IBinder) pair.first, (ActivityOptions) pair.second);
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
                                                                        ActivityThread.handleAttachAgent((String) msg.obj, app != null ? app.mLoadedApk : null);
                                                                        break;
                                                                    case 156:
                                                                        ActivityThread.this.mUpdatingSystemConfig = true;
                                                                        try {
                                                                            ActivityThread activityThread4 = ActivityThread.this;
                                                                            ApplicationInfo applicationInfo = (ApplicationInfo) msg.obj;
                                                                            if (msg.arg1 != 1) {
                                                                                z2 = z;
                                                                            }
                                                                            activityThread4.handleApplicationInfoChanged(applicationInfo, z2);
                                                                            break;
                                                                        } finally {
                                                                            ActivityThread.this.mUpdatingSystemConfig = z;
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
                                                                                SomeArgs args1 = ActivityThread.this;
                                                                                IBinder iBinder3 = (IBinder) msg.obj;
                                                                                if (msg.arg1 == 1) {
                                                                                    z = true;
                                                                                }
                                                                                args1.handleRelaunchActivityLocally(iBinder3, z);
                                                                                break;
                                                                            case 161:
                                                                                SomeArgs args12 = (SomeArgs) msg.obj;
                                                                                ActivityThread.this.handleContentOther((IBinder) args12.arg1, (Bundle) args12.arg2, msg.arg1);
                                                                                break;
                                                                            case 162:
                                                                                SomeArgs args = (SomeArgs) msg.obj;
                                                                                ActivityThread.this.handleRequestNode((IBinder) args.arg1, (Bundle) args.arg2, msg.arg1);
                                                                                break;
                                                                            default:
                                                                                switch (i) {
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
                                                                                        if (ActivityThread.this.mHwActivityThread != null) {
                                                                                            ActivityThread.this.mHwActivityThread.loadAppCyclePatternAsync(ActivityThread.this.mBoundApplication.info, ActivityThread.this.mBoundApplication.appInfo, ActivityThread.this.mBoundApplication.processName);
                                                                                            break;
                                                                                        }
                                                                                        break;
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
            } else {
                if (!ActivityThread.this.handleScheduleReportRT()) {
                    Slog.e(ActivityThread.TAG, "fail to report RT infomation!!!");
                }
                sendMessageDelayed(obtainMessage(2000), 3600000);
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

        public final boolean queueIdle() {
            ActivityClientRecord a = ActivityThread.this.mNewActivities;
            boolean stopProfiling = false;
            if (!(ActivityThread.this.mBoundApplication == null || ActivityThread.this.mProfiler.profileFd == null || !ActivityThread.this.mProfiler.autoStopProfiler)) {
                stopProfiling = true;
            }
            if (a != null) {
                ActivityThread.this.mNewActivities = null;
                IActivityManager am = ActivityManager.getService();
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
                    ActivityClientRecord prev = a;
                    a = a.nextIdle;
                    prev.nextIdle = null;
                } while (a != null);
            }
            if (stopProfiling) {
                ActivityThread.this.mProfiler.stopProfiling();
            }
            ActivityThread.this.ensureJitEnabled();
            return false;
        }
    }

    private final class PreloadThreadHandler extends Handler {
        private static final int ASYNC_THREAD_QUIT = 0;
        private static final int PRE_BINDER_API_CACHE = 6;
        private static final int PRE_INIT_BUILD_MODEL = 2;
        private static final int PRE_INIT_LOGEXCEPTION = 1;
        private static final int PRE_INIT_SCROLLER_BOOST_MANAGER = 7;
        private static final int PRE_INSTALL_MEMORY_LEAK_MONITOR = 4;
        private static final int PRE_LODE_MULTIDPIINFO = 5;
        private static final int PRE_REPORT_BIND_APP_TO_AWARE = 8;

        public PreloadThreadHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    synchronized (ActivityThread.mPreloadLock) {
                        if (ActivityThread.USE_CACHE) {
                            HwFrameworkFactory.getHwApiCacheManagerEx().disableCache();
                        }
                        if (!(ActivityThread.this.mPreloadHandlerThread == null || ActivityThread.this.mPreloadHandlerThread.getLooper() == null)) {
                            ActivityThread.this.mPreloadHandlerThread.getLooper().quit();
                            HandlerThread unused = ActivityThread.this.mPreloadHandlerThread = null;
                            PreloadThreadHandler unused2 = ActivityThread.this.mPreloadHandler = null;
                        }
                    }
                    return;
                case 1:
                    AppBindData data = (AppBindData) msg.obj;
                    LogException logexception = HwFrameworkFactory.getLogException();
                    if ((data.appInfo.flags & 129) != 0 && (data.appInfo.hwFlags & 33554432) == 0) {
                        logexception.setliblogparam(1, "");
                    }
                    if (data.appInfo.packageName != null && logexception.isInLogBlackList(data.appInfo.packageName)) {
                        BatteryManager batteryManager = (BatteryManager) ActivityThread.this.getSystemContext().getSystemService(Context.BATTERY_SERVICE);
                        if (batteryManager == null || !batteryManager.isCharging()) {
                            Flog.i(101, "package " + data.appInfo.packageName + " is in black list, forbid logcat output");
                            logexception.setliblogparam(2, "");
                            return;
                        }
                        Flog.i(101, "package " + data.appInfo.packageName + " is in black list but usb is connected, enable logcat output");
                        return;
                    }
                    return;
                case 2:
                    AppBindData data1 = (AppBindData) msg.obj;
                    if (HwFrameworkFactory.getHwActivityThread() != null) {
                        HwFrameworkFactory.getHwActivityThread().changeToSpecialModel(data1.appInfo.packageName);
                        return;
                    }
                    return;
                case 4:
                    ((Application) msg.obj).installMemoryLeakMonitor();
                    return;
                case 5:
                    Resources.getPreMultidpiInfo((String) msg.obj);
                    return;
                case 6:
                    PackageManager pm = ActivityThread.this.getSystemContext().getPackageManager();
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
                default:
                    Slog.e(ActivityThread.TAG, "Invalid preload activity message msg:" + msg.what);
                    return;
            }
        }
    }

    static final class Profiler {
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
            if (this.profiling) {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return;
            }
            if (this.profileFd != null) {
                try {
                    this.profileFd.close();
                } catch (IOException e2) {
                }
            }
            this.profileFile = profilerInfo.profileFile;
            this.profileFd = fd;
            this.samplingInterval = profilerInfo.samplingInterval;
            this.autoStopProfiler = profilerInfo.autoStopProfiler;
            this.streamingOutput = profilerInfo.streamingOutput;
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
                if (this.profileFd != null) {
                    try {
                        this.profileFd.close();
                    } catch (IOException e) {
                    }
                }
                this.profileFd = null;
                this.profileFile = null;
            }
        }
    }

    final class ProviderClientRecord {
        final ContentProviderHolder mHolder;
        final ContentProvider mLocalProvider;
        final String[] mNames;
        final IContentProvider mProvider;

        ProviderClientRecord(String[] names, IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
            this.mNames = names;
            this.mProvider = provider;
            this.mLocalProvider = localProvider;
            this.mHolder = holder;
        }
    }

    private static final class ProviderKey {
        final String authority;
        final int userId;

        public ProviderKey(String authority2, int userId2) {
            this.authority = authority2;
            this.userId = userId2;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof ProviderKey)) {
                return false;
            }
            ProviderKey other = (ProviderKey) o;
            if (Objects.equals(this.authority, other.authority) && this.userId == other.userId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.authority != null ? this.authority.hashCode() : 0) ^ this.userId;
        }
    }

    private static final class ProviderRefCount {
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

    static final class ReceiverData extends BroadcastReceiver.PendingResult {
        CompatibilityInfo compatInfo;
        ActivityInfo info;
        Intent intent;

        public ReceiverData(Intent intent2, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, IBinder token, int sendingUser) {
            super(resultCode, resultData, resultExtras, 0, ordered, sticky, token, sendingUser, intent2.getFlags());
            this.intent = intent2;
        }

        public String toString() {
            return "ReceiverData{intent=" + this.intent + " packageName=" + this.info.packageName + " resultCode=" + getResultCode() + " resultData=" + getResultData() + " resultExtras=" + getResultExtras(false) + "}";
        }
    }

    static final class RequestAssistContextExtras {
        IBinder activityToken;
        int flags;
        IBinder requestToken;
        int requestType;
        int sessionId;

        RequestAssistContextExtras() {
        }
    }

    static final class ServiceArgsData {
        Intent args;
        int flags;
        int startId;
        boolean taskRemoved;
        IBinder token;

        ServiceArgsData() {
        }

        public String toString() {
            return "ServiceArgsData{token=" + this.token + " startId=" + this.startId + " args=" + this.args + "}";
        }
    }

    static final class UpdateCompatibilityData {
        CompatibilityInfo info;
        String pkg;

        UpdateCompatibilityData() {
        }
    }

    /* access modifiers changed from: private */
    public native void nDumpGraphicsInfo(FileDescriptor fileDescriptor);

    static {
        boolean z = true;
        if (System.getenv("MAPLE_RUNTIME") == null) {
            z = false;
        }
        sIsMygote = z;
    }

    public void updatePendingConfiguration(Configuration config) {
        this.mAppThread.updatePendingConfiguration(updateConfig(config));
    }

    public void updateProcessState(int processState, boolean fromIpc) {
        this.mAppThread.updateProcessState(processState, fromIpc);
    }

    /* access modifiers changed from: private */
    public void sendPreloadMessage(int msgWhat, Object data, long delayTime) {
        synchronized (mPreloadLock) {
            if (this.mPreloadHandler == null) {
                this.mPreloadHandlerThread = new HandlerThread("queued-work-looper", 10);
                this.mPreloadHandlerThread.start();
                this.mPreloadHandler = new PreloadThreadHandler(this.mPreloadHandlerThread.getLooper());
            }
            this.mPreloadHandler.sendMessageDelayed(this.mPreloadHandler.obtainMessage(msgWhat, data), delayTime);
        }
    }

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

    public static String currentPackageName() {
        ActivityThread am = currentActivityThread();
        if (am == null || am.mBoundApplication == null) {
            return null;
        }
        return am.mBoundApplication.appInfo.packageName;
    }

    public static String currentProcessName() {
        ActivityThread am = currentActivityThread();
        if (am == null || am.mBoundApplication == null) {
            return null;
        }
        return am.mBoundApplication.processName;
    }

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

    public static boolean isWechatScanOpt() {
        boolean isOptEnabled = false;
        boolean isWechatScanOpt = false;
        if (HwFrameworkFactory.getHwActivityThread() != null) {
            isOptEnabled = HwFrameworkFactory.getHwActivityThread().getWechatScanOpt();
        }
        if (isOptEnabled) {
            String name = HwFrameworkFactory.getHwActivityThread().getWechatScanActivity();
            isWechatScanOpt = name != null ? name.equals(currentActivityName()) : false;
        }
        Slog.d(TAG, "isOptEnabled=" + isOptEnabled + " isWechatScanOpt=" + isWechatScanOpt);
        return isWechatScanOpt;
    }

    public static IContentSensorManager getContentSensorManager() {
        return sContentSensorManager;
    }

    public static void setContentSensorManager(IContentSensorManager contentSensorManager) {
        sContentSensorManager = contentSensorManager;
    }

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
        if (!compat.supportsScreen()) {
            this.mMainThreadConfig.setTo(config);
            config = this.mMainThreadConfig;
            compat.applyToConfiguration(displayDensity, config);
        }
        return config;
    }

    /* access modifiers changed from: package-private */
    public Resources getTopLevelResources(String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, LoadedApk pkgInfo) {
        return this.mResourcesManager.getResources(null, resDir, splitResDirs, overlayDirs, libDirs, displayId, null, pkgInfo.getCompatibilityInfo(), pkgInfo.getClassLoader());
    }

    /* access modifiers changed from: package-private */
    public final Handler getHandler() {
        return this.mH;
    }

    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags) {
        return getPackageInfo(packageName, compatInfo, flags, UserHandle.myUserId());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0082, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0090, code lost:
        r1 = getPackageManager().getApplicationInfo(r8, 268436480, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0092, code lost:
        if (r1 == null) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0098, code lost:
        return getPackageInfo(r1, r9, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0099, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x009a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009f, code lost:
        throw r2.rethrowFromSystemServer();
     */
    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags, int userId) {
        WeakReference<LoadedApk> ref;
        boolean differentUser = UserHandle.myUserId() != userId;
        synchronized (this.mResourcesManager) {
            if (differentUser) {
                ref = null;
            } else if ((flags & 1) != 0) {
                try {
                    ref = this.mPackages.get(packageName);
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            } else {
                ref = this.mResourcePackages.get(packageName);
            }
            LoadedApk packageInfo = ref != null ? (LoadedApk) ref.get() : null;
            if (packageInfo != null && (packageInfo.mResources == null || packageInfo.mResources.getAssets().isUpToDate())) {
                if (packageInfo.isSecurityViolation()) {
                    if ((flags & 2) == 0) {
                        throw new SecurityException("Requesting code from " + packageName + " to be run in process " + this.mBoundApplication.processName + "/" + this.mBoundApplication.appInfo.uid);
                    }
                }
            }
        }
    }

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

    public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo) {
        return getPackageInfo(ai, compatInfo, null, false, true, false);
    }

    public final LoadedApk peekPackageInfo(String packageName, boolean includeCode) {
        WeakReference<LoadedApk> ref;
        LoadedApk loadedApk;
        synchronized (this.mResourcesManager) {
            if (includeCode) {
                try {
                    ref = this.mPackages.get(packageName);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                ref = this.mResourcePackages.get(packageName);
            }
            loadedApk = ref != null ? (LoadedApk) ref.get() : null;
        }
        return loadedApk;
    }

    private LoadedApk getPackageInfo(ApplicationInfo aInfo, CompatibilityInfo compatInfo, ClassLoader baseLoader, boolean securityViolation, boolean includeCode, boolean registerPackage) {
        WeakReference<LoadedApk> ref;
        LoadedApk packageInfo;
        String str;
        ApplicationInfo applicationInfo = aInfo;
        CompatibilityInfo compatibilityInfo = compatInfo;
        boolean differentUser = UserHandle.myUserId() != UserHandle.getUserId(applicationInfo.uid);
        synchronized (this.mResourcesManager) {
            if (differentUser) {
                ref = null;
            } else if (includeCode) {
                try {
                    ref = this.mPackages.get(applicationInfo.packageName);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                ref = this.mResourcePackages.get(applicationInfo.packageName);
            }
            String str2 = null;
            packageInfo = ref != null ? (LoadedApk) ref.get() : null;
            if (packageInfo == null || (packageInfo.mResources != null && !packageInfo.mResources.getAssets().isUpToDate())) {
                if (localLOGV) {
                    StringBuilder sb = new StringBuilder();
                    if (includeCode) {
                        str = "Loading code package ";
                    } else {
                        str = "Loading resource-only package ";
                    }
                    sb.append(str);
                    sb.append(applicationInfo.packageName);
                    sb.append(" (in ");
                    if (this.mBoundApplication != null) {
                        str2 = this.mBoundApplication.processName;
                    }
                    sb.append(str2);
                    sb.append(")");
                    Slog.v(TAG, sb.toString());
                }
                LoadedApk loadedApk = new LoadedApk(this, applicationInfo, compatibilityInfo, baseLoader, securityViolation, includeCode && (applicationInfo.flags & 4) != 0, registerPackage);
                packageInfo = loadedApk;
                if (this.mSystemThread && "android".equals(applicationInfo.packageName)) {
                    packageInfo.installSystemApplicationInfo(applicationInfo, getSystemContext().mPackageInfo.getClassLoader());
                }
                if (!differentUser) {
                    if (includeCode) {
                        this.mPackages.put(applicationInfo.packageName, new WeakReference(packageInfo));
                    } else {
                        this.mResourcePackages.put(applicationInfo.packageName, new WeakReference(packageInfo));
                    }
                }
            }
            if (compatibilityInfo != null && !compatInfo.supportsScreen() && !packageInfo.getCompatibilityInfo().equals(compatibilityInfo)) {
                packageInfo.setCompatibilityInfo(compatibilityInfo);
            }
        }
        return packageInfo;
    }

    ActivityThread() {
    }

    public ApplicationThread getApplicationThread() {
        return this.mAppThread;
    }

    public Instrumentation getInstrumentation() {
        return this.mInstrumentation;
    }

    public boolean isProfiling() {
        return (this.mProfiler == null || this.mProfiler.profileFile == null || this.mProfiler.profileFd != null) ? false : true;
    }

    public String getProfileFilePath() {
        return this.mProfiler.profileFile;
    }

    public Looper getLooper() {
        return this.mLooper;
    }

    public Executor getExecutor() {
        return this.mExecutor;
    }

    public Application getApplication() {
        return this.mInitialApplication;
    }

    public String getProcessName() {
        return this.mBoundApplication.processName;
    }

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

    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        synchronized (this) {
            getSystemContext().installSystemApplicationInfo(info, classLoader);
            getSystemUiContext().installSystemApplicationInfo(info, classLoader);
            this.mProfiler = new Profiler();
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureJitEnabled() {
        if (!this.mJitEnabled) {
            this.mJitEnabled = true;
            VMRuntime.getRuntime().startJitCompilation();
        }
    }

    /* access modifiers changed from: package-private */
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
    public void doGcIfNeeded() {
        this.mGcIdlerScheduled = false;
        if (BinderInternal.getLastGcTime() + MIN_TIME_BETWEEN_GCS < SystemClock.uptimeMillis()) {
            BinderInternal.forceGc("bg");
            if (sIsMygote) {
                VMRuntime.getRuntime();
                VMRuntime.onAppBackgroundGc();
            }
        }
    }

    static void printRow(PrintWriter pw, String format, Object... objs) {
        pw.println(String.format(format, objs));
    }

    public static void dumpMemInfoTable(PrintWriter pw, Debug.MemoryInfo memInfo, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, int pid, String processName, long nativeMax, long nativeAllocated, long nativeFree, long dalvikMax, long dalvikAllocated, long dalvikFree) {
        long j;
        int otherPss;
        int i;
        int i2;
        int otherSwappedOutPss;
        int mySharedClean;
        int otherSharedClean;
        int otherPrivateClean;
        int i3;
        int i4;
        PrintWriter printWriter = pw;
        Debug.MemoryInfo memoryInfo = memInfo;
        long j2 = nativeMax;
        long j3 = nativeAllocated;
        long j4 = nativeFree;
        long j5 = dalvikMax;
        long j6 = dalvikAllocated;
        long j7 = dalvikFree;
        int i5 = 0;
        if (checkin) {
            printWriter.print(4);
            printWriter.print(',');
            printWriter.print(pid);
            printWriter.print(',');
            printWriter.print(processName);
            printWriter.print(',');
            printWriter.print(j2);
            printWriter.print(',');
            printWriter.print(j5);
            printWriter.print(',');
            printWriter.print("N/A,");
            printWriter.print(j2 + j5);
            printWriter.print(',');
            printWriter.print(j3);
            printWriter.print(',');
            printWriter.print(j6);
            printWriter.print(',');
            printWriter.print("N/A,");
            printWriter.print(j3 + j6);
            printWriter.print(',');
            printWriter.print(j4);
            printWriter.print(',');
            printWriter.print(j7);
            printWriter.print(',');
            printWriter.print("N/A,");
            printWriter.print(j4 + j7);
            printWriter.print(',');
            Debug.MemoryInfo memoryInfo2 = memInfo;
            printWriter.print(memoryInfo2.nativePss);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikPss);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherPss);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalPss());
            printWriter.print(',');
            printWriter.print(memoryInfo2.nativeSwappablePss);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikSwappablePss);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherSwappablePss);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalSwappablePss());
            printWriter.print(',');
            printWriter.print(memoryInfo2.nativeSharedDirty);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikSharedDirty);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherSharedDirty);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalSharedDirty());
            printWriter.print(',');
            printWriter.print(memoryInfo2.nativeSharedClean);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikSharedClean);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherSharedClean);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalSharedClean());
            printWriter.print(',');
            printWriter.print(memoryInfo2.nativePrivateDirty);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikPrivateDirty);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherPrivateDirty);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalPrivateDirty());
            printWriter.print(',');
            printWriter.print(memoryInfo2.nativePrivateClean);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikPrivateClean);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherPrivateClean);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalPrivateClean());
            printWriter.print(',');
            printWriter.print(memoryInfo2.nativeSwappedOut);
            printWriter.print(',');
            printWriter.print(memoryInfo2.dalvikSwappedOut);
            printWriter.print(',');
            printWriter.print(memoryInfo2.otherSwappedOut);
            printWriter.print(',');
            printWriter.print(memInfo.getTotalSwappedOut());
            printWriter.print(',');
            if (memoryInfo2.hasSwappedOutPss) {
                printWriter.print(memoryInfo2.nativeSwappedOutPss);
                printWriter.print(',');
                printWriter.print(memoryInfo2.dalvikSwappedOutPss);
                printWriter.print(',');
                printWriter.print(memoryInfo2.otherSwappedOutPss);
                printWriter.print(',');
                printWriter.print(memInfo.getTotalSwappedOutPss());
                printWriter.print(',');
            } else {
                printWriter.print("N/A,");
                printWriter.print("N/A,");
                printWriter.print("N/A,");
                printWriter.print("N/A,");
            }
            while (true) {
                int i6 = i5;
                if (i6 < 17) {
                    printWriter.print(Debug.MemoryInfo.getOtherLabel(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherPss(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherSwappablePss(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherSharedDirty(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherSharedClean(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherPrivateDirty(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherPrivateClean(i6));
                    printWriter.print(',');
                    printWriter.print(memoryInfo2.getOtherSwappedOut(i6));
                    printWriter.print(',');
                    if (memoryInfo2.hasSwappedOutPss) {
                        printWriter.print(memoryInfo2.getOtherSwappedOutPss(i6));
                        printWriter.print(',');
                    } else {
                        printWriter.print("N/A,");
                    }
                    i5 = i6 + 1;
                } else {
                    return;
                }
            }
        } else {
            Debug.MemoryInfo memoryInfo3 = memoryInfo;
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
                    objArr[7] = memoryInfo3.hasSwappedOutPss ? "SwapPss" : "Swap";
                    objArr[8] = "Heap";
                    objArr[9] = "Heap";
                    objArr[10] = "Heap";
                    printRow(printWriter, HEAP_FULL_COLUMN, objArr);
                    printRow(printWriter, HEAP_FULL_COLUMN, "", "Total", "Clean", "Dirty", "Dirty", "Clean", "Clean", "Dirty", "Size", "Alloc", "Free");
                    printRow(printWriter, HEAP_FULL_COLUMN, "", "------", "------", "------", "------", "------", "------", "------", "------", "------", "------");
                    Object[] objArr2 = new Object[11];
                    objArr2[0] = "Native Heap";
                    objArr2[1] = Integer.valueOf(memoryInfo3.nativePss);
                    objArr2[2] = Integer.valueOf(memoryInfo3.nativeSwappablePss);
                    objArr2[3] = Integer.valueOf(memoryInfo3.nativeSharedDirty);
                    objArr2[4] = Integer.valueOf(memoryInfo3.nativePrivateDirty);
                    objArr2[5] = Integer.valueOf(memoryInfo3.nativeSharedClean);
                    objArr2[6] = Integer.valueOf(memoryInfo3.nativePrivateClean);
                    objArr2[7] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? memoryInfo3.nativeSwappedOutPss : memoryInfo3.nativeSwappedOut);
                    objArr2[8] = Long.valueOf(nativeMax);
                    objArr2[9] = Long.valueOf(nativeAllocated);
                    objArr2[10] = Long.valueOf(nativeFree);
                    printRow(printWriter, HEAP_FULL_COLUMN, objArr2);
                    Object[] objArr3 = new Object[11];
                    objArr3[0] = "Dalvik Heap";
                    objArr3[1] = Integer.valueOf(memoryInfo3.dalvikPss);
                    objArr3[2] = Integer.valueOf(memoryInfo3.dalvikSwappablePss);
                    objArr3[3] = Integer.valueOf(memoryInfo3.dalvikSharedDirty);
                    objArr3[4] = Integer.valueOf(memoryInfo3.dalvikPrivateDirty);
                    objArr3[5] = Integer.valueOf(memoryInfo3.dalvikSharedClean);
                    objArr3[6] = Integer.valueOf(memoryInfo3.dalvikPrivateClean);
                    objArr3[7] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? memoryInfo3.dalvikSwappedOutPss : memoryInfo3.dalvikSwappedOut);
                    j = dalvikMax;
                    objArr3[8] = Long.valueOf(dalvikMax);
                    objArr3[9] = Long.valueOf(dalvikAllocated);
                    objArr3[10] = Long.valueOf(dalvikFree);
                    printRow(printWriter, HEAP_FULL_COLUMN, objArr3);
                } else {
                    j = dalvikMax;
                    Object[] objArr4 = new Object[8];
                    objArr4[0] = "";
                    objArr4[1] = "Pss";
                    objArr4[2] = "Private";
                    objArr4[3] = "Private";
                    objArr4[4] = memoryInfo3.hasSwappedOutPss ? "SwapPss" : "Swap";
                    objArr4[5] = "Heap";
                    objArr4[6] = "Heap";
                    objArr4[7] = "Heap";
                    printRow(printWriter, HEAP_COLUMN, objArr4);
                    printRow(printWriter, HEAP_COLUMN, "", "Total", "Dirty", "Clean", "Dirty", "Size", "Alloc", "Free");
                    printRow(printWriter, HEAP_COLUMN, "", "------", "------", "------", "------", "------", "------", "------", "------");
                    Object[] objArr5 = new Object[8];
                    objArr5[0] = "Native Heap";
                    objArr5[1] = Integer.valueOf(memoryInfo3.nativePss);
                    objArr5[2] = Integer.valueOf(memoryInfo3.nativePrivateDirty);
                    objArr5[3] = Integer.valueOf(memoryInfo3.nativePrivateClean);
                    if (memoryInfo3.hasSwappedOutPss) {
                        i3 = memoryInfo3.nativeSwappedOutPss;
                    } else {
                        i3 = memoryInfo3.nativeSwappedOut;
                    }
                    objArr5[4] = Integer.valueOf(i3);
                    objArr5[5] = Long.valueOf(nativeMax);
                    objArr5[6] = Long.valueOf(nativeAllocated);
                    objArr5[7] = Long.valueOf(nativeFree);
                    printRow(printWriter, HEAP_COLUMN, objArr5);
                    Object[] objArr6 = new Object[8];
                    objArr6[0] = "Dalvik Heap";
                    objArr6[1] = Integer.valueOf(memoryInfo3.dalvikPss);
                    objArr6[2] = Integer.valueOf(memoryInfo3.dalvikPrivateDirty);
                    objArr6[3] = Integer.valueOf(memoryInfo3.dalvikPrivateClean);
                    if (memoryInfo3.hasSwappedOutPss) {
                        i4 = memoryInfo3.dalvikSwappedOutPss;
                    } else {
                        i4 = memoryInfo3.dalvikSwappedOut;
                    }
                    objArr6[4] = Integer.valueOf(i4);
                    objArr6[5] = Long.valueOf(dalvikMax);
                    objArr6[6] = Long.valueOf(dalvikAllocated);
                    objArr6[7] = Long.valueOf(dalvikFree);
                    printRow(printWriter, HEAP_COLUMN, objArr6);
                }
                int otherPss2 = memoryInfo3.otherPss;
                int otherSwappablePss = memoryInfo3.otherSwappablePss;
                int otherSharedDirty = memoryInfo3.otherSharedDirty;
                int otherPrivateDirty = memoryInfo3.otherPrivateDirty;
                int otherPss3 = otherPss2;
                int otherSharedClean2 = memoryInfo3.otherSharedClean;
                int otherPrivateClean2 = memoryInfo3.otherPrivateClean;
                int otherSwappedOut = memoryInfo3.otherSwappedOut;
                int otherSwappedOutPss2 = memoryInfo3.otherSwappedOutPss;
                int otherPrivateDirty2 = otherPrivateDirty;
                int i7 = 0;
                int otherSharedClean3 = otherSharedClean2;
                int otherPrivateClean3 = otherPrivateClean2;
                int otherSharedDirty2 = otherSharedDirty;
                int otherSwappablePss2 = otherSwappablePss;
                int otherPss4 = otherPss3;
                while (i7 < 17) {
                    int myPss = memoryInfo3.getOtherPss(i7);
                    int mySwappablePss = memoryInfo3.getOtherSwappablePss(i7);
                    int mySharedDirty = memoryInfo3.getOtherSharedDirty(i7);
                    int myPrivateDirty = memoryInfo3.getOtherPrivateDirty(i7);
                    int mySharedClean2 = memoryInfo3.getOtherSharedClean(i7);
                    int myPrivateClean = memoryInfo3.getOtherPrivateClean(i7);
                    int mySwappedOut = memoryInfo3.getOtherSwappedOut(i7);
                    int mySwappedOutPss = memoryInfo3.getOtherSwappedOutPss(i7);
                    if (myPss == 0 && mySharedDirty == 0 && myPrivateDirty == 0 && mySharedClean2 == 0 && myPrivateClean == 0) {
                        otherSwappedOutPss = otherSwappedOutPss2;
                        if ((memoryInfo3.hasSwappedOutPss != 0 ? mySwappedOutPss : mySwappedOut) == 0) {
                            otherSwappedOutPss2 = otherSwappedOutPss;
                            i7++;
                            long j8 = nativeMax;
                            long j9 = nativeAllocated;
                            long j10 = dalvikAllocated;
                        }
                    } else {
                        otherSwappedOutPss = otherSwappedOutPss2;
                    }
                    if (dumpFullInfo) {
                        otherSharedClean = otherSharedClean3;
                        otherPrivateClean = otherPrivateClean3;
                        Object[] objArr7 = new Object[11];
                        objArr7[0] = Debug.MemoryInfo.getOtherLabel(i7);
                        objArr7[1] = Integer.valueOf(myPss);
                        objArr7[2] = Integer.valueOf(mySwappablePss);
                        objArr7[3] = Integer.valueOf(mySharedDirty);
                        objArr7[4] = Integer.valueOf(myPrivateDirty);
                        objArr7[5] = Integer.valueOf(mySharedClean2);
                        objArr7[6] = Integer.valueOf(myPrivateClean);
                        objArr7[7] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut);
                        mySharedClean = mySharedClean2;
                        objArr7[8] = "";
                        objArr7[9] = "";
                        objArr7[10] = "";
                        printRow(printWriter, HEAP_FULL_COLUMN, objArr7);
                    } else {
                        mySharedClean = mySharedClean2;
                        otherSharedClean = otherSharedClean3;
                        otherPrivateClean = otherPrivateClean3;
                        Object[] objArr8 = new Object[8];
                        objArr8[0] = Debug.MemoryInfo.getOtherLabel(i7);
                        objArr8[1] = Integer.valueOf(myPss);
                        objArr8[2] = Integer.valueOf(myPrivateDirty);
                        objArr8[3] = Integer.valueOf(myPrivateClean);
                        objArr8[4] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut);
                        objArr8[5] = "";
                        objArr8[6] = "";
                        objArr8[7] = "";
                        printRow(printWriter, HEAP_COLUMN, objArr8);
                    }
                    otherPss4 -= myPss;
                    otherSwappablePss2 -= mySwappablePss;
                    otherSharedDirty2 -= mySharedDirty;
                    otherPrivateDirty2 -= myPrivateDirty;
                    otherSharedClean3 = otherSharedClean - mySharedClean;
                    otherPrivateClean3 = otherPrivateClean - myPrivateClean;
                    otherSwappedOut -= mySwappedOut;
                    otherSwappedOutPss2 = otherSwappedOutPss - mySwappedOutPss;
                    i7++;
                    long j82 = nativeMax;
                    long j92 = nativeAllocated;
                    long j102 = dalvikAllocated;
                }
                int otherSharedClean4 = otherSharedClean3;
                int otherPrivateClean4 = otherPrivateClean3;
                int otherSwappedOutPss3 = otherSwappedOutPss2;
                if (dumpFullInfo) {
                    Object[] objArr9 = new Object[11];
                    objArr9[0] = "Unknown";
                    objArr9[1] = Integer.valueOf(otherPss4);
                    objArr9[2] = Integer.valueOf(otherSwappablePss2);
                    objArr9[3] = Integer.valueOf(otherSharedDirty2);
                    objArr9[4] = Integer.valueOf(otherPrivateDirty2);
                    int otherSharedClean5 = otherSharedClean4;
                    objArr9[5] = Integer.valueOf(otherSharedClean5);
                    int otherPrivateClean5 = otherPrivateClean4;
                    objArr9[6] = Integer.valueOf(otherPrivateClean5);
                    objArr9[7] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? otherSwappedOutPss3 : otherSwappedOut);
                    objArr9[8] = "";
                    objArr9[9] = "";
                    objArr9[10] = "";
                    printRow(printWriter, HEAP_FULL_COLUMN, objArr9);
                    Object[] objArr10 = new Object[11];
                    objArr10[0] = "TOTAL";
                    objArr10[1] = Integer.valueOf(memInfo.getTotalPss());
                    objArr10[2] = Integer.valueOf(memInfo.getTotalSwappablePss());
                    objArr10[3] = Integer.valueOf(memInfo.getTotalSharedDirty());
                    objArr10[4] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                    objArr10[5] = Integer.valueOf(memInfo.getTotalSharedClean());
                    objArr10[6] = Integer.valueOf(memInfo.getTotalPrivateClean());
                    if (memoryInfo3.hasSwappedOutPss) {
                        i2 = memInfo.getTotalSwappedOutPss();
                    } else {
                        i2 = memInfo.getTotalSwappedOut();
                    }
                    objArr10[7] = Integer.valueOf(i2);
                    objArr10[8] = Long.valueOf(nativeMax + j);
                    int i8 = otherSharedClean5;
                    objArr10[9] = Long.valueOf(nativeAllocated + dalvikAllocated);
                    int i9 = otherSwappablePss2;
                    int otherSwappablePss3 = otherPrivateClean5;
                    objArr10[10] = Long.valueOf(nativeFree + dalvikFree);
                    printRow(printWriter, HEAP_FULL_COLUMN, objArr10);
                    int i10 = otherPrivateDirty2;
                    long j11 = nativeAllocated;
                } else {
                    int otherPrivateDirty3 = otherPrivateDirty2;
                    int i11 = otherSwappablePss2;
                    int i12 = otherSharedClean4;
                    long j12 = nativeMax;
                    long j13 = nativeFree;
                    long j14 = dalvikFree;
                    long j15 = dalvikAllocated;
                    Object[] objArr11 = new Object[8];
                    objArr11[0] = "Unknown";
                    objArr11[1] = Integer.valueOf(otherPss4);
                    objArr11[2] = Integer.valueOf(otherPrivateDirty3);
                    objArr11[3] = Integer.valueOf(otherPrivateClean4);
                    objArr11[4] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? otherSwappedOutPss3 : otherSwappedOut);
                    objArr11[5] = "";
                    objArr11[6] = "";
                    objArr11[7] = "";
                    printRow(printWriter, HEAP_COLUMN, objArr11);
                    Object[] objArr12 = new Object[8];
                    objArr12[0] = "TOTAL";
                    objArr12[1] = Integer.valueOf(memInfo.getTotalPss());
                    objArr12[2] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                    objArr12[3] = Integer.valueOf(memInfo.getTotalPrivateClean());
                    if (memoryInfo3.hasSwappedOutPss) {
                        i = memInfo.getTotalSwappedOutPss();
                    } else {
                        i = memInfo.getTotalSwappedOut();
                    }
                    objArr12[4] = Integer.valueOf(i);
                    int i13 = otherPrivateDirty3;
                    objArr12[5] = Long.valueOf(j12 + j);
                    objArr12[6] = Long.valueOf(nativeAllocated + j15);
                    objArr12[7] = Long.valueOf(j13 + j14);
                    printRow(printWriter, HEAP_COLUMN, objArr12);
                }
                if (dumpDalvik) {
                    printWriter.println(" ");
                    printWriter.println(" Dalvik Details");
                    int i14 = 17;
                    while (true) {
                        int i15 = i14;
                        if (i15 >= 31) {
                            break;
                        }
                        int myPss2 = memoryInfo3.getOtherPss(i15);
                        int mySwappablePss2 = memoryInfo3.getOtherSwappablePss(i15);
                        int mySharedDirty2 = memoryInfo3.getOtherSharedDirty(i15);
                        int myPrivateDirty2 = memoryInfo3.getOtherPrivateDirty(i15);
                        int mySharedClean3 = memoryInfo3.getOtherSharedClean(i15);
                        int myPrivateClean2 = memoryInfo3.getOtherPrivateClean(i15);
                        int mySwappedOut2 = memoryInfo3.getOtherSwappedOut(i15);
                        int mySwappedOutPss2 = memoryInfo3.getOtherSwappedOutPss(i15);
                        if (myPss2 == 0 && mySharedDirty2 == 0 && myPrivateDirty2 == 0 && mySharedClean3 == 0 && myPrivateClean2 == 0) {
                            if ((memoryInfo3.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2) == 0) {
                                otherPss = otherPss4;
                                i14 = i15 + 1;
                                otherPss4 = otherPss;
                                long j16 = nativeAllocated;
                                long j17 = nativeFree;
                            }
                        }
                        if (dumpFullInfo) {
                            otherPss = otherPss4;
                            Object[] objArr13 = new Object[11];
                            objArr13[0] = Debug.MemoryInfo.getOtherLabel(i15);
                            objArr13[1] = Integer.valueOf(myPss2);
                            objArr13[2] = Integer.valueOf(mySwappablePss2);
                            objArr13[3] = Integer.valueOf(mySharedDirty2);
                            objArr13[4] = Integer.valueOf(myPrivateDirty2);
                            objArr13[5] = Integer.valueOf(mySharedClean3);
                            objArr13[6] = Integer.valueOf(myPrivateClean2);
                            objArr13[7] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2);
                            int i16 = mySharedClean3;
                            objArr13[8] = "";
                            objArr13[9] = "";
                            objArr13[10] = "";
                            printRow(printWriter, HEAP_FULL_COLUMN, objArr13);
                        } else {
                            otherPss = otherPss4;
                            Object[] objArr14 = new Object[8];
                            objArr14[0] = Debug.MemoryInfo.getOtherLabel(i15);
                            objArr14[1] = Integer.valueOf(myPss2);
                            objArr14[2] = Integer.valueOf(myPrivateDirty2);
                            objArr14[3] = Integer.valueOf(myPrivateClean2);
                            objArr14[4] = Integer.valueOf(memoryInfo3.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2);
                            objArr14[5] = "";
                            objArr14[6] = "";
                            objArr14[7] = "";
                            printRow(printWriter, HEAP_COLUMN, objArr14);
                        }
                        i14 = i15 + 1;
                        otherPss4 = otherPss;
                        long j162 = nativeAllocated;
                        long j172 = nativeFree;
                    }
                }
            } else {
                long j18 = j6;
                long j19 = dalvikMax;
            }
            printWriter.println(" ");
            printWriter.println(" App Summary");
            printRow(printWriter, ONE_COUNT_COLUMN_HEADER, "", "Pss(KB)");
            printRow(printWriter, ONE_COUNT_COLUMN_HEADER, "", "------");
            printRow(printWriter, ONE_COUNT_COLUMN, "Java Heap:", Integer.valueOf(memInfo.getSummaryJavaHeap()));
            printRow(printWriter, ONE_COUNT_COLUMN, "Native Heap:", Integer.valueOf(memInfo.getSummaryNativeHeap()));
            printRow(printWriter, ONE_COUNT_COLUMN, "Code:", Integer.valueOf(memInfo.getSummaryCode()));
            printRow(printWriter, ONE_COUNT_COLUMN, "Stack:", Integer.valueOf(memInfo.getSummaryStack()));
            printRow(printWriter, ONE_COUNT_COLUMN, "Graphics:", Integer.valueOf(memInfo.getSummaryGraphics()));
            printRow(printWriter, ONE_COUNT_COLUMN, "Private Other:", Integer.valueOf(memInfo.getSummaryPrivateOther()));
            printRow(printWriter, ONE_COUNT_COLUMN, "System:", Integer.valueOf(memInfo.getSummarySystem()));
            printWriter.println(" ");
            if (memoryInfo3.hasSwappedOutPss) {
                printRow(printWriter, TWO_COUNT_COLUMNS, "TOTAL:", Integer.valueOf(memInfo.getSummaryTotalPss()), "TOTAL SWAP PSS:", Integer.valueOf(memInfo.getSummaryTotalSwapPss()));
            } else {
                printRow(printWriter, TWO_COUNT_COLUMNS, "TOTAL:", Integer.valueOf(memInfo.getSummaryTotalPss()), "TOTAL SWAP (KB):", Integer.valueOf(memInfo.getSummaryTotalSwap()));
            }
        }
    }

    private static void dumpMemoryInfo(ProtoOutputStream proto, long fieldId, String name, int pss, int cleanPss, int sharedDirty, int privateDirty, int sharedClean, int privateClean, boolean hasSwappedOutPss, int dirtySwap, int dirtySwapPss) {
        ProtoOutputStream protoOutputStream = proto;
        long token = proto.start(fieldId);
        protoOutputStream.write(1138166333441L, name);
        protoOutputStream.write(1120986464258L, pss);
        protoOutputStream.write(1120986464259L, cleanPss);
        protoOutputStream.write(1120986464260L, sharedDirty);
        protoOutputStream.write(1120986464261L, privateDirty);
        protoOutputStream.write(1120986464262L, sharedClean);
        protoOutputStream.write(1120986464263L, privateClean);
        if (hasSwappedOutPss) {
            protoOutputStream.write(NotificationChannelProto.LIGHT_COLOR, dirtySwapPss);
            int i = dirtySwap;
        } else {
            int i2 = dirtySwapPss;
            protoOutputStream.write(1120986464264L, dirtySwap);
        }
        protoOutputStream.end(token);
    }

    public static void dumpMemInfoTable(ProtoOutputStream proto, Debug.MemoryInfo memInfo, boolean dumpDalvik, boolean dumpSummaryOnly, long nativeMax, long nativeAllocated, long nativeFree, long dalvikMax, long dalvikAllocated, long dalvikFree) {
        long tToken;
        int i;
        long j;
        long j2;
        int i2;
        long j3;
        long dvToken;
        ProtoOutputStream protoOutputStream = proto;
        Debug.MemoryInfo memoryInfo = memInfo;
        long j4 = nativeMax;
        long j5 = nativeAllocated;
        long j6 = nativeFree;
        long j7 = dalvikMax;
        long j8 = dalvikAllocated;
        long j9 = dalvikFree;
        if (!dumpSummaryOnly) {
            long nhToken = protoOutputStream.start(1146756268035L);
            long j10 = j5;
            dumpMemoryInfo(protoOutputStream, 1146756268033L, "Native Heap", memoryInfo.nativePss, memoryInfo.nativeSwappablePss, memoryInfo.nativeSharedDirty, memoryInfo.nativePrivateDirty, memoryInfo.nativeSharedClean, memoryInfo.nativePrivateClean, memoryInfo.hasSwappedOutPss, memoryInfo.nativeSwappedOut, memoryInfo.nativeSwappedOutPss);
            protoOutputStream.write(1120986464258L, nativeMax);
            protoOutputStream.write(1120986464259L, nativeAllocated);
            long j11 = nativeFree;
            protoOutputStream.write(1120986464260L, j11);
            long nhToken2 = nhToken;
            protoOutputStream.end(nhToken2);
            long dvToken2 = protoOutputStream.start(1146756268036L);
            long j12 = nhToken2;
            Debug.MemoryInfo memoryInfo2 = memInfo;
            int i3 = memoryInfo2.dalvikPss;
            memoryInfo = memoryInfo2;
            long j13 = j11;
            long j14 = nativeAllocated;
            dumpMemoryInfo(protoOutputStream, 1146756268033L, "Dalvik Heap", i3, memoryInfo2.dalvikSwappablePss, memoryInfo2.dalvikSharedDirty, memoryInfo2.dalvikPrivateDirty, memoryInfo2.dalvikSharedClean, memoryInfo2.dalvikPrivateClean, memoryInfo2.hasSwappedOutPss, memoryInfo2.dalvikSwappedOut, memoryInfo2.dalvikSwappedOutPss);
            long j15 = dalvikMax;
            protoOutputStream.write(1120986464258L, j15);
            long j16 = dalvikAllocated;
            protoOutputStream.write(1120986464259L, j16);
            long j17 = dalvikFree;
            protoOutputStream.write(1120986464260L, j17);
            long dvToken3 = dvToken2;
            protoOutputStream.end(dvToken3);
            int otherPss = memoryInfo.otherPss;
            int otherSwappablePss = memoryInfo.otherSwappablePss;
            int otherSharedDirty = memoryInfo.otherSharedDirty;
            int otherPrivateDirty = memoryInfo.otherPrivateDirty;
            int otherSharedClean = memoryInfo.otherSharedClean;
            int otherPrivateClean = memoryInfo.otherPrivateClean;
            int otherPss2 = otherPss;
            int otherSwappedOut = memoryInfo.otherSwappedOut;
            int myPss = 0;
            int otherSwappedOutPss = memoryInfo.otherSwappedOutPss;
            int otherSharedDirty2 = otherSharedDirty;
            int otherPrivateDirty2 = otherPrivateDirty;
            int otherSharedClean2 = otherSharedClean;
            int otherPrivateClean2 = otherPrivateClean;
            while (true) {
                int i4 = myPss;
                if (i4 >= 17) {
                    break;
                }
                int myPss2 = memoryInfo.getOtherPss(i4);
                int mySwappablePss = memoryInfo.getOtherSwappablePss(i4);
                int mySharedDirty = memoryInfo.getOtherSharedDirty(i4);
                int myPrivateDirty = memoryInfo.getOtherPrivateDirty(i4);
                int mySharedClean = memoryInfo.getOtherSharedClean(i4);
                int myPrivateClean = memoryInfo.getOtherPrivateClean(i4);
                int mySwappedOut = memoryInfo.getOtherSwappedOut(i4);
                int mySwappedOutPss = memoryInfo.getOtherSwappedOutPss(i4);
                if (myPss2 == 0 && mySharedDirty == 0 && myPrivateDirty == 0 && mySharedClean == 0 && myPrivateClean == 0) {
                    if ((memoryInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) == 0) {
                        dvToken = dvToken3;
                        j3 = j17;
                        i2 = i4;
                        j2 = j16;
                        j = j15;
                        myPss = i2 + 1;
                        dvToken3 = dvToken;
                        j17 = j3;
                        j16 = j2;
                        j15 = j;
                    }
                }
                dvToken = dvToken3;
                j3 = j17;
                i2 = i4;
                j2 = j16;
                j = j15;
                dumpMemoryInfo(protoOutputStream, 2246267895813L, Debug.MemoryInfo.getOtherLabel(i4), myPss2, mySwappablePss, mySharedDirty, myPrivateDirty, mySharedClean, myPrivateClean, memoryInfo.hasSwappedOutPss, mySwappedOut, mySwappedOutPss);
                otherPss2 -= myPss2;
                otherSwappablePss -= mySwappablePss;
                otherSharedDirty2 -= mySharedDirty;
                otherPrivateDirty2 -= myPrivateDirty;
                otherSharedClean2 -= mySharedClean;
                otherPrivateClean2 -= myPrivateClean;
                otherSwappedOut -= mySwappedOut;
                otherSwappedOutPss -= mySwappedOutPss;
                myPss = i2 + 1;
                dvToken3 = dvToken;
                j17 = j3;
                j16 = j2;
                j15 = j;
            }
            long j18 = j15;
            int myPss3 = 17;
            dumpMemoryInfo(protoOutputStream, RemoteAnimationTargetProto.CONTENT_INSETS, "Unknown", otherPss2, otherSwappablePss, otherSharedDirty2, otherPrivateDirty2, otherSharedClean2, otherPrivateClean2, memoryInfo.hasSwappedOutPss, otherSwappedOut, otherSwappedOutPss);
            long tToken2 = protoOutputStream.start(IntentProto.COMPONENT);
            dumpMemoryInfo(protoOutputStream, 1146756268033L, "TOTAL", memInfo.getTotalPss(), memInfo.getTotalSwappablePss(), memInfo.getTotalSharedDirty(), memInfo.getTotalPrivateDirty(), memInfo.getTotalSharedClean(), memInfo.getTotalPrivateClean(), memoryInfo.hasSwappedOutPss, memInfo.getTotalSwappedOut(), memInfo.getTotalSwappedOutPss());
            protoOutputStream.write(1120986464258L, nativeMax + j18);
            protoOutputStream.write(1120986464259L, j14 + j16);
            protoOutputStream.write(1120986464260L, j13 + j17);
            long tToken3 = tToken2;
            protoOutputStream.end(tToken3);
            if (dumpDalvik) {
                while (true) {
                    int i5 = myPss3;
                    if (i5 >= 31) {
                        break;
                    }
                    int myPss4 = memoryInfo.getOtherPss(i5);
                    int mySwappablePss2 = memoryInfo.getOtherSwappablePss(i5);
                    int mySharedDirty2 = memoryInfo.getOtherSharedDirty(i5);
                    int myPrivateDirty2 = memoryInfo.getOtherPrivateDirty(i5);
                    int mySharedClean2 = memoryInfo.getOtherSharedClean(i5);
                    int myPrivateClean2 = memoryInfo.getOtherPrivateClean(i5);
                    int mySwappedOut2 = memoryInfo.getOtherSwappedOut(i5);
                    int mySwappedOutPss2 = memoryInfo.getOtherSwappedOutPss(i5);
                    if (myPss4 == 0 && mySharedDirty2 == 0 && myPrivateDirty2 == 0 && mySharedClean2 == 0 && myPrivateClean2 == 0) {
                        if ((memoryInfo.hasSwappedOutPss ? mySwappedOutPss2 : mySwappedOut2) == 0) {
                            i = i5;
                            tToken = tToken3;
                            myPss3 = i + 1;
                            tToken3 = tToken;
                        }
                    }
                    i = i5;
                    tToken = tToken3;
                    dumpMemoryInfo(protoOutputStream, 2246267895816L, Debug.MemoryInfo.getOtherLabel(i5), myPss4, mySwappablePss2, mySharedDirty2, myPrivateDirty2, mySharedClean2, myPrivateClean2, memoryInfo.hasSwappedOutPss, mySwappedOut2, mySwappedOutPss2);
                    myPss3 = i + 1;
                    tToken3 = tToken;
                }
            }
        } else {
            long j19 = j8;
            long j20 = j7;
            long j21 = j6;
            long j22 = j5;
            long j23 = j4;
            long j24 = dalvikFree;
        }
        long asToken = protoOutputStream.start(RemoteAnimationTargetProto.SOURCE_CONTAINER_BOUNDS);
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

    public final Activity startActivityNow(Activity parent, String id, Intent intent, ActivityInfo activityInfo, IBinder token, Bundle state, Activity.NonConfigurationInstances lastNonConfigurationInstances) {
        String name;
        ActivityClientRecord r = new ActivityClientRecord();
        r.token = token;
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

    public final Activity getActivity(IBinder token) {
        return this.mActivities.get(token).activity;
    }

    public ActivityClientRecord getActivityClient(IBinder token) {
        return this.mActivities.get(token);
    }

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
    public TransactionExecutor getTransactionExecutor() {
        return this.mTransactionExecutor;
    }

    /* access modifiers changed from: package-private */
    public void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0, false);
    }

    /* access modifiers changed from: private */
    public void sendMessage(int what, Object obj, int arg1) {
        sendMessage(what, obj, arg1, 0, false);
    }

    /* access modifiers changed from: private */
    public void sendMessage(int what, Object obj, int arg1, int arg2) {
        sendMessage(what, obj, arg1, arg2, false);
    }

    /* access modifiers changed from: private */
    public void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mH.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void sendMessageAtFront(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mH.sendMessageAtFrontOfQueue(msg);
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
    }

    /* access modifiers changed from: package-private */
    public final void scheduleContextCleanup(ContextImpl context, String who, String what) {
        ContextCleanupInfo cci = new ContextCleanupInfo();
        cci.context = context;
        cci.who = who;
        cci.what = what;
        sendMessage(119, cci);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:107:0x026a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x026b, code lost:
        r1 = r5;
        r26 = r8;
        r28 = r9;
        r20 = r12;
        r2 = r13;
        r3 = r14;
        r4 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x02a3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x02a4, code lost:
        r1 = r5;
        r26 = r8;
        r7 = r9;
        r20 = r12;
        r2 = r13;
        r3 = r14;
        r4 = r15;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x026a A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:18:0x0087] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x027e  */
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        Activity activity;
        ComponentName component;
        ActivityThread activityThread;
        ActivityThread activityThread2;
        ActivityClientRecord activityClientRecord;
        Activity activity2;
        Intent activity3;
        Activity activity4;
        String str;
        Application app;
        Intent intent;
        Activity activity5;
        Instrumentation instrumentation;
        Activity activity6;
        Activity activity7;
        ActivityClientRecord activityClientRecord2 = r;
        Intent intent2 = customIntent;
        ActivityInfo aInfo = activityClientRecord2.activityInfo;
        if (activityClientRecord2.packageInfo == null) {
            activityClientRecord2.packageInfo = getPackageInfo(aInfo.applicationInfo, activityClientRecord2.compatInfo, 1);
        }
        ComponentName component2 = activityClientRecord2.intent.getComponent();
        if (component2 == null) {
            component2 = activityClientRecord2.intent.resolveActivity(this.mInitialApplication.getPackageManager());
            activityClientRecord2.intent.setComponent(component2);
        }
        if (activityClientRecord2.activityInfo.targetActivity != null) {
            component2 = new ComponentName(activityClientRecord2.activityInfo.packageName, activityClientRecord2.activityInfo.targetActivity);
        }
        ComponentName component3 = component2;
        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity8 = null;
        try {
            ClassLoader cl = appContext.getClassLoader();
            activity8 = this.mInstrumentation.newActivity(cl, component3.getClassName(), activityClientRecord2.intent);
            this.mCurrentActivity = component3.getClassName();
            StrictMode.incrementExpectedActivityCount(activity8.getClass());
            activityClientRecord2.intent.setExtrasClassLoader(cl);
            activityClientRecord2.intent.prepareToEnterProcess();
            if (activityClientRecord2.state != null) {
                activityClientRecord2.state.setClassLoader(cl);
            }
        } catch (Exception e) {
            if (!this.mInstrumentation.onException(null, e)) {
                ActivityInfo activityInfo = aInfo;
                Intent intent3 = intent2;
                ActivityClientRecord activityClientRecord3 = activityClientRecord2;
                throw new RuntimeException("Unable to instantiate activity " + component3 + ": " + e.toString(), e);
            }
        }
        Activity activity9 = activity8;
        try {
            Instrumentation instrumentation2 = null;
            Application app2 = activityClientRecord2.packageInfo.makeApplication(false, this.mInstrumentation);
            if (activity9 != null) {
                CharSequence title = activityClientRecord2.activityInfo.loadLabel(appContext.getPackageManager());
                Configuration config = new Configuration(this.mCompatConfiguration);
                if (activityClientRecord2.overrideConfig != null) {
                    try {
                        config.updateFrom(activityClientRecord2.overrideConfig);
                    } catch (SuperNotCalledException e2) {
                        e = e2;
                        Activity activity10 = activity9;
                        ContextImpl contextImpl = appContext;
                        ComponentName componentName = component3;
                        ActivityInfo activityInfo2 = aInfo;
                        Intent intent4 = intent2;
                        ActivityClientRecord activityClientRecord4 = activityClientRecord2;
                    } catch (Exception e3) {
                        e = e3;
                        activity = activity9;
                        ContextImpl contextImpl2 = appContext;
                        component = component3;
                        ActivityInfo activityInfo3 = aInfo;
                        Intent intent5 = intent2;
                        ActivityClientRecord activityClientRecord5 = activityClientRecord2;
                        activityThread = this;
                        if (!activityThread.mInstrumentation.onException(activity, e)) {
                        }
                        return activity;
                    }
                }
                if (localLOGV) {
                    Slog.v(TAG, "Performing launch of " + activityClientRecord2 + " with config " + config);
                }
                Window window = null;
                if (activityClientRecord2.mPendingRemoveWindow != null) {
                    if (activityClientRecord2.mPreserveWindow) {
                        window = activityClientRecord2.mPendingRemoveWindow;
                        activityClientRecord2.mPendingRemoveWindow = null;
                        activityClientRecord2.mPendingRemoveWindowManager = null;
                    }
                }
                Window window2 = window;
                appContext.setOuterContext(activity9);
                Instrumentation instrumentation3 = getInstrumentation();
                IBinder iBinder = activityClientRecord2.token;
                int i = activityClientRecord2.ident;
                Intent intent6 = activityClientRecord2.intent;
                ActivityInfo activityInfo4 = activityClientRecord2.activityInfo;
                ActivityInfo aInfo2 = aInfo;
                try {
                    activity3 = intent6;
                    activity4 = activityClientRecord2.parent;
                    try {
                        activity3 = intent6;
                        str = activityClientRecord2.embeddedID;
                        app = app2;
                        intent = intent6;
                        activity5 = activity9;
                        instrumentation = instrumentation3;
                        activity6 = activity9;
                        ContextImpl contextImpl3 = appContext;
                        component = component3;
                        ActivityInfo activityInfo5 = aInfo2;
                        ActivityClientRecord activityClientRecord6 = activityClientRecord2;
                    } catch (SuperNotCalledException e4) {
                        e = e4;
                        Activity activity11 = activity9;
                        ContextImpl contextImpl4 = appContext;
                        ActivityClientRecord activityClientRecord7 = activityClientRecord2;
                        ActivityInfo activityInfo6 = aInfo2;
                        Intent intent7 = customIntent;
                        throw e;
                    } catch (Exception e5) {
                        e = e5;
                        activity = activity9;
                        ContextImpl contextImpl5 = appContext;
                        component = component3;
                        ActivityClientRecord activityClientRecord8 = activityClientRecord2;
                        activityThread = this;
                        ActivityInfo activityInfo7 = aInfo2;
                        Intent intent8 = customIntent;
                        if (!activityThread.mInstrumentation.onException(activity, e)) {
                        }
                        return activity;
                    }
                } catch (SuperNotCalledException e6) {
                    e = e6;
                    Activity activity12 = activity9;
                    ContextImpl contextImpl6 = appContext;
                    Intent intent9 = intent2;
                    ActivityClientRecord activityClientRecord9 = activityClientRecord2;
                    ActivityInfo activityInfo8 = aInfo2;
                    throw e;
                } catch (Exception e7) {
                    e = e7;
                    activity = activity9;
                    ContextImpl contextImpl7 = appContext;
                    component = component3;
                    Intent intent10 = intent2;
                    ActivityClientRecord activityClientRecord10 = activityClientRecord2;
                    activityThread = this;
                    ActivityInfo activityInfo9 = aInfo2;
                    if (!activityThread.mInstrumentation.onException(activity, e)) {
                    }
                    return activity;
                }
                try {
                    activity3 = activity5;
                    instrumentation2 = instrumentation;
                    activity5.attach(appContext, this, instrumentation, iBinder, i, app, intent, activityInfo4, title, activity4, str, activityClientRecord2.lastNonConfigurationInstances, config, activityClientRecord2.referrer, activityClientRecord2.voiceInteractor, window2, activityClientRecord2.configCallback);
                    Intent intent11 = customIntent;
                    if (intent11 != null) {
                        Activity activity13 = activity6;
                        try {
                            activity13.mIntent = intent11;
                            activity7 = activity13;
                        } catch (SuperNotCalledException e8) {
                            e = e8;
                            ComponentName componentName2 = component;
                            ActivityClientRecord activityClientRecord11 = r;
                        } catch (Exception e9) {
                            e = e9;
                            activityThread = this;
                            ActivityClientRecord activityClientRecord12 = r;
                            activity = activity13;
                            if (!activityThread.mInstrumentation.onException(activity, e)) {
                            }
                            return activity;
                        }
                    } else {
                        activity7 = activity6;
                    }
                    activityClientRecord = r;
                    instrumentation2 = null;
                    try {
                        activity3 = activity7;
                        activityClientRecord.lastNonConfigurationInstances = null;
                        checkAndBlockForNetworkAccess();
                        activity7.mStartedActivity = false;
                        int theme = activityClientRecord.activityInfo.getThemeResource();
                        if (theme != 0) {
                            activity7.setTheme(theme);
                        }
                        activity7.mCalled = false;
                        Slog.v(TAG, "callActivityOnCreate");
                        if (r.isPersistable()) {
                            ActivityThread activityThread3 = this;
                            try {
                                activity3 = activity7;
                                instrumentation2 = activityThread3;
                                activityThread3.mInstrumentation.callActivityOnCreate(activity7, activityClientRecord.state, activityClientRecord.persistentState);
                                activityThread2 = activityThread3;
                            } catch (SuperNotCalledException e10) {
                                e = e10;
                                throw e;
                            } catch (Exception e11) {
                                e = e11;
                                activityThread = instrumentation2;
                                activity = activity3;
                                if (!activityThread.mInstrumentation.onException(activity, e)) {
                                }
                                return activity;
                            }
                        } else {
                            activityThread2 = this;
                            activityThread2.mInstrumentation.callActivityOnCreate(activity7, activityClientRecord.state);
                        }
                        if (activity7.mCalled) {
                            activityClientRecord.activity = activity7;
                            activity2 = activity7;
                        } else {
                            throw new SuperNotCalledException("Activity " + activityClientRecord.intent.getComponent().toShortString() + " did not call through to super.onCreate()");
                        }
                    } catch (SuperNotCalledException e12) {
                        e = e12;
                        throw e;
                    } catch (Exception e13) {
                        e = e13;
                        activityThread = this;
                        activity = activity7;
                        if (!activityThread.mInstrumentation.onException(activity, e)) {
                        }
                        return activity;
                    }
                } catch (SuperNotCalledException e14) {
                    e = e14;
                    Activity activity14 = activity6;
                    Intent intent12 = customIntent;
                    ActivityClientRecord activityClientRecord13 = r;
                    ComponentName componentName3 = component;
                    throw e;
                } catch (Exception e15) {
                    e = e15;
                    activityThread = this;
                    activity = activity6;
                    Intent intent13 = customIntent;
                    ActivityClientRecord activityClientRecord14 = r;
                    if (!activityThread.mInstrumentation.onException(activity, e)) {
                        throw new RuntimeException("Unable to start activity " + component + ": " + e.toString(), e);
                    }
                    return activity;
                }
            } else {
                activity2 = activity9;
                ContextImpl contextImpl8 = appContext;
                ComponentName componentName4 = component3;
                ActivityInfo activityInfo10 = aInfo;
                Intent intent14 = intent2;
                activityClientRecord = activityClientRecord2;
                activityThread2 = this;
            }
            activityClientRecord.setState(1);
            activityThread2.mActivities.put(activityClientRecord.token, activityClientRecord);
            Slog.d(TAG, "add activity client record, r= " + activityClientRecord + " token= " + activityClientRecord.token);
        } catch (SuperNotCalledException e16) {
            e = e16;
            Activity activity15 = activity9;
            ContextImpl contextImpl9 = appContext;
            ActivityInfo activityInfo11 = aInfo;
            Intent intent15 = intent2;
            ActivityClientRecord activityClientRecord15 = activityClientRecord2;
            ComponentName componentName5 = component3;
            throw e;
        } catch (Exception e17) {
        }
        return activity;
    }

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
                    Log.e(TAG, "checkAndBlockForNetworkAccess()");
                }
            }
        }
    }

    private ContextImpl createBaseContextForActivity(ActivityClientRecord r) {
        try {
            int displayId = ActivityManager.getService().getActivityDisplayId(r.token);
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
            for (int id : dm.getDisplayIds()) {
                if (id != 0) {
                    return (ContextImpl) appContext.createDisplayContext(dm.getCompatibleDisplay(id, appContext.getResources()));
                }
            }
            return appContext;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Activity handleLaunchActivity(ActivityClientRecord r, PendingTransactionActions pendingActions, Intent customIntent) {
        if (Jlog.isMicroTest() && r != null) {
            Jlog.i(3084, Jlog.getMessage(TAG, "handleLaunchActivity", Intent.toPkgClsString(r.intent)));
        }
        if (this.mZrHungAppEyeUiProbe != null && this.mIsNeedStartUiProbe) {
            this.mZrHungAppEyeUiProbe.start(null);
        }
        if (Jlog.isPerfTest() && r != null) {
            Jlog.i(3040, Jlog.getMessage(TAG, "handleLaunchActivity", Intent.toPkgClsString(r.intent)));
        }
        if (!(r == null || r.intent == null || r.intent.getComponent() == null)) {
            Jlog.d(R.styleable.Theme_toastFrameBackground, r.intent.getComponent().getPackageName() + "/" + r.intent.getComponent().getClassName(), "");
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
        if (!ThreadedRenderer.sRendererDisabled) {
            GraphicsEnvironment.earlyInitEGL();
        }
        WindowManagerGlobal.initialize();
        if (r == null) {
            return null;
        }
        Activity a = performLaunchActivity(r, customIntent);
        if (Jlog.isPerfTest()) {
            Jlog.i(3043, Jlog.getMessage(TAG, "handleLaunchActivity", Intent.toPkgClsString(r.intent)));
        }
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
                ActivityManager.getService().finishActivity(r.token, 0, null, 0);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        if (Jlog.isMicroTest()) {
            Jlog.i(3085, Jlog.getMessage(TAG, "handleLaunchActivity", Intent.toPkgClsString(r.intent)));
        }
        return a;
    }

    private void reportSizeConfigurations(ActivityClientRecord r) {
        Configuration[] configurations = r.activity.getResources().getSizeConfigurations();
        if (configurations != null) {
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
                ActivityManager.getService().reportSizeConfigurations(r.token, horizontal.copyKeys(), vertical.copyKeys(), smallest.copyKeys());
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

    /* access modifiers changed from: package-private */
    public void performNewIntents(IBinder token, List<ReferrerIntent> intents, boolean andPause) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            boolean resumed = !r.paused;
            if (resumed) {
                r.activity.mTemporaryPause = true;
                this.mInstrumentation.callActivityOnPause(r.activity);
            }
            checkAndBlockForNetworkAccess();
            deliverNewIntents(r, intents);
            if (resumed) {
                r.activity.performResume(false, "performNewIntents");
                r.activity.mTemporaryPause = false;
            }
            if (r.paused && andPause) {
                performResumeActivity(token, false, "performNewIntents");
                performPauseActivityIfNeeded(r, "performNewIntents");
            }
        }
    }

    public void handleNewIntent(IBinder token, List<ReferrerIntent> intents, boolean andPause) {
        performNewIntents(token, intents, andPause);
    }

    public void handleRequestNode(IBinder appToken, Bundle data, int token) {
        try {
            ActivityClientRecord r = this.mActivities.get(appToken);
            Activity a = r == null ? null : r.activity;
            if (sContentSensorManager != null) {
                sContentSensorManager.updateToken(token, a);
                sContentSensorManager.copyNode(data);
            }
        } catch (Exception e) {
            Slog.e(TAG, "copyNode get exception: " + e);
        }
    }

    public void handleContentOther(IBinder appToken, Bundle data, int token) {
        try {
            ActivityClientRecord r = this.mActivities.get(appToken);
            Activity a = r == null ? null : r.activity;
            if (sContentSensorManager != null) {
                sContentSensorManager.updateToken(token, a);
                sContentSensorManager.processImageAndWebView(data);
            }
        } catch (Exception e) {
            Slog.e(TAG, "processImageAndWebView get exception: " + e);
        }
    }

    /* access modifiers changed from: private */
    public boolean handleScheduleReportRT() {
        if (this.mBoundApplication == null || this.mHwActivityThread == null) {
            return false;
        }
        return this.mHwActivityThread.doReportRuntime(this.mBoundApplication.appInfo.processName, this.mStartTime);
    }

    public void handleRequestAssistContextExtras(RequestAssistContextExtras cmd) {
        AssistStructure structure;
        boolean notSecure = false;
        boolean forAutofill = cmd.requestType == 2;
        if (this.mLastSessionId != cmd.sessionId) {
            this.mLastSessionId = cmd.sessionId;
            for (int i = this.mLastAssistStructures.size() - 1; i >= 0; i--) {
                AssistStructure structure2 = (AssistStructure) this.mLastAssistStructures.get(i).get();
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
        Uri referrer = null;
        if (r != null) {
            if (!forAutofill) {
                r.activity.getApplication().dispatchOnProvideAssistData(r.activity, data);
                r.activity.onProvideAssistData(data);
                referrer = r.activity.onProvideReferrer();
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
            }
        }
        Uri referrer2 = referrer;
        if (structure3 == null) {
            structure = new AssistStructure();
        } else {
            structure = structure3;
        }
        structure.setAcquisitionStartTime(startTime);
        structure.setAcquisitionEndTime(SystemClock.uptimeMillis());
        this.mLastAssistStructures.add(new WeakReference(structure));
        try {
            ActivityManager.getService().reportAssistContextExtras(cmd.requestToken, data, structure, content, referrer2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
            installContentProviders(this.mInitialApplication, Arrays.asList(new ProviderInfo[]{info}));
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /* access modifiers changed from: private */
    public void handleEnterAnimationComplete(IBinder token) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null) {
            r.activity.dispatchEnterAnimationComplete();
        }
    }

    /* access modifiers changed from: private */
    public void handleStartBinderTracking() {
        Binder.enableTracing();
    }

    /* access modifiers changed from: private */
    public void handleStopBinderTrackingAndDump(ParcelFileDescriptor fd) {
        try {
            Binder.disableTracing();
            Binder.getTransactionTracker().writeTracesToFile(fd);
        } finally {
            IoUtils.closeQuietly(fd);
            Binder.getTransactionTracker().clearTraces();
        }
    }

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
    public void handleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor interactor) {
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
    public void handleReceiver(ReceiverData data) {
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
    public void handleCreateBackupAgent(CreateBackupAgentData data) {
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
                BackupAgent agent = this.mBackupAgents.get(packageName);
                if (agent != null) {
                    binder = agent.onBind();
                } else {
                    BackupAgent agent2 = (BackupAgent) packageInfo.getClassLoader().loadClass(classname).newInstance();
                    ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
                    context.setOuterContext(agent2);
                    agent2.attach(context);
                    agent2.onCreate();
                    binder = agent2.onBind();
                    this.mBackupAgents.put(packageName, agent2);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                Slog.e(TAG, "Agent threw during creation: " + e2);
                if (data.backupMode != 2) {
                    if (data.backupMode != 3) {
                        throw e2;
                    }
                }
            } catch (Exception e3) {
                throw new RuntimeException("Unable to create BackupAgent " + classname + ": " + e3.toString(), e3);
            }
            ActivityManager.getService().backupAgentCreated(packageName, binder);
        } catch (RemoteException e4) {
            throw e4.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public void handleDestroyBackupAgent(CreateBackupAgentData data) {
        String packageName = getPackageInfoNoCheck(data.appInfo, data.compatInfo).mPackageName;
        BackupAgent agent = this.mBackupAgents.get(packageName);
        if (agent != null) {
            try {
                agent.onDestroy();
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown in onDestroy by backup agent of " + data.appInfo);
                e.printStackTrace();
            }
            this.mBackupAgents.remove(packageName);
            return;
        }
        Slog.w(TAG, "Attempt to destroy unknown backup agent " + data);
    }

    /* access modifiers changed from: private */
    public void handleCreateService(CreateServiceData data) {
        unscheduleGcIdler();
        LoadedApk packageInfo = getPackageInfoNoCheck(data.info.applicationInfo, data.compatInfo);
        Service service = null;
        try {
            service = packageInfo.getAppFactory().instantiateService(packageInfo.getClassLoader(), data.info.name, data.intent);
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
            Service service2 = service;
            ContextImpl contextImpl = context;
            service2.attach(contextImpl, this, data.info.name, data.token, packageInfo.makeApplication(false, this.mInstrumentation), ActivityManager.getService());
            service.onCreate();
            this.mServices.put(data.token, service);
            ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
            if (Jlog.isUBMEnable()) {
                Jlog.d(270, "SC#" + data.info.name + "(" + currentProcessName() + ")");
            }
        } catch (RemoteException e2) {
            Flog.w(102, "serviceDone failed when creating service " + data.info.name + ": " + e2.toString());
            throw e2.rethrowFromSystemServer();
        } catch (Exception e3) {
            if (!this.mInstrumentation.onException(service, e3)) {
                throw new RuntimeException("Unable to create service " + data.info.name + ": " + e3.toString(), e3);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleBindService(BindServiceData data) {
        Service s = this.mServices.get(data.token);
        if (DEBUG_SERVICE) {
            Slog.v(TAG, "handleBindService s=" + s + " rebind=" + data.rebind);
        }
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                if (!data.rebind) {
                    ActivityManager.getService().publishService(data.token, data.intent, s.onBind(data.intent));
                } else {
                    s.onRebind(data.intent);
                    ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
                }
                ensureJitEnabled();
            } catch (RemoteException ex) {
                Flog.w(102, "publishService failed when binding service " + s + " with " + data.intent + ": " + ex.toString());
                throw ex.rethrowFromSystemServer();
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
    public void handleUnbindService(BindServiceData data) {
        Service s = this.mServices.get(data.token);
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                boolean doRebind = s.onUnbind(data.intent);
                if (doRebind) {
                    ActivityManager.getService().unbindFinished(data.token, data.intent, doRebind);
                } else {
                    ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
                }
            } catch (RemoteException ex) {
                Flog.w(102, "Unable to finish unbind to service " + s + " with " + data.intent + ": " + ex.toString());
                throw ex.rethrowFromSystemServer();
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(s, e)) {
                    throw new RuntimeException("Unable to unbind to service " + s + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDumpService(DumpComponentInfo info) {
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
    public void handleDumpActivity(DumpComponentInfo info) {
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
    public void handleDumpProvider(DumpComponentInfo info) {
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
    public void handleServiceArgs(ServiceArgsData data) {
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
                ActivityManager.getService().serviceDoneExecuting(data.token, 1, data.startId, res);
                ensureJitEnabled();
            } catch (RemoteException e) {
                Flog.w(102, "Unable to finish starting service " + s + " with " + data.args + ": " + e.toString());
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                if (!this.mInstrumentation.onException(s, e2)) {
                    throw new RuntimeException("Unable to start service " + s + " with " + data.args + ": " + e2.toString(), e2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleStopService(IBinder token) {
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
                ActivityManager.getService().serviceDoneExecuting(token, 2, 0, 0);
                if (Jlog.isUBMEnable()) {
                    Jlog.d(271, "SE#" + s.getClassName());
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
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
            if (!finalStateRequest && !"com.huawei.desktop.explorer".equals(r.activity.getPackageName())) {
                RuntimeException e = new IllegalStateException("Trying to resume activity which is already resumed");
                Slog.e(TAG, e.getMessage(), e);
                Slog.e(TAG, r.getStateString());
            }
            return null;
        }
        if (finalStateRequest) {
            r.hideForNow = false;
            r.activity.mStartedActivity = false;
        }
        try {
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
            if ("com.huawei.android.launcher.unihome.UniHomeLauncher".equals(r.activity.getComponentName().getClassName())) {
                Jlog.d(383, "JLID_LAUNCHER_ONRESUMED");
            }
            if ("com.tencent.mm.plugin.sns.ui.SnsBrowseUI".equals(r.activity.getComponentName().getClassName())) {
                Looper.myQueue().enableReduceDelay(true);
            }
            r.state = null;
            r.persistentState = null;
            r.setState(3);
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

    public void handleResumeActivity(IBinder token, boolean finalStateRequest, boolean isForward, String reason) {
        boolean z = isForward;
        if (this.mZrHungAppEyeUiProbe != null && this.mIsNeedStartUiProbe) {
            this.mZrHungAppEyeUiProbe.start(null);
        }
        UniPerf.getInstance().uniPerfEvent(4098, "", new int[0]);
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        ActivityClientRecord r = performResumeActivity(token, finalStateRequest, reason);
        if (r != null) {
            if (Jlog.isPerfTest()) {
                Jlog.i(3049, Jlog.getMessage(TAG, "handleResumeActivity", Intent.toPkgClsString(r.intent)));
            }
            Activity a = r.activity;
            if (localLOGV) {
                Slog.v(TAG, "Resume " + r + " started activity: " + a.mStartedActivity + ", hideForNow: " + r.hideForNow + ", finished: " + a.mFinished);
            }
            int forwardBit = z ? 256 : 0;
            boolean willBeVisible = !a.mStartedActivity;
            if (!willBeVisible) {
                try {
                    willBeVisible = ActivityManager.getService().willActivityBeVisible(a.getActivityToken());
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
                    Slog.v(TAG, "Resuming " + r + " with isForward=" + z);
                }
                WindowManager.LayoutParams l2 = r.window.getAttributes();
                if ((l2.softInputMode & 256) != forwardBit) {
                    l2.softInputMode = (l2.softInputMode & -257) | forwardBit;
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
            ViewRootImpl.setIsFirstFrame(true);
            if (Jlog.isPerfTest()) {
                Jlog.i(3050, Jlog.getMessage(TAG, "handleResumeActivity", Intent.toPkgClsString(r.intent)));
            }
            Looper.myQueue().addIdleHandler(new Idler());
        }
    }

    public void handlePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, PendingTransactionActions pendingActions, String reason) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (Jlog.isMicroTest() && r != null) {
            Jlog.i(3086, Jlog.getMessage(TAG, "handlePauseActivity", Intent.toPkgClsString(r.intent, "who")));
        }
        if (localLOGV) {
            Slog.d(TAG, "Handling pause of " + r + ", finished: " + finished + " userLeaving:" + userLeaving);
        }
        if (r != null) {
            if (Jlog.isPerfTest()) {
                Jlog.i(3025, Jlog.getMessage(TAG, "handlePauseActivity", Intent.toPkgClsString(r.intent, "who")));
            }
            if (userLeaving) {
                performUserLeavingActivity(r);
            }
            r.activity.mConfigChangeFlags |= configChanges;
            performPauseActivity(r, finished, reason, pendingActions);
            if (Jlog.isPerfTest()) {
                Jlog.i(3028, Jlog.getMessage(TAG, "handlePauseActivity", Intent.toPkgClsString(r.intent, "who")));
            }
            if (r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            Jlog.d(386, r.activity.getComponentName().getClassName() + " paused ");
            this.mSomeActivitiesChanged = true;
        }
        if (this.mZrHungAppEyeUiProbe != null) {
            this.mZrHungAppEyeUiProbe.stop(null);
        }
        if (Jlog.isMicroTest() && r != null) {
            Jlog.i(3087, Jlog.getMessage(TAG, "handlePauseActivity", Intent.toPkgClsString(r.intent, "who")));
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
        Bundle bundle = null;
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
        int size = listeners != null ? listeners.size() : 0;
        for (int i = 0; i < size; i++) {
            listeners.get(i).onPaused(r.activity);
        }
        Bundle oldState = pendingActions != null ? pendingActions.getOldState() : null;
        if (oldState != null && r.isPreHoneycomb()) {
            r.state = oldState;
        }
        if (shouldSaveState) {
            bundle = r.state;
        }
        return bundle;
    }

    private void performPauseActivityIfNeeded(ActivityClientRecord r, String reason) {
        if (!r.paused) {
            if (localLOGV) {
                Slog.d(TAG, "Performing pause of " + r + " for reason: " + reason);
            }
            try {
                r.activity.mCalled = false;
                this.mInstrumentation.callActivityOnPause(r.activity);
                if ("com.tencent.mm.plugin.sns.ui.SnsBrowseUI".equals(r.activity.getComponentName().getClassName())) {
                    Looper.myQueue().enableReduceDelay(false);
                }
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
    public final void performStopActivity(IBinder token, boolean saveState, String reason) {
        performStopActivityInner(this.mActivities.get(token), null, false, saveState, false, reason);
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
            r.activity.performStop(false, reason);
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

    public void reportStop(PendingTransactionActions pendingActions) {
        this.mH.post(pendingActions.getStopInfo());
    }

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

    public void handleWindowVisibility(IBinder token, boolean show) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "handleWindowVisibility: no activity for token " + token);
            return;
        }
        if (!show && !r.stopped) {
            performStopActivityInner(r, null, show, false, false, "handleWindowVisibility");
        } else if (show && r.stopped) {
            unscheduleGcIdler();
            r.activity.performRestart(true, "handleWindowVisibility");
            r.setState(2);
        }
        if (r.activity.mDecor != null) {
            Slog.v(TAG, "Handle window " + r + " visibility: " + show);
            updateVisibility(r, show);
        }
        this.mSomeActivitiesChanged = true;
    }

    /* access modifiers changed from: private */
    public void handleSleeping(IBinder token, boolean sleeping) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "handleSleeping: no activity for token " + token);
            return;
        }
        if (sleeping) {
            if (!r.stopped && !r.isPreHoneycomb()) {
                callActivityOnStop(r, true, "sleeping");
            }
            if (!r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            try {
                ActivityManager.getService().activitySlept(r.token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else if (r.stopped && r.activity.mVisibleFromServer) {
            r.activity.performRestart(true, "handleSleeping");
            r.setState(2);
        }
    }

    /* access modifiers changed from: private */
    public void handleSetCoreSettings(Bundle coreSettings) {
        synchronized (this.mResourcesManager) {
            this.mCoreSettings = coreSettings;
        }
        onCoreSettingsChange();
    }

    private void onCoreSettingsChange() {
        boolean debugViewAttributes = this.mCoreSettings.getInt("debug_view_attributes", 0) != 0;
        if (debugViewAttributes != View.mDebugViewAttributes) {
            View.mDebugViewAttributes = debugViewAttributes;
            relaunchAllActivities(false);
        }
    }

    private void relaunchAllActivities(boolean fromThemeChange) {
        for (Map.Entry<IBinder, ActivityClientRecord> entry : this.mActivities.entrySet()) {
            if (!entry.getValue().activity.mFinished) {
                scheduleRelaunchActivity(entry.getKey(), fromThemeChange);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdatePackageCompatibilityInfo(UpdateCompatibilityData data) {
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
                    r.activity.mTemporaryPause = true;
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
                r.activity.mTemporaryPause = false;
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
        this.mActivities.remove(token);
        Slog.d(TAG, "Remove activity client record, r= " + r + " token= " + token);
        StrictMode.decrementExpectedActivityCount(cls);
        return r;
    }

    private static String safeToComponentShortString(Intent intent) {
        ComponentName component = intent.getComponent();
        return component == null ? "[Unknown]" : component.toShortString();
    }

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
                ActivityManager.getService().activityDestroyed(token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        this.mSomeActivitiesChanged = true;
    }

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
            target.createdConfig = config.getGlobalConfiguration();
            target.overrideConfig = config.getOverrideConfiguration();
            target.pendingConfigChanges |= configChanges;
        }
        if (scheduleRelaunch) {
            return target;
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0059, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0067, code lost:
        if (r12.createdConfig == null) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006b, code lost:
        if (r10.mConfiguration == null) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0075, code lost:
        if (r12.createdConfig.isOtherSeqNewer(r10.mConfiguration) == false) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x007f, code lost:
        if (r10.mConfiguration.diff(r12.createdConfig) == 0) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0081, code lost:
        if (r1 == null) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0089, code lost:
        if (r12.createdConfig.isOtherSeqNewer(r1) == false) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x008b, code lost:
        r1 = r12.createdConfig;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x008d, code lost:
        r14 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x008e, code lost:
        if (r14 == null) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0090, code lost:
        r10.mCurDefaultDisplayDpi = r14.densityDpi;
        updateDefaultDensity();
        handleConfigurationChanged(r14, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x009a, code lost:
        r15 = r10.mActivities.get(r12.token);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a7, code lost:
        if (localLOGV == false) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a9, code lost:
        android.util.Slog.v(TAG, "Handling relaunch of " + r15 + ": changedConfig=" + r14 + " with configChanges=0x" + java.lang.Integer.toHexString(r13));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00d3, code lost:
        if (r15 != null) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d5, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00d6, code lost:
        r15.activity.mConfigChangeFlags |= r13;
        r15.mPreserveWindow = r12.mPreserveWindow;
        r15.activity.mChangingConfigurations = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00e7, code lost:
        if (r15.mPreserveWindow == false) goto L_0x00f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00e9, code lost:
        android.view.WindowManagerGlobal.getWindowSession().prepareToReplaceWindows(r15.token, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f2, code lost:
        handleRelaunchActivityInner(r15, r13, r12.pendingResults, r12.pendingIntents, r11, r12.startsNotResumed, r12.overrideConfig, "handleRelaunchActivity");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0104, code lost:
        if (r11 == null) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0106, code lost:
        r11.setReportRelaunchToWindowManager(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0109, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x010a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x010f, code lost:
        throw r0.rethrowFromSystemServer();
     */
    public void handleRelaunchActivity(ActivityClientRecord tmp, PendingTransactionActions pendingActions) {
        ActivityClientRecord activityClientRecord;
        int configChanges;
        PendingTransactionActions pendingTransactionActions = pendingActions;
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        Configuration changedConfig = null;
        synchronized (this.mResourcesManager) {
            try {
                int N = this.mRelaunchingActivities.size();
                activityClientRecord = tmp;
                try {
                    IBinder token = activityClientRecord.token;
                    int i = 0;
                    int configChanges2 = 0;
                    ActivityClientRecord tmp2 = null;
                    while (true) {
                        int i2 = i;
                        if (i2 >= N) {
                            break;
                        }
                        try {
                            ActivityClientRecord r = this.mRelaunchingActivities.get(i2);
                            if (r.token == token) {
                                ActivityClientRecord tmp3 = r;
                                try {
                                    configChanges = tmp3.pendingConfigChanges | configChanges2;
                                } catch (Throwable th) {
                                    e = th;
                                    ActivityClientRecord activityClientRecord2 = tmp3;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            e = th2;
                                        }
                                    }
                                    throw e;
                                }
                                try {
                                    this.mRelaunchingActivities.remove(i2);
                                    i2--;
                                    N--;
                                    tmp2 = tmp3;
                                    configChanges2 = configChanges;
                                } catch (Throwable th3) {
                                    e = th3;
                                    ActivityClientRecord activityClientRecord3 = tmp3;
                                    int i3 = configChanges;
                                    while (true) {
                                        break;
                                    }
                                    throw e;
                                }
                            }
                            i = i2 + 1;
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
                    } else if (this.mPendingConfiguration != null) {
                        changedConfig = this.mPendingConfiguration;
                        this.mPendingConfiguration = null;
                    }
                } catch (Throwable th5) {
                    e = th5;
                    ActivityClientRecord tmp4 = activityClientRecord;
                    while (true) {
                        break;
                    }
                    throw e;
                }
            } catch (Throwable th6) {
                e = th6;
                activityClientRecord = tmp;
                ActivityClientRecord tmp42 = activityClientRecord;
                while (true) {
                    break;
                }
                throw e;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleRelaunchActivity(IBinder token) {
        scheduleRelaunchActivity(token, false);
    }

    /* access modifiers changed from: package-private */
    public void scheduleRelaunchActivity(IBinder token, boolean fromThemeChange) {
        sendMessage(160, token, fromThemeChange);
    }

    /* access modifiers changed from: private */
    public void handleRelaunchActivityLocally(IBinder token, boolean fromThemeChange) {
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
        ActivityClientRecord activityClientRecord = r;
        List<ResultInfo> list = pendingResults;
        List<ReferrerIntent> list2 = pendingIntents;
        String str = reason;
        Intent customIntent = activityClientRecord.activity.mIntent;
        if (!activityClientRecord.paused) {
            performPauseActivity(activityClientRecord, false, str, (PendingTransactionActions) null);
        }
        if (!activityClientRecord.stopped) {
            callActivityOnStop(activityClientRecord, true, str);
        }
        handleDestroyActivity(activityClientRecord.token, false, configChanges, true, str);
        activityClientRecord.activity = null;
        activityClientRecord.window = null;
        activityClientRecord.hideForNow = false;
        activityClientRecord.nextIdle = null;
        if (list != null) {
            if (activityClientRecord.pendingResults == null) {
                activityClientRecord.pendingResults = list;
            } else {
                activityClientRecord.pendingResults.addAll(list);
            }
        }
        if (list2 != null) {
            if (activityClientRecord.pendingIntents == null) {
                activityClientRecord.pendingIntents = list2;
            } else {
                activityClientRecord.pendingIntents.addAll(list2);
            }
        }
        activityClientRecord.startsNotResumed = startsNotResumed;
        activityClientRecord.overrideConfig = overrideConfig;
        handleLaunchActivity(activityClientRecord, pendingActions, customIntent);
    }

    public void reportRelaunch(IBinder token, PendingTransactionActions pendingActions) {
        try {
            ActivityManager.getService().activityRelaunched(token);
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
        int i;
        ArrayList<ComponentCallbacks2> callbacks = new ArrayList<>();
        synchronized (this.mResourcesManager) {
            int NAPP = this.mAllApplications.size();
            for (int i2 = 0; i2 < NAPP; i2++) {
                callbacks.add(this.mAllApplications.get(i2));
            }
            int NACT = this.mActivities.size();
            for (int i3 = 0; i3 < NACT; i3++) {
                ActivityClientRecord ar = this.mActivities.valueAt(i3);
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
            for (int i4 = 0; i4 < NSVC; i4++) {
                callbacks.add(this.mServices.valueAt(i4));
            }
        }
        synchronized (this.mProviderMap) {
            int NPRV = this.mLocalProviders.size();
            for (i = 0; i < NPRV; i++) {
                callbacks.add(this.mLocalProviders.valueAt(i).mLocalProvider);
            }
        }
        return callbacks;
    }

    private void performConfigurationChangedForActivity(ActivityClientRecord r, Configuration newBaseConfig) {
        performConfigurationChangedForActivity(r, newBaseConfig, r.activity.getDisplay().getDisplayId(), false);
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
                activity.mConfigChangeFlags = 0;
                activity.mCurrentConfig = new Configuration(newConfig);
                Configuration configToReport = createNewConfigAndUpdateIfNotNull(newConfig, contextThemeWrapperOverrideConfig);
                if (movedToDifferentDisplay) {
                    activity.dispatchMovedToDisplay(displayId, configToReport);
                }
                if (shouldChangeConfig) {
                    activity.mCalled = false;
                    if (!(activity.getResources() == null || activity.getResources().getCompatibilityInfo() == null)) {
                        CompatibilityInfo info = activity.getResources().getCompatibilityInfo();
                        if (!info.supportsScreen()) {
                            info.applyToConfiguration(configToReport.densityDpi, configToReport);
                        }
                    }
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

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00f5, code lost:
        r5 = collectComponentCallbacks(false, r13);
        freeTextLayoutCachesIfNeeded(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0100, code lost:
        if (android.hwtheme.HwThemeManager.setThemeFontOnConfigChg(r13) == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0102, code lost:
        android.graphics.Typeface.loadSystemFonts();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0109, code lost:
        if (currentPackageName() == null) goto L_0x0135;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0115, code lost:
        if (currentPackageName().contains("com.tencent.mm") == false) goto L_0x0135;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0117, code lost:
        android.util.Slog.i(TAG, "ActivityThread.handleConfigurationChanged , new config = " + r13 + ", compat = " + r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0135, code lost:
        if (r5 == null) goto L_0x017f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0137, code lost:
        r7 = r5.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x013c, code lost:
        if (r1 >= r7) goto L_0x0199;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x013e, code lost:
        r8 = r5.get(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0146, code lost:
        if ((r8 instanceof android.app.Activity) == false) goto L_0x015b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0148, code lost:
        performConfigurationChangedForActivity(r12.mActivities.get(((android.app.Activity) r8).getActivityToken()), r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x015b, code lost:
        if (r2 != false) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x015d, code lost:
        performConfigurationChanged(r8, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0161, code lost:
        android.util.Slog.v(TAG, "Skipping handle non-activity callbacks for app:" + currentPackageName());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x017b, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x017f, code lost:
        android.util.Slog.v(TAG, "There are no configuration change callbacks for app:" + currentPackageName());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0199, code lost:
        return;
     */
    private void handleConfigurationChanged(Configuration config, CompatibilityInfo compat) {
        int i = 0;
        boolean equivalent = (config == null || this.mConfiguration == null || this.mConfiguration.diffPublicOnly(config) != 0) ? false : true;
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
                Slog.v(TAG, "Config is null for app:" + currentPackageName());
                return;
            }
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Handle configuration changed: " + config);
            }
            this.mResourcesManager.applyConfigurationToResourcesLocked(config, compat);
            updateLocaleListFromAppContext(this.mInitialApplication.getApplicationContext(), this.mResourcesManager.getConfiguration().getLocales());
            if (this.mConfiguration == null) {
                this.mConfiguration = new Configuration();
            }
            if (this.mConfiguration.isOtherSeqNewer(config) || compat != null) {
                int configDiff = this.mConfiguration.updateFrom(config);
                Configuration config2 = applyCompatConfiguration(this.mCurDefaultDisplayDpi);
                if ((systemTheme.getChangingConfigurations() & configDiff) != 0) {
                    systemTheme.rebase();
                }
                if ((systemUiTheme.getChangingConfigurations() & configDiff) != 0) {
                    systemUiTheme.rebase();
                }
            } else {
                Slog.v(TAG, "Skipping new config:" + this.mConfiguration + ", config:" + config + " for app:" + currentPackageName());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleApplicationInfoChanged(ApplicationInfo ai, boolean fromThemeChange) {
        LoadedApk apk;
        LoadedApk resApk;
        synchronized (this.mResourcesManager) {
            WeakReference<LoadedApk> ref = this.mPackages.get(ai.packageName);
            apk = ref != null ? (LoadedApk) ref.get() : null;
            WeakReference<LoadedApk> ref2 = this.mResourcePackages.get(ai.packageName);
            resApk = ref2 != null ? (LoadedApk) ref2.get() : null;
        }
        if (apk != null) {
            ArrayList<String> oldPaths = new ArrayList<>();
            LoadedApk.makePaths(this, apk.getApplicationInfo(), oldPaths);
            apk.updateApplicationInfo(ai, oldPaths);
        }
        if (resApk != null) {
            ArrayList<String> oldPaths2 = new ArrayList<>();
            LoadedApk.makePaths(this, resApk.getApplicationInfo(), oldPaths2);
            resApk.updateApplicationInfo(ai, oldPaths2);
        }
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyNewResourceDirsLocked(ai.sourceDir, ai.resourceDirs);
        }
        ApplicationPackageManager.configurationChanged();
        Configuration newConfig = new Configuration();
        newConfig.assetsSeq = (this.mConfiguration != null ? this.mConfiguration.assetsSeq : 0) + 1;
        handleConfigurationChanged(newConfig, null);
        relaunchAllActivities(fromThemeChange);
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

    public void handleActivityConfigurationChanged(IBinder activityToken, Configuration overrideConfig, int displayId) {
        ActivityClientRecord r = this.mActivities.get(activityToken);
        if (r == null || r.activity == null) {
            if (DEBUG_CONFIGURATION) {
                Slog.w(TAG, "Not found target activity to report to: " + r);
            }
            return;
        }
        boolean movedToDifferentDisplay = (displayId == -1 || displayId == r.activity.getDisplay().getDisplayId()) ? false : true;
        r.overrideConfig = overrideConfig;
        ViewRootImpl viewRoot = r.activity.mDecor != null ? r.activity.mDecor.getViewRootImpl() : null;
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
            viewRoot.updateConfiguration(displayId);
        }
        this.mSomeActivitiesChanged = true;
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
        if (this.mProfiler != null) {
            this.mProfiler.stopProfiling();
        }
    }

    static void handleDumpHeap(DumpHeapData dhd) {
        if (dhd.runGc) {
            System.gc();
            System.runFinalization();
            System.gc();
        }
        if (dhd.managed) {
            try {
                Debug.dumpHprofData(dhd.path, dhd.fd.getFileDescriptor());
                try {
                    dhd.fd.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Failure closing profile fd", e);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Managed heap dump failed on path " + dhd.path + " -- can the process access this path?");
                dhd.fd.close();
            } catch (Throwable th) {
                try {
                    dhd.fd.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "Failure closing profile fd", e3);
                }
                throw th;
            }
        } else if (dhd.mallocInfo) {
            Debug.dumpNativeMallocInfo(dhd.fd.getFileDescriptor());
        } else {
            Debug.dumpNativeHeap(dhd.fd.getFileDescriptor());
        }
        try {
            ActivityManager.getService().dumpHeapFinished(dhd.path);
        } catch (RemoteException e4) {
            throw e4.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public final void handleDispatchPackageBroadcast(int cmd, String[] packages) {
        boolean hasPkgInfo = false;
        if (cmd != 0) {
            switch (cmd) {
                case 2:
                    break;
                case 3:
                    if (packages != null) {
                        synchronized (this.mResourcesManager) {
                            int i = packages.length - 1;
                            while (true) {
                                int i2 = i;
                                if (i2 >= 0) {
                                    WeakReference<LoadedApk> ref = this.mPackages.get(packages[i2]);
                                    LoadedApk loadedApk = null;
                                    LoadedApk pkgInfo = ref != null ? (LoadedApk) ref.get() : null;
                                    if (pkgInfo != null) {
                                        hasPkgInfo = true;
                                    } else {
                                        WeakReference<LoadedApk> ref2 = this.mResourcePackages.get(packages[i2]);
                                        if (ref2 != null) {
                                            loadedApk = (LoadedApk) ref2.get();
                                        }
                                        pkgInfo = loadedApk;
                                        if (pkgInfo != null) {
                                            hasPkgInfo = true;
                                        }
                                    }
                                    if (pkgInfo != null) {
                                        try {
                                            String packageName = packages[i2];
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
                                                ArrayList<String> oldPaths = new ArrayList<>();
                                                LoadedApk.makePaths(this, pkgInfo.getApplicationInfo(), oldPaths);
                                                pkgInfo.updateApplicationInfo(aInfo, oldPaths);
                                            }
                                        } catch (RemoteException e) {
                                        }
                                    }
                                    i = i2 - 1;
                                }
                            }
                        }
                        break;
                    }
                    break;
            }
        }
        boolean killApp = cmd == 0;
        if (packages != null) {
            synchronized (this.mResourcesManager) {
                int i3 = packages.length - 1;
                while (true) {
                    int i4 = i3;
                    if (i4 >= 0) {
                        if (!hasPkgInfo) {
                            WeakReference<LoadedApk> ref3 = this.mPackages.get(packages[i4]);
                            if (ref3 == null || ref3.get() == null) {
                                WeakReference<LoadedApk> ref4 = this.mResourcePackages.get(packages[i4]);
                                if (!(ref4 == null || ref4.get() == null)) {
                                    hasPkgInfo = true;
                                }
                            } else {
                                hasPkgInfo = true;
                            }
                        }
                        if (killApp) {
                            this.mPackages.remove(packages[i4]);
                            this.mResourcePackages.remove(packages[i4]);
                        }
                        i3 = i4 - 1;
                    }
                }
            }
        }
        ApplicationPackageManager.handlePackageBroadcast(cmd, packages, hasPkgInfo);
    }

    /* access modifiers changed from: package-private */
    public final void handleLowMemory() {
        ArrayList<ComponentCallbacks2> callbacks = collectComponentCallbacks(true, null);
        int N = callbacks.size();
        for (int i = 0; i < N; i++) {
            callbacks.get(i).onLowMemory();
        }
        if (Process.myUid() != 1000) {
            EventLog.writeEvent(SQLITE_MEM_RELEASED_EVENT_LOG_TAG, SQLiteDatabase.releaseMemory());
        }
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
        BinderInternal.forceGc("mem");
    }

    /* access modifiers changed from: private */
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
                        ThreadedRenderer.setupDiskCache(codeCacheDir);
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
        GraphicsEnvironment.getInstance().setup(context);
        Trace.traceEnd(64);
    }

    private void updateDefaultDensity() {
        int densityDpi = this.mCurDefaultDisplayDpi;
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
    /* JADX WARNING: Removed duplicated region for block: B:129:0x0382  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0461  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0469  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04cc A[Catch:{ Exception -> 0x058e, Exception -> 0x04d2, all -> 0x05b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x04f5  */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x0521 A[SYNTHETIC, Splitter:B:175:0x0521] */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0559  */
    /* JADX WARNING: Removed duplicated region for block: B:210:? A[RETURN, SYNTHETIC] */
    public void handleBindApplication(AppBindData data) {
        InstrumentationInfo ii;
        InstrumentationInfo ii2;
        StrictMode.ThreadPolicy savedPolicy;
        Application app;
        InstrumentationInfo ii3;
        ApplicationInfo instrApp;
        LoadedApk pi;
        ContextImpl instrContext;
        AppBindData appBindData = data;
        sendPreloadMessage(7, null, 0);
        if (Jlog.isPerfTest()) {
            Jlog.i(3038, Jlog.getMessage(TAG, "handleBindApplication", "pid=" + Process.myPid()));
        }
        VMRuntime.registerSensitiveThread();
        if (appBindData.trackAllocation) {
            DdmVmInternal.enableRecentAllocations(true);
        }
        Process.setStartTimes(SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        this.mBoundApplication = appBindData;
        this.mConfiguration = new Configuration(appBindData.config);
        this.mCompatConfiguration = new Configuration(appBindData.config);
        this.mProfiler = new Profiler();
        String agent = null;
        if (appBindData.initProfilerInfo != null) {
            this.mProfiler.profileFile = appBindData.initProfilerInfo.profileFile;
            this.mProfiler.profileFd = appBindData.initProfilerInfo.profileFd;
            this.mProfiler.samplingInterval = appBindData.initProfilerInfo.samplingInterval;
            this.mProfiler.autoStopProfiler = appBindData.initProfilerInfo.autoStopProfiler;
            this.mProfiler.streamingOutput = appBindData.initProfilerInfo.streamingOutput;
            if (appBindData.initProfilerInfo.attachAgentDuringBind) {
                agent = appBindData.initProfilerInfo.agent;
            }
        }
        String agent2 = agent;
        if (USE_CACHE) {
            sendPreloadMessage(6, null, 0);
        }
        sendPreloadMessage(5, currentPackageName(), 0);
        Process.setArgV0(appBindData.processName);
        DdmHandleAppName.setAppName(appBindData.processName, UserHandle.myUserId());
        VMRuntime.setProcessPackageName(appBindData.appInfo.packageName);
        if (this.mProfiler.profileFd != null) {
            this.mProfiler.startProfiling();
        }
        if (appBindData.appInfo.targetSdkVersion <= 12) {
            AsyncTask.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        Message.updateCheckRecycle(appBindData.appInfo.targetSdkVersion);
        ImageDecoder.sApiLevel = appBindData.appInfo.targetSdkVersion;
        TimeZone.setDefault(null);
        LocaleList.setDefault(appBindData.config.getLocales());
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyConfigurationToResourcesLocked(appBindData.config, appBindData.compatInfo);
            this.mCurDefaultDisplayDpi = appBindData.config.densityDpi;
            applyCompatConfiguration(this.mCurDefaultDisplayDpi);
        }
        appBindData.info = getPackageInfoNoCheck(appBindData.appInfo, appBindData.compatInfo);
        if (agent2 != null) {
            handleAttachAgent(agent2, appBindData.info);
        }
        if ((appBindData.appInfo.flags & 8192) == 0) {
            this.mDensityCompatMode = true;
            Bitmap.setDefaultDensity(160);
        }
        updateDefaultDensity();
        String use24HourSetting = this.mCoreSettings.getString("time_12_24");
        Boolean is24Hr = null;
        if (use24HourSetting != null) {
            is24Hr = "24".equals(use24HourSetting) ? Boolean.TRUE : Boolean.FALSE;
        }
        Boolean is24Hr2 = is24Hr;
        DateFormat.set24HourTimePref(is24Hr2);
        View.mDebugViewAttributes = this.mCoreSettings.getInt("debug_view_attributes", 0) != 0;
        StrictMode.initThreadDefaults(appBindData.appInfo);
        StrictMode.initVmDefaults(appBindData.appInfo);
        sendPreloadMessage(1, appBindData, 0);
        try {
            Field field = Build.class.getDeclaredField("SERIAL");
            field.setAccessible(true);
            field.set(Build.class, appBindData.buildSerial);
        } catch (IllegalAccessException | NoSuchFieldException e) {
        }
        sendPreloadMessage(2, appBindData, 0);
        if (appBindData.debugMode != 0) {
            Debug.changeDebugPort(8100);
            if (appBindData.debugMode == 2) {
                Slog.w(TAG, "Application " + appBindData.info.getPackageName() + " is waiting for the debugger on port 8100...");
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
                Slog.w(TAG, "Application " + appBindData.info.getPackageName() + " can be debugged on port 8100...");
            }
        }
        boolean isAppDebuggable = (2 & appBindData.appInfo.flags) != 0;
        Trace.setAppTracingAllowed(isAppDebuggable);
        ThreadedRenderer.setDebuggingEnabled(isAppDebuggable || Build.IS_DEBUGGABLE);
        if (isAppDebuggable && appBindData.enableBinderTracking) {
            Binder.enableTracing();
        }
        Trace.traceBegin(64, "Setup proxies");
        IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
        if (b != null) {
            try {
                Proxy.setHttpProxySystemProperty(IConnectivityManager.Stub.asInterface(b).getProxyForNetwork(null));
            } catch (RemoteException e2) {
                Trace.traceEnd(64);
                throw e2.rethrowFromSystemServer();
            }
        }
        Trace.traceEnd(64);
        if (appBindData.instrumentationName != null) {
            try {
                ii = new ApplicationPackageManager(null, getPackageManager()).getInstrumentationInfo(appBindData.instrumentationName, 0);
                if (!Objects.equals(appBindData.appInfo.primaryCpuAbi, ii.primaryCpuAbi) || !Objects.equals(appBindData.appInfo.secondaryCpuAbi, ii.secondaryCpuAbi)) {
                    Slog.w(TAG, "Package uses different ABI(s) than its instrumentation: package[" + appBindData.appInfo.packageName + "]: " + appBindData.appInfo.primaryCpuAbi + ", " + appBindData.appInfo.secondaryCpuAbi + " instrumentation[" + ii.packageName + "]: " + ii.primaryCpuAbi + ", " + ii.secondaryCpuAbi);
                }
                this.mInstrumentationPackageName = ii.packageName;
                this.mInstrumentationAppDir = ii.sourceDir;
                this.mInstrumentationSplitAppDirs = ii.splitSourceDirs;
                this.mInstrumentationLibDir = getInstrumentationLibrary(appBindData.appInfo, ii);
                this.mInstrumentedAppDir = appBindData.info.getAppDir();
                this.mInstrumentedSplitAppDirs = appBindData.info.getSplitAppDirs();
                this.mInstrumentedLibDir = appBindData.info.getLibDir();
            } catch (PackageManager.NameNotFoundException e3) {
                throw new RuntimeException("Unable to find instrumentation info for: " + appBindData.instrumentationName);
            }
        } else {
            ii = null;
            this.mIsNeedStartUiProbe = true;
        }
        InstrumentationInfo ii4 = ii;
        if (sIsMygote) {
            VMRuntime.getRuntime();
            ii2 = ii4;
            VMRuntime.setAppInfo(appBindData.appInfo.dataDir, appBindData.appInfo.longVersionCode, appBindData.persistent);
        } else {
            ii2 = ii4;
        }
        ContextImpl appContext = ContextImpl.createAppContext(this, appBindData.info);
        updateLocaleListFromAppContext(appContext, this.mResourcesManager.getConfiguration().getLocales());
        if (!Process.isIsolated()) {
            int oldMask = StrictMode.allowThreadDiskWritesMask();
            try {
                setupGraphicsSupport(appContext);
                StrictMode.setThreadPolicyMask(oldMask);
            } catch (Throwable th) {
                StrictMode.setThreadPolicyMask(oldMask);
                throw th;
            }
        } else {
            ThreadedRenderer.setIsolatedProcess(true);
        }
        if (SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false)) {
            BaseDexClassLoader.setReporter(DexLoadReporter.getInstance());
        }
        Trace.traceBegin(64, "NetworkSecurityConfigProvider.install");
        NetworkSecurityConfigProvider.install(appContext);
        Trace.traceEnd(64);
        if (ii2 != null) {
            try {
                ii3 = ii2;
                try {
                    instrApp = getPackageManager().getApplicationInfo(ii3.packageName, 0, UserHandle.myUserId());
                } catch (RemoteException e4) {
                    instrApp = null;
                    if (instrApp == null) {
                    }
                    ApplicationInfo instrApp2 = instrApp;
                    ii3.copyTo(instrApp2);
                    instrApp2.initForUser(UserHandle.myUserId());
                    IBinder iBinder = b;
                    boolean z = isAppDebuggable;
                    Boolean bool = is24Hr2;
                    pi = getPackageInfo(instrApp2, appBindData.compatInfo, appContext.getClassLoader(), false, true, false);
                    instrContext = ContextImpl.createAppContext(this, pi);
                    this.mInstrumentation = (Instrumentation) instrContext.getClassLoader().loadClass(appBindData.instrumentationName.getClassName()).newInstance();
                    ContextImpl contextImpl = instrContext;
                    LoadedApk loadedApk = pi;
                    this.mInstrumentation.init(this, instrContext, appContext, new ComponentName(ii3.packageName, ii3.name), appBindData.instrumentationWatcher, appBindData.instrumentationUiAutomationConnection);
                    this.mProfiler.handlingProfiling = true;
                    File file = new File(this.mProfiler.profileFile);
                    file.getParentFile().mkdirs();
                    Debug.startMethodTracing(file.toString(), 8388608);
                    if ((appBindData.appInfo.flags & 1048576) != 0) {
                    }
                    savedPolicy = StrictMode.allowThreadDiskWrites();
                    StrictMode.ThreadPolicy writesAllowedPolicy = StrictMode.getThreadPolicy();
                    app = appBindData.info.makeApplication(appBindData.restrictedBackupMode, null);
                    app.setAutofillCompatibilityEnabled(appBindData.autofillCompatibilityEnabled);
                    this.mInitialApplication = app;
                    sendPreloadMessage(8, app, 0);
                    installContentProviders(app, appBindData.providers);
                    this.mH.sendEmptyMessageDelayed(132, JobInfo.MIN_BACKOFF_MILLIS);
                    this.mInstrumentation.onCreate(appBindData.instrumentationArgs);
                    this.mInstrumentation.callApplicationOnCreate(app);
                    sendPreloadMessage(4, app, 0);
                    HwThemeManager.initForThemeFont(this.mConfiguration);
                    HwThemeManager.setThemeFont();
                    if (!mChangedFont) {
                    }
                    StrictMode.setThreadPolicy(savedPolicy);
                    Application application = app;
                    if (Jlog.isPerfTest()) {
                    }
                    FontsContract.setApplicationContextForResources(appContext);
                    if (!Process.isIsolated()) {
                    }
                    if (sIsMygote) {
                    }
                }
            } catch (RemoteException e5) {
                ii3 = ii2;
                instrApp = null;
                if (instrApp == null) {
                }
                ApplicationInfo instrApp22 = instrApp;
                ii3.copyTo(instrApp22);
                instrApp22.initForUser(UserHandle.myUserId());
                IBinder iBinder2 = b;
                boolean z2 = isAppDebuggable;
                Boolean bool2 = is24Hr2;
                pi = getPackageInfo(instrApp22, appBindData.compatInfo, appContext.getClassLoader(), false, true, false);
                instrContext = ContextImpl.createAppContext(this, pi);
                this.mInstrumentation = (Instrumentation) instrContext.getClassLoader().loadClass(appBindData.instrumentationName.getClassName()).newInstance();
                ContextImpl contextImpl2 = instrContext;
                LoadedApk loadedApk2 = pi;
                this.mInstrumentation.init(this, instrContext, appContext, new ComponentName(ii3.packageName, ii3.name), appBindData.instrumentationWatcher, appBindData.instrumentationUiAutomationConnection);
                this.mProfiler.handlingProfiling = true;
                File file2 = new File(this.mProfiler.profileFile);
                file2.getParentFile().mkdirs();
                Debug.startMethodTracing(file2.toString(), 8388608);
                if ((appBindData.appInfo.flags & 1048576) != 0) {
                }
                savedPolicy = StrictMode.allowThreadDiskWrites();
                StrictMode.ThreadPolicy writesAllowedPolicy2 = StrictMode.getThreadPolicy();
                app = appBindData.info.makeApplication(appBindData.restrictedBackupMode, null);
                app.setAutofillCompatibilityEnabled(appBindData.autofillCompatibilityEnabled);
                this.mInitialApplication = app;
                sendPreloadMessage(8, app, 0);
                installContentProviders(app, appBindData.providers);
                this.mH.sendEmptyMessageDelayed(132, JobInfo.MIN_BACKOFF_MILLIS);
                this.mInstrumentation.onCreate(appBindData.instrumentationArgs);
                this.mInstrumentation.callApplicationOnCreate(app);
                sendPreloadMessage(4, app, 0);
                HwThemeManager.initForThemeFont(this.mConfiguration);
                HwThemeManager.setThemeFont();
                if (!mChangedFont) {
                }
                StrictMode.setThreadPolicy(savedPolicy);
                Application application2 = app;
                if (Jlog.isPerfTest()) {
                }
                FontsContract.setApplicationContextForResources(appContext);
                if (!Process.isIsolated()) {
                }
                if (sIsMygote) {
                }
            }
            if (instrApp == null) {
                instrApp = new ApplicationInfo();
            }
            ApplicationInfo instrApp222 = instrApp;
            ii3.copyTo(instrApp222);
            instrApp222.initForUser(UserHandle.myUserId());
            IBinder iBinder22 = b;
            boolean z22 = isAppDebuggable;
            Boolean bool22 = is24Hr2;
            pi = getPackageInfo(instrApp222, appBindData.compatInfo, appContext.getClassLoader(), false, true, false);
            instrContext = ContextImpl.createAppContext(this, pi);
            try {
                this.mInstrumentation = (Instrumentation) instrContext.getClassLoader().loadClass(appBindData.instrumentationName.getClassName()).newInstance();
                ContextImpl contextImpl22 = instrContext;
                LoadedApk loadedApk22 = pi;
                this.mInstrumentation.init(this, instrContext, appContext, new ComponentName(ii3.packageName, ii3.name), appBindData.instrumentationWatcher, appBindData.instrumentationUiAutomationConnection);
                if (this.mProfiler.profileFile != null && !ii3.handleProfiling && this.mProfiler.profileFd == null) {
                    this.mProfiler.handlingProfiling = true;
                    File file22 = new File(this.mProfiler.profileFile);
                    file22.getParentFile().mkdirs();
                    Debug.startMethodTracing(file22.toString(), 8388608);
                }
            } catch (Exception e6) {
                ContextImpl contextImpl3 = instrContext;
                LoadedApk loadedApk3 = pi;
                throw new RuntimeException("Unable to instantiate instrumentation " + appBindData.instrumentationName + ": " + e6.toString(), e6);
            }
        } else {
            boolean z3 = isAppDebuggable;
            InstrumentationInfo instrumentationInfo = ii2;
            Boolean bool3 = is24Hr2;
            this.mInstrumentation = new Instrumentation();
            this.mInstrumentation.basicInit(this);
        }
        if ((appBindData.appInfo.flags & 1048576) != 0) {
            VMRuntime.getRuntime().clearGrowthLimit();
        } else {
            VMRuntime.getRuntime().clampGrowthLimit();
        }
        savedPolicy = StrictMode.allowThreadDiskWrites();
        StrictMode.ThreadPolicy writesAllowedPolicy22 = StrictMode.getThreadPolicy();
        try {
            app = appBindData.info.makeApplication(appBindData.restrictedBackupMode, null);
            app.setAutofillCompatibilityEnabled(appBindData.autofillCompatibilityEnabled);
            this.mInitialApplication = app;
            sendPreloadMessage(8, app, 0);
            if (!appBindData.restrictedBackupMode && !ArrayUtils.isEmpty(appBindData.providers)) {
                installContentProviders(app, appBindData.providers);
                this.mH.sendEmptyMessageDelayed(132, JobInfo.MIN_BACKOFF_MILLIS);
            }
            this.mInstrumentation.onCreate(appBindData.instrumentationArgs);
            this.mInstrumentation.callApplicationOnCreate(app);
            sendPreloadMessage(4, app, 0);
            HwThemeManager.initForThemeFont(this.mConfiguration);
            HwThemeManager.setThemeFont();
            if (!mChangedFont) {
                mChangedFont = true;
                Typeface.loadSystemFonts();
            }
        } catch (Exception e7) {
            throw new RuntimeException("Exception thrown in onCreate() of " + appBindData.instrumentationName + ": " + e7.toString(), e7);
        } catch (Exception e8) {
            if (!this.mInstrumentation.onException(app, e8)) {
                throw new RuntimeException("Unable to create application " + app.getClass().getName() + ": " + e8.toString(), e8);
            }
        } catch (Throwable e9) {
            if (appBindData.appInfo.targetSdkVersion < 27 || StrictMode.getThreadPolicy().equals(writesAllowedPolicy22)) {
                StrictMode.setThreadPolicy(savedPolicy);
            }
            throw e9;
        }
        if (appBindData.appInfo.targetSdkVersion < 27 || StrictMode.getThreadPolicy().equals(writesAllowedPolicy22)) {
            StrictMode.setThreadPolicy(savedPolicy);
        }
        Application application22 = app;
        if (Jlog.isPerfTest()) {
            Jlog.i(3039, Jlog.getMessage(TAG, "handleBindApplication", "pid=" + Process.myPid()));
        }
        FontsContract.setApplicationContextForResources(appContext);
        if (!Process.isIsolated()) {
            try {
                ApplicationInfo info = getPackageManager().getApplicationInfo(appBindData.appInfo.packageName, 128, UserHandle.myUserId());
                if (!(info == null || info.metaData == null)) {
                    int preloadedFontsResource = info.metaData.getInt(ApplicationInfo.METADATA_PRELOADED_FONTS, 0);
                    if (preloadedFontsResource != 0) {
                        appBindData.info.getResources().preloadFonts(preloadedFontsResource);
                    }
                }
            } catch (RemoteException e10) {
                throw e10.rethrowFromSystemServer();
            }
        }
        if (sIsMygote) {
            this.mH.sendEmptyMessageDelayed(1007, 0);
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        if (r4 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        android.util.Slog.e(TAG, "Failed to find provider info for " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003c, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        return installProvider(r15, r4, r4.info, true, r4.noReleaseNeeded, r13).provider;
     */
    public final IContentProvider acquireProvider(Context c, String auth, int userId, boolean stable) {
        String str = auth;
        int i = userId;
        IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
        if (provider != null) {
            return provider;
        }
        try {
            synchronized (getGetProviderLock(str, i)) {
                try {
                    boolean z = stable;
                    try {
                        ContentProviderHolder holder = ActivityManager.getService().getContentProvider(getApplicationThread(), str, i, z);
                    } catch (Throwable th) {
                        th = th;
                        try {
                            throw th;
                        } catch (RemoteException e) {
                            ex = e;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    boolean z2 = stable;
                    throw th;
                }
            }
        } catch (RemoteException e2) {
            ex = e2;
            boolean z3 = stable;
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
        int unstableDelta = 0;
        if (stable) {
            prc.stableCount++;
            if (prc.stableCount == 1) {
                if (prc.removePending) {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "incProviderRef: stable snatched provider from the jaws of death");
                    }
                    prc.removePending = false;
                    this.mH.removeMessages(131, prc);
                    unstableDelta = -1;
                }
                int unstableDelta2 = unstableDelta;
                try {
                    if (DEBUG_PROVIDER != 0) {
                        Slog.v(TAG, "incProviderRef Now stable - " + prc.holder.info.name + ": unstableDelta=" + unstableDelta2);
                    }
                    ActivityManager.getService().refContentProvider(prc.holder.connection, 1, unstableDelta2);
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0058, code lost:
        return r4;
     */
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
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002d, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008c, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0122, code lost:
        return true;
     */
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
                if (prc.stableCount != 0) {
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
                } else if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "releaseProvider: stable ref count already 0, how?");
                }
            } else if (prc.unstableCount != 0) {
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
            } else if (DEBUG_PROVIDER) {
                Slog.v(TAG, "releaseProvider: unstable ref count already 0, how?");
            }
            if (lastRef) {
                if (!prc.removePending) {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "releaseProvider: Enqueueing pending removal - " + prc.holder.info.name);
                    }
                    prc.removePending = true;
                    this.mH.sendMessage(this.mH.obtainMessage(131, prc));
                } else {
                    Slog.w(TAG, "Duplicate remove pending of provider " + prc.holder.info.name);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0055, code lost:
        if (DEBUG_PROVIDER == false) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0057, code lost:
        android.util.Slog.v(TAG, "removeProvider: Invoking ActivityManagerService.removeContentProvider(" + r9.holder.info.name + ")");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0079, code lost:
        android.app.ActivityManager.getService().removeContentProvider(r9.holder.connection, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        return;
     */
    public final void completeRemoveProvider(ProviderRefCount prc) {
        synchronized (this.mProviderMap) {
            if (prc.removePending) {
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
                prc.removePending = false;
            } else if (DEBUG_PROVIDER) {
                Slog.v(TAG, "completeRemoveProvider: lost the race, provider still in use");
            }
        }
    }

    /* access modifiers changed from: package-private */
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
                Slog.v(TAG, "Cleaning up dead provider " + provider + " " + prc.holder.info.name);
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
                        if (auth.equals("com.android.calendar")) {
                            c = 4;
                            break;
                        }
                        break;
                    case -172298781:
                        if (auth.equals("call_log")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 63943420:
                        if (auth.equals("call_log_shadow")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 783201304:
                        if (auth.equals("telephony")) {
                            c = 6;
                            break;
                        }
                        break;
                    case 1312704747:
                        if (auth.equals("downloads")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1995645513:
                        if (auth.equals("com.android.blockednumber")) {
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

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00dd  */
    private ContentProviderHolder installProvider(Context context, ContentProviderHolder holder, ProviderInfo info, boolean noisy, boolean noReleaseNeeded, boolean stable) {
        IContentProvider provider;
        ContentProviderHolder retHolder;
        ProviderRefCount providerRefCount;
        ContentProviderHolder contentProviderHolder = holder;
        ProviderInfo providerInfo = info;
        boolean z = stable;
        ContentProvider localProvider = null;
        if (contentProviderHolder == null || contentProviderHolder.provider == null) {
            if (DEBUG_PROVIDER || noisy) {
                Slog.d(TAG, "Loading provider " + providerInfo.authority + ": " + providerInfo.name);
            }
            Context c = null;
            ApplicationInfo ai = providerInfo.applicationInfo;
            if (context.getPackageName().equals(ai.packageName)) {
                c = context;
            } else if (this.mInitialApplication == null || !this.mInitialApplication.getPackageName().equals(ai.packageName)) {
                try {
                    try {
                        c = context.createPackageContext(ai.packageName, 1);
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.w(TAG, "Unable to get context for package " + ai.packageName);
                        if (c != null) {
                        }
                    }
                } catch (PackageManager.NameNotFoundException e2) {
                    Context context2 = context;
                    Slog.w(TAG, "Unable to get context for package " + ai.packageName);
                    if (c != null) {
                    }
                }
                if (c != null) {
                    Slog.w(TAG, "Unable to get context for package " + ai.packageName + " while loading content provider " + providerInfo.name);
                    return null;
                }
                if (providerInfo.splitName != null) {
                    try {
                        c = c.createContextForSplit(providerInfo.splitName);
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
                    localProvider = packageInfo.getAppFactory().instantiateProvider(cl, providerInfo.name);
                    IContentProvider provider2 = localProvider.getIContentProvider();
                    if (provider2 == null) {
                        Slog.e(TAG, "Failed to instantiate class " + providerInfo.name + " from sourceDir " + providerInfo.applicationInfo.sourceDir);
                        return null;
                    }
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "Instantiating local provider " + providerInfo.name);
                    }
                    localProvider.attachInfo(c, providerInfo);
                    provider = provider2;
                } catch (Exception e4) {
                    if (this.mInstrumentation.onException(null, e4)) {
                        return null;
                    }
                    throw new RuntimeException("Unable to get provider " + providerInfo.name + ": " + e4.toString(), e4);
                }
            } else {
                c = this.mInitialApplication;
            }
            Context context3 = context;
            if (c != null) {
            }
        } else {
            provider = contentProviderHolder.provider;
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "Installing external provider " + providerInfo.authority + ": " + providerInfo.name);
            }
            Context context4 = context;
        }
        ContentProvider localProvider2 = localProvider;
        IContentProvider provider3 = provider;
        if (providerInfo.applicationInfo.uid > 10000 && !context.getPackageName().equals(providerInfo.applicationInfo.packageName)) {
            Utils.handleTimeOut("acquire_provider", providerInfo.applicationInfo.packageName, "");
        }
        synchronized (this.mProviderMap) {
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "Checking to add " + provider3 + " / " + providerInfo.name);
            }
            IBinder jBinder = provider3.asBinder();
            if (localProvider2 != null) {
                ComponentName cname = new ComponentName(providerInfo.packageName, providerInfo.name);
                ProviderClientRecord pr = this.mLocalProvidersByName.get(cname);
                if (pr != null) {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "installProvider: lost the race, using existing local provider");
                    }
                    IContentProvider provider4 = pr.mProvider;
                } else {
                    ContentProviderHolder holder2 = new ContentProviderHolder(providerInfo);
                    holder2.provider = provider3;
                    holder2.noReleaseNeeded = true;
                    pr = installProviderAuthoritiesLocked(provider3, localProvider2, holder2);
                    this.mLocalProviders.put(jBinder, pr);
                    this.mLocalProvidersByName.put(cname, pr);
                }
                retHolder = pr.mHolder;
            } else {
                ProviderRefCount prc = this.mProviderRefCountMap.get(jBinder);
                if (prc != null) {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "installProvider: lost the race, updating ref count");
                    }
                    if (!noReleaseNeeded) {
                        incProviderRefLocked(prc, z);
                        try {
                            ActivityManager.getService().removeContentProvider(contentProviderHolder.connection, z);
                        } catch (RemoteException e5) {
                            Log.e(TAG, "installProvider()");
                        }
                    }
                } else {
                    ProviderClientRecord client = installProviderAuthoritiesLocked(provider3, localProvider2, contentProviderHolder);
                    if (noReleaseNeeded) {
                        prc = new ProviderRefCount(contentProviderHolder, client, 1000, 1000);
                    } else {
                        if (z) {
                            providerRefCount = new ProviderRefCount(contentProviderHolder, client, 1, 0);
                        } else {
                            providerRefCount = new ProviderRefCount(contentProviderHolder, client, 0, 1);
                        }
                        prc = providerRefCount;
                    }
                    this.mProviderRefCountMap.put(jBinder, prc);
                }
                retHolder = prc.holder;
            }
        }
        return retHolder;
    }

    /* access modifiers changed from: private */
    public void handleRunIsolatedEntryPoint(String entryPoint, String[] entryPointArgs) {
        try {
            Class.forName(entryPoint).getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{entryPointArgs});
            System.exit(0);
        } catch (ReflectiveOperationException e) {
            throw new AndroidRuntimeException("runIsolatedEntryPoint failed", e);
        }
    }

    private void attach(boolean system, long startSeq) {
        sCurrentActivityThread = this;
        this.mSystemThread = system;
        if (!system) {
            ViewRootImpl.addFirstDrawHandler(new Runnable() {
                public void run() {
                    ActivityThread.this.ensureJitEnabled();
                }
            });
            DdmHandleAppName.setAppName("<pre-initialized>", UserHandle.myUserId());
            RuntimeInit.setApplicationObject(this.mAppThread.asBinder());
            final IActivityManager mgr = ActivityManager.getService();
            try {
                Slog.d(TAG, "Attach thread to application");
                if (Jlog.isPerfTest()) {
                    Jlog.i(3033, Jlog.getMessage(TAG, "attach", "pid=" + Process.myPid()));
                }
                mgr.attachApplication(this.mAppThread, startSeq);
                BinderInternal.addGcWatcher(new Runnable() {
                    public void run() {
                        if (ActivityThread.this.mSomeActivitiesChanged) {
                            Runtime runtime = Runtime.getRuntime();
                            if (runtime.totalMemory() - runtime.freeMemory() > (3 * runtime.maxMemory()) / 4) {
                                ActivityThread.this.mSomeActivitiesChanged = false;
                                try {
                                    mgr.releaseSomeActivities(ActivityThread.this.mAppThread);
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
                if (getApplicationThread() != null) {
                    getApplicationThread().scheduleTrimMemory(80);
                }
                Typeface.loadSystemFonts();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate Application():" + e.toString(), e);
            }
        }
        DropBox.setReporter(new DropBoxReporter());
        ViewRootImpl.addConfigCallback(new ViewRootImpl.ConfigChangedCallback() {
            public final void onConfigurationChanged(Configuration configuration) {
                ActivityThread.lambda$attach$0(ActivityThread.this, configuration);
            }
        });
    }

    public static /* synthetic */ void lambda$attach$0(ActivityThread activityThread, Configuration globalConfig) {
        synchronized (activityThread.mResourcesManager) {
            Configuration globalConfig2 = activityThread.updateConfig(globalConfig);
            if (activityThread.mResourcesManager.applyConfigurationToResourcesLocked(globalConfig2, null)) {
                activityThread.updateLocaleListFromAppContext(activityThread.mInitialApplication.getApplicationContext(), activityThread.mResourcesManager.getConfiguration().getLocales());
                if (activityThread.mPendingConfiguration == null || activityThread.mPendingConfiguration.isOtherSeqNewer(globalConfig2)) {
                    activityThread.mPendingConfiguration = new Configuration(globalConfig2);
                    activityThread.sendMessage(118, globalConfig2);
                }
            }
        }
    }

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
            int i = this.mCoreSettings.getInt(key, defaultValue);
            return i;
        }
    }

    public static void main(String[] args) {
        Trace.traceBegin(64, "ActivityThreadMain");
        if (Jlog.isMicroTest()) {
            Jlog.i(3088, Jlog.getMessage(TAG, "main", Integer.valueOf(Process.myPid())));
        }
        CloseGuard.setEnabled(false);
        Environment.initForCurrentUser();
        Log.initHWLog();
        EventLogger.setReporter(new EventLoggingReporter());
        TrustedCertificateStore.setDefaultUserDirectory(Environment.getUserConfigDirectory(UserHandle.myUserId()));
        Process.setArgV0("<pre-initialized>");
        Looper.prepareMainLooper();
        long startSeq = 0;
        ArrayList<String> displayArgs = new ArrayList<>();
        if (args != null) {
            for (int i = args.length - 1; i >= 0; i--) {
                if (args[i] != null && args[i].startsWith(PROC_START_SEQ_IDENT)) {
                    startSeq = Long.parseLong(args[i].substring(PROC_START_SEQ_IDENT.length()));
                } else if (args[i] != null) {
                    displayArgs.add(0, args[i]);
                }
            }
        }
        ActivityThread thread = new ActivityThread();
        if (!initVRArgs(thread, (String[]) displayArgs.toArray(new String[displayArgs.size()]))) {
            initPCArgs(thread, (String[]) displayArgs.toArray(new String[displayArgs.size()]));
        }
        thread.attach(false, startSeq);
        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
        if (Jlog.isMicroTest()) {
            Jlog.i(3089, Jlog.getMessage(TAG, "main", Integer.valueOf(Process.myPid())));
        }
        Trace.traceEnd(64);
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }

    public void handlePCWindowStateChanged(IBinder token, int windowState) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null && (r.window instanceof AbsWindow)) {
            r.window.onWindowStateChanged(windowState);
        }
    }

    public void handleFreeFormOutLineChanged(IBinder token, int state) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null) {
            r.window.onFreeFormOutLineChanged(state);
        }
    }

    public void handleRestoreFreeFormConfig(IBinder token) {
        ActivityClientRecord r = this.mActivities.get(token);
        if (r != null && r.window != null) {
            r.window.restoreFreeFormConfig();
        }
    }

    private void setInitParam(int displayId, int width, int height) {
        this.mDisplayId = displayId;
        HwPCUtils.setPCDisplayID(displayId);
        if (width > 0 && height > 0) {
            Display display = this.mResourcesManager.getAdjustedDisplay(displayId, Resources.getSystem());
            if (display != null) {
                DisplayMetrics dm = new DisplayMetrics();
                display.getMetrics(dm);
                if (this.mOverrideConfig == null) {
                    this.mOverrideConfig = new Configuration();
                }
                dm.widthPixels = width;
                dm.heightPixels = height;
                this.mOverrideConfig.touchscreen = 1;
                this.mOverrideConfig.densityDpi = dm.densityDpi;
                Configuration configuration = this.mOverrideConfig;
                Configuration configuration2 = this.mOverrideConfig;
                int i = (int) (((float) dm.widthPixels) / dm.density);
                configuration2.screenWidthDp = i;
                configuration.compatScreenWidthDp = i;
                Configuration configuration3 = this.mOverrideConfig;
                Configuration configuration4 = this.mOverrideConfig;
                int i2 = (int) (((float) dm.heightPixels) / dm.density);
                configuration4.screenHeightDp = i2;
                configuration3.compatScreenHeightDp = i2;
                Configuration configuration5 = this.mOverrideConfig;
                Configuration configuration6 = this.mOverrideConfig;
                int i3 = this.mOverrideConfig.screenWidthDp > this.mOverrideConfig.screenHeightDp ? this.mOverrideConfig.screenHeightDp : this.mOverrideConfig.screenWidthDp;
                configuration6.smallestScreenWidthDp = i3;
                configuration5.compatSmallestScreenWidthDp = i3;
                int sl = Configuration.resetScreenLayout(this.mOverrideConfig.screenLayout);
                if (dm.widthPixels > dm.heightPixels) {
                    this.mOverrideConfig.orientation = 2;
                    this.mOverrideConfig.screenLayout = Configuration.reduceScreenLayout(sl, this.mOverrideConfig.screenWidthDp, this.mOverrideConfig.screenHeightDp);
                } else {
                    this.mOverrideConfig.orientation = 1;
                    this.mOverrideConfig.screenLayout = Configuration.reduceScreenLayout(sl, this.mOverrideConfig.screenHeightDp, this.mOverrideConfig.screenWidthDp);
                }
            }
        }
    }

    public Configuration getOverrideConfig() {
        return this.mOverrideConfig;
    }

    public void updateOverrideConfig(Configuration config) {
        if (config != null && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            if (this.mOverrideConfig != null && !this.mOverrideConfig.equals(config)) {
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

    /* access modifiers changed from: private */
    public Configuration updateConfig(Configuration config) {
        if (config != null && HwPCUtils.isValidExtDisplayId(this.mDisplayId) && this.mOverrideConfig != null && !this.mOverrideConfig.equals(Configuration.EMPTY)) {
            config = new Configuration(config);
            config.updateFrom(this.mOverrideConfig);
            if (!(this.mConfiguration == null || new Configuration(this.mConfiguration).updateFrom(config) == 0)) {
                config.seq = 0;
            }
        }
        return config;
    }

    private static void initPCArgs(ActivityThread thread, String[] args) {
        if (HwPCUtils.enabled() && args != null) {
            if (args.length == 3 || args.length == 1 || args.length == 2) {
                try {
                    if (Integer.parseInt(args[0]) > 0) {
                        if (args.length == 1) {
                            thread.setInitParam(Integer.parseInt(args[0]), 0, 0);
                        } else if (args.length == 2) {
                            thread.setInitParam(Integer.parseInt(args[0]), 0, 0);
                            HwPCUtils.mTouchDeviceID = Integer.parseInt(args[1]);
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

    private static boolean initVRArgs(ActivityThread thread, String[] args) {
        if (!(thread == null || args == null || args.length != 3)) {
            try {
                int displayId = Integer.parseInt(args[0]);
                if (HwFrameworkFactory.getVRSystemServiceManager().isVRDisplay(displayId, Integer.parseInt(args[1]), Integer.parseInt(args[2]))) {
                    Slog.i(TAG, "initVRArgs displayid =" + displayId);
                    thread.setVRInitParam(displayId);
                    return true;
                }
            } catch (NumberFormatException e) {
                Slog.i(TAG, "args format error.");
            }
        }
        return false;
    }

    private void setVRInitParam(int displayId) {
        this.mDisplayId = displayId;
        HwFrameworkFactory.getVRSystemServiceManager().setVRDisplayID(displayId, true);
    }

    private final void updateNavigationBarColor(Activity activity) {
        if ((SystemProperties.getInt("persist.sys.navigationbar.mode", 0) & 2) != 0) {
            this.mH.removeMessages(1006);
            sendMessage(1006, activity);
        }
    }
}
