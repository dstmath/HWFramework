package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.Dialog;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IActivityTaskManager;
import android.app.IApplicationThread;
import android.app.IAssistDataReceiver;
import android.app.IHwActivityNotifier;
import android.app.IHwDockCallBack;
import android.app.INotificationManager;
import android.app.IRequestFinishCallback;
import android.app.ITaskStackListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.ProfilerInfo;
import android.app.RemoteAction;
import android.app.WaitResult;
import android.app.WindowConfiguration;
import android.app.admin.DevicePolicyCache;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hwtheme.HwThemeManager;
import android.iawareperf.UniPerf;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.IUserManager;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UpdateLock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.WorkSource;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.VoiceInteractionManagerInternal;
import android.sysprop.DisplayProperties;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.SplitNotificationUtils;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.IRecentsAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.inputmethod.InputMethodSystemProperty;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.TransferPipe;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.KeyguardDismissCallback;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.AttributeCache;
import com.android.server.DeviceIdleController;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.BaseErrorDialog;
import com.android.server.am.EventLogTags;
import com.android.server.am.PendingIntentController;
import com.android.server.am.PendingIntentRecord;
import com.android.server.am.UserState;
import com.android.server.appop.AppOpsService;
import com.android.server.firewall.IntentFirewall;
import com.android.server.os.HwBootFail;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.PermissionPolicyInternal;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.uri.UriGrantsManagerInternal;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IGameObserverEx;
import com.huawei.android.app.IHwActivityTaskManager;
import com.huawei.android.app.IHwAtmDAMonitorCallback;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ActivityTaskManagerService extends IActivityTaskManager.Stub implements IHwActivityTaskManagerInner {
    static final long ACTIVITY_BG_START_GRACE_PERIOD_MS = 10000;
    public static final boolean ANIMATE = true;
    private static final long APP_SWITCH_DELAY_TIME = 5000;
    public static final String DUMP_ACTIVITIES_CMD = "activities";
    public static final String DUMP_ACTIVITIES_SHORT_CMD = "a";
    public static final String DUMP_CONTAINERS_CMD = "containers";
    public static final String DUMP_LASTANR_CMD = "lastanr";
    public static final String DUMP_LASTANR_TRACES_CMD = "lastanr-traces";
    public static final String DUMP_RECENTS_CMD = "recents";
    public static final String DUMP_RECENTS_SHORT_CMD = "r";
    public static final String DUMP_STARTER_CMD = "starter";
    static final int INSTRUMENTATION_KEY_DISPATCHING_TIMEOUT_MS = 60000;
    public static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    public static final int KEY_DISPATCHING_TIMEOUT_MS = 5000;
    private static final int PENDING_ASSIST_EXTRAS_LONG_TIMEOUT = 2000;
    private static final int PENDING_ASSIST_EXTRAS_TIMEOUT = 500;
    private static final int PENDING_AUTOFILL_ASSIST_STRUCTURE_TIMEOUT = 2000;
    public static final int RELAUNCH_REASON_FREE_RESIZE = 2;
    public static final int RELAUNCH_REASON_NONE = 0;
    public static final int RELAUNCH_REASON_WINDOWING_MODE_RESIZE = 1;
    private static final int SERVICE_LAUNCH_IDLE_WHITELIST_DURATION_MS = 5000;
    private static final long START_AS_CALLER_TOKEN_EXPIRED_TIMEOUT = 1802000;
    private static final long START_AS_CALLER_TOKEN_TIMEOUT = 600000;
    private static final long START_AS_CALLER_TOKEN_TIMEOUT_IMPL = 602000;
    static final int STOP_FREEZE_SCREEN = 10086;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_CONFIGURATION = ("ActivityTaskManager" + ActivityTaskManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = "ActivityTaskManager";
    private static final String TAG_IMMERSIVE = "ActivityTaskManager";
    private static final String TAG_KEYGUARD = "ActivityTaskManager_keyguard";
    private static final String TAG_LOCKTASK = "ActivityTaskManager";
    private static final String TAG_STACK = "ActivityTaskManager";
    private static final String TAG_SWITCH = "ActivityTaskManager";
    private static final String TAG_VISIBILITY = ("ActivityTaskManager" + ActivityTaskManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final int TIME_DELAY_STOP_FREEZ_SCREEN = 400;
    private static Set<String> sSkipFreezingWindowActivitys = new HashSet();
    final int GL_ES_VERSION;
    private final MirrorActiveUids mActiveUids = new MirrorActiveUids();
    ComponentName mActiveVoiceInteractionServiceComponent;
    boolean mActivityIdle = false;
    private ActivityStartController mActivityStartController;
    final SparseArray<ArrayMap<String, Integer>> mAllowAppSwitchUids = new SparseArray<>();
    ActivityManagerInternal mAmInternal;
    private AppOpsService mAppOpsService;
    private long mAppSwitchesAllowedTime;
    private AppWarnings mAppWarnings;
    private AssistUtils mAssistUtils;
    HwAtmDAMonitorProxy mAtmDAProxy = new HwAtmDAMonitorProxy();
    private final Map<Integer, Set<Integer>> mCompanionAppUidsMap = new ArrayMap();
    CompatModePackages mCompatModePackages;
    private int mConfigurationSeq;
    Context mContext;
    IActivityController mController = null;
    boolean mControllerIsAMonkey = false;
    AppTimeTracker mCurAppTimeTracker;
    private int mDeviceOwnerUid = -1;
    private boolean mDidAppSwitch;
    final ArrayList<IBinder> mExpiredStartAsCallerTokens = new ArrayList<>();
    final int mFactoryTest;
    private FontScaleSettingObserver mFontScaleSettingObserver;
    boolean mForceResizableActivities;
    private float mFullscreenThumbnailScale;
    final WindowManagerGlobalLock mGlobalLock = new WindowManagerGlobalLock();
    final Object mGlobalLockWithoutBoost = this.mGlobalLock;
    H mH;
    boolean mHasHeavyWeightFeature;
    WindowProcessController mHeavyWeightProcess = null;
    public WindowProcessController mHomeProcess;
    public IHwActivityTaskManagerServiceEx mHwATMSEx = null;
    HwInnerActivityTaskManagerService mHwInnerService = new HwInnerActivityTaskManagerService(this);
    public boolean mInFreeformSnapshot = false;
    IntentFirewall mIntentFirewall;
    @VisibleForTesting
    final ActivityTaskManagerInternal mInternal;
    KeyguardController mKeyguardController;
    private boolean mKeyguardShown = false;
    String mLastANRState;
    ActivityRecord mLastResumedActivity;
    private long mLastStopAppSwitchesTime;
    private final ClientLifecycleManager mLifecycleManager;
    private LockTaskController mLockTaskController;
    private final ArrayList<PendingAssistExtras> mPendingAssistExtras = new ArrayList<>();
    PendingIntentController mPendingIntentController;
    private final SparseArray<String> mPendingTempWhitelist = new SparseArray<>();
    private PermissionPolicyInternal mPermissionPolicyInternal;
    private PackageManagerInternal mPmInternal;
    PowerManagerInternal mPowerManagerInternal;
    WindowProcessController mPreviousProcess;
    long mPreviousProcessVisibleTime;
    final WindowProcessControllerMap mProcessMap = new WindowProcessControllerMap();
    final ProcessMap<WindowProcessController> mProcessNames = new ProcessMap<>();
    String mProfileApp = null;
    WindowProcessController mProfileProc = null;
    ProfilerInfo mProfilerInfo = null;
    RecentTasks mRecentTasks;
    RootActivityContainer mRootActivityContainer;
    IVoiceInteractionSession mRunningVoice;
    final List<ActivityTaskManagerInternal.ScreenObserver> mScreenObservers = new ArrayList();
    private boolean mShouldFreeze;
    private boolean mShowDialogs = true;
    boolean mShuttingDown = false;
    public boolean mSkipShowLauncher = false;
    private boolean mSleeping = false;
    public ActivityStackSupervisor mStackSupervisor;
    final HashMap<IBinder, IBinder> mStartActivitySources = new HashMap<>();
    int mStartFromSelector;
    final StringBuilder mStringBuilder = new StringBuilder(256);
    private String[] mSupportedSystemLocales = null;
    boolean mSupportsFreeformWindowManagement;
    boolean mSupportsMultiDisplay;
    boolean mSupportsMultiWindow;
    boolean mSupportsPictureInPicture;
    boolean mSupportsSplitScreenMultiWindow;
    boolean mSuppressResizeConfigChanges;
    final ActivityThread mSystemThread;
    private TaskChangeNotificationController mTaskChangeNotificationController;
    private Configuration mTempConfig = new Configuration();
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    private final UpdateConfigurationResult mTmpUpdateConfigurationResult = new UpdateConfigurationResult();
    String mTopAction = "android.intent.action.MAIN";
    ComponentName mTopComponent;
    String mTopData;
    int mTopProcessState = 2;
    private ActivityRecord mTracedResumedActivity;
    UriGrantsManagerInternal mUgmInternal;
    final Context mUiContext;
    UiHandler mUiHandler;
    private final UpdateLock mUpdateLock = new UpdateLock("immersive");
    protected UsageStatsManagerInternal mUsageStatsInternal;
    private UserManagerService mUserManager;
    UserManagerInternal mUserManagerInternal;
    private int mViSessionId = 1000;
    PowerManager.WakeLock mVoiceWakeLock;
    int mVr2dDisplayId = -1;
    VrController mVrController;
    IVRSystemServiceManager mVrMananger;
    protected boolean mWarmColdSwitch = false;
    WindowManagerService mWindowManager;

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface HotPath {
        public static final int LRU_UPDATE = 2;
        public static final int NONE = 0;
        public static final int OOM_ADJUSTMENT = 1;
        public static final int PROCESS_CHANGE = 3;

        int caller() default 0;
    }

    static {
        sSkipFreezingWindowActivitys.add("com.tencent.mobileqq/.activity.LoginActivity");
    }

    /* access modifiers changed from: package-private */
    public static final class UpdateConfigurationResult {
        boolean activityRelaunched;
        int changes;

        UpdateConfigurationResult() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.changes = 0;
            this.activityRelaunched = false;
        }
    }

    private final class FontScaleSettingObserver extends ContentObserver {
        private final Uri mFontScaleUri = Settings.System.getUriFor("font_scale");
        private final Uri mHideErrorDialogsUri = Settings.Global.getUriFor("hide_error_dialogs");

        public FontScaleSettingObserver() {
            super(ActivityTaskManagerService.this.mH);
            ContentResolver resolver = ActivityTaskManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mFontScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mHideErrorDialogsUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mFontScaleUri.equals(uri)) {
                ActivityTaskManagerService.this.updateFontScaleIfNeeded(userId);
            } else if (this.mHideErrorDialogsUri.equals(uri)) {
                synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        ActivityTaskManagerService.this.updateShouldShowDialogsLocked(ActivityTaskManagerService.this.getGlobalConfiguration());
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public ActivityTaskManagerService(Context context) {
        this.mContext = context;
        this.mFactoryTest = FactoryTest.getMode();
        this.mSystemThread = ActivityThread.currentActivityThread();
        this.mUiContext = this.mSystemThread.getSystemUiContext();
        this.mLifecycleManager = new ClientLifecycleManager();
        this.mInternal = new LocalService();
        this.GL_ES_VERSION = SystemProperties.getInt("ro.opengles.version", 0);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        this.mHwATMSEx = HwServiceExFactory.getHwActivityTaskManagerServiceEx(this, context);
    }

    public void onSystemReady() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mHasHeavyWeightFeature = this.mContext.getPackageManager().hasSystemFeature("android.software.cant_save_state");
                this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
                this.mAssistUtils = new AssistUtils(this.mContext);
                this.mVrController.onSystemReady();
                this.mRecentTasks.onSystemReadyLocked();
                this.mStackSupervisor.onSystemReady();
                this.mHwATMSEx.onSystemReady();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onInitPowerManagement() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.initPowerManagement();
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                this.mVoiceWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "*voice*");
                this.mVoiceWakeLock.setReferenceCounted(false);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void installSystemProviders() {
        this.mFontScaleSettingObserver = new FontScaleSettingObserver();
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f5 A[Catch:{ all -> 0x0167 }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x010e A[Catch:{ all -> 0x0167 }] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x012e A[Catch:{ all -> 0x0167 }] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x013d A[Catch:{ all -> 0x0167 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0150 A[Catch:{ all -> 0x0167 }] */
    public void retrieveSettings(ContentResolver resolver) {
        boolean multiWindowFormEnabled;
        Configuration globalConfig;
        boolean freeformWindowManagement = HwFreeFormUtils.isFreeFormEnable() || IS_HW_MULTIWINDOW_SUPPORTED || this.mContext.getPackageManager().hasSystemFeature("android.software.freeform_window_management") || Settings.Global.getInt(resolver, "enable_freeform_support", 0) != 0;
        boolean supportsMultiWindow = ActivityTaskManager.supportsMultiWindow(this.mContext);
        boolean supportsPictureInPicture = supportsMultiWindow && this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture");
        boolean supportsSplitScreenMultiWindow = ActivityTaskManager.supportsSplitScreenMultiWindow(this.mContext);
        boolean supportsMultiDisplay = this.mContext.getPackageManager().hasSystemFeature("android.software.activities_on_secondary_displays");
        boolean forceRtl = Settings.Global.getInt(resolver, "debug.force_rtl", 0) != 0;
        boolean forceResizable = Settings.Global.getInt(resolver, "force_resizable_activities", 0) != 0;
        boolean isPc = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.pc");
        DisplayProperties.debug_force_rtl(Boolean.valueOf(forceRtl));
        Configuration configuration = new Configuration();
        Settings.System.getConfiguration(resolver, configuration);
        HwThemeManager.retrieveSimpleUIConfig(resolver, configuration, getCurrentUserId());
        if (forceRtl) {
            configuration.setLayoutDirection(configuration.locale);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mForceResizableActivities = forceResizable;
                if (!freeformWindowManagement && !supportsSplitScreenMultiWindow && !supportsPictureInPicture) {
                    if (!supportsMultiDisplay) {
                        multiWindowFormEnabled = false;
                        if ((!supportsMultiWindow || forceResizable) && multiWindowFormEnabled) {
                            this.mSupportsMultiWindow = true;
                            this.mSupportsFreeformWindowManagement = freeformWindowManagement;
                            this.mSupportsSplitScreenMultiWindow = supportsSplitScreenMultiWindow;
                            this.mSupportsPictureInPicture = supportsPictureInPicture;
                            this.mSupportsMultiDisplay = supportsMultiDisplay;
                        } else {
                            this.mSupportsMultiWindow = false;
                            this.mSupportsFreeformWindowManagement = false;
                            this.mSupportsSplitScreenMultiWindow = false;
                            this.mSupportsPictureInPicture = false;
                            this.mSupportsMultiDisplay = false;
                        }
                        this.mWindowManager.setForceResizableTasks(this.mForceResizableActivities);
                        this.mWindowManager.setSupportsPictureInPicture(this.mSupportsPictureInPicture);
                        this.mWindowManager.setSupportsFreeformWindowManagement(this.mSupportsFreeformWindowManagement);
                        this.mWindowManager.setIsPc(isPc);
                        this.mWindowManager.mRoot.onSettingsRetrieved();
                        updateConfigurationLocked(configuration, null, true);
                        globalConfig = getGlobalConfiguration();
                        if (!ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Initial config: " + globalConfig);
                        }
                        Resources res = this.mContext.getResources();
                        this.mThumbnailWidth = res.getDimensionPixelSize(17104898);
                        this.mThumbnailHeight = res.getDimensionPixelSize(17104897);
                        if ((globalConfig.uiMode & 4) != 4) {
                            this.mFullscreenThumbnailScale = ((float) res.getInteger(17695000)) / ((float) globalConfig.screenWidthDp);
                        } else {
                            this.mFullscreenThumbnailScale = res.getFraction(18022414, 1, 1);
                        }
                        if (SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false)) {
                            this.mFullscreenThumbnailScale *= res.getFraction(34734080, 1, 1);
                        }
                    }
                }
                multiWindowFormEnabled = true;
                if (!supportsMultiWindow) {
                }
                this.mSupportsMultiWindow = true;
                this.mSupportsFreeformWindowManagement = freeformWindowManagement;
                this.mSupportsSplitScreenMultiWindow = supportsSplitScreenMultiWindow;
                this.mSupportsPictureInPicture = supportsPictureInPicture;
                this.mSupportsMultiDisplay = supportsMultiDisplay;
                this.mWindowManager.setForceResizableTasks(this.mForceResizableActivities);
                this.mWindowManager.setSupportsPictureInPicture(this.mSupportsPictureInPicture);
                this.mWindowManager.setSupportsFreeformWindowManagement(this.mSupportsFreeformWindowManagement);
                this.mWindowManager.setIsPc(isPc);
                this.mWindowManager.mRoot.onSettingsRetrieved();
                updateConfigurationLocked(configuration, null, true);
                globalConfig = getGlobalConfiguration();
                if (!ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                }
                Resources res2 = this.mContext.getResources();
                this.mThumbnailWidth = res2.getDimensionPixelSize(17104898);
                this.mThumbnailHeight = res2.getDimensionPixelSize(17104897);
                if ((globalConfig.uiMode & 4) != 4) {
                }
                if (SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false)) {
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        this.mHwATMSEx.setMultiWindowDisabled(false);
    }

    public WindowManagerGlobalLock getGlobalLock() {
        return this.mGlobalLock;
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public Context getUiContext() {
        return this.mUiContext;
    }

    @VisibleForTesting
    public ActivityTaskManagerInternal getAtmInternal() {
        return this.mInternal;
    }

    public void initialize(IntentFirewall intentFirewall, PendingIntentController intentController, Looper looper) {
        this.mH = new H(looper);
        this.mUiHandler = new UiHandler();
        this.mIntentFirewall = intentFirewall;
        File systemDir = SystemServiceManager.ensureSystemDir();
        this.mAppWarnings = new AppWarnings(this, this.mUiContext, this.mH, this.mUiHandler, systemDir);
        this.mCompatModePackages = new CompatModePackages(this, systemDir, this.mH);
        this.mPendingIntentController = intentController;
        this.mTempConfig.setToDefaults();
        this.mTempConfig.setLocales(LocaleList.getDefault());
        this.mTempConfig.seq = 1;
        this.mConfigurationSeq = 1;
        this.mStackSupervisor = createStackSupervisor();
        this.mRootActivityContainer = new RootActivityContainer(this);
        this.mRootActivityContainer.onConfigurationChanged(this.mTempConfig);
        this.mTaskChangeNotificationController = new TaskChangeNotificationController(this.mGlobalLock, this.mStackSupervisor, this.mH);
        this.mLockTaskController = new LockTaskController(this.mContext, this.mStackSupervisor, this.mH);
        this.mActivityStartController = new ActivityStartController(this);
        this.mRecentTasks = createRecentTasks();
        this.mStackSupervisor.setRecentTasks(this.mRecentTasks);
        this.mVrController = new VrController(this.mGlobalLock);
        this.mKeyguardController = this.mStackSupervisor.getKeyguardController();
    }

    public void onActivityManagerInternalAdded() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                this.mUgmInternal = (UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int increaseConfigurationSeqLocked() {
        int i = this.mConfigurationSeq + 1;
        this.mConfigurationSeq = i;
        this.mConfigurationSeq = Math.max(i, 1);
        return this.mConfigurationSeq;
    }

    /* access modifiers changed from: protected */
    public ActivityStackSupervisor createStackSupervisor() {
        ActivityStackSupervisor supervisor;
        HwServiceFactory.IHwActivityStackSupervisor iActivitySS = HwServiceFactory.getHwActivityStackSupervisor();
        if (iActivitySS != null) {
            supervisor = iActivitySS.getInstance(this, this.mH.getLooper());
        } else {
            supervisor = new ActivityStackSupervisor(this, this.mH.getLooper());
        }
        supervisor.initialize();
        return supervisor;
    }

    public void setWindowManager(WindowManagerService wm) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mWindowManager = wm;
                this.mLockTaskController.setWindowManager(wm);
                this.mStackSupervisor.setWindowManager(wm);
                this.mRootActivityContainer.setWindowManager(wm);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setUsageStatsManager(UsageStatsManagerInternal usageStatsManager) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mUsageStatsInternal = usageStatsManager;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public UserManagerService getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        }
        return this.mUserManager;
    }

    /* access modifiers changed from: package-private */
    public AppOpsService getAppOpsService() {
        if (this.mAppOpsService == null) {
            this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        }
        return this.mAppOpsService;
    }

    /* access modifiers changed from: package-private */
    public boolean hasUserRestriction(String restriction, int userId) {
        return getUserManager().hasUserRestriction(restriction, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSystemAlertWindowPermission(int callingUid, int callingPid, String callingPackage) {
        int mode = getAppOpsService().noteOperation(24, callingUid, callingPackage);
        return mode == 3 ? checkPermission("android.permission.SYSTEM_ALERT_WINDOW", callingPid, callingUid) == 0 : mode == 0;
    }

    public RecentTasks createRecentTasks() {
        return new RecentTasks(this, this.mStackSupervisor);
    }

    /* access modifiers changed from: package-private */
    public RecentTasks getRecentTasks() {
        return this.mRecentTasks;
    }

    /* access modifiers changed from: package-private */
    public ClientLifecycleManager getLifecycleManager() {
        return this.mLifecycleManager;
    }

    /* access modifiers changed from: package-private */
    public ActivityStartController getActivityStartController() {
        return this.mActivityStartController;
    }

    /* access modifiers changed from: package-private */
    public TaskChangeNotificationController getTaskChangeNotificationController() {
        return this.mTaskChangeNotificationController;
    }

    /* access modifiers changed from: package-private */
    public LockTaskController getLockTaskController() {
        return this.mLockTaskController;
    }

    /* access modifiers changed from: package-private */
    public Configuration getGlobalConfigurationForCallingPid() {
        return getGlobalConfigurationForPid(Binder.getCallingPid());
    }

    /* access modifiers changed from: package-private */
    public Configuration getGlobalConfigurationForPid(int pid) {
        Configuration configuration;
        if (pid == ActivityManagerService.MY_PID || pid < 0) {
            return getGlobalConfiguration();
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowProcessController app = this.mProcessMap.getProcess(pid);
                configuration = app != null ? app.getConfiguration() : getGlobalConfiguration();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return configuration;
    }

    public ConfigurationInfo getDeviceConfigurationInfo() {
        ConfigurationInfo config = new ConfigurationInfo();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Configuration globalConfig = getGlobalConfigurationForCallingPid();
                config.reqTouchScreen = globalConfig.touchscreen;
                config.reqKeyboardType = globalConfig.keyboard;
                config.reqNavigation = globalConfig.navigation;
                if (globalConfig.navigation == 2 || globalConfig.navigation == 3) {
                    config.reqInputFeatures |= 2;
                }
                if (!(globalConfig.keyboard == 0 || globalConfig.keyboard == 1)) {
                    config.reqInputFeatures |= 1;
                }
                config.reqGlEsVersion = this.GL_ES_VERSION;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return config;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void start() {
        LocalServices.addService(ActivityTaskManagerInternal.class, this.mInternal);
    }

    public static final class Lifecycle extends SystemService {
        private final ActivityTaskManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new ActivityTaskManagerService(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.wm.ActivityTaskManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [android.os.IBinder, com.android.server.wm.ActivityTaskManagerService] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public void onStart() {
            publishBinderService("activity_task", this.mService);
            this.mService.start();
        }

        public void onUnlockUser(int userId) {
            synchronized (this.mService.getGlobalLock()) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.onUserUnlocked(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void onCleanupUser(int userId) {
            synchronized (this.mService.getGlobalLock()) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.mLaunchParamsPersister.onCleanupUser(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public ActivityTaskManagerService getService() {
            return this.mService;
        }
    }

    public final int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions, UserHandle.getCallingUserId());
    }

    public final int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivities");
        return getActivityStartController().startActivities(caller, -1, 0, -1, callingPackage, intents, resolvedTypes, resultTo, SafeActivityOptions.fromBundle(bOptions), handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, "startActivities"), "startActivities", null, false);
    }

    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions, userId, true);
    }

    /* access modifiers changed from: package-private */
    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId, boolean validateIncomingUser) {
        enforceNotIsolatedCaller("startActivityAsUser");
        return getActivityStartController().obtainStarter(intent, "startActivityAsUser").setCaller(caller).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setProfilerInfo(profilerInfo).setActivityOptions(bOptions).setMayWait(getActivityStartController().checkTargetUser(userId, validateIncomingUser, Binder.getCallingPid(), Binder.getCallingUid(), "startActivityAsUser")).execute();
    }

    /* JADX INFO: finally extract failed */
    public int startActivityIntentSender(IApplicationThread caller, IIntentSender target, IBinder whitelistToken, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle bOptions) {
        enforceNotIsolatedCaller("startActivityIntentSender");
        if (fillInIntent != null && fillInIntent.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        } else if (target instanceof PendingIntentRecord) {
            PendingIntentRecord pir = (PendingIntentRecord) target;
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = getTopDisplayFocusedStack();
                    if (stack.mResumedActivity != null && stack.mResumedActivity.info.applicationInfo.uid == Binder.getCallingUid()) {
                        this.mAppSwitchesAllowedTime = 0;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return pir.sendInner(0, fillInIntent, resolvedType, whitelistToken, (IIntentReceiver) null, (String) null, resultTo, resultWho, requestCode, flagsMask, flagsValues, bOptions);
        } else {
            throw new IllegalArgumentException("Bad PendingIntent object");
        }
    }

    public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle bOptions) {
        Throwable th;
        String str;
        if (intent == null || !intent.hasFileDescriptors()) {
            SafeActivityOptions options = SafeActivityOptions.fromBundle(bOptions);
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(callingActivity);
                    if (r == null) {
                        SafeActivityOptions.abort(options);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else if (!r.attachedToProcess()) {
                        SafeActivityOptions.abort(options);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else {
                        Intent intent2 = new Intent(intent);
                        try {
                            intent2.setDataAndType(r.intent.getData(), r.intent.getType());
                            intent2.setComponent(null);
                            int i = 1;
                            boolean debug = (intent2.getFlags() & 8) != 0;
                            ActivityInfo aInfo = null;
                            try {
                                List<ResolveInfo> resolves = AppGlobals.getPackageManager().queryIntentActivities(intent2, r.resolvedType, 66560, UserHandle.getCallingUserId()).getList();
                                int N = resolves != null ? resolves.size() : 0;
                                int i2 = 0;
                                while (true) {
                                    if (i2 >= N) {
                                        break;
                                    }
                                    ResolveInfo rInfo = resolves.get(i2);
                                    if (!rInfo.activityInfo.packageName.equals(r.packageName) || !rInfo.activityInfo.name.equals(r.info.name)) {
                                        i2++;
                                        i = 1;
                                    } else {
                                        int i3 = i2 + i;
                                        if (i3 < N) {
                                            aInfo = resolves.get(i3).activityInfo;
                                        }
                                        if (debug) {
                                            Slog.v("ActivityTaskManager", "Next matching activity: found current " + r.packageName + "/" + r.info.name);
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("Next matching activity: next is ");
                                            if (aInfo == null) {
                                                str = "null";
                                            } else {
                                                str = aInfo.packageName + "/" + aInfo.name;
                                            }
                                            sb.append(str);
                                            Slog.v("ActivityTaskManager", sb.toString());
                                        }
                                    }
                                }
                            } catch (RemoteException e) {
                            }
                            if (aInfo == null) {
                                SafeActivityOptions.abort(options);
                                if (debug) {
                                    Slog.d("ActivityTaskManager", "Next matching activity: nothing found");
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return false;
                            }
                            intent2.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
                            intent2.setFlags(intent2.getFlags() & -503316481);
                            boolean wasFinishing = r.finishing;
                            r.finishing = true;
                            ActivityRecord resultTo = r.resultTo;
                            String resultWho = r.resultWho;
                            int requestCode = r.requestCode;
                            r.resultTo = null;
                            if (resultTo != null) {
                                resultTo.removeResultsLocked(r, resultWho, requestCode);
                            }
                            long origId = Binder.clearCallingIdentity();
                            int res = getActivityStartController().obtainStarter(intent2, "startNextMatchingActivity").setCaller(r.app.getThread()).setResolvedType(r.resolvedType).setActivityInfo(aInfo).setResultTo(resultTo != null ? resultTo.appToken : null).setResultWho(resultWho).setRequestCode(requestCode).setCallingPid(-1).setCallingUid(r.launchedFromUid).setCallingPackage(r.launchedFromPackage).setRealCallingPid(-1).setRealCallingUid(r.launchedFromUid).setActivityOptions(options).execute();
                            Binder.restoreCallingIdentity(origId);
                            r.finishing = wasFinishing;
                            if (res != 0) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return false;
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        } catch (Throwable th2) {
                            th = th2;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    public final WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        Throwable th;
        int i;
        WaitResult res = new WaitResult();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                enforceNotIsolatedCaller("startActivityAndWait");
                i = userId;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
                try {
                    try {
                        try {
                            try {
                                try {
                                } catch (Throwable th3) {
                                    th = th3;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
                try {
                    try {
                        try {
                            try {
                                try {
                                } catch (Throwable th8) {
                                    th = th8;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                    try {
                        getActivityStartController().obtainStarter(intent, "startActivityAndWait").setCaller(caller).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setActivityOptions(bOptions).setMayWait(handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), i, "startActivityAndWait")).setProfilerInfo(profilerInfo).setWaitResult(res).execute();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return res;
                    } catch (Throwable th12) {
                        th = th12;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } catch (Throwable th13) {
                    th = th13;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th14) {
                th = th14;
                i = userId;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public final int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration config, Bundle bOptions, int userId) {
        int execute;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                enforceNotIsolatedCaller("startActivityWithConfig");
                execute = getActivityStartController().obtainStarter(intent, "startActivityWithConfig").setCaller(caller).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setGlobalConfiguration(config).setActivityOptions(bOptions).setMayWait(handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, "startActivityWithConfig")).execute();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return execute;
    }

    /* JADX INFO: finally extract failed */
    public IBinder requestStartActivityPermissionToken(IBinder delegatorToken) {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callingUid) == 1000) {
            IBinder permissionToken = new Binder();
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStartActivitySources.put(permissionToken, delegatorToken);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mUiHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ.INSTANCE, this, permissionToken), START_AS_CALLER_TOKEN_TIMEOUT_IMPL);
            this.mUiHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$7ieG0s7Zp4H2bLiWdOgB6MqhcI.INSTANCE, this, permissionToken), START_AS_CALLER_TOKEN_EXPIRED_TIMEOUT);
            return permissionToken;
        }
        throw new SecurityException("Only the system process can request a permission token, received request from uid: " + callingUid);
    }

    public final int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, IBinder permissionToken, boolean ignoreTargetSecurity, int userId) {
        IBinder sourceToken;
        ActivityRecord sourceRecord;
        int targetUid;
        String targetPackage;
        boolean isResolver;
        SecurityException e;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (resultTo != null) {
                    if (permissionToken != null) {
                        this.mAmInternal.enforceCallingPermission("android.permission.START_ACTIVITY_AS_CALLER", "startActivityAsCaller");
                        sourceToken = this.mStartActivitySources.remove(permissionToken);
                        if (sourceToken == null) {
                            if (this.mExpiredStartAsCallerTokens.contains(permissionToken)) {
                                throw new SecurityException("Called with expired permission token: " + permissionToken);
                            }
                            throw new SecurityException("Called with invalid permission token: " + permissionToken);
                        }
                    } else {
                        sourceToken = resultTo;
                    }
                    sourceRecord = this.mRootActivityContainer.isInAnyStack(sourceToken);
                    if (sourceRecord == null) {
                        throw new SecurityException("Called with bad activity token: " + sourceToken);
                    } else if (sourceRecord.app != null) {
                        if (!sourceRecord.info.packageName.equals("android")) {
                            if (!sourceRecord.info.packageName.equals("com.huawei.android.internal.app")) {
                                throw new SecurityException("Must be called from an activity that is declared in the android package");
                            }
                        }
                        if (UserHandle.getAppId(sourceRecord.app.mUid) != 1000) {
                            if (sourceRecord.app.mUid != sourceRecord.launchedFromUid) {
                                throw new SecurityException("Calling activity in uid " + sourceRecord.app.mUid + " must be system uid or original calling uid " + sourceRecord.launchedFromUid);
                            }
                        }
                        if (ignoreTargetSecurity) {
                            if (intent.getComponent() == null) {
                                throw new SecurityException("Component must be specified with ignoreTargetSecurity");
                            } else if (intent.getSelector() != null) {
                                throw new SecurityException("Selector not allowed with ignoreTargetSecurity");
                            }
                        }
                        targetUid = sourceRecord.launchedFromUid;
                        targetPackage = sourceRecord.launchedFromPackage;
                        isResolver = sourceRecord.isResolverOrChildActivity();
                    } else {
                        throw new SecurityException("Called without a process attached to activity");
                    }
                } else {
                    throw new SecurityException("Must be called from an activity");
                }
            } catch (Throwable th) {
                th = th;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        int userId2 = userId;
        if (userId2 == -10000) {
            userId2 = UserHandle.getUserId(sourceRecord.app.mUid);
        }
        try {
            try {
            } catch (SecurityException e2) {
                e = e2;
                throw e;
            }
            try {
                try {
                } catch (SecurityException e3) {
                    e = e3;
                    throw e;
                }
            } catch (SecurityException e4) {
                e = e4;
                throw e;
            }
            try {
                try {
                    try {
                        return getActivityStartController().obtainStarter(intent, "startActivityAsCaller").setCallingUid(targetUid).setCallingPackage(targetPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setActivityOptions(bOptions).setMayWait(userId2).setIgnoreTargetSecurity(ignoreTargetSecurity).setFilterCallingUid(isResolver ? 0 : targetUid).setAllowBackgroundActivityStart(true).execute();
                    } catch (SecurityException e5) {
                        throw e5;
                    }
                } catch (SecurityException e6) {
                    e = e6;
                    throw e;
                }
            } catch (SecurityException e7) {
                e = e7;
                throw e;
            }
        } catch (SecurityException e8) {
            e = e8;
            throw e;
        }
    }

    /* access modifiers changed from: package-private */
    public int handleIncomingUser(int callingPid, int callingUid, int userId, String name) {
        return this.mAmInternal.handleIncomingUser(callingPid, callingUid, userId, false, 2, name, (String) null);
    }

    public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        this.mAmInternal.enforceCallingPermission("android.permission.BIND_VOICE_INTERACTION", "startVoiceActivity()");
        if (session == null || interactor == null) {
            throw new NullPointerException("null session or interactor");
        }
        return getActivityStartController().obtainStarter(intent, "startVoiceActivity").setCallingUid(callingUid).setCallingPackage(callingPackage).setResolvedType(resolvedType).setVoiceSession(session).setVoiceInteractor(interactor).setStartFlags(startFlags).setProfilerInfo(profilerInfo).setActivityOptions(bOptions).setMayWait(handleIncomingUser(callingPid, callingUid, userId, "startVoiceActivity")).setAllowBackgroundActivityStart(true).execute();
    }

    public int startAssistantActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, Bundle bOptions, int userId) {
        this.mAmInternal.enforceCallingPermission("android.permission.BIND_VOICE_INTERACTION", "startAssistantActivity()");
        return getActivityStartController().obtainStarter(intent, "startAssistantActivity").setCallingUid(callingUid).setCallingPackage(callingPackage).setResolvedType(resolvedType).setActivityOptions(bOptions).setMayWait(handleIncomingUser(callingPid, callingUid, userId, "startAssistantActivity")).setAllowBackgroundActivityStart(true).execute();
    }

    /* JADX INFO: finally extract failed */
    public void startRecentsActivity(Intent intent, IAssistDataReceiver assistDataReceiver, IRecentsAnimationRunner recentsAnimationRunner) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startRecentsActivity()");
        int callingPid = Binder.getCallingPid();
        long origId = Binder.clearCallingIdentity();
        try {
            this.mHwATMSEx.addSurfaceInNotchIfNeed();
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    new RecentsAnimation(this, this.mStackSupervisor, getActivityStartController(), this.mWindowManager, callingPid).startRecentsActivity(intent, recentsAnimationRunner, this.mRecentTasks.getRecentsComponent(), this.mRecentTasks.getRecentsComponentUid(), assistDataReceiver);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public final int startActivityFromRecents(int taskId, Bundle bOptions) {
        int startActivityFromRecents;
        enforceCallerIsRecentsOrHasPermission("android.permission.START_TASKS_FROM_RECENTS", "startActivityFromRecents()");
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(bOptions);
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Flog.i(101, "startActivityFromRecents: taskId =" + taskId);
                    startActivityFromRecents = this.mStackSupervisor.startActivityFromRecents(callingPid, callingUid, taskId, safeOptions);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return startActivityFromRecents;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public final boolean isActivityStartAllowedOnDisplay(int displayId, Intent intent, String resolvedType, int userId) {
        boolean canPlaceEntityOnDisplay;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityInfo aInfo = this.mAmInternal.getActivityInfoForUser(this.mStackSupervisor.resolveActivity(intent, resolvedType, 0, null, userId, ActivityStarter.computeResolveFilterUid(callingUid, callingUid, -10000)), userId);
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    canPlaceEntityOnDisplay = this.mStackSupervisor.canPlaceEntityOnDisplay(displayId, callingPid, callingUid, aInfo);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return canPlaceEntityOnDisplay;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x0189  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x01ad  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c1 A[Catch:{ all -> 0x00b7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00e9 A[Catch:{ all -> 0x00b7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0111 A[Catch:{ all -> 0x00b7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x011a A[Catch:{ all -> 0x00b7 }] */
    public final boolean finishActivity(IBinder token, int resultCode, Intent resultData, int finishTask) {
        boolean z;
        boolean res;
        ActivityStack freeformStack;
        boolean isNull;
        ActivityRecord next;
        if (resultData == null || !resultData.hasFileDescriptors()) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return true;
                    }
                    TaskRecord tr = r.getTaskRecord();
                    ActivityRecord rootR = tr.getRootActivity();
                    if (rootR == null) {
                        Slog.w("ActivityTaskManager", "Finishing task with all activities already finished");
                    }
                    boolean z2 = false;
                    if (getLockTaskController().activityBlockedFromFinish(r)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    if (!(this.mController == null || (next = r.getActivityStack().topRunningActivityLocked(token, 0)) == null)) {
                        boolean resumeOK = true;
                        try {
                            resumeOK = this.mController.activityResuming(next.packageName);
                        } catch (RemoteException e) {
                            this.mController = null;
                            Watchdog.getInstance().setActivityController((IActivityController) null);
                        }
                        if (!resumeOK) {
                            Slog.i("ActivityTaskManager", "Not finishing activity because controller resumed");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        }
                    }
                    if (r.app != null) {
                        r.app.setLastActivityFinishTimeIfNeeded(SystemClock.uptimeMillis());
                    }
                    if (tr.getStack() != null && r == rootR) {
                        tr.getStack().mHwActivityStackEx.resetOtherStacksVisible(true);
                        this.mHwATMSEx.finishRootActivity(r);
                    }
                    long origId = Binder.clearCallingIdentity();
                    boolean finishWithRootActivity = finishTask == 1;
                    if (r == rootR) {
                        try {
                            if (r.inSplitScreenPrimaryWindowingMode()) {
                                z = true;
                                this.mShouldFreeze = z;
                                if (this.mShouldFreeze) {
                                    Slog.i("ActivityTaskManager", "startFreezingScreen");
                                    this.mH.removeMessages(STOP_FREEZE_SCREEN);
                                    this.mH.sendMessageDelayed(this.mH.obtainMessage(STOP_FREEZE_SCREEN), 400);
                                    this.mWindowManager.mShouldResetTime = true;
                                    this.mWindowManager.startFreezingScreen(0, 0);
                                }
                                if (finishTask != 2) {
                                    if (!finishWithRootActivity || r != rootR) {
                                        if (HwFreeFormUtils.isFreeFormEnable()) {
                                            ActivityStack freeformStack2 = this.mRootActivityContainer.getStack(5, 1);
                                            if (freeformStack2 != null) {
                                                if (r == rootR) {
                                                    isNull = false;
                                                    if (!isNull && tr.getChildCount() == 1) {
                                                        if (!tr.inFreeformWindowingMode()) {
                                                            freeformStack2.setFreeFormStackVisible(false);
                                                            freeformStack2.setCurrentPkgUnderFreeForm("");
                                                        } else if (tr.affinity != null && tr.affinity.equals(freeformStack2.getCurrentPkgUnderFreeForm())) {
                                                            this.mStackSupervisor.mHwActivityStackSupervisorEx.removeFreeFromStackLocked();
                                                        }
                                                    }
                                                }
                                            }
                                            isNull = true;
                                            if (!tr.inFreeformWindowingMode()) {
                                            }
                                        }
                                        if (!tr.inHwMagicWindowingMode() || !HwMwUtils.performPolicy(28, new Object[]{token}).getBoolean("BUNDLE_RESULT_ONBACKPRESSED", false)) {
                                            res = tr.getStack().requestFinishActivityLocked(token, resultCode, resultData, "app-request", true);
                                            if (!res) {
                                                Slog.i("ActivityTaskManager", "Failed to finish by app-request");
                                            }
                                            Binder.restoreCallingIdentity(origId);
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            return res;
                                        }
                                        Binder.restoreCallingIdentity(origId);
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        return false;
                                    }
                                }
                                freeformStack = this.mRootActivityContainer.getStack(5, 1);
                                boolean removeFreeformInRecent = false;
                                if (freeformStack == null && freeformStack.getFreeFormStackVisible() && tr.inFreeformWindowingMode()) {
                                    freeformStack.setFreeFormStackVisible(false);
                                    freeformStack.setCurrentPkgUnderFreeForm("");
                                    removeFreeformInRecent = true;
                                    Flog.bdReport(991311063);
                                }
                                ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                                int i = tr.taskId;
                                if (finishWithRootActivity || removeFreeformInRecent) {
                                    z2 = true;
                                }
                                res = activityStackSupervisor.removeTaskByIdLocked(i, false, z2, "finish-activity");
                                if (!res) {
                                    Slog.i("ActivityTaskManager", "Removing task failed to finish activity");
                                }
                                r.mRelaunchReason = 0;
                                Binder.restoreCallingIdentity(origId);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return res;
                            }
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    z = false;
                    this.mShouldFreeze = z;
                    if (this.mShouldFreeze) {
                    }
                    if (finishTask != 2) {
                    }
                    freeformStack = this.mRootActivityContainer.getStack(5, 1);
                    boolean removeFreeformInRecent2 = false;
                    if (freeformStack == null && freeformStack.getFreeFormStackVisible() && tr.inFreeformWindowingMode()) {
                    }
                    ActivityStackSupervisor activityStackSupervisor2 = this.mStackSupervisor;
                    int i2 = tr.taskId;
                    z2 = true;
                    res = activityStackSupervisor2.removeTaskByIdLocked(i2, false, z2, "finish-activity");
                    if (!res) {
                    }
                    r.mRelaunchReason = 0;
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return res;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    public boolean finishActivityAffinity(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return false;
                    }
                    TaskRecord task = r.getTaskRecord();
                    if (getLockTaskController().activityBlockedFromFinish(r)) {
                        Binder.restoreCallingIdentity(origId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean finishActivityAffinityLocked = task.getStack().finishActivityAffinityLocked(r);
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return finishActivityAffinityLocked;
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public final void activityIdle(IBinder token, Configuration config, boolean stopProfiling) {
        long origId = Binder.clearCallingIdentity();
        if (!this.mActivityIdle) {
            this.mActivityIdle = true;
            HwBootFail.notifyBootSuccess();
        }
        WindowProcessController proc = null;
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityRecord.getStackLocked(token) == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityRecord r = this.mStackSupervisor.activityIdleInternalLocked(token, false, false, config);
                    if (r != null) {
                        proc = r.app;
                    }
                    if (stopProfiling && proc != null) {
                        proc.clearProfilerIfNeeded();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public final void activityResumed(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord.activityResumedLocked(token);
                this.mWindowManager.notifyAppResumedFinished(token);
                this.mHwATMSEx.dispatchActivityLifeState(ActivityRecord.forToken(token), "onResume");
                this.mHwATMSEx.resumeCoordinationPrimaryStack(ActivityRecord.forToken(token));
                this.mStackSupervisor.mHandler.removeMessages(1200);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public final void activityTopResumedStateLost() {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.handleTopResumedStateReleased(false);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public final void activityPaused(IBinder token) {
        long hiviewTime = SystemClock.uptimeMillis();
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long start = SystemClock.uptimeMillis();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityPausedLocked(token, false);
                    this.mHwATMSEx.dispatchActivityLifeState(ActivityRecord.forToken(token), "onPause");
                }
                Flog.i(101, "activityPaused cost " + (SystemClock.uptimeMillis() - start));
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
        Jlog.betaUserPrint(404, hiviewTime, token.toString());
    }

    public final void activityStopped(IBinder token, Bundle icicle, PersistableBundle persistentState, CharSequence description) {
        ActivityRecord r;
        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
            Slog.v("ActivityTaskManager", "Activity stopped: token=" + token);
        }
        if (icicle == null || !icicle.hasFileDescriptors()) {
            long origId = Binder.clearCallingIdentity();
            String restartingName = null;
            int restartingUid = 0;
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    r = ActivityRecord.isInStackLocked(token);
                    if (r != null) {
                        if (r.attachedToProcess() && r.isState(ActivityStack.ActivityState.RESTARTING_PROCESS)) {
                            restartingName = r.app.mName;
                            restartingUid = r.app.mUid;
                        }
                        r.activityStoppedLocked(icicle, persistentState, description);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (restartingName != null) {
                this.mStackSupervisor.removeRestartTimeouts(r);
                this.mAmInternal.killProcess(restartingName, restartingUid, "restartActivityProcess");
            }
            this.mH.post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$YoKo5aeVLR6dVqL_rpujyVUxllc */

                @Override // java.lang.Runnable
                public final void run() {
                    ActivityTaskManagerService.this.lambda$activityStopped$0$ActivityTaskManagerService();
                }
            });
            Binder.restoreCallingIdentity(origId);
            return;
        }
        throw new IllegalArgumentException("File descriptors passed in Bundle");
    }

    public /* synthetic */ void lambda$activityStopped$0$ActivityTaskManagerService() {
        this.mAmInternal.trimApplications();
    }

    public final void activityDestroyed(IBinder token) {
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v("ActivityTaskManager", "ACTIVITY DESTROYED: " + token);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityDestroyedLocked(token, "activityDestroyed");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public final void activityRelaunched(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.activityRelaunchedLocked(token);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public final void activitySlept(IBinder token) {
        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
            Slog.v("ActivityTaskManager", "Activity slept: token=" + token);
        }
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    this.mStackSupervisor.activitySleptLocked(r);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public void setRequestedOrientation(IBinder token, int requestedOrientation) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    Flog.i(101, "setRequestedOrientation r: " + r + ", callingPid: " + Binder.getCallingPid() + ", callingUid: " + Binder.getCallingUid() + ", requestedOrientation " + requestedOrientation);
                    if (this.mHwATMSEx.isMaximizedPortraitAppOnPCMode(r)) {
                        HwPCUtils.log("ActivityTaskManager", "setRequestedOrientation " + r + "is Portrait & Maximized.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (!r.inHwPCMultiStackWindowingMode() || !(r.getState() == ActivityStack.ActivityState.STOPPED || r.getState() == ActivityStack.ActivityState.STOPPING)) {
                        long origId = Binder.clearCallingIdentity();
                        try {
                            r.setRequestedOrientation(requestedOrientation);
                            Binder.restoreCallingIdentity(origId);
                            this.mHwATMSEx.setRequestedOrientation(requestedOrientation);
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    } else {
                        Flog.i(101, "setRequestedOrientation is ignored, " + r + " state is STOPPED or STOPPING.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getRequestedOrientation(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return -1;
                }
                int orientation = r.getOrientation();
                WindowManagerService.resetPriorityAfterLockedSection();
                return orientation;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setImmersive(IBinder token, boolean immersive) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.immersive = immersive;
                    if (r.isResumedActivityOnDisplay()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_IMMERSIVE) {
                            Slog.d("ActivityTaskManager", "Frontmost changed immersion: " + r);
                        }
                        applyUpdateLockStateLocked(r);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void applyUpdateLockStateLocked(ActivityRecord r) {
        this.mH.post(new Runnable(r != null && r.immersive, r) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$9Q7wpaNhVooEMYX2fSYb7oJRS2g */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ ActivityRecord f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityTaskManagerService.this.lambda$applyUpdateLockStateLocked$1$ActivityTaskManagerService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$applyUpdateLockStateLocked$1$ActivityTaskManagerService(boolean nextState, ActivityRecord r) {
        if (this.mUpdateLock.isHeld() != nextState) {
            if (ActivityTaskManagerDebugConfig.DEBUG_IMMERSIVE) {
                Slog.d("ActivityTaskManager", "Applying new update lock state '" + nextState + "' for " + r);
            }
            if (nextState) {
                this.mUpdateLock.acquire();
            } else {
                this.mUpdateLock.release();
            }
        }
    }

    public boolean isImmersive(IBinder token) {
        boolean z;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    z = r.immersive;
                } else {
                    throw new IllegalArgumentException();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public boolean isTopActivityImmersive() {
        boolean z;
        enforceNotIsolatedCaller("isTopActivityImmersive");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getTopDisplayFocusedStack().topRunningActivityLocked();
                z = r != null ? r.immersive : false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord self = ActivityRecord.isInStackLocked(token);
                if (self != null) {
                    if (this.mWindowManager.getRecentsAnimationController() != null) {
                        enterAnim = 0;
                        exitAnim = 0;
                        Slog.d("ActivityTaskManager", "Modify overridePendingTransition to empty during recents animation");
                    }
                    if (this.mHwATMSEx.skipOverridePendingTransitionForMagicWindow(self)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (this.mHwATMSEx.skipOverridePendingTransitionForPC(self)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (!self.toString().contains("com.huawei.android.internal.app") || !self.isState(ActivityStack.ActivityState.PAUSING) || enterAnim != 0 || exitAnim != 0) {
                        long origId = Binder.clearCallingIdentity();
                        if (self.isState(ActivityStack.ActivityState.RESUMED, ActivityStack.ActivityState.PAUSING) || ((self.inHwFreeFormWindowingMode() && self.isState(ActivityStack.ActivityState.DESTROYING) && enterAnim == 0 && exitAnim == 0) || (self.isSplitBaseActivity() && self.isState(ActivityStack.ActivityState.PAUSED)))) {
                            self.getDisplay().mDisplayContent.mAppTransition.overridePendingAppTransition(packageName, enterAnim, exitAnim, null);
                        }
                        Binder.restoreCallingIdentity(origId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getFrontActivityScreenCompatMode() {
        enforceNotIsolatedCaller("getFrontActivityScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getTopDisplayFocusedStack().topRunningActivityLocked();
                if (r == null) {
                    return -3;
                }
                int computeCompatModeLocked = this.mCompatModePackages.computeCompatModeLocked(r.info.applicationInfo);
                WindowManagerService.resetPriorityAfterLockedSection();
                return computeCompatModeLocked;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setFrontActivityScreenCompatMode(int mode) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setFrontActivityScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getTopDisplayFocusedStack().topRunningActivityLocked();
                if (r == null) {
                    Slog.w("ActivityTaskManager", "setFrontActivityScreenCompatMode failed: no top activity");
                    return;
                }
                this.mCompatModePackages.setPackageScreenCompatModeLocked(r.info.applicationInfo, mode);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getLaunchedFromUid(IBinder activityToken) {
        ActivityRecord srec;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                srec = ActivityRecord.forTokenLocked(activityToken);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (srec == null) {
            return -1;
        }
        return srec.launchedFromUid;
    }

    public String getLaunchedFromPackage(IBinder activityToken) {
        ActivityRecord srec;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                srec = ActivityRecord.forTokenLocked(activityToken);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (srec == null) {
            return null;
        }
        return srec.launchedFromPackage;
    }

    /* JADX INFO: finally extract failed */
    public boolean convertFromTranslucent(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean translucentChanged = r.changeWindowTranslucency(true);
                    if (translucentChanged) {
                        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    }
                    this.mWindowManager.setAppFullscreen(token, true);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return translucentChanged;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean convertToTranslucent(IBinder token, Bundle options) {
        SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(options);
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    TaskRecord task = r.getTaskRecord();
                    int index = task.mActivities.lastIndexOf(r);
                    if (index > 0) {
                        task.mActivities.get(index - 1).returningOptions = safeOptions != null ? safeOptions.getOptions(r) : null;
                    }
                    boolean translucentChanged = r.changeWindowTranslucency(false);
                    if (translucentChanged) {
                        r.getActivityStack().convertActivityToTranslucent(r);
                    }
                    this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    this.mWindowManager.setAppFullscreen(token, false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return translucentChanged;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void notifyActivityDrawn(IBinder token) {
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            Slog.d(str, "notifyActivityDrawn: token=" + token);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = this.mRootActivityContainer.isInAnyStack(token);
                if (r != null) {
                    r.getActivityStack().notifyActivityDrawnLocked(r);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reportActivityFullyDrawn(IBinder token, boolean restoredFromBundle) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.reportFullyDrawnLocked(restoredFromBundle);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getActivityDisplayId(IBinder activityToken) throws RemoteException {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(activityToken);
                if (stack == null || stack.mDisplayId == -1) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return 0;
                }
                return stack.mDisplayId;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getStackInfo()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack focusedStack = getTopDisplayFocusedStack();
                    if (focusedStack != null) {
                        ActivityManager.StackInfo stackInfo = this.mRootActivityContainer.getStackInfo(focusedStack.mStackId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return stackInfo;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void setFocusedStack(int stackId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFocusedStack()");
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            Slog.d("ActivityTaskManager", "setFocusedStack: stackId=" + stackId);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        Slog.w("ActivityTaskManager", "setFocusedStack: No stack with id=" + stackId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityRecord r = stack.topRunningActivityLocked();
                    if (r != null && r.moveFocusableActivityToTop("setFocusedStack")) {
                        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    public void setFocusedTask(int taskId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFocusedTask()");
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            Slog.d("ActivityTaskManager", "setFocusedTask: taskId=" + taskId);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityRecord r = task.topRunningActivityLocked();
                    if (r == null || !this.mStackSupervisor.mStoppingActivities.contains(r) || !r.isState(ActivityStack.ActivityState.RESUMED)) {
                        if (r != null && r.moveFocusableActivityToTop("setFocusedTask")) {
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(callingId);
                        return;
                    }
                    Slog.i("ActivityTaskManager", "skip setFocusedStack: taskId=" + taskId + " for stopping " + r);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    public void restartActivityProcessIfVisible(IBinder activityToken) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "restartActivityProcess()");
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    r.restartProcessIfVisible();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean removeTask(int taskId) {
        boolean removeTaskByIdLocked;
        TaskRecord task;
        enforceCallerIsRecentsOrHasPermission("android.permission.REMOVE_TASKS", "removeTask()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Slog.i("ActivityTaskManager", "removeTask taskId=" + taskId + " callerUid=" + Binder.getCallingUid() + " callerPid=" + Binder.getCallingPid());
                long ident = Binder.clearCallingIdentity();
                try {
                    if (HwPCUtils.isPcCastModeInServer() && (task = this.mRootActivityContainer.anyTaskForId(taskId, 1)) != null) {
                        this.mHwATMSEx.updateUsageStatsForPCMode(task.getTopActivity(), false, this.mUsageStatsInternal);
                    }
                    removeTaskByIdLocked = this.mStackSupervisor.removeTaskByIdLocked(taskId, true, true, "remove-task");
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return removeTaskByIdLocked;
    }

    public void removeAllVisibleRecentTasks() {
        enforceCallerIsRecentsOrHasPermission("android.permission.REMOVE_TASKS", "removeAllVisibleRecentTasks()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Slog.i("ActivityTaskManager", "removeAllVisibleRecentTasks caller=" + Binder.getCallingUid() + " callerPid=" + Binder.getCallingPid());
                long ident = Binder.clearCallingIdentity();
                try {
                    getRecentTasks().removeAllVisibleTasks(this.mAmInternal.getCurrentUserId());
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean shouldUpRecreateTask(IBinder token, String destAffinity) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord srec = ActivityRecord.forTokenLocked(token);
                if (srec != null) {
                    return srec.getActivityStack().shouldUpRecreateTaskLocked(srec, destAffinity);
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean navigateUpTo(IBinder token, Intent destIntent, int resultCode, Intent resultData) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.ACTIVITYMANAGER_NAVIGATEUPTO);
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    return r.getActivityStack().navigateUpToLocked(r, destIntent, resultCode, resultData);
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) {
        ActivityRecord r;
        enforceNotIsolatedCaller("moveActivityTaskToBack");
        if (ActivityTaskManagerDebugConfig.HWFLOW) {
            Flog.i(101, "moveActivityTaskToBack pid =" + Binder.getCallingPid());
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mHwATMSEx.moveActivityTaskToBackEx(token);
                long origId = Binder.clearCallingIdentity();
                if (HwMwUtils.ENABLED && (r = ActivityRecord.forTokenLocked(token)) != null && r.inHwMagicWindowingMode()) {
                    nonRoot = true;
                }
                try {
                    int taskId = ActivityRecord.getTaskForActivityLocked(token, !nonRoot);
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task != null) {
                        boolean result = ActivityRecord.getStackLocked(token).moveTaskToBackLocked(taskId);
                        if (result && task.inHwPCMultiStackWindowingMode()) {
                            this.mHwATMSEx.updateWindowForPcFreeForm(task.getTaskInfo());
                        }
                        return result;
                    }
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public Rect getTaskBounds(int taskId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getTaskBounds()");
        long ident = Binder.clearCallingIdentity();
        Rect rect = new Rect();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 1);
                    if (task == null) {
                        Slog.w("ActivityTaskManager", "getTaskBounds: taskId=" + taskId + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return rect;
                    }
                    if (task.getStack() != null) {
                        task.getWindowContainerBounds(rect);
                    } else if (!task.matchParentBounds()) {
                        rect.set(task.getBounds());
                    } else if (task.mLastNonFullscreenBounds != null) {
                        rect.set(task.mLastNonFullscreenBounds);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return rect;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public ActivityManager.TaskDescription getTaskDescription(int id) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getTaskDescription()");
                TaskRecord tr = this.mRootActivityContainer.anyTaskForId(id, 1);
                if (tr != null) {
                    return tr.lastTaskDescription;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop) {
        if (windowingMode == 3) {
            setTaskWindowingModeSplitScreenPrimary(taskId, 0, toTop, true, null, true);
            return;
        }
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setTaskWindowingMode()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w("ActivityTaskManager", "setTaskWindowingMode: No task for id=" + taskId);
                        return;
                    }
                    Slog.i("ActivityTaskManager", "setTaskWindowingMode: moving task=" + taskId + " to windowingMode=" + windowingMode + " toTop=" + toTop);
                    if (task.isActivityTypeStandardOrUndefined()) {
                        ActivityStack stack = task.getStack();
                        if (!stack.inHwSplitScreenWindowingMode() || !WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode) || !stack.mTaskStack.isVisible()) {
                            if (!WindowConfiguration.isHwFreeFormWindowingMode(windowingMode)) {
                                if (toTop) {
                                    stack.moveToFront("setTaskWindowingMode", task);
                                }
                                stack.setWindowingMode(windowingMode);
                            } else {
                                stack.setWindowingMode(windowingMode, false, false, false, true, false);
                                if (toTop) {
                                    stack.moveToFront("setTaskWindowingMode", task);
                                }
                                this.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
                                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                            }
                            Binder.restoreCallingIdentity(ident);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        Slog.i("ActivityTaskManager", "Task already splited and visible, no need to setTaskWindowingMode for taskId: " + taskId);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    throw new IllegalArgumentException("setTaskWindowingMode: Attempt to move non-standard task " + taskId + " to windowing mode=" + windowingMode);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public String getCallingPackage(IBinder token) {
        String str;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getCallingRecordLocked(token);
                str = r != null ? r.info.packageName : null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return str;
    }

    public ComponentName getCallingActivity(IBinder token) {
        ComponentName component;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getCallingRecordLocked(token);
                component = r != null ? r.intent.getComponent() : null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return component;
    }

    private ActivityRecord getCallingRecordLocked(IBinder token) {
        ActivityRecord r = ActivityRecord.isInStackLocked(token);
        if (r == null) {
            return null;
        }
        return r.resultTo;
    }

    public void unhandledBack() {
        this.mAmInternal.enforceCallingPermission("android.permission.FORCE_BACK", "unhandledBack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    getTopDisplayFocusedStack().unhandledBackLocked();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onBackPressedOnTaskRoot(IBinder token, IRequestFinishCallback callback) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    ActivityStack stack = r.getActivityStack();
                    if (stack == null || !stack.isSingleTaskInstance()) {
                        try {
                            callback.requestFinish();
                        } catch (RemoteException e) {
                            Slog.e("ActivityTaskManager", "Failed to invoke request finish callback", e);
                        }
                    } else {
                        this.mTaskChangeNotificationController.notifyBackPressedOnTaskRoot(r.getTaskRecord().getTaskInfo());
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveTaskToFront(IApplicationThread appThread, String callingPackage, int taskId, int flags, Bundle bOptions) {
        int[] combinedTaskIds;
        this.mAmInternal.enforceCallingPermission("android.permission.REORDER_TASKS", "moveTaskToFront()");
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.d("ActivityTaskManager", "moveTaskToFront: moving taskId=" + taskId);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityManagerService.MY_PID != Binder.getCallingPid()) {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.d("ActivityTaskManager", "Could not find task for id: " + taskId);
                        return;
                    } else if (task.inHwSplitScreenWindowingMode() && !task.isVisible()) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            for (int i : this.mHwATMSEx.getCombinedSplitScreenTaskIds(task.getStack())) {
                                TaskRecord combinedTask = this.mRootActivityContainer.anyTaskForId(i, 0);
                                combinedTask.getStack().moveToFront("hwSplitScreenToTop", combinedTask);
                            }
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                }
                moveTaskToFrontLocked(appThread, callingPackage, taskId, flags, SafeActivityOptions.fromBundle(bOptions), false);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00da, code lost:
        if (r8 == false) goto L_0x00dc;
     */
    public void moveTaskToFrontLocked(IApplicationThread appThread, String callingPackage, int taskId, int flags, SafeActivityOptions options, boolean fromRecents) {
        WindowProcessController callerApp;
        Throwable th;
        ActivityOptions realOptions;
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        if (!isSameApp(callingUid, callingPackage)) {
            String msg = "Permission Denial: moveTaskToFrontLocked() from pid=" + Binder.getCallingPid() + " as package " + callingPackage;
            Slog.w("ActivityTaskManager", msg);
            throw new SecurityException(msg);
        } else if (!checkAppSwitchAllowedLocked(callingPid, callingUid, -1, -1, "Task to front")) {
            SafeActivityOptions.abort(options);
        } else {
            long origId = Binder.clearCallingIdentity();
            if (appThread != null) {
                callerApp = getProcessController(appThread);
            } else {
                callerApp = null;
            }
            if (!getActivityStartController().obtainStarter(null, "moveTaskToFront").shouldAbortBackgroundActivityStart(callingUid, callingPid, callingPackage, -1, -1, callerApp, null, false, null) || isBackgroundActivityStartsEnabled()) {
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task == null) {
                        Slog.d("ActivityTaskManager", "Could not find task for id: " + taskId);
                        SafeActivityOptions.abort(options);
                        Binder.restoreCallingIdentity(origId);
                    } else if (getLockTaskController().isLockTaskModeViolation(task)) {
                        Slog.e("ActivityTaskManager", "moveTaskToFront: Attempt to violate Lock Task Mode");
                        SafeActivityOptions.abort(options);
                        Binder.restoreCallingIdentity(origId);
                    } else {
                        if (options != null) {
                            try {
                                realOptions = options.getOptions(this.mStackSupervisor);
                            } catch (Throwable th2) {
                                th = th2;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        } else {
                            realOptions = null;
                        }
                        this.mStackSupervisor.findTaskToMoveToFront(task, flags, realOptions, "moveTaskToFront", false);
                        ActivityRecord topActivity = task.getTopActivity();
                        if (topActivity != null) {
                            boolean z = (task.inFreeformWindowingMode() || task.inSplitScreenPrimaryWindowingMode()) ? fromRecents : fromRecents;
                            try {
                                topActivity.showStartingWindow(null, false, true, z);
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th4) {
                    th = th4;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSameApp(int callingUid, String packageName) {
        if (callingUid == 0 || callingUid == 1000) {
            return true;
        }
        if (packageName == null) {
            return false;
        }
        try {
            return UserHandle.isSameApp(callingUid, AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, UserHandle.getUserId(callingUid)));
        } catch (RemoteException e) {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkAppSwitchAllowedLocked(int sourcePid, int sourceUid, int callingPid, int callingUid, String name) {
        if (this.mAppSwitchesAllowedTime < SystemClock.uptimeMillis()) {
            return true;
        }
        if (this.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(sourceUid)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d("ActivityTaskManager", "AppSwitchAllowed has no toast visible window sourceUid:" + sourceUid);
            }
            return true;
        } else if (getRecentTasks().isCallerRecents(sourceUid) || checkComponentPermission("android.permission.STOP_APP_SWITCHES", sourcePid, sourceUid, -1, true) == 0 || checkAllowAppSwitchUid(sourceUid)) {
            return true;
        } else {
            if (callingUid != -1 && callingUid != sourceUid && (checkComponentPermission("android.permission.STOP_APP_SWITCHES", callingPid, callingUid, -1, true) == 0 || checkAllowAppSwitchUid(callingUid))) {
                return true;
            }
            Slog.w("ActivityTaskManager", name + " request from " + sourceUid + " stopped");
            return false;
        }
    }

    private boolean checkAllowAppSwitchUid(int uid) {
        ArrayMap<String, Integer> types = this.mAllowAppSwitchUids.get(UserHandle.getUserId(uid));
        if (types == null) {
            return false;
        }
        for (int i = types.size() - 1; i >= 0; i--) {
            if (types.valueAt(i).intValue() == uid) {
                return true;
            }
        }
        return false;
    }

    public void setActivityController(IActivityController controller, boolean imAMonkey) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mController = controller;
                this.mControllerIsAMonkey = imAMonkey;
                Watchdog.getInstance().setActivityController(controller);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isControllerAMonkey() {
        boolean z;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mController != null && this.mControllerIsAMonkey;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public int getTaskForActivity(IBinder token, boolean onlyRoot) {
        int taskForActivityLocked;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                taskForActivityLocked = ActivityRecord.getTaskForActivityLocked(token, onlyRoot);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return taskForActivityLocked;
    }

    public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum) {
        return getFilteredTasks(maxNum, 0, 0);
    }

    public List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode) {
        int callingUid = Binder.getCallingUid();
        ArrayList<ActivityManager.RunningTaskInfo> list = new ArrayList<>();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                    Slog.v("ActivityTaskManager", "getTasks: max=" + maxNum);
                }
                this.mRootActivityContainer.getRunningTasks(maxNum, list, ignoreActivityType, ignoreWindowingMode, callingUid, isGetTasksAllowed("getTasks", Binder.getCallingPid(), callingUid));
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return list;
    }

    public final void finishSubActivity(IBinder token, String resultWho, int requestCode) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.getActivityStack().finishSubActivityLocked(r, resultWho, requestCode);
                }
                Binder.restoreCallingIdentity(origId);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean willActivityBeVisible(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    return stack.willActivityBeVisibleLocked(token);
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTaskToStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task == null) {
                        Slog.w("ActivityTaskManager", "moveTaskToStack: No task for id=" + taskId);
                        return;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        Slog.d("ActivityTaskManager", "moveTaskToStack: moving task=" + taskId + " to stackId=" + stackId + " toTop=" + toTop);
                    }
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        throw new IllegalStateException("moveTaskToStack: No stack for stackId=" + stackId);
                    } else if (stack.isActivityTypeStandardOrUndefined()) {
                        if (stack.inSplitScreenPrimaryWindowingMode()) {
                            this.mWindowManager.setDockedStackCreateState(0, null);
                        }
                        task.reparent(stack, toTop, 1, true, false, "moveTaskToStack");
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else {
                        throw new IllegalArgumentException("moveTaskToStack: Attempt to move task " + taskId + " to stack " + stackId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void resizeStack(int stackId, Rect destBounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) {
        Throwable th;
        Throwable th2;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (animate) {
                        ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                        if (stack == null) {
                            Slog.w("ActivityTaskManager", "resizeStack: stackId " + stackId + " not found.");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        } else if (stack.getWindowingMode() != 2) {
                            throw new IllegalArgumentException("Stack: " + stackId + " doesn't support animated resize.");
                        } else if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
                            try {
                                stack.animateResizePinnedStack(null, destBounds, animationDuration, false);
                            } catch (Throwable th3) {
                                th2 = th3;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th2;
                            }
                        } else {
                            HwPCUtils.log("ActivityTaskManager", "ignore resizeStack PINNED_STACK_ID in pad pc mode");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                    } else {
                        ActivityStack stack2 = this.mRootActivityContainer.getStack(stackId);
                        if (stack2 == null) {
                            Slog.w("ActivityTaskManager", "resizeStack: stackId " + stackId + " not found.");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        Flog.i(101, "resizeStack, stackId: " + stackId + ", destBounds: " + destBounds);
                        this.mRootActivityContainer.resizeStack(stack2, destBounds, null, null, preserveWindows, allowResizeInDockedMode, false);
                    }
                } catch (Throwable th4) {
                    th2 = th4;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th2;
                }
                try {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th5) {
                    th = th5;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
        } catch (Throwable th6) {
            th = th6;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public void offsetPinnedStackBounds(int stackId, Rect compareBounds, int xOffset, int yOffset, int animationDuration) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "offsetPinnedStackBounds()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (xOffset == 0 && yOffset == 0) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        Slog.w("ActivityTaskManager", "offsetPinnedStackBounds: stackId " + stackId + " not found.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                    } else if (stack.getWindowingMode() == 2) {
                        Rect destBounds = new Rect();
                        stack.getAnimationOrCurrentBounds(destBounds);
                        if (destBounds.isEmpty() || !destBounds.equals(compareBounds)) {
                            Slog.w("ActivityTaskManager", "The current stack bounds does not matched! It may be obsolete.");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        destBounds.offset(xOffset, yOffset);
                        stack.animateResizePinnedStack(null, destBounds, animationDuration, false);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                    } else {
                        throw new IllegalArgumentException("Stack: " + stackId + " doesn't support animated resize.");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean showRecents) {
        Throwable th;
        Throwable th2;
        WindowState primaryWindow;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setTaskWindowingModeSplitScreenPrimary()");
        if (HwFreeFormUtils.isFreeFormEnable() && HwFreeFormUtils.getFreeFormStackVisible()) {
            return false;
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                int callingUid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w("ActivityTaskManager", "setTaskWindowingModeSplitScreenPrimary: No task for id=" + taskId);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        Slog.d("ActivityTaskManager", "setTaskWindowingModeSplitScreenPrimary: moving task=" + taskId + " to createMode=" + createMode + " toTop=" + toTop);
                    }
                    if (task.isActivityTypeStandardOrUndefined()) {
                        boolean z = true;
                        try {
                            String[] callingPkgNames = AppGlobals.getPackageManager().getPackagesForUid(callingUid);
                            if (callingPkgNames != null) {
                                int length = callingPkgNames.length;
                                int i = 0;
                                while (true) {
                                    if (i >= length) {
                                        break;
                                    } else if ("com.android.systemui".equals(callingPkgNames[i])) {
                                        this.mSkipShowLauncher = true;
                                        this.mWindowManager.setSplitScreenResizing(true);
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                            Slog.e("ActivityTaskManager", "setTaskWindowingModeSplitScreenPrimary get calling package remote error!");
                        }
                        try {
                            this.mWindowManager.setDockedStackCreateState(createMode, initialBounds);
                            int windowingMode = task.getWindowingMode();
                            ActivityStack stack = task.getStack();
                            if (toTop) {
                                stack.moveToFront("setTaskWindowingModeSplitScreenPrimary", task);
                            }
                            if (HwMwUtils.ENABLED) {
                                HwMwUtils.performPolicy(13, new Object[]{false, true, 3, Integer.valueOf(taskId), Integer.valueOf(stack.getStackId()), -1});
                            }
                            if (!(task.mTask == null || (primaryWindow = task.mTask.getTopVisibleAppMainWindow()) == null)) {
                                primaryWindow.showBackgroundSurfaceLocked();
                            }
                            stack.setWindowingMode(3, animate, showRecents, false, false, false);
                            this.mSkipShowLauncher = false;
                            if (windowingMode == task.getWindowingMode()) {
                                z = false;
                            }
                            try {
                                Binder.restoreCallingIdentity(ident);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return z;
                            } catch (Throwable th3) {
                                th = th3;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th2 = th4;
                            Binder.restoreCallingIdentity(ident);
                            throw th2;
                        }
                    } else {
                        throw new IllegalArgumentException("setTaskWindowingMode: Attempt to move non-standard task " + taskId + " to split-screen windowing mode");
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    Binder.restoreCallingIdentity(ident);
                    throw th2;
                }
            } catch (Throwable th6) {
                th = th6;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public void removeStacksInWindowingModes(int[] windowingModes) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStacksInWindowingModes()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.removeStacksInWindowingModes(windowingModes);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeStacksWithActivityTypes(int[] activityTypes) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStacksWithActivityTypes()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.removeStacksWithActivityTypes(activityTypes);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public ParceledListSlice<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) {
        ParceledListSlice<ActivityManager.RecentTaskInfo> recentTasks;
        int callingUid = Binder.getCallingUid();
        int userId2 = handleIncomingUser(Binder.getCallingPid(), callingUid, userId, "getRecentTasks");
        boolean allowed = isGetTasksAllowed("getRecentTasks", Binder.getCallingPid(), callingUid);
        boolean detailed = checkGetTasksPermission("android.permission.GET_DETAILED_TASKS", Binder.getCallingPid(), UserHandle.getAppId(callingUid)) == 0;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                recentTasks = this.mRecentTasks.getRecentTasks(maxNum, flags, allowed, detailed, userId2, callingUid);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return recentTasks;
    }

    /* JADX INFO: finally extract failed */
    public List<ActivityManager.StackInfo> getAllStackInfos() {
        ArrayList<ActivityManager.StackInfo> allStackInfos;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getAllStackInfos()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    allStackInfos = this.mRootActivityContainer.getAllStackInfos();
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return allStackInfos;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public ActivityManager.StackInfo getStackInfo(int windowingMode, int activityType) {
        ActivityManager.StackInfo stackInfo;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getStackInfo()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    stackInfo = this.mRootActivityContainer.getStackInfo(windowingMode, activityType);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return stackInfo;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void cancelRecentsAnimation(boolean restoreHomeStackPosition) {
        int i;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "cancelRecentsAnimation()");
        long callingUid = (long) Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService windowManagerService = this.mWindowManager;
                    if (restoreHomeStackPosition) {
                        i = 2;
                    } else {
                        i = 0;
                    }
                    windowManagerService.cancelRecentsAnimationSynchronously(i, "cancelRecentsAnimation/uid=" + callingUid);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void startLockTaskModeByToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    startLockTaskModeLocked(r.getTaskRecord(), false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void startSystemLockTaskMode(int taskId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startSystemLockTaskMode");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    task.getStack().moveToFront("startSystemLockTaskMode");
                    startLockTaskModeLocked(task, true);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void stopLockTaskModeByToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    stopLockTaskModeInternal(r.getTaskRecord(), false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void stopSystemLockTaskMode() throws RemoteException {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "stopSystemLockTaskMode");
        stopLockTaskModeInternal(null, true);
    }

    private void startLockTaskModeLocked(TaskRecord task, boolean isSystemCaller) {
        if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.w("ActivityTaskManager", "startLockTaskModeLocked: " + task);
        }
        if (task != null && task.mLockTaskAuth != 0) {
            ActivityStack stack = this.mRootActivityContainer.getTopDisplayFocusedStack();
            if (stack == null || task != stack.topTask()) {
                throw new IllegalArgumentException("Invalid task, not in foreground");
            }
            int callingUid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                this.mRootActivityContainer.removeStacksInWindowingModes(2);
                getLockTaskController().startLockTaskMode(task, isSystemCaller, callingUid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void stopLockTaskModeInternal(TaskRecord task, boolean isSystemCaller) {
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    getLockTaskController().stopLockTaskMode(task, isSystemCaller, callingUid);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
            if (tm != null) {
                tm.showInCallScreen(false);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void updateLockTaskPackages(int userId, String[] packages) {
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 0 || callingUid == 1000)) {
            this.mAmInternal.enforceCallingPermission("android.permission.UPDATE_LOCK_TASK_PACKAGES", "updateLockTaskPackages()");
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.w("ActivityTaskManager", "Whitelisting " + userId + ":" + Arrays.toString(packages));
                }
                getLockTaskController().updateLockTaskPackages(userId, packages);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isInLockTaskMode() {
        return getLockTaskModeState() != 0;
    }

    public int getLockTaskModeState() {
        int lockTaskModeState;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                lockTaskModeState = getLockTaskController().getLockTaskModeState();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return lockTaskModeState;
    }

    public void setTaskDescription(IBinder token, ActivityManager.TaskDescription td) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.ACTIVITYMANAGER_SETTASKDESCRIPTION);
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.setTaskDescription(td);
                    TaskRecord task = r.getTaskRecord();
                    task.updateTaskDescription();
                    this.mTaskChangeNotificationController.notifyTaskDescriptionChanged(task.getTaskInfo());
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public Bundle getActivityOptions(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    Bundle bundle = null;
                    if (r != null) {
                        ActivityOptions activityOptions = r.takeOptionsLocked(true);
                        if (activityOptions != null) {
                            bundle = activityOptions.toBundle();
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return bundle;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return null;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public List<IBinder> getAppTasks(String callingPackage) {
        ArrayList<IBinder> appTasksList;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.ACTIVITYMANAGER_GETAPPTASKS);
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    appTasksList = this.mRecentTasks.getAppTasksList(callingUid, callingPackage);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return appTasksList;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void finishVoiceTask(IVoiceInteractionSession session) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.finishVoiceTask(session);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isTopOfTask(IBinder token) {
        boolean z;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                z = r != null && r.getTaskRecord().getTopActivity() == r;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void notifyLaunchTaskBehindComplete(IBinder token) {
        this.mStackSupervisor.scheduleLaunchTaskBehindComplete(token);
    }

    public void notifyEnterAnimationComplete(IBinder token) {
        this.mH.post(new Runnable(token) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$08dKVvWq1bH80h9nigX6w4yunLg */
            private final /* synthetic */ IBinder f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityTaskManagerService.this.lambda$notifyEnterAnimationComplete$2$ActivityTaskManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifyEnterAnimationComplete$2$ActivityTaskManagerService(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null && r.attachedToProcess()) {
                    try {
                        r.app.getThread().scheduleEnterAnimationComplete(r.appToken);
                    } catch (RemoteException e) {
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reportAssistContextExtras(IBinder token, Bundle extras, AssistStructure structure, AssistContent content, Uri referrer) {
        IAssistDataReceiver sendReceiver;
        PendingAssistExtras pae = (PendingAssistExtras) token;
        synchronized (pae) {
            pae.result = extras;
            pae.structure = structure;
            pae.content = content;
            if (referrer != null) {
                pae.extras.putParcelable("android.intent.extra.REFERRER", referrer);
            }
            if (!(structure == null || pae.activity == null || pae.activity.getTaskRecord() == null)) {
                structure.setTaskId(pae.activity.getTaskRecord().taskId);
                structure.setActivityComponent(pae.activity.mActivityComponent);
                structure.setHomeActivity(pae.isHome);
            }
            pae.haveResult = true;
            pae.notifyAll();
            if (pae.intent == null && pae.receiver == null) {
                return;
            }
        }
        Bundle sendBundle = null;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                buildAssistBundleLocked(pae, extras);
                boolean exists = this.mPendingAssistExtras.remove(pae);
                this.mUiHandler.removeCallbacks(pae);
                if (exists) {
                    sendReceiver = pae.receiver;
                    if (!(sendReceiver == null || pae.activity == null || pae.activity.getTaskRecord() == null)) {
                        sendBundle = new Bundle();
                        sendBundle.putInt(ActivityTaskManagerInternal.ASSIST_TASK_ID, pae.activity.getTaskRecord().taskId);
                        sendBundle.putBinder(ActivityTaskManagerInternal.ASSIST_ACTIVITY_ID, pae.activity.assistToken);
                        sendBundle.putBundle(ActivityTaskManagerInternal.ASSIST_KEY_DATA, pae.extras);
                        sendBundle.putParcelable(ActivityTaskManagerInternal.ASSIST_KEY_STRUCTURE, pae.structure);
                        sendBundle.putParcelable(ActivityTaskManagerInternal.ASSIST_KEY_CONTENT, pae.content);
                        sendBundle.putBundle(ActivityTaskManagerInternal.ASSIST_KEY_RECEIVER_EXTRAS, pae.receiverExtras);
                    }
                } else {
                    return;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (sendReceiver != null) {
            try {
                sendReceiver.onHandleAssistData(sendBundle);
            } catch (RemoteException e) {
            }
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                if (TextUtils.equals(pae.intent.getAction(), "android.service.voice.VoiceInteractionService")) {
                    pae.intent.putExtras(pae.extras);
                    startVoiceInteractionServiceAsUser(pae.intent, pae.userHandle, "AssistContext");
                } else {
                    pae.intent.replaceExtras(pae.extras);
                    pae.intent.setFlags(872415232);
                    this.mInternal.closeSystemDialogs("assist");
                    try {
                        this.mContext.startActivityAsUser(pae.intent, new UserHandle(pae.userHandle));
                    } catch (ActivityNotFoundException e2) {
                        Slog.w("ActivityTaskManager", "No activity to handle assist action.", e2);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private void startVoiceInteractionServiceAsUser(Intent intent, int userHandle, String reason) {
        ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveServiceAsUser(intent, 0, userHandle);
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            Slog.e("ActivityTaskManager", "VoiceInteractionService intent does not resolve. Not starting.");
            return;
        }
        intent.setPackage(resolveInfo.serviceInfo.packageName);
        ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class)).addPowerSaveTempWhitelistApp(Process.myUid(), intent.getPackage(), (long) APP_SWITCH_DELAY_TIME, userHandle, false, reason);
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.of(userHandle));
        } catch (RuntimeException e) {
            Slog.e("ActivityTaskManager", "VoiceInteractionService failed to start.", e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x015f, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x016d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:39:0x00cb, B:54:0x0154] */
    public int addAppTask(IBinder activityToken, Intent intent, ActivityManager.TaskDescription description, Bitmap thumbnail) throws RemoteException {
        Throwable th;
        int callingUid = Binder.getCallingUid();
        long callingIdent = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (r != null) {
                        try {
                            ComponentName comp = intent.getComponent();
                            if (comp != null) {
                                if (thumbnail.getWidth() == this.mThumbnailWidth) {
                                    if (thumbnail.getHeight() == this.mThumbnailHeight) {
                                        if (intent.getSelector() != null) {
                                            intent.setSelector(null);
                                        }
                                        if (intent.getSourceBounds() != null) {
                                            intent.setSourceBounds(null);
                                        }
                                        if ((intent.getFlags() & 524288) != 0 && (intent.getFlags() & 8192) == 0) {
                                            intent.addFlags(8192);
                                        }
                                        ActivityInfo ainfo = AppGlobals.getPackageManager().getActivityInfo(comp, 1024, UserHandle.getUserId(callingUid));
                                        if (ainfo == null || ainfo.applicationInfo.uid == callingUid) {
                                            ActivityStack stack = r.getActivityStack();
                                            TaskRecord task = stack.createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(r.mUserId), ainfo, intent, null, null, false);
                                            if (!this.mRecentTasks.addToBottom(task)) {
                                                stack.removeTask(task, "addAppTask", 0);
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                Binder.restoreCallingIdentity(callingIdent);
                                                return -1;
                                            }
                                            task.lastTaskDescription.copyFrom(description);
                                            int i = task.taskId;
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            Binder.restoreCallingIdentity(callingIdent);
                                            return i;
                                        }
                                        throw new SecurityException("Can't add task for another application: target uid=" + ainfo.applicationInfo.uid + ", calling uid=" + callingUid);
                                    }
                                }
                                throw new IllegalArgumentException("Bad thumbnail size: got " + thumbnail.getWidth() + "x" + thumbnail.getHeight() + ", require " + this.mThumbnailWidth + "x" + this.mThumbnailHeight);
                            }
                            throw new IllegalArgumentException("Intent " + intent + " must specify explicit component");
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(callingIdent);
                                throw th;
                            }
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Activity does not exist; token=");
                        sb.append(activityToken);
                        throw new IllegalArgumentException(sb.toString());
                    }
                } catch (Throwable th4) {
                    th = th4;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            Binder.restoreCallingIdentity(callingIdent);
            throw th;
        }
    }

    public Point getAppTaskThumbnailSize() {
        Point point;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                point = new Point(this.mThumbnailWidth, this.mThumbnailHeight);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return point;
    }

    public void setTaskResizeable(int taskId, int resizeableMode) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 1);
                if (task == null) {
                    Slog.w("ActivityTaskManager", "setTaskResizeable: taskId=" + taskId + " not found");
                    return;
                }
                task.setResizeMode(resizeableMode);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a9  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c6  */
    public void resizeTask(int taskId, Rect bounds, int resizeMode) {
        ActivityStack stack;
        boolean preserveWindow;
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeTask()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w("ActivityTaskManager", "resizeTask: taskId=" + taskId + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityStack stack2 = task.getStack();
                    if (task.getWindowConfiguration().canResizeTask()) {
                        boolean preserveWindow2 = true;
                        if (HwPCUtils.isExtDynamicStack(task.getStackId())) {
                            if (this.mHwATMSEx.isTaskNotResizeableEx(task, bounds)) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                Binder.restoreCallingIdentity(ident);
                                return;
                            }
                        } else if (bounds == null && stack2 != null && stack2.getWindowingMode() == 5) {
                            stack = stack2.getDisplay().getOrCreateStack(1, stack2.getActivityType(), true);
                            if ((resizeMode & 1) == 0) {
                            }
                            if (stack != task.getStack()) {
                            }
                            task.resize(bounds, resizeMode, preserveWindow, false);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        } else if (!(bounds == null || stack2 == null || stack2.getWindowingMode() == 5)) {
                            stack = stack2.getDisplay().getOrCreateStack(5, stack2.getActivityType(), true);
                            if ((resizeMode & 1) == 0) {
                                preserveWindow2 = false;
                            }
                            if (stack != task.getStack()) {
                                task.reparent(stack, true, 1, true, true, "resizeTask");
                                preserveWindow = false;
                            } else {
                                preserveWindow = preserveWindow2;
                            }
                            task.resize(bounds, resizeMode, preserveWindow, false);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        stack = stack2;
                        if ((resizeMode & 1) == 0) {
                        }
                        if (stack != task.getStack()) {
                        }
                        task.resize(bounds, resizeMode, preserveWindow, false);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    throw new IllegalArgumentException("resizeTask not allowed on task=" + task);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean releaseActivityInstance(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return false;
                    }
                    boolean safelyDestroyActivityLocked = r.getActivityStack().safelyDestroyActivityLocked(r, "app-req");
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return safelyDestroyActivityLocked;
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void releaseSomeActivities(IApplicationThread appInt) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.releaseSomeActivitiesLocked(getProcessController(appInt), "low-mem");
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setLockScreenShown(boolean keyguardShowing, boolean aodShowing) {
        if (checkCallingPermission("android.permission.DEVICE_POWER") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "setKeyguardShowing: " + keyguardShowing + " aodShowing: " + aodShowing + " by pid:" + Binder.getCallingPid());
                    long ident = Binder.clearCallingIdentity();
                    if (this.mKeyguardShown != keyguardShowing) {
                        this.mKeyguardShown = keyguardShowing;
                        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw.INSTANCE, this.mAmInternal, Boolean.valueOf(keyguardShowing)));
                    }
                    try {
                        this.mKeyguardController.setKeyguardShown(keyguardShowing, aodShowing);
                        if (keyguardShowing) {
                            this.mHwATMSEx.exitSingleHandMode();
                        }
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mH.post(new Runnable(keyguardShowing) {
                /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$B6FTtGarYmExgdnI8_SwswMhaKI */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActivityTaskManagerService.this.lambda$setLockScreenShown$3$ActivityTaskManagerService(this.f$1);
                }
            });
            return;
        }
        throw new SecurityException("Requires permission android.permission.DEVICE_POWER");
    }

    public /* synthetic */ void lambda$setLockScreenShown$3$ActivityTaskManagerService(boolean keyguardShowing) {
        for (int i = this.mScreenObservers.size() - 1; i >= 0; i--) {
            this.mScreenObservers.get(i).onKeyguardStateChanged(keyguardShowing);
        }
    }

    public void onScreenAwakeChanged(boolean isAwake) {
        this.mH.post(new Runnable(isAwake) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$cU03QNPhz2EXnqFLYvA6QZbZOoY */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityTaskManagerService.this.lambda$onScreenAwakeChanged$4$ActivityTaskManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onScreenAwakeChanged$4$ActivityTaskManagerService(boolean isAwake) {
        for (int i = this.mScreenObservers.size() - 1; i >= 0; i--) {
            this.mScreenObservers.get(i).onAwakeStateChanged(isAwake);
        }
    }

    public Bitmap getTaskDescriptionIcon(String filePath, int userId) {
        int userId2 = handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTaskDescriptionIcon");
        if (new File(TaskPersister.getUserImagesDir(userId2), new File(filePath).getName()).getPath().equals(filePath) && filePath.contains("_activity_icon_")) {
            return this.mRecentTasks.getTaskDescriptionIcon(filePath);
        }
        throw new IllegalArgumentException("Bad file path: " + filePath + " passed for userId " + userId2);
    }

    public void startInPlaceAnimationOnFrontMostApplication(Bundle opts) {
        ActivityOptions activityOptions;
        SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(opts);
        if (safeOptions != null) {
            activityOptions = safeOptions.getOptions(this.mStackSupervisor);
        } else {
            activityOptions = null;
        }
        if (activityOptions == null || activityOptions.getAnimationType() != 10 || activityOptions.getCustomInPlaceResId() == 0) {
            throw new IllegalArgumentException("Expected in-place ActivityOption with valid animation");
        }
        ActivityStack focusedStack = getTopDisplayFocusedStack();
        if (focusedStack != null) {
            DisplayContent dc = focusedStack.getDisplay().mDisplayContent;
            dc.prepareAppTransition(17, false);
            dc.mAppTransition.overrideInPlaceAppTransition(activityOptions.getPackageName(), activityOptions.getCustomInPlaceResId());
            dc.executeAppTransition();
        }
    }

    public void removeStack(int stackId) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        Slog.w("ActivityTaskManager", "removeStack: No stack with id=" + stackId);
                    } else if (stack.isActivityTypeStandardOrUndefined()) {
                        this.mStackSupervisor.removeStack(stack);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else {
                        throw new IllegalArgumentException("Removing non-standard stack is not allowed.");
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveStackToDisplay(int stackId, int displayId) {
        this.mAmInternal.enforceCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "moveStackToDisplay()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        Slog.d("ActivityTaskManager", "moveStackToDisplay: moving stackId=" + stackId + " to displayId=" + displayId);
                    }
                    this.mRootActivityContainer.moveStackToDisplay(stackId, displayId, true);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void toggleFreeformWindowingMode(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    ActivityRecord r = ActivityRecord.forTokenLocked(token);
                    if (r != null) {
                        this.mHwATMSEx.toggleFreeformWindowingModeEx(r);
                        ActivityStack stack = r.getActivityStack();
                        if (stack != null) {
                            if (!stack.inFreeformWindowingMode() && stack.getWindowingMode() != 1 && !HwPCUtils.isExtDynamicStack(stack.mStackId)) {
                                if (!stack.inHwFreeFormWindowingMode()) {
                                    throw new IllegalStateException("toggleFreeformWindowingMode: You can only toggle between fullscreen and freeform.");
                                }
                            }
                            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isPcDynamicStack(stack.mStackId)) {
                                if (stack.inFreeformWindowingMode()) {
                                    this.mInFreeformSnapshot = true;
                                    stack.setWindowingMode(1);
                                    this.mInFreeformSnapshot = false;
                                    if (HwFreeFormUtils.isFreeFormEnable()) {
                                        stack.setFreeFormStackVisible(false);
                                        Flog.bdReport(991311062);
                                    }
                                } else if (stack.getParent().inFreeformWindowingMode()) {
                                    stack.setWindowingMode(0);
                                } else if (stack.inHwFreeFormWindowingMode()) {
                                    if (!HwMwUtils.ENABLED || !HwMwUtils.performPolicy(16, new Object[]{Integer.valueOf(stack.mStackId)}).getBoolean("RESULT_HWMULTISTACK", false)) {
                                        stack.setWindowingMode(1);
                                        if (HwDisplaySizeUtil.hasSideInScreen() && r.mAppWindowToken != null) {
                                            WindowManagerPolicy policy = this.mWindowManager.getPolicy();
                                            if (policy instanceof PhoneWindowManager) {
                                                PhoneWindowManager phoneWindowManager = (PhoneWindowManager) policy;
                                                WindowState mainWindow = r.mAppWindowToken.findMainWindow(false);
                                                if (mainWindow != null) {
                                                    phoneWindowManager.notchControlFilletForSideScreen(mainWindow, true);
                                                }
                                            }
                                        }
                                    }
                                    this.mTaskChangeNotificationController.notifyTaskStackChanged();
                                } else {
                                    stack.setWindowingMode(5);
                                }
                                Binder.restoreCallingIdentity(ident);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            HwPCUtils.log("ActivityTaskManager", "the task want exitFreeformMode on pc statck  so return");
                            return;
                        }
                        throw new IllegalStateException("toggleFreeformWindowingMode: the activity doesn't have a stack");
                    }
                    throw new IllegalArgumentException("toggleFreeformWindowingMode: No activity record matching token=" + token);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerTaskStackListener(ITaskStackListener listener) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "registerTaskStackListener()");
        this.mTaskChangeNotificationController.registerTaskStackListener(listener);
    }

    public void unregisterTaskStackListener(ITaskStackListener listener) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "unregisterTaskStackListener()");
        this.mTaskChangeNotificationController.unregisterTaskStackListener(listener);
    }

    public boolean requestAssistContextExtras(int requestType, IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId) {
        return enqueueAssistContext(requestType, null, null, receiver, receiverExtras, activityToken, focused, newSessionId, UserHandle.getCallingUserId(), null, 2000, 0) != null;
    }

    public boolean requestAutofillData(IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, int flags) {
        return enqueueAssistContext(2, null, null, receiver, receiverExtras, activityToken, true, true, UserHandle.getCallingUserId(), null, 2000, flags) != null;
    }

    public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle, Bundle args) {
        return enqueueAssistContext(requestType, intent, hint, null, null, null, true, true, userHandle, args, 500, 0) != null;
    }

    /* JADX INFO: finally extract failed */
    public Bundle getAssistContextExtras(int requestType) {
        PendingAssistExtras pae = enqueueAssistContext(requestType, null, null, null, null, null, true, true, UserHandle.getCallingUserId(), null, 500, 0);
        if (pae == null) {
            return null;
        }
        synchronized (pae) {
            while (!pae.haveResult) {
                try {
                    pae.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                buildAssistBundleLocked(pae, pae.result);
                this.mPendingAssistExtras.remove(pae);
                this.mUiHandler.removeCallbacks(pae);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return pae.extras;
    }

    private static int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    /* access modifiers changed from: package-private */
    public void enforceCallerIsRecentsOrHasPermission(String permission, String func) {
        if (!getRecentTasks().isCallerRecents(Binder.getCallingUid())) {
            this.mAmInternal.enforceCallingPermission(permission, func);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int checkGetTasksPermission(String permission, int pid, int uid) {
        return checkPermission(permission, pid, uid);
    }

    static int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        return checkComponentPermission(permission, pid, uid, -1, true);
    }

    public static int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
        return ActivityManagerService.checkComponentPermission(permission, pid, uid, owningUid, exported);
    }

    /* access modifiers changed from: package-private */
    public boolean isGetTasksAllowed(String caller, int callingPid, int callingUid) {
        boolean z = true;
        if (getRecentTasks().isCallerRecents(callingUid)) {
            return true;
        }
        if (checkGetTasksPermission("android.permission.REAL_GET_TASKS", callingPid, callingUid) != 0) {
            z = false;
        }
        boolean allowed = z;
        if (!allowed) {
            if (checkGetTasksPermission("android.permission.GET_TASKS", callingPid, callingUid) == 0) {
                try {
                    if (AppGlobals.getPackageManager().isUidPrivileged(callingUid)) {
                        allowed = true;
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.w("ActivityTaskManager", caller + ": caller " + callingUid + " is using old GET_TASKS but privileged; allowing");
                        }
                    }
                } catch (RemoteException e) {
                }
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.w("ActivityTaskManager", caller + ": caller " + callingUid + " does not hold REAL_GET_TASKS; limiting output");
            }
        }
        return allowed;
    }

    private PendingAssistExtras enqueueAssistContext(int requestType, Intent intent, String hint, IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId, int userHandle, Bundle args, long timeout, int flags) {
        RemoteException e;
        ActivityRecord activity;
        ActivityRecord caller;
        this.mAmInternal.enforceCallingPermission("android.permission.GET_TOP_ACTIVITY_INFO", "enqueueAssistContext()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord activity2 = getTopDisplayFocusedStack().getTopActivity();
                if (activity2 == null) {
                    Slog.w("ActivityTaskManager", "getAssistContextExtras failed: no top activity");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } else if (!activity2.attachedToProcess()) {
                    Slog.w("ActivityTaskManager", "getAssistContextExtras failed: no process for " + activity2);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } else {
                    if (!focused) {
                        ActivityRecord activity3 = ActivityRecord.forTokenLocked(activityToken);
                        if (activity3 == null) {
                            Slog.w("ActivityTaskManager", "enqueueAssistContext failed: activity for token=" + activityToken + " couldn't be found");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        } else if (!activity3.attachedToProcess()) {
                            Slog.w("ActivityTaskManager", "enqueueAssistContext failed: no process for " + activity3);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        } else {
                            activity = activity3;
                        }
                    } else if (activityToken == null || activity2 == (caller = ActivityRecord.forTokenLocked(activityToken))) {
                        activity = activity2;
                    } else {
                        Slog.w("ActivityTaskManager", "enqueueAssistContext failed: caller " + caller + " is not current top " + activity2);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    Bundle extras = new Bundle();
                    if (args != null) {
                        extras.putAll(args);
                    }
                    extras.putString("android.intent.extra.ASSIST_PACKAGE", activity.packageName);
                    extras.putInt("android.intent.extra.ASSIST_UID", activity.app.mUid);
                    PendingAssistExtras pae = new PendingAssistExtras(activity, extras, intent, hint, receiver, receiverExtras, userHandle);
                    pae.isHome = activity.isActivityTypeHome();
                    if (newSessionId) {
                        this.mViSessionId++;
                    }
                    try {
                        activity.app.getThread().requestAssistContextExtras(activity.appToken, pae, requestType, this.mViSessionId, flags);
                        this.mPendingAssistExtras.add(pae);
                        try {
                            this.mUiHandler.postDelayed(pae, timeout);
                        } catch (RemoteException e2) {
                        }
                    } catch (RemoteException e3) {
                        Slog.w("ActivityTaskManager", "getAssistContextExtras failed: crash calling " + activity);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    try {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return pae;
                    } catch (Throwable th) {
                        e = th;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw e;
                    }
                }
            } catch (Throwable th2) {
                e = th2;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw e;
            }
        }
    }

    private void buildAssistBundleLocked(PendingAssistExtras pae, Bundle result) {
        if (result != null) {
            pae.extras.putBundle("android.intent.extra.ASSIST_CONTEXT", result);
        }
        if (pae.hint != null) {
            pae.extras.putBoolean(pae.hint, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pendingAssistExtrasTimedOut(PendingAssistExtras pae) {
        IAssistDataReceiver receiver;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mPendingAssistExtras.remove(pae);
                receiver = pae.receiver;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (receiver != null) {
            Bundle sendBundle = new Bundle();
            sendBundle.putBundle(ActivityTaskManagerInternal.ASSIST_KEY_RECEIVER_EXTRAS, pae.receiverExtras);
            try {
                pae.receiver.onHandleAssistData(sendBundle);
            } catch (RemoteException e) {
            }
        }
    }

    public class PendingAssistExtras extends Binder implements Runnable {
        public final ActivityRecord activity;
        public AssistContent content = null;
        public final Bundle extras;
        public boolean haveResult = false;
        public final String hint;
        public final Intent intent;
        public boolean isHome;
        public final IAssistDataReceiver receiver;
        public Bundle receiverExtras;
        public Bundle result = null;
        public AssistStructure structure = null;
        public final int userHandle;

        public PendingAssistExtras(ActivityRecord _activity, Bundle _extras, Intent _intent, String _hint, IAssistDataReceiver _receiver, Bundle _receiverExtras, int _userHandle) {
            this.activity = _activity;
            this.extras = _extras;
            this.intent = _intent;
            this.hint = _hint;
            this.receiver = _receiver;
            this.receiverExtras = _receiverExtras;
            this.userHandle = _userHandle;
        }

        @Override // java.lang.Runnable
        public void run() {
            Slog.w("ActivityTaskManager", "getAssistContextExtras failed: timeout retrieving from " + this.activity);
            synchronized (this) {
                this.haveResult = true;
                notifyAll();
            }
            ActivityTaskManagerService.this.pendingAssistExtrasTimedOut(this);
        }
    }

    public boolean isAssistDataAllowedOnCurrentActivity() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack focusedStack = getTopDisplayFocusedStack();
                if (focusedStack != null) {
                    if (!focusedStack.isActivityTypeAssistant()) {
                        ActivityRecord activity = focusedStack.getTopActivity();
                        if (activity == null) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        }
                        int userId = activity.mUserId;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return !DevicePolicyCache.getInstance().getScreenCaptureDisabled(userId);
                    }
                }
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean showAssistFromActivity(IBinder token, Bundle args) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord caller = ActivityRecord.forTokenLocked(token);
                    ActivityRecord top = getTopDisplayFocusedStack().getTopActivity();
                    if (top != caller) {
                        Slog.w("ActivityTaskManager", "showAssistFromActivity failed: caller " + caller + " is not current top " + top);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else if (!top.nowVisible) {
                        Slog.w("ActivityTaskManager", "showAssistFromActivity failed: caller " + caller + " is not visible");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        boolean showSessionForActiveService = this.mAssistUtils.showSessionForActiveService(args, 8, (IVoiceInteractionSessionShowCallback) null, token);
                        Binder.restoreCallingIdentity(ident);
                        return showSessionForActiveService;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isRootVoiceInteraction(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return false;
                }
                boolean z = r.rootVoiceInteraction;
                WindowManagerService.resetPriorityAfterLockedSection();
                return z;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLocalVoiceInteractionStartedLocked(IBinder activity, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        ActivityRecord activityToCallback = ActivityRecord.forTokenLocked(activity);
        if (activityToCallback != null) {
            activityToCallback.setVoiceSessionLocked(voiceSession);
            try {
                activityToCallback.app.getThread().scheduleLocalVoiceInteractionStarted(activity, voiceInteractor);
                long token = Binder.clearCallingIdentity();
                try {
                    startRunningVoiceLocked(voiceSession, activityToCallback.appInfo.uid);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException e) {
                activityToCallback.clearVoiceSessionLocked();
            }
        }
    }

    private void startRunningVoiceLocked(IVoiceInteractionSession session, int targetUid) {
        Slog.d("ActivityTaskManager", "<<<  startRunningVoiceLocked()");
        this.mVoiceWakeLock.setWorkSource(new WorkSource(targetUid));
        IVoiceInteractionSession iVoiceInteractionSession = this.mRunningVoice;
        if (iVoiceInteractionSession == null || iVoiceInteractionSession.asBinder() != session.asBinder()) {
            boolean wasRunningVoice = this.mRunningVoice != null;
            this.mRunningVoice = session;
            if (!wasRunningVoice) {
                this.mVoiceWakeLock.acquire();
                updateSleepIfNeededLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void finishRunningVoiceLocked() {
        if (this.mRunningVoice != null) {
            this.mRunningVoice = null;
            this.mVoiceWakeLock.release();
            updateSleepIfNeededLocked();
        }
    }

    public void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mRunningVoice != null && this.mRunningVoice.asBinder() == session.asBinder()) {
                    if (keepAwake) {
                        this.mVoiceWakeLock.acquire();
                    } else {
                        this.mVoiceWakeLock.release();
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public ComponentName getActivityClassForToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return null;
                }
                ComponentName component = r.intent.getComponent();
                WindowManagerService.resetPriorityAfterLockedSection();
                return component;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public String getPackageForToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return null;
                }
                String str = r.packageName;
                WindowManagerService.resetPriorityAfterLockedSection();
                return str;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void showLockTaskEscapeMessage(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityRecord.forTokenLocked(token) != null) {
                    getLockTaskController().showLockTaskToast();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void keyguardGoingAway(int flags) {
        enforceNotIsolatedCaller("keyguardGoingAway");
        Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "setKeyguardGoingAway flags: " + flags + " by pid:" + Binder.getCallingPid());
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.sendLaunchTimeOutResume();
                    this.mKeyguardController.keyguardGoingAway(flags);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void positionTaskInStack(int taskId, int stackId, int position) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "positionTaskInStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        Slog.d("ActivityTaskManager", "positionTaskInStack: positioning task=" + taskId + " in stackId=" + stackId + " at position=" + position);
                    }
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task != null) {
                        ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                        if (stack == null) {
                            throw new IllegalArgumentException("positionTaskInStack: no stack for id=" + stackId);
                        } else if (!stack.isActivityTypeStandardOrUndefined()) {
                            throw new IllegalArgumentException("positionTaskInStack: Attempt to change the position of task " + taskId + " in/to non-standard stack");
                        } else if (task.getStack() == stack) {
                            stack.positionChildAt(task, position);
                        } else {
                            task.reparent(stack, position, 2, false, false, "positionTaskInStack");
                        }
                    } else {
                        throw new IllegalArgumentException("positionTaskInStack: no task for id=" + taskId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.v("ActivityTaskManager", "Report configuration: " + token + " " + horizontalSizeConfiguration + " " + verticalSizeConfigurations);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord record = ActivityRecord.isInStackLocked(token);
                if (record != null) {
                    record.setSizeConfigurations(horizontalSizeConfiguration, verticalSizeConfigurations, smallestSizeConfigurations);
                } else {
                    throw new IllegalArgumentException("reportSizeConfigurations: ActivityRecord not found for: " + token);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void swapDockedAndFullscreenStack() throws RemoteException {
        TaskRecord secondaryTask;
        boolean z;
        boolean z2;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    ActivityStack primarySplitScreenStack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
                    if (primarySplitScreenStack == null) {
                        Slog.w("ActivityTaskManager", "swapDockedAndFullscreenStack: primary split-screen stack not found.");
                        return;
                    }
                    ArrayList<TaskRecord> primaryTasks = primarySplitScreenStack.getAllTasks();
                    ActivityStack secondarySplitScreenStack = primarySplitScreenStack.getDisplay().getTopStackInWindowingMode(4);
                    if (secondarySplitScreenStack == null) {
                        Slog.w("ActivityTaskManager", "swapDockedAndFullscreenStack: secondary split-screen stack not found.");
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    TaskRecord secondaryTask2 = secondarySplitScreenStack.topTask();
                    boolean z3 = true;
                    boolean z4 = false;
                    if (!(primaryTasks == null || secondaryTask2 == null || primaryTasks.size() == 0) && !secondaryTask2.isActivityTypeHome()) {
                        if (!secondaryTask2.isActivityTypeRecents()) {
                            this.mWindowManager.startFreezingScreen(0, 0);
                            this.mWindowManager.mShouldResetTime = true;
                            secondaryTask2.reparent(primarySplitScreenStack, true, 1, true, true, "swapDockedAndFullscreenStack - sss->pss");
                            int size = primaryTasks.size();
                            int i = 0;
                            while (i < size) {
                                if (primaryTasks.get(i).taskId == secondaryTask2.taskId) {
                                    z = z4;
                                    z2 = z3;
                                    secondaryTask = secondaryTask2;
                                } else {
                                    TaskRecord taskRecord = primaryTasks.get(i);
                                    boolean z5 = z4 ? 1 : 0;
                                    Object[] objArr = z4 ? 1 : 0;
                                    Object[] objArr2 = z4 ? 1 : 0;
                                    z = z5;
                                    z2 = z3;
                                    secondaryTask = secondaryTask2;
                                    taskRecord.reparent(secondarySplitScreenStack, true, 1, true, true, "swapDockedAndFullscreenStack - pss->sss");
                                }
                                i++;
                                z3 = z2;
                                z4 = z;
                                secondaryTask2 = secondaryTask;
                            }
                            RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
                            int i2 = z4 ? 1 : 0;
                            int i3 = z4 ? 1 : 0;
                            int i4 = z4 ? 1 : 0;
                            rootActivityContainer.ensureActivitiesVisible(null, i2, z4, z3);
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                            this.mWindowManager.stopFreezingScreen();
                            Binder.restoreCallingIdentity(ident);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                    Slog.w("ActivityTaskManager", "Unable to swap tasks, either secondary or primary split-screen stack is empty.");
                    Binder.restoreCallingIdentity(ident);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void dismissSplitScreenMode(boolean toTop) {
        ActivityStack otherStack;
        ActivityRecord secondTopActivityRecord;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "dismissSplitScreenMode()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
                    if (stack == null) {
                        Slog.w("ActivityTaskManager", "dismissSplitScreenMode: primary split-screen stack not found.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    stack.getTopActivity();
                    boolean shouldfreeze = false;
                    if (!toTop) {
                        ActivityStack secondTopStack = this.mRootActivityContainer.getDefaultDisplay().getTopStackInWindowingMode(4);
                        if (secondTopStack != null) {
                            secondTopActivityRecord = secondTopStack.getTopActivity();
                        } else {
                            secondTopActivityRecord = null;
                        }
                        if (!(secondTopActivityRecord == null || secondTopActivityRecord.info == null || secondTopActivityRecord.info.name == null || !secondTopActivityRecord.info.name.contains("splitscreen.SplitScreenAppActivity"))) {
                            shouldfreeze = true;
                        }
                    }
                    if (shouldfreeze) {
                        this.mWindowManager.mShouldResetTime = true;
                        this.mWindowManager.startFreezingScreen(0, 0);
                    }
                    if (toTop) {
                        stack.moveToFront("dismissSplitScreenMode");
                    } else if (this.mRootActivityContainer.isTopDisplayFocusedStack(stack) && (otherStack = stack.getDisplay().getTopStackInWindowingMode(4)) != null) {
                        otherStack.moveToFront("dismissSplitScreenMode_other");
                    }
                    if (this.mHwATMSEx.isSwitchToMagicWin(stack.getStackId(), shouldfreeze, getGlobalConfiguration().orientation)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    stack.setWindowingMode(0);
                    if (shouldfreeze) {
                        this.mH.postDelayed(new Runnable() {
                            /* class com.android.server.wm.ActivityTaskManagerService.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                ActivityTaskManagerService.this.mWindowManager.stopFreezingScreen();
                            }
                        }, 400);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void dismissPip(boolean animate, int animationDuration) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "dismissPip()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getPinnedStack();
                    if (stack == null) {
                        Slog.w("ActivityTaskManager", "dismissPip: pinned stack not found.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (stack.getWindowingMode() == 2) {
                        if (animate) {
                            stack.animateResizePinnedStack(null, null, animationDuration, false);
                        } else {
                            this.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, true);
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                    } else {
                        throw new IllegalArgumentException("Stack: " + stack + " doesn't support animated resize.");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "suppressResizeConfigChanges()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mSuppressResizeConfigChanges = suppress;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveTasksToFullscreenStack(int fromStackId, boolean onTop) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTasksToFullscreenStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    ActivityStack stack = this.mRootActivityContainer.getStack(fromStackId);
                    if (stack != null) {
                        if (stack.isActivityTypeStandardOrUndefined()) {
                            this.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, onTop);
                        } else {
                            throw new IllegalArgumentException("You can't move tasks from non-standard stacks.");
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) {
        boolean moveTopStackActivityToPinnedStack;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTopActivityToPinnedStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mSupportsPictureInPicture) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        moveTopStackActivityToPinnedStack = this.mRootActivityContainer.moveTopStackActivityToPinnedStack(stackId);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    throw new IllegalStateException("moveTopActivityToPinnedStack:Device doesn't support picture-in-picture mode");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return moveTopStackActivityToPinnedStack;
    }

    /* JADX INFO: finally extract failed */
    public boolean isInMultiWindowMode(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else if (this.mHwATMSEx.isVideosNeedFullScreenInConfig(r.packageName)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else if (this.mHwATMSEx.isSpecialVideoForPCMode(r)) {
                        HwPCUtils.log("ActivityTaskManager", "isInMultiWindowMode video running on PC modere turn");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else if (r.inHwMagicWindowingMode()) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else {
                        boolean inMultiWindowMode = r.inMultiWindowMode();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return inMultiWindowMode;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean isInPictureInPictureMode(IBinder token) {
        boolean isInPictureInPictureMode;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isInPictureInPictureMode = isInPictureInPictureMode(ActivityRecord.forTokenLocked(token));
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return isInPictureInPictureMode;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private boolean isInPictureInPictureMode(ActivityRecord r) {
        if (r == null || r.getActivityStack() == null || !r.inPinnedWindowingMode() || r.getActivityStack().isInStackLocked(r) == null) {
            return false;
        }
        return !r.getActivityStack().getTaskStack().isAnimatingBoundsToFullscreen();
    }

    /* JADX INFO: finally extract failed */
    public boolean enterPictureInPictureMode(IBinder token, PictureInPictureParams params) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ensureValidPictureInPictureActivityParamsLocked("enterPictureInPictureMode", token, params);
                    if (isInPictureInPictureMode(r)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return true;
                    } else if (!r.checkEnterPictureInPictureState("enterPictureInPictureMode", false)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(r.getDisplayId())) {
                        final Runnable enterPipRunnable = new Runnable(r, params) {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$w9YUNuxagIopMwCOeLElktyYVWA */
                            private final /* synthetic */ ActivityRecord f$1;
                            private final /* synthetic */ PictureInPictureParams f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                ActivityTaskManagerService.this.lambda$enterPictureInPictureMode$5$ActivityTaskManagerService(this.f$1, this.f$2);
                            }
                        };
                        if (!HwActivityTaskManager.isPCMultiCastMode() || !r.inHwPCMultiStackWindowingMode() || r.getTaskRecord() == null) {
                            if (isKeyguardLocked()) {
                                dismissKeyguard(token, new KeyguardDismissCallback() {
                                    /* class com.android.server.wm.ActivityTaskManagerService.AnonymousClass2 */

                                    public void onDismissSucceeded() {
                                        ActivityTaskManagerService.this.mH.post(enterPipRunnable);
                                    }
                                }, null);
                            } else {
                                enterPipRunnable.run();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(origId);
                            return true;
                        }
                        this.mHwATMSEx.onEnteringPipForMultiDisplay(r.getTaskRecord().taskId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else {
                        HwPCUtils.log("ActivityTaskManager", "ignore enterPictureInPictureMode in pc mode");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public /* synthetic */ void lambda$enterPictureInPictureMode$5$ActivityTaskManagerService(ActivityRecord r, PictureInPictureParams params) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                r.pictureInPictureArgs.copyOnlySet(params);
                float aspectRatio = r.pictureInPictureArgs.getAspectRatio();
                List<RemoteAction> actions = r.pictureInPictureArgs.getActions();
                this.mRootActivityContainer.moveActivityToPinnedStack(r, new Rect(r.pictureInPictureArgs.getSourceRectHint()), aspectRatio, "enterPictureInPictureMode");
                ActivityStack stack = r.getActivityStack();
                stack.setPictureInPictureAspectRatio(aspectRatio);
                stack.setPictureInPictureActions(actions);
                MetricsLoggerWrapper.logPictureInPictureEnter(this.mContext, r.appInfo.uid, r.shortComponentName, r.supportsEnterPipOnTaskSwitch);
                logPictureInPictureArgs(params);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setPictureInPictureParams(IBinder token, PictureInPictureParams params) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ensureValidPictureInPictureActivityParamsLocked("setPictureInPictureParams", token, params);
                    r.pictureInPictureArgs.copyOnlySet(params);
                    if (r.inPinnedWindowingMode()) {
                        ActivityStack stack = r.getActivityStack();
                        if (!stack.isAnimatingBoundsToFullscreen()) {
                            stack.setPictureInPictureAspectRatio(r.pictureInPictureArgs.getAspectRatio());
                            stack.setPictureInPictureActions(r.pictureInPictureArgs.getActions());
                        }
                    }
                    logPictureInPictureArgs(params);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public int getMaxNumPictureInPictureActions(IBinder token) {
        return 3;
    }

    private void logPictureInPictureArgs(PictureInPictureParams params) {
        if (params.hasSetActions()) {
            MetricsLogger.histogram(this.mContext, "tron_varz_picture_in_picture_actions_count", params.getActions().size());
        }
        if (params.hasSetAspectRatio()) {
            LogMaker lm = new LogMaker(824);
            lm.addTaggedData(825, Float.valueOf(params.getAspectRatio()));
            MetricsLogger.action(lm);
        }
    }

    private ActivityRecord ensureValidPictureInPictureActivityParamsLocked(String caller, IBinder token, PictureInPictureParams params) {
        if (this.mSupportsPictureInPicture) {
            ActivityRecord r = ActivityRecord.forTokenLocked(token);
            if (r == null) {
                throw new IllegalStateException(caller + ": Can't find activity for token=" + token);
            } else if (!r.supportsPictureInPicture()) {
                throw new IllegalStateException(caller + ": Current activity does not support picture-in-picture.");
            } else if (!params.hasSetAspectRatio() || this.mWindowManager.isValidPictureInPictureAspectRatio(r.getActivityStack().mDisplayId, params.getAspectRatio())) {
                params.truncateActions(getMaxNumPictureInPictureActions(token));
                return r;
            } else {
                float minAspectRatio = this.mContext.getResources().getFloat(17105069);
                float maxAspectRatio = this.mContext.getResources().getFloat(17105068);
                throw new IllegalArgumentException(String.format(caller + ": Aspect ratio is too extreme (must be between %f and %f).", Float.valueOf(minAspectRatio), Float.valueOf(maxAspectRatio)));
            }
        } else {
            throw new IllegalStateException(caller + ": Device doesn't support picture-in-picture mode.");
        }
    }

    public IBinder getUriPermissionOwnerForActivity(IBinder activityToken) {
        Binder externalToken;
        enforceNotIsolatedCaller("getUriPermissionOwnerForActivity");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                if (r != null) {
                    externalToken = r.getUriPermissionsLocked().getExternalToken();
                } else {
                    throw new IllegalArgumentException("Activity does not exist; token=" + activityToken);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return externalToken;
    }

    /* JADX INFO: finally extract failed */
    public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeDockedStack()");
        long ident = Binder.clearCallingIdentity();
        Slog.i("ActivityTaskManager", "resizeDockedStack dockedBounds:" + dockedBounds);
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, true);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void setSplitScreenResizing(boolean resizing) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setSplitScreenResizing()");
        long ident = Binder.clearCallingIdentity();
        Slog.i("ActivityTaskManager", "setSplitScreenResizing resizing:" + resizing);
        this.mWindowManager.setSplitScreenResizing(resizing);
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.setSplitScreenResizing(resizing);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void enforceSystemHasVrFeature() {
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.vr.high_performance")) {
            throw new UnsupportedOperationException("VR mode not supported on this device!");
        }
    }

    /* JADX INFO: finally extract failed */
    public int setVrMode(IBinder token, boolean enabled, ComponentName packageName) {
        ActivityRecord r;
        enforceSystemHasVrFeature();
        VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                r = ActivityRecord.isInStackLocked(token);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (r != null) {
            int err = vrService.hasVrPackage(packageName, r.mUserId);
            if (err != 0) {
                return err;
            }
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        r.requestedVrComponent = enabled ? packageName : null;
                        if (r.isResumedActivityOnDisplay()) {
                            applyUpdateVrModeLocked(r);
                        }
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return 0;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startLocalVoiceInteraction(IBinder callingActivity, Bundle options) {
        Slog.i("ActivityTaskManager", "Activity tried to startLocalVoiceInteraction");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord activity = getTopDisplayFocusedStack().getTopActivity();
                if (ActivityRecord.forTokenLocked(callingActivity) == activity) {
                    if (this.mRunningVoice == null && activity.getTaskRecord().voiceSession == null) {
                        if (activity.voiceSession == null) {
                            if (activity.pendingVoiceInteractionStart) {
                                Slog.w("ActivityTaskManager", "Pending start of voice interaction already.");
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            activity.pendingVoiceInteractionStart = true;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).startLocalVoiceInteraction(callingActivity, options);
                            return;
                        }
                    }
                    Slog.w("ActivityTaskManager", "Already in a voice interaction, cannot start new voice interaction");
                    return;
                }
                throw new SecurityException("Only focused activity can call startVoiceInteraction");
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void stopLocalVoiceInteraction(IBinder callingActivity) {
        ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).stopLocalVoiceInteraction(callingActivity);
    }

    public boolean supportsLocalVoiceInteraction() {
        return ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).supportsLocalVoiceInteraction();
    }

    public void notifyPinnedStackAnimationStarted() {
        this.mTaskChangeNotificationController.notifyPinnedStackAnimationStarted();
    }

    public void notifyPinnedStackAnimationEnded() {
        this.mTaskChangeNotificationController.notifyPinnedStackAnimationEnded();
    }

    /* JADX INFO: finally extract failed */
    public void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizePinnedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.resizePinnedStackLocked(pinnedBounds, tempPinnedTaskBounds);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean updateDisplayOverrideConfiguration(Configuration values, int displayId) {
        this.mAmInternal.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateDisplayOverrideConfiguration()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                boolean z = false;
                if (!this.mRootActivityContainer.isDisplayAdded(displayId)) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.w("ActivityTaskManager", "Trying to update display configuration for non-existing displayId=" + displayId);
                    }
                    return false;
                }
                if (values == null && this.mWindowManager != null) {
                    values = this.mWindowManager.computeNewConfiguration(displayId);
                    if (displayId != 0 && (((this.mVrMananger.isVRDeviceConnected() && this.mVrMananger.isVirtualScreenMode() && this.mVrMananger.isVrVirtualDisplay(displayId)) || this.mHwATMSEx.isVirtualDisplayId(displayId, "padCast")) && values != null)) {
                        Slog.i("ActivityTaskManager", "update night mask into vr virtual display " + displayId + " during creation.");
                        values.uiMode = (values.uiMode & -49) | (this.mRootActivityContainer.getDisplayOverrideConfiguration(0).uiMode & 48);
                    }
                }
                if (this.mWindowManager != null) {
                    this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ.INSTANCE, this.mAmInternal, Integer.valueOf(displayId)));
                }
                long origId = Binder.clearCallingIdentity();
                if (values != null) {
                    try {
                        Settings.System.clearConfiguration(values);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                }
                updateDisplayOverrideConfigurationLocked(values, null, false, displayId, this.mTmpUpdateConfigurationResult);
                if (this.mTmpUpdateConfigurationResult.changes != 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(origId);
                WindowManagerService.resetPriorityAfterLockedSection();
                return z;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean updateConfiguration(Configuration values) {
        boolean z;
        this.mAmInternal.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = false;
                if (values == null && this.mWindowManager != null) {
                    values = this.mWindowManager.computeNewConfiguration(0);
                }
                if (this.mWindowManager != null) {
                    this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ.INSTANCE, this.mAmInternal, 0));
                }
                long origId = Binder.clearCallingIdentity();
                if (values != null) {
                    try {
                        Settings.System.clearConfiguration(values);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                }
                updateConfigurationLocked(values, null, false, false, -10000, false, this.mTmpUpdateConfigurationResult);
                if (this.mTmpUpdateConfigurationResult.changes != 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(origId);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    /* JADX INFO: finally extract failed */
    public void dismissKeyguard(IBinder token, IKeyguardDismissCallback callback, CharSequence message) {
        if (message != null) {
            this.mAmInternal.enforceCallingPermission("android.permission.SHOW_KEYGUARD_MESSAGE", "dismissKeyguard()");
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mKeyguardController.dismissKeyguard(token, callback, message);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    public void cancelTaskWindowTransition(int taskId) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "cancelTaskWindowTransition()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w("ActivityTaskManager", "cancelTaskWindowTransition: taskId=" + taskId + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    task.cancelWindowTransition();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) {
        enforceCallerIsRecentsOrHasPermission("android.permission.READ_FRAME_BUFFER", "getTaskSnapshot()");
        long ident = Binder.clearCallingIdentity();
        try {
            return getTaskSnapshot(taskId, reducedResolution, true);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution, boolean restoreFromDisk) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 1);
                if (task == null) {
                    Slog.w("ActivityTaskManager", "getTaskSnapshot: taskId=" + taskId + " not found");
                    return null;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return task.getSnapshot(reducedResolution, restoreFromDisk);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDisablePreviewScreenshots(IBinder token, boolean disable) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    Slog.w("ActivityTaskManager", "setDisablePreviewScreenshots: Unable to find activity for token=" + token);
                    return;
                }
                long origId = Binder.clearCallingIdentity();
                try {
                    r.setDisablePreviewScreenshots(disable);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getLastResumedActivityUserId() {
        this.mAmInternal.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "getLastResumedActivityUserId()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mLastResumedActivity == null) {
                    return getCurrentUserId();
                }
                int i = this.mLastResumedActivity.mUserId;
                WindowManagerService.resetPriorityAfterLockedSection();
                return i;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void updateLockTaskFeatures(int userId, int flags) {
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 0 || callingUid == 1000)) {
            this.mAmInternal.enforceCallingPermission("android.permission.UPDATE_LOCK_TASK_PACKAGES", "updateLockTaskFeatures()");
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.w("ActivityTaskManager", "Allowing features " + userId + ":0x" + Integer.toHexString(flags));
                }
                getLockTaskController().updateLockTaskFeatures(userId, flags);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setShowWhenLocked(IBinder token, boolean showWhenLocked) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setShowWhenLocked(showWhenLocked);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setInheritShowWhenLocked(IBinder token, boolean inheritShowWhenLocked) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setInheritShowWhenLocked(inheritShowWhenLocked);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setTurnScreenOn(IBinder token, boolean turnScreenOn) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setTurnScreenOn(turnScreenOn);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerRemoteAnimations(IBinder token, RemoteAnimationDefinition definition) {
        this.mAmInternal.enforceCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "registerRemoteAnimations");
        definition.setCallingPid(Binder.getCallingPid());
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.registerRemoteAnimations(definition);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerRemoteAnimationForNextActivityStart(String packageName, RemoteAnimationAdapter adapter) {
        this.mAmInternal.enforceCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "registerRemoteAnimationForNextActivityStart");
        adapter.setCallingPid(Binder.getCallingPid());
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    getActivityStartController().registerRemoteAnimationForNextActivityStart(packageName, adapter);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerRemoteAnimationsForDisplay(int displayId, RemoteAnimationDefinition definition) {
        this.mAmInternal.enforceCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "registerRemoteAnimations");
        definition.setCallingPid(Binder.getCallingPid());
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityDisplay display = this.mRootActivityContainer.getActivityDisplay(displayId);
                if (display == null) {
                    Slog.e("ActivityTaskManager", "Couldn't find display with id: " + displayId);
                    return;
                }
                long origId = Binder.clearCallingIdentity();
                try {
                    display.mDisplayContent.registerRemoteAnimations(definition);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void alwaysShowUnsupportedCompileSdkWarning(ComponentName activity) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mAppWarnings.alwaysShowUnsupportedCompileSdkWarning(activity);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setVrThread(int tid) {
        enforceSystemHasVrFeature();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                int pid = Binder.getCallingPid();
                this.mVrController.setVrThreadLocked(tid, pid, this.mProcessMap.getProcess(pid));
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setPersistentVrThread(int tid) {
        if (checkCallingPermission("android.permission.RESTRICTED_VR_ACCESS") == 0) {
            enforceSystemHasVrFeature();
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    int pid = Binder.getCallingPid();
                    this.mVrController.setPersistentVrThreadLocked(tid, pid, this.mProcessMap.getProcess(pid));
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        String msg = "Permission Denial: setPersistentVrThread() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires android.permission.RESTRICTED_VR_ACCESS";
        Slog.w("ActivityTaskManager", msg);
        throw new SecurityException(msg);
    }

    public void stopAppSwitches() {
        enforceCallerIsRecentsOrHasPermission("android.permission.STOP_APP_SWITCHES", "stopAppSwitches");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAppSwitchesAllowedTime = SystemClock.uptimeMillis() + APP_SWITCH_DELAY_TIME;
                this.mLastStopAppSwitchesTime = SystemClock.uptimeMillis();
                this.mDidAppSwitch = false;
                getActivityStartController().schedulePendingActivityLaunches(APP_SWITCH_DELAY_TIME);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void resumeAppSwitches() {
        enforceCallerIsRecentsOrHasPermission("android.permission.STOP_APP_SWITCHES", "resumeAppSwitches");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAppSwitchesAllowedTime = 0;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long getLastStopAppSwitchesTime() {
        return this.mLastStopAppSwitchesTime;
    }

    /* access modifiers changed from: package-private */
    public void onStartActivitySetDidAppSwitch() {
        if (this.mDidAppSwitch) {
            this.mAppSwitchesAllowedTime = 0;
        } else {
            this.mDidAppSwitch = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDisableNonVrUiLocked() {
        return this.mVrController.shouldDisableNonVrUiLocked();
    }

    private void applyUpdateVrModeLocked(ActivityRecord r) {
        if (!(r.requestedVrComponent == null || r.getDisplayId() == 0)) {
            Slog.i("ActivityTaskManager", "Moving " + r.shortComponentName + " from display " + r.getDisplayId() + " to main display for VR");
            this.mRootActivityContainer.moveStackToDisplay(r.getStackId(), 0, true);
        }
        this.mH.post(new Runnable(r) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$nuSrfdXdOXcutw3SV8Ualpreu30 */
            private final /* synthetic */ ActivityRecord f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityTaskManagerService.this.lambda$applyUpdateVrModeLocked$6$ActivityTaskManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$applyUpdateVrModeLocked$6$ActivityTaskManagerService(ActivityRecord r) {
        if (this.mVrController.onVrModeChanged(r)) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    boolean disableNonVrUi = this.mVrController.shouldDisableNonVrUiLocked();
                    this.mWindowManager.disableNonVrUi(disableNonVrUi);
                    if (disableNonVrUi) {
                        this.mRootActivityContainer.removeStacksInWindowingModes(2);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public int getPackageScreenCompatMode(String packageName) {
        int packageScreenCompatModeLocked;
        enforceNotIsolatedCaller("getPackageScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                packageScreenCompatModeLocked = this.mCompatModePackages.getPackageScreenCompatModeLocked(packageName);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return packageScreenCompatModeLocked;
    }

    public void setPackageScreenCompatMode(String packageName, int mode) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setPackageScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mCompatModePackages.setPackageScreenCompatModeLocked(packageName, mode);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean getPackageAskScreenCompat(String packageName) {
        boolean packageAskCompatModeLocked;
        enforceNotIsolatedCaller("getPackageAskScreenCompat");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                packageAskCompatModeLocked = this.mCompatModePackages.getPackageAskCompatModeLocked(packageName);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return packageAskCompatModeLocked;
    }

    public void setPackageAskScreenCompat(String packageName, boolean ask) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setPackageAskScreenCompat");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mCompatModePackages.setPackageAskCompatModeLocked(packageName, ask);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public static String relaunchReasonToString(int relaunchReason) {
        if (relaunchReason == 1) {
            return "window_resize";
        }
        if (relaunchReason != 2) {
            return null;
        }
        return "free_resize";
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getTopDisplayFocusedStack() {
        return this.mRootActivityContainer.getTopDisplayFocusedStack();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskPersisterLocked(TaskRecord task, boolean flush) {
        this.mRecentTasks.notifyTaskPersisterLocked(task, flush);
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardLocked() {
        return this.mKeyguardController.isKeyguardLocked();
    }

    public void clearLaunchParamsForPackages(List<String> packageNames) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "clearLaunchParamsForPackages");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                for (int i = 0; i < packageNames.size(); i++) {
                    this.mStackSupervisor.mLaunchParamsPersister.removeRecordForPackage(packageNames.get(i));
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDisplayToSingleTaskInstance(int displayId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setDisplayToSingleTaskInstance");
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityDisplay display = this.mRootActivityContainer.getActivityDisplayOrCreate(displayId);
            if (display != null) {
                display.setDisplayToSingleTaskInstance();
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpLastANRLocked(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER LAST ANR (dumpsys activity lastanr)");
        String str = this.mLastANRState;
        if (str == null) {
            pw.println("  <no ANR has occurred since boot>");
        } else {
            pw.println(str);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0064, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0065, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
        throw r4;
     */
    public void dumpLastANRTracesLocked(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER LAST ANR TRACES (dumpsys activity lastanr-traces)");
        File[] files = new File("/data/anr").listFiles();
        if (ArrayUtils.isEmpty(files)) {
            pw.println("  <no ANR has occurred since boot>");
            return;
        }
        File latest = null;
        for (File f : files) {
            if (latest == null || latest.lastModified() < f.lastModified()) {
                latest = f;
            }
        }
        pw.print("File: ");
        pw.print(latest.getName());
        pw.println();
        try {
            BufferedReader in = new BufferedReader(new FileReader(latest));
            while (true) {
                String line = in.readLine();
                if (line != null) {
                    pw.println(line);
                } else {
                    in.close();
                    return;
                }
            }
        } catch (IOException e) {
            pw.print("Unable to read: ");
            pw.print(e);
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        dumpActivitiesLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage, "ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)");
    }

    /* access modifiers changed from: package-private */
    public void dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage, String header) {
        pw.println(header);
        boolean printedAnything = this.mRootActivityContainer.dumpActivities(fd, pw, dumpAll, dumpClient, dumpPackage);
        boolean needSep = printedAnything;
        if (ActivityStackSupervisor.printThisActivity(pw, this.mRootActivityContainer.getTopResumedActivity(), dumpPackage, needSep, "  ResumedActivity: ")) {
            printedAnything = true;
            needSep = false;
        }
        if (dumpPackage == null) {
            if (needSep) {
                pw.println();
            }
            printedAnything = true;
            this.mStackSupervisor.dump(pw, "  ");
        }
        if (!printedAnything) {
            pw.println("  (nothing)");
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpActivityContainersLocked(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER STARTER (dumpsys activity containers)");
        this.mRootActivityContainer.dumpChildrenNames(pw, " ");
        pw.println(" ");
    }

    /* access modifiers changed from: package-private */
    public void dumpActivityStarterLocked(PrintWriter pw, String dumpPackage) {
        pw.println("ACTIVITY MANAGER STARTER (dumpsys activity starter)");
        getActivityStartController().dump(pw, "", dumpPackage);
    }

    /* access modifiers changed from: protected */
    public boolean dumpActivity(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll, boolean dumpVisibleStacksOnly, boolean dumpFocusedStackOnly) {
        Throwable th;
        ArrayList<ActivityRecord> activities;
        Throwable th2;
        TaskRecord lastTask;
        ActivityTaskManagerService activityTaskManagerService = this;
        synchronized (activityTaskManagerService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                try {
                    activities = activityTaskManagerService.mRootActivityContainer.getDumpActivities(name, dumpVisibleStacksOnly, dumpFocusedStackOnly);
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (activities.size() <= 0) {
            return false;
        }
        String[] newArgs = new String[(args.length - opti)];
        System.arraycopy(args, opti, newArgs, 0, args.length - opti);
        boolean needSep = false;
        long start = SystemClock.uptimeMillis();
        int i = activities.size() - 1;
        TaskRecord lastTask2 = null;
        while (true) {
            if (i < 0) {
                break;
            }
            ActivityRecord r = activities.get(i);
            Flog.i(101, "dumpActivity " + r);
            if (needSep) {
                pw.println();
            }
            synchronized (activityTaskManagerService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = r.getTaskRecord();
                    if (lastTask2 != task) {
                        try {
                            pw.print("TASK ");
                            pw.print(task.affinity);
                            pw.print(" id=");
                            pw.print(task.taskId);
                            pw.print(" userId=");
                            pw.println(task.userId);
                            if (dumpAll) {
                                task.dump(pw, "  ");
                            }
                            lastTask = task;
                        } catch (Throwable th5) {
                            th2 = th5;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th6) {
                                    th2 = th6;
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th2;
                        }
                    } else {
                        lastTask = lastTask2;
                    }
                    try {
                    } catch (Throwable th7) {
                        th2 = th7;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th2;
                    }
                } catch (Throwable th8) {
                    th2 = th8;
                    while (true) {
                        break;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th2;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            dumpActivity("  ", fd, pw, activities.get(i), newArgs, dumpAll);
            if (SystemClock.uptimeMillis() - start > 1000) {
                Flog.e(101, "dumpActivity timeout, skip left");
                break;
            }
            i--;
            activityTaskManagerService = this;
            needSep = true;
            lastTask2 = lastTask;
            newArgs = newArgs;
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    private void dumpActivity(String prefix, FileDescriptor fd, PrintWriter pw, ActivityRecord r, String[] args, boolean dumpAll) {
        String str;
        String innerPrefix = prefix + "  ";
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("ACTIVITY ");
                pw.print(r.shortComponentName);
                pw.print(" ");
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.hasProcess()) {
                    pw.println(r.app.getPid());
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        long start = SystemClock.uptimeMillis();
        if (r.attachedToProcess()) {
            pw.flush();
            try {
                TransferPipe tp = new TransferPipe();
                try {
                    r.app.getThread().dumpActivity(tp.getWriteFd(), r.appToken, innerPrefix, args);
                    tp.go(fd, 60);
                } finally {
                    tp.kill();
                }
            } catch (IOException e) {
                Flog.e(101, "dumpActivity error " + e.toString());
                pw.println(innerPrefix + "Failure while dumping the activity: " + e);
            } catch (RemoteException e2) {
                Flog.e(101, "dumpActivity error " + e2.toString());
                pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("dumpActivity cost ");
        sb.append(SystemClock.uptimeMillis() - start);
        sb.append(" for ");
        if (r.hasProcess()) {
            str = "" + r.app.getPid();
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(" ");
        sb.append(r.shortComponentName);
        Flog.i(101, sb.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeSleepStateToProto(ProtoOutputStream proto, int wakeFullness, boolean testPssMode) {
        long sleepToken = proto.start(1146756268059L);
        proto.write(1159641169921L, PowerManagerInternal.wakefulnessToProtoEnum(wakeFullness));
        Iterator<ActivityTaskManagerInternal.SleepToken> it = this.mRootActivityContainer.mSleepTokens.iterator();
        while (it.hasNext()) {
            proto.write(2237677961218L, it.next().toString());
        }
        proto.write(1133871366147L, this.mSleeping);
        proto.write(1133871366148L, this.mShuttingDown);
        proto.write(1133871366149L, testPssMode);
        proto.end(sleepToken);
    }

    /* access modifiers changed from: package-private */
    public int getCurrentUserId() {
        return this.mAmInternal.getCurrentUserId();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceNotIsolatedCaller(String caller) {
        if (UserHandle.isIsolated(Binder.getCallingUid())) {
            throw new SecurityException("Isolated process not allowed to call " + caller);
        }
    }

    public Configuration getConfiguration() {
        Configuration ci;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ci = new Configuration(getGlobalConfigurationForCallingPid());
                ci.userSetLocale = false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return ci;
    }

    /* access modifiers changed from: package-private */
    public Configuration getGlobalConfiguration() {
        return this.mRootActivityContainer.getConfiguration();
    }

    /* access modifiers changed from: package-private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale) {
        return updateConfigurationLocked(values, starting, initLocale, false);
    }

    /* access modifiers changed from: package-private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean deferResume) {
        return updateConfigurationLocked(values, starting, initLocale, false, -10000, deferResume);
    }

    /* JADX INFO: finally extract failed */
    public void updatePersistentConfiguration(Configuration values, int userId) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    updateConfigurationLocked(values, null, false, true, userId, false);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean persistent, int userId, boolean deferResume) {
        return updateConfigurationLocked(values, starting, initLocale, persistent, userId, deferResume, null);
    }

    /* access modifiers changed from: package-private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean persistent, int userId, boolean deferResume, UpdateConfigurationResult result) {
        int changes = 0;
        WindowManagerService windowManagerService = this.mWindowManager;
        if (windowManagerService != null) {
            windowManagerService.deferSurfaceLayout();
        }
        if (values != null) {
            try {
                changes = updateGlobalConfigurationLocked(values, initLocale, persistent, userId, deferResume);
            } catch (Throwable th) {
                WindowManagerService windowManagerService2 = this.mWindowManager;
                if (windowManagerService2 != null) {
                    windowManagerService2.continueSurfaceLayout();
                }
                throw th;
            }
        }
        boolean kept = ensureConfigAndVisibilityAfterUpdate(starting, changes);
        WindowManagerService windowManagerService3 = this.mWindowManager;
        if (windowManagerService3 != null) {
            windowManagerService3.continueSurfaceLayout();
        }
        if (result != null) {
            result.changes = changes;
            result.activityRelaunched = !kept;
        }
        return kept;
    }

    private int updateGlobalConfigurationLocked(Configuration values, boolean initLocale, boolean persistent, int userId, boolean deferResume) {
        int newNightMask;
        int oldNightMask;
        int oldOrientation;
        this.mTempConfig.setTo(getGlobalConfiguration());
        int oldOrientation2 = this.mTempConfig.orientation;
        int oldNightMask2 = this.mTempConfig.uiMode & 48;
        int newNightMask2 = values.uiMode & 48;
        if (!(newNightMask2 == 0 || oldNightMask2 == newNightMask2)) {
            Slog.i(TAG_CONFIGURATION, "clear snapshot reason : change night mask from " + oldNightMask2 + " to " + newNightMask2);
            this.mWindowManager.mTaskSnapshotController.clearSnapshot();
            int vrVirtualDisplayId = this.mVrMananger.getTopVrVirtualDisplayId();
            if (this.mVrMananger.isVRDeviceConnected() && this.mVrMananger.isVirtualScreenMode() && vrVirtualDisplayId != 0) {
                Slog.i(TAG_CONFIGURATION, "update night mask into vr virtual display " + vrVirtualDisplayId);
                Configuration vrConfig = new Configuration(this.mRootActivityContainer.getDisplayOverrideConfiguration(vrVirtualDisplayId));
                vrConfig.uiMode = (vrConfig.uiMode & -49) | newNightMask2;
                performDisplayOverrideConfigUpdate(vrConfig, deferResume, vrVirtualDisplayId);
                this.mTempConfig.setTo(getGlobalConfiguration());
            }
            performPadCastVirtualDisplayConfig(deferResume, newNightMask2);
        }
        int changes = this.mTempConfig.updateFrom(values);
        WindowManagerService windowManagerService = this.mWindowManager;
        if (!(windowManagerService == null || windowManagerService.mIsPerfBoost || oldOrientation2 == this.mTempConfig.orientation || this.mTempConfig.orientation == 0 || oldOrientation2 == 0)) {
            this.mWindowManager.mIsPerfBoost = true;
            UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{0});
        }
        if (changes == 0) {
            performDisplayOverrideConfigUpdate(values, deferResume, 0);
            return 0;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.i(TAG_CONFIGURATION, "Updating global configuration to: " + values);
        }
        IHwConfiguration oldConfigEx = getGlobalConfiguration().extraConfig;
        IHwConfiguration newConfigEx = this.mTempConfig.extraConfig;
        int oldConfigHwt = oldConfigEx.getConfigItem(1);
        int newConfigHwt = newConfigEx.getConfigItem(1);
        getCurrentUserId();
        if (oldConfigHwt != newConfigHwt) {
            Process.updateHwThemeZipsAndSomeIcons(getCurrentUserId());
        }
        EventLog.writeEvent(2719, changes);
        StatsLog.write(66, values.colorMode, values.densityDpi, values.fontScale, values.hardKeyboardHidden, values.keyboard, values.keyboardHidden, values.mcc, values.mnc, values.navigation, values.navigationHidden, values.orientation, values.screenHeightDp, values.screenLayout, values.screenWidthDp, values.smallestScreenWidthDp, values.touchscreen, values.uiMode);
        if (!initLocale && !values.getLocales().isEmpty() && values.userSetLocale) {
            LocaleList locales = values.getLocales();
            int bestLocaleIndex = 0;
            if (locales.size() > 1) {
                if (this.mSupportedSystemLocales == null) {
                    this.mSupportedSystemLocales = Resources.getSystem().getAssets().getLocales();
                }
                bestLocaleIndex = Math.max(0, locales.getFirstMatchIndex(this.mSupportedSystemLocales));
            }
            SystemProperties.set("persist.sys.locale", locales.get(bestLocaleIndex).toLanguageTag());
            LocaleList.setDefault(locales, bestLocaleIndex);
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k.INSTANCE, this, locales.get(bestLocaleIndex)));
        }
        HwThemeManager.updateSimpleUIConfig(this.mContext.getContentResolver(), this.mTempConfig, changes);
        this.mTempConfig.seq = increaseConfigurationSeqLocked();
        this.mRootActivityContainer.onConfigurationChanged(this.mTempConfig);
        Slog.i("ActivityTaskManager", "Config changes=" + Integer.toHexString(changes) + " " + this.mTempConfig);
        this.mUsageStatsInternal.reportConfigurationChange(this.mTempConfig, this.mAmInternal.getCurrentUserId());
        updateShouldShowDialogsLocked(this.mTempConfig);
        AttributeCache ac = AttributeCache.instance();
        if (ac != null) {
            ac.updateConfiguration(this.mTempConfig);
        }
        this.mSystemThread.applyConfigurationToResources(this.mTempConfig);
        Configuration configCopy = new Configuration(this.mTempConfig);
        if (persistent && Settings.System.hasInterestingConfigurationChanges(changes)) {
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4.INSTANCE, this, Integer.valueOf(userId), configCopy));
        }
        if (!this.mHwATMSEx.isOverrideConfigByMagicWin(configCopy)) {
            SparseArray<WindowProcessController> pidMap = this.mProcessMap.getPidMap();
            int i = pidMap.size() - 1;
            while (i >= 0) {
                WindowProcessController app = pidMap.get(pidMap.keyAt(i));
                if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    oldOrientation = oldOrientation2;
                    String str = TAG_CONFIGURATION;
                    oldNightMask = oldNightMask2;
                    StringBuilder sb = new StringBuilder();
                    newNightMask = newNightMask2;
                    sb.append("Update process config of ");
                    sb.append(app.mName);
                    sb.append(" to new config ");
                    sb.append(configCopy);
                    Slog.v(str, sb.toString());
                } else {
                    oldOrientation = oldOrientation2;
                    oldNightMask = oldNightMask2;
                    newNightMask = newNightMask2;
                }
                app.onConfigurationChanged(configCopy);
                i--;
                oldOrientation2 = oldOrientation;
                oldNightMask2 = oldNightMask;
                newNightMask2 = newNightMask;
            }
        }
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$swA_sUfSJdP8eC8AA9Iby3SuOY.INSTANCE, this.mAmInternal, Integer.valueOf(changes), Boolean.valueOf(initLocale)));
        performDisplayOverrideConfigUpdate(this.mRootActivityContainer.getConfiguration(), deferResume, 0);
        return changes;
    }

    private void performPadCastVirtualDisplayConfig(boolean deferResume, int newNightMask) {
        int padCastVirtualDisplayId = this.mHwATMSEx.getVirtualDisplayId("padCast");
        if (padCastVirtualDisplayId != -1 && !this.mHwATMSEx.isMirrorCast("padCast")) {
            String str = TAG_CONFIGURATION;
            Slog.i(str, "update night mask into pad cast virtual display " + padCastVirtualDisplayId);
            Configuration padCastConfig = new Configuration(this.mRootActivityContainer.getDisplayOverrideConfiguration(padCastVirtualDisplayId));
            padCastConfig.uiMode = (padCastConfig.uiMode & -49) | newNightMask;
            performDisplayOverrideConfigUpdate(padCastConfig, deferResume, padCastVirtualDisplayId);
            this.mTempConfig.setTo(getGlobalConfiguration());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateDisplayOverrideConfigurationLocked(Configuration values, ActivityRecord starting, boolean deferResume, int displayId) {
        return updateDisplayOverrideConfigurationLocked(values, starting, deferResume, displayId, null);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0032: APUT  (r5v2 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r7v1 android.view.IApplicationToken$Stub) */
    /* access modifiers changed from: package-private */
    public boolean updateDisplayOverrideConfigurationLocked(Configuration values, ActivityRecord starting, boolean deferResume, int displayId, UpdateConfigurationResult result) {
        int changes = 0;
        WindowManagerService windowManagerService = this.mWindowManager;
        if (windowManagerService != null) {
            windowManagerService.deferSurfaceLayout();
        }
        boolean z = true;
        if (values != null) {
            if (displayId == 0) {
                try {
                    if (HwMwUtils.ENABLED) {
                        Object[] objArr = new Object[3];
                        objArr[0] = Integer.valueOf(getGlobalConfiguration().orientation);
                        objArr[1] = Integer.valueOf(values.orientation);
                        objArr[2] = starting != null ? starting.appToken : null;
                        HwMwUtils.performPolicy(4, objArr);
                    }
                    changes = updateGlobalConfigurationLocked(values, false, false, -10000, deferResume);
                } catch (Throwable th) {
                    WindowManagerService windowManagerService2 = this.mWindowManager;
                    if (windowManagerService2 != null) {
                        windowManagerService2.continueSurfaceLayout();
                    }
                    throw th;
                }
            } else {
                changes = performDisplayOverrideConfigUpdate(values, deferResume, displayId);
            }
        }
        boolean kept = ensureConfigAndVisibilityAfterUpdate(starting, changes);
        WindowManagerService windowManagerService3 = this.mWindowManager;
        if (windowManagerService3 != null) {
            windowManagerService3.continueSurfaceLayout();
        }
        if (result != null) {
            result.changes = changes;
            if (kept) {
                z = false;
            }
            result.activityRelaunched = z;
        }
        return kept;
    }

    private int performDisplayOverrideConfigUpdate(Configuration values, boolean deferResume, int displayId) {
        this.mTempConfig.setTo(this.mRootActivityContainer.getDisplayOverrideConfiguration(displayId));
        int changes = this.mTempConfig.updateFrom(values);
        if (changes != 0) {
            Slog.i("ActivityTaskManager", "Override config changes=" + Integer.toHexString(changes) + " " + this.mTempConfig + " for displayId=" + displayId);
            this.mRootActivityContainer.setDisplayOverrideConfiguration(this.mTempConfig, displayId);
            if (((changes & 4096) != 0) && displayId == 0) {
                this.mAppWarnings.onDensityChanged();
                this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM.INSTANCE, this.mAmInternal, 24, 7));
            }
        }
        return changes;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEventDispatchingLocked(boolean booted) {
        this.mWindowManager.setEventDispatching(booted && !this.mShuttingDown);
    }

    /* access modifiers changed from: private */
    public void sendPutConfigurationForUserMsg(int userId, Configuration config) {
        Settings.System.putConfigurationForUser(this.mContext.getContentResolver(), config, userId);
    }

    /* access modifiers changed from: private */
    public void sendLocaleToMountDaemonMsg(Locale l) {
        try {
            IStorageManager storageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            Log.d("ActivityTaskManager", "Storing locale " + l.toLanguageTag() + " for decryption UI");
            storageManager.setField("SystemLocale", l.toLanguageTag());
        } catch (RemoteException e) {
            Log.e("ActivityTaskManager", "Error storing locale for decryption UI", e);
        }
    }

    /* access modifiers changed from: private */
    public void expireStartAsCallerTokenMsg(IBinder permissionToken) {
        this.mStartActivitySources.remove(permissionToken);
        this.mExpiredStartAsCallerTokens.add(permissionToken);
    }

    /* access modifiers changed from: private */
    public void forgetStartAsCallerTokenMsg(IBinder permissionToken) {
        this.mExpiredStartAsCallerTokens.remove(permissionToken);
    }

    /* access modifiers changed from: package-private */
    public boolean isActivityStartsLoggingEnabled() {
        return this.mAmInternal.isActivityStartsLoggingEnabled();
    }

    /* access modifiers changed from: package-private */
    public boolean isBackgroundActivityStartsEnabled() {
        return this.mAmInternal.isBackgroundActivityStartsEnabled();
    }

    /* access modifiers changed from: package-private */
    public void enableScreenAfterBoot(boolean booted) {
        EventLog.writeEvent(3050, SystemClock.uptimeMillis());
        Jlog.d(34, "JL_BOOT_PROGRESS_ENABLE_SCREEN");
        this.mWindowManager.enableScreenAfterBoot();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                updateEventDispatchingLocked(booted);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    static long getInputDispatchingTimeoutLocked(ActivityRecord r) {
        if (r == null || !r.hasProcess()) {
            return APP_SWITCH_DELAY_TIME;
        }
        return getInputDispatchingTimeoutLocked(r.app);
    }

    private static long getInputDispatchingTimeoutLocked(WindowProcessController r) {
        return r != null ? r.getInputDispatchingTimeout() : APP_SWITCH_DELAY_TIME;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateShouldShowDialogsLocked(Configuration config) {
        boolean z = false;
        boolean inputMethodExists = (config.keyboard == 1 && config.touchscreen == 1 && config.navigation == 1) ? false : true;
        int modeType = config.uiMode & 15;
        boolean uiModeSupportsDialogs = (modeType == 3 || (modeType == 6 && Build.IS_USER) || modeType == 4 || modeType == 7) ? false : true;
        boolean hideDialogsSet = Settings.Global.getInt(this.mContext.getContentResolver(), "hide_error_dialogs", 0) != 0;
        if (inputMethodExists && uiModeSupportsDialogs && !hideDialogsSet) {
            z = true;
        }
        this.mShowDialogs = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFontScaleIfNeeded(int userId) {
        float scaleFactor = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "font_scale", 1.0f, userId);
        synchronized (this) {
            if (getGlobalConfiguration().fontScale != scaleFactor) {
                Configuration configuration = this.mWindowManager.computeNewConfiguration(0);
                configuration.fontScale = scaleFactor;
                updatePersistentConfiguration(configuration, userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSleepingOrShuttingDownLocked() {
        return isSleepingLocked() || this.mShuttingDown;
    }

    public boolean isSleepingLocked() {
        return this.mSleeping;
    }

    /* access modifiers changed from: package-private */
    public void setResumedActivityUncheckLocked(ActivityRecord r, String reason) {
        IVoiceInteractionSession session;
        this.mHwATMSEx.setResumedActivityUncheckLocked(this.mLastResumedActivity, r, reason);
        TaskRecord task = r.getTaskRecord();
        if (!task.isActivityTypeStandard()) {
            r.appTimeTracker = null;
        } else if (this.mCurAppTimeTracker != r.appTimeTracker) {
            AppTimeTracker appTimeTracker = this.mCurAppTimeTracker;
            if (appTimeTracker != null) {
                appTimeTracker.stop();
                this.mH.obtainMessage(1, this.mCurAppTimeTracker).sendToTarget();
                this.mRootActivityContainer.clearOtherAppTimeTrackers(r.appTimeTracker);
                this.mCurAppTimeTracker = null;
            }
            if (r.appTimeTracker != null) {
                this.mCurAppTimeTracker = r.appTimeTracker;
                startTimeTrackingFocusedActivityLocked();
            }
        } else {
            startTimeTrackingFocusedActivityLocked();
        }
        if (task.voiceInteractor != null) {
            startRunningVoiceLocked(task.voiceSession, r.info.applicationInfo.uid);
        } else {
            finishRunningVoiceLocked();
            ActivityRecord activityRecord = this.mLastResumedActivity;
            if (activityRecord != null) {
                TaskRecord lastResumedActivityTask = activityRecord.getTaskRecord();
                if (lastResumedActivityTask == null || lastResumedActivityTask.voiceSession == null) {
                    session = this.mLastResumedActivity.voiceSession;
                } else {
                    session = lastResumedActivityTask.voiceSession;
                }
                if (session != null) {
                    finishVoiceTask(session);
                }
            }
        }
        if (!(this.mLastResumedActivity == null || r.mUserId == this.mLastResumedActivity.mUserId)) {
            this.mAmInternal.sendForegroundProfileChanged(r.mUserId);
        }
        updateResumedAppTrace(r);
        this.mLastResumedActivity = r;
        r.getDisplay().setFocusedApp(r, true);
        applyUpdateLockStateLocked(r);
        applyUpdateVrModeLocked(r);
        EventLogTags.writeAmSetResumedActivity(r.mUserId, r.shortComponentName, reason);
        notifyActivityState(r, ActivityStack.ActivityState.RESUMED);
    }

    /* access modifiers changed from: package-private */
    public ActivityTaskManagerInternal.SleepToken acquireSleepToken(String tag, int displayId) {
        ActivityTaskManagerInternal.SleepToken token;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                token = this.mRootActivityContainer.createSleepToken(tag, displayId);
                updateSleepIfNeededLocked();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public void updateSleepIfNeededLocked() {
        ActivityDisplay defaultDisplay;
        int i;
        DisplayContent dc;
        boolean shouldSleep = !this.mRootActivityContainer.hasAwakeDisplay();
        boolean wasSleeping = this.mSleeping;
        boolean updateOomAdj = false;
        if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.i(TAG_KEYGUARD, "updateSleep shouldSleep:" + shouldSleep + " wasSleeping:" + wasSleeping + " callers=" + Debug.getCallers(5));
        } else {
            Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "updateSleep shouldSleep:" + shouldSleep + " wasSleeping:" + wasSleeping);
        }
        if (!shouldSleep) {
            if (wasSleeping) {
                this.mSleeping = false;
                StatsLog.write(14, 2);
                startTimeTrackingFocusedActivityLocked();
                this.mTopProcessState = 2;
                Slog.d("ActivityTaskManager", "Top Process State changed to PROCESS_STATE_TOP");
                this.mStackSupervisor.comeOutOfSleepIfNeededLocked();
            }
            this.mRootActivityContainer.applySleepTokens(true);
            if (wasSleeping) {
                updateOomAdj = true;
            }
        } else if (!this.mSleeping && shouldSleep) {
            this.mSleeping = true;
            StatsLog.write(14, 1);
            AppTimeTracker appTimeTracker = this.mCurAppTimeTracker;
            if (appTimeTracker != null) {
                appTimeTracker.stop();
            }
            this.mTopProcessState = 13;
            Slog.d("ActivityTaskManager", "Top Process State changed to PROCESS_STATE_TOP_SLEEPING");
            this.mStackSupervisor.goingToSleepLocked();
            updateResumedAppTrace(null);
            updateOomAdj = true;
            if (HwDisplaySizeUtil.hasSideInScreen() && (dc = this.mWindowManager.getDefaultDisplayContentLocked()) != null) {
                dc.mSideSurfaceBox.hideSideBox(true);
            }
        }
        if (updateOomAdj) {
            H h = this.mH;
            ActivityManagerInternal activityManagerInternal = this.mAmInternal;
            Objects.requireNonNull(activityManagerInternal);
            h.post(new Runnable(activityManagerInternal) {
                /* class com.android.server.wm.$$Lambda$yIIsPVyXvnU3Rv8mcliitgIpSs */
                private final /* synthetic */ ActivityManagerInternal f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.updateOomAdj();
                }
            });
        }
        if (!(wasSleeping == shouldSleep || (defaultDisplay = this.mRootActivityContainer.getDefaultDisplay()) == null)) {
            for (int stackNdx = defaultDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = defaultDisplay.getChildAt(stackNdx);
                ActivityRecord activityRecord = stack.topRunningActivityLocked();
                if (activityRecord != null && activityRecord.nowVisible) {
                    IHwActivityTaskManagerServiceEx iHwActivityTaskManagerServiceEx = this.mHwATMSEx;
                    if (shouldSleep) {
                        i = 1102;
                    } else {
                        i = 1101;
                    }
                    iHwActivityTaskManagerServiceEx.reportAppWindowMode(i, activityRecord, stack.getWindowingMode(), shouldSleep ? "ScreenOff" : "ScreenOn");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateOomAdj() {
        H h = this.mH;
        ActivityManagerInternal activityManagerInternal = this.mAmInternal;
        Objects.requireNonNull(activityManagerInternal);
        h.post(new Runnable(activityManagerInternal) {
            /* class com.android.server.wm.$$Lambda$yIIsPVyXvnU3Rv8mcliitgIpSs */
            private final /* synthetic */ ActivityManagerInternal f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateOomAdj();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateCpuStats() {
        H h = this.mH;
        ActivityManagerInternal activityManagerInternal = this.mAmInternal;
        Objects.requireNonNull(activityManagerInternal);
        h.post(new Runnable(activityManagerInternal) {
            /* class com.android.server.wm.$$Lambda$LYW1ECaEajjYgarzgKdTZ4O1fi0 */
            private final /* synthetic */ ActivityManagerInternal f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.updateCpuStats();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateBatteryStats(ActivityRecord component, boolean resumed) {
        if (component.app == null) {
            Slog.e("ActivityTaskManager", "Error calling updateBatteryStats.");
            return;
        }
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$hT1kyMEAhvB1Uxr0DFAlnuU3cQ.INSTANCE, this.mAmInternal, component.mActivityComponent, Integer.valueOf(component.app.mUid), Integer.valueOf(component.mUserId), Boolean.valueOf(resumed)));
    }

    /* access modifiers changed from: package-private */
    public void updateActivityUsageStats(ActivityRecord activity, int event) {
        ActivityRecord rootActivity;
        ComponentName taskRoot = null;
        TaskRecord task = activity.getTaskRecord();
        if (!(task == null || (rootActivity = task.getRootActivity()) == null)) {
            taskRoot = rootActivity.mActivityComponent;
        }
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw.INSTANCE, this.mAmInternal, activity.mActivityComponent, Integer.valueOf(activity.mUserId), Integer.valueOf(event), activity.appToken, taskRoot));
    }

    /* access modifiers changed from: package-private */
    public void setBooting(boolean booting) {
        this.mAmInternal.setBooting(booting);
    }

    /* access modifiers changed from: package-private */
    public boolean isBooting() {
        return this.mAmInternal.isBooting();
    }

    /* access modifiers changed from: package-private */
    public void setBooted(boolean booted) {
        this.mAmInternal.setBooted(booted);
    }

    /* access modifiers changed from: package-private */
    public boolean isBooted() {
        return this.mAmInternal.isBooted();
    }

    /* access modifiers changed from: package-private */
    public void postFinishBooting(boolean finishBooting, boolean enableScreen) {
        this.mH.post(new Runnable(finishBooting, enableScreen) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$hgT7_BsCohDVg6qQfaw5Xpx0yQ */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityTaskManagerService.this.lambda$postFinishBooting$7$ActivityTaskManagerService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$postFinishBooting$7$ActivityTaskManagerService(boolean finishBooting, boolean enableScreen) {
        if (finishBooting) {
            this.mAmInternal.finishBooting();
        }
        if (enableScreen) {
            this.mInternal.enableScreenAfterBoot(isBooted());
        }
    }

    /* access modifiers changed from: package-private */
    public void setHeavyWeightProcess(ActivityRecord root) {
        this.mHeavyWeightProcess = root.app;
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0.INSTANCE, this, root.app, root.intent, Integer.valueOf(root.mUserId)));
    }

    /* access modifiers changed from: package-private */
    public void clearHeavyWeightProcessIfEquals(WindowProcessController proc) {
        WindowProcessController windowProcessController = this.mHeavyWeightProcess;
        if (windowProcessController != null && windowProcessController == proc) {
            this.mHeavyWeightProcess = null;
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc.INSTANCE, this, Integer.valueOf(proc.mUserId)));
        }
    }

    /* access modifiers changed from: private */
    public void cancelHeavyWeightProcessNotification(int userId) {
        INotificationManager inm = NotificationManager.getService();
        if (inm != null) {
            try {
                inm.cancelNotificationWithTag("android", (String) null, 11, userId);
            } catch (RuntimeException e) {
                Slog.w("ActivityTaskManager", "Error canceling notification for service", e);
            } catch (RemoteException e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void postHeavyWeightProcessNotification(WindowProcessController proc, Intent intent, int userId) {
        INotificationManager inm;
        if (proc != null && (inm = NotificationManager.getService()) != null) {
            try {
                Context context = this.mContext.createPackageContext(proc.mInfo.packageName, 0);
                String text = this.mContext.getString(17040242, context.getApplicationInfo().loadLabel(context.getPackageManager()));
                try {
                    inm.enqueueNotificationWithTag("android", "android", (String) null, 11, new Notification.Builder(context, SystemNotificationChannels.HEAVY_WEIGHT_APP).setSmallIcon(17303544).setWhen(0).setOngoing(true).setTicker(text).setColor(this.mContext.getColor(17170460)).setContentTitle(text).setContentText(this.mContext.getText(17040243)).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, new UserHandle(userId))).build(), userId);
                } catch (RuntimeException e) {
                    Slog.w("ActivityTaskManager", "Error showing notification for heavy-weight app", e);
                } catch (RemoteException e2) {
                }
            } catch (PackageManager.NameNotFoundException e3) {
                Slog.w("ActivityTaskManager", "Unable to create context for heavy notification", e3);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IIntentSender getIntentSenderLocked(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions) {
        ActivityRecord activity;
        if (type == 3) {
            ActivityRecord activity2 = ActivityRecord.isInStackLocked(token);
            if (activity2 == null) {
                Slog.w("ActivityTaskManager", "Failed createPendingResult: activity " + token + " not in any stack");
                return null;
            } else if (activity2.finishing) {
                Slog.w("ActivityTaskManager", "Failed createPendingResult: activity " + activity2 + " is finishing");
                return null;
            } else {
                activity = activity2;
            }
        } else {
            activity = null;
        }
        PendingIntentRecord rec = this.mPendingIntentController.getIntentSender(type, packageName, callingUid, userId, token, resultWho, requestCode, intents, resolvedTypes, flags, bOptions);
        if (!((flags & 536870912) != 0) && type == 3) {
            if (activity.pendingResults == null) {
                activity.pendingResults = new HashSet<>();
            }
            activity.pendingResults.add(rec.ref);
        }
        return rec;
    }

    private void startTimeTrackingFocusedActivityLocked() {
        AppTimeTracker appTimeTracker;
        ActivityRecord resumedActivity = this.mRootActivityContainer.getTopResumedActivity();
        if (!this.mSleeping && (appTimeTracker = this.mCurAppTimeTracker) != null && resumedActivity != null) {
            appTimeTracker.start(resumedActivity.packageName);
        }
    }

    private void updateResumedAppTrace(ActivityRecord resumed) {
        ActivityRecord activityRecord = this.mTracedResumedActivity;
        if (activityRecord != null) {
            Trace.asyncTraceEnd(64, constructResumedTraceName(activityRecord.packageName), 0);
        }
        if (resumed != null) {
            Trace.asyncTraceBegin(64, constructResumedTraceName(resumed.packageName), 0);
        }
        this.mTracedResumedActivity = resumed;
    }

    private String constructResumedTraceName(String packageName) {
        return "focused app: " + packageName;
    }

    private boolean ensureConfigAndVisibilityAfterUpdate(ActivityRecord starting, int changes) {
        ActivityStack mainStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (mainStack == null) {
            return true;
        }
        if (changes != 0 && starting == null) {
            starting = mainStack.topRunningActivityLocked();
        }
        if (starting == null) {
            return true;
        }
        boolean kept = starting.ensureActivityConfiguration(changes, false);
        this.mRootActivityContainer.ensureActivitiesVisible(starting, changes, false);
        return kept;
    }

    public /* synthetic */ void lambda$scheduleAppGcsLocked$8$ActivityTaskManagerService() {
        this.mAmInternal.scheduleAppGcs();
    }

    /* access modifiers changed from: package-private */
    public void scheduleAppGcsLocked() {
        this.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$yEgPBZvesgjR6r_sca6FAEYeiA */

            @Override // java.lang.Runnable
            public final void run() {
                ActivityTaskManagerService.this.lambda$scheduleAppGcsLocked$8$ActivityTaskManagerService();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        return this.mCompatModePackages.compatibilityInfoForPackageLocked(ai);
    }

    /* access modifiers changed from: package-private */
    public IPackageManager getPackageManager() {
        return AppGlobals.getPackageManager();
    }

    /* access modifiers changed from: package-private */
    public PackageManagerInternal getPackageManagerInternalLocked() {
        if (this.mPmInternal == null) {
            this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPmInternal;
    }

    /* access modifiers changed from: package-private */
    public PermissionPolicyInternal getPermissionPolicyInternal() {
        if (this.mPermissionPolicyInternal == null) {
            this.mPermissionPolicyInternal = (PermissionPolicyInternal) LocalServices.getService(PermissionPolicyInternal.class);
        }
        return this.mPermissionPolicyInternal;
    }

    /* access modifiers changed from: package-private */
    public AppWarnings getAppWarningsLocked() {
        return this.mAppWarnings;
    }

    /* access modifiers changed from: package-private */
    public Intent getHomeIntent() {
        String str = this.mTopAction;
        String str2 = this.mTopData;
        Intent intent = new Intent(str, str2 != null ? Uri.parse(str2) : null);
        intent.setComponent(this.mTopComponent);
        intent.addFlags(256);
        if (this.mFactoryTest != 1) {
            intent.addCategory("android.intent.category.HOME");
            intent.addFlags(512);
        }
        this.mHwATMSEx.changeStartActivityIfNeed(intent);
        return intent;
    }

    /* access modifiers changed from: package-private */
    public Intent getSecondaryHomeIntent(String preferredPackage) {
        String str = this.mTopAction;
        String str2 = this.mTopData;
        Intent intent = new Intent(str, str2 != null ? Uri.parse(str2) : null);
        boolean useSystemProvidedLauncher = this.mContext.getResources().getBoolean(17891564);
        if (preferredPackage == null || useSystemProvidedLauncher) {
            intent.setComponent(ComponentName.unflattenFromString(this.mContext.getResources().getString(17039889)));
        } else {
            intent.setPackage(preferredPackage);
        }
        intent.addFlags(256);
        if (this.mFactoryTest != 1) {
            intent.addCategory("android.intent.category.SECONDARY_HOME");
        }
        return intent;
    }

    /* access modifiers changed from: package-private */
    public ApplicationInfo getAppInfoForUser(ApplicationInfo info, int userId) {
        if (info == null) {
            return null;
        }
        ApplicationInfo newInfo = new ApplicationInfo(info);
        newInfo.initForUser(userId);
        return newInfo;
    }

    /* access modifiers changed from: package-private */
    public WindowProcessController getProcessController(String processName, int uid) {
        if (uid == 1000) {
            SparseArray<WindowProcessController> procs = (SparseArray) this.mProcessNames.getMap().get(processName);
            if (procs == null) {
                return null;
            }
            int procCount = procs.size();
            for (int i = 0; i < procCount; i++) {
                int procUid = procs.keyAt(i);
                if (!UserHandle.isApp(procUid) && UserHandle.isSameUser(procUid, uid)) {
                    return procs.valueAt(i);
                }
            }
        }
        return (WindowProcessController) this.mProcessNames.get(processName, uid);
    }

    /* access modifiers changed from: package-private */
    public WindowProcessController getProcessController(IApplicationThread thread) {
        if (thread == null) {
            return null;
        }
        IBinder threadBinder = thread.asBinder();
        ArrayMap<String, SparseArray<WindowProcessController>> pmap = this.mProcessNames.getMap();
        for (int i = pmap.size() - 1; i >= 0; i--) {
            SparseArray<WindowProcessController> procs = pmap.valueAt(i);
            if (procs == null) {
                Slog.e("ActivityTaskManager", "get procs is not exist, key :" + pmap.keyAt(i));
            } else {
                for (int j = procs.size() - 1; j >= 0; j--) {
                    WindowProcessController proc = procs.valueAt(j);
                    if (proc.hasThread() && proc.getThread().asBinder() == threadBinder) {
                        return proc;
                    }
                }
                continue;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowProcessController getProcessController(int pid, int uid) {
        WindowProcessController proc = this.mProcessMap.getProcess(pid);
        if (proc != null && UserHandle.isApp(uid) && proc.mUid == uid) {
            return proc;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getUidState(int uid) {
        return this.mActiveUids.getUidState(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isUidForeground(int uid) {
        return this.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceOwner(int uid) {
        return uid >= 0 && this.mDeviceOwnerUid == uid;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceOwnerUid(int uid) {
        this.mDeviceOwnerUid = uid;
    }

    /* access modifiers changed from: package-private */
    public String getPendingTempWhitelistTagForUidLocked(int uid) {
        return this.mPendingTempWhitelist.get(uid);
    }

    /* access modifiers changed from: package-private */
    public void logAppTooSlow(WindowProcessController app, long startTime, String msg) {
    }

    /* access modifiers changed from: package-private */
    public boolean isAssociatedCompanionApp(int userId, int uid) {
        Set<Integer> allUids = this.mCompanionAppUidsMap.get(Integer.valueOf(userId));
        if (allUids == null) {
            return false;
        }
        return allUids.contains(Integer.valueOf(uid));
    }

    /* access modifiers changed from: package-private */
    public final class H extends Handler {
        static final int DELAY_KILL_PROCS_FOR_REMOVED_TASK_MSG = 10087;
        static final int DELAY_TIME_FOR_KILL_PROCS = 200;
        static final int FIRST_ACTIVITY_STACK_MSG = 100;
        static final int FIRST_SUPERVISOR_STACK_MSG = 200;
        static final int REPORT_TIME_TRACKER_MSG = 1;

        H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ((AppTimeTracker) msg.obj).deliverResult(ActivityTaskManagerService.this.mContext);
            } else if (i == ActivityTaskManagerService.STOP_FREEZE_SCREEN) {
                Slog.i("ActivityTaskManager", "STOP_FREEZE_SCREEN");
                ActivityTaskManagerService.this.mWindowManager.stopFreezingScreen();
            } else if (i == DELAY_KILL_PROCS_FOR_REMOVED_TASK_MSG) {
                Slog.i("ActivityTaskManager", "DELAY_KILL_PROCS_FOR_REMOVED_TASK_MSG");
                ActivityTaskManagerService.this.filterProcsListToKill((ArrayList) msg.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void filterProcsListToKill(ArrayList<Object> procsToKill) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (procsToKill != null) {
                    Iterator<Object> iterator = procsToKill.iterator();
                    while (iterator.hasNext()) {
                        if (((WindowProcessController) iterator.next()).hasActivities()) {
                            iterator.remove();
                        }
                    }
                } else {
                    return;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (procsToKill.size() > 0) {
            this.mAmInternal.killProcessesForRemovedTask(procsToKill);
        }
    }

    /* access modifiers changed from: package-private */
    public final class UiHandler extends Handler {
        static final int DISMISS_DIALOG_UI_MSG = 1;

        public UiHandler() {
            super(UiThread.get().getLooper(), null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ((Dialog) msg.obj).dismiss();
            }
        }
    }

    final class LocalService extends ActivityTaskManagerInternal {
        LocalService() {
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityTaskManagerInternal.SleepToken acquireSleepToken(String tag, int displayId) {
            Preconditions.checkNotNull(tag);
            return ActivityTaskManagerService.this.acquireSleepToken(tag, displayId);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ComponentName getHomeActivityForUser(int userId) {
            ComponentName componentName;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord homeActivity = ActivityTaskManagerService.this.mRootActivityContainer.getDefaultDisplayHomeActivityForUser(userId);
                    componentName = homeActivity == null ? null : homeActivity.mActivityComponent;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return componentName;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onLocalVoiceInteractionStarted(IBinder activity, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.onLocalVoiceInteractionStartedLocked(activity, voiceSession, voiceInteractor);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyAppTransitionStarting(SparseIntArray reasons, long timestamp) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mStackSupervisor.getActivityMetricsLogger().notifyTransitionStarting(reasons, timestamp);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyAppTransitionFinished() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mStackSupervisor.notifyAppTransitionDone();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyAppTransitionCancelled() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mStackSupervisor.notifyAppTransitionDone();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public List<IBinder> getTopVisibleActivities() {
            List<IBinder> topVisibleActivities;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    topVisibleActivities = ActivityTaskManagerService.this.mRootActivityContainer.getTopVisibleActivities();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return topVisibleActivities;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyDockedStackMinimizedChanged(boolean minimized) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.setDockedStackMinimized(minimized);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivitiesAsPackage(String packageName, int userId, Intent[] intents, Bundle bOptions) {
            int packageUid;
            Throwable th;
            Preconditions.checkNotNull(intents, "intents");
            String[] resolvedTypes = new String[intents.length];
            long ident = Binder.clearCallingIdentity();
            for (int i = 0; i < intents.length; i++) {
                try {
                    resolvedTypes[i] = intents[i].resolveTypeIfNeeded(ActivityTaskManagerService.this.mContext.getContentResolver());
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    packageUid = 0;
                    return ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(packageUid, packageName, intents, resolvedTypes, null, SafeActivityOptions.fromBundle(bOptions), userId, false, null, false);
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            try {
                packageUid = AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, userId);
                Binder.restoreCallingIdentity(ident);
            } catch (RemoteException e2) {
                Binder.restoreCallingIdentity(ident);
                packageUid = 0;
                return ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(packageUid, packageName, intents, resolvedTypes, null, SafeActivityOptions.fromBundle(bOptions), userId, false, null, false);
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            return ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(packageUid, packageName, intents, resolvedTypes, null, SafeActivityOptions.fromBundle(bOptions), userId, false, null, false);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivitiesInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
            int startActivitiesInPackage;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startActivitiesInPackage = ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(uid, realCallingPid, realCallingUid, callingPackage, intents, resolvedTypes, resultTo, options, userId, validateIncomingUser, originatingPendingIntent, allowBackgroundActivityStart);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startActivitiesInPackage;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivityInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, SafeActivityOptions options, int userId, TaskRecord inTask, String reason, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
            int startActivityInPackage;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startActivityInPackage = ActivityTaskManagerService.this.getActivityStartController().startActivityInPackage(uid, realCallingPid, realCallingUid, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, options, userId, inTask, reason, validateIncomingUser, originatingPendingIntent, allowBackgroundActivityStart);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startActivityInPackage;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivityAsUser(IApplicationThread caller, String callerPacakge, Intent intent, Bundle options, int userId) {
            ActivityTaskManagerService activityTaskManagerService = ActivityTaskManagerService.this;
            return activityTaskManagerService.startActivityAsUser(caller, callerPacakge, intent, intent.resolveTypeIfNeeded(activityTaskManagerService.mContext.getContentResolver()), null, null, 0, 268435456, null, options, userId, false);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyKeyguardFlagsChanged(Runnable callback, int displayId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityDisplay activityDisplay = ActivityTaskManagerService.this.mRootActivityContainer.getActivityDisplay(displayId);
                    if (activityDisplay != null) {
                        DisplayContent dc = activityDisplay.mDisplayContent;
                        boolean wasTransitionSet = dc.mAppTransition.getAppTransition() != 0;
                        if (!wasTransitionSet) {
                            dc.prepareAppTransition(0, false);
                        }
                        ActivityTaskManagerService.this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                        if (!wasTransitionSet) {
                            dc.executeAppTransition();
                        }
                    } else {
                        return;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (callback != null) {
                callback.run();
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyKeyguardTrustedChanged() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mKeyguardController.isKeyguardShowing(0)) {
                        ActivityTaskManagerService.this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setVr2dDisplayId(int vr2dDisplayId) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d("ActivityTaskManager", "setVr2dDisplayId called for: " + vr2dDisplayId);
            }
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mVr2dDisplayId = vr2dDisplayId;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setFocusedActivity(IBinder token) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.forTokenLocked(token);
                    if (r == null) {
                        throw new IllegalArgumentException("setFocusedActivity: No activity record matching token=" + token);
                    } else if (r.moveFocusableActivityToTop("setFocusedActivity")) {
                        ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void registerScreenObserver(ActivityTaskManagerInternal.ScreenObserver observer) {
            ActivityTaskManagerService.this.mScreenObservers.add(observer);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isCallerRecents(int callingUid) {
            return ActivityTaskManagerService.this.getRecentTasks().isCallerRecents(callingUid);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isRecentsComponentHomeActivity(int userId) {
            return ActivityTaskManagerService.this.getRecentTasks().isRecentsComponentHomeActivity(userId);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void cancelRecentsAnimation(boolean restoreHomeStackPosition) {
            ActivityTaskManagerService.this.cancelRecentsAnimation(restoreHomeStackPosition);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void enforceCallerIsRecentsOrHasPermission(String permission, String func) {
            ActivityTaskManagerService.this.enforceCallerIsRecentsOrHasPermission(permission, func);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyActiveVoiceInteractionServiceChanged(ComponentName component) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mActiveVoiceInteractionServiceComponent = component;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setAllowAppSwitches(String type, int uid, int userId) {
            if (ActivityTaskManagerService.this.mAmInternal.isUserRunning(userId, 1)) {
                synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        ArrayMap<String, Integer> types = ActivityTaskManagerService.this.mAllowAppSwitchUids.get(userId);
                        if (types == null) {
                            if (uid >= 0) {
                                types = new ArrayMap<>();
                                ActivityTaskManagerService.this.mAllowAppSwitchUids.put(userId, types);
                            } else {
                                return;
                            }
                        }
                        if (uid < 0) {
                            types.remove(type);
                        } else {
                            types.put(type, Integer.valueOf(uid));
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUserStopped(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.getRecentTasks().unloadUserDataFromMemoryLocked(userId);
                    ActivityTaskManagerService.this.mAllowAppSwitchUids.remove(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isGetTasksAllowed(String caller, int callingPid, int callingUid) {
            boolean isGetTasksAllowed;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isGetTasksAllowed = ActivityTaskManagerService.this.isGetTasksAllowed(caller, callingPid, callingUid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isGetTasksAllowed;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessAdded(WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mProcessNames.put(proc.mName, proc.mUid, proc);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessRemoved(String name, int uid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mProcessNames.remove(name, uid);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onCleanUpApplicationRecord(WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                if (proc == ActivityTaskManagerService.this.mHomeProcess) {
                    ActivityTaskManagerService.this.mHomeProcess = null;
                    ActivityTaskManagerService.this.mHwATMSEx.reportHomeProcess(null);
                }
                if (proc == ActivityTaskManagerService.this.mPreviousProcess) {
                    ActivityTaskManagerService.this.mPreviousProcess = null;
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int getTopProcessState() {
            int i;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                i = ActivityTaskManagerService.this.mTopProcessState;
            }
            return i;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isHeavyWeightProcess(WindowProcessController proc) {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                z = proc == ActivityTaskManagerService.this.mHeavyWeightProcess;
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearHeavyWeightProcessIfEquals(WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.clearHeavyWeightProcessIfEquals(proc);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void finishHeavyWeightApp() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mHeavyWeightProcess != null) {
                        ActivityTaskManagerService.this.mHeavyWeightProcess.finishActivities();
                    }
                    ActivityTaskManagerService.this.clearHeavyWeightProcessIfEquals(ActivityTaskManagerService.this.mHeavyWeightProcess);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isSleeping() {
            boolean isSleepingLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                isSleepingLocked = ActivityTaskManagerService.this.isSleepingLocked();
            }
            return isSleepingLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isShuttingDown() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = ActivityTaskManagerService.this.mShuttingDown;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean shuttingDown(boolean booted, int timeout) {
            boolean shutdownLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mShuttingDown = true;
                    ActivityTaskManagerService.this.mRootActivityContainer.prepareForShutdown();
                    ActivityTaskManagerService.this.updateEventDispatchingLocked(booted);
                    ActivityTaskManagerService.this.notifyTaskPersisterLocked(null, true);
                    shutdownLocked = ActivityTaskManagerService.this.mStackSupervisor.shutdownLocked(timeout);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return shutdownLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void enableScreenAfterBoot(boolean booted) {
            EventLog.writeEvent(3050, SystemClock.uptimeMillis());
            Jlog.d(34, "JL_BOOT_PROGRESS_ENABLE_SCREEN");
            ActivityTaskManagerService.this.mWindowManager.enableScreenAfterBoot();
            ActivityTaskManagerService.this.updateEventDispatchingLocked(booted);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean showStrictModeViolationDialog() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = ActivityTaskManagerService.this.mShowDialogs && !ActivityTaskManagerService.this.mSleeping && !ActivityTaskManagerService.this.mShuttingDown;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void showSystemReadyErrorDialogsIfNeeded() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (AppGlobals.getPackageManager().hasSystemUidErrors()) {
                        Slog.e("ActivityTaskManager", "UIDs on the system are inconsistent, you need to wipe your data partition or your device will be unstable.");
                        ActivityTaskManagerService.this.mUiHandler.post(new Runnable() {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$LocalService$hXNJNh8HjV10X1ZEOI6o0Yzmq8o */

                            @Override // java.lang.Runnable
                            public final void run() {
                                ActivityTaskManagerService.LocalService.this.lambda$showSystemReadyErrorDialogsIfNeeded$0$ActivityTaskManagerService$LocalService();
                            }
                        });
                    }
                } catch (RemoteException e) {
                }
                try {
                    if (!Build.isBuildConsistent()) {
                        Slog.e("ActivityTaskManager", "Build fingerprint is not consistent, warning user");
                        ActivityTaskManagerService.this.mUiHandler.post(new Runnable() {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$LocalService$xIfx_hFO4SXyNq34zoEHe3S9eU */

                            @Override // java.lang.Runnable
                            public final void run() {
                                ActivityTaskManagerService.LocalService.this.lambda$showSystemReadyErrorDialogsIfNeeded$1$ActivityTaskManagerService$LocalService();
                            }
                        });
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public /* synthetic */ void lambda$showSystemReadyErrorDialogsIfNeeded$0$ActivityTaskManagerService$LocalService() {
            if (ActivityTaskManagerService.this.mShowDialogs) {
                AlertDialog d = new BaseErrorDialog(ActivityTaskManagerService.this.mUiContext);
                d.getWindow().setType(2010);
                d.setCancelable(false);
                d.setTitle(ActivityTaskManagerService.this.mUiContext.getText(17039593));
                d.setMessage(ActivityTaskManagerService.this.mUiContext.getText(17041356));
                d.setButton(-1, ActivityTaskManagerService.this.mUiContext.getText(17039370), ActivityTaskManagerService.this.mUiHandler.obtainMessage(1, d));
                d.show();
            }
        }

        public /* synthetic */ void lambda$showSystemReadyErrorDialogsIfNeeded$1$ActivityTaskManagerService$LocalService() {
            if (ActivityTaskManagerService.this.mShowDialogs) {
                AlertDialog d = new BaseErrorDialog(ActivityTaskManagerService.this.mUiContext);
                d.getWindow().setType(2010);
                d.setCancelable(false);
                d.setTitle(ActivityTaskManagerService.this.mUiContext.getText(17039593));
                d.setMessage(ActivityTaskManagerService.this.mUiContext.getText(17041355));
                d.setButton(-1, ActivityTaskManagerService.this.mUiContext.getText(17039370), ActivityTaskManagerService.this.mUiHandler.obtainMessage(1, d));
                d.show();
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessMapped(int pid, WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProcessMap.put(pid, proc);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessUnMapped(int pid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProcessMap.remove(pid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageDataCleared(String name) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mCompatModePackages.handlePackageDataClearedLocked(name);
                    ActivityTaskManagerService.this.mAppWarnings.onPackageDataCleared(name);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageUninstalled(String name) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mAppWarnings.onPackageUninstalled(name);
                    ActivityTaskManagerService.this.mCompatModePackages.handlePackageUninstalledLocked(name);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageAdded(String name, boolean replacing) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mCompatModePackages.handlePackageAddedLocked(name, replacing);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageReplaced(ApplicationInfo aInfo) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.updateActivityApplicationInfo(aInfo);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public CompatibilityInfo compatibilityInfoForPackage(ApplicationInfo ai) {
            CompatibilityInfo compatibilityInfoForPackageLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    compatibilityInfoForPackageLocked = ActivityTaskManagerService.this.compatibilityInfoForPackageLocked(ai);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return compatibilityInfoForPackageLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onImeWindowSetOnDisplay(int pid, int displayId) {
            if (!InputMethodSystemProperty.MULTI_CLIENT_IME_ENABLED) {
                if (pid != ActivityManagerService.MY_PID && pid >= 0) {
                    synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityDisplay activityDisplay = ActivityTaskManagerService.this.mRootActivityContainer.getActivityDisplay(displayId);
                            if (activityDisplay == null) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.w("ActivityTaskManager", "Trying to update display configuration for non-existing displayId=" + displayId);
                                }
                                return;
                            }
                            WindowProcessController process = ActivityTaskManagerService.this.mProcessMap.getProcess(pid);
                            if (process == null) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.w("ActivityTaskManager", "Trying to update display configuration for invalid process, pid=" + pid);
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            process.registerDisplayConfigurationListenerLocked(activityDisplay);
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.w("ActivityTaskManager", "Trying to update display configuration for system/invalid process.");
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void sendActivityResult(int callingUid, IBinder activityToken, String resultWho, int requestCode, int resultCode, Intent data) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (!(r == null || r.getActivityStack() == null)) {
                        r.getActivityStack().sendActivityResultLocked(callingUid, r, resultWho, requestCode, resultCode, data);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearPendingResultForActivity(IBinder activityToken, WeakReference<PendingIntentRecord> pir) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (!(r == null || r.pendingResults == null)) {
                        r.pendingResults.remove(pir);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityTaskManagerInternal.ActivityTokens getTopActivityForTask(int taskId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord taskRecord = ActivityTaskManagerService.this.mRootActivityContainer.anyTaskForId(taskId);
                    if (taskRecord == null) {
                        Slog.w("ActivityTaskManager", "getApplicationThreadForTopActivity failed: Requested task not found");
                        return null;
                    }
                    ActivityRecord activity = taskRecord.getTopActivity();
                    if (activity == null) {
                        Slog.w("ActivityTaskManager", "getApplicationThreadForTopActivity failed: Requested activity not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else if (!activity.attachedToProcess()) {
                        Slog.w("ActivityTaskManager", "getApplicationThreadForTopActivity failed: No process for " + activity);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else {
                        ActivityTaskManagerInternal.ActivityTokens activityTokens = new ActivityTaskManagerInternal.ActivityTokens(activity.appToken, activity.assistToken, activity.app.getThread());
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return activityTokens;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public IIntentSender getIntentSender(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions) {
            IIntentSender intentSenderLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    intentSenderLocked = ActivityTaskManagerService.this.getIntentSenderLocked(type, packageName, callingUid, userId, token, resultWho, requestCode, intents, resolvedTypes, flags, bOptions);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return intentSenderLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityServiceConnectionsHolder getServiceConnectionsHolder(IBinder token) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return null;
                    }
                    if (r.mServiceConnectionsHolder == null) {
                        r.mServiceConnectionsHolder = new ActivityServiceConnectionsHolder(ActivityTaskManagerService.this, r);
                    }
                    ActivityServiceConnectionsHolder activityServiceConnectionsHolder = r.mServiceConnectionsHolder;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return activityServiceConnectionsHolder;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public Intent getHomeIntent() {
            Intent homeIntent;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    homeIntent = ActivityTaskManagerService.this.getHomeIntent();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return homeIntent;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean startHomeActivity(int userId, String reason) {
            boolean startHomeOnDisplay;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startHomeOnDisplay = ActivityTaskManagerService.this.mRootActivityContainer.startHomeOnDisplay(userId, reason, 0);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startHomeOnDisplay;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean startHomeOnDisplay(int userId, String reason, int displayId, boolean allowInstrumenting, boolean fromHomeKey) {
            boolean startHomeOnDisplay;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mHwATMSEx.setFreeformStackVisibility(displayId, null, false);
                    startHomeOnDisplay = ActivityTaskManagerService.this.mRootActivityContainer.startHomeOnDisplay(userId, reason, displayId, allowInstrumenting, fromHomeKey);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startHomeOnDisplay;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean startHomeOnAllDisplays(int userId, String reason) {
            boolean startHomeOnAllDisplays;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startHomeOnAllDisplays = ActivityTaskManagerService.this.mRootActivityContainer.startHomeOnAllDisplays(userId, reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startHomeOnAllDisplays;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isFactoryTestProcess(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                boolean z = false;
                if (ActivityTaskManagerService.this.mFactoryTest == 0) {
                    return false;
                }
                if (ActivityTaskManagerService.this.mFactoryTest == 1 && ActivityTaskManagerService.this.mTopComponent != null && wpc.mName.equals(ActivityTaskManagerService.this.mTopComponent.getPackageName())) {
                    return true;
                }
                if (ActivityTaskManagerService.this.mFactoryTest == 2 && (wpc.mInfo.flags & 16) != 0) {
                    z = true;
                }
                return z;
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void updateTopComponentForFactoryTest() {
            CharSequence errorMsg;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mFactoryTest == 1) {
                        ResolveInfo ri = ActivityTaskManagerService.this.mContext.getPackageManager().resolveActivity(new Intent("android.intent.action.FACTORY_TEST"), 1024);
                        if (ri != null) {
                            ActivityInfo ai = ri.activityInfo;
                            ApplicationInfo app = ai.applicationInfo;
                            if ((1 & app.flags) != 0) {
                                ActivityTaskManagerService.this.mTopAction = "android.intent.action.FACTORY_TEST";
                                ActivityTaskManagerService.this.mTopData = null;
                                ActivityTaskManagerService.this.mTopComponent = new ComponentName(app.packageName, ai.name);
                                errorMsg = null;
                            } else {
                                errorMsg = ActivityTaskManagerService.this.mContext.getResources().getText(17040134);
                            }
                        } else {
                            errorMsg = ActivityTaskManagerService.this.mContext.getResources().getText(17040133);
                        }
                        if (errorMsg == null) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        ActivityTaskManagerService.this.mTopAction = null;
                        ActivityTaskManagerService.this.mTopData = null;
                        ActivityTaskManagerService.this.mTopComponent = null;
                        ActivityTaskManagerService.this.mUiHandler.post(new Runnable(errorMsg) {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$LocalService$smesvyl87CxHptMAvRA559Glc1k */
                            private final /* synthetic */ CharSequence f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                ActivityTaskManagerService.LocalService.this.lambda$updateTopComponentForFactoryTest$2$ActivityTaskManagerService$LocalService(this.f$1);
                            }
                        });
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.wm.FactoryErrorDialog, android.app.Dialog] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public /* synthetic */ void lambda$updateTopComponentForFactoryTest$2$ActivityTaskManagerService$LocalService(CharSequence errorMsg) {
            new FactoryErrorDialog(ActivityTaskManagerService.this.mUiContext, errorMsg).show();
            ActivityTaskManagerService.this.mAmInternal.ensureBootCompleted();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void handleAppDied(WindowProcessController wpc, boolean restarting, Runnable finishInstrumentationCallback) {
            TaskRecord diedTask;
            ActivityRecord topResumed;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                if (ActivityTaskManagerService.this.mLastResumedActivity == null || ActivityTaskManagerService.this.mLastResumedActivity.app != wpc) {
                    diedTask = null;
                } else {
                    diedTask = ActivityTaskManagerService.this.mLastResumedActivity.task;
                }
                boolean hasVisibleActivities = ActivityTaskManagerService.this.mRootActivityContainer.handleAppDied(wpc);
                wpc.clearRecentTasks();
                wpc.clearActivities();
                if (wpc.isInstrumenting()) {
                    finishInstrumentationCallback.run();
                }
                if (!restarting && hasVisibleActivities) {
                    ActivityTaskManagerService.this.mWindowManager.deferSurfaceLayout();
                    try {
                        if (!ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities()) {
                            ActivityTaskManagerService.this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                        }
                        if (!(diedTask == null || (topResumed = ActivityTaskManagerService.this.mRootActivityContainer.getTopResumedActivity()) == null || ActivityTaskManagerService.this.mLastResumedActivity == topResumed)) {
                            ActivityTaskManagerService.this.setResumedActivityUncheckLocked(topResumed, "appDied");
                            if (diedTask.inHwFreeFormWindowingMode()) {
                                List<TaskRecord> addTasks = new ArrayList<>(1);
                                addTasks.add(diedTask);
                                ActivityTaskManagerService.this.mHwATMSEx.dispatchFreeformBallLifeState(addTasks, "add");
                            }
                        }
                    } finally {
                        ActivityTaskManagerService.this.mWindowManager.continueSurfaceLayout();
                    }
                }
            }
        }

        /* JADX INFO: finally extract failed */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void closeSystemDialogs(String reason) {
            ActivityTaskManagerService.this.enforceNotIsolatedCaller("closeSystemDialogs");
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (uid >= 10000) {
                            WindowProcessController proc = ActivityTaskManagerService.this.mProcessMap.getProcess(pid);
                            if (!proc.isPerceptible()) {
                                Slog.w("ActivityTaskManager", "Ignoring closeSystemDialogs " + reason + " from background process " + proc);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        }
                        ActivityTaskManagerService.this.mWindowManager.closeSystemDialogs(reason);
                        ActivityTaskManagerService.this.mRootActivityContainer.closeSystemDialogs();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        ActivityTaskManagerService.this.mAmInternal.broadcastCloseSystemDialogs(reason);
                        Binder.restoreCallingIdentity(origId);
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void cleanupDisabledPackageComponents(String packageName, Set<String> disabledClasses, int userId, boolean booted) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mRootActivityContainer.finishDisabledPackageActivities(packageName, disabledClasses, true, false, userId) && booted) {
                        ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        ActivityTaskManagerService.this.mStackSupervisor.scheduleIdleLocked();
                    }
                    ActivityTaskManagerService.this.getRecentTasks().cleanupDisabledPackageTasksLocked(packageName, disabledClasses, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean onForceStopPackage(String packageName, boolean doit, boolean evenPersistent, int userId) {
            boolean didSomething;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    didSomething = ActivityTaskManagerService.this.getActivityStartController().clearPendingActivityLaunches(packageName) | ActivityTaskManagerService.this.mRootActivityContainer.finishDisabledPackageActivities(packageName, null, doit, evenPersistent, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return didSomething;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void resumeTopActivities(boolean scheduleIdle) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    if (scheduleIdle) {
                        ActivityTaskManagerService.this.mStackSupervisor.scheduleIdleLocked();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void preBindApplication(WindowProcessController wpc, Configuration outOverrideConfig) {
            ActivityRecord starting;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mStackSupervisor.getActivityMetricsLogger().notifyBindApplication(wpc.mInfo);
                if (outOverrideConfig != null && HwActivityTaskManager.IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED && (starting = ActivityTaskManagerService.this.mRootActivityContainer.getStartingActivity(wpc)) != null && HwActivityTaskManager.isAdjustConfig(starting.getConfiguration())) {
                    outOverrideConfig.setMultiWindowConfigTo(starting.getConfiguration());
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean attachApplication(WindowProcessController wpc) throws RemoteException {
            boolean attachApplication;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                attachApplication = ActivityTaskManagerService.this.mRootActivityContainer.attachApplication(wpc);
            }
            return attachApplication;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyLockedProfile(int userId, int currentUserId) {
            try {
                if (AppGlobals.getPackageManager().isUidPrivileged(Binder.getCallingUid())) {
                    synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            long ident = Binder.clearCallingIdentity();
                            try {
                                if (ActivityTaskManagerService.this.mAmInternal.shouldConfirmCredentials(userId)) {
                                    if (ActivityTaskManagerService.this.mKeyguardController.isKeyguardLocked()) {
                                        startHomeActivity(currentUserId, "notifyLockedProfile");
                                    }
                                    ActivityTaskManagerService.this.mRootActivityContainer.lockAllProfileTasks(userId);
                                }
                            } finally {
                                Binder.restoreCallingIdentity(ident);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                }
                throw new SecurityException("Only privileged app can call notifyLockedProfile");
            } catch (RemoteException ex) {
                throw new SecurityException("Fail to check is caller a privileged app", ex);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void startConfirmDeviceCredentialIntent(Intent intent, Bundle options) {
            ActivityTaskManagerService.this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startConfirmDeviceCredentialIntent");
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    long ident = Binder.clearCallingIdentity();
                    try {
                        intent.addFlags(276824064);
                        ActivityTaskManagerService.this.mContext.startActivityAsUser(intent, (options != null ? new ActivityOptions(options) : ActivityOptions.makeBasic()).toBundle(), UserHandle.CURRENT);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void writeActivitiesToProto(ProtoOutputStream proto) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.writeToProto(proto, 1146756268033L, 0);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void saveANRState(String reason) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                    pw.println("  ANR time: " + DateFormat.getDateTimeInstance().format(new Date()));
                    if (reason != null) {
                        pw.println("  Reason: " + reason);
                    }
                    pw.println();
                    ActivityTaskManagerService.this.getActivityStartController().dump(pw, "  ", null);
                    pw.println();
                    pw.println("-------------------------------------------------------------------------------");
                    ActivityTaskManagerService.this.dumpActivitiesLocked(null, pw, null, 0, true, false, null, "");
                    pw.println();
                    pw.close();
                    ActivityTaskManagerService.this.mLastANRState = sw.toString();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearSavedANRState() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mLastANRState = null;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void dump(String cmd, FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage) {
            Throwable th;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!ActivityTaskManagerService.DUMP_ACTIVITIES_CMD.equals(cmd)) {
                        if (!ActivityTaskManagerService.DUMP_ACTIVITIES_SHORT_CMD.equals(cmd)) {
                            if (ActivityTaskManagerService.DUMP_LASTANR_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpLastANRLocked(pw);
                            } else if (ActivityTaskManagerService.DUMP_LASTANR_TRACES_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpLastANRTracesLocked(pw);
                            } else if (ActivityTaskManagerService.DUMP_STARTER_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpActivityStarterLocked(pw, dumpPackage);
                            } else if (ActivityTaskManagerService.DUMP_CONTAINERS_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpActivityContainersLocked(pw);
                            } else {
                                if (!ActivityTaskManagerService.DUMP_RECENTS_CMD.equals(cmd)) {
                                    if (ActivityTaskManagerService.DUMP_RECENTS_SHORT_CMD.equals(cmd)) {
                                    }
                                }
                                if (ActivityTaskManagerService.this.getRecentTasks() != null) {
                                    try {
                                        ActivityTaskManagerService.this.getRecentTasks().dump(pw, dumpAll, dumpPackage);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    ActivityTaskManagerService.this.dumpActivitiesLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x00c4 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x00e3 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0108 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x0191 A[SYNTHETIC, Splitter:B:61:0x0191] */
        /* JADX WARNING: Removed duplicated region for block: B:70:0x0253 A[Catch:{ all -> 0x024e, all -> 0x0357 }] */
        /* JADX WARNING: Removed duplicated region for block: B:73:0x025b A[Catch:{ all -> 0x024e, all -> 0x0357 }] */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x026f A[Catch:{ all -> 0x024e, all -> 0x0357 }] */
        /* JADX WARNING: Removed duplicated region for block: B:96:0x02f0 A[Catch:{ all -> 0x036d }] */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x02f4 A[Catch:{ all -> 0x036d }] */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean dumpForProcesses(FileDescriptor fd, PrintWriter pw, boolean dumpAll, String dumpPackage, int dumpAppId, boolean needSep, boolean testPssMode, int wakefulness) {
            Throwable th;
            boolean needSep2;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mHomeProcess != null) {
                        if (dumpPackage != null) {
                            try {
                                if (ActivityTaskManagerService.this.mHomeProcess.mPkgList.contains(dumpPackage)) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        if (needSep) {
                            pw.println();
                            needSep2 = false;
                        } else {
                            needSep2 = needSep;
                        }
                        try {
                            pw.println("  mHomeProcess: " + ActivityTaskManagerService.this.mHomeProcess);
                            if (ActivityTaskManagerService.this.mPreviousProcess != null && (dumpPackage == null || ActivityTaskManagerService.this.mPreviousProcess.mPkgList.contains(dumpPackage))) {
                                if (needSep2) {
                                    pw.println();
                                    needSep2 = false;
                                }
                                pw.println("  mPreviousProcess: " + ActivityTaskManagerService.this.mPreviousProcess);
                            }
                            if (dumpAll && (ActivityTaskManagerService.this.mPreviousProcess == null || dumpPackage == null || ActivityTaskManagerService.this.mPreviousProcess.mPkgList.contains(dumpPackage))) {
                                StringBuilder sb = new StringBuilder(128);
                                sb.append("  mPreviousProcessVisibleTime: ");
                                TimeUtils.formatDuration(ActivityTaskManagerService.this.mPreviousProcessVisibleTime, sb);
                                pw.println(sb);
                            }
                            if (ActivityTaskManagerService.this.mHeavyWeightProcess != null && (dumpPackage == null || ActivityTaskManagerService.this.mHeavyWeightProcess.mPkgList.contains(dumpPackage))) {
                                if (needSep2) {
                                    pw.println();
                                    needSep2 = false;
                                }
                                pw.println("  mHeavyWeightProcess: " + ActivityTaskManagerService.this.mHeavyWeightProcess);
                            }
                            if (dumpPackage == null) {
                                pw.println("  mGlobalConfiguration: " + ActivityTaskManagerService.this.getGlobalConfiguration());
                                ActivityTaskManagerService.this.mRootActivityContainer.dumpDisplayConfigs(pw, "  ");
                            }
                            if (dumpAll) {
                                if (dumpPackage == null) {
                                    pw.println("  mConfigWillChange: " + ActivityTaskManagerService.this.getTopDisplayFocusedStack().mConfigWillChange);
                                }
                                if (ActivityTaskManagerService.this.mCompatModePackages.getPackages().size() > 0) {
                                    boolean printed = false;
                                    for (Map.Entry<String, Integer> entry : ActivityTaskManagerService.this.mCompatModePackages.getPackages().entrySet()) {
                                        String pkg = entry.getKey();
                                        int mode = entry.getValue().intValue();
                                        if (dumpPackage == null || dumpPackage.equals(pkg)) {
                                            if (!printed) {
                                                pw.println("  mScreenCompatPackages:");
                                                printed = true;
                                            }
                                            pw.println("    " + pkg + ": " + mode);
                                        }
                                    }
                                }
                            }
                            if (dumpPackage != null) {
                                try {
                                    pw.println("  mWakefulness=" + PowerManagerInternal.wakefulnessToString(wakefulness));
                                    pw.println("  mSleepTokens=" + ActivityTaskManagerService.this.mRootActivityContainer.mSleepTokens);
                                    if (ActivityTaskManagerService.this.mRunningVoice != null) {
                                        pw.println("  mRunningVoice=" + ActivityTaskManagerService.this.mRunningVoice);
                                        pw.println("  mVoiceWakeLock" + ActivityTaskManagerService.this.mVoiceWakeLock);
                                    }
                                    pw.println("  mSleeping=" + ActivityTaskManagerService.this.mSleeping);
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("  mShuttingDown=");
                                    sb2.append(ActivityTaskManagerService.this.mShuttingDown);
                                    sb2.append(" mTestPssMode=");
                                    sb2.append(testPssMode);
                                    pw.println(sb2.toString());
                                    pw.println("  mVrController=" + ActivityTaskManagerService.this.mVrController);
                                } catch (Throwable th3) {
                                    th = th3;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            if (ActivityTaskManagerService.this.mCurAppTimeTracker != null) {
                                ActivityTaskManagerService.this.mCurAppTimeTracker.dumpWithHeader(pw, "  ", true);
                            }
                            if (ActivityTaskManagerService.this.mAllowAppSwitchUids.size() <= 0) {
                                boolean printed2 = false;
                                for (int i = 0; i < ActivityTaskManagerService.this.mAllowAppSwitchUids.size(); i++) {
                                    ArrayMap<String, Integer> types = ActivityTaskManagerService.this.mAllowAppSwitchUids.valueAt(i);
                                    for (int j = 0; j < types.size(); j++) {
                                        if (dumpPackage != null) {
                                            if (UserHandle.getAppId(types.valueAt(j).intValue()) != dumpAppId) {
                                            }
                                        }
                                        if (needSep2) {
                                            try {
                                                pw.println();
                                                needSep2 = false;
                                            } catch (Throwable th4) {
                                                th = th4;
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                        if (!printed2) {
                                            pw.println("  mAllowAppSwitchUids:");
                                            printed2 = true;
                                        }
                                        pw.print("    User ");
                                        pw.print(ActivityTaskManagerService.this.mAllowAppSwitchUids.keyAt(i));
                                        pw.print(": Type ");
                                        pw.print(types.keyAt(j));
                                        pw.print(" = ");
                                        UserHandle.formatUid(pw, types.valueAt(j).intValue());
                                        pw.println();
                                    }
                                }
                            }
                            if (dumpPackage == null) {
                                if (ActivityTaskManagerService.this.mController != null) {
                                    pw.println("  mController=" + ActivityTaskManagerService.this.mController + " mControllerIsAMonkey=" + ActivityTaskManagerService.this.mControllerIsAMonkey);
                                }
                                pw.println("  mGoingToSleepWakeLock=" + ActivityTaskManagerService.this.mStackSupervisor.mGoingToSleepWakeLock);
                                pw.println("  mLaunchingActivityWakeLock=" + ActivityTaskManagerService.this.mStackSupervisor.mLaunchingActivityWakeLock);
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return needSep2;
                        } catch (Throwable th5) {
                            th = th5;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    needSep2 = needSep;
                    if (needSep2) {
                    }
                    pw.println("  mPreviousProcess: " + ActivityTaskManagerService.this.mPreviousProcess);
                    StringBuilder sb3 = new StringBuilder(128);
                    sb3.append("  mPreviousProcessVisibleTime: ");
                    TimeUtils.formatDuration(ActivityTaskManagerService.this.mPreviousProcessVisibleTime, sb3);
                    pw.println(sb3);
                    if (needSep2) {
                    }
                    pw.println("  mHeavyWeightProcess: " + ActivityTaskManagerService.this.mHeavyWeightProcess);
                    if (dumpPackage == null) {
                    }
                    if (dumpAll) {
                    }
                    if (dumpPackage != null) {
                    }
                    if (ActivityTaskManagerService.this.mCurAppTimeTracker != null) {
                    }
                    if (ActivityTaskManagerService.this.mAllowAppSwitchUids.size() <= 0) {
                    }
                    if (dumpPackage == null) {
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return needSep2;
                } catch (Throwable th6) {
                    th = th6;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void writeProcessesToProto(ProtoOutputStream proto, String dumpPackage, int wakeFullness, boolean testPssMode) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (dumpPackage == null) {
                        ActivityTaskManagerService.this.getGlobalConfiguration().writeToProto(proto, 1146756268051L);
                        proto.write(1133871366165L, ActivityTaskManagerService.this.getTopDisplayFocusedStack().mConfigWillChange);
                        ActivityTaskManagerService.this.writeSleepStateToProto(proto, wakeFullness, testPssMode);
                        if (ActivityTaskManagerService.this.mRunningVoice != null) {
                            long vrToken = proto.start(1146756268060L);
                            proto.write(1138166333441L, ActivityTaskManagerService.this.mRunningVoice.toString());
                            ActivityTaskManagerService.this.mVoiceWakeLock.writeToProto(proto, 1146756268034L);
                            proto.end(vrToken);
                        }
                        ActivityTaskManagerService.this.mVrController.writeToProto(proto, 1146756268061L);
                        if (ActivityTaskManagerService.this.mController != null) {
                            long token = proto.start(1146756268069L);
                            proto.write(1146756268069L, ActivityTaskManagerService.this.mController.toString());
                            proto.write(1133871366146L, ActivityTaskManagerService.this.mControllerIsAMonkey);
                            proto.end(token);
                        }
                        ActivityTaskManagerService.this.mStackSupervisor.mGoingToSleepWakeLock.writeToProto(proto, 1146756268079L);
                        ActivityTaskManagerService.this.mStackSupervisor.mLaunchingActivityWakeLock.writeToProto(proto, 1146756268080L);
                    }
                    if (ActivityTaskManagerService.this.mHomeProcess != null && (dumpPackage == null || ActivityTaskManagerService.this.mHomeProcess.mPkgList.contains(dumpPackage))) {
                        ActivityTaskManagerService.this.mHomeProcess.writeToProto(proto, 1146756268047L);
                    }
                    if (ActivityTaskManagerService.this.mPreviousProcess != null && (dumpPackage == null || ActivityTaskManagerService.this.mPreviousProcess.mPkgList.contains(dumpPackage))) {
                        ActivityTaskManagerService.this.mPreviousProcess.writeToProto(proto, 1146756268048L);
                        proto.write(1112396529681L, ActivityTaskManagerService.this.mPreviousProcessVisibleTime);
                    }
                    if (ActivityTaskManagerService.this.mHeavyWeightProcess != null && (dumpPackage == null || ActivityTaskManagerService.this.mHeavyWeightProcess.mPkgList.contains(dumpPackage))) {
                        ActivityTaskManagerService.this.mHeavyWeightProcess.writeToProto(proto, 1146756268050L);
                    }
                    for (Map.Entry<String, Integer> entry : ActivityTaskManagerService.this.mCompatModePackages.getPackages().entrySet()) {
                        String pkg = entry.getKey();
                        int mode = entry.getValue().intValue();
                        if (dumpPackage == null || dumpPackage.equals(pkg)) {
                            long compatToken = proto.start(2246267895830L);
                            proto.write(1138166333441L, pkg);
                            proto.write(1120986464258L, mode);
                            proto.end(compatToken);
                        }
                    }
                    if (ActivityTaskManagerService.this.mCurAppTimeTracker != null) {
                        ActivityTaskManagerService.this.mCurAppTimeTracker.writeToProto(proto, 1146756268063L, true);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean dumpActivity(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll, boolean dumpVisibleStacksOnly, boolean dumpFocusedStackOnly) {
            boolean dumpActivity;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    dumpActivity = ActivityTaskManagerService.this.dumpActivity(fd, pw, name, args, opti, dumpAll, dumpVisibleStacksOnly, dumpFocusedStackOnly);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return dumpActivity;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void dumpForOom(PrintWriter pw) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    pw.println("  mHomeProcess: " + ActivityTaskManagerService.this.mHomeProcess);
                    pw.println("  mPreviousProcess: " + ActivityTaskManagerService.this.mPreviousProcess);
                    if (ActivityTaskManagerService.this.mHeavyWeightProcess != null) {
                        pw.println("  mHeavyWeightProcess: " + ActivityTaskManagerService.this.mHeavyWeightProcess);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean canGcNow() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!isSleeping()) {
                        if (!ActivityTaskManagerService.this.mRootActivityContainer.allResumedActivitiesIdle()) {
                            z = false;
                        }
                    }
                    z = true;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public WindowProcessController getTopApp() {
            WindowProcessController windowProcessController;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityRecord top = ActivityTaskManagerService.this.mRootActivityContainer.getTopResumedActivity();
                windowProcessController = top != null ? top.app : null;
            }
            return windowProcessController;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void rankTaskLayersIfNeeded() {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                if (ActivityTaskManagerService.this.mRootActivityContainer != null) {
                    ActivityTaskManagerService.this.mRootActivityContainer.rankTaskLayersIfNeeded();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void scheduleDestroyAllActivities(String reason) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.scheduleDestroyAllActivities(null, reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void removeUser(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.removeUser(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean switchUser(int userId, UserState userState) {
            boolean switchUser;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    HwFreeFormManager.getInstance(ActivityTaskManagerService.this.mContext).removeFloatListView();
                    switchUser = ActivityTaskManagerService.this.mRootActivityContainer.switchUser(userId, userState);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return switchUser;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onHandleAppCrash(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.handleAppCrash(wpc);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int finishTopCrashedActivities(WindowProcessController crashedApp, String reason) {
            int finishTopCrashedActivities;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    finishTopCrashedActivities = ActivityTaskManagerService.this.mRootActivityContainer.finishTopCrashedActivities(crashedApp, reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return finishTopCrashedActivities;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidActive(int uid, int procState) {
            ActivityTaskManagerService.this.mActiveUids.onUidActive(uid, procState);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidInactive(int uid) {
            ActivityTaskManagerService.this.mActiveUids.onUidInactive(uid);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onActiveUidsCleared() {
            ActivityTaskManagerService.this.mActiveUids.onActiveUidsCleared();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidProcStateChanged(int uid, int procState) {
            ActivityTaskManagerService.this.mActiveUids.onUidProcStateChanged(uid, procState);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidAddedToPendingTempWhitelist(int uid, String tag) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mPendingTempWhitelist.put(uid, tag);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidRemovedFromPendingTempWhitelist(int uid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mPendingTempWhitelist.remove(uid);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean handleAppCrashInActivityController(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace, Runnable killCrashingAppCallback) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mController == null) {
                        return false;
                    }
                    try {
                        if (!ActivityTaskManagerService.this.mController.appCrashed(processName, pid, shortMsg, longMsg, timeMillis, stackTrace)) {
                            killCrashingAppCallback.run();
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        }
                    } catch (RemoteException e) {
                        ActivityTaskManagerService.this.mController = null;
                        Watchdog.getInstance().setActivityController((IActivityController) null);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void removeRecentTasksByPackageName(String packageName, int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.removeTasksByPackageName(packageName, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void cleanupRecentTasksForUser(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.cleanupLocked(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void loadRecentTasksForUser(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.loadUserRecentsLocked(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackagesSuspendedChanged(String[] packages, boolean suspended, int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.onPackagesSuspendedChanged(packages, suspended, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void flushRecentTasks() {
            ActivityTaskManagerService.this.mRecentTasks.flush();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public WindowProcessController getHomeProcess() {
            WindowProcessController windowProcessController;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    windowProcessController = ActivityTaskManagerService.this.mHomeProcess;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return windowProcessController;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public WindowProcessController getPreviousProcess() {
            WindowProcessController windowProcessController;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    windowProcessController = ActivityTaskManagerService.this.mPreviousProcess;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return windowProcessController;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearLockedTasks(String reason) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.getLockTaskController().clearLockedTasks(reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void updateUserConfiguration() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Configuration configuration = new Configuration(ActivityTaskManagerService.this.getGlobalConfiguration());
                    int currentUserId = ActivityTaskManagerService.this.mAmInternal.getCurrentUserId();
                    Settings.System.adjustConfigurationForUser(ActivityTaskManagerService.this.mContext.getContentResolver(), configuration, currentUserId, Settings.System.canWrite(ActivityTaskManagerService.this.mContext));
                    ActivityTaskManagerService.this.updateConfigurationLocked(configuration, null, false, false, currentUserId, false);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean canShowErrorDialogs() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = false;
                    if (ActivityTaskManagerService.this.mShowDialogs && !ActivityTaskManagerService.this.mSleeping && !ActivityTaskManagerService.this.mShuttingDown && !ActivityTaskManagerService.this.mKeyguardController.isKeyguardOrAodShowing(0) && !ActivityTaskManagerService.this.hasUserRestriction("no_system_error_dialogs", ActivityTaskManagerService.this.mAmInternal.getCurrentUserId()) && (!UserManager.isDeviceInDemoMode(ActivityTaskManagerService.this.mContext) || !ActivityTaskManagerService.this.mAmInternal.getCurrentUser().isDemo())) {
                        z = true;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setProfileApp(String profileApp) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProfileApp = profileApp;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setProfileProc(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProfileProc = wpc;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setProfilerInfo(ProfilerInfo profilerInfo) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProfilerInfo = profilerInfo;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityInfo getLastResumedActivity() {
            if (ActivityTaskManagerService.this.mLastResumedActivity == null) {
                return null;
            }
            return ActivityTaskManagerService.this.mLastResumedActivity.info;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void enterCoordinationMode() {
            ActivityTaskManagerService.this.mHwATMSEx.enterCoordinationMode();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean exitCoordinationMode(boolean toTop, boolean changeMode) {
            return ActivityTaskManagerService.this.mHwATMSEx.exitCoordinationMode(toTop, changeMode);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void exitCoordinationMode() {
            ActivityTaskManagerService.this.mHwATMSEx.exitCoordinationMode();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityMetricsLaunchObserverRegistry getLaunchObserverRegistry() {
            ActivityMetricsLaunchObserverRegistry launchObserverRegistry;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    launchObserverRegistry = ActivityTaskManagerService.this.mStackSupervisor.getActivityMetricsLogger().getLaunchObserverRegistry();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return launchObserverRegistry;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityManager.TaskSnapshot getTaskSnapshotNoRestore(int taskId, boolean reducedResolution) {
            return ActivityTaskManagerService.this.getTaskSnapshot(taskId, reducedResolution, false);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void dismissSplitScreenMode(boolean toTop) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.dismissSplitScreenMode(toTop);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isUidForeground(int uid) {
            boolean isUidForeground;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isUidForeground = ActivityTaskManagerService.this.isUidForeground(uid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isUidForeground;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setDeviceOwnerUid(int uid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.setDeviceOwnerUid(uid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setCompanionAppPackages(int userId, Set<String> companionAppPackages) {
            HashSet hashSet = new HashSet();
            for (String pkg : companionAppPackages) {
                int uid = ActivityTaskManagerService.this.getPackageManagerInternalLocked().getPackageUid(pkg, 0, userId);
                if (uid >= 0) {
                    hashSet.add(Integer.valueOf(uid));
                }
            }
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mCompanionAppUidsMap.put(Integer.valueOf(userId), hashSet);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyHoldScreenStateChange(String tag, int lockHash, int ownerUid, int ownerPid, String state) {
            ActivityTaskManagerService.this.mHwATMSEx.notifyHoldScreenStateChange(tag, lockHash, ownerUid, ownerPid, state);
        }

        /* JADX INFO: finally extract failed */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setExpandScreenTurningOn(boolean isExpandScreenTurningOn) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mKeyguardController != null) {
                        ActivityTaskManagerService.this.mKeyguardController.setExpandScreenTurningOn(isExpandScreenTurningOn);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            Slog.i("ActivityTaskManager", "setExpandScreenTurningOn " + isExpandScreenTurningOn);
        }
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public ActivityTaskManagerService getATMS() {
        return this;
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public ActivityRecord getLastResumedActivityRecord() {
        return this.mLastResumedActivity;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.wm.ActivityTaskManagerService$HwInnerActivityTaskManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public class HwInnerActivityTaskManagerService extends IHwActivityTaskManager.Stub {
        ActivityTaskManagerService mATMS;

        HwInnerActivityTaskManagerService(ActivityTaskManagerService atms) {
            this.mATMS = atms;
        }

        public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) {
            ActivityTaskManagerService.this.mHwATMSEx.registerHwActivityNotifier(notifier, reason);
        }

        public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) {
            ActivityTaskManagerService.this.mHwATMSEx.unregisterHwActivityNotifier(notifier);
        }

        public Bundle getTopActivity() {
            return ActivityTaskManagerService.this.mHwATMSEx.getTopActivity();
        }

        public boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) {
            return ActivityTaskManagerService.this.mHwATMSEx.requestContentNode(componentName, bundle, token);
        }

        public boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) {
            return ActivityTaskManagerService.this.mHwATMSEx.requestContentOther(componentName, bundle, token);
        }

        public ActivityInfo getLastResumedActivity() {
            int callingUid = UserHandle.getAppId(Binder.getCallingUid());
            if (callingUid != 1000) {
                int callingPid = Binder.getCallingPid();
                if (!(ActivityTaskManagerService.checkPermission("android.permission.REAL_GET_TASKS", callingPid, callingUid) == 0)) {
                    Slog.d("ActivityTaskManager", "permission denied for, callingPid:" + callingPid + ", callingUid:" + callingUid + ", requires: android.Manifest.permission.REAL_GET_TASKS");
                    return null;
                }
            }
            if (ActivityTaskManagerService.this.mLastResumedActivity == null) {
                return null;
            }
            return ActivityTaskManagerService.this.mLastResumedActivity.info;
        }

        private boolean checkPermissionForHAM() {
            int uid = UserHandle.getAppId(Binder.getCallingUid());
            if (uid == 1000) {
                return true;
            }
            Slog.e("ActivityTaskManager", "Process Permission error! uid:" + uid);
            return false;
        }

        public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) {
            if (!checkPermissionForHAM()) {
                return -1;
            }
            return ActivityTaskManagerService.this.mHwATMSEx.getTopTaskIdInDisplay(displayId, pkgName, invisibleAlso);
        }

        public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) {
            if (!checkPermissionForHAM()) {
                return false;
            }
            return ActivityTaskManagerService.this.mHwATMSEx.isTaskSupportResize(taskId, isFullscreen, isMaximized);
        }

        public Rect getPCTopTaskBounds(int displayId) {
            if (!checkPermissionForHAM()) {
                return null;
            }
            return ActivityTaskManagerService.this.mHwATMSEx.getPCTopTaskBounds(displayId);
        }

        public void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) {
            if (checkPermissionForHAM()) {
                ActivityTaskManagerService.this.mAtmDAProxy.registerAtmDAMonitorCallback(callback);
            }
        }

        public void setWarmColdSwitch(boolean enable) {
            if (checkPermissionForHAM()) {
                ActivityTaskManagerService.this.mWarmColdSwitch = enable;
            }
        }

        public void hwRestoreTask(int taskId, float xPos, float yPos) {
            ActivityTaskManagerService.this.mHwATMSEx.hwRestoreTask(taskId, xPos, yPos);
        }

        public void hwResizeTask(int taskId, Rect bounds) {
            ActivityTaskManagerService.this.mHwATMSEx.hwResizeTask(taskId, bounds);
        }

        public int getWindowState(IBinder token) {
            return ActivityTaskManagerService.this.mHwATMSEx.getWindowState(token);
        }

        public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
            return ActivityTaskManagerService.this.mHwATMSEx.getHwRecentTaskInfo(taskId);
        }

        public void togglePCMode(boolean pcMode, int displayId) {
            ActivityTaskManagerService.this.mHwATMSEx.togglePCMode(pcMode, displayId);
        }

        public void toggleHome() {
            ActivityTaskManagerService.this.mHwATMSEx.toggleHome();
        }

        public void unRegisterHwTaskStackListener(ITaskStackListener listener) {
            ActivityTaskManagerService.this.mHwATMSEx.unRegisterHwTaskStackListener(listener);
        }

        public void registerHwTaskStackListener(ITaskStackListener listener) {
            ActivityTaskManagerService.this.mHwATMSEx.registerHwTaskStackListener(listener);
        }

        public boolean checkTaskId(int taskId) {
            return ActivityTaskManagerService.this.mHwATMSEx.checkTaskId(taskId);
        }

        public void moveTaskBackwards(int taskId) {
            ActivityTaskManagerService.this.mHwATMSEx.moveTaskBackwards(taskId);
        }

        public Bitmap getTaskThumbnailOnPCMode(int taskId) {
            return ActivityTaskManagerService.this.mHwATMSEx.getTaskThumbnailOnPCMode(taskId);
        }

        public boolean isInMultiWindowMode() {
            return ActivityTaskManagerService.this.mHwATMSEx.isInMultiWindowMode();
        }

        public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
            return ActivityTaskManagerService.this.mHwATMSEx.registerThirdPartyCallBack(aCallBackHandler);
        }

        public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
            return ActivityTaskManagerService.this.mHwATMSEx.unregisterThirdPartyCallBack(aCallBackHandler);
        }

        public boolean addGameSpacePackageList(List<String> packageList) {
            return ActivityTaskManagerService.this.mHwATMSEx.addGameSpacePackageList(packageList);
        }

        public boolean delGameSpacePackageList(List<String> packageList) {
            return ActivityTaskManagerService.this.mHwATMSEx.delGameSpacePackageList(packageList);
        }

        public void registerGameObserver(IGameObserver observer) {
            ActivityTaskManagerService.this.mHwATMSEx.registerGameObserver(observer);
        }

        public void unregisterGameObserver(IGameObserver observer) {
            ActivityTaskManagerService.this.mHwATMSEx.unregisterGameObserver(observer);
        }

        public void registerGameObserverEx(IGameObserverEx observer) {
            ActivityTaskManagerService.this.mHwATMSEx.registerGameObserverEx(observer);
        }

        public void unregisterGameObserverEx(IGameObserverEx observer) {
            ActivityTaskManagerService.this.mHwATMSEx.unregisterGameObserverEx(observer);
        }

        public boolean isInGameSpace(String packageName) {
            return ActivityTaskManagerService.this.mHwATMSEx.isInGameSpace(packageName);
        }

        public List<String> getGameList() {
            return ActivityTaskManagerService.this.mHwATMSEx.getGameList();
        }

        public boolean isGameDndOn() {
            return ActivityTaskManagerService.this.mHwATMSEx.isGameDndOn();
        }

        public boolean isGameDndOnEx() {
            return ActivityTaskManagerService.this.mHwATMSEx.isGameDndOnEx();
        }

        public boolean isGameKeyControlOn() {
            return ActivityTaskManagerService.this.mHwATMSEx.isGameKeyControlOn();
        }

        public boolean isGameGestureDisabled() {
            return ActivityTaskManagerService.this.mHwATMSEx.isGameGestureDisabled();
        }

        public float getAspectRatioWithUserSet(String packageName, String aspectName, ActivityInfo info) {
            return ActivityTaskManagerService.this.mHwATMSEx.getAspectRatioWithUserSet(packageName, aspectName, info);
        }

        public boolean isFreeFormVisible() {
            return ActivityTaskManagerService.this.mHwATMSEx.isFreeFormVisible();
        }

        public boolean isTaskVisible(int id) {
            return ActivityTaskManagerService.this.mHwATMSEx.isTaskVisible(id);
        }

        public void updateFreeFormOutLine(int state) {
            ActivityTaskManagerService.this.mHwATMSEx.updateFreeFormOutLine(state);
        }

        public int getCaptionState(IBinder token) {
            return ActivityTaskManagerService.this.mHwATMSEx.getCaptionState(token);
        }

        public int getActivityWindowMode(IBinder token) {
            return ActivityTaskManagerService.this.mHwATMSEx.getActivityWindowMode(token);
        }

        public Bundle getHwMultiWindowState() {
            return ActivityTaskManagerService.this.mHwATMSEx.getHwMultiWindowState();
        }

        public Rect resizeActivityStack(IBinder token, Rect bounds, boolean isAlwaysOnTop) {
            return ActivityTaskManagerService.this.mHwATMSEx.resizeActivityStack(token, bounds, isAlwaysOnTop);
        }

        public int getVirtualDisplayId(String castType) {
            return ActivityTaskManagerService.this.mHwATMSEx.getVirtualDisplayId(castType);
        }

        public boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) {
            return ActivityTaskManagerService.this.mHwATMSEx.moveStacksToDisplay(fromDisplayId, toDisplayId, isOnlyFocus);
        }

        public int getActivityDisplayId(int pid, int uid) {
            int i = 0;
            if (pid == ActivityManagerService.MY_PID) {
                return 0;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                WindowProcessController wpc = ActivityTaskManagerService.this.getProcessController(pid, uid);
                if (wpc != null) {
                    i = wpc.getActivityDisplayId();
                }
                return i;
            } catch (Exception e) {
                Slog.i("ActivityTaskManager", "getActivityDisplayId exception");
                return 0;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void setStackWindowingMode(IBinder token, int windowingMode, Rect bounds) {
            ActivityTaskManagerService.this.mHwATMSEx.setStackWindowingMode(token, windowingMode, bounds);
        }

        public void onCaptionDropAnimationDone(IBinder token) {
            ActivityTaskManagerService.this.mHwATMSEx.onCaptionDropAnimationDone(token);
        }

        public void setForegroundFreeFormNum(int num) {
            ActivityTaskManagerService.this.mHwATMSEx.setForegroundFreeFormNum(num);
        }

        public Map<String, Boolean> getAppUserAwarenessState(int displayId, List<String> packageNames) {
            return ActivityTaskManagerService.this.mHwATMSEx.getAppUserAwarenessState(displayId, packageNames);
        }

        public List<ActivityManager.RunningTaskInfo> getVisibleTasks() {
            return ActivityTaskManagerService.this.mHwATMSEx.getVisibleTasks();
        }

        public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) {
            return ActivityTaskManagerService.this.mHwATMSEx.getTaskSnapshot(taskId, reducedResolution);
        }

        public boolean isSupportsSplitScreenWindowingMode(IBinder activityToken) {
            return ActivityTaskManagerService.this.mHwATMSEx.isSupportsSplitScreenWindowingMode(activityToken);
        }

        public ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder activityToken, boolean isReducedResolution) {
            return ActivityTaskManagerService.this.mHwATMSEx.getActivityTaskSnapshot(activityToken, isReducedResolution);
        }

        public Bitmap getApplicationIcon(IBinder activityToken, boolean isCheckAppLock) {
            return ActivityTaskManagerService.this.mHwATMSEx.getApplicationIcon(activityToken, isCheckAppLock);
        }

        public boolean isSupportDragToSplitScreen(IBinder token, boolean isCheckAppLock) {
            return ActivityTaskManagerService.this.mHwATMSEx.isSupportDragToSplitScreen(token, isCheckAppLock);
        }

        public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) {
            return ActivityTaskManagerService.this.mHwATMSEx.setFreeformStackVisibility(displayId, stackIdArray, isVisible);
        }

        public void dismissSplitScreenToFocusedStack() {
            ActivityTaskManagerService.this.mHwATMSEx.dismissSplitScreenToFocusedStack();
        }

        public boolean enterCoordinationMode(Intent intent) {
            return ActivityTaskManagerService.this.mHwATMSEx.enterCoordinationMode(intent);
        }

        public boolean exitCoordinationMode(boolean toTop) {
            return ActivityTaskManagerService.this.mHwATMSEx.exitCoordinationMode(toTop, true);
        }

        public void handleMultiWindowSwitch(IBinder token, Bundle info) {
            ActivityTaskManagerService.this.mHwATMSEx.handleMultiWindowSwitch(token, info);
        }

        public Bundle getSplitStacksPos(int displayId, int splitRatio) {
            return ActivityTaskManagerService.this.mHwATMSEx.getSplitStacksPos(displayId, splitRatio);
        }

        public void setSplitBarVisibility(boolean isVisibility) {
            ActivityTaskManagerService.this.mHwATMSEx.setSplitBarVisibility(isVisibility);
        }

        public boolean setCustomActivityController(IActivityController controller) {
            return ActivityTaskManagerService.this.mHwATMSEx.setCustomActivityController(controller);
        }

        public boolean isResizableApp(ActivityInfo activityInfo) {
            if (activityInfo == null) {
                return false;
            }
            return ActivityTaskManagerService.this.mHwATMSEx.isResizableApp(activityInfo.packageName, activityInfo.resizeMode);
        }

        public Bundle getHwMultiWindowAppControlLists() {
            return ActivityTaskManagerService.this.mHwATMSEx.getHwMultiWindowAppControlLists();
        }

        public boolean isNeedAdapterCaptionView(String packageName) {
            return ActivityTaskManagerService.this.mHwATMSEx.isNeedAdapterCaptionView(packageName);
        }

        public void saveMultiWindowTipState(String tipKey, int state) {
            ActivityTaskManagerService.this.mHwATMSEx.saveMultiWindowTipState(tipKey, state);
        }

        public boolean isSupportDragForMultiWin(IBinder token) {
            return ActivityTaskManagerService.this.mHwATMSEx.isSupportDragForMultiWin(token);
        }

        public List<String> getVisiblePackages() {
            return ActivityTaskManagerService.this.mHwATMSEx.getVisiblePackages();
        }

        public boolean setMultiWindowDisabled(boolean disabled) {
            return ActivityTaskManagerService.this.mHwATMSEx.setMultiWindowDisabled(disabled);
        }

        public boolean getMultiWindowDisabled() {
            return ActivityTaskManagerService.this.mHwATMSEx.getMultiWindowDisabled();
        }

        public void moveTaskToFrontForMultiDisplay(int taskId) {
            ActivityTaskManagerService.this.mHwATMSEx.moveTaskToFrontForMultiDisplay(taskId);
        }

        public void moveTaskBackwardsForMultiDisplay(int taskId) {
            ActivityTaskManagerService.this.mHwATMSEx.moveTaskBackwardsForMultiDisplay(taskId);
        }

        public void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) {
            ActivityTaskManagerService.this.mHwATMSEx.hwResizeTaskForMultiDisplay(taskId, bounds);
        }

        public void setFocusedTaskForMultiDisplay(int taskId) {
            ActivityTaskManagerService.this.mHwATMSEx.setFocusedTaskForMultiDisplay(taskId);
        }

        public void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) {
            ActivityTaskManagerService.this.mHwATMSEx.setPCFullSize(fullWidth, fullHeight, phoneOrientation);
        }

        public void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) {
            ActivityTaskManagerService.this.mHwATMSEx.setPCVirtualSize(virtualWidth, virtualHeight, phoneOrientation);
        }

        public void setPCMultiCastMode(boolean isPCMultiCastMode) {
            ActivityTaskManagerService.this.mHwATMSEx.setPCMultiCastMode(isPCMultiCastMode);
        }

        public void setCurOrientation(int curOrientation) {
            ActivityTaskManagerService.this.mHwATMSEx.setCurOrientation(curOrientation);
        }

        public int getPCVirtualWidth() {
            return ActivityTaskManagerService.this.mHwATMSEx.getPCVirtualWidth();
        }

        public int getPCVirtualHeight() {
            return ActivityTaskManagerService.this.mHwATMSEx.getPCVirtualHeight();
        }

        public int getPCFullWidth() {
            return ActivityTaskManagerService.this.mHwATMSEx.getPCFullWidth();
        }

        public int getPCFullHeight() {
            return ActivityTaskManagerService.this.mHwATMSEx.getPCFullHeight();
        }

        public void registerMultiDisplayMessenger(Messenger messenger) {
            ActivityTaskManagerService.this.mHwATMSEx.registerMultiDisplayMessenger(messenger);
        }

        public void unregisterMultiDisplayMessenger(Messenger messenger) {
            ActivityTaskManagerService.this.mHwATMSEx.unregisterMultiDisplayMessenger(messenger);
        }

        public void hwTogglePCFloatWindow(int taskId) {
            ActivityTaskManagerService.this.mHwATMSEx.hwTogglePCFloatWindow(taskId);
        }

        public void hwTogglePhoneFullScreen(int taskId) {
            ActivityTaskManagerService.this.mHwATMSEx.hwTogglePhoneFullScreen(taskId);
        }

        public List<Bundle> getTaskList() {
            return ActivityTaskManagerService.this.mHwATMSEx.getTaskList();
        }

        public int getCurTopFullScreenTaskState() {
            return ActivityTaskManagerService.this.mHwATMSEx.getCurTopFullScreenTaskState();
        }

        public int getCurPCWindowAreaNum() {
            return ActivityTaskManagerService.this.mHwATMSEx.getCurPCWindowAreaNum();
        }

        public List<Bundle> getLastRencentTaskList() {
            return ActivityTaskManagerService.this.mHwATMSEx.getLastRencentTaskList();
        }

        public int retrievePCMultiWinConfig(String configXML) {
            return ActivityTaskManagerService.this.mHwATMSEx.retrievePCMultiWinConfig(configXML);
        }

        public void setPcSize(int pcWidth, int pcHeight) {
            ActivityTaskManagerService.this.mHwATMSEx.setPcSize(pcWidth, pcHeight);
        }

        public int getPcWidth() {
            return ActivityTaskManagerService.this.mHwATMSEx.getPcWidth();
        }

        public int getPcHeight() {
            return ActivityTaskManagerService.this.mHwATMSEx.getPcHeight();
        }

        public void setMultiDisplayParamsWithType(int type, Bundle bundle) {
            ActivityTaskManagerService.this.mHwATMSEx.setMultiDisplayParamsWithType(type, bundle);
        }

        public Rect getLocalLayerRectForMultiDisplay() {
            return ActivityTaskManagerService.this.mHwATMSEx.getLocalLayerRectForMultiDisplay();
        }

        public Rect getLocalDisplayRectForMultiDisplay() {
            return ActivityTaskManagerService.this.mHwATMSEx.getLocalDisplayRectForMultiDisplay();
        }

        public Rect getVirtualLayerRectForMultiDisplay() {
            return ActivityTaskManagerService.this.mHwATMSEx.getVirtualLayerRectForMultiDisplay();
        }

        public Rect getVirtualDisplayRectForMultiDisplay() {
            return ActivityTaskManagerService.this.mHwATMSEx.getVirtualDisplayRectForMultiDisplay();
        }

        public Rect adjustScreenShotRectForPCCast(Rect sourceCrop) {
            return ActivityTaskManagerService.this.mHwATMSEx.adjustScreenShotRectForPCCast(sourceCrop);
        }

        public void hwSetRequestedOrientation(int taskId, int orientation) {
            ActivityTaskManagerService.this.mHwATMSEx.hwSetRequestedOrientation(taskId, orientation);
        }

        public Bundle getActivityOptionFromAppProcess(IApplicationThread thread) {
            WindowProcessController wpc = this.mATMS.getProcessController(thread);
            if (wpc != null) {
                return wpc.getActivityOptionFromAppProcess();
            }
            return null;
        }

        public List<ActivityManager.RecentTaskInfo> getFilteredTasks(int userId, int displayId, String packageName, int[] windowingModes, boolean isIgnoreVisible, int maxNum) {
            return ActivityTaskManagerService.this.mHwATMSEx.getFilteredTasks(userId, displayId, packageName, windowingModes, isIgnoreVisible, maxNum);
        }

        public boolean removeTask(int taskId, IBinder token, String packageName, boolean isRemoveFromRecents, String reason) {
            return ActivityTaskManagerService.this.mHwATMSEx.removeTask(taskId, token, packageName, isRemoveFromRecents, reason);
        }

        public void removeTasks(int[] taskIds) {
            ActivityTaskManagerService.this.mHwATMSEx.removeTasks(taskIds);
        }

        public void toggleFreeformWindowingMode(IBinder appToken, String packageName) {
            ActivityTaskManagerService.this.mHwATMSEx.toggleFreeformWindowingMode(appToken, packageName);
        }

        public boolean setStackScale(int taskId, float scale) {
            return ActivityTaskManagerService.this.mHwATMSEx.setStackScale(taskId, scale);
        }

        public boolean setDockCallBackInfo(IHwDockCallBack callBack, int type) {
            return ActivityTaskManagerService.this.mHwATMSEx.setDockCallBackInfo(callBack, type);
        }

        public int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> bOptions, boolean divideSplitScreen, int flag) {
            return ActivityTaskManagerService.this.mHwATMSEx.startActivitiesFromRecents(taskIds, bOptions, divideSplitScreen, flag);
        }

        public boolean isDisplayHoldScreen(int displayId) {
            return ActivityTaskManagerService.this.mHwATMSEx.isDisplayHoldScreen(displayId);
        }

        public boolean isPadCastMaxSizeEnable() {
            return ActivityTaskManagerService.this.mHwATMSEx.isPadCastMaxSizeEnable();
        }

        public boolean isMirrorCast(String castType) {
            return ActivityTaskManagerService.this.mHwATMSEx.isMirrorCast(castType);
        }

        public int getTopFocusedDisplayId() {
            return ActivityTaskManagerService.this.mWindowManager.mRoot.mTopFocusedDisplayId;
        }

        public float getStackScale(int taskId) {
            return ActivityTaskManagerService.this.mHwATMSEx.getStackScale(taskId);
        }

        public boolean isStatusBarPermenantlyShowing() {
            return ActivityTaskManagerService.this.mHwATMSEx.isStatusBarPermenantlyShowing();
        }

        public void updateFloatingBallPos(Rect pos) {
            ActivityTaskManagerService.this.mHwATMSEx.updateFloatingBallPos(pos);
        }

        public void notifyCameraStateForAtms(Bundle options) {
            ActivityTaskManagerService.this.mHwATMSEx.notifyCameraStateForAtms(options);
        }

        public boolean minimizeHwFreeForm(IBinder token, String packageName, boolean nonRoot) {
            return ActivityTaskManagerService.this.mHwATMSEx.minimizeHwFreeForm(token, packageName, nonRoot);
        }

        public void updateFreeFormOutLineForFloating(IBinder token, int state) {
            ActivityTaskManagerService.this.mHwATMSEx.updateFreeFormOutLineForFloating(token, state);
        }

        public boolean isFullScreen(IBinder token) {
            return ActivityTaskManagerService.this.mHwATMSEx.isFullScreen(token);
        }

        public void notifyLauncherAction(String category, Bundle bundle) {
            ActivityTaskManagerService.this.mHwATMSEx.notifyLauncherAction(category, bundle);
        }

        public List<String> getVisibleCanShowWhenLockedPackages(int displayId) {
            return ActivityTaskManagerService.this.mHwATMSEx.getVisibleCanShowWhenLockedPackages(displayId);
        }

        public Bundle getFreeformBoundsInCenter(int displayId, int centerX) {
            return ActivityTaskManagerService.this.mHwATMSEx.getFreeformBoundsInCenter(displayId, centerX);
        }

        public void notifyNotificationAnimationFinish(int displayId) {
            ActivityTaskManagerService.this.mHwATMSEx.notifyNotificationAnimationFinish(displayId);
        }
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public ActivityStackSupervisor getStackSupervisor() {
        return this.mStackSupervisor;
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public RootActivityContainer getRootActivityContainer() {
        return this.mRootActivityContainer;
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public boolean getSystemReady() {
        return this.mAmInternal.isSystemReady();
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public HwAtmDAMonitorProxy getAtmDAMonitor() {
        return this.mAtmDAProxy;
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public WindowProcessController getProcessControllerForHwAtmsEx(String processName, int uid) {
        return getProcessController(processName, uid);
    }

    @Override // com.android.server.wm.IHwActivityTaskManagerInner
    public ArrayList<TaskRecord> getRecentRawTasks() {
        return this.mRecentTasks.getRawTasks();
    }

    static boolean isTimerAlertActivity(ActivityRecord r) {
        return "com.android.deskclock/.timer.TimerAlertActivity".equals(r.shortComponentName) || "com.huawei.deskclock/.timer.TimerAlertActivity".equals(r.shortComponentName);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mHwATMSEx.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    public void onCoordinationModeDismissed() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                getRootActivityContainer().getDefaultDisplay().onCoordinationModeDismissed();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityState(ActivityRecord r, ActivityStack.ActivityState state) {
        this.mHwATMSEx.notifyActivityState(r, state != null ? state.toString() : null);
    }

    public void showUninstallLauncherDialog(String pkgName) {
        this.mHwATMSEx.showUninstallLauncherDialog(pkgName);
    }

    /* access modifiers changed from: package-private */
    public boolean isInFreeformWhiteList(String packageName) {
        if (packageName != null && SplitNotificationUtils.getInstance(this.mContext).getListPkgName(3).contains(packageName)) {
            return true;
        }
        return false;
    }

    public void applyUpdateSplitBarPos(int position, int displayId) {
        this.mHwATMSEx.updateSplitBarPosForIm(position, displayId);
    }

    public boolean isNeedSkipForceStopForHwMultiWindow(String packageName, int userId, String reason) {
        return this.mHwATMSEx.isNeedSkipForceStopForHwMultiWindow(packageName, userId, reason);
    }
}
