package com.android.server.wm;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.animation.AnimationHandler;
import android.animation.ValueAnimator;
import android.annotation.IntDef;
import android.app.ActivityManager;
import android.app.ActivityManager.TaskSnapshot;
import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.input.InputManager;
import android.iawareperf.UniPerf;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.pc.IHwPCManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.rms.HwSysResource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.util.Xml;
import android.view.AppTransitionAnimationSpec;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDockedStackListener;
import android.view.IInputFilter;
import android.view.IOnKeyguardExitResult;
import android.view.IPinnedStackListener;
import android.view.IRotationWatcher;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.WindowManagerInternal.MagnificationCallbacks;
import android.view.WindowManagerInternal.OnHardKeyboardStatusChangeListener;
import android.view.WindowManagerInternal.WindowsForAccessibilityCallback;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.ScreenOffListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.WindowManagerPolicyThread;
import com.android.server.AbsLocationManagerService;
import com.android.server.AnimationThread;
import com.android.server.DisplayThread;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwWindowManagerService;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.input.InputManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.ShutdownThread;
import com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass8;
import com.huawei.android.view.IHwWindowManager.Stub;
import com.huawei.cust.HwCustUtils;
import com.huawei.pgmng.log.LogPower;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WindowManagerService extends AbsWindowManagerService implements IHwWindowManagerInner, Monitor, WindowManagerFuncs {
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
    static final long DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS = 5000000000L;
    private static final String DENSITY_OVERRIDE = "ro.config.density_override";
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    static final boolean HISI_PERF_OPT = SystemProperties.getBoolean("build.hisi_perf_opt", false);
    static final boolean HWFLOW = true;
    private static final int INPUT_DEVICES_READY_FOR_SAFE_MODE_DETECTION_TIMEOUT_MILLIS = 1000;
    static final boolean IS_DEBUG_VERSION;
    protected static final boolean IS_NOTCH_PROP;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    static final int LAYER_OFFSET_DIM = 1;
    static final int LAYER_OFFSET_THUMBNAIL = 4;
    static final int LAYOUT_REPEAT_THRESHOLD = 4;
    static final int MAX_ANIMATION_DURATION = 10000;
    private static final int MAX_SCREENSHOT_RETRIES = 3;
    static final boolean PROFILE_ORIENTATION = false;
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    static final boolean SCREENSHOT_FORCE_565 = true;
    static final int SEAMLESS_ROTATION_TIMEOUT_DURATION = 2000;
    private static final String SIZE_OVERRIDE = "ro.config.size_override";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    private static final String TAG = "WindowManager";
    private static final int TRANSITION_ANIMATION_SCALE = 1;
    static final int TYPE_LAYER_MULTIPLIER = 10000;
    static final int TYPE_LAYER_OFFSET = 1000;
    static final int UPDATE_FOCUS_NORMAL = 0;
    static final int UPDATE_FOCUS_PLACING_SURFACES = 2;
    static final int UPDATE_FOCUS_WILL_ASSIGN_LAYERS = 1;
    static final int UPDATE_FOCUS_WILL_PLACE_SURFACES = 3;
    static final int WINDOWS_FREEZING_SCREENS_ACTIVE = 1;
    static final int WINDOWS_FREEZING_SCREENS_NONE = 0;
    static final int WINDOWS_FREEZING_SCREENS_TIMEOUT = 2;
    private static final int WINDOW_ANIMATION_SCALE = 0;
    static final int WINDOW_FREEZE_TIMEOUT_DURATION = 2000;
    static final int WINDOW_LAYER_MULTIPLIER = 5;
    static final int WINDOW_REPLACEMENT_TIMEOUT_DURATION = 2000;
    static final boolean localLOGV = false;
    private static WindowManagerService sInstance;
    static WindowManagerThreadPriorityBooster sThreadPriorityBooster = new WindowManagerThreadPriorityBooster();
    AccessibilityController mAccessibilityController;
    final IActivityManager mActivityManager;
    final AppTransitionListener mActivityManagerAppTransitionNotifier = new AppTransitionListener() {
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
                    if (atoken.mEnteringAnimation) {
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
    private HwSysResource mActivityResource;
    final boolean mAllowAnimationsInLowPowerMode;
    final boolean mAllowBootMessages;
    boolean mAllowTheaterModeWakeFromLayout;
    final ActivityManagerInternal mAmInternal;
    boolean mAnimateWallpaperWithTarget;
    final Handler mAnimationHandler = new Handler(AnimationThread.getHandler().getLooper());
    private boolean mAnimationsDisabled = false;
    final WindowAnimator mAnimator;
    private float mAnimatorDurationScaleSetting = 1.0f;
    final ArrayList<AppFreezeListener> mAppFreezeListeners = new ArrayList();
    final AppOpsManager mAppOps;
    final AppTransition mAppTransition;
    int mAppsFreezingScreen = 0;
    boolean mBootAnimationStopped = false;
    final BoundsAnimationController mBoundsAnimationController;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED")) {
                WindowManagerService.this.mKeyguardDisableHandler.sendEmptyMessage(3);
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (userId != -10000) {
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            WindowManagerService.this.mScreenCaptureDisabled.remove(userId);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            }
        }
    };
    CircularDisplayMask mCircularDisplayMask;
    boolean mClientFreezingScreen = false;
    final ArraySet<AppWindowToken> mClosingApps = new ArraySet();
    protected final Context mContext;
    WindowState mCurrentFocus = null;
    int[] mCurrentProfileIds = new int[0];
    int mCurrentUserId;
    private HwCustWindowManagerService mCust;
    int mDeferredRotationPauseCount;
    final ArrayList<WindowState> mDestroyPreservedSurface = new ArrayList();
    final ArrayList<WindowState> mDestroySurface = new ArrayList();
    boolean mDisplayEnabled = false;
    long mDisplayFreezeTime = 0;
    boolean mDisplayFrozen = false;
    final DisplayManager mDisplayManager;
    final DisplayManagerInternal mDisplayManagerInternal;
    boolean mDisplayReady;
    final DisplaySettings mDisplaySettings;
    private final Display[] mDisplays;
    Rect mDockedStackCreateBounds;
    int mDockedStackCreateMode = 0;
    DragState mDragState = null;
    final long mDrawLockTimeoutMillis;
    EmulatorDisplayOverlay mEmulatorDisplayOverlay;
    private int mEnterAnimId;
    private boolean mEventDispatchingEnabled;
    private int mExitAnimId;
    private final PointerEventDispatcher mExternalPointerEventDispatcher;
    final ArrayList<AppWindowToken> mFinishedEarlyAnim = new ArrayList();
    final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList();
    boolean mFocusMayChange;
    AppWindowToken mFocusedApp = null;
    boolean mForceDisplayEnabled = false;
    final ArrayList<WindowState> mForceRemoves = new ArrayList();
    boolean mForceResizableTasks = false;
    private int mFrozenDisplayId;
    final SurfaceSession mFxSession;
    final H mH = new H();
    boolean mHardKeyboardAvailable;
    OnHardKeyboardStatusChangeListener mHardKeyboardStatusChangeListener;
    final boolean mHasPermanentDpad;
    final boolean mHaveInputMethods;
    private ArrayList<WindowState> mHidingNonSystemOverlayWindows = new ArrayList();
    private Session mHoldingScreenOn;
    private WakeLock mHoldingScreenWakeLock;
    HwInnerWindowManagerService mHwInnerService = new HwInnerWindowManagerService(this);
    IHwWindowManagerServiceEx mHwWMSEx = null;
    boolean mInTouchMode;
    final InputManagerService mInputManager;
    IInputMethodManager mInputMethodManager;
    WindowState mInputMethodTarget = null;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow = null;
    final InputMonitor mInputMonitor = new InputMonitor(this);
    public boolean mIsPerfBoost;
    boolean mIsTouchDevice;
    boolean mKeyguardAttachWallpaper;
    private final KeyguardDisableHandler mKeyguardDisableHandler;
    Runnable mKeyguardDismissDoneCallback;
    boolean mKeyguardGoingAway;
    WindowState mKeyguardWin;
    String mLastANRState;
    int mLastDispatchedSystemUiVisibility = 0;
    int mLastDisplayFreezeDuration = 0;
    Object mLastFinishedFreezeSource = null;
    WindowState mLastFocus = null;
    int mLastStatusBarVisibility = 0;
    WindowState mLastWakeLockHoldingWindow = null;
    WindowState mLastWakeLockObscuringWindow = null;
    int mLayoutSeq = 0;
    public int mLazyModeOn = 0;
    final boolean mLimitedAlphaCompositing;
    ArrayList<WindowState> mLosingFocus = new ArrayList();
    final int mMaxUiWidth;
    MousePositionTracker mMousePositionTracker = new MousePositionTracker();
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished = new ArrayList();
    final boolean mOnlyCore;
    final ArraySet<AppWindowToken> mOpeningApps = new ArraySet();
    public IHwPCManager mPCManager = null;
    private final HashMap<String, Boolean> mPackages = new HashMap();
    final ArrayList<WindowState> mPendingRemove = new ArrayList();
    WindowState[] mPendingRemoveTmp = new WindowState[20];
    private final PointerEventDispatcher mPointerEventDispatcher;
    final WindowManagerPolicy mPolicy;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    final ArrayList<WindowState> mResizingWindows = new ArrayList();
    RootWindowContainer mRoot;
    ArrayList<RotationWatcher> mRotationWatchers = new ArrayList();
    boolean mSafeMode;
    private SparseArray<Boolean> mScreenCaptureDisabled = new SparseArray();
    private final WakeLock mScreenFrozenLock;
    final Rect mScreenRect = new Rect();
    int mSeamlessRotationCount = 0;
    final ArraySet<Session> mSessions = new ArraySet();
    SettingsObserver mSettingsObserver;
    boolean mShowAlertWindowNotifications = true;
    boolean mShowingBootMessages = false;
    boolean mSkipAppTransitionAnimation = false;
    StrictModeFlash mStrictModeFlash;
    boolean mSupportsPictureInPicture = false;
    boolean mSwitchingUser = false;
    boolean mSystemBooted = false;
    int mSystemDecorLayer = 0;
    TaskPositioner mTaskPositioner;
    final TaskSnapshotController mTaskSnapshotController;
    final Configuration mTempConfiguration = new Configuration();
    private WindowContentFrameStats mTempWindowRenderStats;
    final float[] mTmpFloats = new float[9];
    final Rect mTmpRect = new Rect();
    final Rect mTmpRect2 = new Rect();
    final Rect mTmpRect3 = new Rect();
    final RectF mTmpRectF = new RectF();
    private final SparseIntArray mTmpTaskIds = new SparseIntArray();
    final Matrix mTmpTransform = new Matrix();
    int mTopWallpaperAnimLayer;
    WindowState mTopWallpaperWin;
    int mTransactionSequence;
    private float mTransitionAnimationScaleSetting = 1.0f;
    boolean mTurnOnScreen;
    final UnknownAppVisibilityController mUnknownAppVisibilityController = new UnknownAppVisibilityController(this);
    private ViewServer mViewServer;
    private long mWaitAllWindowDrawStartTime = 0;
    boolean mWaitingForConfig = false;
    ArrayList<WindowState> mWaitingForDrawn = new ArrayList();
    Runnable mWaitingForDrawnCallback;
    Watermark mWatermark;
    private final ArrayList<WindowState> mWinAddedSinceNullFocus = new ArrayList();
    private final ArrayList<WindowState> mWinRemovedSinceNullFocus = new ArrayList();
    private float mWindowAnimationScaleSetting = 1.0f;
    final ArrayList<WindowChangeListener> mWindowChangeListeners = new ArrayList();
    final WindowHashMap mWindowMap = new WindowHashMap();
    final WindowSurfacePlacer mWindowPlacerLocked;
    final ArrayList<AppWindowToken> mWindowReplacementTimeouts = new ArrayList();
    boolean mWindowsChanged = false;
    int mWindowsFreezingScreen = 0;

    interface AppFreezeListener {
        void onAppFreezeTimeout();
    }

    public interface WindowChangeListener {
        void focusChanged();

        void windowsChanged();
    }

    final class DragInputEventReceiver extends InputEventReceiver {
        private boolean mIsStartEvent = true;
        private boolean mMuteInput = false;
        private boolean mStylusButtonDownAtStart;

        public DragInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0049 A:{Catch:{ all -> 0x00e1, all -> 0x00d5, all -> 0x00b6, all -> 0x0097, Exception -> 0x0089, all -> 0x009d }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (WindowManagerService.this.mDragState == null) {
                    finishInputEvent(event, true);
                    return;
                }
                if (!(!(event instanceof MotionEvent) || (event.getSource() & 2) == 0 || (this.mMuteInput ^ 1) == 0)) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    boolean endDrag = false;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0;
                    if (this.mIsStartEvent) {
                        if (isStylusButtonDown) {
                            this.mStylusButtonDownAtStart = true;
                        }
                        this.mIsStartEvent = false;
                    }
                    switch (motionEvent.getAction()) {
                        case 1:
                            this.mMuteInput = true;
                            synchronized (WindowManagerService.this.mWindowMap) {
                                WindowManagerService.boostPriorityForLockedSection();
                                endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                        case 2:
                            if (!this.mStylusButtonDownAtStart || (isStylusButtonDown ^ 1) == 0) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    WindowManagerService.this.mDragState.notifyMoveLw(newX, newY);
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                            } else {
                                this.mMuteInput = true;
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                            break;
                        case 3:
                            this.mMuteInput = true;
                            endDrag = true;
                            if (endDrag) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    WindowManagerService.this.mDragState.endDragLw();
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                this.mStylusButtonDownAtStart = false;
                                this.mIsStartEvent = true;
                            }
                            handled = true;
                            break;
                    }
                    if (endDrag) {
                    }
                    handled = true;
                }
                finishInputEvent(event, handled);
            } catch (Exception e) {
                Slog.e(WindowManagerService.TAG, "Exception caught by drag handleMotion", e);
                finishInputEvent(event, false);
            } catch (Throwable th) {
                finishInputEvent(event, false);
            }
        }
    }

    public final class H extends Handler {
        public static final int ALL_WINDOWS_DRAWN = 33;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int APP_TRANSITION_GETSPECSFUTURE_TIMEOUT = 102;
        public static final int APP_TRANSITION_TIMEOUT = 13;
        public static final int BOOT_TIMEOUT = 23;
        public static final int CHECK_IF_BOOT_ANIMATION_FINISHED = 37;
        public static final int CLIENT_FREEZE_TIMEOUT = 30;
        public static final int DO_ANIMATION_CALLBACK = 26;
        public static final int DRAG_END_TIMEOUT = 21;
        public static final int DRAG_START_TIMEOUT = 20;
        public static final int ENABLE_SCREEN = 16;
        public static final int FINISH_TASK_POSITIONING = 40;
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
        public static final int SHOW_CIRCULAR_DISPLAY_MASK = 35;
        public static final int SHOW_EMULATOR_DISPLAY_OVERLAY = 36;
        public static final int SHOW_STRICT_MODE_VIOLATION = 25;
        public static final int TAP_OUTSIDE_TASK = 31;
        public static final int TEAR_DOWN_DRAG_AND_DROP_INPUT = 44;
        public static final int UNUSED = 0;
        public static final int UPDATE_ANIMATION_SCALE = 51;
        public static final int UPDATE_DOCKED_STACK_DIVIDER = 41;
        public static final int WAITING_FOR_DRAWN_TIMEOUT = 24;
        public static final int WAIT_KEYGUARD_DISMISS_DONE_TIMEOUT = 100;
        public static final int WALLPAPER_DRAW_PENDING_TIMEOUT = 39;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int WINDOW_HIDE_TIMEOUT = 52;
        public static final int WINDOW_REPLACEMENT_TIMEOUT = 46;

        /* JADX WARNING: Missing block: B:29:0x00a0, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
        /* JADX WARNING: Missing block: B:30:0x00a3, code:
            if (r5 == null) goto L_0x00a8;
     */
        /* JADX WARNING: Missing block: B:31:0x00a5, code:
            r5.onWindowFocusChangedNotLocked();
     */
        /* JADX WARNING: Missing block: B:32:0x00a8, code:
            if (r20 == null) goto L_0x00ca;
     */
        /* JADX WARNING: Missing block: B:33:0x00aa, code:
            r20.reportFocusChangedSerialized(true, r32.this$0.mInTouchMode);
            com.android.server.wm.WindowManagerService.-wrap4(r32.this$0);
     */
        /* JADX WARNING: Missing block: B:34:0x00ca, code:
            if (r16 == null) goto L_0x0009;
     */
        /* JADX WARNING: Missing block: B:35:0x00cc, code:
            r16.reportFocusChangedSerialized(false, r32.this$0.mInTouchMode);
     */
        /* JADX WARNING: Missing block: B:102:0x0435, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
            java.lang.Runtime.getRuntime().gc();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            int N;
            int i;
            IBinder win;
            Runnable callback;
            switch (msg.what) {
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
                                if (!(newFocus == null || lastFocus == null || (newFocus.isDisplayedLw() ^ 1) == 0)) {
                                    WindowManagerService.this.mLosingFocus.add(lastFocus);
                                    lastFocus = null;
                                    break;
                                }
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case 3:
                    ArrayList<WindowState> losers;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            losers = WindowManagerService.this.mLosingFocus;
                            WindowManagerService.this.mLosingFocus = new ArrayList();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    N = losers.size();
                    for (i = 0; i < N; i++) {
                        ((WindowState) losers.get(i)).reportFocusChangedSerialized(false, WindowManagerService.this.mInTouchMode);
                    }
                    break;
                case 11:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            WindowManagerService.this.getDefaultDisplayContentLocked().onWindowFreezeTimeout();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 13:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (!(!WindowManagerService.this.mAppTransition.isTransitionSet() && (WindowManagerService.this.mOpeningApps.isEmpty() ^ 1) == 0 && (WindowManagerService.this.mClosingApps.isEmpty() ^ 1) == 0)) {
                                Slog.w(WindowManagerService.TAG, "*** APP TRANSITION TIMEOUT. isTransitionSet()=" + WindowManagerService.this.mAppTransition.isTransitionSet() + " mOpeningApps.size()=" + WindowManagerService.this.mOpeningApps.size() + " mClosingApps.size()=" + WindowManagerService.this.mClosingApps.size());
                                WindowManagerService.this.mAppTransition.setTimeout();
                                N = WindowManagerService.this.mOpeningApps.size();
                                for (i = 0; i < N; i++) {
                                    AppWindowToken appToken = (AppWindowToken) WindowManagerService.this.mOpeningApps.valueAt(i);
                                    appToken.mPendingRelaunchCount = 0;
                                    appToken.mFrozenBounds.clear();
                                    appToken.mFrozenMergedConfig.clear();
                                }
                                WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 14:
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                    break;
                case 15:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerService.this.mAnimator.isAnimating() || WindowManagerService.this.mAnimator.isAnimationScheduled()) {
                                sendEmptyMessageDelayed(15, 2000);
                                break;
                            } else if (WindowManagerService.this.mDisplayFrozen) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
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
                            for (i = WindowManagerService.this.mAppFreezeListeners.size() - 1; i >= 0; i--) {
                                mListenersSize++;
                                ((AppFreezeListener) WindowManagerService.this.mAppFreezeListeners.get(i)).onAppFreezeTimeout();
                            }
                            if (mListenersSize == 0) {
                                Slog.e(WindowManagerService.TAG, "mAppFreezeListeners is empty ! so stopFreezingDisplayLocked");
                                WindowManagerService.this.stopFreezingDisplayLocked();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 18:
                    removeMessages(18, msg.obj);
                    int displayId = ((Integer) msg.obj).intValue();
                    if (WindowManagerService.this.mRoot.getDisplayContent(displayId) == null) {
                        Slog.w(WindowManagerService.TAG, "non-existing display ,so reset mWaitingForConfig");
                        if (WindowManagerService.this.mWaitingForConfig) {
                            WindowManagerService.this.mWaitingForConfig = false;
                            break;
                        }
                    }
                    WindowManagerService.this.sendNewConfiguration(displayId);
                    break;
                    break;
                case 19:
                    if (WindowManagerService.this.mWindowsChanged) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                WindowManagerService.this.mWindowsChanged = false;
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        WindowManagerService.this.notifyWindowsChanged();
                        break;
                    }
                    break;
                case 20:
                    win = msg.obj;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerService.this.mDragState != null) {
                                WindowManagerService.this.mDragState.unregister();
                                WindowManagerService.this.mDragState.reset();
                                WindowManagerService.this.mDragState = null;
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 21:
                    win = (IBinder) msg.obj;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerService.this.mDragState != null) {
                                WindowManagerService.this.mDragState.mDragResult = false;
                                WindowManagerService.this.mDragState.endDragLw();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 22:
                    WindowManagerService.this.notifyHardKeyboardStatusChange();
                    break;
                case 23:
                    WindowManagerService.this.performBootTimeout();
                    break;
                case 24:
                    callback = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            Flog.i(NativeResponseCode.SERVICE_FOUND, "Timeout waiting for drawn: undrawn=" + WindowManagerService.this.mWaitingForDrawn);
                            WindowManagerService.this.mWaitingForDrawn.clear();
                            callback = WindowManagerService.this.mWaitingForDrawnCallback;
                            WindowManagerService.this.mWaitingForDrawnCallback = null;
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case 25:
                    WindowManagerService.this.showStrictModeViolation(msg.arg1, msg.arg2);
                    break;
                case DO_ANIMATION_CALLBACK /*26*/:
                    try {
                        ((IRemoteCallback) msg.obj).sendResult(null);
                        break;
                    } catch (RemoteException e) {
                        break;
                    }
                case 30:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerService.this.mClientFreezingScreen) {
                                WindowManagerService.this.mClientFreezingScreen = false;
                                WindowManagerService.this.mLastFinishedFreezeSource = "client-timeout";
                                WindowManagerService.this.stopFreezingDisplayLocked();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 31:
                    WindowManagerService.this.handleTapOutsideTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 32:
                    try {
                        WindowManagerService.this.mActivityManager.notifyActivityDrawn((IBinder) msg.obj);
                        break;
                    } catch (RemoteException e2) {
                        break;
                    }
                case 33:
                    if (WindowManagerService.this.isPrintAllWindowsDrawnLogs()) {
                        Flog.i(NativeResponseCode.SERVICE_FOUND, "ALL_WINDOWS_DRAWN timeout");
                    }
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            callback = WindowManagerService.this.mWaitingForDrawnCallback;
                            WindowManagerService.this.mWaitingForDrawnCallback = null;
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case 34:
                    float scale = WindowManagerService.this.getCurrentAnimatorScale();
                    ValueAnimator.setDurationScale(scale);
                    Session session = msg.obj;
                    if (session == null) {
                        ArrayList<IWindowSessionCallback> callbacks = new ArrayList();
                        synchronized (WindowManagerService.this.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                for (i = 0; i < WindowManagerService.this.mSessions.size(); i++) {
                                    callbacks.add(((Session) WindowManagerService.this.mSessions.valueAt(i)).mCallback);
                                }
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        for (i = 0; i < callbacks.size(); i++) {
                            try {
                                ((IWindowSessionCallback) callbacks.get(i)).onAnimatorScaleChanged(scale);
                            } catch (RemoteException e3) {
                            }
                        }
                        break;
                    }
                    try {
                        session.mCallback.onAnimatorScaleChanged(scale);
                        break;
                    } catch (RemoteException e4) {
                        break;
                    }
                case 35:
                    WindowManagerService.this.showCircularMask(msg.arg1 == 1);
                    break;
                case 36:
                    WindowManagerService.this.showEmulatorDisplayOverlay();
                    break;
                case 37:
                    boolean bootAnimationComplete;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                                Slog.i(WindowManagerService.TAG, "CHECK_IF_BOOT_ANIMATION_FINISHED:");
                            }
                            bootAnimationComplete = WindowManagerService.this.checkBootAnimationCompleteLocked();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
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
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    WindowManagerService.this.mAmInternal.clearSavedANRState();
                    break;
                case 39:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (WindowManagerService.this.mRoot.mWallpaperController.processWallpaperDrawPendingTimeout()) {
                                WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 40:
                    WindowManagerService.this.finishPositioning();
                    break;
                case 41:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            DisplayContent displayContent = WindowManagerService.this.getDefaultDisplayContentLocked();
                            displayContent.getDockedDividerController().reevaluateVisibility(false);
                            displayContent.adjustForImeIfNeeded();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 44:
                    InputInterceptor interceptor = msg.obj;
                    if (interceptor != null) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                interceptor.tearDown();
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        break;
                    }
                    break;
                case WINDOW_REPLACEMENT_TIMEOUT /*46*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            for (i = WindowManagerService.this.mWindowReplacementTimeouts.size() - 1; i >= 0; i--) {
                                ((AppWindowToken) WindowManagerService.this.mWindowReplacementTimeouts.get(i)).onWindowReplacementTimeout();
                            }
                            WindowManagerService.this.mWindowReplacementTimeouts.clear();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
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
                case 51:
                    switch (msg.arg1) {
                        case 0:
                            WindowManagerService.this.mWindowAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                            break;
                        case 1:
                            WindowManagerService.this.mTransitionAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                            break;
                        case 2:
                            WindowManagerService.this.mAnimatorDurationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                            WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            break;
                    }
                    break;
                case 52:
                    WindowState window = msg.obj;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            LayoutParams layoutParams = window.mAttrs;
                            layoutParams.flags &= -129;
                            window.hidePermanentlyLw();
                            window.setDisplayLayoutNeeded();
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 53:
                    WindowManagerService.this.mAmInternal.notifyDockedStackMinimizedChanged(msg.arg1 == 1);
                    break;
                case 54:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            WindowManagerService.this.getDefaultDisplayContentLocked().onSeamlessRotationTimeout();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 55:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            WindowManagerService.this.restorePointerIconLocked((DisplayContent) msg.obj, (float) msg.arg1, (float) msg.arg2);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 56:
                    WindowManagerService.this.mAmInternal.notifyKeyguardFlagsChanged((Runnable) msg.obj);
                    break;
                case NOTIFY_KEYGUARD_TRUSTED_CHANGED /*57*/:
                    WindowManagerService.this.mAmInternal.notifyKeyguardTrustedChanged();
                    break;
                case SET_HAS_OVERLAY_UI /*58*/:
                    WindowManagerService.this.mAmInternal.setHasOverlayUi(msg.arg1, msg.arg2 == 1);
                    break;
                case 100:
                case 101:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            callback = WindowManagerService.this.mKeyguardDismissDoneCallback;
                            WindowManagerService.this.mKeyguardDismissDoneCallback = null;
                            WindowManagerService.this.mKeyguardWin = null;
                            WindowManagerService.this.mTopWallpaperWin = null;
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case 102:
                    Flog.w(307, "APP_TRANSITION_ANIMATIONS_SPECSFUTURE timeout");
                    break;
                case 103:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            Slog.w(WindowManagerService.TAG, "stopFreezingDisplayLocked  by PC_FREEZE_TIMEOUT");
                            WindowManagerService.this.stopFreezingDisplayLocked();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 104:
                    try {
                        WindowManagerService.this.mActivityManager.setFocusedTask(msg.arg1);
                        break;
                    } catch (RemoteException e5) {
                        Log.e(WindowManagerService.TAG, "setFocusedDisplay()");
                        break;
                    }
            }
        }
    }

    public class HwInnerWindowManagerService extends Stub {
        WindowManagerService mWMS;

        HwInnerWindowManagerService(WindowManagerService wms) {
            this.mWMS = wms;
        }

        public int releaseSnapshots(int memLevel) {
            if (WindowManagerService.this.mHwWMSEx != null) {
                return WindowManagerService.this.mHwWMSEx.releaseSnapshots(memLevel);
            }
            return 0;
        }
    }

    private final class LocalService extends WindowManagerInternal {
        /* synthetic */ LocalService(WindowManagerService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void requestTraversalFromDisplayManager() {
            WindowManagerService.this.requestTraversal();
        }

        public void setMagnificationSpec(MagnificationSpec spec) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mAccessibilityController != null) {
                        WindowManagerService.this.mAccessibilityController.setMagnificationSpecLocked(spec);
                    } else {
                        throw new IllegalStateException("Magnification callbacks not set!");
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
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
                    } else {
                        MagnificationSpec spec = null;
                        if (WindowManagerService.this.mAccessibilityController != null) {
                            spec = WindowManagerService.this.mAccessibilityController.getMagnificationSpecForWindowLocked(windowState);
                        }
                        if ((spec == null || spec.isNop()) && windowState.mGlobalScale == 1.0f) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        }
                        spec = spec == null ? MagnificationSpec.obtain() : MagnificationSpec.obtain(spec);
                        spec.scale *= windowState.mGlobalScale;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return spec;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return null;
        }

        public void setMagnificationCallbacks(MagnificationCallbacks callbacks) {
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
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback callback) {
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
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void setInputFilter(IInputFilter filter) {
            WindowManagerService.this.mInputManager.setInputFilter(filter);
        }

        public IBinder getFocusedWindowToken() {
            IBinder asBinder;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = WindowManagerService.this.getFocusedWindowLocked();
                    if (windowState != null) {
                        asBinder = windowState.mClient.asBinder();
                    } else {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return asBinder;
        }

        public boolean isCoverOpen() {
            return WindowManagerService.this.isCoverOpen();
        }

        public boolean isKeyguardLocked() {
            return WindowManagerService.this.isKeyguardLocked();
        }

        public boolean isKeyguardGoingAway() {
            return WindowManagerService.this.mKeyguardGoingAway;
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
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void waitForAllWindowsDrawn(Runnable callback, long timeout) {
            boolean allWindowsDrawn = false;
            WindowManagerService.this.mWaitAllWindowDrawStartTime = SystemClock.elapsedRealtime();
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
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
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
                        } else {
                            WindowToken token = dc.removeWindowToken(binder);
                            if (token == null) {
                                Slog.w(WindowManagerService.TAG, "removeWindowToken: Attempted to remove non-existing token: " + binder);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            token.removeAllWindowsIfPossible();
                        }
                    }
                    WindowManagerService.this.removeWindowToken(binder, displayId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void registerAppTransitionListener(AppTransitionListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mAppTransition.registerListenerLocked(listener);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public int getInputMethodWindowVisibleHeight() {
            int inputMethodWindowVisibleHeightLw;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    inputMethodWindowVisibleHeightLw = WindowManagerService.this.mPolicy.getInputMethodWindowVisibleHeightLw();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return inputMethodWindowVisibleHeightLw;
        }

        public void saveLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerService.this.mInputMethodWindow != null) {
                        WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(WindowManagerService.this.mInputMethodWindow, WindowManagerService.this.mInputMethodTarget);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void clearLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(null, null);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void updateInputMethodWindowStatus(IBinder imeToken, boolean imeWindowVisible, boolean dismissImeOnBackKeyPressed, IBinder targetWindowToken) {
            WindowManagerService.this.mPolicy.setDismissImeOnBackKeyPressed(dismissImeOnBackKeyPressed);
        }

        public boolean isHardKeyboardAvailable() {
            boolean z;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = WindowManagerService.this.mHardKeyboardAvailable;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        public void setOnHardKeyboardStatusChangeListener(OnHardKeyboardStatusChangeListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mHardKeyboardStatusChangeListener = listener;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public boolean isStackVisible(int stackId) {
            boolean isStackVisible;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isStackVisible = WindowManagerService.this.getDefaultDisplayContentLocked().isStackVisible(stackId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isStackVisible;
        }

        public boolean isDockedDividerResizing() {
            boolean isResizing;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isResizing = WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().isResizing();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isResizing;
        }

        public void computeWindowsForAccessibility() {
            AccessibilityController accessibilityController;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    accessibilityController = WindowManagerService.this.mAccessibilityController;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (accessibilityController != null) {
                accessibilityController.performComputeChangedWindowsNotLocked();
            }
        }

        public void waitForKeyguardDismissDone(Runnable callback, long timeout) {
            boolean iskeyguardWindNull = false;
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.mKeyguardDismissDoneCallback = callback;
                    WindowManagerService.this.getDefaultDisplayContentLocked().forAllWindows((Consumer) new -$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w(this), true);
                    WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                    if (WindowManagerService.this.mKeyguardWin == null) {
                        iskeyguardWindNull = true;
                    } else {
                        iskeyguardWindNull = false;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            WindowManagerService.this.mH.removeMessages(100);
            if (iskeyguardWindNull) {
                Slog.i(WindowManagerService.TAG, "fingerunlock--waitForKeyguardDismissDone there is no keyguard.");
                callback.run();
                return;
            }
            WindowManagerService.this.mH.sendEmptyMessageDelayed(100, timeout);
        }

        /* synthetic */ void lambda$-com_android_server_wm_WindowManagerService$LocalService_365777(WindowState win) {
            boolean z = false;
            if (win.mAttrs.type == 2004) {
                WindowManagerService.this.mKeyguardWin = win;
                WindowManagerService windowManagerService = WindowManagerService.this;
                if ((WindowManagerService.this.mKeyguardWin.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                    z = true;
                }
                windowManagerService.mKeyguardAttachWallpaper = z;
            }
            if (win.mAttrs.type == 2013 && WindowManagerService.this.mTopWallpaperWin == null) {
                WindowManagerService.this.mTopWallpaperWin = win;
                WindowManagerService.this.mTopWallpaperAnimLayer = WindowManagerService.this.mTopWallpaperWin.mWinAnimator.mAnimLayer;
            }
        }

        public void setDockedStackDividerRotation(int rotation) {
            synchronized (WindowManagerService.this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().setDockedStackDividerRotation(rotation);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
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
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isMinimizedDock;
        }

        public String getFullStackTopWindow() {
            TaskStack stack = WindowManagerService.this.mRoot.getStackById(1);
            if (stack == null || stack.getTopChild() == null || ((Task) stack.getTopChild()).getTopFullscreenAppToken() == null) {
                return null;
            }
            return ((Task) stack.getTopChild()).getTopFullscreenAppToken().appComponentName;
        }
    }

    private static class MousePositionTracker implements PointerEventListener {
        private boolean mLatestEventWasMouse;
        private float mLatestMouseX;
        private float mLatestMouseY;

        /* synthetic */ MousePositionTracker(MousePositionTracker -this0) {
            this();
        }

        private MousePositionTracker() {
        }

        void updatePosition(float x, float y) {
            synchronized (this) {
                this.mLatestEventWasMouse = true;
                this.mLatestMouseX = x;
                this.mLatestMouseY = y;
            }
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent.isFromSource(8194)) {
                updatePosition(motionEvent.getRawX(), motionEvent.getRawY());
                return;
            }
            synchronized (this) {
                this.mLatestEventWasMouse = false;
            }
        }
    }

    class RotationWatcher {
        DeathRecipient mDeathRecipient;
        int mDisplayId;
        IRotationWatcher mWatcher;

        RotationWatcher(IRotationWatcher watcher, DeathRecipient deathRecipient, int displayId) {
            this.mWatcher = watcher;
            this.mDeathRecipient = deathRecipient;
            this.mDisplayId = displayId;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mAnimationDurationScaleUri = Global.getUriFor("animator_duration_scale");
        private final Uri mDisplayInversionEnabledUri = Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mTransitionAnimationScaleUri = Global.getUriFor("transition_animation_scale");
        private final Uri mWindowAnimationScaleUri = Global.getUriFor("window_animation_scale");

        public SettingsObserver() {
            super(new Handler());
            ContentResolver resolver = WindowManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            resolver.registerContentObserver(this.mWindowAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mTransitionAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mAnimationDurationScaleUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    WindowManagerService.this.updateCircularDisplayMaskIfNeeded();
                } else {
                    int mode;
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

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateAnimationScaleMode {
    }

    static {
        boolean z;
        boolean z2 = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        } else {
            z = false;
        }
        IS_DEBUG_VERSION = z;
        if (!SystemProperties.get("ro.config.hw_notch_size", "").equals("")) {
            z2 = true;
        }
        IS_NOTCH_PROP = z2;
    }

    int getDragLayerLocked() {
        return (this.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    static void boostPriorityForLockedSection() {
        sThreadPriorityBooster.boost();
    }

    static void resetPriorityAfterLockedSection() {
        sThreadPriorityBooster.reset();
    }

    void openSurfaceTransaction() {
        try {
            Trace.traceBegin(32, "openSurfaceTransaction");
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                if (this.mRoot.mSurfaceTraceEnabled) {
                    this.mRoot.mRemoteEventTrace.openSurfaceTransaction();
                }
                SurfaceControl.openTransaction();
            }
            resetPriorityAfterLockedSection();
            Trace.traceEnd(32);
        } catch (Throwable th) {
            Trace.traceEnd(32);
        }
    }

    void closeSurfaceTransaction() {
        try {
            Trace.traceBegin(32, "closeSurfaceTransaction");
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                if (this.mRoot.mSurfaceTraceEnabled) {
                    this.mRoot.mRemoteEventTrace.closeSurfaceTransaction();
                }
                SurfaceControl.closeTransaction();
            }
            resetPriorityAfterLockedSection();
            Trace.traceEnd(32);
        } catch (Throwable th) {
            Trace.traceEnd(32);
        }
    }

    void executeEmptyAnimationTransaction() {
        try {
            Trace.traceBegin(32, "openSurfaceTransaction");
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                if (this.mRoot.mSurfaceTraceEnabled) {
                    this.mRoot.mRemoteEventTrace.openSurfaceTransaction();
                }
                SurfaceControl.openTransaction();
                SurfaceControl.setAnimationTransaction();
                if (this.mRoot.mSurfaceTraceEnabled) {
                    this.mRoot.mRemoteEventTrace.closeSurfaceTransaction();
                }
            }
            resetPriorityAfterLockedSection();
            Trace.traceEnd(32);
            try {
                Trace.traceBegin(32, "closeSurfaceTransaction");
                SurfaceControl.closeTransaction();
            } finally {
                Trace.traceEnd(32);
            }
        } catch (Throwable th) {
            Trace.traceEnd(32);
        }
    }

    static WindowManagerService getInstance() {
        return sInstance;
    }

    public static WindowManagerService main(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        WindowManagerService[] holder = new WindowManagerService[1];
        DisplayThread.getHandler().runWithScissors(new AnonymousClass8(haveInputMethods, showBootMsgs, onlyCore, context, im, policy), 0);
        return sInstance;
    }

    static /* synthetic */ void lambda$-com_android_server_wm_WindowManagerService_47633(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        IHwWindowManagerService iwms = HwServiceFactory.getHuaweiWindowManagerService();
        if (iwms != null) {
            sInstance = iwms.getInstance(context, im, haveInputMethods, showBootMsgs, onlyCore, policy);
        } else {
            sInstance = new WindowManagerService(context, im, haveInputMethods, showBootMsgs, onlyCore, policy);
        }
    }

    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());
                WindowManagerService.this.mPolicy.init(WindowManagerService.this.mContext, WindowManagerService.this, WindowManagerService.this);
            }
        }, 0);
    }

    public boolean getAccelPackages(String pkg) {
        Boolean pkgIn = (Boolean) this.mPackages.get(pkg);
        return Boolean.valueOf(pkgIn != null ? pkgIn.booleanValue() : false).booleanValue();
    }

    private void initAccelPackages() {
        File systemDir = new File(Environment.getRootDirectory(), "etc");
        if (systemDir.mkdirs()) {
            Slog.w(TAG, "system/etc dir is not exist! Creat");
        }
        FileInputStream fileInputStream = null;
        XmlPullParser xmlPullParser = null;
        try {
            fileInputStream = new AtomicFile(new File(systemDir, "accelpackages.xml")).openRead();
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, null);
            while (true) {
                XmlUtils.nextElement(xmlPullParser);
                String element = xmlPullParser.getName();
                if (element == null) {
                    break;
                } else if (element.equals(AbsLocationManagerService.DEL_PKG)) {
                    String pkg = xmlPullParser.getAttributeValue(null, "name");
                    if (pkg != null) {
                        this.mPackages.put(pkg, Boolean.valueOf(true));
                    }
                }
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (XmlPullParserException e2) {
            Slog.w(TAG, "Error parse accelpackages.xml", e2);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3) {
                }
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e4) {
                }
            }
        } catch (IOException e5) {
            if (fileInputStream == null) {
                Slog.w(TAG, "Error reading accelpackages.xml", e5);
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e6) {
                }
            }
        } catch (Throwable th) {
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                }
            }
        }
    }

    protected WindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
        InputChannel inputChannel;
        this.mHwWMSEx = HwServiceExFactory.getHwWindowManagerServiceEx(this, context);
        LockGuard.installLock((Object) this, 5);
        this.mRoot = new RootWindowContainer(this);
        this.mContext = context;
        this.mHaveInputMethods = haveInputMethods;
        this.mAllowBootMessages = showBootMsgs;
        this.mOnlyCore = onlyCore;
        this.mLimitedAlphaCompositing = context.getResources().getBoolean(17957002);
        this.mHasPermanentDpad = context.getResources().getBoolean(17956971);
        this.mInTouchMode = context.getResources().getBoolean(17956916);
        this.mDrawLockTimeoutMillis = (long) context.getResources().getInteger(17694781);
        this.mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(17956871);
        this.mMaxUiWidth = context.getResources().getInteger(17694808);
        this.mInputManager = inputManager;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mDisplaySettings = new DisplaySettings();
        this.mDisplaySettings.readSettingsLocked();
        this.mWindowPlacerLocked = new WindowSurfacePlacer(this);
        this.mPolicy = policy;
        this.mTaskSnapshotController = new TaskSnapshotController(this);
        LocalServices.addService(WindowManagerPolicy.class, this.mPolicy);
        if (this.mInputManager != null) {
            inputChannel = this.mInputManager.monitorInput(TAG);
            this.mPointerEventDispatcher = inputChannel != null ? new PointerEventDispatcher(inputChannel) : null;
        } else {
            this.mPointerEventDispatcher = null;
        }
        if (this.mInputManager != null) {
            inputChannel = this.mInputManager.monitorInput("ExternalPCChannel");
            this.mExternalPointerEventDispatcher = inputChannel != null ? new PointerEventDispatcher(inputChannel) : null;
        } else {
            this.mExternalPointerEventDispatcher = null;
        }
        this.mFxSession = new SurfaceSession();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mCust = (HwCustWindowManagerService) HwCustUtils.createObj(HwCustWindowManagerService.class, new Object[0]);
        this.mDisplays = this.mDisplayManager.getDisplays();
        for (Display display : this.mDisplays) {
            createDisplayContentLocked(display);
        }
        this.mKeyguardDisableHandler = new KeyguardDisableHandler(this.mContext, this.mPolicy);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        if (this.mPowerManagerInternal != null) {
            this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
                public int getServiceType() {
                    return 3;
                }

                public void onLowPowerModeChanged(PowerSaveState result) {
                    synchronized (WindowManagerService.this.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            boolean enabled = result.batterySaverEnabled;
                            if (!(WindowManagerService.this.mAnimationsDisabled == enabled || (WindowManagerService.this.mAllowAnimationsInLowPowerMode ^ 1) == 0)) {
                                WindowManagerService.this.mAnimationsDisabled = enabled;
                                WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            });
            this.mAnimationsDisabled = this.mPowerManagerInternal.getLowPowerState(3).batterySaverEnabled;
        }
        this.mScreenFrozenLock = this.mPowerManager.newWakeLock(1, "SCREEN_FROZEN");
        this.mScreenFrozenLock.setReferenceCounted(false);
        this.mAppTransition = HwServiceFactory.createHwAppTransition(context, this);
        this.mAppTransition.registerListenerLocked(this.mActivityManagerAppTransitionNotifier);
        AnimationHandler animationHandler = new AnimationHandler();
        animationHandler.setProvider(new SfVsyncFrameCallbackProvider());
        this.mBoundsAnimationController = new BoundsAnimationController(context, this.mAppTransition, AnimationThread.getHandler(), animationHandler);
        this.mActivityManager = ActivityManager.getService();
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        OnOpChangedInternalListener opListener = new OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                WindowManagerService.this.sendUpdateAppOpsState();
                WindowManagerService.this.updateAppOpsStateReport(op, packageName);
            }
        };
        this.mAppOps.startWatchingMode(24, null, opListener);
        this.mAppOps.startWatchingMode(45, null, opListener);
        this.mWindowAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "window_animation_scale", this.mWindowAnimationScaleSetting);
        this.mTransitionAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "transition_animation_scale", context.getResources().getFloat(17104946));
        setAnimatorDurationScale(Global.getFloat(context.getContentResolver(), "animator_duration_scale", this.mAnimatorDurationScaleSetting));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        filter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mSettingsObserver = new SettingsObserver();
        this.mHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870922, TAG);
        this.mHoldingScreenWakeLock.setReferenceCounted(false);
        this.mAnimator = new WindowAnimator(this);
        this.mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(17956885);
        LocalServices.addService(WindowManagerInternal.class, new LocalService(this, null));
        initPolicy();
        Watchdog.getInstance().addMonitor(this);
        openSurfaceTransaction();
        try {
            createWatermarkInTransaction();
            showEmulatorDisplayOverlayIfNeeded();
            if (HISI_PERF_OPT) {
                initAccelPackages();
            }
        } finally {
            closeSurfaceTransaction();
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
        switch (windowType) {
            case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
            case 2012:
            case 2019:
                return true;
            default:
                return false;
        }
    }

    static boolean excludeWindowsFromTapOutTask(WindowState win) {
        LayoutParams attrs = win == null ? null : win.getAttrs();
        if (attrs == null) {
            return false;
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && "com.huawei.desktop.systemui".equals(attrs.packageName)) {
            return true;
        }
        return attrs.type == 1000 && "com.baidu.input_huawei".equals(attrs.packageName) && (!HwPCUtils.isPcCastModeInServer() || HwPCUtils.enabledInPad());
    }

    /* JADX WARNING: Missing block: B:404:0x08fd, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:405:0x0900, code:
            if (r39 == false) goto L_0x0909;
     */
    /* JADX WARNING: Missing block: B:406:0x0902, code:
            sendNewConfiguration(r51);
     */
    /* JADX WARNING: Missing block: B:407:0x0909, code:
            android.os.Binder.restoreCallingIdentity(r36);
     */
    /* JADX WARNING: Missing block: B:408:0x090c, code:
            return r40;
     */
    /* JADX WARNING: Missing block: B:413:0x091b, code:
            if (r45.mCurrentFocus.mOwnerUid == r27) goto L_0x072c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int addWindow(Session session, IWindow client, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
        int[] appOp = new int[1];
        int res = this.mPolicy.checkAddPermission(attrs, appOp);
        if (res != 0) {
            return res;
        }
        int forceCompatMode;
        boolean reportNewConfig = false;
        WindowState parentWindow = null;
        int callingUid = Binder.getCallingUid();
        int type = attrs.type;
        if (attrs.type >= 1000 && attrs.type <= 1999) {
            forceCompatMode = -3;
        } else if (attrs.packageName == null) {
            forceCompatMode = -3;
        } else if ((attrs.privateFlags & 128) != 0) {
            forceCompatMode = 1;
        } else {
            forceCompatMode = 0;
        }
        synchronized (this.mWindowMap) {
            boostPriorityForLockedSection();
            if (this.mDisplayReady) {
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent == null) {
                    Slog.w(TAG, "Attempted to add window to a display that does not exist: " + displayId + ".  Aborting.");
                } else if (displayContent.hasAccess(session.mUid) || (this.mDisplayManagerInternal.isUidPresentOnDisplay(session.mUid, displayId) ^ 1) == 0) {
                    try {
                        if (this.mWindowMap.containsKey(client.asBinder())) {
                            Slog.w(TAG, "Window " + client + " is already added");
                            resetPriorityAfterLockedSection();
                            return -5;
                        }
                        if (type >= 1000 && type <= 1999) {
                            parentWindow = windowForClientLocked(null, attrs.token, false);
                            if (parentWindow == null) {
                                Slog.w(TAG, "Attempted to add window with token that is not a window: " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -2;
                            } else if (parentWindow.mAttrs.type >= 1000 && parentWindow.mAttrs.type <= 1999) {
                                Slog.w(TAG, "Attempted to add window with token that is a sub-window: " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -2;
                            }
                        }
                        if (type == 2030) {
                            if ((displayContent.isPrivate() ^ 1) != 0) {
                                Slog.w(TAG, "Attempted to add private presentation window to a non-private display.  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -8;
                            }
                        }
                        AppWindowToken atoken = null;
                        boolean hasParent = parentWindow != null;
                        WindowToken token = displayContent.getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);
                        if (token == null && "com.google.android.marvin.talkback".equals(attrs.packageName) && HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                            token = this.mRoot.getDisplayContentOrCreate(0).getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);
                        }
                        int rootType = hasParent ? parentWindow.mAttrs.type : type;
                        boolean addToastWindowRequiresToken = false;
                        if (token == null) {
                            if (rootType >= 1 && rootType <= 99) {
                                Slog.w(TAG, "Attempted to add application window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType == 2011) {
                                Slog.w(TAG, "Attempted to add input method window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType == 2031) {
                                Slog.w(TAG, "Attempted to add voice interaction window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType == 2013) {
                                Slog.w(TAG, "Attempted to add wallpaper window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType == 2023) {
                                Slog.w(TAG, "Attempted to add Dream window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType == 2035) {
                                Slog.w(TAG, "Attempted to add QS dialog window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else if (rootType == 2032) {
                                Slog.w(TAG, "Attempted to add Accessibility overlay window with unknown token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            } else {
                                if (type == 2005) {
                                    if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow)) {
                                        Slog.w(TAG, "Attempted to add a toast window with unknown token " + attrs.token + ".  Aborting.");
                                        resetPriorityAfterLockedSection();
                                        return -1;
                                    }
                                }
                                token = new WindowToken(this, attrs.token != null ? attrs.token : client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);
                            }
                        } else if (rootType >= 1 && rootType <= 99) {
                            atoken = token.asAppWindowToken();
                            if (atoken == null) {
                                Slog.w(TAG, "Attempted to add window with non-application token " + token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -3;
                            } else if (atoken.removed) {
                                Slog.w(TAG, "Attempted to add window with exiting application token " + token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -4;
                            }
                        } else if (rootType == 2011) {
                            if (token.windowType != 2011) {
                                Slog.w(TAG, "Attempted to add input method window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (rootType == 2031) {
                            if (token.windowType != 2031) {
                                Slog.w(TAG, "Attempted to add voice interaction window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (rootType == 2013) {
                            if (token.windowType != 2013) {
                                Slog.w(TAG, "Attempted to add wallpaper window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (rootType == 2023) {
                            if (token.windowType != 2023) {
                                Slog.w(TAG, "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (rootType == 2032) {
                            if (token.windowType != 2032) {
                                Slog.w(TAG, "Attempted to add Accessibility overlay window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (type == 2005) {
                            addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow);
                            if (addToastWindowRequiresToken && token.windowType != 2005) {
                                Slog.w(TAG, "Attempted to add a toast window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (type == 2035) {
                            if (token.windowType != 2035) {
                                Slog.w(TAG, "Attempted to add QS dialog window with bad token " + attrs.token + ".  Aborting.");
                                resetPriorityAfterLockedSection();
                                return -1;
                            }
                        } else if (token.asAppWindowToken() != null) {
                            Slog.w(TAG, "Non-null appWindowToken for system window of rootType=" + rootType);
                            attrs.token = null;
                            WindowToken windowToken = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);
                        } else if (!(this.mCust == null || !this.mCust.isChargingAlbumType(type) || this.mCust.isChargingAlbumType(token.windowType))) {
                            Slog.w(TAG, "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                            resetPriorityAfterLockedSection();
                            return -1;
                        }
                        WindowState win = new WindowState(this, session, client, token, parentWindow, appOp[0], seq, attrs, viewVisibility, session.mUid, session.mCanAddInternalSystemWindow, forceCompatMode);
                        if (win.mDeathRecipient == null) {
                            Slog.w(TAG, "Adding window client " + client.asBinder() + " that is dead, aborting.");
                            resetPriorityAfterLockedSection();
                            return -4;
                        } else if (win.getDisplayContent() == null) {
                            Slog.w(TAG, "Adding window to Display that has been removed.");
                            resetPriorityAfterLockedSection();
                            return -9;
                        } else {
                            this.mPolicy.adjustWindowParamsLw(win.mAttrs);
                            win.setShowToOwnerOnlyLocked(this.mPolicy.checkShowToOwnerOnly(attrs));
                            res = this.mPolicy.prepareAddWindowLw(win, attrs);
                            if (res != 0) {
                                resetPriorityAfterLockedSection();
                                return res;
                            }
                            boolean openInputChannels = outInputChannel != null ? (attrs.inputFeatures & 2) == 0 : false;
                            if (openInputChannels) {
                                win.openInputChannel(outInputChannel);
                            }
                            if (type == 2005) {
                                if (getDefaultDisplayContentLocked().canAddToastWindowForUid(callingUid)) {
                                    if (!addToastWindowRequiresToken) {
                                        if ((attrs.flags & 8) != 0) {
                                            if (this.mCurrentFocus != null) {
                                            }
                                        }
                                    }
                                    this.mH.sendMessageDelayed(this.mH.obtainMessage(52, win), win.mAttrs.hideTimeoutMilliseconds);
                                } else {
                                    Slog.w(TAG, "Adding more than one toast window for UID at a time.");
                                    resetPriorityAfterLockedSection();
                                    return -5;
                                }
                            }
                            res = 0;
                            if (this.mCurrentFocus == null) {
                                this.mWinAddedSinceNullFocus.add(win);
                            }
                            if (excludeWindowTypeFromTapOutTask(type) || excludeWindowsFromTapOutTask(win)) {
                                displayContent.mTapExcludedWindows.add(win);
                            }
                            long origId = Binder.clearCallingIdentity();
                            win.attach();
                            this.mWindowMap.put(client.asBinder(), win);
                            int opMode = 3;
                            if (win.mAppOp != -1) {
                                int startOpResult = this.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                                if (!(startOpResult == 0 || startOpResult == 3)) {
                                    setAppOpHideHook(win, false);
                                }
                                opMode = startOpResult;
                            }
                            addWindowReport(win, opMode);
                            setVisibleFromParent(win);
                            win.lambda$-com_android_server_wm_WindowManagerService_375781(this.mHidingNonSystemOverlayWindows.isEmpty() ^ 1);
                            AppWindowToken aToken = token.asAppWindowToken();
                            if (type == 3 && aToken != null) {
                                aToken.startingWindow = win;
                            }
                            boolean imMayMove = true;
                            win.mToken.addWindow(win);
                            if (type == 2011) {
                                win.mGivenInsetsPending = true;
                                setInputMethodWindowLocked(win);
                                imMayMove = false;
                            } else if (type == 2012) {
                                displayContent.computeImeTarget(true);
                                imMayMove = false;
                            } else if (type == 2013) {
                                displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
                                displayContent.pendingLayoutChanges |= 4;
                            } else if ((attrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                                displayContent.pendingLayoutChanges |= 4;
                            } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
                                displayContent.pendingLayoutChanges |= 4;
                            }
                            win.applyAdjustForImeIfNeeded();
                            if (type == 2034) {
                                this.mRoot.getDisplayContent(displayId).getDockedDividerController().setWindow(win);
                            }
                            WindowStateAnimator winAnimator = win.mWinAnimator;
                            winAnimator.mEnterAnimationPending = true;
                            winAnimator.mEnteringAnimation = true;
                            if (!(atoken == null || !atoken.isVisible() || (prepareWindowReplacementTransition(atoken) ^ 1) == 0)) {
                                prepareNoneTransitionForRelaunching(atoken);
                            }
                            if (displayContent.isDefaultDisplay) {
                                Rect taskBounds;
                                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                                if (atoken == null || atoken.getTask() == null) {
                                    taskBounds = null;
                                } else {
                                    taskBounds = this.mTmpRect;
                                    atoken.getTask().getBounds(this.mTmpRect);
                                }
                                if (this.mPolicy.getInsetHintLw(win.mAttrs, taskBounds, displayInfo.rotation, displayInfo.logicalWidth, displayInfo.logicalHeight, outContentInsets, outStableInsets, outOutsets)) {
                                    res = 4;
                                }
                            } else {
                                outContentInsets.setEmpty();
                                outStableInsets.setEmpty();
                            }
                            if (this.mInTouchMode) {
                                res |= 1;
                            }
                            if (win.mAppToken == null || (win.mAppToken.isClientHidden() ^ 1) != 0) {
                                res |= 2;
                            }
                            this.mInputMonitor.setUpdateInputWindowsNeededLw();
                            boolean focusChanged = false;
                            if (win.canReceiveKeys()) {
                                focusChanged = updateFocusedWindowLocked(1, false);
                                if (focusChanged) {
                                    imMayMove = false;
                                }
                            }
                            if (imMayMove) {
                                int isValidExtDisplayId;
                                if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                                    isValidExtDisplayId = HwPCUtils.isValidExtDisplayId(displayContent.getDisplayId()) ^ 1;
                                } else {
                                    isValidExtDisplayId = 0;
                                }
                                if (isValidExtDisplayId == 0) {
                                    displayContent.computeImeTarget(true);
                                }
                            }
                            displayContent.assignWindowLayers(false);
                            if (focusChanged) {
                                this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, false);
                            }
                            this.mInputMonitor.updateInputWindowsLw(false);
                            Slog.v(TAG, "addWindow: New client " + client.asBinder() + ": window=" + win + " Callers=" + Debug.getCallers(5));
                            if (win.isVisibleOrAdding() && updateOrientationFromAppTokensLocked(false, displayId)) {
                                reportNewConfig = true;
                            }
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                } else {
                    Slog.w(TAG, "Attempted to add window to a display for which the application does not have access: " + displayId + ".  Aborting.");
                    resetPriorityAfterLockedSection();
                    return -9;
                }
            }
            throw new IllegalStateException("Display has not been initialialized");
        }
        return -9;
    }

    private boolean doesAddToastWindowRequireToken(String packageName, int callingUid, WindowState attachedWindow) {
        boolean z = true;
        if (attachedWindow != null) {
            if (attachedWindow.mAppToken == null) {
                z = false;
            } else if (attachedWindow.mAppToken.mTargetSdk < 26) {
                z = false;
            }
            return z;
        }
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
            if (appInfo.uid == callingUid) {
                return appInfo.targetSdkVersion >= 26;
            } else {
                throw new SecurityException("Package " + packageName + " not in UID " + callingUid);
            }
        } catch (NameNotFoundException e) {
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
        if (this.mDisplayFrozen && (this.mOpeningApps.contains(atoken) ^ 1) != 0 && atoken.isRelaunching()) {
            this.mOpeningApps.add(atoken);
            prepareAppTransition(0, false);
            executeAppTransition();
        }
    }

    boolean isScreenCaptureDisabledLocked(int userId) {
        Boolean disabled = (Boolean) this.mScreenCaptureDisabled.get(userId);
        if (disabled == null) {
            return false;
        }
        return disabled.booleanValue();
    }

    boolean isSecureLocked(WindowState w) {
        return (w.mAttrs.flags & 8192) != 0 || isScreenCaptureDisabledLocked(UserHandle.getUserId(w.mOwnerUid));
    }

    public void enableSurfaceTrace(ParcelFileDescriptor pfd) {
        int callingUid = Binder.getCallingUid();
        if (callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.enableSurfaceTrace(pfd);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Only shell can call enableSurfaceTrace");
    }

    public void disableSurfaceTrace() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0 || callingUid == 1000) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.disableSurfaceTrace();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Only shell can call disableSurfaceTrace");
    }

    public void setScreenCaptureDisabled(int userId, boolean disabled) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only system can call setScreenCaptureDisabled.");
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mScreenCaptureDisabled.put(userId, Boolean.valueOf(disabled));
                this.mRoot.setSecureSurfaceState(userId, disabled);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void removeWindow(Session session, IWindow client) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                } else {
                    win.removeIfPossible();
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void postWindowRemoveCleanupLocked(WindowState win) {
        Slog.v(TAG, "postWindowRemoveCleanupLocked: " + win);
        this.mWindowMap.remove(win.mClient.asBinder());
        if (win.mAppOp != -1) {
            this.mAppOps.finishOp(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
        }
        removeWindowReport(win);
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
        if (!(dc == null || (this.mWindowPlacerLocked.isInLayout() ^ 1) == 0)) {
            dc.assignWindowLayers(true);
            this.mWindowPlacerLocked.performSurfacePlacement();
            if (win.mAppToken != null) {
                win.mAppToken.updateReportedVisibilityLocked();
            }
        }
        this.mInputMonitor.updateInputWindowsLw(true);
    }

    void setInputMethodWindowLocked(WindowState win) {
        this.mInputMethodWindow = win;
        (win != null ? win.getDisplayContent() : getDefaultDisplayContentLocked()).computeImeTarget(true);
    }

    void updateAppOpsState() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.updateAppOpsState();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
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

    void setTransparentRegionWindow(Session session, IWindow client, Region region) {
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
        }
    }

    void setInsetsWindow(Session session, IWindow client, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                WindowState w = windowForClientLocked(session, client, false);
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
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void getWindowDisplayFrame(Session session, IWindow client, Rect outDisplayFrame) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    outDisplayFrame.setEmpty();
                } else {
                    outDisplayFrame.set(win.mDisplayFrame);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public IWindowId getWindowId(IBinder token) {
        IWindowId iWindowId = null;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState window = (WindowState) this.mWindowMap.get(token);
                if (window != null) {
                    iWindowId = window.mWindowId;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return iWindowId;
    }

    public void pokeDrawLock(Session session, IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState window = windowForClientLocked(session, token, false);
                if (window != null) {
                    window.pokeDrawLockLw(this.mDrawLockTimeoutMillis);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:189:0x0616, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:190:0x0619, code:
            if (r7 == false) goto L_0x0631;
     */
    /* JADX WARNING: Missing block: B:191:0x061b, code:
            android.os.Trace.traceBegin(32, "relayoutWindow: sendNewConfiguration");
            sendNewConfiguration(r9);
            android.os.Trace.traceEnd(32);
     */
    /* JADX WARNING: Missing block: B:192:0x0631, code:
            android.os.Binder.restoreCallingIdentity(r18);
     */
    /* JADX WARNING: Missing block: B:193:0x0634, code:
            return r20;
     */
    /* JADX WARNING: Missing block: B:200:0x0663, code:
            if (r26.mAttrs.surfaceInsets.bottom == 0) goto L_0x02dd;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int relayoutWindow(Session session, IWindow client, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, MergedConfiguration mergedConfiguration, Surface outSurface) {
        int result = 0;
        boolean hasStatusBarPermission = this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0;
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mWindowMap) {
            WindowState win;
            try {
                boostPriorityForLockedSection();
                win = windowForClientLocked(session, client, false);
                if (win == null) {
                    resetPriorityAfterLockedSection();
                    return 0;
                }
                boolean shouldRelayout;
                int displayId = win.getDisplayId();
                WindowStateAnimator winAnimator = win.mWinAnimator;
                if (viewVisibility != 8) {
                    win.setRequestedSize(requestedWidth, requestedHeight);
                }
                int attrChanges = 0;
                int flagChanges = 0;
                if (attrs != null) {
                    this.mPolicy.adjustWindowParamsLw(attrs);
                    if (seq == win.mSeq) {
                        int systemUiVisibility = attrs.systemUiVisibility | attrs.subtreeSystemUiVisibility;
                        if (!((67043328 & systemUiVisibility) == 0 || hasStatusBarPermission)) {
                            systemUiVisibility &= -67043329;
                        }
                        win.mSystemUiVisibility = systemUiVisibility;
                    }
                    if (win.mAttrs.type != attrs.type) {
                        throw new IllegalArgumentException("Window type can not be changed after the window is added, type changed from " + win.mAttrs.type + " to " + attrs.type);
                    }
                    if ((attrs.privateFlags & 8192) != 0) {
                        attrs.x = win.mAttrs.x;
                        attrs.y = win.mAttrs.y;
                        attrs.width = win.mAttrs.width;
                        attrs.height = win.mAttrs.height;
                    }
                    LayoutParams layoutParams = win.mAttrs;
                    flagChanges = layoutParams.flags ^ attrs.flags;
                    layoutParams.flags = flagChanges;
                    attrChanges = win.mAttrs.copyFrom(attrs);
                    if ((attrChanges & 16385) != 0) {
                        win.mLayoutNeeded = true;
                    }
                    if (!(win.mAppToken == null || ((DumpState.DUMP_FROZEN & flagChanges) == 0 && (DumpState.DUMP_CHANGES & flagChanges) == 0))) {
                        win.mAppToken.checkKeyguardFlagsChanged();
                    }
                    if (!((33554432 & attrChanges) == 0 || this.mAccessibilityController == null || (win.getDisplayId() != 0 && (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.enabledInPad())))) {
                        this.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
                    }
                }
                if (win.mViewVisibility != viewVisibility) {
                    Flog.i(307, "Relayout " + win + ": viewVisibility=" + viewVisibility + " req=" + requestedWidth + "x" + requestedHeight + " " + win.mAttrs);
                }
                winAnimator.mSurfaceDestroyDeferred = (flags & 2) != 0;
                if (this.mCurrentFocus == win && (Integer.MIN_VALUE & attrChanges) != 0) {
                    this.mPolicy.updateSystemUiColorLw(win);
                }
                win.mEnforceSizeCompat = (win.mAttrs.privateFlags & 128) != 0;
                if ((attrChanges & 128) != 0) {
                    winAnimator.mAlpha = attrs.alpha;
                }
                win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                if (win.mAttrs.surfaceInsets.left == 0 && win.mAttrs.surfaceInsets.top == 0) {
                    if (win.mAttrs.surfaceInsets.right == 0) {
                    }
                }
                winAnimator.setOpaqueLocked(false);
                boolean imMayMove = (131080 & flagChanges) != 0;
                boolean isDefaultDisplay = win.isDefaultDisplay();
                boolean focusMayChange = isDefaultDisplay ? (win.mViewVisibility == viewVisibility && (flagChanges & 8) == 0) ? win.mRelayoutCalled ^ 1 : true : false;
                boolean wallpaperMayMove = win.mViewVisibility != viewVisibility ? (win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0 : false;
                wallpaperMayMove |= (DumpState.DUMP_DEXOPT & flagChanges) != 0 ? 1 : 0;
                if (!((flagChanges & 8192) == 0 || winAnimator.mSurfaceController == null)) {
                    winAnimator.mSurfaceController.setSecure(isSecureLocked(win));
                }
                win.mRelayoutCalled = true;
                win.mInRelayout = true;
                int oldVisibility = win.mViewVisibility;
                win.mViewVisibility = viewVisibility;
                if (viewVisibility != 0) {
                    shouldRelayout = false;
                } else if (win.mAppToken == null || win.mAttrs.type == 3) {
                    shouldRelayout = true;
                } else {
                    shouldRelayout = win.mAppToken.isClientHidden() ^ 1;
                }
                if (shouldRelayout) {
                    Trace.traceBegin(32, "relayoutWindow: viewVisibility_1");
                    if (win.mLayoutSeq == -1 || (win.mFrame.width() == 0 && win.mFrame.height() == 0)) {
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement(true);
                    }
                    result = createSurfaceControl(outSurface, win.relayoutVisibleWindow(0, attrChanges, oldVisibility), win, winAnimator);
                    if ((result & 2) != 0) {
                        focusMayChange = isDefaultDisplay;
                    }
                    if (win.mAttrs.type == 2011 && (this.mInputMethodWindow == null || this.mInputMethodWindow != win)) {
                        setInputMethodWindowLocked(win);
                        imMayMove = true;
                    }
                    if (win.mAttrs.type == 2012 && this.mInputMethodWindow == null) {
                        if (((HwPCUtils.enabledInPad() ? HwPCUtils.isPcCastModeInServer() : 0) ^ 1) != 0) {
                            Slog.d(TAG, "relayoutwindow TYPE_INPUT_METHOD_DIALOG , do setInputMethodWindowLocked");
                            setInputMethodWindowLocked(win);
                        }
                    }
                    win.adjustStartingWindowFlags();
                    Trace.traceEnd(32);
                } else {
                    Trace.traceBegin(32, "relayoutWindow: viewVisibility_2");
                    winAnimator.mEnterAnimationPending = false;
                    winAnimator.mEnteringAnimation = false;
                    int usingSavedSurfaceBeforeVisible = oldVisibility != 0 ? win.isAnimatingWithSavedSurface() : 0;
                    if (!(!winAnimator.hasSurface() || (win.mAnimatingExit ^ 1) == 0 || (usingSavedSurfaceBeforeVisible ^ 1) == 0)) {
                        if (!win.mWillReplaceWindow) {
                            focusMayChange = tryStartExitingAnimation(win, winAnimator, isDefaultDisplay, focusMayChange);
                        }
                        result = 4;
                    }
                    String winTitle;
                    if (viewVisibility == 0 && winAnimator.hasSurface()) {
                        Trace.traceBegin(32, "relayoutWindow: getSurface");
                        winAnimator.mSurfaceController.getSurface(outSurface);
                        winTitle = win.mAttrs.getTitle().toString();
                        if (winTitle != null && winTitle.contains("Toast")) {
                            Slog.w(TAG, "EGLdebug surface name is " + winTitle + "getSurface " + outSurface);
                        }
                        Trace.traceEnd(32);
                    } else {
                        Trace.traceBegin(32, "wmReleaseOutSurface_" + win.mAttrs.getTitle());
                        outSurface.release();
                        winTitle = win.mAttrs.getTitle().toString();
                        if (winTitle != null && winTitle.contains("Toast")) {
                            Slog.w(TAG, "EGLdebug surface name is " + winTitle + "releaseSurface " + outSurface);
                        }
                        Trace.traceEnd(32);
                    }
                    Trace.traceEnd(32);
                }
                setHwSecureScreen(win);
                if (focusMayChange && updateFocusedWindowLocked(3, false)) {
                    imMayMove = false;
                }
                boolean toBeDisplayed = (result & 2) != 0;
                DisplayContent dc = win.getDisplayContent();
                if (imMayMove) {
                    dc.computeImeTarget(true);
                    if (toBeDisplayed) {
                        dc.assignWindowLayers(false);
                    }
                }
                if (wallpaperMayMove) {
                    DisplayContent displayContent = win.getDisplayContent();
                    displayContent.pendingLayoutChanges |= 4;
                }
                if (win.mAppToken != null) {
                    this.mUnknownAppVisibilityController.notifyRelayouted(win.mAppToken);
                }
                win.setDisplayLayoutNeeded();
                win.mGivenInsetsPending = (flags & 1) != 0;
                Trace.traceBegin(32, "relayoutWindow: updateOrientationFromAppTokens");
                boolean configChanged = updateOrientationFromAppTokensLocked(false, displayId);
                Trace.traceEnd(32);
                this.mWindowPlacerLocked.performSurfacePlacement(true);
                if (toBeDisplayed && win.mIsWallpaper) {
                    DisplayInfo displayInfo = win.getDisplayContent().getDisplayInfo();
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
                if (!win.isGoneForLayoutLw()) {
                    win.mResizedWhileGone = false;
                }
                if (shouldRelayout) {
                    win.getMergedConfiguration(mergedConfiguration);
                } else {
                    win.getLastReportedMergedConfiguration(mergedConfiguration);
                }
                win.setLastReportedMergedConfiguration(mergedConfiguration);
                outFrame.set(win.mCompatFrame);
                outOverscanInsets.set(win.mOverscanInsets);
                outContentInsets.set(win.mContentInsets);
                win.mLastRelayoutContentInsets.set(win.mContentInsets);
                outVisibleInsets.set(win.mVisibleInsets);
                outStableInsets.set(win.mStableInsets);
                outOutsets.set(win.mOutsets);
                outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                result |= this.mInTouchMode ? 1 : 0;
                this.mInputMonitor.updateInputWindowsLw(true);
                win.mInRelayout = false;
            } catch (Exception e) {
                this.mInputMonitor.updateInputWindowsLw(true);
                Slog.w(TAG, "Exception thrown when creating surface for client " + client + " (" + win.mAttrs.getTitle() + ")", e);
                Binder.restoreCallingIdentity(origId);
                resetPriorityAfterLockedSection();
                return 0;
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean tryStartExitingAnimation(WindowState win, WindowStateAnimator winAnimator, boolean isDefaultDisplay, boolean focusMayChange) {
        int transit = 2;
        if (win.mAttrs.type == 3) {
            transit = 5;
        }
        if (win.isWinVisibleLw() && (shouldHideIMExitAnim(win) ^ 1) != 0 && winAnimator.applyAnimationLocked(transit, false)) {
            focusMayChange = isDefaultDisplay;
            win.mAnimatingExit = true;
            win.mWinAnimator.mAnimating = true;
        } else if (win.mWinAnimator.isAnimationSet()) {
            win.mAnimatingExit = true;
            win.mWinAnimator.mAnimating = true;
        } else if (win.getDisplayContent().mWallpaperController.isWallpaperTarget(win)) {
            win.mAnimatingExit = true;
            win.mWinAnimator.mAnimating = true;
        } else {
            if (this.mInputMethodWindow == win) {
                setInputMethodWindowLocked(null);
            }
            boolean stopped = win.mAppToken != null ? win.mAppToken.mAppStopped : false;
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
                String winTitle = win.mAttrs.getTitle().toString();
                if (winTitle != null && winTitle.contains("Toast")) {
                    Slog.w(TAG, "EGLdebug surface name is " + winTitle + "surface is " + outSurface);
                }
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
        }
    }

    void finishDrawingWindow(Session session, IWindow client) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(session, client, false);
                Slog.d(TAG, "finishDrawingWindow: " + win + " mDrawState=" + (win != null ? win.mWinAnimator.drawStateToString() : "null"));
                if (win != null && win.mWinAnimator.finishDrawingLocked()) {
                    if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                        DisplayContent displayContent = win.getDisplayContent();
                        displayContent.pendingLayoutChanges |= 4;
                    }
                    win.setDisplayLayoutNeeded();
                    this.mWindowPlacerLocked.requestTraversal();
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    protected boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
        return okToDisplay();
    }

    boolean applyAnimationLocked(AppWindowToken atoken, LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        Trace.traceBegin(32, "WM#applyAnimationLocked");
        DisplayContent displayContent = atoken.getTask().getDisplayContent();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        int width = displayInfo.appWidth;
        int height = displayInfo.appHeight;
        if (isDisplayOkForAnimation(width, height, transit, atoken)) {
            WindowState win = atoken.findMainWindow();
            Rect frame = new Rect(0, 0, width, height);
            Rect displayFrame = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
            Rect insets = new Rect();
            Rect surfaceInsets = null;
            boolean freeform = win != null ? win.inFreeformWorkspace() : false;
            if (win != null) {
                if (freeform) {
                    frame.set(win.mFrame);
                } else {
                    frame.set(win.mContainingFrame);
                }
                surfaceInsets = win.getAttrs().surfaceInsets;
                insets.set(win.mContentInsets);
            }
            if (atoken.mLaunchTaskBehind) {
                enter = false;
            }
            Configuration displayConfig = displayContent.getConfiguration();
            Animation a = this.mAppTransition.loadAnimation(lp, transit, enter, displayConfig.uiMode, displayConfig.orientation, frame, displayFrame, insets, surfaceInsets, isVoiceInteraction, freeform, atoken.getTask().mTaskId);
            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId)) {
                a = this.mAppTransition.loadAnimationRes(lp, enter ? 17432576 : 17432577);
                a.setDuration(300);
            }
            if (a != null) {
                atoken.mAppAnimator.setAnimation(a, frame.width(), frame.height(), width, height, this.mAppTransition.canSkipFirstFrame(), this.mAppTransition.getAppStackClipMode(), transit, this.mAppTransition.getTransitFlags());
            }
        } else {
            atoken.mAppAnimator.clearAnimation();
        }
        Trace.traceEnd(32);
        if (atoken.mAppAnimator.animation != null) {
            return true;
        }
        return false;
    }

    boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    boolean okToDisplay() {
        return (this.mDisplayFrozen || !this.mDisplayEnabled) ? false : this.mPolicy.isScreenOn();
    }

    /* JADX WARNING: Missing block: B:17:0x0075, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:18:0x0078, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addWindowToken(IBinder binder, int type, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    DisplayContent dc = this.mRoot.getDisplayContentOrCreate(displayId);
                    WindowToken token = dc.getWindowToken(binder);
                    if (token != null) {
                        Slog.w(TAG, "addWindowToken: Attempted to add binder token: " + binder + " for already created window token: " + token + " displayId=" + displayId);
                    } else if (type == 2013) {
                        WallpaperWindowToken wallpaperWindowToken = new WallpaperWindowToken(this, binder, true, dc, true);
                    } else {
                        WindowToken windowToken = new WindowToken(this, binder, type, true, dc, true);
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    protected boolean isTokenFound(IBinder binder, DisplayContent dc) {
        return false;
    }

    protected void setFocusedDisplay(int displayId, boolean findTopTask, String reason) {
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
                    } else if (dc.removeWindowToken(binder) != null || isTokenFound(binder, dc)) {
                        this.mInputMonitor.updateInputWindowsLw(true);
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                    } else {
                        Slog.w(TAG, "removeWindowToken: Attempted to remove non-existing token: " + binder);
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "updateOrientationFromAppTokens()")) {
            long ident = Binder.clearCallingIdentity();
            try {
                Configuration config;
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    config = updateOrientationFromAppTokensLocked(currentConfig, freezeThisOneIfNeeded, displayId);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
                return config;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }
    }

    private Configuration updateOrientationFromAppTokensLocked(Configuration currentConfig, IBinder freezeThisOneIfNeeded, int displayId) {
        if (!this.mDisplayReady) {
            return null;
        }
        Configuration config = null;
        if (updateOrientationFromAppTokensLocked(false, displayId)) {
            if (!(freezeThisOneIfNeeded == null || (this.mRoot.mOrientationChangeComplete ^ 1) == 0)) {
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
                if (displayContent.isDimming()) {
                    anim[1] = 0;
                    anim[0] = 0;
                } else {
                    this.mPolicy.selectRotationAnimationLw(anim);
                }
                if (!this.mIgnoreFrozen) {
                    startFreezingDisplayLocked(false, anim[0], anim[1], displayContent);
                }
                config = new Configuration(this.mTempConfiguration);
            }
        }
        if (this.mIgnoreFrozen) {
            this.mIgnoreFrozen = false;
        }
        return config;
    }

    boolean updateOrientationFromAppTokensLocked(boolean inTransaction, int displayId) {
        long ident = Binder.clearCallingIdentity();
        try {
            DisplayContent dc = this.mRoot.getDisplayContent(displayId);
            if (dc == null) {
                return false;
            }
            int req = dc.getOrientation();
            if (req != dc.getLastOrientation()) {
                startIntelliServiceFR(req);
                dc.setLastOrientation(req);
                if (dc.isDefaultDisplay) {
                    this.mPolicy.setCurrentOrientationLw(req);
                }
                if (dc.updateRotationUnchecked(inTransaction)) {
                    Binder.restoreCallingIdentity(ident);
                    return true;
                }
            }
            Binder.restoreCallingIdentity(ident);
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean rotationNeedsUpdateLocked() {
        DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
        int lastOrientation = defaultDisplayContent.getLastOrientation();
        int oldRotation = defaultDisplayContent.getRotation();
        boolean oldAltOrientation = defaultDisplayContent.getAltOrientation();
        int rotation = this.mPolicy.rotationForOrientationLw(lastOrientation, oldRotation);
        boolean altOrientation = this.mPolicy.rotationHasCompatibleMetricsLw(lastOrientation, rotation) ^ 1;
        if (oldRotation == rotation && oldAltOrientation == altOrientation) {
            return false;
        }
        return true;
    }

    public int[] setNewDisplayOverrideConfiguration(Configuration overrideConfig, int displayId) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewDisplayOverrideConfiguration()")) {
            int[] displayOverrideConfigurationIfNeeded;
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (this.mWaitingForConfig) {
                        this.mWaitingForConfig = false;
                        this.mLastFinishedFreezeSource = "new-config";
                    }
                    displayOverrideConfigurationIfNeeded = this.mRoot.setDisplayOverrideConfigurationIfNeeded(overrideConfig, displayId);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return displayOverrideConfigurationIfNeeded;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    void setFocusTaskRegionLocked(AppWindowToken previousFocus) {
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
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setFocusedApp()")) {
            synchronized (this.mWindowMap) {
                try {
                    AppWindowToken newFocus;
                    boostPriorityForLockedSection();
                    if (token == null) {
                        newFocus = null;
                    } else {
                        newFocus = this.mRoot.getAppWindowToken(token);
                        if (newFocus == null) {
                            Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                        }
                    }
                    boolean changed = this.mFocusedApp != newFocus;
                    if (changed) {
                        AppWindowToken prev = this.mFocusedApp;
                        this.mFocusedApp = newFocus;
                        if (!(!HwPCUtils.isPcCastModeInServer() || this.mFocusedApp == null || (this.mFocusedApp.getDisplayContent().isDefaultDisplay ^ 1) == 0)) {
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
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
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
                    if (this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent, flags, forceOverride) && okToDisplay()) {
                        this.mSkipAppTransitionAnimation = false;
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionScaleUp(startX, startY, startWidth, startHeight);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionClipReveal(startX, startY, startWidth, startHeight);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionThumb(GraphicBuffer srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionThumb(srcThumb, startX, startY, startedCallback, scaleUp);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionAspectScaledThumb(GraphicBuffer srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionAspectScaledThumb(srcThumb, startX, startY, targetWidth, targetHeight, startedCallback, scaleUp);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionMultiThumb(specs, onAnimationStartedCallback, onAnimationFinishedCallback, scaleUp);
                prolongAnimationsFromSpecs(specs, scaleUp);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void prolongAnimationsFromSpecs(AppTransitionAnimationSpec[] specs, boolean scaleUp) {
        this.mTmpTaskIds.clear();
        for (int i = specs.length - 1; i >= 0; i--) {
            this.mTmpTaskIds.put(specs[i].taskId, 0);
        }
        for (WindowState win : this.mWindowMap.values()) {
            Task task = win.getTask();
            if (!(task == null || this.mTmpTaskIds.get(task.mTaskId, -1) == -1 || !task.inFreeformWorkspace())) {
                AppWindowToken appToken = win.mAppToken;
                if (!(appToken == null || appToken.mAppAnimator == null)) {
                    appToken.mAppAnimator.startProlongAnimation(scaleUp ? 2 : 1);
                }
            }
        }
    }

    public void overridePendingAppTransitionInPlace(String packageName, int anim) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overrideInPlaceAppTransition(packageName, anim);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mAppTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, callback, scaleUp);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void endProlongedAnimations() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                for (WindowState win : this.mWindowMap.values()) {
                    AppWindowToken appToken = win.mAppToken;
                    if (!(appToken == null || appToken.mAppAnimator == null)) {
                        appToken.mAppAnimator.endProlongedAnimation();
                    }
                }
                this.mAppTransition.notifyProlongedAnimationsEnded();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void executeAppTransition() {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "executeAppTransition()")) {
            synchronized (this.mWindowMap) {
                long origId;
                try {
                    boostPriorityForLockedSection();
                    if (this.mAppTransition.isTransitionSet()) {
                        this.mAppTransition.setReady();
                        origId = Binder.clearCallingIdentity();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setWindowOpaque(IBinder token, boolean isOpaque) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                setWindowOpaqueLocked(token, isOpaque);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void setWindowOpaqueLocked(IBinder token, boolean isOpaque) {
        AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
        if (wtoken != null) {
            WindowState win = wtoken.findMainWindow();
            if (win != null) {
                win.mWinAnimator.setOpaqueLocked(isOpaque);
            }
        }
    }

    void updateTokenInPlaceLocked(AppWindowToken wtoken, int transit) {
        if (transit != -1) {
            if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                wtoken.mAppAnimator.setNullAnimation();
            }
            applyAnimationLocked(wtoken, null, transit, false, false);
        }
    }

    public void setDockedStackCreateState(int mode, Rect bounds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                setDockedStackCreateStateLocked(mode, bounds);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void setDockedStackCreateStateLocked(int mode, Rect bounds) {
        this.mDockedStackCreateMode = mode;
        this.mDockedStackCreateBounds = bounds;
    }

    public boolean isValidPictureInPictureAspectRatio(int displayId, float aspectRatio) {
        return this.mRoot.getDisplayContent(displayId).getPinnedStackController().isValidPictureInPictureAspectRatio(aspectRatio);
    }

    public void getStackBounds(int stackId, Rect bounds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                TaskStack stack = this.mRoot.getStackById(stackId);
                if (stack != null) {
                    stack.getBounds(bounds);
                } else {
                    bounds.setEmpty();
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyShowingDreamChanged() {
        notifyKeyguardFlagsChanged(null);
    }

    public WindowState getInputMethodWindowLw() {
        return this.mInputMethodWindow;
    }

    public void notifyKeyguardTrustedChanged() {
        this.mH.sendEmptyMessage(57);
    }

    public void screenTurningOff(ScreenOffListener listener) {
        this.mTaskSnapshotController.screenTurningOff(listener);
    }

    public void deferSurfaceLayout() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.deferLayout();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void continueSurfaceLayout() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.continueLayout();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean containsShowWhenLockedWindow(IBinder token) {
        boolean containsShowWhenLockedWindow;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
                containsShowWhenLockedWindow = wtoken != null ? wtoken.containsShowWhenLockedWindow() : false;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return containsShowWhenLockedWindow;
    }

    public boolean containsDismissKeyguardWindow(IBinder token) {
        boolean containsDismissKeyguardWindow;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken wtoken = this.mRoot.getAppWindowToken(token);
                containsDismissKeyguardWindow = wtoken != null ? wtoken.containsDismissKeyguardWindow() : false;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return containsDismissKeyguardWindow;
    }

    void notifyKeyguardFlagsChanged(Runnable callback) {
        Object wrappedCallback;
        if (callback != null) {
            wrappedCallback = new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass3(this, callback);
        } else {
            wrappedCallback = null;
        }
        this.mH.obtainMessage(56, wrappedCallback).sendToTarget();
    }

    /* synthetic */ void lambda$-com_android_server_wm_WindowManagerService_154813(Runnable callback) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                callback.run();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isKeyguardTrusted() {
        boolean isKeyguardTrustedLw;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                isKeyguardTrustedLw = this.mPolicy.isKeyguardTrustedLw();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return isKeyguardTrustedLw;
    }

    public void setKeyguardGoingAway(boolean keyguardGoingAway) {
    }

    public void startFreezingScreen(int exitAnim, int enterAnim) {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                long origId;
                try {
                    boostPriorityForLockedSection();
                    if (!this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = true;
                        origId = Binder.clearCallingIdentity();
                        startFreezingDisplayLocked(false, exitAnim, enterAnim);
                        this.mH.removeMessages(30);
                        this.mH.sendEmptyMessageDelayed(30, 5000);
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void stopFreezingScreen() {
        if (checkCallingPermission("android.permission.FREEZE_SCREEN", "stopFreezingScreen()")) {
            synchronized (this.mWindowMap) {
                long origId;
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
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new SecurityException("Requires FREEZE_SCREEN permission");
    }

    public void disableKeyguard(IBinder token, String tag) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (Binder.getCallingUid() != 1000 && isKeyguardSecure()) {
            Log.d(TAG, "current mode is SecurityMode, ignore disableKeyguard");
        } else if (!isCurrentProfileLocked(UserHandle.getCallingUserId())) {
            Log.d(TAG, "non-current profiles, ignore disableKeyguard");
        } else if (token == null) {
            throw new IllegalArgumentException("token == null");
        } else {
            this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(1, new Pair(token, tag)));
            Slog.i(TAG, "disableKeyguard pid = " + Binder.getCallingPid() + " ,callers = " + Debug.getCallers(5));
        }
    }

    public void reenableKeyguard(IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            Log.e(TAG, "SecurityException is in reenableKeyguard!");
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (token == null) {
            Log.e(TAG, "IllegalArgumentException is in reenableKeyguard!");
            throw new IllegalArgumentException("token == null");
        } else {
            this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(2, token));
            Slog.i(TAG, "reenableKeyguard pid = " + Binder.getCallingPid() + " ,callers = " + Debug.getCallers(5));
        }
    }

    public void exitKeyguardSecurely(final IOnKeyguardExitResult callback) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        } else {
            this.mPolicy.exitKeyguardSecurely(new OnKeyguardExitResult() {
                public void onKeyguardExitResult(boolean success) {
                    try {
                        callback.onKeyguardExitResult(success);
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }

    public boolean inKeyguardRestrictedInputMode() {
        return this.mPolicy.inKeyguardRestrictedKeyInputMode();
    }

    public boolean isKeyguardLocked() {
        return this.mPolicy.isKeyguardLocked();
    }

    public boolean isKeyguardSecure() {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            boolean isKeyguardSecure = this.mPolicy.isKeyguardSecure(userId);
            return isKeyguardSecure;
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return isShowingDreamLw;
    }

    public void dismissKeyguard(IKeyguardDismissCallback callback) {
        checkCallingPermission("android.permission.CONTROL_KEYGUARD", "dismissKeyguard");
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.dismissKeyguardLw(callback);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void onKeyguardOccludedChanged(boolean occluded) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.onKeyguardOccludedChangedLw(occluded);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setSwitchingUser(boolean switching) {
        if (checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "setSwitchingUser()")) {
            this.mPolicy.setSwitchingUser(switching);
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mSwitchingUser = switching;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires INTERACT_ACROSS_USERS_FULL permission");
    }

    void showGlobalActions() {
        this.mPolicy.showGlobalActions();
    }

    public void closeSystemDialogs(String reason) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.closeSystemDialogs(reason);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
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
            scale = fixScale(scale);
            switch (which) {
                case 0:
                    this.mWindowAnimationScaleSetting = scale;
                    break;
                case 1:
                    this.mTransitionAnimationScaleSetting = scale;
                    break;
                case 2:
                    this.mAnimatorDurationScaleSetting = scale;
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
        return this.mAnimationsDisabled ? 0.0f : this.mWindowAnimationScaleSetting;
    }

    public float getTransitionAnimationScaleLocked() {
        return this.mAnimationsDisabled ? 0.0f : this.mTransitionAnimationScaleSetting;
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return f;
    }

    void dispatchNewAnimatorScaleLocked(Session session) {
        this.mH.obtainMessage(34, session).sendToTarget();
    }

    public void registerPointerEventListener(PointerEventListener listener) {
        if (listener != null) {
            this.mPointerEventDispatcher.registerInputEventListener(listener);
        }
    }

    public void unregisterPointerEventListener(PointerEventListener listener) {
        this.mPointerEventDispatcher.unregisterInputEventListener(listener);
    }

    boolean canDispatchPointerEvents() {
        return this.mPointerEventDispatcher != null;
    }

    public void registerExternalPointerEventListener(PointerEventListener listener) {
        if (listener != null) {
            this.mExternalPointerEventDispatcher.registerInputEventListener(listener);
        }
    }

    public void unregisterExternalPointerEventListener(PointerEventListener listener) {
        this.mExternalPointerEventDispatcher.unregisterInputEventListener(listener);
    }

    public int getFocusedDisplayId() {
        return 0;
    }

    boolean canDispatchExternalPointerEvents() {
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
        return sw == 0 ? 0 : -1;
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        boolean z = false;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mCurrentUserId = newUserId;
                this.mCurrentProfileIds = currentProfileIds;
                this.mAppTransition.setCurrentUser(newUserId);
                this.mPolicy.setCurrentUserLw(newUserId);
                this.mPolicy.enableKeyguard(true);
                this.mRoot.switchUser();
                this.mWindowPlacerLocked.performSurfacePlacement();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                TaskStack stack = displayContent.getDockedStackIgnoringVisibility();
                DockedStackDividerController dockedStackDividerController = displayContent.mDividerControllerLocked;
                if (stack != null) {
                    z = stack.hasTaskForUser(newUserId);
                }
                dockedStackDividerController.notifyDockedStackExistsChanged(z);
                if (this.mDisplayReady) {
                    int targetDensity;
                    int forcedDensity = getForcedDisplayDensityForUserLocked(newUserId);
                    if (forcedDensity != 0) {
                        targetDensity = forcedDensity;
                    } else {
                        targetDensity = displayContent.mInitialDisplayDensity;
                    }
                    setForcedDisplayDensityLocked(displayContent, targetDensity);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    boolean isCurrentProfileLocked(int userId) {
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
                } else {
                    this.mSystemBooted = true;
                    hideBootMessagesLocked();
                    this.mH.sendEmptyMessageDelayed(23, 30000);
                    resetPriorityAfterLockedSection();
                    this.mPolicy.systemBooted();
                    performEnableScreen();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void enableScreenIfNeeded() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                enableScreenIfNeededLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void enableScreenIfNeededLocked() {
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Slog.i(TAG, "enableScreenIfNeededLocked: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted, here);
        }
        if (!this.mDisplayEnabled) {
            if (this.mSystemBooted || (this.mShowingBootMessages ^ 1) == 0) {
                this.mH.sendEmptyMessage(16);
            }
        }
    }

    public void performBootTimeout() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mDisplayEnabled) {
                } else {
                    Slog.w(TAG, "***** BOOT TIMEOUT: forcing display enabled");
                    this.mForceDisplayEnabled = true;
                    resetPriorityAfterLockedSection();
                    performEnableScreen();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void onSystemUiStarted() {
        this.mPolicy.onSystemUiStarted();
    }

    /* JADX WARNING: Missing block: B:72:0x0146, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:74:?, code:
            r10.mActivityManager.bootAnimationComplete();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void performEnableScreen() {
        synchronized (this.mWindowMap) {
            boostPriorityForLockedSection();
            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                Slog.i(TAG, "performEnableScreen: mDisplayEnabled=" + this.mDisplayEnabled + " mForceDisplayEnabled=" + this.mForceDisplayEnabled + " mShowingBootMessages=" + this.mShowingBootMessages + " mSystemBooted=" + this.mSystemBooted + " mOnlyCore=" + this.mOnlyCore, new RuntimeException("here").fillInStackTrace());
            }
            if (this.mDisplayEnabled) {
                resetPriorityAfterLockedSection();
                return;
            } else if (this.mSystemBooted || (this.mShowingBootMessages ^ 1) == 0) {
                try {
                    if (!this.mShowingBootMessages && (this.mPolicy.canDismissBootAnimation() ^ 1) != 0) {
                        Flog.i(305, "Keyguard not drawn complete,can not dismiss boot animation");
                        resetPriorityAfterLockedSection();
                        return;
                    } else if (this.mForceDisplayEnabled || !getDefaultDisplayContentLocked().checkWaitingForWindows()) {
                        if (!this.mBootAnimationStopped) {
                            Trace.asyncTraceBegin(32, "Stop bootanim", 0);
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
                            this.mBootAnimationStopped = true;
                        }
                        if (this.mForceDisplayEnabled || (checkBootAnimationCompleteLocked() ^ 1) == 0) {
                            EventLog.writeEvent(EventLogTags.WM_BOOT_ANIMATION_DONE, SystemClock.uptimeMillis());
                            Trace.asyncTraceEnd(32, "Stop bootanim", 0);
                            this.mDisplayEnabled = true;
                            if (WindowManagerDebugConfig.DEBUG_BOOT) {
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
                    resetPriorityAfterLockedSection();
                }
            } else {
                resetPriorityAfterLockedSection();
                return;
            }
        }
        this.mPolicy.enableScreenAfterBoot();
        updateRotationUnchecked(false, false);
    }

    private boolean checkBootAnimationCompleteLocked() {
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

    /* JADX WARNING: Missing block: B:28:0x0089, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:29:0x008c, code:
            if (r0 == false) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:30:0x008e, code:
            performEnableScreen();
     */
    /* JADX WARNING: Missing block: B:31:0x0091, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                if (this.mAllowBootMessages) {
                    if (!this.mShowingBootMessages) {
                        if (always) {
                            first = true;
                        } else {
                            resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                    if (this.mSystemBooted) {
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    this.mShowingBootMessages = true;
                    this.mPolicy.showBootMessage(msg, always);
                }
            } finally {
                resetPriorityAfterLockedSection();
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void updateCircularDisplayMaskIfNeeded() {
        if (this.mContext.getResources().getConfiguration().isScreenRound() && this.mContext.getResources().getBoolean(17957069)) {
            int currentUserId;
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    currentUserId = this.mCurrentUserId;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            int showMask = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_inversion_enabled", 0, currentUserId) == 1 ? 0 : 1;
            Message m = this.mH.obtainMessage(35);
            m.arg1 = showMask;
            this.mH.sendMessage(m);
        }
    }

    public void showEmulatorDisplayOverlayIfNeeded() {
        if (this.mContext.getResources().getBoolean(17957065) && SystemProperties.getBoolean(PROPERTY_EMULATOR_CIRCULAR, false) && Build.IS_EMULATOR) {
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
                        this.mCircularDisplayMask = new CircularDisplayMask(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 10, this.mContext.getResources().getInteger(17694908), this.mContext.getResources().getDimensionPixelSize(17104944));
                    }
                    this.mCircularDisplayMask.setVisibility(true);
                } else if (this.mCircularDisplayMask != null) {
                    this.mCircularDisplayMask.setVisibility(false);
                    this.mCircularDisplayMask = null;
                }
                closeSurfaceTransaction();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void showEmulatorDisplayOverlay() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                openSurfaceTransaction();
                if (this.mEmulatorDisplayOverlay == null) {
                    this.mEmulatorDisplayOverlay = new EmulatorDisplayOverlay(this.mContext, getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 10);
                }
                this.mEmulatorDisplayOverlay.setVisibility(true);
                closeSurfaceTransaction();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
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

    private void showStrictModeViolation(int arg, int pid) {
        boolean on = arg != 0;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (!on || (this.mRoot.canShowStrictModeViolation(pid) ^ 1) == 0) {
                    SurfaceControl.openTransaction();
                    if (this.mStrictModeFlash == null) {
                        this.mStrictModeFlash = new StrictModeFlash(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession);
                    }
                    this.mStrictModeFlash.setVisibility(on);
                    SurfaceControl.closeTransaction();
                    resetPriorityAfterLockedSection();
                    return;
                }
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setStrictModeVisualIndicatorPreference(String value) {
        SystemProperties.set("persist.sys.strictmode.visual", value);
    }

    public Bitmap screenshotWallpaper() {
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotWallpaper()")) {
            try {
                Trace.traceBegin(32, "screenshotWallpaper");
                Bitmap screenshotApplications = screenshotApplications(null, 0, -1, -1, true, 1.0f, Config.ARGB_8888, true, false);
                return screenshotApplications;
            } finally {
                Trace.traceEnd(32);
            }
        } else {
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }
    }

    public boolean requestAssistScreenshot(IAssistScreenshotReceiver receiver) {
        if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "requestAssistScreenshot()")) {
            FgThread.getHandler().post(new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass4(this, receiver));
            return true;
        }
        throw new SecurityException("Requires READ_FRAME_BUFFER permission");
    }

    /* synthetic */ void lambda$-com_android_server_wm_WindowManagerService_188659(IAssistScreenshotReceiver receiver) {
        Bitmap bm = screenshotApplications(null, 0, -1, -1, true, 1.0f, Config.ARGB_8888, false, false);
        if (receiver != null) {
            try {
                receiver.send(bm);
            } catch (RemoteException e) {
            }
        }
    }

    public TaskSnapshot getTaskSnapshot(int taskId, int userId, boolean reducedResolution) {
        return this.mTaskSnapshotController.getSnapshot(taskId, userId, true, reducedResolution);
    }

    public void removeObsoleteTaskFiles(ArraySet<Integer> persistentTaskIds, int[] runningUserIds) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mTaskSnapshotController.removeObsoleteTaskFiles(persistentTaskIds, runningUserIds);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private Bitmap screenshotApplications(IBinder appToken, int displayId, int width, int height, boolean includeFullDisplay, float frameScale, Config config, boolean wallpaperOnly, boolean includeDecor) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null) {
                    resetPriorityAfterLockedSection();
                    return displayContent.screenshotApplications(appToken, width, height, includeFullDisplay, frameScale, config, wallpaperOnly, includeDecor);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return null;
    }

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
                WindowManagerPolicy windowManagerPolicy = this.mPolicy;
                if (rotation != -1) {
                    defaultDisplayRotation = rotation;
                }
                windowManagerPolicy.setUserRotationMode(1, defaultDisplayRotation);
                updateRotationUnchecked(false, false);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public void thawRotation() {
        if (checkCallingPermission("android.permission.SET_ORIENTATION", "thawRotation()")) {
            long origId = Binder.clearCallingIdentity();
            try {
                this.mPolicy.setUserRotationMode(0, 777);
                updateRotationUnchecked(false, false);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        }
    }

    public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        updateRotationUnchecked(alwaysSendConfiguration, forceRelayout);
    }

    void pauseRotationLocked() {
        this.mDeferredRotationPauseCount++;
    }

    void resumeRotationLocked() {
        if (this.mDeferredRotationPauseCount > 0) {
            this.mDeferredRotationPauseCount--;
            if (this.mDeferredRotationPauseCount == 0) {
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                if (displayContent.updateRotationUnchecked(false)) {
                    this.mH.obtainMessage(18, Integer.valueOf(displayContent.getDisplayId())).sendToTarget();
                }
            }
        }
    }

    void updateRotationUnchecked(boolean alwaysSendConfiguration, boolean forceRelayout) {
        Slog.v(TAG, "updateRotationUnchecked: alwaysSendConfiguration=" + alwaysSendConfiguration + " forceRelayout=" + forceRelayout);
        Trace.traceBegin(32, "updateRotation");
        long origId = Binder.clearCallingIdentity();
        try {
            boolean rotationChanged;
            int displayId;
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                Trace.traceBegin(32, "updateRotation: display");
                rotationChanged = displayContent.updateRotationUnchecked(false);
                Trace.traceEnd(32);
                if (rotationChanged) {
                    LogPower.push(128);
                }
                if (rotationChanged) {
                    if (!this.mIsPerfBoost) {
                        this.mIsPerfBoost = true;
                        UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{0});
                    }
                    if (this.mLastFinishedFreezeSource != null) {
                        Jlog.d(58, "" + this.mLastFinishedFreezeSource);
                    } else {
                        Jlog.d(58, "");
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
        }
    }

    public int getDefaultDisplayRotation() {
        int rotation;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                rotation = getDefaultDisplayContentLocked().getRotation();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return rotation;
    }

    public boolean isRotationFrozen() {
        return this.mPolicy.getUserRotationMode() == 1;
    }

    public int watchRotation(IRotationWatcher watcher, int displayId) {
        int defaultDisplayRotation;
        final IBinder watcherBinder = watcher.asBinder();
        DeathRecipient dr = new DeathRecipient() {
            public void binderDied() {
                synchronized (WindowManagerService.this.mWindowMap) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        int i = 0;
                        while (i < WindowManagerService.this.mRotationWatchers.size()) {
                            if (watcherBinder == ((RotationWatcher) WindowManagerService.this.mRotationWatchers.get(i)).mWatcher.asBinder()) {
                                IBinder binder = ((RotationWatcher) WindowManagerService.this.mRotationWatchers.remove(i)).mWatcher.asBinder();
                                if (binder != null) {
                                    binder.unlinkToDeath(this, 0);
                                }
                                i--;
                            }
                            i++;
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        };
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                try {
                    watcher.asBinder().linkToDeath(dr, 0);
                    this.mRotationWatchers.add(new RotationWatcher(watcher, dr, displayId));
                } catch (RemoteException e) {
                }
                defaultDisplayRotation = getDefaultDisplayRotation();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return defaultDisplayRotation;
    }

    public void removeRotationWatcher(IRotationWatcher watcher) {
        IBinder watcherBinder = watcher.asBinder();
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                int i = 0;
                while (i < this.mRotationWatchers.size()) {
                    if (watcherBinder == ((RotationWatcher) this.mRotationWatchers.get(i)).mWatcher.asBinder()) {
                        RotationWatcher removed = (RotationWatcher) this.mRotationWatchers.remove(i);
                        IBinder binder = removed.mWatcher.asBinder();
                        if (binder != null) {
                            binder.unlinkToDeath(removed.mDeathRecipient, 0);
                        }
                        i--;
                    }
                    i++;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
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
                    }
                    resetPriorityAfterLockedSection();
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return 81;
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
        if ("1".equals(SystemProperties.get(SYSTEM_SECURE, "1"))) {
            return "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
        }
        return false;
    }

    public boolean stopViewServer() {
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "stopViewServer") || this.mViewServer == null) {
            return false;
        }
        return this.mViewServer.stop();
    }

    public boolean isViewServerRunning() {
        boolean z = false;
        if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "isViewServerRunning")) {
            return false;
        }
        if (this.mViewServer != null) {
            z = this.mViewServer.isRunning();
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007f A:{SYNTHETIC, Splitter: B:29:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0089 A:{SYNTHETIC, Splitter: B:35:0x0089} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean viewServerListWindows(Socket client) {
        Throwable th;
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        ArrayList<WindowState> windows = new ArrayList();
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.forAllWindows((Consumer) new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass2(windows), false);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            try {
                int count = windows.size();
                for (int i = 0; i < count; i++) {
                    WindowState w = (WindowState) windows.get(i);
                    out2.write(Integer.toHexString(System.identityHashCode(w)));
                    out2.write(32);
                    out2.append(w.mAttrs.getTitle());
                    out2.write(10);
                }
                out2.write("DONE.\n");
                out2.flush();
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e) {
                        result = false;
                    }
                }
                out = out2;
            } catch (Exception e2) {
                out = out2;
                result = false;
                if (out != null) {
                }
                return result;
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            result = false;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    result = false;
                }
            }
            return result;
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004f A:{SYNTHETIC, Splitter: B:21:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0059 A:{SYNTHETIC, Splitter: B:27:0x0059} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean viewServerGetFocusedWindow(Socket client) {
        Throwable th;
        if (isSystemSecure()) {
            return false;
        }
        boolean result = true;
        WindowState focusedWindow = getFocusedWindow();
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), 8192);
            if (focusedWindow != null) {
                try {
                    out2.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                    out2.write(32);
                    out2.append(focusedWindow.mAttrs.getTitle());
                } catch (Exception e) {
                    out = out2;
                    result = false;
                    if (out != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            }
            out2.write(10);
            out2.flush();
            if (out2 != null) {
                try {
                    out2.close();
                } catch (IOException e2) {
                    result = false;
                }
            }
            out = out2;
        } catch (Exception e3) {
            result = false;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    result = false;
                }
            }
            return result;
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f1 A:{SYNTHETIC, Splitter: B:50:0x00f1} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d4  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00de A:{SYNTHETIC, Splitter: B:41:0x00de} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean viewServerWindowCommand(Socket client, String command, String parameters) {
        Exception e;
        Throwable th;
        if (isSystemSecure()) {
            return false;
        }
        boolean success = true;
        Parcel parcel = null;
        Parcel parcel2 = null;
        BufferedWriter out = null;
        try {
            int index = parameters.indexOf(32);
            if (index == -1) {
                index = parameters.length();
            }
            int hashCode = (int) Long.parseLong(parameters.substring(0, index), 16);
            if (index < parameters.length()) {
                parameters = parameters.substring(index + 1);
            } else {
                parameters = "";
            }
            WindowState window = findWindow(hashCode);
            if (window == null) {
                return false;
            }
            parcel = Parcel.obtain();
            parcel.writeInterfaceToken("android.view.IWindow");
            parcel.writeString(command);
            parcel.writeString(parameters);
            parcel.writeInt(1);
            ParcelFileDescriptor.fromSocket(client).writeToParcel(parcel, 0);
            parcel2 = Parcel.obtain();
            window.mClient.asBinder().transact(1, parcel, parcel2, 0);
            parcel2.readException();
            if (!client.isOutputShutdown()) {
                BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                try {
                    out2.write("DONE\n");
                    out2.flush();
                    out = out2;
                } catch (Exception e2) {
                    e = e2;
                    out = out2;
                    try {
                        Slog.w(TAG, "Could not send command " + command + " with parameters " + parameters, e);
                        success = false;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e3) {
                            }
                        }
                        return success;
                    } catch (Throwable th2) {
                        th = th2;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (parcel != null) {
                    }
                    if (parcel2 != null) {
                    }
                    if (out != null) {
                    }
                    throw th;
                }
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            return success;
        } catch (Exception e6) {
            e = e6;
            Slog.w(TAG, "Could not send command " + command + " with parameters " + parameters, e);
            success = false;
            if (parcel != null) {
            }
            if (parcel2 != null) {
            }
            if (out != null) {
            }
            return success;
        }
    }

    public void addWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowChangeListeners.add(listener);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeWindowChangeListener(WindowChangeListener listener) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowChangeListeners.remove(listener);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0024, code:
            resetPriorityAfterLockedSection();
            r0 = r2.length;
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0029, code:
            if (r1 >= r0) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:12:0x002b, code:
            r2[r1].windowsChanged();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:15:0x0039, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyWindowsChanged() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mWindowChangeListeners.isEmpty()) {
                } else {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0024, code:
            resetPriorityAfterLockedSection();
            r0 = r2.length;
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0029, code:
            if (r1 >= r0) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:12:0x002b, code:
            r2[r1].focusChanged();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:15:0x0039, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyFocusChanged() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mWindowChangeListeners.isEmpty()) {
                } else {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private WindowState findWindow(int hashCode) {
        if (hashCode == -1) {
            return getFocusedWindow();
        }
        WindowState window;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                window = this.mRoot.getWindow(new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass5(hashCode));
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return window;
    }

    static /* synthetic */ boolean lambda$-com_android_server_wm_WindowManagerService_215064(int hashCode, WindowState w) {
        return System.identityHashCode(w) == hashCode;
    }

    void sendNewConfiguration(int displayId) {
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
            resetPriorityAfterLockedSection();
        }
    }

    public Configuration computeNewConfiguration(int displayId) {
        Configuration computeNewConfigurationLocked;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                computeNewConfigurationLocked = computeNewConfigurationLocked(displayId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return computeNewConfigurationLocked;
    }

    Configuration computeNewConfigurationLocked(int displayId) {
        if (!this.mDisplayReady) {
            return null;
        }
        Configuration config = new Configuration();
        this.mRoot.getDisplayContent(displayId).computeScreenConfiguration(config);
        return config;
    }

    void notifyHardKeyboardStatusChange() {
        OnHardKeyboardStatusChangeListener listener;
        boolean available;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                listener = this.mHardKeyboardStatusChangeListener;
                available = this.mHardKeyboardAvailable;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (listener != null) {
            listener.onHardKeyboardStatusChange(available);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x001f, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:10:?, code:
            r9.mActivityManager.setFocusedTask(r1.getTask().mTaskId);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean startMovingTask(IWindow window, float startX, float startY) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                WindowState win = windowForClientLocked(null, window, false);
                if (!startPositioningLocked(win, false, false, startX, startY)) {
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return true;
        return false;
    }

    /* JADX WARNING: Missing block: B:18:0x0046, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:19:0x004d, code:
            if (android.util.HwPCUtils.isPcCastModeInServer() == false) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:20:0x004f, code:
            setFocusedDisplay(r13.getDisplayId(), false, "handleTapOutsideTaskXY");
     */
    /* JADX WARNING: Missing block: B:21:0x0059, code:
            if (r9 >= 0) goto L_0x0088;
     */
    /* JADX WARNING: Missing block: B:22:0x005b, code:
            r0 = r13.isDefaultDisplay ^ 1;
     */
    /* JADX WARNING: Missing block: B:23:0x005f, code:
            setPCLauncherFocused(r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0062, code:
            if (r9 < 0) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r12.mActivityManager.setFocusedTask(r9);
     */
    /* JADX WARNING: Missing block: B:39:0x0088, code:
            r0 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handleTapOutsideTask(DisplayContent displayContent, int x, int y) {
        boolean oldPCLauncherFocused;
        if (HwPCUtils.isPcCastModeInServer() && x == -1 && y == -1) {
            setFocusedDisplay(displayContent.getDisplayId(), true, "handleTapOutsideTask-1-1");
            return;
        }
        oldPCLauncherFocused = getPCLauncherFocused();
        setPCLauncherFocused(false);
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                Task task = displayContent.findTaskForResizePoint(x, y);
                int taskId;
                if (task != null) {
                    if (startPositioningLocked(task.getTopVisibleAppMainWindow(), true, task.preserveOrientationOnResize(), (float) x, (float) y)) {
                        taskId = task.mTaskId;
                    }
                } else {
                    taskId = displayContent.taskIdFromPoint(x, y);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return;
        if (oldPCLauncherFocused != getPCLauncherFocused()) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    displayContent.layoutAndAssignWindowLayersIfNeeded();
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private boolean startPositioningLocked(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        if (win == null || win.getAppToken() == null) {
            Slog.w(TAG, "startPositioningLocked: Bad window " + win);
            return false;
        } else if (win.mInputChannel == null) {
            Slog.wtf(TAG, "startPositioningLocked: " + win + " has no input channel, " + " probably being removed");
            return false;
        } else {
            DisplayContent displayContent = win.getDisplayContent();
            if (displayContent == null) {
                Slog.w(TAG, "startPositioningLocked: Invalid display content " + win);
                return false;
            }
            Display display = displayContent.getDisplay();
            this.mTaskPositioner = new TaskPositioner(this);
            this.mTaskPositioner.register(display);
            this.mInputMonitor.updateInputWindowsLw(true);
            WindowState transferFocusFromWin = win;
            if (!(this.mCurrentFocus == null || this.mCurrentFocus == win || this.mCurrentFocus.mAppToken != win.mAppToken)) {
                transferFocusFromWin = this.mCurrentFocus;
            }
            if (this.mInputManager.transferTouchFocus(transferFocusFromWin.mInputChannel, this.mTaskPositioner.mServerChannel)) {
                this.mTaskPositioner.startDrag(win, resize, preserveOrientation, startX, startY);
                return true;
            }
            Slog.e(TAG, "startPositioningLocked: Unable to transfer touch focus");
            this.mTaskPositioner.unregister();
            this.mTaskPositioner = null;
            this.mInputMonitor.updateInputWindowsLw(true);
            return false;
        }
    }

    private void finishPositioning() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (this.mTaskPositioner != null) {
                    this.mTaskPositioner.unregister();
                    this.mTaskPositioner = null;
                    this.mInputMonitor.updateInputWindowsLw(true);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0119 A:{Catch:{ all -> 0x0126 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    IBinder prepareDragSurface(IWindow window, SurfaceSession session, int flags, int width, int height, Surface outSurface) {
        OutOfResourcesException e;
        Throwable th;
        int displayId = 0;
        WindowState wState = windowForClientLocked(null, window, false);
        if (wState != null) {
            displayId = wState.getDisplayId();
        }
        if (displayId == 0 && getLazyMode() != 0) {
            width = (int) (((float) width) * 0.75f);
            height = (int) (((float) height) * 0.75f);
        }
        int callerPid = Binder.getCallingPid();
        int callerUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        IBinder token = null;
        IBinder token2;
        try {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    try {
                        if (this.mDragState == null) {
                            Display display = getDefaultDisplayContentLocked().getDisplay();
                            SurfaceControl surface = new SurfaceControl(session, "drag surface", width, height, -3, 4);
                            surface.setLayerStack(display.getLayerStack());
                            float alpha = 1.0f;
                            if ((flags & 512) == 0) {
                                alpha = DRAG_SHADOW_ALPHA_TRANSPARENT;
                            }
                            surface.setAlpha(alpha);
                            outSurface.copyFrom(surface);
                            IBinder winBinder = window.asBinder();
                            token2 = new Binder();
                            try {
                                this.mDragState = new DragState(this, token2, surface, flags, winBinder);
                                this.mDragState.mPid = callerPid;
                                this.mDragState.mUid = callerUid;
                                this.mDragState.mOriginalAlpha = alpha;
                                token = new Binder();
                                this.mDragState.mToken = token;
                                this.mH.removeMessages(20, winBinder);
                                this.mH.sendMessageDelayed(this.mH.obtainMessage(20, winBinder), 5000);
                                token2 = token;
                            } catch (OutOfResourcesException e2) {
                                e = e2;
                                try {
                                    Slog.e(TAG, "Can't allocate drag surface w=" + width + " h=" + height, e);
                                    if (this.mDragState != null) {
                                        this.mDragState.reset();
                                        this.mDragState = null;
                                    }
                                    resetPriorityAfterLockedSection();
                                    Binder.restoreCallingIdentity(origId);
                                    return token2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            try {
                                resetPriorityAfterLockedSection();
                                Binder.restoreCallingIdentity(origId);
                                return token2;
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        }
                        Slog.w(TAG, "Drag already in progress");
                        token2 = null;
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return token2;
                    } catch (OutOfResourcesException e3) {
                        e = e3;
                        token2 = token;
                        Slog.e(TAG, "Can't allocate drag surface w=" + width + " h=" + height, e);
                        if (this.mDragState != null) {
                        }
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return token2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    token2 = null;
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            token2 = null;
            Binder.restoreCallingIdentity(origId);
            throw th;
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
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    protected WindowState getFocusedWindow() {
        WindowState focusedWindowLocked;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                focusedWindowLocked = getFocusedWindowLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return focusedWindowLocked;
    }

    public String getFocusedAppComponentName() {
        if (this.mFocusedApp != null) {
            return this.mFocusedApp.appComponentName;
        }
        return null;
    }

    private WindowState getFocusedWindowLocked() {
        return this.mCurrentFocus;
    }

    TaskStack getImeFocusStackLocked() {
        if (this.mFocusedApp == null || this.mFocusedApp.getTask() == null) {
            return null;
        }
        return this.mFocusedApp.getTask().mStack;
    }

    public boolean detectSafeMode() {
        boolean z = true;
        if (!this.mInputMonitor.waitForInputDevicesReady(1000)) {
            Slog.w(TAG, "Devices still not ready after waiting 1000 milliseconds before attempting to detect safe mode.");
        }
        if (Global.getInt(this.mContext.getContentResolver(), "safe_boot_disallowed", 0) != 0) {
            return false;
        }
        int menuState = this.mInputManager.getKeyCodeState(-1, -256, 82);
        int sState = this.mInputManager.getKeyCodeState(-1, -256, 47);
        int dpadState = this.mInputManager.getKeyCodeState(-1, 513, 23);
        int trackballState = this.mInputManager.getScanCodeState(-1, 65540, InputManagerService.BTN_MOUSE);
        int volumeDownState = this.mInputManager.getKeyCodeState(-1, -256, 25);
        if (menuState <= 0 && sState <= 0 && dpadState <= 0 && trackballState <= 0 && volumeDownState <= 0) {
            z = false;
        }
        this.mSafeMode = z;
        try {
            if (!(SystemProperties.getInt(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, 0) == 0 && SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0)) {
                this.mSafeMode = true;
                SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, "");
            }
        } catch (IllegalArgumentException e) {
        }
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            this.mSafeMode = false;
        }
        if (this.mSafeMode) {
            Log.i(TAG, "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState + " dpad=" + dpadState + " trackball=" + trackballState + ")");
            SystemProperties.set(ShutdownThread.RO_SAFEMODE_PROPERTY, "1");
        } else {
            Log.i(TAG, "SAFE MODE not enabled");
        }
        this.mPolicy.setSafeMode(this.mSafeMode);
        return this.mSafeMode;
    }

    public void displayReady() {
        for (Display display : this.mDisplays) {
            displayReady(display.getDisplayId());
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                int rotation = SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90;
                if (this.mMaxUiWidth > 0) {
                    displayContent.setMaxUiWidth(this.mMaxUiWidth);
                }
                readForcedDisplayPropertiesLocked(displayContent);
                if (rotation != 0) {
                    displayContent.computeScreenConfiguration(new Configuration());
                }
                this.mDisplayReady = true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        try {
            this.mActivityManager.updateConfiguration(null);
        } catch (RemoteException e) {
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mIsTouchDevice = this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
                configureDisplayPolicyLocked(getDefaultDisplayContentLocked());
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
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
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null) {
                    this.mAnimator.addDisplayLocked(displayId);
                    displayContent.initializeDisplayBaseInfo();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void systemReady() {
        this.mPolicy.systemReady();
        this.mTaskSnapshotController.systemReady();
    }

    void destroyPreservedSurfaceLocked() {
        for (int i = this.mDestroyPreservedSurface.size() - 1; i >= 0; i--) {
            ((WindowState) this.mDestroyPreservedSurface.get(i)).mWinAnimator.destroyPreservedSurfaceLocked();
        }
        this.mDestroyPreservedSurface.clear();
    }

    void stopUsingSavedSurfaceLocked() {
        for (int i = this.mFinishedEarlyAnim.size() - 1; i >= 0; i--) {
            ((AppWindowToken) this.mFinishedEarlyAnim.get(i)).stopUsingSavedSurfaceLocked();
        }
        this.mFinishedEarlyAnim.clear();
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

    /* JADX WARNING: Missing block: B:28:0x0055, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:29:0x0059, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                if (HwPCUtils.isPcCastModeInServer()) {
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(getFocusedDisplayId());
                    if (displayContent != null && displayContent.inputMethodClientHasFocus(client)) {
                    }
                } else if (getDefaultDisplayContentLocked().inputMethodClientHasFocus(client)) {
                    resetPriorityAfterLockedSection();
                    return true;
                }
                if (this.mCurrentFocus == null || this.mCurrentFocus.mSession.mClient == null || this.mCurrentFocus.mSession.mClient.asBinder() != client.asBinder()) {
                } else {
                    resetPriorityAfterLockedSection();
                    return true;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return true;
    }

    public void getInitialDisplaySize(int displayId, Point size) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mInitialDisplayWidth;
                    size.y = displayContent.mInitialDisplayHeight;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void getBaseDisplaySize(int displayId, Point size) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mBaseDisplayWidth;
                    size.y = displayContent.mBaseDisplayHeight;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setForcedDisplaySize(int displayId, int width, int height) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                    if (displayContent != null) {
                        width = Math.min(Math.max(width, 200), displayContent.mInitialDisplayWidth * 2);
                        height = Math.min(Math.max(height, 200), displayContent.mInitialDisplayHeight * 2);
                        Slog.d(TAG, "setForcedDisplaySize and updateResourceConfiguration for HW_ROG_SUPPORT");
                        updateResourceConfiguration(displayId, displayContent.mBaseDisplayDensity, width, height);
                        setForcedDisplaySizeLocked(displayContent, width, height);
                        Global.putString(this.mContext.getContentResolver(), "display_size_forced", width + "," + height);
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void setForcedDisplayScalingMode(int displayId, int mode) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                    if (displayContent != null) {
                        if (mode < 0 || mode > 1) {
                            mode = 0;
                        }
                        setForcedDisplayScalingModeLocked(displayContent, mode);
                        Global.putInt(this.mContext.getContentResolver(), "display_scaling_force", mode);
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private void setForcedDisplayScalingModeLocked(DisplayContent displayContent, int mode) {
        boolean z;
        Slog.i(TAG, "Using display scaling mode: " + (mode == 0 ? Shell.NIGHT_MODE_STR_AUTO : "off"));
        if (mode != 0) {
            z = true;
        } else {
            z = false;
        }
        displayContent.mDisplayScalingDisabled = z;
        reconfigureDisplayLocked(displayContent);
    }

    private void readForcedDisplayPropertiesLocked(DisplayContent displayContent) {
        String sizeStr = Global.getString(this.mContext.getContentResolver(), "display_size_forced");
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
        if (Global.getInt(this.mContext.getContentResolver(), "display_scaling_force", 0) != 0) {
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
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                    if (displayContent != null) {
                        setForcedDisplaySizeLocked(displayContent, displayContent.mInitialDisplayWidth, displayContent.mInitialDisplayHeight);
                        Global.putString(this.mContext.getContentResolver(), "display_size_forced", "");
                    }
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0020, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:12:0x0024, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getInitialDisplayDensity(int displayId) {
        int i;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                } else {
                    i = displayContent.mInitialDisplayDensity;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    /* JADX WARNING: Missing block: B:11:0x0020, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:12:0x0024, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getBaseDisplayDensity(int displayId) {
        int i;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                } else {
                    i = displayContent.mBaseDisplayDensity;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    public void setForcedDisplayDensityForUser(int displayId, int density, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            int targetUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                    if (displayContent != null && this.mCurrentUserId == targetUserId) {
                        Slog.d(TAG, "setForcedDisplayDensityForUser and updateResourceConfiguration for HW_ROG_SUPPORT");
                        updateResourceConfiguration(displayId, density, displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight);
                        setForcedDisplayDensityLocked(displayContent, density);
                    }
                    Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), targetUserId);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void clearForcedDisplayDensityForUser(int displayId, int userId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            int callingUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearForcedDisplayDensityForUser", null);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    boostPriorityForLockedSection();
                    DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                    if (displayContent != null && this.mCurrentUserId == callingUserId) {
                        int curWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
                        if (curWidth > 0) {
                            setForcedDisplayDensityLocked(displayContent, (SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)) * curWidth) / displayContent.mInitialDisplayWidth);
                        } else {
                            setForcedDisplayDensityLocked(displayContent, displayContent.mInitialDisplayDensity);
                        }
                    }
                    Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", "", callingUserId);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private int getForcedDisplayDensityForUserLocked(int userId) {
        String densityStr = Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", userId);
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

    protected void reconfigureDisplayLocked(DisplayContent displayContent) {
        if (this.mDisplayReady) {
            configureDisplayPolicyLocked(displayContent);
            displayContent.setLayoutNeeded();
            int displayId = displayContent.getDisplayId();
            boolean configChanged = updateOrientationFromAppTokensLocked(false, displayId);
            Configuration currentDisplayConfig = displayContent.getConfiguration();
            this.mTempConfiguration.setTo(currentDisplayConfig);
            displayContent.computeScreenConfiguration(this.mTempConfiguration);
            if (configChanged | (currentDisplayConfig.diff(this.mTempConfiguration) != 0 ? 1 : 0)) {
                this.mWaitingForConfig = true;
                startFreezingDisplayLocked(false, 0, 0, displayContent);
                this.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
            }
            this.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    void configureDisplayPolicyLocked(DisplayContent displayContent) {
        this.mPolicy.setInitialDisplaySize(displayContent.getDisplay(), displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight, displayContent.mBaseDisplayDensity);
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        this.mPolicy.setDisplayOverscan(displayContent.getDisplay(), displayInfo.overscanLeft, displayInfo.overscanTop, displayInfo.overscanRight, displayInfo.overscanBottom);
    }

    public void getDisplaysInFocusOrder(SparseIntArray displaysInFocusOrder) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.getDisplaysInFocusOrder(displaysInFocusOrder);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setOverscan(int displayId, int left, int top, int right, int bottom) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mWindowMap) {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null) {
                    setOverscanLocked(displayContent, left, top, right, bottom);
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
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

    final WindowState windowForClientLocked(Session session, IWindow client, boolean throwOnError) {
        return windowForClientLocked(session, client.asBinder(), throwOnError);
    }

    final WindowState windowForClientLocked(Session session, IBinder client, boolean throwOnError) {
        WindowState win = (WindowState) this.mWindowMap.get(client);
        if (win == null) {
            if (throwOnError) {
                throw new IllegalArgumentException("Requested window " + client + " does not exist");
            }
            Slog.w(TAG, "Failed looking up window callers=" + Debug.getCallers(3));
            return null;
        } else if (session == null || win.mSession == session) {
            return win;
        } else {
            if (throwOnError) {
                throw new IllegalArgumentException("Requested window " + client + " is in session " + win.mSession + ", not " + session);
            }
            Slog.w(TAG, "Failed looking up window callers=" + Debug.getCallers(3));
            return null;
        }
    }

    /* renamed from: makeWindowFreezingScreenIfNeededLocked */
    void -com_android_server_wm_AppWindowToken-mthref-0(WindowState w) {
        if (!okToDisplay() && this.mWindowsFreezingScreen != 2) {
            w.mOrientationChanging = true;
            w.mLastFreezeDuration = 0;
            this.mRoot.mOrientationChangeComplete = false;
            if (this.mWindowsFreezingScreen == 0) {
                this.mWindowsFreezingScreen = 1;
                this.mH.removeMessages(11);
                this.mH.sendEmptyMessageDelayed(11, 2000);
            }
        }
    }

    int handleAnimatingStoppedAndTransitionLocked() {
        this.mAppTransition.setIdle();
        for (int i = this.mNoAnimationNotifyOnTransitionFinished.size() - 1; i >= 0; i--) {
            this.mAppTransition.notifyAppTransitionFinishedLocked((IBinder) this.mNoAnimationNotifyOnTransitionFinished.get(i));
        }
        this.mNoAnimationNotifyOnTransitionFinished.clear();
        DisplayContent dc = getDefaultDisplayContentLocked();
        dc.mWallpaperController.hideDeferredWallpapersIfNeeded();
        dc.onAppTransitionDone();
        if (getFocusedDisplayId() == 0) {
            dc.computeImeTarget(true);
        }
        this.mRoot.mWallpaperMayChange = true;
        this.mFocusMayChange = true;
        return 1;
    }

    void checkDrawnWindowsLocked() {
        if (!this.mWaitingForDrawn.isEmpty() && this.mWaitingForDrawnCallback != null) {
            boolean printLog = isPrintAllWindowsDrawnLogs();
            for (int j = this.mWaitingForDrawn.size() - 1; j >= 0; j--) {
                WindowState win = (WindowState) this.mWaitingForDrawn.get(j);
                Flog.i(NativeResponseCode.SERVICE_FOUND, "Waiting for drawn " + win + ": removed=" + win.mRemoved + " visible=" + win.isVisibleLw() + " mHasSurface=" + win.mHasSurface + " drawState=" + win.mWinAnimator.mDrawState);
                if (win.mRemoved || (win.mHasSurface ^ 1) != 0 || (win.mPolicyVisibility ^ 1) != 0) {
                    this.mWaitingForDrawn.remove(win);
                } else if (win.hasDrawnLw()) {
                    this.mWaitingForDrawn.remove(win);
                }
            }
            if (this.mWaitingForDrawn.isEmpty()) {
                this.mH.removeMessages(24);
                this.mH.sendEmptyMessage(33);
            }
        }
    }

    void setHoldScreenLocked(Session newHoldScreen) {
        boolean hold = newHoldScreen != null;
        if (hold && this.mHoldingScreenOn != newHoldScreen) {
            this.mHoldingScreenWakeLock.setWorkSource(new WorkSource(newHoldScreen.mUid));
        }
        this.mHoldingScreenOn = newHoldScreen;
        if (hold == this.mHoldingScreenWakeLock.isHeld()) {
            return;
        }
        if (hold) {
            this.mLastWakeLockHoldingWindow = this.mRoot.mHoldScreenWindow;
            this.mLastWakeLockObscuringWindow = null;
            this.mHoldingScreenWakeLock.acquire();
            this.mPolicy.keepScreenOnStartedLw();
            return;
        }
        this.mLastWakeLockHoldingWindow = null;
        this.mLastWakeLockObscuringWindow = this.mRoot.mObscuringWindow;
        this.mPolicy.keepScreenOnStoppedLw();
        this.mHoldingScreenWakeLock.release();
    }

    void requestTraversal() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mWindowPlacerLocked.requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void scheduleAnimationLocked() {
        this.mAnimator.scheduleAnimation();
    }

    boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        WindowState newFocus = this.mRoot.computeFocusedWindow();
        if (this.mCurrentFocus == newFocus) {
            return false;
        }
        Trace.traceBegin(32, "wmUpdateFocus");
        this.mH.removeMessages(2);
        this.mH.sendEmptyMessage(2);
        DisplayContent displayContent = getDefaultDisplayContentLocked();
        if (HwPCUtils.isPcCastModeInServer()) {
            if (HwPCUtils.enabledInPad()) {
                DisplayContent pcDC = this.mRoot.getDisplayContentOrCreate(getFocusedDisplayId());
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
                    DisplayContent focusedDC = this.mRoot.getDisplayContentOrCreate(getFocusedDisplayId());
                    if (focusedDC != null) {
                        displayContent = focusedDC;
                    }
                }
            }
        }
        boolean imWindowChanged = false;
        if (this.mInputMethodWindow != null) {
            imWindowChanged = this.mInputMethodTarget != displayContent.computeImeTarget(true);
            if (!(mode == 1 || mode == 3)) {
                int prevImeAnimLayer = this.mInputMethodWindow.mWinAnimator.mAnimLayer;
                displayContent.assignWindowLayers(false);
                imWindowChanged |= prevImeAnimLayer != this.mInputMethodWindow.mWinAnimator.mAnimLayer ? 1 : 0;
            }
        }
        if (imWindowChanged) {
            this.mWindowsChanged = true;
            displayContent.setLayoutNeeded();
            newFocus = this.mRoot.computeFocusedWindow();
        }
        WindowState oldFocus = this.mCurrentFocus;
        this.mCurrentFocus = newFocus;
        this.mInputManager.setCurFocusWindow(this.mCurrentFocus);
        this.mLosingFocus.remove(newFocus);
        if (this.mCurrentFocus != null) {
            this.mWinAddedSinceNullFocus.clear();
            this.mWinRemovedSinceNullFocus.clear();
        }
        if (this.mCurrentFocus != null) {
            this.mWinAddedSinceNullFocus.clear();
            this.mWinRemovedSinceNullFocus.clear();
        }
        int focusChanged = this.mPolicy.focusChangedLw(oldFocus, newFocus);
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            if (newFocus != null) {
                try {
                    if (newFocus.getAttrs() != null) {
                        hwAft.notifyFocusChange(newFocus.mSession.mPid, newFocus.getAttrs().getTitle().toString());
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "binder call hwAft throw " + e);
                }
            }
            hwAft.notifyFocusChange(0, null);
        }
        if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
            if (mode == 2) {
                displayContent.performLayout(true, updateInputWindows);
                focusChanged &= -2;
            } else if (mode == 3) {
                displayContent.assignWindowLayers(false);
            }
        }
        if ((focusChanged & 1) != 0) {
            displayContent.setLayoutNeeded();
            if (mode == 2) {
                displayContent.performLayout(true, updateInputWindows);
            }
        }
        if (mode != 1) {
            this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, updateInputWindows);
        }
        Flog.i(304, "oldFocusWindow: " + oldFocus + ", currentFocusWindow: " + this.mCurrentFocus + ", currentFocusApp: " + this.mFocusedApp);
        if (IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("checkType", "FocusWindowErrorScene");
            params.put("looper", BackgroundThread.getHandler().getLooper());
            if (this.mCurrentFocus != null) {
                params.put("focusedWindowName", this.mCurrentFocus.toString());
                params.put("focusedWindowHashCode", Integer.toHexString(System.identityHashCode(this.mCurrentFocus.mAppToken)));
            } else {
                params.put("focusedWindowName", "null");
                params.put("focusedWindowHashCode", "null");
            }
            if (this.mFocusedApp != null) {
                params.put("focusedPackageName", this.mFocusedApp.appPackageName);
                params.put("focusedActivityName", this.mFocusedApp.appComponentName);
                params.put("focusedActivityHashCode", Integer.toHexString(System.identityHashCode(this.mFocusedApp)));
            } else {
                params.put("focusedPackageName", "null");
                params.put("focusedActivityName", "null");
                params.put("focusedActivityHashCode", "null");
            }
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                if (this.mFocusedApp == null || this.mCurrentFocus == null || this.mFocusedApp != this.mCurrentFocus.mAppToken) {
                    HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
                } else {
                    HwServiceFactory.getWinFreezeScreenMonitor().cancelCheckFreezeScreen(params);
                }
            }
        }
        displayContent.adjustForImeIfNeeded();
        displayContent.scheduleToastWindowsTimeoutIfNeededLocked(oldFocus, newFocus);
        Trace.traceEnd(32);
        return true;
    }

    void startFreezingDisplayLocked(boolean inTransaction, int exitAnim, int enterAnim) {
        startFreezingDisplayLocked(inTransaction, exitAnim, enterAnim, getDefaultDisplayContentLocked());
    }

    void startFreezingDisplayLocked(boolean inTransaction, int exitAnim, int enterAnim, DisplayContent displayContent) {
        if (!this.mDisplayFrozen && displayContent.isReady() && (this.mPolicy.isScreenOn() ^ 1) == 0) {
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
            this.mExitAnimId = exitAnim;
            this.mEnterAnimId = enterAnim;
            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mFrozenDisplayId);
            if (screenRotationAnimation != null) {
                screenRotationAnimation.kill();
            }
            boolean isSecure = displayContent.hasSecureWindowOnScreen();
            displayContent.updateDisplayInfo();
            boolean enabledInPad = HwPCUtils.isPcCastModeInServer() ? !HwPCUtils.isValidExtDisplayId(displayContent.getDisplayId()) ? HwPCUtils.enabledInPad() : true : false;
            if (enabledInPad) {
                this.mH.removeMessages(103);
                this.mH.sendEmptyMessageDelayed(103, 3000);
            } else {
                IHwScreenRotationAnimation hwSRA = HwServiceFactory.getHwScreenRotationAnimation();
                if (hwSRA != null) {
                    screenRotationAnimation = hwSRA.create(this.mContext, displayContent, this.mFxSession, inTransaction, this.mPolicy.isDefaultOrientationForced(), isSecure, this);
                } else {
                    screenRotationAnimation = new ScreenRotationAnimation(this.mContext, displayContent, this.mFxSession, inTransaction, this.mPolicy.isDefaultOrientationForced(), isSecure, this);
                }
                this.mAnimator.setScreenRotationAnimationLocked(this.mFrozenDisplayId, screenRotationAnimation);
            }
        }
    }

    void stopFreezingDisplayLocked() {
        if (this.mDisplayFrozen) {
            int bReturn;
            if (this.mWaitingForConfig || this.mAppsFreezingScreen > 0 || this.mWindowsFreezingScreen == 1) {
                bReturn = 1;
            } else {
                bReturn = this.mClientFreezingScreen;
            }
            int size = this.mOpeningApps.size();
            if (bReturn != 0 || size > 0) {
                Slog.d(TAG, "stopFreezingDisplayLocked: Returning mWaitingForConfig=" + this.mWaitingForConfig + ", mAppsFreezingScreen=" + this.mAppsFreezingScreen + ", mWindowsFreezingScreen=" + this.mWindowsFreezingScreen + ", mClientFreezingScreen=" + this.mClientFreezingScreen + ", mOpeningApps.size()=" + this.mOpeningApps.size());
                if ((bReturn ^ 1) != 0 && size > 0) {
                    printFreezingDisplayLogs();
                }
                return;
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
            this.mLastDisplayFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
            StringBuilder stringBuilder = new StringBuilder(128);
            stringBuilder.append("Screen frozen for ");
            TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, stringBuilder);
            if (this.mLastFinishedFreezeSource != null) {
                stringBuilder.append(" due to ");
                stringBuilder.append(this.mLastFinishedFreezeSource);
            }
            Slog.i(TAG, stringBuilder.toString());
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
                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                if (!this.mPolicy.validateRotationAnimationLw(this.mExitAnimId, this.mEnterAnimId, displayContent.isDimming())) {
                    this.mEnterAnimId = 0;
                    this.mExitAnimId = 0;
                }
                if (screenRotationAnimation.dismiss(this.mFxSession, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight, this.mExitAnimId, this.mEnterAnimId)) {
                    scheduleAnimationLocked();
                } else {
                    screenRotationAnimation.kill();
                    this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                    updateRotation = true;
                }
            }
            this.mInputMonitor.thawInputDispatchingLw();
            boolean configChanged = updateOrientationFromAppTokensLocked(false, displayId);
            this.mH.removeMessages(15);
            this.mH.sendEmptyMessageDelayed(15, 2000);
            this.mScreenFrozenLock.release();
            if (updateRotation) {
                configChanged |= displayContent.updateRotationUnchecked(false);
            }
            if (configChanged) {
                this.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
            }
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

    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0064 A:{SYNTHETIC, Splitter: B:37:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0053 A:{SYNTHETIC, Splitter: B:28:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0075 A:{SYNTHETIC, Splitter: B:46:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0064 A:{SYNTHETIC, Splitter: B:37:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0053 A:{SYNTHETIC, Splitter: B:28:0x0053} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0075 A:{SYNTHETIC, Splitter: B:46:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x007b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void createWatermarkInTransaction() {
        Throwable th;
        if (this.mWatermark == null) {
            FileInputStream in = null;
            DataInputStream ind = null;
            try {
                FileInputStream in2 = new FileInputStream(new File("/system/etc/setup.conf"));
                try {
                    DataInputStream ind2 = new DataInputStream(in2);
                    try {
                        String line = ind2.readLine();
                        if (line != null) {
                            String[] toks = line.split("%");
                            if (toks != null && toks.length > 0) {
                                DisplayContent displayContent = getDefaultDisplayContentLocked();
                                this.mWatermark = new Watermark(displayContent.getDisplay(), displayContent.mRealDisplayMetrics, this.mFxSession, toks);
                            }
                        }
                        if (ind2 != null) {
                            try {
                                ind2.close();
                            } catch (IOException e) {
                            }
                        } else if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException e2) {
                            }
                        }
                        in = in2;
                    } catch (FileNotFoundException e3) {
                        ind = ind2;
                        in = in2;
                        if (ind != null) {
                        }
                    } catch (IOException e4) {
                        ind = ind2;
                        in = in2;
                        if (ind != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        ind = ind2;
                        in = in2;
                        if (ind != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e5) {
                    in = in2;
                    if (ind != null) {
                    }
                } catch (IOException e6) {
                    in = in2;
                    if (ind != null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (ind != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e8) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e9) {
                    }
                }
            } catch (IOException e10) {
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e11) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e12) {
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                if (ind != null) {
                    try {
                        ind.close();
                    } catch (IOException e13) {
                    }
                } else if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e14) {
                    }
                }
                throw th;
            }
        }
    }

    public void setRecentsVisibility(boolean visible) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.setRecentsVisibilityLw(visible);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setPipVisibility(boolean visible) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mPolicy.setPipVisibilityLw(visible);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void statusBarVisibilityChanged(int visibility) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                int diff = visibility ^ this.mLastStatusBarVisibility;
                if (diff != 0) {
                    Flog.i(303, "statusBarVisibilityChanged,vis=" + Integer.toHexString(visibility) + ",diff=" + Integer.toHexString(diff));
                }
                this.mLastStatusBarVisibility = visibility;
                visibility = this.mPolicy.adjustSystemUiVisibilityLw(visibility);
                if ((diff & 201326592) == 201326592 || (diff & 134217728) == 134217728) {
                    DisplayContent displayContent = getDefaultDisplayContentLocked();
                    displayContent.pendingLayoutChanges |= 1;
                    this.mRoot.performSurfacePlacement(false);
                }
                updateStatusBarVisibilityLocked(visibility);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean updateStatusBarVisibilityLocked(int visibility) {
        if (this.mLastDispatchedSystemUiVisibility == visibility) {
            return false;
        }
        int globalDiff = ((this.mLastDispatchedSystemUiVisibility ^ visibility) & 7) & (~visibility);
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public InputConsumer createInputConsumer(Looper looper, String name, Factory inputEventReceiverFactory) {
        InputConsumer createInputConsumer;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                createInputConsumer = this.mInputMonitor.createInputConsumer(looper, name, inputEventReceiverFactory);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return createInputConsumer;
    }

    public void createInputConsumer(String name, InputChannel inputChannel) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mInputMonitor.createInputConsumer(name, inputChannel);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean destroyInputConsumer(String name) {
        boolean destroyInputConsumer;
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                destroyInputConsumer = this.mInputMonitor.destroyInputConsumer(name);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return destroyInputConsumer;
    }

    public boolean hasNavigationBar() {
        return this.mPolicy.hasNavigationBar();
    }

    public void lockNow(Bundle options) {
        this.mPolicy.lockNow(options);
    }

    public void showRecentApps(boolean fromHome) {
        this.mPolicy.showRecentApps(fromHome);
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
                    } else {
                        WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                        if (surfaceController == null) {
                            resetPriorityAfterLockedSection();
                            return false;
                        }
                        boolean clearWindowContentFrameStats = surfaceController.clearWindowContentFrameStats();
                        resetPriorityAfterLockedSection();
                        return clearWindowContentFrameStats;
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires FRAME_STATS permission");
        }
        return false;
    }

    public WindowContentFrameStats getWindowContentFrameStats(IBinder token) {
        if (checkCallingPermission("android.permission.FRAME_STATS", "getWindowContentFrameStats()")) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                    } else {
                        WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                        if (surfaceController == null) {
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                        if (this.mTempWindowRenderStats == null) {
                            this.mTempWindowRenderStats = new WindowContentFrameStats();
                        }
                        WindowContentFrameStats stats = this.mTempWindowRenderStats;
                        if (surfaceController.getWindowContentFrameStats(stats)) {
                            resetPriorityAfterLockedSection();
                            return stats;
                        }
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new SecurityException("Requires FRAME_STATS permission");
        }
        return null;
    }

    public void notifyAppRelaunching(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.startRelaunching();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppRelaunchingFinished(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.finishRelaunching();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppRelaunchesCleared(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.clearRelaunching();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyAppResumedFinished(IBinder token) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindow = this.mRoot.getAppWindowToken(token);
                if (appWindow != null) {
                    this.mUnknownAppVisibilityController.notifyAppResumedFinished(appWindow);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyTaskRemovedFromRecents(int taskId, int userId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mTaskSnapshotController.notifyTaskRemovedFromRecents(taskId, userId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
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
        if (!this.mOpeningApps.isEmpty() || (this.mClosingApps.isEmpty() ^ 1) != 0) {
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
            Session s = (Session) this.mSessions.valueAt(i);
            pw.print("  Session ");
            pw.print(s);
            pw.println(':');
            s.dump(pw, "    ");
        }
    }

    private void dumpWindowsLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        pw.println("WINDOW MANAGER WINDOWS (dumpsys window windows)");
        dumpWindowsNoHeaderLocked(pw, dumpAll, windows);
    }

    private void dumpWindowsNoHeaderLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        int i;
        WindowState w;
        this.mRoot.dumpWindowsNoHeader(pw, dumpAll, windows);
        if (!this.mHidingNonSystemOverlayWindows.isEmpty()) {
            pw.println();
            pw.println("  Hiding System Alert Windows:");
            for (i = this.mHidingNonSystemOverlayWindows.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mHidingNonSystemOverlayWindows.get(i);
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
            for (i = this.mPendingRemove.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mPendingRemove.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Remove #");
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
        }
        if (this.mForceRemoves != null && this.mForceRemoves.size() > 0) {
            pw.println();
            pw.println("  Windows force removing:");
            for (i = this.mForceRemoves.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mForceRemoves.get(i);
                pw.print("  Removing #");
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
        if (this.mDestroySurface.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to destroy their surface:");
            for (i = this.mDestroySurface.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mDestroySurface.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Destroy #");
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
        }
        if (this.mLosingFocus.size() > 0) {
            pw.println();
            pw.println("  Windows losing focus:");
            for (i = this.mLosingFocus.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mLosingFocus.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Losing #");
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
        }
        if (this.mResizingWindows.size() > 0) {
            pw.println();
            pw.println("  Windows waiting to resize:");
            for (i = this.mResizingWindows.size() - 1; i >= 0; i--) {
                w = (WindowState) this.mResizingWindows.get(i);
                if (windows == null || windows.contains(w)) {
                    pw.print("  Resizing #");
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
        }
        if (this.mWaitingForDrawn.size() > 0) {
            pw.println();
            pw.println("  Clients waiting for these windows to be drawn:");
            for (i = this.mWaitingForDrawn.size() - 1; i >= 0; i--) {
                WindowState win = (WindowState) this.mWaitingForDrawn.get(i);
                pw.print("  Waiting #");
                pw.print(i);
                pw.print(' ');
                pw.print(win);
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
        pw.print(this.mInTouchMode);
        pw.print(" mLayoutSeq=");
        pw.println(this.mLayoutSeq);
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
        }
    }

    private boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<WindowState> windows = new ArrayList();
        if ("apps".equals(name) || "visible".equals(name) || "visible-apps".equals(name)) {
            boolean appsOnly = name.contains("apps");
            boolean visibleOnly = name.contains("visible");
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    if (appsOnly) {
                        this.mRoot.dumpDisplayContents(pw);
                    }
                    this.mRoot.forAllWindows((Consumer) new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass7(visibleOnly, appsOnly, windows), true);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    this.mRoot.getWindowsByName(windows, name);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        if (windows.size() <= 0) {
            return false;
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                dumpWindowsLocked(pw, dumpAll, windows);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return true;
    }

    static /* synthetic */ void lambda$-com_android_server_wm_WindowManagerService_330593(boolean visibleOnly, boolean appsOnly, ArrayList windows, WindowState w) {
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

    void saveANRStateLocked(AppWindowToken appWindowToken, WindowState windowState, String reason) {
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
        this.mH.sendEmptyMessageDelayed(38, 7200000);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        String str = null;
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
                    pw.println("  cmd may also be a NAME to dump windows.  NAME may");
                    pw.println("    be a partial substring in a window name, a");
                    pw.println("    Window hex object identifier, or");
                    pw.println("    \"all\" for all windows, or");
                    pw.println("    \"visible\" for the visible windows.");
                    pw.println("    \"visible-apps\" for the visible app windows.");
                    pw.println("  -a: include all available server state.");
                    return;
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
            if (opti < args.length) {
                String cmd = args[opti];
                opti++;
                if ("lastanr".equals(cmd) || "l".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpLastANRLocked(pw);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("policy".equals(cmd) || "p".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpPolicyLocked(pw, args, true);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("animator".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpAnimatorLocked(pw, args, true);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("sessions".equals(cmd) || "s".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpSessionsLocked(pw, true);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("surfaces".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            SurfaceTrace.dumpAllSurfaces(pw, null);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("displays".equals(cmd) || "d".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            this.mRoot.dumpDisplayContents(pw);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("tokens".equals(cmd) || "t".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpTokensLocked(pw, true);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("windows".equals(cmd) || "w".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpWindowsLocked(pw, true, null);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("all".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            dumpWindowsLocked(pw, true, null);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else if ("containers".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        try {
                            boostPriorityForLockedSection();
                            StringBuilder output = new StringBuilder();
                            this.mRoot.dumpChildrenNames(output, " ");
                            pw.println(output.toString());
                            pw.println(" ");
                            this.mRoot.forAllWindows((Consumer) new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass1(pw), true);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                } else {
                    if (!dumpWindows(pw, cmd, args, opti, dumpAll)) {
                        pw.println("Bad window command, or no windows match: " + cmd);
                        pw.println("Use -h for help.");
                    }
                    return;
                }
            }
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
                        str = "-------------------------------------------------------------------------------";
                    }
                    SurfaceTrace.dumpAllSurfaces(pw, str);
                    pw.println();
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
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public void monitor() {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void createDisplayContentLocked(Display display) {
        if (display == null) {
            throw new IllegalArgumentException("getDisplayContent: display must not be null");
        }
        this.mRoot.getDisplayContentOrCreate(display.getDisplayId());
    }

    DisplayContent getDefaultDisplayContentLocked() {
        return this.mRoot.getDisplayContentOrCreate(0);
    }

    public void onDisplayAdded(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                Display display = this.mDisplayManager.getDisplay(displayId);
                if (display != null) {
                    createDisplayContentLocked(display);
                    displayReady(displayId);
                }
                this.mWindowPlacerLocked.requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void onDisplayRemoved(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null) {
                    displayContent.removeIfPossible();
                }
                this.mAnimator.removeDisplayLocked(displayId);
                this.mWindowPlacerLocked.requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void onDisplayChanged(int displayId) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                DisplayContent displayContent = this.mRoot.getDisplayContentOrCreate(displayId);
                if (displayContent != null) {
                    displayContent.updateDisplayInfo();
                }
                this.mWindowPlacerLocked.requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public Object getWindowManagerLock() {
        return this.mWindowMap;
    }

    public void setWillReplaceWindow(IBinder token, boolean animate) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null || (appWindowToken.hasContentToDisplay() ^ 1) != 0) {
                    Slog.w(TAG, "Attempted to set replacing window on non-existing app token " + token);
                } else {
                    appWindowToken.setWillReplaceWindows(animate);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void setWillReplaceWindows(IBinder token, boolean childrenOnly) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null || (appWindowToken.hasContentToDisplay() ^ 1) != 0) {
                    Slog.w(TAG, "Attempted to set replacing window on non-existing app token " + token);
                } else {
                    if (childrenOnly) {
                        appWindowToken.setWillReplaceChildWindows();
                    } else {
                        appWindowToken.setWillReplaceWindows(false);
                    }
                    scheduleClearWillReplaceWindows(token, true);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0033, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:13:0x0036, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void scheduleClearWillReplaceWindows(IBinder token, boolean replacing) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                AppWindowToken appWindowToken = this.mRoot.getAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w(TAG, "Attempted to reset replacing window on non-existing app token " + token);
                } else if (replacing) {
                    scheduleWindowReplacementTimeouts(appWindowToken);
                } else {
                    appWindowToken.clearWillReplaceWindows();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void scheduleWindowReplacementTimeouts(AppWindowToken appWindowToken) {
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
                TaskStack dockedStack = getDefaultDisplayContentLocked().getDockedStackIgnoringVisibility();
                dockSide = dockedStack == null ? -1 : dockedStack.getDockSide();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return dockSide;
    }

    public void setDockedStackResizing(boolean resizing) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setResizing(resizing);
                requestTraversal();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDockedStackDividerTouchRegion(Rect touchRegion) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setTouchRegion(touchRegion);
                setFocusTaskRegionLocked(null);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                getDefaultDisplayContentLocked().getDockedDividerController().setResizeDimLayer(visible, targetStackId, alpha);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setForceResizableTasks(boolean forceResizableTasks) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mForceResizableTasks = forceResizableTasks;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setSupportsPictureInPicture(boolean supportsPictureInPicture) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mSupportsPictureInPicture = supportsPictureInPicture;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    static int dipToPixel(int dip, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(1, (float) dip, displayMetrics);
    }

    public void registerDockedStackListener(IDockedStackListener listener) {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerDockedStackListener()") || (checkCallingPermission("huawei.android.permission.MULTIWINDOW_SDK", "registerDockedStackListener()") ^ 1) == 0) {
            synchronized (this.mWindowMap) {
                try {
                    boostPriorityForLockedSection();
                    getDefaultDisplayContentLocked().mDividerControllerLocked.registerDockedStackListener(listener);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public void registerPinnedStackListener(int displayId, IPinnedStackListener listener) {
        if (!checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerPinnedStackListener()") || !this.mSupportsPictureInPicture) {
            return;
        }
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "ignore pinned stack listener in pad pc mode");
            return;
        }
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                this.mRoot.getDisplayContent(displayId).getPinnedStackController().registerPinnedStackListener(listener);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
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
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void getStableInsetsLocked(int displayId, Rect outInsets) {
        outInsets.setEmpty();
        DisplayContent dc = this.mRoot.getDisplayContent(displayId);
        if (dc != null) {
            DisplayInfo di = dc.getDisplayInfo();
            this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
        }
    }

    void intersectDisplayInsetBounds(Rect display, Rect insets, Rect inOutBounds) {
        this.mTmpRect3.set(display);
        this.mTmpRect3.inset(insets);
        inOutBounds.intersect(this.mTmpRect3);
    }

    /* JADX WARNING: Missing block: B:10:0x001a, code:
            r7 = r10.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:11:0x001c, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            boostPriorityForLockedSection();
     */
    /* JADX WARNING: Missing block: B:14:0x0022, code:
            if (r10.mDragState == null) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:15:0x0024, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:16:0x0025, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:17:0x0028, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r0 = windowForClientLocked(null, r11, false);
     */
    /* JADX WARNING: Missing block: B:24:0x0032, code:
            if (r0 != null) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:25:0x0034, code:
            android.util.Slog.w(TAG, "Bad requesting window " + r11);
     */
    /* JADX WARNING: Missing block: B:26:0x004e, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:27:0x004f, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:28:0x0052, code:
            return;
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r1 = r0.getDisplayContent();
     */
    /* JADX WARNING: Missing block: B:31:0x0057, code:
            if (r1 != null) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:32:0x0059, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:33:0x005a, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:34:0x005d, code:
            return;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r5 = r1.getTouchableWinAtPointLocked(r3, r4);
     */
    /* JADX WARNING: Missing block: B:37:0x0062, code:
            if (r5 == r0) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:38:0x0064, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:39:0x0065, code:
            resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:40:0x0068, code:
            return;
     */
    /* JADX WARNING: Missing block: B:42:?, code:
            r5.mClient.updatePointerIcon(r5.translateToWindowX(r3), r5.translateToWindowY(r4));
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            android.util.Slog.w(TAG, "unable to update pointer icon");
     */
    /* JADX WARNING: Missing block: B:51:0x0088, code:
            resetPriorityAfterLockedSection();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void updatePointerIcon(IWindow client) {
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

    void restorePointerIconLocked(DisplayContent displayContent, float latestX, float latestY) {
        this.mMousePositionTracker.updatePosition(latestX, latestY);
        WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(latestX, latestY);
        if (windowUnderPointer != null) {
            try {
                windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(latestX), windowUnderPointer.translateToWindowY(latestY));
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "unable to restore pointer icon");
                return;
            }
        }
        InputManager.getInstance().setPointerIconType(1000);
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutKeyReceiver) throws RemoteException {
        if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerShortcutKey")) {
            this.mPolicy.registerShortcutKey(shortcutCode, shortcutKeyReceiver);
            return;
        }
        throw new SecurityException("Requires REGISTER_WINDOW_MANAGER_LISTENERS permission");
    }

    void markForSeamlessRotation(WindowState w, boolean seamlesslyRotated) {
        if (seamlesslyRotated != w.mSeamlesslyRotated) {
            w.mSeamlesslyRotated = seamlesslyRotated;
            if (seamlesslyRotated) {
                this.mSeamlessRotationCount++;
            } else {
                this.mSeamlessRotationCount--;
            }
            if (this.mSeamlessRotationCount == 0) {
                DisplayContent displayContent = w.getDisplayContent();
                if (displayContent.updateRotationUnchecked(false)) {
                    this.mH.obtainMessage(18, Integer.valueOf(displayContent.getDisplayId())).sendToTarget();
                }
            }
        }
    }

    void registerAppFreezeListener(AppFreezeListener listener) {
        if (!this.mAppFreezeListeners.contains(listener)) {
            this.mAppFreezeListeners.add(listener);
        }
    }

    void unregisterAppFreezeListener(AppFreezeListener listener) {
        this.mAppFreezeListeners.remove(listener);
    }

    public final void performhwLayoutAndPlaceSurfacesLocked() {
        this.mWindowPlacerLocked.performSurfacePlacement();
    }

    protected boolean canBeFloatImeTarget(WindowState w) {
        int fl = w.mAttrs.flags & 131080;
        if (fl == 0 || fl == 131080 || w.mAttrs.type == 3) {
            return w.isVisibleOrAdding();
        }
        return false;
    }

    private void printFreezingDisplayLogs() {
        int appsCount = this.mOpeningApps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = (AppWindowToken) this.mOpeningApps.valueAt(i);
            StringBuilder builder = new StringBuilder();
            builder.append("opening app wtoken = ");
            builder.append(wtoken.toString());
            builder.append(", allDrawn= ");
            builder.append(wtoken.allDrawn);
            builder.append(", startingDisplayed =  ");
            builder.append(wtoken.startingDisplayed);
            builder.append(", startingMoved =  ");
            builder.append(wtoken.startingMoved);
            builder.append(", isRelaunching =  ");
            builder.append(wtoken.isRelaunching());
            Slog.d(TAG, "printFreezingDisplayLogs" + builder.toString());
        }
    }

    private boolean isPrintAllWindowsDrawnLogs() {
        if (SystemClock.elapsedRealtime() - this.mWaitAllWindowDrawStartTime > 1000) {
            return true;
        }
        return false;
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
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void disableNonVrUi(boolean disable) {
        synchronized (this.mWindowMap) {
            try {
                boostPriorityForLockedSection();
                boolean showAlertWindowNotifications = disable ^ 1;
                if (showAlertWindowNotifications == this.mShowAlertWindowNotifications) {
                } else {
                    this.mShowAlertWindowNotifications = showAlertWindowNotifications;
                    for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                        ((Session) this.mSessions.valueAt(i)).setShowingAlertWindowNotificationAllowed(this.mShowAlertWindowNotifications);
                    }
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void updateNonSystemOverlayWindowsVisibilityIfNeeded(WindowState win, boolean surfaceShown) {
        if (win.hideNonSystemOverlayWindowsWhenVisible()) {
            boolean systemAlertWindowsHidden = this.mHidingNonSystemOverlayWindows.isEmpty() ^ 1;
            if (!surfaceShown) {
                this.mHidingNonSystemOverlayWindows.remove(win);
            } else if (!this.mHidingNonSystemOverlayWindows.contains(win)) {
                this.mHidingNonSystemOverlayWindows.add(win);
            }
            boolean hideSystemAlertWindows = this.mHidingNonSystemOverlayWindows.isEmpty() ^ 1;
            if (systemAlertWindowsHidden != hideSystemAlertWindows) {
                this.mRoot.forAllWindows((Consumer) new com.android.server.wm.-$Lambda$eBBEuGZ8VbEXJy0r5EYYbvnl-8w.AnonymousClass6(hideSystemAlertWindows), false);
            }
        }
    }

    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public WindowHashMap getWindowMap() {
        return this.mWindowMap;
    }

    public TaskSnapshotController getTaskSnapshotController() {
        return this.mTaskSnapshotController;
    }

    public void layoutWindowForPadPCMode(WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        if (win instanceof WindowState) {
            this.mHwWMSEx.layoutWindowForPadPCMode((WindowState) win, this.mInputMethodTarget, this.mInputMethodWindow, pf, df, cf, vf, contentBottom);
        }
    }
}
