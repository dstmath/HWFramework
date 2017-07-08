package com.android.server.wm;

import android.animation.ValueAnimator;
import android.annotation.IntDef;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.IActivityManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.input.InputManager;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.rms.HwSysResource;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.util.Xml;
import android.view.AppTransitionAnimationSpec;
import android.view.Choreographer;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IApplicationToken;
import android.view.IDockedStackListener;
import android.view.IInputFilter;
import android.view.IOnKeyguardExitResult;
import android.view.IRotationWatcher;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.View;
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
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.R;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IShortcutService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.WindowManagerPolicyThread;
import com.android.server.AbsLocationManagerService;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.DisplayThread;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwWindowManagerService;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.input.InputManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.ShutdownThread;
import com.android.server.usb.UsbAudioDevice;
import com.hisi.perfhub.PerfHub;
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
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WindowManagerService extends AbsWindowManagerService implements Monitor, WindowManagerFuncs {
    private static final boolean ALWAYS_KEEP_CURRENT = true;
    private static final int ANIMATION_DURATION_SCALE = 2;
    private static final int BOOT_ANIMATION_POLL_INTERVAL = 200;
    private static final String BOOT_ANIMATION_SERVICE = "bootanim";
    public static final int COMPAT_MODE_DISABLED = 0;
    public static final int COMPAT_MODE_ENABLED = 1;
    public static final int COMPAT_MODE_MATCH_PARENT = -3;
    static final boolean CUSTOM_SCREEN_ROTATION = true;
    static final long DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS = 5000000000L;
    private static final String DENSITY_OVERRIDE = "ro.config.density_override";
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    static final boolean HISI_PERF_OPT = false;
    static final boolean HWFLOW = true;
    private static final int INPUT_DEVICES_READY_FOR_SAFE_MODE_DETECTION_TIMEOUT_MILLIS = 1000;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    static final int LAYER_OFFSET_DIM = 1;
    static final int LAYER_OFFSET_THUMBNAIL = 4;
    static final int LAYOUT_REPEAT_THRESHOLD = 4;
    static final int MAX_ANIMATION_DURATION = 10000;
    private static final int MAX_SCREENSHOT_RETRIES = 3;
    static final boolean PROFILE_ORIENTATION = false;
    private static final String PROPERTY_BUILD_DATE_UTC = "ro.build.date.utc";
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    static final boolean SCREENSHOT_FORCE_565 = true;
    private static final String SIZE_OVERRIDE = "ro.config.size_override";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    private static final String TAG = null;
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
    AccessibilityController mAccessibilityController;
    final IActivityManager mActivityManager;
    private final AppTransitionListener mActivityManagerAppTransitionNotifier;
    private HwSysResource mActivityResource;
    final boolean mAllowAnimationsInLowPowerMode;
    final boolean mAllowBootMessages;
    boolean mAllowTheaterModeWakeFromLayout;
    boolean mAltOrientation;
    final ActivityManagerInternal mAmInternal;
    boolean mAnimateWallpaperWithTarget;
    boolean mAnimationScheduled;
    boolean mAnimationsDisabled;
    final WindowAnimator mAnimator;
    float mAnimatorDurationScaleSetting;
    final AppOpsManager mAppOps;
    final AppTransition mAppTransition;
    int mAppsFreezingScreen;
    boolean mBootAnimationStopped;
    private final BoundsAnimationController mBoundsAnimationController;
    final BroadcastReceiver mBroadcastReceiver;
    private final ArrayList<Integer> mChangedStackList;
    final Choreographer mChoreographer;
    CircularDisplayMask mCircularDisplayMask;
    boolean mClientFreezingScreen;
    final ArraySet<AppWindowToken> mClosingApps;
    final DisplayMetrics mCompatDisplayMetrics;
    float mCompatibleScreenScale;
    protected final Context mContext;
    Configuration mCurConfiguration;
    WindowState mCurrentFocus;
    int[] mCurrentProfileIds;
    int mCurrentUserId;
    private HwCustWindowManagerService mCust;
    int mDeferredRotationPauseCount;
    final ArrayList<WindowState> mDestroyPreservedSurface;
    final ArrayList<WindowState> mDestroySurface;
    SparseArray<DisplayContent> mDisplayContents;
    boolean mDisplayEnabled;
    long mDisplayFreezeTime;
    boolean mDisplayFrozen;
    final DisplayManager mDisplayManager;
    final DisplayManagerInternal mDisplayManagerInternal;
    final DisplayMetrics mDisplayMetrics;
    boolean mDisplayReady;
    final DisplaySettings mDisplaySettings;
    final Display[] mDisplays;
    Rect mDockedStackCreateBounds;
    int mDockedStackCreateMode;
    DragState mDragState;
    final long mDrawLockTimeoutMillis;
    EmulatorDisplayOverlay mEmulatorDisplayOverlay;
    int mEnterAnimId;
    private boolean mEventDispatchingEnabled;
    int mExitAnimId;
    final ArrayList<AppWindowToken> mFinishedEarlyAnim;
    final ArrayList<AppWindowToken> mFinishedStarting;
    private boolean mFirstStartHome;
    boolean mFocusMayChange;
    AppWindowToken mFocusedApp;
    float mForceCompatibleScreenScale;
    boolean mForceDisplayEnabled;
    final ArrayList<WindowState> mForceRemoves;
    boolean mForceResizableTasks;
    int mForcedAppOrientation;
    final SurfaceSession mFxSession;
    final H mH;
    boolean mHardKeyboardAvailable;
    OnHardKeyboardStatusChangeListener mHardKeyboardStatusChangeListener;
    final boolean mHasPermanentDpad;
    final boolean mHaveInputMethods;
    Session mHoldingScreenOn;
    WakeLock mHoldingScreenWakeLock;
    boolean mInTouchMode;
    InputConsumerImpl mInputConsumer;
    final InputManagerService mInputManager;
    final ArrayList<WindowState> mInputMethodDialogs;
    IInputMethodManager mInputMethodManager;
    WindowState mInputMethodTarget;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow;
    final InputMonitor mInputMonitor;
    boolean mIsPerfBoost;
    boolean mIsTouchDevice;
    boolean mKeyguardAttachWallpaper;
    private final KeyguardDisableHandler mKeyguardDisableHandler;
    Runnable mKeyguardDismissDoneCallback;
    private boolean mKeyguardWaitingForActivityDrawn;
    WindowState mKeyguardWin;
    String mLastANRState;
    int mLastDispatchedSystemUiVisibility;
    int mLastDisplayFreezeDuration;
    Object mLastFinishedFreezeSource;
    WindowState mLastFocus;
    int mLastKeyguardForcedOrientation;
    int mLastStatusBarVisibility;
    WindowState mLastWakeLockHoldingWindow;
    WindowState mLastWakeLockObscuringWindow;
    int mLastWindowForcedOrientation;
    final WindowLayersController mLayersController;
    int mLayoutSeq;
    public int mLazyModeOn;
    final boolean mLimitedAlphaCompositing;
    ArrayList<WindowState> mLosingFocus;
    private MousePositionTracker mMousePositionTracker;
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished;
    final boolean mOnlyCore;
    final ArraySet<AppWindowToken> mOpeningApps;
    private final HashMap<String, Boolean> mPackages;
    final ArrayList<WindowState> mPendingRemove;
    WindowState[] mPendingRemoveTmp;
    private PerfHub mPerfHub;
    private final PointerEventDispatcher mPointerEventDispatcher;
    final WindowManagerPolicy mPolicy;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    final DisplayMetrics mRealDisplayMetrics;
    private final boolean mRebuildAppWinsOnBoot;
    WindowState[] mRebuildTmp;
    private final DisplayContentList mReconfigureOnConfigurationChanged;
    final ArrayList<AppWindowToken> mReplacingWindowTimeouts;
    final ArrayList<WindowState> mResizingWindows;
    int mRotation;
    ArrayList<RotationWatcher> mRotationWatchers;
    boolean mSafeMode;
    SparseArray<Boolean> mScreenCaptureDisabled;
    private final WakeLock mScreenFrozenLock;
    final Rect mScreenRect;
    final ArraySet<Session> mSessions;
    SettingsObserver mSettingsObserver;
    boolean mShowingBootMessages;
    boolean mSkipAppTransitionAnimation;
    SparseArray<TaskStack> mStackIdToStack;
    StrictModeFlash mStrictModeFlash;
    boolean mSystemBooted;
    int mSystemDecorLayer;
    SparseArray<Task> mTaskIdToTask;
    TaskPositioner mTaskPositioner;
    final Configuration mTempConfiguration;
    private WindowContentFrameStats mTempWindowRenderStats;
    final DisplayMetrics mTmpDisplayMetrics;
    final float[] mTmpFloats;
    final Rect mTmpRect;
    final Rect mTmpRect2;
    final Rect mTmpRect3;
    private final SparseIntArray mTmpTaskIds;
    final ArrayList<WindowState> mTmpWindows;
    final HashMap<IBinder, WindowToken> mTokenMap;
    int mTopWallpaperAnimLayer;
    WindowState mTopWallpaperWin;
    int mTransactionSequence;
    float mTransitionAnimationScaleSetting;
    boolean mTurnOnScreen;
    private ViewServer mViewServer;
    boolean mWaitingForConfig;
    ArrayList<WindowState> mWaitingForDrawn;
    Runnable mWaitingForDrawnCallback;
    WallpaperController mWallpaperControllerLocked;
    InputConsumerImpl mWallpaperInputConsumer;
    Watermark mWatermark;
    float mWindowAnimationScaleSetting;
    final ArrayList<WindowChangeListener> mWindowChangeListeners;
    final HashMap<IBinder, WindowState> mWindowMap;
    final WindowSurfacePlacer mWindowPlacerLocked;
    boolean mWindowsChanged;
    int mWindowsFreezingScreen;

    public interface WindowChangeListener {
        void focusChanged();

        void windowsChanged();
    }

    /* renamed from: com.android.server.wm.WindowManagerService.10 */
    class AnonymousClass10 implements Runnable {
        final /* synthetic */ int val$animationDuration;
        final /* synthetic */ Rect val$bounds;
        final /* synthetic */ Rect val$originalBounds;
        final /* synthetic */ TaskStack val$stack;

        AnonymousClass10(TaskStack val$stack, Rect val$originalBounds, Rect val$bounds, int val$animationDuration) {
            this.val$stack = val$stack;
            this.val$originalBounds = val$originalBounds;
            this.val$bounds = val$bounds;
            this.val$animationDuration = val$animationDuration;
        }

        public void run() {
            WindowManagerService.this.mBoundsAnimationController.animateBounds(this.val$stack, this.val$originalBounds, this.val$bounds, this.val$animationDuration);
        }
    }

    /* renamed from: com.android.server.wm.WindowManagerService.3 */
    static class AnonymousClass3 implements Runnable {
        final /* synthetic */ Context val$context;
        final /* synthetic */ boolean val$haveInputMethods;
        final /* synthetic */ WindowManagerService[] val$holder;
        final /* synthetic */ InputManagerService val$im;
        final /* synthetic */ boolean val$onlyCore;
        final /* synthetic */ boolean val$showBootMsgs;

        AnonymousClass3(WindowManagerService[] val$holder, Context val$context, InputManagerService val$im, boolean val$haveInputMethods, boolean val$showBootMsgs, boolean val$onlyCore) {
            this.val$holder = val$holder;
            this.val$context = val$context;
            this.val$im = val$im;
            this.val$haveInputMethods = val$haveInputMethods;
            this.val$showBootMsgs = val$showBootMsgs;
            this.val$onlyCore = val$onlyCore;
        }

        public void run() {
            IHwWindowManagerService iwms = HwServiceFactory.getHuaweiWindowManagerService();
            if (iwms != null) {
                this.val$holder[WindowManagerService.WINDOW_ANIMATION_SCALE] = iwms.getInstance(this.val$context, this.val$im, this.val$haveInputMethods, this.val$showBootMsgs, this.val$onlyCore);
            } else {
                this.val$holder[WindowManagerService.WINDOW_ANIMATION_SCALE] = new WindowManagerService(this.val$context, this.val$im, this.val$haveInputMethods, this.val$showBootMsgs, this.val$onlyCore);
            }
        }
    }

    /* renamed from: com.android.server.wm.WindowManagerService.7 */
    class AnonymousClass7 implements OnKeyguardExitResult {
        final /* synthetic */ IOnKeyguardExitResult val$callback;

        AnonymousClass7(IOnKeyguardExitResult val$callback) {
            this.val$callback = val$callback;
        }

        public void onKeyguardExitResult(boolean success) {
            try {
                this.val$callback.onKeyguardExitResult(success);
            } catch (RemoteException e) {
            }
        }
    }

    /* renamed from: com.android.server.wm.WindowManagerService.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ IAssistScreenshotReceiver val$receiver;

        AnonymousClass8(IAssistScreenshotReceiver val$receiver) {
            this.val$receiver = val$receiver;
        }

        public void run() {
            try {
                this.val$receiver.send(WindowManagerService.this.screenshotApplicationsInner(null, WindowManagerService.WINDOW_ANIMATION_SCALE, -1, -1, WindowManagerService.SCREENSHOT_FORCE_565, 1.0f, Config.ARGB_8888));
            } catch (RemoteException e) {
            }
        }
    }

    /* renamed from: com.android.server.wm.WindowManagerService.9 */
    class AnonymousClass9 implements DeathRecipient {
        final /* synthetic */ IBinder val$watcherBinder;

        AnonymousClass9(IBinder val$watcherBinder) {
            this.val$watcherBinder = val$watcherBinder;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void binderDied() {
            synchronized (WindowManagerService.this.mWindowMap) {
                int i = WindowManagerService.WINDOW_ANIMATION_SCALE;
                while (true) {
                    if (i < WindowManagerService.this.mRotationWatchers.size()) {
                        if (this.val$watcherBinder == ((RotationWatcher) WindowManagerService.this.mRotationWatchers.get(i)).watcher.asBinder()) {
                            IBinder binder = ((RotationWatcher) WindowManagerService.this.mRotationWatchers.remove(i)).watcher.asBinder();
                            if (binder != null) {
                                binder.unlinkToDeath(this, WindowManagerService.WINDOW_ANIMATION_SCALE);
                            }
                            i--;
                        }
                        i += WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                }
            }
        }
    }

    final class DragInputEventReceiver extends InputEventReceiver {
        private boolean mIsStartEvent;
        private boolean mStylusButtonDownAtStart;

        public DragInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
            this.mIsStartEvent = WindowManagerService.SCREENSHOT_FORCE_565;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            boolean handled = WindowManagerService.PROFILE_ORIENTATION;
            try {
                if (!(!(event instanceof MotionEvent) || (event.getSource() & WindowManagerService.WINDOWS_FREEZING_SCREENS_TIMEOUT) == 0 || WindowManagerService.this.mDragState == null)) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    boolean endDrag = WindowManagerService.PROFILE_ORIENTATION;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0 ? WindowManagerService.SCREENSHOT_FORCE_565 : WindowManagerService.PROFILE_ORIENTATION;
                    if (this.mIsStartEvent) {
                        if (isStylusButtonDown) {
                            this.mStylusButtonDownAtStart = WindowManagerService.SCREENSHOT_FORCE_565;
                        }
                        this.mIsStartEvent = WindowManagerService.PROFILE_ORIENTATION;
                    }
                    switch (motionEvent.getAction()) {
                        case WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE /*1*/:
                            synchronized (WindowManagerService.this.mWindowMap) {
                                endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                                break;
                            }
                        case WindowManagerService.WINDOWS_FREEZING_SCREENS_TIMEOUT /*2*/:
                            if (this.mStylusButtonDownAtStart && !isStylusButtonDown) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                                    break;
                                }
                            }
                            synchronized (WindowManagerService.this.mWindowMap) {
                                WindowManagerService.this.mDragState.notifyMoveLw(newX, newY);
                                break;
                            }
                            break;
                        case WindowManagerService.UPDATE_FOCUS_WILL_PLACE_SURFACES /*3*/:
                            endDrag = WindowManagerService.SCREENSHOT_FORCE_565;
                            if (endDrag) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.this.mDragState.endDragLw();
                                    break;
                                }
                                this.mStylusButtonDownAtStart = WindowManagerService.PROFILE_ORIENTATION;
                                this.mIsStartEvent = WindowManagerService.SCREENSHOT_FORCE_565;
                            }
                            handled = WindowManagerService.SCREENSHOT_FORCE_565;
                            break;
                    }
                    if (endDrag) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            WindowManagerService.this.mDragState.endDragLw();
                        }
                        this.mStylusButtonDownAtStart = WindowManagerService.PROFILE_ORIENTATION;
                        this.mIsStartEvent = WindowManagerService.SCREENSHOT_FORCE_565;
                    }
                    handled = WindowManagerService.SCREENSHOT_FORCE_565;
                }
                finishInputEvent(event, handled);
            } catch (Exception e) {
                Slog.e("WindowManager", "Exception caught by drag handleMotion", e);
                finishInputEvent(event, WindowManagerService.PROFILE_ORIENTATION);
            } catch (Throwable th) {
                finishInputEvent(event, WindowManagerService.PROFILE_ORIENTATION);
            }
        }
    }

    public final class H extends Handler {
        public static final int ADD_STARTING = 5;
        public static final int ALL_WINDOWS_DRAWN = 33;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int APP_TRANSITION_TIMEOUT = 13;
        public static final int BOOT_TIMEOUT = 23;
        public static final int CHECK_IF_BOOT_ANIMATION_FINISHED = 37;
        public static final int CLIENT_FREEZE_TIMEOUT = 30;
        public static final int DO_ANIMATION_CALLBACK = 26;
        public static final int DO_DISPLAY_ADDED = 27;
        public static final int DO_DISPLAY_CHANGED = 29;
        public static final int DO_DISPLAY_REMOVED = 28;
        public static final int DO_TRAVERSAL = 4;
        public static final int DRAG_END_TIMEOUT = 21;
        public static final int DRAG_START_TIMEOUT = 20;
        public static final int ENABLE_SCREEN = 16;
        public static final int FINISHED_STARTING = 7;
        public static final int FINISH_TASK_POSITIONING = 40;
        public static final int FORCE_GC = 15;
        public static final int KEYGUARD_DISMISS_DONE = 101;
        public static final int NEW_ANIMATOR_SCALE = 34;
        public static final int NOTIFY_ACTIVITY_DRAWN = 32;
        public static final int NOTIFY_APP_TRANSITION_CANCELLED = 48;
        public static final int NOTIFY_APP_TRANSITION_FINISHED = 49;
        public static final int NOTIFY_APP_TRANSITION_STARTING = 47;
        public static final int NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED = 53;
        public static final int NOTIFY_STARTING_WINDOW_DRAWN = 50;
        public static final int PERSIST_ANIMATION_SCALE = 14;
        public static final int REMOVE_STARTING = 6;
        public static final int REPORT_APPLICATION_TOKEN_DRAWN = 9;
        public static final int REPORT_APPLICATION_TOKEN_WINDOWS = 8;
        public static final int REPORT_FOCUS_CHANGE = 2;
        public static final int REPORT_HARD_KEYBOARD_STATUS_CHANGE = 22;
        public static final int REPORT_LOSING_FOCUS = 3;
        public static final int REPORT_WINDOWS_CHANGE = 19;
        public static final int RESET_ANR_MESSAGE = 38;
        public static final int RESIZE_STACK = 42;
        public static final int RESIZE_TASK = 43;
        public static final int SEND_NEW_CONFIGURATION = 18;
        public static final int SHOW_CIRCULAR_DISPLAY_MASK = 35;
        public static final int SHOW_EMULATOR_DISPLAY_OVERLAY = 36;
        public static final int SHOW_STRICT_MODE_VIOLATION = 25;
        public static final int TAP_OUTSIDE_TASK = 31;
        public static final int TWO_FINGER_SCROLL_START = 44;
        public static final int UNUSED = 0;
        public static final int UPDATE_ANIMATION_SCALE = 51;
        public static final int UPDATE_DOCKED_STACK_DIVIDER = 41;
        public static final int WAITING_FOR_DRAWN_TIMEOUT = 24;
        public static final int WAIT_KEYGUARD_DISMISS_DONE_TIMEOUT = 100;
        public static final int WALLPAPER_DRAW_PENDING_TIMEOUT = 39;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int WINDOW_REMOVE_TIMEOUT = 52;
        public static final int WINDOW_REPLACEMENT_TIMEOUT = 46;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            int N;
            int i;
            HashMap hashMap;
            AppWindowToken wtoken;
            View view;
            IBinder iBinder;
            IBinder win;
            Runnable callback;
            switch (msg.what) {
                case REPORT_FOCUS_CHANGE /*2*/:
                    AccessibilityController accessibilityController = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mAccessibilityController != null && WindowManagerService.this.getDefaultDisplayContentLocked().getDisplayId() == 0) {
                            accessibilityController = WindowManagerService.this.mAccessibilityController;
                        }
                        WindowState lastFocus = WindowManagerService.this.mLastFocus;
                        WindowState newFocus = WindowManagerService.this.mCurrentFocus;
                        if (lastFocus != newFocus) {
                            WindowManagerService.this.mLastFocus = newFocus;
                            if (!(newFocus == null || lastFocus == null || newFocus.isDisplayedLw())) {
                                WindowManagerService.this.mLosingFocus.add(lastFocus);
                                lastFocus = null;
                                break;
                            }
                            if (accessibilityController != null) {
                                accessibilityController.onWindowFocusChangedNotLocked();
                            }
                            if (newFocus != null) {
                                newFocus.reportFocusChangedSerialized(WindowManagerService.SCREENSHOT_FORCE_565, WindowManagerService.this.mInTouchMode);
                                WindowManagerService.this.notifyFocusChanged();
                            }
                            if (lastFocus != null) {
                                lastFocus.reportFocusChangedSerialized(WindowManagerService.PROFILE_ORIENTATION, WindowManagerService.this.mInTouchMode);
                                break;
                            }
                            break;
                        }
                    }
                case REPORT_LOSING_FOCUS /*3*/:
                    ArrayList<WindowState> losers;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        losers = WindowManagerService.this.mLosingFocus;
                        WindowManagerService.this.mLosingFocus = new ArrayList();
                        break;
                    }
                    N = losers.size();
                    for (i = UNUSED; i < N; i += WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        ((WindowState) losers.get(i)).reportFocusChangedSerialized(WindowManagerService.PROFILE_ORIENTATION, WindowManagerService.this.mInTouchMode);
                    }
                    break;
                case DO_TRAVERSAL /*4*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                    break;
                case ADD_STARTING /*5*/:
                    wtoken = msg.obj;
                    StartingData sd = wtoken.startingData;
                    Flog.i(301, "Add starting wtoken: " + wtoken + " sd= " + sd);
                    if (sd != null) {
                        Configuration configuration;
                        boolean abort;
                        view = null;
                        if (wtoken != null) {
                            try {
                                if (wtoken.mTask != null) {
                                    configuration = wtoken.mTask.mOverrideConfig;
                                    view = WindowManagerService.this.mPolicy.addStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, configuration);
                                    if (view != null) {
                                        abort = WindowManagerService.PROFILE_ORIENTATION;
                                        synchronized (WindowManagerService.this.mWindowMap) {
                                            if (wtoken.removed && wtoken.startingData != null) {
                                                wtoken.startingView = view;
                                                break;
                                            } else if (wtoken.startingWindow != null) {
                                                wtoken.startingWindow = null;
                                                wtoken.startingData = null;
                                                abort = WindowManagerService.SCREENSHOT_FORCE_565;
                                            }
                                            break;
                                        }
                                        if (abort) {
                                            try {
                                                WindowManagerService.this.mPolicy.removeStartingWindow(wtoken.token, view);
                                                break;
                                            } catch (Throwable e) {
                                                Slog.w("WindowManager", "Exception when removing starting window", e);
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Throwable e2) {
                                Slog.w("WindowManager", "Exception when adding starting window", e2);
                            }
                        }
                        configuration = null;
                        view = WindowManagerService.this.mPolicy.addStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, configuration);
                        if (view != null) {
                            abort = WindowManagerService.PROFILE_ORIENTATION;
                            synchronized (WindowManagerService.this.mWindowMap) {
                                if (wtoken.removed) {
                                    break;
                                }
                                if (wtoken.startingWindow != null) {
                                    wtoken.startingWindow = null;
                                    wtoken.startingData = null;
                                    abort = WindowManagerService.SCREENSHOT_FORCE_565;
                                }
                            }
                            if (abort) {
                                WindowManagerService.this.mPolicy.removeStartingWindow(wtoken.token, view);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case REMOVE_STARTING /*6*/:
                    wtoken = (AppWindowToken) msg.obj;
                    iBinder = null;
                    view = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        Flog.i(301, "Remove starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                        if (wtoken.startingWindow != null) {
                            view = wtoken.startingView;
                            iBinder = wtoken.token;
                            wtoken.startingData = null;
                            wtoken.startingView = null;
                            wtoken.startingWindow = null;
                            wtoken.startingDisplayed = WindowManagerService.PROFILE_ORIENTATION;
                        }
                        break;
                    }
                    if (view != null) {
                        try {
                            WindowManagerService.this.mPolicy.removeStartingWindow(iBinder, view);
                            break;
                        } catch (Throwable e22) {
                            Slog.w("WindowManager", "Exception when removing starting window", e22);
                            break;
                        }
                    }
                    break;
                case FINISHED_STARTING /*7*/:
                    while (true) {
                        hashMap = WindowManagerService.this.mWindowMap;
                        synchronized (hashMap) {
                            N = WindowManagerService.this.mFinishedStarting.size();
                            if (N <= 0) {
                                break;
                            }
                            wtoken = (AppWindowToken) WindowManagerService.this.mFinishedStarting.remove(N - 1);
                            Flog.i(301, "Finished starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                            if (wtoken.startingWindow != null) {
                                view = wtoken.startingView;
                                iBinder = wtoken.token;
                                wtoken.startingData = null;
                                wtoken.startingView = null;
                                wtoken.startingWindow = null;
                                wtoken.startingDisplayed = WindowManagerService.PROFILE_ORIENTATION;
                                try {
                                    WindowManagerService.this.mPolicy.removeStartingWindow(iBinder, view);
                                } catch (Throwable e222) {
                                    Slog.w("WindowManager", "Exception when removing starting window", e222);
                                }
                                break;
                            }
                            break;
                        }
                    }
                case REPORT_APPLICATION_TOKEN_WINDOWS /*8*/:
                    wtoken = (AppWindowToken) msg.obj;
                    boolean nowVisible = msg.arg1 != 0 ? WindowManagerService.SCREENSHOT_FORCE_565 : WindowManagerService.PROFILE_ORIENTATION;
                    if (msg.arg2 != 0) {
                    }
                    if (!nowVisible) {
                        wtoken.appToken.windowsGone();
                        break;
                    }
                    try {
                        wtoken.appToken.windowsVisible();
                        break;
                    } catch (RemoteException e3) {
                        break;
                    }
                case REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                    try {
                        ((AppWindowToken) msg.obj).appToken.windowsDrawn();
                        break;
                    } catch (RemoteException e4) {
                        break;
                    }
                case WINDOW_FREEZE_TIMEOUT /*11*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    Slog.w("WindowManager", "Window freeze timeout expired.");
                    WindowManagerService.this.mWindowsFreezingScreen = REPORT_FOCUS_CHANGE;
                    WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                    i = windows.size();
                    while (i > 0) {
                        i--;
                        WindowState w = (WindowState) windows.get(i);
                        if (w.mOrientationChanging) {
                            w.mOrientationChanging = WindowManagerService.PROFILE_ORIENTATION;
                            w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - WindowManagerService.this.mDisplayFreezeTime);
                            Slog.w("WindowManager", "Force clearing orientation change: " + w);
                        }
                    }
                    WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                    break;
                case APP_TRANSITION_TIMEOUT /*13*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    if (!(!WindowManagerService.this.mAppTransition.isTransitionSet() && WindowManagerService.this.mOpeningApps.isEmpty() && WindowManagerService.this.mClosingApps.isEmpty())) {
                        Slog.w("WindowManager", "*** APP TRANSITION TIMEOUT. isTransitionSet()=" + WindowManagerService.this.mAppTransition.isTransitionSet() + " mOpeningApps.size()=" + WindowManagerService.this.mOpeningApps.size() + " mClosingApps.size()=" + WindowManagerService.this.mClosingApps.size());
                        WindowManagerService.this.mAppTransition.setTimeout();
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        break;
                    }
                case PERSIST_ANIMATION_SCALE /*14*/:
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                    break;
                case FORCE_GC /*15*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (!WindowManagerService.this.mAnimator.isAnimating() && !WindowManagerService.this.mAnimationScheduled) {
                            if (!WindowManagerService.this.mDisplayFrozen) {
                                Runtime.getRuntime().gc();
                                break;
                            }
                            return;
                            break;
                        }
                        sendEmptyMessageDelayed(FORCE_GC, 2000);
                    }
                case ENABLE_SCREEN /*16*/:
                    WindowManagerService.this.performEnableScreen();
                    break;
                case APP_FREEZE_TIMEOUT /*17*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    Slog.w("WindowManager", "App freeze timeout expired.");
                    WindowManagerService.this.mWindowsFreezingScreen = REPORT_FOCUS_CHANGE;
                    int numStacks = WindowManagerService.this.mStackIdToStack.size();
                    for (int stackNdx = UNUSED; stackNdx < numStacks; stackNdx += WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        ArrayList<Task> tasks = ((TaskStack) WindowManagerService.this.mStackIdToStack.valueAt(stackNdx)).getTasks();
                        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                            AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                            for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                                AppWindowToken tok = (AppWindowToken) tokens.get(tokenNdx);
                                if (tok.mAppAnimator.freezingScreen) {
                                    Slog.w("WindowManager", "Force clearing freeze: " + tok);
                                    WindowManagerService.this.unsetAppFreezingScreenLocked(tok, WindowManagerService.SCREENSHOT_FORCE_565, WindowManagerService.SCREENSHOT_FORCE_565);
                                }
                            }
                        }
                    }
                    break;
                case SEND_NEW_CONFIGURATION /*18*/:
                    removeMessages(SEND_NEW_CONFIGURATION);
                    WindowManagerService.this.sendNewConfiguration();
                    break;
                case REPORT_WINDOWS_CHANGE /*19*/:
                    if (WindowManagerService.this.mWindowsChanged) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            WindowManagerService.this.mWindowsChanged = WindowManagerService.PROFILE_ORIENTATION;
                            break;
                        }
                        WindowManagerService.this.notifyWindowsChanged();
                        break;
                    }
                    break;
                case DRAG_START_TIMEOUT /*20*/:
                    win = msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    if (WindowManagerService.this.mDragState != null) {
                        WindowManagerService.this.mDragState.unregister();
                        WindowManagerService.this.mInputMonitor.updateInputWindowsLw(WindowManagerService.SCREENSHOT_FORCE_565);
                        WindowManagerService.this.mDragState.reset();
                        WindowManagerService.this.mDragState = null;
                        break;
                    }
                    break;
                case DRAG_END_TIMEOUT /*21*/:
                    win = (IBinder) msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    if (WindowManagerService.this.mDragState != null) {
                        WindowManagerService.this.mDragState.mDragResult = WindowManagerService.PROFILE_ORIENTATION;
                        WindowManagerService.this.mDragState.endDragLw();
                        break;
                    }
                    break;
                case REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                    WindowManagerService.this.notifyHardKeyboardStatusChange();
                    break;
                case BOOT_TIMEOUT /*23*/:
                    WindowManagerService.this.performBootTimeout();
                    break;
                case WAITING_FOR_DRAWN_TIMEOUT /*24*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        Flog.i(NativeResponseCode.SERVICE_FOUND, "Timeout waiting for drawn: undrawn=" + WindowManagerService.this.mWaitingForDrawn);
                        WindowManagerService.this.mWaitingForDrawn.clear();
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                        break;
                    }
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
                    } catch (RemoteException e5) {
                        break;
                    }
                case DO_DISPLAY_ADDED /*27*/:
                    WindowManagerService.this.handleDisplayAdded(msg.arg1);
                    break;
                case DO_DISPLAY_REMOVED /*28*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    WindowManagerService.this.handleDisplayRemovedLocked(msg.arg1);
                    break;
                case DO_DISPLAY_CHANGED /*29*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    WindowManagerService.this.handleDisplayChangedLocked(msg.arg1);
                    break;
                case CLIENT_FREEZE_TIMEOUT /*30*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    if (WindowManagerService.this.mClientFreezingScreen) {
                        WindowManagerService.this.mClientFreezingScreen = WindowManagerService.PROFILE_ORIENTATION;
                        WindowManagerService.this.mLastFinishedFreezeSource = "client-timeout";
                        WindowManagerService.this.stopFreezingDisplayLocked();
                        break;
                    }
                    break;
                case TAP_OUTSIDE_TASK /*31*/:
                    WindowManagerService.this.handleTapOutsideTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case NOTIFY_ACTIVITY_DRAWN /*32*/:
                    try {
                        WindowManagerService.this.mActivityManager.notifyActivityDrawn((IBinder) msg.obj);
                        break;
                    } catch (RemoteException e6) {
                        break;
                    }
                case ALL_WINDOWS_DRAWN /*33*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                        break;
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case NEW_ANIMATOR_SCALE /*34*/:
                    break;
                case SHOW_CIRCULAR_DISPLAY_MASK /*35*/:
                    WindowManagerService.this.showCircularMask(msg.arg1 == WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE ? WindowManagerService.SCREENSHOT_FORCE_565 : WindowManagerService.PROFILE_ORIENTATION);
                    break;
                case SHOW_EMULATOR_DISPLAY_OVERLAY /*36*/:
                    WindowManagerService.this.showEmulatorDisplayOverlay();
                    break;
                case CHECK_IF_BOOT_ANIMATION_FINISHED /*37*/:
                    boolean bootAnimationComplete;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        bootAnimationComplete = WindowManagerService.this.checkBootAnimationCompleteLocked();
                        break;
                    }
                    if (bootAnimationComplete) {
                        WindowManagerService.this.performEnableScreen();
                        break;
                    }
                    break;
                case RESET_ANR_MESSAGE /*38*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    WindowManagerService.this.mLastANRState = null;
                    break;
                case WALLPAPER_DRAW_PENDING_TIMEOUT /*39*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mWallpaperControllerLocked.processWallpaperDrawPendingTimeout()) {
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        }
                        break;
                    }
                case FINISH_TASK_POSITIONING /*40*/:
                    WindowManagerService.this.finishPositioning();
                    break;
                case UPDATE_DOCKED_STACK_DIVIDER /*41*/:
                    break;
                case RESIZE_STACK /*42*/:
                    try {
                        WindowManagerService.this.mActivityManager.resizeStack(msg.arg1, (Rect) msg.obj, msg.arg2 == WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE ? WindowManagerService.SCREENSHOT_FORCE_565 : WindowManagerService.PROFILE_ORIENTATION, WindowManagerService.PROFILE_ORIENTATION, WindowManagerService.PROFILE_ORIENTATION, -1);
                        break;
                    } catch (RemoteException e7) {
                        break;
                    }
                case RESIZE_TASK /*43*/:
                    try {
                        WindowManagerService.this.mActivityManager.resizeTask(msg.arg1, (Rect) msg.obj, msg.arg2);
                        break;
                    } catch (RemoteException e8) {
                        break;
                    }
                case TWO_FINGER_SCROLL_START /*44*/:
                    WindowManagerService.this.startScrollingTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case WINDOW_REPLACEMENT_TIMEOUT /*46*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        for (i = WindowManagerService.this.mReplacingWindowTimeouts.size() - 1; i >= 0; i--) {
                            ((AppWindowToken) WindowManagerService.this.mReplacingWindowTimeouts.get(i)).clearTimedoutReplacesLocked();
                        }
                        WindowManagerService.this.mReplacingWindowTimeouts.clear();
                        break;
                    }
                case NOTIFY_APP_TRANSITION_STARTING /*47*/:
                    break;
                case NOTIFY_APP_TRANSITION_CANCELLED /*48*/:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionCancelled();
                    break;
                case NOTIFY_APP_TRANSITION_FINISHED /*49*/:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionFinished();
                    break;
                case NOTIFY_STARTING_WINDOW_DRAWN /*50*/:
                    WindowManagerService.this.mAmInternal.notifyStartingWindowDrawn();
                    break;
                case UPDATE_ANIMATION_SCALE /*51*/:
                    switch (msg.arg1) {
                        case UNUSED /*0*/:
                            WindowManagerService.this.mWindowAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                            break;
                        case WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE /*1*/:
                            WindowManagerService.this.mTransitionAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                            break;
                        case REPORT_FOCUS_CHANGE /*2*/:
                            WindowManagerService.this.mAnimatorDurationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                            WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            break;
                        default:
                            break;
                    }
                case WINDOW_REMOVE_TIMEOUT /*52*/:
                    WindowState window = msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        break;
                    }
                    LayoutParams layoutParams = window.mAttrs;
                    layoutParams.flags &= -129;
                    window.setDisplayLayoutNeeded();
                    WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                    break;
                case NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED /*53*/:
                    WindowManagerService.this.mAmInternal.notifyDockedStackMinimizedChanged(msg.arg1 == WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE ? WindowManagerService.SCREENSHOT_FORCE_565 : WindowManagerService.PROFILE_ORIENTATION);
                    break;
                case WAIT_KEYGUARD_DISMISS_DONE_TIMEOUT /*100*/:
                case KEYGUARD_DISMISS_DONE /*101*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        callback = WindowManagerService.this.mKeyguardDismissDoneCallback;
                        WindowManagerService.this.mKeyguardDismissDoneCallback = null;
                        WindowManagerService.this.mKeyguardWin = null;
                        WindowManagerService.this.mTopWallpaperWin = null;
                        break;
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
            }
        }
    }

    private static final class HideNavInputConsumer extends InputConsumerImpl implements InputConsumer {
        private final InputEventReceiver mInputEventReceiver;

        HideNavInputConsumer(WindowManagerService service, Looper looper, Factory inputEventReceiverFactory) {
            super(service, "input consumer", null);
            this.mInputEventReceiver = inputEventReceiverFactory.createInputEventReceiver(this.mClientChannel, looper);
        }

        public void dismiss() {
            if (this.mService.removeInputConsumer()) {
                synchronized (this.mService.mWindowMap) {
                    this.mInputEventReceiver.dispose();
                    disposeChannelsLw();
                }
            }
        }
    }

    private final class LocalService extends WindowManagerInternal {
        private LocalService() {
        }

        public void requestTraversalFromDisplayManager() {
            WindowManagerService.this.requestTraversal();
        }

        public void setMagnificationSpec(MagnificationSpec spec) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController != null) {
                    WindowManagerService.this.mAccessibilityController.setMagnificationSpecLocked(spec);
                } else {
                    throw new IllegalStateException("Magnification callbacks not set!");
                }
            }
            if (Binder.getCallingPid() != Process.myPid()) {
                spec.recycle();
            }
        }

        public void getMagnificationRegion(Region magnificationRegion) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController != null) {
                    WindowManagerService.this.mAccessibilityController.getMagnificationRegionLocked(magnificationRegion);
                } else {
                    throw new IllegalStateException("Magnification callbacks not set!");
                }
            }
        }

        public MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder windowToken) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(windowToken);
                if (windowState == null) {
                    return null;
                }
                MagnificationSpec spec = null;
                if (WindowManagerService.this.mAccessibilityController != null) {
                    spec = WindowManagerService.this.mAccessibilityController.getMagnificationSpecForWindowLocked(windowState);
                }
                if ((spec == null || spec.isNop()) && windowState.mGlobalScale == 1.0f) {
                    return null;
                }
                spec = spec == null ? MagnificationSpec.obtain() : MagnificationSpec.obtain(spec);
                spec.scale *= windowState.mGlobalScale;
                return spec;
            }
        }

        public void setMagnificationCallbacks(MagnificationCallbacks callbacks) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController == null) {
                    WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                }
                WindowManagerService.this.mAccessibilityController.setMagnificationCallbacksLocked(callbacks);
                if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                    WindowManagerService.this.mAccessibilityController = null;
                }
            }
        }

        public void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback callback) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController == null) {
                    WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                }
                WindowManagerService.this.mAccessibilityController.setWindowsForAccessibilityCallback(callback);
                if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                    WindowManagerService.this.mAccessibilityController = null;
                }
            }
        }

        public void setInputFilter(IInputFilter filter) {
            WindowManagerService.this.mInputManager.setInputFilter(filter);
        }

        public IBinder getFocusedWindowToken() {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = WindowManagerService.this.getFocusedWindowLocked();
                if (windowState != null) {
                    IBinder asBinder = windowState.mClient.asBinder();
                    return asBinder;
                }
                return null;
            }
        }

        public boolean isKeyguardLocked() {
            return WindowManagerService.this.isKeyguardLocked();
        }

        public void showGlobalActions() {
            WindowManagerService.this.showGlobalActions();
        }

        public void getWindowFrame(IBinder token, Rect outBounds) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                if (windowState != null) {
                    outBounds.set(windowState.mFrame);
                } else {
                    outBounds.setEmpty();
                }
            }
        }

        public void waitForAllWindowsDrawn(Runnable callback, long timeout) {
            boolean allWindowsDrawn = WindowManagerService.PROFILE_ORIENTATION;
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mWaitingForDrawnCallback = callback;
                WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                int winNdx;
                WindowState win;
                boolean isForceHiding;
                if (WindowManagerService.this.isCoverOpen()) {
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "waitForAllWindowsDrawn  cover is open or null");
                    for (winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                        win = (WindowState) windows.get(winNdx);
                        isForceHiding = WindowManagerService.this.mPolicy.isForceHiding(win.mAttrs);
                        if (win.isVisibleLw() && (win.mAppToken != null || isForceHiding)) {
                            win.mWinAnimator.mDrawState = WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE;
                            win.mLastContentInsets.set(-1, -1, -1, -1);
                            WindowManagerService.this.mWaitingForDrawn.add(win);
                            if (isForceHiding) {
                                break;
                            }
                        }
                    }
                } else {
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "waitForAllWindowsDrawn  cover is close");
                    for (winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                        win = (WindowState) windows.get(winNdx);
                        isForceHiding = WindowManagerService.this.mPolicy.isForceHiding(win.mAttrs);
                        if ((win.isVisibleLw() && (win.mAppToken != null || isForceHiding)) || win.mAttrs.type == 2100 || win.mAttrs.type == 2101) {
                            win.mWinAnimator.mDrawState = WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE;
                            win.mLastContentInsets.set(-1, -1, -1, -1);
                            WindowManagerService.this.mWaitingForDrawn.add(win);
                            if (isForceHiding) {
                                break;
                            }
                        }
                    }
                }
                WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                WindowManagerService.this.mH.removeMessages(24);
                if (WindowManagerService.this.mWaitingForDrawn.isEmpty()) {
                    allWindowsDrawn = WindowManagerService.SCREENSHOT_FORCE_565;
                } else {
                    WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                    WindowManagerService.this.checkDrawnWindowsLocked();
                }
            }
            if (allWindowsDrawn) {
                callback.run();
                return;
            }
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                WindowManagerService.this.checkDrawnWindowsLocked();
            }
        }

        public void addWindowToken(IBinder token, int type) {
            WindowManagerService.this.addWindowToken(token, type);
        }

        public void removeWindowToken(IBinder token, boolean removeWindows) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (removeWindows) {
                    WindowToken wtoken = (WindowToken) WindowManagerService.this.mTokenMap.remove(token);
                    if (wtoken != null) {
                        wtoken.removeAllWindows();
                    }
                }
                WindowManagerService.this.removeWindowToken(token);
            }
        }

        public void registerAppTransitionListener(AppTransitionListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mAppTransition.registerListenerLocked(listener);
            }
        }

        public int getInputMethodWindowVisibleHeight() {
            int inputMethodWindowVisibleHeightLw;
            synchronized (WindowManagerService.this.mWindowMap) {
                inputMethodWindowVisibleHeightLw = WindowManagerService.this.mPolicy.getInputMethodWindowVisibleHeightLw();
            }
            return inputMethodWindowVisibleHeightLw;
        }

        public void saveLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mInputMethodWindow != null) {
                    WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(WindowManagerService.this.mInputMethodWindow, WindowManagerService.this.mInputMethodTarget);
                }
            }
        }

        public void clearLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(null, null);
            }
        }

        public boolean isHardKeyboardAvailable() {
            boolean z;
            synchronized (WindowManagerService.this.mWindowMap) {
                z = WindowManagerService.this.mHardKeyboardAvailable;
            }
            return z;
        }

        public void setOnHardKeyboardStatusChangeListener(OnHardKeyboardStatusChangeListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mHardKeyboardStatusChangeListener = listener;
            }
        }

        public boolean isStackVisible(int stackId) {
            boolean isStackVisibleLocked;
            synchronized (WindowManagerService.this.mWindowMap) {
                isStackVisibleLocked = WindowManagerService.this.isStackVisibleLocked(stackId);
            }
            return isStackVisibleLocked;
        }

        public boolean isDockedDividerResizing() {
            boolean isResizing;
            synchronized (WindowManagerService.this.mWindowMap) {
                isResizing = WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().isResizing();
            }
            return isResizing;
        }

        public void waitForKeyguardDismissDone(Runnable callback, long timeout) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mKeyguardDismissDoneCallback = callback;
                WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState win = (WindowState) windows.get(winNdx);
                    if (win.mAttrs.type == 2004) {
                        boolean z;
                        WindowManagerService.this.mKeyguardWin = win;
                        WindowManagerService windowManagerService = WindowManagerService.this;
                        if ((WindowManagerService.this.mKeyguardWin.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            z = WindowManagerService.SCREENSHOT_FORCE_565;
                        } else {
                            z = WindowManagerService.PROFILE_ORIENTATION;
                        }
                        windowManagerService.mKeyguardAttachWallpaper = z;
                    }
                    if (win.mAttrs.type == 2013 && WindowManagerService.this.mTopWallpaperWin == null) {
                        WindowManagerService.this.mTopWallpaperWin = win;
                        WindowManagerService.this.mTopWallpaperAnimLayer = WindowManagerService.this.mTopWallpaperWin.mWinAnimator.mAnimLayer;
                    }
                }
                WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                boolean iskeyguardWindNull = WindowManagerService.this.mKeyguardWin == null ? WindowManagerService.SCREENSHOT_FORCE_565 : WindowManagerService.PROFILE_ORIENTATION;
            }
            WindowManagerService.this.mH.removeMessages(100);
            if (WindowManagerService.this.mPolicy.isStatusBarKeyguardShowing() || !iskeyguardWindNull) {
                WindowManagerService.this.mH.sendEmptyMessageDelayed(100, timeout);
                return;
            }
            Slog.i(WindowManagerService.TAG, "waitForKeyguardDismissDone there is no keyguard.");
            callback.run();
        }

        public void setDockedStackDividerRotation(int rotation) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().setDockedStackDividerRotation(rotation);
            }
        }
    }

    private static class MousePositionTracker implements PointerEventListener {
        private boolean mLatestEventWasMouse;
        private float mLatestMouseX;
        private float mLatestMouseY;

        private MousePositionTracker() {
        }

        void updatePosition(float x, float y) {
            synchronized (this) {
                this.mLatestEventWasMouse = WindowManagerService.SCREENSHOT_FORCE_565;
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
                this.mLatestEventWasMouse = WindowManagerService.PROFILE_ORIENTATION;
            }
        }
    }

    class RotationWatcher {
        DeathRecipient deathRecipient;
        IRotationWatcher watcher;

        RotationWatcher(IRotationWatcher w, DeathRecipient d) {
            this.watcher = w;
            this.deathRecipient = d;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mAnimationDurationScaleUri;
        private final Uri mDisplayInversionEnabledUri;
        private final Uri mTransitionAnimationScaleUri;
        private final Uri mWindowAnimationScaleUri;

        public SettingsObserver() {
            super(new Handler());
            this.mDisplayInversionEnabledUri = Secure.getUriFor("accessibility_display_inversion_enabled");
            this.mWindowAnimationScaleUri = Global.getUriFor("window_animation_scale");
            this.mTransitionAnimationScaleUri = Global.getUriFor("transition_animation_scale");
            this.mAnimationDurationScaleUri = Global.getUriFor("animator_duration_scale");
            ContentResolver resolver = WindowManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mDisplayInversionEnabledUri, WindowManagerService.PROFILE_ORIENTATION, this, -1);
            resolver.registerContentObserver(this.mWindowAnimationScaleUri, WindowManagerService.PROFILE_ORIENTATION, this, -1);
            resolver.registerContentObserver(this.mTransitionAnimationScaleUri, WindowManagerService.PROFILE_ORIENTATION, this, -1);
            resolver.registerContentObserver(this.mAnimationDurationScaleUri, WindowManagerService.PROFILE_ORIENTATION, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    WindowManagerService.this.updateCircularDisplayMaskIfNeeded();
                } else {
                    int mode;
                    if (this.mWindowAnimationScaleUri.equals(uri)) {
                        mode = WindowManagerService.WINDOW_ANIMATION_SCALE;
                    } else if (this.mTransitionAnimationScaleUri.equals(uri)) {
                        mode = WindowManagerService.WINDOWS_FREEZING_SCREENS_ACTIVE;
                    } else if (this.mAnimationDurationScaleUri.equals(uri)) {
                        mode = WindowManagerService.WINDOWS_FREEZING_SCREENS_TIMEOUT;
                    } else {
                        return;
                    }
                    WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(51, mode, WindowManagerService.WINDOW_ANIMATION_SCALE));
                }
            }
        }
    }

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateAnimationScaleMode {
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.WindowManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.WindowManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowManagerService.<clinit>():void");
    }

    boolean updateStatusBarVisibilityLocked(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.WindowManagerService.updateStatusBarVisibilityLocked(int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
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
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowManagerService.updateStatusBarVisibilityLocked(int):boolean");
    }

    int getDragLayerLocked() {
        return (this.mPolicy.windowTypeToLayerLw(2016) * TYPE_LAYER_MULTIPLIER) + TYPE_LAYER_OFFSET;
    }

    public static WindowManagerService main(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        WindowManagerService[] holder = new WindowManagerService[WINDOWS_FREEZING_SCREENS_ACTIVE];
        DisplayThread.getHandler().runWithScissors(new AnonymousClass3(holder, context, im, haveInputMethods, showBootMsgs, onlyCore), 0);
        return holder[WINDOW_ANIMATION_SCALE];
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
        return Boolean.valueOf(pkgIn != null ? pkgIn.booleanValue() : PROFILE_ORIENTATION).booleanValue();
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
                        this.mPackages.put(pkg, Boolean.valueOf(SCREENSHOT_FORCE_565));
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

    protected WindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        this.mPackages = new HashMap();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction())) {
                    WindowManagerService.this.mKeyguardDisableHandler.sendEmptyMessage(WindowManagerService.UPDATE_FOCUS_WILL_PLACE_SURFACES);
                }
            }
        };
        this.mCurrentProfileIds = new int[WINDOW_ANIMATION_SCALE];
        this.mPolicy = HwPolicyFactory.getHwPhoneWindowManager();
        this.mSessions = new ArraySet();
        this.mWindowMap = new HashMap();
        this.mTokenMap = new HashMap();
        this.mFinishedStarting = new ArrayList();
        this.mFinishedEarlyAnim = new ArrayList();
        this.mReplacingWindowTimeouts = new ArrayList();
        this.mResizingWindows = new ArrayList();
        this.mPendingRemove = new ArrayList();
        this.mPendingRemoveTmp = new WindowState[20];
        this.mDestroySurface = new ArrayList();
        this.mDestroyPreservedSurface = new ArrayList();
        this.mLosingFocus = new ArrayList();
        this.mForceRemoves = new ArrayList();
        this.mWaitingForDrawn = new ArrayList();
        this.mRebuildTmp = new WindowState[20];
        this.mScreenCaptureDisabled = new SparseArray();
        this.mTmpFloats = new float[9];
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mTmpRect3 = new Rect();
        this.mDisplayEnabled = PROFILE_ORIENTATION;
        this.mSystemBooted = PROFILE_ORIENTATION;
        this.mForceDisplayEnabled = PROFILE_ORIENTATION;
        this.mShowingBootMessages = PROFILE_ORIENTATION;
        this.mBootAnimationStopped = PROFILE_ORIENTATION;
        this.mLastWakeLockHoldingWindow = null;
        this.mLastWakeLockObscuringWindow = null;
        this.mDisplayContents = new SparseArray(WINDOWS_FREEZING_SCREENS_TIMEOUT);
        this.mRotation = SystemProperties.getInt("ro.panel.hw_orientation", WINDOW_ANIMATION_SCALE) / 90;
        this.mForcedAppOrientation = -1;
        this.mAltOrientation = PROFILE_ORIENTATION;
        this.mDockedStackCreateMode = WINDOW_ANIMATION_SCALE;
        this.mTmpTaskIds = new SparseIntArray();
        this.mChangedStackList = new ArrayList();
        this.mForceResizableTasks = PROFILE_ORIENTATION;
        this.mRotationWatchers = new ArrayList();
        this.mSystemDecorLayer = WINDOW_ANIMATION_SCALE;
        this.mScreenRect = new Rect();
        this.mDisplayFrozen = PROFILE_ORIENTATION;
        this.mDisplayFreezeTime = 0;
        this.mLastDisplayFreezeDuration = WINDOW_ANIMATION_SCALE;
        this.mLastFinishedFreezeSource = null;
        this.mWaitingForConfig = PROFILE_ORIENTATION;
        this.mWindowsFreezingScreen = WINDOW_ANIMATION_SCALE;
        this.mClientFreezingScreen = PROFILE_ORIENTATION;
        this.mAppsFreezingScreen = WINDOW_ANIMATION_SCALE;
        this.mLastWindowForcedOrientation = -1;
        this.mLastKeyguardForcedOrientation = -1;
        this.mLayoutSeq = WINDOW_ANIMATION_SCALE;
        this.mLastStatusBarVisibility = WINDOW_ANIMATION_SCALE;
        this.mLastDispatchedSystemUiVisibility = WINDOW_ANIMATION_SCALE;
        this.mCurConfiguration = new Configuration();
        this.mSkipAppTransitionAnimation = PROFILE_ORIENTATION;
        this.mOpeningApps = new ArraySet();
        this.mClosingApps = new ArraySet();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mRealDisplayMetrics = new DisplayMetrics();
        this.mTmpDisplayMetrics = new DisplayMetrics();
        this.mCompatDisplayMetrics = new DisplayMetrics();
        this.mH = new H();
        this.mChoreographer = Choreographer.getInstance();
        this.mCurrentFocus = null;
        this.mLastFocus = null;
        this.mInputMethodTarget = null;
        this.mInputMethodWindow = null;
        this.mInputMethodDialogs = new ArrayList();
        this.mTmpWindows = new ArrayList();
        this.mFirstStartHome = SCREENSHOT_FORCE_565;
        this.mRebuildAppWinsOnBoot = SystemProperties.getBoolean("ro.config.hw_rebuild_windows", PROFILE_ORIENTATION);
        this.mFocusedApp = null;
        this.mWindowAnimationScaleSetting = 1.0f;
        this.mTransitionAnimationScaleSetting = 1.0f;
        this.mAnimatorDurationScaleSetting = 1.0f;
        this.mAnimationsDisabled = PROFILE_ORIENTATION;
        this.mDragState = null;
        this.mTaskIdToTask = new SparseArray();
        this.mStackIdToStack = new SparseArray();
        this.mWindowChangeListeners = new ArrayList();
        this.mWindowsChanged = PROFILE_ORIENTATION;
        this.mTempConfiguration = new Configuration();
        this.mIsPerfBoost = PROFILE_ORIENTATION;
        this.mNoAnimationNotifyOnTransitionFinished = new ArrayList();
        this.mReconfigureOnConfigurationChanged = new DisplayContentList();
        this.mActivityManagerAppTransitionNotifier = new AppTransitionListener() {
            public void onAppTransitionCancelledLocked() {
                WindowManagerService.this.mH.sendEmptyMessage(48);
            }

            public void onAppTransitionFinishedLocked(IBinder token) {
                WindowManagerService.this.mH.sendEmptyMessage(49);
                AppWindowToken atoken = WindowManagerService.this.findAppWindowToken(token);
                if (atoken != null) {
                    if (atoken.mLaunchTaskBehind) {
                        try {
                            WindowManagerService.this.mActivityManager.notifyLaunchTaskBehindComplete(atoken.token);
                        } catch (RemoteException e) {
                        }
                        atoken.mLaunchTaskBehind = WindowManagerService.PROFILE_ORIENTATION;
                    } else {
                        atoken.updateReportedVisibilityLocked();
                        if (atoken.mEnteringAnimation) {
                            atoken.mEnteringAnimation = WindowManagerService.PROFILE_ORIENTATION;
                            try {
                                WindowManagerService.this.mActivityManager.notifyEnterAnimationComplete(atoken.token);
                            } catch (RemoteException e2) {
                            }
                        }
                    }
                }
            }
        };
        this.mLazyModeOn = WINDOW_ANIMATION_SCALE;
        this.mInputMonitor = new InputMonitor(this);
        this.mMousePositionTracker = new MousePositionTracker();
        this.mContext = context;
        this.mHaveInputMethods = haveInputMethods;
        this.mAllowBootMessages = showBootMsgs;
        this.mOnlyCore = onlyCore;
        this.mLimitedAlphaCompositing = context.getResources().getBoolean(17956875);
        this.mHasPermanentDpad = context.getResources().getBoolean(17956998);
        this.mInTouchMode = context.getResources().getBoolean(17957025);
        this.mDrawLockTimeoutMillis = (long) context.getResources().getInteger(17694870);
        this.mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(17957027);
        this.mInputManager = inputManager;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mDisplaySettings = new DisplaySettings();
        this.mDisplaySettings.readSettingsLocked();
        this.mWallpaperControllerLocked = new WallpaperController(this);
        this.mWindowPlacerLocked = new WindowSurfacePlacer(this);
        this.mLayersController = new WindowLayersController(this);
        LocalServices.addService(WindowManagerPolicy.class, this.mPolicy);
        this.mPointerEventDispatcher = new PointerEventDispatcher(this.mInputManager.monitorInput("WindowManager"));
        this.mFxSession = new SurfaceSession();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mCust = (HwCustWindowManagerService) HwCustUtils.createObj(HwCustWindowManagerService.class, new Object[WINDOW_ANIMATION_SCALE]);
        this.mDisplays = this.mDisplayManager.getDisplays();
        Display[] displayArr = this.mDisplays;
        int length = displayArr.length;
        for (int i = WINDOW_ANIMATION_SCALE; i < length; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
            createDisplayContentLocked(displayArr[i]);
        }
        this.mKeyguardDisableHandler = new KeyguardDisableHandler(this.mContext, this.mPolicy);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
            public void onLowPowerModeChanged(boolean enabled) {
                synchronized (WindowManagerService.this.mWindowMap) {
                    if (!(WindowManagerService.this.mAnimationsDisabled == enabled || WindowManagerService.this.mAllowAnimationsInLowPowerMode)) {
                        WindowManagerService.this.mAnimationsDisabled = enabled;
                        WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                    }
                }
            }
        });
        this.mAnimationsDisabled = this.mPowerManagerInternal.getLowPowerModeEnabled();
        this.mScreenFrozenLock = this.mPowerManager.newWakeLock(WINDOWS_FREEZING_SCREENS_ACTIVE, "SCREEN_FROZEN");
        this.mScreenFrozenLock.setReferenceCounted(PROFILE_ORIENTATION);
        this.mAppTransition = HwServiceFactory.createHwAppTransition(context, this);
        this.mAppTransition.registerListenerLocked(this.mActivityManagerAppTransitionNotifier);
        this.mBoundsAnimationController = new BoundsAnimationController(this.mAppTransition, UiThread.getHandler());
        this.mActivityManager = ActivityManagerNative.getDefault();
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
        this.mTransitionAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "transition_animation_scale", this.mTransitionAnimationScaleSetting);
        setAnimatorDurationScale(Global.getFloat(context.getContentResolver(), "animator_duration_scale", this.mAnimatorDurationScaleSetting));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mSettingsObserver = new SettingsObserver();
        this.mHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870922, "WindowManager");
        this.mHoldingScreenWakeLock.setReferenceCounted(PROFILE_ORIENTATION);
        this.mAnimator = new WindowAnimator(this);
        this.mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(17956916);
        LocalServices.addService(WindowManagerInternal.class, new LocalService());
        initPolicy();
        Watchdog.getInstance().addMonitor(this);
        SurfaceControl.openTransaction();
        try {
            createWatermarkInTransaction();
            showEmulatorDisplayOverlayIfNeeded();
            if (HISI_PERF_OPT) {
                initAccelPackages();
            }
        } finally {
            SurfaceControl.closeTransaction();
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
                Slog.wtf("WindowManager", "Window Manager Crash", e);
            }
            throw e;
        }
    }

    private void placeWindowAfter(WindowState pos, WindowState window) {
        WindowList windows = pos.getWindowList();
        windows.add(windows.indexOf(pos) + WINDOWS_FREEZING_SCREENS_ACTIVE, window);
        this.mWindowsChanged = SCREENSHOT_FORCE_565;
    }

    private void placeWindowBefore(WindowState pos, WindowState window) {
        WindowList windows = pos.getWindowList();
        int i = windows.indexOf(pos);
        if (i < 0) {
            Slog.w("WindowManager", "placeWindowBefore: Unable to find " + pos + " in " + windows);
            i = WINDOW_ANIMATION_SCALE;
        }
        windows.add(i, window);
        this.mWindowsChanged = SCREENSHOT_FORCE_565;
    }

    protected int findIdxBasedOnAppTokens(WindowState win) {
        WindowList windows = win.getWindowList();
        for (int j = windows.size() - 1; j >= 0; j--) {
            if (((WindowState) windows.get(j)).mAppToken == win.mAppToken) {
                return j;
            }
        }
        return -1;
    }

    private WindowList getTokenWindowsOnDisplay(WindowToken token, DisplayContent displayContent) {
        WindowList windowList = new WindowList();
        int count = token.windows.size();
        for (int i = WINDOW_ANIMATION_SCALE; i < count; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
            WindowState win = (WindowState) token.windows.get(i);
            if (win.getDisplayContent() == displayContent) {
                windowList.add(win);
            }
        }
        return windowList;
    }

    private int indexOfWinInWindowList(WindowState targetWin, WindowList windows) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) windows.get(i);
            if (w == targetWin) {
                return i;
            }
            if (!w.mChildWindows.isEmpty() && indexOfWinInWindowList(targetWin, w.mChildWindows) >= 0) {
                return i;
            }
        }
        return -1;
    }

    private int addAppWindowToListLocked(WindowState win) {
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent == null) {
            return WINDOW_ANIMATION_SCALE;
        }
        IWindow client = win.mClient;
        WindowToken token = win.mToken;
        WindowList windows = displayContent.getWindowList();
        WindowList tokenWindowList = getTokenWindowsOnDisplay(token, displayContent);
        if (!tokenWindowList.isEmpty()) {
            return addAppWindowToTokenListLocked(win, token, windows, tokenWindowList);
        }
        WindowState pos = null;
        ArrayList<Task> tasks = displayContent.getTasks();
        int tokenNdx = -1;
        int taskNdx = tasks.size() - 1;
        while (taskNdx >= 0) {
            AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
            tokenNdx = tokens.size() - 1;
            while (tokenNdx >= 0) {
                WindowToken t = (AppWindowToken) tokens.get(tokenNdx);
                if (t == token) {
                    tokenNdx--;
                    if (tokenNdx < 0) {
                        taskNdx--;
                        if (taskNdx >= 0) {
                            tokenNdx = ((Task) tasks.get(taskNdx)).mAppTokens.size() - 1;
                        }
                    }
                    if (tokenNdx >= 0) {
                        break;
                    }
                    taskNdx--;
                } else {
                    tokenWindowList = getTokenWindowsOnDisplay(t, displayContent);
                    if (!t.sendingToBottom && tokenWindowList.size() > 0) {
                        pos = (WindowState) tokenWindowList.get(WINDOW_ANIMATION_SCALE);
                    }
                    tokenNdx--;
                }
            }
            if (tokenNdx >= 0) {
                break;
            }
            taskNdx--;
        }
        WindowToken atoken;
        if (pos != null) {
            atoken = (WindowToken) this.mTokenMap.get(pos.mClient.asBinder());
            if (atoken != null) {
                tokenWindowList = getTokenWindowsOnDisplay(atoken, displayContent);
                if (tokenWindowList.size() > 0) {
                    WindowState bottom = (WindowState) tokenWindowList.get(WINDOW_ANIMATION_SCALE);
                    if (bottom.mSubLayer < 0) {
                        pos = bottom;
                    }
                }
            }
            placeWindowBefore(pos, win);
            return WINDOW_ANIMATION_SCALE;
        }
        for (taskNdx = 
        /* Method generation error in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r15_5 'taskNdx' int) = (r15_3 'taskNdx' int), (r15_1 'taskNdx' int) binds: {(r15_3 'taskNdx' int)=B:66:0x0087, (r15_1 'taskNdx' int)=B:65:0x0087} in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 27 more
 */

        private int addAppWindowToTokenListLocked(WindowState win, WindowToken token, WindowList windows, WindowList tokenWindowList) {
            if (win.mAttrs.type == WINDOWS_FREEZING_SCREENS_ACTIVE) {
                WindowState lowestWindow = (WindowState) tokenWindowList.get(WINDOW_ANIMATION_SCALE);
                placeWindowBefore(lowestWindow, win);
                return indexOfWinInWindowList(lowestWindow, token.windows);
            }
            AppWindowToken atoken = win.mAppToken;
            WindowState lastWindow = (WindowState) tokenWindowList.get(tokenWindowList.size() - 1);
            if (atoken == null || lastWindow != atoken.startingWindow) {
                int tokenWindowsPos;
                int newIdx = findIdxBasedOnAppTokens(win);
                windows.add(newIdx + WINDOWS_FREEZING_SCREENS_ACTIVE, win);
                if (newIdx < 0) {
                    tokenWindowsPos = WINDOW_ANIMATION_SCALE;
                } else {
                    tokenWindowsPos = indexOfWinInWindowList((WindowState) windows.get(newIdx), token.windows) + WINDOWS_FREEZING_SCREENS_ACTIVE;
                }
                this.mWindowsChanged = SCREENSHOT_FORCE_565;
                return tokenWindowsPos;
            }
            placeWindowBefore(lastWindow, win);
            return indexOfWinInWindowList(lastWindow, token.windows);
        }

        private void addFreeWindowToListLocked(WindowState win) {
            WindowList windows = win.getWindowList();
            int myLayer = win.mBaseLayer;
            int i = windows.size() - 1;
            while (i >= 0) {
                WindowState otherWin = (WindowState) windows.get(i);
                if (otherWin.getBaseType() != 2013 && otherWin.mBaseLayer <= myLayer) {
                    break;
                }
                i--;
            }
            windows.add(i + WINDOWS_FREEZING_SCREENS_ACTIVE, win);
            this.mWindowsChanged = SCREENSHOT_FORCE_565;
        }

        private void addAttachedWindowToListLocked(WindowState win, boolean addToToken) {
            WindowToken token = win.mToken;
            DisplayContent displayContent = win.getDisplayContent();
            if (displayContent != null) {
                int i;
                WindowState attached = win.mAttachedWindow;
                WindowList tokenWindowList = getTokenWindowsOnDisplay(token, displayContent);
                int NA = tokenWindowList.size();
                int sublayer = win.mSubLayer;
                int largestSublayer = UsbAudioDevice.kAudioDeviceMeta_Alsa;
                WindowState windowWithLargestSublayer = null;
                for (i = WINDOW_ANIMATION_SCALE; i < NA; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState w = (WindowState) tokenWindowList.get(i);
                    int wSublayer = w.mSubLayer;
                    if (wSublayer >= largestSublayer) {
                        largestSublayer = wSublayer;
                        windowWithLargestSublayer = w;
                    }
                    if (sublayer < 0) {
                        if (wSublayer >= sublayer) {
                            if (addToToken) {
                                token.windows.add(i, win);
                            }
                            if (wSublayer >= 0) {
                                w = attached;
                            }
                            placeWindowBefore(w, win);
                            if (i >= NA) {
                                if (addToToken) {
                                    token.windows.add(win);
                                }
                                if (sublayer < 0) {
                                    placeWindowBefore(attached, win);
                                } else {
                                    if (largestSublayer < 0) {
                                        windowWithLargestSublayer = attached;
                                    }
                                    placeWindowAfter(windowWithLargestSublayer, win);
                                }
                            }
                        }
                    } else if (wSublayer > sublayer) {
                        if (addToToken) {
                            token.windows.add(i, win);
                        }
                        placeWindowBefore(w, win);
                        if (i >= NA) {
                            if (addToToken) {
                                token.windows.add(win);
                            }
                            if (sublayer < 0) {
                                if (largestSublayer < 0) {
                                    windowWithLargestSublayer = attached;
                                }
                                placeWindowAfter(windowWithLargestSublayer, win);
                            } else {
                                placeWindowBefore(attached, win);
                            }
                        }
                    }
                }
                if (i >= NA) {
                    if (addToToken) {
                        token.windows.add(win);
                    }
                    if (sublayer < 0) {
                        placeWindowBefore(attached, win);
                    } else {
                        if (largestSublayer < 0) {
                            windowWithLargestSublayer = attached;
                        }
                        placeWindowAfter(windowWithLargestSublayer, win);
                    }
                }
            }
        }

        private void addWindowToListInOrderLocked(WindowState win, boolean addToToken) {
            if (this.mActivityResource == null) {
                this.mActivityResource = HwFrameworkFactory.getHwResource(36);
            }
            if (!(this.mActivityResource == null || win.mAttrs.packageName == null)) {
                if (Log.HWINFO) {
                    Slog.d(TAG, "ACTIVITY check resid: " + win.mAttrs.packageName + ", size=" + win.mToken.windows.size());
                }
                this.mActivityResource.acquire(win.mOwnerUid, win.mAttrs.packageName, -1, win.mToken.windows.size());
            }
            if (win.mAttachedWindow == null) {
                WindowToken token = win.mToken;
                int tokenWindowsPos = WINDOW_ANIMATION_SCALE;
                if (token.appWindowToken != null) {
                    tokenWindowsPos = addAppWindowToListLocked(win);
                } else {
                    addFreeWindowToListLocked(win);
                }
                if (addToToken) {
                    token.windows.add(tokenWindowsPos, win);
                }
            } else {
                addAttachedWindowToListLocked(win, addToToken);
            }
            AppWindowToken appToken = win.mAppToken;
            if (appToken != null && addToToken) {
                appToken.addWindow(win);
            }
            if (!this.mRebuildAppWinsOnBoot || !this.mFirstStartHome || win.mLastTitle == null) {
                return;
            }
            if (win.mLastTitle.toString().contains("com.huawei.android.launcher.drawer.DrawerLauncher") || win.mLastTitle.toString().contains("com.huawei.android.launcher.unihome.UniHomeLauncher")) {
                if (Log.HWINFO) {
                    Slog.i("WindowManager", "addWindowToListInOrderLocked rebuild app window");
                }
                this.mFirstStartHome = PROFILE_ORIENTATION;
                rebuildAppWindowListLocked();
            }
        }

        static boolean canBeImeTarget(WindowState w) {
            int fl = w.mAttrs.flags & 131080;
            int type = w.mAttrs.type;
            if (fl == 0 || fl == 131080 || type == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                return w.isVisibleOrAdding();
            }
            return PROFILE_ORIENTATION;
        }

        int findDesiredInputMethodWindowIndexLocked(boolean willMove) {
            WindowState curTarget;
            WindowList curWindows;
            WindowState dockedDivider;
            int dividerIndex;
            WindowList windows = getDefaultWindowListLocked();
            WindowState windowState = null;
            int i = windows.size() - 1;
            while (i >= 0) {
                WindowState win = (WindowState) windows.get(i);
                AppWindowToken token;
                WindowState windowState2;
                int highestPos;
                int pos;
                if (canBeImeTarget(win)) {
                    windowState = win;
                    if (!willMove && win.mAttrs.type == UPDATE_FOCUS_WILL_PLACE_SURFACES && i > 0) {
                        WindowState wb = (WindowState) windows.get(i - 1);
                        if (wb.mAppToken == win.mAppToken && canBeImeTarget(wb)) {
                            i--;
                            windowState = wb;
                        }
                    }
                    curTarget = this.mInputMethodTarget;
                    if (curTarget == null && curTarget.isDisplayedLw() && curTarget.isClosing() && (windowState == null || curTarget.mWinAnimator.mAnimLayer > windowState.mWinAnimator.mAnimLayer)) {
                        return windows.indexOf(curTarget) + WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                    if (willMove && windowState != null) {
                        token = curTarget != null ? null : curTarget.mAppToken;
                        if (token != null) {
                            windowState2 = null;
                            highestPos = WINDOW_ANIMATION_SCALE;
                            if (token.mAppAnimator.animating || token.mAppAnimator.animation != null) {
                                curWindows = curTarget.getWindowList();
                                for (pos = curWindows.indexOf(curTarget); pos >= 0; pos--) {
                                    win = (WindowState) curWindows.get(pos);
                                    if (win.mAppToken == token) {
                                        break;
                                    }
                                    if (!win.mRemoved && (r5 == null || win.mWinAnimator.mAnimLayer > r5.mWinAnimator.mAnimLayer)) {
                                        windowState2 = win;
                                        highestPos = pos;
                                    }
                                }
                            }
                            if (windowState2 != null) {
                                if (this.mAppTransition.isTransitionSet()) {
                                    this.mInputMethodTargetWaitingAnim = SCREENSHOT_FORCE_565;
                                    this.mInputMethodTarget = windowState2;
                                    return highestPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                                } else if (windowState2.mWinAnimator.isAnimationSet() && windowState2.mWinAnimator.mAnimLayer > windowState.mWinAnimator.mAnimLayer) {
                                    this.mInputMethodTargetWaitingAnim = SCREENSHOT_FORCE_565;
                                    this.mInputMethodTarget = windowState2;
                                    return highestPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                                }
                            }
                        }
                    }
                    if (windowState == null) {
                        if (willMove) {
                            this.mInputMethodTarget = windowState;
                            this.mInputMethodTargetWaitingAnim = PROFILE_ORIENTATION;
                            if (windowState.mAppToken == null) {
                                this.mLayersController.setInputMethodAnimLayerAdjustment(windowState.mAppToken.mAppAnimator.animLayerAdjustment);
                            } else {
                                this.mLayersController.setInputMethodAnimLayerAdjustment(WINDOW_ANIMATION_SCALE);
                            }
                        }
                        dockedDivider = windowState.mDisplayContent.mDividerControllerLocked.getWindow();
                        if (dockedDivider != null && dockedDivider.isVisibleLw()) {
                            dividerIndex = windows.indexOf(dockedDivider);
                            if (dividerIndex > 0 && dividerIndex > i) {
                                return dividerIndex + WINDOWS_FREEZING_SCREENS_ACTIVE;
                            }
                        }
                        return i + WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                    if (willMove) {
                        this.mInputMethodTarget = null;
                        this.mLayersController.setInputMethodAnimLayerAdjustment(WINDOW_ANIMATION_SCALE);
                    }
                    return -1;
                }
                i--;
            }
            curTarget = this.mInputMethodTarget;
            if (curTarget == null) {
            }
            if (curTarget != null) {
            }
            if (token != null) {
                windowState2 = null;
                highestPos = WINDOW_ANIMATION_SCALE;
                curWindows = curTarget.getWindowList();
                while (pos >= 0) {
                    win = (WindowState) curWindows.get(pos);
                    if (win.mAppToken == token) {
                        windowState2 = win;
                        highestPos = pos;
                    } else {
                        break;
                        if (windowState2 != null) {
                            if (this.mAppTransition.isTransitionSet()) {
                                this.mInputMethodTargetWaitingAnim = SCREENSHOT_FORCE_565;
                                this.mInputMethodTarget = windowState2;
                                return highestPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                            }
                            this.mInputMethodTargetWaitingAnim = SCREENSHOT_FORCE_565;
                            this.mInputMethodTarget = windowState2;
                            return highestPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                        }
                    }
                }
                if (windowState2 != null) {
                    if (this.mAppTransition.isTransitionSet()) {
                        this.mInputMethodTargetWaitingAnim = SCREENSHOT_FORCE_565;
                        this.mInputMethodTarget = windowState2;
                        return highestPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                    this.mInputMethodTargetWaitingAnim = SCREENSHOT_FORCE_565;
                    this.mInputMethodTarget = windowState2;
                    return highestPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                }
            }
            if (windowState == null) {
                if (willMove) {
                    this.mInputMethodTarget = null;
                    this.mLayersController.setInputMethodAnimLayerAdjustment(WINDOW_ANIMATION_SCALE);
                }
                return -1;
            }
            if (willMove) {
                this.mInputMethodTarget = windowState;
                this.mInputMethodTargetWaitingAnim = PROFILE_ORIENTATION;
                if (windowState.mAppToken == null) {
                    this.mLayersController.setInputMethodAnimLayerAdjustment(WINDOW_ANIMATION_SCALE);
                } else {
                    this.mLayersController.setInputMethodAnimLayerAdjustment(windowState.mAppToken.mAppAnimator.animLayerAdjustment);
                }
            }
            dockedDivider = windowState.mDisplayContent.mDividerControllerLocked.getWindow();
            dividerIndex = windows.indexOf(dockedDivider);
            return dividerIndex + WINDOWS_FREEZING_SCREENS_ACTIVE;
        }

        void addInputMethodWindowToListLocked(WindowState win) {
            int pos = findDesiredInputMethodWindowIndexLocked(SCREENSHOT_FORCE_565);
            if (pos >= 0) {
                win.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                getDefaultWindowListLocked().add(pos, win);
                this.mWindowsChanged = SCREENSHOT_FORCE_565;
                moveInputMethodDialogsLocked(pos + WINDOWS_FREEZING_SCREENS_ACTIVE);
                return;
            }
            win.mTargetAppToken = null;
            addWindowToListInOrderLocked(win, SCREENSHOT_FORCE_565);
            moveInputMethodDialogsLocked(pos);
        }

        private int tmpRemoveWindowLocked(int interestingPos, WindowState win) {
            WindowList windows = win.getWindowList();
            int wpos = windows.indexOf(win);
            if (wpos >= 0) {
                if (wpos < interestingPos) {
                    interestingPos--;
                }
                windows.remove(wpos);
                this.mWindowsChanged = SCREENSHOT_FORCE_565;
                int NC = win.mChildWindows.size();
                while (NC > 0) {
                    NC--;
                    int cpos = windows.indexOf((WindowState) win.mChildWindows.get(NC));
                    if (cpos >= 0) {
                        if (cpos < interestingPos) {
                            interestingPos--;
                        }
                        windows.remove(cpos);
                    }
                }
            }
            return interestingPos;
        }

        private void reAddWindowToListInOrderLocked(WindowState win) {
            addWindowToListInOrderLocked(win, PROFILE_ORIENTATION);
            WindowList windows = win.getWindowList();
            int wpos = windows.indexOf(win);
            if (wpos >= 0) {
                windows.remove(wpos);
                this.mWindowsChanged = SCREENSHOT_FORCE_565;
                reAddWindowLocked(wpos, win);
            }
        }

        void logWindowList(WindowList windows, String prefix) {
            int N = windows.size();
            while (N > 0) {
                N--;
                Slog.v("WindowManager", prefix + "#" + N + ": " + windows.get(N));
            }
        }

        void moveInputMethodDialogsLocked(int pos) {
            int i;
            ArrayList<WindowState> dialogs = this.mInputMethodDialogs;
            WindowList windows = getDefaultWindowListLocked();
            int N = dialogs.size();
            for (i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                pos = tmpRemoveWindowLocked(pos, (WindowState) dialogs.get(i));
            }
            if (pos >= 0) {
                AppWindowToken targetAppToken = this.mInputMethodTarget.mAppToken;
                if (this.mInputMethodWindow != null) {
                    while (pos < windows.size()) {
                        WindowState wp = (WindowState) windows.get(pos);
                        if (wp != this.mInputMethodWindow && wp.mAttachedWindow != this.mInputMethodWindow) {
                            break;
                        }
                        pos += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                }
                for (i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState win = (WindowState) dialogs.get(i);
                    win.mTargetAppToken = targetAppToken;
                    pos = reAddWindowLocked(pos, win);
                }
                return;
            }
            for (i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                win = (WindowState) dialogs.get(i);
                win.mTargetAppToken = null;
                reAddWindowToListInOrderLocked(win);
            }
        }

        boolean moveInputMethodWindowsIfNeededLocked(boolean needAssignLayers) {
            WindowState imWin = this.mInputMethodWindow;
            int DN = this.mInputMethodDialogs.size();
            if (imWin == null && DN == 0) {
                return PROFILE_ORIENTATION;
            }
            WindowList windows = getDefaultWindowListLocked();
            int imPos = findDesiredInputMethodWindowIndexLocked(SCREENSHOT_FORCE_565);
            if (imPos >= 0) {
                WindowState baseImWin;
                int N = windows.size();
                WindowState windowState = imPos < N ? (WindowState) windows.get(imPos) : null;
                if (imWin != null) {
                    baseImWin = imWin;
                } else {
                    baseImWin = (WindowState) this.mInputMethodDialogs.get(WINDOW_ANIMATION_SCALE);
                }
                if (baseImWin.mChildWindows.size() > 0) {
                    WindowState cw = (WindowState) baseImWin.mChildWindows.get(WINDOW_ANIMATION_SCALE);
                    if (cw.mSubLayer < 0) {
                        baseImWin = cw;
                    }
                }
                if (windowState == baseImWin) {
                    int pos = imPos + WINDOWS_FREEZING_SCREENS_ACTIVE;
                    while (pos < N && ((WindowState) windows.get(pos)).mIsImWindow) {
                        pos += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                    pos += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    while (pos < N && !((WindowState) windows.get(pos)).mIsImWindow) {
                        pos += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                    if (pos >= N) {
                        if (imWin != null) {
                            imWin.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                        }
                        return PROFILE_ORIENTATION;
                    }
                }
                if (imWin != null) {
                    imPos = tmpRemoveWindowLocked(imPos, imWin);
                    imWin.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                    reAddWindowLocked(imPos, imWin);
                    if (DN > 0) {
                        moveInputMethodDialogsLocked(imPos + WINDOWS_FREEZING_SCREENS_ACTIVE);
                    }
                } else {
                    moveInputMethodDialogsLocked(imPos);
                }
            } else if (imWin != null) {
                tmpRemoveWindowLocked(WINDOW_ANIMATION_SCALE, imWin);
                imWin.mTargetAppToken = null;
                reAddWindowToListInOrderLocked(imWin);
                if (DN > 0) {
                    moveInputMethodDialogsLocked(-1);
                }
            } else {
                moveInputMethodDialogsLocked(-1);
            }
            if (needAssignLayers) {
                this.mLayersController.assignLayersLocked(windows);
            }
            return SCREENSHOT_FORCE_565;
        }

        private static boolean excludeWindowTypeFromTapOutTask(int windowType) {
            switch (windowType) {
                case WINDOW_REPLACEMENT_TIMEOUT_DURATION /*2000*/:
                case 2012:
                case 2019:
                    return SCREENSHOT_FORCE_565;
                default:
                    return PROFILE_ORIENTATION;
            }
        }

        private static boolean excludeWindowsFromTapOutTask(WindowState win) {
            boolean z = PROFILE_ORIENTATION;
            LayoutParams attrs = null;
            if (win != null) {
                attrs = win.getAttrs();
            }
            if (attrs == null) {
                return PROFILE_ORIENTATION;
            }
            if (attrs.type == TYPE_LAYER_OFFSET) {
                z = "com.baidu.input_huawei".equals(attrs.packageName);
            }
            return z;
        }

        public int addWindow(Session session, IWindow client, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
            int[] appOp = new int[WINDOWS_FREEZING_SCREENS_ACTIVE];
            int res = this.mPolicy.checkAddPermission(attrs, appOp);
            if (res != 0) {
                return res;
            }
            int forceCompatMode;
            boolean reportNewConfig = PROFILE_ORIENTATION;
            WindowState windowState = null;
            int type = attrs.type;
            if (attrs.type >= TYPE_LAYER_OFFSET && attrs.type <= 1999) {
                forceCompatMode = COMPAT_MODE_MATCH_PARENT;
            } else if (attrs.packageName == null) {
                forceCompatMode = COMPAT_MODE_MATCH_PARENT;
            } else if ((attrs.privateFlags & DumpState.DUMP_PACKAGES) != 0) {
                forceCompatMode = WINDOWS_FREEZING_SCREENS_ACTIVE;
            } else {
                forceCompatMode = WINDOW_ANIMATION_SCALE;
            }
            synchronized (this.mWindowMap) {
                if (this.mDisplayReady) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to add window to a display that does not exist: " + displayId + ".  Aborting.");
                        return -9;
                    } else if (!displayContent.hasAccess(session.mUid)) {
                        Slog.w("WindowManager", "Attempted to add window to a display for which the application does not have access: " + displayId + ".  Aborting.");
                        return -9;
                    } else if (this.mWindowMap.containsKey(client.asBinder())) {
                        Slog.w("WindowManager", "Window " + client + " is already added");
                        return -5;
                    } else {
                        if (type >= TYPE_LAYER_OFFSET && type <= 1999) {
                            windowState = windowForClientLocked(null, attrs.token, (boolean) PROFILE_ORIENTATION);
                            if (windowState == null) {
                                Slog.w("WindowManager", "Attempted to add window with token that is not a window: " + attrs.token + ".  Aborting.");
                                return -2;
                            } else if (windowState.mAttrs.type >= TYPE_LAYER_OFFSET && windowState.mAttrs.type <= 1999) {
                                Slog.w("WindowManager", "Attempted to add window with token that is a sub-window: " + attrs.token + ".  Aborting.");
                                return -2;
                            }
                        }
                        if (type == 2030) {
                            if (!displayContent.isPrivate()) {
                                Slog.w("WindowManager", "Attempted to add private presentation window to a non-private display.  Aborting.");
                                return -8;
                            }
                        }
                        boolean addToken = PROFILE_ORIENTATION;
                        WindowToken token = (WindowToken) this.mTokenMap.get(attrs.token);
                        AppWindowToken atoken = null;
                        if (token == null) {
                            if (type < WINDOWS_FREEZING_SCREENS_ACTIVE || type > 99) {
                                if (type == 2011) {
                                    Slog.w("WindowManager", "Attempted to add input method window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                } else if (type == 2031) {
                                    Slog.w("WindowManager", "Attempted to add voice interaction window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                } else if (type == 2013) {
                                    Slog.w("WindowManager", "Attempted to add wallpaper window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                } else if (type == 2023) {
                                    Slog.w("WindowManager", "Attempted to add Dream window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                } else if (type == 2035) {
                                    Slog.w("WindowManager", "Attempted to add QS dialog window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                } else if (type == 2032) {
                                    Slog.w("WindowManager", "Attempted to add Accessibility overlay window with unknown token " + attrs.token + ".  Aborting.");
                                    return -1;
                                } else {
                                    token = new WindowToken(this, attrs.token, -1, PROFILE_ORIENTATION);
                                    addToken = SCREENSHOT_FORCE_565;
                                }
                            } else {
                                Slog.w("WindowManager", "Attempted to add application window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type >= WINDOWS_FREEZING_SCREENS_ACTIVE && type <= 99) {
                            atoken = token.appWindowToken;
                            if (atoken == null) {
                                Slog.w("WindowManager", "Attempted to add window with non-application token " + token + ".  Aborting.");
                                return COMPAT_MODE_MATCH_PARENT;
                            } else if (atoken.removed) {
                                Slog.w("WindowManager", "Attempted to add window with exiting application token " + token + ".  Aborting.");
                                return -4;
                            } else if (type == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                if (atoken.firstWindowDrawn) {
                                    return -6;
                                }
                            }
                        } else if (type == 2011) {
                            if (token.windowType != 2011) {
                                Slog.w("WindowManager", "Attempted to add input method window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2031) {
                            if (token.windowType != 2031) {
                                Slog.w("WindowManager", "Attempted to add voice interaction window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2013) {
                            if (token.windowType != 2013) {
                                Slog.w("WindowManager", "Attempted to add wallpaper window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2023) {
                            if (token.windowType != 2023) {
                                Slog.w("WindowManager", "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2032) {
                            if (token.windowType != 2032) {
                                Slog.w("WindowManager", "Attempted to add Accessibility overlay window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2035) {
                            if (token.windowType != 2035) {
                                Slog.w("WindowManager", "Attempted to add QS dialog window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (token.appWindowToken != null) {
                            Slog.w("WindowManager", "Non-null appWindowToken for system window of type=" + type);
                            attrs.token = null;
                            token = new WindowToken(this, null, -1, PROFILE_ORIENTATION);
                            addToken = SCREENSHOT_FORCE_565;
                        } else if (!(this.mCust == null || !this.mCust.isChargingAlbumType(type) || this.mCust.isChargingAlbumType(token.windowType))) {
                            Slog.w(TAG, "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                        WindowState win = new WindowState(this, session, client, token, windowState, appOp[WINDOW_ANIMATION_SCALE], seq, attrs, viewVisibility, displayContent, forceCompatMode);
                        if (win.mDeathRecipient == null) {
                            Slog.w("WindowManager", "Adding window client " + client.asBinder() + " that is dead, aborting.");
                            return -4;
                        } else if (win.getDisplayContent() == null) {
                            Slog.w("WindowManager", "Adding window to Display that has been removed.");
                            return -9;
                        } else {
                            this.mPolicy.adjustWindowParamsLw(win.mAttrs);
                            win.setShowToOwnerOnlyLocked(this.mPolicy.checkShowToOwnerOnly(attrs));
                            res = this.mPolicy.prepareAddWindowLw(win, attrs);
                            if (res != 0) {
                                return res;
                            }
                            boolean openInputChannels = outInputChannel != null ? (attrs.inputFeatures & WINDOWS_FREEZING_SCREENS_TIMEOUT) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : PROFILE_ORIENTATION;
                            if (openInputChannels) {
                                win.openInputChannel(outInputChannel);
                            }
                            res = WINDOW_ANIMATION_SCALE;
                            if (excludeWindowTypeFromTapOutTask(type) || excludeWindowsFromTapOutTask(win)) {
                                displayContent.mTapExcludedWindows.add(win);
                            }
                            long origId = Binder.clearCallingIdentity();
                            if (addToken) {
                                this.mTokenMap.put(attrs.token, token);
                            }
                            win.attach();
                            this.mWindowMap.put(client.asBinder(), win);
                            if (win.mAppOp != -1) {
                                int startOpResult = this.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                                if (!(startOpResult == 0 || startOpResult == UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
                                    setAppOpHideHook(win, PROFILE_ORIENTATION);
                                }
                                addWindowReport(win, startOpResult);
                            }
                            setVisibleFromParent(win);
                            if (type == UPDATE_FOCUS_WILL_PLACE_SURFACES && token.appWindowToken != null) {
                                token.appWindowToken.startingWindow = win;
                            }
                            boolean imMayMove = SCREENSHOT_FORCE_565;
                            if (type == 2011) {
                                win.mGivenInsetsPending = SCREENSHOT_FORCE_565;
                                this.mInputMethodWindow = win;
                                addInputMethodWindowToListLocked(win);
                                imMayMove = PROFILE_ORIENTATION;
                            } else if (type == 2012) {
                                this.mInputMethodDialogs.add(win);
                                addWindowToListInOrderLocked(win, SCREENSHOT_FORCE_565);
                                moveInputMethodDialogsLocked(findDesiredInputMethodWindowIndexLocked(SCREENSHOT_FORCE_565));
                                imMayMove = PROFILE_ORIENTATION;
                            } else {
                                addWindowToListInOrderLocked(win, SCREENSHOT_FORCE_565);
                                if (type == 2013) {
                                    this.mWallpaperControllerLocked.clearLastWallpaperTimeoutTime();
                                    displayContent.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                                } else if ((attrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                                    displayContent.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                                } else if (this.mWallpaperControllerLocked.isBelowWallpaperTarget(win)) {
                                    displayContent.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                                }
                            }
                            win.applyScrollIfNeeded();
                            win.applyAdjustForImeIfNeeded();
                            if (type == 2034) {
                                getDefaultDisplayContentLocked().getDockedDividerController().setWindow(win);
                            }
                            WindowStateAnimator winAnimator = win.mWinAnimator;
                            winAnimator.mEnterAnimationPending = SCREENSHOT_FORCE_565;
                            winAnimator.mEnteringAnimation = SCREENSHOT_FORCE_565;
                            if (!(atoken == null || prepareWindowReplacementTransition(atoken))) {
                                prepareNoneTransitionForRelaunching(atoken);
                            }
                            if (displayContent.isDefaultDisplay) {
                                Rect taskBounds;
                                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                                if (atoken == null || atoken.mTask == null) {
                                    taskBounds = null;
                                } else {
                                    taskBounds = this.mTmpRect;
                                    atoken.mTask.getBounds(this.mTmpRect);
                                }
                                if (this.mPolicy.getInsetHintLw(win.mAttrs, taskBounds, this.mRotation, displayInfo.logicalWidth, displayInfo.logicalHeight, outContentInsets, outStableInsets, outOutsets)) {
                                    res = LAYOUT_REPEAT_THRESHOLD;
                                }
                            } else {
                                outContentInsets.setEmpty();
                                outStableInsets.setEmpty();
                            }
                            if (this.mInTouchMode) {
                                res |= WINDOWS_FREEZING_SCREENS_ACTIVE;
                            }
                            if (win.mAppToken == null || !win.mAppToken.clientHidden) {
                                res |= WINDOWS_FREEZING_SCREENS_TIMEOUT;
                            }
                            this.mInputMonitor.setUpdateInputWindowsNeededLw();
                            boolean focusChanged = PROFILE_ORIENTATION;
                            if (win.canReceiveKeys()) {
                                focusChanged = updateFocusedWindowLocked(WINDOWS_FREEZING_SCREENS_ACTIVE, PROFILE_ORIENTATION);
                                if (focusChanged) {
                                    imMayMove = PROFILE_ORIENTATION;
                                }
                            } else if (win.getAttrs().type == WINDOWS_FREEZING_SCREENS_ACTIVE) {
                                this.mPolicy.updateSystemUiColorLw(win);
                            }
                            if (imMayMove) {
                                moveInputMethodWindowsIfNeededLocked(PROFILE_ORIENTATION);
                            }
                            this.mLayersController.assignLayersLocked(displayContent.getWindowList());
                            if (focusChanged) {
                                this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, PROFILE_ORIENTATION);
                            }
                            this.mInputMonitor.updateInputWindowsLw(PROFILE_ORIENTATION);
                            if (win.isVisibleOrAdding() && updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION)) {
                                reportNewConfig = SCREENSHOT_FORCE_565;
                            }
                            if (attrs.removeTimeoutMilliseconds > 0) {
                                this.mH.sendMessageDelayed(this.mH.obtainMessage(52, win), attrs.removeTimeoutMilliseconds);
                            }
                            if (reportNewConfig) {
                                sendNewConfiguration();
                            }
                            Binder.restoreCallingIdentity(origId);
                            return res;
                        }
                    }
                }
                throw new IllegalStateException("Display has not been initialialized");
            }
        }

        private boolean prepareWindowReplacementTransition(AppWindowToken atoken) {
            atoken.clearAllDrawn();
            WindowState replacedWindow = null;
            for (int i = atoken.windows.size() - 1; i >= 0 && replacedWindow == null; i--) {
                WindowState candidate = (WindowState) atoken.windows.get(i);
                if (candidate.mAnimatingExit && candidate.mWillReplaceWindow && candidate.mAnimateReplacingWindow) {
                    replacedWindow = candidate;
                }
            }
            if (replacedWindow == null) {
                return PROFILE_ORIENTATION;
            }
            Rect frame = replacedWindow.mVisibleFrame;
            this.mOpeningApps.add(atoken);
            prepareAppTransition(18, SCREENSHOT_FORCE_565);
            this.mAppTransition.overridePendingAppTransitionClipReveal(frame.left, frame.top, frame.width(), frame.height());
            executeAppTransition();
            return SCREENSHOT_FORCE_565;
        }

        private void prepareNoneTransitionForRelaunching(AppWindowToken atoken) {
            if (this.mDisplayFrozen && !this.mOpeningApps.contains(atoken) && atoken.isRelaunching()) {
                this.mOpeningApps.add(atoken);
                prepareAppTransition(WINDOW_ANIMATION_SCALE, PROFILE_ORIENTATION);
                executeAppTransition();
            }
        }

        boolean isScreenCaptureDisabledLocked(int userId) {
            Boolean disabled = (Boolean) this.mScreenCaptureDisabled.get(userId);
            if (disabled == null) {
                return PROFILE_ORIENTATION;
            }
            return disabled.booleanValue();
        }

        boolean isSecureLocked(WindowState w) {
            return ((w.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) == 0 && !isScreenCaptureDisabledLocked(UserHandle.getUserId(w.mOwnerUid))) ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
        }

        public void setScreenCaptureDisabled(int userId, boolean disabled) {
            if (Binder.getCallingUid() != TYPE_LAYER_OFFSET) {
                throw new SecurityException("Only system can call setScreenCaptureDisabled.");
            }
            synchronized (this.mWindowMap) {
                this.mScreenCaptureDisabled.put(userId, Boolean.valueOf(disabled));
                for (int displayNdx = this.mDisplayContents.size() - 1; displayNdx >= 0; displayNdx--) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                        WindowState win = (WindowState) windows.get(winNdx);
                        if (win.mHasSurface && userId == UserHandle.getUserId(win.mOwnerUid)) {
                            win.mWinAnimator.setSecureLocked(disabled);
                        }
                    }
                }
            }
        }

        private void setupWindowForRemoveOnExit(WindowState win) {
            win.mRemoveOnExit = SCREENSHOT_FORCE_565;
            win.setDisplayLayoutNeeded();
            boolean focusChanged = updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES, PROFILE_ORIENTATION);
            this.mWindowPlacerLocked.performSurfacePlacement();
            if (focusChanged) {
                this.mInputMonitor.updateInputWindowsLw(PROFILE_ORIENTATION);
            }
        }

        public void removeWindow(Session session, IWindow client) {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                if (win == null) {
                    return;
                }
                removeWindowLocked(win);
            }
        }

        void removeWindowLocked(WindowState win) {
            removeWindowLocked(win, PROFILE_ORIENTATION);
        }

        void removeWindowLocked(WindowState win, boolean keepVisibleDeadWindow) {
            long origId;
            boolean z;
            win.mWindowRemovalAllowed = SCREENSHOT_FORCE_565;
            boolean startingWindow = win.mAttrs.type == UPDATE_FOCUS_WILL_PLACE_SURFACES ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            if (startingWindow) {
                origId = Binder.clearCallingIdentity();
                win.disposeInputChannel();
                z = PROFILE_ORIENTATION;
            } else {
                origId = Binder.clearCallingIdentity();
                win.disposeInputChannel();
                z = PROFILE_ORIENTATION;
            }
            if (win.mHasSurface && okToDisplay()) {
                AppWindowToken appToken = win.mAppToken;
                if (win.mWillReplaceWindow) {
                    win.mAnimatingExit = SCREENSHOT_FORCE_565;
                    win.mReplacingRemoveRequested = SCREENSHOT_FORCE_565;
                    Binder.restoreCallingIdentity(origId);
                    return;
                } else if (!win.isAnimatingWithSavedSurface() || appToken.allDrawnExcludingSaved) {
                    z = win.isWinVisibleLw();
                    if (keepVisibleDeadWindow) {
                        win.mAppDied = SCREENSHOT_FORCE_565;
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        win.openInputChannel(null);
                        this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    WindowStateAnimator winAnimator = win.mWinAnimator;
                    if (z) {
                        int transit = !startingWindow ? WINDOWS_FREEZING_SCREENS_TIMEOUT : WINDOW_LAYER_MULTIPLIER;
                        if (winAnimator.applyAnimationLocked(transit, PROFILE_ORIENTATION)) {
                            win.mAnimatingExit = SCREENSHOT_FORCE_565;
                        }
                        if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                            this.mAccessibilityController.onWindowTransitionLocked(win, transit);
                        }
                    }
                    boolean isAnimating = (!winAnimator.isAnimationSet() || winAnimator.isDummyAnimation()) ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
                    boolean lastWindowIsStartingWindow = (!startingWindow || appToken == null) ? PROFILE_ORIENTATION : appToken.allAppWindows.size() == WINDOWS_FREEZING_SCREENS_ACTIVE ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                    if (winAnimator.getShown() && win.mAnimatingExit && (!lastWindowIsStartingWindow || isAnimating)) {
                        setupWindowForRemoveOnExit(win);
                        if (appToken != null) {
                            appToken.updateReportedVisibilityLocked();
                        }
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                } else {
                    setupWindowForRemoveOnExit(win);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            removeWindowInnerLocked(win);
            if (z && updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION)) {
                this.mH.sendEmptyMessage(18);
            }
            updateFocusedWindowLocked(WINDOW_ANIMATION_SCALE, SCREENSHOT_FORCE_565);
            Binder.restoreCallingIdentity(origId);
        }

        void removeWindowInnerLocked(WindowState win) {
            removeWindowInnerLocked(win, SCREENSHOT_FORCE_565);
        }

        void removeWindowInnerLocked(WindowState win, boolean performLayout) {
            if (!win.mRemoved) {
                for (int i = win.mChildWindows.size() - 1; i >= 0; i--) {
                    WindowState cwin = (WindowState) win.mChildWindows.get(i);
                    Slog.w("WindowManager", "Force-removing child win " + cwin + " from container " + win);
                    removeWindowInnerLocked(cwin);
                }
                win.mRemoved = SCREENSHOT_FORCE_565;
                if (this.mInputMethodTarget == win) {
                    moveInputMethodWindowsIfNeededLocked(PROFILE_ORIENTATION);
                }
                int type = win.mAttrs.type;
                if (excludeWindowTypeFromTapOutTask(type) || excludeWindowsFromTapOutTask(win)) {
                    win.getDisplayContent().mTapExcludedWindows.remove(win);
                }
                this.mPolicy.removeWindowLw(win);
                win.removeLocked();
                this.mWindowMap.remove(win.mClient.asBinder());
                if (win.mAppOp != -1) {
                    this.mAppOps.finishOp(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                    removeWindowReport(win);
                }
                this.mPendingRemove.remove(win);
                this.mResizingWindows.remove(win);
                this.mWindowsChanged = SCREENSHOT_FORCE_565;
                if (this.mInputMethodWindow == win) {
                    this.mInputMethodWindow = null;
                } else if (win.mAttrs.type == 2012) {
                    this.mInputMethodDialogs.remove(win);
                }
                WindowToken token = win.mToken;
                AppWindowToken atoken = win.mAppToken;
                token.windows.remove(win);
                if (atoken != null) {
                    atoken.allAppWindows.remove(win);
                }
                if (token.windows.size() == 0) {
                    if (!token.explicit) {
                        this.mTokenMap.remove(token.token);
                    } else if (atoken != null) {
                        atoken.firstWindowDrawn = PROFILE_ORIENTATION;
                        atoken.clearAllDrawn();
                    }
                }
                if (atoken != null) {
                    if (atoken.startingWindow == win) {
                        scheduleRemoveStartingWindowLocked(atoken);
                    } else if (atoken.allAppWindows.size() == 0 && atoken.startingData != null) {
                        atoken.startingData = null;
                    } else if (atoken.allAppWindows.size() == WINDOWS_FREEZING_SCREENS_ACTIVE && atoken.startingView != null) {
                        scheduleRemoveStartingWindowLocked(atoken);
                    }
                }
                DisplayContent defaultDisplayContentLocked;
                if (type == 2013) {
                    this.mWallpaperControllerLocked.clearLastWallpaperTimeoutTime();
                    defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                    defaultDisplayContentLocked.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                } else if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                    defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                    defaultDisplayContentLocked.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                }
                WindowList windows = win.getWindowList();
                if (windows != null) {
                    windows.remove(win);
                    if (!this.mWindowPlacerLocked.isInLayout()) {
                        this.mLayersController.assignLayersLocked(windows);
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        if (win.mAppToken != null) {
                            win.mAppToken.updateReportedVisibilityLocked();
                        }
                    }
                }
                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
            }
        }

        public void updateAppOpsState() {
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = WINDOW_ANIMATION_SCALE; winNdx < numWindows; winNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        WindowState win = (WindowState) windows.get(winNdx);
                        if (win.mAppOp != -1) {
                            setAppOpVisibilityLwHook(win, this.mAppOps.checkOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage()));
                        }
                    }
                }
            }
        }

        static void logSurface(WindowState w, String msg, boolean withStackTrace) {
            String str = "  SURFACE " + msg + ": " + w;
            if (withStackTrace) {
                logWithStack(TAG, str);
            } else {
                Slog.i("WindowManager", str);
            }
        }

        static void logSurface(SurfaceControl s, String title, String msg) {
            Slog.i("WindowManager", "  SURFACE " + s + ": " + msg + " / " + title);
        }

        static void logWithStack(String tag, String s) {
            Slog.i(tag, s, null);
        }

        void setTransparentRegionWindow(Session session, IWindow client, Region region) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState w = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                    if (w != null && w.mHasSurface) {
                        w.mWinAnimator.setTransparentRegionHintLocked(region);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        void setInsetsWindow(Session session, IWindow client, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState w = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                    if (w != null) {
                        w.mGivenInsetsPending = PROFILE_ORIENTATION;
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
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void getWindowDisplayFrame(Session session, IWindow client, Rect outDisplayFrame) {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                if (win == null) {
                    outDisplayFrame.setEmpty();
                    return;
                }
                outDisplayFrame.set(win.mDisplayFrame);
            }
        }

        public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
            synchronized (this.mWindowMap) {
                if (this.mAccessibilityController != null) {
                    WindowState window = (WindowState) this.mWindowMap.get(token);
                    if (window != null && window.getDisplayId() == 0) {
                        this.mAccessibilityController.onRectangleOnScreenRequestedLocked(rectangle);
                    }
                }
            }
        }

        public IWindowId getWindowId(IBinder token) {
            IWindowId iWindowId = null;
            synchronized (this.mWindowMap) {
                WindowState window = (WindowState) this.mWindowMap.get(token);
                if (window != null) {
                    iWindowId = window.mWindowId;
                }
            }
            return iWindowId;
        }

        public void pokeDrawLock(Session session, IBinder token) {
            synchronized (this.mWindowMap) {
                WindowState window = windowForClientLocked(session, token, (boolean) PROFILE_ORIENTATION);
                if (window != null) {
                    window.pokeDrawLockLw(this.mDrawLockTimeoutMillis);
                }
            }
        }

        void repositionChild(Session session, IWindow client, int left, int top, int right, int bottom, long frameNumber, Rect outFrame) {
            Trace.traceBegin(32, "repositionChild");
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                    if (win == null) {
                        Binder.restoreCallingIdentity(origId);
                        Trace.traceEnd(32);
                    } else if (win.mAttachedWindow == null) {
                        throw new IllegalArgumentException("repositionChild called but window is notattached to a parent win=" + win);
                    } else {
                        win.mAttrs.x = left;
                        win.mAttrs.y = top;
                        win.mAttrs.width = right - left;
                        win.mAttrs.height = bottom - top;
                        win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                        if (win.mHasSurface) {
                            SurfaceControl.openTransaction();
                            win.applyGravityAndUpdateFrame(win.mContainingFrame, win.mDisplayFrame);
                            win.mWinAnimator.computeShownFrameLocked();
                            win.mWinAnimator.setSurfaceBoundariesLocked(PROFILE_ORIENTATION);
                            if (frameNumber > 0) {
                                win.mWinAnimator.deferTransactionUntilParentFrame(frameNumber);
                            }
                            SurfaceControl.closeTransaction();
                        }
                        outFrame = win.mCompatFrame;
                        Binder.restoreCallingIdentity(origId);
                        Trace.traceEnd(32);
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                Trace.traceEnd(32);
            }
        }

        public int relayoutWindow(Session session, IWindow client, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, Configuration outConfig, Surface outSurface) {
            int result = WINDOW_ANIMATION_SCALE;
            boolean hasStatusBarPermission = this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            long origId = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                if (win == null) {
                    return WINDOW_ANIMATION_SCALE;
                }
                boolean isRogEnable;
                boolean z;
                boolean isDefaultDisplay;
                boolean focusMayChange;
                boolean wallpaperMayMove;
                int oldVisibility;
                boolean configChanged;
                DisplayInfo displayInfo;
                WindowStateAnimator winAnimator = win.mWinAnimator;
                if (viewVisibility != 8) {
                    win.setRequestedSize(requestedWidth, requestedHeight);
                }
                int attrChanges = WINDOW_ANIMATION_SCALE;
                int flagChanges = WINDOW_ANIMATION_SCALE;
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
                    if ((attrs.privateFlags & DumpState.DUMP_PREFERRED_XML) != 0) {
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
                        win.mLayoutNeeded = SCREENSHOT_FORCE_565;
                    }
                }
                winAnimator.mSurfaceDestroyDeferred = (flags & WINDOWS_FREEZING_SCREENS_TIMEOUT) != 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                if (this.mCurrentFocus == win && (UsbAudioDevice.kAudioDeviceMeta_Alsa & attrChanges) != 0) {
                    this.mPolicy.updateSystemUiColorLw(win);
                }
                if ((win.mAttrs.privateFlags & DumpState.DUMP_PACKAGES) == 0) {
                    isRogEnable = win.isRogEnable();
                } else {
                    isRogEnable = SCREENSHOT_FORCE_565;
                }
                win.mEnforceSizeCompat = isRogEnable;
                if ((attrChanges & DumpState.DUMP_PACKAGES) != 0) {
                    winAnimator.mAlpha = attrs.alpha;
                }
                win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                if (win.mAttrs.surfaceInsets.left == 0 && win.mAttrs.surfaceInsets.top == 0) {
                    if (win.mAttrs.surfaceInsets.right == 0) {
                        if (win.mAttrs.surfaceInsets.bottom != 0) {
                        }
                        z = (131080 & flagChanges) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                        isDefaultDisplay = win.isDefaultDisplay();
                        focusMayChange = isDefaultDisplay ? (win.mViewVisibility == viewVisibility || (flagChanges & 8) != 0) ? SCREENSHOT_FORCE_565 : win.mRelayoutCalled ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                        wallpaperMayMove = win.mViewVisibility == viewVisibility ? (win.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : PROFILE_ORIENTATION;
                        wallpaperMayMove |= (DumpState.DUMP_DEXOPT & flagChanges) == 0 ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE;
                        if (!((flagChanges & DumpState.DUMP_PREFERRED_XML) == 0 || winAnimator.mSurfaceController == null)) {
                            winAnimator.mSurfaceController.setSecure(isSecureLocked(win));
                        }
                        win.mRelayoutCalled = SCREENSHOT_FORCE_565;
                        win.mInRelayout = SCREENSHOT_FORCE_565;
                        oldVisibility = win.mViewVisibility;
                        win.mViewVisibility = viewVisibility;
                        if (viewVisibility == 0 || (win.mAppToken != null && win.mAppToken.clientHidden)) {
                            winAnimator.mEnterAnimationPending = PROFILE_ORIENTATION;
                            winAnimator.mEnteringAnimation = PROFILE_ORIENTATION;
                            boolean usingSavedSurfaceBeforeVisible = oldVisibility == 0 ? win.isAnimatingWithSavedSurface() : PROFILE_ORIENTATION;
                            if (!(!winAnimator.hasSurface() || win.mAnimatingExit || usingSavedSurfaceBeforeVisible)) {
                                if (!win.mWillReplaceWindow) {
                                    focusMayChange = tryStartExitingAnimation(win, winAnimator, isDefaultDisplay, focusMayChange);
                                }
                                result = LAYOUT_REPEAT_THRESHOLD;
                            }
                            outSurface.release();
                        } else {
                            try {
                                result = createSurfaceControl(outSurface, relayoutVisibleWindow(outConfig, WINDOW_ANIMATION_SCALE, win, winAnimator, attrChanges, oldVisibility), win, winAnimator);
                                if ((result & WINDOWS_FREEZING_SCREENS_TIMEOUT) != 0) {
                                    focusMayChange = isDefaultDisplay;
                                }
                                if (win.mAttrs.type == 2011 && (this.mInputMethodWindow == null || this.mInputMethodWindow != win)) {
                                    this.mInputMethodWindow = win;
                                    z = SCREENSHOT_FORCE_565;
                                }
                                win.adjustStartingWindowFlags();
                            } catch (Exception e) {
                                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                                Slog.w("WindowManager", "Exception thrown when creating surface for client " + client + " (" + win.mAttrs.getTitle() + ")", e);
                                Binder.restoreCallingIdentity(origId);
                                return WINDOW_ANIMATION_SCALE;
                            }
                        }
                        if (focusMayChange && updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES, PROFILE_ORIENTATION)) {
                            z = PROFILE_ORIENTATION;
                        }
                        boolean toBeDisplayed = (result & WINDOWS_FREEZING_SCREENS_TIMEOUT) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                        if (z && (moveInputMethodWindowsIfNeededLocked(PROFILE_ORIENTATION) || toBeDisplayed)) {
                            this.mLayersController.assignLayersLocked(win.getWindowList());
                        }
                        if (wallpaperMayMove) {
                            DisplayContent defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                            defaultDisplayContentLocked.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                        }
                        win.setDisplayLayoutNeeded();
                        win.mGivenInsetsPending = (flags & WINDOWS_FREEZING_SCREENS_ACTIVE) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                        configChanged = updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION);
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        if (toBeDisplayed && win.mIsWallpaper) {
                            displayInfo = getDefaultDisplayInfoLocked();
                            this.mWallpaperControllerLocked.updateWallpaperOffset(win, displayInfo.logicalWidth, displayInfo.logicalHeight, PROFILE_ORIENTATION);
                        }
                        if (win.mAppToken != null) {
                            win.mAppToken.updateReportedVisibilityLocked();
                        }
                        if (winAnimator.mReportSurfaceResized) {
                            winAnimator.mReportSurfaceResized = PROFILE_ORIENTATION;
                            result |= 32;
                        }
                        if (this.mPolicy.isNavBarForcedShownLw(win)) {
                            result |= 64;
                        }
                        if (!win.isGoneForLayoutLw()) {
                            win.mResizedWhileGone = PROFILE_ORIENTATION;
                        }
                        outFrame.set(win.mCompatFrame);
                        outOverscanInsets.set(win.mOverscanInsets);
                        outContentInsets.set(win.mContentInsets);
                        outVisibleInsets.set(win.mVisibleInsets);
                        outStableInsets.set(win.mStableInsets);
                        outOutsets.set(win.mOutsets);
                        outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                        result |= this.mInTouchMode ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE;
                        this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                        win.mInRelayout = PROFILE_ORIENTATION;
                        if (configChanged) {
                            sendNewConfiguration();
                        }
                        Binder.restoreCallingIdentity(origId);
                        return result;
                    }
                }
                winAnimator.setOpaqueLocked(PROFILE_ORIENTATION);
                if ((131080 & flagChanges) == 0) {
                }
                isDefaultDisplay = win.isDefaultDisplay();
                if (isDefaultDisplay) {
                    if (win.mViewVisibility == viewVisibility) {
                    }
                }
                if (win.mViewVisibility == viewVisibility) {
                }
                if ((DumpState.DUMP_DEXOPT & flagChanges) == 0) {
                }
                wallpaperMayMove |= (DumpState.DUMP_DEXOPT & flagChanges) == 0 ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE;
                winAnimator.mSurfaceController.setSecure(isSecureLocked(win));
                win.mRelayoutCalled = SCREENSHOT_FORCE_565;
                win.mInRelayout = SCREENSHOT_FORCE_565;
                oldVisibility = win.mViewVisibility;
                win.mViewVisibility = viewVisibility;
                if (viewVisibility == 0) {
                }
                winAnimator.mEnterAnimationPending = PROFILE_ORIENTATION;
                winAnimator.mEnteringAnimation = PROFILE_ORIENTATION;
                if (oldVisibility == 0) {
                }
                if (win.mWillReplaceWindow) {
                    focusMayChange = tryStartExitingAnimation(win, winAnimator, isDefaultDisplay, focusMayChange);
                }
                result = LAYOUT_REPEAT_THRESHOLD;
                outSurface.release();
                z = PROFILE_ORIENTATION;
                if ((result & WINDOWS_FREEZING_SCREENS_TIMEOUT) == 0) {
                }
                this.mLayersController.assignLayersLocked(win.getWindowList());
                if (wallpaperMayMove) {
                    DisplayContent defaultDisplayContentLocked2 = getDefaultDisplayContentLocked();
                    defaultDisplayContentLocked2.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                }
                win.setDisplayLayoutNeeded();
                if ((flags & WINDOWS_FREEZING_SCREENS_ACTIVE) == 0) {
                }
                win.mGivenInsetsPending = (flags & WINDOWS_FREEZING_SCREENS_ACTIVE) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                configChanged = updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION);
                this.mWindowPlacerLocked.performSurfacePlacement();
                displayInfo = getDefaultDisplayInfoLocked();
                this.mWallpaperControllerLocked.updateWallpaperOffset(win, displayInfo.logicalWidth, displayInfo.logicalHeight, PROFILE_ORIENTATION);
                if (win.mAppToken != null) {
                    win.mAppToken.updateReportedVisibilityLocked();
                }
                if (winAnimator.mReportSurfaceResized) {
                    winAnimator.mReportSurfaceResized = PROFILE_ORIENTATION;
                    result |= 32;
                }
                if (this.mPolicy.isNavBarForcedShownLw(win)) {
                    result |= 64;
                }
                if (win.isGoneForLayoutLw()) {
                    win.mResizedWhileGone = PROFILE_ORIENTATION;
                }
                outFrame.set(win.mCompatFrame);
                outOverscanInsets.set(win.mOverscanInsets);
                outContentInsets.set(win.mContentInsets);
                outVisibleInsets.set(win.mVisibleInsets);
                outStableInsets.set(win.mStableInsets);
                outOutsets.set(win.mOutsets);
                outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                if (this.mInTouchMode) {
                }
                result |= this.mInTouchMode ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE;
                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                win.mInRelayout = PROFILE_ORIENTATION;
                if (configChanged) {
                    sendNewConfiguration();
                }
                Binder.restoreCallingIdentity(origId);
                return result;
            }
        }

        private boolean tryStartExitingAnimation(WindowState win, WindowStateAnimator winAnimator, boolean isDefaultDisplay, boolean focusMayChange) {
            int transit = WINDOWS_FREEZING_SCREENS_TIMEOUT;
            if (win.mAttrs.type == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                transit = WINDOW_LAYER_MULTIPLIER;
            }
            if (win.isWinVisibleLw() && winAnimator.applyAnimationLocked(transit, PROFILE_ORIENTATION)) {
                focusMayChange = isDefaultDisplay;
                win.mAnimatingExit = SCREENSHOT_FORCE_565;
                win.mWinAnimator.mAnimating = SCREENSHOT_FORCE_565;
            } else if (win.mWinAnimator.isAnimationSet()) {
                win.mAnimatingExit = SCREENSHOT_FORCE_565;
                win.mWinAnimator.mAnimating = SCREENSHOT_FORCE_565;
            } else if (this.mWallpaperControllerLocked.isWallpaperTarget(win)) {
                win.mAnimatingExit = SCREENSHOT_FORCE_565;
                win.mWinAnimator.mAnimating = SCREENSHOT_FORCE_565;
            } else {
                if (this.mInputMethodWindow == win) {
                    this.mInputMethodWindow = null;
                }
                win.destroyOrSaveSurface();
            }
            if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                this.mAccessibilityController.onWindowTransitionLocked(win, transit);
            }
            return focusMayChange;
        }

        private int createSurfaceControl(Surface outSurface, int result, WindowState win, WindowStateAnimator winAnimator) {
            if (!win.mHasSurface) {
                result |= LAYOUT_REPEAT_THRESHOLD;
            }
            WindowSurfaceController surfaceController = winAnimator.createSurfaceLocked();
            if (surfaceController != null) {
                surfaceController.getSurface(outSurface);
            } else {
                outSurface.release();
            }
            return result;
        }

        private int relayoutVisibleWindow(Configuration outConfig, int result, WindowState win, WindowStateAnimator winAnimator, int attrChanges, int oldVisibility) {
            int i;
            int i2 = WINDOW_ANIMATION_SCALE;
            if (win.isVisibleLw()) {
                i = WINDOW_ANIMATION_SCALE;
            } else {
                i = WINDOWS_FREEZING_SCREENS_TIMEOUT;
            }
            result |= i;
            if (win.mAnimatingExit) {
                Slog.d(TAG, "relayoutVisibleWindow: " + win + " mAnimatingExit=true, mRemoveOnExit=" + win.mRemoveOnExit + ", mDestroying=" + win.mDestroying);
                winAnimator.cancelExitAnimationForNextAnimationLocked();
                win.mAnimatingExit = PROFILE_ORIENTATION;
            }
            if (win.mDestroying) {
                win.mDestroying = PROFILE_ORIENTATION;
                this.mDestroySurface.remove(win);
            }
            if (oldVisibility == 8) {
                winAnimator.mEnterAnimationPending = SCREENSHOT_FORCE_565;
            }
            winAnimator.mEnteringAnimation = SCREENSHOT_FORCE_565;
            if ((result & WINDOWS_FREEZING_SCREENS_TIMEOUT) != 0) {
                win.prepareWindowToDisplayDuringRelayout(outConfig);
            } else if (!(this.mPowerManager.isScreenOn() || (win.mAttrs.flags & 2097152) == 0 || win.mOwnerUid != 1001)) {
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
            }
            if (!((attrChanges & 8) == 0 || winAnimator.tryChangeFormatInPlaceLocked())) {
                winAnimator.preserveSurfaceLocked();
                result |= 6;
            }
            if (win.isDragResizeChanged() || win.isResizedWhileNotDragResizing()) {
                win.setDragResizing();
                win.setResizedWhileNotDragResizing(PROFILE_ORIENTATION);
                if (win.mHasSurface && win.mAttachedWindow == null) {
                    winAnimator.preserveSurfaceLocked();
                    result |= WINDOWS_FREEZING_SCREENS_TIMEOUT;
                }
            }
            boolean freeformResizing = win.isDragResizing() ? win.getResizeMode() == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : PROFILE_ORIENTATION;
            boolean dockedResizing = win.isDragResizing() ? win.getResizeMode() == WINDOWS_FREEZING_SCREENS_ACTIVE ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : PROFILE_ORIENTATION;
            if (freeformResizing) {
                i = 16;
            } else {
                i = WINDOW_ANIMATION_SCALE;
            }
            result |= i;
            if (dockedResizing) {
                i2 = 8;
            }
            result |= i2;
            if (win.isAnimatingWithSavedSurface()) {
                return result | WINDOWS_FREEZING_SCREENS_TIMEOUT;
            }
            return result;
        }

        public void performDeferredDestroyWindow(Session session, IWindow client) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                    if (win == null || win.mWillReplaceWindow) {
                        return;
                    }
                    win.mWinAnimator.destroyDeferredSurfaceLocked();
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public boolean outOfMemoryWindow(Session session, IWindow client) {
            boolean z = PROFILE_ORIENTATION;
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                    if (win == null) {
                        return z;
                    }
                    z = "from-client";
                    boolean reclaimSomeSurfaceMemoryLocked = reclaimSomeSurfaceMemoryLocked(win.mWinAnimator, z, PROFILE_ORIENTATION);
                    Binder.restoreCallingIdentity(origId);
                    return reclaimSomeSurfaceMemoryLocked;
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void finishDrawingWindow(Session session, IWindow client) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, (boolean) PROFILE_ORIENTATION);
                    if (win != null && win.mWinAnimator.finishDrawingLocked()) {
                        if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            DisplayContent defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                            defaultDisplayContentLocked.pendingLayoutChanges |= LAYOUT_REPEAT_THRESHOLD;
                        }
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.requestTraversal();
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        private boolean applyAnimationLocked(AppWindowToken atoken, LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
            if (okToDisplay()) {
                DisplayInfo displayInfo = getDefaultDisplayInfoLocked();
                int width = displayInfo.appWidth;
                int height = displayInfo.appHeight;
                WindowState win = atoken.findMainWindow();
                Rect frame = new Rect(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, width, height);
                Rect displayFrame = new Rect(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, displayInfo.logicalWidth, displayInfo.logicalHeight);
                Rect insets = new Rect();
                Rect surfaceInsets = null;
                boolean inFreeformWorkspace = win != null ? win.inFreeformWorkspace() : PROFILE_ORIENTATION;
                if (win != null) {
                    if (inFreeformWorkspace) {
                        frame.set(win.mFrame);
                    } else {
                        frame.set(win.mContainingFrame);
                    }
                    surfaceInsets = win.getAttrs().surfaceInsets;
                    insets.set(win.mContentInsets);
                }
                if (atoken.mLaunchTaskBehind) {
                    enter = PROFILE_ORIENTATION;
                }
                Animation a = this.mAppTransition.loadAnimation(lp, transit, enter, this.mCurConfiguration.uiMode, this.mCurConfiguration.orientation, frame, displayFrame, insets, surfaceInsets, isVoiceInteraction, inFreeformWorkspace, atoken.mTask.mTaskId);
                if (a != null) {
                    atoken.mAppAnimator.setAnimation(a, frame.width(), frame.height(), this.mAppTransition.canSkipFirstFrame(), this.mAppTransition.getAppStackClipMode());
                }
            } else {
                atoken.mAppAnimator.clearAnimation();
            }
            if (atoken.mAppAnimator.animation != null) {
                return SCREENSHOT_FORCE_565;
            }
            return PROFILE_ORIENTATION;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void validateAppTokens(int stackId, List<TaskGroup> tasks) {
            synchronized (this.mWindowMap) {
                int t = tasks.size() - 1;
                if (t < 0) {
                    Slog.w("WindowManager", "validateAppTokens: empty task list");
                    return;
                }
                int taskId = ((TaskGroup) tasks.get(WINDOW_ANIMATION_SCALE)).taskId;
                DisplayContent displayContent = ((Task) this.mTaskIdToTask.get(taskId)).getDisplayContent();
                if (displayContent == null) {
                    Slog.w("WindowManager", "validateAppTokens: no Display for taskId=" + taskId);
                    return;
                }
                ArrayList<Task> localTasks = ((TaskStack) this.mStackIdToStack.get(stackId)).getTasks();
                int taskNdx = localTasks.size() - 1;
                while (taskNdx >= 0 && t >= 0) {
                    AppTokenList localTokens = ((Task) localTasks.get(taskNdx)).mAppTokens;
                    TaskGroup task = (TaskGroup) tasks.get(t);
                    List<IApplicationToken> tokens = task.tokens;
                    DisplayContent lastDisplayContent = displayContent;
                    displayContent = ((Task) this.mTaskIdToTask.get(taskId)).getDisplayContent();
                    if (displayContent != lastDisplayContent) {
                        Slog.w("WindowManager", "validateAppTokens: displayContent changed in TaskGroup list!");
                        return;
                    }
                    int tokenNdx = localTokens.size() - 1;
                    int v = task.tokens.size() - 1;
                    while (tokenNdx >= 0 && v >= 0) {
                        AppWindowToken atoken = (AppWindowToken) localTokens.get(tokenNdx);
                        if (atoken.removed) {
                            tokenNdx--;
                        } else {
                            IBinder iBinder = tokens.get(v);
                            IBinder iBinder2 = atoken.token;
                            if (iBinder != r0) {
                                break;
                            }
                            tokenNdx--;
                            v--;
                        }
                    }
                    if (tokenNdx < 0 && v < 0) {
                        taskNdx--;
                        t--;
                    }
                }
                if (taskNdx >= 0 || t >= 0) {
                    Slog.w("WindowManager", "validateAppTokens: Mismatch! ActivityManager=" + tasks);
                    Slog.w("WindowManager", "validateAppTokens: Mismatch! WindowManager=" + localTasks);
                    Slog.w("WindowManager", "validateAppTokens: Mismatch! Callers=" + Debug.getCallers(LAYOUT_REPEAT_THRESHOLD));
                }
            }
        }

        public void validateStackOrder(Integer[] remoteStackIds) {
        }

        boolean checkCallingPermission(String permission, String func) {
            if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
                return SCREENSHOT_FORCE_565;
            }
            Slog.w("WindowManager", "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
            return PROFILE_ORIENTATION;
        }

        boolean okToDisplay() {
            return (this.mDisplayFrozen || !this.mDisplayEnabled) ? PROFILE_ORIENTATION : this.mPolicy.isScreenOn();
        }

        AppWindowToken findAppWindowToken(IBinder token) {
            WindowToken wtoken = (WindowToken) this.mTokenMap.get(token);
            if (wtoken == null) {
                return null;
            }
            return wtoken.appWindowToken;
        }

        public void addWindowToken(IBinder token, int type) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
                synchronized (this.mWindowMap) {
                    if (((WindowToken) this.mTokenMap.get(token)) != null) {
                        Slog.w("WindowManager", "Attempted to add existing input method token: " + token);
                        return;
                    }
                    WindowToken wtoken = new WindowToken(this, token, type, SCREENSHOT_FORCE_565);
                    this.mTokenMap.put(token, wtoken);
                    if (type == 2013) {
                        this.mWallpaperControllerLocked.addWallpaperToken(wtoken);
                    }
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void removeWindowToken(IBinder token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeWindowToken()")) {
                long origId = Binder.clearCallingIdentity();
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = null;
                    WindowToken wtoken = (WindowToken) this.mTokenMap.remove(token);
                    if (wtoken != null) {
                        boolean delayed = PROFILE_ORIENTATION;
                        if (!wtoken.hidden) {
                            int N = wtoken.windows.size();
                            boolean changed = PROFILE_ORIENTATION;
                            for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                                WindowState win = (WindowState) wtoken.windows.get(i);
                                displayContent = win.getDisplayContent();
                                if (win.mWinAnimator.isAnimationSet()) {
                                    delayed = SCREENSHOT_FORCE_565;
                                }
                                if (win.isVisibleNow()) {
                                    win.mWinAnimator.applyAnimationLocked(WINDOWS_FREEZING_SCREENS_TIMEOUT, PROFILE_ORIENTATION);
                                    if (this.mAccessibilityController != null && win.isDefaultDisplay()) {
                                        this.mAccessibilityController.onWindowTransitionLocked(win, WINDOWS_FREEZING_SCREENS_TIMEOUT);
                                    }
                                    changed = SCREENSHOT_FORCE_565;
                                    if (displayContent != null) {
                                        displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                                    }
                                }
                            }
                            wtoken.hidden = SCREENSHOT_FORCE_565;
                            if (changed) {
                                this.mWindowPlacerLocked.performSurfacePlacement();
                                updateFocusedWindowLocked(WINDOW_ANIMATION_SCALE, PROFILE_ORIENTATION);
                            }
                            if (delayed && displayContent != null) {
                                displayContent.mExitingTokens.add(wtoken);
                            } else if (wtoken.windowType == 2013) {
                                this.mWallpaperControllerLocked.removeWallpaperToken(wtoken);
                            }
                        } else if (wtoken.windowType == 2013) {
                            this.mWallpaperControllerLocked.removeWallpaperToken(wtoken);
                        }
                        this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                    } else {
                        Slog.w("WindowManager", "Attempted to remove non-existing token: " + token);
                    }
                }
                Binder.restoreCallingIdentity(origId);
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private Task createTaskLocked(int taskId, int stackId, int userId, AppWindowToken atoken, Rect bounds, Configuration config) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack == null) {
                throw new IllegalArgumentException("addAppToken: invalid stackId=" + stackId);
            }
            boolean z;
            Object[] objArr = new Object[WINDOWS_FREEZING_SCREENS_TIMEOUT];
            objArr[WINDOW_ANIMATION_SCALE] = Integer.valueOf(taskId);
            objArr[WINDOWS_FREEZING_SCREENS_ACTIVE] = Integer.valueOf(stackId);
            EventLog.writeEvent(EventLogTags.WM_TASK_CREATED, objArr);
            Task task = new Task(taskId, stack, userId, this, bounds, config);
            this.mTaskIdToTask.put(taskId, task);
            if (atoken.mLaunchTaskBehind) {
                z = PROFILE_ORIENTATION;
            } else {
                z = SCREENSHOT_FORCE_565;
            }
            stack.addTask(task, z, atoken.showForAllUsers);
            return task;
        }

        public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration config, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion) {
            addAppToken(addPos, token, taskId, stackId, requestedOrientation, fullscreen, showForAllUsers, userId, configChanges, voiceInteraction, launchTaskBehind, taskBounds, config, taskResizeMode, alwaysFocusable, homeTask, targetSdkVersion, PROFILE_ORIENTATION);
        }

        public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration config, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion, boolean naviBarHide) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addAppToken()")) {
                long inputDispatchingTimeoutNanos;
                try {
                    inputDispatchingTimeoutNanos = token.getKeyDispatchingTimeout() * 1000000;
                } catch (RemoteException ex) {
                    Slog.w("WindowManager", "Could not get dispatching timeout.", ex);
                    inputDispatchingTimeoutNanos = DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS;
                }
                synchronized (this.mWindowMap) {
                    if (findAppWindowToken(token.asBinder()) != null) {
                        Slog.w("WindowManager", "Attempted to add existing app token: " + token);
                        return;
                    }
                    AppWindowToken atoken = new AppWindowToken(this, token, voiceInteraction);
                    atoken.inputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
                    atoken.appFullscreen = fullscreen;
                    atoken.showForAllUsers = showForAllUsers;
                    atoken.targetSdk = targetSdkVersion;
                    atoken.requestedOrientation = requestedOrientation;
                    atoken.navigationBarHide = naviBarHide;
                    atoken.layoutConfigChanges = (configChanges & 1152) != 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                    atoken.mLaunchTaskBehind = launchTaskBehind;
                    atoken.mAlwaysFocusable = alwaysFocusable;
                    Task task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        task = createTaskLocked(taskId, stackId, userId, atoken, taskBounds, config);
                    }
                    task.addAppToken(addPos, atoken, taskResizeMode, homeTask);
                    this.mTokenMap.put(token.asBinder(), atoken);
                    atoken.hidden = SCREENSHOT_FORCE_565;
                    atoken.hiddenRequested = SCREENSHOT_FORCE_565;
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void setAppTask(IBinder token, int taskId, int stackId, Rect taskBounds, Configuration config, int taskResizeMode, boolean homeTask) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppTask()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken atoken = findAppWindowToken(token);
                    if (atoken == null) {
                        Slog.w("WindowManager", "Attempted to set task id of non-existing app token: " + token);
                        return;
                    }
                    Task oldTask = atoken.mTask;
                    oldTask.removeAppToken(atoken);
                    Task newTask = (Task) this.mTaskIdToTask.get(taskId);
                    if (newTask == null) {
                        newTask = createTaskLocked(taskId, stackId, oldTask.mUserId, atoken, taskBounds, config);
                    }
                    newTask.addAppToken(Integer.MAX_VALUE, atoken, taskResizeMode, homeTask);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public int getOrientationLocked() {
            AppWindowToken appShowWhenLocked = null;
            if (!this.mDisplayFrozen) {
                int req;
                WindowList windows = getDefaultWindowListLocked();
                for (int pos = windows.size() - 1; pos >= 0; pos--) {
                    WindowState win = (WindowState) windows.get(pos);
                    if (win.mAppToken != null) {
                        break;
                    }
                    if (win.isVisibleLw() && win.mPolicyVisibilityAfterAnim) {
                        req = win.mAttrs.screenOrientation;
                        if (!(req == -1 || req == UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
                            if (this.mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                                this.mLastKeyguardForcedOrientation = req;
                            }
                            this.mLastWindowForcedOrientation = req;
                            return req;
                        }
                    }
                }
                this.mLastWindowForcedOrientation = -1;
                if (this.mPolicy.isKeyguardLocked()) {
                    WindowState winShowWhenLocked = (WindowState) this.mPolicy.getWinShowWhenLockedLw();
                    if (winShowWhenLocked != null) {
                        appShowWhenLocked = winShowWhenLocked.mAppToken;
                    }
                    if (appShowWhenLocked == null) {
                        return this.mLastKeyguardForcedOrientation;
                    }
                    req = appShowWhenLocked.requestedOrientation;
                    if (req == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                        req = this.mLastKeyguardForcedOrientation;
                    }
                    return req;
                }
            } else if (this.mLastWindowForcedOrientation != -1) {
                return this.mLastWindowForcedOrientation;
            }
            return getAppSpecifiedOrientation();
        }

        private int getAppSpecifiedOrientation() {
            boolean inMultiWindow;
            int lastOrientation = -1;
            boolean findingBehind = PROFILE_ORIENTATION;
            boolean lastFullscreen = PROFILE_ORIENTATION;
            ArrayList<Task> tasks = getDefaultDisplayContentLocked().getTasks();
            if (isStackVisibleLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES)) {
                inMultiWindow = SCREENSHOT_FORCE_565;
            } else {
                inMultiWindow = isStackVisibleLocked(WINDOWS_FREEZING_SCREENS_TIMEOUT);
            }
            boolean dockMinimized = getDefaultDisplayContentLocked().mDividerControllerLocked.isMinimizedDock();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                int firstToken = tokens.size() - 1;
                for (int tokenNdx = firstToken; tokenNdx >= 0; tokenNdx--) {
                    AppWindowToken atoken = (AppWindowToken) tokens.get(tokenNdx);
                    if (!findingBehind && !atoken.hidden && atoken.hiddenRequested) {
                        Slog.v(TAG, "Skipping " + atoken + " -- going to hide");
                    } else if (tokenNdx == firstToken && lastOrientation != UPDATE_FOCUS_WILL_PLACE_SURFACES && r7) {
                        return lastOrientation;
                    } else {
                        if (!atoken.hiddenRequested && (!inMultiWindow || (atoken.mTask.isHomeTask() && dockMinimized))) {
                            if (tokenNdx == 0) {
                                lastOrientation = atoken.requestedOrientation;
                            }
                            int or = atoken.requestedOrientation;
                            lastFullscreen = atoken.appFullscreen;
                            if (lastFullscreen && or != UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                return or;
                            }
                            if (or != -1 && or != UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                return or;
                            }
                            findingBehind |= or == UPDATE_FOCUS_WILL_PLACE_SURFACES ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE;
                        }
                    }
                }
            }
            return inMultiWindow ? -1 : this.mForcedAppOrientation;
        }

        public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "updateOrientationFromAppTokens()")) {
                Configuration config;
                long ident = Binder.clearCallingIdentity();
                synchronized (this.mWindowMap) {
                    config = updateOrientationFromAppTokensLocked(currentConfig, freezeThisOneIfNeeded);
                }
                Binder.restoreCallingIdentity(ident);
                return config;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private Configuration updateOrientationFromAppTokensLocked(Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
            if (!this.mDisplayReady) {
                return null;
            }
            Configuration config = null;
            if (updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION)) {
                if (freezeThisOneIfNeeded != null) {
                    AppWindowToken atoken = findAppWindowToken(freezeThisOneIfNeeded);
                    if (atoken != null) {
                        startAppFreezingScreenLocked(atoken);
                    }
                }
                config = computeNewConfigurationLocked();
            } else if (currentConfig != null) {
                this.mTempConfiguration.setToDefaults();
                this.mTempConfiguration.updateFrom(currentConfig);
                computeScreenConfigurationLocked(this.mTempConfiguration);
                if (currentConfig.diff(this.mTempConfiguration) != 0) {
                    this.mWaitingForConfig = SCREENSHOT_FORCE_565;
                    DisplayContent displayContent = getDefaultDisplayContentLocked();
                    displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                    int[] anim = new int[WINDOWS_FREEZING_SCREENS_TIMEOUT];
                    if (displayContent.isDimming()) {
                        anim[WINDOWS_FREEZING_SCREENS_ACTIVE] = WINDOW_ANIMATION_SCALE;
                        anim[WINDOW_ANIMATION_SCALE] = WINDOW_ANIMATION_SCALE;
                    } else {
                        this.mPolicy.selectRotationAnimationLw(anim);
                    }
                    if (!this.mIgnoreFrozen) {
                        startFreezingDisplayLocked(PROFILE_ORIENTATION, anim[WINDOW_ANIMATION_SCALE], anim[WINDOWS_FREEZING_SCREENS_ACTIVE]);
                    }
                    config = new Configuration(this.mTempConfiguration);
                }
            }
            if (this.mIgnoreFrozen) {
                this.mIgnoreFrozen = PROFILE_ORIENTATION;
            }
            return config;
        }

        boolean updateOrientationFromAppTokensLocked(boolean inTransaction) {
            long ident = Binder.clearCallingIdentity();
            try {
                int req = getOrientationLocked();
                if (req != this.mForcedAppOrientation) {
                    this.mForcedAppOrientation = req;
                    this.mPolicy.setCurrentOrientationLw(req);
                    if (updateRotationUncheckedLocked(inTransaction)) {
                        return SCREENSHOT_FORCE_565;
                    }
                }
                Binder.restoreCallingIdentity(ident);
                return PROFILE_ORIENTATION;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int[] setNewConfiguration(Configuration config) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewConfiguration()")) {
                int[] onConfigurationChanged;
                long callingId = Binder.clearCallingIdentity();
                try {
                    if ((this.mCurConfiguration.diff(config) & DumpState.DUMP_VERSION) == 0 && (this.mCurConfiguration.diff(config) & LAYOUT_REPEAT_THRESHOLD) == 0) {
                        if ((this.mCurConfiguration.diff(config) & DumpState.DUMP_INSTALLS) != 0) {
                        }
                        Binder.restoreCallingIdentity(callingId);
                        synchronized (this.mWindowMap) {
                            if (this.mWaitingForConfig) {
                                this.mWaitingForConfig = PROFILE_ORIENTATION;
                                this.mLastFinishedFreezeSource = "new-config";
                            }
                            if (this.mCurConfiguration.diff(config) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION) {
                                return null;
                            }
                            prepareFreezingAllTaskBounds();
                            this.mCurConfiguration = new Configuration(config);
                            onConfigurationChanged = onConfigurationChanged();
                            return onConfigurationChanged;
                        }
                    }
                    ApplicationInfo appInfo = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getApplicationInfo("com.android.browser", this.mCurrentUserId);
                    if (appInfo != null) {
                        Slog.d(TAG, "update configuration and killUid + " + appInfo.uid + " for com.android.browser temporarily");
                        this.mActivityManager.killUid(UserHandle.getAppId(appInfo.uid), this.mCurrentUserId, null);
                    }
                    Binder.restoreCallingIdentity(callingId);
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
                synchronized (this.mWindowMap) {
                    if (this.mWaitingForConfig) {
                        this.mWaitingForConfig = PROFILE_ORIENTATION;
                        this.mLastFinishedFreezeSource = "new-config";
                    }
                    if (this.mCurConfiguration.diff(config) == 0) {
                    }
                    if (this.mCurConfiguration.diff(config) == 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION) {
                        prepareFreezingAllTaskBounds();
                        this.mCurConfiguration = new Configuration(config);
                        onConfigurationChanged = onConfigurationChanged();
                        return onConfigurationChanged;
                    }
                    return null;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public Rect getBoundsForNewConfiguration(int stackId) {
            Rect outBounds;
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                outBounds = new Rect();
                stack.getBoundsForNewConfiguration(outBounds);
            }
            return outBounds;
        }

        private void prepareFreezingAllTaskBounds() {
            for (int i = this.mDisplayContents.size() - 1; i >= 0; i--) {
                ArrayList<TaskStack> stacks = ((DisplayContent) this.mDisplayContents.valueAt(i)).getStacks();
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    ((TaskStack) stacks.get(stackNdx)).prepareFreezingTaskBounds();
                }
            }
        }

        private int[] onConfigurationChanged() {
            this.mPolicy.onConfigurationChanged();
            DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
            if (!this.mReconfigureOnConfigurationChanged.contains(defaultDisplayContent)) {
                this.mReconfigureOnConfigurationChanged.add(defaultDisplayContent);
            }
            for (int i = this.mReconfigureOnConfigurationChanged.size() - 1; i >= 0; i--) {
                reconfigureDisplayLocked((DisplayContent) this.mReconfigureOnConfigurationChanged.remove(i));
            }
            defaultDisplayContent.getDockedDividerController().onConfigurationChanged();
            this.mChangedStackList.clear();
            for (int stackNdx = this.mStackIdToStack.size() - 1; stackNdx >= 0; stackNdx--) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.valueAt(stackNdx);
                if (stack.onConfigurationChanged()) {
                    this.mChangedStackList.add(Integer.valueOf(stack.mStackId));
                }
            }
            return this.mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(this.mChangedStackList);
        }

        public void setAppOrientation(IApplicationToken token, int requestedOrientation) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppOrientation()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken atoken = findAppWindowToken(token.asBinder());
                    if (atoken == null) {
                        Slog.w("WindowManager", "Attempted to set orientation of non-existing app token: " + token);
                        return;
                    }
                    atoken.requestedOrientation = requestedOrientation;
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public int getAppOrientation(IApplicationToken token) {
            synchronized (this.mWindowMap) {
                AppWindowToken wtoken = findAppWindowToken(token.asBinder());
                if (wtoken == null) {
                    return -1;
                }
                int i = wtoken.requestedOrientation;
                return i;
            }
        }

        void setFocusTaskRegionLocked() {
            if (this.mFocusedApp != null) {
                Task task = this.mFocusedApp.mTask;
                DisplayContent displayContent = task.getDisplayContent();
                if (displayContent != null) {
                    displayContent.setTouchExcludeRegion(task);
                }
            }
        }

        public void setFocusedApp(IBinder token, boolean moveFocusNow) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setFocusedApp()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken appWindowToken;
                    if (token == null) {
                        appWindowToken = null;
                    } else {
                        appWindowToken = findAppWindowToken(token);
                        if (appWindowToken == null) {
                            Slog.w("WindowManager", "Attempted to set focus to non-existing app token: " + token);
                        }
                    }
                    boolean changed = this.mFocusedApp != appWindowToken ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                    if (changed) {
                        this.mFocusedApp = appWindowToken;
                        this.mInputMonitor.setFocusedAppLw(appWindowToken);
                        setFocusTaskRegionLocked();
                    }
                    if (moveFocusNow && changed) {
                        long origId = Binder.clearCallingIdentity();
                        updateFocusedWindowLocked(WINDOW_ANIMATION_SCALE, SCREENSHOT_FORCE_565);
                        Binder.restoreCallingIdentity(origId);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "prepareAppTransition()")) {
                synchronized (this.mWindowMap) {
                    if (this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent) && okToDisplay()) {
                        this.mSkipAppTransitionAnimation = PROFILE_ORIENTATION;
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
                this.mAppTransition.overridePendingAppTransition(packageName, enterAnim, exitAnim, startedCallback);
            }
        }

        public void setExitPosition(int startX, int startY, int width, int height) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.setExitPosition(startX, startY, width, height);
            }
        }

        public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionScaleUp(startX, startY, startWidth, startHeight);
            }
        }

        public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionClipReveal(startX, startY, startWidth, startHeight);
            }
        }

        public void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionThumb(srcThumb, startX, startY, startedCallback, scaleUp);
            }
        }

        public void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionAspectScaledThumb(srcThumb, startX, startY, targetWidth, targetHeight, startedCallback, scaleUp);
            }
        }

        public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionMultiThumb(specs, onAnimationStartedCallback, onAnimationFinishedCallback, scaleUp);
                prolongAnimationsFromSpecs(specs, scaleUp);
            }
        }

        void prolongAnimationsFromSpecs(AppTransitionAnimationSpec[] specs, boolean scaleUp) {
            this.mTmpTaskIds.clear();
            for (int i = specs.length - 1; i >= 0; i--) {
                this.mTmpTaskIds.put(specs[i].taskId, WINDOW_ANIMATION_SCALE);
            }
            for (WindowState win : this.mWindowMap.values()) {
                Task task = win.getTask();
                if (!(task == null || this.mTmpTaskIds.get(task.mTaskId, -1) == -1 || !task.inFreeformWorkspace())) {
                    AppWindowToken appToken = win.mAppToken;
                    if (!(appToken == null || appToken.mAppAnimator == null)) {
                        appToken.mAppAnimator.startProlongAnimation(scaleUp ? WINDOWS_FREEZING_SCREENS_TIMEOUT : WINDOWS_FREEZING_SCREENS_ACTIVE);
                    }
                }
            }
        }

        public void overridePendingAppTransitionInPlace(String packageName, int anim) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overrideInPlaceAppTransition(packageName, anim);
            }
        }

        public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, callback, scaleUp);
            }
        }

        public void endProlongedAnimations() {
            synchronized (this.mWindowMap) {
                for (WindowState win : this.mWindowMap.values()) {
                    AppWindowToken appToken = win.mAppToken;
                    if (!(appToken == null || appToken.mAppAnimator == null)) {
                        appToken.mAppAnimator.endProlongedAnimation();
                    }
                }
                this.mAppTransition.notifyProlongedAnimationsEnded();
            }
        }

        public void executeAppTransition() {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "executeAppTransition()")) {
                synchronized (this.mWindowMap) {
                    if (this.mAppTransition.isTransitionSet()) {
                        this.mAppTransition.setReady();
                        long origId = Binder.clearCallingIdentity();
                        try {
                            this.mWindowPlacerLocked.performSurfacePlacement();
                            Binder.restoreCallingIdentity(origId);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public boolean setAppStartingWindow(IBinder token, String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean createIfNeeded) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppStartingWindow()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null) {
                        Slog.w("WindowManager", "Attempted to set icon of non-existing app token: " + token);
                        return PROFILE_ORIENTATION;
                    }
                    Flog.i(301, "setAppStartingWindow: token=" + token + " pkg=" + pkg + " transferFrom=" + transferFrom + " windowFlags=" + windowFlags + " createIfNeeded=" + createIfNeeded + " okToDisplay=" + okToDisplay());
                    if (!okToDisplay()) {
                        return PROFILE_ORIENTATION;
                    } else if (wtoken.startingData != null) {
                        return PROFILE_ORIENTATION;
                    } else {
                        if (theme != 0) {
                            Entry ent = AttributeCache.instance().get(pkg, theme, R.styleable.Window, this.mCurrentUserId);
                            if (ent == null) {
                                return PROFILE_ORIENTATION;
                            }
                            boolean windowIsTranslucent = ent.array.getBoolean(WINDOW_LAYER_MULTIPLIER, PROFILE_ORIENTATION);
                            boolean windowIsFloating = ent.array.getBoolean(LAYOUT_REPEAT_THRESHOLD, PROFILE_ORIENTATION);
                            boolean windowShowWallpaper = ent.array.getBoolean(14, PROFILE_ORIENTATION);
                            boolean windowDisableStarting = ent.array.getBoolean(12, PROFILE_ORIENTATION);
                            if ("com.huawei.android.launcher".equals(pkg) || isSplitMode()) {
                                return PROFILE_ORIENTATION;
                            }
                            if (windowIsTranslucent) {
                                if (HISI_PERF_OPT) {
                                    Boolean pkgIn = (Boolean) this.mPackages.get(pkg);
                                    Boolean accel = Boolean.valueOf(pkgIn != null ? pkgIn.booleanValue() : PROFILE_ORIENTATION);
                                    if (accel.booleanValue()) {
                                        Slog.i(TAG, "setAppStartingWindow pkgname " + pkg + ".accel " + accel);
                                        if (ent.array.getResourceId(WINDOWS_FREEZING_SCREENS_ACTIVE, WINDOW_ANIMATION_SCALE) == 0) {
                                            return PROFILE_ORIENTATION;
                                        }
                                    }
                                    return PROFILE_ORIENTATION;
                                }
                                return PROFILE_ORIENTATION;
                            }
                            if (windowIsFloating || windowDisableStarting) {
                                return PROFILE_ORIENTATION;
                            } else if (windowShowWallpaper) {
                                if (this.mWallpaperControllerLocked.getWallpaperTarget() == null) {
                                    windowFlags |= DumpState.DUMP_DEXOPT;
                                } else {
                                    return PROFILE_ORIENTATION;
                                }
                            }
                        }
                        if (transferStartingWindow(transferFrom, wtoken)) {
                            return SCREENSHOT_FORCE_565;
                        } else if (createIfNeeded) {
                            wtoken.startingData = new StartingData(pkg, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags);
                            this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(WINDOW_LAYER_MULTIPLIER, wtoken));
                            return SCREENSHOT_FORCE_565;
                        } else {
                            return PROFILE_ORIENTATION;
                        }
                    }
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private boolean transferStartingWindow(IBinder transferFrom, AppWindowToken wtoken) {
            if (transferFrom == null) {
                return PROFILE_ORIENTATION;
            }
            AppWindowToken ttoken = findAppWindowToken(transferFrom);
            if (ttoken == null) {
                return PROFILE_ORIENTATION;
            }
            WindowState startingWindow = ttoken.startingWindow;
            if (startingWindow != null && ttoken.startingView != null) {
                this.mSkipAppTransitionAnimation = SCREENSHOT_FORCE_565;
                long origId = Binder.clearCallingIdentity();
                wtoken.startingData = ttoken.startingData;
                wtoken.startingView = ttoken.startingView;
                wtoken.startingDisplayed = ttoken.startingDisplayed;
                ttoken.startingDisplayed = PROFILE_ORIENTATION;
                wtoken.startingWindow = startingWindow;
                wtoken.reportedVisible = ttoken.reportedVisible;
                ttoken.startingData = null;
                ttoken.startingView = null;
                ttoken.startingWindow = null;
                ttoken.startingMoved = SCREENSHOT_FORCE_565;
                startingWindow.mToken = wtoken;
                startingWindow.mRootToken = wtoken;
                startingWindow.mAppToken = wtoken;
                startingWindow.getWindowList().remove(startingWindow);
                this.mWindowsChanged = SCREENSHOT_FORCE_565;
                ttoken.windows.remove(startingWindow);
                ttoken.allAppWindows.remove(startingWindow);
                if (wtoken.startingView == null) {
                    Slog.d(TAG, "***transferFrom view error for starting window: " + startingWindow);
                    this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(WINDOW_LAYER_MULTIPLIER, wtoken));
                    Binder.restoreCallingIdentity(origId);
                    return SCREENSHOT_FORCE_565;
                }
                addWindowToListInOrderLocked(startingWindow, SCREENSHOT_FORCE_565);
                if (ttoken.allDrawn) {
                    wtoken.allDrawn = SCREENSHOT_FORCE_565;
                    wtoken.deferClearAllDrawn = ttoken.deferClearAllDrawn;
                }
                if (ttoken.firstWindowDrawn) {
                    wtoken.firstWindowDrawn = SCREENSHOT_FORCE_565;
                }
                if (!ttoken.hidden) {
                    wtoken.hidden = PROFILE_ORIENTATION;
                    wtoken.hiddenRequested = PROFILE_ORIENTATION;
                }
                if (wtoken.clientHidden != ttoken.clientHidden) {
                    wtoken.clientHidden = ttoken.clientHidden;
                    wtoken.sendAppVisibilityToClients();
                }
                ttoken.mAppAnimator.transferCurrentAnimation(wtoken.mAppAnimator, startingWindow.mWinAnimator);
                updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES, SCREENSHOT_FORCE_565);
                getDefaultDisplayContentLocked().layoutNeeded = SCREENSHOT_FORCE_565;
                this.mWindowPlacerLocked.performSurfacePlacement();
                Binder.restoreCallingIdentity(origId);
                return SCREENSHOT_FORCE_565;
            } else if (ttoken.startingData != null) {
                wtoken.startingData = ttoken.startingData;
                ttoken.startingData = null;
                ttoken.startingMoved = SCREENSHOT_FORCE_565;
                this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(WINDOW_LAYER_MULTIPLIER, wtoken));
                return SCREENSHOT_FORCE_565;
            } else {
                AppWindowAnimator tAppAnimator = ttoken.mAppAnimator;
                AppWindowAnimator wAppAnimator = wtoken.mAppAnimator;
                if (tAppAnimator.thumbnail != null) {
                    if (wAppAnimator.thumbnail != null) {
                        wAppAnimator.thumbnail.destroy();
                    }
                    wAppAnimator.thumbnail = tAppAnimator.thumbnail;
                    wAppAnimator.thumbnailLayer = tAppAnimator.thumbnailLayer;
                    wAppAnimator.thumbnailAnimation = tAppAnimator.thumbnailAnimation;
                    tAppAnimator.thumbnail = null;
                }
                return PROFILE_ORIENTATION;
            }
        }

        public void removeAppStartingWindow(IBinder token) {
            synchronized (this.mWindowMap) {
                scheduleRemoveStartingWindowLocked(((WindowToken) this.mTokenMap.get(token)).appWindowToken);
            }
        }

        public void setAppFullscreen(IBinder token, boolean toOpaque) {
            synchronized (this.mWindowMap) {
                AppWindowToken atoken = findAppWindowToken(token);
                if (atoken != null) {
                    atoken.appFullscreen = toOpaque;
                    setWindowOpaqueLocked(token, toOpaque);
                    this.mWindowPlacerLocked.requestTraversal();
                }
            }
        }

        public void setWindowOpaque(IBinder token, boolean isOpaque) {
            synchronized (this.mWindowMap) {
                setWindowOpaqueLocked(token, isOpaque);
            }
        }

        public void setWindowOpaqueLocked(IBinder token, boolean isOpaque) {
            AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken != null) {
                WindowState win = wtoken.findMainWindow();
                if (win != null) {
                    win.mWinAnimator.setOpaqueLocked(isOpaque);
                }
            }
        }

        boolean setTokenVisibilityLocked(AppWindowToken wtoken, LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
            int i;
            boolean delayed = PROFILE_ORIENTATION;
            if (wtoken.clientHidden == visible) {
                wtoken.clientHidden = visible ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
                wtoken.sendAppVisibilityToClients();
            }
            boolean visibilityChanged = PROFILE_ORIENTATION;
            if (wtoken.hidden == visible || ((wtoken.hidden && wtoken.mIsExiting) || (visible && wtoken.waitingForReplacement()))) {
                boolean changed = PROFILE_ORIENTATION;
                boolean runningAppAnimation = PROFILE_ORIENTATION;
                if (transit != -1) {
                    if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                        wtoken.mAppAnimator.setNullAnimation();
                    }
                    if (applyAnimationLocked(wtoken, lp, transit, visible, isVoiceInteraction)) {
                        runningAppAnimation = SCREENSHOT_FORCE_565;
                        delayed = SCREENSHOT_FORCE_565;
                    }
                    WindowState window = wtoken.findMainWindow();
                    if (!(window == null || this.mAccessibilityController == null || window.getDisplayId() != 0)) {
                        this.mAccessibilityController.onAppWindowTransitionLocked(window, transit);
                    }
                    changed = SCREENSHOT_FORCE_565;
                }
                int windowsCount = wtoken.allAppWindows.size();
                for (i = WINDOW_ANIMATION_SCALE; i < windowsCount; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState win = (WindowState) wtoken.allAppWindows.get(i);
                    if (win == wtoken.startingWindow) {
                        if (!visible && win.isVisibleNow() && wtoken.mAppAnimator.isAnimating()) {
                            win.mAnimatingExit = SCREENSHOT_FORCE_565;
                            win.mRemoveOnExit = SCREENSHOT_FORCE_565;
                            win.mWindowRemovalAllowed = SCREENSHOT_FORCE_565;
                        }
                    } else if (visible) {
                        if (!win.isVisibleNow()) {
                            if (!runningAppAnimation) {
                                win.mWinAnimator.applyAnimationLocked(WINDOWS_FREEZING_SCREENS_ACTIVE, SCREENSHOT_FORCE_565);
                                if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                                    this.mAccessibilityController.onWindowTransitionLocked(win, WINDOWS_FREEZING_SCREENS_ACTIVE);
                                }
                            }
                            changed = SCREENSHOT_FORCE_565;
                            win.setDisplayLayoutNeeded();
                        }
                    } else if (win.isVisibleNow()) {
                        if (!runningAppAnimation) {
                            win.mWinAnimator.applyAnimationLocked(WINDOWS_FREEZING_SCREENS_TIMEOUT, PROFILE_ORIENTATION);
                            if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                                this.mAccessibilityController.onWindowTransitionLocked(win, WINDOWS_FREEZING_SCREENS_TIMEOUT);
                            }
                        }
                        changed = SCREENSHOT_FORCE_565;
                        win.setDisplayLayoutNeeded();
                    }
                }
                boolean z = visible ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
                wtoken.hiddenRequested = z;
                wtoken.hidden = z;
                visibilityChanged = SCREENSHOT_FORCE_565;
                if (visible) {
                    WindowState swin = wtoken.startingWindow;
                    if (!(swin == null || swin.isDrawnLw())) {
                        swin.mPolicyVisibility = PROFILE_ORIENTATION;
                        swin.mPolicyVisibilityAfterAnim = PROFILE_ORIENTATION;
                    }
                } else {
                    unsetAppFreezingScreenLocked(wtoken, SCREENSHOT_FORCE_565, SCREENSHOT_FORCE_565);
                }
                if (changed) {
                    this.mInputMonitor.setUpdateInputWindowsNeededLw();
                    if (performLayout) {
                        updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES, PROFILE_ORIENTATION);
                        this.mWindowPlacerLocked.performSurfacePlacement();
                    }
                    this.mInputMonitor.updateInputWindowsLw(PROFILE_ORIENTATION);
                }
            }
            if (wtoken.mAppAnimator.animation != null) {
                delayed = SCREENSHOT_FORCE_565;
            }
            for (i = wtoken.allAppWindows.size() - 1; i >= 0 && !delayed; i--) {
                if (((WindowState) wtoken.allAppWindows.get(i)).mWinAnimator.isWindowAnimationSet()) {
                    delayed = SCREENSHOT_FORCE_565;
                }
            }
            if (visibilityChanged) {
                if (visible && !delayed) {
                    wtoken.mEnteringAnimation = SCREENSHOT_FORCE_565;
                    this.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(wtoken.token);
                }
                if (!(this.mClosingApps.contains(wtoken) || this.mOpeningApps.contains(wtoken))) {
                    getDefaultDisplayContentLocked().getDockedDividerController().notifyAppVisibilityChanged();
                }
            }
            return delayed;
        }

        void updateTokenInPlaceLocked(AppWindowToken wtoken, int transit) {
            if (transit != -1) {
                if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                    wtoken.mAppAnimator.setNullAnimation();
                }
                applyAnimationLocked(wtoken, null, transit, PROFILE_ORIENTATION, PROFILE_ORIENTATION);
            }
        }

        public void notifyAppStopped(IBinder token, boolean stopped) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "notifyAppStopped()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null) {
                        Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + token);
                        return;
                    }
                    wtoken.notifyAppStopped(stopped);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void setAppVisibility(IBinder token, boolean visible) {
            boolean z = PROFILE_ORIENTATION;
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppVisibility()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null) {
                        Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + token);
                        return;
                    }
                    this.mOpeningApps.remove(wtoken);
                    this.mClosingApps.remove(wtoken);
                    wtoken.waitingToShow = PROFILE_ORIENTATION;
                    if (!visible) {
                        z = SCREENSHOT_FORCE_565;
                    }
                    wtoken.hiddenRequested = z;
                    if (!visible) {
                        wtoken.removeAllDeadWindows();
                        wtoken.setVisibleBeforeClientHidden();
                    } else if (visible) {
                        if (!this.mAppTransition.isTransitionSet() && this.mAppTransition.isReady()) {
                            this.mOpeningApps.add(wtoken);
                        }
                        wtoken.startingMoved = PROFILE_ORIENTATION;
                        if (wtoken.hidden || wtoken.mAppStopped) {
                            wtoken.clearAllDrawn();
                            if (wtoken.hidden) {
                                wtoken.waitingToShow = SCREENSHOT_FORCE_565;
                            }
                            if (wtoken.clientHidden) {
                                wtoken.clientHidden = PROFILE_ORIENTATION;
                                wtoken.sendAppVisibilityToClients();
                            }
                        }
                        wtoken.requestUpdateWallpaperIfNeeded();
                        wtoken.mAppStopped = PROFILE_ORIENTATION;
                    }
                    if (okToDisplay() && this.mAppTransition.isTransitionSet()) {
                        if (wtoken.mAppAnimator.usingTransferredAnimation && wtoken.mAppAnimator.animation == null) {
                            Slog.wtf("WindowManager", "Will NOT set dummy animation on: " + wtoken + ", using null transfered animation!");
                        }
                        if (!wtoken.mAppAnimator.usingTransferredAnimation && (!wtoken.startingDisplayed || this.mSkipAppTransitionAnimation)) {
                            wtoken.mAppAnimator.setDummyAnimation();
                        }
                        wtoken.inPendingTransaction = SCREENSHOT_FORCE_565;
                        if (visible) {
                            this.mOpeningApps.add(wtoken);
                            wtoken.mEnteringAnimation = SCREENSHOT_FORCE_565;
                        } else {
                            this.mClosingApps.add(wtoken);
                            wtoken.mEnteringAnimation = PROFILE_ORIENTATION;
                        }
                        if (this.mAppTransition.getAppTransition() == 16) {
                            WindowState win = findFocusedWindowLocked(getDefaultDisplayContentLocked());
                            if (win != null) {
                                AppWindowToken focusedToken = win.mAppToken;
                                if (focusedToken != null) {
                                    focusedToken.hidden = SCREENSHOT_FORCE_565;
                                    this.mOpeningApps.add(focusedToken);
                                }
                            }
                        }
                        return;
                    }
                    long origId = Binder.clearCallingIdentity();
                    wtoken.inPendingTransaction = PROFILE_ORIENTATION;
                    setTokenVisibilityLocked(wtoken, null, visible, -1, SCREENSHOT_FORCE_565, wtoken.voiceInteraction);
                    wtoken.updateReportedVisibilityLocked();
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void unsetAppFreezingScreenLocked(AppWindowToken wtoken, boolean unfreezeSurfaceNow, boolean force) {
            if (!(wtoken == null || wtoken.mAppAnimator == null || !wtoken.mAppAnimator.freezingScreen)) {
                Slog.i("WindowManager", "Clear freezing of " + wtoken + " force=" + force + " unfreezeSurfaceNow " + unfreezeSurfaceNow);
                int N = wtoken.allAppWindows.size();
                boolean unfrozeWindows = PROFILE_ORIENTATION;
                for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState w = (WindowState) wtoken.allAppWindows.get(i);
                    if (w.mAppFreezing) {
                        w.mAppFreezing = PROFILE_ORIENTATION;
                        if (!(!w.mHasSurface || w.mOrientationChanging || this.mWindowsFreezingScreen == WINDOWS_FREEZING_SCREENS_TIMEOUT)) {
                            w.mOrientationChanging = SCREENSHOT_FORCE_565;
                            this.mWindowPlacerLocked.mOrientationChangeComplete = PROFILE_ORIENTATION;
                        }
                        w.mLastFreezeDuration = WINDOW_ANIMATION_SCALE;
                        unfrozeWindows = SCREENSHOT_FORCE_565;
                        w.setDisplayLayoutNeeded();
                    }
                }
                if (force || unfrozeWindows) {
                    wtoken.mAppAnimator.freezingScreen = PROFILE_ORIENTATION;
                    wtoken.mAppAnimator.lastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                    this.mAppsFreezingScreen--;
                    this.mLastFinishedFreezeSource = wtoken;
                }
                if (unfreezeSurfaceNow) {
                    if (unfrozeWindows) {
                        this.mWindowPlacerLocked.performSurfacePlacement();
                    }
                    stopFreezingDisplayLocked();
                }
            }
        }

        private void startAppFreezingScreenLocked(AppWindowToken wtoken) {
            logWithStack(TAG, "Set freezing of " + wtoken.appToken + ": hidden=" + wtoken.hidden + " freezing=" + wtoken.mAppAnimator.freezingScreen);
            if (!wtoken.hiddenRequested) {
                if (!wtoken.mAppAnimator.freezingScreen) {
                    wtoken.mAppAnimator.freezingScreen = SCREENSHOT_FORCE_565;
                    wtoken.mAppAnimator.lastFreezeDuration = WINDOW_ANIMATION_SCALE;
                    this.mAppsFreezingScreen += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    if (this.mAppsFreezingScreen == WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        startFreezingDisplayLocked(PROFILE_ORIENTATION, WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE);
                        this.mH.removeMessages(17);
                        this.mH.sendEmptyMessageDelayed(17, 2000);
                    }
                }
                int N = wtoken.allAppWindows.size();
                for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    ((WindowState) wtoken.allAppWindows.get(i)).mAppFreezing = SCREENSHOT_FORCE_565;
                }
            }
        }

        public void startAppFreezingScreen(IBinder token, int configChanges) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    if (configChanges == 0) {
                        if (okToDisplay()) {
                            return;
                        }
                    }
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null || wtoken.appToken == null) {
                        Slog.w("WindowManager", "Attempted to freeze screen with non-existing app token: " + wtoken);
                        return;
                    }
                    long origId = Binder.clearCallingIdentity();
                    startAppFreezingScreenLocked(wtoken);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void stopAppFreezingScreen(IBinder token, boolean force) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null || wtoken.appToken == null) {
                        return;
                    }
                    long origId = Binder.clearCallingIdentity();
                    unsetAppFreezingScreenLocked(wtoken, SCREENSHOT_FORCE_565, force);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void removeAppToken(IBinder token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeAppToken()")) {
                AppWindowToken appWindowToken = null;
                AppWindowToken appWindowToken2 = null;
                boolean z = PROFILE_ORIENTATION;
                long origId = Binder.clearCallingIdentity();
                synchronized (this.mWindowMap) {
                    WindowToken basewtoken = (WindowToken) this.mTokenMap.remove(token);
                    if (basewtoken != null) {
                        appWindowToken = basewtoken.appWindowToken;
                        if (appWindowToken != null) {
                            z = setTokenVisibilityLocked(appWindowToken, null, PROFILE_ORIENTATION, -1, SCREENSHOT_FORCE_565, appWindowToken.voiceInteraction);
                            appWindowToken.inPendingTransaction = PROFILE_ORIENTATION;
                            this.mOpeningApps.remove(appWindowToken);
                            appWindowToken.waitingToShow = PROFILE_ORIENTATION;
                            if (this.mClosingApps.contains(appWindowToken)) {
                                z = SCREENSHOT_FORCE_565;
                            } else if (this.mAppTransition.isTransitionSet()) {
                                this.mClosingApps.add(appWindowToken);
                                z = SCREENSHOT_FORCE_565;
                            }
                            TaskStack stack = appWindowToken.mTask.mStack;
                            if (!z || appWindowToken.allAppWindows.isEmpty()) {
                                appWindowToken.mAppAnimator.clearAnimation();
                                appWindowToken.mAppAnimator.animating = PROFILE_ORIENTATION;
                                appWindowToken.removeAppFromTaskLocked();
                            } else {
                                stack.mExitingAppTokens.add(appWindowToken);
                                appWindowToken.mIsExiting = SCREENSHOT_FORCE_565;
                            }
                            appWindowToken.removed = SCREENSHOT_FORCE_565;
                            if (appWindowToken.startingData != null) {
                                appWindowToken2 = appWindowToken;
                            }
                            unsetAppFreezingScreenLocked(appWindowToken, SCREENSHOT_FORCE_565, SCREENSHOT_FORCE_565);
                            if (this.mFocusedApp == appWindowToken) {
                                this.mFocusedApp = null;
                                updateFocusedWindowLocked(WINDOW_ANIMATION_SCALE, SCREENSHOT_FORCE_565);
                                this.mInputMonitor.setFocusedAppLw(null);
                            }
                            if (!(z || appWindowToken == null)) {
                                appWindowToken.updateReportedVisibilityLocked();
                            }
                            scheduleRemoveStartingWindowLocked(appWindowToken2);
                        }
                    }
                    Slog.w("WindowManager", "Attempted to remove non-existing app token: " + token);
                    appWindowToken.updateReportedVisibilityLocked();
                    scheduleRemoveStartingWindowLocked(appWindowToken2);
                }
                Binder.restoreCallingIdentity(origId);
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        void scheduleRemoveStartingWindowLocked(AppWindowToken wtoken) {
            if (wtoken != null && !this.mH.hasMessages(6, wtoken)) {
                if (wtoken == null || wtoken.startingWindow != null) {
                    Flog.i(301, "Schedule remove starting " + wtoken + " startingWindow= " + wtoken.startingWindow);
                    this.mH.sendMessage(this.mH.obtainMessage(6, wtoken));
                    return;
                }
                if (wtoken.startingData != null) {
                    wtoken.startingData = null;
                }
            }
        }

        void dumpAppTokensLocked() {
            int numStacks = this.mStackIdToStack.size();
            for (int stackNdx = WINDOW_ANIMATION_SCALE; stackNdx < numStacks; stackNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.valueAt(stackNdx);
                Slog.v("WindowManager", "  Stack #" + stack.mStackId + " tasks from bottom to top:");
                ArrayList<Task> tasks = stack.getTasks();
                int numTasks = tasks.size();
                for (int taskNdx = WINDOW_ANIMATION_SCALE; taskNdx < numTasks; taskNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    Task task = (Task) tasks.get(taskNdx);
                    Slog.v("WindowManager", "    Task #" + task.mTaskId + " activities from bottom to top:");
                    AppTokenList tokens = task.mAppTokens;
                    int numTokens = tokens.size();
                    for (int tokenNdx = WINDOW_ANIMATION_SCALE; tokenNdx < numTokens; tokenNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        Slog.v("WindowManager", "      activity #" + tokenNdx + ": " + ((AppWindowToken) tokens.get(tokenNdx)).token);
                    }
                }
            }
        }

        void dumpWindowsLocked() {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                DisplayContent displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                Slog.v("WindowManager", " Display #" + displayContent.getDisplayId());
                WindowList windows = displayContent.getWindowList();
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    Slog.v("WindowManager", "  #" + winNdx + ": " + windows.get(winNdx));
                }
            }
        }

        private final int reAddWindowLocked(int index, WindowState win) {
            WindowList windows = win.getWindowList();
            int NCW = win.mChildWindows.size();
            boolean winAdded = PROFILE_ORIENTATION;
            for (int j = WINDOW_ANIMATION_SCALE; j < NCW; j += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                WindowState cwin = (WindowState) win.mChildWindows.get(j);
                if (!winAdded && cwin.mSubLayer >= 0) {
                    win.mRebuilding = PROFILE_ORIENTATION;
                    windows.add(index, win);
                    index += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    winAdded = SCREENSHOT_FORCE_565;
                }
                cwin.mRebuilding = PROFILE_ORIENTATION;
                windows.add(index, cwin);
                index += WINDOWS_FREEZING_SCREENS_ACTIVE;
            }
            if (!winAdded) {
                win.mRebuilding = PROFILE_ORIENTATION;
                windows.add(index, win);
                index += WINDOWS_FREEZING_SCREENS_ACTIVE;
            }
            this.mWindowsChanged = SCREENSHOT_FORCE_565;
            return index;
        }

        private final int reAddAppWindowsLocked(DisplayContent displayContent, int index, WindowToken token, boolean needNotifyColor) {
            int NW = token.windows.size();
            for (int i = WINDOW_ANIMATION_SCALE; i < NW; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                WindowState win = (WindowState) token.windows.get(i);
                DisplayContent winDisplayContent = win.getDisplayContent();
                if (winDisplayContent == displayContent || winDisplayContent == null) {
                    win.mDisplayContent = displayContent;
                    index = reAddWindowLocked(index, win);
                    if (needNotifyColor && win.canCarryColors() && win.isWinVisibleLw()) {
                        this.mPolicy.updateSystemUiColorLw(win);
                    }
                }
            }
            return index;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void moveStackWindowsLocked(DisplayContent displayContent) {
            WindowList windows = displayContent.getWindowList();
            this.mTmpWindows.addAll(windows);
            rebuildAppWindowListLocked(displayContent);
            int tmpSize = this.mTmpWindows.size();
            int winSize = windows.size();
            int tmpNdx = WINDOW_ANIMATION_SCALE;
            int winNdx = WINDOW_ANIMATION_SCALE;
            while (tmpNdx < tmpSize && winNdx < winSize) {
                while (true) {
                    int tmpNdx2 = tmpNdx + WINDOWS_FREEZING_SCREENS_ACTIVE;
                    WindowState tmp = (WindowState) this.mTmpWindows.get(tmpNdx);
                    if (tmpNdx2 >= tmpSize || tmp.mAppToken == null || !tmp.mAppToken.mIsExiting) {
                        while (true) {
                            int winNdx2 = winNdx + WINDOWS_FREEZING_SCREENS_ACTIVE;
                            WindowState win = (WindowState) windows.get(winNdx);
                            winNdx = winNdx2;
                        }
                    } else {
                        tmpNdx = tmpNdx2;
                    }
                }
                while (true) {
                    int winNdx22 = winNdx + WINDOWS_FREEZING_SCREENS_ACTIVE;
                    WindowState win2 = (WindowState) windows.get(winNdx);
                    if (winNdx22 < winSize && win2.mAppToken != null && win2.mAppToken.mIsExiting) {
                        winNdx = winNdx22;
                    } else if (tmp != win2) {
                        displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                        winNdx = winNdx22;
                        tmpNdx = tmpNdx2;
                        break;
                    } else {
                        winNdx = winNdx22;
                        tmpNdx = tmpNdx2;
                    }
                }
            }
            if (tmpNdx != winNdx) {
                displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
            }
            this.mTmpWindows.clear();
            if (!updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES, PROFILE_ORIENTATION)) {
                this.mLayersController.assignLayersLocked(displayContent.getWindowList());
            }
            this.mInputMonitor.setUpdateInputWindowsNeededLw();
            this.mWindowPlacerLocked.performSurfacePlacement();
            this.mInputMonitor.updateInputWindowsLw(PROFILE_ORIENTATION);
        }

        public void moveTaskToTop(int taskId) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    Task task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        return;
                    }
                    TaskStack stack = task.mStack;
                    DisplayContent displayContent = task.getDisplayContent();
                    if (displayContent == null) {
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    displayContent.moveStack(stack, SCREENSHOT_FORCE_565);
                    if (displayContent.isDefaultDisplay) {
                        TaskStack homeStack = displayContent.getHomeStack();
                        if (homeStack != stack) {
                            displayContent.moveStack(homeStack, PROFILE_ORIENTATION);
                        }
                    }
                    stack.moveTaskToTop(task);
                    if (this.mAppTransition.isTransitionSet()) {
                        task.setSendingToBottom(PROFILE_ORIENTATION);
                    }
                    moveStackWindowsLocked(displayContent);
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void moveTaskToBottom(int taskId) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    Task task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        Slog.e("WindowManager", "moveTaskToBottom: taskId=" + taskId + " not found in mTaskIdToTask");
                        return;
                    }
                    TaskStack stack = task.mStack;
                    stack.moveTaskToBottom(task);
                    if (this.mAppTransition.isTransitionSet()) {
                        task.setSendingToBottom(SCREENSHOT_FORCE_565);
                    }
                    moveStackWindowsLocked(stack.getDisplayContent());
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        boolean isStackVisibleLocked(int stackId) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            return stack != null ? stack.isVisibleLocked() : PROFILE_ORIENTATION;
        }

        public void setDockedStackCreateState(int mode, Rect bounds) {
            synchronized (this.mWindowMap) {
                setDockedStackCreateStateLocked(mode, bounds);
            }
        }

        void setDockedStackCreateStateLocked(int mode, Rect bounds) {
            this.mDockedStackCreateMode = mode;
            this.mDockedStackCreateBounds = bounds;
        }

        public Rect attachStack(int stackId, int displayId, boolean onTop) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = (DisplayContent) this.mDisplayContents.get(displayId);
                    if (displayContent != null) {
                        TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                        if (stack == null) {
                            stack = new TaskStack(this, stackId);
                            this.mStackIdToStack.put(stackId, stack);
                            if (stackId == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                getDefaultDisplayContentLocked().mDividerControllerLocked.notifyDockedStackExistsChanged(SCREENSHOT_FORCE_565);
                            }
                        }
                        stack.attachDisplayContent(displayContent);
                        displayContent.attachStack(stack, onTop);
                        if (stack.getRawFullscreen()) {
                            return null;
                        }
                        Rect bounds = new Rect();
                        stack.getRawBounds(bounds);
                        Binder.restoreCallingIdentity(origId);
                        return bounds;
                    }
                    Binder.restoreCallingIdentity(origId);
                    return null;
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        void detachStackLocked(DisplayContent displayContent, TaskStack stack) {
            displayContent.detachStack(stack);
            stack.detachDisplay();
            if (stack.mStackId == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                getDefaultDisplayContentLocked().mDividerControllerLocked.notifyDockedStackExistsChanged(PROFILE_ORIENTATION);
            }
        }

        public void detachStack(int stackId) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    DisplayContent displayContent = stack.getDisplayContent();
                    if (displayContent != null) {
                        if (stack.isAnimating()) {
                            stack.mDeferDetach = SCREENSHOT_FORCE_565;
                            return;
                        }
                        detachStackLocked(displayContent, stack);
                    }
                }
            }
        }

        public void removeStack(int stackId) {
            synchronized (this.mWindowMap) {
                this.mStackIdToStack.remove(stackId);
            }
        }

        public void removeTask(int taskId) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                task.removeLocked();
            }
        }

        public void cancelTaskWindowTransition(int taskId) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.cancelTaskWindowTransition();
                }
            }
        }

        public void cancelTaskThumbnailTransition(int taskId) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.cancelTaskThumbnailTransition();
                }
            }
        }

        public void addTask(int taskId, int stackId, boolean toTop) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                stack.addTask(task, toTop);
                stack.getDisplayContent().layoutNeeded = SCREENSHOT_FORCE_565;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    return;
                }
                task.moveTaskToStack(stack, toTop);
                stack.getDisplayContent().layoutNeeded = SCREENSHOT_FORCE_565;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        public void getStackDockedModeBounds(int stackId, Rect bounds, boolean ignoreVisibility) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    stack.getStackDockedModeBoundsLocked(bounds, ignoreVisibility);
                    return;
                }
                bounds.setEmpty();
            }
        }

        public void getStackBounds(int stackId, Rect bounds) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    stack.getBounds(bounds);
                    return;
                }
                bounds.setEmpty();
            }
        }

        public boolean resizeStack(int stackId, Rect bounds, SparseArray<Configuration> configs, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
            boolean rawFullscreen;
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    throw new IllegalArgumentException("resizeStack: stackId " + stackId + " not found.");
                }
                if (stack.setBounds(bounds, configs, taskBounds, taskTempInsetBounds) && stack.isVisibleLocked()) {
                    stack.getDisplayContent().layoutNeeded = SCREENSHOT_FORCE_565;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
                rawFullscreen = stack.getRawFullscreen();
            }
            return rawFullscreen;
        }

        public void prepareFreezingTaskBounds(int stackId) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    throw new IllegalArgumentException("prepareFreezingTaskBounds: stackId " + stackId + " not found.");
                }
                stack.prepareFreezingTaskBounds();
            }
        }

        public void positionTaskInStack(int taskId, int stackId, int position, Rect bounds, Configuration config) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    return;
                }
                task.positionTaskInStack(stack, position, bounds, config);
                stack.getDisplayContent().layoutNeeded = SCREENSHOT_FORCE_565;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        public void resizeTask(int taskId, Rect bounds, Configuration configuration, boolean relayout, boolean forced) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    throw new IllegalArgumentException("resizeTask: taskId " + taskId + " not found.");
                }
                if (task.resizeLocked(bounds, configuration, forced) && relayout) {
                    task.getDisplayContent().layoutNeeded = SCREENSHOT_FORCE_565;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
            }
        }

        public void setTaskDockedResizing(int taskId, boolean resizing) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    Slog.w(TAG, "setTaskDockedResizing: taskId " + taskId + " not found.");
                    return;
                }
                task.setDragResizing(resizing, WINDOWS_FREEZING_SCREENS_ACTIVE);
            }
        }

        public void scrollTask(int taskId, Rect bounds) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    throw new IllegalArgumentException("scrollTask: taskId " + taskId + " not found.");
                }
                if (task.scrollLocked(bounds)) {
                    task.getDisplayContent().layoutNeeded = SCREENSHOT_FORCE_565;
                    this.mInputMonitor.setUpdateInputWindowsNeededLw();
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
            }
        }

        public void deferSurfaceLayout() {
            synchronized (this.mWindowMap) {
                this.mWindowPlacerLocked.deferLayout();
            }
        }

        public void continueSurfaceLayout() {
            synchronized (this.mWindowMap) {
                this.mWindowPlacerLocked.continueLayout();
            }
        }

        public void getTaskBounds(int taskId, Rect bounds) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.getBounds(bounds);
                    return;
                }
                bounds.setEmpty();
            }
        }

        public boolean isValidTaskId(int taskId) {
            boolean z;
            synchronized (this.mWindowMap) {
                z = this.mTaskIdToTask.get(taskId) != null ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            }
            return z;
        }

        public void startFreezingScreen(int exitAnim, int enterAnim) {
            if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    if (!this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = SCREENSHOT_FORCE_565;
                        long origId = Binder.clearCallingIdentity();
                        try {
                            startFreezingDisplayLocked(PROFILE_ORIENTATION, exitAnim, enterAnim);
                            this.mH.removeMessages(30);
                            this.mH.sendEmptyMessageDelayed(30, 5000);
                            Binder.restoreCallingIdentity(origId);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                }
                return;
            }
            throw new SecurityException("Requires FREEZE_SCREEN permission");
        }

        public void stopFreezingScreen() {
            if (checkCallingPermission("android.permission.FREEZE_SCREEN", "stopFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    if (this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = PROFILE_ORIENTATION;
                        this.mLastFinishedFreezeSource = "client";
                        long origId = Binder.clearCallingIdentity();
                        try {
                            stopFreezingDisplayLocked();
                            Binder.restoreCallingIdentity(origId);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                }
                return;
            }
            throw new SecurityException("Requires FREEZE_SCREEN permission");
        }

        public void disableKeyguard(IBinder token, String tag) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            } else if (Binder.getCallingUid() != TYPE_LAYER_OFFSET && isKeyguardSecure()) {
                Log.d("WindowManager", "current mode is SecurityMode, ignore disableKeyguard");
            } else if (Binder.getCallingUserHandle().getIdentifier() != this.mCurrentUserId) {
                Log.d("WindowManager", "non-current user, ignore disableKeyguard");
            } else if (token == null) {
                throw new IllegalArgumentException("token == null");
            } else {
                this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(WINDOWS_FREEZING_SCREENS_ACTIVE, new Pair(token, tag)));
            }
        }

        public void reenableKeyguard(IBinder token) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            } else if (token == null) {
                throw new IllegalArgumentException("token == null");
            } else {
                this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(WINDOWS_FREEZING_SCREENS_TIMEOUT, token));
            }
        }

        public void exitKeyguardSecurely(IOnKeyguardExitResult callback) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            } else if (callback == null) {
                throw new IllegalArgumentException("callback == null");
            } else {
                this.mPolicy.exitKeyguardSecurely(new AnonymousClass7(callback));
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

        public void dismissKeyguard() {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            }
            synchronized (this.mWindowMap) {
                this.mPolicy.dismissKeyguardLw();
            }
        }

        public void keyguardGoingAway(int flags) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            }
            synchronized (this.mWindowMap) {
                this.mAnimator.mKeyguardGoingAway = SCREENSHOT_FORCE_565;
                this.mAnimator.mKeyguardGoingAwayFlags = flags;
                this.mWindowPlacerLocked.requestTraversal();
            }
        }

        public void keyguardWaitingForActivityDrawn() {
            synchronized (this.mWindowMap) {
                this.mKeyguardWaitingForActivityDrawn = SCREENSHOT_FORCE_565;
            }
        }

        public void notifyActivityDrawnForKeyguard() {
            synchronized (this.mWindowMap) {
                if (this.mKeyguardWaitingForActivityDrawn) {
                    this.mPolicy.notifyActivityDrawnForKeyguardLw();
                    this.mKeyguardWaitingForActivityDrawn = PROFILE_ORIENTATION;
                }
            }
        }

        void showGlobalActions() {
            this.mPolicy.showGlobalActions();
        }

        public void closeSystemDialogs(String reason) {
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = WINDOW_ANIMATION_SCALE; winNdx < numWindows; winNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        WindowState w = (WindowState) windows.get(winNdx);
                        if (w.mHasSurface) {
                            try {
                                w.mClient.closeSystemDialogs(reason);
                            } catch (RemoteException e) {
                            }
                        }
                    }
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
                    case WINDOW_ANIMATION_SCALE /*0*/:
                        this.mWindowAnimationScaleSetting = scale;
                        break;
                    case WINDOWS_FREEZING_SCREENS_ACTIVE /*1*/:
                        this.mTransitionAnimationScaleSetting = scale;
                        break;
                    case WINDOWS_FREEZING_SCREENS_TIMEOUT /*2*/:
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
                    if (scales.length >= WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        this.mWindowAnimationScaleSetting = fixScale(scales[WINDOW_ANIMATION_SCALE]);
                    }
                    if (scales.length >= WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                        this.mTransitionAnimationScaleSetting = fixScale(scales[WINDOWS_FREEZING_SCREENS_ACTIVE]);
                    }
                    if (scales.length >= UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                        this.mAnimatorDurationScaleSetting = fixScale(scales[WINDOWS_FREEZING_SCREENS_TIMEOUT]);
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
                case WINDOW_ANIMATION_SCALE /*0*/:
                    return this.mWindowAnimationScaleSetting;
                case WINDOWS_FREEZING_SCREENS_ACTIVE /*1*/:
                    return this.mTransitionAnimationScaleSetting;
                case WINDOWS_FREEZING_SCREENS_TIMEOUT /*2*/:
                    return this.mAnimatorDurationScaleSetting;
                default:
                    return 0.0f;
            }
        }

        public float[] getAnimationScales() {
            float[] fArr = new float[UPDATE_FOCUS_WILL_PLACE_SURFACES];
            fArr[WINDOW_ANIMATION_SCALE] = this.mWindowAnimationScaleSetting;
            fArr[WINDOWS_FREEZING_SCREENS_ACTIVE] = this.mTransitionAnimationScaleSetting;
            fArr[WINDOWS_FREEZING_SCREENS_TIMEOUT] = this.mAnimatorDurationScaleSetting;
            return fArr;
        }

        public float getCurrentAnimatorScale() {
            float f;
            synchronized (this.mWindowMap) {
                f = this.mAnimationsDisabled ? 0.0f : this.mAnimatorDurationScaleSetting;
            }
            return f;
        }

        void dispatchNewAnimatorScaleLocked(Session session) {
            this.mH.obtainMessage(34, session).sendToTarget();
        }

        public void registerPointerEventListener(PointerEventListener listener) {
            this.mPointerEventDispatcher.registerInputEventListener(listener);
        }

        public void unregisterPointerEventListener(PointerEventListener listener) {
            this.mPointerEventDispatcher.unregisterInputEventListener(listener);
        }

        public int getLidState() {
            int sw = this.mInputManager.getSwitchState(-1, -256, WINDOW_ANIMATION_SCALE);
            if (sw > 0) {
                return WINDOW_ANIMATION_SCALE;
            }
            if (sw == 0) {
                return WINDOWS_FREEZING_SCREENS_ACTIVE;
            }
            return -1;
        }

        public void lockDeviceNow() {
            lockNow(null);
        }

        public int getCameraLensCoverState() {
            int sw = this.mInputManager.getSwitchState(-1, -256, 9);
            if (sw > 0) {
                return WINDOWS_FREEZING_SCREENS_ACTIVE;
            }
            return sw == 0 ? WINDOW_ANIMATION_SCALE : -1;
        }

        public void switchInputMethod(boolean forwardDirection) {
            InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
            if (inputMethodManagerInternal != null) {
                inputMethodManagerInternal.switchInputMethod(forwardDirection);
            }
        }

        public void shutdown(boolean confirm) {
            ShutdownThread.shutdown(this.mContext, "userrequested", confirm);
        }

        public void rebootSafeMode(boolean confirm) {
            ShutdownThread.rebootSafeMode(this.mContext, confirm);
        }

        public void setCurrentProfileIds(int[] currentProfileIds) {
            synchronized (this.mWindowMap) {
                this.mCurrentProfileIds = currentProfileIds;
            }
        }

        public void setCurrentUser(int newUserId, int[] currentProfileIds) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent;
                this.mCurrentUserId = newUserId;
                this.mCurrentProfileIds = currentProfileIds;
                this.mAppTransition.setCurrentUser(newUserId);
                this.mPolicy.setCurrentUserLw(newUserId);
                this.mPolicy.enableKeyguard(SCREENSHOT_FORCE_565);
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                    displayContent.switchUserStacks();
                    rebuildAppWindowListLocked(displayContent);
                }
                this.mWindowPlacerLocked.performSurfacePlacement();
                displayContent = getDefaultDisplayContentLocked();
                displayContent.mDividerControllerLocked.notifyDockedStackExistsChanged(hasDockedTasksForUser(newUserId));
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
            }
        }

        boolean hasDockedTasksForUser(int userId) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(UPDATE_FOCUS_WILL_PLACE_SURFACES);
            if (stack == null) {
                return PROFILE_ORIENTATION;
            }
            ArrayList<Task> tasks = stack.getTasks();
            boolean hasUserTask = PROFILE_ORIENTATION;
            for (int i = tasks.size() - 1; i >= 0 && !hasUserTask; i--) {
                hasUserTask = ((Task) tasks.get(i)).mUserId == userId ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            }
            return hasUserTask;
        }

        boolean isCurrentProfileLocked(int userId) {
            if (userId == this.mCurrentUserId) {
                return SCREENSHOT_FORCE_565;
            }
            for (int i = WINDOW_ANIMATION_SCALE; i < this.mCurrentProfileIds.length; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                if (this.mCurrentProfileIds[i] == userId) {
                    return SCREENSHOT_FORCE_565;
                }
            }
            return PROFILE_ORIENTATION;
        }

        public void enableScreenAfterBoot() {
            synchronized (this.mWindowMap) {
                if (this.mSystemBooted) {
                    return;
                }
                this.mSystemBooted = SCREENSHOT_FORCE_565;
                hideBootMessagesLocked();
                this.mH.sendEmptyMessageDelayed(23, 30000);
                this.mPolicy.systemBooted();
                performEnableScreen();
            }
        }

        public void enableScreenIfNeeded() {
            synchronized (this.mWindowMap) {
                enableScreenIfNeededLocked();
            }
        }

        void enableScreenIfNeededLocked() {
            if (!this.mDisplayEnabled) {
                if (this.mSystemBooted || this.mShowingBootMessages) {
                    this.mH.sendEmptyMessage(16);
                }
            }
        }

        public void performBootTimeout() {
            synchronized (this.mWindowMap) {
                if (this.mDisplayEnabled) {
                    return;
                }
                Slog.w("WindowManager", "***** BOOT TIMEOUT: forcing display enabled");
                this.mForceDisplayEnabled = SCREENSHOT_FORCE_565;
                performEnableScreen();
            }
        }

        private boolean checkWaitingForWindowsLocked() {
            boolean haveBootMsg = PROFILE_ORIENTATION;
            boolean haveApp = PROFILE_ORIENTATION;
            boolean haveWallpaper = PROFILE_ORIENTATION;
            boolean wallpaperEnabled = this.mContext.getResources().getBoolean(17956944) ? this.mOnlyCore ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            boolean haveKeyguard = this.mBootAnimationStopped;
            WindowList windows = getDefaultWindowListLocked();
            int N = windows.size();
            for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                WindowState w = (WindowState) windows.get(i);
                if (w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
                    return SCREENSHOT_FORCE_565;
                }
                if (w.isDrawnLw()) {
                    if (w.mAttrs.type == 2021) {
                        haveBootMsg = SCREENSHOT_FORCE_565;
                    } else if (w.mAttrs.type == WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                        haveApp = SCREENSHOT_FORCE_565;
                    } else if (w.mAttrs.type == 2013) {
                        haveWallpaper = SCREENSHOT_FORCE_565;
                    } else if (w.mAttrs.type == WINDOW_REPLACEMENT_TIMEOUT_DURATION) {
                        haveKeyguard = this.mPolicy.isKeyguardDrawnLw();
                    }
                }
            }
            if (!this.mSystemBooted && !haveBootMsg) {
                return SCREENSHOT_FORCE_565;
            }
            if (!this.mSystemBooted || ((haveApp || haveKeyguard) && (!wallpaperEnabled || haveWallpaper))) {
                return PROFILE_ORIENTATION;
            }
            return SCREENSHOT_FORCE_565;
        }

        public void performEnableScreen() {
            synchronized (this.mWindowMap) {
                if (this.mDisplayEnabled) {
                } else if (!this.mSystemBooted && !this.mShowingBootMessages) {
                } else if (this.mForceDisplayEnabled || !checkWaitingForWindowsLocked()) {
                    if (!this.mBootAnimationStopped) {
                        Trace.asyncTraceBegin(32, "Stop bootanim", WINDOW_ANIMATION_SCALE);
                        try {
                            IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
                            if (surfaceFlinger != null) {
                                Flog.i(304, "******* TELLING SURFACE FLINGER WE ARE BOOTED!");
                                Parcel data = Parcel.obtain();
                                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                                surfaceFlinger.transact(WINDOWS_FREEZING_SCREENS_ACTIVE, data, null, WINDOW_ANIMATION_SCALE);
                                data.recycle();
                            }
                        } catch (RemoteException e) {
                            Slog.e("WindowManager", "Boot completed: SurfaceFlinger is dead!");
                        }
                        this.mBootAnimationStopped = SCREENSHOT_FORCE_565;
                    }
                    if (this.mForceDisplayEnabled || checkBootAnimationCompleteLocked()) {
                        EventLog.writeEvent(EventLogTags.WM_BOOT_ANIMATION_DONE, SystemClock.uptimeMillis());
                        Trace.asyncTraceEnd(32, "Stop bootanim", WINDOW_ANIMATION_SCALE);
                        this.mDisplayEnabled = SCREENSHOT_FORCE_565;
                        this.mInputMonitor.setEventDispatchingLw(this.mEventDispatchingEnabled);
                        try {
                            this.mActivityManager.bootAnimationComplete();
                        } catch (RemoteException e2) {
                        }
                        this.mPolicy.enableScreenAfterBoot();
                        updateRotationUnchecked(PROFILE_ORIENTATION, PROFILE_ORIENTATION);
                        return;
                    }
                }
            }
        }

        private boolean checkBootAnimationCompleteLocked() {
            if (!SystemService.isRunning(BOOT_ANIMATION_SERVICE)) {
                return SCREENSHOT_FORCE_565;
            }
            this.mH.removeMessages(37);
            this.mH.sendEmptyMessageDelayed(37, 200);
            return PROFILE_ORIENTATION;
        }

        public void showBootMessage(CharSequence msg, boolean always) {
            boolean first = PROFILE_ORIENTATION;
            synchronized (this.mWindowMap) {
                if (this.mAllowBootMessages) {
                    if (!this.mShowingBootMessages) {
                        if (always) {
                            first = SCREENSHOT_FORCE_565;
                        } else {
                            return;
                        }
                    }
                    if (this.mSystemBooted) {
                        return;
                    }
                    this.mShowingBootMessages = SCREENSHOT_FORCE_565;
                    this.mPolicy.showBootMessage(msg, always);
                    if (first) {
                        performEnableScreen();
                    }
                    return;
                }
            }
        }

        public void hideBootMessagesLocked() {
            if (this.mShowingBootMessages) {
                this.mShowingBootMessages = PROFILE_ORIENTATION;
                this.mPolicy.hideBootMessages();
            }
        }

        public void setInTouchMode(boolean mode) {
            synchronized (this.mWindowMap) {
                this.mInTouchMode = mode;
            }
        }

        private void updateCircularDisplayMaskIfNeeded() {
            if (this.mContext.getResources().getConfiguration().isScreenRound() && this.mContext.getResources().getBoolean(17957001)) {
                int currentUserId;
                synchronized (this.mWindowMap) {
                    currentUserId = this.mCurrentUserId;
                }
                int showMask = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_inversion_enabled", WINDOW_ANIMATION_SCALE, currentUserId) == WINDOWS_FREEZING_SCREENS_ACTIVE ? WINDOW_ANIMATION_SCALE : WINDOWS_FREEZING_SCREENS_ACTIVE;
                Message m = this.mH.obtainMessage(35);
                m.arg1 = showMask;
                this.mH.sendMessage(m);
            }
        }

        public void showEmulatorDisplayOverlayIfNeeded() {
            if (this.mContext.getResources().getBoolean(17957002) && SystemProperties.getBoolean(PROPERTY_EMULATOR_CIRCULAR, PROFILE_ORIENTATION) && Build.IS_EMULATOR) {
                this.mH.sendMessage(this.mH.obtainMessage(36));
            }
        }

        public void showCircularMask(boolean visible) {
            synchronized (this.mWindowMap) {
                SurfaceControl.openTransaction();
                if (visible) {
                    try {
                        if (this.mCircularDisplayMask == null) {
                            this.mCircularDisplayMask = new CircularDisplayMask(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.windowTypeToLayerLw(2018) * TYPE_LAYER_MULTIPLIER) + 10, this.mContext.getResources().getInteger(17694869), this.mContext.getResources().getDimensionPixelSize(17105050));
                        }
                        this.mCircularDisplayMask.setVisibility(SCREENSHOT_FORCE_565);
                    } catch (Throwable th) {
                        SurfaceControl.closeTransaction();
                    }
                } else if (this.mCircularDisplayMask != null) {
                    this.mCircularDisplayMask.setVisibility(PROFILE_ORIENTATION);
                    this.mCircularDisplayMask = null;
                }
                SurfaceControl.closeTransaction();
            }
        }

        public void showEmulatorDisplayOverlay() {
            synchronized (this.mWindowMap) {
                SurfaceControl.openTransaction();
                try {
                    if (this.mEmulatorDisplayOverlay == null) {
                        this.mEmulatorDisplayOverlay = new EmulatorDisplayOverlay(this.mContext, getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.windowTypeToLayerLw(2018) * TYPE_LAYER_MULTIPLIER) + 10);
                    }
                    this.mEmulatorDisplayOverlay.setVisibility(SCREENSHOT_FORCE_565);
                    SurfaceControl.closeTransaction();
                } catch (Throwable th) {
                    SurfaceControl.closeTransaction();
                }
            }
        }

        public void showStrictModeViolation(boolean on) {
            this.mH.sendMessage(this.mH.obtainMessage(25, on ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE, Binder.getCallingPid()));
        }

        private void showStrictModeViolation(int arg, int pid) {
            boolean on = arg != 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            synchronized (this.mWindowMap) {
                if (on) {
                    boolean isVisible = PROFILE_ORIENTATION;
                    int numDisplays = this.mDisplayContents.size();
                    for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                        int numWindows = windows.size();
                        for (int winNdx = WINDOW_ANIMATION_SCALE; winNdx < numWindows; winNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                            WindowState ws = (WindowState) windows.get(winNdx);
                            if (ws.mSession.mPid == pid && ws.isVisibleLw()) {
                                isVisible = SCREENSHOT_FORCE_565;
                                break;
                            }
                        }
                    }
                    if (!isVisible) {
                        return;
                    }
                }
                SurfaceControl.openTransaction();
                try {
                    if (this.mStrictModeFlash == null) {
                        this.mStrictModeFlash = new StrictModeFlash(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession);
                    }
                    this.mStrictModeFlash.setVisibility(on);
                    SurfaceControl.closeTransaction();
                } catch (Throwable th) {
                    SurfaceControl.closeTransaction();
                }
            }
        }

        public void setStrictModeVisualIndicatorPreference(String value) {
            SystemProperties.set("persist.sys.strictmode.visual", value);
        }

        private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
            int tmp;
            if (rot == WINDOWS_FREEZING_SCREENS_ACTIVE) {
                tmp = crop.top;
                crop.top = dw - crop.right;
                crop.right = crop.bottom;
                crop.bottom = dw - crop.left;
                crop.left = tmp;
            } else if (rot == WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                tmp = crop.top;
                crop.top = dh - crop.bottom;
                crop.bottom = dh - tmp;
                tmp = crop.right;
                crop.right = dw - crop.left;
                crop.left = dw - tmp;
            } else if (rot == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                tmp = crop.top;
                crop.top = crop.left;
                crop.left = dh - crop.bottom;
                crop.bottom = crop.right;
                crop.right = dh - tmp;
            }
        }

        public boolean requestAssistScreenshot(IAssistScreenshotReceiver receiver) {
            if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "requestAssistScreenshot()")) {
                FgThread.getHandler().post(new AnonymousClass8(receiver));
                return SCREENSHOT_FORCE_565;
            }
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }

        public Bitmap screenshotApplications(IBinder appToken, int displayId, int width, int height, float frameScale) {
            if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotApplications()")) {
                try {
                    Trace.traceBegin(32, "screenshotApplications");
                    Bitmap screenshotApplicationsInner = screenshotApplicationsInner(appToken, displayId, width, height, PROFILE_ORIENTATION, frameScale, Config.RGB_565);
                    return screenshotApplicationsInner;
                } finally {
                    Trace.traceEnd(32);
                }
            } else {
                throw new SecurityException("Requires READ_FRAME_BUFFER permission");
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Bitmap screenshotApplicationsInner(IBinder appToken, int displayId, int width, int height, boolean includeFullDisplay, float frameScale, Config config) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null) {
                    return null;
                }
                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                int dw = displayInfo.logicalWidth;
                int dh = displayInfo.logicalHeight;
                if (dw == 0 || dh == 0) {
                    return null;
                }
                boolean screenshotReady;
                int minLayer;
                int maxLayer = WINDOW_ANIMATION_SCALE;
                Rect frame = new Rect();
                Rect stackBounds = new Rect();
                if (appToken == null) {
                    screenshotReady = SCREENSHOT_FORCE_565;
                    minLayer = WINDOW_ANIMATION_SCALE;
                } else {
                    screenshotReady = PROFILE_ORIENTATION;
                    minLayer = Integer.MAX_VALUE;
                }
                synchronized (this.mWindowMap) {
                    AppWindowToken appWindowToken = this.mInputMethodTarget != null ? this.mInputMethodTarget.mAppToken : null;
                    boolean includeImeInScreenshot = (appWindowToken == null || appWindowToken.appToken == null || appWindowToken.appToken.asBinder() != appToken) ? PROFILE_ORIENTATION : this.mInputMethodTarget.isInMultiWindowMode() ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
                }
                int aboveAppLayer = ((this.mPolicy.windowTypeToLayerLw(WINDOWS_FREEZING_SCREENS_TIMEOUT) + WINDOWS_FREEZING_SCREENS_ACTIVE) * TYPE_LAYER_MULTIPLIER) + TYPE_LAYER_OFFSET;
                synchronized (this.mWindowMap) {
                    WindowState appWin = null;
                    WindowList windows = displayContent.getWindowList();
                    for (int i = windows.size() - 1; i >= 0; i--) {
                        int rot;
                        int tmp;
                        WindowState ws = (WindowState) windows.get(i);
                        if (ws.mHasSurface) {
                            if (ws.mLayer >= aboveAppLayer) {
                                continue;
                            } else {
                                if (ws.mIsImWindow) {
                                    if (!includeImeInScreenshot) {
                                        continue;
                                    }
                                } else if (ws.mIsWallpaper) {
                                    if (appWin == null) {
                                    }
                                } else if (appToken != null) {
                                    if (ws.mAppToken != null && ws.mAppToken.token == appToken) {
                                        appWin = ws;
                                    }
                                }
                                WindowStateAnimator winAnim = ws.mWinAnimator;
                                int layer = winAnim.mSurfaceController.getLayer();
                                if (maxLayer < layer) {
                                    maxLayer = layer;
                                }
                                if (minLayer > layer) {
                                    minLayer = layer;
                                }
                                if (!(includeFullDisplay || ws.mIsWallpaper)) {
                                    Rect wf = ws.mFrame;
                                    Rect cr = ws.mContentInsets;
                                    frame.union(wf.left + cr.left, wf.top + cr.top, wf.right - cr.right, wf.bottom - cr.bottom);
                                    ws.getVisibleBounds(stackBounds);
                                    if (!Rect.intersects(frame, stackBounds)) {
                                        frame.setEmpty();
                                    }
                                }
                                if (ws.mAppToken != null && ws.mAppToken.token == appToken && ws.isDisplayedLw() && winAnim.getShown()) {
                                    screenshotReady = SCREENSHOT_FORCE_565;
                                }
                                if (ws.isObscuringFullscreen(displayInfo)) {
                                    if (!ws.mIsWallpaper) {
                                    }
                                    if (appToken == null && appWin == null) {
                                        return null;
                                    } else if (!screenshotReady) {
                                        Slog.i("WindowManager", "Failed to capture screenshot of " + appToken + " appWin=" + (appWin != null ? "null" : appWin + " drawState=" + appWin.mWinAnimator.mDrawState));
                                        return null;
                                    } else if (maxLayer != 0) {
                                        return null;
                                    } else {
                                        rot = getDefaultDisplayContentLocked().getDisplay().getRotation();
                                        if (rot == WINDOWS_FREEZING_SCREENS_ACTIVE || rot == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                            tmp = width;
                                            width = height;
                                            height = tmp;
                                        }
                                        if (!includeFullDisplay) {
                                            frame.set(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, dw, dh);
                                        } else if (!frame.intersect(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, dw, dh)) {
                                            frame.setEmpty();
                                        }
                                        if (frame.isEmpty()) {
                                            if (width < 0) {
                                                width = (int) (((float) frame.width()) * frameScale);
                                            }
                                            if (height < 0) {
                                                height = (int) (((float) frame.height()) * frameScale);
                                            }
                                            if (width > 0 || height <= 0) {
                                                Slog.i(TAG, "Taking screenshot with width and height must be > 0");
                                                return null;
                                            }
                                            boolean isAnimating;
                                            Rect crop = new Rect(frame);
                                            if (this.mLazyModeOn != 0) {
                                                setCropOnSingleHandMode(this.mLazyModeOn, PROFILE_ORIENTATION, dw, dh, crop);
                                            } else {
                                                if (((float) width) / ((float) frame.width()) < ((float) height) / ((float) frame.height())) {
                                                    crop.right = crop.left + ((int) ((((float) width) / ((float) height)) * ((float) frame.height())));
                                                    if (crop.right < frame.width()) {
                                                        crop.right = frame.width();
                                                    }
                                                } else {
                                                    crop.bottom = crop.top + ((int) ((((float) height) / ((float) width)) * ((float) frame.width())));
                                                    if (crop.bottom < crop.top + frame.height()) {
                                                        crop.bottom = crop.top + frame.height();
                                                    }
                                                }
                                            }
                                            if (rot == WINDOWS_FREEZING_SCREENS_ACTIVE || rot == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                                rot = rot == WINDOWS_FREEZING_SCREENS_ACTIVE ? UPDATE_FOCUS_WILL_PLACE_SURFACES : WINDOWS_FREEZING_SCREENS_ACTIVE;
                                            }
                                            convertCropForSurfaceFlinger(crop, rot, dw, dh);
                                            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(WINDOW_ANIMATION_SCALE);
                                            if (screenRotationAnimation != null) {
                                                isAnimating = screenRotationAnimation.isAnimating();
                                            } else {
                                                isAnimating = PROFILE_ORIENTATION;
                                            }
                                            Slog.i(TAG, "Taking screenshot from Surface with crop:[" + crop + "], width:[" + width + "], height:[" + height + "], minLayer:[" + minLayer + "], maxLayer:[" + maxLayer + "], inRotation:[" + isAnimating + "], rot:[" + rot + "]");
                                            SurfaceControl.openTransaction();
                                            SurfaceControl.closeTransactionSync();
                                            Bitmap bm = SurfaceControl.screenshot(crop, width, height, minLayer, maxLayer, isAnimating, rot);
                                            if (bm == null) {
                                                Slog.w("WindowManager", "Screenshot failure taking screenshot for (" + dw + "x" + dh + ") to layer " + maxLayer);
                                                return null;
                                            }
                                            if (rot == WINDOWS_FREEZING_SCREENS_ACTIVE || rot == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                                                tmp = width;
                                                width = height;
                                                height = tmp;
                                            }
                                            Bitmap ret = Bitmap.createBitmap(width, height, Config.RGB_565);
                                            Matrix matrix = new Matrix();
                                            hwProcessOnMatrix(rot, width, height, frame, matrix);
                                            Canvas canvas = new Canvas(ret);
                                            canvas.drawColor(UsbAudioDevice.kAudioDeviceMetaMask);
                                            canvas.drawBitmap(bm, matrix, null);
                                            canvas.setBitmap(null);
                                            bm.recycle();
                                            return ret;
                                        }
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                    if (appToken == null) {
                    }
                    if (!screenshotReady) {
                        if (appWin != null) {
                        }
                        Slog.i("WindowManager", "Failed to capture screenshot of " + appToken + " appWin=" + (appWin != null ? "null" : appWin + " drawState=" + appWin.mWinAnimator.mDrawState));
                        return null;
                    } else if (maxLayer != 0) {
                        rot = getDefaultDisplayContentLocked().getDisplay().getRotation();
                        tmp = width;
                        width = height;
                        height = tmp;
                        if (!includeFullDisplay) {
                            frame.set(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, dw, dh);
                        } else if (frame.intersect(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, dw, dh)) {
                            frame.setEmpty();
                        }
                        if (frame.isEmpty()) {
                            if (width < 0) {
                                width = (int) (((float) frame.width()) * frameScale);
                            }
                            if (height < 0) {
                                height = (int) (((float) frame.height()) * frameScale);
                            }
                            if (width > 0) {
                            }
                            Slog.i(TAG, "Taking screenshot with width and height must be > 0");
                            return null;
                        }
                        return null;
                    } else {
                        return null;
                    }
                }
            }
        }

        public void freezeRotation(int rotation) {
            if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
                throw new SecurityException("Requires SET_ORIENTATION permission");
            } else if (rotation < -1 || rotation > UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
            } else {
                long origId = Binder.clearCallingIdentity();
                try {
                    WindowManagerPolicy windowManagerPolicy = this.mPolicy;
                    if (rotation == -1) {
                        rotation = this.mRotation;
                    }
                    windowManagerPolicy.setUserRotationMode(WINDOWS_FREEZING_SCREENS_ACTIVE, rotation);
                    updateRotationUnchecked(PROFILE_ORIENTATION, PROFILE_ORIENTATION);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }

        public void thawRotation() {
            if (checkCallingPermission("android.permission.SET_ORIENTATION", "thawRotation()")) {
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mPolicy.setUserRotationMode(WINDOW_ANIMATION_SCALE, 777);
                    updateRotationUnchecked(PROFILE_ORIENTATION, PROFILE_ORIENTATION);
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
            this.mDeferredRotationPauseCount += WINDOWS_FREEZING_SCREENS_ACTIVE;
        }

        void resumeRotationLocked() {
            if (this.mDeferredRotationPauseCount > 0) {
                this.mDeferredRotationPauseCount--;
                if (this.mDeferredRotationPauseCount == 0 && updateRotationUncheckedLocked(PROFILE_ORIENTATION)) {
                    this.mH.sendEmptyMessage(18);
                }
            }
        }

        public void updateRotationUnchecked(boolean alwaysSendConfiguration, boolean forceRelayout) {
            Slog.i("WindowManager", "updateRotationUnchecked(alwaysSendConfiguration=" + alwaysSendConfiguration + ")");
            long origId = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                boolean changed = updateRotationUncheckedLocked(PROFILE_ORIENTATION);
                if (changed) {
                    LogPower.push(DumpState.DUMP_PACKAGES);
                }
                if (changed) {
                    if (this.mPerfHub == null) {
                        this.mPerfHub = new PerfHub();
                    }
                    if (this.mPerfHub != null) {
                        this.mIsPerfBoost = SCREENSHOT_FORCE_565;
                        int[] iArr = new int[WINDOWS_FREEZING_SCREENS_ACTIVE];
                        iArr[WINDOW_ANIMATION_SCALE] = WINDOWS_FREEZING_SCREENS_ACTIVE;
                        this.mPerfHub.perfEvent(6, "", iArr);
                    }
                }
                if (!changed || forceRelayout) {
                    getDefaultDisplayContentLocked().layoutNeeded = SCREENSHOT_FORCE_565;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
                if (changed) {
                    if (this.mLastFinishedFreezeSource != null) {
                        Jlog.d(58, "" + this.mLastFinishedFreezeSource);
                    } else {
                        Jlog.d(58, "");
                    }
                }
            }
            if (changed || alwaysSendConfiguration) {
                sendNewConfiguration();
            }
            Binder.restoreCallingIdentity(origId);
        }

        public boolean updateRotationUncheckedLocked(boolean inTransaction) {
            if (this.mDeferredRotationPauseCount > 0) {
                Slog.i("WindowManager", "Deferring rotation, rotation is paused.");
                return PROFILE_ORIENTATION;
            }
            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(WINDOW_ANIMATION_SCALE);
            if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                return PROFILE_ORIENTATION;
            }
            if (!this.mDisplayEnabled) {
                return PROFILE_ORIENTATION;
            }
            int rotation = this.mPolicy.rotationForOrientationLw(this.mForcedAppOrientation, this.mRotation);
            boolean altOrientation = this.mPolicy.rotationHasCompatibleMetricsLw(this.mForcedAppOrientation, rotation) ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
            Slog.i("WindowManager", "Application requested orientation " + this.mForcedAppOrientation + ", got rotation " + rotation + " which has " + (altOrientation ? "incompatible" : "compatible") + " metrics");
            if (this.mRotation == rotation && this.mAltOrientation == altOrientation) {
                return PROFILE_ORIENTATION;
            }
            int i;
            Slog.i("WindowManager", "Rotation changed to " + rotation + (altOrientation ? " (alt)" : "") + " from " + this.mRotation + (this.mAltOrientation ? " (alt)" : "") + ", forceApp=" + this.mForcedAppOrientation);
            this.mRotation = rotation;
            this.mAltOrientation = altOrientation;
            this.mPolicy.setRotationLw(this.mRotation);
            this.mWindowsFreezingScreen = WINDOWS_FREEZING_SCREENS_ACTIVE;
            this.mH.removeMessages(11);
            this.mH.sendEmptyMessageDelayed(11, 2000);
            this.mWaitingForConfig = SCREENSHOT_FORCE_565;
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
            int[] anim = new int[WINDOWS_FREEZING_SCREENS_TIMEOUT];
            if (displayContent.isDimming()) {
                anim[WINDOWS_FREEZING_SCREENS_ACTIVE] = WINDOW_ANIMATION_SCALE;
                anim[WINDOW_ANIMATION_SCALE] = WINDOW_ANIMATION_SCALE;
            } else {
                this.mPolicy.selectRotationAnimationLw(anim);
            }
            startFreezingDisplayLocked(inTransaction, anim[WINDOW_ANIMATION_SCALE], anim[WINDOWS_FREEZING_SCREENS_ACTIVE]);
            screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(WINDOW_ANIMATION_SCALE);
            updateDisplayAndOrientationLocked(this.mCurConfiguration.uiMode);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            if (!inTransaction) {
                SurfaceControl.openTransaction();
            }
            if (screenRotationAnimation != null) {
                try {
                    if (screenRotationAnimation.hasScreenshot() && screenRotationAnimation.setRotationInTransaction(rotation, this.mFxSession, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight)) {
                        scheduleAnimationLocked();
                    }
                } catch (RuntimeException e) {
                    Slog.wtf(TAG, "Display exception in Window Manager", e);
                    if (!inTransaction) {
                        SurfaceControl.closeTransaction();
                    }
                } catch (Throwable th) {
                    if (!inTransaction) {
                        SurfaceControl.closeTransaction();
                    }
                }
            }
            this.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
            if (!inTransaction) {
                SurfaceControl.closeTransaction();
            }
            WindowList windows = displayContent.getWindowList();
            for (i = windows.size() - 1; i >= 0; i--) {
                WindowState w = (WindowState) windows.get(i);
                if (w.mAppToken != null) {
                    w.mAppToken.destroySavedSurfaces();
                }
                if (w.mHasSurface) {
                    w.mOrientationChanging = SCREENSHOT_FORCE_565;
                    this.mWindowPlacerLocked.mOrientationChangeComplete = PROFILE_ORIENTATION;
                }
                w.mLastFreezeDuration = WINDOW_ANIMATION_SCALE;
            }
            for (i = this.mRotationWatchers.size() - 1; i >= 0; i--) {
                try {
                    ((RotationWatcher) this.mRotationWatchers.get(i)).watcher.onRotationChanged(rotation);
                } catch (RemoteException e2) {
                }
            }
            if (screenRotationAnimation == null && this.mAccessibilityController != null && displayContent.getDisplayId() == 0) {
                this.mAccessibilityController.onRotationChangedLocked(getDefaultDisplayContentLocked(), rotation);
            }
            return SCREENSHOT_FORCE_565;
        }

        public int getRotation() {
            return this.mRotation;
        }

        public boolean isRotationFrozen() {
            return this.mPolicy.getUserRotationMode() == WINDOWS_FREEZING_SCREENS_ACTIVE ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
        }

        public int watchRotation(IRotationWatcher watcher) {
            int i;
            DeathRecipient dr = new AnonymousClass9(watcher.asBinder());
            synchronized (this.mWindowMap) {
                try {
                    watcher.asBinder().linkToDeath(dr, WINDOW_ANIMATION_SCALE);
                    this.mRotationWatchers.add(new RotationWatcher(watcher, dr));
                } catch (RemoteException e) {
                }
                i = this.mRotation;
            }
            return i;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void removeRotationWatcher(IRotationWatcher watcher) {
            IBinder watcherBinder = watcher.asBinder();
            synchronized (this.mWindowMap) {
                int i = WINDOW_ANIMATION_SCALE;
                while (true) {
                    if (i < this.mRotationWatchers.size()) {
                        if (watcherBinder == ((RotationWatcher) this.mRotationWatchers.get(i)).watcher.asBinder()) {
                            RotationWatcher removed = (RotationWatcher) this.mRotationWatchers.remove(i);
                            IBinder binder = removed.watcher.asBinder();
                            if (binder != null) {
                                binder.unlinkToDeath(removed.deathRecipient, WINDOW_ANIMATION_SCALE);
                            }
                            i--;
                        }
                        i += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                }
            }
        }

        public int getPreferredOptionsPanelGravity() {
            synchronized (this.mWindowMap) {
                int rotation = getRotation();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                if (displayContent.mInitialDisplayWidth < displayContent.mInitialDisplayHeight) {
                    switch (rotation) {
                        case WINDOWS_FREEZING_SCREENS_ACTIVE /*1*/:
                            return 85;
                        case WINDOWS_FREEZING_SCREENS_TIMEOUT /*2*/:
                            return 81;
                        case UPDATE_FOCUS_WILL_PLACE_SURFACES /*3*/:
                            return 8388691;
                        default:
                            return 81;
                    }
                }
                switch (rotation) {
                    case WINDOWS_FREEZING_SCREENS_ACTIVE /*1*/:
                        return 81;
                    case WINDOWS_FREEZING_SCREENS_TIMEOUT /*2*/:
                        return 8388691;
                    case UPDATE_FOCUS_WILL_PLACE_SURFACES /*3*/:
                        return 81;
                    default:
                        return 85;
                }
            }
        }

        public boolean startViewServer(int port) {
            if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "startViewServer") || port < DumpState.DUMP_PROVIDERS) {
                return PROFILE_ORIENTATION;
            }
            if (this.mViewServer != null) {
                if (!this.mViewServer.isRunning()) {
                    try {
                        return this.mViewServer.start();
                    } catch (IOException e) {
                        Slog.w("WindowManager", "View server did not start");
                    }
                }
                return PROFILE_ORIENTATION;
            }
            try {
                this.mViewServer = new ViewServer(this, port);
                return this.mViewServer.start();
            } catch (IOException e2) {
                Slog.w("WindowManager", "View server did not start");
                return PROFILE_ORIENTATION;
            }
        }

        private boolean isSystemSecure() {
            if ("1".equals(SystemProperties.get(SYSTEM_SECURE, "1"))) {
                return "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
            }
            return PROFILE_ORIENTATION;
        }

        public boolean stopViewServer() {
            if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "stopViewServer") || this.mViewServer == null) {
                return PROFILE_ORIENTATION;
            }
            return this.mViewServer.stop();
        }

        public boolean isViewServerRunning() {
            boolean z = PROFILE_ORIENTATION;
            if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "isViewServerRunning")) {
                return PROFILE_ORIENTATION;
            }
            if (this.mViewServer != null) {
                z = this.mViewServer.isRunning();
            }
            return z;
        }

        boolean viewServerListWindows(Socket client) {
            Throwable th;
            if (isSystemSecure()) {
                return PROFILE_ORIENTATION;
            }
            boolean result = SCREENSHOT_FORCE_565;
            WindowList windows = new WindowList();
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    windows.addAll(((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList());
                }
            }
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
                try {
                    int count = windows.size();
                    for (int i = WINDOW_ANIMATION_SCALE; i < count; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        WindowState w = (WindowState) windows.get(i);
                        out.write(Integer.toHexString(System.identityHashCode(w)));
                        out.write(32);
                        out.append(w.mAttrs.getTitle());
                        out.write(10);
                    }
                    out.write("DONE.\n");
                    out.flush();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            result = PROFILE_ORIENTATION;
                        }
                    }
                    bufferedWriter = out;
                } catch (Exception e2) {
                    bufferedWriter = out;
                    result = PROFILE_ORIENTATION;
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e3) {
                            result = PROFILE_ORIENTATION;
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedWriter = out;
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                result = PROFILE_ORIENTATION;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                throw th;
            }
            return result;
        }

        boolean viewServerGetFocusedWindow(Socket client) {
            Throwable th;
            if (isSystemSecure()) {
                return PROFILE_ORIENTATION;
            }
            boolean result = SCREENSHOT_FORCE_565;
            WindowState focusedWindow = getFocusedWindow();
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
                if (focusedWindow != null) {
                    try {
                        out.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                        out.write(32);
                        out.append(focusedWindow.mAttrs.getTitle());
                    } catch (Exception e) {
                        bufferedWriter = out;
                        result = PROFILE_ORIENTATION;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e2) {
                                result = PROFILE_ORIENTATION;
                            }
                        }
                        return result;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedWriter = out;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e3) {
                            }
                        }
                        throw th;
                    }
                }
                out.write(10);
                out.flush();
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                        result = PROFILE_ORIENTATION;
                    }
                }
                bufferedWriter = out;
            } catch (Exception e5) {
                result = PROFILE_ORIENTATION;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                throw th;
            }
            return result;
        }

        boolean viewServerWindowCommand(Socket client, String command, String parameters) {
            Exception e;
            Throwable th;
            if (isSystemSecure()) {
                return PROFILE_ORIENTATION;
            }
            boolean success = SCREENSHOT_FORCE_565;
            Parcel parcel = null;
            Parcel parcel2 = null;
            BufferedWriter bufferedWriter = null;
            try {
                int index = parameters.indexOf(32);
                if (index == -1) {
                    index = parameters.length();
                }
                int hashCode = (int) Long.parseLong(parameters.substring(WINDOW_ANIMATION_SCALE, index), 16);
                if (index < parameters.length()) {
                    parameters = parameters.substring(index + WINDOWS_FREEZING_SCREENS_ACTIVE);
                } else {
                    parameters = "";
                }
                WindowState window = findWindow(hashCode);
                if (window == null) {
                    return PROFILE_ORIENTATION;
                }
                parcel = Parcel.obtain();
                parcel.writeInterfaceToken("android.view.IWindow");
                parcel.writeString(command);
                parcel.writeString(parameters);
                parcel.writeInt(WINDOWS_FREEZING_SCREENS_ACTIVE);
                ParcelFileDescriptor.fromSocket(client).writeToParcel(parcel, WINDOW_ANIMATION_SCALE);
                parcel2 = Parcel.obtain();
                window.mClient.asBinder().transact(WINDOWS_FREEZING_SCREENS_ACTIVE, parcel, parcel2, WINDOW_ANIMATION_SCALE);
                parcel2.readException();
                if (!client.isOutputShutdown()) {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    try {
                        out.write("DONE\n");
                        out.flush();
                        bufferedWriter = out;
                    } catch (Exception e2) {
                        e = e2;
                        bufferedWriter = out;
                        try {
                            Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e);
                            success = PROFILE_ORIENTATION;
                            if (parcel != null) {
                                parcel.recycle();
                            }
                            if (parcel2 != null) {
                                parcel2.recycle();
                            }
                            if (bufferedWriter != null) {
                                try {
                                    bufferedWriter.close();
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
                            if (bufferedWriter != null) {
                                try {
                                    bufferedWriter.close();
                                } catch (IOException e4) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedWriter = out;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        if (bufferedWriter != null) {
                            bufferedWriter.close();
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
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e5) {
                    }
                }
                return success;
            } catch (Exception e6) {
                e = e6;
                Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e);
                success = PROFILE_ORIENTATION;
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return success;
            }
        }

        public void addWindowChangeListener(WindowChangeListener listener) {
            synchronized (this.mWindowMap) {
                this.mWindowChangeListeners.add(listener);
            }
        }

        public void removeWindowChangeListener(WindowChangeListener listener) {
            synchronized (this.mWindowMap) {
                this.mWindowChangeListeners.remove(listener);
            }
        }

        private void notifyWindowsChanged() {
            synchronized (this.mWindowMap) {
                if (this.mWindowChangeListeners.isEmpty()) {
                    return;
                }
                WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                int N = windowChangeListeners.length;
                for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    windowChangeListeners[i].windowsChanged();
                }
            }
        }

        private void notifyFocusChanged() {
            synchronized (this.mWindowMap) {
                if (this.mWindowChangeListeners.isEmpty()) {
                    return;
                }
                WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                int N = windowChangeListeners.length;
                for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    windowChangeListeners[i].focusChanged();
                }
            }
        }

        private WindowState findWindow(int hashCode) {
            if (hashCode == -1) {
                return getFocusedWindow();
            }
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = WINDOW_ANIMATION_SCALE; winNdx < numWindows; winNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        WindowState w = (WindowState) windows.get(winNdx);
                        if (System.identityHashCode(w) == hashCode) {
                            return w;
                        }
                    }
                }
                return null;
            }
        }

        void sendNewConfiguration() {
            try {
                this.mActivityManager.updateConfiguration(null);
            } catch (RemoteException e) {
            }
        }

        public Configuration computeNewConfiguration() {
            Configuration computeNewConfigurationLocked;
            synchronized (this.mWindowMap) {
                computeNewConfigurationLocked = computeNewConfigurationLocked();
            }
            return computeNewConfigurationLocked;
        }

        Configuration computeNewConfigurationLocked() {
            if (!this.mDisplayReady) {
                return null;
            }
            Configuration config = new Configuration();
            config.fontScale = 0.0f;
            computeScreenConfigurationLocked(config);
            return config;
        }

        private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int rotation, int uiMode, int dw, int dh) {
            int width = this.mPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode);
            if (width < displayInfo.smallestNominalAppWidth) {
                displayInfo.smallestNominalAppWidth = width;
            }
            if (width > displayInfo.largestNominalAppWidth) {
                displayInfo.largestNominalAppWidth = width;
            }
            int height = this.mPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode);
            if (height < displayInfo.smallestNominalAppHeight) {
                displayInfo.smallestNominalAppHeight = height;
            }
            if (height > displayInfo.largestNominalAppHeight) {
                displayInfo.largestNominalAppHeight = height;
            }
        }

        private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode) {
            int w = this.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode);
            int h = this.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode);
            int longSize = w;
            int shortSize = h;
            if (w < h) {
                int tmp = w;
                longSize = h;
                shortSize = w;
            }
            return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
        }

        private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
            int unrotDw;
            int unrotDh;
            if (rotated) {
                unrotDw = dh;
                unrotDh = dw;
            } else {
                unrotDw = dw;
                unrotDh = dh;
            }
            displayInfo.smallestNominalAppWidth = 1073741824;
            displayInfo.smallestNominalAppHeight = 1073741824;
            displayInfo.largestNominalAppWidth = WINDOW_ANIMATION_SCALE;
            displayInfo.largestNominalAppHeight = WINDOW_ANIMATION_SCALE;
            adjustDisplaySizeRanges(displayInfo, WINDOW_ANIMATION_SCALE, uiMode, unrotDw, unrotDh);
            adjustDisplaySizeRanges(displayInfo, WINDOWS_FREEZING_SCREENS_ACTIVE, uiMode, unrotDh, unrotDw);
            adjustDisplaySizeRanges(displayInfo, WINDOWS_FREEZING_SCREENS_TIMEOUT, uiMode, unrotDw, unrotDh);
            adjustDisplaySizeRanges(displayInfo, UPDATE_FOCUS_WILL_PLACE_SURFACES, uiMode, unrotDh, unrotDw);
            int sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(Configuration.resetScreenLayout(outConfig.screenLayout), WINDOW_ANIMATION_SCALE, density, unrotDw, unrotDh, uiMode), WINDOWS_FREEZING_SCREENS_ACTIVE, density, unrotDh, unrotDw, uiMode), WINDOWS_FREEZING_SCREENS_TIMEOUT, density, unrotDw, unrotDh, uiMode), UPDATE_FOCUS_WILL_PLACE_SURFACES, density, unrotDh, unrotDw, uiMode);
            outConfig.smallestScreenWidthDp = (int) (((float) displayInfo.smallestNominalAppWidth) / density);
            outConfig.screenLayout = sl;
        }

        private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh) {
            dm.noncompatWidthPixels = this.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode);
            dm.noncompatHeightPixels = this.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode);
            int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, null)) / dm.density) + TaskPositioner.RESIZING_HINT_ALPHA);
            if (curSize == 0 || size < curSize) {
                return size;
            }
            return curSize;
        }

        private int computeCompatSmallestWidth(boolean rotated, int uiMode, DisplayMetrics dm, int dw, int dh) {
            int unrotDw;
            int unrotDh;
            this.mTmpDisplayMetrics.setTo(dm);
            DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
            if (rotated) {
                unrotDw = dh;
                unrotDh = dw;
            } else {
                unrotDw = dw;
                unrotDh = dh;
            }
            return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, uiMode, tmpDm, unrotDw, unrotDh), WINDOWS_FREEZING_SCREENS_ACTIVE, uiMode, tmpDm, unrotDh, unrotDw), WINDOWS_FREEZING_SCREENS_TIMEOUT, uiMode, tmpDm, unrotDw, unrotDh), UPDATE_FOCUS_WILL_PLACE_SURFACES, uiMode, tmpDm, unrotDh, unrotDw);
        }

        DisplayInfo updateDisplayAndOrientationLocked(int uiMode) {
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            boolean rotated = this.mRotation != WINDOWS_FREEZING_SCREENS_ACTIVE ? this.mRotation == UPDATE_FOCUS_WILL_PLACE_SURFACES ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
            int realdw = rotated ? displayContent.mBaseDisplayHeight : displayContent.mBaseDisplayWidth;
            int realdh = rotated ? displayContent.mBaseDisplayWidth : displayContent.mBaseDisplayHeight;
            int dw = realdw;
            int dh = realdh;
            if (this.mAltOrientation) {
                if (realdw > realdh) {
                    int maxw = (int) (((float) realdh) / 1.3f);
                    if (maxw < realdw) {
                        dw = maxw;
                    }
                } else {
                    int maxh = (int) (((float) realdw) / 1.3f);
                    if (maxh < realdh) {
                        dh = maxh;
                    }
                }
            }
            int appWidth = this.mPolicy.getNonDecorDisplayWidth(dw, dh, this.mRotation, uiMode);
            int appHeight = this.mPolicy.getNonDecorDisplayHeight(dw, dh, this.mRotation, uiMode);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            displayInfo.rotation = this.mRotation;
            displayInfo.logicalWidth = dw;
            displayInfo.logicalHeight = dh;
            displayInfo.logicalDensityDpi = displayContent.mBaseDisplayDensity;
            displayInfo.appWidth = appWidth;
            displayInfo.appHeight = appHeight;
            displayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
            displayInfo.getAppMetrics(this.mDisplayMetrics);
            if (displayContent.mDisplayScalingDisabled) {
                displayInfo.flags |= 1073741824;
            } else {
                displayInfo.flags &= -1073741825;
            }
            this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayContent.getDisplayId(), displayInfo);
            displayContent.mBaseDisplayRect.set(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, dw, dh);
            this.mForceCompatibleScreenScale = CompatibilityInfo.computeForceCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
            this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
            return displayInfo;
        }

        void computeScreenConfigurationLocked(Configuration config) {
            int i;
            DisplayInfo displayInfo = updateDisplayAndOrientationLocked(config.uiMode);
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            if (dw <= dh) {
                i = WINDOWS_FREEZING_SCREENS_ACTIVE;
            } else {
                i = WINDOWS_FREEZING_SCREENS_TIMEOUT;
            }
            config.orientation = i;
            config.screenWidthDp = (int) (((float) this.mPolicy.getConfigDisplayWidth(dw, dh, this.mRotation, config.uiMode)) / this.mDisplayMetrics.density);
            config.screenHeightDp = (int) (((float) this.mPolicy.getConfigDisplayHeight(dw, dh, this.mRotation, config.uiMode)) / this.mDisplayMetrics.density);
            boolean rotated = this.mRotation != WINDOWS_FREEZING_SCREENS_ACTIVE ? this.mRotation == UPDATE_FOCUS_WILL_PLACE_SURFACES ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : SCREENSHOT_FORCE_565;
            computeSizeRangesAndScreenLayout(displayInfo, rotated, config.uiMode, dw, dh, this.mDisplayMetrics.density, config);
            int i2 = config.screenLayout & -769;
            if ((displayInfo.flags & 16) != 0) {
                i = DumpState.DUMP_MESSAGES;
            } else {
                i = DumpState.DUMP_SHARED_USERS;
            }
            config.screenLayout = i | i2;
            config.compatScreenWidthDp = (int) (((float) config.screenWidthDp) / this.mCompatibleScreenScale);
            config.compatScreenHeightDp = (int) (((float) config.screenHeightDp) / this.mCompatibleScreenScale);
            config.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated, config.uiMode, this.mDisplayMetrics, dw, dh);
            config.densityDpi = displayInfo.logicalDensityDpi;
            config.touchscreen = WINDOWS_FREEZING_SCREENS_ACTIVE;
            config.keyboard = WINDOWS_FREEZING_SCREENS_ACTIVE;
            config.navigation = WINDOWS_FREEZING_SCREENS_ACTIVE;
            int keyboardPresence = WINDOW_ANIMATION_SCALE;
            int navigationPresence = WINDOW_ANIMATION_SCALE;
            InputDevice[] devices = this.mInputManager.getInputDevices();
            int len = devices.length;
            for (int i3 = WINDOW_ANIMATION_SCALE; i3 < len; i3 += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                InputDevice device = devices[i3];
                if (!device.isVirtual()) {
                    int presenceFlag;
                    int sources = device.getSources();
                    if (device.isExternal()) {
                        presenceFlag = WINDOWS_FREEZING_SCREENS_TIMEOUT;
                    } else {
                        presenceFlag = WINDOWS_FREEZING_SCREENS_ACTIVE;
                    }
                    if (!this.mIsTouchDevice) {
                        config.touchscreen = WINDOWS_FREEZING_SCREENS_ACTIVE;
                    } else if ((sources & 4098) == 4098) {
                        config.touchscreen = UPDATE_FOCUS_WILL_PLACE_SURFACES;
                    }
                    if ((65540 & sources) == 65540) {
                        config.navigation = UPDATE_FOCUS_WILL_PLACE_SURFACES;
                        navigationPresence |= presenceFlag;
                    } else if ((sources & 513) == 513 && config.navigation == WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        config.navigation = WINDOWS_FREEZING_SCREENS_TIMEOUT;
                        navigationPresence |= presenceFlag;
                    }
                    if (device.getKeyboardType() == WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                        config.keyboard = WINDOWS_FREEZING_SCREENS_TIMEOUT;
                        keyboardPresence |= presenceFlag;
                    }
                }
            }
            if (config.navigation == WINDOWS_FREEZING_SCREENS_ACTIVE && this.mHasPermanentDpad) {
                config.navigation = WINDOWS_FREEZING_SCREENS_TIMEOUT;
                navigationPresence |= WINDOWS_FREEZING_SCREENS_ACTIVE;
            }
            boolean hardKeyboardAvailable = config.keyboard != WINDOWS_FREEZING_SCREENS_ACTIVE ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            if (hardKeyboardAvailable != this.mHardKeyboardAvailable) {
                this.mHardKeyboardAvailable = hardKeyboardAvailable;
                this.mH.removeMessages(22);
                this.mH.sendEmptyMessage(22);
            }
            config.keyboardHidden = WINDOWS_FREEZING_SCREENS_ACTIVE;
            config.hardKeyboardHidden = WINDOWS_FREEZING_SCREENS_ACTIVE;
            config.navigationHidden = WINDOWS_FREEZING_SCREENS_ACTIVE;
            this.mPolicy.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
        }

        void notifyHardKeyboardStatusChange() {
            synchronized (this.mWindowMap) {
                OnHardKeyboardStatusChangeListener listener = this.mHardKeyboardStatusChangeListener;
                boolean available = this.mHardKeyboardAvailable;
            }
            if (listener != null) {
                listener.onHardKeyboardStatusChange(available);
            }
        }

        boolean startMovingTask(IWindow window, float startX, float startY) {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(null, window, (boolean) PROFILE_ORIENTATION);
                if (startPositioningLocked(win, PROFILE_ORIENTATION, startX, startY)) {
                    try {
                        this.mActivityManager.setFocusedTask(win.getTask().mTaskId);
                    } catch (RemoteException e) {
                    }
                    return SCREENSHOT_FORCE_565;
                }
                return PROFILE_ORIENTATION;
            }
        }

        private void startScrollingTask(DisplayContent displayContent, int startX, int startY) {
            Task task = null;
            synchronized (this.mWindowMap) {
                int taskId = displayContent.taskIdFromPoint(startX, startY);
                if (taskId >= 0) {
                    task = (Task) this.mTaskIdToTask.get(taskId);
                }
                if (task != null && task.isDockedInEffect() && startPositioningLocked(task.getTopVisibleAppMainWindow(), PROFILE_ORIENTATION, (float) startX, (float) startY)) {
                    try {
                        this.mActivityManager.setFocusedTask(task.mTaskId);
                    } catch (RemoteException e) {
                    }
                    return;
                }
            }
        }

        private void handleTapOutsideTask(DisplayContent displayContent, int x, int y) {
            synchronized (this.mWindowMap) {
                int taskId;
                Task task = displayContent.findTaskForControlPoint(x, y);
                if (task == null) {
                    taskId = displayContent.taskIdFromPoint(x, y);
                } else if (startPositioningLocked(task.getTopVisibleAppMainWindow(), SCREENSHOT_FORCE_565, (float) x, (float) y)) {
                    taskId = task.mTaskId;
                } else {
                    return;
                }
                if (taskId >= 0) {
                    try {
                        this.mActivityManager.setFocusedTask(taskId);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        private boolean startPositioningLocked(WindowState win, boolean resize, float startX, float startY) {
            if (win == null || win.getAppToken() == null) {
                Slog.w("WindowManager", "startPositioningLocked: Bad window " + win);
                return PROFILE_ORIENTATION;
            } else if (win.mInputChannel == null) {
                Slog.wtf("WindowManager", "startPositioningLocked: " + win + " has no input channel, " + " probably being removed");
                return PROFILE_ORIENTATION;
            } else {
                DisplayContent displayContent = win.getDisplayContent();
                if (displayContent == null) {
                    Slog.w("WindowManager", "startPositioningLocked: Invalid display content " + win);
                    return PROFILE_ORIENTATION;
                }
                Display display = displayContent.getDisplay();
                this.mTaskPositioner = new TaskPositioner(this);
                this.mTaskPositioner.register(display);
                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                WindowState transferFocusFromWin = win;
                if (!(this.mCurrentFocus == null || this.mCurrentFocus == win || this.mCurrentFocus.mAppToken != win.mAppToken)) {
                    transferFocusFromWin = this.mCurrentFocus;
                }
                if (this.mInputManager.transferTouchFocus(transferFocusFromWin.mInputChannel, this.mTaskPositioner.mServerChannel)) {
                    this.mTaskPositioner.startDragLocked(win, resize, startX, startY);
                    return SCREENSHOT_FORCE_565;
                }
                Slog.e("WindowManager", "startPositioningLocked: Unable to transfer touch focus");
                this.mTaskPositioner.unregister();
                this.mTaskPositioner = null;
                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                return PROFILE_ORIENTATION;
            }
        }

        private void finishPositioning() {
            synchronized (this.mWindowMap) {
                if (this.mTaskPositioner != null) {
                    this.mTaskPositioner.unregister();
                    this.mTaskPositioner = null;
                    this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                }
            }
        }

        void adjustForImeIfNeeded(DisplayContent displayContent) {
            WindowState imeWin = this.mInputMethodWindow;
            boolean imeVisible = (imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw()) ? displayContent.mDividerControllerLocked.isImeHideRequested() ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            boolean dockVisible = isStackVisibleLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES);
            TaskStack imeTargetStack = getImeFocusStackLocked();
            int imeDockSide = (!dockVisible || imeTargetStack == null) ? -1 : imeTargetStack.getDockSide();
            boolean imeOnTop = imeDockSide == WINDOWS_FREEZING_SCREENS_TIMEOUT ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            boolean imeOnBottom = imeDockSide == LAYOUT_REPEAT_THRESHOLD ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            boolean dockMinimized = displayContent.mDividerControllerLocked.isMinimizedDock();
            int imeHeight = this.mPolicy.getInputMethodWindowVisibleHeightLw();
            boolean imeHeightChanged = imeVisible ? imeHeight != displayContent.mDividerControllerLocked.getImeHeightAdjustedFor() ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : PROFILE_ORIENTATION;
            ArrayList<TaskStack> stacks;
            int i;
            if (imeVisible && dockVisible && ((imeOnTop || imeOnBottom) && !dockMinimized)) {
                stacks = displayContent.getStacks();
                for (i = stacks.size() - 1; i >= 0; i--) {
                    TaskStack stack = (TaskStack) stacks.get(i);
                    boolean isDockedOnBottom = stack.getDockSide() == LAYOUT_REPEAT_THRESHOLD ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                    if (stack.isVisibleLocked() && (imeOnBottom || isDockedOnBottom)) {
                        stack.setAdjustedForIme(imeWin, imeOnBottom ? imeHeightChanged : PROFILE_ORIENTATION);
                    } else {
                        stack.resetAdjustedForIme(PROFILE_ORIENTATION);
                    }
                }
                displayContent.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, SCREENSHOT_FORCE_565, SCREENSHOT_FORCE_565, imeWin, imeHeight);
                return;
            }
            stacks = displayContent.getStacks();
            for (i = stacks.size() - 1; i >= 0; i--) {
                ((TaskStack) stacks.get(i)).resetAdjustedForIme(dockVisible ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565);
            }
            displayContent.mDividerControllerLocked.setAdjustedForIme(PROFILE_ORIENTATION, PROFILE_ORIENTATION, dockVisible, imeWin, imeHeight);
        }

        IBinder prepareDragSurface(IWindow window, SurfaceSession session, int flags, int width, int height, Surface outSurface) {
            OutOfResourcesException e;
            Throwable th;
            if (getLazyMode() != 0) {
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
                        if (this.mDragState == null) {
                            Display display = getDefaultDisplayContentLocked().getDisplay();
                            SurfaceControl surface = new SurfaceControl(session, "drag surface", width, height, COMPAT_MODE_MATCH_PARENT, LAYOUT_REPEAT_THRESHOLD);
                            surface.setLayerStack(display.getLayerStack());
                            float alpha = 1.0f;
                            if ((flags & DumpState.DUMP_MESSAGES) == 0) {
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
                                    Slog.e("WindowManager", "Can't allocate drag surface w=" + width + " h=" + height, e);
                                    if (this.mDragState != null) {
                                        this.mDragState.reset();
                                        this.mDragState = null;
                                    }
                                    Binder.restoreCallingIdentity(origId);
                                    return token2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                            try {
                                Binder.restoreCallingIdentity(origId);
                                return token2;
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        }
                        Slog.w("WindowManager", "Drag already in progress");
                        token2 = null;
                        Binder.restoreCallingIdentity(origId);
                        return token2;
                    } catch (OutOfResourcesException e3) {
                        e = e3;
                        token2 = token;
                        Slog.e("WindowManager", "Can't allocate drag surface w=" + width + " h=" + height, e);
                        if (this.mDragState != null) {
                            this.mDragState.reset();
                            this.mDragState = null;
                        }
                        Binder.restoreCallingIdentity(origId);
                        return token2;
                    } catch (Throwable th4) {
                        th = th4;
                        token2 = token;
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

        public void pauseKeyDispatching(IBinder _token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "pauseKeyDispatching()")) {
                synchronized (this.mWindowMap) {
                    WindowToken token = (WindowToken) this.mTokenMap.get(_token);
                    if (token != null) {
                        this.mInputMonitor.pauseDispatchingLw(token);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void resumeKeyDispatching(IBinder _token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "resumeKeyDispatching()")) {
                synchronized (this.mWindowMap) {
                    WindowToken token = (WindowToken) this.mTokenMap.get(_token);
                    if (token != null) {
                        this.mInputMonitor.resumeDispatchingLw(token);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void setEventDispatching(boolean enabled) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setEventDispatching()")) {
                synchronized (this.mWindowMap) {
                    this.mEventDispatchingEnabled = enabled;
                    if (this.mDisplayEnabled) {
                        this.mInputMonitor.setEventDispatchingLw(enabled);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private WindowState getFocusedWindow() {
            WindowState focusedWindowLocked;
            synchronized (this.mWindowMap) {
                focusedWindowLocked = getFocusedWindowLocked();
            }
            return focusedWindowLocked;
        }

        private WindowState getFocusedWindowLocked() {
            return this.mCurrentFocus;
        }

        TaskStack getImeFocusStackLocked() {
            if (this.mFocusedApp == null || this.mFocusedApp.mTask == null) {
                return null;
            }
            return this.mFocusedApp.mTask.mStack;
        }

        private void showAuditSafeModeNotification() {
            PendingIntent pendingIntent = PendingIntent.getActivity(this.mContext, WINDOW_ANIMATION_SCALE, new Intent("android.intent.action.VIEW", Uri.parse("https://support.google.com/nexus/answer/2852139")), WINDOW_ANIMATION_SCALE);
            String title = this.mContext.getString(17040859);
            ((NotificationManager) this.mContext.getSystemService("notification")).notifyAsUser(null, 17040859, new Builder(this.mContext).setSmallIcon(17301642).setWhen(0).setOngoing(SCREENSHOT_FORCE_565).setTicker(title).setLocalOnly(SCREENSHOT_FORCE_565).setPriority(WINDOWS_FREEZING_SCREENS_ACTIVE).setVisibility(WINDOWS_FREEZING_SCREENS_ACTIVE).setColor(this.mContext.getColor(17170519)).setContentTitle(title).setContentText(this.mContext.getString(17040860)).setContentIntent(pendingIntent).build(), UserHandle.ALL);
        }

        public boolean detectSafeMode() {
            if (!this.mInputMonitor.waitForInputDevicesReady(1000)) {
                Slog.w("WindowManager", "Devices still not ready after waiting 1000 milliseconds before attempting to detect safe mode.");
            }
            if (Global.getInt(this.mContext.getContentResolver(), "safe_boot_disallowed", WINDOW_ANIMATION_SCALE) != 0) {
                return PROFILE_ORIENTATION;
            }
            int menuState = this.mInputManager.getKeyCodeState(-1, -256, 82);
            int sState = this.mInputManager.getKeyCodeState(-1, -256, 47);
            int dpadState = this.mInputManager.getKeyCodeState(-1, 513, 23);
            int trackballState = this.mInputManager.getScanCodeState(-1, 65540, InputManagerService.BTN_MOUSE);
            boolean z = (menuState > 0 || sState > 0 || dpadState > 0 || trackballState > 0) ? SCREENSHOT_FORCE_565 : this.mInputManager.getKeyCodeState(-1, -256, 25) > 0 ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            this.mSafeMode = z;
            try {
                if (!(SystemProperties.getInt(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, WINDOW_ANIMATION_SCALE) == 0 && SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, WINDOW_ANIMATION_SCALE) == 0)) {
                    int auditSafeMode = SystemProperties.getInt(ShutdownThread.AUDIT_SAFEMODE_PROPERTY, WINDOW_ANIMATION_SCALE);
                    if (auditSafeMode == 0) {
                        this.mSafeMode = SCREENSHOT_FORCE_565;
                        SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, "");
                    } else if (auditSafeMode >= SystemProperties.getInt(PROPERTY_BUILD_DATE_UTC, WINDOW_ANIMATION_SCALE)) {
                        this.mSafeMode = SCREENSHOT_FORCE_565;
                        showAuditSafeModeNotification();
                    } else {
                        SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, "");
                        SystemProperties.set(ShutdownThread.AUDIT_SAFEMODE_PROPERTY, "");
                    }
                }
            } catch (IllegalArgumentException e) {
            }
            if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                this.mSafeMode = PROFILE_ORIENTATION;
            }
            if (this.mSafeMode) {
                Log.i("WindowManager", "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState + " dpad=" + dpadState + " trackball=" + trackballState + ")");
                SystemProperties.set(ShutdownThread.RO_SAFEMODE_PROPERTY, "1");
            } else {
                Log.i("WindowManager", "SAFE MODE not enabled");
            }
            this.mPolicy.setSafeMode(this.mSafeMode);
            return this.mSafeMode;
        }

        public void displayReady() {
            Display[] displayArr = this.mDisplays;
            int length = displayArr.length;
            for (int i = WINDOW_ANIMATION_SCALE; i < length; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                displayReady(displayArr[i].getDisplayId());
            }
            synchronized (this.mWindowMap) {
                readForcedDisplayPropertiesLocked(getDefaultDisplayContentLocked());
                this.mDisplayReady = SCREENSHOT_FORCE_565;
            }
            try {
                this.mActivityManager.updateConfiguration(null);
            } catch (RemoteException e) {
            }
            synchronized (this.mWindowMap) {
                this.mIsTouchDevice = this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
                configureDisplayPolicyLocked(getDefaultDisplayContentLocked());
            }
            try {
                this.mActivityManager.updateConfiguration(null);
            } catch (RemoteException e2) {
            }
            updateCircularDisplayMaskIfNeeded();
        }

        private void displayReady(int displayId) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null) {
                    this.mAnimator.addDisplayLocked(displayId);
                    displayContent.initializeDisplayBaseInfo();
                    if (displayContent.mTapDetector != null) {
                        displayContent.mTapDetector.init();
                    }
                }
            }
        }

        public void systemReady() {
            this.mPolicy.systemReady();
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

        public boolean inputMethodClientHasFocus(IInputMethodClient client) {
            synchronized (this.mWindowMap) {
                int idx = findDesiredInputMethodWindowIndexLocked(PROFILE_ORIENTATION);
                if (idx > 0) {
                    WindowState imFocus = (WindowState) getDefaultWindowListLocked().get(idx - 1);
                    if (imFocus != null) {
                        if (imFocus.mAttrs.type == UPDATE_FOCUS_WILL_PLACE_SURFACES && imFocus.mAppToken != null) {
                            for (int i = WINDOW_ANIMATION_SCALE; i < imFocus.mAppToken.windows.size(); i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                                WindowState w = (WindowState) imFocus.mAppToken.windows.get(i);
                                if (w != imFocus) {
                                    Log.i("WindowManager", "Switching to real app window: " + w);
                                    imFocus = w;
                                    break;
                                }
                            }
                        }
                        if (imFocus.mSession.mClient != null && imFocus.mSession.mClient.asBinder() == client.asBinder()) {
                            return SCREENSHOT_FORCE_565;
                        }
                    }
                }
                if (this.mCurrentFocus == null || this.mCurrentFocus.mSession.mClient == null || this.mCurrentFocus.mSession.mClient.asBinder() != client.asBinder()) {
                    return PROFILE_ORIENTATION;
                }
                return SCREENSHOT_FORCE_565;
            }
        }

        public void getInitialDisplaySize(int displayId, Point size) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mInitialDisplayWidth;
                    size.y = displayContent.mInitialDisplayHeight;
                }
            }
        }

        public void getBaseDisplaySize(int displayId, Point size) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mBaseDisplayWidth;
                    size.y = displayContent.mBaseDisplayHeight;
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
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            width = Math.min(Math.max(width, BOOT_ANIMATION_POLL_INTERVAL), displayContent.mInitialDisplayWidth * WINDOWS_FREEZING_SCREENS_TIMEOUT);
                            height = Math.min(Math.max(height, BOOT_ANIMATION_POLL_INTERVAL), displayContent.mInitialDisplayHeight * WINDOWS_FREEZING_SCREENS_TIMEOUT);
                            updateResourceConfiguration(displayId, displayContent.mBaseDisplayDensity, width, height);
                            setForcedDisplaySizeLocked(displayContent, width, height);
                            Global.putString(this.mContext.getContentResolver(), "display_size_forced", width + "," + height);
                        }
                    }
                } finally {
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
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            if (mode < 0 || mode > WINDOWS_FREEZING_SCREENS_ACTIVE) {
                                mode = WINDOW_ANIMATION_SCALE;
                            }
                            setForcedDisplayScalingModeLocked(displayContent, mode);
                            Global.putInt(this.mContext.getContentResolver(), "display_scaling_force", mode);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        private void setForcedDisplayScalingModeLocked(DisplayContent displayContent, int mode) {
            boolean z;
            Slog.i("WindowManager", "Using display scaling mode: " + (mode == 0 ? "auto" : "off"));
            if (mode != 0) {
                z = SCREENSHOT_FORCE_565;
            } else {
                z = PROFILE_ORIENTATION;
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
                        int width = Integer.parseInt(sizeStr.substring(WINDOW_ANIMATION_SCALE, pos));
                        int height = Integer.parseInt(sizeStr.substring(pos + WINDOWS_FREEZING_SCREENS_ACTIVE));
                        if (!(displayContent.mBaseDisplayWidth == width && displayContent.mBaseDisplayHeight == height)) {
                            Slog.i("WindowManager", "FORCED DISPLAY SIZE: " + width + "x" + height);
                            displayContent.mBaseDisplayWidth = width;
                            displayContent.mBaseDisplayHeight = height;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            int density = getForcedDisplayDensityForUserLocked(this.mCurrentUserId);
            if (density != 0) {
                displayContent.mBaseDisplayDensity = density;
            }
            if (Global.getInt(this.mContext.getContentResolver(), "display_scaling_force", WINDOW_ANIMATION_SCALE) != 0) {
                Slog.i("WindowManager", "FORCED DISPLAY SCALING DISABLED");
                displayContent.mDisplayScalingDisabled = SCREENSHOT_FORCE_565;
            }
        }

        private void setForcedDisplaySizeLocked(DisplayContent displayContent, int width, int height) {
            Slog.i("WindowManager", "Using new display size: " + width + "x" + height);
            displayContent.mBaseDisplayWidth = width;
            displayContent.mBaseDisplayHeight = height;
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
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            setForcedDisplaySizeLocked(displayContent, displayContent.mInitialDisplayWidth, displayContent.mInitialDisplayHeight);
                            Global.putString(this.mContext.getContentResolver(), "display_size_forced", "");
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public int getInitialDisplayDensity(int displayId) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                    return -1;
                }
                int i = displayContent.mInitialDisplayDensity;
                return i;
            }
        }

        public int getBaseDisplayDensity(int displayId) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                    return -1;
                }
                int i = displayContent.mBaseDisplayDensity;
                return i;
            }
        }

        public void setForcedDisplayDensity(int displayId, int density) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            updateResourceConfiguration(displayId, density, displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight);
                            setForcedDisplayDensityLocked(displayContent, density);
                            Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), this.mCurrentUserId);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void clearForcedDisplayDensity(int displayId) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            setForcedDisplayDensityLocked(displayContent, displayContent.mInitialDisplayDensity);
                            Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", "", this.mCurrentUserId);
                        }
                    }
                } finally {
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
            return WINDOW_ANIMATION_SCALE;
        }

        private void setForcedDisplayDensityLocked(DisplayContent displayContent, int density) {
            displayContent.mBaseDisplayDensity = density;
            reconfigureDisplayLocked(displayContent);
        }

        protected void reconfigureDisplayLocked(DisplayContent displayContent) {
            if (this.mDisplayReady) {
                configureDisplayPolicyLocked(displayContent);
                displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                boolean configChanged = updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION);
                this.mTempConfiguration.setToDefaults();
                this.mTempConfiguration.updateFrom(this.mCurConfiguration);
                computeScreenConfigurationLocked(this.mTempConfiguration);
                if (configChanged | (this.mCurConfiguration.diff(this.mTempConfiguration) != 0 ? WINDOWS_FREEZING_SCREENS_ACTIVE : WINDOW_ANIMATION_SCALE)) {
                    this.mWaitingForConfig = SCREENSHOT_FORCE_565;
                    startFreezingDisplayLocked(PROFILE_ORIENTATION, WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE);
                    this.mH.sendEmptyMessage(18);
                    if (!this.mReconfigureOnConfigurationChanged.contains(displayContent)) {
                        this.mReconfigureOnConfigurationChanged.add(displayContent);
                    }
                }
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        private void configureDisplayPolicyLocked(DisplayContent displayContent) {
            this.mPolicy.setInitialDisplaySize(displayContent.getDisplay(), displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight, displayContent.mBaseDisplayDensity);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            this.mPolicy.setDisplayOverscan(displayContent.getDisplay(), displayInfo.overscanLeft, displayInfo.overscanTop, displayInfo.overscanRight, displayInfo.overscanBottom);
        }

        public void setOverscan(int displayId, int left, int top, int right, int bottom) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null) {
                        setOverscanLocked(displayContent, left, top, right, bottom);
                    }
                }
            } finally {
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
            RuntimeException ex;
            if (win == null) {
                ex = new IllegalArgumentException("Requested window " + client + " does not exist");
                if (throwOnError) {
                    throw ex;
                }
                Slog.w("WindowManager", "Failed looking up window", ex);
                return null;
            } else if (session == null || win.mSession == session) {
                return win;
            } else {
                ex = new IllegalArgumentException("Requested window " + client + " is in session " + win.mSession + ", not " + session);
                if (throwOnError) {
                    throw ex;
                }
                Slog.w("WindowManager", "Failed looking up window", ex);
                return null;
            }
        }

        final void rebuildAppWindowListLocked() {
            rebuildAppWindowListLocked(getDefaultDisplayContentLocked());
        }

        private void rebuildAppWindowListLocked(DisplayContent displayContent) {
            int stackNdx;
            WindowList windows = displayContent.getWindowList();
            int NW = windows.size();
            int lastBelow = -1;
            int numRemoved = WINDOW_ANIMATION_SCALE;
            int length = this.mRebuildTmp.length;
            if (r0 < NW) {
                this.mRebuildTmp = new WindowState[(NW + 10)];
            }
            int i = WINDOW_ANIMATION_SCALE;
            while (i < NW) {
                WindowState w = (WindowState) windows.get(i);
                if (w.mAppToken != null) {
                    WindowState win = (WindowState) windows.remove(i);
                    win.mRebuilding = SCREENSHOT_FORCE_565;
                    this.mRebuildTmp[numRemoved] = win;
                    this.mWindowsChanged = SCREENSHOT_FORCE_565;
                    NW--;
                    numRemoved += WINDOWS_FREEZING_SCREENS_ACTIVE;
                } else {
                    if (lastBelow == i - 1) {
                        length = w.mAttrs.type;
                        if (r0 == 2013) {
                            lastBelow = i;
                        }
                    }
                    i += WINDOWS_FREEZING_SCREENS_ACTIVE;
                }
            }
            lastBelow += WINDOWS_FREEZING_SCREENS_ACTIVE;
            i = lastBelow;
            ArrayList<TaskStack> stacks = displayContent.getStacks();
            int numStacks = stacks.size();
            for (stackNdx = WINDOW_ANIMATION_SCALE; stackNdx < numStacks; stackNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                AppTokenList exitingAppTokens = ((TaskStack) stacks.get(stackNdx)).mExitingAppTokens;
                int NT = exitingAppTokens.size();
                for (int j = WINDOW_ANIMATION_SCALE; j < NT; j += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    i = reAddAppWindowsLocked(displayContent, i, (WindowToken) exitingAppTokens.get(j), PROFILE_ORIENTATION);
                }
            }
            for (stackNdx = WINDOW_ANIMATION_SCALE; stackNdx < numStacks; stackNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
                int numTasks = tasks.size();
                for (int taskNdx = WINDOW_ANIMATION_SCALE; taskNdx < numTasks; taskNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                    int numTokens = tokens.size();
                    for (int tokenNdx = WINDOW_ANIMATION_SCALE; tokenNdx < numTokens; tokenNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        WindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                        if (!wtoken.mIsExiting || wtoken.waitingForReplacement()) {
                            i = reAddAppWindowsLocked(displayContent, i, wtoken, SCREENSHOT_FORCE_565);
                        }
                    }
                }
            }
            i -= lastBelow;
            if (i != numRemoved) {
                displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                Slog.w("WindowManager", "On display=" + displayContent.getDisplayId() + " Rebuild removed " + numRemoved + " windows but added " + i + " rebuildAppWindowListLocked() " + " callers=" + Debug.getCallers(10));
                for (i = WINDOW_ANIMATION_SCALE; i < numRemoved; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState ws = this.mRebuildTmp[i];
                    if (ws.mRebuilding) {
                        Writer sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, PROFILE_ORIENTATION, DumpState.DUMP_PROVIDERS);
                        ws.dump(pw, "", SCREENSHOT_FORCE_565);
                        pw.flush();
                        Slog.w("WindowManager", "This window was lost: " + ws);
                        Slog.w("WindowManager", sw.toString());
                        ws.mWinAnimator.destroySurfaceLocked();
                    }
                }
                Slog.w("WindowManager", "Current app token list:");
                dumpAppTokensLocked();
                Slog.w("WindowManager", "Final window list:");
                dumpWindowsLocked();
            }
            Arrays.fill(this.mRebuildTmp, null);
        }

        void makeWindowFreezingScreenIfNeededLocked(WindowState w) {
            if (!okToDisplay() && this.mWindowsFreezingScreen != WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                w.mOrientationChanging = SCREENSHOT_FORCE_565;
                w.mLastFreezeDuration = WINDOW_ANIMATION_SCALE;
                this.mWindowPlacerLocked.mOrientationChangeComplete = PROFILE_ORIENTATION;
                if (this.mWindowsFreezingScreen == 0) {
                    this.mWindowsFreezingScreen = WINDOWS_FREEZING_SCREENS_ACTIVE;
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
            this.mWallpaperControllerLocked.hideDeferredWallpapersIfNeeded();
            ArrayList<TaskStack> stacks = getDefaultDisplayContentLocked().getStacks();
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                    for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                        ((AppWindowToken) tokens.get(tokenNdx)).sendingToBottom = PROFILE_ORIENTATION;
                    }
                }
            }
            rebuildAppWindowListLocked();
            moveInputMethodWindowsIfNeededLocked(SCREENSHOT_FORCE_565);
            this.mWindowPlacerLocked.mWallpaperMayChange = SCREENSHOT_FORCE_565;
            this.mFocusMayChange = SCREENSHOT_FORCE_565;
            return WINDOWS_FREEZING_SCREENS_ACTIVE;
        }

        void updateResizingWindows(WindowState w) {
            WindowStateAnimator winAnimator = w.mWinAnimator;
            if (w.mHasSurface && w.mLayoutSeq == this.mLayoutSeq && !w.isGoneForLayoutLw()) {
                Task task = w.getTask();
                if (task == null || !task.mStack.getBoundsAnimating()) {
                    w.setInsetsChanged();
                    boolean configChanged = w.isConfigChanged();
                    boolean dragResizingChanged = w.isDragResizeChanged() ? w.isDragResizingChangeReported() ? PROFILE_ORIENTATION : SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                    w.mLastFrame.set(w.mFrame);
                    if (w.mContentInsetsChanged || w.mVisibleInsetsChanged || winAnimator.mSurfaceResized || w.mOutsetsChanged || configChanged || dragResizingChanged || !w.isResizedWhileNotDragResizingReported()) {
                        if (w.mAppToken == null || !w.mAppDied) {
                            w.mLastOverscanInsets.set(w.mOverscanInsets);
                            w.mLastContentInsets.set(w.mContentInsets);
                            w.mLastVisibleInsets.set(w.mVisibleInsets);
                            w.mLastStableInsets.set(w.mStableInsets);
                            w.mLastOutsets.set(w.mOutsets);
                            makeWindowFreezingScreenIfNeededLocked(w);
                            if (w.mOrientationChanging || dragResizingChanged || w.isResizedWhileNotDragResizing()) {
                                winAnimator.mDrawState = WINDOWS_FREEZING_SCREENS_ACTIVE;
                                if (w.mAppToken != null) {
                                    w.mAppToken.clearAllDrawn();
                                }
                            }
                            if (!this.mResizingWindows.contains(w)) {
                                this.mResizingWindows.add(w);
                            }
                        } else {
                            w.mAppToken.removeAllDeadWindows();
                        }
                    } else if (w.mOrientationChanging && w.isDrawnLw()) {
                        w.mOrientationChanging = PROFILE_ORIENTATION;
                        w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                    }
                }
            }
        }

        void checkDrawnWindowsLocked() {
            if (!this.mWaitingForDrawn.isEmpty() && this.mWaitingForDrawnCallback != null) {
                for (int j = this.mWaitingForDrawn.size() - 1; j >= 0; j--) {
                    WindowState win = (WindowState) this.mWaitingForDrawn.get(j);
                    if (win.mRemoved || !win.mHasSurface || !win.mPolicyVisibility) {
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
            boolean hold = newHoldScreen != null ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
            if (hold && this.mHoldingScreenOn != newHoldScreen) {
                this.mHoldingScreenWakeLock.setWorkSource(new WorkSource(newHoldScreen.mUid));
            }
            this.mHoldingScreenOn = newHoldScreen;
            if (hold == this.mHoldingScreenWakeLock.isHeld()) {
                return;
            }
            if (hold) {
                this.mLastWakeLockHoldingWindow = this.mWindowPlacerLocked.mHoldScreenWindow;
                this.mLastWakeLockObscuringWindow = null;
                this.mHoldingScreenWakeLock.acquire();
                this.mPolicy.keepScreenOnStartedLw();
                return;
            }
            this.mLastWakeLockHoldingWindow = null;
            this.mLastWakeLockObscuringWindow = this.mWindowPlacerLocked.mObsuringWindow;
            this.mPolicy.keepScreenOnStoppedLw();
            this.mHoldingScreenWakeLock.release();
        }

        void requestTraversal() {
            synchronized (this.mWindowMap) {
                this.mWindowPlacerLocked.requestTraversal();
            }
        }

        void scheduleAnimationLocked() {
            if (!this.mAnimationScheduled) {
                this.mAnimationScheduled = SCREENSHOT_FORCE_565;
                this.mChoreographer.postFrameCallback(this.mAnimator.mAnimationFrameCallback);
            }
        }

        boolean needsLayout() {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                if (((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).layoutNeeded) {
                    return SCREENSHOT_FORCE_565;
                }
            }
            return PROFILE_ORIENTATION;
        }

        int adjustAnimationBackground(WindowStateAnimator winAnimator) {
            WindowList windows = winAnimator.mWin.getWindowList();
            for (int i = windows.size() - 1; i >= 0; i--) {
                WindowState testWin = (WindowState) windows.get(i);
                if (testWin.mIsWallpaper && testWin.isVisibleNow()) {
                    return testWin.mWinAnimator.mAnimLayer;
                }
            }
            return winAnimator.mAnimLayer;
        }

        boolean reclaimSomeSurfaceMemoryLocked(WindowStateAnimator winAnimator, String operation, boolean secure) {
            int displayNdx;
            WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
            boolean leakedSurface = PROFILE_ORIENTATION;
            boolean killedApps = PROFILE_ORIENTATION;
            String[] strArr = new Object[UPDATE_FOCUS_WILL_PLACE_SURFACES];
            strArr[WINDOW_ANIMATION_SCALE] = winAnimator.mWin.toString();
            strArr[WINDOWS_FREEZING_SCREENS_ACTIVE] = Integer.valueOf(winAnimator.mSession.mPid);
            strArr[WINDOWS_FREEZING_SCREENS_TIMEOUT] = operation;
            EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, strArr);
            long callingIdentity = Binder.clearCallingIdentity();
            Slog.i("WindowManager", "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mDisplayContents.size();
            for (displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                int winNdx;
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int numWindows = windows.size();
                for (winNdx = WINDOW_ANIMATION_SCALE; winNdx < numWindows; winNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState ws = (WindowState) windows.get(winNdx);
                    WindowStateAnimator wsa = ws.mWinAnimator;
                    if (wsa.mSurfaceController != null) {
                        StringBuilder append;
                        if (!this.mSessions.contains(wsa.mSession)) {
                            append = new StringBuilder().append("LEAKED SURFACE (session doesn't exist): ");
                            WindowSurfaceController windowSurfaceController = wsa.mSurfaceController;
                            WindowToken windowToken = ws.mToken;
                            Slog.w("WindowManager", r21.append(ws).append(" surface=").append(r0).append(" token=").append(r0).append(" pid=").append(ws.mSession.mPid).append(" uid=").append(ws.mSession.mUid).toString());
                            wsa.destroySurface();
                            this.mForceRemoves.add(ws);
                            leakedSurface = SCREENSHOT_FORCE_565;
                        } else if (ws.mAppToken != null) {
                            if (ws.mAppToken.clientHidden) {
                                append = new StringBuilder().append("LEAKED SURFACE (app token hidden): ");
                                Slog.w("WindowManager", r21.append(ws).append(" surface=").append(wsa.mSurfaceController).append(" token=").append(ws.mAppToken).append(" saved=").append(ws.hasSavedSurface()).toString());
                                wsa.destroySurface();
                                leakedSurface = SCREENSHOT_FORCE_565;
                            }
                        }
                    }
                }
            }
            if (!leakedSurface) {
                Slog.w("WindowManager", "No leaked surfaces; killing applicatons!");
                SparseIntArray pidCandidates = new SparseIntArray();
                displayNdx = WINDOW_ANIMATION_SCALE;
                while (displayNdx < numDisplays) {
                    windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    numWindows = windows.size();
                    for (winNdx = WINDOW_ANIMATION_SCALE; winNdx < numWindows; winNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        ws = (WindowState) windows.get(winNdx);
                        if (!this.mForceRemoves.contains(ws)) {
                            wsa = ws.mWinAnimator;
                            if (wsa.mSurfaceController != null) {
                                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
                            }
                        }
                    }
                    try {
                        if (pidCandidates.size() > 0) {
                            int[] pids = new int[pidCandidates.size()];
                            int i = WINDOW_ANIMATION_SCALE;
                            while (true) {
                                int length = pids.length;
                                if (i < r0) {
                                    pids[i] = pidCandidates.keyAt(i);
                                    i += WINDOWS_FREEZING_SCREENS_ACTIVE;
                                } else {
                                    try {
                                        break;
                                    } catch (RemoteException e) {
                                    }
                                }
                            }
                            if (this.mActivityManager.killPids(pids, "Free memory", secure)) {
                                killedApps = SCREENSHOT_FORCE_565;
                            }
                        }
                        displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingIdentity);
                    }
                }
            }
            if (leakedSurface || killedApps) {
                Slog.w("WindowManager", "Looks like we have reclaimed some memory, clearing surface for retry.");
                if (surfaceController != null) {
                    winAnimator.destroySurface();
                    scheduleRemoveStartingWindowLocked(winAnimator.mWin.mAppToken);
                }
                try {
                    winAnimator.mWin.mClient.dispatchGetNewSurface();
                } catch (RemoteException e2) {
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return !leakedSurface ? killedApps : SCREENSHOT_FORCE_565;
        }

        boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
            WindowState newFocus = computeFocusedWindowLocked();
            if (this.mCurrentFocus == newFocus) {
                return PROFILE_ORIENTATION;
            }
            Trace.traceBegin(32, "wmUpdateFocus");
            this.mH.removeMessages(WINDOWS_FREEZING_SCREENS_TIMEOUT);
            this.mH.sendEmptyMessage(WINDOWS_FREEZING_SCREENS_TIMEOUT);
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            boolean z = mode != WINDOWS_FREEZING_SCREENS_ACTIVE ? mode != UPDATE_FOCUS_WILL_PLACE_SURFACES ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION : PROFILE_ORIENTATION;
            boolean imWindowChanged = moveInputMethodWindowsIfNeededLocked(z);
            if (imWindowChanged) {
                displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                newFocus = computeFocusedWindowLocked();
            }
            WindowState oldFocus = this.mCurrentFocus;
            this.mCurrentFocus = newFocus;
            this.mInputManager.setCurFocusWindow(this.mCurrentFocus);
            this.mLosingFocus.remove(newFocus);
            int focusChanged = this.mPolicy.focusChangedLw(oldFocus, newFocus);
            if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
                if (mode == WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                    this.mWindowPlacerLocked.performLayoutLockedInner(displayContent, SCREENSHOT_FORCE_565, updateInputWindows);
                    focusChanged &= -2;
                } else if (mode == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                    this.mLayersController.assignLayersLocked(displayContent.getWindowList());
                }
            }
            if ((focusChanged & WINDOWS_FREEZING_SCREENS_ACTIVE) != 0) {
                displayContent.layoutNeeded = SCREENSHOT_FORCE_565;
                if (mode == WINDOWS_FREEZING_SCREENS_TIMEOUT) {
                    this.mWindowPlacerLocked.performLayoutLockedInner(displayContent, SCREENSHOT_FORCE_565, updateInputWindows);
                }
            }
            if (mode != WINDOWS_FREEZING_SCREENS_ACTIVE) {
                this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, updateInputWindows);
            }
            String NUM_REGEX = "[0-9]++";
            Pattern pattern = Pattern.compile("[0-9]++");
            Matcher matcherForOldFocusWin = pattern.matcher(oldFocus != null ? oldFocus.getAttrs().getTitle() : "");
            Matcher matcherForNewFocusWin = pattern.matcher(newFocus != null ? newFocus.getAttrs().getTitle() : "");
            if (!(matcherForOldFocusWin.find() || matcherForNewFocusWin.find())) {
                Flog.i(304, "oldFocusWindow: " + oldFocus + ", currentFocusWindow: " + this.mCurrentFocus + ", currentFocusApp: " + this.mFocusedApp);
            }
            adjustForImeIfNeeded(displayContent);
            Trace.traceEnd(32);
            return SCREENSHOT_FORCE_565;
        }

        private WindowState computeFocusedWindowLocked() {
            int displayCount = this.mDisplayContents.size();
            for (int i = WINDOW_ANIMATION_SCALE; i < displayCount; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                WindowState win = findFocusedWindowLocked((DisplayContent) this.mDisplayContents.valueAt(i));
                if (win != null) {
                    return win;
                }
            }
            return null;
        }

        WindowState findFocusedWindowLocked(DisplayContent displayContent) {
            WindowList windows = displayContent.getWindowList();
            for (int i = windows.size() - 1; i >= 0; i--) {
                WindowState win = (WindowState) windows.get(i);
                if (win.canReceiveKeys()) {
                    AppWindowToken wtoken = win.mAppToken;
                    if (wtoken == null || !(wtoken.removed || wtoken.sendingToBottom)) {
                        if (!(wtoken == null || win.mAttrs.type == UPDATE_FOCUS_WILL_PLACE_SURFACES || this.mFocusedApp == null)) {
                            ArrayList<Task> tasks = displayContent.getTasks();
                            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                                int tokenNdx = tokens.size() - 1;
                                while (tokenNdx >= 0) {
                                    AppWindowToken token = (AppWindowToken) tokens.get(tokenNdx);
                                    if (wtoken == token) {
                                        break;
                                    } else if (this.mFocusedApp == token && token.windowsAreFocusable()) {
                                        return null;
                                    } else {
                                        tokenNdx--;
                                    }
                                }
                                if (tokenNdx >= 0) {
                                    break;
                                }
                            }
                        }
                        return win;
                    }
                }
            }
            return null;
        }

        private void startFreezingDisplayLocked(boolean inTransaction, int exitAnim, int enterAnim) {
            if (!this.mDisplayFrozen && this.mDisplayReady && this.mPolicy.isScreenOn()) {
                this.mScreenFrozenLock.acquire();
                this.mDisplayFrozen = SCREENSHOT_FORCE_565;
                this.mDisplayFreezeTime = SystemClock.elapsedRealtime();
                this.mLastFinishedFreezeSource = null;
                this.mInputMonitor.freezeInputDispatchingLw();
                this.mPolicy.setLastInputMethodWindowLw(null, null);
                if (this.mAppTransition.isTransitionSet()) {
                    this.mAppTransition.freeze();
                }
                this.mExitAnimId = exitAnim;
                this.mEnterAnimId = enterAnim;
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                int displayId = displayContent.getDisplayId();
                ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation != null) {
                    screenRotationAnimation.kill();
                }
                boolean isSecure = PROFILE_ORIENTATION;
                WindowList windows = getDefaultWindowListLocked();
                int N = windows.size();
                for (int i = WINDOW_ANIMATION_SCALE; i < N; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    WindowState ws = (WindowState) windows.get(i);
                    if (ws.isOnScreen() && (ws.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0) {
                        isSecure = SCREENSHOT_FORCE_565;
                        break;
                    }
                }
                displayContent.updateDisplayInfo();
                this.mAnimator.setScreenRotationAnimationLocked(displayId, new ScreenRotationAnimation(this.mContext, displayContent, this.mFxSession, inTransaction, this.mPolicy.isDefaultOrientationForced(), isSecure));
            }
        }

        void stopFreezingDisplayLocked() {
            if (this.mDisplayFrozen) {
                boolean z;
                if (this.mWaitingForConfig || this.mAppsFreezingScreen > 0 || this.mWindowsFreezingScreen == WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    z = SCREENSHOT_FORCE_565;
                } else {
                    z = this.mClientFreezingScreen;
                }
                int size = this.mOpeningApps.size();
                if (z || size > 0) {
                    Slog.d("WindowManager", "stopFreezingDisplayLocked: Returning mWaitingForConfig=" + this.mWaitingForConfig + ", mAppsFreezingScreen=" + this.mAppsFreezingScreen + ", mWindowsFreezingScreen=" + this.mWindowsFreezingScreen + ", mClientFreezingScreen=" + this.mClientFreezingScreen + ", mOpeningApps.size()=" + this.mOpeningApps.size());
                    if (!z && size > 0) {
                        printFreezingDisplayLogs();
                    }
                    return;
                }
                this.mDisplayFrozen = PROFILE_ORIENTATION;
                this.mLastDisplayFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                StringBuilder stringBuilder = new StringBuilder(DumpState.DUMP_PACKAGES);
                stringBuilder.append("Screen frozen for ");
                TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, stringBuilder);
                if (this.mLastFinishedFreezeSource != null) {
                    stringBuilder.append(" due to ");
                    stringBuilder.append(this.mLastFinishedFreezeSource);
                }
                Slog.i("WindowManager", stringBuilder.toString());
                this.mH.removeMessages(17);
                this.mH.removeMessages(30);
                boolean updateRotation = PROFILE_ORIENTATION;
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                int displayId = displayContent.getDisplayId();
                ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation == null || !screenRotationAnimation.hasScreenshot()) {
                    if (screenRotationAnimation != null) {
                        screenRotationAnimation.kill();
                        this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                    }
                    updateRotation = SCREENSHOT_FORCE_565;
                } else {
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    if (!this.mPolicy.validateRotationAnimationLw(this.mExitAnimId, this.mEnterAnimId, displayContent.isDimming())) {
                        this.mEnterAnimId = WINDOW_ANIMATION_SCALE;
                        this.mExitAnimId = WINDOW_ANIMATION_SCALE;
                    }
                    if (screenRotationAnimation.dismiss(this.mFxSession, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight, this.mExitAnimId, this.mEnterAnimId)) {
                        scheduleAnimationLocked();
                    } else {
                        screenRotationAnimation.kill();
                        this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                        updateRotation = SCREENSHOT_FORCE_565;
                    }
                }
                this.mInputMonitor.thawInputDispatchingLw();
                boolean configChanged = updateOrientationFromAppTokensLocked(PROFILE_ORIENTATION);
                this.mH.removeMessages(15);
                this.mH.sendEmptyMessageDelayed(15, 2000);
                this.mScreenFrozenLock.release();
                if (updateRotation) {
                    configChanged |= updateRotationUncheckedLocked(PROFILE_ORIENTATION);
                }
                if (configChanged) {
                    this.mH.sendEmptyMessage(18);
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

        void createWatermarkInTransaction() {
            Throwable th;
            if (this.mWatermark == null) {
                FileInputStream fileInputStream = null;
                DataInputStream dataInputStream = null;
                try {
                    FileInputStream in = new FileInputStream(new File("/system/etc/setup.conf"));
                    try {
                        DataInputStream ind = new DataInputStream(in);
                        try {
                            String line = ind.readLine();
                            if (line != null) {
                                String[] toks = line.split("%");
                                if (toks != null && toks.length > 0) {
                                    this.mWatermark = new Watermark(getDefaultDisplayContentLocked().getDisplay(), this.mRealDisplayMetrics, this.mFxSession, toks);
                                }
                            }
                            if (ind != null) {
                                try {
                                    ind.close();
                                } catch (IOException e) {
                                }
                            } else if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e2) {
                                }
                            }
                            fileInputStream = in;
                        } catch (FileNotFoundException e3) {
                            dataInputStream = ind;
                            fileInputStream = in;
                            if (dataInputStream == null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e4) {
                                }
                            } else if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e5) {
                                }
                            }
                        } catch (IOException e6) {
                            dataInputStream = ind;
                            fileInputStream = in;
                            if (dataInputStream == null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e7) {
                                }
                            } else if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e8) {
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            dataInputStream = ind;
                            fileInputStream = in;
                            if (dataInputStream == null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e9) {
                                }
                            } else if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e10) {
                                }
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e11) {
                        fileInputStream = in;
                        if (dataInputStream == null) {
                            dataInputStream.close();
                        } else if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    } catch (IOException e12) {
                        fileInputStream = in;
                        if (dataInputStream == null) {
                            dataInputStream.close();
                        } else if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileInputStream = in;
                        if (dataInputStream == null) {
                            dataInputStream.close();
                        } else if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e13) {
                    if (dataInputStream == null) {
                        dataInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e14) {
                    if (dataInputStream == null) {
                        dataInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (dataInputStream == null) {
                        dataInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
        }

        public void statusBarVisibilityChanged(int visibility) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
                throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
            }
            synchronized (this.mWindowMap) {
                int diff = visibility ^ this.mLastStatusBarVisibility;
                this.mLastStatusBarVisibility = visibility;
                visibility = this.mPolicy.adjustSystemUiVisibilityLw(visibility);
                if ((diff & 201326592) == 201326592) {
                    DisplayContent displayContent = getDefaultDisplayContentLocked();
                    displayContent.pendingLayoutChanges |= WINDOWS_FREEZING_SCREENS_ACTIVE;
                    this.mWindowPlacerLocked.performSurfacePlacementInner(PROFILE_ORIENTATION);
                }
                updateStatusBarVisibilityLocked(visibility);
            }
        }

        public void reevaluateStatusBarVisibility() {
            synchronized (this.mWindowMap) {
                if (updateStatusBarVisibilityLocked(this.mPolicy.adjustSystemUiVisibilityLw(this.mLastStatusBarVisibility))) {
                    this.mWindowPlacerLocked.requestTraversal();
                }
            }
        }

        public InputConsumer addInputConsumer(Looper looper, Factory inputEventReceiverFactory) {
            HideNavInputConsumer inputConsumerImpl;
            synchronized (this.mWindowMap) {
                inputConsumerImpl = new HideNavInputConsumer(this, looper, inputEventReceiverFactory);
                this.mInputConsumer = inputConsumerImpl;
                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
            }
            return inputConsumerImpl;
        }

        boolean removeInputConsumer() {
            synchronized (this.mWindowMap) {
                if (this.mInputConsumer != null) {
                    this.mInputConsumer = null;
                    this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                    return SCREENSHOT_FORCE_565;
                }
                return PROFILE_ORIENTATION;
            }
        }

        public void createWallpaperInputConsumer(InputChannel inputChannel) {
            synchronized (this.mWindowMap) {
                this.mWallpaperInputConsumer = new InputConsumerImpl(this, "wallpaper input", inputChannel);
                this.mWallpaperInputConsumer.mWindowHandle.hasWallpaper = SCREENSHOT_FORCE_565;
                this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
            }
        }

        public void removeWallpaperInputConsumer() {
            synchronized (this.mWindowMap) {
                if (this.mWallpaperInputConsumer != null) {
                    this.mWallpaperInputConsumer.disposeChannelsLw();
                    this.mWallpaperInputConsumer = null;
                    this.mInputMonitor.updateInputWindowsLw(SCREENSHOT_FORCE_565);
                }
            }
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
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        return PROFILE_ORIENTATION;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        return PROFILE_ORIENTATION;
                    }
                    boolean clearWindowContentFrameStats = surfaceController.clearWindowContentFrameStats();
                    return clearWindowContentFrameStats;
                }
            }
            throw new SecurityException("Requires FRAME_STATS permission");
        }

        public WindowContentFrameStats getWindowContentFrameStats(IBinder token) {
            if (checkCallingPermission("android.permission.FRAME_STATS", "getWindowContentFrameStats()")) {
                synchronized (this.mWindowMap) {
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        return null;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        return null;
                    }
                    if (this.mTempWindowRenderStats == null) {
                        this.mTempWindowRenderStats = new WindowContentFrameStats();
                    }
                    WindowContentFrameStats stats = this.mTempWindowRenderStats;
                    if (surfaceController.getWindowContentFrameStats(stats)) {
                        return stats;
                    }
                    return null;
                }
            }
            throw new SecurityException("Requires FRAME_STATS permission");
        }

        public void notifyAppRelaunching(IBinder token) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindow = findAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.startRelaunching();
                }
            }
        }

        public void notifyAppRelaunchingFinished(IBinder token) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindow = findAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.finishRelaunching();
                }
            }
        }

        public int getDockedDividerInsetsLw() {
            return getDefaultDisplayContentLocked().getDockedDividerController().getContentInsets();
        }

        void dumpPolicyLocked(PrintWriter pw, String[] args, boolean dumpAll) {
            pw.println("WINDOW MANAGER POLICY STATE (dumpsys window policy)");
            this.mPolicy.dump("    ", pw, args);
        }

        void dumpAnimatorLocked(PrintWriter pw, String[] args, boolean dumpAll) {
            pw.println("WINDOW MANAGER ANIMATOR STATE (dumpsys window animator)");
            this.mAnimator.dumpLocked(pw, "    ", dumpAll);
        }

        void dumpTokensLocked(PrintWriter pw, boolean dumpAll) {
            WindowToken token;
            pw.println("WINDOW MANAGER TOKENS (dumpsys window tokens)");
            if (!this.mTokenMap.isEmpty()) {
                pw.println("  All tokens:");
                for (WindowToken token2 : this.mTokenMap.values()) {
                    pw.print("  ");
                    pw.print(token2);
                    if (dumpAll) {
                        pw.println(':');
                        token2.dump(pw, "    ");
                    } else {
                        pw.println();
                    }
                }
            }
            this.mWallpaperControllerLocked.dumpTokens(pw, "  ", dumpAll);
            if (!this.mFinishedStarting.isEmpty()) {
                pw.println();
                pw.println("  Finishing start of application tokens:");
                for (int i = this.mFinishedStarting.size() - 1; i >= 0; i--) {
                    token2 = (WindowToken) this.mFinishedStarting.get(i);
                    pw.print("  Finished Starting #");
                    pw.print(i);
                    pw.print(' ');
                    pw.print(token2);
                    if (dumpAll) {
                        pw.println(':');
                        token2.dump(pw, "    ");
                    } else {
                        pw.println();
                    }
                }
            }
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

        void dumpSessionsLocked(PrintWriter pw, boolean dumpAll) {
            pw.println("WINDOW MANAGER SESSIONS (dumpsys window sessions)");
            for (int i = WINDOW_ANIMATION_SCALE; i < this.mSessions.size(); i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                Session s = (Session) this.mSessions.valueAt(i);
                pw.print("  Session ");
                pw.print(s);
                pw.println(':');
                s.dump(pw, "    ");
            }
        }

        void dumpDisplayContentsLocked(PrintWriter pw, boolean dumpAll) {
            pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
            if (this.mDisplayReady) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).dump("  ", pw);
                }
                return;
            }
            pw.println("  NO DISPLAY");
        }

        void dumpWindowsLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
            pw.println("WINDOW MANAGER WINDOWS (dumpsys window windows)");
            dumpWindowsNoHeaderLocked(pw, dumpAll, windows);
        }

        void dumpWindowsNoHeaderLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
            int displayNdx;
            int i;
            int numDisplays = this.mDisplayContents.size();
            for (displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                WindowList windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                for (int winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState w = (WindowState) windowList.get(winNdx);
                    if (!w.toString().contains("hwSingleMode_window") && (windows == null || windows.contains(w))) {
                        pw.print("  Window #");
                        pw.print(winNdx);
                        pw.print(' ');
                        pw.print(w);
                        pw.println(":");
                        String str = "    ";
                        boolean z = (dumpAll || windows != null) ? SCREENSHOT_FORCE_565 : PROFILE_ORIENTATION;
                        w.dump(pw, str, z);
                    }
                }
            }
            if (this.mInputMethodDialogs.size() > 0) {
                pw.println();
                pw.println("  Input method dialogs:");
                for (i = this.mInputMethodDialogs.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mInputMethodDialogs.get(i);
                    if (windows == null || windows.contains(w)) {
                        pw.print("  IM Dialog #");
                        pw.print(i);
                        pw.print(": ");
                        pw.println(w);
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
                            w.dump(pw, "    ", SCREENSHOT_FORCE_565);
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
                        w.dump(pw, "    ", SCREENSHOT_FORCE_565);
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
                            w.dump(pw, "    ", SCREENSHOT_FORCE_565);
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
                            w.dump(pw, "    ", SCREENSHOT_FORCE_565);
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
                            w.dump(pw, "    ", SCREENSHOT_FORCE_565);
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
            pw.print("  mCurConfiguration=");
            pw.println(this.mCurConfiguration);
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
                this.mWallpaperControllerLocked.dump(pw, "  ");
                this.mLayersController.dump(pw, "  ");
                pw.print("  mSystemBooted=");
                pw.print(this.mSystemBooted);
                pw.print(" mDisplayEnabled=");
                pw.println(this.mDisplayEnabled);
                if (needsLayout()) {
                    pw.print("  layoutNeeded on displays=");
                    for (displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        DisplayContent displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                        if (displayContent.layoutNeeded) {
                            pw.print(displayContent.getDisplayId());
                        }
                    }
                    pw.println();
                }
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
                pw.print("  mRotation=");
                pw.print(this.mRotation);
                pw.print(" mAltOrientation=");
                pw.println(this.mAltOrientation);
                pw.print("  mLastWindowForcedOrientation=");
                pw.print(this.mLastWindowForcedOrientation);
                pw.print(" mForcedAppOrientation=");
                pw.println(this.mForcedAppOrientation);
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
                pw.print(" mSkipAppTransitionAnimation=");
                pw.println(this.mSkipAppTransitionAnimation);
                pw.println("  mLayoutToAnim:");
                this.mAppTransition.dump(pw, "    ");
            }
        }

        boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
            WindowList windows = new WindowList();
            HashMap hashMap;
            int numDisplays;
            int displayNdx;
            WindowList windowList;
            int winNdx;
            WindowState w;
            if ("apps".equals(name) || "visible".equals(name) || "visible-apps".equals(name)) {
                boolean appsOnly = name.contains("apps");
                boolean visibleOnly = name.contains("visible");
                hashMap = this.mWindowMap;
                synchronized (hashMap) {
                    if (appsOnly) {
                    }
                    numDisplays = this.mDisplayContents.size();
                    for (displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                        windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                        for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                            w = (WindowState) windowList.get(winNdx);
                            if ((!visibleOnly || w.mWinAnimator.getShown()) && !(appsOnly && w.mAppToken == null)) {
                                windows.add(w);
                            }
                        }
                    }
                }
                dumpDisplayContentsLocked(pw, SCREENSHOT_FORCE_565);
                numDisplays = this.mDisplayContents.size();
                for (displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                        w = (WindowState) windowList.get(winNdx);
                        windows.add(w);
                    }
                }
            } else {
                CharSequence name2;
                int objectId = WINDOW_ANIMATION_SCALE;
                try {
                    objectId = Integer.parseInt(name, 16);
                    name2 = null;
                } catch (RuntimeException e) {
                }
                hashMap = this.mWindowMap;
                synchronized (hashMap) {
                }
                numDisplays = this.mDisplayContents.size();
                for (displayNdx = WINDOW_ANIMATION_SCALE; displayNdx < numDisplays; displayNdx += WINDOWS_FREEZING_SCREENS_ACTIVE) {
                    windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                        w = (WindowState) windowList.get(winNdx);
                        if (name2 != null) {
                            if (w.mAttrs.getTitle().toString().contains(name2)) {
                                windows.add(w);
                            }
                        } else if (System.identityHashCode(w) == objectId) {
                            windows.add(w);
                        }
                    }
                }
            }
            if (windows.size() <= 0) {
                return PROFILE_ORIENTATION;
            }
            synchronized (this.mWindowMap) {
                dumpWindowsLocked(pw, dumpAll, windows);
            }
            return SCREENSHOT_FORCE_565;
        }

        void dumpLastANRLocked(PrintWriter pw) {
            pw.println("WINDOW MANAGER LAST ANR (dumpsys window lastanr)");
            if (this.mLastANRState == null) {
                pw.println("  <no ANR has occurred since boot>");
            } else {
                pw.println(this.mLastANRState);
            }
        }

        public void saveANRStateLocked(AppWindowToken appWindowToken, WindowState windowState, String reason) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new FastPrintWriter(sw, PROFILE_ORIENTATION, DumpState.DUMP_PROVIDERS);
            pw.println("  ANR time: " + DateFormat.getInstance().format(new Date()));
            if (appWindowToken != null) {
                pw.println("  Application at fault: " + appWindowToken.stringName);
            }
            if (windowState != null) {
                pw.println("  Window at fault: " + windowState.mAttrs.getTitle());
            }
            if (reason != null) {
                pw.println("  Reason: " + reason);
            }
            pw.println();
            dumpWindowsNoHeaderLocked(pw, SCREENSHOT_FORCE_565, null);
            pw.println();
            pw.println("Last ANR continued");
            dumpDisplayContentsLocked(pw, SCREENSHOT_FORCE_565);
            pw.close();
            this.mLastANRState = sw.toString();
            this.mH.removeMessages(38);
            this.mH.sendEmptyMessageDelayed(38, 7200000);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            String str = null;
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump WindowManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            boolean dumpAll = PROFILE_ORIENTATION;
            int opti = WINDOW_ANIMATION_SCALE;
            while (opti < args.length) {
                String opt = args[opti];
                if (opt != null && opt.length() > 0 && opt.charAt(WINDOW_ANIMATION_SCALE) == '-') {
                    opti += WINDOWS_FREEZING_SCREENS_ACTIVE;
                    if ("-a".equals(opt)) {
                        dumpAll = SCREENSHOT_FORCE_565;
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
            }
            if (opti < args.length) {
                String cmd = args[opti];
                opti += WINDOWS_FREEZING_SCREENS_ACTIVE;
                if ("lastanr".equals(cmd) || "l".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpLastANRLocked(pw);
                    }
                    return;
                } else if ("policy".equals(cmd) || "p".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpPolicyLocked(pw, args, SCREENSHOT_FORCE_565);
                    }
                    return;
                } else if ("animator".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpAnimatorLocked(pw, args, SCREENSHOT_FORCE_565);
                    }
                    return;
                } else if ("sessions".equals(cmd) || "s".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpSessionsLocked(pw, SCREENSHOT_FORCE_565);
                    }
                    return;
                } else if ("surfaces".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        SurfaceTrace.dumpAllSurfaces(pw, null);
                    }
                    return;
                } else if ("displays".equals(cmd) || "d".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpDisplayContentsLocked(pw, SCREENSHOT_FORCE_565);
                    }
                    return;
                } else if ("tokens".equals(cmd) || "t".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpTokensLocked(pw, SCREENSHOT_FORCE_565);
                    }
                    return;
                } else if ("windows".equals(cmd) || "w".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpWindowsLocked(pw, SCREENSHOT_FORCE_565, null);
                    }
                    return;
                } else if ("all".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpWindowsLocked(pw, SCREENSHOT_FORCE_565, null);
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
                dumpDisplayContentsLocked(pw, dumpAll);
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
            }
        }

        public void monitor() {
            synchronized (this.mWindowMap) {
            }
        }

        private DisplayContent newDisplayContentLocked(Display display) {
            DisplayContent displayContent = new DisplayContent(display, this);
            int displayId = display.getDisplayId();
            this.mDisplayContents.put(displayId, displayContent);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            Rect rect = new Rect();
            this.mDisplaySettings.getOverscanLocked(displayInfo.name, displayInfo.uniqueId, rect);
            displayInfo.overscanLeft = rect.left;
            displayInfo.overscanTop = rect.top;
            displayInfo.overscanRight = rect.right;
            displayInfo.overscanBottom = rect.bottom;
            this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, displayInfo);
            configureDisplayPolicyLocked(displayContent);
            if (displayId == 0) {
                displayContent.mTapDetector = new TaskTapPointerEventListener(this, displayContent);
                registerPointerEventListener(displayContent.mTapDetector);
                registerPointerEventListener(this.mMousePositionTracker);
            }
            return displayContent;
        }

        public void createDisplayContentLocked(Display display) {
            if (display == null) {
                throw new IllegalArgumentException("getDisplayContent: display must not be null");
            }
            getDisplayContentLocked(display.getDisplayId());
        }

        public DisplayContent getDisplayContentLocked(int displayId) {
            DisplayContent displayContent = (DisplayContent) this.mDisplayContents.get(displayId);
            if (displayContent != null) {
                return displayContent;
            }
            Display display = this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                return newDisplayContentLocked(display);
            }
            return displayContent;
        }

        public DisplayContent getDefaultDisplayContentLocked() {
            return getDisplayContentLocked(WINDOW_ANIMATION_SCALE);
        }

        public WindowList getDefaultWindowListLocked() {
            return getDefaultDisplayContentLocked().getWindowList();
        }

        public DisplayInfo getDefaultDisplayInfoLocked() {
            return getDefaultDisplayContentLocked().getDisplayInfo();
        }

        public WindowList getWindowListLocked(Display display) {
            return getWindowListLocked(display.getDisplayId());
        }

        public WindowList getWindowListLocked(int displayId) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                return displayContent.getWindowList();
            }
            return null;
        }

        public void onDisplayAdded(int displayId) {
            this.mH.sendMessage(this.mH.obtainMessage(27, displayId, WINDOW_ANIMATION_SCALE));
        }

        public void handleDisplayAdded(int displayId) {
            synchronized (this.mWindowMap) {
                Display display = this.mDisplayManager.getDisplay(displayId);
                if (display != null) {
                    createDisplayContentLocked(display);
                    displayReady(displayId);
                }
                this.mWindowPlacerLocked.requestTraversal();
            }
        }

        public void onDisplayRemoved(int displayId) {
            this.mH.sendMessage(this.mH.obtainMessage(28, displayId, WINDOW_ANIMATION_SCALE));
        }

        private void handleDisplayRemovedLocked(int displayId) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                if (displayContent.isAnimating()) {
                    displayContent.mDeferredRemoval = SCREENSHOT_FORCE_565;
                    return;
                }
                this.mDisplayContents.delete(displayId);
                displayContent.close();
                if (displayId == 0) {
                    unregisterPointerEventListener(displayContent.mTapDetector);
                    unregisterPointerEventListener(this.mMousePositionTracker);
                }
            }
            this.mAnimator.removeDisplayLocked(displayId);
            this.mWindowPlacerLocked.requestTraversal();
        }

        public void onDisplayChanged(int displayId) {
            this.mH.sendMessage(this.mH.obtainMessage(29, displayId, WINDOW_ANIMATION_SCALE));
        }

        private void handleDisplayChangedLocked(int displayId) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                displayContent.updateDisplayInfo();
            }
            this.mWindowPlacerLocked.requestTraversal();
        }

        public Object getWindowManagerLock() {
            return this.mWindowMap;
        }

        public void setReplacingWindow(IBinder token, boolean animate) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null || !appWindowToken.isVisible()) {
                    Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                    return;
                }
                appWindowToken.setReplacingWindows(animate);
            }
        }

        public void setReplacingWindows(IBinder token, boolean childrenOnly) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null || !appWindowToken.isVisible()) {
                    Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                    return;
                }
                if (childrenOnly) {
                    appWindowToken.setReplacingChildren();
                } else {
                    appWindowToken.setReplacingWindows(PROFILE_ORIENTATION);
                }
                scheduleClearReplacingWindowIfNeeded(token, SCREENSHOT_FORCE_565);
            }
        }

        public void scheduleClearReplacingWindowIfNeeded(IBinder token, boolean replacing) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w("WindowManager", "Attempted to reset replacing window on non-existing app token " + token);
                    return;
                }
                if (replacing) {
                    scheduleReplacingWindowTimeouts(appWindowToken);
                } else {
                    appWindowToken.resetReplacingWindows();
                }
            }
        }

        void scheduleReplacingWindowTimeouts(AppWindowToken appWindowToken) {
            if (!this.mReplacingWindowTimeouts.contains(appWindowToken)) {
                this.mReplacingWindowTimeouts.add(appWindowToken);
            }
            this.mH.removeMessages(46);
            this.mH.sendEmptyMessageDelayed(46, 2000);
        }

        public int getDockedStackSide() {
            int dockSide;
            synchronized (this.mWindowMap) {
                TaskStack dockedStack = getDefaultDisplayContentLocked().getDockedStackVisibleForUserLocked();
                dockSide = dockedStack == null ? -1 : dockedStack.getDockSide();
            }
            return dockSide;
        }

        public void setDockedStackResizing(boolean resizing) {
            synchronized (this.mWindowMap) {
                getDefaultDisplayContentLocked().getDockedDividerController().setResizing(resizing);
                requestTraversal();
            }
        }

        public void setDockedStackDividerTouchRegion(Rect touchRegion) {
            synchronized (this.mWindowMap) {
                getDefaultDisplayContentLocked().getDockedDividerController().setTouchRegion(touchRegion);
                setFocusTaskRegionLocked();
            }
        }

        public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
            synchronized (this.mWindowMap) {
                getDefaultDisplayContentLocked().getDockedDividerController().setResizeDimLayer(visible, targetStackId, alpha);
            }
        }

        public void animateResizePinnedStack(Rect bounds, int animationDuration) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(LAYOUT_REPEAT_THRESHOLD);
                if (stack == null) {
                    Slog.w(TAG, "animateResizePinnedStack: stackId 4 not found.");
                    return;
                }
                Rect originalBounds = new Rect();
                stack.getBounds(originalBounds);
                UiThread.getHandler().post(new AnonymousClass10(stack, originalBounds, bounds, animationDuration));
            }
        }

        public void setTaskResizeable(int taskId, int resizeMode) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.setResizeable(resizeMode);
                }
            }
        }

        public void setForceResizableTasks(boolean forceResizableTasks) {
            synchronized (this.mWindowMap) {
                this.mForceResizableTasks = forceResizableTasks;
            }
        }

        static int dipToPixel(int dip, DisplayMetrics displayMetrics) {
            return (int) TypedValue.applyDimension(WINDOWS_FREEZING_SCREENS_ACTIVE, (float) dip, displayMetrics);
        }

        public void registerDockedStackListener(IDockedStackListener listener) {
            if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerDockedStackListener()") || checkCallingPermission("huawei.android.permission.MULTIWINDOW_SDK", "registerDockedStackListener()")) {
                getDefaultDisplayContentLocked().mDividerControllerLocked.registerDockedStackListener(listener);
            }
        }

        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            try {
                WindowState focusedWindow = getFocusedWindow();
                if (focusedWindow != null && focusedWindow.mClient != null) {
                    getFocusedWindow().mClient.requestAppKeyboardShortcuts(receiver, deviceId);
                }
            } catch (RemoteException e) {
            }
        }

        public void getStableInsets(Rect outInsets) throws RemoteException {
            synchronized (this.mWindowMap) {
                getStableInsetsLocked(outInsets);
            }
        }

        void getStableInsetsLocked(Rect outInsets) {
            DisplayInfo di = getDefaultDisplayInfoLocked();
            this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
        }

        private void getNonDecorInsetsLocked(Rect outInsets) {
            DisplayInfo di = getDefaultDisplayInfoLocked();
            this.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
        }

        public void subtractStableInsets(Rect inOutBounds) {
            synchronized (this.mWindowMap) {
                getStableInsetsLocked(this.mTmpRect2);
                DisplayInfo di = getDefaultDisplayInfoLocked();
                this.mTmpRect.set(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, di.logicalWidth, di.logicalHeight);
                subtractInsets(this.mTmpRect, this.mTmpRect2, inOutBounds);
            }
        }

        public void subtractNonDecorInsets(Rect inOutBounds) {
            synchronized (this.mWindowMap) {
                getNonDecorInsetsLocked(this.mTmpRect2);
                DisplayInfo di = getDefaultDisplayInfoLocked();
                this.mTmpRect.set(WINDOW_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, di.logicalWidth, di.logicalHeight);
                subtractInsets(this.mTmpRect, this.mTmpRect2, inOutBounds);
            }
        }

        void subtractInsets(Rect display, Rect insets, Rect inOutBounds) {
            this.mTmpRect3.set(display);
            this.mTmpRect3.inset(insets);
            inOutBounds.intersect(this.mTmpRect3);
        }

        public int getSmallestWidthForTaskBounds(Rect bounds) {
            int smallestWidthDpForBounds;
            synchronized (this.mWindowMap) {
                smallestWidthDpForBounds = getDefaultDisplayContentLocked().getDockedDividerController().getSmallestWidthDpForBounds(bounds);
            }
            return smallestWidthDpForBounds;
        }

        void updatePointerIcon(IWindow client) {
            synchronized (this.mMousePositionTracker) {
                if (this.mMousePositionTracker.mLatestEventWasMouse) {
                    float mouseX = this.mMousePositionTracker.mLatestMouseX;
                    float mouseY = this.mMousePositionTracker.mLatestMouseY;
                    synchronized (this.mWindowMap) {
                        if (this.mDragState != null) {
                            return;
                        }
                        WindowState callingWin = windowForClientLocked(null, client, (boolean) PROFILE_ORIENTATION);
                        if (callingWin == null) {
                            Slog.w("WindowManager", "Bad requesting window " + client);
                            return;
                        }
                        DisplayContent displayContent = callingWin.getDisplayContent();
                        if (displayContent == null) {
                            return;
                        }
                        WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(mouseX, mouseY);
                        if (windowUnderPointer != callingWin) {
                            return;
                        }
                        try {
                            windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(mouseX), windowUnderPointer.translateToWindowY(mouseY));
                        } catch (RemoteException e) {
                            Slog.w("WindowManager", "unable to update pointer icon");
                        }
                        return;
                    }
                }
            }
        }

        void restorePointerIconLocked(DisplayContent displayContent, float latestX, float latestY) {
            this.mMousePositionTracker.updatePosition(latestX, latestY);
            WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(latestX, latestY);
            if (windowUnderPointer != null) {
                try {
                    windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(latestX), windowUnderPointer.translateToWindowY(latestY));
                    return;
                } catch (RemoteException e) {
                    Slog.w("WindowManager", "unable to restore pointer icon");
                    return;
                }
            }
            InputManager.getInstance().setPointerIconType(TYPE_LAYER_OFFSET);
        }

        public void registerShortcutKey(long shortcutCode, IShortcutService shortcutKeyReceiver) throws RemoteException {
            if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerShortcutKey")) {
                this.mPolicy.registerShortcutKey(shortcutCode, shortcutKeyReceiver);
                return;
            }
            throw new SecurityException("Requires REGISTER_WINDOW_MANAGER_LISTENERS permission");
        }

        public final void performhwLayoutAndPlaceSurfacesLocked() {
            this.mWindowPlacerLocked.performSurfacePlacement();
        }

        protected boolean canBeFloatImeTarget(WindowState w) {
            int fl = w.mAttrs.flags & 131080;
            if (fl == 0 || fl == 131080 || w.mAttrs.type == UPDATE_FOCUS_WILL_PLACE_SURFACES) {
                return w.isVisibleOrAdding();
            }
            return PROFILE_ORIENTATION;
        }

        private void printFreezingDisplayLogs() {
            int appsCount = this.mOpeningApps.size();
            for (int i = WINDOW_ANIMATION_SCALE; i < appsCount; i += WINDOWS_FREEZING_SCREENS_ACTIVE) {
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
    }
