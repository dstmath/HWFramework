package android.view;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.ResourcesManager;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.contentsensor.ContentSensorManagerFactory;
import android.contentsensor.IContentSensorManager;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RenderNode;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.iawareperf.IHwRtgSchedImpl;
import android.media.AudioManager;
import android.media.TtmlUtils;
import android.net.TrafficStats;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.CalendarContract;
import android.rms.IHwAppInnerBoost;
import android.rms.iaware.HwDynBufManager;
import android.sysprop.DisplayProperties;
import android.telephony.SmsManager;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.LongArray;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Choreographer;
import android.view.DisplayCutout;
import android.view.IWindow;
import android.view.InputDevice;
import android.view.InputQueue;
import android.view.KeyCharacterMap;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeIdManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Scroller;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.PhoneFallbackEventHandler;
import com.android.internal.util.Preconditions;
import com.android.internal.view.BaseSurfaceHolder;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.SurfaceCallbackHelper;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.IHwWindowManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public final class ViewRootImpl implements ViewParent, View.AttachInfo.Callbacks, ThreadedRenderer.DrawCallbacks {
    private static final boolean DBG = false;
    private static final boolean DEBUG_CONFIGURATION = false;
    private static final boolean DEBUG_CONTENT_CAPTURE = false;
    private static final boolean DEBUG_DIALOG = false;
    private static final boolean DEBUG_DRAW = false;
    private static final boolean DEBUG_FPS = false;
    private static final boolean DEBUG_HWFLOW = (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getInt("ro.logsystem.usertype", 1) == 3 || SystemProperties.getInt("ro.logsystem.usertype", 1) == 5);
    private static final boolean DEBUG_IMF = false;
    private static final boolean DEBUG_INPUT_RESIZE = false;
    private static final boolean DEBUG_INPUT_STAGES = false;
    private static final boolean DEBUG_KEEP_SCREEN_ON = false;
    private static final boolean DEBUG_LAYOUT = false;
    private static final boolean DEBUG_MOVE = (SystemProperties.getBoolean("ro.config.hw_log", false) || (SystemProperties.getBoolean("ro.debuggable", false) && SystemProperties.getBoolean("persist.sys.input.debug_move", false)));
    private static final boolean DEBUG_ORIENTATION = false;
    private static final boolean DEBUG_TRACKBALL = false;
    public static final boolean DEBUG_VIEW_TRACE = SystemProperties.getBoolean("ro.config.hw_view_trace", false);
    private static final long INVALID_TIME = 0;
    private static final boolean IS_FOLD_DISP = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty());
    private static final boolean IS_SIDE_PROP = (!SystemProperties.get("ro.config.hw_curved_side_disp", "").equals(""));
    private static final boolean LOCAL_LOGV = false;
    private static final int MAX_QUEUED_INPUT_EVENT_POOL_SIZE = 10;
    static final int MAX_TRACKBALL_DELAY = 250;
    protected static final long MIN_PERIOD = 500;
    private static final int MSG_CHECK_FOCUS = 13;
    private static final int MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST = 21;
    private static final int MSG_CLOSE_SYSTEM_DIALOGS = 14;
    private static final int MSG_DIE = 3;
    private static final int MSG_DISPATCH_APP_VISIBILITY = 8;
    private static final int MSG_DISPATCH_DRAG_ENTERED = 20;
    private static final int MSG_DISPATCH_DRAG_EVENT = 15;
    private static final int MSG_DISPATCH_DRAG_LOCATION_EVENT = 16;
    private static final int MSG_DISPATCH_GET_NEW_SURFACE = 9;
    private static final int MSG_DISPATCH_INPUT_EVENT = 7;
    private static final int MSG_DISPATCH_KEY_FROM_AUTOFILL = 12;
    private static final int MSG_DISPATCH_KEY_FROM_IME = 11;
    private static final int MSG_DISPATCH_SYSTEM_UI_VISIBILITY = 17;
    private static final int MSG_DISPATCH_WINDOW_SHOWN = 25;
    private static final int MSG_DRAW_FINISHED = 29;
    private static final int MSG_INSETS_CHANGED = 30;
    private static final int MSG_INSETS_CONTROL_CHANGED = 31;
    private static final int MSG_INVALIDATE = 1;
    private static final int MSG_INVALIDATE_RECT = 2;
    private static final int MSG_INVALIDATE_WORLD = 22;
    private static final int MSG_POINTER_CAPTURE_CHANGED = 28;
    private static final int MSG_PROCESS_INPUT_EVENTS = 19;
    private static final int MSG_REQUEST_KEYBOARD_SHORTCUTS = 26;
    private static final int MSG_RESIZED = 4;
    private static final int MSG_RESIZED_REPORT = 5;
    private static final int MSG_SYNTHESIZE_INPUT_EVENT = 24;
    private static final int MSG_SYSTEM_GESTURE_EXCLUSION_CHANGED = 32;
    private static final int MSG_UPDATE_CONFIGURATION = 18;
    private static final int MSG_UPDATE_POINTER_ICON = 27;
    private static final int MSG_WINDOW_FOCUS_CHANGED = 6;
    private static final int MSG_WINDOW_MOVED = 23;
    private static final boolean MT_RENDERER_AVAILABLE = true;
    public static final int NEW_INSETS_MODE_FULL = 2;
    public static final int NEW_INSETS_MODE_IME = 1;
    public static final int NEW_INSETS_MODE_NONE = 0;
    private static final float PENDING_DROP_VIEW_LOC_DIVIDER = 2.0f;
    private static final int PENDING_DROP_VIEW_LOC_SIZE = 2;
    public static final String PROPERTY_EMULATOR_WIN_OUTSET_BOTTOM_PX = "ro.emu.win_outset_bottom_px";
    private static final String PROPERTY_PROFILE_RENDERING = "viewroot.profile_rendering";
    private static final String TAG = "ViewRootImpl";
    private static final String TAG_INPUT_DISPATCH = "InputEvent";
    private static final String USE_NEW_INSETS_PROPERTY = "persist.wm.new_insets";
    static final Interpolator mResizeInterpolator = new AccelerateDecelerateInterpolator();
    private static boolean sAlwaysAssignFocus;
    private static boolean sCompatibilityDone = false;
    private static final ArrayList<ConfigChangedCallback> sConfigCallbacks = new ArrayList<>();
    static boolean sFirstDrawComplete = false;
    static final ArrayList<Runnable> sFirstDrawHandlers = new ArrayList<>();
    protected static long sLastRelayoutNotifyTime = 0;
    public static int sNewInsetsMode = SystemProperties.getInt(USE_NEW_INSETS_PROPERTY, 0);
    protected static long sRelayoutNotifyPeriod = 0;
    @UnsupportedAppUsage
    static final ThreadLocal<HandlerActionQueue> sRunQueues = new ThreadLocal<>();
    private static final boolean sSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    static final RemoteCallbackList<IWindowLayoutObserver> sWindowLayoutObservers = new RemoteCallbackList<>();
    public int hwForceDarkState;
    View mAccessibilityFocusedHost;
    AccessibilityNodeInfo mAccessibilityFocusedVirtualView;
    final AccessibilityInteractionConnectionManager mAccessibilityInteractionConnectionManager = new AccessibilityInteractionConnectionManager();
    AccessibilityInteractionController mAccessibilityInteractionController;
    final AccessibilityManager mAccessibilityManager;
    private ActivityConfigCallback mActivityConfigCallback;
    private boolean mActivityRelaunched;
    @UnsupportedAppUsage
    boolean mAdded;
    boolean mAddedTouchMode;
    private boolean mAppVisibilityChanged;
    boolean mAppVisible = true;
    boolean mApplyInsetsRequested;
    @UnsupportedAppUsage
    final View.AttachInfo mAttachInfo;
    AudioManager mAudioManager;
    final String mBasePackageName;
    public final Surface mBoundsSurface = new Surface();
    public SurfaceControl mBoundsSurfaceControl;
    private int mCanvasOffsetX;
    private int mCanvasOffsetY;
    Choreographer mChoreographer;
    int mClientWindowLayoutFlags;
    final ConsumeBatchedInputImmediatelyRunnable mConsumeBatchedInputImmediatelyRunnable;
    boolean mConsumeBatchedInputImmediatelyScheduled;
    boolean mConsumeBatchedInputScheduled;
    final ConsumeBatchedInputRunnable mConsumedBatchedInputRunnable;
    @UnsupportedAppUsage
    public final Context mContext;
    int mCurScrollY;
    View mCurrentDragView;
    private PointerIcon mCustomPointerIcon = null;
    private final int mDensity;
    public int mDetectedFlag;
    @UnsupportedAppUsage
    Rect mDirty;
    final Rect mDispatchContentInsets = new Rect();
    DisplayCutout mDispatchDisplayCutout = DisplayCutout.NO_CUTOUT;
    final Rect mDispatchStableInsets = new Rect();
    Display mDisplay;
    private final DisplayManager.DisplayListener mDisplayListener;
    final DisplayManager mDisplayManager;
    ClipDescription mDragDescription;
    final PointF mDragPoint = new PointF();
    private boolean mDragResizing;
    boolean mDrawingAllowed;
    int mDrawsNeededToReport;
    private boolean mEventChanged = false;
    @UnsupportedAppUsage
    FallbackEventHandler mFallbackEventHandler;
    boolean mFirst;
    InputStage mFirstInputStage;
    InputStage mFirstPostImeInputStage;
    private boolean mForceDecorViewVisibility = false;
    private boolean mForceNextConfigUpdate;
    boolean mForceNextWindowRelayout;
    private int mFpsNumFrames;
    private long mFpsPrevTime = -1;
    private long mFpsStartTime = -1;
    boolean mFullRedrawNeeded;
    private final GestureExclusionTracker mGestureExclusionTracker;
    boolean mHadWindowFocus;
    final ViewRootHandler mHandler;
    boolean mHandlingLayoutInLayoutRequest = false;
    int mHardwareXOffset;
    int mHardwareYOffset;
    boolean mHasHadWindowFocus;
    @UnsupportedAppUsage
    int mHeight;
    final HighContrastTextManager mHighContrastTextManager;
    private final IHwAppInnerBoost mHwAppInnerBoost;
    IHwBlurWindowManager mHwBlurWindowManager = HwFrameworkFactory.getHwBlurWindowManager();
    private final IHwRio mHwRio;
    IHwViewRootImpl mHwViewRootImpl;
    private boolean mInLayout = false;
    InputChannel mInputChannel;
    private final InputEventCompatProcessor mInputCompatProcessor;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    WindowInputEventReceiver mInputEventReceiver;
    InputQueue mInputQueue;
    InputQueue.Callback mInputQueueCallback;
    private final InsetsController mInsetsController;
    final InvalidateOnAnimationRunnable mInvalidateOnAnimationRunnable;
    private boolean mInvalidateRootRequested;
    boolean mIsAmbientMode = false;
    public boolean mIsAnimating;
    boolean mIsCreating;
    boolean mIsDrawing;
    private boolean mIsInBasicMode;
    boolean mIsInTraversal;
    private final Configuration mLastConfigurationFromResources = new Configuration();
    final ViewTreeObserver.InternalInsetsInfo mLastGivenInsets = new ViewTreeObserver.InternalInsetsInfo();
    boolean mLastInCompatMode = false;
    boolean mLastOverscanRequested;
    private final MergedConfiguration mLastReportedMergedConfiguration = new MergedConfiguration();
    @UnsupportedAppUsage
    WeakReference<View> mLastScrolledFocus;
    int mLastSystemUiVisibility;
    final PointF mLastTouchPoint = new PointF();
    int mLastTouchSource;
    boolean mLastWasImTarget;
    private WindowInsets mLastWindowInsets;
    boolean mLayoutRequested;
    ArrayList<View> mLayoutRequesters = new ArrayList<>();
    volatile Object mLocalDragState;
    final WindowLeaked mLocation;
    boolean mLostWindowFocus;
    private boolean mNeedsRendererSetup;
    boolean mNewSurfaceNeeded;
    private final int mNoncompatDensity;
    public Point mOffset = null;
    int mOrigWindowType = -1;
    boolean mPausedForTransition = false;
    boolean mPendingAlwaysConsumeSystemBars;
    final Rect mPendingBackDropFrame = new Rect();
    final Rect mPendingContentInsets = new Rect();
    final DisplayCutout.ParcelableWrapper mPendingDisplayCutout = new DisplayCutout.ParcelableWrapper(DisplayCutout.NO_CUTOUT);
    int mPendingInputEventCount;
    QueuedInputEvent mPendingInputEventHead;
    String mPendingInputEventQueueLengthCounterName = "pq";
    QueuedInputEvent mPendingInputEventTail;
    private final MergedConfiguration mPendingMergedConfiguration = new MergedConfiguration();
    final Rect mPendingOutsets = new Rect();
    final Rect mPendingOverscanInsets = new Rect();
    final Rect mPendingStableInsets = new Rect();
    private ArrayList<LayoutTransition> mPendingTransitions;
    final Rect mPendingVisibleInsets = new Rect();
    boolean mPointerCapture;
    private int mPointerIconType = 1;
    final Region mPreviousTransparentRegion;
    boolean mProcessInputEventsScheduled;
    private boolean mProfile;
    private boolean mProfileRendering;
    private QueuedInputEvent mQueuedInputEventPool;
    private int mQueuedInputEventPoolSize;
    private boolean mRemoved;
    private Choreographer.FrameCallback mRenderProfiler;
    private boolean mRenderProfilingEnabled;
    boolean mReportNextDraw;
    private int mResizeMode;
    boolean mScrollMayChange;
    int mScrollY;
    Scroller mScroller;
    SendWindowContentChangedAccessibilityEvent mSendWindowContentChangedAccessibilityEvent;
    private View mSenderView;
    int mSeq;
    int mSoftInputMode;
    @UnsupportedAppUsage
    boolean mStopped = false;
    @UnsupportedAppUsage
    public final Surface mSurface = new Surface();
    private final SurfaceControl mSurfaceControl = new SurfaceControl();
    BaseSurfaceHolder mSurfaceHolder;
    SurfaceHolder.Callback2 mSurfaceHolderCallback;
    private SurfaceSession mSurfaceSession;
    InputStage mSyntheticInputStage;
    private String mTag;
    final int mTargetSdkVersion;
    private final Rect mTempBoundsRect = new Rect();
    HashSet<View> mTempHashSet;
    private InsetsState mTempInsets = new InsetsState();
    final Rect mTempRect;
    public final Thread mThread;
    final Rect mTmpFrame = new Rect();
    final int[] mTmpLocation = new int[2];
    final TypedValue mTmpValue = new TypedValue();
    private final SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();
    CompatibilityInfo.Translator mTranslator;
    final Region mTransparentRegion;
    int mTraversalBarrier;
    final TraversalRunnable mTraversalRunnable;
    public boolean mTraversalScheduled;
    boolean mUnbufferedInputDispatch;
    private final UnhandledKeyManager mUnhandledKeyManager = new UnhandledKeyManager();
    @GuardedBy({"this"})
    boolean mUpcomingInTouchMode;
    @GuardedBy({"this"})
    boolean mUpcomingWindowFocus;
    private boolean mUseMTRenderer;
    @UnsupportedAppUsage
    View mView;
    final ViewConfiguration mViewConfiguration;
    public int mViewCount;
    private int mViewLayoutDirectionInitial;
    int mViewVisibility;
    final Rect mVisRect;
    @UnsupportedAppUsage
    int mWidth;
    boolean mWillDrawSoon;
    final Rect mWinFrame;
    final W mWindow;
    public final WindowManager.LayoutParams mWindowAttributes = new WindowManager.LayoutParams();
    boolean mWindowAttributesChanged = false;
    int mWindowAttributesChangesFlag = 0;
    @GuardedBy({"mWindowCallbacks"})
    final ArrayList<WindowCallbacks> mWindowCallbacks = new ArrayList<>();
    CountDownLatch mWindowDrawCountDown;
    @GuardedBy({"this"})
    boolean mWindowFocusChanged;
    @UnsupportedAppUsage
    final IWindowSession mWindowSession;
    private final ArrayList<WindowStoppedCallback> mWindowStoppedCallbacks;

    public interface ActivityConfigCallback {
        void onConfigurationChanged(Configuration configuration, int i);
    }

    public interface ConfigChangedCallback {
        void onConfigurationChanged(Configuration configuration);
    }

    interface WindowStoppedCallback {
        void windowStopped(boolean z);
    }

    /* access modifiers changed from: package-private */
    public static final class SystemUiVisibilityInfo {
        int globalVisibility;
        int localChanges;
        int localValue;
        int seq;

        SystemUiVisibilityInfo() {
        }
    }

    public ViewRootImpl(Context context, Display display) {
        boolean z = false;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mInsetsController = new InsetsController(this);
        this.mGestureExclusionTracker = new GestureExclusionTracker();
        this.mTag = TAG;
        this.mHwViewRootImpl = HwFrameworkFactory.getHwViewRootImpl();
        this.mHwAppInnerBoost = HwFrameworkFactory.getHwAppInnerBoostImpl();
        this.mHwRio = HwFrameworkFactory.getHwRioImpl();
        this.mIsInBasicMode = false;
        this.mProfile = false;
        this.hwForceDarkState = 0;
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            /* class android.view.ViewRootImpl.AnonymousClass1 */

            @Override // android.hardware.display.DisplayManager.DisplayListener
            @RCUnownedThisRef
            public void onDisplayChanged(int displayId) {
                int oldDisplayState;
                int newDisplayState;
                if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mDisplay != null && ViewRootImpl.this.mDisplay.getDisplayId() == displayId && (oldDisplayState = ViewRootImpl.this.mAttachInfo.mDisplayState) != (newDisplayState = ViewRootImpl.this.mDisplay.getState())) {
                    ViewRootImpl.this.mAttachInfo.mDisplayState = newDisplayState;
                    ViewRootImpl.this.pokeDrawLockIfNeeded();
                    if (oldDisplayState != 0) {
                        int oldScreenState = toViewScreenState(oldDisplayState);
                        int newScreenState = toViewScreenState(newDisplayState);
                        if (oldScreenState != newScreenState) {
                            ViewRootImpl.this.mView.dispatchScreenStateChanged(newScreenState);
                        }
                        if (oldDisplayState == 1) {
                            ViewRootImpl viewRootImpl = ViewRootImpl.this;
                            viewRootImpl.mFullRedrawNeeded = true;
                            viewRootImpl.scheduleTraversals();
                        }
                    }
                }
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int displayId) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int displayId) {
            }

            private int toViewScreenState(int displayState) {
                if (displayState == 1) {
                    return 0;
                }
                return 1;
            }
        };
        this.mWindowStoppedCallbacks = new ArrayList<>();
        this.mDrawsNeededToReport = 0;
        this.mHandler = new ViewRootHandler();
        this.mTraversalRunnable = new TraversalRunnable();
        this.mConsumedBatchedInputRunnable = new ConsumeBatchedInputRunnable();
        this.mConsumeBatchedInputImmediatelyRunnable = new ConsumeBatchedInputImmediatelyRunnable();
        this.mInvalidateOnAnimationRunnable = new InvalidateOnAnimationRunnable();
        this.mContext = context;
        this.mWindowSession = WindowManagerGlobal.getWindowSession();
        this.mDisplay = display;
        this.mBasePackageName = context.getBasePackageName();
        this.mThread = Thread.currentThread();
        this.mLocation = new WindowLeaked(null);
        this.mLocation.fillInStackTrace();
        this.mWidth = -1;
        this.mHeight = -1;
        this.mDirty = new Rect();
        this.mTempRect = new Rect();
        this.mVisRect = new Rect();
        this.mWinFrame = new Rect();
        this.mWindow = new W(this);
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        this.mViewVisibility = 8;
        this.mTransparentRegion = new Region();
        this.mPreviousTransparentRegion = new Region();
        this.mFirst = true;
        this.mAdded = false;
        this.mAttachInfo = new View.AttachInfo(this.mWindowSession, this.mWindow, display, this, this.mHandler, this, context);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAccessibilityManager.addAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager, this.mHandler);
        this.mHighContrastTextManager = new HighContrastTextManager();
        this.mAccessibilityManager.addHighTextContrastStateChangeListener(this.mHighContrastTextManager, this.mHandler);
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        this.mNoncompatDensity = context.getResources().getDisplayMetrics().noncompatDensityDpi;
        this.mFallbackEventHandler = new PhoneFallbackEventHandler(context);
        this.mChoreographer = Choreographer.getInstance();
        this.mDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        String processorOverrideName = context.getResources().getString(R.string.config_inputEventCompatProcessorOverrideClassName);
        if (processorOverrideName.isEmpty()) {
            this.mInputCompatProcessor = new InputEventCompatProcessor(context);
        } else {
            InputEventCompatProcessor compatProcessor = null;
            try {
                compatProcessor = (InputEventCompatProcessor) Class.forName(processorOverrideName).getConstructor(Context.class).newInstance(context);
            } catch (Exception e) {
                Log.e(TAG, "Unable to create the InputEventCompatProcessor. ", e);
            } finally {
                this.mInputCompatProcessor = compatProcessor;
            }
        }
        if (!sCompatibilityDone) {
            sAlwaysAssignFocus = this.mTargetSdkVersion < 28 ? true : z;
            sCompatibilityDone = true;
        }
        loadSystemProperties();
        Point pt = new Point();
        this.mDisplay.getRealSize(pt);
        HwFrameworkFactory.getHwViewRootImpl().setRealSize(pt);
        IHwAppInnerBoost iHwAppInnerBoost = this.mHwAppInnerBoost;
        if (iHwAppInnerBoost != null) {
            iHwAppInnerBoost.initialize(context.getPackageName());
        }
        this.mHwViewRootImpl.savePkgName(context.getPackageName());
    }

    public static void addFirstDrawHandler(Runnable callback) {
        synchronized (sFirstDrawHandlers) {
            if (!sFirstDrawComplete) {
                sFirstDrawHandlers.add(callback);
            }
        }
    }

    @UnsupportedAppUsage
    public static void addConfigCallback(ConfigChangedCallback callback) {
        synchronized (sConfigCallbacks) {
            sConfigCallbacks.add(callback);
        }
    }

    public void setActivityConfigCallback(ActivityConfigCallback callback) {
        this.mActivityConfigCallback = callback;
    }

    public void addWindowCallbacks(WindowCallbacks callback) {
        synchronized (this.mWindowCallbacks) {
            this.mWindowCallbacks.add(callback);
        }
    }

    public void removeWindowCallbacks(WindowCallbacks callback) {
        synchronized (this.mWindowCallbacks) {
            this.mWindowCallbacks.remove(callback);
        }
    }

    public void reportDrawFinish() {
        CountDownLatch countDownLatch = this.mWindowDrawCountDown;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public void profile() {
        this.mProfile = true;
    }

    static boolean isInTouchMode() {
        IWindowSession windowSession = WindowManagerGlobal.peekWindowSession();
        if (windowSession == null) {
            return false;
        }
        try {
            return windowSession.getInTouchMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void notifyChildRebuilt() {
        if (this.mView instanceof RootViewSurfaceTaker) {
            SurfaceHolder.Callback2 callback2 = this.mSurfaceHolderCallback;
            if (callback2 != null) {
                this.mSurfaceHolder.removeCallback(callback2);
            }
            this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheSurface();
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder = new TakenSurfaceHolder();
                this.mSurfaceHolder.setFormat(0);
                this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
            } else {
                this.mSurfaceHolder = null;
            }
            this.mInputQueueCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheInputQueue();
            InputQueue.Callback callback = this.mInputQueueCallback;
            if (callback != null) {
                callback.onInputQueueCreated(this.mInputQueue);
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x046a  */
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        Throwable th;
        WindowManager.LayoutParams attrs2;
        boolean restore;
        WindowManager.LayoutParams attrs3;
        RemoteException e;
        RemoteException e2;
        int res;
        synchronized (this) {
            try {
                if (this.mView == null) {
                    this.mView = view;
                    this.mAttachInfo.mDisplayState = this.mDisplay.getState();
                    this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
                    this.mViewLayoutDirectionInitial = this.mView.getRawLayoutDirection();
                    this.mFallbackEventHandler.setView(view);
                    try {
                        this.mWindowAttributes.copyFrom(attrs);
                        if (this.mWindowAttributes.packageName == null) {
                            this.mWindowAttributes.packageName = this.mBasePackageName;
                        }
                        attrs2 = this.mWindowAttributes;
                        try {
                            setTag();
                            this.mHwRio.attachRio(this.mContext, getView(), getTitle(), this.mDisplay);
                            this.mClientWindowLayoutFlags = attrs2.flags;
                            setAccessibilityFocus(null, null);
                            if (view instanceof RootViewSurfaceTaker) {
                                try {
                                    this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) view).willYouTakeTheSurface();
                                    if (this.mSurfaceHolderCallback != null) {
                                        this.mSurfaceHolder = new TakenSurfaceHolder();
                                        this.mSurfaceHolder.setFormat(0);
                                        this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            if (!attrs2.hasManualSurfaceInsets) {
                                attrs2.setSurfaceInsets(view, false, true);
                            }
                            CompatibilityInfo compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                            this.mTranslator = compatibilityInfo.getTranslator();
                            if (this.mSurfaceHolder == null) {
                                enableHardwareAcceleration(attrs2);
                                boolean useMTRenderer = this.mAttachInfo.mThreadedRenderer != null;
                                if (this.mUseMTRenderer != useMTRenderer) {
                                    endDragResizing();
                                    this.mUseMTRenderer = useMTRenderer;
                                }
                            }
                            if (this.mTranslator != null) {
                                this.mSurface.setCompatibilityTranslator(this.mTranslator);
                                attrs2.backup();
                                this.mTranslator.translateWindowLayout(attrs2);
                                restore = true;
                            } else {
                                restore = false;
                            }
                            if (!compatibilityInfo.supportsScreen()) {
                                attrs2.privateFlags |= 128;
                                this.mLastInCompatMode = true;
                            }
                            this.mSoftInputMode = attrs2.softInputMode;
                            this.mWindowAttributesChanged = true;
                            this.mWindowAttributesChangesFlag = -1;
                            this.mAttachInfo.mRootView = view;
                            this.mAttachInfo.mScalingRequired = this.mTranslator != null;
                            this.mAttachInfo.mApplicationScale = this.mTranslator == null ? 1.0f : this.mTranslator.applicationScale;
                            if (panelParentView != null) {
                                this.mAttachInfo.mPanelParentWindowToken = panelParentView.getApplicationWindowToken();
                            }
                            this.mAdded = true;
                            requestLayout();
                            if ((this.mWindowAttributes.inputFeatures & 2) == 0) {
                                this.mInputChannel = new InputChannel();
                            }
                            this.mForceDecorViewVisibility = (this.mWindowAttributes.privateFlags & 16384) != 0;
                            try {
                                this.mOrigWindowType = this.mWindowAttributes.type;
                                this.mAttachInfo.mRecomputeGlobalAttributes = true;
                                collectViewAttributes();
                                try {
                                } catch (RemoteException e3) {
                                    e2 = e3;
                                    attrs3 = attrs2;
                                    try {
                                        this.mAdded = false;
                                        this.mView = null;
                                        this.mAttachInfo.mRootView = null;
                                        this.mInputChannel = null;
                                        this.mFallbackEventHandler.setView(null);
                                        unscheduleTraversals();
                                        setAccessibilityFocus(null, null);
                                        throw new RuntimeException("Adding window failed", e2);
                                    } catch (Throwable th3) {
                                        e = th3;
                                        if (restore) {
                                            attrs3.restore();
                                        }
                                        throw e;
                                    }
                                } catch (Throwable th4) {
                                    e = th4;
                                    attrs3 = attrs2;
                                    if (restore) {
                                    }
                                    throw e;
                                }
                                try {
                                    res = this.mWindowSession.addToDisplay(this.mWindow, this.mSeq, this.mWindowAttributes, getHostVisibility(), this.mDisplay.getDisplayId(), this.mTmpFrame, this.mAttachInfo.mContentInsets, this.mAttachInfo.mStableInsets, this.mAttachInfo.mOutsets, this.mAttachInfo.mDisplayCutout, this.mInputChannel, this.mTempInsets);
                                    if (!compatibilityInfo.supportsScreen()) {
                                        try {
                                            this.mTmpFrame.scale(compatibilityInfo.getSdrLowResolutionRatio());
                                        } catch (RemoteException e4) {
                                            e2 = e4;
                                            attrs3 = attrs2;
                                        } catch (Throwable th5) {
                                            e = th5;
                                            attrs3 = attrs2;
                                            if (restore) {
                                            }
                                            throw e;
                                        }
                                    }
                                    setFrame(this.mTmpFrame);
                                    if (restore) {
                                        try {
                                            attrs2.restore();
                                        } catch (Throwable th6) {
                                            th = th6;
                                        }
                                    }
                                    try {
                                        if (this.mTranslator != null) {
                                            this.mTranslator.translateRectInScreenToAppWindow(this.mAttachInfo.mContentInsets);
                                        }
                                        this.mPendingOverscanInsets.set(0, 0, 0, 0);
                                        this.mPendingContentInsets.set(this.mAttachInfo.mContentInsets);
                                        this.mPendingStableInsets.set(this.mAttachInfo.mStableInsets);
                                        this.mPendingDisplayCutout.set(this.mAttachInfo.mDisplayCutout);
                                        this.mPendingVisibleInsets.set(0, 0, 0, 0);
                                        this.mAttachInfo.mAlwaysConsumeSystemBars = (res & 4) != 0;
                                        this.mPendingAlwaysConsumeSystemBars = this.mAttachInfo.mAlwaysConsumeSystemBars;
                                        this.mInsetsController.onStateChanged(this.mTempInsets);
                                    } catch (Throwable th7) {
                                        th = th7;
                                    }
                                } catch (RemoteException e5) {
                                    e2 = e5;
                                    attrs3 = attrs2;
                                    this.mAdded = false;
                                    this.mView = null;
                                    this.mAttachInfo.mRootView = null;
                                    this.mInputChannel = null;
                                    this.mFallbackEventHandler.setView(null);
                                    unscheduleTraversals();
                                    setAccessibilityFocus(null, null);
                                    throw new RuntimeException("Adding window failed", e2);
                                } catch (Throwable th8) {
                                    e = th8;
                                    attrs3 = attrs2;
                                    if (restore) {
                                    }
                                    throw e;
                                }
                            } catch (RemoteException e6) {
                                e2 = e6;
                                attrs3 = attrs2;
                                this.mAdded = false;
                                this.mView = null;
                                this.mAttachInfo.mRootView = null;
                                this.mInputChannel = null;
                                this.mFallbackEventHandler.setView(null);
                                unscheduleTraversals();
                                setAccessibilityFocus(null, null);
                                throw new RuntimeException("Adding window failed", e2);
                            } catch (Throwable th9) {
                                e = th9;
                                attrs3 = attrs2;
                                if (restore) {
                                }
                                throw e;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                    }
                    if (res < 0) {
                        try {
                            this.mAttachInfo.mRootView = null;
                            this.mAdded = false;
                            this.mFallbackEventHandler.setView(null);
                            unscheduleTraversals();
                            setAccessibilityFocus(null, null);
                            switch (res) {
                                case -10:
                                    throw new WindowManager.InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified window type " + this.mWindowAttributes.type + " is not valid");
                                case -9:
                                    throw new WindowManager.InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified display can not be found");
                                case -8:
                                    throw new WindowManager.BadTokenException("Unable to add window " + this.mWindow + " -- permission denied for window type " + this.mWindowAttributes.type);
                                case -7:
                                    throw new WindowManager.BadTokenException("Unable to add window " + this.mWindow + " -- another window of type " + this.mWindowAttributes.type + " already exists");
                                case -6:
                                    return;
                                case -5:
                                    throw new WindowManager.BadTokenException("Unable to add window -- window " + this.mWindow + " has already been added");
                                case -4:
                                    throw new WindowManager.BadTokenException("Unable to add window -- app for token " + attrs2.token + " is exiting");
                                case -3:
                                    throw new WindowManager.BadTokenException("Unable to add window -- token " + attrs2.token + " is not for an application");
                                case -2:
                                case -1:
                                    throw new WindowManager.BadTokenException("Unable to add window -- token " + attrs2.token + " is not valid; is your activity running?");
                                default:
                                    try {
                                        throw new RuntimeException("Unable to add window -- unknown error code " + res);
                                    } catch (Throwable th12) {
                                        th = th12;
                                        break;
                                    }
                            }
                        } catch (Throwable th13) {
                            th = th13;
                        }
                        throw th;
                    }
                    try {
                        if (view instanceof RootViewSurfaceTaker) {
                            this.mInputQueueCallback = ((RootViewSurfaceTaker) view).willYouTakeTheInputQueue();
                        }
                        if (this.mInputChannel != null) {
                            if (this.mInputQueueCallback != null) {
                                this.mInputQueue = new InputQueue();
                                this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
                            }
                            this.mInputEventReceiver = new WindowInputEventReceiver(this.mInputChannel, Looper.myLooper());
                        }
                        view.assignParent(this);
                        this.mAddedTouchMode = (res & 1) != 0;
                        this.mAppVisible = (res & 2) != 0;
                        if (this.mAccessibilityManager.isEnabled()) {
                            this.mAccessibilityInteractionConnectionManager.ensureConnection();
                        }
                        if (view.getImportantForAccessibility() == 0) {
                            view.setImportantForAccessibility(1);
                        }
                        CharSequence counterSuffix = attrs2.getTitle();
                        this.mSyntheticInputStage = new SyntheticInputStage();
                        InputStage earlyPostImeStage = new EarlyPostImeInputStage(new NativePostImeInputStage(new ViewPostImeInputStage(this.mSyntheticInputStage), "aq:native-post-ime:" + ((Object) counterSuffix)));
                        this.mFirstInputStage = new NativePreImeInputStage(new ViewPreImeInputStage(new ImeInputStage(earlyPostImeStage, "aq:ime:" + ((Object) counterSuffix))), "aq:native-pre-ime:" + ((Object) counterSuffix));
                        this.mFirstPostImeInputStage = earlyPostImeStage;
                        this.mPendingInputEventQueueLengthCounterName = "aq:pending:" + ((Object) counterSuffix);
                    } catch (Throwable th14) {
                        th = th14;
                    }
                }
                this.hwForceDarkState = HwFrameworkFactory.getHwForceDarkManager().updateHwForceDarkState(this.mContext, getView(), this.mWindowAttributes);
            } catch (Throwable th15) {
                th = th15;
            }
        }
    }

    private void setTag() {
        String[] split = this.mWindowAttributes.getTitle().toString().split("\\.");
        if (split.length > 0) {
            this.mTag = "ViewRootImpl[" + split[split.length - 1] + "]";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInLocalFocusMode() {
        return (this.mWindowAttributes.flags & 268435456) != 0;
    }

    @UnsupportedAppUsage
    public int getWindowFlags() {
        return this.mWindowAttributes.flags;
    }

    public int getDisplayId() {
        return this.mDisplay.getDisplayId();
    }

    public CharSequence getTitle() {
        return this.mWindowAttributes.getTitle();
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    public void destroyHardwareResources() {
        ThreadedRenderer renderer = this.mAttachInfo.mThreadedRenderer;
        if (renderer == null) {
            return;
        }
        if (Looper.myLooper() != this.mAttachInfo.mHandler.getLooper()) {
            this.mAttachInfo.mHandler.postAtFrontOfQueue(new Runnable() {
                /* class android.view.$$Lambda$dj1hfDQd0iEp_uBDBPEUMMYJJwk */

                @Override // java.lang.Runnable
                public final void run() {
                    ViewRootImpl.this.destroyHardwareResources();
                }
            });
            return;
        }
        renderer.destroyHardwareResources(this.mView);
        renderer.destroy();
    }

    @UnsupportedAppUsage
    public void detachFunctor(long functor) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.stopDrawing();
        }
    }

    @UnsupportedAppUsage
    public static void invokeFunctor(long functor, boolean waitForCompletion) {
        ThreadedRenderer.invokeFunctor(functor, waitForCompletion);
    }

    public void registerAnimatingRenderNode(RenderNode animator) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerAnimatingRenderNode(animator);
            return;
        }
        if (this.mAttachInfo.mPendingAnimatingRenderNodes == null) {
            this.mAttachInfo.mPendingAnimatingRenderNodes = new ArrayList();
        }
        this.mAttachInfo.mPendingAnimatingRenderNodes.add(animator);
    }

    public void registerVectorDrawableAnimator(NativeVectorDrawableAnimator animator) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerVectorDrawableAnimator(animator);
        }
    }

    public void registerRtFrameCallback(HardwareRenderer.FrameDrawingCallback callback) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerRtFrameCallback(new HardwareRenderer.FrameDrawingCallback() {
                /* class android.view.$$Lambda$ViewRootImpl$IReiNMSbDakZSGbIZuL_ifaFWn8 */

                @Override // android.graphics.HardwareRenderer.FrameDrawingCallback
                public final void onFrameDraw(long j) {
                    ViewRootImpl.lambda$registerRtFrameCallback$0(HardwareRenderer.FrameDrawingCallback.this, j);
                }
            });
        }
    }

    static /* synthetic */ void lambda$registerRtFrameCallback$0(HardwareRenderer.FrameDrawingCallback callback, long frame) {
        try {
            callback.onFrameDraw(frame);
        } catch (Exception e) {
            Log.e(TAG, "Exception while executing onFrameDraw", e);
        }
    }

    @UnsupportedAppUsage
    private void enableHardwareAcceleration(WindowManager.LayoutParams attrs) {
        View.AttachInfo attachInfo = this.mAttachInfo;
        boolean wideGamut = false;
        attachInfo.mHardwareAccelerated = false;
        attachInfo.mHardwareAccelerationRequested = false;
        if (this.mTranslator == null) {
            boolean hardwareAccelerated = (attrs.flags & 16777216) != 0;
            if (DEBUG_HWFLOW && !hardwareAccelerated) {
                Log.d(this.mTag, "enableHardwareAcceleration false");
            }
            if (hardwareAccelerated) {
                if (ThreadedRenderer.isAvailable()) {
                    boolean fakeHwAccelerated = (attrs.privateFlags & 1) != 0;
                    boolean forceHwAccelerated = (attrs.privateFlags & 2) != 0;
                    if (fakeHwAccelerated) {
                        this.mAttachInfo.mHardwareAccelerationRequested = true;
                    } else if (!ThreadedRenderer.sRendererDisabled || (ThreadedRenderer.sSystemRendererDisabled && forceHwAccelerated)) {
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            this.mAttachInfo.mThreadedRenderer.destroy();
                        }
                        Rect insets = attrs.surfaceInsets;
                        boolean translucent = attrs.format != -1 || (insets.left != 0 || insets.right != 0 || insets.top != 0 || insets.bottom != 0);
                        if (this.mContext.getResources().getConfiguration().isScreenWideColorGamut() && attrs.getColorMode() == 1) {
                            wideGamut = true;
                        }
                        this.mAttachInfo.mThreadedRenderer = ThreadedRenderer.create(this.mContext, translucent, attrs.getTitle().toString());
                        this.mAttachInfo.mThreadedRenderer.setWideGamut(wideGamut);
                        updateForceDarkMode();
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            View.AttachInfo attachInfo2 = this.mAttachInfo;
                            attachInfo2.mHardwareAccelerationRequested = true;
                            attachInfo2.mHardwareAccelerated = true;
                        }
                    }
                } else {
                    return;
                }
            }
            if (this.mAttachInfo.mThreadedRenderer == null) {
                Jlog.d(378, "#ARG1:<" + attrs.getTitle().toString() + ">");
            }
        }
    }

    private int getNightMode() {
        return this.mContext.getResources().getConfiguration().uiMode & 48;
    }

    private void updateForceDarkMode() {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            boolean z = true;
            boolean useAutoDark = getNightMode() == 32;
            if (useAutoDark) {
                boolean forceDarkAllowedDefault = SystemProperties.getBoolean(ThreadedRenderer.DEBUG_FORCE_DARK, false);
                TypedArray a = this.mContext.obtainStyledAttributes(R.styleable.Theme);
                if (!a.getBoolean(279, true) || !a.getBoolean(278, forceDarkAllowedDefault)) {
                    z = false;
                }
                useAutoDark = z;
                a.recycle();
            }
            if (this.mAttachInfo.mThreadedRenderer.setForceDark(useAutoDark)) {
                invalidateWorld(this.mView);
            }
        }
    }

    @UnsupportedAppUsage
    public View getView() {
        return this.mView;
    }

    /* access modifiers changed from: package-private */
    public final WindowLeaked getLocation() {
        return this.mLocation;
    }

    @UnsupportedAppUsage
    public void setBlurMode(int blurMode) {
        synchronized (this) {
            if (this.mHwBlurWindowManager != null && this.mHwBlurWindowManager.setBlurMode(blurMode)) {
                scheduleTraversals();
            }
        }
    }

    @UnsupportedAppUsage
    public void setBlurEnabled(boolean enabled) {
        synchronized (this) {
            if (this.mHwBlurWindowManager != null && this.mHwBlurWindowManager.setBlurEnabled(enabled)) {
                scheduleTraversals();
            }
        }
    }

    @UnsupportedAppUsage
    public void setBlurCacheEnabled(boolean enabled) {
        synchronized (this) {
            if (this.mHwBlurWindowManager != null && this.mHwBlurWindowManager.setBlurCacheEnabled(enabled)) {
                scheduleTraversals();
            }
        }
    }

    @UnsupportedAppUsage
    public void setBlurProgress(float progress) {
        synchronized (this) {
            if (this.mHwBlurWindowManager != null && this.mHwBlurWindowManager.setBlurProgress(progress)) {
                scheduleTraversals();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setLayoutParams(WindowManager.LayoutParams attrs, boolean newView) {
        synchronized (this) {
            int oldInsetLeft = this.mWindowAttributes.surfaceInsets.left;
            int oldInsetTop = this.mWindowAttributes.surfaceInsets.top;
            int oldInsetRight = this.mWindowAttributes.surfaceInsets.right;
            int oldInsetBottom = this.mWindowAttributes.surfaceInsets.bottom;
            int oldSoftInputMode = this.mWindowAttributes.softInputMode;
            boolean oldHasManualSurfaceInsets = this.mWindowAttributes.hasManualSurfaceInsets;
            this.mClientWindowLayoutFlags = attrs.flags;
            int compatibleWindowFlag = this.mWindowAttributes.privateFlags & 128;
            attrs.systemUiVisibility = this.mWindowAttributes.systemUiVisibility;
            attrs.subtreeSystemUiVisibility = this.mWindowAttributes.subtreeSystemUiVisibility;
            this.mWindowAttributesChangesFlag = this.mWindowAttributes.copyFrom(attrs);
            if ((this.mWindowAttributesChangesFlag & 524288) != 0) {
                this.mAttachInfo.mRecomputeGlobalAttributes = true;
            }
            if ((this.mWindowAttributesChangesFlag & 1) != 0) {
                this.mAttachInfo.mNeedsUpdateLightCenter = true;
            }
            if (this.mWindowAttributes.packageName == null) {
                this.mWindowAttributes.packageName = this.mBasePackageName;
            }
            this.mWindowAttributes.privateFlags |= compatibleWindowFlag;
            if (this.mWindowAttributes.preservePreviousSurfaceInsets) {
                this.mWindowAttributes.surfaceInsets.set(oldInsetLeft, oldInsetTop, oldInsetRight, oldInsetBottom);
                this.mWindowAttributes.hasManualSurfaceInsets = oldHasManualSurfaceInsets;
            } else if (!(this.mWindowAttributes.surfaceInsets.left == oldInsetLeft && this.mWindowAttributes.surfaceInsets.top == oldInsetTop && this.mWindowAttributes.surfaceInsets.right == oldInsetRight && this.mWindowAttributes.surfaceInsets.bottom == oldInsetBottom)) {
                this.mNeedsRendererSetup = true;
            }
            applyKeepScreenOnFlag(this.mWindowAttributes);
            if (newView) {
                this.mSoftInputMode = attrs.softInputMode;
                requestLayout();
            }
            if ((attrs.softInputMode & 240) == 0) {
                this.mWindowAttributes.softInputMode = (this.mWindowAttributes.softInputMode & TrafficStats.TAG_SYSTEM_IMPERSONATION_RANGE_END) | (oldSoftInputMode & 240);
            }
            this.mWindowAttributesChanged = true;
            scheduleTraversals();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleAppVisibility(boolean visible) {
        if (this.mAppVisible != visible) {
            this.mAppVisible = visible;
            this.mAppVisibilityChanged = true;
            if (DEBUG_HWFLOW) {
                String str = this.mTag;
                Log.i(str, "handleAppVisibility: mAppVisible " + this.mAppVisible);
            }
            scheduleTraversals();
            if (!this.mAppVisible) {
                WindowManagerGlobal.trimForeground();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleGetNewSurface() {
        this.mNewSurfaceNeeded = true;
        this.mFullRedrawNeeded = true;
        scheduleTraversals();
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
        if (this.mDisplay.getDisplayId() != displayId) {
            updateInternalDisplay(displayId, this.mView.getResources());
            this.mAttachInfo.mDisplayState = this.mDisplay.getState();
            this.mView.dispatchMovedToDisplay(this.mDisplay, config);
        }
    }

    private void updateInternalDisplay(int displayId, Resources resources) {
        Display preferredDisplay = ResourcesManager.getInstance().getAdjustedDisplay(displayId, resources);
        if (preferredDisplay == null) {
            Slog.w(TAG, "Cannot get desired display with Id: " + displayId);
            this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(0, resources);
        } else {
            this.mDisplay = preferredDisplay;
        }
        this.mContext.updateDisplay(this.mDisplay.getDisplayId());
    }

    /* access modifiers changed from: package-private */
    public void pokeDrawLockIfNeeded() {
        int displayState = this.mAttachInfo.mDisplayState;
        if (this.mView != null && this.mAdded && this.mTraversalScheduled) {
            if (displayState == 3 || (!sSupportAod && displayState == 4)) {
                try {
                    this.mWindowSession.pokeDrawLock(this.mWindow);
                } catch (RemoteException e) {
                }
            }
        }
    }

    @Override // android.view.ViewParent
    public void requestFitSystemWindows() {
        checkThread();
        this.mApplyInsetsRequested = true;
        scheduleTraversals();
    }

    /* access modifiers changed from: package-private */
    public void notifyInsetsChanged() {
        if (sNewInsetsMode != 0) {
            this.mApplyInsetsRequested = true;
            if (!this.mIsInTraversal) {
                scheduleTraversals();
            }
        }
    }

    @Override // android.view.ViewParent
    public void requestLayout() {
        if (!this.mHandlingLayoutInLayoutRequest) {
            checkThread();
            this.mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    @Override // android.view.ViewParent
    public boolean isLayoutRequested() {
        return this.mLayoutRequested;
    }

    @Override // android.view.ViewParent
    public void onDescendantInvalidated(View child, View descendant) {
        if (!HwFrameworkFactory.getHwApsImpl().isDropEmptyFrame(descendant)) {
            if ((descendant.mPrivateFlags & 64) != 0) {
                this.mIsAnimating = true;
            }
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void invalidate() {
        this.mDirty.set(0, 0, this.mWidth, this.mHeight);
        if (!this.mWillDrawSoon) {
            scheduleTraversals();
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateWorld(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                invalidateWorld(parent.getChildAt(i));
            }
        }
    }

    @Override // android.view.ViewParent
    public void invalidateChild(View child, Rect dirty) {
        invalidateChildInParent(null, dirty);
    }

    @Override // android.view.ViewParent
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        checkThread();
        if (dirty == null) {
            invalidate();
            return null;
        } else if (dirty.isEmpty() && !this.mIsAnimating) {
            return null;
        } else {
            if (!(this.mCurScrollY == 0 && this.mTranslator == null)) {
                this.mTempRect.set(dirty);
                dirty = this.mTempRect;
                int i = this.mCurScrollY;
                if (i != 0) {
                    dirty.offset(0, -i);
                }
                CompatibilityInfo.Translator translator = this.mTranslator;
                if (translator != null) {
                    translator.translateRectInAppWindowToScreen(dirty);
                }
                if (this.mAttachInfo.mScalingRequired) {
                    dirty.inset(-1, -1);
                }
            }
            invalidateRectOnScreen(dirty);
            return null;
        }
    }

    private void invalidateRectOnScreen(Rect dirty) {
        Rect localDirty = this.mDirty;
        localDirty.union(dirty.left, dirty.top, dirty.right, dirty.bottom);
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean intersected = localDirty.intersect(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        if (!intersected) {
            localDirty.setEmpty();
        }
        if (this.mWillDrawSoon) {
            return;
        }
        if (intersected || this.mIsAnimating) {
            scheduleTraversals();
        }
    }

    public void setIsAmbientMode(boolean ambient) {
        this.mIsAmbientMode = ambient;
    }

    /* access modifiers changed from: package-private */
    public void addWindowStoppedCallback(WindowStoppedCallback c) {
        this.mWindowStoppedCallbacks.add(c);
    }

    /* access modifiers changed from: package-private */
    public int getWindowModeType() {
        View view = this.mView;
        if (view instanceof DecorView) {
            return ((DecorView) view).getWindowMode();
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void removeWindowStoppedCallback(WindowStoppedCallback c) {
        this.mWindowStoppedCallbacks.remove(c);
    }

    /* access modifiers changed from: package-private */
    public void setWindowStopped(boolean stopped) {
        if (this.mStopped != stopped) {
            this.mStopped = stopped;
            ThreadedRenderer renderer = this.mAttachInfo.mThreadedRenderer;
            if (renderer != null) {
                renderer.setStopped(this.mStopped);
            }
            if (!this.mStopped) {
                this.mNewSurfaceNeeded = true;
                scheduleTraversals();
            } else if (renderer != null) {
                renderer.destroyHardwareResources(this.mView);
            }
            for (int i = 0; i < this.mWindowStoppedCallbacks.size(); i++) {
                this.mWindowStoppedCallbacks.get(i).windowStopped(stopped);
            }
            if (this.mStopped) {
                if (this.mSurfaceHolder != null) {
                    notifySurfaceDestroyed();
                }
                destroySurface();
            }
        }
    }

    public void createBoundsSurface(int zOrderLayer, boolean isUpdate) {
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        if (this.mBoundsSurfaceControl == null || !this.mBoundsSurface.isValid() || isUpdate) {
            if (getWindowModeType() == 102) {
                SurfaceControl.Builder containerLayer = new SurfaceControl.Builder(this.mSurfaceSession).setContainerLayer();
                this.mBoundsSurfaceControl = containerLayer.setName("Bounds for - " + getTitle().toString()).setParent(this.mSurfaceControl).build();
            } else {
                SurfaceControl.Builder builder = new SurfaceControl.Builder(this.mSurfaceSession);
                this.mBoundsSurfaceControl = builder.setName("Bounds for - " + getTitle().toString()).setParent(this.mSurfaceControl).build();
            }
            setBoundsSurfaceCrop();
            this.mTransaction.setLayer(this.mBoundsSurfaceControl, zOrderLayer).show(this.mBoundsSurfaceControl).apply();
            this.mBoundsSurface.copyFrom(this.mBoundsSurfaceControl);
        }
    }

    private void setBoundsSurfaceCrop() {
        this.mTempBoundsRect.set(this.mWinFrame);
        this.mTempBoundsRect.offsetTo(this.mWindowAttributes.surfaceInsets.left, this.mWindowAttributes.surfaceInsets.top);
        this.mTransaction.setWindowCrop(this.mBoundsSurfaceControl, this.mTempBoundsRect);
    }

    private void updateBoundsSurface() {
        if (this.mBoundsSurfaceControl != null && this.mSurface.isValid()) {
            this.mTransaction.reparent(this.mBoundsSurfaceControl, this.mSurfaceControl);
            setBoundsSurfaceCrop();
            SurfaceControl.Transaction transaction = this.mTransaction;
            SurfaceControl surfaceControl = this.mBoundsSurfaceControl;
            Surface surface = this.mSurface;
            transaction.deferTransactionUntilSurface(surfaceControl, surface, surface.getNextFrameNumber()).apply();
        }
    }

    private void destroySurface() {
        this.mSurface.release();
        this.mSurfaceControl.release();
        this.mSurfaceSession = null;
        SurfaceControl surfaceControl = this.mBoundsSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
            this.mBoundsSurface.release();
            this.mBoundsSurfaceControl = null;
        }
    }

    public void setPausedForTransition(boolean paused) {
        this.mPausedForTransition = paused;
    }

    @Override // android.view.ViewParent
    public ViewParent getParent() {
        return null;
    }

    @Override // android.view.ViewParent
    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        if (child == this.mView) {
            return r.intersect(0, 0, this.mWidth, this.mHeight);
        }
        throw new RuntimeException("child is not mine, honest!");
    }

    @Override // android.view.ViewParent
    public void bringChildToFront(View child) {
    }

    /* access modifiers changed from: package-private */
    public int getHostVisibility() {
        if (this.mAppVisible || this.mForceDecorViewVisibility) {
            return this.mView.getVisibility();
        }
        return 8;
    }

    public void requestTransitionStart(LayoutTransition transition) {
        ArrayList<LayoutTransition> arrayList = this.mPendingTransitions;
        if (arrayList == null || !arrayList.contains(transition)) {
            if (this.mPendingTransitions == null) {
                this.mPendingTransitions = new ArrayList<>();
            }
            this.mPendingTransitions.add(transition);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyRendererOfFramePending() {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.notifyFramePending();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void scheduleTraversals() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
            this.mChoreographer.postCallback(3, this.mTraversalRunnable, null);
            if (!this.mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
            IHwAppInnerBoost iHwAppInnerBoost = this.mHwAppInnerBoost;
            if (iHwAppInnerBoost != null) {
                iHwAppInnerBoost.onTraversal();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unscheduleTraversals() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            this.mChoreographer.removeCallbacks(3, this.mTraversalRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void doTraversal() {
        if (this.mTraversalScheduled) {
            IHwRtgSchedImpl hwRtgSchedImpl = null;
            if (!(this.mAttachInfo.mThreadedRenderer == null || (hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl()) == null)) {
                hwRtgSchedImpl.beginDoTraversal();
            }
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            if (this.mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }
            performTraversals();
            if (this.mSurface.isValid()) {
                this.mSurface.syncDynamicBufSize(HwDynBufManager.getImpl().getTargetBufCount());
                HwDynBufManager.getImpl().updateSurfaceBufCount(this.mSurface.getBufferCount());
            }
            if (hwRtgSchedImpl != null) {
                hwRtgSchedImpl.endDoTraversal();
            }
            if (this.mProfile) {
                Debug.stopMethodTracing();
                this.mProfile = false;
            }
        }
    }

    private void applyKeepScreenOnFlag(WindowManager.LayoutParams params) {
        if (this.mAttachInfo.mKeepScreenOn) {
            params.flags |= 128;
        } else {
            params.flags = (params.flags & -129) | (this.mClientWindowLayoutFlags & 128);
        }
    }

    private boolean collectViewAttributes() {
        if (this.mAttachInfo.mRecomputeGlobalAttributes) {
            View.AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mRecomputeGlobalAttributes = false;
            boolean oldScreenOn = attachInfo.mKeepScreenOn;
            View.AttachInfo attachInfo2 = this.mAttachInfo;
            attachInfo2.mKeepScreenOn = false;
            attachInfo2.mSystemUiVisibility = 0;
            attachInfo2.mHasSystemUiListeners = false;
            this.mView.dispatchCollectViewAttributes(attachInfo2, 0);
            this.mAttachInfo.mSystemUiVisibility &= ~this.mAttachInfo.mDisabledSystemUiVisibility;
            WindowManager.LayoutParams params = this.mWindowAttributes;
            this.mAttachInfo.mSystemUiVisibility |= getImpliedSystemUiVisibility(params);
            if (!(this.mAttachInfo.mKeepScreenOn == oldScreenOn && this.mAttachInfo.mSystemUiVisibility == params.subtreeSystemUiVisibility && this.mAttachInfo.mHasSystemUiListeners == params.hasSystemUiListeners)) {
                applyKeepScreenOnFlag(params);
                params.subtreeSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                params.hasSystemUiListeners = this.mAttachInfo.mHasSystemUiListeners;
                this.mView.dispatchWindowSystemUiVisiblityChanged(this.mAttachInfo.mSystemUiVisibility);
                return true;
            }
        }
        return false;
    }

    private int getImpliedSystemUiVisibility(WindowManager.LayoutParams params) {
        int vis = 0;
        if ((params.flags & 67108864) != 0) {
            vis = 0 | 1280;
        }
        if ((params.flags & 134217728) != 0) {
            return vis | 768;
        }
        return vis;
    }

    private boolean measureHierarchy(View host, WindowManager.LayoutParams lp, Resources res, int desiredWindowWidth, int desiredWindowHeight) {
        boolean goodMeasure = false;
        if (lp.width == -2) {
            DisplayMetrics packageMetrics = res.getDisplayMetrics();
            res.getValue(R.dimen.config_prefDialogWidth, this.mTmpValue, true);
            int baseSize = 0;
            if (this.mTmpValue.type == 5) {
                baseSize = (int) this.mTmpValue.getDimension(packageMetrics);
            }
            if (baseSize != 0 && desiredWindowWidth > baseSize) {
                int childWidthMeasureSpec = getRootMeasureSpec(baseSize, lp.width);
                int childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                if ((host.getMeasuredWidthAndState() & 16777216) == 0) {
                    goodMeasure = true;
                } else {
                    performMeasure(getRootMeasureSpec((baseSize + desiredWindowWidth) / 2, lp.width), childHeightMeasureSpec);
                    if ((host.getMeasuredWidthAndState() & 16777216) == 0) {
                        goodMeasure = true;
                    }
                }
            }
        }
        if (goodMeasure) {
            return false;
        }
        performMeasure(getRootMeasureSpec(desiredWindowWidth, lp.width), getRootMeasureSpec(desiredWindowHeight, lp.height));
        if (this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void transformMatrixToGlobal(Matrix m) {
        m.preTranslate((float) this.mAttachInfo.mWindowLeft, (float) this.mAttachInfo.mWindowTop);
    }

    /* access modifiers changed from: package-private */
    public void transformMatrixToLocal(Matrix m) {
        m.postTranslate((float) (-this.mAttachInfo.mWindowLeft), (float) (-this.mAttachInfo.mWindowTop));
    }

    /* access modifiers changed from: package-private */
    public WindowInsets getWindowInsets(boolean forceConstruct) {
        DisplayCutout displayCutout;
        if (this.mLastWindowInsets == null || forceConstruct) {
            this.mDispatchContentInsets.set(this.mAttachInfo.mContentInsets);
            this.mDispatchStableInsets.set(this.mAttachInfo.mStableInsets);
            this.mDispatchDisplayCutout = this.mAttachInfo.mDisplayCutout.get();
            Rect contentInsets = this.mDispatchContentInsets;
            Rect stableInsets = this.mDispatchStableInsets;
            DisplayCutout displayCutout2 = this.mDispatchDisplayCutout;
            if (forceConstruct || (this.mPendingContentInsets.equals(contentInsets) && this.mPendingStableInsets.equals(stableInsets) && this.mPendingDisplayCutout.get().equals(displayCutout2))) {
                displayCutout = displayCutout2;
            } else {
                contentInsets = this.mPendingContentInsets;
                stableInsets = this.mPendingStableInsets;
                displayCutout = this.mPendingDisplayCutout.get();
            }
            Rect outsets = this.mAttachInfo.mOutsets;
            if (outsets.left > 0 || outsets.top > 0 || outsets.right > 0 || outsets.bottom > 0) {
                contentInsets = new Rect(contentInsets.left + outsets.left, contentInsets.top + outsets.top, contentInsets.right + outsets.right, contentInsets.bottom + outsets.bottom);
            }
            this.mLastWindowInsets = this.mInsetsController.calculateInsets(this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeSystemBars, displayCutout, ensureInsetsNonNegative(contentInsets, "content"), ensureInsetsNonNegative(stableInsets, "stable"), this.mWindowAttributes.softInputMode);
            if (!(this.mLastWindowInsets == null || displayCutout.getDisplaySideSafeInsets() == null)) {
                this.mLastWindowInsets.setDisplaySideRegionRect(displayCutout.getDisplaySideSafeInsets());
            }
        }
        return this.mLastWindowInsets;
    }

    private Rect ensureInsetsNonNegative(Rect insets, String kind) {
        if (insets.left >= 0 && insets.top >= 0 && insets.right >= 0 && insets.bottom >= 0) {
            return insets;
        }
        String str = this.mTag;
        Log.wtf(str, "Negative " + kind + "Insets: " + insets + ", mFirst=" + this.mFirst);
        return new Rect(Math.max(0, insets.left), Math.max(0, insets.top), Math.max(0, insets.right), Math.max(0, insets.bottom));
    }

    /* access modifiers changed from: package-private */
    public void dispatchApplyInsets(View host) {
        Trace.traceBegin(8, "dispatchApplyInsets");
        boolean dispatchCutout = true;
        WindowInsets insets = getWindowInsets(true);
        if (!(this.mWindowAttributes.layoutInDisplayCutoutMode == 1 || this.mWindowAttributes.layoutInDisplayCutoutMode == 3 || (this.mWindowAttributes.hwFlags & 65536) != 0)) {
            dispatchCutout = false;
        }
        if (!dispatchCutout) {
            insets = insets.consumeDisplayCutout();
        }
        if (!isUseSideRegion()) {
            insets.setDisplaySideRegionRect(new Rect());
        }
        DisplayCutout cutout = insets.getDisplayCutout();
        if (cutout != null) {
            if (!isUseWaterfall()) {
                cutout.setDisplayWaterfallInsets(new Rect());
            } else {
                cutout.updateSafeInsets(cutout.getWaterfallInsets().toRect());
            }
        }
        host.dispatchApplyWindowInsets(insets);
        if (!(!this.mWindowAttributes.getTitle().toString().contains("ChatroomInfoUI") || host.mListenerInfo == null || host.mListenerInfo.mOnApplyWindowInsetsListener == null)) {
            host.onApplyWindowInsets(insets);
        }
        Trace.traceEnd(8);
    }

    private boolean isUseSideRegion() {
        return this.mWindowAttributes.layoutInDisplaySideMode == 1;
    }

    private boolean isUseWaterfall() {
        return this.mWindowAttributes.layoutInDisplayCutoutMode == 3;
    }

    /* access modifiers changed from: package-private */
    public InsetsController getInsetsController() {
        return this.mInsetsController;
    }

    private static boolean shouldUseDisplaySize(WindowManager.LayoutParams lp) {
        return lp.type == 2014 || lp.type == 2011 || lp.type == 2020;
    }

    private int dipToPx(int dip) {
        return (int) ((this.mContext.getResources().getDisplayMetrics().density * ((float) dip)) + 0.5f);
    }

    /* JADX INFO: Multiple debug info for r9v38 'params'  android.view.WindowManager$LayoutParams: [D('params' android.view.WindowManager$LayoutParams), D('desiredWindowWidth' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x02a2  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x02b0  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x02b8  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x02d4  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x032f  */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x035c  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x0362  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0367 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:201:0x03a4  */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x03ba  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x03d1  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x03d3  */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x03e0 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x040c  */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x0419  */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x041f  */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x042a  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0438  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x046f  */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x047b A[SYNTHETIC, Splitter:B:255:0x047b] */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x04d6  */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x04e4  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x04fd  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x0519  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x051b  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x052a  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x052c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:301:0x053b  */
    /* JADX WARNING: Removed duplicated region for block: B:302:0x053d  */
    /* JADX WARNING: Removed duplicated region for block: B:305:0x054c  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x054e  */
    /* JADX WARNING: Removed duplicated region for block: B:309:0x055d  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x055f  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0574  */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x0576  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0583  */
    /* JADX WARNING: Removed duplicated region for block: B:318:0x0585  */
    /* JADX WARNING: Removed duplicated region for block: B:321:0x0594  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x059f  */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x05ad  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x05bb  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x05c9  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:340:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x0626  */
    /* JADX WARNING: Removed duplicated region for block: B:347:0x0628  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x062e A[SYNTHETIC, Splitter:B:350:0x062e] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x06ce  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:434:0x075d  */
    /* JADX WARNING: Removed duplicated region for block: B:435:0x075f  */
    /* JADX WARNING: Removed duplicated region for block: B:438:0x0764  */
    /* JADX WARNING: Removed duplicated region for block: B:439:0x0766  */
    /* JADX WARNING: Removed duplicated region for block: B:442:0x076b A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:447:0x0776  */
    /* JADX WARNING: Removed duplicated region for block: B:475:0x07ea A[Catch:{ RemoteException -> 0x0816 }] */
    /* JADX WARNING: Removed duplicated region for block: B:478:0x07f9 A[Catch:{ RemoteException -> 0x0816 }] */
    /* JADX WARNING: Removed duplicated region for block: B:497:0x0870  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0100  */
    /* JADX WARNING: Removed duplicated region for block: B:532:0x0938  */
    /* JADX WARNING: Removed duplicated region for block: B:551:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:552:0x097c  */
    /* JADX WARNING: Removed duplicated region for block: B:564:0x09c4  */
    /* JADX WARNING: Removed duplicated region for block: B:567:0x09db  */
    /* JADX WARNING: Removed duplicated region for block: B:569:0x09ee  */
    /* JADX WARNING: Removed duplicated region for block: B:573:0x09fc  */
    /* JADX WARNING: Removed duplicated region for block: B:575:0x0a01  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0117  */
    /* JADX WARNING: Removed duplicated region for block: B:583:0x0a0f  */
    /* JADX WARNING: Removed duplicated region for block: B:589:0x0a1c  */
    /* JADX WARNING: Removed duplicated region for block: B:606:0x0a9c  */
    /* JADX WARNING: Removed duplicated region for block: B:608:0x0aa6  */
    /* JADX WARNING: Removed duplicated region for block: B:610:0x0ab2  */
    /* JADX WARNING: Removed duplicated region for block: B:627:0x0b22  */
    /* JADX WARNING: Removed duplicated region for block: B:634:0x0b36 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:640:0x0b43  */
    /* JADX WARNING: Removed duplicated region for block: B:657:0x0b7a  */
    /* JADX WARNING: Removed duplicated region for block: B:664:0x0b89 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:668:0x0b90  */
    /* JADX WARNING: Removed duplicated region for block: B:673:0x0b99  */
    /* JADX WARNING: Removed duplicated region for block: B:674:0x0b9d A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:682:0x0bae  */
    /* JADX WARNING: Removed duplicated region for block: B:683:0x0bb0  */
    /* JADX WARNING: Removed duplicated region for block: B:688:0x0bbc  */
    /* JADX WARNING: Removed duplicated region for block: B:691:0x0bd0  */
    /* JADX WARNING: Removed duplicated region for block: B:704:0x0c2b  */
    /* JADX WARNING: Removed duplicated region for block: B:711:0x0c41  */
    /* JADX WARNING: Removed duplicated region for block: B:721:0x0c6b  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0164  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0187  */
    private void performTraversals() {
        boolean viewUserVisibilityChanged;
        boolean surfaceChanged;
        boolean supportsScreen;
        boolean z;
        WindowManager.LayoutParams params;
        int desiredWindowWidth;
        int desiredWindowHeight;
        boolean layoutRequested;
        long traversalTime;
        boolean insetsChanged;
        int desiredWindowHeight2;
        Rect frame;
        int i;
        int desiredWindowWidth2;
        WindowManager.LayoutParams params2;
        WindowManager.LayoutParams params3;
        boolean windowSizeMayChange;
        boolean computesInternalInsets;
        int relayoutResult;
        boolean updatedConfiguration;
        int surfaceGenerationId;
        boolean isViewVisible;
        boolean focusChangedDueToTouchMode;
        boolean insetsPending;
        int params4;
        boolean didLayout;
        boolean triggerGlobalLayoutListener;
        boolean hasWindowFocus;
        boolean regainedFocus;
        boolean cancelDraw;
        WindowManager.LayoutParams layoutParams;
        boolean isToast;
        Region touchableRegion;
        Rect visibleInsets;
        Rect visibleInsets2;
        int relayoutResult2;
        boolean insetsPending2;
        BaseSurfaceHolder baseSurfaceHolder;
        boolean updatedConfiguration2;
        boolean contentInsetsChanged;
        boolean hadSurface;
        boolean hwInitialized;
        boolean isPreviousTransparentRegionReseted;
        boolean surfaceSizeChanged;
        int relayoutResult3;
        boolean isPreviousTransparentRegionReseted2;
        boolean surfaceSizeChanged2;
        ThreadedRenderer threadedRenderer;
        int relayoutResult4;
        boolean measureAgain;
        boolean isPreviousTransparentRegionReseted3;
        boolean surfaceSizeChanged3;
        int fl;
        boolean overscanInsetsChanged;
        boolean visibleInsetsChanged;
        boolean stableInsetsChanged;
        boolean cutoutChanged;
        boolean alwaysConsumeSystemBarsChanged;
        long traversalTime2;
        boolean dragResizing;
        boolean dragResizing2;
        int i2;
        Surface.OutOfResourcesException e;
        int resizeMode;
        View host = this.mView;
        if (host != null && this.mAdded) {
            this.mHwRio.hookAttribute();
            this.mIsInTraversal = true;
            this.mWillDrawSoon = true;
            boolean windowSizeMayChange2 = false;
            WindowManager.LayoutParams lp = this.mWindowAttributes;
            long traversalTime3 = System.nanoTime();
            int viewVisibility = getHostVisibility();
            boolean viewVisibilityChanged = !this.mFirst && (this.mViewVisibility != viewVisibility || this.mNewSurfaceNeeded || this.mAppVisibilityChanged);
            this.mAppVisibilityChanged = false;
            if (!this.mFirst) {
                if ((this.mViewVisibility == 0) != (viewVisibility == 0)) {
                    viewUserVisibilityChanged = true;
                    WindowManager.LayoutParams params5 = null;
                    if (!this.mWindowAttributesChanged) {
                        this.mWindowAttributesChanged = false;
                        params5 = lp;
                        surfaceChanged = true;
                    } else {
                        surfaceChanged = false;
                    }
                    supportsScreen = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen();
                    z = this.mLastInCompatMode;
                    if (supportsScreen != z) {
                        this.mFullRedrawNeeded = true;
                        this.mLayoutRequested = true;
                        if (z) {
                            lp.privateFlags &= -129;
                            this.mLastInCompatMode = false;
                        } else {
                            lp.privateFlags |= 128;
                            this.mLastInCompatMode = true;
                        }
                        params = lp;
                    } else {
                        params = params5;
                    }
                    this.mWindowAttributesChangesFlag = 0;
                    Rect frame2 = this.mWinFrame;
                    if (!this.mFirst) {
                        this.mFullRedrawNeeded = true;
                        this.mLayoutRequested = true;
                        if (DEBUG_HWFLOW) {
                            Log.i(this.mTag, "first performTraversals");
                        }
                        Configuration config = this.mContext.getResources().getConfiguration();
                        if (shouldUseDisplaySize(lp)) {
                            Point size = new Point();
                            this.mDisplay.getRealSize(size);
                            desiredWindowWidth = size.x;
                            desiredWindowHeight = size.y;
                        } else {
                            desiredWindowWidth = this.mWinFrame.width();
                            desiredWindowHeight = this.mWinFrame.height();
                        }
                        View.AttachInfo attachInfo = this.mAttachInfo;
                        attachInfo.mUse32BitDrawingCache = true;
                        attachInfo.mWindowVisibility = viewVisibility;
                        attachInfo.mRecomputeGlobalAttributes = false;
                        this.mLastConfigurationFromResources.setTo(config);
                        this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                        if (this.mViewLayoutDirectionInitial == 2) {
                            host.setLayoutDirection(config.getLayoutDirection());
                        }
                        host.dispatchAttachedToWindow(this.mAttachInfo, 0);
                        this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
                        dispatchApplyInsets(host);
                    } else {
                        desiredWindowWidth = frame2.width();
                        desiredWindowHeight = frame2.height();
                        if (!(desiredWindowWidth == this.mWidth && desiredWindowHeight == this.mHeight)) {
                            this.mFullRedrawNeeded = true;
                            this.mLayoutRequested = true;
                            windowSizeMayChange2 = true;
                        }
                    }
                    if (viewVisibilityChanged) {
                        if (DEBUG_HWFLOW) {
                            String str = this.mTag;
                            StringBuilder sb = new StringBuilder();
                            sb.append("viewVisibility change to ");
                            sb.append(viewVisibility == 0 ? CalendarContract.CalendarColumns.VISIBLE : "invisible");
                            Log.i(str, sb.toString());
                        }
                        this.mAttachInfo.mWindowVisibility = viewVisibility;
                        host.dispatchWindowVisibilityChanged(viewVisibility);
                        if (viewUserVisibilityChanged) {
                            host.dispatchVisibilityAggregated(viewVisibility == 0);
                        }
                        if (viewVisibility != 0 || this.mNewSurfaceNeeded) {
                            endDragResizing();
                            destroyHardwareResources();
                        }
                        if (viewVisibility == 8) {
                            this.mHasHadWindowFocus = false;
                        }
                    }
                    if (this.mAttachInfo.mWindowVisibility != 0) {
                        host.clearAccessibilityFocus();
                    }
                    getRunQueue().executeActions(this.mAttachInfo.mHandler);
                    boolean insetsChanged2 = false;
                    layoutRequested = !this.mLayoutRequested && (!this.mStopped || this.mReportNextDraw);
                    if (!layoutRequested) {
                        Resources res = this.mView.getContext().getResources();
                        if (this.mFirst) {
                            View.AttachInfo attachInfo2 = this.mAttachInfo;
                            boolean z2 = this.mAddedTouchMode;
                            attachInfo2.mInTouchMode = !z2;
                            ensureTouchModeLocally(z2);
                            insetsChanged = false;
                            desiredWindowHeight2 = desiredWindowHeight;
                            desiredWindowWidth2 = desiredWindowWidth;
                        } else {
                            if (!this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                                insetsChanged2 = true;
                            }
                            if (!this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets)) {
                                insetsChanged2 = true;
                            }
                            if (!this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                                insetsChanged2 = true;
                            }
                            if (!this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout)) {
                                insetsChanged2 = true;
                            }
                            if (!this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                                this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                            }
                            if (!this.mPendingOutsets.equals(this.mAttachInfo.mOutsets)) {
                                insetsChanged2 = true;
                            }
                            if (this.mPendingAlwaysConsumeSystemBars != this.mAttachInfo.mAlwaysConsumeSystemBars) {
                                insetsChanged2 = true;
                            }
                            if (lp.width == -2 || lp.height == -2) {
                                windowSizeMayChange2 = true;
                                if (shouldUseDisplaySize(lp)) {
                                    Point size2 = new Point();
                                    this.mDisplay.getRealSize(size2);
                                    int desiredWindowWidth3 = size2.x;
                                    insetsChanged = insetsChanged2;
                                    desiredWindowHeight2 = size2.y;
                                    desiredWindowWidth2 = desiredWindowWidth3;
                                } else if (IS_SIDE_PROP) {
                                    DisplayInfo displayInfo = new DisplayInfo();
                                    getDisplayInfo(displayInfo);
                                    DisplayMetrics metrics = new DisplayMetrics();
                                    displayInfo.getAppMetrics(metrics);
                                    int desiredWindowWidth4 = metrics.widthPixels;
                                    insetsChanged = insetsChanged2;
                                    desiredWindowHeight2 = metrics.heightPixels;
                                    desiredWindowWidth2 = desiredWindowWidth4;
                                } else {
                                    Configuration config2 = res.getConfiguration();
                                    int desiredWindowWidth5 = dipToPx(config2.screenWidthDp);
                                    int desiredWindowHeight3 = dipToPx(config2.screenHeightDp);
                                    DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
                                    if (((float) Math.abs(desiredWindowWidth5 - dm.widthPixels)) <= dm.density) {
                                        desiredWindowWidth5 = dm.widthPixels;
                                    }
                                    if (((float) Math.abs(desiredWindowHeight3 - dm.heightPixels)) <= dm.density) {
                                        insetsChanged = insetsChanged2;
                                        desiredWindowHeight2 = dm.heightPixels;
                                        desiredWindowWidth2 = desiredWindowWidth5;
                                    } else {
                                        insetsChanged = insetsChanged2;
                                        desiredWindowHeight2 = desiredWindowHeight3;
                                        desiredWindowWidth2 = desiredWindowWidth5;
                                    }
                                }
                            } else {
                                insetsChanged = insetsChanged2;
                                desiredWindowHeight2 = desiredWindowHeight;
                                desiredWindowWidth2 = desiredWindowWidth;
                            }
                        }
                        traversalTime = traversalTime3;
                        i = -2;
                        frame = frame2;
                        windowSizeMayChange2 |= measureHierarchy(host, lp, res, desiredWindowWidth2, desiredWindowHeight2);
                    } else {
                        traversalTime = traversalTime3;
                        i = -2;
                        frame = frame2;
                        insetsChanged = false;
                        desiredWindowHeight2 = desiredWindowHeight;
                        desiredWindowWidth2 = desiredWindowWidth;
                    }
                    if (collectViewAttributes()) {
                        params = lp;
                    }
                    if (this.mAttachInfo.mForceReportNewAttributes) {
                        this.mAttachInfo.mForceReportNewAttributes = false;
                        params = lp;
                    }
                    if (this.mFirst || this.mAttachInfo.mViewVisibilityChanged) {
                        View.AttachInfo attachInfo3 = this.mAttachInfo;
                        attachInfo3.mViewVisibilityChanged = false;
                        resizeMode = this.mSoftInputMode & 240;
                        if (resizeMode == 0) {
                            int N = attachInfo3.mScrollContainers.size();
                            for (int i3 = 0; i3 < N; i3++) {
                                if (this.mAttachInfo.mScrollContainers.get(i3).isShown()) {
                                    resizeMode = 16;
                                }
                            }
                            if (resizeMode == 0) {
                                resizeMode = 32;
                            }
                            if ((lp.softInputMode & 240) != resizeMode) {
                                lp.softInputMode = (lp.softInputMode & TrafficStats.TAG_SYSTEM_IMPERSONATION_RANGE_END) | resizeMode;
                                params2 = lp;
                                if (params2 != null) {
                                    if ((host.mPrivateFlags & 512) != 0 && !PixelFormat.formatHasAlpha(params2.format)) {
                                        params2.format = -3;
                                    }
                                    this.mAttachInfo.mOverscanRequested = (params2.flags & 33554432) != 0;
                                }
                                if (this.mApplyInsetsRequested) {
                                    this.mApplyInsetsRequested = false;
                                    this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                                    dispatchApplyInsets(host);
                                    if (this.mLayoutRequested) {
                                        params3 = params2;
                                        windowSizeMayChange = windowSizeMayChange2 | measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth2, desiredWindowHeight2);
                                        if (layoutRequested) {
                                            this.mLayoutRequested = false;
                                        }
                                        boolean windowShouldResize = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowWidth2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowHeight2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                                        computesInternalInsets = !this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners() || this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
                                        relayoutResult = 0;
                                        updatedConfiguration = false;
                                        surfaceGenerationId = this.mSurface.getGenerationId();
                                        isViewVisible = viewVisibility != 0;
                                        boolean windowRelayoutWasForced = this.mForceNextWindowRelayout;
                                        focusChangedDueToTouchMode = false;
                                        boolean isPreviousTransparentRegionReseted4 = false;
                                        if (!this.mFirst || windowShouldResize || insetsChanged || viewVisibilityChanged) {
                                            params4 = params3;
                                        } else {
                                            params4 = params3;
                                            if (params4 == null && !this.mForceNextWindowRelayout) {
                                                maybeHandleWindowMove(frame);
                                                insetsPending = false;
                                                params4 = surfaceGenerationId;
                                                if (focusChangedDueToTouchMode) {
                                                    updateBoundsSurface();
                                                }
                                                didLayout = !layoutRequested && (!this.mStopped || this.mReportNextDraw);
                                                triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                                if (didLayout) {
                                                    performLayout(lp, this.mWidth, this.mHeight);
                                                    if ((host.mPrivateFlags & 512) != 0) {
                                                        host.getLocationInWindow(this.mTmpLocation);
                                                        Region region = this.mTransparentRegion;
                                                        int[] iArr = this.mTmpLocation;
                                                        region.set(iArr[0], iArr[1], (iArr[0] + host.mRight) - host.mLeft, (this.mTmpLocation[1] + host.mBottom) - host.mTop);
                                                        host.gatherTransparentRegion(this.mTransparentRegion);
                                                        CompatibilityInfo.Translator translator = this.mTranslator;
                                                        if (translator != null) {
                                                            translator.translateRegionInWindowToScreen(this.mTransparentRegion);
                                                        }
                                                        if (!this.mTransparentRegion.equals(this.mPreviousTransparentRegion) || (IS_FOLD_DISP && isPreviousTransparentRegionReseted4 && this.mTransparentRegion.isEmpty())) {
                                                            this.mPreviousTransparentRegion.set(this.mTransparentRegion);
                                                            this.mFullRedrawNeeded = true;
                                                            try {
                                                                this.mWindowSession.setTransparentRegion(this.mWindow, this.mTransparentRegion);
                                                            } catch (RemoteException e2) {
                                                            }
                                                        }
                                                    }
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                    View.AttachInfo attachInfo4 = this.mAttachInfo;
                                                    attachInfo4.mRecomputeGlobalAttributes = false;
                                                    attachInfo4.mTreeObserver.dispatchOnGlobalLayout();
                                                }
                                                if (computesInternalInsets) {
                                                    ViewTreeObserver.InternalInsetsInfo insets = this.mAttachInfo.mGivenInternalInsets;
                                                    insets.reset();
                                                    this.mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
                                                    this.mAttachInfo.mHasNonEmptyGivenInternalInsets = !insets.isEmpty();
                                                    if (insetsPending || !this.mLastGivenInsets.equals(insets)) {
                                                        this.mLastGivenInsets.set(insets);
                                                        CompatibilityInfo.Translator translator2 = this.mTranslator;
                                                        if (translator2 != null) {
                                                            Rect contentInsets = translator2.getTranslatedContentInsets(insets.contentInsets);
                                                            Rect visibleInsets3 = this.mTranslator.getTranslatedVisibleInsets(insets.visibleInsets);
                                                            touchableRegion = this.mTranslator.getTranslatedTouchableArea(insets.touchableRegion);
                                                            visibleInsets = visibleInsets3;
                                                            visibleInsets2 = contentInsets;
                                                        } else {
                                                            Rect contentInsets2 = insets.contentInsets;
                                                            Rect visibleInsets4 = insets.visibleInsets;
                                                            touchableRegion = insets.touchableRegion;
                                                            visibleInsets = visibleInsets4;
                                                            visibleInsets2 = contentInsets2;
                                                        }
                                                        try {
                                                            try {
                                                                this.mWindowSession.setInsets(this.mWindow, insets.mTouchableInsets, visibleInsets2, visibleInsets, touchableRegion);
                                                            } catch (RemoteException e3) {
                                                            }
                                                        } catch (RemoteException e4) {
                                                        }
                                                    }
                                                }
                                                if (this.mHwBlurWindowManager != null && this.mSurfaceControl.isValid()) {
                                                    this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                }
                                                if (this.mFirst) {
                                                    if (sAlwaysAssignFocus || !isInTouchMode()) {
                                                        View focused = this.mView;
                                                        if (focused != null && !focused.hasFocus()) {
                                                            this.mView.restoreDefaultFocus();
                                                        }
                                                    } else {
                                                        View view = this.mView;
                                                        if (view != null) {
                                                            View focused2 = view.findFocus();
                                                            if ((focused2 instanceof ViewGroup) && ((ViewGroup) focused2).getDescendantFocusability() == 262144) {
                                                                focused2.restoreDefaultFocus();
                                                            }
                                                        }
                                                    }
                                                }
                                                boolean changedVisibility = (!viewVisibilityChanged || this.mFirst) && isViewVisible;
                                                hasWindowFocus = !this.mAttachInfo.mHasWindowFocus && isViewVisible;
                                                regainedFocus = !hasWindowFocus && this.mLostWindowFocus;
                                                if (regainedFocus) {
                                                    this.mLostWindowFocus = false;
                                                } else if (!hasWindowFocus && this.mHadWindowFocus) {
                                                    this.mLostWindowFocus = true;
                                                }
                                                if (changedVisibility || regainedFocus) {
                                                    layoutParams = this.mWindowAttributes;
                                                    if (layoutParams == null) {
                                                        isToast = false;
                                                    } else {
                                                        isToast = layoutParams.type == 2005;
                                                    }
                                                    if (!isToast) {
                                                        host.sendAccessibilityEvent(32);
                                                    }
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus || isInLocalFocusMode()) {
                                                    cancelDraw = true;
                                                } else {
                                                    boolean imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                    if (imTarget != this.mLastWasImTarget) {
                                                        this.mLastWasImTarget = imTarget;
                                                        InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService(InputMethodManager.class);
                                                        if (imm == null || !imTarget) {
                                                            cancelDraw = true;
                                                        } else {
                                                            imm.onPreWindowFocus(this.mView, hasWindowFocus);
                                                            View view2 = this.mView;
                                                            cancelDraw = true;
                                                            imm.onPostWindowFocus(view2, view2.findFocus(), this.mWindowAttributes.softInputMode, !this.mHasHadWindowFocus, this.mWindowAttributes.flags);
                                                        }
                                                    } else {
                                                        cancelDraw = true;
                                                    }
                                                }
                                                if ((relayoutResult & 2) != 0) {
                                                    reportNextDraw();
                                                }
                                                if (!this.mAttachInfo.mTreeObserver.dispatchOnPreDraw() && isViewVisible) {
                                                    cancelDraw = false;
                                                }
                                                if (!cancelDraw) {
                                                    ArrayList<LayoutTransition> arrayList = this.mPendingTransitions;
                                                    if (arrayList != null && arrayList.size() > 0) {
                                                        for (int i4 = 0; i4 < this.mPendingTransitions.size(); i4++) {
                                                            this.mPendingTransitions.get(i4).startChangingAnimations();
                                                        }
                                                        this.mPendingTransitions.clear();
                                                    }
                                                    performDraw();
                                                } else if (isViewVisible) {
                                                    if (DEBUG_HWFLOW) {
                                                        Log.i(this.mTag, "performTraversals: cancelDraw " + cancelDraw);
                                                    }
                                                    scheduleTraversals();
                                                } else {
                                                    ArrayList<LayoutTransition> arrayList2 = this.mPendingTransitions;
                                                    if (arrayList2 != null && arrayList2.size() > 0) {
                                                        for (int i5 = 0; i5 < this.mPendingTransitions.size(); i5++) {
                                                            this.mPendingTransitions.get(i5).endChangingAnimations();
                                                        }
                                                        this.mPendingTransitions.clear();
                                                    }
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        }
                                        relayoutResult2 = 0;
                                        this.mForceNextWindowRelayout = false;
                                        if (!isViewVisible) {
                                            insetsPending2 = computesInternalInsets && (this.mFirst || viewVisibilityChanged);
                                        } else {
                                            insetsPending2 = false;
                                        }
                                        baseSurfaceHolder = this.mSurfaceHolder;
                                        if (baseSurfaceHolder == null) {
                                            baseSurfaceHolder.mSurfaceLock.lock();
                                            updatedConfiguration2 = false;
                                            this.mDrawingAllowed = true;
                                        } else {
                                            updatedConfiguration2 = false;
                                        }
                                        contentInsetsChanged = false;
                                        hadSurface = this.mSurface.isValid();
                                        if (params4 == null) {
                                            try {
                                                int fl2 = params4.flags;
                                                if (this.mAttachInfo.mKeepScreenOn) {
                                                    params4.flags |= 128;
                                                }
                                                params4.subtreeSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                                                params4.hasSystemUiListeners = this.mAttachInfo.mHasSystemUiListeners;
                                                fl = fl2;
                                            } catch (RemoteException e5) {
                                                hwInitialized = false;
                                                surfaceSizeChanged3 = false;
                                                isPreviousTransparentRegionReseted3 = false;
                                                insetsPending = insetsPending2;
                                                params4 = surfaceGenerationId;
                                                relayoutResult3 = relayoutResult2;
                                                updatedConfiguration = updatedConfiguration2;
                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                this.mAttachInfo.mWindowTop = frame.top;
                                                this.mWidth = frame.width();
                                                this.mHeight = frame.height();
                                                if (this.mSurfaceHolder != null) {
                                                }
                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                int childWidthMeasureSpec = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                                                int width = host.getMeasuredWidth();
                                                int height = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        } else {
                                            fl = 0;
                                        }
                                        if (this.mAttachInfo.mThreadedRenderer == null) {
                                            try {
                                                if (this.mAttachInfo.mThreadedRenderer.pause()) {
                                                    hwInitialized = false;
                                                    try {
                                                        surfaceSizeChanged3 = false;
                                                        try {
                                                            isPreviousTransparentRegionReseted3 = false;
                                                            try {
                                                                this.mDirty.set(0, 0, this.mWidth, this.mHeight);
                                                            } catch (RemoteException e6) {
                                                                insetsPending = insetsPending2;
                                                                params4 = surfaceGenerationId;
                                                                relayoutResult3 = relayoutResult2;
                                                                updatedConfiguration = updatedConfiguration2;
                                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                                this.mAttachInfo.mWindowTop = frame.top;
                                                                this.mWidth = frame.width();
                                                                this.mHeight = frame.height();
                                                                if (this.mSurfaceHolder != null) {
                                                                }
                                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                                this.mNeedsRendererSetup = false;
                                                                int childWidthMeasureSpec2 = getRootMeasureSpec(this.mWidth, lp.width);
                                                                int childHeightMeasureSpec2 = getRootMeasureSpec(this.mHeight, lp.height);
                                                                performMeasure(childWidthMeasureSpec2, childHeightMeasureSpec2);
                                                                int width2 = host.getMeasuredWidth();
                                                                int height2 = host.getMeasuredHeight();
                                                                measureAgain = false;
                                                                relayoutResult4 = relayoutResult3;
                                                                if (lp.horizontalWeight > 0.0f) {
                                                                }
                                                                if (lp.verticalWeight > 0.0f) {
                                                                }
                                                                if (measureAgain) {
                                                                }
                                                                layoutRequested = true;
                                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                                relayoutResult = relayoutResult4;
                                                                if (focusChangedDueToTouchMode) {
                                                                }
                                                                if (!layoutRequested) {
                                                                }
                                                                if (!didLayout) {
                                                                }
                                                                if (didLayout) {
                                                                }
                                                                if (triggerGlobalLayoutListener) {
                                                                }
                                                                if (computesInternalInsets) {
                                                                }
                                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                                if (this.mFirst) {
                                                                }
                                                                if (!viewVisibilityChanged) {
                                                                }
                                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                                }
                                                                if (!hasWindowFocus) {
                                                                }
                                                                if (regainedFocus) {
                                                                }
                                                                layoutParams = this.mWindowAttributes;
                                                                if (layoutParams == null) {
                                                                }
                                                                if (!isToast) {
                                                                }
                                                                this.mFirst = false;
                                                                this.mWillDrawSoon = false;
                                                                this.mNewSurfaceNeeded = false;
                                                                this.mActivityRelaunched = false;
                                                                this.mViewVisibility = viewVisibility;
                                                                this.mHadWindowFocus = hasWindowFocus;
                                                                if (hasWindowFocus) {
                                                                }
                                                                cancelDraw = true;
                                                                if ((relayoutResult & 2) != 0) {
                                                                }
                                                                cancelDraw = false;
                                                                if (!cancelDraw) {
                                                                }
                                                                this.mIsInTraversal = false;
                                                            }
                                                        } catch (RemoteException e7) {
                                                            isPreviousTransparentRegionReseted3 = false;
                                                            insetsPending = insetsPending2;
                                                            params4 = surfaceGenerationId;
                                                            relayoutResult3 = relayoutResult2;
                                                            updatedConfiguration = updatedConfiguration2;
                                                            surfaceSizeChanged = surfaceSizeChanged3;
                                                            isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                            this.mAttachInfo.mWindowLeft = frame.left;
                                                            this.mAttachInfo.mWindowTop = frame.top;
                                                            this.mWidth = frame.width();
                                                            this.mHeight = frame.height();
                                                            if (this.mSurfaceHolder != null) {
                                                            }
                                                            threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                            threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                            this.mNeedsRendererSetup = false;
                                                            int childWidthMeasureSpec22 = getRootMeasureSpec(this.mWidth, lp.width);
                                                            int childHeightMeasureSpec22 = getRootMeasureSpec(this.mHeight, lp.height);
                                                            performMeasure(childWidthMeasureSpec22, childHeightMeasureSpec22);
                                                            int width22 = host.getMeasuredWidth();
                                                            int height22 = host.getMeasuredHeight();
                                                            measureAgain = false;
                                                            relayoutResult4 = relayoutResult3;
                                                            if (lp.horizontalWeight > 0.0f) {
                                                            }
                                                            if (lp.verticalWeight > 0.0f) {
                                                            }
                                                            if (measureAgain) {
                                                            }
                                                            layoutRequested = true;
                                                            focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                            isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                            relayoutResult = relayoutResult4;
                                                            if (focusChangedDueToTouchMode) {
                                                            }
                                                            if (!layoutRequested) {
                                                            }
                                                            if (!didLayout) {
                                                            }
                                                            if (didLayout) {
                                                            }
                                                            if (triggerGlobalLayoutListener) {
                                                            }
                                                            if (computesInternalInsets) {
                                                            }
                                                            this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                            if (this.mFirst) {
                                                            }
                                                            if (!viewVisibilityChanged) {
                                                            }
                                                            if (!this.mAttachInfo.mHasWindowFocus) {
                                                            }
                                                            if (!hasWindowFocus) {
                                                            }
                                                            if (regainedFocus) {
                                                            }
                                                            layoutParams = this.mWindowAttributes;
                                                            if (layoutParams == null) {
                                                            }
                                                            if (!isToast) {
                                                            }
                                                            this.mFirst = false;
                                                            this.mWillDrawSoon = false;
                                                            this.mNewSurfaceNeeded = false;
                                                            this.mActivityRelaunched = false;
                                                            this.mViewVisibility = viewVisibility;
                                                            this.mHadWindowFocus = hasWindowFocus;
                                                            if (hasWindowFocus) {
                                                            }
                                                            cancelDraw = true;
                                                            if ((relayoutResult & 2) != 0) {
                                                            }
                                                            cancelDraw = false;
                                                            if (!cancelDraw) {
                                                            }
                                                            this.mIsInTraversal = false;
                                                        }
                                                    } catch (RemoteException e8) {
                                                        surfaceSizeChanged3 = false;
                                                        isPreviousTransparentRegionReseted3 = false;
                                                        insetsPending = insetsPending2;
                                                        params4 = surfaceGenerationId;
                                                        relayoutResult3 = relayoutResult2;
                                                        updatedConfiguration = updatedConfiguration2;
                                                        surfaceSizeChanged = surfaceSizeChanged3;
                                                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                        this.mAttachInfo.mWindowLeft = frame.left;
                                                        this.mAttachInfo.mWindowTop = frame.top;
                                                        this.mWidth = frame.width();
                                                        this.mHeight = frame.height();
                                                        if (this.mSurfaceHolder != null) {
                                                        }
                                                        threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                        threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                        this.mNeedsRendererSetup = false;
                                                        int childWidthMeasureSpec222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                        int childHeightMeasureSpec222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                        performMeasure(childWidthMeasureSpec222, childHeightMeasureSpec222);
                                                        int width222 = host.getMeasuredWidth();
                                                        int height222 = host.getMeasuredHeight();
                                                        measureAgain = false;
                                                        relayoutResult4 = relayoutResult3;
                                                        if (lp.horizontalWeight > 0.0f) {
                                                        }
                                                        if (lp.verticalWeight > 0.0f) {
                                                        }
                                                        if (measureAgain) {
                                                        }
                                                        layoutRequested = true;
                                                        focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                        isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                        relayoutResult = relayoutResult4;
                                                        if (focusChangedDueToTouchMode) {
                                                        }
                                                        if (!layoutRequested) {
                                                        }
                                                        if (!didLayout) {
                                                        }
                                                        if (didLayout) {
                                                        }
                                                        if (triggerGlobalLayoutListener) {
                                                        }
                                                        if (computesInternalInsets) {
                                                        }
                                                        this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                        if (this.mFirst) {
                                                        }
                                                        if (!viewVisibilityChanged) {
                                                        }
                                                        if (!this.mAttachInfo.mHasWindowFocus) {
                                                        }
                                                        if (!hasWindowFocus) {
                                                        }
                                                        if (regainedFocus) {
                                                        }
                                                        layoutParams = this.mWindowAttributes;
                                                        if (layoutParams == null) {
                                                        }
                                                        if (!isToast) {
                                                        }
                                                        this.mFirst = false;
                                                        this.mWillDrawSoon = false;
                                                        this.mNewSurfaceNeeded = false;
                                                        this.mActivityRelaunched = false;
                                                        this.mViewVisibility = viewVisibility;
                                                        this.mHadWindowFocus = hasWindowFocus;
                                                        if (hasWindowFocus) {
                                                        }
                                                        cancelDraw = true;
                                                        if ((relayoutResult & 2) != 0) {
                                                        }
                                                        cancelDraw = false;
                                                        if (!cancelDraw) {
                                                        }
                                                        this.mIsInTraversal = false;
                                                    }
                                                } else {
                                                    hwInitialized = false;
                                                    surfaceSizeChanged3 = false;
                                                    isPreviousTransparentRegionReseted3 = false;
                                                }
                                                this.mChoreographer.mFrameInfo.addFlags(1);
                                            } catch (RemoteException e9) {
                                                hwInitialized = false;
                                                surfaceSizeChanged3 = false;
                                                isPreviousTransparentRegionReseted3 = false;
                                                insetsPending = insetsPending2;
                                                params4 = surfaceGenerationId;
                                                relayoutResult3 = relayoutResult2;
                                                updatedConfiguration = updatedConfiguration2;
                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                this.mAttachInfo.mWindowTop = frame.top;
                                                this.mWidth = frame.width();
                                                this.mHeight = frame.height();
                                                if (this.mSurfaceHolder != null) {
                                                }
                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                int childWidthMeasureSpec2222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec2222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec2222, childHeightMeasureSpec2222);
                                                int width2222 = host.getMeasuredWidth();
                                                int height2222 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        } else {
                                            hwInitialized = false;
                                            surfaceSizeChanged3 = false;
                                            isPreviousTransparentRegionReseted3 = false;
                                        }
                                        relayoutResult2 = relayoutWindow(params4, viewVisibility, insetsPending2);
                                        if (params4 != null) {
                                            params4.flags = fl;
                                        }
                                        if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                                            performConfigurationChange(this.mPendingMergedConfiguration, !this.mFirst, -1);
                                            updatedConfiguration2 = true;
                                        }
                                        overscanInsetsChanged = this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets);
                                        contentInsetsChanged = this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                                        visibleInsetsChanged = this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets);
                                        stableInsetsChanged = this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets);
                                        cutoutChanged = this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout);
                                        boolean outsetsChanged = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                                        surfaceSizeChanged3 = (relayoutResult2 & 32) == 0;
                                        surfaceChanged |= surfaceSizeChanged3;
                                        alwaysConsumeSystemBarsChanged = this.mPendingAlwaysConsumeSystemBars == this.mAttachInfo.mAlwaysConsumeSystemBars;
                                        boolean colorModeChanged = hasColorModeChanged(lp.getColorMode());
                                        if (contentInsetsChanged) {
                                            this.mAttachInfo.mContentInsets.set(this.mPendingContentInsets);
                                        }
                                        if (overscanInsetsChanged) {
                                            this.mAttachInfo.mOverscanInsets.set(this.mPendingOverscanInsets);
                                            contentInsetsChanged = true;
                                        }
                                        if (stableInsetsChanged) {
                                            this.mAttachInfo.mStableInsets.set(this.mPendingStableInsets);
                                            contentInsetsChanged = true;
                                        }
                                        if (cutoutChanged) {
                                            this.mAttachInfo.mDisplayCutout.set(this.mPendingDisplayCutout);
                                            contentInsetsChanged = true;
                                        }
                                        if (alwaysConsumeSystemBarsChanged) {
                                            this.mAttachInfo.mAlwaysConsumeSystemBars = this.mPendingAlwaysConsumeSystemBars;
                                            contentInsetsChanged = true;
                                        }
                                        if (contentInsetsChanged || this.mLastSystemUiVisibility != this.mAttachInfo.mSystemUiVisibility || this.mApplyInsetsRequested || this.mLastOverscanRequested != this.mAttachInfo.mOverscanRequested || outsetsChanged) {
                                            this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                                            this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                                            this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                                            this.mApplyInsetsRequested = false;
                                            dispatchApplyInsets(host);
                                            contentInsetsChanged = true;
                                        }
                                        if (visibleInsetsChanged) {
                                            this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                                        }
                                        if (colorModeChanged && this.mAttachInfo.mThreadedRenderer != null) {
                                            this.mAttachInfo.mThreadedRenderer.setWideGamut(lp.getColorMode() != 1);
                                        }
                                        if (hadSurface) {
                                            try {
                                                if (this.mSurface.isValid()) {
                                                    this.mFullRedrawNeeded = true;
                                                    this.mPreviousTransparentRegion.setEmpty();
                                                    try {
                                                        traversalTime2 = traversalTime;
                                                    } catch (RemoteException e10) {
                                                        insetsPending = insetsPending2;
                                                        isPreviousTransparentRegionReseted3 = true;
                                                        params4 = surfaceGenerationId;
                                                        relayoutResult3 = relayoutResult2;
                                                        updatedConfiguration = updatedConfiguration2;
                                                        surfaceSizeChanged = surfaceSizeChanged3;
                                                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                        this.mAttachInfo.mWindowLeft = frame.left;
                                                        this.mAttachInfo.mWindowTop = frame.top;
                                                        this.mWidth = frame.width();
                                                        this.mHeight = frame.height();
                                                        if (this.mSurfaceHolder != null) {
                                                        }
                                                        threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                        threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                        this.mNeedsRendererSetup = false;
                                                        int childWidthMeasureSpec22222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                        int childHeightMeasureSpec22222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                        performMeasure(childWidthMeasureSpec22222, childHeightMeasureSpec22222);
                                                        int width22222 = host.getMeasuredWidth();
                                                        int height22222 = host.getMeasuredHeight();
                                                        measureAgain = false;
                                                        relayoutResult4 = relayoutResult3;
                                                        if (lp.horizontalWeight > 0.0f) {
                                                        }
                                                        if (lp.verticalWeight > 0.0f) {
                                                        }
                                                        if (measureAgain) {
                                                        }
                                                        layoutRequested = true;
                                                        focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                        isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                        relayoutResult = relayoutResult4;
                                                        if (focusChangedDueToTouchMode) {
                                                        }
                                                        if (!layoutRequested) {
                                                        }
                                                        if (!didLayout) {
                                                        }
                                                        if (didLayout) {
                                                        }
                                                        if (triggerGlobalLayoutListener) {
                                                        }
                                                        if (computesInternalInsets) {
                                                        }
                                                        this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                        if (this.mFirst) {
                                                        }
                                                        if (!viewVisibilityChanged) {
                                                        }
                                                        if (!this.mAttachInfo.mHasWindowFocus) {
                                                        }
                                                        if (!hasWindowFocus) {
                                                        }
                                                        if (regainedFocus) {
                                                        }
                                                        layoutParams = this.mWindowAttributes;
                                                        if (layoutParams == null) {
                                                        }
                                                        if (!isToast) {
                                                        }
                                                        this.mFirst = false;
                                                        this.mWillDrawSoon = false;
                                                        this.mNewSurfaceNeeded = false;
                                                        this.mActivityRelaunched = false;
                                                        this.mViewVisibility = viewVisibility;
                                                        this.mHadWindowFocus = hasWindowFocus;
                                                        if (hasWindowFocus) {
                                                        }
                                                        cancelDraw = true;
                                                        if ((relayoutResult & 2) != 0) {
                                                        }
                                                        cancelDraw = false;
                                                        if (!cancelDraw) {
                                                        }
                                                        this.mIsInTraversal = false;
                                                    }
                                                    try {
                                                        this.mHwViewRootImpl.setRealFrameTime(traversalTime2);
                                                        if (this.mAttachInfo.mThreadedRenderer != null) {
                                                            try {
                                                                insetsPending = insetsPending2;
                                                                try {
                                                                    boolean hwInitialized2 = this.mAttachInfo.mThreadedRenderer.initialize(this.mSurface);
                                                                    if (hwInitialized2) {
                                                                        try {
                                                                            if ((host.mPrivateFlags & 512) == 0) {
                                                                                this.mAttachInfo.mThreadedRenderer.allocateBuffers();
                                                                            }
                                                                        } catch (Surface.OutOfResourcesException e11) {
                                                                            e = e11;
                                                                            hwInitialized = hwInitialized2;
                                                                            try {
                                                                                handleOutOfResourcesException(e);
                                                                                return;
                                                                            } catch (RemoteException e12) {
                                                                                isPreviousTransparentRegionReseted3 = true;
                                                                                params4 = surfaceGenerationId;
                                                                                relayoutResult3 = relayoutResult2;
                                                                                updatedConfiguration = updatedConfiguration2;
                                                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                                                this.mAttachInfo.mWindowTop = frame.top;
                                                                                this.mWidth = frame.width();
                                                                                this.mHeight = frame.height();
                                                                                if (this.mSurfaceHolder != null) {
                                                                                }
                                                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                                                this.mNeedsRendererSetup = false;
                                                                                int childWidthMeasureSpec222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                                                int childHeightMeasureSpec222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                                                performMeasure(childWidthMeasureSpec222222, childHeightMeasureSpec222222);
                                                                                int width222222 = host.getMeasuredWidth();
                                                                                int height222222 = host.getMeasuredHeight();
                                                                                measureAgain = false;
                                                                                relayoutResult4 = relayoutResult3;
                                                                                if (lp.horizontalWeight > 0.0f) {
                                                                                }
                                                                                if (lp.verticalWeight > 0.0f) {
                                                                                }
                                                                                if (measureAgain) {
                                                                                }
                                                                                layoutRequested = true;
                                                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                                                relayoutResult = relayoutResult4;
                                                                                if (focusChangedDueToTouchMode) {
                                                                                }
                                                                                if (!layoutRequested) {
                                                                                }
                                                                                if (!didLayout) {
                                                                                }
                                                                                if (didLayout) {
                                                                                }
                                                                                if (triggerGlobalLayoutListener) {
                                                                                }
                                                                                if (computesInternalInsets) {
                                                                                }
                                                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                                                if (this.mFirst) {
                                                                                }
                                                                                if (!viewVisibilityChanged) {
                                                                                }
                                                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                                                }
                                                                                if (!hasWindowFocus) {
                                                                                }
                                                                                if (regainedFocus) {
                                                                                }
                                                                                layoutParams = this.mWindowAttributes;
                                                                                if (layoutParams == null) {
                                                                                }
                                                                                if (!isToast) {
                                                                                }
                                                                                this.mFirst = false;
                                                                                this.mWillDrawSoon = false;
                                                                                this.mNewSurfaceNeeded = false;
                                                                                this.mActivityRelaunched = false;
                                                                                this.mViewVisibility = viewVisibility;
                                                                                this.mHadWindowFocus = hasWindowFocus;
                                                                                if (hasWindowFocus) {
                                                                                }
                                                                                cancelDraw = true;
                                                                                if ((relayoutResult & 2) != 0) {
                                                                                }
                                                                                cancelDraw = false;
                                                                                if (!cancelDraw) {
                                                                                }
                                                                                this.mIsInTraversal = false;
                                                                            }
                                                                        } catch (RemoteException e13) {
                                                                            hwInitialized = hwInitialized2;
                                                                            isPreviousTransparentRegionReseted3 = true;
                                                                            params4 = surfaceGenerationId;
                                                                            relayoutResult3 = relayoutResult2;
                                                                            updatedConfiguration = updatedConfiguration2;
                                                                            surfaceSizeChanged = surfaceSizeChanged3;
                                                                            isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                                            this.mAttachInfo.mWindowLeft = frame.left;
                                                                            this.mAttachInfo.mWindowTop = frame.top;
                                                                            this.mWidth = frame.width();
                                                                            this.mHeight = frame.height();
                                                                            if (this.mSurfaceHolder != null) {
                                                                            }
                                                                            threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                                            threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                                            this.mNeedsRendererSetup = false;
                                                                            int childWidthMeasureSpec2222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                                            int childHeightMeasureSpec2222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                                            performMeasure(childWidthMeasureSpec2222222, childHeightMeasureSpec2222222);
                                                                            int width2222222 = host.getMeasuredWidth();
                                                                            int height2222222 = host.getMeasuredHeight();
                                                                            measureAgain = false;
                                                                            relayoutResult4 = relayoutResult3;
                                                                            if (lp.horizontalWeight > 0.0f) {
                                                                            }
                                                                            if (lp.verticalWeight > 0.0f) {
                                                                            }
                                                                            if (measureAgain) {
                                                                            }
                                                                            layoutRequested = true;
                                                                            focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                                            isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                                            relayoutResult = relayoutResult4;
                                                                            if (focusChangedDueToTouchMode) {
                                                                            }
                                                                            if (!layoutRequested) {
                                                                            }
                                                                            if (!didLayout) {
                                                                            }
                                                                            if (didLayout) {
                                                                            }
                                                                            if (triggerGlobalLayoutListener) {
                                                                            }
                                                                            if (computesInternalInsets) {
                                                                            }
                                                                            this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                                            if (this.mFirst) {
                                                                            }
                                                                            if (!viewVisibilityChanged) {
                                                                            }
                                                                            if (!this.mAttachInfo.mHasWindowFocus) {
                                                                            }
                                                                            if (!hasWindowFocus) {
                                                                            }
                                                                            if (regainedFocus) {
                                                                            }
                                                                            layoutParams = this.mWindowAttributes;
                                                                            if (layoutParams == null) {
                                                                            }
                                                                            if (!isToast) {
                                                                            }
                                                                            this.mFirst = false;
                                                                            this.mWillDrawSoon = false;
                                                                            this.mNewSurfaceNeeded = false;
                                                                            this.mActivityRelaunched = false;
                                                                            this.mViewVisibility = viewVisibility;
                                                                            this.mHadWindowFocus = hasWindowFocus;
                                                                            if (hasWindowFocus) {
                                                                            }
                                                                            cancelDraw = true;
                                                                            if ((relayoutResult & 2) != 0) {
                                                                            }
                                                                            cancelDraw = false;
                                                                            if (!cancelDraw) {
                                                                            }
                                                                            this.mIsInTraversal = false;
                                                                        }
                                                                    }
                                                                    hwInitialized = hwInitialized2;
                                                                    isPreviousTransparentRegionReseted3 = true;
                                                                } catch (Surface.OutOfResourcesException e14) {
                                                                    e = e14;
                                                                    handleOutOfResourcesException(e);
                                                                    return;
                                                                }
                                                            } catch (Surface.OutOfResourcesException e15) {
                                                                e = e15;
                                                                insetsPending = insetsPending2;
                                                                handleOutOfResourcesException(e);
                                                                return;
                                                            }
                                                        } else {
                                                            insetsPending = insetsPending2;
                                                            isPreviousTransparentRegionReseted3 = true;
                                                        }
                                                    } catch (RemoteException e16) {
                                                        insetsPending = insetsPending2;
                                                        isPreviousTransparentRegionReseted3 = true;
                                                        params4 = surfaceGenerationId;
                                                        relayoutResult3 = relayoutResult2;
                                                        updatedConfiguration = updatedConfiguration2;
                                                        surfaceSizeChanged = surfaceSizeChanged3;
                                                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                        this.mAttachInfo.mWindowLeft = frame.left;
                                                        this.mAttachInfo.mWindowTop = frame.top;
                                                        this.mWidth = frame.width();
                                                        this.mHeight = frame.height();
                                                        if (this.mSurfaceHolder != null) {
                                                        }
                                                        threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                        threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                        this.mNeedsRendererSetup = false;
                                                        int childWidthMeasureSpec22222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                        int childHeightMeasureSpec22222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                        performMeasure(childWidthMeasureSpec22222222, childHeightMeasureSpec22222222);
                                                        int width22222222 = host.getMeasuredWidth();
                                                        int height22222222 = host.getMeasuredHeight();
                                                        measureAgain = false;
                                                        relayoutResult4 = relayoutResult3;
                                                        if (lp.horizontalWeight > 0.0f) {
                                                        }
                                                        if (lp.verticalWeight > 0.0f) {
                                                        }
                                                        if (measureAgain) {
                                                        }
                                                        layoutRequested = true;
                                                        focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                        isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                        relayoutResult = relayoutResult4;
                                                        if (focusChangedDueToTouchMode) {
                                                        }
                                                        if (!layoutRequested) {
                                                        }
                                                        if (!didLayout) {
                                                        }
                                                        if (didLayout) {
                                                        }
                                                        if (triggerGlobalLayoutListener) {
                                                        }
                                                        if (computesInternalInsets) {
                                                        }
                                                        this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                        if (this.mFirst) {
                                                        }
                                                        if (!viewVisibilityChanged) {
                                                        }
                                                        if (!this.mAttachInfo.mHasWindowFocus) {
                                                        }
                                                        if (!hasWindowFocus) {
                                                        }
                                                        if (regainedFocus) {
                                                        }
                                                        layoutParams = this.mWindowAttributes;
                                                        if (layoutParams == null) {
                                                        }
                                                        if (!isToast) {
                                                        }
                                                        this.mFirst = false;
                                                        this.mWillDrawSoon = false;
                                                        this.mNewSurfaceNeeded = false;
                                                        this.mActivityRelaunched = false;
                                                        this.mViewVisibility = viewVisibility;
                                                        this.mHadWindowFocus = hasWindowFocus;
                                                        if (hasWindowFocus) {
                                                        }
                                                        cancelDraw = true;
                                                        if ((relayoutResult & 2) != 0) {
                                                        }
                                                        cancelDraw = false;
                                                        if (!cancelDraw) {
                                                        }
                                                        this.mIsInTraversal = false;
                                                    }
                                                } else {
                                                    traversalTime2 = traversalTime;
                                                    insetsPending = insetsPending2;
                                                }
                                            } catch (RemoteException e17) {
                                                insetsPending = insetsPending2;
                                                params4 = surfaceGenerationId;
                                                relayoutResult3 = relayoutResult2;
                                                updatedConfiguration = updatedConfiguration2;
                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                this.mAttachInfo.mWindowTop = frame.top;
                                                this.mWidth = frame.width();
                                                this.mHeight = frame.height();
                                                if (this.mSurfaceHolder != null) {
                                                }
                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                int childWidthMeasureSpec222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec222222222, childHeightMeasureSpec222222222);
                                                int width222222222 = host.getMeasuredWidth();
                                                int height222222222 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        } else {
                                            traversalTime2 = traversalTime;
                                            insetsPending = insetsPending2;
                                            try {
                                                if (!this.mSurface.isValid()) {
                                                    try {
                                                        if (this.mLastScrolledFocus != null) {
                                                            this.mLastScrolledFocus.clear();
                                                        }
                                                        this.mCurScrollY = 0;
                                                        this.mScrollY = 0;
                                                        if (this.mView instanceof RootViewSurfaceTaker) {
                                                            ((RootViewSurfaceTaker) this.mView).onRootViewScrollYChanged(this.mCurScrollY);
                                                        }
                                                        if (this.mScroller != null) {
                                                            this.mScroller.abortAnimation();
                                                        }
                                                        if (this.mAttachInfo.mThreadedRenderer != null && this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                                                            if (this.mView != null) {
                                                                this.mAttachInfo.mThreadedRenderer.destroyHardwareResources(this.mView);
                                                            }
                                                            this.mAttachInfo.mThreadedRenderer.destroy();
                                                        }
                                                    } catch (RemoteException e18) {
                                                        params4 = surfaceGenerationId;
                                                        relayoutResult3 = relayoutResult2;
                                                        updatedConfiguration = updatedConfiguration2;
                                                        surfaceSizeChanged = surfaceSizeChanged3;
                                                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                        this.mAttachInfo.mWindowLeft = frame.left;
                                                        this.mAttachInfo.mWindowTop = frame.top;
                                                        this.mWidth = frame.width();
                                                        this.mHeight = frame.height();
                                                        if (this.mSurfaceHolder != null) {
                                                        }
                                                        threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                        threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                        this.mNeedsRendererSetup = false;
                                                        int childWidthMeasureSpec2222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                        int childHeightMeasureSpec2222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                        performMeasure(childWidthMeasureSpec2222222222, childHeightMeasureSpec2222222222);
                                                        int width2222222222 = host.getMeasuredWidth();
                                                        int height2222222222 = host.getMeasuredHeight();
                                                        measureAgain = false;
                                                        relayoutResult4 = relayoutResult3;
                                                        if (lp.horizontalWeight > 0.0f) {
                                                        }
                                                        if (lp.verticalWeight > 0.0f) {
                                                        }
                                                        if (measureAgain) {
                                                        }
                                                        layoutRequested = true;
                                                        focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                        isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                        relayoutResult = relayoutResult4;
                                                        if (focusChangedDueToTouchMode) {
                                                        }
                                                        if (!layoutRequested) {
                                                        }
                                                        if (!didLayout) {
                                                        }
                                                        if (didLayout) {
                                                        }
                                                        if (triggerGlobalLayoutListener) {
                                                        }
                                                        if (computesInternalInsets) {
                                                        }
                                                        this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                        if (this.mFirst) {
                                                        }
                                                        if (!viewVisibilityChanged) {
                                                        }
                                                        if (!this.mAttachInfo.mHasWindowFocus) {
                                                        }
                                                        if (!hasWindowFocus) {
                                                        }
                                                        if (regainedFocus) {
                                                        }
                                                        layoutParams = this.mWindowAttributes;
                                                        if (layoutParams == null) {
                                                        }
                                                        if (!isToast) {
                                                        }
                                                        this.mFirst = false;
                                                        this.mWillDrawSoon = false;
                                                        this.mNewSurfaceNeeded = false;
                                                        this.mActivityRelaunched = false;
                                                        this.mViewVisibility = viewVisibility;
                                                        this.mHadWindowFocus = hasWindowFocus;
                                                        if (hasWindowFocus) {
                                                        }
                                                        cancelDraw = true;
                                                        if ((relayoutResult & 2) != 0) {
                                                        }
                                                        cancelDraw = false;
                                                        if (!cancelDraw) {
                                                        }
                                                        this.mIsInTraversal = false;
                                                    }
                                                } else if ((surfaceGenerationId != this.mSurface.getGenerationId() || surfaceSizeChanged3 || windowRelayoutWasForced || colorModeChanged) && this.mSurfaceHolder == null && this.mAttachInfo.mThreadedRenderer != null) {
                                                    this.mFullRedrawNeeded = true;
                                                    try {
                                                        this.mAttachInfo.mThreadedRenderer.updateSurface(this.mSurface);
                                                    } catch (Surface.OutOfResourcesException e19) {
                                                        handleOutOfResourcesException(e19);
                                                        return;
                                                    }
                                                }
                                            } catch (RemoteException e20) {
                                                params4 = surfaceGenerationId;
                                                relayoutResult3 = relayoutResult2;
                                                updatedConfiguration = updatedConfiguration2;
                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                this.mAttachInfo.mWindowTop = frame.top;
                                                this.mWidth = frame.width();
                                                this.mHeight = frame.height();
                                                if (this.mSurfaceHolder != null) {
                                                }
                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                int childWidthMeasureSpec22222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec22222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec22222222222, childHeightMeasureSpec22222222222);
                                                int width22222222222 = host.getMeasuredWidth();
                                                int height22222222222 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        }
                                        boolean freeformResizing = (relayoutResult2 & 16) == 0;
                                        dragResizing = !freeformResizing || ((relayoutResult2 & 8) == 0);
                                        if (this.mDragResizing != dragResizing) {
                                            dragResizing2 = dragResizing;
                                            params4 = surfaceGenerationId;
                                        } else if (dragResizing) {
                                            if (freeformResizing) {
                                                i2 = 0;
                                            } else {
                                                i2 = 1;
                                            }
                                            this.mResizeMode = i2;
                                            try {
                                                try {
                                                    dragResizing2 = dragResizing;
                                                    params4 = surfaceGenerationId;
                                                } catch (RemoteException e21) {
                                                    params4 = surfaceGenerationId;
                                                    relayoutResult3 = relayoutResult2;
                                                    updatedConfiguration = updatedConfiguration2;
                                                    surfaceSizeChanged = surfaceSizeChanged3;
                                                    isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                    this.mAttachInfo.mWindowLeft = frame.left;
                                                    this.mAttachInfo.mWindowTop = frame.top;
                                                    this.mWidth = frame.width();
                                                    this.mHeight = frame.height();
                                                    if (this.mSurfaceHolder != null) {
                                                    }
                                                    threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                    threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                    this.mNeedsRendererSetup = false;
                                                    int childWidthMeasureSpec222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                    int childHeightMeasureSpec222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                    performMeasure(childWidthMeasureSpec222222222222, childHeightMeasureSpec222222222222);
                                                    int width222222222222 = host.getMeasuredWidth();
                                                    int height222222222222 = host.getMeasuredHeight();
                                                    measureAgain = false;
                                                    relayoutResult4 = relayoutResult3;
                                                    if (lp.horizontalWeight > 0.0f) {
                                                    }
                                                    if (lp.verticalWeight > 0.0f) {
                                                    }
                                                    if (measureAgain) {
                                                    }
                                                    layoutRequested = true;
                                                    focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                    isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                    relayoutResult = relayoutResult4;
                                                    if (focusChangedDueToTouchMode) {
                                                    }
                                                    if (!layoutRequested) {
                                                    }
                                                    if (!didLayout) {
                                                    }
                                                    if (didLayout) {
                                                    }
                                                    if (triggerGlobalLayoutListener) {
                                                    }
                                                    if (computesInternalInsets) {
                                                    }
                                                    this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                    if (this.mFirst) {
                                                    }
                                                    if (!viewVisibilityChanged) {
                                                    }
                                                    if (!this.mAttachInfo.mHasWindowFocus) {
                                                    }
                                                    if (!hasWindowFocus) {
                                                    }
                                                    if (regainedFocus) {
                                                    }
                                                    layoutParams = this.mWindowAttributes;
                                                    if (layoutParams == null) {
                                                    }
                                                    if (!isToast) {
                                                    }
                                                    this.mFirst = false;
                                                    this.mWillDrawSoon = false;
                                                    this.mNewSurfaceNeeded = false;
                                                    this.mActivityRelaunched = false;
                                                    this.mViewVisibility = viewVisibility;
                                                    this.mHadWindowFocus = hasWindowFocus;
                                                    if (hasWindowFocus) {
                                                    }
                                                    cancelDraw = true;
                                                    if ((relayoutResult & 2) != 0) {
                                                    }
                                                    cancelDraw = false;
                                                    if (!cancelDraw) {
                                                    }
                                                    this.mIsInTraversal = false;
                                                }
                                            } catch (RemoteException e22) {
                                                params4 = surfaceGenerationId;
                                                relayoutResult3 = relayoutResult2;
                                                updatedConfiguration = updatedConfiguration2;
                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                this.mAttachInfo.mWindowTop = frame.top;
                                                this.mWidth = frame.width();
                                                this.mHeight = frame.height();
                                                if (this.mSurfaceHolder != null) {
                                                }
                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                int childWidthMeasureSpec2222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec2222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec2222222222222, childHeightMeasureSpec2222222222222);
                                                int width2222222222222 = host.getMeasuredWidth();
                                                int height2222222222222 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                            try {
                                                startDragResizing(this.mPendingBackDropFrame, !(this.mWinFrame.width() == this.mPendingBackDropFrame.width() && this.mWinFrame.height() == this.mPendingBackDropFrame.height()), this.mPendingVisibleInsets, this.mPendingStableInsets, this.mResizeMode);
                                            } catch (RemoteException e23) {
                                                relayoutResult3 = relayoutResult2;
                                                updatedConfiguration = updatedConfiguration2;
                                                surfaceSizeChanged = surfaceSizeChanged3;
                                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                                this.mAttachInfo.mWindowLeft = frame.left;
                                                this.mAttachInfo.mWindowTop = frame.top;
                                                this.mWidth = frame.width();
                                                this.mHeight = frame.height();
                                                if (this.mSurfaceHolder != null) {
                                                }
                                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                int childWidthMeasureSpec22222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec22222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec22222222222222, childHeightMeasureSpec22222222222222);
                                                int width22222222222222 = host.getMeasuredWidth();
                                                int height22222222222222 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (measureAgain) {
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        } else {
                                            dragResizing2 = dragResizing;
                                            params4 = surfaceGenerationId;
                                            endDragResizing();
                                        }
                                        if (!this.mUseMTRenderer) {
                                            if (dragResizing2) {
                                                this.mCanvasOffsetX = this.mWinFrame.left;
                                                this.mCanvasOffsetY = this.mWinFrame.top;
                                            } else {
                                                this.mCanvasOffsetY = 0;
                                                this.mCanvasOffsetX = 0;
                                            }
                                        }
                                        relayoutResult3 = relayoutResult2;
                                        updatedConfiguration = updatedConfiguration2;
                                        surfaceSizeChanged = surfaceSizeChanged3;
                                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                        this.mAttachInfo.mWindowLeft = frame.left;
                                        this.mAttachInfo.mWindowTop = frame.top;
                                        if (!(this.mWidth == frame.width() && this.mHeight == frame.height())) {
                                            this.mWidth = frame.width();
                                            this.mHeight = frame.height();
                                        }
                                        if (this.mSurfaceHolder != null) {
                                            if (this.mSurface.isValid()) {
                                                this.mSurfaceHolder.mSurface = this.mSurface;
                                            }
                                            this.mSurfaceHolder.setSurfaceFrameSize(this.mWidth, this.mHeight);
                                            this.mSurfaceHolder.mSurfaceLock.unlock();
                                            if (this.mSurface.isValid()) {
                                                if (!hadSurface) {
                                                    this.mSurfaceHolder.ungetCallbacks();
                                                    this.mIsCreating = true;
                                                    SurfaceHolder.Callback[] callbacks = this.mSurfaceHolder.getCallbacks();
                                                    if (callbacks != null) {
                                                        int length = callbacks.length;
                                                        int i6 = 0;
                                                        while (i6 < length) {
                                                            callbacks[i6].surfaceCreated(this.mSurfaceHolder);
                                                            i6++;
                                                            callbacks = callbacks;
                                                        }
                                                    }
                                                    surfaceChanged = true;
                                                }
                                                if (surfaceChanged || params4 != this.mSurface.getGenerationId()) {
                                                    SurfaceHolder.Callback[] callbacks2 = this.mSurfaceHolder.getCallbacks();
                                                    if (callbacks2 != null) {
                                                        int i7 = 0;
                                                        for (int length2 = callbacks2.length; i7 < length2; length2 = length2) {
                                                            callbacks2[i7].surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                                                            i7++;
                                                            callbacks2 = callbacks2;
                                                            surfaceSizeChanged = surfaceSizeChanged;
                                                            isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted;
                                                        }
                                                        surfaceSizeChanged2 = surfaceSizeChanged;
                                                        isPreviousTransparentRegionReseted2 = isPreviousTransparentRegionReseted;
                                                    } else {
                                                        surfaceSizeChanged2 = surfaceSizeChanged;
                                                        isPreviousTransparentRegionReseted2 = isPreviousTransparentRegionReseted;
                                                    }
                                                } else {
                                                    surfaceSizeChanged2 = surfaceSizeChanged;
                                                    isPreviousTransparentRegionReseted2 = isPreviousTransparentRegionReseted;
                                                }
                                                this.mIsCreating = false;
                                            } else {
                                                surfaceSizeChanged2 = surfaceSizeChanged;
                                                isPreviousTransparentRegionReseted2 = isPreviousTransparentRegionReseted;
                                                if (hadSurface) {
                                                    notifySurfaceDestroyed();
                                                    this.mSurfaceHolder.mSurfaceLock.lock();
                                                    try {
                                                        this.mSurfaceHolder.mSurface = new Surface();
                                                    } finally {
                                                        this.mSurfaceHolder.mSurfaceLock.unlock();
                                                    }
                                                }
                                            }
                                        } else {
                                            surfaceSizeChanged2 = surfaceSizeChanged;
                                            isPreviousTransparentRegionReseted2 = isPreviousTransparentRegionReseted;
                                        }
                                        threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                        if (threadedRenderer != null && threadedRenderer.isEnabled() && (hwInitialized || this.mWidth != threadedRenderer.getWidth() || this.mHeight != threadedRenderer.getHeight() || this.mNeedsRendererSetup)) {
                                            threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                            this.mNeedsRendererSetup = false;
                                        }
                                        if (!this.mStopped || this.mReportNextDraw) {
                                            if (ensureTouchModeLocally((relayoutResult3 & 1) != 0) || this.mWidth != host.getMeasuredWidth() || this.mHeight != host.getMeasuredHeight() || contentInsetsChanged || updatedConfiguration) {
                                                int childWidthMeasureSpec222222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec222222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                                performMeasure(childWidthMeasureSpec222222222222222, childHeightMeasureSpec222222222222222);
                                                int width222222222222222 = host.getMeasuredWidth();
                                                int height222222222222222 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                relayoutResult4 = relayoutResult3;
                                                if (lp.horizontalWeight > 0.0f) {
                                                    childWidthMeasureSpec222222222222222 = View.MeasureSpec.makeMeasureSpec(width222222222222222 + ((int) (((float) (this.mWidth - width222222222222222)) * lp.horizontalWeight)), 1073741824);
                                                    measureAgain = true;
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                    childHeightMeasureSpec222222222222222 = View.MeasureSpec.makeMeasureSpec(height222222222222222 + ((int) (((float) (this.mHeight - height222222222222222)) * lp.verticalWeight)), 1073741824);
                                                    measureAgain = true;
                                                }
                                                if (measureAgain) {
                                                    performMeasure(childWidthMeasureSpec222222222222222, childHeightMeasureSpec222222222222222);
                                                }
                                                layoutRequested = true;
                                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                                isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                                relayoutResult = relayoutResult4;
                                                if (focusChangedDueToTouchMode) {
                                                }
                                                if (!layoutRequested) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                                if (this.mFirst) {
                                                }
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams == null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mFirst = false;
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                if (!cancelDraw) {
                                                }
                                                this.mIsInTraversal = false;
                                            }
                                        }
                                        relayoutResult4 = relayoutResult3;
                                        focusChangedDueToTouchMode = surfaceSizeChanged2;
                                        isPreviousTransparentRegionReseted4 = isPreviousTransparentRegionReseted2;
                                        relayoutResult = relayoutResult4;
                                        if (focusChangedDueToTouchMode) {
                                        }
                                        if (!layoutRequested) {
                                        }
                                        if (!didLayout) {
                                        }
                                        if (didLayout) {
                                        }
                                        if (triggerGlobalLayoutListener) {
                                        }
                                        if (computesInternalInsets) {
                                        }
                                        this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                        if (this.mFirst) {
                                        }
                                        if (!viewVisibilityChanged) {
                                        }
                                        if (!this.mAttachInfo.mHasWindowFocus) {
                                        }
                                        if (!hasWindowFocus) {
                                        }
                                        if (regainedFocus) {
                                        }
                                        layoutParams = this.mWindowAttributes;
                                        if (layoutParams == null) {
                                        }
                                        if (!isToast) {
                                        }
                                        this.mFirst = false;
                                        this.mWillDrawSoon = false;
                                        this.mNewSurfaceNeeded = false;
                                        this.mActivityRelaunched = false;
                                        this.mViewVisibility = viewVisibility;
                                        this.mHadWindowFocus = hasWindowFocus;
                                        if (hasWindowFocus) {
                                        }
                                        cancelDraw = true;
                                        if ((relayoutResult & 2) != 0) {
                                        }
                                        cancelDraw = false;
                                        if (!cancelDraw) {
                                        }
                                        this.mIsInTraversal = false;
                                    }
                                    params3 = params2;
                                } else {
                                    params3 = params2;
                                }
                                windowSizeMayChange = windowSizeMayChange2;
                                if (layoutRequested) {
                                }
                                boolean windowShouldResize2 = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowWidth2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowHeight2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                                if (!this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                                }
                                relayoutResult = 0;
                                updatedConfiguration = false;
                                surfaceGenerationId = this.mSurface.getGenerationId();
                                if (viewVisibility != 0) {
                                }
                                boolean windowRelayoutWasForced2 = this.mForceNextWindowRelayout;
                                focusChangedDueToTouchMode = false;
                                boolean isPreviousTransparentRegionReseted42 = false;
                                if (!this.mFirst) {
                                }
                                params4 = params3;
                                relayoutResult2 = 0;
                                this.mForceNextWindowRelayout = false;
                                if (!isViewVisible) {
                                }
                                baseSurfaceHolder = this.mSurfaceHolder;
                                if (baseSurfaceHolder == null) {
                                }
                                contentInsetsChanged = false;
                                hadSurface = this.mSurface.isValid();
                                if (params4 == null) {
                                }
                                if (this.mAttachInfo.mThreadedRenderer == null) {
                                }
                                relayoutResult2 = relayoutWindow(params4, viewVisibility, insetsPending2);
                                if (params4 != null) {
                                }
                                if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                                }
                                if (this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                                }
                                contentInsetsChanged = this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                                if (this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                                }
                                if (this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                                }
                                if (this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout)) {
                                }
                                boolean outsetsChanged2 = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                                surfaceSizeChanged3 = (relayoutResult2 & 32) == 0;
                                surfaceChanged |= surfaceSizeChanged3;
                                if (this.mPendingAlwaysConsumeSystemBars == this.mAttachInfo.mAlwaysConsumeSystemBars) {
                                }
                                boolean colorModeChanged2 = hasColorModeChanged(lp.getColorMode());
                                if (contentInsetsChanged) {
                                }
                                if (overscanInsetsChanged) {
                                }
                                if (stableInsetsChanged) {
                                }
                                if (cutoutChanged) {
                                }
                                if (alwaysConsumeSystemBarsChanged) {
                                }
                                this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                                this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                                this.mApplyInsetsRequested = false;
                                dispatchApplyInsets(host);
                                contentInsetsChanged = true;
                                if (visibleInsetsChanged) {
                                }
                                this.mAttachInfo.mThreadedRenderer.setWideGamut(lp.getColorMode() != 1);
                                if (hadSurface) {
                                }
                                if ((relayoutResult2 & 16) == 0) {
                                }
                                if (!freeformResizing) {
                                }
                                if (this.mDragResizing != dragResizing) {
                                }
                                if (!this.mUseMTRenderer) {
                                }
                                relayoutResult3 = relayoutResult2;
                                updatedConfiguration = updatedConfiguration2;
                                surfaceSizeChanged = surfaceSizeChanged3;
                                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                                this.mAttachInfo.mWindowLeft = frame.left;
                                this.mAttachInfo.mWindowTop = frame.top;
                                this.mWidth = frame.width();
                                this.mHeight = frame.height();
                                if (this.mSurfaceHolder != null) {
                                }
                                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                this.mNeedsRendererSetup = false;
                                int childWidthMeasureSpec2222222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                                int childHeightMeasureSpec2222222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                                performMeasure(childWidthMeasureSpec2222222222222222, childHeightMeasureSpec2222222222222222);
                                int width2222222222222222 = host.getMeasuredWidth();
                                int height2222222222222222 = host.getMeasuredHeight();
                                measureAgain = false;
                                relayoutResult4 = relayoutResult3;
                                if (lp.horizontalWeight > 0.0f) {
                                }
                                if (lp.verticalWeight > 0.0f) {
                                }
                                if (measureAgain) {
                                }
                                layoutRequested = true;
                                focusChangedDueToTouchMode = surfaceSizeChanged2;
                                isPreviousTransparentRegionReseted42 = isPreviousTransparentRegionReseted2;
                                relayoutResult = relayoutResult4;
                                if (focusChangedDueToTouchMode) {
                                }
                                if (!layoutRequested) {
                                }
                                if (!didLayout) {
                                }
                                if (didLayout) {
                                }
                                if (triggerGlobalLayoutListener) {
                                }
                                if (computesInternalInsets) {
                                }
                                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                                if (this.mFirst) {
                                }
                                if (!viewVisibilityChanged) {
                                }
                                if (!this.mAttachInfo.mHasWindowFocus) {
                                }
                                if (!hasWindowFocus) {
                                }
                                if (regainedFocus) {
                                }
                                layoutParams = this.mWindowAttributes;
                                if (layoutParams == null) {
                                }
                                if (!isToast) {
                                }
                                this.mFirst = false;
                                this.mWillDrawSoon = false;
                                this.mNewSurfaceNeeded = false;
                                this.mActivityRelaunched = false;
                                this.mViewVisibility = viewVisibility;
                                this.mHadWindowFocus = hasWindowFocus;
                                if (hasWindowFocus) {
                                }
                                cancelDraw = true;
                                if ((relayoutResult & 2) != 0) {
                                }
                                cancelDraw = false;
                                if (!cancelDraw) {
                                }
                                this.mIsInTraversal = false;
                            }
                        }
                    }
                    params2 = params;
                    if (params2 != null) {
                    }
                    if (this.mApplyInsetsRequested) {
                    }
                    windowSizeMayChange = windowSizeMayChange2;
                    if (layoutRequested) {
                    }
                    boolean windowShouldResize22 = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowWidth2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowHeight2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                    if (!this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                    }
                    relayoutResult = 0;
                    updatedConfiguration = false;
                    surfaceGenerationId = this.mSurface.getGenerationId();
                    if (viewVisibility != 0) {
                    }
                    boolean windowRelayoutWasForced22 = this.mForceNextWindowRelayout;
                    focusChangedDueToTouchMode = false;
                    boolean isPreviousTransparentRegionReseted422 = false;
                    if (!this.mFirst) {
                    }
                    params4 = params3;
                    relayoutResult2 = 0;
                    this.mForceNextWindowRelayout = false;
                    if (!isViewVisible) {
                    }
                    baseSurfaceHolder = this.mSurfaceHolder;
                    if (baseSurfaceHolder == null) {
                    }
                    contentInsetsChanged = false;
                    hadSurface = this.mSurface.isValid();
                    if (params4 == null) {
                    }
                    if (this.mAttachInfo.mThreadedRenderer == null) {
                    }
                    try {
                        relayoutResult2 = relayoutWindow(params4, viewVisibility, insetsPending2);
                        if (params4 != null) {
                        }
                        if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                        }
                        if (this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                        }
                        contentInsetsChanged = this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                        if (this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                        }
                        if (this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                        }
                        if (this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout)) {
                        }
                        boolean outsetsChanged22 = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                        surfaceSizeChanged3 = (relayoutResult2 & 32) == 0;
                        surfaceChanged |= surfaceSizeChanged3;
                        if (this.mPendingAlwaysConsumeSystemBars == this.mAttachInfo.mAlwaysConsumeSystemBars) {
                        }
                        boolean colorModeChanged22 = hasColorModeChanged(lp.getColorMode());
                        if (contentInsetsChanged) {
                        }
                        if (overscanInsetsChanged) {
                        }
                        if (stableInsetsChanged) {
                        }
                        if (cutoutChanged) {
                        }
                        if (alwaysConsumeSystemBarsChanged) {
                        }
                        this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                        this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                        this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                        this.mApplyInsetsRequested = false;
                        dispatchApplyInsets(host);
                        contentInsetsChanged = true;
                        if (visibleInsetsChanged) {
                        }
                        this.mAttachInfo.mThreadedRenderer.setWideGamut(lp.getColorMode() != 1);
                        if (hadSurface) {
                        }
                        if ((relayoutResult2 & 16) == 0) {
                        }
                        if (!freeformResizing) {
                        }
                        if (this.mDragResizing != dragResizing) {
                        }
                        if (!this.mUseMTRenderer) {
                        }
                        relayoutResult3 = relayoutResult2;
                        updatedConfiguration = updatedConfiguration2;
                        surfaceSizeChanged = surfaceSizeChanged3;
                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                    } catch (RemoteException e24) {
                        insetsPending = insetsPending2;
                        params4 = surfaceGenerationId;
                        relayoutResult3 = relayoutResult2;
                        updatedConfiguration = updatedConfiguration2;
                        surfaceSizeChanged = surfaceSizeChanged3;
                        isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                        this.mAttachInfo.mWindowLeft = frame.left;
                        this.mAttachInfo.mWindowTop = frame.top;
                        this.mWidth = frame.width();
                        this.mHeight = frame.height();
                        if (this.mSurfaceHolder != null) {
                        }
                        threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                        threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                        this.mNeedsRendererSetup = false;
                        int childWidthMeasureSpec22222222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                        int childHeightMeasureSpec22222222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                        performMeasure(childWidthMeasureSpec22222222222222222, childHeightMeasureSpec22222222222222222);
                        int width22222222222222222 = host.getMeasuredWidth();
                        int height22222222222222222 = host.getMeasuredHeight();
                        measureAgain = false;
                        relayoutResult4 = relayoutResult3;
                        if (lp.horizontalWeight > 0.0f) {
                        }
                        if (lp.verticalWeight > 0.0f) {
                        }
                        if (measureAgain) {
                        }
                        layoutRequested = true;
                        focusChangedDueToTouchMode = surfaceSizeChanged2;
                        isPreviousTransparentRegionReseted422 = isPreviousTransparentRegionReseted2;
                        relayoutResult = relayoutResult4;
                        if (focusChangedDueToTouchMode) {
                        }
                        if (!layoutRequested) {
                        }
                        if (!didLayout) {
                        }
                        if (didLayout) {
                        }
                        if (triggerGlobalLayoutListener) {
                        }
                        if (computesInternalInsets) {
                        }
                        this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                        if (this.mFirst) {
                        }
                        if (!viewVisibilityChanged) {
                        }
                        if (!this.mAttachInfo.mHasWindowFocus) {
                        }
                        if (!hasWindowFocus) {
                        }
                        if (regainedFocus) {
                        }
                        layoutParams = this.mWindowAttributes;
                        if (layoutParams == null) {
                        }
                        if (!isToast) {
                        }
                        this.mFirst = false;
                        this.mWillDrawSoon = false;
                        this.mNewSurfaceNeeded = false;
                        this.mActivityRelaunched = false;
                        this.mViewVisibility = viewVisibility;
                        this.mHadWindowFocus = hasWindowFocus;
                        if (hasWindowFocus) {
                        }
                        cancelDraw = true;
                        if ((relayoutResult & 2) != 0) {
                        }
                        cancelDraw = false;
                        if (!cancelDraw) {
                        }
                        this.mIsInTraversal = false;
                    }
                    this.mAttachInfo.mWindowLeft = frame.left;
                    this.mAttachInfo.mWindowTop = frame.top;
                    this.mWidth = frame.width();
                    this.mHeight = frame.height();
                    if (this.mSurfaceHolder != null) {
                    }
                    threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                    threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                    this.mNeedsRendererSetup = false;
                    int childWidthMeasureSpec222222222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                    int childHeightMeasureSpec222222222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                    performMeasure(childWidthMeasureSpec222222222222222222, childHeightMeasureSpec222222222222222222);
                    int width222222222222222222 = host.getMeasuredWidth();
                    int height222222222222222222 = host.getMeasuredHeight();
                    measureAgain = false;
                    relayoutResult4 = relayoutResult3;
                    if (lp.horizontalWeight > 0.0f) {
                    }
                    if (lp.verticalWeight > 0.0f) {
                    }
                    if (measureAgain) {
                    }
                    layoutRequested = true;
                    focusChangedDueToTouchMode = surfaceSizeChanged2;
                    isPreviousTransparentRegionReseted422 = isPreviousTransparentRegionReseted2;
                    relayoutResult = relayoutResult4;
                    if (focusChangedDueToTouchMode) {
                    }
                    if (!layoutRequested) {
                    }
                    if (!didLayout) {
                    }
                    if (didLayout) {
                    }
                    if (triggerGlobalLayoutListener) {
                    }
                    if (computesInternalInsets) {
                    }
                    this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                    if (this.mFirst) {
                    }
                    if (!viewVisibilityChanged) {
                    }
                    if (!this.mAttachInfo.mHasWindowFocus) {
                    }
                    if (!hasWindowFocus) {
                    }
                    if (regainedFocus) {
                    }
                    layoutParams = this.mWindowAttributes;
                    if (layoutParams == null) {
                    }
                    if (!isToast) {
                    }
                    this.mFirst = false;
                    this.mWillDrawSoon = false;
                    this.mNewSurfaceNeeded = false;
                    this.mActivityRelaunched = false;
                    this.mViewVisibility = viewVisibility;
                    this.mHadWindowFocus = hasWindowFocus;
                    if (hasWindowFocus) {
                    }
                    cancelDraw = true;
                    if ((relayoutResult & 2) != 0) {
                    }
                    cancelDraw = false;
                    if (!cancelDraw) {
                    }
                    this.mIsInTraversal = false;
                }
            }
            viewUserVisibilityChanged = false;
            WindowManager.LayoutParams params52 = null;
            if (!this.mWindowAttributesChanged) {
            }
            supportsScreen = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen();
            z = this.mLastInCompatMode;
            if (supportsScreen != z) {
            }
            this.mWindowAttributesChangesFlag = 0;
            Rect frame22 = this.mWinFrame;
            if (!this.mFirst) {
            }
            if (viewVisibilityChanged) {
            }
            if (this.mAttachInfo.mWindowVisibility != 0) {
            }
            getRunQueue().executeActions(this.mAttachInfo.mHandler);
            boolean insetsChanged22 = false;
            layoutRequested = !this.mLayoutRequested && (!this.mStopped || this.mReportNextDraw);
            if (!layoutRequested) {
            }
            if (collectViewAttributes()) {
            }
            if (this.mAttachInfo.mForceReportNewAttributes) {
            }
            View.AttachInfo attachInfo32 = this.mAttachInfo;
            attachInfo32.mViewVisibilityChanged = false;
            resizeMode = this.mSoftInputMode & 240;
            if (resizeMode == 0) {
            }
            params2 = params;
            if (params2 != null) {
            }
            if (this.mApplyInsetsRequested) {
            }
            windowSizeMayChange = windowSizeMayChange2;
            if (layoutRequested) {
            }
            boolean windowShouldResize222 = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowWidth2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowHeight2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
            if (!this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
            }
            relayoutResult = 0;
            updatedConfiguration = false;
            surfaceGenerationId = this.mSurface.getGenerationId();
            if (viewVisibility != 0) {
            }
            boolean windowRelayoutWasForced222 = this.mForceNextWindowRelayout;
            focusChangedDueToTouchMode = false;
            boolean isPreviousTransparentRegionReseted4222 = false;
            if (!this.mFirst) {
            }
            params4 = params3;
            relayoutResult2 = 0;
            this.mForceNextWindowRelayout = false;
            if (!isViewVisible) {
            }
            baseSurfaceHolder = this.mSurfaceHolder;
            if (baseSurfaceHolder == null) {
            }
            contentInsetsChanged = false;
            hadSurface = this.mSurface.isValid();
            if (params4 == null) {
            }
            try {
                if (this.mAttachInfo.mThreadedRenderer == null) {
                }
                relayoutResult2 = relayoutWindow(params4, viewVisibility, insetsPending2);
                if (params4 != null) {
                }
                if (!this.mPendingMergedConfiguration.equals(this.mLastReportedMergedConfiguration)) {
                }
                if (this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                }
                contentInsetsChanged = this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                if (this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                }
                if (this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                }
                if (this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout)) {
                }
                boolean outsetsChanged222 = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                surfaceSizeChanged3 = (relayoutResult2 & 32) == 0;
                surfaceChanged |= surfaceSizeChanged3;
                if (this.mPendingAlwaysConsumeSystemBars == this.mAttachInfo.mAlwaysConsumeSystemBars) {
                }
                boolean colorModeChanged222 = hasColorModeChanged(lp.getColorMode());
                if (contentInsetsChanged) {
                }
                if (overscanInsetsChanged) {
                }
                if (stableInsetsChanged) {
                }
                if (cutoutChanged) {
                }
                if (alwaysConsumeSystemBarsChanged) {
                }
                this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                this.mApplyInsetsRequested = false;
                dispatchApplyInsets(host);
                contentInsetsChanged = true;
                if (visibleInsetsChanged) {
                }
                this.mAttachInfo.mThreadedRenderer.setWideGamut(lp.getColorMode() != 1);
                if (hadSurface) {
                }
                if ((relayoutResult2 & 16) == 0) {
                }
                if (!freeformResizing) {
                }
                if (this.mDragResizing != dragResizing) {
                }
                if (!this.mUseMTRenderer) {
                }
                relayoutResult3 = relayoutResult2;
                updatedConfiguration = updatedConfiguration2;
                surfaceSizeChanged = surfaceSizeChanged3;
                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
            } catch (RemoteException e25) {
                hwInitialized = false;
                surfaceSizeChanged3 = false;
                isPreviousTransparentRegionReseted3 = false;
                insetsPending = insetsPending2;
                params4 = surfaceGenerationId;
                relayoutResult3 = relayoutResult2;
                updatedConfiguration = updatedConfiguration2;
                surfaceSizeChanged = surfaceSizeChanged3;
                isPreviousTransparentRegionReseted = isPreviousTransparentRegionReseted3;
                this.mAttachInfo.mWindowLeft = frame.left;
                this.mAttachInfo.mWindowTop = frame.top;
                this.mWidth = frame.width();
                this.mHeight = frame.height();
                if (this.mSurfaceHolder != null) {
                }
                threadedRenderer = this.mAttachInfo.mThreadedRenderer;
                threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                this.mNeedsRendererSetup = false;
                int childWidthMeasureSpec2222222222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
                int childHeightMeasureSpec2222222222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
                performMeasure(childWidthMeasureSpec2222222222222222222, childHeightMeasureSpec2222222222222222222);
                int width2222222222222222222 = host.getMeasuredWidth();
                int height2222222222222222222 = host.getMeasuredHeight();
                measureAgain = false;
                relayoutResult4 = relayoutResult3;
                if (lp.horizontalWeight > 0.0f) {
                }
                if (lp.verticalWeight > 0.0f) {
                }
                if (measureAgain) {
                }
                layoutRequested = true;
                focusChangedDueToTouchMode = surfaceSizeChanged2;
                isPreviousTransparentRegionReseted4222 = isPreviousTransparentRegionReseted2;
                relayoutResult = relayoutResult4;
                if (focusChangedDueToTouchMode) {
                }
                if (!layoutRequested) {
                }
                if (!didLayout) {
                }
                if (didLayout) {
                }
                if (triggerGlobalLayoutListener) {
                }
                if (computesInternalInsets) {
                }
                this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
                if (this.mFirst) {
                }
                if (!viewVisibilityChanged) {
                }
                if (!this.mAttachInfo.mHasWindowFocus) {
                }
                if (!hasWindowFocus) {
                }
                if (regainedFocus) {
                }
                layoutParams = this.mWindowAttributes;
                if (layoutParams == null) {
                }
                if (!isToast) {
                }
                this.mFirst = false;
                this.mWillDrawSoon = false;
                this.mNewSurfaceNeeded = false;
                this.mActivityRelaunched = false;
                this.mViewVisibility = viewVisibility;
                this.mHadWindowFocus = hasWindowFocus;
                if (hasWindowFocus) {
                }
                cancelDraw = true;
                if ((relayoutResult & 2) != 0) {
                }
                cancelDraw = false;
                if (!cancelDraw) {
                }
                this.mIsInTraversal = false;
            }
            this.mAttachInfo.mWindowLeft = frame.left;
            this.mAttachInfo.mWindowTop = frame.top;
            this.mWidth = frame.width();
            this.mHeight = frame.height();
            if (this.mSurfaceHolder != null) {
            }
            threadedRenderer = this.mAttachInfo.mThreadedRenderer;
            threadedRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
            this.mNeedsRendererSetup = false;
            int childWidthMeasureSpec22222222222222222222 = getRootMeasureSpec(this.mWidth, lp.width);
            int childHeightMeasureSpec22222222222222222222 = getRootMeasureSpec(this.mHeight, lp.height);
            performMeasure(childWidthMeasureSpec22222222222222222222, childHeightMeasureSpec22222222222222222222);
            int width22222222222222222222 = host.getMeasuredWidth();
            int height22222222222222222222 = host.getMeasuredHeight();
            measureAgain = false;
            relayoutResult4 = relayoutResult3;
            if (lp.horizontalWeight > 0.0f) {
            }
            if (lp.verticalWeight > 0.0f) {
            }
            if (measureAgain) {
            }
            layoutRequested = true;
            focusChangedDueToTouchMode = surfaceSizeChanged2;
            isPreviousTransparentRegionReseted4222 = isPreviousTransparentRegionReseted2;
            relayoutResult = relayoutResult4;
            if (focusChangedDueToTouchMode) {
            }
            if (!layoutRequested) {
            }
            if (!didLayout) {
            }
            if (didLayout) {
            }
            if (triggerGlobalLayoutListener) {
            }
            if (computesInternalInsets) {
            }
            this.mHwBlurWindowManager.updateWindowBlurParams(this, !this.mFirst || viewVisibilityChanged);
            if (this.mFirst) {
            }
            if (!viewVisibilityChanged) {
            }
            if (!this.mAttachInfo.mHasWindowFocus) {
            }
            if (!hasWindowFocus) {
            }
            if (regainedFocus) {
            }
            layoutParams = this.mWindowAttributes;
            if (layoutParams == null) {
            }
            if (!isToast) {
            }
            this.mFirst = false;
            this.mWillDrawSoon = false;
            this.mNewSurfaceNeeded = false;
            this.mActivityRelaunched = false;
            this.mViewVisibility = viewVisibility;
            this.mHadWindowFocus = hasWindowFocus;
            if (hasWindowFocus) {
            }
            cancelDraw = true;
            if ((relayoutResult & 2) != 0) {
            }
            cancelDraw = false;
            if (!cancelDraw) {
            }
            this.mIsInTraversal = false;
        }
    }

    private void notifySurfaceDestroyed() {
        this.mSurfaceHolder.ungetCallbacks();
        SurfaceHolder.Callback[] callbacks = this.mSurfaceHolder.getCallbacks();
        if (callbacks != null) {
            for (SurfaceHolder.Callback c : callbacks) {
                c.surfaceDestroyed(this.mSurfaceHolder);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeHandleWindowMove(Rect frame) {
        boolean windowMoved = (this.mAttachInfo.mWindowLeft == frame.left && this.mAttachInfo.mWindowTop == frame.top) ? false : true;
        if (windowMoved) {
            CompatibilityInfo.Translator translator = this.mTranslator;
            if (translator != null) {
                translator.translateRectInScreenToAppWinFrame(frame);
            }
            this.mAttachInfo.mWindowLeft = frame.left;
            this.mAttachInfo.mWindowTop = frame.top;
        }
        if (windowMoved || this.mAttachInfo.mNeedsUpdateLightCenter) {
            if (this.mAttachInfo.mThreadedRenderer != null) {
                this.mAttachInfo.mThreadedRenderer.setLightCenter(this.mAttachInfo);
            }
            this.mAttachInfo.mNeedsUpdateLightCenter = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWindowFocusChanged() {
        boolean hasWindowFocus;
        boolean inTouchMode;
        synchronized (this) {
            if (this.mWindowFocusChanged) {
                this.mWindowFocusChanged = false;
                hasWindowFocus = this.mUpcomingWindowFocus;
                inTouchMode = this.mUpcomingInTouchMode;
            } else {
                return;
            }
        }
        if (DEBUG_HWFLOW) {
            Log.i(this.mTag, "handleWindowFocusChanged: hasWindowFocus " + hasWindowFocus + " inTouchMode " + inTouchMode);
        }
        if (sNewInsetsMode != 0) {
            if (hasWindowFocus) {
                this.mInsetsController.onWindowFocusGained();
            } else {
                this.mInsetsController.onWindowFocusLost();
            }
        }
        if (this.mAdded) {
            profileRendering(hasWindowFocus);
            if (hasWindowFocus) {
                ensureTouchModeLocally(inTouchMode);
                if (this.mAttachInfo.mThreadedRenderer != null && this.mSurface.isValid()) {
                    this.mFullRedrawNeeded = true;
                    try {
                        WindowManager.LayoutParams lp = this.mWindowAttributes;
                        this.mAttachInfo.mThreadedRenderer.initializeIfNeeded(this.mWidth, this.mHeight, this.mAttachInfo, this.mSurface, lp != null ? lp.surfaceInsets : null);
                    } catch (Surface.OutOfResourcesException e) {
                        Log.e(this.mTag, "OutOfResourcesException locking surface", e);
                        try {
                            if (!this.mWindowSession.outOfMemory(this.mWindow)) {
                                Slog.w(this.mTag, "No processes killed for memory; killing self");
                                Process.killProcess(Process.myPid());
                            }
                        } catch (RemoteException e2) {
                        }
                        ViewRootHandler viewRootHandler = this.mHandler;
                        viewRootHandler.sendMessageDelayed(viewRootHandler.obtainMessage(6), MIN_PERIOD);
                        return;
                    }
                }
            }
            this.mAttachInfo.mHasWindowFocus = hasWindowFocus;
            this.mLastWasImTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
            InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService(InputMethodManager.class);
            if (imm != null && this.mLastWasImTarget && !isInLocalFocusMode()) {
                imm.onPreWindowFocus(this.mView, hasWindowFocus);
            }
            if (this.mView != null) {
                this.mAttachInfo.mKeyDispatchState.reset();
                this.mView.dispatchWindowFocusChanged(hasWindowFocus);
                this.mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(hasWindowFocus);
                if (this.mAttachInfo.mTooltipHost != null) {
                    this.mAttachInfo.mTooltipHost.hideTooltip();
                }
            }
            if (hasWindowFocus) {
                if (imm != null && this.mLastWasImTarget && !isInLocalFocusMode()) {
                    View view = this.mView;
                    imm.onPostWindowFocus(view, view.findFocus(), this.mWindowAttributes.softInputMode, !this.mHasHadWindowFocus, this.mWindowAttributes.flags);
                }
                this.mWindowAttributes.softInputMode &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
                ((WindowManager.LayoutParams) this.mView.getLayoutParams()).softInputMode &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
                this.mHasHadWindowFocus = true;
                fireAccessibilityFocusEventIfHasFocusedNode();
            } else if (this.mPointerCapture) {
                handlePointerCaptureChanged(false);
            }
        }
        this.mFirstInputStage.onWindowFocusChanged(hasWindowFocus);
    }

    private void fireAccessibilityFocusEventIfHasFocusedNode() {
        View focusedView;
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && (focusedView = this.mView.findFocus()) != null) {
            AccessibilityNodeProvider provider = focusedView.getAccessibilityNodeProvider();
            if (provider == null) {
                focusedView.sendAccessibilityEvent(8);
                return;
            }
            AccessibilityNodeInfo focusedNode = findFocusedVirtualNode(provider);
            if (focusedNode != null) {
                int virtualId = AccessibilityNodeInfo.getVirtualDescendantId(focusedNode.getSourceNodeId());
                AccessibilityEvent event = AccessibilityEvent.obtain(8);
                event.setSource(focusedView, virtualId);
                event.setPackageName(focusedNode.getPackageName());
                event.setChecked(focusedNode.isChecked());
                event.setContentDescription(focusedNode.getContentDescription());
                event.setPassword(focusedNode.isPassword());
                event.getText().add(focusedNode.getText());
                event.setEnabled(focusedNode.isEnabled());
                focusedView.getParent().requestSendAccessibilityEvent(focusedView, event);
                focusedNode.recycle();
            }
        }
    }

    private AccessibilityNodeInfo findFocusedVirtualNode(AccessibilityNodeProvider provider) {
        AccessibilityNodeInfo focusedNode = provider.findFocus(1);
        if (focusedNode != null) {
            return focusedNode;
        }
        if (!this.mContext.isAutofillCompatibilityEnabled()) {
            return null;
        }
        AccessibilityNodeInfo current = provider.createAccessibilityNodeInfo(-1);
        if (current != null && current.isFocused()) {
            return current;
        }
        Queue<AccessibilityNodeInfo> fringe = new LinkedList<>();
        fringe.offer(current);
        while (!fringe.isEmpty()) {
            AccessibilityNodeInfo current2 = fringe.poll();
            LongArray childNodeIds = current2.getChildNodeIds();
            if (childNodeIds != null && childNodeIds.size() > 0) {
                int childCount = childNodeIds.size();
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(childNodeIds.get(i)));
                    if (child != null) {
                        if (child.isFocused()) {
                            return child;
                        }
                        fringe.offer(child);
                    }
                }
                current2.recycle();
            }
        }
        return null;
    }

    private void handleOutOfResourcesException(Surface.OutOfResourcesException e) {
        Log.e(this.mTag, "OutOfResourcesException initializing HW surface", e);
        try {
            if (!this.mWindowSession.outOfMemory(this.mWindow) && Process.myUid() != 1000) {
                Slog.w(this.mTag, "No processes killed for memory; killing self");
                Process.killProcess(Process.myPid());
            }
        } catch (RemoteException e2) {
        }
        this.mLayoutRequested = true;
    }

    private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
        if (this.mView != null) {
            Trace.traceBegin(8, "measure");
            IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl();
            if (hwRtgSchedImpl != null) {
                try {
                    Trace.traceBegin(8, "Measure Message Send");
                    hwRtgSchedImpl.doMeasure();
                } finally {
                    Trace.traceEnd(8);
                }
            }
            this.mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            Trace.traceEnd(8);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInLayout() {
        return this.mInLayout;
    }

    /* access modifiers changed from: package-private */
    public boolean requestLayoutDuringLayout(View view) {
        if (view.mParent == null || view.mAttachInfo == null) {
            return true;
        }
        if (!this.mLayoutRequesters.contains(view)) {
            this.mLayoutRequesters.add(view);
        }
        if (!this.mHandlingLayoutInLayoutRequest) {
            return true;
        }
        return false;
    }

    /* JADX INFO: finally extract failed */
    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth, int desiredWindowHeight) {
        ArrayList<View> validLayoutRequesters;
        this.mLayoutRequested = false;
        this.mScrollMayChange = true;
        this.mInLayout = true;
        View host = this.mView;
        if (host != null) {
            Trace.traceBegin(8, TtmlUtils.TAG_LAYOUT);
            try {
                host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                this.mInLayout = false;
                if (this.mLayoutRequesters.size() > 0 && (validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, false)) != null) {
                    this.mHandlingLayoutInLayoutRequest = true;
                    int numValidRequests = validLayoutRequesters.size();
                    for (int i = 0; i < numValidRequests; i++) {
                        View view = validLayoutRequesters.get(i);
                        Log.w("View", "requestLayout() improperly called by " + view + " during layout: running second layout pass");
                        view.requestLayout();
                    }
                    measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                    this.mInLayout = true;
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                    this.mHandlingLayoutInLayoutRequest = false;
                    final ArrayList<View> validLayoutRequesters2 = getValidLayoutRequesters(this.mLayoutRequesters, true);
                    if (validLayoutRequesters2 != null) {
                        getRunQueue().post(new Runnable() {
                            /* class android.view.ViewRootImpl.AnonymousClass2 */

                            @Override // java.lang.Runnable
                            public void run() {
                                int numValidRequests = validLayoutRequesters2.size();
                                for (int i = 0; i < numValidRequests; i++) {
                                    View view = (View) validLayoutRequesters2.get(i);
                                    Log.w("View", "requestLayout() improperly called by " + view + " during second layout pass: posting in next frame");
                                    view.requestLayout();
                                }
                            }
                        });
                    }
                }
                Trace.traceEnd(8);
                this.mInLayout = false;
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }
    }

    private ArrayList<View> getValidLayoutRequesters(ArrayList<View> layoutRequesters, boolean secondLayoutRequests) {
        int numViewsRequestingLayout = layoutRequesters.size();
        ArrayList<View> validLayoutRequesters = null;
        for (int i = 0; i < numViewsRequestingLayout; i++) {
            View view = layoutRequesters.get(i);
            if (!(view == null || view.mAttachInfo == null || view.mParent == null || !(secondLayoutRequests || (view.mPrivateFlags & 4096) == 4096))) {
                boolean gone = false;
                View parent = view;
                while (true) {
                    if (parent == null) {
                        break;
                    } else if ((parent.mViewFlags & 12) == 8) {
                        gone = true;
                        break;
                    } else if (parent.mParent instanceof View) {
                        parent = (View) parent.mParent;
                    } else {
                        parent = null;
                    }
                }
                if (!gone) {
                    if (validLayoutRequesters == null) {
                        validLayoutRequesters = new ArrayList<>();
                    }
                    validLayoutRequesters.add(view);
                }
            }
        }
        if (!secondLayoutRequests) {
            for (int i2 = 0; i2 < numViewsRequestingLayout; i2++) {
                View view2 = layoutRequesters.get(i2);
                while (view2 != null && (view2.mPrivateFlags & 4096) != 0) {
                    view2.mPrivateFlags &= -4097;
                    if (view2.mParent instanceof View) {
                        view2 = (View) view2.mParent;
                    } else {
                        view2 = null;
                    }
                }
            }
        }
        layoutRequesters.clear();
        return validLayoutRequesters;
    }

    @Override // android.view.ViewParent
    public void requestTransparentRegion(View child) {
        checkThread();
        View view = this.mView;
        if (view == child) {
            view.mPrivateFlags |= 512;
            this.mWindowAttributesChanged = true;
            this.mWindowAttributesChangesFlag = 0;
            requestLayout();
        }
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        if (rootDimension == -2) {
            return View.MeasureSpec.makeMeasureSpec(windowSize, Integer.MIN_VALUE);
        }
        if (rootDimension != -1) {
            return View.MeasureSpec.makeMeasureSpec(rootDimension, 1073741824);
        }
        return View.MeasureSpec.makeMeasureSpec(windowSize, 1073741824);
    }

    @Override // android.view.ThreadedRenderer.DrawCallbacks
    public void onPreDraw(RecordingCanvas canvas) {
        if (!(this.mCurScrollY == 0 || this.mHardwareYOffset == 0 || !this.mAttachInfo.mThreadedRenderer.isOpaque())) {
            canvas.drawColor(-16777216);
        }
        canvas.translate((float) (-this.mHardwareXOffset), (float) (-this.mHardwareYOffset));
    }

    @Override // android.view.ThreadedRenderer.DrawCallbacks
    public void onPostDraw(RecordingCanvas canvas) {
        drawAccessibilityFocusedDrawableIfNeeded(canvas);
        if (this.mUseMTRenderer) {
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                this.mWindowCallbacks.get(i).onPostDraw(canvas);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void outputDisplayList(View view) {
        view.mRenderNode.output();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void profileRendering(boolean enabled) {
        if (this.mProfileRendering) {
            this.mRenderProfilingEnabled = enabled;
            Choreographer.FrameCallback frameCallback = this.mRenderProfiler;
            if (frameCallback != null) {
                this.mChoreographer.removeFrameCallback(frameCallback);
            }
            if (this.mRenderProfilingEnabled) {
                if (this.mRenderProfiler == null) {
                    this.mRenderProfiler = new Choreographer.FrameCallback() {
                        /* class android.view.ViewRootImpl.AnonymousClass3 */

                        @Override // android.view.Choreographer.FrameCallback
                        public void doFrame(long frameTimeNanos) {
                            ViewRootImpl.this.mDirty.set(0, 0, ViewRootImpl.this.mWidth, ViewRootImpl.this.mHeight);
                            ViewRootImpl.this.scheduleTraversals();
                            if (ViewRootImpl.this.mRenderProfilingEnabled) {
                                ViewRootImpl.this.mChoreographer.postFrameCallback(ViewRootImpl.this.mRenderProfiler);
                            }
                        }
                    };
                }
                this.mChoreographer.postFrameCallback(this.mRenderProfiler);
                return;
            }
            this.mRenderProfiler = null;
        }
    }

    private void trackFPS() {
        long nowTime = System.currentTimeMillis();
        if (this.mFpsStartTime < 0) {
            this.mFpsPrevTime = nowTime;
            this.mFpsStartTime = nowTime;
            this.mFpsNumFrames = 0;
            return;
        }
        this.mFpsNumFrames++;
        String thisHash = Integer.toHexString(System.identityHashCode(this));
        long totalTime = nowTime - this.mFpsStartTime;
        Log.v(this.mTag, "0x" + thisHash + "\tFrame time:\t" + (nowTime - this.mFpsPrevTime));
        this.mFpsPrevTime = nowTime;
        if (totalTime > 1000) {
            float fps = (((float) this.mFpsNumFrames) * 1000.0f) / ((float) totalTime);
            Log.v(this.mTag, "0x" + thisHash + "\tFPS:\t" + fps);
            this.mFpsStartTime = nowTime;
            this.mFpsNumFrames = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void drawPending() {
        this.mDrawsNeededToReport++;
    }

    /* access modifiers changed from: package-private */
    public void pendingDrawFinished() {
        int i = this.mDrawsNeededToReport;
        if (i != 0) {
            this.mDrawsNeededToReport = i - 1;
            if (this.mDrawsNeededToReport == 0) {
                reportDrawFinished();
                return;
            }
            return;
        }
        throw new RuntimeException("Unbalanced drawPending/pendingDrawFinished calls");
    }

    /* access modifiers changed from: private */
    public void postDrawFinished() {
        this.mHandler.sendEmptyMessage(29);
    }

    private void reportDrawFinished() {
        try {
            this.mDrawsNeededToReport = 0;
            this.mWindowSession.finishDrawing(this.mWindow);
        } catch (RemoteException e) {
        }
    }

    /* JADX INFO: finally extract failed */
    private void performDraw() {
        String str;
        if (!sSupportAod ? this.mAttachInfo.mDisplayState == 1 : this.mAttachInfo.mDisplayState == 1 || this.mAttachInfo.mDisplayState == 4 || this.mStopped) {
            if (!this.mReportNextDraw) {
                if (!sSupportAod) {
                    return;
                }
                if ((this.mAttachInfo.mDisplayState == 1 || this.mAttachInfo.mDisplayState == 4 || this.mStopped) && (str = this.mTag) != null && str.contains("[AOD]")) {
                    String str2 = this.mTag;
                    Slog.d(str2, "performDraw return,DisplayState:" + this.mAttachInfo.mDisplayState + ",mStopped:" + this.mStopped + ",mReportNextDraw:" + this.mReportNextDraw);
                    return;
                }
                return;
            }
        }
        if (this.mView != null) {
            boolean fullRedrawNeeded = this.mFullRedrawNeeded || this.mReportNextDraw;
            this.mFullRedrawNeeded = false;
            this.mIsDrawing = true;
            this.mHwViewRootImpl.setIsNeedDraw(true);
            Trace.traceBegin(8, "draw");
            boolean usingAsyncReport = false;
            if (this.mAttachInfo.mThreadedRenderer != null && this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                ArrayList<Runnable> commitCallbacks = this.mAttachInfo.mTreeObserver.captureFrameCommitCallbacks();
                if (this.mReportNextDraw) {
                    usingAsyncReport = true;
                    Handler handler = this.mAttachInfo.mHandler;
                    if (DEBUG_HWFLOW) {
                        Log.i(this.mTag, "reportNextDraw set callback");
                    }
                    this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(new HardwareRenderer.FrameCompleteCallback(handler, commitCallbacks) {
                        /* class android.view.$$Lambda$ViewRootImpl$YBiqAhbCbXVPSKdbE3K4rH2gpxI */
                        private final /* synthetic */ Handler f$1;
                        private final /* synthetic */ ArrayList f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // android.graphics.HardwareRenderer.FrameCompleteCallback
                        public final void onFrameComplete(long j) {
                            ViewRootImpl.this.lambda$performDraw$2$ViewRootImpl(this.f$1, this.f$2, j);
                        }
                    });
                } else if (commitCallbacks != null && commitCallbacks.size() > 0) {
                    this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(new HardwareRenderer.FrameCompleteCallback(commitCallbacks) {
                        /* class android.view.$$Lambda$ViewRootImpl$zlBUjCwDtoAWMNaHI62DIqeKFY */
                        private final /* synthetic */ ArrayList f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // android.graphics.HardwareRenderer.FrameCompleteCallback
                        public final void onFrameComplete(long j) {
                            Handler.this.postAtFrontOfQueue(new Runnable(this.f$1) {
                                /* class android.view.$$Lambda$ViewRootImpl$dgEKMWLAJVMlaVy41safRlNQBo */
                                private final /* synthetic */ ArrayList f$0;

                                {
                                    this.f$0 = r1;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    ViewRootImpl.lambda$performDraw$3(this.f$0);
                                }
                            });
                        }
                    });
                }
            } else if (DEBUG_HWFLOW) {
                String str3 = this.mTag;
                StringBuilder sb = new StringBuilder();
                sb.append("performDraw: mAttachInfo.mThreadedRenderer is ");
                sb.append(this.mAttachInfo.mThreadedRenderer == null ? "null" : "disabled");
                Log.w(str3, sb.toString());
            }
            try {
                boolean canUseAsync = draw(fullRedrawNeeded);
                if (usingAsyncReport && !canUseAsync) {
                    this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(null);
                    usingAsyncReport = false;
                }
                this.mIsDrawing = false;
                Trace.traceEnd(8);
                if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                    int count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                    for (int i = 0; i < count; i++) {
                        this.mAttachInfo.mPendingAnimatingRenderNodes.get(i).endAllAnimators();
                    }
                    this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
                }
                if (this.mReportNextDraw) {
                    this.mReportNextDraw = false;
                    CountDownLatch countDownLatch = this.mWindowDrawCountDown;
                    if (countDownLatch != null) {
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            Log.e(this.mTag, "Window redraw count down interrupted!");
                        }
                        this.mWindowDrawCountDown = null;
                    }
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        this.mAttachInfo.mThreadedRenderer.setStopped(this.mStopped);
                    }
                    if (this.mSurfaceHolder != null && this.mSurface.isValid()) {
                        new SurfaceCallbackHelper(new Runnable() {
                            /* class android.view.$$Lambda$ViewRootImpl$dznxCZGM2R1fsBljsJKomLjBRoM */

                            @Override // java.lang.Runnable
                            public final void run() {
                                ViewRootImpl.this.postDrawFinished();
                            }
                        }).dispatchSurfaceRedrawNeededAsync(this.mSurfaceHolder, this.mSurfaceHolder.getCallbacks());
                    } else if (!usingAsyncReport) {
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            this.mAttachInfo.mThreadedRenderer.fence();
                        }
                        pendingDrawFinished();
                    }
                }
            } catch (Throwable th) {
                this.mIsDrawing = false;
                Trace.traceEnd(8);
                throw th;
            }
        } else if (DEBUG_HWFLOW) {
            Log.w(this.mTag, "performDraw: mView is null");
        }
    }

    public /* synthetic */ void lambda$performDraw$2$ViewRootImpl(Handler handler, ArrayList commitCallbacks, long frameNr) {
        handler.postAtFrontOfQueue(new Runnable(commitCallbacks) {
            /* class android.view.$$Lambda$ViewRootImpl$7A_3tkr_Kw4TZAeIUGVlOoTcZhg */
            private final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ViewRootImpl.this.lambda$performDraw$1$ViewRootImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$performDraw$1$ViewRootImpl(ArrayList commitCallbacks) {
        if (DEBUG_HWFLOW) {
            Log.i(this.mTag, "nextDraw finished callback");
        }
        pendingDrawFinished();
        if (commitCallbacks != null) {
            for (int i = 0; i < commitCallbacks.size(); i++) {
                ((Runnable) commitCallbacks.get(i)).run();
            }
        }
    }

    static /* synthetic */ void lambda$performDraw$3(ArrayList commitCallbacks) {
        for (int i = 0; i < commitCallbacks.size(); i++) {
            ((Runnable) commitCallbacks.get(i)).run();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:114:0x01f7  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x021d A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0221  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01a2  */
    private boolean draw(boolean fullRedrawNeeded) {
        int curScrollY;
        boolean fullRedrawNeeded2;
        int yOffset;
        int yOffset2;
        boolean accessibilityFocusDirty;
        int yOffset3;
        Surface.OutOfResourcesException e;
        Drawable drawable;
        Scroller scroller;
        Surface surface = this.mSurface;
        if (!surface.isValid()) {
            return false;
        }
        if (!sFirstDrawComplete) {
            synchronized (sFirstDrawHandlers) {
                sFirstDrawComplete = true;
                int count = sFirstDrawHandlers.size();
                for (int i = 0; i < count; i++) {
                    this.mHandler.post(sFirstDrawHandlers.get(i));
                }
            }
        }
        Rect surfaceInsets = null;
        scrollToRectOrFocus(null, false);
        if (this.mAttachInfo.mViewScrollChanged) {
            View.AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mViewScrollChanged = false;
            attachInfo.mTreeObserver.dispatchOnScrollChanged();
        }
        Scroller scroller2 = this.mScroller;
        boolean animating = scroller2 != null && scroller2.computeScrollOffset();
        if (animating) {
            curScrollY = this.mScroller.getCurrY();
        } else {
            curScrollY = this.mScrollY;
        }
        if (this.mCurScrollY != curScrollY) {
            this.mCurScrollY = curScrollY;
            View view = this.mView;
            if (view instanceof RootViewSurfaceTaker) {
                ((RootViewSurfaceTaker) view).onRootViewScrollYChanged(this.mCurScrollY);
            }
            fullRedrawNeeded2 = true;
        } else {
            fullRedrawNeeded2 = fullRedrawNeeded;
        }
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean scalingRequired = this.mAttachInfo.mScalingRequired;
        Rect dirty = this.mDirty;
        if (this.mSurfaceHolder != null) {
            dirty.setEmpty();
            if (animating && (scroller = this.mScroller) != null) {
                scroller.abortAnimation();
            }
            return false;
        }
        if (fullRedrawNeeded2) {
            dirty.set(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        }
        this.mAttachInfo.mTreeObserver.dispatchOnDraw();
        int xOffset = -this.mCanvasOffsetX;
        int yOffset4 = (-this.mCanvasOffsetY) + curScrollY;
        WindowManager.LayoutParams params = this.mWindowAttributes;
        if (params != null) {
            surfaceInsets = params.surfaceInsets;
        }
        if (surfaceInsets != null) {
            int xOffset2 = xOffset - surfaceInsets.left;
            int yOffset5 = yOffset4 - surfaceInsets.top;
            dirty.offset(surfaceInsets.left, surfaceInsets.right);
            yOffset = yOffset5;
            yOffset2 = xOffset2;
        } else {
            yOffset = yOffset4;
            yOffset2 = xOffset;
        }
        Drawable drawable2 = this.mAttachInfo.mAccessibilityFocusDrawable;
        if (drawable2 != null) {
            Rect bounds = this.mAttachInfo.mTmpInvalRect;
            if (!getAccessibilityFocusedRect(bounds)) {
                bounds.setEmpty();
            }
            if (!bounds.equals(drawable2.getBounds())) {
                accessibilityFocusDirty = true;
                this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
                boolean useAsyncReport = false;
                if (dirty.isEmpty() || this.mIsAnimating || accessibilityFocusDirty) {
                    if (this.mAttachInfo.mThreadedRenderer != null || !this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            yOffset3 = yOffset;
                        } else if (this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                            yOffset3 = yOffset;
                        } else if (!this.mAttachInfo.mThreadedRenderer.isRequested()) {
                            yOffset3 = yOffset;
                        } else if (this.mSurface.isValid()) {
                            try {
                                try {
                                    this.mAttachInfo.mThreadedRenderer.initializeIfNeeded(this.mWidth, this.mHeight, this.mAttachInfo, this.mSurface, surfaceInsets);
                                    this.mFullRedrawNeeded = true;
                                    scheduleTraversals();
                                    return false;
                                } catch (Surface.OutOfResourcesException e2) {
                                    e = e2;
                                    handleOutOfResourcesException(e);
                                    return false;
                                }
                            } catch (Surface.OutOfResourcesException e3) {
                                e = e3;
                                handleOutOfResourcesException(e);
                                return false;
                            }
                        } else {
                            yOffset3 = yOffset;
                        }
                        if (!drawSoftware(surface, this.mAttachInfo, yOffset2, yOffset3, scalingRequired, dirty, surfaceInsets)) {
                            return false;
                        }
                    } else {
                        boolean invalidateRoot = accessibilityFocusDirty || this.mInvalidateRootRequested;
                        this.mInvalidateRootRequested = false;
                        this.mIsAnimating = false;
                        if (!(this.mHardwareYOffset == yOffset && this.mHardwareXOffset == yOffset2)) {
                            this.mHardwareYOffset = yOffset;
                            this.mHardwareXOffset = yOffset2;
                            invalidateRoot = true;
                        }
                        if (invalidateRoot) {
                            this.mAttachInfo.mThreadedRenderer.invalidateRoot();
                        }
                        dirty.setEmpty();
                        boolean updated = updateContentDrawBounds();
                        if (this.mReportNextDraw) {
                            drawable = drawable2;
                            this.mAttachInfo.mThreadedRenderer.setStopped(false);
                        } else {
                            drawable = drawable2;
                        }
                        if (updated) {
                            requestDrawWindow();
                        }
                        useAsyncReport = true;
                        this.mAttachInfo.mThreadedRenderer.draw(this.mView, this.mAttachInfo, this);
                    }
                }
                if (animating) {
                    this.mFullRedrawNeeded = true;
                    scheduleTraversals();
                }
                return useAsyncReport;
            }
        }
        accessibilityFocusDirty = false;
        this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
        boolean useAsyncReport2 = false;
        if (dirty.isEmpty()) {
        }
        if (this.mAttachInfo.mThreadedRenderer != null) {
        }
        if (this.mAttachInfo.mThreadedRenderer != null) {
        }
        if (!drawSoftware(surface, this.mAttachInfo, yOffset2, yOffset3, scalingRequired, dirty, surfaceInsets)) {
        }
        if (animating) {
        }
        return useAsyncReport2;
    }

    public boolean getDisplayInfo(DisplayInfo outDisplayInfo) {
        return this.mDisplay.getDisplayInfo(outDisplayInfo);
    }

    private boolean drawSoftware(Surface surface, View.AttachInfo attachInfo, int xoff, int yoff, boolean scalingRequired, Rect dirty, Rect surfaceInsets) {
        int dirtyYOffset;
        int dirtyYOffset2;
        if (surfaceInsets != null) {
            int dirtyXOffset = xoff + surfaceInsets.left;
            dirtyYOffset = yoff + surfaceInsets.top;
            dirtyYOffset2 = dirtyXOffset;
        } else {
            dirtyYOffset = yoff;
            dirtyYOffset2 = xoff;
        }
        try {
            dirty.offset(-dirtyYOffset2, -dirtyYOffset);
            int i = dirty.left;
            int i2 = dirty.top;
            int i3 = dirty.right;
            int i4 = dirty.bottom;
            Canvas canvas = this.mSurface.lockCanvas(dirty);
            canvas.setDensity(this.mDensity);
            try {
                if (!(canvas.isOpaque() && yoff == 0 && xoff == 0)) {
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                }
                dirty.setEmpty();
                this.mIsAnimating = false;
                this.mView.mPrivateFlags |= 32;
                canvas.translate((float) (-xoff), (float) (-yoff));
                if (this.mTranslator != null) {
                    this.mTranslator.translateCanvas(canvas);
                }
                canvas.setScreenDensity(scalingRequired ? this.mNoncompatDensity : 0);
                this.mView.draw(canvas);
                if (this.mHwBlurWindowManager != null) {
                    this.mHwBlurWindowManager.performDrawBlurLayer(this, this.mView);
                }
                drawAccessibilityFocusedDrawableIfNeeded(canvas);
                try {
                    return true;
                } catch (IllegalArgumentException e) {
                    Log.e(this.mTag, "Could not unlock surface", e);
                    this.mLayoutRequested = true;
                    return false;
                }
            } finally {
                surface.syncFrameInfo(this.mChoreographer);
                if (this.mHwBlurWindowManager != null) {
                    this.mHwBlurWindowManager.updateWindowBlurDrawOp(this, true);
                }
                surface.unlockCanvasAndPost(canvas);
            }
        } catch (Surface.OutOfResourcesException e2) {
            handleOutOfResourcesException(e2);
            return false;
        } catch (IllegalArgumentException e3) {
            Log.e(this.mTag, "Could not lock surface", e3);
            this.mLayoutRequested = true;
            return false;
        } finally {
            dirty.offset(dirtyYOffset2, dirtyYOffset);
        }
    }

    private void drawAccessibilityFocusedDrawableIfNeeded(Canvas canvas) {
        Rect bounds = this.mAttachInfo.mTmpInvalRect;
        if (getAccessibilityFocusedRect(bounds)) {
            Drawable drawable = getAccessibilityFocusedDrawable();
            if (drawable != null) {
                drawable.setBounds(bounds);
                drawable.draw(canvas);
            }
        } else if (this.mAttachInfo.mAccessibilityFocusDrawable != null) {
            this.mAttachInfo.mAccessibilityFocusDrawable.setBounds(0, 0, 0, 0);
        }
    }

    private boolean getAccessibilityFocusedRect(Rect bounds) {
        View host;
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mView.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled() || (host = this.mAccessibilityFocusedHost) == null || host.mAttachInfo == null) {
            return false;
        }
        if (host.getAccessibilityNodeProvider() == null) {
            host.getBoundsOnScreen(bounds, true);
        } else {
            AccessibilityNodeInfo accessibilityNodeInfo = this.mAccessibilityFocusedVirtualView;
            if (accessibilityNodeInfo == null) {
                return false;
            }
            accessibilityNodeInfo.getBoundsInScreen(bounds);
        }
        View.AttachInfo attachInfo = this.mAttachInfo;
        bounds.offset(0, attachInfo.mViewRootImpl.mScrollY);
        bounds.offset(-attachInfo.mWindowLeft, -attachInfo.mWindowTop);
        if (!bounds.intersect(0, 0, attachInfo.mViewRootImpl.mWidth, attachInfo.mViewRootImpl.mHeight)) {
            bounds.setEmpty();
        }
        return !bounds.isEmpty();
    }

    private Drawable getAccessibilityFocusedDrawable() {
        if (this.mAttachInfo.mAccessibilityFocusDrawable == null) {
            TypedValue value = new TypedValue();
            if (this.mView.mContext.getTheme().resolveAttribute(R.attr.accessibilityFocusedDrawable, value, true)) {
                this.mAttachInfo.mAccessibilityFocusDrawable = this.mView.mContext.getDrawable(value.resourceId);
            }
        }
        return this.mAttachInfo.mAccessibilityFocusDrawable;
    }

    /* access modifiers changed from: package-private */
    public void updateSystemGestureExclusionRectsForView(View view) {
        this.mGestureExclusionTracker.updateRectsForView(view);
        this.mHandler.sendEmptyMessage(32);
    }

    /* access modifiers changed from: package-private */
    public void systemGestureExclusionChanged() {
        List<Rect> rectsForWindowManager = this.mGestureExclusionTracker.computeChangedRects();
        if (rectsForWindowManager != null && this.mView != null) {
            try {
                this.mWindowSession.reportSystemGestureExclusionChanged(this.mWindow, rectsForWindowManager);
                this.mAttachInfo.mTreeObserver.dispatchOnSystemGestureExclusionRectsChanged(rectsForWindowManager);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRootSystemGestureExclusionRects(List<Rect> rects) {
        this.mGestureExclusionTracker.setRootSystemGestureExclusionRects(rects);
        this.mHandler.sendEmptyMessage(32);
    }

    public List<Rect> getRootSystemGestureExclusionRects() {
        return this.mGestureExclusionTracker.getRootSystemGestureExclusionRects();
    }

    public void requestInvalidateRootRenderNode() {
        this.mInvalidateRootRequested = true;
    }

    /* access modifiers changed from: package-private */
    public boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
        Rect ci = this.mAttachInfo.mContentInsets;
        Rect vi = this.mAttachInfo.mVisibleInsets;
        int scrollY = 0;
        boolean handled = false;
        if (vi.left > ci.left || vi.top > ci.top || vi.right > ci.right || vi.bottom > ci.bottom) {
            scrollY = this.mScrollY;
            View focus = this.mView.findFocus();
            if (focus == null) {
                return false;
            }
            WeakReference<View> weakReference = this.mLastScrolledFocus;
            View lastScrolledFocus = weakReference != null ? weakReference.get() : null;
            if (focus != lastScrolledFocus) {
                rectangle = null;
            }
            if (!(focus == lastScrolledFocus && !this.mScrollMayChange && rectangle == null)) {
                this.mLastScrolledFocus = new WeakReference<>(focus);
                this.mScrollMayChange = false;
                if (focus.getGlobalVisibleRect(this.mVisRect, null)) {
                    if (rectangle == null) {
                        focus.getFocusedRect(this.mTempRect);
                        View view = this.mView;
                        if (view instanceof ViewGroup) {
                            ((ViewGroup) view).offsetDescendantRectToMyCoords(focus, this.mTempRect);
                        }
                    } else {
                        this.mTempRect.set(rectangle);
                    }
                    if (this.mTempRect.intersect(this.mVisRect)) {
                        if (this.mTempRect.height() <= (this.mView.getHeight() - vi.top) - vi.bottom) {
                            if (this.mTempRect.top < vi.top) {
                                scrollY = this.mTempRect.top - vi.top;
                            } else if (this.mTempRect.bottom > this.mView.getHeight() - vi.bottom) {
                                scrollY = this.mTempRect.bottom - (this.mView.getHeight() - vi.bottom);
                            } else {
                                scrollY = 0;
                            }
                        }
                        handled = true;
                    }
                }
            }
        }
        if (scrollY != this.mScrollY) {
            if (!immediate) {
                if (this.mScroller == null) {
                    this.mScroller = new Scroller(this.mView.getContext());
                }
                Scroller scroller = this.mScroller;
                int i = this.mScrollY;
                scroller.startScroll(0, i, 0, scrollY - i);
            } else {
                Scroller scroller2 = this.mScroller;
                if (scroller2 != null) {
                    scroller2.abortAnimation();
                }
            }
            this.mScrollY = scrollY;
        }
        return handled;
    }

    @UnsupportedAppUsage
    public View getAccessibilityFocusedHost() {
        return this.mAccessibilityFocusedHost;
    }

    @UnsupportedAppUsage
    public AccessibilityNodeInfo getAccessibilityFocusedVirtualView() {
        return this.mAccessibilityFocusedVirtualView;
    }

    /* access modifiers changed from: package-private */
    public void setAccessibilityFocus(View view, AccessibilityNodeInfo node) {
        if (this.mAccessibilityFocusedVirtualView != null) {
            AccessibilityNodeInfo focusNode = this.mAccessibilityFocusedVirtualView;
            View focusHost = this.mAccessibilityFocusedHost;
            this.mAccessibilityFocusedHost = null;
            this.mAccessibilityFocusedVirtualView = null;
            focusHost.clearAccessibilityFocusNoCallbacks(64);
            AccessibilityNodeProvider provider = focusHost.getAccessibilityNodeProvider();
            if (provider != null) {
                focusNode.getBoundsInParent(this.mTempRect);
                focusHost.invalidate(this.mTempRect);
                provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(focusNode.getSourceNodeId()), 128, null);
            }
            focusNode.recycle();
        }
        View view2 = this.mAccessibilityFocusedHost;
        if (!(view2 == null || view2 == view)) {
            view2.clearAccessibilityFocusNoCallbacks(64);
        }
        this.mAccessibilityFocusedHost = view;
        this.mAccessibilityFocusedVirtualView = node;
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.invalidateRoot();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPointerCapture() {
        return this.mPointerCapture;
    }

    /* access modifiers changed from: package-private */
    public void requestPointerCapture(boolean enabled) {
        if (this.mPointerCapture != enabled) {
            InputManager.getInstance().requestPointerCapture(this.mAttachInfo.mWindowToken, enabled);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePointerCaptureChanged(boolean hasCapture) {
        if (this.mPointerCapture != hasCapture) {
            this.mPointerCapture = hasCapture;
            View view = this.mView;
            if (view != null) {
                view.dispatchPointerCaptureChanged(hasCapture);
            }
        }
    }

    private boolean hasColorModeChanged(int colorMode) {
        if (this.mAttachInfo.mThreadedRenderer == null) {
            return false;
        }
        boolean isWideGamut = colorMode == 1;
        if (this.mAttachInfo.mThreadedRenderer.isWideGamut() == isWideGamut) {
            return false;
        }
        if (!isWideGamut || this.mContext.getResources().getConfiguration().isScreenWideColorGamut()) {
            return true;
        }
        return false;
    }

    @Override // android.view.ViewParent
    public void requestChildFocus(View child, View focused) {
        checkThread();
        scheduleTraversals();
    }

    @Override // android.view.ViewParent
    public void clearChildFocus(View child) {
        checkThread();
        scheduleTraversals();
    }

    @Override // android.view.ViewParent
    public ViewParent getParentForAccessibility() {
        return null;
    }

    @Override // android.view.ViewParent
    public void focusableViewAvailable(View v) {
        checkThread();
        View view = this.mView;
        if (view == null) {
            return;
        }
        if (view.hasFocus()) {
            View focused = this.mView.findFocus();
            if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == 262144 && isViewDescendantOf(v, focused)) {
                v.requestFocus();
            }
        } else if (sAlwaysAssignFocus || !this.mAttachInfo.mInTouchMode) {
            v.requestFocus();
        }
    }

    @Override // android.view.ViewParent
    public void recomputeViewAttributes(View child) {
        checkThread();
        if (this.mView == child) {
            this.mAttachInfo.mRecomputeGlobalAttributes = true;
            if (!this.mWillDrawSoon) {
                scheduleTraversals();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchDetachedFromWindow() {
        InputQueue inputQueue;
        this.mFirstInputStage.onDetachedFromWindow();
        View view = this.mView;
        if (!(view == null || view.mAttachInfo == null)) {
            this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(false);
            this.mView.dispatchDetachedFromWindow();
        }
        this.mAccessibilityInteractionConnectionManager.ensureNoConnection();
        this.mAccessibilityManager.removeAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager);
        this.mAccessibilityManager.removeHighTextContrastStateChangeListener(this.mHighContrastTextManager);
        removeSendWindowContentChangedCallback();
        destroyHardwareRenderer();
        setAccessibilityFocus(null, null);
        this.mView.assignParent(null);
        this.mView = null;
        this.mAttachInfo.mRootView = null;
        destroySurface();
        InputQueue.Callback callback = this.mInputQueueCallback;
        if (!(callback == null || (inputQueue = this.mInputQueue) == null)) {
            callback.onInputQueueDestroyed(inputQueue);
            this.mInputQueue.dispose();
            this.mInputQueueCallback = null;
            this.mInputQueue = null;
        }
        WindowInputEventReceiver windowInputEventReceiver = this.mInputEventReceiver;
        if (windowInputEventReceiver != null) {
            windowInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        try {
            this.mWindowSession.remove(this.mWindow);
        } catch (RemoteException e) {
        }
        InputChannel inputChannel = this.mInputChannel;
        if (inputChannel != null) {
            inputChannel.dispose();
            this.mInputChannel = null;
        }
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        unscheduleTraversals();
        this.mHwRio.detachRio();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performConfigurationChange(MergedConfiguration mergedConfiguration, boolean force, int newDisplayId) {
        if (mergedConfiguration != null) {
            Configuration globalConfig = mergedConfiguration.getGlobalConfiguration();
            Configuration overrideConfig = mergedConfiguration.getOverrideConfiguration();
            if (!(HwActivityTaskManager.getVirtualDisplayId(HwActivityTaskManager.PAD_CAST) == -1 || overrideConfig == null || overrideConfig.getWindowConfigurationBounds() == null)) {
                Rect rect = overrideConfig.getWindowConfigurationBounds();
                Log.i(TAG, "pad cast cacheSize compute. rect: " + rect);
                ViewConfiguration.get(this.mContext).setPadCastMaximumDrawingCacheSize(rect.width() * 4 * rect.height());
            }
            CompatibilityInfo ci = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
            if (!ci.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
                globalConfig = new Configuration(globalConfig);
                ci.applyToConfiguration(this.mNoncompatDensity, globalConfig);
            }
            synchronized (sConfigCallbacks) {
                for (int i = sConfigCallbacks.size() - 1; i >= 0; i--) {
                    sConfigCallbacks.get(i).onConfigurationChanged(globalConfig);
                }
            }
            this.mLastReportedMergedConfiguration.setConfiguration(globalConfig, overrideConfig);
            this.mForceNextConfigUpdate = force;
            ActivityConfigCallback activityConfigCallback = this.mActivityConfigCallback;
            if (activityConfigCallback != null) {
                activityConfigCallback.onConfigurationChanged(overrideConfig, newDisplayId);
            } else {
                updateConfiguration(newDisplayId);
            }
            this.mForceNextConfigUpdate = false;
            return;
        }
        throw new IllegalArgumentException("No merged config provided.");
    }

    public void updateConfiguration(int newDisplayId) {
        View view = this.mView;
        if (view != null) {
            Resources localResources = view.getResources();
            Configuration config = localResources.getConfiguration();
            if (newDisplayId != -1) {
                onMovedToDisplay(newDisplayId, config);
            }
            if (this.mForceNextConfigUpdate || this.mLastConfigurationFromResources.diff(config) != 0) {
                if (!localResources.getClass().equals(Resources.class)) {
                    Log.w(TAG, "resources is not instanceof android Resources: " + localResources);
                }
                updateInternalDisplay(this.mDisplay.getDisplayId(), localResources);
                if (this.mDisplay != null || this.mView.getContext() == null || !isValidExtDisplayId(this.mView.getContext())) {
                    int lastLayoutDirection = this.mLastConfigurationFromResources.getLayoutDirection();
                    int currentLayoutDirection = config.getLayoutDirection();
                    this.mLastConfigurationFromResources.setTo(config);
                    if (lastLayoutDirection != currentLayoutDirection && this.mViewLayoutDirectionInitial == 2) {
                        this.mView.setLayoutDirection(currentLayoutDirection);
                    }
                    this.mView.dispatchConfigurationChanged(config);
                    this.mForceNextWindowRelayout = true;
                    requestLayout();
                } else {
                    HwPCUtils.log(TAG, "mDisplay is null...");
                    doDie();
                    return;
                }
            }
            updateForceDarkMode();
            this.hwForceDarkState = HwFrameworkFactory.getHwForceDarkManager().updateHwForceDarkState(this.mContext, getView(), this.mWindowAttributes);
        }
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewDescendantOf((View) theParent, parent)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static void forceLayout(View view) {
        view.forceLayout();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                forceLayout(group.getChildAt(i));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class ViewRootHandler extends Handler {
        ViewRootHandler() {
        }

        @Override // android.os.Handler
        public String getMessageName(Message message) {
            switch (message.what) {
                case 1:
                    return "MSG_INVALIDATE";
                case 2:
                    return "MSG_INVALIDATE_RECT";
                case 3:
                    return "MSG_DIE";
                case 4:
                    return "MSG_RESIZED";
                case 5:
                    return "MSG_RESIZED_REPORT";
                case 6:
                    return "MSG_WINDOW_FOCUS_CHANGED";
                case 7:
                    return "MSG_DISPATCH_INPUT_EVENT";
                case 8:
                    return "MSG_DISPATCH_APP_VISIBILITY";
                case 9:
                    return "MSG_DISPATCH_GET_NEW_SURFACE";
                case 10:
                case 22:
                case 26:
                default:
                    return super.getMessageName(message);
                case 11:
                    return "MSG_DISPATCH_KEY_FROM_IME";
                case 12:
                    return "MSG_DISPATCH_KEY_FROM_AUTOFILL";
                case 13:
                    return "MSG_CHECK_FOCUS";
                case 14:
                    return "MSG_CLOSE_SYSTEM_DIALOGS";
                case 15:
                    return "MSG_DISPATCH_DRAG_EVENT";
                case 16:
                    return "MSG_DISPATCH_DRAG_LOCATION_EVENT";
                case 17:
                    return "MSG_DISPATCH_SYSTEM_UI_VISIBILITY";
                case 18:
                    return "MSG_UPDATE_CONFIGURATION";
                case 19:
                    return "MSG_PROCESS_INPUT_EVENTS";
                case 20:
                    return "MSG_DISPATCH_DRAG_ENTERED";
                case 21:
                    return "MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST";
                case 23:
                    return "MSG_WINDOW_MOVED";
                case 24:
                    return "MSG_SYNTHESIZE_INPUT_EVENT";
                case 25:
                    return "MSG_DISPATCH_WINDOW_SHOWN";
                case 27:
                    return "MSG_UPDATE_POINTER_ICON";
                case 28:
                    return "MSG_POINTER_CAPTURE_CHANGED";
                case 29:
                    return "MSG_DRAW_FINISHED";
                case 30:
                    return "MSG_INSETS_CHANGED";
                case 31:
                    return "MSG_INSETS_CONTROL_CHANGED";
                case 32:
                    return "MSG_SYSTEM_GESTURE_EXCLUSION_CHANGED";
            }
        }

        @Override // android.os.Handler
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if (msg.what != 26 || msg.obj != null) {
                return super.sendMessageAtTime(msg, uptimeMillis);
            }
            throw new NullPointerException("Attempted to call MSG_REQUEST_KEYBOARD_SHORTCUTS with null receiver:");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = -1;
            boolean hasCapture = true;
            switch (msg.what) {
                case 1:
                    ((View) msg.obj).invalidate();
                    return;
                case 2:
                    View.AttachInfo.InvalidateInfo info = (View.AttachInfo.InvalidateInfo) msg.obj;
                    info.target.invalidate(info.left, info.top, info.right, info.bottom);
                    info.recycle();
                    return;
                case 3:
                    ViewRootImpl.this.doDie();
                    return;
                case 4:
                    SomeArgs args = (SomeArgs) msg.obj;
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (ViewRootImpl.this.mWinFrame.equals(args.arg1) && ViewRootImpl.this.mPendingOverscanInsets.equals(args.arg5) && ViewRootImpl.this.mPendingContentInsets.equals(args.arg2) && ViewRootImpl.this.mPendingStableInsets.equals(args.arg6) && ViewRootImpl.this.mPendingDisplayCutout.get().equals(args.arg9) && ViewRootImpl.this.mPendingVisibleInsets.equals(args.arg3) && ViewRootImpl.this.mPendingOutsets.equals(args.arg7) && ViewRootImpl.this.mPendingBackDropFrame.equals(args.arg8) && args.arg4 == null && args.argi1 == 0 && ViewRootImpl.this.mDisplay.getDisplayId() == args.argi3) {
                        return;
                    }
                case 5:
                    break;
                case 6:
                    ViewRootImpl.this.handleWindowFocusChanged();
                    return;
                case 7:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    ViewRootImpl.this.enqueueInputEvent((InputEvent) args2.arg1, (InputEventReceiver) args2.arg2, 0, true);
                    args2.recycle();
                    return;
                case 8:
                    ViewRootImpl viewRootImpl = ViewRootImpl.this;
                    if (msg.arg1 == 0) {
                        hasCapture = false;
                    }
                    viewRootImpl.handleAppVisibility(hasCapture);
                    return;
                case 9:
                    ViewRootImpl.this.handleGetNewSurface();
                    return;
                case 10:
                default:
                    return;
                case 11:
                    KeyEvent event = (KeyEvent) msg.obj;
                    if ((event.getFlags() & 8) != 0) {
                        event = KeyEvent.changeFlags(event, event.getFlags() & -9);
                    }
                    ViewRootImpl.this.enqueueInputEvent(event, null, 1, true);
                    return;
                case 12:
                    ViewRootImpl.this.enqueueInputEvent((KeyEvent) msg.obj, null, 0, true);
                    return;
                case 13:
                    InputMethodManager imm = (InputMethodManager) ViewRootImpl.this.mContext.getSystemService(InputMethodManager.class);
                    if (imm != null) {
                        imm.checkFocus();
                        return;
                    }
                    return;
                case 14:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.mView.onCloseSystemDialogs((String) msg.obj);
                        return;
                    }
                    return;
                case 15:
                case 16:
                    DragEvent event2 = (DragEvent) msg.obj;
                    event2.mLocalState = ViewRootImpl.this.mLocalDragState;
                    ViewRootImpl.this.handleDragEvent(event2);
                    return;
                case 17:
                    ViewRootImpl.this.handleDispatchSystemUiVisibilityChanged((SystemUiVisibilityInfo) msg.obj);
                    return;
                case 18:
                    Configuration config = (Configuration) msg.obj;
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (config.isOtherSeqNewer(ViewRootImpl.this.mLastReportedMergedConfiguration.getMergedConfiguration())) {
                        config = ViewRootImpl.this.mLastReportedMergedConfiguration.getGlobalConfiguration();
                    }
                    ViewRootImpl.this.mPendingMergedConfiguration.setConfiguration(config, ViewRootImpl.this.mLastReportedMergedConfiguration.getOverrideConfiguration());
                    ViewRootImpl viewRootImpl2 = ViewRootImpl.this;
                    viewRootImpl2.performConfigurationChange(viewRootImpl2.mPendingMergedConfiguration, false, -1);
                    return;
                case 19:
                    ViewRootImpl viewRootImpl3 = ViewRootImpl.this;
                    viewRootImpl3.mProcessInputEventsScheduled = false;
                    viewRootImpl3.doProcessInputEvents();
                    return;
                case 20:
                    ViewRootImpl.this.checkViewDroppable((DragEvent) msg.obj);
                    return;
                case 21:
                    ViewRootImpl.this.setAccessibilityFocus(null, null);
                    return;
                case 22:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl viewRootImpl4 = ViewRootImpl.this;
                        viewRootImpl4.invalidateWorld(viewRootImpl4.mView);
                        return;
                    }
                    return;
                case 23:
                    HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
                    if (ViewRootImpl.this.mAdded) {
                        int w = ViewRootImpl.this.mWinFrame.width();
                        int h = ViewRootImpl.this.mWinFrame.height();
                        int l = msg.arg1;
                        int t = msg.arg2;
                        ViewRootImpl.this.mTmpFrame.left = l;
                        ViewRootImpl.this.mTmpFrame.right = l + w;
                        ViewRootImpl.this.mTmpFrame.top = t;
                        ViewRootImpl.this.mTmpFrame.bottom = t + h;
                        ViewRootImpl viewRootImpl5 = ViewRootImpl.this;
                        viewRootImpl5.setFrame(viewRootImpl5.mTmpFrame);
                        ViewRootImpl.this.mPendingBackDropFrame.set(ViewRootImpl.this.mWinFrame);
                        ViewRootImpl viewRootImpl6 = ViewRootImpl.this;
                        viewRootImpl6.maybeHandleWindowMove(viewRootImpl6.mWinFrame);
                        return;
                    }
                    return;
                case 24:
                    ViewRootImpl.this.enqueueInputEvent((InputEvent) msg.obj, null, 32, true);
                    return;
                case 25:
                    ViewRootImpl.this.handleDispatchWindowShown();
                    return;
                case 26:
                    ViewRootImpl.this.handleRequestKeyboardShortcuts((IResultReceiver) msg.obj, msg.arg1);
                    return;
                case 27:
                    ViewRootImpl.this.resetPointerIcon((MotionEvent) msg.obj);
                    return;
                case 28:
                    if (msg.arg1 == 0) {
                        hasCapture = false;
                    }
                    ViewRootImpl.this.handlePointerCaptureChanged(hasCapture);
                    return;
                case 29:
                    ViewRootImpl.this.pendingDrawFinished();
                    return;
                case 30:
                    ViewRootImpl.this.mInsetsController.onStateChanged((InsetsState) msg.obj);
                    return;
                case 31:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    ViewRootImpl.this.mInsetsController.onControlsChanged((InsetsSourceControl[]) args3.arg2);
                    ViewRootImpl.this.mInsetsController.onStateChanged((InsetsState) args3.arg1);
                    return;
                case 32:
                    ViewRootImpl.this.systemGestureExclusionChanged();
                    return;
            }
            HwFrameworkFactory.getHwViewRootImpl().clearDisplayPoint();
            if (ViewRootImpl.this.mAdded) {
                SomeArgs args4 = (SomeArgs) msg.obj;
                int displayId = args4.argi3;
                MergedConfiguration mergedConfiguration = (MergedConfiguration) args4.arg4;
                boolean displayChanged = ViewRootImpl.this.mDisplay.getDisplayId() != displayId;
                boolean configChanged = false;
                if (!ViewRootImpl.this.mLastReportedMergedConfiguration.equals(mergedConfiguration)) {
                    ViewRootImpl viewRootImpl7 = ViewRootImpl.this;
                    if (displayChanged) {
                        i = displayId;
                    }
                    viewRootImpl7.performConfigurationChange(mergedConfiguration, false, i);
                    configChanged = true;
                } else if (displayChanged) {
                    ViewRootImpl viewRootImpl8 = ViewRootImpl.this;
                    viewRootImpl8.onMovedToDisplay(displayId, viewRootImpl8.mLastConfigurationFromResources);
                }
                boolean framesChanged = !ViewRootImpl.this.mWinFrame.equals(args4.arg1) || !ViewRootImpl.this.mPendingOverscanInsets.equals(args4.arg5) || !ViewRootImpl.this.mPendingContentInsets.equals(args4.arg2) || !ViewRootImpl.this.mPendingStableInsets.equals(args4.arg6) || !ViewRootImpl.this.mPendingDisplayCutout.get().equals(args4.arg9) || !ViewRootImpl.this.mPendingVisibleInsets.equals(args4.arg3) || !ViewRootImpl.this.mPendingOutsets.equals(args4.arg7);
                ViewRootImpl.this.setFrame((Rect) args4.arg1);
                ViewRootImpl.this.mPendingOverscanInsets.set((Rect) args4.arg5);
                ViewRootImpl.this.mPendingContentInsets.set((Rect) args4.arg2);
                ViewRootImpl.this.mPendingStableInsets.set((Rect) args4.arg6);
                ViewRootImpl.this.mPendingDisplayCutout.set((DisplayCutout) args4.arg9);
                ViewRootImpl.this.mPendingVisibleInsets.set((Rect) args4.arg3);
                ViewRootImpl.this.mPendingOutsets.set((Rect) args4.arg7);
                ViewRootImpl.this.mPendingBackDropFrame.set((Rect) args4.arg8);
                ViewRootImpl.this.mForceNextWindowRelayout = args4.argi1 != 0;
                ViewRootImpl viewRootImpl9 = ViewRootImpl.this;
                if (args4.argi2 == 0) {
                    hasCapture = false;
                }
                viewRootImpl9.mPendingAlwaysConsumeSystemBars = hasCapture;
                args4.recycle();
                if (msg.what == 5) {
                    ViewRootImpl.this.reportNextDraw();
                }
                if (ViewRootImpl.this.mView != null && (framesChanged || configChanged)) {
                    ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                }
                ViewRootImpl.this.requestLayout();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean ensureTouchMode(boolean inTouchMode) {
        if (this.mAttachInfo.mInTouchMode == inTouchMode) {
            return false;
        }
        try {
            this.mWindowSession.setInTouchMode(inTouchMode);
            return ensureTouchModeLocally(inTouchMode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean ensureTouchModeLocally(boolean inTouchMode) {
        if (this.mAttachInfo.mInTouchMode == inTouchMode) {
            return false;
        }
        View.AttachInfo attachInfo = this.mAttachInfo;
        attachInfo.mInTouchMode = inTouchMode;
        attachInfo.mTreeObserver.dispatchOnTouchModeChanged(inTouchMode);
        return inTouchMode ? enterTouchMode() : leaveTouchMode();
    }

    private boolean enterTouchMode() {
        View focused;
        View view = this.mView;
        if (view == null || !view.hasFocus() || (focused = this.mView.findFocus()) == null || focused.isFocusableInTouchMode()) {
            return false;
        }
        ViewGroup ancestorToTakeFocus = findAncestorToTakeFocusInTouchMode(focused);
        if (ancestorToTakeFocus != null) {
            return ancestorToTakeFocus.requestFocus();
        }
        focused.clearFocusInternal(null, true, false);
        return true;
    }

    private static ViewGroup findAncestorToTakeFocusInTouchMode(View focused) {
        ViewParent parent = focused.getParent();
        while (parent instanceof ViewGroup) {
            ViewGroup vgParent = (ViewGroup) parent;
            if (vgParent.getDescendantFocusability() == 262144 && vgParent.isFocusableInTouchMode()) {
                return vgParent;
            }
            if (vgParent.isRootNamespace()) {
                return null;
            }
            parent = vgParent.getParent();
        }
        return null;
    }

    private boolean leaveTouchMode() {
        View view = this.mView;
        if (view == null) {
            return false;
        }
        if (view.hasFocus()) {
            View focusedView = this.mView.findFocus();
            if (!(focusedView instanceof ViewGroup) || ((ViewGroup) focusedView).getDescendantFocusability() != 262144) {
                return false;
            }
        }
        return this.mView.restoreDefaultFocus();
    }

    /* access modifiers changed from: package-private */
    @RCUnownedThisRef
    public abstract class InputStage {
        protected static final int FINISH_HANDLED = 1;
        protected static final int FINISH_NOT_HANDLED = 2;
        protected static final int FORWARD = 0;
        private final InputStage mNext;

        public InputStage(InputStage next) {
            this.mNext = next;
        }

        public final void deliver(QueuedInputEvent q) {
            if ((q.mFlags & 4) != 0) {
                forward(q);
            } else if (shouldDropInputEvent(q)) {
                finish(q, false);
            } else {
                apply(q, onProcess(q));
            }
        }

        /* access modifiers changed from: protected */
        public void finish(QueuedInputEvent q, boolean handled) {
            q.mFlags |= 4;
            if (handled) {
                q.mFlags |= 8;
            }
            forward(q);
        }

        /* access modifiers changed from: protected */
        public void forward(QueuedInputEvent q) {
            onDeliverToNext(q);
        }

        /* access modifiers changed from: protected */
        public void apply(QueuedInputEvent q, int result) {
            if (result == 0) {
                forward(q);
            } else if (result == 1) {
                finish(q, true);
            } else if (result == 2) {
                finish(q, false);
            } else {
                throw new IllegalArgumentException("Invalid result: " + result);
            }
        }

        /* access modifiers changed from: protected */
        public int onProcess(QueuedInputEvent q) {
            return 0;
        }

        /* access modifiers changed from: protected */
        public void onDeliverToNext(QueuedInputEvent q) {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.deliver(q);
            } else {
                ViewRootImpl.this.finishInputEvent(q);
            }
        }

        /* access modifiers changed from: protected */
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.onWindowFocusChanged(hasWindowFocus);
            }
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.onDetachedFromWindow();
            }
        }

        /* access modifiers changed from: protected */
        public boolean shouldDropInputEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView == null || !ViewRootImpl.this.mAdded) {
                String str = ViewRootImpl.this.mTag;
                Slog.w(str, "Dropping event due to root view being removed: " + q.mEvent);
                return true;
            } else if ((ViewRootImpl.this.mAttachInfo.mHasWindowFocus || q.mEvent.isFromSource(2) || ViewRootImpl.this.isAutofillUiShowing()) && !ViewRootImpl.this.mStopped && ((!ViewRootImpl.this.mIsAmbientMode || q.mEvent.isFromSource(1)) && (!ViewRootImpl.this.mPausedForTransition || isBack(q.mEvent)))) {
                return false;
            } else {
                String str2 = ViewRootImpl.this.mTag;
                Slog.w(str2, "hasFocus:" + ViewRootImpl.this.mAttachInfo.mHasWindowFocus + ", stopped:" + ViewRootImpl.this.mStopped + ", isAmbientMode:" + ViewRootImpl.this.mIsAmbientMode + ", paused:" + ViewRootImpl.this.mPausedForTransition + ", flags:" + q.mFlags);
                if (ViewRootImpl.this.mDisplay != null && HwPCUtils.isValidExtDisplayId(ViewRootImpl.this.mDisplay.getDisplayId()) && (q.mEvent instanceof KeyEvent) && (((KeyEvent) q.mEvent).getFlags() & 4096) != 0) {
                    HwPCUtils.log(ViewRootImpl.this.mTag, "shouldDropInputEvent: no, ext display key event");
                    return false;
                } else if (ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                    q.mEvent.cancel();
                    String str3 = ViewRootImpl.this.mTag;
                    Slog.w(str3, "Cancelling event due to no window focus: " + q.mEvent);
                    return false;
                } else {
                    String str4 = ViewRootImpl.this.mTag;
                    Slog.w(str4, "Dropping event due to no window focus: " + q.mEvent);
                    return true;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(String prefix, PrintWriter writer) {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.dump(prefix, writer);
            }
        }

        private boolean isBack(InputEvent event) {
            if (!(event instanceof KeyEvent) || ((KeyEvent) event).getKeyCode() != 4) {
                return false;
            }
            return true;
        }
    }

    @RCUnownedThisRef
    abstract class AsyncInputStage extends InputStage {
        protected static final int DEFER = 3;
        private QueuedInputEvent mQueueHead;
        private int mQueueLength;
        private QueuedInputEvent mQueueTail;
        private final String mTraceCounter;

        public AsyncInputStage(InputStage next, String traceCounter) {
            super(next);
            this.mTraceCounter = traceCounter;
        }

        /* access modifiers changed from: protected */
        public void defer(QueuedInputEvent q) {
            q.mFlags |= 2;
            enqueue(q);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void forward(QueuedInputEvent q) {
            q.mFlags &= -3;
            QueuedInputEvent curr = this.mQueueHead;
            if (curr == null) {
                super.forward(q);
                return;
            }
            int deviceId = q.mEvent.getDeviceId();
            QueuedInputEvent prev = null;
            boolean blocked = false;
            while (curr != null && curr != q) {
                if (!blocked && deviceId == curr.mEvent.getDeviceId()) {
                    blocked = true;
                }
                prev = curr;
                curr = curr.mNext;
            }
            if (!blocked) {
                if (curr != null) {
                    curr = curr.mNext;
                    dequeue(q, prev);
                }
                super.forward(q);
                while (curr != null) {
                    if (deviceId != curr.mEvent.getDeviceId()) {
                        prev = curr;
                        curr = curr.mNext;
                    } else if ((curr.mFlags & 2) == 0) {
                        QueuedInputEvent next = curr.mNext;
                        dequeue(curr, prev);
                        super.forward(curr);
                        curr = next;
                    } else {
                        return;
                    }
                }
            } else if (curr == null) {
                enqueue(q);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void apply(QueuedInputEvent q, int result) {
            if (result == 3) {
                defer(q);
            } else {
                super.apply(q, result);
            }
        }

        private void enqueue(QueuedInputEvent q) {
            QueuedInputEvent queuedInputEvent = this.mQueueTail;
            if (queuedInputEvent == null) {
                this.mQueueHead = q;
                this.mQueueTail = q;
            } else {
                queuedInputEvent.mNext = q;
                this.mQueueTail = q;
            }
            this.mQueueLength++;
            Trace.traceCounter(4, this.mTraceCounter, this.mQueueLength);
        }

        private void dequeue(QueuedInputEvent q, QueuedInputEvent prev) {
            if (prev == null) {
                this.mQueueHead = q.mNext;
            } else {
                prev.mNext = q.mNext;
            }
            if (this.mQueueTail == q) {
                this.mQueueTail = prev;
            }
            q.mNext = null;
            this.mQueueLength--;
            Trace.traceCounter(4, this.mTraceCounter, this.mQueueLength);
        }

        /* access modifiers changed from: package-private */
        @Override // android.view.ViewRootImpl.InputStage
        public void dump(String prefix, PrintWriter writer) {
            writer.print(prefix);
            writer.print(getClass().getName());
            writer.print(": mQueueLength=");
            writer.println(this.mQueueLength);
            super.dump(prefix, writer);
        }
    }

    @RCUnownedThisRef
    final class NativePreImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePreImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mInputQueue == null || !(q.mEvent instanceof KeyEvent)) {
                return 0;
            }
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, true, this);
            return 3;
        }

        @Override // android.view.InputQueue.FinishedInputEventCallback
        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    @RCUnownedThisRef
    final class ViewPreImeInputStage extends InputStage {
        public ViewPreImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView.dispatchKeyEventPreIme((KeyEvent) q.mEvent)) {
                return 1;
            }
            return 0;
        }
    }

    @RCUnownedThisRef
    final class ImeInputStage extends AsyncInputStage implements InputMethodManager.FinishedInputEventCallback {
        public ImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            InputMethodManager imm;
            if (!ViewRootImpl.this.mLastWasImTarget || ViewRootImpl.this.isInLocalFocusMode() || (imm = (InputMethodManager) ViewRootImpl.this.mContext.getSystemService(InputMethodManager.class)) == null) {
                return 0;
            }
            int result = imm.dispatchInputEvent(q.mEvent, q, this, ViewRootImpl.this.mHandler);
            if (result == 1) {
                return 1;
            }
            if (result == 0) {
                return 0;
            }
            return 3;
        }

        @Override // android.view.inputmethod.InputMethodManager.FinishedInputEventCallback
        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    @RCUnownedThisRef
    final class EarlyPostImeInputStage extends InputStage {
        public EarlyPostImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            if (q.mEvent instanceof MotionEvent) {
                return processMotionEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = (KeyEvent) q.mEvent;
            if (ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.handleTooltipKey(event);
            }
            if (ViewRootImpl.this.checkForLeavingTouchModeAndConsume(event)) {
                return 1;
            }
            ViewRootImpl.this.mFallbackEventHandler.preDispatchKeyEvent(event);
            return 0;
        }

        private int processMotionEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if (event.isFromSource(2)) {
                return processPointerEvent(q);
            }
            int action = event.getActionMasked();
            if ((action == 0 || action == 8) && event.isFromSource(8)) {
                ViewRootImpl.this.ensureTouchMode(false);
            }
            return 0;
        }

        private int processPointerEvent(QueuedInputEvent q) {
            AutofillManager afm;
            MotionEvent event = (MotionEvent) q.mEvent;
            if (ViewRootImpl.this.mDisplay != null) {
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                if (viewRootImpl.isValidExtDisplayId(viewRootImpl.mDisplay.getDisplayId())) {
                    event.setOffset(ViewRootImpl.this.mOffset);
                }
            }
            if (ViewRootImpl.this.mTranslator != null) {
                ViewRootImpl.this.mTranslator.translateEventInScreenToAppWindow(event);
            }
            HwFrameworkFactory.getHwApsImpl().processApsPointerEvent(ViewRootImpl.this.mContext, ViewRootImpl.this.mContext.getPackageName(), ViewRootImpl.this.mView.getResources().getDisplayMetrics().widthPixels, Process.myPid(), event);
            int action = event.getAction();
            if (action == 0 || action == 8) {
                if (ViewRootImpl.this.mDisplay != null && !event.isFromSource(4098)) {
                    ViewRootImpl viewRootImpl2 = ViewRootImpl.this;
                    if (viewRootImpl2.shouldKeepTouchMode(viewRootImpl2.mDisplay.getDisplayId())) {
                        HwPCUtils.log(ViewRootImpl.TAG, "ensureTouchMode true");
                        ViewRootImpl.this.ensureTouchMode(true);
                    }
                }
                ViewRootImpl.this.ensureTouchMode(event.isFromSource(4098));
            }
            if (action == 0 && (afm = ViewRootImpl.this.getAutofillManager()) != null) {
                afm.requestHideFillUi();
            }
            if (action == 0 && ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.hideTooltip();
            }
            if (ViewRootImpl.this.mCurScrollY != 0) {
                event.offsetLocation(0.0f, (float) ViewRootImpl.this.mCurScrollY);
            }
            if (!event.isTouchEvent()) {
                return 0;
            }
            ViewRootImpl.this.mLastTouchPoint.x = event.getRawX();
            ViewRootImpl.this.mLastTouchPoint.y = event.getRawY();
            ViewRootImpl.this.mLastTouchSource = event.getSource();
            return 0;
        }
    }

    @RCUnownedThisRef
    final class NativePostImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePostImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mInputQueue == null) {
                return 0;
            }
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, false, this);
            return 3;
        }

        @Override // android.view.InputQueue.FinishedInputEventCallback
        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    @RCUnownedThisRef
    final class ViewPostImeInputStage extends InputStage {
        public ViewPostImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            int source = q.mEvent.getSource();
            if ((source & 2) != 0) {
                return processPointerEvent(q);
            }
            if ((source & 4) != 0) {
                return processTrackballEvent(q);
            }
            return processGenericMotionEvent(q);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onDeliverToNext(QueuedInputEvent q) {
            if (ViewRootImpl.this.mUnbufferedInputDispatch && (q.mEvent instanceof MotionEvent) && ((MotionEvent) q.mEvent).isTouchEvent() && ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                viewRootImpl.mUnbufferedInputDispatch = false;
                viewRootImpl.scheduleConsumeBatchedInput();
            }
            super.onDeliverToNext(q);
        }

        private boolean performFocusNavigation(KeyEvent event) {
            int direction = 0;
            int keyCode = event.getKeyCode();
            if (keyCode != 61) {
                switch (keyCode) {
                    case 19:
                        if (event.hasNoModifiers()) {
                            direction = 33;
                            break;
                        }
                        break;
                    case 20:
                        if (event.hasNoModifiers()) {
                            direction = 130;
                            break;
                        }
                        break;
                    case 21:
                        if (event.hasNoModifiers()) {
                            direction = 17;
                            break;
                        }
                        break;
                    case 22:
                        if (event.hasNoModifiers()) {
                            direction = 66;
                            break;
                        }
                        break;
                }
            } else if (event.hasNoModifiers()) {
                direction = 2;
            } else if (event.hasModifiers(1)) {
                direction = 1;
            }
            if (direction == 0) {
                return false;
            }
            View focused = ViewRootImpl.this.mView.findFocus();
            if (focused != null) {
                View v = focused.focusSearch(direction);
                if (!(v == null || v == focused)) {
                    focused.getFocusedRect(ViewRootImpl.this.mTempRect);
                    if (ViewRootImpl.this.mView instanceof ViewGroup) {
                        ((ViewGroup) ViewRootImpl.this.mView).offsetDescendantRectToMyCoords(focused, ViewRootImpl.this.mTempRect);
                        ((ViewGroup) ViewRootImpl.this.mView).offsetRectIntoDescendantCoords(v, ViewRootImpl.this.mTempRect);
                    }
                    if (v.requestFocus(direction, ViewRootImpl.this.mTempRect)) {
                        ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                        return true;
                    }
                }
                if (ViewRootImpl.this.mView.dispatchUnhandledMove(focused, direction)) {
                    return true;
                }
                return false;
            } else if (ViewRootImpl.this.mView.restoreDefaultFocus()) {
                return true;
            } else {
                return false;
            }
        }

        private boolean performKeyboardGroupNavigation(int direction) {
            View cluster;
            View focused = ViewRootImpl.this.mView.findFocus();
            if (focused == null && ViewRootImpl.this.mView.restoreDefaultFocus()) {
                return true;
            }
            if (focused == null) {
                cluster = ViewRootImpl.this.keyboardNavigationClusterSearch(null, direction);
            } else {
                cluster = focused.keyboardNavigationClusterSearch(null, direction);
            }
            int realDirection = direction;
            if (direction == 2 || direction == 1) {
                realDirection = 130;
            }
            if (cluster != null && cluster.isRootNamespace()) {
                if (cluster.restoreFocusNotInCluster()) {
                    ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                    return true;
                }
                cluster = ViewRootImpl.this.keyboardNavigationClusterSearch(null, direction);
            }
            if (cluster == null || !cluster.restoreFocusInCluster(realDirection)) {
                return false;
            }
            ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            return true;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = (KeyEvent) q.mEvent;
            if (ViewRootImpl.this.mUnhandledKeyManager.preViewDispatch(event) || ViewRootImpl.this.mView.dispatchKeyEvent(event)) {
                return 1;
            }
            if (shouldDropInputEvent(q)) {
                return 2;
            }
            if (ViewRootImpl.this.mUnhandledKeyManager.dispatch(ViewRootImpl.this.mView, event)) {
                return 1;
            }
            int groupNavigationDirection = 0;
            if (event.getAction() == 0 && event.getKeyCode() == 61) {
                if (KeyEvent.metaStateHasModifiers(event.getMetaState(), 65536)) {
                    groupNavigationDirection = 2;
                } else if (KeyEvent.metaStateHasModifiers(event.getMetaState(), 65537)) {
                    groupNavigationDirection = 1;
                }
            }
            if (event.getAction() == 0 && !KeyEvent.metaStateHasNoModifiers(event.getMetaState()) && event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(event.getKeyCode()) && groupNavigationDirection == 0) {
                if (ViewRootImpl.this.mView.dispatchKeyShortcutEvent(event)) {
                    return 1;
                }
                if (shouldDropInputEvent(q)) {
                    return 2;
                }
            }
            if (ViewRootImpl.this.mFallbackEventHandler.dispatchKeyEvent(event)) {
                return 1;
            }
            if (shouldDropInputEvent(q)) {
                return 2;
            }
            if (event.getAction() != 0) {
                return 0;
            }
            if (groupNavigationDirection != 0) {
                if (performKeyboardGroupNavigation(groupNavigationDirection)) {
                    return 1;
                }
                return 0;
            } else if (performFocusNavigation(event)) {
                return 1;
            } else {
                return 0;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x006b  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:22:? A[RETURN, SYNTHETIC] */
        private int processPointerEvent(QueuedInputEvent q) {
            boolean handled;
            ViewRootImpl viewRootImpl;
            MotionEvent event = (MotionEvent) q.mEvent;
            ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested = false;
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = true;
            if (ViewRootImpl.this.mDisplay != null) {
                ViewRootImpl viewRootImpl2 = ViewRootImpl.this;
                if (viewRootImpl2.isValidExtDisplayId(viewRootImpl2.mDisplay.getDisplayId()) && ViewRootImpl.this.mHwViewRootImpl.interceptMotionEvent(ViewRootImpl.this.mView, event)) {
                    handled = true;
                    if (!handled && ViewRootImpl.this.mView != null) {
                        handled = ViewRootImpl.this.mView.dispatchPointerEvent(event);
                    }
                    maybeUpdatePointerIcon(event);
                    ViewRootImpl.this.maybeUpdateTooltip(event);
                    ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = false;
                    if (ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested && !ViewRootImpl.this.mUnbufferedInputDispatch) {
                        viewRootImpl = ViewRootImpl.this;
                        viewRootImpl.mUnbufferedInputDispatch = true;
                        if (viewRootImpl.mConsumeBatchedInputScheduled) {
                            ViewRootImpl.this.scheduleConsumeBatchedInputImmediately();
                        }
                    }
                    if (!handled) {
                        return 1;
                    }
                    return 0;
                }
            }
            handled = false;
            handled = ViewRootImpl.this.mView.dispatchPointerEvent(event);
            maybeUpdatePointerIcon(event);
            ViewRootImpl.this.maybeUpdateTooltip(event);
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = false;
            viewRootImpl = ViewRootImpl.this;
            viewRootImpl.mUnbufferedInputDispatch = true;
            if (viewRootImpl.mConsumeBatchedInputScheduled) {
            }
            if (!handled) {
            }
        }

        private void maybeUpdatePointerIcon(MotionEvent event) {
            if (event.getPointerCount() == 1 && event.isFromSource(8194)) {
                if (event.getActionMasked() == 9 || event.getActionMasked() == 10) {
                    ViewRootImpl.this.mPointerIconType = 1;
                }
                if (event.getActionMasked() != 10 && !ViewRootImpl.this.updatePointerIcon(event) && event.getActionMasked() == 7) {
                    ViewRootImpl.this.mPointerIconType = 1;
                }
            }
        }

        private int processTrackballEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if ((!event.isFromSource(InputDevice.SOURCE_MOUSE_RELATIVE) || (ViewRootImpl.this.hasPointerCapture() && !ViewRootImpl.this.mView.dispatchCapturedPointerEvent(event))) && !ViewRootImpl.this.mView.dispatchTrackballEvent(event)) {
                return 0;
            }
            return 1;
        }

        private int processGenericMotionEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if ((!event.isFromSource(InputDevice.SOURCE_TOUCHPAD) || !ViewRootImpl.this.hasPointerCapture() || !ViewRootImpl.this.mView.dispatchCapturedPointerEvent(event)) && !ViewRootImpl.this.mView.dispatchGenericMotionEvent(event)) {
                return 0;
            }
            return 1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetPointerIcon(MotionEvent event) {
        this.mPointerIconType = 1;
        updatePointerIcon(event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updatePointerIcon(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        View view = this.mView;
        if (view == null) {
            Slog.d(this.mTag, "updatePointerIcon called after view was removed");
            return false;
        } else if (x < 0.0f || x >= ((float) view.getWidth()) || y < 0.0f || y >= ((float) this.mView.getHeight())) {
            Slog.d(this.mTag, "updatePointerIcon called with position out of bounds");
            return false;
        } else {
            PointerIcon pointerIcon = this.mView.onResolvePointerIcon(event, 0);
            int pointerType = pointerIcon != null ? pointerIcon.getType() : 1000;
            if (this.mPointerIconType != pointerType) {
                this.mPointerIconType = pointerType;
                this.mCustomPointerIcon = null;
                if (this.mPointerIconType != -1) {
                    InputManager.getInstance().setPointerIconType(pointerType);
                    return true;
                }
            }
            if (this.mPointerIconType == -1 && !pointerIcon.equals(this.mCustomPointerIcon)) {
                this.mCustomPointerIcon = pointerIcon;
                InputManager.getInstance().setCustomPointerIcon(this.mCustomPointerIcon);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeUpdateTooltip(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            int action = event.getActionMasked();
            if (action == 9 || action == 7 || action == 10) {
                AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
                if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
                    View view = this.mView;
                    if (view == null) {
                        Slog.d(this.mTag, "maybeUpdateTooltip called after view was removed");
                    } else {
                        view.dispatchTooltipHoverEvent(event);
                    }
                }
            }
        }
    }

    @RCUnownedThisRef
    final class SyntheticInputStage extends InputStage {
        private final SyntheticJoystickHandler mJoystick = new SyntheticJoystickHandler();
        private final SyntheticKeyboardHandler mKeyboard = new SyntheticKeyboardHandler();
        private final SyntheticTouchNavigationHandler mTouchNavigation = new SyntheticTouchNavigationHandler();
        private final SyntheticTrackballHandler mTrackball = new SyntheticTrackballHandler();

        public SyntheticInputStage() {
            super(null);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            q.mFlags |= 16;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent event = (MotionEvent) q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.process(event);
                    return 1;
                } else if ((source & 16) != 0) {
                    this.mJoystick.process(event);
                    return 1;
                } else if ((source & 2097152) != 2097152) {
                    return 0;
                } else {
                    this.mTouchNavigation.process(event);
                    return 1;
                }
            } else if ((q.mFlags & 32) == 0) {
                return 0;
            } else {
                this.mKeyboard.process((KeyEvent) q.mEvent);
                return 1;
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onDeliverToNext(QueuedInputEvent q) {
            if ((q.mFlags & 16) == 0 && (q.mEvent instanceof MotionEvent)) {
                MotionEvent event = (MotionEvent) q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.cancel();
                } else if ((source & 16) != 0) {
                    this.mJoystick.cancel();
                } else if ((source & 2097152) == 2097152) {
                    this.mTouchNavigation.cancel(event);
                }
            }
            super.onDeliverToNext(q);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            if (!hasWindowFocus) {
                this.mJoystick.cancel();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onDetachedFromWindow() {
            this.mJoystick.cancel();
        }
    }

    @RCUnownedThisRef
    final class SyntheticTrackballHandler {
        private long mLastTime;
        private final TrackballAxis mX = new TrackballAxis();
        private final TrackballAxis mY = new TrackballAxis();

        SyntheticTrackballHandler() {
        }

        public void process(MotionEvent event) {
            long curTime;
            int i;
            float accel;
            int keycode;
            long curTime2;
            int keycode2;
            int keycode3;
            long curTime3 = SystemClock.uptimeMillis();
            if (this.mLastTime + 250 < curTime3) {
                this.mX.reset(0);
                this.mY.reset(0);
                this.mLastTime = curTime3;
            }
            int action = event.getAction();
            int metaState = event.getMetaState();
            if (action == 0) {
                this.mX.reset(2);
                this.mY.reset(2);
                curTime = curTime3;
                i = 2;
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime3, curTime3, 0, 23, 0, metaState, -1, 0, 1024, 257));
            } else if (action != 1) {
                curTime = curTime3;
                i = 2;
            } else {
                this.mX.reset(2);
                this.mY.reset(2);
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime3, curTime3, 1, 23, 0, metaState, -1, 0, 1024, 257));
                curTime = curTime3;
                i = 2;
            }
            float xOff = this.mX.collect(event.getX(), event.getEventTime(), "X");
            float yOff = this.mY.collect(event.getY(), event.getEventTime(), "Y");
            int movement = 0;
            if (xOff > yOff) {
                movement = this.mX.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode3 = 22;
                    } else {
                        keycode3 = 21;
                    }
                    float accel2 = this.mX.acceleration;
                    this.mY.reset(i);
                    keycode = keycode3;
                    accel = accel2;
                } else {
                    keycode = 0;
                    accel = 1.0f;
                }
            } else if (yOff > 0.0f) {
                movement = this.mY.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode2 = 20;
                    } else {
                        keycode2 = 19;
                    }
                    float accel3 = this.mY.acceleration;
                    this.mX.reset(i);
                    keycode = keycode2;
                    accel = accel3;
                } else {
                    keycode = 0;
                    accel = 1.0f;
                }
            } else {
                keycode = 0;
                accel = 1.0f;
            }
            if (keycode != 0) {
                if (movement < 0) {
                    movement = -movement;
                }
                int accelMovement = (int) (((float) movement) * accel);
                if (accelMovement > movement) {
                    int movement2 = movement - 1;
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 2, keycode, accelMovement - movement2, metaState, -1, 0, 1024, 257));
                    curTime2 = curTime;
                    movement = movement2;
                } else {
                    curTime2 = curTime;
                }
                while (movement > 0) {
                    movement--;
                    curTime2 = SystemClock.uptimeMillis();
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime2, curTime2, 0, keycode, 0, metaState, -1, 0, 1024, 257));
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime2, curTime2, 1, keycode, 0, metaState, -1, 0, 1024, 257));
                }
                this.mLastTime = curTime2;
            }
        }

        public void cancel() {
            this.mLastTime = -2147483648L;
            if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mAdded) {
                ViewRootImpl.this.ensureTouchMode(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class TrackballAxis {
        static final float ACCEL_MOVE_SCALING_FACTOR = 0.025f;
        static final long FAST_MOVE_TIME = 150;
        static final float FIRST_MOVEMENT_THRESHOLD = 0.5f;
        static final float MAX_ACCELERATION = 20.0f;
        static final float SECOND_CUMULATIVE_MOVEMENT_THRESHOLD = 2.0f;
        static final float SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD = 1.0f;
        float acceleration = 1.0f;
        int dir;
        long lastMoveTime = 0;
        int nonAccelMovement;
        float position;
        int step;

        TrackballAxis() {
        }

        /* access modifiers changed from: package-private */
        public void reset(int _step) {
            this.position = 0.0f;
            this.acceleration = 1.0f;
            this.lastMoveTime = 0;
            this.step = _step;
            this.dir = 0;
        }

        /* access modifiers changed from: package-private */
        public float collect(float off, long time, String axis) {
            long normTime;
            float f = 1.0f;
            if (off > 0.0f) {
                normTime = (long) (150.0f * off);
                if (this.dir < 0) {
                    this.position = 0.0f;
                    this.step = 0;
                    this.acceleration = 1.0f;
                    this.lastMoveTime = 0;
                }
                this.dir = 1;
            } else if (off < 0.0f) {
                normTime = (long) ((-off) * 150.0f);
                if (this.dir > 0) {
                    this.position = 0.0f;
                    this.step = 0;
                    this.acceleration = 1.0f;
                    this.lastMoveTime = 0;
                }
                this.dir = -1;
            } else {
                normTime = 0;
            }
            if (normTime > 0) {
                long delta = time - this.lastMoveTime;
                this.lastMoveTime = time;
                float acc = this.acceleration;
                if (delta < normTime) {
                    float scale = ((float) (normTime - delta)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc *= scale;
                    }
                    float f2 = MAX_ACCELERATION;
                    if (acc < MAX_ACCELERATION) {
                        f2 = acc;
                    }
                    this.acceleration = f2;
                } else {
                    float scale2 = ((float) (delta - normTime)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale2 > 1.0f) {
                        acc /= scale2;
                    }
                    if (acc > 1.0f) {
                        f = acc;
                    }
                    this.acceleration = f;
                }
            }
            this.position += off;
            return Math.abs(this.position);
        }

        /* access modifiers changed from: package-private */
        public int generate() {
            int movement = 0;
            this.nonAccelMovement = 0;
            while (true) {
                int dir2 = this.position >= 0.0f ? 1 : -1;
                int i = this.step;
                if (i != 0) {
                    if (i != 1) {
                        if (Math.abs(this.position) < 1.0f) {
                            return movement;
                        }
                        movement += dir2;
                        this.position -= ((float) dir2) * 1.0f;
                        float acc = this.acceleration * 1.1f;
                        this.acceleration = acc < MAX_ACCELERATION ? acc : this.acceleration;
                    } else if (Math.abs(this.position) < SECOND_CUMULATIVE_MOVEMENT_THRESHOLD) {
                        return movement;
                    } else {
                        movement += dir2;
                        this.nonAccelMovement += dir2;
                        this.position -= ((float) dir2) * SECOND_CUMULATIVE_MOVEMENT_THRESHOLD;
                        this.step = 2;
                    }
                } else if (Math.abs(this.position) < FIRST_MOVEMENT_THRESHOLD) {
                    return movement;
                } else {
                    movement += dir2;
                    this.nonAccelMovement += dir2;
                    this.step = 1;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @RCUnownedThisRef
    public final class SyntheticJoystickHandler extends Handler {
        private static final int MSG_ENQUEUE_X_AXIS_KEY_REPEAT = 1;
        private static final int MSG_ENQUEUE_Y_AXIS_KEY_REPEAT = 2;
        private final SparseArray<KeyEvent> mDeviceKeyEvents = new SparseArray<>();
        private final JoystickAxesState mJoystickAxesState = new JoystickAxesState();

        public SyntheticJoystickHandler() {
            super(true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if ((i == 1 || i == 2) && ViewRootImpl.this.mAttachInfo.mHasWindowFocus) {
                KeyEvent oldEvent = (KeyEvent) msg.obj;
                KeyEvent e = KeyEvent.changeTimeRepeat(oldEvent, SystemClock.uptimeMillis(), oldEvent.getRepeatCount() + 1);
                ViewRootImpl.this.enqueueInputEvent(e);
                Message m = obtainMessage(msg.what, e);
                m.setAsynchronous(true);
                sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatDelay());
            }
        }

        public void process(MotionEvent event) {
            int actionMasked = event.getActionMasked();
            if (actionMasked == 2) {
                update(event);
            } else if (actionMasked != 3) {
                String str = ViewRootImpl.this.mTag;
                Log.w(str, "Unexpected action: " + event.getActionMasked());
            } else {
                cancel();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancel() {
            removeMessages(1);
            removeMessages(2);
            for (int i = 0; i < this.mDeviceKeyEvents.size(); i++) {
                KeyEvent keyEvent = this.mDeviceKeyEvents.valueAt(i);
                if (keyEvent != null) {
                    ViewRootImpl.this.enqueueInputEvent(KeyEvent.changeTimeRepeat(keyEvent, SystemClock.uptimeMillis(), 0));
                }
            }
            this.mDeviceKeyEvents.clear();
            this.mJoystickAxesState.resetState();
        }

        private void update(MotionEvent event) {
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; h++) {
                long time = event.getHistoricalEventTime(h);
                this.mJoystickAxesState.updateStateForAxis(event, time, 0, event.getHistoricalAxisValue(0, 0, h));
                this.mJoystickAxesState.updateStateForAxis(event, time, 1, event.getHistoricalAxisValue(1, 0, h));
                this.mJoystickAxesState.updateStateForAxis(event, time, 15, event.getHistoricalAxisValue(15, 0, h));
                this.mJoystickAxesState.updateStateForAxis(event, time, 16, event.getHistoricalAxisValue(16, 0, h));
            }
            long time2 = event.getEventTime();
            this.mJoystickAxesState.updateStateForAxis(event, time2, 0, event.getAxisValue(0));
            this.mJoystickAxesState.updateStateForAxis(event, time2, 1, event.getAxisValue(1));
            this.mJoystickAxesState.updateStateForAxis(event, time2, 15, event.getAxisValue(15));
            this.mJoystickAxesState.updateStateForAxis(event, time2, 16, event.getAxisValue(16));
        }

        /* access modifiers changed from: package-private */
        @RCUnownedThisRef
        public final class JoystickAxesState {
            private static final int STATE_DOWN_OR_RIGHT = 1;
            private static final int STATE_NEUTRAL = 0;
            private static final int STATE_UP_OR_LEFT = -1;
            final int[] mAxisStatesHat = {0, 0};
            final int[] mAxisStatesStick = {0, 0};

            JoystickAxesState() {
            }

            /* access modifiers changed from: package-private */
            public void resetState() {
                int[] iArr = this.mAxisStatesHat;
                iArr[0] = 0;
                iArr[1] = 0;
                int[] iArr2 = this.mAxisStatesStick;
                iArr2[0] = 0;
                iArr2[1] = 0;
            }

            /* access modifiers changed from: package-private */
            public void updateStateForAxis(MotionEvent event, long time, int axis, float value) {
                int repeatMessage;
                int axisStateIndex;
                int currentState;
                if (isXAxis(axis)) {
                    axisStateIndex = 0;
                    repeatMessage = 1;
                } else if (isYAxis(axis)) {
                    axisStateIndex = 1;
                    repeatMessage = 2;
                } else {
                    String str = ViewRootImpl.this.mTag;
                    Log.e(str, "Unexpected axis " + axis + " in updateStateForAxis!");
                    return;
                }
                int newState = joystickAxisValueToState(value);
                if (axis == 0 || axis == 1) {
                    currentState = this.mAxisStatesStick[axisStateIndex];
                } else {
                    currentState = this.mAxisStatesHat[axisStateIndex];
                }
                if (currentState != newState) {
                    int metaState = event.getMetaState();
                    int deviceId = event.getDeviceId();
                    int source = event.getSource();
                    if (currentState == 1 || currentState == -1) {
                        int keyCode = joystickAxisAndStateToKeycode(axis, currentState);
                        if (keyCode != 0) {
                            ViewRootImpl.this.enqueueInputEvent(new KeyEvent(time, time, 1, keyCode, 0, metaState, deviceId, 0, 1024, source));
                            deviceId = deviceId;
                            SyntheticJoystickHandler.this.mDeviceKeyEvents.put(deviceId, null);
                        }
                        SyntheticJoystickHandler.this.removeMessages(repeatMessage);
                    }
                    if (newState == 1 || newState == -1) {
                        int keyCode2 = joystickAxisAndStateToKeycode(axis, newState);
                        if (keyCode2 != 0) {
                            KeyEvent keyEvent = new KeyEvent(time, time, 0, keyCode2, 0, metaState, deviceId, 0, 1024, source);
                            ViewRootImpl.this.enqueueInputEvent(keyEvent);
                            Message m = SyntheticJoystickHandler.this.obtainMessage(repeatMessage, keyEvent);
                            m.setAsynchronous(true);
                            SyntheticJoystickHandler.this.sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                            SyntheticJoystickHandler.this.mDeviceKeyEvents.put(deviceId, new KeyEvent(time, time, 1, keyCode2, 0, metaState, deviceId, 0, 1056, source));
                        }
                    }
                    if (axis == 0 || axis == 1) {
                        this.mAxisStatesStick[axisStateIndex] = newState;
                    } else {
                        this.mAxisStatesHat[axisStateIndex] = newState;
                    }
                }
            }

            private boolean isXAxis(int axis) {
                return axis == 0 || axis == 15;
            }

            private boolean isYAxis(int axis) {
                return axis == 1 || axis == 16;
            }

            private int joystickAxisAndStateToKeycode(int axis, int state) {
                if (isXAxis(axis) && state == -1) {
                    return 21;
                }
                if (isXAxis(axis) && state == 1) {
                    return 22;
                }
                if (isYAxis(axis) && state == -1) {
                    return 19;
                }
                if (isYAxis(axis) && state == 1) {
                    return 20;
                }
                String str = ViewRootImpl.this.mTag;
                Log.e(str, "Unknown axis " + axis + " or direction " + state);
                return 0;
            }

            private int joystickAxisValueToState(float value) {
                if (value >= 0.5f) {
                    return 1;
                }
                if (value <= -0.5f) {
                    return -1;
                }
                return 0;
            }
        }
    }

    @RCUnownedThisRef
    final class SyntheticTouchNavigationHandler extends Handler {
        private static final float DEFAULT_HEIGHT_MILLIMETERS = 48.0f;
        private static final float DEFAULT_WIDTH_MILLIMETERS = 48.0f;
        private static final float FLING_TICK_DECAY = 0.8f;
        private static final boolean LOCAL_DEBUG = false;
        private static final String LOCAL_TAG = "SyntheticTouchNavigationHandler";
        private static final float MAX_FLING_VELOCITY_TICKS_PER_SECOND = 20.0f;
        private static final float MIN_FLING_VELOCITY_TICKS_PER_SECOND = 6.0f;
        private static final int TICK_DISTANCE_MILLIMETERS = 12;
        private float mAccumulatedX;
        private float mAccumulatedY;
        private int mActivePointerId = -1;
        private float mConfigMaxFlingVelocity;
        private float mConfigMinFlingVelocity;
        private float mConfigTickDistance;
        private boolean mConsumedMovement;
        private int mCurrentDeviceId = -1;
        private boolean mCurrentDeviceSupported;
        private int mCurrentSource;
        private final Runnable mFlingRunnable = new Runnable() {
            /* class android.view.ViewRootImpl.SyntheticTouchNavigationHandler.AnonymousClass1 */

            @Override // java.lang.Runnable
            @RCUnownedThisRef
            public void run() {
                long time = SystemClock.uptimeMillis();
                SyntheticTouchNavigationHandler syntheticTouchNavigationHandler = SyntheticTouchNavigationHandler.this;
                syntheticTouchNavigationHandler.sendKeyDownOrRepeat(time, syntheticTouchNavigationHandler.mPendingKeyCode, SyntheticTouchNavigationHandler.this.mPendingKeyMetaState);
                SyntheticTouchNavigationHandler.access$3432(SyntheticTouchNavigationHandler.this, SyntheticTouchNavigationHandler.FLING_TICK_DECAY);
                if (!SyntheticTouchNavigationHandler.this.postFling(time)) {
                    SyntheticTouchNavigationHandler.this.mFlinging = false;
                    SyntheticTouchNavigationHandler.this.finishKeys(time);
                }
            }
        };
        private float mFlingVelocity;
        private boolean mFlinging;
        private float mLastX;
        private float mLastY;
        private int mPendingKeyCode = 0;
        private long mPendingKeyDownTime;
        private int mPendingKeyMetaState;
        private int mPendingKeyRepeatCount;
        private float mStartX;
        private float mStartY;
        private VelocityTracker mVelocityTracker;

        static /* synthetic */ float access$3432(SyntheticTouchNavigationHandler x0, float x1) {
            float f = x0.mFlingVelocity * x1;
            x0.mFlingVelocity = f;
            return f;
        }

        public SyntheticTouchNavigationHandler() {
            super(true);
        }

        /* JADX INFO: Multiple debug info for r8v1 boolean: [D('caughtFling' boolean), D('x' float)] */
        public void process(MotionEvent event) {
            long time = event.getEventTime();
            int deviceId = event.getDeviceId();
            int source = event.getSource();
            if (!(this.mCurrentDeviceId == deviceId && this.mCurrentSource == source)) {
                finishKeys(time);
                finishTracking(time);
                this.mCurrentDeviceId = deviceId;
                this.mCurrentSource = source;
                this.mCurrentDeviceSupported = false;
                InputDevice device = event.getDevice();
                if (device != null) {
                    InputDevice.MotionRange xRange = device.getMotionRange(0);
                    InputDevice.MotionRange yRange = device.getMotionRange(1);
                    if (!(xRange == null || yRange == null)) {
                        this.mCurrentDeviceSupported = true;
                        float xRes = xRange.getResolution();
                        if (xRes <= 0.0f) {
                            xRes = xRange.getRange() / 48.0f;
                        }
                        float yRes = yRange.getResolution();
                        if (yRes <= 0.0f) {
                            yRes = yRange.getRange() / 48.0f;
                        }
                        this.mConfigTickDistance = 12.0f * (xRes + yRes) * 0.5f;
                        float f = this.mConfigTickDistance;
                        this.mConfigMinFlingVelocity = MIN_FLING_VELOCITY_TICKS_PER_SECOND * f;
                        this.mConfigMaxFlingVelocity = f * MAX_FLING_VELOCITY_TICKS_PER_SECOND;
                    }
                }
            }
            if (this.mCurrentDeviceSupported) {
                int action = event.getActionMasked();
                if (action == 0) {
                    boolean caughtFling = this.mFlinging;
                    finishKeys(time);
                    finishTracking(time);
                    this.mActivePointerId = event.getPointerId(0);
                    this.mVelocityTracker = VelocityTracker.obtain();
                    this.mVelocityTracker.addMovement(event);
                    this.mStartX = event.getX();
                    this.mStartY = event.getY();
                    this.mLastX = this.mStartX;
                    this.mLastY = this.mStartY;
                    this.mAccumulatedX = 0.0f;
                    this.mAccumulatedY = 0.0f;
                    this.mConsumedMovement = caughtFling;
                } else if (action == 1 || action == 2) {
                    int i = this.mActivePointerId;
                    if (i >= 0) {
                        int index = event.findPointerIndex(i);
                        if (index < 0) {
                            finishKeys(time);
                            finishTracking(time);
                            return;
                        }
                        this.mVelocityTracker.addMovement(event);
                        float x = event.getX(index);
                        float y = event.getY(index);
                        this.mAccumulatedX += x - this.mLastX;
                        this.mAccumulatedY += y - this.mLastY;
                        this.mLastX = x;
                        this.mLastY = y;
                        consumeAccumulatedMovement(time, event.getMetaState());
                        if (action == 1) {
                            if (this.mConsumedMovement && this.mPendingKeyCode != 0) {
                                this.mVelocityTracker.computeCurrentVelocity(1000, this.mConfigMaxFlingVelocity);
                                if (!startFling(time, this.mVelocityTracker.getXVelocity(this.mActivePointerId), this.mVelocityTracker.getYVelocity(this.mActivePointerId))) {
                                    finishKeys(time);
                                }
                            }
                            finishTracking(time);
                        }
                    }
                } else if (action == 3) {
                    finishKeys(time);
                    finishTracking(time);
                }
            }
        }

        public void cancel(MotionEvent event) {
            if (this.mCurrentDeviceId == event.getDeviceId() && this.mCurrentSource == event.getSource()) {
                long time = event.getEventTime();
                finishKeys(time);
                finishTracking(time);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void finishKeys(long time) {
            cancelFling();
            sendKeyUp(time);
        }

        private void finishTracking(long time) {
            if (this.mActivePointerId >= 0) {
                this.mActivePointerId = -1;
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
        }

        private void consumeAccumulatedMovement(long time, int metaState) {
            float absX = Math.abs(this.mAccumulatedX);
            float absY = Math.abs(this.mAccumulatedY);
            if (absX >= absY) {
                if (absX >= this.mConfigTickDistance) {
                    this.mAccumulatedX = consumeAccumulatedMovement(time, metaState, this.mAccumulatedX, 21, 22);
                    this.mAccumulatedY = 0.0f;
                    this.mConsumedMovement = true;
                }
            } else if (absY >= this.mConfigTickDistance) {
                this.mAccumulatedY = consumeAccumulatedMovement(time, metaState, this.mAccumulatedY, 19, 20);
                this.mAccumulatedX = 0.0f;
                this.mConsumedMovement = true;
            }
        }

        private float consumeAccumulatedMovement(long time, int metaState, float accumulator, int negativeKeyCode, int positiveKeyCode) {
            while (accumulator <= (-this.mConfigTickDistance)) {
                sendKeyDownOrRepeat(time, negativeKeyCode, metaState);
                accumulator += this.mConfigTickDistance;
            }
            while (accumulator >= this.mConfigTickDistance) {
                sendKeyDownOrRepeat(time, positiveKeyCode, metaState);
                accumulator -= this.mConfigTickDistance;
            }
            return accumulator;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendKeyDownOrRepeat(long time, int keyCode, int metaState) {
            if (this.mPendingKeyCode != keyCode) {
                sendKeyUp(time);
                this.mPendingKeyDownTime = time;
                this.mPendingKeyCode = keyCode;
                this.mPendingKeyRepeatCount = 0;
            } else {
                this.mPendingKeyRepeatCount++;
            }
            this.mPendingKeyMetaState = metaState;
            ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 0, this.mPendingKeyCode, this.mPendingKeyRepeatCount, this.mPendingKeyMetaState, this.mCurrentDeviceId, 1024, this.mCurrentSource));
        }

        private void sendKeyUp(long time) {
            int i = this.mPendingKeyCode;
            if (i != 0) {
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 1, i, 0, this.mPendingKeyMetaState, this.mCurrentDeviceId, 0, 1024, this.mCurrentSource));
                this.mPendingKeyCode = 0;
            }
        }

        private boolean startFling(long time, float vx, float vy) {
            switch (this.mPendingKeyCode) {
                case 19:
                    if ((-vy) >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vy;
                        break;
                    } else {
                        return false;
                    }
                case 20:
                    if (vy >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vy;
                        break;
                    } else {
                        return false;
                    }
                case 21:
                    if ((-vx) >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vx;
                        break;
                    } else {
                        return false;
                    }
                    break;
                case 22:
                    if (vx >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vx;
                        break;
                    } else {
                        return false;
                    }
            }
            this.mFlinging = postFling(time);
            return this.mFlinging;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean postFling(long time) {
            float f = this.mFlingVelocity;
            if (f < this.mConfigMinFlingVelocity) {
                return false;
            }
            postAtTime(this.mFlingRunnable, time + ((long) ((this.mConfigTickDistance / f) * 1000.0f)));
            return true;
        }

        private void cancelFling() {
            if (this.mFlinging) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlinging = false;
            }
        }
    }

    @RCUnownedThisRef
    final class SyntheticKeyboardHandler {
        SyntheticKeyboardHandler() {
        }

        public void process(KeyEvent event) {
            if ((event.getFlags() & 1024) == 0) {
                KeyCharacterMap.FallbackAction fallbackAction = event.getKeyCharacterMap().getFallbackAction(event.getKeyCode(), event.getMetaState());
                if (fallbackAction != null) {
                    KeyEvent fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), null);
                    fallbackAction.recycle();
                    ViewRootImpl.this.enqueueInputEvent(fallbackEvent);
                }
            }
        }
    }

    private static boolean isNavigationKey(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyCode == 61 || keyCode == 62 || keyCode == 66 || keyCode == 92 || keyCode == 93 || keyCode == 122 || keyCode == 123) {
            return true;
        }
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return true;
            default:
                return false;
        }
    }

    private static boolean isTypingKey(KeyEvent keyEvent) {
        return keyEvent.getUnicodeChar() > 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkForLeavingTouchModeAndConsume(KeyEvent event) {
        if (!this.mAttachInfo.mInTouchMode) {
            return false;
        }
        int action = event.getAction();
        if ((action != 0 && action != 2) || (event.getFlags() & 4) != 0) {
            return false;
        }
        if (isNavigationKey(event)) {
            return ensureTouchMode(false);
        }
        if (!isTypingKey(event)) {
            return false;
        }
        ensureTouchMode(false);
        return false;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void setLocalDragState(Object obj) {
        this.mLocalDragState = obj;
    }

    private void setPendingDragEndedLoc() throws RemoteException {
        int[] loc = new int[2];
        this.mCurrentDragView.getLocationOnScreen(loc);
        int x = (int) (((float) loc[0]) + (((float) this.mCurrentDragView.getWidth()) / PENDING_DROP_VIEW_LOC_DIVIDER));
        int y = (int) (((float) loc[1]) + (((float) this.mCurrentDragView.getHeight()) / PENDING_DROP_VIEW_LOC_DIVIDER));
        Log.d(TAG, "drag view: mCurrentDragView = " + this.mCurrentDragView + ", x = " + x + ", y = " + y);
        this.mWindowSession.setPendingDragEndedLoc(this.mWindow, x, y);
    }

    /* access modifiers changed from: package-private */
    public void setLocalDragShadow(View sourceView) {
        this.mSenderView = sourceView;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDragEvent(DragEvent event) {
        boolean result;
        if (this.mView != null && this.mAdded) {
            int what = event.mAction;
            if (what == 1) {
                this.mCurrentDragView = null;
                this.mDragDescription = event.mClipDescription;
                this.mIsInBasicMode = HwPCUtils.isInBasicMode();
            } else {
                if (what == 4) {
                    this.mDragDescription = null;
                    this.mIsInBasicMode = false;
                }
                event.mClipDescription = this.mDragDescription;
            }
            if ((this.mIsInBasicMode || HwPCUtils.isInSinkWindowsCastMode()) && what == 7) {
                HwViewEx.switchDragShadow(event.getResult());
            }
            if ((this.mIsInBasicMode || HwPCUtils.isInSinkWindowsCastMode()) && what == 9) {
                HwViewEx.restoreShadow();
            }
            if (what == 6) {
                if (View.sCascadedDragDrop) {
                    this.mView.dispatchDragEnterExitInPreN(event);
                }
                setDragFocus(null, event);
            } else {
                DragEvent eventDrop = null;
                if (this.mIsInBasicMode || HwPCUtils.isInSinkWindowsCastMode()) {
                    eventDrop = DragEvent.obtain(event);
                }
                if (what == 2 || what == 3) {
                    if ((this.mIsInBasicMode || HwPCUtils.isInSinkWindowsCastMode()) && HwWindowManager.shouldSwitchDrag(this.mContext) && what == 2) {
                        Log.d(this.mTag, "call checkViewDroppableAndSwitchShadow");
                        checkViewDroppableAndSwitchShadow(event);
                    }
                    this.mDragPoint.set(event.mX, event.mY);
                    CompatibilityInfo.Translator translator = this.mTranslator;
                    if (translator != null) {
                        translator.translatePointInScreenToAppWindow(this.mDragPoint);
                    }
                    int i = this.mCurScrollY;
                    if (i != 0) {
                        this.mDragPoint.offset(0.0f, (float) i);
                    }
                    event.mX = this.mDragPoint.x;
                    event.mY = this.mDragPoint.y;
                    if (HwPCUtils.isInSinkWindowsCastMode() && what == 2) {
                        Log.d(this.mTag, "call checkAndRestoreShadow");
                        HwWindowManager.checkAndRestoreShadow(this.mContext);
                    }
                }
                View prevDragView = this.mCurrentDragView;
                if (what == 3 && event.mClipData != null) {
                    event.mClipData.prepareToEnterProcess();
                }
                if ((this.mIsInBasicMode || HwPCUtils.isInSinkWindowsCastMode()) && what == 3 && HwWindowManager.dropStartForMultiDisplay(eventDrop, event, this.mView)) {
                    Log.d(this.mTag, "drop dropStartFormultidisplay ture");
                    result = true;
                } else {
                    result = this.mView.dispatchDragEvent(event);
                }
                if (what == 2 && !event.mEventHandlerWasCalled) {
                    setDragFocus(null, event);
                }
                if (prevDragView != this.mCurrentDragView) {
                    if (prevDragView != null) {
                        try {
                            this.mWindowSession.dragRecipientExited(this.mWindow);
                        } catch (RemoteException e) {
                            Slog.e(this.mTag, "Unable to note drag target change");
                        }
                    }
                    if (this.mCurrentDragView != null) {
                        this.mWindowSession.dragRecipientEntered(this.mWindow);
                        setPendingDragEndedLoc();
                    }
                }
                if (what == 3) {
                    try {
                        String str = this.mTag;
                        Log.i(str, "Reporting drop result: " + result);
                        this.mWindowSession.reportDropResult(this.mWindow, result);
                    } catch (RemoteException e2) {
                        Log.e(this.mTag, "Unable to report drop result");
                    }
                }
                if (what == 4) {
                    this.mCurrentDragView = null;
                    setLocalDragState(null);
                    View.AttachInfo attachInfo = this.mAttachInfo;
                    attachInfo.mDragToken = null;
                    if (attachInfo.mDragSurface != null) {
                        this.mAttachInfo.mDragSurface.release();
                        this.mAttachInfo.mDragSurface = null;
                    }
                }
            }
        }
        event.recycle();
    }

    public void handleDispatchSystemUiVisibilityChanged(SystemUiVisibilityInfo args) {
        if (this.mSeq != args.seq) {
            this.mSeq = args.seq;
            this.mAttachInfo.mForceReportNewAttributes = true;
            scheduleTraversals();
        }
        if (this.mView != null) {
            if (args.localChanges != 0) {
                this.mView.updateLocalSystemUiVisibility(args.localValue, args.localChanges);
            }
            int visibility = args.globalVisibility & 7;
            if (visibility != this.mAttachInfo.mGlobalSystemUiVisibility) {
                this.mAttachInfo.mGlobalSystemUiVisibility = visibility;
                this.mView.dispatchSystemUiVisibilityChanged(visibility);
            }
        }
    }

    public void onWindowTitleChanged() {
        this.mAttachInfo.mForceReportNewAttributes = true;
    }

    public void handleDispatchWindowShown() {
        this.mAttachInfo.mTreeObserver.dispatchOnWindowShown();
    }

    public void handleRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        Bundle data = new Bundle();
        ArrayList<KeyboardShortcutGroup> list = new ArrayList<>();
        View view = this.mView;
        if (view != null) {
            view.requestKeyboardShortcuts(list, deviceId);
        }
        data.putParcelableArrayList(WindowManager.PARCEL_KEY_SHORTCUTS_ARRAY, list);
        try {
            receiver.send(0, data);
        } catch (RemoteException e) {
        }
    }

    @UnsupportedAppUsage
    public void getLastTouchPoint(Point outLocation) {
        outLocation.x = (int) this.mLastTouchPoint.x;
        outLocation.y = (int) this.mLastTouchPoint.y;
    }

    public int getLastTouchSource() {
        return this.mLastTouchSource;
    }

    public void setDragFocus(View newDragTarget, DragEvent event) {
        if (this.mCurrentDragView != newDragTarget && !View.sCascadedDragDrop) {
            float tx = event.mX;
            float ty = event.mY;
            int action = event.mAction;
            ClipData td = event.mClipData;
            event.mX = 0.0f;
            event.mY = 0.0f;
            event.mClipData = null;
            View view = this.mCurrentDragView;
            if (view != null) {
                event.mAction = 6;
                view.callDragEventHandler(event);
            }
            if (newDragTarget != null) {
                event.mAction = 5;
                newDragTarget.callDragEventHandler(event);
            }
            event.mAction = action;
            event.mX = tx;
            event.mY = ty;
            event.mClipData = td;
        }
        this.mCurrentDragView = newDragTarget;
    }

    private AudioManager getAudioManager() {
        View view = this.mView;
        if (view != null) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) view.getContext().getSystemService("audio");
            }
            return this.mAudioManager;
        }
        throw new IllegalStateException("getAudioManager called when there is no mView");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AutofillManager getAutofillManager() {
        View view = this.mView;
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup decorView = (ViewGroup) view;
        if (decorView.getChildCount() > 0) {
            return (AutofillManager) decorView.getChildAt(0).getContext().getSystemService(AutofillManager.class);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAutofillUiShowing() {
        AutofillManager afm = getAutofillManager();
        if (afm == null) {
            return false;
        }
        return afm.isAutofillUiShowing();
    }

    public AccessibilityInteractionController getAccessibilityInteractionController() {
        if (this.mView != null) {
            if (this.mAccessibilityInteractionController == null) {
                this.mAccessibilityInteractionController = new AccessibilityInteractionController(this);
            }
            return this.mAccessibilityInteractionController;
        }
        throw new IllegalStateException("getAccessibilityInteractionController called when there is no mView");
    }

    private int relayoutWindow(WindowManager.LayoutParams params, int viewVisibility, boolean insetsPending) throws RemoteException {
        boolean restore;
        long frameNumber;
        float appScale = this.mAttachInfo.mApplicationScale;
        if (params == null || this.mTranslator == null) {
            restore = false;
        } else {
            params.backup();
            this.mTranslator.translateWindowLayout(params);
            restore = true;
        }
        if (!(params == null || this.mOrigWindowType == params.type || this.mTargetSdkVersion >= 14)) {
            Slog.w(this.mTag, "Window type can not be changed after the window is added; ignoring change of " + this.mView);
            params.type = this.mOrigWindowType;
        }
        if (this.mSurface.isValid()) {
            frameNumber = this.mSurface.getNextFrameNumber();
        } else {
            frameNumber = -1;
        }
        if (DEBUG_HWFLOW) {
            Log.i(this.mTag, "relayoutWindow");
        }
        int relayoutResult = this.mWindowSession.relayout(this.mWindow, this.mSeq, params, (int) ((((float) this.mView.getMeasuredWidth()) * appScale) + 0.5f), (int) ((((float) this.mView.getMeasuredHeight()) * appScale) + 0.5f), viewVisibility, insetsPending ? 1 : 0, frameNumber, this.mTmpFrame, this.mPendingOverscanInsets, this.mPendingContentInsets, this.mPendingVisibleInsets, this.mPendingStableInsets, this.mPendingOutsets, this.mPendingBackDropFrame, this.mPendingDisplayCutout, this.mPendingMergedConfiguration, this.mSurfaceControl, this.mTempInsets);
        if (DEBUG_HWFLOW) {
            Log.i(this.mTag, "relayoutWindow relayoutResult " + relayoutResult);
        }
        if (this.mSurfaceControl.isValid()) {
            this.mSurface.copyFrom(this.mSurfaceControl);
        } else {
            destroySurface();
        }
        this.mPendingAlwaysConsumeSystemBars = (relayoutResult & 64) != 0;
        if (restore) {
            params.restore();
        }
        CompatibilityInfo.Translator translator = this.mTranslator;
        if (translator != null) {
            translator.translateRectInScreenToAppWinFrame(this.mTmpFrame);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingOverscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingContentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingVisibleInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingStableInsets);
        }
        setFrame(this.mTmpFrame);
        this.mInsetsController.onStateChanged(this.mTempInsets);
        return relayoutResult;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFrame(Rect frame) {
        this.mWinFrame.set(frame);
        this.mInsetsController.onFrameChanged(frame);
    }

    @Override // android.view.View.AttachInfo.Callbacks
    public void playSoundEffect(int effectId) {
        checkThread();
        try {
            AudioManager audioManager = getAudioManager();
            if (effectId == 0) {
                audioManager.playSoundEffect(0);
            } else if (effectId == 1) {
                audioManager.playSoundEffect(3);
            } else if (effectId == 2) {
                audioManager.playSoundEffect(1);
            } else if (effectId == 3) {
                audioManager.playSoundEffect(4);
            } else if (effectId == 4) {
                audioManager.playSoundEffect(2);
            } else {
                throw new IllegalArgumentException("unknown effect id " + effectId + " not defined in " + SoundEffectConstants.class.getCanonicalName());
            }
        } catch (IllegalStateException e) {
            String str = this.mTag;
            Log.e(str, "FATAL EXCEPTION when attempting to play sound effect: " + e);
            e.printStackTrace();
        }
    }

    @Override // android.view.View.AttachInfo.Callbacks
    public boolean performHapticFeedback(int effectId, boolean always) {
        try {
            return this.mWindowSession.performHapticFeedback(effectId, always);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.view.ViewParent
    public View focusSearch(View focused, int direction) {
        checkThread();
        if (!(this.mView instanceof ViewGroup)) {
            return null;
        }
        return FocusFinder.getInstance().findNextFocus((ViewGroup) this.mView, focused, direction);
    }

    @Override // android.view.ViewParent
    public View keyboardNavigationClusterSearch(View currentCluster, int direction) {
        checkThread();
        return FocusFinder.getInstance().findNextKeyboardNavigationCluster(this.mView, currentCluster, direction);
    }

    public void debug() {
        this.mView.debug();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.println("ViewRoot:");
        writer.print(innerPrefix);
        writer.print("mAdded=");
        writer.print(this.mAdded);
        writer.print(" mRemoved=");
        writer.println(this.mRemoved);
        writer.print(innerPrefix);
        writer.print("mConsumeBatchedInputScheduled=");
        writer.println(this.mConsumeBatchedInputScheduled);
        writer.print(innerPrefix);
        writer.print("mConsumeBatchedInputImmediatelyScheduled=");
        writer.println(this.mConsumeBatchedInputImmediatelyScheduled);
        writer.print(innerPrefix);
        writer.print("mPendingInputEventCount=");
        writer.println(this.mPendingInputEventCount);
        writer.print(innerPrefix);
        writer.print("mProcessInputEventsScheduled=");
        writer.println(this.mProcessInputEventsScheduled);
        writer.print(innerPrefix);
        writer.print("mTraversalScheduled=");
        writer.print(this.mTraversalScheduled);
        writer.print(innerPrefix);
        writer.print("mIsAmbientMode=");
        writer.print(this.mIsAmbientMode);
        if (this.mTraversalScheduled) {
            writer.print(" (barrier=");
            writer.print(this.mTraversalBarrier);
            writer.println(")");
        } else {
            writer.println();
        }
        this.mFirstInputStage.dump(innerPrefix, writer);
        this.mChoreographer.dump(prefix, writer);
        this.mInsetsController.dump(prefix, writer);
        writer.print(prefix);
        writer.println("View Hierarchy:");
        dumpViewHierarchy(innerPrefix, writer, this.mView);
    }

    private void dumpViewHierarchy(String prefix, PrintWriter writer, View view) {
        ViewGroup grp;
        int N;
        writer.print(prefix);
        if (view == null) {
            writer.println("null");
            return;
        }
        writer.println(view.toString());
        if ((view instanceof ViewGroup) && (N = (grp = (ViewGroup) view).getChildCount()) > 0) {
            String prefix2 = prefix + "  ";
            for (int i = 0; i < N; i++) {
                dumpViewHierarchy(prefix2, writer, grp.getChildAt(i));
            }
        }
    }

    public void dumpGfxInfo(int[] info) {
        info[1] = 0;
        info[0] = 0;
        View view = this.mView;
        if (view != null) {
            getGfxInfo(view, info);
        }
    }

    private static void getGfxInfo(View view, int[] info) {
        RenderNode renderNode = view.mRenderNode;
        info[0] = info[0] + 1;
        if (renderNode != null) {
            info[1] = info[1] + ((int) renderNode.computeApproximateMemoryUsage());
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                getGfxInfo(group.getChildAt(i), info);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean die(boolean immediate) {
        if (!immediate || this.mIsInTraversal) {
            if (DEBUG_HWFLOW && this.mThread != Thread.currentThread()) {
                String str = this.mTag;
                Log.d(str, "die immediate " + immediate);
            }
            if (this.mIsDrawing || this.mIsInTraversal) {
                String str2 = this.mTag;
                Log.e(str2, "Attempting to destroy the window while drawing!\n  window=" + this + ", title=" + ((Object) this.mWindowAttributes.getTitle()));
            } else {
                destroyHardwareRenderer();
            }
            this.mHandler.sendEmptyMessage(3);
            return true;
        }
        doDie();
        return false;
    }

    /* access modifiers changed from: package-private */
    public void doDie() {
        View view = this.mView;
        boolean viewVisibilityChanged = true;
        if (view != null && view.isTouchableInOtherThread()) {
            Slog.i(this.mTag, "doDie in " + this + ",CREATE IN " + this.mThread + ",DIE IN " + Thread.currentThread());
        } else {
            checkThread();
        }
        synchronized (this) {
            if (!this.mRemoved) {
                this.mRemoved = true;
                if (this.mAdded) {
                    Slog.i(TAG, "dispatchDetachedFromWindow in doDie");
                    dispatchDetachedFromWindow();
                }
                if (this.mAdded && !this.mFirst) {
                    destroyHardwareRenderer();
                    if (this.mView != null) {
                        int viewVisibility = this.mView.getVisibility();
                        if (this.mViewVisibility == viewVisibility) {
                            viewVisibilityChanged = false;
                        }
                        if (this.mWindowAttributesChanged || viewVisibilityChanged) {
                            try {
                                if ((relayoutWindow(this.mWindowAttributes, viewVisibility, false) & 2) != 0) {
                                    this.mWindowSession.finishDrawing(this.mWindow);
                                }
                            } catch (RemoteException e) {
                            }
                        }
                        destroySurface();
                    }
                }
                this.mAdded = false;
                WindowManagerGlobal.getInstance().doRemoveView(this);
            }
        }
    }

    public void requestUpdateConfiguration(Configuration config) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, config));
    }

    public void loadSystemProperties() {
        this.mHandler.post(new Runnable() {
            /* class android.view.ViewRootImpl.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                ViewRootImpl.this.mProfileRendering = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_PROFILE_RENDERING, false);
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                viewRootImpl.profileRendering(viewRootImpl.mAttachInfo.mHasWindowFocus);
                if (ViewRootImpl.this.mAttachInfo.mThreadedRenderer != null && ViewRootImpl.this.mAttachInfo.mThreadedRenderer.loadSystemProperties()) {
                    ViewRootImpl.this.invalidate();
                }
                boolean layout = DisplayProperties.debug_layout().orElse(false).booleanValue();
                if (layout != ViewRootImpl.this.mAttachInfo.mDebugLayout) {
                    ViewRootImpl.this.mAttachInfo.mDebugLayout = layout;
                    if (!ViewRootImpl.this.mHandler.hasMessages(22)) {
                        ViewRootImpl.this.mHandler.sendEmptyMessageDelayed(22, 200);
                    }
                }
            }
        });
    }

    private void destroyHardwareRenderer() {
        ThreadedRenderer hardwareRenderer = this.mAttachInfo.mThreadedRenderer;
        if (hardwareRenderer != null) {
            View view = this.mView;
            if (view != null) {
                hardwareRenderer.destroyHardwareResources(view);
            }
            hardwareRenderer.destroy();
            hardwareRenderer.setRequested(false);
            View.AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mThreadedRenderer = null;
            attachInfo.mHardwareAccelerated = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
        boolean sameProcessCall = true;
        if (this.mDragResizing && this.mUseMTRenderer) {
            boolean fullscreen = frame.equals(backDropFrame);
            synchronized (this.mWindowCallbacks) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowSizeIsChanging(backDropFrame, fullscreen, visibleInsets, stableInsets);
                }
            }
        }
        Message msg = this.mHandler.obtainMessage(reportDraw ? 5 : 4);
        CompatibilityInfo.Translator translator = this.mTranslator;
        if (translator != null) {
            translator.translateRectInScreenToAppWindow(frame);
            this.mTranslator.translateRectInScreenToAppWindow(overscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(contentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(visibleInsets);
        }
        SomeArgs args = SomeArgs.obtain();
        if (Binder.getCallingPid() != Process.myPid()) {
            sameProcessCall = false;
        }
        args.arg1 = sameProcessCall ? new Rect(frame) : frame;
        args.arg2 = sameProcessCall ? new Rect(contentInsets) : contentInsets;
        args.arg3 = sameProcessCall ? new Rect(visibleInsets) : visibleInsets;
        args.arg4 = (!sameProcessCall || mergedConfiguration == null) ? mergedConfiguration : new MergedConfiguration(mergedConfiguration);
        args.arg5 = sameProcessCall ? new Rect(overscanInsets) : overscanInsets;
        args.arg6 = sameProcessCall ? new Rect(stableInsets) : stableInsets;
        args.arg7 = sameProcessCall ? new Rect(outsets) : outsets;
        args.arg8 = sameProcessCall ? new Rect(backDropFrame) : backDropFrame;
        args.arg9 = displayCutout.get();
        args.argi1 = forceLayout ? 1 : 0;
        args.argi2 = alwaysConsumeSystemBars ? 1 : 0;
        args.argi3 = displayId;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchInsetsChanged(InsetsState insetsState) {
        this.mHandler.obtainMessage(30, insetsState).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchInsetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = insetsState;
        args.arg2 = activeControls;
        this.mHandler.obtainMessage(31, args).sendToTarget();
    }

    public void dispatchMoved(int newX, int newY) {
        if (this.mTranslator != null) {
            PointF point = new PointF((float) newX, (float) newY);
            this.mTranslator.translatePointInScreenToAppWindow(point);
            newX = (int) (((double) point.x) + 0.5d);
            newY = (int) (((double) point.y) + 0.5d);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23, newX, newY));
    }

    /* access modifiers changed from: private */
    public static final class QueuedInputEvent {
        public static final int FLAG_DEFERRED = 2;
        public static final int FLAG_DELIVER_POST_IME = 1;
        public static final int FLAG_FINISHED = 4;
        public static final int FLAG_FINISHED_HANDLED = 8;
        public static final int FLAG_MODIFIED_FOR_COMPATIBILITY = 64;
        public static final int FLAG_RESYNTHESIZED = 16;
        public static final int FLAG_UNHANDLED = 32;
        public InputEvent mEvent;
        public int mFlags;
        public QueuedInputEvent mNext;
        public InputEventReceiver mReceiver;

        private QueuedInputEvent() {
        }

        public boolean shouldSkipIme() {
            if ((this.mFlags & 1) != 0) {
                return true;
            }
            InputEvent inputEvent = this.mEvent;
            if (!(inputEvent instanceof MotionEvent) || (!inputEvent.isFromSource(2) && !this.mEvent.isFromSource(4194304))) {
                return false;
            }
            return true;
        }

        public boolean shouldSendToSynthesizer() {
            if ((this.mFlags & 32) != 0) {
                return true;
            }
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("QueuedInputEvent{flags=");
            if (!flagToString("UNHANDLED", 32, flagToString("RESYNTHESIZED", 16, flagToString("FINISHED_HANDLED", 8, flagToString("FINISHED", 4, flagToString("DEFERRED", 2, flagToString("DELIVER_POST_IME", 1, false, sb), sb), sb), sb), sb), sb)) {
                sb.append(WifiEnterpriseConfig.ENGINE_DISABLE);
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(", hasNextQueuedEvent=");
            String str = "true";
            sb2.append(this.mEvent != null ? str : "false");
            sb.append(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(", hasInputEventReceiver=");
            if (this.mReceiver == null) {
                str = "false";
            }
            sb3.append(str);
            sb.append(sb3.toString());
            sb.append(", mEvent=" + this.mEvent + "}");
            return sb.toString();
        }

        private boolean flagToString(String name, int flag, boolean hasPrevious, StringBuilder sb) {
            if ((this.mFlags & flag) == 0) {
                return hasPrevious;
            }
            if (hasPrevious) {
                sb.append("|");
            }
            sb.append(name);
            return true;
        }
    }

    private QueuedInputEvent obtainQueuedInputEvent(InputEvent event, InputEventReceiver receiver, int flags) {
        QueuedInputEvent q = this.mQueuedInputEventPool;
        if (q != null) {
            this.mQueuedInputEventPoolSize--;
            this.mQueuedInputEventPool = q.mNext;
            q.mNext = null;
        } else {
            q = new QueuedInputEvent();
        }
        q.mEvent = event;
        q.mReceiver = receiver;
        q.mFlags = flags;
        return q;
    }

    private void recycleQueuedInputEvent(QueuedInputEvent q) {
        q.mEvent = null;
        q.mReceiver = null;
        int i = this.mQueuedInputEventPoolSize;
        if (i < 10) {
            this.mQueuedInputEventPoolSize = i + 1;
            q.mNext = this.mQueuedInputEventPool;
            this.mQueuedInputEventPool = q;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void enqueueInputEvent(InputEvent event) {
        enqueueInputEvent(event, null, 0, false);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void enqueueInputEvent(InputEvent event, InputEventReceiver receiver, int flags, boolean processImmediately) {
        QueuedInputEvent q = obtainQueuedInputEvent(event, receiver, flags);
        QueuedInputEvent last = this.mPendingInputEventTail;
        if (last == null) {
            this.mPendingInputEventHead = q;
            this.mPendingInputEventTail = q;
        } else {
            last.mNext = q;
            this.mPendingInputEventTail = q;
        }
        this.mPendingInputEventCount++;
        Trace.traceCounter(4, this.mPendingInputEventQueueLengthCounterName, this.mPendingInputEventCount);
        if (processImmediately) {
            doProcessInputEvents();
        } else {
            scheduleProcessInputEvents();
        }
    }

    private void scheduleProcessInputEvents() {
        if (!this.mProcessInputEventsScheduled) {
            this.mProcessInputEventsScheduled = true;
            Message msg = this.mHandler.obtainMessage(19);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public void doProcessInputEvents() {
        while (this.mPendingInputEventHead != null) {
            QueuedInputEvent q = this.mPendingInputEventHead;
            this.mPendingInputEventHead = q.mNext;
            if (this.mPendingInputEventHead == null) {
                this.mPendingInputEventTail = null;
            }
            q.mNext = null;
            this.mPendingInputEventCount--;
            Trace.traceCounter(4, this.mPendingInputEventQueueLengthCounterName, this.mPendingInputEventCount);
            long eventTime = q.mEvent.getEventTimeNano();
            long oldestEventTime = eventTime;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent me = (MotionEvent) q.mEvent;
                if (me.getHistorySize() > 0) {
                    oldestEventTime = me.getHistoricalEventTimeNano(0);
                }
                this.mHwViewRootImpl.updateInputState(me);
                this.mHwViewRootImpl.updateOldestInputTime(oldestEventTime);
                Jlog.recordTouchState(me, oldestEventTime);
            }
            this.mChoreographer.mFrameInfo.updateInputEventTime(eventTime, oldestEventTime);
            deliverInputEvent(q);
            Jlog.clearTouchState();
        }
        if (this.mProcessInputEventsScheduled) {
            this.mProcessInputEventsScheduled = false;
            this.mHandler.removeMessages(19);
        }
    }

    private String getTraceResult(QueuedInputEvent q) {
        String traceResult;
        MotionEvent me = (MotionEvent) q.mEvent;
        int actionMasked = me.getActionMasked();
        if (actionMasked == 0) {
            traceResult = "Down";
        } else if (actionMasked == 1) {
            traceResult = "UP";
        } else if (actionMasked == 2) {
            traceResult = "Move";
        } else {
            traceResult = "action= " + actionMasked;
        }
        return traceResult + ", dt=" + (((double) (System.nanoTime() - me.getEventTimeNano())) / 1000000.0d) + "(" + me.getX() + SmsManager.REGEX_PREFIX_DELIMITER + me.getY() + ")";
    }

    private void deliverInputEvent(QueuedInputEvent q) {
        InputStage stage;
        if (DEBUG_VIEW_TRACE && (q.mEvent instanceof MotionEvent)) {
            String traceStr = getTraceResult(q);
            Trace.traceBegin(8, traceStr);
            Log.d("deliverInputEvent", "AdaptVsyncOffsetInfo " + traceStr);
            Trace.traceEnd(8);
        }
        IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl();
        if (hwRtgSchedImpl != null) {
            Trace.traceBegin(8, "DeliverInput Send");
            hwRtgSchedImpl.doDeliverInput();
            Trace.traceEnd(8);
        }
        Trace.asyncTraceBegin(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }
        IHwAppInnerBoost iHwAppInnerBoost = this.mHwAppInnerBoost;
        if (iHwAppInnerBoost != null) {
            iHwAppInnerBoost.onInputEvent(q.mEvent);
        }
        if (q.shouldSendToSynthesizer()) {
            stage = this.mSyntheticInputStage;
        } else {
            stage = q.shouldSkipIme() ? this.mFirstPostImeInputStage : this.mFirstInputStage;
        }
        if (q.mEvent instanceof KeyEvent) {
            this.mUnhandledKeyManager.preDispatch((KeyEvent) q.mEvent);
        }
        if (stage != null) {
            handleWindowFocusChanged();
            stage.deliver(q);
        } else {
            finishInputEvent(q);
        }
        this.mHwViewRootImpl.checkOldestInputTime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldKeepTouchMode(int displayId) {
        return displayId == 0 ? HwPCUtils.isInWindowsCastMode() : isValidExtDisplayId(displayId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidExtDisplayId(int displayId) {
        try {
            return HwPCUtils.isValidExtDisplayId(displayId);
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "ViewRootImpl isValidExtDisplayId NoSuchMethodError");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "ViewRootImpl isValidExtDisplayId Exception");
            return false;
        }
    }

    private boolean isValidExtDisplayId(Context context) {
        try {
            return HwPCUtils.isValidExtDisplayId(context);
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "ViewRootImpl isValidExtDisplayId NoSuchMethodError");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "ViewRootImpl isValidExtDisplayId Exception");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishInputEvent(QueuedInputEvent q) {
        Trace.asyncTraceEnd(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        if (q.mReceiver != null) {
            boolean modified = true;
            boolean handled = (q.mFlags & 8) != 0;
            if ((q.mFlags & 64) == 0) {
                modified = false;
            }
            if (modified) {
                Trace.traceBegin(8, "processInputEventBeforeFinish");
                try {
                    InputEvent processedEvent = this.mInputCompatProcessor.processInputEventBeforeFinish(q.mEvent);
                    if (processedEvent != null) {
                        q.mReceiver.finishInputEvent(processedEvent, handled);
                    }
                } finally {
                    Trace.traceEnd(8);
                }
            } else {
                q.mReceiver.finishInputEvent(q.mEvent, handled);
            }
        } else {
            q.mEvent.recycleIfNeededAfterDispatch();
        }
        recycleQueuedInputEvent(q);
    }

    static boolean isTerminalInputEvent(InputEvent event) {
        if (event instanceof KeyEvent) {
            return ((KeyEvent) event).getAction() == 1;
        }
        int action = ((MotionEvent) event).getAction();
        return action == 1 || action == 3 || action == 10;
    }

    /* access modifiers changed from: package-private */
    public void scheduleConsumeBatchedInput() {
        if (!this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = true;
            this.mChoreographer.postCallback(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void unscheduleConsumeBatchedInput() {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            this.mChoreographer.removeCallbacks(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleConsumeBatchedInputImmediately() {
        if (!this.mConsumeBatchedInputImmediatelyScheduled) {
            unscheduleConsumeBatchedInput();
            this.mConsumeBatchedInputImmediatelyScheduled = true;
            this.mHandler.post(this.mConsumeBatchedInputImmediatelyRunnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void doConsumeBatchedInput(long frameTimeNanos) {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            WindowInputEventReceiver windowInputEventReceiver = this.mInputEventReceiver;
            if (!(windowInputEventReceiver == null || !windowInputEventReceiver.consumeBatchedInputEvents(frameTimeNanos) || frameTimeNanos == -1)) {
                scheduleConsumeBatchedInput();
            }
            doProcessInputEvents();
        }
    }

    /* access modifiers changed from: package-private */
    public final class TraversalRunnable implements Runnable {
        TraversalRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            boolean viewScrollChanged = ViewRootImpl.this.mAttachInfo.mViewScrollChanged;
            ViewRootImpl.this.doTraversal();
            if (ViewRootImpl.sLastRelayoutNotifyTime != 0 || Math.abs(System.currentTimeMillis() - ViewRootImpl.sLastRelayoutNotifyTime) > ViewRootImpl.sRelayoutNotifyPeriod) {
                ViewRootImpl.dispatchWindowLayoutChange();
                ViewRootImpl.sLastRelayoutNotifyTime = System.currentTimeMillis();
            }
            ViewRootImpl.this.mHwViewRootImpl.processJank(viewScrollChanged, ViewRootImpl.this.mAttachInfo.mThreadedRenderer != null ? ViewRootImpl.this.mAttachInfo.mThreadedRenderer.getJankDrawData() : null, ViewRootImpl.this.mWindowAttributes.getTitle().toString(), ViewRootImpl.this.mWindowAttributes.type);
        }
    }

    /* access modifiers changed from: package-private */
    public final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX INFO: finally extract failed */
        @Override // android.view.InputEventReceiver
        public void onInputEvent(InputEvent event) {
            boolean isMove = false;
            if (ViewRootImpl.DEBUG_HWFLOW) {
                ViewRootImpl.this.mHwViewRootImpl.traceInputEventInfo(event);
                isMove = ViewRootImpl.this.printEventIfNeed(event);
            }
            Trace.traceBegin(8, "processInputEventForCompatibility");
            try {
                List<InputEvent> processedEvents = ViewRootImpl.this.mInputCompatProcessor.processInputEventForCompatibility(event);
                Trace.traceEnd(8);
                if (processedEvents != null) {
                    if (processedEvents.isEmpty()) {
                        finishInputEvent(event, true);
                        return;
                    }
                    for (int i = 0; i < processedEvents.size(); i++) {
                        ViewRootImpl.this.enqueueInputEvent(processedEvents.get(i), this, 64, true);
                    }
                } else if (HwFrameworkFactory.getHwViewRootImpl().shouldQueueInputEvent(event, ViewRootImpl.this.mContext, ViewRootImpl.this.mView, ViewRootImpl.this.mWindowAttributes)) {
                    ViewRootImpl.this.enqueueInputEvent(event, this, 0, true);
                } else {
                    if (ViewRootImpl.DEBUG_HWFLOW && (!isMove || ViewRootImpl.DEBUG_MOVE)) {
                        Log.i(ViewRootImpl.this.mTag, "Event not queue and finish!");
                    }
                    finishInputEvent(event, false);
                }
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }

        public long getNextFrameTimeNanos() {
            if (!ViewRootImpl.this.mHwViewRootImpl.isInputInAdvance() || ViewRootImpl.this.mUnbufferedInputDispatch) {
                return 0;
            }
            Choreographer choreographer = ViewRootImpl.this.mChoreographer;
            if (Choreographer.sIsSingleton) {
                return ViewRootImpl.this.mChoreographer.getNextFrameTimeNanos();
            }
            return 0;
        }

        public void dispatchBatchedInputEvent() {
            long nextFrameTimeNanos = getNextFrameTimeNanos();
            if (nextFrameTimeNanos > 0 && consumeBatchedInputEvents(nextFrameTimeNanos)) {
                ViewRootImpl.this.scheduleConsumeBatchedInput();
            }
        }

        @Override // android.view.InputEventReceiver
        public void onBatchedInputEventPending() {
            if (ViewRootImpl.this.mUnbufferedInputDispatch) {
                super.onBatchedInputEventPending();
            } else {
                ViewRootImpl.this.scheduleConsumeBatchedInput();
            }
        }

        @Override // android.view.InputEventReceiver
        public void dispose() {
            ViewRootImpl.this.unscheduleConsumeBatchedInput();
            super.dispose();
        }
    }

    public boolean peekEvent() {
        WindowInputEventReceiver windowInputEventReceiver = this.mInputEventReceiver;
        if (windowInputEventReceiver != null) {
            return windowInputEventReceiver.peekEvent();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final class ConsumeBatchedInputRunnable implements Runnable {
        ConsumeBatchedInputRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            long startTime = System.nanoTime();
            ViewRootImpl viewRootImpl = ViewRootImpl.this;
            viewRootImpl.doConsumeBatchedInput(viewRootImpl.mChoreographer.getFrameTimeNanos());
            ViewRootImpl.this.mHwViewRootImpl.onBatchedInputConsumed(startTime);
        }
    }

    /* access modifiers changed from: package-private */
    @RCUnownedThisRef
    public final class ConsumeBatchedInputImmediatelyRunnable implements Runnable {
        ConsumeBatchedInputImmediatelyRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            ViewRootImpl.this.doConsumeBatchedInput(-1);
        }
    }

    /* access modifiers changed from: package-private */
    @RCUnownedThisRef
    public final class InvalidateOnAnimationRunnable implements Runnable {
        private boolean mPosted;
        private View.AttachInfo.InvalidateInfo[] mTempViewRects;
        private View[] mTempViews;
        private final ArrayList<View.AttachInfo.InvalidateInfo> mViewRects = new ArrayList<>();
        private final ArrayList<View> mViews = new ArrayList<>();

        InvalidateOnAnimationRunnable() {
        }

        public void addView(View view) {
            synchronized (this) {
                this.mViews.add(view);
                postIfNeededLocked();
            }
        }

        public void addViewRect(View.AttachInfo.InvalidateInfo info) {
            synchronized (this) {
                this.mViewRects.add(info);
                postIfNeededLocked();
            }
        }

        public void removeView(View view) {
            synchronized (this) {
                this.mViews.remove(view);
                int i = this.mViewRects.size();
                while (true) {
                    int i2 = i - 1;
                    if (i <= 0) {
                        break;
                    }
                    View.AttachInfo.InvalidateInfo info = this.mViewRects.get(i2);
                    if (info.target == view) {
                        this.mViewRects.remove(i2);
                        info.recycle();
                    }
                    i = i2;
                }
                if (this.mPosted && this.mViews.isEmpty() && this.mViewRects.isEmpty()) {
                    ViewRootImpl.this.mChoreographer.removeCallbacks(1, this, null);
                    this.mPosted = false;
                }
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            int viewCount;
            int viewRectCount;
            synchronized (this) {
                this.mPosted = false;
                viewCount = this.mViews.size();
                if (viewCount != 0) {
                    this.mTempViews = (View[]) this.mViews.toArray(this.mTempViews != null ? this.mTempViews : new View[viewCount]);
                    this.mViews.clear();
                }
                viewRectCount = this.mViewRects.size();
                if (viewRectCount != 0) {
                    this.mTempViewRects = (View.AttachInfo.InvalidateInfo[]) this.mViewRects.toArray(this.mTempViewRects != null ? this.mTempViewRects : new View.AttachInfo.InvalidateInfo[viewRectCount]);
                    this.mViewRects.clear();
                }
            }
            for (int i = 0; i < viewCount; i++) {
                this.mTempViews[i].invalidate();
                this.mTempViews[i] = null;
            }
            for (int i2 = 0; i2 < viewRectCount; i2++) {
                View.AttachInfo.InvalidateInfo info = this.mTempViewRects[i2];
                info.target.invalidate(info.left, info.top, info.right, info.bottom);
                info.recycle();
            }
        }

        private void postIfNeededLocked() {
            if (!this.mPosted) {
                ViewRootImpl.this.mChoreographer.postCallback(1, this, null);
                this.mPosted = true;
            }
        }
    }

    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, view), delayMilliseconds);
    }

    public void dispatchInvalidateRectDelayed(View.AttachInfo.InvalidateInfo info, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, info), delayMilliseconds);
    }

    public void dispatchInvalidateOnAnimation(View view) {
        this.mInvalidateOnAnimationRunnable.addView(view);
    }

    public void dispatchInvalidateRectOnAnimation(View.AttachInfo.InvalidateInfo info) {
        this.mInvalidateOnAnimationRunnable.addViewRect(info);
    }

    @UnsupportedAppUsage
    public void cancelInvalidate(View view) {
        this.mHandler.removeMessages(1, view);
        this.mHandler.removeMessages(2, view);
        this.mInvalidateOnAnimationRunnable.removeView(view);
    }

    @UnsupportedAppUsage
    public void dispatchInputEvent(InputEvent event) {
        dispatchInputEvent(event, null);
    }

    @UnsupportedAppUsage
    public void dispatchInputEvent(InputEvent event, InputEventReceiver receiver) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = event;
        args.arg2 = receiver;
        Message msg = this.mHandler.obtainMessage(7, args);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    public void synthesizeInputEvent(InputEvent event) {
        Message msg = this.mHandler.obtainMessage(24, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    @UnsupportedAppUsage
    public void dispatchKeyFromIme(KeyEvent event) {
        Message msg = this.mHandler.obtainMessage(11, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    public void dispatchKeyFromAutofill(KeyEvent event) {
        Message msg = this.mHandler.obtainMessage(12, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    @UnsupportedAppUsage
    public void dispatchUnhandledInputEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            event = MotionEvent.obtain((MotionEvent) event);
        }
        synthesizeInputEvent(event);
    }

    public void dispatchAppVisibility(boolean visible) {
        Message msg = this.mHandler.obtainMessage(8);
        msg.arg1 = visible ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchGetNewSurface() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9));
    }

    public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
        synchronized (this) {
            this.mWindowFocusChanged = true;
            this.mUpcomingWindowFocus = hasFocus;
            this.mUpcomingInTouchMode = inTouchMode;
        }
        if (DEBUG_HWFLOW) {
            String str = this.mTag;
            Slog.i(str, "windowFocusChanged: hasFocus " + hasFocus + " inTouchMode " + inTouchMode);
        }
        Message msg = Message.obtain();
        msg.what = 6;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchWindowShown() {
        this.mHandler.sendEmptyMessage(25);
    }

    public void dispatchCloseSystemDialogs(String reason) {
        Message msg = Message.obtain();
        msg.what = 14;
        msg.obj = reason;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkViewDroppable(DragEvent dv) {
        if (this.mView == null) {
            Log.i(this.mTag, "checkViewDroppable mView is null!");
            return;
        }
        this.mDragPoint.set(dv.mX, dv.mY);
        CompatibilityInfo.Translator translator = this.mTranslator;
        if (translator != null) {
            translator.translatePointInScreenToAppWindow(this.mDragPoint);
        }
        int i = this.mCurScrollY;
        if (i != 0) {
            this.mDragPoint.offset(0.0f, (float) i);
        }
        float x = this.mDragPoint.x;
        float y = this.mDragPoint.y;
        this.mView.dispatchDragEvent(DragEvent.obtain(1, x, y, dv.getLocalState(), dv.getClipDescription(), null, null, true));
        boolean result = this.mView.dispatchDragEvent(DragEvent.obtain(2, x, y, dv.getLocalState(), dv.getClipDescription(), null, null, true));
        this.mView.dispatchDragEvent(DragEvent.obtain(4, x, y, dv.getLocalState(), dv.getClipDescription(), null, null, true));
        HwWindowManager.setDroppableForMultiDisplay(x, y, result);
    }

    private void checkViewDroppableAndSwitchShadow(DragEvent dv) {
        if (!HwPCUtils.ASSOCIATEASS_PACKAGE_NAME.equals(this.mContext.getBasePackageName())) {
            this.mDragPoint.set(dv.mX, dv.mY);
            CompatibilityInfo.Translator translator = this.mTranslator;
            if (translator != null) {
                translator.translatePointInScreenToAppWindow(this.mDragPoint);
            }
            int i = this.mCurScrollY;
            if (i != 0) {
                this.mDragPoint.offset(0.0f, (float) i);
            }
            boolean result = this.mView.dispatchDragEvent(DragEvent.obtain(2, this.mDragPoint.x, this.mDragPoint.y, dv.getLocalState(), dv.getClipDescription(), null, null, true));
            Log.d(TAG, "checkViewDroppableAndSwitchShadow is droppable result = " + result);
            DragEvent dven = DragEvent.obtain(7, 0.0f, 0.0f, null, null, null, null, result);
            IHwWindowManager wm = HwWindowManager.getService();
            if (wm != null) {
                try {
                    wm.notifyDragAndDropForMultiDisplay(0.0f, 0.0f, 0, dven);
                } catch (RemoteException e) {
                    Log.e(TAG, "notifyDragAndDropForMultiDisplay failed: catch RemoteException!");
                }
            }
        }
    }

    public void dispatchDragEvent(DragEvent event) {
        int what;
        if (HwPCUtils.isInWindowsCastMode() && event.mAction == 5) {
            Slog.i(TAG, "check view droppable.");
            what = 20;
        } else if (event.getAction() == 2) {
            what = 16;
            this.mHandler.removeMessages(16);
        } else {
            what = 15;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(what, event));
    }

    public void notifyDragEnterExitState(boolean isEntered, int mimeTypeSupportState, int supportItemCnt) {
        this.mSenderView.updateDragEnterExitState(isEntered, mimeTypeSupportState, supportItemCnt);
    }

    public void updatePointerIcon(float x, float y) {
        this.mHandler.removeMessages(27);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(27, MotionEvent.obtain(0, SystemClock.uptimeMillis(), 7, x, y, 0)));
    }

    public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
        SystemUiVisibilityInfo args = new SystemUiVisibilityInfo();
        args.seq = seq;
        args.globalVisibility = globalVisibility;
        args.localValue = localValue;
        args.localChanges = localChanges;
        ViewRootHandler viewRootHandler = this.mHandler;
        viewRootHandler.sendMessage(viewRootHandler.obtainMessage(17, args));
    }

    public void dispatchCheckFocus() {
        if (!this.mHandler.hasMessages(13)) {
            this.mHandler.sendEmptyMessage(13);
        }
    }

    public void dispatchRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        this.mHandler.obtainMessage(26, deviceId, 0, receiver).sendToTarget();
    }

    public void dispatchPointerCaptureChanged(boolean on) {
        this.mHandler.removeMessages(28);
        Message msg = this.mHandler.obtainMessage(28);
        msg.arg1 = on ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    private void postSendWindowContentChangedCallback(View source, int changeType) {
        if (this.mSendWindowContentChangedAccessibilityEvent == null) {
            this.mSendWindowContentChangedAccessibilityEvent = new SendWindowContentChangedAccessibilityEvent();
        }
        this.mSendWindowContentChangedAccessibilityEvent.runOrPost(source, changeType);
    }

    private void removeSendWindowContentChangedCallback() {
        SendWindowContentChangedAccessibilityEvent sendWindowContentChangedAccessibilityEvent = this.mSendWindowContentChangedAccessibilityEvent;
        if (sendWindowContentChangedAccessibilityEvent != null) {
            this.mHandler.removeCallbacks(sendWindowContentChangedAccessibilityEvent);
        }
    }

    @Override // android.view.ViewParent
    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    @Override // android.view.ViewParent
    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return false;
    }

    @Override // android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return null;
    }

    @Override // android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        return null;
    }

    @Override // android.view.ViewParent
    public void createContextMenu(ContextMenu menu) {
    }

    @Override // android.view.ViewParent
    public void childDrawableStateChanged(View child) {
    }

    @Override // android.view.ViewParent
    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        AccessibilityNodeProvider provider;
        SendWindowContentChangedAccessibilityEvent sendWindowContentChangedAccessibilityEvent;
        if (this.mView == null || this.mStopped || this.mPausedForTransition) {
            return false;
        }
        if (!(event.getEventType() == 2048 || (sendWindowContentChangedAccessibilityEvent = this.mSendWindowContentChangedAccessibilityEvent) == null || sendWindowContentChangedAccessibilityEvent.mSource == null)) {
            this.mSendWindowContentChangedAccessibilityEvent.removeCallbacksAndRun();
        }
        int eventType = event.getEventType();
        View source = getSourceForAccessibilityEvent(event);
        if (eventType == 2048) {
            handleWindowContentChangedEvent(event);
        } else if (eventType != 32768) {
            if (!(eventType != 65536 || source == null || source.getAccessibilityNodeProvider() == null)) {
                setAccessibilityFocus(null, null);
            }
        } else if (!(source == null || (provider = source.getAccessibilityNodeProvider()) == null)) {
            setAccessibilityFocus(source, provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(event.getSourceNodeId())));
        }
        if (!this.mAccessibilityManager.isEnabled()) {
            return true;
        }
        try {
            this.mAccessibilityManager.sendAccessibilityEvent(event);
            return true;
        } catch (IllegalStateException e) {
            Log.e(TAG, "Request send accessibility event error.");
            return true;
        }
    }

    private View getSourceForAccessibilityEvent(AccessibilityEvent event) {
        return AccessibilityNodeIdManager.getInstance().findView(AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId()));
    }

    private void handleWindowContentChangedEvent(AccessibilityEvent event) {
        View focusedHost = this.mAccessibilityFocusedHost;
        if (focusedHost != null && this.mAccessibilityFocusedVirtualView != null) {
            AccessibilityNodeProvider provider = focusedHost.getAccessibilityNodeProvider();
            if (provider == null) {
                this.mAccessibilityFocusedHost = null;
                this.mAccessibilityFocusedVirtualView = null;
                focusedHost.clearAccessibilityFocusNoCallbacks(0);
                return;
            }
            int changes = event.getContentChangeTypes();
            if ((changes & 1) != 0 || changes == 0) {
                int changedViewId = AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId());
                boolean hostInSubtree = false;
                View root = this.mAccessibilityFocusedHost;
                while (root != null && !hostInSubtree) {
                    if (changedViewId == root.getAccessibilityViewId()) {
                        hostInSubtree = true;
                    } else {
                        ViewParent parent = root.getParent();
                        if (parent instanceof View) {
                            root = (View) parent;
                        } else {
                            root = null;
                        }
                    }
                }
                if (hostInSubtree) {
                    int focusedChildId = AccessibilityNodeInfo.getVirtualDescendantId(this.mAccessibilityFocusedVirtualView.getSourceNodeId());
                    Rect oldBounds = this.mTempRect;
                    this.mAccessibilityFocusedVirtualView.getBoundsInScreen(oldBounds);
                    this.mAccessibilityFocusedVirtualView = provider.createAccessibilityNodeInfo(focusedChildId);
                    AccessibilityNodeInfo accessibilityNodeInfo = this.mAccessibilityFocusedVirtualView;
                    if (accessibilityNodeInfo == null) {
                        this.mAccessibilityFocusedHost = null;
                        focusedHost.clearAccessibilityFocusNoCallbacks(0);
                        provider.performAction(focusedChildId, AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
                        invalidateRectOnScreen(oldBounds);
                        return;
                    }
                    Rect newBounds = accessibilityNodeInfo.getBoundsInScreen();
                    if (!oldBounds.equals(newBounds)) {
                        oldBounds.union(newBounds);
                        invalidateRectOnScreen(oldBounds);
                    }
                }
            }
        }
    }

    @Override // android.view.ViewParent
    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
        postSendWindowContentChangedCallback((View) Preconditions.checkNotNull(source), changeType);
    }

    @Override // android.view.ViewParent
    public boolean canResolveLayoutDirection() {
        return true;
    }

    @Override // android.view.ViewParent
    public boolean isLayoutDirectionResolved() {
        return true;
    }

    @Override // android.view.ViewParent
    public int getLayoutDirection() {
        return 0;
    }

    @Override // android.view.ViewParent
    public boolean canResolveTextDirection() {
        return true;
    }

    @Override // android.view.ViewParent
    public boolean isTextDirectionResolved() {
        return true;
    }

    @Override // android.view.ViewParent
    public int getTextDirection() {
        return 1;
    }

    @Override // android.view.ViewParent
    public boolean canResolveTextAlignment() {
        return true;
    }

    @Override // android.view.ViewParent
    public boolean isTextAlignmentResolved() {
        return true;
    }

    @Override // android.view.ViewParent
    public int getTextAlignment() {
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private View getCommonPredecessor(View first, View second) {
        if (this.mTempHashSet == null) {
            this.mTempHashSet = new HashSet<>();
        }
        HashSet<View> seen = this.mTempHashSet;
        seen.clear();
        View firstCurrent = first;
        while (firstCurrent != null) {
            seen.add(firstCurrent);
            ViewParent firstCurrentParent = firstCurrent.mParent;
            if (firstCurrentParent instanceof View) {
                firstCurrent = (View) firstCurrentParent;
            } else {
                firstCurrent = null;
            }
        }
        View secondCurrent = second;
        while (secondCurrent != null) {
            if (seen.contains(secondCurrent)) {
                seen.clear();
                return secondCurrent;
            }
            ViewParent secondCurrentParent = secondCurrent.mParent;
            if (secondCurrentParent instanceof View) {
                secondCurrent = (View) secondCurrentParent;
            } else {
                secondCurrent = null;
            }
        }
        seen.clear();
        return null;
    }

    /* access modifiers changed from: package-private */
    public void checkThread() {
        if (this.mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException("Only the original thread that created a view hierarchy can touch its views.");
        }
    }

    @Override // android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @Override // android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        if (rectangle == null) {
            return scrollToRectOrFocus(null, immediate);
        }
        rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        boolean scrolled = scrollToRectOrFocus(rectangle, immediate);
        this.mTempRect.set(rectangle);
        this.mTempRect.offset(0, -this.mCurScrollY);
        this.mTempRect.offset(this.mAttachInfo.mWindowLeft, this.mAttachInfo.mWindowTop);
        try {
            this.mWindowSession.onRectangleOnScreenRequested(this.mWindow, this.mTempRect);
        } catch (RemoteException e) {
        }
        return scrolled;
    }

    @Override // android.view.ViewParent
    public void childHasTransientStateChanged(View child, boolean hasTransientState) {
    }

    @Override // android.view.ViewParent
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    @Override // android.view.ViewParent
    public void onStopNestedScroll(View target) {
    }

    @Override // android.view.ViewParent
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    }

    @Override // android.view.ViewParent
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    @Override // android.view.ViewParent
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    }

    @Override // android.view.ViewParent
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override // android.view.ViewParent
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override // android.view.ViewParent
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportNextDraw() {
        if (!this.mReportNextDraw) {
            drawPending();
        }
        this.mReportNextDraw = true;
    }

    public void setReportNextDraw() {
        reportNextDraw();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void changeCanvasOpacity(boolean opaque) {
        String str = this.mTag;
        Log.d(str, "changeCanvasOpacity: opaque=" + opaque);
        boolean opaque2 = opaque & ((this.mView.mPrivateFlags & 512) == 0);
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.setOpaque(opaque2);
        }
    }

    public boolean dispatchUnhandledKeyEvent(KeyEvent event) {
        return this.mUnhandledKeyManager.dispatch(this.mView, event);
    }

    /* access modifiers changed from: package-private */
    public class TakenSurfaceHolder extends BaseSurfaceHolder {
        TakenSurfaceHolder() {
        }

        @Override // com.android.internal.view.BaseSurfaceHolder
        public boolean onAllowLockCanvas() {
            return ViewRootImpl.this.mDrawingAllowed;
        }

        @Override // com.android.internal.view.BaseSurfaceHolder
        public void onRelayoutContainer() {
        }

        @Override // com.android.internal.view.BaseSurfaceHolder, android.view.SurfaceHolder
        public void setFormat(int format) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceFormat(format);
        }

        @Override // com.android.internal.view.BaseSurfaceHolder, android.view.SurfaceHolder
        public void setType(int type) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceType(type);
        }

        @Override // com.android.internal.view.BaseSurfaceHolder
        public void onUpdateSurface() {
            throw new IllegalStateException("Shouldn't be here");
        }

        @Override // android.view.SurfaceHolder
        public boolean isCreating() {
            return ViewRootImpl.this.mIsCreating;
        }

        @Override // com.android.internal.view.BaseSurfaceHolder, android.view.SurfaceHolder
        public void setFixedSize(int width, int height) {
            throw new UnsupportedOperationException("Currently only support sizing from layout");
        }

        @Override // android.view.SurfaceHolder
        public void setKeepScreenOn(boolean screenOn) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceKeepScreenOn(screenOn);
        }
    }

    /* access modifiers changed from: package-private */
    public static class W extends IWindow.Stub {
        private final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            this.mViewAncestor = new WeakReference<>(viewAncestor);
            this.mWindowSession = viewAncestor.mWindowSession;
        }

        @Override // android.view.IWindow
        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, backDropFrame, forceLayout, alwaysConsumeSystemBars, displayId, displayCutout);
            }
        }

        @Override // android.view.IWindow
        public void insetsChanged(InsetsState insetsState) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchInsetsChanged(insetsState);
            }
        }

        @Override // android.view.IWindow
        public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchInsetsControlChanged(insetsState, activeControls);
            }
        }

        @Override // android.view.IWindow
        public void moved(int newX, int newY) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchMoved(newX, newY);
            }
        }

        @Override // android.view.IWindow
        public void dispatchAppVisibility(boolean visible) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }

        @Override // android.view.IWindow
        public void dispatchGetNewSurface() {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchGetNewSurface();
            }
        }

        @Override // android.view.IWindow
        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.windowFocusChanged(hasFocus, inTouchMode);
            }
        }

        private static int checkCallingPermission(String permission) {
            try {
                return ActivityManager.getService().checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return -1;
            }
        }

        @Override // android.view.IWindow
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            View view;
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null && (view = viewAncestor.mView) != null) {
                if (checkCallingPermission(Manifest.permission.DUMP) == 0) {
                    OutputStream clientStream = null;
                    try {
                        clientStream = new ParcelFileDescriptor.AutoCloseOutputStream(out);
                        ViewDebug.dispatchCommand(view, command, parameters, clientStream);
                        try {
                            clientStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        if (clientStream != null) {
                            clientStream.close();
                        }
                    } catch (Throwable th) {
                        if (clientStream != null) {
                            try {
                                clientStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } else {
                    throw new SecurityException("Insufficient permissions to invoke executeCommand() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
            }
        }

        @Override // android.view.IWindow
        public void closeSystemDialogs(String reason) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchCloseSystemDialogs(reason);
            }
        }

        @Override // android.view.IWindow
        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperOffsetsComplete(asBinder());
                } catch (RemoteException e) {
                }
            }
        }

        @Override // android.view.IWindow
        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperCommandComplete(asBinder(), null);
                } catch (RemoteException e) {
                }
            }
        }

        @Override // android.view.IWindow
        public void dispatchDragEvent(DragEvent event) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchDragEvent(event);
            }
        }

        @Override // android.view.IWindow
        public void notifyDragEnterExitState(boolean isEntered, int mimeTypeSupportState, int supportItemCnt) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.notifyDragEnterExitState(isEntered, mimeTypeSupportState, supportItemCnt);
            }
        }

        @Override // android.view.IWindow
        public void updatePointerIcon(float x, float y) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updatePointerIcon(x, y);
            }
        }

        @Override // android.view.IWindow
        public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchSystemUiVisibilityChanged(seq, globalVisibility, localValue, localChanges);
            }
        }

        @Override // android.view.IWindow
        public void dispatchWindowShown() {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchWindowShown();
            }
        }

        @Override // android.view.IWindow
        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchRequestKeyboardShortcuts(receiver, deviceId);
            }
        }

        @Override // android.view.IWindow
        public void dispatchPointerCaptureChanged(boolean hasCapture) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchPointerCaptureChanged(hasCapture);
            }
        }

        @Override // android.view.IWindow
        public void registerWindowObserver(IWindowLayoutObserver observer, long period) {
            if (period <= 0) {
                Slog.e(ViewRootImpl.TAG, "registerWindowObserver with wrong period " + period);
                return;
            }
            ViewRootImpl.sRelayoutNotifyPeriod = period;
            if (ViewRootImpl.sRelayoutNotifyPeriod < ViewRootImpl.MIN_PERIOD) {
                ViewRootImpl.sRelayoutNotifyPeriod = ViewRootImpl.MIN_PERIOD;
            }
            ViewRootImpl.sLastRelayoutNotifyTime = 0;
            synchronized (ViewRootImpl.sWindowLayoutObservers) {
                ViewRootImpl.sWindowLayoutObservers.register(observer);
            }
            ViewRootImpl.dispatchWindowLayoutChange();
            ViewRootImpl.sLastRelayoutNotifyTime = System.currentTimeMillis();
        }

        @Override // android.view.IWindow
        public void unRegisterWindowObserver(IWindowLayoutObserver observer) {
            synchronized (ViewRootImpl.sWindowLayoutObservers) {
                ViewRootImpl.sWindowLayoutObservers.unregister(observer);
            }
        }

        @Override // android.view.IWindow
        public void notifyFocusChanged() {
            if (ActivityThread.getContentSensorManager() == null) {
                ActivityThread.setContentSensorManager(ContentSensorManagerFactory.createContentSensorManager(0, null));
            }
            IContentSensorManager csm = ActivityThread.getContentSensorManager();
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null && csm != null) {
                csm.notifyFocusChanged(viewAncestor.mView);
            }
        }
    }

    public static void dispatchWindowLayoutChange() {
        synchronized (sWindowLayoutObservers) {
            int i = sWindowLayoutObservers.beginBroadcast();
            while (i > 0) {
                i--;
                IWindowLayoutObserver observer = sWindowLayoutObservers.getBroadcastItem(i);
                if (observer != null) {
                    try {
                        observer.onLayoutChanged(0, 0, null);
                    } catch (RemoteException e) {
                        sWindowLayoutObservers.unregister(observer);
                        Slog.w(TAG, "dispatchWindowLayoutChange get RemoteException, remove observer " + observer);
                    }
                }
            }
            sWindowLayoutObservers.finishBroadcast();
        }
    }

    public static final class CalledFromWrongThreadException extends AndroidRuntimeException {
        @UnsupportedAppUsage
        public CalledFromWrongThreadException(String msg) {
            super(msg);
        }
    }

    static HandlerActionQueue getRunQueue() {
        HandlerActionQueue rq = sRunQueues.get();
        if (rq != null) {
            return rq;
        }
        HandlerActionQueue rq2 = new HandlerActionQueue();
        sRunQueues.set(rq2);
        return rq2;
    }

    private void startDragResizing(Rect initialBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets, int resizeMode) {
        if (!this.mDragResizing) {
            this.mDragResizing = true;
            if (this.mUseMTRenderer) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowDragResizeStart(initialBounds, fullscreen, systemInsets, stableInsets, resizeMode);
                }
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private void endDragResizing() {
        if (this.mDragResizing) {
            this.mDragResizing = false;
            if (this.mUseMTRenderer) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowDragResizeEnd();
                }
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private boolean updateContentDrawBounds() {
        boolean updated = false;
        boolean z = true;
        if (this.mUseMTRenderer) {
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                updated |= this.mWindowCallbacks.get(i).onContentDrawn(this.mWindowAttributes.surfaceInsets.left, this.mWindowAttributes.surfaceInsets.top, this.mWidth, this.mHeight);
            }
        }
        if (!this.mDragResizing || !this.mReportNextDraw) {
            z = false;
        }
        return updated | z;
    }

    private void requestDrawWindow() {
        if (this.mUseMTRenderer) {
            if (this.mReportNextDraw) {
                this.mWindowDrawCountDown = new CountDownLatch(this.mWindowCallbacks.size());
            }
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                this.mWindowCallbacks.get(i).onRequestDraw(this.mReportNextDraw);
            }
        }
    }

    public void reportActivityRelaunched() {
        this.mActivityRelaunched = true;
    }

    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }

    /* access modifiers changed from: package-private */
    public final class AccessibilityInteractionConnectionManager implements AccessibilityManager.AccessibilityStateChangeListener {
        AccessibilityInteractionConnectionManager() {
        }

        @Override // android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener
        public void onAccessibilityStateChanged(boolean enabled) {
            if (enabled) {
                ensureConnection();
                if (ViewRootImpl.this.mAttachInfo.mHasWindowFocus && ViewRootImpl.this.mView != null) {
                    ViewRootImpl.this.mView.sendAccessibilityEvent(32);
                    View focusedView = ViewRootImpl.this.mView.findFocus();
                    if (focusedView != null && focusedView != ViewRootImpl.this.mView) {
                        focusedView.sendAccessibilityEvent(8);
                        return;
                    }
                    return;
                }
                return;
            }
            ensureNoConnection();
            ViewRootImpl.this.mHandler.obtainMessage(21).sendToTarget();
        }

        public void ensureConnection() {
            if (!(ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId != -1)) {
                ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = ViewRootImpl.this.mAccessibilityManager.addAccessibilityInteractionConnection(ViewRootImpl.this.mWindow, ViewRootImpl.this.mContext.getPackageName(), new AccessibilityInteractionConnection(ViewRootImpl.this));
            }
        }

        public void ensureNoConnection() {
            if (ViewRootImpl.this.mAttachInfo != null) {
                if (ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId != -1) {
                    ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = -1;
                    ViewRootImpl.this.mAccessibilityManager.removeAccessibilityInteractionConnection(ViewRootImpl.this.mWindow);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @RCUnownedThisRef
    public final class HighContrastTextManager implements AccessibilityManager.HighTextContrastChangeListener {
        HighContrastTextManager() {
            ThreadedRenderer.setHighContrastText(ViewRootImpl.this.mAccessibilityManager.isHighTextContrastEnabled());
        }

        @Override // android.view.accessibility.AccessibilityManager.HighTextContrastChangeListener
        public void onHighTextContrastStateChanged(boolean enabled) {
            ThreadedRenderer.setHighContrastText(enabled);
            ViewRootImpl.this.destroyHardwareResources();
            ViewRootImpl.this.invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    public static final class AccessibilityInteractionConnection extends IAccessibilityInteractionConnection.Stub {
        private final WeakReference<ViewRootImpl> mViewRootImpl;

        AccessibilityInteractionConnection(ViewRootImpl viewRootImpl) {
            this.mViewRootImpl = new WeakReference<>(viewRootImpl);
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle args) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfoByAccessibilityIdClientThread(accessibilityNodeId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec, args);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setPerformAccessibilityActionResult(false, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().performAccessibilityActionClientThread(accessibilityNodeId, action, arguments, interactionId, callback, flags, interrogatingPid, interrogatingTid);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByViewIdClientThread(accessibilityNodeId, viewId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByTextClientThread(accessibilityNodeId, text, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findFocus(long accessibilityNodeId, int focusType, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findFocusClientThread(accessibilityNodeId, focusType, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void focusSearch(long accessibilityNodeId, int direction, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().focusSearchClientThread(accessibilityNodeId, direction, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void clearAccessibilityFocus() {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl != null && viewRootImpl.mView != null) {
                viewRootImpl.getAccessibilityInteractionController().clearAccessibilityFocusClientThread();
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void notifyOutsideTouch() {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl != null && viewRootImpl.mView != null) {
                viewRootImpl.getAccessibilityInteractionController().notifyOutsideTouchClientThread();
            }
        }
    }

    /* access modifiers changed from: private */
    @RCUnownedThisRef
    public class SendWindowContentChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        public long mLastEventTimeMillis;
        public StackTraceElement[] mOrigin;
        public View mSource;

        private SendWindowContentChangedAccessibilityEvent() {
            this.mChangeTypes = 0;
        }

        @Override // java.lang.Runnable
        public void run() {
            View source = this.mSource;
            this.mSource = null;
            if (source == null) {
                Log.e(ViewRootImpl.TAG, "Accessibility content change has no source");
                return;
            }
            if (AccessibilityManager.getInstance(ViewRootImpl.this.mContext).isEnabled()) {
                this.mLastEventTimeMillis = SystemClock.uptimeMillis();
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(2048);
                event.setContentChangeTypes(this.mChangeTypes);
                source.sendAccessibilityEventUnchecked(event);
            } else {
                this.mLastEventTimeMillis = 0;
            }
            source.resetSubtreeAccessibilityStateChanged();
            this.mChangeTypes = 0;
        }

        public void runOrPost(View source, int changeType) {
            if (ViewRootImpl.this.mHandler.getLooper() != Looper.myLooper()) {
                Log.e(ViewRootImpl.TAG, "Accessibility content change on non-UI thread. Future Android versions will throw an exception.", new CalledFromWrongThreadException("Only the original thread that created a view hierarchy can touch its views."));
                ViewRootImpl.this.mHandler.removeCallbacks(this);
                if (this.mSource != null) {
                    run();
                }
            }
            View view = this.mSource;
            if (view != null) {
                View predecessor = ViewRootImpl.this.getCommonPredecessor(view, source);
                if (predecessor != null) {
                    predecessor = predecessor.getSelfOrParentImportantForA11y();
                }
                this.mSource = predecessor != null ? predecessor : source;
                this.mChangeTypes |= changeType;
                return;
            }
            this.mSource = source;
            this.mChangeTypes = changeType;
            long timeSinceLastMillis = SystemClock.uptimeMillis() - this.mLastEventTimeMillis;
            long minEventIntevalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
            if (timeSinceLastMillis >= minEventIntevalMillis) {
                removeCallbacksAndRun();
            } else {
                ViewRootImpl.this.mHandler.postDelayed(this, minEventIntevalMillis - timeSinceLastMillis);
            }
        }

        public void removeCallbacksAndRun() {
            ViewRootImpl.this.mHandler.removeCallbacks(this);
            run();
        }
    }

    /* access modifiers changed from: private */
    public static class UnhandledKeyManager {
        private final SparseArray<WeakReference<View>> mCapturedKeys;
        private WeakReference<View> mCurrentReceiver;
        private boolean mDispatched;

        private UnhandledKeyManager() {
            this.mDispatched = true;
            this.mCapturedKeys = new SparseArray<>();
            this.mCurrentReceiver = null;
        }

        /* access modifiers changed from: package-private */
        public boolean dispatch(View root, KeyEvent event) {
            if (this.mDispatched) {
                return false;
            }
            try {
                Trace.traceBegin(8, "UnhandledKeyEvent dispatch");
                this.mDispatched = true;
                View consumer = root.dispatchUnhandledKeyEvent(event);
                if (event.getAction() == 0) {
                    int keycode = event.getKeyCode();
                    if (consumer != null && !KeyEvent.isModifierKey(keycode)) {
                        this.mCapturedKeys.put(keycode, new WeakReference<>(consumer));
                    }
                }
                if (consumer != null) {
                    return true;
                }
                return false;
            } finally {
                Trace.traceEnd(8);
            }
        }

        /* access modifiers changed from: package-private */
        public void preDispatch(KeyEvent event) {
            int idx;
            this.mCurrentReceiver = null;
            if (event.getAction() == 1 && (idx = this.mCapturedKeys.indexOfKey(event.getKeyCode())) >= 0) {
                this.mCurrentReceiver = this.mCapturedKeys.valueAt(idx);
                this.mCapturedKeys.removeAt(idx);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean preViewDispatch(KeyEvent event) {
            this.mDispatched = false;
            if (this.mCurrentReceiver == null) {
                this.mCurrentReceiver = this.mCapturedKeys.get(event.getKeyCode());
            }
            WeakReference<View> weakReference = this.mCurrentReceiver;
            if (weakReference == null) {
                return false;
            }
            View target = weakReference.get();
            if (event.getAction() == 1) {
                this.mCurrentReceiver = null;
            }
            if (target != null && target.isAttachedToWindow()) {
                target.onUnhandledKeyEvent(event);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean printEventIfNeed(InputEvent event) {
        if (event instanceof MotionEvent) {
            MotionEvent e = (MotionEvent) event;
            boolean isMove = false;
            if (!DEBUG_MOVE) {
                if (e.getActionMasked() == 2) {
                    if (this.mEventChanged) {
                        Log.i(TAG_INPUT_DISPATCH, "Moving, count=" + e.getPointerCount());
                        this.mEventChanged = false;
                    }
                    return true;
                }
                this.mEventChanged = true;
            }
            if (e.getX() <= 0.0f && e.getY() <= 0.0f) {
                return false;
            }
            StringBuilder sb = new StringBuilder();
            int count = e.getPointerCount();
            for (int i = 0; i < count; i++) {
                sb.append("(");
                sb.append(Math.round(e.getX(i)));
                sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
                sb.append(Math.round(e.getY(i)));
                sb.append(")");
            }
            int action = e.getAction();
            sb.append(action);
            Log.i(TAG_INPUT_DISPATCH, sb.toString());
            if (action == 2) {
                isMove = true;
            }
            return isMove;
        } else if (!(event instanceof KeyEvent)) {
            return false;
        } else {
            KeyEvent e2 = (KeyEvent) event;
            Log.i(TAG_INPUT_DISPATCH, e2.getKeyCode() + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + e2.getAction());
            return false;
        }
    }

    public void setForceNextConfigUpdate(boolean force) {
        this.mForceNextConfigUpdate = force;
    }
}
