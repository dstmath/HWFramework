package com.android.server.wm;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.animation.AnimationHandler;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IAssistDataReceiver;
import android.app.admin.DevicePolicyCache;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.configstore.V1_0.ISurfaceFlingerConfigs;
import android.hardware.configstore.V1_0.OptionalBool;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManager;
import android.iawareperf.UniPerf;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.util.proto.ProtoOutputStream;
import android.view.AppTransitionAnimationSpec;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDockedStackListener;
import android.view.IHwRotateObserver;
import android.view.IInputFilter;
import android.view.IOnKeyguardExitResult;
import android.view.IPinnedStackListener;
import android.view.IRecentsAnimationRunner;
import android.view.IRotationWatcher;
import android.view.IWallpaperVisibilityListener;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.RemoteAnimationAdapter;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.inputmethod.InputMethodManagerInternal;
import android.vrsystem.IVRSystemServiceManager;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.LatencyTracker;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.WindowManagerPolicyThread;
import com.android.server.AnimationThread;
import com.android.server.DisplayThread;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.NsdService;
import com.android.server.UiModeManagerService;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.input.InputManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.ShutdownThread;
import com.android.server.usage.AppStandbyController;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.utils.PriorityDump;
import com.android.server.wm.RecentsAnimationController;
import com.android.server.wm.SurfaceAnimator;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowState;
import com.android.server.zrhung.IZRHungService;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.android.view.IHwWMDAMonitorCallback;
import com.huawei.android.view.IHwWindowManager;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WindowManagerService extends AbsWindowManagerService implements IHwWindowManagerInner, Watchdog.Monitor, WindowManagerPolicy.WindowManagerFuncs {
    private static final long ALLWINDOWDRAW_MAX_TIMEOUT_TIME = 1000;
    private static final boolean ALWAYS_KEEP_CURRENT = true;
    private static final int ANIMATION_DURATION_SCALE = 2;
    static final int APP_ANIMATION_DURATION = 300;
    private static final int BOOT_ANIMATION_POLL_INTERVAL = 200;
    private static final String BOOT_ANIMATION_SERVICE = "bootanim";
    public static final int COMPAT_MODE_DISABLED = 0;
    public static final int COMPAT_MODE_ENABLED = 1;
    public static final int COMPAT_MODE_MATCH_PARENT = -3;
    static final boolean CUSTOM_SCREEN_ROTATION = true;
    public static final String DEBUG_ALL_OFF_CMD = "hwDebugWmsAllOff";
    public static final String DEBUG_ALL_ON_CMD = "hwDebugWmsAllOn";
    public static final String DEBUG_APP_TRANSITIONS_CMD = "hwDebugWmsTransition";
    public static final String DEBUG_CONFIGURATION_CMD = "hwDebugWmsConfiguration";
    public static final String DEBUG_DISPLAY_CMD = "hwDebugWmsDisplay";
    public static final String DEBUG_FOCUS_CMD = "hwDebugWmsFocus";
    public static final String DEBUG_INPUT_CMD = "hwDebugWmsInput";
    public static final String DEBUG_LAYERS_CMD = "hwDebugWmsLayer";
    public static final String DEBUG_LAYOUT_CMD = "hwDebugWmsLayout";
    public static final String DEBUG_ORIENTATION_CMD = "hwDebugWmsOrientation";
    public static final String DEBUG_PREFIX = "hwDebugWms";
    public static final String DEBUG_SCREEN_ON_CMD = "hwDebugWmsScreen";
    public static final String DEBUG_STARTING_WINDOW_CMD = "hwDebugWmsStartingWindow";
    public static final String DEBUG_VISIBILITY_CMD = "hwDebugWmsVisibility";
    public static final String DEBUG_WALLPAPER_CMD = "hwDebugWmsWallpaper";
    static final long DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS = 5000000000L;
    private static final String DENSITY_OVERRIDE = "ro.config.density_override";
    static final String[] DISABLE_HW_LAUNCHER_EXIT_ANIM_CMP_LIST = {PERMISSION_DIALOG_CMP, STK_DIALOG_CMP};
    static final String DRAWER_LAUNCHER_CMP = "com.huawei.android.launcher/.drawer.DrawerLauncher";
    static final boolean HWFLOW = true;
    static final boolean HW_SUPPORT_LAUNCHER_EXIT_ANIM = (!SystemProperties.getBoolean("ro.config.disable_launcher_exit_anim", false));
    private static final int INPUT_DEVICES_READY_FOR_SAFE_MODE_DETECTION_TIMEOUT_MILLIS = 1000;
    static final boolean IS_DEBUG_VERSION = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    static final int LAYER_OFFSET_DIM = 1;
    static final int LAYER_OFFSET_THUMBNAIL = 4;
    static final int LAYOUT_REPEAT_THRESHOLD = 4;
    public static final float LAZY_MODE_SCALE = 0.75f;
    static final int MAX_ANIMATION_DURATION = 10000;
    private static final int MAX_SCREENSHOT_RETRIES = 3;
    static final String NEW_SIMPLE_LAUNCHER_CMP = "com.huawei.android.launcher/.newsimpleui.NewSimpleLauncher";
    static final String PERMISSION_DIALOG_CMP = "com.android.packageinstaller/.permission.ui.GrantPermissionsActivity";
    static final boolean PROFILE_ORIENTATION = false;
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    static final boolean SCREENSHOT_FORCE_565 = true;
    static final int SEAMLESS_ROTATION_TIMEOUT_DURATION = 2000;
    private static final String SIZE_OVERRIDE = "ro.config.size_override";
    static final String STK_DIALOG_CMP = "com.android.stk/.StkDialogActivity";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    private static final String TAG = "WindowManager";
    private static final int TRANSITION_ANIMATION_SCALE = 1;
    static final int TYPE_LAYER_MULTIPLIER = 10000;
    static final int TYPE_LAYER_OFFSET = 1000;
    static final String UNI_LAUNCHER_CMP = "com.huawei.android.launcher/.unihome.UniHomeLauncher";
    static final int UPDATE_FOCUS_NORMAL = 0;
    static final int UPDATE_FOCUS_PLACING_SURFACES = 2;
    static final int UPDATE_FOCUS_WILL_ASSIGN_LAYERS = 1;
    static final int UPDATE_FOCUS_WILL_PLACE_SURFACES = 3;
    static final int WINDOWS_FREEZING_SCREENS_ACTIVE = 1;
    static final int WINDOWS_FREEZING_SCREENS_NONE = 0;
    static final int WINDOWS_FREEZING_SCREENS_TIMEOUT = 2;
    private static final int WINDOW_ANIMATION_SCALE = 0;
    static final int WINDOW_FREEZE_TIMEOUT_DURATION = SystemProperties.getInt("ro.config.win_freeze_timeout_duration", IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
    static final int WINDOW_LAYER_MULTIPLIER = 5;
    static final int WINDOW_REPLACEMENT_TIMEOUT_DURATION = 2000;
    static final boolean localLOGV = false;
    public static boolean mSupporInputMethodFilletAdaptation = SystemProperties.getBoolean("ro.config.support_inputmethod_fillet_adaptation", false);
    private static WindowManagerService sInstance;
    private static boolean sIsMygote = (System.getenv("MAPLE_RUNTIME") != null);
    static WindowManagerThreadPriorityBooster sThreadPriorityBooster = new WindowManagerThreadPriorityBooster();
    AccessibilityController mAccessibilityController;
    final IActivityManager mActivityManager;
    final WindowManagerInternal.AppTransitionListener mActivityManagerAppTransitionNotifier = new WindowManagerInternal.AppTransitionListener() {
        public void onAppTransitionCancelledLocked(int transit) {
            WindowManagerService.this.mH.sendEmptyMessage(48);
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            WindowManagerService.this.mH.sendEmptyMessage(49);
            AppWindowToken atoken = WindowManagerService.this.mRoot.getAppWindowToken(token);
            if (atoken != null) {
                if (atoken.mLaunchTaskBehind) {
                    try {
                        WindowManagerService.this.mActivityManager.notifyLaunchTaskBehindComplete(atoken.token);
                    } catch (RemoteException e) {
                    }
                    atoken.mLaunchTaskBehind = false;
                } else {
                    atoken.updateReportedVisibilityLocked();
                    if (atoken.mEnteringAnimation && (WindowManagerService.this.getRecentsAnimationController() == null || !WindowManagerService.this.getRecentsAnimationController().isTargetApp(atoken))) {
                        atoken.mEnteringAnimation = false;
                        try {
                            WindowManagerService.this.mActivityManager.notifyEnterAnimationComplete(atoken.token);
                        } catch (RemoteException e2) {
                        }
                    }
                }
            }
        }
    };
    final boolean mAllowAnimationsInLowPowerMode;
    final boolean mAllowBootMessages;
    boolean mAllowTheaterModeWakeFromLayout;
    final ActivityManagerInternal mAmInternal;
    final Map<SurfaceAnimator.Animatable, AnimationAdapter> mAnimationFinishMap = new ArrayMap();
    final Handler mAnimationHandler = new Handler(AnimationThread.getHandler().getLooper());
    final ArrayMap<AnimationAdapter, SurfaceAnimator> mAnimationTransferMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public boolean mAnimationsDisabled = false;
    final WindowAnimator mAnimator;
    /* access modifiers changed from: private */
    public float mAnimatorDurationScaleSetting = 1.0f;
    final ArrayList<AppFreezeListener> mAppFreezeListeners = new ArrayList<>();
    final AppOpsManager mAppOps;
    public String mAppTransitTrack = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    final AppTransition mAppTransition;
    int mAppsFreezingScreen = 0;
    boolean mBootAnimationStopped = false;
    final BoundsAnimationController mBoundsAnimationController;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((action.hashCode() == 988075300 && action.equals("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED")) ? (char) 0 : 65535) == 0) {
                WindowManagerService.this.mKeyguardDisableHandler.sendEmptyMessage(3);
            }
        }
    };
    CircularDisplayMask mCircularDisplayMask;
    boolean mClientFreezingScreen = false;
    final ArraySet<AppWindowToken> mClosingApps = new ArraySet<>();
    protected final Context mContext;
    WindowState mCurrentFocus = null;
    private int mCurrentFoldDisplayMode;
    int[] mCurrentProfileIds = new int[0];
    int mCurrentUserId;
    final ArrayList<WindowState> mDeferRelayoutWindow = new ArrayList<>();
    int mDeferredRotationPauseCount;
    final ArrayList<WindowState> mDestroyPreservedSurface = new ArrayList<>();
    final ArrayList<WindowState> mDestroySurface = new ArrayList<>();
    boolean mDisableTransitionAnimation = false;
    boolean mDisplayEnabled = false;
    long mDisplayFreezeTime = 0;
    boolean mDisplayFrozen = false;
    final DisplayManager mDisplayManager;
    final DisplayManagerInternal mDisplayManagerInternal;
    boolean mDisplayReady;
    final DisplaySettings mDisplaySettings;
    Rect mDockedStackCreateBounds;
    int mDockedStackCreateMode = 0;
    final DragDropController mDragDropController;
    final long mDrawLockTimeoutMillis;
    EmulatorDisplayOverlay mEmulatorDisplayOverlay;
    private int mEnterAnimId;
    private boolean mEventDispatchingEnabled;
    private int mExitAnimId;
    int mExitFlag = -1;
    Bitmap mExitIconBitmap;
    int mExitIconHeight = 0;
    int mExitIconWidth = 0;
    float mExitPivotX = -1.0f;
    float mExitPivotY = -1.0f;
    private final PointerEventDispatcher mExternalPointerEventDispatcher;
    private HandlerThread mFingerHandlerThread = new HandlerThread("hw_finger_unlock_handler");
    private FingerUnlockHandler mFingerUnlockHandler;
    final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList<>();
    boolean mFocusMayChange;
    AppWindowToken mFocusedApp = null;
    boolean mForceDisplayEnabled = false;
    final ArrayList<WindowState> mForceRemoves = new ArrayList<>();
    boolean mForceResizableTasks = false;
    private int mFrozenDisplayId;
    final H mH = new H();
    boolean mHardKeyboardAvailable;
    WindowManagerInternal.OnHardKeyboardStatusChangeListener mHardKeyboardStatusChangeListener;
    final boolean mHasPermanentDpad;
    private boolean mHasWideColorGamutSupport;
    final boolean mHaveInputMethods;
    private ArrayList<WindowState> mHidingNonSystemOverlayWindows = new ArrayList<>();
    private Session mHoldingScreenOn;
    protected PowerManager.WakeLock mHoldingScreenWakeLock;
    HwInnerWindowManagerService mHwInnerService = new HwInnerWindowManagerService(this);
    IHwWindowManagerServiceEx mHwWMSEx = null;
    boolean mInTouchMode;
    final InputManagerService mInputManager;
    IInputMethodManager mInputMethodManager;
    WindowState mInputMethodTarget = null;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow = null;
    final InputMonitor mInputMonitor = new InputMonitor(this);
    public boolean mIsFullInSubFoldMode;
    public boolean mIsPerfBoost;
    boolean mIsTouchDevice;
    /* access modifiers changed from: private */
    public final KeyguardDisableHandler mKeyguardDisableHandler;
    boolean mKeyguardGoingAway;
    boolean mKeyguardOrAodShowingOnDefaultDisplay;
    String mLastANRState;
    int mLastDispatchedSystemUiVisibility = 0;
    int mLastDisplayFreezeDuration = 0;
    Object mLastFinishedFreezeSource = null;
    WindowState mLastFocus = null;
    int mLastStatusBarVisibility = 0;
    WindowState mLastWakeLockHoldingWindow = null;
    WindowState mLastWakeLockObscuringWindow = null;
    private final LatencyTracker mLatencyTracker;
    final boolean mLimitedAlphaCompositing;
    ArrayList<WindowState> mLosingFocus = new ArrayList<>();
    final int mMaxUiWidth;
    MousePositionTracker mMousePositionTracker = new MousePositionTracker();
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished = new ArrayList();
    protected boolean mNotifyFocusedWindow = false;
    final boolean mOnlyCore;
    final ArraySet<AppWindowToken> mOpeningApps = new ArraySet<>();
    protected PowerManager.WakeLock mPCHoldingScreenWakeLock;
    public IHwPCManager mPCManager = null;
    final ArrayList<WindowState> mPendingRemove = new ArrayList<>();
    WindowState[] mPendingRemoveTmp = new WindowState[20];
    final PackageManagerInternal mPmInternal;
    private final PointerEventDispatcher mPointerEventDispatcher;
    final WindowManagerPolicy mPolicy;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    private final PriorityDump.PriorityDumper mPriorityDumper = new PriorityDump.PriorityDumper() {
        public void dumpCritical(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
            WindowManagerService.this.doDump(fd, pw, new String[]{"-a"}, asProto);
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args, boolean asProto) {
            WindowManagerService.this.doDump(fd, pw, args, asProto);
        }
    };
    /* access modifiers changed from: private */
    public RecentsAnimationController mRecentsAnimationController;
    final ArrayList<WindowState> mResizingWindows = new ArrayList<>();
    RootWindowContainer mRoot;
    private boolean mRotatingSeamlessly = false;
    ArrayList<RotationWatcher> mRotationWatchers = new ArrayList<>();
    boolean mSafeMode;
    private final PowerManager.WakeLock mScreenFrozenLock;
    final Rect mScreenRect = new Rect();
    private int mSeamlessRotationCount = 0;
    final ArraySet<Session> mSessions = new ArraySet<>();
    SettingsObserver mSettingsObserver;
    public boolean mShouldResetTime = false;
    public boolean mShouldShowWallpaper = false;
    boolean mShowAlertWindowNotifications = true;
    boolean mShowingBootMessages = false;
    boolean mSkipAppTransitionAnimation = false;
    StrictModeFlash mStrictModeFlash;
    public float mSubFoldModeScale = 0.79577464f;
    boolean mSupportsPictureInPicture = false;
    final SurfaceAnimationRunner mSurfaceAnimationRunner;
    SurfaceBuilderFactory mSurfaceBuilderFactory = $$Lambda$WindowManagerService$XZU3HlCFtHp_gydNmNMeRmQMCI.INSTANCE;
    boolean mSwitchingUser = false;
    boolean mSystemBooted = false;
    int mSystemDecorLayer = 0;
    final TaskPositioningController mTaskPositioningController;
    final TaskSnapshotController mTaskSnapshotController;
    final Configuration mTempConfiguration = new Configuration();
    private WindowContentFrameStats mTempWindowRenderStats;
    final float[] mTmpFloats = new float[9];
    final Rect mTmpRect = new Rect();
    final Rect mTmpRect2 = new Rect();
    final Rect mTmpRect3 = new Rect();
    final RectF mTmpRectF = new RectF();
    final Matrix mTmpTransform = new Matrix();
    private final SurfaceControl.Transaction mTransaction = this.mTransactionFactory.make();
    TransactionFactory mTransactionFactory = $$Lambda$WindowManagerService$hBnABSAsqXWvQ0zKwHWE4BZ3Mc0.INSTANCE;
    int mTransactionSequence;
    /* access modifiers changed from: private */
    public float mTransitionAnimationScaleSetting = 1.0f;
    final UnknownAppVisibilityController mUnknownAppVisibilityController = new UnknownAppVisibilityController(this);
    private ViewServer mViewServer;
    int mVr2dDisplayId = -1;
    private IVRSystemServiceManager mVrMananger;
    HwWMDAMonitorProxy mWMProxy = new HwWMDAMonitorProxy();
    /* access modifiers changed from: private */
    public long mWaitAllWindowDrawStartTime = 0;
    boolean mWaitingForConfig = false;
    ArrayList<WindowState> mWaitingForDrawn = new ArrayList<>();
    Runnable mWaitingForDrawnCallback;
    final WallpaperVisibilityListeners mWallpaperVisibilityListeners = new WallpaperVisibilityListeners();
    Watermark mWatermark;
    private final ArrayList<WindowState> mWinAddedSinceNullFocus = new ArrayList<>();
    private final ArrayList<WindowState> mWinRemovedSinceNullFocus = new ArrayList<>();
    /* access modifiers changed from: private */
    public float mWindowAnimationScaleSetting = 1.0f;
    final ArrayList<WindowChangeListener> mWindowChangeListeners = new ArrayList<>();
    final WindowHashMap mWindowMap = new WindowHashMap();
    final WindowSurfacePlacer mWindowPlacerLocked;
    final ArrayList<AppWindowToken> mWindowReplacementTimeouts = new ArrayList<>();
    final WindowTracing mWindowTracing;
    boolean mWindowsChanged = false;
    int mWindowsFreezingScreen = 0;

    interface AppFreezeListener {
        void onAppFreezeTimeout();
    }

    private class FingerUnlockHandler extends Handler {
        public FingerUnlockHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Runnable callback;
            super.handleMessage(msg);
            if (msg.what == 33) {
                if (WindowManagerService.this.isPrintAllWindowsDrawnLogs()) {
                    Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "ALL_WINDOWS_DRAWN timeout");
                }
                Flog.i(307, "ALL_WINDOWS_DRAWN start callback!");
                synchronized (WindowManagerService.this.mWindowMap) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                    } catch (Throwable th) {
                        while (true) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                if (callback != null) {
                    callback.run();
                }
            }
        }
    }

    public final class H extends Handler {
        public static final int ALL_WINDOWS_DRAWN = 33;
        public static final int ANIMATION_FAILSAFE = 60;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int APP_TRANSITION_GETSPECSFUTURE_TIMEOUT = 102;
        public static final int APP_TRANSITION_TIMEOUT = 13;
        public static final int BOOT_TIMEOUT = 23;
        public static final int CHECK_IF_BOOT_ANIMATION_FINISHED = 37;
        public static final int CLIENT_FREEZE_TIMEOUT = 30;
        public static final int DO_ANIMATION_CALLBACK = 26;
        public static final int ENABLE_SCREEN = 16;
        public static final int FORCE_GC = 15;
        public static final int KEYGUARD_DISMISS_DONE = 101;
        public static final int NEW_ANIMATOR_SCALE = 34;
        public static final int NOTIFY_ACTIVITY_DRAWN = 32;
        public static final int NOTIFY_APP_TRANSITION_CANCELLED = 48;
        public static final int NOTIFY_APP_TRANSITION_FINISHED = 49;
        public static final int NOTIFY_APP_TRANSITION_STARTING = 47;
        public static final int NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED = 53;
        public static final int NOTIFY_KEYGUARD_FLAGS_CHANGED = 56;
        public static final int NOTIFY_KEYGUARD_TRUSTED_CHANGED = 57;
        public static final int PC_FREEZE_TIMEOUT = 103;
        public static final int PERSIST_ANIMATION_SCALE = 14;
        public static final int RECOMPUTE_FOCUS = 61;
        public static final int REPORT_FOCUS_CHANGE = 2;
        public static final int REPORT_HARD_KEYBOARD_STATUS_CHANGE = 22;
        public static final int REPORT_LOSING_FOCUS = 3;
        public static final int REPORT_WINDOWS_CHANGE = 19;
        public static final int RESET_ANR_MESSAGE = 38;
        public static final int RESTORE_POINTER_ICON = 55;
        public static final int SEAMLESS_ROTATION_TIMEOUT = 54;
        public static final int SEND_NEW_CONFIGURATION = 18;
        public static final int SET_FOCUSED_TASK = 104;
        public static final int SET_HAS_OVERLAY_UI = 58;
        public static final int SET_INPUT_ROTATION_TASK = 105;
        public static final int SET_RUNNING_REMOTE_ANIMATION = 59;
        public static final int SHOW_CIRCULAR_DISPLAY_MASK = 35;
        public static final int SHOW_EMULATOR_DISPLAY_OVERLAY = 36;
        public static final int SHOW_STRICT_MODE_VIOLATION = 25;
        public static final int UNUSED = 0;
        public static final int UPDATE_ANIMATION_SCALE = 51;
        public static final int UPDATE_DOCKED_STACK_DIVIDER = 41;
        public static final int WAITING_FOR_DRAWN_TIMEOUT = 24;
        public static final int WAIT_KEYGUARD_DISMISS_DONE_TIMEOUT = 100;
        public static final int WALLPAPER_DRAW_PENDING_TIMEOUT = 39;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int WINDOW_HIDE_TIMEOUT = 52;
        public static final int WINDOW_REPLACEMENT_TIMEOUT = 46;

        public H() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:289:0x04b1, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
            java.lang.Runtime.getRuntime().gc();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:365:0x067a, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
            r3 = r4;
            r4 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:366:0x067f, code lost:
            if (r0 == null) goto L_0x0684;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:367:0x0681, code lost:
            r0.onWindowFocusChangedNotLocked();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:368:0x0684, code lost:
            if (r4 == null) goto L_0x06ac;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:370:0x0688, code lost:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT == false) goto L_0x06a0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:371:0x068a, code lost:
            android.util.Slog.i(com.android.server.wm.WindowManagerService.TAG, "Gaining focus: " + r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:372:0x06a0, code lost:
            r4.reportFocusChangedSerialized(true, r9.this$0.mInTouchMode);
            com.android.server.wm.WindowManagerService.access$600(r9.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:373:0x06ac, code lost:
            if (r3 == null) goto L_0x0766;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:375:0x06b0, code lost:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT == false) goto L_0x06c8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:376:0x06b2, code lost:
            android.util.Slog.i(com.android.server.wm.WindowManagerService.TAG, "Losing focus: " + r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:377:0x06c8, code lost:
            r3.reportFocusChangedSerialized(false, r9.this$0.mInTouchMode);
         */
        public void handleMessage(Message msg) {
            ArrayList<WindowState> losers;
            Runnable callback;
            Runnable callback2;
            boolean bootAnimationComplete;
            int ortation;
            int i = msg.what;
            if (i != 11) {
                if (i == 30) {
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerService.this.mClientFreezingScreen) {
                                WindowManagerService.this.mClientFreezingScreen = false;
                                WindowManagerService.this.mLastFinishedFreezeSource = "client-timeout";
                                WindowManagerService.this.stopFreezingDisplayLocked();
                            }
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (i != 41) {
                    boolean z = true;
                    switch (i) {
                        case 2:
                            AccessibilityController accessibilityController = null;
                            synchronized (WindowManagerService.this.mWindowMap) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    if (WindowManagerService.this.mAccessibilityController != null && (WindowManagerService.this.getDefaultDisplayContentLocked().getDisplayId() == 0 || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad()))) {
                                        accessibilityController = WindowManagerService.this.mAccessibilityController;
                                    }
                                    WindowState lastFocus = WindowManagerService.this.mLastFocus;
                                    WindowState newFocus = WindowManagerService.this.mCurrentFocus;
                                    if (lastFocus != newFocus) {
                                        WindowManagerService.this.mLastFocus = newFocus;
                                        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                            Slog.i(WindowManagerService.TAG, "Focus moving from " + lastFocus + " to " + newFocus);
                                        }
                                        if (!(newFocus == null || lastFocus == null || newFocus.isDisplayedLw())) {
                                            WindowManagerService.this.mLosingFocus.add(lastFocus);
                                            lastFocus = null;
                                            break;
                                        }
                                    } else {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                } catch (Throwable th2) {
                                    while (true) {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th2;
                                        break;
                                    }
                                }
                            }
                        case 3:
                            synchronized (WindowManagerService.this.mWindowMap) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    losers = WindowManagerService.this.mLosingFocus;
                                    WindowManagerService.this.mLosingFocus = new ArrayList<>();
                                } catch (Throwable th3) {
                                    while (true) {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th3;
                                        break;
                                    }
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            int N = losers.size();
                            for (int i2 = 0; i2 < N; i2++) {
                                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                                    Slog.i(WindowManagerService.TAG, "Losing delayed focus: " + losers.get(i2));
                                }
                                losers.get(i2).reportFocusChangedSerialized(false, WindowManagerService.this.mInTouchMode);
                            }
                            break;
                        default:
                            switch (i) {
                                case 13:
                                    synchronized (WindowManagerService.this.mWindowMap) {
                                        try {
                                            WindowManagerService.boostPriorityForLockedSection();
                                            if (WindowManagerService.this.mAppTransition.isTransitionSet() || !WindowManagerService.this.mOpeningApps.isEmpty() || !WindowManagerService.this.mClosingApps.isEmpty()) {
                                                boolean z2 = WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS;
                                                Slog.w(WindowManagerService.TAG, "*** APP TRANSITION TIMEOUT. isTransitionSet()=" + WindowManagerService.this.mAppTransition.isTransitionSet() + " mOpeningApps.size()=" + WindowManagerService.this.mOpeningApps.size() + " mClosingApps.size()=" + WindowManagerService.this.mClosingApps.size());
                                                WindowManagerService.this.mAppTransition.setTimeout();
                                                int N2 = WindowManagerService.this.mOpeningApps.size();
                                                for (int i3 = 0; i3 < N2; i3++) {
                                                    AppWindowToken appToken = WindowManagerService.this.mOpeningApps.valueAt(i3);
                                                    appToken.mPendingRelaunchCount = 0;
                                                    appToken.mFrozenBounds.clear();
                                                    appToken.mFrozenMergedConfig.clear();
                                                }
                                                WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                                            }
                                        } catch (Throwable th4) {
                                            while (true) {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th4;
                                                break;
                                            }
                                        }
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    break;
                                case 14:
                                    Settings.Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                                    Settings.Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                                    Settings.Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                                    break;
                                case 15:
                                    synchronized (WindowManagerService.this.mWindowMap) {
                                        try {
                                            WindowManagerService.boostPriorityForLockedSection();
                                            if (!WindowManagerService.this.mAnimator.isAnimating()) {
                                                if (!WindowManagerService.this.mAnimator.isAnimationScheduled()) {
                                                    if (!WindowManagerService.this.mDisplayFrozen) {
                                                        break;
                                                    } else {
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                        return;
                                                    }
                                                }
                                            }
                                            sendEmptyMessageDelayed(15, 2000);
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        } catch (Throwable th5) {
                                            while (true) {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th5;
                                                break;
                                            }
                                        }
                                    }
                                case 16:
                                    WindowManagerService.this.performEnableScreen();
                                    break;
                                case 17:
                                    synchronized (WindowManagerService.this.mWindowMap) {
                                        try {
                                            WindowManagerService.boostPriorityForLockedSection();
                                            Slog.w(WindowManagerService.TAG, "App freeze timeout expired.");
                                            WindowManagerService.this.mWindowsFreezingScreen = 2;
                                            int mListenersSize = 0;
                                            int i4 = WindowManagerService.this.mAppFreezeListeners.size() - 1;
                                            while (true) {
                                                int i5 = i4;
                                                if (i5 >= 0) {
                                                    mListenersSize++;
                                                    WindowManagerService.this.mAppFreezeListeners.get(i5).onAppFreezeTimeout();
                                                    i4 = i5 - 1;
                                                } else if (mListenersSize == 0) {
                                                    Slog.e(WindowManagerService.TAG, "mAppFreezeListeners is empty ! so stopFreezingDisplayLocked");
                                                    WindowManagerService.this.stopFreezingDisplayLocked();
                                                }
                                            }
                                        } catch (Throwable th6) {
                                            while (true) {
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th6;
                                                break;
                                            }
                                        }
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    break;
                                case 18:
                                    removeMessages(18, msg.obj);
                                    int displayId = ((Integer) msg.obj).intValue();
                                    if (WindowManagerService.this.mRoot.getDisplayContent(displayId) == null) {
                                        if (WindowManagerDebugConfig.DEBUG_CONFIGURATION) {
                                            Slog.w(WindowManagerService.TAG, "Trying to send configuration to non-existing displayId=" + displayId);
                                        }
                                        Slog.w(WindowManagerService.TAG, "non-existing display ,so reset mWaitingForConfig");
                                        if (WindowManagerService.this.mWaitingForConfig) {
                                            WindowManagerService.this.mWaitingForConfig = false;
                                            break;
                                        }
                                    } else {
                                        WindowManagerService.this.sendNewConfiguration(displayId);
                                        break;
                                    }
                                    break;
                                case REPORT_WINDOWS_CHANGE /*19*/:
                                    if (WindowManagerService.this.mWindowsChanged) {
                                        synchronized (WindowManagerService.this.mWindowMap) {
                                            try {
                                                WindowManagerService.boostPriorityForLockedSection();
                                                WindowManagerService.this.mWindowsChanged = false;
                                            } catch (Throwable th7) {
                                                while (true) {
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw th7;
                                                    break;
                                                }
                                            }
                                        }
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        WindowManagerService.this.notifyWindowsChanged();
                                        break;
                                    }
                                    break;
                                default:
                                    switch (i) {
                                        case REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                                            WindowManagerService.this.notifyHardKeyboardStatusChange();
                                            break;
                                        case BOOT_TIMEOUT /*23*/:
                                            WindowManagerService.this.performBootTimeout();
                                            break;
                                        case 24:
                                            synchronized (WindowManagerService.this.mWindowMap) {
                                                try {
                                                    WindowManagerService.boostPriorityForLockedSection();
                                                    Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "Timeout waiting for drawn: undrawn=" + WindowManagerService.this.mWaitingForDrawn);
                                                    WindowManagerService.this.mWaitingForDrawn.clear();
                                                    callback = WindowManagerService.this.mWaitingForDrawnCallback;
                                                    WindowManagerService.this.mWaitingForDrawnCallback = null;
                                                } catch (Throwable th8) {
                                                    while (true) {
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                        throw th8;
                                                        break;
                                                    }
                                                }
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            if (callback != null) {
                                                callback.run();
                                                break;
                                            }
                                            break;
                                        case SHOW_STRICT_MODE_VIOLATION /*25*/:
                                            WindowManagerService.this.showStrictModeViolation(msg.arg1, msg.arg2);
                                            break;
                                        case DO_ANIMATION_CALLBACK /*26*/:
                                            try {
                                                ((IRemoteCallback) msg.obj).sendResult(null);
                                                break;
                                            } catch (RemoteException e) {
                                                break;
                                            }
                                        default:
                                            switch (i) {
                                                case 32:
                                                    try {
                                                        WindowManagerService.this.mActivityManager.notifyActivityDrawn((IBinder) msg.obj);
                                                        break;
                                                    } catch (RemoteException e2) {
                                                        break;
                                                    }
                                                case 33:
                                                    if (WindowManagerService.this.isPrintAllWindowsDrawnLogs()) {
                                                        Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "ALL_WINDOWS_DRAWN timeout");
                                                    }
                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                        try {
                                                            WindowManagerService.boostPriorityForLockedSection();
                                                            callback2 = WindowManagerService.this.mWaitingForDrawnCallback;
                                                            WindowManagerService.this.mWaitingForDrawnCallback = null;
                                                        } catch (Throwable th9) {
                                                            while (true) {
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th9;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    if (callback2 != null) {
                                                        callback2.run();
                                                        break;
                                                    }
                                                    break;
                                                case 34:
                                                    float scale = WindowManagerService.this.getCurrentAnimatorScale();
                                                    ValueAnimator.setDurationScale(scale);
                                                    Session session = (Session) msg.obj;
                                                    if (session == null) {
                                                        ArrayList arrayList = new ArrayList();
                                                        synchronized (WindowManagerService.this.mWindowMap) {
                                                            try {
                                                                WindowManagerService.boostPriorityForLockedSection();
                                                                for (int i6 = 0; i6 < WindowManagerService.this.mSessions.size(); i6++) {
                                                                    arrayList.add(WindowManagerService.this.mSessions.valueAt(i6).mCallback);
                                                                }
                                                            } catch (Throwable th10) {
                                                                while (true) {
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    throw th10;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                        for (int i7 = 0; i7 < arrayList.size(); i7++) {
                                                            try {
                                                                ((IWindowSessionCallback) arrayList.get(i7)).onAnimatorScaleChanged(scale);
                                                            } catch (RemoteException e3) {
                                                            }
                                                        }
                                                        break;
                                                    } else {
                                                        try {
                                                            session.mCallback.onAnimatorScaleChanged(scale);
                                                            break;
                                                        } catch (RemoteException e4) {
                                                            break;
                                                        }
                                                    }
                                                case 35:
                                                    WindowManagerService windowManagerService = WindowManagerService.this;
                                                    if (msg.arg1 != 1) {
                                                        z = false;
                                                    }
                                                    windowManagerService.showCircularMask(z);
                                                    break;
                                                case 36:
                                                    WindowManagerService.this.showEmulatorDisplayOverlay();
                                                    break;
                                                case 37:
                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                        try {
                                                            WindowManagerService.boostPriorityForLockedSection();
                                                            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                                                                Slog.i(WindowManagerService.TAG, "CHECK_IF_BOOT_ANIMATION_FINISHED:");
                                                            }
                                                            bootAnimationComplete = WindowManagerService.this.checkBootAnimationCompleteLocked();
                                                        } catch (Throwable th11) {
                                                            while (true) {
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th11;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    if (bootAnimationComplete) {
                                                        WindowManagerService.this.performEnableScreen();
                                                        break;
                                                    }
                                                    break;
                                                case 38:
                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                        try {
                                                            WindowManagerService.boostPriorityForLockedSection();
                                                            WindowManagerService.this.mLastANRState = null;
                                                        } catch (Throwable th12) {
                                                            while (true) {
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th12;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    WindowManagerService.this.mAmInternal.clearSavedANRState();
                                                    break;
                                                case 39:
                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                        try {
                                                            WindowManagerService.boostPriorityForLockedSection();
                                                            if (WindowManagerService.this.mRoot.mWallpaperController.processWallpaperDrawPendingTimeout()) {
                                                                WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                                                            }
                                                        } catch (Throwable th13) {
                                                            while (true) {
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th13;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case WINDOW_REPLACEMENT_TIMEOUT /*46*/:
                                                            synchronized (WindowManagerService.this.mWindowMap) {
                                                                try {
                                                                    WindowManagerService.boostPriorityForLockedSection();
                                                                    int i8 = WindowManagerService.this.mWindowReplacementTimeouts.size() - 1;
                                                                    while (true) {
                                                                        int i9 = i8;
                                                                        if (i9 >= 0) {
                                                                            WindowManagerService.this.mWindowReplacementTimeouts.get(i9).onWindowReplacementTimeout();
                                                                            i8 = i9 - 1;
                                                                        } else {
                                                                            WindowManagerService.this.mWindowReplacementTimeouts.clear();
                                                                        }
                                                                    }
                                                                } catch (Throwable th14) {
                                                                    while (true) {
                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                        throw th14;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                            break;
                                                        case 47:
                                                            WindowManagerService.this.mAmInternal.notifyAppTransitionStarting((SparseIntArray) msg.obj, msg.getWhen());
                                                            break;
                                                        case 48:
                                                            WindowManagerService.this.mAmInternal.notifyAppTransitionCancelled();
                                                            break;
                                                        case 49:
                                                            WindowManagerService.this.mAmInternal.notifyAppTransitionFinished();
                                                            break;
                                                        default:
                                                            switch (i) {
                                                                case 51:
                                                                    switch (msg.arg1) {
                                                                        case 0:
                                                                            float unused = WindowManagerService.this.mWindowAnimationScaleSetting = Settings.Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                                                                            break;
                                                                        case 1:
                                                                            float unused2 = WindowManagerService.this.mTransitionAnimationScaleSetting = Settings.Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                                                                            break;
                                                                        case 2:
                                                                            float unused3 = WindowManagerService.this.mAnimatorDurationScaleSetting = Settings.Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                                                                            WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                                                                            break;
                                                                    }
                                                                case 52:
                                                                    WindowState window = (WindowState) msg.obj;
                                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                                        try {
                                                                            WindowManagerService.boostPriorityForLockedSection();
                                                                            window.mAttrs.flags &= -129;
                                                                            window.hidePermanentlyLw();
                                                                            window.setDisplayLayoutNeeded();
                                                                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                                                                        } catch (Throwable th15) {
                                                                            while (true) {
                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                throw th15;
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    break;
                                                                case 53:
                                                                    ActivityManagerInternal activityManagerInternal = WindowManagerService.this.mAmInternal;
                                                                    if (msg.arg1 != 1) {
                                                                        z = false;
                                                                    }
                                                                    activityManagerInternal.notifyDockedStackMinimizedChanged(z);
                                                                    break;
                                                                case 54:
                                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                                        try {
                                                                            WindowManagerService.boostPriorityForLockedSection();
                                                                            WindowManagerService.this.getDefaultDisplayContentLocked().onSeamlessRotationTimeout();
                                                                        } catch (Throwable th16) {
                                                                            while (true) {
                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                throw th16;
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    break;
                                                                case 55:
                                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                                        try {
                                                                            WindowManagerService.boostPriorityForLockedSection();
                                                                            WindowManagerService.this.restorePointerIconLocked((DisplayContent) msg.obj, (float) msg.arg1, (float) msg.arg2);
                                                                        } catch (Throwable th17) {
                                                                            while (true) {
                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                throw th17;
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    break;
                                                                case 56:
                                                                    WindowManagerService.this.mAmInternal.notifyKeyguardFlagsChanged((Runnable) msg.obj);
                                                                    break;
                                                                case NOTIFY_KEYGUARD_TRUSTED_CHANGED /*57*/:
                                                                    WindowManagerService.this.mAmInternal.notifyKeyguardTrustedChanged();
                                                                    break;
                                                                case SET_HAS_OVERLAY_UI /*58*/:
                                                                    ActivityManagerInternal activityManagerInternal2 = WindowManagerService.this.mAmInternal;
                                                                    int i10 = msg.arg1;
                                                                    if (msg.arg2 != 1) {
                                                                        z = false;
                                                                    }
                                                                    activityManagerInternal2.setHasOverlayUi(i10, z);
                                                                    break;
                                                                case SET_RUNNING_REMOTE_ANIMATION /*59*/:
                                                                    ActivityManagerInternal activityManagerInternal3 = WindowManagerService.this.mAmInternal;
                                                                    int i11 = msg.arg1;
                                                                    if (msg.arg2 != 1) {
                                                                        z = false;
                                                                    }
                                                                    activityManagerInternal3.setRunningRemoteAnimation(i11, z);
                                                                    break;
                                                                case 60:
                                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                                        try {
                                                                            WindowManagerService.boostPriorityForLockedSection();
                                                                            if (WindowManagerService.this.mRecentsAnimationController != null) {
                                                                                WindowManagerService.this.mRecentsAnimationController.scheduleFailsafe();
                                                                            }
                                                                        } catch (Throwable th18) {
                                                                            while (true) {
                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                throw th18;
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    break;
                                                                case RECOMPUTE_FOCUS /*61*/:
                                                                    synchronized (WindowManagerService.this.mWindowMap) {
                                                                        try {
                                                                            WindowManagerService.boostPriorityForLockedSection();
                                                                            WindowManagerService.this.updateFocusedWindowLocked(0, true);
                                                                        } catch (Throwable th19) {
                                                                            while (true) {
                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                throw th19;
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    break;
                                                                default:
                                                                    switch (i) {
                                                                        case 102:
                                                                            Flog.w(307, "APP_TRANSITION_ANIMATIONS_SPECSFUTURE timeout");
                                                                            break;
                                                                        case 103:
                                                                            synchronized (WindowManagerService.this.mWindowMap) {
                                                                                try {
                                                                                    WindowManagerService.boostPriorityForLockedSection();
                                                                                    Slog.w(WindowManagerService.TAG, "stopFreezingDisplayLocked  by PC_FREEZE_TIMEOUT");
                                                                                    WindowManagerService.this.stopFreezingDisplayLocked();
                                                                                } catch (Throwable th20) {
                                                                                    while (true) {
                                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                                        throw th20;
                                                                                        break;
                                                                                    }
                                                                                }
                                                                            }
                                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                                            break;
                                                                        case 104:
                                                                            try {
                                                                                WindowManagerService.this.mActivityManager.setFocusedTask(msg.arg1);
                                                                                break;
                                                                            } catch (RemoteException e5) {
                                                                                Log.e(WindowManagerService.TAG, "setFocusedDisplay()");
                                                                                break;
                                                                            }
                                                                        case 105:
                                                                            Slog.v(WindowManagerService.TAG, "Fsm_comm setInputViewOrientation , ortation : " + ortation);
                                                                            WindowManagerService.this.mInputManager.setInputViewOrientation(ortation);
                                                                            break;
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
                } else {
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            DisplayContent displayContent = WindowManagerService.this.getDefaultDisplayContentLocked();
                            displayContent.getDockedDividerController().reevaluateVisibility(false);
                            displayContent.adjustForImeIfNeeded();
                        } catch (IllegalArgumentException e6) {
                            Log.e(WindowManagerService.TAG, "catch IllegalArgumentException ");
                        } catch (Throwable th21) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th21;
                            }
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } else {
                synchronized (WindowManagerService.this.mWindowMap) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        WindowManagerService.this.getDefaultDisplayContentLocked().onWindowFreezeTimeout();
                        if (HwPCUtils.isPcCastModeInServer()) {
                            HwPCUtils.log(WindowManagerService.TAG, "Window Freeze TimeOut try stopFreezingDisplayLocked");
                            WindowManagerService.this.stopFreezingDisplayLocked();
                        } else if (WindowManagerService.this.mDisplayFrozen) {
                            Slog.w(WindowManagerService.TAG, "freeze TimeOut, call stopFreezingDisplayLocked");
                            WindowManagerService.this.stopFreezingDisplayLocked();
                        }
                    } catch (Throwable th22) {
                        while (true) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th22;
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public class HwInnerWindowManagerService extends IHwWindowManager.Stub {
        WindowManagerService mWMS;

        HwInnerWindowManagerService(WindowManagerService wms) {
            this.mWMS = wms;
        }

        public int getRestrictedScreenHeight() {
            return this.mWMS.mPolicy.getRestrictedScreenHeight();
        }

        public boolean isWindowSupportKnuckle() {
            return this.mWMS.mPolicy.isWindowSupportKnuckle();
        }

        public boolean isNavigationBarVisible() {
            return this.mWMS.mPolicy.isNavigationBarVisible();
        }

        public void dismissKeyguardLw() {
            this.mWMS.mPolicy.dismissKeyguardLw(null, null);
        }

        public boolean isFullScreenDevice() {
            if (WindowManagerService.this.mHwWMSEx != null) {
                return WindowManagerService.this.mHwWMSEx.isFullScreenDevice();
            }
            return false;
        }

        public float getDeviceMaxRatio() {
            if (WindowManagerService.this.mHwWMSEx != null) {
                return WindowManagerService.this.mHwWMSEx.getDeviceMaxRatio();
            }
            return 0.0f;
        }

        public Rect getTopAppDisplayBounds(float appMaxRatioint, int rotation) {
            WindowState focusedWindow = WindowManagerService.this.getFocusedWindow();
            if (focusedWindow != null) {
                return focusedWindow.getBounds();
            }
            return null;
        }

        public void registerRotateObserver(IHwRotateObserver observer) {
            WindowManagerService.this.mPolicy.registerRotateObserver(observer);
        }

        public void unregisterRotateObserver(IHwRotateObserver observer) {
            WindowManagerService.this.mPolicy.unregisterRotateObserver(observer);
        }

        public List<String> getNotchSystemApps() {
            if (WindowManagerService.this.mHwWMSEx != null) {
                return WindowManagerService.this.mHwWMSEx.getNotchSystemApps();
            }
            return null;
        }

        public int getAppUseNotchMode(String packageName) {
            if (WindowManagerService.this.mHwWMSEx != null) {
                return WindowManagerService.this.mHwWMSEx.getAppUseNotchMode(packageName);
            }
            return -1;
        }

        private boolean checkPermission() {
            int uid = UserHandle.getAppId(Binder.getCallingUid());
            if (uid == 1000) {
                return true;
            }
            Slog.e(WindowManagerService.TAG, "Process Permission error! uid:" + uid);
            return false;
        }

        public boolean registerWMMonitorCallback(IHwWMDAMonitorCallback callback) {
            if (!checkPermission()) {
                return false;
            }
            WindowManagerService.this.mWMProxy.registerWMMonitorCallback(callback);
            return true;
        }

        public List<Bundle> getVisibleWindows(int ops) {
            if (!checkPermission()) {
                return null;
            }
            return WindowManagerService.this.mHwWMSEx.getVisibleWindows(ops);
        }

        public int getFocusWindowWidth() {
            return WindowManagerService.this.mHwWMSEx.getFocusWindowWidth(WindowManagerService.this.mCurrentFocus, WindowManagerService.this.mInputMethodTarget);
        }

        public void startNotifyWindowFocusChange() {
            if (WindowManagerService.this.checkCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "startNotifyWindowFocusChange()")) {
                this.mWMS.mNotifyFocusedWindow = true;
            }
        }

        public void stopNotifyWindowFocusChange() {
            if (WindowManagerService.this.checkCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "stopNotifyWindowFocusChange()")) {
                this.mWMS.mNotifyFocusedWindow = false;
            }
        }

        public void getCurrFocusedWinInExtDisplay(Bundle outBundle) {
            if (checkPermission()) {
                WindowManagerService.this.mHwWMSEx.getCurrFocusedWinInExtDisplay(outBundle);
            }
        }

        public boolean hasLighterViewInPCCastMode() {
            if (!checkPermission()) {
                return false;
            }
            return WindowManagerService.this.mHwWMSEx.hasLighterViewInPCCastMode();
        }

        public boolean shouldDropMotionEventForTouchPad(float x, float y) {
            if (!checkPermission()) {
                return false;
            }
            return WindowManagerService.this.mHwWMSEx.shouldDropMotionEventForTouchPad(x, y);
        }

        public HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean refresh) {
            return WindowManagerService.this.mHwWMSEx.getForegroundTaskSnapshotWrapper(WindowManagerService.this.mTaskSnapshotController, WindowManagerService.this.getFocusedWindow(), refresh);
        }

        public void setCoverManagerState(boolean isCoverOpen) {
            if (WindowManagerService.this.mHwWMSEx != null) {
                WindowManagerService.this.mHwWMSEx.setCoverManagerState(isCoverOpen);
            }
        }

        public void freezeOrThawRotation(int rotation) {
            this.mWMS.freezeOrThawRotation(rotation);
        }

        public void setGestureNavMode(String packageName, int leftMode, int rightMode, int bottomMode) {
            WindowManagerService.this.mPolicy.setGestureNavMode(packageName, Binder.getCallingUid(), leftMode, rightMode, bottomMode);
        }
    }

    private final class LocalService extends WindowManagerInternal {
        private LocalService() {
        }

        public void requestTraversalFromDisplayManager() {
            WindowManagerService.this.requestTraversal();
        }

        /* JADX INFO: finally extract failed */
        public void setMagnificationSpec(MagnificationSpec spec) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.setMagnificationSpecLocked(spec);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (Binder.getCallingPid() != Process.myPid()) {
                spec.recycle();
            }
        }

        public void setForceShowMagnifiableBounds(boolean show) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.setForceShowMagnifiableBoundsLocked(show);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void getMagnificationRegion(Region magnificationRegion) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.getMagnificationRegionLocked(magnificationRegion);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder windowToken) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(windowToken);
                    if (windowState == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    MagnificationSpec spec = null;
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        spec = WindowManagerService.this.mAccessibilityController.getMagnificationSpecForWindowLocked(windowState);
                    }
                    if ((spec == null || spec.isNop()) && windowState.mGlobalScale == 1.0f) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    MagnificationSpec spec2 = spec == null ? MagnificationSpec.obtain() : MagnificationSpec.obtain(spec);
                    spec2.scale *= windowState.mGlobalScale;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return spec2;
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        }

        public void setMagnificationCallbacks(WindowManagerInternal.MagnificationCallbacks callbacks) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController == null) {
                        WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                    }
                    WindowManagerService.this.mAccessibilityController.setMagnificationCallbacksLocked(callbacks);
                    if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                        WindowManagerService.this.mAccessibilityController = null;
                    }
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public void setWindowsForAccessibilityCallback(WindowManagerInternal.WindowsForAccessibilityCallback callback) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController == null) {
                        WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                    }
                    WindowManagerService.this.mAccessibilityController.setWindowsForAccessibilityCallback(callback);
                    if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                        WindowManagerService.this.mAccessibilityController = null;
                    }
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public void setInputFilter(IInputFilter filter) {
            WindowManagerService.this.mInputManager.setInputFilter(filter);
        }

        public IBinder getFocusedWindowToken() {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = WindowManagerService.this.getFocusedWindowLocked();
                    if (windowState != null) {
                        IBinder asBinder = windowState.mClient.asBinder();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return asBinder;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        }

        public boolean isCoverOpen() {
            return WindowManagerService.this.mHwWMSEx.isCoverOpen();
        }

        public boolean isKeyguardLocked() {
            return WindowManagerService.this.isKeyguardLocked();
        }

        public boolean isKeyguardShowingAndNotOccluded() {
            return WindowManagerService.this.isKeyguardShowingAndNotOccluded();
        }

        public void showGlobalActions() {
            WindowManagerService.this.showGlobalActions();
        }

        public void getWindowFrame(IBinder token, Rect outBounds) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                    if (windowState != null) {
                        outBounds.set(windowState.mFrame);
                    } else {
                        outBounds.setEmpty();
                    }
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public void waitForAllWindowsDrawn(Runnable callback, long timeout) {
            boolean allWindowsDrawn = false;
            long unused = WindowManagerService.this.mWaitAllWindowDrawStartTime = SystemClock.elapsedRealtime();
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mWaitingForDrawnCallback = callback;
                    WindowManagerService.this.getDefaultDisplayContentLocked().waitForAllWindowsDrawn();
                    WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                    WindowManagerService.this.mH.removeMessages(24);
                    if (WindowManagerService.this.mWaitingForDrawn.isEmpty()) {
                        allWindowsDrawn = true;
                    } else {
                        WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                        WindowManagerService.this.checkDrawnWindowsLocked();
                    }
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (allWindowsDrawn) {
                callback.run();
            }
        }

        public void addWindowToken(IBinder token, int type, int displayId) {
            WindowManagerService.this.addWindowToken(token, type, displayId);
        }

        public void removeWindowToken(IBinder binder, boolean removeWindows, int displayId) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (removeWindows) {
                        DisplayContent dc = WindowManagerService.this.mRoot.getDisplayContent(displayId);
                        if (dc == null) {
                            Slog.w(WindowManagerService.TAG, "removeWindowToken: Attempted to remove token: " + binder + " for non-exiting displayId=" + displayId);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        WindowToken token = dc.removeWindowToken(binder);
                        if (token == null) {
                            Slog.w(WindowManagerService.TAG, "removeWindowToken: Attempted to remove non-existing token: " + binder);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        token.removeAllWindowsIfPossible();
                    }
                    WindowManagerService.this.removeWindowToken(binder, displayId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        }

        public void registerAppTransitionListener(WindowManagerInternal.AppTransitionListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mAppTransition.registerListenerLocked(listener);
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public int getInputMethodWindowVisibleHeight() {
            int inputMethodWindowVisibleHeight;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    inputMethodWindowVisibleHeight = WindowManagerService.this.getDefaultDisplayContentLocked().mDisplayFrames.getInputMethodWindowVisibleHeight();
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return inputMethodWindowVisibleHeight;
        }

        public void saveLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mInputMethodWindow != null) {
                        WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(WindowManagerService.this.mInputMethodWindow, WindowManagerService.this.mInputMethodTarget);
                    }
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public void clearLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(null, null);
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public void updateInputMethodWindowStatus(IBinder imeToken, boolean imeWindowVisible, boolean dismissImeOnBackKeyPressed, IBinder targetWindowToken) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.w(WindowManagerService.TAG, "updateInputMethodWindowStatus: imeToken=" + imeToken + " dismissImeOnBackKeyPressed=" + dismissImeOnBackKeyPressed + " imeWindowVisible=" + imeWindowVisible + " targetWindowToken=" + targetWindowToken);
            }
            WindowManagerService.this.mPolicy.setDismissImeOnBackKeyPressed(dismissImeOnBackKeyPressed);
        }

        public boolean isHardKeyboardAvailable() {
            boolean z;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = WindowManagerService.this.mHardKeyboardAvailable;
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return z;
        }

        public void setOnHardKeyboardStatusChangeListener(WindowManagerInternal.OnHardKeyboardStatusChangeListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mHardKeyboardStatusChangeListener = listener;
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public boolean isStackVisible(int windowingMode) {
            boolean isStackVisible;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isStackVisible = WindowManagerService.this.getDefaultDisplayContentLocked().isStackVisible(windowingMode);
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return isStackVisible;
        }

        public boolean isDockedDividerResizing() {
            boolean isResizing;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isResizing = WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().isResizing();
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return isResizing;
        }

        public void computeWindowsForAccessibility() {
            AccessibilityController accessibilityController;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    accessibilityController = WindowManagerService.this.mAccessibilityController;
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (accessibilityController != null) {
                accessibilityController.performComputeChangedWindowsNotLocked();
            }
        }

        public void setVr2dDisplayId(int vr2dDisplayId) {
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.d(WindowManagerService.TAG, "setVr2dDisplayId called for: " + vr2dDisplayId);
            }
            synchronized (WindowManagerService.this) {
                WindowManagerService.this.mVr2dDisplayId = vr2dDisplayId;
            }
        }

        public void registerDragDropControllerCallback(WindowManagerInternal.IDragDropCallback callback) {
            WindowManagerService.this.mDragDropController.registerCallback(callback);
        }

        public void lockNow() {
            WindowManagerService.this.lockNow(null);
        }

        public int getWindowOwnerUserId(IBinder token) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState window = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                    if (window != null) {
                        int userId = UserHandle.getUserId(window.mOwnerUid);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return userId;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return -10000;
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        }

        public void waitForKeyguardDismissDone(Runnable callback, long timeout) {
            Slog.i(WindowManagerService.TAG, "fingerunlock--waitForKeyguardDismissDone there is no keyguard.");
            callback.run();
        }

        public void setDockedStackDividerRotation(int rotation) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().setDockedStackDividerRotation(rotation);
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }

        public int getFocusedDisplayId() {
            return WindowManagerService.this.getFocusedDisplayId();
        }

        public void setFocusedDisplayId(int displayId, String reason) {
            WindowManagerService.this.setFocusedDisplay(displayId, true, reason);
        }

        public boolean isMinimizedDock() {
            boolean isMinimizedDock;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isMinimizedDock = WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().isMinimizedDock();
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return isMinimizedDock;
        }

        public String getFullStackTopWindow() {
            TaskStack stack = null;
            try {
                stack = WindowManagerService.this.mRoot.getStack(4, 1);
            } catch (IndexOutOfBoundsException e) {
                Log.i(WindowManagerService.TAG, "not find secondary stack");
            }
            if (stack == null || stack.getTopChild() == null || ((Task) stack.getTopChild()).getTopFullscreenAppToken() == null) {
                return null;
            }
            return ((Task) stack.getTopChild()).getTopFullscreenAppToken().appComponentName;
        }

        public void setForcedDisplaySize(int displayId, int width, int height) {
            WindowManagerService.this.setForcedDisplaySize(displayId, width, height);
        }

        public boolean isInFoldFullDisplayMode() {
            return WindowManagerService.this.mHwWMSEx.isInFoldFullDisplayMode();
        }
    }

    private static class MousePositionTracker implements WindowManagerPolicyConstants.PointerEventListener {
        /* access modifiers changed from: private */
        public boolean mLatestEventWasMouse;
        /* access modifiers changed from: private */
        public float mLatestMouseX;
        /* access modifiers changed from: private */
        public float mLatestMouseY;

        private MousePositionTracker() {
        }

        /* access modifiers changed from: package-private */
        public void updatePosition(float x, float y) {
            synchronized (this) {
                this.mLatestEventWasMouse = true;
                this.mLatestMouseX = x;
                this.mLatestMouseY = y;
            }
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
                updatePosition(motionEvent.getRawX(), motionEvent.getRawY());
                return;
            }
            synchronized (this) {
                this.mLatestEventWasMouse = false;
            }
        }
    }

    class RotationWatcher {
        final IBinder.DeathRecipient mDeathRecipient;
        final int mDisplayId;
        final IRotationWatcher mWatcher;

        RotationWatcher(IRotationWatcher watcher, IBinder.DeathRecipient deathRecipient, int displayId) {
            this.mWatcher = watcher;
            this.mDeathRecipient = deathRecipient;
            this.mDisplayId = displayId;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mAnimationDurationScaleUri = Settings.Global.getUriFor("animator_duration_scale");
        private final Uri mDisplayInversionEnabledUri = Settings.Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mTransitionAnimationScaleUri = Settings.Global.getUriFor("transition_animation_scale");
        private final Uri mWindowAnimationScaleUri = Settings.Global.getUriFor("window_animation_scale");

        public SettingsObserver() {
            super(new Handler());
            ContentResolver resolver = WindowManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            resolver.registerContentObserver(this.mWindowAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mTransitionAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mAnimationDurationScaleUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            int mode;
            if (uri != null) {
                if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    WindowManagerService.this.updateCircularDisplayMaskIfNeeded();
                } else {
                    if (this.mWindowAnimationScaleUri.equals(uri)) {
                        mode = 0;
                    } else if (this.mTransitionAnimationScaleUri.equals(uri)) {
                        mode = 1;
                    } else if (this.mAnimationDurationScaleUri.equals(uri)) {
                        mode = 2;
                    } else {
                        return;
                    }
                    WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(51, mode, 0));
                }
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateAnimationScaleMode {
    }

    public interface WindowChangeListener {
        void focusChanged();

        void windowsChanged();
    }

    /* renamed from: lambda$XZ-U3HlCFtHp_gydNmNMeRmQMCI  reason: not valid java name */
    public static /* synthetic */ SurfaceControl.Builder m23lambda$XZU3HlCFtHp_gydNmNMeRmQMCI(SurfaceSession surfaceSession) {
        return new SurfaceControl.Builder(surfaceSession);
    }

    public static /* synthetic */ SurfaceControl.Transaction lambda$hBnABSAsqXWvQ0zKwHWE4BZ3Mc0() {
        return new SurfaceControl.Transaction();
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getTopOpeningApp() {
        int topOpeningLayer = 0;
        AppWindowToken topOpeningApp = null;
        int appsCount = this.mOpeningApps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = this.mOpeningApps.valueAt(i);
            int layer = wtoken.getHighestAnimLayer();
            if (topOpeningApp == null || layer > topOpeningLayer) {
                topOpeningApp = wtoken;
                topOpeningLayer = layer;
            }
        }
        return topOpeningApp;
    }

    /* access modifiers changed from: package-private */
    public boolean isSupportHwAppExitAnim(AppWindowToken aToken) {
        if (aToken == null) {
            Slog.w(TAG, "check app support hw app exit animation failed!");
            return false;
        }
        String aTokenStr = aToken.toString();
        if (aTokenStr == null) {
            Slog.w(TAG, "check app support hw app exit animation failed!");
            return false;
        }
        for (String contains : DISABLE_HW_LAUNCHER_EXIT_ANIM_CMP_LIST) {
            if (aTokenStr.contains(contains)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int getDragLayerLocked() {
        return (this.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    static void boostPriorityForLockedSection() {
        sThreadPriorityBooster.boost();
    }

    static void resetPriorityAfterLockedSection() {
        sThreadPriorityBooster.reset();
    }

    /* access modifiers changed from: package-private */
    public void openSurfaceTransaction() {
        try {
            Trace.traceBegin(32, "openSurfaceTransaction");
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                SurfaceControl.openTransaction();
            }
            resetPriorityAfterLockedSection();
            Trace.traceEnd(32);
        } catch (Throwable th) {
            Trace.traceEnd(32);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeSurfaceTransaction(String where) {
        try {
            Trace.traceBegin(32, "closeSurfaceTransaction");
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    traceStateLocked(where);
                    SurfaceControl.closeTransaction();
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            resetPriorityAfterLockedSection();
        } finally {
            Trace.traceEnd(32);
        }
    }

    static WindowManagerService getInstance() {
        return sInstance;
    }

    public static WindowManagerService main(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        WindowManagerService[] windowManagerServiceArr = new WindowManagerService[1];
        Handler handler = DisplayThread.getHandler();
        $$Lambda$WindowManagerService$qOaUiWHWefHk1N5KT4WND2mknQ r2 = new Runnable(context, im, haveInputMethods, showBootMsgs, onlyCore, policy) {
            private final /* synthetic */ Context f$0;
            private final /* synthetic */ InputManagerService f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ boolean f$3;
            private final /* synthetic */ boolean f$4;
            private final /* synthetic */ WindowManagerPolicy f$5;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                WindowManagerService.lambda$main$0(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        };
        handler.runWithScissors(r2, 0);
        return sInstance;
    }

    static /* synthetic */ void lambda$main$0(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        HwServiceFactory.IHwWindowManagerService iwms = HwServiceFactory.getHuaweiWindowManagerService();
        if (iwms != null) {
            sInstance = iwms.getInstance(context, im, haveInputMethods, showBootMsgs, onlyCore, policy);
            return;
        }
        WindowManagerService windowManagerService = new WindowManagerService(context, im, haveInputMethods, showBootMsgs, onlyCore, policy);
        sInstance = windowManagerService;
    }

    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());
                WindowManagerService.this.mPolicy.init(WindowManagerService.this.mContext, WindowManagerService.this, WindowManagerService.this);
            }
        }, 0);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver result) {
        new WindowManagerShellCommand(this).exec(this, in, out, err, args, callback, result);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: com.android.server.wm.WindowManagerService$6} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: android.app.AppOpsManager$OnOpChangedListener} */
    /* JADX WARNING: Multi-variable type inference failed */
    protected WindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        Context context2 = context;
        this.mFingerHandlerThread.start();
        this.mFingerUnlockHandler = new FingerUnlockHandler(this.mFingerHandlerThread.getLooper());
        this.mHwWMSEx = HwServiceExFactory.getHwWindowManagerServiceEx(this, context);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        LockGuard.installLock((Object) this, 5);
        this.mContext = context2;
        this.mHaveInputMethods = haveInputMethods;
        this.mAllowBootMessages = showBootMsgs;
        this.mOnlyCore = onlyCore;
        this.mLimitedAlphaCompositing = context.getResources().getBoolean(17957015);
        this.mHasPermanentDpad = context.getResources().getBoolean(17956981);
        this.mInTouchMode = context.getResources().getBoolean(17956919);
        this.mDrawLockTimeoutMillis = (long) context.getResources().getInteger(17694782);
        this.mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(17956871);
        this.mMaxUiWidth = context.getResources().getInteger(17694812);
        this.mDisableTransitionAnimation = context.getResources().getBoolean(17956930);
        this.mInputManager = inputManager;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mDisplaySettings = new DisplaySettings();
        this.mDisplaySettings.readSettingsLocked();
        this.mPolicy = policy;
        this.mAnimator = new WindowAnimator(this);
        this.mRoot = new RootWindowContainer(this);
        this.mWindowPlacerLocked = new WindowSurfacePlacer(this);
        this.mTaskSnapshotController = new TaskSnapshotController(this);
        this.mWindowTracing = WindowTracing.createDefaultAndStartLooper(context);
        LocalServices.addService(WindowManagerPolicy.class, this.mPolicy);
        if (this.mInputManager != null) {
            InputChannel inputChannel = this.mInputManager.monitorInput(TAG);
            this.mPointerEventDispatcher = inputChannel != null ? new PointerEventDispatcher(inputChannel) : null;
        } else {
            this.mPointerEventDispatcher = null;
        }
        if (this.mInputManager != null) {
            InputChannel inputChannel2 = this.mInputManager.monitorInput("ExternalPCChannel");
            this.mExternalPointerEventDispatcher = inputChannel2 != null ? new PointerEventDispatcher(inputChannel2) : null;
        } else {
            this.mExternalPointerEventDispatcher = null;
        }
        this.mDisplayManager = (DisplayManager) context2.getSystemService("display");
        this.mKeyguardDisableHandler = new KeyguardDisableHandler(this.mContext, this.mPolicy);
        this.mPowerManager = (PowerManager) context2.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        if (this.mPowerManagerInternal != null) {
            this.mPowerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                public int getServiceType() {
                    return 3;
                }

                public void onLowPowerModeChanged(PowerSaveState result) {
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            boolean enabled = result.batterySaverEnabled;
                            if (WindowManagerService.this.mAnimationsDisabled != enabled && !WindowManagerService.this.mAllowAnimationsInLowPowerMode) {
                                boolean unused = WindowManagerService.this.mAnimationsDisabled = enabled;
                                WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            }
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            });
            this.mAnimationsDisabled = this.mPowerManagerInternal.getLowPowerState(3).batterySaverEnabled;
        }
        this.mScreenFrozenLock = this.mPowerManager.newWakeLock(1, "SCREEN_FROZEN");
        this.mScreenFrozenLock.setReferenceCounted(false);
        this.mAppTransition = new AppTransition(context2, this);
        this.mAppTransition.registerListenerLocked(this.mActivityManagerAppTransitionNotifier);
        this.mBoundsAnimationController = new BoundsAnimationController(context2, this.mAppTransition, AnimationThread.getHandler(), new AnimationHandler());
        this.mActivityManager = ActivityManager.getService();
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAppOps = (AppOpsManager) context2.getSystemService("appops");
        AppOpsManager.OnOpChangedListener r5 = new AppOpsManager.OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                if (WindowManagerService.this.mHwWMSEx != null) {
                    WindowManagerService.this.mHwWMSEx.sendUpdateAppOpsState();
                }
                WindowManagerService.this.mHwWMSEx.updateAppOpsStateReport(op, packageName);
            }
        };
        this.mAppOps.startWatchingMode(24, null, r5);
        this.mAppOps.startWatchingMode(45, null, r5);
        this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        IntentFilter suspendPackagesFilter = new IntentFilter();
        suspendPackagesFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        suspendPackagesFilter.addAction("android.intent.action.PACKAGES_UNSUSPENDED");
        IntentFilter intentFilter = suspendPackagesFilter;
        Object obj = r5;
        context2.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String[] affectedPackages = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                WindowManagerService.this.updateHiddenWhileSuspendedState(new ArraySet(Arrays.asList(affectedPackages)), "android.intent.action.PACKAGES_SUSPENDED".equals(intent.getAction()));
            }
        }, UserHandle.ALL, suspendPackagesFilter, null, null);
        this.mWindowAnimationScaleSetting = Settings.Global.getFloat(context.getContentResolver(), "window_animation_scale", this.mWindowAnimationScaleSetting);
        this.mTransitionAnimationScaleSetting = Settings.Global.getFloat(context.getContentResolver(), "transition_animation_scale", context.getResources().getFloat(17104958));
        setAnimatorDurationScale(Settings.Global.getFloat(context.getContentResolver(), "animator_duration_scale", this.mAnimatorDurationScaleSetting));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mLatencyTracker = LatencyTracker.getInstance(context);
        this.mSettingsObserver = new SettingsObserver();
        this.mHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870922, TAG);
        this.mHoldingScreenWakeLock.setReferenceCounted(false);
        this.mPCHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870918, TAG);
        this.mPCHoldingScreenWakeLock.setReferenceCounted(false);
        this.mSurfaceAnimationRunner = new SurfaceAnimationRunner();
        this.mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(17956886);
        IntentFilter intentFilter2 = filter;
        TaskPositioningController taskPositioningController = new TaskPositioningController(this, this.mInputManager, this.mInputMonitor, this.mActivityManager, this.mH.getLooper());
        this.mTaskPositioningController = taskPositioningController;
        this.mDragDropController = new DragDropController(this, this.mH.getLooper());
        LocalServices.addService(WindowManagerInternal.class, new LocalService());
    }

    /* JADX INFO: finally extract failed */
    public void onInitReady() {
        initPolicy();
        Watchdog.getInstance().addMonitor(this);
        IZrHung iZrHung = HwFrameworkFactory.getZrHung("appeye_frameworkblock");
        if (iZrHung != null) {
            ZrHungData data = new ZrHungData();
            data.put("monitor", this);
            iZrHung.check(data);
        }
        openSurfaceTransaction();
        try {
            createWatermarkInTransaction();
            closeSurfaceTransaction("createWatermarkInTransaction");
            showEmulatorDisplayOverlayIfNeeded();
        } catch (Throwable th) {
            closeSurfaceTransaction("createWatermarkInTransaction");
            throw th;
        }
    }

    public InputMonitor getInputMonitor() {
        return this.mInputMonitor;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Window Manager Crash", e);
            }
            throw e;
        }
    }

    static boolean excludeWindowTypeFromTapOutTask(int windowType) {
        if (windowType == 2000 || windowType == 2012 || windowType == 2019) {
            return true;
        }
        return false;
    }

    static boolean excludeWindowsFromTapOutTask(WindowState win) {
        WindowManager.LayoutParams attrs = win == null ? null : win.getAttrs();
        if (attrs == null) {
            return false;
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && "com.huawei.desktop.systemui".equals(attrs.packageName)) {
            return true;
        }
        if (attrs.type != 1000 || !"com.baidu.input_huawei".equals(attrs.packageName) || (HwPCUtils.isPcCastModeInServer() && !HwPCUtils.enabledInPad())) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v2, resolved type: android.view.IWindow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v3, resolved type: android.view.IWindow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v6, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v9, resolved type: android.view.IWindow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v1, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v10, resolved type: android.view.IWindow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v11, resolved type: android.view.IWindow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v2, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v23, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v9, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v13, resolved type: android.view.IWindow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r27v14, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v18, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v19, resolved type: com.android.server.wm.Session} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v20, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v21, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v22, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v23, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v24, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v25, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v26, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v27, resolved type: com.android.server.wm.WindowState} */
    /* JADX WARNING: type inference failed for: r6v3, types: [int] */
    /* JADX WARNING: type inference failed for: r4v15, types: [int] */
    /* JADX WARNING: Code restructure failed: missing block: B:411:0x0725, code lost:
        if (r14.mCurrentFocus.mOwnerUid == r8) goto L_0x074d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:429:0x0762, code lost:
        if (excludeWindowsFromTapOutTask(r1) != false) goto L_0x0764;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:354:0x0678  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x06a6 A[SYNTHETIC, Splitter:B:362:0x06a6] */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x06f4 A[Catch:{ all -> 0x069d }] */
    /* JADX WARNING: Removed duplicated region for block: B:394:0x06fd A[SYNTHETIC, Splitter:B:394:0x06fd] */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x074b  */
    /* JADX WARNING: Removed duplicated region for block: B:422:0x0753 A[SYNTHETIC, Splitter:B:422:0x0753] */
    /* JADX WARNING: Removed duplicated region for block: B:427:0x075e A[SYNTHETIC, Splitter:B:427:0x075e] */
    /* JADX WARNING: Removed duplicated region for block: B:448:0x07ea  */
    /* JADX WARNING: Removed duplicated region for block: B:451:0x07f2 A[Catch:{ all -> 0x0739 }] */
    /* JADX WARNING: Removed duplicated region for block: B:472:0x0830 A[SYNTHETIC, Splitter:B:472:0x0830] */
    /* JADX WARNING: Removed duplicated region for block: B:482:0x084d  */
    /* JADX WARNING: Removed duplicated region for block: B:487:0x085c A[SYNTHETIC, Splitter:B:487:0x085c] */
    /* JADX WARNING: Removed duplicated region for block: B:505:0x08bc A[Catch:{ all -> 0x097f }] */
    /* JADX WARNING: Removed duplicated region for block: B:508:0x08c4 A[Catch:{ all -> 0x097f }] */
    /* JADX WARNING: Removed duplicated region for block: B:516:0x08e4 A[Catch:{ all -> 0x097f }] */
    /* JADX WARNING: Removed duplicated region for block: B:519:0x08f2 A[Catch:{ all -> 0x097f }] */
    /* JADX WARNING: Removed duplicated region for block: B:530:0x0918 A[Catch:{ all -> 0x097f }] */
    /* JADX WARNING: Removed duplicated region for block: B:535:0x0945 A[Catch:{ all -> 0x097f }] */
    /* JADX WARNING: Removed duplicated region for block: B:544:0x0978  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public int addWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel) {
        int forceCompatMode;
        WindowHashMap windowHashMap;
        IBinder iBinder;
        int callingUid;
        int[] appOp;
        int forceCompatMode2;
        WindowState parentWindow;
        Rect rect;
        Session session2;
        int rootType;
        WindowToken token;
        boolean z;
        AppWindowToken atoken;
        IWindow iWindow;
        WindowState win;
        boolean openInputChannels;
        ? r6;
        int res;
        long origId;
        boolean imMayMove;
        int i;
        AppWindowToken atoken2;
        DisplayFrames displayFrames;
        DisplayInfo displayInfo;
        Rect taskBounds;
        boolean focusChanged;
        int callingUid2;
        int callingUid3;
        WindowState parentWindow2;
        int callingUid4;
        int callingUid5;
        WindowState parentWindow3;
        int forceCompatMode3;
        Session session3 = session;
        WindowManager.LayoutParams layoutParams = attrs;
        int i2 = displayId;
        InputChannel inputChannel = outInputChannel;
        if (this.mHwWMSEx != null) {
            this.mHwWMSEx.preAddWindow(layoutParams);
        }
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_ADDWINDOW, new Object[]{layoutParams});
        int[] appOp2 = new int[1];
        int res2 = this.mPolicy.checkAddPermission(layoutParams, appOp2);
        if (res2 != 0) {
            return res2;
        }
        boolean reportNewConfig = false;
        WindowState parentWindow4 = null;
        int callingUid6 = Binder.getCallingUid();
        int type = layoutParams.type;
        if (layoutParams.type >= 1000 && layoutParams.type <= 1999) {
            forceCompatMode = -3;
        } else if (layoutParams.packageName == null) {
            forceCompatMode = -3;
        } else if ((layoutParams.privateFlags & 128) != 0) {
            forceCompatMode = 1;
        } else {
            forceCompatMode = 0;
        }
        int forceCompatMode4 = forceCompatMode;
        if (WindowManagerDebugConfig.DEBUG_LAYOUT != 0) {
            Slog.e(TAG, "!!!!!!! addWindow: pkg:" + layoutParams.packageName + " forceCompatMode:" + forceCompatMode4);
        }
        WindowHashMap windowHashMap2 = this.mWindowMap;
        synchronized (windowHashMap2) {
            try {
                boostPriorityForLockedSection();
                if (this.mDisplayReady) {
                    DisplayContent displayContent = getDisplayContentOrCreate(i2);
                    if (displayContent == null) {
                        try {
                            Slog.w(TAG, "Attempted to add window to a display that does not exist: " + i2 + ".  Aborting.");
                            resetPriorityAfterLockedSection();
                            return -9;
                        } catch (Throwable th) {
                            th = th;
                            windowHashMap = windowHashMap2;
                            int i3 = i2;
                            resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        if (!displayContent.hasAccess(session3.mUid)) {
                            if (!this.mDisplayManagerInternal.isUidPresentOnDisplay(session3.mUid, i2)) {
                                Slog.w(TAG, "Attempted to add window to a display for which the application does not have access: " + i2 + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -9;
                            }
                        }
                        if (this.mWindowMap.containsKey(client.asBinder())) {
                            try {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Window ");
                                sb.append(client);
                                sb.append(" is already added");
                                Slog.w(TAG, sb.toString());
                                resetPriorityAfterLockedSection();
                                return -5;
                            } catch (Throwable th2) {
                                th = th2;
                                IWindow iWindow2 = client;
                                windowHashMap = windowHashMap2;
                                int i32 = i2;
                                resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else {
                            IWindow iWindow3 = client;
                            if (type >= 1000 && type <= 1999) {
                                parentWindow4 = windowForClientLocked((Session) null, layoutParams.token, false);
                                if (parentWindow4 == null) {
                                    Slog.w(TAG, "Attempted to add window with token that is not a window: " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -2;
                                } else if (parentWindow4.mAttrs.type >= 1000 && parentWindow4.mAttrs.type <= 1999) {
                                    Slog.w(TAG, "Attempted to add window with token that is a sub-window: " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -2;
                                }
                            }
                            WindowState parentWindow5 = parentWindow4;
                            if (type == 2030) {
                                try {
                                    if (!displayContent.isPrivate()) {
                                        Slog.w(TAG, "Attempted to add private presentation window to a non-private display.  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -8;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    WindowState windowState = parentWindow5;
                                }
                            }
                            boolean hasParent = parentWindow5 != null;
                            if (hasParent) {
                                iBinder = parentWindow5.mAttrs.token;
                            } else {
                                try {
                                    iBinder = layoutParams.token;
                                } catch (Throwable th4) {
                                    th = th4;
                                    WindowState windowState2 = parentWindow5;
                                    int i4 = forceCompatMode4;
                                    int i5 = type;
                                    int i6 = callingUid6;
                                    int[] iArr = appOp2;
                                    windowHashMap = windowHashMap2;
                                    int i7 = i2;
                                    WindowState windowState3 = windowState2;
                                    resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            WindowToken token2 = displayContent.getWindowToken(iBinder);
                            if (token2 == null) {
                                if ("com.google.android.marvin.talkback".equals(layoutParams.packageName) && HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                                    DisplayContent defaultDisplayContent = this.mRoot.getDisplayContent(0);
                                    if (defaultDisplayContent != null) {
                                        token2 = defaultDisplayContent.getWindowToken(hasParent ? parentWindow5.mAttrs.token : layoutParams.token);
                                    }
                                }
                            }
                            int rootType2 = hasParent ? parentWindow5.mAttrs.type : type;
                            boolean addToastWindowRequiresToken = false;
                            if (token2 != null) {
                                token = token2;
                                WindowState parentWindow6 = parentWindow5;
                                forceCompatMode2 = forceCompatMode4;
                                iWindow3 = type;
                                callingUid3 = callingUid6;
                                appOp = appOp2;
                                rootType = rootType2;
                                z = true;
                                if (rootType < 1 || rootType > 99) {
                                    if (rootType == 2011) {
                                        parentWindow2 = parentWindow6;
                                        if (token.windowType != 2011) {
                                            Slog.w(TAG, "Attempted to add input method window with bad token " + layoutParams.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                    } else if (rootType == 2031) {
                                        parentWindow2 = parentWindow6;
                                        if (token.windowType != 2031) {
                                            Slog.w(TAG, "Attempted to add voice interaction window with bad token " + layoutParams.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                    } else {
                                        if (rootType == 2013) {
                                            parentWindow2 = parentWindow6;
                                            if (token.windowType != 2013) {
                                                Slog.w(TAG, "Attempted to add wallpaper window with bad token " + layoutParams.token + ".  Aborting.");
                                                resetPriorityAfterLockedSection();
                                                return -1;
                                            }
                                        } else if (rootType == 2023) {
                                            parentWindow2 = parentWindow6;
                                            if (token.windowType != 2023) {
                                                Slog.w(TAG, "Attempted to add Dream window with bad token " + layoutParams.token + ".  Aborting.");
                                                resetPriorityAfterLockedSection();
                                                return -1;
                                            }
                                        } else if (rootType == 2032) {
                                            parentWindow2 = parentWindow6;
                                            if (token.windowType != 2032) {
                                                Slog.w(TAG, "Attempted to add Accessibility overlay window with bad token " + layoutParams.token + ".  Aborting.");
                                                resetPriorityAfterLockedSection();
                                                return -1;
                                            }
                                        } else if (iWindow3 == 2005) {
                                            try {
                                                callingUid4 = callingUid3;
                                                try {
                                                    addToastWindowRequiresToken = doesAddToastWindowRequireToken(layoutParams.packageName, callingUid4, parentWindow6);
                                                    if (!addToastWindowRequiresToken || token.windowType == 2005) {
                                                        callingUid = callingUid4;
                                                        parentWindow = parentWindow6;
                                                        atoken = null;
                                                        session2 = session;
                                                        rect = null;
                                                        int i8 = appOp[0];
                                                        int i9 = session2.mUid;
                                                        r1 = r1;
                                                        Session session4 = session2;
                                                        IWindow iWindow4 = client;
                                                        boolean z2 = session2.mCanAddInternalSystemWindow;
                                                        AppWindowToken atoken3 = atoken;
                                                        int i10 = i8;
                                                        boolean z3 = z;
                                                        windowHashMap = windowHashMap2;
                                                        WindowToken token3 = token;
                                                        iWindow = iWindow3;
                                                        Rect rect2 = rect;
                                                        InputChannel inputChannel2 = outInputChannel;
                                                        int i11 = rootType;
                                                        WindowManager.LayoutParams layoutParams2 = layoutParams;
                                                        WindowState windowState4 = new WindowState(this, session4, iWindow4, token, parentWindow, i10, seq, layoutParams, viewVisibility, i9, z2, forceCompatMode2);
                                                        win = windowState4;
                                                        if (win.mDeathRecipient == null) {
                                                            try {
                                                                Slog.w(TAG, "Adding window client " + client.asBinder() + " that is dead, aborting.");
                                                                resetPriorityAfterLockedSection();
                                                                return -4;
                                                            } catch (Throwable th5) {
                                                                th = th5;
                                                                WindowState windowState5 = parentWindow;
                                                                int i12 = callingUid;
                                                                IWindow iWindow5 = iWindow;
                                                                int i13 = displayId;
                                                                resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                        } else if (win.getDisplayContent() == null) {
                                                            Slog.w(TAG, "Adding window to Display that has been removed.");
                                                            resetPriorityAfterLockedSection();
                                                            return -9;
                                                        } else {
                                                            boolean hasStatusBarServicePermission = this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") == 0;
                                                            this.mPolicy.adjustWindowParamsLw(win, win.mAttrs, hasStatusBarServicePermission);
                                                            win.setShowToOwnerOnlyLocked(this.mPolicy.checkShowToOwnerOnly(layoutParams2));
                                                            int res3 = this.mPolicy.prepareAddWindowLw(win, layoutParams2);
                                                            if (res3 != 0) {
                                                                resetPriorityAfterLockedSection();
                                                                return res3;
                                                            }
                                                            WindowManager.LayoutParams layoutParams3 = layoutParams2;
                                                            InputChannel inputChannel3 = outInputChannel;
                                                            if (inputChannel3 != null) {
                                                                if ((layoutParams3.inputFeatures & 2) == 0) {
                                                                    openInputChannels = true;
                                                                    if (openInputChannels) {
                                                                        win.openInputChannel(inputChannel3);
                                                                    }
                                                                    r6 = iWindow;
                                                                    if (r6 != 2005) {
                                                                        try {
                                                                            callingUid2 = callingUid;
                                                                        } catch (Throwable th6) {
                                                                            th = th6;
                                                                            int i14 = callingUid;
                                                                            boolean z4 = r6;
                                                                            WindowState windowState6 = parentWindow;
                                                                            int i15 = displayId;
                                                                            resetPriorityAfterLockedSection();
                                                                            throw th;
                                                                        }
                                                                        try {
                                                                            if (!getDefaultDisplayContentLocked().canAddToastWindowForUid(callingUid2)) {
                                                                                Slog.w(TAG, "Adding more than one toast window for UID at a time.");
                                                                                resetPriorityAfterLockedSection();
                                                                                return -5;
                                                                            }
                                                                            if (!addToastWindowRequiresToken) {
                                                                                if ((layoutParams3.flags & 8) != 0) {
                                                                                    if (this.mCurrentFocus != null) {
                                                                                    }
                                                                                }
                                                                            }
                                                                            this.mH.sendMessageDelayed(this.mH.obtainMessage(52, win), win.mAttrs.hideTimeoutMilliseconds);
                                                                        } catch (Throwable th7) {
                                                                            th = th7;
                                                                            boolean z5 = r6;
                                                                            WindowState windowState7 = parentWindow;
                                                                        }
                                                                    }
                                                                    res = 0;
                                                                    if (this.mCurrentFocus == null) {
                                                                        this.mWinAddedSinceNullFocus.add(win);
                                                                    }
                                                                    if (!excludeWindowTypeFromTapOutTask(r6)) {
                                                                    }
                                                                    displayContent.mTapExcludedWindows.add(win);
                                                                    origId = Binder.clearCallingIdentity();
                                                                    win.attach();
                                                                    this.mWindowMap.put(client.asBinder(), win);
                                                                    win.initAppOpsState();
                                                                    win.setHiddenWhileSuspended(this.mPmInternal.isPackageSuspended(win.getOwningPackage(), UserHandle.getUserId(win.getOwningUid())));
                                                                    win.setForceHideNonSystemOverlayWindowIfNeeded(!this.mHidingNonSystemOverlayWindows.isEmpty());
                                                                    AppWindowToken aToken = token3.asAppWindowToken();
                                                                    if (r6 == 3 || aToken == null) {
                                                                        this.mHwWMSEx.updateHwStartWindowRecord(session.mUid);
                                                                    } else {
                                                                        aToken.startingWindow = win;
                                                                        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                                                            boolean z6 = hasStatusBarServicePermission;
                                                                            Slog.v(TAG, "addWindow: " + aToken + " startingWindow=" + win);
                                                                            Session session5 = session;
                                                                        } else {
                                                                            Session session6 = session;
                                                                        }
                                                                    }
                                                                    boolean imMayMove2 = true;
                                                                    win.mToken.addWindow(win);
                                                                    if (r6 != 2011) {
                                                                        win.mGivenInsetsPending = true;
                                                                        setInputMethodWindowLocked(win);
                                                                        imMayMove2 = false;
                                                                    } else if (r6 == 2012) {
                                                                        displayContent.computeImeTarget(true);
                                                                        imMayMove2 = false;
                                                                    } else if (r6 == 2013) {
                                                                        displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
                                                                        displayContent.pendingLayoutChanges |= 4;
                                                                    } else if ((layoutParams3.flags & DumpState.DUMP_DEXOPT) != 0) {
                                                                        displayContent.pendingLayoutChanges |= 4;
                                                                    } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
                                                                        displayContent.pendingLayoutChanges |= 4;
                                                                    }
                                                                    win.applyAdjustForImeIfNeeded();
                                                                    if (r6 != 2034) {
                                                                        try {
                                                                            imMayMove = imMayMove2;
                                                                            i = displayId;
                                                                            try {
                                                                                this.mRoot.getDisplayContent(i).getDockedDividerController().setWindow(win);
                                                                            } catch (Throwable th8) {
                                                                                th = th8;
                                                                            }
                                                                        } catch (Throwable th9) {
                                                                            th = th9;
                                                                            int i16 = displayId;
                                                                            int type2 = r6;
                                                                            resetPriorityAfterLockedSection();
                                                                            throw th;
                                                                        }
                                                                    } else {
                                                                        imMayMove = imMayMove2;
                                                                        i = displayId;
                                                                    }
                                                                    WindowStateAnimator winAnimator = win.mWinAnimator;
                                                                    winAnimator.mEnterAnimationPending = true;
                                                                    winAnimator.mEnteringAnimation = true;
                                                                    atoken2 = atoken3;
                                                                    if (atoken2 != null) {
                                                                        if (atoken2.isVisible() && !prepareWindowReplacementTransition(atoken2)) {
                                                                            prepareNoneTransitionForRelaunching(atoken2);
                                                                        }
                                                                    }
                                                                    displayFrames = displayContent.mDisplayFrames;
                                                                    boolean z7 = openInputChannels;
                                                                    boolean z8 = r6;
                                                                    displayInfo = displayContent.getDisplayInfo();
                                                                    try {
                                                                        displayFrames.onDisplayInfoUpdated(displayInfo, displayContent.calculateDisplayCutoutForRotation(displayInfo.rotation));
                                                                        if (atoken2 != null || atoken2.getTask() == null) {
                                                                            DisplayInfo displayInfo2 = displayInfo;
                                                                            taskBounds = rect2;
                                                                        } else {
                                                                            Rect taskBounds2 = this.mTmpRect;
                                                                            DisplayInfo displayInfo3 = displayInfo;
                                                                            AppWindowToken appWindowToken = atoken2;
                                                                            atoken2.getTask().getBounds(this.mTmpRect);
                                                                            taskBounds = taskBounds2;
                                                                        }
                                                                        if (this.mPolicy.getLayoutHintLw(win.mAttrs, taskBounds, displayFrames, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout)) {
                                                                            res = 0 | 4;
                                                                        }
                                                                        if (this.mInTouchMode != 0) {
                                                                            res |= 1;
                                                                        }
                                                                        if (win.mAppToken == null || !win.mAppToken.isClientHidden()) {
                                                                            res |= 2;
                                                                        }
                                                                        this.mInputMonitor.setUpdateInputWindowsNeededLw();
                                                                        focusChanged = false;
                                                                        if (!win.canReceiveKeys()) {
                                                                            focusChanged = updateFocusedWindowLocked(1, false);
                                                                            if (focusChanged) {
                                                                                imMayMove = false;
                                                                            }
                                                                        }
                                                                        if (imMayMove && (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || HwPCUtils.isValidExtDisplayId(displayContent.getDisplayId()))) {
                                                                            displayContent.computeImeTarget(true);
                                                                        }
                                                                        win.getParent().assignChildLayers();
                                                                        if (!focusChanged) {
                                                                            if (HwPCUtils.isPcCastModeInServer()) {
                                                                                StringBuilder sb2 = new StringBuilder();
                                                                                DisplayContent displayContent2 = displayContent;
                                                                                sb2.append("updateFocusedWindowLocked mCurrentFocus = ");
                                                                                sb2.append(this.mCurrentFocus);
                                                                                HwPCUtils.log(TAG, sb2.toString());
                                                                            }
                                                                            this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, false);
                                                                        }
                                                                        this.mInputMonitor.updateInputWindowsLw(false);
                                                                        Slog.v(TAG, "addWindow: " + win);
                                                                        if (win.isVisibleOrAdding() && updateOrientationFromAppTokensLocked(i)) {
                                                                            reportNewConfig = true;
                                                                        }
                                                                        resetPriorityAfterLockedSection();
                                                                        if (reportNewConfig) {
                                                                            sendNewConfiguration(i);
                                                                        }
                                                                        Binder.restoreCallingIdentity(origId);
                                                                        return res;
                                                                    } catch (Throwable th10) {
                                                                        th = th10;
                                                                        resetPriorityAfterLockedSection();
                                                                        throw th;
                                                                    }
                                                                }
                                                            }
                                                            openInputChannels = false;
                                                            if (openInputChannels) {
                                                            }
                                                            r6 = iWindow;
                                                            if (r6 != 2005) {
                                                            }
                                                            res = 0;
                                                            if (this.mCurrentFocus == null) {
                                                            }
                                                            try {
                                                                if (!excludeWindowTypeFromTapOutTask(r6)) {
                                                                }
                                                                displayContent.mTapExcludedWindows.add(win);
                                                                origId = Binder.clearCallingIdentity();
                                                                win.attach();
                                                                this.mWindowMap.put(client.asBinder(), win);
                                                                win.initAppOpsState();
                                                                win.setHiddenWhileSuspended(this.mPmInternal.isPackageSuspended(win.getOwningPackage(), UserHandle.getUserId(win.getOwningUid())));
                                                                win.setForceHideNonSystemOverlayWindowIfNeeded(!this.mHidingNonSystemOverlayWindows.isEmpty());
                                                                AppWindowToken aToken2 = token3.asAppWindowToken();
                                                                if (r6 == 3) {
                                                                }
                                                                this.mHwWMSEx.updateHwStartWindowRecord(session.mUid);
                                                                boolean imMayMove22 = true;
                                                                win.mToken.addWindow(win);
                                                                if (r6 != 2011) {
                                                                }
                                                                win.applyAdjustForImeIfNeeded();
                                                                if (r6 != 2034) {
                                                                }
                                                            } catch (Throwable th11) {
                                                                th = th11;
                                                                boolean z9 = r6;
                                                                int i17 = displayId;
                                                                resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                            try {
                                                                WindowStateAnimator winAnimator2 = win.mWinAnimator;
                                                                winAnimator2.mEnterAnimationPending = true;
                                                                winAnimator2.mEnteringAnimation = true;
                                                                atoken2 = atoken3;
                                                                if (atoken2 != null) {
                                                                }
                                                                displayFrames = displayContent.mDisplayFrames;
                                                                boolean z72 = openInputChannels;
                                                                boolean z82 = r6;
                                                                displayInfo = displayContent.getDisplayInfo();
                                                                displayFrames.onDisplayInfoUpdated(displayInfo, displayContent.calculateDisplayCutoutForRotation(displayInfo.rotation));
                                                                if (atoken2 != null) {
                                                                }
                                                                DisplayInfo displayInfo22 = displayInfo;
                                                                taskBounds = rect2;
                                                                if (this.mPolicy.getLayoutHintLw(win.mAttrs, taskBounds, displayFrames, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout)) {
                                                                }
                                                                if (this.mInTouchMode != 0) {
                                                                }
                                                                res |= 2;
                                                                this.mInputMonitor.setUpdateInputWindowsNeededLw();
                                                                focusChanged = false;
                                                                if (!win.canReceiveKeys()) {
                                                                }
                                                                displayContent.computeImeTarget(true);
                                                                win.getParent().assignChildLayers();
                                                                if (!focusChanged) {
                                                                }
                                                                this.mInputMonitor.updateInputWindowsLw(false);
                                                                Slog.v(TAG, "addWindow: " + win);
                                                                reportNewConfig = true;
                                                                resetPriorityAfterLockedSection();
                                                                if (reportNewConfig) {
                                                                }
                                                                Binder.restoreCallingIdentity(origId);
                                                                return res;
                                                            } catch (Throwable th12) {
                                                                th = th12;
                                                                boolean z10 = r6;
                                                                resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                        }
                                                    } else {
                                                        Slog.w(TAG, "Attempted to add a toast window with bad token " + layoutParams.token + ".  Aborting.");
                                                        resetPriorityAfterLockedSection();
                                                        return -1;
                                                    }
                                                } catch (Throwable th13) {
                                                    th = th13;
                                                    int i18 = callingUid4;
                                                    IWindow iWindow6 = iWindow3;
                                                    windowHashMap = windowHashMap2;
                                                    WindowState windowState8 = parentWindow6;
                                                    int i132 = displayId;
                                                    resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            } catch (Throwable th14) {
                                                th = th14;
                                                IWindow iWindow7 = iWindow3;
                                                windowHashMap = windowHashMap2;
                                                WindowState windowState9 = parentWindow6;
                                                int i19 = callingUid3;
                                                int i20 = displayId;
                                                resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        } else {
                                            callingUid4 = callingUid3;
                                            if (iWindow3 != 2035) {
                                                try {
                                                    if (token.asAppWindowToken() != null) {
                                                        try {
                                                            Slog.w(TAG, "Non-null appWindowToken for system window of rootType=" + rootType);
                                                            layoutParams.token = null;
                                                            parentWindow = parentWindow6;
                                                            session2 = session;
                                                            try {
                                                                token = token;
                                                                rect = null;
                                                                callingUid = callingUid4;
                                                                try {
                                                                    WindowToken token4 = new WindowToken(this, client.asBinder(), iWindow3, false, displayContent, session2.mCanAddInternalSystemWindow);
                                                                    token = token4;
                                                                    atoken = null;
                                                                    int i82 = appOp[0];
                                                                    int i92 = session2.mUid;
                                                                    windowState4 = windowState4;
                                                                    Session session42 = session2;
                                                                    IWindow iWindow42 = client;
                                                                    boolean z22 = session2.mCanAddInternalSystemWindow;
                                                                    AppWindowToken atoken32 = atoken;
                                                                    int i102 = i82;
                                                                    boolean z32 = z;
                                                                    windowHashMap = windowHashMap2;
                                                                    WindowToken token32 = token;
                                                                    iWindow = iWindow3;
                                                                    Rect rect22 = rect;
                                                                    InputChannel inputChannel22 = outInputChannel;
                                                                    int i112 = rootType;
                                                                    WindowManager.LayoutParams layoutParams22 = layoutParams;
                                                                    WindowState windowState42 = new WindowState(this, session42, iWindow42, token, parentWindow, i102, seq, layoutParams, viewVisibility, i92, z22, forceCompatMode2);
                                                                    win = windowState42;
                                                                    if (win.mDeathRecipient == null) {
                                                                    }
                                                                } catch (Throwable th15) {
                                                                    th = th15;
                                                                    IWindow iWindow8 = iWindow3;
                                                                    windowHashMap = windowHashMap2;
                                                                    WindowState windowState10 = parentWindow;
                                                                    int i21 = callingUid;
                                                                    int i1322 = displayId;
                                                                    resetPriorityAfterLockedSection();
                                                                    throw th;
                                                                }
                                                            } catch (Throwable th16) {
                                                                th = th16;
                                                                int i22 = callingUid4;
                                                                IWindow iWindow9 = iWindow3;
                                                                windowHashMap = windowHashMap2;
                                                                WindowState windowState11 = parentWindow;
                                                                int i23 = displayId;
                                                                resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                        } catch (Throwable th17) {
                                                            th = th17;
                                                            int i24 = callingUid4;
                                                            IWindow iWindow10 = iWindow3;
                                                            windowHashMap = windowHashMap2;
                                                            WindowState windowState12 = parentWindow6;
                                                            int i25 = displayId;
                                                            resetPriorityAfterLockedSection();
                                                            throw th;
                                                        }
                                                    } else {
                                                        callingUid5 = callingUid4;
                                                        parentWindow3 = parentWindow6;
                                                        session2 = session;
                                                        rect = null;
                                                        atoken = null;
                                                        int i822 = appOp[0];
                                                        int i922 = session2.mUid;
                                                        windowState42 = windowState42;
                                                        Session session422 = session2;
                                                        IWindow iWindow422 = client;
                                                        boolean z222 = session2.mCanAddInternalSystemWindow;
                                                        AppWindowToken atoken322 = atoken;
                                                        int i1022 = i822;
                                                        boolean z322 = z;
                                                        windowHashMap = windowHashMap2;
                                                        WindowToken token322 = token;
                                                        iWindow = iWindow3;
                                                        Rect rect222 = rect;
                                                        InputChannel inputChannel222 = outInputChannel;
                                                        int i1122 = rootType;
                                                        WindowManager.LayoutParams layoutParams222 = layoutParams;
                                                        WindowState windowState422 = new WindowState(this, session422, iWindow422, token, parentWindow, i1022, seq, layoutParams, viewVisibility, i922, z222, forceCompatMode2);
                                                        win = windowState422;
                                                        if (win.mDeathRecipient == null) {
                                                        }
                                                    }
                                                } catch (Throwable th18) {
                                                    th = th18;
                                                    int i26 = callingUid4;
                                                    IWindow iWindow11 = iWindow3;
                                                    windowHashMap = windowHashMap2;
                                                    int i27 = displayId;
                                                    WindowState windowState13 = parentWindow6;
                                                    resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            } else if (token.windowType != 2035) {
                                                Slog.w(TAG, "Attempted to add QS dialog window with bad token " + layoutParams.token + ".  Aborting.");
                                                resetPriorityAfterLockedSection();
                                                return -1;
                                            } else {
                                                callingUid5 = callingUid4;
                                                parentWindow3 = parentWindow6;
                                                session2 = session;
                                                rect = null;
                                                atoken = null;
                                                int i8222 = appOp[0];
                                                int i9222 = session2.mUid;
                                                windowState422 = windowState422;
                                                Session session4222 = session2;
                                                IWindow iWindow4222 = client;
                                                boolean z2222 = session2.mCanAddInternalSystemWindow;
                                                AppWindowToken atoken3222 = atoken;
                                                int i10222 = i8222;
                                                boolean z3222 = z;
                                                windowHashMap = windowHashMap2;
                                                WindowToken token3222 = token;
                                                iWindow = iWindow3;
                                                Rect rect2222 = rect;
                                                InputChannel inputChannel2222 = outInputChannel;
                                                int i11222 = rootType;
                                                WindowManager.LayoutParams layoutParams2222 = layoutParams;
                                                WindowState windowState4222 = new WindowState(this, session4222, iWindow4222, token, parentWindow, i10222, seq, layoutParams, viewVisibility, i9222, z2222, forceCompatMode2);
                                                win = windowState4222;
                                                if (win.mDeathRecipient == null) {
                                                }
                                            }
                                        }
                                        parentWindow3 = parentWindow6;
                                        callingUid5 = callingUid3;
                                        session2 = session;
                                        rect = null;
                                        atoken = null;
                                        int i82222 = appOp[0];
                                        int i92222 = session2.mUid;
                                        windowState4222 = windowState4222;
                                        Session session42222 = session2;
                                        IWindow iWindow42222 = client;
                                        boolean z22222 = session2.mCanAddInternalSystemWindow;
                                        AppWindowToken atoken32222 = atoken;
                                        int i102222 = i82222;
                                        boolean z32222 = z;
                                        windowHashMap = windowHashMap2;
                                        WindowToken token32222 = token;
                                        iWindow = iWindow3;
                                        Rect rect22222 = rect;
                                        InputChannel inputChannel22222 = outInputChannel;
                                        int i112222 = rootType;
                                        WindowManager.LayoutParams layoutParams22222 = layoutParams;
                                        WindowState windowState42222 = new WindowState(this, session42222, iWindow42222, token, parentWindow, i102222, seq, layoutParams, viewVisibility, i92222, z22222, forceCompatMode2);
                                        win = windowState42222;
                                        if (win.mDeathRecipient == null) {
                                        }
                                    }
                                    parentWindow3 = parentWindow6;
                                    callingUid5 = callingUid3;
                                    session2 = session;
                                    rect = null;
                                    atoken = null;
                                    int i822222 = appOp[0];
                                    int i922222 = session2.mUid;
                                    windowState42222 = windowState42222;
                                    Session session422222 = session2;
                                    IWindow iWindow422222 = client;
                                    boolean z222222 = session2.mCanAddInternalSystemWindow;
                                    AppWindowToken atoken322222 = atoken;
                                    int i1022222 = i822222;
                                    boolean z322222 = z;
                                    windowHashMap = windowHashMap2;
                                    WindowToken token322222 = token;
                                    iWindow = iWindow3;
                                    Rect rect222222 = rect;
                                    InputChannel inputChannel222222 = outInputChannel;
                                    int i1122222 = rootType;
                                    WindowManager.LayoutParams layoutParams222222 = layoutParams;
                                    WindowState windowState422222 = new WindowState(this, session422222, iWindow422222, token, parentWindow, i1022222, seq, layoutParams, viewVisibility, i922222, z222222, forceCompatMode2);
                                    win = windowState422222;
                                    if (win.mDeathRecipient == null) {
                                    }
                                } else {
                                    AppWindowToken atoken4 = token.asAppWindowToken();
                                    if (atoken4 == null) {
                                        Slog.w(TAG, "Attempted to add window with non-application token " + token + ".  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -3;
                                    }
                                    parentWindow2 = parentWindow6;
                                    if (atoken4.removed) {
                                        Slog.w(TAG, "Attempted to add window with exiting application token " + token + ".  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -4;
                                    }
                                    if (iWindow3 == 3) {
                                        parentWindow2 = parentWindow6;
                                        if (atoken4.startingWindow != null) {
                                            Slog.w(TAG, "Attempted to add starting window to token with already existing starting window");
                                            resetPriorityAfterLockedSection();
                                            return -5;
                                        }
                                    }
                                    atoken = atoken4;
                                    parentWindow = parentWindow6;
                                    callingUid = callingUid3;
                                }
                            } else if (rootType2 < 1 || rootType2 > 99) {
                                WindowToken token5 = token2;
                                forceCompatMode3 = forceCompatMode4;
                                if (rootType2 == 2011) {
                                    Slog.w(TAG, "Attempted to add input method window with unknown token " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -1;
                                } else if (rootType2 == 2031) {
                                    Slog.w(TAG, "Attempted to add voice interaction window with unknown token " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -1;
                                } else if (rootType2 == 2013) {
                                    Slog.w(TAG, "Attempted to add wallpaper window with unknown token " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -1;
                                } else if (rootType2 == 2023) {
                                    Slog.w(TAG, "Attempted to add Dream window with unknown token " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -1;
                                } else if (rootType2 == 2035) {
                                    Slog.w(TAG, "Attempted to add QS dialog window with unknown token " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -1;
                                } else if (rootType2 == 2032) {
                                    Slog.w(TAG, "Attempted to add Accessibility overlay window with unknown token " + layoutParams.token + ".  Aborting.");
                                    resetPriorityAfterLockedSection();
                                    return -1;
                                } else {
                                    if (type == 2005) {
                                        if (doesAddToastWindowRequireToken(layoutParams.packageName, callingUid6, parentWindow5)) {
                                            Slog.w(TAG, "Attempted to add a toast window with unknown token " + layoutParams.token + ".  Aborting.");
                                            resetPriorityAfterLockedSection();
                                            return -1;
                                        }
                                    }
                                    try {
                                        parentWindow2 = session3;
                                        IBinder binder = layoutParams.token != null ? layoutParams.token : client.asBinder();
                                        try {
                                            parentWindow2 = session3;
                                            rootType = rootType2;
                                            boolean isRoundedCornerOverlay = (layoutParams.privateFlags & DumpState.DUMP_DEXOPT) != 0;
                                            boolean z11 = session3.mCanAddInternalSystemWindow;
                                            WindowToken windowToken = token5;
                                            token = token;
                                            WindowState parentWindow7 = parentWindow5;
                                            forceCompatMode2 = forceCompatMode3;
                                            iWindow3 = type;
                                            callingUid3 = callingUid6;
                                            appOp = appOp2;
                                            parentWindow2 = parentWindow7;
                                            WindowToken token6 = new WindowToken(this, binder, type, false, displayContent, z11, isRoundedCornerOverlay);
                                            token = token6;
                                            parentWindow = parentWindow7;
                                            atoken = null;
                                            callingUid = callingUid3;
                                            z = true;
                                        } catch (Throwable th19) {
                                            th = th19;
                                            IWindow iWindow12 = iWindow3;
                                            windowHashMap = windowHashMap2;
                                            Session session7 = parentWindow2;
                                            int i28 = callingUid3;
                                            int i13222 = displayId;
                                            resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } catch (Throwable th20) {
                                        th = th20;
                                        WindowState windowState14 = parentWindow5;
                                        int[] iArr2 = appOp2;
                                        int i29 = forceCompatMode3;
                                        int i30 = type;
                                        int i31 = callingUid6;
                                        windowHashMap = windowHashMap2;
                                        int i33 = i2;
                                        WindowState windowState15 = windowState14;
                                        resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                            } else {
                                WindowToken windowToken2 = token2;
                                try {
                                    StringBuilder sb3 = new StringBuilder();
                                    forceCompatMode3 = forceCompatMode4;
                                    try {
                                        sb3.append("Attempted to add application window with unknown token ");
                                        sb3.append(layoutParams.token);
                                        sb3.append(".  Aborting.");
                                        Slog.w(TAG, sb3.toString());
                                        resetPriorityAfterLockedSection();
                                        return -1;
                                    } catch (Throwable th21) {
                                        th = th21;
                                        WindowState windowState16 = parentWindow5;
                                        int i34 = type;
                                        int i35 = callingUid6;
                                        int[] iArr3 = appOp2;
                                        windowHashMap = windowHashMap2;
                                        int i36 = i2;
                                        int i37 = forceCompatMode3;
                                    }
                                } catch (Throwable th22) {
                                    th = th22;
                                    WindowState windowState17 = parentWindow5;
                                    int i38 = forceCompatMode4;
                                    int i39 = type;
                                    int i40 = callingUid6;
                                    int[] iArr4 = appOp2;
                                    windowHashMap = windowHashMap2;
                                    int i41 = i2;
                                    resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            session2 = session;
                            rect = null;
                            try {
                                int i8222222 = appOp[0];
                                int i9222222 = session2.mUid;
                                windowState422222 = windowState422222;
                                Session session4222222 = session2;
                                IWindow iWindow4222222 = client;
                                boolean z2222222 = session2.mCanAddInternalSystemWindow;
                                AppWindowToken atoken3222222 = atoken;
                                int i10222222 = i8222222;
                                boolean z3222222 = z;
                                windowHashMap = windowHashMap2;
                                WindowToken token3222222 = token;
                                iWindow = iWindow3;
                                Rect rect2222222 = rect;
                                InputChannel inputChannel2222222 = outInputChannel;
                                int i11222222 = rootType;
                                WindowManager.LayoutParams layoutParams2222222 = layoutParams;
                            } catch (Throwable th23) {
                                th = th23;
                                IWindow iWindow13 = iWindow3;
                                windowHashMap = windowHashMap2;
                                int i42 = callingUid;
                                int i43 = displayId;
                                WindowState windowState18 = parentWindow;
                                resetPriorityAfterLockedSection();
                                throw th;
                            }
                            try {
                                WindowState windowState4222222 = new WindowState(this, session4222222, iWindow4222222, token, parentWindow, i10222222, seq, layoutParams, viewVisibility, i9222222, z2222222, forceCompatMode2);
                                win = windowState4222222;
                                if (win.mDeathRecipient == null) {
                                }
                            } catch (Throwable th24) {
                                th = th24;
                                int i44 = callingUid;
                                IWindow iWindow14 = iWindow;
                                int i45 = displayId;
                                WindowState windowState19 = parentWindow;
                                resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                } else {
                    int i46 = type;
                    int i47 = callingUid6;
                    int[] iArr5 = appOp2;
                    windowHashMap = windowHashMap2;
                    int i48 = i2;
                    throw new IllegalStateException("Display has not been initialialized");
                }
            } catch (Throwable th25) {
                th = th25;
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    private DisplayContent getDisplayContentOrCreate(int displayId) {
        DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
        if (displayContent != null) {
            return displayContent;
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display != null) {
            return this.mRoot.createDisplayContent(display, null);
        }
        return displayContent;
    }

    private boolean doesAddToastWindowRequireToken(String packageName, int callingUid, WindowState attachedWindow) {
        boolean z = true;
        if (attachedWindow != null) {
            if (attachedWindow.mAppToken == null || attachedWindow.mAppToken.mTargetSdk < 26) {
                z = false;
            }
            return z;
        }
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
            if (appInfo.uid == callingUid) {
                return appInfo.targetSdkVersion >= 26;
            }
            throw new SecurityException("Package " + packageName + " not in UID " + callingUid);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    private boolean prepareWindowReplacementTransition(AppWindowToken atoken) {
        atoken.clearAllDrawn();
        WindowState replacedWindow = atoken.getReplacingWindow();
        if (replacedWindow == null) {
            return false;
        }
        Rect frame = replacedWindow.mVisibleFrame;
        this.mOpeningApps.add(atoken);
        prepareAppTransition(18, true);
        this.mAppTransition.overridePendingAppTransitionClipReveal(frame.left, frame.top, frame.width(), frame.height());
        executeAppTransition();
        return true;
    }

    private void prepareNoneTransitionForRelaunching(AppWindowToken atoken) {
        if (this.mDisplayFrozen && !this.mOpeningApps.contains(atoken) && atoken.isRelaunching()) {
            this.mOpeningApps.add(atoken);
            prepareAppTransition(0, false);
            executeAppTransition();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSecureLocked(WindowState w) {
        if ((w.mAttrs.flags & 8192) == 0 && !DevicePolicyCache.getInstance().getScreenCaptureDisabled(UserHandle.getUserId(w.mOwnerUid))) {
            return false;
        }
        return true;
    }

    public void refreshScreenCaptureDisabled(int userId) {
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.setSecureSurfaceState(userId, DevicePolicyCache.getInstance().getScreenCaptureDisabled(userId));
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Only system can call refreshScreenCaptureDisabled.");
    }

    public void updateAppOpsState() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.updateAppOpsState();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void removeWindow(Session session, IWindow client) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    resetPriorityAfterLockedSection();
                    return;
                }
                win.removeIfPossible();
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postWindowRemoveCleanupLocked(WindowState win) {
        Slog.v(TAG, "postWindowRemoveCleanupLocked: " + win);
        this.mWindowMap.remove(win.mClient.asBinder());
        markForSeamlessRotation(win, false);
        win.resetAppOpsState();
        this.mHwWMSEx.removeWindowReport(win);
        if (this.mCurrentFocus == null) {
            this.mWinRemovedSinceNullFocus.add(win);
        }
        this.mPendingRemove.remove(win);
        this.mResizingWindows.remove(win);
        updateNonSystemOverlayWindowsVisibilityIfNeeded(win, false);
        this.mWindowsChanged = true;
        if (this.mInputMethodWindow == win) {
            setInputMethodWindowLocked(null);
        }
        WindowToken token = win.mToken;
        AppWindowToken atoken = win.mAppToken;
        Slog.v(TAG, "Removing " + win + " from " + token);
        if (token.isEmpty()) {
            if (!token.mPersistOnEmpty) {
                token.removeImmediately();
            } else if (atoken != null) {
                atoken.firstWindowDrawn = false;
                atoken.clearAllDrawn();
                TaskStack stack = atoken.getStack();
                if (stack != null) {
                    stack.mExitingAppTokens.remove(atoken);
                }
            }
        }
        if (atoken != null) {
            atoken.postWindowRemoveStartingWindowCleanup(win);
        }
        DisplayContent dc = win.getDisplayContent();
        if (win.mAttrs.type == 2013) {
            dc.mWallpaperController.clearLastWallpaperTimeoutTime();
            dc.pendingLayoutChanges |= 4;
        } else if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
            dc.pendingLayoutChanges |= 4;
        }
        if (dc != null && !this.mWindowPlacerLocked.isInLayout()) {
            dc.assignWindowLayers(true);
            this.mWindowPlacerLocked.performSurfacePlacement();
            if (win.mAppToken != null) {
                win.mAppToken.updateReportedVisibilityLocked();
            }
        }
        this.mInputMonitor.updateInputWindowsLw(true);
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodWindowLocked(WindowState win) {
        if (win != null && ((this.mInputMethodWindow == null || this.mInputMethodWindow != win) && isLandScapeMultiWindowMode() && (this.mPolicy instanceof PhoneWindowManager))) {
            ((PhoneWindowManager) this.mPolicy).setFocusChangeIMEFrozenTag(false);
        }
        this.mInputMethodWindow = win;
        (win != null ? win.getDisplayContent() : getDefaultDisplayContentLocked()).computeImeTarget(true);
    }

    /* access modifiers changed from: private */
    public void updateHiddenWhileSuspendedState(ArraySet<String> packages, boolean suspended) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.updateHiddenWhileSuspendedState(packages, suspended);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    static void logSurface(WindowState w, String msg, boolean withStackTrace) {
        String str = "  SURFACE " + msg + ": " + w;
        if (withStackTrace) {
            logWithStack(TAG, str);
        } else {
            Slog.i(TAG, str);
        }
    }

    static void logSurface(SurfaceControl s, String title, String msg) {
        Slog.i(TAG, "  SURFACE " + s + ": " + msg + " / " + title);
    }

    static void logWithStack(String tag, String s) {
        Slog.i(tag, s, null);
    }

    /* access modifiers changed from: package-private */
    public void setTransparentRegionWindow(Session session, IWindow client, Region region) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                WindowState w = windowForClientLocked(session, client, false);
                if (w != null && w.mHasSurface) {
                    w.mWinAnimator.setTransparentRegionHintLocked(region);
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void setInsetsWindow(Session session, IWindow client, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                WindowState w = windowForClientLocked(session, client, false);
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.d(TAG, "setInsetsWindow " + w + ", contentInsets=" + w.mGivenContentInsets + " -> " + contentInsets + ", visibleInsets=" + w.mGivenVisibleInsets + " -> " + visibleInsets + ", touchableRegion=" + w.mGivenTouchableRegion + " -> " + touchableRegion + ", touchableInsets " + w.mTouchableInsets + " -> " + touchableInsets);
                }
                if (w != null) {
                    w.mGivenInsetsPending = false;
                    w.mGivenContentInsets.set(contentInsets);
                    w.mGivenVisibleInsets.set(visibleInsets);
                    w.mGivenTouchableRegion.set(touchableRegion);
                    w.mTouchableInsets = touchableInsets;
                    if (w.mGlobalScale != 1.0f) {
                        w.mGivenContentInsets.scale(w.mGlobalScale);
                        w.mGivenVisibleInsets.scale(w.mGlobalScale);
                        w.mGivenTouchableRegion.scale(w.mGlobalScale);
                    }
                    w.setDisplayLayoutNeeded();
                    this.mWindowPlacerLocked.performSurfacePlacement();
                    if (this.mAccessibilityController != null && w.getDisplayContent().getDisplayId() == 0) {
                        this.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                    }
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    public void getWindowDisplayFrame(Session session, IWindow client, Rect outDisplayFrame) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    outDisplayFrame.setEmpty();
                    resetPriorityAfterLockedSection();
                    return;
                }
                outDisplayFrame.set(win.mDisplayFrame);
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mAccessibilityController != null) {
                    WindowState window = (WindowState) this.mWindowMap.get(token);
                    if (window != null && (window.getDisplayId() == 0 || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad()))) {
                        this.mAccessibilityController.onRectangleOnScreenRequestedLocked(rectangle);
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public IWindowId getWindowId(IBinder token) {
        WindowState.WindowId windowId;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState window = (WindowState) this.mWindowMap.get(token);
                windowId = window != null ? window.mWindowId : null;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return windowId;
    }

    public void pokeDrawLock(Session session, IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState window = windowForClientLocked(session, token, false);
                if (window != null) {
                    window.pokeDrawLockLw(this.mDrawLockTimeoutMillis);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:141:0x01df, code lost:
        if (r12.mAttrs.surfaceInsets.bottom == 0) goto L_0x01e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:430:0x0668, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:431:0x066c, code lost:
        if (r13 == false) goto L_0x067c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:432:0x066e, code lost:
        android.os.Trace.traceBegin(32, "relayoutWindow: sendNewConfiguration");
        sendNewConfiguration(r2);
        android.os.Trace.traceEnd(32);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:433:0x067c, code lost:
        android.os.Binder.restoreCallingIdentity(r38);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:434:0x0681, code lost:
        return (int) r10;
     */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x01b6 A[SYNTHETIC, Splitter:B:130:0x01b6] */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x01c9 A[SYNTHETIC, Splitter:B:135:0x01c9] */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x01ef A[SYNTHETIC, Splitter:B:148:0x01ef] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0238 A[Catch:{ all -> 0x06c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x0248 A[Catch:{ all -> 0x06c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x025a A[SYNTHETIC, Splitter:B:174:0x025a] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0274 A[SYNTHETIC, Splitter:B:189:0x0274] */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x0283 A[Catch:{ all -> 0x0268 }] */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0286 A[Catch:{ all -> 0x0268 }] */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x0290 A[Catch:{ all -> 0x0268 }] */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x02ad A[SYNTHETIC, Splitter:B:208:0x02ad] */
    /* JADX WARNING: Removed duplicated region for block: B:210:0x02e0  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x02eb A[Catch:{ all -> 0x06c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x02ed A[Catch:{ all -> 0x06c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x02f3 A[SYNTHETIC, Splitter:B:219:0x02f3] */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x0318 A[Catch:{ all -> 0x0268 }] */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x033e A[Catch:{ all -> 0x0268 }] */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x034e A[SYNTHETIC, Splitter:B:245:0x034e] */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x036b  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x043a  */
    /* JADX WARNING: Removed duplicated region for block: B:339:0x04d5  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x04e8  */
    /* JADX WARNING: Removed duplicated region for block: B:349:0x04ea  */
    /* JADX WARNING: Removed duplicated region for block: B:353:0x04f1  */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x04fd A[Catch:{ Exception -> 0x03ef, all -> 0x042f, all -> 0x04e0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x050d A[SYNTHETIC, Splitter:B:363:0x050d] */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x0529 A[SYNTHETIC, Splitter:B:369:0x0529] */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x055f A[Catch:{ all -> 0x0556 }] */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0580 A[SYNTHETIC, Splitter:B:386:0x0580] */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x0589  */
    /* JADX WARNING: Removed duplicated region for block: B:398:0x0597 A[Catch:{ all -> 0x0684 }] */
    /* JADX WARNING: Removed duplicated region for block: B:401:0x05a0  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x05a5 A[Catch:{ all -> 0x0556 }] */
    /* JADX WARNING: Removed duplicated region for block: B:406:0x05ab  */
    /* JADX WARNING: Removed duplicated region for block: B:414:0x0601  */
    public int relayoutWindow(Session session, IWindow client, int seq, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, long frameNumber, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, DisplayCutout.ParcelableWrapper outCutout, MergedConfiguration mergedConfiguration, Surface outSurface) {
        int attrChanges;
        int flagChanges;
        boolean z;
        int oldVisibility;
        boolean imMayMove;
        boolean isDefaultDisplay;
        boolean focusMayChange;
        boolean wallpaperMayMove;
        boolean wallpaperMayMove2;
        int displayId;
        boolean shouldRelayout;
        boolean focusMayChange2;
        long origId;
        WindowState win;
        WindowStateAnimator winAnimator;
        int result;
        boolean toBeDisplayed;
        long origId2;
        MergedConfiguration mergedConfiguration2;
        DisplayContent displayContent;
        int result2;
        long j;
        IWindow iWindow = client;
        WindowManager.LayoutParams layoutParams = attrs;
        int i = requestedWidth;
        int i2 = requestedHeight;
        int i3 = viewVisibility;
        MergedConfiguration mergedConfiguration3 = mergedConfiguration;
        Surface surface = outSurface;
        int result3 = 0;
        boolean hasStatusBarPermission = this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0;
        boolean hasStatusBarServicePermission = this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") == 0;
        long origId3 = Binder.clearCallingIdentity();
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                long origId4 = origId3;
                try {
                    WindowState win2 = windowForClientLocked(session, iWindow, false);
                    if (win2 == null) {
                        try {
                            resetPriorityAfterLockedSection();
                            return 0;
                        } catch (Throwable th) {
                            e = th;
                            Rect rect = outFrame;
                            Surface surface2 = surface;
                            boolean z2 = hasStatusBarPermission;
                            boolean z3 = hasStatusBarServicePermission;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    e = th2;
                                }
                            }
                            resetPriorityAfterLockedSection();
                            throw e;
                        }
                    } else {
                        int displayId2 = win2.getDisplayId();
                        this.mHwWMSEx.updateWindowReport(win2, i, i2);
                        WindowStateAnimator winAnimator2 = win2.mWinAnimator;
                        if (i3 != 8) {
                            win2.setRequestedSize(i, i2);
                        }
                        try {
                            win2.setFrameNumber(frameNumber);
                            int flagChanges2 = 0;
                            if (layoutParams != null) {
                                try {
                                    this.mPolicy.adjustWindowParamsLw(win2, layoutParams, hasStatusBarServicePermission);
                                    if (seq == win2.mSeq) {
                                        int systemUiVisibility = layoutParams.systemUiVisibility | layoutParams.subtreeSystemUiVisibility;
                                        if ((67043328 & systemUiVisibility) != 0 && !hasStatusBarPermission) {
                                            systemUiVisibility &= -67043329;
                                        }
                                        win2.mSystemUiVisibility = systemUiVisibility;
                                    }
                                    if (win2.mAttrs.type == layoutParams.type) {
                                        if ((layoutParams.privateFlags & 8192) != 0) {
                                            layoutParams.x = win2.mAttrs.x;
                                            layoutParams.y = win2.mAttrs.y;
                                            layoutParams.width = win2.mAttrs.width;
                                            layoutParams.height = win2.mAttrs.height;
                                        }
                                        WindowManager.LayoutParams layoutParams2 = win2.mAttrs;
                                        int i4 = layoutParams.flags ^ layoutParams2.flags;
                                        layoutParams2.flags = i4;
                                        flagChanges2 = i4;
                                        int attrChanges2 = win2.mAttrs.copyFrom(layoutParams);
                                        if ((attrChanges2 & 16385) != 0) {
                                            win2.mLayoutNeeded = true;
                                        }
                                        if (!(win2.mAppToken == null || ((flagChanges2 & DumpState.DUMP_FROZEN) == 0 && (flagChanges2 & DumpState.DUMP_CHANGES) == 0))) {
                                            win2.mAppToken.checkKeyguardFlagsChanged();
                                        }
                                        if (!((33554432 & attrChanges2) == 0 || this.mAccessibilityController == null || (win2.getDisplayId() != 0 && (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.enabledInPad())))) {
                                            this.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                                        }
                                        if ((flagChanges2 & DumpState.DUMP_FROZEN) != 0) {
                                            updateNonSystemOverlayWindowsVisibilityIfNeeded(win2, win2.mWinAnimator.getShown());
                                        }
                                        attrChanges = attrChanges2;
                                    } else {
                                        throw new IllegalArgumentException("Window type can not be changed after the window is added, type changed from " + win2.mAttrs.type + " to " + layoutParams.type);
                                    }
                                } catch (Throwable th3) {
                                    e = th3;
                                    Rect rect2 = outFrame;
                                    boolean z4 = hasStatusBarPermission;
                                    Surface surface3 = outSurface;
                                    while (true) {
                                        break;
                                    }
                                    resetPriorityAfterLockedSection();
                                    throw e;
                                }
                            } else {
                                attrChanges = 0;
                            }
                            flagChanges = flagChanges2;
                            boolean z5 = hasStatusBarPermission;
                            if (win2.toString().contains("HwFullScreenWindow")) {
                                try {
                                    this.mPolicy.setFullScreenWinVisibile(i3 == 0);
                                    this.mPolicy.setFullScreenWindow(win2);
                                } catch (Throwable th4) {
                                    e = th4;
                                    Rect rect3 = outFrame;
                                    Surface surface32 = outSurface;
                                    while (true) {
                                        break;
                                    }
                                    resetPriorityAfterLockedSection();
                                    throw e;
                                }
                            }
                        } catch (Throwable th5) {
                            e = th5;
                            Rect rect4 = outFrame;
                            boolean z6 = hasStatusBarPermission;
                            boolean z7 = hasStatusBarServicePermission;
                            long j2 = origId4;
                            Surface surface4 = outSurface;
                            while (true) {
                                break;
                            }
                            resetPriorityAfterLockedSection();
                            throw e;
                        }
                        try {
                            if (HW_SUPPORT_LAUNCHER_EXIT_ANIM) {
                                if (win2.mWinAnimator != null && win2.mAnimatingExit && i3 == 0) {
                                    win2.mWinAnimator.setWindowClipFlag(0);
                                    Log.d(TAG, "Relayout clear glRoundRectFlag");
                                }
                            }
                            WindowStateAnimator winAnimator3 = winAnimator2;
                            winAnimator3.mSurfaceDestroyDeferred = (flags & 2) != 0;
                            if (this.mCurrentFocus == win2 && (Integer.MIN_VALUE & attrChanges) != 0) {
                                this.mPolicy.updateSystemUiColorLw(win2);
                            }
                            if ((win2.mAttrs.privateFlags & 128) != 0) {
                                if (!win2.isInHwFreeFormWorkspace()) {
                                    z = true;
                                    win2.mEnforceSizeCompat = z;
                                    if ((attrChanges & 128) != 0) {
                                        winAnimator3.mAlpha = layoutParams.alpha;
                                    }
                                    win2.setWindowScale(win2.mRequestedWidth, win2.mRequestedHeight);
                                    if (win2.mAttrs.surfaceInsets.left == 0) {
                                        if (win2.mAttrs.surfaceInsets.top == 0) {
                                            if (win2.mAttrs.surfaceInsets.right == 0) {
                                            }
                                        }
                                    }
                                    winAnimator3.setOpaqueLocked(false);
                                    oldVisibility = win2.mViewVisibility;
                                    boolean shouldPrintLog = false;
                                    if (!WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                        if (win2.mViewVisibility == i3) {
                                            boolean z8 = hasStatusBarServicePermission;
                                            boolean becameVisible = (oldVisibility != 4 || oldVisibility == 8) && i3 == 0;
                                            if ((flagChanges & 131080) == 0) {
                                                if (!becameVisible) {
                                                    imMayMove = false;
                                                    isDefaultDisplay = win2.isDefaultDisplay();
                                                    if (isDefaultDisplay) {
                                                        try {
                                                            if (win2.mViewVisibility != i3 || (flagChanges != false && true) || !win2.mRelayoutCalled) {
                                                                focusMayChange = true;
                                                                if (win2.mViewVisibility != i3) {
                                                                    if ((win2.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                                                                        wallpaperMayMove = true;
                                                                        boolean wallpaperMayMove3 = wallpaperMayMove | ((flagChanges & DumpState.DUMP_DEXOPT) == 0);
                                                                        boolean z9 = becameVisible;
                                                                        if ((flagChanges & 8192) != 0 || winAnimator3.mSurfaceController == null) {
                                                                        } else {
                                                                            boolean z10 = flagChanges;
                                                                            winAnimator3.mSurfaceController.setSecure(isSecureLocked(win2));
                                                                        }
                                                                        win2.mRelayoutCalled = true;
                                                                        win2.mInRelayout = true;
                                                                        win2.mViewVisibility = i3;
                                                                        if (!WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                                                            RuntimeException stack = new RuntimeException();
                                                                            stack.fillInStackTrace();
                                                                            displayId = displayId2;
                                                                            StringBuilder sb = new StringBuilder();
                                                                            wallpaperMayMove2 = wallpaperMayMove3;
                                                                            sb.append("Relayout ");
                                                                            sb.append(win2);
                                                                            sb.append(": oldVis=");
                                                                            sb.append(oldVisibility);
                                                                            sb.append(" newVis=");
                                                                            sb.append(i3);
                                                                            Slog.i(TAG, sb.toString(), stack);
                                                                        } else {
                                                                            wallpaperMayMove2 = wallpaperMayMove3;
                                                                            displayId = displayId2;
                                                                        }
                                                                        win2.setDisplayLayoutNeeded();
                                                                        win2.mGivenInsetsPending = (flags & 1) == 0;
                                                                        if (i3 == 0) {
                                                                            if (win2.mAppToken == null || win2.mAttrs.type == 3 || !win2.mAppToken.isClientHidden()) {
                                                                                shouldRelayout = true;
                                                                                if (!shouldRelayout && winAnimator3.hasSurface() && !win2.mAnimatingExit) {
                                                                                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                                                        Slog.i(TAG, "Relayout invis " + win2 + ": mAnimatingExit=" + win2.mAnimatingExit);
                                                                                    }
                                                                                    result3 = 0 | 4;
                                                                                    if (!win2.mWillReplaceWindow) {
                                                                                        focusMayChange2 = tryStartExitingAnimation(win2, winAnimator3, isDefaultDisplay, focusMayChange);
                                                                                        this.mAppTransitTrack = "relayout";
                                                                                        if (this.mWaitingForConfig) {
                                                                                            if (win2.mAppToken != null && !this.mDeferRelayoutWindow.contains(win2)) {
                                                                                                this.mDeferRelayoutWindow.add(win2);
                                                                                            }
                                                                                        }
                                                                                        this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                                                        WindowState win3 = win2;
                                                                                        if (!shouldRelayout) {
                                                                                            try {
                                                                                                Trace.traceBegin(32, "relayoutWindow: viewVisibility_1");
                                                                                                win = win3;
                                                                                                try {
                                                                                                    result = createSurfaceControl(outSurface, win.relayoutVisibleWindow((int) result3, attrChanges, oldVisibility), win, winAnimator3);
                                                                                                    if ((result & 2) != 0) {
                                                                                                        focusMayChange2 = isDefaultDisplay;
                                                                                                    }
                                                                                                } catch (Exception e) {
                                                                                                    int i5 = oldVisibility;
                                                                                                    boolean z11 = isDefaultDisplay;
                                                                                                    Exception exc = e;
                                                                                                    this.mInputMonitor.updateInputWindowsLw(true);
                                                                                                    Slog.w(TAG, "Exception thrown when creating surface for client " + iWindow + " (" + win.mAttrs.getTitle() + ")", e);
                                                                                                    origId = origId4;
                                                                                                    Binder.restoreCallingIdentity(origId);
                                                                                                    resetPriorityAfterLockedSection();
                                                                                                    return 0;
                                                                                                } catch (Throwable th6) {
                                                                                                    th = th6;
                                                                                                    long j3 = origId;
                                                                                                    Rect rect5 = outFrame;
                                                                                                    while (true) {
                                                                                                        break;
                                                                                                    }
                                                                                                    resetPriorityAfterLockedSection();
                                                                                                    throw e;
                                                                                                }
                                                                                            } catch (Throwable th7) {
                                                                                                e = th7;
                                                                                                Surface surface5 = outSurface;
                                                                                                Rect rect6 = outFrame;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                resetPriorityAfterLockedSection();
                                                                                                throw e;
                                                                                            }
                                                                                            try {
                                                                                                if (win.mAttrs.type == 2011 && (this.mInputMethodWindow == null || this.mInputMethodWindow != win)) {
                                                                                                    setInputMethodWindowLocked(win);
                                                                                                    imMayMove = true;
                                                                                                }
                                                                                                if (win.mAttrs.type == 2011 && isLandScapeMultiWindowMode() && (this.mPolicy instanceof PhoneWindowManager)) {
                                                                                                    ((PhoneWindowManager) this.mPolicy).setFocusChangeIMEFrozenTag(false);
                                                                                                }
                                                                                                if (win.mAttrs.type == 2012 && this.mInputMethodWindow == null && (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer())) {
                                                                                                    Slog.d(TAG, "relayoutwindow TYPE_INPUT_METHOD_DIALOG , do setInputMethodWindowLocked");
                                                                                                    setInputMethodWindowLocked(win);
                                                                                                }
                                                                                                win.adjustStartingWindowFlags();
                                                                                                int i6 = oldVisibility;
                                                                                                boolean z12 = isDefaultDisplay;
                                                                                                Trace.traceEnd(32);
                                                                                                int i7 = attrChanges;
                                                                                                winAnimator = winAnimator3;
                                                                                                origId = origId4;
                                                                                            } catch (Throwable th8) {
                                                                                                e = th8;
                                                                                                Rect rect7 = outFrame;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                resetPriorityAfterLockedSection();
                                                                                                throw e;
                                                                                            }
                                                                                        } else {
                                                                                            int i8 = oldVisibility;
                                                                                            boolean z13 = isDefaultDisplay;
                                                                                            origId = origId4;
                                                                                            win = win3;
                                                                                            Surface surface6 = outSurface;
                                                                                            WindowStateAnimator winAnimator4 = winAnimator3;
                                                                                            try {
                                                                                                Trace.traceBegin(32, "relayoutWindow: viewVisibility_2");
                                                                                                winAnimator = winAnimator4;
                                                                                                winAnimator.mEnterAnimationPending = false;
                                                                                                winAnimator.mEnteringAnimation = false;
                                                                                                if (i3 == 0) {
                                                                                                    try {
                                                                                                        if (winAnimator.hasSurface()) {
                                                                                                            int i9 = attrChanges;
                                                                                                            result2 = result3;
                                                                                                            try {
                                                                                                                Trace.traceBegin(32, "relayoutWindow: getSurface");
                                                                                                                winAnimator.mSurfaceController.getSurface(surface6);
                                                                                                                Trace.traceEnd(32);
                                                                                                                j = 32;
                                                                                                                Trace.traceEnd(j);
                                                                                                                result = result2;
                                                                                                            } catch (Throwable th9) {
                                                                                                                th = th9;
                                                                                                                long j4 = origId;
                                                                                                                int i10 = result2;
                                                                                                            }
                                                                                                        }
                                                                                                    } catch (Throwable th10) {
                                                                                                        e = th10;
                                                                                                        int i11 = result3;
                                                                                                        long j5 = origId;
                                                                                                        Rect rect8 = outFrame;
                                                                                                        while (true) {
                                                                                                            break;
                                                                                                        }
                                                                                                        resetPriorityAfterLockedSection();
                                                                                                        throw e;
                                                                                                    }
                                                                                                }
                                                                                                int i12 = attrChanges;
                                                                                                result2 = result3;
                                                                                            } catch (Throwable th11) {
                                                                                                e = th11;
                                                                                                long j6 = origId;
                                                                                                int i13 = result3;
                                                                                                Rect rect9 = outFrame;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                resetPriorityAfterLockedSection();
                                                                                                throw e;
                                                                                            }
                                                                                            try {
                                                                                                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                                                                    Slog.i(TAG, "Releasing surface in: " + win);
                                                                                                }
                                                                                                try {
                                                                                                    j = 32;
                                                                                                    Trace.traceBegin(32, "wmReleaseOutSurface_" + win.mAttrs.getTitle());
                                                                                                    outSurface.release();
                                                                                                } catch (Throwable th12) {
                                                                                                    e = th12;
                                                                                                    int i14 = result2;
                                                                                                    while (true) {
                                                                                                        break;
                                                                                                    }
                                                                                                    resetPriorityAfterLockedSection();
                                                                                                    throw e;
                                                                                                }
                                                                                                try {
                                                                                                    Trace.traceEnd(32);
                                                                                                    Trace.traceEnd(j);
                                                                                                    result = result2;
                                                                                                } catch (Throwable th13) {
                                                                                                    e = th13;
                                                                                                    Rect rect10 = outFrame;
                                                                                                    long j7 = origId;
                                                                                                    int i15 = result2;
                                                                                                    while (true) {
                                                                                                        break;
                                                                                                    }
                                                                                                    resetPriorityAfterLockedSection();
                                                                                                    throw e;
                                                                                                }
                                                                                            } catch (Throwable th14) {
                                                                                                e = th14;
                                                                                                long j8 = origId;
                                                                                                Rect rect11 = outFrame;
                                                                                                int i16 = result2;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                resetPriorityAfterLockedSection();
                                                                                                throw e;
                                                                                            }
                                                                                        }
                                                                                        this.mHwWMSEx.setHwSecureScreenShot(win);
                                                                                        if (focusMayChange2) {
                                                                                            if (updateFocusedWindowLocked(3, false)) {
                                                                                                imMayMove = false;
                                                                                            }
                                                                                        }
                                                                                        toBeDisplayed = (result & 2) == 0;
                                                                                        DisplayContent dc = win.getDisplayContent();
                                                                                        if (imMayMove) {
                                                                                            dc.computeImeTarget(true);
                                                                                            if (toBeDisplayed) {
                                                                                                dc.assignWindowLayers(false);
                                                                                            }
                                                                                        }
                                                                                        if (wallpaperMayMove2) {
                                                                                            win.getDisplayContent().pendingLayoutChanges |= 4;
                                                                                        }
                                                                                        if (win.mAppToken != null) {
                                                                                            this.mUnknownAppVisibilityController.notifyRelayouted(win.mAppToken);
                                                                                        }
                                                                                        origId2 = origId;
                                                                                        Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                                                                        int displayId3 = displayId;
                                                                                        boolean configChanged = updateOrientationFromAppTokensLocked(displayId3);
                                                                                        Trace.traceEnd(32);
                                                                                        if (shouldRelayout) {
                                                                                            try {
                                                                                                if (win.mFrame.width() == 0 && win.mFrame.height() == 0) {
                                                                                                    Slog.w(TAG, "force to relayout later when size is 1*1 for:" + win);
                                                                                                    this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                                                                }
                                                                                            } catch (Throwable th15) {
                                                                                                th = th15;
                                                                                                Rect rect12 = outFrame;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                resetPriorityAfterLockedSection();
                                                                                                throw e;
                                                                                            }
                                                                                        }
                                                                                        if (toBeDisplayed || !win.mIsWallpaper) {
                                                                                        } else {
                                                                                            DisplayInfo displayInfo = win.getDisplayContent().getDisplayInfo();
                                                                                            boolean z14 = toBeDisplayed;
                                                                                            DisplayInfo displayInfo2 = displayInfo;
                                                                                            dc.mWallpaperController.updateWallpaperOffset(win, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                                                                                        }
                                                                                        if (win.mAppToken != null) {
                                                                                            win.mAppToken.updateReportedVisibilityLocked();
                                                                                        }
                                                                                        if (winAnimator.mReportSurfaceResized) {
                                                                                            winAnimator.mReportSurfaceResized = false;
                                                                                            result |= 32;
                                                                                        }
                                                                                        if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                                                                            result |= 64;
                                                                                        }
                                                                                        if (win.isGoneForLayoutLw() == 0) {
                                                                                            win.mResizedWhileGone = false;
                                                                                        }
                                                                                        if (!shouldRelayout) {
                                                                                            mergedConfiguration2 = mergedConfiguration;
                                                                                            win.getMergedConfiguration(mergedConfiguration2);
                                                                                        } else {
                                                                                            mergedConfiguration2 = mergedConfiguration;
                                                                                            win.getLastReportedMergedConfiguration(mergedConfiguration2);
                                                                                        }
                                                                                        win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                                                        win.updateLastInsetValues();
                                                                                        outFrame.set(win.mCompatFrame);
                                                                                        outOverscanInsets.set(win.mOverscanInsets);
                                                                                        outContentInsets.set(win.mContentInsets);
                                                                                        win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                                                                        outVisibleInsets.set(win.mVisibleInsets);
                                                                                        outStableInsets.set(win.mStableInsets);
                                                                                        outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                                                                        outOutsets.set(win.mOutsets);
                                                                                        outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                                                                        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                                                            try {
                                                                                                Slog.v(TAG, "Relayout of " + win + ": focusMayChange=" + focusMayChange2);
                                                                                            } catch (Throwable th16) {
                                                                                                th = th16;
                                                                                            }
                                                                                        }
                                                                                        int result4 = result | this.mInTouchMode;
                                                                                        this.mInputMonitor.updateInputWindowsLw(true);
                                                                                        if (WindowManagerDebugConfig.DEBUG_LAYOUT || shouldPrintLog) {
                                                                                            Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                                                                        }
                                                                                        win.mInRelayout = false;
                                                                                        displayContent = getDefaultDisplayContentLocked();
                                                                                        if (displayContent != null && i3 == 0) {
                                                                                            displayContent.checkNeedNotifyFingerWinCovered();
                                                                                            displayContent.mObserveToken = null;
                                                                                            displayContent.mTopAboveAppToken = null;
                                                                                        }
                                                                                    }
                                                                                }
                                                                                focusMayChange2 = focusMayChange;
                                                                                this.mAppTransitTrack = "relayout";
                                                                                if (this.mWaitingForConfig) {
                                                                                }
                                                                                this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                                                WindowState win32 = win2;
                                                                                if (!shouldRelayout) {
                                                                                }
                                                                                this.mHwWMSEx.setHwSecureScreenShot(win);
                                                                                if (focusMayChange2) {
                                                                                }
                                                                                if ((result & 2) == 0) {
                                                                                }
                                                                                DisplayContent dc2 = win.getDisplayContent();
                                                                                if (imMayMove) {
                                                                                }
                                                                                if (wallpaperMayMove2) {
                                                                                }
                                                                                if (win.mAppToken != null) {
                                                                                }
                                                                                origId2 = origId;
                                                                                Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                                                                int displayId32 = displayId;
                                                                                boolean configChanged2 = updateOrientationFromAppTokensLocked(displayId32);
                                                                                Trace.traceEnd(32);
                                                                                if (shouldRelayout) {
                                                                                }
                                                                                if (toBeDisplayed) {
                                                                                }
                                                                                if (win.mAppToken != null) {
                                                                                }
                                                                                if (winAnimator.mReportSurfaceResized) {
                                                                                }
                                                                                if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                                                                }
                                                                                if (win.isGoneForLayoutLw() == 0) {
                                                                                }
                                                                                if (!shouldRelayout) {
                                                                                }
                                                                                win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                                                win.updateLastInsetValues();
                                                                                outFrame.set(win.mCompatFrame);
                                                                                outOverscanInsets.set(win.mOverscanInsets);
                                                                                outContentInsets.set(win.mContentInsets);
                                                                                win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                                                                outVisibleInsets.set(win.mVisibleInsets);
                                                                                outStableInsets.set(win.mStableInsets);
                                                                                outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                                                                outOutsets.set(win.mOutsets);
                                                                                outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                                                                if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                                                }
                                                                                int result42 = result | this.mInTouchMode;
                                                                                this.mInputMonitor.updateInputWindowsLw(true);
                                                                                Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                                                                win.mInRelayout = false;
                                                                                displayContent = getDefaultDisplayContentLocked();
                                                                                displayContent.checkNeedNotifyFingerWinCovered();
                                                                                displayContent.mObserveToken = null;
                                                                                displayContent.mTopAboveAppToken = null;
                                                                            }
                                                                        }
                                                                        shouldRelayout = false;
                                                                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                                        }
                                                                        result3 = 0 | 4;
                                                                        if (!win2.mWillReplaceWindow) {
                                                                        }
                                                                        focusMayChange2 = focusMayChange;
                                                                        this.mAppTransitTrack = "relayout";
                                                                        if (this.mWaitingForConfig) {
                                                                        }
                                                                        this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                                        WindowState win322 = win2;
                                                                        if (!shouldRelayout) {
                                                                        }
                                                                        this.mHwWMSEx.setHwSecureScreenShot(win);
                                                                        if (focusMayChange2) {
                                                                        }
                                                                        if ((result & 2) == 0) {
                                                                        }
                                                                        DisplayContent dc22 = win.getDisplayContent();
                                                                        if (imMayMove) {
                                                                        }
                                                                        if (wallpaperMayMove2) {
                                                                        }
                                                                        if (win.mAppToken != null) {
                                                                        }
                                                                        origId2 = origId;
                                                                        Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                                                        int displayId322 = displayId;
                                                                        boolean configChanged22 = updateOrientationFromAppTokensLocked(displayId322);
                                                                        Trace.traceEnd(32);
                                                                        if (shouldRelayout) {
                                                                        }
                                                                        if (toBeDisplayed) {
                                                                        }
                                                                        if (win.mAppToken != null) {
                                                                        }
                                                                        if (winAnimator.mReportSurfaceResized) {
                                                                        }
                                                                        if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                                                        }
                                                                        if (win.isGoneForLayoutLw() == 0) {
                                                                        }
                                                                        if (!shouldRelayout) {
                                                                        }
                                                                        win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                                        win.updateLastInsetValues();
                                                                        outFrame.set(win.mCompatFrame);
                                                                        outOverscanInsets.set(win.mOverscanInsets);
                                                                        outContentInsets.set(win.mContentInsets);
                                                                        win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                                                        outVisibleInsets.set(win.mVisibleInsets);
                                                                        outStableInsets.set(win.mStableInsets);
                                                                        outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                                                        outOutsets.set(win.mOutsets);
                                                                        outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                                                        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                                        }
                                                                        int result422 = result | this.mInTouchMode;
                                                                        this.mInputMonitor.updateInputWindowsLw(true);
                                                                        Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                                                        win.mInRelayout = false;
                                                                        displayContent = getDefaultDisplayContentLocked();
                                                                        displayContent.checkNeedNotifyFingerWinCovered();
                                                                        displayContent.mObserveToken = null;
                                                                        displayContent.mTopAboveAppToken = null;
                                                                    }
                                                                }
                                                                wallpaperMayMove = false;
                                                                boolean wallpaperMayMove32 = wallpaperMayMove | ((flagChanges & DumpState.DUMP_DEXOPT) == 0);
                                                                boolean z92 = becameVisible;
                                                                if ((flagChanges & 8192) != 0) {
                                                                }
                                                                win2.mRelayoutCalled = true;
                                                                win2.mInRelayout = true;
                                                                win2.mViewVisibility = i3;
                                                                if (!WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                                                }
                                                                win2.setDisplayLayoutNeeded();
                                                                win2.mGivenInsetsPending = (flags & 1) == 0;
                                                                if (i3 == 0) {
                                                                }
                                                                shouldRelayout = false;
                                                                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                                }
                                                                result3 = 0 | 4;
                                                                if (!win2.mWillReplaceWindow) {
                                                                }
                                                                focusMayChange2 = focusMayChange;
                                                                this.mAppTransitTrack = "relayout";
                                                                if (this.mWaitingForConfig) {
                                                                }
                                                                this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                                WindowState win3222 = win2;
                                                                if (!shouldRelayout) {
                                                                }
                                                                this.mHwWMSEx.setHwSecureScreenShot(win);
                                                                if (focusMayChange2) {
                                                                }
                                                                if ((result & 2) == 0) {
                                                                }
                                                                DisplayContent dc222 = win.getDisplayContent();
                                                                if (imMayMove) {
                                                                }
                                                                if (wallpaperMayMove2) {
                                                                }
                                                                if (win.mAppToken != null) {
                                                                }
                                                                origId2 = origId;
                                                                Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                                                int displayId3222 = displayId;
                                                                boolean configChanged222 = updateOrientationFromAppTokensLocked(displayId3222);
                                                                Trace.traceEnd(32);
                                                                if (shouldRelayout) {
                                                                }
                                                                if (toBeDisplayed) {
                                                                }
                                                                if (win.mAppToken != null) {
                                                                }
                                                                if (winAnimator.mReportSurfaceResized) {
                                                                }
                                                                if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                                                }
                                                                if (win.isGoneForLayoutLw() == 0) {
                                                                }
                                                                if (!shouldRelayout) {
                                                                }
                                                                win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                                win.updateLastInsetValues();
                                                                outFrame.set(win.mCompatFrame);
                                                                outOverscanInsets.set(win.mOverscanInsets);
                                                                outContentInsets.set(win.mContentInsets);
                                                                win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                                                outVisibleInsets.set(win.mVisibleInsets);
                                                                outStableInsets.set(win.mStableInsets);
                                                                outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                                                outOutsets.set(win.mOutsets);
                                                                outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                                                if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                                }
                                                                int result4222 = result | this.mInTouchMode;
                                                                this.mInputMonitor.updateInputWindowsLw(true);
                                                                Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                                                win.mInRelayout = false;
                                                                displayContent = getDefaultDisplayContentLocked();
                                                                displayContent.checkNeedNotifyFingerWinCovered();
                                                                displayContent.mObserveToken = null;
                                                                displayContent.mTopAboveAppToken = null;
                                                            }
                                                        } catch (Throwable th17) {
                                                            e = th17;
                                                            Rect rect13 = outFrame;
                                                        }
                                                    }
                                                    focusMayChange = false;
                                                    if (win2.mViewVisibility != i3) {
                                                    }
                                                    wallpaperMayMove = false;
                                                    boolean wallpaperMayMove322 = wallpaperMayMove | ((flagChanges & DumpState.DUMP_DEXOPT) == 0);
                                                    boolean z922 = becameVisible;
                                                    if ((flagChanges & 8192) != 0) {
                                                    }
                                                    win2.mRelayoutCalled = true;
                                                    win2.mInRelayout = true;
                                                    win2.mViewVisibility = i3;
                                                    if (!WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                                    }
                                                    win2.setDisplayLayoutNeeded();
                                                    win2.mGivenInsetsPending = (flags & 1) == 0;
                                                    if (i3 == 0) {
                                                    }
                                                    shouldRelayout = false;
                                                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                                    }
                                                    result3 = 0 | 4;
                                                    if (!win2.mWillReplaceWindow) {
                                                    }
                                                    focusMayChange2 = focusMayChange;
                                                    this.mAppTransitTrack = "relayout";
                                                    if (this.mWaitingForConfig) {
                                                    }
                                                    this.mWindowPlacerLocked.performSurfacePlacement(true);
                                                    WindowState win32222 = win2;
                                                    if (!shouldRelayout) {
                                                    }
                                                    this.mHwWMSEx.setHwSecureScreenShot(win);
                                                    if (focusMayChange2) {
                                                    }
                                                    if ((result & 2) == 0) {
                                                    }
                                                    DisplayContent dc2222 = win.getDisplayContent();
                                                    if (imMayMove) {
                                                    }
                                                    if (wallpaperMayMove2) {
                                                    }
                                                    if (win.mAppToken != null) {
                                                    }
                                                    origId2 = origId;
                                                    Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                                    int displayId32222 = displayId;
                                                    boolean configChanged2222 = updateOrientationFromAppTokensLocked(displayId32222);
                                                    Trace.traceEnd(32);
                                                    if (shouldRelayout) {
                                                    }
                                                    if (toBeDisplayed) {
                                                    }
                                                    if (win.mAppToken != null) {
                                                    }
                                                    if (winAnimator.mReportSurfaceResized) {
                                                    }
                                                    if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                                    }
                                                    if (win.isGoneForLayoutLw() == 0) {
                                                    }
                                                    if (!shouldRelayout) {
                                                    }
                                                    win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                    win.updateLastInsetValues();
                                                    outFrame.set(win.mCompatFrame);
                                                    outOverscanInsets.set(win.mOverscanInsets);
                                                    outContentInsets.set(win.mContentInsets);
                                                    win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                                    outVisibleInsets.set(win.mVisibleInsets);
                                                    outStableInsets.set(win.mStableInsets);
                                                    outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                                    outOutsets.set(win.mOutsets);
                                                    outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                                    if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                    }
                                                    int result42222 = result | this.mInTouchMode;
                                                    this.mInputMonitor.updateInputWindowsLw(true);
                                                    Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                                    win.mInRelayout = false;
                                                    displayContent = getDefaultDisplayContentLocked();
                                                    displayContent.checkNeedNotifyFingerWinCovered();
                                                    displayContent.mObserveToken = null;
                                                    displayContent.mTopAboveAppToken = null;
                                                }
                                            }
                                            imMayMove = true;
                                            isDefaultDisplay = win2.isDefaultDisplay();
                                            if (isDefaultDisplay) {
                                            }
                                            focusMayChange = false;
                                            if (win2.mViewVisibility != i3) {
                                            }
                                            wallpaperMayMove = false;
                                            boolean wallpaperMayMove3222 = wallpaperMayMove | ((flagChanges & DumpState.DUMP_DEXOPT) == 0);
                                            boolean z9222 = becameVisible;
                                            if ((flagChanges & 8192) != 0) {
                                            }
                                            win2.mRelayoutCalled = true;
                                            win2.mInRelayout = true;
                                            win2.mViewVisibility = i3;
                                            if (!WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                            }
                                            win2.setDisplayLayoutNeeded();
                                            win2.mGivenInsetsPending = (flags & 1) == 0;
                                            if (i3 == 0) {
                                            }
                                            shouldRelayout = false;
                                            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                            }
                                            result3 = 0 | 4;
                                            if (!win2.mWillReplaceWindow) {
                                            }
                                            focusMayChange2 = focusMayChange;
                                            this.mAppTransitTrack = "relayout";
                                            if (this.mWaitingForConfig) {
                                            }
                                            this.mWindowPlacerLocked.performSurfacePlacement(true);
                                            WindowState win322222 = win2;
                                            if (!shouldRelayout) {
                                            }
                                            this.mHwWMSEx.setHwSecureScreenShot(win);
                                            if (focusMayChange2) {
                                            }
                                            if ((result & 2) == 0) {
                                            }
                                            DisplayContent dc22222 = win.getDisplayContent();
                                            if (imMayMove) {
                                            }
                                            if (wallpaperMayMove2) {
                                            }
                                            if (win.mAppToken != null) {
                                            }
                                            origId2 = origId;
                                            Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                            int displayId322222 = displayId;
                                            boolean configChanged22222 = updateOrientationFromAppTokensLocked(displayId322222);
                                            Trace.traceEnd(32);
                                            if (shouldRelayout) {
                                            }
                                            if (toBeDisplayed) {
                                            }
                                            try {
                                                if (win.mAppToken != null) {
                                                }
                                                if (winAnimator.mReportSurfaceResized) {
                                                }
                                                if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                                }
                                                if (win.isGoneForLayoutLw() == 0) {
                                                }
                                                if (!shouldRelayout) {
                                                }
                                                win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                                win.updateLastInsetValues();
                                                outFrame.set(win.mCompatFrame);
                                                outOverscanInsets.set(win.mOverscanInsets);
                                                outContentInsets.set(win.mContentInsets);
                                                win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                                outVisibleInsets.set(win.mVisibleInsets);
                                                outStableInsets.set(win.mStableInsets);
                                                outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                                outOutsets.set(win.mOutsets);
                                                outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                                if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                                }
                                            } catch (Throwable th18) {
                                                th = th18;
                                                Rect rect14 = outFrame;
                                                while (true) {
                                                    break;
                                                }
                                                resetPriorityAfterLockedSection();
                                                throw e;
                                            }
                                            try {
                                                int result422222 = result | this.mInTouchMode;
                                                this.mInputMonitor.updateInputWindowsLw(true);
                                                Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                                win.mInRelayout = false;
                                                displayContent = getDefaultDisplayContentLocked();
                                                displayContent.checkNeedNotifyFingerWinCovered();
                                                displayContent.mObserveToken = null;
                                                displayContent.mTopAboveAppToken = null;
                                            } catch (Throwable th19) {
                                                th = th19;
                                                while (true) {
                                                    break;
                                                }
                                                resetPriorityAfterLockedSection();
                                                throw e;
                                            }
                                        }
                                    }
                                    StringBuilder sb2 = new StringBuilder();
                                    boolean z15 = hasStatusBarServicePermission;
                                    sb2.append("start Relayout ");
                                    sb2.append(win2);
                                    sb2.append(" oldVis=");
                                    sb2.append(oldVisibility);
                                    sb2.append(" newVis=");
                                    sb2.append(i3);
                                    sb2.append(" width=");
                                    sb2.append(i);
                                    sb2.append(" height=");
                                    sb2.append(i2);
                                    Flog.i(307, sb2.toString());
                                    shouldPrintLog = true;
                                    if (oldVisibility != 4) {
                                    }
                                    if ((flagChanges & 131080) == 0) {
                                    }
                                    imMayMove = true;
                                    isDefaultDisplay = win2.isDefaultDisplay();
                                    if (isDefaultDisplay) {
                                    }
                                    focusMayChange = false;
                                    if (win2.mViewVisibility != i3) {
                                    }
                                    wallpaperMayMove = false;
                                    boolean wallpaperMayMove32222 = wallpaperMayMove | ((flagChanges & DumpState.DUMP_DEXOPT) == 0);
                                    boolean z92222 = becameVisible;
                                    if ((flagChanges & 8192) != 0) {
                                    }
                                    win2.mRelayoutCalled = true;
                                    win2.mInRelayout = true;
                                    win2.mViewVisibility = i3;
                                    if (!WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                    }
                                    win2.setDisplayLayoutNeeded();
                                    win2.mGivenInsetsPending = (flags & 1) == 0;
                                    if (i3 == 0) {
                                    }
                                    shouldRelayout = false;
                                    if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                    }
                                    result3 = 0 | 4;
                                    if (!win2.mWillReplaceWindow) {
                                    }
                                    focusMayChange2 = focusMayChange;
                                    this.mAppTransitTrack = "relayout";
                                    if (this.mWaitingForConfig) {
                                    }
                                    this.mWindowPlacerLocked.performSurfacePlacement(true);
                                    WindowState win3222222 = win2;
                                    if (!shouldRelayout) {
                                    }
                                    try {
                                        this.mHwWMSEx.setHwSecureScreenShot(win);
                                        if (focusMayChange2) {
                                        }
                                        if ((result & 2) == 0) {
                                        }
                                        DisplayContent dc222222 = win.getDisplayContent();
                                        if (imMayMove) {
                                        }
                                        if (wallpaperMayMove2) {
                                        }
                                        if (win.mAppToken != null) {
                                        }
                                        origId2 = origId;
                                        Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                                        int displayId3222222 = displayId;
                                        boolean configChanged222222 = updateOrientationFromAppTokensLocked(displayId3222222);
                                        Trace.traceEnd(32);
                                        if (shouldRelayout) {
                                        }
                                        if (toBeDisplayed) {
                                        }
                                        if (win.mAppToken != null) {
                                        }
                                        if (winAnimator.mReportSurfaceResized) {
                                        }
                                        if (this.mPolicy.isNavBarForcedShownLw(win)) {
                                        }
                                        if (win.isGoneForLayoutLw() == 0) {
                                        }
                                        if (!shouldRelayout) {
                                        }
                                        win.setLastReportedMergedConfiguration(mergedConfiguration2);
                                        win.updateLastInsetValues();
                                        outFrame.set(win.mCompatFrame);
                                        outOverscanInsets.set(win.mOverscanInsets);
                                        outContentInsets.set(win.mContentInsets);
                                        win.mLastRelayoutContentInsets.set(win.mContentInsets);
                                        outVisibleInsets.set(win.mVisibleInsets);
                                        outStableInsets.set(win.mStableInsets);
                                        outCutout.set(win.mDisplayCutout.getDisplayCutout());
                                        outOutsets.set(win.mOutsets);
                                        outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                                        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                                        }
                                        int result4222222 = result | this.mInTouchMode;
                                        this.mInputMonitor.updateInputWindowsLw(true);
                                        Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                                        win.mInRelayout = false;
                                        displayContent = getDefaultDisplayContentLocked();
                                        displayContent.checkNeedNotifyFingerWinCovered();
                                        displayContent.mObserveToken = null;
                                        displayContent.mTopAboveAppToken = null;
                                    } catch (Throwable th20) {
                                        e = th20;
                                        Rect rect15 = outFrame;
                                        long j9 = origId;
                                        while (true) {
                                            break;
                                        }
                                        resetPriorityAfterLockedSection();
                                        throw e;
                                    }
                                }
                            }
                            z = false;
                            win2.mEnforceSizeCompat = z;
                            if ((attrChanges & 128) != 0) {
                            }
                            win2.setWindowScale(win2.mRequestedWidth, win2.mRequestedHeight);
                            if (win2.mAttrs.surfaceInsets.left == 0) {
                            }
                            winAnimator3.setOpaqueLocked(false);
                            oldVisibility = win2.mViewVisibility;
                            boolean shouldPrintLog2 = false;
                            if (!WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            }
                            StringBuilder sb22 = new StringBuilder();
                            boolean z152 = hasStatusBarServicePermission;
                            sb22.append("start Relayout ");
                            sb22.append(win2);
                            sb22.append(" oldVis=");
                            sb22.append(oldVisibility);
                            sb22.append(" newVis=");
                            sb22.append(i3);
                            sb22.append(" width=");
                            sb22.append(i);
                            sb22.append(" height=");
                            sb22.append(i2);
                            Flog.i(307, sb22.toString());
                            shouldPrintLog2 = true;
                            if (oldVisibility != 4) {
                            }
                            if ((flagChanges & 131080) == 0) {
                            }
                            imMayMove = true;
                            isDefaultDisplay = win2.isDefaultDisplay();
                            if (isDefaultDisplay) {
                            }
                            focusMayChange = false;
                            try {
                                if (win2.mViewVisibility != i3) {
                                }
                                wallpaperMayMove = false;
                                boolean wallpaperMayMove322222 = wallpaperMayMove | ((flagChanges & DumpState.DUMP_DEXOPT) == 0);
                                boolean z922222 = becameVisible;
                                if ((flagChanges & 8192) != 0) {
                                }
                                win2.mRelayoutCalled = true;
                                win2.mInRelayout = true;
                                win2.mViewVisibility = i3;
                                if (!WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                                }
                                win2.setDisplayLayoutNeeded();
                                win2.mGivenInsetsPending = (flags & 1) == 0;
                                if (i3 == 0) {
                                }
                                shouldRelayout = false;
                                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                                }
                                result3 = 0 | 4;
                                if (!win2.mWillReplaceWindow) {
                                }
                                focusMayChange2 = focusMayChange;
                            } catch (Throwable th21) {
                                e = th21;
                                Rect rect16 = outFrame;
                                long j10 = origId4;
                                Surface surface7 = outSurface;
                                while (true) {
                                    break;
                                }
                                resetPriorityAfterLockedSection();
                                throw e;
                            }
                        } catch (Throwable th22) {
                            e = th22;
                            Rect rect17 = outFrame;
                            boolean z16 = hasStatusBarServicePermission;
                            long j11 = origId4;
                            Surface surface8 = outSurface;
                            while (true) {
                                break;
                            }
                            resetPriorityAfterLockedSection();
                            throw e;
                        }
                        try {
                            this.mAppTransitTrack = "relayout";
                            if (this.mWaitingForConfig) {
                            }
                            this.mWindowPlacerLocked.performSurfacePlacement(true);
                            WindowState win32222222 = win2;
                            if (!shouldRelayout) {
                            }
                            this.mHwWMSEx.setHwSecureScreenShot(win);
                            if (focusMayChange2) {
                            }
                            if ((result & 2) == 0) {
                            }
                            DisplayContent dc2222222 = win.getDisplayContent();
                            if (imMayMove) {
                            }
                            if (wallpaperMayMove2) {
                            }
                            if (win.mAppToken != null) {
                            }
                            origId2 = origId;
                            Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                            int displayId32222222 = displayId;
                            boolean configChanged2222222 = updateOrientationFromAppTokensLocked(displayId32222222);
                            Trace.traceEnd(32);
                            if (shouldRelayout) {
                            }
                            if (toBeDisplayed) {
                            }
                            if (win.mAppToken != null) {
                            }
                            if (winAnimator.mReportSurfaceResized) {
                            }
                            if (this.mPolicy.isNavBarForcedShownLw(win)) {
                            }
                            if (win.isGoneForLayoutLw() == 0) {
                            }
                            if (!shouldRelayout) {
                            }
                            win.setLastReportedMergedConfiguration(mergedConfiguration2);
                            win.updateLastInsetValues();
                            outFrame.set(win.mCompatFrame);
                            outOverscanInsets.set(win.mOverscanInsets);
                            outContentInsets.set(win.mContentInsets);
                            win.mLastRelayoutContentInsets.set(win.mContentInsets);
                            outVisibleInsets.set(win.mVisibleInsets);
                            outStableInsets.set(win.mStableInsets);
                            outCutout.set(win.mDisplayCutout.getDisplayCutout());
                            outOutsets.set(win.mOutsets);
                            outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                            }
                            int result42222222 = result | this.mInTouchMode;
                            this.mInputMonitor.updateInputWindowsLw(true);
                            Flog.i(307, "complete Relayout " + win + "  size:" + outFrame.toShortString());
                            win.mInRelayout = false;
                            displayContent = getDefaultDisplayContentLocked();
                            displayContent.checkNeedNotifyFingerWinCovered();
                            displayContent.mObserveToken = null;
                            displayContent.mTopAboveAppToken = null;
                        } catch (Throwable th23) {
                            e = th23;
                            Rect rect18 = outFrame;
                            int i17 = result3;
                            long j12 = origId4;
                            Surface surface9 = outSurface;
                            while (true) {
                                break;
                            }
                            resetPriorityAfterLockedSection();
                            throw e;
                        }
                    }
                } catch (Throwable th24) {
                    e = th24;
                    Rect rect19 = outFrame;
                    Surface surface10 = surface;
                    boolean z17 = hasStatusBarPermission;
                    boolean z18 = hasStatusBarServicePermission;
                    long j13 = origId4;
                    while (true) {
                        break;
                    }
                    resetPriorityAfterLockedSection();
                    throw e;
                }
            } catch (Throwable th25) {
                e = th25;
                Rect rect20 = outFrame;
                long j14 = origId3;
                boolean z19 = hasStatusBarPermission;
                boolean z20 = hasStatusBarServicePermission;
                Surface surface11 = surface;
                while (true) {
                    break;
                }
                resetPriorityAfterLockedSection();
                throw e;
            }
        }
    }

    private boolean tryStartExitingAnimation(WindowState win, WindowStateAnimator winAnimator, boolean isDefaultDisplay, boolean focusMayChange) {
        int transit = 2;
        if (win.mAttrs.type == 3) {
            transit = 5;
        }
        if (win.isWinVisibleLw() && !shouldHideIMExitAnim(win) && winAnimator.applyAnimationLocked(transit, false)) {
            focusMayChange = isDefaultDisplay;
            win.mAnimatingExit = true;
        } else if (win.mWinAnimator.isAnimationSet() && !shouldHideIMExitAnim(win)) {
            win.mAnimatingExit = true;
        } else if (win.getDisplayContent().mWallpaperController.isWallpaperTarget(win)) {
            win.mAnimatingExit = true;
        } else {
            if (this.mInputMethodWindow == win) {
                setInputMethodWindowLocked(null);
            }
            boolean stopped = win.mAppToken != null ? win.mAppToken.mAppStopped : true;
            win.mDestroying = true;
            win.destroySurface(false, stopped);
        }
        if (this.mAccessibilityController != null && (win.getDisplayId() == 0 || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad()))) {
            this.mAccessibilityController.onWindowTransitionLocked(win, transit);
        }
        SurfaceControl.openTransaction();
        winAnimator.detachChildren();
        SurfaceControl.closeTransaction();
        return focusMayChange;
    }

    private int createSurfaceControl(Surface outSurface, int result, WindowState win, WindowStateAnimator winAnimator) {
        if (!win.mHasSurface) {
            result |= 4;
        }
        try {
            Trace.traceBegin(32, "createSurfaceControl");
            WindowSurfaceController surfaceController = winAnimator.createSurfaceLocked(win.mAttrs.type, win.mOwnerUid);
            if (surfaceController != null) {
                surfaceController.getSurface(outSurface);
            } else {
                Slog.w(TAG, "Failed to create surface control for " + win);
                outSurface.release();
            }
            return result;
        } finally {
            Trace.traceEnd(32);
        }
    }

    public boolean outOfMemoryWindow(Session session, IWindow client) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                boolean reclaimSomeSurfaceMemory = this.mRoot.reclaimSomeSurfaceMemory(win.mWinAnimator, "from-client", false);
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return reclaimSomeSurfaceMemory;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void finishDrawingWindow(Session session, IWindow client) {
        Flog.i(307, "finishDrawingWindow...");
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win != null && win.mWinAnimator.finishDrawingLocked()) {
                    if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                        win.getDisplayContent().pendingLayoutChanges |= 4;
                    }
                    win.setDisplayLayoutNeeded();
                    this.mWindowPlacerLocked.requestTraversal();
                    this.mHwWMSEx.updateWindowReport(win, win.mRequestedWidth, win.mRequestedHeight);
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0063, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0066, code lost:
        return;
     */
    public void addWindowToken(IBinder binder, int type, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                    WindowToken token = dc.getWindowToken(binder);
                    if (token != null) {
                        Slog.w(TAG, "addWindowToken: Attempted to add binder token: " + binder + " for already created window token: " + token + " displayId=" + displayId);
                        resetPriorityAfterLockedSection();
                    } else if (type == 2013) {
                        new WallpaperWindowToken(this, binder, true, dc, true);
                    } else {
                        new WindowToken(this, binder, type, true, dc, true);
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTokenFound(IBinder binder, DisplayContent dc) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setFocusedDisplay(int displayId, boolean findTopTask, String reason) {
    }

    public void removeWindowToken(IBinder binder, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeWindowToken()")) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                    if (dc == null) {
                        Slog.w(TAG, "removeWindowToken: Attempted to remove token: " + binder + " for non-exiting displayId=" + displayId);
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    if (dc.removeWindowToken(binder) == null) {
                        if (!isTokenFound(binder, dc)) {
                            Slog.w(TAG, "removeWindowToken: Attempted to remove non-existing token: " + binder);
                            resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                    }
                    this.mInputMonitor.updateInputWindowsLw(true);
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded, int displayId) {
        return updateOrientationFromAppTokens(currentConfig, freezeThisOneIfNeeded, displayId, false);
    }

    public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded, int displayId, boolean forceUpdate) {
        Configuration config;
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "updateOrientationFromAppTokens()")) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    config = updateOrientationFromAppTokensLocked(currentConfig, freezeThisOneIfNeeded, displayId, forceUpdate);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
                return config;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    private Configuration updateOrientationFromAppTokensLocked(Configuration currentConfig, IBinder freezeThisOneIfNeeded, int displayId, boolean forceUpdate) {
        if (!this.mDisplayReady) {
            Flog.i(308, "display is not ready");
            return null;
        }
        Configuration config = null;
        if (updateOrientationFromAppTokensLocked(displayId, forceUpdate)) {
            if (freezeThisOneIfNeeded != null && !this.mRoot.mOrientationChangeComplete) {
                AppWindowToken atoken = this.mRoot.getAppWindowToken(freezeThisOneIfNeeded);
                if (atoken != null) {
                    atoken.startFreezingScreen();
                }
            }
            config = computeNewConfigurationLocked(displayId);
        } else if (currentConfig != null) {
            this.mTempConfiguration.unset();
            this.mTempConfiguration.updateFrom(currentConfig);
            DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
            displayContent.computeScreenConfiguration(this.mTempConfiguration);
            if (currentConfig.diff(this.mTempConfiguration) != 0) {
                this.mWaitingForConfig = true;
                displayContent.setLayoutNeeded();
                int[] anim = new int[2];
                this.mPolicy.selectRotationAnimationLw(anim);
                if (!this.mHwWMSEx.getIgnoreFrozen()) {
                    startFreezingDisplayLocked(anim[0], anim[1], displayContent);
                }
                config = new Configuration(this.mTempConfiguration);
            }
        }
        if (this.mHwWMSEx.getIgnoreFrozen()) {
            this.mHwWMSEx.setIgnoreFrozen(false);
        }
        return config;
    }

    /* access modifiers changed from: package-private */
    public boolean updateOrientationFromAppTokensLocked(int displayId) {
        return updateOrientationFromAppTokensLocked(displayId, false);
    }

    /* access modifiers changed from: package-private */
    public boolean updateOrientationFromAppTokensLocked(int displayId, boolean forceUpdate) {
        long ident = Binder.clearCallingIdentity();
        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
        if (dc == null) {
            Binder.restoreCallingIdentity(ident);
            return false;
        } else if (!this.mVrMananger.isVRDeviceConnected() || !this.mVrMananger.isValidVRDisplayId(displayId)) {
            try {
                int req = dc.getOrientation();
                if (req == dc.getLastOrientation() && !forceUpdate) {
                    return false;
                }
                startIntelliServiceFR(req);
                dc.setLastOrientation(req);
                if (dc.isDefaultDisplay) {
                    this.mPolicy.setCurrentOrientationLw(req);
                }
                boolean updateRotationUnchecked = dc.updateRotationUnchecked(forceUpdate);
                Binder.restoreCallingIdentity(ident);
                return updateRotationUnchecked;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Binder.restoreCallingIdentity(ident);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean rotationNeedsUpdateLocked() {
        DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
        int lastOrientation = defaultDisplayContent.getLastOrientation();
        int oldRotation = defaultDisplayContent.getRotation();
        boolean oldAltOrientation = defaultDisplayContent.getAltOrientation();
        int rotation = this.mPolicy.rotationForOrientationLw(lastOrientation, oldRotation, true);
        boolean altOrientation = !this.mPolicy.rotationHasCompatibleMetricsLw(lastOrientation, rotation);
        if (oldRotation == rotation && oldAltOrientation == altOrientation) {
            return false;
        }
        return true;
    }

    public int[] setNewDisplayOverrideConfiguration(Configuration overrideConfig, int displayId) {
        int[] displayOverrideConfigurationIfNeeded;
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewDisplayOverrideConfiguration()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mHwWMSEx.handleNewDisplayConfiguration(overrideConfig, displayId);
                    if (this.mWaitingForConfig) {
                        this.mWaitingForConfig = false;
                        this.mLastFinishedFreezeSource = "new-config";
                    }
                    displayOverrideConfigurationIfNeeded = this.mRoot.setDisplayOverrideConfigurationIfNeeded(overrideConfig, displayId);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return displayOverrideConfigurationIfNeeded;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    /* access modifiers changed from: package-private */
    public void setFocusTaskRegionLocked(AppWindowToken previousFocus) {
        Task focusedTask = this.mFocusedApp != null ? this.mFocusedApp.getTask() : null;
        Task previousTask = previousFocus != null ? previousFocus.getTask() : null;
        DisplayContent focusedDisplayContent = focusedTask != null ? focusedTask.getDisplayContent() : null;
        DisplayContent previousDisplayContent = previousTask != null ? previousTask.getDisplayContent() : null;
        if (HwPCUtils.isPcCastModeInServer()) {
            int count = this.mRoot.mChildren.size();
            for (int j = 0; j < count; j++) {
                DisplayContent dc = (DisplayContent) this.mRoot.mChildren.get(j);
                if (focusedDisplayContent == null || focusedDisplayContent.getDisplayId() != dc.getDisplayId()) {
                    dc.setTouchExcludeRegion(null);
                } else {
                    dc.setTouchExcludeRegion(focusedTask);
                }
            }
            return;
        }
        if (!(previousDisplayContent == null || previousDisplayContent == focusedDisplayContent)) {
            previousDisplayContent.setTouchExcludeRegion(null);
        }
        if (focusedDisplayContent != null) {
            focusedDisplayContent.setTouchExcludeRegion(focusedTask);
        }
    }

    public void setFocusedApp(IBinder token, boolean moveFocusNow) {
        AppWindowToken newFocus;
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setFocusedApp()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (token == null) {
                        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                            Slog.v(TAG, "Clearing focused app, was " + this.mFocusedApp);
                        }
                        newFocus = null;
                    } else {
                        newFocus = this.mRoot.getAppWindowToken(token);
                        if (newFocus == null) {
                            Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                        }
                        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                            Slog.v(TAG, "Set focused app to: " + newFocus + " old focus=" + this.mFocusedApp + " moveFocusNow=" + moveFocusNow);
                        }
                    }
                    this.mHwWMSEx.checkSingleHandMode(this.mFocusedApp, newFocus);
                    boolean changed = this.mFocusedApp != newFocus;
                    if (changed) {
                        AppWindowToken prev = this.mFocusedApp;
                        this.mFocusedApp = newFocus;
                        if (HwPCUtils.isPcCastModeInServer() && this.mFocusedApp != null && !this.mFocusedApp.getDisplayContent().isDefaultDisplay) {
                            setPCLauncherFocused(false);
                        }
                        this.mInputMonitor.setFocusedAppLw(newFocus);
                        setFocusTaskRegionLocked(prev);
                    }
                    if (moveFocusNow && changed) {
                        long origId = Binder.clearCallingIdentity();
                        updateFocusedWindowLocked(0, true);
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
        prepareAppTransition(transit, alwaysKeepCurrent, 0, false);
    }

    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent, int flags, boolean forceOverride) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "prepareAppTransition()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    boolean prepared = this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent, flags, forceOverride);
                    DisplayContent dc = this.mRoot.getDisplayContent(0);
                    if (prepared && dc != null && dc.okToAnimate()) {
                        this.mSkipAppTransitionAnimation = false;
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public int getPendingAppTransition() {
        return this.mAppTransition.getAppTransition();
    }

    public void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransition(packageName, enterAnim, exitAnim, startedCallback);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionScaleUp(startX, startY, startWidth, startHeight);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionClipReveal(startX, startY, startWidth, startHeight);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionThumb(GraphicBuffer srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionThumb(srcThumb, startX, startY, startedCallback, scaleUp);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionAspectScaledThumb(GraphicBuffer srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionAspectScaledThumb(srcThumb, startX, startY, targetWidth, targetHeight, startedCallback, scaleUp);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionMultiThumb(specs, onAnimationStartedCallback, onAnimationFinishedCallback, scaleUp);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionStartCrossProfileApps() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionStartCrossProfileApps();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionInPlace(String packageName, int anim) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overrideInPlaceAppTransition(packageName, anim);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, callback, scaleUp);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void overridePendingAppTransitionRemote(RemoteAnimationAdapter remoteAnimationAdapter) {
        if (checkCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "overridePendingAppTransitionRemote()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mAppTransition.overridePendingAppTransitionRemote(remoteAnimationAdapter);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS permission");
    }

    public void endProlongedAnimations() {
    }

    public void executeAppTransition() {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "executeAppTransition()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                        Flog.i(307, "Execute app transition: " + this.mAppTransition + " Callers=" + Debug.getCallers(5));
                    }
                    if (this.mAppTransition.isTransitionSet()) {
                        this.mAppTransition.setReady();
                        this.mWindowPlacerLocked.requestTraversal();
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void initializeRecentsAnimation(int targetActivityType, IRecentsAnimationRunner recentsAnimationRunner, RecentsAnimationController.RecentsAnimationCallbacks callbacks, int displayId, SparseBooleanArray recentTaskIds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRecentsAnimationController = new RecentsAnimationController(this, recentsAnimationRunner, callbacks, displayId);
                this.mAppTransition.updateBooster();
                this.mRecentsAnimationController.initialize(targetActivityType, recentTaskIds);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public RecentsAnimationController getRecentsAnimationController() {
        return this.mRecentsAnimationController;
    }

    public boolean canStartRecentsAnimation() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mAppTransition.isTransitionSet()) {
                    resetPriorityAfterLockedSection();
                    return false;
                }
                resetPriorityAfterLockedSection();
                return true;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void cancelRecentsAnimationSynchronously(@RecentsAnimationController.ReorderMode int reorderMode, String reason) {
        if (this.mRecentsAnimationController != null) {
            this.mRecentsAnimationController.cancelAnimationSynchronously(reorderMode, reason);
        }
    }

    public void cleanupRecentsAnimation(@RecentsAnimationController.ReorderMode int reorderMode) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mRecentsAnimationController != null) {
                    this.mRecentsAnimationController.cleanupAnimation(reorderMode);
                    this.mRecentsAnimationController = null;
                    this.mAppTransition.updateBooster();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setAppFullscreen(IBinder token, boolean toOpaque) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken atoken = this.mRoot.getAppWindowToken(token);
                if (atoken != null) {
                    atoken.setFillsParent(toOpaque);
                    setWindowOpaqueLocked(token, toOpaque);
                    this.mWindowPlacerLocked.requestTraversal();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setWindowOpaque(IBinder token, boolean isOpaque) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                setWindowOpaqueLocked(token, isOpaque);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    private void setWindowOpaqueLocked(IBinder token, boolean isOpaque) {
        AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
        if (wtoken != null) {
            WindowState win = wtoken.findMainWindow();
            if (win == null) {
                return;
            }
            if (!win.inFreeformWindowingMode()) {
                win.mWinAnimator.setOpaqueLocked(isOpaque);
            } else {
                win.mWinAnimator.setOpaqueLocked(false);
            }
        }
    }

    public void setDockedStackCreateState(int mode, Rect bounds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                setDockedStackCreateStateLocked(mode, bounds);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void setDockedStackCreateStateLocked(int mode, Rect bounds) {
        this.mDockedStackCreateMode = mode;
        this.mDockedStackCreateBounds = bounds;
    }

    public void checkSplitScreenMinimizedChanged(boolean animate) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().checkMinimizeChanged(animate);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean isValidPictureInPictureAspectRatio(int displayId, float aspectRatio) {
        return this.mRoot.getDisplayContent(displayId).getPinnedStackController().isValidPictureInPictureAspectRatio(aspectRatio);
    }

    public void getStackBounds(int windowingMode, int activityType, Rect bounds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                TaskStack stack = this.mRoot.getStack(windowingMode, activityType);
                if (stack != null) {
                    stack.getBounds(bounds);
                    resetPriorityAfterLockedSection();
                    return;
                }
                bounds.setEmpty();
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void notifyShowingDreamChanged() {
        notifyKeyguardFlagsChanged(null);
    }

    public WindowManagerPolicy.WindowState getInputMethodWindowLw() {
        return this.mInputMethodWindow;
    }

    public void notifyKeyguardTrustedChanged() {
        this.mH.sendEmptyMessage(57);
    }

    public void screenTurningOff(WindowManagerPolicy.ScreenOffListener listener) {
        this.mTaskSnapshotController.screenTurningOff(listener);
    }

    public void triggerAnimationFailsafe() {
        this.mH.sendEmptyMessage(60);
    }

    public void onKeyguardShowingAndNotOccludedChanged() {
        this.mH.sendEmptyMessage(61);
    }

    public void deferSurfaceLayout() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.deferLayout();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void continueSurfaceLayout() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.continueLayout();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean containsShowWhenLockedWindow(IBinder token) {
        boolean z;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
                z = wtoken != null && wtoken.containsShowWhenLockedWindow();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return z;
    }

    public boolean containsDismissKeyguardWindow(IBinder token) {
        boolean z;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
                z = wtoken != null && wtoken.containsDismissKeyguardWindow();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return z;
    }

    /* access modifiers changed from: package-private */
    public void notifyKeyguardFlagsChanged(Runnable callback) {
        Runnable wrappedCallback;
        if (callback != null) {
            wrappedCallback = new Runnable(callback) {
                private final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WindowManagerService.lambda$notifyKeyguardFlagsChanged$1(WindowManagerService.this, this.f$1);
                }
            };
        } else {
            wrappedCallback = null;
        }
        this.mH.obtainMessage(56, wrappedCallback).sendToTarget();
    }

    public static /* synthetic */ void lambda$notifyKeyguardFlagsChanged$1(WindowManagerService windowManagerService, Runnable callback) {
        synchronized (windowManagerService.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                callback.run();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean isKeyguardTrusted() {
        boolean isKeyguardTrustedLw;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                isKeyguardTrustedLw = this.mPolicy.isKeyguardTrustedLw();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return isKeyguardTrustedLw;
    }

    public boolean isKeyguardOccluded() {
        boolean isKeyguardOccluded;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                isKeyguardOccluded = this.mPolicy.isKeyguardOccluded();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return isKeyguardOccluded;
    }

    public void setKeyguardGoingAway(boolean keyguardGoingAway) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mKeyguardGoingAway = keyguardGoingAway;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setKeyguardOrAodShowingOnDefaultDisplay(boolean showing) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mKeyguardOrAodShowingOnDefaultDisplay = showing;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void startFreezingScreen(int exitAnim, int enterAnim) {
        long origId;
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (!this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = true;
                        origId = Binder.clearCallingIdentity();
                        startFreezingDisplayLocked(exitAnim, enterAnim);
                        this.mH.removeMessages(30);
                        this.mH.sendEmptyMessageDelayed(30, 5000);
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void stopFreezingScreen() {
        long origId;
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "stopFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = false;
                        this.mLastFinishedFreezeSource = "client";
                        origId = Binder.clearCallingIdentity();
                        stopFreezingDisplayLocked();
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void disableKeyguard(IBinder token, String tag) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_DISABLEKEYGUARD);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (Binder.getCallingUid() != 1000 && isKeyguardSecure()) {
            Log.d(TAG, "current mode is SecurityMode, ignore disableKeyguard");
        } else if (!isCurrentProfileLocked(UserHandle.getCallingUserId())) {
            Log.d(TAG, "non-current profiles, ignore disableKeyguard");
        } else if (token != null) {
            this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(1, Binder.getCallingUid(), 0, new Pair(token, tag)));
            Slog.i(TAG, "disableKeyguard pid = " + Binder.getCallingPid() + " ,callers = " + Debug.getCallers(5));
        } else {
            throw new IllegalArgumentException("token == null");
        }
    }

    public void reenableKeyguard(IBinder token) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_REENABLEKEYGUARD);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            Log.e(TAG, "SecurityException is in reenableKeyguard!");
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (token != null) {
            this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(2, token));
            Slog.i(TAG, "reenableKeyguard pid = " + Binder.getCallingPid() + " ,callers = " + Debug.getCallers(5));
        } else {
            Log.e(TAG, "IllegalArgumentException is in reenableKeyguard!");
            throw new IllegalArgumentException("token == null");
        }
    }

    public void exitKeyguardSecurely(final IOnKeyguardExitResult callback) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_EXITKEYGUARDSECURELY);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (callback != null) {
            this.mPolicy.exitKeyguardSecurely(new WindowManagerPolicy.OnKeyguardExitResult() {
                public void onKeyguardExitResult(boolean success) {
                    try {
                        callback.onKeyguardExitResult(success);
                    } catch (RemoteException e) {
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("callback == null");
        }
    }

    public boolean isKeyguardLocked() {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_ISKEYGUARDLOCKED);
        return this.mPolicy.isKeyguardLocked();
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        return this.mPolicy.isKeyguardShowingAndNotOccluded();
    }

    public boolean isKeyguardSecure() {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_ISKEYGUARDSECURE);
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            return this.mPolicy.isKeyguardSecure(userId);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean isShowingDream() {
        boolean isShowingDreamLw;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                isShowingDreamLw = this.mPolicy.isShowingDreamLw();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return isShowingDreamLw;
    }

    public void dismissKeyguard(IKeyguardDismissCallback callback, CharSequence message) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_DISMISSKEYGUARD);
        if (checkCallingPermission("android.permission.CONTROL_KEYGUARD", "dismissKeyguard")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mPolicy.dismissKeyguardLw(callback, message);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires CONTROL_KEYGUARD permission");
    }

    public void onKeyguardOccludedChanged(boolean occluded) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.onKeyguardOccludedChangedLw(occluded);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setSwitchingUser(boolean switching) {
        if (checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "setSwitchingUser()")) {
            this.mPolicy.setSwitchingUser(switching);
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mSwitchingUser = switching;
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires INTERACT_ACROSS_USERS_FULL permission");
    }

    /* access modifiers changed from: package-private */
    public void showGlobalActions() {
        this.mPolicy.showGlobalActions();
    }

    public void closeSystemDialogs(String reason) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.closeSystemDialogs(reason);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    static float fixScale(float scale) {
        if (scale < 0.0f) {
            scale = 0.0f;
        } else if (scale > 20.0f) {
            scale = 20.0f;
        }
        return Math.abs(scale);
    }

    public void setAnimationScale(int which, float scale) {
        if (checkCallingPermission("android.permission.SET_ANIMATION_SCALE", "setAnimationScale()")) {
            float scale2 = fixScale(scale);
            switch (which) {
                case 0:
                    this.mWindowAnimationScaleSetting = scale2;
                    break;
                case 1:
                    this.mTransitionAnimationScaleSetting = scale2;
                    break;
                case 2:
                    this.mAnimatorDurationScaleSetting = scale2;
                    break;
            }
            this.mH.sendEmptyMessage(14);
            return;
        }
        throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
    }

    public void setAnimationScales(float[] scales) {
        if (checkCallingPermission("android.permission.SET_ANIMATION_SCALE", "setAnimationScale()")) {
            if (scales != null) {
                if (scales.length >= 1) {
                    this.mWindowAnimationScaleSetting = fixScale(scales[0]);
                }
                if (scales.length >= 2) {
                    this.mTransitionAnimationScaleSetting = fixScale(scales[1]);
                }
                if (scales.length >= 3) {
                    this.mAnimatorDurationScaleSetting = fixScale(scales[2]);
                    dispatchNewAnimatorScaleLocked(null);
                }
            }
            this.mH.sendEmptyMessage(14);
            return;
        }
        throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
    }

    private void setAnimatorDurationScale(float scale) {
        this.mAnimatorDurationScaleSetting = scale;
        ValueAnimator.setDurationScale(scale);
    }

    public float getWindowAnimationScaleLocked() {
        if (this.mAnimationsDisabled) {
            return 0.0f;
        }
        return this.mWindowAnimationScaleSetting;
    }

    public float getTransitionAnimationScaleLocked() {
        if (this.mAnimationsDisabled) {
            return 0.0f;
        }
        return this.mTransitionAnimationScaleSetting;
    }

    public float getAnimationScale(int which) {
        switch (which) {
            case 0:
                return this.mWindowAnimationScaleSetting;
            case 1:
                return this.mTransitionAnimationScaleSetting;
            case 2:
                return this.mAnimatorDurationScaleSetting;
            default:
                return 0.0f;
        }
    }

    public float[] getAnimationScales() {
        return new float[]{this.mWindowAnimationScaleSetting, this.mTransitionAnimationScaleSetting, this.mAnimatorDurationScaleSetting};
    }

    public float getCurrentAnimatorScale() {
        float f;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                f = this.mAnimationsDisabled ? 0.0f : this.mAnimatorDurationScaleSetting;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return f;
    }

    /* access modifiers changed from: package-private */
    public void dispatchNewAnimatorScaleLocked(Session session) {
        this.mH.obtainMessage(34, session).sendToTarget();
    }

    public void registerPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        if (listener != null) {
            this.mPointerEventDispatcher.registerInputEventListener(listener);
        }
    }

    public void unregisterPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mPointerEventDispatcher.unregisterInputEventListener(listener);
    }

    /* access modifiers changed from: package-private */
    public boolean canDispatchPointerEvents() {
        return this.mPointerEventDispatcher != null;
    }

    public void registerExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        if (listener != null) {
            this.mExternalPointerEventDispatcher.registerInputEventListener(listener);
        }
    }

    public void unregisterExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mExternalPointerEventDispatcher.unregisterInputEventListener(listener);
    }

    public int getFocusedDisplayId() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean canDispatchExternalPointerEvents() {
        return this.mExternalPointerEventDispatcher != null;
    }

    public int getLidState() {
        int sw = this.mInputManager.getSwitchState(-1, -256, 0);
        if (sw > 0) {
            return 0;
        }
        if (sw == 0) {
            return 1;
        }
        return -1;
    }

    public void lockDeviceNow() {
        lockNow(null);
    }

    public int getCameraLensCoverState() {
        int sw = this.mInputManager.getSwitchState(-1, -256, 9);
        if (sw > 0) {
            return 1;
        }
        if (sw == 0) {
            return 0;
        }
        return -1;
    }

    public void switchKeyboardLayout(int deviceId, int direction) {
        this.mInputManager.switchKeyboardLayout(deviceId, direction);
    }

    public void switchInputMethod(boolean forwardDirection) {
        InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        if (inputMethodManagerInternal != null) {
            inputMethodManagerInternal.switchInputMethod(forwardDirection);
        }
    }

    public void shutdown(boolean confirm) {
        ShutdownThread.shutdown(ActivityThread.currentActivityThread().getSystemUiContext(), "userrequested", confirm);
    }

    public void reboot(boolean confirm) {
        ShutdownThread.reboot(ActivityThread.currentActivityThread().getSystemUiContext(), "userrequested", confirm);
    }

    public void rebootSafeMode(boolean confirm) {
        ShutdownThread.rebootSafeMode(ActivityThread.currentActivityThread().getSystemUiContext(), confirm);
    }

    public void setCurrentProfileIds(int[] currentProfileIds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mCurrentProfileIds = currentProfileIds;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mCurrentUserId = newUserId;
                this.mCurrentProfileIds = currentProfileIds;
                this.mAppTransition.setCurrentUser(newUserId);
                this.mPolicy.setCurrentUserLw(newUserId);
                boolean z = true;
                this.mPolicy.enableKeyguard(true);
                this.mRoot.switchUser();
                this.mWindowPlacerLocked.performSurfacePlacement();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                TaskStack stack = displayContent.getSplitScreenPrimaryStackIgnoringVisibility();
                DockedStackDividerController dockedStackDividerController = displayContent.mDividerControllerLocked;
                if (stack == null || !stack.hasTaskForUser(newUserId)) {
                    z = false;
                }
                dockedStackDividerController.notifyDockedStackExistsChanged(z);
                if (this.mDisplayReady) {
                    int forcedDensity = getForcedDisplayDensityForUserLocked(newUserId);
                    setForcedDisplayDensityLocked(displayContent, forcedDensity != 0 ? forcedDensity : displayContent.mInitialDisplayDensity);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        this.mHwWMSEx.setCurrentUser(newUserId, currentProfileIds);
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUserId) {
            return true;
        }
        for (int i : this.mCurrentProfileIds) {
            if (i == userId) {
                return true;
            }
        }
        return false;
    }

    public void enableScreenAfterBoot() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_BOOT) {
                    RuntimeException here = new RuntimeException("here");
                    here.fillInStackTrace();
                    Slog.i(TAG, "enableScreenAfterBoot: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
                }
                if (this.mSystemBooted) {
                    resetPriorityAfterLockedSection();
                    return;
                }
                this.mSystemBooted = true;
                hideBootMessagesLocked();
                this.mH.sendEmptyMessageDelayed(23, 30000);
                resetPriorityAfterLockedSection();
                this.mPolicy.systemBooted();
                performEnableScreen();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void enableScreenIfNeeded() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                enableScreenIfNeededLocked();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void enableScreenIfNeededLocked() {
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Slog.i(TAG, "enableScreenIfNeededLocked: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
        }
        if (!this.mDisplayEnabled) {
            if (this.mSystemBooted || this.mShowingBootMessages) {
                this.mH.sendEmptyMessage(16);
            }
        }
    }

    public void performBootTimeout() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mDisplayEnabled) {
                    resetPriorityAfterLockedSection();
                    return;
                }
                Slog.w(TAG, "***** BOOT TIMEOUT: forcing display enabled");
                this.mForceDisplayEnabled = true;
                resetPriorityAfterLockedSection();
                performEnableScreen();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void onSystemUiStarted() {
        this.mPolicy.onSystemUiStarted();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x011d, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:?, code lost:
        r9.mActivityManager.bootAnimationComplete();
     */
    public void performEnableScreen() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_BOOT) {
                    Slog.i(TAG, "performEnableScreen: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted + " mOnlyCore=" + this.mOnlyCore, new RuntimeException("here").fillInStackTrace());
                }
                if (this.mDisplayEnabled) {
                    resetPriorityAfterLockedSection();
                    return;
                } else if (!this.mSystemBooted && !this.mShowingBootMessages) {
                    resetPriorityAfterLockedSection();
                    return;
                } else if (!this.mShowingBootMessages && !this.mPolicy.canDismissBootAnimation()) {
                    Flog.i(305, "Keyguard not drawn complete,can not dismiss boot animation");
                    resetPriorityAfterLockedSection();
                    return;
                } else if (this.mForceDisplayEnabled || !getDefaultDisplayContentLocked().checkWaitingForWindows()) {
                    if (!this.mBootAnimationStopped) {
                        Trace.asyncTraceBegin(32, "Stop bootanim", 0);
                        SystemProperties.set("service.bootanim.exit", "1");
                        this.mBootAnimationStopped = true;
                    }
                    if (this.mForceDisplayEnabled || checkBootAnimationCompleteLocked()) {
                        IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
                        if (surfaceFlinger != null) {
                            Flog.i(304, "******* TELLING SURFACE FLINGER WE ARE BOOTED!");
                            Parcel data = Parcel.obtain();
                            data.writeInterfaceToken("android.ui.ISurfaceComposer");
                            surfaceFlinger.transact(1, data, null, 0);
                            data.recycle();
                        } else {
                            Flog.i(304, "performEnableScreen: SurfaceFlinger is dead!");
                        }
                        EventLog.writeEvent(EventLogTags.WM_BOOT_ANIMATION_DONE, SystemClock.uptimeMillis());
                        Trace.asyncTraceEnd(32, "Stop bootanim", 0);
                        this.mDisplayEnabled = true;
                        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
                            Slog.i(TAG, "******************** ENABLING SCREEN!");
                        }
                        this.mInputMonitor.setEventDispatchingLw(this.mEventDispatchingEnabled);
                    } else {
                        Flog.i(304, "Waiting for anim complete");
                        resetPriorityAfterLockedSection();
                        return;
                    }
                } else {
                    Flog.i(304, "Waiting for all visiable windows drawn");
                    resetPriorityAfterLockedSection();
                    return;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Boot completed: SurfaceFlinger is dead!");
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        this.mPolicy.enableScreenAfterBoot();
        updateRotationUnchecked(false, false);
    }

    /* access modifiers changed from: private */
    public boolean checkBootAnimationCompleteLocked() {
        if (SystemService.isRunning(BOOT_ANIMATION_SERVICE)) {
            this.mH.removeMessages(37);
            this.mH.sendEmptyMessageDelayed(37, 200);
            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                Slog.i(TAG, "checkBootAnimationComplete: Waiting for anim complete");
            }
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i(TAG, "checkBootAnimationComplete: Animation complete!");
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0079, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007c, code lost:
        if (r0 == false) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007e, code lost:
        performEnableScreen();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0081, code lost:
        return;
     */
    public void showBootMessage(CharSequence msg, boolean always) {
        boolean first = false;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (WindowManagerDebugConfig.DEBUG_BOOT) {
                    RuntimeException here = new RuntimeException("here");
                    here.fillInStackTrace();
                    Slog.i(TAG, "showBootMessage: msg=" + msg + " always=" + always + " mAllowBootMessages=" + this.mAllowBootMessages + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
                }
                if (!this.mAllowBootMessages) {
                    resetPriorityAfterLockedSection();
                    return;
                }
                if (!this.mShowingBootMessages) {
                    if (!always) {
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    first = true;
                }
                if (this.mSystemBooted) {
                    resetPriorityAfterLockedSection();
                } else {
                    this.mShowingBootMessages = true;
                    this.mPolicy.showBootMessage(msg, always);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void hideBootMessagesLocked() {
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Slog.i(TAG, "hideBootMessagesLocked: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
        }
        if (this.mShowingBootMessages) {
            this.mShowingBootMessages = false;
            this.mPolicy.hideBootMessages();
        }
    }

    public void setInTouchMode(boolean mode) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mInTouchMode = mode;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: private */
    public void updateCircularDisplayMaskIfNeeded() {
        int currentUserId;
        if (this.mContext.getResources().getConfiguration().isScreenRound() && this.mContext.getResources().getBoolean(17957095)) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    currentUserId = this.mCurrentUserId;
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            int showMask = 1;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_inversion_enabled", 0, currentUserId) == 1) {
                showMask = 0;
            }
            Message m = this.mH.obtainMessage(35);
            m.arg1 = showMask;
            this.mH.sendMessage(m);
        }
    }

    public void showEmulatorDisplayOverlayIfNeeded() {
        if (this.mContext.getResources().getBoolean(17957091) && SystemProperties.getBoolean(PROPERTY_EMULATOR_CIRCULAR, false) && Build.IS_EMULATOR) {
            this.mH.sendMessage(this.mH.obtainMessage(36));
        }
    }

    public void showCircularMask(boolean visible) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                openSurfaceTransaction();
                if (visible) {
                    if (this.mCircularDisplayMask == null) {
                        this.mCircularDisplayMask = new CircularDisplayMask(getDefaultDisplayContentLocked(), (this.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 10, this.mContext.getResources().getInteger(17694930), this.mContext.getResources().getDimensionPixelSize(17104956));
                    }
                    this.mCircularDisplayMask.setVisibility(true);
                } else if (this.mCircularDisplayMask != null) {
                    this.mCircularDisplayMask.setVisibility(false);
                    this.mCircularDisplayMask = null;
                }
                closeSurfaceTransaction("showCircularMask");
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setExitInfo(float pivotX, float pivotY, int iconWidth, int iconHeight, Bitmap iconBitmap, int flag) {
        this.mExitIconWidth = iconBitmap != null ? iconBitmap.getWidth() : iconWidth;
        this.mExitIconHeight = iconBitmap != null ? iconBitmap.getHeight() : iconHeight;
        this.mExitPivotX = pivotX;
        this.mExitPivotY = pivotY;
        this.mExitIconBitmap = iconBitmap;
        this.mExitFlag = flag;
    }

    public void showEmulatorDisplayOverlay() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                openSurfaceTransaction();
                if (this.mEmulatorDisplayOverlay == null) {
                    this.mEmulatorDisplayOverlay = new EmulatorDisplayOverlay(this.mContext, getDefaultDisplayContentLocked(), (this.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 10);
                }
                this.mEmulatorDisplayOverlay.setVisibility(true);
                closeSurfaceTransaction("showEmulatorDisplayOverlay");
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void showStrictModeViolation(boolean on) {
        int pid = Binder.getCallingPid();
        if (on) {
            this.mH.sendMessage(this.mH.obtainMessage(25, 1, pid));
            this.mH.sendMessageDelayed(this.mH.obtainMessage(25, 0, pid), 1000);
            return;
        }
        this.mH.sendMessage(this.mH.obtainMessage(25, 0, pid));
    }

    /* access modifiers changed from: private */
    public void showStrictModeViolation(int arg, int pid) {
        boolean on = arg != 0;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (!on || this.mRoot.canShowStrictModeViolation(pid)) {
                    SurfaceControl.openTransaction();
                    if (this.mStrictModeFlash == null) {
                        this.mStrictModeFlash = new StrictModeFlash(getDefaultDisplayContentLocked());
                    }
                    this.mStrictModeFlash.setVisibility(on);
                    SurfaceControl.closeTransaction();
                    resetPriorityAfterLockedSection();
                    return;
                }
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public void setStrictModeVisualIndicatorPreference(String value) {
        SystemProperties.set("persist.sys.strictmode.visual", value);
    }

    public Bitmap screenshotWallpaper() {
        Bitmap screenshotWallpaperLocked;
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotWallpaper()")) {
            try {
                Trace.traceBegin(32, "screenshotWallpaper");
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    screenshotWallpaperLocked = this.mRoot.mWallpaperController.screenshotWallpaperLocked();
                }
                resetPriorityAfterLockedSection();
                Trace.traceEnd(32);
                return screenshotWallpaperLocked;
            } catch (Throwable th) {
                Trace.traceEnd(32);
                throw th;
            }
        } else {
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }
    }

    public boolean requestAssistScreenshot(IAssistDataReceiver receiver) {
        Bitmap bm;
        Bitmap bm2;
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "requestAssistScreenshot()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(0);
                    if (displayContent == null) {
                        bm = null;
                    } else {
                        bm = displayContent.screenshotDisplayLocked(Bitmap.Config.ARGB_8888);
                    }
                    bm2 = bm;
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            FgThread.getHandler().post(new Runnable(receiver, bm2) {
                private final /* synthetic */ IAssistDataReceiver f$0;
                private final /* synthetic */ Bitmap f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void run() {
                    WindowManagerService.lambda$requestAssistScreenshot$2(this.f$0, this.f$1);
                }
            });
            return true;
        }
        throw new SecurityException("Requires READ_FRAME_BUFFER permission");
    }

    static /* synthetic */ void lambda$requestAssistScreenshot$2(IAssistDataReceiver receiver, Bitmap bm) {
        if (receiver != null) {
            try {
                receiver.onHandleAssistScreenshot(bm);
            } catch (RemoteException e) {
            }
        }
    }

    public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, int userId, boolean reducedResolution) {
        return this.mTaskSnapshotController.getSnapshot(taskId, userId, true, reducedResolution);
    }

    public void removeObsoleteTaskFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mTaskSnapshotController.removeObsoleteTaskFiles(persistentTaskIds, runningUserIds);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* JADX INFO: finally extract failed */
    public void freezeRotation(int rotation) {
        if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        } else if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        } else {
            int defaultDisplayRotation = getDefaultDisplayRotation();
            Flog.i(308, "freezeRotation: mRotation=" + defaultDisplayRotation + ",rotation=" + rotation + ",by pid=" + Binder.getCallingPid());
            long origId = Binder.clearCallingIdentity();
            try {
                this.mPolicy.setUserRotationMode(1, rotation == -1 ? defaultDisplayRotation : rotation);
                Binder.restoreCallingIdentity(origId);
                updateRotationUnchecked(false, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void thawRotation() {
        if (checkCallingPermission("android.permission.SET_ORIENTATION", "thawRotation()")) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "thawRotation: mRotation=" + getDefaultDisplayRotation() + ", by pid=" + Binder.getCallingPid());
            }
            long origId = Binder.clearCallingIdentity();
            try {
                this.mPolicy.setUserRotationMode(0, 777);
                Binder.restoreCallingIdentity(origId);
                updateRotationUnchecked(false, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        }
    }

    public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        updateRotationUnchecked(alwaysSendConfiguration, forceRelayout);
    }

    /* access modifiers changed from: package-private */
    public void pauseRotationLocked() {
        this.mDeferredRotationPauseCount++;
    }

    /* access modifiers changed from: package-private */
    public void resumeRotationLocked() {
        if (this.mDeferredRotationPauseCount > 0) {
            this.mDeferredRotationPauseCount--;
            if (this.mDeferredRotationPauseCount == 0) {
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                if (displayContent.updateRotationUnchecked()) {
                    this.mH.obtainMessage(18, Integer.valueOf(displayContent.getDisplayId())).sendToTarget();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRotationUnchecked(boolean alwaysSendConfiguration, boolean forceRelayout) {
        boolean rotationChanged;
        int displayId;
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "updateRotationUnchecked: alwaysSendConfiguration=" + alwaysSendConfiguration + " forceRelayout=" + forceRelayout);
        }
        Trace.traceBegin(32, "updateRotation");
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                Trace.traceBegin(32, "updateRotation: display");
                rotationChanged = displayContent.updateRotationUnchecked();
                Trace.traceEnd(32);
                if (rotationChanged) {
                    LogPower.push(128);
                }
                Flog.i(308, "updateRotationUnchecked: alwaysSendConfiguration=" + alwaysSendConfiguration + " forceRelayout=" + forceRelayout + " rotationChanged=" + rotationChanged);
                if (rotationChanged) {
                    if (!this.mIsPerfBoost) {
                        this.mIsPerfBoost = true;
                        UniPerf.getInstance().uniPerfEvent(4105, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, new int[]{0});
                    }
                    if (this.mLastFinishedFreezeSource != null) {
                        Jlog.d(58, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + this.mLastFinishedFreezeSource);
                    } else {
                        Jlog.d(58, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    }
                }
                if (!rotationChanged || forceRelayout) {
                    displayContent.setLayoutNeeded();
                    Trace.traceBegin(32, "updateRotation: performSurfacePlacement");
                    this.mWindowPlacerLocked.performSurfacePlacement();
                    Trace.traceEnd(32);
                }
                displayId = displayContent.getDisplayId();
            }
            resetPriorityAfterLockedSection();
            if (rotationChanged || alwaysSendConfiguration) {
                Trace.traceBegin(32, "updateRotation: sendNewConfiguration");
                sendNewConfiguration(displayId);
                Trace.traceEnd(32);
            }
            Binder.restoreCallingIdentity(origId);
            Trace.traceEnd(32);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            Trace.traceEnd(32);
            throw th;
        }
    }

    public int getDefaultDisplayRotation() {
        int rotation;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                rotation = getDefaultDisplayContentLocked().getRotation();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return rotation;
    }

    public boolean isRotationFrozen() {
        return this.mPolicy.getUserRotationMode() == 1;
    }

    public int watchRotation(IRotationWatcher watcher, int displayId) {
        int defaultDisplayRotation;
        final IBinder watcherBinder = watcher.asBinder();
        IBinder.DeathRecipient dr = new IBinder.DeathRecipient() {
            public void binderDied() {
                synchronized (WindowManagerService.this.mWindowMap) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        int i = 0;
                        while (i < WindowManagerService.this.mRotationWatchers.size()) {
                            if (watcherBinder == WindowManagerService.this.mRotationWatchers.get(i).mWatcher.asBinder()) {
                                IBinder binder = WindowManagerService.this.mRotationWatchers.remove(i).mWatcher.asBinder();
                                if (binder != null) {
                                    binder.unlinkToDeath(this, 0);
                                }
                                i--;
                            }
                            i++;
                        }
                    } catch (Throwable th) {
                        while (true) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        };
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                watcher.asBinder().linkToDeath(dr, 0);
                this.mRotationWatchers.add(new RotationWatcher(watcher, dr, displayId));
            } catch (RemoteException e) {
            }
            try {
                defaultDisplayRotation = getDefaultDisplayRotation();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return defaultDisplayRotation;
    }

    public void removeRotationWatcher(IRotationWatcher watcher) {
        IBinder watcherBinder = watcher.asBinder();
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                int i = 0;
                while (i < this.mRotationWatchers.size()) {
                    if (watcherBinder == this.mRotationWatchers.get(i).mWatcher.asBinder()) {
                        RotationWatcher removed = this.mRotationWatchers.remove(i);
                        IBinder binder = removed.mWatcher.asBinder();
                        if (binder != null) {
                            binder.unlinkToDeath(removed.mDeathRecipient, 0);
                        }
                        i--;
                    }
                    i++;
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean registerWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) {
        boolean isWallpaperVisible;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    this.mWallpaperVisibilityListeners.registerWallpaperVisibilityListener(listener, displayId);
                    isWallpaperVisible = displayContent.mWallpaperController.isWallpaperVisible();
                } else {
                    throw new IllegalArgumentException("Trying to register visibility event for invalid display: " + displayId);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return isWallpaperVisible;
    }

    public void unregisterWallpaperVisibilityListener(IWallpaperVisibilityListener listener, int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWallpaperVisibilityListeners.unregisterWallpaperVisibilityListener(listener, displayId);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public int getPreferredOptionsPanelGravity() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                int rotation = displayContent.getRotation();
                if (displayContent.mInitialDisplayWidth < displayContent.mInitialDisplayHeight) {
                    switch (rotation) {
                        case 1:
                            resetPriorityAfterLockedSection();
                            return 85;
                        case 2:
                            resetPriorityAfterLockedSection();
                            return 81;
                        case 3:
                            resetPriorityAfterLockedSection();
                            return 8388691;
                        default:
                            resetPriorityAfterLockedSection();
                            return 81;
                    }
                } else {
                    switch (rotation) {
                        case 1:
                            resetPriorityAfterLockedSection();
                            return 81;
                        case 2:
                            resetPriorityAfterLockedSection();
                            return 8388691;
                        case 3:
                            resetPriorityAfterLockedSection();
                            return 81;
                        default:
                            resetPriorityAfterLockedSection();
                            return 85;
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public boolean startViewServer(int port) {
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "startViewServer") || port < 1024) {
            return false;
        }
        if (this.mViewServer != null) {
            if (!this.mViewServer.isRunning()) {
                try {
                    return this.mViewServer.start();
                } catch (IOException e) {
                    Slog.w(TAG, "View server did not start");
                }
            }
            return false;
        }
        try {
            this.mViewServer = new ViewServer(this, port);
            return this.mViewServer.start();
        } catch (IOException e2) {
            Slog.w(TAG, "View server did not start");
            return false;
        }
    }

    private boolean isSystemSecure() {
        return "1".equals(SystemProperties.get(SYSTEM_SECURE, "1")) && "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
    }

    public boolean stopViewServer() {
        if (!isSystemSecure() && checkCallingPermission("android.permission.DUMP", "stopViewServer") && this.mViewServer != null) {
            return this.mViewServer.stop();
        }
        return false;
    }

    public boolean isViewServerRunning() {
        boolean z = false;
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "isViewServerRunning")) {
            return false;
        }
        if (this.mViewServer != null && this.mViewServer.isRunning()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean viewServerListWindows(Socket client) {
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        ArrayList<WindowState> windows = new ArrayList<>();
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(windows) {
                    private final /* synthetic */ ArrayList f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void accept(Object obj) {
                        this.f$0.add((WindowState) obj);
                    }
                }, false);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            int count = windows.size();
            for (int i = 0; i < count; i++) {
                WindowState w = windows.get(i);
                out2.write(Integer.toHexString(System.identityHashCode(w)));
                out2.write(32);
                out2.append(w.mAttrs.getTitle());
                out2.write(10);
            }
            out2.write("DONE.\n");
            out2.flush();
            try {
                out2.close();
            } catch (IOException e) {
                result = false;
            }
        } catch (Exception e2) {
            result = false;
            if (out != null) {
                out.close();
            }
        } catch (Throwable th2) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th2;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean viewServerGetFocusedWindow(Socket client) {
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        WindowState focusedWindow = getFocusedWindow();
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            if (focusedWindow != null) {
                out2.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                out2.write(32);
                out2.append(focusedWindow.mAttrs.getTitle());
            }
            out2.write(10);
            out2.flush();
            try {
                out2.close();
            } catch (IOException e) {
                result = false;
            }
        } catch (Exception e2) {
            result = false;
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean viewServerWindowCommand(Socket client, String command, String parameters) {
        if (isSystemSecure()) {
            return false;
        }
        boolean success = true;
        Parcel data = null;
        Parcel reply = null;
        BufferedWriter out = null;
        try {
            int index = parameters.indexOf(32);
            if (index == -1) {
                index = parameters.length();
            }
            int hashCode = (int) Long.parseLong(parameters.substring(0, index), 16);
            parameters = index < parameters.length() ? parameters.substring(index + 1) : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            WindowState window = findWindow(hashCode);
            if (window == null) {
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
                return false;
            }
            Parcel data2 = Parcel.obtain();
            data2.writeInterfaceToken("android.view.IWindow");
            data2.writeString(command);
            data2.writeString(parameters);
            data2.writeInt(1);
            ParcelFileDescriptor.fromSocket(client).writeToParcel(data2, 0);
            Parcel reply2 = Parcel.obtain();
            window.mClient.asBinder().transact(1, data2, reply2, 0);
            reply2.readException();
            if (!client.isOutputShutdown()) {
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                out.write("DONE\n");
                out.flush();
            }
            if (data2 != null) {
                data2.recycle();
            }
            if (reply2 != null) {
                reply2.recycle();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                }
            }
            return success;
        } catch (Exception e3) {
            Slog.w(TAG, "Could not send command " + command + " with parameters " + parameters, e3);
            success = false;
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public void addWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowChangeListeners.add(listener);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void removeWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowChangeListeners.remove(listener);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
        resetPriorityAfterLockedSection();
        r0 = r1.length;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
        if (r2 >= r0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        r1[r2].windowsChanged();
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        return;
     */
    public void notifyWindowsChanged() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mWindowChangeListeners.isEmpty()) {
                    resetPriorityAfterLockedSection();
                } else {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
        resetPriorityAfterLockedSection();
        r0 = r1.length;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
        if (r2 >= r0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        r1[r2].focusChanged();
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        return;
     */
    public void notifyFocusChanged() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mWindowChangeListeners.isEmpty()) {
                    resetPriorityAfterLockedSection();
                } else {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    private WindowState findWindow(int hashCode) {
        WindowState window;
        if (hashCode == -1) {
            return getFocusedWindow();
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                window = this.mRoot.getWindow(new Predicate(hashCode) {
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final boolean test(Object obj) {
                        return WindowManagerService.lambda$findWindow$4(this.f$0, (WindowState) obj);
                    }
                });
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return window;
    }

    static /* synthetic */ boolean lambda$findWindow$4(int hashCode, WindowState w) {
        return System.identityHashCode(w) == hashCode;
    }

    /* access modifiers changed from: package-private */
    public void sendNewConfiguration(int displayId) {
        try {
            if (!this.mActivityManager.updateDisplayOverrideConfiguration(null, displayId)) {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    if (this.mWaitingForConfig) {
                        this.mWaitingForConfig = false;
                        this.mLastFinishedFreezeSource = "config-unchanged";
                        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                        if (dc != null) {
                            dc.setLayoutNeeded();
                        }
                        this.mWindowPlacerLocked.performSurfacePlacement();
                    }
                }
                resetPriorityAfterLockedSection();
            }
        } catch (RemoteException e) {
        } catch (Throwable th) {
            while (true) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public Configuration computeNewConfiguration(int displayId) {
        Configuration computeNewConfigurationLocked;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                computeNewConfigurationLocked = computeNewConfigurationLocked(displayId);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return computeNewConfigurationLocked;
    }

    /* access modifiers changed from: package-private */
    public Configuration computeNewConfigurationLocked(int displayId) {
        if (!this.mDisplayReady) {
            return null;
        }
        Configuration config = new Configuration();
        this.mRoot.getDisplayContent(displayId).computeScreenConfiguration(config);
        return config;
    }

    /* access modifiers changed from: package-private */
    public void notifyHardKeyboardStatusChange() {
        WindowManagerInternal.OnHardKeyboardStatusChangeListener listener;
        boolean available;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                listener = this.mHardKeyboardStatusChangeListener;
                available = this.mHardKeyboardAvailable;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        if (listener != null) {
            listener.onHardKeyboardStatusChange(available);
        }
    }

    public void setEventDispatching(boolean enabled) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setEventDispatching()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mEventDispatchingEnabled = enabled;
                    if (this.mDisplayEnabled) {
                        this.mInputMonitor.setEventDispatchingLw(enabled);
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public WindowState getFocusedWindow() {
        WindowState focusedWindowLocked;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                focusedWindowLocked = getFocusedWindowLocked();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return focusedWindowLocked;
    }

    public String getFocusedAppComponentName() {
        if (this.mFocusedApp != null) {
            return this.mFocusedApp.appComponentName;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public WindowState getFocusedWindowLocked() {
        return this.mCurrentFocus;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getImeFocusStackLocked() {
        if (this.mFocusedApp == null || this.mFocusedApp.getTask() == null) {
            return null;
        }
        return this.mFocusedApp.getTask().mStack;
    }

    public boolean detectSafeMode() {
        if (this.mHwWMSEx.detectSafeMode()) {
            return this.mHwWMSEx.getSafeMode();
        }
        if (!this.mInputMonitor.waitForInputDevicesReady(1000)) {
            Slog.w(TAG, "Devices still not ready after waiting 1000 milliseconds before attempting to detect safe mode.");
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "safe_boot_disallowed", 0) != 0) {
            return false;
        }
        int menuState = this.mInputManager.getKeyCodeState(-1, -256, 82);
        int sState = this.mInputManager.getKeyCodeState(-1, -256, 47);
        int dpadState = this.mInputManager.getKeyCodeState(-1, UsbTerminalTypes.TERMINAL_IN_MIC, 23);
        int trackballState = this.mInputManager.getScanCodeState(-1, 65540, 272);
        this.mSafeMode = menuState > 0 || sState > 0 || dpadState > 0 || trackballState > 0;
        try {
            if (!(SystemProperties.getInt(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, 0) == 0 && SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0)) {
                this.mSafeMode = true;
                SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
        } catch (IllegalArgumentException e) {
        }
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            this.mSafeMode = false;
        }
        if (this.mSafeMode) {
            Log.i(TAG, "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState + " dpad=" + dpadState + " trackball=" + trackballState + ")");
            if (SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0) {
                SystemProperties.set(ShutdownThread.RO_SAFEMODE_PROPERTY, "1");
            }
        } else {
            Log.i(TAG, "SAFE MODE not enabled");
        }
        this.mPolicy.setSafeMode(this.mSafeMode);
        return this.mSafeMode;
    }

    public void displayReady() {
        int displayCount = this.mRoot.mChildren.size();
        for (int i = 0; i < displayCount; i++) {
            displayReady(((DisplayContent) this.mRoot.mChildren.get(i)).getDisplayId());
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                if (this.mMaxUiWidth > 0) {
                    displayContent.setMaxUiWidth(this.mMaxUiWidth);
                }
                readForcedDisplayPropertiesLocked(displayContent);
                this.mDisplayReady = true;
            } finally {
                while (true) {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        resetPriorityAfterLockedSection();
        try {
            this.mActivityManager.updateConfiguration(null);
        } catch (RemoteException e) {
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mIsTouchDevice = this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
                getDefaultDisplayContentLocked().configureDisplayPolicy();
            } catch (Throwable th) {
                while (true) {
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        try {
            this.mActivityManager.updateConfiguration(null);
        } catch (RemoteException e2) {
        }
        updateCircularDisplayMaskIfNeeded();
    }

    private void displayReady(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    this.mAnimator.addDisplayLocked(displayId);
                    displayContent.initializeDisplayBaseInfo();
                    reconfigureDisplayLocked(displayContent);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void systemReady() {
        this.mPolicy.systemReady();
        this.mTaskSnapshotController.systemReady();
        this.mHasWideColorGamutSupport = queryWideColorGamutSupport();
        this.mHwWMSEx.hwSystemReady();
    }

    private static boolean queryWideColorGamutSupport() {
        try {
            OptionalBool hasWideColor = ISurfaceFlingerConfigs.getService().hasWideColorDisplay();
            if (hasWideColor != null) {
                return hasWideColor.value;
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void destroyPreservedSurfaceLocked() {
        for (int i = this.mDestroyPreservedSurface.size() - 1; i >= 0; i--) {
            this.mDestroyPreservedSurface.get(i).mWinAnimator.destroyPreservedSurfaceLocked();
        }
        this.mDestroyPreservedSurface.clear();
    }

    public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        if (client == null) {
            throw new IllegalArgumentException("null client");
        } else if (inputContext != null) {
            return new Session(this, callback, client, inputContext);
        } else {
            throw new IllegalArgumentException("null inputContext");
        }
    }

    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (HwPCUtils.isPcCastModeInServer()) {
                    DisplayContent displayContent = this.mRoot.getDisplayContent(getFocusedDisplayId());
                    if (displayContent != null && displayContent.inputMethodClientHasFocus(client)) {
                        resetPriorityAfterLockedSection();
                        return true;
                    }
                } else if (getDefaultDisplayContentLocked().inputMethodClientHasFocus(client)) {
                    resetPriorityAfterLockedSection();
                    return true;
                }
                if (this.mCurrentFocus == null || this.mCurrentFocus.mSession.mClient == null || this.mCurrentFocus.mSession.mClient.asBinder() != client.asBinder()) {
                    resetPriorityAfterLockedSection();
                    return false;
                }
                resetPriorityAfterLockedSection();
                return true;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void getInitialDisplaySize(int displayId, Point size) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mInitialDisplayWidth;
                    size.y = displayContent.mInitialDisplayHeight;
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void getBaseDisplaySize(int displayId, Point size) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mBaseDisplayWidth;
                    size.y = displayContent.mBaseDisplayHeight;
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setForcedDisplaySize(int displayId, int width, int height) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (HwFoldScreenState.isFoldScreenDevice()) {
                    this.mCurrentFoldDisplayMode = this.mDisplayManagerInternal.getDisplayMode();
                }
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null) {
                        int width2 = Math.min(Math.max(width, 200), displayContent.mInitialDisplayWidth * 2);
                        int height2 = Math.min(Math.max(height, 200), displayContent.mInitialDisplayHeight * 2);
                        Slog.d(TAG, "setForcedDisplaySize and updateResourceConfiguration for HW_ROG_SUPPORT");
                        this.mHwWMSEx.updateResourceConfiguration(displayId, displayContent.mBaseDisplayDensity, width2, height2);
                        setForcedDisplaySizeLocked(displayContent, width2, height2);
                        ContentResolver contentResolver = this.mContext.getContentResolver();
                        Settings.Global.putString(contentResolver, "display_size_forced", width2 + "," + height2);
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Can only set the default display");
        }
    }

    public void setForcedDisplayScalingMode(int displayId, int mode) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null) {
                        if (mode < 0 || mode > 1) {
                            mode = 0;
                        }
                        setForcedDisplayScalingModeLocked(displayContent, mode);
                        Settings.Global.putInt(this.mContext.getContentResolver(), "display_scaling_force", mode);
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Can only set the default display");
        }
    }

    private void setForcedDisplayScalingModeLocked(DisplayContent displayContent, int mode) {
        StringBuilder sb = new StringBuilder();
        sb.append("Using display scaling mode: ");
        sb.append(mode == 0 ? UiModeManagerService.Shell.NIGHT_MODE_STR_AUTO : "off");
        Slog.i(TAG, sb.toString());
        displayContent.mDisplayScalingDisabled = mode != 0;
        reconfigureDisplayLocked(displayContent);
    }

    private void readForcedDisplayPropertiesLocked(DisplayContent displayContent) {
        String sizeStr = Settings.Global.getString(this.mContext.getContentResolver(), "display_size_forced");
        if (sizeStr == null || sizeStr.length() == 0) {
            sizeStr = SystemProperties.get(SIZE_OVERRIDE, null);
        }
        if (sizeStr != null && sizeStr.length() > 0) {
            int pos = sizeStr.indexOf(44);
            if (pos > 0 && sizeStr.lastIndexOf(44) == pos) {
                try {
                    int width = Integer.parseInt(sizeStr.substring(0, pos));
                    int height = Integer.parseInt(sizeStr.substring(pos + 1));
                    if (!(displayContent.mBaseDisplayWidth == width && displayContent.mBaseDisplayHeight == height)) {
                        Slog.i(TAG, "FORCED DISPLAY SIZE: " + width + "x" + height);
                        displayContent.updateBaseDisplayMetrics(width, height, displayContent.mBaseDisplayDensity);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        int density = getForcedDisplayDensityForUserLocked(this.mCurrentUserId);
        if (density != 0) {
            displayContent.mBaseDisplayDensity = density;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "display_scaling_force", 0) != 0) {
            Slog.i(TAG, "FORCED DISPLAY SCALING DISABLED");
            displayContent.mDisplayScalingDisabled = true;
        }
    }

    private void setForcedDisplaySizeLocked(DisplayContent displayContent, int width, int height) {
        Slog.i(TAG, "Using new display size: " + width + "x" + height);
        displayContent.updateBaseDisplayMetrics(width, height, displayContent.mBaseDisplayDensity);
        reconfigureDisplayLocked(displayContent);
    }

    public void clearForcedDisplaySize(int displayId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null) {
                        setForcedDisplaySizeLocked(displayContent, displayContent.mInitialDisplayWidth, displayContent.mInitialDisplayHeight);
                        Settings.Global.putString(this.mContext.getContentResolver(), "display_size_forced", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Can only set the default display");
        }
    }

    public int getInitialDisplayDensity(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                    resetPriorityAfterLockedSection();
                    return -1;
                }
                int i = displayContent.mInitialDisplayDensity;
                resetPriorityAfterLockedSection();
                return i;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public int getBaseDisplayDensity(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                    resetPriorityAfterLockedSection();
                    return -1;
                }
                int i = displayContent.mBaseDisplayDensity;
                resetPriorityAfterLockedSection();
                return i;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void setForcedDisplayDensityForUser(int displayId, int density, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId == 0) {
            int targetUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null && this.mCurrentUserId == targetUserId) {
                        Slog.d(TAG, "setForcedDisplayDensityForUser and updateResourceConfiguration for HW_ROG_SUPPORT");
                        this.mHwWMSEx.updateResourceConfiguration(displayId, density, displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight);
                        setForcedDisplayDensityLocked(displayContent, density);
                    }
                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), targetUserId);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Can only set the default display");
        }
    }

    public void clearForcedDisplayDensityForUser(int displayId, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId == 0) {
            int callingUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null && this.mCurrentUserId == callingUserId) {
                        int curWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
                        if (curWidth > 0) {
                            setForcedDisplayDensityLocked(displayContent, (SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)) * curWidth) / displayContent.mInitialDisplayWidth);
                        } else {
                            setForcedDisplayDensityLocked(displayContent, displayContent.mInitialDisplayDensity);
                        }
                    }
                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, callingUserId);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Can only set the default display");
        }
    }

    private int getForcedDisplayDensityForUserLocked(int userId) {
        String densityStr = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", userId);
        if (densityStr == null || densityStr.length() == 0) {
            densityStr = SystemProperties.get(DENSITY_OVERRIDE, null);
        }
        if (densityStr != null && densityStr.length() > 0) {
            try {
                return Integer.parseInt(densityStr);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    private void setForcedDisplayDensityLocked(DisplayContent displayContent, int density) {
        displayContent.mBaseDisplayDensity = density;
        reconfigureDisplayLocked(displayContent);
    }

    /* access modifiers changed from: protected */
    public void reconfigureDisplayLocked(DisplayContent displayContent) {
        if (displayContent.isReady()) {
            displayContent.configureDisplayPolicy();
            displayContent.setLayoutNeeded();
            int displayId = displayContent.getDisplayId();
            boolean configChanged = updateOrientationFromAppTokensLocked(displayId);
            Configuration currentDisplayConfig = displayContent.getConfiguration();
            this.mTempConfiguration.setTo(currentDisplayConfig);
            displayContent.computeScreenConfiguration(this.mTempConfiguration);
            if (configChanged || (currentDisplayConfig.diff(this.mTempConfiguration) != 0)) {
                this.mWaitingForConfig = true;
                startFreezingDisplayLocked(0, 0, displayContent);
                this.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
            }
            this.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    public void getDisplaysInFocusOrder(SparseIntArray displaysInFocusOrder) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.getDisplaysInFocusOrder(displaysInFocusOrder);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setOverscan(int displayId, int left, int top, int right, int bottom) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                    if (displayContent != null) {
                        setOverscanLocked(displayContent, left, top, right, bottom);
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    private void setOverscanLocked(DisplayContent displayContent, int left, int top, int right, int bottom) {
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        displayInfo.overscanLeft = left;
        displayInfo.overscanTop = top;
        displayInfo.overscanRight = right;
        displayInfo.overscanBottom = bottom;
        this.mDisplaySettings.setOverscanLocked(displayInfo.uniqueId, displayInfo.name, left, top, right, bottom);
        this.mDisplaySettings.writeSettingsLocked();
        reconfigureDisplayLocked(displayContent);
    }

    public void startWindowTrace() {
        try {
            this.mWindowTracing.startTrace(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopWindowTrace() {
        this.mWindowTracing.stopTrace(null);
    }

    public boolean isWindowTraceEnabled() {
        return this.mWindowTracing.isEnabled();
    }

    /* access modifiers changed from: package-private */
    public final WindowState windowForClientLocked(Session session, IWindow client, boolean throwOnError) {
        return windowForClientLocked(session, client.asBinder(), throwOnError);
    }

    /* access modifiers changed from: package-private */
    public final WindowState windowForClientLocked(Session session, IBinder client, boolean throwOnError) {
        WindowState win = (WindowState) this.mWindowMap.get(client);
        if (win == null) {
            if (!throwOnError) {
                Slog.w(TAG, "Failed looking up window callers=" + Debug.getCallers(3));
                return null;
            }
            throw new IllegalArgumentException("Requested window " + client + " does not exist");
        } else if (session == null || win.mSession == session) {
            return win;
        } else {
            if (!throwOnError) {
                Slog.w(TAG, "Failed looking up window callers=" + Debug.getCallers(3));
                return null;
            }
            throw new IllegalArgumentException("Requested window " + client + " is in session " + win.mSession + ", not " + session);
        }
    }

    /* access modifiers changed from: package-private */
    public void makeWindowFreezingScreenIfNeededLocked(WindowState w) {
        if (!w.mToken.okToDisplay() && this.mWindowsFreezingScreen != 2) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Changing surface while display frozen: " + w);
            }
            w.setOrientationChanging(true);
            w.mLastFreezeDuration = 0;
            this.mRoot.mOrientationChangeComplete = false;
            if (this.mWindowsFreezingScreen == 0) {
                this.mWindowsFreezingScreen = 1;
                this.mH.removeMessages(11);
                this.mH.sendEmptyMessageDelayed(11, (long) WINDOW_FREEZE_TIMEOUT_DURATION);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int handleAnimatingStoppedAndTransitionLocked() {
        this.mAppTransition.setIdle();
        for (int i = this.mNoAnimationNotifyOnTransitionFinished.size() - 1; i >= 0; i--) {
            this.mAppTransition.notifyAppTransitionFinishedLocked(this.mNoAnimationNotifyOnTransitionFinished.get(i));
        }
        this.mNoAnimationNotifyOnTransitionFinished.clear();
        DisplayContent dc = getDefaultDisplayContentLocked();
        dc.mWallpaperController.hideDeferredWallpapersIfNeeded();
        dc.onAppTransitionDone();
        int changes = 0 | 1;
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.v(TAG, "Wallpaper layer changed: assigning layers + relayout");
        }
        if (getFocusedDisplayId() == 0) {
            dc.computeImeTarget(true);
        }
        this.mRoot.mWallpaperMayChange = true;
        this.mFocusMayChange = true;
        return changes;
    }

    /* access modifiers changed from: package-private */
    public void checkDrawnWindowsLocked() {
        if (!this.mWaitingForDrawn.isEmpty() && this.mWaitingForDrawnCallback != null) {
            boolean isPrintAllWindowsDrawnLogs = isPrintAllWindowsDrawnLogs();
            for (int j = this.mWaitingForDrawn.size() - 1; j >= 0; j--) {
                WindowState win = this.mWaitingForDrawn.get(j);
                Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Waiting for drawn " + win + ": removed=" + win.mRemoved + " visible=" + win.isVisibleLw() + " mHasSurface=" + win.mHasSurface + " drawState=" + win.mWinAnimator.mDrawState);
                if (win.mRemoved || !win.mHasSurface || !win.mPolicyVisibility) {
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Slog.w(TAG, "Aborted waiting for drawn: " + win);
                    }
                    this.mWaitingForDrawn.remove(win);
                } else if (win.hasDrawnLw()) {
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Slog.d(TAG, "Window drawn win=" + win);
                    }
                    this.mWaitingForDrawn.remove(win);
                }
            }
            if (this.mWaitingForDrawn.isEmpty()) {
                Flog.i(307, "All windows drawn!");
                this.mH.removeMessages(24);
                this.mFingerUnlockHandler.sendEmptyMessage(33);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setHoldScreenLocked(Session newHoldScreen) {
        boolean hold = newHoldScreen != null;
        if (hold && this.mHoldingScreenOn != newHoldScreen) {
            this.mHoldingScreenWakeLock.setWorkSource(new WorkSource(newHoldScreen.mUid));
        }
        if (this.mHoldingScreenOn != newHoldScreen) {
            Slog.i("DebugKeepScreenOn", "setHoldScreenLocked newHoldScreen:" + newHoldScreen + " currentHoldScreen:" + this.mHoldingScreenOn + " wakeLockState:" + this.mHoldingScreenWakeLock.isHeld());
        }
        this.mHoldingScreenOn = newHoldScreen;
        if (hold == this.mHoldingScreenWakeLock.isHeld()) {
            return;
        }
        if (hold) {
            Slog.i("DebugKeepScreenOn", "Acquiring screen wakelock due to " + this.mRoot.mHoldScreenWindow);
            this.mLastWakeLockHoldingWindow = this.mRoot.mHoldScreenWindow;
            int displayId = this.mLastWakeLockHoldingWindow.getDisplayId();
            this.mLastWakeLockObscuringWindow = null;
            if (HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
                this.mHoldingScreenWakeLock.acquire();
            } else {
                this.mPCHoldingScreenWakeLock.acquire();
            }
            this.mPolicy.keepScreenOnStartedLw();
            return;
        }
        Slog.i("DebugKeepScreenOn", "Releasing screen wakelock, obscured by " + this.mRoot.mObscuringWindow);
        this.mLastWakeLockHoldingWindow = null;
        this.mLastWakeLockObscuringWindow = this.mRoot.mObscuringWindow;
        this.mPolicy.keepScreenOnStoppedLw();
        this.mHoldingScreenWakeLock.release();
        this.mPCHoldingScreenWakeLock.release();
    }

    /* access modifiers changed from: package-private */
    public void requestTraversal() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.requestTraversal();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void scheduleAnimationLocked() {
        if (this.mAnimator != null) {
            this.mAnimator.scheduleAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        int i = mode;
        boolean z = updateInputWindows;
        WindowState newFocus = this.mRoot.computeFocusedWindow();
        if (this.mCurrentFocus == newFocus) {
            return false;
        }
        Trace.traceBegin(32, "wmUpdateFocus");
        this.mH.removeMessages(2);
        this.mH.sendEmptyMessage(2);
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        if (isLandScapeMultiWindowMode() && (this.mPolicy instanceof PhoneWindowManager)) {
            ((PhoneWindowManager) this.mPolicy).setFocusChangeIMEFrozenTag(true);
        }
        if (HwPCUtils.isPcCastModeInServer()) {
            if (HwPCUtils.enabledInPad()) {
                DisplayContent pcDC = this.mRoot.getDisplayContent(getFocusedDisplayId());
                if (pcDC != null) {
                    displayContent = pcDC;
                }
            } else {
                Slog.d(TAG, "updateFocusedWindowLocked: " + newFocus + ", focusedDisplayId:" + getFocusedDisplayId());
                if (newFocus != null) {
                    displayContent = newFocus.getDisplayContent();
                    if (getFocusedDisplayId() != displayContent.getDisplayId()) {
                        Slog.w(TAG, "newFocus is not in focused PC displayContent");
                    }
                } else {
                    DisplayContent focusedDC = this.mRoot.getDisplayContent(getFocusedDisplayId());
                    if (focusedDC != null) {
                        displayContent = focusedDC;
                    }
                }
            }
        }
        boolean imWindowChanged = false;
        if (this.mInputMethodWindow != null) {
            imWindowChanged = this.mInputMethodTarget != displayContent.computeImeTarget(true);
            if (!(i == 1 || i == 3)) {
                int prevImeAnimLayer = this.mInputMethodWindow.mWinAnimator.mAnimLayer;
                displayContent.assignWindowLayers(false);
                imWindowChanged |= prevImeAnimLayer != this.mInputMethodWindow.mWinAnimator.mAnimLayer;
            }
        }
        if (imWindowChanged) {
            this.mWindowsChanged = true;
            displayContent.setLayoutNeeded();
            newFocus = this.mRoot.computeFocusedWindow();
        }
        WindowState newFocus2 = newFocus;
        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v(TAG, "Changing focus from " + this.mCurrentFocus + " to " + newFocus2 + " Callers=" + Debug.getCallers(4));
        }
        WindowState oldFocus = this.mCurrentFocus;
        this.mCurrentFocus = newFocus2;
        if (this.mNotifyFocusedWindow) {
            IWindow currentWindow = null;
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    Iterator<Map.Entry<IBinder, WindowState>> it = this.mWindowMap.entrySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        WindowState windowState = this.mCurrentFocus;
                        Map.Entry next = it.next();
                        if (windowState == next.getValue()) {
                            currentWindow = IWindow.Stub.asInterface((IBinder) next.getKey());
                            break;
                        }
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            Slog.i(TAG, "currentWindow = " + currentWindow);
            if (currentWindow != null) {
                try {
                    currentWindow.notifyFocusChanged();
                } catch (RemoteException e) {
                    RemoteException remoteException = e;
                    Slog.w(TAG, "currentWindow.notifyFocusChanged() get RemoteException!");
                }
            }
        }
        this.mInputManager.setCurFocusWindow(this.mCurrentFocus);
        this.mLosingFocus.remove(newFocus2);
        if (this.mCurrentFocus != null) {
            this.mWinAddedSinceNullFocus.clear();
            this.mWinRemovedSinceNullFocus.clear();
        }
        if (this.mCurrentFocus != null) {
            this.mWinAddedSinceNullFocus.clear();
            this.mWinRemovedSinceNullFocus.clear();
        }
        int focusChanged = this.mPolicy.focusChangedLw(oldFocus, newFocus2);
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            if (newFocus2 != null) {
                try {
                    if (newFocus2.getAttrs() != null) {
                        hwAft.notifyFocusChange(newFocus2.mSession.mPid, newFocus2.getAttrs().getTitle().toString());
                    }
                } catch (RemoteException e2) {
                    Slog.e(TAG, "binder call hwAft throw " + e2);
                }
            }
            hwAft.notifyFocusChange(0, null);
        }
        if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
            if (i == 2) {
                displayContent.performLayout(true, z);
                focusChanged &= -2;
            } else if (i == 3) {
                displayContent.assignWindowLayers(false);
            }
        }
        if ((focusChanged & 1) != 0) {
            displayContent.setLayoutNeeded();
            if (i == 2) {
                displayContent.performLayout(true, z);
            }
        }
        if (i != 1) {
            this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, z);
        }
        Flog.i(304, "oldFocusWindow: " + oldFocus + ", currentFocusWindow: " + this.mCurrentFocus + ", currentFocusApp: " + this.mFocusedApp);
        ArrayMap<String, Object> params = new ArrayMap<>();
        if (this.mCurrentFocus != null) {
            params.put("focusedWindowName", this.mCurrentFocus.toString());
            params.put("layoutParams", this.mCurrentFocus.getAttrs());
            if (this.mCurrentFocus.mSession != null) {
                params.put(IZRHungService.PARAM_PID, Integer.valueOf(this.mCurrentFocus.mSession.mPid));
            }
        } else {
            params.put("focusedWindowName", "null");
            params.put("layoutParams", "null");
            params.put(IZRHungService.PARAM_PID, 0);
        }
        if (this.mFocusedApp != null) {
            params.put("focusedPackageName", this.mFocusedApp.appPackageName);
            params.put("focusedActivityName", this.mFocusedApp.appComponentName);
        } else {
            params.put("focusedPackageName", "null");
            params.put("focusedActivityName", "null");
        }
        ZrHungData arg = new ZrHungData();
        arg.putAll(params);
        IZrHung focusWindowZrHung = HwFrameworkFactory.getZrHung("appeye_nofocuswindow");
        if (focusWindowZrHung != null) {
            if (this.mCurrentFocus == null) {
                focusWindowZrHung.check(arg);
            } else {
                focusWindowZrHung.cancelCheck(arg);
            }
        }
        IZrHung transWindowZrHung = HwFrameworkFactory.getZrHung("appeye_transparentwindow");
        if (transWindowZrHung != null) {
            transWindowZrHung.cancelCheck(arg);
            if (this.mCurrentFocus != null) {
                transWindowZrHung.check(arg);
            }
        }
        displayContent.adjustForImeIfNeeded();
        displayContent.scheduleToastWindowsTimeoutIfNeededLocked(oldFocus, newFocus2);
        Trace.traceEnd(32);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void startFreezingDisplayLocked(int exitAnim, int enterAnim) {
        startFreezingDisplayLocked(exitAnim, enterAnim, getDefaultDisplayContentLocked());
    }

    /* access modifiers changed from: package-private */
    public void startFreezingDisplayLocked(int exitAnim, int enterAnim, DisplayContent displayContent) {
        ScreenRotationAnimation screenRotationAnimation;
        if (!this.mDisplayFrozen && !this.mRotatingSeamlessly && displayContent.isReady() && this.mPolicy.isScreenOn() && displayContent.okToAnimate()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(TAG, "startFreezingDisplayLocked: exitAnim=" + exitAnim + " enterAnim=" + enterAnim + " called by " + Debug.getCallers(8));
            }
            this.mScreenFrozenLock.acquire();
            this.mDisplayFrozen = true;
            this.mDisplayFreezeTime = SystemClock.elapsedRealtime();
            this.mLastFinishedFreezeSource = null;
            this.mFrozenDisplayId = displayContent.getDisplayId();
            this.mInputMonitor.freezeInputDispatchingLw();
            this.mPolicy.setLastInputMethodWindowLw(null, null);
            if (this.mAppTransition.isTransitionSet()) {
                this.mAppTransition.freeze();
            }
            this.mLatencyTracker.onActionStart(6);
            if (displayContent.isDefaultDisplay) {
                this.mExitAnimId = exitAnim;
                this.mEnterAnimId = enterAnim;
                ScreenRotationAnimation screenRotationAnimation2 = this.mAnimator.getScreenRotationAnimationLocked(this.mFrozenDisplayId);
                if (screenRotationAnimation2 != null) {
                    screenRotationAnimation2.kill();
                }
                boolean isSecure = displayContent.hasSecureWindowOnScreen();
                displayContent.updateDisplayInfo();
                if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayContent.getDisplayId())) {
                    IHwScreenRotationAnimation hwSRA = HwServiceFactory.getHwScreenRotationAnimation();
                    if (hwSRA != null) {
                        screenRotationAnimation = hwSRA.create(this.mContext, displayContent, this.mPolicy.isDefaultOrientationForced(), isSecure, this);
                    } else {
                        ScreenRotationAnimation screenRotationAnimation3 = new ScreenRotationAnimation(this.mContext, displayContent, this.mPolicy.isDefaultOrientationForced(), isSecure, this);
                        screenRotationAnimation = screenRotationAnimation3;
                    }
                    this.mAnimator.setScreenRotationAnimationLocked(this.mFrozenDisplayId, screenRotationAnimation);
                } else {
                    this.mH.removeMessages(103);
                    this.mH.sendEmptyMessageDelayed(103, 3000);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopFreezingDisplayLocked() {
        if (this.mDisplayFrozen) {
            boolean z = true;
            if (!this.mWaitingForConfig && this.mAppsFreezingScreen <= 0 && this.mWindowsFreezingScreen != 1 && !this.mClientFreezingScreen) {
                z = false;
            }
            boolean bReturn = z;
            int size = this.mOpeningApps.size();
            if (bReturn || size > 0) {
                boolean configChanged = WindowManagerDebugConfig.DEBUG_ORIENTATION;
                Slog.d(TAG, "stopFreezingDisplayLocked: Returning mWaitingForConfig=" + this.mWaitingForConfig + ", mAppsFreezingScreen=" + this.mAppsFreezingScreen + ", mWindowsFreezingScreen=" + this.mWindowsFreezingScreen + ", mClientFreezingScreen=" + this.mClientFreezingScreen + ", mOpeningApps.size()=" + this.mOpeningApps.size());
                if (!bReturn && size > 0) {
                    printFreezingDisplayLogs();
                }
                return;
            }
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(TAG, "stopFreezingDisplayLocked: Unfreezing now");
            }
            if (HwPCUtils.isPcCastModeInServer()) {
                this.mH.removeMessages(103);
            }
            DisplayContent displayContent = this.mRoot.getDisplayContent(this.mFrozenDisplayId);
            if (displayContent == null) {
                this.mH.removeMessages(17);
                this.mH.removeMessages(30);
                this.mInputMonitor.thawInputDispatchingLw();
                this.mH.removeMessages(15);
                this.mH.sendEmptyMessageDelayed(15, 2000);
                this.mScreenFrozenLock.release();
                this.mFrozenDisplayId = -1;
                this.mDisplayFrozen = false;
                Slog.w(TAG, "stopFreezingDisplayLocked: Attempted to updateRotation for non-exiting displayId = " + this.mFrozenDisplayId);
                return;
            }
            int displayId = this.mFrozenDisplayId;
            this.mFrozenDisplayId = -1;
            this.mDisplayFrozen = false;
            this.mInputMonitor.thawInputDispatchingLw();
            this.mLastDisplayFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
            StringBuilder sb = new StringBuilder(128);
            sb.append("Screen frozen for ");
            TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, sb);
            if (this.mLastFinishedFreezeSource != null) {
                sb.append(" due to ");
                sb.append(this.mLastFinishedFreezeSource);
            }
            Slog.i(TAG, sb.toString());
            this.mH.removeMessages(17);
            this.mH.removeMessages(30);
            boolean updateRotation = false;
            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
            if (screenRotationAnimation == null || !screenRotationAnimation.hasScreenshot()) {
                if (screenRotationAnimation != null) {
                    screenRotationAnimation.kill();
                    this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                }
                updateRotation = true;
            } else {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.i(TAG, "**** Dismissing screen rotation animation");
                }
                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                if (!this.mPolicy.validateRotationAnimationLw(this.mExitAnimId, this.mEnterAnimId, false)) {
                    this.mEnterAnimId = 0;
                    this.mExitAnimId = 0;
                }
                DisplayInfo displayInfo2 = displayInfo;
                if (screenRotationAnimation.dismiss(this.mTransaction, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight, this.mExitAnimId, this.mEnterAnimId)) {
                    this.mTransaction.apply();
                    scheduleAnimationLocked();
                } else {
                    screenRotationAnimation.kill();
                    this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                    updateRotation = true;
                }
            }
            boolean configChanged2 = updateOrientationFromAppTokensLocked(displayId);
            if (!sIsMygote) {
                this.mH.removeMessages(15);
                this.mH.sendEmptyMessageDelayed(15, 2000);
            }
            this.mScreenFrozenLock.release();
            if (updateRotation) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.d(TAG, "Performing post-rotate rotation");
                }
                configChanged2 |= displayContent.updateRotationUnchecked();
            }
            if (configChanged2) {
                this.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
            }
            this.mLatencyTracker.onActionEnd(6);
        }
    }

    static int getPropertyInt(String[] tokens, int index, int defUnits, int defDps, DisplayMetrics dm) {
        if (index < tokens.length) {
            String str = tokens[index];
            if (str != null && str.length() > 0) {
                try {
                    return Integer.parseInt(str);
                } catch (Exception e) {
                }
            }
        }
        if (defUnits == 0) {
            return defDps;
        }
        return (int) TypedValue.applyDimension(defUnits, (float) defDps, dm);
    }

    /* access modifiers changed from: package-private */
    public void createWatermarkInTransaction() {
        if (this.mWatermark == null) {
            FileInputStream in = null;
            DataInputStream ind = null;
            try {
                DataInputStream ind2 = new DataInputStream(new FileInputStream(new File("/system/etc/setup.conf")));
                String line = ind2.readLine();
                if (line != null) {
                    String[] toks = line.split("%");
                    if (toks != null && toks.length > 0) {
                        DisplayContent displayContent = getDefaultDisplayContentLocked();
                        this.mWatermark = new Watermark(displayContent, displayContent.mRealDisplayMetrics, toks);
                    }
                }
                try {
                    ind2.close();
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e2) {
                if (ind != null) {
                    ind.close();
                } else if (in != null) {
                    in.close();
                }
            } catch (IOException e3) {
                if (ind != null) {
                    ind.close();
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e5) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        }
    }

    public void setRecentsVisibility(boolean visible) {
        this.mAmInternal.enforceCallerIsRecentsOrHasPermission("android.permission.STATUS_BAR", "setRecentsVisibility()");
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.setRecentsVisibilityLw(visible);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setPipVisibility(boolean visible) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mPolicy.setPipVisibilityLw(visible);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    public void setShelfHeight(boolean visible, int shelfHeight) {
        this.mAmInternal.enforceCallerIsRecentsOrHasPermission("android.permission.STATUS_BAR", "setShelfHeight()");
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getPinnedStackController().setAdjustedForShelf(visible, shelfHeight);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void statusBarVisibilityChanged(int visibility) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    int diff = this.mLastStatusBarVisibility ^ visibility;
                    if (diff != 0) {
                        Flog.i(303, "statusBarVisibilityChanged,vis=" + Integer.toHexString(visibility) + ",diff=" + Integer.toHexString(diff));
                    }
                    this.mLastStatusBarVisibility = visibility;
                    int visibility2 = this.mPolicy.adjustSystemUiVisibilityLw(visibility);
                    if ((diff & 201326592) == 201326592 || (diff & 134217728) == 134217728 || (this.mLastStatusBarVisibility & 2) != 0) {
                        getDefaultDisplayContentLocked().pendingLayoutChanges |= 1;
                        this.mRoot.performSurfacePlacement(false);
                    }
                    updateStatusBarVisibilityLocked(visibility2);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    public void setNavBarVirtualKeyHapticFeedbackEnabled(boolean enabled) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mPolicy.setNavBarVirtualKeyHapticFeedbackEnabledLw(enabled);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
    }

    private boolean updateStatusBarVisibilityLocked(int visibility) {
        if (this.mLastDispatchedSystemUiVisibility == visibility) {
            return false;
        }
        int globalDiff = (this.mLastDispatchedSystemUiVisibility ^ visibility) & 7 & (~visibility);
        this.mLastDispatchedSystemUiVisibility = visibility;
        this.mInputManager.setSystemUiVisibility(visibility);
        getDefaultDisplayContentLocked().updateSystemUiVisibility(visibility, globalDiff);
        return true;
    }

    public void reevaluateStatusBarVisibility() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (updateStatusBarVisibilityLocked(this.mPolicy.adjustSystemUiVisibilityLw(this.mLastStatusBarVisibility))) {
                    this.mWindowPlacerLocked.requestTraversal();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public int getNavBarPosition() {
        int navBarPosition;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().performLayout(false, false);
                navBarPosition = this.mPolicy.getNavBarPosition();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return navBarPosition;
    }

    public WindowManagerPolicy.InputConsumer createInputConsumer(Looper looper, String name, InputEventReceiver.Factory inputEventReceiverFactory) {
        WindowManagerPolicy.InputConsumer createInputConsumer;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                createInputConsumer = this.mInputMonitor.createInputConsumer(looper, name, inputEventReceiverFactory);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return createInputConsumer;
    }

    public void createInputConsumer(IBinder token, String name, InputChannel inputChannel) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mInputMonitor.createInputConsumer(token, name, inputChannel, Binder.getCallingPid(), Binder.getCallingUserHandle());
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean destroyInputConsumer(String name) {
        boolean destroyInputConsumer;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                destroyInputConsumer = this.mInputMonitor.destroyInputConsumer(name);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return destroyInputConsumer;
    }

    public Region getCurrentImeTouchRegion() {
        Region r;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.RESTRICTED_VR_ACCESS") == 0) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    r = new Region();
                    if (this.mInputMethodWindow != null) {
                        this.mInputMethodWindow.getTouchableRegion(r);
                    }
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return r;
        }
        throw new SecurityException("getCurrentImeTouchRegion is restricted to VR services");
    }

    public boolean hasNavigationBar() {
        return this.mPolicy.hasNavigationBar();
    }

    public void lockNow(Bundle options) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WINDOWNMANAGER_LOCKNOW);
        this.mPolicy.lockNow(options);
    }

    public void showRecentApps() {
        this.mPolicy.showRecentApps();
    }

    public boolean isSafeModeEnabled() {
        return this.mSafeMode;
    }

    public boolean clearWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "clearWindowContentFrameStats()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        resetPriorityAfterLockedSection();
                        return false;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean clearWindowContentFrameStats = surfaceController.clearWindowContentFrameStats();
                    resetPriorityAfterLockedSection();
                    return clearWindowContentFrameStats;
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        } else {
            throw new SecurityException("Requires FRAME_STATS permission");
        }
    }

    public WindowContentFrameStats getWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "getWindowContentFrameStats()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                    if (this.mTempWindowRenderStats == null) {
                        this.mTempWindowRenderStats = new WindowContentFrameStats();
                    }
                    WindowContentFrameStats stats = this.mTempWindowRenderStats;
                    if (!surfaceController.getWindowContentFrameStats(stats)) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                    resetPriorityAfterLockedSection();
                    return stats;
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        } else {
            throw new SecurityException("Requires FRAME_STATS permission");
        }
    }

    public void notifyAppRelaunching(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.startRelaunching();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void notifyAppRelaunchingFinished(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.finishRelaunching();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void notifyAppRelaunchesCleared(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.clearRelaunching();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void notifyAppResumedFinished(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    this.mUnknownAppVisibilityController.notifyAppResumedFinished(appWindow);
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void notifyTaskRemovedFromRecents(int taskId, int userId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mTaskSnapshotController.notifyTaskRemovedFromRecents(taskId, userId);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public int getDockedDividerInsetsLw() {
        return getDefaultDisplayContentLocked().getDockedDividerController().getContentInsets();
    }

    private void dumpPolicyLocked(PrintWriter pw, String[] args, boolean dumpAll) {
        pw.println("WINDOW MANAGER POLICY STATE (dumpsys window policy)");
        this.mPolicy.dump("    ", pw, args);
    }

    private void dumpAnimatorLocked(PrintWriter pw, String[] args, boolean dumpAll) {
        pw.println("WINDOW MANAGER ANIMATOR STATE (dumpsys window animator)");
        this.mAnimator.dumpLocked(pw, "    ", dumpAll);
    }

    private void dumpTokensLocked(PrintWriter pw, boolean dumpAll) {
        pw.println("WINDOW MANAGER TOKENS (dumpsys window tokens)");
        this.mRoot.dumpTokens(pw, dumpAll);
        if (!this.mOpeningApps.isEmpty() || !this.mClosingApps.isEmpty()) {
            pw.println();
            if (this.mOpeningApps.size() > 0) {
                pw.print("  mOpeningApps=");
                pw.println(this.mOpeningApps);
            }
            if (this.mClosingApps.size() > 0) {
                pw.print("  mClosingApps=");
                pw.println(this.mClosingApps);
            }
        }
    }

    private void dumpSessionsLocked(PrintWriter pw, boolean dumpAll) {
        pw.println("WINDOW MANAGER SESSIONS (dumpsys window sessions)");
        for (int i = 0; i < this.mSessions.size(); i++) {
            Session s = this.mSessions.valueAt(i);
            pw.print("  Session ");
            pw.print(s);
            pw.println(':');
            s.dump(pw, "    ");
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProtoLocked(ProtoOutputStream proto, boolean trim) {
        this.mPolicy.writeToProto(proto, 1146756268033L);
        this.mRoot.writeToProto(proto, 1146756268034L, trim);
        if (this.mCurrentFocus != null) {
            this.mCurrentFocus.writeIdentifierToProto(proto, 1146756268035L);
        }
        if (this.mFocusedApp != null) {
            this.mFocusedApp.writeNameToProto(proto, 1138166333444L);
        }
        if (this.mInputMethodWindow != null) {
            this.mInputMethodWindow.writeIdentifierToProto(proto, 1146756268037L);
        }
        proto.write(1133871366150L, this.mDisplayFrozen);
        DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
        proto.write(1120986464263L, defaultDisplayContent.getRotation());
        proto.write(1120986464264L, defaultDisplayContent.getLastOrientation());
        this.mAppTransition.writeToProto(proto, 1146756268041L);
    }

    /* access modifiers changed from: package-private */
    public void traceStateLocked(String where) {
        Trace.traceBegin(32, "traceStateLocked");
        try {
            this.mWindowTracing.traceStateLocked(where, this);
        } catch (Exception e) {
            Log.wtf(TAG, "Exception while tracing state", e);
        } catch (Throwable th) {
            Trace.traceEnd(32);
            throw th;
        }
        Trace.traceEnd(32);
    }

    private void dumpHandlerLocked(PrintWriter pw) {
        pw.println("  mHandler:");
        this.mH.dump(new PrintWriterPrinter(pw), "    ");
    }

    private void setWmsDebugFlag(PrintWriter pw, String cmd) {
        if (cmd.equals(DEBUG_FOCUS_CMD)) {
            WindowManagerDebugConfig.DEBUG_FOCUS = true;
            pw.println("Set DEBUG_FOCUS flag on");
        } else if (cmd.equals(DEBUG_VISIBILITY_CMD)) {
            WindowManagerDebugConfig.DEBUG_VISIBILITY = true;
            WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON = true;
            pw.println("Set DEBUG_VISIBILITY flag on");
        } else if (cmd.equals(DEBUG_LAYERS_CMD)) {
            WindowManagerDebugConfig.DEBUG_LAYERS = true;
            pw.println("Set DEBUG_LAYERS flag on");
        } else if (cmd.equals(DEBUG_ORIENTATION_CMD)) {
            WindowManagerDebugConfig.DEBUG_ORIENTATION = true;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = true;
            pw.println("Set DEBUG_ORIENTATION, DEBUG_APP_ORIENTATION flag on");
        } else if (cmd.equals(DEBUG_APP_TRANSITIONS_CMD)) {
            WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS = true;
            pw.println("Set DEBUG_APP_TRANSITIONS flag on");
        } else if (cmd.equals(DEBUG_CONFIGURATION_CMD)) {
            WindowManagerDebugConfig.DEBUG_CONFIGURATION = true;
            pw.println("Set DEBUG_CONFIGURATION flag on");
        } else if (cmd.equals(DEBUG_SCREEN_ON_CMD)) {
            WindowManagerDebugConfig.DEBUG_SCREEN_ON = true;
            pw.println("Set DEBUG_SCREEN_ON flag on");
        } else if (cmd.equals(DEBUG_WALLPAPER_CMD)) {
            WindowManagerDebugConfig.DEBUG_WALLPAPER = true;
            WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT = true;
            pw.println("Set DEBUG_WALLPAPER flag on");
        } else if (cmd.equals(DEBUG_INPUT_CMD)) {
            WindowManagerDebugConfig.DEBUG_INPUT_METHOD = true;
            WindowManagerDebugConfig.DEBUG_INPUT = true;
            pw.println("Set DEBUG_INPUT flag on");
        } else if (cmd.equals(DEBUG_LAYOUT_CMD)) {
            WindowManagerDebugConfig.DEBUG_LAYOUT = true;
            pw.println("Set DEBUG_LAYOUT flag on");
        } else if (cmd.equals(DEBUG_DISPLAY_CMD)) {
            WindowManagerDebugConfig.DEBUG_DISPLAY = true;
            pw.println("Set DEBUG_DISPLAY flag on");
        } else if (cmd.equals(DEBUG_STARTING_WINDOW_CMD)) {
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE = true;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW = true;
            pw.println("Set DEBUG_STARTING_WINDOW flag on");
        } else if (cmd.equals(DEBUG_ALL_ON_CMD)) {
            WindowManagerDebugConfig.DEBUG_FOCUS = true;
            WindowManagerDebugConfig.DEBUG_VISIBILITY = true;
            WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON = true;
            WindowManagerDebugConfig.DEBUG_LAYERS = true;
            WindowManagerDebugConfig.DEBUG_ORIENTATION = true;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = true;
            WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS = true;
            WindowManagerDebugConfig.DEBUG_CONFIGURATION = true;
            WindowManagerDebugConfig.DEBUG_SCREEN_ON = true;
            WindowManagerDebugConfig.DEBUG_WALLPAPER = true;
            WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT = true;
            WindowManagerDebugConfig.DEBUG_INPUT_METHOD = true;
            WindowManagerDebugConfig.DEBUG_INPUT = true;
            WindowManagerDebugConfig.DEBUG_LAYOUT = true;
            WindowManagerDebugConfig.DEBUG_DISPLAY = true;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE = true;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW = true;
            pw.println("Set wms debug flag allOn");
        } else if (cmd.equals(DEBUG_ALL_OFF_CMD)) {
            WindowManagerDebugConfig.DEBUG_FOCUS = false;
            WindowManagerDebugConfig.DEBUG_VISIBILITY = false;
            WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON = false;
            WindowManagerDebugConfig.DEBUG_LAYERS = false;
            WindowManagerDebugConfig.DEBUG_ORIENTATION = false;
            WindowManagerDebugConfig.DEBUG_APP_ORIENTATION = false;
            WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS = false;
            WindowManagerDebugConfig.DEBUG_CONFIGURATION = false;
            WindowManagerDebugConfig.DEBUG_SCREEN_ON = false;
            WindowManagerDebugConfig.DEBUG_WALLPAPER = false;
            WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT = false;
            WindowManagerDebugConfig.DEBUG_INPUT_METHOD = false;
            WindowManagerDebugConfig.DEBUG_INPUT = false;
            WindowManagerDebugConfig.DEBUG_LAYOUT = false;
            WindowManagerDebugConfig.DEBUG_DISPLAY = false;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE = false;
            WindowManagerDebugConfig.DEBUG_STARTING_WINDOW = false;
            pw.println("Set wms debug flag allOff");
        } else {
            pw.println("Bad window command.");
        }
    }

    private void dumpWindowsLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        pw.println("WINDOW MANAGER WINDOWS (dumpsys window windows)");
        dumpWindowsNoHeaderLocked(pw, dumpAll, windows);
    }

    private void dumpWindowsNoHeaderLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        this.mRoot.dumpWindowsNoHeader(pw, dumpAll, windows);
        if (!this.mHidingNonSystemOverlayWindows.isEmpty()) {
            pw.println();
            pw.println("  Hiding System Alert Windows:");
            for (int i = this.mHidingNonSystemOverlayWindows.size() - 1; i >= 0; i--) {
                WindowState w = this.mHidingNonSystemOverlayWindows.get(i);
                pw.print("  #");
                pw.print(i);
                pw.print(' ');
                pw.print(w);
                if (dumpAll) {
                    pw.println(":");
                    w.dump(pw, "    ", true);
                } else {
                    pw.println();
                }
            }
        }
        if (this.mPendingRemove.size() > 0) {
            pw.println();
            pw.println("  Remove pending for:");
            for (int i2 = this.mPendingRemove.size() - 1; i2 >= 0; i2--) {
                WindowState w2 = this.mPendingRemove.get(i2);
                if (windows == null || windows.contains(w2)) {
                    pw.print("  Remove #");
                    pw.print(i2);
                    pw.print(' ');
                    pw.print(w2);
                    if (dumpAll) {
                        pw.println(":");
                        w2.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        if (this.mForceRemoves != null && this.mForceRemoves.size() > 0) {
            pw.println();
            pw.println("  Windows force removing:");
            for (int i3 = this.mForceRemoves.size() - 1; i3 >= 0; i3--) {
                WindowState w3 = this.mForceRemoves.get(i3);
                pw.print("  Removing #");
                pw.print(i3);
                pw.print(' ');
                pw.print(w3);
                if (dumpAll) {
                    pw.println(":");
                    w3.dump(pw, "    ", true);
                } else {
                    pw.println();
                }
            }
        }
        if (this.mDestroySurface.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to destroy their surface:");
            for (int i4 = this.mDestroySurface.size() - 1; i4 >= 0; i4--) {
                WindowState w4 = this.mDestroySurface.get(i4);
                if (windows == null || windows.contains(w4)) {
                    pw.print("  Destroy #");
                    pw.print(i4);
                    pw.print(' ');
                    pw.print(w4);
                    if (dumpAll) {
                        pw.println(":");
                        w4.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        if (this.mLosingFocus.size() > 0) {
            pw.println();
            pw.println("  Windows losing focus:");
            for (int i5 = this.mLosingFocus.size() - 1; i5 >= 0; i5--) {
                WindowState w5 = this.mLosingFocus.get(i5);
                if (windows == null || windows.contains(w5)) {
                    pw.print("  Losing #");
                    pw.print(i5);
                    pw.print(' ');
                    pw.print(w5);
                    if (dumpAll) {
                        pw.println(":");
                        w5.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        if (this.mResizingWindows.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to resize:");
            for (int i6 = this.mResizingWindows.size() - 1; i6 >= 0; i6--) {
                WindowState w6 = this.mResizingWindows.get(i6);
                if (windows == null || windows.contains(w6)) {
                    pw.print("  Resizing #");
                    pw.print(i6);
                    pw.print(' ');
                    pw.print(w6);
                    if (dumpAll) {
                        pw.println(":");
                        w6.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
        }
        if (this.mWaitingForDrawn.size() > 0) {
            pw.println();
            pw.println("  Clients waiting for these windows to be drawn:");
            for (int i7 = this.mWaitingForDrawn.size() - 1; i7 >= 0; i7--) {
                pw.print("  Waiting #");
                pw.print(i7);
                pw.print(' ');
                pw.print(this.mWaitingForDrawn.get(i7));
            }
        }
        pw.println();
        pw.print("  mGlobalConfiguration=");
        pw.println(this.mRoot.getConfiguration());
        pw.print("  mHasPermanentDpad=");
        pw.println(this.mHasPermanentDpad);
        pw.print("  mCurrentFocus=");
        pw.println(this.mCurrentFocus);
        if (this.mLastFocus != this.mCurrentFocus) {
            pw.print("  mLastFocus=");
            pw.println(this.mLastFocus);
        }
        pw.print("  mFocusedApp=");
        pw.println(this.mFocusedApp);
        if (this.mInputMethodTarget != null) {
            pw.print("  mInputMethodTarget=");
            pw.println(this.mInputMethodTarget);
        }
        pw.print("  mInTouchMode=");
        pw.println(this.mInTouchMode);
        pw.print("  mLastDisplayFreezeDuration=");
        TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, pw);
        if (this.mLastFinishedFreezeSource != null) {
            pw.print(" due to ");
            pw.print(this.mLastFinishedFreezeSource);
        }
        pw.println();
        pw.print("  mLastWakeLockHoldingWindow=");
        pw.print(this.mLastWakeLockHoldingWindow);
        pw.print(" mLastWakeLockObscuringWindow=");
        pw.print(this.mLastWakeLockObscuringWindow);
        pw.println();
        this.mInputMonitor.dump(pw, "  ");
        this.mUnknownAppVisibilityController.dump(pw, "  ");
        this.mTaskSnapshotController.dump(pw, "  ");
        if (dumpAll) {
            pw.print("  mSystemDecorLayer=");
            pw.print(this.mSystemDecorLayer);
            pw.print(" mScreenRect=");
            pw.println(this.mScreenRect.toShortString());
            if (this.mLastStatusBarVisibility != 0) {
                pw.print("  mLastStatusBarVisibility=0x");
                pw.println(Integer.toHexString(this.mLastStatusBarVisibility));
            }
            if (this.mInputMethodWindow != null) {
                pw.print("  mInputMethodWindow=");
                pw.println(this.mInputMethodWindow);
            }
            this.mWindowPlacerLocked.dump(pw, "  ");
            this.mRoot.mWallpaperController.dump(pw, "  ");
            pw.print("  mSystemBooted=");
            pw.print(this.mSystemBooted);
            pw.print(" mDisplayEnabled=");
            pw.println(this.mDisplayEnabled);
            this.mRoot.dumpLayoutNeededDisplayIds(pw);
            pw.print("  mTransactionSequence=");
            pw.println(this.mTransactionSequence);
            pw.print("  mDisplayFrozen=");
            pw.print(this.mDisplayFrozen);
            pw.print(" windows=");
            pw.print(this.mWindowsFreezingScreen);
            pw.print(" client=");
            pw.print(this.mClientFreezingScreen);
            pw.print(" apps=");
            pw.print(this.mAppsFreezingScreen);
            pw.print(" waitingForConfig=");
            pw.println(this.mWaitingForConfig);
            DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
            pw.print("  mRotation=");
            pw.print(defaultDisplayContent.getRotation());
            pw.print(" mAltOrientation=");
            pw.println(defaultDisplayContent.getAltOrientation());
            pw.print("  mLastWindowForcedOrientation=");
            pw.print(defaultDisplayContent.getLastWindowForcedOrientation());
            pw.print(" mLastOrientation=");
            pw.println(defaultDisplayContent.getLastOrientation());
            pw.print("  mDeferredRotationPauseCount=");
            pw.println(this.mDeferredRotationPauseCount);
            pw.print("  Animation settings: disabled=");
            pw.print(this.mAnimationsDisabled);
            pw.print(" window=");
            pw.print(this.mWindowAnimationScaleSetting);
            pw.print(" transition=");
            pw.print(this.mTransitionAnimationScaleSetting);
            pw.print(" animator=");
            pw.println(this.mAnimatorDurationScaleSetting);
            pw.print("  mSkipAppTransitionAnimation=");
            pw.println(this.mSkipAppTransitionAnimation);
            pw.println("  mLayoutToAnim:");
            this.mAppTransition.dump(pw, "    ");
            if (this.mRecentsAnimationController != null) {
                pw.print("  mRecentsAnimationController=");
                pw.println(this.mRecentsAnimationController);
                this.mRecentsAnimationController.dump(pw, "    ");
            }
        }
    }

    private boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<WindowState> windows = new ArrayList<>();
        if ("apps".equals(name) || "visible".equals(name) || "visible-apps".equals(name)) {
            boolean appsOnly = name.contains("apps");
            boolean visibleOnly = name.contains("visible");
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (appsOnly) {
                        this.mRoot.dumpDisplayContents(pw);
                    }
                    this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(visibleOnly, appsOnly, windows) {
                        private final /* synthetic */ boolean f$0;
                        private final /* synthetic */ boolean f$1;
                        private final /* synthetic */ ArrayList f$2;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void accept(Object obj) {
                            WindowManagerService.lambda$dumpWindows$5(this.f$0, this.f$1, this.f$2, (WindowState) obj);
                        }
                    }, true);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
        } else {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.getWindowsByName(windows, name);
                } catch (Throwable th2) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th2;
                    }
                }
            }
            resetPriorityAfterLockedSection();
        }
        if (windows.size() <= 0) {
            return false;
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                dumpWindowsLocked(pw, dumpAll, windows);
            } catch (Throwable th3) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th3;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return true;
    }

    static /* synthetic */ void lambda$dumpWindows$5(boolean visibleOnly, boolean appsOnly, ArrayList windows, WindowState w) {
        if (visibleOnly && !w.mWinAnimator.getShown()) {
            return;
        }
        if (!appsOnly || w.mAppToken != null) {
            windows.add(w);
        }
    }

    private void dumpLastANRLocked(PrintWriter pw) {
        pw.println("WINDOW MANAGER LAST ANR (dumpsys window lastanr)");
        if (this.mLastANRState == null) {
            pw.println("  <no ANR has occurred since boot>");
        } else {
            pw.println(this.mLastANRState);
        }
    }

    /* access modifiers changed from: package-private */
    public void saveANRStateLocked(AppWindowToken appWindowToken, WindowState windowState, String reason) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
        pw.println("  ANR time: " + DateFormat.getDateTimeInstance().format(new Date()));
        if (appWindowToken != null) {
            pw.println("  Application at fault: " + appWindowToken.stringName);
        }
        if (windowState != null) {
            pw.println("  Window at fault: " + windowState.mAttrs.getTitle());
        }
        if (reason != null) {
            pw.println("  Reason: " + reason);
        }
        if (!this.mWinAddedSinceNullFocus.isEmpty()) {
            pw.println("  Windows added since null focus: " + this.mWinAddedSinceNullFocus);
        }
        if (!this.mWinRemovedSinceNullFocus.isEmpty()) {
            pw.println("  Windows removed since null focus: " + this.mWinRemovedSinceNullFocus);
        }
        pw.println();
        dumpWindowsNoHeaderLocked(pw, true, null);
        pw.println();
        pw.println("Last ANR continued");
        this.mRoot.dumpDisplayContents(pw);
        pw.close();
        this.mLastANRState = sw.toString();
        this.mH.removeMessages(38);
        this.mH.sendEmptyMessageDelayed(38, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        PriorityDump.dump(this.mPriorityDumper, fd, pw, args);
    }

    /* access modifiers changed from: private */
    public void doDump(FileDescriptor fd, PrintWriter pw, String[] args, boolean useProto) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            boolean dumpAll = false;
            int opti = 0;
            while (opti < args.length) {
                String opt = args[opti];
                if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("-a".equals(opt)) {
                    dumpAll = true;
                } else if ("-h".equals(opt)) {
                    pw.println("Window manager dump options:");
                    pw.println("  [-a] [-h] [cmd] ...");
                    pw.println("  cmd may be one of:");
                    pw.println("    l[astanr]: last ANR information");
                    pw.println("    p[policy]: policy state");
                    pw.println("    a[animator]: animator state");
                    pw.println("    s[essions]: active sessions");
                    pw.println("    surfaces: active surfaces (debugging enabled only)");
                    pw.println("    d[isplays]: active display contents");
                    pw.println("    t[okens]: token list");
                    pw.println("    w[indows]: window list");
                    pw.println("    h[andler]: dump message queue");
                    if (IS_DEBUG_VERSION) {
                        pw.println("    hwDebugWms[Focus|Visibility|Layer|Orientation|Transition|Configuration|Screen|AllOn|AllOff]: turn wms debug flag on or off");
                    }
                    pw.println("  cmd may also be a NAME to dump windows.  NAME may");
                    pw.println("    be a partial substring in a window name, a");
                    pw.println("    Window hex object identifier, or");
                    pw.println("    \"all\" for all windows, or");
                    pw.println("    \"visible\" for the visible windows.");
                    pw.println("    \"visible-apps\" for the visible app windows.");
                    pw.println("  -a: include all available server state.");
                    pw.println("  --proto: output dump in protocol buffer format.");
                    return;
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
            if (useProto) {
                ProtoOutputStream proto = new ProtoOutputStream(fd);
                synchronized (this.mWindowMap) {
                    try {
                        boostPriorityForLockedSection();
                        writeToProtoLocked(proto, false);
                    } catch (Throwable th) {
                        while (true) {
                            resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                resetPriorityAfterLockedSection();
                proto.flush();
            } else if (opti < args.length) {
                String cmd = args[opti];
                int opti2 = opti + 1;
                if ("lastanr".equals(cmd) || "l".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpLastANRLocked(pw);
                        } catch (Throwable th2) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th2;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("policy".equals(cmd) || "p".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpPolicyLocked(pw, args, true);
                        } catch (Throwable th3) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th3;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("animator".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpAnimatorLocked(pw, args, true);
                        } catch (Throwable th4) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th4;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("sessions".equals(cmd) || "s".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpSessionsLocked(pw, true);
                        } catch (Throwable th5) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th5;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("displays".equals(cmd) || "d".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            this.mRoot.dumpDisplayContents(pw);
                        } catch (Throwable th6) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th6;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("tokens".equals(cmd) || "t".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpTokensLocked(pw, true);
                        } catch (Throwable th7) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th7;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("windows".equals(cmd) || "w".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpWindowsLocked(pw, true, null);
                        } catch (Throwable th8) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th8;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("all".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpWindowsLocked(pw, true, null);
                        } catch (Throwable th9) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th9;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("containers".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            this.mRoot.dumpChildrenNames(pw, " ");
                            pw.println(" ");
                            this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(pw) {
                                private final /* synthetic */ PrintWriter f$0;

                                {
                                    this.f$0 = r1;
                                }

                                public final void accept(Object obj) {
                                    this.f$0.println((WindowState) obj);
                                }
                            }, true);
                        } catch (Throwable th10) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th10;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if ("handler".equals(cmd) || "h".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpHandlerLocked(pw);
                        } catch (Throwable th11) {
                            while (true) {
                                resetPriorityAfterLockedSection();
                                throw th11;
                            }
                        }
                    }
                    resetPriorityAfterLockedSection();
                } else if (!IS_DEBUG_VERSION || TextUtils.isEmpty(cmd) || !cmd.startsWith(DEBUG_PREFIX)) {
                    if (!dumpWindows(pw, cmd, args, opti2, dumpAll)) {
                        pw.println("Bad window command, or no windows match: " + cmd);
                        pw.println("Use -h for help.");
                    }
                } else {
                    setWmsDebugFlag(pw, cmd);
                }
            } else {
                synchronized (this.mWindowMap) {
                    try {
                        boostPriorityForLockedSection();
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpLastANRLocked(pw);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpPolicyLocked(pw, args, dumpAll);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpAnimatorLocked(pw, args, dumpAll);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpSessionsLocked(pw, dumpAll);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        this.mRoot.dumpDisplayContents(pw);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpTokensLocked(pw, dumpAll);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpWindowsLocked(pw, dumpAll, null);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpHandlerLocked(pw);
                    } catch (Throwable th12) {
                        while (true) {
                            resetPriorityAfterLockedSection();
                            throw th12;
                        }
                    }
                }
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void monitor() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public DisplayContent getDefaultDisplayContentLocked() {
        return this.mRoot.getDisplayContent(0);
    }

    public void onDisplayAdded(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mDisplayManager.getDisplay(displayId) != null) {
                    displayReady(displayId);
                }
                this.mWindowPlacerLocked.requestTraversal();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void onDisplayRemoved(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAnimator.removeDisplayLocked(displayId);
                this.mWindowPlacerLocked.requestTraversal();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void onOverlayChanged() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.onOverlayChangedLw();
                getDefaultDisplayContentLocked().updateDisplayInfo();
                requestTraversal();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void onDisplayChanged(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContent(displayId);
                if (displayContent != null) {
                    displayContent.updateDisplayInfo();
                }
                this.mWindowPlacerLocked.requestTraversal();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public Object getWindowManagerLock() {
        return this.mWindowMap;
    }

    public void setWillReplaceWindow(IBinder token, boolean animate) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w(TAG, "Attempted to set replacing window on non-existing app token " + token);
                    resetPriorityAfterLockedSection();
                } else if (!appWindowToken.hasContentToDisplay()) {
                    Slog.w(TAG, "Attempted to set replacing window on app token with no content" + token);
                    resetPriorityAfterLockedSection();
                } else {
                    appWindowToken.setWillReplaceWindows(animate);
                    resetPriorityAfterLockedSection();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceWindows(IBinder token, boolean childrenOnly) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w(TAG, "Attempted to set replacing window on non-existing app token " + token);
                    resetPriorityAfterLockedSection();
                } else if (!appWindowToken.hasContentToDisplay()) {
                    Slog.w(TAG, "Attempted to set replacing window on app token with no content" + token);
                    resetPriorityAfterLockedSection();
                } else {
                    if (childrenOnly) {
                        appWindowToken.setWillReplaceChildWindows();
                    } else {
                        appWindowToken.setWillReplaceWindows(false);
                    }
                    scheduleClearWillReplaceWindows(token, true);
                    resetPriorityAfterLockedSection();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0036, code lost:
        return;
     */
    public void scheduleClearWillReplaceWindows(IBinder token, boolean replacing) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w(TAG, "Attempted to reset replacing window on non-existing app token " + token);
                    resetPriorityAfterLockedSection();
                } else if (replacing) {
                    scheduleWindowReplacementTimeouts(appWindowToken);
                } else {
                    appWindowToken.clearWillReplaceWindows();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleWindowReplacementTimeouts(AppWindowToken appWindowToken) {
        if (!this.mWindowReplacementTimeouts.contains(appWindowToken)) {
            this.mWindowReplacementTimeouts.add(appWindowToken);
        }
        this.mH.removeMessages(46);
        this.mH.sendEmptyMessageDelayed(46, 2000);
    }

    public int getDockedStackSide() {
        int dockSide;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                TaskStack dockedStack = getDefaultDisplayContentLocked().getSplitScreenPrimaryStackIgnoringVisibility();
                dockSide = dockedStack == null ? -1 : dockedStack.getDockSide();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
        return dockSide;
    }

    public void setDockedStackResizing(boolean resizing) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setResizing(resizing);
                requestTraversal();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setDockedStackDividerTouchRegion(Rect touchRegion) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setTouchRegion(touchRegion);
                setFocusTaskRegionLocked(null);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setResizeDimLayer(boolean visible, int targetWindowingMode, float alpha) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setResizeDimLayer(visible, targetWindowingMode, alpha);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setForceResizableTasks(boolean forceResizableTasks) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mForceResizableTasks = forceResizableTasks;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setSupportsPictureInPicture(boolean supportsPictureInPicture) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mSupportsPictureInPicture = supportsPictureInPicture;
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    static int dipToPixel(int dip, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(1, (float) dip, displayMetrics);
    }

    public void registerDockedStackListener(IDockedStackListener listener) {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerDockedStackListener()") || checkCallingPermission("huawei.android.permission.MULTIWINDOW_SDK", "registerDockedStackListener()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    getDefaultDisplayContentLocked().mDividerControllerLocked.registerDockedStackListener(listener);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
        }
    }

    public void registerPinnedStackListener(int displayId, IPinnedStackListener listener) {
        if (!checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerPinnedStackListener()") || !this.mSupportsPictureInPicture) {
            return;
        }
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.getDisplayContent(displayId).getPinnedStackController().registerPinnedStackListener(listener);
                } catch (Throwable th) {
                    while (true) {
                        resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        HwPCUtils.log(TAG, "ignore pinned stack listener in pad pc mode");
    }

    public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        try {
            WindowState focusedWindow = getFocusedWindow();
            if (focusedWindow != null && focusedWindow.mClient != null) {
                focusedWindow.mClient.requestAppKeyboardShortcuts(receiver, deviceId);
            }
        } catch (RemoteException e) {
        }
    }

    public void getStableInsets(int displayId, Rect outInsets) throws RemoteException {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getStableInsetsLocked(displayId, outInsets);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void getStableInsetsLocked(int displayId, Rect outInsets) {
        outInsets.setEmpty();
        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
        if (dc != null) {
            DisplayInfo di = dc.getDisplayInfo();
            this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, di.displayCutout, outInsets);
        }
    }

    /* access modifiers changed from: package-private */
    public void intersectDisplayInsetBounds(Rect display, Rect insets, Rect inOutBounds) {
        this.mTmpRect3.set(display);
        this.mTmpRect3.inset(insets);
        inOutBounds.intersect(this.mTmpRect3);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        boostPriorityForLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
        if (r9.mDragDropController.dragDropActiveLocked() == false) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r0 = windowForClientLocked((com.android.server.wm.Session) null, r10, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0033, code lost:
        if (r0 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0035, code lost:
        android.util.Slog.w(TAG, "Bad requesting window " + r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004b, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r4 = r0.getDisplayContent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        if (r4 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0056, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0057, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r5 = r4.getTouchableWinAtPointLocked(r1, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0060, code lost:
        if (r5 == r0) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0062, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0063, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0066, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r5.mClient.updatePointerIcon(r5.translateToWindowX(r1), r5.translateToWindowY(r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        android.util.Slog.w(TAG, "unable to update pointer icon");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0083, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0085, code lost:
        resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0088, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        r3 = r9.mWindowMap;
     */
    public void updatePointerIcon(IWindow client) {
        WindowHashMap windowHashMap;
        synchronized (this.mMousePositionTracker) {
            if (this.mMousePositionTracker.mLatestEventWasMouse) {
                float mouseX = this.mMousePositionTracker.mLatestMouseX;
                float mouseY = this.mMousePositionTracker.mLatestMouseY;
            } else {
                return;
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void restorePointerIconLocked(DisplayContent displayContent, float latestX, float latestY) {
        this.mMousePositionTracker.updatePosition(latestX, latestY);
        WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(latestX, latestY);
        if (windowUnderPointer != null) {
            try {
                windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(latestX), windowUnderPointer.translateToWindowY(latestY));
            } catch (RemoteException e) {
                Slog.w(TAG, "unable to restore pointer icon");
            }
        } else {
            InputManager.getInstance().setPointerIconType(1000);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateTapExcludeRegion(IWindow client, int regionId, int left, int top, int width, int height) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState callingWin = windowForClientLocked((Session) null, client, false);
                if (callingWin == null) {
                    Slog.w(TAG, "Bad requesting window " + client);
                    resetPriorityAfterLockedSection();
                    return;
                }
                callingWin.updateTapExcludeRegion(regionId, left, top, width, height);
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void dontOverrideDisplayInfo(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent dc = getDisplayContentOrCreate(displayId);
                if (dc != null) {
                    dc.mShouldOverrideDisplayConfiguration = false;
                    this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, null);
                } else {
                    throw new IllegalArgumentException("Trying to register a non existent display.");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutKeyReceiver) throws RemoteException {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerShortcutKey")) {
            this.mPolicy.registerShortcutKey(shortcutCode, shortcutKeyReceiver);
            return;
        }
        throw new SecurityException("Requires REGISTER_WINDOW_MANAGER_LISTENERS permission");
    }

    public void requestUserActivityNotification() {
        if (checkCallingPermission("android.permission.USER_ACTIVITY", "requestUserActivityNotification()")) {
            this.mPolicy.requestUserActivityNotification();
            return;
        }
        throw new SecurityException("Requires USER_ACTIVITY permission");
    }

    /* access modifiers changed from: package-private */
    public void markForSeamlessRotation(WindowState w, boolean seamlesslyRotated) {
        if (seamlesslyRotated != w.mSeamlesslyRotated) {
            w.mSeamlesslyRotated = seamlesslyRotated;
            if (seamlesslyRotated) {
                this.mSeamlessRotationCount++;
            } else {
                this.mSeamlessRotationCount--;
            }
            if (this.mSeamlessRotationCount == 0) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.i(TAG, "Performing post-rotate rotation after seamless rotation");
                }
                finishSeamlessRotation();
                DisplayContent displayContent = w.getDisplayContent();
                if (displayContent.updateRotationUnchecked()) {
                    this.mH.obtainMessage(18, Integer.valueOf(displayContent.getDisplayId())).sendToTarget();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerAppFreezeListener(AppFreezeListener listener) {
        if (!this.mAppFreezeListeners.contains(listener)) {
            this.mAppFreezeListeners.add(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterAppFreezeListener(AppFreezeListener listener) {
        this.mAppFreezeListeners.remove(listener);
    }

    /* access modifiers changed from: protected */
    public boolean canBeFloatImeTarget(WindowState w) {
        int fl = w.mAttrs.flags & 131080;
        if (fl == 0 || fl == 131080 || w.mAttrs.type == 3) {
            return w.isVisibleOrAdding();
        }
        return false;
    }

    private void printFreezingDisplayLogs() {
        int appsCount = this.mOpeningApps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = this.mOpeningApps.valueAt(i);
            StringBuilder sb = new StringBuilder();
            sb.append("printFreezingDisplayLogs");
            sb.append("opening app wtoken = " + wtoken.toString() + ", allDrawn= " + wtoken.allDrawn + ", startingDisplayed =  " + wtoken.startingDisplayed + ", startingMoved =  " + wtoken.startingMoved + ", isRelaunching =  " + wtoken.isRelaunching());
            Slog.d(TAG, sb.toString());
        }
    }

    /* access modifiers changed from: private */
    public boolean isPrintAllWindowsDrawnLogs() {
        if (SystemClock.elapsedRealtime() - this.mWaitAllWindowDrawStartTime > 1000) {
            return true;
        }
        return false;
    }

    private boolean isLandScapeMultiWindowMode() {
        int dockSide = getDockedStackSide();
        int rot = getDefaultDisplayContentLocked().getDisplay().getRotation();
        return (rot == 1 || rot == 3) && dockSide != -1;
    }

    public void inSurfaceTransaction(Runnable exec) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                SurfaceControl.openTransaction();
                exec.run();
                SurfaceControl.closeTransaction();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
                throw th;
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void disableNonVrUi(boolean disable) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                boolean showAlertWindowNotifications = !disable;
                if (showAlertWindowNotifications == this.mShowAlertWindowNotifications) {
                    resetPriorityAfterLockedSection();
                    return;
                }
                this.mShowAlertWindowNotifications = showAlertWindowNotifications;
                for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                    this.mSessions.valueAt(i).setShowingAlertWindowNotificationAllowed(this.mShowAlertWindowNotifications);
                }
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasWideColorGamutSupport() {
        return this.mHasWideColorGamutSupport && SystemProperties.getInt("persist.sys.sf.native_mode", 0) != 1;
    }

    /* access modifiers changed from: package-private */
    public void updateNonSystemOverlayWindowsVisibilityIfNeeded(WindowState win, boolean surfaceShown) {
        if (win.hideNonSystemOverlayWindowsWhenVisible() || this.mHidingNonSystemOverlayWindows.contains(win)) {
            boolean systemAlertWindowsHidden = !this.mHidingNonSystemOverlayWindows.isEmpty();
            if (!surfaceShown) {
                this.mHidingNonSystemOverlayWindows.remove(win);
            } else if (!this.mHidingNonSystemOverlayWindows.contains(win)) {
                this.mHidingNonSystemOverlayWindows.add(win);
            }
            boolean hideSystemAlertWindows = !this.mHidingNonSystemOverlayWindows.isEmpty();
            if (systemAlertWindowsHidden != hideSystemAlertWindows) {
                this.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(hideSystemAlertWindows) {
                    private final /* synthetic */ boolean f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void accept(Object obj) {
                        ((WindowState) obj).setForceHideNonSystemOverlayWindowIfNeeded(this.f$0);
                    }
                }, false);
            }
        }
    }

    public void applyMagnificationSpec(MagnificationSpec spec) {
        getDefaultDisplayContentLocked().applyMagnificationSpec(spec);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeSurfaceBuilder(SurfaceSession s) {
        return this.mSurfaceBuilderFactory.make(s);
    }

    /* access modifiers changed from: package-private */
    public void sendSetRunningRemoteAnimation(int pid, boolean runningRemoteAnimation) {
        this.mH.obtainMessage(59, pid, runningRemoteAnimation).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void startSeamlessRotation() {
        this.mSeamlessRotationCount = 0;
        this.mRotatingSeamlessly = true;
    }

    /* access modifiers changed from: package-private */
    public void finishSeamlessRotation() {
        this.mRotatingSeamlessly = false;
    }

    public void onLockTaskStateChanged(int lockTaskState) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.onLockTaskStateChangedLw(lockTaskState);
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void setAodShowing(boolean aodShowing) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mPolicy.setAodShowing(aodShowing)) {
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
            } catch (Throwable th) {
                while (true) {
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.wm.WindowManagerService$HwInnerWindowManagerService, android.os.IBinder] */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
        this.mHwWMSEx.reevaluateStatusBarSize(layoutNaviBar);
    }

    public Configuration getCurNaviConfiguration() {
        return this.mHwWMSEx.getCurNaviConfiguration();
    }

    public IHwWindowManagerServiceEx getWindowManagerServiceEx() {
        return this.mHwWMSEx;
    }

    public WindowState getInputMethodWindow() {
        return this.mInputMethodWindow;
    }

    public WindowSurfacePlacer getWindowSurfacePlacer() {
        return this.mWindowPlacerLocked;
    }

    public H getWindowMangerServiceHandler() {
        return this.mH;
    }

    public HwWMDAMonitorProxy getWMMonitor() {
        return this.mWMProxy;
    }

    public WindowHashMap getWindowMap() {
        return this.mWindowMap;
    }

    public TaskSnapshotController getTaskSnapshotController() {
        return this.mTaskSnapshotController;
    }

    public AppWindowToken getFocusedAppWindowToken() {
        return this.mFocusedApp;
    }

    public AppOpsManager getAppOps() {
        return this.mAppOps;
    }

    public WindowManagerPolicy getPolicy() {
        return this.mPolicy;
    }

    public RootWindowContainer getRoot() {
        return this.mRoot;
    }

    public void getAppDisplayRect(float appMaxRatio, Rect rect, int left) {
        if (this.mHwWMSEx != null) {
            this.mHwWMSEx.getAppDisplayRect(appMaxRatio, rect, left, getDefaultDisplayContentLocked().getRotation());
        }
    }

    public float getDeviceMaxRatio() {
        if (this.mHwWMSEx != null) {
            return this.mHwWMSEx.getDeviceMaxRatio();
        }
        return 0.0f;
    }

    public int getLazyMode() {
        return this.mHwWMSEx.getLazyModeEx();
    }

    public void setLazyMode(int lazyMode) {
        this.mHwWMSEx.setLazyModeEx(lazyMode);
    }

    public void notifyFingerWinCovered(boolean covered, Rect frame) {
        if (this.mHwWMSEx != null) {
            this.mHwWMSEx.notifyFingerWinCovered(covered, frame);
        }
    }

    public WindowManagerService getService() {
        return this;
    }

    public void freezeOrThawRotation(int rotation) {
        if (this.mHwWMSEx == null) {
            return;
        }
        if (checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
            this.mHwWMSEx.freezeOrThawRotation(rotation);
            updateRotationUnchecked(false, false);
            return;
        }
        throw new SecurityException("Requires SET_ORIENTATION permission");
    }

    public void showWallpaperIfNeed(WindowState w) {
        this.mHwWMSEx.showWallpaperIfNeed(w);
    }

    public void prepareForForceRotation(IBinder token, String packageName, int pid, String processName) {
        this.mHwWMSEx.prepareForForceRotation(token, packageName, pid, processName);
    }

    /* access modifiers changed from: protected */
    public boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
        return this.mHwWMSEx.checkAppOrientationForForceRotation(aToken);
    }

    /* access modifiers changed from: protected */
    public boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
        return this.mHwWMSEx.isDisplayOkForAnimation(width, height, transit, atoken);
    }

    public void layoutWindowForPadPCMode(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        WindowManagerPolicy.WindowState windowState = win;
        if (windowState instanceof WindowState) {
            this.mHwWMSEx.layoutWindowForPadPCMode((WindowState) windowState, this.mInputMethodTarget, this.mInputMethodWindow, pf, df, cf, vf, contentBottom);
        }
    }

    public boolean isInDisplayFrozen() {
        return this.mDisplayFrozen;
    }

    public InputManagerService getInputManager() {
        return this.mInputManager;
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
        this.mHwWMSEx.setForcedDisplayDensityAndSize(displayId, density, width, height);
    }

    public WindowAnimator getWindowAnimator() {
        return this.mAnimator;
    }

    public DisplayManagerInternal getDisplayManagerInternal() {
        return this.mDisplayManagerInternal;
    }

    public Point updateLazyModePoint(int type, Point point) {
        return this.mHwWMSEx.updateLazyModePoint(type, point);
    }

    public float getLazyModeScale() {
        return this.mHwWMSEx.getLazyModeScale();
    }

    public Rect getFocuseWindowVisibleFrame() {
        return this.mHwWMSEx.getFocuseWindowVisibleFrame(this);
    }

    public boolean isPendingLock() {
        return this.mPolicy.isPendingLock();
    }

    public boolean isInSubFoldScaleMode() {
        return getFoldDisplayMode() == 3 && !this.mIsFullInSubFoldMode;
    }

    public int getFoldDisplayMode() {
        return this.mCurrentFoldDisplayMode;
    }
}
